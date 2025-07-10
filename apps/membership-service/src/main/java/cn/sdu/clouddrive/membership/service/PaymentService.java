package cn.sdu.clouddrive.membership.service;

import cn.sdu.clouddrive.membership.dto.CreatePaymentOrderRequest;
import cn.sdu.clouddrive.membership.dto.PaymentOrderDTO;
import cn.sdu.clouddrive.membership.entity.MembershipLevel;
import cn.sdu.clouddrive.membership.entity.PaymentOrder;
import cn.sdu.clouddrive.membership.mapper.PaymentOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class PaymentService extends ServiceImpl<PaymentOrderMapper, PaymentOrder> {

    @Autowired
    private MembershipLevelService membershipLevelService;
    
    @Autowired
    private UserSubscriptionService userSubscriptionService;

    public PaymentOrderDTO createPaymentOrder(CreatePaymentOrderRequest request) {
        MembershipLevel level = membershipLevelService.getById(request.getMembershipLevelId());
        if (level == null) {
            throw new RuntimeException("会员等级不存在");
        }

        // 检查用户是否可以订阅该等级
        if (!userSubscriptionService.canSubscribeToLevel(request.getUserId(), request.getMembershipLevelId())) {
            throw new RuntimeException("您当前的会员等级已包含此权限，无需重复购买");
        }

        PaymentOrder order = new PaymentOrder();
        order.setUserId(request.getUserId());
        order.setMembershipLevelId(request.getMembershipLevelId());
        order.setOrderNumber(generateOrderNumber());
        order.setAmount(level.getPrice());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus("pending");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        save(order);
        return convertToDTO(order);
    }

    public PaymentOrderDTO processPayment(String orderId, String transactionId) {
        PaymentOrder order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!"pending".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不正确");
        }

        // 更新订单状态
        order.setStatus("paid");
        order.setTransactionId(transactionId);
        order.setPaidAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        updateById(order);

        // 创建订阅记录
        userSubscriptionService.createSubscription(
            order.getUserId(),
            order.getMembershipLevelId(),
            order.getPaymentMethod(),
            order.getAmount()
        );

        return convertToDTO(order);
    }

    public List<PaymentOrderDTO> getUserPaymentOrders(String userId) {
        QueryWrapper<PaymentOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("created_at");
        
        List<PaymentOrder> orders = list(wrapper);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PaymentOrderDTO getPaymentOrder(String orderId) {
        PaymentOrder order = getById(orderId);
        return order != null ? convertToDTO(order) : null;
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "ORDER" + timestamp + random;
    }

    private PaymentOrderDTO convertToDTO(PaymentOrder order) {
        PaymentOrderDTO dto = new PaymentOrderDTO();
        BeanUtils.copyProperties(order, dto);
        
        // 获取会员等级名称
        MembershipLevel level = membershipLevelService.getById(order.getMembershipLevelId());
        if (level != null) {
            dto.setMembershipLevelName(level.getName());
        }
        
        return dto;
    }
}
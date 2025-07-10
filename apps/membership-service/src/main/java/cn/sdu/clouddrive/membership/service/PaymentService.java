package cn.sdu.clouddrive.membership.service;

import cn.sdu.clouddrive.membership.dto.CreatePaymentOrderRequest;
import cn.sdu.clouddrive.membership.dto.OrderMessage;
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
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OrderMessageProducer orderMessageProducer;

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
        
        // 发送订单消息到RabbitMQ待处理队列
        try {
            OrderMessage orderMessage = new OrderMessage(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getMembershipLevelId(),
                order.getAmount(),
                order.getPaymentMethod(),
                order.getCreatedAt(),
                order.getCreatedAt().plusDays(1) // 24小时后过期
            );
            orderMessageProducer.sendOrderMessage(orderMessage);
        } catch (Exception e) {
            // 消息发送失败不影响订单创建，记录日志
            System.err.println("Warning: Failed to send order message for order: " + order.getId() + ", error: " + e.getMessage());
        }
        
        return convertToDTO(order);
    }

    public PaymentOrderDTO createTestPaymentOrder(CreatePaymentOrderRequest request) {
        MembershipLevel level = membershipLevelService.getById(request.getMembershipLevelId());
        if (level == null) {
            throw new RuntimeException("会员等级不存在");
        }

        // 测试模式：跳过权限检查
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
        
        // 发送订单消息到RabbitMQ待处理队列
        try {
            OrderMessage orderMessage = new OrderMessage(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getMembershipLevelId(),
                order.getAmount(),
                order.getPaymentMethod(),
                order.getCreatedAt(),
                order.getCreatedAt().plusDays(1) // 24小时后过期
            );
            orderMessageProducer.sendOrderMessage(orderMessage);
        } catch (Exception e) {
            // 消息发送失败不影响订单创建，记录日志
            System.err.println("Warning: Failed to send order message for order: " + order.getId() + ", error: " + e.getMessage());
        }
        
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

        try {
            // 首先尝试创建订阅记录（这里会进行权限检查）
            userSubscriptionService.createSubscription(
                order.getUserId(),
                order.getMembershipLevelId(),
                order.getPaymentMethod(),
                order.getAmount()
            );

            // 订阅创建成功，更新订单状态
            order.setStatus("paid");
            order.setTransactionId(transactionId);
            order.setPaidAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            updateById(order);

            // 更新用户存储配额
            MembershipLevel level = membershipLevelService.getById(order.getMembershipLevelId());
            if (level != null) {
                boolean updateResult = userService.updateUserStorageQuota(order.getUserId(), level.getStorageQuota());
                if (!updateResult) {
                    // 记录警告日志，但不影响支付流程
                    System.err.println("Warning: Failed to update user storage quota for user: " + order.getUserId());
                }
            }

            // 确认订单支付，标记订单已处理（避免被自动取消）
            try {
                orderMessageProducer.confirmOrderPayment(orderId);
            } catch (Exception e) {
                System.err.println("Warning: Failed to confirm order payment for order: " + orderId + ", error: " + e.getMessage());
            }

            return convertToDTO(order);
            
        } catch (RuntimeException e) {
            // 如果创建订阅失败（比如权限检查失败），订单状态保持pending
            // 这样前端可以检测到失败并取消订单
            throw e;
        }
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

    public PaymentOrderDTO cancelPaymentOrder(String orderId) {
        PaymentOrder order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!"pending".equals(order.getStatus())) {
            throw new RuntimeException("只能取消待支付的订单");
        }

        // 更新订单状态为已取消
        order.setStatus("cancelled");
        order.setUpdatedAt(LocalDateTime.now());
        updateById(order);

        return convertToDTO(order);
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
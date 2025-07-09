package cn.sdu.clouddrive.admin.Controller;

import cn.sdu.clouddrive.admin.Service.MembershipLevelService;
import cn.sdu.clouddrive.admin.pojo.MembershipLevel;
import cn.sdu.clouddrive.admin.util.ServerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin-api/membership-levels") // 此Controller下的所有方法都以 /admin-api/membership-levels 开头
public class MembershipLevelController {

    @Autowired
    private MembershipLevelService membershipLevelService; // 注入 MembershipLevelService

    /**
     * 获取所有会员等级信息
     *
     * @return ServerResult 封装的会员等级列表
     */
    @GetMapping("/getAll")
    public ServerResult<List<MembershipLevel>> getAllMembershipLevels() {
        List<MembershipLevel> levels = membershipLevelService.getAllMembershipLevels();
        return ServerResult.ok(levels, "获取所有会员等级成功");
    }

    /**
     * 添加新的会员等级
     *
     * @param membershipLevel 包含新会员等级信息的JSON请求体
     * @return ServerResult 封装的添加结果
     */
    @PostMapping("/add")
    public ServerResult<String> addMembershipLevel(@RequestBody MembershipLevel membershipLevel) {
        // 通常，ID在插入时由数据库或业务逻辑生成，这里简单示例可以前端传递或后端生成UUID
        // 如果ID是自增的，则不需要设置
        if (membershipLevel.getId() == null || membershipLevel.getId().isEmpty()) {
            membershipLevel.setId(java.util.UUID.randomUUID().toString());
        }

        int result = membershipLevelService.addMembershipLevel(membershipLevel);
        if (result > 0) {
            return ServerResult.ok("会员等级添加成功");
        } else {
            return ServerResult.fail("会员等级添加失败");
        }
    }

    /**
     * 根据ID更新会员等级信息
     *
     * @param membershipLevel 包含更新后会员等级信息的JSON请求体
     * @return ServerResult 封装的更新结果
     */
    @PutMapping("/update")
    public ServerResult<String> updateMembershipLevel(@RequestBody MembershipLevel membershipLevel) {
        if (membershipLevel.getId() == null || membershipLevel.getId().isEmpty()) {
            return ServerResult.fail("会员等级ID不能为空");
        }

        int result = membershipLevelService.updateMembershipLevel(membershipLevel);
        if (result > 0) {
            return ServerResult.ok("会员等级更新成功");
        } else {
            return ServerResult.fail("会员等级更新失败");
        }
    }

    /**
     * 根据ID删除会员等级
     *
     * @param id 要删除的会员等级ID
     * @return ServerResult 封装的删除结果
     */
    @DeleteMapping("/delete/{id}")
    public ServerResult<String> deleteMembershipLevel(@PathVariable String id) {
        int result = membershipLevelService.deleteMembershipLevel(id);
        if (result > 0) {
            return ServerResult.ok("会员等级删除成功");
        } else {
            return ServerResult.fail("会员等级删除失败");
        }
    }
}
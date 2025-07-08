package cn.sdu.clouddrive.membership.controller;

import cn.sdu.clouddrive.membership.common.Result;
import cn.sdu.clouddrive.membership.dto.MembershipLevelDTO;
import cn.sdu.clouddrive.membership.service.MembershipLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membership")
@CrossOrigin(origins = "*")
public class MembershipController {

    @Autowired
    private MembershipLevelService membershipLevelService;

    @GetMapping("/levels")
    public Result<List<MembershipLevelDTO>> getAllLevels() {
        List<MembershipLevelDTO> levels = membershipLevelService.getAllLevels();
        return Result.success(levels);
    }

    @GetMapping("/levels/{id}")
    public Result<MembershipLevelDTO> getLevelById(@PathVariable String id) {
        MembershipLevelDTO level = membershipLevelService.getLevelById(id);
        if (level != null) {
            return Result.success(level);
        } else {
            return Result.error("会员等级不存在");
        }
    }

    @GetMapping("/levels/name/{name}")
    public Result<MembershipLevelDTO> getLevelByName(@PathVariable String name) {
        MembershipLevelDTO level = membershipLevelService.getLevelByName(name);
        if (level != null) {
            return Result.success(level);
        } else {
            return Result.error("会员等级不存在");
        }
    }
}
package cn.sdu.clouddrive.membership.service;

import cn.sdu.clouddrive.membership.dto.MembershipLevelDTO;
import cn.sdu.clouddrive.membership.entity.MembershipLevel;
import cn.sdu.clouddrive.membership.mapper.MembershipLevelMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MembershipLevelService extends ServiceImpl<MembershipLevelMapper, MembershipLevel> {

    public List<MembershipLevelDTO> getAllLevels() {
        List<MembershipLevel> levels = list();
        return levels.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MembershipLevelDTO getLevelById(String id) {
        MembershipLevel level = getById(id);
        return level != null ? convertToDTO(level) : null;
    }

    public MembershipLevelDTO getLevelByName(String name) {
        QueryWrapper<MembershipLevel> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        MembershipLevel level = getOne(wrapper);
        return level != null ? convertToDTO(level) : null;
    }

    private MembershipLevelDTO convertToDTO(MembershipLevel level) {
        MembershipLevelDTO dto = new MembershipLevelDTO();
        BeanUtils.copyProperties(level, dto);
        
        // 格式化存储配额和文件大小
        dto.setStorageQuotaFormatted(formatBytes(level.getStorageQuota()));
        dto.setMaxFileSizeFormatted(formatBytes(level.getMaxFileSize()));
        
        // 标记推荐等级
        dto.setIsRecommended("基础版".equals(level.getName()));
        
        return dto;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return (bytes / 1024) + " KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return (bytes / (1024 * 1024)) + " MB";
        } else {
            return (bytes / (1024 * 1024 * 1024)) + " GB";
        }
    }
}
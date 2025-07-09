package cn.sdu.clouddrive.admin.Service.Impl;

import cn.sdu.clouddrive.admin.Mapper.MembershipLevelMapper;
import cn.sdu.clouddrive.admin.Service.MembershipLevelService;
import cn.sdu.clouddrive.admin.pojo.MembershipLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MembershipLevelServiceImpl implements MembershipLevelService {
    @Autowired
    private MembershipLevelMapper membershipLevelMapper;

    @Override
    public List<MembershipLevel> getAllMembershipLevels() {
        return membershipLevelMapper.selectList(null);
    }

    @Override
    public int addMembershipLevel(MembershipLevel level) {
        return membershipLevelMapper.insert(level);
    }

    @Override
    public int updateMembershipLevel(MembershipLevel level) {
        return membershipLevelMapper.updateById(level);
    }

    @Override
    public int deleteMembershipLevel(String id) {
        return membershipLevelMapper.deleteById(id);
    }
}
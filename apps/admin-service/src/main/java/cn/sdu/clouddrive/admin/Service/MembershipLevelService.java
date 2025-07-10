package cn.sdu.clouddrive.admin.Service;

import cn.sdu.clouddrive.admin.pojo.MembershipLevel;

import java.util.List;

public interface MembershipLevelService {
    List<MembershipLevel> getAllMembershipLevels();
    int addMembershipLevel(MembershipLevel level);
    int updateMembershipLevel(MembershipLevel level);
    int deleteMembershipLevel(String id);

}

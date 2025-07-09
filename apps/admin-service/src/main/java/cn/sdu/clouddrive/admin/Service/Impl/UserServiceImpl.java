package cn.sdu.clouddrive.admin.Service.Impl;

import cn.sdu.clouddrive.admin.Mapper.UserMapper;
import cn.sdu.clouddrive.admin.Service.UserService;

import cn.sdu.clouddrive.admin.pojo.SummaryInfo;
import cn.sdu.clouddrive.admin.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private UserMapper userMapper;
    @Override
    public User adminLogin(String username, String password) {
        User admin = userMapper.selectAdminByUsernameAndPassword(username, password);
        return admin;
    }
    @Override
    public SummaryInfo getSummaryInfo(){
        SummaryInfo summaryInfo = new SummaryInfo();
        return summaryInfo;
    }
    @Override
    public List<User> getAllUsers(){
        return userMapper.selectList(null);
    }
    @Override
    public int deleteUserById(String id){
        return userMapper.deleteById(id);
    }
    @Override
    public int insertUser(User user){
        return userMapper.insert(user);
    }
    @Override
    public int updateUserById(User user){
        return userMapper.updateById(user);
    }

}

package cn.sdu.clouddrive.admin.Service.Impl;

import cn.sdu.clouddrive.admin.Service.FileInfoService;
import cn.sdu.clouddrive.admin.Mapper.FileInfoMapper; // Assuming you have a Mapper interface
import cn.sdu.clouddrive.admin.pojo.FileInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileInfoServiceImpl implements FileInfoService {

    @Autowired
    private FileInfoMapper fileInfoMapper; // Inject your FileInfoMapper

    @Override
    public List<FileInfo> getAllFileInfos() {
        // QueryWrapper allows building flexible queries.
        // select * from file_info
        return fileInfoMapper.selectList(null);
    }

    @Override
    public List<FileInfo> getFileInfosByUserId(String userId) {
        // select * from file_info where user_id = #{userId}
        QueryWrapper<FileInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return fileInfoMapper.selectList(queryWrapper);
    }

    @Override
    public FileInfo getFileInfoByIdAndUserId(String fileId, String userId) {
        // select * from file_info where file_id = #{fileId} and user_id = #{userId}
        QueryWrapper<FileInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("file_id", fileId);
        queryWrapper.eq("user_id", userId);
        return fileInfoMapper.selectOne(queryWrapper);
    }

    @Override
    public int addFileInfo(FileInfo fileInfo) {
        // insert into file_info (...) values (...)
        return fileInfoMapper.insert(fileInfo);
    }

    @Override
    public int updateFileInfo(FileInfo fileInfo) {
        // update file_info set ... where file_id = #{fileId} and user_id = #{userId}
        // Since FileInfo has a composite primary key, we use an UpdateWrapper for precise targeting.
        UpdateWrapper<FileInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("file_id", fileInfo.getFileId());
        updateWrapper.eq("user_id", fileInfo.getUserId());
        // The first argument `fileInfo` contains the fields to be updated.
        return fileInfoMapper.update(fileInfo, updateWrapper);
    }

    @Override
    public int deleteFileInfo(String fileId, String userId) {
        // delete from file_info where file_id = #{fileId} and user_id = #{userId}
        QueryWrapper<FileInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("file_id", fileId);
        queryWrapper.eq("user_id", userId);
        return fileInfoMapper.delete(queryWrapper);
    }

    @Override
    public Integer getTotalNormalFiles() {
        // select count(*) from file_info where del_flag = 2
        QueryWrapper<FileInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 2);
        return fileInfoMapper.selectCount(queryWrapper);
    }
}
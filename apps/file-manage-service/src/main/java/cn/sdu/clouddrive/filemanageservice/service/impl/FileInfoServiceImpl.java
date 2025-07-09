package cn.sdu.clouddrive.filemanageservice.service.impl;

import cn.sdu.clouddrive.filemanageservice.mapper.FileInfoMapper;
import cn.sdu.clouddrive.filemanageservice.po.FileInfo;
import cn.sdu.clouddrive.filemanageservice.service.FileInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileInfoServiceImpl implements FileInfoService {
    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Override
    public List<FileInfo> selectByPidAndUserId(String pid, String userId, Integer delFlag) {
        List<FileInfo> fileInfos = fileInfoMapper.selectByPidAndUserId(pid, userId, delFlag);
        return fileInfos;
    }
    @Override
    public void CopyFile(String fileId, String userId, String TargetId, String newFileId){
        fileInfoMapper.CopyFile(fileId, userId, TargetId, newFileId);
    }
    @Override
    public void RecycleFile(String fileId, String userId, Integer del_flag, String newPid){
        fileInfoMapper.RecycleFile(fileId, userId, del_flag, newPid);
    }
    @Override
    public void RenameFile(String fileId, String userId, String newName){
        fileInfoMapper.RenameFile(fileId, userId, newName);
    }
    @Override
    public void NewFolder(String fileId, String userId, String filePid, String fileName){
        fileInfoMapper.NewFolder(fileId, userId, filePid, fileName);
    }
    @Override
    public void UpdateTime(String fileId, String userId){
        fileInfoMapper.UpdateTime(fileId, userId);
    }
    @Override
    public void CreateTime(String fileId, String userId){
        fileInfoMapper.CreateTime(fileId, userId);
    }
}

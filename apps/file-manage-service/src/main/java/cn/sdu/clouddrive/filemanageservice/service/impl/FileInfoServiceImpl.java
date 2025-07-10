package cn.sdu.clouddrive.filemanageservice.service.impl;

import cn.sdu.clouddrive.filemanageservice.mapper.FileInfoMapper;
import cn.sdu.clouddrive.filemanageservice.po.FileInfo;
import cn.sdu.clouddrive.filemanageservice.service.FileInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
        // 2. 查询当前文件下的所有子文件/目录（假设delFlag=2表示正常文件）
        List<FileInfo> children = selectByPidAndUserId(fileId, userId, 2);

        // 3. 递归复制每个子文件/目录
        for (FileInfo child : children) {
            // 生成新的唯一文件ID（实际项目中建议使用UUID或数据库自增策略）
            String newChildFileId = UUID.randomUUID().toString();

            // 递归复制子文件/目录（目标父ID为当前新文件的ID）
            CopyFile(child.getFileId(), userId, newFileId, newChildFileId);
        }
    }
    @Override
    public void RecycleFile(String fileId, String userId, Integer del_flag, String newPid){
        // 1. 回收当前文件/目录
        fileInfoMapper.RecycleFile(fileId, userId, del_flag, newPid);
        Integer origin_del_flag;
        if(del_flag == 1){
            origin_del_flag = 2;
        }
        else{
            origin_del_flag = 1;
        }
        // 2. 查询当前目录下的所有子文件/目录
        List<FileInfo> children = selectByPidAndUserId(fileId, userId, origin_del_flag);

        // 3. 递归回收每个子文件/目录
        for (FileInfo child : children) {
            RecycleFile(child.getFileId(), userId, del_flag, fileId);
        }
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
    @Override
    public void clearRecycle(String userId){
        fileInfoMapper.clearRecycle(userId);
    }
    @Override
    public void deleteFile(String fileId, String userId){
        fileInfoMapper.RecycleFile(fileId, userId, 0, "0");
    }
}

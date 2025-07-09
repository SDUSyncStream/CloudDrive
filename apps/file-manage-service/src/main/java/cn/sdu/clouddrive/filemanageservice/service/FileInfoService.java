package cn.sdu.clouddrive.filemanageservice.service;

import cn.sdu.clouddrive.filemanageservice.po.FileInfo;

import java.util.List;

public interface FileInfoService {
    List<FileInfo> selectByPidAndUserId(String pid, String userId, Integer delFlag);
    void CopyFile(String fileId, String userId, String TargetId, String newFileId);
    void RecycleFile(String fileId, String userId, Integer del_flag, String newPid);
    void RenameFile(String fileId, String userId, String newName);
    void NewFolder(String fileId, String userId, String filePid, String fileName);


}

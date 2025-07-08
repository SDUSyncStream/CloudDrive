package cn.sdu.fileupdownservice.Service;

import cn.sdu.fileupdownservice.Entity.File;

import java.util.List;

public interface uploadService {
    // 检查MD5是否存在（秒传）
    File checkMd5(String md5);

    // 保存文件信息
    void saveFileInfo(File fileInfo);

    // 更新文件状态（转码结果）
    boolean updateFileStatus(String fileId, String userId, File updateInfo, Integer oldStatus);

    // 获取用户已使用空间
    Long getUserUseSpace(String userId);

    // 批量保存文件信息
    void saveFileBatch(List<File> fileInfoList);
}

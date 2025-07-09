package cn.sdu.clouddrive.admin.Service;

import cn.sdu.clouddrive.admin.pojo.FileInfo;
import java.util.List;

public interface FileInfoService {
    /**
     * 获取所有文件信息
     * @return 所有文件信息的列表
     */
    List<FileInfo> getAllFileInfos();

    /**
     * 根据用户ID获取该用户的所有文件信息
     * @param userId 用户ID
     * @return 该用户的所有文件列表
     */
    List<FileInfo> getFileInfosByUserId(String userId);

    /**
     * 根据文件ID和用户ID获取单个文件信息
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 对应的文件信息，如果不存在则返回null
     */
    FileInfo getFileInfoByIdAndUserId(String fileId, String userId);

    /**
     * 添加新的文件信息
     * @param fileInfo 要添加的文件信息对象
     * @return 影响的行数，通常为 1 表示成功
     */
    int addFileInfo(FileInfo fileInfo);

    /**
     * 更新文件信息
     * 由于是联合主键，这里通过fileId和userId来精确更新
     * @param fileInfo 包含更新字段和主键信息的文件信息对象
     * @return 影响的行数
     */
    int updateFileInfo(FileInfo fileInfo);

    /**
     * 根据文件ID和用户ID删除文件信息
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 影响的行数
     */
    int deleteFileInfo(String fileId, String userId);

    /**
     * 获取文件总数（del_flag=2正常状态的文件）
     *
     * @return 文件总数
     */
    Integer getTotalNormalFiles();
}
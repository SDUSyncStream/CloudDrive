package cn.sdu.clouddrive.fileshare.service;


import cn.sdu.clouddrive.fileshare.pojo.po.FileInfo;
import cn.sdu.clouddrive.fileshare.pojo.query.FileInfoQuery;
import cn.sdu.clouddrive.fileshare.pojo.vo.PaginationResultVO;


import java.util.List;


/**
 * 文件信息 业务接口
 */
public interface FileInfoService {

    /**
     * 根据条件查询列表
     */
    List<FileInfo> findListByParam(FileInfoQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(FileInfoQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<FileInfo> findListByPage(FileInfoQuery param);


    /**
     * 根据FileIdAndUserId查询对象
     */
    FileInfo getFileInfoByFileIdAndUserId(String fileId, String userId);


    void checkRootFilePid(String rootFilePid, String userId, String fileId);

    void saveShare(String shareRootFilePid, String shareFileIds, String myFolderId, String shareUserId, String cureentUserId);



}
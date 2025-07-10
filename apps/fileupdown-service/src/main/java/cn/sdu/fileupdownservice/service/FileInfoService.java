package cn.sdu.fileupdownservice.service;



import cn.sdu.fileupdownservice.entity.dto.SessionWebUserDto;
import cn.sdu.fileupdownservice.entity.dto.UploadResultDto;
import cn.sdu.fileupdownservice.entity.dto.UserSpaceDto;
import cn.sdu.fileupdownservice.entity.po.FileInfo;
import cn.sdu.fileupdownservice.entity.query.FileInfoQuery;
import cn.sdu.fileupdownservice.entity.vo.PaginationResultVO;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * 文件信息 业务接口
 */
public interface FileInfoService {
    public UserSpaceDto getUserSpace(@PathVariable String userId) ;
    /**
     * 根据条件查询列表
     */
    List<FileInfo> findListByParam(FileInfoQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(FileInfoQuery param);
    /**
     * 根据FileIdAndUserId查询对象
     */
    FileInfo getFileInfoByFileIdAndUserId(String fileId, String userId);


    /**
     * 根据FileIdAndUserId修改
     */
    Integer updateFileInfoByFileIdAndUserId(FileInfo bean, String fileId, String userId);


    /**
     * 根据FileIdAndUserId删除
     */

    UploadResultDto uploadFile(SessionWebUserDto webUserDto, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5, Integer chunkIndex,
                               Integer chunks);


    void delFileBatch(String userId, String fileIds, Boolean adminOp);

    void checkRootFilePid(String rootFilePid, String userId, String fileId);

    Long getUserUseSpace(@Param("userId") String userId);
    void deleteFileByUserId(@Param("userId") String userId);
}
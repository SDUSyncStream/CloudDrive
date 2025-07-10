package cn.sdu.clouddrive.fileshare.service;

import cn.sdu.clouddrive.fileshare.pojo.dto.SessionShareDto;
import cn.sdu.clouddrive.fileshare.pojo.po.FileShare;
import cn.sdu.clouddrive.fileshare.pojo.query.FileShareQuery;
import cn.sdu.clouddrive.fileshare.pojo.vo.PaginationResultVO;
import java.util.List;

public interface FileShareService {
    List<FileShare> findListByParam(FileShareQuery param);
    Integer findCountByParam(FileShareQuery param);
    PaginationResultVO<FileShare> findListByPage(FileShareQuery param);
    Integer add(FileShare bean);
    Integer addBatch(List<FileShare> listBean);
    Integer addOrUpdateBatch(List<FileShare> listBean);
    FileShare getFileShareByShareId(String shareId);
    Integer updateFileShareByShareId(FileShare bean, String shareId);
    Integer deleteFileShareByShareId(String shareId);
    void saveShare(FileShare share);
    void deleteFileShareBatch(String[] shareIdArray, String userId);
    SessionShareDto checkShareCode(String shareId, String code);
} 
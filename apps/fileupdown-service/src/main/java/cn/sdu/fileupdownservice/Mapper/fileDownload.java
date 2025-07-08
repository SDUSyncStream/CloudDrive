package cn.sdu.fileupdownservice.Mapper;

import cn.sdu.fileupdownservice.Entity.File;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.tomcat.jni.FileInfo;

import java.util.List;

public interface fileDownload extends BaseMapper<File> {
    Integer insert(@Param("bean") FileInfo fileInfo);

    /**
     * 插入或更新文件信息（存在则更新）
     */
    Integer insertOrUpdate(@Param("bean") FileInfo fileInfo);

    /**
     * 批量插入文件信息
     */
    Integer insertBatch(@Param("list") List<FileInfo> fileInfoList);

    /**
     * 根据MD5查询文件（用于秒传校验）
     */
    List<FileInfo> selectByMd5(@Param("fileMd5") String fileMd5);

    /**
     * 更新文件转码状态（带旧状态校验）
     */
    Integer updateFileStatusWithOldStatus(
            @Param("fileId") String fileId,
            @Param("userId") String userId,
            @Param("bean") FileInfo updateInfo,
            @Param("oldStatus") Integer oldStatus
    );

    /**
     * 查询用户已使用空间
     */
    Long selectUseSpace(@Param("userId") String userId);
}

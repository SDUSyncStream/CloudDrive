package cn.sdu.fileupdownservice.Mapper;

import cn.sdu.fileupdownservice.Entity.File;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


public interface fileUploadMapper extends BaseMapper<File> {
    @Select("SELECT * FROM file_info WHERE file_md5 = #{fileMd5} AND status = 2 AND del_flag = 2 LIMIT 1")
    File selectByMd5(@Param("fileMd5") String fileMd5);

    // 更新文件状态（带旧状态校验）
    @Update("<script>" +
            "UPDATE file_info " +
            "SET last_update_time = NOW() " +
            "<if test='bean.status != null'>, status = #{bean.status}</if>" +
            "<if test='bean.fileCover != null'>, file_cover = #{bean.fileCover}</if>" +
            "<if test='bean.filePath != null'>, file_path = #{bean.filePath}</if>" +
            "WHERE file_id = #{fileId} " +
            "AND user_id = #{userId} " +
            "AND status = #{oldStatus}" +
            "</script>")
    int updateFileStatusWithOldStatus(
            @Param("fileId") String fileId,
            @Param("userId") String userId,
            @Param("bean") File updateInfo,
            @Param("oldStatus") Integer oldStatus
    );

    // 查询用户已使用空间
    @Select("SELECT IFNULL(SUM(file_size), 0) FROM file_info " +
            "WHERE user_id = #{userId} AND del_flag = 2 AND status = 2 AND folder_type = 0")
    Long selectUseSpace(@Param("userId") String userId);
}

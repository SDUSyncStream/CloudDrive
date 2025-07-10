package cn.sdu.clouddrive.filemanageservice.mapper;

import cn.sdu.clouddrive.filemanageservice.po.FileInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {

    @Select("select * from file_info where user_id = #{userId} and file_pid = #{pid} and del_flag = #{delFlag} order by file_id")
    List<FileInfo> selectByPidAndUserId(String pid, String userId, Integer delFlag);

    @Insert("insert INTO file_info (file_id, user_id, file_md5, file_pid, file_size, file_name, file_cover, file_path, create_time, last_update_time, folder_type, file_category, file_type, status, recovery_time, del_flag) SELECT #{newFileId}, user_id, file_md5, #{TargetId}, file_size, #{fileName}, file_cover, file_path, create_time, last_update_time, folder_type, file_category, file_type, status, recovery_time, del_flag FROM file_info WHERE file_id = #{fileId} and user_id = #{userId};")
    void CopyFile(String fileId, String userId, String TargetId, String newFileId, String fileName);

    @Update("update file_info set del_flag = #{del_flag}, file_pid = #{newPid} where file_id = #{fileId} and user_id = #{userId}")
    void RecycleFile(String fileId, String userId, Integer del_flag, String newPid);

    @Update("update file_info set file_name = #{newName} where file_id = #{fileId} and user_id = #{userId}")
    void RenameFile(String fileId, String userId, String newName);

    @Insert("insert into file_info (file_id, user_id, file_pid, file_name, folder_type, del_flag) values(#{fileId}, #{userId}, #{filePid}, #{fileName}, 1, 2)")
    void NewFolder(String fileId, String userId, String filePid, String fileName);

    @Update("update file_info set last_update_time = NOW() where file_id = #{fileId} and user_id = #{userId}")
    void UpdateTime(String fileId, String userId);

    @Update("update file_info set create_time = NOW() where file_id = #{fileId} and user_id = #{userId}")
    void CreateTime(String fileId, String userId);

    @Update("update file_info set del_flag = 0 where del_flag = 1 and user_id = #{userId}")
    void clearRecycle(String userId);

    @Select("select * from file_info where user_id = #{userId} and file_id = #{fileId} order by file_id")
    List<FileInfo> getFile(String fileId, String userId);
}

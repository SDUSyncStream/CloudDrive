package cn.sdu.clouddrive.admin.Mapper;

import cn.sdu.clouddrive.admin.pojo.FileInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件信息Mapper接口
 * 继承BaseMapper，提供基本的CRUD功能
 * 注意：对于联合主键，BaseMapper的updateById/deleteById可能不适用，
 * 通常需要使用QueryWrapper或手写SQL。
 */
@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {
    // 可以在这里定义任何自定义的SQL查询方法
    // 例如：
    // FileInfo selectFileInfoByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);
}
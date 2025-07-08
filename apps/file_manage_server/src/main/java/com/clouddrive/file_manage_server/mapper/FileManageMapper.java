package com.clouddrive.file_manage_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clouddrive.file_manage_server.po.FileInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件信息Mapper接口
 */
@Mapper
public interface FileManageMapper extends BaseMapper<FileInfo> {

}

package com.clouddrive.file_manage_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.clouddrive.file_manage_server.mapper.FileManageMapper;
import com.clouddrive.file_manage_server.po.FileInfo;
import com.clouddrive.file_manage_server.service.FileManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文件信息服务实现类
 */
@Service
public class FileManageServiceImpl implements FileManageService {

    @Autowired
    private FileManageMapper FileManageMapper;

    @Override
    public List<FileInfo> selectByUserId(Integer userId) {
        // 创建查询条件
        QueryWrapper queryWrapper = new QueryWrapper();
        // 设置查询条件 eq()表示等于 发送的SQL语句：select * from item where category_id = ?
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("del_flag", 2);
        queryWrapper.orderByDesc("create_time");
        List<FileInfo> fileInfoList = FileManageMapper.selectList(queryWrapper);
        return fileInfoList;
    }

    @Override
    public FileInfo findByMd5(String md5) {
        // 创建查询条件
        QueryWrapper queryWrapper = new QueryWrapper();
        // 设置查询条件 eq()表示等于 发送的SQL语句：select * from item where file_md5 = ?
        queryWrapper.eq("file_md5", md5);
        FileInfo fileInfo = FileManageMapper.selectOne(queryWrapper);
    }

    @Override
    public FileInfo findByUserIdAnd(String md5) {
        // 创建查询条件
        QueryWrapper queryWrapper = new QueryWrapper();
        // 设置查询条件 eq()表示等于 发送的SQL语句：select * from item where file_md5 = ?
        queryWrapper.eq("file_md5", md5);
        FileInfo fileInfo = FileManageMapper.selectOne(queryWrapper);
    }

    @Override
    public List<FileInfo> findByPid(String pid) {
        QueryWrapper<FileInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("file_pid", pid)
                .eq("del_flag", 2)
                .orderByDesc("folder_type"); // 目录在前
        return this.list(wrapper);
    }



    @Override
    public Long getUsedSpace(String userId) {
        QueryWrapper<FileInfo> wrapper = new QueryWrapper<>();
        wrapper.select("SUM(file_size) as totalSize")
                .eq("user_id", userId)
                .eq("del_flag", 2)
                .eq("folder_type", 0); // 只计算文件
        FileInfo fileInfo = this.getOne(wrapper);
        return fileInfo != null ? fileInfo.getFileSize() : 0L;
    }
}
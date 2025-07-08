package com.clouddrive.file_manage_server.service;

import com.clouddrive.file_manage_server.po.FileInfo;

import java.util.List;

public interface FileManageService {

    List<FileInfo> selectByUserId(Integer userId);

    FileInfo findByMd5(String md5);
}



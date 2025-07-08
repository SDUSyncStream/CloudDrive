package cn.sdu.fileupdownservice.Service.Impl;

import cn.sdu.fileupdownservice.Entity.File;
import cn.sdu.fileupdownservice.Entity.FileStatusEnum;
import cn.sdu.fileupdownservice.Mapper.fileUploadMapper;
import cn.sdu.fileupdownservice.Service.uploadService;
import cn.sdu.fileupdownservice.Service.userService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class uploadServiceImpl extends ServiceImpl<fileUploadMapper, File> implements uploadService {


        @Resource
        private fileUploadMapper fileInfoMapper;

        @Resource
        private userService userInfoService;

        @Override
        public File checkMd5(String md5) {
            QueryWrapper<File> wrapper = new QueryWrapper<>();
            wrapper.eq("file_md5", md5)
                    .eq("status", FileStatusEnum.TRANSCODE_SUCCESS.getStatus())
                    .eq("del_flag", FileStatusEnum.NORMAL.getStatus());
            return getOne(wrapper);
        }

        @Override
        @Transactional
        public void saveFileInfo(File fileInfo) {
            // 设置默认值
            if (fileInfo.getCreateTime() == null) {
                fileInfo.setCreateTime(LocalDateTime.now());
            }
            if (fileInfo.getLastUpdateTime() == null) {
                fileInfo.setLastUpdateTime(LocalDateTime.now());
            }
            if (fileInfo.getFolderType() == null) {
                fileInfo.setFolderType(0); // 默认为文件
            }
            if (fileInfo.getStatus() == null) {
                fileInfo.setStatus(FileStatusEnum.TRANSCODING.getStatus());
            }
            if (fileInfo.getDelFlag() == null) {
                fileInfo.setDelFlag(FileStatusEnum.NORMAL.getStatus());
            }

            // 保存文件信息
            save(fileInfo);

            // 更新用户空间使用量
            updateUserSpace(fileInfo.getUserId());
        }

        @Override
        @Transactional
        public boolean updateFileStatus(String fileId, String userId, File updateInfo, Integer oldStatus) {
            int count = fileInfoMapper.updateFileStatusWithOldStatus(
                    fileId, userId, updateInfo, oldStatus
            );

            if (count > 0 && updateInfo.getStatus() == FileStatusEnum.TRANSCODE_SUCCESS.getStatus()) {
                // 转码成功后更新用户空间
                updateUserSpace(userId);
            }

            return count > 0;
        }

        @Override
        public Long getUserUseSpace(String userId) {
            return fileInfoMapper.selectUseSpace(userId);
        }

        @Override
        @Transactional
        public void saveFileBatch(List<File> fileInfoList) {
            if (fileInfoList == null || fileInfoList.isEmpty()) {
                return;
            }

            // 批量保存
            saveBatch(fileInfoList);

            // 更新用户空间（取第一个文件的用户ID）
            String userId = fileInfoList.get(0).getUserId();
            updateUserSpace(userId);
        }

        // 更新用户空间使用量
        private void updateUserSpace(String userId) {
            Long useSpace = getUserUseSpace(userId);
            userInfoService.updateUserSpace(userId, useSpace);
        }

        // 其他辅助方法...
}



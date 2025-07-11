package cn.sdu.fileupdownservice.component;


import cn.sdu.fileupdownservice.entity.constants.Constants;
import cn.sdu.fileupdownservice.entity.dto.DownloadFileDto;
import cn.sdu.fileupdownservice.entity.dto.SysSettingsDto;
import cn.sdu.fileupdownservice.entity.dto.UserSpaceDto;
import cn.sdu.fileupdownservice.entity.po.FileInfo;
import cn.sdu.fileupdownservice.entity.po.Users;
import cn.sdu.fileupdownservice.entity.query.FileInfoQuery;
import cn.sdu.fileupdownservice.entity.query.UserInfoQuery;
import cn.sdu.fileupdownservice.mappers.FileInfoMapper;
import cn.sdu.fileupdownservice.mappers.UserInfoMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;


@Component("redisComponent")
public class RedisComponent {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private UserInfoMapper<Users, UserInfoQuery> userInfoMapper;

    @Resource
    private FileInfoMapper<FileInfo, FileInfoQuery> fileInfoMapper;

    /**
     * 获取系统设置
     *
     * @return
     */
    public SysSettingsDto getSysSettingsDto() {
        SysSettingsDto sysSettingsDto = (SysSettingsDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if (sysSettingsDto == null) {
            sysSettingsDto = new SysSettingsDto();
            redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingsDto);
        }
        return sysSettingsDto;
    }

    /**
     * 保存设置
     *
     * @param sysSettingsDto
     */

    public void saveSysSettingsDto(SysSettingsDto sysSettingsDto) {
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingsDto);
    }

    public void saveDownloadCode(String code, DownloadFileDto downloadFileDto) {
        redisUtils.setex(Constants.REDIS_KEY_DOWNLOAD + code, downloadFileDto, Constants.REDIS_KEY_EXPIRES_FIVE_MIN);
    }

    public DownloadFileDto getDownloadCode(String code) {
        return (DownloadFileDto) redisUtils.get(Constants.REDIS_KEY_DOWNLOAD + code);
    }


    /**
     * 获取用户使用的空间
     *
     * @param userId
     * @return
     */
    public UserSpaceDto getUserSpaceUse(String userId) {
        UserSpaceDto spaceDto = (UserSpaceDto) redisUtils.get(Constants.REDIS_KEY_USER_SPACE_USE + userId);
        if (null == spaceDto) {
            resetUserSpaceUse(userId);
            spaceDto = new UserSpaceDto();
            Long useSpace = this.fileInfoMapper.selectUseSpace(userId);
            spaceDto.setUseSpace(useSpace);
            System.out.println("用户空间"+useSpace);
            spaceDto.setTotalSpace(getSysSettingsDto().getUserInitUseSpace() * Constants.MB);
            System.out.println("用户总空间"+spaceDto.getTotalSpace());
            redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USE + userId, spaceDto, Constants.REDIS_KEY_EXPIRES_FIVE_MIN);
        }
        return spaceDto;
    }

    /**
     * 保存已使用的空间
     *
     * @param userId
     */
    public void saveUserSpaceUse(String userId, UserSpaceDto userSpaceDto) {
        redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USE + userId, userSpaceDto, Constants.REDIS_KEY_EXPIRES_FIVE_MIN);
    }

    public UserSpaceDto resetUserSpaceUse(String userId) {
        UserSpaceDto spaceDto = new UserSpaceDto();
        Long useSpace = this.fileInfoMapper.selectUseSpace(userId);
        spaceDto.setUseSpace(useSpace);

        Users users = this.userInfoMapper.selectUser(userId);
        spaceDto.setTotalSpace(users.getTotalSpace());
        redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USE + userId, spaceDto, Constants.REDIS_KEY_EXPIRES_FIVE_MIN);
        return spaceDto;
    }

    //保存文件临时大小
    public void saveFileTempSize(String userId, String fileId, Long fileSize) {
        Long currentSize = getFileTempSize(userId, fileId);
        redisUtils.setex(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE + userId + fileId, currentSize + fileSize, Constants.REDIS_KEY_EXPIRES_FIVE_MIN);
    }

    public Long getFileTempSize(String userId, String fileId) {
        Long currentSize = getFileSizeFromRedis(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE + userId + fileId);
        return currentSize;
    }

    private Long getFileSizeFromRedis(String key) {
        Object sizeObj = redisUtils.get(key);
        if (sizeObj == null) {
            return 0L;
        }
        if (sizeObj instanceof Integer) {
            return ((Integer) sizeObj).longValue();
        } else if (sizeObj instanceof Long) {
            return (Long) sizeObj;
        }

        return 0L;
    }
}

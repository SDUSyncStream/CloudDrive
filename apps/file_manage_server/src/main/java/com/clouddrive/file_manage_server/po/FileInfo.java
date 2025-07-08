package com.clouddrive.file_manage_server.po;

package com.example.clouddrive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件信息表
 */
@Data
@TableName("file_info")
public class FileInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    @TableId(value = "file_id", type = IdType.ASSIGN_ID)
    private String fileId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 文件MD5
     */
    private String fileMd5;

    /**
     * 父文件ID
     */
    private String filePid;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件封面
     */
    private String fileCover;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后更新时间
     */
    private Date lastUpdateTime;

    /**
     * 文件夹类型 0-文件 1-目录
     */
    private Integer folderType;

    /**
     * 文件分类 1-视频 2-音频 3-图片 4-文档 5-其他
     */
    private Integer fileCategory;

    /**
     * 文件类型 1-视频 2-音频 3-图片 4-pdf 5-doc 6-excel 7-txt 8-code 9-zip 10-其他
     */
    private Integer fileType;

    /**
     * 状态 0-转码中 1-转码失败 2-转码成功
     */
    private Integer status;

    /**
     * 进入回收站时间
     */
    private Date recoveryTime;

    /**
     * 删除标志 0-删除 1-回收站 2-正常
     */
    private Integer delFlag;
}

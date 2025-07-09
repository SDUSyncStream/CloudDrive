package cn.sdu.clouddrive.admin.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data // Lombok 注解，自动生成 getter, setter, toString, equals, hashCode 方法
@TableName("file_info") // 指定实体类对应的数据库表名
public class FileInfo {

    @TableId(value = "file_id") // 复合主键的第一部分
    private String fileId; // 数据库是 varchar(10)

    @TableField("user_id") // 复合主键的第二部分，也作为普通字段映射
    private String userId; // 数据库是 varchar(10)

    @TableField("file_md5")
    private String fileMd5; // 数据库是 varchar(32)

    @TableField("file_pid")
    private String filePid; // 数据库是 varchar(10)

    @TableField("file_size")
    private Long fileSize; // 数据库是 bigint，对应Java的Long

    @TableField("file_name")
    private String fileName; // 数据库是 varchar(200)

    @TableField("file_cover")
    private String fileCover; // 数据库是 varchar(100)

    @TableField("file_path")
    private String filePath; // 数据库是 varchar(100)

    @TableField("create_time")
    private LocalDateTime createTime; // 数据库是 datetime，对应Java 8的LocalDateTime

    @TableField("last_update_time")
    private LocalDateTime lastUpdateTime; // 数据库是 datetime，对应Java 8的LocalDateTime

    @TableField("folder_type")
    private Integer folderType; // 数据库是 tinyint(1)，0文件1目录

    @TableField("file_category")
    private Integer fileCategory; // 数据库是 tinyint(1)，1视频2音频3图片4文档5其他

    @TableField("file_type")
    private Integer fileType; // 数据库是 tinyint，1视频2音频3图片4pdf5doc6excel7txt8code9zip10其他

    @TableField("status")
    private Integer status; // 数据库是 tinyint，0转码中1转码失败2转码成功

    @TableField("recovery_time")
    private LocalDateTime recoveryTime; // 数据库是 datetime，进入回收站时间

    @TableField("del_flag")
    private Integer delFlag; // 数据库是 tinyint(1)，0删除1回收站2正常
}
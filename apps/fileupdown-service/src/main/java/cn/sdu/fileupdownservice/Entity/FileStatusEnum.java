package cn.sdu.fileupdownservice.Entity;

public enum FileStatusEnum {
    TRANSCODING(0, "转码中"),
    TRANSCODE_FAILED(1, "转码失败"),
    TRANSCODE_SUCCESS(2, "转码成功"),

    DELETED(0, "已删除"),
    RECYCLE(1, "回收站"),
    NORMAL(2, "正常");

    private final Integer status;
    private final String desc;

    FileStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}

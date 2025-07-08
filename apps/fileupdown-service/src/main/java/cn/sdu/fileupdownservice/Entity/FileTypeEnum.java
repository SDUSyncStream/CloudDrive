package cn.sdu.fileupdownservice.Entity;

public enum FileTypeEnum {
    VIDEO(1, "视频"),
    AUDIO(2, "音频"),
    IMAGE(3, "图片"),
    PDF(4, "PDF"),
    DOC(5, "DOC"),
    EXCEL(6, "Excel"),
    TXT(7, "TXT"),
    CODE(8, "代码"),
    ZIP(9, "压缩包"),
    OTHER(10, "其他");

    private final Integer type;
    private final String desc;

    FileTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}

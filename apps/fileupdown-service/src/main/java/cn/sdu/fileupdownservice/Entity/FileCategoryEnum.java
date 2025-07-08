package cn.sdu.fileupdownservice.Entity;

public enum FileCategoryEnum {
    VIDEO(1, "视频"),
    AUDIO(2, "音频"),
    IMAGE(3, "图片"),
    DOC(4, "文档"),
    OTHER(5, "其他");

    private final Integer category;
    private final String desc;

    FileCategoryEnum(Integer category, String desc) {
        this.category = category;
        this.desc = desc;
    }

    public Integer getCategory() {
        return category;
    }

    public String getDesc() {
        return desc;
    }
}

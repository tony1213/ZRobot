package com.robot.et.entity;

/**
 * Created by houdeming on 2016/9/12.
 * 图片信息
 */
public class PictureInfo {
    private int picId;
    private String picUUid;
    private String robotNum;
    private String picName;
    private String picPath;
    private String createTime;
    private String spareContent;
    private String spareContent2;

    public int getPicId() {
        return picId;
    }

    public void setPicId(int picId) {
        this.picId = picId;
    }

    public String getPicUUid() {
        return picUUid;
    }

    public void setPicUUid(String picUUid) {
        this.picUUid = picUUid;
    }

    public String getRobotNum() {
        return robotNum;
    }

    public void setRobotNum(String robotNum) {
        this.robotNum = robotNum;
    }

    public String getPicName() {
        return picName;
    }

    public void setPicName(String picName) {
        this.picName = picName;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getSpareContent() {
        return spareContent;
    }

    public void setSpareContent(String spareContent) {
        this.spareContent = spareContent;
    }

    public String getSpareContent2() {
        return spareContent2;
    }

    public void setSpareContent2(String spareContent2) {
        this.spareContent2 = spareContent2;
    }

    public PictureInfo() {
        super();
    }
}

package com.robot.et.entity;

/**
 * Created by houdeming on 2016/8/22.
 * 视觉认识环境
 */
public class VisionRecogniseEnvironmentInfo {
    private String robotNum;
    private String familyName;
    private String positionName;
    private String positionX;
    private String positionY;
    private int spareType;
    private String spareContent;
    private String spareContent2;
    private String spareContent3;

    public String getRobotNum() {
        return robotNum;
    }

    public void setRobotNum(String robotNum) {
        this.robotNum = robotNum;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getPositionX() {
        return positionX;
    }

    public void setPositionX(String positionX) {
        this.positionX = positionX;
    }

    public String getPositionY() {
        return positionY;
    }

    public void setPositionY(String positionY) {
        this.positionY = positionY;
    }

    public int getSpareType() {
        return spareType;
    }

    public void setSpareType(int spareType) {
        this.spareType = spareType;
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

    public String getSpareContent3() {
        return spareContent3;
    }

    public void setSpareContent3(String spareContent3) {
        this.spareContent3 = spareContent3;
    }

    public VisionRecogniseEnvironmentInfo() {
        super();
    }
}

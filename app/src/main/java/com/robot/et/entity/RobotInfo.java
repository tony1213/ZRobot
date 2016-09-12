package com.robot.et.entity;

// 机器人信息
public class RobotInfo {
    private String robotUUId;// 机器人uuid
    private String robotPic;// 机器人图片
    private String robotNum;// 机器人编号
    private String robotName;// 机器人名字
    private String adminPhone;// 该机器人的管理员手机号

    public String getRobotUUId() {
        return robotUUId;
    }

    public void setRobotUUId(String robotUUId) {
        this.robotUUId = robotUUId;
    }

    public String getRobotPic() {
        return robotPic;
    }

    public void setRobotPic(String robotPic) {
        this.robotPic = robotPic;
    }

    public String getRobotNum() {
        return robotNum;
    }

    public void setRobotNum(String robotNum) {
        this.robotNum = robotNum;
    }

    public String getRobotName() {
        return robotName;
    }

    public void setRobotName(String robotName) {
        this.robotName = robotName;
    }

    public String getAdminPhone() {
        return adminPhone;
    }

    public void setAdminPhone(String adminPhone) {
        this.adminPhone = adminPhone;
    }

    public RobotInfo() {
        super();
    }

}

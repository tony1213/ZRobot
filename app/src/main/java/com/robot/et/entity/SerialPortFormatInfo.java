package com.robot.et.entity;

/**
 * Created by houdeming on 2016/9/27.
 * 串口发送控制的数据格式
 */
public class SerialPortFormatInfo {
    private String act;// 当前指令的行为
    private String cmd;// 运动方向
    private int aim;// 目标速度
    private int tim;// 运动执行时间
    private int rad;// 运动半径

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public int getTim() {
        return tim;
    }

    public void setTim(int tim) {
        this.tim = tim;
    }

    public int getAim() {
        return aim;
    }

    public void setAim(int aim) {
        this.aim = aim;
    }

    public int getRad() {
        return rad;
    }

    public void setRad(int rad) {
        this.rad = rad;
    }

    public SerialPortFormatInfo() {
        super();
    }
}

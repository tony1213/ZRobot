package com.robot.et.entity;

/**
 * Created by houdeming on 2016/8/26.
 * 串口接受数据
 * {"rdl":0,"rdm":0,"rdr":0,"xf":1,"xag":20,"hw":1}
 */
public class SerialPortReceiverInfo {
    private String act;
    private String cmd;
    private int dataL;// 雷达左边
    private int dataM;// 雷达中间
    private int dataR;// 雷达右边

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

    public int getDataL() {
        return dataL;
    }

    public void setDataL(int dataL) {
        this.dataL = dataL;
    }

    public int getDataM() {
        return dataM;
    }

    public void setDataM(int dataM) {
        this.dataM = dataM;
    }

    public int getDataR() {
        return dataR;
    }

    public void setDataR(int dataR) {
        this.dataR = dataR;
    }

    public SerialPortReceiverInfo() {
        super();
    }
}

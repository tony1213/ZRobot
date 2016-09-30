package com.robot.et.common.enums;

// 控制动的指令
public enum ControlMoveEnum {

    FORWARD(1, "前进"),
    FORWARD2(1, "向前"),
    FORWARD3(1, "过来"),
    TURN_AFTER(6, "后转"),
    TURN_AFTER2(6, "后传"),
    TURN_AFTER3(6, "向后"),
    BACKWARD(2, "后退"),
    BACKWARD2(2, "再退"),
    LEFT(3, "左转"),
    LEFT2(3, "向左"),
    RIGHT(4, "右转"),
    RIGHT2(4, "向右"),
    STOP(5, "停止"),
    STOP2(5, "停");

    // 动作指令的key，当控制运动时所要传递的动作key值
    private int moveKey;
    // 所代表的具体动作说明
    private String moveName;

    ControlMoveEnum(int moveKey, String moveName) {
        this.moveKey = moveKey;
        this.moveName = moveName;
    }

    public int getMoveKey() {
        return moveKey;
    }

    public String getMoveName() {
        return moveName;
    }

}

package com.robot.et.core.software.ros.move;

import org.ros.internal.message.Message;

/**
 * Created by Tony on 2016/8/23.
 */
public interface MoveRequest extends Message {
    String _TYPE = "com.robot.et.core.software.ros.move/MoveRequest";
    String _DEFINITION = "string frame\nfloat32 x\nfloat32 y\nfloat32 angle\n";

    String getFrame();

    void setFrame(String var1);

    float getX();

    void setX(float var1);

    float getY();

    void setY(float var1);

    float getAngle();

    void setAngle(float var1);
}
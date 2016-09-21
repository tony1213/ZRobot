package com.robot.et.core.software.ros.client.move;

import org.ros.internal.message.Message;

/**
 * Created by Tony on 2016/8/23.
 */
public interface MoveResponse extends Message {
    String _TYPE = "com.robot.et.core.software.ros.client.move/MoveResponse";
    String _DEFINITION = "string status\n";

    String getStatus();

    void setStatus(String var1);
}

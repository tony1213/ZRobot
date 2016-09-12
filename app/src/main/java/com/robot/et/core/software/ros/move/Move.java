package com.robot.et.core.software.ros.move;

import org.ros.internal.message.Message;

/**
 * Created by Tony on 2016/8/23.
 */
public interface Move extends Message {
    String _TYPE = "com.robot.et.core.software.ros.move/Move";
    String _DEFINITION = "string frame\nfloat32 x\nfloat32 y\nfloat32 angle\n---\nstring status\n";
}

package com.robot.et.core.software.ros.visual;

import org.ros.internal.message.Message;

/**
 * Created by Tony on 2016/8/23.
 */

public interface VisualResponse extends Message {
    String _TYPE = "com.robot.et.core.software.ros.visual/VisualResponse";
    String _DEFINITION = "int64 result\nstring name\n";

    long getResult();

    void setResult(long var1);

    String getName();

    void setName(String var1);
}

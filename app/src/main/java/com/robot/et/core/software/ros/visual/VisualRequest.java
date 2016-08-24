package com.robot.et.core.software.ros.visual;

import org.ros.internal.message.Message;

/**
 * Created by Tony on 2016/8/23.
 */

public interface VisualRequest extends Message {
    String _TYPE = "com.robot.et.core.software.ros.visual/VisualRequest";
    String _DEFINITION = "int64 id\nstring name\n";

    long getId();

    void setId(long var1);

    String getName();

    void setName(String var1);
}

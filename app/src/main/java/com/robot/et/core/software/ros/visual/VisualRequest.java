package com.robot.et.core.software.ros.visual;

import org.ros.internal.message.Message;

/**
 * Created by Tony on 2016/8/23.
 */

public interface VisualRequest extends Message {
    String _TYPE = "com.robot.et.core.software.ros.visual/VisualRequest";
    String _DEFINITION = "int16 request_id\nstring input_name\n";

    short getRequestId();

    void setRequestId(short var1);

    String getInputName();

    void setInputName(String var1);
}

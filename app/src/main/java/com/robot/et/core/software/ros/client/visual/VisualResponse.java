package com.robot.et.core.software.ros.client.visual;

import org.ros.internal.message.Message;

/**
 * Created by Tony on 2016/8/23.
 */

public interface VisualResponse extends Message {
    String _TYPE = "com.robot.et.core.software.ros.client.visual/VisualResponse";
    String _DEFINITION = "int16 status\nint16 confidence\nstring output_name\n";

    short getStatus();

    void setStatus(short var1);

    short getConfidence();

    void setConfidence(short var1);

    String getOutputName();

    void setOutputName(String var1);
}

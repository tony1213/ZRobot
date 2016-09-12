package com.robot.et.core.software.ros.visual;

import org.ros.internal.message.Message;

/**
 * Created by Tony on 2016/8/23.
 */
public interface Visual extends Message {
    String _TYPE = "com.robot.et.core.software.ros.visual/Visual";
    String _DEFINITION = "int16 request_id\nstring input_name\n---\nint16 status\nint16 confidence\nstring output_name\n";
}

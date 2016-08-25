package com.robot.et.core.software.ros.map;

import org.ros.internal.message.Message;

/**
 * Created by Tony on 2016/8/25.
 */
public interface RmapRequest extends Message {
    String _TYPE = "com.robot.et.core.software.ros.map/RmapRequest";
    String _DEFINITION = "string map_name\n";

    String getMapName();

    void setMapName(String var1);

}

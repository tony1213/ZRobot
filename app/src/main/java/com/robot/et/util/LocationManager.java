package com.robot.et.util;

import com.robot.et.entity.LocationInfo;

/**
 * Created by houdeming on 2016/9/20.
 * 位置管理
 */
public class LocationManager {
    private static LocationInfo info = new LocationInfo();

    public static LocationInfo getInfo() {
        return info;
    }

    public static void setInfo(LocationInfo info) {
        LocationManager.info = info;
    }
}

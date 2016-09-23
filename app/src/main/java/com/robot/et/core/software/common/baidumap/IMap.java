package com.robot.et.core.software.common.baidumap;

import com.baidu.location.BDLocation;

/**
 * Created by houdeming on 2016/9/19.
 * 百度地图信息接口回调
 */
public interface IMap {
    // 获取位置信息
    void getLocationInfo(BDLocation location);
}

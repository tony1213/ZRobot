package com.robot.et.core.software.common.baidumap;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

/**
 * Created by houdeming on 2016/9/19.
 * 百度地图
 */
public class Map {
    private final String TAG = "map";
    private Location mLocation;
    private IMap iMap;

    public Map(Context context, IMap iMap) {
        Log.i(TAG, "Map()");
        this.iMap = iMap;
        // 初始化
        mLocation = new Location(context);
        mLocation.registerListener(mListener);
        mLocation.setLocationOption(mLocation.getDefaultLocationClientOption());
        // 开始定位
        startLocation();
    }

    // 注销掉监听
    public void destroyMap() {
        Log.i(TAG, "DestroyMap()");
        mLocation.unregisterListener(mListener);
        //停止定位服务
        mLocation.stop();
    }

    // 开始定位
    public void startLocation() {
        Log.i(TAG, "startLocation()");
        mLocation.start();
    }

    // 监听器
    private BDLocationListener mListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                String city = location.getCity();
                String area = location.getDistrict();
                Log.i(TAG, "city===" + city + ",area===" + area);
                if (!TextUtils.isEmpty(city)) {// 有时候可能只能定位到城市不能定位到区域
                    if (iMap != null) {
                        iMap.getLocationInfo(location);
                    }
                    //停止定位服务（百度地图在不断的定位）
                    destroyMap();
                } else {
                    Log.i(TAG, "null != location 没有定位成功");
                }
            } else {
                Log.i(TAG, "null == location 没有定位成功");
            }
        }
    };
}

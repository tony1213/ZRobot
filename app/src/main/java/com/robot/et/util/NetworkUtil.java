package com.robot.et.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by houdeming on 2016/9/22.
 */
public class NetworkUtil {

    // 获取WiFi的名字
    public static String getConnectWifiName(Context context) {
        //SSID: ROBOTAI_MINO, BSSID: 8c:a6:df:6e:91:0a, MAC: 94:a1:a2:4a:21:65, Supplicant state:
        // COMPLETED, RSSI: -42, Link speed: 72Mbps, Frequency: 2412MHz, Net ID: 0, Metered hint: false, score: 60
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }
}

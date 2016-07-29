package com.robot.et.common.enums;

import android.text.TextUtils;

public class EnumManager {

    //获取科大讯飞提供的场景service
    public static SceneServiceEnum getIflyScene(String str) {
        for (SceneServiceEnum serviceEnum : SceneServiceEnum.values()) {
            if (TextUtils.equals(str, serviceEnum.getServiceKey())) {
                return serviceEnum;
            }
        }
        return null;
    }

    //获取场景的enum
    public static MatchSceneEnum getScene(String str) {
        if (!TextUtils.isEmpty(str)) {
            for (MatchSceneEnum sceneEnum: MatchSceneEnum.values()) {
                if (sceneEnum.isScene(str)) {
                    return sceneEnum;
                }
            }
        }
        return null;
    }

}

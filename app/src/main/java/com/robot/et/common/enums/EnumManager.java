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

    //获取控制运动的key
    public static int getMoveKey(String str) {
        int moveKey = 0;
        if (!TextUtils.isEmpty(str)) {
            for (ControlMoveEnum moveEnum: ControlMoveEnum.values()) {
                if (str.contains(moveEnum.getMoveName())) {
                    moveKey  =  moveEnum.getMoveKey();
                }
            }
        }
        return moveKey;
    }

}

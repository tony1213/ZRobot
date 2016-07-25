package com.robot.et.util;

import android.text.TextUtils;

import com.robot.et.enums.SceneServiceEnum;

public class EnumManager {

	//获取科大讯飞提供的场景service
	public static SceneServiceEnum getIflyService(String str){
		for(SceneServiceEnum serviceEnum : SceneServiceEnum.values()){
			if(TextUtils.equals(str, serviceEnum.getServiceKey())){
				return serviceEnum;
			}
		}
		return null;
	}

}

package com.robot.et.core.hardware.light;

public class EarsLight {

	static {
		System.loadLibrary("EarsLight");
	}

	// 初始化耳朵灯
	public static native int initEarsLight();

	// 设置耳朵等的状态
	public static native int setLightStatus(int lightStatus);

}

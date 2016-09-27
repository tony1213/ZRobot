package com.robot.et.core.hardware.light;

public class FloodLight {

	static {
		System.loadLibrary("FloodLight");
	}

	// 初始化照明灯
	public static native int initFloodLight();

	// 设置照明灯的状态
	public static native int setLightStatus(int lightStatus);

}

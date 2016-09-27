package com.robot.et.core.hardware.wakeup;

// 一直都会唤醒
public class AllAwake {

	static {
		System.loadLibrary("AllAwake");
	}

	// 初始化
	public static native int initAllAwake();

	// 获取唤醒的值
	public static native int getAllAwakeValue();
}

package com.robot.et.core.hardware.sleepawake;

// 只有沉睡的时候才会唤醒
public class SleepAwake {
	static {
		System.loadLibrary("SleepAwake");
	}

	// 初始化
	public static native int initSleepAwake();

	// 获取唤醒的值
	public static native int getSleepAwakeValue();
}

package com.robot.et.core.hardware.wakeup;

public class WakeUp {
	
	static {
		System.loadLibrary("WakeUp");
	}

	//语言唤醒
	public static native int open(String path, int oFlag); 
	public static native int getWakeUpState(int fid);
	public static native int getWakeUpDegree();
	public static native int setGainDirection(int direction);
	public static native int wakeUpReset();
    public static native int close(int fId);

	//人脸唤醒
	public static native int wakeUpInit();
	public static native int getWakeUpState();
	public static native int faceWakeUpInit();
	public static native int getFaceWakeUpState();
	public static native int close();

}

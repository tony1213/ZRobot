package com.robot.et.core.software.common.log;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;

// APP崩溃的记录日志
public class AppCrash implements UncaughtExceptionHandler{
	private static final String TAG = "log";
	private static AppCrash crashHandler;
	
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (crashHandler != null) {
			try {
				Log.i(TAG, "将crash log写入文件");
				String fileName = Environment.getExternalStorageDirectory() + File.separator + "robot_error_log.txt";
				FileOutputStream fileOutputStream = new FileOutputStream(fileName, true);
				PrintStream printStream = new PrintStream(fileOutputStream);
				ex.printStackTrace(printStream);
				printStream.flush();
				printStream.close();
				fileOutputStream.close();
			} catch (FileNotFoundException e) {
				Log.i(TAG, "FileNotFoundException");
			} catch (IOException e) {
				Log.i(TAG, "IOException");
			}
		}
	} 
	
	//设置默认处理器
	public void init() {
		Thread.setDefaultUncaughtExceptionHandler(this);
	}
	
	private AppCrash() {

	}
	
	public static AppCrash instance() {
		if (crashHandler == null) {
			synchronized (AppCrash.class) {
				if (crashHandler == null) {
					crashHandler = new AppCrash();
				}
			}
		}
		return crashHandler;
	}
}

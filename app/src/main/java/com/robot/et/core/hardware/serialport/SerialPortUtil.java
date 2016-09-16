package com.robot.et.core.hardware.serialport;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 串口操作类
 *
 * @author Jerome http://gqdy365.iteye.com/blog/2188906
 */
public class SerialPortUtil {
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private String path = "/dev/ttyS1";
    private int baudrate = 115200;
    private static SerialPortUtil portUtil;
    private OnDataReceiveListener onDataReceiveListener = null;
    private boolean isStop = false;

    public interface OnDataReceiveListener {
        void onDataReceive(byte[] buffer, int size);
    }

    public void setOnDataReceiveListener(
            OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

    public static SerialPortUtil getInstance() {
        if (null == portUtil) {
            portUtil = new SerialPortUtil();
            portUtil.onCreate();
        }
        return portUtil;
    }

    /**
     * 初始化串口信息
     */
    public void onCreate() {
        try {
            mSerialPort = new SerialPort(new File(path), baudrate);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

            // 不接受硬件返回来的信息
//            mReadThread = new ReadThread();
//            isStop = false;
//            mReadThread.start();
        } catch (Exception e) {
            Log.i("SerialPort", "onCreate() Exception==" + e.getMessage());
        }
    }

    /**
     * 发送指令到串口
     *
     * @param cmd
     * @return
     */
    public boolean sendCmds(String cmd) {
        boolean result = true;
        byte[] mBuffer = (cmd + "\n").getBytes();
        try {
            if (mOutputStream != null) {
                mOutputStream.write(mBuffer);
            } else {
                result = false;
            }
        } catch (IOException e) {
            Log.i("SerialPort", "sendCmds IOException==" + e.getMessage());
            result = false;
        }
        return result;
    }

    // 发送字节数组到硬件
    public boolean sendBuffer(byte[] mBuffer) {
        boolean result = true;
        try {
            if (mOutputStream != null) {
                mOutputStream.write(mBuffer);
                Log.i("SerialPort", "write done");
            } else {
                result = false;
            }
        } catch (IOException e) {
            Log.i("SerialPort", "sendBuffer IOException==" + e.getMessage());
            result = false;
        }
        try {
            Thread.sleep(50);// 此处休眠50，等待一个指令执行完成
        } catch (InterruptedException e) {
            Log.i("SerialPort", "sendBuffer InterruptedException==" + e.getMessage());
        }
        return result;
    }

    // 开启子线程不断的读取硬件发来的信息
    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isStop && !isInterrupted()) {
                int size;
                try {
                    if (mInputStream == null) {
                        return;
                    }

                    byte[] buffer = new byte[1024];
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        if (null != onDataReceiveListener) {
                            Log.i("SerialPort", "RUN READ DATA:length is:" + size + ",data is:" + new String(buffer, 0, size));
                            onDataReceiveListener.onDataReceive(buffer, size);
                        } else {
                            Log.i("SerialPort", "null == onDataReceiveListener");
                        }
                    }
                    Thread.sleep(100);
                } catch (Exception e) {
                    Log.i("SerialPort", "ReadThread  Exception==" + e.getMessage());
                    return;
                }
            }
        }
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort() {
        isStop = true;
        if (mReadThread != null) {
            mReadThread.interrupt();
        }
        if (mSerialPort != null) {
            mSerialPort.close();
        }
    }
}

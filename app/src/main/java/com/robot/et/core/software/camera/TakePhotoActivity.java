package com.robot.et.core.software.camera;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.iflytek.cloud.util.Accelerometer;
import com.robot.et.R;
import com.robot.et.common.BroadcastAction;
import com.robot.et.core.software.system.media.Sound;
import com.robot.et.util.BitmapUtil;
import com.robot.et.util.BroadcastEnclosure;

import java.io.IOException;

// 自动拍照
public class TakePhotoActivity extends Activity {
    private SurfaceView mPreviewSurface;
    private Camera mCamera;
    // 默认打开后置摄像头  前置摄像头：CAMERA_FACING_FRONT  后置摄像头：CAMERA_FACING_BACK
    private int mCameraId = CameraInfo.CAMERA_FACING_BACK;
    // Camera nv21格式预览帧的尺寸，默认设置640*480
    private int PREVIEW_WIDTH = 640;
    private int PREVIEW_HEIGHT = 480;
    // 预览帧数据存储数组和缓存数组
    private byte[] nv21;
    private byte[] buffer;
    //识别到的图片信息
    private byte[] mImageData;
    // 加速度感应器，用于获取手机的朝向
    private Accelerometer mAcc;
    private boolean mStopTrack;
    private final String TAG = "camera";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置屏幕常亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_take_photo);
        // 初始化界面
        initUI();
        nv21 = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 2];
        buffer = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 2];
        mAcc = new Accelerometer(this);
    }

    // SurfaceView预览监听器
    private Callback mPreviewCallback = new Callback() {

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            closeCamera();
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            openCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
    };

    // 初始化界面
    private void initUI() {
        mPreviewSurface = (SurfaceView) findViewById(R.id.sfv_preview);
        mPreviewSurface.getHolder().addCallback(mPreviewCallback);
        mPreviewSurface.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    // 打开相机
    private void openCamera() {
        if (null != mCamera) {
            return;
        }
        // 检查相机权限
        if (!checkCameraPermission()) {
            Log.i(TAG, "摄像头权限未打开，请打开后再试");
            mStopTrack = true;
            return;
        }
        // 只有一个摄相头，打开后置
        if (Camera.getNumberOfCameras() == 1) {
            mCameraId = CameraInfo.CAMERA_FACING_BACK;
        }
        try {
            // 打开相机
            mCamera = Camera.open(mCameraId);
            if (CameraInfo.CAMERA_FACING_FRONT == mCameraId) {
                Log.i(TAG, "前置摄像头已开启，点击可切换");
            } else {
                Log.i(TAG, "后置摄像头已开启，点击可切换");
            }
        } catch (Exception e) {
            Log.i(TAG, "Camera Exception==" + e.getMessage());
            // 关闭相机
            closeCamera();
            return;
        }
        // 设置相机Parameters参数
        Parameters params = mCamera.getParameters();
        params.setPreviewFormat(ImageFormat.NV21);
        params.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
        mCamera.setParameters(params);
        // 设置显示的偏转角度，大部分机器是顺时针90度，某些机器需要按情况设置
//        mCamera.setDisplayOrientation(90);
        mCamera.setPreviewCallback(new PreviewCallback() {

            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                System.arraycopy(data, 0, nv21, 0, data.length);
            }
        });

        try {
            mCamera.setPreviewDisplay(mPreviewSurface.getHolder());
            mCamera.startPreview();
        } catch (IOException e) {
            Log.i(TAG, "Camera IOException==" + e.getMessage());
        }
    }

    // 关闭相机
    private void closeCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    // 检查相机权限
    private boolean checkCameraPermission() {
        int status = checkPermission(permission.CAMERA, Process.myPid(), Process.myUid());
        if (PackageManager.PERMISSION_GRANTED == status) {
            return true;
        }
        return false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.i(TAG, "onWindowFocusChanged");
        // 2s之后拍照
        SystemClock.sleep(2000);
        takePicture();
    }

    // 拍照
    private void takePicture() {
        if (null != mAcc) {
            mAcc.start();
        }

        mStopTrack = false;
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!mStopTrack) {
                    // 如果没获取到数据继续
                    if (null == nv21) {
                        Log.i(TAG, "null == nv21");
                        continue;
                    }
                    synchronized (nv21) {
                        System.arraycopy(nv21, 0, buffer, 0, nv21.length);
                    }

                    byte[] tmp = new byte[nv21.length];
                    System.arraycopy(nv21, 0, tmp, 0, nv21.length);

                    mImageData = BitmapUtil.bitmap2Byte(BitmapUtil.decodeToBitMap(tmp, PREVIEW_WIDTH, PREVIEW_HEIGHT));

                    mStopTrack = true;

                }
            }
        }).start();

        // 播放拍照提示音
        BroadcastEnclosure.playSoundTips(TakePhotoActivity.this, Sound.SOUND_CAMERA);
        // 发送图片数据
        Intent intent = new Intent();
        intent.setAction(BroadcastAction.ACTION_TAKE_PHOTO_COMPLECTED);
        intent.putExtra("photoData", mImageData);
        sendBroadcast(intent);

        // 1s之后关闭
        SystemClock.sleep(1000);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeCamera();
        if (null != mAcc) {
            mAcc.stop();
        }
    }
}


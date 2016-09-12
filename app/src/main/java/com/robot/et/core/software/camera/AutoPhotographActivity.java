package com.robot.et.core.software.camera;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import com.robot.et.R;

/**
 * Created by houdeming on 2016/9/8.
 * 自动拍照
 */
public class AutoPhotographActivity extends Activity implements ICamera {
    private final String TAG = "camera";
    private CustomCameraView cameraView;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_auto_photograph);
        cameraView = (CustomCameraView) findViewById(R.id.cc_camera);
        cameraView.setCameraCallBack(this);
    }

    // 实现ICamera接口方法 快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作
    @Override
    public void onShutter() {
        Log.i(TAG, "onShutter() 快门按下的回调");
    }

    // 实现ICamera接口方法  聚焦成功
    @Override
    public void onAutoFocus() {
        Log.i(TAG, "onAutoFocus() 聚焦成功");
    }

    // 实现ICamera接口方法 拍照结果的返回
    @Override
    public void onTakePictureInfo(byte[] data) {
        Log.i(TAG, "onTakePictureInfo() 拍照结果的返回");
        if (data != null && data.length > 0) {

        }
    }
}


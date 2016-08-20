package com.robot.et.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.robot.et.R;
import com.robot.et.core.hardware.serialport.SerialPortService;

/**
 * Created by houdeming on 2016/8/19.
 */
public class TestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        startService(new Intent(this, SerialPortService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this,SerialPortService.class));
    }
}

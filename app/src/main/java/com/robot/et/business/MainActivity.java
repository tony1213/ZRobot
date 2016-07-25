package com.robot.et.business;

import android.os.Bundle;
import com.robot.et.R;
import org.ros.android.RosActivity;
import org.ros.node.NodeMainExecutor;

import java.net.URI;

public class MainActivity extends RosActivity {

    public MainActivity(){
        super("XRobot","Xrobot", URI.create("http://192.168.3.1:11311"));//本体的ROS IP和端口
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {

    }
}

/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.robot.et.core.software.ros.client;

import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.core.software.ros.visual.VisualRequest;
import com.robot.et.core.software.ros.visual.VisualResponse;

import org.ros.exception.RemoteException;
import org.ros.exception.RosRuntimeException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;

public class VisualClient extends AbstractNodeMain {

    private int flag;
    private String name;

    public VisualClient(int flag, String name) {
        this.flag = flag;
        this.name = name;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("rosjava_tutorial_services/visualclient");
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
        ServiceClient<VisualRequest, VisualResponse> serviceClient;
        try {
            serviceClient = connectedNode.newServiceClient("learn_to_recognize_ros_server", com.robot.et.core.software.ros.visual.Visual._TYPE);
        } catch (ServiceNotFoundException e) {
            throw new RosRuntimeException(e);
        }
        final VisualRequest request = serviceClient.newMessage();
        request.setId(flag);
        request.setName(name);
        serviceClient.call(request, new ServiceResponseListener<VisualResponse>() {
            @Override
            public void onSuccess(VisualResponse response) {
                Log.e("ROS_Client", "onSuccess:Result:" + response.getResult() + ",Name:" + response.getName());
                if (flag == 0) {
                    //视觉初始化
                    if (response.getResult() == 0) {
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "视觉初始化成功");
                    } else {
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "视觉初始化失败");
                    }
                } else if (flag == 1) {
                    //视觉学习
                    if (response.getResult()!=0){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "距离太近或者太远，请重新开始");
                    }else {
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "好的，记住了");
                    }
                } else if (flag == 2) {
                    //视觉识别
                    if (response.getResult()==0){
                        String result=response.getName();
                        if (TextUtils.equals("",result)){
                            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "我不认识这个东西，让我学习一下吧！");
                        }else {
                            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "这是一个："+response.getName());
                        }
                    }else {
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "距离太近或者太远");
                    }
                }
            }

            @Override
            public void onFailure(RemoteException e) {
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "视觉出现异常");
                Log.e("ROS_Client", "onFailure");
                throw new RosRuntimeException(e);
            }
        });
    }
}

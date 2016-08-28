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

import android.text.TextUtils;
import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.core.software.ros.move.MoveRequest;
import com.robot.et.core.software.ros.move.MoveResponse;

import org.ros.exception.RemoteException;
import org.ros.exception.RosRuntimeException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;

public class MoveClient extends AbstractNodeMain {

    private String frame;
    private float x;
    private float y;
    private float angle;

    public MoveClient(String frame,float x,float y,float angle) {
        this.frame =frame;
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("rosjava_tutorial_services/moveClient");
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
        ServiceClient<MoveRequest, MoveResponse> serviceClient=null;
        try {
            serviceClient = connectedNode.newServiceClient("set_goal", com.robot.et.core.software.ros.move.Move._TYPE);
        } catch (ServiceNotFoundException e) {
            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "服务未初始化，请初始化移动服务");
            return;
        }
        final MoveRequest request = serviceClient.newMessage();
        request.setFrame(frame);
        request.setX(x);
        request.setY(y);
        request.setAngle(angle);
        serviceClient.call(request, new ServiceResponseListener<MoveResponse>() {
            @Override
            public void onSuccess(MoveResponse response) {
                Log.e("WorldNavigation", "response:" + response.getStatus());
                if (TextUtils.equals("SUCCESS",response.getStatus())){
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "目标到达成功");
                }else if (TextUtils.equals("FAILED",response.getStatus())){
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "目标不可到达");
                }
            }

            @Override
            public void onFailure(RemoteException e) {
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "移动出现异常");
                Log.e("WorldNavigation", "onFailure");
//                throw new RosRuntimeException(e);
            }
        });
    }
}

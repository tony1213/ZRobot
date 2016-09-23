package com.robot.et.core.software.common.push.netty;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.core.software.common.push.PushResultHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyClientHandler extends SimpleChannelInboundHandler<Object> {
    private Context context;
    private PushResultHandler pushResultHandler;

    public NettyClientHandler(Context context) {
        this.context = context;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        Log.i("netty", "接受到netty服务器发来的消息msg===" + msg);
        String message = (String) msg;
        if (!TextUtils.isEmpty(message)) {
            if (pushResultHandler == null) {
                pushResultHandler = new PushResultHandler(context);
            }
            pushResultHandler.setPushResult(message);
        }
    }
}

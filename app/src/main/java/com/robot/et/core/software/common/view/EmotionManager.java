package com.robot.et.core.software.common.view;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by houdeming on 2016/8/17.
 */
public class EmotionManager {
    private static ImageView imgLeft;
    private static ImageView imgRight;
    private static LinearLayout showLinearLayout;

    public static void setView(LinearLayout showLinearLayout, ImageView imgLeft, ImageView imgRight) {
        EmotionManager.showLinearLayout = showLinearLayout;
        EmotionManager.imgLeft = imgLeft;
        EmotionManager.imgRight = imgRight;
    }

    //显示表情
    public static void showEmotionAnim(int resId) {
        if (imgLeft != null && imgRight != null) {
            showEmotionLinearLayout(true);
            AnimationDrawable animLeft = getAnimationDrawable(imgLeft, resId);
            AnimationDrawable animRight = getAnimationDrawable(imgRight, resId);
            playEmotionAnim(animLeft);
            playEmotionAnim(animRight);
        }
    }

    //显示表情父布局
    public static void showEmotionLinearLayout(boolean isShowEmotionLinearLayout) {
        if (showLinearLayout != null) {
            if (isShowEmotionLinearLayout) {
                showLinearLayout.setVisibility(View.VISIBLE);
            } else {
                showLinearLayout.setVisibility(View.GONE);
            }
        }
    }

    //显示正常表情
    public static void showEmotion(int resId) {
        if (imgLeft != null && imgRight != null) {
            showEmotionLinearLayout(true);
            setEmotion(imgLeft, resId);
            setEmotion(imgRight, resId);
        }
    }

    private static AnimationDrawable getAnimationDrawable(ImageView img, int resId) {
        img.setBackgroundResource(resId);
        AnimationDrawable animationDrawable = (AnimationDrawable) img.getBackground();
        return animationDrawable;
    }

    private static void playEmotionAnim(AnimationDrawable animationDrawable) {
        animationDrawable.stop();
        animationDrawable.start();
    }

    private static void setEmotion(ImageView img, int resId) {
        img.setBackgroundResource(resId);
    }

}

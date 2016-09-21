package com.robot.et.core.software.common.view;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by houdeming on 2016/8/17.
 */
public class EmotionManager {
    private static ImageView img;
    private static LinearLayout showLinearLayout;

    public static void setView(LinearLayout showLinearLayout, ImageView img) {
        EmotionManager.showLinearLayout = showLinearLayout;
        EmotionManager.img = img;
    }

    //显示表情
    public static void showEmotionAnim(int resId) {
        if (img != null) {
            showEmotionLinearLayout(true);
            AnimationDrawable imgAnim = getAnimationDrawable(img, resId);
            playEmotionAnim(imgAnim);
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
        if (img != null) {
            showEmotionLinearLayout(true);
            setEmotion(img, resId);
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

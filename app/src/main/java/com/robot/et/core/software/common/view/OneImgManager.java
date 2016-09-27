package com.robot.et.core.software.common.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by houdeming on 2016/8/17.
 */
public class OneImgManager {
    private static ImageView imageView;
    private static ImageView imageBitmap;
    private static ImageView imagePhoto;
    private static LinearLayout showLinearLayout;

    public static void setView(LinearLayout showLinearLayout, ImageView imageView, ImageView imageBitmap, ImageView imagePhoto) {
        OneImgManager.showLinearLayout = showLinearLayout;
        OneImgManager.imageView = imageView;
        OneImgManager.imageBitmap = imageBitmap;
        OneImgManager.imagePhoto = imagePhoto;
    }

    //显示图片
    public static void showImg(int resId) {
        if (imageView != null) {
            showImgLinearLayout(true);
            if (imageBitmap != null) {
                imageBitmap.setVisibility(View.GONE);
                imagePhoto.setVisibility(View.GONE);
            }
            imageView.setVisibility(View.VISIBLE);
            imageView.setBackgroundResource(resId);
        }
    }

    //显示图片
    public static void showImg(Bitmap bitmap) {
        if (imageBitmap != null) {
            showImgLinearLayout(true);
            if (imageView != null) {
                imageView.setVisibility(View.GONE);
                imagePhoto.setVisibility(View.GONE);
            }
            imageBitmap.setVisibility(View.VISIBLE);
            imageBitmap.setImageBitmap(bitmap);
        }
    }

    //显示拍的照片
    public static void showPhoto(Context context, int animId, Bitmap bitmap) {
        if (imagePhoto != null) {
            showImgLinearLayout(true);
            if (imageBitmap != null) {
                imageBitmap.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
            }
            imagePhoto.setVisibility(View.VISIBLE);
            if (animId == 0) {
                imagePhoto.setImageBitmap(bitmap);
            } else {
                playAnim(context, animId, bitmap);
            }
        }
    }

    // 播放图片切换动画
    private static void playAnim(Context context, int animId, final Bitmap bitmap) {
        Animation animation = AnimationUtils.loadAnimation(context, animId);
        imagePhoto.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imagePhoto.setImageBitmap(bitmap);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //显示表情父布局
    public static void showImgLinearLayout(boolean isShowLinearLayout) {
        if (showLinearLayout != null) {
            if (isShowLinearLayout) {
                showLinearLayout.setVisibility(View.VISIBLE);
            } else {
                showLinearLayout.setVisibility(View.GONE);
            }
        }
    }

}

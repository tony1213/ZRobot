package com.robot.et.core.software.common.view;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by houdeming on 2016/8/17.
 */
public class OneImgManager {
    private static ImageView imageView;
    private static ImageView imageBitmap;
    private static LinearLayout showLinearLayout;

    public static void setView(LinearLayout showLinearLayout, ImageView imageView, ImageView imageBitmap) {
        OneImgManager.showLinearLayout = showLinearLayout;
        OneImgManager.imageView = imageView;
        OneImgManager.imageBitmap = imageBitmap;
    }

    //显示图片
    public static void showImg(int resId) {
        if (imageView != null) {
            showImgLinearLayout(true);
            if (imageBitmap != null) {
                imageBitmap.setVisibility(View.GONE);
                showLinearLayout.setBackgroundColor(Color.BLACK);
            }
            imageView.setVisibility(View.VISIBLE);
            imageView.setBackgroundResource(resId);
        }
    }

    //显示图片
    public static void showImg(Bitmap bitmap, boolean isWhite) {
        if (imageBitmap != null) {
            showImgLinearLayout(true);
            if (imageView != null) {
                imageView.setVisibility(View.GONE);
                if (isWhite) {
                    showLinearLayout.setBackgroundColor(Color.WHITE);
                } else {
                    showLinearLayout.setBackgroundColor(Color.BLACK);
                }
            }
            imageBitmap.setVisibility(View.VISIBLE);
            imageBitmap.setImageBitmap(bitmap);
        }
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

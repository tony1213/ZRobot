package com.robot.et.core.software.common.view;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by houdeming on 2016/8/17.
 */
public class OneImgManager {
    private static ImageView imageView;
    private static LinearLayout showLinearLayout;

    public static void setView(LinearLayout showLinearLayout, ImageView imageView) {
        OneImgManager.showLinearLayout = showLinearLayout;
        OneImgManager.imageView = imageView;
    }

    //显示图片
    public static void showImg(int resId) {
        if (imageView != null) {
            showImgLinearLayout(true);
            imageView.setBackgroundResource(resId);
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

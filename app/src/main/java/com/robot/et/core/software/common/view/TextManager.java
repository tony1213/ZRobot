package com.robot.et.core.software.common.view;

import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by houdeming on 2016/8/17.
 */
public class TextManager {

    private static CustomTextView textView;
    private static LinearLayout showTextLl;

    public static void setView(LinearLayout showTextLl, CustomTextView textView) {
        TextManager.showTextLl = showTextLl;
        TextManager.textView = textView;
    }

    //显示文本内容
    public static void showText(String text) {
        if (textView != null) {
            showTextLinearLayout(true);
            textView.setText("");
            textView.setText(text);
        }
    }

    //隐藏文本内容
    public static void hideText() {
        if (textView != null) {
            textView.setText("");
        }
    }

    //显示文本内容的父布局
    public static void showTextLinearLayout(boolean isShowTextLinearLayout) {
        if (showTextLl != null) {
            if (isShowTextLinearLayout) {
                showTextLl.setVisibility(View.VISIBLE);
            } else {
                showTextLl.setVisibility(View.GONE);
            }
        }
    }

}

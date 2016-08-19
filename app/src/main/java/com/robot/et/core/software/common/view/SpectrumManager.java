package com.robot.et.core.software.common.view;

import android.media.audiofx.Visualizer;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by houdeming on 2016/8/18.
 * 音乐频谱界面的管理
 */
public class SpectrumManager {
    private static LinearLayout mLinearLayout;
    private static VisualizerView mVisualizerView;
    private static Visualizer mVisualizer;

    public static void setView(LinearLayout mLinearLayout, VisualizerView mVisualizerView) {
        SpectrumManager.mLinearLayout = mLinearLayout;
        SpectrumManager.mVisualizerView = mVisualizerView;
    }

    public static void setVisualizer(Visualizer mVisualizer) {
        SpectrumManager.mVisualizer = mVisualizer;
    }

    //是否显示频谱的父布局
    public static void showSpectrumLinearLayout(boolean isShowLinearLayout) {
        if (mLinearLayout != null) {
            if (isShowLinearLayout) {
                mLinearLayout.setVisibility(View.VISIBLE);
            } else {
                mLinearLayout.setVisibility(View.GONE);
            }
        }
    }

    //显示频谱
    public static void showSpectrum() {
        //设置允许波形表示，并且捕获它
        if (mVisualizerView != null && mVisualizer != null) {
            showSpectrumLinearLayout(true);
            mVisualizerView.setVisualizer(mVisualizer);
            mVisualizer.setEnabled(true);//false 则不显示
        }
    }

    //隐藏频谱
    public static void hideSpectrum() {
        if (mVisualizer != null) {
            mVisualizer.setEnabled(false);
        }
    }

}

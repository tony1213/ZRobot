package com.robot.et.util;

import android.text.TextUtils;

/**
 * Created by houdeming on 2016/7/25.
 */
public class PlayerControl {
    private static String musicSrc;

    public static String getMusicSrc() {
        return musicSrc;
    }

    public static void setMusicSrc(String musicSrc) {
        PlayerControl.musicSrc = musicSrc;
    }

    public static String getMusicSpeakContent(String result, String musicSplit) {
        String content = "";
        if (!TextUtils.isEmpty(result)) {
            if (result.contains(musicSplit)) {
                String[] datas = result.split(musicSplit);
                if (datas != null && datas.length > 0) {
                    //歌手+歌名 + 歌曲src
                    content = "好的，开始播放" + datas[0] + "：" + datas[1];
                    setMusicSrc(datas[2]);
                }
            }
        }
        return content;
    }

}

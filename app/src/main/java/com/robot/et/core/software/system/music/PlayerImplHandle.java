package com.robot.et.core.software.system.music;

/**
 * Created by houdeming on 2016/7/25.
 */
public class PlayerImplHandle {
    private static PlayerImpl player;

    public static void setPlayer(PlayerImpl player) {
        PlayerImplHandle.player = player;
    }

    public static void play(String musicSrc) {
        if (player != null) {
            player.startPlay(musicSrc);
        }
    }

    public static void stopPlay() {
        if (player != null) {
            player.stopPlay();
        }
    }

}

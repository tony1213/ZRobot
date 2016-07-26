package com.robot.et.service.software.impl;

import com.robot.et.service.software.system.music.Player;

/**
 * Created by houdeming on 2016/7/25.
 */
public class PlayerHand {
    private static Player player;

    public static void setPlayer(Player player) {
        PlayerHand.player = player;
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

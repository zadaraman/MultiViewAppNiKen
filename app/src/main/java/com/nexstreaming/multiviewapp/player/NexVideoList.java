package com.nexstreaming.multiviewapp.player;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class NexVideoList {

    private List<NexVideoPlayer> playerList = new ArrayList<>();

    public void register(NexVideoPlayer player) {
        playerList.add(player);
    }

    public void notifyReady() {
        for (NexVideoPlayer player : playerList) {
            if (player.isReady() == false) {
                return;
            }
        }
        for (NexVideoPlayer player : playerList) {
            player.start();
        }
    }

    public NexVideoPlayer getPlayer(int index) {
        return playerList.get(index);
    }

    public void stop() {
        for (NexVideoPlayer player : playerList) {
            player.stopPlayer();

        }
        try {
            while (isStopped(true) == false) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Log.e("TAG", "Exception - stopPlayer() : " + e.getMessage());
        }

    }

    public void release() {
        for (NexVideoPlayer player : playerList) {
            player.releasePlayer();
        }
    }

    public boolean isStopped(boolean forceToStop) {
        for (NexVideoPlayer player : playerList) {
            if (player.isStopped() == false) {
                if (forceToStop)
                    player.stop();

                return false;
            }
        }

        return true;
    }

    public boolean isReady() {
        for (NexVideoPlayer player : playerList) {
            if (player.isReady() == false) {
                return false;
            }
        }

        return true;
    }

    public int getPlayerCount() {
        return playerList.size();
    }
}

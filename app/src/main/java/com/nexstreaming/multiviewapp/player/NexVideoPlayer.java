package com.nexstreaming.multiviewapp.player;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nexstreaming.nexplayerengine.NexABRController;
import com.nexstreaming.nexplayerengine.NexALFactory;
import com.nexstreaming.nexplayerengine.NexClosedCaption;
import com.nexstreaming.nexplayerengine.NexEventReceiver;
import com.nexstreaming.nexplayerengine.NexPlayer;
import com.nexstreaming.nexplayerengine.NexVideoRenderer;
import com.nexstreaming.nexplayerengine.NexVideoViewFactory;

public class NexVideoPlayer {
    private static final String TAG = "NexVideoPlayer";
    private static final int BANDWIDTH_KBPS = 1024;
    //    private static final int HIGH_RES_BANDWIDTH = 2400 * BANDWIDTH_KBPS;
    private static final int HIGH_RES_BANDWIDTH = 4000 * BANDWIDTH_KBPS;
    private static final int LOW_RES_BANDWIDTH = 4000 * BANDWIDTH_KBPS;
    private final NexABRController mABRController;

    private NexPlayer mNexPlayer;
    private NexVideoViewFactory.INexVideoView mVideoView;
    private Context mContext;
    private NexVideoList mSynchronizer;
    private int index = 0;
    private boolean mReady = false;
    private TextView mTextView;
    private final NexALFactory mNexALFactory;
    private boolean isMain;

    public NexVideoPlayer(Context context, NexVideoViewFactory.INexVideoView videoView, NexVideoList synchronizer, int index) {
        mContext = context;
        mSynchronizer = synchronizer;
        this.index = index;
        isMain = index == 0;
        mNexPlayer = new NexPlayer();
        mABRController = new NexABRController(mNexPlayer);
        mVideoView = videoView;
        synchronizer.register(this);

        mNexALFactory = new NexALFactory();
        int logLevel = 4;

        if (mNexALFactory.init(context, android.os.Build.MODEL, NexPlayer.NEX_DEVICE_USE_AUTO, logLevel, 1) == false) {
            Log.d(TAG, "ALFactory initialization failed");
            return;
        }

        mNexPlayer.setNexALFactory(mNexALFactory);

        setPlayerListener(mNexPlayer);
        setVideoViewListener(videoView);

        NexPlayer.NexErrorCode result = mNexPlayer.init(context, logLevel);
        mNexPlayer.setDebugLogs(logLevel, logLevel, logLevel);

        if (NexPlayer.NexErrorCode.NONE != result) {
            Log.d(TAG, "NexPlayer initialization failed: " + result.getDesc());
            return;
        }

        mVideoView.init(mNexPlayer);
        mVideoView.setVisibility(View.VISIBLE);

        mNexPlayer.setVolume(0);
        mNexPlayer.setProperty(NexPlayer.NexProperty.MAX_BW, index == 0 ? HIGH_RES_BANDWIDTH : LOW_RES_BANDWIDTH);
        mNexPlayer.setProperty(NexPlayer.NexProperty.ENABLE_SPD_SYNC_TO_GLOBAL_TIME, 1);
        mNexPlayer.setProperty(NexPlayer.NexProperty.ENABLE_SPD_SYNC_TO_DEVICE_TIME, 1);
        mNexPlayer.setProperty(NexPlayer.NexProperty.SET_SPD_SYNC_DIFF_TIME, 300);
        mNexPlayer.setProperty(NexPlayer.NexProperty.SET_SPD_TOO_MUCH_DIFF_TIME, 5000);
        mNexPlayer.setProperty(NexPlayer.NexProperty.SET_PRESENTATION_DELAY, 10000);

        RelativeLayout parent = ((RelativeLayout) mVideoView.getView().getParent());
//        mTextView = (TextView) parent.getChildAt(1);
    }

    public void open(String url) {
        mABRController.setABREnabled(true);
        mNexPlayer.setProperty(NexPlayer.NexProperty.MAX_BW, LOW_RES_BANDWIDTH);
        mNexPlayer.open(url, null, null, NexPlayer.NEXPLAYER_SOURCE_TYPE_STREAMING, NexPlayer.NEXPLAYER_TRANSPORT_TYPE_TCP);
        mNexPlayer.setVolume(index == 0 ? 1 : 0);
    }

    public void setVolume(int volume){
        mNexPlayer.setVolume(volume);
    }

    private void changeMaxBandwidth(boolean isMain) {
        int kbps = isMain ? HIGH_RES_BANDWIDTH : LOW_RES_BANDWIDTH;
        mNexPlayer.setProperty(NexPlayer.NexProperty.MAX_BW, kbps);
        mABRController.changeMaxBandWidth(kbps);
        mABRController.setTargetBandWidth(kbps, isMain ? NexABRController.SegmentOption.QUICKMIX : NexABRController.SegmentOption.QUICKMIX, NexABRController.TargetOption.BELOW);
    }

    private void setVideoViewListener(NexVideoViewFactory.INexVideoView videoView) {
        videoView.setListener(new NexVideoRenderer.IListener() {
            @Override
            public void onDisplayedRectChanged() {
            }

            @Override
            public void onFirstVideoRenderCreate() {
                updateVideoSize();
            }

            @Override
            public void onSizeChanged() {
            }

            @Override
            public void onVideoSizeChanged() {
                updateVideoSize(0, 0);
            }
        });
    }

    private void setPlayerListener(NexPlayer nexPlayer) {

        nexPlayer.setListener(new NexEventReceiver() {
            @Override
            public void onError(NexPlayer mp, NexPlayer.NexErrorCode errorcode) {
                Log.d(TAG, "NexVideoPlayer Error: " + errorcode.getDesc());
            }

            @Override
            public void onStateChanged(NexPlayer mp, int pre, int now) {

            }

            @Override
            public void onAsyncCmdComplete(NexPlayer mp, int command, int result, int param1, int param2) {
                Log.e(TAG, "onAsyncCmdComplete - result() : " + result);
                if (command == NexPlayer.NEXPLAYER_ASYNC_CMD_OPEN_STREAMING) {
                    if (mReady == false) {
                        mReady = true;
                        mSynchronizer.notifyReady();
                        changeMaxBandwidth(isMain);
                    }
                } else if (command == NexPlayer.NEXPLAYER_ASYNC_CMD_STOP) {
                    Log.d(TAG, "Video Stopped" + String.valueOf(index + 1));

                } else if (command == NexPlayer.NEXPLAYER_ASYNC_CMD_START_STREAMING) {
                    Log.d(TAG, "Video Resumed" + String.valueOf(index + 1));
                }
            }

            @Override
            public void onVideoRenderPrepared(NexPlayer mp) {
                mp.resume();
            }

            @Override
            public void onTextRenderRender(NexPlayer mp, int trackIndex, NexClosedCaption textInfo) {
            }
        });
    }

    public void updateVideoSize() {
        updateVideoSize(0, 0);
    }

    public void updateVideoSize(int vw, int vh) {
        Point videoSize = new Point();
        mVideoView.getVideoSize(videoSize);

        int videoWidth = videoSize.x;
        int videoHeight = videoSize.y;
        int screenWidth = vw == 0 ? mVideoView.getWidth() : vw;
        int screenHeight = vh == 0 ? mVideoView.getHeight() : vh;

        if (videoWidth != 0 && videoHeight != 0) {
            float scale = Math.min((float) screenWidth / (float) videoWidth, (float) screenHeight / (float) videoHeight);
            int width = (int) (videoWidth * scale);
            int height = (int) (videoHeight * scale);
            int top = (screenHeight - height) / 2;
            int left = (screenWidth - width) / 2;
            mVideoView.setOutputPos(left, top, width, height);
            mNexPlayer.setProperty(NexPlayer.NexProperty.MAX_WIDTH, videoWidth);
            mNexPlayer.setProperty(NexPlayer.NexProperty.MAX_HEIGHT, videoHeight);
        }
    }

    public boolean isReady() {
        return mReady;
    }

    public void start() {
        if (mReady) {
            mNexPlayer.start(0);
        }
    }

    public void stop() {
        mNexPlayer.stop();
    }

    public NexVideoViewFactory.INexVideoView getView() {
        return mVideoView;
    }

    public void pause() {
        mNexPlayer.pause();
        mTextView.setText("Video " + String.valueOf(index + 1) + " (Paused)");
    }

    public void resume() {
        if (mReady) {
            mTextView.setText("Video " + String.valueOf(index + 1));
            mNexPlayer.resume();
        }
    }

    public boolean isStopped() {
        return mNexPlayer.getState() <= NexPlayer.NEXPLAYER_STATE_STOP;
    }

    public void stopPlayer() {
        if (mNexPlayer.getState() > NexPlayer.NEXPLAYER_STATE_STOP) {
            mNexPlayer.stop();
        }
    }

    public void releasePlayer() {
        try {
            if (mNexPlayer != null) {
                if (mNexPlayer.getState() > NexPlayer.NEXPLAYER_STATE_CLOSED) {
                    mNexPlayer.close();
                }

                mNexPlayer.release();
            }
            if (mNexALFactory != null) {
                mNexALFactory.release();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception - releasePlayer() : " + e.getMessage());
        }
    }

    public void setPlayerType(boolean isMain) {
        this.isMain = isMain;
        changeMaxBandwidth(isMain);
    }
}

package com.nexstreaming.multiviewapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.nexstreaming.multiviewapp.helper.HomeKeyListener;
import com.nexstreaming.multiviewapp.player.NexVideoList;
import com.nexstreaming.multiviewapp.player.NexVideoPlayer;
import com.nexstreaming.nexplayerengine.NexVideoViewFactory;

import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {

    public static final String PARAM_STREAMS = "PARAM_STREAMS";
    public static final String TAG = "NEXPLAYER LOG";
    private final NexVideoList mSynchronizer = new NexVideoList();

    private List<NexVideoViewFactory.INexVideoView> playerViews = new ArrayList<>();
    RelativeLayout mainPlayerContainer;
    private int selectedIndex = 0;
    private boolean isPaused = false;
    private boolean isStopped = false;
    HomeKeyListener mHomeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        final String streams[] = bundle.getStringArray(PARAM_STREAMS);

        String StreamSize = "_" + String.valueOf(streams.length);

//        RelativeLayout layout = (RelativeLayout) findViewById(getResources().getIdentifier("activity_player" + StreamSize, "layout", getPackageName()));

        if(streams.length == 1) {
            setContentView(R.layout.activity_player_1);
        } else if(streams.length == 2) {
            setContentView(R.layout.activity_player_2);
        } else if(streams.length == 3) {
            setContentView(R.layout.activity_player_3);
        } else if(streams.length == 4) {
            setContentView(R.layout.activity_player_4);
        } else if(streams.length == 5) {
            setContentView(R.layout.activity_player_3);
        }else {
            Log.d("hey", "lumagpas na");
        }



        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().hide();





        mHomeListener = new HomeKeyListener(this);



        mainPlayerContainer = findViewById(getResources().getIdentifier("videoView0Parent" + StreamSize,"id", getPackageName()));

        for (int i = 0; i < streams.length; i++) {
            Log.d("message",String.valueOf(i));
            int resID = getResources().getIdentifier("videoView" + i + StreamSize, "id", getPackageName());
            playerViews.add((NexVideoViewFactory.INexVideoView) findViewById(resID));
            createNewPlayer(this, playerViews.get(i), i == 0, streams[i]);
        }


        mHomeListener.setOnHomePressedListener(new HomeKeyListener.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                Log.d(TAG, "onHomePressed");
                for (int i = 0; i < playerViews.size(); i++) {
                    NexVideoPlayer playerInstance = mSynchronizer.getPlayer(i);
                    playerInstance.pause();

                    NexVideoViewFactory.INexVideoView player = playerViews.get(i);
                    player.onPause();
                }
                isPaused = true;
            }

            @Override
            public void onHomeLongPressed() {
                Log.d(TAG, "onHomeLongPressed");
                for (int i = 0; i < playerViews.size(); i++) {
                    NexVideoPlayer playerInstance = mSynchronizer.getPlayer(i);
                    playerInstance.pause();

                    NexVideoViewFactory.INexVideoView player = playerViews.get(i);
                    player.onPause();
                }
                isPaused = true;
            }
        });
        mHomeListener.start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        for (int i = 0; i < playerViews.size(); i++) {
            NexVideoPlayer playerInstance = mSynchronizer.getPlayer(i);
            if (isPaused) {
                playerInstance.resume();
            } else {
                playerInstance.start();
            }
            NexVideoViewFactory.INexVideoView player = playerViews.get(i);
            player.onResume();
        }
        isPaused = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        isStopped = true;
        mSynchronizer.stop();
        mSynchronizer.release();
        mHomeListener.stop();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isStopped)
            return;

        isPaused = true;

        for (int i = 0; i < playerViews.size(); i++) {
            NexVideoPlayer playerInstance = mSynchronizer.getPlayer(i);
            playerInstance.pause();
            NexVideoViewFactory.INexVideoView player = playerViews.get(i);
            player.onPause();
        }
        isPaused = true;
    }

    private void createNewPlayer(final Context context, final NexVideoViewFactory.INexVideoView videoView, final boolean isMain, final String streamUrl) {
        new Runnable() {
            @Override
            public void run() {
                videoView.setZOrderMediaOverlay(!isMain);
                videoView.setSupportMultiView(true);
                NexVideoPlayer player = new NexVideoPlayer(context, videoView, mSynchronizer, mSynchronizer.getPlayerCount());
                player.open(streamUrl);
            }
        }.run();

        videoView.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMainPlayerContainer(v);
            }
        });
    }

    public void changeMainPlayerContainer(View view) {
        int newIndex = playerViews.indexOf(view);
        if (selectedIndex == newIndex) {
            return;
        }

        //Swap Players
        RelativeLayout smallPlayerContainer = (RelativeLayout) view.getParent();
        ConstraintLayout.LayoutParams mainParams = (ConstraintLayout.LayoutParams) mainPlayerContainer.getLayoutParams();
        ConstraintLayout.LayoutParams smallParams = (ConstraintLayout.LayoutParams) smallPlayerContainer.getLayoutParams();
        mainPlayerContainer.setLayoutParams(smallParams);
        smallPlayerContainer.setLayoutParams(mainParams);

        mSynchronizer.getPlayer(newIndex).updateVideoSize(mainPlayerContainer.getWidth(), mainPlayerContainer.getHeight());
        mSynchronizer.getPlayer(newIndex).setVolume(1);
        mSynchronizer.getPlayer(selectedIndex).updateVideoSize(smallPlayerContainer.getWidth(), smallPlayerContainer.getHeight());
        mSynchronizer.getPlayer(selectedIndex).setVolume(0);

        mainPlayerContainer = smallPlayerContainer;
        selectedIndex = playerViews.indexOf(view);

        for (int i = 0; i < playerViews.size(); i++) {
            NexVideoPlayer player = mSynchronizer.getPlayer(i);
            player.setPlayerType(i == selectedIndex);
        }
    }
}


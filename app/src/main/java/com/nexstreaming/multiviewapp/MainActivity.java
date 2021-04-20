package com.nexstreaming.multiviewapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.nexstreaming.multiviewapp.helper.PermissionManager;
import com.nexstreaming.multiviewapp.model.Stream;
import com.nexstreaming.multiviewapp.model.StreamListResponse;
import com.nexstreaming.multiviewapp.player.PlayerEnginePreLoader;
import com.nexstreaming.nexplayerengine.NexSystemInfo;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class  MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;

    private static final String BASE_URL = "http://192.168.1.213:8860/";

    private PermissionManager mPermissionManager = null;

    public static ArrayList<String> streamUrls = new ArrayList<String>();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    StreamApi streamApi = retrofit.create(StreamApi.class);
    List<Stream> sliderItems = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setPreloader();
        viewPager2 = findViewById(R.id.viewPagerImageSlider);


        Call<StreamListResponse> call = streamApi.getStreamList();
        call.enqueue(new Callback<StreamListResponse>(){

            @Override
            public void onResponse(Call<StreamListResponse> call, Response<StreamListResponse> response) {
                StreamListResponse streamListResponse = response.body();

                sliderItems = streamListResponse.data;
                viewPager2.setAdapter(new SliderAdapter(sliderItems, viewPager2, streamUrls, getApplicationContext()));

                viewPager2.setClipToPadding(false);
                viewPager2.setClipChildren(false);
                viewPager2.setOffscreenPageLimit(3);
                viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

                CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
                compositePageTransformer.addTransformer(new MarginPageTransformer(40));
                compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
                    @Override
                    public void transformPage(@NonNull View page, float position) {
                        float r = 1 - Math.abs(position);
                        page.setScaleY(0.85f + r * 0.15f);
                    }

                });

                viewPager2.setPageTransformer(compositePageTransformer);



            }

            @Override
            public void onFailure(Call<StreamListResponse> call, Throwable t) {

            }
        });

//        sliderItems.add(new SliderItem(R.drawable.image1));
//        sliderItems.add(new SliderItem(R.drawable.image2));
//        sliderItems.add(new SliderItem(R.drawable.image3));

        if (NexSystemInfo.getPlatformInfo() >= NexSystemInfo.NEX_SUPPORT_PLATFORM_MARSHMALLOW) {
            mPermissionManager = new PermissionManager(this);
            mPermissionManager.setPermissionFlags(PermissionManager.REQUEST_STORAGE);
            mPermissionManager.requestPermissions();
        }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mPermissionManager != null)
            mPermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

//    public void onStartClick(View view) {
//        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
//        intent.putExtras(getOptions());
//        startActivity(intent);
//    }

    private void setPreloader() {
        if (!PlayerEnginePreLoader.isLoaded()) {
            int codecMode = 3;
            String libraryPath = this.getApplicationInfo().dataDir + "/";
            PlayerEnginePreLoader.Load(libraryPath, this, codecMode);
        }
    }

//    private Bundle getOptions() {
//        Bundle bundle = new Bundle();
//        bundle.putStringArray(PlayerActivity.PARAM_STREAMS, streamUrls);
//        return bundle;
//    }
}
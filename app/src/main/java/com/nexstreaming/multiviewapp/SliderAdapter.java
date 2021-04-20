package com.nexstreaming.multiviewapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nexstreaming.multiviewapp.model.Stream;
import com.nexstreaming.multiviewapp.model.StreamView;
import com.nexstreaming.multiviewapp.model.StreamViewListResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder>{

    private static final String BASE_URL = "http://192.168.1.213:8860/";

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    StreamApi streamApi = retrofit.create(StreamApi.class);

    private List<Stream> sliderItems;
    private ArrayList<String> streamUrls;
    private ViewPager2 viewPager2;
    private Context parent;

    SliderAdapter(List<Stream> sliderItems, ViewPager2 viewPager2, ArrayList<String> streamUrls, Context parent) {
        this.sliderItems = sliderItems;
        this.viewPager2 = viewPager2;
        this.streamUrls = streamUrls;
        this.parent = parent;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SliderViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.slider_item_container,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        if(!(sliderItems.get(position).image == null)){
            if(sliderItems.get(position).image.contains("data:image/jpeg;base64,")){
                sliderItems.get(position).image = sliderItems.get(position).image.replace("data:image/jpeg;base64,","");
            }
            byte[] decodedBytesImage = Base64.decode(sliderItems.get(position).image, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytesImage, 0, decodedBytesImage.length);
            holder.imageView.setImageBitmap(decodedBitmap);
        }
        holder.txtStreamInfo.setText(sliderItems.get(position).info);
        holder.txtStreamTitle.setText(sliderItems.get(position).name);
        holder.imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();

                Call<StreamViewListResponse> call = streamApi.getStreamViewList(String.valueOf(sliderItems.get(position).streamid));
                call.enqueue(new Callback<StreamViewListResponse>(){

                    @Override
                    public void onResponse(Call<StreamViewListResponse> call, Response<StreamViewListResponse> response) {
                        StreamViewListResponse streamViewListResponse = response.body();
                        List<StreamView> streamView = streamViewListResponse.data;
                        for(int i = 0; i < streamView.size(); i++){
                            String stream = "";
                            stream = streamView.get(i).url.replace("rtmp","http");
                            stream = stream.replace("LiveApp/", "LiveApp/streams/");
                            if(streamView.get(i).primary.equals("1")) {
                                streamUrls.add(0,stream+".m3u8");
                            } else {
                                streamUrls.add(stream+".m3u8");
                            }

                            Log.d("hey", streamUrls.get(i));
                        }

                        String[] listUrls = new String[streamUrls.size()];
                        listUrls = streamUrls.toArray(listUrls);
                        bundle.putStringArray(PlayerActivity.PARAM_STREAMS, listUrls);
                        Intent intent = new Intent(parent, PlayerActivity.class);
                        intent.putExtras(bundle);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        streamUrls.clear();
                        parent.startActivity(intent);
                    }

                    @Override
                    public void onFailure(Call<StreamViewListResponse> call, Throwable t) {

                    }
                });


            }
        });
    }

    @Override
    public int getItemCount() {
        return sliderItems.size();
    }

    class SliderViewHolder extends RecyclerView.ViewHolder {
        private RoundedImageView imageView;
        private TextView txtStreamTitle;
        private TextView txtStreamInfo;

        SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSlide);
            txtStreamTitle = itemView.findViewById(R.id.txtStreamTitle);
            txtStreamInfo = itemView.findViewById(R.id.txtStreamInfo);
        }

        void setImage(SliderItem sliderItem) {
            imageView.setImageResource(sliderItem.getImage());
        }
    }
}

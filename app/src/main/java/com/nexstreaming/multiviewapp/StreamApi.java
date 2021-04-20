package com.nexstreaming.multiviewapp;

import com.nexstreaming.multiviewapp.model.StreamListResponse;
import com.nexstreaming.multiviewapp.model.StreamViewListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface StreamApi {

    @GET("stream")
    Call<StreamListResponse> getStreamList();

    @GET("stream/{viewid}/views")
    Call<StreamViewListResponse> getStreamViewList(@Path("viewid") String viewid);
}

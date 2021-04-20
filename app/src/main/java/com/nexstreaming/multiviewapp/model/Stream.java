package com.nexstreaming.multiviewapp.model;

public class Stream {

    public String name;
    public String stream_created;
    public int streamid;
    public String info;
    public String image;

    public Stream(String name, String steam_created, int streamid, String info, String image){
        this.name = name;
        this.stream_created = stream_created;
        this.streamid = streamid;
        this.info = info;
        this.image = image;
    }


}

package com.nexstreaming.multiviewapp.model;

public class StreamView {

    public String description;
    public int viewid;
    public String view_added;
    public String url;
    public String primary;
    public String image;

    public StreamView(String description, int viewid, String view_added, String url, String primary){
        this.description = description;
        this.viewid = viewid;
        this.view_added = view_added;
        this.url = url;
        this.primary = primary;
        this.image = image;
    }

}

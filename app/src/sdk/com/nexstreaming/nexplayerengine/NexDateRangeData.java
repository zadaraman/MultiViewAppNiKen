package com.nexstreaming.nexplayerengine;

public final class NexDateRangeData {


    /**
     * The unique string used to identify the dataRange.
     */
    public String mID;

    /**
     * The defined client that specifies a set of attributes and their associated semantic values.
     */
    public String mClass;

    /**
     * The start date.
     */
    public String mStartDate;

    /**
     * The end date.
     */
    public String mEndDate;

    /**
     * The SCTE35-CMD data.
     */
    public String mSCTE35CMD;

    /**
     * The SCTE35-IN data.
     */
    public String mSCTE35IN;

    /**
     * The SCTE35-OUT data.
     */
    public String mSCTE35OUT;

    /**
     * The  #EXT-X-DATERANGE tag.
     */
    public String mFullString;

    /**
     * The plan duration
     */
    public int mPlanDuration;

    /**
     * The duration of the dataRange
     */
    public int mDuration;

    /**
     * The END-ON-NEXT value.
     */
    public int  mEndOnNext;

    public NexDateRangeData(String mID, String mClass, String mStartDate, String mEndDate, String mSCTE35CMD, String mSCTE35IN, String mSCTE35OUT, String mFullString, int mPlanDuration, int mDuration , int mEndOnNext) {
        this.mID = mID;
        this.mClass = mClass;
        this.mStartDate = mStartDate;
        this.mEndDate = mEndDate;
        this.mSCTE35CMD = mSCTE35CMD;
        this.mSCTE35IN = mSCTE35IN;
        this.mSCTE35OUT = mSCTE35OUT;
        this.mFullString = mFullString;
        this.mPlanDuration = mPlanDuration;
        this.mDuration = mDuration;
        this.mEndOnNext = mEndOnNext;
    }
}



package com.nexstreaming.nexplayerengine;

/**
 * This class contains information about an event message of the emsg box.
 */
public final class NexEmsgData {

    /**
     * The \c mVersoin.
     */
    public int mVersion;

    /**
     * The \c mFlags.
     */
    public int mFlags;
	
    /**
     * The \c mTimescale provides the timescale, in ticks per second, for the time delta and duration fields within \link NexEmsgData.mVersion mVersion\endlink 0 of emsg box.
     */
    public int mTimescale;

    /**
     * The \c mPresentationTime.
     * 	- If \link NexEmsgData.mVersion mVersion\endlink is 0, provides the Media Presentation time delta of the media presentation time of the event and the earliest presentation time in this segment. The timescale is provided in the \link NexEmsgData.mTimescale mTimescale\endlink field.
     * 	- If \link NexEmsgData.mVersion mVersion\endlink is 1, provides the Media Presentation time of the event measured on the Movie timeline, in the timescale provided in the \link NexEmsgData.mTimescale mTimescale\endlink field.
     */
    public long mPresentationTime;

    /**
     * The \c mEventDuration provides the duration of event in media presentation time. The value 0xFFFF indicates an unknown duration.
     */
    public int mEventDuration;

    /**
     * The \c mId is a field identifying this instance of the message. Messages with equivalent semantics shall have the same value, 
     *  - i.e. processing of any one event message box with the same id is sufficient.
     */
    public int mId;

    /**
     * The \c mSchemeIdUri used identify the message scheme. The semantics and syntax of the \link NexEmsgData.mMessageData mMessageData\endlink are defined by the owner of the scheme identified.
     *  
     *  For SCTE-35 event, the \c mSchemeIdUri is equal to "urn:scte:scte35:2013:bin", "urn:scte:scte35:2013:xml" and "urn:scte:scte35:2014:xml+bin".
     */
    public String mSchemeIdUri;

    /**
     * The \c mValue used to specify the value for the event. 
     */
    public String mValue;

    /**
     * The \c mMessageDataSize is size of the \link NexEmsgData.mMessageData mMessageData\endlink.
     */
    public int mMessageDataSize;

    /**
     * The \c mMessageData is body of the message, which fills the remainder of the message box. This may be empty depending on the above information. The syntax and semantics of this field must be defined by the owner of the scheme identified in the \link NexEmsgData.mSchemeIdUri mSchemeIdUri\endlink field.
     */
    public byte[] mMessageData;

    /**
     * The \c mStartTime provides the actual start time of event in the Media Presentation time. It is expressed in Unix Epoch Time in milliseconds.
     */
    public long mStartTime;

    /**
     * The \c mEndTime provides the actual end time of event in the Media Presentation time. It is expressed in Unix Epoch Time in milliseconds.
     */
    public long mEndTime;


    public NexEmsgData(int mVersion, int mFlags, int mTimescale, long mPresentationTime, int mEventDuration, int mId, String mSchemeIdUri, String mValue, int mMessageDataSize, byte[] mMessageData, long mStartTime, long mEndTime) {
        this.mVersion = mVersion;
        this.mFlags = mFlags;
        this.mTimescale = mTimescale;
        this.mPresentationTime = mPresentationTime;
        this.mEventDuration = mEventDuration;
        this.mId = mId;
        this.mSchemeIdUri = mSchemeIdUri;
        this.mValue = mValue;
        this.mMessageDataSize = mMessageDataSize;
        this.mMessageData = mMessageData;
        this.mStartTime = mStartTime;
        this.mEndTime = mEndTime;
    }
}



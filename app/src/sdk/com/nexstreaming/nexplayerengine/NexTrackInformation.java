package com.nexstreaming.nexplayerengine;

/**
 * Stores and provides information about an individual content track, for formats that
 * use multiple tracks (such as HLS).  See {@link NexContentInformation} for details.
 * 
 * @author NexStreaming Corp.
 */
public final class NexTrackInformation
{
	/** 
	 * The ID of the track.  This is an arbitrary value, not an index, but can be matched
	 * to the currently playing track as indicated by \link NexStreamInformation#mCurrTrackID mCurrTrackID\endlink.
	 * second parameter of the \link NexPlayer#setMediaTrack(int, int) setMediaTrack\endlink.
	 */
	public int mTrackID;

	/** 
	 * The Custom Attribute ID of the track.  In some cases, a stream may have multiple
	 * equivalent tracks.  Setting a custom attribute ID in 
	 * \link NexPlayer#setMediaStream() setMediaStream\endlink causes only tracks
	 * with a matching custom attribute ID to be selected.  A custom attribute ID
	 * represents a particular key/value attribute pair.  The full list of available pairs
	 * and their associated ID values can be found in 
	 * {@link NexStreamInformation#mArrCustomAttribInformation mArrCustomAttribInformation}.
	 * 
	 * Please keep in mind that this is an arbitrary value, not an index into the custom
	 * attribute array. 
	 */
	public int mCustomAttribID;
	
	/**
	 *  Bandwidth of the track in bits per second (bps).
	 */
	public int mBandWidth;
	
	/** 
	 * This indicates the type of track:
	 *  - <b>1</b> : Audio Only
	 *  - <b>2</b> : Video Only
	 *  - <b>3</b> : AV
	 * For local playback, the type will be Audio(1) or Video(2).
	 * For streaming playback, the type can be Audio(1) ,Video(2), or AV(3).
	 */
	public int mType;


	/** This indicates the codec used for the given track.
	 *  \warning Do not trust this value in HLS and MS Smooth Streaming mode, as invalid values are sometimes provided.
	 *
	 * If this track type is video, this value will be one of:
	 *    - \link NexContentInformation#NEXOTI_MPEG4V NEXOTI_MPEG4V\endlink 
	 *    - \link NexContentInformation#NEXOTI_H263 NEXOTI_H263\endlink 
	 *    - \link NexContentInformation#NEXOTI_H264 NEXOTI_H264\endlink 
	 *    - \link NexContentInformation#NEXOTI_WMV NEXOTI_WMV\endlink 
	 *    - \link NexContentInformation#NEXOTI_RV NEXOTI_RV\endlink
	 * 
	 * If this track type is audio, this value will be one of:
	 *    - \link NexContentInformation#NEXOTI_AAC NEXOTI_AAC\endlink
	 *    - \link NexContentInformation#NEXOTI_AAC_GENERIC NEXOTI_AAC_GENERIC\endlink
	 *    - \link NexContentInformation#NEXOTI_AAC_PLUS NEXOTI_AAC_PLUS\endlink
	 *    - \link NexContentInformation#NEXOTI_MPEG2AAC NEXOTI_MPEG2AAC\endlink
	 *    - \link NexContentInformation#NEXOTI_MP3inMP4 NEXOTI_MP3inMP4\endlink
	 *    - \link NexContentInformation#NEXOTI_MP2 NEXOTI_MP2\endlink
	 *    - \link NexContentInformation#NEXOTI_MP3 NEXOTI_MP3\endlink
	 *    - \link NexContentInformation#NEXOTI_BSAC NEXOTI_BSAC\endlink
	 *    - \link NexContentInformation#NEXOTI_WMA NEXOTI_WMA\endlink
	 *    - \link NexContentInformation#NEXOTI_RA NEXOTI_RA\endlink
	 *    - \link NexContentInformation#NEXOTI_AC3 NEXOTI_AC3\endlink
 	 *    - \link NexContentInformation#NEXOTI_EC3 NEXOTI_EC3\endlink
	 *    - \link NexContentInformation#NEXOTI_AC4 NEXOTI_AC4\endlink
	 *    - \link NexContentInformation#NEXOTI_DRA NEXOTI_DRA\endlink
	 * 
	 *   or (in future versions) one of the following speech codec constants:
	 *    - \link NexContentInformation#NEXOTI_AMR NEXOTI_AMR\endlink
	 *    - \link NexContentInformation#NEXOTI_EVRC NEXOTI_EVRC\endlink
	 *    - \link NexContentInformation#NEXOTI_QCELP NEXOTI_QCELP\endlink
	 *    - \link NexContentInformation#NEXOTI_QCELP_ALT NEXOTI_QCELP_ALT\endlink
	 *    - \link NexContentInformation#NEXOTI_SMV NEXOTI_SMV\endlink
	 *    - \link NexContentInformation#NEXOTI_AMRWB NEXOTI_AMRWB\endlink
	 *    - \link NexContentInformation#NEXOTI_G711 NEXOTI_G711\endlink
	 *    - \link NEXOTI_G723\endlink
	 *
	 * \since version 6.2.0	 
	 */
	public int mCodecType; 
	
	/**
	 * Indicates if this track is valid (that is, if the codecs, bit rates, and so on are
	 * supported by NexPlayer&trade;).
	 *  - <b>0</b> : Unsupported or invalid track
	 *  - <b>1</b> : Valid and supported track 
	 */
	public int mValid;
        /** 
         * Indicates if the track is a track of IFrames for the video content or not.
         * 
         * Possible Values:
         *  - <b>0</b>: \c FALSE. Not a track of IFrames.
         *  - <b>1</b>: \c TRUE. A track of IFrames.
         *  
         * \since version 6.34         
         */  
	public boolean mIFrameTrack;

	/** 
	 * This indicates if track includes video track, native passes its Width and Height :
	 */
	public int mWidth; 
	public int mHeight;

	/**
	 * This indicates if track includes video track and AVC, native passes its Profile and Level :
	 */
	public int mAVCProfile;
	public int mAVCLevel;

	public float mFrameRate;

	/** Possible value for NexTrackInformation.mReason */
	public final static int REASON_TRACK_NOT_SUPPORT_VIDEO_CODEC		= 0x0000001;
    /** Possible value for NexTrackInformation.mReason */ 
	public final static int REASON_TRACK_NOT_SUPPORT_AUDIO_CODEC		= 0x0000002;
    /** Possible value for NexTrackInformation.mReason */ 
	public final static int REASON_TRACK_NOT_SUPPORT_VIDEO_RESOLUTION	= 0x0000003;
    /** Possible value for NexTrackInformation.mReason */ 
	public final static int REASON_TRACK_NOT_SUPPORT_VIDEO_RENDER		= 0x0000004;
	
    /**
     * For invalid tracks, this variable indicates the reason they are not currently valid.
     *
     * This may be any of the following values:
     * - <b>\link NexTrackInformation::REASON_TRACK_NOT_SUPPORT_VIDEO_CODEC REASON_TRACK_NOT_SUPPORT_VIDEO_CODEC\endlink</b>
     *          if the player doesn't support the video codec used for this content.
     * - <b>\link NexTrackInformation::REASON_TRACK_NOT_SUPPORT_AUDIO_CODEC REASON_TRACK_NOT_SUPPORT_AUDIO_CODEC\endlink</b>
     *          if the player doesn't support the audio video codec used for this content.
     * - <b>\link NexTrackInformation::REASON_TRACK_NOT_SUPPORT_VIDEO_RESOLUTION REASON_TRACK_NOT_SUPPORT_VIDEO_RESOLUTION\endlink</b>
     *          if the track is locked out because the video resolution is too high to play, as determined by the settings of the
     *          MAX_HEIGHT and MAX_WIDTH properties.
     * - <b>\link NexTrackInformation::REASON_TRACK_NOT_SUPPORT_VIDEO_RENDER REASON_TRACK_NOT_SUPPORT_VIDEO_RENDER\endlink</b>
     *          if the track was locked out because the video renderer wasn't capable of playing it smoothly (the resolution and/or bit rate too high).
     */
	public int mReason;

	protected NexTrackInformation( int iTrackID, int iCustomAttribID, int iBandWidth, int iType, int codecType, int iValid, int iReason, boolean iIFrameTrack)
	{
		mTrackID = iTrackID;
		mCustomAttribID = iCustomAttribID;
		mBandWidth = iBandWidth;
		mType = iType;
		mCodecType = codecType;
		mValid = iValid;
		mReason = iReason;
		mIFrameTrack = iIFrameTrack;
	}

	
	// overloading NexTrackInformation 
	protected NexTrackInformation( int iTrackID, int iCustomAttribID, int iBandWidth, int iType, int codecType, int iValid, int iReason, boolean iIFrameTrack, int iWidth, int iHeight, int iAVCProfile, int iAVCLevel, double iFrameRate)
	{
		mTrackID = iTrackID;
		mCustomAttribID = iCustomAttribID;
		mBandWidth = iBandWidth;
		mType = iType;
		mCodecType = codecType;
		mValid = iValid;
		mReason = iReason;
		mIFrameTrack = iIFrameTrack;

		mWidth = iWidth; 
		mHeight = iHeight;

		mAVCProfile = iAVCProfile;
		mAVCLevel 	= iAVCLevel;
		mFrameRate 	= (float)iFrameRate;
	}
}

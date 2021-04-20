package com.nexstreaming.nexplayerengine;


/**
 * \brief This class provides information on a content stream.  
 * 
 * Content streams are listed in the
 *  {@link NexContentInformation#mArrStreamInformation mArrStreamInformation}
 *  member of {@link NexContentInformation}. See there for details.
 *  
 * @author NexStreaming Corp.
 *
 */
public final class NexStreamInformation
{
	/** 
	 * This is a unique integer used to identify the current stream in API calls.
	 * For example, this is used when calling 
	 * \link NexPlayer#setMediaStream(int, int, int, int) setMediaStream\endlink.
	 * 
	 */
	public int 		mID;
	
	/** 
	 * This indicates the stream type (audio or video).
	 * This is one of:
	 *   - <b>(0x00)</b> {@link NexPlayer#MEDIA_STREAM_TYPE_AUDIO MEDIA_STREAM_TYPE_AUDIO}
	 *   - <b>(0x01)</b> {@link NexPlayer#MEDIA_STREAM_TYPE_VIDEO MEDIA_STREAM_TYPE_VIDEO}
	 *   - <b>(0x02)</b> {@link NexPlayer#MEDIA_STREAM_TYPE_TEXT MEDIA_STREAM_TYPE_TEXT}
	 *   - <b>(0x00)</b> {@link NexPlayer#MEDIA_TRACK_TYPE_AUDIO MEDIA_TRACK_TYPE_AUDIO}
	 */
	public int 		mType;
	
	/**
	 * The name of the media stream, for streaming formats that
	 * have named streams.  This is an arbitrary value set by the
	 * author, and is generally intended for user display (to allow
	 * the user to select among different alternative streams).
	 * 
	 */
	public NexID3TagText mName;
	//public String	mName;
	
	/**
	 * \brief  This is the language of the media stream, for streaming formats that
	 *         include language data.  
	 * 
	 * This is an arbitrary value set by the
	 * author, and is intended for user display (to allow users to
	 * select among different alternative streams).  Applications
	 * should NOT rely on this being any particular format; it is
	 * most likely to be the display name of the language, but may
	 * be any string.
	 */
	public NexID3TagText mLanguage;
	//public String	mLanguage;
	
	/**
	 * This indicates the number of custom attributes associated with this stream.
	 */
	public int 		mAttrCount;
	
	/**
	 * This is the number of tracks associated with this stream.  This is the
	 * same as the length of \c mArrTrackInformation, and may be zero.
	 */
	public int 		mTrackCount;

	/**
	 * \brief This is the ID of the track within the stream that is currently
	 * playing, or -1 if no track in this stream is currently playing.
	 *        
	 * This ID matches a value in <code>mArrTrackInformation[].mTrackID</code>.
	 * If the \c mArrTrackInformation array is empty, this value
	 * is undefined.
	 * 
	 */
	public int		mCurrTrackID;
	
	/**
	 * The ID of the custom attribute within this stream that is currently
	 * active, or -1 if no custom attribute in this stream is currently active.
	 * This ID matches a value in \c NexCustomAttribInformation[].mID.
	 * If the \c NexCustomAttribInformation array is empty, this value
	 * is undefined.
	 */
	public int 		mCurrCustomAttrID;
	
	/**
	 * For HLS content, this indicates whether the track is a normal audio video track
	 * or a track of only I-frames.  For an I-frame-only track, this value will be 1, 
	 * and for other kinds of tracks, it will be 0.  Since other kinds of content do not include
	 * any I-frame-only tracks, this value will always be 0 for content other than HLS.
	 * 
	 *  \since version 5.12
	 */
	public int		mIsIframeTrack;

	/**
	 * This indicates whether a track is disabled or not.
	 * Tracks will be disabled if the content requires an unsupported codec (audio or video).
	 *
	 * \note  This value should only be used for verification and should not be changed.  This value
	 * is only for NexPlayer&trade;'s internal use.
	 *
	 * \since version 5.16
	 */
	public int		mIsDisabled;

    /**
     * \brief This is the INSTREAM-ID TAG of the media stream.
     *
     * This is an arbitrary value set by the
     * author, and is intended for user displaying (to allow users to
     * select among different alternative streams).
     *
     * For HLS content, this value is filled when the TYPE attribute is CLOSED-CAPTIONS.
     * In this case, this value must be one of the following values:
     * "CC1", "CC2", "CC3", "CC4" or "SERVICEn", where n must be an integer between 1 and 63.
     *
     * \since version 6.54
     */
	public String mInStreamID;

	/**
	 * For formats such as HLS that support multiple tracks for
	 * a given stream, this is an array containing information on
	 * each track associated with the stream.  This may be an
	 * empty array for formats that don't have track information.
	 */
	public NexTrackInformation[] mArrTrackInformation;
	
	/**
	 * This is an array of the custom attributes associated with the current
	 * stream, for formats such as Smooth Streaming that support
	 * custom attributes.
	 */
	public NexCustomAttribInformation[] mArrCustomAttribInformation;


	/**
	 * The type of codec available in the stream.
	 *
	 * it will be set to 0 until the stream is downloaded.
	 * If this is for CEA 608/708 captions embedded in the content, it will be represented as NexContentInformation.NEX_TEXT_CEA.
	 */
	public int mRepresentCodecType;
	
	/**
	 * This is the sole constructor for NexStreamInformation. The parameters match
	 * the members of the class one-to-one.  Generally, it is not
	 * necessary to call the constructor; rather, objects of this class
	 * are created by NexPlayer&trade;&nbsp;internally and made available through
	 * \link NexContentInformation#mArrStreamInformation mArrStreamInformation\endlink.
	 * 
	 * \param iID			Initial value for \c mID member.
	 * \param iType			Initial value for \c mType member.
	 * \param currCustomAttrId Initial value for \c mCurrCustomAttrId member.
	 * \param currTrackId 	Initial value for \c mCurrTrackId member.
	 * \param isIFrameTrack	Initial value for \c mIsIframeTrack member.
	 * \param isDisabled           Initial value for \c mIsDisabled member.
	 * \param name			Initial value for \c mName member.
	 * \param language		Initial value for \c mLanguage member.
	 * \param strInStreamID Initial value for \c mInStreamID.
	 * \param representCodecType Initial value for \c mRepresentCodecType.
	 */
	public NexStreamInformation( int iID, int iType, int currCustomAttrId, int currTrackId, int isIFrameTrack, int isDisabled,  NexID3TagText name, NexID3TagText language, String strInStreamID, int representCodecType)
	{
		mID = iID;
		mType = iType;
		mName = name;
		mLanguage = language;
		mCurrCustomAttrID = currCustomAttrId;
		mCurrTrackID = currTrackId;
		mIsIframeTrack = isIFrameTrack;
		mIsDisabled = isDisabled;
		mInStreamID = strInStreamID;
		mRepresentCodecType = representCodecType;
	}
	
	/*public void clearStreamInformation()
	{
		mID		= 0;
		mType	= 0;
		mName	= "";
		mLanguage = "";
		
		mAttrCount = 0;
		mTrackCount = 0;
	}*/
	
	@SuppressWarnings("unused")		// Called from native code
	private void copyCustomAttribInformation(NexCustomAttribInformation[] customAttribInformation)
	{
		mArrCustomAttribInformation = customAttribInformation;
		mAttrCount = mArrCustomAttribInformation.length;
	}
	
	@SuppressWarnings("unused")		// Called from native code
	private void copyTrackInformation(NexTrackInformation[] trackInformation)
	{
		mArrTrackInformation = trackInformation;
		mTrackCount = mArrTrackInformation.length;
	}
}

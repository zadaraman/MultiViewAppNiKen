package com.nexstreaming.nexplayerengine;


/**
 * Provides information on content.  This is returned by \link NexPlayer.getContentInfo\endlink. See that
 * method for details.
 * 
 * @author NexStreaming Corp.
 *
 */
public final class NexContentInformation
{
	// --- Video Codecs ---
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_MPEG4V = 0x00000020;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_H263 = 0x000000C0;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_H264 = 0x000000C1;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
    public static final int NEXOTI_HEVC = 0x10010400;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_WMV = 0x5F574D56;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_RV = 0x000000DB;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_MPEG1 = 0x000000F2;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_MPEG2 = 0x000000F3;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_MPEG4Sv1 = 0x00000001;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_MPEG4Sv2 = 0x00000002;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_S263 = 0x000000C2;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_WMV1 = 0x31564d57;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_WMV2 = 0x32564d57;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_WMV3 = 0x33564d57;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_WVC1 = 0x31435657;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_MP43 = 0x3334504d;
	
	// --- Audio Codecs ---
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_AAC = 0x00000040;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_AAC_GENERIC = 0x00000041;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_AAC_PLUS = 0x00000041;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_MPEG2AAC = 0x00000067;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_MP3inMP4 = 0x0000006B;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_MP2 = 0x00000021;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_MP3 = 0x0000016B;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_BSAC = 0x00000016;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_WMA = 0x5F574D41;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_RA = 0x000000DA;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_AC3 = 0x00002000;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_EC3 = 0x00002001;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_AC4 = 0x00002002;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_DRA = 0x000000E0;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_DTS = 0x40000003;

	// --- Speech Codecs ---
	/** Possible speech codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink.
	 * This is not supported in the current version; do not use it. 
	 * @deprecated Not supported in the current version; do not use. */
	public static final int NEXOTI_AMR = 0x000000D0;
	/** Possible speech codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. 
	 * This is not supported in the current version; do not use it.
	 * @deprecated Not supported in the current version; do not use.  */
	public static final int NEXOTI_EVRC = 0x000000D1;
	/** Possible speech codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. 
	 * This is not supported in the current version; do not use it.
	 * @deprecated Not supported in the current version; do not use.  */
	public static final int NEXOTI_QCELP = 0x000000D2;
	/** Possible speech codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink.
	 * This is not supported in the current version; do not use it. 
	 * @deprecated Not supported in the current version; do not use.  */
	public static final int NEXOTI_QCELP_ALT = 0x000000E1;
	/** Possible speech codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink.
	 * This is not supported in the current version; do not use it. 
	 * @deprecated Not supported in the current version; do not use.  */
	public static final int NEXOTI_SMV = 0x000000D3;
	/** Possible speech codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink.
	 * This is not supported in the current version; do not use it. 
	 * @deprecated Not supported in the current version; do not use.  */
	public static final int NEXOTI_AMRWB = 0x000000D4;
	/** Possible speech codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. 
	 * This is not supported in the current version; do not use it.
	 * @deprecated Not supported in the current version; do not use.  */
	public static final int NEXOTI_G711 = 0x000000DF;
	/** Possible speech codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. 
	 * This is not supported in the current version; do not use it. 
	 * @deprecated Not supported in the current version; do not use.  */
	public static final int NEXOTI_G723 = 0x000000DE;

	// --- Text Codecs ---
	/** Possible text codec for subtitles. This is not supported in the current version; do not use it.
	 * @deprecated Not supported in the current version; do not use.*/
	public static final int NEXOTI_TEXT_3GPP = 0x000000E0;
	/** Possible text codec for subtitles. This is not supported in the current version; do not use it.
	 * @deprecated Not supported in the current version; do not use.*/
	public static final int NEXOTI_TEXT_SKT = 0x000000E2;


	// ---Caption Types---
	/** Possible caption type for subtitles for \link NexContentInformation.mCaptionType mCaptionType\endlink, when content includes subtitles in an unrecognized format.*/
	public static final int NEX_TEXT_UNKNOWN = 0x00000000;
	/** Possible caption type for subtitles for \link NexContentInformation.mCaptionType mCaptionType\endlink, when content includes SMI subtitles.*/
	public static final int NEX_TEXT_EXTERNAL_SMI = 0x00000002;
	/** Possible caption type for subtitles for \link NexContentInformation.mCaptionType mCaptionType\endlink, when content includes SRT subtitles.*/
	public static final int NEX_TEXT_EXTERNAL_SRT = 0x00000003;
	/** Possible caption type for subtitles for \link NexContentInformation.mCaptionType mCaptionType\endlink, when content includes SUB subtitles.*/
	public static final int NEX_TEXT_EXTERNAL_SUB = 0x00000004;
	/** Possible caption type for subtitles for \link NexContentInformation.mCaptionType mCaptionType\endlink, when content includes timed text.*/
	public static final int NEX_TEXT_3GPP_TIMEDTEXT = 0x50000000;
	/** Possible caption type for subtitles for \link NexContentInformation.mCaptionType mCaptionType\endlink, when content includes WebVTT text tracks.*/
	public static final int NEX_TEXT_WEBVTT = 0x50000001;
	/** Possible caption type for subtitles for \link NexContentInformation.mCaptionType mCaptionType\endlink, when content includes TTML timed text.*/
	public static final int NEX_TEXT_TTML = 0x50000002;
	/** Possible caption type for subtitles for \link NexContentInformation.mCaptionType mCaptionType\endlink, content can include CEA-608 or CEA-708.*/
	public static final int NEX_TEXT_CEA = 0x50000010;
	/** Possible caption type for subtitles for \link NexContentInformation.mCaptionType mCaptionType\endlink, when content includes CEA-608.*/
	public static final int NEX_TEXT_CEA608 = 0x50000011;
	/** Possible caption type for subtitles for \link NexContentInformation.mCaptionType mCaptionType\endlink, when content includes CEA-708.*/
	public static final int NEX_TEXT_CEA708 = 0x50000012;
	

	/** Possible H.264 profile value for \link NexContentInformation.mVideoProfile mVideoProfile\endlink. */
	public static final int H264_BASELINE_PROFILE = 66;	
	/** Possible H.264 profile value for \link NexContentInformation.mVideoProfile mVideoProfile\endlink. */	
	public static final int H264_MAIN_PROFILE = 77;	
	/** Possible H.264 profile value for \link NexContentInformation.mVideoProfile mVideoProfile\endlink. */
	public static final int H264_HIGH_PROFILE = 100;	
	
	/** Type of media that has been opened.
	 * 
	 * <b>Possible Values:</b>
	 *    - <b>1</b> : Audio Only
	 *    - <b>2</b> : Video Only
	 *    - <b>3</b> : AV
	 *
	 */
	// XXX:  Should this use constants or an enum?
	public int mMediaType;
	
	/** Length of open media in milliseconds. 
	 *If the value is -1, the media is live content. */
	public int mMediaDuration;
	
	/** Classification of video codec.
	 * 
	 * <b>Possible Values:</b>
	 *    - <b>0</b> : SW codec
	 *    - <b>1</b> : HW codec
	 *
	 */
	public int mVideoCodecClass;
	
	/** 
	 * Video codec used by the currently open media. This is one of:
	 *    - \link NexContentInformation#NEXOTI_MPEG4V NEXOTI_MPEG4V\endlink 
	 *    - \link NexContentInformation#NEXOTI_H263 NEXOTI_H263\endlink 
	 *    - \link NexContentInformation#NEXOTI_H264 NEXOTI_H264\endlink 
	 *    - \link NexContentInformation#NEXOTI_WMV NEXOTI_WMV\endlink 
	 *    - \link NexContentInformation#NEXOTI_RV NEXOTI_RV\endlink 
	 * 
	 */
	public int mVideoCodec;
	
	/** 
	 * Video FourCC used by the currently open media.
	 * 
	 */	
	public int mVideoFourCC;
	
	/** 
	 * Video codec profile used by the currently open media.
	 * Currently, only H.264 profiles are supported.
	 * 
	 * This is one of:
	 *    - \link NexContentInformation#H264_BASELINE_PROFILE H264_BASELINE_PROFILE\endlink 
	 *    - \link NexContentInformation#H264_MAIN_PROFILE H264_MAIN_PROFILE\endlink 
	 *    - \link NexContentInformation#H264_HIGH_PROFILE H264_HIGH_PROFILE\endlink 
	 */	
	public int mVideoProfile;
	
	/** 
	 * Video codec level used by the currently open media. 
	 * Currently, only H.264 is supported.
	 * 
	 */	
	public int mVideoLevel;
	
	/** 
	 * Error information about video codec.  This is one of:
	 *    - NexErrorCode.NONE
	 *    - NexErrorCode.NOT_SUPPORT_VIDEO_CODEC
	 *    - NexErrorCode.NOT_SUPPORT_VIDEO_RESOLUTION
	 *    - NexErrorCode.SYSTEM_FAIL    
	 *    - NexErrorCode.NOT_SUPPORT_DEVICE  
	 */		
	public int mVideoCodecError;
	
	// XXX:  What are width and height for audio-only 
	//       sources?  Are they zero?  Undefined?
	/** Width of the video, in pixels */
	public int mVideoWidth;
	/** Height of the video, in pixels */
	public int mVideoHeight;
	
	/** 
	 * \brief Frame rate of the video, in frames per second.  This is the frame rate specified in the content.
	 * 
	 * If the device isn't powerful enough to decode and
	 * display the video stream in real-time, the actual number
	 * of displayed frames may be lower than this value.
	 * 
	 * For the actual number of displayed frames, call
	 * \link NexPlayer#getContentInfoInt(int) getContentInfoInt\endlink
	 * with the value \n \c CONTENT_INFO_INDEX_VIDEO_RENDER_AVE_DSP.
	 * 
	 */
	public int mVideoFrameRate;
	
	/** Bit rate of the video, in bits per second */
	public int mVideoBitRate;
	
	/** 
	 * Audio codec used by the currently open media. 
	 * 
	 * This can be one of the following audio codec constants:
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
	 *    - \link NexContentInformation#NEXOTI_DRA NEXOTI_DRA\endlink
	 *    - \link NexContentInformation#NEXOTI_DTS NEXOTI_DTS\endlink
	 * 
	 * Or (in future versions) one of the following speech codec constants:
	 *    - \link NexContentInformation#NEXOTI_AMR NEXOTI_AMR\endlink
	 *    - \link NexContentInformation#NEXOTI_EVRC NEXOTI_EVRC\endlink
	 *    - \link NexContentInformation#NEXOTI_QCELP NEXOTI_QCELP\endlink
	 *    - \link NexContentInformation#NEXOTI_QCELP_ALT NEXOTI_QCELP_ALT\endlink
	 *    - \link NexContentInformation#NEXOTI_SMV NEXOTI_SMV\endlink
	 *    - \link NexContentInformation#NEXOTI_AMRWB NEXOTI_AMRWB\endlink
	 *    - \link NexContentInformation#NEXOTI_G711 NEXOTI_G711\endlink
	 *    - \link NexContentInformation#NEXOTI_G723 NEXOTI_G723\endlink
	 * 
	 */
	public int mAudioCodec;

	/**
	 *
	 * \brief Additional information about the audio codec in use, namely the FOURCC (or "four character code") of the codec.
	 *
	 *  When the audio codec of the current content is DTS, this will be one of the following values:
	 *			- 0x64747363 : DTSC
	 *			- 0x64747363 : DTSH
	 *			- 0x64747363 : DTSL
	 *			- 0x64747363 : DTSE
	 *
	 * \since version 6.4.1
	 *
	 * \warning This information is not supported by current API version.
	 */
	public int mAudioFourCC;

	/** Classification of audio codec.
	 * 
	 * <b>Possible Values:</b>
	 *    - <b>0</b> : SW codec
	 *    - <b>1</b> : HW codec
	 *
	 */
	public int mAudioCodecClass;

	/** Audio sampling rate in samples per second. */
	public int mAudioSamplingRate;
	/** Number of audio channels. */
	public int mAudioNumOfChannel;
	/** Audio bitrate, in bits per second. */
	public int mAudioBitRate;


	/**
	 * \brief One of the possible values for \c mIsSeekable in \link NexContentInformation.mIsSeekable \endlink.
	 *  This indicates that seeking is not available in the current content.
	 *  
     * \since version 6.40
	 */
	public static final int NEXPLAYER_SEEKABLE_NONE			= 0;
	/**
	 * \brief One of the possible values for \c mIsSeekable in \link NexContentInformation.mIsSeekable \endlink.
	 *  This indicates that the player can seek within the current content.
	 * 
     * \since version 6.40
	 */
	public static final int NEXPLAYER_SEEKABLE_IN_CONTENT 	= 1;
	/**
	 * \brief One of the possible values for \c mIsSeekable in \link NexContentInformation.mIsSeekable \endlink.
	 *  This indicates that the player can seek within the prefetch buffer of the current content.
     *
     * \since version 6.40
	 */
	public static final int NEXPLAYER_SEEKABLE_IN_BUFFER 	= 2;
	
    /**
	 * \brief One of the possible values for \c mIsSeekable in \link NexContentInformation.mIsSeekable \endlink.
	 *  This indicates that the player can only seek to 0 seconds in the current content (in other words, 
     *  the player can seek and return to the beginning of the content).
     * 
     * \since version 6.40
	 */
	public static final int NEXPLAYER_SEEKABLE_TO_ZERO 		= 4;

	/**
	 * \brief Whether or not it is possible for the player to seek in the current content.
	 * 
     * Depending on the kind of content playing, seeking may or may not be possible throughout the content or the prefetch buffer
     * that contains content data that has already been received but not yet played.
     *
	 * The possible seek modes in content are indicated by an integer from 0 to 7, and are described as follows:
	 *
     * -# <b>NEXPLAYER_SEEKABLE_NONE:</b> Seeking is not available in the current content.
     * -# <b>NEXPLAYER_SEEKABLE_IN_CONTENT:</b> Seeking is possible within the current content.    
     * -# <b>NEXPLAYER_SEEKABLE_IN_BUFFER:</b> Seeking is possible within the prefetch buffer.  
     * -# <b>NEXPLAYER_SEEKABLE_IN_CONTENT | NEXPLAYER_SEEKABLE_IN_BUFFER:</b> Seeking is possible within the current content or the prefetch buffer. 
     * -# <b>NEXPLAYER_SEEKABLE_TO_ZERO:</b> Can only seek to 0 seconds. (The only I-frame within the current content is at 0 seconds.)    
     * -# <b>NEXPLAYER_SEEKABLE_IN_CONTENT | NEXPLAYER_SEEKABLE_TO_ZERO:</b> Seeking is possible within the current content or to 0 seconds. 
     * -# <b>NEXPLAYER_SEEKABLE_IN_BUFFER | NEXPLAYER_SEEKABLE_TO_ZERO:</b> Seeking is possible within the prefetch buffer or to 0 seconds. 
     * -# <b>NEXPLAYER_SEEKABLE_IN_CONTENT | NEXPLAYER_SEEKABLE_IN_BUFFER | NEXPLAYER_SEEKABLE_TO_ZERO:</b> Seeking is possible within the current content, the prefetch buffer, and to 0 seconds. 
	 * 
	 * \since version 6.40
	 */
	public int mIsSeekable;
	/** If the media supports pausing, this is 1; otherwise it is 0. */
	public int mIsPausable;
	
	//**
	// * \brief ID of currently playing track, for content that has multiple tracks.
	// * 
	// * The set of values is dependent on the format.  For example, in HLS streaming,
	// * this is the ID associated with the track in the HLS play list.<p>
	// * 
	// * Note that this is <b>not</b> a channel or stream number; the track refers to
	// * a track in the sense that HLS uses it: the same content in an alternate format
	// * or with an alternate resolution or bit rate. <p>
	// * 
	// * Although the values used here are arbitrary and format-dependent, then can be
	//* used to look up the current track in {@link NexContentInformation#mArrTrackInformation}
	// * by searching for an entry where the entry's <code>mTrackID</code> is equal to 
	// * <code>mCurrTrackID</code>.<p>
	// */
	//public int mCurrTrackID;
	
	/* The number of tracks in content.<p>
	 *
	 * Note that this does <b>not</b> refer to channels or streams; these are tracks in
	 * the sense that HLS uses the term: the same content in alternate formats
	 * or with alternate resolutions or bit rates. <p>
	 */
	//public int mTrackNum;
	
	/**
	 * The picture associated with the current content, for formats such
	 * as MP3 and AAC that can have an optional associated still image.
	 * 
	 * This is generally used in place of video for content that does not
	 * have video.  The exact use of the still image is up to the content
	 * producer. In the case of an MP3 or AAC audio file, it is usually
	 * the album cover artwork.  In the case of HTTP Live Streaming,
	 * audio-only tracks often have a still image to be shown in place of
	 * the video.
	 */
	public NexID3TagInformation mID3Tag;
	
	/**
	 * ID of currently selected video stream, for content types with
	 * multiple streams. This matches the ID member of an entry in the
	 * NexContentInformation.mArrStreamInformation() array.
	 */
	public int mCurrVideoStreamID;
	
	/**
	 * ID of currently selected audio stream, for content types with
	 * multiple streams. This matches the ID member of an entry in the
	 * NexContentInformation.mArrStreamInformation() array.
	 */
	public int mCurrAudioStreamID;

	/**
	 * ID of currently selected text stream, for content types with
	 * multiple streams. This matches the ID member of an entry in the
	 * NexContentInformation.mArrStreamInformation() array.
	 */
	public int mCurrTextStreamID;
	
	/**
	 * The number of streams (audio and video) available for the current
	 * content.  This is the same as the length of the 
	 * NexContentInformation.mArrStreamInformation() array.
	 * For formats that don't support multiple streams, this may be zero, or it
	 * may describe a single default stream.
	 */
	public int mStreamNum;
	
	/**
	 * The type of captions available in the current content.  
	 * 
	 * This value will be set after \c NexPlayer.open is called and will be one of: 
	 * 		- \link NexContentInformation#NEX_TEXT_UNKNOWN NEX_TEXT_UNKNOWN \endlink = 0x00000000: Unknown caption format.
	 * 		- \link NexContentInformation#NEX_TEXT_EXTERNAL_SMI NEX_TEXT_EXTERNAL_SMI\endlink = 0x00000002: SMI subtitles.
	 * 		- \link NexContentInformation#NEX_TEXT_EXTERNAL_SRT NEX_TEXT_EXTERNAL_SRT\endlink = 0x00000003:  SRT subtitles.
	 * 		- \link NexContentInformation#NEX_TEXT_3GPP_TIMEDTEXT NEX_TEXT_3GPP_TIMEDTEXT\endlink = 0x50000000: 3GPP timed text.
	 * 		- \link NexContentInformation#NEX_TEXT_WEBVTT NEX_TEXT_WEBVTT\endlink = 0x50000001:  WebVTT text tracks.
	 * 		- \link NexContentInformation#NEX_TEXT_TTML NEX_TEXT_TTML\endlink = 0x50000002:  TTML timed text.
	 * 
	 * In the current version, only the types listed above will be recognized and if captions in another format exist, this will be
	 * set to NEX_TEXT_UNKNOWN. 
	 * 
	 * Furthermore, if an external subtitle file is included in addition to another format, this member will be set to the external subtitle type (SMI or SRT).
	 * 
	 * Since CEA 608 and CEA 708 closed captions cannot be identified until decoding begins, \c mCaptionType will also be set to NEX_TEXT_UNKNOWN 
	 * when they are included in content.
	 * 
	 * \since version 6.9
	 */
	public int mCaptionType;

       /**	
        * Additional information about the type of content being played.
        * 
        * Possible values of \c mSubSrcType include:
        * 	- 0  : type none : When the type of content is unknown.
        * 	- 1  : 3GPP RTSP
        * 	- 2  : Real Media Type
        * 	- 3  : MS RTSP
        * 	- 4  : Window Media Streaming
        * 	- 5  : HTTP Live Streaming
        * 	- 6  : Smooth Streaming
        * 	- 7  : MPEG DASH
        * 	- 8  : Progressive Download
        * 	- 9  : Remote File Cache
        * 	- 10 : Rich Communication Service
        * 	- 11 : Local  
        * 
        * \since version 6.11
        */
	public int mSubSrcType;

	/**
	 * Additional information about the rotation degree of video content being played.
	 *
	 * This value will be one of the following:
	 * - 0
	 * - 90
	 * - 180
	 * - 270
	 *
	 * \since version 6.60
	 */
	public int mRotationDegree;

	public boolean mTunnelingMode;

	public boolean mPassThrough;

	public boolean mDolbyVision;

	public String mVideoCodecName;

	public boolean mSecureSurface;

	/*public void clearContentInformation()
	{
		mMediaType = 0;
		mMediaDuration = 0;
		mVideoCodec = 0;
		mVideoWidth = 0;
		mVideoHeight = 0;
		mVideoFrameRate = 0;
		mVideoBitRate = 0;
		mAudioCodec = 0;
		mAudioSamplingRate = 0;
		mAudioNumOfChannel = 0;
		mAudioBitRate = 0;
		mIsSeekable = 0;
		mIsPausable = 0;
		
		mCurrVideoStreamID = 0xFFFFFFFF;
		mCurrAudioStreamID = 0xFFFFFFFF;
		mStreamNum = 0;
	}*/
	
	/*
	 * An array describing the tracks that are available for the current content,
	 * for formats such as HLS that support multiple tracks.  These are tracks in
	 * the HLS sense, not channels or streams.  See {@link NexTrackInformation} for
	 * details.
	 */
	//public NexTrackInformation[] mArrTrackInformation;
	
	/*
	 * Sets the value of the <code>mArrTrackInformation</code> member variable.<p>
	 * 
	 * @param trackInformation new value for mArrTrackInformation
	 */
	/*
	public void copyTrackInformation(NexTrackInformation[] trackInformation)
	{
		NexLog.d("CONTENT INFO", "copyTrackInformation()");
		mArrTrackInformation = trackInformation;
	}
	*/
	
	/**
	 * \brief This is an array describing the streams that are available for the current content.
	 * 
	 * Streams have semantically different content (for example, presented in
	 * different languages or presented from different camera angles) and are
	 * generally intended to be selected via the user interface.
	 * 
	 * For each stream, there can be multiple tracks.  Generally, tracks contain
	 * content that is equivalent but presented with different trade-offs between
	 * quality and bandwidth.  While streams are selected by the user, tracks are
	 * usually selected automatically based on available bandwidth and system
	 * capabilities, in order to provide the best experience to the user.
	 * 
	 * For formats that do not support multiple streams or multiple tracks, these
	 * arrays may be empty or may contain only a single element.
	 * 
	 */
	public NexStreamInformation[] mArrStreamInformation;
	
	@SuppressWarnings("unused")		// Called from native code
	private void copyStreamInformation(NexStreamInformation[] streamInformation)
	{
		mArrStreamInformation = streamInformation;
	}

	/**
	 * A list of the caption languages available for the current content (for
	 * example, from a subtitle file).  The format of this depends on the
	 * caption file.  This may be the language name spelled out, or it may be
	 * a language identifier such as EN for English or KR for Korean.
	 * 
	 * For SMI files, this is the class name of a given subtitle track, as specified
	 * in the SMI file.
	 * 
	 * There is currently no way to access the additional data associated with a
	 * subtitle track, but it is possible to guess the language (and therefore the encoding)
	 * indirectly from the track's class name.
	 * 
	 * Although the class name is arbitrary,
	 * many files follow the convention "LLCCTT" where LL is the language (EN for English,
	 * KR for Korean and so on), CC is the country (and may be omitted) and TT is the type
	 * (for example "CC" for closed captions).
	 * 
	 * For example, "ENUSCC" would be EN(English), US(United States), CC(Closed Captions).
	 * "KRCC" would be KR(Korean), CC(Closed Captions).
	 * 
	 * Currently, the safest way is to check only the first two letters of the class name
	 * to find the language, and assume the most common encoding for that language.
	 * 
	 */
	public String[] mCaptionLanguages;
	
	@SuppressWarnings("unused")		// Called from native code
	private void copyCaptionLanguages(String[] captionLanguages)
	{
		mCaptionLanguages = captionLanguages;
	}

	/**
	 * The value of the first #EXT-X-PROGRAM-DATE-TIME tag in the HLS playlist,
	 */
 	public String mFirstProgramDateTimeTag;

	@SuppressWarnings("unused")		// Called from native code
	private void copyProgramDateTimeTag(String ProgramDateTimeTag)
	{
		mFirstProgramDateTimeTag = ProgramDateTimeTag;
	}

 	/**
     * \brief This method converts codec information to mime type
     * \return      Returns mime type of the given codec type
     */
	protected static String getMediaCodecMimeType(int mediaType, int codecType ) {
		if( mediaType == 2 || mediaType == 3) { 	// video or Audio/Video
			switch ( codecType ) {
				case NexContentInformation.NEXOTI_MPEG4V:
				case NexContentInformation.NEXOTI_MPEG4Sv1:
				case NexContentInformation.NEXOTI_MPEG4Sv2:
				case NexContentInformation.NEXOTI_MP43:
					return "video/mp4v-es";
				case NexContentInformation.NEXOTI_H263:
					return "video/3gpp";
				case NexContentInformation.NEXOTI_H264:
					return "video/avc";
				case NexContentInformation.NEXOTI_HEVC:
					return "video/hevc";
				case NexContentInformation.NEXOTI_WMV:
				case NexContentInformation.NEXOTI_WMV1:
				case NexContentInformation.NEXOTI_WMV2:
				case NexContentInformation.NEXOTI_WMV3:
					return "video/x-ms-wmv";
				case NexContentInformation.NEXOTI_RV:
					return "video/vnd.rn-realvideo";
				case NexContentInformation.NEXOTI_MPEG1:
					return "video/mpeg";
				case NexContentInformation.NEXOTI_MPEG2:
					return "video/mpeg2";
				case NexContentInformation.NEXOTI_S263:
					return "video/3gpp2";
				case NexContentInformation.NEXOTI_WVC1:
					return "video/wvc1";
				default:
					return Integer.toString(codecType);
			}
		}
		else if( mediaType == 1 ){	// audio
			switch ( codecType ) {
				case NexContentInformation.NEXOTI_AAC:
				case NexContentInformation.NEXOTI_MPEG2AAC:
					return "audio/mp4a-latm";
				case NexContentInformation.NEXOTI_AAC_PLUS:
				case NexContentInformation.NEXOTI_EC3:
					return "audio/eac3";
				case NexContentInformation.NEXOTI_MP3inMP4:
				case NexContentInformation.NEXOTI_MP2:
				case NexContentInformation.NEXOTI_MP3:
				case NexContentInformation.NEXOTI_BSAC:
					return "audio/mpeg";
				case NexContentInformation.NEXOTI_AC3:
					return "audio/ac3";
				case NexContentInformation.NEXOTI_AC4:
					return "audio/ac4";
				case NexContentInformation.NEXOTI_DTS:
					return "audio/vnd.dts";
				case NexContentInformation.NEXOTI_WMA:
					return "audio/x-ms-wma";
				case NexContentInformation.NEXOTI_RA:
				case NexContentInformation.NEXOTI_DRA:
					return "audio/x-pn-realaudio";
				default :
					return Integer.toString(codecType);
			}
		}
		else if( mediaType == 4 ){	// Text
			return "text";
		}
		else
			return Integer.toString(codecType);
	}
}

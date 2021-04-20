package com.nexstreaming.nexplayerengine;

import java.util.ArrayList;


/**
 * \brief This class contains information about the metadata associated with the  
 * content, from sources such as ID3 tags.
 * 
 * This includes a series of text fields (the set of fields that actually
 * contain information will vary depending on the format and which
 * fields the content creator has filled in).  These fields include
 * information such as the album name, artist, lyrics and so on.
 * 
 * This also includes the picture associated with the content, for formats 
 * such as MP3 and AAC that can have an optional associated still image, included as 
 * a NexID3TagPicture object.
 *  
 * This image is generally used in place of video for content that does not
 * have video.  The exact use of the still image is up to the content
 * producer. In the case of an MP3 or AAC audio file, it is usually
 * the album cover artwork.  In the case of HTTP Live Streaming,
 * audio-only tracks often have a still image to be shown in place of
 * the video.  This image may change during playback (for example,
 * some audio-only HLS streams provide a new image every ten seconds
 * or so).
 * 
 * When timed metadata is used with HLS content, the NexID3TagInformation object
 * associated with the content will be updated each time new metadata is available
 * during playback, and should be updated accordingly.
 * 
 */
public class NexID3TagInformation {

	private NexID3TagText mTitle;
	private NexID3TagText mAlbum;
	private NexID3TagText mArtist;
	private NexID3TagText mDate;
	private NexID3TagText mGenre;
	private NexID3TagText mSessionInfo;
	private NexID3TagText mTrackNum;
	private NexID3TagText mYear;
	private NexID3TagPicture mPicture;
	private NexID3TagText mLyric;
	private NexID3TagText mPrivateFrame;
	private NexID3TagText mComment;
	private NexID3TagText mText;
	private ArrayList<NexID3TagText> mArrExtraData;
	private int mTimecode;


	private NexID3TagInformation(	NexID3TagText title, NexID3TagText album, NexID3TagText artist,
									 NexID3TagText date, NexID3TagText genre, NexID3TagText sessionInfo, NexID3TagText trackNum,
									 NexID3TagText year, NexID3TagPicture picture, NexID3TagText lyric, NexID3TagText privateFrame,
									 NexID3TagText comment, NexID3TagText text, ArrayList<NexID3TagText> arrExtraData, int timeCode)
	{
		NexLog.d("ID3Tag", "ID3Tag Constructor");
		mTitle = title;
		mAlbum = album;
		mArtist = artist;
		mDate = date;
		mGenre = genre;
		mSessionInfo = sessionInfo;
		mTrackNum = trackNum;
		mYear = year;
		mPicture = picture;
		mLyric = lyric;
		mPrivateFrame = privateFrame;
		mComment = comment;
		mText = text;
		mArrExtraData = arrExtraData;
		mTimecode = timeCode;
	}

	/** 
	 * \brief This method gets the title of the current track.
	 */
	public NexID3TagText getTitle()
	{
		return mTitle;
	}
	/**
	 * \brief This method sets the title of the current track.
	 */
	public void setTitle(NexID3TagText title)
	{
		mTitle = title;
	}

	/**
	 * \brief This method returns the album name of the current content.
	 */
	public NexID3TagText getAlbum()
	{
		return mAlbum;
	}
	/**
	 * \brief This method sets the album name of the current content.
	 */
	public void setAlbum(NexID3TagText album)
	{
		mAlbum = album;
	}

		/**
	 * \brief This method returns the genre name of the current content.
	 */
	public NexID3TagText getGenre()
	{
		return mGenre;
	}
	/**
	 * \brief This method sets the genre name of the current content.
	 */
	public void setGenre(NexID3TagText genre)
	{
		mGenre = genre;
	}
	
	/**
	 * \brief This method returns the artist of the current content.
	 */
	public NexID3TagText getArtist()
	{
		return mArtist;
	}
	/**
	 * \brief This method sets the artist of the current content.
	 */
	public void setArtist(NexID3TagText artist)
	{
		mArtist = artist;
	}
	
	/**
	 * \brief This method gets date information about the current content.
	 */
	public NexID3TagText getDate()
	{
		return mDate;
	}
	/**
	 * \brief This method sets the date information of the current content.
	 */
	public void setDate(NexID3TagText date)
	{
		mDate = date;
	}
	
	/**
	 * \brief This method gets session information about the current content.
	 */
	public NexID3TagText getSessionInfo()
	{
		return mSessionInfo;
	}
	/**
	 * \brief  This method sets the session information about the current content.
	 */
	public void setSessionInfo(NexID3TagText sessionInfo)
	{
		mSessionInfo = sessionInfo;
	}
	
	/**
	 * \brief This method gets the track number of the current content.
	 */
	public NexID3TagText getTrackNumber()
	{
		return mTrackNum;
	}
    /** 
     * \brief This method sets the track number of the current content.
     */
	public void setTrackNumber(NexID3TagText trackNum)
	{
		mTrackNum = trackNum;
	}
	
	/**
	 * \brief This method gets the year information about the current content.
	 */
	public NexID3TagText getYear()
	{
		return mYear;
	}
    /**
     * \brief This method sets the year information about the current content.
     */
	public void setYear(NexID3TagText year)
	{
		mYear = year;
	}
	
	/**
	 * \brief This method gets the picture associated with the current content.
	 */
	public NexID3TagPicture getPicture()
	{
		return mPicture;
	}
	/**
	 * \brief This method sets the picture associated with the current content, if available.
	 * 
	 * This may be used as a still image to display in the event that video for particular
	 * content may not be displayed (due to network variability or perhaps an unsupported video
	 * codec is in use).
	 * 
	 * \param  picture	A NexID3TagPicture object containing the picture data and MIME type.
	 */
	public void setPicture(NexID3TagPicture picture)
	{
		mPicture = picture;
	}
    
	/**
     * \brief This method gets the lyrics associated with the current content, if available.
     */
	public NexID3TagText getLyric() 
	{
		return mLyric;
	}
    /**
     * \brief  This method sets the lyrics associated with the current content when available. 
     */
	public void setLyric(NexID3TagText lyric)
	{
		mLyric = lyric;
	}


    /**
     * \brief  This method gets the information included in private frames of the current
     * content's ID3 tags.
     * 
     * The private frames in ID3 tags are used to include additional information that is required
     * by a program but can't be included in the other ID3 tag frames, and are tagged:
     * 
     *\code
     <Header for 'Private frame', ID: "PRIV">
     	Owner identifier      <text string> $00
    	The private data      <binary data>
	 \endcode
	 *
	 * Where the 'Owner identifier' string includes contact information for the organization
	 * which is responsible for the frame and its provided binary data.
	 * 
	 * NexPlayer&trade;&nbsp;merely passes the private frame information to the client
	 * application where it can be handled as desired.
	 * 
	 * \since version 6.0
	 */
	public NexID3TagText getPrivateFrame()
	{
		return mPrivateFrame;
	}
	
	/**
	 * \brief  This method sets the information included in private frames of the current
	 * content's ID3 tags.
	 * 
	 * \see getPrivateFrame for more information.
	 * 
	 * \since version 6.0
	 */
	public void setPrivateFrame(NexID3TagText privateFrame)
	{
		mPrivateFrame = privateFrame;
	}
	/** 
	 * \brief  This method gets the data included in the comment frame of the current content's ID3 tags. 
	 *
	 * \return  The comment frame data as a NexID3TagText object.
	 *
	 * \since version 6.0.5
	 */
	public NexID3TagText getComment()
	{
		return mComment;
	}
	/**
	 * \brief This method sets the information from the comment frame
	 * in the current content's ID3 tags.
	 *
	 * \param comment	The data in the comment frame as a NexID3TagText object.
	 *
	 * \since version 6.0.5
	 */
	public void setComment(NexID3TagText comment)
	{
		mComment = comment;
	}
	
	/**
	 * \brief This method gets the data in the text frame included in the ID3 tags of the current content, if available.
	 *
	 * \returns The text frame data as a NexID3TagText object.
	 *
	 * \since version 6.0.5
	 */
	public NexID3TagText getText()
	{
		return mText;
	}
	/**
	 * \brief  This method sets the text frame data in this class.
	 * 
	 * \param text  The text frame data as a NexID3TagText object.
	 * 
	 * \since version 6.0.5
	 */	
	public void setText(NexID3TagText text)
	{
		mText = text;
	}

	/**
	 * 
	 * \brief  This method gets a list of customized ID3 tags and the extra data they contain included in content timed metadata. 
	 * 
	 * For the list of customized ID3 tags to be recognized and handled by NexPlayer&trade;, 
	 * they should be set using the NexProperty \c TIMED_ID3_META_KEY after NexPlayer&trade;&nbsp;is initialized but before \c NexPlayer.open is called. 
	 *  
	 * \returns The customized ID3 tags and the additional data they contain as an ArrayList of NexID3TagText objects.
	 * 
	 * \see  setArrExtraData  
	 * \see  \link NexPlayer.NexProperty#TIMED_ID3_META_KEY TIMED_ID3_META_KEY\endlink
 	 * 
 	 * \since version 6.9
	 */	

	public ArrayList<NexID3TagText> getArrExtraData()
	{
		return mArrExtraData;
	}
	
	/**
	 * \brief  This method sets the list of customized ID3 tags and the extra data they contain included in content timed metadata.
	 * 
	 * For the list of customized ID3 tags to be recognized and handled by NexPlayer&trade;, they should be set
	 * using the NexProperty \c TIMED_ID3_META_KEY after NexPlayer&trade;&nbsp;is initialized but before \c NexPlayer.open is called.
	 * 
	 * \param ExtraData     The list of customized ID3 tags and the extra data they contain, as an ArrayList of NexID3TagText objects.
	 * 
	 * \see getArrExtraData 
	 * \see \link NexPlayer.NexProperty#TIMED_ID3_META_KEY TIMED_ID3_META_KEY\endlink
	 * 
	 * \since version 6.9
	 */	
	
	public void setArrExtraData(ArrayList<NexID3TagText> ExtraData)
	{
		mArrExtraData = ExtraData;
	}

	/** 
	 * \brief  This method gets the timestamp of timed metadata.
	 *
	 * \return  The timestamp of timed metadata in millisecond unit.
	 *
	 * \since version 6.63
	 */


	public int getTimeCode() { return mTimecode; }

	/**
	 * 
	 * @param size		
	 * @param mimeType	
	 * @param data		
	 */

	/**
	 * The sole constructor.
	 * 
	 * @param title			Initial value for mTitle
	 * @param album			Initial value for mAlbum
	 * @param artist		Initial value for mArtist
	 * @param date			Initial value for mDate
	 * @param genre			Initial value for mGenre
	 * @param sessioninfo	Initial value for mSessionInfo
	 * @param trackNum		Initial value for mTrackNum
	 * @param year			Initial value for mYear
	 * @param size			Initial value for mSize (should match data.length)
	 * @param mimeType		Initial value for mMimeType
	 * @param data			Initial value for mByteData
	 */
	/*
	public NexID3TagInformation( 	String title, String album, String artist,
								String date, String genre, String sessioninfo, 
								String trackNum, String year,
								int size, String mimeType, byte[] data)
	{
		mTitle = title;
		mAlbum = album;
		mArtist = artist;
		mDate = date;
		mGenre = genre;
		mSessionInfo = sessioninfo;
		mTrackNum = trackNum;
		mYear = year;

		mImageSize = size;
		mImageMimeType = mimeType;
		mImageByteData = data;
	}
	*/

	
	/** The image data, encoded according to the MIME type in mMimeType */
	//public byte[] 	mImageByteData;
	
	/** The size of the image data, in bytes.  This is the same as mByteData.length */
	//public int 		mImageSize;
	
	/** The MIME type of the data in mByteData.  This can be used to determine how to decode the image data. */
	//public String 	mImageMimeType;
	
	/** The 'Title' ID3 tag or equivalent. Often a song title. */
	//public String 	mTitle;
	/** The 'Album' ID3 tag or equivalent. Usually the name of the album containing the song. */
	//public String	mAlbum;
	/** The 'Artist' ID3 tag or equivalent. Usually the name of the performer. */
	//public String 	mArtist;
	/** The 'Date' ID3 tag or equivalent. Usually the date when the content was produced. This is a string and the internal format may vary depending on the content type and content producer. */	
	//public String 	mDate;
	/** The 'Genre' ID3 tag or equivalent. For formats that store genre as a number, the number is converted into the appropriate string and the string is given here. */
	//public String 	mGenre;
	/** The 'Session Info' ID3 tag or equivalent. */
	//public String	mSessionInfo;
	/** The 'Track Number' ID3 tag or equivalent. For content that is also available on a CD, this is usually the matching track number from the CD. */
	//public String	mTrackNum;
	/** The 'Year' ID3 tag or equivalent. This is usually the year when the album was produced or the content was produced, where applicable. */
	//public String	mYear;
}

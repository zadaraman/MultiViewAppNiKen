package com.nexstreaming.nexplayerengine;

/**
 * \brief  This class allows NexPlayer&trade;&nbsp;to handle picture information included in timed metadata ID3 tags.
 * 
 * NexPlayer&trade;&nbsp;passes an instance of this class whenever new picture information for the current content
 * is received from its ID3 tags, and is included in an updated NexID3TagInformation object.
 */

public class NexID3TagPicture {

	private byte[] mPictureData;
	private NexID3TagText mMimeType;
	
	private NexID3TagPicture(byte[] pictureData, NexID3TagText mimeType)
	{
		mPictureData = pictureData;
		mMimeType = mimeType;		
	}
	/**
	 * \brief  This method gets the picture data associated with current content from the 
	 * content's timed metadata ID3 tags.
	 * 
	 * \returns  A byte array of the picture data.
	 */
	public byte[] getPictureData()
	{
		return mPictureData;
	}
	/**
	 * \brief  This method gets the MIME type of the picture associated with the current content (from
	 * the content's timed metadata ID3 tags).
	 */
	public NexID3TagText getMimeType()
	{
		return mMimeType;
	}
}

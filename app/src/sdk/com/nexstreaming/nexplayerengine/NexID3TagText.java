package com.nexstreaming.nexplayerengine;


/**
 * \brief  This class determines the encoding of text data included in content ID3 tags.
 */
public class NexID3TagText {

/** A possible return value for NexID3TagText.getEncodingType.  */
	public static final int ENCODING_TYPE_UNKNOWN		= 0x00000000;
/** A possible return value for NexID3TagText.getEncodingType.  */
	public static final int ENCODING_TYPE_UNICODE		= 0x10000000;
/** A possible return value for NexID3TagText.getEncodingType.  */
	public static final int ENCODING_TYPE_UTF8			= 0x10000010;
/** A possible return value for NexID3TagText.getEncodingType.  */
	public static final int ENCODING_TYPE_UTF16		= 0x10000020;
/** A possible return value for NexID3TagText.getEncodingType.  */
	public static final int ENCODING_TYPE_UTF16_BE		= 0x10000030;
/** A possible return value for NexID3TagText.getEncodingType.  */
	public static final int ENCODING_TYPE_UTF32		= 0x10000040;
/** A possible return value for NexID3TagText.getEncodingType.  */
	public static final int ENCODING_TYPE_UTF32_BE		= 0x10000050;
/** A possible return value for NexID3TagText.getEncodingType.  */
	public static final int ENCODING_TYPE_ASCII		= 0x20000000;
/** A possible return value for NexID3TagText.getEncodingType.  */
	public static final int ENCODING_TYPE_ISO_8859		= 0x30000000;
/** A possible return value for NexID3TagText.getEncodingType.  */
	public static final int ENCODING_TYPE_ISO_8859_1	= 0x30000010;
	
	private int			mEncodingType	= ENCODING_TYPE_ISO_8859_1;
	private byte[]		mTextData		= null;
	private byte[]		mExtraDataID	= null;
	
	private NexID3TagText(int encodingType, byte[] text)
	{
		if(text == null)
		{
			NexLog.d("ID3TagText", "ID3TagText text is null!!");
		}

		switch(encodingType)
		{
		case ENCODING_TYPE_ISO_8859_1:
		case ENCODING_TYPE_ISO_8859:
		case ENCODING_TYPE_UTF16:
		case ENCODING_TYPE_UTF16_BE:
		case ENCODING_TYPE_UTF32:
		case ENCODING_TYPE_UTF32_BE:
		case ENCODING_TYPE_UTF8:
		case ENCODING_TYPE_ASCII:
		case ENCODING_TYPE_UNICODE:
			mEncodingType = encodingType;
			break;
		default:
			mEncodingType = ENCODING_TYPE_UNKNOWN;
		}
		mTextData = text;
	}
	
	private NexID3TagText(int encodingType, byte[] ExtraDataID, byte[] text)
	{
		if (null == ExtraDataID) {
			NexLog.d("ID3TagText", "ID3TagText ExtraDataID is null!!");
		} else {
			String s = new String(ExtraDataID);
			NexLog.d("ID3TagText", "ID3TagText ExtraDataID is " + s);
		}
	
		if(null == text)
		{
			NexLog.d("ID3TagText", "ID3TagText text is null!!");
		}

		switch(encodingType)
		{
		case ENCODING_TYPE_ISO_8859_1:
		case ENCODING_TYPE_ISO_8859:
		case ENCODING_TYPE_UTF16:
		case ENCODING_TYPE_UTF16_BE:
		case ENCODING_TYPE_UTF32:
		case ENCODING_TYPE_UTF32_BE:
		case ENCODING_TYPE_UTF8:
		case ENCODING_TYPE_ASCII:
		case ENCODING_TYPE_UNICODE:
			mEncodingType = encodingType;
			break;
		default:
			mEncodingType = ENCODING_TYPE_UNKNOWN;
		}
		mExtraDataID = ExtraDataID;
		mTextData = text;
	}
	
/**  This method gets the encoding type of text included in content ID3 tags.
* \returns  The encoding type of the text.
 */
	public int getEncodingType()
	{
		return mEncodingType;
	}
	public String getEncodingTypeName() {
		String name = "UTF-8";
		switch(mEncodingType) {
			case ENCODING_TYPE_ISO_8859_1:
				name = "ISO-8859-1";
				break;
			case ENCODING_TYPE_UTF16:
				name = "UTF-16";
				break;
			case ENCODING_TYPE_UTF16_BE:
				name = "UTF-16BE";
				break;
			case ENCODING_TYPE_UTF8:
				name = "UTF-8";
				break;
			case ENCODING_TYPE_ASCII:
				name = "US-ASCII";
				break;
			case ENCODING_TYPE_UNICODE:
				name = "UNICODE";
				break;
		}

		return name;
	}
	/**
*  This method gets the text data included in content ID3 tags.
*  \returns The text data as an array.
*/
	public byte[] getTextData()
	{
		return mTextData;
	}

/**
 * \brief  This method gets the customized ID3 tags of the extra data added to content timed metadata so they can be passed to the app.
 * 
 * \warning This method can only be used if the customized ID3 tag names have already been set using the NexProperty \c TIMED_ID3_META_KEY.  
 * 
 * \returns The customized ID3 tags as a byte array, or \c null if \c TIMED_ID3_META_KEY has not been set.
 *
 * \see \link NexPlayer.NexProperty#TIMED_ID3_META_KEY TIMED_ID3_META_KEY\endlink
 *
 * \since version 6.9
 */

	public byte[] getExtraDataID()
	{
		return mExtraDataID;
	}
}

package com.nexstreaming.nexplayerengine;

/**
 * Created by splitter03 on 08/02/2017.
 */

public class NexSessionData {
	/**
	 * This is a unique string used to identify the session data.
	 */
	public String mDataID;

	/**
	 * It is identified by a string, DATA-ID. If LANGUAGE is specified, this attribute must be included in a human-readable form in the specified language.
	 */
	public String mValue;

	/**
	 * A string containing a URI, the resource identified by this URI must be formatted with JSON (JavaScript Standard Object Notation, one of the notations, RFC 7159).
	 */
	public String mUri;

	/**
	 * Language of VALUE, using RFC5646 value
	 */
	public String mLanguage;

	/**
	 * Absolute request URI
	 */
	public String mAbstractUrl;

	/**
	 * Arbitrary data from URI
	 */
	public byte[] mDataFromUrl;

	private int mId;

	NexSessionData(int id, String dataID, String value, String uri, String language, String absUrl, byte[] data)
	{
		mId = id;
		mDataID = dataID;
		mValue = value;
		mUri = uri;
		mLanguage = language;
		mAbstractUrl = absUrl;
		mDataFromUrl = data;
	}
}

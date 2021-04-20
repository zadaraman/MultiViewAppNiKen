
package com.nexstreaming.nexplayerengine;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;

/**
 * \brief		The primary interface to the NexThumbnail class.
 *
 * This class allows NexPlayer&trade;&nbsp;to handle thumbnail information in content.
 *
 * Follow the steps below in order to use the NexThumbnail class:
 *
 * <ol>
 *   <li> Init()		: Initializes the thumbnail instance.</li>
 *   <li> getInfo()		: Gets the content information(original width, hight, pitch, rotation info).</li>
 *   <li> getData()	    : Gets the thumbnail output data, where choice of output image type(RGB565, RGB888...) should be made.</li>
 *   <li> Deinit()		: Releases the thumbnail instance.</li>
 * </ol>
 *
 * @author NexStreaming Corporation
 * @version 1.0
 *
 */

/** \deprecated  For internal use only.  Please do not use. */
public final class NexThumbnail
{
	private final static String TAG = "NexThumbnail";
	public int	mNativeNexThumbnailClient = 0;

	static {
		System.loadLibrary("nexadaptation_layer_for_dlsdk");
		NexLog.d(TAG, "Loading nexadaptation_layer_for_dlsdk.");
		
		System.loadLibrary("nexplayerengine");
		NexLog.d(TAG, "Loading nexplayerengine.");
		
		System.loadLibrary("nexthumbnail");
		NexLog.d(TAG, "Loading nexthumbnail.");
	}

	/* Bitmap.java(\frameworks\base\graphics\java\android\graphics)
	*	public enum Config {
	*		ALPHA_8     	(2),
	*		RGB_565     	(4),
	*		ARGB_4444   	(5),
	*		ARGB_8888   	(6);
	*
	*	private static Config sConfigs[] = {
       *     		null, null, ALPHA_8, null, RGB_565, ARGB_4444, ARGB_8888
       *	};
	*/
	public static final int OUTPUT_TYPE_RGB565 = 4;
	public static final int OUTPUT_TYPE_RGB888 = 6;

	private int		m_NativeContext;	// accessed by native methods
	private long	m_thumbnailTStamp;
	private String	m_path = null;
	ThumbnailInformation mThumbInfo = null;

	public NexThumbnail(String path) 
	{
		m_path = path;
		native_init();
	}

	static public class ThumbnailInformation
	{
		private int mWidth;
		private int mHeight;
		private int mPitch;
		private int mRotate;

		public ThumbnailInformation()
		{
			mWidth	= 0;
			mHeight = 0;
			mPitch	= 0;
			mRotate = 0;
		}

		public int getThumbnailInfoWidth()
		{
			return mWidth;
		}

		public int getThumbnailInfoHeight()
		{
			return mHeight;
		}

		public int getThumbnailInfoPitch()
		{
			return mPitch;
		}

		public int getThumbnailInfoRatate()
		{
			return mRotate;
		}
	};

	private static native final void native_init();
	private native final void native_setup();
	
	private native int Init(String path);		/** parser create -> open 	*/
	private native int Deinit();				/** parser close -> destroy */
	private native int GetInfo(Object info);	/** get content information 	*/
	private native byte[] GetData(int in_width, int in_height, int in_eType, long in_timestamp);

	/**
	 * \brief Call this function after GetData()
	 *
	 *
	 * \returns Thumbnail timestamp value
	 *
	 */
	public long getThumbnailTStamp()
	{
		return m_thumbnailTStamp;
	}
	
    /**
     * \brief  This method returns the nearest I-Frame timestamp in front of target position. 
     *
     * It can get the nearest timestamp of I-Frame in front of target posion to use highlight function.
     *
     * @param targetTS	A pointer to the timestamp of target
     *
     * \returns Zero or positive number if successful, negative number if there was an error.
     *
     */    
    public native int GetIFramePos(int targetTS);

	/**
	 * \brief This method returns the I-Frame count.
	 *
	 * @param targetTS A point to the timestamp of target.
	 *
	 * \returns Zero or I-Frame count if successful, negative number if there was an error.
	 *
	 */
	public native int GetIFrameCount(int startTS);

	public int GetIFrameCnt(int startTS)
	{
		return GetIFrameCount(startTS);
	}

	/**
	 * \brief This method get the I-Frame timestamp information.
	 *
	 * int[] info is I-Frame timestamp array.
	 *
	 * @param startTS A point to the start timestamp.
	 *
	 * \returns Zero or negative number.
	 *
	 */
	public native int GetIFrameInfo(int startTS, int[] info);

	private static int[] arrFrameInfo = new int[20];

	public int[] GetIFrameInformation(int startTS)
	{
		int nRet = 0;
		nRet = GetIFrameInfo(startTS, arrFrameInfo);
		NexLog.w(TAG, "GetIFramePosition. return:"+nRet);
		if (nRet != 0)
		{
			return null;
		}

		return arrFrameInfo;
	}

	/**
	 * \brief  Call this function after \c getFrameAtTime.
	 *
	 * On failure, a \c RuntimeException is thrown.
	 *
	 */
	public ThumbnailInformation getThumbnailInformation()
	{
		ThumbnailInformation thumbnailInfo = new ThumbnailInformation();
		int retcode = GetInfo(thumbnailInfo);
		if(retcode != 0) {
			throw new RuntimeException("[NexThumbnail/getThumbnailInformation] failure code: " + retcode);
		}
		return thumbnailInfo;
	}

	/**
	* A Bitmap containing a representative video frame, which can be null, 
	* if such a frame cannot be retrieved.
	*
	* @param width
	* @param height
	* @param etype	OUTPUT_TYPE_RGB565 or OUTPUT_TYPE_RGB888
	* @param timestamp
	*/
	public Bitmap GetThumbData(int width, int height, int etype, long timestamp)
	{
		byte[] buffer = GetData(width, height, etype, timestamp);
		if (buffer.length == 0)
			return null;

		Bitmap retBitmap = null;
		if (etype == OUTPUT_TYPE_RGB888)
		{
			retBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		}
		else
		{
			retBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		}
		ByteBuffer bytebuffer = ByteBuffer.allocate(buffer.length);
		bytebuffer.put(buffer, 0, buffer.length);
		bytebuffer.rewind();
		retBitmap.copyPixelsFromBuffer(bytebuffer);
		return retBitmap;
	}

	/**
	* On failure, a RuntimeException is thrown.
	*
	*/
	public void open()
	{
		native_setup();
		int retcode = Init(m_path);
		if (retcode != 0) {
			throw new RuntimeException("[NexThumbnail/open] failure code: " + retcode);
		}
	}

	/**
	* On failure, a RuntimeException is thrown.
	*
	*/
	public void close()
	{
		int retcode = Deinit();
		if (retcode != 0) {
			throw new RuntimeException("[NexThumbnail/close] failure code: " + retcode);
		}
	}
};


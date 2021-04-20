package com.nexstreaming.nexplayerengine;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.UUID;

/**
 * \brief  The primary interface to the NexALFactory.  
 * 
 * NexALFactory handles codecs and renderer selection for the NexPlayer&trade;&nbsp;SDK starting from NexPlayer&trade;&nbsp;SDK version 6.0. 
 * To use the NexPlayer&trade;&nbsp;SDK, the application must create an instance of the NexALFactory class (supplied as part of the NexPlayer&trade;&nbsp;SDK).
 * 
 * In addition, an application must also do the following: 
 * 
 *  -# Create an instance of \link NexPlayer NexPlayer\endlink &trade;&nbsp;and an instance of \link NexALFactory NexALFactory\endlink
 *     and set the codec usage policy. 
 *     \code
 *      mNexPlayer = new NexPlayer();
 *      mNexALFactory = new NexALFactory();
 *     \endcode
 *  -# Call the \link NexALFactory.init\endlink
 *      method, passing the model name of the current device.
 *     \code
 *      mNexALFactory.init(this, android.os.Build.MODEL, android.os.Build.MODEL, nLogLevel, colorDepth);
 *      mNexPlayer.setNexALFactory(mNexALFactory);
 *      mNexPlayer.init(this, 1);
 *     \endcode
 *  -# After playback by NexPlayer&trade;, \link NexALFactory.release\endlink should be called when instances of NexPlayer&trade;&nbsp;
 *     and NexALFactory are no longer needed.
 *     \code
 *      mNexPlayer.release();
 *      mNexALFactory.release();     
 *     \endcode
 * 
 * 
 * For details on usage, see the 
 * NexPlayer&trade;&nbsp;Engine \ref legal "package" documentation.
 * 
 * @author NexStreaming Corp.
 */
public final class NexALFactory {

    private final static String TAG = "NEXALFACTORY_JAVA";
    
	private static final int NEXCODECDOWNLOADER_EVENT_DOWNLOAD_BEGIN = 0x00990001;
	private static final int NEXCODECDOWNLOADER_EVENT_DOWNLOAD_PROGRESS = 0x00990002;
	private static final int NEXCODECDOWNLOADER_EVENT_DOWNLOAD_COMPLETE = 0x00990003;
	private static final int NEXCODECDOWNLOADER_EVENT_DOWNLOAD_ERROR = 0x00990004;

    protected static final int NEX_EXTERNAL_VIEW_SURFACETEXTURE = 1;
    /* 
     * native NexALFactory context. This is accessed by native methods. 
     */
    private long mALFactoryContext = 0;
    private String mPackageName;
    private String mDeviceUUID;
    private static ICodecDownListener    mCodecDownloadListener;
    private Context mContext;
    
    static
    {
        /*
         * Load the library. If it's already loaded, this does nothing.
         */
        if( !NexPlayer.isStaticSDK ) {
          //For shared library SDK. 
          System.loadLibrary("nexadaptation_layer_for_dlsdk");
          NexLog.d(TAG,"Loading nexadaptation_layer_for_dlsdk.");
  
          System.loadLibrary("nexalfactory");
          NexLog.d(TAG,"Loading nexalfactory.");
        }
    }       
        
    /** 
     * \brief  Checks whether the current device can use native decoders or not.
     * 
     * \param strDeviceModel Device model name. 
     *                       Under normal (production) use, you should pass the MODEL
         *                       as available via the Android API in \c android.os.Build.MODEL.
     * \param sdkInfo
     *            The Android SDK version number. Refer to NexSystemInfo.getPlatformInfo()
     *              - 0x15 : SDK version 1.5 CUPCAKE
     *              - 0x16 : SDK version 1.6 DONUT
     *              - 0x21 : SDK version 2.1 ECLAIR
     *              - 0x22 : SDK version 2.2 FROYO
     *              - 0x30 : SDK version 2.3 GINGERBREAD
     *              - 0x31 : SDK version 3.0 HONEYCOMB
     *              - 0x40 : SDK version 4.0 ICECREAM SANDWICH
     *              - 0x41 : SDK version 4.1 JELLYBEAN
     * \return  
     *            An integer greater than 0 if native decoder can be used; and 0 or 
     *            less than 0 if the native decoder cannot be used.
     *              - 0 : A device where the native decoder cannot be used.
     *              - 1 : Devices that are expected to support hardware codecs.
     *              - 2 : specific devices that are verified to support hardware codecs.     
     */
    public static native int canUseNativeDecoder(String strDeviceModel,int sdkInfo);

        
    /** 
     * Sole constructor for NexALFactory;.
     * 
     * After constructing a NexALFactory &nbsp;object, you <i>must</i> call
     * NexALFactory.init before you can call any other methods.
     */
    public NexALFactory() {

        
    }
    /** 
     * \brief Returns the NexALFactoryContext.
     * 
     * This method is called by the NexPlayer&trade;&nbsp;Engine.
     * This is just for native methods.  Don't use this method for other purposes.
     */
    public long getNexALFactoryContext() {
        return mALFactoryContext;
    }
    
    /**
     * \brief  Initializes NexALFactory.
     * 
     * NexPlayer&trade;&nbsp;automatically detects the devices where HW codecs and H.264 Main/High profiles can be supported
     * so there is no need to indicate any specific device model to the player.
     * 
     * \warning  Although it is possible to change the device model parameter, \c strModel, it is not recommended because
     *           changing the model name may result in the H/W decoder not working properly.  Please do NOT change
     *           the device model name in the sample code. Similarly, although it is possible to change the render mode parameter, \c strRenderMode, NexPlayer&trade;&nbsp;
     *           is only guaranteed to work properly with \c strRenderMode set to \c NEX_DEVICE_USE_AUTO.  Please do NOT change the render mode
     *           in the sample code.
     * 
     * @param context       The current context; from \c Activity subclasses, you can
     *                      just pass <code>this</code>.
     * @param strModel      Device model name.  NexPlayer&trade;&nbsp; includes multiple renderer
     *                      modules, and past versions of the player selected the module most suitable
     *                      to the device based on this value. The renderer is now set by the parameter
     *                      \c strRenderMode.
     *                      Under normal use, you should pass the MODEL
     *                      as available via the Android API in \c android.os.Build.MODEL.  
     *                      For example:
     * \code
     * nexALFactory.init(this, android.os.Build.MODEL, NEX_DEVICE_USE_AUTO, 0, 1);
     * \endcode
     *                      NexPlayer&trade;&nbsp;uses this to select the most appropriate renderer if no renderer is selected
     *                      (\c NULL is passed) with the parameter \c strRenderMode below.  For OS versions
     *                      up to Gingerbread, this is always the Android Renderer (although from Froyo, other renderers are
     *                      supported as well) if the SW codec is in use. For Honeycomb and Ice Cream Sandwich (ICS), this is always the OpenGL renderer when
     *                      the SW codec is in use.
     * \param strRenderMode The Renderer to use, as a string. The recommended render mode to use is \c NEX_DEVICE_USE_AUTO, which will choose
     *                      the most appropriate render mode automatically.  For devices that support the hardware codec, this will be:
     *                        - <b>{@link NexPlayer#NEX_USE_RENDER_IOMX NEX_USE_RENDER_IOMX}.  </b>
     *                       For devices that don't support the hardware codec, this will be one of the software renderers.  
     *                       While most devices should work properly with OpenGL,
     *                       occasionally another rendering module may be beneficial, for example if a device supports 3D rendering,
     *                       if an application doesn't implement support for the OpenGL renderer, or
     *                       for devices running older versions of the Android OS.  In some other cases, such as the Kindle Fire running on 
     *                       Gingerbread, while the default renderer is Android, the OpenGL renderer is recommended because
     *                       of improved performance. The available software renderers are:
     *                          - <b>{@link NexPlayer#NEX_DEVICE_USE_AUTO NEX_DEVICE_USE_AUTO} ("Auto")</b>  Added in NexPlayer SDK version 6.1.2,
     *                          this chooses which renderer to use automatically. 
     *                          - <b>{@link NexPlayer#NEX_DEVICE_USE_ONLY_ANDROID NEX_DEVICE_USE_ONLY_ANDROID}
     *                          ("Android")</b> Use only standard Android API bitmaps to display frames.  This
     *                          is usually slower, but is more portable.
     *                          - <b>{@link NexPlayer#NEX_DEVICE_USE_JAVA NEX_DEVICE_USE_JAVA}
     *                          ("JAVA")</b> Use the Java renderer.
     *                          - <b>{@link NexPlayer#NEX_DEVICE_USE_OPENGL NEX_DEVICE_USE_OPENGL}
     *                          ("OPENGL")</b> Use the OpenGL renderer.
     *                          - <b>{@link NexPlayer#NEX_DEVICE_USE_ANDROID_3D NEX_DEVICE_USE_ANDROID_3D}
     *                          ("Android 3D")</b>Use the 3D video renderer with standard Android API bitmaps.
     * @param logLevel      NexPlayer&trade;&nbsp;SDK logging level.  This affects the messages that the SDK writes to the
     *                      Android log.
     *                          - <b>-1</b> : Do not output any log messages. (If you do not want to output any log messages in NexPlayerSample App, you should set the loglevel to 0xF0000000.)
     *                          - <b>0</b> : Output basic log messages only (recommended).
     *                          - <b>1~4</b> : Output detailed log messages; higher numbers result in more verbose
     *                                      log entries, but may cause performance issues in some cases and are
     *                                      not recommended for general release code.
     * @param colorDepth    Video output image color depth.
     *                          - <b>1</b> : RGBA_8888
     *                          - <b>4</b> : RGB_565
     * 
     * @return              \c TRUE if initialization succeeded; \c FALSE in the case of a 
     *                      failure (in the case of failure, check the log for details).
     *                      
     * \see NexPlayer.getRenderMode for more details.
     */
    public boolean init( Context context, String strModel, String strRenderMode, int logLevel, int colorDepth) {
        
        int iCPUInfo = 0;
        int iPlatform = 0;
        int iStartIndex = 0;
        int iPackageNameLength = 0;
        String strDeviceModel = "";
        String strDeviceRenderMode = "";
        boolean bNexALFactoryInit = true;
        
        mContext = context.getApplicationContext();
        mPackageName = mContext.getPackageName();

        int [] extraEncodingList = NexSystemInfo.getExtraEncodinglist(mContext);
        File fileDir = context.getFilesDir();
        if( fileDir == null)
            throw new IllegalStateException("No files directory - cannot play video - relates to Android issue: 8886!");
        String strPath = fileDir.getAbsolutePath();
        
        String strLibPath = "";
        
        iPackageNameLength = mPackageName.length();
        iStartIndex = strPath.indexOf(mPackageName);
        
        iCPUInfo = NexSystemInfo.getCPUInfo();
        iPlatform = NexSystemInfo.getPlatformInfo();
        
        if(strModel == null)
        {
            strDeviceModel = NexSystemInfo.getDeviceInfo();
        }
        else
        {
            strDeviceModel = strModel;
        }
        
        if(strRenderMode == null)
        {
            strDeviceRenderMode = NexSystemInfo.getDeviceInfo();
        }
        else
        {
            strDeviceRenderMode = strRenderMode;
        }
        
        if( iPlatform == NexSystemInfo.NEX_SUPPORT_PLATFORM_CUPCAKE )
            iCPUInfo = NexSystemInfo.NEX_SUPPORT_CPU_ARMV5;
        
        strLibPath = strPath.substring(0, iStartIndex + iPackageNameLength) + "/";  
        final ApplicationInfo applicationInfo = context.getApplicationInfo();
        String strNativePath = strLibPath;
        try
        {
            if(Build.VERSION.SDK_INT >= 9)
            {
                Field f = ApplicationInfo.class.getField("nativeLibraryDir");
                strNativePath = (String) f.get(applicationInfo) + "/";
            }
        }
        catch (Exception e)
        {
            NexLog.d(TAG, "exception from application info");
        }
        
        if(logLevel >= 0)
        {
            NexLog.Debug = true;
            NexLog.d(TAG, "PackageName : " + mPackageName);
            NexLog.d(TAG, "Files Dir : " + strPath);
            NexLog.d(TAG, "LibPath :" + strLibPath);
            NexLog.d(TAG, "NativeLibPath :" + strNativePath);
            NexLog.d(TAG, "CPUINFO :" + iCPUInfo + " SDKINFO : 0x" + Integer.toHexString(iPlatform));
            NexLog.d(TAG, "Model : " + strDeviceModel);
            NexLog.d(TAG, "RenderMode : " + strDeviceRenderMode);
            NexLog.d(TAG, "Log Level : " + logLevel);
        }
        else
        {
        	NexLog.Debug = false;
        }
        
        if(iPlatform >= NexSystemInfo.NEX_SUPPORT_PLATFORM_GINGERBREAD)
            mDeviceUUID = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        else
            mDeviceUUID = UUID.randomUUID().toString();
        
        int initRet = initialize_native(new WeakReference<NexALFactory>(this), strLibPath,strNativePath,strDeviceModel, strDeviceRenderMode, iPlatform, iCPUInfo, logLevel, colorDepth, extraEncodingList);
        if(initRet == 0)
            bNexALFactoryInit = true;
        else
            bNexALFactoryInit = false;
        
        return bNexALFactoryInit;
    }

    protected native void setExternalSurfaceMode(int mode);
    /** 
     * \brief This method sets the unique App code provided.
     * This must be called <b>after</b> \link NexALFactory.init\endlink has been called.  
     * 
     * \param strAppUCode   The provided unique App code.
     * 
     * \returns Zero if successful, or a non-zero error code.
     * \since version 6.0.6
     */     
    public int setAppUniqueCode(String strAppUCode)
    {
        String strDeviceUUID = mDeviceUUID;
        String strPackageName = mPackageName;
        
        set_appcode_native(strPackageName,strAppUCode, strDeviceUUID);
        return 0;
    }

/** 
     * \brief This method rescans the codec files in the application files directory(\c /data/data/&lt;package_name&gt;/files) to update the codecs available in NexPlayer&trade;.
     * 
     * \warning This must be called <b>after</b> \link NexALFactory.init\endlink has been called.  
     * 
     * Any time an application downloads an additional codec by calling NexALFactory.downloadCodec, this method
     * should be called to inform NexPlayer&trade;&nbsp;of the newly available codecs. 
     * 
     * \returns Zero if successful, or a non-zero error code.
     *
     * \since version 6.16
     */ 	
	public native int rescanCodecs();	
	
    /** 
     * \brief This method retrieves the codec list that can be used.
     * This must be called <b>after</b> \link NexALFactory.init\endlink has been called. 
     * 
     *  \return An array of the available codecs as NexCodecInformation objects.
     *  
     *  \see NexCodecInformation
     */	 	
	public NexCodecInformation[] getAvailableCodecs()
	{
		NexCodecInformation[] codecList = null;
		codecList = (NexCodecInformation[])(getAvailableCodecs_native());
		return codecList;
	}	

    /** 
     * \brief This method retrieves a list of the codecs that can be downloaded.
     * 
     * This must be called <b>after</b> \link NexALFactory.init\endlink has been called.  
     * 
     * \return An array of the codecs available to be downloaded from the server.
     *
     * \since version 6.16
     */	 	
	public NexCodecInformation[] getDownloadableCodecs()
	{
		NexCodecInformation[] codecList = null;
		codecList = (NexCodecInformation[])(getDownloadableCodecs_native());
		return codecList;
	}	
	
    /** 
     * \brief This method downloads a codec.
     * 
     * \warning This must be called <b>after</b> \link NexALFactory.getDownloadableCodecs\endlink has been called.  
     * 
     * After a codec is downloaded by calling this method, the application should also call 
     * NexALFactory.rescanCodecs to inform NexPlayer&trade;&nbsp;of the newly available codecs.
     * 
     * \param codecInfo  A NexCodecInformation object of the codec to be downloaded.
     * 
     * \return Zero if successful or a non-zero error code.
     * 
     * \see rescanCodecs
     * \since version 6.16
     */	 	
	public int downloadCodec(NexCodecInformation codecInfo)
	{
		return downloadCodec_native(codecInfo);
	}		
	
    /** 
     * \brief This method cancels a codec download. 
     *  
     * \return Zero if successful or a non-zero error code.
     * \since version 6.16
     */		
	public native int cancelDownloadCodec();

    /**
     * \brief This method releases resources used by the NexALFactory instance.
     * 
     * This should be called when the instance is no longer needed.  After
     * calling this method, the instance can no longer be used.
     */  
    public void release() {
    	synchronized(this) {
    		if ( mALFactoryContext != 0 ) {
    			/* mALFactoryContext becomes 0 once release_native() completes */
    			release_native();
    		} else {
    			NexLog.w(TAG, "release() not valid for uninitialized object");
    		}
    	}
    }
    
    /**
     * Releases native resources
     */
    @Override
    protected void finalize()
    {
        release();
    }   
    
    
    /** NexALFactory 
     * 
     * @param alfactory_this
     *            The NexALFactory instance pointer.
     * @param strLibPath
     *            The library path for NexALFactory;. 
     * @param strNativeLibPath
     *            The native library path for NexALFactory;. 
     * @param strDeviceModel
     *            The device model name.
     * \param strDeviceRenderMode
     *            The rendering mode.  This will be one of:
     *                          - NEX_DEVICE_USE_AUTO
     *                          - NEX_DEVICE_USE_ONLY_ANDROID
     *                          - NEX_DEVICE_USE_JAVA
     *                          - NEX_DEVICE_USE_OPENGL
     *                          - NEX_DEVICE_USE_ANDROID_3D
     * @param sdkInfo
     *            The Android SDK version.  
     *              - 0x15 : SDK version 1.5 CUPCAKE
     *              - 0x16 : SDK version 1.6 DONUT
     *              - 0x21 : SDK version 2.1 ECLAIR
     *              - 0x22 : SDK version 2.2 FROYO
     *              - ...
     * @param cpuInfo
     *            The cpuVersion.
     *               - 4 : armv4
     *               - 5 : armv5
     *               - 6 : armv6
     *               - 7 : armv7        
     * @param logLevel
     *            NexALFactory&nbsp;SDK log display level.
     * @param pixelFormat
     *            The pixel format to use when using the Java renderer. 
     *            For more information, see the <i>Java Renderer</i> section of
     *            \link com.nexstreaming.nexplayerengine nexplayerengine\endlink.
     *              - <b>1:</b> RGBA 8888
     *              - <b>4:</b> RGB 565 
     */
    private native int initialize_native(Object alfactory_this,String strLibPath,String strNativeLibPath, String strDeviceModel, String strDeviceRenderMode,
                                            int sdkInfo, int cpuInfo, int logLevel, int pixelFormat, int[] extraEncldingList);
    /**
     * This function releases the native NexALFactory.
     */
    private native void release_native();
    
    /** 
     * This method sets the unique application code to native NexALFactory.
     */ 
    private native int set_appcode_native(String strPackage, String strAppCode, String strDeviceUUID);

    /** 
     * This method retrieves the codec list that can be used.
     * 
     */
    private native Object[] getAvailableCodecs_native();
    
    /**
     * This method retrieves the codec list that can be downloaded.
     * 
     */
    private native Object[] getDownloadableCodecs_native();   
    
    /**
     * This method downloads the codec.
     * 
     * @param info
     *            Codec information class object to download.
     * \return Zero if successful or a non-zero error code.
     */
    private native int downloadCodec_native( Object info );
	
    /** 
     * \brief Possible error codes that can be returned by NexPlayer&trade;'s \c NexALFactory.
     *
     * This is a Java \c enum so
     * each error constant is an object, but it can be converted to or from a numerical
     * code using the instance and class methods.
     * 
     * To get the error constant of a given code, call:  {@link com.nexstreaming.nexplayerengine.NexALFactory.NexALFactoryErrorCode#fromIntegerValue(int) fromIntegerValue(int)}.
     *
     * To get the error description given an error constant, call:  {@link com.nexstreaming.nexplayerengine.NexALFactory.NexALFactoryErrorCode#getDesc() getDesc()}.
     * 
     * To get the error code given an error constant, call:  {@link com.nexstreaming.nexplayerengine.NexALFactory.NexALFactoryErrorCode#getIntegerCode() getIntegerCode()}.
     *
     * Because this is a Java \c enum, it is very easy to include the name of the
     * error constant in error messages instead of just the number values.  For example, the following
     * code logs the errors that are received from \c NexALFactory:
     *
     * \code
     * void onCodecDownloaderEventError(NexALFactoryErrorCode errorCode) {
     * {
     *     NexLog.d( "onError",
     *            "Received the error: "
     *               + errorCode.getDesc()
     *               + " ("
     *               + errorCode.getIntegerCode()
     *               + ")."
     *          );
     * }
     * \endcode
     *
     * @author NexStreaming
     * @since version 6.29
     */
    public enum NexALFactoryErrorCode {
    	ERROR_SW_CODEC_DOWNLOAD_INTERNAL_ERROR(-1, "Internal error or invalid socket handle."),    	
    	ERROR_SW_CODEC_DOWNLOAD_RECV_TIME_OUT(-2, "Receive time out."),
    	ERROR_SW_CODEC_DOWNLOAD_DNS_FAIL(-9, "DNS lookup failure."),
    	ERROR_SW_CODEC_DOWNLOAD_CONNECTION_FAIL(-11, "Connection failure.");

    	private int mErrorCode;
    	private String mDesc;
    	
    	NexALFactoryErrorCode( int errorCode, String desc){
    		mErrorCode = errorCode;
    		mDesc = desc;
    	}
    	/** \brief  This method gets the integer code for a given \c NexALFactoryError. */
    	public int getIntegerCode() {
            return mErrorCode;
        }
        /** \brief  This method gets the description of a given \c NexALFactoryError, given an error constant.*/
        public String getDesc() {
            return mDesc;
        }
    	/** \brief  This method gets the error code of a given \c NexALFactoryError, from a given integer value. */
    	public static NexALFactoryErrorCode fromIntegerValue( int code ) {
    		for( int i=0; i<NexALFactoryErrorCode.values().length; i++ ) {
    			if( NexALFactoryErrorCode.values()[i].mErrorCode == code )
    				return NexALFactoryErrorCode.values()[i];
    		}
    		return NexALFactoryErrorCode.values()[0]; //Insure error description.
    	}
    }
    
    @SuppressWarnings("unused") // Actually used (called from native code)
    private static void callbackFromNative(int what, int arg1,int arg2, int arg3,int arg4 )
    {
    	int nRet = 0;
    	if(mCodecDownloadListener == null){
    		return;
    	}
    	switch ( what )
        {
	    	case NEXCODECDOWNLOADER_EVENT_DOWNLOAD_BEGIN:
	    		mCodecDownloadListener.onCodecDownloaderEventBegin(arg1, arg3);
				break;
	    	case NEXCODECDOWNLOADER_EVENT_DOWNLOAD_PROGRESS:
        		mCodecDownloadListener.onCodecDownloaderProgress(arg1, arg4, arg2, arg3);
				break;
        	case NEXCODECDOWNLOADER_EVENT_DOWNLOAD_COMPLETE:
        		mCodecDownloadListener.onCodecDownloaderEventComplete(arg1,arg3);	
				break;
        	case NEXCODECDOWNLOADER_EVENT_DOWNLOAD_ERROR:
        		mCodecDownloadListener.onCodecDownloaderEventError(NexALFactoryErrorCode.fromIntegerValue(arg4));
        		break;
            default:
                break;
        }
    }
    
    /**
     * \warning This method is for internal NexStreaming usage only. Please do not use.
     *
     * \since version 6.16	 
	 */
    public void setCodecDownloadListener( ICodecDownListener listener )
    {
        mCodecDownloadListener = listener;
    }
    
    /** 
     * \warning These methods are for internal NexStreaming usage only.  Please do not use.
     *
     * \since version 6.16
     */
    public interface ICodecDownListener
    {
		/**
		 * For internal use only. Please do not use.
		 *  
		 * \since version 6.16
		 */
        void onCodecDownloaderEventBegin(int param1, long param2);
        
		/**
	     * For internal use only. Please do not use. 
		 * 
		 * \since version 6.16
		 */
		void onCodecDownloaderProgress(int param1, int param2, long param3, long param4);	
        
		/**
		* For internal use only. Please do not use.
		*
		* \since version 6.16
		*/
		void onCodecDownloaderEventComplete(int param1, int result);
		
		/**
		* 
		* For internal use only. Please do not use.
		*
		* \since version 6.29
		*/
		void onCodecDownloaderEventError(NexALFactoryErrorCode errorcode);
	}    
}

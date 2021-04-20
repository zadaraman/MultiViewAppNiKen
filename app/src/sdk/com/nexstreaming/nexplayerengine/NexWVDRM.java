/*
 * DRM Manager class.
 *
 * @copyright:
 * Copyright (c) 2009-2013 NexStreaming Corporation, all rights reserved.
 */

package com.nexstreaming.nexplayerengine;

import java.util.HashMap;

/**
 * \brief  This class allows NexPlayer&trade;&nbsp;to handle and descramble Widevine HLS content.
 *
 * NexWVDRM provides an API for assigning information to NexPlayer to play Widevine DRM applied content.
 * Widevine DRM applied content must receive the key required for descrambling from the specified license server.
 * The license server sends the key only to the authenticated user.
 * So NexPlayer must know the information that can be authenticated from the license server and the URL of the license server.
 * NexPlayer also offers offline playback for Widevine content. To do this, select On (store / retrieve / store & retrieve) / OFF.
 * NexWVDRM provides an API to specify such information and applies the specified information to Widevine module so that Widevine DRM contents can be played back stably.
 */

public class NexWVDRM implements NexWVDRMSession.IWVDRMSessionListener {
    private static final String TAG = "NexWVDRM";
    private static final int NEXWVDRM_EVENT_MODIFYKEYATTR = 0x1000;
    private static final int NEXWVDRM_EVENT_CDM_REQUEST   = 0x1001;
	protected IWVDrmListener m_listener;
	private INexDRMLicenseListener mLicenseRequestListener;
    private HashMap<String, String> mOptionalHeaderFields = null;
    private byte[] mServiceCertificate = null;

    private static final long   REQ_CACH_SERVICE_CERTIFICATE = 0xFFFFFFFF;

    private int licenseRequestTimeout = 30000; //milliseconds
    /**
     * NexWVDRM constructor.
     */
    public NexWVDRM() {
    }

    /**
     * \brief Sets a specified timeout value for conntection and waiting for a server response, in milliseconds
     *
     * \param timeout   an int that specifies the timeout value in milliseconds
     */
    public void setLicenseRequestTimeout(int timeout) {
        if(timeout != 0){
            licenseRequestTimeout = timeout;
        }
        NexHTTPUtil.RequestTimeoutMs = licenseRequestTimeout;
    }

    /**
     * \brief This method initializes the minimum necessary information for playing Widevine DRM contents and registers it in Widevine DRM module.
     *
     * When this API was called service certification will be retrieved automatically internally.
     * This method registers the function pointer of the NexPlayer SDK Engine as a callback function of the Widevine DRM module and requests and receives the authentication information to the license server to descramble the content.
     * And, this information immediately uses or store to the specified path by offline mode
     *
     * \param strEngineLibName	The relevant engine library name as a \c string.
     *                          Register for communication between NexPlayer SDK Engine and Widvine DRM module.
     * \param strFilePath       This is the path to store the key and authentication information received from the License server when Store / Retrieve play is performed.
     *                          In the Offline state, it can not connect to the License Server and stores the received authentication information to a file.
     *                          This information should be used only Widevine DRM module of the certified player inside because it is encrypted.
     * \param strKeyServerURL   This is a license server URL that can receive information for descrambling Widevine DRM contents.
     * \param offlineMode       Select the offline playback mode to use.
     *                          0: On-line mode. NexPlayer will open a temporary DRM session, which does not store any session handles. Network connection is required.
     *				            1: Storing mode. NexPlayer will open a permanent DRM session, and store it after opening it. Network connection is required.
     *				            2: Retrieving mode. NexPlayer will find a stored DRM session, and then restore it.
     *				            3: Storing + Retrieving mode. NexPlayer will find a stored DRM session. If it exists, then NexPlayer will restore it; if not, the engine will create a new session and then store it. Network connection is required.
     *
     * \code
     * NexWVDRM nexWVDRM = new NexWVDRM();
     * String   strEngineLibName = Context.getApplicationInfo().dataDir + "/lib/libnexplayerengine.so";
     * String   strFilePath = Context.getFilesDir().getAbsolutePath() + "/wvcert";
     * String   strKeyServerURL = "http://wv-ref-eme-player.appspot.com/proxy";
     * int      offlineMode = 0;
     * nexWVDRM.initDRMManager(strEngineLibName, strFilePath, strKeyServerURL, offlineMode);
     * \endcode
     */
    public int initDRMManager(String strEnginePath, String strFilePath, String strKeyServerURL, int offlineMode)
    {
        return initDRMManagerMulti(null, strEnginePath, strFilePath, strKeyServerURL, offlineMode);
    }

    /**
     * \brief The minimum necessary information for playing a plurality of Widevine DRM contents is initialized and registered in the Widevine DRM module.
     *
     * When this API was called service certification will be retrieved automatically internally.
     * This method is for using multi instances
     * For multiple instances, the function pointer of NexPlayer SDK Engine is registered as a callback function of Widevine DRM module,
     * and authentication information is requested and received from the license server for descrambling of contents.
     * And, this information immediately uses or store to the specified path by offline mode
     *
     * \param nexplayerInstance Update the callbacks registered in the previously created NexPlayer SDK Engine.
     * \param strEngineLibName	The relevant engine library name as a \c string.
     *                          Register for communication between NexPlayer SDK Engine and Widvine DRM module.
     * \param strFilePath       This is the path to store the key and authentication information received from the License server when Store / Retrieve play is performed.
     *                          In the Offline state, it can not connect to the License Server and stores the received authentication information to a file.
     *                          This information should be used only Widevine DRM module of the certified player inside because it is encrypted.
     * \param strKeyServerURL   This is a license server URL that can receive information for descrambling Widevine DRM contents.
     * \param offlineMode       Select the offline playback mode to use.
     *                          0: On-line mode. NexPlayer will open a temporary DRM session, which does not store any session handles. Network connection is required.
     *				            1: Storing mode. NexPlayer will open a permanent DRM session, and store it after opening it. Network connection is required.
     *				            2: Retrieving mode. NexPlayer will find a stored DRM session, and then restore it.
     *				            3: Storing + Retrieving mode. NexPlayer will find a stored DRM session. If it exists, then NexPlayer will restore it; if not, the engine will create a new session and then store it. Network connection is required.
     *
     * \code
     * NexPlayer    mNexPlayer = new NexPlayer();
     * NexWVDRM     nexWVDRM = new NexWVDRM();
     * String       strEngineLibName = Context.getApplicationInfo().dataDir + "/lib/libnexplayerengine.so";
     * String       strFilePath = Context.getFilesDir().getAbsolutePath() + "/wvcert";
     * String       strKeyServerURL = "http://wv-ref-eme-player.appspot.com/proxy";
     * int          offlineMode = 0;
     *
     * nexWVDRM.initDRMManagerMulti(mNexPlayer, strEngineLibName, strFilePath, strKeyServerURL, offlineMode);
     * \endcode
     */
    public int initDRMManagerMulti(Object nexplayerInstance, String strEnginePath, String strFilePath, String strKeyServerURL, int offlineMode)
    {
        if(!mWVLoadLibrary){
            NexLog.e(TAG, "DRM Init fail - nexwvdrm library failed to load ");
            return -1;
        }

        return initDRMManagerMultiInternal(nexplayerInstance, strEnginePath, strFilePath, strKeyServerURL, offlineMode);
    }
    /**
     * For internal use only. Please do not use.
     */
    private native int initDRMManagerMultiInternal(Object nexPlayerHandle, String strEnginePath, String strFilePath, String strKeyServerURL, int offlineMode);


    /**
     * For internal use only. Please do not use.
     */
    public native void releaseDRMManager();

	/**
     * \brief	This method sets optionalParameters when sending requests to the Key Server of NexWVDRM.
     *
     * Specify the header field and value to be added when sending an http request message to the license server (key server URL) specified when the NexWVDRM.initDRMManager / NexWVDRM.initDRMManagerMulti function is called.
     * The license server can use this header and value to determine whether the user is authenticated.
     * To add an optional header, you must call it before calling the NexWVDRM.initDRMManager / NexWVDRM.initDRMManagerMulti function.
     *
     * \param optionalHeaderFields HashMap is included in the key request message to allow a client application to provide additional message parameters to the server.
     *
     * \code
     * HashMap<String, String>  optionalHeaders = new HashMap<>();
     * String key1 = "test_header1";
     * String value1 = "test_value1";
     * String key2 = "test_header2";
     * String value2 = "test_value2";
     *
     * optionalHeaders.put(key1, value1);
     * optionalHeaders.put(key2, value2);
     *
     * nexWVDRM.setNexWVDrmOptionalHeaderFields(optionalHeaders);
     * \endcode
     */
    public void setNexWVDrmOptionalHeaderFields(HashMap<String, String> optionalHeaderFields)
    {
        mOptionalHeaderFields = optionalHeaderFields;
    }
    private native void setOptionalHeaderFields(Object objHeaderField);
    private native void enableCallback(boolean enable);

    /**
     * For internal use only. Please do not use.
     */
	public native void enableWVDRMLogs(boolean enable);

    /**
     * For internal use only. Please do not use.
     */
	public native void setProperties(int properties, int value);

    /**
     * For internal use only. Please do not use.
     */
    public native void processCdmResponse(byte[] response, long cach);

    //Internal use only.
    private long mNativeContext = 0;

    static private boolean mWVLoadLibrary = false;

    static {
        try {
            System.loadLibrary("nexwvdrm");
            mWVLoadLibrary = true;
        } catch (UnsatisfiedLinkError e) {
            NexLog.e(TAG, "nexwvdrm library failed to load : " + e);
        }		
    }

    /**
     * \brief   When sending http request messages or receiving http response messages to the license server (key server URL) specified through the NexWVDRM.initDRMManager / NexWVDRM.initDRMManagerMulti function,
     *          register a callback function to control this from outside the NexPlayer SDK.
     *
     * \param listener INexDRMLicenseListener: the object on which methods will be called when new events occur.
     *            This must implement the \c INexDRMLicenseListener interface.
     *
     * \code
     * INexDRMLicenseListener licenseRequestListener = new INexDRMLicenseListener() {
     *  @Override
     *  public byte[] onLicenseRequest(byte[] requestData) {
     *      final String LicenseServer = mCurrentExtraData;
     *      Log.d(LOG_TAG, "onLicenseRequest data length : " + requestData.length);
     *      Object response = null;
     *      try {
     *          response = NexHTTPUtil.executePost(LicenseServer, requestData, null);
     *          Log.d(LOG_TAG, "[getLicense ] license resonse: length:" + ((byte[])response).length);
     *      } catch (IOException e) {
     *
     *      }
     *      return (byte[]) response;
     *  }
     * };
     * nexWVDRM.setLicenseRequestListener(licenseRequestListener);
     * \endcode
     */
    public void setLicenseRequestListener(INexDRMLicenseListener listener)
    {
        if(listener != null) {
            mLicenseRequestListener = listener;
        }
    }

	/**
	 * \brief Registers a callback that will be invoked when new events occur.
     *
     * Create and register a listener using NexWVDRM.IWVDrmListener.
     * NexWVDRM.IWVDrmListener currently has an onModifyKeyAttribute, which can be overridden to receive and modify HLS # EXT-X-KEY tag data.
	 *
	 * \param listener IWVDrmListener: the object on which methods will be called when new events occur.
	 *            This must implement the \c IWVDrmListener interface.
     *
     * \code
     * NexWVDRM.IWVDrmListener wvDrmListener = new NexWVDRM.IWVDrmListener() {
     *  @Override
     *  public String onModifyKeyAttribute(String strKeyAttr) {
     *      String strAttr = strKeyAttr;
     *      String strRet = strKeyAttr;
     *      List<String> keyAttrArray = new ArrayList<String>();
     *      String strKeyElem = "";
     *      String strKeyRemain = "";
     *      int end = 0;
     *      while (true) {
     *          end = strAttr.indexOf("\n");
     *          if (end != -1 && end != 0) {
     *              strKeyElem = strAttr.substring(0, end);
     *              keyAttrArray.add(strKeyElem);
     *              strKeyRemain = strAttr.substring(end, strAttr.length());
     *              strAttr = strKeyRemain;
     *          }
     *          else if ((end == -1 || end == 0) && strKeyElem.isEmpty() == false) {
     *              keyAttrArray.add(strAttr.substring(0, strAttr.length()));
     *              break;
     *          }
     *          else {
     *              keyAttrArray.add(strAttr);
     *              break;
     *          }
     *      }
     *
     *      for (int i = 0; i < keyAttrArray.size(); i++) {
     *          strKeyElem = keyAttrArray.get(i);
     *          if (strKeyElem.indexOf("com.widevine") != -1) {
     *              // Found Key
     *              strRet = strKeyElem;
     *              break;
     *          }
     *      }
     *
     *      return strRet;
     *  }
     * };
     * nexWVDRM.setListener(wvDrmListener);
     * \endcode
	 */
    public void setListener(IWVDrmListener listener)
    {
        if(listener != null) {
            enableCallback(true);
            m_listener = listener;
        }
        else
            enableCallback(false);
    }

	/**
	 * \brief The application must implement this interface in order to receive
	 *         events from NexWVDrm.
	 */
	public interface IWVDrmListener {
		/**
		 * \brief This method provides the key attribute that will be used by NexWVDRM when the key attribute is modified.
         *
         * By overriding this function, you can modify the # EXT-X-KEY tag data in the HLS Playlist.
         * The corresponding data is passed as a string to the strKeyAttr parameter.
         * You can refer to the example code setListener.
         *
		 * \param strKeyAttr    strKeyAttr is the key attribute(#EXT-X-KEY) that is written in the HLS playlist file.
 		 * \return A \c string with modified key attribute. NexWVDRM will send this \c string without any modification.
		 * 			If modification is not needed, then the UI should return the input parameter: strKeyAttr.
		 */
		public String onModifyKeyAttribute(String strKeyAttr);
	}

	@Override
    public void processResonsde(byte[] arrResponse, long cach)
    {
        if(arrResponse != null) {
            NexLog.d(TAG, "[processResponse] response len:" + arrResponse.length);
        }

        processCdmResponse(arrResponse, cach);
    }



    //will be called by native
    private String callbackFromNativeStringRet(int msg, int type, byte[] reqMsg, int arg3, long cach, Object what)
    {
        NexLog.d(TAG, "[callbackFromNativeStringRet] msg:"+msg+" reqMsg.length:"+reqMsg.length+" cach:"+Long.toHexString(cach));
        NexLog.d(TAG, "[callbackFromNativeStringRet] URL:" + (String)what);
        switch(msg)
        {
            case NEXWVDRM_EVENT_MODIFYKEYATTR:
            {
                if(m_listener != null)
                {
                    return m_listener.onModifyKeyAttribute((String)what);
                }
                else
                {
                    return (String)what;
                }
            }
            case NEXWVDRM_EVENT_CDM_REQUEST: {
                if (reqMsg.length > 0 && mLicenseRequestListener != null) {
                    NexLog.d(TAG, "[license delegator] before length" + reqMsg.length);
                    final byte[] resp = mLicenseRequestListener.onLicenseRequest(reqMsg);
                    final long cachVal = cach;

                    if (resp == null) {
                        NexLog.e(TAG, "[license delegator] Response is null");
                    }

                    new Thread(new Runnable() {
                        public void run() {
                            processResonsde((byte[]) resp, cachVal);
                        }
                    }).start();
                } else {
                    NexWVDRMSession WVSession = new NexWVDRMSession(this);
                    if (WVSession != null) {
                        WVSession.setOptionalHeaderFields(mOptionalHeaderFields);
                        WVSession.processRequest(type, reqMsg, cach, what);
                    }
                }
            }
            default:
                return null;
        }
    }
}

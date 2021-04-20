/*
 * DRM Manager class.
 *
 * @copyright:
 * Copyright (c) 2009-2013 NexStreaming Corporation, all rights reserved.
 */

package com.nexstreaming.nexplayerengine;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;

import java.io.IOException;
import java.util.HashMap;

/**
 * \brief  This class allows NexPlayer&trade;&nbsp;to handle and descramble WideVine HLS content.
 */

public class NexWVDRMSession {
    private static final String TAG = "NexWVDRMSession";

    private HandlerThread       mRequestHandlerThread = null;
    private Handler             mPostRequestHandler = null;
    private HandlerThread       mResponseHandlerThread = null;
    private Handler             mPostResponseHandler = null;

    private static final int    MSG_PROVISION_REQUEST = 1;
    private static final int    MSG_KEY_REQUEST = 2;

    private HashMap<String, String> mOptionalHeaderFields = null;
    private long                mReqCach;
    private IWVDRMSessionListener mListener = null;

    class _CdmRequestMsg {
        private    String svrUrl;
        private    byte[] reqMessage;
        private    long   cdmCach;

        public    _CdmRequestMsg(String url, byte[] msg, long cach) {
            svrUrl = url;
            reqMessage = msg;
            cdmCach = cach;
        }

        public String getSvrUrl() { return svrUrl; }
        public byte[] getMessage() { return reqMessage; }
        public long   getCach() { return cdmCach; }
    }

    public interface IWVDRMSessionListener {
        void processResonsde(byte[] arrResponse, long cach);
    }

    /**
     * NexWVDRM constructor.
     */
    public NexWVDRMSession(IWVDRMSessionListener l) {
        mListener = l;

        NexLog.d(TAG, "[NexWVDRMSession] mListener : " + mListener.toString());


        if (mPostRequestHandler == null) {
            mRequestHandlerThread = new HandlerThread("DrmRequestHandler");
            mRequestHandlerThread.start();
            mPostRequestHandler = new NexWVDRMSession.PostRequestHandler(mRequestHandlerThread.getLooper());
        }

        if (mPostResponseHandler == null) {
            mResponseHandlerThread = new HandlerThread("DrmResponseHandler");
            mResponseHandlerThread.start();
            mPostResponseHandler = new NexWVDRMSession.PostResponseHandler(mResponseHandlerThread.getLooper());
        }

    }

	/**
     * \brief	This method sets optionalParameters when sending requests to the Key Server of NexWVDRM.
     * @param optionalHeaderFields HashMap is included in the key request message to allow a client application to provide additional message parameters to the server.
     */
    public void setOptionalHeaderFields(HashMap<String, String> optionalHeaderFields)
    {
        mOptionalHeaderFields = optionalHeaderFields;
    }

    public void processRequest(int type, byte[] reqMsg, long cach, Object url)
    {
        String svrUrl = (String)url;
        mReqCach = cach;
        _CdmRequestMsg request = new _CdmRequestMsg(svrUrl, reqMsg, cach);

        NexLog.e(TAG, "URL : " + svrUrl);
        NexLog.e(TAG, "type : " + type);
        NexLog.e(TAG, "request len : " + reqMsg.length);
        NexLog.e(TAG, "cach : " + Long.toHexString(cach));
        mPostRequestHandler.obtainMessage(type, request).sendToTarget();
    }


    @SuppressLint("HandlerLeak")
    private class PostResponseHandler extends Handler {

        public PostResponseHandler(Looper backgroundLooper) {
            super(backgroundLooper);
        }

        @Override
        public void handleMessage(Message msg) {
            /*
            switch (msg.what) {
                case MSG_PROVISION_REQUEST:
                case MSG_KEY_REQUEST:
                    procCdmResponse(msg.obj);
                    break;
            }
            */
            NexLog.d(TAG, "[PostResponseHandler] before procCmdResponse.");
            procCdmResponse(msg.obj);
        }
    }

    @SuppressLint("HandlerLeak")
    private class PostRequestHandler extends Handler {

        public PostRequestHandler(Looper backgroundLooper) {
            super(backgroundLooper);
        }

        @Override
        public void handleMessage(Message msg) {
            Object response = null;
            _CdmRequestMsg reqMsg = (_CdmRequestMsg)msg.obj;
            try {

                switch (msg.what) {
                    case MSG_PROVISION_REQUEST:
                        response = executeProvisionRequest(reqMsg);
                        break;
                    case MSG_KEY_REQUEST:
                    default:
                        response = executeKeyRequest(reqMsg);
                        break;
                }
            } catch (Exception e) {
                response = e;
            }

            if( mPostResponseHandler != null )
                mPostResponseHandler.obtainMessage(msg.what, response).sendToTarget();
        }
    }

    @TargetApi(8)
    private void procCdmResponse(Object response) {
        if (response instanceof Exception) {
            NexLog.e(TAG, "[procCmdResponse] Exception.");
            if(mListener != null) {
                mListener.processResonsde(null, mReqCach);
            }
            return;
        }

        try {
            if(mListener != null) {
                byte[] cdmResponse = (byte[])response;
                NexLog.d(TAG, "[procCdmResponse] response len : " + cdmResponse.length);
                //String strResponse = new String((byte[]) response, 0, cdmResponse.length);
                String strResponse = Base64.encodeToString(cdmResponse, Base64.NO_WRAP);
                NexLog.d(TAG, "CdmResponse["+strResponse.length()+"] : " + strResponse);
                mListener.processResonsde(cdmResponse, mReqCach);
            }
            return;
        } catch (Exception e) {
            return;
        }
    }

    private Object executeProvisionRequest(_CdmRequestMsg request) {
        NexLog.d(TAG, "[PostRequestHandler] executeProvisionRequest...");

        Object response;
        String url = request.getSvrUrl();
        try {
            response = NexHTTPUtil.executePost(url, null, null);
        } catch (IOException e) {
            IOException retException = new IOException("executeProvisionRequest error");
            response = retException;
        }

        return response;
    }

    private Object executeKeyRequest(_CdmRequestMsg request)
    {
        NexLog.d(TAG, "[PostRequestHandler] executeKeyRequest...");
        Object response = null;
        if(request == null)
        {
            NexLog.e(TAG, "Invalid request data.");
            return null;
        }


        String url = request.getSvrUrl();

        if (TextUtils.isEmpty(url)) {
            NexLog.e(TAG, "Invalid key server address");
            IOException retException = new IOException("Invalid key server address");
            return retException;
        }

        try {
            NexLog.d(TAG, "executeKeyRequest...");

            if(mOptionalHeaderFields == null) {
                mOptionalHeaderFields = new HashMap<String, String>();
            }


            if(url.contains("signedRequest") == false) {

                if (!mOptionalHeaderFields.containsKey("Content-Type")) {
                    mOptionalHeaderFields.put("Content-Type", "application/octet-stream");
                }
            }

            byte[] byteRequest = request.getMessage();
            response = NexHTTPUtil.executePost(url, byteRequest, mOptionalHeaderFields);
            NexLog.d(TAG, "[PostRequestHandler] executeKeyRequest... Done. ResponseLen:");
        }
        catch (IOException e)
        {
            NexLog.e(TAG, "[executeKeyRequest] IOException error :" + e.toString());
            IOException retException = new IOException("executeKeyRequest error");
            return retException;
        }


        return response;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        if(mRequestHandlerThread != null){
            mRequestHandlerThread.quit();
            mRequestHandlerThread = null;
        }

        if(mResponseHandlerThread != null){
            mResponseHandlerThread.quit();
            mResponseHandlerThread = null;
        }
    }
}
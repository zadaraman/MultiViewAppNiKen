package com.nexstreaming.nexplayerengine;

/*
 * API for process MediaDrm .
 *
 * \since version 6.37.0
 */

import android.annotation.TargetApi;
import android.media.DeniedByServerException;
import android.media.MediaDrm;
import android.media.MediaDrmException;
import android.media.NotProvisionedException;
import android.media.UnsupportedSchemeException;
import android.os.Build;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/** \deprecated  For internal use only.  Please do not use. */
@TargetApi(18)
public class NexMediaDrm{
    private final static String TAG = "NexMediaDrm";

    private final MediaDrm mediaDrm;

    private static final UUID COMMON_PSSH_UUID = new UUID(0x1077EFECC0B24D02L, 0xACE33C1E52E2FB4BL);

    static final UUID CLEARKEY_UUID = new UUID(0xE2719D58A985B3C9L, 0x781AB030AF78D30EL);

    static final UUID WIDEVINE_UUID = new UUID(0xEDEF8BA979D64ACEL, 0xA3C827DCD51D21EDL);

    static final UUID PLAYREADY_UUID = new UUID(0x9A04F07998404286L, 0xAB92E65BE0885F95L);

    static final long TIME_UNSET = Long.MIN_VALUE + 1;

    static final int SCHEME_UNKNOWN_TYPE = 0;
    static final int SCHEME_WIDEVINE_TYPE = 1;
    static final int SCHEME_PLAYREADY_TYPE = 2;

    static NexMediaDrm newInstance(UUID uuid) throws Exception {
        return new NexMediaDrm(uuid);
    }

    private NexMediaDrm(UUID uuid) throws UnsupportedSchemeException {
        // ClearKey had to be accessed using the Common PSSH UUID prior to API level 27.
        uuid = android.os.Build.VERSION.SDK_INT <= 26 && CLEARKEY_UUID.equals(uuid) ? COMMON_PSSH_UUID : uuid;
        this.mediaDrm = new MediaDrm(uuid);
    }

    public String getSecurityLevel(){
        return (mediaDrm != null)?  mediaDrm.getPropertyString("securityLevel"): null;
    }

    public void forceToL3(){
        if(mediaDrm != null){
            mediaDrm.setPropertyString("securityLevel", "L3");
        }
    }

    public String getOrigin(){
        return (mediaDrm != null)?  mediaDrm.getPropertyString("origin"): null;
    }
    public void setOrigin(String origin){
        if(mediaDrm != null){
            mediaDrm.setPropertyString("origin", origin);
        }
    }

    public void unprovisionDevice(){
        byte[] response = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mediaDrm != null) {
                response = "unprovision".getBytes();
                try {
                    mediaDrm.provideProvisionResponse(response);
                } catch (DeniedByServerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void setOnEventListener(
            final NexMediaDrm.OnEventListener listener) {
        mediaDrm.setOnEventListener(listener == null ? null : new MediaDrm.OnEventListener() {
            @Override
            public void onEvent(MediaDrm md, byte[] sessionId, int event, int extra, byte[] data) {
                listener.onEvent(NexMediaDrm.this, sessionId, event, extra, data);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void setOnKeyStatusChangeListener(final NexMediaDrm.OnKeyStatusChangeListener listener) {
        if (Build.VERSION.SDK_INT < 23) {
            throw new UnsupportedOperationException();
        }

        mediaDrm.setOnKeyStatusChangeListener(listener == null ? null
                : new MediaDrm.OnKeyStatusChangeListener() {
            @Override
            public void onKeyStatusChange(MediaDrm md, byte[] sessionId, List<MediaDrm.KeyStatus> keyInfo, boolean hasNewUsableKey) {
                List<KeyStatus> keyStatusInfo = new ArrayList<KeyStatus>();
                for (MediaDrm.KeyStatus keyStatus : keyInfo) {
                    keyStatusInfo.add(new DefaultKeyStatus(keyStatus.getStatusCode(), keyStatus.getKeyId()));
                }
                listener.onKeyStatusChange(NexMediaDrm.this, sessionId, keyStatusInfo, hasNewUsableKey);
            }
        }, null);
    }

    byte[] openSession() throws MediaDrmException {
        return mediaDrm.openSession();
    }

    void closeSession(byte[] sessionId) {
        mediaDrm.closeSession(sessionId);
    }

    void removeKeys(byte[] sessionId) {
        mediaDrm.removeKeys(sessionId);
    }

    NexMediaDrm.KeyRequest getKeyRequest(byte[] scope, byte[] init, String mimeType, int keyType,
                                         HashMap<String, String> optionalParameters) throws NotProvisionedException {
        final MediaDrm.KeyRequest request = mediaDrm.getKeyRequest(scope, init, mimeType, keyType, optionalParameters);
        return new DefaultKeyRequest(request.getData(), request.getDefaultUrl());
    }

    byte[] provideKeyResponse(byte[] scope, byte[] response)
            throws NotProvisionedException, DeniedByServerException {
        return mediaDrm.provideKeyResponse(scope, response);
    }

    DefaultProvisionRequest getProvisionRequest() {
        final MediaDrm.ProvisionRequest request = mediaDrm.getProvisionRequest();
        return new DefaultProvisionRequest(request.getData(), request.getDefaultUrl());
    }

    void provideProvisionResponse(byte[] response) throws DeniedByServerException {
        mediaDrm.provideProvisionResponse(response);
    }

    Map<String, String> queryKeyStatus(byte[] sessionId) {
        try {
            Map<String, String> keyStatus = mediaDrm.queryKeyStatus(sessionId);
            return keyStatus;
        } catch(Exception e) {
            return null;
        }
    }

    public void release() {
        mediaDrm.release();
    }

    void restoreKeys(byte[] sessionId, byte[] keySetId) {
        mediaDrm.restoreKeys(sessionId, keySetId);
    }

    String getPropertyString(String propertyName) {
        return mediaDrm.getPropertyString(propertyName);
    }

    byte[] getPropertyByteArray(String propertyName) {
        return mediaDrm.getPropertyByteArray(propertyName);
    }

    void setPropertyString(String propertyName, String value) {
        mediaDrm.setPropertyString(propertyName, value);
    }

    void setPropertyByteArray(String propertyName, byte[] value) {
        mediaDrm.setPropertyByteArray(propertyName, value);
    }

    interface KeyStatus {
        int getStatusCode();
        byte[] getKeyId();
    }

    /**
     * @see android.media.MediaDrm.ProvisionRequest
     */
    interface ProvisionRequest {
        byte[] getData();
        String getDefaultUrl();
    }

    final class DefaultProvisionRequest implements ProvisionRequest {

        private final byte[] data;
        private final String defaultUrl;

        DefaultProvisionRequest(byte[] data, String defaultUrl) {
            this.data = data;
            this.defaultUrl = defaultUrl;
        }

        @Override
        public byte[] getData() {
            return data;
        }

        @Override
        public String getDefaultUrl() {
            return defaultUrl;
        }

    }

    /**
     * Default implementation of {@link KeyStatus}.
     */
    final class DefaultKeyStatus implements KeyStatus {

        private final int statusCode;
        private final byte[] keyId;

        DefaultKeyStatus(int statusCode, byte[] keyId) {
            this.statusCode = statusCode;
            this.keyId = keyId;
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public byte[] getKeyId() {
            return keyId;
        }

    }

    /**
     * @see android.media.MediaDrm.KeyRequest
     */
    interface KeyRequest {
        byte[] getData();
        String getDefaultUrl();
    }

    /**
     * Default implementation of {@link KeyRequest}.
     */
    static final class DefaultKeyRequest implements KeyRequest {

        private final byte[] data;
        private final String defaultUrl;

        DefaultKeyRequest(byte[] data, String defaultUrl) {
            this.data = data;
            this.defaultUrl = defaultUrl;
        }

        @Override
        public byte[] getData() {
            return data;
        }

        @Override
        public String getDefaultUrl() {
            return defaultUrl;
        }

    }

    interface OnEventListener {
        /**
         * Called when an event occurs that requires the app to be notified
         *
         * @param mediaDrm the {@link NexMediaDrm} object on which the event occurred.
         * @param sessionId the DRM session ID on which the event occurred
         * @param event indicates the event type
         * @param extra an secondary error code
         * @param data optional byte array of data that may be associated with the event
         */
        void onEvent(NexMediaDrm mediaDrm, byte[] sessionId, int event, int extra, byte[] data);
    }

    interface OnKeyStatusChangeListener {
        /**
         * Called when the keys in a session change status, such as when the license is renewed or
         * expires.
         *
         * @param mediaDrm the {@link NexMediaDrm} object on which the event occurred.
         * @param sessionId the DRM session ID on which the event occurred.
         * @param exoKeyInfo a list of {@link KeyStatus} that contains key ID and status.
         * @param hasNewUsableKey true if new key becomes usable.
         */
        void onKeyStatusChange(NexMediaDrm mediaDrm, byte[] sessionId,
                               List<KeyStatus> exoKeyInfo, boolean hasNewUsableKey);
    }

    static final class HttpNexMediaDrmCallback {
        private Map<String, String> keyRequestProperties;
        private final String defaultLicenseUrl;
        private final INexDRMLicenseListener licenseListener;

        HttpNexMediaDrmCallback(String defaultLicenseUrl, INexDRMLicenseListener licenseListener, HashMap<String, String> optionalHeaderFields) {
            this.defaultLicenseUrl = defaultLicenseUrl;
            keyRequestProperties = optionalHeaderFields;

            if (null == keyRequestProperties) {
                keyRequestProperties = new HashMap<String, String>();
            }

            this.licenseListener = licenseListener;
        }

        void setOptionalHeaderFields(HashMap<String, String> optionalHeaderFields) {
            if (null != optionalHeaderFields) {
                keyRequestProperties = optionalHeaderFields;
            } else {
                keyRequestProperties.clear();
            }
        }

        Object executeProvisionRequest(UUID uuid, NexMediaDrm.ProvisionRequest request) {
            NexLog.d(TAG, "[PostRequestHandler] executeProvisionRequest...");

            Object response;
            String url = request.getDefaultUrl() + "&signedRequest=" + new String(request.getData());
            try {
                response = NexHTTPUtil.executePost(url, null, null);
            } catch (IOException e) {
                response = e;
            }

            return response;
        }

        Object executeKeyRequest(UUID uuid, NexMediaDrm.KeyRequest request) {
            NexLog.d(TAG, "[PostRequestHandler] executeKeyRequest...");

            if (request == null) {
                return new Exception("Invalid request data.");
            }

            Object response = null;

            if (null != licenseListener) {
                NexLog.d(TAG, " get key response by listener...");
                response = licenseListener.onLicenseRequest(request.getData());
                if (null == response) {
                    return new Exception("licenseListener Response is null");
                }
            }

            if (null == response) {
                NexLog.d(TAG, " get key response ...");

                String url = request.getDefaultUrl();
                if (TextUtils.isEmpty(url)) {
                    url = defaultLicenseUrl;//"http://wv-ref-eme-player.appspot.com/proxy";
                }

                if (TextUtils.isEmpty(url)) {
                    return new IOException("Invalid key server address");
                }

                try {
                    NexLog.d(TAG, "executeKeyRequest...");

                    if (!keyRequestProperties.containsKey("Content-Type")) {
                        String contentType = PLAYREADY_UUID.equals(uuid) ? "text/xml"
                                : (CLEARKEY_UUID.equals(uuid) ? "application/json" : "application/octet-stream");

                        keyRequestProperties.put("Content-Type", contentType);
                    }
                    if (PLAYREADY_UUID.equals(uuid)) {
                        keyRequestProperties.put("SOAPAction",
                                "http://schemas.microsoft.com/DRM/2007/03/protocols/AcquireLicense");
                    }

                    response = NexHTTPUtil.executePost(url, request.getData(), keyRequestProperties);
                    NexLog.d(TAG, "executeKeyRequest... Done");
                } catch (Exception e) {
                    NexLog.e(TAG, "[executeKeyRequest] Exception error :" + e.toString());
                    return e;
                }
            }

            NexLog.d(TAG, "[PostRequestHandler] executeKeyRequest... Done");

            return response;
        }
    }

    static boolean isSupportDRMScheme(UUID uuid) {
        return MediaDrm.isCryptoSchemeSupported(uuid);
    }
}

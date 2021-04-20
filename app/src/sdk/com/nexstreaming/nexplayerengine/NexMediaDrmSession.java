package com.nexstreaming.nexplayerengine;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.media.MediaDrm;
import android.media.NotProvisionedException;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nexstreaming.nexplayerengine.NexMediaDrm.TIME_UNSET;


@TargetApi(18)
public class NexMediaDrmSession {

    public interface ProvisioningManager {
        void provisionRequired(NexMediaDrmSession session);

        void onProvisionError(Exception error);

        void onProvisionCompleted();
    }

    private static final String TAG = "NexMediaDrmSession";

    private static final int MSG_PROVISION = 0;
    private static final int MSG_KEYS = 1;
    private static final int MAX_LICENSE_DURATION_TO_RENEW = 60;

    private final NexMediaDrm mediaDrm;
    private final ProvisioningManager provisioningManager;
    private final byte[] initData;
    private final String mimeType;
    private final int mode;
    private final HashMap<String, String> optionalKeyRequestParameters;
    private final NexMediaDrmSessionManager.EventListener eventListener;
    private ArrayList<Integer> mediaTypes;
    private int uniqueId;
    private final int initialDrmRequestRetryCount;

    final NexMediaDrm.HttpNexMediaDrmCallback callback;
    private final UUID uuid;
    private final PostResponseHandler postResponseHandler;

    private int state;
    private int openCount;
    private HandlerThread requestHandlerThread;
    private PostRequestHandler postRequestHandler;
    private byte[] sessionId;
    private byte[] offlineLicenseKeySetId;

    final static int STATE_RELEASED = 0;

    final static int STATE_ERROR = 1;

    final static int STATE_OPENING = 2;

    final static int STATE_OPENED = 3;

    final static int STATE_OPENED_WITH_KEYS = 4;

    private static final int PSSH_SIZE = 32;

    //PSSH v1
    private int nBytesToSkip = 16;
    private int nKeyIDs = 0;

    private static final int VERSION_INDEX = 8;

    private static final int READ_NUMBER_OF_KEYIDS = 4;

    private static final int NUMBER_OF_KEYIDS_INDEX = 28;

    private static final int DATA_SIZE = 4;

    NexMediaDrmSession(UUID uuid, NexMediaDrm mediaDrm,
                       ProvisioningManager provisioningManager, byte[] initData, String mimeType,
                       int mode, byte[] offlineLicenseKeySetId,
                       HashMap<String, String> optionalKeyRequestParameters, NexMediaDrm.HttpNexMediaDrmCallback callback,
                       Looper playbackLooper,
                       NexMediaDrmSessionManager.EventListener eventListener,
                       int initialDrmRequestRetryCount) {
        this.uuid = uuid;
        this.provisioningManager = provisioningManager;
        this.mediaDrm = mediaDrm;
        this.mode = mode;
        this.offlineLicenseKeySetId = offlineLicenseKeySetId;
        this.optionalKeyRequestParameters = optionalKeyRequestParameters;
        this.callback = callback;
        this.initialDrmRequestRetryCount = initialDrmRequestRetryCount;
        this.eventListener = eventListener;
        this.mediaTypes = new ArrayList<Integer>();


        state = STATE_OPENING;

        postResponseHandler = new PostResponseHandler(playbackLooper);
        requestHandlerThread = new HandlerThread("DrmRequestHandler");
        requestHandlerThread.start();
        postRequestHandler = new PostRequestHandler(requestHandlerThread.getLooper());

        if (mode == NexMediaDrmSessionManager.MODE_KEYEXPIRE_DOWNLOAD_AND_PLAYBACK) {
            if (isPSSHContained(initData)) {
                int index = 0;

                byte[] newPssh = createPsshBox(initData);

                int initPoint = initData.length  - (initData.length - PSSH_SIZE - nBytesToSkip);
                int modInitPoint = (nKeyIDs > 0) ? initPoint += DATA_SIZE : initPoint;

                for (int i = modInitPoint; i < initData.length; i++) {
                    newPssh[index] = initData[i];
                    index++;
                }
                this.initData = newPssh;
            } else {
                this.initData = initData;
            }

            this.mimeType = mimeType;
        } else {
            if (offlineLicenseKeySetId == null) {
                if (isPSSHContained(initData)) {
                    int index = 0;

                    byte[] newPssh = createPsshBox(initData);

                    int initPoint = initData.length  - (initData.length - PSSH_SIZE - nBytesToSkip);
                    int modInitPoint = (nKeyIDs > 0) ? initPoint += DATA_SIZE : initPoint;

                    for (int i = modInitPoint; i < initData.length; i++) {
                        newPssh[index] = initData[i];
                        index++;
                    }
                    this.initData = newPssh;
                } else {
                    this.initData = initData;
                }

                this.mimeType = mimeType;
            } else {
                this.initData = null;
                this.mimeType = null;
            }
        }
    }

    private byte[] createPsshBox(byte [] data){
        if(data[VERSION_INDEX] > 0){
            for(int i = 0; i < READ_NUMBER_OF_KEYIDS; i++){
                int value = data[i + NUMBER_OF_KEYIDS_INDEX];
                nKeyIDs += value;
            }
            nBytesToSkip*=nKeyIDs;
            return new byte[data.length - PSSH_SIZE - nBytesToSkip - DATA_SIZE];
        }else{
            nBytesToSkip = 0;
            return new byte[data.length - PSSH_SIZE];
        }
    }

    private boolean isPSSHContained(byte [] pssh) {
        boolean contained = false;
        if(pssh.length > 0) {
            if (('p' == pssh[4] && 's' == pssh[5] && 's' == pssh[6]  && 'h' == pssh[7] )) {
                contained = true;
            } else {
                contained = false;
            }
        }
        return contained;
    }

    void acquire() {
        if (++openCount == 1) {
            if (state == STATE_ERROR) {
                return;
            }
            if (openInternal(true)) {
                doLicense(true);
            }
        }
    }

    public boolean release() {
        if (--openCount == 0) {
            state = STATE_RELEASED;
            postResponseHandler.removeCallbacksAndMessages(null);
            postRequestHandler.removeCallbacksAndMessages(null);
            postRequestHandler = null;
            requestHandlerThread.quit();
            requestHandlerThread = null;
            if (sessionId != null) {
                mediaDrm.closeSession(sessionId);
                sessionId = null;
            }
            return true;
        }
        return false;
    }

    boolean hasInitData(byte[] initData) {
        if (isPSSHContained(initData)) {
            int index = 0;

            byte[] newPssh = createPsshBox(initData);

            int initPoint = initData.length  - (initData.length - PSSH_SIZE - nBytesToSkip);
            int modInitPoint = (nKeyIDs > 0) ? initPoint += DATA_SIZE : initPoint;

            for (int i = modInitPoint; i < initData.length; i++) {
                newPssh[index] = initData[i];
                index++;
            }
            return Arrays.equals(this.initData, newPssh);
        } else {
            return Arrays.equals(this.initData, initData);
        }
    }

    boolean hasSessionId(byte[] sessionId) {
        return Arrays.equals(this.sessionId, sessionId);
    }

    int addMediaType(int mediaType) {
       mediaTypes.add(mediaType);
       return mediaTypes.size();
    }

    int getMediaTypeCount() {
        return mediaTypes.size();
    }

    int getMediaType(int index) {
        return mediaTypes.get(index);
    }

    void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }

    int getUniqueId() {
        return uniqueId;
    }

    void provision() {
        NexMediaDrm.ProvisionRequest request = mediaDrm.getProvisionRequest();
        postRequestHandler.obtainMessage(MSG_PROVISION, request, true).sendToTarget();
    }

    void onProvisionCompleted() {
        if (openInternal(false)) {
            doLicense(true);
        }
    }

    void onProvisionError(Exception error) {
        onError(error);
    }

    public final int getState() {
        return state;
    }

    byte[] getSessionId() {
        return sessionId;
    }

    Map<String, String> queryKeyStatus() {
        return sessionId == null ? null : mediaDrm.queryKeyStatus(sessionId);
    }

    private boolean openInternal(boolean allowProvisioning) {
        if (isOpen()) {
            // Already opened
            return true;
        }

        try {
            sessionId = mediaDrm.openSession();
            state = STATE_OPENED;
            return true;
        } catch (NotProvisionedException e) {
            if (allowProvisioning) {
                provisioningManager.provisionRequired(this);
            } else {
                onError(e);
            }
        } catch (Exception e) {
            onError(e);
        }

        return false;
    }

    private void onProvisionResponse(Object response) {
        if (state != STATE_OPENING && !isOpen()) {
            // This event is stale.
            return;
        }

        if (response instanceof Exception) {
            provisioningManager.onProvisionError((Exception) response);
            return;
        }

        try {
            mediaDrm.provideProvisionResponse((byte[]) response);
        } catch (Exception e) {
            provisioningManager.onProvisionError(e);
            return;
        }

        provisioningManager.onProvisionCompleted();
    }

    private void doLicense(boolean allowRetry) {
        switch (mode) {
            case NexMediaDrmSessionManager.MODE_PLAYBACK:
            case NexMediaDrmSessionManager.MODE_QUERY:
                if (offlineLicenseKeySetId == null) {
                    postKeyRequest(MediaDrm.KEY_TYPE_STREAMING, allowRetry);
                } else if (state == STATE_OPENED_WITH_KEYS || restoreKeys()) {
                    long licenseDurationRemainingSec = getLicenseDurationRemainingSec();
                    if (mode == NexMediaDrmSessionManager.MODE_PLAYBACK
                            && licenseDurationRemainingSec <= MAX_LICENSE_DURATION_TO_RENEW) {
                        NexLog.d(TAG, "Offline license has expired or will expire soon. "
                                + "Remaining seconds: " + licenseDurationRemainingSec);
                        postKeyRequest(MediaDrm.KEY_TYPE_OFFLINE, allowRetry);
                    } else if (licenseDurationRemainingSec <= 0) {
                        onError(new Exception("key expired"));
                    } else {
                        state = STATE_OPENED_WITH_KEYS;
                        if (eventListener != null) {
                            eventListener.onDrmKeysRestored();
                        }
                    }
                }
                break;
            case NexMediaDrmSessionManager.MODE_DOWNLOAD:
            case NexMediaDrmSessionManager.MODE_DOWNLOAD_AND_PLAYBACK:
                if (offlineLicenseKeySetId == null) {
                    postKeyRequest(MediaDrm.KEY_TYPE_OFFLINE, allowRetry);
                } else {
                    if (restoreKeys()) {
                        postKeyRequest(MediaDrm.KEY_TYPE_OFFLINE, allowRetry);
                    }
                }
                break;
            case NexMediaDrmSessionManager.MODE_KEYEXPIRE_DOWNLOAD_AND_PLAYBACK:
                    if (restoreKeys()) {
                        long licenseDurationRemainingSec = getLicenseDurationRemainingSec();
                        if (licenseDurationRemainingSec <= 0) {
                            NexLog.d(TAG, "License Expired - DOWNLOAD_AND_PLAYBACK Mode");

                            onKeyExpired(new Exception(("key expired")));
                        } else {
                            NexLog.d(TAG, "License is not Expired - DOWNLOAD_AND_PLAYBACK Mode");
                            state = STATE_OPENED_WITH_KEYS;
                            if (eventListener != null) {
                                eventListener.onDrmKeysRestored();
                            }
                        }
                    }
                    break;
            case NexMediaDrmSessionManager.MODE_RELEASE:
                if (restoreKeys()) {
                    postKeyRequest(MediaDrm.KEY_TYPE_RELEASE, allowRetry);
                }
                break;
            default:
                break;
        }
    }

    public void updateDRMKey() {
        NexLog.d(TAG, "Start to postKeyRequest for updateDRMKey");

        // Remove Before Session
        mediaDrm.removeKeys(sessionId);
        mediaDrm.closeSession(sessionId);

        try {
            sessionId = mediaDrm.openSession();
        } catch (Exception e) {
            e.printStackTrace();
        }

        postKeyRequest(MediaDrm.KEY_TYPE_OFFLINE, true);
    }

    private boolean restoreKeys() {
        NexLog.d(TAG, "RestoreKeys");
        try {
            mediaDrm.restoreKeys(sessionId, offlineLicenseKeySetId);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error trying to restore Widevine keys.", e);
            onError(e);
        }
        return false;
    }

    private long getLicenseDurationRemainingSec() {
        if (!NexMediaDrm.WIDEVINE_UUID.equals(uuid)) {
            return Long.MAX_VALUE;
        }
        Pair<Long, Long> pair = WidevineUtil.getLicenseDurationRemainingSec(this);

        long result = 0;
        if (null != pair) {
            result = Math.min(pair.first, pair.second);
        }

        return result;
    }

    private void postKeyRequest(int type, boolean allowRetry) {
        byte[] scope = type == MediaDrm.KEY_TYPE_RELEASE ? offlineLicenseKeySetId : sessionId;
        try {
            NexMediaDrm.KeyRequest request = mediaDrm.getKeyRequest(scope, initData, mimeType, type,
                    optionalKeyRequestParameters);
            if (NexMediaDrm.CLEARKEY_UUID.equals(uuid)) {
                request = new NexMediaDrm.DefaultKeyRequest(ClearKeyUtil.adjustRequestData(request.getData()), request.getDefaultUrl());
            }
            //Log.d(TAG, "DRM Request");
            //Log.d(TAG, new String(request.getData()));
            postRequestHandler.obtainMessage(MSG_KEYS, request, allowRetry).sendToTarget();
        } catch (Exception e) {
            onKeysError(e);
        }
    }

    private void onKeyResponse(Object response) {
        if (!isOpen()) {
            return;
        }

        if (response instanceof Exception) {
            onKeysError((Exception) response);
            return;
        }

        try {
            byte[] responseData = (byte[]) response;
            //Log.d(TAG, "DRM Response");
            //Log.d(TAG, new String(responseData));
            if (NexMediaDrm.CLEARKEY_UUID.equals(uuid)) {
                responseData = ClearKeyUtil.adjustResponseData(responseData);
            }
            if (mode == NexMediaDrmSessionManager.MODE_RELEASE) {
                mediaDrm.provideKeyResponse(offlineLicenseKeySetId, responseData);
                if (eventListener != null) {
                    eventListener.onDrmKeysRemoved();
                }
            } else {
                final byte[] keySetId = mediaDrm.provideKeyResponse(sessionId, responseData);
                if ((mode == NexMediaDrmSessionManager.MODE_DOWNLOAD
                        || (mode == NexMediaDrmSessionManager.MODE_PLAYBACK && offlineLicenseKeySetId != null)
                        || (mode == NexMediaDrmSessionManager.MODE_DOWNLOAD_AND_PLAYBACK && offlineLicenseKeySetId != null))
                        && keySetId != null && keySetId.length != 0) {
                    NexLog.d(TAG, "Set KeySetId to offlineLicenseKeySetId");
                    offlineLicenseKeySetId = keySetId;
                }
                state = STATE_OPENED_WITH_KEYS;
                if (eventListener != null) {
                    eventListener.onDrmKeysLoaded(keySetId, sessionId);
                }
            }
        } catch (Exception e) {
            onKeysError(e);
        }
    }

    private void onKeysExpired() {
        if (state == STATE_OPENED_WITH_KEYS) {
            state = STATE_OPENED;
            onError(new Exception("key expired"));
        }
    }

    private void onKeysError(Exception e) {
        if (e instanceof NotProvisionedException) {
            provisioningManager.provisionRequired(this);
        } else {
            onError(e);
        }
    }

    private void onError(final Exception e) {
        if (state != STATE_OPENED_WITH_KEYS) {
            state = STATE_ERROR;
        }
        if (eventListener != null) {
            eventListener.onDrmSessionManagerError(e);
        }
    }

    private void onKeyExpired(final Exception e) {
        if (eventListener != null) {
            eventListener.onDrmKeyExpired(e);
        }
    }

    private boolean isOpen() {
        return state == STATE_OPENED || state == STATE_OPENED_WITH_KEYS;
    }

    @SuppressWarnings("deprecation")
    void onMediaDrmEvent(int what) {
        if (!isOpen()) {
            return;
        }
        switch (what) {
            case MediaDrm.EVENT_KEY_REQUIRED:
                doLicense(false);
                break;
            case MediaDrm.EVENT_KEY_EXPIRED:
                onKeysExpired();
                break;
            case MediaDrm.EVENT_PROVISION_REQUIRED:
                state = STATE_OPENED;
                provisioningManager.provisionRequired(this);
                break;
            default:
                break;
        }

    }

    @SuppressLint("HandlerLeak")
    private class PostResponseHandler extends Handler {

        PostResponseHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PROVISION:
                    onProvisionResponse(msg.obj);
                    break;
                case MSG_KEYS:
                    onKeyResponse(msg.obj);
                    break;
                default:
                    break;

            }
        }

    }

    @SuppressLint("HandlerLeak")
    private class PostRequestHandler extends Handler {

        PostRequestHandler(Looper backgroundLooper) {
            super(backgroundLooper);
        }

        Message obtainMessage(int what, Object object, boolean allowRetry) {
            return obtainMessage(what, allowRetry ? 1 : 0 /* allow retry*/, 0 /* error count */,
                    object);
        }

        @Override
        public void handleMessage(Message msg) {
            Object response;
            try {
            switch (msg.what) {
                    case MSG_PROVISION:
                        response = callback.executeProvisionRequest(uuid, (NexMediaDrm.ProvisionRequest) msg.obj);
                        break;
                    case MSG_KEYS:
                        response = callback.executeKeyRequest(uuid, (NexMediaDrm.KeyRequest) msg.obj);
                        break;
                    default:
                        NexLog.d(TAG, "default response: " + msg.what);
                        throw new RuntimeException();
                }
            } catch (Exception e) {
                if (maybeRetryRequest(msg)) {
                    return;
                }
                response = e;
            }
            postResponseHandler.obtainMessage(msg.what, response).sendToTarget();
        }

        private boolean maybeRetryRequest(Message originalMsg) {
            boolean allowRetry = originalMsg.arg1 == 1;
            if (!allowRetry) {
                return false;
            }
            int errorCount = originalMsg.arg2 + 1;
            if (errorCount > initialDrmRequestRetryCount) {
                return false;
            }
            Message retryMsg = Message.obtain(originalMsg);
            retryMsg.arg2 = errorCount;
            sendMessageDelayed(retryMsg, getRetryDelayMillis(errorCount));
            return true;
        }

        private long getRetryDelayMillis(int errorCount) {
            return Math.min((errorCount - 1) * 1000, 5000);
        }

    }
}

final class ClearKeyUtil {

    private static final String TAG = "ClearKeyUtil";
    private static final Pattern REQUEST_KIDS_PATTERN = Pattern.compile("\"kids\":\\[\"(.*?)\"]");

    private ClearKeyUtil() {}

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    static byte[] adjustRequestData(byte[] request) {
        if (android.os.Build.VERSION.SDK_INT > 26) {
            return request;
        }

        String requestString = new String(request, Charset.forName("UTF-8"));
        Matcher requestKidsMatcher = REQUEST_KIDS_PATTERN.matcher(requestString);
        if (!requestKidsMatcher.find()) {
            NexLog.e(TAG, "Failed to adjust request data: " + requestString);
            return request;
        }
        int kidsStartIndex = requestKidsMatcher.start(1);
        int kidsEndIndex = requestKidsMatcher.end(1);
        StringBuilder adjustedRequestBuilder = new StringBuilder(requestString);
        base64ToBase64Url(adjustedRequestBuilder, kidsStartIndex, kidsEndIndex);
        String adjustedString = adjustedRequestBuilder.toString();
        return adjustedString.getBytes(Charset.forName("UTF-8"));
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    static byte[] adjustResponseData(byte[] response) {
        if (Build.VERSION.SDK_INT > 26) {
            return response;
        }

        try {
            String responseString = new String(response, Charset.forName("UTF-8"));
            JSONObject responseJson = new JSONObject(responseString);
            JSONArray keysArray = responseJson.getJSONArray("keys");
            for (int i = 0; i < keysArray.length(); i++) {
                JSONObject key = keysArray.getJSONObject(i);
                key.put("k", base64UrlToBase64(key.getString("k")));
                key.put("kid", base64UrlToBase64(key.getString("kid")));
            }

            String value = responseJson.toString();
            return value.getBytes(Charset.forName("UTF-8"));
        } catch (JSONException e) {
            Log.e(TAG, "Failed to adjust response data", e);
            return response;
        }
    }

    private static void base64ToBase64Url(StringBuilder base64, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            switch (base64.charAt(i)) {
                case '+':
                    base64.setCharAt(i, '-');
                    break;
                case '/':
                    base64.setCharAt(i, '_');
                    break;
                default:
                    break;
            }
        }
    }

    private static String base64UrlToBase64(String base64) {
        return base64.replace('-', '+').replace('_', '/');
    }

}

final class WidevineUtil {
    
    private static final String PROPERTY_LICENSE_DURATION_REMAINING = "LicenseDurationRemaining";
    private static final String PROPERTY_PLAYBACK_DURATION_REMAINING = "PlaybackDurationRemaining";

    private WidevineUtil() {}

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    static Pair<Long, Long> getLicenseDurationRemainingSec(NexMediaDrmSession drmSession) {
        Map<String, String> keyStatus = drmSession.queryKeyStatus();
        if (keyStatus == null) {
            return null;
        }
        return new Pair<Long, Long>(getDurationRemainingSec(keyStatus, PROPERTY_LICENSE_DURATION_REMAINING),
                getDurationRemainingSec(keyStatus, PROPERTY_PLAYBACK_DURATION_REMAINING));
    }

    private static long getDurationRemainingSec(Map<String, String> keyStatus, String property) {
        if (keyStatus != null) {
            try {
                String value = keyStatus.get(property);
                if (value != null) {
                    return Long.parseLong(value);
                }
            } catch (NumberFormatException e) {
                // do nothing.
            }
        }
        return TIME_UNSET;
    }

}
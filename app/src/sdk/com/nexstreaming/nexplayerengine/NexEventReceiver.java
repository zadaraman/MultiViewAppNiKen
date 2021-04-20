package com.nexstreaming.nexplayerengine;

import com.nexstreaming.nexplayerengine.NexPlayer.IDynamicThumbnailListener;
import com.nexstreaming.nexplayerengine.NexPlayer.IHTTPABRTrackChangeListener;
import com.nexstreaming.nexplayerengine.NexPlayer.IListener;
import com.nexstreaming.nexplayerengine.NexPlayer.IOfflineKeyListener;
import com.nexstreaming.nexplayerengine.NexPlayer.IVideoRendererListener;

import static com.nexstreaming.nexplayerengine.NexPlayer.NexErrorCode;



/**
 *
 * \brief This class implements all NexPlayer interfaces.
 *
 * An instance of \c NexEventReceiver can be used for parameter of NexPlayer.addEventRecevier, NexPlayer.removeEventReceiver
 *
 * \since version 6.51
 */
public class NexEventReceiver implements IListener, IDynamicThumbnailListener, NexPlayer.IMetaDataEventListener,
                                    IHTTPABRTrackChangeListener, IOfflineKeyListener, NexPlayer.IReleaseListener, IVideoRendererListener
{

    protected static Integer HAS_NO_EFFECT = 0xF000F000;

    @Override
    public void onEndOfContent(NexPlayer mp) {

    }

    @Override
    public void onStartVideoTask(NexPlayer mp) {

    }

    @Override
    public void onStartAudioTask(NexPlayer mp) {

    }

    @Override
    public void onTime(NexPlayer mp, int millisec) {

    }

    @Override
    public void onProgramTime(NexPlayer mp, String strTag, long offset) {

    }

    @Override
    public void onError(NexPlayer mp, NexErrorCode errorcode) {

    }

    @Override
    public void onSignalStatusChanged(NexPlayer mp, int pre, int now) {

    }

    @Override
    public void onStateChanged(NexPlayer mp, int pre, int now) {

    }

    @Override
    public void onRecordingErr(NexPlayer mp, int err) {

    }

    @Override
    public void onRecordingEnd(NexPlayer mp, int success) {

    }

    @Override
    public void onRecording(NexPlayer mp, int recDuration, int recSize) {

    }

    @Override
    public void onTimeshiftErr(NexPlayer mp, int err) {

    }

    @Override
    public void onTimeshift(NexPlayer mp, int currTime, int TotalTime) {

    }

    @Override
    public void onAsyncCmdComplete(NexPlayer mp, int command, int result, int param1, int param2) {

    }

    @Override
    public void onRTSPCommandTimeOut(NexPlayer mp) {

    }

    @Override
    public void onPauseSupervisionTimeOut(NexPlayer mp) {

    }

    @Override
    public void onDataInactivityTimeOut(NexPlayer mp) {

    }

	@Override
    public void onDataInactivityTimeOutWarning(NexPlayer mp) {

    }

    @Override
    public void onBufferingBegin(NexPlayer mp) {

    }

    @Override
    public void onBufferingEnd(NexPlayer mp) {

    }

    @Override
    public void onBuffering(NexPlayer mp, int progress_in_percent) {

    }

    @Override
    public void onAudioRenderPrepared(NexPlayer mp) {

    }

    @Override
    public void onAudioRenderCreate(NexPlayer mp, int samplingRate, int channelNum) {

    }

    @Override
    public void onAudioRenderDelete(NexPlayer mp) {

    }

    @Override
    public void onVideoRenderPrepared(NexPlayer mp) {

    }

    @Override
    public void onVideoRenderCreate(NexPlayer mp, int width, int height, Object rgbBuffer) {

    }

    @Override
    public void onVideoRenderDelete(NexPlayer mp) {

    }

    @Override
    public void onVideoRenderRender(NexPlayer mp) {

    }

    @Override
    public void onVideoRenderCapture(NexPlayer mp, int width, int height, int pixelbyte, Object bitmap) {

    }

    @Override
    public void onTextRenderInit(NexPlayer mp, int numTracks) {

    }

    @Override
    public void onTextRenderRender(NexPlayer mp, int trackIndex, NexClosedCaption textInfo) {

    }

    @Override
    public void onTimedMetaRenderRender(NexPlayer mp, NexID3TagInformation TimedMeta) {

    }

    @Override
    public void onStatusReport(NexPlayer mp, int msg, int param1) {

    }

    @Override
    public void onDownloaderError(NexPlayer mp, int msg, int param1) {

    }

    @Override
    public void onDownloaderAsyncCmdComplete(NexPlayer mp, int msg, int param1, int param2) {

    }

    @Override
    public void onDownloaderEventBegin(NexPlayer mp, int param1, int param2) {

    }

    @Override
    public void onDownloaderEventProgress(NexPlayer mp, int param1, int param2, long param3, long param4) {

    }

    @Override
    public void onDownloaderEventComplete(NexPlayer mp, int param1) {

    }

    @Override
    public void onDownloaderEventState(NexPlayer mp, int param1, int param2) {

    }

    @Override
    public void onSessionData(NexPlayer mp, NexSessionData[] data) {

    }

    @Override
    public void onDateRangeData(NexPlayer mp, NexDateRangeData[] data) {

    }

    @Override
    public void onEmsgData(NexPlayer mp, NexEmsgData data) {

    }

    @Override
    public void onHlsFirstProgramDateTime(NexPlayer mp, String str) {

    }

    @Override
    public void onDashScte35Event(NexPlayer mp, NexEmsgData[] data) {

    }

    @Override
    public void onPictureTimingInfo(NexPlayer mp, NexPictureTimingInfo[] arrPictureTimingInfo) {

    }

    @Override
    public void onHTTPResponse(NexPlayer mp, String strResponse) {

    }

    @Override
    public void onHTTPRequest(NexPlayer mp, String strRequest) {

    }

    @Override
    public String onModifyHttpRequest(NexPlayer mp, int param1, Object input_obj) {
        return null;
    }

    @Override
    public void onDynamicThumbnailData(NexPlayer mp, int width, int height, int cts, Object bitmap) {

    }

    @Override
    public void onDynamicThumbnailRecvEnd(NexPlayer mp) {

    }

    @Override
    public int onHTTPABRTrackChange(NexPlayer mp, int param1, int param2, int param3) {
        return HAS_NO_EFFECT;
    }

    @Override
    public void onOfflineKeyStoreListener(NexPlayer mp, byte[] keyId) {

    }

    @Override
    public byte[] onOfflineKeyRetrieveListener(NexPlayer mp) {
        return null;
    }

    @Override
    public void onOfflineKeyExpiredListener(NexPlayer mp) {

    }

    @Override
    public void onPlayerRelease(NexPlayer mp) {

    }

}

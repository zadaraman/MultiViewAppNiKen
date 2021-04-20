package com.nexstreaming.nexplayerengine;



import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.nexstreaming.nexplayerengine.NexPlayer.NexErrorCode;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.util.Map;

import static java.lang.Thread.sleep;


class NexMediaPlayer {
	private static final String LOG_TAG = "NexMediaPlayer";

	private NexPlayer mNexPlayer = null;
	private NexALFactory mNexALFactory = null;
	private Context mContext = null;

	private IListener mIListener = null;

	private boolean mIsLive = false;
	private int mOpenCommand = 0;
	private int mCurrentPosition = 0;

	private static final int BANDWIDTH_KBPS = 1024;

	protected NexMediaPlayer(Context context) {
		mContext = context;
		mNexPlayer = new NexPlayer();
		mNexALFactory = new NexALFactory();
		System.gc();
	}

	protected boolean init(int logLevel, int codecMode, String renderMode) {
		boolean result = true;

		int debugLogLevel = logLevel;
		if( debugLogLevel < 0 )
			debugLogLevel = 0xF0000000;

		//&&$$ start internaltest
		debugLogLevel = debugLogLevel + (codecMode << 4);
		//&&$$ end

		if( !mNexALFactory.init(mContext, android.os.Build.MODEL, renderMode, debugLogLevel, 1) ) {
			if( mIListener != null )
				mIListener.onError(mNexPlayer, NexErrorCode.PLAYER_ERROR_INIT);
			result = false;
		} else {
			mNexPlayer.setNexALFactory(mNexALFactory);

			if( NexErrorCode.NONE != mNexPlayer.init(mContext, logLevel) ) {
				if( mIListener != null )
					mIListener.onError(mNexPlayer, NexErrorCode.PLAYER_ERROR_INIT);
				result = false;
			} else {
				mNexPlayer.setListener(mPlayerListener);
				mNexPlayer.setDynamicThumbnailListener(mIDynamicThumbnailListener);
			}
		}
		return result;
	}

    protected int setDataSource(FileDescriptor fd, long offset, long length) {
        int result = NexErrorCode.INVALID_STATE.getIntegerCode();
        if( getState() == NexPlayer.NEXPLAYER_STATE_CLOSED ) {
            result = mNexPlayer.openFD(fd, offset, length);
        }

        return result;
    }

	protected void start(int startSec) {
		NexLog.d(LOG_TAG, "start startSec : " + startSec);
		int state = getState();
		int result = NexErrorCode.INVALID_STATE.getIntegerCode();
		int command = 0;
		if( state == NexPlayer.NEXPLAYER_STATE_STOP ) {
			result = mNexPlayer.start(startSec);
			command = (mOpenCommand == NexPlayer.NEXPLAYER_ASYNC_CMD_OPEN_LOCAL) ?
					NexPlayer.NEXPLAYER_ASYNC_CMD_START_LOCAL :
					NexPlayer.NEXPLAYER_ASYNC_CMD_START_STREAMING;
		} else if( state == NexPlayer.NEXPLAYER_STATE_PAUSE ) {
			result = mNexPlayer.resume();
			command = NexPlayer.NEXPLAYER_ASYNC_CMD_RESUME;
		}

		sendAsyncCmdIfError(command, result);
	}

	protected void stop() {
		NexLog.d(LOG_TAG, "stop");
		int result = NexErrorCode.INVALID_STATE.getIntegerCode();
		int state = getState();
		if( state >= NexPlayer.NEXPLAYER_STATE_STOP )
			result = mNexPlayer.stop();
		else if( state == NexPlayer.NEXPLAYER_STATE_CLOSED)
			result = mNexPlayer.close();

		sendAsyncCmdIfError(NexPlayer.NEXPLAYER_ASYNC_CMD_STOP, result);
	}

	protected MediaSeekableRange getSeekableRange() {
		long[] seekableRange = mNexPlayer.getSeekableRangeInfo();
		long startTime = 0;
		long endTime = 0;

		if( seekableRange != null ) {
			startTime = seekableRange[0];
			endTime = seekableRange[1];
		}

		return new MediaSeekableRange(startTime, endTime);
	}

	protected NexPlayer getPlayer() {
		return mNexPlayer;
	}

	protected void pause() {
		NexLog.d(LOG_TAG, "pause");
		int result = NexErrorCode.INVALID_STATE.getIntegerCode();
		if( getState() == NexPlayer.NEXPLAYER_STATE_PLAY )
			result = mNexPlayer.pause();

		sendAsyncCmdIfError(NexPlayer.NEXPLAYER_ASYNC_CMD_PAUSE, result);
	}

	protected void seekTo(int msec) {
		NexLog.d(LOG_TAG, "seekTo 1 msec : " + msec);
		int result = NexErrorCode.INVALID_STATE.getIntegerCode();
		if( getState() > NexPlayer.NEXPLAYER_STATE_STOP ) {
			MediaSeekableRange seekableRange = getSeekableRange();
			int min =  0;
			int max = mNexPlayer.getContentInfoInt(NexPlayer.CONTENT_INFO_INDEX_MEDIA_DURATION);

			if( mIsLive ) {
				max = (int)seekableRange.getEndTime() - (int)seekableRange.getStartTime();
				min = 0;
			}

			if( msec < min ) {
				msec = min;
			} else if( msec > max ) {
				msec = max;
			}

			mCurrentPosition = msec;
			if( mIsLive ) {
				msec += seekableRange.getStartTime();
			}
			NexLog.d(LOG_TAG, "seekTo 2 msec : " + msec);
			result =  mNexPlayer.seek(msec);
		}

		sendAsyncCmdIfError(NexPlayer.NEXPLAYER_ASYNC_CMD_SEEK, result);
	}

	protected void resume() {
		NexLog.d(LOG_TAG, "resume");
		int result = NexErrorCode.INVALID_STATE.getIntegerCode();
		if( getState() == NexPlayer.NEXPLAYER_STATE_PAUSE )
			result = mNexPlayer.resume();

		sendAsyncCmdIfError(NexPlayer.NEXPLAYER_ASYNC_CMD_RESUME, result);
	}

	private void sendAsyncCmdIfError(int cmd, int result) {
		if( mIListener != null && result != 0 )
			mIListener.onAsyncCmdComplete(mNexPlayer, cmd, result, 0, 0);
	}

	protected void release() {
		NexLog.d(LOG_TAG, "release");
		stop();
		while( getState() > NexPlayer.NEXPLAYER_STATE_STOP ) {
			try {
				sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		close();
		mNexPlayer.release();
		mNexALFactory.release();
		mNexPlayer = null;
		mNexALFactory = null;
	}

	protected void close() {
		NexLog.d(LOG_TAG, "close");
		int state = getState();
		if( mNexPlayer.isInitialized() )
			mNexPlayer.close();
	}

	protected void addHTTPHeaderFields(Map<String, String> headers) {
		if( headers != null && !headers.isEmpty() ) {
			String[] keys = new String[headers.size()];
			String[] values = new String[headers.size()];

			int i = 0;
			for ( Map.Entry<String, String> entry: headers.entrySet() ) {
				keys[i] = entry.getKey();
				values[i] = entry.getValue();
				mNexPlayer.addHTTPHeaderFields(keys[i] + ":" + values[i]);
				++i;
			}
		}
	}

	protected int getState() {
		int state = NexPlayer.NEXPLAYER_STATE_NONE;
		if( mNexPlayer != null && mNexPlayer.isInitialized() )
			state = mNexPlayer.getState();
		return state;
	}

	private int turnTextOff(NexContentInformation info, boolean isCEA608) {
		int ret;

		if( isCEA608 ) {
			ret = mNexPlayer.setCEA608CaptionChannel(0);
		} else {
			ret = mNexPlayer.setMediaStream(
					info.mCurrAudioStreamID == NexPlayer.MEDIA_STREAM_DISABLE_ID ? NexPlayer.MEDIA_STREAM_DISABLE_ID : NexPlayer.MEDIA_STREAM_DEFAULT_ID,
					NexPlayer.MEDIA_STREAM_DISABLE_ID,
					info.mCurrVideoStreamID == NexPlayer.MEDIA_STREAM_DISABLE_ID ? NexPlayer.MEDIA_STREAM_DISABLE_ID : NexPlayer.MEDIA_STREAM_DEFAULT_ID,
					NexPlayer.MEDIA_STREAM_DEFAULT_ID);
		}

		return ret;
	}

	private int turnTextOn(NexContentInformation info, boolean isCEA608) {
		int ret;

		if( isCEA608 ) {
			ret = mNexPlayer.setCEA608CaptionChannel(1);
		} else {
			ret = mNexPlayer.setMediaStream(
					info.mCurrAudioStreamID == NexPlayer.MEDIA_STREAM_DISABLE_ID ? NexPlayer.MEDIA_STREAM_DISABLE_ID : NexPlayer.MEDIA_STREAM_DEFAULT_ID,
					NexPlayer.MEDIA_STREAM_DEFAULT_ID,
					info.mCurrVideoStreamID == NexPlayer.MEDIA_STREAM_DISABLE_ID ? NexPlayer.MEDIA_STREAM_DISABLE_ID : NexPlayer.MEDIA_STREAM_DEFAULT_ID,
					NexPlayer.MEDIA_STREAM_DEFAULT_ID);
		}

		return ret;
	}

	private int changeTextStream(NexContentInformation info, boolean isCEA608, int streamID) {
		int ret;

		if( isCEA608 ) {
			ret = mNexPlayer.setCEA608CaptionChannel(streamID);
		} else {
			ret = mNexPlayer.setMediaStream(
					info.mCurrAudioStreamID == NexPlayer.MEDIA_STREAM_DISABLE_ID ? NexPlayer.MEDIA_STREAM_DISABLE_ID : NexPlayer.MEDIA_STREAM_DEFAULT_ID,
					streamID,
					info.mCurrVideoStreamID == NexPlayer.MEDIA_STREAM_DISABLE_ID ? NexPlayer.MEDIA_STREAM_DISABLE_ID : NexPlayer.MEDIA_STREAM_DEFAULT_ID,
					NexPlayer.MEDIA_STREAM_DEFAULT_ID);
		}

		return ret;
	}

	private int turnAudioOn(NexContentInformation info) {
		return mNexPlayer.setMediaStream(
				NexPlayer.MEDIA_STREAM_DEFAULT_ID,
				info.mCurrTextStreamID == NexPlayer.MEDIA_STREAM_DISABLE_ID ? NexPlayer.MEDIA_STREAM_DISABLE_ID : NexPlayer.MEDIA_STREAM_DEFAULT_ID,
				NexPlayer.MEDIA_STREAM_DEFAULT_ID,
				NexPlayer.MEDIA_STREAM_DEFAULT_ID);
	}

	private int turnAudioOff(NexContentInformation info) {
		return mNexPlayer.setMediaStream(
				NexPlayer.MEDIA_STREAM_DISABLE_ID,
				info.mCurrTextStreamID == NexPlayer.MEDIA_STREAM_DISABLE_ID ? NexPlayer.MEDIA_STREAM_DISABLE_ID : NexPlayer.MEDIA_STREAM_DEFAULT_ID,
				NexPlayer.MEDIA_STREAM_DEFAULT_ID,
				NexPlayer.MEDIA_STREAM_DEFAULT_ID);
	}

	private int changeAudioStream(NexContentInformation info, int streamID) {
		return mNexPlayer.setMediaStream(
				streamID,
				info.mCurrTextStreamID == NexPlayer.MEDIA_STREAM_DISABLE_ID ? NexPlayer.MEDIA_STREAM_DISABLE_ID : NexPlayer.MEDIA_STREAM_DEFAULT_ID,
				info.mCurrVideoStreamID == NexPlayer.MEDIA_STREAM_DISABLE_ID ? NexPlayer.MEDIA_STREAM_DISABLE_ID : NexPlayer.MEDIA_STREAM_DEFAULT_ID,
				NexPlayer.MEDIA_STREAM_DEFAULT_ID);
	}

	private int turnVideoOn(NexContentInformation info) {
		return mNexPlayer.setMediaStream(
				info.mCurrAudioStreamID == NexPlayer.MEDIA_STREAM_DISABLE_ID ? NexPlayer.MEDIA_STREAM_DISABLE_ID : NexPlayer.MEDIA_STREAM_DEFAULT_ID,
				info.mCurrTextStreamID == NexPlayer.MEDIA_STREAM_DISABLE_ID ? NexPlayer.MEDIA_STREAM_DISABLE_ID : NexPlayer.MEDIA_STREAM_DEFAULT_ID,
				NexPlayer.MEDIA_STREAM_DEFAULT_ID,
				NexPlayer.MEDIA_STREAM_DEFAULT_ID);
	}

	private int turnVideoOff(NexContentInformation info) {
		return mNexPlayer.setMediaStream(
				NexPlayer.MEDIA_STREAM_DEFAULT_ID,
				info.mCurrTextStreamID == NexPlayer.MEDIA_STREAM_DISABLE_ID ? NexPlayer.MEDIA_STREAM_DISABLE_ID : NexPlayer.MEDIA_STREAM_DEFAULT_ID,
				NexPlayer.MEDIA_STREAM_DISABLE_ID,
				NexPlayer.MEDIA_STREAM_DEFAULT_ID);
	}

	private int changeVideoStream(NexContentInformation info, int streamID, int customAttrID) {
		return mNexPlayer.setMediaStream(
				info.mCurrAudioStreamID == NexPlayer.MEDIA_STREAM_DISABLE_ID ? NexPlayer.MEDIA_STREAM_DISABLE_ID : NexPlayer.MEDIA_STREAM_DEFAULT_ID,
				info.mCurrTextStreamID == NexPlayer.MEDIA_STREAM_DISABLE_ID ? NexPlayer.MEDIA_STREAM_DISABLE_ID : NexPlayer.MEDIA_STREAM_DEFAULT_ID,
				streamID,
				customAttrID);
	}

	protected int setMediaStream(int streamType, boolean isCEA608, int streamID, int customAttrID) {
		NexLog.d(LOG_TAG, "setMediaStream streamType : " + streamType + " isCEA608 : " + isCEA608 + " streamID : " + streamID + " customAttrID : " + customAttrID);
		int ret = -1;
		int state = getState();
		if( state == NexPlayer.NEXPLAYER_STATE_PLAY ||
				state == NexPlayer.NEXPLAYER_STATE_PAUSE ||
                state == NexPlayer.NEXPLAYER_STATE_STOP ) {
			NexContentInformation info = mNexPlayer.getContentInfo();

			if( info != null ) {
				switch (streamType) {
					case NexVideoView.STREAM_TYPE_TEXT:
						if( streamID == NexPlayer.MEDIA_STREAM_DISABLE_ID )
							ret = turnTextOff(info, isCEA608);
						else if( streamID == NexPlayer.MEDIA_STREAM_DEFAULT_ID )
							ret = turnTextOn(info, isCEA608);
						else
							ret = changeTextStream(info, isCEA608, streamID);
						break;
					case NexVideoView.STREAM_TYPE_AUDIO:
						if( streamID == NexPlayer.MEDIA_STREAM_DISABLE_ID )
							ret = turnAudioOff(info);
						else if( streamID == NexPlayer.MEDIA_STREAM_DEFAULT_ID )
							ret = turnAudioOn(info);
						else
							ret = changeAudioStream(info, streamID);
						break;
					case NexVideoView.STREAM_TYPE_VIDEO:
						if( streamID == NexPlayer.MEDIA_STREAM_DISABLE_ID )
							ret = turnVideoOff(info);
						else if( streamID == NexPlayer.MEDIA_STREAM_DEFAULT_ID )
							ret = turnVideoOn(info);
						else
							ret = changeVideoStream(info, streamID, customAttrID);
						break;
				}
			}
		}

		return ret;
	}

	private static final int AUDIO_ONLY = 1;
	private static final int VIDEO_ONLY = 2;
	private static final int AUDIO_VIDEO = 3;

	protected boolean isPlaying() {
		boolean ret = false;
		if( getState() == NexPlayer.NEXPLAYER_STATE_PLAY )
			ret = true;
		return ret;
	}

	protected int getBufferPercentage() {
		int ret = 0;
		NexContentInformation info;

		if( mNexPlayer != null && (info = mNexPlayer.getContentInfo()) != null ) {
			int streamType = info.mMediaType == VIDEO_ONLY ? NexPlayer.MEDIA_STREAM_TYPE_VIDEO : NexPlayer.MEDIA_STREAM_TYPE_AUDIO;
			float bufferInfo = mNexPlayer.getBufferInfo(streamType, NexPlayer.NEXPLAYER_BUFINFO_INDEX_LASTCTS);
			int duration = getDuration();
			ret = (int)(bufferInfo / duration * 100);
		}

		NexLog.d(LOG_TAG, "getBufferPercentage ret : " + ret);
		return ret;
	}

	protected int getCurrentPosition() {
		if(mNexPlayer != null) {
			return mNexPlayer.getCurrentPosition();
		} else {
			return mCurrentPosition;
		}
	}

	protected int getDuration() {
		int ret = 0;
		if( mNexPlayer != null && mNexPlayer.isInitialized() && getState() >= NexPlayer.NEXPLAYER_STATE_STOP ) {
			ret = mNexPlayer.getContentInfoInt(NexPlayer.CONTENT_INFO_INDEX_MEDIA_DURATION);
			if( mIsLive ) {
				MediaSeekableRange seekableRange = getSeekableRange();
				ret = (int)(seekableRange.getEndTime() - seekableRange.getStartTime());
			}
		}

		return ret;
	}

	protected boolean canPause() {
		boolean ret = false;

		if( mNexPlayer != null && mNexPlayer.getContentInfo() != null ) {
			ret = mNexPlayer.getContentInfo().mIsPausable == 1;
		}

		return ret;
	}

	protected boolean canSeekBackward() {
		boolean ret = false;

		if( mNexPlayer != null && mNexPlayer.getContentInfo() != null ) {
			ret = mNexPlayer.getContentInfo().mIsSeekable > 0;
		}

		return ret;
	}

	protected boolean canSeekForward() {
		boolean ret = false;

		if( mNexPlayer != null && mNexPlayer.getContentInfo() != null ) {
			ret = mNexPlayer.getContentInfo().mIsSeekable > 0;
		}

		return ret;
	}

	private boolean checkValidBandWidthForFastPlay(int bandwidth, int minBW, int maxBW) {
		return (minBW == 0 && maxBW == 0) || (bandwidth > (minBW * BANDWIDTH_KBPS) && bandwidth < (maxBW * BANDWIDTH_KBPS));
	}

	private boolean isFastPlayPossible(NexContentInformation info, int minBW, int maxBW) {
		boolean result = false;

		if( info != null &&	info.mCurrVideoStreamID != NexPlayer.MEDIA_STREAM_DISABLE_ID ) {
			for( int i = 0; i < info.mStreamNum; i++ ) {
				for (int j = 0; j < info.mArrStreamInformation[i].mTrackCount; j++) {
					if( checkValidBandWidthForFastPlay(info.mArrStreamInformation[i].mArrTrackInformation[j].mBandWidth, minBW, maxBW) ) {
						if( info.mArrStreamInformation[i].mArrTrackInformation[j].mIFrameTrack ) {
							result = true;
							break;
						}
					}
				}
			}
		}

		return result;
	}

	protected int fastPlayStart(int msec, float rate) {
		int ret = -1;

		if( getState() == NexPlayer.NEXPLAYER_STATE_PLAY ) {
			int minBW = mNexPlayer.getProperty(NexPlayer.NexProperty.MIN_BW);
			int maxBW = mNexPlayer.getProperty(NexPlayer.NexProperty.MAX_BW);
			NexLog.d(LOG_TAG, "fastPlayStart minBW : " + minBW + " maxBW : " + maxBW);

			if( isFastPlayPossible(mNexPlayer.getContentInfo(), minBW, maxBW) )
				ret = mNexPlayer.fastPlayStart(msec, rate);
		}

		return ret;
	}

	protected int fastPlayStop(boolean bResume) {
		int ret = -1;
		if( getState() == NexPlayer.NEXPLAYER_STATE_PLAYxN ) {
			ret = mNexPlayer.fastPlayStop(bResume);
		}
		return ret;
	}

	protected int fastPlaySetPlaybackRate(float rate) {
		int ret = -1;
		if( getState() == NexPlayer.NEXPLAYER_STATE_PLAYxN ) {
			ret = mNexPlayer.fastPlaySetPlaybackRate(rate);
		}
		return ret;
	}

	private NexPlayer.IDynamicThumbnailListener mIDynamicThumbnailListener = new NexPlayer.IDynamicThumbnailListener() {
		@Override
		public void onDynamicThumbnailData(NexPlayer mp, int width, int height, int cts, Object bitmap) {
			if( mIListener != null) {
				Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
				ByteBuffer buffer = (ByteBuffer) bitmap;
				bm.copyPixelsFromBuffer(buffer.asIntBuffer());

				mIListener.onDynamicThumbnailData(cts, bm);
			}
		}

		@Override
		public void onDynamicThumbnailRecvEnd(NexPlayer mp) {
			if( mIListener != null) {
				mIListener.onDynamicThumbnailRecvEnd();
			}
		}
	};

	private NexPlayer.IListener mPlayerListener = new NexPlayer.IListener() {
		@Override
		public void onAsyncCmdComplete(NexPlayer mp, int command, int result, int param1, int param2) {
			NexLog.d(LOG_TAG, "onAsyncCmdComplete command : " + command + " result : " + result + " param1 : " + param1 + " param2 : " + param2);
			switch ( command ) {
				case NexPlayer.NEXPLAYER_ASYNC_CMD_OPEN_LOCAL:
				case NexPlayer.NEXPLAYER_ASYNC_CMD_OPEN_STREAMING:
					if ( result == 0 ) {
						mIsLive =  mNexPlayer.getContentInfoInt(NexPlayer.CONTENT_INFO_INDEX_MEDIA_DURATION) < 0;
						mOpenCommand = command;
					}
					break;
				case NexPlayer.NEXPLAYER_ASYNC_CMD_STOP:
					mCurrentPosition = 0;
					break;
			}

			if ( mIListener != null )
				mIListener.onAsyncCmdComplete(mp, command, result, param1, param2);
		}

		@Override
		public void onRTSPCommandTimeOut(NexPlayer mp) {

		}

		@Override
		public void onPauseSupervisionTimeOut(NexPlayer mp) {

		}

		@Override
		public void onDataInactivityTimeOut(NexPlayer mp) {
			NexLog.d( "NexMediaPlayer", "onDataInactivityTimeOut");
			onError(mp, NexErrorCode.DATA_INACTIVITY_TIMEOUT);
		}

		@Override
		public void onDataInactivityTimeOutWarning(NexPlayer mp) {
			NexLog.d( "NexMediaPlayer", "onDataInactivityTimeOutWarning");
			//onError(mp, NexErrorCode.DATA_INACTIVITY_TIMEOUT_WARNING);
		}

		@Override
		public void onEndOfContent(NexPlayer mp) {
			if( mIListener != null )
				mIListener.onEndOfContent(mp);
		}

		@Override
		public void onStartVideoTask(NexPlayer mp) {

		}

		@Override
		public void onStartAudioTask(NexPlayer mp) {

		}

		@Override
		public void onTextRenderRender(NexPlayer mp, int trackIndex, NexClosedCaption textInfo) {
			if( mIListener != null )
				mIListener.onTextRenderRender(mp, trackIndex, textInfo);
		}

		@Override
		public void onTimedMetaRenderRender(NexPlayer mp, NexID3TagInformation TimedMeta) {
			if( mIListener != null )
				mIListener.onTimedMetaRenderRender(mp, TimedMeta);
		}

		@Override
		public void onStatusReport(NexPlayer mp, int msg, int param1) {
			if( mIListener != null )
				mIListener.onStatusReport(mp, msg, param1);
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
		public void onDateRangeData(NexPlayer mp , NexDateRangeData[] data) {

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
		public void onError(NexPlayer mp, NexErrorCode errorCode) {
			if( mIListener != null ) {
				mIListener.onError(mp, errorCode);
			}
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
		public void onBuffering(NexPlayer mp, int progress_in_percent) {
			if( mIListener != null )
				mIListener.onBuffering(mp, progress_in_percent);
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
		public void onBufferingBegin(NexPlayer mp) {
			if( mIListener != null )
				mIListener.onBufferingBegin(mp);
		}

		@Override
		public void onBufferingEnd(NexPlayer mp) {
			if( mIListener != null )
				mIListener.onBufferingEnd(mp);
		}

		@Override
		public void onTime(NexPlayer mp, int sec) {
			if( mIsLive ) {
				MediaSeekableRange seekableRange = getSeekableRange();
				sec = Math.max(sec - (int)seekableRange.getStartTime(), 0);
			}
			mCurrentPosition = sec;

			if(mIListener != null) {
				mIListener.onTime(mp, sec);
			}
			NexLog.d(LOG_TAG, "onTime mCurrentPosition : " + mCurrentPosition);
		}

		@Override
		public void onProgramTime(NexPlayer mp, String strTag, long offset) {

		}
	};

	protected void setListener(IListener l) {
		mIListener = l;
	}

	protected interface IListener {
		void onAsyncCmdComplete(NexPlayer mp, int command, int result, int param1, int param2);
		void onBufferingBegin(NexPlayer mp);
		void onBuffering(NexPlayer mp, int progress_in_percent);
		void onBufferingEnd(NexPlayer mp);
		void onStatusReport(NexPlayer mp, int msg, int param1);
		void onError(NexPlayer mp, NexErrorCode errorCode);
		void onTextRenderRender(NexPlayer mp, int trackIndex, NexClosedCaption textInfo);
		void onTimedMetaRenderRender(NexPlayer mp, NexID3TagInformation TimedMeta);
		void onDynamicThumbnailData(int cts, Bitmap bitmap);
		void onDynamicThumbnailRecvEnd();
		void onEndOfContent(NexPlayer mp);
		void onTime(NexPlayer mp, int currTime);
	}

	protected class MediaSeekableRange {
		private long mStartTime = 0;
		private long mEndTime = 0;
		public MediaSeekableRange(long startTime, long endTime) {
			mStartTime = startTime;
			mEndTime = endTime;
		}

		public long getStartTime() {
			return mStartTime;
		}

		public long getEndTime() {
			return mEndTime;
		}
	}
}

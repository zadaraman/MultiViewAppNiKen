package com.nexstreaming.nexplayerengine;

import com.nexstreaming.nexplayerengine.NexPlayer.IListener;
import com.nexstreaming.nexplayerengine.NexPlayer.NexErrorCode;

import java.util.concurrent.CopyOnWriteArrayList;

class NexEventForwarder {

	private CopyOnWriteArrayList mEventReceivers;
	private NexEventProxy mEventProxy;

	protected NexEventForwarder() {
		mEventReceivers = new CopyOnWriteArrayList<Object>();
		mEventProxy = new NexEventProxy();
	}

	protected boolean hasInterface(Class<?> cls) {
		for (Object receiver : mEventReceivers) {
			if (receiver != null) {
				if( cls == IListener.class ) {
					if(receiver instanceof IListener) {
						return true;
					}
				}
				else if( cls == NexPlayer.IOfflineKeyListener.class ) {
					if(receiver instanceof  NexPlayer.IOfflineKeyListener) {
						return true;
					}
				}
				else if( cls == NexPlayer.IDynamicThumbnailListener.class ) {
					if(receiver instanceof NexPlayer.IDynamicThumbnailListener) {
						return true;
					}
				}
				else if( cls == NexPlayer.IHTTPABRTrackChangeListener.class ) {
					if(receiver instanceof NexPlayer.IHTTPABRTrackChangeListener) {
						return true;
					}
				}
				else if( cls == NexPlayer.IReleaseListener.class ) {
					if(receiver instanceof NexPlayer.IReleaseListener) {
						return true;
					}
				}
			}
		}
		return false;
	}

	protected boolean addReceiver(Object receiver) {
		return mEventReceivers.add( receiver );
	}

	synchronized protected boolean removeReceiver(Object receiver) {
		if( mEventReceivers != null ) {
			for (Object obj : mEventReceivers) {
				if (obj.equals(receiver) ) {
					return mEventReceivers.remove(obj);
				}
			}
		}
		return false;
	}

	protected NexEventProxy getEventProxy() {
		return mEventProxy;
	}


	protected Object handleEvent(NexPlayer nexplayer, IListener listener, NexPlayerEvent event) {





		Object ret = handleEvent(nexplayer, event);
		if( listener != null ) {
			Object tmp = handleIListenerEvent(nexplayer, listener, event);
			if(tmp != null) {
				ret = tmp;
			}
		}
		mEventProxy.handleEvent(nexplayer, event);

		return ret;
	}

	synchronized protected Object handleEvent(NexPlayer nexplayer, NexPlayerEvent event) {

		Object ret = null;

		if( mEventReceivers != null ) {
			for (Object receiver : mEventReceivers) {
				if ( receiver != null ) {
					Object tmp = null;
					switch(event.what) {
						case NexPlayerEvent.NEXPLAYER_OFFLINE_STORE_KEY :
							if(receiver instanceof NexPlayer.IOfflineKeyListener) {
								((NexPlayer.IOfflineKeyListener)receiver).onOfflineKeyStoreListener(nexplayer, (byte[]) event.obj);
							}
							break;
						case NexPlayerEvent.NEXPLAYER_OFFLINE_RETREIVE_KEY :
							if(receiver instanceof NexPlayer.IOfflineKeyListener) {
								tmp = ((NexPlayer.IOfflineKeyListener)receiver).onOfflineKeyRetrieveListener(nexplayer);
							}
							break;
						case NexPlayerEvent.NEXPLAYER_OFFLINE_KEY_EXPIRED:
							if(receiver instanceof  NexPlayer.IOfflineKeyListener) {
								((NexPlayer.IOfflineKeyListener)receiver).onOfflineKeyExpiredListener(nexplayer);
							}
							break;
						case NexPlayerEvent.NEXPLAYER_EVENT_THUMBNAIL_REPORT :
							if(receiver instanceof NexPlayer.IDynamicThumbnailListener) {
								((NexPlayer.IDynamicThumbnailListener)receiver).onDynamicThumbnailData(nexplayer, event.intArgs[0], event.intArgs[1], event.intArgs[2], event.obj);
							}
							break;
						case NexPlayerEvent.NEXPLAYER_EVENT_THUMBNAIL_REPORT_END :
							if(receiver instanceof NexPlayer.IDynamicThumbnailListener) {
								((NexPlayer.IDynamicThumbnailListener)receiver).onDynamicThumbnailRecvEnd(nexplayer);
							}
							break;
						case NexPlayerEvent.NEXPLAYER_CALLBACK_HTTP_ABR_TRACKCHANGE :
							if(receiver instanceof NexPlayer.IHTTPABRTrackChangeListener) {
								tmp = ((NexPlayer.IHTTPABRTrackChangeListener)receiver).onHTTPABRTrackChange(nexplayer, event.intArgs[0], event.intArgs[1], event.intArgs[2]);
								if( tmp == NexEventReceiver.HAS_NO_EFFECT ) {
									tmp = (ret == null) ? event.intArgs[2] : null;
								}
							}
							break;
						case NexPlayerEvent.NEXPLAYER_EVENT_METADATA :
							if (event.intArgs[0] == NexPlayer.NEXPLAYER_METADATA_EMSG) {
								if (receiver instanceof NexPlayer.IMetaDataEventListener) {
									((NexPlayer.IMetaDataEventListener) receiver).onEmsgData(nexplayer, (NexEmsgData) event.obj);
								}
							}
							else if (event.intArgs[0] == NexPlayer.NEXPLAYER_METADATA_HLS_FIRST_PROGRAM_DATE_TIME) {
								if (receiver instanceof NexPlayer.IMetaDataEventListener) {
									((NexPlayer.IMetaDataEventListener) receiver).onHlsFirstProgramDateTime(nexplayer, (String) event.obj);
								}
							}
							else if (event.intArgs[0] == NexPlayer.NEXPLAYER_METADATA_DASH_SCTE35) {
								if (receiver instanceof NexPlayer.IMetaDataEventListener) {
									((NexPlayer.IMetaDataEventListener) receiver).onDashScte35Event(nexplayer, (NexEmsgData[]) event.obj);
								}
							}
							break;
						case NexPlayerEvent.NEXPLAYER_EVENT_WILL_RELEASE :
							if(receiver instanceof NexPlayer.IReleaseListener) {
								((NexPlayer.IReleaseListener)receiver).onPlayerRelease(nexplayer);
							}
						case NexPlayerEvent.NEXPLAYER_EVENT_WILL_INIT:
						case NexPlayerEvent.NEXPLAYER_EVENT_WILL_OPEN:
						case NexPlayerEvent.NEXPLAYER_EVENT_WILL_START:
						case NexPlayerEvent.NEXPLAYER_EVENT_WILL_RESUME:
						case NexPlayerEvent.NEXPLAYER_EVENT_WILL_SEEK:
						case NexPlayerEvent.NEXPLAYER_EVENT_WILL_PAUSE:
						case NexPlayerEvent.NEXPLAYER_EVENT_WILL_STOP:
						case NexPlayerEvent.NEXPLAYER_EVENT_WILL_CLOSE:
						case NexPlayerEvent.NEXPLAYER_EVENT_ONHTTPSTATS:
							mEventProxy.handleEvent(nexplayer, event);
							break;
						default:
							if(receiver instanceof IListener) {
								tmp = handleIListenerEvent(nexplayer, (IListener)receiver, event);
							}
							break;
					}
					if(tmp != null) {
						ret = tmp;
					}
				}
			}
		}
		return ret;
	}

	synchronized private Object handleIListenerEvent(NexPlayer nexplayer, IListener listener, NexPlayerEvent event) {
		Object ret = null;
		
		if(listener != null) {
			switch (event.what) {

				case NexPlayerEvent.NEXPLAYER_EVENT_NOP:
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_ENDOFCONTENT:
					listener.onEndOfContent(nexplayer);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_STARTVIDEOTASK:
					listener.onStartVideoTask(nexplayer);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_STARTAUDIOTASK:
					listener.onStartAudioTask(nexplayer);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_TIME:
					listener.onTime(nexplayer, event.intArgs[0]);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_PROGRAMTIME:
					listener.onProgramTime(nexplayer, (String) event.obj, event.longArgs[0]);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_ERROR:
					NexErrorCode errorCode = NexErrorCode.fromIntegerValue(event.intArgs[0]);

					if (null != event.obj) {
						errorCode.setSubErrorInfo(event.intArgs[1], (String)event.obj);
					}

					listener.onError(nexplayer, errorCode);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_STATECHANGED:
					listener.onStateChanged(nexplayer, event.intArgs[0], event.intArgs[1]);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_SIGNALSTATUSCHANGED:
					listener.onSignalStatusChanged(nexplayer, event.intArgs[0], event.intArgs[1]);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_ASYNC_CMD_COMPLETE:
					listener.onAsyncCmdComplete(nexplayer, event.intArgs[0], event.intArgs[1], event.intArgs[2], event.intArgs[3]);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_RTSP_COMMAND_TIMEOUT:
					listener.onRTSPCommandTimeOut(nexplayer);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_PAUSE_SUPERVISION_TIMEOUT:
					listener.onPauseSupervisionTimeOut(nexplayer);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_DATA_INACTIVITY_TIMEOUT:
					// Log.d("NexEventForwader", "NEXPLAYER_EVENT_DATA_INACTIVITY_TIMEOUT");
					listener.onDataInactivityTimeOut(nexplayer);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_DATA_INACTIVITY_TIMEOUT_WARNING:
					// Log.d("NexEventForwader", "NEXPLAYER_EVENT_DATA_INACTIVITY_TIMEOUT_WARNING");
					listener.onDataInactivityTimeOutWarning(nexplayer);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_RECORDING_ERROR:
					listener.onRecordingErr(nexplayer, event.intArgs[0]);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_RECORDING:
					listener.onRecording(nexplayer, event.intArgs[0], event.intArgs[1]);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_RECORDEND:
					listener.onRecordingEnd(nexplayer, event.intArgs[0]);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_TIMESHIFT_ERROR:
					listener.onTimeshiftErr(nexplayer, event.intArgs[0]);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_TIMESHIFT:
					listener.onTimeshift(nexplayer, event.intArgs[0], event.intArgs[1]);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_BUFFERINGBEGIN:
					listener.onBufferingBegin(nexplayer);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_BUFFERINGEND:
					listener.onBufferingEnd(nexplayer);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_BUFFERING:
					listener.onBuffering(nexplayer, event.intArgs[0]);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_AUDIO_RENDER_PREPARED:
					listener.onAudioRenderPrepared(nexplayer);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_AUDIO_RENDER_CREATE:
					listener.onAudioRenderCreate(nexplayer, event.intArgs[0], event.intArgs[1]);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_AUDIO_RENDER_DELETE:
					listener.onAudioRenderDelete(nexplayer);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_VIDEO_RENDER_PREPARED:
					listener.onVideoRenderPrepared(nexplayer);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_VIDEO_RENDER_CREATE:
					listener.onVideoRenderCreate(nexplayer, event.intArgs[0], event.intArgs[1], event.obj);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_VIDEO_RENDER_DELETE:
					listener.onVideoRenderDelete(nexplayer);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_VIDEO_RENDER_CAPTURE:
					listener.onVideoRenderCapture(nexplayer, event.intArgs[0], event.intArgs[1], event.intArgs[2], event.obj);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_VIDEO_RENDER_RENDER:
					listener.onVideoRenderRender(nexplayer);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_TEXT_RENDER_INIT:
					listener.onTextRenderInit(nexplayer, event.intArgs[0]);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_TEXT_RENDER_RENDER:
					listener.onTextRenderRender(nexplayer, event.intArgs[1], (NexClosedCaption) event.obj);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_TIMEDMETA_RENDER_RENDER:
					listener.onTimedMetaRenderRender(nexplayer, (NexID3TagInformation) event.obj);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_STATUS_REPORT:
					listener.onStatusReport(nexplayer, event.intArgs[0], event.intArgs[1]);
					break;

				case NexPlayerEvent.NEXPLAYER_EVENT_DEBUGINFO:
					switch (event.intArgs[0]) {
						case NexPlayer.NEXPLAYER_DEBUGINFO_H264_SEI_PICTIMING_INFO:
							listener.onPictureTimingInfo(nexplayer, (NexPictureTimingInfo[]) event.obj);
							break;

						case NexPlayer.NEXPLAYER_DEBUGINFO_HTTP_RESPONSE:
							listener.onHTTPResponse(nexplayer, (String) event.obj);
							break;

						case NexPlayer.NEXPLAYER_DEBUGINFO_HTTP_REQUEST:
							listener.onHTTPRequest(nexplayer, (String) event.obj);
							break;
						case NexPlayer.NEXPLAYER_DEBUGINFO_SESSION_DATA:
							listener.onSessionData(nexplayer, (NexSessionData[]) event.obj);
							break;
						case NexPlayer.NEXPLAYER_DEBUGINFO_DATERAGNE_DATA:
							listener.onDateRangeData(nexplayer,(NexDateRangeData[])  event.obj);
							break;
						default:
							break;
					}
					break;
				case NexPlayerEvent.NEXDOWNLOADER_EVENT_ERROR:
					listener.onDownloaderError(nexplayer, event.intArgs[0], event.intArgs[1]);
					break;

				case NexPlayerEvent.NEXDOWNLOADER_EVENT_ASYNC_CMD_BASEID:
					listener.onDownloaderAsyncCmdComplete(nexplayer, event.intArgs[0], event.intArgs[1], event.intArgs[2]);
					break;

				case NexPlayerEvent.NEXDOWNLOADER_EVENT_COMMON_DOWNLOAD_BEGIN:
					listener.onDownloaderEventBegin(nexplayer, event.intArgs[1], event.intArgs[2]);
					break;

				case NexPlayerEvent.NEXDOWNLOADER_EVENT_COMMON_DOWNLOAD_PROGRESS:
					listener.onDownloaderEventProgress(nexplayer, event.intArgs[1], event.intArgs[2], event.longArgs[0], event.longArgs[1]);
					break;

				case NexPlayerEvent.NEXDOWNLOADER_EVENT_COMMON_DOWNLOAD_COMPLETE:
					listener.onDownloaderEventComplete(nexplayer, event.intArgs[0]);
					break;

				case NexPlayerEvent.NEXDOWNLOADER_EVENT_COMMON_STATE_CHANGED:
					listener.onDownloaderEventState(nexplayer, event.intArgs[0], event.intArgs[1]);
					break;

				case NexPlayerEvent.NEXPLAYER_SUPPORT_MODIFY_HTTP_REQUEST:
					ret = listener.onModifyHttpRequest(nexplayer, event.intArgs[0], event.obj);
					break;
			}

		}

		return ret;
	}
}

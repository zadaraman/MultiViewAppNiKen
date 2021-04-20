package com.nexstreaming.nexplayerengine;

import android.content.Context;

import com.nexstreaming.nexplayerengine.NexPlayer.NexErrorCode;

import java.util.ArrayList;

/**
 * \For internal use only. Please do not use.
 */
public class NexClientManager {

	protected final static int		INVALID_PARAMETER = -1;

	private ArrayList<NexClient>	mClientList;
	private boolean					mForwardEvent;
	private NexEventProxy.INexEventReceiver mEventReceiver;

	public NexClientManager(NexPlayer nexplayer) {
		mClientList = new ArrayList<NexClient>();
		mForwardEvent = false;
		setupEventReceiver(nexplayer);
	}

	protected class OpenParams {
		Context context;
		NexPlayer player;
		String path;
		String smiPath;
		String externalPDPath;
		int type;
		int transportType;
		int bufferingTime;

		protected OpenParams(Context context, String path, String smiPath, String externalPDPath, int type, int transportType, int bufferingTime) {
			this.context = context;
			this.path = path;
			this.smiPath = smiPath;
			this.externalPDPath = externalPDPath;
			this.type = type;
			this.transportType = transportType;
			this.bufferingTime = bufferingTime;
		}
	}

	private void setupEventReceiver(NexPlayer nexplayer) {
		mEventReceiver = new NexEventProxy.INexEventReceiver() {
			@Override
			public NexPlayerEvent[] eventsAccepted() {
				return new NexPlayerEvent[]{
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_WILL_INIT),
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_WILL_OPEN),
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_WILL_START),
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_WILL_RESUME),
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_WILL_SEEK),
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_WILL_PAUSE),
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_WILL_STOP),
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_WILL_CLOSE),
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_WILL_RELEASE),
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_ERROR),
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_TIME),
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_BUFFERINGBEGIN),
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_BUFFERINGEND),
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_BUFFERING),
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_ENDOFCONTENT),
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_DEBUGINFO),
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_ASYNC_CMD_COMPLETE),
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_STATECHANGED),
						new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_ONHTTPSTATS),
				};
			}

			@Override
			public void onReceive(NexPlayer nexplayer, NexPlayerEvent event) {

				switch (event.what) {

					case NexPlayerEvent.NEXPLAYER_EVENT_WILL_INIT :
						break;

					case NexPlayerEvent.NEXPLAYER_EVENT_WILL_OPEN :
						open(nexplayer, (OpenParams)event.obj);
						break;

					case NexPlayerEvent.NEXPLAYER_EVENT_WILL_START :
						start();
						break;

					case NexPlayerEvent.NEXPLAYER_EVENT_WILL_RESUME :
						resume();
						break;

					case NexPlayerEvent.NEXPLAYER_EVENT_WILL_SEEK :
						seek(event.intArgs[0]);
						break;

					case NexPlayerEvent.NEXPLAYER_EVENT_WILL_PAUSE :
						pause();
						break;

					case NexPlayerEvent.NEXPLAYER_EVENT_WILL_STOP :
						stop();
						break;

					case NexPlayerEvent.NEXPLAYER_EVENT_WILL_CLOSE :
						close();
						break;

					case NexPlayerEvent.NEXPLAYER_EVENT_WILL_RELEASE :
						release();
						break;

					case NexPlayerEvent.NEXPLAYER_EVENT_ERROR:
						onError(NexErrorCode.fromIntegerValue(event.intArgs[0]));
						break;

					case NexPlayerEvent.NEXPLAYER_EVENT_TIME:
						onTime(event.intArgs[0]);
						break;

					case NexPlayerEvent.NEXPLAYER_EVENT_BUFFERINGBEGIN:
						onBufferingBegin();
						break;

					case NexPlayerEvent.NEXPLAYER_EVENT_BUFFERINGEND:
						onBufferingEnd();
						break;

					case NexPlayerEvent.NEXPLAYER_EVENT_BUFFERING:
						onBuffering(event.intArgs[0]);
						break;

					case NexPlayerEvent.NEXPLAYER_EVENT_ENDOFCONTENT:
						onEndOfContent();
						break;

					case NexPlayerEvent.NEXPLAYER_EVENT_DEBUGINFO:
						if (event.intArgs[0] == NexPlayer.NEXPLAYER_DEBUGINFO_HTTP_REQUEST) {
							onHttpRequest(nexplayer, (String) event.obj);
						}
						break;

					case NexPlayerEvent.NEXPLAYER_EVENT_ASYNC_CMD_COMPLETE:
						onAsyncCmdComplete(nexplayer, event.intArgs[0], event.intArgs[1], event.intArgs[2], event.intArgs[3]);
						break;

					case NexPlayerEvent.NEXPLAYER_EVENT_STATECHANGED:
						onStateChanged(nexplayer, event.intArgs[0], event.intArgs[1]);
						break;

					case NexPlayerEvent.NEXPLAYER_EVENT_ONHTTPSTATS:
						onHttpStats(nexplayer, event.intArgs[0], event.obj);
						break;
				}
			}
		};
		nexplayer.getEventProxy().registerReceiver(mEventReceiver);
	}

	protected int addClient(NexClient client) {
		if( client != null ) {
			if( mClientList.add(client) == true ) {
				return (mClientList.size()-1);
			}
		}
		return INVALID_PARAMETER;
	}
	
	protected NexClient removeClient(int index) {
		NexClient ret = null;
		if ( (index > 0) && (index < mClientList.size()) ) {
			try {
				mClientList.get(index).releaseSession();
				ret = mClientList.remove(index);
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	// Deprecated. Do not used.
	protected int getStatus(int id) {
		return 0;
	}
	
	protected NexClient getClient(int index) {
		if( (mClientList != null) && ( index < mClientList.size() ) && (index > 0) ) {
			return mClientList.get(index);
		}
		return null;
	}

	private void open(NexPlayer nexplayer, OpenParams params) {

		mForwardEvent = (params.type == NexPlayer.NEXPLAYER_SOURCE_TYPE_STREAMING) ? true :false;
		if( mForwardEvent ) {
			for( NexClient client : mClientList ) {
				client.openSession(params.context, nexplayer, params.path);
			}
		}
	}

	private void start() {
		if( mForwardEvent ) {
			for( NexClient client : mClientList ) {
				client.startSession();
			}
		}
	}

	private void stop() {
		if( mForwardEvent ) {
			for( NexClient client : mClientList ) {
				client.stopSession();
			}
		}
	}

	private void resume() {
		if( mForwardEvent ) {
			for( NexClient client : mClientList ) {
				client.resumeSession();
			}
		}
	}

	private void pause() {
		if( mForwardEvent ) {
			for( NexClient client : mClientList ) {
				client.pauseSession();
			}
		}
	}

	private void close() {
		if( mForwardEvent ) {
			for( NexClient client : mClientList ) {
				client.closeSession();
			}
		}
	}

	private void release() {
		if( mForwardEvent ) {
			for( NexClient client : mClientList ) {
				client.releaseSession();
			}
		}
		mClientList.clear();
	}
	
	private void seek( int msec ) {
		if( mForwardEvent ) {
			for( NexClient client : mClientList ) {
				client.seek(msec);
			}
		}
	}

	protected void onError(NexErrorCode code) {
		if( mForwardEvent ) {
			for( NexClient client : mClientList ) {
				client.onError(code);
			}
		}
	}

	private void onTime( int msec ) {
		if( mForwardEvent ) {
			for( NexClient client : mClientList ) {
				client.onTime(msec);
			}
		}
	}

	private void onBufferingBegin() {
		if( mForwardEvent ) {
			for( NexClient client : mClientList ) {
				client.onBufferingBegin();
			}
		}
	}

	private void onBufferingEnd() {
		if( mForwardEvent ) {
			for( NexClient client : mClientList ) {
				client.onBufferingEnd();
			}
		}
	}

	private void onBuffering( int arg1 ) {
		if( mForwardEvent ) {
			for( NexClient client : mClientList ) {
				client.onBuffering(arg1);
			}
		}
	}

	private void onEndOfContent() {
		if( mForwardEvent ) {
			for( NexClient client : mClientList ) {
				client.onEndOfContent();
			}
		}
	}

	private void onHttpRequest(NexPlayer mp, String httpRequest) {
		if( mForwardEvent ) {
			for( NexClient client : mClientList ) {
				client.onHttpRequest(mp, httpRequest);
			}
		}
	}

	private void onAsyncCmdComplete(NexPlayer mp, int command, int result, int param1, int param2) {
		if( mForwardEvent ) {
			for( NexClient client : mClientList ) {
				client.onAsyncCmdComplete(mp, command, result, param1, param2);
			}
		}
	}

	private void onStateChanged( NexPlayer mp, int arg1, int arg2 ) {
		if( mForwardEvent ) {
			for( NexClient client : mClientList ) {
				client.onStateChanged(mp, arg1, arg2);
			}
		}
	}

	private void onHttpStats(NexPlayer mp, int arg1, Object obj) {
		if( mForwardEvent ) {
			for( NexClient client : mClientList ) {
				client.onHttpStats(mp, arg1, obj);
			}
		}
	}
}

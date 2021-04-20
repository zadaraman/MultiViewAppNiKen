package com.nexstreaming.nexplayerengine;

import android.content.Context;

import com.nexstreaming.nexplayerengine.NexPlayer.NexErrorCode;


public abstract class NexClient {
	
	public enum CLIENT_PROPERTY {
		PREF_STR_CUSTOMER_KEY,
		PREF_STR_USER_ID,
		PREF_STR_CDN_NAME,
		PREF_STR_PLAYER_NAME,
		PREF_STR_ASSET_NAME,
		PREF_BOOL_IS_LIVE
	}
	
	abstract protected void	openSession(Context context, NexPlayer player, String path);
	abstract protected void	startSession();
	abstract protected void pauseSession();
	abstract protected void resumeSession();
	abstract protected void stopSession();
	abstract protected void closeSession();
	abstract protected void releaseSession();

	protected void seek(int msec) {};
	protected void onError( NexErrorCode errorcode ){};
	protected void onTime( int msec ){};
	protected void onBufferingBegin(){};
	protected void onBufferingEnd(){};
	protected void onBuffering( int arg1 ){};
	protected void onEndOfContent(){};
	protected void onStateChanged( NexPlayer mp, int arg1, int arg2 ){};
	protected void onAsyncCmdComplete(NexPlayer mp, int command, int result, int param1, int param2){};
	protected void onHttpRequest(NexPlayer mp, String msg){};
	protected void onHttpStats(NexPlayer mp, int arg1, Object obj){};
	protected boolean isPrecision() { return false; };
}

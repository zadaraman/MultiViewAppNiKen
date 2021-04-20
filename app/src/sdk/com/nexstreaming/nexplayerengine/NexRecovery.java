package com.nexstreaming.nexplayerengine;

public class NexRecovery {

	private static String TAG = "NexRecovery";

	public NexRecovery() {
		NexLog.d(TAG, "Initializing NexRecovery");
	}
	
	public NexPlayerEvent handleRecoveryEvent(NexPlayerEvent eventToProcess) {
		if (eventToProcess == null) {
			return null;
		}

		switch (eventToProcess.what) {
			case NexPlayerEvent.NEXPLAYER_EVENT_ASYNC_CMD_COMPLETE:
				//NexWVSWDrm start
				if (eventToProcess.intArgs[1] == 44) {
					NexLog.d(TAG, "Need to recover from: " + eventToProcess.what + " event");
					NexPlayerEvent warningEvent = new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_WARNING_DRM_RECOVERY, new int[0], new long[0], null);
					return warningEvent;
				}
				//NexWVSWDrm end
				break;
		}

		return eventToProcess;
	}

	public void recoverFromFail(NexPlayer mp, NexPlayerEvent playerEvent) {
		if (playerEvent != null) {
			switch (playerEvent.what) {
				//NexWVSWDrm start
				case NexPlayerEvent.NEXPLAYER_WARNING_DRM_RECOVERY:
					NexLog.d(TAG, "Recovering from: " + playerEvent.what + " event");
					mp.recoverFromDRM();
					break;
				//NexWVSWDrm end
			}
		}
	}
}

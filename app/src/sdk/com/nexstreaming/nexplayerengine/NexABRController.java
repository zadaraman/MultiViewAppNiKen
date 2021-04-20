package com.nexstreaming.nexplayerengine;

import com.nexstreaming.nexplayerengine.NexEventProxy.INexEventReceiver;
import com.nexstreaming.nexplayerengine.NexPlayer.NexErrorCode;

/**
 * \brief  This class allows applications to control and use ABR-related methods within the NexPlayer&trade;&nbsp;SDK.
 * 
 * An instance of \c NexABRController can be called once NexPlayer&trade;&nbsp;has been created and initialized.
 * 
 * \since version 6.34
 */

public class NexABRController {
	private NexPlayer mNexPlayer;
	private IABREventListener mIABREventListener;
	private INexEventReceiver mEventReceiver;

	/**
	 * 
     * Sole constructor for \c NexABRController.
     *
     * The application must create an instance of the \c NexABRController class to control ABR-related methods 
     * which for example change the minimum/maximum allowed bandwidths or the target bandwidth for streaming content with multiple tracks.
     *
     * \since version 6.34
	 *
	 */
	public NexABRController(NexPlayer mp){
		mNexPlayer = mp;
		mEventReceiver = new INexEventReceiver() {
			@Override
			public NexPlayerEvent[] eventsAccepted() {
				return new NexPlayerEvent[]{ new NexPlayerEvent( NexPlayerEvent.NEXPLAYER_EVENT_STATUS_REPORT ) };
			}

			@Override
			public void onReceive(NexPlayer nexplayer, NexPlayerEvent event) {
				if( event.what  == NexPlayerEvent.NEXPLAYER_EVENT_STATUS_REPORT ) {
					if (event.intArgs[0] == NexPlayer.NEXPLAYER_STATUS_REPORT_MINMAX_BANDWIDTH_CHANGED) {
						notifyMinMaxBandWidthChanged( event.intArgs[1], event.intArgs[2], event.intArgs[3] );
					}
					else if (event.intArgs[0] == NexPlayer.NEXPLAYER_STATUS_REPORT_TARGET_BANDWIDTH_CHANGED) {
						notifyTargetBandWidthChanged( event.intArgs[1], event.intArgs[2], event.intArgs[3] );
					}
				}
			}
		};
		mNexPlayer.getEventProxy().registerReceiver(mEventReceiver);
	}

	/**
	 * 
	 * \brief  This interface must be implemented in order for the application to receive 
	 *  \c ABRControl events from \c NexABRController.
	 *
	 * \c NexABRController will call the methods provided in this interface
	 * automatically during playback to notify the application when various
	 * \c ABRControl events have occurred.
	 *
	 * In most cases, the handling of these events is optional; NexPlayer&trade;&nbsp;
	 * will continue to play content back normally without the application doing anything
	 * special in response to the events received.
	 * 
	 * \since version 6.34
	 *
	 */
	public interface IABREventListener {
		/**
		 * 
		 * \brief This method will be called by the \c NexABRController when either the minimum or maximum bandwith allowed for streaming content is changed.
		 *
		 * @param result
		 *					\c NexErrorCode object for the specified error code.
		 * @param minBwBps
		 *					Minimum bandwidth in bps (bits per second)
		 * @param maxBwBps
		 *					Maximum bandwidth in bps (bits per second)
		 *
		 * \see changeMinMaxBandWidth()
		 * \see changeMaxBandWidth()
		 * \see changeMinBandWidth()
		 *
		 * \since version 6.34
		 */
		abstract void onMinMaxBandWidthChanged(NexErrorCode result, int minBwBps, int maxBwBps);

		/**
		 *
		 * \brief This method will be called by the \c NexABRController when the target bandwidth for streaming content is changed.
		 *
		 *  For example, if content has three tracks at bandwidths of 500k, 900k, and 1200k and the applications calls 
		 *  \c setTargetBandwidth to set a target of 700k with the target option \c BELOW, \c reqBwBps will be 700,000 and 
		 *  \c selBwBps will be 500,000.
		 *
		 * @param result
		 *					\c NexErrorCode object for the specified error code.
		 * @param reqBwBps
		 *					The requested target bandwidth to select a track, in bps (bits per second)
		 * @param selBwBps
		 *					The actual bandwidth of the track selected, in bps (bits per second)
		 *
		 * \see setTargetBandWidth
		 * \since version 6.34
		 */
		abstract void onTargetBandWidthChanged(NexErrorCode result, int reqBwBps, int selBwBps);
	}

	/**
	 * \brief This method sets and registers an \c IABREventListener listener for the application playing content with NexPlayer&trade;. 
     * 
     * @param listener		IABREventListener
     *
     * \see NexABRController.IABREventListener
     * \since version 6.34
	 *
	 */
	public void setIABREventListener(IABREventListener listener) {
		mIABREventListener = listener;
	}

	/**
	 * 
	 * \brief This enumeration defines the possible options for how an application should use a target bandwidth set.
	 * 
	 * The options defined by this enumeration are possible values used to set the \c targetOption parameter when 
	 * calling \c setTargetBandwidth to set a new target bandwidth for streaming content with multiple tracks at different bandwidths such as HLS.
	 * 
	 * By default, when a target bandwidth is set, NexPlayer&trade;&nbsp;will choose the closest track available at a bandwidth below the target.
	 * For example, if there is content with five tracks at bandwidths of 500k, 900k, 1200k, 1500k, and 2000k, and the target bandwidth is chosen as 1200k with the option set to
	 * \c DEFAULT or \c BELOW, the target bandwidth will be set to 900k.
	 * If instead the target option is set to \c ABOVE in the same example, NexPlayer&trade;&nbsp;will set the target bandwidth to 1500k.
	 * 
	 * If a target bandwidth is to be set exactly, using the target option \c MATCH, then the target value will ONLY be changed if exactly
	 * a track with a bandwidth exactly the same as the value set is available.
	 *
	 * The target option should be set to one of the following:
	 * - <b>\c DEFAULT</b>: Default target option (\c BELOW)
	 * - <b>\c BELOW</b>: Select a track with a bandwidth below the target bandwidth.
	 * - <b>\c ABOVE</b>: Select a track with a bandwidth above the target bandwidth.
	 * - <b>\c MATCH</b>: Select the track that has a bandwidth that matches the target set; otherwise send an error and no new target bandwidth is selected.
	 *
	 * \see setTargetBandWidth
	 * \since version 6.34
	 *
	 */
	public enum TargetOption {
		DEFAULT (0x00000000),
		BELOW   (0x00000001),
		ABOVE   (0x00000002),
		MATCH   (0x00000003);

		int mCode;
		TargetOption(int code) {
			mCode = code;
		}
		private int getIntegerCode() {
			return mCode;
		}
	}

	/**
	 * 
	 *  \brief This enum defines the options possible for how NexPlayer&trade;&nbsp;should 
	 *  handle existing buffered content as a track changes (due to a set target bandwidth).
	 *
	 *  While NexPlayer&trade;&nbsp;will by default change to a new target bandwidth as optimally as possible,
	 *  there may be instances when it is preferable either for an application to preferentially change to 
	 *  the target bandwidth track more quickly (regardless of buffered content segments)
	 *  or to first play buffered segments before changing tracks.
	 * 
	 * These segment options can be used to set the parameter \c segOption when calling \c setTargetBandwidth to one of the following:
	 * 
	 *  - <b>\c DEFAULT</b>:  Default (NexPlayer&trade;&nbsp;will decide between \c QUICKMIX (changing tracks quickly) and \c LATEMIX (playing buffered content and changing tracks more slowly)).
	 *  - <b>\c QUICKMIX</b>:  NexPlayer&trade;&nbsp;will clear the buffer as much as possible and will start to download new track so user can see a new track faster.
	 *  - <b>\c LATEMIX</b>:  NexPlayer&trade;&nbsp;will preserve and play the content segments already buffered and will download a new track.
	 *
	 * \see setTargetBandwidth
	 * \since version 6.34
	 *
	 */
	public enum SegmentOption {
		DEFAULT     (0x00000000), ///< Default.  NexPlayer&trade;&nbsp;will decide between \c QUICKMIX and \c LATEMIX options automatically.
		QUICKMIX    (0x00000001), ///< NexPlayer&trade;&nbsp;will clear the buffer quickly and will start downloading new track segments more quickly.
		LATEMIX     (0x00000002); ///< NexPlayer&trade;&nbsp;will preserve buffered segments and will download new track.

		int mCode;
		SegmentOption(int code) {
			mCode = code;
		}
		private int getIntegerCode() {
			return mCode;
		}
	}

	private void notifyMinMaxBandWidthChanged(int result, int minBwBps, int maxBwBps) {
		if( mIABREventListener != null && mNexPlayer != null ) {
			mIABREventListener.onMinMaxBandWidthChanged(NexErrorCode.fromIntegerValue(result), minBwBps, maxBwBps);
		}
	}

	private void notifyTargetBandWidthChanged(int result, int reqBwBps, int selBwBps) {
		if( mIABREventListener != null ) {
			mIABREventListener.onTargetBandWidthChanged(NexErrorCode.fromIntegerValue(result), reqBwBps, selBwBps);
		}
	}

	/**
	 * 
	 * \brief  This method sets the minimum and maximum bandwidth for streaming playback dynamically during playback.
	 *
	 * This applies in
	 * cases where there are multiple tracks at different bandwidths (such as
	 * in the case of HLS).  The player will not consider
	 * any track under the minimum, and over the maximum bandwidth when determining whether a track
	 * change is appropriate, even if it detects less, and more bandwidth available.
	 *
	 *
	 * @param minBwBps
	 *					Minimum bandwidth in bps (bits per second).
	 * @param maxBwBps
	 *					Maximum bandwidth in bps (bits per second).
	 * @return NexErrorCode 
	 *
	 * \since version 6.34
	 *
	 */
	public NexErrorCode changeMinMaxBandWidth(int minBwBps, int maxBwBps) {
		NexErrorCode ret = NexErrorCode.HAS_NO_EFFECT;
		if( mNexPlayer != null ) {
			ret = NexErrorCode.fromIntegerValue( mNexPlayer.changeMinMaxBandWidthBps(minBwBps, maxBwBps));
		}
		return ret;
	}

	/**
	 *
	 * \brief  This method sets the maximum bandwidth for streaming playback dynamically during playback.
	 *
	 * This applies in
	 * cases with content where there are multiple tracks at different bandwidths (such as
	 * in the case of HLS).  The player will not consider
	 * any track over the maximum bandwidth when determining whether a track
	 * change is appropriate, even if it detects more bandwidth available.
	 *
	 * @param maxBwBps
	 *					Maximum bandwidth in bps (bits per second).
	 * @return NexErrorCode
	 *
	 * \since version 6.34
	 *
	 */
	public NexErrorCode changeMaxBandWidth(int maxBwBps) {
		NexErrorCode ret = NexErrorCode.HAS_NO_EFFECT;
		if( mNexPlayer != null ) {
			ret = NexErrorCode.fromIntegerValue( mNexPlayer.changeMaxBandWidthBps(maxBwBps));
		}
		return ret;
	}

	/**
	 * 
	 * \brief  This method sets the minimum bandwidth for streaming playback dynamically during playback.
	 *
	 * This applies in
	 * cases with content where there are multiple tracks at different bandwidths (such as
	 * in the case of HLS).  The player will not consider
	 * any track under the minimum bandwidth when determining whether a track
	 * change is appropriate, even if it detects less bandwidth available.
	 *
	 * @param minBwBps
	 *					Minimum bandwidth in bps (bits per second).
	 * @return NexErrorCode
	 *
	 * \since version 6.34
	 *
	 */
	public NexErrorCode changeMinBandWidth(int minBwBps) {
		NexErrorCode ret = NexErrorCode.HAS_NO_EFFECT;
		if( mNexPlayer != null ) {
			ret = NexErrorCode.fromIntegerValue( mNexPlayer.changeMinBandWidthBps(minBwBps) );
		}
		return ret;
	}

	/**
	 * \brief  This method sets the target bandwidth for streaming playback dynamically during playback.
	 *
	 * This method should be called after NexPlayer.open().
	 * This applies in cases with content where there are multiple tracks at different bandwidths (such as
	 * in the case of HLS).  The player will not consider
	 * any track under the target bandwidth and over the target bandwidth when determining whether a track
	 * change is appropriate, even if it detects less and more bandwidth available.
	 *
	 * @param targetBwBps
	 *					Target bandwidth in bps (bits per second).
	 * @param segOption One of the following \c SegmentOption values, indicating how to handle buffered content when the track changes:
	 * 					  - <b>\c DEFAULT</b>:  Default (NexPlayer&trade;&nbsp;will decide between \c QUICKMIX (changing tracks quickly) and \c LATEMIX (playing buffered content and changing tracks more slowly)).
	 * 					  - <b>\c QUICKMIX</b>:  NexPlayer&trade;&nbsp;will clear the buffer as much as possible and will start to download new track so user can see a new track faster.
	 * 					  - <b>\c LATEMIX</b>:  NexPlayer&trade;&nbsp;will preserve and play the content segments already buffered and will download a new track.
	 * 
	 * @param targetOption  How to use the target bandwidth value set.  One of the following \c TargetOption options:
	 * 						  - <b>\c DEFAULT</b>: Default target option (\c BELOW)
	 * 						  - <b>\c BELOW</b>: Select a track with a bandwidth below the target bandwidth.
	 * 						  - <b>\c ABOVE</b>: Select a track with a bandwidth above the target bandwidth.
	 * 						  - <b>\c MATCH</b>: Select the track that has a bandwidth that matches the target set; otherwise send an error and no new target bandwidth is selected.
	 *
	 *
	 * @return NexErrorCode
	 *
	 * \see TargetOption enumeration
	 * \see SegmentOption enumeration
	 * \see setABREnabled
	 * \since version 6.34
	 */
	public NexErrorCode setTargetBandWidth(int targetBwBps, SegmentOption segOption, TargetOption targetOption) {
		NexErrorCode ret = NexErrorCode.HAS_NO_EFFECT;
		if( mNexPlayer != null ) {
			ret = NexErrorCode.fromIntegerValue(
					mNexPlayer.setTargetBandWidth( targetBwBps, segOption.getIntegerCode(), targetOption.getIntegerCode()) );
		}
		return ret;
	}

	/**
	 * \brief  This method sets whether ABR methods should be used or not.
	 * 
	 * In general, NexPlayer&trade;&nbsp;plays streaming content, including content with multiple tracks at different bandwidths such as HLS,
	 * by choosing the optimal track according to network conditions and device performance.  This is the default behavior of NexPlayer&trade;&nbsp;and
	 * this occurs when ABR is enabled (or calling \c setABREnabled with the parameter \c enabled set to \c TRUE).
	 * 
	 * However, there may be instances when an application may want to set limits on which tracks should be selected and played by NexPlayer&trade;&nbsp;in
	 * order to provide a specific user experience, and to force NexPlayer&trade;&nsbp;to stay on a particular bandwidth track, regardless of network conditions.  
	 * In cases like this, in order to keep playing a track at a target bandwidth (set with \c setTargetBandWidth) this method must be called
	 * to disable NexPlayer&trade;'s ABR behavior (with the parameter \c enabled set to \c FALSE).
	 * 
	 * \warning This method <em>must</em> be called with \c enabled set to \c FALSE <b>before</b> calling \c setTargetBandWidth if the application should
	 *          continue playing the target bandwidth <em>regardless</em> of network conditions.
	 *
	 * @param enabled
	 *			- <b> \c TRUE</b>: ABR enabled.  NexPlayer&trade;&nbsp;will handle track changes automatically.
	 *			- <b> \c FALSE</b>: ABR disabled.  NexPlayer&trade;&nbsp;will continue playing the target bandwidth track set, regardless of network conditions. 
	 * @return NexErrorCode
	 *
	 * \see setTargetBandWidth
	 * \since version 6.34
	 *
	 */
	public NexErrorCode setABREnabled(boolean enabled) {
		NexErrorCode ret = NexErrorCode.HAS_NO_EFFECT;
		if( mNexPlayer != null ) {
			ret = NexErrorCode.fromIntegerValue( mNexPlayer.setABREnabled(enabled));
		}
		return ret;
	}
}

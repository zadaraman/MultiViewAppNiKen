package com.nexstreaming.nexplayerengine;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The new offline playback feature is an expansion, which is a better way of saving and managing the data required for offline playback storing than a database.
 *
 * To use the storing feature, create a NexOfflineStoreController object. 
 *
 * @see Section How to store HLS content for more details.
 */
public class NexOfflineStoreController {
	private static final String TAG = "NexOfflineStoreController";

	private NexPlayer mNexPlayer;
	private NexEventProxy.INexEventReceiver mEventReceiver;
	private Context mContext;
	private OfflineStoreState mState = OfflineStoreState.NONE;
	private NexSettingDataForStoring mOfflineStoreSettingData = new NexSettingDataForStoring();
	private IOfflineStoreListener mListener = null;

	private int mNumOfRepetition = 0;
	private int mMaxNumOfRepetition = 0;
	private int mStartingNumOfRepetition = 0;

	private final Handler mHandler = new Handler();

	private enum OfflineStoreState {
		NONE, STORE, CONTINUE_STORE
	}

	public static final int MEDIA_TYPE_AUDIO     = 0x00;
	public static final int MEDIA_TYPE_VIDEO     = 0x01;
	public static final int MEDIA_TYPE_TEXT      = 0x02;

	private boolean bUseTrackIDSetting;

	private class TargetStreamID {
		int mAudioStreamID = NexPlayer.MEDIA_STREAM_DEFAULT_ID;
		int mVideoStreamID = NexPlayer.MEDIA_STREAM_DEFAULT_ID;
		int mTextStreamID = NexPlayer.MEDIA_STREAM_DEFAULT_ID;
		int mCustomAttributeID = NexPlayer.MEDIA_STREAM_DEFAULT_ID;

		public TargetStreamID(int audioStreamID, int videoStreamID, int textStreamID, int customAttributeID) {
			mAudioStreamID = audioStreamID;
			mVideoStreamID = videoStreamID;
			mTextStreamID = textStreamID;
			mCustomAttributeID = customAttributeID;
		}

		@Override
		public String toString() {
			return "audioStreamId : " + mAudioStreamID +
					", videoStreamId : " + mVideoStreamID +
					", textStreamId : " + mTextStreamID +
					", customAttrId : " + mCustomAttributeID;
		}
	}

	private ArrayList<TargetStreamID> mTargetStreamIDArrayList = new ArrayList<TargetStreamID>();
	private FileDescriptor mStoredInfoFD = null;

	/**
	 * @brief This interface allows the application to get events about the Offline Store from NexOfflineStoreController.
	 * When stroing data to NexOfflineStoreController, the user must call setListener(IOfflineStoreListener i) to get events before startOfflineStore. 
	 * When the user wants to call an API that edits UI-related APIs or the state of OfflineStore such as stopOfflineStore() from IOfflineStoreListener callback, they must use a Handler in order for the API to be able to run on the main application thread.
	 */
	public interface IOfflineStoreListener {
		/**
		 * This event is called when there is an error.
		 *
		 * @param errorCode The error code for the generated error.
		 */
		void onError(NexPlayer.NexErrorCode errorCode);

		/**
		 * This event is called after the Offline Store is started after startOfflineStore API is called. 
		 */
		void offlineStoreStarted();

		/**
		 * This event is called after the Offline Store is stopped after stopOfflineStore API is called.
		 */
		void offlineStoreStopped();

		/**
		 * This event is called after the Offline Store is resumed after resumeOfflineStore API is called.
		 */
		void offlineStoreResumed();

		/**
		 * This event is called after the Offline Store is paused after pauseOfflineStore API is called.
		 */
		void offlineStorePaused();

		/**
		 * This event is called when the download starts.
		 */
		void onDownloadBegin();

		/**
		 * This event reports the end of download.
		 *
		 * @param percentage
		 */
		void onDownloading(int percentage);

		/**
		 * This event is called when the download is complete.
		 *
		 * @param completed Set to \c TRUE for when the content is completely downloaded. 
		 *                  Call stopOfflineStore after onDownloadEnd(true) is received. 
		 */
		void onDownloadEnd(boolean completed);

		/**
		 * This event is called when the stream information is complete.
		 */
		boolean onContentInfoReady();
	}

	/**
	 * @brief The user must register IOfflineStoreListener in NexOfflineStoreController in order for the application to get Offline Store events.
	 * The application must call setListener before calling startOfflineStore.
	 *
	 * @param l The callback to be invoked.
	 */
	public void setListener(IOfflineStoreListener l) {
		mListener = l;
	}

	/**
	 * @brief Constructor for NexOfflineStoreController.
	 *
	 * To use the storing feature of new offline playback, the user must create an instance of NexOfflineStoreController.
	 *
	 * @param player NexPlayer instance.
	 * @param context The current context; from \c Activity subclasses, you can
	 *                      just pass <code>this</code>.
	 */
	public NexOfflineStoreController(NexPlayer player, Context context) {
		mNexPlayer = player;
		mContext = context;
		mEventReceiver = new NexEventProxy.INexEventReceiver() {
			@Override
			public NexPlayerEvent[] eventsAccepted() {
				return new NexPlayerEvent[]{ new NexPlayerEvent( NexPlayerEvent.NEXPLAYER_EVENT_STATUS_REPORT ),
						new NexPlayerEvent( NexPlayerEvent.NEXPLAYER_EVENT_ASYNC_CMD_COMPLETE ),
						new NexPlayerEvent( NexPlayerEvent.NEXPLAYER_EVENT_ENDOFCONTENT ),
						new NexPlayerEvent( NexPlayerEvent.NEXPLAYER_EVENT_ERROR )};
			}

			@Override
			public void onReceive(NexPlayer nexplayer, NexPlayerEvent event) {
				if( mState != OfflineStoreState.NONE ) {
					switch (event.what) {
						case NexPlayerEvent.NEXPLAYER_EVENT_STATUS_REPORT:
							if (event.intArgs[0] == NexPlayer.NEXPLAYER_STATUS_REPORT_DOWNLOAD_PROGRESS) {
								int progress = (int)(((float)event.intArgs[1] / (mMaxNumOfRepetition +1) ) + (100.0f / (mMaxNumOfRepetition +1)) * mNumOfRepetition);
								if( mOfflineStoreSettingData.storePercentage < progress ) {
									progress = Math.min(100, progress);
									mOfflineStoreSettingData.storePercentage = progress;
									if (mListener != null)
										mListener.onDownloading(progress);
								}
							}
							break;
						case NexPlayerEvent.NEXPLAYER_EVENT_ASYNC_CMD_COMPLETE:
							onAsyncCmdComplete(nexplayer, event.intArgs[0], event.intArgs[1], event.intArgs[2], event.intArgs[3]);
							break;
						case NexPlayerEvent.NEXPLAYER_EVENT_ENDOFCONTENT:
							mOfflineStoreSettingData.storePercentage = (int)((100.0f / mTargetStreamIDArrayList.size()) * (mNumOfRepetition +1));
							if( mMaxNumOfRepetition == mNumOfRepetition) {
								if (mListener != null)
									mListener.onDownloadEnd(true);
							} else {
								nexplayer.stop();
							}
							break;
						case NexPlayerEvent.NEXPLAYER_EVENT_ERROR:
							if( mListener != null )
								mListener.onError(NexPlayer.NexErrorCode.fromIntegerValue(event.intArgs[0]));
							break;
					}
				}
			}
		};
		mNexPlayer.getEventProxy().registerReceiver(mEventReceiver);
	}

	/**
	 * @brief This method starts Offline storing.
	 * If IOfflineStoreListener is already registered, offlineStoreStarted callback is called when the Offline Store is started.
	 *
	 * @param url The URL to store.
	 * @param storedInfoFilePath The absolute path of the stored info file.
	 * @param transportType The network transport type to use on the connection. This should be one of:
	 *							- NEXPLAYER_TRANSPORT_TYPE_TCP
	 *							- NEXPLAYER_TRANSPORT_TYPE_UDP
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp; error code in the event of a failure.
	 *
	 */
	public int startOfflineStore(String url, String storedInfoFilePath, int transportType) {
		NexLog.d(TAG, "startOfflineStore url : " + url + " storedInfoFilePath : " + storedInfoFilePath);
		mOfflineStoreSettingData.storeURL = url;
		mOfflineStoreSettingData.storeInfoFile = storedInfoFilePath;

		return startOfflineStore(mContext, mNexPlayer, mOfflineStoreSettingData, transportType,null,null);
	}
		/**
	 * @brief This method starts Offline storing.
	 * If IOfflineStoreListener is already registered, offlineStoreStarted callback is called when the Offline Store is started.
	 *
	 * @param url The URL to store.
	 * @param storedInfoFilePath The absolute path of the stored info file.
	 * @param transportType The network transport type to use on the connection. This should be one of:
	 *							- NEXPLAYER_TRANSPORT_TYPE_TCP
	 *							- NEXPLAYER_TRANSPORT_TYPE_UDP
	 * @param optionalHeaders Optional Headers for the license request
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp; error code in the event of a failure.
	 *
	 */
	public int startOfflineStore(String url, String storedInfoFilePath, int transportType, Map<String, String> optionalHeaders) {
		NexLog.d(TAG, "startOfflineStore url : " + url + " storedInfoFilePath : " + storedInfoFilePath);
		mOfflineStoreSettingData.storeURL = url;
		mOfflineStoreSettingData.storeInfoFile = storedInfoFilePath;

		return startOfflineStore(mContext, mNexPlayer, mOfflineStoreSettingData, transportType, optionalHeaders,null);
	}

	/**
	 * @brief This method starts Offline storing.
	 * If IOfflineStoreListener is already registered, offlineStoreStarted callback is called when the Offline Store is started.
	 *
	 * @param url The URL to store.
	 * @param storedInfoFilePath The absolute path of the stored info file.
	 * @param transportType The network transport type to use on the connection. This should be one of:
	 *							- NEXPLAYER_TRANSPORT_TYPE_TCP
	 *							- NEXPLAYER_TRANSPORT_TYPE_UDP
	 * @param optionalHeaders Optional Headers for the license request
	 * @param httpHeaders Http Headers for the
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp; error code in the event of a failure.
	 *
	 */
	public int startOfflineStore(String url, String storedInfoFilePath, int transportType, Map<String, String> optionalHeaders, List<String> httpHeaders) {
		NexLog.d(TAG, "startOfflineStore url : " + url + " storedInfoFilePath : " + storedInfoFilePath);

		mOfflineStoreSettingData.storeURL = url;
		mOfflineStoreSettingData.storeInfoFile = storedInfoFilePath;

		return startOfflineStore(mContext, mNexPlayer, mOfflineStoreSettingData, transportType, optionalHeaders,httpHeaders);
	}

	/**
	 * @brief This method starts storing from where it left off based on the stored info file.
	 * This will start storing where it left off, so changing the setting to setOfflineStoreSetting will not have any effect.
	 * If IOfflineStoreListener is already registered, offlineStoreStarted callback is called when the Offline Store is started.
	 *
	 * @param storedInfoFD The file descriptor of the info file created from storing.
	 * @param transportType The network transport type to use on the connection. This should be one of:
	 *							- NEXPLAYER_TRANSPORT_TYPE_TCP
	 *							- NEXPLAYER_TRANSPORT_TYPE_UDP
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp; error code in the event of a failure.
	 */
	public int startOfflineStore(FileDescriptor storedInfoFD, int transportType) {
		mOfflineStoreSettingData = new NexSettingDataForStoring(storedInfoFD);
		NexLog.d(TAG, "startOfflineStore storedInfoFD : " + storedInfoFD);

		int ret = NexPlayer.NexErrorCode.INVALID_PARAMETER.getIntegerCode();
		if(!TextUtils.isEmpty(mOfflineStoreSettingData.storeURL)) {
			ret = startOfflineStore(mContext, mNexPlayer, mOfflineStoreSettingData, transportType,null, null);
		}
		return ret;
	}
     /**
	 * @brief This method starts storing from where it left off based on the stored info file.
	 * This will start storing where it left off, so changing the setting to setOfflineStoreSetting will not have any effect.
	 * If IOfflineStoreListener is already registered, offlineStoreStarted callback is called when the Offline Store is started.
	 *
	 * @param storedInfoFilePath The absolute path of the stored
	 * @param transportType The network transport type to use on the connection. This should be one of:
	 *							- NEXPLAYER_TRANSPORT_TYPE_TCP
	 *							- NEXPLAYER_TRANSPORT_TYPE_UDP
	 * @param optionalHeaders Optional Headers for the license request
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp; error code in the event of a failure.
	 */
	public int startOfflineStore(String storedInfoFilePath, int transportType) {
		mOfflineStoreSettingData = new NexSettingDataForStoring(new File(storedInfoFilePath));
		return startOfflineStore(mOfflineStoreSettingData.storeURL, storedInfoFilePath, transportType,null);
	}
	/**
	 * @brief This method starts storing from where it left off based on the stored info file.
	 * This will start storing where it left off, so changing the setting to setOfflineStoreSetting will not have any effect.
	 * If IOfflineStoreListener is already registered, offlineStoreStarted callback is called when the Offline Store is started.
	 *
	 * @param storedInfoFilePath The absolute path of the stored
	 * @param transportType The network transport type to use on the connection. This should be one of:
	 *							- NEXPLAYER_TRANSPORT_TYPE_TCP
	 *							- NEXPLAYER_TRANSPORT_TYPE_UDP
	 * @param optionalHeaders Optional Headers for the license request
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp; error code in the event of a failure.
	 */
	public int startOfflineStore(String storedInfoFilePath, int transportType,Map<String, String> optionalHeaders) {
		mOfflineStoreSettingData = new NexSettingDataForStoring(new File(storedInfoFilePath));
		return startOfflineStore(mOfflineStoreSettingData.storeURL, storedInfoFilePath, transportType,optionalHeaders);
	}


	/**
         * @brief This method pauses storing.
	 * If IOfflineStoreListener is already registered, offlineStorePaused callback is called when the Offline Store is paused.
	 *
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp; error code in the event of a failure.
	 */
	public int pauseOfflineStore() {
		int ret = NexPlayer.NexErrorCode.HAS_NO_EFFECT.getIntegerCode();
		if( mNexPlayer != null && mNexPlayer.isInitialized() )
			ret = mNexPlayer.pause();
		return ret;
	}

	/**
     * @brief This method resumes storing from where it paused.
	 * If IOfflineStoreListener is already registered, offlineStoreResumed callback is called when the Offline Store is resumed.
	 *
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp; error code in the event of a failure.
	 */
	public int resumeOfflineStore() {
		int ret = NexPlayer.NexErrorCode.HAS_NO_EFFECT.getIntegerCode();
		if(  mNexPlayer != null && mNexPlayer.isInitialized() && mNexPlayer.getState() == NexPlayer.NEXPLAYER_STATE_PAUSE )
			ret = mNexPlayer.resume();
		return ret;
	}

	/**
	 * @brief This method stops storing.
	 * When stopOfflineStore is called, data will be written in the stored info file, so this means
	 * the user must call this method after executing startOfflineStore.
         * If IOfflineStoreListener is already registered, offlineStoreStopped callback is called when the Offline Store is stopped.	     *
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp; error code in the event of a failure.
	 */
	public int stopOfflineStore() {
		int ret = NexPlayer.NexErrorCode.HAS_NO_EFFECT.getIntegerCode();
		if(  mNexPlayer != null && mNexPlayer.isInitialized() ) {
			int state = mNexPlayer.getState();
			if( state == NexPlayer.NEXPLAYER_STATE_STOP )
				ret = mNexPlayer.close();
			else
				ret = mNexPlayer.stop();
		}

		return ret;
	}

	/**
	 * @brief This method deletes the cache directory files and then the stored info files.
	 *
	 * @param storedInfoFile The file descriptor of the info file created from storing.
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp; error code in the event of a failure.
	 */
	public static int deleteOfflineCache(File storedInfoFile) {
		NexLog.d(TAG, "deleteOfflineCache storedInfoFile : " + storedInfoFile);
		return NexStoredInfoFileUtils.deleteOfflineCache(storedInfoFile);
	}

	/**
	 * @brief This method deletes the cache directory files and then the stored info files.
	 *
	 * @param storedInfoFilePath  The absolute path of the stored info file.
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp; error code in the event of a failure.
	 */
	public static int deleteOfflineCache(String storedInfoFilePath) {
		return NexStoredInfoFileUtils.deleteOfflineCache(new File(storedInfoFilePath));
	}

	/**
	 * @brief This method sets the setting values needed for storing.
	 * Settings that are not customized will be set to default values.
	 * The user must call this method before calling startOfflineStore(),
	 * but settings will be ignored for Continue Store and Retrieve.
	 *
	 * @param setting The NexOfflineStoreSetting to set.
	 * @param value The new \c integer value for the NexOfflineStoreSetting.
	 */
	public void setOfflineStoreSetting(NexOfflineStoreSetting setting, int value) {
		mOfflineStoreSettingData.setValue(setting, value);
		if (setting == setting.INTEGER_AUDIO_TRACK_ID && value != -1)
			bUseTrackIDSetting = true;
	}

	/**
	 * @brief This method sets the setting values needed for storing.
	 * Settings that are not customized will be set to default values.
	 * The user must call this method before calling startOfflineStore(),
	 * but settings will be ignored for Continue Store and Retrieve.
	 *
	 * @param setting The NexOfflineStoreSetting to set.
	 * @param value The new \c String value for the NexOfflineStoreSetting.
	 */
	public void setOfflineStoreSetting(NexOfflineStoreSetting setting, String value) {
		mOfflineStoreSettingData.setValue(setting, value);
	}

	/**
	 * The parameters needed for the offline storing are defined as follows.
	 */
	public enum NexOfflineStoreSetting {
		/**
		 * Possible key value for \c key parameter of {@link setOfflineStoreSetting( NexOfflineStoreSetting , int) }.
		 * This stores the nearest track to the set bandwidth.
		 *
		 * unit : bps
		 * default value : 3000000
		 * */
		INTEGER_BANDWIDTH,
		/**
		 * Possible key value for \c key parameter of {@link setOfflineStoreSetting( NexOfflineStoreSetting , int) }.
		 * This stores the audio stream that matches the set stream ID.
		 * If there is none, it stores the default stream.
		 *
		 * If you want to store all audio streams, set the value to STREAM_ID_ALL.
		 *
		 * @note default value : NexPlayer.MEDIA_STREAM_DEFAULT_ID
		 * */
		INTEGER_AUDIO_STREAM_ID,
		/**
		 * Possible key value for \c key parameter of {@link setOfflineStoreSetting( NexOfflineStoreSetting , int) }.
		 * This stores the video stream that matches the set stream ID.
		 * If there is none, it stores the default stream.
		 *
		 * If you want to store all video streams, set the value to STREAM_ID_ALL.
		 *
		 * @note default value : NexPlayer.MEDIA_STREAM_DEFAULT_ID
		 * */
		INTEGER_VIDEO_STREAM_ID,
		/**
		 * Possible key value for \c key parameter of {@link setOfflineStoreSetting( NexOfflineStoreSetting , int) }.
		 * This stores the text stream that matches the set stream ID.
		 * If there is none, it stores the default stream.
		 *
		 * If you want to store all text streams, set the value to STREAM_ID_ALL.
		 *
		 * @note default value : NexPlayer.MEDIA_STREAM_DEFAULT_ID
		 * */
		INTEGER_TEXT_STREAM_ID,
		/**
		 * Possible key value for \c key parameter of {@link setOfflineStoreSetting( NexOfflineStoreSetting , int) }.
		 * This stores the custom attribute stream that matches the set stream ID.
		 * If there is none, it stores the default stream.
		 *
		 * @note default value : NexPlayer.MEDIA_STREAM_DEFAULT_ID
		 * */
		INTEGER_CUSTOM_ATTRIBUTE_ID,
		/**
		 * Possible key value for \c key parameter of {@link setOfflineStoreSetting( NexOfflineStoreSetting , String) }.
		 * This sets the directory to save the cache file.
		 *
		 * @note default value : Environment.getExternalStorageDirectory().getPath() + File.separator + "NexPlayerCache" + File.separator
		 * */
		STRING_STORE_PATH,
		/**
		 * Possible key value for \c key parameter of {@link setOfflineStoreSetting( NexOfflineStoreSetting , String) }.
		 * For offline playback in the future, the user must pass the keyID created with onOfflineKeyStoreListener to NexPlayer.
		 * @see How to Store Media DRM Content in the document.
		 *
		 * @note default value : \c NULL
		 * */
		STRING_OFFLINE_KEY_ID,
		/**
		 * Possible key value for \c key parameter of {@link setOfflineStoreSetting( NexOfflineStoreSetting , String) }.
		 * the URL of the Key Server.
		 * The user must adjust the settings to store media DRM content.
		 *
		 * @note default value : \c NULL
		 * */
		STRING_MEDIA_DRM_KEY_SERVER_URL,
		/**
		 * Possible key value for \c key parameter of {@link setOfflineStoreSetting( NexOfflineStoreSetting , String) }.
		 * This sets the audio language to store.
		 * If INTEGER_AUDIO_STREAM_ID has a set value, this value will be ignored.
		 *
		 * @note default value : \c NULL
		 * */
		STRING_PREFER_LANGUAGE_AUDIO,
		/**
		 * Possible key value for \c key parameter of {@link setOfflineStoreSetting( NexOfflineStoreSetting , String) }.
		 * This sets the text language to store.
		 * INTEGER_TEXT_STREAM_ID has a set value, this value will be ignored.
		 *
		 * @note default value : \c NULL
		 * */
		STRING_PREFER_LANGUAGE_TEXT,
		/**
		 * Possible key value for \c key parameter of {@link setOfflineStoreSetting( NexOfflineStoreSetting , int) }.
		 * This sets the DRM type whitch using MediaDrm interface or SW WideVine DRM module.
		 *						Possible Values:
		 *							0 : No DRM
		 *							1 : Usng Android MediaDrm interface
		 *						    2 : Using SW WideVine CDM
		 *						    3 : Using Automatic selection of SW/HW WideVine Drm
		 *
		 * default value : 0
		 * */
		INTEGER_DRM_TYPE,
		/**
		 * Possible key value for \c key parameter of {@link setOfflineStoreSetting( NexOfflineStoreSetting , int) }.
		 * This stores the audio track that matches the set track ID.
		 * If there is none, it stores the default track.
		 *
		 * @note default value : NexPlayer.MEDIA_TRACK_DEFAULT_ID
		 * */
		INTEGER_AUDIO_TRACK_ID,
		/**
		 * Possible key value for \c key parameter of {@link setOfflineStoreSetting( NexOfflineStoreSetting , int) }.
		 * This sets the number of segments in parallel for speeding up the downloads. Min is 1 and max is 5.
		 *
		 * @note default value : 1
		 * */
		PARALLEL_SEGMENTS_TO_DOWNLOAD;
		/**
		 * Possible value for \c value parameter of {@link setOfflineStoreSetting( NexOfflineStoreSetting , int) }.
		 *
		 * If you want to store all streams of specific type, set the value to STREAM_ID_ALL.
		 *
		 * ex/ setOfflineStoreSetting( INTEGER_AUDIO_STREAM_ID , STREAM_ID_ALL )
		 *     -> it will store all audio streams.
		 * */
		public static final int STREAM_ID_ALL = -3;
	}

	private void getTargetStreamIDArrayList(ArrayList<TargetStreamID> list, NexContentInformation info, NexSettingDataForStoring settingData) {
		if( list != null ) {
			list.clear();

			ArrayList<Integer> audioStreamIdList = new ArrayList<Integer>();
			ArrayList<Integer> videoStreamIdList = new ArrayList<Integer>();
			ArrayList<Integer> textStreamIdList = new ArrayList<Integer>();
			ArrayList<Integer> customAttrIdList = new ArrayList<Integer>();

			customAttrIdList.add(getCustomAttrStreamID(settingData, info));

			NexLog.d(TAG, "settingData.audioStreamID : " + settingData.audioStreamID + " settingData.videoStreamID : " + settingData.videoStreamID + " settingData.textStreamID : " + settingData.textStreamID);

			if( settingData.audioStreamID == NexOfflineStoreSetting.STREAM_ID_ALL ||
					settingData.videoStreamID == NexOfflineStoreSetting.STREAM_ID_ALL ||
					settingData.textStreamID == NexOfflineStoreSetting.STREAM_ID_ALL ) {
				for (NexStreamInformation curStream : info.mArrStreamInformation) {
					switch (curStream.mType) {
						case NexPlayer.MEDIA_STREAM_TYPE_AUDIO:
							if( settingData.audioStreamID == NexOfflineStoreSetting.STREAM_ID_ALL ||
									curStream.mID == settingData.audioStreamID )
								audioStreamIdList.add(curStream.mID);
							break;
						case NexPlayer.MEDIA_STREAM_TYPE_VIDEO:
							if( settingData.videoStreamID == NexOfflineStoreSetting.STREAM_ID_ALL  ||
									curStream.mID == settingData.videoStreamID )
								videoStreamIdList.add(curStream.mID);
							break;
						case NexPlayer.MEDIA_STREAM_TYPE_TEXT:
							if( settingData.textStreamID == NexOfflineStoreSetting.STREAM_ID_ALL  ||
									curStream.mID == settingData.textStreamID )
								textStreamIdList.add(curStream.mID);
							break;
					}
				}

				int count = Math.max(audioStreamIdList.size(), videoStreamIdList.size());
				count = Math.max(count, textStreamIdList.size());
				count = Math.max(count, 1);

				for( int i = 1; i <= count; i++ ) {
					if( audioStreamIdList.size() < i )
						audioStreamIdList.add(NexPlayer.MEDIA_STREAM_DEFAULT_ID);
					if( videoStreamIdList.size() < i )
						videoStreamIdList.add(NexPlayer.MEDIA_STREAM_DEFAULT_ID);
					if( textStreamIdList.size() < i )
						textStreamIdList.add(NexPlayer.MEDIA_STREAM_DEFAULT_ID);
					if( customAttrIdList.size() < i )
						customAttrIdList.add(NexPlayer.MEDIA_STREAM_DEFAULT_ID);
				}

				for( int i = 0; i < count; i++ ) {
					list.add(new TargetStreamID(audioStreamIdList.get(i), videoStreamIdList.get(i), textStreamIdList.get(i), customAttrIdList.get(i)));
				}
			} else {
				audioStreamIdList.add(getStreamID(mOfflineStoreSettingData, NexPlayer.MEDIA_STREAM_TYPE_AUDIO, info));
				videoStreamIdList.add(getStreamID(mOfflineStoreSettingData, NexPlayer.MEDIA_STREAM_TYPE_VIDEO, info));
				textStreamIdList.add(getStreamID(mOfflineStoreSettingData, NexPlayer.MEDIA_STREAM_TYPE_TEXT, info));
				list.add(new TargetStreamID(audioStreamIdList.get(0), videoStreamIdList.get(0), textStreamIdList.get(0), customAttrIdList.get(0)));
			}

			for( int i = 0; i < list.size(); i++ ) {
				NexLog.d(TAG, "getTargetStreamIDArrayList list.get(" + i + ").toString() : " + list.get(i).toString());
			}
		}
	}

	private void onAsyncCmdComplete(final NexPlayer player, int cmd, int result, int param1, int param2) {
		NexLog.d(TAG, "onAsyncCmdComplete mState : " + mState + " cmd : " + cmd + " result : " + result + " param1 : " + param1 + " param2 : " + param2);
		if( mState.equals(OfflineStoreState.NONE) )
			return;

		switch (cmd) {
			case NexPlayer.NEXPLAYER_ASYNC_CMD_OPEN_STORE_STREAM:
				if( result == 0 ) {
					if (mListener != null) {
						mListener.onContentInfoReady();
						NexLog.d(TAG, "onContentInfoReady : set trackID = " + bUseTrackIDSetting);
					}

					NexContentInformation contentInfo = player.getContentInfo();

					getTargetStreamIDArrayList(mTargetStreamIDArrayList, contentInfo, mOfflineStoreSettingData);
					mMaxNumOfRepetition = mTargetStreamIDArrayList.size() - 1;
					mNumOfRepetition = 0;
					if (mMaxNumOfRepetition >= 1 && mState == OfflineStoreState.CONTINUE_STORE) {
						mNumOfRepetition = mOfflineStoreSettingData.storePercentage / (100 / mTargetStreamIDArrayList.size());
					}
					mStartingNumOfRepetition = mNumOfRepetition;

					TargetStreamID streamIds = mTargetStreamIDArrayList.get(mNumOfRepetition);

					boolean bResult = false;
					if( bUseTrackIDSetting ) {
						// try to check audioTrackID in the audioStreamID
						if( mOfflineStoreSettingData.audioTrackID != getAudioTrackID(mOfflineStoreSettingData, streamIds.mAudioStreamID, contentInfo) ) {
							NexLog.d(TAG, "We haven't found the audioTrackID in the audioStreamID. So set it to the default trackID");
							setOfflineStoreSetting(NexOfflineStoreSetting.INTEGER_AUDIO_TRACK_ID, NexPlayer.MEDIA_TRACK_DEFAULT_ID);
						}

						bResult = setMediaStreamTrack(player, streamIds.mAudioStreamID, streamIds.mVideoStreamID, streamIds.mTextStreamID, streamIds.mCustomAttributeID,
								mOfflineStoreSettingData.audioTrackID, mOfflineStoreSettingData.preferLanguageAudio, mOfflineStoreSettingData.preferLanguageText);
					} else {
						bResult = setMediaStream(player, streamIds.mAudioStreamID, streamIds.mVideoStreamID, streamIds.mTextStreamID, streamIds.mCustomAttributeID,
								mOfflineStoreSettingData.preferLanguageAudio, mOfflineStoreSettingData.preferLanguageText);
					}

					if (bResult == false)
						player.start(0);
				} else {
					if( mListener != null )
						mListener.onError(NexPlayer.NexErrorCode.fromIntegerValue(result));
				}
				break;
			case NexPlayer.NEXPLAYER_ASYNC_CMD_START_STORE_STREAM:
				if( result == 0 ) {
					if( mListener != null ) {
						if( mNumOfRepetition == mStartingNumOfRepetition ) {
							mListener.onDownloadBegin();
							mListener.offlineStoreStarted();
						}
					}
				} else {
					if( mListener != null )
						mListener.onError(NexPlayer.NexErrorCode.fromIntegerValue(result));
				}
				break;
			case NexPlayer.NEXPLAYER_ASYNC_CMD_SET_MEDIA_STREAM_TRACK:
				if( result == 0 ) {
					NexLog.d(TAG, "setMediaStreamTrack : OK...");
					player.start(0);
				} else {
					if( mListener != null)
						mListener.onError(NexPlayer.NexErrorCode.fromIntegerValue(result));
				}
				break;
			case NexPlayer.NEXPLAYER_ASYNC_CMD_SET_MEDIA_STREAM:
				if( result == 0 ) {
					NexLog.d(TAG, "setMediaStream : OK...");
					player.start(0);
				} else {
					if( mListener != null )
						mListener.onError(NexPlayer.NexErrorCode.fromIntegerValue(result));
				}
				break;
				/*
			case NexPlayer.NEXPLAYER_ASYNC_CMD_SET_MEDIA_TRACK:
				if( result == 0 ) {
					NexLog.d(TAG, "setMediaTrack : OK, start player!!!");
					player.start(0);
				} else {
					if( mListener != null )
						mListener.onError(NexPlayer.NexErrorCode.fromIntegerValue(result));
				}
				break;
				*/
			case NexPlayer.NEXPLAYER_ASYNC_CMD_STOP:
				if( result == 0 ) {
					if( mMaxNumOfRepetition > mNumOfRepetition && mOfflineStoreSettingData.storePercentage == (int)((100.0f / mTargetStreamIDArrayList.size()) * (mNumOfRepetition +1)) ) {
						mNumOfRepetition++;
						TargetStreamID streamIds = mTargetStreamIDArrayList.get(mNumOfRepetition);
						setMediaStream(player, streamIds.mAudioStreamID, streamIds.mVideoStreamID, streamIds.mTextStreamID, streamIds.mCustomAttributeID,
								mOfflineStoreSettingData.preferLanguageAudio, mOfflineStoreSettingData.preferLanguageText);
						player.start(0);
					} else {
						boolean shouldDeInitStoreManager = false;
						boolean shouldDeInitRetrieveManager = false;

						switch (mState) {
							case CONTINUE_STORE:
								shouldDeInitStoreManager = shouldDeInitRetrieveManager = true;
								break;
							case STORE:
								shouldDeInitStoreManager = true;
								break;
						}

						if( mStoredInfoFD != null )
							NexStoredInfoFileUtils.updateStoredInfoFile(mStoredInfoFD, mOfflineStoreSettingData);
						else
							NexStoredInfoFileUtils.makeStoredInfoFile(mOfflineStoreSettingData);
						if( shouldDeInitStoreManager )
							NexPlayer.deinitStoreManagerMulti(player);
						if( shouldDeInitRetrieveManager )
							NexPlayer.deinitRetrieveManagerMulti(player);


						resetAllValuables();

						mHandler.post(new Runnable() {
							@Override
							public void run() {
								player.close();
							}
						});

						if( mListener != null ) {
							if( mOfflineStoreSettingData.storePercentage != 100 )
								mListener.onDownloadEnd(false);
							mListener.offlineStoreStopped();
						}
					}
				} else {
					if( mListener != null )
						mListener.onError(NexPlayer.NexErrorCode.fromIntegerValue(result));
				}
				break;
			case NexPlayer.NEXPLAYER_ASYNC_CMD_PAUSE:
				if( mListener != null )
					mListener.offlineStorePaused();
				break;
			case NexPlayer.NEXPLAYER_ASYNC_CMD_RESUME:
				if( mListener != null )
					mListener.offlineStoreResumed();
		}
	}

	private int startOfflineStore(Context context, NexPlayer player, NexSettingDataForStoring settings, int transportType,Map<String, String> optionalHeaders, List<String> httpHeaders) {
		boolean shouldOpenPlayer = true;
		mNexPlayer.setProperties(595, settings.parallelSegments);
		if(mNexPlayer != null && httpHeaders != null) {
			for(String httpHeader : httpHeaders) {
				mNexPlayer.addHTTPHeaderFields(httpHeader);
			}
		}

		if ( settings.storePercentage > 0 && settings.storePercentage < 100 ) {
			mState = OfflineStoreState.CONTINUE_STORE;
			NexPlayer.initRetrieveManagerMulti(player, settings.storePath);
			NexPlayer.initStoreManagerMulti(player, settings.storePath);
		} else if( settings.storePercentage == 100 ) {
			shouldOpenPlayer = false;
			if( mListener != null )
				mListener.onDownloadEnd(true);
		} else {
			mState = OfflineStoreState.STORE;

			File file = new File(settings.storePath);
			if( !file.exists() )
				file.mkdirs();

			NexPlayer.initStoreManagerMulti(player, settings.storePath);
		}

		int ret = NexPlayer.NexErrorCode.NONE.getIntegerCode();

		if( shouldOpenPlayer ) {
			ret = NexPlayer.NexErrorCode.INVALID_STATE.getIntegerCode();
			if( player != null && player.isInitialized() ) {
				ret = setupPlayerBeforeOpen(context, player, settings,optionalHeaders);

				if (player.getState() == NexPlayer.NEXPLAYER_STATE_CLOSED) {
					if (ret == NexPlayer.NexErrorCode.NONE.getIntegerCode())
						ret = player.open(settings.storeURL, null, null, NexPlayer.NEXPLAYER_SOURCE_TYPE_STORE_STREAM, transportType);
				}
			}
		}

		return ret;
	}

	// Widevine start
	private boolean checkWidevineDrmIntersection(int checkDrmType, int combinedDrmType) {
		boolean hasDrmType = false;

		if (checkDrmType == (checkDrmType & combinedDrmType)) {
			hasDrmType = true;
		}

		return hasDrmType;
	}
	// Widevine end

	private int setupPlayerBeforeOpen(Context context, NexPlayer player, final NexSettingDataForStoring settings,  Map<String, String> optionalHeaders) {
		int ret = NexPlayer.NexErrorCode.NONE.getIntegerCode();
		int[] bitrate = new int[1];
		bitrate[0] = settings.bandwidth;
		player.setVideoBitrates(bitrate);

		if( settings.audioStreamID == NexPlayer.MEDIA_STREAM_DEFAULT_ID && !TextUtils.isEmpty(settings.preferLanguageAudio) )
			player.setProperty(NexPlayer.NexProperty.PREFER_LANGUAGE_AUDIO, settings.preferLanguageAudio);
		else
			settings.preferLanguageAudio = "";

		if( settings.textStreamID == NexPlayer.MEDIA_STREAM_DEFAULT_ID && !TextUtils.isEmpty(settings.preferLanguageText) )
			player.setProperty(NexPlayer.NexProperty.PREFER_LANGUAGE_TEXT, settings.preferLanguageText);
		else
			settings.preferLanguageText = "";


		// NexMediaDrm Start
		if( checkWidevineDrmIntersection(1, settings.drmType) && !TextUtils.isEmpty(settings.mediaDrmKeyServer) ) {
			player.setNexMediaDrmKeyServerUri(settings.mediaDrmKeyServer);
		}
		// NexMediaDrm end

		//NexWVSWDrm start
		if(checkWidevineDrmIntersection(2, settings.drmType)) {
			NexWVDRM mNexWVDRM = new NexWVDRM();
			if(optionalHeaders != null)mNexWVDRM.setNexWVDrmOptionalHeaderFields(new HashMap<>(optionalHeaders));
			File fileDir = mContext.getFilesDir();
			String strCertPath = fileDir.getAbsolutePath() + "/wvcert";

			File certDirectory = new File(strCertPath);
			certDirectory.mkdir();

			String keyServer = settings.mediaDrmKeyServer;

			NexLog.d(TAG, "SWDRM: Proxy server addr is.. ( " + keyServer + " )");

			int offlineMode = 1;

			if(mNexWVDRM.initDRMManager(NexPlayer.getDefaultEngineLibPath(context), strCertPath, keyServer, offlineMode) == 0) {
				mNexWVDRM.enableWVDRMLogs(true);
				mNexWVDRM.setListener(new NexWVDRM.IWVDrmListener() {
					@Override
					public String onModifyKeyAttribute(String strKeyAttr) {
						String strAttr = strKeyAttr;
						String strRet = strKeyAttr;
						//modify here;
						NexLog.d(TAG, "Key Attr: " + strAttr);
						List<String> keyAttrArray = new ArrayList<String>();
						String strKeyElem = "";
						String strKeyRemain = "";
						int end = 0;
						while (true) {
							end = strAttr.indexOf("\n");
							if (end != -1 && end != 0) {
								strKeyElem = strAttr.substring(0, end);
								keyAttrArray.add(strKeyElem);
								strKeyRemain = strAttr.substring(end, strAttr.length());
								strAttr = strKeyRemain;
							} else if ((end == -1 || end == 0) && strKeyElem.isEmpty() == false) {
								keyAttrArray.add(strAttr.substring(0, strAttr.length()));
								break;
							} else {
								keyAttrArray.add(strAttr);
								break;
							}
						}

						for (int i = 0; i < keyAttrArray.size(); i++) {
							strKeyElem = keyAttrArray.get(i);
							if (strKeyElem.indexOf("com.widevine") != -1) {
								NexLog.d(TAG, "Found Key!");
								strRet = strKeyElem;
								break;
							}
						}

						return strRet;
					}
				});
			}
		}
		//NexWVSWDrm end

		// Widevine start
		mNexPlayer.setProperties(NexPlayer.NEXPLAYER_PROPERTY_ENABLE_MEDIA_DRM, settings.drmType);
		// Widevine end

		return ret;
	}

	private boolean shouldSetMediaStream(int videoStreamID, int audioStreamID, int textStreamID, int customAttrID, String audioPreferLanguage, String textPreferLanguage) {
		return videoStreamID != NexPlayer.MEDIA_STREAM_DEFAULT_ID ||
				(audioStreamID != NexPlayer.MEDIA_STREAM_DEFAULT_ID && TextUtils.isEmpty(audioPreferLanguage)) ||
				(textStreamID != NexPlayer.MEDIA_STREAM_DEFAULT_ID && TextUtils.isEmpty(textPreferLanguage)) ||
				customAttrID != NexPlayer.MEDIA_STREAM_DEFAULT_ID;
	}

	private boolean shouldSetMediaStreamTrack(int videoStreamID, int audioStreamID, int textStreamID, int customAttrID, int audioTrackID, String audioPreferLanguage, String textPreferLanguage) {
		return videoStreamID != NexPlayer.MEDIA_STREAM_DEFAULT_ID ||
				(audioStreamID != NexPlayer.MEDIA_STREAM_DEFAULT_ID && TextUtils.isEmpty(audioPreferLanguage)) ||
				(textStreamID != NexPlayer.MEDIA_STREAM_DEFAULT_ID && TextUtils.isEmpty(textPreferLanguage)) ||
				customAttrID != NexPlayer.MEDIA_STREAM_DEFAULT_ID ||
				audioTrackID != NexPlayer.MEDIA_TRACK_DEFAULT_ID;
	}

	private boolean shouldSetMediaTrack(int iMediaType, int audioTrackID) {
		if (iMediaType != MEDIA_TYPE_AUDIO) {
			NexLog.d(TAG, "media type is not AUDIO!");
			return false;
		}
		return audioTrackID != NexPlayer.MEDIA_TRACK_DEFAULT_ID;
	}

	private int getAudioTrackID(NexSettingDataForStoring settings, int streamId, NexContentInformation info) {
		int id = NexPlayer.MEDIA_TRACK_DEFAULT_ID;

		if( settings.audioTrackID != NexPlayer.MEDIA_TRACK_DEFAULT_ID ) {
			NexStreamInformation[] streamArray = info.mArrStreamInformation;
			for( NexStreamInformation curStream : streamArray ) {
				if( curStream.mType == NexPlayer.MEDIA_STREAM_TYPE_AUDIO && curStream.mID == streamId )
				{
					NexTrackInformation[] trackArray = curStream.mArrTrackInformation;
					for ( NexTrackInformation curTrack : trackArray ) {
						if ( curTrack.mTrackID == settings.audioTrackID )
							id = curTrack.mTrackID;
					}
				}
			}
		}

		NexLog.d(TAG, "getAudioTrackID StreamID : " + streamId + ", Found TrackID : " + id);
		return id;
	}

	private int getStreamID(NexSettingDataForStoring settings, int streamType, NexContentInformation info) {
		int id = NexPlayer.MEDIA_STREAM_DEFAULT_ID;

		switch (streamType) {
			case NexPlayer.MEDIA_STREAM_TYPE_AUDIO:
				if( settings.audioStreamID == NexPlayer.MEDIA_STREAM_DEFAULT_ID && !TextUtils.isEmpty(settings.preferLanguageAudio) ) {
					id = info.mCurrAudioStreamID;
				} else if( settings.audioStreamID != NexPlayer.MEDIA_STREAM_DEFAULT_ID ){
					NexStreamInformation[] streamArray = info.mArrStreamInformation;
					for( NexStreamInformation curStream : streamArray ) {
						if( curStream.mType == NexPlayer.MEDIA_STREAM_TYPE_AUDIO && curStream.mID == settings.audioStreamID )
							id = curStream.mID;
					}
				}
				break;
			case NexPlayer.MEDIA_STREAM_TYPE_VIDEO:
				if( settings.videoStreamID != NexPlayer.MEDIA_STREAM_DEFAULT_ID ) {
					NexStreamInformation[] streamArray = info.mArrStreamInformation;
					for (NexStreamInformation curStream : streamArray) {
						if (curStream.mType == NexPlayer.MEDIA_STREAM_TYPE_VIDEO && curStream.mID == settings.videoStreamID) {
							id = curStream.mID;
						}
					}
				}
				break;
			case NexPlayer.MEDIA_STREAM_TYPE_TEXT:
				if( settings.textStreamID == NexPlayer.MEDIA_STREAM_DEFAULT_ID && !TextUtils.isEmpty(settings.preferLanguageText) ) {
					id = info.mCurrTextStreamID;
				} else if( settings.textStreamID != NexPlayer.MEDIA_STREAM_DEFAULT_ID ) {
					NexStreamInformation[] streamArray = info.mArrStreamInformation;
					for (NexStreamInformation curStream : streamArray) {
						if (curStream.mType == NexPlayer.MEDIA_STREAM_TYPE_TEXT && curStream.mID == settings.textStreamID)
							id = curStream.mID;
					}
				}
				break;
		}

		NexLog.d(TAG, "getStreamID streamType : " + streamType + " id : " + id);

		return id;
	}

	private int getCustomAttrStreamID(NexSettingDataForStoring settings, NexContentInformation info) {
		int id = NexPlayer.MEDIA_STREAM_DEFAULT_ID;

		if( settings.customAttrID != id && settings.customAttrID != NexOfflineStoreSetting.STREAM_ID_ALL ) {
			NexStreamInformation[] streamArray = info.mArrStreamInformation;
			for (NexStreamInformation curStream : streamArray) {
				if (curStream.mType == NexPlayer.MEDIA_STREAM_TYPE_VIDEO && curStream.mID == settings.videoStreamID) {
					NexCustomAttribInformation[] attrArray = curStream.mArrCustomAttribInformation;
					for( NexCustomAttribInformation attrInfo : attrArray ) {
						if( attrInfo.mID == settings.customAttrID ) {
							id = settings.customAttrID;
						}
					}
				}
			}
		}

		return id;
	}

	private boolean setMediaStream(NexPlayer player, int audioStreamID, int videoStreamID, int textStreamID, int customAttrID, String audioPreferLanguage, String textPreferLanguage) {
		boolean ret;
		if( ret = shouldSetMediaStream(videoStreamID, audioStreamID, textStreamID, customAttrID, audioPreferLanguage, textPreferLanguage) ) {
			audioStreamID = TextUtils.isEmpty(audioPreferLanguage) ? audioStreamID : NexPlayer.MEDIA_STREAM_DEFAULT_ID;
			textStreamID = TextUtils.isEmpty(textPreferLanguage) ? textStreamID : NexPlayer.MEDIA_STREAM_DEFAULT_ID;

			player.setMediaStream(audioStreamID, textStreamID, videoStreamID, customAttrID);
		}
		return ret;
	}

	private boolean setMediaStreamTrack(NexPlayer player, int audioStreamID, int videoStreamID, int textStreamID, int customAttrID, int audioTrackID, String audioPreferLanguage, String textPreferLanguage) {
		boolean ret;
		if( ret = shouldSetMediaStreamTrack(videoStreamID, audioStreamID, textStreamID, customAttrID, audioTrackID, audioPreferLanguage, textPreferLanguage) ) {
			audioStreamID = TextUtils.isEmpty(audioPreferLanguage) ? audioStreamID : NexPlayer.MEDIA_STREAM_DEFAULT_ID;
			textStreamID = TextUtils.isEmpty(textPreferLanguage) ? textStreamID : NexPlayer.MEDIA_STREAM_DEFAULT_ID;

			player.setMediaStreamTrack(audioStreamID, textStreamID, videoStreamID, customAttrID, MEDIA_TYPE_AUDIO, audioTrackID);
		}
		return ret;
	}

	private boolean setMediaTrack(NexPlayer player, int iMediaType, int audioTrackID) {
		boolean ret;
		if( ret = shouldSetMediaTrack(iMediaType, audioTrackID)) {
			player.setMediaTrack(iMediaType, audioTrackID);
		}
		return ret;
	}

	private void resetAllValuables() {
		mState = OfflineStoreState.NONE;
	}
}

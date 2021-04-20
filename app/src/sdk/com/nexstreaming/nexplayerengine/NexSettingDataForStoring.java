package com.nexstreaming.nexplayerengine;


import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;

class NexSettingDataForStoring {
	int audioStreamID = NexPlayer.MEDIA_STREAM_DEFAULT_ID;
	int videoStreamID = NexPlayer.MEDIA_STREAM_DEFAULT_ID;
	int textStreamID = NexPlayer.MEDIA_STREAM_DEFAULT_ID;
	int customAttrID = NexPlayer.MEDIA_STREAM_DEFAULT_ID;
	int audioTrackID = NexPlayer.MEDIA_TRACK_DEFAULT_ID;

	String preferLanguageAudio = "";
	String preferLanguageText = "";

	int bandwidth = 3000;
	int storePercentage = 0;

	String storeURL = "";
	String storePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "NexPlayerCache" + File.separator;
	String storeInfoFile = Environment.getExternalStorageDirectory().getPath() + File.separator + "NexPlayerCache" + File.separator + "storeInfo.nex.store";


	String mediaDrmKeyServer = "";
	String offlineKeyID = "";
	int drmType = 0;
	int parallelSegments = 1;

	protected NexSettingDataForStoring() {}

	protected NexSettingDataForStoring(FileDescriptor fd) {
		JSONObject object = NexStoredInfoFileUtils.parseJSONObject(fd);
		if( object != null ) {
			try {
				bandwidth = object.getInt(NexStoredInfoFileUtils.STORED_INFO_KEY_BW);
				audioStreamID = object.getInt(NexStoredInfoFileUtils.STORED_INFO_KEY_AUDIO_STREAM_ID);
				//For backward compatibility, use the following function(optInt api).
				audioTrackID = object.optInt(NexStoredInfoFileUtils.STORED_INFO_KEY_AUDIO_TRACK_ID, -1);
				videoStreamID = object.getInt(NexStoredInfoFileUtils.STORED_INFO_KEY_VIDEO_STREAM_ID);
				textStreamID = object.getInt(NexStoredInfoFileUtils.STORED_INFO_KEY_TEXT_STREAM_ID);
				customAttrID = object.getInt(NexStoredInfoFileUtils.STORED_INFO_KEY_CUSTOM_ATTR_ID);
				storePercentage = object.getInt(NexStoredInfoFileUtils.STORED_INFO_KEY_STORE_PERCENTAGE);
				storeURL = object.getString(NexStoredInfoFileUtils.STORED_INFO_KEY_STORE_URL);
				storePath = object.getString(NexStoredInfoFileUtils.STORED_INFO_KEY_STORE_PATH);


				mediaDrmKeyServer = object.getString(NexStoredInfoFileUtils.STORED_INFO_KEY_MEDIA_DRM_KEY_SERVER_URI);
				offlineKeyID = object.getString(NexStoredInfoFileUtils.STORED_INFO_KEY_OFFLINE_KEY_ID);
				drmType = object.getInt(NexStoredInfoFileUtils.STORED_INFO_KEY_DRM_TYPE);
				parallelSegments = object.getInt(NexStoredInfoFileUtils.STORED_PARALLEL_SEGMENTS_TO_DOWNLOAD);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	protected NexSettingDataForStoring(File storeFile) {
		JSONObject object = NexStoredInfoFileUtils.parseJSONObject(storeFile);
		if( object != null ) {
			try {
				bandwidth = object.getInt(NexStoredInfoFileUtils.STORED_INFO_KEY_BW);
				audioStreamID = object.getInt(NexStoredInfoFileUtils.STORED_INFO_KEY_AUDIO_STREAM_ID);
				//For backward compatibility, use the following function(optInt api).
				audioTrackID = object.optInt(NexStoredInfoFileUtils.STORED_INFO_KEY_AUDIO_TRACK_ID, -1);
				videoStreamID = object.getInt(NexStoredInfoFileUtils.STORED_INFO_KEY_VIDEO_STREAM_ID);
				textStreamID = object.getInt(NexStoredInfoFileUtils.STORED_INFO_KEY_TEXT_STREAM_ID);
				customAttrID = object.getInt(NexStoredInfoFileUtils.STORED_INFO_KEY_CUSTOM_ATTR_ID);
				storePercentage = object.getInt(NexStoredInfoFileUtils.STORED_INFO_KEY_STORE_PERCENTAGE);
				storeURL = object.getString(NexStoredInfoFileUtils.STORED_INFO_KEY_STORE_URL);
				storePath = object.getString(NexStoredInfoFileUtils.STORED_INFO_KEY_STORE_PATH);
				storeInfoFile = storeFile.getAbsolutePath();
				preferLanguageAudio = object.getString(NexStoredInfoFileUtils.STORED_INFO_KEY_AUDIO_PREFER_LANGUAGE);
				preferLanguageText = object.getString(NexStoredInfoFileUtils.STORED_INFO_KEY_TEXT_PREFER_LANGUAGE);


				mediaDrmKeyServer = object.getString(NexStoredInfoFileUtils.STORED_INFO_KEY_MEDIA_DRM_KEY_SERVER_URI);
				offlineKeyID = object.getString(NexStoredInfoFileUtils.STORED_INFO_KEY_OFFLINE_KEY_ID);
				drmType = object.getInt(NexStoredInfoFileUtils.STORED_INFO_KEY_DRM_TYPE);
				parallelSegments = object.getInt(NexStoredInfoFileUtils.STORED_PARALLEL_SEGMENTS_TO_DOWNLOAD);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	protected void setValue(NexOfflineStoreController.NexOfflineStoreSetting setting, int value) {
		switch (setting) {
			case INTEGER_BANDWIDTH:
				bandwidth = value;
				break;
			case INTEGER_AUDIO_STREAM_ID:
				audioStreamID = value;
				break;
			case INTEGER_AUDIO_TRACK_ID:
				audioTrackID = value;
				break;
			case INTEGER_TEXT_STREAM_ID:
				textStreamID = value;
				break;
			case INTEGER_VIDEO_STREAM_ID:
				videoStreamID = value;
				break;
			case INTEGER_CUSTOM_ATTRIBUTE_ID:
				customAttrID = value;
				break;
			case INTEGER_DRM_TYPE:
				drmType = value;
				break;
			case PARALLEL_SEGMENTS_TO_DOWNLOAD:
				parallelSegments = value;
		}
	}

	protected void setValue(NexOfflineStoreController.NexOfflineStoreSetting setting, String value) {
		switch (setting) {
			case STRING_STORE_PATH:
				storePath = value;
				break;
			case STRING_PREFER_LANGUAGE_AUDIO:
				preferLanguageAudio = value;
				break;
			case STRING_PREFER_LANGUAGE_TEXT:
				preferLanguageText = value;
				break;
			case STRING_MEDIA_DRM_KEY_SERVER_URL:
				mediaDrmKeyServer = value;
				break;
			case STRING_OFFLINE_KEY_ID:
				offlineKeyID = value;
				break;
		}
	}
}

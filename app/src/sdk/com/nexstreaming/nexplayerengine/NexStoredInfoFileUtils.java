package com.nexstreaming.nexplayerengine;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * @brief This class parses the stored info files, which are created from Offline Storing, and gets data from them.
 */
public class NexStoredInfoFileUtils {
	private static final String TAG = "NexStoredInfoFileUtils";
	private static final String STORE_INFO_KEY = "NexOfflineStore";

	/**
	 * The stored URL as an \c integer.
	 */
	public static final String STORED_INFO_KEY_STORE_URL = "URL";
	/**
	 * The set bandwidth value as an \c integer.
	 */
	public static final String STORED_INFO_KEY_BW = "Bandwidth";
	/**
	 * The stored audio stream ID as an \c integer.
	 */
	public static final String STORED_INFO_KEY_AUDIO_STREAM_ID = "Audio_Stream_ID";
	/**
	 * The stored audio track ID as an \c integer.
	 */
	public static final String STORED_INFO_KEY_AUDIO_TRACK_ID = "Audio_Track_ID";
	/**
	 * The stored video stream ID store as an \c integer.
	 */
	public static final String STORED_INFO_KEY_VIDEO_STREAM_ID = "Video_Stream_ID";
	/**
	 * The stored text stream ID as an \c integer. 
	 */
	public static final String STORED_INFO_KEY_TEXT_STREAM_ID = "Text_Stream_ID";
	/**
	 * The stored custom attribute stream ID as an \c integer.
	 */
	public static final String STORED_INFO_KEY_CUSTOM_ATTR_ID = "Custom_Attr_ID";
	/**
	 * The preferred language of the stored audio as a \c string.
	 */
	public static final String STORED_INFO_KEY_AUDIO_PREFER_LANGUAGE = "Audio_Prefer_Language";
	/**
	 * The preferred language of the stored text as a \c string.
	 */
	public static final String STORED_INFO_KEY_TEXT_PREFER_LANGUAGE = "Text_Prefer_Language";
	/**
	 * The cache directory as a \c string.
	 */
	public static final String STORED_INFO_KEY_STORE_PATH = "Store_Path";
	/**
	 * The stored percentage as an \c integer.
	 */
	public static final String STORED_INFO_KEY_STORE_PERCENTAGE = "Store_Percentage";
	/**
	 * The key server’s URL as a \c string.
	 */
	public static final String STORED_INFO_KEY_MEDIA_DRM_KEY_SERVER_URI = "Media_DRM_Key_Server_URI";
	/**
	 * The key ID issued from NexPlayer.IOfflineKeyListener.onOfflineKeyStoreListener as a \c string.
	 */
	public static final String STORED_INFO_KEY_OFFLINE_KEY_ID = "Offline_Key_ID";

	public static final String STORED_INFO_KEY_DRM_TYPE = "DRM_Type";

	public static final String STORED_PARALLEL_SEGMENTS_TO_DOWNLOAD = "Parallel_Segments";

	private static String getStoredInfoFileContent(NexSettingDataForStoring settings) {
		JSONObject object = new JSONObject();
		String info = STORE_INFO_KEY + "\n";
		try {
			object.put(STORED_INFO_KEY_STORE_URL, settings.storeURL);
			object.put(STORED_INFO_KEY_STORE_PATH, settings.storePath);
			object.put(STORED_INFO_KEY_STORE_PERCENTAGE, settings.storePercentage);
			object.put(STORED_INFO_KEY_BW, settings.bandwidth);
			object.put(STORED_INFO_KEY_AUDIO_STREAM_ID, settings.audioStreamID);
			object.put(STORED_INFO_KEY_AUDIO_TRACK_ID, settings.audioTrackID);
			object.put(STORED_INFO_KEY_VIDEO_STREAM_ID, settings.videoStreamID);
			object.put(STORED_INFO_KEY_TEXT_STREAM_ID, settings.textStreamID);
			object.put(STORED_INFO_KEY_CUSTOM_ATTR_ID, settings.customAttrID);
			object.put(STORED_INFO_KEY_TEXT_PREFER_LANGUAGE, settings.preferLanguageText);
			object.put(STORED_INFO_KEY_AUDIO_PREFER_LANGUAGE, settings.preferLanguageAudio);
			object.put(STORED_INFO_KEY_MEDIA_DRM_KEY_SERVER_URI, settings.mediaDrmKeyServer);
			object.put(STORED_INFO_KEY_OFFLINE_KEY_ID, settings.offlineKeyID);
			object.put(STORED_INFO_KEY_DRM_TYPE, settings.drmType);
			object.put(STORED_PARALLEL_SEGMENTS_TO_DOWNLOAD, settings.parallelSegments);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return info + object.toString();
	}

	/**
	 * @brief This method parses the store info file as JSONObject.
	 *
	 * @param storedInfoFD The file descriptor of the info file created from storing.
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp; error code in the event of a failure.
	 */
	public static JSONObject parseJSONObject(FileDescriptor storedInfoFD) {
		JSONObject object = null;
		if( storedInfoFD != null ) {
			BufferedReader reader = new BufferedReader(new FileReader(storedInfoFD));
			try {
				String key = "";
				if((key = reader.readLine()) != null && key.trim().equals(STORE_INFO_KEY)) {
					String info = reader.readLine();
					NexLog.d(TAG, "parseJSONObject info : " + info);
					if( info != null ) {
						info = info.trim();
						object = new JSONObject(info);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return object;
	}

	/**
	 * @brief This method parses the store info file as JSONObject. 
	 *
	 * @param storedInfoFile The file descriptor of the info file created from storing.
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp; error code in the event of a failure.
	 */
	public static JSONObject parseJSONObject(File storedInfoFile) {
		JSONObject object = null;
		if( storedInfoFile != null && storedInfoFile.exists() ) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(storedInfoFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			if( reader != null ) {
				String keyword = null;
				try {
					if( (keyword = reader.readLine()) != null && keyword.equals(STORE_INFO_KEY) ) {
						String info = reader.readLine();
						NexLog.d(TAG, "parseJSONObject info : " + info);
						if( info != null ) {
							info = info.trim();
							object = new JSONObject(info);
						}
					}
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		return object;
	}

	protected static int makeStoredInfoFile(NexSettingDataForStoring settings) {
		int ret = 0;

		try {
			File outputFile = new File(settings.storeInfoFile);
			if( outputFile.exists() ) {
				outputFile.delete();
			} else {
				String parentPath = settings.storeInfoFile.substring(0, settings.storeInfoFile.lastIndexOf(File.separator));
				File file = new File(parentPath);
				if( !file.exists() )
					file.mkdirs();
			}

			NexLog.d(TAG, "makeStoredInfoFile outputFile : " + outputFile.getAbsolutePath());
			FileOutputStream outputStream = new FileOutputStream(outputFile);
			outputStream.write(getStoredInfoFileContent(settings).getBytes());
			outputStream.close();
		} catch (FileNotFoundException e) {
			ret = -1;
			e.printStackTrace();
		} catch (IOException e) {
			ret = -1;
			e.printStackTrace();
		}

		NexLog.d(TAG, "makeStoredInfoFile ret : " + ret);
		return ret;
	}

	protected static int updateStoredInfoFile(FileDescriptor fd, NexSettingDataForStoring settings) {
		int ret = 0;

		try {
			NexLog.d(TAG, "updateStoredInfoFile fd : " + fd);
			FileOutputStream outputStream = new FileOutputStream(fd);
			outputStream.write(getStoredInfoFileContent(settings).getBytes());
			outputStream.close();
		} catch (FileNotFoundException e) {
			ret = -1;
			e.printStackTrace();
		} catch (IOException e) {
			ret = -1;
			e.printStackTrace();
		}

		NexLog.d(TAG, "updateStoredInfoFile ret : " + ret);
		return ret;
	}

	private static void deleteFolder(File file) {
		if( file != null && file.exists() ) {
			File[] childFileList = file.listFiles();
			if( childFileList != null ) {
				for( File childFile : childFileList ) {
					if( childFile.isDirectory() ) {
						deleteFolder(childFile);
					} else {
						childFile.delete();
					}
				}
			}
			file.delete();
		}
	}

	protected static int deleteOfflineCache(File storedInfoFile) {
		NexLog.d(TAG, "deleteOfflineCache storedInfoFile : " + storedInfoFile);
		JSONObject storedInfo = parseJSONObject(storedInfoFile);
		int ret = -1;

		if( storedInfo != null ) {
			String cachePath = null;
			try {
				cachePath = storedInfo.getString(STORED_INFO_KEY_STORE_PATH);
				NexLog.d(TAG, "deleteOfflineCache cachePath : " + cachePath);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if( cachePath != null ) {
				deleteFolder(new File(cachePath));
				ret = storedInfoFile.delete() ? 0 : -1;
			}
		}

		return ret;
	}
}

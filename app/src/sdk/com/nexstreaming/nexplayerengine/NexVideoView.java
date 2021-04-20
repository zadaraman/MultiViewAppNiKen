package com.nexstreaming.nexplayerengine;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.nexstreaming.nexplayerengine.NexCaptionRenderView.RenderingArea;

/**
 * @brief This class allows to use NexPlayer&tm;SDK easily on the application.
 *
 * \c NexVideoView class contains \c VideoRenderer, \c Caption Renderer, \c NexPlayer, \c NexAlFactory classes so that the  
 * developer can play the media content by only using a \c NexVideoView object.  
 *
 * @since version 6.42
 */
public class NexVideoView extends RelativeLayout implements MediaController.MediaPlayerControl  {
	private static final String LOG_TAG = "NexVideoView";
	private static final Handler mHandler = new Handler();

	private static final int MEDIA_CONTROLLER_TIMEOUT_SEC = 5000;

	public static final int STREAM_TYPE_VIDEO = 2;
	public static final int STREAM_TYPE_AUDIO = 1;
	public static final int STREAM_TYPE_TEXT = 0;

	private Context mContext = null;

	private NexMediaPlayer mPlayer = null;
	private MediaController mMediaController = null;
	private NexVideoViewFactory.INexVideoView mVideoRenderView = null;
	private NexCaptionRenderView mCaptionRenderView = null;
	private NexABRController mABRController = null;
	private Settings mSettings = null;
	private Application.ActivityLifecycleCallbacks mActivityLifecycleCallbacks = null;

	private OnConfigurationListener mConfigurationListener = null;
	private OnPreparedListener mPreparedListener = null;
	private OnCompletionListener mCompletionListener = null;
	private OnErrorListener mErrorListener = null;
	private OnStartCompleteListener mStartCompleteListener = null;
	private OnStopCompleteListener mStopCompleteListener = null;
	private OnPauseCompleteListener mPauseCompleteListener = null;
	private OnResumeCompleteListener mResumeCompleteListener = null;
	private OnMediaStreamChangedListener mMediaStreamChangedListener = null;
	private OnFastPlayListener mFastPlayListener = null;
	private OnExternalSubtitleChangedListener mExternalSubtitleChangedListener = null;
	private OnSeekCompleteListener mSeekCompleteListener = null;
	private OnDynamicThumbnailListener mDynamicThumbnailListener = null;
	private OnTimedMetaRenderRenderListener mTimedMetaRenderRenderListener = null;
	private OnInfoListener mInfoListener = null;
	private OnBufferingUpdateListener mBufferingUpdateListener = null;
	private OnTimeUpdateListener mTimeUpdateListener = null;

	private Point mVideoSize = null;
	private boolean mIsInitialized = false;
	private RenderingArea mRenderingArea;
	private int mCaptionType = NexClosedCaption.TEXT_TYPE_GENERAL;

	private String  mediaDrmLicenseServer = null;
	private String  wideVineLicenseServer = null;

	//NexWVSWDrm start
	private NexWVDRM mNexWVDRM = null;
	//NexWVSWDrm end

	private int mRotationDegree = 0;

	/**
	 * @brief This enumeration defines the possible scaling mode for output video and captions.
	 *
	 * - <b>\c SCALE_ASPECT_FIT</b>: The size of the output video and caption will be adjusted according to the size of the video view, keeping its original aspect ratio.
	 * - <b>\c SCALE_TO_FILL</b>: The output video and caption will fill the video view, ignoring its original aspect ratio.
	 *
	 * @since version 6.42
	 *
	 */
	public enum ScalingMode {
		SCALE_ASPECT_FIT(0), SCALE_TO_FILL(1), SCALE_ASPECT_FILL(2);

		int mMode;

		ScalingMode(int mode) {
			mMode = mode;
		}

		public int getInteger() {
			return mMode;
		}
	}
	private ScalingMode mScalingMode = ScalingMode.SCALE_ASPECT_FIT;

	/**
	 * @brief Constructor for \c NexVideoView.
	 *
	 * @param context The {@link android.content.Context Context} instance associated with the activity
	 * 		that will contain this view.
	 *
	 * @since version 6.42
	 */
	public NexVideoView(Context context) {
		super(context);
		mContext = context;
		initInternal(context);
	}
	/**
	 * @brief Constructor for \c NexVideoView.
	 *
	 * @see NexVideoView.NexVideoView(android.content.Context)
	 *
	 * @since version 6.42
	 */
	public NexVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initInternal(context);
	}

	private void initInternal(Context context) {
		mVideoSize = new Point();

		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ) {
			registerActivityLifecycleCallbacks((Activity)context);
		}

		mCaptionRenderView = new NexCaptionRenderView(context);

		mPlayer = new NexMediaPlayer(context);
		mABRController = new NexABRController(mPlayer.getPlayer());
		mSettings = new Settings();
		mRenderingArea = new RenderingArea();
		mPlayer.setListener(mPlayerIListener);

		mVideoRenderView = new NexVideoRenderer(mContext);

		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		this.addView((View)mVideoRenderView, params);
		mVideoRenderView.setListener(mVideoRendererListener);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void registerActivityLifecycleCallbacks(Activity activity) {
		mActivityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {

			@Override
			public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
			}

			@Override
			public void onActivityStarted(Activity activity) {
			}

			@Override
			public void onActivityResumed(Activity activity) {
				if( mContext == activity ) {
					NexLog.d(LOG_TAG, "onActivityResumed");
					resumeVideoRenderView();
				}
			}

			@Override
			public void onActivityPaused(Activity activity) {
				if( mContext == activity ) {
					NexLog.d(LOG_TAG, "onActivityPaused");
					pauseVideoRenderView();
				}
			}

			@Override
			public void onActivityStopped(Activity activity) {
			}

			@Override
			public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

			}

			@Override
			public void onActivityDestroyed(Activity activity) {

			}
		};

		activity.getApplication().registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void unregisterActivityLifecycleCallbacks(Activity activity) {
		activity.getApplication().unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
	}

	private boolean init(NexMediaPlayer playerController, Settings settings) {
		int codecMode = 3;
		String renderMode = NexPlayer.NEX_DEVICE_USE_AUTO;


		boolean ret = playerController.init(settings.mLogLevel, codecMode, renderMode);
		NexLog.d(LOG_TAG, "init playerController.init() returned " + ret);

		if( ret ) {
			initVideoRenderView(playerController);
			setupCaptionRenderView(mCaptionRenderView);
		}

		return ret;
	}

	private void setupCaptionRenderView(NexCaptionRenderView captionRenderView) {
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		this.addView(captionRenderView.getCaptionPainter(), params);
	}

	private void initVideoRenderView(NexMediaPlayer playerController) {
		int pixelFormat = mSettings.mPixelFormat;


		mVideoRenderView.setScreenPixelFormat(pixelFormat);
		mVideoRenderView.init(playerController.getPlayer());
		mVideoRenderView.setVisibility(View.VISIBLE);
	}

	/**
	 * @brief This method gets the type of the current caption in use.
	 *
	 * @return The type of the current caption.
	 * The return value will be one from the following values :
	 *			- TEXT_TYPE_UNKNOWN
	 *			- TEXT_TYPE_GENERAL
	 *			- TEXT_TYPE_EXTERNAL_TTML
	 *			- TEXT_TYPE_ATSCMH_CC
	 *			- TEXT_TYPE_ATSCMH_BAR
	 *			- TEXT_TYPE_ATSCMH_AFD
	 *			- TEXT_TYPE_NTSC_CC_CH1
	 *			- TEXT_TYPE_NTSC_CC_CH2
	 *			- TEXT_TYPE_3GPP_TIMEDTEXT
	 *			- TEXT_TYPE_TTML_TIMEDTEXT
	 *			- TEXT_TYPE_WEBVTT
	 *			- TEXT_TYPE_SMI
	 *			- TEXT_TYPE_SRT
	 *
	 * @since version 6.42
	 */
	public int getCaptionType() {
		return mCaptionType;
	}

	/**
	 * This method allows the subtitle file for a particular content to be changed during playback.
	 *
	 * This method operates asynchronously. When \c addSubtitleSource has completed and if \c OnExternalSubtitleChangedListener is registered
	 * on the \c NexVideoView, a \c onExternalSubtitleChanged event will be called.
	 *
	 * @param subtitlePath The path to the new subtitle file or the URL to load the new subtitle file.
	 *
	 * @since version 6.42
	 *
	 */
	public void addSubtitleSource(String subtitlePath) {
		NexLog.d(LOG_TAG, "addSubtitleSource subtitlePath : " + subtitlePath);
		mPlayer.getPlayer().changeSubtitlePath(subtitlePath);
		mCaptionRenderView.clearCaptionString();

	}

	public void addSubtitleSource(FileDescriptor fd, long offset, long length) {
		NexLog.d(LOG_TAG, "addSubtitleSource fd : " + fd + " offset : " + offset + " length : " + length);
		if( fd != null ) {
			mPlayer.getPlayer().changeSubtitleFD(fd, offset, length);
			mCaptionRenderView.clearCaptionString();
		}
	}

	public void addSubtitleSource(AssetFileDescriptor afd) {
		addSubtitleSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
	}

	/**
	 * @brief This method sets \c MediaController to \c NexVideoView to control the NexPlayer&tm;.
	 *
	 * @param controller A \c MediaController object to connect to NexPlayer&tm;.
	 *
	 * @since version 6.42
	 */
	public void setMediaController(MediaController controller) {
		NexLog.d(LOG_TAG, "setMediaController controller : " + controller);
		hideMediaController();
		mMediaController = controller;
		attachMediaController();
	}

	private void attachMediaController() {
		if ( mPlayer != null && mMediaController != null ) {
			mMediaController.setMediaPlayer(this);
			View anchorView = this.getParent() instanceof View ?
					(View)this.getParent() : this;
			mMediaController.setAnchorView(anchorView);
			mMediaController.setEnabled(isInPlaybackState());
		}
	}

	private boolean isInPlaybackState() {
		return mPlayer.getPlayer() != null &&
				mPlayer.getState() > NexPlayer.NEXPLAYER_STATE_STOP;
	}

	/**
	 * @brief Starts playing media from the specified timestamp.
	 *
	 * This method must be called after \c onPrepared() method has been successfully called by \c setVideoPath or \c setVideoURI.
	 *
	 * This method operates asynchronously therefore,
	 * when \c start() is successful, and if \c OnStartCompleteListener is registered on \c NexVideoView, a \c onStartComplete event will be called.
	 * Otherwise, when error occurs, and if \c OnErrorListener is registered on \c NexVideoView, an \c onError event will be called.
	 *
	 * @since version 6.42
	 *
	 */
	@Override
	public void start() {
		mPlayer.start(0);
	}

	/**
	 * @brief Starts playing media from the specified timestamp.
	 *
	 * This method must be called after \c onPrepared() method has been successfully called by \c setVideoPath or \c setVideoURI.
	 *
	 * This method operates asynchronously and when \c start() is successful, and if \c OnStartCompleteListener is registered on \c NexVideoView, a \c onStartComplete event will be called.
	 * Otherwise, when error occurs, and if \c OnErrorListener is registered on \c NexVideoView, an \c onError event will be called.
	 *
	 * @param msec The offset (in milliseconds) from the beginning of the media at which to start playback. This should be zero to start at the beginning.
	 *
	 * @since version 6.42
	 *
	 */
	public void start(int msec) {
		mPlayer.start(msec);
	}

	/**
	 * @brief This method sets the scaling mode for both output video and caption.
	 *
	 * @param mode The scaling mode of current video and caption. One of the following scaling mode :
	 *  - <b>ScalingMode.SCALE_ASPECT_FIT</b>
	 *  - <b>ScalingMode.SCALE_TO_FILL</b> \n
	 *
	 * The default scaling mode is \c ScalingMode.SCALE_ASPECT_FIT
	 *
	 * @since version 6.42
	 */
	public void setScalingMode(ScalingMode mode) {
		NexLog.d(LOG_TAG, "setScalingMode mode : " + mode);
		mScalingMode = mode;
		setOutputPos(mVideoSize.x, mVideoSize.y, mScalingMode);
	}

	/**
	 * @brief This method returns the scaling mode set to video and caption currently displayed on the output screen.
	 *
	 * @return The scaling mode set to current video and caption.
	 *
	 * @see ScalingMode
	 *
	 * @since version 6.42
	 */
	public ScalingMode getScalingMode() {
		return mScalingMode;
	}

	/**
	 * @brief This method pauses the current playback.
	 *
	 * Please note that when the hardware codec is in use, if the application is sent to the background by pressing the home button,
	 * pausing and resuming the playback may return an error because of resource limitations.
	 * To avoid this potential issue, stopping and starting playback is recommended in algorithm handling
	 * this specific case of the application being sent to the background because the home button was pressed.
	 *
	 * @since version 6.42
	 */
	@Override
	public void pause() {
		mPlayer.pause();
	}

	/**
	 * @brief This method gets the total duration of a media content.
	 * For live contents, the seekable range will be returned.
	 *
	 * @return The total duration of a media content, in \c milliseconds (msec).
	 *
	 * @since version 6.42
	 */
	@Override
	public int getDuration() {
		return mPlayer.getDuration();
	}

	/**
	 * @brief This method gets the current play time position of NexPlayer&tm; in the given content.
	 *
	 * This method can be called at any time to check the current position.
	 *
	 * @return The current play time position in milliseconds (msec).
	 *
	 * @since version 6.42
	 */
	@Override
	public int getCurrentPosition() {
		return mPlayer.getCurrentPosition();
	}

	/**
	 * @brief This method seeks the playback position to the specified time.
	 *
	 * This method will operate when the NexPlayer&tm; is playing or paused but will not operate when
	 * NexPlayer&tm; has stopped or if the live steam doesn't support seeking.
	 * This method operates asynchronously and when \c seekTo() is successful, and if \c OnSeekCompleteListener is registered on \c NexVideoView, a \c onSeekComplete event will be called.
	 *
	 * @param msec The offset in \c milliseconds from the beginning of the media to which the playback position should seek.
	 *
	 * @since version 6.42
	 */
	@Override
	public void seekTo(int msec) {
		mPlayer.seekTo(msec);
		mCaptionRenderView.clearCaptionString();
	}

	/**
	 * @brief This method determines whether the media content is currently being played.
	 *
	 * @return \c true if NexPlayer's state returns NexPlayer.NEXPLAYER_STATE_PLAY, otherwise \c false.
	 *
	 * @since version 6.42
	 */
	@Override
	public boolean isPlaying() {
		return mPlayer.isPlaying();
	}


	/**
	 * @brief This method determines the amount of currently buffered data, in percentage.
	 *
	 * It calculates the percentage of data that has been buffered with the cts of the last frame in buffer.
	 * Note that CTS stands for "Current Time Stamp".
	 *
	 * @return The percentage of buffered data.
	 *
	 * @since version 6.42
	 */
	@Override
	public int getBufferPercentage() {
		return mPlayer.getBufferPercentage();
	}

	/**
	 * @brief This method determines whether the current media content can be paused.
	 *
	 * @return \c true if the content can be paused, otherwise \c false.
	 *
	 * @since version 6.42
	 */
	@Override
	public boolean canPause() {
		return mPlayer.canPause();
	}

	/**
	 * @brief This method determines whether the media content allows to seek backward.
	 *
	 * @return \c true if the content allows to seek backward, otherwise \c false.
	 *
	 * @since version 6.42
	 */
	@Override
	public boolean canSeekBackward() {
		return mPlayer.canSeekBackward();
	}

	/**
	 * @brief This method determines whether the media content allows to seek forward.
	 *
	 * @return \c true if the content allows to seek forward, otherwise \c false.
	 *
	 * @since version 6.42
	 */
	@Override
	public boolean canSeekForward() {
		return mPlayer.canSeekForward();
	}

	/**
	 * @brief This method gets an audio session ID in order to use Android's audio effects with the NexPlayer&tm;.
	 *
	 * @see NexPlayer.getAudioSessionId
	 *
	 * @since version 6.42
	 */
	@Override
	public int getAudioSessionId() {
		return mPlayer.getPlayer().getAudioSessionId();
	}

	public int setDataSource(FileDescriptor fd, long offset, long length) {
		NexLog.d(LOG_TAG, "setDataSource fd : " + fd + " offset : " + offset + " length : " + length);
		int ret = -1;

		if( !mIsInitialized ) {
			mIsInitialized = init(mPlayer, mSettings);
		}

		if( mIsInitialized ) {
			NexPlayer player = mPlayer.getPlayer();

			mPlayer.close();
			if( mPlayer.getState() == NexPlayer.NEXPLAYER_STATE_CLOSED ) {
				if( mConfigurationListener != null)
					mConfigurationListener.onConfiguration();

				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if ( mMediaController != null )
							mMediaController.setEnabled(false);
					}
				});

				NexLog.d(LOG_TAG, "openFD");
				ret = mPlayer.setDataSource(fd, offset, length);
			}
		}

		return ret;
	}

	public int setDataSource(AssetFileDescriptor afd) {
		return setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
	}

	/**
	 * @brief This method begins opening the media at the specified path or URL.
	 *
	 * This method operates asynchronously, and when \c setVideoPath has successfully completed, and if \c OnPreparedListener is registered on \c NexVideoView, an \c onPrepared event will be called.
	 * Otherwise, when error occurs, and if \c OnErrorListener is registered on \c NexVideoView, an \c onError event will be called.
	 *
	 * @note To change the setting value of \c NexvideoView, \c setSettings should be called before calling this method.
	 *
	 * @param path The location of the content: a path (for local content) or URL (for remote content).
	 *
	 * @since version 6.42
	 */
	public void setVideoPath(String path) {
		setVideoPath(path, null);
	}

	private void setVideoPath(String path, Map<String, String> headers) {
		if( !mIsInitialized ) {
			mIsInitialized = init(mPlayer, mSettings);
		}

		if( mIsInitialized ) {
			if( path != null && path.length() > 0 ) {
				NexPlayer player = mPlayer.getPlayer();

				mPlayer.close();
				if( mPlayer.getState() == NexPlayer.NEXPLAYER_STATE_CLOSED ) {

					mPlayer.addHTTPHeaderFields(headers);

					if( mConfigurationListener != null)
						mConfigurationListener.onConfiguration();

					mHandler.post(new Runnable() {
						@Override
						public void run() {
							if ( mMediaController != null )
								mMediaController.setEnabled(false);
						}
					});

					NexLog.d(LOG_TAG, "open");
					int srcType = isStreaming(path) ? NexPlayer.NEXPLAYER_SOURCE_TYPE_STREAMING : NexPlayer.NEXPLAYER_SOURCE_TYPE_LOCAL_NORMAL;
					int transportType = mSettings.mUseUDP ? NexPlayer.NEXPLAYER_TRANSPORT_TYPE_TCP : NexPlayer.NEXPLAYER_TRANSPORT_TYPE_UDP;
					mCaptionType = NexClosedCaption.TEXT_TYPE_GENERAL;

					// Check DRM
					// NexMediaDrm Start
					if(mediaDrmLicenseServer != null) {
						NexLog.d(LOG_TAG, "setNexMediaDrmKeyServerUri ( " + mediaDrmLicenseServer + " )");
						player.setNexMediaDrmKeyServerUri(mediaDrmLicenseServer);
					}
					// NexMediaDrm end

					//NexWVSWDrm start
					if (wideVineLicenseServer != null) {
						NexLog.d(LOG_TAG, "setWideVineDrmKeyServerUrl ( " + wideVineLicenseServer + " ) ");

						if (mNexWVDRM != null) {
							mNexWVDRM.releaseDRMManager();
							mNexWVDRM = null;
						}
						mNexWVDRM = new NexWVDRM();
						File fileDir = mContext.getFilesDir();
						String strCertPath = fileDir.getAbsolutePath() + "/wvcert";

						String keyServer = wideVineLicenseServer;
						NexLog.d(LOG_TAG, "SWDRM: Proxy server addr is.. ( " + keyServer + " )");

						int offlineMode = 0;

						mNexWVDRM.initDRMManager(NexPlayer.getDefaultEngineLibPath(mContext), strCertPath, keyServer, offlineMode);
						mNexWVDRM.enableWVDRMLogs(true);
						mNexWVDRM.setListener(new NexWVDRM.IWVDrmListener() {
							@Override
							public String onModifyKeyAttribute(String strKeyAttr) {
								String strAttr = strKeyAttr;
								String strRet = strKeyAttr;
								//modify here;
								NexLog.d(LOG_TAG, "Key Attr: " + strAttr);
								List<String> keyAttrArray = new ArrayList<String>();
								String strKeyElem = "";
								String strKeyRemain = "";
								int end = 0;
								boolean bFound = false;
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
										NexLog.d(LOG_TAG, "Found Key!");
										strRet = strKeyElem;
										break;
									}
								}
								return strRet;
							}
						});

					}
					//NexWVSWDrm end

					player.open(path, null, null, srcType, transportType);
				}
			}
		}
	}

	/**
	 * @brief This method begins opening the media at a given URI.
	 *
	 * This method operates asynchronously, and when \c setVideoPath has successfully completed, and if \c OnPreparedListener is registered on \c NexVideoView, an \c onPrepared event will be called.
	 * Otherwise, when error occurs, and if \c OnErrorListener is registered on \c NexVideoView, an \c onError event will be called.
	 *
	 * @note To change the setting value of \c NexvideoView, \c setSettings should be called before calling this method.
	 *
	 * @param uri The URI of the video.
	 *
	 * @since version 6.42
	 */
	public void setVideoURI(Uri uri) {
		setVideoURI(uri, null);
	}

	/**
	 * @brief This method sets the video URI using specific headers and begins opening the media at a given URI.
	 *
	 * This method operates asynchronously, and when \c setVideoPath has successfully completed, and if \c OnPreparedListener is registered on \c NexVideoView, an \c onPrepared event will be called.
	 * Otherwise, when error occurs, and if \c OnErrorListener is registered on \c NexVideoView, an \c onError event will be called.
	 *
	 * @note To change the setting value of \c NexvideoView, \c setSettings should be called before calling this method.
	 *
	 * @param uri The URI of the video.
	 * @param headers The headers for requesting the URI. The header formats are specified on {@link NexPlayer.addHTTPHeaderFields addHTTPHeaderFields}.
	 *
	 * @since version 6.42
	 */
	public void setVideoURI(Uri uri, Map<String, String> headers) {
		NexLog.d(LOG_TAG, "setVideoURI uri : " + uri + " headers : " + headers);
		String path = "";
		String proj[] = { MediaStore.Video.Media._ID,
				MediaStore.Video.Media.DATA};

		ContentResolver cr = mContext.getContentResolver();
		Cursor videoCursor = null;
		try {
			videoCursor = cr.query(uri, proj, null, null, null);
		} catch(Exception e) {
			NexLog.e(LOG_TAG, "Exception - getIntentExtra() : " + e.getMessage());
		}

		if ( videoCursor != null && videoCursor.moveToFirst() ) {
			int videoDataCol = videoCursor
					.getColumnIndex(MediaStore.Video.Media.DATA);
			String url = videoCursor.getString(videoDataCol);

			if ( url != null && url.length() > 0 ) {
				path = url;
			}

			videoCursor.close();
		} else if ( "file".equals(uri.getScheme()) )
			path = uri.getPath();
		else
			path = Uri.decode(uri.toString());

		setVideoPath(path, headers);
	}

	/**
	 * @brief This method resumes the playback, beginning at the point at which the player was last paused.
	 *
	 * @since version 6.42
	 */
	public void resume() {
		mPlayer.resume();
	}

	/**
	 * @brief This method stops the current playback.
	 *
	 * This method operates asynchronously, and when \c stop has successfully completed, and if \c OnStopCompleteListener is registered on \c NexVideoView, an \c onStopComplete event will be called.
	 *
	 * @since version 6.42
	 */
	public void stopPlayback() {
		mPlayer.stop();
		mCaptionRenderView.clearCaptionString();
	}

	/**
	 * @brief This method activates the \c fastPlay feature in HLS content.
	 *
	 * This method operates asynchronously, and when \c fastPlayStart has successfully completed, and if \c OnFastPlayListener is registered on \c NexVideoView, an \c onFastPlayStart event will be called.
	 *
	 * @param msec The time in the content at which to start \c fastPlay, in \c milliseconds.
	 * @param rate The speed at which the video will play in \c fastPlay mode.
	 *                This speed is indicated by any float value (but NOT zero),
	 *                where negative values rewind the video at faster than normal playback speed and positive values play the video faster than normal (like fast forward).
	 *                For example:
	 *            -<b> rate = 3.0</b> (fastPlay plays video at 3x normal speed)
	 *            -<b> rate = - 2.0</b> (fastPlay rewinds video at 2x normal speed)
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
	 *
	 * @see NexPlayer.fastPlaystart
	 *
	 * @since version 6.42
	 */
	public int fastPlayStart(int msec, float rate) {
		int ret = mPlayer.fastPlayStart(msec, rate);

		NexLog.d(LOG_TAG, "fastPlayStart msec : " + msec + " rate : " + rate + " ret : " + ret);
		if( ret == 0 ) {
			mCaptionRenderView.clearCaptionString();
		}

		return ret;
	}

	/**
	 * @brief This method turns off the \c fastPlay feature in HLS content.
	 * Once the \c fastPlay feature has been activated by calling \c fastPlayStart, this method must be called in order to stop \c fastPlay.
	 *
	 * This method operates asynchronously, and when \c fastPlayStop has successfully completed, and if \c OnFastPlayListener is registered on \c NexVideoView, an \c onFastPlayStop event will be called.
	 *
	 * @param bResume This boolean value sets whether to resume playback after \c fastPlay or not.
	 *                   Set to 1 to automatically resume playback when fastPlay stops.
	 *                   Set to 0 to pause content when \c fastPlay stops.
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
	 *
	 * @since version 6.42
	 */
	public int fastPlayStop(boolean bResume) {
		NexLog.d(LOG_TAG, "fastPlayStop bResume : " + bResume);
		return mPlayer.fastPlayStop(bResume);
	}

	/**
	 * @brief This method sets the video playback rate for the \c fastPlay feature.
	 *
	 * @param rate This boolean value sets whether to resume playback after \c fastPlay or not.
	 *                   Set to 1 to automatically resume playback when \c fastPlay stops.
	 *                   Set to 0 to pause content when \c fastPlay stops.
	 *
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
	 *
	 * @see NexPlayer.fastPlaySetPlaybackRate
	 *
	 * @since version 6.42
	 */
	public int fastPlaySetPlaybackRate(float rate) {
		NexLog.d(LOG_TAG, "fastPlaySetPlaybackRate rate : " + rate);
		return mPlayer.fastPlaySetPlaybackRate(rate);
	}

	/**
	 * @brief This method registers a callback to be invoked when the media file is loaded and ready to go.
	 *
	 * @param l The callback to be invoked.
	 *
	 * @since version 6.42
	 */
	public void setOnPreparedListener(OnPreparedListener l) {
		NexLog.d(LOG_TAG, "setOnPreparedListener");
		mPreparedListener = l;
	}

	/**
	 * @brief This method registers a callback to be invoked when the end of a media file has been reached during playback.
	 *
	 * @param l The callback to be invoked.
	 *
	 * @since version 6.42
	 */
	public void setOnCompletionListener(OnCompletionListener l) {
		NexLog.d(LOG_TAG, "setOnCompletionListener");
		mCompletionListener = l;
	}

	/**
	 * @brief This method registers a callback to be invoked when new timed metadata is ready for display in HLS.
	 *
	 * @param l The callback to be invoked.
	 *
	 * @since version 6.42
	 */
	public void setOnTimedMetaRenderRenderListener(OnTimedMetaRenderRenderListener l) {
		NexLog.d(LOG_TAG, "setOnTimedMetaRenderRenderListener");
		mTimedMetaRenderRenderListener = l;
	}

	/**
	 * @brief This method registers a callback to be invoked when an error occurs during the playback.
	 *
	 * @param l The callback to be invoked.
	 *
	 * @since version 6.42
	 */
	public void setOnErrorListener(OnErrorListener l) {
		NexLog.d(LOG_TAG, "setOnErrorListener");
		mErrorListener = l;
	}

	/**
	 * @brief This method registers a callback to be invoked when an informational event occurs during the playback.
	 *
	 * @param l The callback to be invoked.
	 *
	 * @since version 6.42
	 */
	public void setOnInfoListener(OnInfoListener l) {
		NexLog.d(LOG_TAG, "setOnInfoListener");
		mInfoListener = l;
	}

	/**
	 * @brief This method registers a callback to be invoked when buffering event occurs during the playback.
	 *
	 * @param l The callback to be invoked.
	 *
	 * @since version 6.42
	 */
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {
		NexLog.d(LOG_TAG, "setOnBufferingUpdateListener");
		mBufferingUpdateListener = l;
	}

	/**
	 * @brief This method registers a callback to be invoked when \c start() has successfully completed.
	 *
	 * @param l The callback to be invoked.
	 *
	 * @since version 6.42
	 */
	public void setOnStartCompleteListener(OnStartCompleteListener l) {
		NexLog.d(LOG_TAG, "setOnStartCompleteListener");
		mStartCompleteListener = l;
	}

	/**
	 * @brief This method registers a callback to be invoked when \c stopPlayback() has successfully completed.
	 *
	 * @param l The callback to be invoked.
	 *
	 * @since version 6.42
	 */
	public void setOnStopCompleteListener(OnStopCompleteListener l) {
		NexLog.d(LOG_TAG, "setOnStopCompleteListener");
		mStopCompleteListener = l;
	}

	/**
	 * @brief This method registers a callback to be invoked when \c pause() is complete.
	 *
	 * @param l The callback to be invoked.
	 *
	 * @since version 6.46
	 */
	public void setOnPauseCompleteListener(OnPauseCompleteListener l) {
		NexLog.d(LOG_TAG, "setOnPauseCompleteListener");
		mPauseCompleteListener = l;
	}

	/**
	 * @brief This method registers a callback to be invoked when \c resume() is complete.
	 *
	 * @param l The callback to be invoked.
	 *
	 * @since version 6.46
	 */
	public void setOnResumeCompleteListener(OnResumeCompleteListener l) {
		NexLog.d(LOG_TAG, "setOnResumeCompleteListener");
		mResumeCompleteListener = l;
	}

	/**
	 * @brief This method registers a callback to be invoked when a \c FastPlay event occurs during playback.
	 *
	 * @param l The callback to be invoked.
	 *
	 * @since version 6.42
	 */
	public void setOnFastPlayListener(OnFastPlayListener l) {
		NexLog.d(LOG_TAG, "setOnFastPlayListener");
		mFastPlayListener = l;
	}

	/**
	 * @brief This method registers a callback to be invoked when the external subtitle changes during  playback.
	 *
	 * @param l The callback to be invoked.
	 *
	 * @since version 6.42
	 */
	public void setOnExternalSubtitleChangedListener(OnExternalSubtitleChangedListener l) {
		NexLog.d(LOG_TAG, "setOnExternalSubtitleChangedListener");
		mExternalSubtitleChangedListener = l;
	}

	/**
	 * @brief This method registers a callback to be invoked when the media stream changes during the playback.
	 *
	 * @param l The callback to be invoked.
	 *
	 * @since version 6.42
	 */
	public void setOnMediaStreamChangedListener(OnMediaStreamChangedListener l) {
		NexLog.d(LOG_TAG, "setOnMediaStreamChangedListener");
		mMediaStreamChangedListener = l;
	}

	/**
	 * @brief This method registers a callback to be invoked when \c seek() has successfully completed.
	 *
	 * @param l The callback to be invoked.
	 *
	 * @since version 6.42
	 */
	public void setOnSeekCompleteListener(OnSeekCompleteListener l) {
		NexLog.d(LOG_TAG, "setOnSeekCompleteListener");
		mSeekCompleteListener = l;
	}

	/**
	 * @brief This method registers a callback to be invoked when a dynamic thumbnail event occurs during the playback.
	 *
	 * @param l The callback to be invoked.
	 *
	 * @since version 6.42
	 */
	public void setOnDynamicThumbnailListener(OnDynamicThumbnailListener l) {
		NexLog.d(LOG_TAG, "setOnDynamicThumbnailListener");
		mDynamicThumbnailListener = l;
	}

	public void setOnTimeUpdateListener(OnTimeUpdateListener l) {
		NexLog.d(LOG_TAG, "setOnTimeUpdateListener");
		mTimeUpdateListener = l;
	}


	/**
	 * @brief This method sets the settings for NexPlayer, NexALFactory and Video and Caption renderer.
	 * This method should be called before setVideoPath or setVideoURI to be able to set the setting successfully.
	 *
	 * @param settings The object with the setting values.
	 *
	 * @since version 6.42
	 */
	public void setSettings(Settings settings) {
		mSettings = settings;
	}

	/**
	 * @brief This method sets the value of an individual properties of the NexPlayer&tm;.
	 *
	 * It's recommended to call this method after getting the \c onConfiguration callback by registering \c OnConfigurationListener on \c NexVideoView.
	 * We can not guarantee what result the NexPlayer&tm; will return if this method is called at different state.
	 *
	 * @param property     The property to set.
	 * @param value        The new value for the property.
	 *
	 * @since version 6.42
	 */
	public void setProperty(NexPlayer.NexProperty property, Object value) {
		if( value instanceof Integer ) {
			mPlayer.getPlayer().setProperty(property, (Integer)value);
		} else if( value instanceof String ) {
			mPlayer.getPlayer().setProperty(property, (String)value);
		}
	}

	/*public void setProperties( int property, int value ) {
		mPlayer.getPlayer().setProperties(property, value);
	}

	public void setProperties( int property, String value ) {
		mPlayer.getPlayer().setProperties(property, value);
	}*/

	public void setProperties( int property, Object value ) {
		if( value instanceof Integer ) {
			mPlayer.getPlayer().setProperties(property, (Integer)value);
		} else if( value instanceof String ) {
			mPlayer.getPlayer().setProperties(property, (String)value);
		}
	}

	/**
	 * @brief This method gets the value of an individual properties of the NexPlayer&tm;.
	 * The properties control the behavior of NexPlayer&tm; and the features that are enabled.
	 *
	 * @param property The property to get.
	 *
	 * @return The value of the property.
	 *
	 * @since version 6.42
	 */
	public Object getProperty(NexPlayer.NexProperty property) {
		return mPlayer.getPlayer().getProperty(property);
	}

	/**
	 * @brief Selects the caption (subtitle) track that will be used.
	 *
	 * Subtitles for the selected track will be passed to \c onTextRenderRender for display.
	 * This is used for file-based captions only.
	 * For streaming media with included captions, \c setMediaStream() should be used instead,
	 * and local captions should be turned off since running both types of captions at the same time has undefined results.
	 *
	 * @param indexOfCaptionLanguage An index into the \link NexContentInformation#mCaptionLanguages mCaptionLanguages\endlink
	 *          					 array specifying which language to use.  If there are <b> \c n </b> entries
	 *          					 in the caption array, then you may pass \c 0...n-1 to
	 *          					 specify a single language, \c n to specify all languages,
	 *          					 and \c n+1 to turn off captions.
	 *
	 * @since version 6.42
	 */
	public void setCaptionLanguage(int indexOfCaptionLanguage) {
		NexLog.d(LOG_TAG, "setCaptionLanguage indexOfCaptionLanguage : " + indexOfCaptionLanguage);
		mPlayer.getPlayer().setCaptionLanguage(indexOfCaptionLanguage);
		mCaptionRenderView.clearCaptionString();
	}

	/**
	 * @brief For media with multiple streams, this method selects the streams that will be presented to the user.
	 * This method is also used when changing CEA 608 channel index.
	 *
	 * This method operates asynchronously, and when \c setMediaStream has successfully completed, and if \c OnMediaStreamChangedListener is
	 * registered on \c NexVideoView, an \c onMediaStreamChanged event will be called.
	 *
	 * @param streamType The type of the stream to change. This will be one of the following values :
	 *  				- <b>STREAM_TYPE_VIDEO = 2</b>
	 *  				- <b>STREAM_TYPE_AUDIO = 1</b>
	 *  				- <b>STREAM_TYPE_TEXT = 0</b>
	 * @param streamID The ID of the stream to change. If the \c streamType value is set to \c STREAM_TYPE_TEXT and the current caption type is CEA 608,
	 * 				   this value will be used as CEA 608 channel index instead of stream ID.
	 *
	 * @param customAttrID This parameter is used when the \c streamType value is set to \c STREAM_TYPE_VIDEO. Otherwise any value set to this parameter will be ignored.
	 *
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
	 *
	 * @since version 6.42
	 */
	public int setMediaStream(int streamType, int streamID, int customAttrID) {
		NexLog.d(LOG_TAG, "setMediaStream streamType : " + streamType + " streamID : " + streamID + " customAttrID : " + customAttrID);

		int ret = mPlayer.setMediaStream(streamType, NexContentInformation.NEX_TEXT_CEA608 == mCaptionRenderView.getCaptionPainter().getCaptionType(), streamID, customAttrID);

		if( streamType == STREAM_TYPE_TEXT ) {

			if( NexContentInformation.NEX_TEXT_CEA608 == mCaptionRenderView.getCaptionPainter().getCaptionType() && mMediaStreamChangedListener != null ) {
				mMediaStreamChangedListener.onMediaStreamChanged(getNexPlayer(), ret, streamType, streamID);
			}
		}

		NexLog.d(LOG_TAG, "setMediaStream ret : " + ret);
		return ret;
	}
	/**
	 * @brief This method gets an object of the NexPlayer&tm;.
	 *
	 * To use a specific feature provided by the NexPlayer&tm; SDK, use this method to call desired API to the NexPlayer&tm; object.
	 *
	 * @return A NexPlayer&tm; object.
	 *
	 * @since version 6.42
	 */
	public NexPlayer getNexPlayer() {
		return mPlayer.getPlayer();
	}

	public void setOnConfigurationListener(OnConfigurationListener l) {
		mConfigurationListener = l;
	}

	public NexVideoViewFactory.INexVideoView getVideoRenderView() {
		return mVideoRenderView;
	}

	private void setOutputPos(int videoWidth, int videoHeight, ScalingMode scalingMode) {
		setOutputPos(videoWidth, videoHeight, scalingMode, null);
	}

	private void setOutputPos(int videoWidth, int videoHeight, ScalingMode scalingMode, int[] timedTextRegion) {
		switch (scalingMode) {
			case SCALE_ASPECT_FIT: {
				mRenderingArea = getRenderingAreaScaleAspectFit(videoWidth, videoHeight, timedTextRegion);
				break;
			}
			case SCALE_TO_FILL: {
				mRenderingArea = getRenderingAreaScaleToFill(videoWidth, videoHeight, timedTextRegion);
				break;
			}
			case SCALE_ASPECT_FILL: {
				mRenderingArea = getRenderingAreaScaleAspectFill(videoWidth, videoHeight, timedTextRegion);
				break;
			}
		}
		mScalingMode = scalingMode;

		mCaptionRenderView.setRenderingArea(mRenderingArea);
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mCaptionRenderView.invalidate();
			}
		});

		if( mVideoRenderView != null ) {
			mVideoRenderView.setOutputPos(mRenderingArea.mVideo.left, mRenderingArea.mVideo.top, mRenderingArea.mVideo.width(), mRenderingArea.mVideo.height());
		}
	}

	private RenderingArea getRenderingAreaScaleAspectFit(int videoWidth, int videoHeight, int[] timedTextRegion) {
		RenderingArea area = new RenderingArea();
		area.mView = new Rect( 0, 0, getWidth(), getHeight() );

		float scale = Math.min((float) area.mView.width() / (float) videoWidth, (float) area.mView.height() / (float) videoHeight);

		area.mVideo = new Rect();
		area.mVideo.left = (area.mView.width() - ((int) (videoWidth * scale))) / 2;
		area.mVideo.top = (area.mView.height() - ((int) (videoHeight * scale))) / 2;
		area.mVideo.right = area.mVideo.left + ((int) (videoWidth * scale));
		area.mVideo.bottom = area.mVideo.top + ((int) (videoHeight * scale));

		area.mText = new Rect();
		if (timedTextRegion != null) {
			area.mText.left = (area.mVideo.width() - ((int) ((timedTextRegion[2] - timedTextRegion[0]) * scale))) / 2;
			area.mText.top = (area.mVideo.height() - ((int) ((timedTextRegion[3] - timedTextRegion[1]) * scale))) / 2;
			area.mText.right = (area.mText.left + ((int) ((timedTextRegion[2] - timedTextRegion[0]) * scale)));
			area.mText.bottom = (area.mText.top + ((int) ((timedTextRegion[3] - timedTextRegion[1]) * scale)));
		}
		area.mRatio = scale;
		return area;
	}

	private RenderingArea getRenderingAreaScaleToFill(int videoWidth, int videoHeight, int[] timedTextRegion) {
		RenderingArea area = new RenderingArea();
		area.mView = new Rect( 0, 0, getWidth(), getHeight() );

		if( videoWidth > 0 && videoHeight > 0 ) {
			float scaleX = (float) area.mView.width() / (float) videoWidth;
			float scaleY = (float) area.mView.height() / (float) videoHeight;

			area.mVideo = new Rect();
			area.mVideo.left = 0;
			area.mVideo.top = 0;
			area.mVideo.right = area.mView.width();
			area.mVideo.bottom = area.mView.height();

			area.mText = new Rect();
			if (timedTextRegion != null) {
				area.mText.left = (area.mVideo.width() - ((int) ((timedTextRegion[2] - timedTextRegion[0]) * scaleX))) / 2;
				area.mText.top = (area.mVideo.height() - ((int) ((timedTextRegion[3] - timedTextRegion[1]) * scaleY))) / 2;
				area.mText.right = (area.mText.left + ((int) ((timedTextRegion[2] - timedTextRegion[0]) * scaleX)));
				area.mText.bottom = (area.mText.top + ((int) ((timedTextRegion[3] - timedTextRegion[1]) * scaleY)));
			}
			area.mRatio = Math.min(scaleX, scaleY);
		}
		else {
			area.mVideo = new Rect( 0,0,0,0 );

			area.mText = new Rect();
			if (timedTextRegion != null) {
				area.mText.left = timedTextRegion[0];
				area.mText.top = timedTextRegion[1];
				area.mText.right = timedTextRegion[2];
				area.mText.bottom = timedTextRegion[3];
			}
			area.mRatio = 1.0f;
		}
		return area;
	}

	private RenderingArea getRenderingAreaScaleAspectFill(int videoWidth, int videoHeight, int[] timedTextRegion) {
		RenderingArea area = new RenderingArea();
		area.mView = new Rect( 0, 0, getWidth(), getHeight() );

		if( videoWidth > 0 && videoHeight > 0 ) {

			float scale = Math.max(((float) area.mView.width() / (float) videoWidth), ((float) area.mView.height() / (float) videoHeight));

			area.mVideo = new Rect();
			area.mVideo.left = (int)((area.mView.width() - (videoWidth * scale)) / 2);
			area.mVideo.top = (int)((area.mView.height() - (videoHeight * scale)) / 2);
			area.mVideo.right = (int)(area.mVideo.left + (videoWidth * scale));
			area.mVideo.bottom = (int)(area.mVideo.top + (videoHeight * scale));

			area.mText = new Rect();
			if (timedTextRegion != null) {
				area.mText.left = (area.mVideo.width() - ((int) ((timedTextRegion[2] - timedTextRegion[0]) * scale))) / 2;
				area.mText.top = (area.mVideo.height() - ((int) ((timedTextRegion[3] - timedTextRegion[1]) * scale))) / 2;
				area.mText.right = (area.mText.left + ((int) ((timedTextRegion[2] - timedTextRegion[0]) * scale)));
				area.mText.bottom = (area.mText.top + ((int) ((timedTextRegion[3] - timedTextRegion[1]) * scale)));
			}
			area.mRatio = scale;
		}
		else {
			area.mVideo = new Rect( 0,0,0,0 );

			area.mText = new Rect();
			if (timedTextRegion != null) {
				area.mText.left = timedTextRegion[0];
				area.mText.top = timedTextRegion[1];
				area.mText.right = timedTextRegion[2];
				area.mText.bottom = timedTextRegion[3];
			}
			area.mRatio = 1.0f;
		}
		return area;
	}

	private boolean isStreaming(String path) {
		boolean ret = false;

		if( path != null && path.length() > 0 ) {
			if( path.startsWith("http://") || path.startsWith("https://") )
				ret = true;
		}

		return ret;
	}

	/**
	 * @brief This method gets an object of the \c NexCaptionRenderView.
	 *
	 * To use any specific feature provided by the \c NexCaptionRenderView, use this method to call desired API to the \c NexCaptionRenderView object.
	 *
	 * @return A \c NexCaptionRenderView object.
	 * @since version 6.42

	 * This method is deprecated and not supported in the current API version. Do not use it.
	 * @deprecated Not supported in current API version; do use getCaptionPainter instead of this method for captions.
	 */
	public NexCaptionRenderView getCaptionRenderView() {
		return mCaptionRenderView;
	}

	/**
	 * @brief This method gets an object of the \c NexCaptionPainter.
	 *
	 * To use any specific feature provided by the \c NexCaptionPainter, use this method to call desired API to the \c NexCaptionPainter object.
	 *
	 * @return A \c NexCaptionPainter object.
	 * @since version 6.66
	 */
	public NexCaptionPainter getCaptionPainter() {
		return mCaptionRenderView.getCaptionPainter();
	}

	/**
	 * @brief This method gets an object of the \c NexABRController.
	 *
	 * To use any specific feature provided by the \c NexABRController, use this method to call desired API to the \c NexABRController object.
	 *
	 * @return A \c NexABRController object.
	 *
	 * @since version 6.42
	 */
	public NexABRController getABRController() {
		return mABRController;
	}

	NexVideoRenderer.IListener mVideoRendererListener = new NexVideoRenderer.IListener() {

		@Override
		public void onDisplayedRectChanged() {
		}

		@Override
		public void onFirstVideoRenderCreate() {
			setOutputPos(mVideoSize.x, mVideoSize.y, mScalingMode);
		}

		@Override
		public void onSizeChanged() {
			setOutputPos(mVideoSize.x, mVideoSize.y, mScalingMode);
		}

		@Override
		public void onVideoSizeChanged() {
			mVideoRenderView.getVideoSize(mVideoSize);

			if (90 == mRotationDegree || 270 == mRotationDegree) {
				int rotationWidth = mVideoSize.x;
				mVideoSize.x = mVideoSize.y;
				mVideoSize.y = rotationWidth;
			}

			setOutputPos(mVideoSize.x, mVideoSize.y, mScalingMode);
		}
	};

	/**
	 * @brief The application must implement this method to handle touch screen motion events.
	 *
	 * To use this method for detecting tap actions, it is recommended to operate the tapping action with \c performClick().
	 * This will ensure the consistent system behavior, including: dispatching \c OnClickListener calls
	 * handling ACTION_CLICK when accessibility features are enabled.
	 *
	 * @param event The motion event.
	 *
	 * @return \c true if the event was handled, otherwise \c false.
	 * @since version 6.42
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		toggleMediaControlsVisibility();
		return super.onTouchEvent(event);
	}

	private void toggleMediaControlsVisibility() {
		if ( mMediaController != null && isInPlaybackState() ) {
			if( mMediaController.isShowing() )
				mMediaController.hide();
			else
				mMediaController.show(MEDIA_CONTROLLER_TIMEOUT_SEC);
		}
	}

	private NexMediaPlayer.IListener mPlayerIListener = new NexMediaPlayer.IListener() {
		@Override
		public void onAsyncCmdComplete(NexPlayer mp, int command, int result, int param1, int param2) {
			switch (command) {
				case NexPlayer.NEXPLAYER_ASYNC_CMD_OPEN_LOCAL:
				case NexPlayer.NEXPLAYER_ASYNC_CMD_OPEN_STREAMING:
					if( result == 0 ) {
						final NexContentInformation info = mp.getContentInfo();

						mCaptionRenderView.getCaptionPainter().setCaptionType(info.mCaptionType);

						if ( mPreparedListener != null )
							mPreparedListener.onPrepared(mp);
					} else {
						onError(mp, NexPlayer.NexErrorCode.fromIntegerValue(result));
					}

					break;
				case NexPlayer.NEXPLAYER_ASYNC_CMD_START_LOCAL:
				case NexPlayer.NEXPLAYER_ASYNC_CMD_START_STREAMING:
					if( result == 0 ) {
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								if (mMediaController != null)
									mMediaController.setEnabled(true);
							}
						});

						if ( mStartCompleteListener != null ) {
							mStartCompleteListener.onStartComplete(mp);
						}
					} else {
						onError(mp, NexPlayer.NexErrorCode.fromIntegerValue(result));
					}
					break;
				case NexPlayer.NEXPLAYER_ASYNC_CMD_PAUSE:
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							updateMediaController();
						}
					});
					if ( mPauseCompleteListener != null ) {
						mPauseCompleteListener.onPauseComplete(mp);
					}
					break;
				case NexPlayer.NEXPLAYER_ASYNC_CMD_RESUME:
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							updateMediaController();
						}
					});
					if ( mResumeCompleteListener != null ) {
						mResumeCompleteListener.onResumeComplete(mp);
					}
					break;
				case NexPlayer.NEXPLAYER_ASYNC_CMD_STOP:
					if( mStopCompleteListener != null )
						mStopCompleteListener.onStopComplete(mp, result);
					break;
				case NexPlayer.NEXPLAYER_ASYNC_CMD_SEEK:
					mCaptionRenderView.clearCaptionString();
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							updateMediaController();
						}
					});

					if( mSeekCompleteListener != null )
						mSeekCompleteListener.onSeekComplete(mp, result, param1);
					break;
				case NexPlayer.NEXPLAYER_ASYNC_CMD_SET_MEDIA_STREAM:
					if ( param1 == NexPlayer.MEDIA_STREAM_TYPE_TEXT ) {
						mCaptionRenderView.clearCaptionString();
					}

					if ( mMediaStreamChangedListener != null ) {
						if( param1 == NexPlayer.MEDIA_STREAM_TYPE_AUDIO )
							param1 = STREAM_TYPE_AUDIO;
						else if( param1 == NexPlayer.MEDIA_STREAM_TYPE_TEXT )
							param1 = STREAM_TYPE_TEXT;
						else if( param1 == NexPlayer.MEDIA_STREAM_TYPE_VIDEO )
							param1 = STREAM_TYPE_VIDEO;

						mMediaStreamChangedListener.onMediaStreamChanged(mp, result, param1, param2);
					}

					break;
				case NexPlayer.NEXPLAYER_ASYNC_CMD_SETEXTSUBTITLE:
					if( result == 0 ) {
						mCaptionRenderView.clearCaptionString();
					}

					if( mExternalSubtitleChangedListener != null )
						mExternalSubtitleChangedListener.onExternalSubtitleChanged(mp, result);

					break;
				case NexPlayer.NEXPLAYER_ASYNC_CMD_FASTPLAY_START:
					mCaptionRenderView.clearCaptionString();

					if( mFastPlayListener != null )
						mFastPlayListener.onFastPlayStart(mp, result);
					break;
				case NexPlayer.NEXPLAYER_ASYNC_CMD_FASTPLAY_STOP:
					if( mFastPlayListener != null )
						mFastPlayListener.onFastPlayStop(mp, result);
					break;
			}
		}

		@Override
		public void onBufferingBegin(NexPlayer mp) {
			if( mBufferingUpdateListener != null )
				mBufferingUpdateListener.onBufferingBegin(mp);
		}

		@Override
		public void onBuffering(NexPlayer mp, int progressInPercent) {
			if( mBufferingUpdateListener != null )
				mBufferingUpdateListener.onBuffering(mp, progressInPercent);
		}

		@Override
		public void onBufferingEnd(NexPlayer mp) {
			if( mBufferingUpdateListener != null )
				mBufferingUpdateListener.onBufferingEnd(mp);
		}

		@Override
		public void onStatusReport(NexPlayer mp, int msg, int param1) {
			if( msg == NexPlayer.NEXPLAYER_STATUS_REPORT_CONTENT_INFO_UPDATED ) {
				NexContentInformation info = mp.getContentInfo();
				mRotationDegree = info.mRotationDegree;
				mCaptionRenderView.getCaptionPainter().setCaptionType(info.mCaptionType);
				if( mInfoListener != null )
					mInfoListener.onInfo(mp, info);
			}
		}

		@Override
		public void onError(NexPlayer mp, NexPlayer.NexErrorCode errorCode) {
			NexLog.e(LOG_TAG, "onError mp : " + mp + " errorCode : " + errorCode);
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					hideMediaController();
				}
			});

			if( mErrorListener != null )
				mErrorListener.onError(mp, errorCode);
		}

		@Override
		public void onTextRenderRender(NexPlayer mp, int trackIndex, NexClosedCaption textInfo) {
			mCaptionRenderView.renderClosedCaption(textInfo.getCaptionType(), textInfo);
		}

		@Override
		public void onTimedMetaRenderRender(NexPlayer mp, NexID3TagInformation TimedMeta) {
			if( mTimedMetaRenderRenderListener != null )
				mTimedMetaRenderRenderListener.onTimedMetaRenderRender(mp, TimedMeta);
		}

		@Override
		public void onDynamicThumbnailData(int cts, Bitmap bitmap) {
			if( mDynamicThumbnailListener != null )
				mDynamicThumbnailListener.onDynamicThumbnailData(cts, bitmap);
		}

		@Override
		public void onDynamicThumbnailRecvEnd() {
			if( mDynamicThumbnailListener != null )
				mDynamicThumbnailListener.onDynamicThumbnailRecvEnd();
		}

		@Override
		public void onEndOfContent(NexPlayer mp) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					hideMediaController();
				}
			});

			if ( mCompletionListener != null)
				mCompletionListener.onCompletion(mp);
		}

		@Override
		public void onTime(NexPlayer mp, int currTime) {
			if(mTimeUpdateListener != null) {
				mTimeUpdateListener.onTime(mp, currTime);
			}
		}
	};

	private void updateMediaController() {
		if( mMediaController != null && mMediaController.isShowing() )
			mMediaController.show(MEDIA_CONTROLLER_TIMEOUT_SEC);
	}

	private void hideMediaController() {
		if ( mMediaController != null && mMediaController.isShowing() ) {
			mMediaController.hide();
		}
	}

	/**
	 * @brief This method informs the view that the activity is paused.
	 *
	 * The owner of this view must call this method when the activity is paused.
	 * Calling this method will pause the rendering thread.
	 *
	 * @warning This method will be called automatically on devices running the Android ICS (4.0) or higher. 
	 * 			Otherwise, this method must be called when the activity is paused. 
	 *
	 * @since version 6.42
	 */
	public void onPause() {
		NexLog.d(LOG_TAG, "onPause");
		if( Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ) {
			pauseVideoRenderView();
		}
	}

	/**
	 * @brief This method informs the view that the activity has resumed.
	 *
	 * The owner of this view must call this method when the activity is being resumed.
	 * Calling this method will recreate the OpenGL display and resume the rendering thread.
	 *
	 * @warning This method will be called automatically on devices running the Android ICS (4.0) or higher. 
	 * 			Otherwise, this method must be called when the activity is paused. 
	 * @since version 6.42
	 */
	public void onResume() {
		NexLog.d(LOG_TAG, "onResume");
		if( Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ) {
			resumeVideoRenderView();
		}
	}

	/**
	 * @brief This method clears the resources of \c NexVideoView including \c NexPlayer&tm; and \c NexALFactory. 
	 * This method must be called when the activity is destroyed. 
	 *
	 * @since version 6.42
	 */
	public void release() {
		NexLog.d(LOG_TAG, "release");

		if( mActivityLifecycleCallbacks != null ) {
			unregisterActivityLifecycleCallbacks((Activity) mContext);
		}

		hideMediaController();

		mIsInitialized = false;
		mPlayer.release();
		if (mVideoRenderView != null) {
			mVideoRenderView.release();
		}
		mHandler.removeCallbacksAndMessages(null);
	}

	public void setMediaDrmLicenseServer(String url) {
		mediaDrmLicenseServer = url;
	}

	public void setWideVineLicenseServer(String url) {
		wideVineLicenseServer = url;
	}

	private void pauseVideoRenderView() {
		if ( mVideoRenderView != null )
			mVideoRenderView.onPause();
	}

	private void resumeVideoRenderView() {
		if( mVideoRenderView != null )
			mVideoRenderView.onResume();
	}

	/**
	 * @brief This method gets the current settings of the \c NexVideoView. 
	 * @return A settings object currently set to \c NexVideoView.
	 *
	 * @see NexVideoView.Settings
	 * @since version 6.42
	 */
	public Settings getSettings() {
		return mSettings;
	}

	/**
	 * @brief The application must implement this interface in order to receive \c NexVideoView events before NexPlayer&tm; is opened. 
	 *
	 * The application must implement settings for the NexPlayer&tm; before the NexPlayer.open() such as \c setProperty. 
	 *
	 * @warning These callbacks may occur in any thread, not
	 * necessarily the main application thread. In some cases, it may not
	 * be safe to call UI-related functions from within \c IListener
	 * callbacks.  The safest way to update the UI is to use \c android.os.Handler
	 * to post an event back to the main application thread.
	 * @since version 6.42
	 */
	public interface OnConfigurationListener {
		/**
		 * @brief This method is called before \cNexVideoView opens the NexPlayer&tm;. 
		 *
		 */
		void onConfiguration();
	}

	/**
	 * @brief The application must implement this interface in order to receive \c NexVideoView events when NexPlayer&tm; is ready for the playback. 
	 * This callback must be registered to operate \c NexVideoView successfully.  
	 *
	 * @warning These callbacks may occur in any thread, not
	 * necessarily the main application thread. In some cases, it may not
	 * be safe to call UI-related functions from within \c IListener
	 * callbacks.  The safest way to update the UI is to use \c android.os.Handler
	 * to post an event back to the main application thread.
	 * @since version 6.42
	 *
	 */
	public interface OnPreparedListener {
		/**
		 * @brief This method is called when \c setVideoPath or \c setVideoURI is successful. 
		 * In case of an error, an \c onError event will occur instead of an \c onPrepared event.
		 *
		 * @param mp The NexPlaye&tm; object to which this event applies.
		 */
		void onPrepared(NexPlayer mp);
	}
	/**
	 * @brief The application must implement this interface in order to receive \c NexVideoView events when playback is successful up to the end of the content. 
	 *
	 * When the playback has completed and \c onCompletion is returned, \c setVideoPath or \c setVideoURI should be called only after when the player has stopped 
	 * successfully and \c onStopComplete is returned by calling \c stopPlayback.
	 *
	 * It is suggested to call \c stopPlayback at \c onCompletion. However there are two cases with exceptions : 
	 * -# When destroying activity after playback has completed. In this case, request Activity.finish at \c onCompletion, then call \c release when activity is destroyed. 
	 * -# When seek back to the beginning of content when playback has completed. In this case, seek to a desired position by using \c seekTo at \c onCompletion, then call \c resume.   
	 *
	 * @warning These callbacks may occur in any thread, not
	 * necessarily the main application thread. In some cases, it may not
	 * be safe to call UI-related functions from within \c IListener
	 * callbacks.  The safest way to update the UI is to use \c android.os.Handler
	 * to post an event back to the main application thread.
	 *
	 * @since version 6.42
	 */
	public interface OnCompletionListener {
		/**
		 * This method indicates when playback is successful up to the end of the content.
		 *
		 * @param mp The NexPlaye&tm; object generating the event.
		 */
		void onCompletion(NexPlayer mp);
	}

	/**
	 * @brief The application must implement this interface in order to receive error events from \c NexVideoView.
	 *
	 * @warning These callbacks may occur in any thread, not
	 * necessarily the main application thread. In some cases, it may not
	 * be safe to call UI-related functions from within \c IListener
	 * callbacks.  The safest way to update the UI is to use \c android.os.Handler
	 * to post an event back to the main application thread.
	 * @since version 6.42
	 */
	public interface OnErrorListener {
		/**
		 * @brief An error has occurred during playback.
		 *
		 * @param mp The NexPlayer&tm; object generating the event.
		 * @param errorCode The error code for the generated error.
		 */
		void onError(NexPlayer mp, NexPlayer.NexErrorCode errorCode);
	}

	/**
	 * @brief This interface transfer updated \c NexContentInformation to the application. 
	 *
	 *
	 * @warning These callbacks may occur in any thread, not
	 * necessarily the main application thread. In some cases, it may not
	 * be safe to call UI-related functions from within \c IListener
	 * callbacks.  The safest way to update the UI is to use \c android.os.Handler
	 * to post an event back to the main application thread.
	 * @since version 6.42
	 */
	public interface OnInfoListener {
		/**
		 * @brief An event will occur when \c NexContentInformation is being updated.  
		 *
		 * @param mp The NexPlaye&tm; object to which this event applies.
		 * @param info The information of the current content.
		 */
		void onInfo(NexPlayer mp, NexContentInformation info);
	}

	/**
	 * @brief The application must implement this interface in order to receive \c TimedMeta events from \c NexVideoView. 
	 *
	 * @warning These callbacks may occur in any thread, not
	 * necessarily the main application thread. In some cases, it may not
	 * be safe to call UI-related functions from within \c IListener
	 * callbacks.  The safest way to update the UI is to use \c android.os.Handler
	 * to post an event back to the main application thread.
	 *
	 * @see NexPlayer.IListener onTimedMetaRenderRender
	 * @since version 6.42
	 */
	public interface OnTimedMetaRenderRenderListener {
		/**
		 * This method is called when new timed metadata is ready for display in HLS.
		 *
		 * @param mp The NexPlaye&tm; object to which this event applies.
		 * @param timedMeta An NexID3TagInformation object that contains the timed metadata associated with the content to be displayed.
		 */
		void onTimedMetaRenderRender(NexPlayer mp, NexID3TagInformation timedMeta);
	}

	/**
	 * @brief The application must implement this interface in order to receive \c onstartComplete events from \c NexVideoView. 
	 *
	 * @warning These callbacks may occur in any thread, not
	 * necessarily the main application thread. In some cases, it may not
	 * be safe to call UI-related functions from within \c IListener
	 * callbacks.  The safest way to update the UI is to use \c android.os.Handler
	 * to post an event back to the main application thread.
	 * @since version 6.42
	 */
	public interface OnStartCompleteListener {
		/**
		 * @brief An event will occur when start() is successful.  
		 * In case of an error, \c onError will occur instead of an \c onStartComplete event. 
		 *
		 * @param mp The NexPlayer&tm; object to which this event applies.
		 */
		void onStartComplete(NexPlayer mp);
	}

	/**
	 * @brief The application must implement this interface in order to receive \c onStopComplete events from \c NexVideoView. 
	 *
	 * @warning These callbacks may occur in any thread, not
	 * necessarily the main application thread. In some cases, it may not
	 * be safe to call UI-related functions from within \c IListener
	 * callbacks.  The safest way to update the UI is to use \c android.os.Handler
	 * to post an event back to the main application thread.
	 * @since version 6.42
	 */
	public interface OnStopCompleteListener {
		/**
		 * @brief An event will occur when \c stopPlayback is successful. 
		 *
		 * @param mp The NexPlayer&tm; object to which this event applies.
		 * @param result Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
		 */
		void onStopComplete(NexPlayer mp, int result);
	}

	/**
	 * @brief The application must implement this interface in order to receive \c onPauseComplete events from \c NexVideoView.
	 *
	 * @warning These callbacks may occur in any thread, not
	 * necessarily the main application thread. In some cases, it may not
	 * be safe to call UI-related functions from within \c IListener
	 * callbacks.  The safest way to update the UI is to use \c android.os.Handler
	 * to post an event back to the main application thread.
	 * @since version 6.46
	 */
	public interface OnPauseCompleteListener {
		/**
		 * @brief An event will occur when \c pause is successful.
		 *
		 * @param mp The NexPlayer&tm; object to which this event applies.
		 */
		void onPauseComplete(NexPlayer mp);
	}

	/**
	 * @brief The application must implement this interface in order to receive \c onResumeComplete events from \c NexVideoView.
	 *
	 * @warning These callbacks may occur in any thread, not
	 * necessarily the main application thread. In some cases, it may not
	 * be safe to call UI-related functions from within \c IListener
	 * callbacks.  The safest way to update the UI is to use \c android.os.Handler
	 * to post an event back to the main application thread.
	 * @since version 6.46
	 */
	public interface OnResumeCompleteListener {
		/**
		 * @brief An event will occur when \c resume is successful.
		 *
		 * @param mp The NexPlayer&tm; object to which this event applies.
		 */
		void onResumeComplete(NexPlayer mp);
	}

	/**
	 * @brief The application must implement this interface in order to receive events from \c NexVideoView when \c seek is successful. 
	 *
	 * @warning These callbacks may occur in any thread, not
	 * necessarily the main application thread. In some cases, it may not
	 * be safe to call UI-related functions from within \c IListener
	 * callbacks.  The safest way to update the UI is to use \c android.os.Handler
	 * to post an event back to the main application thread.
	 * @since version 6.42
	 *
	 */
	public interface OnSeekCompleteListener {
		/**
		 * @brief An event will occur when \c seekTo is successful. 
		 *
		 * @param mp    	The NexPlayer&tm; object to which this event applies.
		 * @param result    Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
		 * @param position  The actual position at which the \c seek command completed.
		 *					Depending on the media format, this maybe different than the position
		 *					that was requested for the \c seek operation.
		 * @since version 6.42
		 */
		void onSeekComplete(NexPlayer mp, int result, int position);
	}

	/**
	 * @brief The application must implement this interface in order to receive \c fastPlay related events from \c NexVideoView. 
	 *
	 * @warning These callbacks may occur in any thread, not
	 * necessarily the main application thread. In some cases, it may not
	 * be safe to call UI-related functions from within \c IListener
	 * callbacks.  The safest way to update the UI is to use \c android.os.Handler
	 * to post an event back to the main application thread.
	 * @since version 6.42
	 */
	public interface OnFastPlayListener {
		/**
		 * @brief An event will occur when \c fastPlayStart is successful. 
		 *
		 * @param mp The NexPlaye&tm; object to which this event applies.
		 * @param result Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
		 */
		void onFastPlayStart(NexPlayer mp, int result);
		/**
		 * @brief An event will occur when \c fastPlayStop is successful. 
		 *
		 * @param mp The NexPlaye&tm; object to which this event applies.
		 * @param result Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
		 */
		void onFastPlayStop(NexPlayer mp, int result);
	}

	/**
	 * @brief The application must implement this interface in order to receive \c onMediaStreamChanged events from \c NexVideoView. 
	 *
	 * @warning These callbacks may occur in any thread, not
	 * necessarily the main application thread. In some cases, it may not
	 * be safe to call UI-related functions from within \c IListener
	 * callbacks.  The safest way to update the UI is to use \c android.os.Handler
	 * to post an event back to the main application thread.
	 * @since version 6.42
	 */
	public interface OnMediaStreamChangedListener {
		/**
		 * @brief An event will occur when \c setMediaStream is successful. 
		 *
		 * @param mp  		  The NexPlayer&tm;  object to which this event applies.
		 * @param result 	  Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
		 * @param streamType      The type of changed stream. This will be one of the supported stream types of \c setMediaStream.                  
		 * @param streamID 	  The ID of changed stream.  
		 */
		void onMediaStreamChanged(NexPlayer mp, int result, int streamType, int streamID);
	}

	/**
	 * @brief The application must implement this interface in order to receive \c onExternalSubtitleChanged events from \c NexVideoView. 
	 *
	 * @warning These callbacks may occur in any thread, not
	 * necessarily the main application thread. In some cases, it may not
	 * be safe to call UI-related functions from within \c IListener
	 * callbacks.  The safest way to update the UI is to use \c android.os.Handler
	 * to post an event back to the main application thread.
	 * @since version 6.42
	 */
	public interface OnExternalSubtitleChangedListener {
		/**
		 * @brief An event will occur when \c addSubtitleSource is successful. 
		 *
		 * @param mp The NexPlaye&tm; object to which this event applies.
		 * @param  Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
		 */
		void onExternalSubtitleChanged(NexPlayer mp, int result);
	}

	/**
	 * @brief The application must implement this interface in order to receive buffering events from \c NexVideoView. 
	 *
	 * @warning These callbacks may occur in any thread, not
	 * necessarily the main application thread. In some cases, it may not
	 * be safe to call UI-related functions from within \c IListener
	 * callbacks.  The safest way to update the UI is to use \c android.os.Handler
	 * to post an event back to the main application thread.
	 * @since version 6.42
	 */
	public interface OnBufferingUpdateListener {
		/**
		 * @briefReports the start of buffering.
		 *
		 * @param mp The NexPlayer&tm; object to which this event applies.
		 */
		void onBufferingBegin(NexPlayer mp);

		/**
		 * @briefThis reports the current buffering status.
		 *
		 * @param mp The NexPlayer&tm; object to which this event applies.
		 * @param progressInPercent The current buffering percentage.
		 */
		void onBuffering(NexPlayer mp, int progressInPercent);

		/**
		 * @brief This reports the end of buffering.
		 *
		 * @param mp The NexPlayer&tm; object to which this event applies.
		 */
		void onBufferingEnd(NexPlayer mp);
	}

	/**
	 * @brief The application must implement this interface in order to receive Dynamic Thumbnail related events from \c NexVideoView. 
	 *
	 * @warning These callbacks may occur in any thread, not
	 * necessarily the main application thread. In some cases, it may not
	 * be safe to call UI-related functions from within \c IListener
	 * callbacks.  The safest way to update the UI is to use \c android.os.Handler
	 * to post an event back to the main application thread.
	 * @since version 6.42
	 */
	public interface OnDynamicThumbnailListener {
		/**
		 * @brief This method will be called by the NexPlayer&tm; engine when thumbnail data is created.
		 *
		 * @param cts    The current timestamp of the thumbnail image.
		 * @param bitmap RGB buffer pointer(RGB565) of the thumbnail image. 
		 */
		void onDynamicThumbnailData(int cts, Bitmap bitmap);

		/**
		 * @brief This callback method informs the Dynamic Thumbnail listener when the end of thumbnail data is received.
		 */
		void onDynamicThumbnailRecvEnd();
	}

	public interface OnTimeUpdateListener {
		void onTime(NexPlayer mp, int currTime);
	}

	/**
	 * @brief This class manages the setting values needed for initializing Video Renderer, Caption Renderer and NexPlayer&tm; internally. 
	 *
	 * To initialize successfully, \c setSettings must be called before \c setVideoPath or \c setVideoURI is called, after changing the setting value by using \c setValue.
	 * Note that \c setSettings is not guaranteed to work properly when called at any other time.
	 *
	 * @since version 6.42                                                                                                                                                                                                                                                  .  
	 *
	 */
	public static class Settings {

		/**
		 * Possible key value for \c key parameter of {@link Settings.setValue(int, int) setValue}.
		 *  Possible value range : -1 to 5
		 * */
		public final static int INT_LOG_LEVEL               = 0;
		/**
		 * Possible key value for \c key parameter of {@link Settings#setValue(int, CEARenderMode) setValue}.
		 *
		 * This will be one of the following values : 
		 *      - Settings.CEARenderMode.CEA_608 : Print CEA 608 in the case where there are both CEA 708 and CEA 608 closed captions available. 
		 *      - Settings.CEARenderMode.CEA_708 : Print CEA 708 in the case where there are both CEA 708 and CEA 608 closed captions available. 
		 */
		public final static int CEA_RENDER_MODE                 = 1;
		/**
		 * Possible key value for \c key parameter of {@link Settings#setValue(int, int) setValue}.
		 *  This will be one of the following values : 
		 *      - PixelFormat.RGBA_8888
		 *      - PixelFormat.RGB_565
		 * */
		public final static int PIXELFORMAT_FORMAT              = 2;
		/**
		 * Possible key value for \c key parameter of {@link Settings#setValue(int, boolean) setValue}.
		 * */
		public final static int BOOL_USE_UDP                    = 3;

		/**
		 * @brief This enumeration defines the possible modes for CEA rendering. 
		 *
		 */
		public enum CEARenderMode {
			CEA_608(0), CEA_708(1);

			int mValue;

			CEARenderMode(int value) {
				mValue = value;
			}

			public int getInteger() {
				return mValue;
			}
		}

		protected int mLogLevel                         = 0;
		protected int mPixelFormat                      = PixelFormat.RGBA_8888;
		protected CEARenderMode mCEARenderMode          = CEARenderMode.CEA_608;
		protected boolean mCEA608Standard               = true;
		protected boolean mUseUDP                       = false;


		public Settings() { }

		/**
		 * @brief This method sets the setting values for the properties of NexPlayer&tm;. 
		 *
		 * @param key The key to set new settings value, as an \c integer. This will be one of the following values :
		 *              - <b>0 : INT_LOG_LEVEL</b>
		 *              - <b>2 : PIXELFORMAT_FORMAT</b>
		 * @param value The new value for the property, as an \c integer.
		 *
		 * @return The value of the property. 
		 * @since version 6.42
		 */
		public boolean setValue(int key, int value) {
			boolean ret = false;
			switch (key) {
				case INT_LOG_LEVEL:
					mLogLevel = value;
					ret = true;
					break;
				case PIXELFORMAT_FORMAT:
					mPixelFormat = value;
					ret = true;
					break;
			}
			return ret;
		}

		/**
		 * @brief This method sets the setting values for the properties of NexPlayer&tm;. 
		 *
		 * @param key  The key to set new settings value, as an \c integer. Supported key value :
		 *              - <b>3: BOOL_USE_UDP</b>
		 * @param value The new value for the property, in \c boolean.
		 *
		 * @return The value of the property.
		 * @since version 6.42
		 *
		 */
		public boolean setValue(int key, boolean value) {
			boolean ret = false;
			switch (key) {
				case BOOL_USE_UDP:
					mUseUDP = value;
					ret = true;
					break;
			}
			return ret;
		}
		/**
		 * @brief This method sets the setting values for the properties of NexPlayer&tm;. 
		 *
		 * @param key The key to set new settings value, as an \c integer. Supported key value :
		 *         - <b>1: CEA_RENDER_MODE</b>
		 *
		 * @param value The new value for the property.
		 * @return The value of the property.
		 * @since version 6.42
		 */
		public boolean setValue(int key, CEARenderMode value) {
			if( key == CEA_RENDER_MODE ) {
				mCEARenderMode = value;
				return true;
			}
			return false;
		}

		/**
		 * @brief This method gets the setting value correspondent to a specific key. 
		 *
		 * @param key The key to retrieve setting value. 
		 *
		 * @return The value correspondent to the key. 
		 * @since version 6.42
		 */
		public Object getValue(int key) {
			Object ret = null;
			switch (key) {
				case INT_LOG_LEVEL:
					ret = mLogLevel;
					break;
				case PIXELFORMAT_FORMAT:
					ret = mPixelFormat;
					break;
				case CEA_RENDER_MODE:
					ret = mCEARenderMode;
					break;
				case BOOL_USE_UDP:
					ret = mUseUDP;
					break;
			}
			return ret;
		}
	}
}

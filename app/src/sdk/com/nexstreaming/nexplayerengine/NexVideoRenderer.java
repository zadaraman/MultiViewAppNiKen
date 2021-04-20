package com.nexstreaming.nexplayerengine;

import android.annotation.SuppressLint;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * \brief A prebuilt FrameLayout containing the video view associated with a
 * NexPlayer&trade;&nbsp;instance.
 * 
 * NexVideoRenderer can be used to simplify the video rendering tasks in an
 * application integrating with the NexPlayer&trade;&nbsp;SDK. It handles the
 * different renderer types that can be used by \link NexPlayer
 * NexPlayer\endlink &trade;&nbsp;but depend on the device.
 * 
 * To use it: 
 *   -# Pass in a \link android.content.Context Context\endlink to the constructor. 
 *   -# Set up listeners (\link NexPlayer.IListener IListener\endlink and \link NexPlayer.IVideoRendererListener IVideoRendererListener\endlink).
 *   -# Create an instance of \link NexPlayer\endlink. 
 *   -# Perform the necessary setup for NexPlayer&trade;&nbsp; (such as \link
 *      NexPlayer.setNexALFactory\endlink and \link NexPlayer.init\endlink). 
 *   -# Call \link NexVideoRenderer.init init\endlink with the \link NexPlayer\endlink instance.
 *   -# Add the NexVideoRenderer instance as a view to your layout.
 * 
 * For additional details on how to use this video renderer, please refer to the sample code provided 
 * with the SDK.
 * 
 * @author NexStreaming Corp.
 * 
 * \since version 6.1
 */
public class NexVideoRenderer extends FrameLayout implements NexVideoViewFactory.INexVideoView
{

	/**
	 * \brief The application must implement this interface in order to receive
	 * events from NexVideoRenderer.
	 * 
	 * \warning  These callbacks may occur in any thread, not necessarily
	 * the main application thread. In some cases, it may not be safe to call
	 * UI-related functions from within \c IListener callbacks. The safest way
	 * to update the UI is to use \c android.os.Handler to post an event back to
	 * the main application thread.
	 * 
	 * NexVideoRenderer will call the methods provided in this interface
	 * automatically during playback to notify the application when various
	 * events have occurred.
	 * 
	 * In most cases, the handling of these events is optional;
	 * NexPlayer&trade;&nbsp; will continue playback normally without the
	 * application doing anything special. For best results, handling all events
	 * is recommended.
	 * 
	 * See each individual \c IListener method for a recommendation on how to
	 * handle the event in the application.
	 * 
	 * \since version 6.1
	 */
	public interface IListener {
		/**
		 * \brief This callback indicates that the displayed rectangle's
		 * position or size has changed.
		 * 
		 * This event occurs when \link NexVideoRenderer.setOutputPos\endlink is
		 * called.
		 * 
		 * This would be a good time for the application to handle any related
		 * layout changes that need to be performed, such as changing the layout
		 * of subtitles or the player UI controls.
		 * 
		 * \since version 6.1
		 */
		void onDisplayedRectChanged();

		/**
		 * \brief This callback indicates that the NexPlayer&trade;&nbsp;instance has
		 * performed its first of possibly many \link
		 * NexPlayer.IVideoRendererListener.onVideoRenderCreate
		 * onVideoRenderCreate\endlink callbacks.
		 * 
		 * This event occurs when NexVideoRenderer first receives the \link
		 * NexPlayer.IVideoRendererListener.onVideoRenderCreate
		 * onVideoRenderCreate\endlink callback from the associated NexPlayer&trade;&nbsp;
		 * instance.
		 * 
		 * On receiving this callback, the application should call \link
		 * NexVideoRenderer.setOutputPos\endlink to display the video at the
		 * desired resolution and aspect ratio.
		 * 
		 * \since version 6.1
		 */
		void onFirstVideoRenderCreate();

		/**
		 * \brief This callback indicates that the size of the NexVideoRenderer
		 * FrameLayout has changed.
		 * 
		 * This event could occur when the device is rotated or the application
		 * requests the view to change size.
		 * 
		 * On receiving this callback, the application should call \link
		 * NexVideoRenderer.setOutputPos\endlink to display the video at the
		 * desired resolution and aspect ratio.
		 * 
		 * \since version 6.1
		 */
		void onSizeChanged();

		/**
		 * \brief This callback indicates that the size of the media content has
		 * changed.
		 * 
		 * This event will occur at least once when first starting playback.
		 * 
		 * On receiving this callback, the application should call \link
		 * NexVideoRenderer.setOutputPos\endlink to display the video at the
		 * desired resolution and aspect ratio.
		 * 
		 * \since version 6.1
		 */
		void onVideoSizeChanged();
	}

	private NexPlayer.IVideoRendererListener iVideoRendererListener = null;
	private SurfaceHolder.Callback surfaceHolderCallback = null;

	private boolean activityPaused = false;

	private Paint blitPaint = null; // used by JAVA renderer
	//private int clearReq = 0; // used by JAVA renderer // deprecated
	private Bitmap frameBitmap = null; // used by JAVA renderer

	private Rect displayedRect = null;
	private boolean firstVideoRenderCreate = false;
	private GLRenderer glRenderer = null;
	private boolean initGLRenderer = false;

	private Bitmap lastCapturedFrame = null;

	private IListener videoSizeListener = null;
	private final String LOG_TAG = "NexVideoRenderer";

	private NexPlayer nexPlayer = null;

	private int orientation = Configuration.ORIENTATION_UNDEFINED;
	private GLRenderer.IListener postGLRendererListener = null;
	private NexPlayer.IVideoRendererListener postNexPlayerVideoRendererListener = null;
	private SurfaceHolder.Callback postSurfaceHolderCallback = null;
	private GLRenderer.IListener preGLRendererListener = null;
	private NexPlayer.IVideoRendererListener preNexPlayerVideoRendererListener = null;

	private SurfaceHolder.Callback preSurfaceHolderCallback = null;
	private int screenPixelFormat = 0;

	private boolean shouldFilterBitmap = false;
	private Paint solidPaint = null;
	private boolean useOpenGL = false;

	private boolean videoPrepared = false;
	private int visibility = View.INVISIBLE;
	private boolean videoInitEnd = false;
	private SurfaceHolder videoNormalSurfaceHolder = null;
	private SurfaceHolder videoOpenGLSurfaceHolder = null;
	private SurfaceView videoNormalSurfaceView = null;
	private boolean videoSurfaceExists = false;

	private Point surfaceSize = new Point(0, 0);
	private Point videoSize = new Point(0, 0);

	private Handler mHandler = new Handler();
	private Boolean mUseSecure = false;
	
	private View mBlackScreen = null;
	private boolean mBlackScreenOn;
	
	private LayoutParams mLayoutParams;
	private boolean mTextureLoaded = false;

	private String mVideoCodecName;
	private boolean mSecureSurface;
	private boolean mEnableMutliView;

	private boolean mPaused = false;
	/**
	 * \brief Constructor for NexVideoRenderer.
	 * 
	 * After creating an instance, you may call \link
	 * NexVideoRenderer.getColorDepth getColorDepth\endlink to pass to \link
	 * NexALFactory.init\endlink.
	 * 
	 * @param context The \link android.content.Context Context\endlink instance
	 * associated with the activity that will contain this view.
	 * 
	 * \since version 6.1
	 */
	public NexVideoRenderer(Context context) {
		super(context);
		internal_init();
	}

	/**
	 * \brief Constructor for NexVideoRenderer.
	 * 
	 * \see NexVideoRenderer.NexVideoRenderer(android.content.Context)
	 * 
	 * \since version 6.1
	 */
	public NexVideoRenderer(Context context, AttributeSet attrs) {
		super(context, attrs);
		internal_init();
	}

	/**
	 * \brief Constructor for NexVideoRenderer.
	 * 
	 * \see NexVideoRenderer.NexVideoRenderer(android.content.Context)
	 * 
	 * \since version 6.1
	 */
	public NexVideoRenderer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		internal_init();
	}


	public void setCodecName(String name) {
		mVideoCodecName = name;
	}

	public void setSecureSurfaceFlag(Boolean secure) {
		mSecureSurface = secure;
	}

	public void setSupportMultiView(Boolean enable) {
		if((videoNormalSurfaceView instanceof NexSurfaceTextureView)) {
			mEnableMutliView = enable;
			((NexSurfaceTextureView)videoNormalSurfaceView).enableMutliView(mEnableMutliView);
		}
	}

	@SuppressWarnings("deprecation")
	private void ChangeViewToNomalSurfaceView() {
		NexLog.d(LOG_TAG, "--->ChangeViewToNomalSurfaceView() :");

		removeGLRenderer();

		if (null != videoNormalSurfaceView) {
			if (NexPlayer.NEX_USE_RENDER_IOMX == nexPlayer.GetRenderMode()) {
				videoNormalSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // For
																							// HW
																							// Renderer
				NexLog.d(LOG_TAG, "--->SURFACE_TYPE_PUSH_BUFFERS");
			} else {
				videoNormalSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL); // For
																						// GB
																						// Android
																						// Renderer
				NexLog.d(LOG_TAG, "--->SURFACE_TYPE_NORMAL");
			}
			
			if(Build.VERSION.SDK_INT >= 17 && mUseSecure) {
				try {
					Class<?> cls = Class.forName("android.view.SurfaceView");
		    		Method ml = cls.getDeclaredMethod("setSecure", boolean.class);
		    		NexLog.d(LOG_TAG, "found setSecure method ");
		    		ml.invoke(videoNormalSurfaceView, mUseSecure);
	    		} catch(Exception e) {
	    			NexLog.d(LOG_TAG, "setSecure - Call Error!");
	    			e.printStackTrace();
	    		}
				//videoNormalSurfaceView.setSecure(mUseSecure);
			}

			if (videoSurfaceExists) {
				NexLog.d(LOG_TAG, "------>mNexPlayer.setDisplay :");
				if(mUseSurfaceTexture) {
					Surface surface = ((NexSurfaceTextureView) videoNormalSurfaceView).getSurfaceFromSurfaceTexture();
					if(surface != null)
						nexPlayer.setDisplay(((NexSurfaceTextureView) videoNormalSurfaceView).getSurfaceFromSurfaceTexture());
				} else {
					nexPlayer.setDisplay(videoNormalSurfaceHolder, 0);
				}
			} else {
				removeView(videoNormalSurfaceView);
				if (mEnableMutliView) {
					addView(videoNormalSurfaceView, 0, mLayoutParams); 
				} else {
					addView(videoNormalSurfaceView, mLayoutParams); 
				}

				setVisibility(this.visibility);
			}
		}

	}

	private void ChangeViewToOpenGLView() {
		NexLog.d(LOG_TAG, "--->ChangeViewToOpenGLView() :");
		if (videoNormalSurfaceView != null) {
			videoNormalSurfaceView.setVisibility(View.INVISIBLE);
			removeView(videoNormalSurfaceView);
			if(mUseSurfaceTextureView)
				((NexSurfaceTextureView) videoNormalSurfaceView).release();
		}

		if (initGLRenderer && glRenderer != null) {
			{
				NexLog.d(LOG_TAG, "------>mGLRenderer inited. init again for another videorenderer.");
				glRenderer.mReInitRenderer = true;
				glRenderer.requestRender();
			}
		} else {
			createAndAddGLRenderer();
			setVisibility(this.visibility);
		}

	}

	/**
	 * \brief  This method requests that the NexVideoRenderer view display a blank canvas (black).
	 *
	 * @deprecated For internal use only. Please do not use.
	 * 
	 * \since version 6.1
	 */
	public void clearCanvas() {
		if(videoPrepared == false)
			return;
		int rendermode = nexPlayer.GetRenderMode();
		if (rendermode != NexPlayer.NEX_USE_RENDER_OPENGL) {
			Canvas canvas;
			NexLog.d(LOG_TAG, "---->> clearCanvas()");

			if (rendermode != NexPlayer.NEX_USE_RENDER_IOMX) 
			{
				canvas = videoNormalSurfaceHolder.lockCanvas();
				NexLog.d(LOG_TAG, "---->> clearCanvas() 001");

				if (null == solidPaint) {
					solidPaint = new Paint();
				}

				if (canvas != null) {
					NexLog.d(LOG_TAG, "---->> clearCanvas() 002");
					solidPaint.setColor(0xFF000000);
					canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), solidPaint);
					videoNormalSurfaceHolder.unlockCanvasAndPost(canvas);
				}

				canvas = videoNormalSurfaceHolder.lockCanvas();

				if (canvas != null) {
					solidPaint.setColor(0xFF000000);
					canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), solidPaint);
					videoNormalSurfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
			else
			{
				mBlackScreen.setVisibility(View.VISIBLE);
				mBlackScreenOn = true;
			}
		}
		NexLog.d(LOG_TAG, "clearCanvas end");
	}
	
//	public void showCanvas()
//	{
//		if (glRenderer != null) {
//			glRenderer.mClearScreen = false;
//			glRenderer.requestRender();
//			try {
//				Thread.sleep(1);
//			} catch(Exception e)
//			{
//				NexLog.d(LOG_TAG, "exception in thread!");
//			}				
//		}
//	}

	private void createAndAddGLRenderer() {
		if (null == glRenderer) {
			glRenderer = new GLRenderer(getContext(), nexPlayer, new GLRenderer.IListener() {
				@Override
				public void onGLChangeSurfaceSize(int width, int height) {
					NexLog.d(LOG_TAG, "onGLChangeSurfaceSize()");

					if (null != NexVideoRenderer.this.preGLRendererListener) {
						NexVideoRenderer.this.preGLRendererListener.onGLChangeSurfaceSize(width, height);
					}
					
					NexVideoRenderer.this.initGLRenderer = true;
					NexVideoRenderer.this.setSurfaceSize(width, height);

					mHandler.post(new Runnable() {
						@Override
						public void run() {
							NexVideoRenderer.this.videoSizeListener.onSizeChanged();
						}
					});

					if (null != NexVideoRenderer.this.postGLRendererListener) {
						NexVideoRenderer.this.postGLRendererListener.onGLChangeSurfaceSize(width, height);
					}
				}
			}, getColorDepth());

			videoOpenGLSurfaceHolder = glRenderer.getHolder();
			videoOpenGLSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
			videoOpenGLSurfaceHolder.addCallback(surfaceHolderCallback);
			videoOpenGLSurfaceHolder.setFormat(PixelFormat.RGBA_8888);
			
			if(Build.VERSION.SDK_INT >= 17 && mUseSecure) {
				try {
					Class<?> cls = Class.forName("android.view.SurfaceView");
		    		Method ml = cls.getDeclaredMethod("setSecure", boolean.class);
		    		NexLog.d(LOG_TAG, "found setSecure method ");
		    		ml.invoke(glRenderer, mUseSecure);
	    		} catch(Exception e) {
	    			NexLog.d(LOG_TAG, "setSecure - Call Error!");
	    			e.printStackTrace();
	    		}
				//glRenderer.setSecure(mUseSecure);
			}
			
			addView(glRenderer);
		}
	}

	/**
	 * \brief The color depth associated with this view.
	 * 
	 * This is associated with the screen pixel format which can be retrieved by
	 * \link NexVideoRenderer.getScreenPixelFormat getScreenPixelFormat\endlink
	 * and set with \link NexVideoRenderer.setScreenPixelFormat
	 * setScreenPixelFormat\endlink.
	 * 
	 * \returns  One of the constants specified in \link android.graphics.PixelFormat\endlink.
	 * 
	 * \since version 6.1
	 */
	public int getColorDepth() {
		if (screenPixelFormat == PixelFormat.RGBA_8888 || screenPixelFormat == PixelFormat.RGBX_8888) {
			return PixelFormat.RGBA_8888;
		} else {
			return PixelFormat.RGB_565;
		}
	}

	/**
	 * \brief The \link android.graphics.Rect\endlink that will be displayed by
	 * this layout.
	 * 
	 * This is the position and size of the rectangle within the layout that will
	 * display the media content.
	 * 
	 * This can be changed with a call to \link NexVideoRenderer.setOutputPos
	 * setOutputPos\endlink.
	 * 
	 * \since version 6.1
	 */
	public Rect getDisplayedRect() {
		return displayedRect;
	}

	/**
	 * \brief This method gets the last captured video frame.
	 * 
	 * \returns  A \link android.graphics.Bitmap\endlink describing the
	 * last captured frame.
	 * 
	 * \since version 6.1
	 */
	public Bitmap getLastCapturedFrame() {
		return lastCapturedFrame;
	}

	/**
	 * \brief This method gets the current screen pixel format.
	 * 
	 * \returns  One of the constants specified in \link android.graphics.PixelFormat\endlink.
	 * 
	 * \since version 6.1
	 */
	public int getScreenPixelFormat() {
		return screenPixelFormat;
	}

	/**
	 * \brief This method gets the current media's video size.
	 * 
	 * @param outSize [out] 	A valid instance of \link android.graphics.Point\endlink whose
	 * 						values will be set by this method.
	 * 
	 * \since version 6.1
	 */
	public void getVideoSize(Point outSize) {
		outSize.set(videoSize.x, videoSize.y);
	}

	/**
	 * \brief This method initializes the NexVideoRenderer instance.
	 * 
	 * @param nexPlayer	An instance of \link NexPlayer\endlink after its \link NexPlayer.init init method\endlink
	 * 					has been called.
	 * 
	 * \since version 6.1
	 */
	@SuppressLint("InlinedApi")
	public void init(NexPlayer nexPlayer) {
		NexLog.d(LOG_TAG, "NexVideoRenderer.init:" + nexPlayer.toString());
		videoInitEnd = initGLRenderer = videoSurfaceExists = false;
		videoPrepared = false;
		
		int visibility = this.visibility;
		setVisibility(View.INVISIBLE);
		this.visibility = visibility;

		nexPlayer.setVideoRendererListener(iVideoRendererListener);
		nexPlayer.addReleaseListener(new NexPlayer.IReleaseListener() {
			@Override
			public void onPlayerRelease(NexPlayer mp) {
				if(mp.equals(NexVideoRenderer.this.nexPlayer))
				{
					NexLog.d(LOG_TAG, "onPlayerRelease : nexplayer is released");
					NexVideoRenderer.this.release();
				}
			}
		});

		this.nexPlayer = nexPlayer;
		keepScreenOn(true);
		if(mUseSurfaceTextureView) {
			((NexSurfaceTextureView)videoNormalSurfaceView).setNexPlayer(nexPlayer);
			((NexSurfaceTextureView)videoNormalSurfaceView).setSurfaceTextureMode(mUseSurfaceTexture, mUseRenderThread);
		}
	}

	private boolean isAndroidTV() {
		boolean ret = false;
		UiModeManager uiModeManager = (UiModeManager) getContext().getSystemService(Context.UI_MODE_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO &&
				uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
			NexLog.d(LOG_TAG, "Running on a TV Device");
			ret = true;
		}
		return ret;
	}

	private void internal_init() {
		mEnableMutliView = false;
		mSecureSurface = false;
		orientation = getContext().getResources().getConfiguration().orientation;
		displayedRect = new Rect(0, 0, 0, 0);

		int layoutSizeParam = LayoutParams.FILL_PARENT;
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO )
			layoutSizeParam = LayoutParams.MATCH_PARENT;
		
		mLayoutParams = new LayoutParams(layoutSizeParam, layoutSizeParam);
		mLayoutParams.gravity = Gravity.NO_GRAVITY;

		iVideoRendererListener = new NexVideoRenderer.IVideoRendererListener();
		surfaceHolderCallback = new NexVideoRenderer.SurfaceHolderCallback();
		mBlackScreen = new View(this.getContext());
		mBlackScreen.setBackgroundColor(0xFF000000);

		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ) {
			videoNormalSurfaceView = new NexSurfaceTextureView(getContext());
			mUseSurfaceTextureView = true;
		}
		else
			videoNormalSurfaceView = new SurfaceView(getContext());

		videoNormalSurfaceHolder = videoNormalSurfaceView.getHolder();
		videoNormalSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
		videoNormalSurfaceHolder.addCallback(surfaceHolderCallback);
		videoNormalSurfaceView.setVisibility(View.INVISIBLE);
		//videoNormalSurfaceHolder.setFormat(PixelFormat.RGBA_8888);
		
		addView(videoNormalSurfaceView, mLayoutParams);
		addView(mBlackScreen);
		mBlackScreen.setVisibility(View.INVISIBLE);
		mBlackScreenOn = false;
		
		if(screenPixelFormat == 0)
			setScreenPixelFormatFromDefaultDisplay();

		boolean useSurfaceTextureMode = false;
		boolean useSeparateThread = false;
		if(isAndroidTV())
			useSurfaceTextureMode = false;
		setUseSurfaceTexture(useSurfaceTextureMode, useSeparateThread);
	}

	/**
	 * \brief This method checks NexVideoRenderer's current initialization status.
	 * 
	 * The application should check this method before calling any method other
	 * than \link NexVideoRenderer.init init\endlink or one of the get methods.
	 * 
	 * \returns TRUE if NexVideoRenderer is initialized, and otherwise FALSE.
	 * 
	 * \since version 6.1
	 */
	public boolean isInitialized() {
		NexLog.d(LOG_TAG, "VideoRenderer videoInitEnd:" + videoInitEnd + ", initGLRenderer:" + initGLRenderer + ", videoSurfaceExists:" + videoSurfaceExists);
		return videoInitEnd && (initGLRenderer || videoSurfaceExists);
	}

    /**
     * \brief  This method releases resources that are used by the instance of \c NexVideoRenderer.
     *
     * This should be called before the \link NexPlayer.release \endlink method is called
     * when the instance is no longer needed.
     *
     * \since version 6.23
     */
    public void release()
    {
		if (videoNormalSurfaceView != null) {
			videoNormalSurfaceView.setVisibility(View.INVISIBLE);

			if(mUseSurfaceTextureView)
				((NexSurfaceTextureView) videoNormalSurfaceView).release();
		}

        if (videoNormalSurfaceHolder != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                videoNormalSurfaceHolder.getSurface().release();

        }

		removeGLRenderer();
		mHandler.removeCallbacksAndMessages(null);
		
		videoInitEnd = initGLRenderer = videoSurfaceExists = false;
		videoPrepared = false;
    }		
	
	/**
	 * \brief This method checks to see if NexVideoRenderer is currently using OpenGL.
	 * 
	 * \returns TRUE if NexVideoRenderer is using OpenGL, otherwise FALSE.
	 * 
	 * \since version 6.1
	 */
	public boolean isUsingOpenGL() {
		return useOpenGL;
	}

	/**
	 * \brief   This method sends a signal to notify NexVideoRenderer of a \link
	 * android.content.Configuration Configuration\endlink change.
	 * 
	 * Overriding \link android.app.Activity.onConfigurationChanged Activity's
	 * onConfigurationChanged\endlink or \link android.app.Fragment Fragment's
	 * onConfigurationChanged\endlink is recommended and a pass-through to this
	 * method is required to receive timely notifications of any change of size.
	 * 
	 * @param newConfig  The new \link android.content.Configuration\endlink .    
	 * 
	 * \since version 6.1
	 */
	public void onConfigChanged(Configuration newConfig) {
		if (orientation != newConfig.orientation) {
			orientation = newConfig.orientation;

			getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

				@SuppressLint("NewApi")
				@SuppressWarnings("deprecation")
				@Override
				public void onGlobalLayout() {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						NexVideoRenderer.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					} else {
						NexVideoRenderer.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
					NexVideoRenderer.this.videoSizeListener.onSizeChanged();
				}
			});
		}
	}
	
	/**
	 * \brief This method informs the view that the activity is paused. 
	 * 
	 * The owner of this view must call this method when the activity is paused. 
	 * Calling this method will pause the rendering thread. 
	 * 
	 * \since version 6.26
	 */
	public void onPause() {
	    NexLog.d(LOG_TAG, "[nexPlayerSDK_onPause] onPause called");
		mPaused = true;

		if (mEnableMutliView) {
	    	if (videoNormalSurfaceView != null) {
				videoNormalSurfaceView.setVisibility(View.INVISIBLE);
			}
		}

		if(null != glRenderer ) {
            glRenderer.onPause();
        }
    }
	
	/**
	 * \brief This method informs the view that the activity has resumed. 
	 * 
	 * The owner of this view must call this method when the activity is being resumed. 
	 * Calling this method will recreate the OpenGL display and resume the rendering thread. 
	 * 
	 * \since version 6.26
	 */
	public void onResume() {
	    NexLog.d(LOG_TAG, "[nexPlayerSDK_onResume] onResume called");
	    
	    if (mEnableMutliView) {
	    	if (mPaused == true && videoNormalSurfaceView != null) {	    		
				videoNormalSurfaceView.setVisibility(View.VISIBLE);	
	    	}
	    }		
        
        if(null != glRenderer ) {
            glRenderer.onResume();
        }

		mPaused = false;
    }

	public View getView() {
		return this;
	}

	private class IVideoRendererListener implements NexPlayer.IVideoRendererListener {
		@Override
		public void onVideoRenderCapture(NexPlayer mp, int width, int height, int pixelbyte, Object rgbBuffer) {
			NexLog.d(LOG_TAG, "onVideoRenderCapture called");

			if (null != NexVideoRenderer.this.preNexPlayerVideoRendererListener) {
				NexVideoRenderer.this.preNexPlayerVideoRendererListener.onVideoRenderCapture(mp, width, height,
						pixelbyte, rgbBuffer);
			}

			Bitmap bitmap = Bitmap.createBitmap(width, height, pixelbyte == 2 ? Config.RGB_565 : Config.ARGB_8888);
			ByteBuffer RGBBuffer = (ByteBuffer) rgbBuffer;

			if (RGBBuffer.capacity() > 0 && null != bitmap) {
				RGBBuffer.asIntBuffer();
				bitmap.copyPixelsFromBuffer(RGBBuffer);

				NexVideoRenderer.this.lastCapturedFrame = bitmap;
			} else {
				NexVideoRenderer.this.lastCapturedFrame = null;
			}

			if (null != NexVideoRenderer.this.postNexPlayerVideoRendererListener) {
				NexVideoRenderer.this.postNexPlayerVideoRendererListener.onVideoRenderCapture(mp, width, height,
						pixelbyte, rgbBuffer);
			}
		}

		@Override
		public void onVideoRenderCreate(NexPlayer mp, int width, int height, Object rgbBuffer) {

			if (null != NexVideoRenderer.this.preNexPlayerVideoRendererListener) {
				NexVideoRenderer.this.preNexPlayerVideoRendererListener.onVideoRenderCreate(mp, width, height,
						rgbBuffer);
			}
			
			int[] arrSize = new int[2];
			mp.getSARInfo(arrSize);
			
			NexLog.d(LOG_TAG, "orignal onVideoRenderCreate W : " + width + " H : " + height);
			
			float nRatioOffset = (float)arrSize[1] / (float)arrSize[0];
			
			if (0 < nRatioOffset)
				height *= nRatioOffset;
			
			NexLog.d(LOG_TAG, "onVideoRenderCreate W : " + width + " H : " + height + " offset " + nRatioOffset);

			if(mUseSurfaceTextureView)
				((NexSurfaceTextureView)videoNormalSurfaceView).mVideoRenderListener.onVideoRenderCreate(mp, width, height, rgbBuffer);

			if (NexVideoRenderer.this.activityPaused) {
				if (null != NexVideoRenderer.this.postNexPlayerVideoRendererListener) {
					NexVideoRenderer.this.postNexPlayerVideoRendererListener.onVideoRenderCreate(mp, width, height,
							rgbBuffer);
				}
				return;
			}

			NexVideoRenderer.this.videoInitEnd = true;
			NexVideoRenderer.this.setVideoSize(width, height);
			if (NexVideoRenderer.this.firstVideoRenderCreate == false) {
				NexVideoRenderer.this.firstVideoRenderCreate = true;
				if (null != NexVideoRenderer.this.videoSizeListener) {
					NexVideoRenderer.this.videoSizeListener.onFirstVideoRenderCreate();
				}
			}

			int rendermode = NexVideoRenderer.this.nexPlayer.GetRenderMode();
			if (rendermode == NexPlayer.NEX_USE_RENDER_JAVA) {
				if (NexVideoRenderer.this.screenPixelFormat == PixelFormat.RGBA_8888
						|| NexVideoRenderer.this.screenPixelFormat == PixelFormat.RGBX_8888) {
					NexVideoRenderer.this.frameBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
				} else {
					NexVideoRenderer.this.frameBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
				}
				NexVideoRenderer.this.nexPlayer.SetBitmap(NexVideoRenderer.this.frameBitmap);
			}

			if (null != NexVideoRenderer.this.postNexPlayerVideoRendererListener) {
				NexVideoRenderer.this.postNexPlayerVideoRendererListener.onVideoRenderCreate(mp, width, height,
						rgbBuffer);
			}
		}

		@Override
		public void onVideoRenderDelete(NexPlayer mp) {
			NexLog.d(LOG_TAG, "onVideoRenderDelete:");

			if (null != NexVideoRenderer.this.preNexPlayerVideoRendererListener) {
				NexVideoRenderer.this.preNexPlayerVideoRendererListener.onVideoRenderDelete(mp);
			}

			NexVideoRenderer.this.videoInitEnd = false;

			if(mUseSurfaceTextureView)
				((NexSurfaceTextureView)videoNormalSurfaceView).mVideoRenderListener.onVideoRenderDelete(mp);

			if (null != NexVideoRenderer.this.postNexPlayerVideoRendererListener) {
				NexVideoRenderer.this.postNexPlayerVideoRendererListener.onVideoRenderDelete(mp);
			}
			NexVideoRenderer.this.mTextureLoaded = false;
		}

		@Override
		public void onVideoRenderPrepared(NexPlayer mp) {
			NexLog.d(LOG_TAG, "onVideoRenderPrepared! ");

			if (null != NexVideoRenderer.this.preNexPlayerVideoRendererListener) {
				NexVideoRenderer.this.preNexPlayerVideoRendererListener.onVideoRenderPrepared(mp);
			}

			NexVideoRenderer.this.videoPrepared = true;
			NexVideoRenderer.this.videoInitEnd = false;
			NexVideoRenderer.this.firstVideoRenderCreate = false;
			if (NexVideoRenderer.this.nexPlayer.GetRenderMode() == NexPlayer.NEX_USE_RENDER_OPENGL) {
				NexVideoRenderer.this.useOpenGL = true;
				NexLog.d(LOG_TAG, "UseOpenGL! ChangeViewToOpenGLView start");
				mHandler.post(new Runnable() {
					public void run() {
						NexVideoRenderer.this.ChangeViewToOpenGLView();
					}
				});
			} else {
				NexVideoRenderer.this.useOpenGL = false;
				NexLog.d(LOG_TAG, "Not UseOpenGL! ChangeViewToNomalSurfaceView start");
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						NexVideoRenderer.this.ChangeViewToNomalSurfaceView();
					}
				});
			}

			if(mUseSurfaceTextureView)
				((NexSurfaceTextureView)videoNormalSurfaceView).mVideoRenderListener.onVideoRenderPrepared(mp);

			if (null != NexVideoRenderer.this.postNexPlayerVideoRendererListener) {
				NexVideoRenderer.this.postNexPlayerVideoRendererListener.onVideoRenderPrepared(mp);
			}
		}

		@Override
		public void onVideoRenderRender(NexPlayer mp) {

//			NexLog.d(LOG_TAG, "onVideoRenderRender called");
			if(mBlackScreenOn)
			{
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mBlackScreen.setVisibility(View.INVISIBLE);
					}
				});
				mBlackScreenOn = false;
			}

			if (null != NexVideoRenderer.this.preNexPlayerVideoRendererListener) {
				NexVideoRenderer.this.preNexPlayerVideoRendererListener.onVideoRenderRender(mp);
			}

			if (NexVideoRenderer.this.useOpenGL && NexVideoRenderer.this.glRenderer != null) {
				NexVideoRenderer.this.glRenderer.requestRender();
				NexVideoRenderer.this.mTextureLoaded = true;

				if (null != NexVideoRenderer.this.postNexPlayerVideoRendererListener) {
					NexVideoRenderer.this.postNexPlayerVideoRendererListener.onVideoRenderRender(mp);
				}
				return;
			}

			// for NexPlayer.NEX_USE_RENDER_JAVA
			if (NexVideoRenderer.this.videoSurfaceExists && NexVideoRenderer.this.frameBitmap != null /*
																									 * &&
																									 * skipped
																									 * ++
																									 * >
																									 * 0
																									 */) {

				// skipped = 0;
				// if( mp != null)
				// mFrameBitmap.copyPixelsFromBuffer(mRGBBuffer);
				// else
				// NexLog.d(LOG_TAG,"------>onVideoRenderRender(null) redisplay existing frame");
				Canvas canvas;
				canvas = NexVideoRenderer.this.videoNormalSurfaceHolder.lockCanvas();
				// mp.SetBitmap(mFrameBitmap);

				if (canvas != null) {
					/*
					 * // deprecated if (NexVideoRenderer.this.clearReq > 0) {
					 * NexVideoRenderer.this.clearReq--; if (null ==
					 * NexVideoRenderer.this.solidPaint) {
					 * NexVideoRenderer.this.solidPaint = new Paint(); }
					 * NexVideoRenderer.this.solidPaint.setColor(0xFF000000);
					 * canvas.drawRect(0, 0, canvas.getWidth(),
					 * canvas.getHeight(), NexVideoRenderer.this.solidPaint); }
					 */
					Rect rctSrc = new Rect(0, 0, NexVideoRenderer.this.frameBitmap.getWidth(),
							NexVideoRenderer.this.frameBitmap.getHeight());

					canvas.drawColor(Color.BLACK); // SWSEO 2010/09/30

					if (null == NexVideoRenderer.this.blitPaint) {
						NexVideoRenderer.this.blitPaint = new Paint();
						NexVideoRenderer.this.blitPaint.setFilterBitmap(shouldFilterBitmap);
					}
					Rect dstRect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
					canvas.drawBitmap(NexVideoRenderer.this.frameBitmap, rctSrc, /* displayedRect */
							dstRect, NexVideoRenderer.this.blitPaint);

					NexVideoRenderer.this.videoNormalSurfaceHolder.unlockCanvasAndPost(canvas);
				}
			}

			if (null != NexVideoRenderer.this.postNexPlayerVideoRendererListener) {
				NexVideoRenderer.this.postNexPlayerVideoRendererListener.onVideoRenderRender(mp);
			}
		}
	}

	private void removeGLRenderer() {
		if (null != glRenderer) {
			videoOpenGLSurfaceHolder.removeCallback(surfaceHolderCallback);
			videoOpenGLSurfaceHolder = null;

			removeView(glRenderer);
			glRenderer.release();
			glRenderer = null;
		}
		initGLRenderer = false;
	}

	/**
	 * 
	 * \brief This method resets the \c surfaceView or \c GLSurfaceView of \c NexVideoRenderer.
	 * 
	 * This method informs the \c NexVideoRenderer that the holder of \c NexVideoRenderer has changed. 
	 * After this method is called, \c onSizeChanged() may be called.
	 * 
	 * \since version 6.0.9
	 */
	public void resetSurface() {
		if (this.videoPrepared)
		{
			NexLog.d(LOG_TAG, "resetSurface");
			setVisibility(View.INVISIBLE);
			
			if (NexPlayer.NEX_USE_RENDER_OPENGL == nexPlayer.GetRenderMode())
			{
				removeGLRenderer();
			}
			else
			{
				updateViewLayout(videoNormalSurfaceView, mLayoutParams);
			}

			setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * \brief This method requests that the canvas be cleared.
	 * 
	 * @deprecated This is no longer supported; use \link
	 *             NexVideoRenderer.clearCanvas clearCanvas\endlink instead.
	 */
	public void requestClear() {
//		clearReq = 2;
	}

	/**
	 * \brief This method notifies NexVideoRenderer of an \link Activity.onPause
	 * onPause\endlink callback.
	 * 
	 * This is to prevent NexVideoRenderer from performing unnecessary
	 * operations that could potentially lead to a crash after the activity or
	 * fragment is in the background.
	 * 
	 * \param bPaused  TRUE when the Activity is paused, otherwise FALSE.
	 * 
	 * \since version 6.1
	 */
	public void setActivityPaused(boolean bPaused) {
		activityPaused = bPaused;
	}

	private void setDisplayedRect(Rect r) {
		if (null != r) {
			displayedRect = r;
			if (null != videoSizeListener) {
				videoSizeListener.onDisplayedRectChanged();
			}
		}
	}

	/**
	 * \brief This method sets the \link NexVideoRenderer.IListener IListener\endlink.
	 * 
	 * @param listener 	An \link NexVideoRenderer.IListener IListener\endlink instance
	 * requesting the events that this NexVideoRenderer generates.
	 * 
	 * \since version 6.1
	 */
	public void setListener(IListener listener) {
		videoSizeListener = listener;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if( this.videoSizeListener != null )
			this.videoSizeListener.onSizeChanged();
	}

	/**
	 * \brief  This method sets the displayed rectangle's position and size.
	 * 
	 * Within the NexVideoRenderer layout, these parameters will be used to set
	 * the output position and size of the media content.
	 * 
	 * Any time the size of the layout is changed, a rotation occurs, or the
	 * size of the content changes, the application should call this method to
	 * reset the display boundaries.
	 * 
	 * The parameters \c left and \c top define the top left-hand corner of the rectangle 
         * where video contents will be displayed, while \c width and \c height define the size
         * of this display rectangle.
	 * 
	 * \param left 	The horizontal position in pixels of the top left-hand corner of the desired rectangle
	 * 				within the NexVideoRenderer layout to start rendering the media content.
	 * \param top	The vertical position in pixels of the top left-hand corner of the desired rectangle
	 * 				within the NexVideoRenderer layout to start rendering the media content.
	 * \param width		The width in pixels of the desired rectangle within the NexVideoRenderer
	 *					layout to render the media content.
	 * \param height	The height in pixels of the desired rectangle within the NexVideoRenderer
	 *					layout to render the media content.
	 *            
	 * \since version 6.1 
	 */
	public void setOutputPos(int left, int top, int width, int height) {
		NexLog.d(LOG_TAG, "setOutputPos left:" + left + " top:" + top + " width:" + width + " height:" + height + " getW:"
				+ getWidth() + " getH:" + getHeight());
		if(getWidth() == 0 || getHeight() == 0)
		{
			NexLog.d(LOG_TAG, "NexVideoRenderer.init() is not called. setOutputPos() must be called after init() called");
			return ;
		}
		
		if(width == 0 || height == 0)
		{
			NexLog.d(LOG_TAG, "setOutputPos : width or height is zero. width and height should be bigger than zero");
			return ;
		}
		
		Rect outputPos = new Rect(left, top, left + width, top + height);
		setDisplayedRect(outputPos);
		if(!mUseSurfaceTexture) {
			mLayoutParams.width = displayedRect.width();
			mLayoutParams.height = displayedRect.height();
			mLayoutParams.leftMargin = displayedRect.left;
			mLayoutParams.topMargin = displayedRect.top;
		}
		
		if (useOpenGL) {
			if (isInitialized()) {
				nexPlayer.setOutputPos(left, top, width, height);
				if(mTextureLoaded)
				{
					iVideoRendererListener.onVideoRenderRender(nexPlayer);
				}
			}
		} else {

			if(mUseSurfaceTexture) {
				((NexSurfaceTextureView) videoNormalSurfaceView).setOutputPos(left, top, width, height);
			} else {
				mHandler.post(new Runnable() {
					public void run() {
						videoNormalSurfaceView.setLayoutParams(mLayoutParams);

						nexPlayer.setOutputPos(0, 0, mLayoutParams.width, mLayoutParams.height);
						iVideoRendererListener.onVideoRenderRender(nexPlayer);
					}
				});
			}
		}
	}

	/**
	 * \brief This method sets a listener for handling the finer details of the video renderer.
	 * 
	 * Setting this listener is absolutely optional and intended for the experts who want finer control of the rendering process.
	 *  
	 * @param postGLRendererListener
	 *            An instance of the \link GLRenderer.IListener\endlink that requests the callbacks from GLRenderer to handle them after NexVideoRenderer 
	 *            has finished performing its operations.
	 *            
	 * \since version 6.1
	 */
	public void setPostGLRendererListener(GLRenderer.IListener postGLRendererListener) {
		this.postGLRendererListener = postGLRendererListener;
	}

	/**
	 * \brief This method sets a listener for handling the finer details of the video renderer.
	 * 
	 * Setting this listener is absolutely optional and intended for the experts who want finer control of the rendering process.
	 *  
	 * @param postNexPlayerVideoRendererListener
	 *            An instance of the \link NexPlayer.IVideoRendererListener\endlink that requests the callbacks from NexPlayer to handle them
	 *            after NexVideoRenderer has finished performing its operations.
	 *            
	 * \since version 6.1
	 */
	public void setPostNexPlayerVideoRendererListener(
			NexPlayer.IVideoRendererListener postNexPlayerVideoRendererListener) {
		this.postNexPlayerVideoRendererListener = postNexPlayerVideoRendererListener;
	}

	/**
	 * \brief This method sets a listener for handling the finer details of the video renderer.
	 * 
	 * Setting this listener is absolutely optional and intended for the experts who want finer control of the rendering process.
	 *  
	 * @param postSurfaceHolderCallback
	 *            An instance of \link SurfaceHolder.Callback\endlink that requests the callbacks from the Surface to handle them
	 *            after NexVideoRenderer has finished performing its operations.
	 *            
	 * \since version 6.1
	 */
	public void setPostSurfaceHolderCallback(SurfaceHolder.Callback postSurfaceHolderCallback) {
		this.postSurfaceHolderCallback = postSurfaceHolderCallback;
	}

	/**
	 * \brief This method sets a listener for handling the finer details of the video renderer.
	 * 
	 * Setting this listener is absolutely optional and intended for the experts who want finer control of the rendering process.
	 *  
	 * @param preGLRendererListener
	 *            An instance of \link GLRenderer.IListener\endlink that requests the callbacks from GLRenderer to handle them before NexVideoRenderer 
	 *            has started performing its operations.
	 * 
	 * \since version 6.1
	 */
	public void setPreGLRendererListener(GLRenderer.IListener preGLRendererListener) {
		this.preGLRendererListener = preGLRendererListener;
	}

	/**
	 * \brief This method sets a listener for handling the finer details of the video renderer.
	 * 
	 * Setting this listener is absolutely optional and intended for the experts who want finer control of the rendering process.
	 *  
	 * @param preNexPlayerVideoRendererListener
	 *            An instance of \link NexPlayer.IVideoRendererListener\endlink that requests the callbacks from NexPlayer&trade;&nbsp;to handle them
	 *            before NexVideoRenderer has started performing its operations.
	 * 
	 * \since version 6.1
	 */
	public void setPreNexPlayerVideoRendererListener(NexPlayer.IVideoRendererListener preNexPlayerVideoRendererListener) {
		this.preNexPlayerVideoRendererListener = preNexPlayerVideoRendererListener;
	}

	/**
	 * \brief This method sets a listener for handling the finer details of the video renderer.
	 * 
	 * Setting this listener is absolutely optional and intended for the experts who want finer control of the rendering process.
	 *  
	 * @param preSurfaceHolderCallback
	 *            An instance of \link SurfaceHolder.Callback\endlink that requests the callbacks from the Surface to handle them
	 *            before NexVideoRenderer has started performing its operations.
	 * 
	 * \since version 6.1
	 */
	public void setPreSurfaceHolderCallback(SurfaceHolder.Callback preSurfaceHolderCallback) {
		this.preSurfaceHolderCallback = preSurfaceHolderCallback;
	}

	/**
	 * \brief This method sets the current screen pixel format.
	 * 
	 * If the model is "Milestone", the screen pixel format will be forced to
	 * \link android.graphics.PixelFormat.RGB_565 RGB_565\endlink.
	 * 
	 * @param screenPixelFormatToSet  One of the constants specified in \link android.graphics.PixelFormat\endlink.
	 * 
	 * \since version 6.1
	 */
	public void setScreenPixelFormat(int screenPixelFormatToSet) {
		screenPixelFormat = screenPixelFormatToSet;

		if (android.os.Build.MODEL.equals("Milestone")) {
			NexLog.d(LOG_TAG, "THIS IS DROID. ScreenPixelFormat set to RGB_565");
			screenPixelFormat = PixelFormat.RGB_565;
		}
	}

	@SuppressWarnings("deprecation")
	private void setScreenPixelFormatFromDefaultDisplay() {
		int devicePixelFormat;

		if (Build.VERSION.SDK_INT >= 17) {
			devicePixelFormat = PixelFormat.RGBA_8888;
		} else {
			Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			devicePixelFormat = display.getPixelFormat();
		}

		if (devicePixelFormat == PixelFormat.RGBA_8888 || devicePixelFormat == PixelFormat.RGBX_8888
				|| devicePixelFormat == PixelFormat.RGB_888 || devicePixelFormat == 5) {
			setScreenPixelFormat(PixelFormat.RGBA_8888);
			NexLog.d(LOG_TAG, "888 : DevicePixelFormat:" + devicePixelFormat + "  ScreenPixelFormat:"
					+ screenPixelFormat);
		} else {
			setScreenPixelFormat(PixelFormat.RGB_565);
			NexLog.d(LOG_TAG, "565 : DevicePixelFormat:" + devicePixelFormat + "  ScreenPixelFormat:"
					+ screenPixelFormat);
		}
	}

	/**
	 * \brief This method sets whether or not the bitmap should be filtered when the Java renderer is used.
	 * 
	 * @param shouldFilterBitmap  TRUE if the bitmap should be filtered, and otherwise FALSE.
	 * 
	 * \since version 6.1
	 */
	public void setShouldFilterBitmap(boolean shouldFilterBitmap) {
		this.shouldFilterBitmap = shouldFilterBitmap;
	}

	private void setSurfaceSize(int width, int height) {
		surfaceSize.set(width, height);
	}

	private void setVideoSize(int width, int height) {
		videoSize.set(width, height);
		if (null != videoSizeListener) {
			videoSizeListener.onVideoSizeChanged();
		}
	}

	/**
	 * \brief This method sets the visibility of the NexVideoRenderer layout.
	 * 
	 * @param visibility  A constant from \link android.view.View\endlink.
	 * 
	 * \since version 6.1
	 */
	@Override
	public void setVisibility(int visibility) {
		this.visibility = visibility;
		if (View.VISIBLE == visibility) {
			if (this.videoPrepared) {
				if (null == nexPlayer) {
					NexLog.w(LOG_TAG, "NexPlayer has not been set yet");
					return;
				}
				super.setVisibility(visibility);
				if (NexPlayer.NEX_USE_RENDER_OPENGL == nexPlayer.GetRenderMode()) {
					useOpenGL = true;
					videoNormalSurfaceView.setVisibility(View.INVISIBLE);
					createAndAddGLRenderer();
					glRenderer.setVisibility(View.VISIBLE);
				} else {
					useOpenGL = false;
					if (NexPlayer.NEX_USE_RENDER_IOMX == nexPlayer.GetRenderMode()) {
						videoNormalSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // For HW Renderer
						NexLog.d(LOG_TAG, "--->SURFACE_TYPE_PUSH_BUFFERS");
					} else {
						videoNormalSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL); // For GB Android Renderer
						NexLog.d(LOG_TAG, "--->SURFACE_TYPE_NORMAL");
					}					
					videoNormalSurfaceView.setVisibility(View.VISIBLE);
					removeGLRenderer();
				}
			}
		} else {
			if (videoNormalSurfaceView != null) {
				videoNormalSurfaceView.setVisibility(View.INVISIBLE);
			}
			removeGLRenderer();
			super.setVisibility(visibility);
		}
	}


	/**
     * \brief This method can keep the screen turned \c ON or \c OFF.
     * \param enable \c TRUE : Keep the screen turned \c ON.
     *               \c FALSE: Screen turns \c OFF after a while.
     * \default : \c TRUE.
     *
     * \since version 6.44
	 */
	public void keepScreenOn(boolean enable)
	{
		if(NexPlayer.NEX_USE_RENDER_IOMX == nexPlayer.GetRenderMode())
		{
			if(videoNormalSurfaceHolder != null)
			{
				videoNormalSurfaceHolder.setKeepScreenOn(enable);
			}
		}
	}

	private class SurfaceHolderCallback implements SurfaceHolder.Callback {
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			NexLog.d(LOG_TAG, "------> surfaceChanged()F:" + format + " W: " + width + " H : " + height);
	
			if (null != NexVideoRenderer.this.preSurfaceHolderCallback) {
				NexVideoRenderer.this.preSurfaceHolderCallback.surfaceChanged(holder, format, width, height);
			}
	
			// kwangsik.lee
			if (NexPlayer.NEX_USE_RENDER_OPENGL == NexVideoRenderer.this.nexPlayer.GetRenderMode()) {
				NexLog.d(LOG_TAG, "-----------> RendererMode is Changed to OpenGL!");
	
				if (null != NexVideoRenderer.this.postSurfaceHolderCallback) {
					NexVideoRenderer.this.postSurfaceHolderCallback.surfaceChanged(holder, format, width, height);
				}
				return;
			}
	
			// onVideoRenderRender(null); // Redraw current frame; on some devices
			// (Galaxy S, maybe others too) the scaling is wrong after unlocking if
			// we don't do this
			NexVideoRenderer.this.setSurfaceSize(width, height);

			if(mUseSurfaceTextureView)
				((NexSurfaceTextureView)videoNormalSurfaceView).surfaceChanged(holder, format, width, height);

			if (null != NexVideoRenderer.this.postSurfaceHolderCallback) {
				NexVideoRenderer.this.postSurfaceHolderCallback.surfaceChanged(holder, format, width, height);
			}
		}
	
		@Override
		public void surfaceCreated(SurfaceHolder arg0) {
			NexLog.d(LOG_TAG, "------> surfaceCreated()");
	
			if (null != NexVideoRenderer.this.preSurfaceHolderCallback) {
				NexVideoRenderer.this.preSurfaceHolderCallback.surfaceCreated(arg0);
			}
	
	
			if (null == NexVideoRenderer.this.nexPlayer) {
				NexLog.w(LOG_TAG, "NexPlayer has not been set yet");
	
				if (null != NexVideoRenderer.this.postSurfaceHolderCallback) {
					NexVideoRenderer.this.postSurfaceHolderCallback.surfaceCreated(arg0);
				}
				return;
			}
	
			int renderMode = NexVideoRenderer.this.nexPlayer.GetRenderMode();
	
			if (NexPlayer.NEX_USE_RENDER_OPENGL == renderMode) {
				NexLog.d(LOG_TAG, "-----------> RendererMode is Changed to OpenGL!");
	
				if (null != NexVideoRenderer.this.postSurfaceHolderCallback) {
					NexVideoRenderer.this.postSurfaceHolderCallback.surfaceCreated(arg0);
				}
				return;
			}

			if(mUseSurfaceTextureView)
				((NexSurfaceTextureView)videoNormalSurfaceView).surfaceCreated(arg0);
			else
				NexVideoRenderer.this.nexPlayer.setDisplay(NexVideoRenderer.this.videoNormalSurfaceHolder, 0);
	
			Rect rect = null;
			rect = NexVideoRenderer.this.videoNormalSurfaceHolder.getSurfaceFrame();
	
			int mScreenWidth = rect.width();
			int mScreenHeight = rect.height();
			boolean surfacesizechanged = false;
			if (NexVideoRenderer.this.surfaceSize.x == 0 || NexVideoRenderer.this.surfaceSize.y == 0) {
				surfacesizechanged = true;
			}
	
			if (surfacesizechanged) {
				NexVideoRenderer.this.setSurfaceSize(mScreenWidth, mScreenHeight);
			}
	
			NexLog.d(LOG_TAG, "mSurfaceWidth : " + NexVideoRenderer.this.surfaceSize.x + "mSurfaceHeight : " + NexVideoRenderer.this.surfaceSize.y);
			NexVideoRenderer.this.setDisplayedRect(new Rect(0, 0, NexVideoRenderer.this.surfaceSize.x, NexVideoRenderer.this.surfaceSize.y));
	
			NexVideoRenderer.this.videoSurfaceExists = true;
			
			if (null != NexVideoRenderer.this.postSurfaceHolderCallback) {
				NexVideoRenderer.this.postSurfaceHolderCallback.surfaceCreated(arg0);
			}
		}
	
		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {
			NexLog.d(LOG_TAG, "------> surfaceDestroyed()");

			if (null != NexVideoRenderer.this.preSurfaceHolderCallback) {
				NexVideoRenderer.this.preSurfaceHolderCallback.surfaceDestroyed(arg0);
			}

			NexVideoRenderer.this.videoSurfaceExists = false;

			if(mUseSurfaceTextureView) {
				((NexSurfaceTextureView)videoNormalSurfaceView).setVideoInfo(mVideoCodecName,mSecureSurface);
				((NexSurfaceTextureView)videoNormalSurfaceView).surfaceDestroyed(arg0);
			}
			else {
				if (NexPlayer.NEX_USE_RENDER_IOMX == NexVideoRenderer.this.nexPlayer.GetRenderMode()) {
					NexVideoRenderer.this.nexPlayer.setDisplay(null, 0);
				}
			}

			if (null != NexVideoRenderer.this.postSurfaceHolderCallback) {
				NexVideoRenderer.this.postSurfaceHolderCallback.surfaceDestroyed(arg0);
			}
		}	
	}
	
	/**
         * \brief This method prevents the user from recording the screen on devices running the Android KitKat (4.4) OS and above. 
         *
         * Call this API right after \c init if screen recording should be prevented.
         *
         * \param usesecure Set to \c TRUE to turn on Secure mode to prevent screen recording; otherwise FALSE. This API works on devices running the KitKat OS and above.
         * 
         * \since version 6.7
	 */
	public void setSurfaceSecure(Boolean usesecure){
			mUseSecure = usesecure;
		}

	@Override
	public void setZOrderMediaOverlay(boolean isMediaOverlay) {
		videoNormalSurfaceView.setZOrderMediaOverlay(isMediaOverlay);
	}

	private boolean mUseSurfaceTexture = true;
	private boolean mUseRenderThread = false;
	private boolean mUseSurfaceTextureView = false;

	/**
	 * \brief The developer can choose SurfaceTexture mode instead of SurfaceView.
	 * SurfaceTexture mode provides a more flexible UI layout, but the performance of SurfaceView is better than that of SurfaceTexture.
	 *
	 * \param useSurfaceTexture Set to \c FALSE if you want to display video frames using SurfaceView; otherwise \c TRUE. SurfaceView performs much better than SurfaceTexture.
	 * \param useRenderThread Set to \c FALSE if you want to display video frames on the UI thread ; otherwise \c TRUE. if useRenderThread is \c TRUE, video frames will display on a separate thread.
	 * This parameter can only be applied when using SurfaceTexture.
	 * This API works properly on devices running the ICS OS and above.
	 *
	 * \note Call this API right before \c init.
	 * 
	 * \since version 6.43
	 */
	public void setUseSurfaceTexture(boolean useSurfaceTexture, boolean useRenderThread) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			mUseSurfaceTexture = false;
			mUseRenderThread = false;
			NexLog.w(LOG_TAG, "SurfaceTexture is not supported on this device");
			return;
		}

		mUseSurfaceTexture = useSurfaceTexture;
		mUseRenderThread = useRenderThread;
	}
}

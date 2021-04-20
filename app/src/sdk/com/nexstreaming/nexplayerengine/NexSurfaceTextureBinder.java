package com.nexstreaming.nexplayerengine;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.view.Surface;
import android.view.View;

import com.nexstreaming.nexplayerengine.NexVideoViewFactory.NexVideoRendererStatus;

@TargetApi(14)
public class NexSurfaceTextureBinder implements NexVideoViewFactory.INexVideoView {
    protected final String LOG_TAG = "NexSurfaceTextureBinder";
    private SurfaceTexture mSurfaceTexture = null;
    private Surface mSurface = null;
    private View mBaseView = null;
    private NexPlayer mNexPlayer;

    private Handler mHandler = new Handler();
    private Point videoSize = new Point(0, 0);
    private Rect displayedRect = new Rect(0, 0, 0, 0);

    private int videoRenderMode = 0;
    private boolean activityPaused = false;
    private boolean firstVideoRenderCreate = false;
    private boolean needSetDisplay = false;

    private NexVideoRendererStatus videoRendererStatus = NexVideoRendererStatus.VIDEO_RENDERER_NONE;
    private NexVideoRenderer.IListener videoSizeListener = null;
    private NexPlayer.IVideoRendererListener iVideoRendererListener = null;
    private NexPlayer.IVideoRendererListener postNexPlayerVideoRendererListener = null;
    private NexPlayer.IVideoRendererListener preNexPlayerVideoRendererListener = null;

    private boolean initBinder() {
        iVideoRendererListener = new NexSurfaceTextureBinder.IVideoRendererListener();
        mNexPlayer.setVideoRendererListener(iVideoRendererListener);
        NexALFactory alfactory = mNexPlayer.getNexALFactory();
        if(null != alfactory)
        {
            alfactory.setExternalSurfaceMode(NexALFactory.NEX_EXTERNAL_VIEW_SURFACETEXTURE);
        }
        return true;
    }

    private void setVideoSize(int width, int height)
    {
        videoSize.set(width, height);
        if (null != videoSizeListener)
        {
            videoSizeListener.onVideoSizeChanged();
        }
    }

    private void setDisplayedRect(Rect r)
    {
        if (null != r)
        {
            displayedRect = r;
            if (null != videoSizeListener)
            {
                videoSizeListener.onDisplayedRectChanged();
            }
        }
    }

    public NexSurfaceTextureBinder() {

    }

    public void setCodecName(String name) {

    }

    public void setSecureSurfaceFlag(Boolean secure) {

    }

    public void setSupportMultiView(Boolean enable) {

    }

    /**
     * Sets the SurfaceTexture to be used as the sink for the video portion of the media.
     * @param surfaceTexture
     */
    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        Surface surface = null;
        mSurfaceTexture = surfaceTexture;
        if (surfaceTexture != null)
            surface = new Surface(mSurfaceTexture);
        setSurface(surface);
    }

    /**
     * Sets the Surface to be used as the sink for the video portion of the media.
     * @param surface
     */
    public void setSurface(Surface surface) {
        mSurface = surface;
        if (needSetDisplay || mSurface == null) {
            NexSurfaceTextureBinder.this.mNexPlayer.setDisplay(mSurface);
            needSetDisplay = false;
        }
    }

    /*public void setSurface(Surface surface) {
        mSurface = surface;

        if(needSetDisplay || surface == null) {
            NexLog.d(LOG_TAG, "Native window Mode start");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                   NexSurfaceTextureBinder.this.mNexPlayer.setDisplay(mSurface);
                }
            });
            needSetDisplay = false;
        }
    }*/

    /**
     * Sets the View that owns the SurfaceTexture.
     * @param view
     */
    public void setBaseView(View view)
    {
        mBaseView = view;
    }

    /**
     * \brief This method initializes the NexSurfaceTextureBinder instance.
     *
     * @param nexPlayer	An instance of \link NexPlayer\endlink after its \link NexPlayer.init init method\endlink
     * 					has been called.
     *
     * \since version 6.1
     */
    @Override
    public void init(NexPlayer nexPlayer) {
        mNexPlayer = nexPlayer;
        initBinder();
    }

    /**
     * \brief This method checks NexSurfaceTextureBinder's current initialization status.
     *
     * The application should check this method before calling any method other
     * than \link NexSurfaceTextureBinder.init init\endlink or one of the get methods.
     *
     * \returns TRUE if NexSurfaceTextureBinder is initialized, and otherwise FALSE.
     *
     * \since version 6.1
     */
    @Override
    public boolean isInitialized() {
        NexLog.d(LOG_TAG, "isInitialized - videoRendererStatus:" + NexSurfaceTextureBinder.this.videoRendererStatus);
        return NexSurfaceTextureBinder.this.videoRendererStatus == NexVideoRendererStatus.VIDEO_RENDERER_INITED;
    }

    /**
     * \brief This method gets the current media's video size.
     *
     * @param outSize [out] 	A valid instance of \link android.graphics.Point\endlink whose
     *                values will be set by this method.
     *                <p/>
     *                \since version 6.1
     */
    @Override
    public void getVideoSize(Point outSize)
    {
        outSize.set(videoSize.x, videoSize.y);
    }

    /**
     * \brief The \link android.graphics.Rect\endlink that will be displayed by
     * this layout.
     * <p/>
     * This is the position and size of the rectangle within the layout that will
     * display the media content.
     * <p/>
     * This can be changed with a call to \link NexSurfaceTextureBinder.setOutputPos
     * setOutputPos\endlink.
     * <p/>
     * \since version 6.1
     */
    @Override
    public Rect getDisplayedRect()
    {
        return displayedRect;
    }

    /**
     * \brief  This method sets the displayed rectangle's position and size.
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
     * 				within the View layout to start rendering the media content.
     * \param top	The vertical position in pixels of the top left-hand corner of the desired rectangle
     * 				within the View layout to start rendering the media content.
     * \param width		The width in pixels of the desired rectangle within the View
     *					layout to render the media content.
     * \param height	The height in pixels of the desired rectangle within the View
     *					layout to render the media content.
     *
     * \since version 6.1
     */
    @Override
    public void setOutputPos(int left, int top, int width, int height) {
        NexLog.d(LOG_TAG, "setOutputPos mode:0x" + Integer.toHexString(videoRenderMode) + " left:" + left + " top:" + top
                + " width:" + width + " height:" + height + " getW:" + getWidth() + " getH:" + getHeight());

        if (width == 0 || height == 0)
        {
            NexLog.d(LOG_TAG, "setOutputPos : width or height is zero. width and height should be bigger than zero");
            return;
        }

        Rect outputPos = new Rect(left, top, left + width, top + height);
        setDisplayedRect(outputPos);

        if(mBaseView instanceof NexSurfaceTextureView) {
            ((NexSurfaceTextureView)mBaseView).setOutputPos(left, top, width, height);
        }
    }

    /**
     * @deprecated
     * \brief  This method requests that the NexSurfaceTextureBinder view display a blank canvas (black).
     *
     * \since version 6.1
     */
    @Override
    public void clearCanvas() {

    }

    /**
     * @deprecated This method do not work anything.
     */
    @Override
    public void resetSurface() {

    }

    /**
     * @deprecated This method do not work anything.
     */
    @Override
    public void setScreenPixelFormat(int screenPixelFormatToSet) {

    }

    /**
     * \brief This method sets the \link NexVideoRenderer.IListener IListener\endlink.
     *
     * @param listener An \link NexVideoRenderer.IListener IListener\endlink instance
     *                 requesting the events that this NexSurfaceTextureBinder generates.
     *                 <p/>
     *                 \since version 6.1
     */
    @Override
    public void setListener(NexVideoRenderer.IListener listener) {
        this.videoSizeListener = listener;
    }

    /**
     * \brief This method sets a listener for handling the finer details of the video renderer.
     *
     * Setting this listener is absolutely optional and intended for the experts who want finer control of the rendering process.
     *
     * @param preNexPlayerVideoRendererListener
     *            An instance of \link NexPlayer.IVideoRendererListener\endlink that requests the callbacks from NexPlayer&trade;&nbsp;to handle them
     *            before NexSurfaceTextureBinder has started performing its operations.
     *
     * \since version 6.1
     */
    @Override
    public void setPreNexPlayerVideoRendererListener(NexPlayer.IVideoRendererListener preNexPlayerVideoRendererListener) {
        this.preNexPlayerVideoRendererListener = preNexPlayerVideoRendererListener;
    }

    /**
     * \brief This method sets a listener for handling the finer details of the video renderer.
     *
     * Setting this listener is absolutely optional and intended for the experts who want finer control of the rendering process.
     *
     * @param postNexPlayerVideoRendererListener
     *            An instance of the \link NexPlayer.IVideoRendererListener\endlink that requests the callbacks from NexPlayer to handle them
     *            after NexSurfaceTextureBinder has finished performing its operations.
     *
     * \since version 6.1
     */
    @Override
    public void setPostNexPlayerVideoRendererListener(NexPlayer.IVideoRendererListener postNexPlayerVideoRendererListener) {
        this.postNexPlayerVideoRendererListener = postNexPlayerVideoRendererListener;
    }

    /**
     * @deprecated This method do not work anything.
     */
    @Override
    public void setSurfaceSecure(Boolean usesecure) {

    }

    @Override
    public void setZOrderMediaOverlay(boolean isMediaOverlay) {

    }

    /**
     * @deprecated This method do not work anything.
     */
    @Override
    public void setVisibility(int visibility) {

    }

    /**
     * @deprecated This method always returns View.VISIBLE.
     *
     */
    @Override
    public int getVisibility() {
        return View.VISIBLE;
    }

    /**
     * Return the width of the your view.
     *
     * @return The width of your view, in pixels.
     */
    @Override
    public int getWidth() {
        if(mBaseView != null)
            return mBaseView.getWidth();
        else
            return 0;
    }

    /**
     * Return the height of your view.
     *
     * @return The height of your view, in pixels.
     */
    @Override
    public int getHeight() {
        if(mBaseView != null)
            return mBaseView.getHeight();
        else
            return 0;
    }

    public void keepScreenOn(boolean enable)
    {
        //not support here
    }

    /**
     * Retruen your view that set by setBaseView
     */
    @Override
    public View getView() {
        return mBaseView;
    }

    /**
     * \brief  This method releases resources that are used by the instance of \c NexSurfaceTextureBinder.
     *
     * This should be called before the \link NexPlayer.release \endlink method is called
     * when the instance is no longer needed.
     *
     * \since version 6.23
     */
    @Override
    public void release() {
        videoRendererStatus = NexVideoRendererStatus.VIDEO_RENDERER_NONE;
        if(mBaseView instanceof NexSurfaceTextureView) {
            ((NexSurfaceTextureView)mBaseView).release();
        }
        mNexPlayer = null;
        mSurface.release();
        setListener(null);
    }

    /**
     * \brief This method informs the view that the activity is paused.
     *
     * The owner of this view must call this method when the activity is paused.
     * Calling this method will pause the rendering thread.
     *
     * \since version 6.26
     */
    @Override
    public void onPause() {
        NexLog.d(LOG_TAG, "onPause called");
        activityPaused = true;
    }

    /**
     * \brief This method informs the view that the activity has resumed.
     *
     * The owner of this view must call this method when the activity is being resumed.
     *
     * \since version 6.26
     */
    @Override
    public void onResume() {
        NexLog.d(LOG_TAG, "onResume called");
        activityPaused = false;
    }

    private class IVideoRendererListener implements NexPlayer.IVideoRendererListener {
        @Override
        public void onVideoRenderCapture(NexPlayer mp, int width, int height, int pixelbyte, Object rgbBuffer) {
            NexLog.d(LOG_TAG, "onVideoRenderCapture called");
        }

        @Override
        public void onVideoRenderCreate(NexPlayer mp, int width, int height, Object rgbBuffer) {
            if (null != preNexPlayerVideoRendererListener) {
                preNexPlayerVideoRendererListener.onVideoRenderCreate(mp, width, height, rgbBuffer);
            }

            int[] arrSize = new int[2];
            mp.getSARInfo(arrSize);

            NexLog.d(LOG_TAG, "orignal onVideoRenderCreate W : " + width + " H : " + height);

            float nRatioOffset = (float) arrSize[1] / (float) arrSize[0];

            if (0 < nRatioOffset)
                height *= nRatioOffset;

            NexLog.d(LOG_TAG, "onVideoRenderCreate W : " + width + " H : " + height + " offset " + nRatioOffset);

            if(mBaseView instanceof NexSurfaceTextureView) {
                ((NexSurfaceTextureView)mBaseView).mVideoRenderListener.onVideoRenderCreate(mp, width, height, rgbBuffer);
            }

            if (NexSurfaceTextureBinder.this.activityPaused) {
                if (null != postNexPlayerVideoRendererListener) {
                    postNexPlayerVideoRendererListener.onVideoRenderCreate(mp, width, height, rgbBuffer);
                }
                return;
            }

            NexSurfaceTextureBinder.this.videoRendererStatus = NexVideoRendererStatus.VIDEO_RENDERER_INITED;
            NexSurfaceTextureBinder.this.setVideoSize(width, height);
            if (!NexSurfaceTextureBinder.this.firstVideoRenderCreate) {
                NexSurfaceTextureBinder.this.firstVideoRenderCreate = true;
                if (null != NexSurfaceTextureBinder.this.videoSizeListener) {
                    NexSurfaceTextureBinder.this.videoSizeListener.onFirstVideoRenderCreate();
                }
            }

            if (null != postNexPlayerVideoRendererListener) {
                postNexPlayerVideoRendererListener.onVideoRenderCreate(mp, width, height, rgbBuffer);
            }
        }

        @Override
        public void onVideoRenderDelete(NexPlayer mp) {
            NexLog.d(LOG_TAG, "onVideoRenderDelete:");
            if (null != preNexPlayerVideoRendererListener) {
                preNexPlayerVideoRendererListener.onVideoRenderDelete(mp);
            }
            NexSurfaceTextureBinder.this.videoRendererStatus = NexVideoRendererStatus.VIDEO_RENDERER_DEINITED;

            if(mBaseView instanceof NexSurfaceTextureView) {
                ((NexSurfaceTextureView)mBaseView).mVideoRenderListener.onVideoRenderDelete(mp);
            }

            if (null != postNexPlayerVideoRendererListener) {
                postNexPlayerVideoRendererListener.onVideoRenderDelete(mp);
            }
        }

        @Override
        public void onVideoRenderPrepared(NexPlayer mp) {
            NexSurfaceTextureBinder.this.videoRendererStatus = NexVideoRendererStatus.VIDEO_RENDERER_PREPARED;
            NexSurfaceTextureBinder.this.firstVideoRenderCreate = false;
            NexSurfaceTextureBinder.this.videoRenderMode = NexSurfaceTextureBinder.this.mNexPlayer.GetRenderMode();
            NexLog.d(LOG_TAG, "onVideoRenderPrepared! mode:0x" + Integer.toHexString(NexSurfaceTextureBinder.this.videoRenderMode));

            if (NexSurfaceTextureBinder.this.videoRenderMode == NexPlayer.NEX_USE_RENDER_OPENGL) {
                NexLog.d(LOG_TAG, "UseOpenGL! OpenGL Mode start");
                mHandler.post(new Runnable() {
                    public void run() {

                    }
                });
            } else {
                NexLog.d(LOG_TAG, "Native window Mode start");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mSurface != null && NexSurfaceTextureBinder.this.mNexPlayer != null)
                            NexSurfaceTextureBinder.this.mNexPlayer.setDisplay(mSurface);
                        else
                            needSetDisplay = true;
                    }
                });
            }
            if(mBaseView instanceof NexSurfaceTextureView) {
                ((NexSurfaceTextureView)mBaseView).mVideoRenderListener.onVideoRenderPrepared(mp);
            }
        }

        @Override
        public void onVideoRenderRender(NexPlayer mp) {
            NexLog.d(LOG_TAG, "onVideoRenderRender called");
        }
    }
}

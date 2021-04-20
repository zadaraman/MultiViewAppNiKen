package com.nexstreaming.nexplayerengine;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.nexstreaming.nexplayerengine.gles.NexEglSurface;
import com.nexstreaming.nexplayerengine.gles.NexGLUtil;
import com.nexstreaming.nexplayerengine.gles.NexVideo2dProgram;

import java.lang.ref.WeakReference;


/**
 * This class is a private class for inner use.
 */
@TargetApi(14)
public class
NexSurfaceTextureView extends SurfaceView implements SurfaceHolder.Callback, SurfaceTexture.OnFrameAvailableListener {

    private final static String LOG_TAG = "NexSurfaceTextureView";

    private final static int SUPPORT_MUTLI_VIEW = 10;
    private NexSurfaceTextureBinder mSurfaceTextureBinder;
    private WeakReference<NexPlayer> mWeakPlayer;
    private SurfaceTexture mSurfaceTexture;
    private Surface mCreatedSurface;

    private boolean mUseRenderThread = false;
    private HandlerThread mRenderThread;
    private Handler mHandler = null;

    private boolean mUseSurfaceTexture = true;
    private NexEglSurface.EglManager mEglManager;
    private NexEglSurface mDisplaySurface;
    private int mTextureId;
    private NexVideo2dProgram mVideoRenderer;
    private float[] mTransMatrix = new float[16];

    private final Object mLock = new Object();
    private boolean mFrameAvailable = false;
    private boolean mDisplaySurfaceAvailable = false;
    private boolean mNativeInitialized = false;
    private boolean mFirstFrameUpdated = false;
    private Surface dummySurface;
    private String mVideoCodecName;
    private boolean mSecureSurface;
    private Context mContext;
    private boolean mSupportMutliView;

    protected NexPlayer.IVideoRendererListener mVideoRenderListener;
    public NexSurfaceTextureView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public NexSurfaceTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public NexSurfaceTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    public void setNexPlayerSurfaceBinder(NexSurfaceTextureBinder binder) {
        mSurfaceTextureBinder = binder;
        getHolder().addCallback(this);
    }

    public void setNexPlayer(NexPlayer nexPlayer) {
        mWeakPlayer = new WeakReference<NexPlayer>(nexPlayer);
    }

    public void enableMutliView(boolean enable) {
        mSupportMutliView = enable;
    }

    public void setSurfaceTextureMode(boolean useSurfaceTexture, boolean useRenderThread) {
        mUseSurfaceTexture = useSurfaceTexture;
        mUseRenderThread = useRenderThread;
        if(useRenderThread) {
            initRenderThread();
        }
    }

    public Surface getSurfaceFromSurfaceTexture() {
        return mCreatedSurface;
    }

    @SuppressLint("NewApi")
    public void release() {
        NexLog.d(LOG_TAG, "release");
        if(dummySurface != null) {
            ((DummySurface)dummySurface).release();
        }
        dummySurface = null;
        mHandler.sendEmptyMessage(RenderHandler.MSG_GLES_RELEASE);
    }

    public void setVideoInfo(String codecName, boolean secure) {
        mVideoCodecName = codecName;
        mSecureSurface =   secure;
    }

    private void init() {
        mSecureSurface = false;
        dummySurface = null;
        mSupportMutliView = false;
        Matrix.setIdentityM(mTransMatrix, 0);
        mHandler = new RenderHandler(this);;
        mVideoRenderListener = new NexPlayer.IVideoRendererListener() {
            @Override
            public void onVideoRenderPrepared(NexPlayer mp) {
                mFirstFrameUpdated = false;
            }

            @Override
            public void onVideoRenderCreate(NexPlayer mp, int width, int height, Object rgbBuffer) {
                synchronized (mLock) {
                    mNativeInitialized = true;
                }
            }

            @Override
            public void onVideoRenderDelete(NexPlayer mp) {
                synchronized (mLock) {
                    mNativeInitialized = false;
                }
            }

            @Override
            public void onVideoRenderRender(NexPlayer mp) {

            }

            @Override
            public void onVideoRenderCapture(NexPlayer mp, int width, int height, int pixelbyte, Object bitmap) {

            }
        };
    }

    private void initRenderThread() {
        mRenderThread = new HandlerThread("NexSurfaceTexture");
        mRenderThread.start();
        mHandler = null;
        mHandler = new RenderHandler(mRenderThread.getLooper(), this);
    }
    // *********** SurfaceHolder ****************
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        NexLog.d(LOG_TAG, "surfaceCreated holder=" + holder);

        if(mUseSurfaceTexture) {
            if(mUseRenderThread && mRenderThread == null) {
                initRenderThread();
            }
            mHandler.sendEmptyMessage(RenderHandler.MSG_GLES_INITGLSURFACE);
            //initGLSurface();
        } else {
            if(mSurfaceTextureBinder != null)
                mSurfaceTextureBinder.setSurface(holder.getSurface());
            else if(mWeakPlayer != null) {
                ((NexPlayer)mWeakPlayer.get()).setDisplay(holder);
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        NexLog.d(LOG_TAG, "surfaceChanged holder=" + holder + "size(" + width + "," + height + ")");
        if(mUseSurfaceTexture) {
            // I'm not sure
            //mHandler.sendEmptyMessage(RenderHandler.MSG_GLES_RESIZE);
            {
                Message msg = Message.obtain();
                msg.what = RenderHandler.MSG_FRAME_AVAILABLE;
                msg.arg1 = -1;
                mHandler.sendMessage(msg);
            }
            //mHandler.sendEmptyMessage(RenderHandler.MSG_FRAME_AVAILABLE);
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        NexLog.d(LOG_TAG, "surfaceDestroyed holder=" + holder);
        if (mUseSurfaceTexture) {
            synchronized (mLock) {
                mDisplaySurfaceAvailable = false;
            }
            mHandler.sendEmptyMessage(RenderHandler.MSG_GLES_DEINITGLSURFACE);
            //deinitGLSurface();
        } else {
            if (mSurfaceTextureBinder != null)
                mSurfaceTextureBinder.setSurface(null);
            else if (mWeakPlayer != null) {
                int surface_number = 0;
                mSecureSurface = ((NexPlayer) mWeakPlayer.get()).getDRMEnable();
                if (mSupportMutliView) {
                    if (dummySurface == null && (!mSecureSurface || DummySurface.isSecureSupported(mContext))) {
                        NexLog.d(LOG_TAG, "secure option: " + mSecureSurface);
                        dummySurface = DummySurface.newInstanceV17(mSecureSurface);
                    } else if (((DummySurface) dummySurface).secure != mSecureSurface) {
                        NexLog.d(LOG_TAG, "secure option was changed to " + mSecureSurface);
                        ((DummySurface) dummySurface).release();
                        dummySurface = null;
                        if (!mSecureSurface || DummySurface.isSecureSupported(mContext))
                            dummySurface = DummySurface.newInstanceV17(mSecureSurface);
                    }
                    surface_number = SUPPORT_MUTLI_VIEW;
                }

                ((NexPlayer) mWeakPlayer.get()).setDisplay(null, dummySurface, surface_number);
            }
        }
    }
    // *********** SurfaceTexture ****************
    private long mFrameUpdatedNs = 0;
    private long mPreviousDrawNs = 0;

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        long updatedTimeNs = System.nanoTime();
        //NexLog.d(LOG_TAG, "onFrameAvailable interval:" + (updatedTimeNs - mFrameUpdatedNs) / 1000000 + "ms");
        synchronized (mLock) {
            mFrameAvailable = true;
            mFrameUpdatedNs = updatedTimeNs;
            if(mNativeInitialized)
                mFirstFrameUpdated = true;
        }
        if(!mUseVSync) {
            if (mUseSurfaceTexture && mHandler != null) {
                Message msg = Message.obtain();
                msg.what = RenderHandler.MSG_FRAME_AVAILABLE;
                msg.arg1 = 0;
                mHandler.sendMessage(msg);
            }
        }
    }

    private static class RenderHandler extends Handler {
        public static final int MSG_GLES_INITGLSURFACE = 1;
        public static final int MSG_GLES_DEINITGLSURFACE = 2;
        public static final int MSG_GLES_RELEASE = 3;
        public static final int MSG_FRAME_AVAILABLE = 4;
        public static final int MSG_GLES_RESIZE = 5;

        private WeakReference<NexSurfaceTextureView> mWeakView;

        public RenderHandler(NexSurfaceTextureView surfaceView) {
            mWeakView = new WeakReference<NexSurfaceTextureView>(surfaceView);
        }
        public RenderHandler(Looper looper, NexSurfaceTextureView surfaceView) {
            super(looper);
            mWeakView = new WeakReference<NexSurfaceTextureView>(surfaceView);

        }

        @Override
        public void handleMessage(Message msg) {
            NexSurfaceTextureView view = mWeakView.get();
			if(view == null) return;
            switch (msg.what) {
                case MSG_FRAME_AVAILABLE: {
                    view.drawFrame(msg.arg1, 0);
                    break;
                }
                case MSG_GLES_INITGLSURFACE: {
                    view.initGLSurface();
                    break;
                }
                case MSG_GLES_DEINITGLSURFACE: {
                    view.deinitGLSurface();
                    break;
                }
                case MSG_GLES_RELEASE: {
                    view.releaseGL();
                    break;
                }
                case MSG_GLES_RESIZE: {
                    view.resizeWindow();
                    break;
                }
            }
        }
    }

    // ***********  ****************

    private boolean initGLSurface() {
        NexLog.d(LOG_TAG, "initGLSurface");
        int width = this.getWidth();
        int height = this.getHeight();

        if(mEglManager == null)
            mEglManager = new NexEglSurface.EglManager();

        mDisplaySurface = new NexEglSurface(mEglManager, getHolder(), false);
        mDisplaySurface.makeCurrent();
        synchronized (mLock) {
            mDisplaySurfaceAvailable = true;
        }

        if(mUseVSync)
            initVSync();

        if(mSurfaceTexture == null) {
            mVideoRenderer = new NexVideo2dProgram();
            mTextureId = mVideoRenderer.createTextureObject();
            mSurfaceTexture = new SurfaceTexture(mTextureId);
            mSurfaceTexture.setOnFrameAvailableListener(this);
            NexLog.d(LOG_TAG, "SurfaceTexture created:" + mSurfaceTexture);

            if(mSurfaceTextureBinder != null)
                mSurfaceTextureBinder.setSurfaceTexture(mSurfaceTexture);
            else if(mWeakPlayer != null) {
                mCreatedSurface = new Surface(mSurfaceTexture);
                (mWeakPlayer.get()).setDisplay(mCreatedSurface);
            }
            mSurfaceTexture.getTransformMatrix(mTexMatrix);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                mSurfaceTexture.setDefaultBufferSize(width, height);
            }
        } else {
            synchronized (mLock) {
                if(mFrameAvailable) {
                    Message msg = Message.obtain();
                    msg.what = RenderHandler.MSG_FRAME_AVAILABLE;
                    msg.arg1 = -2;
                    mHandler.sendMessage(msg);
                }
            }
        }

        return true;
    }

    private boolean deinitGLSurface() {
        NexLog.d(LOG_TAG, "deinitGLSurface");

        mEglManager.makeNothingCurrent();
        mDisplaySurface.release();
        mDisplaySurface = null;

        return true;
    }

    private void resizeWindow() {
        NexLog.d(LOG_TAG, "resizeWindow");
        deinitGLSurface();
        initGLSurface();
    }

    private void releaseGL() {
        NexLog.d(LOG_TAG, "releaseGL");
        if (mVideoRenderer != null) {
            mVideoRenderer.release();
            mVideoRenderer = null;
        }
        if(mCreatedSurface != null) {
            mCreatedSurface.release();
            mCreatedSurface = null;
        }

        if(mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        if (mEglManager != null) {
            mEglManager.release();
            mEglManager = null;
        }

        if(mRenderThread != null) {
            mRenderThread.quit();
            mRenderThread = null;
            /*
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mRenderThread.quit();
                    mRenderThread = null;
                }
            });
            */
        }
        if(mVSyncCallback != null) {
            Choreographer.getInstance().removeFrameCallback((Choreographer.FrameCallback) mVSyncCallback);
            mVSyncCallback = null;
        }
    }

    private int mFrameNum;
    float[] mTexMatrix = new float[16];
    private void drawFrame(int drawType, long frameTimeNanos) {
        NexLog.v(LOG_TAG, "drawFrame drawType:" +  drawType + "," + mFrameNum + " interval:" + (frameTimeNanos - mPreviousDrawNs) / 1000000 + "ms");
        NexLog.v(LOG_TAG, "drawFrame gap:" +  (frameTimeNanos - mFrameUpdatedNs) / 1000000 + "ms");
        mPreviousDrawNs = frameTimeNanos;
        synchronized (mLock) {
            mFrameAvailable = false;
            if (mEglManager == null || mDisplaySurface == null || !mDisplaySurfaceAvailable) {
                NexLog.d(LOG_TAG, "Skipping drawFrame after shutdown");
                return;
            }
        }

        mDisplaySurface.makeCurrent();
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mTexMatrix);

        // Fill the SurfaceView with it.
        int viewWidth = this.getWidth();
        int viewHeight = this.getHeight();
        //NexLog.v(LOG_TAG, "drawFrame viewWidth:" + viewWidth + "viewHeight:" + viewHeight);
        GLES20.glViewport(0, 0, viewWidth, viewHeight);

        if(mFirstFrameUpdated)
            mVideoRenderer.drawFrame(mTextureId, mTransMatrix, mTexMatrix);
        else
            mVideoRenderer.drawFrame(mTextureId, NexGLUtil.IDENTITY_MATRIX, mTexMatrix); //for logo
        mDisplaySurface.swapBuffers();

        mFrameNum++;
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
     * \since version 6.X
     */
    public void setOutputPos(int left, int top, int width, int height) {
        NexLog.d(LOG_TAG, "setOutputPos left:" + left + " top:" + top + " width:" + width + " height:" + height + " getW:"
                + getWidth() + " getH:" + getHeight());
        if(getWidth() == 0 || getHeight() == 0) {
            NexLog.w(LOG_TAG, "surface of NexSurfaceTextureView was not created");
            return ;
        }

        if(width == 0 || height == 0) {
            NexLog.w(LOG_TAG, "setOutputPos : width or height is zero. width and height should be bigger than zero");
            return;
        }

        int windowWidth = getWidth();
        int windowHeight = getHeight();
        if(getParent() instanceof NexVideoRenderer) {
            //because size of parent view is changed before size of this view is changed.
            windowWidth = ((NexVideoRenderer) getParent()).getWidth();
            windowHeight = ((NexVideoRenderer) getParent()).getHeight();
            NexLog.d(LOG_TAG, "this width:" + getWidth() + " height:" + getHeight() + " parent width:" + windowWidth + " parent height:" + windowHeight);
        }

        float translateX = (float)2*left / (float)windowWidth;
        float translateY = (float)2*top / (float)windowHeight;
        float scaleX = (float)width / (float)windowWidth;
        float scaleY = (float)height / (float)windowHeight;

        mTransMatrix[0] = scaleX;
        mTransMatrix[5] = scaleY;
        mTransMatrix[10] = 1;
        mTransMatrix[12] = scaleX + translateX - 1;
        mTransMatrix[13] = 1 - scaleY - translateY;
        mTransMatrix[15] = 1;

        if(mUseSurfaceTexture) {
            NexLog.w(LOG_TAG, "redraw frame to adjust output position NativeInitialized:" + mNativeInitialized);
            if(mNativeInitialized) {
                {
                    Message msg = Message.obtain();
                    msg.what = RenderHandler.MSG_FRAME_AVAILABLE;
                    msg.arg1 = -3;
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

    private Object mVSyncCallback;
    private boolean mUseVSync = true;
    @TargetApi(16)
    private void initVSync() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mVSyncCallback = new Choreographer.FrameCallback() {
                @Override
                public void doFrame(long frameTimeNanos) {

                    if(mUseSurfaceTexture && mHandler != null) {
                        //NexLog.v(LOG_TAG, "VSync doFrame gap:" + (frameTimeNanos - mFrameUpdatedNs) / 1000000 + "ms");

                        if(mFrameAvailable) {
                            boolean useHandler = false;
                            if(useHandler) {
                                Message msg = Message.obtain();
                                msg.what = RenderHandler.MSG_FRAME_AVAILABLE;
                                msg.arg1 = 0;
                                mHandler.sendMessage(msg);
                            } else {
                                drawFrame(0, frameTimeNanos);
                            }
                        }
                        Choreographer.getInstance().postFrameCallback((Choreographer.FrameCallback) mVSyncCallback);
                    }
                }
            };
            Choreographer.getInstance().postFrameCallback((Choreographer.FrameCallback) mVSyncCallback);
        } else {
            mUseVSync = false;
        }
    }

}

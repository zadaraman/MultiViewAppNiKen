package com.nexstreaming.nexplayerengine.gles;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.nexstreaming.nexplayerengine.NexLog;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;



/**
 * This class is a private class for inner use.
 */
@TargetApi(8)
public class NexEglSurface {
    protected static final String LOG_TAG = NexGLUtil.LOG_TAG;

    /**
     * Core EGL state (display, context, config).
     * <p/>
     * The EGLContext must only be attached to one thread at a time.  This class is not thread-safe.
     */
    @TargetApi(8)
    public final static class EglManager {
        private static Object EGL_DEFAULT_DISPLAY = EGL10.EGL_DEFAULT_DISPLAY;
        private static EGLContext EGL_NO_CONTEXT = EGL10.EGL_NO_CONTEXT;
        private static EGLDisplay EGL_NO_DISPLAY = EGL10.EGL_NO_DISPLAY;
        private static EGLSurface EGL_NO_SURFACE = EGL10.EGL_NO_SURFACE;

        /**
         * Constructor flag: ask for GLES3, fall back to GLES2 if not available.  Without this
         * flag, GLES2 is used.
         */
        public static final int FLAG_TRY_GLES3 = 0x02;

        private EGL10 mEgl;
        private EGLDisplay mEGLDisplay = EGL_NO_DISPLAY;
        private EGLContext mEGLContext = EGL_NO_CONTEXT;
        private EGLConfig mEGLConfig = null;
        private int mGlVersion = -1;


        /**
         * Prepares EGL display and context.
         * <p/>
         * Equivalent to EglManager(null, 0).
         */
        public EglManager() {
            this(null, 0);
        }

        /**
         * Prepares EGL display and context.
         * <p/>
         *
         * @param sharedContext The context to share, or null if sharing is not desired.
         * @param flags         Configuration bit flags, e.g. FLAG_RECORDABLE.
         */
        public EglManager(EGLContext sharedContext, int flags) {
            if (mEGLDisplay != EGL_NO_DISPLAY) {
                throw new RuntimeException("EGL already set up");
            }

            if (sharedContext == null) {
                sharedContext = EGL_NO_CONTEXT;
            }

            mEgl = (EGL10) EGLContext.getEGL();
            mEGLDisplay = mEgl.eglGetDisplay(EGL_DEFAULT_DISPLAY);
            if (mEGLDisplay == EGL_NO_DISPLAY) {
                throw new RuntimeException("unable to get EGL14 display");
            }

            int[] version = new int[2];
            if (!mEgl.eglInitialize(mEGLDisplay, version)) {
                mEGLDisplay = null;
                throw new RuntimeException("eglInitialize failed");
            }

            int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
            // Try to get a GLES3 context, if requested.
            if ((flags & FLAG_TRY_GLES3) != 0) {
                //NexLog.d(LOG_TAG, "Trying GLES 3");
                EGLConfig config = getConfig(3);
                if (config != null) {
                    int[] attrib3_list = {
                            EGL_CONTEXT_CLIENT_VERSION, 3,
                            EGL10.EGL_NONE
                    };
                    EGLContext context = mEgl.eglCreateContext(mEGLDisplay, config, sharedContext, attrib3_list);

                    if (mEgl.eglGetError() == EGL10.EGL_SUCCESS) {
                        //NexLog.d(LOG_TAG, "Got GLES 3 config");
                        mEGLConfig = config;
                        mEGLContext = context;
                        mGlVersion = 3;
                    }
                }
            }
            if (mEGLContext == EGL_NO_CONTEXT) {  // GLES 2 only, or GLES 3 attempt failed
                //NexLog.d(LOG_TAG, "Trying GLES 2");
                EGLConfig config = getConfig(2);
                if (config == null) {
                    throw new RuntimeException("Unable to find a suitable EGLConfig");
                }
                int[] attrib2_list = {
                        EGL_CONTEXT_CLIENT_VERSION, 2,
                        EGL10.EGL_NONE
                };
                EGLContext context = mEgl.eglCreateContext(mEGLDisplay, config, sharedContext, attrib2_list);
                checkEglError("eglCreateContext");
                mEGLConfig = config;
                mEGLContext = context;
                mGlVersion = 2;
            }

            // Confirm with query.
            int[] values = new int[1];
            mEgl.eglQueryContext(mEGLDisplay, mEGLContext, EGL_CONTEXT_CLIENT_VERSION, values);
            NexLog.d(LOG_TAG, "EGLContext created, client version " + values[0]);
        }

        /**
         * Finds a suitable EGLConfig.
         *
         * @param version Must be 2 or 3.
         */
        private EGLConfig getConfig(int version) {
            int EGL_OPENGL_ES2_BIT = 0x0004;
            int EGL_OPENGL_ES3_BIT_KHR = 0x0040;

            int renderableType = EGL_OPENGL_ES2_BIT;
            if (version >= 3) {
                renderableType |= EGL_OPENGL_ES3_BIT_KHR;
            }

            // The actual surface is generally RGBA or RGBX, so situationally omitting alpha
            // doesn't really help.  It can also lead to a huge performance hit on glReadPixels()
            // when reading into a GL_RGBA buffer.
            int[] attribList = {
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_ALPHA_SIZE, 8,
                    //EGL14.EGL_DEPTH_SIZE, 16,
                    //EGL14.EGL_STENCIL_SIZE, 8,
                    EGL10.EGL_RENDERABLE_TYPE, renderableType,
                    EGL10.EGL_NONE, 0,      // placeholder for recordable [@-3]
                    EGL10.EGL_NONE
            };

            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfigs = new int[1];
            if (!mEgl.eglChooseConfig(mEGLDisplay, attribList, configs, configs.length, numConfigs)) {
                NexLog.w(LOG_TAG, "unable to find RGB8888 / " + version + " EGLConfig");
                return null;
            }
            return configs[0];
        }

        /**
         * Discards all resources held by this class, notably the EGL context.  This must be
         * called from the thread where the context was created.
         * <p/>
         * On completion, no context will be current.
         */
        public void release() {
            if (mEGLDisplay != EGL_NO_DISPLAY) {
                // Android is unusual in that it uses a reference-counted EGLDisplay.  So for
                // every eglInitialize() we need an eglTerminate().
                mEgl.eglMakeCurrent(mEGLDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
                mEgl.eglDestroyContext(mEGLDisplay, mEGLContext);
                //mEgl.eglReleaseThread();
                mEgl.eglTerminate(mEGLDisplay);
            }

            mEGLDisplay = EGL_NO_DISPLAY;
            mEGLContext = EGL_NO_CONTEXT;
            mEGLConfig = null;
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                if (mEGLDisplay != EGL_NO_DISPLAY) {
                    // We're limited here -- finalizers don't run on the thread that holds
                    // the EGL state, so if a surface or context is still current on another
                    // thread we can't fully release it here.  Exceptions thrown from here
                    // are quietly discarded.  Complain in the log file.
                    NexLog.w(LOG_TAG, "WARNING: EglManager was not explicitly released -- state may be leaked");
                    release();
                }
            }
            finally {
                super.finalize();
            }
        }

        /**
         * Destroys the specified surface.  Note the EGLSurface won't actually be destroyed if it's
         * still current in a context.
         */
        public void releaseSurface(EGLSurface eglSurface) {
            mEgl.eglDestroySurface(mEGLDisplay, eglSurface);
        }

        /**
         * Creates an EGL surface associated with a Surface.
         * <p/>
         * If this is destined for MediaCodec, the EGLConfig should have the "recordable" attribute.
         */
        public EGLSurface createWindowSurface(Object native_window) {
            Object surface = null;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (!(native_window instanceof SurfaceView) && !(native_window instanceof SurfaceTexture)
                        && !(native_window instanceof SurfaceHolder)) {
                    throw new RuntimeException("invalid native_window: " + native_window);
                }
                surface = native_window;
            } else {
                if (!(native_window instanceof SurfaceView) && !(native_window instanceof SurfaceTexture)
                        && !(native_window instanceof SurfaceHolder) && !(native_window instanceof Surface)) {
                    throw new RuntimeException("invalid native_window: " + native_window);
                }
                surface = native_window;
            }

            // Create a window surface, and attach it to the Surface we received.
            int[] surfaceAttribs = {
                    EGL10.EGL_NONE
            };
            EGLSurface eglSurface = mEgl.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface, surfaceAttribs);
            checkEglError("eglCreateWindowSurface");
            if (eglSurface == null) {
                throw new RuntimeException("surface was null");
            }
            return eglSurface;
        }

        /**
         * Makes our EGL context current, using the supplied surface for both "draw" and "read".
         */
        public void makeCurrent(EGLSurface eglSurface) {
            if (mEGLDisplay == EGL_NO_DISPLAY) {
                // called makeCurrent() before create?
                NexLog.d(LOG_TAG, "NOTE: makeCurrent w/o display");
            }
            if (!mEgl.eglMakeCurrent(mEGLDisplay, eglSurface, eglSurface, mEGLContext)) {
                throw new RuntimeException("eglMakeCurrent failed");
            }
        }

        /**
         * Makes no context current.
         */
        public void makeNothingCurrent() {
            if (!mEgl.eglMakeCurrent(mEGLDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE,
                    EGL_NO_CONTEXT)) {
                throw new RuntimeException("eglMakeCurrent failed");
            }
        }

        /**
         * Calls eglSwapBuffers.  Use this to "publish" the current frame.
         *
         * @return false on failure
         */
        public boolean swapBuffers(EGLSurface eglSurface) {
            return mEgl.eglSwapBuffers(mEGLDisplay, eglSurface);
        }

        /**
         * Returns true if our context and the specified surface are current.
         */
        public boolean isCurrent(EGLSurface eglSurface) {
            return mEGLContext.equals(mEgl.eglGetCurrentContext()) &&
                    eglSurface.equals(mEgl.eglGetCurrentSurface(EGL10.EGL_DRAW));
        }

        /**
         * Performs a simple surface query.
         */
        public int querySurface(EGLSurface eglSurface, int what) {
            int[] value = new int[1];
            mEgl.eglQuerySurface(mEGLDisplay, eglSurface, what, value);
            return value[0];
        }

        /**
         * Queries a string value.
         */
        public String queryString(int what) {
            return mEgl.eglQueryString(mEGLDisplay, what);
        }

        /**
         * Returns the GLES version this context is configured for (currently 2 or 3).
         */
        public int getGlVersion() {
            return mGlVersion;
        }

        /**
         * Checks for EGL errors.  Throws an exception if an error has been raised.
         */
        private void checkEglError(String msg) {
            int error;
            if ((error = mEgl.eglGetError()) != EGL10.EGL_SUCCESS) {
                throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
            }
        }
    }


    // EglManager object we're associated with.  It may be associated with multiple surfaces.
    protected EglManager mEglManager;
    private EGLSurface mEGLSurface = EglManager.EGL_NO_SURFACE;
    private int mWidth = -1;
    private int mHeight = -1;
    private Surface mSurface;
    private boolean mReleaseSurface;

    public NexEglSurface(EglManager eglCore, SurfaceHolder holder, boolean releaseSurface) {
        mEglManager = eglCore;
        createWindowSurface(holder);
        mSurface = holder.getSurface();
        mReleaseSurface = releaseSurface;
    }

    /**
     * Creates a window surface.
     * <p/>
     *
     * @param surface May be a Surface or SurfaceTexture.
     */
    public void createWindowSurface(Object surface) {
        if (mEGLSurface != EglManager.EGL_NO_SURFACE) {
            throw new IllegalStateException("surface already created");
        }
        mEGLSurface = mEglManager.createWindowSurface(surface);
    }

    /**
     * Returns the surface's width, in pixels.
     * <p/>
     * If this is called on a window surface, and the underlying surface is in the process
     * of changing size, we may not see the new size right away (e.g. in the "surfaceChanged"
     * callback).  The size should match after the next buffer swap.
     */
    public int getWidth() {
        if (mWidth < 0) {
            return mEglManager.querySurface(mEGLSurface, EGL10.EGL_WIDTH);
        } else {
            return mWidth;
        }
    }

    /**
     * Returns the surface's height, in pixels.
     */
    public int getHeight() {
        if (mHeight < 0) {
            return mEglManager.querySurface(mEGLSurface, EGL10.EGL_HEIGHT);
        } else {
            return mHeight;
        }
    }

    /**
     * Release the EGL surface.
     */
    public void releaseEglSurface() {
        mEglManager.releaseSurface(mEGLSurface);
        mEGLSurface = EglManager.EGL_NO_SURFACE;
        mWidth = mHeight = -1;
    }

    /**
     * Makes our EGL context and surface current.
     */
    public void makeCurrent() {
        mEglManager.makeCurrent(mEGLSurface);
    }

    /**
     * Calls eglSwapBuffers.  Use this to "publish" the current frame.
     *
     * @return false on failure
     */
    public boolean swapBuffers() {
        boolean result = mEglManager.swapBuffers(mEGLSurface);
        if (!result) {
            NexLog.d(LOG_TAG, "WARNING: swapBuffers() failed");
        }
        return result;
    }

    /**
     * Releases any resources associated with the EGL surface (and, if configured to do so,
     * with the Surface as well).
     * <p/>
     * Does not require that the surface's EGL context be current.
     */
    public void release() {
        releaseEglSurface();
        if (mSurface != null) {
            if (mReleaseSurface) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                    mSurface.release();
            }
            mSurface = null;
        }
    }
}

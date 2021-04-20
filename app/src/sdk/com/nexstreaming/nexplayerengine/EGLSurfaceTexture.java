/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nexstreaming.nexplayerengine;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

import javax.microedition.khronos.egl.EGL10;

import static android.opengl.GLU.gluErrorString;

/** Generates a {@link SurfaceTexture} using EGL/GLES functions. */
@RequiresApi(17)
public final class EGLSurfaceTexture implements SurfaceTexture.OnFrameAvailableListener, Runnable {

    /** Listener to be called when the texture image on {@link SurfaceTexture} has been updated. */
    public interface TextureImageListener {
        /** Called when the {@link SurfaceTexture} receives a new frame from its image producer. */
        void onFrameAvailable();
    }

    /**
     * Secure mode to be used by the EGL surface and context. One of {@link #SECURE_MODE_NONE}, {@link
     * #SECURE_MODE_SURFACELESS_CONTEXT} or {@link #SECURE_MODE_PROTECTED_PBUFFER}.
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SECURE_MODE_NONE, SECURE_MODE_SURFACELESS_CONTEXT, SECURE_MODE_PROTECTED_PBUFFER})
    public @interface SecureMode {}
    /**
     * Like {@link Build#MANUFACTURER}, but in a place where it can be conveniently overridden for
     * local testing.
     */
    public static final String MANUFACTURER = Build.MANUFACTURER;

    private static final String EXTENSION_PROTECTED_CONTENT = "EGL_EXT_protected_content";
    private static final String EXTENSION_SURFACELESS_CONTEXT = "EGL_KHR_surfaceless_context";
    /**
     * Like {@link Build#MODEL}, but in a place where it can be conveniently overridden for local
     * testing.
     */
    public static final String MODEL = Build.MODEL;
    /** No secure EGL surface and context required. */
    public static final int SECURE_MODE_NONE = 0;
    /** Creating a surfaceless, secured EGL context. */
    public static final int SECURE_MODE_SURFACELESS_CONTEXT = 1;
    /** Creating a secure surface backed by a pixel buffer. */
    public static final int SECURE_MODE_PROTECTED_PBUFFER = 2;

    private static final int EGL_SURFACE_WIDTH = 1;
    private static final int EGL_SURFACE_HEIGHT = 1;
    public static final int SDK_INT = "R".equals(Build.VERSION.CODENAME) ? 30 : Build.VERSION.SDK_INT;
    private static final int[] EGL_CONFIG_ATTRIBUTES =
            new int[] {
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_DEPTH_SIZE, 0,
                    EGL14.EGL_CONFIG_CAVEAT, EGL14.EGL_NONE,
                    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
                    EGL14.EGL_NONE
            };

    private static final int EGL_PROTECTED_CONTENT_EXT = 0x32C0;

    /** A runtime exception to be thrown if some EGL operations failed. */
    public static final class GlException extends RuntimeException {
        private GlException(String msg) {
            super(msg);
        }
    }

    private final Handler handler;
    private final int[] textureIdHolder;
    @Nullable
    private final TextureImageListener callback;

    @Nullable private EGLDisplay display;
    @Nullable private EGLContext context;
    @Nullable private EGLSurface surface;
    @Nullable private SurfaceTexture texture;

    /**
     * @param handler The {@link Handler} that will be used to call {@link
     *     SurfaceTexture#updateTexImage()} to update images on the {@link SurfaceTexture}. Note that
     *     {@link #init(int)} has to be called on the same looper thread as the {@link Handler}'s
     *     looper.
     */
    public EGLSurfaceTexture(Handler handler) {
        this(handler, /* callback= */ null);
    }

    /**
     * @param handler The {@link Handler} that will be used to call {@link
     *     SurfaceTexture#updateTexImage()} to update images on the {@link SurfaceTexture}. Note that
     *     {@link #init(int)} has to be called on the same looper thread as the looper of the {@link
     *     Handler}.
     * @param callback The {@link TextureImageListener} to be called when the texture image on {@link
     *     SurfaceTexture} has been updated. This callback will be called on the same handler thread
     *     as the {@code handler}.
     */
    public EGLSurfaceTexture(Handler handler, @Nullable TextureImageListener callback) {
        this.handler = handler;
        this.callback = callback;
        textureIdHolder = new int[1];
    }

    /**
     * Initializes required EGL parameters and creates the {@link SurfaceTexture}.
     *
     * @param secureMode The {@link SecureMode} to be used for EGL surface.
     */
    public void init(@SecureMode int secureMode) {
        display = getDefaultDisplay();
        EGLConfig config = chooseEGLConfig(display);
        context = createEGLContext(display, config, secureMode);
        surface = createEGLSurface(display, config, context, secureMode);
        generateTextureIds(textureIdHolder);
        texture = new SurfaceTexture(textureIdHolder[0]);
        texture.setOnFrameAvailableListener(this);
    }

    /** Releases all allocated resources. */
    @SuppressWarnings({"nullness:argument.type.incompatible"})
    public void release() {
        handler.removeCallbacks(this);
        try {
            if (texture != null) {
                texture.release();
                GLES20.glDeleteTextures(1, textureIdHolder, 0);
            }
        } finally {
            if (display != null && !display.equals(EGL14.EGL_NO_DISPLAY)) {
                EGL14.eglMakeCurrent(
                        display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            }
            if (surface != null && !surface.equals(EGL14.EGL_NO_SURFACE)) {
                EGL14.eglDestroySurface(display, surface);
            }
            if (context != null) {
                EGL14.eglDestroyContext(display, context);
            }
            // EGL14.eglReleaseThread could crash before Android K (see [internal: b/11327779]).
            if (SDK_INT >= 19) {
                EGL14.eglReleaseThread();
            }
            if (display != null && !display.equals(EGL14.EGL_NO_DISPLAY)) {
                // Android is unusual in that it uses a reference-counted EGLDisplay.  So for
                // every eglInitialize() we need an eglTerminate().
                EGL14.eglTerminate(display);
            }
            display = null;
            context = null;
            surface = null;
            texture = null;
        }
    }
    /**
     * Throws {@link NullPointerException} if {@code reference} is null.
     *
     * @param <T> The type of the reference.
     * @param reference The reference.
     * @return The non-null reference that was validated.
     * @throws NullPointerException If {@code reference} is null.
     */
    @SuppressWarnings({"contracts.postcondition.not.satisfied", "return.type.incompatible"})
    private <T> T checkNotNull(@Nullable T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    /**
     * Returns the wrapped {@link SurfaceTexture}. This can only be called after {@link #init(int)}.
     */
    public SurfaceTexture getSurfaceTexture() {
        return checkNotNull(texture);
    }

    // SurfaceTexture.OnFrameAvailableListener

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        handler.post(this);
    }

    // Runnable

    @Override
    public void run() {
        // Run on the provided handler thread when a new image frame is available.
        dispatchOnFrameAvailable();
        if (texture != null) {
            try {
                texture.updateTexImage();
            } catch (RuntimeException e) {
                // Ignore
            }
        }
    }

    private void dispatchOnFrameAvailable() {
        if (callback != null) {
            callback.onFrameAvailable();
        }
    }

    private static EGLDisplay getDefaultDisplay() {
        EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (display == null) {
            throw new GlException("eglGetDisplay failed");
        }

        int[] version = new int[2];
        boolean eglInitialized =
                EGL14.eglInitialize(display, version, /* majorOffset= */ 0, version, /* minorOffset= */ 1);
        if (!eglInitialized) {
            throw new GlException("eglInitialize failed");
        }
        return display;
    }


    /**
     * Formats a string using {@link Locale#US}.
     *
     * @see String#format(String, Object...)
     */
    private static String formatInvariant(Object... args) {
        return String.format(Locale.US, "eglChooseConfig failed: success=%b, numConfigs[0]=%d, configs[0]=%s", args);
    }

    private static EGLConfig chooseEGLConfig(EGLDisplay display) {
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        boolean success =
                EGL14.eglChooseConfig(
                        display,
                        EGL_CONFIG_ATTRIBUTES,
                        /* attrib_listOffset= */ 0,
                        configs,
                        /* configsOffset= */ 0,
                        /* config_size= */ 1,
                        numConfigs,
                        /* num_configOffset= */ 0);
        if (!success || numConfigs[0] <= 0 || configs[0] == null) {
            throw new GlException(
                    formatInvariant(
                            /* format= */
                            success, numConfigs[0], configs[0]));
        }

        return configs[0];
    }

    private static EGLContext createEGLContext(
            EGLDisplay display, EGLConfig config, @SecureMode int secureMode) {
        int[] glAttributes;
        if (secureMode == SECURE_MODE_NONE) {
            glAttributes = new int[] {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE};
        } else {
            glAttributes =
                    new int[] {
                            EGL14.EGL_CONTEXT_CLIENT_VERSION,
                            2,
                            EGL_PROTECTED_CONTENT_EXT,
                            EGL14.EGL_TRUE,
                            EGL14.EGL_NONE
                    };
        }
        EGLContext context =
                EGL14.eglCreateContext(
                        display, config, android.opengl.EGL14.EGL_NO_CONTEXT, glAttributes, 0);
        if (context == null) {
            throw new GlException("eglCreateContext failed");
        }
        return context;
    }

    private static EGLSurface createEGLSurface(
            EGLDisplay display, EGLConfig config, EGLContext context, @SecureMode int secureMode) {
        EGLSurface surface;
        if (secureMode == SECURE_MODE_SURFACELESS_CONTEXT) {
            surface = EGL14.EGL_NO_SURFACE;
        } else {
            int[] pbufferAttributes;
            if (secureMode == SECURE_MODE_PROTECTED_PBUFFER) {
                pbufferAttributes =
                        new int[] {
                                EGL14.EGL_WIDTH,
                                EGL_SURFACE_WIDTH,
                                EGL14.EGL_HEIGHT,
                                EGL_SURFACE_HEIGHT,
                                EGL_PROTECTED_CONTENT_EXT,
                                EGL14.EGL_TRUE,
                                EGL14.EGL_NONE
                        };
            } else {
                pbufferAttributes =
                        new int[] {
                                EGL14.EGL_WIDTH,
                                EGL_SURFACE_WIDTH,
                                EGL14.EGL_HEIGHT,
                                EGL_SURFACE_HEIGHT,
                                EGL14.EGL_NONE
                        };
            }
            surface = EGL14.eglCreatePbufferSurface(display, config, pbufferAttributes, /* offset= */ 0);
            if (surface == null) {
                throw new GlException("eglCreatePbufferSurface failed");
            }
        }

        boolean eglMadeCurrent =
                EGL14.eglMakeCurrent(display, /* draw= */ surface, /* read= */ surface, context);
        if (!eglMadeCurrent) {
            throw new GlException("eglMakeCurrent failed");
        }
        return surface;
    }

    public static void checkGlError() {
        int lastError = GLES20.GL_NO_ERROR;
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            NexLog.d("NEXSURFACETEXTURE","glError " + gluErrorString(error));
            lastError = error;
        }
        if (lastError != GLES20.GL_NO_ERROR) {
            throw new RuntimeException("glError " + gluErrorString(lastError));
        }
    }

    private static void generateTextureIds(int[] textureIdHolder) {
        GLES20.glGenTextures(/* n= */ 1, textureIdHolder, /* offset= */ 0);
        checkGlError();
    }


    public static boolean isProtectedContentExtensionSupported(Context context) {
        if (SDK_INT < 24) {
            return false;
        }
        if (SDK_INT < 26 && ("samsung".equals(MANUFACTURER) || "XT1650".equals(MODEL))) {
            // Samsung devices running Nougat are known to be broken. See
            // https://github.com/google/ExoPlayer/issues/3373 and [Internal: b/37197802].
            // Moto Z XT1650 is also affected. See
            // https://github.com/google/ExoPlayer/issues/3215.
            return false;
        }

        EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        @Nullable String eglExtensions = EGL14.eglQueryString(display, EGL10.EGL_EXTENSIONS);
        return eglExtensions != null && eglExtensions.contains(EXTENSION_PROTECTED_CONTENT);
    }

    /**
     * Returns whether creating a GL context with {@value #EXTENSION_SURFACELESS_CONTEXT} is possible.
     */
    public static boolean isSurfacelessContextExtensionSupported() {
        if (SDK_INT < 17) {
            return false;
        }
        EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        @Nullable String eglExtensions = EGL14.eglQueryString(display, EGL10.EGL_EXTENSIONS);
        return eglExtensions != null && eglExtensions.contains(EXTENSION_SURFACELESS_CONTEXT);
    }
}

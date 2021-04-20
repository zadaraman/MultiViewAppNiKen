package com.nexstreaming.nexplayerengine;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaDrm;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.webkit.WebSettings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * The primary interface to the NexPlayer&trade;&nbsp;engine.
 * For details on usage, see the
 * NexPlayer&trade;&nbsp;Engine \ref legal "package" documentation.
 *
 * @author NexStreaming Corp.
 */
//@version 6.39

public final class NexPlayer
{
    static final boolean isStaticSDK = false;
    // NOTE: Unused constants have been disabled to suppress warnings; these can
    //       be re-enabled later.

    private static final int NEXPLAYER_VERSION_MAJOR = 6;
    private static final int NEXPLAYER_VERSION_MINOR = 72;

    //--- Signal status ---
    /** Normal signal status; see
     * {@link IListener#onSignalStatusChanged(NexPlayer, int, int) onSignalStatusChanged}
     * for details. */
    public static final int NEXPLAYER_SIGNAL_STATUS_NORMAL      = 0;
    /** Weak signal status; see
     * {@link IListener#onSignalStatusChanged(NexPlayer, int, int) onSignalStatusChanged}
     * for details. */
    public static final int NEXPLAYER_SIGNAL_STATUS_WEAK        = 1;
    /** No signal (out of service area); see
     * {@link IListener#onSignalStatusChanged(NexPlayer, int, int) onSignalStatusChanged}
     * for details. */
    public static final int NEXPLAYER_SIGNAL_STATUS_OUT         = 2;

    //--- Async command completion values ---
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_NONE             = 0x00000000;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_OPEN_LOCAL       = 0x00000001;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_OPEN_STREAMING   = 0x00000002;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}.
     * @deprecated Experimental; may or may not be present in future versions.*/
    public static final int NEXPLAYER_ASYNC_CMD_OPEN_TV          = 0x00000003;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_START_LOCAL      = 0x00000005;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_START_STREAMING  = 0x00000006;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. 
     * @deprecated Experimental; may or may not be present in future versions.*/
    public static final int NEXPLAYER_ASYNC_CMD_START_TV         = 0x00000007;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_STOP             = 0x00000008;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_PAUSE            = 0x00000009;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_RESUME           = 0x0000000A;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_SEEK             = 0x0000000B;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. 
     * @deprecated Experimental; may or may not be present in future versions.*/
    public static final int NEXPLAYER_ASYNC_CMD_STEP_SEEK        = 0x0000000C;
    public static final int NEXPLAYER_ASYNC_CMD_SETEXTSUBTITLE   = 0x0000000F;

    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}.
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_RECORD_START     = 0x0000001A;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}.
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_RECORD_STOP      = 0x0000001B;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}.
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_RECORD_PAUSE     = 0x0000001C;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}.
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_RECORD_RESUME    = 0x0000001D;
    /**
     * Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}.
     * @deprecated Ignore if the application receives this event. 
     * \since version 6.3.4
     */
    public static final int NEXPLAYER_ASYNC_CMD_REINITVIDEO  = 0x00000013;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}.
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_TIMESHIFT_CREATE    = 0x00000021;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}.
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_TIMESHIFT_DESTROY   = 0x00000022;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}.
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_TIMESHIFT_PAUSE     = 0x00000023;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}.
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_TIMESHIFT_RESUME    = 0x00000024;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}.
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_TIMESHIFT_FORWARD   = 0x00000025;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}.
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_TIMESHIFT_BACKWARD  = 0x00000026;

    /** Possible value for <code>command</code> parameter of
     * {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}.
     * \since version 5.12  */
    public static final int NEXPLAYER_ASYNC_CMD_FASTPLAY_START = 0x00000027;
    /** Possible value for <code>command</code> parameter of
     * {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}.
     * \since version 5.12  */
    public static final int NEXPLAYER_ASYNC_CMD_FASTPLAY_STOP = 0x00000028;

    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_SET_MEDIA_STREAM = 0x00000031;

    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_SET_MEDIA_TRACK = 0x00000032;

    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_SET_MEDIA_STREAM_TRACK = 0x00000033;

    /** Possible value for \c msg parameter of
     * {@link IListener#onDownloaderAsyncCmdComplete(NexPlayer mp, int msg, int param1, int param2) onDownloaderAsyncCmdComplete}. */
    public static final int NEXDOWNLOADER_ASYNC_CMD_OPEN			= 0x00200001;
    /** Possible value for \c msg parameter of
     * {@link IListener#onDownloaderAsyncCmdComplete(NexPlayer mp, int msg, int param1, int param2) onDownloaderAsyncCmdComplete}. */
    public static final int NEXDOWNLOADER_ASYNC_CMD_CLOSE			= 0x00200002;
    /** Possible value for \c msg parameter of
     * {@link IListener#onDownloaderAsyncCmdComplete(NexPlayer mp, int msg, int param1, int param2) onDownloaderAsyncCmdComplete}. */
    public static final int NEXDOWNLOADER_ASYNC_CMD_START			= 0x00200003;
    /** Possible value for \c msg parameter of
     * {@link IListener#onDownloaderAsyncCmdComplete(NexPlayer mp, int msg, int param1, int param2) onDownloaderAsyncCmdComplete}. */
    public static final int NEXDOWNLOADER_ASYNC_CMD_STOP			= 0x00200004;

    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_OPEN_STORE_STREAM	= 0x00000101;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_START_STORE_STREAM	= 0x00000102;

    /** Treats \c path as a local media file; a possible value for the \c type parameter of \link NexPlayer.open \endlink. */
    public static final int NEXPLAYER_SOURCE_TYPE_LOCAL_NORMAL      = 0;
    /** Treats \c path as a URL to a streaming media source; a possible value for the \c type parameter of \link NexPlayer.open \endlink. */
    public static final int NEXPLAYER_SOURCE_TYPE_STREAMING         = 1;
    /** Treats \c path as a URL to a streaming media source to be stored for offline playback; a possible value for the \c type parameter of \link NexPlayer.open \endlink. */
    public static final int NEXPLAYER_SOURCE_TYPE_STORE_STREAM      = 2;

    /** Use TCP as the transport; possible value for the \link NexPlayer.open \endlink method */
    public static final int NEXPLAYER_TRANSPORT_TYPE_TCP            = 0;
    /** Use UDP as the transport; possible value for the \link NexPlayer.open \endlink method */
    public static final int NEXPLAYER_TRANSPORT_TYPE_UDP            = 1;


    /** possible value for the \link NexPlayer.enableTrack \endlink method  */
    /** Enable the track which is disabled by temporary content issues; possible value for the \link NexPlayer.enableTrack \endlink method */
    public static final int NEXPLAYER_TRACK_ENABLE_OPTION_DISABLED_TEMPORARY         = 1;
    /** Enable the track which is disabled by performance issues; possible value for the \link NexPlayer.enableTrack \endlink method */
    public static final int NEXPLAYER_TRACK_ENABLE_OPTION_DISABLED_PERFORMANCE       = 2;

    // --- Return values for getState() ---
    /** No state information available for NexPlayer&trade;&nbsp;(this
     * is the state after {@link NexPlayer#release() release} has
     * been called); a possible \c return value of \link NexPlayer#getState() NexPlayer.getState\endlink.
     */
    public static final int NEXPLAYER_STATE_NONE = 0;

    /** No media source is open (this is the state when
     * the NexPlayer&trade;&nbsp;instance is initially created, and
     * after {@link NexPlayer#close() close} has completed);
     * a possible \c return value of \link NexPlayer#getState() getState\endlink.
     */
    public static final int NEXPLAYER_STATE_CLOSED = 1;

    /** A media source is open but is currently stopped (this is the state
     * after {@link NexPlayer#open open} or {@link NexPlayer#stop() stop} has completed); 
     * a possible \c return value of \link NexPlayer#getState() getState\endlink.
     */
    public static final int NEXPLAYER_STATE_STOP = 2;

    /** A media source is open and playing (this is the state
     * after {@link NexPlayer#start(int) start} has completed);
     * a possible \c return value of \link NexPlayer#getState() getState\endlink.
     */
    public static final int NEXPLAYER_STATE_PLAY = 3;

    /** A media source is open but has been paused (this is the state
     * after {@link NexPlayer#pause() pause} has completed); 
     * a possible \c return value of \link NexPlayer#getState() getState\endlink.
     */
    public static final int NEXPLAYER_STATE_PAUSE = 4;

    /** A media source is open and fast playing(this is the state
     * after {@link NexPlayer#start() start} has completed); 
     * a possible \c return value of \link NexPlayer#getState() getState\endlink.
     */
    public static final int NEXPLAYER_STATE_PLAYxN = 5;

    private static final int NEXPLAYER_SUPPORT_MUTLIVIEW = 10;

    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_MEDIA_TYPE = 0;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_MEDIA_DURATION = 1;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_CODEC = 2;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_WIDTH = 3;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_HEIGHT = 4;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_FRAMERATE = 5;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_BITRATE = 6;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_AUDIO_CODEC = 7;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_AUDIO_SAMPLINGRATE = 8;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_AUDIO_NUMOFCHANNEL = 9;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_AUDIO_BITRATE = 10;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_MEDIA_ISSEEKABLE = 11;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_MEDIA_ISPAUSABLE = 12;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_FOURCC = 13;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_CODEC_CLASS = 14;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_PROFILE = 15;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_LEVEL = 16;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_CODEC_ERROR = 17;

    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_RENDER_AVE_FPS = 1000;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_RENDER_AVE_DSP = 1001;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_RENDER_COUNT = 1002;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_RENDER_TOTAL_COUNT = 1003;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_CODEC_DECODING_COUNT = 1004;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_CODEC_DECODING_TOTAL_COUNT = 1005;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_CODEC_AVG_DECODE_TIME = 1006;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_CODEC_AVG_RENDER_TIME = 1007;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_CODEC_DECODE_TIME = 1008;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_CODEC_RENDER_TIME = 1009;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_AVG_BITRATE = 1010;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_FRAMEBYTES = 1011;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_AUDIO_AVG_BITRATE = 1012;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_AUDIO_FRAMEBYTES = 1013;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_FRAME_COUNT = 1014;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_TOTAL_FRAME_COUNT = 1015;
	/** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_TOTAL_SKIP_COUNT = 1016;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
	public static final int CONTENT_INFO_INDEX_SPD_CURRENT_SYNC_DIFF = 1017;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
	public static final int CONTENT_INFO_INDEX_TOTAL_BUFFERING_TIME = 2000;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
	public static final int CONTENT_INFO_INDEX_MEDIA_OPEN_TIME = 2001;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
	public static final int CONTENT_INFO_INDEX_MEDIA_START_TIME = 2002;

    /** A possible argument value for {@link NexPlayer#getBufferInfo(int, int) getBufferInfo}*/
    public static final int NEXPLAYER_BUFINFO_INDEX_BUFSIZE						= 0x0;
    /** A possible argument value for {@link NexPlayer#getBufferInfo(int, int) getBufferInfo}*/
    public static final int NEXPLAYER_BUFINFO_INDEX_INITBUFSIZE					= 0x1;
    /** A possible argument value for {@link NexPlayer#getBufferInfo(int, int) getBufferInfo}*/
    public static final int NEXPLAYER_BUFINFO_INDEX_INITBUFTIME					= 0x2;
    /** A possible argument value for {@link NexPlayer#getBufferInfo(int, int) getBufferInfo}*/
    public static final int NEXPLAYER_BUFINFO_INDEX_BUFFEREDSIZE 				= 0x3;
    /** A possible argument value for {@link NexPlayer#getBufferInfo(int, int) getBufferInfo}*/
    public static final int NEXPLAYER_BUFINFO_INDEX_BUFRATE						= 0x4;
    /** A possible argument value for {@link NexPlayer#getBufferInfo(int, int) getBufferInfo}*/
    public static final int NEXPLAYER_BUFINFO_INDEX_FIRSTCTS 					= 0x5;
    /** A possible argument value for {@link NexPlayer#getBufferInfo(int, int) getBufferInfo}*/
    public static final int NEXPLAYER_BUFINFO_INDEX_LASTCTS						= 0x6;
    /** A possible argument value for {@link NexPlayer#getBufferInfo(int, int) getBufferInfo}*/
    public static final int NEXPLAYER_BUFINFO_INDEX_DURATION 					= 0x7;
    /** A possible argument value for {@link NexPlayer#getBufferInfo(int, int) getBufferInfo}*/
    public static final int NEXPLAYER_BUFINFO_INDEX_FRAMECOUNT					= 0x8;
    /** A possible argument value for {@link NexPlayer#getBufferInfo(int, int) getBufferInfo}*/
    public static final int NEXPLAYER_BUFINFO_INDEX_STATE						= 0x9;

    // Widevine start
    protected static final int NEXPLAYER_PROPERTY_ENABLE_MEDIA_DRM = 215;
    // Widevine end

    private final static String TAG = "NEXPLAYER_JAVA";

    /** Used for Picture-in-Picture support; not supported by current API version.
     *
     * This method is deprecated and not supported in the current API version. Do not use it.
     * @deprecated Not supported in current API version; do not use.
     */
    private long mNativeNexPlayerClient = 0; // accessed by native methods

    private Surface mSurface; // accessed by native methods
    private Surface mDummySurface;
    private SurfaceHolder mSurfaceHolder;

    private IListener    mListener;
    private IVideoRendererListener mVideoRendererListener = null;
    private NexEventForwarder mEventForwarder = new NexEventForwarder();
    private NexRecovery mEventRecovery = new NexRecovery();
    protected NexClientManager mClientManager;

    // Tracks whether or not the NexPlayer engine has been successfully initialized
    private boolean mNexPlayerInit = false;

    private int mPortingLogLevel = -1;


    private JSONObject mStoreInfo = null;

    private NexALFactory mALFactory = null;

    private String mErrorStrings = "";

    private boolean mEnableStoring = false;
    private boolean mEnableRetrieving = false;

    //NexWVSWDrm start
    private NexWVDRM mNexWVDRM = null;
    //NexWVSWDrm end

    private int selectedDrmScheme = 0;

    private NexLogsToFile mLogsToFile = null;
    private Boolean mDrmEnabled = false;

    private String mCurrentPath = "";
    private int mSourceType = -1;
    private int mTransportType = -1;

    /**
     * \brief This enum defines the categories for errors.
     *
     * <B>CAUTION:</B> This is experimental and is subject to change.
     *
     * Each error code has an associated category.  The intent of this is
     * to group errors based on cause so that a friendlier message can be
     * displayed to the user.  The exact groupings may change in future
     * versions.
     *
     * @author NexStreaming
     *
     */
    public enum NexErrorCategory {
        /** There is no error */
        NO_ERROR,

        /** Something is wrong with what was passed to the API; indicates a
         *  bug in the host application. */
        API,

        /** Something went wrong internally; this could be due to API
         *  misuse, something wrong with the OS, or a bug. */
        INTERNAL,

        /** Some feature of the media is not supported. */
        NOT_SUPPORT,

        /** General errors. */
        GENERAL,

        /** Errors we can't control relating to the system (for example,
         *  memory allocation errors). */
        SYSTEM,

        /** Something is wrong with the content itself, or it uses a
         *  feature we don't recognize. */
        CONTENT_ERROR,

        /** There was an error communicating with the server or an error
         *  in the protocol */
        PROTOCOL,

        /** A network error was detected. */
        NETWORK,

        /** An error code base value (these shouldn't be used, so this
         *  should be treated as an internal error). */
        BASE,

        /** Authentication error; not authorized to view this content,
         *  or a DRM error while determining authorization. */
        AUTH,

        /**
         * An error was generated by the Downloader module.
         */
        DOWNLOADER;
    }

    /**
     * \brief This enumerator defines the possible properties that can be set on a NexPlayer&trade;&nbsp;instance.
     *
     *
     * To set a property, call \link NexPlayer#setProperty(NexProperty, int) setProperty\endlink on
     * the NexPlayer&trade;&nbsp;instance.  To get the current value of a property, call
     *  \link NexPlayer#getProperty(NexProperty) getProperty\endlink.
     *
     * <h2>Property Fine-Tuning Guidelines</h2>
     * The default values for the properties should be acceptable for most common cases.
     * However, in some cases, adjusting the properties will give better performance or
     * better behavior.
     *
     * <h3>Fine-Tuning Buffering Time</h3>
     * When dealing with streaming content, adjusting the buffer size can give smoother
     * playback.  For RTSP streaming, the recommended buffering time is between 3 and 5
     * seconds; for HTTP Live Streaming, the recommended buffering time is 8 seconds.
     *
     * There are two settings for buffering time:  the initial time (the first time data is
     * buffered before playback starts) and the re-buffering time (if buffering is needed
     * later, after playback has started).  Both default to 5 seconds.  For example, to
     * set the buffering time to 8 seconds for HTTP Live Streaming:
     *
     * \code
     * void setBufferingTime( NexPlayer hNexPlayer ) {
     *     hNexPlayer.setProperty(
     *         NexProperty.INITIAL_BUFFERING_DURATION,
     *         8000);
     *     hNexPlayer.setProperty(
     *         NexProperty.RE_BUFFERING_DURATION,
     *         8000);
     * }
     * \endcode
     *
     * <h2>Numeric Property Identifiers</h2>
     * Properties can also be identified by numeric value. This is how NexPlayer&trade;&nbsp;identifies
     * properties internally, but in general, it is better to use this \c enum and the methods
     * listed above instead.
     *
     * If you must work with the numeric property identifiers directly,
     * you can retrieve them using the {@link com.nexstreaming.nexplayerengine.NexProperty#getPropertyCode() getPropertyCode}
     * method of a member of this enum, and the methods {@link com.nexstreaming.nexplayerengine.NexPlayer#getProperties(int) getProperties(int)} and
     * {@link com.nexstreaming.nexplayerengine.NexPlayer#setProperties(int, int) setProperties(int, int)} can be used to get or set a property based
     * on the numeric identifier.
     *
     *
     */
    public enum NexProperty {

        /**
         * The number of milliseconds of media to buffer initially before
         * beginning streaming playback (HLS, RTSP, etc.).
         *
         * This is the initial amount of audio and video that NexPlayer&trade;&nbsp;buffers
         * when it begins playback.  To set this property separately, it must be set by calling
         * \link NexPlayer#setProperty(NexProperty, int) setProperty\endlink <em>after</em>
         * calling open() and before start() is called.
         *
         * If further buffering is required later in
         * the playback process, the value of the property RE_BUFFERING_DURATION
         * will be used instead.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 5000 (5 seconds) \n
         */
        INITIAL_BUFFERING_DURATION              (9),
        /**
         * The number of milliseconds of media to buffer if additional buffering
         * is required during streaming playback (HLS, RTSP, etc).
         *
         * This is the amount of audio and video that NexPlayer&trade;&nbsp;buffers
         * when the buffer becomes empty during playback (requiring additional
         * buffering).  <em>After</em> open() is called, this property can be
         * set at any time during playback by calling
         * \link NexPlayer#setProperty(NexProperty, int) setProperty\endlink.
         *
         * For the initial buffering, the value of the property
         * INITIAL_BUFFERING_DURATION is used instead.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 5000 (5 seconds) \n
         */
        RE_BUFFERING_DURATION                   (10),
        /**
         * The number of milliseconds (as a negative number) that video is allowed
         * to run ahead of audio before the system waits for audio to catch up.
         *
         * For example, -50 means that if the current video time is more than 50msec
         * ahead of the audio time, the current video frame will not be displayed until
         * the audio catches up to the same time stamp. This is used to adjust video
         * and audio synchronization.
         *
         * \note As of version 6.33, the default value changed from -20 msec to -50 msec.
         *
         * <b>Type:</b> integer <i>(should be negative)</i> \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> -50 (50msec) \n
         */
        TIMESTAMP_DIFFERENCE_VDISP_WAIT         (13),
        /**
         * The number of milliseconds that video is allowed to run behind audio
         * before the system begins skipping frames to maintain synchronization.
         *
         * For example, 70 means that if the current video time is more than 70msec
         * behind the audio time, the current video frame will be skipped.
         * This is used to adjust video and audio synchronization.
         *
         *  \note As of version 6.33, the default value changed from 200 msec to 70 msec.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 70 (0.07 sec) \n
         */
        TIMESTAMP_DIFFERENCE_VDISP_SKIP         (14),

        /**
         * The size of the prefetch buffer to prepare for playback.
         *
         * If the buffer status satisfies either limit set by MAX_BUFFER_RATE or MAX_BUFFER_DURATION,
         * the filling of the prefetch buffer will be stopped even though there may be spare space still available
         * in the prefetch buffer.
         *
         * If this value is set to 20MB, 1/4 (5MB) is allocated to the past (content already played) and 3/4(15MB)
         * is allocated to the future (content yet to be played).
         *
         * \warning  Setting too large of a value here may lead to a large consumption of data packets
         * under 3G or LTE network conditions.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> bytes \n
         * <b>Default:</b> 50x1024x1024 (50MB) \n
         *
         * \since version 6.0.5
         */
        PREFETCH_BUFFER_SIZE	                (16),
        /**
         * The amount of time to wait for a server response before
         * generating an error event.
         *
         * If there is no response from the server for longer than
         * the amount of time specified here, an error event will be
         * generated and playback will stop.
         *
         * Set this to zero to disable timeout (NexPlayer&trade;&nbsp;will wait
         * indefinitely for a response).
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 60000 (60 seconds) \n
         */
        DATA_INACTIVITY_TIMEOUT                 (19),
        /**
         * The amount of time to wait before timing out when establishing
         * a connection to the server.
         *
         * If the connection to the server (the socket connection) cannot
         * be established within the specified time, an error event will
         * be generated and playback will not start.
         *
         * Set this to zero to disable timeout (NexPlayer&trade;&nbsp;will wait
         * indefinitely for a connection).
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 30000 (30 seconds) \n
         */
        SOCKET_CONNECTION_TIMEOUT               (20),
        /**
         * The maximum waiting time for an HTTP request/response message to be sent to NexPlayer&trade;.
         *
         * The maximum waiting time for an HTTP request/response message
         * after it is sent to the streaming server. If the reply 
         * does not arrive within this time, it will be regarded as an error.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 30,000 (30 seconds) \n
         */
        SOCKET_OPERATION_TIMEOUT                (73),
        /**
         *
         * The minimum possible port number for the RTP port that is created
         * when performing RTSP streaming over UDP. \n
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> 6000 \n
         *
         * This property is not supported in this API version.
         */
        RTP_PORT_MIN                            (22),
        /**
         *
         * The maximum possible port number for the RTP port that is created
         * when performing RTSP streaming over UDP. \n
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> 7000 \n
         *
         * This property is not supported in this API version.
         */
        RTP_PORT_MAX                            (23),
        /**
         * Prevents the audio track from playing back when set to TRUE (1). \n
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         */
        NOTOPEN_PLAYAUDIO                       (27),
        /**
         * Prevents the video track from playing back when set to TRUE (1). \n
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         */
        NOTOPEN_PLAYVIDEO                       (28),
        /**
         * Prevents the text (subtitle) track from playing back when set to TRUE (1). \n
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         */
        NOTOPEN_PLAYTEXT                        (29),


        /**
         * Sets the proxy address.
         *
         * <b>Type:</b> String\n
         * <b>Default:</b> \c null\n 
         */
        PROXY_ADDRESS							(31),

        /**
         * Sets the proxy port number.
         *
         * <b>Type:</b> integer\n
         * <b>Default:</b> 0\n
         */
        PROXY_PORT								(32),

        /**
         * The logging level for the NexPlayer&trade;&nbsp;protocol module.
         *
         * This affects the type of messages that are logged by the
         * protocol module (it does not affect the logging level of
         * other NexPlayer&trade;&nbsp;components).
         *
         * This value is made by or-ing together zero or more of the
         * following values:
         *   - <b>LOG_LEVEL_NONE (0x00000000)</b>  Don't log anything <i>(not currently supported)</i>
         *   - <b>LOG_LEVEL_DEBUG (0x00000001)</b>  Log start, stop and errors (default for the debug version)
         *   - <b>LOG_LEVEL_RTP (0x00000002)</b>  Generate log entries relating to RTP packets
         *   - <b>LOG_LEVEL_RTCP (0x00000004)</b>  Generate log entries relating to RTCP packets
         *   - <b>LOG_LEVEL_FRAME (0x00000008)</b>  Log information about the frame buffer
         *   - <b>LOG_LEVEL_ALL (0x0000FFFF)</b>  Log everything <i>(not currently supported)</i>
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> LOG_LEVEL_DEBUG \n
         */
        LOG_LEVEL                               (35),
        /**
         * Controls when video initialization happens.
         *
         * This can be any of the following values:
         *
         * <ul>
         * <li><b>AV_INIT_PARTIAL (0x00000000)</b><br />
         *  If there is an audio track, wait for audio initialization to complete
         *  before initializing video.
         * <li><b>AV_INIT_ALL (0x00000001)</b><br />
         *  Begin video initialization as soon as there is any video data, without
         *  any relation to the audio track status.
         * </ul>
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> AV_INIT_ALL \n
         */
        AV_INIT_OPTION                          (46),
        /**
         * If set to 0, returns an error or generates an error event if the audio codec is not supported.
         *
         * The default behavior (if this is 1) is to allow media playback even if the audio codec is not supported. 
         *
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> 1 \n
         */
        PLAYABLE_FOR_NOT_SUPPORT_AUDIO_CODEC    (48),
        /**
         * If set to 0, returns an error or generates an error event if the video codec is not supported.
         *
         * The default behavior (if this is 1) is to allow media playback even if the video codec is not supported.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> 1 \n
         */
        PLAYABLE_FOR_NOT_SUPPORT_VIDEO_CODEC    (49),
        /**
         * This property enables the Eye Pleaser feature.
         *
         * If the decoding or display of video frames takes too long, it may be necessary to skip
         * some frames in order to maintain a normal rate of playback.	Under normal operation,
         * frames are skipped immediately to catch up.	When Eye Pleaser is enabled, skipped
         * frames are spread out to try to make playback appear smoother.
         *
         * Note that in older versions of the NexPlayer&trade;&nbsp;SDK, this property was
         * also called USE_EYEPLEASER.
         *
         * <b>Type:</b> integer \n
         * <b>Default:</b> 1 \n
         * <b>Values:</b>
         *		- 0: Normal operation (frames are skipped immediately).
         *		- 1: Eye Pleaser enabled (skipped frames are spread out).
         *
         */
        SUPPORT_EYE_PLEASER                (50),
        /**
         * Live HLS playback option.<p>
         *
         * This must be one of the following values:
         *    - <b>LIVE_VIEW_RECENT (0x00000000)</b>
         * Start playback from the most recently received media segment (.ts) files of the HLS live playlist. 
         * (The player will begin playback at a media segment that was loaded four(4) segments earlier than the latest media segment file loaded.)
         * For example, if 5.ts is the latest media segment (.ts) file loaded in a sequence of five media segments, playback will begin at
         * the beginning of the second media segment, four segments (2.ts, 3.ts, 4.ts, and 5.ts) preceding the latest media segment file loaded. 
         *
         *    - <b>LIVE_VIEW_RECENT_BYTARGETDUR (0x00000001)</b>
         * Start playback from the most recently received media segement (.ts) files, based on the value set for the EXT-X-TARGETDURATION tag in the HLS live playlist.  
         * (The player will begin playback at the media segment that is immediately precedes the media segment that is <em>three times (x3) the target duration</em> 
         * before the latest media segment file loaded.) 
         * As a concrete example, if the target duration is set to 12 seconds and the total duration of currently loaded media segments is 48 seconds, playback will
         * begin at the media file that immediately precedes the media segment with the timestamp at 12 (48-36) seconds. 
         * If this example HLS playlist includes media segment files 1.ts (duration of 10 seconds), 2.ts (9 sec), 3.ts (11 sec), 4.ts (10 sec), and 5.ts (8 sec), 
         * then playback will begin at the first media segment, 1.ts, because it immediate precedes the 2.ts segment (where the timestamp at 12 seconds occurs).    
         *
         *    - <b>LIVE_VIEW_FIRST (0x00000002)</b>
         * Unconditionally start HLS playback from the first entry in the HLS playlist.
         *
         *    - <b>LIVE_VIEW_LOW_LATENCY (0x00000003)</b>
         * Playback starts from a position close to real-time and frame skipping may occur during playback to maintain low latency.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> LIVE_VIEW_RECENT \n
         */
        LIVE_VIEW_OPTION                    (53),

        /**
         * Specifies the segment offset when starting playback.<p>
         * This property may have different meaning depending on the LIVE_VIEW_OPTION.
         *
         * If LIVE_VIEW_OPTION is set to LIVE_VIEW_RECENT, then this property specifies the offset of segment from the live edge when starting playback.
         * For example,
         *      1: The player will select the last segment in the manifest.
         *      2: The player will select the second last segment in the manifest.
         *      3: The player will select the third last segment in the manifest.
         *      If this property is set to 0, the player will select the segment depending on its policy.
         *
         * This property is not currently defined for other LIVE_VIEW_OPTION.
         *
         * <b>Type:</b> integer \n
         * <b>Default:</b> 0 \n
         */
        LIVE_OFFSET_SEGMENT_COUNT                    (54),

        /**
         * RTSP/HTTP User Agent value.
         *
         * <b>Type:</b> String \n
         * <b>Default:</b> &ldquo; User-Agent: Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_1_2 like Mac OS X; ko-kr)
         *                      AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7D11 Safari/528.16 &rdquo;
         */
        USERAGENT_STRING                    (58),

        /**
         * Controls what is displayed while the player is waiting for audio data.
         *
         * If this is set to 1 (the default), the first video frame is displayed as soon
         * as it has been decoded, and the player waits in a "freeze-frame" state until
         * the audio starts, at which point both the audio and video play together.
         *
         * If this is set to 0, the player will not display the first video frame until
         * the audio is ready to play. Whatever was previously displayed will continue
         * to be visible (typically a black frame).
         *
         * Once audio has started, the behavior for both settings is the same; this only
         * affects what is displayed while the player is waiting for audio data.
         *
         * Under old versions of the SDK (prior to the addition of this property) the
         * default behavior was as though this property were set to zero.
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 1 \n
         */
        FIRST_DISPLAY_VIDEOFRAME                (60),


        /**
         * Sets the amount of time to wait for an {@link NexPlayer#open open} request to complete.
         *
         * This is used when NexPlayer&trade;&nbsp;tries to open new media.  If there is no response from the server for longer than
         * the amount of time specified here, the {@link NexPlayer#open open} request will be stopped
         * and {@link NexPlayer.IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}
         * will be called with the result, namely the error code, NexErrorCode.SOURCE_OPEN_TIMEOUT.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 300000 (300 seconds) \n
         *
         * \since version 6.0.5
         */
        SOURCE_OPEN_TIMEOUT                (63),

        /**
         * Set a low latency buffer option.<p>
         *
         * This must be one of the following values:
         *    - <b>LOW_LATENCY_BUFFEROPTION_NONE (0x00000000)</b>
         * The latency value is set by INITIAL_BUFFERING_DURATION and RE_BUFFERING_DURATION of NexProperty. It should set the reliable value depending on the bitrate of content and network environment.
         *
         *    - <b>LOW_LATENCY_BUFFEROPTION_AUTO_BUFFER (0x00000001)</b>
         * The latency value is calculated by the player at runtime. During playback, the latency may increase or decrease because it may change depending on the network environment.
         *
         *    - <b>LOW_LATENCY_BUFFEROPTION_CONST_BUFFER (0x00000002)</b>
         * The latency value is calculated by the player at the beginning of playback and maintains the value unchanged during playback.
         * The latency increases more than when using Auto Buffer Mode, but the rebuffering will be reduced and try to maintain constant latency after rebuffering.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> LOW_LATENCY_BUFFEROPTION_NONE \n
         */
        LOW_LATENCY_BUFFER_OPTION           (75),

        /**
         * Specifies the segment offset in time when starting playback.<p>
         * This property may have different meaning depending on the LIVE_VIEW_OPTION.
         * This property has higher priority than LIVE_OFFSET_SEGMENT_COUNT.
         * If LIVE_VIEW_OPTION is set to LIVE_VIEW_RECENT, then this property specifies the time offset of segment from the live edge when starting playback.
         * For example, if the duration of each segment is 6 seconds,\n
         * 
         * <b>1 ~ 6000:</b> The player will select the last segment in the manifest. \n
         * <b>6001 ~ 12000:</b> The player will select the second last segment in the manifest. \n
         * <b>12001 ~ 18000:</b> The player will select the third last segment in the manifest. \n\n
         * 
         * If this property is set to 0, the player will select the segment depending on its policy. \n\n
         * 
         * This property is not currently defined for other LIVE_VIEW_OPTION.\n\n
         * 
         * <b>Type:</b> integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 0 \n
         */
        LIVE_OFFSET_TIME           (76),

        /**
         * Set a low latency playback control option.<p>
         *
         * This must be one of the following values:
         *     - <b>LOW_LATENCY_SYNC_CONTROL_SEEK</b>
         * When the latency rate increases, the latency rate is reduced to the Seek function of the player.
         *
         *     - <b>LOW_LATENCY_SYNC_CONTROL_SPEEDCONTROL</b>
         * When the latency rate increases, it adjusts the regeneration rate at 1.1x playback speed, slowly reducing the latency rate.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> LOW_LATENCY_CONTROL_SEEK
         */
        LOW_LATENCY_SYNC_CONTROL_OPTION          (77),

        /**
         * Sets the range where NexPlayer&trade;&nbsp;will seek to a Random Access point rather than
         * the exact target position provided in the \link NexPlayer.seek seek\endlink API.
         *
         * \warning  This property is <b>only</b> valid when the first parameter, \c exact, in the \link NexPlayer.seek seek\endlink
         * API is \c true.
         *
         * Setting this value is a kind of option for the \link NexPlayer.seek seek\endlink API and can be used
         * to minimize the time required to seek in content by taking advantage of Random Access points in the content.
         *
         * A Random Access point is a specific position that the parser is allowed to seek to directly.
         *
         * This value sets the range where NexPlayer&trade;&nbsp;will seek from a Random Access point given by the parser
         * to a target position that equals \c msec (milliseconds), the first parameter in the seek() API.
         *
         * If the \c exact parameter, the second parameter in the \link NexPlayer.seek seek\endlink API, is \c true and
         * the difference between a Random Access point and the target position is within this value, \link NexPlayer.seek seek\endlink
         * will find and seek to the exact target position.
         * If the \c exact parameter is set to \c true and the difference between a Random Access point and the target position
         * is beyond this range, \c seek will give up the accurate target point and will instead seek to and play from
         * the Random Access point.
         *
         * For example, if NexPlayer&trade;&nbsp;is seeking to 10000 ms exactly (\c exact = \c true) and there is a Random Access
         * point at 7000 ms, if this property is set to less than 3000 ms, the player will ignore the exact target value and will
         * instead play from 7000 ms.  On the other hand, if this property is set to more than 3000 ms, then NexPlayer&trade;&nbsp;
         * will seek exactly to 10000 ms and begin playback.
         *
         * \warning  Please remember that in order to seek to a target position, audio or video frames have to be decoded.
         * If too large of a value is set here, it may cause the seek process to consume an excessive amount
         * of time especially in high resolution video content.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 10000 (10 seconds) \n
         *
         * \see \link NexPlayer.seek seek \endlink for more information
         * \since version 6.0.5
         */
        SEEK_RANGE_FROM_RA_POINT                (102),

        /**
         * If set to true, unconditionally skips all B-frames without decoding them; not currently supported. \n
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         */
        SET_TO_SKIP_BFRAME                      (103),
        /**
         *
         * Maximum amount of silence to insert to make up for lost audio frames.
         *
         * Under normal operation, if audio frames are lost (if there is a time gap
         * in received audio frames), silence will be inserted automatically to make
         * up the gap.
         *
         * However, if the number of audio frames lost represents a span of time
         * larger than the value set for this property, it is assumed that there is
         * a network problem or some other abnormal condition and silence is not
         * inserted.
         *
         * This prevents, for example, a corruption in the time stamp in an audio
         * frame from causing the system to insert an exceptionally long period of
         * silence (which could possibly prevent further audio playback or cause
         * other unusual behavior).
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 20000 (20 seconds) \n
         *
         * \warning This property is not supported in this API version.
         */
        TOO_MUCH_LOSTFRAME_DURATION             (105),
        /**
         * If set to 1, enables local file playback support. \n
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 1 \n
         */
        SUPPORT_LOCAL                           (110),
        /**
         *
         * If set to 1, enables RTSP streaming support. \n
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         *
         * \warning This property is not supported in this API version. 
         */
        SUPPORT_RTSP                            (111),
        /**
         * If set to 1, enables progressive download support. \n
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 1 \n
         */
        SUPPORT_PD                              (112),
        /**
         * If set to 1, enables Microsoft Windows Media Streaming support. \n
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         */
        SUPPORT_WMS                             (113),
        /**
         * If set to 1, enables Real Media Streaming support. \n
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         */
        SUPPORT_RDT                             (114),
        /**
         * If set to 1, enables Apple HTTP Live Streaming (HLS) support. \n
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 1 \n
         */
        SUPPORT_APPLE_HTTP                      (115),
        /**
         * If set to 1, enables HLS Adaptive Bit Rate (ABR) support. \n
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 1 \n
         */
        SUPPORT_ABR                             (116),
        /**
         * When using HLS ABR, this is the maximum allowable bandwidth.  Any track
         * with a bandwidth greater than this value will not be played back.
         *
         * @warning To take effect, this property should be set before calling {@link NexPlayer#open(String, String, String, int, int, int) NexPlayer.open}.
         * \c NexPlayer.setProperty(MAX_BW) can be used to set the bandwidth limit for initial segments of content, which are downloaded shortly after NexPlayer&trade;&nbsp;opens.
         * To change the maximum allowable bandwidth dynamically while content is playing, please call
         * the method \link NexABRController::changeMaxBandWidth changeMaxBandWidth\endlink instead.
         *
         * This property should be set to zero for no maximum.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> bps (bits per second) \n
         * <b>Default:</b> 0 (no maximum) \n
         *
         */
        MAX_BW                                  (117),
        /**
         * Limits the H.264 profile that can be selected from an HLS playlist.
         *
         * Under normal operation, the track with the highest supported H.264 profile
         * is selected from an HLS playlist.  If this property is set, no track with
         * a profile higher than this value will be selected.
         *
         * This should be set to zero for no limit.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> 0 (use any profile) \n
         */
        MAX_H264_PROFILE                        (118),
        /**
         *
         * If set to 1, lost audio frames are always ignored (silence is never inserted).
         *
         * See
         * {@link NexPlayer.NexProperty#TOO_MUCH_LOSTFRAME_DURATION TOO_MUCH_LOSTFRAME_DURATION}
         * for details about the insertion of silence for lost audio frames.
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         *
         * \warning This property is not supported in this API version. 
         */
        IGNORE_AUDIO_LOST_FRAME                 (119),
        /**
         *
         * This is used to force NexPlayer&trade;&nbsp;to begin buffering as soon as all
         * available audio frames have been processed, without regard to the state
         * of the video buffer.
         *
         * Under normal operation, when there are no audio frames left in the audio
         * buffer, NexPlayer&trade;&nbsp;switches to buffering mode and temporarily suspends
         * playback.
         *
         * There is an exception if the video buffer is more than 60% full.  In this
         * case, NexPlayer&trade;&nbsp;will continue video playback even if there is no more
         * audio available.
         *
         * Setting this property to \c true (1) bypasses this exception
         * and forces the system to go to buffering immediately if there are no audio
         * frames left to play.
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         *
         * \warning This property is not supported in this API version. 
         */
        ALWAYS_BUFFERING                        (120),
        /**
         *
         * When \c true (1), this property causes audio/video synchronization to be bypassed; not currently supported.
         *
         * In this state, audio and video are played back independently as soon as data is
         * received.
         *
         * This property can be enabled if audio and video synchronization are not important,
         * and if real-time behavior is needed between the server and the client.
         *
         * In normal cases, this should <i>not</i> be used (it should be set to zero) because it will
         * cause video and audio to quickly lose synchronization for most normal media streams.
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         *
         * \warning This property is not supported in this API version. 
         */
        IGNORE_AV_SYNC                          (121),

        /**
         * If set to 1, this enables MS Smooth Streaming support. \n
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 1 \n
         */
        SUPPORT_MS_SMOOTH_STREAMING                     (123),

        /**
         * Adjusts A/V synchronization by ofsetting video relative to audio.
         *
         * Positive values cause the video to play faster than the audio, while
         * negative values cause the audio to play faster than the video.  Under normal
         * operation, this can be set to zero, but in some cases where the synchronization
         * is bad in the original content, this can be used to correct for the error.
         *
         * While A/V synchronization is generally optimized internally by NexPlayer&trade;&nbsp;, there may
         * occasionally be devices which need to be offset in order to improve
         * overall A/V synchronization.  For examples of how to set AV_SET_OFFSET based on the
         * device in use, please see the Sample Application code as well as the
         * introductory section \ref avSync "A/V Synchronization" section.
         *
         * Appropriate values for any other problematic devices need to be determined experimentally
         * by testing manually.
         *
         * <b>Type:</b> integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Range:</b> -2000 ~ +2000 \n
         * <b>Default:</b> 0 \n
         *
         */
        AV_SYNC_OFFSET                          (124),          // JDKIM 2010/12/09

        /**
         * Limits the maximum width (in pixels) of the video tracks that can be
         * selected during streaming play.
         *
         * This is used to prevent NexPlayer&trade;&nbsp;from attempting to play
         * tracks that are encoded at too high a resolution for the device to
         * handle effectively.  NexPlayer&trade;&nbsp;will instead select a track
         * with a lower resolution.
         *
         * <b>Type:</b> integer \n
         * <b>Unit:</b> pixels \n
         * <b>Default:</b> 0x7FFFFFFF \n
         */
        MAX_WIDTH                               (125),

        /**
         * Limits the maximum height (in pixels) of the video tracks that can be
         * selected during streaming play.
         *
         * This is used to prevent NexPlayer&trade;&nbsp;from attempting to play
         * tracks that are encoded at too high a resolution for the device to
         * handle effectively.  NexPlayer&trade;&nbsp;will instead select a track
         * with a lower resolution.
         *
         * <b>Type:</b> integer \n
         * <b>Unit:</b> pixels \n
         * <b>Default:</b> 0x7FFFFFFF \n
         */
        MAX_HEIGHT                              (126),


        /**
         * This property sets the preferred bandwidth when switching tracks during streaming play.
         *
         * Under normal operation (when this property is zero), if the available
         * network bandwidth drops below the minimum needed to play the current
         * track without buffering, the player will immediately switch to a lower
         * bandwidth track, if one is available, to minimize any time spent buffering.
         *
         * If this property is set, the player will attempt to choose only tracks
         * above the specified bandwidth, even if that causes some buffering.
         * However, if the buffering becomes too severe or lasts for an extended
         * time, the player may eventually switch to a lower-bandwidth track anyway.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> kbps (kilobits per second) \n
         * <b>Default:</b> 0 \n
         *
         * \see {@link NexPlayer.NexProperty.PREFER_AV PREFER_AV}
         */
        PREFER_BANDWIDTH                        (129),

        /**
         * Controls whether NexPlayer&trade;&nbsp;prefers tracks with both
         * audio and video content.
         *
         * If this property is set to 0, if the available
         * network bandwidth drops below the minimum needed to play the current
         * track without buffering, the player will immediately switch to a lower
         * bandwidth track, if one is available, to minimize any time spent buffering.
         *
         * If this property is set to 1, the player will attempt to choose only tracks
         * that include both audio and video content, even if that causes some buffering.
         * However, if the buffering becomes too severe or lasts for an extended
         * time, the player may eventually switch to an audio-only track anyway.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> 1 \n
         * <b>Values:</b>
         *      - 0: Immediate switching to a lower bandwidth track.
         *      - 1: Prefer tracks with both audio and video.
         *
         * @see {@link NexPlayer.NexProperty#PREFER_BANDWIDTH PREFER_BANDWIDTH}
         */
        PREFER_AV                               (130),

        /**
         * Allows NexPlayer&trade;&nbsp;to switch to a lower bandwidth track if the
         * resolution or bitrate of the current track is too high for the
         * device to play smoothly.
         *
         * Under normal operation, NexPlayer&trade;&nbsp;switches tracks based solely on
         * current network conditions.  When this property is enabled, NexPlayer&trade;&nbsp;
         * will also switch to a lower bandwith track if too many frames are skipped
         * during playback.
         *
         * This is useful for content that is targeted for a variety of
         * devices, some of which may not be powerful enough to handle the higher
         * quality streams.
         *
         * The {@link NexProperty#TRACKDOWN_VIDEO_RATIO TRACKDOWN_VIDEO_RATIO} property
         * controls the threshold at which the track change will occur, if frames
         * are being skipped.
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         * <b>Values:</b>
         *     - 0: Normal behavior (switch based on network conditions only)
         *     - 1: Switch based on network conditions and device performance.
         *
         */
        ENABLE_TRACKDOWN                        (131),

        /**
         * This property controls the ratio of skipped frames that will be tolerated before
         * a track change is forced, if ENABLE_TRACKDOWN
         * is enabled.
         *
         * The formula used to determine if a track switch is necessary is:
         * \code 100 * (RenderedFrames / DecodedFrames) < TRACKDOWN_VIDEO_RATIO \endcode
         *
         * In other words, if this property is set to 70, and ENABLE_TRACKDOWN
         * is set to 1, NexPlayer&trade;&nbsp;will require that at least 70% of the decoded frames
         * be displayed.  If less than 70% can be displayed (greater than 30% skipped frames),
         * then the next lower bandwidth track will be selected.
         *
         * A performance-based track switch <b>permanently</b> limits the maximum bandwidth of
         * tracks that are eligible for playback until the content is closed.  For this reason, setting this
         * ratio higher than the default value of 70 is strongly discouraged.
         * (This differs from the bandwidth-based algorithm, which continuously adapts to current
         * network bandwidth).
         *
         * <b>Type:</b> integer \n
         * <b>Range:</b> 0 to 100 \n
         * <b>Default:</b> 70 \n
         */
        TRACKDOWN_VIDEO_RATIO                   (132),

        /**
         * Controls the algorithm used for bitrate switching when playing an HLS stream. \n
         *
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> 0 \n
         * <b>Values:</b>
         *     - <b>0:</b> Use a more aggressive algorithm: up-switching happens sooner.
         *     - <b>1:</b> Use a more conservative algorithm: up-switching happens only if a
         *          significant amount of extra bandwidth is available beyond that
         *          required to support the given bitrate.  This is similar to
         *          the iPhone algorithm.
         *
         */
        HLS_RUNMODE                             (133),

        /**
         * Additional HTTP headers to use to supply credentials when a 401 response
         * is received from the server.
         *
         * The string should be in the form of zero or more HTTP headers (header
         * name and value), and each header (including the last) should be terminated
         * with a CRLF sequence, for example:
         * \code
         * "id: test1\r\npw: 12345\r\n"
         * \endcode
         * The particulars of the headers depend on the server and the authentication
         * method being used.
         *
         * <b>Type:</b> String
         */
        HTTP_CREDENTIAL                             (134),

	 /**
         * The amount of time to wait for a server response before
         * generating an error(warning) event.
         *
         * If there is no response from the server for longer than
         * the amount of time specified here, an error(actually warning) event will be
         * generated but playback will continue to play until the time of DATA_INACTIVITY_TIMEOUT.
         *
         * Set this to zero to disable timeout (NexPlayer&trade;&nbsp;will wait
         * indefinitely for a response).
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 50000 (50 seconds) \n
         */
        DATA_INACTIVITY_TIMEOUT_WARNING             (135),


        /**
         * The minumum filled percentage of the prefetch buffer to resume filling the buffer.
         *
         * If the prefetch buffer is less full than the value set by this property, the buffer will resume filling
         * until the buffer status meets a condition set by either MAX_BUFFER_RATE or MAX_BUFFER_DURATION.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> percent \n
         * <b>Default:</b> 30 (30%) \n
         *
         * \since version 6.0.5
         */
        MIN_BUFFER_RATE								(140),


        /**
         * The maximum filled percentage of the prefetch buffer to pause filling the buffer.
         *
         * If the prefetch buffer is more than this value percent full, filling the buffer will be paused
         * until the buffer status meets the condition set by the property MIN_BUFFER_RATE.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> percent \n
         * <b>Default:</b> 90 (90%) \n
         *
         * \since version 6.0.5
         */
        MAX_BUFFER_RATE								(141),


        /**
         * The minumum duration of prefetch buffer to resume filling the buffer.
         *
         * If the duration of content available in the filling buffer is less than this value, filling of the
         * buffer will be resumed until the buffer status meets a condition set by either MAX_BUFFER_RATE or MAX_BUFFER_DURATION.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> milliseconds (ms) \n
         * <b>Default:</b> 30000 (30s) \n
         *
         * \since version 6.0.5
         */
        MIN_BUFFER_DURATION							(142),


        /**
         * The maximum duration of prefetch buffer to pause filling the buffer.
         *
         * If the duration of content available in the filling prefetch buffer is greater than this value, filling of the
         * buffer will be paused until the buffer status meets the condition of MIN_BUFFER_DURATION.
         *
         * \warning Note that when setting MAX_BUFFER_DURATION to a specific value, the value chosen must be at least
         *  twice the value of RE_BUFFERING_DURATION. If a smaller value is chosen, the value of MAX_BUFFER_DURATION will
         *  automatically be increased to twice the  value of RE_BUFFERING_DURATION. For example, if RE_BUFFERING_DURATION=5000 ms
         *  and one tries to set MAX_BUFFER_DURATION to 7000 ms, MAX_BUFFER_DURATION will automatically be set to 10000 ms instead.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> milliseconds \n
         * <b>Default:</b> 300000 (300s) \n
         *
         * \since version 6.0.5
         */
        MAX_BUFFER_DURATION							(143),

        /**
         *
         * Sets whether or not NexPlayer&trade;&nbsp;should use SyncTask feature; not currently supported.
         *
         * When the software video codec is being used, SyncTask can improve decoding performance.
         *
         * \note  This property must be set before playing content so it
         * must be set when the codec mode (SW) is selected, prior to calling NexPlayer.init or NexPlayer.open.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b>1 \n
         * <b>Values:</b> \n
         *    - <b>0:</b> Do not use SyncTask.
         *    - <b>1:</b> Use SyncTask.
         *
         * \since version 6.3.5
         *
         * \warning This property is not supported in this API version.
         */
        USE_SYNCTASK								(187),


        /**
         * This sets whether or not NexPlayer&trade;&nbsp;should select a higher track if the first track is Audio only.
         *
         * If the first track is audio only in HLS, a higher track will be selected.
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         * <b>Values:</b> \n
         *    - <b>0:</b> Do not select a higher track.
         *    - <b>1:</b> Select a higher track.
         *
         * \note This property must be set <b>before</b> NexPlayer.open is called.
         *
         * \since version 6.52
         */
        APPLS_FORCESTART_AVTRACK							(198),

        /**
         *
         * Sets the size of the buffer used to receive decoded video frames when the software video codec is used.
         *
         * Setting the buffer size in advance based on device system performance can improve decoding
         * performance with the SW codec, as more data can be decoded and saved in the buffer.
         *
         * \note This property must be used with the property USE_SYNCTASK set to 1.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> decoded video frame count \n
         * <b>Default:</b> 0 \n
         *
         * \since version 6.3.5
         *
         * \warning This property is not supported in this API version.
         */
        SW_DECODED_VIDEO_BUFFER_COUNT						(200),

        /**
         *
         * Sets the idr frame search option during seek.
         *
         * If you want to search nearest IDR frame set SEEK_NEAREST_IDR_FRAME_DURING_LOCAL(1)|SEEK_NEAREST_IDR_FRAME_DURING_STREAMING(2) as content type.
         *
         * <b>Type:</b> unsigned integer \n         *
         * <b>Default:</b> SEEK_NEAREST_IDR_FRAME_DURING_STREAMING(2) \n
         * <b>Values:</b>
         *          - <b>0:</b> not search the nearest idr-frame.
         *          - <b>1:</b> search the nearest idr-frame on Local Content.
         *          - <b>2:</b> search the nearest idr-frame on Streaming Content.
         *
         * \since version 6.3.5
         *
         * \warning This property is not supported in this API version.
         */
        SEEK_NEAREST_IDR_FRAME                              (217),

        /**
         * Controls whether or not the player honors cookies sent by the server. \n
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b>1 \n
         * <b>Values:</b>
         *    - <b>0:</b> Ignore HTTP cookie headers sent by the server.
         *    - <b>1:</b> Cookie headers received from a streaming server along
         *                with the initial manifest or playlist are included
         *                with further HTTP requests during the session.
         *
         */
        SET_COOKIE                                  (500),

        /**
         * For internal use only.  This should be otherwise ignored.
         */

        SET_DURATION_OF_UPDATE_CONTENT_INFO     (501),

        /**
         * Sets the type of caption display for CEA 608 captions, but is a deprecated property.
         *
         * Because CEA 608 closed captions include multiple text attributes and additional display modes, in past versions
         * of the NexPlayer&trade;&nbsp;SDK, this property allowed these captions to be displayed more simply in BASIC form
         * (where the CEA 608 captions
         * were essentially treated in the same way as other forms of subtitles) or allowed
         * the player to fully support all attributes and display modes available with CEA 608 specifications.
         *
         * Because the BASIC mode did not always easily allow the player to display the captions in such a way that they were easily read, especially
         * with live content, and to always support the full specification, this property has been deprecated and
         * CEA 608 closed captions are always fully implemented according to the specification, as if
         * this property were set to 1.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> 0 \n
         * <b>Values:</b>
         *     - 0 : BASIC:  Captions displayed one row at a time.
         *     - 1 : FULL:  Each character in the Closed Captions added individually.  This type of
         *       caption display supports all CEA 608 text attributes and display specifications.
         *
         * \deprecated  Do not use.
         */
        SET_CEA608_TYPE				 			(502),


        /**
         * Sets the SmoothStreaming \c LiveBackOff property when playing Smooth Streaming content.
         *
         * This property sets the duration of content (closest to live) that cannot yet be accessed or downloaded
         * by the player.  It is like setting how long to wait before asking for the latest fragment in a live
         * presentation, and thus basically sets the played "live" point back from the actual
         * live content available on the server.
         *
         * The end-to-end latency of the player (what is being played "live" in the player compared to what is
         * available live on the server) is at least the duration of \c LiveBackOff added to the duration
         * set for \c LivePlaybackOffset with SET_LIVEPLAYBACKOFFSET.
         *
         * <b>Type:</b> unsigned int \n
         * <b>Units:</b> milliseconds (ms) \n
         * <b>Default:</b> 6000 (ms) \n
         *
         * \since version 5.9
         *
         */
        SET_LIVEBACKOFF								(504),
        /**
         * Sets the SmoothStreaming \c LivePlaybackOffset property when playing Smooth Streaming content.
         *
         * This property sets the duration away from the live position to start playback when joining a
         * live presentation when the LiveView option is set to "Recent", but excludes the \c LiveBackOff
         * duration (set by SET_LIVEBACKOFF).
         *
         * As a result, live content will be played behind the actual live position by a duration
         * determined by BOTH \c LiveBackOff and the value for \c LivePlaybackOffset set here.
         *
         * Setting this property enables faster startup because it allows a buffer to be built up
         * as fast as bandwidth will support (potentially faster than real time), which creates a buffer
         * against network jitter.  It does however also increase end-to-end latency, which means what
         * is played "live" in the player is farther behind the actual live playing point of the
         * content.
         *
         * <b>Type:</b> unsigned int \n
         * <b>Units:</b> milliseconds (ms) \n
         * <b>Default:</b> 7000 (ms) \n
         *
         * \since version 5.9
         */
        SET_LIVEPLAYBACKOFFSET							(505),
        /**
         * Starts video together with or separately from audio.
         *
         * This property starts to play audio and video together when starting, if the video timestamp
         * is slower than audio's timestamp.
         *
         * If it is 1, it forces the video and audio to start at the same time.
         * If it is 0, it lets the video and audio to start separately.
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 (video and audio will start separately, based on the timestamps) \n
         */
        START_WITH_AV	(506),
        /**
         *
         * Ignores abnormal segment timestamps.
         *
         * If it is 1 or enabled, NexPlayer&trade;&nbsp;will ignore abnormal segment timestamps.
         * If it is 0 or disabled, NexPlayer&trade;&nbsp;will not ignore any abnormal segment timestamps.
         *
         * <b>Type:</b> boolean \n
         *
         * \warning This property is not supported in this API version.
         */
        IGNORE_ABNORMAL_SEGMENT_TIMESTAMP(508),


        /**
         *
         * Enables NexPlayer&trade;&nbsp;to deliver SEI (Supplemental Enhancement Information) in H.264 content to the application.
         *
         * When this property is set equal to 1 (enabled) and H.264 content includes SEI, NexPlayer&trade;&nbsp;delivers SEI picture
         * timing information through the onPictureTimingInfo method.
         *
         * <b>Type:</b> unsigned int \n
         * <b>Values:</b> \n
         * 		- 0 : Disabled.  Ignores any available SEI.\n
         * 		- 1 : Enabled.  Available SEI picture timing information will be delivered through the onPictureTimingInfo method. \n
         * <b>Default:</b> 0 (disabled) \n
         *
         * \see onPictureTimingInfo and NexPictureTimingInfo for more information.
         *
         * \since 5.15
         *
         */
        ENABLE_H264_SEI(509),

        /**
         * Sets the track selection mode NexPlayer&trade;&nbsp;will use when playing Smooth Streaming content.
         *
         * This property determines how a new track is selected when starting to play new Smooth Streaming content.  Based
         * on the mode set here, NexPlayer&trade;&nbsp;will prefer the specified range of tracks when selecting which to
         * play first.
         *
         * While the default setting of selecting high bitrate tracks will be acceptable generally, there may be certain
         * circumstances in which it is preferable that a lower bitrate track be selected first, and this property
         * offers that flexibility.
         *
         * Please see the sample code for an example of how to use this property.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Values:</b>
         *     - 0: Based on the estimated bitrate when receiving the manifest file, NexPlayer&trade;&nbsp;selects a lower bitrate track than the estimated bitrate.
         *          For example, for track bitrates of 1, 2, 3, and 4Mbps, NexPlayer&trade;&nbsp;calculates a bitrate of 3.2Mbps, and thus selects the 3Mbps bitrate track.
         *     - 1: Prefer high bitrate tracks.
         *     - 2: Prefer mid-range bitrate tracks.
         *     - 3: Prefer low bitrate tracks.\n
         *     .
         * <b>Default:</b> 1: Prefer high bitrate tracks.
         * \since version 5.14
         */
        NEW_TRACK_SELECTION_MODE(510),

        /**
         * \brief  Ignores the Roll Up (RU) column reset command in CEA 608 closed captions.
         *
         * Because CEA 608 closed captions support the Roll Up (RU) mode, the player starts displaying captions "rolling up" the
         * screen after receiving the Roll Up(RU) command.
         *
         * The RU command can however have two meanings according to the specifications.  One indicates the start of Roll Up mode.
         * The other indicates that the line should be erased and the prompt(cursor) moved to the lefthand edge of the display (to prepare for a new caption).
         *
         * In the event that some content contains many extra RU commands in the stream, captions may not be displayed properly because they
         * will not be displayed fully and will be continuouly erased from the screen.
         *
         * If this property is enabled, NexPlayer&trade;&nbsp;will ignore those extra RU column reset commands and thus will not erase the affected line or
         * move the cursor.
         *
         * <b>Type:</b> unsigned int \n
         * <b>Default:</b> 0 \n
         * <b>Values:</b> \n
         *   - 0 : Disabled.  CEA 608 closed captions will be displayed according to the specifications.
         *   - 1 : Enabled.  Roll Up column reset commands in CEA 608 closed captions will be skipped.
         *
         * \since version 6.0.5
         */
        IGNORE_CARRIAGERETURN_WHEN_RECEIVE_ROLLUP(511),


        /**
         * Enables NexPlayer&trade;&nbsp;to deliver an HTTP Request (to be used by NexPlayer&trade;) to the application.
         *
         * When this property is set equal to 1 (enabled), NexPlayer&trade;&nbsp;delivers the HTTP Request
         * that NexPlayer&trade;&nbsp;will use to the application.
         *
         * This property is related to the \c onModifyHttpRequest method.  For more information on how
         * to modify HTTP requests, please see the introductory section \ref enable_mod_http "Enabling Modified HTTP Requests".
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         * <b>Values:</b> \n
         *   - 0 : Disabled. Don't deliver any HTTP Request.
         *   - 1 : Enabled. Deliver HTTP Request to modify on the application side.
         *
         * \see NexPlayer.IListener.onModifyHttpRequest
         *
         * \since version 6.0.5
         */
        ENABLE_MODIFY_HTTP_REQUEST(512),

        /**
         * Allows NexPlayer&trade;&nbsp;to begin downloading content media files from a specified time stamp in the content.
         *
         * \warning This property is currently only supported for VOD in HTTP Live Streaming (HLS).
         *
         * NexPlayer&trade;&nbsp;allows users to start playback in the middle of a content file with the
         * \link NexPlayer.start start\endlink api, but typically, before playback can begin, the player
         * still opens content from the first media file available and then has to receive all of the media files between
         * the first file and the point where the user would like to start playback.
         *
         * When playback is to start in the middle of content, this property allows the player to skip receiving
         * the unneeded earlier media files based on the time stamp value set by the application, and instead begin
         * downloading media files from a position closer to the specified time stamp instead.
         *
         * This property can thus reduce the time needed to open and start playing a media file in
         * the middle of VOD content.
         *
         * \note This property must be set <b>before</b> NexPlayer.open is called.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 s) \n
         * <b>Default:</b> 0xFFFFFFFF \n
         *
         * \since 5.15
         */
        OPEN_MEDIA_FILE_FROM_SPECIFIED_TS(513),

        /**
         *
         * Allows NexPlayer&trade;&nbsp;to ignore the \c text \c mode command in
         * CEA608 closed captions.
         *
         * This property may be useful in cases where CEA 608 closed captions have been implemented in ways
         * other than strictly following the standard specifications which include extra \c text \c mode commands.
         * It allows NexPlayer&trade;&nbsp;to properly display those alternatively implemented captions in video content.
         *
         * \warning  In content that includes standard CEA 608 closed captions that follow the specifications, enabling this property may
         *           result in captions that do not appear as expected, so use of this property is discouraged in those cases as NexPlayer&trade;&nbsp;
         *           will properly display CEA 608 closed captions according to the specifications.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> 0 \n
         * <b>Values:</b>
         *    - <b>0:</b> Disabled.  NexPlayer&trade;&nbsp;will display
         *                CEA 608 closed captions according to the specifications.
         *    - <b>1:</b> Enabled.  NexPlayer&trade;&nbsp;will ignore the
         *                \c text \c mode command when displaying CEA 608 closed captions.
         * \since version 5.16
         */
        IGNORE_CEA608_TEXTMODE_COMMAND(514),

        /**
         *
         * Allows NexPlayer&trade;&nbsp;to add an explicit request for metadata in internet radio stream content.
         *
         * \warning  This is currently only supported in SHOUTcast and IceCast HLS content.
         *
         * If an explicit request for metadata is not made, a server may or may not send internet radio stream metadata even if it exists.
         *
         * Setting this property to 1 means an explicit request for internet radio stream metadata will be included with every content request sent
         * to a server.  If this property is set to 2, NexPlayer&trade;&nbsp;will only add an explicit request for metadata <b>after</b> initially receiving content and
         * identifying it as a SHOUTcast or Icecast stream.
         *
         * <b>Values:</b> \n
         *   - <b> 0 : DEFAULT :</b> Default mode. Does not add an additional explicit request for internet radio stream metadata.
         *   - <b> 1 : INSERT_HEADER :</b> Always inserts a header with a request for internet radio stream metadata (with every content request).
         *   - <b> 2 : INSERT_HEADER_AFTER_TRIAL :</b> If NexPlayer&trade;&nbsp;determines content is a SHOUTcast or Icecast stream, a header with
         *             an explicit request for metadata will be added to subsequent server requests.
         *
         * \since version 6.0.5
         *
         */
        REQUEST_RADIO_METADATA_MODE(515),

        /**
         * When using HLS ABR, this is the minimum allowable bandwidth.  Any track
         * with a bandwidth smaller than this value will not be played back.
         *
         *  \note  To dynamically set a minimum bandwidth allowed while content is playing, please call the method \c NexABRController::changeMinBandWidth() instead.
         * This property should be set to zero for no minimum.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> bps (bits per second) \n
         * <b>Default:</b> 0 (no minimum) \n
         */
        MIN_BW                                  (516),

        /**
         *
         * Enables rendering and display of CEA 708 closed captions in content when available.
         *
         * While CEA 608 closed captions are always enabled, it is necessary to set this property
         * to 1 in order for NexPlayer&trade;&nbsp;to support CEA 708 closed captions.
         *
         * In the case where content contains both CEA 608 and CEA 708 closed captions and this property
         * enables CEA 708 closed captions, the application will have to handle choosing which captions
         * to render and display to the user.
         *
         * <b>Type:</b> boolean
         * <b>Default:</b> 0
         * <b>Values:</b>
         *     - <b>0:</b> CEA 708 closed captions disabled.
         *     - <b>1:</b> CEA 708 closed captions enabled.
         *
         * \see NexEIA708CaptionView
         * \see NexEIA708Struct
         *
         * \since version 6.1.2
         */
        ENABLE_CEA708								(517),

        /**
         * Sets whether or not to display WebVTT text tracks automatically when they are included in content.
         *
         * In the case when both CEA 708 closed captions and WebVTT text tracks are included in content, this
         * property can be used to set whether to display the WebVTT text tracks or the closed captions automatically.
         *
         * By default, this property is set to 1 to enable WebVTT text tracks automatically if they exist in content
         * (as was the behavior of NexPlayer&trade;&nbsp;previously).  If for some reason it would be preferable that
         * CEA 708 closed captions be displayed instead of the WebVTT text tracks, this property should be set to
         * 0 with by with \c setProperty:
         * \code
         * mNexPlayer.setProperty(NexProperty.ENABLE_WEBVTT, 0);
         * \endcode
         * This property should only be called once, immediately after calling \c init but before calling \c open.
         *
         * <b>Type:</b> boolean \n
         * <b>Values:</b> \n
         *   - 0 :  WebVTT text tracks ignored; CEA 708 closed captions enabled \n
         *   - 1 :  WebVTT text tracks enabled; CEA 708 closed captions ignored \n
         * <b>Default:</b> 1 (WebVTT text tracks enabled)
         *
         * \since version 6.8.2
         */
        ENABLE_WEBVTT								(518),

        /**
         * Sets whether or not to begin playback after a part of the TS file is downloaded.
         *
         * By default, this property is set to 0 to download the first TS file completely to play content.
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         * <b>Values:</b>
         *   - 0 :  Partial prefetch ignored; playback will begin after downloading the first TS file completely.\n
         *   - 1 :  Partial prefetch enabled; playback will begin after a part of the TS file is downloaded.
         *
         * \since version 6.43
         */
        PARTIAL_PREFETCH							(519),

        /**
         * Allows custom ID3 tags added to timed metadata in content to be recognized and handled by NexPlayer&trade;.
         *
         * When customized ID3 tags with additional information have been added to the timed metadata in content,
         * this property can be used to help NexPlayer&trade;&nbsp;recognize and pass those ID3 tags and the extra information they
         * contain to an application.
         *
         * Additional customized timed metadata will be saved as an ArrayList of NexID3TagText objects and can be retrieved by calling
         * \c NexID3TagInformation.getArrExtraData.
         *
         * This property must be set <em>before</em> \c NexPlayer.open is called.
         *
         * <b>Type:</b> String \n
         * <b>Values:</b> a list of the customized ID3 tag names separated by semicolons (;)\n
         * <b>Default:</b> nothing
         *
         * \see NexID3TagText.getExtraDataID
         * \see NexID3TagInformation.getArrExtraData
         * \see NexID3TagInformation.setArrExtraData
         *
         * \since version 6.9
         *
         */
        TIMED_ID3_META_KEY							(521),

        /**
         * Sets whether or not to display TTML text tracks in ID3 tags automatically when they are included in content.
         *
         * In the case, when both CEA closed captions and TTML text tracks in ID3 tags are included in content, this
         * property can be used to set whether to display the TTML text tracks in ID3 tags or the closed captions automatically.
         *
         * By default, this property is set to 0 to disable TTML text tracks in ID3 tags automatically if they exist in content
         * (as was the behavior of NexPlayer&trade;&nbsp;previously). If for some reason it would be preferable that
         * TTML captions in ID3 tag be displayed instead of the CEA closed captions text tracks, this property should be set to
         * 1 using \c setProperty:
         * \code
         * mNexPlayer.setProperty(NexProperty.ENABLE_ID3_TTML, 1);
         * \endcode
         * This property should only be called once, after calling \c init but before calling \c open.
         *
         * \warning  Do not use with PARTIAL_PREFETCH.
         *
         * <b>Type:</b> boolean \n
         * <b>Values:</b> \n
         * - 0 : TTML text tracks in ID3 tags ignored; CEA closed captions enabled \n
         * - 1 : TTML text tracks in ID3 tags enabled; CEA closed captions ignored \n
         * <b>Default:</b> 0
         *
         * \since version 6.55
         */
        ENABLE_ID3_TTML							(522),

        /**
         * Sets the language of both audio and text played in multi-stream content.
         *
         * It can be used to set the preferred language of audio and text streams to be displayed in content, <em>before</em>
         * NexPlayer&trade;&nbsp;begins playing content.
         *
         * \warning  To change any media stream <em>while</em> content is playing, the method \link NexPlayer.setMediaStream setMediaStream\endlink
         * should be called instead.
         *
         * This property should be set by calling \c setProperty() after \c init and before \c NexPlayer.open() is called, as
         * demonstrated in the following sample code:
         * \code
         * mNexPlayer.setProperty(NexProperty.PREFER_LANGUAGE, "eng");
         * \endcode
         * \note  Accurate language labels (not the name of a text stream) should be used for the values of this property.
         *
         * <b>Type:</b> String \n
         * <b>Default:</b> \c null \n
         * <b>Values:</b> Accurate language labels as \c Strings.  For example, "eng" for English.
         *
         * \since version 6.8
         */
        PREFER_LANGUAGE								(530),
        /**
         * Sets the language to use for audio in multi-stream content, before content is played.
         *
         * This property can be used to set the preferred language of audio streams to be used in content, <em> before</em>
         * NexPlayer&trade;&nbsp;begins playing content.
         *
         * \warning  To change any media stream <em>while</em> content is playing, the method \link NexPlayer.setMediaStream setMediaStream\endlink
         * should be called instead.
         *
         * \warning To set the preferred language for both audio and text streams to the same language, use the NexProperty, PREFER_LANGUAGE, instead.
         *
         * This property should be set by calling \c setProperty() after \c init and before \c NexPlayer.open() is called.
         *
         * \note  Accurate language labels (not the name of a text stream) should be used for the values of this property.
         *
         * <b>Type:</b> String \n
         * <b>Default:</b> \c null \n
         * <b>Values:</b> Accurate language labels as \c Strings.  For example, "eng" for English.
         *
         * \since version 6.22
         */
        PREFER_LANGUAGE_AUDIO						(531),
        /**
         * Sets the language to use for text in multi-stream content, before content is played.
         *
         * This property can be used to set the preferred language of text streams to be displayed in content, <em> before</em>
         * NexPlayer&trade;&nbsp;begins playing content.
         *
         * \warning  To change any media stream <em>while</em> content is playing, the method \link NexPlayer.setMediaStream setMediaStream\endlink
         * should be called instead.
         *
         * \warning To set the preferred language for both audio and text streams to the same language, use the NexProperty, PREFER_LANGUAGE, instead.
         *
         * This property should be set by calling \c setProperty() after \c init and before \c NexPlayer.open() is called.
         *
         * \note  Accurate language labels (not the name of a text stream) should be used for the values of this property.
         *
         * <b>Type:</b> String \n
         * <b>Default:</b> \c null \n
         * <b>Values:</b> Accurate language labels as \c Strings.  For example, "eng" for English.
         *
         * \since version 6.22
         */
        PREFER_LANGUAGE_TEXT						(532),


        /**
         * Avoids waiting for the first Caption segment to download when starting to play content.
         *
         * This property can be used when playing Caption content.  By default, NexPlayer&trade;&nbsp;waits until
         * the first Caption segment is downloaded before content begins to play so that no caption text will be missed.
         *
         * However, if this property is disabled (set to 0),
         * the player will start playing content as soon as possible (instead of waiting until the first Caption segment is fully downloaded).
         * This may be preferred if content should start playing as quickly as possible (although initial Caption may be missed).
         *
         * This property should be called once, immediately after calling \c init but before calling \c open.
         *
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 1    \n
         * <b>Values:</b>
         *    - <b>0:</b> Content starts playing before first Caption segment is downloaded.
         *    - <b>1:</b> Content starts playing after first Caption segment is downloaded.
         * \since version 6.13
         */
        CAPTION_WAITOPEN								(540),

        /**
         * Sets a target bandwidth (before playing HLS content) when selecting which track to play as playback starts.
         *
         * While NexPlayer&trade;&nbsp;automatically chooses an ideal track to play based on several factors including device capability and network conditions,
         * there may be situations in which starting playback from a track with a bandwidth near a particular bandwidth is desired.
         *
         * When this property is set, NexPlayer&trade;&nbsp;selects and starts playing the track in content that has the bandwidth closest to the set bandwidth value,
         * initially ignoring other factors.
         *
         * Note that as playback continues, the track played may change as NexPlayer&trade;&nbsp;judges all factors that influence streaming playback and chooses the best option.
         *
         * This property should be called after \c init but before calling \c open.
         *
         * <b>Type:</b> int        \n
         * <b>Unit:</b> bps (bits per second) \n
         * <b>Default:</b> null    \n
         * <b>Values:</b>   The target bandwidth value, in bits per second (bps). Note that if \c START_NEARESTBW is set to 0, NexPlayer&trade;&nbsp;will
         * 					play normally, as if this property has not been set.
         *
         * \since version 6.14
         */
        START_NEARESTBW								(555),

        /**
         * Sets whether to enable or disable the Audio Only track in HLS content.
         *
         * This property should be called after \c init but before calling \c open.
         *
         * <b>Type:</b> int    \n
         * <b>Default:</b> 1    \n
         * <b>Values:</b>
         *    - <b>0:</b> Audio Only track disabled.
         *    - <b>1:</b> Audio Only track enabled.
         *
         * \since version 6.25
         */
        ENABLE_AUDIOONLY_TRACK						(556),

        /**
         * Sets whether or not to continue downloading data in pause state when playing content.
         *
         * When this property is set, content data will continue to be downloaded even when NexPlayer;&trade;&nbsp;is paused. 
         *
         * This property should be called after \c init and before calling \c open. 
         *
         * <b>Type:</b> int   \n
         * <b>Default:</b> 0   \n
         * <b>Values:</b>
         *    - <b>0:</b> Stop downloading in pause state. 
         *    - <b>1:</b> Continue downloading in pause state.
         *
         * \since version 6.25
         */
        CONTINUE_DOWNLOAD_AT_PAUSE		(561),

        /**
         * Sets whether or not to trust a content segment's timestamp when playback starts.
         *
         * This property should be called after \c init but before calling \c open.
         *
         * <b>Type:</b> int   \n
         * <b>Default:</b> 1   \n
         * <b>Values:</b>
         *    - <b>0:</b> Adjust the timestamp during playback.
         *    - <b>1:</b> Trust the timestamp during playback.
         *
         * \since version 6.25
         */
        SEGMENT_TS_RELIABLE				(570),

        /**
         * This sets whether or not to activate seamless playback when switching to another track. It can be used from Android os 4.4 and up.
         *
         *
         * <b>Type:</b> int   \n
         * <b>Default:</b> 0   \n
         * <b>Values:</b>
         *    - <b>0:</b> Disable seamless playback when switching to another track.
         *    - <b>1:</b> Enable seamless playback when switching to another track.
         *
         * \since version 6.49
         */
        ENABLE_DECODER_SEAMLESS			(571),

        /**
         * Indicates whether or not an ABR track switch callback is available.
         *
         * <b>Type:</b> int   \n
         * <b>Default:</b> 0   \n
         * <b>Values:</b>
         *    - <b>0:</b> Disable an ABR track switch callback.
         *    - <b>1:</b> Enable an ABR track switch callback.
         *
         * \since version 6.44
         */
        ENABLE_HTTPABRTRACKCHANGE_CALLBACK	(583),

        /**
         *
         * This makes sure that the currently playing track is not switched to another one with a lower frame rate when switching to a track with a higher Bitrate. 
         *
         * <b>Type:</b> int   \n
         * <b>Default:</b> 0   \n
         * <b>Values:</b>
         *    - <b>0:</b> Disable the frame rate restriction.
         *    - <b>1:</b> Enable the frame rate restriction.
         *
         * \since version 6.50
         */
        ENABLE_FRAMERATE_RESTRICTION (588),

		/**
		 *
		 * For DASH, use suggestedPresentationDelay to synchronize end users.
		 * For HLS, use the #EXT-X-PROGRAM-DATE-TIME tag to sync end users.
		 *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 2000 (2 sec) \n
         *
         * \since version 6.64
		 */
        SET_PRESENTATION_DELAY (590),

        /**
		 *
		 * Enables synchronization to UTC time(SPD).
		 *
         * <b>Type:</b> int    \n
         * <b>Default:</b> 0    \n
         * <b>Values:</b>
         *    - <b>0:</b> SPD disabled.
         *    - <b>1:</b> SPD enabled.
         *
         * \since version 6.64
		 */
		ENABLE_SPD_SYNC_TO_GLOBAL_TIME 		(591),

		/**
		 *
		 * If the current playback is not more synchronized than this value, the player will speed up playback and make sync.
		 *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 300 (300 msec) \n
         *
         * \since version 6.64
		 */
		SET_SPD_SYNC_DIFF_TIME				(592),

		/**
		 *
		 * If playback is out of sync than this value, the player will jump to synchronize the video rather than make it by speeding up.
		 *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 5000 (5 seconds) \n
         *
         * \since version 6.64
		 */
		SET_SPD_TOO_MUCH_DIFF_TIME			(593),

        /**
         *
         * Enables synchronization to device UTC (SPD).
         *
         * <b>Type:</b> int    \n
         * <b>Default:</b> 0    \n
         * <b>Values:</b>
         *    - <b>0:</b> Disabled device UTC
         *    - <b>1:</b> Enabled device UTC
         *
         * \since version 6.71
         */
        ENABLE_SPD_SYNC_TO_DEVICE_TIME			(594),

        /**
         *
         * Set number of segments to download
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> Number of segments to download \n
         * <b>Default:</b> 1 \n
         *
         * \since version 6.71
         */
        NUMBER_OF_SEGMENTS_TO_DOWNLOAD			(595),

        /**
         *
         * Set reference server utc for SPD
         * This property should be set before calling \c open.
         * The value is string of Unix epoch time in miliseconds e.g. "1602555563000" is Tuesday, October 13, 2020 2:19:23 AM (GMT).
         * 
         * If the value is less than availability start time in MPD, media not found will occur.
         * If difference between reference server utc and device time is greater than a day, it will be ignored.
         * 
         * <b>Type:</b> String \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> null    \n
         *
         * \since version 6.72
         */
        SET_REFERENCE_SERVER_UTC			(596),

        /**
         * Set it to get detail error through \link NexPlayer.getDetailedError \endlink.
         * For more information, \link NexPlayer.getDetailedError \endlink.
         *
         * This property should be called after \c init but before calling \c open.
         *
         * <b>Type:</b> int \n
         * <b>Default:</b> 0 \n
         * <b>Values:</b>
         *    - <b>0:</b> not supporting detail error.
         *    - <b>1:</b> supporting detail error.
         *
         * \since version 6.56.10
         */
        ENABLE_DETAIL_ERROR						(600),

        /**
         * Set it to enable tunneled playback if the device supports tunneled playback. If not supported, it will be is ignored.
         * It can be used from Android os 5.0 and up.
         *
         * This property should be called after \c init but before calling \c open.
         *
         * <b>Type:</b> int \n
         * <b>Default:</b> 0 \n
         * <b>Values:</b>
         *    - <b>0:</b> disable
         *    - <b>1:</b> enable
         *
         * \since version 6.64
         */
        ENABLE_TUNNELED_PLAYBACK				(601),

        /**
         * Set it to decide which events passed through onDashScte35Event listener.
         *
         * This property should be called after \c init but before calling \c open.
         *
         * <b>Type:</b> int \n
         * <b>Default:</b> 0 \n
         * <b>Values:</b>
         *    - <b>0:</b> Only scte35 events will be passed through onDashScte35Event listener.
         *    - <b>1:</b> All the events in the MPD will be passed through onDashScte35Event listener.
         *
         * \since version 6.65
         */
        SEND_ALL_DASH_MPD_EVENTS				(605),

        /**
         * Set the duration for retrying when <b>404 Not Found</b> or <b>403 Forbidden</b> HTTP status code occurred.<p>
         *
         * When getting <b>404 Not Found</b> or <b>403 Forbidden</b> HTTP status code while playing live content, NexPlayer will retry to connect during this value.
         * If NexPlayer fails to connect during that time, NexPlayer will change to another track.
         * In this case, the failed track is temporarily disabled. The disabled track will enable after 5 and 10 minutes. \n
         * If set value is 0, the duration is set to the segment duration(#EXT-X-TARGETDURATION) of the content. \n
         * 
         * \warning  This is only for HLS Live content.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> milliseconds \n
         * <b>Default:</b> 0xFFFFFFFF \n
         * <b>Values:</b>
         *     - <b>0:</b> Retry only for the duration of segment(#EXT-X-TARGETDURATION). \n
         *     - <b>Above 0:</b> Retry only for the value(msec).
         *
         * \since version 6.68
         */
         HTTP_FAILOVER_DURATION                 (610),

        /**
         * Set it to extend max caption length.
         *
         * This property should be called after \c init but before calling \c open.
         *
         * <b>Type:</b> int \n
         * <b>Default:</b> 8192 \n        
         * \since version 6.64
         */
        MAX_CAPTION_LENGTH				(901),

        /**
         *
         * Enables to deliver all logging messages of a native framework to app side instead of using adb system.
         *
         * <b>Type:</b> int    \n
         * <b>Default:</b> 0    \n
         * <b>Values:</b>
         *    - <b>0:</b> Disabled to deliver a log to app side
         *    - <b>1:</b> Enabled to deliver a log to app side
         *
         * \since version 6.71
         */
        ENABLE_LOGGING_TO_APP_SIDE			(902),

        /**
         * Sets a name of unix domain socket.
         *
         * <b>Type:</b> String\n
         * <b>Default:</b> \c null\n
         */
        SET_UDS_NAME_FOR_LOGGING							(903),

        /**
         * If set to 1, enables to support HTTP2 Protocol.
         *
         * <b>Type:</b>  Boolean \n
         * <b>Default:</b> 0\n
         */
        ENABLE_SUPPORT_HTTP2							(904),

        /**
         * Indicates whether or not speed control is available on this device.
         *
         * As audio solution components are optional, this property can be checked for availability.
         *
         * This is useful to determine whether to display the speed control
         * in the user interface.
         *
         * <b>Type:</b> unsigned integer <i>(read-only)</i> \n
         * <b>Values:</b>
         *    - <b>0:</b> Device does not support speed control.
         *    - <b>1:</b> Device supports speed control.
         *
         */
        SPEED_CONTROL_AVAILABILITY                  (0x00050001),

        /**
         * Indicates whether or not the NexSound audio solution component, EarComfort, is available on
         * this device.
         *
         * As audio solution components are optional, this property can be checked for availability.
         *
         * <b>Type:</b> unsigned integer <i>(read-only)</i> \n
         * <b>Values:</b>
         *    - <b>0:</b> Device does not support EarComfort component.
         *    - <b>1:</b> Device supports EarComfort component.
         */
        AS_EARCOMFORT_AVAILABILITY                  	(0x00050002),

        /**
         * Indicates whether or not the NexSound audio solution component, Reverb, is available on
         * this device.
         *
         * As audio solution components are optional, this property can be checked for availability.
         *
         * <b>Type:</b> unsigned integer <i>(read-only)</i> \n
         * <b>Values:</b>
         *    - <b>0:</b> Device does not support Reverb component.
         *    - <b>1:</b> Device supports Reverb component.
         */
        AS_REVERB_AVAILABILITY                      	(0x00050003),

        /**
         * Indicates whether or not the NexSound audio solution component, Stereo Chorus, is available on
         * this device.
         *
         * As audio solution components are optional, this property can be checked for availability.
         *
         * <b>Type:</b> unsigned integer <i>(read-only)</i> \n
         * <b>Values:</b>
         *    - <b>0:</b> Device does not support Stereo Chorus component.
         *    - <b>1:</b> Device supports Stereo Chorus component.
         */
        AS_STEREO_CHORUS_AVAILABILITY               	(0x00050004),

        /**
         * Indicates whether or not the NexSound audio solution component, Music Enhancer, is available on
         * this device.
         *
         * As audio solution components are optional, this property can be checked for availability.
         *
         * <b>Type:</b> unsigned integer <i>(read-only)</i> \n
         * <b>Values:</b>
         *    - <b>0:</b> Device does not support Music Enhancer component.
         *    - <b>1:</b> Device supports Music Enhancer component.
         */
        AS_MUSIC_ENHANCER_AVAILABILITY              	(0x00050005),

        /**
         * Indicates whether or not the NexSound audio solution component, Cinema Sound, is available on
         * this device.
         *
         * As audio solution components are optional, this property can be checked for availability.
         *
         * <b>Type:</b> unsigned integer <i>(read-only)</i> \n
         * <b>Values:</b>
         *    - <b>0:</b> Device does not support Cinema Sound component.
         *    - <b>1:</b> Device supports Cinema Sound component.
         */
        AS_CINEMA_SOUND_AVAILABILITY                	(0x00050006),

        /**
         * Indicates whether or not the NexSound audio solution, speed control component, DTS audio solution headphone X, and head tracking are available on
         * this device.
         *
         * As audio solutions components are optional in the NexPlayer&trade;&nbsp;SDK, this property can be used
         * to checked their availability.
         *
         * When this property is set equal to 1 (enabled), NexPlayer&trade;&nbsp;uses NexSound audio solution and speed control.
         * When this property is set equal to 2 (enabled), NexPlayer&trade;&nbsp; uses DTS audio solution and headphone X.
         * When this property is set equal to 4 (enabled), NexPlayer&trade;&nbsp; uses DTS audio solution and head tracking.
         *
         * This property is related to the \c notifyHeadsetState method.
         *
         * <b>Type:</b> unsigned integer  \n
         * <b>Default:</b> 0 (Disabled/Not Available) \n
         * <b>Values:</b>
         *    - <b>0:</b> Disabled/Not Available.  NexPlayer&trade;&nbsp;does not support any other sound effects.
         *    - <b>1:</b> NexSound Speed Control/Available.  NexPlayer&trade;&nbsp; supports NexSound audio solution and speed control.
         *    - <b>2:</b> DTS Headphone X/Available.  NexPlayer&trade;&nbsp; supports DTS audio solution and headphone X.
         *    - <b>4:</b> DTS Head Tracking/Available.  NexPlayer&trade;&nbsp; supports DTS audio solution and head tracking.
         *
         * \see NexPlayer.notifyHeadsetState
         * \since version 6.0.7.3
         */
        ENHANCED_SOUND_AVAILABILITY                   	(0x00050008),

        /**
         *
         * Check the HTTP Request and Response headers length.
         *
         * If this property is set, NexPlayer will check the HTTP request and response headers,
         * and send them through \c onHTTPRequest and \c onHTTPResponse.
         *
         * <b>Type:</b> int   \n
         * <b>Default:</b> 0   \n
         * <b>Values:</b>
         *    - <b>0:</b> Do not check the HTTP Request and Response headers length.
         *    - <b>1:</b> Check the HTTP Request and Response headers length.
         *
         * \see NexPlayer.onHTTPRequest
         * \see NexPlayer.onHTTPResponse
         *
         * \since version 6.53.
         */
        CHECK_HTTP_HEADER_LENGTH                        (0x00060001),

        /**
         *
         * Controls the maximum number of pages the player can allocate for
         * the remote file cache.
         *
         * The remote file cache stores data that has been read from disk or
         * received over the network (this includes local, streaming and
         * progressive content).
         *
         * In general, this value should not be changed, as an incorrect
         * setting can adversely affect performance, particularly when seeking.
         *
         * In order to play multiplexed content, at least one audio chunk and
         * one video chunk must fit inside a single RFC buffer page.  For certain formats
         * (PIFF, for example) at very high bitrates, the chunks may be too big
         * to fit in a single page, in which case the RFC buffer page size will need
         * to be increased.  If the system has limited memory resources, it may be
         * necessary to decrease the buffer count when increasing the page size.
         *
         * Increasing the page size can increase seek times, especially for data
         * received over the network (progressive download and streaming cases), so
         * this value should not be changed unless there are issues playing
         * specific content that cannot be solved in another way.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> number of buffers \n
         * <b>Default:</b> 20 \n
         *
         * \warning This property is not supported in this API version. 
         */
        RFC_BUFFER_COUNT                            (0x00070001),

        /**
         *
         * Controls the size of each page in the remote file cache.
         *
         * Use caution when adjusting this value.  Improper settings may
         * adversely affect performance, or may cause some content to
         * fail to play.
         *
         * \see RFC_BUFFER_COUNT for a detailed description.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> kilobytes (kB) \n
         * <b>Default:</b> 256 \n
         *
         * \warning This property is not supported in this API version. 
         */
        RFC_BUFFER_PAGE_SIZE                        (0x00070002),

        /**
         * An RTSP/HTTP User Agent value associated with the Downloader module.
         * This property should be set before the Downloader module is opened.
         *
         * <b>Type:</b> String \n
         */
        DOWNLOADER_USERAGENT_STRING		(0x00090001),
        /**
         *  This property adds additional header fields to be sent along with the HTTP headers
         *  when sending streaming requests (HLS and Smooth Streaming) from the Downloader module.
         *  This property should be set before the Downloader module is opened by DownloaderOpen().
         *
         *
         *  <b>Type:</b> String \n
         */
        DOWNLOADER_HTTP_HEADER			(0x00090002),

        /**
         * For limited time versions of NexPlayer&trade;, this indicates the start date of the limited period of valid use.
         *
         * Time locked versions of NexPlayer&trade;&nbsp;will only be valid and play content during the period
         * defined by the properties \c LOCK_START_DATE and \c LOCK_END_DATE, and will otherwise
         * return an error ( \c PLAYER_ERROR_TIME_LOCKED).
         *
         * This property cannot be set independently but can only be retrieved by calling
         * \link NexPlayer#getStringProperty(NexProperty) getStringProperty\endlink.  This may be useful for informing users of the limited availability
         * of the player they are using.
         *
         * <b>Type:</b> String \n
         * <b>Default:</b> 0 \n
         *
         * \since version 5.10
         */
        LOCK_START_DATE					(0x000A0001),			// JDKIM 2012/06/11

        /**
         * For limited time versions of NexPlayer&trade;, this indicates the end date of the limited period of valid use.
         *
         * Time locked versions of NexPlayer&trade;&nbsp;will only be valid and play content during the period
         * defined by the properties \c LOCK_START_DATE and \c LOCK_END_DATE, and will otherwise
         * return an error ( \c PLAYER_ERROR_TIME_LOCKED).
         *
         * This property cannot be set independently but can only be retrieved by calling
         * \link NexPlayer#getStringProperty(NexProperty) getStringProperty\endlink.  This may be useful for informing users of the limited availability
         * of the player they are using.
         *
         * <b>Type:</b> String \n
         * <b>Default:</b> 0 \n
         *
         * \since version 5.10
         */
        LOCK_END_DATE					(0x000A0002),		// JDKIM 2012/06/11

        SUBTITLE_TEMP_PATH              (0x000A0003),

        SET_LOGS_TO_FILE                (0x000D0000);

        private int mIntCode;

        /** This is a possible setting for the LOG_LEVEL property; see that property for details. */
        public static final int LOG_LEVEL_NONE = 0x00000000;
        /** This is a possible setting for the LOG_LEVEL property; see that property for details. */
        public static final int LOG_LEVEL_DEBUG = 0x00000001;
        /** This is a possible setting for the LOG_LEVEL property; see that property for details. */
        public static final int LOG_LEVEL_RTP = 0x00000002;
        /** This is a possible setting for the LOG_LEVEL property; see that property for details. */
        public static final int LOG_LEVEL_RTCP = 0x00000004;
        /** This is a possible setting for the LOG_LEVEL property; see that property for details. */
        public static final int LOG_LEVEL_FRAME = 0x00000008;
        /** This is a possible setting for the LOG_LEVEL property; see that property for details. */
        public static final int LOG_LEVEL_ALL = 0x0000FFFF;

        /** This is a possible setting for the AV_INIT_OPTION property; see that property for details. */
        public static final int AV_INIT_PARTIAL = 0x00000000;
        /** This is a possible setting for the AV_INIT_OPTION property; see that property for details. */
        public static final int AV_INIT_ALL = 0x00000001;

        /** This is a possible setting for the LIVE_VIEW_OPTION property; see that property for details. */
        public static final int LIVE_VIEW_RECENT = 0x00000000;
        /** This is a possible setting for the LIVE_VIEW_OPTION property; see that property for details. */
        public static final int LIVE_VIEW_RECENT_BYTARGETDUR = 0x00000001;
        /** This is a possible setting for the LIVE_VIEW_OPTION property; see that property for details. */
        public static final int LIVE_VIEW_FIRST = 0x00000002;
        /** This is a possible setting for the LIVE_VIEW_OPTION property; see that property for details. */
        public static final int LIVE_VIEW_LOW_LATENCY = 0x00000003;

        /** This is a possible setting for the LOW_LATENCY_BUFFER_OPTION property; see that property for details. */
        public static final int LOW_LATENCY_BUFFEROPTION_NONE = 0x00000000;
        /** This is a possible setting for the LOW_LATENCY_BUFFER_OPTION property; see that property for details. */
        public static final int LOW_LATENCY_BUFFEROPTION_AUTO_BUFFER = 0x00000001;
        /** This is a possible setting for the LOW_LATENCY_BUFFER_OPTION property; see that property for details. */
        public static final int LOW_LATENCY_BUFFEROPTION_CONST_BUFFER = 0x00000002;

        /** This is a possible setting for the LOW_LATENCY_CONTROL_OPTION property; see that property for details. */
        public static final int LOW_LATENCY_SYNC_CONTROL_SEEK = 0x00000000;
        /** This is a possible setting for the LOW_LATENCY_CONTROL_OPTION property; see that property for details. */
        public static final int LOW_LATENCY_SYNC_CONTROL_SPEEDCONTROL = 0x00000001;


        /**
         * Sets the NexProperty
         */
        NexProperty( int intCode ) {
            mIntCode = intCode;
        }

        /**
         * Gets the integer code for the NexPlayer&trade;&nbsp;property.
         *
         * @return The integer code for the specified property.
         */
        public int getPropertyCode( ) {
            return mIntCode;
        }
    }

    /**
     * \brief Possible error codes that NexPlayer&trade;&nbsp;can return.
     *
     * This is a Java \c enum so
     * each error constant is an object, but you can convert to or from a numerical
     * code using instance and class methods.
     *
     * To get the error constant for a given code, call {@link com.nexstreaming.nexplayerengine.NexPlayer.NexErrorCode#fromIntegerValue(int) fromIntegerValue(int)}.
     *
     * To get the error code given an error constant, call {@link com.nexstreaming.nexplayerengine.NexPlayer.NexErrorCode#getIntegerCode() getIntegerCode()}.
     *
     * Because this is a Java \c enum, it is very easy to include the name of the
     * error constant in an error message instead of just the number.  For example, the following
     * code logs the errors that are received from the NexPlayer&trade;&nbsp;engine:
     *
     * \code
     * void onError( NexPlayer mp,
     *               NexErrorCode errorCode )
     * {
     *     NexLog.d( "onError",
     *            "Received the error: "
     *               + errorCode.name()
     *               + " (0x"
     *               + Integer.toHexString(
     *                    errorCode.getIntegerCode())
     *               + ")."
     *          );
     * }
     * \endcode
     *
     * @author NexStreaming
     *
     */
    public enum NexErrorCode {
        /**
         * No error.
         */
        NONE(                           0x00000000,NexErrorCategory.NO_ERROR,"No error"),

        /**
         * The same command has been called already in the same state or the command is invalid.
         * E.g. If an open API is called while processing an open API, the engine does not regard it as an error.
         */
        HAS_NO_EFFECT(                  0x00000001,NexErrorCategory.API,"Method has no effect"),

        /**
         * This error will be called if the UI has wrong parameters such as \c NULL.
         */
        INVALID_PARAMETER(              0x00000002,NexErrorCategory.API,"Parameter is invalid"),

        /**
         * The command is invalid at the current state.
         * E.g. pause() is called when the current state is NEXPLAYER_STATE_STOP.
         */
        INVALID_STATE(                  0x00000004,NexErrorCategory.API,"State is invalid"),

        /**
         * The content file contains invalid syntax.
         * It should be checked whether or not the content is a normal DRM file.
         */
        INVALID_MEDIA(                  0x00000007,NexErrorCategory.CONTENT_ERROR, "File contains invalid syntax"),

        /**
         * NexPlayer does not support the audio codec of the content.
         * E.g. The content is not yet supported by NexPlayer or a customer could not include codecs of NexPlayer SDK due to licensing issues.
         */
        NOT_SUPPORT_AUDIO_CODEC(        0x00000009,NexErrorCategory.NOT_SUPPORT, "The audio codec is not supported"),

        /**
         * NexPlayer does not support the video codec of the content.
         * E.g. The content is not yet supported by NexPlayer or the customer did not include codecs of NexPlayer SDK due to licensing issues.
         */
        NOT_SUPPORT_VIDEO_CODEC(        0x0000000A,NexErrorCategory.NOT_SUPPORT, "The video codec is not supported"),

        /**
         * The resolution of the content is too high to play back.
         * E.g. The device has a resolution limit of 1080P but the content is 4K.
         */
        NOT_SUPPORT_VIDEO_RESOLUTION(   0x0000000B,NexErrorCategory.NOT_SUPPORT, "The video resolution is not supported"),

        /**
         * The format of the content is not supported.
         * The content has unavailable protocol(HLS,DASH, etc) or no playable A/V track which has audio or video codecs that are not supported.
         */
        NOT_SUPPORT_MEDIA(              0x0000000C,NexErrorCategory.NOT_SUPPORT, "The content format is not supported or is not playable A/V track"),

        /**
         * Audio or video decoding error.
         * E.g.  NexPlayer get a failure during parsing the content for playback or during decoding the audio or video bitstreams..
         */
        CODEC_DECODING_ERROR(           0x0000000E,NexErrorCategory.GENERAL, "The codec reported an error"),

        /**
         * Unknown error. 
         * This error is a kind of internal error such as "system failure".
         * E.g. NexPlayer returns this when memory allocation is failed for unknown reasons. It's mostly an error that the user can not handle.
         * Please contact a NexPlayer developer for more details.
         */
        UNKNOWN(                        0x00000017,NexErrorCategory.GENERAL,"Unknown Error"),

        /**
         * The media source does not support seeking.
         * The only Iframe content(Content is composed of I, B, and P frames), chunked mode based PD content, or live content with an wide interval between each Iframe cannot be seeked.
         */
        NOT_SUPPORT_TO_SEEK(            0x00000018,NexErrorCategory.NOT_SUPPORT, "The media source does not support seeking."),

        /**
         * The content has an unsupported text type.
         * For supported text, refer to  \ref CEA608CC.
         */
        NOT_SUPPORT_TEXT(             	0x0000001E,NexErrorCategory.NOT_SUPPORT, "The text is not supported"),

        /**
         * There is no response from the server within the set time of SOURCE_OPEN_TIMEOUT property while calling open().
         * The default value of the SOURCE_OPEN_TIMEOUT property is 300 seconds.
         */
        SOURCE_OPEN_TIMEOUT(            0x00000023,NexErrorCategory.GENERAL, "The media source open timed out"),

        /**
         * There is no response from the server within the set time of DATA_INACTIVITY_TIMEOUT property.
         * The default value of the DATA_INACTIVITY_TIMEOUT property is 60 seconds.
         */
        DATA_INACTIVITY_TIMEOUT(        0x00000026,NexErrorCategory.GENERAL, "The response timed out"),

        /**
         * Network related error such as socket errors.  
         * Ex. socket open fail, connect fail, bind fail,...
         */
        ERROR_NETWORK_PROTOCOL(        0x00000029,NexErrorCategory.PROTOCOL, "Network or protocol error"),

        /**
         * The content media was not found on the server.         
         * Ex. 403 forbidden, 404 not found,....
         */
        ERROR_MEDIA_NOT_FOUND(        0x0000002A,NexErrorCategory.GENERAL, "The content media was not found."),

        /**
         * The default error of content DRM Decrypt Fail
         */
        DRM_DECRYPT_FAILED(             0x00000022,NexErrorCategory.NOT_SUPPORT, "The content DRM decrypt fail"),

         /**
         * The content DRM Inital Fail         
         * Ex. Invalid LAURL,
         */
        DRM_INIT_FAILED(                0x0000002c,NexErrorCategory.NOT_SUPPORT, "The content DRM initialization fail"),

        /**
         * The content DRM Decrypt Fail
         */
        DRM_INSUFFICIENT_HDCP_LEVEL(0x0000002d, NexErrorCategory.NOT_SUPPORT, "HDCP levels are insufficient to meet the requirements."),

        /**
         * The content DRM Inital Fail
         */
        DRM_NOT_SUPPORT_HDCP(0x0000002e, NexErrorCategory.NOT_SUPPORT, "HDCP is not supported on the device."),

        /**
         * The content DRM Decrypt Fail
         * from Android 4.4 (Kitkat)
         */
        DRM_DECRYPT_NO_KEY(0x00000031, NexErrorCategory.AUTH, "Crypto key not available."),
        DRM_DECRYPT_KEY_EXPIRED(0x00000032, NexErrorCategory.AUTH, "Drm license expired."),
        DRM_DECRYPT_RESOURCE_BUSY(0x00000033, NexErrorCategory.SYSTEM, "Drm Resource busy or unavailable."),

        //
        /**
         * MediaCodec API Exception types
         * from android 6.0 (Q)
         */
        MEDIACODEC_INSUFFICIENT_RESOURCE(0x00000039, NexErrorCategory.GENERAL, "Required resource was not able to be allocated."),
        MEDIACODEC_RECLAIMED(0x0000003A, NexErrorCategory.GENERAL, "Resource manager reclaimed the media resource used by the codec."),

        /**
         * The content DRM Decrypt Fail
         * from Android 6.0 (M)
         */
        DRM_DECRYPT_SESSION_NOT_OPENED(0x00000034, NexErrorCategory.AUTH, "Attempted to use a closed drm session."),


        /**
         * The content DRM Decrypt Fail
         * from Android 7.0 (N)
         */
        DRM_DECRYPT_UNSUPPORTED_OPERATION(0x00000035, NexErrorCategory.NOT_SUPPORT, "Drm Operation not supported in this configuration."),

        //
        /**
         * The content DRM Decrypt Fail
         * from android 10.0 (Q)
         */
        DRM_DECRYPT_INSUFFICIENT_SECURITY(0x00000036, NexErrorCategory.NOT_SUPPORT, "Required security level is not met."),
        DRM_DECRYPT_FRAME_TOO_LARGE(0x00000037, NexErrorCategory.SYSTEM, "Decrytped frame exceeds size of output buffer."),
        DRM_DECRYPT_LOST_STATE(0x00000038, NexErrorCategory.GENERAL, "Drm Session state was lost."),
        
        /**
         * The URL of content is invalid.
         */
	    ERROR_INVALID_URL(        0x00010001,NexErrorCategory.NOT_SUPPORT, "The URL is invalid."),


        /**
         * The syntax of the response is invalid. 


         * Ex. Mandatory header is missed in the response.
         */
	    ERROR_INVALID_RESPONSE(        0x00010002,NexErrorCategory.NOT_SUPPORT, "The syntax of the response is invalid."),

        /**
         * ContentInfo parse fail. (SDP, ASF Header, Playlist...)
         */
	    ERROR_CONTENTINFO_PARSING_FAIL(        0x00010003,NexErrorCategory.NOT_SUPPORT, "ContentInfo parse fail."),

        /**
         * Socket connection closed.
         */
	    ERROR_NET_CONNECTION_CLOSED(        0x0001000A,NexErrorCategory.PROTOCOL, "Socket connection closed."),

        /**
         * Response is not arrived until timeout.
         */
	    ERROR_NET_REQUEST_TIMEOUT(        0x0001000D,NexErrorCategory.PROTOCOL, "Response is not arrived until timeout."),

        /**
         * The HTTP response data recevied from the Server is error. It's not 200 in HTTP response.
         * Ex. 500, 503 ...
         */
	    ERROR_INVALID_SERVER_STATUSCODE(        0x00020000,NexErrorCategory.PROTOCOL, "The HTTP response data recevied from the Server is error."),
		
		
        /** 
         * No track to play due to a bitrate/resolution limit.
         * E.g. The MIN_BW property is set to 2 Mbps and the playlist of playing contain has only 500 Kbps and 1 Mbps tracks.
         */
        ERROR_DISABLED_MEDIA(        0x00010010,NexErrorCategory.GENERAL, "No track to play by Bitrate/Resolution limit."),

        /**
         * Failed to download the key file to decrypt the file that has been encrypted with AES. (HLS)
         */
        ERROR_AES_KEY_RECV_FAIL(        0x00010011,NexErrorCategory.GENERAL, "Failed to download AES key file."),

        /**
         * NexPlayer met some errors when creating HTTP Downloader.
         */
        HTTPDOWNLOADER_ERROR_FAIL(0x00100000 + 1, NexErrorCategory.DOWNLOADER, "Http downloader error"),

        /**
         * The user called a part of HTTP Downloader before creating or initializing.
         */
        HTTPDOWNLOADER_ERROR_UNINIT_ERROR(0x00100000 + 2, NexErrorCategory.DOWNLOADER, "Http downloader uninitialized"),

        /**
         * A parameter from the UI is \c null or an incorrect value when HTTP downloader sends the message.
         */
        HTTPDOWNLOADER_ERROR_INVALID_PARAMETER(0x00100000 + 3, NexErrorCategory.DOWNLOADER, "Http downloader - parameter is invalid "),

        /**
         * Http downloader failed to allocate or free memory.
         */
        HTTPDOWNLOADER_ERROR_MEMORY_FAIL(0x00100000 + 4, NexErrorCategory.DOWNLOADER, "Http downloader - memory call failure"),

        /**
         * System related error.
         */
        HTTPDOWNLOADER_ERROR_SYSTEM_FAIL(0x00100000 + 5, NexErrorCategory.DOWNLOADER, "Http downloader - system call failure"),

        /**
         * @deprecated For internal use only. Please do not use.
         */
        HTTPDOWNLOADER_ERROR_WRITE_FAIL(0x00100000 + 6, NexErrorCategory.DOWNLOADER, "Http downloader - file writing failure"),

        /**
         * The user attempted to call the same method many times.
         * All duplicates(except the first one) will be regarded as an error.
         */
        HTTPDOWNLOADER_ERROR_HAS_NO_EFFEECT(0x00100000 + 7, NexErrorCategory.DOWNLOADER, "Http downloader - method has no effect"),

        /**
         * @deprecated For internal use only. Please do not use.
         */
        HTTPDOWNLOADER_ERROR_EVENT_FULL(0x00100000 + 9, NexErrorCategory.DOWNLOADER, "Http downloader - event is full"),

        /**
         * Http downloader failed to connect to a network.
         */
        HTTPDOWNLOADER_ERROR_NETWORK(0x00120000 + 0, NexErrorCategory.DOWNLOADER, "Http downloader - can't connect network"),

        /**
         * Http downloader received an invalid response from the server.
         */
        HTTPDOWNLOADER_ERROR_NETWORK_RECV_FAIL(0x00120000 + 1, NexErrorCategory.DOWNLOADER, "Http downloader - recv failure"),

        /**
         * Http downloader received an invalid response from the server.
         */
        HTTPDOWNLOADER_ERROR_NETWORK_INVALID_RESPONSE(0x00120000 + 2, NexErrorCategory.DOWNLOADER, "http downloader - The response is invalid"),

        /**
         * Http downloader detected an incorrect URL.
         */
        HTTPDOWNLOADER_ERROR_PARSE_URL(0x00120000 + 3, NexErrorCategory.DOWNLOADER, "Http downloader - url is incorrect"),

        /**
         * Http downloader requested an already downloaded content file.
         */
        HTTPDOWNLOADER_ERROR_ALREADY_DOWNLOADED(0x00130000, NexErrorCategory.DOWNLOADER, "Http downloader - file is already downloaded"),

        /**
         * NexPlayer detected an unsupported feature(i.e. recording or timeshift) or called a specific feature(i.e. call video capture in H/W mode).
         */
        UNSUPPORTED_SDK_FEATURE(0x70000001, NexErrorCategory.API, " JNI - SDK called unsupported feature" ),

        /**
         * Error while creating or initializing NexPlayer.
         */
        PLAYER_ERROR_NO_LICENSE_FILE(0x800000C0,NexErrorCategory.GENERAL, "NexPlayer initialization failed by License File"),

        /**
         * Error while creating or initializing NexPlayer.
         */
        PLAYER_ERROR_INVALID_SDK(0x8000000D,NexErrorCategory.GENERAL, "NexPlayer initialization failed by the invalid SDK"),
        /**
         * Error while creating or initializing NexPlayer.
         */
        PLAYER_ERROR_INIT(0x80000011,NexErrorCategory.GENERAL, "NexPlayer initialization failed"),

        /**
         * Error while creating or initializing NexPlayer.
         */
        PLAYER_ERROR_NOT_ACTIVATED_APP_ID(0x80000012,NexErrorCategory.GENERAL, "The current app id is not activated"),

        /**
         * The SDK has expired.
         */
        PLAYER_ERROR_TIME_LOCKED(0x800000A0, NexErrorCategory.API, "SDK has expired");

        private int mCode;

        protected String mDesc;
        private NexErrorCategory mCategory;
		private static int mUnknownSubCode =0;

		private int mSubCode;
        private String mSubDec;

        NexErrorCode() {
            mDesc = "An error occurred (error 0x " + Integer.toHexString(mCode) + ": " + this.name() + ").";
            mSubCode = 0;
            mSubDec = "";
        }

        NexErrorCode( int code, String desc ){
            mCode = code;
            mDesc = desc;
            mCategory = NexErrorCategory.GENERAL;

            mSubCode = 0;
            mSubDec = "";
        }

        NexErrorCode( int code, NexErrorCategory category, String desc ){
            mCode = code;
            mDesc = desc;
            mCategory = category;

            mSubCode = 0;
            mSubDec = "";
        }

        NexErrorCode( int code, NexErrorCategory category ){
            mCode = code;
            mDesc = "An error occurred (error 0x " + Integer.toHexString(mCode) + ": " + this.name() + ").";
            mCategory = category;

            mSubCode = 0;
            mSubDec = "";
        }

        NexErrorCode( int code ){
            mCode = code;
            mDesc = "An error occurred (error 0x " + Integer.toHexString(mCode) + ": " + this.name() + ").";
            mCategory = NexErrorCategory.GENERAL;

            mSubCode = 0;
            mSubDec = "";
        }

        void setSubErrorInfo(int subCode, String subDec) {
            mSubCode = subCode;
            mSubDec = subDec;
        }

        /**
         * Gets the integer code associated with a given error.
         *
         * @return An integer error code as provided by the NexPlayer&trade;&nbsp;engine.
         */
        public int getIntegerCode() {
            return mCode;
        }

        /**
         * Gets a description of the error suitable for display in an
         * error pop-up.
         *
         * <B>CAUTION:</B> This is experimental and is subject to change.
         * The strings returned by this method may change in future versions,
         * may not cover all possible errors, and are not currently localized.
         *
         * @return A string describing the error.
         */
        public String getDesc() {
            String Desc;
            if (mCode == NexErrorCode.UNKNOWN.getIntegerCode()) {
                Desc = mDesc + " - Error Code : 0x" + String.format("%08X", mUnknownSubCode) + ".";
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD &&
                    mCode == NexErrorCode.DRM_DECRYPT_FAILED.getIntegerCode() &&
                    !mSubDec.isEmpty()) {
                Desc = mDesc + " : " + mSubDec;
            } else {
                Desc = mDesc;
            }

            return Desc;
        }

        /**
         * Returns the category of the error.
         *
         * <B>CAUTION:</B> This is experimental and is subject to change.
         *
         * Error categories are an experimental feature.  The idea is that the
         * application can provide a friendlier (and possibly more useful) message
         * based on the category of the error.  For example, if the category is
         * <i>NETWORK</i>, the application may suggest that the user check their
         * network connection.
         *
         * This is experimental, so the set of categories may change in future
         * versions of the API, or the feature may be removed entirely.  Use it
         * with caution.
         *
         * @return The category to which the error belongs.
         */
        public NexErrorCategory getCategory() {
            return mCategory;
        }

        /**
         * Returns a NexErrorCode object for the specified error code.
         *
         * @param code
         *          The integer code to convert into a NexErrorCode object.
         * @return
         *          The corresponding NexErrorCode object or \c null if
         *          an invalid code was passed.
         */
        public static NexErrorCode fromIntegerValue( int code ) {
            for( int i=0; i<NexErrorCode.values().length; i++ ) {
                if( NexErrorCode.values()[i].mCode == code )
                    return NexErrorCode.values()[i];
            }
            NexLog.d(TAG, "Unknown Error occured internally. Error Code = 0x" + String.format("%08X%n", code));
            mUnknownSubCode = code;
            return UNKNOWN;
        }

        /**
         * Gets the integer sub error code when unknown error occurs.
         *
         * @return An integer sub error code of UNKNOWN as provided by the NexPlayer&trade;&nbsp;engine.
         */
        public static int getUnknownSubCode( ) {
            NexLog.d(TAG, "Unknown Error. Error Code = 0x" + String.format("%08X%n", mUnknownSubCode));
            return mUnknownSubCode;
        }

        /**
         * Gets the integer sub error code when unknown error occurs.
         *
         * @return An integer sub error code of UNKNOWN as provided by the NexPlayer&trade;&nbsp;engine.
         */
        public String getSubDesc( ) {
            NexLog.d(TAG, "Sub Error Code = 0x" + String.format("%08X%n", mSubCode) + " strings : " + mSubDec);
            return mSubDec;
        }
    }

    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_NONE                    = 0x0;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_AUDIO_GET_CODEC_FAILED  = 0x1;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_VIDEO_GET_CODEC_FAILED  = 0x2;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_AUDIO_INIT_FAILED       = 0x3;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_VIDEO_INIT_FAILED       = 0x4;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_TRACK_CHANGED           = 0x5;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_STREAM_CHANGED          = 0x6;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_DSI_CHANGED             = 0x7;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_OBJECT_CHANGED          = 0x8;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_CONTENT_INFO_UPDATED    = 0x9;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_AVMODE_CHANGED          = 0xa;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_HTTP_INVALID_RESPONSE   = 0xb;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_DISCONTINUITY_EXIST     = 0x12;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_EXTERNAL_DOWNLOAD_CANCELED   = 0x20;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_MINMAX_BANDWIDTH_CHANGED = 0x21;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    protected static final int NEXPLAYER_STATUS_REPORT_TARGET_BANDWIDTH_CHANGED = 0x22;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_STREAM_RECV_PAUSE   = 0x60;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_STREAM_RECV_RESUME   = 0x61;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_DOWNLOAD_PROGRESS   = 0x80;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_MAX                     = 0xFFFFFFFF;

    // for onPictureTimingInfo.
    protected static final int NEXPLAYER_DEBUGINFO_HTTP_RESPONSE = 0x06;
    protected static final int NEXPLAYER_DEBUGINFO_H264_SEI_PICTIMING_INFO = 0x09;
    protected static final int NEXPLAYER_DEBUGINFO_HTTP_REQUEST = 0x11;
    protected static final int NEXPLAYER_DEBUGINFO_SESSION_DATA = 0x13;
    protected static final int NEXPLAYER_DEBUGINFO_DATERAGNE_DATA = 0x14;    //for DATETIME Data information.
    protected static final int NEXPLAYER_DEBUGINFO_EMSG_DATA = 0x15;






    static
    {
        /*
         * Load the library. If it's already loaded, this does nothing.
         */
        if( !isStaticSDK ) {
            // For shared library SDK.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

                System.loadLibrary("nexplayerengine");
                NexLog.d(TAG, "Loading nexplayerengine.");

                System.loadLibrary("nexadaptation_layer_for_dlsdk");
                NexLog.d(TAG, "Loading nexadaptation_layer_for_dlsdk.");

                System.loadLibrary("nexalfactory");
                NexLog.d(TAG, "Loading nexalfactory.");
            } else {

                System.loadLibrary("nexadaptation_layer_for_dlsdk");
                NexLog.d(TAG, "Loading nexadaptation_layer_for_dlsdk.");

                System.loadLibrary("nexalfactory");
                NexLog.d(TAG, "Loading nexalfactory.");

                System.loadLibrary("nexplayerengine");
                NexLog.d(TAG, "Loading nexplayerengine.");
            }
        }
        else {
            // For static library SDK.
            System.loadLibrary("nexplayerengine");
            NexLog.d(TAG,"Loading nexplayerengine.");
        }
    }


    /** A possible value for the \c strRenderMode parameter of \link NexALFactory.init\endlink.  See that method description for details.
     * \since version 6.1.2 */
    public static final String NEX_DEVICE_USE_AUTO = "Auto";
    /** A possible value for the \c strRenderMode parameter of \link NexALFactory.init\endlink.  See that method description for details.*/
    public static final String NEX_DEVICE_USE_ONLY_ANDROID = "Android";
    /** A possible value for the \c strRenderMode parameter of \link NexALFactory.init\endlink.  See that method description for details.*/
    public static final String NEX_DEVICE_USE_JAVA = "JAVA";
    /** A possible value for the \c strRenderMode parameter of \link NexALFactory.init\endlink.  See that method description for details.*/
    public static final String NEX_DEVICE_USE_OPENGL = "OPENGL";
    /** A possible value for the \c strRenderMode parameter of \link NexALFactory.init\endlink.  See that method description for details.*/
    public static final String NEX_DEVICE_USE_ANDROID_3D = "Android 3D";


    /**
     * Sole constructor for NexPlayer&trade;.
     *
     * After constructing a NexPlayer&trade;&nbsp;object, you <i>must</i> call
     * NexPlayer.init before you can call any other methods
     */
    public NexPlayer( )
    {
        mNexPlayerInit = false;
    }

    /**
     * \brief Sets the NexALFactory in order to inform what type of codec NexPlayer&trade;&nbsp;will use.
     *
     * \warning  This <b>must</b> be called before NexPlayer.init methods.
     *
     * For more information, see the \link NexPlayer.init NexPlayer.init\endlink.
     *
     * @param alFactory       The \link NexALFactory NexALFactory\endlink instance.
     */
    public void setNexALFactory(NexALFactory alFactory) {

        mALFactory = alFactory;
        setNexALFactory_native();
    }

    /**
     * \brief Gets the NexALFactory set.
     *
     */
    protected NexALFactory getNexALFactory() {

        return mALFactory;
    }

    private native int setNexALFactory_native();

    /**
     * Requested by NexPlayer&trade;&nbsp;Engine. Return NexALFactoryContext.
     */
    private long getNexALFactoryContext() {
        return mALFactory.getNexALFactoryContext();
    }

    /**
     * \brief  Determines if NexPlayer&trade;&nbsp;is currently initialized.
     *
     * To initialize NexPlayer&trade;, NexPlayer.init must be called. If that
     * method returns \c true, then this method will also return
     * \c true if called on the same instance of NexPlayer&trade;.
     *
     * In some cases, it is necessary to call NexPlayer&trade;&nbsp; functions from event handlers
     * in subclasses of \c Activity (such as \c onPause or \c onStop ).
     * In such event handlers, it is possible for them to be called before code that
     * initializes NexPlayer&trade;, or for them to be called after a failed initialization.  Therefore,
     * any calls to NexPlayer&trade;&nbsp;methods made from \c onPause or similar event handlers
     * must be protected as follows:
     * \code
     * if( nexPlayer.isInitialized() )
     * {
     *     // Calls to other methods are safe here
     * }
     * \endcode
     *
     * @return \c true if NexPlayer&trade;&nbsp;is currently initialized.
     */
    public boolean isInitialized() {
        return mNexPlayerInit;
    }

    private AudioManager mAudioManager;
    private Context mContext;
    private NexNetworkUtils mNetUtil;

    /**
     * \brief This method initializes NexPlayer&trade;.
     * This must be called <b>after</b> \link NexPlayer.setNexALFactory\endlink has been called.
     * This can be done for example by using:
     * \code
     * mNexALFactory.init(this, strModel, strRenderMode, 0, colorDepth);
     * mNexPlayer.setNexALFactory(mNexALFactory);
     * mNexPlayer.init(this);
     * \endcode
     *
     * @param context       The current context; from \c Activity subclasses, you can
     *                      just pass <code>this</code>.
     * \return  NexPlayer&trade;&nbsp;error code for the generated error.
     * 
     * \since version 6.64.4.754
     */
    public NexErrorCode init(Context context) {
        NexLog.d(TAG, "Request to init player; current init status=" + mNexPlayerInit);

        NexErrorCode result = NexErrorCode.NONE;
        int iPlatform = NexSystemInfo.getPlatformInfo();

        if (mALFactory == null) {
            NexLog.d(TAG, "Init failure: NexALFactory did not set");
            return NexErrorCode.PLAYER_ERROR_INIT;
        }

        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        mContext = context.getApplicationContext();
        mClientManager = new NexClientManager(this);
        mNetUtil = new NexNetworkUtils(mContext, new NexNetworkUtils.NetworkListener() {
            @Override
            public void onDownloadComplete(String subtitlePath, int result) {
                NexLog.d(TAG, "onDownloadComplete subtitlePath : " + subtitlePath + " result : " + result);
                if( result == 0 && isInitialized() && getState() >= NEXPLAYER_STATE_STOP ) {
                    result = changeSubtitlePathInternal(subtitlePath);
                }
                if( result != 0 ) {
                    int[] intArgs = {NexPlayer.NEXPLAYER_ASYNC_CMD_SETEXTSUBTITLE, result, 0, 0};
                    NexPlayerEvent event = new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_ASYNC_CMD_COMPLETE, intArgs, new long[0], null);
                    mEventForwarder.handleEvent(NexPlayer.this, mListener, event);
                }
            }
        });
        NexPlayerEvent event = new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_WILL_INIT, new int[0], new long[0], null);
        mEventForwarder.handleEvent(this, event);

        if( !mNexPlayerInit ) {

            int nMajor = getVersion(0);
            int nMinor = getVersion(1);

            if(nMajor != NEXPLAYER_VERSION_MAJOR || nMinor != NEXPLAYER_VERSION_MINOR)
            {
                NexLog.d( TAG, "NexPlayer Version Mismatch!" );
                return NexErrorCode.PLAYER_ERROR_INVALID_SDK;
            }

            int nRet = _Constructor( new WeakReference<NexPlayer>( this ),
                    context.getApplicationContext().getPackageName(),
                    iPlatform,
                    mPortingLogLevel);

            result = NexErrorCode.fromIntegerValue(nRet);

            if( nRet == 0 ) {
                mNexPlayerInit = true;
                mALFactory.setAppUniqueCode("");

                //Set User-Agent based on WebSettings of devices(this api works on API 17 or above than
                if(Build.VERSION.SDK_INT >= 17){
                    setUserAgentFromWebSettings(context);
                }

                NexLog.d( TAG, "Init success!" );
            } else {
                NexLog.d( TAG, "Init failure: constructor result : " + nRet + " errorCode : " + result.getIntegerCode());
            }
        }

        return result;
    }

    /**
     * \brief This method sets the RTSP/HTTP User Agent value.
     *  The default user agent will have the following template:
     *
     *  User-Agent: Mozilla/5.0 (Linux; {Android Version}; {Build Tag etc.})
     *  AppleWebKit/{WebKit Rev} (KHTML, like Gecko)
     *  Chrome/{Chrome Rev} Mobile Safari/{WebKit Rev}
     *
     * @param context       The current context; from \c Activity subclasses, you can
     *                      just pass <code>this</code>.
     */
    @TargetApi(17)
    private void setUserAgentFromWebSettings(Context context)
    {
        try {
            String userAgent = WebSettings.getDefaultUserAgent(context);
            NexLog.d(TAG, "Default User-agent : " + userAgent);
            setProperty(NexProperty.USERAGENT_STRING, userAgent);
        }catch (Exception e){
            NexLog.d(TAG, "Default User-agent not found");
			e.printStackTrace();
        }
    }

    /**
     * \brief This method initializes NexPlayer&trade;.
     * This must be called <b>after</b> \link NexPlayer.setNexALFactory\endlink has been called.
     * This can be done for example by using:
     * \code
     * mNexALFactory.init(this, strModel, strRenderMode, 0, colorDepth);
     * mNexPlayer.setNexALFactory(mNexALFactory);
     * mNexPlayer.init(this, 0)
     * \endcode
     *
     * @param context       The current context; from \c Activity subclasses, you can
     *                      just pass <code>this</code>.
     * @param logLevel      NexPlayer&trade;&nbsp;SDK logging level.  This affects the messages that the SDK writes to the
     *                      Android log.
     *                          - <b>-1</b> : Do not output any log messages.
     *                          - <b>0</b> : Output basic log messages only (recommended).
     *                          - <b>1~4</b> : Output detailed log messages; higher numbers result in more verbose
     *                                      log entries, but may cause performance issues in some cases and are
     *                                      not recommended for general release code.
     * @return              \c NexErrorCode.NONE if initialization succeeded;
     *                      \c NexPlayer&trade;&nbsp;error code for the generated error in the case of a failure (in the case of failure, check the log for details).
     *
     * \since version 6.65.5.789
     */
    public NexErrorCode init( Context context, int logLevel) {
        mPortingLogLevel = logLevel;
        return init(context);
    }

    /**
     * \brief  Registers a callback that will be invoked when new events occur.
     *
     * The events dispatched to this callback interface serve three functions:
     *  - to provide new video and audio data to the application,
     *      for the application to present to the user.
     *  - to notify the application when a command has completed,
     *      so that the application can issue any follow-up
     *      commands.  For example, issuing {@link NexPlayer#start(int) start}
     *      when {@link NexPlayer#open open} has completed.
     *  - to notify the application when there are state changes that the
     *      application may wish to reflect in the interface.
     *
     * All applications <i>must</i> implement
     * this callback and provide certain minimal functionality. See the
     * \link NexPlayer.IListener IListener\endlink documentation for a list of
     * events and information on implementing them.
     *
     * In an Android application, there are two common idioms for implementing
     * this.  The most typical is to have the \c Activity subclass
     * implement the \c IListener interface.
     *
     * The other approach is to define an anonymous class in-line:
     * \code
     * mNexPlayer.setListener(new NexPlayer.IListener() {
     *
     *     &#064;Override
     *     public void onVideoRenderRender(NexPlayer mp) {
     *         // ...event implementation goes here...
     *     }
     *
     *     // ...other methods defined by the interface go here...
     * });
     * \endcode
     *
     * \param listener
     *            The object on which methods will be called when new events occur.
     *            This must implement the \c IListener interface.
     */
    public void setListener( IListener listener )
    {
        if( !mNexPlayerInit )
        {
            NexLog.d(TAG, "Attempt to call setListener() but player not initialized; call NexPlayer.init() first!");
        }
        mListener = listener;
    }

    /**
     * \brief This method adds an \c event receiver.
     *
     * \note  If the developer wants to register several IListeners, they can register a receiver with this method.
     *		  If the developer set a listener with \link NexPlayer.setListener\endlink and added event receivers,
     *		  NexPlayer will use the return value of the listener.
     *        If the developer did not set a listener and added only receivers,
     *        NexPlayer will use the return value of the last registered event receiver.
     *        Events will be forwarded in sequence from receivers to listener.
     *
     * \param receiver
     *            The object to which methods will be called when events occur.
     *
     * \see NexEventReceiver
     * \see NexPlayer.removeReleaseListener
     *
     * \since version 6.51
     */
    public boolean addEventReceiver(NexEventReceiver receiver) {
        NexLog.d(TAG, "add addEventReceiver");
        return mEventForwarder.addReceiver(receiver);
    }

    /**
     *
     * \brief  This removes a receiver which was added with \c addEventReceiver.
     *
     * \param receiver
     *            The object to which methods will be called when events occur.
     *
     * \see NexEventReceiver
     * \see NexPlayer.addReleaseListener
     *
     * \since version 6.51
     *
     */
    public boolean removeEventReceiver(NexEventReceiver receiver) {
        NexLog.d(TAG, "remove removeEventReceiver");
        return mEventForwarder.removeReceiver(receiver);
    }

    protected NexEventProxy getEventProxy() {
        return mEventForwarder.getEventProxy();
    }

    protected NexEventForwarder getEventForwarder() {
        return mEventForwarder;
    }

    /**
     * \brief  Registers a callback that will be invoked when new video renderer events occur.
     *
     * \note  All applications <i>must</i> implement
     * this callback and provide certain minimal functionality. See the
     * \link NexPlayer.IVideoRendererListener IVideoRendererListener\endlink documentation for a list of
     * events and information on implementing them.
     *
     * In an Android application, there are two common idioms for implementing
     * this.  The most typical is to have the \c Activity subclass
     * implement the \c IVideoRendererListener interface.
     *
     * The other approach is to define an anonymous class in-line:
     * \code
     * mNexPlayer.setVideoRendererListener(new NexPlayer.IVideoRendererListener() {
     *
     *     &#064;Override
     *     public void onVideoRenderRender(NexPlayer mp) {
     *         // ...event implementaton goes here...
     *     }
     *
     *     // ...other methods defined by the interface go here...
     * });
     * \endcode
     *
     * \param listener
     *            The object on which methods will be called when new events occur.
     *            This must implement the \c IVideoRendererListener interface.
     *
     * \see renderers The introductory section on renderers in the NexPlayer&trade;&nbsp;SDK.
     * \see NexVideoRenderer
     * \see NexPlayer.IVideoRendererListener
     *
     * \since version 6.1
     *
     */
    public void setVideoRendererListener( IVideoRendererListener listener )
    {
        if( !mNexPlayerInit )
        {
            NexLog.d(TAG, "Attempt to call setVideoRendererListener() but player not initialized; call NexPlayer.init() first!");
        }
        mVideoRendererListener = listener;
    }


    /**
     * \brief  Adds a callback that will be invoked when a NexPlayer&trade;&nbsp;instance is released.
     *
     * This is a callback that provides certain minimal functionality. See the
     * \link NexPlayer.IReleaseListener\endlink documentation for more
     * information on implementation.
     *
     * In an Android application, there are two common ways to implement
     * this.  The most typical approach is to have the \c Activity subclass
     * implement the \c IReleaseListener interface.
     *
     * The other approach is to define an anonymous class inline, as follows:
     * \code
     * mNexPlayer.addReleaseListener(new NexPlayer.IReleaseListener() {
     *
     *     &#064;Override
     *     public void onPlayerRelease(NexPlayer mp) {
     *         // ...event implementaton goes here...
     *     }
     * });
     * \endcode
     *
     * \param listener
     *            The object on which methods will be called when the instance of NexPlayer&trade;&nbsp;is released.
     *            This must implement the \c IReleaseListener interface.
     *
     * \see NexPlayer.IReleaseListener
     * \see NexPlayer.removeReleaseListener
     *
     * \since version 6.23
     *
     */
    public void addReleaseListener( IReleaseListener listener )
    {
        if( !mNexPlayerInit )
        {
            NexLog.d(TAG, "Attempt to call setNexPlayerReleaseListener() but player not initialized; call NexPlayer.init() first!");
        }
        if(listener != null) {
            NexLog.d(TAG, "add addReleaseListener");
            mEventForwarder.addReceiver(listener);
        }
    }

    /**
     * \brief  Removes a callback listener which was added with \c addReleaseListener.
     *
     * \param listener
     *            The object on which methods will be called when the instance of NexPlayer&trade;&nbsp;is released.
     *
     * \see NexPlayer.IReleaseListener
     * \see NexPlayer.addReleaseListener
     *
     * \since version 6.23
     *
     */
    public void removeReleaseListener( IReleaseListener listener )
    {
        if( !mNexPlayerInit )
        {
            NexLog.d(TAG, "Attempt to call setNexPlayerReleaseListener() but player not initialized; call NexPlayer.init() first!");
        }
        NexLog.d(TAG, "remove removeReleaseListener");
        mEventForwarder.removeReceiver(listener);
    }

    /**
     *
     * \brief This method begins opening the media at the specified path or URL.  This supports both
     *        local content and streaming content.
     *
     * When the stored info file is created by using NexOfflineStoreController and then passed to the path parameter, this method opens content for offline playback.
     *
     * This is an asynchronous operation that
     * will run in the background (even for local content).
     *
     * When this operation completes,
     * \link NexPlayer.IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete\endlink
     * is called with one of the following command constants (depending on the \c type
     * specified in the \c open call):
     * - \link NexPlayer#NEXPLAYER_ASYNC_CMD_OPEN_LOCAL NEXPLAYER_ASYNC_CMD_OPEN_LOCAL\endlink
     * - \link NexPlayer#NEXPLAYER_ASYNC_CMD_OPEN_STREAMING NEXPLAYER_ASYNC_CMD_OPEN_STREAMING\endlink
     * - \link NexPlayer#NEXPLAYER_ASYNC_CMD_OPEN_STORE_STREAM NEXPLAYER_ASYNC_CMD_OPEN_STORE_STREAM\endlink
     *
     * Success or failure of the operation can be determined by checking the \c result
     * argument passed to \c onAsyncCmdComplete.  If the result is 0, the media was
     * successfully opened; if it is any other value, the operation failed.
     *
     * Calls to \c open must be matched with calls to NexPlayer.close .
     *
     * @param path
     *          The location of the content: a path (for local content) or URL (for remote content).
     * @param smiPath
     *          The path to a local subtitle file, the URL to load a subtitle file, or \c null for no subtitles.  
     *          For streaming content that already includes subtitles, this should be \c null (using both types of subtitles
     *          at the same time will cause undefined behavior).
     * @param externalPDPath
     *          When not \c null, the external path used to play PD content downloaded by the Downloader module.
     *          This is only available for content in MP4 containers.
     * @param type
     *          This determines how the path argument is interpreted.  This will be one of:
     *                - \link NexPlayer#NEXPLAYER_SOURCE_TYPE_LOCAL_NORMAL NEXPLAYER_SOURCE_TYPE_LOCAL_NORMAL\endlink
     *                  to play local media (the path is a local file system path)
     *                - \link NexPlayer#NEXPLAYER_SOURCE_TYPE_STREAMING NEXPLAYER_SOURCE_TYPE_STREAMING\endlink
     *                  to play remote media sources (including RTSP streaming,
     *                  progressive download and HTTP Live streaming).  The path is
     *                  interpreted as a URL.
     *                - \link NexPlayer#NEXPLAYER_SOURCE_TYPE_STORE_STREAM NEXPLAYER_SOURCE_TYPE_STORE_STREAM\endlink
     *                  to store remote media content (only currently available with HTTP Live streaming (HLS) content and not supported for live content) for later offline playback.\n
     *
     *          Other \c NEXPLAYER_SOURCE_* values are not
     *          supported in this version and should not be used.
     * @param transportType
     *          The network transport type to use on the connection.  This should be one of:
     *                - \link NexPlayer#NEXPLAYER_TRANSPORT_TYPE_TCP  NEXPLAYER_TRANSPORT_TYPE_TCP\endlink
     *                - \link NexPlayer#NEXPLAYER_TRANSPORT_TYPE_UDP  NEXPLAYER_TRANSPORT_TYPE_UDP\endlink
     *
     *
     *
     * @return The status of the operation: this is zero in the case of success, or
     *          a non-zero NexPlayer&trade;&nbsp;error code in the case of failure.
     *
     *          \note This only indicates the success or failure of <i>starting</i> the operation.
     *          Even if this reports success, the operation may still fail later,
     *          asynchronously, in which case the application is notified in
     *          \c onAsyncCmdComplete.
     */
    public int open(String path, String smiPath, String externalPDPath, int type, int transportType)
    {
        if( mListener == null && !mEventForwarder.hasInterface(IListener.class) )
        {
            NexLog.e(TAG, "You should register listener before opening NexPlayer.");
            return 1;
        }
        else if(path == null)
        {
            NexLog.e(TAG, "You should input Content Path to open the content");
            return NexErrorCode.INVALID_PARAMETER.getIntegerCode();
        }
        else
        {
            if( NexNetworkUtils.isHttpURL(smiPath) ) {
                mNetUtil.startDownload(smiPath);
                smiPath = null;
            }

            int ret = NexErrorCode.HAS_NO_EFFECT.getIntegerCode();

            mCurrentPath = path;
            mSourceType = type;
            mTransportType = transportType;

            if( type == NEXPLAYER_SOURCE_TYPE_LOCAL_NORMAL ) {
                File file = new File(path);
                mStoreInfo = NexStoredInfoFileUtils.parseJSONObject(file);
                if( mStoreInfo != null ) {
                    NexSettingDataForStoring settings = new NexSettingDataForStoring(file);
                    if( settings.storePercentage > 0 ) {
                        ret = setupPlayerBeforeOpen(mContext, this, settings);

                        if( ret == NexPlayer.NexErrorCode.NONE.getIntegerCode() ) {
                            ret = openInternal(settings.storeURL, smiPath, externalPDPath, NexPlayer.NEXPLAYER_SOURCE_TYPE_STREAMING, NexPlayer.NEXPLAYER_TRANSPORT_TYPE_UDP, 0);
                        }
                    }
                }
            }

            if( ret == NexErrorCode.HAS_NO_EFFECT.getIntegerCode() ) {
                if( type == NEXPLAYER_SOURCE_TYPE_STREAMING ) {
                    NexClientManager.OpenParams opemParams = mClientManager.new OpenParams(mContext, path, smiPath, externalPDPath, type, transportType, 0);
                    NexPlayerEvent event = new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_WILL_OPEN, new int[0], new long[0], opemParams);
                    mEventForwarder.handleEvent(this, event);
                }

                ret = openInternal(path, smiPath, externalPDPath, type, transportType, 0);

                if( ret != 0 ) {
                    mClientManager.onError( NexErrorCode.fromIntegerValue(ret) );
                }
            }

            return ret;
        }
    }

    /**
     * @deprecated This API is deprecated. Please use {@link NexPlayer#open(String, String, String, int, int)} instead.
     * Use \c INITIAL_BUFFERING_DURATION and \c RE_BUFFERING_DURATION of \c NexProperty instead of the parameter \c bufferingTime. 
     */
    public int open(String path, String smiPath, String externalPDPath, int type, int transportType, int bufferingTime)
    {
        setProperty(NexProperty.INITIAL_BUFFERING_DURATION, bufferingTime);
        setProperty(NexProperty.RE_BUFFERING_DURATION, bufferingTime);
        return open(path, smiPath, externalPDPath, type, transportType);
    }

    public Boolean getDRMEnable() {
        return mDrmEnabled;
    }

    protected native int openInternal( String path, String smiPath, String externalPDPath, int type, int transportType, int bufferingTime );

    protected static native int initStoreManagerMulti(Object nexPlayerHandle, String cacheFolder);
    protected static native int initRetrieveManagerMulti(Object nexPlayerHandle, String cacheFolder);
    protected static native int initUpdateManagerMulti(Object nexPlayerHandle, String cacheFolder);
    protected static native int deinitStoreManagerMulti(Object nexPlayerHandle);
    protected static native int deinitRetrieveManagerMulti(Object nexPlayerHandle);
    /**
     * \brief Open the media resource file which is attached in "res/raw" or "assets" folder
     * Access should be provided by AssetFileDescriptor. Length and Offset will be 
     * calculated internally.
     *
     * @param afd Asset file descriptor.
     * @return The status of the operation: this is zero in the case of success, or
     * a non-zero NexPlayer&trade;&nbsp;error code in the case of failure.
     *
     * @warning When building the APK, most of resources will be compressed. This makes it difficult to extract FileDescriptor.
     * To avoid compressing, you MUST change your file's extension. Below extensions are allowed:
     * ".jpg", ".jpeg", ".png", ".gif", ".wav", ".mp2", ".mp3", ".ogg", ".aac", ".mpg", ".mpeg", ".mid", ".midi", ".smf", ".jet",
     * ".rtttl", ".imy", ".xmf", ".mp4", ".m4a", ".m4v", ".3gp", ".3gpp", ".3g2", ".3gpp2", ".amr", ".awb", ".wma", ".wmv"
     *
     * @since version 6.45
     */
    public int openFD(AssetFileDescriptor afd)
    {
        return openFDInternal(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
    }

    /**
     * \brief Open the resource files which are attached in "res/raw" or "assets" folder.
     * Length and Offset can be acquired from the UI.
     *
     * When the stored info file is created by using NexOfflineStoreController and then passed to the path parameter, this method opens content for offline playback.
     *
     * @param fd        File descriptor.
     * @param offset    Offset.
     * @param length    Length.
     * @return          The status of the operation: this is zero in the case of success, or
     *                  a non-zero NexPlayer&trade;&nbsp;error code in the case of failure.
     *
     * @warning When building the APK, most of resources will be compressed. This makes it difficult to extract FileDescriptor.
     * To avoid compressing, you MUST change your file's extension. Below extensions are allowed:
     * ".jpg", ".jpeg", ".png", ".gif", ".wav", ".mp2", ".mp3", ".ogg", ".aac", ".mpg", ".mpeg", ".mid", ".midi", ".smf", ".jet",
     * ".rtttl", ".imy", ".xmf", ".mp4", ".m4a", ".m4v", ".3gp", ".3gpp", ".3g2", ".3gpp2", ".amr", ".awb", ".wma", ".wmv"
     *
     * since version 6.45
     */
    public int openFD(FileDescriptor fd, long offset, long length)
    {
        int ret = NexErrorCode.HAS_NO_EFFECT.getIntegerCode();
        if(fd == null)
        {
            NexLog.e(TAG, "You should input Content Path to open the content");
            return NexErrorCode.INVALID_PARAMETER.getIntegerCode();
        }
        JSONObject obj = NexStoredInfoFileUtils.parseJSONObject(fd);
        if( obj != null ) {
            NexSettingDataForStoring settings = new NexSettingDataForStoring(fd);
            if( settings.storePercentage > 0 ) {
                ret = setupPlayerBeforeOpen(mContext, this, settings);

                if( ret == NexPlayer.NexErrorCode.NONE.getIntegerCode() ) {
                    ret = open(settings.storeURL, null, null, NexPlayer.NEXPLAYER_SOURCE_TYPE_STREAMING, NexPlayer.NEXPLAYER_TRANSPORT_TYPE_UDP);
                }
            }
        }

        if( ret == NexErrorCode.HAS_NO_EFFECT.getIntegerCode() ) {
            ret = openFDInternal(fd, offset, length);
        }

        return ret;
    }
    protected native int openFDInternal(FileDescriptor fd, long offset, long length);

    public void setAESKeyCallback(int aesCallbackPtr)
    {
        setAESKeyCallbackInternal(aesCallbackPtr);
    }
    protected native void setAESKeyCallbackInternal(int aesCallbackPtr);

    private int setupPlayerBeforeOpen(Context context, NexPlayer player, NexSettingDataForStoring settings) {
        int ret = NexPlayer.NexErrorCode.NONE.getIntegerCode();
        int[] bitrate = new int[1];
        bitrate[0] = settings.bandwidth;
        player.setVideoBitrates(bitrate);

        if( !TextUtils.isEmpty(settings.preferLanguageAudio) )
            player.setProperty(NexPlayer.NexProperty.PREFER_LANGUAGE_AUDIO, settings.preferLanguageAudio);

        if( !TextUtils.isEmpty(settings.preferLanguageText) )
            player.setProperty(NexPlayer.NexProperty.PREFER_LANGUAGE_TEXT, settings.preferLanguageText);


        //Widevine start
        if( settings.drmType != 0 ) {
            player.setProperties(NEXPLAYER_PROPERTY_ENABLE_MEDIA_DRM, settings.drmType);
        }
        //Widevine end

        if( !TextUtils.isEmpty(settings.mediaDrmKeyServer) ) {
            // NexMediaDrm Start
            if(1 == (1 & settings.drmType)) {
                player.setNexMediaDrmKeyServerUri(settings.mediaDrmKeyServer);
            }
            // NexMediaDrm end

            //NexWVSWDrm start
            if(2 == (2 & settings.drmType)) {
                mNexWVDRM = new NexWVDRM();
                File fileDir = mContext.getFilesDir();
                String strCertPath = fileDir.getAbsolutePath() + "/wvcert";

                File certDirectory = new File(strCertPath);
                certDirectory.mkdir();

                NexLog.d(TAG, "SWDRM: Proxy server addr is.. ( " + settings.mediaDrmKeyServer + " )");
                int offlineMode = 2;

                if (mNexWVDRM.initDRMManager(getDefaultEngineLibPath(context), strCertPath, settings.mediaDrmKeyServer, offlineMode) == 0) {
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
                            boolean bFound = false;
                            while (true) {
                                end = strAttr.indexOf("\n");
                                if (end != -1 && end != 0) {
                                    strKeyElem = strAttr.substring(0, end);
                                    keyAttrArray.add(strKeyElem);
                                    strKeyRemain = strAttr.substring(end, strAttr.length());
                                    strAttr = strKeyRemain;
                                } else if ((end == -1 || end == 0) && !TextUtils.isEmpty(strKeyElem)) {
                                    keyAttrArray.add(strAttr.substring(0, strAttr.length()));
                                    break;
                                } else {
                                    keyAttrArray.add(strAttr);
                                    break;
                                }
                            }

                            for (int i = 0; i < keyAttrArray.size(); i++) {
                                strKeyElem = keyAttrArray.get(i);
                                if (strKeyElem.contains("com.widevine")) {
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
        }

        // NexMediaDrm Start
        if (offlineExpiredKeyFetch) {
            initUpdateManagerMulti(player, settings.storePath);
            return ret;
        }
        // NexMediaDrm end

        initRetrieveManagerMulti(player, settings.storePath);

        return ret;
    }

    /**
     * \brief Starts playing media from the specified timestamp.
     *
     * The media must have already been successfully opened with
     * \link NexPlayer#open open\endlink.  This only works
     * for media that is in the stopped state; to change the play
     * position of media that is currently playing or paused, call
     * \link NexPlayer#seek(int) seek\endlink instead.
     *
     * When this operation completes,
     * \link NexPlayer.IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete\endlink
     * is called with one of the following command constants (depending on the \c type
     * specified in the \c open call):
     *  - \link NexPlayer#NEXPLAYER_ASYNC_CMD_START_LOCAL  NEXPLAYER_ASYNC_CMD_START_LOCAL\endlink
     *  - \link NexPlayer#NEXPLAYER_ASYNC_CMD_START_STREAMING  NEXPLAYER_ASYNC_CMD_START_STREAMING\endlink
     *  - \link NexPlayer#NEXPLAYER_ASYNC_CMD_START_STORE_STREAM  NEXPLAYER_ASYNC_CMD_START_STORE_STREAM\endlink
     *
     * Success or failure of the operation can be determined by checking the \c result
     * argument passed to \c onAsyncCmdComplete.  If the result is 0, the media was
     * successfully opened; if it is any other value, the operation failed.
     *
     * \param msec
     *          The offset (in milliseconds) from the beginning of the media
     *          at which to start playback.  This should be zero to start at the beginning.
     *          This parameter will be ignored if content is opened with the \c NEXPLAYER_SOURCE_TYPE_STORE_STREAM parameter since the content will be stored instead of played.
     *          This parameter must be set to a valid value within the seekable range.
     *          The value of the seekable range can be obtained using the getSeekableRangeInfo () API.
     *          If the content is a live stream, it is recommended to use the LIVE_VIEW_OPTION property.
     *
     * \return  The status of the operation.  This is zero in the case of success, or
     *          a non-zero NexPlayer&trade;&nbsp;error code in the case of failure.
     *
     *          \note This only indicates the success or failure of <i>starting</i> the operation.
     *          Even if this reports success, the operation may still fail later,
     *          asynchronously, in which case the application is notified in
     *          \c onAsyncCmdComplete.
     */
    public int start( int msec )
    {
        NexPlayerEvent event = new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_WILL_START, new int[0], new long[0], null);
        mEventForwarder.handleEvent(this, event);

        int ret = start(msec, false);

        if( ret != 0 ) {
            mClientManager.onError( NexErrorCode.fromIntegerValue(ret) );
        }
        return ret;
    }

    /**
     * \brief Starts playing media from the specified timestamp.
     *
     * The media must have already been successfully opened with
     * \link NexPlayer#open open\endlink.  This only works
     * for media that is in the stopped state; to change the play
     * position of media that is currently playing or paused, call
     * \link NexPlayer#seek(int) seek\endlink instead.
     *
     * When this operation completes,
     * \link NexPlayer.IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete\endlink
     * is called with one of the following command constants (depending on the \c type
     * specified in the \c open call):
     *  - \link NexPlayer#NEXPLAYER_ASYNC_CMD_START_LOCAL  NEXPLAYER_ASYNC_CMD_START_LOCAL\endlink
     *  - \link NexPlayer#NEXPLAYER_ASYNC_CMD_START_STREAMING  NEXPLAYER_ASYNC_CMD_START_STREAMING\endlink
     *  - \link NexPlayer#NEXPLAYER_ASYNC_CMD_START_STORE_STREAM  NEXPLAYER_ASYNC_CMD_START_STORE_STREAM\endlink 
     *
     * Success or failure of the operation can be determined by checking the \c result
     * argument passed to \c onAsyncCmdComplete.  If the result is 0, the media was
     * successfully opened; if it is any other value, the operation failed.
     *
     * \param msec
     *          The offset (in milliseconds) from the beginning of the media
     *          at which to start playback.  This should be zero to start at the beginning.
     *          This parameter will be ignored if content is opened with \c NEXPLAYER_SOURCE_TYPE_STORE_STREAM since the content will be stored instead of played.
     *          This parameter must be set to a valid value within the seekable range.
     *          The value of the seekable range can be obtained using the getSeekableRangeInfo () API.
     *          If the content is a live stream, it is recommended to use the LIVE_VIEW_OPTION property.
     * \param pauseAfterReady
     *          If this value is true, NexPlayer&trade;&nbsp;will pause just after buffering and before play
     *          This parameter will be ignored if content is opened with the \c NEXPLAYER_SOURCE_TYPE_STORE_STREAM parameter.
     *
     *          Note that if this value is \c TRUE, NexPlayer's state will be changed to \link NexPlayer#NEXPLAYER_STATE_PAUSE NEXPLAYER_STATE_PAUSE\endlink when initial buffering is done. This could take some time.
     *
     * \return  The status of the operation.  This is zero in the case of success, or
     *          a non-zero NexPlayer&trade;&nbsp;error code in the case of failure.
     *
     *          \note This only indicates the success or failure of <i>starting</i> the operation.
     *          Even if this reports success, the operation may still fail later,
     *          asynchronously, in which case the application is notified in
     *          \c onAsyncCmdComplete.
     */
    public native int start( int msec, boolean pauseAfterReady  );

    /**
     * \brief This method pauses the current playback.
     *
     * Please note that when the hardware codec is in use, if the application is sent to the background by pressing the home button, 
     * pausing and resuming playback may return an error because of resource limitations. 
     * To avoid this potential issue, stopping and starting playback is recommended in algorithm handling this specific case of the 
     * application being sent to the background because the home button was pressed. 
     *
     * \return  Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
     */
    public int pause() {
        NexPlayerEvent event = new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_WILL_PAUSE, new int[0], new long[0], null);
        mEventForwarder.handleEvent(this, event);
        return pauseInternal();
    }
    private native int pauseInternal();

    /**
     * \brief This method resumes playback beginning at the point at which the player
     *        was last paused.
     *
     * \return  Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
     */
    public int resume() {
        NexPlayerEvent event = new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_WILL_RESUME, new int[0], new long[0], null);
        mEventForwarder.handleEvent(this, event);
        return resumeInternal();
    }
    private native int resumeInternal();

    /**
     * \brief This method seeks the playback position to the specified time.
     *
     * This doesn't work if NexPlayer&trade;&nbsp;is stopped or if the stream
     * doesn't support seeking, but does work if NexPlayer&trade;&nbsp;is playing or paused.
     *
     * Note that even if the parameter \c exact is \c TRUE, it is possible to minimize seek time
     * by adjusting the NexProperty SEEK_RANGE_FROM_RA_POINT.
     *
     * Note that NexPlayer's state will be changed to pause and resume automatically if this method is called during playback.
     * Therefore, if getState is calling while NexPlayer is seeking, the UI can get \link NexPlayer#NEXPLAYER_STATE_PAUSE NEXPLAYER_STATE_PAUSE\endlink .
     *
     * \param msec
     *          The offset in \c msec (milliseconds) from the beginning of the media to which the playback position should
     *          seek.
     * \param exact
     *          If \c exact is \c true, the player will seek exactly to the time specified by \c msec (milliseconds).  Otherwise,
     *          the playhead will seek to the nearest approximate position for faster seeking performance.
     *
     * \return  Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
     *
     * \see NexProperty.SEEK_RANGE_FROM_RA_POINT
     */
    public native int seek( int msec, boolean exact );

    /**
     * \brief This function seeks the playback position exactly to a specific time.
     *
     * This doesn't work if NexPlayer&trade;&nbsp;is stopped or if the stream
     * doesn't support seeking, but does work if NexPlayer&trade;&nbsp;is playing or paused.
     *
     * This method behaves in the same way as the method \link seek(int msec, boolean exact) \endlink
     * with the second parameter, \c exact, set to \c true.  In other words, NexPlayer&trade;&nbsp;will seek
     * exactly to the time specified by \c msec (milliseconds).
     *
     * \param msec
     *          The offset in milliseconds from the beginning of the media to which the playback position should
     *          seek.
     * \return  Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
     *
     * \see NexPlayer.seek(int msec, boolean exact)
     */
    public int seek( int msec)
    {
        int intArgs[] = { msec };
        NexPlayerEvent event = new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_WILL_SEEK, intArgs, new long[0], null);
        mEventForwarder.handleEvent(this, event);

        return seek(msec,true);
    }

    /**
     * \brief This method moves to the current live position after the actual playback position. 
     *
     * Normally, when playing live content, previously recorded data (for example, a few seconds earlier than the actual live position) is played to avoid buffering. 
     * This method however ignores this concept and moves directly to the latest loaded playback position (where the server is currently being encoded).
     *
     * This method behaves in the same way as the method \link gotoCurrentLivePosition(boolean exact) \endlink
     * with the parameter, \c exact, set to \c true.  In other words, NexPlayer&trade;&nbsp;will seek
     * exactly to the time specified by \c msec (milliseconds).
     *
     * \return Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
     *
     * \since version 6.33
     */
    public int gotoCurrentLivePosition()
    {

        return gotoCurrentLivePosition(true);
    }

    /**
     * \brief This method moves to the current live position after the actual playback position. 
     *
     * Normally, when playing live content, previously recorded data (for example, a few seconds earlier than the actual live position) to avoid buffering. 
     * This method however ignores this concept and moves directly to the latest loaded playback position (where the server is currently being encoded).
     *
     * \param exact  If \c exact is \c true, the player will seek exactly to the time specified by \c msec (milliseconds). Otherwise,
     *          the playhead will seek to the nearest approximate position for faster seeking performance.
     *
     * \return Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
     *
     * \since version 6.33
     */
    public native int gotoCurrentLivePosition(boolean exact);


    /**
     * \brief This method allows NexPlayer&trade;&nbsp;to reconnect to the media server in the case of streaming content.
     *
     * It allows NexPlayer&trade;&nbsp;to reconnect to a media server when network conditions may have
     * closed a connection.
     *
     * \warning This is <b>only</b> supported in HLS, DASH, SmoothStreaming and PD streaming content.
     *
     * \return Zero if successful or a non-zero error code.
     *
     * \since version 6.0.5
     */
    public native int reconnectNetwork();


    /**
     * \brief This method enables the disabled tracks due to temporary content and performance issues.
     *
     * It enables the disabled tracks due to temporary content issues (download fail (ex. 404, 502 error), playlist or response data from the server parsing failing )
     * and performance issues (decoding & rendering performances).
     *
     * \warning This is <b>only</b> supported in HLS and DASH contents.
     *
     * \param enableoption
     *          This can be used by bitwise or operation. This should be one of:
     *                - \link NexPlayer#NEXPLAYER_TRACK_ENABLE_OPTION_DISABLED_TEMPORARY  NEXPLAYER_TRACK_ENABLE_OPTION_DISABLED_TEMPORARY\endlink
     *                - \link NexPlayer#NEXPLAYER_TRACK_ENABLE_OPTION_DISABLED_PERFORMANCE  NEXPLAYER_TRACK_ENABLE_OPTION_DISABLED_PERFORMANCE\endlink
     * \return Zero if successful or a non-zero error code.
     *
     * \since version 6.56.0
     */
    public native int enableTrack(int enableoption);

    /**
     *  \brief  This method activates the \c fastPlay feature in HLS content.
     *
     *  \warning  This method can be used in HLS content only.
     *
     *  The \c fastPlay feature allows NexPlayer&trade;&nbsp;to play HLS content at a speed other than normal playback speed.
     *  When \c fastPlay is activated, content is played more quickly than normal and there is no audio (similar to a fast forward feature).
     *
     *  The player can also rewind quickly through HLS content using the \c fastPlay feature by setting the \c rate parameter to a
     *  negative value.
     *
     *  To change the speed or direction of the \c fastPlay feature, simply call the \link NexPlayer#fastPlaySetPlaybackRate fastPlaySetPlaybackRate\endlink
     *  method and change the \c rate parameter to the desired value.
     *
     *  \param msec		The time in the content at which to start \c fastPlay (in \c msec (milliseconds)).
     *  \param rate		The speed at which video will play in \c fastPlay mode.
     *  				This speed is indicated by any \c float value (but NOT zero), where
     *  				negative values rewind the video at faster than normal playback speed and positive
     *  				values play the video faster than normal (like fast forward).
     *  				For example:
     *  					- \c rate = 3.0 (\c fastPlay plays video at 3x normal speed)
     *  					- \c rate = - 2.0 ( \c fastPlay rewinds video at 2x normal speed)
     *
     *  \return  Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
     *
     *  \since version 5.12
     */
    public native int fastPlayStart(int msec, float rate);

    /**
     *  \brief  This function sets the video playback rate for the \c fastPlay feature.
     *
     *  HLS video content will be played at the speed set by the playback rate when the \c fastPlay feature
     *  is activated by calling \c fastPlayStart.  This rate can be set to any \c float value (excluding zero),
     *  where positive values will play content back at a faster speed and negative values will rewind content
     *  at the set rate faster than normal playback speed.
     *
     *  If \c rate is set to zero, this method will return an error.
     *
     *  \param rate		The speed at which video will play in \c fastPlay mode.
     *  				This speed is indicated by any \c float value (but NOT zero), where
     *  				negative values rewind the video at faster than normal playback speed and positive
     *  				values play the video faster than normal (like fast forward).
     *  				For example:
     *  					- rate = 3.0 (\c fastPlay plays video at 3x normal speed)
     *  					- rate = - 2.0 ( \c fastPlay rewinds video at 2x normal speed)
     *
     *  \return  Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
     *
     *  \since version 5.12
     *
     */
    public native int fastPlaySetPlaybackRate( float rate);


    /**
     * \brief  This function turns off the \c fastPlay feature in HLS content.
     *
     * Once the \c fastPlay feature has been activated by calling \link NexPlayer#fastPlayStart fastPlayStart\endlink, this method must be
     * called in order to stop \c fastPlay.
     *
     * In order to reactivate the \c fastPlay feature after calling \link NexPlayer#fastPlayStop fastPlayStop\endlink,
     * simply call the \link NexPlayer#fastPlayStart fastPlayStart\endlink method again.  If fastPlayStop is called when \c fastPlay is not
     * activated, an error will be returned.
     *
     * \param bResume	This boolean value sets whether to resume playback after \c fastPlay or not.
     * 					If \c bResume = 1, video will automatically resume playback when \c fastPlay stops.
     * 					If \c bResume = 0, when \c fastPlay stops, the content in NexPlayer&trade;&nbsp;will
     * 					be paused.
     *
     * \return Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
     *
     *  \since version 5.12
     */
    public int fastPlayStop(boolean bResume)
    {
        return fastPlayStop(bResume?1:0);
    }


    private native int fastPlayStop(int bResume);


    /**
     * \brief This function stops the current playback.
     *
     * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
     */
    public int stop() {
        NexPlayerEvent event = new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_WILL_STOP, new int[0], new long[0], null);
        mEventForwarder.handleEvent(this, event);
        return stopInternal();
    }

    private native int stopInternal();
    /**
     * \brief This method ends all the work on the content currently open and
     * closes content data.  The content must be stopped <i>before</i> calling
     * this method.
     *
     * The correct way to finish playing content is to either wait for the
     * end of content, or to call \c stop and wait for the stop
     * operation to complete, then call \c close.
     *
     * \warning However, do not call \c close in IListener's event handlers as this may give rise to a deadlock.
     * A safe way to call \c close is to use the Android UI main thread's message handler.
     *
     * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
     */
    public int close() {
        NexLog.d("NexPlayer", "NexPlayer.close() called.");

        NexPlayerEvent event = new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_WILL_CLOSE, new int[0], new long[0], null);
        mEventForwarder.handleEvent(this, event);

        NexNetworkUtils.STATE state = mNetUtil.getState();
        if( state == NexNetworkUtils.STATE.DOWNLOADING ) {
            mNetUtil.cancelDownload();
        } else if( state == NexNetworkUtils.STATE.DOWNLOADED ) {
            mNetUtil.deleteDownloadedFile();
        }

        NexLog.d("NexPlayer", "NexPlayer.closeInternal() called.");
        int result = closeInternal();

        //NexMediaDrm start
        if (null != drmHandlerThread) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                drmHandlerThread.quit();
            }
            drmHandlerThread = null;

            if (null != drmSessionManager) {
                if (null != drmKeySession) {
                    drmSessionManager.releaseSession(drmKeySession);
                    drmKeySession = null;
                }

                drmSessionManager.releaseMediaDrm();
                drmSessionManager = null;
            }
        }

        eventDrmSessionListener = null;
        //NexMediaDrm end

        return result;
    }

    private native int closeInternal();

    /**
     * \brief This method retrieves the current state of NexPlayer&trade;.
     *
     * Calling methods such as \link NexPlayer#open open\endlink
     * and \link NexPlayer#start start\endlink does not immediately change the
     * state.  The state changes asynchronously, and the new state goes
     * into effect at the same time
     * \link NexPlayer.IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete\endlink
     * is called to notify the application.
     *
     * \return  A constant indicating the current state.  This is one
     *          of the following values:
     *            - \link NexPlayer#NEXPLAYER_STATE_CLOSED NEXPLAYER_STATE_CLOSED\endlink
     *            - \link NexPlayer#NEXPLAYER_STATE_NONE NEXPLAYER_STATE_NONE\endlink
     *            - \link NexPlayer#NEXPLAYER_STATE_PAUSE NEXPLAYER_STATE_PAUSE\endlink
     *            - \link NexPlayer#NEXPLAYER_STATE_PLAY NEXPLAYER_STATE_PLAY\endlink
     *            - \link NexPlayer#NEXPLAYER_STATE_STOP NEXPLAYER_STATE_STOP\endlink
     *            .
     *
     */
    public native int getState();


    // for Recording
    /** Recording interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int recStart( String path, int maxsize );
    /** Recording interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int recPause();
    /** Recording interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int recResume();
    /** Recording interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int recStop();

    // for TimeShift
    /** Timeshift interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int timeStart( String AudioFile, String VideoFile, int maxtime, int maxfilesize );
    /** Timeshift interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int timeResume();
    /** Timeshift interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int timeStop();
    /** Timeshift interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int timePause();
    /** Timeshift interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int timeBackward( int skiptime );
    /** Timeshift interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int timeForward( int skiptime );

    /**
     *
     * \brief Sets the input channel for CEA 608 closed captions.
     *
     *  This method will be one of four available channels.  Setting the input channel to zero will also disable or turn off CEA 608 closed
     *  captions if they are present.
     *
     *  When viewing content including CEA 608 closed captions, it is important to choose whether to have the
     *  application support them in BASIC or FULL mode with the property {@link NexProperty#SET_CEA608_TYPE SET_CEA608_TYPE}.
     *  In order to meet CEA 608 closed caption specifications completely, the closed captions should be displayed
     *  in the FULL mode.  If BASIC mode is selected, the closed captions will be treated similar to other subtitles
     *  and may not always be accurately displayed (or may be difficult for a user to read) depending on the display attributes
     *  used in the closed captions.
     *
     *  When supporting CEA 608 closed captions in BASIC mode, setting the input channel to any number between 1 and 4 merely
     *  enables and displays the captions.
     *
     *  Since CEA 608 closed captions may include different information on the available input channels, the desired input
     *  channel may be selected in FULL mode by choosing the relevant channel number (1, 2, 3, or 4).
     *
     *  \param nChannel  This will be an integer from 0 to 4.  If it is zero, the closed captions will be disabled.
     *                   When using CEA 608 captions in BASIC mode, 1 to 4 simply enable the captions.
     *                   When using CEA 608 captions in FULL mode, 1 to 4 chooses the input channel to be displayed
     *                   as closed captions.
     *
     *  \returns Zero if successful or a non-zero error code.
     */
    public native int setCEA608CaptionChannel( int nChannel ); // 0 = OFF, 1...4 = channel number

    /**
     *
     * This method allows the subtitle file for particular content to be changed during playback.
     *
     * A new subtitle file can be loaded from the device's storage or 
     * from a given URL as passed in the parameter \c path. 
     * For example, the user can change the subtitles to a different language while playing the particular content. 
     *
     * \param path		The path to the new subtitle file to use or the URL to load the new subtitle file from. 
     *
     * \returns  Zero if successful or a non-zero error code. 
     *
     * \since version 6.37
     */
    public int changeSubtitlePath(String path) {
        int ret = 0;
        if( NexNetworkUtils.isHttpURL(path) ) {
            if( mNetUtil.getState() == NexNetworkUtils.STATE.DOWNLOADING )
                mNetUtil.cancelDownload();
            mNetUtil.startDownload(path);
        } else {
            ret = changeSubtitlePathInternal(path);
        }

        return ret;
    }

    public int addAD(String uri, int startTime){
        int ret = 0;

        ret = addADInternal(uri, startTime);

        return ret;
    }
    private native int addADInternal(String uri, int startTime);

    public int skipAD(){
        int ret = 0;

        ret = skipADInternal();

        return ret;
    }

    private native int skipADInternal();

    public int removeAD(int startTime){
        int ret = 0;

        ret = removeADInternal(startTime);


        return ret;
    }

    private native int removeADInternal(int startTime);


    /**
     * \brief Open the caption resource file which is attached in "res/raw" or "assets" folder.
     *
     * Access should be provided by AssetFileDescriptor. Length and Offset will be calculated internally.
     *
     * \param afd   Asset file descriptor.
     * \return      The status of the operation: this is zero in the case of success, or
     *              a non-zero NexPlayer&trade;&nbsp;error code in the case of failure.
     *
     * \warning When building the APK, most of resources will be compressed. This makes it difficult to extract FileDescriptor.
     * To avoid compressing, you MUST change your file's extension. Below extensions are allowed:
     * ".jpg", ".jpeg", ".png", ".gif", ".wav", ".mp2", ".mp3", ".ogg", ".aac", ".mpg", ".mpeg", ".mid", ".midi", ".smf", ".jet",
     * ".rtttl", ".imy", ".xmf", ".mp4", ".m4a", ".m4v", ".3gp", ".3gpp", ".3g2", ".3gpp2", ".amr", ".awb", ".wma", ".wmv"
     *
     * \since version 6.45
     */
    public int changeSubtitleFD(AssetFileDescriptor afd)
    {
        return changeSubtitleFDInternal(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
    }
    private native int changeSubtitlePathInternal(String path);

    /**
     * \brief Open the caption resource file which is attached in "res/raw" or "assets" folder.
     *
     * Length and Offset can be acquired from the UI. This API should be called after START() is completed.
     *
     * \param fd        File descriptor.
     * \param offset    Offset.
     * \param length    Length.
     * \return          The status of the operation: this is zero in the case of success, or
     *                  a non-zero NexPlayer&trade;&nbsp;error code in the case of failure.
     *
     * \warning When building the APK, most of resources will be compressed. This makes it difficult to extract FileDescriptor.
     * To avoid compressing, you MUST change your file's extension. Below extensions are allowed:
     * ".jpg", ".jpeg", ".png", ".gif", ".wav", ".mp2", ".mp3", ".ogg", ".aac", ".mpg", ".mpeg", ".mid", ".midi", ".smf", ".jet",
     * ".rtttl", ".imy", ".xmf", ".mp4", ".m4a", ".m4v", ".3gp", ".3gpp", ".3g2", ".3gpp2", ".amr", ".awb", ".wma", ".wmv"
     *
     * \since version 6.45
     */
    public int changeSubtitleFD(FileDescriptor fd, long offset, long length)
    {
        return changeSubtitleFDInternal(fd, offset, length);
    }

    protected native int changeSubtitleFDInternal(FileDescriptor fd, long offset, long length);

    /**
     * \brief This method retrieves information from the current content.
     *
     * @param info
     *            Content information class object.
     */
    private native int getInfo( Object info );

    /**
     * \brief This function retrieves information from the current content.
     *
     * \note <i>The \link NexPlayer#getContentInfoInt(int) getContentInfoInt\endlink function
     *       also returns information on the current content.  In some cases, the same information
     *       is available through both functions.  However, some items are available only through
     *       one of the functions.</i>
     *
     * <b>PERFORMANCE NOTE:</b> This allocates a new instance of \c NexContentInformation
     * every time it is called, which may place a burden on the garbage collector in some cases.
     * If you need to access multiple fields, save the returned object in a variable. For cases
     * that are particularly sensitive to performance, selected content information is available
     * through \link NexPlayer#getContentInfoInt(int) getContentInfoInt\endlink, which doesn't allocate
     * any objects.
     *
     * \return A \link NexContentInformation\endlink object containing information on the currently open content.
     *
     * \see \link NexPlayer#getContentInfoInt(int) getContentInfoInt\endlink
     */
    public NexContentInformation getContentInfo()
    {
        NexContentInformation info = new NexContentInformation();

        getInfo( info );

        return info;
    }

    /**
     * \brief Retrieves the specified content information item.  In most cases, this is equivalent
     *        to calling \link NexPlayer#getContentInfo() getContentInfo\endlink and accessing an individual
     *        field in the return value.
     *
     * However, there are a few items that are only available
     * through this method, and for items available through both methods, this one may
     * be more efficient in certain cases. See \c getContentInfo for more information.
     *
     *   Certain fields (such as the list of tracks) are only
     * available through the full structure, and
     * certain fields (such as frames displayed per second) are only available
     * here.
     *
     * <b>Content Info Indexes:</b> The following integer constants
     * identify different content information items that are available; they
     * are passed in the \c info_index argument to specify which
     * content information item the caller is interested in.
     *
     * <b>Also available in \c getContentInfo:</b>
     *  - <b>CONTENT_INFO_INDEX_MEDIA_TYPE (0)</b> Same as the \c mMediaType member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_MEDIA_DURATION (1)</b> Same as the \c mMediaDuration member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_VIDEO_CODEC (2)</b> Same as the \c mVideoCodec member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_VIDEO_WIDTH (3)</b> Same as the \c mVideoWidth member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_VIDEO_HEIGHT (4)</b> Same as the \c mVideoHeight member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_VIDEO_FRAMERATE (5)</b> Same as the \c mVideoFrameRate member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_VIDEO_BITRATE (6)</b> Same as the \c mVideoBitRate member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_AUDIO_CODEC (7)</b> Same as the \c mAudioCodec member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_AUDIO_SAMPLINGRATE (8)</b> Same as the \c mAudioSamplingRate member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_AUDIO_NUMOFCHANNEL (9)</b> Same as the \c mAudioNumOfChannel member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_AUDIO_BITRATE (10)</b> Same as the \c mAudioBitRate member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_MEDIA_ISSEEKABLE (11)</b> Same as the \c mIsSeekable member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_MEDIA_ISPAUSABLE (12)</b> Same as the \c mIsPausable member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_VIDEO_FOURCC (13)</b> Same as the \c mVideoFourCC member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_VIDEO_CODEC_CLASS (14)</b> Same as the \c mVideoCodecClass member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_VIDEO_PROFILE (15)</b> Same as the \c mVideoProfile member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_VIDEO_LEVEL (16)</b> Same as the \c mIsVideoLevel member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_VIDEO_CODEC_ERROR (17)</b> Same as the \c mVideoCodecError member of \c NexContentInfo     *
     *
     * <b>Video Performance Information (Available only via \c getContentInfoInt):</b>
     * The NexPlayer&trade;&nbsp;engine reads frames from the content, then decodes and displays
     * each frame.  If the device is not powerful enough for the resolution or bitrate being played, the decoding or
     * display of some frames may be skipped in order to maintain synchronization with the audio track.
     *
     * The values of the parameters in this section provide information about the number of frames actually being displayed.
     * Per-second averages are calculated every two seconds (although this interval may change in future releases).
     * Frame counts reset at the same interval, so the ratio is generally more meaningful than the
     * actual numbers (since the interval may change).  Running totals are also provided, and are updated
     * at the same interval.
     *
     * If you wish to perform your own calculations or average over other intervals, you can
     * periodically sample the running totals.  Running totals are reset when new content is opened.
     *  - <b>CONTENT_INFO_INDEX_VIDEO_RENDER_AVE_FPS (1000)</b> Average number of video frames per second decoded. This number was multiplied by 10, so you should divide by 10.
     *  - <b>CONTENT_INFO_INDEX_VIDEO_RENDER_AVE_DSP (1001)</b> Average number of video frames per second actually displayed. This number was multiplied by 10, so you should divide by 10.
     *  - <b>CONTENT_INFO_INDEX_VIDEO_RENDER_COUNT (1002)</b> Number of video frames displayed.
     *  - <b>CONTENT_INFO_INDEX_VIDEO_RENDER_TOTAL_COUNT (1003)</b> Total number of video frames displayed.
     *  - <b>CONTENT_INFO_INDEX_VIDEO_CODEC_DECODING_COUNT (1004)</b> Number of video frames decoded during the last interval.
     *  - <b>CONTENT_INFO_INDEX_VIDEO_CODEC_DECODING_TOTAL_COUNT (1005)</b> Total number of video frames decoded.
     *  - <b>CONTENT_INFO_INDEX_VIDEO_CODEC_AVG_DECODE_TIME (1006)</b> Average time to decode video frames.
     *  - <b>CONTENT_INFO_INDEX_VIDEO_CODEC_AVG_RENDER_TIME (1007)</b> Average time to render video frames.
     *  - <b>CONTENT_INFO_INDEX_VIDEO_CODEC_DECODE_TIME (1008)</b> Time taken to decode one video frame.
     *  - <b>CONTENT_INFO_INDEX_VIDEO_CODEC_RENDER_TIME (1009)</b> Time taken to render one video frame.
     *  - <b>CONTENT_INFO_INDEX_VIDEO_AVG_BITRATE (1010)</b> Average bitrate of video currently playing.
     *  - <b>CONTENT_INFO_INDEX_VIDEO_FRAMEBYTES (1011)</b>  Total size of video frames, in bytes.
     *  - <b>CONTENT_INFO_INDEX_AUDIO_AVG_BITRATE (1012)</b> Average bitrate of audio currently playing.
     *  - <b>CONTENT_INFO_INDEX_AUDIO_FRAMEBYTES (1013)</b> Total size of audio frames, in bytes.
     *  - <b>CONTENT_INFO_INDEX_VIDEO_FRAME_COUNT (1014)</b> Number of video frames available to be decoded during the last interval.
     *  - <b>CONTENT_INFO_INDEX_VIDEO_TOTAL_FRAME_COUNT (1015)</b> Total number of video frames to decode.
     *
     * For example, to determine the number of B-frames skipped in the previous interval simply calculate
     * CONTENT_INFO_INDEX_VIDEO_FRAME_COUNT - CONTENT_INFO_INDEX_VIDEO_CODEC_DECODING_COUNT.
     *
     * Also note that CONTENT_INFO_INDEX_VIDEO_CODEC_DECODING_COUNT - CONTENT_INFO_INDEX_VIDEO_RENDER_COUNT can be used to determine how many frames
     * were decoded but were not rendered due to performance limitations.
     *
     * @param info_index    The integer index of the content information item to return.
     *                      This is one of the \c CONTENT_INFO_INDEX_* constants described above.
     *
     * @return The integer value of the requested content information item.
     *
     * @see CONTENT_INFO_INDEX_VIDEO_RENDER_AVE_FPS
     * @see CONTENT_INFO_INDEX_VIDEO_RENDER_AVE_DSP
     * @see \link NexPlayer#getContentInfo() getContentInfo\endlink
     * @see \link NexContentInformation \endlink
     */
    public native int getContentInfoInt( int info_index );

    /**
     * Sets the value of an individual NexPlayer&trade;&nbsp;property.
     *
     * Properties control the behavior of NexPlayer&trade;&nbsp;and the features
     * that are enabled.
     *
     * This sets integer properties; use the \link NexPlayer#setProperty(NexProperty, String) setProperty(NexProperty, String)\endlink
     * version of this method for string properties.  If any properties need to be set before the player is opened, it is
     * possible to call this method before calling \link NexPlayer#open open\endlink.
     *
     * \see {@link NexProperty} for more details on specific properties.
     *
     * \param property  The property to set.
     * \param value     The new value for the property.
     *
     * \return          Zero if the property was succesfully set; non-zero if there was an error.
     */
    public int setProperty(NexProperty property, int value) {
        if (property == NexProperty.ENABLE_TUNNELED_PLAYBACK && 1 == value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                value = mAudioManager.generateAudioSessionId();
            } else {
                return -1;
            }
        } else if (property == NexProperty.SET_LOGS_TO_FILE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (null == mLogsToFile) {
                    mLogsToFile = new NexLogsToFile.Builder(this)
                            .setPreset(NexLogsToFile.NexFileLogPreset.fromIntegerValue(value))
                            .seFileCount(20)
                            .seBufferSize(50_000)
                            .build();
                }
                mLogsToFile.run();
                return 0;
            }else{
                return -1;
            }
        } else if( property == NexProperty.ENABLE_LOGGING_TO_APP_SIDE ) {
            NexLog.enableLoggingToAppSide(value);
        }
        return setProperties(property.getPropertyCode(), value);
    }

    /**
     * \brief  Sets the value of an individual NexPlayer&trade;&nbsp;property.
     *
     * Properties control the behavior of NexPlayer&trade;&nbsp;and the features
     * that are enabled.
     *
     * This sets string properties; use the \link NexPlayer#setProperty(NexProperty, int) setProperty(NexProperty, int)\endlink
     * version of this method for integer properties.  If any properties need to be set before the player is opened, it is
     * possible to call this method before calling \link NexPlayer#open open\endlink.
     *
     * \see {@link NexProperty} for details.
     *
     * \param property  The property to set.
     * \param value     The new string value for the property.
     *
     * \return          Zero if the property was succesfully set; non-zero if there was an error.
     */
    public int setProperty (NexProperty property, String value) {
        if( property == NexProperty.SUBTITLE_TEMP_PATH ) {
            mNetUtil.setDownloadPath(value);
        } else if( property == NexProperty.SET_UDS_NAME_FOR_LOGGING ) {
            NexLog.socketNameForLogs = value;
        }

        return setProperties( property.getPropertyCode(), value );
    }

    /**
     * Gets the value of an individual NexPlayer&trade;&nbsp;integer property.
     *
     * Properties control the behavior of NexPlayer&trade;&nbsp;and the features
     * that are enabled.
     *
     * This gets integer properties; for string properties, use
     * \link NexPlayer#getStringProperty(NexProperty) getStringProperty\endlink
     * instead.
     *
     * \see {@link NexProperty} for details.
     *
     * \param property  The property to get.
     *
     * \return          The value of the property.
     */
    public int getProperty (NexProperty property) {
        return getProperties(property.getPropertyCode());
    }

    /**
     * Gets the string value of an individual NexPlayer&trade;&nbsp;property.
     *
     * Properties control the behavior of NexPlayer&trade;&nbsp;and the features
     * that are enabled.
     *
     * This gets string properties; for integer properties, use
     * \link NexPlayer#getProperty(NexProperty) getProperty\endlink
     * instead.
     *
     * \see {@link NexProperty} for details.
     *
     * \param property  The property to get.
     *
     * \return          The string value of the property.
     */
    public String getStringProperty (NexProperty property) {
        String value;
        if( property == NexProperty.SUBTITLE_TEMP_PATH ) {
            value = mNetUtil.getDownloadPath();
        } else {
            value = getStringProperties( property.getPropertyCode() );
        }

        return value;
    }

    /**
     * Sets the value of an individual NexPlayer&trade;&nbsp;integer property based on the
     * numerical ID of the property.
     *
     * Normally, \link NexPlayer#setProperty(NexProperty, int) setProperty\endlink should
     * be used instead of this method.  Use this method <i>only</i> if you have a numeric
     * property code.
     *
     * For a full list of properties, see the \link NexPlayer.NexProperty NexProperty\endlink
     * enum.  To get the numeric code for a property, call the \c getPropertyCode
     * method on the enum member.
     *
     * For example:
     * \code
     * setProperties(
     *         NexProperty.SUPPORT_RTSP.getPropertyCode(),
     *         1  // enable RTSP support
     *         );
     * \endcode
     *
     * \param property  The numeric property code identifying the property to set.
     * \param value     The new value for the property.
     *
     * \return          Zero if the property was set successfully; non-zero
     *                  if there was an error.
     */
    public native int setProperties( int property, int value );

    /**
     * Sets the value of an individual NexPlayer&trade;&nbsp;string property based on the
     * numerical ID of the property.
     *
     * This is a string version of \link NexPlayer#setProperties(int, int) setProperties(int, int)\endlink.
     *
     * Normally, \link NexPlayer#setProperty setProperty\endlink should
     * be used instead of this method.  Use this method <i>only</i> if you have a numeric
     * property code.
     *
     * \param property  The numeric property code identifying the property to set.
     * \param value     The new string value for the property.
     *
     * \return         Zero if the property was set successfully; non-zero
     *                  if there was an error.
     */
    public native int setProperties( int property, String value );

    /**
     * \note  For internal NexStreaming use only. Please do not use. 
     */
    public native int setProperties( int property, byte[] value );

    /**
     * Gets the value of an individual NexPlayer&trade;&nbsp;property based on the
     * numerical ID of the property.
     *
     * Normally, \link NexPlayer#getProperty(NexProperty) getProperty\endlink should
     * be used instead of this method.  Use this method <i>only</i> if you have a numeric
     * property code.
     *
     * For a full list of properties, see the \link NexPlayer.NexProperty NexProperty\endlink
     * enum.  To get the numeric code for a property, call the \c getPropertyCode
     * method on the enum member.
     *
     * For example:
     * \code
     * int supportRTSP =
     *     getProperties(
     *         NexProperty.SUPPORT_RTSP.getPropertyCode()
     *         );
     * \endcode
     *
     * \param property  The numeric property code identifying the property to get.
     *
     * \return          The value of the property.
     */
    public native int getProperties( int property );

    /**
     * Gets the string value of an individual NexPlayer&trade;&nbsp;property based on the
     * numerical ID of the property.
     *
     * Normally, \link NexPlayer#getStringProperty(NexProperty) getStringProperty\endlink should
     * be used instead of this method.  Use this method <i>only</i> if you have a numeric
     * property code.
     *
     * For a full list of properties, see the \link NexPlayer.NexProperty NexProperty\endlink
     * enum.  To get the numeric code for a property, call the \c getPropertyCode
     * method on the enum member.
     *
     * For example:
     * \code
     * String userAgent =
     *     getProperties(
     *         NexProperty.USERAGENT_STRING.getPropertyCode()
     *         );
     * \endcode
     *
     * \param property  The numeric property code identifying the property to get.
     *
     * \return          The string value of the property.
     *
     */
    public native String getStringProperties( int property );

    /**
     * \brief   This function adds an RTSP header to be included with all future
     *          RTSP requests.
     *
     * RTSP headers have the same format as HTTP headers,
     * but the set of field names is different.
     *
     * There are several request types that are part of the RTSP protocol,
     * and when a header is added, you must specify with which request types
     * it will be included.  This is done by performing a bitwise \c OR on one
     * or more of the following values, and specifying the result in the
     * \c methods parameter:
     *  - <b>RTSP_METHOD_DESCRIBE</b>
     *  - <b>RTSP_METHOD_SETUP</b>
     *  - <b>RTSP_METHOD_OPTIONS</b>
     *  - <b>RTSP_METHOD_PLAY</b>
     *  - <b>RTSP_METHOD_PAUSE</b>
     *  - <b>RTSP_METHOD_GETPARAMETER</b>
     *  - <b>RTSP_METHOD_TEARDOWN</b>
     *  - <b>RTSP_METHOD_ALL</b>
     *
     * For example, to set a different user agent for the SETUP and PLAY requests:
     *
     * \code
     * addRTSPHeaderFields(
     *     RTSP_METHOD_SETUP | RTSP_METHOD_PLAY,
     *     "User-Agent: NexStreaming Android Player");
     * \endcode
     *
     * \param methods   The set of request methods to which this will
     *                  apply (RTSP_METHOD_* constants OR-ed together).
     * \param str       The actual header to add (including header name and value).
     *
     * \return          Zero if successful, non-zero if there was an error.
     */
    public native int addRTSPHeaderFields( int methods, String str );

    /**
     * \brief   This function adds additional header fields to be sent along with the HTTP headers
     *          when sending streaming requests (HLS and Smooth Streaming).
     *
     * The string should contain a single valid HTTP header, and should include the
     * header name, value, and delimiter.
     *
     * For example: \code addHTTPHeaderFields("X-Example: test value."); \endcode
     *
     * \note To add multiple header fields, simply call this function multiple times
     *
     * \param str   The header (including delimeter) to add to future HTTP requests.
     *
     * \return      Zero if successful, non-zero if there was an error.
     */
    public native int addHTTPHeaderFields( String str);

    /**
     * \brief   This function updates the header fields added with addHTTPHeaderFields.
     *          This API should be called after OPEN() is completed.
     *
     * The string should contain a single valid HTTP header, and should include the
     * header name, value, and delimiter.
     *
     * For example: \code updateHTTPHeaderFields("X-Example: test value updated."); \endcode
     *
     * \note To add multiple header fields, simply call this function multiple times
     *
     * \param str   The header (including delimeter) to add to HTTP requests.
     *
     * \return      Zero if successful, non-zero if there was an error.
     */
    native int updateHTTPHeaderFields( String str);

    /**
     * \brief  This method sets a user cookie to be used while playing content.
     *
     * In prior versions of the NexPlayer&trade;&nbsp;SDK, it was only possible to set a user cookie
     * before content was opened in the player but this method makes it possible
     * to set a cookie while content is playing.  The cookie set with this method may also
     * be different than an initial cookie set.
     *
     * \param strCookie  The user cookie to set, as a \c String.
     *                  When setting a cookie string, it should start with "Set-Cookie:". For example : <i>mNexPlayer.setUserCookie("Set-Cookie: [cookie string]"); </i>  \n
     *                  When setting two or more cookies, it should start with "Cookie:" and each cookies are separated by ";". For example : <i>mNexPlayer.setUserCookie("Cookie: [cookie string1];[cookie string2];[cookie string3]...");</i> 
     *
     *
     * \return    Zero if successful, non-zero if there was an error.
     * \since version 6.3
     */

    public native int setUserCookie(String strCookie);

    /**
     * \brief  This method controls the playback speed of content by the given percent.
     *
     * \note  Speed Control is an optional feature.
     * This method makes it possible to allow users to adjust the playback speed of content, from a quarter of the original speed to double speed, by changing the value of the parameter \c fPlaySeed.  For example, to play content at half-speed, \c fPlaySeed should be set to 0.5
     *
     * This method doesn't work if it is called when NexPlayer&trade;&nbsp;is stopped.
     *
     * \param fPlaySeed
     *            This float represents the percentage by which to change the playback speed.
     *            It must be in the range of 0.25 to 2.0, which adjusts the playback speed from 0.25x to 2x the original speed of the content.
     *
     * \warning  When using this method with HLS or Smooth Streaming content, playing multitrack content may cause unstable performance.
     *           Therefore, playing content as a single track is encouraged.
     */
    public native int playspeedcontrol( float fPlaySeed);


    /** One of the NexSound audio modes to be set by the \c uiAudioMode parameter in audioSetParam().    */
    public static final int NEX_AS_EARCOMFORT   			= 0x00000001;
    /** One of the NexSound audio modes to be set by the \c uiAudioMode parameter in audioSetParam().    */
    public static final int NEX_AS_REVERB					= 0x00000002;
    /** One of the NexSound audio modes to be set by the \c uiAudioMode parameter in audioSetParam().    */
    public static final int NEX_AS_STEREO_CHORUS 			= 0x00000003;
    /** One of the NexSound audio modes to be set by the \c uiAudioMode parameter in audioSetParam().    */
    public static final int NEX_AS_MUSIC_ENHANCER 			= 0x00000004;
    /** One of the NexSound audio modes to be set by the \c uiAudioMode parameter in audioSetParam().    */
    public static final int NEX_AS_CINEMA_SOUND 	= 0x00000006;


    /**
     * \brief  This audio effect interface enhances sound on NexPlayer&trade;&nbsp;but is only available in some product categories.
     *
     * \note The audio effects available in this interface are optional.
     *
     * The availability of each NexSound audio component can be checked by calling
     * \link NexPlayer#getProperty(NexProperty) getProperty\endlink on the property related to the component
     * to be checked, namely one of:
     *              - <b>AS_EARCOMFORT_AVAILABILITY (0x00050002) </b>
     *              - <b>AS_REVERB_AVAILABILITY (0x00050003) </b>
     *              - <b>AS_STEREO_CHORUS_AVAILABILITY (0x00050004)</b>
     *              - <b>AS_MUSIC_ENHANCER_AVAILABILITY (0x00050005)</b>
     *              - <b>AS_CINEMA_SOUND_AVAILABILITY (0x00050006)</b>
     *
     * \param uiAudioMode       The NexSound mode to set.  This is an integer and will be one of:
     *                            - <b> NEX_AS_EARCOMFORT = 0x00000001 </b>:  EarComfort mode moves the sound image to
     *                                  a position outside of the listener's head, simulating the more comfortable feeling
     *                                  of listening to speakers but through earphones. \n
     *                                  <b>Default Values</b>:
     *                                     - \c uiEffectStrength = 2
     *                                     - \c uiBassStrength = 3
     *                            - <b> NEX_AS_REVERB = 0x00000002 </b>: Reverb mode adds reverb to audio. \n
     *                                  <b>Default Values</b>:
     *                                     - \c uiEffectStrength = 3
     *                                     - \c uiBassStrength = 3
     *                            - <b> NEX_AS_STEREO_CHORUS = 0x00000003 </b>:  Stereo Chorus mode. \n
     *                                  <b>Default Values</b>:
     *                                     - \c uiEffectStrength = 5
     *                                     - \c uiBassStrength = 3
     *                            - <b> NEX_AS_MUSIC_ENHANCER = 0x00000004 </b>: Music Enhancer mode.\n
     *                                  <b>Default Values</b>:
     *                                     - \c uiEffectStrength = 6
     *                                     - \c uiBassStrength = 5
     *                            - <b> NEX_AS_CINEMA_SOUND = 0x00000006</b>:  Virtual surround sound effect on ordinary
     *                                  earphones.  This mode can only be turned ON and OFF and parameters \c uiEffectStrength and
     *                                  \c uiBassStrength do not apply.
     * \param uiEffectStrength  This sets the strength of the audio mode selected.  It is an integer between 0 and 6.
     * \param uiBassStrength    This sets the bass strength of the audio mode selected.  It is an integer between 0 and 6.
     *
     * \returns  Zero if successful, or a non-zero error code, including:
     *              - <b>NOT_SUPPORT (0x8000000FL)</b>: The requested NexSound audio mode is not supported in this version.
     */
    public native int audioSetParam( int uiAudioMode, int uiEffectStrength, int uiBassStrength);


    /**
     * \brief  This method turns the Auto Volume feature \c on or \c off, but this feature is only available in some product categories.
     *
     * \note  Auto Volume is an optional feature.
     *
     * When Auto Volume is turned \c on, NexPlayer&trade;&nbsp;automatically adjusts the volume level of different content
     * so that it is played at a consistent and optimal volume level, allowing the user to play different content without having to constantly adjust
     * the volume when new content starts.
     *
     * By default, Auto Volume is turned \c off (identical to the behavior of the player in product categories that do
     * not support this feature).
     *
     * \param uiOnOff  This turns the Auto Volume feature \c on and \off.  By default, this feature is \c off = 0. \n
     *                 <b>Possible Values:</b>
     *                      - \c on = 1
     *                      - \c off = 0
     *
     * \returns  The new value of Auto Volume.  If Auto Volume was turned \c off, this will be 0. \n
     *           If Auto Volume was turned \c on, it will return 1. \n
     *           If Auto Volume is not supported in this version of the NexPlayer&trade;, this will return
     *           the error, <b>NOT_SUPPORT (0x8000000FL)</b>.
     *
     * \since version 5.10
     */

    public native int setAutoVolume(int uiOnOff);

    /**
     *
     * \brief This method sets the pitch control settings for audio in content.
     *
     * \note  Audio pitch control is an optional feature in the NexPlayer&trade;&nbsp;SDK.
     *
     *  The pitch of audio in content is adjusted compared to the original pitch of the audio.  Setting \c iPitchIndex = 0
     * will not change the pitch, but with each integer step, the pitch will be adjusted by another semitone.  
     *
     * For example, if the original audio has a pitch of C, then with \c iPitchIndex = 1, the new pitch set will be a semitone higher than the original, or C sharp (D flat).
     * Similarly, if the pitch is to be lower than the original, \c iPitchIndex should be set to a negative value (in particular, for original audio with a pitch of C, \c iPitchIndex = -2 will change the pitch to A sharp (B flat)). 
     *
     * \param iPitchIndex  The index of the pitch to apply to the content, where 0 is no change in pitch, and each other integer value indicates an additional semitone change in pitch away from the original audio.
     *	         Range of pitch control (index): {-12, -11, -10, ... -1, 0, 1, ... 10, 11, 12}
     *
     * \returns  Zero if successful, or an error code in the case of failure.
     *
     * \since version 6.29
     */

    public native int setAudioPitch(int iPitchIndex);

    /**
     * \brief  This method notifies the NexPlayer&trade;&nbsp;engine whether a wired headset has been plugged in or unplugged.
     *
     * When the ENHANCED_SOUND_AVAILABILITY property is set equal to 1 and more, this method must be called (1) before calling NexPlayer.start()
     * and (2) every time the state of the headset changes.
     *
     * For example:
     *
     * \code
     * if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG))
     * {
     *     int headSetState = intent.getExtras().getInt("state");
     *     // 0 : unplugged 1 : plug in
     *     mNexPlayer.notifyHeadsetState(headSetState);
     * }
     * \endcode
     *
     * \param uiOnOff  An integer indicating whether enhanced sound is \c on (a headset is plugged in) or \c off (the headset is unplugged)
     *                 when enhanced sound is available in the NexPlayer&trade;&nbsp;SDK.
     *                 <b>Possible Values:</b>
     *                      - \c on = 1
     *                      - \c off = 0
     *
     * \returns  Zero is successful or a non-zero error code.
     *
     * \since version 6.0.7.3
     */
    public native int notifyHeadsetState(int uiOnOff );



    /** Possible \c return value for NexPlayer.GetRenderMode */
    public static final int NEX_USE_RENDER_AND      = 0x00000002;
    /** Possible \c return value for NexPlayer.GetRenderMode */
    public static final int NEX_USE_RENDER_JAVA     = 0x00000010;
    /** Possible \c return value for NexPlayer.GetRenderMode */
    public static final int NEX_USE_RENDER_OPENGL   = 0x00000020;
    /** Possible \c return value for NexPlayer.GetRenderMode */
    public static final int NEX_USE_RENDER_IOMX  	= 0x00000040;

    /**
     * \brief Sets a bitmap to be used to receive rendered frames for display, when
     * using the Java-based renderer.
     *
     * For more information on this method, please also refer to the <i>\ref javarenderer</i> section of
     * the NexPlayer&trade;&nbsp;Engine documentation.
     *
     * \param mFrameBitmap  The bitmap to receive rendered frames (when using Java-based renderer).
     *
     * \returns    Always 0, but may change in future versions.  The return value should be ignored.
     */
    public native int SetBitmap(Object mFrameBitmap);

    /**
     * \brief Informs NexPlayer&trade;&nbsp;of the current size of the \c GLSurfaceView subclass instance.
     *
     * This should be called whenever the size of the \c GLSurfaceView subclass
     * instance changes, as well as when the instance is initially created.  This
     * is because internally, OpenGL APIs use a different coordinate system, and
     * NexPlayer&trade;&nbsp;must know the pixel dimensions in order to map the OpenGL
     * coordinate system to per-pixel coordinates.
     *
     * \param width     Width of \c GLSurfaceView subclass instance, in pixels.
     * \param height    Height of \c GLSurfaceView subclass instance, in pixels.
     *
     * \returns Always 0, but may change in future versions.  The return value should be ignored.
     */
    public native int GLInit(int width, int height);

    /**
     * \brief Draws in the current OpenGL context.
     *
     * \deprecated  This method supports legacy code but should not be called by new code.
     *              Instead use the GLRenderer class.
     *
     * This remains public to support legacy code that implemented a \c GLSurfaceView
     *              subclass directly. However, new code should not call this method.  Instead,
     * simply use the GLRenderer class provided with the NexPlayer&trade;&nbsp;SDK. That
     *              class automatically calls GLDraw when needed.
     *
     * \warning This <em>must</em> be called from the OpenGL renderer thread
     *          (the thread where \c GLSurfaceView.Renderer.onDrawFrame is called).
     *          Calling this from anywhere else will result in undefined behavior
     *          and possibly cause the application to crash.
     *
     * \param mode     The type of drawing operation to perform.
     *                  - <b>0:</b> Draw the most recent video frame.
     *                  - <b>1:</b> Erase the surface to black.
     * \returns Always zero, but may change in future versions.  The return value should be ignored.
     */
    public native int GLDraw(int mode);

    /**
     * \brief Returns the type of renderer in use by the NexPlayer&trade;&nbsp;engine.
     *
     * You must check the render mode using this method and adjust
     * the application behavior appropriately. For details see
     * \ref javarenderer or \ref glrenderer.
     *
     * When using the Java renderer (\c NEX_USE_RENDER_JAVA), the application
     * must NOT call \c setOutputPos or \c setDisplay.  Doing so may
     * cause the application to crash if running under Honeycomb.
     *
     * When using the OpenGL renderer (\c NEX_USE_RENDER_OPENGL), the
     * application must NOT call \c setDisplay.
     *
     * @return Render mode; one of:
     * - <b> \link NexPlayer::NEX_USE_RENDER_AND NEX_USE_RENDER_AND\endlink</b>
     *          Using only standard Android API bitmaps
     *          to display frames.
     * - <b> \link NexPlayer::NEX_USE_RENDER_JAVA NEX_USE_RENDER_JAVA\endlink</b>
     *          Don't render to the display.  Instead, each
     *          frame is decoded and converted to the appropriate
     *          color space, and then sent to the application to display.
     * - <b> \link NexPlayer::NEX_USE_RENDER_OPENGL NEX_USE_RENDER_OPENGL\endlink</b>
     *          Using OpenGL ES 2.0 to display frames.
     * - <b> \link NexPlayer::NEX_USE_RENDER_IOMX NEX_USE_RENDER_IOMX\endlink</b>
     *          Using the hardware video renderer to display frames. Note that this renderer is used
     *          with Ice Cream Sandwich and higher versions of OS and only on supported hardware.
     */
    public native int GetRenderMode();

    /**
     * \brief Specifies the path to the renderer configuration file.
     *
     * The renderer configuration file defines which combinations of
     * codec and device should make use of which available renderer.  The configuration
     * file is provided with the SDK, but it is the responsibility of the app
     * developer to include the file with the application, and specify the path
     * using this method.
     *
     * The path must be specified before opening any content, otherwise the
     * renderer configuration file will not be used, and the player will choose the renderer
     * based on the version of Android OS alone without regard to the device model.
     *
     * \param strConfPath   The path to the configuration file.
     *
     * \returns Always zero, but may change in future versions.  The return value should be ignored.
     *
     * @deprecated Do not use. 
     */
    public native int SetConfigFilePath(String strConfPath);

    /**
     * \brief  This method allows NexPlayer&trade;&nbsp;to adjust the contrast and brightness of the
     *         displayed content.
     *
     *  These values can be adjusted either from within the code itself or can be set by the user interface.
     *  If setting the values within code, it is important to stay <b>within</b> the given range of each parameter.
     *  Values outside of these ranges will be ignored and the existing value will be retained, but unexpected results
     *  could also be potentially produced.
     *
     *  These settings can be continuously adjusted by calling the method multiple times.
     *
     *  \warning This feature is not supported on devices using the HW decoder. To check whether or not the HW decoder is used, check the value of \link NexContentInformation#mVideoCodecClass mVideoCodecClass\endlink in \c NexContentInformation.
     *  		 If \c mVideoCodecClass = 0, this method can be used to adjust contrast and brightness of displayed content.
     *
     *  \param  Contrast    This adjusts the contrast of the display.  It is an integer from 0 to 255.
     *                      By default, this value is set to 128.
     *  \param  Brightness  This adjusts the brightness of the display.  It is an integer from -127 to +128.
     *                      By default, this value is set to 0.
     *
     *  \returns  Always zero, but may change in future versions.  The return value should be ignored.
     */

    public native int SetContrastBrightness(int Contrast, int Brightness);

    /** Possible value for arguments to {@link NexPlayer.setMediaStream()}.*/
    public static final int MEDIA_STREAM_DEFAULT_ID     = 0xFFFFFFFF;
    /** Possible value for arguments to {@link NexPlayer.setMediaStream()}.*/
    public static final int MEDIA_STREAM_DISABLE_ID		= 0xFFFFFFFE;

    /** Possible value for arguments to {@link NexPlayer.setMediaTrack()}.*/
    public static final int MEDIA_TRACK_DEFAULT_ID      = 0xFFFFFFFF;
    /** Possible value for arguments to {@link NexPlayer.setMediaTrack()}.*/
    public static final int MEDIA_TRACK_DISABLE_ID      = 0xFFFFFFFE;

    /** Possible value for {@link NexStreamInformation.mType}; see there for details.*/
    public static final int MEDIA_STREAM_TYPE_AUDIO     = 0x00;
    /** Possible value for {@link NexStreamInformation.mType}; see there for details.*/
    public static final int MEDIA_STREAM_TYPE_VIDEO     = 0x01;
    /** Possible value for {@link NexStreamInformation.mType}; see there for details.*/
    public static final int MEDIA_STREAM_TYPE_TEXT      = 0x02;
    /** Possible value for {@link NexStreamInformation.mType}; see there for details.*/
    public static final int MEDIA_TRACK_TYPE_AUDIO      = 0x10;

    private static final int MEDIA_STREAM_TYPE_CUSTOM_ATTR = 0x03;

    /**
     * \brief    For media with multiple streams, this method selects the streams that will be presented
     *           to the user.
     *
     * This method should be called to set a specific media stream (video, audio, or text) while NexPlayer&trade;&nbsp;is
     * playing content with multiple streams.  To have NexPlayer&trade;&nbsp;prefer text streams in a particular language
     * <em>before</em> playing content, the property \link NexProperty#PREFER_LANGUAGE PREFER_LANGUAGE\endlink should be set
     * instead.
     *
     * The full list of available streams (if any) can be found in
     * the \link NexContentInformation#mArrStreamInformation mArrStreamInformation\endlink
     * array in NexContentInformation.  Each stream is either an audio stream or a video stream, and one of each may be
     * selected for presentation to the user.  Please see \ref multiAV "Multi-Audio and Multi-Video Stream Playback" for more explanation.
     *
     * Some streams, for example in Smooth Streaming, may in turn have associated custom attributes.  Custom attributes
     * limit playback to a subset of tracks within the stream.  Custom attributes are
     * key/value pairs.  Each possible pairing (from all the tracks in a stream) is
     * listed in \link NexStreamInformation#mArrCustomAttribInformation mArrCustomAttribInformation\endlink
     * along with an associated integer ID.  Specifying that particular integer ID causes
     * only tracks with that particular key/value pairing to beused.  Only one ID may be
     * specified at any given time.
     *
     *
     * <b>Turning Video On/Off</b>
     * This method may also be used to turn video off, for example if it is desirable that content continue to play
     * when an application using NexPlayer&trade;&nbsp;moves into the background.
     *
     * Setting any of the streams to -2 will disable it or turn it off.  There are some restrictions though when using
     * this setting:
     *  - The content must include both audio and video.  This means it is not possible to set all of the
     *    parameters (\c iAudioStreamId, \c iTextStreamId, \c iVideoStreamId, \c iVideoCustomAttrId ) equal to -2 at once.
     *  - If the player goes into the background, the UI <em>must</em> call video off (set \c iVideoStreamId = -2) immediately to provide
     *    the audio only stream while the player is in the background.
     *
     * \note To make it possible for an application to switch content tracks while
     * in the background, it is necessary to make the Application Activity a service
     * and it is recommended that the service be registered with the Android
     * operating system to help ensure it won't be closed when device
     * resources are managed.
     *
     * Please also see the sample code for more details on how to turn video on and off.
     *
     * \param iAudioStreamId
     *             The ID of the stream to use for audio.
     *             If this is <b>MEDIA_STREAM_DEFAULT_ID</b>, the initial
     *             audio stream played will continue to be used.
     *
     * \param iTextStreamId
     *             The ID of the stream to use for text (subtitles, captions, and so on).
     *             If this is <b>MEDIA_STREAM_DEFAULT_ID</b>, the initial
     *             text stream played will continue to be used.
     *
     * \param iVideoStreamId
     *             The ID of the stream to use for video.
     *             If this is <b>MEDIA_STREAM_DEFAULT_ID</b>, the initial
     *             video stream played will continue to be used.
     * \param iVideoCustomAttrId
     *             The ID of the custom attribute to use.  If this
     *             is <b>MEDIA_STREAM_DEFAULT_ID</b>,
     *             the default custom attribute will be used.  If no
     *             custom attributes are associated with a stream, this will be undefined.
     *
     * \return   Zero if successful, non-zero if there was an error.
     *
     */
    public native int setMediaStream(int iAudioStreamId, int iTextStreamId, int iVideoStreamId, int iVideoCustomAttrId);

    /**
     * \brief   For a stream with multiple tracks, this method selects the track that will be presented to the user.
     *          This method should be called to set a specific media track (audio only) while NexPlayer is playing content with multiple tracks.
     *
     * \param iMediaType  The type of media stream to change the track (it supports MEDIA_STREAM_TYPE_AUDIO).
     *
     * \param iTrackId The ID of the track.. \link NexTrackInformation#mTrackID mTrackID\endlink.
     *
     * \return   Zero if successful, non-zero if there was an error.
     */
    public native int setMediaTrack(int iMediaType, int iTrackId);

    /**
     * \brief    For media with multiple streams, this method selects the streams that will be presented
     *           to the user.
     *
     * This method should be called to set a specific media stream (video, audio, or text) while NexPlayer&trade;&nbsp;is
     * playing content with multiple streams.  To have NexPlayer&trade;&nbsp;prefer text streams in a particular language
     * <em>before</em> playing content, the property \link NexProperty#PREFER_LANGUAGE PREFER_LANGUAGE\endlink should be set
     * instead.
     *
     * The full list of available streams (if any) can be found in
     * the \link NexContentInformation#mArrStreamInformation mArrStreamInformation\endlink
     * array in NexContentInformation.  Each stream is either an audio stream or a video stream, and one of each may be
     * selected for presentation to the user.  Please see \ref multiAV "Multi-Audio and Multi-Video Stream Playback" for more explanation.
     *
     * In case of DASH streamming service, user can set the audioTrackID in the corresponding audioStreamID.
     * This API is the same as setMediaStream(), but it supports the setting of audioTrackID at the same time when
     * using DASH streamming service.
     *
     * Some streams, for example in Smooth Streaming, may in turn have associated custom attributes.  Custom attributes
     * limit playback to a subset of tracks within the stream.  Custom attributes are
     * key/value pairs.  Each possible pairing (from all the tracks in a stream) is
     * listed in \link NexStreamInformation#mArrCustomAttribInformation mArrCustomAttribInformation\endlink
     * along with an associated integer ID.  Specifying that particular integer ID causes
     * only tracks with that particular key/value pairing to beused.  Only one ID may be
     * specified at any given time.
     *
     *
     * <b>Turning Video On/Off</b>
     * This method may also be used to turn video off, for example if it is desirable that content continue to play
     * when an application using NexPlayer&trade;&nbsp;moves into the background.
     *
     * Setting any of the streams to -2 will disable it or turn it off.  There are some restrictions though when using
     * this setting:
     *  - The content must include both audio and video.  This means it is not possible to set all of the
     *    parameters (\c iAudioStreamId, \c iTextStreamId, \c iVideoStreamId, \c iVideoCustomAttrId ) equal to -2 at once.
     *  - If the player goes into the background, the UI <em>must</em> call video off (set \c iVideoStreamId = -2) immediately to provide
     *    the audio only stream while the player is in the background.
     *
     * \note To make it possible for an application to switch content tracks while
     * in the background, it is necessary to make the Application Activity a service
     * and it is recommended that the service be registered with the Android
     * operating system to help ensure it won't be closed when device
     * resources are managed.
     *
     * Please also see the sample code for more details on how to turn video on and off.
     *
     * \param iAudioStreamId
     *             The ID of the stream to use for audio.
     *             If this is <b>MEDIA_STREAM_DEFAULT_ID</b>, the initial
     *             audio stream played will continue to be used.
     *
     * \param iTextStreamId
     *             The ID of the stream to use for text (subtitles, captions, and so on).
     *             If this is <b>MEDIA_STREAM_DEFAULT_ID</b>, the initial
     *             text stream played will continue to be used.
     *
     * \param iVideoStreamId
     *             The ID of the stream to use for video.
     *             If this is <b>MEDIA_STREAM_DEFAULT_ID</b>, the initial
     *             video stream played will continue to be used.
     * \param iVideoCustomAttrId
     *             The ID of the custom attribute to use.  If this
     *             is <b>MEDIA_STREAM_DEFAULT_ID</b>,
     *             the default custom attribute will be used.  If no
     *             custom attributes are associated with a stream, this will be undefined.
     * \param iMediaType
     *             The type of media stream to change the track. ( now only support MEDIA_STREAM_TYPE_AUDIO )
     * \param iAudioTrackId
     *             The ID of the audio track.
     *
     * \return   Zero if successful, non-zero if there was an error.
     *
     */
    public native int setMediaStreamTrack(int iAudioStreamId, int iTextStreamId, int iVideoStreamId, int iVideoCustomAttrId, int iMediaType, int iAudioTrackId);

    /**
     *  \brief  This method sets the maximum bandwidth for streaming playback dynamically during playback.
     *
     * \warning It is recommended that the method \c NexABRController::changeMaxBandWidth() be used to set maximum bandwidth allowed instead of using this method.
     *
     * \note To dynamically change the maximum bandwidth in the middle of playback, please use this method.  To take effect, this method should be called after calling {@link NexPlayer#open(String, String, String, int, int, int) NexPlayer.open}.
     * Note that the maximum bandwith can also be set before play begins
     * by setting the NexProperty, \c MAX_BW, with \link NexPlayer.setProperty NexPlayer.setProperty(MAX_BW)\endlink.
     *
     * This applies in
     * cases with content where there are multiple tracks at different bandwidths (such as
     * in the case of HLS).  The player will not consider
     * any track over the maximum bandwidth when determining whether a track
     * change is appropriate, even if it detects more bandwidth available.
     *
     * If this method returns successfully, the method \c onStatusReport() will be called with a \c msg parameter \c NEXPLAYER_STATUS_REPORT_MINMAX_BANDWIDTH_CHANGED
     * Then, if the method \c onStatusReport() returns successfully, the maximum bandwidth will be changed.
     *
     * Note that to remove a maximum that has been set with this method (so that NexPlayer&trade;&nbsp;will again
     * consider all tracks regardless of bandwidth), set \c iMaxBandWidth to \c 0x00000000.
     *
     * \param iMaxBandWidth    Maximum bandwidth in kbps (kilobits per second).  To reset to no maximum bandwidth, set \c iMaxBandWidth = 0x00000000.
     *
     * \return              Zero if successful, otherwise non-zero if there was an error.
     *
     * \see NexABRController::changeMaxBandWidth()
     * \since version 6.28
     */
    public int changeMaxBandWidth(int iMaxBandWidth) {
        return changeMaxBandWidthBps(iMaxBandWidth*1024);
    }

    /**
     *
     *  \brief  This method sets the maximum bandwidth for streaming playback dynamically during playback.
     *
     * \warning \warning It is recommended that the method \c NexABRController::changeMaxBandWidth() be used to set maximum bandwidth allowed instead of using this method.
     *
     * \note  To dynamically change the maximum bandwidth in the middle of playback, please use this method.  To take effect, this method should be called after calling {@link NexPlayer#open(String, String, String, int, int, int) NexPlayer.open}.
     * Note that the maximum bandwith can also be set before play begins
     * by setting the NexProperty, \c MAX_BW, with \link NexPlayer.setProperty NexPlayer.setProperty(MAX_BW)\endlink.
     *
     * This applies in
     * cases with content where there are multiple tracks at different bandwidths (such as
     * in the case of HLS).  The player will not consider
     * any track over the maximum bandwidth when determining whether a track
     * change is appropriate, even if it detects more bandwidth available.
     *
     * If this method returns successfully, the method \c onStatusReport() will be called with a \c msg parameter \c NEXPLAYER_STATUS_REPORT_MINMAX_BANDWIDTH_CHANGED
     * Then, if the method \c onStatusReport() returns successfully, the maximum bandwidth will be changed.
     *
     * Note that to remove a maximum that has been set with this method (so that NexPlayer&trade;&nbsp;will again
     * consider all tracks regardless of bandwidth), set \c maxBwBps to \c 0x00000000.
     *
     * \param maxBwBps    Maximum bandwidth in bps (bits per second).  To reset to no maximum bandwidth, set \c maxBwBps = 0x00000000.
     *
     * \return              Zero if successful, otherwise non-zero if there was an error.
     *
     * \see NexABRController::changeMaxBandWidth()
     * \since version 6.34
     */
    public native int changeMaxBandWidthBps(int maxBwBps);

    /**
     *  \brief  This method sets the minimum bandwidth for streaming playback dynamically during playback.
     *
     * \warning It is recommended that the method \c NexABRController::changeMinBandWidth() be used to set minimum bandwidth allowed instead of using this method.
     *
     * \note To dynamically change the minimum bandwidth in the middle of playback, please use this method.  To take effect, this method should be called after calling {@link NexPlayer#open(String, String, String, int, int, int) NexPlayer.open}.
     * Note that the minimum bandwith can also be set before play begins
     * by setting the NexProperty, \c MIN_BW, with \link NexPlayer.setProperty NexPlayer.setProperty(MIN_BW)\endlink.
     *
     * This applies in
     * cases with content where there are multiple tracks at different bandwidths (such as
     * in the case of HLS).  The player will not consider
     * any track under the minimum bandwidth when determining whether a track
     * change is appropriate, even if it detects less bandwidth available.
     *
     * If this method returns successfully, the method \c onStatusReport() will be called with a \c msg parameter \c NEXPLAYER_STATUS_REPORT_MINMAX_BANDWIDTH_CHANGED.
     * Then, if the method \c onStatusReport() returns successfully, the minimum bandwidth will be changed.
     *
     * Note that to remove a minimum that has been set with this method (so that NexPlayer&trade;&nbsp;will again
     * consider all tracks regardless of bandwidth), set \c iMinBandWidth to \c 0x00000000.
     *
     * \param iMinBandWidth    Minimum bandwidth in kbps (kilobits per second).  To reset to no minimum bandwidth, set \c iMinBandWidth = 0x00000000.
     *
     * \return              Zero if successful, otherwise non-zero if there was an error.
     *
     * \see NexABRController::changeMinBandWidth()
     * \since version 6.28
     */
    public int changeMinBandWidth(int iMinBandWidth) {
        return changeMinBandWidthBps(iMinBandWidth*1024);
    }

    /**
     *  \brief  This method sets the minimum bandwidth for streaming playback dynamically during playback.
     *
     * \warning It is recommended that the method \c NexABRController::changeMinBandWidth() be used to set minimum bandwidth allowed instead of using this method.
     *
     * \note To dynamically change the minimum bandwidth in the middle of playback, please use this method.  To take effect, this method should be called after calling {@link NexPlayer#open(String, String, String, int, int, int) NexPlayer.open}.
     * Note that the minimum bandwith can also be set before play begins
     * by setting the NexProperty, \c MIN_BW, with \link NexPlayer.setProperty NexPlayer.setProperty(MIN_BW)\endlink.
     *
     * This applies in
     * cases with content where there are multiple tracks at different bandwidths (such as
     * in the case of HLS).  The player will not consider
     * any track under the minimum bandwidth when determining whether a track
     * change is appropriate, even if it detects less bandwidth available.
     *
     * If this method returns successfully, the method \c onStatusReport() will be called with a \c msg parameter \c NEXPLAYER_STATUS_REPORT_MINMAX_BANDWIDTH_CHANGED.
     * Then, if the method \c onStatusReport() returns successfully, the minimum bandwidth will be changed.
     *
     * Note that to remove a minimum that has been set with this method (so that NexPlayer&trade;&nbsp;will again
     * consider all tracks regardless of bandwidth), set \c minBwBps to \c 0x00000000.
     *
     * \param minBwBps    Minimum bandwidth in bps (bits per second).  To reset to no minimum bandwidth, set \c minBwBps = 0x00000000.
     *
     * \return              Zero if successful, otherwise non-zero if there was an error.
     *
     * \see NexABRController::changeMinBandWidth()
     * \since version 6.34
     */
    public native int changeMinBandWidthBps(int minBwBps);

    /**
     *  \brief  This method sets the minimum and maximum bandwidth for streaming playback dynamically during playback.
     *
     * \warning It is recommended that the method \c NexABRController::changeMinMaxBandWidth() be used to set minimum and maximum bandwidths allowed instead of using this method.
     *
     * \warning To dynamically change the minimum and maximum bandwidth in the middle of playback, please use this method.  To take effect, this method should be called after calling {@link NexPlayer#open(String, String, String, int, int, int) NexPlayer.open}.
     * Note that the minimum, and maximum bandwith can also be set before play begins
     * by setting the NexProperty, MAX_BW, with \link NexPlayer.setProperty NexPlayer.setProperty(MAX_BW)\endlink.
     *                             MIN_BW, with \link NexPlayer.setProperty NexPlayer.setProperty(MIN_BW)\endlink.
     *
     * This applies in
     * cases where there are multiple tracks at different bandwidths (such as
     * in the case of HLS).  The player will not consider
     * any track under the minimum, and over the maximum bandwidth when determining whether a track
     * change is appropriate, even if it detects less, and more bandwidth available.
     *
     * If this method returns success, the method onStatusReport() will be called with a msg parameter \c NEXPLAYER_STATUS_REPORT_MINMAX_BANDWIDTH_CHANGED
     * Then, if the method onStatusReport() returns success, the minimum and maximum bandwidth will be changed.
     *
     * Note that to remove a minimum and maximum that has been set with this method (so that NexPlayer&trade;&nbsp;will again
     * consider all tracks regardless of bandwidth), set both of \c iMinBandWidth, and \c iMaxBandWidth to \c 0x00000000.
     *
     * \param iMinBandWidth    Minimum bandwidth in kbps (kilobits per second).  To reset to no minimum bandwidth, \c iMinBandWidth = 0x00000000.
     * \param iMaxBandWidth    Maximum bandwidth in kbps (kilobits per second).  To reset to no maximum bandwidth, \c iMaxBandWidth = 0x00000000.
     *
     * \return              Zero if successful, otherwise non-zero if there was an error.
     *
     * \see NexABRController::changeMinMaxBandWidth()
     * \since version 6.28
     */
    public int changeMinMaxBandWidth(int iMinBandWidth, int iMaxBandWidth) {
        return changeMinMaxBandWidthBps(iMinBandWidth*1024, iMaxBandWidth*1024);
    }

    /**
     *
     *  \brief  This method sets the minimum and maximum bandwidth for streaming playback dynamically during playback.
     *
     * \warning  It is recommended that the method \c NexABRController::changeMinMaxBandWidth() be used to set minimum and maximum bandwidth values instead of using this method.
     *
     * \note  To dynamically change the minimum and maximum bandwidth in the middle of playback, please use this method.  To take effect, this method should be called after calling {@link NexPlayer#open(String, String, String, int, int, int) NexPlayer.open}.
     * Note that the minimum, and maximum bandwith can also be set before play begins
     * by setting the NexProperty, MAX_BW, with \link NexPlayer.setProperty NexPlayer.setProperty(MAX_BW)\endlink.
     *                             MIN_BW, with \link NexPlayer.setProperty NexPlayer.setProperty(MIN_BW)\endlink.
     *
     * This applies in
     * cases where there are multiple tracks at different bandwidths (such as
     * in the case of HLS).  The player will not consider
     * any track under the minimum, and over the maximum bandwidth when determining whether a track
     * change is appropriate, even if it detects less, and more bandwidth available.
     *
     * If this method returns success, the method onStatusReport() will be called with a msg parameter \c NEXPLAYER_STATUS_REPORT_MINMAX_BANDWIDTH_CHANGED
     * Then, if the method onStatusReport() returns success, the minimum and maximum bandwidth will be changed.
     *
     * Note that to remove a minimum and maximum that has been set with this method (so that NexPlayer&trade;&nbsp;will again
     * consider all tracks regardless of bandwidth), set both of \c minBwBps, and \c maxBwBps to \c 0x00000000.
     *
     * \param minBwBps    Minimum bandwidth in bps (bits per second).  To reset to no minimum bandwidth, \c minBwBps = 0x00000000.
     * \param maxBwBps    Maximum bandwidth in bps (bits per second).  To reset to no maximum bandwidth, \c maxBwBps = 0x00000000.
     *
     * \return              Zero if successful, otherwise non-zero if there was an error.
     *
     * \see NexABRController::changeMinMaxBandWidth()
     * \since version 6.34
     */
    public native int changeMinMaxBandWidthBps(int minBwBps, int maxBwBps);

    /**
     * \brief This method changes the maxWidth and maxHeight while playing content. This only works on HLS content, and switching to a track with bigger maxWidth and maxHeight will not be possible. 
     * If the currently playing track has width and height values bigger than maxWidth and maxHeight, it will switch to a track with smaller than those. 
     *
     * \param maxWidth      Maximum width.
     * \param maxHeight     Maximum height.
     *
     * \return              Zero for success, or a non-zero NexPlayer&trade;&nbsp; error code in the event of a failure.
     *
     * \since version 6.50
     */
    public native int changeMaxResolution(int maxWidth, int maxHeight);

    /** This is a possible value for the \c methods parameter of {@link NexPlayer#addRTSPHeaderFields(int, String) addRTSPHeaderFields}.  See that method for details. */
    public static int RTSP_METHOD_DESCRIBE      = 0x00000001;
    /** This is a possible value for the \c methods parameter of {@link NexPlayer#addRTSPHeaderFields(int, String) addRTSPHeaderFields}.  See that method for details. */
    public static int RTSP_METHOD_SETUP         = 0x00000002;
    /** This is a possible value for the \c methods parameter of {@link NexPlayer#addRTSPHeaderFields(int, String) addRTSPHeaderFields}.  See that method for details. */
    public static int RTSP_METHOD_OPTIONS       = 0x00000004;
    /** This is a possible value for the \c methods parameter of {@link NexPlayer#addRTSPHeaderFields(int, String) addRTSPHeaderFields}.  See that method for details. */
    public static int RTSP_METHOD_PLAY          = 0x00000008;
    /** This is a possible value for the \c methods parameter of {@link NexPlayer#addRTSPHeaderFields(int, String) addRTSPHeaderFields}.  See that method for details. */
    public static int RTSP_METHOD_PAUSE         = 0x00000010;
    /** This is a possible value for the \c methods parameter of {@link NexPlayer#addRTSPHeaderFields(int, String) addRTSPHeaderFields}.  See that method for details. */
    public static int RTSP_METHOD_GETPARAMETER  = 0x00000020;
    /** This is a possible value for the \c methods parameter of {@link NexPlayer#addRTSPHeaderFields(int, String) addRTSPHeaderFields}.  See that method for details. */
    public static int RTSP_METHOD_TEARDOWN      = 0x00000040;
    /** This is a possible value for the \c methods parameter of {@link NexPlayer#addRTSPHeaderFields(int, String) addRTSPHeaderFields}.  See that method for details. */
    public static int RTSP_METHOD_ALL           = ( RTSP_METHOD_DESCRIBE
            | RTSP_METHOD_SETUP
            | RTSP_METHOD_OPTIONS
            | RTSP_METHOD_PLAY
            | RTSP_METHOD_PAUSE
            | RTSP_METHOD_GETPARAMETER
            | RTSP_METHOD_TEARDOWN );

    /**
     * \brief  This method determines the amount of currently buffered data.
     *
     * It returns the amount of data that has been buffered ahead of the current playing position.  This is useful
     * to know in cases when it is possible to seek in (for example) a progressive
     * download without needing to buffer.
     *
     * \return The number of milliseconds (1/1000 sec) of media that has been buffered ahead.
     *
     * @deprecated  This method is deprecated from version 6.0.5, and using it is not recommended.
     *              Please use {@link NexPlayer#getBufferInfo getBufferInfo} instead.
     */
    public native int getBufferStatus();


    /**
     * \brief Retrieves the specified buffer information item.
     *
     * This method provides the ability to monitor the buffer conditions and
     * returns the specified buffer information that has been requested.
     *
     * <b>Buffer Info Indexes:</b> The following integer constants
     * identify different buffer information items that are available; they
     * are passed in the \c info_index argument to specify which
     * buffer information item the caller is interested in.
     *
     * Note that CTS stands for "Current Time Stamp".
     *
     * \param iMediaStreamType  The type of media stream being received and buffered. This will be one of:
     * 							  - MEDIA_STREAM_TYPE_AUDIO, or
     * 							  - MEDIA_STREAM_TYPE_VIDEO.
     * \param iBufferInfoIdx	The integer index of the buffer information item to return.  This is one of the \c NEXPLAYER_BUFINFO_INDEX_* constants,
     * 							namely one of:
     * 							  - <b>NEXPLAYER_BUFINFO_INDEX_BUFSIZE (0)</b>:  Buffer size.
     * 							  - <b>NEXPLAYER_BUFINFO_INDEX_INITBUFSIZE (1)</b>:  Initial buffering size.
     * 							  - <b>NEXPLAYER_BUFINFO_INDEX_INITBUFTIME (2)</b>:  Initial buffering time.
     * 							  - <b>NEXPLAYER_BUFINFO_INDEX_BUFFEREDSIZE (3)</b>:  Buffered size.
     * 							  - <b>NEXPLAYER_BUFINFO_INDEX_BUFRATE (4)</b>:  (Buffered size)*100/(Total Buffer size), or the percentage full that the buffer is.
     * 							  - <b>NEXPLAYER_BUFINFO_INDEX_FIRSTCTS (5)</b>:  CTS of the first frame in buffer.	(If there is no frame: 0xFFFFFFFF)
     * 							  - <b>NEXPLAYER_BUFINFO_INDEX_LASTCTS (6)</b>:  CTS of the last frame in buffer. 	(If there is no frame: 0xFFFFFFFF)
     * 							  - <b>NEXPLAYER_BUFINFO_INDEX_DURATION (7)</b>:  The total duration of frames in buffer.
     * 							  - <b>NEXPLAYER_BUFINFO_INDEX_FRAMECOUNT (8)</b>:  The total count of the frames in buffer.
     * 							  - <b>NEXPLAYER_BUFINFO_INDEX_STATE (9)</b>:  Buffering state (0: paused, 1: resumed).
     *
     * \return  The integer value of the requested buffer information item.
     *
     * \since version 6.0.5
     */
    public native int getBufferInfo(int iMediaStreamType, int iBufferInfoIdx);

    private native int prepareSurface(int surfacetype);


    /** This is a possible value for the \c iFlag parameter of {@link NexPlayer#setRenderOption(int) setRenderOption}.  See that method for details. */
    public static final int RENDER_MODE_VIDEO_NONE  =           0x00000000;
    /** This is a possible value for the \c iFlag parameter of {@link NexPlayer#setRenderOption(int) setRenderOption}.  See that method for details. */
    public static final int RENDER_MODE_VIDEO_FILTERBITMAP =    0x00000001;
    /** This is a possible value for the \c iFlag parameter of {@link NexPlayer#setRenderOption(int) setRenderOption}.  See that method for details. */
    public static final int  RENDER_MODE_VIDEO_DITHERING =      0x00000002;
    /** This is a possible value for the \c iFlag parameter of {@link NexPlayer#setRenderOption(int) setRenderOption}.  See that method for details. */
    public static final int  RENDER_MODE_VIDEO_ANTIALIAS =      0x00000004;
    /** This is a possible value for the \c iFlag parameter of {@link NexPlayer#setRenderOption(int) setRenderOption}.  See that method for details. */
    public static final int  RENDER_MODE_VIDEO_ALLFLAG =        0xFFFFFFFF;

    /**
     * \brief  This function configures the paint flags used with the Android bitmap rendering module.
     *
     * There are multiple rendering modules that can be used for displaying video and
     * NexPlayer&trade;&nbsp;automatically selects the best one for the given content and device.
     *
     * \see NexPlayer.init for more details on possible rendering modules.
     *
     * In the case where the rendering
     * module uses bitmaps provided through the Android API, the rendering options specified here are
     * used to set up the flags on the \c android.os.Paint object that is used to display the bitmap.
     *
     * For all other rendering modules, the values set here are ignored.
     *
     * \param iFlag
     *            This is an integer representing options for video rendering. This can be zero or more of the following values, combined
     *            together using a bitwise \c OR.  Each value corresponds to an Android API flag available
     *            on a Paint object.
     *             - <b>{@link NexPlayer#RENDER_MODE_VIDEO_NONE RENDER_MODE_VIDEO_NONE} (0x00000000)</b>
     *                      No special paint flags are set.
     *             - <b>{@link NexPlayer#RENDER_MODE_VIDEO_FILTERBITMAP RENDER_MODE_VIDEO_FILTERBITMAP} (0x00000001)</b>
     *                      Corresponds to \c android.os.Paint.FILTER_BITMAP_FLAG.
     *             - <b>{@link NexPlayer#RENDER_MODE_VIDEO_ANTIALIAS RENDER_MODE_VIDEO_ANTIALIAS} (0x00000002)</b>
     *                      Corresponds to \c android.os.Paint.ANTI_ALIAS_FLAG.
     *             - <b>{@link NexPlayer#RENDER_MODE_VIDEO_DITHERING RENDER_MODE_VIDEO_DITHERING} (0x00000004)</b>
     *                      Corresponds to \c android.os.Paint.DITHER_FLAG.
     *             - <b>{@link NexPlayer#RENDER_MODE_VIDEO_ALLFLAG RENDER_MODE_VIDEO_ALLFLAG} (0xFFFFFFFF)</b>
     *                      Enables all options.
     *
     * \return Always zero, but may change in future versions; the return value should be ignored.
     */
    public native int setRenderOption(int iFlag);

    /**
     * \brief This method sets the output video's position and size.
     *
     * The position is relative to and within the surface specified in \link NexPlayer#setDisplay(SurfaceHolder) setDisplay\endlink
     * or relative to and within the application's OpenGL surface, if the OpenGL renderer is being used.
     *
     * X and Y are the distances from the upper-left corner.  All units are in pixels and are resolution-dependent.
     *
     * If the video is larger than the surface, or part of it is outside the surface, it will be cropped
     * appropriately.  Negative values are therefore acceptable for iX and iY.
     *
     * \warning setOutputPos is <b>not</b> supported when the render mode is NEX_USE_RENDER_IOMX.
     *
     * \param iX
     *            The video display's horizontal (X) position.
     * \param iY
     *            The video display's vertical (Y) position.
     * \param iWidth
     *            The width of the video display.
     * \param iHeight
     *            The height of the video display.
     *
     * \return Always zero, but may change in future versions; the return value should be ignored.
     */
    public native int setOutputPos(int iX, int iY, int iWidth, int iHeight);

    /**
     * \brief  This method turns video rendering on or off.
     *
     * If video rendering is turned off, any existing frame will
     * remain on the display.  If you wish to clear it, you may
     * directly draw to the surface and fill it with black pixels
     * after turning off video rendering.
     *
     * \warning This method only turns video rendering on or off.  Video decoding is still performed.
     *
     * \param bOn  \c TRUE to render video, \c FALSE to turn off video rendering.
     *
     */
    public void videoOnOff(boolean bOn) {
        videoOnOff(bOn?1:0,0);
    }

    /**
     * \brief This method turns video rendering on or off.
     *
     * \warning This method is deprecated.  Use of \link NexPlayer#videoOnOff(boolean) videoOnOff(boolean)\endlink
     *          is recommended over of this function.
     * \deprecated Use videoOnOff(boolean) instead of this method.
     *
     * \param bOn
     *            1 to turn on video rendering, 0 to turn off video rendering. Other values
     *            are reserved and should not be used.
     * \param bErase
     *            This parameter is reserved; it must be zero.
     *
     * @return Always zero, but may change in future versions; the return value should be ignored.
     */
    public native int videoOnOff(int bOn, int bErase);

    /**
     * \brief  This sets the player output volume.
     *
     * This affects the output of the player before it is mixed with other sounds.
     * Normally, this should be left at the default setting of 1.0, and the volume
     * should be adjusted via the device master volume setting (adjustable by
     * the user via the hardware volume buttons on the device).  However, if the
     * application contains multiple audio sources (or if there is other audio being
     * played on the device), this property can be used to reduce the NexPlayer&trade;&nbsp;
     * volume in relation to other sounds.
     *
     * The valid range for this property is 0.0 ~ 1.0.  A value of 0.0 will silence
     * the output of the player, and a value of 1.0 (the default) plays the audio at
     * the original level, affected only by the device master volume setting (controlled
     * by the hardware buttons).
     *
     * It is not recommended to use this setting for volume controlled by the user; that
     * is best handled by adjusting the device master volume.
     *
     * \param fGain  This is a \c float between 0.0 and 1.0.  It is set to 1.0 by default.
     *
     */
    public native int setVolume(float fGain);

    /**
     * @brief This method selects a caption (subtitle) track that will be used.
     *
     * Subtitles for the selected track will be passed to
     * \link IListener#onTextRenderRender onTextRenderRender\endlink
     * for display.
     *
     * This is used for file-based captions only.  For streaming media with included
     * captions, \c setMediaStream() should be used instead, and local captions should
     * be turned off since running both types of captions at the same time has undefined
     * results.
     *
     * \param indexOfCaptionLanguage
     *          An index into the \link NexContentInformation#mCaptionLanguages mCaptionLanguages\endlink
     *          array specifying which language to use.  If there are <b> \c n </b> entries
     *          in the caption array, then you may pass \c 0...n-1 to
     *          specify the language, \c n to turn off captions.
     *
     * \return
     *          Zero if successful, non-zero if there was an error.
     */
    public native int setCaptionLanguage(int indexOfCaptionLanguage);


    public native int getCaptionLanguage();

    /**
     * \brief This function begins capturing video frames.
     *
     * This may be used to capture a single frame immediately, or to capture a series of frames at
     * regular intervals. In either case, the captured frames are actually sent to the
     * \link IListener#onVideoRenderCapture(NexPlayer, int, int, int, Object) onVideoRenderCapture\endlink
     * handler, so your application must implement it to receive the frames.
     *
     * When this function is called, the current frame will immediately be
     * sent to \c onVideoRenderCapture, and then any scheduled frames
     * will be sent after the specified interval has elapsed.
     *
     * \warning  \c captureVideo is NOT supported.
     *
     * @param iCount
     *              The number of frames to capture; this should be at least 1 or the
     *              method has no effect.
     *
     * @param iInterval
     *              If \c iCount is greater than 1, this is the number of milliseconds to
     *              wait between captures.  For example, if \c iCount is 3 and \c iInterval
     *              is 100, then one frame will be captured immediately, another after 1/10sec, and
     *              a third after a further 1/10sec.
     * @return
     *              Zero if successful, non-zero if there was an error.
     */
    public native int captureVideo( int iCount, int iInterval );

    /**
     * \brief   Gets NexPlayer&trade;&nbsp;SDK version information.
     *
     * The return value is an integer; the meaning is based on the
     * \c mode argument passed.
     *
     * Generally, the components of the version are assembled as follows:
     *
     * \code
     * String versionString = nexPlayer.getVersion(0) + "." +
     *                        nexPlayer.getVersion(1) + "." +
     *                        nexPlayer.getVersion(2) + "." +
     *                        nexPlayer.getVersion(3);
     * \endcode
     *
     * \param mode
     *              Version information to return.  This must be one of the following values (other
     *              values are reserved and should not be used):
     *                  - 0 : Major version
     *                  - 1 : Minor version
     *                  - 2 : Patch version
     *                  - 3 : Build version
     *
     * \return   Requested version information (see \c mode above).
     */
    public native int   getVersion(int mode);


    /**
     * \brief This function creates the NexPlayer&trade;&nbsp;engine.
     *
     * @param nexplayer_this
     *            The CNexPlayer instance pointer.
     * @param strPackageName
     *            The application package name. (ex. com.nexstreaming.nexplayersample)
     * @param sdkInfo
     *            The Android SDK version.
     *              - 0x15 : SDK version 1.5 CUPCAKE
     *              - 0x16 : SDK version 1.6 DONUT
     *              - 0x21 : SDK version 2.1 ECLAIR
     *              - 0x22 : SDK version 2.2 FROYO
     * @param logLevel
     *            NexPlayer&trade;&nbsp;SDK log display level.
     *
     */
    private native final int    _Constructor( Object nexplayer_this, String strPackageName, int sdkInfo, int logLevel );

    /**
     * This function releases the NexPlayer&trade;&nbsp;engine.
     */
    private native void         _Release();

    /**
     * \brief   This function set the Drm Key Info List
     *
     * \param str   The SessionId of each drm session.
     *
     * \param KeyIdCount	The total count of the Key ID.
     *
     * \param keyIdList	The Key ID list of drm.
     *
     * \param KeyIdSize	The Size of the Key ID.
     *
     * \param ErrorCode	The error code for the generated error.
     *
     * \return      Zero if successful, non-zero if there was an error.
     
     */
    private native int			SetKeyIdList(byte[] pSessionId, int uiSessionIdLen, int uiUniqueId, int uiKeyIdCount, byte[] pKeyIdList, int uiKeyIdSize, int uiErrorCode);

    @SuppressWarnings("unused")  // This called from the native code, so it is actually used
    private void callbackFromNative( Object nexplayer_ref, int what, int arg1,
                                     int arg2, int arg3,int arg4, Object obj )
    {
        @SuppressWarnings("unchecked") // The type cast to WeakReference<NexPlayer> is always safe because
                // this function is only called by known native code that always
                // passes an object of this type.
                NexPlayer nexplayer = ( (WeakReference<NexPlayer>)nexplayer_ref ).get();
        if ( nexplayer == null )
        {
            // NexLog.w(TAG, "callbackFromNative returns null");
            return;
        }

        int[] intArgs = {arg1, arg2, arg3, arg4};
        NexPlayerEvent event = new NexPlayerEvent(what, intArgs, new long[0], obj);

        _setDetailedErrorCode(event);

        switch (event.what) {

            case NexPlayerEvent.NEXPLAYER_EVENT_ASYNC_CMD_COMPLETE : {
                if( arg1 == NEXPLAYER_ASYNC_CMD_SETEXTSUBTITLE && mNetUtil.getDownloadedFile() != null ) {
                    mNetUtil.deleteDownloadedFile();
                } else if( arg1 == NEXPLAYER_ASYNC_CMD_OPEN_LOCAL
                        || arg1 == NEXPLAYER_ASYNC_CMD_OPEN_STREAMING ) {

                    if( mStoreInfo != null ) {
                        int audioStreamID = getStreamId(mStoreInfo, MEDIA_STREAM_TYPE_AUDIO);
                        int videoStreamID = getStreamId(mStoreInfo, MEDIA_STREAM_TYPE_VIDEO);
                        int textStreamID = getStreamId(mStoreInfo, MEDIA_STREAM_TYPE_TEXT);
                        int customStreamID = getStreamId(mStoreInfo, MEDIA_STREAM_TYPE_CUSTOM_ATTR);
                        int audioTrackID = getStreamId(mStoreInfo, MEDIA_TRACK_TYPE_AUDIO);

                        if (audioTrackID == MEDIA_TRACK_DEFAULT_ID) {
                            if (audioStreamID != MEDIA_STREAM_DEFAULT_ID ||
                                    videoStreamID != MEDIA_STREAM_DEFAULT_ID ||
                                    textStreamID != MEDIA_STREAM_DEFAULT_ID ||
                                    customStreamID != MEDIA_STREAM_DEFAULT_ID)
                                setMediaStream(audioStreamID, textStreamID, videoStreamID, customStreamID);
                        } else {
                            if (audioStreamID != MEDIA_STREAM_DEFAULT_ID ||
                                    videoStreamID != MEDIA_STREAM_DEFAULT_ID ||
                                    textStreamID != MEDIA_STREAM_DEFAULT_ID ||
                                    customStreamID != MEDIA_STREAM_DEFAULT_ID)
                                setMediaStreamTrack(audioStreamID, textStreamID, videoStreamID, customStreamID, MEDIA_STREAM_TYPE_AUDIO, audioTrackID);
                        }
                    }

                    if( mNetUtil.getDownloadedFile() != null ) {
                        changeSubtitlePathInternal(mNetUtil.getDownloadedFile());
                    }
                }
            }
            break;

            case NexPlayerEvent.NEXPLAYER_EVENT_ERROR : {
                NexNetworkUtils.STATE state = mNetUtil.getState();
                if (state == NexNetworkUtils.STATE.DOWNLOADING) {
                    mNetUtil.cancelDownload();
                } else if (state == NexNetworkUtils.STATE.DOWNLOADED) {
                    mNetUtil.deleteDownloadedFile();
                }
            }
            break;
        }

        NexPlayerEvent recoveryEvent = mEventRecovery.handleRecoveryEvent(event);

        if( mVideoRendererListener != null) {
            switch(event.what) {
                case NexPlayerEvent.NEXPLAYER_EVENT_VIDEO_RENDER_PREPARED:
                    mVideoRendererListener.onVideoRenderPrepared( nexplayer );
                    mEventForwarder.handleEvent(nexplayer, recoveryEvent);
                    break;
                case NexPlayerEvent.NEXPLAYER_EVENT_VIDEO_RENDER_CREATE:
                    mVideoRendererListener.onVideoRenderCreate( nexplayer, event.intArgs[0], event.intArgs[1], event.obj );
                    mEventForwarder.handleEvent(nexplayer, recoveryEvent);
                    break;
                case NexPlayerEvent.NEXPLAYER_EVENT_VIDEO_RENDER_DELETE:
                    mVideoRendererListener.onVideoRenderDelete( nexplayer);
                    mEventForwarder.handleEvent(nexplayer, recoveryEvent);
                    break;
                case NexPlayerEvent.NEXPLAYER_EVENT_VIDEO_RENDER_CAPTURE:
                    mVideoRendererListener.onVideoRenderCapture(nexplayer, event.intArgs[0], event.intArgs[1],event.intArgs[2], event.obj );
                    mEventForwarder.handleEvent(nexplayer, recoveryEvent);
                    break;
                case NexPlayerEvent.NEXPLAYER_EVENT_VIDEO_RENDER_RENDER:
                    mVideoRendererListener.onVideoRenderRender(nexplayer);
                    mEventForwarder.handleEvent(nexplayer, recoveryEvent);
                    break;
                default:
                    mEventForwarder.handleEvent(nexplayer, mListener, recoveryEvent);
                    break;
            }
        }
        else {
            mEventForwarder.handleEvent(nexplayer, mListener, recoveryEvent);
        }

        if(recoveryEvent != event) {
            mEventRecovery.recoverFromFail(nexplayer, recoveryEvent);
        }
    }

    private int getStreamId(JSONObject obj, int type) {
        int streamId = MEDIA_STREAM_DEFAULT_ID;

        if( obj != null ) {
            try {
                switch (type) {
                    case MEDIA_STREAM_TYPE_AUDIO:
                        String preferLanguageAudio = mStoreInfo.getString(NexStoredInfoFileUtils.STORED_INFO_KEY_AUDIO_PREFER_LANGUAGE);
                        if (TextUtils.isEmpty(preferLanguageAudio))
                            streamId = mStoreInfo.getInt(NexStoredInfoFileUtils.STORED_INFO_KEY_AUDIO_STREAM_ID);
                        break;
                    case MEDIA_TRACK_TYPE_AUDIO:
                        //For backward compatibility, use the following function(optInt api).
                        streamId = mStoreInfo.optInt(NexStoredInfoFileUtils.STORED_INFO_KEY_AUDIO_TRACK_ID, -1);
                        break;
                    case MEDIA_STREAM_TYPE_VIDEO:
                        streamId = mStoreInfo.getInt(NexStoredInfoFileUtils.STORED_INFO_KEY_VIDEO_STREAM_ID);
                        break;
                    case MEDIA_STREAM_TYPE_TEXT:
                        String preferLanguageText = mStoreInfo.getString(NexStoredInfoFileUtils.STORED_INFO_KEY_TEXT_PREFER_LANGUAGE);
                        if (TextUtils.isEmpty(preferLanguageText))
                            streamId = mStoreInfo.getInt(NexStoredInfoFileUtils.STORED_INFO_KEY_TEXT_STREAM_ID);
                        break;
                    case MEDIA_STREAM_TYPE_CUSTOM_ATTR:
                        streamId = mStoreInfo.getInt(NexStoredInfoFileUtils.STORED_INFO_KEY_CUSTOM_ATTR_ID);
                        break;
                }

                if (streamId < MEDIA_STREAM_DISABLE_ID )
                    streamId = MEDIA_STREAM_DEFAULT_ID;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return streamId;
    }

    @SuppressWarnings("unused") // Actually used (called from native code)
    private int callbackFromNativeRet( Object nexplayer_ref, int what, int arg1,
                                       int arg2, int arg3, int arg4, Object obj )
    {
        int nRet = 0;
        @SuppressWarnings("unchecked") // Because the object handle is from known native code, the type is guaranteed
                NexPlayer nexplayer = ( (WeakReference<NexPlayer>)nexplayer_ref ).get();
        if ( nexplayer == null )
        {
            //NexLog.e( TAG, "NexPlayer is NULL!!" );
            return -1;
        }

        NexLog.d(TAG, "callbackFromNativeRet  [what : " + what + "] "
                +"[arg1 : " + arg1 + "] "
                +"[arg2 : " + arg2 + "] "
                +"[arg3 : " + arg3 + "] "
                +"[arg4 : " + arg4 + "] ");

        int[] intArgs = {arg1, arg2, arg3};
        long[] longArgs = {arg4 };
        NexPlayerEvent event = new NexPlayerEvent(what, intArgs, longArgs, obj);
        Object handledevent = mEventForwarder.handleEvent(nexplayer, mListener, event);
        if(handledevent != null) nRet = (Integer)handledevent;
        else nRet = 0;

        return nRet;
    }

    @SuppressWarnings("unused") // Actually used (called from native code)
    private void callbackFromNative2( Object nexplayer_ref, int what, int arg1,
                                      int arg2, int arg3, long arg4, long arg5, Object Obj)
    {


        @SuppressWarnings("unchecked") // The type cast to WeakReference<NexPlayer> is always safe because
                // this function is only called by known native code that always
                // passes an object of this type.
                NexPlayer nexplayer = ( (WeakReference<NexPlayer>)nexplayer_ref ).get();
        if ( nexplayer == null )
        {
            // NexLog.w(TAG, "callbackFromNative returns null");
            return;
        }

        NexLog.d(TAG, "callbackFromNative2  [what : " + what + "] "
                +"[arg1 : " + arg1 + "] "
                +"[arg2 : " + arg2 + "] "
                +"[arg3 : " + arg3 + "] "
                +"[arg4 : " + arg4 + "] "
                +"[arg5 : " + arg5 + "] ");

        int[] intArgs = {arg1, arg2, arg3};
        long[] longArgs = {arg4, arg5};
        NexPlayerEvent event = new NexPlayerEvent(what, intArgs, longArgs, Obj);
        mEventForwarder.handleEvent(nexplayer, mListener, event);
    }

    @SuppressWarnings("unused") // Actually used (called from native code)
    private String callbackFromNativeRet2( Object nexplayer_ref, int what, int arg1,
                                           int arg2, int arg3, int arg4, Object Obj)
    {


        @SuppressWarnings("unchecked") // The type cast to WeakReference<NexPlayer> is always safe because
                // this function is only called by known native code that always
                // passes an object of this type.
                String strRet = "";
        NexPlayer nexplayer = ( (WeakReference<NexPlayer>)nexplayer_ref ).get();
        if ( nexplayer == null )
        {
            // NexLog.w(TAG, "callbackFromNative returns null");
            return strRet;
        }

        NexLog.d(TAG, "callbackFromNativeRet2  [what : " + what + "] "
                +"[arg1 : " + arg1 + "] "
                +"[arg2 : " + arg2 + "] "
                +"[arg3 : " + arg3 + "] "
                +"[arg4 : " + arg4 + "] "
                +"[Obj : " + Obj + "] "
        );

        int[] intArgs = {arg1, arg2, arg3, arg4};
        NexPlayerEvent event = new NexPlayerEvent(what, intArgs, new long[0], Obj);
        strRet = (String)mEventForwarder.handleEvent(nexplayer, mListener, event);
        return strRet;
    }

    private INexDRMLicenseListener mLicenseRequestListener = null;
    /**
     * \brief Registers a callback that will be invoked when new events occur.
     *
     * @param listener INexDRMLicenseListener: the object on which methods will be called when new events occur.
     *            This must implement the \c INexDRMLicenseListener interface.
     */
    public void setLicenseRequestListener(INexDRMLicenseListener listener)
    {
        if(listener != null) {
            mLicenseRequestListener = listener;
        }
    }

    //NexMediaDrm start

    class NexDRMInitInfo {
        int GetErrorCode() {return mErrorCode;};
        byte[] GetSessionID() {return mSesstionId;};
        void SetSessionID(byte[] sessionID) {mSesstionId = sessionID;};
        void SetErrorCode(int errorCode) {mErrorCode = errorCode;};

        private int mErrorCode = -1;
        private byte[] mSesstionId = null;
    }

    private String mMediaDrmKeyServer;

    private HashMap<String, String> mOptionalHeaderFields = null;
    private HandlerThread drmHandlerThread = null;

    private NexMediaDrmSessionManager drmSessionManager = null;
    private NexMediaDrmSession drmKeySession = null;
    private NexMediaDrmSession drmSession = null;
    private NexMediaDrmSessionManager.EventListener eventDrmSessionListener = null;
    private NexDRMInitInfo mNexDRMInitInfo = null;
    private boolean offlineExpiredKeyFetch = false;


    /**
     * \brief This method sets optionalParameters when sending requests to the Key Server of MediaDrm.
     *
     * \warning  This <b>must</b> be called before \c NexPlayer.open.
     *
     * @param optionalHeaderFields       HashMap: are included in the key request message to allow a client application to provide additional message parameters to the server.
     */
    public void setNexMediaDrmOptionalHeaderFields(HashMap<String, String> optionalHeaderFields) {
        mOptionalHeaderFields = optionalHeaderFields;
    }

    /**
     * \brief This method switch using either KEYEXPIRE_RETRIEVE_STORE mode or RETRIEVE mode when only using Offline Playback on media DRM
     *
     * \warning  This <b>must</b> be called before \c NexPlayer.open.
     *
     * @param setMode       \c TRUE to setting KEYEXPIRE_RETRIEVE_STORE mode, \c FALSE to setting RETRIEVE mode. when Offline Playback
     */
    public void onOfflineExpiredKeyFetchMode(boolean setMode) {
        offlineExpiredKeyFetch = setMode;
    }

    public enum OfflineMode {
        NONE(0),
        STORE(1),
        RETRIEVE(2),
        RETRIEVE_STORE(3),
        KEYEXPIRE_RETRIEVE_STORE(4);

        private int id;
        OfflineMode(int id) {
            this.id = id;
        }

        public static OfflineMode fromIntegerValue(int id) {
            for (OfflineMode type : OfflineMode.values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return NONE;
        }
    }
    // NexMediaDrm end

    public interface IOfflineKeyListener
    {
        /**
         * \brief This method will be called by the NexPlayer&trade;&nbsp; engine when the keyId of media DRM should be stored.
         *
         * @param mp
         *               The NexPlayer&trade;&nbsp; object generating the event.
         *
         * @param keyId
         *               The Key ID of Media drm for offline playback.
         *
         * \return  void.
         * \since version
         */
        void onOfflineKeyStoreListener(NexPlayer mp, byte[] keyId);

        /**
         * \brief This method will be called by the NexPlayer&trade;&nbsp; engine when the keyId of media DRM should be retrieved.
         *
         * @param mp
         *               The NexPlayer&trade;&nbsp; object generating the event.
         * \return the key ID of media drm stored with onOfflineKeyStoreListener.
         * \since version
         */
        byte[] onOfflineKeyRetrieveListener(NexPlayer mp);

        /**
         * \brief This method will be called by the NexPlayer&trade;&nbsp; engine when the keyId of media DRM should be expired.
         *
         * @param mp
         *               The NexPlayer&trade;&nbsp; object generating the event.
         * \return the key ID of media drm stored with onOfflineKeyStoreListener.
         * \since version
         */
        void onOfflineKeyExpiredListener(NexPlayer mp);
    }

    /**
     * \brief This method registers an Offline Key listener for managing offline keys.
     *
     * \warning  This <b>must</b> be called before  \c NexPlayer.open.
     *
     * @param OfflineKeyListener       IOfflineKeyListener
     * * \see NexPlayer.IDynamicThumbnailListener
     */
    public void setOfflineKeyListener(IOfflineKeyListener OfflineKeyListener) {
        if (!mNexPlayerInit) {
            NexLog.d(TAG, "Attempt to call setListener() but player not initialized; call NexPlayer.init() first!");
        }
        NexLog.d(TAG, "add setOfflineKeyListener");
        mEventForwarder.addReceiver(OfflineKeyListener);
    }

    // NexMediaDrm Start
    /**
     * \brief This sets the Key Server's URL to obtain keys to decrypt encrypted content.
     *
     * \warning  This <b>must</b> be called before  \c NexPlayer.open.
     *
     * @param keyUri the Key Server's URL
     */
    public void setNexMediaDrmKeyServerUri(String keyUri) {
        mMediaDrmKeyServer = keyUri;
    }

    //NexWVSWDrm start
    public void recoverFromDRM(){
        NexLog.d("NEXPLAYER", "I AM RECOVERING");
        // We need to close the current player in order to open it again after setting WV DRM
        if (this != null) {
            if (getState() > NexPlayer.NEXPLAYER_STATE_CLOSED) {
                close();
                if(mNexWVDRM != null){
                    mNexWVDRM.releaseDRMManager();
                }
            }
        }

        //SW DRM Initialization
        mNexWVDRM = new NexWVDRM();
        File fileDir = mContext.getFilesDir();
        String strCertPath = fileDir.getAbsolutePath() + "/wvcert";
        if(mNexWVDRM.initDRMManager(getEnginePath(mContext), strCertPath, mMediaDrmKeyServer, 0) != 0) {
            NexLog.d("NEXPLAYER", "SW DRM FAILURE");
            return;
        }
        setProperties(215, 2);
        open(mCurrentPath, null, null, mSourceType,
                mTransportType);
    }

    private String getEnginePath(Context context){
        String engine = "libnexplayerengine.so";

        Context iContext = context.getApplicationContext();
        String strPath = iContext.getFilesDir().getAbsolutePath();

        String strLibPath = "";

        int iPackageNameLength = iContext.getPackageName().length();
        int iStartIndex = strPath.indexOf(iContext.getPackageName());

        strLibPath = strPath.substring(0, iStartIndex + iPackageNameLength) + "/";
        final ApplicationInfo applicationInfo = context.getApplicationInfo();
        String strNativePath = strLibPath;
        try
        {
            if(Build.VERSION.SDK_INT >= 9)
            {
                Field f = ApplicationInfo.class.getField("nativeLibraryDir");
                strNativePath = (String) f.get(applicationInfo) + "/";
            }
        }
        catch (Exception e)
        {

        }

        String ret = strNativePath + engine;
        return ret;
    }
    //NexWVSWDrm end

    @TargetApi(18)
    private Object callbackFromNativeMediaDrm(Object nexplayer_ref, int what, byte[] uuid, byte[] pssh, int arg1, int arg2, Object obj)
    {
        @SuppressWarnings("unchecked") // The type cast to WeakReference<NexPlayer> is always safe because
                // this function is only called by known native code that always
                // passes an object of this type.
        int nRet = -1;
		
        NexPlayer nexplayer = ((WeakReference<NexPlayer>) nexplayer_ref).get();

        if (mNexDRMInitInfo == null) {
            mNexDRMInitInfo = new NexDRMInitInfo();
        }

        if (nexplayer == null || Build.VERSION.SDK_INT < 18) {
            NexLog.e(TAG, "[CB] what : " + what + " , platform : " + Build.VERSION.SDK_INT);
            return mNexDRMInitInfo;
        }

        switch (what) {
            case NexPlayerEvent.NEXPLAYER_INIT_MEDIA_DRM:
                NexLog.v(TAG, "[CB] NEXPLAYER_INIT_MEDIA_DRM. " );
                nRet = initNexMediaDrmSession(makeUUID(uuid), pssh, OfflineMode.fromIntegerValue(arg1), arg2);
                break;
            case NexPlayerEvent.NEXPLAYER_DEINIT_MEDIA_DRM:
                NexLog.v(TAG, "[CB] NEXPLAYER_DEINIT_MEDIA_DRM. " + (String)obj);
                try {
                    if (obj != null) {
                        NexMediaDrmSession session = drmSessionManager.getSession((String) obj);
                        drmSessionManager.releaseSession(session);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                nRet = 0;
                break;
            case NexPlayerEvent.NEXPLAYER_DRM_TYPE_ACCEPTED:
                NexLog.v(TAG, "[CB] NEXPLAYER_DRM_TYPE_ACCEPTED");
                nRet = selectDrmScheme((ArrayList<byte[]>)obj);
                break;
            default:
                break;
        }

        mNexDRMInitInfo.SetErrorCode(nRet);
        if(drmSession != null)
            mNexDRMInitInfo.SetSessionID(drmSession.getSessionId());
        return mNexDRMInitInfo;

    }

    private void sendSetKeyIdList(NexMediaDrmSession session, int uiKeyIdCount, byte[] pKeyIdList, int uiKeyIdSize, int uiErrorCode) {
        int uniqueId = 0;
        byte[] pSessionId = null;
        int nSessionIdLen = 0;

        if (session != null) {
            uniqueId = session.getUniqueId();

            byte[] orgSessionId = session.getSessionId();
            nSessionIdLen = orgSessionId.length;
            pSessionId  = new byte[nSessionIdLen + 1];
            System.arraycopy(orgSessionId, 0, pSessionId, 0, nSessionIdLen);
            pSessionId[nSessionIdLen] = 0;
        }
        else {
            NexLog.e(TAG, "[sendSetKeyIdList] session is null! errorCode: " + uiErrorCode);
            if (uiErrorCode == NexErrorCode.NONE.getIntegerCode()) {
                uiErrorCode = NexErrorCode.DRM_INIT_FAILED.getIntegerCode();
            }
        }

        SetKeyIdList(pSessionId, nSessionIdLen, uniqueId, uiKeyIdCount, pKeyIdList, uiKeyIdSize, uiErrorCode);
    }

    private int initNexMediaDrmSession(UUID uuid, byte[] pssh, OfflineMode offlineMode, int uniqueId) {
        // The followings should be synced with return codes in NEXPLAYER_IDescramble.h
        NexErrorCode errorCode = NexErrorCode.DRM_INIT_FAILED;

        try {
            if(drmSessionManager == null) {
                drmSessionManager = buildMediaDrmSessionManager(uuid);
            }
            NexMediaDrmSession session = drmSessionManager.acquireSession(drmHandlerThread.getLooper(), pssh, offlineMode, uniqueId, mOptionalHeaderFields);

            if (null != session) {
                if (null == drmKeySession) {
                    drmKeySession = session;
                }

                drmSession = session;

                switch (session.getState()) {
                    case NexMediaDrmSession.STATE_RELEASED:
                    case NexMediaDrmSession.STATE_ERROR:
                        mDrmEnabled = false;
                        break;
                    case NexMediaDrmSession.STATE_OPENING:
                        errorCode = NexErrorCode.NONE;
                        mDrmEnabled = true;
                        break;
                    case NexMediaDrmSession.STATE_OPENED:
                        errorCode = NexErrorCode.NONE;
                        mDrmEnabled = true;
                        break;
                    case NexMediaDrmSession.STATE_OPENED_WITH_KEYS:
                        sendSetKeyIdList(session, 0, null, 0, 0);
                        errorCode = NexErrorCode.NONE;
                        mDrmEnabled = true;
                        break;
                }
            }
        } catch (Exception e) {
            NexLog.e(TAG, "media drm exception occurred.");
            e.printStackTrace();
            errorCode = NexErrorCode.DRM_INIT_FAILED;
        }

        return errorCode.getIntegerCode();
    }

    private NexMediaDrmSessionManager buildMediaDrmSessionManager(UUID uuid) throws Exception {
        if (null == drmHandlerThread) {
            drmHandlerThread = new HandlerThread("DrmHandler");
            drmHandlerThread.start();
        }

        eventDrmSessionListener = new NexMediaDrmSessionManager.EventListener() {
            @Override
            public void onDrmKeysLoaded(byte[] keySetId, byte[] sessionId) {
                OfflineMode offlineMode = drmSessionManager.getMode();

                if (NexPlayer.OfflineMode.STORE == offlineMode || NexPlayer.OfflineMode.RETRIEVE_STORE == offlineMode || OfflineMode.KEYEXPIRE_RETRIEVE_STORE == offlineMode) {
                    if (mEventForwarder.hasInterface(NexPlayer.IOfflineKeyListener.class)) {
                        NexLog.d(TAG, "onOfflineKeyStoreListener...");
                        NexPlayerEvent event = new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_OFFLINE_STORE_KEY, new int[0], new long[0], keySetId);
                        mEventForwarder.handleEvent(NexPlayer.this, event);
                    } else {
                        NexLog.e(TAG, "please add a callback function for storing key id");
                    }
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || NexMediaDrm.SCHEME_PLAYREADY_TYPE == selectedDrmScheme) {
                    NexMediaDrmSession session = null;
                    try {
                        session = drmSessionManager.getSessionByByteId(sessionId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    sendSetKeyIdList(session, 0, null, 0, 0);
                }

                NexLog.v(TAG, "onDrmKeysLoaded done");
            }

            @Override
            public void onDrmSessionManagerError(Exception e) {
                NexErrorCode errorCode = NexErrorCode.DRM_INIT_FAILED;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    if (e instanceof MediaCodec.CryptoException) {
                        int cryptoErrorCode = ((MediaCodec.CryptoException) e).getErrorCode();
                        if (4 == cryptoErrorCode) {
                            errorCode = NexErrorCode.DRM_INSUFFICIENT_HDCP_LEVEL;
                        }
                    }
                }

                // Do not report an error here. onError will be called by NexPlayer
                sendSetKeyIdList(null, 0, null, 0, errorCode.getIntegerCode());

                NexLog.e(TAG, " onDrmSessionManagerError errorCode : " + errorCode + " exception : " + e.toString());
            }

            @Override
            public void onDrmKeyStatusChanged(List<NexMediaDrm.KeyStatus> KeyStatusInfo, byte[] sessionId) {
                int ErrorCode = 0, KeyIdSize = 0, count = 0;
                for (NexMediaDrm.KeyStatus keyInfo : KeyStatusInfo) {
                    KeyIdSize = keyInfo.getKeyId().length;

                    if (MediaDrm.KeyStatus.STATUS_OUTPUT_NOT_ALLOWED == keyInfo.getStatusCode()) {
                        ErrorCode = keyInfo.getStatusCode();
                        break;
                    }
                }

                int KeyIdCount = KeyStatusInfo.size();
                byte[] KeyIdList = new byte[KeyIdCount * KeyIdSize];
                for (NexMediaDrm.KeyStatus keyInfo : KeyStatusInfo) {
                    System.arraycopy(keyInfo.getKeyId(), 0, KeyIdList, count * KeyIdSize, KeyIdSize);
                    count++;
                }

                NexMediaDrmSession session = null;

                try {
                    session = drmSessionManager.getSessionByByteId(sessionId);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                sendSetKeyIdList(session, KeyIdCount, KeyIdList, KeyIdSize, ErrorCode);
            }

            @Override
            public void onDrmKeyExpired(Exception e) {
                NexLog.d(TAG, "onDrmKeyExpired");

                drmKeyExpired();
            }
			
            @Override
            public void onDrmKeysRestored() {

            }

            @Override
            public void onDrmKeysRemoved() {

            }
        };

        return new NexMediaDrmSessionManager(this, uuid, NexMediaDrm.newInstance(uuid), new NexMediaDrm.HttpNexMediaDrmCallback(mMediaDrmKeyServer, mLicenseRequestListener, mOptionalHeaderFields),
                null, eventDrmSessionListener, true, 3);
    }

    private void drmKeyExpired() {
        NexPlayerEvent event = new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_OFFLINE_KEY_EXPIRED, null, null, null);
        mEventForwarder.handleEvent(this, event);
    }

    public void fetchDRMKey(Context context) {
        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (drmSessionManager != null &&
                networkInfo != null &&
                (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE || networkInfo.getType() == ConnectivityManager.TYPE_WIFI)) {
            NexLog.d(TAG, "UpdateDRMKey on Internet connection");
            drmSessionManager.updateDRMKey();
        }
    }

    private UUID makeUUID(byte[] systemId) {
        long h = 0, l = 0;
        for (int i = 0; i < 8; i++) {
            h = (h << 8) + (systemId[i] & 0xff);
            l = (l << 8) + (systemId[i + 8] & 0xff);
        }

        return new UUID(h, l);
    }

    private int selectDrmScheme(ArrayList<byte[]> systemIds) {
        int selectedDrmIndex = -1;

        if (null != systemIds) {
            for (int index = 0; index < systemIds.size(); ++index) {
                UUID drmUUID = makeUUID(systemIds.get(index));
                if (canUseDrmScheme(drmUUID)) {
                    if (0 == drmUUID.compareTo(NexMediaDrm.WIDEVINE_UUID)) {
                        selectedDrmScheme = NexMediaDrm.SCHEME_WIDEVINE_TYPE;
                        selectedDrmIndex = index;
                        NexLog.d(TAG, "WideVine DRM (" + NexMediaDrm.WIDEVINE_UUID.toString() + ")");
                        break;
                    } else if (0 == drmUUID.compareTo(NexMediaDrm.PLAYREADY_UUID)) {
                        selectedDrmScheme = NexMediaDrm.SCHEME_PLAYREADY_TYPE;
                        selectedDrmIndex = index;
                        NexLog.d(TAG, "PlayReady DRM (" + NexMediaDrm.PLAYREADY_UUID.toString() + ")");
                    }
                }
            }
        }

        NexLog.d(TAG, "accepted drm index is " + selectedDrmIndex);
        return selectedDrmIndex;
    }

    private boolean canUseDrmScheme(UUID drmScheme) {
        boolean support = false;
        if(NexMediaDrm.isSupportDRMScheme(drmScheme)) {
            NexLog.e(TAG, "This device support this DRM Scheme");
            support = true;
            NexLog.e(TAG, "This is supported drm (" + drmScheme.toString() + ")");
        } else {
            NexLog.e(TAG, "This device doesn't support this DRM Scheme");
            NexLog.e(TAG, "This is not supported drm(" + drmScheme.toString() + ")");
        }

        return support;
    }
    // NexMediaDrm end

    public void setOfflineMode(boolean enableStoring, boolean enableRetrieving)
    {
        mEnableStoring = enableStoring;
        mEnableRetrieving = enableRetrieving;
    }

    /**
     * \brief  This method gets an audio session ID in order to use Android's audio effects with NexPlayer&trade;.
     *
     * \warning  This API is only supported on devices running Android OS version 2.3 (Gingerbread) and above.
     *
     * This API allows NexPlayer&trade;&nbsp;to support use of the Android audio effects like the Android Audio Equalizer.
     * This method should be called <b>before</b> using any audio effect.
     *
     * \returns An audio session ID as an integer.
     *
     * \since version 6.1
     */
    public native int getAudioSessionId();

    /**
     * \brief This method sets the surface on which video will be displayed.
     *
     * \warning <i>This is <b>NOT</b> supported with the Java or OpenGL renderers, and should <b>not</b> be called
     *          if one of those renderers is in use.</i>
     *
     * This function actually takes the \c android.view.SurfaceHolder associated
     * with the surface on which the video will be displayed.
     *
     * This function should be called from
     * \link NexPlayer.IListener#onVideoRenderPrepared(NexPlayer) onVideoRenderPrepared\endlink
     * after the surface has been created.  In addition, if the surface object changes (for example, if the
     * <code>SurfaceHolder</code>'s \c surfaceCreated callback is after the initial setup), this
     * function should be called again to provide the new surface.
     *
     * If the surface object is destroyed (for example, if the
     * <code>SurfaceHolder</code>'s \c surfaceDestroyed callback is after the initial setup), this
     * function should be called again to notify that the surface was destroyed.
     *
     * The surface should match the pixel format of the screen, if possible, or should
     * bet set to \c PixelFormat.RGB_565.
     *
     * In general, the surface should be created as follows:
     * \code
     * Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
     * int displayPixelFormat = display.getPixelFormat();
     *
     * SurfaceView surfaceView = new SurfaceView(this);
     * SurfaceHolder surfaceHolder = mVideoSurfaceView.getHolder();
     *
     * if( displayPixelFormat == PixelFormat.RGBA_8888 ||
     *     displayPixelFormat == PixelFormat.RGBX_8888 ||
     *     displayPixelFormat == PixelFormat.RGB_888 ||
     *     displayPixelFormat == 5 )
     * {
     *     surfaceHolder.setFormat(PixelFormat.RGBA_8888);
     * }
     * else
     * {
     *     surfaceHolder.setFormat(PixelFormat.RGB_565);
     * }
     *
     * surfaceHolder.addCallback(new SurfaceHolder.Callback() {
     *     &#x0040;Override
     *     public void surfaceDestroyed(SurfaceHolder holder) {
     *         mSurfaceExists = false;
     *         if( mPlaybackStarted ) {
     *             mNexPlayer.setDisplay(null);
     *         }
     *     }
     *     &#x0040;Override
     *     public void surfaceCreated(SurfaceHolder holder) {
     *         mSurfaceExists = true;
     *         if( mPlaybackStarted ) {
     *             mNexPlayer.setDisplay(holder);
     *         }
     *     }
     *     &#x0040;Override
     *     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
     *         // do nothing
     *     }
     * });
     * \endcode
     *
     * In \c onVideoRenderCreate, the code should ensure that the surface has already been created
     * before passing the surface holder to \c setDisplay.  Because \c onViewRenderCreate
     * can run asynchronously, it may need to wait until the surface is created by sleeping and polling.
     *
     * For example, if using the example code above, \c onVideoRenderCreate would wait until
     * \c mSurfaceExists becomes \c true, using something like:
     *
     * \code
     * while(!mSurfaceExists)
     *     Thread.sleep(10);
     * nexPlayer.setDisplay(surfaceHolder);
     * \endcode
     *
     * It is strongly recommended to use \c NexVideoRenderer instead of using this method directly. 
     *
     * \param sh    The \c android.view.SurfaceHolder holding the surface on which to display video.
     *
     */
    public void setDisplay( SurfaceHolder sh ) {
        setDisplay(sh, 0);
    }

    /**
     * \brief  This method sets the surface on which video will be displayed.
     *
     * This is the same as {@link NexPlayer#setDisplay(SurfaceHolder) setDisplay(SurfaceHolder)}, except that
     * it takes an additional surface number parameter.  Currently, only one surface
     * at a time is supported, so this additional parameter must always be zero.
     *
     * In general, it's better to use {@link NexPlayer#setDisplay(SurfaceHolder) setDisplay(SurfaceHolder)}.
     *
     * It is strongly recommended to use \c NexVideoRenderer instead of using this method directly.  
     *
     * \param sh  The \c android.view.SurfaceHolder holding the surface on which to display video.
     * \param surfaceNumber
     *            This integer sets the number of additional surfaces (currently must be zero).
     *
     * \return    Zero if successful, non-zero if there was an error.
     *
     */
    public int setDisplay( SurfaceHolder sh, int surfaceNumber )
    {

        if ( surfaceNumber == 0 )
        {
            mSurfaceHolder = sh;
            if ( mSurfaceHolder == null )
                mSurface = null;
            else
                mSurface = sh.getSurface();
            NexLog.w( TAG, "setDisplay : " + mSurfaceHolder + "," + mSurface );
        }
        return prepareSurface( surfaceNumber );
    }

    public int setDisplay( SurfaceHolder sh, Surface dummpy, int surfaceNumber )
    {
        if ( surfaceNumber == 0 )
        {
            mSurfaceHolder = sh;
            if ( mSurfaceHolder == null ) {
                mSurface = null;
                mDummySurface = dummpy;
            } else
                mSurface = sh.getSurface();
            NexLog.w( TAG, "setDisplay : " + mSurfaceHolder + "," + mSurface );
        }
        else if(surfaceNumber == NEXPLAYER_SUPPORT_MUTLIVIEW) {
            mSurfaceHolder = sh;
            if(mSurfaceHolder == null) mSurface = null;
            mDummySurface = dummpy;
        }
        return prepareSurface( surfaceNumber );
    }

    public int setDisplay( Surface surface )
    {
        mSurface = surface;
        NexLog.w( TAG, "setDisplay : no surface holder" + "," + mSurface );

        return prepareSurface( 0 );
    }

    /**
     * \brief This method releases resources used by the NexPlayer&trade;&nbsp;instance.
     *
     * This should be called when the instance is no longer needed.  After
     * calling this method, the instance can no longer be used, and methods
     * on it should not be called, except for {@link NexPlayer#getState() getState} which
     * will return {@link NexPlayer#NEXPLAYER_STATE_NONE NEXPLAYER_STATE_NONE}.
     *
     */

    public void release()
    {
        synchronized(this) {
            if ( mNativeNexPlayerClient != 0) {
                //NexWVSWDrm start
                if( mNexWVDRM != null ) {
                    mNexWVDRM.releaseDRMManager();
                    mNexWVDRM = null;
                }
                //NexWVSWDrm end

                mNexPlayerInit = false;
                NexPlayerEvent event = new NexPlayerEvent(NexPlayerEvent.NEXPLAYER_EVENT_WILL_RELEASE, new int[0], new long[0], null);
                mEventForwarder.handleEvent(this, event);
                _Release();

                if (null != mLogsToFile) {
                    mLogsToFile.kill();
                    mLogsToFile = null;
                }

            } else {
                NexLog.w(TAG, "release() not valid for uninitialized object");
            }
        }
    }


    /**
     * releases native resources
     */
    @Override
    protected void finalize()
    {
        release();
    }


    /**
     *
     * \brief This method obtains a unique ID value for the given device.  
     *
     * \param appContext       The application context.  This value can be obtained by calling the \c android.content.ContextWrapper.getApplicationContext() method.
     *
     * \returns                 A unique device ID value as a \c String.
     *
     * \since version 5.14
     */
    public native String getUniqueID(Object appContext);

	
    /**
     * \brief This method sets the path to the specific license file included with the NexPlayer&trade;&nbsp;SDK.
     *
     * This should be called after NexAlFactory::init and before NexPlayer::setNexAlFactory.
     * The license file will be included with the NexPlayer&trade;&nbsp;SDK Java files and when called with this API,
     * an application will be able to input the license file information to run NexPlayer&trade;.
     *
     * \warning The license file will be updated regularly, so please
     * always check for updates and be sure to replace and use the latest license file in applications built with the NexPlayer&trade;&nbsp;SDK.
     *
     * \param strPath Path to the license file, as a \c String.
     *
     * \see setLicenseBuffer(String strBuffer)
     *
     * \since version 6.19
     */
    public native int setLicenseFile(String strPath);

    /**
     * \brief This method inputs the license file information into a NexPlayer&trade;&nbsp;buffer.
     *
     * This should be called after calls to \c NexAlFactory::init and before calling \c NexPlayer::setNexAlFactory.
     *
     * The location of the license file included with the NexPlayer&trade;&nbsp;SDK Java files can be set using the method \c setLicenseFile.
     *
     * \param strBuffer  Information in the license file, as a \c String.
     *
     * \see setLicenseFile(String strPath) for more information.
     *
     * \since version 6.19
     */
    public native int setLicenseBuffer(String strBuffer);

    /**
     * \brief  The application must implement this interface in order to receive
     *         events from NexPlayer&trade;.
     *
     * <b>CAUTION:</b> These callbacks may occur in any thread, not
     * necessarily the main application thread. In some cases, it may not
     * be safe to call UI-related functions from within \c IListener
     * callbacks.  The safest way to update the UI is to use \c android.os.Handler
     * to post an event back to the main application thread.
     *
     * NexPlayer&trade;&nbsp;will call the methods provided in this interface
     * automatically during playback to notify the application when various
     * events have occurred.
     *
     * In most cases, the handling of these events is optional; NexPlayer&trade;&nbsp;
     * will continue playback normally without the application doing anything
     * special.  There are a few exceptions to this which are listed below.
     *
     * There are two categories of notifications.  First of all, for any asynchronous command
     * issued to NexPlayer&trade;&nbsp;(via the appropriate method call), a callback
     * will occur when that command has completed to notify the application of
     * the success or failure of the operation.
     *
     * The other category of notifications are notifications that occur during
     * playback to notify the application of changes in the state of NexPlayer&trade;.
     * For example, if NexPlayer&trade;&nbsp;begins buffering data during streaming
     * playback, an event occurs to allow the application to display an appropriate
     * message, if necessary.
     *
     * For asynchronous commands, the application will generally want to take
     * action in the following cases (some applications may need to handle
     * these differently depending on their requirements; these are correct
     * for most cases):
     *
     *   - When any command fails, display an error to the user.
     *   - When an \c open command succeeds, issue a \c start command to
     *      begin actual playback.
     *   - When a \c stop command succeeds, issue a \c close command to
     *      close the file.
     *
     *
     * This is because commands such as \c open and \c stop take some
     * time to execute, and follow-up commands such as \c start and \c close
     * cannot be called immediately, but must wait until the first command has
     * completed.
     *
     * \warning  However, do not call \c close in these event handlers as this may give rise to a deadlock.
     * A safe way to call \c close is to use the Android UI main thread's message handler.
     *
     * See each individual \c IListener method for a recommendation
     * on how to implement that method in your application.
     *
     */
    public interface IListener
    {
        /**
         * \brief This method indicates when playback has completed successfully up to the end of the content.
         *
         * This event occurs when the player reaches the end of the file or stream.
         * In most cases, applications should respond to this by calling \link NexPlayer.stop\endlink
         * and then updating the user interface.
         *
         * \param mp The NexPlayer&trade;&nbsp;object generating the event.
         *
         */
        void onEndOfContent( NexPlayer mp );

        /**
         * \brief The NexPlayer&trade;&nbsp;video task has started.
         *
         * \deprecated This method is only included for compatibility with older code and
         *             should not be used.
         *
         * This is provided for compatibility with older code, and new
         * applications may safely ignore this event.
         *
         * \param mp The NexPlayer&trade;&nbsp;object generating the event.
         *
         */
        void onStartVideoTask( NexPlayer mp );

        /**
         * \brief The NexPlayer&trade;&nbsp;audio task has started.
         *
         * \deprecated This method is only included for compatibility with older code and
         *             should not be used.
         *
         * This is provided for compatibility with older code, and new
         * applications may safely ignore this event.
         *
         * @param mp The NexPlayer&trade;&nbsp;object generating the event.
         */
        void onStartAudioTask( NexPlayer mp);

        /**
         * \brief This method indicates that playback has progressed to the specified position.
         *
         * This event occurs once per second. If the application is
         * displaying the current play position, it should update it
         * to reflect this new value.
         *
         * Applications that wish to update the play time more often
         * that once per second or with a greater accuracy may ignore
         * this event and create their own timer, in which case they
         * can use the current play time reported in {@link NexContentInformation}.
         *
         * @param mp The NexPlayer&trade;&nbsp;object generating the event.
         * @param millisec
         *            Current play position in milliseconds.
         */
        void onTime( NexPlayer mp, int millisec );

        /**
         *
         * \brief  This retrieves the program time and date information from HLS content when the #EXT-X-PROGRAM-DATE-TIME tag is present.
         *
         * This event happens approximately once every second when a \c .ts file is decoding (approximately every 30 frames).  The \c strTag value will
         * remain the same until a new #EXT-X-PROGRAM-DATE-TIME tag is received in a subsequent segment, whereas the value of the time \c offset parameter
         * will continuously rise in subsequent frames until a new #EXT-X-PROGRAM-DATE-TIME tag is received.
         *
         * The values from \c strTag and \c offset parameters can be added to determine the current frame's timestamp.  The time can then be used to help sync content and
         * text streams or to determine when content should play.
         *
         * \note If the program time is required at a more specific moment in time, the same information can be retrieved by calling the method, \link getProgramTime\endlink.
         *
         * \param mp The NexPlayer&trade;&nbsp;object generating the event.
         * \param strTag  The most recent #EXT-X-PROGRAM-DATE-TIME tag in the HLS content, as a \c String.
         * \param offset  The time offset of the currently decoding frame's timestamp with respect to the #EXT-X-PROGRAM-DATE-TIME tag time, in milliseconds.
         *
         * \see getProgramTime
         *
         * \since version 6.4
         *
         */

        void onProgramTime( NexPlayer mp, String strTag, long offset );

        /**
         * \brief    An error has occurred during playback.
         *
         * @param mp The NexPlayer&trade;&nbsp;object generating the event.
         * @param errorcode The error code for the generated error.
         */
        void onError( NexPlayer mp, NexErrorCode errorcode );

        /**
         * \brief NexPlayer&trade;'s signal status has been changed.
         *
         * @param mp The NexPlayer&trade;&nbsp;object generating the event.
         * @param pre
         *            The previous signal status.
         * @param now
         *            The current signal status.
         */
        void onSignalStatusChanged( NexPlayer mp, int pre, int now );

        /**
         * \brief NexPlayer&trade;'s state has been changed.
         * This method is called when NexPlayer&trade;'s state has been changed but it does not mean that
         * the changing operation has completed.  Therefore, the next operation can be carried out only after
         * the event \c onAsyncCmdComplete is received, not when this event, \c onStateChanged, is called.
         *
         * @param mp
         *            The NexPlayer&trade;&nbsp;object generating the event.
         * @param pre
         *            The previous play status.
         * @param now
         *            The current play status.
         */
        void onStateChanged( NexPlayer mp, int pre, int now );

        /**
         * \brief This indicates when there has been a NexPlayer&trade;&nbsp;recording error.
         *
         * @param mp
         *            The NexPlayer&trade;&nbsp;object generating the event.
         * \param err
         *            An error while recording.
         * @deprecated Not available in current version; do not use.
         */
        void onRecordingErr( NexPlayer mp, int err );

        /**
         * \brief This indicates when NexPlayer&trade;&nbsp; recording has ended.
         *
         * @param mp
         *            The NexPlayer&trade;&nbsp;object generating the event.
         * \param success
         * @deprecated Not available in current version; do not use.
         */
        void onRecordingEnd( NexPlayer mp, int success );

        /**
         * \brief This reports NexPlayer&trade;'s recording status.
         *
         * @param mp
         *            The NexPlayer&trade;&nbsp;object generating the event.
         * \param recDuration
         *            An integer indicating the duration of the recording so far.
         * \param recSize
         *            An integer indicating the size of the recording so far.
         * @deprecated Not available in current version; do not use.
         */
        void onRecording( NexPlayer mp, int recDuration, int recSize );

        /**
         * \brief This is a deprecated method that formerly reported any NexPlayer&trade;&nbsp;Time shift error.
         * @deprecated Not available in current version; do not use.
         */
        void onTimeshiftErr( NexPlayer mp, int err );

        /**
         * \brief This is a deprecated method that formerly reported NexPlayer&trade;'s Time shift status.
         *
         * @param mp
         *            The NexPlayer&trade;&nbsp;object generating the event.
         * \param currTime
         *            The current time.
         * \param TotalTime
         *            The total time.
         * @deprecated Not available in current version; do not use.
         */
        void onTimeshift( NexPlayer mp, int currTime, int TotalTime );

        /**
         * \brief   When an asynchronous method of NexPlayer&trade;&nbsp; has completed
         *          successfully or failed, this event occurs.
         *
         * @param mp The NexPlayer&trade;&nbsp;object generating the event.
         *
         * @param command   The command which completed.  This may be any
         *                  of the following values:
         *                    <ul>
         *                    <li><code>NEXPLAYER_ASYNC_CMD_OPEN_LOCAL</code> (0x00000001)</li>
         *                    <li><code>NEXPLAYER_ASYNC_CMD_OPEN_STREAMING</code> (0x00000002)</li>
         *                    <li><code>NEXPLAYER_ASYNC_CMD_OPEN_TV</code> (0x00000003)</li>
         *                    <li><code>NEXPLAYER_ASYNC_CMD_START_LOCAL</code> (0x00000005)</li>
         *                    <li><code>NEXPLAYER_ASYNC_CMD_START_STREAMING</code> (0x00000006)</li>
         *                    <li><code>NEXPLAYER_ASYNC_CMD_START_TV</code> (0x00000007)</li>
         *                    <li><code>NEXPLAYER_ASYNC_CMD_STOP</code> (0x00000008)</li>
         *                    <li><code>NEXPLAYER_ASYNC_CMD_PAUSE</code> (0x00000009)</li>
         *                    <li><code>NEXPLAYER_ASYNC_CMD_RESUME</code> (0x0000000A)</li>
         *                    <li><code>NEXPLAYER_ASYNC_CMD_SEEK</code> (0x0000000B)</li>
         *                    <li><code>NEXPLAYER_ASYNC_CMD_STEP_SEEK</code> (0x0000000E)</li>
         *                    <li><code>NEXPLAYER_ASYNC_CMD_REINITVIDEO</code> (0x00000013)</li>
         *                    <li><code>NEXPLAYER_ASYNC_CMD_FASTPLAY_START</code> (0x00000027)</li>
         *                    <li><code>NEXPLAYER_ASYNC_CMD_FASTPLAY_STOP</code> (0x00000028)</li>
         *                    <li><code>NEXPLAYER_ASYNC_CMD_SET_MEDIA_STREAM</code> (0x00000031)</li>
         *                    </ul>
         * @param result    Zero if the command was successful, otherwise
         *                  an error code.
         *
         *
         *                  Below are the possible error codes for async_command_value.
         *                   * NEXPLAYER_ASYNC_CMD_OPEN_LOCAL
         *                     - INVALID_STATE
         *                     - INVALID_PARAMETER
         *                     - SOURCE_OPEN_TIMEOUT
         *                     - NOT_SUPPORT_AUDIO_CODEC
         *                     - NOT_SUPPORT_VIDEO_CODEC
         *                     - NOT_SUPPORT_MEDIA
         *                     - FILE_INVALID_SYNTAX
         *                     - UNKNOWN
         *
         *                  * NEXPLAYER_ASYNC_CMD_OPEN_STREAMING
         *                     - INVALID_STATE
         *                     - INVALID_PARAMETER
         *                     - SOURCE_OPEN_TIMEOUT
         *                     - NOT_SUPPORT_AUDIO_CODEC
         *                     - NOT_SUPPORT_VIDEO_CODEC
         *                     - NOT_SUPPORT_MEDIA
         *                     - FILE_INVALID_SYNTAX
         *                     - ERROR_NETWORK_PROTOCOL
         *                     - UNKNOWN
         *
         *                  * NEXPLAYER_ASYNC_CMD_START_LOCAL
         *                     - INVALID_STATE
         *                     - INVALID_PARAMETER
         *                     - DATA_INACTIVITY_TIMEOUT
         *                     - NOT_SUPPORT_AUDIO_CODEC
         *                     - NOT_SUPPORT_VIDEO_CODEC
         *                     - NOT_SUPPORT_MEDIA
         *                     - FILE_INVALID_SYNTAX
         *                     - ERROR_NETWORK_PROTOCOL
         *                     - CODEC_DECODING_ERROR
         *                     - NOT_SUPPORT_VIDEO_RESOLUTION
         *                     - UNKNOWN
         *
         *                  * NEXPLAYER_ASYNC_CMD_START_STREAMING
         *                     - INVALID_STATE
         *                     - INVALID_PARAMETER
         *                     - DATA_INACTIVITY_TIMEOUT
         *                     - NOT_SUPPORT_AUDIO_CODEC
         *                     - NOT_SUPPORT_VIDEO_CODEC
         *                     - NOT_SUPPORT_MEDIA
         *                     - FILE_INVALID_SYNTAX
         *                     - ERROR_NETWORK_PROTOCOL
         *                     - CODEC_DECODING_ERROR
         *                     - NOT_SUPPORT_VIDEO_RESOLUTION
         *                     - UNKNOWN
         *
         *                  * NEXPLAYER_ASYNC_CMD_STOP
         *                     - NOT_SUPPORT_MEDIA
         *                     - ERROR_NETWORK_PROTOCOL
         *                     - UNKNOWN
         *
         *                  * NEXPLAYER_ASYNC_CMD_PAUSE
         *                     - INVALID_STATE
         *                     - INVALID_PARAMETER
         *                     - ERROR_NETWORK_PROTOCOL
         *                     - UNKNOWN
         *
         *                  * NEXPLAYER_ASYNC_CMD_RESUME
         *                     - INVALID_STATE
         *                     - INVALID_PARAMETER
         *                     - NOT_SUPPORT_AUDIO_CODEC
         *                     - NOT_SUPPORT_VIDEO_CODEC
         *                     - NOT_SUPPORT_MEDIA
         *                     - FILE_INVALID_SYNTAX
         *                     - ERROR_NETWORK_PROTOCOL
         *                     - UNKNOWN
         *
         *                  * NEXPLAYER_ASYNC_CMD_SEEK
         *                     - INVALID_STATE
         *                     - NOT_SUPPORT_TO_SEEK
         *                     - DATA_INACTIVITY_TIMEOUT
         *                     - NOT_SUPPORT_VIDEO_CODEC
         *                     - CODEC_DECODING_ERROR
         *                     - UNKNOWN
         *
         *                  * NEXPLAYER_ASYNC_CMD_SETEXTSUBTITLE
         *                     - INVALID_STATE
         *                     - INVALID_PARAMETER
         *                     - UNKNOWN
         *
         *                  * NEXPLAYER_ASYNC_CMD_REINITVIDEO
         *                     - INVALID_STATE
         *                     - NOT_SUPPORT_VIDEO_CODEC
         *                     - FILE_INVALID_SYNTAX
         *                     - ERROR_NETWORK_PROTOCOL
         *                     - CODEC_DECODING_ERROR
         *                     - NOT_SUPPORT_TO_SEEK
         *                     - DATA_INACTIVITY_TIMEOUT
         *                     - UNKNOWN
         *
         *                  * NEXPLAYER_ASYNC_CMD_FASTPLAY_START
         *                     - INVALID_STATE
         *                     - UNKNOWN
         *
         *                  * NEXPLAYER_ASYNC_CMD_FASTPLAY_STOP
         *                     - NOT_SUPPORT_AUDIO_CODEC
         *                     - CODEC_DECODING_ERROR
         *
         *                  * NEXPLAYER_ASYNC_CMD_SET_MEDIA_STREAM
         *                     - INVALID_STATE
         *                     - INVALID_PARAMETER
         *                     - UNKNOWN
         *
         *                  * NEXPLAYER_ASYNC_CMD_OPEN_STORE_STREAM
         *                     - INVALID_PARAMETER
         *                     - SOURCE_OPEN_TIMEOUT
         *                     - NOT_SUPPORT_AUDIO_CODEC
         *                     - NOT_SUPPORT_VIDEO_CODEC
         *                     - ERROR_NETWORK_PROTOCOL
         *                     - NOT_SUPPORT_MEDIA
         *                     - FILE_INVALID_SYNTAX
         *                     - UNKNOWN
         *
         *                  * NEXPLAYER_ASYNC_CMD_START_STORE_STREAM
         *                     - INVALID_STATE
         *                     - INVALID_PARAMETER
         *                     - NOT_SUPPORT_AUDIO_CODEC
         *                     - NOT_SUPPORT_VIDEO_CODEC
         *                     - NOT_SUPPORT_MEDIA
         *                     - ERROR_NETWORK_PROTOCOL
         *                     - FILE_INVALID_SYNTAX
         *                     - UNKNOWN
         *
         * @param param1    A value specific to the command that has completed.  The following
         *                  commands use this value (for all other commands, the value is
         *                  undefined and reserved for future use, and must be ignored):
         *                  <ul>
         *                    <li><b>NEXPLAYER_ASYNC_CMD_SEEK:</b><br />
         *                      The actual position at which the seek command completed.  Depending on the
         *                      media format, this may be different than the position that was requested for the seek operation.
         *                  </ul>
         * @param param2    A value specific to the command that has completed.  Currently
         *                  there are no commands that pass this parameter, and it is
         *                  reserved for future use.  Applications should ignore this value.
         */
        void onAsyncCmdComplete( NexPlayer mp, int command, int result, int param1, int param2 );

        /**
         * Reports RTSP command Timeout.
         *
         * \param mp  The NexPlayer&trade;&nbsp;object to which this event applies.
         */
        void onRTSPCommandTimeOut( NexPlayer mp );

        /**
         * Reports Pause Supervision Timeout.
         *
         * \param mp  The NexPlayer&trade;&nbsp;object to which this event applies.
         */
        void onPauseSupervisionTimeOut( NexPlayer mp );

        /**
         * Reports Data Inactivity Timeout.
         *
         * \param mp  The NexPlayer&trade;&nbsp;object to which this event applies.
         */
        void onDataInactivityTimeOut( NexPlayer mp );

	/**
         * Reports Data Inactivity TimeoutWarning.
         *
         * \param mp  The NexPlayer&trade;&nbsp;object to which this event applies.
         */
        void onDataInactivityTimeOutWarning( NexPlayer mp );

        /**
         * \brief Reports the start of buffering.
         *
         * \param mp  The NexPlayer&trade;&nbsp;object to which this event applies.
         */
        void onBufferingBegin( NexPlayer mp );

        /**
         * \brief This reports the end of buffering.
         *
         * \param mp  The NexPlayer&trade;&nbsp;object to which this event applies.
         */
        void onBufferingEnd( NexPlayer mp );

        /**
         * \brief This reports the current buffering status.
         *
         * \param mp  The NexPlayer&trade;&nbsp;object to which this event applies.
         * \param progress_in_percent
         *            The current buffering percentage.
         */
        void onBuffering( NexPlayer mp, int progress_in_percent );

        /**
         * \brief This method is called when NexPlayer&trade;&nbsp;recognizes which audio render will be used.
         *
         * Because NexPlayer&trade;&nbsp;supports only a SW audio render, this method will not be used.
         *
         * @param mp
         *            The NexPlayer&trade;&nbsp;object to which this event applies.
         */
        void onAudioRenderPrepared( NexPlayer mp );

        /**
         * \brief Notification that the audio rendering thread has been created.
         *
         * Under previous versions of the SDK, it was necessary to create and
         * manage the audio renderer.  However, under the current version this
         * is done automatically, and the \c onAudioRenderCreate method should
         * be empty or contain only diagnostic code.
         *
         * @param mp
         *            The NexPlayer&trade;&nbsp;object to which this event applies.
         * @param samplingRate
         *            The sample rate (in Hz) of the content to be played back.
         * @param channelNum
         *            The number of channels in the content (1=mono, 2=stereo).
         */
        void onAudioRenderCreate( NexPlayer mp, int samplingRate, int channelNum );

        /**
         * \brief Notification that the audio rendering thread has been destroyed.
         *
         * Under previous versions of the SDK, it was necessary to destroy
         * the audio renderer.  However, under the current version this
         * is done automatically, and the \c onAudioRenderDelete method should
         * be empty or contain only diagnostic code.
         *
         * @param mp
         *           The NexPlayer&trade;&nbsp;object to which this event applies.
         */
        void onAudioRenderDelete( NexPlayer mp );

        /**
         * \brief  This method is called when NexPlayer&trade;&nbsp;recognizes which video render type will be used.
         * @deprecated Not available in current version; use IVideoRendererListener instead.
         *
         * At first, NexPlayer&trade;&nbsp;does not know which renderer will be used.
         * When this method is called, the application can determine the video renderer mode by calling
         * {@link NexPlayer#GetRenderMode() GetRenderMode}
         * and prepare for the specified video renderer, as in the following example code:
         *     \code
         *     	public void onVideoRenderPrepared(NexPlayer mp) {
         *      if(mNexPlayer.GetRenderMode() == NexPlayer.NEX_USE_RENDER_OPENGL) {
         *      	UseOpenGL = true;
         *      	mHandler.post(new Runnable() {
         *      		public void run() {
         *      			mVideoSurfaceView.setVisibility(View.INVISIBLE);
         *      			int colorDepth = 4;
         *      			if(glRenderer == null)
         *      			{
         *      				glRenderer = new GLRenderer(mContext, mNexPlayer, this, colorDepth);
         *      				FrameLayout view = (FrameLayout)findViewById(R.id.gl_container);
         *      				view.addView(glRenderer);
         *      			}
         *      			else if(mInitGLRenderer == true)
         *      			{
         *      				glRenderer.mReInitRenderer = true;
         *      				glRenderer.requestRender();
         *      			}
         *      			else
         *      			{
         *      				glRenderer.setVisibility(View.VISIBLE );
         *      			}
         *
         *      		}
         *      	});
         *      }
         *      else
         *      {
         *      	UseOpenGL = false;
         *      	mHandler.post(new Runnable() {
         *      		public void run() {
         *      			if(mNexPlayer.GetRenderMode() == NexPlayer.NEX_USE_RENDER_AND)
         *      			{
         *      				mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);//For Gingerbread Android Renderer
         *      			}
         *      			else
         *      			{
         *      				mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//For HW Renderer
         *      			}
         *      			if(glRenderer != null)
         *      			{
         *      				glRenderer.setVisibility(View.INVISIBLE );
         *      				glRenderer = null;
         *      			}
         *      			mVideoSurfaceView.setVisibility(View.VISIBLE); // This invokes nexPlayer.setDisplay(mSurfaceHolderForSW, 0);
         *
         *      		}
         *      	});
         *      }
         *      }
         *     \endcode
         *
         * @param mp
         *            The NexPlayer&trade;&nbsp;object to which this event applies.
         */
        void onVideoRenderPrepared( NexPlayer mp );

        /**
         * \brief  This method is called when NexPlayer&trade;&nbsp;needs the application to create a surface on which
         *         to render the video.
         * @deprecated Not available in current version; use IVideoRendererListener instead.
         *
         * The application must respond to this by calling
         * {@link NexPlayer#setDisplay(SurfaceHolder) setDisplay}.
         *
         * Generally speaking, the application will actually create the surface earlier,
         * during GUI layout, and will simply use the existing handle in response to this
         * call.  There are, however, some threading considerations.  See
         * {@link NexPlayer#setDisplay(SurfaceHolder) setDisplay} for details.
         *
         * @param mp
         *            The NexPlayer&trade;&nbsp;object to which this event applies.
         * @param width
         *            The width of the source video.
         * @param height
         *            The height of the source video.
         * @param rgbBuffer
         *            Direct RGB Buffer(RGB565 format).
         *            This RGB buffer is shared with NexPlayer&trade;&nbsp;Engine native code.
         */
        void onVideoRenderCreate( NexPlayer mp, int width, int height, Object rgbBuffer );

        /**
         * \brief This method is called when NexPlayer&trade;&nbsp;no longer needs the render surface.
         * @deprecated Not available in current version; use IVideoRendererListener instead.
         *
         * If a surface was created in \c onVideoRenderCreate, this is the
         * place to destroy it.  However, if (as in most cases) an existing surface
         * was used, then this function need not take any special action, other than
         * updating whatever state the application needs to track.
         *
         * @param mp
         *            The NexPlayer&trade;&nbsp;object to which this event applies.
         */
        void onVideoRenderDelete( NexPlayer mp);

        /**
         * \brief  This requests to display Video frame data at JAVA application.
         * @deprecated Not available in current version; use IVideoRendererListener instead.
         *
         * \param mp  The NexPlayer&trade;&nbsp;object to which this event applies.
         */
        void onVideoRenderRender( NexPlayer mp );

        /**
         * \brief   Called when a frame of video has been captured.
         * @deprecated Not available in current version; use IVideoRendererListener instead.
         *
         * After calling {@link NexPlayer#captureVideo(int, int) captureVideo} to
         * set up video capture, this function will be called whenever a frame is
         * captured, and can process the captured frame as necessary.
         *
         * \code
         * Bitmap bitmap = Bitmap.createBitmap(width, height, pixelbyte==2?Config.RGB_565:Config.ARGB_8888 );
         * ByteBuffer RGBBuffer = (ByteBuffer)rgbBuffer;
         * RGBBuffer.asIntBuffer();
         * bitmap.copyPixelsFromBuffer(RGBBuffer);
         * \endcode
         *
         * @param mp
         *            The NexPlayer&trade;&nbsp;object to which this event applies.
         * @param width
         *            The width of the captured frame.
         * @param height
         *            The height of the captured frame.
         * @param pixelbyte
         *            The number of bytes per pixel (2 for RGB565; 4 for RGBA).
         * @param bitmap
         *            The object where the captured video frame data is stored.
         *
         */
        void onVideoRenderCapture(NexPlayer mp, int width, int height, int pixelbyte, Object bitmap );

        /**
         * \brief   Called when initially beginning playback of media content with
         *          associated subtitles.
         *
         * @param mp
         *          The NexPlayer&trade;&nbsp;object to which this event applies.
         *
         * @param numTracks
         *          The number of subtitle tracks available for this media.  Note
         *          that this may be 0 if there are no subtitles, or this function
         *          may not be called at all.
         */
        void onTextRenderInit( NexPlayer mp, int numTracks );

        /**
         * \brief This function is called when new subtitle data is ready for display.
         *
         * This is called whenever playback reaches a point in time where subtitles on any
         * track need to be displayed or cleared.
         *
         * The text to display is provided in a \c NexClosedCaption object as a byte array;
         * it is the responsibility of the application to convert this to text with the appropriate
         * encoding.  Where possible, the encoding information will be provided in the
         * NexClosedCaption.mEncodingType, but many subtitle
         * file formats do not explicitly specify an encoding, so it may be necessary for
         * the application to guess the encoding or allow the user to select it.
         *
         * \par <i>HISTORIAL NOTE 1:</i>
         *      In previous API versions, it was the responsibility of the
         * application to handle the case where there were multiple tracks in a file by
         *      filtering based on \c trackIndex.  This is no longer necessary (or even
         * possible) as that functionality has been replaced by
         *      {@link NexPlayer#setCaptionLanguage(int) setCaptionLanguage} and \c trackIndex
         *      is no longer used and is always zero.
         *
         * \par <i>HISTORIAL NOTE 2:</i>
         *      In previous API versions, the third argument of this method
         *      was a Java byte array, and encoding information was not specified.
         *
         * @param mp
         *          The NexPlayer&trade;&nbsp;object to which this event applies.
         *
         * @param trackIndex
         *          This is always zero and should always be ignored.
         *
         * @param textInfo
         *          The text to be displayed (cast this to a \c NexClosedCaption object).
         */
        void onTextRenderRender( NexPlayer mp, int trackIndex, NexClosedCaption textInfo );


        /**
         * \brief This method is called when new timed metadata is ready for display in HLS.
         *
         * Timed metadata includes additional information about the playing content that may be
         * displayed to the user and this information may change at different times throughout the
         * content.  Each time new metadata is available for display, this method is called.
         *
         * \see  NexID3TagInformation for more details on the available metadata information.
         *
         * \param mp         The NexPlayer&trade;&nbsp;object to which this event applies.
         * \param TimedMeta  An NexID3TagInformation object that contains the timed metadata
         *                   associated with the content to be displayed.
         *
         */
        void onTimedMetaRenderRender( NexPlayer mp, NexID3TagInformation TimedMeta );

        /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}.
         * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_NONE} instead. */
        int eNEXPLAYER_STATUS_NONE              = 0x00000000;
        /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}.
         * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_AUDIO_GET_CODEC_FAILED} instead. */
        int eNEXPLAYER_AUDIO_GET_CODEC_FAILED   = 0x00000001;
        /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}.
         * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_VIDEO_GET_CODEC_FAILED} instead. */
        int eNEXPLAYER_VIDEO_GET_CODEC_FAILED   = 0x00000002;
        /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}.
         * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_AUDIO_INIT_FAILED} instead. */
        int eNEXPLAYER_AUDIO_INIT_FAILED        = 0x00000003;
        /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}.
         * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_VIDEO_INIT_FAILED} instead. */
        int eNEXPLAYER_VIDEO_INIT_FAILED        = 0x00000004;
        /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}.
         * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_TRACK_CHANGED} instead. */
        int eNEXPLAYER_TRACK_CHANGED            = 0x00000005;
        /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}.
         * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_STREAM_CHANGED} instead. */
        int eNEXPLAYER_STREAM_CHANGED           = 0x00000006;
        /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}.
         * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_DSI_CHANGED} instead. */
        int eNEXPLAYER_DSI_CHANGED              = 0x00000007;
        /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}.
         * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_OBJECT_CHANGED} instead. */
        int eNEXPLAYER_OBJECT_CHANGED           = 0x00000008;
        /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}.
         * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_CONTENT_INFO_UPDATED} instead. */
        int eNEXPLAYER_CONTENT_INFO_UPDATED     = 0x00000009;
        /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}.
         * This value is deprecated and has been renamed.  Use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_MAX NEXPLAYER_STATUS_REPORT_MAX} instead.
         * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_MAX} instead. */
        int NEXPLAYER_STATUS_MAX                = 0xFFFFFFFF;


        /**
         * \brief This function is called when there is a change in the available content information.
         *
         * This can happen, for example, if the track changes during HLS playback,
         * resulting in changes to the bitrate, resolution, or even the codec
         * in use.
         *
         * The \c msg parameter contains information about the condition
         * that has changed.
         *
         * Because multiple calls to this function can be issued for the same event,
         * unknown values for \c msg should generally be ignored.  To handle
         * general status changes that affect content information without processing
         * duplicate messages, the best approach is just to handle
         * \link NexPlayer.IListener.NEXPLAYER_STATUS_REPORT_CONTENT_INFO_UPDATED NEXPLAYER_STATUS_REPORT_CONTENT_INFO_UPDATED\endlink.
         *
         * To determine the new content information when this event occurs, call
         * \link NexPlayer#getContentInfo() getContentInfo\endlink or
         * \link NexPlayer#getContentInfoInt(int) getContentInfoInt\endlink.
         *
         * @param mp
         *        The NexPlayer&trade;&nbsp;object to which this event applies.
         *
         * @param msg
         *        The type of notification.  This is one of the following values:
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_NONE NEXPLAYER_STATUS_REPORT_NONE} (0x00000000) </b>
         *              No status change (this value is not normally passed to \c onStatusReport, and
         *              should generally be ignored).
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_AUDIO_GET_CODEC_FAILED NEXPLAYER_STATUS_REPORT_AUDIO_GET_CODEC_FAILED} (0x00000001) </b>
         *              Failed to determine the audio codec.  This notification can happen at the beginning of
         *              playback, or during playback if there is an audio codec change.  This can happen because of a
         *              switch to a new codec that NexPlayer&trade;&nbsp;does not support, or due to an error in the format
         *              of the content or corrupted data in the content.
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_VIDEO_GET_CODEC_FAILED NEXPLAYER_STATUS_REPORT_VIDEO_GET_CODEC_FAILED} (0x00000002) </b>
         *              Failed to determine the video codec.  This notification can happen at the beginning of
         *              playback, or during playback if there is a video codec change.  This can happen because of a
         *              switch to a new codec that NexPlayer&trade;&nbsp;does not support, or due to an error in the format
         *              of the content or corrupted data in the content.
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_AUDIO_INIT_FAILED NEXPLAYER_STATUS_REPORT_AUDIO_INIT_FAILED} (0x00000003) </b>
         *              The audio codec failed to initialize.  This can happen for several reasons.  The container may
         *              indicate the wrong audio codec, or the audio stream may be incorrect or corrupted, or the audio
         *              stream may use a codec version or features that NexPlayer&trade;&nbsp;doesn't support.
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_VIDEO_INIT_FAILED NEXPLAYER_STATUS_REPORT_VIDEO_INIT_FAILED} (0x00000004) </b>
         *              The video codec failed to initialize.  This can happen for several reasons.  The container may
         *              indicate the wrong video codec, or the video stream may be incorrect or corrupted, or the video
         *              stream may use a codec version or features that NexPlayer&trade;&nbsp;doesn't support.
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_TRACK_CHANGED NEXPLAYER_STATUS_REPORT_TRACK_CHANGED} (0x00000005) </b>
         *              The track has changed. This happens for protocols such as HLS that provide the content
         *              in multiple formats or at multiple resolutions or bitrates.  The ID of the new track can
         *              be found in \link NexStreamInformation#mCurrTrackID mCurrTrackID\endlink, and also in \c param1.
         *              <i>When this event occurs, NexPlayer&trade;&nbsp;also generates a eNEXPLAYER_CONTENT_INFO_UPDATED event.</i>
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_STREAM_CHANGED NEXPLAYER_STATUS_REPORT_STREAM_CHANGED} (0x00000006) </b>
         *              The stream being played back has changed (between the states Audio-Only, Video-Only and Audio+Video).
         *              The new stream type is in \link NexContentInformation#mMediaType mMediaType\endlink, and also in \c param1.
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_DSI_CHANGED NEXPLAYER_STATUS_REPORT_DSI_CHANGED} (0x00000007) </b>
         *              An attribute relating to the video or audio format (such as the resolution, bitrate, etc.) has changed. This is
         *              considered Decoder Specific Information (DSI).
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_OBJECT_CHANGED NEXPLAYER_STATUS_REPORT_OBJECT_CHANGED} (0x00000008) </b>
         *              One of the codec objects in use has changed (that is, the audio or video codec in use
         *              has changed).
         *              See \link NexContentInformation#mAudioCodec mAudioCodec\endlink and
         *              \link NexContentInformation#mVideoCodec mVideoCodec\endlink to get the ID of the new codec.
         * @param Note Continued on the next page.
         * @param msg
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_CONTENT_INFO_UPDATED NEXPLAYER_STATUS_REPORT_CONTENT_INFO_UPDATED} (0x00000009) </b>
         *              The content information has changed.  When onStatusReport is called with any other non-Failure
         *              value for \c msg, it will also be called with this as well. This is a good
         *              place to monitor insignificant changes to the content information.
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_DOWNLOAD_PROGRESS NEXPLAYER_STATUS_REPORT_DOWNLOAD_PROGRESS} (0x00000080) </b>
         *              Reports the progress made storing content for offline play.  This message will be called when content is opened with \c NEXPLAYER_SOURCE_TYPE_STORE_STREAM type.
         *              For this notification, the parameter \c param1 returns the percentage of downloading complete (0-100).
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_AVMODE_CHANGED NEXPLAYER_STATUS_REPORT_AVMODE_CHANGED} (0x0000000A) </b>
         *              The stream being played back has changed and the new stream
         *              has a different media type.  This event happens whenever the state changes between
         *              video-only, audio-only and audio-video. \c param1 contains the new media type: 1 for audio, 2 for video, 3 for both.
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_HTTP_INVALID_RESPONSE NEXPLAYER_STATUS_REPORT_HTTP_INVALID_RESPONSE} (0x0000000B) </b>
         *              An HTTP error response was received from the server.  \c param1 contains the error code (this is
         *              a normal HTTP response code, such as 404, 500, etc.)
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_DISCONTINUITY_EXIST NEXPLAYER_STATUS_REPORT_DISCONTINUITY_EXIST} (0x00000012) </b>
         *              There is a discontinuity tag found in the HLS content playlist.
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_EXTERNAL_DOWNLOAD_CANCELED NEXPLAYER_STATUS_REPORT_EXTERNAL_DOWNLOAD_CANCELED} (0x00000020) </b>
         *              The player canceled External PD Mode and played content on Normal PD Mode. (e.g. The engine attempted to play regular content instead of piff content on External PD Mode).
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_MINMAX_BANDWIDTH_CHANGED NEXPLAYER_STATUS_REPORT_MINMAX_BANDWIDTH_CHANGED} (0x00000021) </b>
         *              Either the Minimum bandwidth or Maximum bandwidth has been changed.
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_STREAM_RECV_PAUSE NEXPLAYER_STATUS_REPORT_STREAM_RECV_PAUSE} (0x00000060) </b>
         *              When the prefetch buffer exceeds the maximum value, the player pauses the buffer control. The user can adjust the maximum value of the prefetch buffer by using \link NexPlayer.setProperty setProperty\endlink of the properties \link NexPlayer.NexProperty#MAX_BUFFER_RATE MAX_BUFFER_RATE\endlink or \link NexPlayer.NexProperty#MAX_BUFFER_DURATION MAX_BUFFER_DURATION\endlink.
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_STREAM_RECV_RESUME NEXPLAYER_STATUS_REPORT_STREAM_RECV_RESUME} (0x00000061) </b>
         *              When the prefetch buffer is below the minimum value, the player resumes the buffer control. The user can adjust the mimimum value of the prefetch buffer by using \link NexPlayer.setProperty setProperty\endlink of the properties \link NexPlayer.NexProperty#MIN_BUFFER_RATE MIN_BUFFER_RATE\endlink or \link NexPlayer.NexProperty#MIN_BUFFER_DURATION MIN_BUFFER_DURATION\endlink.
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_DOWNLOAD_PROGRESS NEXPLAYER_STATUS_REPORT_DOWNLOAD_PROGRESS} (0x00000080) </b>
         *              Reports the progress made storing content for offline play.  This message will be called when content is opened with \c NEXPLAYER_SOURCE_TYPE_STORE_STREAM type.
         *              For this notification, the parameter \c param1 returns the percentage of downloading complete (0-100).
         *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_MAX NEXPLAYER_STATUS_REPORT_MAX} (0xFFFFFFFF) </b>
         *              This value is reserved; do not use it.
         *            .
         * @param param1
         *          Additional information.  The meaning of this depends on the value of \c msg.  If the description
         *          above doesn't refer to \c param1, then this parameter is undefined for that value of
         *          \c msg and should not be used.
         */
        void onStatusReport( NexPlayer mp, int msg, int param1);

        /**
         *  \brief  This function is called when an error is generated by the Downloader module.
         *
         * @param mp       The NexPlayer&trade;&nbsp;object to which this event applies.
         * @param msg      An integer indicating the error generated by the Downloader module.
         * @param param1   This parameter is currently undefined but reserved for future use and should not be used.
         *
         */
        void onDownloaderError(NexPlayer mp, int msg, int param1);
        /**
         *  \brief  This function is called when an asynchronous command in the Downloader module is complete.
         *
         *  This method will be called whenever DownloaderOpen(), DownloaderClose(), DownloaderStart() or DownloaderStop()
         *  finish asynchronously.
         *
         * @param mp      The NexPlayer&trade;&nbsp;object to which this event applies.
         * @param msg     The asynchronous command completed.  This will be one of:
         *                   - <b>NEXDOWNLOADER_ASYNC_CMD_OPEN = 0x00200001</b>
         *                   - <b>NEXDOWNLOADER_ASYNC_CMD_CLOSE = 0x00200002</b>
         *                   - <b>NEXDOWNLOADER_ASYNC_CMD_START = 0x00200003</b>
         *                   - <b>NEXDOWNLOADER_ASYNC_CMD_STOP = 0x00200004</b>
         * @param param1  This integer indicates the result of the command.  It will be 0 in the event of
         *                success, or will be an error code in the event of failure.
         * @param param2  Additional information, if available, concerning the result reported in \c param1.  For example
         *                if the error is invalid response, \c param2 gives the HTTP status code.
         *
         */
        void onDownloaderAsyncCmdComplete(NexPlayer mp, int msg, int param1, int param2);

        /**
         * \brief  This method reports when a Downloader event has started.
         *
         *  \param mp      The NexPlayer&trade;&nbsp;object to which this event applies.
         *  \param param1  This parameter is currently undefined and is not used but is reserved for future use.
         *  \param param2  The total size of the content file to be downloaded.
         *
         */
        void onDownloaderEventBegin(NexPlayer mp, int param1, int param2);

        /**
         *  \brief  This function is called to pass the downloading progress of a Downloader event.
         *
         * @param mp      The NexPlayer&trade;&nbsp;object to which this event applies.
         * @param param1  This parameter is currently undefined and is not used but is reserved for future use.
         * @param param2  The time remaining until the downloading file has saved completely, in \c msec (milliseconds).
         * @param param3  The size of the portion of the downloading file already received. in bytes (B).
         * @param param4  The total size of the content file being downloaded, in bytes (B).
         *
         */
        void onDownloaderEventProgress(NexPlayer mp, int param1, int param2, long param3, long param4);
        /**
         * \brief  This function is called when a Downloader event has completed.
         *
         * @param mp      The NexPlayer&trade;&nbsp;object to which this event applies.
         * @param param1  This parameter is currently undefined and is not used but is reserved for future use.
         *
         */
        void onDownloaderEventComplete(NexPlayer mp, int param1);
        /**
         * \brief  This method reports the current state of a Downloader event.
         *
         * @param mp      The NexPlayer&trade;&nbsp;object to which this event applies.
         * @param param1  This parameter is currently undefined and is not used but is reserved for future use.
         * @param param2  This is an integer that indicates the current state of the Downloader event.
         *                This will be one of:
         *                  - <b>NEXDOWNLOADER_STATE_NONE = 0</b>
         *                  - <b>NEXDOWNLOADER_STATE_CLOSED = 2</b>
         *                  - <b>NEXDOWNLOADER_STATE_STOP = 3</b>
         *                  - <b>NEXDOWNLOADER_STATE_DOWNLOAD = 4</b>
         */
        void onDownloaderEventState(NexPlayer mp, int param1, int param2);


        /**
         * \brief  This method reports the dataRange value that is contained in the EXT-X-DATERANGE tag.
         *
         * @param mp    The NexPlayer&trade;&nbsp;object to which this event applies.
         * @param data  The array of NexDateRangeData object that includes dataRange and datacontained in the EXT-X-DATERANGE tag.
         */
        void onDateRangeData(NexPlayer mp , NexDateRangeData[] data);

        /**
         * \brief  This method reports the arbitrary session data of the HLS master playlist.
         *
         * @param mp    The NexPlayer&trade;&nbsp;object to which this event applies.
         * @param data  The array of NexSessionData object that includes the arbitrary session data of the HLS master playlist.
         */
        void onSessionData(NexPlayer mp, NexSessionData[] data);

        /**
         * \brief  This method provides SEI picture timing information about video frames of H.264 content when available.
         *
         * This method is called when \c ENABLE_H264_SEI is enabled and the H.264 content contains supplemental enhancement information (SEI).
         * While SEI may include a variety of attributes, this method specifically receives SEI picture timing information when available.
         *
         * NexPlayer&trade;&nbsp;delivers the timing information through this method by passing an instance of NexPictureTimingInfo
         * whenever SEI picture timing information is received.
         *
         * \param mp 	The NexPlayer&trade;&nbsp;object to which this event applies.
         * \param arrPictureTimingInfo	The NexPictureTimingInfo object that includes the SEI picture timing information for the content.
         *
         * \since version 6.0.5
         */
        void onPictureTimingInfo(NexPlayer mp, NexPictureTimingInfo[] arrPictureTimingInfo);
        /**
         * \brief  This method allows responses from an HTTP server to be received and handled in a more customized way.
         *
         * While NexPlayer&trade;&nbsp;normally handles HTTP requests and responses internally, in cases where additional information is
         * required from the server (for example user cookies), this method can be used in conjunction with \c onHTTPRequest to handle that information directly.
         *
         * \note  This should be called after a response has been received from the server.  To change the requests being made, \c onModifyHttpRequest should be called.
         *
         * \param mp 	The NexPlayer&trade;&nbsp;object to which this event applies.
         * \param strResponse  The response from the HTTP server, as a \c String.
         *
         * \see NexPlayer.onHTTPRequest
         * \see NexPlayer.onModifyHttpRequest
         * \since version 6.12
         */
        void onHTTPResponse(NexPlayer mp, String strResponse);
        /**
         * \brief  This method allows NexPlayer&trade;&nbsp;to pass HTTP request messages to an application.
         *
         * While NexPlayer&trade;&nbsp;normally handles HTTP requests and responses internally, in cases where additional information is
         * required from the server (for example user cookies), this method can be used in conjunction with \c onHTTPResponse to allow an application to handle that information directly.
         *
         * \note  This should be called before a request is sent to an HTTP server. To modify an HTTP request, see \c onModifyHttpRequest. To handle the response received, call \c onHTTPResponse.
         * \param mp  The NexPlayer&trade;&nbsp;object to which this event applies.
         * \param strRequest  The HTTP request to be sent to the server, as a \c String.
         *
         * \see NexPlayer.onHTTPResponse
         * \see NexPlayer.onModifyHttpResponse
         * \since version 6.12
         */
        void onHTTPRequest(NexPlayer mp, String strRequest);


        /**
         * \brief  This method provides the HTTP Request that will be used by NexPlayer&trade;&nbsp;when an HTTP request is modified.
         *
         * \param mp 	The NexPlayer&trade;&nbsp;object to which this event applies.
         * \param  param1      The length of the current HTTP Request data.
         * \param  input_obj   The modified HTTP Request data.
         *
         * \returns  A String with the modified HTTP request.  This value will be used by
         *           NexPlayer&trade;&nbsp;instead of the previous HTTP request.
         *
         * \see \ref enable_mod_http "Enabling Modified HTTP Requests" for more information.
         */
        String onModifyHttpRequest(NexPlayer mp, int param1, Object input_obj);


        /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onDebugInfo(NexPlayer, int, String) onDebugInfo}. */
        //public static final int NEXPLAYER_DEBUGINFO_RTSP            = 0x00;
        /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onDebugInfo(NexPlayer, int, String) onDebugInfo}. */
        //public static final int NEXPLAYER_DEBUGINFO_RTCP_RR_SEND    = 0x01;
        /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onDebugInfo(NexPlayer, int, String) onDebugInfo}. */
        //public static final int NEXPLAYER_DEBUGINFO_RTCP_BYE_RECV   = 0x02;
        /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onDebugInfo(NexPlayer, int, String) onDebugInfo}. */
        //public static final int NEXPLAYER_DEBUGINFO_CONTENT_INFO    = 0x03;

        /**
         * Provides debugging and diagnostic information during playback.  The information provided
         * here is for debugging purposes only; the contents of the strings provided may change in future
         * versions, so do not attempt to parse them or make programmating decisions based on the contents.
         * Also, do not make assumptions about line length or number of lines.
         *
         * <b>Superceded:</b> The relevant information provided in the freeform text strings
         *                    that used to be passed to this method is now available directly
         *                    in NexContentInformation.<p>
         *
         * @param mp
         *          The NexPlayer&trade;&nbsp;object to which this event applies.
         *
         * @param msg
         *          Identifies the type of debugging information being provided.  This is one of the following values:
         * <ul>
         * <li><b>{@link NexPlayer.IListener#NEXPLAYER_DEBUGINFO_RTSP NEXPLAYER_DEBUGINFO_RTSP} (0x00)</b>
         *      Debugging information related to the RTSP connection status.</li>
         * <li><b>{@link NexPlayer.IListener#NEXPLAYER_DEBUGINFO_RTCP_RR_SEND NEXPLAYER_DEBUGINFO_RTCP_RR_SEND} (0x01)</b>
         *      Debugging information associated with the transmission of an RTCP RR (Receiver Report) packet.</li>
         * <li><b>{@link NexPlayer.IListener#NEXPLAYER_DEBUGINFO_RTCP_BYE_RECV NEXPLAYER_DEBUGINFO_RTCP_BYE_RECV} (0x02)</b>
         *      This occurs when an RTCP BYE packet is received.</li>
         * <li><b>{@link NexPlayer.IListener#NEXPLAYER_DEBUGINFO_CONTENT_INFO NEXPLAYER_DEBUGINFO_CONTENT_INFO} (0x03)</b>
         *      General information about the content that is currently playing.  This is intended to be shown
         *      in a "heads-up" style overlay or suplementary display, and replaces information provided in
         *      any previous <code>NEXPLAYER_DEBUGINFO_CONTENT_INFO</code> calls.</li>
         * </ul>
         * @param strDbg
         *      A string containing the debugging information associated with the event.  This may contain
         *      multiple lines of text.
         */
        //void onDebugInfo( NexPlayer mp, int msg, Object obj);


    }

    /**
     * \brief  The application must implement this interface in order to receive
     *         video renderer-specific events from NexPlayer&trade;.
     *
     * \warning  These callbacks may occur in any thread, not
     * necessarily the main application thread. In some cases, it may not
     * be safe to call UI-related functions from within \c IListener
     * callbacks.  The safest way to update the UI is to use \c android.os.Handler
     * to post an event back to the main application thread.
     *
     * \note  This interface replaces the deprecated methods in \link NexPlayer.IListener IListener\endlink that received video renderer-specific
     *        events from NexPlayer&trade;.  Note that in existing older applications, the video renderer related methods of IListener (now deprecated) can be reused.
     *
     * NexPlayer&trade;&nbsp;will call the methods provided in this interface
     * automatically during playback to notify the application when various
     * video renderer-specific events have occurred.
     *
     * In most cases, the handling of these events is optional; NexPlayer&trade;&nbsp;
     * will continue playback normally without the application doing anything
     * special.  There are a few exceptions to this which are listed below.
     *
     * See each individual \c IVideoRendererListener method for a recommendation
     * on how to implement that method in your application.
     *
     * \see NexPlayer.setVideoRendererListener
     * \see NexVideoRenderer
     *
     * \since version 6.1
     */
    public interface IVideoRendererListener
    {
        /**
         * \brief  This method is called when NexPlayer&trade;&nbsp;recognizes which type of video renderer will be used.
         *
         * At first, NexPlayer&trade;&nbsp;does not know which renderer will be used.
         * When this method is called, the application can determine the video renderer mode by calling
         * {@link NexPlayer#GetRenderMode() GetRenderMode}
         * and prepare for the specified video renderer, as in the following example code:
         *     \code
         *     	public void onVideoRenderPrepared(NexPlayer mp) {
         *      if(mNexPlayer.GetRenderMode() == NexPlayer.NEX_USE_RENDER_OPENGL) {
         *      	UseOpenGL = true;
         *      	mHandler.post(new Runnable() {
         *      		public void run() {
         *      			mVideoSurfaceView.setVisibility(View.INVISIBLE);
         *      			int colorDepth = 4;
         *      			if(glRenderer == null)
         *      			{
         *      				glRenderer = new GLRenderer(mContext, mNexPlayer, this, colorDepth);
         *      				FrameLayout view = (FrameLayout)findViewById(R.id.gl_container);
         *      				view.addView(glRenderer);
         *      			}
         *      			else if(mInitGLRenderer == true)
         *      			{
         *      				glRenderer.mReInitRenderer = true;
         *      				glRenderer.requestRender();
         *      			}
         *      			else
         *      			{
         *      				glRenderer.setVisibility(View.VISIBLE );
         *      			}
         *
         *      		}
         *      	});
         *      }
         *      else
         *      {
         *      	UseOpenGL = false;
         *      	mHandler.post(new Runnable() {
         *      		public void run() {
         *      			if(mNexPlayer.GetRenderMode() == NexPlayer.NEX_USE_RENDER_AND)
         *      			{
         *      				mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);//For Gingerbread Android Renderer
         *      			}
         *      			else
         *      			{
         *      				mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//For HW Renderer
         *      			}
         *      			if(glRenderer != null)
         *      			{
         *      				glRenderer.setVisibility(View.INVISIBLE );
         *      				glRenderer = null;
         *      			}
         *      			mVideoSurfaceView.setVisibility(View.VISIBLE); // This invokes nexPlayer.setDisplay(mSurfaceHolderForSW, 0);
         *
         *      		}
         *      	});
         *      }
         *      }
         *     \endcode
         *
         * @param mp
         *            The NexPlayer&trade;&nbsp;object to which this event applies.
         */
        void onVideoRenderPrepared( NexPlayer mp );

        /**
         * \brief  This method is called when NexPlayer&trade;&nbsp;needs the application to create a surface on which
         *         to render the video.
         *
         * The application must respond to this by calling
         * {@link NexPlayer#setDisplay(SurfaceHolder) setDisplay}.
         *
         * Generally speaking, the application will actually create the surface earlier,
         * during GUI layout, and will simply use the existing handle in response to this
         * call.  There are, however, some threading considerations.  See
         * {@link NexPlayer#setDisplay(SurfaceHolder) setDisplay} for details.
         *
         * @param mp
         *            The NexPlayer&trade;&nbsp;object to which this event applies.
         * @param width
         *            The width of the source video.
         * @param height
         *            The height of the source video.
         * @param rgbBuffer
         *            Direct RGB Buffer(RGB565 format).
         *            This RGB buffer is shared with NexPlayer&trade;&nbsp;Engine native code.
         */
        void onVideoRenderCreate( NexPlayer mp, int width, int height, Object rgbBuffer );

        /**
         * \brief This method is called when NexPlayer&trade;&nbsp;no longer needs the render surface.
         *
         * If a surface was created in \c onVideoRenderCreate, this is the
         * place to destroy it.  However, if (as in most cases) an existing surface
         * was used, then this function need not take any special action, other than
         * updating whatever state the application needs to track.
         *
         * @param mp
         *            The NexPlayer&trade;&nbsp;object to which this event applies.
         */
        void onVideoRenderDelete( NexPlayer mp);

        /**
         * \brief  This requests to display Video frame data at JAVA application.
         *
         * \param mp  The NexPlayer&trade;&nbsp;object to which this event applies.
         */
        void onVideoRenderRender( NexPlayer mp );

        /**
         * \brief   Called when a frame of video has been captured.
         *
         * After calling {@link NexPlayer#captureVideo(int, int) captureVideo} to
         * set up video capture, this function will be called whenever a frame is
         * captured, and can process the captured frame as necessary.
         *
         * \code
         * Bitmap bitmap = Bitmap.createBitmap(width, height, pixelbyte==2?Config.RGB_565:Config.ARGB_8888 );
         * ByteBuffer RGBBuffer = (ByteBuffer)rgbBuffer;
         * RGBBuffer.asIntBuffer();
         * bitmap.copyPixelsFromBuffer(RGBBuffer);
         * \endcode
         *
         * @param mp
         *            The NexPlayer&trade;&nbsp;object to which this event applies.
         * @param width
         *            The width of the captured frame.
         * @param height
         *            The height of the captured frame.
         * @param pixelbyte
         *            The number of bytes per pixel (2 for RGB565; 4 for RGBA).
         * @param bitmap
         *            The object where the captured video frame data is stored.
         *
         */
        void onVideoRenderCapture(NexPlayer mp, int width, int height, int pixelbyte, Object bitmap );

    }

    /**
     *
     * \brief  This interface must be implemented in order for the application to receive 
     *  Dynamic Thumbnail events from NexPlayer&trade;.
     *
     * NexPlayer&trade;&nbsp;will call the methods provided in this interface
     * automatically during playback to notify the application when various
     * Dynamic Thumbnail events have occurred.
     *
     * In most cases, the handling of these events is optional; NexPlayer&trade;&nbsp;
     * will continue play content back normally without the application doing anything
     * special in response to the events received.
     *
     * \since version 6.29
     */
    public interface IDynamicThumbnailListener
    {
        /**
         *
         * \brief This method will be called by the NexPlayer&trade;&nbsp;engine when thumbnail data is created.
         *
         * If the \c enableDynamicThumbnail() method is called before Smooth Streaming content is in the \c open state,
         * then this method will be called and gets the thumbnail information associated with the content.  
         *
         * @param mp
         * 				The NexPlayer&trade;&nbsp;object generating the event.
         * @param width
         * 				The width of the thumbnail image.
         * @param height
         * 				The height of the thumbnail image.
         * @param cts
         * 				The current timestamp of the thumbnail image.
         * @param bitmap
         * 				RGB buffer pointer(RGB565) of the thumbnail image.
         *
         * \since version 6.29
         */
        void onDynamicThumbnailData( NexPlayer mp, int width, int height, int cts, Object bitmap);

        /**
         *
         * \brief This callback method informs the Dynamic Thumbnail listener when the end of thumbnail data is received.
         *
         * @param mp  The NexPlayer&trade;&nbsp;object generating the event.
         *
         * \since version 6.29
         */
        void onDynamicThumbnailRecvEnd( NexPlayer mp );
    }

    /**
     * \brief This method sets and registers a \c IDynamicThumbnailListener listener. 
     *
     * @param listener		IDynamicThumbnailListener
     *
     * \see NexPlayer.IDynamicThumbnailListener
     *
     * \since version 6.29 
     */
    public void setDynamicThumbnailListener(IDynamicThumbnailListener listener)
    {
        if( !mNexPlayerInit )
        {
            NexLog.d(TAG, "Attempt to call setListener() but player not initialized; call NexPlayer.init() first!");
        }
        if(listener != null) {
            NexLog.d(TAG, "add setDynamicThumbnailListener");
            mEventForwarder.addReceiver(listener);
        }
    }

    /**
     * \brief  This interface can be implemented in an application in order to receive
     *          NexPlayer&trade;&nbsp;release events from NexPlayer&trade;.
     *
     * In most cases, the handling of these events is optional; NexPlayer&trade;&nbsp;
     * will continue playback normally without the application doing anything
     * special in response to the events received.
     *
     * \see NexPlayer.addReleaseListener
     * \see NexPlayer.removeReleaseListener
     *
     * \since version 6.23
     */
    public interface IReleaseListener
    {
        void onPlayerRelease(NexPlayer mp);
    }

    /**
     * \brief This method sets and registers a \c IHTTPABRTrackChangeListener listener. 
     *
     * @param listener      IHTTPABRTrackChangeListener
     *
     * \see NexPlayer.IHTTPABRTrackChangeListener
     *
     * \since version 6.44
     */
    public void setHTTPABRTrackChangeListener(IHTTPABRTrackChangeListener listener)
    {
        if( !mNexPlayerInit )
        {
            NexLog.d(TAG, "Attempt to call setListener() but player not initialized; call NexPlayer.init() first!");
        }
        if(listener != null) {
            NexLog.d(TAG, "add setHTTPABRTrackChangeListener");
            mEventForwarder.addReceiver(listener);
        }
    }

    /**
     * \brief This interface must be implemented in order for the application to receive 
     *  an ABR Track switch event from NexPlayer&trade;&nbsp;.
     *
     * NexPlayer&trade;&nbsp; will call the methods provided in this interface
     * automatically during playback to notify the application when an
     * ABR Track switch event has occurred.
     *
     * In most cases, the handling of this event is optional; NexPlayer&trade;&nbsp;
     * will continue to play content normally without the application doing anything
     * special in response to the event received.
     *
     * \note To use the method defined by this interface, NexProperty.ENABLE_HTTPABRTRACKCHANGE_CALLBACK should be enabled before calling open.
     *
     * \since version 6.44
     */
    public interface IHTTPABRTrackChangeListener
    {
        /**
         * \brief This method will be called by the NexPlayer&trade;&nbsp; engine when the ABR track is switched.
         *
         * @param mp
         *               The NexPlayer&trade;&nbsp; object generating the event.
         *
         * @param param1
         *               The current network bandwidth.
         *
         * @param param2
         *               The current track bandwidth.
         *
         * @param param3
         *               The target track bandwidth.
         *
         * \return  The exact track bandwidth that the user wants to set to forcibly.
         * \since version 6.44
         */
        int onHTTPABRTrackChange(NexPlayer mp, int param1, int param2, int param3);
    }

    /**
     * This is a possible value for MetaData sub event*/
    static final int NEXPLAYER_METADATA_EMSG = 0x01;
    static final int NEXPLAYER_METADATA_HLS_FIRST_PROGRAM_DATE_TIME = 0x02;
    static final int NEXPLAYER_METADATA_DASH_SCTE35 = 0x03;

    interface IMetaDataEventListener {
        /**
         * \brief  This method reports the Emsg information that is contained in the emsg box.
         *
         * @param mp    The NexPlayer&trade;&nbsp;object to which this event applies.
         * @param data  NexEmsgData object that includes information of the emsg box.
         */
        void onEmsgData(NexPlayer mp, NexEmsgData data);

        /**
         * \brief  This method reports the HLS first program date time information.
         *
         * @param mp    The NexPlayer&trade;&nbsp;object to which this event applies.
         * @param str   HLS first program date time string data.
         */
        void onHlsFirstProgramDateTime(NexPlayer mp, String str);

		/**
		 * \brief  This method reports the scte35 information that is contained in the EventStream.
		 *
		 * @param mp		The NexPlayer&trade;&nbsp;object to which this event applies.
		 * @param data  	List of the NexEmsgData object that includes scte35 information of the EventStream.
		 */
		void onDashScte35Event(NexPlayer mp, NexEmsgData[] data);
    }

    /**
     * \brief  This method sets the size of the file being downloaded in the Downloader module.
     *
     * \param  ReceivedSize  The size of portion of the file received so far, in bytes (B).
     * \param  TotalSize     The total size of the file being downloaded, in bytes (B).
     *
     * \returns  Zero if successful, another value in the case of failure.
     */
    public native int SetExternalPDFileDownloadSize(long ReceivedSize, long TotalSize);

    //  APIs of Downloader must be called after init().
    /**
     * This is a possible value for the parameter \c eType in the method DownloaderOpen(). */
    public static final int NEXDOWNLOADER_OPEN_TYPE_CREATE = 0;
    /** This is a possible value for the parameter \c eType in the method DownloaderOpen(). */
    public static final int NEXDOWNLOADER_OPEN_TYPE_APPEND = 1;


    /** This is a possible value for the parameter \c param2 of {@link IListener#onDownloaderEventState(NexPlayer mp, int param1, int param2) onDownloaderEventState}.  */
    public static final int NEXDOWNLOADER_STATE_NONE = 0;
    /** This is a possible value for the parameter \c param2 of {@link IListener#onDownloaderEventState(NexPlayer mp, int param1, int param2) onDownloaderEventState}.  */
    public static final int NEXDOWNLOADER_STATE_CLOSED = 2;
    /** This is a possible value for the parameter \c param2 of {@link IListener#onDownloaderEventState(NexPlayer mp, int param1, int param2) onDownloaderEventState}.  */
    public static final int NEXDOWNLOADER_STATE_STOP = 3;
    /** This is a possible value for the parameter \c param2 of {@link IListener#onDownloaderEventState(NexPlayer mp, int param1, int param2) onDownloaderEventState}.  */
    public static final int NEXDOWNLOADER_STATE_DOWNLOAD = 4;


    /**
     * \brief  This method is called to open a new event in the Downloader module.
     *
     * The Downloader module allows users to download and save streaming progressive download (PD) content in MP4 containers so that
     * it can be viewed at a later time.  It must be opened before opening the content to be downloaded and calling \c NexPlayer.open().
     *
     * Note that this method cannot be called until after initializing NexPlayer&trade;&nbsp;by calling init().
     *
     * @param strUrl        This is a string passing the URL to the content to be downloaded.
     * @param strStorePath  This is a string indicating the path to where the downloaded file is saved.
     * @param proxyPath     This is a string indicating the path to the proxy server.
     * @param proxyPort     This is an integer indicating the port to use on the proxy server.
     * @param eType         This is an integer indicating the type of event being opened.  It will be one of:
     *                        - <b>NEXDOWNLOADER_OPEN_TYPE_CREATE = 0 </b>:  This creates a new Downloader event.
     *                        - <b>NEXDOWNLOADER_OPEN_TYPE_APPEND = 1 </b>:  This appends newly downloaded information to
     *                          an existing file already begun.  Note that not every server will support APPEND
     *                          events so this should only be used conditional on the content server.
     *
     * \return  Zero if successful, or an error code in the event of failure.
     */
    public native int DownloaderOpen(String strUrl, String strStorePath, String proxyPath, int proxyPort, int eType );

    /**
     * \brief  This method is called to close a Downloader event.
     *
     * Note that this method cannot be called until after initializing NexPlayer&trade;&nbsp;by calling init().  Anytime DownloaderOpen()
     * is called, this method must also be called properly close the Downloader module.  If a call is also made to
     * DownloaderStart(), then DownloaderStop() must be called BEFORE calling DownloaderClose() to properly end the event.
     *
     * \return  Zero if successful, or an error code in the event of failure.
     */
    public native int DownloaderClose( );
    /**
     * \brief  This method is called to start downloading and saving content in a Downloader event.
     *
     * Note that this method cannot be called until after both initializing NexPlayer&trade;&nbsp;by calling init() and
     * opening an event in the Downloader module with DownloaderOpen().
     *
     * \return  Zero if successful, or an error code in the event of failure.
     */
    public native int DownloaderStart( );

    /**
     * \brief  This method is called to stop downloading and saving content in a Downloader event.
     *
     * Note that this method cannot be called until after initializing NexPlayer&trade;&nbsp;by calling init().  Anytime a
     * call is made to DownloaderStart(), it must be matched by a call to this method to properly stop and finish
     * a Downloader event.
     *
     * \return  Zero if successful, or an error code in the event of failure.
     */
    public native int DownloaderStop( );

    /**
     * \brief  This method is called to get information about a Downloader event.
     *
     * Note that this method cannot be called until after initializing NexPlayer&trade;&nbsp;by calling init().
     *
     * \param  info  The Downloader object to get information about.
     *
     *
     */
    public native int DownloaderGetInfo( Object info);

    private native int getSeekableRange(long[] info);

    private static long[] arrLongInfo = new long[4];

    /**
     * \brief  This method returns the range of the current content that is seekable.
     *
     * This method is used to allow NexPlayer&trade;&nbsp;to support timeshifting playback within HLS Live and
     * Smooth Streaming content.  Based on the amount of content available from the server at a particular time,
     * it determines the seekable range within the playing content which also indicates the range where playback may
     * be timeshifted.  This range will be constantly shifting as the live streaming content available from the server
     * changes in real time, so this method will need to be repeatedly called to ensure accurate shifting of playback.
     *
     * For local content this method will always return the same two values, and the second value indicating the end
     * of the seekable range will continuously change in progressive download (PD) content, but this method is
     * most relevant when playing live streaming content, as with HLS and Smooth Streaming.
     *
     * For more information about how this method may be used to timeshift playback in live content, please
     * also refer to the introductory section on \ref timeshift "time shift support".
     *
     * \returns  An array of two \c longs, the first \c long being the timestamp indicating the start of the seekable range
     *           and the second being the timestamp indicating the end of the seekable range.
     */

    public long[] getSeekableRangeInfo()
    {
        //long[] info = new long[2];
        int nRet = 0;
        nRet = getSeekableRange( arrLongInfo );
        NexLog.w(TAG, "getSeekableRange. return:"+nRet);
        if(nRet != 0)
        {
            return null;
        }

        return arrLongInfo;
    }

    /**
     * \brief  This method returns the name of the NexPlayer&trade;&nbsp;SDK in use.
     *
     * It can be used for confirmation and for debugging purposes but should generally
     * be ignored.
     *
     * \returns The name of the NexPlayer&trade;&nbsp;SDK as a \c String.
     */
    public native String getSDKName( );

    /**
     * \brief  This method returns the nearest I-Frame timestamp in front of the target position when seeking.
     *
     * It can get the nearest timestamp of I-Frame in front of target position to use highlight function.
     *
     * \param targetTS	 A pointer to the timestamp of the target position.
     *
     * \returns Zero or a positive number if successful, a negative number if there was an error.
     *
     */
    public native int GetNearestIFramePos(int targetTS);

    /**
     * \brief This method retrieves the SAR (Sample Aspect Ratio) of H.264 content when specified.
     *
     * The sample aspect ratio (SAR) returned by this method is expressed as a ratio of the width of the sample size to the height of the sample
     * size.  It can be used to appropriately display content to the user.
     *
     * This sample aspect ratio will be one of the following ratios: \n
     * 1:1, 4:3, 3:2, 2:1, 12:11, 10:11, 40:33, 24:11, 20:11, 32:11, 80:33, 18:11, 15:11, 64:33, or 160:99.\n
     * Note that if SAR information is not specified for given H.264 content, the
     * returned ratio will also be 1:1.
     *
     * To retrieve the SAR information as integers, the method \link NexPlayer.getSARInfo(int[]) getSARInfo(int[] info)\endlink should be called instead.
     *
     * For more information about the SAR information included in H.264 content, please consult Table E-1 - Meaning of Sample Aspect Ratio Indicator
     * on page 374 of the H.264 specifications (Rec. ITU-T H.264 (03/2010).
     *
     * \returns  The sample aspect ratio of the content as a \c String, for example "1:1".
     *
     * \see getSARInfo(int[])
     *
     * \since version 6.1
     */
    public native String getSARInfo();

    /**
     * \brief This method can be used to retrieve the SAR (Sample Aspect Ratio) information of H.264 content as separate integers.
     *
     * To receive the SAR as a \c String, the method \link NexPlayer.getSARInfo() getSARInfo()\endlink should be called instead.
     * The first element of the getSARInfo integer array will be the aspect width of the sample and the second element will be the aspect height
     * of the sample.
     *
     * \param info  An integer array of 2 elements where the first is the aspect width of the sample and the second is the height.
     *
     * \see getSARInfo()
     *
     * \since version 6.1
     */

    public void getSARInfo(int[] info)
    {
        String strSARInfo = getSARInfo();
        int sarIterator = strSARInfo.indexOf(":");
        info[0] = Integer.parseInt(strSARInfo.substring(0, sarIterator));
        info[1] = Integer.parseInt(strSARInfo.substring(sarIterator+1));
        NexLog.w(TAG, "SAR information : W : " + info[0] + " H : " + info[1]);
    }

    /**
     * \brief  This method gets the current play time position of NexPlayer&trade;&nbsp;in the given content.
     *
     * This method can be called at any time to check the current position.
     *
     * \returns The current play time position in \c msec (milliseconds).
     *
     * \since version 6.2.0
     */
    public native int getCurrentPosition();

    /**
     * \brief This class handles the date and time information included in the #EXT-X-PROGRAM-DATE-TIME tag in HLS content.
     *
     * It can be used with the method \link getProgramTime to determine when an HLS segment should be played or to help sync content
     * and associated text streams.
     *
     * \since version 6.4
     */
    public static class PROGRAM_TIME{

        /** This is the current #EXT-X-PROGRAM-DATE-TIME tag from HLS content as a \c String. */
        String m_strTAG;
        /** This is the time offset of the currently decoded frame's timestamp compared to the #EXT-X-PROGRAM-DATE-TIME tag information, in milliseconds (ms)*/
        long m_Offset;
        /** This is the PROGRAM_TIME constructor.*/
        public PROGRAM_TIME() {
            // TODO Auto-generated constructor stub
        }
        /** This method gets the HLS tag, #EXT-X-PROGRAM-DATE-TIME, if included in HLS content. */
        public String getTAG()
        {
            return m_strTAG;
        }
        /** This method gets the time offset of the currently decoded frame's timestamp with respect to time information in the #EXT-X-PROGRAM-DATE-TIME tag. */
        public long getOffset()
        {
            return m_Offset;
        }
        /** This method sets the #EXT-X-PROGRAM-DATE-TIME tag for the current HLS content.  */
        public void setTAG(String tag)
        {
            m_strTAG = tag;
        }
        /** This method sets the time offset from the most recent #EXT-X-PROGRAM-DATE-TIME tag in HLS content.
         *
         * \param offset  The time offset of the current frame from the most recent #EXT-X-PROGRAM-DATE-TIME tag time, in milliseconds.
         */
        public void setOffset(long offset)
        {
            m_Offset = offset;
        }
    }

    /**
     * \brief This method gets the date and time information in HLS content when the HLS tag, #EXT-X-PROGRAM-DATE-TIME, is included.
     *
     * The same information is also returned approximately once a second by the IListener event, onProgramTime.  It can be used
     * to determine the current time of the frame and help when syncing content and text or when determining when to play text.
     *
     * \param time  The program time information of the current decoding frame as a PROGRAM_TIME object.
     *
     * \returns Always zero.
     *
     * \see PROGRAM_TIME for more details
     * \see IListener.onProgramTime
     *
     * \since version 6.4
     */
    public int getProgramTime(PROGRAM_TIME time)
    {
//		time.setTAG(getProgramTimeTag());
//		time.setOffset(getProgramTimeOffset());
        byte[] temp = new byte[1024];
        long[] offset = new long[1];
        int[] len = new int[1];
        int ret = getProgramTimeInternal(temp, len, offset);
        if(ret == 0)
        {
            time.setTAG(new String(temp, 0, len[0]));
            time.setOffset(offset[0]);
        }
        else
        {
            NexLog.d(TAG, "Has no Program Date Time!");
        }
        return 0;
    }

    @Deprecated
    private native long getProgramTimeOffset();
    @Deprecated
    private native String getProgramTimeTag();
    /**
     * getProgramTimeOffset and getProgramTimeTag is deprecated. Please do not use that function. You can use getProgramTimeInternal instead of them.
     * */
    private native int getProgramTimeInternal(byte[] tag, int[] len, long[] offset);
    /**
     * \brief  This method sets the debugging log levels related to codecs, rendering, and protocols in NexPlayer&trade;.
     *
     * \warning  Calls to this method should be made after calling \c init but before calling \c open.
     *
     * By default, the parameters \c codecLog and \c RendererLog are set to -1 so that they are hidden and \c protocol_Log is set to 0 so
     * basic logs are produced.
     * However when debugging applications, they can be set to a higher integer value so that more logs are generated.
     *
     * \param codecLog  The level of logs to be generated related to codecs, as an integer.
     * \param RendererLog  The level of logs to be generated related to rendering, as an integer.
     * \param protocol_Log  The level of logs to be generated related to protocols, as an integer.
     *
     * \returns Zero if successful or a non-zero error code.
     * \since version 6.5
     */
    public native int setDebugLogs(int codecLog, int RendererLog, int protocol_Log);
    /**
     * \brief This is a possible value for the \c option parameter in setVideoBitrates(int [] bitrates, int option).
     *
     * \since version 6.7
     */
    public static final int  AVAILBITRATES_NONE = 0x00000000;		  // Default. No restriction.


    /**
     * \brief This is a possible value for the \c option parameter in setVideoBitrates(int [] bitrates, int option).
     *
     * \since version 6.7
     */
    public static final int  AVAILBITRATES_MATCH = 0x00000001;		  // Only use the tracks which have exact same bitrate.

    /**
     * \brief This is a possible value for the \c option parameter in setVideoBitrates(int [] bitrates, int option).
     *
     * \since version 6.7
     */
    public static final int  AVAILBITRATES_NEAREST	= 0x00000002	;	  // Only use the tracks which have bitrate close to the target bitrate described in the list.

    /**
     * \brief This is a possible value for the \c option parameter in setVideoBitrates(int [] bitrates, int option).
     *
     * \since version 6.7
     */														  //
    public static final int  AVAILBITRATES_HIGH	= 0x00000003	;		  // Only use the tracks which have bitrate equal or higher than target bitrate.

    /**
     * \brief This is a possible value for the \c option parameter in setVideoBitrates(int [] bitrates, int option).
     *
     * \since version 6.7
     */														  // The first bitrate in the list is target bitrate, the rest will be ignored.
    public static final int  AVAILBITRATES_LOW	= 0x00000004;		  // Only use the tracks which have bitrate equal or lower than target bitrate.

    /**
     * \brief This is a possible value for the \c option parameter in setVideoBitrates(int [] bitrates, int option).
     *
     * \since version 6.7
     */														  // The first bitrate in the list is target bitrate, the rest will be ignored.
    public static final int  AVAILBITRATES_INSIDERANGE	= 0x00000005	; // Only use the tracks which have bitrate inside the range. (lower boundary <= bitrate <= upper boundary.)
    // The first bitrate in the list is lower boundary, the second is upper boundary, the rest will be ignored.

    /**
     * \brief This method allows specific subtracks to be selected and played based on the bitrates of the tracks in HLS content.
     *
     * Only the tracks with the bitrates passed to this method with the parameter \c bitrates will be played by NexPlayer&trade;.
     *
     * \param bitrates  The bitrates of the HLS content subtracks to play, as an integer array.
     *
     * \returns  Zero if successful or a non-zero error code.
     *
     * \since version 6.5.2
     */

    public int setVideoBitrates(int [] bitrates)
    {
        return setVideoBitrates(bitrates, bitrates.length, AVAILBITRATES_NEAREST);
    }
    /**
     * \brief This method allows specific subtracks to be selected and played based on the bitrates of the tracks in HLS content.
     *
     * Only the tracks with the bitrates passed on this method with the parameter \c bitrates will be played by NexPlayer&trade;.  However,
     * choosing a different option with the parameter \c option allows NexPlayer&trade;&nbsp;to choose and play the selected subtracks based on
     * the passed bitrates differently.
     *
     * \param bitrates The bitrates of the HLS content subtracks to play, as an integer array.
     * \param option   How HLS subtracks should be played based on the bitrates selected in \c bitrates.  This will be one of:
     * 					-  AVAILBITRATES_NONE = 0x00000000: Default.  No restriction on subtracks other than the bitrates selected in \c bitrates.
     * 					-  AVAILBITRATES_MATCH = 0x00000001: Only use subtracks which have <em>exact</em> same bitrate as the selected bitrates passed in \c bitrates.
     * 					-  AVAILBITRATES_NEAREST = 0x00000002: Only use subtracks which have the nearest bitrates to the target bitrates described in the list passed in \c bitrates.
     * 						For example, if the target bitrates passed are [300K, 600K] and the HLS playlist includes 100K, 200K, 500K, 700K tracks, only the 200K (close to 300K) and 500K (close to 600K) tracks will be used.
     * 					-  AVAILBITRATES_HIGH = 0x00000003: Only use subtracks which have bitrates equal to or higher than the target bitrate.  The first bitrate in the list passed in \c bitrates is the target bitrate, the rest will be ignored.
     * 					-  AVAILBITRATES_LOW = 0x00000004: Only use subtracks which have bitrates equal to or lower than the target bitrate.  The first bitrate in the list passed in \c bitrates is the target bitrate, the rest will be ignored.
     * 					-  AVAILBITRATES_INSIDERANGE = 0x00000005: Only use subtracks which have bitrates inside the range defined by the bitrates passed in \c bitrates. The first bitrate in the list is taken as the lower boundary, the second as the upper boundary, and the rest of the list will be ignored. Subtracks with bitrates between the lower and upper boundaries will be used.
     *
     * \returns  Zero if successful or a non-zero error code.
     *
     * \since version 6.7
     */
    public int setVideoBitrates(int [] bitrates, int option)
    {
        return setVideoBitrates(bitrates, bitrates.length, option);
    }
    private native int setVideoBitrates(int[] bitrates, int count, int option);


    /**
     * \brief  This method adds a client module to NexPlayer&trade;.
     *
     * \warning Do not call this method when playing local content. It should only be used for streaming content.
     *
     * Note that client modules are optional features of the NexPlayer&trade;&nbsp;SDK.  For more information
     * on how to integrate and use a client module available in the SDK (as well as sample code), please see the relevant \c NexXXXClient class.
     *
     * \param client
     *        The instance of the client module as a \c NexClient object.
     *
     * \returns The client ID as an integer if successful, or -1 in the event of failure.
     *          Note that if no client module is available in the current version of the SDK, -1 will be returned.
     *
     * \see removeNexClient
     * \see getClientStatus
     *
     * \since version 6.10
     */
    public int addNexClient(NexClient client) {
        return mClientManager.addClient(client);
    }

    /**
     * \brief This method removes a client module integrated into the NexPlayer&trade;&nbsp;SDK.
     *
     * Note that client modules are optional features of the NexPlayer&trade;&nbsp;SDK.  For more information
     * on how to integrate and use a client module available in the SDK (as well as sample code), please see the relevant \c NexXXXClient class.
     *
     * \warning  When NexPlayer&trade;&nbsp;is released, the client will be removed automatically.
     *
     * \param client_id  The ID of the client module as an integer.  This is the return value of \c addNexClient().
     *
     * \returns  The removed client module as a \c NexClient object.
     *
     * \see addNexClient
     * \see getClientStatus
     *
     * \since version 6.10
     */
    public NexClient removeNexClient(int client_id) {
        return mClientManager.removeClient(client_id);
    }

    /**
     * \brief This method gets the current status of a client module integrated into the NexPlayer&trade;&nbsp;SDK.
     *
     * \warning  Note that client modules are optional features of the NexPlayer&trade;&nbsp;SDK.  For more information
     * on how to integrate and use a client module available in the SDK (as well as sample code), please see the relevant \c NexXXXClient class.
     *
     * \param id
     *        The client ID as an integer.
     *
     * \returns  0 if the status of the client is normal, -1 if the client has an error, and 1 if the client module has not been initialized.
     *
     * \since version 6.10
     *
     * \see addNexClient
     * \see removeNexClient
     */
    public int getClientStatus(int id) {
        return mClientManager.getStatus(id);
    }

    private native int getRTStreamInfo( Object info );

    /**
     *
     * \brief  This method enables the client time shift feature in the NexPlayer&trade;&nbsp;SDK.
     *
     * Time shifting is a feature to store realtime data as it is received so that the user can watch past data while playing live content.
     * When this feature is enabled, NexPlayer&trade;&nbsp;prepares for the time shift and only after \c pause is called, it stores received data from a live stream in local file storage with the specified
     * buffer size and duration set with this API. As a result, the user can seek and pause even when the content playing is live.
     * This method will be stopped when the method \c gotoCurrentLivePosition is called.
     *
     * \note This method should be called between calls to \c init() and \c open().  Furthermore, once \c open() is called, the parameter values cannot be changed unless
     * the application is closed by calling \c close() and then initialized with \c init() again.
     *
     * \warning Values for the \c uiTimeShiftBufferSize and \c uiTimeShiftDuration parameter should be set carefully because each device has different capabilities and the
     *          NexPlayer&trade;&nbsp;SDK can't guarantee that the device has enough free space to store data as the user specifies.
     *          If storage becomes full and there is no available space to save the new data, NexPlayer&trade;&nbsp;will force the content to resume
     *          to use up saved data and make more storage space available. When the player resumes, \link NexPlayer.IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete\endlink is called.
     *
     * \param bEnable	                Sets whether to enable or disable the time shift feature.
     * \param strFileBufferPath	        The folder name where temporary data for time shifting will be stored when there is no more storage available in the memory buffer.
     *                                   If there is enough memory and no folder is needed, this parameter will be \c NULL.
     * \param uiTimeShiftBufferSize	    Size of the memory buffer for the time shift feature in megabytes (MB). If the size set to this parameter is bigger than the actual
     *                                   memory available, extra realtime data will be saved in the folder set by the parameter \c strFileBufferPath.
     * \param uiTimeShiftDuration	    Maximum duration for the time shift feature, or how much time a user can shift back in live content in minutes.
     * \param uiMaxBackwardDuration	    Maximum duration for the streaming data backup feature, or how much time (of streamed data files preceding the current \c ts file) can be stored.
     *                                   Normally, streamed data files preceding the latest \c ts file will be deleted to maintain the allowed memory buffer size.
     *                                   However this parameter allows the streamed data files preceding the latest \c ts file to be kept and stored if there is free space available.
     *                                   For example when the available memory buffer is larger than the value set for the parameters \c uiTimeShiftBufferSize or \c uiTimeShiftDuration,
     *                                   this parameter allows users to seek backwards in live content during playback for the specified amount of time.
     *                                   If this parameter is set to the default value 1, streamed data files for the 1 minute preceding the latest \c ts file will be kept.
     *                                   If this parameter is set to 0, all remaining storage space will be available to keep previously streamed data files.
     *                                   When this parameter is set to 0 and the parameter \c strFileBufferPath is set to save streamed data in another storage folder, this parameter will be automatically set to 1.
     *
     *
     * \return  Zero if successful, or an error code in the event of failure.
     *
     * \since version 6.27
     *
     * \see gotoCurrentLivePosition
     */
    public int setClientTimeShift(boolean bEnable, String strFileBufferPath, int uiTimeShiftBufferSize, int uiTimeShiftDuration)
    {
        return setClientTimeShift(bEnable, strFileBufferPath, uiTimeShiftBufferSize, uiTimeShiftDuration, 1);
    }

    public native int setClientTimeShift(boolean bEnable, String strFileBufferPath, int uiTimeShiftBufferSize, int uiTimeShiftDuration, int uiMaxBackwardDuration);

    /**
     *  \brief This is one possible option property for the Dynamic Thumbnail feature in Smooth Streaming content and a possible value of the \c option parameter of \c setOptionDynamicThumbnail.
     *
     * \since 6.29
     */
    public static final int OPTION_DYNAMIC_THUMBNAIL_INTERVAL = 1;

    /**
     * \brief This method is used to enable and apply the Dynamic Thumbnail feature for Smooth Streaming content.
     *
     * Refer to the following steps to use this method accurately:
     *
     * -# The \c enableDynamicThumbnail() method should be called before \c NexPlayer.open.
     * -# When \c open completes, use the \c getContentInfo() method to get the total playtime of the content.
     *    By dividing the extracted total playtime value by the number of the thumbnail buffer array from the UI (the number of available thumbnails), the interval time is determined.
     *    The interval time can then be used with the \c setOptionDynamicThumbnail() method to get thumbnail information.
     * -# If the setting above works normally, NexPlayer&trade;&nbsp;will use the \c onDynamicThumbnailData() method to send thumbnail data to the UI.
     * -# The \c disableDynamicThumbnail() method should be called before \c NexPlayer.close when closing content.
     * -# If a video track is changed while content is playing, these methods should be called in the following order:
     *       - FIRST, \c disableDynamicThumbnail()
     *       - SECOND, \c enableDynamicThumbnail() to enable Dynamic Thumbnails for the new content, and
     *       - LASTLY, \c setOptionDynamicThumbnail(OPTION_DYNAMIC_THUMBNAIL_INTERVAL, interval_time, 0) to set the appropriate interval for the new thumbnails.
     *
     * \return  Zero if successful, or an error code in the event of failure.
     *
     * \since version 6.29
     */
    public native int enableDynamicThumbnail();

    /**
     *
     * \brief This method disables the Dynamic Thumbnail feature, if enabled.
     *
     * \warning The Dynamic Thumbnail feature <em>must</em> be disabled by calling this method before calling \c NexPlayer.close when a player is being closed.
     *
     * \returns  Zero if successful, or an error code in the event of failure.
     *
     * \since version 6.29
     */
    public native int disableDynamicThumbnail();

    /**
     *
     * \brief This method sets option parameters related to the Dynamic Thumbnail feature in Smooth Streaming when handling thumbnail data.
     *
     * @param option	The \c option property to set thumbnail data.
     * @param param1	The first parameter of the \c option property.
     * @param param2	The second parameter of the \c option property. If the option being set only needs one parameter, \c param2 will be \c NULL.
     *
     * \return  Zero if successful, or an error code in the event of failure.
     *
     * \since version 6.29
     */
    public native int setOptionDynamicThumbnail(int option, int param1, int param2);


    protected native int setTargetBandWidth(int iTargetBandwidth, int iSegmentOption, int iTargetOption);


    protected native int setABREnabled(boolean enabled);


    protected NexRTStreamInformation getRTStreamInfo() {
        NexRTStreamInformation info = new NexRTStreamInformation();
        if ( getRTStreamInfo(info) != 0 ) {
            return null;
        }
        return info;
    }



    /**
     * \note  For internal use only.
     */
    protected class NexRTStreamInformation {
        /* Full content of the master manifest file */
        protected String mMasterMpd;
        /* Master manifest url */
        protected String mMasterMpdUrl;
        /* Full content of the initial manifest file */
        protected String mInitialMpd;
        /* The actual URI(after all redirects) for the request of the manifest file. */
        protected String mInitialMpdUrl;
        /* The description of the initially selected profile */
        protected String mStartSegUrl;
        /* The actual bitrate in kilobit/s of the read segments. The read bitrate is the speed at which the segments are read form the network. */
        protected long mCurNetworkBw;
        /* The bitrate in kilobit/s as specified in the profile for the read segemtns. */
        protected long mCurTrackBw;
        /* The Total number of redirects. This includes redirects for both the manifest file and the individual segments. */
        protected long mNumOfRedirect;
        /* The number of segments that has an actual read bitrate below the bitrate specified in the profile.
         The read bitrate is the speed at which the segments are read from the network.*/
        protected long mNumOfSegDownRate;
        /* The number of segment reads resulting in failures such as HTTP errors */
        protected long mNumOfSegFailToParse;
        /* The number of segment in buffer */
        protected long mNumOfSegInBuffer;
        /* The number of received segments */
        protected long mNumOfSegReceived;
        /* The number of segments fail to receive */
        protected long mNumOfSegFailToReceive;
        /* The number of requested segments */
        protected long mNumOfSegRequest;
        /* The number of segment reads resulting in a timeout */
        protected long mNumOfSegTimeout;
        /* The number of times the content profile has changed to a profile with a lower bitrate */
        protected long mNumOfTrackSwitchDown;
        /* The number of times the content profile has changed to a profile with a higher bitrate */
        protected  long mNumOfTrackSwitchUp;
        /* The number of bytes received */
        protected  long mNumOfBytesRecv;

        protected NexRTStreamInformation() {
            mMasterMpd = null;
            mMasterMpdUrl = null;
            mInitialMpd	= null;
            mInitialMpdUrl = null;
            mStartSegUrl = null;
            mCurNetworkBw = 0;
            mCurTrackBw = 0;
            mNumOfRedirect = 0;
            mNumOfSegDownRate = 0;
            mNumOfSegFailToParse = 0;
            mNumOfSegInBuffer = 0;
            mNumOfSegReceived = 0;
            mNumOfSegFailToReceive = 0;
            mNumOfSegRequest = 0;
            mNumOfSegTimeout = 0;
            mNumOfTrackSwitchDown = 0;
            mNumOfTrackSwitchUp = 0;
            mNumOfBytesRecv = 0;
        }
    }

    /**
     * \brief This method sets a table and table type to be used by the NexPlayer&trade;&nbsp;to retrieve host information when custom IP addresses should be used.
     *
     * \param table                  The table to use, containing the hostname and its corresponding custom IP address.
     * \param nNetAddrTableType      The type of table to be used, setting different options of how to retrieve an IP address with the given host information.
     *                               This should be one of:
     *                                   - \link NexNetAddrTable#NETADDR_TABLE_OVERRIDE  NETADDR_TABLE_OVERRIDE\endlink:  The table will override existing host database information.
     *                                   - \link NexNetAddrTable#NETADDR_TABLE_FALLBACK  NETADDR_TABLE_FALLBACK\endlink:  The table will be used as a fallback option to retrieve host information.
     * \return                      0 if successfully set; non-zero if there was an error.
     *
     * \since version 6.38
     */

    public int setNetAddrTable(NexNetAddrTable table, int nNetAddrTableType)
    {
        return setNetAddrTableInternal(table, nNetAddrTableType);
    }

    private native int setNetAddrTableInternal(Object obj, int eType);

    /**
     * For internal use only. Please do not use.
     */
    public int getCurrentSoundEffect()
    {
        return getCurrentSoundEffectInternal();
    }

    private native int getCurrentSoundEffectInternal();

    /**
     * For internal use only. Please do not use.
     */
    public native int dummyAPI(int apiidx, String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8);

    /**
     * For internal use only. Please do not use.
     */
    public native int forceCallDRMCallback(int index);

    private void _setDetailedErrorCode(NexPlayerEvent event) {
        if (null != event.obj) {
            boolean shouldSetDetailErrorInfo = false;
            switch (event.what) {
                case NexPlayerEvent.NEXPLAYER_EVENT_ERROR:
                    shouldSetDetailErrorInfo = true;
                    break;
                case NexPlayerEvent.NEXPLAYER_EVENT_DATA_INACTIVITY_TIMEOUT:
                    shouldSetDetailErrorInfo = true;
                    break;
                case NexPlayerEvent.NEXPLAYER_EVENT_ASYNC_CMD_COMPLETE:
                    if (NEXPLAYER_ASYNC_CMD_OPEN_STREAMING == event.intArgs[0]) {
                        NexErrorCode errorCode = NexErrorCode.fromIntegerValue(event.intArgs[1]);
                        if (NexErrorCode.SOURCE_OPEN_TIMEOUT == errorCode ||
                           (NexErrorCode.ERROR_NETWORK_PROTOCOL.getIntegerCode() <= event.intArgs[1] && NexErrorCode.ERROR_INVALID_SERVER_STATUSCODE.getIntegerCode() >= event.intArgs[1])) {
                            shouldSetDetailErrorInfo = true;
                        }
                    }
                    break;
                default:
                    break;
            }

            if (shouldSetDetailErrorInfo) {
                if (event.obj instanceof String) {
                    mErrorStrings = (String)event.obj;
                    NexLog.d(TAG, "_setDetailedErrorCode : " + event.what + " param 1 : " + event.intArgs[0] + ", param 2 : " + event.intArgs[1] + ", strings : " + mErrorStrings);
                }
            }
        }
    }

    /**
     * \brief This method provides a string of information when a network or protocol error occurs, such as a timeout due to a connection delay.
     * If the category of the NexErrorCode is Network or Protocol, or a timeout such as SOURCE_OPEN_TIMEOUT / DATA_INACTIVITY_TIMEOUT by NEXPLAYER_EVENT_DATA_INACTIVITY_TIMEOUT, the string will be filled.
     *
     * \warning  NexProperty.ENABLE_DETAIL_ERROR should be enabled to get this information
     *
     * \return      Returns a string of information for Network or Protocol errors, an empty string if none.
     */
    public String getDetailedError() {
        return mErrorStrings;
    }

    /**
     * For internal use only.  This should be otherwise ignored.
     */
    static String getDefaultEngineLibPath(Context context) {
        String engine = "libnexplayerengine.so";


        Context iContext = context.getApplicationContext();
        String strPath = iContext.getFilesDir().getAbsolutePath();

        int iPackageNameLength = iContext.getPackageName().length();
        int iStartIndex = strPath.indexOf(iContext.getPackageName());

        String strLibPath = strPath.substring(0, iStartIndex + iPackageNameLength) + "/";
        final ApplicationInfo applicationInfo = context.getApplicationInfo();
        String strNativePath = strLibPath;
        try {
            if (Build.VERSION.SDK_INT >= 9) {
                Field f = ApplicationInfo.class.getField("nativeLibraryDir");
                strNativePath = f.get(applicationInfo) + "/";
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return strNativePath + engine;
    }

    /**
     * \brief This method sets the proxy setting for the requests, only if the actual network configuration of the device has it.
     *
     * \param proxyAddress           The proxy server address to use.
     * \param proxyPort              The proxy server port number to use.
     *
     * The default value of proxyAddress is null.
     * The default value of proxyPort is -1.
     *
     * \deprecated This API will be deprecated. Instead of this API, please use setProperties with NexProperty.PROXY_ADDRESS and NexProperty.PROXY_PORT values.
     *
     * \since version 6.67
     */
    public void setProxyInfo(String proxyAddress, int proxyPort) {
        boolean IS_ICS_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;

        if( IS_ICS_OR_LATER ) {
            if( proxyAddress == null || "".equals(proxyAddress) ) {
                proxyAddress = System.getProperty( "http.proxyHost" );
            }

            if( proxyPort == -1 ) {
                String portStr = System.getProperty("http.proxyPort");
                proxyPort = Integer.parseInt((portStr != null ? portStr : "-1"));
            }
        } else {
            if( proxyAddress == null  || "".equals(proxyAddress) ) {
                proxyAddress = android.net.Proxy.getHost(this.mContext);
            }

            if( proxyPort == -1 ) {
                proxyPort = android.net.Proxy.getPort(this.mContext);
            }
        }

        if ( proxyAddress != null ) {
            this.setProperties(NexProperty.PROXY_ADDRESS.getPropertyCode(), proxyAddress);
        }

        if ( proxyPort != -1 ) {
            this.setProperties(NexProperty.PROXY_PORT.getPropertyCode(), proxyPort);
        }
    }

    /**
     * This method removes the unsupported video resolutions based on the native MediaCodec video capability information.
     * If the track has lower resolution than the one reported by the MediaCodecInfo API, then that track will be ignored
     * and won't be played.
     * 
     * This feature only works for API Level 21 and above
     */
    public void disableUnsupportedResolutions() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            NexLog.d(TAG, "This method is not supported in this version of Android: disableUnsupportedResolutions()");
            return;
        }

        int minWidth = 0;
        int minHeight = 0;

        for (NexStreamInformation streamInformation : getContentInfo().mArrStreamInformation) {
            for (NexTrackInformation trackInformation : streamInformation.mArrTrackInformation) {
                String mimeType = NexContentInformation.getMediaCodecMimeType(trackInformation.mType, trackInformation.mCodecType);
                MediaCodecInfo.VideoCapabilities capability = getVideoCapability(mimeType);
                if (capability == null) {
                    continue;
                }
                if (minWidth == 0 || minWidth > capability.getSupportedWidths().getLower()) {
                    minWidth = capability.getSupportedWidths().getLower();
                }
                if (minHeight == 0 || minHeight > capability.getSupportedHeights().getLower()) {
                    minHeight = capability.getSupportedHeights().getLower();
                }

            }
        }

        NexLog.d(TAG, "Setting minimum supported resolution: " + minWidth + "x" + minHeight);
        setProperties(230, minWidth);
        setProperties(231, minHeight);
    }

    private MediaCodecInfo.VideoCapabilities getVideoCapability(String mimeType) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            NexLog.d(TAG, "This method is not supported in this version of Android: getVideoCapability(String mimeType)");
            return null;
        }

        MediaCodecInfo[] mAllCodecInfo = new MediaCodecList(MediaCodecList.ALL_CODECS).getCodecInfos();

        for (MediaCodecInfo info : mAllCodecInfo) {
            String[] types = info.getSupportedTypes();

            if (info.isEncoder() || types.length == 0 || types[0].contains("audio")) {
                continue;
            }

            for (int j = 0; j < types.length; ++j) {
                if (types[j].equals(mimeType)) {
                    try {
                        MediaCodecInfo.CodecCapabilities cap = info.getCapabilitiesForType(types[j]);
                        MediaCodecInfo.VideoCapabilities vCap = cap.getVideoCapabilities();
                        return vCap;
                    } catch (IllegalArgumentException ex) {
                        return null;
                    }
                }
            }
        }
        NexLog.d(TAG, "Media codec capability not found for mime type: " + mimeType);
        return null;
    }
}

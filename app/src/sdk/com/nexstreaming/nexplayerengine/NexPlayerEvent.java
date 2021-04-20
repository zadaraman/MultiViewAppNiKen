package com.nexstreaming.nexplayerengine;

class NexPlayerEvent {

    /*************************************************************************************************************************************/
    // Protected Events - Internal purpose.
    /*************************************************************************************************************************************/
    protected static final int NEXPLAYER_EVENT_NOP                              = 0; // interface test message
    protected static final int NEXPLAYER_EVENT_COMMON_BASEID                    = 0x00010000;
    protected static final int NEXPLAYER_EVENT_ENDOFCONTENT                     = ( NEXPLAYER_EVENT_COMMON_BASEID + 1 );
    protected static final int NEXPLAYER_EVENT_STARTVIDEOTASK                   = ( NEXPLAYER_EVENT_COMMON_BASEID + 2 );
    protected static final int NEXPLAYER_EVENT_STARTAUDIOTASK                   = ( NEXPLAYER_EVENT_COMMON_BASEID + 3 );
    protected static final int NEXPLAYER_EVENT_TIME                             = ( NEXPLAYER_EVENT_COMMON_BASEID + 4 );
    protected static final int NEXPLAYER_EVENT_ERROR                            = ( NEXPLAYER_EVENT_COMMON_BASEID + 5 );
    protected static final int NEXPLAYER_EVENT_RECORDEND                        = ( NEXPLAYER_EVENT_COMMON_BASEID + 6 );
    protected static final int NEXPLAYER_EVENT_STATECHANGED                     = ( NEXPLAYER_EVENT_COMMON_BASEID + 7 );
    protected static final int NEXPLAYER_EVENT_SIGNALSTATUSCHANGED              = ( NEXPLAYER_EVENT_COMMON_BASEID + 8 );
    protected static final int NEXPLAYER_EVENT_DEBUGINFO                        = ( NEXPLAYER_EVENT_COMMON_BASEID + 9 );
    protected static final int NEXPLAYER_EVENT_ASYNC_CMD_COMPLETE               = ( NEXPLAYER_EVENT_COMMON_BASEID + 10 );
    protected static final int NEXPLAYER_EVENT_RTSP_COMMAND_TIMEOUT             = ( NEXPLAYER_EVENT_COMMON_BASEID + 11 );
    protected static final int NEXPLAYER_EVENT_PAUSE_SUPERVISION_TIMEOUT        = ( NEXPLAYER_EVENT_COMMON_BASEID + 12 );
    protected static final int NEXPLAYER_EVENT_DATA_INACTIVITY_TIMEOUT          = ( NEXPLAYER_EVENT_COMMON_BASEID + 13 );

    protected static final int NEXPLAYER_EVENT_RECORDING_ERROR                  = ( NEXPLAYER_EVENT_COMMON_BASEID + 14 );
    protected static final int NEXPLAYER_EVENT_RECORDING                        = ( NEXPLAYER_EVENT_COMMON_BASEID + 15 );
    protected static final int NEXPLAYER_EVENT_TIMESHIFT_ERROR                  = ( NEXPLAYER_EVENT_COMMON_BASEID + 16 );
    protected static final int NEXPLAYER_EVENT_TIMESHIFT                        = ( NEXPLAYER_EVENT_COMMON_BASEID + 17 );
    protected static final int NEXPLAYER_EVENT_STATUS_REPORT                    = ( NEXPLAYER_EVENT_COMMON_BASEID + 20 );
    protected static final int NEXPLAYER_EVENT_PROGRAMTIME                      = ( NEXPLAYER_EVENT_COMMON_BASEID + 22 );
    protected static final int NEXPLAYER_EVENT_ONHTTPSTATS                      = ( NEXPLAYER_EVENT_COMMON_BASEID + 23 );
    protected static final int NEXPLAYER_EVENT_THUMBNAIL_REPORT                 = ( NEXPLAYER_EVENT_COMMON_BASEID + 24 );
    protected static final int NEXPLAYER_EVENT_THUMBNAIL_REPORT_END             = ( NEXPLAYER_EVENT_COMMON_BASEID + 25 );
    protected static final int NEXPLAYER_EVENT_METADATA                             = ( NEXPLAYER_EVENT_COMMON_BASEID + 26 );
    protected static final int NEXPLAYER_EVENT_DATA_INACTIVITY_TIMEOUT_WARNING      = ( NEXPLAYER_EVENT_COMMON_BASEID + 27 );

    // Streaming Events
    protected static final int NEXPLAYER_EVENT_STREAMING_BASEID                 = 0x00030000;
    protected static final int NEXPLAYER_EVENT_BUFFERINGBEGIN                   = ( NEXPLAYER_EVENT_STREAMING_BASEID + 1 );
    protected static final int NEXPLAYER_EVENT_BUFFERINGEND                     = ( NEXPLAYER_EVENT_STREAMING_BASEID + 2 );
    protected static final int NEXPLAYER_EVENT_BUFFERING                        = ( NEXPLAYER_EVENT_STREAMING_BASEID + 3 );

    // Audio Renderer Events
    protected static final int NEXPLAYER_EVENT_AUDIO_RENDER_BASEID              = 0x00060000;
    protected static final int NEXPLAYER_EVENT_AUDIO_RENDER_CREATE              = ( NEXPLAYER_EVENT_AUDIO_RENDER_BASEID + 1 );
    protected static final int NEXPLAYER_EVENT_AUDIO_RENDER_DELETE              = ( NEXPLAYER_EVENT_AUDIO_RENDER_BASEID + 2 );
    protected static final int NEXPLAYER_EVENT_AUDIO_RENDER_PAUSE               = ( NEXPLAYER_EVENT_AUDIO_RENDER_BASEID + 4 );
    protected static final int NEXPLAYER_EVENT_AUDIO_RENDER_RESUME              = ( NEXPLAYER_EVENT_AUDIO_RENDER_BASEID + 5 );
    protected static final int NEXPLAYER_EVENT_AUDIO_RENDER_PREPARED            = ( NEXPLAYER_EVENT_AUDIO_RENDER_BASEID + 6 );

    // Video Renderer Events
    protected static final int NEXPLAYER_EVENT_VIDEO_RENDER_BASEID              = 0x00070000;
    protected static final int NEXPLAYER_EVENT_VIDEO_RENDER_CREATE              = ( NEXPLAYER_EVENT_VIDEO_RENDER_BASEID + 1 );
    protected static final int NEXPLAYER_EVENT_VIDEO_RENDER_DELETE              = ( NEXPLAYER_EVENT_VIDEO_RENDER_BASEID + 2 );
    protected static final int NEXPLAYER_EVENT_VIDEO_RENDER_RENDER              = ( NEXPLAYER_EVENT_VIDEO_RENDER_BASEID + 3 );
    protected static final int NEXPLAYER_EVENT_VIDEO_RENDER_CAPTURE             = ( NEXPLAYER_EVENT_VIDEO_RENDER_BASEID + 4 );
    protected static final int NEXPLAYER_EVENT_VIDEO_RENDER_PREPARED            = ( NEXPLAYER_EVENT_VIDEO_RENDER_BASEID + 5 );

    // Text Renderer Events
    protected static final int NEXPLAYER_EVENT_TEXT_RENDER_BASEID               = 0x00080000;
    protected static final int NEXPLAYER_EVENT_TEXT_RENDER_INIT                 = ( NEXPLAYER_EVENT_TEXT_RENDER_BASEID + 1 );
    protected static final int NEXPLAYER_EVENT_TEXT_RENDER_RENDER               = ( NEXPLAYER_EVENT_TEXT_RENDER_BASEID + 2 );

    // Timedmeta Events
    protected static final int NEXPLAYER_EVENT_TIMEDMETA_RENDER_BASEID          = 0x00090000;
    protected static final int NEXPLAYER_EVENT_TIMEDMETA_RENDER_RENDER          = ( NEXPLAYER_EVENT_TIMEDMETA_RENDER_BASEID + 1 );

    // Modify http request Event
    protected static final int NEXPLAYER_SUPPORT_MODIFY_HTTP_REQUEST            = 0x000B0001;

    // DRM Events
    protected static final int NEXPLAYER_INIT_MEDIA_DRM                         = 0x000B0002;
    protected static final int NEXPLAYER_DEINIT_MEDIA_DRM                       = 0x000B0003;
    protected static final int NEXPLAYER_DRM_TYPE_ACCEPTED                      = 0x000B0004;

    // Track change Events
    protected static final int NEXPLAYER_CALLBACK_HTTP_ABR_TRACKCHANGE          = 0x000C0001;

    // Downloader Events
    protected static final int NEXDOWNLOADER_EVENT_ERROR                        = 0x00100000;
    protected static final int NEXDOWNLOADER_EVENT_ASYNC_CMD_BASEID             = 0x00200000;
    protected static final int NEXDOWNLOADER_EVENT_COMMON                       = 0x00300000;
    protected static final int NEXDOWNLOADER_EVENT_COMMON_DOWNLOAD_BEGIN        = 0x00320001;
    protected static final int NEXDOWNLOADER_EVENT_COMMON_DOWNLOAD_PROGRESS     = 0x00320002;
    protected static final int NEXDOWNLOADER_EVENT_COMMON_DOWNLOAD_COMPLETE     = 0x00320003;
    protected static final int NEXDOWNLOADER_EVENT_COMMON_STATE_CHANGED         = 0x00320004;


    // User Action Events.
    protected static final int NEXPLAYER_EVENT_WILL_INIT                        = 0xC0000000;
    protected static final int NEXPLAYER_EVENT_WILL_OPEN                        = 0xC0000001;
    protected static final int NEXPLAYER_EVENT_WILL_START                       = 0xC0000002;
    protected static final int NEXPLAYER_EVENT_WILL_RESUME                      = 0xC0000003;
    protected static final int NEXPLAYER_EVENT_WILL_SEEK                        = 0xC0000004;
    protected static final int NEXPLAYER_EVENT_WILL_PAUSE                       = 0xC0000005;
    protected static final int NEXPLAYER_EVENT_WILL_STOP                        = 0xC0000006;
    protected static final int NEXPLAYER_EVENT_WILL_CLOSE                       = 0xC0000007;
    protected static final int NEXPLAYER_EVENT_WILL_RELEASE                     = 0xC0000008;

    // Offline Playback Events
    protected static final int NEXPLAYER_OFFLINE_RETREIVE_KEY                   = 0xC0000009;
    protected static final int NEXPLAYER_OFFLINE_STORE_KEY                      = 0xC0000010;
    protected static final int NEXPLAYER_OFFLINE_KEY_EXPIRED                    = 0XC0000011;

    // Warning events
    //NexWVSWDrm start
    protected static final int NEXPLAYER_WARNING_DRM_RECOVERY                   = 0xD0000000;
    //NexWVSWDrm end

    protected int what = NEXPLAYER_EVENT_NOP;
    protected int[] intArgs = new int[0];
    protected long[] longArgs = new long[0];
    protected Object obj = new Object[0];

    protected NexPlayerEvent(int what) { this.what = what; }

    protected NexPlayerEvent(int what, int[] intArgs, long[] longArgs, Object obj) {
        this.what = what;
        this.intArgs = intArgs;
        this.longArgs = longArgs;
        this.obj = obj;
    }
}

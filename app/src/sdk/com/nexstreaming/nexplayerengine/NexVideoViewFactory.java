package com.nexstreaming.nexplayerengine;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class NexVideoViewFactory
{
    public interface INexVideoView
    {
        void init(NexPlayer nexPlayer);
        boolean isInitialized();
        void getVideoSize(Point outSize);
        Rect getDisplayedRect();
        void setOutputPos(int left, int top, int width, int height);
        void clearCanvas();
        void resetSurface();
        void setScreenPixelFormat(int screenPixelFormatToSet);
        void setListener(NexVideoRenderer.IListener listener);
        void setPreNexPlayerVideoRendererListener(NexPlayer.IVideoRendererListener listener);
        void setPostNexPlayerVideoRendererListener(NexPlayer.IVideoRendererListener listener);
        void setSurfaceSecure(Boolean usesecure);
        void setZOrderMediaOverlay(boolean isMediaOverlay);
        void setCodecName(String name);
        void setSecureSurfaceFlag(Boolean secure);
        void setSupportMultiView(Boolean enable);

        int getVisibility();
        void setVisibility(int visibility);
        int getWidth();
        int getHeight();
        View getView();

        void release();
        void onPause();
        void onResume();
        void keepScreenOn(boolean enable);

    }

    public enum NexVideoRendererStatus
    {
        VIDEO_RENDERER_NONE,
        VIDEO_RENDERER_PREPARED,
        VIDEO_RENDERER_INITED,
        VIDEO_RENDERER_DEINITED
    }

    public enum NexVideoViewType
    {
        NEXVIEW_NORMAL
        ,NEXVIEW_SURFACETEXTURE
    }

    public static INexVideoView createNexVideoView(Context context, NexVideoViewType PreferType)
    {
        INexVideoView videoView = null;


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            if(PreferType == NexVideoViewType.NEXVIEW_SURFACETEXTURE)
            {
                videoView = new NexSurfaceTextureBinder();
            }
        }
        else
        {
            if(PreferType != NexVideoViewType.NEXVIEW_NORMAL)
                NexLog.e("NexVideoViewFactory","this android version does not support SurfaceTexture");
        }


        if(videoView == null)
        {
            videoView = new NexVideoRenderer(context);
        }

        return videoView;
    }

    public static INexVideoView createNexVideoView(Context context, AttributeSet attr, NexVideoViewType PreferType)
    {
        INexVideoView videoView = null;


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            if(PreferType == NexVideoViewType.NEXVIEW_SURFACETEXTURE)
            {
                videoView = new NexSurfaceTextureBinder();
            }
        }
        else
        {
            if(PreferType != NexVideoViewType.NEXVIEW_NORMAL)
                NexLog.e("NexVideoViewFactory","this android version does not support SurfaceTexture");
        }


        if(videoView == null)
        {
            videoView = new NexVideoRenderer(context, attr);
        }

        return videoView;
    }

    public static INexVideoView createNexVideoView(Context context, AttributeSet attr, int defStyle, NexVideoViewType PreferType)
    {
        INexVideoView videoView = null;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            if(PreferType == NexVideoViewType.NEXVIEW_SURFACETEXTURE)
            {
                videoView = new NexSurfaceTextureBinder();
            }
        }
        else
        {
            if(PreferType != NexVideoViewType.NEXVIEW_NORMAL)
                NexLog.e("NexVideoViewFactory","this android version does not support SurfaceTexture");
        }

        if(videoView == null)
        {
            videoView = new NexVideoRenderer(context, attr, defStyle);
        }

        return videoView;
    }

    public static void replaceView(View currentView, View newView)
    {
        if(currentView != null)
        {
            ViewGroup parent = (ViewGroup)currentView.getParent();
            if(parent != null)
            {
                final int index = parent.indexOfChild(currentView);
                ViewGroup.LayoutParams layoutparam = currentView.getLayoutParams();
                parent.removeView(currentView);
                parent.addView((View) newView, index);
                newView.setLayoutParams(layoutparam);
            }
        }
    }
}

package com.nexstreaming.nexplayerengine;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * @brief This class is used to manage caption rendering in \c NexVideoView.*
 * \warning This class will be deprecated. Please use {@link NexCaptionPainter} instead.
 */
public class NexCaptionRenderView extends FrameLayout {
    private static final Handler mHandler = new Handler();

    private NexCaptionPainter mCaptionPainter;
    private NexCaptionSetting mCaptionSetting;
    private NexCaptionAttribute mCaptionAttribute;

    /**
     * @param context The \link android.content.Context Context\endlink instance
     *                associated with the activity that will contain this view.
     * @brief Constructor for NexCaptionRenderView.
     * @since version 6.42
     */
    public NexCaptionRenderView(Context context) {
        super(context);
        init(context);
    }

    /**
     * @brief Constructor for NexCaptionRenderView.
     * @see NexVideoRenderer.NexVideoRenderer(android.content.Context)
     * @since version 6.42
     */
    public NexCaptionRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * @param attribute A \c NexCaptionAttribute object.
     * @brief This method sets the attributes of captions used by the \c NexVideoView.
     * @since version 6.42
     */
    public void setCaptionAttribute(NexCaptionAttribute attribute) {
        mCaptionAttribute = attribute;

        if (null != attribute) {
            mCaptionSetting.mEdgeStyle = convertEdgeStyle(attribute.mEdgeStyle);
            mCaptionSetting.mBackgroundColor = getColorFromCapColor(attribute.mBackGroundColor, attribute.mBackgroundOpacity);
            mCaptionSetting.mWindowColor = getColorFromCapColor(attribute.mWindowColor, attribute.mWindowOpacity);
            mCaptionSetting.mFontColor = getColorFromCapColor(attribute.mFontColor, attribute.mFontOpacity);
            mCaptionSetting.mFontScale = attribute.mScaleFactor;

        } else {
            mCaptionSetting.init();
        }

        mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
    }

    /**
     * @return The \c NexCaptionAttribute object set to the current caption.
     * @brief This method gets the attribute information set to the current caption.
     * @since version 6.42
     */
    public NexCaptionAttribute getCaptionAttribute() {
        return mCaptionAttribute;
    }

    /**
     * @param textview    A TextView instance.
     * @param param       The layout parameters associated with the textview.
     * @param charsetName Converts the byte array to a string using the named charset.
     * @brief This method sets the text view to display external subtitles.
     * @since version 6.42
     * @deprecated this API is deprecated. The external subtitle will be displayed by this class internally.
     */
    public void setExternalSubtitleTextView(TextView textview, LayoutParams param, String encodingPreset) {
    }

    protected void clearCaptionString() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCaptionPainter.clear();
            }
        });
    }

    protected void setRenderingArea(RenderingArea area) {
        mCaptionPainter.setRenderingArea(area.mVideo, area.mRatio);
    }

    protected static class RenderingArea {
        Rect mVideo;
        Rect mView;
        Rect mText;
        float mRatio;

        RenderingArea() {
            mVideo = null;
            mView = null;
            mText = null;
            mRatio = 0.0f;
        }
    }

    protected void renderClosedCaption(int captionType, final NexClosedCaption textInfo) {
        if (textInfo != null) {
            mCaptionPainter.setDataSource(textInfo);
        }
    }

    private void init(Context context) {
        mCaptionAttribute = null;
        mCaptionSetting = new NexCaptionSetting();
        mCaptionPainter = new NexCaptionPainter(context, NexContentInformation.NEX_TEXT_CEA608);
    }

    private NexCaptionSetting.EdgeStyle convertEdgeStyle(NexCaptionAttribute.EdgeStyle edgeStyle) {
        NexCaptionSetting.EdgeStyle captionSettingEdgeStyle = NexCaptionSetting.EdgeStyle.DEFAULT;
        switch (edgeStyle) {
            case NONE:
                captionSettingEdgeStyle = NexCaptionSetting.EdgeStyle.NONE;
                break;
            case DROP_SHADOW:
                captionSettingEdgeStyle = NexCaptionSetting.EdgeStyle.DROP_SHADOW;
                break;
            case RAISED:
                captionSettingEdgeStyle = NexCaptionSetting.EdgeStyle.RAISED;
                break;
            case DEPRESSED:
                captionSettingEdgeStyle = NexCaptionSetting.EdgeStyle.DEPRESSED;
                break;
            case UNIFORM:
                captionSettingEdgeStyle = NexCaptionSetting.EdgeStyle.UNIFORM;
                break;
            default:
        }
        return captionSettingEdgeStyle;
    }

    private int getColorFromCapColor(NexClosedCaption.CaptionColor cColor, int cOpacity) {
        int setColor = cColor.getFGColor();
        return Color.argb(cOpacity, Color.red(setColor), Color.green(setColor), Color.blue(setColor));
    }

    NexCaptionPainter getCaptionPainter() { return mCaptionPainter; }
}
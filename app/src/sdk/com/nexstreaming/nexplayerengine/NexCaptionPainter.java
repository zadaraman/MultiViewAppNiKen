package com.nexstreaming.nexplayerengine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import static com.nexstreaming.nexplayerengine.NexCaptionSetting.DEFAULT;
import static com.nexstreaming.nexplayerengine.NexCaptionSetting.EdgeStyle;
import static com.nexstreaming.nexplayerengine.NexCaptionSetting.StringStyle;
import static com.nexstreaming.nexplayerengine.NexClosedCaption.convertEncodingType;

/**
 * \brief  This class defines the caption renderer view and displays the information to the user.
 *
 * In order to support all of the text attributes and display options of the CEA 608 / CEA 708 / WebVTT / TTML / SMI / SRT / SUB specifications, it is necessary
 * to create a Caption Renderer view with the NexCaptionPainter class.
 *
 * The NexCaptionPainter view is overlaid on the application's video output, and as a result, whenever the video
 * display changes, the NexCaptionPainter must also be updated in order for captions to be
 * accurately displayed.
 *
 * In particular, the NexCaptionPainter in an application requires the following information:
 *   - <b>Rendering Area Information:</b>  Whenever the video output in an application UI changes its size or the orientation changes,
 *   the new size information should also be passed to the NexCaptionPainter class by calling the NexCaptionPainter.setRenderingArea method.
 *   - <b>New Caption Data:</b>  Whenever new caption data is received, it should be passed to
 *   the NexCaptionPainter by calling the NexCaptionPainter.setDataSource method.
 *
 *  When it is necessary to clear captions from the screen, for example when seeking in or stopping content,
 *  calling the \link NexCaptionPainter.clear\endlink method will remove the captions existing on the screen.
 *
 *  When you want to set attributes to the captions, the NexCaptionSetting should be passed to NexCaptionPainter by calling the NexCaptionPainter.setUserCaptionSettings method.
 *
 */

public class NexCaptionPainter extends ViewGroup {
    private final String LOG_TAG = "NexCaptionPainter";
    private Context mContext = null;
    private int mCaptionType = NexContentInformation.NEX_TEXT_UNKNOWN;
    private NexCaptionExtractor mCaptionExtractor = null;
    private NexCaptionSetting mUserCaptionSettings = new NexCaptionSetting();
    private Rect mRenderingArea = new Rect();
    private float mRenderingScale = 1f;
    ArrayList<NexCaptionRenderingAttribute> mRenderingAttributes = new ArrayList<NexCaptionRenderingAttribute>();
    private CollisionRectHelper mCollisionRectHelper = new CollisionRectHelper();
    private final Object lock = new Object();
    public SparseIntArray cachedMappedFontColors;

    Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * \brief This is an alternative constructor for the NexCaptionPainter.
     *
     * \param context  The handle for the player.
     * \param captionType  The type of caption to be displayed.
     */
    public NexCaptionPainter(Context context, int captionType) {
        super(context);
        mContext = context;
        mCaptionExtractor = NexCaptionExtractorFactory.create(captionType);

        WrapSetLayerType();

        if (null != mCaptionExtractor) {
            if (NexContentInformation.NEX_TEXT_CEA == captionType) {
                mCaptionType = NexContentInformation.NEX_TEXT_CEA608;
            } else {
                mCaptionType = captionType;
            }

            NexLog.d(LOG_TAG, "default caption type : " + mCaptionType);
        }
    }

    /**
     * \brief This is an alternative constructor for the NexCaptionPainter.
     * \warning If the caption view has to be used in Android xml, this constructor must be called. CEA-608 caption type will be selected as a default.
     *
     * \param context  The handle for the player.
     * \param attrs   The set of attributes associated with the view.
     */
    public NexCaptionPainter(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mCaptionExtractor = NexCaptionExtractorFactory.create(NexContentInformation.NEX_TEXT_CEA608);

        WrapSetLayerType();

        if (null != mCaptionExtractor) {
            mCaptionType = NexContentInformation.NEX_TEXT_CEA608;
        }
    }

    /**
     * \brief This is a method to set the type of caption to be displayed.
     *
     * \param captionType the type of caption to be displayed.
     * \see caption types in NexContentInformation
     */
    public void setCaptionType(int captionType) {
        if (NexContentInformation.NEX_TEXT_UNKNOWN != captionType && mCaptionType != captionType) {
            if ((NexContentInformation.NEX_TEXT_CEA == mCaptionType || NexContentInformation.NEX_TEXT_CEA608 == mCaptionType) &&
                    (NexContentInformation.NEX_TEXT_CEA == captionType || NexContentInformation.NEX_TEXT_CEA608 == captionType)) {
                NexLog.d(LOG_TAG, "same type");
                NexLog.d(LOG_TAG, "set caption type old : " + mCaptionType + " , new : " + captionType);
                mCaptionType = NexContentInformation.NEX_TEXT_CEA608;
            } else {
                NexLog.d(LOG_TAG, "[1] mCaptionExtractor create start");
                synchronized(lock) {
                    NexCaptionExtractor captionExtractor = NexCaptionExtractorFactory.create(captionType);
                    if (null != captionExtractor) {
                        mCaptionExtractor = captionExtractor;
                        mCaptionExtractor.setRenderingArea(mRenderingArea, mRenderingScale);
                        mCaptionExtractor.setMappedFontColors(cachedMappedFontColors);

                        if (NexContentInformation.NEX_TEXT_CEA == captionType) {
                            mCaptionType = NexContentInformation.NEX_TEXT_CEA608;
                        } else {
                            mCaptionType = captionType;
                        }
                    }
                }
                NexLog.d(LOG_TAG, "[1] mCaptionExtractor create end");
                NexLog.d(LOG_TAG, "set caption type : " + mCaptionType);
            }
        }
    }

    /**
     * \brief This is a method to get the type of caption to be displayed.
     *
     * \returns the type of the caption to be displayed.
     * \see caption types in NexContentInformation class
     */
    public int getCaptionType() {
        return mCaptionType;
    }

    /**
     * \brief This method sets the caption rendering area, compared to the video rendering area.
     *          This method can be used to reposition where captions will appear relative to the playing content.
     *
     * \param renderingArea  The area of caption.
     * \param scale  The scale should be used to scale the text renderer accordingly as well.
     */
    public void setRenderingArea(Rect renderingArea, float scale) {
        NexLog.d(LOG_TAG, "rendering area : " + renderingArea + ", scale : " + scale);

        mRenderingArea = renderingArea;
        mRenderingScale = scale;

        if (null != mCaptionExtractor) {
            mCaptionExtractor.setRenderingArea(mRenderingArea, scale);
            mCollisionRectHelper.clear();
            mCollisionRectHelper.setBoundary(mRenderingArea.left, mRenderingArea.top, mRenderingArea.width(), mRenderingArea.height());

            if (!mRenderingAttributes.isEmpty()) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        float anchorHeight = (float)mRenderingArea.height() / NexCaptionExtractor.DEFAULT_VERTICAL_CELL;
                        for (NexCaptionRenderingAttribute renderingAttribute : mRenderingAttributes) {
                            NexCaptionWindowRect userSetRect = new NexCaptionWindowRect(renderingAttribute.mCaptionSettings.mRelativeWindowRect);

                            float fontScale = DEFAULT; float fontSize = renderingAttribute.mRelativeFontSize * anchorHeight;
                            if (null != mUserCaptionSettings.mRelativeWindowRect) { userSetRect.copyTouchedSettings(mUserCaptionSettings.mRelativeWindowRect); }
                            if (DEFAULT != mUserCaptionSettings.mFontSize) { fontSize = convertAdjustedUserFontSize(mRenderingArea, mUserCaptionSettings.mFontSize); }
                            if (DEFAULT != mUserCaptionSettings.mFontScale) { fontScale = mUserCaptionSettings.mFontScale; }

                            if (renderingAttribute.view instanceof NexCaptionTextView) {
                                setFontSize((TextView)renderingAttribute.view, fontSize, fontScale);
                            }

                            setViewLayout(renderingAttribute.view, renderingAttribute, userSetRect);
                        }

                        avoidCollisionRects();
                    }
                });
            }
        }
    }

    /**
     * \brief This method sets the caption data to be rendered
     *
     * \param caption  The NexClosedCaption object containing the closed captions and attributes to be displayed.
     */
    public void setDataSource(final NexClosedCaption data) {
        if (mCaptionType == data.mCaptionType) {
            final ArrayList<NexCaptionRenderingAttribute> renderingAttributeList = makeRenderingAttribute(data);

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (null != renderingAttributeList) {
                        for (int i = 0; i < renderingAttributeList.size(); ++i) {
                            NexCaptionRenderingAttribute renderingAttribute = renderingAttributeList.get(i);

                            if (renderingAttribute.removeById) {
                                updateRemovingView(renderingAttribute);
                            } else {
                                if (0 == i) {
                                    updateRemovingView(renderingAttribute);
                                }
                            }

                            updateDrawingView(renderingAttribute);
                        }

                        //avoidCollisionRects();
                    }
                }
            });
        } else {
            NexLog.d(LOG_TAG, "type is not matched. NexCaptionPainter : " + mCaptionType + " , NexClosedCaption : " + data.mCaptionType);
        }
    }

    /**
     * \brief  This clears the currently displayed captions.
     */
    public void clear() {
        NexLog.d(LOG_TAG, "clear called");
        this.removeAllViews();

        if (null != mCaptionExtractor) {
            mCaptionExtractor.clear();
        }

        mRenderingAttributes.clear();
        mCollisionRectHelper.clear();
    }

    /**
     * \brief This method sets the attributes of the captions.
     *
     * \param captionSettings  An object of NexCaptionSetting
     * \see NexCaptionSetting
     */
    public void setUserCaptionSettings(NexCaptionSetting captionSettings) {
        mUserCaptionSettings.copyAllSettings(captionSettings);

        if (null != captionSettings) {
            mCaptionExtractor.setMappedFontColors(captionSettings.mappedFontColors);
            cachedMappedFontColors = captionSettings.mappedFontColors;

            NexLog.d(LOG_TAG, "setUserCaptionSettings called");
        }

        mCollisionRectHelper.clear();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (NexCaptionRenderingAttribute renderingAttribute : mRenderingAttributes) {
                    NexCaptionSetting captionSettingApplied = new NexCaptionSetting(renderingAttribute.mCaptionSettings);
                    captionSettingApplied.copyTouchedSettings(mUserCaptionSettings);

                    if (DEFAULT != mUserCaptionSettings.mFontSize) { captionSettingApplied.mFontSize = convertAdjustedUserFontSize(mRenderingArea, mUserCaptionSettings.mFontSize); }

                    updateCaptionSettings(renderingAttribute, captionSettingApplied);
                }

                avoidCollisionRects();
            }
        });
    }

    /**
     * \brief This method gets the attributes of the captions.
     *
     * \return	   The attributes of the captions. If the user has not set this before, it will returned the default properties.
     *
     */
    public NexCaptionSetting getUserCaptionSettings() {
        return mUserCaptionSettings;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //do nothing
    }

    @SuppressLint("NewApi")
    private void WrapSetLayerType() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            setLayerType(ViewGroup.LAYER_TYPE_SOFTWARE, null);
        }
    }

    private void updateRemovingView(NexCaptionRenderingAttribute renderingAttribute) {
        boolean hasRemovingData = renderingAttribute.mRemoveTime > 0 || renderingAttribute.removeById;
        if (hasRemovingData) {
            Iterator<NexCaptionRenderingAttribute> iterator = mRenderingAttributes.iterator();
            while (iterator.hasNext()) {
                NexCaptionRenderingAttribute curAttribute = iterator.next();
                boolean shouldRemove = false;

                if (curAttribute.removeById) {
                    if (curAttribute.id == renderingAttribute.mRemoveTime) {
                        shouldRemove = true;
                    }
                } else {
                    if (curAttribute.mEndTime <= renderingAttribute.mRemoveTime) {
                        shouldRemove = true;
                    }
                }

                if (shouldRemove) {
                    removeView(curAttribute.view);
                    NexLog.d(LOG_TAG, "remove id : " + curAttribute.id);
                    mCollisionRectHelper.remove(curAttribute.id);
                    iterator.remove();
                }
            }
        }
    }

    private void updateDrawingView(NexCaptionRenderingAttribute renderingAttribute) {
        boolean hasDrawingData = renderingAttribute.mEndTime > 0;
        if (hasDrawingData) {
            if (null != renderingAttribute.view) {
                mRenderingAttributes.add(renderingAttribute);

                NexLog.d(LOG_TAG, "add id : " + renderingAttribute.id);

                if (!renderingAttribute.mCaptionSettings.mRelativeWindowRect.userDefined ||
                        renderingAttribute.mCaptionSettings.mRelativeWindowRect.autoAdjustment) {

                    mCollisionRectHelper.add(renderingAttribute.id, renderingAttribute.mWindowRect);

                    NexLog.d(LOG_TAG, "call avoidCollisionRects");
                    avoidCollisionRects();
                }

                addView(renderingAttribute.view);
            }
        }
    }

    private void applyCaptionSettings(NexCaptionTextView view, NexCaptionRenderingAttribute renderingAttribute, NexCaptionSetting captionSettings) {
        if (null != captionSettings) {
            SpannableString spannableString = (SpannableString) view.getText();

            if (StringStyle.APPLY == captionSettings.mItalic) {
                if (null != renderingAttribute.mStrings) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                        if (1 < renderingAttribute.mStrings.size()) {
                            // if all each character has an italic property with font/background color spans under the kitkat version, the characters will be cut off.
                            // To avoid this, removed the properties and applied again in a lump if user set an italic property.
                            setStyleSpan(spannableString, StringStyle.REMOVE, Typeface.ITALIC);
                            removeSpan(spannableString, ForegroundColorSpan.class);
                            removeSpan(spannableString, BackgroundColorSpan.class);

                            if (DEFAULT == captionSettings.mFontColor) {
                                spannableString.setSpan(new ForegroundColorSpan(getBaseTextColor(renderingAttribute.mStrings)), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }

                            if (DEFAULT == captionSettings.mBackgroundColor) {
                                spannableString.setSpan(new BackgroundColorSpan(getBaseBackgroundColor(renderingAttribute.mStrings)), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }
                    }
                }
            }

            setStyleSpan(spannableString, captionSettings.mItalic, Typeface.ITALIC);
            setStyleSpan(spannableString, captionSettings.mBold, Typeface.BOLD);

            view.setTypeface(captionSettings.mFontFamily);

            if (DEFAULT != captionSettings.mFontColor) {
                view.setBaseTextColor(captionSettings.mFontColor);
                // can apply font color after removing applied font colors of spannable string.
                removeSpan(spannableString, ForegroundColorSpan.class);
                spannableString.setSpan(new ForegroundColorSpan(captionSettings.mFontColor), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if (StringStyle.APPLY == captionSettings.mUnderLine) {
                spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (StringStyle.REMOVE == captionSettings.mUnderLine) {
                removeSpan(spannableString, UnderlineSpan.class);
            }

            if (DEFAULT != captionSettings.mBackgroundColor) {
                spannableString.setSpan(new BackgroundColorSpan(captionSettings.mBackgroundColor), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                view.setBaseBackgroundColors(spannableString);
            }

            if (DEFAULT != captionSettings.mWindowColor) {
                view.setBackgroundColor(captionSettings.mWindowColor);
            }

            setFontSize(view, captionSettings.mFontSize, captionSettings.mFontScale);

            if (EdgeStyle.DEFAULT != captionSettings.mEdgeStyle) {
                setEdgeStyle(view, captionSettings.mEdgeStyle, captionSettings.mEdgeColor, captionSettings.mEdgeWidth);
            }

            setPadding(view, captionSettings);

            if (DEFAULT != captionSettings.mGravity) {
                view.setGravity(captionSettings.mGravity);
            }

			
            NexLog.d(LOG_TAG, "FontColor : " + captionSettings.mFontColor + ", UnderLine : " + captionSettings.mUnderLine + ", BackgroundColor : " + captionSettings.mBackgroundColor + 
            				", WindowColor : " + captionSettings.mWindowColor + ", EdgeStyle : " + captionSettings.mEdgeStyle + ", Gravity : " + captionSettings.mGravity);

            setViewLayout(view, renderingAttribute, captionSettings.mRelativeWindowRect);
        }
    }

    private void setStyleSpan(SpannableString spannableString, StringStyle span, int style) {
        if (StringStyle.APPLY == span) {
            spannableString.setSpan(new StyleSpan(style), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (StringStyle.REMOVE == span) {
            StyleSpan[] styleSpans = spannableString.getSpans(0, spannableString.length(), StyleSpan.class);
            for (StyleSpan styleSpan : styleSpans) {
                if (style == styleSpan.getStyle()) {
                    spannableString.removeSpan(styleSpan);
                }
            }
        }
    }

    private <T> void removeSpan(SpannableString spannableString, Class<T> kind) {
        T[] styleSpans = spannableString.getSpans(0, spannableString.length(), kind);
        for (T styleSpan : styleSpans) {
            spannableString.removeSpan(styleSpan);
        }
    }

    private void setEdgeStyle(NexCaptionTextView view, EdgeStyle edgeStyle, int color, float thickness) {
        view.initEdgeStyle();

        if (EdgeStyle.DROP_SHADOW == edgeStyle) {
            view.setDropShadow(true, color);
        } else if (EdgeStyle.DEPRESSED == edgeStyle) {
            view.setDepressed(true, color);
        } else if (EdgeStyle.RAISED == edgeStyle) {
            view.setRaised(true, color);
        } else if (EdgeStyle.UNIFORM == edgeStyle) {
            // can apply UNIFORM after removing applied font colors of spannable string.
            SpannableString spannableString = (SpannableString) view.getText();
            removeSpan(spannableString, ForegroundColorSpan.class);
            view.setCaptionStroke(color, thickness);
        }
    }

    private SpannableStringBuilder makeSpannableString(ArrayList<NodeString> nodeStrings) {
        SpannableStringBuilder stringBuilder = null;

        if (null != nodeStrings) {
            for (NodeString nodeString : nodeStrings) {
                if (null == stringBuilder) {
                    stringBuilder = new SpannableStringBuilder();
                }

                if (nodeString.mItalic) {
                    if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                        nodeString.mString += ' ';
                    }
                }

                SpannableString text = new SpannableString(nodeString.mString);

                if (nodeString.mBold) {
                    text.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, nodeString.mString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (nodeString.mItalic) {
                    text.setSpan(new StyleSpan(Typeface.ITALIC), 0, nodeString.mString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (nodeString.mUnderLine) {
                    text.setSpan(new UnderlineSpan(), 0, nodeString.mString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (nodeString.mLineThrough) {
                    text.setSpan(new StrikethroughSpan(), 0, nodeString.mString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (0 != nodeString.mFontColor) {
                    text.setSpan(new ForegroundColorSpan(nodeString.mFontColor), 0, nodeString.mString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (0 != nodeString.mBackgroundColor) {
                    text.setSpan(new BackgroundColorSpan(nodeString.mBackgroundColor), 0, nodeString.mString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                stringBuilder.append(text);
            }
        }

        return stringBuilder;
    }

    private int getBaseTextColor(ArrayList<NodeString> nodeStrings) {
        int baseTextColor = 0;

        if (null != nodeStrings && !nodeStrings.isEmpty()) {
            int index = 0;

            for (NodeString nodeString : nodeStrings) {
                if ("\n".equals(nodeString.mString)) {
                    index++;
                } else {
                    break;
                }
            }

            if (0 < index && nodeStrings.size() == index) {
                index -= 1;
            }

            baseTextColor = nodeStrings.get(index).mFontColor;
        }

        return baseTextColor;
    }

    private int getBaseBackgroundColor(ArrayList<NodeString> nodeStrings) {
        int baseBackgroundColor = 0;

        if (null != nodeStrings && !nodeStrings.isEmpty()) {
            int index = 0;

            for (NodeString nodeString : nodeStrings) {
                if ("\n".equals(nodeString.mString)) {
                    index++;
                } else {
                    break;
                }
            }

            if (0 < index && nodeStrings.size() == index) {
                index -= 1;
            }

            baseBackgroundColor = nodeStrings.get(index).mBackgroundColor;
        }

        return baseBackgroundColor;
    }

    private ImageView makeImageView(NexCaptionRenderingAttribute renderingAttribute) {
        ImageView imageView = new ImageView(mContext);
        imageView.setImageBitmap(renderingAttribute.mImage);
        NexCaptionWindowRect userSetRect = new NexCaptionWindowRect(renderingAttribute.mCaptionSettings.mRelativeWindowRect);
        userSetRect.copyTouchedSettings(mUserCaptionSettings.mRelativeWindowRect);
        setViewLayout(imageView, renderingAttribute, userSetRect);
        NexLog.d(LOG_TAG, "image layout : " + userSetRect);
        return imageView;
    }

    private NexCaptionTextView makeCaptionView(NexCaptionRenderingAttribute renderingAttribute) {
        NexCaptionTextView captionView = null;
        SpannableStringBuilder spannableStringBuilder = makeSpannableString(renderingAttribute.mStrings);
        if (null != spannableStringBuilder) {
            captionView = new NexCaptionTextView(mContext);
            captionView.setIncludeFontPadding(false);

            captionView.setBaseTextColor(getBaseTextColor(renderingAttribute.mStrings));
            captionView.setBaseBackgroundColors(spannableStringBuilder);
            captionView.setText(spannableStringBuilder, TextView.BufferType.SPANNABLE);

            NexLog.d(LOG_TAG, "caption text : " + spannableStringBuilder);

            captionView.setVisibility(renderingAttribute.mVisibility);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                captionView.setTextDirection(renderingAttribute.mDirection);
            }

            NexCaptionSetting captionSetting = new NexCaptionSetting(renderingAttribute.mCaptionSettings);
            captionSetting.copyTouchedSettings(mUserCaptionSettings);

            if (DEFAULT != mUserCaptionSettings.mFontSize) { captionSetting.mFontSize = convertAdjustedUserFontSize(mRenderingArea, mUserCaptionSettings.mFontSize); }

            applyCaptionSettings(captionView, renderingAttribute, captionSetting);
        }

        return captionView;
    }

    private void updateCaptionSettings(NexCaptionRenderingAttribute renderingAttribute, NexCaptionSetting captionSettings) {
        if (renderingAttribute.view instanceof NexCaptionTextView) {
            NexCaptionSetting captionSettingApplied = captionSettings == null ? renderingAttribute.mCaptionSettings : captionSettings;
            applyCaptionSettings((NexCaptionTextView)renderingAttribute.view, renderingAttribute, captionSettingApplied);
        }
    }

    private void setViewMaxExtent(TextView view, int windowSize) {
        double size = (double) windowSize / 100;
        int maxWidth = (int)(mRenderingArea.width() * size);
        int maxHeight = mRenderingArea.height();
        view.setMaxWidth(maxWidth);
        view.setMaxHeight(maxHeight);
    }

    private void setViewExtent(TextView view, Rect rect) {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        if(rect.width() <= 0) {
            rect.right = rect.left + view.getMeasuredWidth();
        }
        if(rect.height() <= 0) {
            rect.bottom = rect.top + view.getMeasuredHeight();
        }

        NexLog.d(LOG_TAG, "setViewExtent rect : " + rect);
        NexLog.d(LOG_TAG, "setViewExtent getMeasuredHeight : " + view.getMeasuredHeight() + " , getMeasuredWidth " + view.getMeasuredWidth());

        view.setWidth(rect.width());
        view.setHeight(rect.height());
    }

    private void setPadding(NexCaptionTextView view, NexCaptionSetting captionSettings) {
        int paddingLeft = 0, paddingTop = 0, paddingRight = 0, paddingBottom = 0;

        if (DEFAULT != captionSettings.mPaddingLeft) {
            paddingLeft = captionSettings.mPaddingLeft;
        }

        if (DEFAULT != captionSettings.mPaddingTop) {
            paddingTop = captionSettings.mPaddingTop;
        }

        if (DEFAULT != captionSettings.mPaddingRight) {
            paddingRight = captionSettings.mPaddingRight;
        }

        if (DEFAULT != captionSettings.mPaddingRight) {
            paddingBottom = captionSettings.mPaddingBottom;
        }

        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    private void setViewLayout(View view, NexCaptionRenderingAttribute renderingAttribute, NexCaptionWindowRect captionWindowRect) {
        if (view instanceof NexCaptionTextView) {
            TextView textView = (TextView) view;
            // extent of view should be initialized before invoking measure()
            textView.setWidth(0); textView.setHeight(0);
            setViewMaxExtent(textView, renderingAttribute.mWindowSize > 0 ? renderingAttribute.mWindowSize : 100);
        }

        renderingAttribute.mWindowRect = getCaptionPosition(captionWindowRect, view);

        if (view instanceof NexCaptionTextView) {
            view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

            if (!captionWindowRect.userDefined || captionWindowRect.autoAdjustment) {
                adjustViewRect((TextView)view, renderingAttribute.mWindowRect);
            }

            setViewExtent((TextView)view, renderingAttribute.mWindowRect);
        }

        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        NexLog.d(LOG_TAG, "setViewLayout : " + renderingAttribute.mWindowRect); 

        view.layout(renderingAttribute.mWindowRect.left, renderingAttribute.mWindowRect.top,
                renderingAttribute.mWindowRect.right, renderingAttribute.mWindowRect.bottom);
    }

    private float convertAdjustedUserFontSize(Rect renderingArea, float userFontSize) {
        float adjustedUserFontSize = 0;

        if (null != renderingArea) {
            float oneCell = NexCaptionExtractor.getFontSize(renderingArea);
            adjustedUserFontSize = userFontSize * oneCell / NexCaptionExtractor.DEFAULT_FONT_SIZE_DIP;
            NexLog.d(LOG_TAG, "convertAdjustedUserFontSize : " + adjustedUserFontSize + " , userFontSize : " + userFontSize + " , one cell is : " + oneCell);
        }

        return adjustedUserFontSize;
    }

    private void setFontSize(TextView textView, float fontSize, float fontScale) {
        float fontSizeApplied = fontSize;
        if (DEFAULT != fontScale) {
            fontSizeApplied *= fontScale;
        }

        NexLog.d(LOG_TAG, "last fontSizeApplied : " + fontSizeApplied + " , fontSize : " + fontSize + " , fontScale : " + fontScale);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSizeApplied);
    }

    private Rect getCaptionPosition(NexCaptionWindowRect relativeRect, View view) {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int width = view.getMeasuredWidth(); int height = view.getMeasuredHeight();

        NexLog.d(LOG_TAG, "relativeRect x : " + relativeRect.xPercent + ", y : " + relativeRect.yPercent + ", width : " + relativeRect.widthPercent + ", height : " + relativeRect.heightPercent);
        NexLog.d(LOG_TAG, "getCaptionPosition getMeasuredWidth: " + width + " , getMeasuredHeight : " + height);
        NexLog.d(LOG_TAG, "relativeRect userDefined: " + relativeRect.userDefined + " , autoAdjustment : " + relativeRect.autoAdjustment);

        return mCaptionExtractor.getCaptionPosition(relativeRect, width, height);
    }

    private void adjustViewRect(TextView view, Rect winRect) {
        int lineCount = view.getLineCount();
        int height = view.getLineHeight() * lineCount + view.getPaddingBottom() + view.getPaddingTop();
        int originHeight = winRect.height();

        int measuredHeight = view.getMeasuredHeight();
        int appliedHeight = Math.max(measuredHeight, height);

        NexLog.d(LOG_TAG, "adjustViewRect padding bottom " + view.getPaddingBottom() + ", padding top : " + view.getPaddingTop());
        NexLog.d(LOG_TAG, "adjustViewRect height " + height + ", measuredHeight : " + measuredHeight + " , appliedHeight : " + appliedHeight);
        NexLog.d(LOG_TAG, "adjustViewRect line count : " + lineCount + " , height : " + appliedHeight + " , originHeight : " + originHeight);

        if (appliedHeight > originHeight) {
            if (winRect.top > mRenderingArea.height() / 2) {
                winRect.top = winRect.bottom - appliedHeight;
            } else {
                winRect.bottom = winRect.top + appliedHeight;
            }
        }
    }

    private ArrayList<NexCaptionRenderingAttribute> makeRenderingAttribute(NexClosedCaption data) {
        ArrayList<NexCaptionRenderingAttribute> list = null;
        if (null != mCaptionExtractor) {
            list = mCaptionExtractor.extract(data);
        }

        if (null != list) {
            for (NexCaptionRenderingAttribute renderingAttribute : list) {
                if (null != renderingAttribute.mStrings) {
                    renderingAttribute.view = makeCaptionView(renderingAttribute);
                } else if (null != renderingAttribute.mImage) {
                    renderingAttribute.view = makeImageView(renderingAttribute);
                }
            }
        }

        return list;
    }

    private void avoidCollisionRects() {
        List<CollisionRectHelper.CollisionRect> listRect = mCollisionRectHelper.avoidCollisionRects();

        if (null != listRect) {
            Iterator<NexCaptionRenderingAttribute> iterator = mRenderingAttributes.iterator();

            for (CollisionRectHelper.CollisionRect collisionRect : listRect) {
                while (iterator.hasNext()) {
                    NexCaptionRenderingAttribute attribute = iterator.next();
                    if (attribute.id == collisionRect.id) {
                        attribute.mWindowRect = collisionRect.rcRect;
                        attribute.view.layout(attribute.mWindowRect.left, attribute.mWindowRect.top,
                                attribute.mWindowRect.right, attribute.mWindowRect.bottom);
                        break;
                    }
                }
            }
        }
    }
}

class NexCaptionExtractorFactory {
    static NexCaptionExtractor create(int textType) {
        NexCaptionExtractor extractor = null;

        if (NexContentInformation.NEX_TEXT_TTML == textType) {
            extractor = new NexTTMLExtractor();
        } else if (NexContentInformation.NEX_TEXT_WEBVTT == textType) {
            extractor = new NexWebVTTExtractor();
        } else if (NexContentInformation.NEX_TEXT_CEA == textType || NexContentInformation.NEX_TEXT_CEA608 == textType) {
            extractor = new NexCEA608CaptionExtractor();
        } else if (NexContentInformation.NEX_TEXT_CEA708 == textType) {
            extractor = new NexCEA708CaptionExtractor();
        } else if (NexContentInformation.NEX_TEXT_EXTERNAL_SMI == textType ||
                    NexContentInformation.NEX_TEXT_EXTERNAL_SRT == textType ||
                    NexContentInformation.NEX_TEXT_EXTERNAL_SUB == textType) {
            extractor = new NexSubtitleExtractor();
        }

        return extractor;
    }
}

interface CaptionExtractorCommonInterface {
    Rect getCaptionPosition(NexCaptionWindowRect relativeRect, int viewWidth, int viewHeight);
    void clear();
}

abstract class NexCaptionExtractor implements CaptionExtractorCommonInterface {
    static final float DEFAULT_HORIZONTAL_CELL = 32.0f;
    static final float DEFAULT_VERTICAL_CELL = 15.0f;
    static final float DEFAULT_FONT_SIZE_DIP = 19.0f;
    private static final float FONT_SIZE_RATE_CELL = 0.85f;
    static final String LOG_TAG = "NexCaptionExtractor";

    private SparseIntArray mappedFontColors;

    abstract void setRenderingArea(Rect renderingArea, float scale);
    abstract ArrayList<NexCaptionRenderingAttribute> extract(NexClosedCaption data);

    static float getFontSize(Rect renderingArea) {
        float fontSize = 0;
        if (null != renderingArea) {
            fontSize = renderingArea.height() / DEFAULT_VERTICAL_CELL * FONT_SIZE_RATE_CELL;
            NexLog.d(LOG_TAG, "get font size by default as a cell : "  + fontSize);
        }

        return fontSize;
    }

    static float getRelativeFontSize(Rect renderingArea, float fontSize) {
        float relativeFontSize = 0;
        if (null != renderingArea) {
            float anchorSize = DEFAULT_VERTICAL_CELL / (float)renderingArea.height();
            relativeFontSize = fontSize * anchorSize;
            NexLog.d(LOG_TAG, "get relative font size by default : "  + relativeFontSize + " , fontSize : " + fontSize);
        }

        return relativeFontSize;
    }

    void setMappedFontColors(SparseIntArray mappedFontColors) { this.mappedFontColors = mappedFontColors; }

    int replaceMappedFontColors(int color) {
        int replacedColor = color;

        if (null != mappedFontColors) {
            replacedColor = mappedFontColors.get(color, color);
        }

        return replacedColor;
    }
}

class CollisionRectHelper {
    private ComparatorRect mComparator = new ComparatorRect();
    private ArrayList<CollisionRect> mlistRect = new ArrayList<CollisionRect>();
    private Rect mIntersect = new Rect();
    private Rect mIntersectCompare = new Rect();

    static final String LOG_TAG = "CollisionRectHelper";

    private int mLeft = 0;
    private int mTop = 0;
    private int mWidth = 0;
    private int mHeight = 0;

    class CollisionRect {
        Rect rcRect = null;
        int id = 0;

        CollisionRect(Rect rcRect, int id) {
            this.rcRect = rcRect;
            this.id = id;
        }
    }

    private enum SortDirection {
        NONE,
        FROM_LEFT,
        FROM_TOP,
        FROM_RIGHT,
        FROM_BOTTOM
    }

    private class ComparatorRect implements Comparator<CollisionRect> {
        SortDirection m_eDirection = SortDirection.FROM_TOP;

        void setDirection(SortDirection eDirection) {
            m_eDirection = eDirection;
        }

        @Override
        public int compare(CollisionRect lhs, CollisionRect rhs) {
            int nTarget = 0;
            int nCompare = 0;

            int nResult = 0;
            int nOffset = 1;

            switch (m_eDirection) {
                case FROM_LEFT:
                    break;
                case FROM_TOP:
                    nTarget = lhs.rcRect.top;
                    nCompare = rhs.rcRect.top;
                    break;
                case FROM_RIGHT:
                    break;
                case FROM_BOTTOM:
                    nTarget = lhs.rcRect.bottom;
                    nCompare = rhs.rcRect.bottom;
                    nOffset = -1;
                    break;
                default:
                    break;
            }

            if (null != lhs.rcRect && null != rhs.rcRect) {
                if (nTarget >= nCompare)
                {
                    nResult = nOffset;
                }
                else if (nTarget < nCompare)
                {
                    nResult = -nOffset;
                }
            }
            return nResult;
        }
    }

    public void clear() {
        if (!mlistRect.isEmpty()) {
            mlistRect.clear();
        }
    }

    public void add(int id, Rect rect) {
        if (null != rect) {
            mlistRect.add(new CollisionRect(rect, id));
        }
    }

    public void remove(int id) {
        if (!mlistRect.isEmpty()) {
            Iterator<CollisionRect> iterator = mlistRect.iterator();
            while (iterator.hasNext()) {
                CollisionRect rect = iterator.next();
                if (rect.id == id) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    void setBoundary(int left, int top, int width, int height) {
        mLeft = left; mTop = top; mWidth = width; mHeight = height;
    }

    ArrayList<CollisionRect> avoidCollisionRects() {
    	try{
	        ArrayList<CollisionRect> listRect = null;
	        if (null != mComparator && null != mlistRect) {
	            boolean bArranged = false;

	            // descending sort
	            mComparator.setDirection(SortDirection.FROM_BOTTOM);
	            Collections.sort(mlistRect, mComparator);

	            // check and move windows from boundary
	            if (reArrangeWindowByBoundary(mlistRect)) { bArranged = true; }

	            // check and move overlapped windows in descending list
	            if (reArrangeWindowByIntersection(mlistRect, SortDirection.FROM_BOTTOM)) { bArranged = true; }

	            // ascending sort
	            mComparator.setDirection(SortDirection.FROM_TOP);
	            Collections.sort(mlistRect, mComparator);

	            // check and move windows from boundary
	            if (reArrangeWindowByBoundary(mlistRect))
	            {
	                // check and move overlapped windows in ascending list
	                reArrangeWindowByIntersection(mlistRect, SortDirection.FROM_TOP);
	                bArranged = true;
	            }

	            if (bArranged) {
	                listRect = mlistRect;
	            }
	        }

	        return listRect;
	    } catch (ConcurrentModificationException ex) {
            NexLog.d(LOG_TAG, "Skipping avoidCollisionRects because of an exception");
            return null;
        }
    }

    private boolean reArrangeWindowByBoundary(ArrayList<CollisionRect> list) {
        boolean bOverBoundary = false;

        if (null != list) {
            for (int i = 0; i < list.size(); ++i) {
                CollisionRect rcCheckingBoundary = list.get(i);

                if (null != rcCheckingBoundary && CheckAndMoveRectFromBoundary(rcCheckingBoundary.rcRect)) {
                    list.set(i, rcCheckingBoundary);
                    bOverBoundary = true;
                    NexLog.d(LOG_TAG, "reArrangeWindowByBoundary id : " + rcCheckingBoundary.id + ", rect : " + rcCheckingBoundary.rcRect);
                }
            }
        }

        return bOverBoundary;
    }

    private boolean reArrangeWindowByIntersection(ArrayList<CollisionRect> list, SortDirection eDefaultDirection) {
        boolean bReArrange = false;

        if (null != list) {
            for (int i = 0; i < list.size(); ++i) {
                if (i < list.size() - 1) {
                    CollisionRect rcFirst = list.get(i);
                    CollisionRect rcSecond = list.get(i+1);

                    if (checkAndMoveRectFromIntersect(rcFirst.rcRect, rcSecond.rcRect, eDefaultDirection)) {
                        list.set(i+1, rcSecond);
                        bReArrange = true;
                        NexLog.d(LOG_TAG, "reArrangeWindowByIntersection rcFirst.rcRect : " + rcFirst.rcRect + ", rcSecond.rcRect : " + rcSecond.rcRect);
                    }
                }
            }
        }

        return bReArrange;
    }

    private boolean checkAndMoveRectFromIntersect(Rect rcBase, Rect rcCompare, SortDirection eDirection) {
        boolean bResult = false;

        if (null != rcBase && null != rcCompare && null != mIntersect && null != mIntersectCompare) {
            mIntersect.set(rcBase);
            mIntersectCompare.set(rcCompare);

            if (mIntersect.intersect(mIntersectCompare)) {
                int nInterval = 0;
                if (eDirection == SortDirection.FROM_BOTTOM) {
                    if (rcCompare.bottom > rcBase.top) {
                        nInterval = rcCompare.bottom - rcBase.top;
                    } else {
                        nInterval = rcCompare.height() + rcBase.top - rcCompare.top;
                    }

                    nInterval *= -1;
                } else if (eDirection == SortDirection.FROM_TOP) {
                    if (rcCompare.top > rcBase.bottom) {
                        nInterval = rcBase.bottom - rcCompare.top;
                    } else {
                        nInterval = rcCompare.height() + rcBase.bottom - rcCompare.bottom;
                    }
                }

                rcCompare.top += nInterval;
                rcCompare.bottom += nInterval;
                bResult = true;
            }
        }

        return bResult;
    }

    private boolean CheckAndMoveRectFromBoundary(Rect rcCompare) {
        boolean bResult = false;

        if (null != rcCompare) {
            int nInterval;
            if (rcCompare.bottom > mHeight + mTop) {
                nInterval = mHeight + mTop - rcCompare.bottom;
                rcCompare.top += nInterval;
                rcCompare.bottom += nInterval;
                bResult = true;
            }

            if (rcCompare.top < mTop) {
                nInterval = mTop - rcCompare.top;
                rcCompare.top += nInterval;
                rcCompare.bottom += nInterval;
                bResult = true;
            }

            if (rcCompare.left < mLeft) {
                nInterval = mLeft - rcCompare.left;
                rcCompare.left += nInterval;
                rcCompare.right += nInterval;
                bResult = true;
            }

            if (rcCompare.right > mLeft + mWidth) {
                nInterval = mWidth + mLeft - rcCompare.right;
                rcCompare.left += nInterval;
                rcCompare.right += nInterval;
                bResult = true;
            }
        }

        return bResult;
    }
}

class NexSubtitleExtractor extends NexCaptionExtractor {
    private Rect mRenderingArea = new Rect();

    private NexCaptionWindowRect makeRelativePosition() {
        NexCaptionWindowRect windowRect = new NexCaptionWindowRect();

        windowRect.xPercent = 10;
        windowRect.yPercent = 75;
        windowRect.widthPercent = 80;
        windowRect.heightPercent = 20;

        return windowRect;
    }

    @Override
    public Rect getCaptionPosition(NexCaptionWindowRect relativeRect, int viewWidth, int viewHeight) {
        Rect rect = new Rect();

        rect.left = mRenderingArea.width() * relativeRect.xPercent / 100 + mRenderingArea.left;
        rect.top = mRenderingArea.height() * relativeRect.yPercent / 100 + mRenderingArea.top;
        rect.right = mRenderingArea.width() * relativeRect.widthPercent / 100 + rect.left;
        rect.bottom = mRenderingArea.height() * relativeRect.heightPercent / 100 + rect.top;

        return rect;
    }

    @Override
    public void clear() {

    }

    @Override
    void setRenderingArea(Rect renderingArea, float scale) {
        mRenderingArea = renderingArea;
    }

    @Override
    ArrayList<NexCaptionRenderingAttribute> extract(NexClosedCaption data) {
        ArrayList<NexCaptionRenderingAttribute> renderingAttributeList = null;
        if (null != data) {
            renderingAttributeList = new ArrayList<NexCaptionRenderingAttribute>();

            ArrayList<NodeString> string = getNodeString(data);
            NexCaptionRenderingAttribute renderingAttribute = new NexCaptionRenderingAttribute();

            if (null != string) {
                renderingAttribute.mStartTime = data.getCaptionTime()[0];
                renderingAttribute.mEndTime = data.getCaptionTime()[1];
                renderingAttribute.mRemoveTime = data.getCaptionTime()[0];
                renderingAttribute.mWindowSize = 100;
                renderingAttribute.mStrings = getNodeString(data);
                renderingAttribute.mRelativeFontSize = getRelativeFontSize(mRenderingArea, getFontSize(mRenderingArea));
                renderingAttribute.mCaptionSettings = getCaptionSettings();
            } else {
                renderingAttribute.mRemoveTime = Integer.MAX_VALUE;
            }

            renderingAttributeList.add(renderingAttribute);
        }

        return renderingAttributeList;
    }

    private ArrayList<NodeString> getNodeString(NexClosedCaption data) {
        ArrayList<NodeString> nodeStrings = null;

        byte[] texts = data.getTextData();

        if (null != texts) {
            try {
                nodeStrings = new ArrayList<NodeString>();

                NodeString string = new NodeString();
                string.mFontColor = replaceMappedFontColors(Color.WHITE);
                string.mString = new String(texts, convertEncodingType(data.getEncodingType()));

                nodeStrings.add(string);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return nodeStrings;
    }

    private NexCaptionSetting getCaptionSettings() {
        NexCaptionSetting captionSettings = new NexCaptionSetting();

        captionSettings.mFontSize = getFontSize(mRenderingArea);
        captionSettings.mGravity = Gravity.CENTER;
        captionSettings.mWindowColor = 0x80000000;
        captionSettings.mRelativeWindowRect = makeRelativePosition();

        int mDefaultPaddingValue = 10;
        captionSettings.mPaddingLeft = captionSettings.mPaddingTop = captionSettings.mPaddingRight = captionSettings.mPaddingBottom = mDefaultPaddingValue;

        return captionSettings;
    }
}

class NodeString {
    String mString;
    int mBackgroundColor;
    int mFontColor;
    boolean mBold;
    boolean mItalic;
    boolean mUnderLine;
    boolean mLineThrough;
    boolean mOverLine;
    boolean mSuperscript;
    boolean mSubscript;
}

class NexCaptionRenderingAttribute {
    int mStartTime;
    int mEndTime;
    int mRemoveTime;
    int mDirection;
    int mZOrder;
    int mVisibility;
    boolean mWrap;
    int mWindowSize;

    Rect mWindowRect;
    float mRelativeFontSize;

    Bitmap mImage;
    ArrayList<NodeString> mStrings;

    NexCaptionSetting mCaptionSettings;

    View view;
    int id;
    boolean removeById;
}

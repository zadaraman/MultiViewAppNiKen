package com.nexstreaming.nexplayerengine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nexstreaming.nexplayerengine.NexClosedCaption.CaptionColor;

import java.util.ArrayList;

/**
*  This class is used internally to manage captions.  Please do not use.  
*  The other caption-related classes in NexPlayer&trade;&nbsp;should be used instead.
*/
public class NexCaptionTextView extends TextView {
	int m_nEdgeColor = 0;
	int m_baseFontColor = 0;
	float m_strokeWidth = 1.5f;
	int mDropColor = 0;

    boolean redrawBackground = false;
	ArrayList<SpanDrawInfo> mBackgroundColorSpanInfo = new ArrayList<SpanDrawInfo>();

	private ViewGroup.LayoutParams params = null;

	protected float[] APPLY_SHADOW_PARAM = {0.0f, 0.0f, 0.0f};

	class SubStringDrawInfo
	{
		int color = 0;
		int start = 0;
		int end = 0;
	}

	private class SpanDrawInfo
	{
		CharacterStyle span = null;
		int start = 0;
		int end = 0;
	}

	public NexCaptionTextView(Context context) {
		super(context);

		params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		setLayoutParams(params);

		init();
	}
	
	protected void setCaptionStroke(CaptionColor color, int opacity, float strokeWidth)
	{
		if (null != color)
		{
			m_strokeWidth = strokeWidth;
			m_nEdgeColor = getColorFromCapColor(color, opacity);
		}
		else
		{
			m_nEdgeColor = 0;
		}
	}
	
	protected void setCaptionStroke(int color, float strokeWidth)
	{
		m_nEdgeColor = color;
		m_strokeWidth = strokeWidth;
	}
	
	protected void setRaised(boolean isTrue)
	{
		if(isTrue)
        {
            getPaint().setShadowLayer(NexClosedCaption.DEFAULT_RAISED_PARAM[0],
                    NexClosedCaption.DEFAULT_RAISED_PARAM[1],
                    NexClosedCaption.DEFAULT_RAISED_PARAM[2], Color.BLACK);
        }
        else
        {
            getPaint().setShadowLayer(0, 0, 0, 0);
        }
	}
	
	protected void setRaised(boolean isTrue, int raisedColor)
	{
		if(isTrue)
        {
			mDropColor = raisedColor;
            getPaint().setShadowLayer(NexClosedCaption.DEFAULT_RAISED_PARAM[0],
                    NexClosedCaption.DEFAULT_RAISED_PARAM[1],
                    NexClosedCaption.DEFAULT_RAISED_PARAM[2], raisedColor);

			APPLY_SHADOW_PARAM = NexClosedCaption.DEFAULT_RAISED_PARAM.clone();
        }
        else
        {
            getPaint().setShadowLayer(0, 0, 0, 0);
			mDropColor = 0;
        }
	}
	
	protected void setDepressed(boolean isTrue)
	{
		if(isTrue)
        {
            getPaint().setShadowLayer(NexClosedCaption.DEFAULT_DEPRESSED_PARAM[0],
                    NexClosedCaption.DEFAULT_DEPRESSED_PARAM[1],
                    NexClosedCaption.DEFAULT_DEPRESSED_PARAM[2], Color.BLACK);
        }
        else
        {
            getPaint().setShadowLayer(0, 0, 0, 0);
        }
	}
	
	protected void setDepressed(boolean isTrue, int depColor)
	{
		if(isTrue)
        {
			mDropColor = depColor;
            getPaint().setShadowLayer(NexClosedCaption.DEFAULT_DEPRESSED_PARAM[0],
                    NexClosedCaption.DEFAULT_DEPRESSED_PARAM[1],
                    NexClosedCaption.DEFAULT_DEPRESSED_PARAM[2], depColor);

			APPLY_SHADOW_PARAM = NexClosedCaption.DEFAULT_DEPRESSED_PARAM.clone();
        }
        else
        {
            getPaint().setShadowLayer(0, 0, 0, 0);
			mDropColor = 0;
        }
	}

	protected void setUniform(boolean isTrue)
	{
		if(isTrue)
		{
			m_strokeWidth = 1.5f;
			m_nEdgeColor = getColorFromCapColor(CaptionColor.BLACK, 255);
		}
        else
        {
            m_nEdgeColor = 0;
        }
	}
	
	protected void setDropShadow(boolean isTrue)
	{
		if(isTrue)
		{
			getPaint().setShadowLayer(NexClosedCaption.DEFAULT_SHADOW_PARAM[0], 
					NexClosedCaption.DEFAULT_SHADOW_PARAM[1], 
					NexClosedCaption.DEFAULT_SHADOW_PARAM[2], NexClosedCaption.DEFAULT_SHADOW_COLOR);
		}
		else
		{
			getPaint().setShadowLayer(0, 0, 0, 0);
		}
	}

	protected void setDropShadow(boolean isTrue, int color)
	{
		if(isTrue)
		{
			mDropColor = color;
			getPaint().setShadowLayer(NexClosedCaption.DEFAULT_SHADOW_PARAM[0], 
					NexClosedCaption.DEFAULT_SHADOW_PARAM[1], 
					NexClosedCaption.DEFAULT_SHADOW_PARAM[2], color);

			APPLY_SHADOW_PARAM = NexClosedCaption.DEFAULT_SHADOW_PARAM.clone();
		}
		else
		{
			getPaint().setShadowLayer(0, 0, 0, 0);
			mDropColor = 0;
		}
	}

	protected void setDropShadow(boolean isTrue, float radius, float dx, float dy, int color)
	{
		if(isTrue)
		{
			mDropColor = color;
			getPaint().setShadowLayer(radius, dx, dy, color);

			APPLY_SHADOW_PARAM[0] = radius;
			APPLY_SHADOW_PARAM[1] = dx;
			APPLY_SHADOW_PARAM[2] = dy;
		}
		else
		{
			getPaint().setShadowLayer(0, 0, 0, 0);
			mDropColor = 0;
		}
	}
	
	private void init()
	{
		WrapSetLayerType();
		m_nEdgeColor = 0;
		mDropColor = 0;
        m_baseFontColor = 0;

		APPLY_SHADOW_PARAM[0] = 0;
		APPLY_SHADOW_PARAM[1] = 0;
		APPLY_SHADOW_PARAM[2] = 0;

        redrawBackground = false;
        mBackgroundColorSpanInfo.clear();
	}

	protected void initEdgeStyle() {
		m_nEdgeColor = 0;
		m_strokeWidth = 1.0f;
		mDropColor = 0;

		getPaint().setShadowLayer(0, 0, 0, 0);
		APPLY_SHADOW_PARAM[0] = 0;
		APPLY_SHADOW_PARAM[1] = 0;
		APPLY_SHADOW_PARAM[2] = 0;
	}
	
	@SuppressLint("NewApi")
	private void WrapSetLayerType()
	{
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
		{
			setLayerType(ViewGroup.LAYER_TYPE_SOFTWARE, null);
		}
	}
	
	protected int getColorFromCapColor(CaptionColor cColor, int cOpacity)
	{
		int setColor = cColor.getFGColor();
		return Color.argb(cOpacity, Color.red(setColor), Color.green(setColor), Color.blue(setColor));
	}

	protected void setBaseTextColor(int color)
	{
		m_baseFontColor = color;
	}

	protected void setBaseBackgroundColors(Spannable spannableString) {
		mBackgroundColorSpanInfo.clear();
		BackgroundColorSpan[] backgroundColorSpans = spannableString.getSpans(0, spannableString.length(), BackgroundColorSpan.class);
		for (BackgroundColorSpan backgroundColorSpan : backgroundColorSpans) {
			SpanDrawInfo spanDrawInfo = new SpanDrawInfo();
			spanDrawInfo.span = new BackgroundColorSpan(backgroundColorSpan.getBackgroundColor());
			spanDrawInfo.start = spannableString.getSpanStart(backgroundColorSpan);
			spanDrawInfo.end = spannableString.getSpanEnd(backgroundColorSpan);
			mBackgroundColorSpanInfo.add(spanDrawInfo);
		}
	}

	@Override
    protected void onDraw(Canvas canvas) {
		if (redrawBackground && 0 < mBackgroundColorSpanInfo.size()) {
			CharSequence charSequence = getText();
			if (charSequence instanceof SpannableString) {
				SpannableString spannableString = (SpannableString) charSequence;

				for (int i = 0; i < mBackgroundColorSpanInfo.size(); ++i) {
					SpanDrawInfo spanDrawInfo = mBackgroundColorSpanInfo.get(i);
					spannableString.setSpan(spanDrawInfo.span, spanDrawInfo.start, spanDrawInfo.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}

        if (0 != m_nEdgeColor) {
            getPaint().setStyle(Paint.Style.STROKE);
            getPaint().setStrokeWidth(m_strokeWidth);
            setTextColor(m_nEdgeColor);
            super.onDraw(canvas);

            CharSequence charSequence = getText();
            if (charSequence instanceof SpannableString) {
                SpannableString spannableString = (SpannableString) charSequence;

                BackgroundColorSpan[] backgroundColorSpans = spannableString.getSpans(0, spannableString.length(), BackgroundColorSpan.class);
                for (BackgroundColorSpan backgroundColorSpan : backgroundColorSpans) {
                    spannableString.removeSpan(backgroundColorSpan);
                }
            }
            redrawBackground = true;
            getPaint().setStyle(Paint.Style.FILL);
            setTextColor(m_baseFontColor);
        } else {
            redrawBackground = false;
            if (0 != mDropColor) {
                getPaint().setStyle(Paint.Style.FILL);
                getPaint().setShadowLayer(0, 0, 0, 0);
                super.onDraw(canvas);

                CharSequence charSequence = getText();
                if (charSequence instanceof SpannableString) {
                    SpannableString spannableString = (SpannableString) charSequence;

                    BackgroundColorSpan[] backgroundColorSpans = spannableString.getSpans(0, spannableString.length(), BackgroundColorSpan.class);
                    for (BackgroundColorSpan backgroundColorSpan : backgroundColorSpans) {
                        spannableString.removeSpan(backgroundColorSpan);
                    }
                }
                redrawBackground = true;
                getPaint().setShadowLayer(APPLY_SHADOW_PARAM[0], APPLY_SHADOW_PARAM[1], APPLY_SHADOW_PARAM[2], mDropColor);
            }
        }

        super.onDraw(canvas);
    }
}

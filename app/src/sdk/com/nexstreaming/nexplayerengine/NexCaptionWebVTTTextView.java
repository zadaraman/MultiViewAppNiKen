package com.nexstreaming.nexplayerengine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

/**
*  This class is used internally to manage captions.  Please do not use.  
*  The other caption-related classes in NexPlayer&trade;&nbsp;should be used instead.
*/
class NexCaptionWebVTTTextView extends NexCaptionTextView {
	
	private static final String TAG = "[CaptionTextView]";
	
	public NexCaptionWebVTTTextView(Context context) {
		super(context);
	}
	
	public NexCaptionWebVTTTextView(Context context, AttributeSet attrs) {
		super(context);
	}
	
	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	    final int updateMeasuredWith = getMeasuredWidth() + (int) (getTextSize()/3);
	    setMeasuredDimension(updateMeasuredWith, getMeasuredHeight());
	    
	}
	
	@Override  
    protected void onDraw(Canvas canvas) {
		getPaint().setStyle(Style.FILL);
        if (0 != m_nEdgeColor)
        {
            setTextColor(m_baseFontColor);
            
            super.onDraw(canvas);            
            
            getPaint().setStyle(Style.STROKE);
            getPaint().setStrokeWidth(m_strokeWidth);
            setTextColor(m_nEdgeColor);
        }

        super.onDraw(canvas);
    } 
}

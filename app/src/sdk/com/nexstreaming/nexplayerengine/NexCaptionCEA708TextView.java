package com.nexstreaming.nexplayerengine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;

/**
*  This class is used internally to manage captions.  Please do not use.  
*  The other caption-related classes in NexPlayer&trade;&nbsp;should be used instead.
*/
class NexCaptionCEA708TextView extends NexCaptionTextView {
	
	private static final String TAG = "[CaptionCEA708TextView]";

	Rect m_rcText = null;
	Rect m_rcLineBounds = null;
	Paint m_paint = null;
	int mHighLightColor = 0;
	
	public NexCaptionCEA708TextView(Context context) {
		super(context);
		init();
	}
	
	public NexCaptionCEA708TextView(Context context, AttributeSet attrs) {
		super(context);
		init();
	}

	private void init() {
		m_rcText = new Rect();
		m_rcLineBounds = new Rect();
		m_paint = new Paint();
		mHighLightColor = 0;
	}

	protected void setHLColor(int nColor) {
		mHighLightColor = nColor;
	}
	
	protected void drawWithBgColor(Canvas canvas)
	{
		if (0 != mHighLightColor)
    	{
    		String strText = getText().toString();
        	String[] textSplit = strText.split("(\r\n)");
        	
        	if (0 < textSplit.length)
        	{
	        	int nGravityPos = 0;
	        	int nWindowWidth = getWidth();
	        	m_paint.setColor(mHighLightColor);
	        	
	        	for (int i = 0; i < textSplit.length; ++i)
	        	{
	        		getLineBounds(i, m_rcLineBounds);
	        		//NexLog.d(TAG, "Caption text view Highlight before m_rcLineBounds.left: " + m_rcLineBounds.left + " m_rcLineBounds.right: " + m_rcLineBounds.right + " m_rcLineBounds.top: " + m_rcLineBounds.top + " m_rcLineBounds.bottom: " + m_rcLineBounds.bottom);
	        		
	        		m_rcText.setEmpty();
	        		String strSplitText = textSplit[i].toString();
	        		getPaint().getTextBounds(strSplitText, 0, strSplitText.length(), m_rcText);
	        	
	        		String strTrimSplitText = strSplitText.trim();
	        		
	        		//NexLog.d(TAG, "Caption text view Highlight strSplitText: " + strSplitText);
	        		
	        		int nMeasureTextWidth = m_rcText.width();
	        		
	        		int nLeftOffset = 0, nRightOffset = 0;
	        		int nRealRemainedSpace = nWindowWidth - nMeasureTextWidth - getPaddingLeft()/2;
	        		
	        		//NexLog.d(TAG, "Caption text view Highlight nWindowWidth: " + nWindowWidth  + " getPaddingLeft: " + getPaddingLeft() + " measureText: "+ nMeasureTextWidth);		        		
	        		//NexLog.d(TAG, "Caption text view Highlight before m_rcText.left: " + m_rcText.left + " m_rcText.right: " + m_rcText.right + " m_rcText.top: " + m_rcText.top + " m_rcText.bottom: " + m_rcText.bottom);
	        		
	        		int gravity = getGravity();
	        		if (Gravity.CENTER == gravity)
		        	{
	        			int nOriginLeft = m_rcText.left;
//	        			nLeftOffset = getPaddingLeft() / 2;
//	        			nRightOffset = getPaddingLeft() / 2 + nLeftOffset / 2;
	        			
	        			nLeftOffset = getPaddingLeft();
	        			nRightOffset = nLeftOffset;
	        			
	        			int nAdjustLeft = (nRealRemainedSpace - nOriginLeft)/ 2;
	        			nGravityPos = nAdjustLeft;
	        			
	        			m_rcText.left = nGravityPos + nOriginLeft - nLeftOffset;
	        			
	        			if (m_rcText.left < nLeftOffset)
	        				m_rcText.left = nLeftOffset;
	        			
	        			m_rcText.right = nGravityPos + nOriginLeft + nMeasureTextWidth + nRightOffset;
	        			
	        			if (nWindowWidth - m_rcText.right <= getPaddingRight())
	        				m_rcText.right = nWindowWidth - nRightOffset;
	        			
	        			//NexLog.d(TAG, "Caption text view Highlight nGravityPos: " + nGravityPos + " nOriginLeft: " + nOriginLeft + " nLeftOffset: " + nLeftOffset);
		        	}
	        		else if (Gravity.RIGHT == gravity)
	        		{
	        			nGravityPos = Math.round(nRealRemainedSpace - getPaddingLeft() - getPaddingRight());
	        			nLeftOffset = getPaddingLeft()/2;
	        			
		        		if (0 > nGravityPos)
		        			nGravityPos = 0;
		        		
		        		if (0 > nGravityPos - nLeftOffset)
	        				nLeftOffset = 0;
		        		
		        		//NexLog.d(TAG, "Caption text view Highlight nGravityPos: " + nGravityPos + " getGravity(): " + gravity);
	        		}
	        		else
	        		{
	        			nLeftOffset = getPaddingLeft();
	        			nRightOffset = nLeftOffset; 
	        			
	        			int nOriginLeft = m_rcText.left;
	        			
	        			m_rcText.left = getPaddingLeft() + nOriginLeft - nLeftOffset;
	        			
	        			if (m_rcText.left < nLeftOffset)
	        				m_rcText.left = nLeftOffset;
	        			
	        			m_rcText.right = getPaddingLeft() + nOriginLeft + nMeasureTextWidth + nRightOffset;
	        			
	        			if (nWindowWidth - m_rcText.right <= getPaddingRight())
	        				m_rcText.right = nWindowWidth - nRightOffset;
	        		}
	        		
	        		//NexLog.d(TAG, "Caption text view nRealRemainedSpace: " + nRealRemainedSpace + " nLeftOffset: " + nLeftOffset + " nRightOffset: " + nRightOffset);
	        		
	        		m_rcText.top = m_rcLineBounds.top;
	        		m_rcText.bottom = m_rcLineBounds.bottom;
	        	
	        		if (0 < strTrimSplitText.length())
	        		{
	        			//NexLog.d(TAG, "Caption text view Highlight after m_rcText.left: " + m_rcText.left + " m_rcText.right: " + m_rcText.right + " m_rcText.top: " + m_rcText.top + " m_rcText.bottom: " + m_rcText.bottom);
	        			canvas.drawRect(m_rcText, m_paint);	
	        		}
	        	}
        	}
    	}
	}
	
	@Override  
    protected void onDraw(Canvas canvas) {
		drawWithBgColor(canvas);
        super.onDraw(canvas);
    }
}

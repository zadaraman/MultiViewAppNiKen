package com.nexstreaming.nexplayerengine;

import android.content.Context;
import android.graphics.Canvas;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import com.nexstreaming.nexplayerengine.NexClosedCaption.CaptionColor;

import java.util.ArrayList;

/**
*  This class is used internally to manage captions.  Please do not use.  
*  The other caption-related classes in NexPlayer&trade;&nbsp;should be used instead.
*/
class NexCaptionTimedTextView extends NexCaptionTextView {
	
	private static final String TAG = "[CaptionTimedTextView]";
	
	ArrayList<SubStringDrawInfo> fgColorList = new ArrayList<SubStringDrawInfo>();
	ArrayList<SubStringDrawInfo> bgColorList = new ArrayList<SubStringDrawInfo>();
	
	public NexCaptionTimedTextView(Context context) {
		super(context);
	}
	
	public NexCaptionTimedTextView(Context context, AttributeSet attrs) {
		super(context);
	}
	
	protected void setFGColorByPosition(int fontColor, int start, int end)
	{		
		SubStringDrawInfo fgColorInfo = new SubStringDrawInfo();
		fgColorInfo.color = fontColor;
		fgColorInfo.start = start;
		fgColorInfo.end = end;
		fgColorList.add(fgColorInfo);
	}
	
	protected void setFGColorByPosition(CaptionColor fontColor, int fontOpacity, int start, int end)
	{
		SubStringDrawInfo fgColorInfo = new SubStringDrawInfo();
		fgColorInfo.color = getColorFromCapColor(fontColor, fontOpacity);
		fgColorInfo.start = start;
		fgColorInfo.end = end;
		fgColorList.add(fgColorInfo);
	}
	
	protected void setBGColorByPosition(int bgColor, int start, int end)
	{
		SubStringDrawInfo fgColorInfo = new SubStringDrawInfo();
		fgColorInfo.color = bgColor;
		fgColorInfo.start = start;
		fgColorInfo.end = end;
		bgColorList.add(fgColorInfo);
	}
	
	protected void setBGColorByPosition(CaptionColor bgColor, int bgOpacity, int start, int end)
	{
		SubStringDrawInfo bgColorInfo = new SubStringDrawInfo();
		bgColorInfo.color = getColorFromCapColor(bgColor, bgOpacity);
		bgColorInfo.start = start;
		bgColorInfo.end = end;
		bgColorList.add(bgColorInfo);
	}
	
	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	    final int updateMeasuredWith = getMeasuredWidth() + (int) (getTextSize()/3);
	    setMeasuredDimension(updateMeasuredWith, getMeasuredHeight());
	    
	}
	
	@Override  
    protected void onDraw(Canvas canvas) {
	    //getPaint().setStyle(Style.FILL);
	    
	    SpannableString spString = null;
        
	    if(!fgColorList.isEmpty() && 0 == m_nEdgeColor)
        {   
	    	spString = new SpannableString(getText());
            for(int a = 0; a < fgColorList.size(); a++)
            {
            	NexLog.d(TAG, "3GPP Caption text caption color = " + fgColorList.get(a).color);
            	ForegroundColorSpan fgColorSpan = new ForegroundColorSpan(fgColorList.get(a).color);		            
            	spString.setSpan(fgColorSpan, fgColorList.get(a).start, fgColorList.get(a).end, 0);
            }
            
            setText(spString);
            spString.removeSpan(spString.getSpans(0, spString.length(), ForegroundColorSpan.class));
        }
	    
	    super.onDraw(canvas);
    } 
}

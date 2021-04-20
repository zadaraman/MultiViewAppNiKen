package com.nexstreaming.nexplayerengine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView.BufferType;

import com.nexstreaming.nexplayerengine.NexClosedCaption.CaptionColor;
import com.nexstreaming.nexplayerengine.NexLogStringQueue.CharUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
* \brief This class defines a caption view for rendering and displaying CEA 708 closed captions.
* 
* All of the information about how CEA 708 closed captions is contained within a NexEIA708Struct object, and
* the caption view defined here provides an example text renderer which can be used to display the captions.
* 
* Whenever new CEA 708 closed caption data is received, it should be handled by calling the \link setEIA708CC \endlink method.
* 
* For other subtitle formats, including CEA 608 closed captions, please see the NexClosedCaption, NexCaptionRenderer, 
* and NexCaptionRendererForTimedText classes instead.
* 
* \see NexEIA708Struct
* \see NexClosedCaption
*
* \since version 6.1.2
*/
public class NexEIA708CaptionView extends ViewGroup {
	
	private static String TAG = "CAPTIONVIEW";

	private NexEIA708Struct m_eia708cc;
	
	/**
	 ** Display
	 */
	private float m_x_interval;
	private float m_y_interval;	

	private float m_x_rp_interval;
	private float m_y_rp_interval;	
	
	
	private float m_display_width;
	private float m_display_height;
	
	private int m_margin_left;
	private int m_margin_top;
	
	private int m_nDefaultPaddingValue = 6;
	private int m_nPaddingValue = 6;
	private final int m_nAnchorWidth = 1280; 
	private float m_font_size;
	private float m_prev_font_size;
	private float m_fontSizeRate = 100.0f;
	private float m_AnchorFontRate = 1;
	
	private CaptionColor setFgColor = null;
	private CaptionColor setBgColor = null;
	private CaptionColor setWindowColor = null;
	private CaptionColor setShadowColor = null;
	private CaptionColor setRaisedColor = null;
	private CaptionColor setDepressedColor = null;
	
	private int setFgOpacity = 0;
	private int setBgOpacity = 0;
	private int setWindowOpacity = 0;
	private int setShadowOpacity = 0;
	private int setRaisedOpacity = 0;
	private int setDepressedOpacity = 0;
	
	private CaptionColor setStrokeColor = null;
	private float setStrokeWidth = 0.0f;
	
	private boolean setBold = false;
	private boolean setShadow = false;
	
	private boolean setRaise = false;
	private boolean setDepressed = false;
	private float userFontSize = 0;
	private float userDefualtFontSize = 0;
	
	private Typeface m_typeItalic = null;
	private Typeface m_typeBoldItalic = null;
	private Typeface m_typeBold = null;
	private Typeface m_typeNormal = null;
	
	private static final float m_nUniformWidth = 1;
	
	private class RectForRearrangement
	{
		Rect rcRect = null;
		int nWindowIndex = 0;
	}
	
	NexLogStringQueue.CharUnit mTmpText[];
	SpannableStringBuilder mDrawtextBuilder = null;
	
	ComparatorRect mComparator = null;
	ArrayList<RectForRearrangement> mlistRect = null;
	
	Rect m_rcintersect = null;
	Rect m_rcintersectCompare = null;
	
	/**
	 ** Window 
	 */
	private static int MAX_WINDOW_COUNT = 8;
	private NexCaptionTextView[] m_window_view = null;
	SpannableStringBuilder m_StringForSize = null;
	private NexCaptionTextView m_view_to_calculate_size = null;
	
	/**
	 ** ETC
	 */
	private int m_service_no = 0;
	private boolean m_isvalidate = false;
	
/**
 * \brief This is the constructor for the CEA 708 closed captions caption view.
 * 
 * \param context	The handle for the player.
 * 
 * \since version 6.1.2
 */
 
		
	public NexEIA708CaptionView(Context context)
	{
		super(context);
		setWillNotDraw(false);
		init();
	}
/** \brief This is an alternative constructor for the CEA 708 closed captions caption view.
 * \warning If the caption view is to be used in Android xml, this constructor must be used.
 *
 * \param context  The handle for the player.
 * \param attrs   The set of attributes associated with the view.
 *
 * \since version 6.6
 */
	public NexEIA708CaptionView(Context context,AttributeSet attrs)
	{
		super(context,attrs);
		setWillNotDraw(false);
		init();
	}
	
	/** \brief This sets the CEA 708 closed captions foreground (text) color.
	 * 
	 * For a full list of colors, please refer to \ref NexClosedCaption::CaptionColor.
	 * 
	 * \param foreground  The foreground color, or \c null to use the color from the original caption data.
	 * \param fgOpacity  The foreground opacity, from 0 (transparent) to 255 (fully opaque). 
	 */
	public void setFGCaptionColor(CaptionColor foreground, int fgOpacity)
	{
		setFgColor = foreground;
		setFgOpacity = fgOpacity;
	}
	
	/** \brief This sets the background color of CEA 708 closed captions. 
	 * 
	 * For a full list of colors , please refer to \ref NexClosedCaption::CaptionColor.
	 * 
	 * \param background  The background color, or \c null to use the color from the original caption data.
	 * \param bgOpacity  The background opacity, from 0 (transparent) to 255 (fully opaque). 
	 */
	public void setBGCaptionColor(CaptionColor background, int bgOpacity)
	{
		setBgColor = background;
		setBgOpacity = bgOpacity;
	}
    /** \brief This method sets the color and opacity of the CEA 708 closed caption window.
     * 
     * \param windowColor  The color of the caption window to set, as a \c CaptionColor object.
     * \param windowOpacity  The opacity of the window to set, as an integer. 
     * 
     * \since version 6.5.2
     */
	public void setCaptionWindowColor(CaptionColor windowColor, int windowOpacity)
	{
		setWindowColor = windowColor;
		setWindowOpacity = windowOpacity;
	}
	/** \brief This method sets the CEA 708 closed caption renderer stroke color and width. 
	 * 
	 * For a full list of colors, please refer to \ref NexClosedCaption::CaptionColor. The stroke line width is in 
	 * pixels. Anti-aliasing is supported, so fractions of a pixel are allowed.  
	 * 
	 * \param strokeColor  The stroke color, or \c null to use the color from the original caption data.
	 * \param strokeWidth  The stroke width in pixels. 
	 */
	public void setCaptionStroke(CaptionColor strokeColor, float strokeWidth)
	{
		if (null != strokeColor)
		{
			resetEdgeEffect();
		}
		
		setStrokeColor = strokeColor;
		setStrokeWidth = strokeWidth;
	}
	/** \brief This method sets whether or not CEA 708 closed captions should be displayed in <b>bold</b> text.
	 * 
	 * Caption data includes attributes such as <b>bold</b> and <i>italics</i>.  
	 * 
	 * Normally, the caption renderer displays each character
	 * in normal, <b>bold</b>, or <i>italics</i> based on the attributes included in the caption data.  
	 * 
	 * However in some cases (such as for
	 * users with visual impairment) it may be desirable to force the use of bold text.  
	 * 
	 * By enabling this option, the
	 * bold attributes in the caption data are ignored and a bold font is used for all characters.
	 * 
	 * \param isBold   Set this to \c TRUE to force <b>bold</b> text, or \c FALSE to use the bold attribute in the original captions.
	 * 
	 */
	public void setBold(boolean isBold)
	{
		setBold = isBold;
	}
	/** \brief This method sets whether the CEA 708 closed captions should be displayed with a shadow.
	 * 
	 * \param isShadow  Set this to \c TRUE to force text to be displayed with a shadow, or \c FALSE for no shadow. 
	 * 
	 */
	public void setShadow(boolean isShadow)
	{
		if (isShadow)
		{
			resetEdgeEffect();
		}
		
		setShadow = isShadow;
	}
	
	/** 
	* \brief  This method sets whether CEA 708 closed captions should be displayed with a colored shadow.
	* 
	* \warning  Note that to set a colored shadow for CEA 608 closed captions, the \c NexClosedCaption::setShadowWithColor method
	*           should be used instead.
	*            
	* \param isShadow  Set this to \c TRUE to force text to be displayed with a shadow, or \c FALSE for no shadow. 
	* \param shadowColor  The shadow color, or \c null to use the color from the original caption data.
	* \param shadowOpacity  The shadow opacity as an integer, from 0 (transparent) to 255 (fully opaque). 
	*/
	public void setShadowWithColor(boolean isShadow, CaptionColor shadowColor, int shadowOpacity)
	{
		if (isShadow)
		{
			resetEdgeEffect();
		}

		setShadow = isShadow;
		setShadowColor = shadowColor;
		setShadowOpacity = shadowOpacity;
	}
	
    /** \brief  This method sets the fonts to be used for CEA 708 closed captions.
     * 
     * Four typefaces may be specified for different combinations of <b>bold</b> and 
     * <i>italics</i>. The caption renderer will select the appropriate typeface from 
     * among these based on the CEA 708 captions being displayed.
     * 
     * For best results, specify all four typefaces.  Any typeface can be set 
     * to \c null, in which case the system default typeface will be used.
     * 
     * \param normType          Typeface to be used for captions that are neither bold nor italic.
     * \param boldType          Typeface to be used for <b>bold</b> CEA 708 captions. 
     * \param italicType        Typeface to be used for <i>italic</i> CEA 708 captions.
     * \param boldItalicType    Typeface to be used for CEA 708 captions that are both <b><i>bold and in italics</i></b>.
     */
	public void setFonts(Typeface normType, Typeface boldType, Typeface italicType, Typeface boldItalicType)
	{
		m_typeBold = boldType;
		m_typeNormal = normType!=null?normType:Typeface.MONOSPACE;
		m_typeItalic = italicType;
		m_typeBoldItalic = boldItalicType;
	}
	/** \brief  This method sets whether or not CEA 708 closed captions should be displayed as if embossed or "raised".
	 * 
	 * To have raised closed captions be displayed in a user-defined color, see the method setRaiseWithColor instead. 
	 * 
	 * \param isRaise  \c TRUE if captions should be embossed or "raised", otherwise \c FALSE.
	 * 
	 * \see NexEIA708CaptionView.setRaiseWithColor
     */
	public void setRaise(boolean isRaise)
	{
		if (isRaise)
		{
			resetEdgeEffect();
		}

		setRaise = isRaise;
	}
	

	/**
	 * \brief This method indicates whether or not CEA 708 closed captions should be displayed as if "raised" (in a set font color).
	 * 
	 * If CEA 708 closed captions are "raised", they should be displayed as if they rise from the video display slightly,
	 * and the color of the raised part of the caption text can be set by the user. 
	 * 
	 * \param isRaise        \c TRUE if captions are raised, \c FALSE if not.
	 * \param raisedColor    The color of the raised part set by the user, or \c null to use the default color.
	 * \param raisedOpacity  The opacity of the raised part as an integer, from 0 (transparent) to 255 (fully opaque).  
	 *
	 * \since version 6.20
	 */	
	public void setRaiseWithColor(boolean isRaise, CaptionColor raisedColor, int raisedOpacity)
	{
		if (isRaise)
		{
			resetEdgeEffect();
		}
		
		setRaise = isRaise;
		setRaisedColor = raisedColor;
		setRaisedOpacity = raisedOpacity;
	}
	
	/** \brief This method sets whether or not CEA 708 closed captions should be displayed as if depressed into the content.
	 * 
	 * To have depressed closed captions be displayed in a user-defined color, see the method \c setDepressedWithColor instead.
	 * 
	 * \param isDepressed  \c TRUE if captions should be depressed, otherwise \c FALSE.
	 * 
	 * \see NexEIA708CaptionView.setDepressedWithColor
	*/
	public void setDepressed(boolean isDepressed)
	{
		if (isDepressed)
		{
			resetEdgeEffect();
		}
	
		setDepressed = isDepressed;
	}
	

	/**
	 * \brief This method indicates wheather or not CEA 708 closed captions should be displayed as if "depressed" (in a set font color).
	 * 
	 * If CEA 708 closed captions are "depressed", they should be displayed as if they are pressed into the video display slightly,
	 * and the color of the depressed part of the caption text can be set by the user.  
	 * 
	 * \param isDepressed  \c TRUE if captions are depressed, \c FALSE if not.
	 * \param depColor     The color of the depressed part set by the user, or \c null to use the default color.
	 * \param depOpacity   The opacity of the depressed part as an integer, from 0 (transparent) to 255 (fully opaque).  
	 *
	 * \since version 6.20
	 */	
	public void setDepressedWithColor(boolean isDepressed, CaptionColor depColor, int depOpacity)
	{
		if (isDepressed)
		{
			resetEdgeEffect();
		}
		
		setDepressed = isDepressed;
		setDepressedColor = depColor;
		setDepressedOpacity = depOpacity;
	}
	
	/**  \brief This method sets the text size of CEA 708 closed captions.
         *
         *  \param px  The font size to set closed caption text, in pixels.
         *
         * \since version 6.6
         */
	public void setTextSize(int px)
	{
		userDefualtFontSize = userFontSize = px;
	}
	

	/**  
	 * \brief This method sets the specular level of the Emboss Mask filter used when a user sets CEA 708 captions to be 'Raised' or 'Depressed' in the UI.
	 * 
	 * \param specular  The specular level of the Emboss Mask filter.
         * 
         * \since version 6.6
	 */ 
	public void setEmbossSpecular(float specular)
	{
	}
	
	/** 
	 * \brief This method sets the blur radius of the Emboss Mask filter used when a user sets CEA 708 captions to be 'Raised' or 'Depressed' in the UI.
	 * 
	 * \param radius  The blur radius of the Emboss Mask filter.
	 *
         * \since version 6.6
	 */ 
	public void setEmbossBlurRadius(float radius)
	{
	}
	
        /** \brief  This method clears the current caption string in CEA 708 closed captions.
         *
         * \since version 6.6
         */
	public void clearCaptionString()
	{
		try
		{
			for(int i=0, si=m_service_no; i < NexEIA708Struct.EIA708_WINDOW_MAX; i++) 
			{
				if (null != m_eia708cc && null != m_eia708cc.mService[si])
				{
					synchronized(m_eia708cc.mService[si].mWindow[i])
					{
						if (null != m_eia708cc.mService[si].mWindow[i])
							m_eia708cc.mService[si].mWindow[i].ClearWindow();
					}
				}
				else
				{
					break;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
     /**
      * \brief This method changes the font size of CEA 708 closed captions.
      * 
      * To double the size of the font, the parameter \c rate should be set to 200.
      * In contrast, to have the size of the font, \c rate should be set to 50 (in other words, 50%).
      *
      * \param rate   The change in size of the caption font, as a percentage.
      *
      * \since version 6.5
      */
	public void changeFontSize(int rate)
	{
		if(rate >= 50 && rate <= 200)
			m_fontSizeRate = (float)rate;
		else
			m_fontSizeRate = 100;
	
		if (0 < userDefualtFontSize)
		{
			userFontSize  = ((m_fontSizeRate/100.0f) * userDefualtFontSize);
		}
		else
		{
			setFontSize(NexEIA708Struct.STANDARD);
		}
	}
	
	private void setFontSize(byte TextType)
	{
		float fFontSize = 0;
		switch (TextType)
		{
		case NexEIA708Struct.SMALL: fFontSize = 20;
			break;
		case NexEIA708Struct.STANDARD: fFontSize = 21;
			break;
		case NexEIA708Struct.LARGE: fFontSize = 22;
			break;
		case NexEIA708Struct.EXTRA_LARGE: fFontSize = 24;
			break;
		default:
			fFontSize = 21;
			break;
		}	
		
		if (0 < m_AnchorFontRate && 0 < m_fontSizeRate)
		{
			fFontSize *= m_AnchorFontRate;
			fFontSize = ((m_fontSizeRate/100.0f) * fFontSize);
	 		
	 		if (m_prev_font_size < fFontSize)
	 		{
	 			m_font_size = calculateFontSize(fFontSize);
	 		}
	 		else
	 		{
	 			if (m_font_size >= fFontSize)
	 				m_font_size = fFontSize;
	 		}
	 		
	 		//NexLog.d(TAG, "setFontSize m_prev_font_size: " + m_prev_font_size + " fFontSize= " + fFontSize + " m_font_size= " + m_font_size);
	 		
	 		m_prev_font_size = fFontSize;
		}
	}
	
	/**
	 * \brief This method sets the area where CEA 708 closed captions will be displayed.
	 * 
	 * \param left    The horizontal (X) position of the top left corner of the area where captions will be displayed.
	 * \param top     The vertical (Y) position of the top left corner of the area where captions will be displayed.
	 * \param width   The width of the area where captions will be displayed.
	 * \param height  the height of the area where captions will be displayed.
	 *	 
	 * \since version 6.1.2
	 */
	public void setDisplayArea(int left, int top, int width, int height) 
	{
		NexLog.d(TAG, "setDisplayArea left: " + left + " top: " + top + " width: " + width + " height: " + height);
		
		m_margin_left = left;
		m_margin_top = top;
		
		float realWidth = width;
		float realHeight = height;
		
		if (0 < left)
			realWidth += (left * 2);
		
		if (0 < top)
			realHeight += (top * 2);
		
		float fSafeCaptionAreaWidth = width * (float)0.9;
		float fSafeCaptionAreaHeight = height * (float)0.9;

		m_display_width = fSafeCaptionAreaWidth;
		m_display_height = fSafeCaptionAreaHeight;
		
		m_margin_left = (int) ((realWidth - fSafeCaptionAreaWidth) / 2);
		m_margin_top = (int) ((realHeight - fSafeCaptionAreaHeight) / 2);		
		
		m_AnchorFontRate = (float)width / (float)m_nAnchorWidth;

		m_nPaddingValue = convertPixel2DIP(m_nDefaultPaddingValue * m_AnchorFontRate);
		
		setFontSize(NexEIA708Struct.STANDARD);
		
 		if(fSafeCaptionAreaWidth > fSafeCaptionAreaHeight) 
 		{
			setScreenRatio(fSafeCaptionAreaWidth, fSafeCaptionAreaHeight, 16, 9);
			
			m_x_interval = m_display_width / 210;
			m_y_interval = m_display_height / 75;
		} 
 		else 
 		{
			setScreenRatio(fSafeCaptionAreaWidth, fSafeCaptionAreaHeight, 4, 3);
			
			m_x_interval = m_display_width / 160;
			m_y_interval = m_display_height / 75;
		}	
 		
 		m_x_rp_interval = m_display_width/100;
		m_y_rp_interval = m_display_height/100;
		
		NexLog.d(TAG, "setDisplayArea m_display_width: " + m_display_width + " m_display_height: " + m_display_height + " m_AnchorFontRate: " + m_AnchorFontRate + " m_nPaddingValue: " + m_nPaddingValue);
		
		setValidateUpdate(true);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		// DO NOTHING.
		//
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		if(isValidateUpdate()) 
		{
			if(m_eia708cc != null)
			{
				mlistRect.clear();
				
				for(int i=0, si=m_service_no; i<NexEIA708Struct.EIA708_WINDOW_MAX; i++) 
				{
					synchronized(m_eia708cc.mService[si].mWindow[i]) 
					{
						if(m_eia708cc.mService[si].mWindow[i].mVisible != 0) 
						{
							m_window_view[i].setVisibility(NexCaptionTextView.VISIBLE);
							setjustify2windowview(m_window_view[i], m_eia708cc.mService[si].mWindow[i].mJustify);
							settext2windowview(m_window_view[i], i);
							setfontproperty2windowview(m_window_view[i], m_font_size);
							
							
							setlayout2windowview(i, m_eia708cc.mService[si].mWindow[i].mAanchorPoint,
									m_eia708cc.mService[si].mWindow[i].mRelativePosition,
									m_eia708cc.mService[si].mWindow[i].mAnchorVertical,
									m_eia708cc.mService[si].mWindow[i].mAnchorHorizontal,
									m_eia708cc.mService[si].mWindow[i].GetHeight(),
									m_eia708cc.mService[si].mWindow[i].GetWidth());
							
							int argbcolor = m_eia708cc.mService[si].mWindow[i].GetARGBColorWindows();
							setbackgrouncolor2windowview(m_window_view[i], argbcolor);
						}
						else
						{
							m_window_view[i].setVisibility(NexCaptionTextView.GONE);
						}
							
					}
				}
				
				reArrangeWindowView();			
			}
			
			setValidateUpdate(false);
		}		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) 
	{
		super.onSizeChanged(w, h, oldw, oldh);	
	}
	
	private boolean isValidateUpdate() 
	{
		return m_isvalidate;
	}
	
	/**
	 * \brief  This method sets whether a CEA 708 closed caption should be updated or not.
	 * 
	 * In order to update CEA 708 closed captions, it is necessary to call this method and then call
	 * \c invalidate.
	 * 
	 * \param isvalid  Whether or not to update captions:  \c TRUE if valid, otherwise \c FALSE.  
	 * 
	 * \since version 6.1.2
	 */
	public void setValidateUpdate(boolean isvalid)
	{
		m_isvalidate = isvalid;
	}
	
	
       /**
        * \brief This method resets the edge effects on CEA 708 closed captions. 
        *
        * Possible edge effects include setShadow, setCaptionStroke, setRaise, and setDepressed.
        * 
        * \since version 6.7
        */
    public void resetEdgeEffect()
	{
		setShadow = false;
		setShadowColor = null;
		setShadowOpacity = 0;
		
		setStrokeColor = null;
		setStrokeWidth = 0;
		
		setRaise = false;
		setDepressed = false;
	}

	private void init()
	{
		if(m_window_view == null)
			m_window_view = new NexCaptionCEA708TextView[MAX_WINDOW_COUNT];
		
		for(int id=0; id<MAX_WINDOW_COUNT; id++) 
		{
			if(m_window_view[id] == null)
				m_window_view[id] = new NexCaptionCEA708TextView(getContext());
			
			m_window_view[id].layout(0,0,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			m_window_view[id].setTypeface(Typeface.DEFAULT);
			addView(m_window_view[id]);
		}
		
		mTmpText = new NexLogStringQueue.CharUnit[NexLogStringQueue.LOGSTRQ_MAX_COUNT_IN_A_ROW];
		mDrawtextBuilder = new SpannableStringBuilder();
		
		mlistRect = new ArrayList<RectForRearrangement>();		
		m_rcintersect = new Rect();
		m_rcintersectCompare = new Rect();
		mComparator = new ComparatorRect();
		m_view_to_calculate_size = new NexCaptionCEA708TextView(getContext());
		m_view_to_calculate_size.layout(0,0,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		m_view_to_calculate_size.setTypeface(Typeface.DEFAULT);
		m_StringForSize = new SpannableStringBuilder();
		
		for (int i = 0; i < 32; ++i)
			m_StringForSize.append('A');
		
		m_view_to_calculate_size.setText(m_StringForSize);
		
		WrapSetLayerType();
	}
	
	@SuppressLint("NewApi")
	private void WrapSetLayerType()
	{
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
		{
			setLayerType(ViewGroup.LAYER_TYPE_SOFTWARE, null);
		}
	}

	/** 
	 * \brief This sets the CEA 708 closed captions for a given content.
	 * 
	 * Whenever new CEA 708 content data becomes available, this method is called to specify the data to the caption view. 
	 * 
	 * \param eia708  The CEA 708 closed captions to set, as a NexEIA708Struct structure.
	 * 
	 * \see NexEIA708Struct
	 * 
	 * \since version 6.1.2
	 */
	public void setEIA708CC(NexEIA708Struct eia708) 
	{
		m_eia708cc = eia708;
	}
	
	/**
	 * \brief This method sets the screen ratio for CEA 708 closed captions.
	 * 
	 * \since version 6.1.2
	 */
	private void setScreenRatio(float width, float height, float width_ratio, float height_ratio) 
	{		
		float screen = (float)(height / width);
		float ratio = height_ratio / width_ratio;
		
		if(screen > ratio) {
			float set_display_height = ((float)width / width_ratio * height_ratio);
			m_display_height = set_display_height;
		}
		else {
			float set_display_width = ((float)height / height_ratio * width_ratio);
			m_display_width = set_display_width;
		}
	}
	
	private static final float DEFAULT_HDIP_DENSITY_SCALE = 1.5f;

	private int convertPixel2DIP(float pixel)
	{
		float scale = getContext().getResources().getDisplayMetrics().density;    
		int nDIP = (int)(pixel / DEFAULT_HDIP_DENSITY_SCALE * scale);
		
		return nDIP;
	}
		
	private void setlayout2windowview(int id, int anchor,int relative_position, int anchor_vertical, int anchor_horizontal,
										int row_cnt, int column_cnt)
	{
		int left = 0;
		int top	= 0;
		int right = 0;
		int bottom = 0;
		
		if (m_window_view[id] == null)
			return;
		
		m_window_view[id].measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
				 MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		
		int nMeasuredWidth = m_window_view[id].getMeasuredWidth();
		int nMeasuredHeight = m_window_view[id].getMeasuredHeight();
		
		//NexLog.d(TAG, " first width height  width : " + nMeasuredWidth + "height: " +nMeasuredHeight);
		
		if (m_display_width < nMeasuredWidth || m_display_height < nMeasuredHeight)
		{
			String sText = m_window_view[id].getText().toString();
			if (0 < sText.length())
			{
				if (m_display_width + (m_margin_left * 2) < nMeasuredWidth)
					nMeasuredWidth = (int) m_display_width;

				if (m_display_height + (m_margin_top * 2) < nMeasuredHeight)
					nMeasuredHeight = (int) m_display_height;
				
			}
		}
		else if (0 >= nMeasuredHeight || 0 >= nMeasuredWidth || 0 >= m_window_view[id].getText().length())
		{
			NexLog.d(TAG, "invalid values nMeasuredWidth: " + nMeasuredWidth + " nMeasuredHeight: " + nMeasuredHeight + " text len: " + m_window_view[id].getText().length());
			m_window_view[id].layout(0, 0, 0, 0);
			return;
		}
		
		if (relative_position == 0)
		{
			left	= (int)(m_x_interval * anchor_horizontal + m_margin_left);
			top		= (int)(m_y_interval * anchor_vertical + m_margin_top);
			right 	= (int)(nMeasuredWidth + left + m_nPaddingValue/2);
			bottom 	= (int)(nMeasuredHeight + top);
		}
		else
		{
			left	= (int)(m_x_rp_interval * anchor_horizontal + m_margin_left);
			top		= (int)(m_y_rp_interval * anchor_vertical + m_margin_top);
			right 	= (int)(nMeasuredWidth + left + + m_nPaddingValue/2);
			bottom 	= (int)(nMeasuredHeight + top);
		}
					
		int nHeight = 0;
		int nWidth = 0;
		switch(anchor) 
		{
		case 0:
			break;
		case 1:
			nWidth = right - left;
			right -= (nWidth/2);
			left -= (nWidth/2);
			break;
		case 2:
			nWidth = right - left;
			right = left;
			left -= nWidth;
			break;
		case 3:
			nHeight = bottom - top;
			bottom -= (nHeight/2);
			top -= (nHeight/2);
			break;
		case 4:
			nHeight = bottom - top;
			bottom -= (nHeight/2);
			top -= (nHeight/2);
			
			nWidth = right - left;
			right -= (nWidth/2);
			left -= (nWidth/2);
			break;
		case 5:
			nHeight = bottom - top;
			bottom -= (nHeight/2);
			top -= (nHeight/2);
			
			nWidth = right - left;
			right = left;
			left -= nWidth;
			break;
		case 6:
			nHeight = bottom - top;
			bottom = top;
			top -= nHeight;
			break;
		case 7:
			nHeight = bottom - top;
			bottom = top;
			top -= nHeight;
			
			nWidth = right - left;
			right -= (nWidth/2);
			left -= (nWidth/2);
			break;
		case 8:			
			nHeight = bottom - top;
			bottom = top;
			top -= nHeight;
			
			nWidth = right - left;
			right = left;
			left -= nWidth;
			break;
		}

		//NexLog.d(TAG, "anchor point setlayout2windowview layout left " + left + " top= " + top + "right= " + right + " bottom= "+ bottom);

		if (m_display_width + (m_margin_left * 2) + m_nPaddingValue * 2 > right - left  && (m_display_height + m_margin_top * 2) > bottom - top)
		{
			m_window_view[id].layout(left, top, right, bottom);
			gatherRectForRearrangement(id, left, top, right, bottom);
		}
	}
	
	private void gatherRectForRearrangement(int id, int left, int top, int right, int bottom)
	{
		if (null != m_window_view && null != m_window_view[id])
		{
			RectForRearrangement rcCheckRect = new RectForRearrangement();
			rcCheckRect.rcRect = new Rect();
			rcCheckRect.nWindowIndex = id;
			rcCheckRect.rcRect.left = left;
			rcCheckRect.rcRect.top = top;
			rcCheckRect.rcRect.right = right;
			rcCheckRect.rcRect.bottom = bottom;
						
			if (false == rcCheckRect.rcRect.isEmpty())
			{
				NexLog.d(TAG, "gatherRectForRearrangement rcCheckRect.left " + rcCheckRect.rcRect.left + " rcCheckRect.rcRect.right= " + rcCheckRect.rcRect.right + " rcCheckRect.rcRect.top= " + rcCheckRect.rcRect.top + " rcCheckRect.rcRect.bottom= "+ rcCheckRect.rcRect.bottom);
				mlistRect.add(rcCheckRect);
			}	
		}
	}
		
	private boolean checkAndMoveRectFromIntersect(Rect rcBase, Rect rcCompare, DirectionForArrangement eDirection)
	{
		boolean bResult = false;
		
		if (null != rcBase && null != rcCompare && null != m_rcintersect && null != m_rcintersectCompare)
		{			
			m_rcintersect.set(rcBase);
			m_rcintersectCompare.set(rcCompare);
			
			if (true == m_rcintersect.intersect(m_rcintersectCompare))
			{
				int nInterval = 0;
				if (eDirection == DirectionForArrangement.FROM_BOTTOM)
				{
					if (rcCompare.bottom > rcBase.top)
					{
						nInterval = rcCompare.bottom - rcBase.top;
					}
					else
					{
						nInterval = rcCompare.height() + rcBase.top - rcCompare.top;
					}
					
					nInterval *= -1;
				}
				else if (eDirection == DirectionForArrangement.FROM_TOP)
				{
					if (rcCompare.top > rcBase.bottom)
					{
						nInterval = rcBase.bottom - rcCompare.top;
					}
					else
					{
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
	
	private boolean reArrangeWindowByBoundary(ArrayList<RectForRearrangement> list)
	{
		boolean bOverBoundary = false;
		
		if (null != list)
		{
			for (int i = 0; i < list.size(); ++i)
			{
				RectForRearrangement rcCheckingBoundary = list.get(i);
				if (null != rcCheckingBoundary && CheckAndMoveRectFromBoundary(rcCheckingBoundary.rcRect))
				{
					list.set(i, rcCheckingBoundary);
					Rect rcCheckedRect = rcCheckingBoundary.rcRect;
					if (null != m_window_view[list.get(i).nWindowIndex])
					{
						NexLog.d(TAG, "reArrangeWindowByBoundary rcCheckedRect.left " + rcCheckedRect.left + " rcCheckedRect.right= " + rcCheckedRect.right + " rcCheckedRect.top= " + rcCheckedRect.top + " rcCheckedRect.bottom= "+ rcCheckedRect.bottom);
						m_window_view[list.get(i).nWindowIndex].layout(rcCheckedRect.left, rcCheckedRect.top, rcCheckedRect.right, rcCheckedRect.bottom);
					}
					
					bOverBoundary = true;
				}
			}
		}
		
		return bOverBoundary;
	}
	
	private void reArrangeWindowByIntersection(ArrayList<RectForRearrangement> list, DirectionForArrangement eDefaultDirection)
	{
		if (null != list)
		{
			for (int i = 0; i < list.size(); ++i)
			{
				if (i < list.size() - 1)
				{
					RectForRearrangement rcFirst = list.get(i);
					RectForRearrangement rcSecond = list.get(i+1);
					
					if (checkAndMoveRectFromIntersect(rcFirst.rcRect, rcSecond.rcRect, eDefaultDirection))
					{
						list.set(i+1, rcSecond);
						
						if (null != m_window_view[rcSecond.nWindowIndex])
						{
							NexLog.d(TAG, "reArrangeWindowByIntersection rcSecond.rcRect.left " + rcSecond.rcRect.left + " rcSecond.rcRect.right= " + rcSecond.rcRect.right + " rcSecond.rcRect.top= " + rcSecond.rcRect.top + " rcSecond.rcRect.bottom= "+ rcSecond.rcRect.bottom);
							m_window_view[rcSecond.nWindowIndex].layout(rcSecond.rcRect.left, rcSecond.rcRect.top, rcSecond.rcRect.right, rcSecond.rcRect.bottom);
						}
					}
				}
			}
		}
	}
	
	private boolean CheckAndMoveRectFromBoundary(Rect rcCompare)
	{
		boolean bResult = false;
		
		if (null != rcCompare)
		{
			int nInterval = 0;
			if (rcCompare.bottom > m_display_height + m_margin_top)
			{
				nInterval = (int) (m_display_height + m_margin_top - rcCompare.bottom);
				rcCompare.top += nInterval;
				rcCompare.bottom += nInterval;
				bResult = true;
			}
			
			if (rcCompare.top < m_margin_top)
			{
				nInterval = (int) (m_margin_top - rcCompare.top);
				rcCompare.top += nInterval;
				rcCompare.bottom += nInterval;
				bResult = true;
			}
			
			if (rcCompare.left < m_margin_left)
			{
				nInterval = (int) (m_margin_left - rcCompare.left);
				rcCompare.left += nInterval;
				rcCompare.right += nInterval;
				bResult = true;
			}
			
			if (rcCompare.right > m_margin_left + m_display_width)
			{
				nInterval = (int) (m_display_width + m_margin_left - rcCompare.right);
				rcCompare.left += nInterval;
				rcCompare.right += nInterval;
				bResult = true;
			}
		}
			
		return bResult;
	}
	
	private void reArrangeWindowView()
	{
		if (null != mComparator && null != mlistRect)
		{	
			// descending sort
			mComparator.setDirection(DirectionForArrangement.FROM_BOTTOM);
			Collections.sort(mlistRect, mComparator);

			// check and move windows from boundary
			reArrangeWindowByBoundary(mlistRect);

			// check and move overlapped windows in descending list
			reArrangeWindowByIntersection(mlistRect, DirectionForArrangement.FROM_BOTTOM);
			
			// ascending sort
			mComparator.setDirection(DirectionForArrangement.FROM_TOP);
			Collections.sort(mlistRect, mComparator);
			
			// check and move windows from boundary
			if (reArrangeWindowByBoundary(mlistRect))
			{				
				// check and move overlapped windows in ascending list
				reArrangeWindowByIntersection(mlistRect, DirectionForArrangement.FROM_TOP);				
			}
		}
	}
	
	private void setbackgrouncolor2windowview(NexCaptionTextView window_view, int color) 
	{
		if (null != window_view)
		{
			if(setWindowColor == null)
				window_view.setBackgroundColor(color);
			else
			{
				int colorInfo = setWindowColor.getFGColor();
				int winColor = Color.argb(setWindowOpacity, Color.red(colorInfo), Color.green(colorInfo), Color.blue(colorInfo));
				window_view.setBackgroundColor(winColor);
			}	
		}
	}
	
	private void setfontproperty2windowview(NexCaptionTextView window_view, float fFontSize)
	{
		if (null == window_view)
			return;
		
		float nDIPSize = userFontSize;
		if(0 >= userFontSize)
		{
			float scale = DEFAULT_HDIP_DENSITY_SCALE / getContext().getResources().getDisplayMetrics().density;
			//NexLog.d(TAG, "setfontproperty2windowview text scale: " + getContext().getResources().getDisplayMetrics().density + " adjusted scale: " + scale);
			nDIPSize = fFontSize * scale;
		}
		else
			nDIPSize = convertPixel2DIP(userFontSize);
		
		//NexLog.d(TAG, "setfontproperty2windowview text size: " + nDIPSize + " userFontSize: " + userFontSize);
		window_view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, nDIPSize);
	}	
	
	private void setFontAttributes(NexCaptionTextView window_view)
	{
		if (null != window_view)
		{
			if(m_typeBoldItalic != null)
			{
				window_view.setTypeface(m_typeBoldItalic, Typeface.BOLD_ITALIC);
			}
			else if(m_typeBold != null || setBold)
			{
				window_view.setTypeface(m_typeBold, Typeface.BOLD);
			}
			else if(m_typeItalic != null)
			{
				window_view.setTypeface(m_typeItalic, Typeface.ITALIC);
			}
			else if (m_typeNormal != null)
			{
				window_view.setTypeface(m_typeNormal);
			}
		}
	}
	
	private boolean setEdgeType(NexCaptionTextView window_view, SpannableStringBuilder text, int nStart, int nEnd, CharUnit charAttr)
	{
		boolean bResult = false;
		
		if (null == window_view)
			return bResult;
		
		if (null == setStrokeColor)
			window_view.setCaptionStroke(null, 0, 0);
		
		if (false == setShadow)
			window_view.setDropShadow(false);
		
		if(setShadow) //From Client
		{
			if (null != setShadowColor)
			{
				int setColor = setShadowColor.getFGColor();
				int shadowColor = Color.argb(setShadowOpacity, Color.red(setColor), Color.green(setColor), Color.blue(setColor));
				window_view.setDropShadow(true, shadowColor);
			}
			else
			{
				window_view.setDropShadow(true);
			}
		}
		else if (null != setStrokeColor)
		{
			int color = charAttr.GetARGBTextColor();
			
			if (null != setFgColor)
			{
				int set_color = setFgColor.getFGColor();
				color = Color.argb(setFgOpacity, Color.red(set_color), Color.green(set_color), Color.blue(set_color));	
			}
			
			window_view.setBaseTextColor(color);
			window_view.setCaptionStroke(setStrokeColor, 255, setStrokeWidth);
		}
		else if (setRaise)
		{
			if(setRaisedColor != null)
			{
				int setColor = setRaisedColor.getFGColor();
				int raisedColor = Color.argb(setRaisedOpacity, Color.red(setColor), Color.green(setColor), Color.blue(setColor));
				window_view.setRaised(true, raisedColor);
			}
			else
			{
				window_view.setRaised(true);
			}
		}
		else if (setDepressed)
		{
			if(setDepressedColor != null)
			{
				int setColor = setDepressedColor.getFGColor();
				int depColor = Color.argb(setDepressedOpacity, Color.red(setColor), Color.green(setColor), Color.blue(setColor));
				window_view.setDepressed(true, depColor);
			}
			else
			{
				window_view.setDepressed(true);
			}
		}
		else //From Provider
		{
			/*Edge section*/
			int nEdgeColorFromProvider = charAttr.GetARGBEdgeColor();
			if(false == setRaise && NexEIA708Struct.RAISED == charAttr.mEdgeType)
			{
				window_view.setRaised(true, nEdgeColorFromProvider);
			}
			else if(false == setDepressed && NexEIA708Struct.DEPRESSED == charAttr.mEdgeType)
			{
				window_view.setDepressed(true, nEdgeColorFromProvider);
			}
			else if(false == setShadow && NexEIA708Struct.SHADOW_LEFT == charAttr.mEdgeType) 
			{
				if(setShadowColor != null)
				{
					int setColor = setShadowColor.getFGColor();
					int shadowColor = Color.argb(setShadowOpacity, Color.red(setColor), Color.green(setColor), Color.blue(setColor));
					window_view.setDropShadow(true, shadowColor);
				}
				else
				{
					window_view.setDropShadow(true, NexClosedCaption.DEFAULT_SHADOW_PARAM[0], 
							-5f, NexClosedCaption.DEFAULT_SHADOW_PARAM[2], nEdgeColorFromProvider);
				}
					
			} 
			else if(false == setShadow && NexEIA708Struct.SHADOW_RIGHT == charAttr.mEdgeType)
			{
				if(setShadowColor != null)
				{
					int setColor = setShadowColor.getFGColor();
					int shadowColor = Color.argb(setShadowOpacity, Color.red(setColor), Color.green(setColor), Color.blue(setColor));
					window_view.setDropShadow(true, NexClosedCaption.DEFAULT_SHADOW_PARAM[0], 
							5f, NexClosedCaption.DEFAULT_SHADOW_PARAM[2], shadowColor);
				}
				else
				{
					window_view.setDropShadow(true, NexClosedCaption.DEFAULT_SHADOW_PARAM[0], 
							5f, NexClosedCaption.DEFAULT_SHADOW_PARAM[2], nEdgeColorFromProvider);
				}
			}
			else if(null == setStrokeColor && NexEIA708Struct.UNIFORM == charAttr.mEdgeType)
			{
				int color = charAttr.GetARGBTextColor();
				
				if (null != setFgColor)
				{
					int set_color = setFgColor.getFGColor();
					color = Color.argb(setFgOpacity, Color.red(set_color), Color.green(set_color), Color.blue(set_color));	
				}
				
				window_view.setBaseTextColor(color);
				window_view.setCaptionStroke(nEdgeColorFromProvider, m_nUniformWidth);
			}
		}
		
		return true;
	}
	
	private void setColor(NexCaptionCEA708TextView window_view, SpannableStringBuilder text, int nStart, int nEnd, CharUnit charAttr)
	{
		//set FG color
		if (null == setStrokeColor && NexEIA708Struct.UNIFORM != charAttr.mEdgeType) //when setStrokeColor is applied, setFGColor is also applied.   
		{
			if(null != setFgColor)
			{
				int set_color = setFgColor.getFGColor();
				int setColor = Color.argb(setFgOpacity, Color.red(set_color), Color.green(set_color), Color.blue(set_color));
				text.setSpan(new ForegroundColorSpan(setColor), 0, nEnd, 0);
			}
			else
			{
				text.setSpan(new ForegroundColorSpan(charAttr.GetARGBTextColor()), 0, nEnd, 0);
			}

			//init edge of color for uniform.
			window_view.setCaptionStroke(0, 0);
		}
		
		//set BG color
		if (null != setBgColor)
		{
			int setColor = setBgColor.getFGColor();
			int nBGColor = Color.argb(setBgOpacity, Color.red(setColor), Color.green(setColor), Color.blue(setColor));
			//window_view.setBGCaptionColor(nBGColor);
			window_view.setHLColor(nBGColor);
		}
		else
		{
			//window_view.setBGCaptionColor(charAttr.GetARGBBGColor());
			window_view.setHLColor(charAttr.GetARGBBGColor());
		}
	}
	
	private void settext2windowview(NexCaptionTextView window_view, int id) 
	{	
		if (null == window_view || null == mDrawtextBuilder || null == mTmpText || null == m_eia708cc)
		{
			NexLog.e(TAG, "Some address for settext2windowview is null.");
			return;
		}
		
		if ((0 > m_service_no && NexEIA708Struct.EIA708_SERVICE_MAX <= m_service_no) ||
			(0 > id && NexEIA708Struct.EIA708_WINDOW_MAX <= id))
		{
			NexLog.e(TAG, "The current service / window number is invalid. service num: " + m_service_no + " window num: " + id);
			return;
		}
		else
		{
			mDrawtextBuilder.clearSpans();
			mDrawtextBuilder.clear();
		}
		
		int x = 0, y = 0, nTextTotalCount = 0, nTextCountInRow = 0;
		int nHeight = m_eia708cc.mService[m_service_no].mWindow[id].GetHeight();
		
		for (y = 0; y < nHeight; y++)
		{
			if (y != 0)
			{
				mDrawtextBuilder.append("\r\n");
				nTextTotalCount += 2;
			}
			
			nTextCountInRow = m_eia708cc.mService[m_service_no].mWindow[id].GetTextLine(mTmpText, y);
			if (0 > nTextCountInRow && NexLogStringQueue.LOGSTRQ_MAX_COUNT_IN_A_ROW <= nTextCountInRow)
			{
				NexLog.e(TAG, "Text count is invalid: " + nTextCountInRow);
				break;
			}
				
			for (x = 0; x < nTextCountInRow; x++, nTextTotalCount++)
			{
				if(mTmpText[x].mCChar == 0x00)
				{
					if (NexEIA708Struct.LEFT == m_eia708cc.mService[m_service_no].mWindow[id].mJustify)
						mDrawtextBuilder.append(" ");
					continue;
				}
				else
				{
					mDrawtextBuilder.append(Character.toString((char)mTmpText[x].mCChar));
				}
				
				/*offset section*/
				if(NexEIA708Struct.SUBSCRIPT == mTmpText[x].mOffset)
				{ //Subscript
					SubscriptSpan SubscriptSpan = new SubscriptSpan();
					mDrawtextBuilder.setSpan(SubscriptSpan, nTextTotalCount, nTextTotalCount+1, 0);
				}
				else if(NexEIA708Struct.SUPERSCRIPT == mTmpText[x].mOffset)
				{ //Superscript
					SuperscriptSpan SuperscriptSpan = new SuperscriptSpan();
					mDrawtextBuilder.setSpan(SuperscriptSpan, nTextTotalCount, nTextTotalCount+1, 0);
				}
				
				/*Underline section*/
				if(NexEIA708Struct.UNDERLINE == mTmpText[x].mUnderline)
				{
					UnderlineSpan UnderlineSpan = new UnderlineSpan();
					mDrawtextBuilder.setSpan(UnderlineSpan, nTextTotalCount, nTextTotalCount+1, 0);
				}
				
				/*Italic section*/
				if(null == m_typeItalic && NexEIA708Struct.ITALICS == mTmpText[x].mItalics)
				{
					StyleSpan StyleSpanItalic = new StyleSpan(android.graphics.Typeface.ITALIC);
					mDrawtextBuilder.setSpan(StyleSpanItalic, nTextTotalCount, nTextTotalCount+1, 0);
				}
			}
		}
	
		int nCharCountForCR = (nHeight - 1) * 2;
		if (0 < nTextTotalCount - nCharCountForCR)
		{
			CharUnit charAttr = m_eia708cc.mService[m_service_no].mWindow[id].GetCharAttr();
			
			setFontSize(charAttr.mPenSize);
			setFontAttributes(window_view);
			setEdgeType(window_view, mDrawtextBuilder, 0, nTextTotalCount, charAttr);
			setColor((NexCaptionCEA708TextView )window_view, mDrawtextBuilder, 0, nTextTotalCount, charAttr);
			
			NexLog.d(TAG, "display text: " + mDrawtextBuilder.toString());
			NexLog.d(TAG, "display id: " + id + " mDrawtext Count: " + nTextTotalCount);
			
			window_view.setText(mDrawtextBuilder, BufferType.SPANNABLE);
			window_view.setPadding(m_nPaddingValue, m_nPaddingValue, m_nPaddingValue, m_nPaddingValue);
		}
		else
		{
			window_view.setPadding(0, 0, 0, 0);
			window_view.setText(null, null);
		}
	}
		
	private void setjustify2windowview(NexCaptionTextView window_view, int justify) 
	{
		if (null != window_view)
		{
			int gravity = convertJustifyToTextView(justify);
			window_view.setGravity(gravity);
		}
	}
	
	private int convertJustifyToTextView(int justify)
	{
		int gravity = Gravity.LEFT;
		switch (justify)
		{
		case 0:
			gravity = Gravity.LEFT;
			break;
		case 1:
			gravity = Gravity.RIGHT;
			break;
		case 2:
			gravity = Gravity.CENTER;
			break;
		case 3:
			gravity = Gravity.FILL;
			break;
		}
		
		return gravity;
	}

	/** 
	 * \brief  This method sets the source of the byte stream for CEA 708 closed captions.
	 * 
	 * Similar to the channels in CEA 608 closed captions, CEA 708 services often provide different
	 * closed caption information for the given content, for example captions in different languages.  
	 * 
	 * \param serviceNO  The CEA 708 service number to use as the source of the caption byte stream.
	 * \param Data  The CEA 708 closed caption data, as an array of bytes.
	 * \param len  The size of the array of CEA 708 closed caption data, in bytes.
	 * 
	 * \returns Returns 1 if there is update in the CEA 708 closed caption view, otherwise 0. 
	 *  
	 * \since version 6.1.2
	 */
	public boolean SetSourceByteStream(int serviceNo, byte[] Data, int len){
		if (serviceNo <= 0 || serviceNo > NexEIA708Struct.EIA708_SERVICE_MAX)
			m_service_no = 0;
		else
			m_service_no = serviceNo - 1;
		
		return m_eia708cc.SetSourceByteStream(serviceNo, Data, len);
	}
	
       /** \brief This method initializes the style attributes of CEA 708 closed captions that may be set by a user, including the colors of the text, background, and caption window as well as the edge style and the font size.
        * 
        * This API does not effect the default caption style attributes of specific streaming content. 
        *
        * \since version 6.13
        */
	public void initCaptionStyle()
	{
		setFgColor = null;
		setBgColor = null;
		setWindowColor = null;
		setFgOpacity = 0;
		setBgOpacity = 0;
		setWindowOpacity = 0;
		setBold = false;
		
		resetEdgeEffect();
		
		userFontSize = 0;
		userDefualtFontSize = 0;
		m_fontSizeRate = 100;
		setFontSize(NexEIA708Struct.STANDARD);
		
		m_typeBold = null;
		m_typeNormal = Typeface.MONOSPACE;
		m_typeItalic = null;
		m_typeBoldItalic = null;

		setValidateUpdate(true);
	}
	
	private enum DirectionForArrangement
	{
		NONE,
		FROM_LEFT,
		FROM_TOP,
		FROM_RIGHT,
		FROM_BOTTOM;
	}
	
	class ComparatorRect implements Comparator<RectForRearrangement>
	{
		DirectionForArrangement m_eDirection = DirectionForArrangement.FROM_TOP;
		
		public void setDirection(DirectionForArrangement eDirection)
		{
			m_eDirection = eDirection;
		}

		@Override
		public int compare(RectForRearrangement lhs,
				RectForRearrangement rhs) 
		{
			int nTarget = 0;
			int nCompare = 0;
			
			int nResult = 0;
			int nOffset = 1;
			
			switch (m_eDirection)
			{
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
			
			if (null != lhs.rcRect && null != rhs.rcRect)
			{
				if (nTarget > nCompare)
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
	
	private float calculateFontSize(float fFontSize)
	{
		float fSize = fFontSize;
		
		if (0 < fSize && null != m_view_to_calculate_size && null != m_StringForSize)
		{
			m_view_to_calculate_size.layout(0,0,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			m_view_to_calculate_size.setTypeface(Typeface.DEFAULT);
			
			m_view_to_calculate_size.setPadding(m_nPaddingValue, m_nPaddingValue, m_nPaddingValue, m_nPaddingValue);
			setFontAttributes(m_view_to_calculate_size);

			setfontproperty2windowview(m_view_to_calculate_size, fFontSize);

			try {
				m_view_to_calculate_size.measure(
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));					
			} catch (Exception e) {
				NexLog.e(TAG, "calculateFontSize() failed to calculate with measure");
				return fSize;
			}
			
			int nMeasuredWidth = m_view_to_calculate_size.getMeasuredWidth();
			int nMeasuredHeight = m_view_to_calculate_size.getMeasuredHeight();

			int nDownCount = 10;
			while (m_display_width < nMeasuredWidth || m_display_height < nMeasuredHeight) 
			{
				if (1 >= fSize)
					break;

				if (nDownCount < 0) 
				{
					if (m_display_width < nMeasuredWidth)
						nMeasuredWidth = (int) m_display_width;

					if (m_display_height < nMeasuredHeight)
						nMeasuredHeight = (int) m_display_height;

					NexLog.d(TAG, "calculateFontSize fail down size nMeasuredWidth= " +  nMeasuredWidth 
							+ " nMeasuredHeight= " + nMeasuredHeight 
							+ " nDownCount= " + nDownCount);
					
					break;
				}

				fSize -= 1;
				setfontproperty2windowview(m_view_to_calculate_size, fSize);

				m_view_to_calculate_size
						.measure(MeasureSpec.makeMeasureSpec(0,
								MeasureSpec.UNSPECIFIED), MeasureSpec
								.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

				nMeasuredWidth = m_view_to_calculate_size.getMeasuredWidth();
				nMeasuredHeight = m_view_to_calculate_size.getMeasuredHeight();

				nDownCount--;
			}
		}
		
		return fSize;
	}

	/**
	 * \brief This method indicates when the orientation of a device has changed; deprecated in the current API version. 
	 *  
	 * \deprecated Not supported in current API version; do not use.
	 *
	 * \param isChange  0 when the device orientation remains unchanged or 1 when the device's orientation has changed.
	 * \since version 6.22
	 * 
	 */
	public void changeOrientation(Boolean isChange)
	{
	}
	
}

class NexCEA708CaptionExtractor extends NexCaptionExtractor implements CaptionExtractorCommonInterface {

	private final static String LOG_TAG = "NexCEA708CaptionExtractor";

	private NexEIA708Struct mStruct = new NexEIA708Struct();
	private NexLogStringQueue.CharUnit[] mLineBuffer = new NexLogStringQueue.CharUnit[NexLogStringQueue.LOGSTRQ_MAX_COUNT_IN_A_ROW];

	private Rect mRenderingArea = new Rect();

	@Override
	public Rect getCaptionPosition(NexCaptionWindowRect relativeRect, int viewWidth, int viewHeight) {
		Rect rect = new Rect();

		rect.left = mRenderingArea.width() * relativeRect.xPercent / 100 + mRenderingArea.left;
		rect.top = mRenderingArea.height() * relativeRect.yPercent / 100 + mRenderingArea.top;

		if (relativeRect.userDefined) {
			rect.right = mRenderingArea.width() * relativeRect.widthPercent / 100 + rect.left;
			rect.bottom = mRenderingArea.height() * relativeRect.heightPercent / 100 + rect.top;
		} else {
			rect.right = rect.left + viewWidth;
			rect.bottom = rect.top + viewHeight;
		}

		return rect;
	}

	@Override
	public void clear() {
		for (int i = 0; i < mStruct.mService[0].mWindow.length; ++i) {
			mStruct.mService[0].mWindow[i].ClearWindow();
			mStruct.mService[0].mWindow[i].HideWindow();
		}
	}

	@Override
	void setRenderingArea(Rect renderingArea, float scale) {
		mRenderingArea = renderingArea;
	}

	@Override
	ArrayList<NexCaptionRenderingAttribute> extract(NexClosedCaption data) {
		ArrayList<NexCaptionRenderingAttribute> renderingAttributeList = null;

		if (null != data) {

			if (1 == data.mCEA708Len) {
				NexLog.d(LOG_TAG, "CEA708 CHAR: " + (char)data.mCEA708Data[0]);
			} else if (1 < data.mCEA708Len) {
				NexLog.d(LOG_TAG, "CEA708 CMD: " + toHexString(data.mCEA708Data));
			}

			if (mStruct.SetSourceByteStream(data.mCEA708ServiceNO, data.mCEA708Data, data.mCEA708Len)) {
				int serviceNum = data.mCEA708ServiceNO - 1;
				if (0 == serviceNum) {
					renderingAttributeList = extractService(data, serviceNum);
				}
			}
		}

		return renderingAttributeList;
	}

	private static String toHexString(byte buf[]){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			sb.append(Integer.toHexString(0x0100 + (buf[i] & 0x00FF)).substring(1));
			sb.append(" ");
		}
		return sb.toString();
	}

	private ArrayList<NexCaptionRenderingAttribute> extractService(NexClosedCaption data, int serviceNum) {
		ArrayList<NexCaptionRenderingAttribute> renderingAttributeList = null;

		for (int i = 0; i < mStruct.mService[serviceNum].mWindow.length; ++i) {
			if (mStruct.mService[serviceNum].mWindow[i].IsUpdate()) {
				if (null == renderingAttributeList) {
					renderingAttributeList = new ArrayList<NexCaptionRenderingAttribute>();
				}

				NexCaptionRenderingAttribute renderingAttribute = new NexCaptionRenderingAttribute();
				renderingAttribute.id = i;
				renderingAttribute.removeById = true;

				boolean shouldRemove = false;
				if (0 != mStruct.mService[serviceNum].mWindow[i].mVisible) {
					renderingAttribute.mStrings = getNodeStrings(mStruct, serviceNum, i);
					if (null == renderingAttribute.mStrings) {
						shouldRemove = true;
					} else {
						renderingAttribute.mStartTime = data.getCaptionTime()[0];
						renderingAttribute.mEndTime = data.getCaptionTime()[0];
						renderingAttribute.mWindowSize = 100;
						renderingAttribute.mRemoveTime = i;
						renderingAttribute.mRelativeFontSize = getRelativeFontSize(mRenderingArea, getFontSize(mRenderingArea));
						renderingAttribute.mCaptionSettings = getCaptionSettings(serviceNum, i);
					}
				} else {
					shouldRemove = true;
				}

				if (shouldRemove) {
					renderingAttribute.mRemoveTime = i;
				}

				renderingAttributeList.add(renderingAttribute);
			}
		}

		return renderingAttributeList;
	}

	private NexCaptionWindowRect makeRelativePosition(Rect renderingArea, int serviceNum, int id) {
		NexCaptionWindowRect windowRect = new NexCaptionWindowRect();

		int anchorX = mStruct.mService[serviceNum].mWindow[id].mAnchorHorizontal, anchorY = mStruct.mService[serviceNum].mWindow[id].mAnchorVertical;

		if (0 == mStruct.mService[serviceNum].mWindow[id].mRelativePosition) {
			float x = (float)renderingArea.width() / 210f * anchorX;
			float y = (float)renderingArea.height() / 75f * anchorY;

			windowRect.xPercent = (int)(x / (float)renderingArea.width() * 100f);
			windowRect.yPercent = (int)(y / (float)renderingArea.height() * 100f);
		} else {
			windowRect.xPercent = anchorX;
			windowRect.yPercent = anchorY;
		}

		return windowRect;
	}

	private NexCaptionSetting getCaptionSettings(int serviceNum, int id) {
		NexCaptionSetting captionSetting = new NexCaptionSetting();

		CharUnit charAttr = mStruct.mService[serviceNum].mWindow[id].GetCharAttr();

		captionSetting.mEdgeStyle = getEdgeStyle(charAttr.mEdgeType);
		captionSetting.mEdgeColor = charAttr.GetARGBEdgeColor();
		captionSetting.mEdgeWidth = 1.5f;
		captionSetting.mFontSize = getFontSize(mRenderingArea);
		captionSetting.mWindowColor = mStruct.mService[serviceNum].mWindow[id].GetARGBColorWindows();
		captionSetting.mFontScale = getFontScale(charAttr.mPenSize);
        captionSetting.mGravity = getGravity(mStruct.mService[serviceNum].mWindow[id].mJustify);
		captionSetting.mRelativeWindowRect = makeRelativePosition(mRenderingArea, serviceNum, id);

		int paddingValue = 6;
		captionSetting.mPaddingLeft = captionSetting.mPaddingTop = captionSetting.mPaddingRight = captionSetting.mPaddingBottom = paddingValue;

		return captionSetting;
	}

	private NexCaptionSetting.EdgeStyle getEdgeStyle(byte edgeType) {
		NexCaptionSetting.EdgeStyle edgeStyle = NexCaptionSetting.EdgeStyle.NONE;

		switch (edgeType) {
			case NexEIA708Struct.RAISED :
				edgeStyle = NexCaptionSetting.EdgeStyle.RAISED;
				break;
			case NexEIA708Struct.DEPRESSED :
				edgeStyle = NexCaptionSetting.EdgeStyle.DEPRESSED;
				break;
			case NexEIA708Struct.UNIFORM :
				edgeStyle = NexCaptionSetting.EdgeStyle.UNIFORM;
				break;
			case NexEIA708Struct.SHADOW_LEFT :
			case NexEIA708Struct.SHADOW_RIGHT :
				edgeStyle = NexCaptionSetting.EdgeStyle.DROP_SHADOW;
				break;
		}

		return edgeStyle;
	}

	private ArrayList<NodeString> getNodeStrings(NexEIA708Struct struct, int serviceNum, int windowNum) {
		ArrayList<NodeString> nodeStrings = null;
		NodeString nodeString = null;

		if (null != struct) {
			NexEIA708Struct.EIA708Service service = mStruct.mService[serviceNum];
			NexEIA708Struct.EIA708Window window = service.mWindow[windowNum];
			int nHeight = window.GetHeight();

			for (int y = 0; y < nHeight; y++) {
				int nTextCountInRow = window.GetTextLine(mLineBuffer, y);
				if (0 > nTextCountInRow && NexLogStringQueue.LOGSTRQ_MAX_COUNT_IN_A_ROW <= nTextCountInRow) {
					NexLog.e(LOG_TAG, "Text count is invalid: " + nTextCountInRow);
					break;
				}

				if (y != 0) {
					if (nodeString != null) {
						NexLog.e(LOG_TAG, "string : " + nodeString.mString + ", line : " + y);
						nodeString.mString += "\r\n";
					}
				}

				for (int x = 0; x < nTextCountInRow; ++x) {
					if (mLineBuffer[x].mCChar == 0x00) {
						continue;
					}

                    if (null == nodeStrings) {
                        nodeStrings = new ArrayList<NodeString>();
                    }

					if (null == nodeString) {
						nodeString = makeNodeString(mLineBuffer[x]);
					} else {
						if (shouldCreateNodeString(nodeString, mLineBuffer[x])) {
							nodeStrings.add(nodeString);
							nodeString = makeNodeString(mLineBuffer[x]);
						} else {
							nodeString.mString += Character.toString((char)mLineBuffer[x].mCChar);
						}
					}
				}
			}

			if (null != nodeString) {
				nodeStrings.add(nodeString);
				NexLog.e(LOG_TAG, "string : " + nodeString.mString);
				NexLog.e(LOG_TAG, "nodeStrings size : " + nodeStrings.size());
			}
		}

		return nodeStrings;
	}

	private NodeString makeNodeString(NexLogStringQueue.CharUnit charUnit) {
		NodeString nodeString = new NodeString();
		nodeString.mString = Character.toString((char) charUnit.mCChar);
		nodeString.mItalic = NexEIA708Struct.ITALICS == charUnit.mItalics;
		nodeString.mUnderLine = NexEIA708Struct.UNDERLINE == charUnit.mUnderline;
		nodeString.mFontColor = replaceMappedFontColors(charUnit.GetARGBTextColor());
		nodeString.mBackgroundColor = charUnit.GetARGBBGColor();
		nodeString.mSuperscript = NexEIA708Struct.SUPERSCRIPT == charUnit.mOffset;
		nodeString.mSubscript = NexEIA708Struct.SUBSCRIPT == charUnit.mOffset;
		return nodeString;
	}

	private boolean shouldCreateNodeString(NodeString nodeString, NexLogStringQueue.CharUnit charUnit) {
		boolean shouldCreateNode = false;
		if (nodeString.mUnderLine != (NexEIA708Struct.UNDERLINE == charUnit.mUnderline)) {
			shouldCreateNode = true;
		} else if (nodeString.mItalic != (NexEIA708Struct.ITALICS == charUnit.mItalics)) {
			shouldCreateNode = true;
		} else if (nodeString.mFontColor != replaceMappedFontColors(charUnit.GetARGBTextColor())) {
			shouldCreateNode = true;
		} else if (nodeString.mBackgroundColor != charUnit.GetARGBBGColor()) {
			shouldCreateNode = true;
		} else if (nodeString.mSuperscript != (NexEIA708Struct.SUPERSCRIPT == charUnit.mOffset)) {
			shouldCreateNode = true;
		} else if (nodeString.mSubscript != (NexEIA708Struct.SUBSCRIPT == charUnit.mOffset)) {
			shouldCreateNode = true;
		}

		return shouldCreateNode;
	}

	private int getGravity(int justify) {
		int gravity = Gravity.START;
		switch (justify) {
			case 1:
				gravity = Gravity.END;
				break;
			case 2:
				gravity = Gravity.CENTER;
				break;
			case 3:
				gravity = Gravity.FILL;
				break;
		}

		return gravity;
	}

	private float getFontScale(byte TextType) {
		float fontSize = 1f;
		switch (TextType) {
			case NexEIA708Struct.SMALL: fontSize = 0.7f;
				break;
			case NexEIA708Struct.STANDARD: fontSize = 1f;
				break;
			case NexEIA708Struct.LARGE: fontSize = 1.2f;
				break;
			case NexEIA708Struct.EXTRA_LARGE: fontSize = 1.5f;
				break;
		}

		return fontSize;
	}
}
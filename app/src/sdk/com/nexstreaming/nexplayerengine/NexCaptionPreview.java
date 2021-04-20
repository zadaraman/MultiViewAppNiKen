package com.nexstreaming.nexplayerengine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.nexstreaming.nexplayerengine.NexClosedCaption.CaptionColor;

import java.net.URL;


/**
 * \brief  This class defines the look of a caption preview renderer so that a preview of captions may be displayed to the user.
 * 
 * This class can be used as a preview of user selections, to show how captions will be rendered on screen during playback when different 
 * attributes such as caption window, background, text color, and edge effect have been set by the user. 
 * 
 * This preview can be used for any format of captions.
 *
 * \since version 6.15
 */
public class NexCaptionPreview extends View {

	private static final String LOG_TAG = "CAPTION_PREVIEW";
	
	public static final int PTEXT_ALIGN_HORIZONTAL_LEFT = 0;
	public static final int PTEXT_ALIGN_HORIZONTAL_CENTER = 1;
	public static final int PTEXT_ALIGN_HORIZONTAL_RIGHT = 2;
	
	public static final int PTEXT_ALIGN_VERTICAL_TOP = 0;
	public static final int PTEXT_ALIGN_VERTICAL_MIDDLE = 1;
	public static final int PTEXT_ALIGN_VERTICAL_BOTTOM = 2;
	
	private static final int[] ATTRS_LAYOUT = new int[] {android.R.attr.height, android.R.attr.width};
	private static final int[] ATTRS_PADDING = new int[] {android.R.attr.padding};	
	private static final int[] ATTRS_COLOR = new int[] {android.R.attr.textColor,android.R.attr.background};
	private static final int[] ATTRS_STYLE = new int[] {android.R.attr.textStyle};
	
	private Context m_context;
	
	private CaptionColor m_fontColor = null;
	private int m_fontOpacity = 0;
	private CaptionColor m_bgColor = null;
	private int m_bgOpacity = 0;
	private CaptionColor m_winColor = null;
	private int m_winOpacity = 0;
	private CaptionColor m_StrokeColor = null;
	private int m_StrokeOpacity = 0;
	private float m_StrokeWidth = 0.0f;
	
	private int m_ShadowColor = NexClosedCaption.DEFAULT_SHADOW_COLOR;
	private int m_RaisedColor = NexClosedCaption.DEFAULT_SHADOW_COLOR;
	private int m_DepressedColor = NexClosedCaption.DEFAULT_SHADOW_COLOR;
//	private int m_ShadowOpacity = 0;
	
	private Typeface m_typeItalic = null;
	private Typeface m_typeBoldItalic = null;
	private Typeface m_typeBold = null;
	private Typeface m_typeNormal = null;
	
	private boolean m_IsShadow = false;
	private boolean m_IsRaised = false;
	private boolean m_IsDepressed = false;
	private boolean m_IsUniform = false;
	
	private float m_EMFSpecular = 8;	//8 is default
	private float m_EMFBlurRadius = 3;	//3 is default
	
	private float m_fontSizeRate = 100;
	
	private int m_vAlign = PTEXT_ALIGN_HORIZONTAL_LEFT;
	private int m_hAlign = PTEXT_ALIGN_VERTICAL_TOP;
	
	private String m_previewText = "";
	private int m_TextSize = 42;
	
	private float m_styleWidth = 0;
	private float m_styleHeight = 0;
	private float m_stylePadding = 0;
	private int m_textStyle = Typeface.NORMAL;
	private int m_styleTextColor = Color.BLACK;
	private String m_styleBackground = "";
	
	Paint m_paint = new Paint();
	Rect m_bounds = new Rect();
	Rect m_BgBounds = new Rect();
	Paint.FontMetrics m_FontMetrics = new Paint.FontMetrics();
	private static final float DEFAULT_HDIP_DENSITY_SCALE = 1.5f;
	/**
	* \brief This method constructs the caption preview renderer.
	*
	* \param context      The handle for the player.
    	*
	* \since version 6.15
	*/
	public NexCaptionPreview(Context context) {
		super(context);	
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
	* \brief This method is an alternative constructor for the caption preview renderer.
	*
	* \param context  The handle for the player. 
	* \param attr     The set of attributes associated with the view. 
	*
	* \warning If the caption view is to be used in Android xml, this constructor must be used. 
	*
	* \since version 6.15
	*/
	@SuppressLint("NewApi")
	@SuppressWarnings("ResourceType")
	public NexCaptionPreview(Context context, AttributeSet attr) {
		super(context, attr);
		m_context = context;
		WrapSetLayerType();
		
		TypedArray ta = context.obtainStyledAttributes(attr, ATTRS_LAYOUT);		
//		for(int i = 0; i < ta.length(); i++)
//			NexLog.d(LOG_TAG, "NexCaptionPreview style : " + ta.getString(i));
		
//		NexLog.d(LOG_TAG, "NexCaptionPreview width : " + index);
		if(ta.getString(0) != null)
		{
			String viewHeight = ta.getString(0);
			float dipHeight = Float.parseFloat(viewHeight.substring(0, viewHeight.indexOf("dip")));
			m_styleHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipHeight, context.getResources().getDisplayMetrics());
		}
		if( ta.getString(1) != null)
		{
			String viewWidth = ta.getString(1);
			float dipWidth = Float.parseFloat(viewWidth.substring(0, viewWidth.indexOf("dip")));
			m_styleWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipWidth, context.getResources().getDisplayMetrics());
		}

		ta.recycle();
		
		ta = context.obtainStyledAttributes(attr, ATTRS_PADDING);
//		for(int i = 0; i < ta.length(); i++)
//			NexLog.d(LOG_TAG, "NexCaptionPreview style : " + ta.getString(i));
		if(ta.getString(0) != null)
		{
			String viewPadding = ta.getString(0);
			float dipPadding = Float.parseFloat(viewPadding.substring(0, viewPadding.indexOf("dip")));
			m_stylePadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipPadding, context.getResources().getDisplayMetrics());
		}
		ta.recycle();
		
		ta = context.obtainStyledAttributes(attr, ATTRS_COLOR);
//		for(int i = 0; i < ta.length(); i++)
//			NexLog.d(LOG_TAG, "NexCaptionPreview attr color: " + ta.getString(i));
		
		if(ta.getString(0) != null)
		{
			String styleColor = ta.getString(0).replaceFirst("#", "");
			NexLog.d(LOG_TAG, "NexCaptionPreview attr color: " + styleColor);
			m_styleTextColor = (int)Long.parseLong(styleColor, 16);			
		}
		
		if(ta.getString(1) != null)
		{
			String background = ta.getString(1);
			
			try {
		        URL url = new URL(background);
		        Drawable d =new BitmapDrawable(BitmapFactory.decodeStream(url.openConnection().getInputStream()));
		        setBackground(d);	        
		      }
		      catch(Exception ex) {}
		}
		ta.recycle();
		
		ta = context.obtainStyledAttributes(attr, ATTRS_STYLE);
		for(int i = 0; i < ta.length(); i++)
			NexLog.d(LOG_TAG, "NexCaptionPreview attr style : " + ta.getString(i));
		
		if(ta.getString(0) != null)
		{
			String styleInfo = ta.getString(0).replaceFirst("0x", "");
			m_textStyle = Integer.parseInt(styleInfo);
		}
		ta.recycle();

			
	}
	/* 
	 * \param pText  The text which will be set in the caption style preview.  
	 * \param size   The size of the text to be previewed.
	 * 
	 * \since version 6.15
	 * 
     */
	public void setPreviewText(String pText, int size)
	{
		m_previewText = pText;
//		m_TextSize = size;
		NexLog.d(LOG_TAG, "setPreviewText() dip = " + size);
		float pSize = size * getContext().getResources().getDisplayMetrics().density;
//		float pSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, m_context.getResources().getDisplayMetrics()); 
//		float scale = DEFAULT_HDIP_DENSITY_SCALE / getContext().getResources().getDisplayMetrics().densityDpi;
		m_TextSize = (int) (pSize);
		
		NexLog.d(LOG_TAG, "setPreviewText() px = " + m_TextSize);
	}
	

	/** 
	 * \brief This method sets the foreground color of caption preview text. 
	 * 
	 * \param fontColor  The color to be used for caption preview text.
	 * \param opacity	The opacity of the preview text as an integer, from 0 to 255, where 0 is completely transparent and 255 is completely opaque.
	 * 
	 * \since version 6.15
     */
	public void setFGCaptionColor(CaptionColor fontColor, int opacity)
	{
		m_fontColor = fontColor;
		m_fontOpacity = opacity;
	}


	/** 
	 * \brief This method sets the background color of text in the caption preview.
	 * 
	 * \param bgColor  The color to be used for the background of text (the window color where caption text will appear).
	 * \param opacity	The opacity of the preview caption window as an integer, from 0  to 255, where 0 is completely transparent and 255 is completely opaque.
	 * 
	 * \since version 6.15
     */
	public void setBGCaptionColor(CaptionColor bgColor, int opacity)
	{
		m_bgColor = bgColor;
		m_bgOpacity = opacity;
				
	}
	

	/** \brief This method sets the stroke color and width of caption preview text.
	 * 
	 * For a full list of colors, please refer to \ref NexClosedCaption::CaptionColor. 
	 * The stroke line width is in 
	 * pixels. Anti-aliasing is supported, so fractions of a pixel are allowed.  
	 * 
	 * \param strokeColor  The stroke color, or \c null to use the color from the original caption data.
	 * \param strokeOpacity The stroke opacity as an integer, from 0 to 255, where 0 is completely transparent and 255 is completely opaque.
	 * \param strokeWidth  The width of the stroke line as a float, in pixels.  
	 * 
	 * \since version 6.15 
	 */
	public void setCaptionStroke(CaptionColor strokeColor, int strokeOpacity, float strokeWidth)
	{
		resetEdgeStyle();
		
		if(strokeColor != null)
			m_StrokeColor = strokeColor;
		
		m_StrokeOpacity = strokeOpacity;
		m_StrokeWidth = strokeWidth;	
	}	
	

	/** \brief This method sets the color of the caption window when previewed.
	 * 
	 * For a full list of colors , please refer to \ref NexClosedCaption::CaptionColor.
	 * 
	 * \param windowColor  The window color, or \c null to use the color from the original caption data.
	 * \param windowOpacity  The window color opacity as an integer, from 0 to 255, where 0 is completely transparent and 255 is completely opaque.
	 * 
	 * \since version 6.15
	 */
	public void setCaptionWindowColor(CaptionColor windowColor, int windowOpacity)
	{
		m_winColor = windowColor;
		m_winOpacity = windowOpacity;
		setBackgroundColor(getColorFromCapColor(m_winColor, m_winOpacity));
	}
	

	/** \brief  This method sets the fonts to be used for caption preview text.
	 * 
	 * Four typefaces may be specified for different combinations of <b>bold</b> and 
	 * <i>italics</i>. The preview caption renderer will select the appropriate typeface from 
	 * among these based on the preview caption selections being displayed.
	 * 
	 * For best results, specify all four typefaces. Any typeface can be set 
	 * to \c null, in which case the system default typeface will be used.
	 * 
	 * \param normType          Typeface to be used for preview captions that are neither bold nor italic.
	 * \param boldType          Typeface to be used for <b>bold</b> preview captions. 
	 * \param italicType        Typeface to be used for preview captions in <i>italics</i>.
	 * \param boldItalicType    Typeface to be used for preview captions that are both <b><i>bold and in italics</i></b>.
	 *
	 * \since version 6.15
	 */
	public void setFonts(Typeface normType, Typeface boldType, Typeface italicType, Typeface boldItalicType)
	{
		if(normType!= null)
			m_typeNormal = normType;		
		if(boldType != null)
			m_typeBold = boldType;		
		if(italicType != null)
			m_typeItalic = italicType;
		if(boldItalicType != null)
			m_typeBoldItalic = boldItalicType;
	}


       /**
        * \brief This method adds a drop shadow effect to caption preview text.
        * 
        * \param isShadow  Set this to \c TRUE to force text to be displayed with a shadow, or \c FALSE for no shadow.
        * \param shadowColor  The shadow color, or \c null to use the color from the original caption data.
        * \param shadowOpacity  The shadow opacity as an integer, from 0 to 255, where 0 is completely transparent and 255 is completely opaque.
        *
        * \since version 6.15
        */
  	public void setShadow(boolean isShadow, CaptionColor shadowColor, int shadowOpacity)
	{
  		resetEdgeStyle();
		m_IsShadow = isShadow;
		
		if(shadowColor != null)
  		{
			m_ShadowColor = getColorFromCapColor(shadowColor, shadowOpacity);
  		}
	}
  	

   /**
    * \brief This method adds a raised filter to caption preview text.
    *
    * \param isRaised \c TRUE if the text should be displayed as if raised; otherwise \c FALSE.
    *
    * \since version 6.15
    */
	public void setRaise(boolean isRaised)
	{
		resetEdgeStyle();
		m_IsRaised = isRaised;
	}
	
	/**
	 * \brief This method indicates whether or not CEA 608 closed captions should be displayed as if "raised" (in a set font color).
	 * 
	 * If CEA 608 closed captions are "raised", they should be displayed as if they rise from the video display slightly,
	 * and the color of the raised portion of the caption text can be set by the user.
	 * 
	 * \param isRaise  \c TRUE if closed captions are raised, \c FALSE if not.
	 * \param raisedColor  The color of the raised part of the text set by the user, or \c null to use the default color.
	 * \param raisedOpacity  The opacity of the raised part of the text as an integer, from 0 (transparent) to 255 (fully opaque).  
	 *
	 * \since version 6.20
	 */
	public void setRaisedWithColor(boolean isRaised, CaptionColor raisedColor, int raisedOpacity)
	{
		resetEdgeStyle();
		m_IsRaised = isRaised;
		
		if(raisedColor != null)
  		{
  			m_RaisedColor = getColorFromCapColor(raisedColor, raisedOpacity);
  		}
	}
	

   /**
    * \brief This method adds a depressed filter to caption preview text.
    *
    * \param isDepressed \c TRUE if the text should be displayed as if depressed into the screen; otherwise \c FALSE.
    * 
    * \since version 6.15
    */
	public void setDepressed(boolean isDepressed)
	{
		resetEdgeStyle();
		m_IsDepressed = isDepressed;
	}
	
       /**
	* \brief This method indicates whether or not CEA 608 closed captions should be displayed as if "depressed" (in a set font color).
	* 
	* If CEA 608 closed captions are "depressed", they should be displayed as if they are pressed into the video display slightly,
	* and the color of the depressed portion of the caption text ("sunken" into the display) can be set by the user.
	* 
	* \param isDepressed  \c TRUE if closed captions are depressed, \c FALSE if not.
	* \param depColor  The color of the depressed part of the text set by the user, or \c null to use the default color.
	* \param depOpacity  The opacity of the depressed part of the text as an integer, from 0 (transparent) to 255 (fully opaque).  
	*
        * \since version 6.20
        */
	public void setDepressedWithColor(boolean isDepressed, CaptionColor depColor, int depOpacity)
	{
  		resetEdgeStyle();  		
  		m_IsDepressed = isDepressed;
  		
  		if(depColor != null)
  		{
  			m_DepressedColor = getColorFromCapColor(depColor, depOpacity);
  		}
	}

    /**
     * \brief This method adds a uniform filter to caption preview text.
     * 
     * \param isUniform \c TRUE if the text should be displayed with a uniform black outline; otherwise \c FALSE.
     * 
     * \since version 6.15
     *
     */
	public void setUniform(boolean isUniform)
	{
		resetEdgeStyle();
		m_IsUniform = isUniform;
		if(isUniform)
		{			
			if(m_StrokeColor == null)
			{
				m_StrokeColor = CaptionColor.BLACK;
				m_StrokeOpacity = 255;
				m_StrokeWidth = 0.5f;
			}
		}
	}

    /**
     * \brief This method resets the edge effects on caption preview text. 
     *
     * Possible edge effects include \c setShadow, \c setCaptionStroke, \c setRaise, and \c setDepressed.
     * 
     * \since version 6.15
     */
	public void resetEdgeStyle()
	{
		m_IsShadow = false;
		m_ShadowColor = NexClosedCaption.DEFAULT_SHADOW_COLOR;
		m_RaisedColor = Color.BLACK;
		m_DepressedColor = Color.BLACK;
		
		m_IsRaised = false;
		m_IsDepressed = false;
		m_IsUniform = false;
		
		m_StrokeColor = null;
		m_StrokeOpacity = 255;
		m_StrokeWidth = 1.0f;
		
		if (null != m_paint)
			m_paint.reset();
	}


	/**  
	 * \brief This method sets the specular level of the Emboss Mask filter used when a user sets the caption preview to be displayed 'Raised' or 'Depressed' in the UI.
	 * 
	 * \param specular  The specular level of the Emboss Mask filter.
	 * 
	 * \since version 6.15
	 */ 
	public void setEmbossSpecular(float specular)
	{
		if(specular >= 0)
			m_EMFSpecular = specular;
	}
	

	/** 
	 * \brief This method sets the radius of blur for the Emboss Mask filter when a user sets the caption preview to be displayed 'Raised' or 'Depressed' in the UI.
	 * 
	 * \param radius  The radius of blur when Emboss Mask filter is used.
	 *    
	 * \since version 6.15            
	 */ 
	public void setEmbossBlurRadius(float radius)
	{
		if(radius >= 0)
			m_EMFBlurRadius = radius;
	}


	/**
	 * \brief  This method changes the font size of text in the caption preview.
	 * 
	 * To double the font size, the parameter \c sizeRate should be set to 200.
	 * In contrast, to halve the font size, \c sizeRate should be set to 50.
	 *
	 * \param sizeRate The change in size of the caption preview text font, as a percentage.
	 * 
	 * \since version 6.15
	 */        
	public void changeFontSize(int sizeRate)
	{
		
		NexLog.d(LOG_TAG, "changeFontSize() : " + sizeRate);
		
		if(sizeRate >= 50 && sizeRate <= 200)
			m_fontSizeRate = (float)sizeRate;
		else
			m_fontSizeRate = 100;
	}
	

	/** 
	 * \brief This method sets the alignment of text inside the caption window preview.
	 * 
	 * \param horizontal    Horizontal alignment value.  This will be one of:
	 * 						- PTEXT_ALIGN_HORIZONTAL_LEFT: left aligned
	 * 						- PTEXT_ALIGN_HORIZONTAL_CENTER: center aligned
	 * 						- PTEXT_ALIGN_HORIZONTAL_RIGHT: right aligned
	 * 
	 * \param vertical:	Vertical alignment value.  This will be one of:
	 * 						- PTEXT_ALIGN_VERTICAL_TOP: top aligned
	 * 						- PTEXT_ALIGN_VERTICAL_MIDDLE: middle aligned
	 * 						- PTEXT_ALIGN_VERTICAL_BOTTOM: bottom aligned
	 * 
	 * \since version 6.15
     */
	public void setPreviewTextAlign(int horizontal, int vertical)
	{
		m_hAlign = horizontal;
		m_vAlign = vertical;
	}


	private int getColorFromCapColor(CaptionColor cColor, int cOpacity)
	{
		int setColor = cColor.getFGColor();
		int color = Color.argb(cOpacity, Color.red(setColor), Color.green(setColor), Color.blue(setColor));
		
		return color;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (null == m_paint)
			m_paint = new Paint();
		
		if (null == m_bounds)
			m_bounds = new Rect();
		
        int measureHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        int measureWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        
        int width = 0;
        int height = 0;
        
        NexLog.d(LOG_TAG, "onMeasure() mode measureWidthMode : " + measureWidthMode + " / measureHeightMode : " + measureHeightMode);
        
        switch(measureWidthMode)
        {
        	//wrap_content
        	case MeasureSpec.AT_MOST:
        		m_paint.setTextSize(m_TextSize);        		
        		m_paint.getTextBounds(m_previewText, 0, m_previewText.length(), m_bounds);
        		width = m_bounds.width();
        		break;
        	
        	//fill_parent, match_parent
        	case MeasureSpec.EXACTLY:
        		width = MeasureSpec.getSize(widthMeasureSpec);
            	break;
            	
        	case MeasureSpec.UNSPECIFIED:
        		width = widthMeasureSpec;
            	break;
        	
        }
        switch(measureHeightMode)
        {
        	//wrap_content
        	case MeasureSpec.AT_MOST:
        		m_paint.setTextSize(m_TextSize);
        		m_paint.getTextBounds(m_previewText, 0, m_previewText.length(), m_bounds);
        		m_paint.getFontMetrics(m_FontMetrics);
        		
        		NexLog.d(LOG_TAG, "onMeasure() getFontMetric bottom : " + m_FontMetrics.bottom + " / top : " + m_FontMetrics.top);
//        		height = (int)(m_FontMetrics.bottom - m_FontMetrics.top);
        		height = m_bounds.height();
        		break;
        	
        	//fill_parent, match_parent
        	case MeasureSpec.EXACTLY:
        		height = MeasureSpec.getSize(heightMeasureSpec);
            	break;
            	
        	case MeasureSpec.UNSPECIFIED:
        		height = heightMeasureSpec;
            	break;
        	
        } 
        
        NexLog.d(LOG_TAG, "onMeasure() width : " + width + " / height : " + height);
        
        if(m_styleWidth > 0)
        	width = (int)m_styleWidth;
        
        if(m_styleHeight > 0)
        	height = (int)m_styleHeight;
                
	    setMeasuredDimension(width, height);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (null == m_paint)
			m_paint = new Paint();
		
		if (null == m_bounds)
			m_bounds = new Rect();
				
		int fontSize = (int)(m_TextSize * (m_fontSizeRate/100f));
		Paint prevPaint = new Paint();
		prevPaint = m_paint;
		prevPaint.reset();		
		prevPaint.setAntiAlias(true);
		
		NexLog.d(LOG_TAG, "text size : " + fontSize);
		
		prevPaint.setTextSize(fontSize);
		//prevPaint.setColor(Color.WHITE);
		prevPaint.setStyle(Style.FILL);
		
		//font type...
		if (m_typeNormal != null) {
			prevPaint.setTypeface(m_typeNormal);
		}
		if (m_typeBold != null) {
			prevPaint.setFakeBoldText(true);
			prevPaint.setTypeface(m_typeBold);
		}
		if (m_typeItalic != null) {
			prevPaint.setTextSkewX(-0.20f);
			prevPaint.setTypeface(m_typeItalic);
		}
		if (m_typeBoldItalic != null) {
			prevPaint.setFakeBoldText(true);
			prevPaint.setTextSkewX(-0.20f);
			prevPaint.setTypeface(m_typeBoldItalic);
		}
	
		int xPos = getLeft();
		int yPos = getTop();


		if(m_winColor != null)
		{

			setBackgroundColor(getColorFromCapColor(m_winColor, m_winOpacity));		
		}
		
        prevPaint.getTextBounds(m_previewText, 0, m_previewText.length(), m_bounds);
        float width = prevPaint.measureText(m_previewText);
        
        switch(m_hAlign)
        {
	        case PTEXT_ALIGN_HORIZONTAL_LEFT:
	        default:
	        	xPos = 0;
	        	break;
	        case PTEXT_ALIGN_HORIZONTAL_CENTER:
	        	xPos = (int) ((getWidth() - width)/2);
	        	break;
	        case PTEXT_ALIGN_HORIZONTAL_RIGHT:
	        	xPos = (int) (getWidth() - width);
	        	break;
        }
        
        switch(m_vAlign)
        {
	        case PTEXT_ALIGN_VERTICAL_TOP:
	        	yPos = 0;
	        default:
	        	break;
	        case PTEXT_ALIGN_VERTICAL_MIDDLE:
	        	yPos = (getHeight() - m_bounds.height() - (int)prevPaint.descent())/2;
	        	break;
	        case PTEXT_ALIGN_VERTICAL_BOTTOM:
	        	yPos = (getHeight() - m_bounds.height() - (int)prevPaint.descent());
	        	break;
        }

		if(m_bgColor != null)
		{	
			if (null == m_BgBounds)
				m_BgBounds = new Rect();
			
			m_BgBounds.set(xPos, yPos, (int) (xPos+width), yPos+m_bounds.height() + (int)prevPaint.descent());
			int color = getColorFromCapColor(m_bgColor, m_bgOpacity);
			prevPaint.setColor(color);
			canvas.drawRect(m_BgBounds, prevPaint);
		}

		if(m_IsShadow)
		{
			prevPaint.setShadowLayer(NexClosedCaption.DEFAULT_SHADOW_PARAM[0], 
					NexClosedCaption.DEFAULT_SHADOW_PARAM[1], 
					NexClosedCaption.DEFAULT_SHADOW_PARAM[2], m_ShadowColor);
		}
		
		else if(m_IsRaised)
		{
			prevPaint.setShadowLayer(NexClosedCaption.DEFAULT_RAISED_PARAM[0], 
					NexClosedCaption.DEFAULT_RAISED_PARAM[1], 
					NexClosedCaption.DEFAULT_RAISED_PARAM[2], m_RaisedColor);
		}
		else if(m_IsDepressed)
		{
			prevPaint.setShadowLayer(NexClosedCaption.DEFAULT_DEPRESSED_PARAM[0], 
					NexClosedCaption.DEFAULT_DEPRESSED_PARAM[1], 
					NexClosedCaption.DEFAULT_DEPRESSED_PARAM[2], m_DepressedColor);
		}
		
//		else
//			prevPaint.setMaskFilter(null);
		
		if(m_fontColor != null)
		{
			prevPaint.setColor(getColorFromCapColor(m_fontColor, m_fontOpacity));	
		}
		else
			prevPaint.setColor(m_styleTextColor);
		
		yPos += fontSize;	
		
		float[] direction = null;
/*	
		if(m_IsShadow)
		{
			if(m_ShadowColor != null)
			{
				int shadowColor = getColorFromCapColor(m_ShadowColor, m_ShadowOpacity);
				prevPaint.setShadowLayer(NexClosedCaption.DEFAULT_SHADOW_PARAM[0], 
						NexClosedCaption.DEFAULT_SHADOW_PARAM[1], 
						NexClosedCaption.DEFAULT_SHADOW_PARAM[2], shadowColor);
			}
			else
				prevPaint.setShadowLayer(NexClosedCaption.DEFAULT_SHADOW_PARAM[0], 
						NexClosedCaption.DEFAULT_SHADOW_PARAM[1], 
						NexClosedCaption.DEFAULT_SHADOW_PARAM[2], NexClosedCaption.DEFAULT_SHADOW_COLOR);
		}
	
		else if(m_IsRaised)
		{
			prevPaint.setMaskFilter(new EmbossMaskFilter( new float[] {-1.0f, -1.0f, -1.0f} , 0.5f, m_EMFSpecular, m_EMFBlurRadius ));
		}		
		else if(m_IsDepressed)
		{
			prevPaint.setMaskFilter(new EmbossMaskFilter( new float[] {1.0f, 1.0f, -1.0f} , 0.5f, m_EMFSpecular, m_EMFBlurRadius ));
		}
*/	
		canvas.drawText(m_previewText, xPos, yPos, prevPaint);
		prevPaint.setMaskFilter(null);
		
		if(m_StrokeColor != null)
		{
			prevPaint.setStyle(Style.STROKE);
			prevPaint.setStrokeWidth(m_StrokeWidth);
			prevPaint.setColor(getColorFromCapColor(m_StrokeColor, m_StrokeOpacity));  
			canvas.drawText(m_previewText, xPos, yPos, prevPaint);
		}	
	}

}

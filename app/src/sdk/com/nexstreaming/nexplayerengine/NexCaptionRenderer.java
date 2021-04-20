package com.nexstreaming.nexplayerengine;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.nexstreaming.nexplayerengine.NexClosedCaption.CaptionColor;
import com.nexstreaming.nexplayerengine.NexClosedCaption.CaptionMode;

import java.util.ArrayList;

/**
 * \brief  This class defines the CEA 608 Closed Caption renderer view and displays the information to the user.
 * 
 * In order to support all of the text attributes and display options of the CEA 608 specifications, it is necessary
 * to create a separate Caption Renderer view with the NexCaptionRenderer class.  
 * 
 * The NexCaptionRenderer view is overlaid on the application's video output, and as a result, whenever the video 
 * display changes, the NexCaptionRenderer must also be updated in order for captions to be
 * accurately displayed.
 * 
 * In particular, the NexCaptionRenderer in an application requires the following information:
 *   - <b>Video Size Information:</b>  Whenever the video output in an application's UI changes size or orientation changes, 
 *   the new size information should also be passed to NexCaptionRenderer by calling the NexCaptionRenderer.setRenderArea method.
 *   - <b>New CEA 608 closed caption Data:</b>  Whenever new CEA 608 closed caption data is received, it should be passed to
 *   the NexCaptionRenderer by calling the NexCaptionRenderer.SetData method.
 * 
 *  When it is necessary to clear CEA 608 closed captions from the screen, for example when seeking in or stopping content, 
 *  calling the \link NexCaptionRenderer.makeBlankData\endlink method will erase captions existing on the screen. 
 * 
 * This class should only be used when displaying CEA 608 closed captions, not other kinds of subtitles.
 * 
 * To display CFF and 3GPP timed text, please use \link NexCaptionRendererForTimedText\endlink.
 */
public class NexCaptionRenderer extends View {
	
	private static final String TAG = "NexCaptionRender";
	public static final int RENDER_MODE_BASIC = 0;
	public static final int RENDER_MODE_CUSTOM = 1;

	NexCaptionPainter mCaptionPainter = null;
	NexCaptionSetting mCaptionSetting = null;
	
	private Context m_context;

	private int m_Width = 0;
	private int m_Height = 0;
	private int m_X = 0;
	private int m_Y = 0;
	
	private int m_border_X = 0;
	private int m_border_Y = 0;
	
	private NexClosedCaption m_Caption = null;
	private Paint m_paint = null;
	
	private Handler m_handler = new Handler();

	private CaptionColor setFgColor = null;
	private CaptionColor setBgColor = null;
	private int setFgOpacity = 0;
	private int setBgOpacity = 0;
	
	private CaptionColor setWindowColor = null;
	private int setWindowOpacity = 0;
	private int setShadowOpacity = 0;
	private int setRaisedOpacity = 0;
	private int setDepressedOpacity = 0;
	
	private CaptionColor setStrokeColor = null;
	private CaptionColor setShadowColor = null;
	private CaptionColor setRaisedColor = null;
	private CaptionColor setDepressedColor = null;
	private float setStrokeWidth = 0.0f;

	private boolean setBold = false;
	private boolean setShadow = false;

	private boolean setRaise = false;
	private boolean setDepressed = false;
	private boolean setUniform = false;
	
	private boolean m_isOffUnderline = false;
	private boolean m_isOffFlashing = false;
	
	private float m_fontScale = 0.0f;
	private float m_fontSize = 0.0f;
	private float m_sizeRate = 100.0f;
	private float m_lineSpaceRate = 100.0f;
	
	private Typeface m_typeItalic = null;
	private Typeface m_typeBoldItalic = null;
	private Typeface m_typeBold = null;
	private Typeface m_typeNormal = null;
	
	private int m_renderMode = RENDER_MODE_BASIC;	//0 is basic, 1 is custom.
	
	/**
	 * \brief This sets the CEA 608 closed caption renderer in FULL mode.
	 * 
	 * In order for captions to be legible when they are displayed, the closed caption rendering area
	 * defined here is a rectangle set within the larger video rendering area.  This ensures that when
	 * characters are displayed on the screen, they are not difficult to see because they are flush with the
	 * edge of the video.
	 * 
	 * The parameters \c borderX and \c borderY set a kind of border around the caption rendering area and within the video
	 * rendering area where no captions will appear.  
	 * 
	 * \param context  The handle for the player.
	 * \param borderX  The horizontal indent from the edge of the video rendering area to the caption rendering area.
	 * \param borderY  The vertical indent from the edge of the video rendering area to the caption rendering area.
	 */
	public NexCaptionRenderer(Context context, int borderX, int borderY) {
		super(context);
		WrapSetLayerType();
		m_context = context;
		m_border_X = borderX;
		m_border_Y = borderY;

		mCaptionPainter = new NexCaptionPainter(m_context, NexContentInformation.NEX_TEXT_CEA608);
		mCaptionSetting = new NexCaptionSetting();
	}
/** 
 * \brief This is an alternative constructor for the NexCaptionRenderer.
 * \warning If the caption view is to be used in Android xml, this constructor must be used.
 *
 * \param context  The handle for the player.
 * \param attrs   The set of attributes associated with the view.
 *
 * \since version 6.6
 */
	public NexCaptionRenderer(Context context, AttributeSet attrs) {
		super(context,attrs);
		m_context = context;
		WrapSetLayerType();

		mCaptionPainter = new NexCaptionPainter(m_context, NexContentInformation.NEX_TEXT_CEA608);
		mCaptionSetting = new NexCaptionSetting();
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
	 * \brief This method sets the caption data to be rendered and displayed with CEA 608 closed captions.
	 * 
	 * \param caption  The NexClosedCaption object containing the closed captions and attributes to be displayed.
	 */
	public void SetData(NexClosedCaption caption)
	{
		m_Caption = caption;

		if (RENDER_MODE_CUSTOM == m_renderMode) {
			mCaptionPainter.setDataSource(caption);
		}
	}
	

       /** 
        * \brief  This method allows the rendering mode of CEA 608 closed captions to be chosen.
        * 
        * The two available rendering modes are basic CEA 608 rendering, which meets the specifications of the CEA 608 standard,
        * and an enhanced rendering mode that makes it possible for CEA 608 closed captions to be rendered with additional display settings.
        * This second mode makes it possible for CEA 608 closed captions to satisfy FCC regulations regarding captioning options (including
        * the changing of foreground and background color, font size, font type). 
        * 
        * 
        * \param mode Zero when CEA 608 closed captions will be rendered and displayed according to the CEA 608 specifications; or
        *             1 when CEA 608 closed captions should be rendered with additional rendering options to satisfy FCC regulations. 
        * 
        * \since version 6.7
        */	

	public void setMode(int mode)
	{
		m_renderMode = mode;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension( MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec) );
	}
	
	/**
	 * \brief This sets the CEA 608 closed caption rendering area within the displayed video area.
	 * 
	 * CEA 608 closed captions will be displayed in this caption rendering area on the screen within the border defined by NexCaptionRenderer.
	 * 
	 * \param x  The horizontal position of the top left corner of the video rendering area, as an integer.
	 * \param y  The vertical position of the top left corner of the video rendering area, as an integer. 
	 * \param width  The width of the caption rendering area, as an integer.
	 * \param height  The height of the caption rendering area, as an integer.
	 */
	public void setRenderArea(int x, int y, int width, int height)
	{
		m_X = x;
		m_Y = y;
		m_Width = width;
		m_Height = height;
		
		NexLog.d(TAG, "SetRenderArea : X = " + x + " Y = " + y + " W = " + width + " H = " + height);

		mCaptionPainter.setRenderingArea(new Rect(m_X + m_border_X, m_Y + m_border_Y, m_X + m_Width - m_border_X, m_Y + m_Height - m_border_Y), 1);
	}
	
	
    /**
     * \brief This method resets the edge effects on CEA 608 closed captions. 
     *
     * Possible edge effects includes setShadow, setCaptionStroke, setRaise, and setDepressed.
     * 
     * \since version 6.18
     */	
	public void resetEdgeEffect()
	{
		setShadowOpacity = 255;
		setShadowColor = CaptionColor.BLACK;
		setStrokeColor = null;
		setStrokeWidth = 0.0f;
		
		setBold = false;
		setShadow = false;		
		setRaise = false;
		setRaisedOpacity = 255;
		setRaisedColor = CaptionColor.BLACK;
		setDepressedOpacity = 255;
		setDepressedColor = CaptionColor.BLACK;
		setDepressed = false;
		setUniform = false;

		mCaptionSetting.mEdgeStyle = NexCaptionSetting.EdgeStyle.DEFAULT;
		mCaptionSetting.mEdgeColor = NexCaptionSetting.DEFAULT;
		mCaptionSetting.mEdgeWidth = NexCaptionSetting.DEFAULT;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
	
         /** \brief This method initializes the style attributes of CEA 608 closed captions that may be set by a user, 
         * 			including the colors of the text, background, and caption window as well as the edge style and the font size.
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

		m_sizeRate = 100.0f;
		
		m_typeItalic = null;
		m_typeBoldItalic = null;
		m_typeBold = null;
		m_typeNormal = null;
		
		resetEdgeEffect();

		mCaptionSetting.init();
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
	
	/**
	 * \brief  This clears the currently displayed captions in CEA 608 closed captions.
	 * 
	 * Use this method if it is necessary to erase captions that are left on the screen.
	 *
	 */
	public void makeBlankData()
	{
		if(null != m_Caption)
			m_Caption.makeBlankData();

		mCaptionPainter.clear();
	}
	/** \brief This sets the CEA 608 closed captions foreground (text) color.
	 * 
	 * For a full list of colors , please refer to \ref NexClosedCaption::CaptionColor.
	 * 
	 * \param foreground  The foreground color, or \c null to use the color from the original caption data.
	 * \param fgOpacity  The foreground opacity, from 0 (transparent) to 255 (fully opaque). 
	 */
	public void setFGCaptionColor(CaptionColor foreground, int fgOpacity)
	{
		setFgColor = foreground;
		setFgOpacity = fgOpacity;

		mCaptionSetting.mFontColor = getColorFromCapColor(setFgColor, setFgOpacity);
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
	/** \brief This sets the background color of CEA 608 closed captions.
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

		mCaptionSetting.mBackgroundColor = getColorFromCapColor(setBgColor, setBgOpacity);
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
	/** \brief This sets the CEA 608 caption renderer stroke color and width.
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

		mCaptionSetting.mEdgeColor = getColorFromCapColor(setStrokeColor, 255);
		mCaptionSetting.mEdgeStyle = NexCaptionSetting.EdgeStyle.UNIFORM;
		mCaptionSetting.mEdgeWidth = strokeWidth;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
	
	
	/** \brief This method sets the window color of CEA 608 closed captions.
	 * 
	 * For a full list of colors , please refer to \ref NexClosedCaption::CaptionColor.
	 * 
	 * \param windowColor  The window color, or \c null to use the color from the original caption data.
	 * \param windowOpacity  The window color opacity, from 0 (transparent) to 255 (fully opaque). 
         * 
         * \since version 6.6
	 */
	public void setWindowColor(CaptionColor windowColor, int windowOpacity)
	{
		setWindowColor = windowColor;
		setWindowOpacity = windowOpacity;

		mCaptionSetting.mWindowColor = getColorFromCapColor(setWindowColor, setWindowOpacity);
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
	/** \brief Controls whether captions are displayed in bold text.
	 * 
	 * Caption data includes attributes such as bold and italic.  
	 * 
	 * Normally, the caption renderer displays each character
	 * in normal, bold or italic based on the attributes included in the caption data.  
	 * 
	 * However in some cases (such as for
	 * users with visual impairment) it may be desirable to force the use of bold text.  
	 * 
	 * By enabling this option, the
	 * bold attributes in the caption data are ignored and a bold font is used for all characters.
	 * 
	 * \param isBold   Set this to \c TRUE to force bold text, or \c FALSE to use the bold attribute in the original captions.
	 * 
	 */
	public void setBold(boolean isBold)
	{
		setBold = isBold;

		mCaptionSetting.mBold = isBold ? NexCaptionSetting.StringStyle.APPLY : NexCaptionSetting.StringStyle.REMOVE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
	/** \brief This sets whether the CEA-608 captions have a shadow.
	 * 
	 * \param isShadow  Set this to \c TRUE to force shadow text, or \c FALSE for no shadow. 
	 * 
	 */
	public void setShadow(boolean isShadow)
	{
		if (isShadow)
		{
			resetEdgeEffect();
		}

		setShadow = isShadow;

		mCaptionSetting.mEdgeStyle = isShadow ? NexCaptionSetting.EdgeStyle.DROP_SHADOW : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
	
	/** 
	* \brief  This method sets whether CEA 608 closed captions should be displayed with a colored shadow.
	* 
        *  It can be used to allow users to select the color they want to use for shadows added to CEA 608 closed captions.
        * 
	* \param isShadow  Set this to \c TRUE to force text to be displayed with a shadow, or \c FALSE for no shadow. 
	* \param shadowColor  The shadow color, or \c null to use the color from the original caption data.
	* \param shadowOpacity  The shadow opacity as an integer, from 0 (transparent) to 255 (fully opaque). 
	* 
        * \since version 6.16
	*/
	public void setShadowWithColor(boolean isShadow, CaptionColor shadowColor, int shadowOpacity)
	{
		if (isShadow)
		{
			resetEdgeEffect();
		}

		NexLog.d(TAG, " setShadowWithColor is called.. color" + shadowColor + "opacity " +shadowOpacity);
		setShadow = isShadow;
		setShadowColor = shadowColor;
		setShadowOpacity = shadowOpacity;

		if(shadowColor != null) {
			mCaptionSetting.mEdgeColor = getColorFromCapColor(shadowColor, shadowOpacity);
		}

		mCaptionSetting.mEdgeStyle = isShadow ? NexCaptionSetting.EdgeStyle.DROP_SHADOW : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
	
	
	/** \brief  This sets the font used for the captions.
     * 
     * Four typefaces may be specified for different combinations of bold and 
     * italic. The  caption renderer will select the appropriate typeface from 
     * among these based on the CEA-608 captions being displayed.
     * 
     * For best results, specify all four typefaces. Any typeface can be set 
     * to \c null, in which case the system default typeface will be used.
     * 
     * \param normType          Typeface to be used for captions that are neither bold  nor italic.
     * \param boldType          Typeface to be used for bold CEA-608 captions. 
     * \param italicType        Typeface to be used for italic CEA-608 captions.
     * \param boldItalicType    Typeface to be used for CEA-608 captions that are both and italic.
     */
	public void setFonts(Typeface normType, Typeface boldType, Typeface italicType, Typeface boldItalicType)
	{
		if(boldItalicType != null)
			m_typeBoldItalic = boldItalicType;
		if(boldType != null)
			m_typeBold = boldType;
		if(normType!= null)
			m_typeNormal = normType;
		if(italicType != null)
			m_typeItalic = italicType;

		mCaptionSetting.mFontFamily = normType;

		if(boldType != null) {
			m_typeBold = boldType;
			mCaptionSetting.mBold = NexCaptionSetting.StringStyle.APPLY;
		} else {
			mCaptionSetting.mBold = NexCaptionSetting.StringStyle.DEFAULT;
		}

		if(italicType != null) {
			m_typeItalic = italicType;
			mCaptionSetting.mItalic = NexCaptionSetting.StringStyle.APPLY;
		} else {
			mCaptionSetting.mBold = NexCaptionSetting.StringStyle.DEFAULT;
		}

		if(boldItalicType != null) {
			m_typeBoldItalic = boldItalicType;
			mCaptionSetting.mBold = NexCaptionSetting.StringStyle.APPLY;
			mCaptionSetting.mItalic = NexCaptionSetting.StringStyle.APPLY;
		}

		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
	
	/**
	 * \brief This method indicates whether or not CEA 608 closed captions are "raised".
	 * 
	 * If CEA 608 closed captions are "raised", they should be displayed as if rising above the video display slightly,
	 * for example as if they were embossed.
	 * 
	 * To have the raised closed captions be displayed in a user-defined color, see the \c setRaiseWithColor method instead.
	 * 
	 * \param isRaise  \c TRUE if closed captions are raised, \c FALSE if they are not.
	 * 
	 * \see NexCaptionRenderer.setRaiseWithColor
	 */
	public void setRaise(boolean isRaise)
	{
		if (isRaise)
		{
			resetEdgeEffect();
		}

		setRaise = isRaise;

		mCaptionSetting.mEdgeStyle = isRaise ? NexCaptionSetting.EdgeStyle.RAISED : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
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
	public void setRaiseWithColor(boolean isRaise, CaptionColor raisedColor, int raisedOpacity)
	{
		if (isRaise)
		{
			resetEdgeEffect();
		}

		setRaise = isRaise;
        setRaisedColor = raisedColor;
        setRaisedOpacity = raisedOpacity;

		if(raisedColor != null) {
			mCaptionSetting.mEdgeColor = getColorFromCapColor(raisedColor, raisedOpacity);
		}

		mCaptionSetting.mEdgeStyle = isRaise ? NexCaptionSetting.EdgeStyle.RAISED : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
	
	/**
	 * \brief This method indicates whether or not CEA 608 closed captions are "depressed".
	 * 
	 * If CEA 608 closed captions are "depressed", they should be displayed as if pressed into the video display slightly.
	 * 
	 * If depressed closed captions are to be displayed in a user-defined color, see the method \c setDepressedWithColor instead.
	 * 
	 * \param isDepressed \c TRUE if closed captions are depressed, \c FALSE if they are not.
	 * 
	 * \see NexCaptionRenderer.setDepressedWithColor
	 *
	 */
	public void setDepressed(boolean isDepressed)
	{
		if (isDepressed)
		{
			resetEdgeEffect();
		}

		setDepressed = isDepressed;

		mCaptionSetting.mEdgeStyle = isDepressed ? NexCaptionSetting.EdgeStyle.DEPRESSED : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
	
	
	/**
	 * \brief This method indicates whether or not CEA 608 closed captions should be displayed as if "depressed" (in a set font color).
	 * 
	 * If CEA 608 closed captions are "depressed", they should be displayed as if they are pressed into the video display slightly,
	 * and the color of the depressed portion of the caption text ("sunken" into the display) can be set by the user.
	 * 
	 * \param isDepressed  \c TRUE if closed captions are depressed, \c FALSE if not.
	 * \param depressedColor  The color of the depressed part of the text set by the user, or \c null to use the default color.
	 * \param depressedOpacity  The opacity of the depressed part of the text as an integer, from 0 (transparent) to 255 (fully opaque).  
	 *
	 * \since version 6.20
	 */
	public void setDepressedWithColor(boolean isDepressed, CaptionColor depressedColor, int depressedOpacity)
	{
		if (isDepressed)
		{
			resetEdgeEffect();
		}

		setDepressed = isDepressed;
        setDepressedColor = depressedColor;
        setDepressedOpacity = depressedOpacity;

		if(depressedColor != null) {
			mCaptionSetting.mEdgeColor = getColorFromCapColor(depressedColor, depressedOpacity);
		}

		mCaptionSetting.mEdgeStyle = isDepressed ? NexCaptionSetting.EdgeStyle.DEPRESSED : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
	/**
	 * \brief This method indicates whether or not CEA 608 closed captions should be displayed with a uniform effect.
	 * 
	 * If CEA 608 closed captions are displayed with the uniform effect, they should be displayed with a uniform black outline.
	 * 
	 * \param isUniform \c TRUE if all caption characters should have a uniform black outline; otherwise \c FALSE.
	 *
         * \since version 6.7
	 */
	public void setUniform(boolean isUniform)
	{
		if (isUniform)
		{
			resetEdgeEffect();
		}

		setUniform = isUniform;

		mCaptionSetting.mEdgeStyle = isUniform ? NexCaptionSetting.EdgeStyle.UNIFORM : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
	
	//kyle.jung_131008_Added attribute menu for Emboss Mask Filter
	/**  
	 * \brief This method sets the specular level of the Emboss Mask filter used when a user sets CEA 608 captions to be 'Raised' or 'Depressed' in the UI.
	 * 
	 * \param specular  The specular level of the Emboss Mask filter.
	 * 
	 * \since version 6.4.1
	 *
	 * \@deprecated This API is deprecated.
	 */
	public void setEmbossSpecular(float specular)
	{

	}
	/** 
	 * \brief This method sets the blur radius of the Emboss Mask filter used when a user sets CEA 608 captions to be 'Raised' or 'Depressed' in the UI.
	 * 
	 * \param radius  The blur radius of the Emboss Mask filter.
	 *
	 * \since version 6.4.1
	 *
	 * \@deprecated This API is deprecated.
	 */
	public void setEmbossBlurRadius(float radius)
	{

	}
	
	/**
	 * \brief  This method turns off underlining in CEA 608 closed captions.
	 * 
	 * CEA 608 closed captions can include an underline attribute to indicate that the captions should
	 * be underlined in order to fully meet the specification.  However, it may at times be desireable 
	 * to turn off this underlining, and this method makes this possible.
	 * 
	 * \param disableUnderline	If set to \c TRUE, the renderer will ignore underline attributes even in content with
	 * 							closed captions that have underline attributes.
	 * 							If set to \c FALSE, the renderer will underline captions if the content's caption
	 * 							information includes an underline attribute.
	 * 
	 * \since version 5.12
	 * **/
	public void setDisableUnderline(boolean disableUnderline)
	{
		m_isOffUnderline = disableUnderline;

		mCaptionSetting.mUnderLine = disableUnderline ? NexCaptionSetting.StringStyle.REMOVE : NexCaptionSetting.StringStyle.DEFAULT;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
	
	/**
	 * \brief This method turns off flashing in CEA 608 closed captions.
	 * 
	 * CEA 608 closed captions can include a "flashing" attribute to indicate that the
	 * captions should be displayed flashing in order to fully meet the specifications.  However, it may
	 * at times be is desireable to turn off this "flashing" display, and this method makes this possible.
	 * 
	 * \param disableFlashing	If set to \c TRUE, the renderer will ignore flashing attributes even in content with
	 * 							closed captions that have flashing attributes.
	 * 							If set to \c FALSE, the renderer will display flashing captions if the content's
	 * 							caption information includes a flashing attribute.
	 * 
	 * \since version 5.12
	 * **/
	public void setDisableFlashing(boolean disableFlashing)
	{
		m_isOffFlashing = disableFlashing;
	}
	
    /**
     * \brief This method lets users control the CEA 608 closed caption font size in NexPlayer&trade;&nbsp;from the application UI.
     *
     * \warning This method should only be used when a \c customFont has been set by calling the method \c NexCaptionRenderer.setFonts AND the captions exceed the size of the caption character cell. 	
     *
     * \param scale  The value to scale the caption text when resizing it, as a \c float.
     * \param textSize  The size of the desired caption text, in points.
     *
     * \see NexCaptionRenderer.setFonts
     * \since version 6.5.2
     */
    public void setTextScaleFactor(float scale, float textSize)
	{
		m_fontScale = scale;
		m_fontSize = textSize;

		mCaptionSetting.mFontSize = textSize;
		mCaptionSetting.mFontScale = scale;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
        
        
   /**
    * \brief  This method sets the default font size for CEA 608 closed captions. 
    * 
    * When the \c setMode API is not in use or the parameter \c mode is set to 0, only the text size of the CEA 608 closed captions will change while
    * the render area of each letter will be fixed (as defined by the CEA 608 specifications). The caption text will then be cut off 
    * when the font size is increased to sizes larger than this fixed rendering area. 
    * To avoid this potential issue and to ensure that caption text can be easily read, the parameter \c mode in the \c setMode API should be
    * set to 1 so that both caption text size and the caption rendering area will be resized together. 
    * 
    * The parameter \c dTextSize can be set to a percentage of the default caption render area (as defined in the CEA 608 specifications).
    * The default value is 70 which means that text will fill 70 percent of the caption rendering area. 
    *
    * \warning  Note that clear text render is not guaranteed when the default font size is set to be too big or too small and may result in caption text being cut off or overlapping. 
    *
    * \param dTextSize  The default text size to set, as a percentage of the caption render area (as a \c float). 
    *
    * \see NexCaptionRender.setMode
    * \since version 6.21
    */
    public void setDefaultTextSize(float dTextSize)
    {
		mCaptionSetting.mFontSize = dTextSize;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
    }
    
 
   /**
    * \brief This method sets the spacing between lines of CEA 608 closed captions. 
    *
    * This method will only work when the parameter \c mode in \c setMode is set to 1, so that CEA 608 closed captions 
    * can be rendered with additional font settings.  The parameter \c spaceRate can be set to a percentage of the default space size between caption lines in CEA 608 closed captions.
    * Also note that a maximum of 15 lines of CEA 608 closed caption text can appear on the screen at one time, which may become relevant when caption font size is increased.  
    *
    * \warning  Note that clear text render is not guaranteed when the value of \c spaceRate is set to be too big or too small and may result in text being cut off or overlapping.
    * 
    * \param spaceRate  The amount of space to leave between lines of text in CEA 608 closed captions, as a percentage of the default size of space between caption lines (as a \c float). 
    *
    * \see NexCaptionRenderer.setMode
    * \see NexCaptionRenderer.setDefaultTextSize
    * 
    * \since version 6.21
    */
    public void setLineSpacingRate(float spaceRate)
    {
    	m_lineSpaceRate = spaceRate;
    }
    /**  \brief This method gets the horizontal position of the CEA 608 caption rendering area, compared to the video rendering area.
     *  
     *  This method can be used along with \c setM_border_X to reposition where CEA 608 closed captions will appear relative content.
     *  
     *  \returns The horizontal indent of the captions, as an integer.
     *
     *  \see NexCaptionRenderer.setM_border_X
     *  \since version 6.6
     */
	public int getM_border_X() {
		return m_border_X;
	}
	/**  \brief This method sets the horizontal position of the CEA 608 caption rendering area, compared to the video rendering area.
     *  
     *  This method can be used to reposition where CEA 608 closed captions will appear relative to the playing content.
     *
     *  \param m_border_X  The horizontal indent of the caption window, as an integer, where 0 is the left of the video rendering area.
     *
     *  \since version 6.6
     */
	public void setM_border_X(int m_border_X) {
		this.m_border_X = m_border_X;

		mCaptionPainter.setRenderingArea(new Rect(m_X + m_border_X, m_Y + m_border_Y, m_X + m_Width - m_border_X, m_Y + m_Height - m_border_Y), 1);
	}
	
	/**  \brief This method gets the horizontal position of the CEA 608 caption rendering area, compared to the video rendering area.
     *  
     *  This method can be used along with \c setM_border_Y to reposition where CEA 608 closed captions will appear relative to the playing content.
     *  
     *  \returns The vertical indent of the captions, as an integer.
     *
     *  \see NexCaptionRenderer.setM_border_Y
     *  \since version 6.6
     */
	public int getM_border_Y() {
		return m_border_Y;
	}
	/**  \brief This method sets the vertical position of the CEA 608 caption rendering area, compared to the video rendering area.
     *  
     *  This method can be used to reposition where CEA 608 closed captions will appear relative to the playing content.
     *
     *  \param m_border_Y  The vertical indent of the caption window, as an integer, where 0 is the top of the video rendering area.
     *
     *  \since version 6.6
     */
	public void setM_border_Y(int m_border_Y) {
		this.m_border_Y = m_border_Y;

		mCaptionPainter.setRenderingArea(new Rect(m_X + m_border_X, m_Y + m_border_Y, m_X + m_Width - m_border_X, m_Y + m_Height - m_border_Y), 1);
	}
	
	/**
	 * \brief This method changes the font size of CEA 608 closed captions when displayed in content.
	 * 
	 * It allows the font size of CEA 608 closed captions to be selected by users in the UI.
	 * 
	 * \param sizeRate  The change in font size as a percentage, from 50 to 200 percent of the original caption text size.
	 * 
	 * \since version 6.8
	 */
	public void changeTextSize(int sizeRate)
	{
		if(sizeRate >= 50 && sizeRate <= 200)
			m_sizeRate = (float)sizeRate;
		else
			m_sizeRate = 100.0f;

		mCaptionSetting.mFontScale = m_sizeRate / 100.f;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
	
	private class CC608_Info
	{
		int row;
		int col;
		String string;
		Paint paint = new Paint();
		Rect rect = new Rect();
	}

	private int getColorFromCapColor(CaptionColor cColor, int cOpacity)
	{
		int setColor = cColor.getFGColor();
		return Color.argb(cOpacity, Color.red(setColor), Color.green(setColor), Color.blue(setColor));
	}

	Rect m_eachBoxRect = new Rect();
	Rect m_textBoundsRect = new Rect();
	Rect m_drawTextRect = new Rect();
	Rect m_drawLineRect = new Rect();
	ArrayList<Rect> windowRectList = new ArrayList<Rect>();
	ArrayList<CC608_Info> captionInfo_default = new ArrayList<CC608_Info>();
	Paint.FontMetricsInt fmi = new Paint.FontMetricsInt();
	CC608_Info info = new CC608_Info();
	Paint winPaint = new Paint();
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		if( m_paint == null ) {
			m_paint = new Paint();
		}
		Paint p = m_paint;
		p.reset();
		p.setAntiAlias(true);
		
		if( m_Caption == null )
			return;
		
		windowRectList.clear();
		
		int winLeft = -1;
		int winTop = -1;
		int winRight = -1;
		int winBottom = -1;		
		
		boolean isSameWindow = false;
		boolean isEmptyLine;
		boolean isAboveLineExists = false;
		
		int width = m_Width - (m_border_X*2);
		int height = m_Height - (m_border_Y*2);
		int block_width = width/32;
		int block_height = height/16;
		
		boolean isFlash = false;
		long uptime = System.currentTimeMillis();
		boolean flashStateOn = (uptime % 400 < 200);
		
		long rollupTime = m_Caption.getRollUpElapsedTime();
		int rollUpProgress = (int)Math.min(13,rollupTime/33);
		int redrawTime = 0;
		int rollUpRows = m_Caption.getRollUpNumRows();
		int rollUpBase = m_Caption.getRollUpBaseRow();
		
		int row;
		int rollUpOffset;
		boolean bSavedState;

		if(m_renderMode == RENDER_MODE_BASIC)
		{
			CaptionColor bgColor;
			captionInfo_default.clear();
			
			for(int rowcount=-1;rowcount<15;rowcount++)
			{
				row = rowcount;
				
				if( m_Caption.getCaptionMode() == CaptionMode.RollUp && rollUpRows > 0 && (row==-1 || (row > rollUpBase - rollUpRows && row <= rollUpBase)) ) 
				{
					row = row==-1?NexClosedCaption.ROLLED_OUT_ROW:row;
					rollUpOffset = block_height - (block_height * rollUpProgress / 13 );
					canvas.save();
					int originx = m_border_X + m_X;
					int originy = (rollUpBase-rollUpRows+1)*block_height + m_border_Y + m_Y;
					canvas.clipRect(originx, originy, originx+width, originy + (rollUpRows * block_height));
					bSavedState = true;
				}
				else if( row == -1 ) 
				{
					continue;
				}
				else
				{
					bSavedState = false;
					rollUpOffset = 0;
				}
				
				isEmptyLine = true;
				
				for(int col=0;col<32;col++)
				{						
					m_eachBoxRect.left = (block_width*(col))+m_border_X + m_X;
					m_eachBoxRect.right = (block_width*(col+1))+m_border_X + m_X;
					m_eachBoxRect.top = (block_height*(row))+m_border_Y + m_Y + rollUpOffset;
					m_eachBoxRect.bottom = (block_height*(row+1))+m_border_Y + m_Y + rollUpOffset;
					
					char charCode = m_Caption.getCharCode(row, col);
					if( charCode != 0 ) {
						
						isEmptyLine = false;
						isAboveLineExists = true;

						NexLog.d(TAG, "CEA608 caption charcode (" + charCode + ") on row: " + row + " / col: " + col);
						
						if(!isSameWindow)
						{
							isSameWindow = true;
							if(winLeft == -1 || m_eachBoxRect.left < winLeft)
								winLeft = m_eachBoxRect.left - 10;
							
							if(winTop == -1)
								winTop = m_eachBoxRect.top - 10;
							
							if(col == 31)
							{
								winRight = m_eachBoxRect.right + 10;
								winBottom = m_eachBoxRect.bottom + 10;
								isSameWindow = false;
							}
						}
						
						else
						{
							if(winLeft == -1 || m_eachBoxRect.left < winLeft)
								winLeft = m_eachBoxRect.left - 10;
							
							if(col == 31)
							{
								winRight = m_eachBoxRect.right + 10;
								winBottom = m_eachBoxRect.bottom + 10;
								isSameWindow = false;
							}
						}

						p.setTextScaleX(0.9f);
						
						if(m_fontSize > 0.0)
						{
							if(m_fontScale > 0.0)
								p.setTextSize(m_fontSize*m_fontScale);
							else
								p.setTextSize(m_fontSize);
						}
						else
						{
							if((m_eachBoxRect.width()*2) >= m_eachBoxRect.height())
								p.setTextSize(m_eachBoxRect.height()*4/5);
							else
								p.setTextSize(m_eachBoxRect.width());
						}

						p.setTextSkewX(0.0f);
						if(m_Caption.isItalic(row, col))
						{
							if(m_Caption.isLarge(row, col))
							{
								p.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
								p.setTextSkewX(-0.25f);
							}
							else
							{
								p.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
								p.setTextSkewX(-0.25f);
							}
						}
						else
						{
							if(m_Caption.isLarge(row, col))							
								p.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
							else
								p.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
						}
						
						if(m_typeNormal != null)
						{
							p.setTypeface(m_typeNormal);
						}
						if(m_typeBold != null)
						{
							p.setFakeBoldText(true);
							p.setTypeface(m_typeBold);
						}
						if(m_typeItalic != null)
						{
							p.setTextSkewX(-0.25f);
							p.setTypeface(m_typeItalic);
						}
						if(m_typeBoldItalic != null)
						{
							p.setFakeBoldText(true);
							p.setTextSkewX(-0.25f);
							p.setTypeface(m_typeBoldItalic);
						}

						if( m_Caption.isFlashing(row, col) && !m_isOffFlashing) {
							isFlash = true;
							if( !flashStateOn )
								continue;
						}

						if(setBold)
						{
							p.setTypeface(m_typeBold);
						}

						CC608_Info info = new CC608_Info();
						info.col = col;
						info.row = row;
						info.string = new String(new char[]{charCode});
						info.rect.set(m_eachBoxRect);
						info.paint.set(p);// = p;
						
						captionInfo_default.add(info);
					}
					else
					{
						if(isSameWindow)
						{
							isSameWindow = false;
							if(winRight < m_eachBoxRect.right)
								winRight = m_eachBoxRect.right - block_width + 10;
							
							winBottom = m_eachBoxRect.bottom + 10;

						}
					}
				}

				if((isAboveLineExists && isEmptyLine) || (isAboveLineExists && row == 14))
				{
					//get window area					
					Rect winRectItem = new Rect();						
					winRectItem.set(winLeft, winTop, winRight, winBottom);
					windowRectList.add(winRectItem);
					
					winLeft = -1;
					winTop = -1;
					winRight = -1;
					winBottom = -1;
						
					isAboveLineExists = false;
				}
				
				if( bSavedState )
					canvas.restore();
			}
			
			for(int i = 0; i < windowRectList.size(); i++)
			{	
				if(setWindowColor != null)
				{
					int set_color = setWindowColor.getFGColor();
					int setColor = Color.argb(setWindowOpacity, Color.red(set_color), Color.green(set_color), Color.blue(set_color));
					winPaint.setColor(setColor);
					canvas.drawRect(windowRectList.get(i), winPaint);
				}
			}
			
			for(int a = 0; a < captionInfo_default.size(); a++)
			{
				CC608_Info cc_info = captionInfo_default.get(a);
				cc_info.paint.set(captionInfo_default.get(a).paint);
				NexLog.d(TAG, "GET CAPTION FROM ARRAYLIST");
				bgColor = m_Caption.getBGColor(cc_info.row, cc_info.col);
				final CaptionColor fgColor = m_Caption.getFGColor(cc_info.row, cc_info.col);

				cc_info.paint.setStyle(Style.FILL);
				if(setBgColor != null)
				{
					int set_color = setBgColor.getFGColor();
					int setColor = Color.argb(setBgOpacity, Color.red(set_color), Color.green(set_color), Color.blue(set_color));
					cc_info.paint.setColor(setColor);
					canvas.drawRect(cc_info.rect, cc_info.paint);
				}
				else if(CaptionColor.TRANSPARENT != bgColor)
				{
					int set_color = bgColor.getBGColor();
					int setColor = Color.argb(255, Color.red(set_color), Color.green(set_color), Color.blue(set_color));
					cc_info.paint.setColor(setColor);
					canvas.drawRect(cc_info.rect, cc_info.paint);
				}
				
				if(m_Caption.isUnderline(cc_info.row, cc_info.col) && !m_isOffUnderline)
				{
					if(fgColor != null) {
						cc_info.paint.setColor(fgColor.getFGColor());
					}
					if(setFgColor != null)
					{
						int set_color = setFgColor.getFGColor();
						int setColor = Color.argb(setFgOpacity, Color.red(set_color), Color.green(set_color), Color.blue(set_color));
						cc_info.paint.setColor(setColor);
					}

					m_drawLineRect.set(cc_info.rect);
					m_drawLineRect.bottom -= 5;

					canvas.drawLine(m_drawLineRect.left, m_drawLineRect.bottom, m_drawLineRect.right, m_drawLineRect.bottom, cc_info.paint);
				}
				
				//set fg
				if(fgColor != null) {
					cc_info.paint.setColor(fgColor.getFGColor());
				}
				if(setFgColor != null)
				{
					int fg_color = Color.argb(setFgOpacity, Color.red(setFgColor.getFGColor()), Color.green(setFgColor.getFGColor()), Color.blue(setFgColor.getFGColor()));
					cc_info.paint.setColor(fg_color);
				}
				
				cc_info.paint.setStyle(Paint.Style.FILL);

				setDrawTextRect(cc_info.paint, cc_info.rect, m_drawTextRect, cc_info.string);
				applyEdgeEffect(canvas, cc_info.paint, m_drawTextRect, cc_info.string);
				drawTextOnCanvas(canvas, cc_info.paint, m_drawTextRect, cc_info.string);
			}
		}
		else if(m_renderMode == RENDER_MODE_CUSTOM) {
			mCaptionPainter.draw(canvas);
		}
		
		if( isFlash ) {
			int flashTime = 200-(int)(uptime%200);
			if( flashTime < redrawTime || redrawTime == 0 )
				redrawTime = flashTime;
		}
		
		if( rollUpRows>0 && rollUpProgress < 13 && (redrawTime==0||redrawTime<33) )
			redrawTime=33;
		
		if( redrawTime > 0 ) {
			m_handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					NexCaptionRenderer.this.invalidate();
				}
			}, redrawTime);
		}
	}

	private int getStringHeight(Paint paint, float lineSpaceRate) { return (int)((Math.abs(paint.descent()) + Math.abs(paint.ascent()) * lineSpaceRate/100.0f)); }

	private void setDrawTextRect(Paint paint, Rect inRect, Rect outRect, String sText)
	{
		paint.getTextBounds(sText, 0, 1, m_textBoundsRect);
		paint.getFontMetricsInt(fmi);

		outRect.set(inRect);
		outRect.left =  inRect.left + (inRect.width() - m_textBoundsRect.width())/2;
		outRect.top = inRect.top + (inRect.height() - fmi.ascent)/2 - fmi.descent/2;
	}

	private void drawTextOnCanvas(Canvas canvas, Paint paint, Rect rect, String sText)
	{
		if (RENDER_MODE_BASIC == m_renderMode)
		{
			canvas.drawText(sText, 0, sText.length(), rect.left, rect.top, paint);
		}
		else
		{
			int offsetYPos = 0;
			for (String line : sText.split("\n"))
			{
				offsetYPos += getStringHeight(paint, m_lineSpaceRate);
				int rectTop = rect.top - (int)paint.descent() + offsetYPos;
				canvas.drawText(line, 0, line.length(), rect.left, rectTop, paint);
			}
		}
	}

	private void applyEdgeEffect(Canvas canvas, Paint paint, Rect rect, String sText)
	{
		float[] EdgeEffectParams = {0.0f, 0.0f, 0.0f};
		int edgeColor = Color.BLACK;

		if (setShadow)
		{
			if(null != setShadowColor)
			{
				edgeColor = getColorFromCapColor(setShadowColor, setShadowOpacity);
			}

			EdgeEffectParams = NexClosedCaption.DEFAULT_SHADOW_PARAM;
		}
		else if (setRaise)
		{
			if (null != setRaisedColor)
			{
				edgeColor = getColorFromCapColor(setRaisedColor, setRaisedOpacity);
			}

			EdgeEffectParams = NexClosedCaption.DEFAULT_RAISED_PARAM;
		}
		else if (setDepressed)
		{
			if(null != setDepressedColor)
			{
				edgeColor = getColorFromCapColor(setDepressedColor, setDepressedOpacity);
			}

			EdgeEffectParams = NexClosedCaption.DEFAULT_DEPRESSED_PARAM;
		}
		else if(setUniform || (setStrokeColor != null))
		{
			drawTextOnCanvas(canvas, paint, rect, sText);

			float nStrokeWidth = 1.0f;
			paint.setStyle(Paint.Style.STROKE);
			if (null != setStrokeColor)
			{
				edgeColor = getColorFromCapColor(setStrokeColor, 255);
				nStrokeWidth = setStrokeWidth;
			}

			paint.setColor(edgeColor);
			paint.setStrokeWidth(nStrokeWidth);
			edgeColor = Color.TRANSPARENT;
		}
		else
		{
			edgeColor = Color.TRANSPARENT;
		}

		paint.setShadowLayer(EdgeEffectParams[0], EdgeEffectParams[1], EdgeEffectParams[2], edgeColor);
	}
}

class NexCEA608CaptionExtractor extends NexCaptionExtractor implements CaptionExtractorCommonInterface {
	private int mCurRow = 0;
	private int mY = 0;
	private int mX = 0;

	private Rect mRenderingArea = new Rect();

	NexCEA608CaptionExtractor() {
	}

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

	}

	@Override
	void setRenderingArea(Rect renderingArea, float scale) {
		mRenderingArea = renderingArea;
	}

	ArrayList<NexCaptionRenderingAttribute> extract(NexClosedCaption data) {
		ArrayList<NexCaptionRenderingAttribute> renderingAttributeList = null;

		if (null != data) {
			renderingAttributeList = new ArrayList<NexCaptionRenderingAttribute>();
			ArrayList<NodeString> strings;

			mCurRow = 0;

			do {
				NexCaptionRenderingAttribute renderingAttribute = new NexCaptionRenderingAttribute();
				renderingAttribute.id = renderingAttribute.hashCode();

				strings = getNodeString(data);

				if (null != strings) {
					renderingAttribute.mStartTime = data.getCaptionTime()[0];
					renderingAttribute.mEndTime = data.getCaptionTime()[0];
					renderingAttribute.mRemoveTime = Integer.MAX_VALUE;
					renderingAttribute.mWindowSize = 100;
					renderingAttribute.mStrings = strings;
                    renderingAttribute.mRelativeFontSize = getRelativeFontSize(mRenderingArea, getFontSize(mRenderingArea));
					renderingAttribute.mCaptionSettings = getCaptionSettings();

					renderingAttributeList.add(renderingAttribute);
				}
			} while (null != strings);

			if (renderingAttributeList.isEmpty()) {
				NexCaptionRenderingAttribute renderingAttribute = new NexCaptionRenderingAttribute();
				renderingAttribute.mRemoveTime = Integer.MAX_VALUE;
				renderingAttributeList.add(renderingAttribute);
			}
		}

		return renderingAttributeList;
	}

	private NexCaptionWindowRect makeRelativePosition(Rect renderingArea) {
		NexCaptionWindowRect windowRect = new NexCaptionWindowRect();

		float x = renderingArea.width() / DEFAULT_HORIZONTAL_CELL * mX;
		float y = renderingArea.height() / DEFAULT_VERTICAL_CELL * mY;

		windowRect.xPercent = (int)(x / (float)renderingArea.width() * 100f);
		windowRect.yPercent = (int)(y / (float)renderingArea.height() * 100f);

		return windowRect;
	}

	private NexCaptionSetting getCaptionSettings() {
		NexCaptionSetting captionSettings = new NexCaptionSetting();

        captionSettings.mFontSize = getFontSize(mRenderingArea);
		captionSettings.mGravity = Gravity.START;
		captionSettings.mRelativeWindowRect = makeRelativePosition(mRenderingArea);

		int mDefaultPaddingValue = 10;
		captionSettings.mPaddingLeft = captionSettings.mPaddingTop = captionSettings.mPaddingRight = captionSettings.mPaddingBottom = mDefaultPaddingValue;

		return captionSettings;
	}

	private ArrayList<NodeString> getNodeString(NexClosedCaption data) {
		ArrayList<NodeString> nodeStrings = null;
		NodeString nodeString = null;

		mX = mY = 0;

		if (null != data) {
			boolean shouldLinePeed = false;
			int prevFirstCharInColumn = 0;
			for (int row = mCurRow; row < 15; ++row) {
				boolean emptyLine = true;
				int firstCharInColumn = 0;
				for (int col = 0; col < 32; ++col) {
					char c = data.getCharCode(row, col);
					if (0 != c) {
						if (null == nodeStrings) {
							nodeStrings = new ArrayList<NodeString>();
							mY = row;
						}

						if (0 == firstCharInColumn) {
							firstCharInColumn = col;
						}

						if (shouldLinePeed) {
							if (nodeString != null) {
								nodeString.mString += "\r\n";
							}

							shouldLinePeed = false;
						}

						if (null == nodeString) {
							nodeString = makeNodeString(data, row, col);
						} else {
							if (shouldCreateNodeString(nodeString, data, row, col)) {
								nodeStrings.add(nodeString);
								nodeString = makeNodeString(data, row, col);
							} else {
								nodeString.mString += Character.toString(c);
							}
						}

						emptyLine = false;
					}
				}

				mCurRow = row + 1;

				if (!emptyLine) {
					shouldLinePeed = true;

					if (0 == mX || firstCharInColumn <= prevFirstCharInColumn) {
						mX = prevFirstCharInColumn = firstCharInColumn;
					} else {
						prevFirstCharInColumn = firstCharInColumn;
					}

				} else {
					if (null != nodeString) {
						break;
					}
				}
			}

			if (null != nodeString) {
				nodeStrings.add(nodeString);
			}
		}

		return nodeStrings;
	}

	private NodeString makeNodeString(NexClosedCaption data, int row, int col) {
		NodeString nodeString = new NodeString();
		nodeString.mString = Character.toString(data.getCharCode(row, col));
		nodeString.mBold = data.isLarge(row, col);
		nodeString.mItalic = data.isItalic(row, col);
		nodeString.mUnderLine = data.isUnderline(row, col);
		nodeString.mFontColor = replaceMappedFontColors(data.getFGColor(row, col).getFGColor());
		nodeString.mBackgroundColor = data.getBGColor(row, col).getFGColor();
		return nodeString;
	}

	private boolean shouldCreateNodeString(NodeString nodeString, NexClosedCaption data, int row, int col) {
		boolean shouldCreateNode = false;
		if (nodeString.mUnderLine != data.isUnderline(row, col)) {
			shouldCreateNode = true;
		} else if (nodeString.mBold != data.isLarge(row, col)) {
			shouldCreateNode = true;
		} else if (nodeString.mItalic != data.isItalic(row, col)) {
			shouldCreateNode = true;
		} else if (nodeString.mFontColor != replaceMappedFontColors(data.getFGColor(row, col).getFGColor())) {
			shouldCreateNode = true;
		} else if (nodeString.mBackgroundColor != data.getBGColor(row, col).getFGColor()) {
			shouldCreateNode = true;
		}

		return shouldCreateNode;
	}
}
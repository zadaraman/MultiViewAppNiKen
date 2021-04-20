package com.nexstreaming.nexplayerengine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.nexstreaming.nexplayerengine.NexClosedCaption.CaptionColor;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * \brief  This class defines the renderer view for CFF and 3GPP timed text subtitles in content and displays them.
 *
 * In order for NexPlayer&trade;&nbsp;to display CFF or 3GPP timed text, a separate Caption Renderer view must be created
 * with the NexCaptionRendererForTimedText class.
 *
 * In particular, in order to use NexCaptionRendererForTimedText, care must be taken to do the following:
 *
 * -# <b>Pass Video Size Information</b>:  Since the NexCaptionRendererForTimedText view is overlaid on the video display
 * in an application, information about the video display must be passed to the caption renderer for timed text to be properly displayed.
 * This means that when the video output size in the application UI is set or changes (including the when the video surface is first
 * created), NexCaptionRendererForTimedText should also be notified.  To do so, the following two methods should be called:
 *     -  \link NexCaptionRendererForTimedText.setScaleRatio setScaleRatio(float scale)\endlink: When the video is scaled
 *     up (for example to fit-screen or full-screen), pass the scale ratio to NexCaptionRendererForTimedText with this method.
 *     -  \link NexCaptionRendererForTimedText.setVideoSizeInformation setVideoSizeInformation(int videoWidth, int videoHeight, int surfaceWidth,
 *     int surfaceHeight, int left, int top)\endlink:  To fit the text render area within the video, NexCaptionRendererForTimedText
 *     also needs to know the video size and position information provided by calling this method.
 * -# <b>Pass Timed Text Data to the Renderer</b>: Whenever timed text is updated, the new timed text data must be passed to NexCaptionRendererForTimedText.
 * To do this:
 *     -# When calling \link NexPlayer.IListener#onTextRenderRender onTextRenderRender\endlink, the text type must be checked
 *     by calling \link NexClosedCaption.getTextType \endlink.
 *     -# If that method returns TEXT_TYPE_TTML_TIMEDTEXT for CFF timed text or TEXT_TYPE_3GPP_TIMEDTEXT for 3GPP timed text, pass
 *     a NexClosedCaption object with the new timed text data to NexCaptionRendererForTimedText with the
 *     \link NexCaptionRendererForTimedText.setData setData(NexClosedCaption data)\endlink method.
 *     -# Finally, call the \link NexCaptionRendererForTimedText.invalidate\endlink method when updating the captions.
 * -# <b>Clear Timed Text on the Screen</b>:  Whenever timed text must be cleared from the screen (for example when seeking or stopping
 * content), calling \link NexCaptionRendererForTimedText.clear clear\endlink and \link NexCaptionRendererForTimedText.invalidate invalidate\endlink
 * will clear any existing text from the device screen.
 *
 * This class replaces the NexCaptionRendererFor3GPPTT in earlier versions of the NexPlayer&trade;&nbsp;SDK.
 *
 * To display CEA 608 closed captions however, please use \link NexCaptionRenderer\endlink.
 *
 * \since version 6.0
 */
public class NexCaptionRendererForTimedText extends View {

	private Context m_Context;
	private int m_width = 0;

	private int m_x = 0;
	private int m_y = 0;
	private int m_videoWidth = 0;
	private int m_videoHeight = 0;

	private int m_windowMarginLeft = 10;
	private int m_windowMarginTop = 10;
	private int m_windowMarginRight = 10;
	private int m_windowMarginBottom = 10;

	private Rect forceBox = null;
    private Rect m_3gppBox = null;

	private NexClosedCaption m_caption = null;
	private Paint m_paint = null;

	private String m_str = null;
	private SpannableString m_ss = null;
	private TextPaint m_textPaint = null;
	private LinearLayout m_TextLayout = null;

	private Rect m_regionRect = null;

	private short[] m_styleStart = null;
	private short[] m_styleEnd = null;
	private int[] m_fontColor = null;
	private int[] m_fontSize = null;
	private boolean[] m_isBold = null;
	private boolean[] m_isUnderline = null;
	private boolean[] m_isItalic = null;

	private boolean m_isFlash = false;
	private Handler m_handler = new Handler();
	private int redrawTime = 0;
	private ForegroundColorSpan[] m_BlinkColorSpan = null;
	private int[] m_blinkStartOffset= null;
	private int[] m_blinkEndOffset= null;

	private float m_scale = -1.0f;

	private int m_styleRecord_Count = 0;
	private int m_charBytes = 1;//default = 1.

    //kyle.jung_130924_make formatting menu for Timed text
	private boolean m_bIsBoldOptionSet = false;

    private boolean m_bIsRaisedStyleSet = false;
    private boolean m_bIsDepressedStyleSet = false;
    private boolean m_bIsUniformStyleSet = false;
    private boolean m_bIsShadowOptionSet = false;

    private CaptionColor m_FGColor = null;
    private CaptionColor m_BGColor = null;
    private CaptionColor m_WindowColor = null;
    private CaptionColor m_StrokeColor = null;

    private int m_ShadowColor = NexClosedCaption.DEFAULT_SHADOW_COLOR;
    private int m_RaisedColor = Color.BLACK;
    private int m_DepressedColor = Color.BLACK;

    private float m_iFontSizeRatio = 100f;
    private float m_defaultFontSize = 0f;

    private int m_FGOpacity = 0;
    private int m_BGOpacity = 0;
    private int m_WindowOpacity = 0;
    private int m_StrokeOpacity = 0;

    private float m_StrokeWidth = 0.0f;

    private Typeface m_typeItalic = null;
	private Typeface m_typeBoldItalic = null;
	private Typeface m_typeBold = null;
	private Typeface m_typeNormal = null;

	private final String LOG_TAG = "NexCaptionRenderer_TimedText";

	NexCaptionPainter mCaptionPainter = null;
	NexCaptionSetting mCaptionSetting = null;

    /** 
     * \brief  This is the constructor for the 3GPP and TTML timed text caption renderer.
     *
     * \param context	The handle for the player.
     *
     * \since version 6.0
     */
	public NexCaptionRendererForTimedText(Context context) {
		super(context);
		m_Context = context;
		mCaptionPainter = new NexCaptionPainter(m_Context, NexContentInformation.NEX_TEXT_TTML);
		mCaptionSetting = new NexCaptionSetting();
		WrapSetLayerType();
	}
/** \brief This is an alternative constructor for the Timed Text caption renderer.
 * \warning If the caption view is to be used in Android xml, this constructor must be used.
 *
 * \param context  The handle for the player.
 * \param attrs   The set of attributes associated with the view.
 *
 * \since version 6.6
 */
	public NexCaptionRendererForTimedText(Context context, AttributeSet attrs) {
		super(context,attrs);
		m_Context = context;
		mCaptionPainter = new NexCaptionPainter(m_Context, NexContentInformation.NEX_TEXT_TTML);
		mCaptionSetting = new NexCaptionSetting();
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
	 * \brief This property clears the screen when using the timed text caption renderer.
	 */
	public void clear()
	{
		NexLog.d(LOG_TAG, "Call clear()");
		if (null != m_regionRect) {
			m_regionRect.setEmpty();;
		}

		m_str = "";
		m_ss = null;

		mCaptionPainter.clear();
	}

	/**
     * \brief This property specifies the size and position of the video surface on the device's screen for timed text.
     *
     * This information is used by the renderer when video is scaled to a different size so that the rendering area
     * can also be scaled proportionally.
	 *
     * \param videoWidth	The width of the displayed video.
     * \param videoHeight	The height of the displayed video.
     * \param surfaceWidth	The width of the surface where video is displayed.
     * \param surfaceHeight	The height of the surface where video is displayed.
     * \param left			The horizontal (X) position of the top left hand corner of the video rendering area.
     * \param top			The vertical (Y) position of the top left hand corner of the video rendering area.
	 *
	 */
	public void setVideoSizeInformation(int videoWidth, int videoHeight, int surfaceWidth, int surfaceHeight, int left, int top)
	{
		NexLog.d(LOG_TAG, "Call Render Area. w : " + videoWidth + " h : " + videoHeight + " left : " + left + " top : " + top);
		m_videoWidth = videoWidth;
		m_videoHeight = videoHeight;
		m_width = surfaceWidth;
		m_x = left;
		m_y = top;

		mCaptionPainter.setRenderingArea(new Rect(left, top, left + m_videoWidth, top + m_videoHeight), m_scale);
	}

	/**
	 * \brief This property overrides the text box and sets it to be able to be moved anywhere desired.
	 *
	 * \param textBox   Override to anywhere desired.
	 */
	public void setTextBoxOnLayout(Rect textBox)
	{
		forceBox = textBox;
	}
	/**
	 * \brief This property describes the video's scale ratio.
	 *
	 * When a displayed video changes in size on the screen (as for example when a video is displayed full screen),
	 * this ratio should be used to scale the text renderer accordingly as well.
	 *
	 * \param scale	The video's scale ratio.
	 */
	public void setScaleRatio(float scale)
	{
		m_scale = scale;
		NexLog.d(LOG_TAG, "Set Scale : " + m_scale);

		mCaptionPainter.setRenderingArea(new Rect(m_x, m_y, m_x + m_videoWidth, m_y + m_videoHeight), m_scale);
	}


        /** \brief This method sets the foreground (text) color of 3GPP/TTML timed text captions.
	 *
	 * For a full list of colors , please refer to \ref NexClosedCaption::CaptionColor.
	 *
	 * \param foreground  The foreground color, or \c null to use the color from the original caption data.
	 * \param fgOpacity  The foreground opacity, from 0 (transparent) to 255 (fully opaque).
         *
         * \since version 6.7
	 */
	public void setFGCaptionColor(CaptionColor foreground, int fgOpacity)
	{
		m_FGColor = foreground;
		m_FGOpacity = fgOpacity;

		mCaptionSetting.mFontColor = getColorFromCapColor(m_FGColor, m_FGOpacity);
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}

	/** \brief This sets the background color of 3GPP/TTML captions.
	 *
	 * For a full list of colors , please refer to \ref NexClosedCaption::CaptionColor.
	 *
	 * \param background  The background color, or \c null to use the color from the original caption data.
	 * \param bgOpacity  The background opacity, from 0 (transparent) to 255 (fully opaque).
	 */
	public void setBGCaptionColor(CaptionColor background, int bgOpacity)
	{
		m_BGColor = background;
		m_BGOpacity = bgOpacity;

		mCaptionSetting.mBackgroundColor = getColorFromCapColor(m_BGColor, m_BGOpacity);
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}

	/** \brief This sets the 3GPP/TTML caption renderer stroke color and width.
	 *
	 * For a full list of colors, please refer to \ref NexClosedCaption::CaptionColor. The stroke line width is in
	 * pixels. Anti-aliasing is supported, so fractions of a pixel are allowed.
	 *
	 * \param strokeColor  The stroke color, or \c null to use the color from the original caption data.
	 * \param strokeOpacity  The stroke opacity, from 0 (transparent) to 255 (fully opaque).
	 * \param strokeWidth  The stroke width in pixels.
	 */
	public void setCaptionStroke(CaptionColor strokeColor, int strokeOpacity, float strokeWidth)
	{
		if (null != strokeColor)
		{
			resetEdgeEffect();
		}
		m_StrokeColor = strokeColor;
        m_StrokeOpacity = strokeOpacity;
        m_StrokeWidth = strokeWidth;

		mCaptionSetting.mEdgeColor = getColorFromCapColor(m_StrokeColor, m_StrokeOpacity);
		mCaptionSetting.mEdgeStyle = NexCaptionSetting.EdgeStyle.UNIFORM;
		mCaptionSetting.mEdgeWidth = strokeWidth;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}

	/** \brief This method sets the window color of 3GPP/TTML captions.
	 *
	 * This is similar to setting a background color behind the caption text.
	 *
	 * For a full list of colors , please refer to \ref NexClosedCaption::CaptionColor.
	 *
	 * \param windowColor  The window color, or \c null to use the color from the original caption data.
	 * \param windowOpacity  The window color opacity, from 0 (transparent) to 255 (fully opaque).
	 */
	public void setCaptionWindowColor(CaptionColor windowColor, int windowOpacity)
	{
		m_WindowColor = windowColor;
		m_WindowOpacity = windowOpacity;

		mCaptionSetting.mWindowColor = getColorFromCapColor(m_WindowColor, m_WindowOpacity);
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}


	 /**
        * \brief    This method sets a margin around caption text to the edge of the caption window, for timed text.
        *
        * \param left      Sets a margin from the left edge of the caption window to caption text, as an \c int.
        * \param top       Sets a margin from the top of the caption window to caption text, as an \c int.
        * \param right     Sets a margin from the right edge of the caption window to caption text, as an \c int.
        * \param bottom    Sets a margin from the bottom of the caption window to caption text, as an \c int.
        *
        * \since version 6.15
        */
	public void setWindowMargin(int left, int top, int right, int bottom)
	{
		m_windowMarginLeft = left;
		m_windowMarginTop = top;
		m_windowMarginRight = right;
		m_windowMarginBottom = bottom;

		mCaptionSetting.mPaddingLeft = left;
		mCaptionSetting.mPaddingRight = right;
		mCaptionSetting.mPaddingTop = top;
		mCaptionSetting.mPaddingBottom = bottom;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}

      /**
       * \brief This method sets the default size of text for 3GPP/TTML timed text captions.
       *
       * To change the font size for example based on a user selection, call the method \c setFontSize instead.
       *
       * \param size The default size of the caption text to set, in dip, as a float.
       *
       * \see setFontSize(float sizePercentage)
       *
       * \version 6.19
       */
	public void setDefaultTextSize(float size){

		m_defaultFontSize = size;
	}

	/** \brief This method changes the font size of 3GPP/TTML timed text.
	 *
	 * The size of text font can be changed from 50 to 200 percent of the original caption font size.
	 *
	 * \param sizePercentage The percentage change in font size, as a float.
	 */
	public void setFontSize(float sizePercentage)
	{
		if(sizePercentage >= 50 && sizePercentage <= 200)
			m_iFontSizeRatio = sizePercentage;
		else
			m_iFontSizeRatio = 100f;

		mCaptionSetting.mFontScale = m_iFontSizeRatio / 100.f;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}


	/** \brief This method controls whether timed text captions are displayed in bold text.
	 *
	 * Caption data includes attributes such as <b>bold</b> and <i>italics</i>.
	 *
	 * Normally, the caption renderer displays each character
	 * in normal, <b>bold</b> or <i>italics</i> based on the attributes included in the caption data.
	 *
	 * However in some cases (such as for
	 * users with visual impairment) it may be desirable to force the use of <b>bold</b> text.
	 *
	 * By enabling this option, the
	 * bold attributes in the caption data are ignored and a bold font is used for all characters.
	 *
	 * \param isBold   Set this to \c TRUE to force bold text, or \c FALSE to use the bold attribute in the original captions.
	 *
	 */
	public void setBold(boolean isBold) {
		m_bIsBoldOptionSet= isBold;

		mCaptionSetting.mBold = isBold ? NexCaptionSetting.StringStyle.APPLY : NexCaptionSetting.StringStyle.REMOVE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}


	/** \brief This method sets whether or not 3GPP/TTML timed text should be displayed with a shadow.
	 *
	 * \param isShadow  Set this to \c TRUE to force text to be displayed with a shadow, or \c FALSE for no shadow.
	 *
	 */
	public void setShadow(boolean isShadow)
	{
		if(isShadow)
			resetEdgeEffect();

		m_bIsShadowOptionSet = isShadow;

		mCaptionSetting.mEdgeStyle = isShadow ? NexCaptionSetting.EdgeStyle.DROP_SHADOW : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}

       /**
	* \brief  This method sets whether or not timed text captions should be displayed with a colored shadow.
	*
	*
	* \param isShadow  Set this to \c TRUE to force text to be displayed with a shadow, or \c FALSE for no shadow.
	* \param shadowColor  The shadow color, or \c null to use the color from the original caption data.
	* \param shadowOpacity  The shadow opacity as an integer, from 0 (transparent) to 255 (fully opaque).
        *
        * \since version 6.18
	*/
	public void setShadowWithColor(boolean isShadow, CaptionColor shadowColor, int shadowOpacity)
	{
  		if(isShadow)
  			resetEdgeEffect();

  		m_bIsShadowOptionSet = isShadow;

  		if(shadowColor != null)
  		{
  			m_ShadowColor = getColorFromCapColor(shadowColor, shadowOpacity);
			mCaptionSetting.mEdgeColor = m_ShadowColor;
  		}

		mCaptionSetting.mEdgeStyle = isShadow ? NexCaptionSetting.EdgeStyle.DROP_SHADOW : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}

	/** \brief  This method sets the fonts to be used for the 3GPP/TTML timed text captions.
     *
     * Four typefaces may be specified for different combinations of bold and
     * italic. The  caption renderer will select the appropriate typeface from
     * among these based on the CEA-608 captions being displayed.
	 *
     * For best results, specify all four typefaces. Any typeface can be set
     * to \c null, in which case the system default typeface will be used.
	 *
     * \param normType          Typeface to be used for captions that are neither bold  nor italic.
     * \param boldType          Typeface to be used for bold 3GPP/TTML captions.
     * \param italicType        Typeface to be used for italic 3GPP/TTML captions.
     * \param boldItalicType    Typeface to be used for 3GPP/TTML captions that are both and italic.
	 */
	public void setFonts(Typeface normType, Typeface boldType, Typeface italicType, Typeface boldItalicType)
	{
		if(normType!= null) {
			m_typeNormal = normType;
		}

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
	 * \brief This method indicates whether or not 3GPP/TTML timed text captions should be displayed as if "raised".
	 *
	 * If 3GPP/TTML timed text captions are "raised", they should be displayed as if rising above the video display slightly,
	 * for example as if they were embossed.
	 *
	 * To have the raised timed text be displayed in a user-defined color, see the \c setRaiseWithColor method instead.
	 *
	 * \param isRaise  \c TRUE if the 3GPP/TTML timed text captions are raised, \c FALSE if they are not.
	 *
         * \since version 6.7
         * 
         * \see NexCaptionRendererForTimedText.setRaiseWithColor
         */
	public void setRaise(boolean isRaise)
	{
		if(isRaise)
			resetEdgeEffect();

		m_bIsRaisedStyleSet = isRaise;
		mCaptionSetting.mEdgeStyle = isRaise ? NexCaptionSetting.EdgeStyle.RAISED : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
	

	/**
	 * \brief This method indicates whether or not 3GPP/TTML timed text should be displayed as if "raised" (in a set font color).
	 * 
	 * If 3GPP/TTML timed text is "raised", it should be displayed as if the text rises from the video display slightly,
	 * and the color of the raised part of the text can be set by the user.
	 * 
	 * \param isRaise  \c TRUE if the timed text is raised, \c FALSE if not.
	 * \param raisedColor  The color of the raised part set by the user, or \c null to use the default color.
	 * \param raisedOpacity  The opacity of the raised part as an integer, from 0 (transparent) to 255 (fully opaque).  
	 *
	 * \since version 6.20
	 */
	 	public void setRaiseWithColor(boolean isRaise, CaptionColor raisedColor, int raisedOpacity)
	{
		if(isRaise)
			resetEdgeEffect();

		m_bIsRaisedStyleSet = isRaise;
		
		if(raisedColor != null)
  		{
  			m_RaisedColor = getColorFromCapColor(raisedColor, raisedOpacity);
			mCaptionSetting.mEdgeColor = m_RaisedColor;
  		}

		mCaptionSetting.mEdgeStyle = isRaise ? NexCaptionSetting.EdgeStyle.RAISED : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}

	/**
	 * \brief This method indicates whether or not 3GPP/TTML timed text captions should be displayed as if "depressed".
	 *
	 * If 3GPP/TTML timed text captions are "depressed", they should be displayed as if pressed into the video display slightly.
	 *
	 * If depressed timed text is to be displayed in a user-defined color, see the method \c setDepressedWithColor instead.
	 *
	 * \param isDepressed \c TRUE if the 3GPP/TTML timed text captions should be displayed as if depressed, otherwise \c FALSE.
	 *
         * \since version 6.7
         * 
         * \see NexCaptionRendererForTimedText.setDepressedWithColor
	 */
	public void setDepressed(boolean isDepressed)
	{
		if(isDepressed)
			resetEdgeEffect();

		m_bIsDepressedStyleSet = isDepressed;
		mCaptionSetting.mEdgeStyle = isDepressed ? NexCaptionSetting.EdgeStyle.DEPRESSED : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
	
	
	/**
	 * \brief This method indicates whether or not 3GPP/TTML timed text should be displayed as if "depressed" (in a set font color).
	 * 
	 * If 3GPP/TTML timed text is "depressed", it should be displayed as if the text is pressed into the video display slightly,
	 * and the color of the depressed part of the text can be set by the user.
	 * 
	 * \param isDepressed  \c TRUE if the timed text is depressed, \c FALSE if not.
	 * \param depColor     The color of the depressed part set by the user, or \c null to use the default color.
	 * \param depOpacity   The opacity of the depressed part as an integer, from 0 (transparent) to 255 (fully opaque).  
	 *
	 * \since version 6.20
	 */
	public void setDepressedWithColor(boolean isDepressed, CaptionColor depColor, int depOpacity)
	{
		if(isDepressed)
			resetEdgeEffect();

		m_bIsDepressedStyleSet = isDepressed;
		
		if(depColor != null)
  		{
  			m_DepressedColor = getColorFromCapColor(depColor, depOpacity);
			mCaptionSetting.mEdgeColor = m_DepressedColor;
  		}

		mCaptionSetting.mEdgeStyle = isDepressed ? NexCaptionSetting.EdgeStyle.DEPRESSED : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}

	/**
	 * \brief This method indicates whether or not 3GPP/TTML timed text captions should be displayed "uniformly".
	 *
	 * If 3GPP/TTML timed text captions are displayed "uniformly", they will have a uniform black outline around each character.
	 *
	 * \param isDepressed \c TRUE if 3GPP/TTML timed text captions should be displayed "uniformly", \c FALSE if they should not.
	 *
	 */
	public void setUniform(boolean isUniform)
	{
		if(isUniform)
			resetEdgeEffect();

		m_bIsUniformStyleSet = isUniform;
		mCaptionSetting.mEdgeStyle = isUniform ? NexCaptionSetting.EdgeStyle.UNIFORM : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}


    /**
     * \brief This method resets the edge effects on timed text captions.
     *
     * Possible edge effects includes setShadow, setCaptionStroke, setRaise, and setDepressed.
     *
     * \since version 6.18
     *
     */
	public void resetEdgeEffect()
	{
		m_bIsShadowOptionSet = false;
		m_ShadowColor = NexClosedCaption.DEFAULT_SHADOW_COLOR;
		m_RaisedColor = Color.BLACK;
		m_DepressedColor = Color.BLACK;
		m_bIsRaisedStyleSet = false;
		m_bIsDepressedStyleSet = false;
		m_bIsUniformStyleSet = false;
		m_StrokeColor = null;
		m_StrokeOpacity = 0;
		m_StrokeWidth = 0.0f;

		mCaptionSetting.mEdgeStyle = NexCaptionSetting.EdgeStyle.DEFAULT;
		mCaptionSetting.mEdgeColor = NexCaptionSetting.DEFAULT;
		mCaptionSetting.mEdgeWidth = NexCaptionSetting.DEFAULT;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}


    /** \brief This method initializes the style attributes of timed text captions that may be set by a user,
     * 	including the colors of the text, background, and caption window as well as the edge style and the font size.
     *
     * This API does not effect the default caption style attributes of specific streaming content.
     *
     * \since version 6.17
     */
	public void initCaptionStyle()
	{
		m_FGColor = null;
		m_BGColor = null;
		m_WindowColor = null;
		m_StrokeColor = null;

		m_FGOpacity = 0;
		m_BGOpacity = 0;
		m_WindowOpacity = 0;
		m_StrokeOpacity = 0;

		resetEdgeEffect();

		m_iFontSizeRatio = 100.0f;

		m_typeItalic = null;
		m_typeBoldItalic = null;
		m_typeBold = null;
		m_typeNormal = null;

		mCaptionSetting.init();
		mCaptionPainter.setUserCaptionSettings(null);
	}


	/**
	 * \brief This method sets the specular level of the Emboss Mask filter used when a user sets 3GPP/TTML timed text to be 'Raised' or 'Depressed' in the UI.
	 *
	 * \param specular  The specular level of the Emboss Mask filter.
	 *
	 */
	public void setEmbossSpecular(float specular)
	{
	}

	/**
	 * \brief This method sets the blur radius of the Emboss Mask filter used when a user sets 3GPP/TTML timed text to be 'Raised' or 'Depressed' in the UI.
	 *
	 * \param radius  The blur radius of the Emboss Mask filter.
	 *
	 */
	public void setEmbossBlurRadius(float radius)
	{
	}

	/**
	 * \brief  This method gets the scale ratio of the displayed video.
	 *
	 * The scale ratio should be used to scale the timed text renderer whenever the video display changes
	 * size.
	 *
	 * \returns  The video's scale ratio as a float.
	 *
	 * \since version 6.0
	 */
	public float getScaleRatio()
	{
		return m_scale;
	}

	private int getColorFromCapColor(CaptionColor cColor, int cOpacity)
	{
		int setColor = cColor.getFGColor();
		return Color.argb(cOpacity, Color.red(setColor), Color.green(setColor), Color.blue(setColor));
	}

	/**
	 * \brief This property specifies the 3GPP or CFF timed text data to the renderer.
	 *
	 * \param data  The timed text data as a NexClosedCaption object.
	 */
	public void setData(NexClosedCaption data)
	{
		NexLog.d(LOG_TAG, "try SetData");
		m_caption = data;

		if(data.getTextType() == NexClosedCaption.TEXT_TYPE_3GPP_TIMEDTEXT)
		{
			AbsoluteSizeSpan fontsizeSpan = null;

			byte[] string = m_caption.getTextDataFor3GPPTT();
			String strEncoding = "UTF-8";
			//check encoding types
			try
			{
				if(string[0] == (byte)0xFE && string[1] == (byte)0xFF)
				{
					strEncoding = "UTF-16";
				}
				else if(string [0] == (byte)0xFF && string[1] == (byte)0xFE)
				{
					strEncoding = "UTF-16";
				}
				else if(string[0] == (byte)0xEF && string[1] == (byte)0xBB && string[2] == (byte)0xBF)
				{
					strEncoding = "UTF-8";
				}
				else
				{
					strEncoding = "UTF-8";
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			try {
				m_str = new String(m_caption.getTextDataFor3GPPTT(), 0, m_caption.getTextDataFor3GPPTT().length, strEncoding);
				NexLog.d(LOG_TAG, "SetData String - " + m_str);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			m_ss = new SpannableString(m_str);
			m_textPaint = new TextPaint();
			NexCaptionTextView captionText = new NexCaptionTimedTextView(m_Context);

			if(m_caption.getTextStyle() != null)
			{
				NexLog.d(LOG_TAG, "Style Set.");
				m_styleRecord_Count = m_caption.getTextStyle().getCount();
				m_styleStart = new short[m_styleRecord_Count];
				m_styleEnd = new short[m_styleRecord_Count];
				m_fontColor = new int[m_styleRecord_Count];
				m_fontSize = new int[m_styleRecord_Count];
				m_isBold = new boolean[m_styleRecord_Count];
				m_isUnderline = new boolean[m_styleRecord_Count];
				m_isItalic = new boolean[m_styleRecord_Count];
			}
			if(m_styleRecord_Count != 0)
			{
				NexLog.d(LOG_TAG, "Style Count ." + m_styleRecord_Count);
				for(int i=0;i<m_styleRecord_Count;i++)
				{
					m_styleStart[i] = (short) (m_caption.getTextStyle().getStyleEntry(i).getStartChar() / m_charBytes);
					m_styleEnd[i] = (short) (m_caption.getTextStyle().getStyleEntry(i).getEndChar() / m_charBytes);
					if(m_styleStart[i] == 0 && m_styleEnd[i] == 0 && m_str.length() > 0)
					{
						//apply whole string.
						m_styleEnd[i] = (short) (m_str.length());
					}
					m_fontColor[i] = m_caption.getTextStyle().getStyleEntry(i).getFontColor();

					if(m_FGColor != null)	//kyle.jung_130926_set color on attribute menu
					{
						m_fontColor[i] = m_FGColor.getFGColor();
					}

					NexLog.d(LOG_TAG, "style start : " + m_styleStart[i] + " end : " + m_styleEnd[i] + " " + m_fontColor[i]);
					m_fontSize[i] = (int)(m_caption.getTextStyle().getStyleEntry(i).getFontSize()*(m_scale==0?1.0:m_scale));
					m_fontSize[i] = (int)((m_iFontSizeRatio/100.0) * m_fontSize[i]);
					NexLog.d(LOG_TAG, "3GPP size style size : " + m_fontSize[i] + " / m_scale: " + m_scale);

					m_isBold[i] = m_caption.getTextStyle().getStyleEntry(i).getBold();
					if(m_bIsBoldOptionSet)
						m_isBold[i] = true;

					m_isUnderline[i] = m_caption.getTextStyle().getStyleEntry(i).getUnderline();
					m_isItalic[i] = m_caption.getTextStyle().getStyleEntry(i).getItalic();
				}
			}

			if(m_styleRecord_Count > 0)
			{
				for(int i=0;i<m_styleRecord_Count;i++)
				{
					if(m_str.length() >= m_styleEnd[i])
					{
						ForegroundColorSpan fgColorSpan = new ForegroundColorSpan(m_fontColor[i]);
						((NexCaptionTimedTextView) captionText).setFGColorByPosition(m_fontColor[i], m_styleStart[i], m_styleEnd[i]);
						if(m_defaultFontSize > 0)
						{
							NexLog.d(LOG_TAG, "3gpp m_defaultFontSize = " + m_defaultFontSize);
							float pxSize = m_defaultFontSize * getContext().getResources().getDisplayMetrics().density;
							m_fontSize[i] = (int) pxSize;
							NexLog.d(LOG_TAG, "3gpp m_defaultFontSize px = " + m_fontSize[i]);
						}
						m_textPaint.setTextSize((float)m_fontSize[i]);

						if(m_isUnderline[i])
						{
							StyleSpan sSpan = new StyleSpan(android.graphics.Typeface.ITALIC);
							m_ss.setSpan(sSpan, m_styleStart[i], m_styleEnd[i], Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
						}
						if(m_isBold[i])
						{
							StyleSpan sSpan = new StyleSpan(android.graphics.Typeface.BOLD);
							m_ss.setSpan(sSpan, m_styleStart[i], m_styleEnd[i], Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
						}
						if(m_isUnderline[i])
						{
							UnderlineSpan ulSpan = new UnderlineSpan();
							m_ss.setSpan(ulSpan, m_styleStart[i], m_styleEnd[i], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						}
						if(m_fontSize[i] != 0)
						{
							AbsoluteSizeSpan aSpan = new AbsoluteSizeSpan(m_fontSize[i]);
							m_ss.setSpan(aSpan, m_styleStart[i], m_styleEnd[i], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							fontsizeSpan = aSpan;
						}
						NexLog.d(LOG_TAG, "Set. Color : " + m_fontColor[i]);
					}
				}
			}
			if(m_caption.getTextBlink() != null)
			{
				NexClosedCaption.TextBlink[] blink = m_caption.getTextBlink();
				int length = blink.length;
				m_BlinkColorSpan = new ForegroundColorSpan[length];
				m_blinkStartOffset = new int[length];
				m_blinkEndOffset = new int[length];
				for(int i=0;i<length;i++)
				{
					m_BlinkColorSpan[i] =new ForegroundColorSpan(m_caption.getBackgroundColorFor3GPPTT());
					m_blinkStartOffset[i] = blink[i].getStartOffset();
					m_blinkEndOffset[i] = blink[i].getEndOffset();
					if(m_blinkEndOffset[i] > m_str.length())
						m_blinkEndOffset[i] = m_str.length();
				}
				m_isFlash = true;
			}
			else
			{
				m_isFlash = false;
			}

			Rect tBox = m_caption.getTextBox();
			NexLog.d(LOG_TAG, "default TBox : " + tBox);

			int [] regionCoord = m_caption.getTextboxCoordinatesFor3GPPTT();
			if(regionCoord[2] == 0 && regionCoord[3] == 0)//how can I handle this coordinates set to 0?
			{
				float pxSize = m_fontSize[0] * getContext().getResources().getDisplayMetrics().density;

				if(m_defaultFontSize > 0)
					pxSize = m_defaultFontSize * getContext().getResources().getDisplayMetrics().density;

				int fontSize = (int)(pxSize/m_scale);

				SpannableString ss = new SpannableString(m_str);
				ss.setSpan(new AbsoluteSizeSpan(fontSize), 0, m_str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				TextPaint textPaint = new TextPaint();
				StaticLayout tempLayout = new StaticLayout(ss, textPaint, getWidth(), Alignment.ALIGN_NORMAL, 1, 0, false);
				m_ss.removeSpan(fontsizeSpan);
				m_ss.setSpan(new AbsoluteSizeSpan(fontSize), 0, m_str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

				NexLog.d(LOG_TAG, "Layout Height : " + tempLayout.getHeight() + "vid H " + m_videoHeight + "fsize : " + fontSize);
				regionCoord[0] = tBox.left;
				regionCoord[1] = (int)(m_videoHeight*0.9) - tempLayout.getHeight();//tBox.top + (tempLayout.getHeight() + (int)(m_videoHeight*0.9));
				regionCoord[2] = tBox.right - tBox.left;
				regionCoord[3] = ((int)(m_videoHeight*0.9) - regionCoord[1]);

				m_regionRect = new Rect();
				m_regionRect.left = regionCoord[0];
				m_regionRect.top = regionCoord[1];
				m_regionRect.right = regionCoord[0] + regionCoord[2];
				m_regionRect.bottom = regionCoord[1] + regionCoord[3];
			}
			else
			{
				m_regionRect = new Rect();
				m_regionRect.left = regionCoord[0] + tBox.left;
				m_regionRect.top = regionCoord[1] + tBox.top;
				m_regionRect.right = m_regionRect.left + tBox.right;
				m_regionRect.bottom = m_regionRect.top + tBox.bottom;
			}
			NexLog.d(LOG_TAG, "RECT : " + m_regionRect + " / x:" + m_x + " / y:" + m_y);

			if(forceBox != null )
			{
				m_regionRect.left = forceBox.left;
				m_regionRect.right = forceBox.right;
				m_regionRect.top = forceBox.top;
				m_regionRect.bottom = forceBox.bottom;
				NexLog.d(LOG_TAG, "Forced RECT : " + m_regionRect);
			}
			m_textPaint.setColor(Color.BLACK);

			captionText.setText(m_ss);

			if(m_typeNormal != null)
				captionText.setTypeface(m_typeNormal);
			if(m_typeBold != null)
				captionText.setTypeface(m_typeBold, Typeface.BOLD);
			if(m_typeItalic != null)
				captionText.setTypeface(m_typeItalic, Typeface.ITALIC);
			if(m_typeBoldItalic != null)
				captionText.setTypeface(m_typeBoldItalic, Typeface.BOLD_ITALIC);

			int backgroundColor = m_caption.getBackgroundColorFor3GPPTT();
			if(m_BGColor != null)
			{
				backgroundColor = getColorFromCapColor(m_BGColor, m_BGOpacity);
			}

			captionText.setBackgroundColor(backgroundColor);

			if(m_FGColor != null) {
				((NexCaptionTimedTextView) captionText).setFGColorByPosition(m_FGColor, m_FGOpacity, 0, m_ss.length());
				captionText.setBaseTextColor(getColorFromCapColor(m_FGColor, m_FGOpacity));
			}

            applyEdgeEffect(captionText);

            captionText.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    			MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int maxWidth = Math.min(m_width , captionText.getMeasuredWidth());
            captionText.setMaxWidth(maxWidth);

			m_TextLayout = new LinearLayout(m_Context);
			m_TextLayout.addView(captionText);

			if(m_WindowColor != null)
			{
				m_TextLayout.setBackgroundColor(getColorFromCapColor(m_WindowColor, m_WindowOpacity));
			}

			if(m_ss.length() > 0)
			{
				m_TextLayout.setPadding(m_windowMarginLeft, m_windowMarginTop, m_windowMarginRight, m_windowMarginBottom);
				m_TextLayout.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
											MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

                NexLog.d(LOG_TAG, "TEXT LAYOUT SIZE WIDTH : " + m_TextLayout.getMeasuredWidth() + " / HEIGHT : " + m_TextLayout.getMeasuredHeight());
                //use to calculate exact text area size
                m_3gppBox = new Rect();
                m_3gppBox.set(m_regionRect.left, m_regionRect.top, m_regionRect.left+m_TextLayout.getMeasuredWidth(), m_regionRect.top+m_TextLayout.getMeasuredHeight());
                m_TextLayout.layout(m_regionRect.left, m_regionRect.top, m_regionRect.left+m_TextLayout.getMeasuredWidth(), m_regionRect.top+m_TextLayout.getMeasuredHeight());
			}
            else
            {
                m_TextLayout.layout(0, 0, 0, 0);
            }

		}
		else if(data.getTextType() == NexClosedCaption.TEXT_TYPE_TTML_TIMEDTEXT) {
			mCaptionPainter.setDataSource(data);
		}
	}

	private void applyEdgeEffect(NexCaptionTextView captionTextView) {
        captionTextView.setDropShadow(m_bIsShadowOptionSet, m_ShadowColor);
        captionTextView.setUniform(m_bIsUniformStyleSet);

        if (m_bIsRaisedStyleSet)
        {
            captionTextView.setRaised(m_bIsRaisedStyleSet, m_RaisedColor);
        }
        else if (m_bIsDepressedStyleSet)
        {
            captionTextView.setDepressed(m_bIsDepressedStyleSet, m_DepressedColor);
        }
        else if (null != m_StrokeColor)
        {
            captionTextView.setCaptionStroke(getColorFromCapColor(m_StrokeColor, m_StrokeOpacity), m_StrokeWidth);
        }
    }

	@Override
	protected void onDraw(Canvas canvas) {
		if(m_paint == null)
		{
			m_paint = new Paint();
		}
		Paint p = m_paint;
		p.reset();
		p.setAntiAlias(true);

		if( m_caption == null )
			return;

		if( m_caption.getTextType() == NexClosedCaption.TEXT_TYPE_3GPP_TIMEDTEXT)
		{
			if(null != m_ss && 0 < m_ss.length())
			{
				long uptime = System.currentTimeMillis();
				boolean flashStateOn = (uptime % 400 < 200);
				int scrollDelay = 33;

				if(m_isFlash)
				{
					if(!flashStateOn)
					{
						for(int i=0;i<m_blinkStartOffset.length;i++)
						{
							m_ss.setSpan(m_BlinkColorSpan[i], m_blinkStartOffset[i], m_blinkEndOffset[i], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						}
					}
					else
					{
						for(int i=0;i<m_blinkStartOffset.length;i++)
						{
							m_ss.removeSpan(m_BlinkColorSpan[i]);
						}
					}
				}

				canvas.save();

				//Android GB seems to not check text layout area clearly, so calculate area when setting data and clip on onDraw.
				if(m_3gppBox != null && !m_3gppBox.isEmpty() && Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
					canvas.clipRect(m_3gppBox);
				}

				m_TextLayout.draw(canvas);
				canvas.restore();

				if( m_isFlash ) {
					int flashTime = 200-(int)(uptime%200);
					if( flashTime < redrawTime || redrawTime == 0 )
						redrawTime = flashTime;
				}
				if(scrollDelay != 0 && redrawTime > scrollDelay)
					redrawTime = scrollDelay;
				if( redrawTime > 0 ) {
					m_handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							NexCaptionRendererForTimedText.this.invalidate();
						}
					}, /* uptime + */ redrawTime);
				}
			}
		}
		else if(m_caption.getTextType() == NexClosedCaption.TEXT_TYPE_TTML_TIMEDTEXT) {
			mCaptionPainter.draw(canvas);
		}
	}
}

class NexTTMLExtractor extends NexCaptionExtractor {
	private Rect mRenderingArea = new Rect(0,0,0,0);
	private float mRenderingScale = 1.0f;

	@Override
	public void setRenderingArea(Rect renderingArea, float scale) {
		mRenderingArea = renderingArea;
		mRenderingScale = scale;
	}

	@Override
	public ArrayList<NexCaptionRenderingAttribute> extract(NexClosedCaption data) {
		ArrayList<NexCaptionRenderingAttribute> list = null;
		if (null != data) {
			list = new ArrayList<NexCaptionRenderingAttribute>();
			NexClosedCaption.TTMLRenderingData ttmlRenderingData = data.ttmlRenderingData;
			NexCaptionRenderingAttribute renderingAttribute = new NexCaptionRenderingAttribute();

			renderingAttribute.mStartTime = ttmlRenderingData.startTime;
			renderingAttribute.mEndTime = ttmlRenderingData.endTime;
			renderingAttribute.mRemoveTime = ttmlRenderingData.clearTime;
			renderingAttribute.mRelativeFontSize = getRelativeFontSize(mRenderingArea, getFontSize(ttmlRenderingData.fontSize));

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				renderingAttribute.mDirection = ttmlRenderingData.direction == 0 ? View.LAYOUT_DIRECTION_LTR : View.LAYOUT_DIRECTION_RTL;
			}

			renderingAttribute.mVisibility = ttmlRenderingData.visibility == 0 ? View.VISIBLE : View.GONE;
			renderingAttribute.mWrap = ttmlRenderingData.wrapOption > 0;
			renderingAttribute.mImage = getImage(ttmlRenderingData.image, ttmlRenderingData.imageLen);
			renderingAttribute.mZOrder = ttmlRenderingData.zIndex;
			renderingAttribute.mWindowSize = 100;

			renderingAttribute.mStrings = getNodeStrings(ttmlRenderingData);

			renderingAttribute.mCaptionSettings = getCaptionSettings(ttmlRenderingData);

			renderingAttribute.id = renderingAttribute.hashCode();

			list.add(renderingAttribute);
		}

		return list;
	}

	private NexCaptionSetting getCaptionSettings(NexClosedCaption.TTMLRenderingData ttmlRenderingData) {
		NexCaptionSetting captionSettings = new NexCaptionSetting();

		float opacity = 0 == ttmlRenderingData.opacity ? 1 : ttmlRenderingData.opacity;

		captionSettings.mFontSize = getFontSize(ttmlRenderingData.fontSize);
		captionSettings.mRelativeWindowRect = makeRelativePosition(mRenderingArea, getPosition(ttmlRenderingData));
		captionSettings.mWindowColor = ttmlRenderingData.extentBackground == 0 ? NexCaptionSetting.DEFAULT : getColorFromRGBA(ttmlRenderingData.extentBackground, opacity);

		Typeface typeface = null;
		if ("sans".equals(ttmlRenderingData.fontFamily)) {
			typeface = Typeface.SANS_SERIF;
		} else if ("serif".equals(ttmlRenderingData.fontFamily)) {
			typeface = Typeface.SERIF;
		} else if ("monospace".equals(ttmlRenderingData.fontFamily)) {
			typeface = Typeface.MONOSPACE;
		} else if ("normal".equals(ttmlRenderingData.fontFamily)) {
			typeface = Typeface.DEFAULT;
		}

		captionSettings.mFontFamily = typeface;

		if (null != ttmlRenderingData.textOutline) {
			captionSettings.mEdgeStyle = NexCaptionSetting.EdgeStyle.UNIFORM;
			captionSettings.mEdgeColor = getColorFromRGBA(ttmlRenderingData.textOutline.getColor(), opacity);

			float anchorValue = ttmlRenderingData.textOutline.getType1().lengthType == NexClosedCaption.TTML_LengthType.Default ? mRenderingArea.height() / 100 : mRenderingArea.height();

			if (ttmlRenderingData.textOutline.getType1().lengthType == NexClosedCaption.TTML_LengthType.px) {
				// Commonly, px value is multiplied by scale but, it will not do that for clear visibility.
				ttmlRenderingData.textOutline.getType1().lengthType = NexClosedCaption.TTML_LengthType.Default;
				anchorValue = ttmlRenderingData.textOutline.getType1().length;
			} else if (ttmlRenderingData.textOutline.getType1().lengthType == NexClosedCaption.TTML_LengthType.percent){
				anchorValue = captionSettings.mFontSize;
			}

			captionSettings.mEdgeWidth = convertStyleLengthToPx(ttmlRenderingData.textOutline.getType1(), anchorValue);
		}

		captionSettings.mGravity = getGravity(ttmlRenderingData.textAlign, ttmlRenderingData.displayAlign);
		captionSettings.mFontScale = 1.0f;

		if (null != ttmlRenderingData.padding) {
			captionSettings.mPaddingTop = (int) convertStyleLengthToPx(ttmlRenderingData.padding[0]);
			captionSettings.mPaddingBottom = (int) convertStyleLengthToPx(ttmlRenderingData.padding[1]);
			captionSettings.mPaddingLeft = (int) convertStyleLengthToPx(ttmlRenderingData.padding[2]);
			captionSettings.mPaddingRight = (int) convertStyleLengthToPx(ttmlRenderingData.padding[3]);
		}

		return captionSettings;
	}

	private float convertStyleLengthToPx(NexClosedCaption.TTML_StyleLength styleLength, float anchorValue) {
		float target;

		if (NexClosedCaption.TTML_LengthType.percent == styleLength.lengthType) {
			target = (anchorValue * styleLength.length) / 100L;
		} else if (NexClosedCaption.TTML_LengthType.c == styleLength.lengthType) {
			target = anchorValue / DEFAULT_VERTICAL_CELL * styleLength.length;
		} else if (NexClosedCaption.TTML_LengthType.px == styleLength.lengthType) {
			target = styleLength.length * mRenderingScale;
		} else {
			target = anchorValue;
		}

		return target;
	}

	private float convertStyleLengthToPx(NexClosedCaption.TTML_StyleLength styleLength) {
		return convertStyleLengthToPx(styleLength, 1);
	}

	private ArrayList<NodeString> getNodeStrings(NexClosedCaption.TTMLRenderingData ttmlRenderingData) {
		ArrayList<NodeString> nodeStrings = null;

		ArrayList<NexClosedCaption.TTMLRenderingData.TTMLNodeData> nodes = ttmlRenderingData.nodes;

		if (null != nodes) {
			for (NexClosedCaption.TTMLRenderingData.TTMLNodeData node : nodes) {
				if (null != node.text) {
					if (null == nodeStrings) {
						nodeStrings = new ArrayList<NodeString>();
					}
					NodeString nodeString = new NodeString();

					float opacity = ttmlRenderingData.opacity == 0 ? 1 : ttmlRenderingData.opacity;

					try {
						nodeString.mString = Html.fromHtml(new String(node.text, "UTF-8").replace(" ", "&nbsp;")).toString();

					}
					catch(UnsupportedEncodingException e) {
						e.printStackTrace();
					}
                    if (node.bgColor != ttmlRenderingData.extentBackground) {
                        nodeString.mBackgroundColor = getColorFromRGBA(node.bgColor, opacity);
                    } else {
						nodeString.mBackgroundColor = NexCaptionSetting.DEFAULT;
					}

					nodeString.mFontColor = replaceMappedFontColors(getColorFromRGBA(node.fontColor, opacity));

					if (1 == node.fontWeight) {
						nodeString.mBold = true;
					}

					if (null != node.fontStyle) {
						if (NexClosedCaption.TTML_Fontstyle.Italic == node.fontStyle || NexClosedCaption.TTML_Fontstyle.Oblique == node.fontStyle) {
							nodeString.mItalic = true;
						}
					}

					setTextDecoration(nodeString, node.textDecoration);

					nodeStrings.add(nodeString);
				}
			}
		}

		return nodeStrings;
	}

	private void setTextDecoration(NodeString nodeString, int textDecoration) {
		switch (textDecoration) {
			case 0:
				nodeString.mUnderLine = false;
				nodeString.mLineThrough = false;
				nodeString.mOverLine = false;
				break;
			case 1:
				nodeString.mUnderLine = true;
				break;
			case 2:
				nodeString.mUnderLine = false;
				break;
			case 3:
				nodeString.mLineThrough = true;
				break;
			case 4:
				nodeString.mLineThrough = false;
				break;
			case 5:
				nodeString.mOverLine = true;
				break;
			case 6:
				nodeString.mOverLine = false;
				break;
		}
	}

	private NexCaptionWindowRect makeRelativePosition(Rect videoArea, RectF position) {
		NexCaptionWindowRect windowRect = new NexCaptionWindowRect();

		windowRect.xPercent = (int)((position.left - videoArea.left) / (float)videoArea.width() * 100f);
		windowRect.yPercent = (int)((position.top - videoArea.top) / (float)videoArea.height() * 100f);
		windowRect.widthPercent = (int)(position.width() / (float)videoArea.width() * 100f);
		windowRect.heightPercent = (int)(position.height() / (float)videoArea.height() * 100f);

		return windowRect;
	}

	private RectF getPosition(NexClosedCaption.TTMLRenderingData ttmlRenderingData) {
		RectF position = new RectF();

		if (null != ttmlRenderingData.origin) {
			float anchorValue = ttmlRenderingData.origin[0].lengthType == NexClosedCaption.TTML_LengthType.Default ? 0 : mRenderingArea.width();
			position.left = convertStyleLengthToPx(ttmlRenderingData.origin[0], anchorValue) + mRenderingArea.left;

			anchorValue = ttmlRenderingData.origin[1].lengthType == NexClosedCaption.TTML_LengthType.Default ? 0 : mRenderingArea.height();
			position.top = convertStyleLengthToPx(ttmlRenderingData.origin[1], anchorValue) + mRenderingArea.top;
		}

		if (null != ttmlRenderingData.extentWidth) {
			float anchorValue = ttmlRenderingData.extentWidth.lengthType == NexClosedCaption.TTML_LengthType.Default ? 0 : mRenderingArea.width();
			position.right = position.left + convertStyleLengthToPx(ttmlRenderingData.extentWidth, anchorValue);
		}

		if (null != ttmlRenderingData.extentHeight) {
			float anchorValue = ttmlRenderingData.extentHeight.lengthType == NexClosedCaption.TTML_LengthType.Default ? 0 : mRenderingArea.height();
			position.bottom = position.top + convertStyleLengthToPx(ttmlRenderingData.extentHeight, anchorValue);
		}

		return position;
	}

	private int getGravity(NexClosedCaption.TTML_TextAlign textAlign, NexClosedCaption.TTML_DisplayAlign displayAlign) {

		int gravity = Gravity.START;

		if (NexClosedCaption.TTML_TextAlign.Left == textAlign) {
			gravity = Gravity.START;
		} else if (NexClosedCaption.TTML_TextAlign.Center == textAlign) {
			gravity = Gravity.CENTER_HORIZONTAL;
		} else if (NexClosedCaption.TTML_TextAlign.Right == textAlign || NexClosedCaption.TTML_TextAlign.End == textAlign) {
			gravity = Gravity.END;
		}

		if (NexClosedCaption.TTML_DisplayAlign.Center == displayAlign) {
			gravity |= Gravity.CENTER_VERTICAL;
		} else if (NexClosedCaption.TTML_DisplayAlign.After == displayAlign) {
			gravity |= Gravity.BOTTOM;
		} else {
			gravity |= Gravity.TOP;
		}

		return gravity;
	}

	private float getFontSize(NexClosedCaption.TTML_StyleLength[] ttmlFontSize) {
		float fontSize = 0;
		if (null != ttmlFontSize) {
			float anchorValue = (mRenderingArea!=null)?mRenderingArea.height():0;
			if (NexClosedCaption.TTML_LengthType.percent == ttmlFontSize[0].lengthType) {
				anchorValue /= DEFAULT_VERTICAL_CELL;
			} else if (NexClosedCaption.TTML_LengthType.Default == ttmlFontSize[0].lengthType) {
				ttmlFontSize[0].lengthType = NexClosedCaption.TTML_LengthType.c;
				ttmlFontSize[0].length = 1;
			}

			fontSize = convertStyleLengthToPx(ttmlFontSize[0], anchorValue);
		}
		return fontSize;
	}

	private Bitmap getImage(byte[] bytes, int len) {
		Bitmap image = null;
		if (null != bytes && 0 < len) {
			image = BitmapFactory.decodeByteArray(bytes, 0, len);
		}
		return image;
	}

	private int getColorFromRGBA(int RGBA, float opacity) {
		int r = (RGBA >> 24) & 0xFF;
		int g = (RGBA >> 16) & 0xFF;
		int b = (RGBA >> 8) & 0xFF;
		int a = RGBA & 0xFF;
		a = (int) ((float) a * opacity);

		return Color.argb(a, r, g, b);
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
}

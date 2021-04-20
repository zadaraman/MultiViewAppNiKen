package com.nexstreaming.nexplayerengine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.nexstreaming.nexplayerengine.NexClosedCaption.CaptionColor;

import java.util.ArrayList;

import static com.nexstreaming.nexplayerengine.NexCaptionSetting.DEFAULT;


/**
 * \brief  This class defines the renderer view for Web Video Text Tracks (WebVTT) text tracks in HLS content and displays them.
 * 
 * In order for NexPlayer&trade;&nbsp;to display WebVTT, a separate Caption Renderer view must be created
 * with the NexCaptionRendererForWebVTT class.
 * 
 * In particular, in order to use NexCaptionRendererForWebVTT, care must be taken to do the following:
 * 
 * -# <b>Pass Video Size Information</b>:  Since the NexCaptionRendererForWebVTT view is overlaid on the video display 
 * in an application, information about the video display must be passed to the caption renderer for the WebVTT text to be properly displayed.
 * This means that when the video output size in the application UI is set or changes (including the when the video surface is first
 * created), NexCaptionRendererForWebVTT should also be notified.  To do so, the following two methods should be called:
 *     -  \link NexCaptionRendererForWebVTT.setScaleRatio setScaleRatio(float scale)\endlink: When the video is scaled
 *     up (for example to fit-screen or full-screen), pass the scale ratio to NexCaptionRendererForWebVTT with this method.
 *     -  \link NexCaptionRendererForWebVTT.setVideoSizeInformation setVideoSizeInformation(int videoWidth, int videoHeight, int surfaceWidth, 
 *     int surfaceHeight, int left, int top)\endlink:  To fit the text render area within the video, NexCaptionRendererForWebVTT 
 *     also needs to know the video size and position information provided by calling this method.
 * -# <b>Pass Caption Data to the Renderer</b>: Whenever WebVTT text is updated, the new caption data must be passed to NexCaptionRendererForWebVTT.
 * To do this:
 *     -# When calling \link NexPlayer.IListener#onTextRenderRender onTextRenderRender\endlink, the text type must be checked
 *     by calling \link NexClosedCaption.getTextType \endlink.
 *     -# If that method returns TEXT_TYPE_WebVTT for WebVTT text, pass
 *     a NexClosedCaption object with the new WebVTT text data to NexCaptionRendererForWebVTT with the 
 *     \link NexCaptionRendererForWebVTT.setData setData(NexClosedCaption data)\endlink method.
 *     -# Finally, call the \link NexCaptionRendererForWebVTT.invalidate\endlink method when updating the captions.
 * -# <b>Clear the Text on the Screen</b>:  Whenever WebVTT text must be cleared from the screen (for example when seeking or stopping
 * content), calling \link NexCaptionRendererForWebVTT.clear clear\endlink and \link NexCaptionRendererForWebVTT.invalidate invalidate\endlink
 * will clear any existing text from the device screen. 
 * 
 * 
 * To display CEA 608 closed captions, CEA 708 closed captions, or timed text (CFF or 3GPP) however, please use the relevant caption renderers,
 * namely one of \link NexCaptionRenderer\endlink, \link NexEIA708CaptionView\endlink, or \link NexCaptionRendererForTimedText\endlink.
 * 
 * \since version 6.4
 */
public class NexCaptionRendererForWebVTT extends View {
	private int m_x = 0;
	private int m_y = 0;
	private int m_videoWidth = 0;
	private int m_videoHeight = 0;

	NexCaptionPainter mCaptionPainter = null;
	NexCaptionSetting mCaptionSetting = null;

	private static final String LOG_TAG = "WEBVTT_RENDERER";


	/**
     * \brief  This is the constructor for the WebVTT caption renderer.
     *
     * \param context	The handle for the player.
     *
     * \since version 6.4
     */
	public NexCaptionRendererForWebVTT(Context context) {
		super(context);
		mCaptionPainter = new NexCaptionPainter(context, NexContentInformation.NEX_TEXT_WEBVTT);
		mCaptionSetting = new NexCaptionSetting();
		WrapSetLayerType();
	}
	/** \brief This is an alternative constructor for the WebVTT caption renderer.
 * \warning If the caption view is to be used in Android xml, this constructor must be used.
 *
 * \param context  The handle for the player.
 * \param attrs   The set of attributes associated with the view.
 *
 * \since version 6.6
 */
	public NexCaptionRendererForWebVTT(Context context,AttributeSet attrs) {
		super(context,attrs);
		mCaptionPainter = new NexCaptionPainter(context, NexContentInformation.NEX_TEXT_WEBVTT);
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
	 * \brief This property sets the captions properties in order to be rendered aligned to the center.
	 *
	 * \deprecated Use NexCaptionPainter class instead of this method.
	 */
	public void setCenterAlignment() {

		mCaptionSetting.mGravity = Gravity.CENTER;
		mCaptionSetting.mRelativeWindowRect.autoAdjustment = true;
		mCaptionSetting.mRelativeWindowRect.heightPercent = 20;
		mCaptionSetting.mRelativeWindowRect.userDefined = true;
		mCaptionSetting.mRelativeWindowRect.widthPercent = 100;
		mCaptionSetting.mRelativeWindowRect.xPercent = 0;
		mCaptionSetting.mRelativeWindowRect.yPercent = 75;

		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
    }

	/**
	 * \brief This property clears the screen when using the WebVTT caption renderer.
	 *
	 * \since version 6.4
	 */
	public void clear()
	{
		mCaptionPainter.clear();
	}

	/**
	 * \brief This property specifies the size and position of the video surface on the device's screen for WebVTT captions.
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
	 * \since version 6.4
	 */
	public synchronized void setVideoSizeInformation(int videoWidth, int videoHeight, int surfaceWidth, int surfaceHeight, int left, int top)
	{
		if(m_videoWidth != videoWidth || m_videoHeight != videoHeight || m_x != left || m_y != top) {
			m_videoWidth = videoWidth; m_videoHeight = videoHeight;
			m_x = left; m_y = top;

			NexLog.d(LOG_TAG, "setRenderingArea video width : " + videoWidth + ", video height : " + videoHeight + ", surface width : " + surfaceWidth + ", surface height" + surfaceHeight + ", left " + left + "top" + top);

			float videoScale = (float)m_videoWidth / Math.max(surfaceHeight, surfaceWidth);
			mCaptionPainter.setRenderingArea(new Rect(left, top, left + m_videoWidth, top + m_videoHeight), videoScale);
		}
	}

    /**  \brief This method checks the current time of the content and checks whether it is in the range of a WebVTT text cue (to determine if it should be rendered and displayed).
	*
        *  \warning  This module needs to be added in order to remove captions that are not 'on time'.
	*  If the current time of the playing content is not in the range of text cue's time stamp, this method returns \c false.
	*  This method can be used to ensure WebVTT text cues are displayed at the proper time while content is playing.
	*
	* \param currentTime The current time of the playing content, as an integer.
	*
	* \returns  \c FALSE if the current time is NOT within the range of the WebVTT text cue's duration range, otherwise \c TRUE.
	*
	* \since version 6.6
	*/
	public void setVideoTimeInfo(int currentTime)
	{
		NexLog.d(LOG_TAG, "Call setVideoTimeInfo " + currentTime);
	}


	/**
	 * \brief This method sets the foreground (text) color of WebVTT text cues.
	 *
	 * \param fontColor  The color to be used for caption text.
	 * \param opacity	The opacity of the text as an integer, where 0 is invisible, and 1 is fully opaque.
	 *
	 * \since version 6.6
     */
	public void setFGCaptionColor(CaptionColor fontColor, int opacity)
	{
		mCaptionSetting.mFontColor = getColorFromCapColor(fontColor, opacity);
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}

	/**
	 * \brief This method sets the background color of WebVTT text cues.
	 *
	 * \param bgColor  The color to be used for the background of text cues (the window color where caption text will appear).
	 * \param opacity	The opacity of the caption window as an integer, from 0 (fully transparent) to 255 (fully opaque).
	 *
	 * \since version 6.6
     */
	public void setBGCaptionColor(CaptionColor bgColor, int opacity)
	{
		mCaptionSetting.mBackgroundColor = getColorFromCapColor(bgColor, opacity);
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}


	/** \brief This method sets the stroke color and width of WebVTT text cues.
	 *
	 * For a full list of colors, please refer to \ref NexClosedCaption::CaptionColor.
         * The stroke line width is in
	 * pixels. Anti-aliasing is supported, so fractions of a pixel are allowed.
	 *
	 * \param strokeColor  The stroke color, or \c null to use the color from the original caption data.
	 * \param strokeOpacity The stroke opacity as an integer, from 0 (fully transparent) to 255 (fully opaque).
         * \param strokeWidth  The stroke line width as a float, in pixels.
         *
         * since version 6.7
	 */
	public void setCaptionStroke(CaptionColor strokeColor, int strokeOpacity, float strokeWidth)
	{
		if (null != strokeColor) {
			resetEdgeEffect();
		}

		mCaptionSetting.mEdgeColor = getColorFromCapColor(strokeColor, strokeOpacity);
		mCaptionSetting.mEdgeStyle = NexCaptionSetting.EdgeStyle.UNIFORM;
		mCaptionSetting.mEdgeWidth = strokeWidth;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}


	/** \brief This method sets the window color of WebVTT text cue captions.
	 *
	 * For a full list of colors , please refer to \ref NexClosedCaption::CaptionColor.
	 *
	 * \param windowColor  The window color, or \c null to use the color from the original caption data.
	 * \param windowOpacity  The window color opacity as an integer, from 0 (transparent) to 255 (fully opaque).
         *
         * \since version 6.7
         */
	public void setCaptionWindowColor(CaptionColor windowColor, int windowOpacity)
	{
		mCaptionSetting.mWindowColor = getColorFromCapColor(windowColor, windowOpacity);
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}

	/** \brief  This method sets the fonts to be used for WebVTT text cue captions.
	 *
     * Four typefaces may be specified for different combinations of <b>bold</b> and
     * <i>italics</i>. The  caption renderer will select the appropriate typeface from
     * among these based on the WebVTT text cue captions being displayed.
	 *
     * For best results, specify all four typefaces. Any typeface can be set
     * to \c null, in which case the system default typeface will be used.
	 *
     * \param normType          Typeface to be used for text cue captions that are neither bold nor italic.
     * \param boldType          Typeface to be used for <b>bold</b> WebVTT text cue captions.
     * \param italicType        Typeface to be used for WebVTT text cue captions in <i>italics</i>.
     * \param boldItalicType    Typeface to be used for WebVTT text cue captions that are both <b><i>bold and in italics</i></b>.
         *
         * \since version 6.7
	 */
	public void setFonts(Typeface normType, Typeface boldType, Typeface italicType, Typeface boldItalicType)
	{
		mCaptionSetting.mFontFamily = normType;

		if(boldType != null) {
			mCaptionSetting.mBold = NexCaptionSetting.StringStyle.APPLY;
		} else {
			mCaptionSetting.mBold = NexCaptionSetting.StringStyle.DEFAULT;
		}

		if(italicType != null) {
			mCaptionSetting.mItalic = NexCaptionSetting.StringStyle.APPLY;
		} else {
			mCaptionSetting.mBold = NexCaptionSetting.StringStyle.DEFAULT;
		}

		if(boldItalicType != null) {
			mCaptionSetting.mBold = NexCaptionSetting.StringStyle.APPLY;
			mCaptionSetting.mItalic = NexCaptionSetting.StringStyle.APPLY;
		}

		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}

   /**
    * \brief This method adds a 'drop shadow' effect to WebVTT text cue captions.
    *
    * \param isShadow \c TRUE if the captions should be displayed with a drop shadow; otherwise \c FALSE.
    *
    * \since version 6.7
    */
  	public void setShadow(boolean isShadow)
	{
  		if(isShadow) {
			resetEdgeEffect();
		}

		mCaptionSetting.mEdgeStyle = isShadow ? NexCaptionSetting.EdgeStyle.DROP_SHADOW : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}


       /**
  	* \brief  This method sets whether or not WebVTT text cue captions should be displayed with a colored shadow.
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
  		if(isShadow) {
			resetEdgeEffect();
		}

  		if(shadowColor != null)  {
			mCaptionSetting.mEdgeColor = getColorFromCapColor(shadowColor, shadowOpacity);
  		}

		mCaptionSetting.mEdgeStyle = isShadow ? NexCaptionSetting.EdgeStyle.DROP_SHADOW : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
   /**
    * \brief This method indicates whether or not WebVTT text cue captions should be displayed as if "raised".
    *
    * To have the raised text cues be displayed in a user-defined color, see the \c setRaisedWithColor method instead.
    *
    * \param isRaised \c TRUE if the text cues should be displayed as if raised; otherwise \c FALSE.
    *
    * \since version 6.7
    *
    * \see NexCaptionRendererForWebVTT.setRaisedWithColor
    */
	public void setRaised(boolean isRaised)
	{
		if(isRaised) {
			resetEdgeEffect();
		}

		mCaptionSetting.mEdgeStyle = isRaised ? NexCaptionSetting.EdgeStyle.RAISED : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}


	/**
	 * \brief This method indicates whether or not WebVTT text cue captions should be displayed as if "raised" (in a set font color).
	 *
	 * If WebVTT text cue captions are "raised", they should be displayed as if they rise from the video display slightly,
	 * and the color of the raised part of the text can be set by the user.
	 *
	 * \param isRaised  \c TRUE if the text cues are raised, \c FALSE if not.
	 * \param raisedColor  The color of the raised part set by the user, or \c null to use the default color.
	 * \param raisedOpacity  The opacity of the raised part as an integer, from 0 (transparent) to 255 (fully opaque).
	 *
	 * \since version 6.20
	 */
	public void setRaisedWithColor(boolean isRaised, CaptionColor raisedColor, int raisedOpacity)
	{
  		if(isRaised) {
			resetEdgeEffect();
		}

  		if(raisedColor != null) {
			mCaptionSetting.mEdgeColor = getColorFromCapColor(raisedColor, raisedOpacity);
  		}

		mCaptionSetting.mEdgeStyle = isRaised ? NexCaptionSetting.EdgeStyle.RAISED : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
   /**
    * \brief This method indicates whether or not WebVTT text cue captions should be displayed as if "depressed".
    *
    * If depressed text cues are to be displayed in a user-defined color, see the method \c setDepressedWithColor instead.
    *
    * \param isDepressed \c TRUE if the text cues should be displayed as if depressed into the screen; otherwise \c FALSE.
    *
    * \since version 6.7
    *
    * \see NexCaptionRendererForWebVTT.setDepressedWithColor
    */
	public void setDepressed(boolean isDepressed)
	{
		if(isDepressed) {
			resetEdgeEffect();
		}

		mCaptionSetting.mEdgeStyle = isDepressed ? NexCaptionSetting.EdgeStyle.DEPRESSED : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}


	/**
	 * \brief This method indicates whether or not WebVTT text cue captions should be displayed as if "depressed" (in a set font color).
	 *
	 * If WebVTT text cue captions are "depressed", they should be displayed as if they are pressed into the video display slightly,
	 * and the color of the depressed part of the text can be set by the user.
	 *
	 * \param isDepressed  \c TRUE if the text cues are depressed, \c FALSE if not.
	 * \param depColor  The color of the depressed part set by the user, or \c null to use the default color.
	 * \param depOpacity  The opacity of the depressed part as an integer, from 0 (transparent) to 255 (fully opaque).
	 *
	 * \since version 6.20
	 */
	public void setDepressedWithColor(boolean isDepressed, CaptionColor depColor, int depOpacity)
	{
  		if(isDepressed) {
			resetEdgeEffect();
		}

  		if(depColor != null) {
			mCaptionSetting.mEdgeColor = getColorFromCapColor(depColor, depOpacity);
  		}

		mCaptionSetting.mEdgeStyle = isDepressed ? NexCaptionSetting.EdgeStyle.DEPRESSED : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}
   /**
    * \brief This method adds a "uniform" filter to WebVTT text cue captions
    *
    * \param isUniform \c TRUE if the text cues should be displayed with a uniform black outline; otherwise \c FALSE.
    *
    * \since version 6.7
    */
	public void setUniform(boolean isUniform)
	{
		if(isUniform) {
			resetEdgeEffect();
		}

		mCaptionSetting.mEdgeStyle = isUniform ? NexCaptionSetting.EdgeStyle.UNIFORM : NexCaptionSetting.EdgeStyle.NONE;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}


    /**
     * \brief This method resets the edge effects on WebVTT text cue captions.
     *
     * Possible edge effects include setShadow, setCaptionStroke, setRaise, and setDepressed.
     *
     * \since version 6.18
     */
	public void resetEdgeEffect()
	{
		mCaptionSetting.mEdgeStyle = NexCaptionSetting.EdgeStyle.DEFAULT;
		mCaptionSetting.mEdgeColor = NexCaptionSetting.DEFAULT;
		mCaptionSetting.mEdgeWidth = NexCaptionSetting.DEFAULT;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}


    /**
     * \brief This method initializes the style attributes of WebVTT text cue captions that may be set by a user,
     * 		including the colors of the text, background, and caption window as well as the edge style and the font size.
     *
     * This API does not effect the default caption style attributes of specific streaming content.
     *
     * \since version 6.18
     */
	public void initCaptionStyle()
	{
		resetEdgeEffect();

		mCaptionSetting.init();
		mCaptionPainter.setUserCaptionSettings(null);
	}

	/**
	 * \brief This method sets the specular level of the Emboss Mask filter used when a user sets WebVTT text cues to be displayed 'Raised' or 'Depressed' in the UI.
	 *
	 * \param specular  The specular level of the Emboss Mask filter.
	 *
	 * \since version 6.6
	 */
	public void setEmbossSpecular(float specular)
	{
	}

	/**
	 * \brief This method sets the blur radius of the Emboss Mask filter used when a user sets WebVTT text cues to be displayed 'Raised' or 'Depressed' in the UI.
	 *
	 * \param radius  The blur radius of the Emboss Mask filter.
	 *
	 * \since version 6.6
	 */
	public void setEmbossBlurRadius(float radius)
	{
	}


	/**
	 * \brief  This method changes the font size of WebVTT text cue captions.
	 *
	 * To double the size of the font, the parameter \c rate should be set to 200.
	 * In contrast, to halve the size of the font, \c rate should be set to 50 (in other words, 50%).
	 *
	 * \param sizeRate The change in size of the WebVTT text cue caption font, as a percentage.
	 *
	 * \since version 6.6
	 */
	public void setTextSize(int sizeRate)
	{
		float fontSizeRate = 100.f;
		if(sizeRate >= 50 && sizeRate <= 200) {
			fontSizeRate = (float)sizeRate;
		}

		mCaptionSetting.mFontScale = fontSizeRate / 100.f;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}

       /**
	* \brief This method sets the default size of text for WebVTT text cues.
	*
	* \param size   The size of the default text in DIP units.
	*
	* \since version 6.15
	*
	*/
	public void setDefaultTextSize(float size)
	{
		mCaptionSetting.mFontSize = size;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}

       /**
        * \brief    This API sets a margin around caption text to the edge of the caption window, for WebVTT text tracks.
        *
        * \param left      Sets margin from the left edge of window to caption text, as an \c int.
        * \param top       Sets margin from the top of window to caption text, as an \c int.
        * \param right     Sets margin from the right edge of window to caption text, as an \c int.
        * \param bottom    Sets margin from the bottom of window to caption text, as an \c int.
        *
        * \since version 6.14
        */
	public void setWindowMargin(int left, int top, int right, int bottom)
	{
		mCaptionSetting.mPaddingLeft = left;
		mCaptionSetting.mPaddingRight = right;
		mCaptionSetting.mPaddingTop = top;
		mCaptionSetting.mPaddingBottom = bottom;
		mCaptionPainter.setUserCaptionSettings(mCaptionSetting);
	}

	/**
	 * \brief This method gets WebVTT text cue caption data as a String.
	 *
	 * \param caption  The WebVTT cue data as a NexClosedCaption object.
	 *
	 * \returns  The text cue data as a String.
	 *
	 * \since version 6.4
	 */
	public String getWebVTTAsString(NexClosedCaption caption)
	{
		String regex = "\\<.*?\\>";
		String textStr = "";
		String htmlText = caption.getHtmlDataForWebVTT();

		textStr = htmlText.replaceAll(regex, "");

		return textStr;
	}

	private int getColorFromCapColor(CaptionColor cColor, int cOpacity)
	{
		int setColor = cColor.getFGColor();
		return Color.argb(cOpacity, Color.red(setColor), Color.green(setColor), Color.blue(setColor));
	}

	private int getGravity(int alignType) {
		int gravity;
		switch (alignType) {
			case 1:
				gravity = Gravity.START;
				break;

			case 2:
				gravity = Gravity.CENTER;
				break;

			case 3:
				gravity = Gravity.END;
				break;

			case 4:
				gravity = Gravity.LEFT;
				break;

			case 5:
				gravity = Gravity.RIGHT;
				break;

			default:
				gravity = Gravity.START;
				break;
		}

		return gravity;
	}

	/**
	 * \brief This method specifies the WebVTT text cue caption data to the renderer.
	 *
	 * \param data  The WebVTT caption data \as a NexClosedCaption object.
	 *
	 * \see NexClosedCaption
	 *
	 * \since version 6.4
	 */
	public void setData(NexClosedCaption data)
	{
		mCaptionPainter.setDataSource(data);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		mCaptionPainter.draw(canvas);
	}
}

class NexWebVTTExtractor extends NexCaptionExtractor implements CaptionExtractorCommonInterface {
	private Rect mRenderingArea = new Rect();
	private final static String LOG_TAG = "NexWebVTTExtractor";

	@Override
	void setRenderingArea(Rect renderingArea, float scale) {
		mRenderingArea = renderingArea;
	}

	@Override
	ArrayList<NexCaptionRenderingAttribute> extract(NexClosedCaption data) {
		ArrayList<NexCaptionRenderingAttribute> list = null;

		if (null != data && null != data.webVTTRenderingData) {
			list = new ArrayList<NexCaptionRenderingAttribute>();
			NexClosedCaption.WebVTTRenderingData webVTTRenderingData = data.webVTTRenderingData;
			NexCaptionRenderingAttribute renderingAttribute = new NexCaptionRenderingAttribute();

			renderingAttribute.mStartTime =  webVTTRenderingData.startTime;
			renderingAttribute.mEndTime =  webVTTRenderingData.endTime;
			renderingAttribute.mRemoveTime =  webVTTRenderingData.clearTime;
			renderingAttribute.mStrings = getNodeString(webVTTRenderingData.nodes);
			renderingAttribute.mWindowSize = webVTTRenderingData.mSize;
			renderingAttribute.mRelativeFontSize = getRelativeFontSize(mRenderingArea, NexSubtitleExtractor.getFontSize(mRenderingArea));

			renderingAttribute.mCaptionSettings = getCaptionSettings(webVTTRenderingData);
			renderingAttribute.id = renderingAttribute.hashCode();

			list.add(renderingAttribute);
		}

		return list;
	}

	private NexCaptionSetting getCaptionSettings(NexClosedCaption.WebVTTRenderingData webVTTRenderingData) {
		NexCaptionSetting captionSettings = new NexCaptionSetting();

		captionSettings.mGravity = getGravity(webVTTRenderingData.alignType);
		captionSettings.mFontSize = NexSubtitleExtractor.getFontSize(mRenderingArea);

		captionSettings.mRelativeWindowRect = new NexCaptionWindowRect();
		captionSettings.mRelativeWindowRect.xPercent = webVTTRenderingData.mTextPosition;
		captionSettings.mRelativeWindowRect.yPercent = convertPercentToInt(webVTTRenderingData.mLinePos);

        int mDefaultPaddingValue = 10;
        captionSettings.mPaddingLeft = captionSettings.mPaddingTop = captionSettings.mPaddingRight = captionSettings.mPaddingBottom = mDefaultPaddingValue;

		return captionSettings;
	}

	private int getGravity(NexClosedCaption.WebVTT_TextAlign alignType) {
		int gravity = Gravity.START;

		if (NexClosedCaption.WebVTT_TextAlign.Middle == alignType) {
			gravity = Gravity.CENTER;
		} else if (NexClosedCaption.WebVTT_TextAlign.End == alignType || NexClosedCaption.WebVTT_TextAlign.Right == alignType) {
			gravity = Gravity.END;
		}

		return gravity;
	}

	private ArrayList<NodeString> getNodeString(ArrayList<NexClosedCaption.WebVTTRenderingData.WebVTTNodeData> nodes) {
		ArrayList<NodeString> nodeStrings = null;

		if (null != nodes) {
			nodeStrings = new ArrayList<NodeString>();
			for (NexClosedCaption.WebVTTRenderingData.WebVTTNodeData node : nodes) {
				if (null != node.text) {
					NodeString nodeString = new NodeString();
					nodeString.mString = node.text;
					nodeString.mBold = node.mBold;
					nodeString.mItalic = node.mItalic;
					nodeString.mUnderLine = node.mUnderline;
					nodeString.mFontColor = replaceMappedFontColors(Color.WHITE);
					nodeString.mBackgroundColor = Color.TRANSPARENT;

					nodeStrings.add(nodeString);
				}
			}
		}

		return nodeStrings;
	}

	private int convertPercentToInt(String percent) {
		int convertInt = DEFAULT;
		if(percent != null) {
			if (percent.contains("%")) {
				String subStr = percent.substring(0, percent.indexOf('%'));
				convertInt = Integer.parseInt(subStr);
			}
		}

		return convertInt;
	}

	@Override
	public Rect getCaptionPosition(NexCaptionWindowRect relativeRect, int viewWidth, int viewHeight) {
		Rect rect = new Rect();

		if (relativeRect.userDefined) {
			rect.left = mRenderingArea.width() * relativeRect.xPercent / 100 + mRenderingArea.left;
			rect.top = mRenderingArea.height() * relativeRect.yPercent / 100 + mRenderingArea.top;
			rect.right = mRenderingArea.width() * relativeRect.widthPercent / 100 + rect.left;
			rect.bottom = mRenderingArea.height() * relativeRect.heightPercent / 100 + rect.top;
		} else {
			rect.left = (int) (mRenderingArea.left + relativeRect.xPercent / 100.0 * (mRenderingArea.width() - viewWidth));

            rect.top = mRenderingArea.top + (mRenderingArea.height() - viewHeight);

            if (DEFAULT != relativeRect.yPercent) {
                rect.top = (int) (mRenderingArea.top + relativeRect.yPercent / 100.0 * (mRenderingArea.height() - viewHeight));
            }

			rect.right = rect.left + viewWidth;
			rect.bottom = rect.top +  viewHeight;
		}
		return rect;
	}

	@Override
	public void clear() {

	}
}
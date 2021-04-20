package com.nexstreaming.nexplayerengine;

import android.graphics.Color;
import android.graphics.Rect;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 *  \brief This class handles the subtitles and closed captions data of content.
 *
 * NexPlayer&trade;&nbsp;uses this class to handle SMI, SRT, SUB, and Smooth Streaming subtitles
 * as well as CEA 608 and CEA 708 closed captions included in HLS content, 3GPP and CFF timed text as well as WebVTT text tracks.  
 *
 * \warning  Certain methods though may only be called to handle specifically CEA 608 closed captions, CEA 708 closed captions, timed text,
 * or WebVTT text tracks, so care must be taken when implementing support for those captions.
 * 
 * Text information from standard subtitle files (SMI, SRT, SUB, and Smooth Streaming subtitles) is handled by the
 * \link getTextData \endlink method while the text of CEA 608 close captions, 3GPP timed text, and WebVTT captions must each be handled separately by, 
 * respectively, \link getString \endlink, \link getTextDataFor3GPPTT\endlink, and \link getTextDataForWebVTT\endlink.  In the case of CFF timed text (TTML), 
 * text data is handled separately by the \link getTextDataforTTML\endlink method. 
 *
 * For CEA 708 closed captions, please see
 * \link NexEIA708Struct\endlink and \link NexEIA708CaptionView\endlink.
 *
 * The instance of NexClosedCaption is delivered through the
 * \link NexPlayer.IListener.onTextRenderRender() onTextRenderRender\endlink method for regular subtitles.
 *
 * When timed text, CEA 608 closed captions, or WebVTT are implemented however, it is necessary to create and display the captions in a
 * separate caption renderer, like \link NexCaptionRendererForTimedText \endlink, \link NexCaptionRenderer \endlink, and \link NexCaptionRendererForWebVTT. .
 *   
 * For example, CEA 608 captions can be displayed one character at a time and thus the
 * position of each character must be considered.  The vertical position of each character is set by a row number, \c row, between 0 and 15, while
 * the horizontal position of the character is set by a column number, \c col, between 0 and 32.
 *
 * \see NexPlayer.IListener.onTextRenderRender, NexCaptionRenderer, NexEIA708Struct, NexEIA708CaptionView, NexCaptionRendererForTimedText, and NexCaptionRendererForWebVTT for additional details.
 * 
 *
 */
public class NexClosedCaption {

    /** This is a possible \c return value for NexClosedCaption.getTextType().  When the text type is unknown, NexPlayer&trade;&nbsp;doesn't
     *  perform any special processing on the text (the same as in the case of general subtitles.)*/
    public static final int TEXT_TYPE_UNKNOWN = 0;          // Same with general type.
    /** This is a possible \c return value for NexClosedCaption.getTextType(). This indicates general text type and requires no special
     *  processing of the text. */
    public static final int TEXT_TYPE_GENERAL = 1;          // No special process is needed.	
    
    /** This is a possible \c return value for NexClosedCaption.getTextType(). This indicates that the text type is external TTML (*.dfxp, or the Distribution Format Exchange Profile). */	
    public static final int TEXT_TYPE_EXTERNAL_TTML = 5;    // Distribution Format Exchange Profile.	
    /** This is a possible \c return value for \link NexClosedCaption.getTextType()\endlink. This format is not currently supported. */
    public static final int TEXT_TYPE_ATSCMH_CC = 0x11;     // ATSC-M/H CC. caption_channel_packet() -> CEA-708-D.
    /** This is a possible \c return value for \link NexClosedCaption.getTextType()\endlink. This format is not currently supported. */
    public static final int TEXT_TYPE_ATSCMH_BAR = 0x12;    // ATSC-M/H CC. bar_data() -> ATSC A/53: ch6.2.3.2 Bar Data. Table 6.8
    /** This is a possible \c return value for \link NexClosedCaption.getTextType()\endlink.  This format is not currently supported. */
    public static final int TEXT_TYPE_ATSCMH_AFD = 0x13;    // ATSC-M/H CC. afd_data() -> ATSC A/53: ch6.2.4 Active Format Description Data. Table 6.10
    /** This is a possible \c return value for \link NexClosedCaption.getTextType()\endlink.  It indicates the contents include CEA 608 closed captions
     *  on Data Channel 1 and processes the attributes accordingly. */
    public static final int TEXT_TYPE_NTSC_CC_CH1 = 0x14;   // CEA-608. Data Channel 1.
    /** This is a possible \c return value for \link NexClosedCaption.getTextType()\endlink.  It indicates the contents include CEA 608 closed captions
     *  on Data Channel 2 and processes the attributes accordingly. */
    public static final int TEXT_TYPE_NTSC_CC_CH2 = 0x15;   // CEA-608. Data Channel 2.
    /** This is a possible \c return value for \link NexClosedCaption.getTextType()\endlink. It indicates the contents include 3GPP timed text.*/
    public static final int TEXT_TYPE_3GPP_TIMEDTEXT = 0x20; //3GPP TS 26.245
    /** This is a possible \c return value for \link NexClosedCaption.getTextType()\endlink. It indicates the contents include CFF timed text (TTML).*/
    public static final int TEXT_TYPE_TTML_TIMEDTEXT = 0x25; //CFF TTML timed text.
    /** This is a possible \c return value for \link NexClosedCaption.getTextType()\endlink. It indicates the contents include WebVTT text tracks. */
    public static final int TEXT_TYPE_WEBVTT = 0x30; //WebVTT
	/** This is a possible \c return value for \link NexClosedCaption.getTextType()\endlink.*/
	public static final int TEXT_TYPE_SMI = 0x40;
	/** This is a possible \c return value for \link NexClosedCaption.getTextType()\endlink.*/
	public static final int TEXT_TYPE_SRT = 0x41;
	/** This is a possible \c return value for \link NexClosedCaption.getTextType()\endlink.*/
	public static final int TEXT_TYPE_SUB = 0x42;



	// Possible caption background colors for CEA 608 closed captions only.
    // Other subtitle formats don't use these values.
    //** These are possible \c return values for getCaptionColor & getbgColor*/
    private final int White = 0;
    private final int Green = 1;
    private final int Blue = 2;
    private final int Cyan = 3;
    private final int Red = 4;
    private final int Yellow = 5;
    private final int Magenta = 6;
    private final int Black = 7;
    private final int Transparent = 8;

    // Encoding Type of Text
    /** This is a possible \c return value for \link NexClosedCaption.getEncodingType()\endlink. */
    public static final int ENCODING_TYPE_ISO8859_1     = 0x0;
    /** This is a possible \c return value for \link NexClosedCaption.getEncodingType()\endlink. */
    public static final int ENCODING_TYPE_UTF16         = 0x1;
    /** This is a possible \c return value for \link NexClosedCaption.getEncodingType()\endlink. */
    public static final int ENCODING_TYPE_UTF16_BE  = 0x2;
    /** This is a possible \c return value for \link NexClosedCaption.getEncodingType()\endlink. */
    public static final int ENCODING_TYPE_UTF8      = 0x3;
    /** This is a possible \c return value for \link NexClosedCaption.getEncodingType()\endlink. */
    public static final int ENCODING_TYPE_ASCII     = 0x10;
    /** This is a possible \c return value for \link NexClosedCaption.getEncodingType()\endlink. */
    public static final int ENCODING_TYPE_UNICODE   = 0x20;
	/** This is a possible \c return value for \link NexClosedCaption.getEncodingType()\endlink. */
	public static final int ENCODING_TYPE_EUC_KR   = 0x21;
    /** This is a possible \c return value for \link NexClosedCaption.getEncodingType()\endlink. */
    public static final int ENCODING_TYPE_UNKNOWN   = 0xFFFFFFFF;

    private int     mTextType= 0;       //TextType;
    private int     mIsItalic= 0;       //Italics
    private int     mIsUnderline = 0;
    private int     mCaptionColor = 0;
    private int     mBgColor = 0;
    private int     mIsOpaque = 0;
    private int     mIsEnable = 0;  //Captions ON?
    private byte[]  mTextData = null;

	int mCaptionType = NexContentInformation.NEX_TEXT_UNKNOWN; // from NexContentInformation

    private int     mRows = 0;
    private int     mIndent = 0;

    private int     mEncodingType   = ENCODING_TYPE_ISO8859_1;

    // This is the support for the FULL display (by character) of CEA 608 closed captions
    private short[] m_attr = null; //= new short[32*16];
	private short[] m_charcode = null; //= new short[32*16];
	private CaptionMode m_captionMode;
	private int m_rollUpBaseRow;
	private int m_rollUpNumRows;
	private int m_rollUpAnimationStartTime;
	private int[] m_UpdateTime = new int[4];

	// Character attribute masks
	private static final int CHARATTR_CHARSET_MASK = 0x0007;
	private static final int CHARATTR_LARGE        = 0x0008; // Character is bolded.
	private static final int CHARATTR_FG_MASK      = 0x00F0; // Sets the foreground (text) color of a character.
	private static final int CHARATTR_BG_MASK      = 0x0F00; // Sets the background color of a character.
	private static final int CHARATTR_ITALIC       = 0x1000; // Character is italicized.
	private static final int CHARATTR_UNDERLINE    = 0x2000; // Character is underlined.
	private static final int CHARATTR_FLASH        = 0x4000; // Character is flashing.
	private static final int CHARATTR_DRAW_BG      = 0x8000; // Draw the background; if set to 0, BG_MASK is ignored (ie transparent).
	
	protected static final float[] DEFAULT_RAISED_PARAM = {2.0f, 0.0f, 3.0f};
	protected static final float[] DEFAULT_DEPRESSED_PARAM = {2.0f, -3.0f, -3.0f};
	protected static final float[] DEFAULT_SHADOW_PARAM = {5.0f, 0.0f, 5.0f};
	protected static final int DEFAULT_SHADOW_COLOR = 0xCC000000;

	/**
	 * \brief This enumeration sets how CEA 608 closed captions will be displayed (in FULL mode only).
	 */
	public enum CaptionMode {
		/** No captions.*/
	    None(0),
	    /** The rows of captions will be displayed and "roll up" the screen.  This may include 2, 3, or 4 rows displayed and "rolling" at once.*/
	    RollUp(1),         // RU2, RU3, RU4 Roll up (number of lines displayed in roll up mode)
	    /** The entire row of the caption will be displayed ("pop on") the screen at once. */
	    PopOn(2),          // RCL, EOC : whole line of text is displayed (pops up on the screen) at once (wait for command to display)
	    /** Each character in the caption will be displayed at a time, as it becomes available.*/
	    PaintOn(3),        // RDC : resume direct captioning:  each character is displayed one at a time.
	    /** Only text will be displayed.  This mode is used for example in emergency broadcast situations.*/
	    Text(4);           // TR, RTD  : TR text restart/resume text display: ONLY text mode ( e.g. in emergency broadcast situations)

		private int m_value;

		private CaptionMode( int value ) {
			m_value = value;
		}

		/**
		 * \brief This gets the integer value of the CaptionMode enumeration for CEA 608 closed captions.
		 *
		 * \returns  The integer value of the CaptionMode to be used.  See the CaptionMode enumeration for more details.
		 */
		public int getValue() {
			return m_value;
		}

		/**
		 * \brief This gets the mode that captions should be displayed based on the integer value of the enumeration.
		 *
		 * \returns  The CaptionMode to be used to display CEA 608 closed captions.  Please see the
		 *           enumeration for details on the different modes.
		 */
		public static CaptionMode fromValue( int value ) {
			for( CaptionMode item : values() ) {
				if( item.getValue() == value )
					return item;
			}
			return null;
		}
	}
	/**
	 * \brief This enumerator defines the encoding character set to be used for CEA 608 closed captions (in FULL mode only).
	 */
	public enum Charset {
		/** Unicode characters, include special characters for French and Spanish accents.*/
		UNICODE_UCS2(0), // includes special characters (french/spanish accents)
		/** Not currently used but reserved for future use.  Can be ignored.*/
		PRIVATE_1(1), // can be ignored
		/** Not currently used but reserved for future use.  Can be ignored.*/
		PRIVATE_2(2), // can be ignored, currently not used but reserved for future use
		/** Korean characters encoding.*/
		KSC_5601_1987(3), // Korean characters
		/** Chinese characters encoding.*/
		GB_2312_80(4); // Chinese characters

		private int m_value;

		private Charset( int value ) {
			m_value = value;
		}

		/**
		 * \brief  This gets the integer value code for the Charset enumeration.
		 *
		 * \returns The integer value of the Charset enumeration.  See the enumeration for possible values.
		 */
		public int getValue() {
			return m_value;
		}
		/**
		 * \brief  This gets the character set to be used for encoding based on the integer value.
		 *
		 * \returns  The character set to be used for encoding.  See the Charset enumeration for the possible character encoding sets.
		 *
		 */
		public static Charset fromValue( int value ) {
			for( Charset item : values() ) {
				if( item.getValue() == value )
					return item;
			}
			return null;
		}
	}

	/**
     * \brief   This enumeration sets the text display and background colors of CEA 608 closed captions.
     *
     * Each color option has an ARGB hexacode associated with the foreground color or background color of the caption to
     * be displayed, as well as a unique value to be used to identify which color is to be selected.
	 */

	public enum CaptionColor {
		/** This sets the CEA 608 closed captions text or background color white. */
		WHITE             (0x00, 0xFFFFFFFF, 0xFFEEEEEE),
		/** This sets the CEA 608 closed captions text or background color semi-transparent white. */
		WHITE_SEMITRANS   (0x01, 0xFFFFFFFF, 0x77FFFFFF),
		/** This sets the CEA 608 closed captions text or background color green. */
		GREEN             (0x02, 0xFF00FF00, 0xFF007700),
		/** This sets the CEA 608 closed captions text or background color semi-transparent green. */
		GREEN_SEMITRANS   (0x03, 0xFF00FF00, 0x7700FF00),
		/** This sets the CEA 608 closed captions text or background color blue. */
		BLUE              (0x04, 0xFF0000FF, 0xFF000077),
		/** This sets the CEA 608 closed captions text or background color semi-transparent blue. */
		BLUE_SEMITRANS    (0x05, 0xFF0000FF, 0x770000FF),
		/** This sets the CEA 608 closed captions text or background color cyan. */
		CYAN              (0x06, 0xFF00FFFF, 0xFF007777),
		/** This sets the CEA 608 closed captions text or background color semi-transparent cyan. */
		CYAN_SEMITRANS    (0x07, 0xFF00FFFF, 0x7700FFFF),
		/** This sets the CEA 608 closed captions text or background color red. */
		RED               (0x08, 0xFFFF0000, 0xFF770000),
		/** This sets the CEA 608 closed captions text or background color semi-transparent red. */
		RED_SEMITRANS     (0x09, 0xFFFF0000, 0x77FF0000),
		/** This sets the CEA 608 closed captions text or background color yellow. */
		YELLOW            (0x0A, 0xFFFFFF00, 0xFF777700),
		/** This sets the CEA 608 closed captions text or background color semi-transparent yellow. */
		YELLOW_SEMITRANS  (0x0B, 0xFFFFFF00, 0x77FFFF00),
		/** This sets the CEA 608 closed captions text or background color magenta. */
		MAGENTA           (0x0C, 0xFFFF00FF, 0xFF770077),
		/** This sets the CEA 608 closed captions text or background color semi-transparent magenta. */
		MAGENTA_SEMITRANS (0x0D, 0xFFFF00FF, 0x77FF00FF),
		/** This sets the CEA 608 closed captions text or background color black. */
		BLACK             (0x0E, 0xFF000000, 0xFF000000),
		/** This sets the CEA 608 closed captions text or background color semi-transparent black. */
		BLACK_SEMITRANS   (0x0F, 0xFF000000, 0x77000000),
		/** This sets the CEA 608 closed captions text or background color transparent (no color). */
		TRANSPARENT		  (0xFF, 0x00000000, 0x00000000);

		private int m_value;
		private int m_fg;
		private int m_bg;

		CaptionColor( int value, int fg, int bg ) {
			m_value = value;
			m_fg = fg;
			m_bg = bg;
		}

		/**
		 * \brief  This gets the integer value of the CaptionColor enumerator, to be used with CEA 608 closed captions in FULL mode.
		 *
		 * \returns  The integer value of the color to be used.  This will be one of:
		 *             - <b>0x00</b>:  White
		 *             - <b>0x01</b>:  Semi-transparent white
		 *             - <b>0x02</b>:  Green
		 *             - <b>0x03</b>:  Semi-transparent green
		 *             - <b>0x04</b>:  Blue
		 *             - <b>0x05</b>:  Semi-transparent blue
		 *             - <b>0x06</b>:  Cyan
		 *             - <b>0x07</b>:  Semi-transparent cyan
		 *             - <b>0x08</b>:  Red
		 *             - <b>0x09</b>:  Semi-transparent red
		 *             - <b>0x0A</b>:  Yellow
		 *             - <b>0x0B</b>:  Semi-transparent yellow
		 *             - <b>0x0C</b>:  Magenta
		 *             - <b>0x0D</b>:  Semi-transparent magenta
		 *             - <b>0x0E</b>:  Black
		 *             - <b>0x0F</b>:  Semi-transparent black
		 *             - <b>0xFF</b>:  Transparent
		 */
		public int getValue() {
			return m_value;
		}

		/**
		 * \brief This gets the caption color to be used with CEA 608 closed captions in FULL mode from the integer value.
		 *
		 * \param value  An integer value indicating the caption color to be selected.  This is a value between 0x00 and 0xFF.
		 *
		 * \returns The caption color to be used.
		 *
		 * \see The enumerator {@link CaptionColor} for more details on the possible captions and color values to be used.
		 */
		public static CaptionColor fromValue( int value ) {
			for( CaptionColor item : values() ) {
				if( item.getValue() == value )
					return item;
			}
			return null;
		}

		public static CaptionColor fromColor( int value ) {
			int index = 0;
			int minDiff = 255*3;
			for( int i=0; i<CaptionColor.values().length; i++ ) {
				int fg = CaptionColor.values()[i].m_fg;
				int r1 = Color.red(fg);
				int g1 = Color.green(fg);
				int b1 = Color.blue(fg);
				int r2 = Color.red(value);
				int g2 = Color.green(value);
				int b2 = Color.blue(value);
				int diff = Math.abs(r1 - r2) + Math.abs(g1-g2) + Math.abs(b1-b2);
				if(diff < minDiff) {
					minDiff = diff;
					index = i;
				}
			}
			return CaptionColor.values()[index];
		}

		/**
		 * \brief This gets the text or foreground color of the character to be displayed with CEA 608 closed captions in FULL mode.
		 *
		 * \returns  The text color to be displayed as an ARGB hexacode.  Please see the enumerator {@link CaptionColor} for the
		 *           possible hexacodes and their associated colors.
		 */
		public int getFGColor() {
			return m_fg;
		}

		/**
		 * \brief This gets the background color of the character to be displayed with CEA 608 closed captions in FULL mode.
		 *
		 * \returns  The background color to be displayed as an ARGB hexacode.  Please see the enumerator {@link CaptionColor} for the
		 *           possible hexacodes and their associated colors.
		 */
		public int getBGColor() {
			return m_bg;
		}

	}

	private static final int CHARSET_UNICODE_UCS2 = 0;

	private NexClosedCaption(int textOTI) {
		mCaptionType = textOTI;
		mTextType = convertTextTypeFromCaptionType(textOTI);
	}

	private int convertTextTypeFromCaptionType(int captionType) {
		int textType;

		switch (captionType) {
			case NexContentInformation.NEX_TEXT_TTML:
				textType = TEXT_TYPE_TTML_TIMEDTEXT;
				break;
			case NexContentInformation.NEX_TEXT_WEBVTT:
				textType = TEXT_TYPE_WEBVTT;
				break;
			case NexContentInformation.NEX_TEXT_3GPP_TIMEDTEXT:
				textType = TEXT_TYPE_3GPP_TIMEDTEXT;
				break;
			case NexContentInformation.NEX_TEXT_EXTERNAL_SMI:
				textType = TEXT_TYPE_SMI;
				break;
			case NexContentInformation.NEX_TEXT_EXTERNAL_SRT:
				textType = TEXT_TYPE_SRT;
				break;
			case NexContentInformation.NEX_TEXT_EXTERNAL_SUB:
				textType = TEXT_TYPE_SUB;
				break;
			case NexContentInformation.NEX_TEXT_CEA608:
				textType = TEXT_TYPE_NTSC_CC_CH1;
				break;
			case NexContentInformation.NEX_TEXT_CEA708:
				textType = TEXT_TYPE_ATSCMH_CC;
				break;
			default:
				textType = TEXT_TYPE_UNKNOWN;
				break;
		}

		return textType;
	}

	// CEA 708 yoon
	
	/**
	 * This is an array containing the CEA 708 closed caption data for the given content.
	 * 
	 * \see NexEIA708Struct
	 * \see NexEIA708CaptionView
	 * 
	 * \since version 6.1.2 */
	public byte[] mCEA708Data;
	/** This is the size of the \c mCEA708Data array containing the text data for CEA 708 closed captions within content.
	 * 
	 * \since version 6.1.2 */
	public int mCEA708Len;
	/** This is the service number of the CEA 708 closed captions to be used to display captions for the given content.
	 *  Different service numbers contain different closed caption data for the same content, for example captions of different languages.
	 *  
	 *  \since version 6.1.2  */
	public int mCEA708ServiceNO;

	/**
	 * In FULL mode, when CEA 608 closed captions are to be displayed and "rolled up" with animation,
	 * this defines the row that is being "rolled out" of the display during the animation, in other words
	 * the row that is disappearing as the next, new row of captions appears.
	 *
	 * This can be ignored if animation of the display is not being used.
	 */
	public static final int ROLLED_OUT_ROW = -1;

	/**
	 * \brief This method gets the base row of the roll up rows (will be a number from 0 to 15)
	 */
	public int getRollUpBaseRow() {
		return m_rollUpBaseRow;
	}
	/**
	 * \brief This gets the number of roll-up rows to be displayed when CEA 608 closed captions are to be displayed "rolling up" in FULL mode.
	 *
	 * \returns The number of roll up rows to be displayed:  2, 3, or 4.
	 */
	public int getRollUpNumRows() {
		return m_rollUpNumRows;
	}
	/**
	 * \brief This gets the time to be taken to "roll up" between rows or the animation time for CEA 608 closed captions in FULL mode.
	 *
	 * \returns The time in milliseconds it takes for a row to "roll up" to the next row, as a \c long.
	 */
	public long getRollUpElapsedTime() {
		return System.currentTimeMillis()%0xFFFFFFFFL - (((long)m_rollUpAnimationStartTime)&0xFFFFFFFFL);
	}

	/**
	 * \brief This gets the time at which the CEA 608 closed captions in the given channel were last updated.
	 *
	 * This function allows NexPlayer&trade;&nbsp;to determine if caption information is available on the
	 * given channel, and thus determine which CEA 608 channels are available for the playing content, using
	 * the method \c getAvailableCaptionChannel.
	 *
	 * \param channelNumber  The CEA 608 channel in which to get the time at which the captions were last updated.
	 *                       This will be 1, 2, 3, or 4.
	 *
	 * \returns  The time at which the caption content was last updated for the channel passed, in milliseconds (ms).
	 *
	 * \see getAvailableCaptionChannel
	 *
	 * \since version 5.11
	 */
	public long getCaptionUpdateTime(int channelNumber)
	{
		try{
			return ((long)(m_UpdateTime[channelNumber-1]) & 0xFFFFFFFFL);
		}catch(Exception e)
		{
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * \brief This method checks if CEA 608 closed caption information is available for the given channel.
	 *
	 * By checking how much time has passed since the channel information was updated (with the method \c getCaptionUpdateTime), this method can
	 * determine if there is any closed caption information available on this channel to be displayed.
	 * This allows the player to present the available CEA 608 channels to the user in order for the desired
	 * channel to be selected.
	 *
	 * Different channels often provide different closed caption information, for example in different languages,
	 * but the four specified channels may not always be used or available for any particular content.
	 *
	 * \param channelNumber  The CEA 608 channel number to check for closed caption information availability.
	 *                       This will be 1, 2, 3, or 4.
	 *
	 * \returns  \c TRUE if the channel has available information, \c FALSE if no recent closed caption information is available
	 *           on the given channel.
	 *
	 * \see getCaptionUpdateTime
	 *
	 * \since version 5.11
	 */
	public boolean getAvailableCaptionChannel(int channelNumber)
	{
		try{
			if(System.currentTimeMillis()%0xFFFFFFFFL - (((long)m_UpdateTime[channelNumber-1])&0xFFFFFFFFL) < 60000)
			{
				return true;
			}
			else
				return false;
			}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * \brief This gets the display mode for the CEA 608 closed captions when supported in FULL mode
	 *
	 * \returns  The CaptionMode of how the captions should be displayed.  This will be one of:
	 *             - CaptionMode.None
	 *             - CaptionMode.RollUp
	 *             - CaptionMode.PopOn
	 *             - CaptionMode.PaintOn
	 *             - CaptionMode.Text
	 *
	 * \see The CaptionMode enumeration for more details on the ways in which captions can be displayed. displaytext/rollup/paint on modes etc
	 */

	public CaptionMode getCaptionMode() {
		return m_captionMode;
	}
	/**
	 * \brief  This determines if the character in CEA 608 closed captions is to be displayed in italics. (FULL mode)
	 *
	 * \param row  The row or vertical position of the character to be displayed.
	 * \param col  The column or horizontal position of the character to be displayed.
	 *
	 * \returns TRUE if italicized, FALSE if not.
	 */
	public boolean isItalic( int row, int col ) {
		if( m_attr == null || ((row<0 || row>14) && row != ROLLED_OUT_ROW )) {
			return false;
		} else if( row==ROLLED_OUT_ROW ) {
			row = 15;
		}
		return (m_attr[ row*32 + col ] & CHARATTR_ITALIC)!=0 ? true : false;
	}

	/**
	 * \brief  This determines if the character in CEA 608 closed captions is to be underlined when displayed (FULL mode).
	 *
	 * \param row  The row or vertical position of the character to be displayed.
	 * \param col  The column or horizontal position of the character to be displayed.
	 *
	 * \returns TRUE if underlined, FALSE if not.
	 *
	 */
	public boolean isUnderline( int row, int col ) {
		if( m_attr == null || ((row<0 || row>14) && row != ROLLED_OUT_ROW )) {
			return false;
		} else if( row==ROLLED_OUT_ROW ) {
			row = 15;
		}
		return (m_attr[ row*32 + col ] & CHARATTR_UNDERLINE)!=0 ? true : false;
	}

	/**
	 * This determines if the character in CEA 608 closed captions is to be displayed flashing (FULL mode).
	 *
	 * \param row  The row or vertical position of the character to be displayed.
	 * \param col  The column or horizontal position of the character to be displayed.
	 *
	 * \returns TRUE if flashing, FALSE if not.
	 */
	public boolean isFlashing( int row, int col ) {
		if( m_attr == null || ((row<0 || row>14) && row != ROLLED_OUT_ROW )) {
			return false;
		} else if( row==ROLLED_OUT_ROW ) {
			row = 15;
		}
		return (m_attr[ row*32 + col ] & CHARATTR_FLASH)!=0 ? true : false;
	}

	/**
	 * \brief This determines if the character in CEA 608 closed captions is to be displayed in <b>BOLD</b> (FULL mode).
	 *
	 * \param row  The row or vertical position of the character to be displayed.
	 * \param col  The column or horizontal position of the character to be displayed.
	 *
	 * \returns TRUE if <b>bold</b>, FALSE if not.
	 */
	public boolean isLarge( int row, int col ) {
		if( m_attr == null || ((row<0 || row>14) && row != ROLLED_OUT_ROW )) {
			return false;
		} else if( row==ROLLED_OUT_ROW ) {
			row = 15;
		}
		return (m_attr[ row*32 + col ] & CHARATTR_LARGE)!=0 ? true : false;
	}

	/**
	 * \brief  This determines if the background of the character in CEA 608 closed captions is to be displayed or not (FULL mode).
	 *
	 * If the background is not to be displayed, it will be transparent and the background color value of the caption will be ignored.
	 * \param row  The row or vertical position of the character to be displayed.
	 * \param col  The column or horizontal position of the character to be displayed.
	 *
	 * \returns TRUE if background color is to be displayed, FALSE if not.
	 */
	public boolean isDrawBackground( int row, int col ) {
		if( m_attr == null || ((row<0 || row>14) && row != ROLLED_OUT_ROW )) {
			return false;
		} else if( row==ROLLED_OUT_ROW ) {
			row = 15;
		}
		return (m_attr[ row*32 + col ] & CHARATTR_DRAW_BG)!=0 ? true : false;
	}
	/**
	 * \brief This determines the color to display the character in CEA 608 closed captions (FULL mode).
	 *
	 * \param row  The row or vertical position of the character to be displayed.
	 * \param col  The column or horizontal position of the character to be displayed.
	 *
	 * \returns TRUE if <i>italicized</i>, FALSE if not.
	 */
	public CaptionColor getFGColor( int row, int col ) {
		if( m_attr == null || ((row<0 || row>14) && row != ROLLED_OUT_ROW )) {
			return null;
		} else if( row==ROLLED_OUT_ROW ) {
			row = 15;
		}
		return CaptionColor.fromValue((m_attr[ row*32 + col ] >> 4) & 0xF);
	}

	/**
	 * \brief This determines the background color to display behind the character in CEA 608 closed captions (FULL mode).
	 *
	 * \param row  The row or vertical position of the character to be displayed.
	 * \param col  The column or horizontal position of the character to be displayed.
	 *
	 * \returns The background color to display.
	 *
	 * \see {@link CaptionColor} for color options available for CEA 608 closed captions in FULL mode.
	 */
	public CaptionColor getBGColor( int row, int col ) {
		if( m_attr == null || ((row<0 || row>14) && row != ROLLED_OUT_ROW )) {
			return null;
		} else if( row==ROLLED_OUT_ROW ) {
			row = 15;
		}
		if( (m_attr[ row*32 + col ] & CHARATTR_DRAW_BG) == 0 ) {
			return CaptionColor.TRANSPARENT;
		}
		return CaptionColor.fromValue((m_attr[ row*32 + col ] >> 8) & 0xF);
	}

	/**
	 * \brief This gets the encoding set for the character in CEA 608 closed captions
	 *
	 * \param row  The row position of the character to be displayed, as an \c integer.
	 * \param col  The column position of the character to be displayed, as an \c integer.
	 *
	 * \returns  The character encoding set to be used.  This will be one of:
	 *               - UNICODE_UCS2(0) : Unicode characters, including special characters for French and Spanish accents.
	 *               - PRIVATE_1(1): Not currently used but reserved for future use.  Can be ignored.
	 *               - PRIVATE_2(2): Not currently used but reserved for future use.  Can be ignored.
	 *               - KSC_5601_1987(3):  Korean characters encoding.
	 *               - GB_2312_80(4):  Chinese characters encoding.
	 */
	public Charset getCharset( int row, int col ) {
		if( m_attr == null || ((row<0 || row>14) && row != ROLLED_OUT_ROW )) {
			return null;
		} else if( row==ROLLED_OUT_ROW ) {
			row = 15;
		}
		return Charset.fromValue((m_attr[ row*32 + col ]) & 0x7);
	}

	/**
	 * \brief This gets the character to display in CEA 608 closed captions (FULL mode).
	 *
	 * \param row  The row position of the character to be displayed, as an \c integer.
	 * \param col  The column position of the character to be displayed, as an \c integer.
	 *
	 * \returns  The character to be displayed.
	 *
	 */
	public char getCharCode( int row, int col ) {
		if( m_charcode == null || ((row<0 || row>14) && row != ROLLED_OUT_ROW )) {
			return 0;
		} else if( row==ROLLED_OUT_ROW ) {
			row = 15;
		}
		return (char)m_charcode[ row*32 + col ];
	}
	
	/**
	 * 
	 * \brief This method returns the String data for CEA 608 closed captions in content.
	 * 
	 * This method allows CEA 608 closed captions to be implemented in a caption renderer
	 * other than the NexCaptionRenderer provided by the NexPlayer&trade;&nbsp;SDK.
	 * 
	 * If this method returns \c null, do not use it.
	 * Otherwise, use the string to implement and display CEA 608 closed captions with your specific renderer.
	 * 
	 * \returns		A String of CEA 608 closed caption data.  If this method returns \c null, no
	 * 				caption data was received and no captions should be displayed.
	 * 
	 * \since version 5.12
	 * */
	
	public String getString()
	{
		String str = new String();
		String strTemp = new String();
		for(int i=0;i<15;i++)
		{
			for(int j=0;j<32;j++)
			{
				char ch = getCharCode(i, j);
				if(ch != 0)
				{
					strTemp = String.format("%c", ch);
					str += strTemp;
				}
			}
		}
		return str;
	}
	/**
	 * \brief This method clears displayed CEA 608 closed captions, displaying a blank line instead.
	 */
	public void makeBlankData()
	{
		m_charcode = null;
		m_attr = null;
	}

	/*
	 * END
	 * */

    private NexClosedCaption(
            int textType,
            int encodingType,
            int isItalic,
            int isUnderline,
            int captionColor,
            int bgColor,
            int isOpaque,
            int isEnable,
            int rows,
            int indent,
            byte[] TextData)
    {
        if(TextData == null)
        {
        	NexLog.d("NexClosedCaption", "ID3TagText text is null!!");
        }

        mTextType = TEXT_TYPE_NTSC_CC_CH1;
        mEncodingType = getEncodingType(encodingType);

        switch(captionColor)
        {
            case White:
                mCaptionColor = 0xFFFFFFFF;
                break;
            case Green:
                mCaptionColor = 0xFF00FF00;
                break;
            case Blue:
                mCaptionColor = 0xFF0000FF;
                break;
            case Cyan:
                mCaptionColor = 0xFF00FFFF;
                break;
            case Red:
                mCaptionColor = 0xFFFF0000;
                break;
            case Yellow:
                mCaptionColor = 0xFFFFFF00;
                break;
            case Magenta:
                mCaptionColor = 0xFFFF00FF;
                break;
            case Black:
                mCaptionColor = 0xFF000000;
                break;
            case Transparent:
                mCaptionColor = 0x00000000;
                break;
            default:
                mCaptionColor = 0xFFFFFFFF;
                break;
        }

        switch(bgColor)
        {
            case White:
                mBgColor = 0xFFFFFFFF;
                break;
            case Green:
                mBgColor = 0xFF00FF00;
                break;
            case Blue:
                mBgColor = 0xFF0000FF;
                break;
            case Cyan:
                mBgColor = 0xFF00FFFF;
                break;
            case Red:
                mBgColor = 0xFFFF0000;
                break;
            case Yellow:
                mBgColor = 0xFFFFFF00;
                break;
            case Magenta:
                mBgColor = 0xFFFF00FF;
                break;
            case Black:
                mBgColor = 0xFF000000;
                break;
            case Transparent:
                mBgColor = 0x00000000;
                break;
            default:
                mBgColor = 0xFF000000;
                break;
        }

        mIsItalic       = isItalic;
        mIsUnderline    = isUnderline;
        mIsOpaque       = isOpaque;
        mIsEnable       = isEnable;

        mRows           = rows;
        mIndent         = indent;

        mTextData = TextData;
    }

    private int getEncodingType(int encodingType)
    {
        switch(encodingType)
        {
        case ENCODING_TYPE_ISO8859_1:
        case ENCODING_TYPE_UTF16:
        case ENCODING_TYPE_UTF16_BE:
        case ENCODING_TYPE_UTF8:
        case ENCODING_TYPE_ASCII:
        case ENCODING_TYPE_UNICODE:
        case ENCODING_TYPE_EUC_KR:
            return encodingType;
        default:
            return ENCODING_TYPE_UNKNOWN;
        }
    }

	static String convertEncodingType(int encodingType) {
		String encoding = "UTF-8";

		switch (encodingType) {
			case NexClosedCaption.ENCODING_TYPE_ISO8859_1 : encoding = "ISO-8859-1"; break;
			case NexClosedCaption.ENCODING_TYPE_UTF16 : encoding = "UTF-16"; break;
			case NexClosedCaption.ENCODING_TYPE_UTF16_BE : encoding = "UTF-16BE"; break;
			case NexClosedCaption.ENCODING_TYPE_UTF8 : encoding = "UTF-8"; break;
			case NexClosedCaption.ENCODING_TYPE_ASCII : encoding = "US-ASCII"; break;
			case NexClosedCaption.ENCODING_TYPE_EUC_KR : encoding = "EUC-KR"; break;
			case NexClosedCaption.ENCODING_TYPE_UNKNOWN : break;
		}

		return encoding;
	}

    /**
     * \brief   This method determines the type of text captions used by the content.
     *
     *  Most subtitles will be displayed without further processing but CEA 608 closed captions can include
     *  additional text attributes.
     *
     * \returns The type of text to be displayed.  This will be one of:
     *          - <b>TEXT_TYPE_UNKNOWN</b> The type of text is unknown. The text is treated like a general text file.
     *          - <b>TEXT_TYPE_GENERAL</b>  This is text only and requires no additional processing.
     *          - <b>TEXT_TYPE_ATSCMH_CC</b> Not a format currently supported.
     *          - <b>TEXT_TYPE_ATSCMH_BAR</b> The text includes text bar data.  Not currently supported.
     *          - <b>TEXT_TYPE_ATSCMH_AFD</b> The text includes Active Format Description data.  Not currently supported.
     *          - <b>TEXT_TYPE_NTSC_CC_CH1</b> The text is CEA 608 closed captions on Data Channel 1.
     *          - <b>TEXT_TYPE_NTSC_CC_CH2</b> The text is CEA 608 closed captions on Data Channel 2.
     */
    public int getTextType()
    {
        return mTextType;
    }

	/**
	 * \brief   This method returns the caption type converted from the NexClosedCaption type to the NexContentInformation.
	 *
	 * \returns The type of text to be displayed.  This will be one of:
	 * 		- \link NexContentInformation#NEX_TEXT_UNKNOWN NEX_TEXT_UNKNOWN \endlink = 0x00000000: Unknown caption format.
	 * 		- \link NexContentInformation#NEX_TEXT_EXTERNAL_SMI NEX_TEXT_EXTERNAL_SMI\endlink = 0x00000002: SMI subtitles.
	 * 		- \link NexContentInformation#NEX_TEXT_EXTERNAL_SRT NEX_TEXT_EXTERNAL_SRT\endlink = 0x00000003:  SRT subtitles.
	 * 		- \link NexContentInformation#NEX_TEXT_3GPP_TIMEDTEXT NEX_TEXT_3GPP_TIMEDTEXT\endlink = 0x50000000: 3GPP timed text.
	 * 		- \link NexContentInformation#NEX_TEXT_WEBVTT NEX_TEXT_WEBVTT\endlink = 0x50000001:  WebVTT text tracks.
	 * 		- \link NexContentInformation#NEX_TEXT_TTML NEX_TEXT_TTML\endlink = 0x50000002:  TTML timed text.
	 *		- \link NexContentInformation#NEX_TEXT_CEA NEX_TEXT_CEA\endlink = 0x50000010:  Closed captions.
	 *		- \link NexContentInformation#NEX_TEXT_CEA608 NEX_TEXT_CEA608\endlink = 0x50000011:  CEA608 caption.
	 *		- \link NexContentInformation#NEX_TEXT_CEA708 NEX_TEXT_CEA708\endlink = 0x50000012:  CEA708 caption.
	 */
	public int getCaptionType()
	{
		return mCaptionType;
	}

    /**
     * \brief   This method returns the text display color of CEA 608 closed captions.
     *
     * \returns  The color of the displayed caption text as an ARGB hexacode.
     * 	         This will be one of:
     *              - <b>White = 0xFFFFFFFF </b> (default)
     *              - <b>Green  = 0xFF00FF00 </b>
     *              - <b>Blue = 0xFF0000FF </b>
     *              - <b>Cyan = 0xFF00FFFF </b>
     *              - <b>Red = 0xFFFF0000 </b>
     *              - <b>Yellow = 0xFFFFFF00 </b>
     *              - <b>Magenta = 0xFFFF00FF </b>
     *              - <b>Black = 0xFF000000 </b>
     *              - <b>Transparent = 0x00000000 </b>
     *
     * @deprecated Do not use.
     *
     */
    public int getCaptionColor()
    {
        return mCaptionColor;
    }

    /**
     * \brief   This method returns the background color of CEA 608 closed captions.
     *
     * \returns  The background color of the displayed caption text as an ARGB hexacode.
     * 	         This will be one of:
     *              - <b>White = 0xFFFFFFFF </b>
     *              - <b>Green  = 0xFF00FF00 </b>
     *              - <b>Blue = 0xFF0000FF </b>
     *              - <b>Cyan = 0xFF00FFFF </b>
     *              - <b>Red = 0xFFFF0000 </b>
     *              - <b>Yellow = 0xFFFFFF00 </b>
     *              - <b>Magenta = 0xFFFF00FF </b>
     *              - <b>Black = 0xFF000000 </b> (default)
     *              - <b>Transparent = 0x00000000 </b>
     *
     * @deprecated Do not use.
     *
     */
    public int getBGColor()
    {
        return mBgColor;
    }

    /**
     * \brief   This method determines CEA 608 closed captions <i>italics</i>.
     *
     * \returns  Zero if the text is displayed normally; 1 if the text should be displayed in <i>italics</i>.
     *
     * @deprecated Do not use.
     * 
     */
    public int isItalic()
    {
        return mIsItalic;
    }

    /**
     * \brief   This method determines whether CEA 608 closed captions are underlined.
     *
     * \returns Zero if the text is displayed normally; 1 if the text should be underlined.
     *
     * @deprecated Do not use.
     *
     */
    public int isUnderline()
    {
        return mIsUnderline;
    }

    /**
     * \brief   This method determines the opacity of CEA 608 closed captions background color.
     *
     * \returns  Always 1 (opaque) for CEA 608 closed captions.
     *
     * @deprecated Do not use.
     * 
     */
    public int isOpaque()
    {
        return mIsOpaque;
    }

    /** \brief  This method controls whether CEA 608 closed captions are enabled or disabled.
     *
     * \returns  Always 0.
     *  
     *  @deprecated Do not use.
     *  
     */
    public int isEnable()
    {
        return mIsEnable;
    }

    /** \brief This method returns the text's vertical position for CEA 608 closed captions only.
     *
     *  CEA 608 closed captions are positioned vertically based on this value.
     *  There are 15 possible rows in which the captions can be displayed, 1 being at the top of the screen and 15 at
     *  the bottom.
     *
     *  \returns The vertical position at which to display the caption. This will be an integer value from 1 to 15.
     */
    public int getRows()
    {
        return mRows;
    }

    /** \brief This method returns the text's horizontal position for CEA 608 closed captions only.
     *
     *  CEA 608 closed captions will be indented horizontally based on this value.
     *  There are 8 possible horizontal column positions, 1 being at the far left of the screen and 8 indented to
     *  the far right.
     *
     *  \returns The horizontal position at which to display the caption. This will be an integer value from 1 to 8.
     */
    public int getIndent()
    {
        return mIndent;
    }


    /**
     * \brief This determines the encoding type of the content's captions or subtitles, when available.
     *
     * \returns The encoding type of the subtitles or captions.  This will be one of the following values:
     *              - <b>ENCODING_TYPE_ISO8859_1    (0x0)</b>
     *              - <b>ENCODING_TYPE_UTF16        (0x1)</b>
     *              - <b>ENCODING_TYPE_UTF16_BE     (0x2)</b>
     *              - <b>ENCODING_TYPE_UTF8         (0x3)</b>
     *              - <b>ENCODING_TYPE_ASCII        (0x10)</b>
     *              - <b>ENCODING_TYPE_UNICODE      (0x20)</b>
	 *              - <b>ENCODING_TYPE_EUC_KR       (0x21)</b>
     *              - <b>ENCODING_TYPE_UNKNOWN  (0xFFFFFFFF)</b>
     */
    public int getEncodingType()
    {
        return mEncodingType;
    }

    /**
     * \brief This method gets the text data of local subtitle files in content.
     * 
     * \warning This method can <b>only</b> be used for standard subtitle files 
     * including SMI, SRT, SUB, and Smooth Streaming subtitles.  It may NOT be used
     * to determine the text of CEA 608 closed captions or CFF and 3GPP timed text.
     * 
     * The byte array of text data returned here can be used to display the content's
     * subtitles as desired.
     * 
     * The caption text of CEA 608 closed captions can be received by calling 
     * \link NexClosedCaption.getString \endlink and text for 3GPP Timed Text is received
     * by calling \link getTextDataFor3GPPTT\endlink.  Text for CFF timed text is received
     * by calling \link getTextDataForTTML \endlink.
     * 
     * \returns A byte array of the subtitle text data for the current content.
     *
     */
    public byte[] getTextData()
    {
        return mTextData;
    }

    
     // 3GPP Timed Text data structure.
     
    
    private final int SampleModifier_TEXTSTYLE = 0;
    private final int SampleModifier_TEXTHIGHLIGHT = 1;
    private final int SampleModifier_TEXTHILIGHTCOLOR = 2;
    private final int SampleModifier_TEXTKARAOKE = 3;
    private final int SampleModifier_TEXTSCROLLDELAY = 4;
    private final int SampleModifier_TEXTHYPERTEXT = 5;
    private final int SampleModifier_TEXTTEXTBOX = 6;
    private final int SampleModifier_TEXTBLINK = 7;
    private final int SampleModifier_TEXTTEXTWRAP = 8;
    
    private final int SampleModifier_VerticalJustification = 10;
    private final int SampleModifier_HorizontalJustification = 11;
    private final int SampleModifier_ScrollIN = 12;
    private final int SampleModifier_ScrollOUT = 13;
    private final int SampleModifier_ScrollDirection = 14;
    private final int SampleModifier_ContinuousKaraoke = 15;
    private final int SampleModifier_WriteVertically = 16;
    private final int SampleModifier_FillTextRegion = 17;
    
    
    private byte[] m_3gppTT_TextBuffer;
    private int m_3gppTTRegionTX = 0;
    private int m_3gppTTRegionTY = 0;
    private int m_3gppTTRegionWidth = 0;
    private int m_3gppTTRegionHeight = 0;
    private int m_3gppTTTextColor = 0xFF000000;//default : BLACK.
    private int m_3gppTTBGColor = 0;
    
    private int m_VerticalJustification = 0;//0 : top, 1: center, -1 : bottom
    private int m_HorizontalJustification = 0;//0:left, 1: center, -1 : right
    
    private boolean isScrollIn = false;
    private boolean isScrollOut = false;
    private int isScrollDirection = 0;
    private boolean isContinuousKaraoke = false;
    private boolean isWriteVertically = false;
    private boolean isFillTextRegion = false;
    
    private int m_startTime = 0;//these values used for scroll delay. 
    private int m_endTime = 0;
    
    /**
	 * \brief Both the sample format and the sample description contain style records, so it is define here for compactness. 3GPP timed text
	 * 
	 * 
	 */
    public static class TextStyleEntry
    {	
    	private short c_startChar = 0;
    	private short c_endChar = 0;
    	private short c_fontID = 0;
    	private short c_fontSize = 0;
    	private int c_textColor = 0;
    	private boolean c_isBold = false;
    	private boolean c_isItalic = false;
    	private boolean c_isUnderline = false;
    	
    	private TextStyleEntry(short startChar, 
    					short endChar,
    					short fontID,
    					short fontSize,
    					int textColor,
    					int isBold,
    					int isItalic,
    					int isUnderline)
    	{
    		c_startChar = startChar;
        	c_endChar = endChar;
        	c_fontID = fontID;
        	c_fontSize = fontSize;
        	c_textColor = textColor;
        	if(isBold > 0)
        		c_isBold = true;
        	if(isItalic < 0)
        		c_isItalic = true;
        	if(isUnderline > 0)
        		c_isUnderline = true;
    	}
    	
    	/**
    	 * \brief This property specifies the starting position of the character.
    	 *
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getStartChar()
    	{
    		return c_startChar;
    	}
    	
    	/**
    	 * \brief This property specifies the ending position of the character.
    	 * 
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getEndChar()
    	{
    		return c_endChar;
    	}
    	
    	/**
    	 * \brief This property specifies the font type that will be used for this style.
    	 * 
    	 * \returns The font table index. 
    	 */
    	public short getFontID()
    	{
    		return c_fontID;
    	}
    	
    	/**
    	 * \brief This property specifies the font size that will be used for this style.
    	 * 
    	 * \returns The pixel of the font. 
    	 */
    	public short getFontSize()
    	{
    		return c_fontSize;
    	}
    	/**
    	 * \brief This property specifies the font color that will be used for this style.
    	 * 
    	 * \returns The color of the font. 
    	 */
    	public int getFontColor()
    	{
    		return c_textColor;
    	}
    	/**
    	 * \brief This property specifies the Bold font type that will be used for this style.
    	 * 
    	 * \returns TRUE if <b>bold</b>, FALSE if not.
    	 */
    	public boolean getBold()
    	{
    		return c_isBold;
    	}
    	/**
    	 * \brief This property specifies the Italic font type that will be used for this style.
    	 * 
    	 * \returns TRUE if <b>italic</b>, FALSE if not.
    	 */
    	public boolean getItalic()
    	{
    		return c_isItalic;
    	}
    	/**
    	 * \brief This property specifies the Underline font type that will be used for this style.
    	 * 
    	 * \returns TRUE if <b>underline</b>, FALSE if not.
    	 */
    	public boolean getUnderline()
    	{
    		return c_isUnderline;
    	}
    }
    
    /**
	 * \brief This property specifies the style of the text and sets the TextStyleEntry.
	 */
    public class TextStyle{
    	private int totalEntry = 0;
    	private TextStyleEntry[] c_entry;
    	private int m_index = 0;
    	private TextStyle(int totalCount){
    		totalEntry = totalCount;
    		c_entry = new TextStyleEntry[totalCount];
    	}
    	/**
    	 * \brief Adds the TextStyleEntry to this specific class.
    	 * 
    	 * \param entry The TextStyleEntry for TextStyle class. 
    	 */
    	public void setTextStyleEntry(TextStyleEntry entry)
    	{
    		if(m_index >= totalEntry)
    		{
    			return;
    		}
    		c_entry[m_index] = entry;
    		m_index++;
    	}
    	
    	/**
    	 * \brief This property specifies the number of the TextStyleEntry.
    	 * 
    	 * \returns The total number of the  TextStyleEntry.
    	 */
    	public int getCount()
    	{
    		return totalEntry;
    	}
    	/**
    	 * \brief This property gets specific TextStyleEntry.
    	 * 
    	 * \param index The index of the specific TextStyleEntry.
    	 * 
    	 * \returns The specific TextStyleEntry.
    	 * 
    	 */
    	public TextStyleEntry getStyleEntry(int index)
    	{
    		if(index >= totalEntry)
    		{
    			return null;
    		}
    		return c_entry[index];
    	}
		/**
		 * \brief This property specifies the current number of the TextStyleEntry.
		 * 
		 * \returns The current TextStyleEntry index.
		 * 
		 */
    	public int getCurrentCount()
    	{
    		return m_index;
    	}
    }
    
    /**
	 * \brief This property specifies the position of the highlighted text.
	 */
    public class TextHighlight
    {
    	private short c_startChar = 0;
    	private short c_endChar = 0;
    	
    	private TextHighlight(short startChar, short endChar)
    	{
    		c_startChar = startChar;
    		c_endChar = endChar;
    	}
    	/**
    	 * \brief This property specifies the starting position of the character.
    	 *
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getStartChar()
    	{
    		return c_startChar;
    	}
    	/**
    	 * \brief This property specifies the ending position of the character.
    	 *
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getEndChar()
    	{
    		return c_endChar;
    	}
    }
    
        /**
  	    * \brief This property specifies the color of the highlighted text.
  	    */
    public class TextHighlightColor
    {
    	private int c_highlightcolor = 0;
    	
    	private TextHighlightColor(int col){
    		c_highlightcolor = col;
    	}
    	/**
    	 * \brief This property specifies the color of the highlighted character.
    	 *
    	 * \returns The color of highlighted character. 
    	 */
    	public int getHighlightColor()
    	{
    		return c_highlightcolor;
    	}
    }
    
    /**
  	 * \brief This property specifies the karaoke type highlighted text.
  	 */
    public static class TextKaraokeEntry
    {
    	private int c_highlight_end_time = 0;
    	private short c_startcharoffset = 0;
    	private short c_endcharoffset = 0;
    	
    	private TextKaraokeEntry(int highlight_end_time, 
    							short startcharoffset, 
    							short endcharoffset)
    	{
    		c_highlight_end_time = highlight_end_time;
    		c_startcharoffset = startcharoffset;
    		c_endcharoffset = endcharoffset;
    	}
    	
    	 /**
    	 * \brief This property specifies the end time of the sequence of the karaoke type highlighted text.
    	 */
    	public int getHighlightEndTime()
    	{
    		return c_highlight_end_time;
    	}
    	/**
    	 * \brief This property specifies the starting position of the character.
    	 *
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getStartCharOffset()
    	{
    		return c_startcharoffset;
    	}
    	/**
    	 * \brief This property specifies the ending position of the character.
    	 *
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getEndCharOffset()
    	{
    		return c_endcharoffset;
    	}
    }
    
    /**
  	 * \brief This property specifies the number of highlighted text.
  	 */
    public class TextKaraoke
    {
    	private int c_startTime = 0;
    	private TextKaraokeEntry[] c_entry = null;
    	private int totalEntry = 0;
    	private int m_index = 0;
    	
    	private TextKaraoke(int startTime, int count){
    		totalEntry = count;
    		c_startTime = startTime;
    		c_entry = new TextKaraokeEntry[count];
    	}
    	/**
    	 * \brief Adds the TextKaraokeEntry to this specific class.
    	 * 
    	 * \param entry The TextKaraokeEntry for TextKaraoke class. 
    	 */
    	public void setKaraokeEntry(TextKaraokeEntry entry)
    	{
    		if(m_index >= totalEntry)
    		{
    			return;
    		}
    		c_entry[m_index] = entry;
    		m_index++;
    	}
    	
    	/**
    	 * \brief This property specifies the starting time of the whole KaraokeEntry.
    	 *
    	 * \returns The start time of the whole KaraokeEntry. 
    	 */
    	public int getStartTime()
    	{
    		return c_startTime;
    	}
    	/**
    	 * \brief This property specifies the number of the TextKaraokeEntry.
    	 * 
    	 * \returns The total number of the TexKaraokeEntry.
    	 */
    	
    	public int getCount()
    	{
    		return totalEntry;
    	}
    	/**
    	 * \brief This property gets specific TextKaraokeEntry.
    	 * 
    	 * \param index The index of the specific TextKaraokeEntry.
    	 * 
    	 * \returns The specific TextKaraokeEntry.
    	 * 
    	 */
    	public TextKaraokeEntry getKaraokeEntry(int index)
    	{
    		return c_entry[index];
    	}
		/**
		 * \brief This property specifies the current number of the TextKaraokeEntry.
		 * 
		 * \returns The current TextKaraokeEntry index.
		 * 
		 */
    	public int getCurrentCount()
    	{
    		return m_index;
    	}
    }
    
    /**
  	 * \brief This property specifies the delaying time of the scrolling text.
  	 */
    public class TextScrollDelay
    {
    	private int c_scrollDelay = 0;
    	
    	private TextScrollDelay(int delay)
    	{
    		c_scrollDelay = delay;
    	}
        /**
      	 * \brief This method returns the delaying time of this class.
      	 * 
      	 * \returns ScrollDelay time.
      	 */
    	public int getScrollDelay()
    	{
    		return c_scrollDelay;
    	}
    }
    
    /**
  	 * \brief This property specifies the hyperlink of text that describes the hypertext information.
  	 */
    public class TextHyperText
    {
    	private short c_startcharoffset;
    	private short c_endcharoffset;
    	private String c_URLString;
    	private String c_altString;
    	
    	private TextHyperText(short startOffset, short endOffset, String URL, String alt)
    	{
    		c_startcharoffset = startOffset;
    		c_endcharoffset = endOffset;
    		c_URLString = URL;
    		c_altString = alt;
    	}
    	/**
    	 * \brief This property specifies the starting position of the character.
    	 *
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getStartOffset()
    	{
    		return c_startcharoffset;
    	}
    	/**
    	 * \brief This property specifies the ending position of the character.
    	 *
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getEndOffset()
    	{
    		return c_endcharoffset;
    	}
    	/**
    	 * \brief The link to URL.
    	 *
    	 * \returns URL link  of this  class. 
    	 */
    	public String getURL()
    	{
    		return c_URLString;
    	}
    	/**
    	 * \brief An 'alt' string for user display.
    	 * 
    	 * The altString is a tool-tip or other visual clue.
    	 *
    	 * \returns alt String of this class. 
    	 */
    	public String getAlt()
    	{
    		return c_altString; 
    	}
    }
    
    
    /**
  	 * \brief This property requests blinking text for the indicated character range.
  	 * 
  	 */
    public class TextBlink
    {
    	private short c_startcharoffset = 0;
    	private short c_endcharoffset = 0;
    	
    	private TextBlink(short start, short end)
    	{
    		c_startcharoffset = start;
    		c_endcharoffset = end;
    	}
    	/**
    	 * \brief This property specifies the starting position of the character.
    	 *
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getStartOffset()
    	{
    		return c_startcharoffset;
    	}
    	/**
    	 * \brief This property specifies the ending position of the character.
    	 *
    	 * \returns The character number position, not the bytes. 
    	 */
    	public short getEndOffset()
    	{
    		return c_endcharoffset;
    	}
    }
    
    /**
  	 * \brief This property specifies the text wrap behavior.
  	 */
    public enum TextWrap
    {
    	NO_WRAP(0),
    	AUTOMATIC_SOFT_WRAP(1);
    	
    	private int m_value;

		private TextWrap( int value ) {
			m_value = value;
		}

		public int getValue() {
			return m_value;
		}
		
		public static TextWrap fromValue( int value ) {
			for( TextWrap item : values() ) {
				if( item.getValue() == value )
					return item;
			}
			return null;
		}
    	
    }
    
    private TextStyle m_textStyle = null;
    private TextHighlight m_textHighlight = null;
    private TextHighlightColor m_textHighlightColor = null;
    private TextKaraoke m_textKaraoke = null;
    private TextScrollDelay m_textScrollDelay = null;
    private TextHyperText m_textHyperText = null;
    private Rect m_TextBox = null;
    private ArrayList<TextBlink> m_textBlink = null;
    private TextWrap m_textWrap = null;
    private float[] m_Matrix = null;
    
    private ArrayList<CharSequence> m_fontTableLable = new ArrayList<CharSequence>();
    private ArrayList<Integer> m_fontTableIndex = new ArrayList<Integer>();
    
    private NexClosedCaption(int region_tx,
    						int region_ty,
    						int region_width,
    						int region_height,
    						int background_color,
    						byte [] textData)
    {
    	m_3gppTT_TextBuffer = textData;
    	m_3gppTTRegionTX = region_tx;
    	m_3gppTTRegionTY = region_ty;
    	m_3gppTTRegionWidth = region_width;
    	m_3gppTTRegionHeight = region_height;
    	m_3gppTTBGColor = background_color;
    	mTextType = TEXT_TYPE_3GPP_TIMEDTEXT;
    	if(m_textBlink != null)
    		m_textBlink = null;
    	m_textBlink = new ArrayList<TextBlink>();
    }
    // This method is called by native.
    @SuppressWarnings("unused")
	private void setSampleModifier_CreateTextStyle(int count)
    {
    	if(m_textStyle != null)
    	{
    		m_textStyle = null;
    	}
    	m_textStyle = new TextStyle(count);
    }
    // This method is called by native.
    @SuppressWarnings("unused")
	private void setSampleModifier_AddTextStyleEntry(TextStyleEntry entry)
    {
    	if(m_textStyle == null)
    	{
    		return;
    	}
    	if(entry != null)
    		m_textStyle.setTextStyleEntry(entry);
//    	else
//    	{
//    		m_textStyle.setCount(m_textStyle.getCurrentCount());
//    	}
    }
 // This method is called by native.
    @SuppressWarnings("unused")
	private void setSampleModifier_Karaoke(int startTime, int count)
    {
    	if(m_textKaraoke != null)
    		m_textKaraoke = null;
    	
    	m_textKaraoke = new TextKaraoke(startTime, count);
    }
 // This method is called by native.
    @SuppressWarnings("unused")
	private void setSampleModifier_KaraokeEntry(TextKaraokeEntry entry)
    {
    	m_textKaraoke.setKaraokeEntry(entry);
    }
 // This method is called by native.
    @SuppressWarnings("unused")
	private void setSampleModifier_HyperText(short startOffset, short endOffset, byte[] byteURL, byte[] byteAlt)
    {
    	if(m_textHyperText != null)
    		m_textHyperText = null;
    	
    	String strURL = null;
    	String strAlt = null;
    	
    	try {
			strURL = new String(byteURL, 0, byteURL.length, "UTF-8");
	    	strAlt = new String(byteAlt, 0, byteAlt.length, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e)
		{
			e.printStackTrace();
		}
    	
    	m_textHyperText = new TextHyperText(startOffset, endOffset, strURL, strAlt);
    }
 // This method is called by native.
    @SuppressWarnings("unused")
	private void setMatrix(float[] matrix)
    {
    	m_Matrix = matrix;
    }
 // This method is called by native.
    @SuppressWarnings("unused")
	private void setSample_FontTable(int fontID, byte[] fontName)
    {
    	m_fontTableLable.add(fontName.toString());
    	m_fontTableIndex.add(fontID);
    }
 // This method is called by native.
    @SuppressWarnings("unused")
	private void setSampleModifier_General(int sampleModifierID, int uUserData1, int uUserData2, int uUserData3, int uUserData4)
    {
    	NexLog.d("NexClosedCaption", "Call setSampleModifier_General, id : " + sampleModifierID + " " + uUserData1 + " " + uUserData2 + " " + uUserData3 + " " + uUserData4);
    	switch(sampleModifierID)
    	{
	    	case SampleModifier_TEXTSTYLE:
	    	{
	    		break;
	    	}
	    	case SampleModifier_TEXTHIGHLIGHT:
	    	{
	    		if(m_textHighlight != null)
	    		{
	    			m_textHighlight = null;
	    			//gc will collecting conventional data.
	    		}
	    		m_textHighlight = new TextHighlight((short)uUserData1, (short)uUserData2);
	    		break;
	    	}
	    	case SampleModifier_TEXTHILIGHTCOLOR:
	    	{
	    		if(m_textHighlightColor != null)
	    		{
	    			m_textHighlightColor = null;
	    			//gc will collecting conventional data.
	    		}
	    		m_textHighlightColor = new TextHighlightColor(uUserData1);
	    		break;
	    	}
	    	case SampleModifier_TEXTKARAOKE:
	    	{
	    		break;
	    	}
	    	case SampleModifier_TEXTSCROLLDELAY:
	    	{
	    		if(m_textScrollDelay != null)
	    		{
	    			m_textScrollDelay = null;
	    		}
	    		m_textScrollDelay = new TextScrollDelay(uUserData1);
	    		m_startTime = uUserData2;
	    		m_endTime = uUserData3;
	    		break;
	    	}
	    	case SampleModifier_TEXTHYPERTEXT:
	    	{
	    		break;
	    	}
	    	case SampleModifier_TEXTTEXTBOX://obj = null, t, l, b, r
	    	{
	    		m_TextBox = new Rect(uUserData2, uUserData1, uUserData4, uUserData3);//left top right bottom
	    		break;
	    	}
	    	case SampleModifier_TEXTBLINK:
	    	{
	    		m_textBlink.add(new TextBlink((short)uUserData1, (short)uUserData2));
	    		break;
	    	}
	    	case SampleModifier_TEXTTEXTWRAP://obj = null, flag
	    	{
	    		m_textWrap = TextWrap.fromValue(uUserData1);
	    		break;
	    	}
	    	case SampleModifier_VerticalJustification:
	    	{
	    		m_VerticalJustification = uUserData1;
	    		break;
	    	}
	    	case SampleModifier_HorizontalJustification:
	    	{
	    		m_HorizontalJustification = uUserData1;
	    		break;
	    	}
	    	case  SampleModifier_ScrollIN:
	    	{
	    		if(uUserData1 != 0)
	    			isScrollIn = true;
	    		else
	    			isScrollIn = false;
	    		break;
	    	}
	    	case SampleModifier_ScrollOUT:
	    	{
	    		if(uUserData1 != 0)
	    			isScrollOut = true;
	    		else
	    			isScrollOut = false;
	    		break;
	    	}
	    	case SampleModifier_ScrollDirection:
	    	{
	    		isScrollDirection = uUserData1;
	    		break;
	    	}
	    	case SampleModifier_ContinuousKaraoke:
	    	{
	    		if(uUserData1 != 0)
	    			isContinuousKaraoke = true;
	    		else
	    			isContinuousKaraoke = false;
	    		break;
	    	}
	    	case SampleModifier_WriteVertically:
	    	{
	    		if(uUserData1 != 0)
	    			isWriteVertically = true;
	    		else
	    			isWriteVertically = false;
	    		break;
	    	}
	    	case SampleModifier_FillTextRegion:
	    	{
	    		if(uUserData1 != 0)
	    			isFillTextRegion = true;
	    		else
	    			isFillTextRegion = false;
	    		break;
	    	}
	    	default:
	    	{
	    		break;
	    	}
    	}
    }
	/**
	 * \brief This property gets the current TextStyle class.
	 * 
	 * \returns The current TextStyle class.
	 */
    public TextStyle getTextStyle()
    {
    	return m_textStyle;
    }
    /**
	 * \brief This property gets the current TextHighlight class.
	 * 
	 * \returns The current TextHighlight class.
	 */
    public TextHighlight getTextHighlight()
    {
    	return m_textHighlight;
    }
    /**
	 * \brief This property gets the current TextHighlightColor class.
	 * 
	 * \returns The current TextHighlightColor class.
	 */
    public TextHighlightColor getTextHighlightColor()
    {
    	return m_textHighlightColor;
    }
    /**
	 * \brief This property gets the current TextKaraoke class.
	 * 
	 * \returns The current TextKaraoke class.
	 */
    public TextKaraoke getTextKaraoke()
    {
    	return m_textKaraoke;
    }
    /**
	 * \brief This property gets the current TextScrollDelay class.
	 * 
	 * \returns The current TextScrollDelay class.
	 */
    public TextScrollDelay getTextScrollDelay()
    {
    	return m_textScrollDelay;
    }
    /**
	 * \brief This property gets the current TextHyperText class.
	 * 
	 * \returns The current TextHyperText class.
	 */
    public TextHyperText getTextHyperText()
    {
    	return m_textHyperText;
    }
    /**
	 * \brief This property gets the rectangle box for text drawing.
	 * 
	 * \returns The rectangle box.
	 */
    public Rect getTextBox()
    {
    	return m_TextBox;
    }
    /**
	 * \brief This property gets the array of the current TextBlink class.
	 * 
	 * \returns The array of the current TextBlink class.
	 */
    public TextBlink[] getTextBlink()
    {
    	TextBlink[] blink = new TextBlink[m_textBlink.size()];
    	try{
    		m_textBlink.toArray(blink);
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	return blink;
    }
    /**
	 * \brief This property gets the current TextWrap class.
	 * 
	 * \returns The current TextWrap class.
	 */
    public TextWrap getTextWrap()
    {
    	return m_textWrap;
    }
    /**
	 * \brief This property gets the coordination of the rectangle text box.
	 * 
	 * \returns The coordination of the rectangle text box.
	 */
    public int[] getTextboxCoordinatesFor3GPPTT()
    {
    	int[] texCoord = new int[4];
    	texCoord[0] = m_3gppTTRegionTX;
    	texCoord[1] = m_3gppTTRegionTY;
    	texCoord[2] = m_3gppTTRegionWidth;
    	texCoord[3] = m_3gppTTRegionHeight;
    	
    	return texCoord;
    }
    
    /**
	 * \brief This property gets default color of the text.
	 * 
	 * \returns The color of the  text.
	 */
    public int getForegroundColorFor3GPPTT()
    {
    	return m_3gppTTTextColor;
    }
    /**
   	 * \brief This property gets color of the rectangle text box.
   	 * 
   	 * \returns The color of the rectangle text box.
   	 */
    public int getBackgroundColorFor3GPPTT()
    {
    	return m_3gppTTBGColor;
    }
    /**
   	 * \brief This property gets data of the text.
   	 * 
   	 * \returns Data of the text.
   	 */
    public byte[] getTextDataFor3GPPTT()
    {
    	return m_3gppTT_TextBuffer;
    }
    
    @Deprecated
    public float[] getMatrix()
    {
    	return m_Matrix;
    }
    /**
   	 * \brief This property justify the alignment of the text vertically, which are top, centre and bottom.
   	 * 
   	 * \returns The alignment of the text. This will be one of the following values:
     *              - <b>Top		(0)</b>
     *              - <b>Centre		(1)</b>
     *              - <b>Bottom		(-1)</b>
   	 */
    public int getVerticalJustification()
    {
    	return m_VerticalJustification;
    }
    /**
   	 * \brief This property justify the alignment of the text horizontally, which are left, centre and right.
   	 * 
   	 * \returns The alignment of the text. This will be one of the following values:
     *              - <b>Left		(0)</b>
     *              - <b>Centre		(1)</b>
     *              - <b>Right		(-1)</b>
   	 */
    public int getHorizontalJustification()
    {
    	return m_HorizontalJustification;
    }
    /**
	 * \brief This property specifies the text entering from outside the rectangle box.
	 * 
	 * \returns TRUE if <b>ON</b>, FALSE if OFF.
	 */
    public boolean getScrollIn()
    {
    	return isScrollIn;
    }
    /**
	 * \brief This property specifies the text exiting from inside the rectangle box.
	 * 
	 * \returns TRUE if <b>ON</b>, FALSE if OFF.
	 */
    public boolean getScrollOut()
    {
    	return isScrollOut;
    }
    /**
   	 * \brief This property specifies the direction of the text entering or exiting the rectangle text box.
   	 * 
   	 * \returns The direction of the text. This will be one of the following values:
     *              - <b>Bottom to top		(0)</b>
     *              - <b>Right to left		(1)</b>
     *              - <b>Top to bottom		(2)</b>
     *              - <b>Left to right		(3)</b>
   	 */
    public int getScrollDirection()
    {
    	return isScrollDirection;
    }
    /**
	 * \brief This property specifies the ContinuousKaraoke.
	 * 
	 * \returns TRUE if <b>YES</b>, FALSE if No.
	 */
    public boolean getContinuousKaraoke()
    {
    	return isContinuousKaraoke;
    }
    /**
	 * \brief This property vertical position the text will be written in.
	 * 
	 * \returns TRUE if <b>YES</b>, FALSE if No.
	 */
    public boolean getWritingVertically()
    {
    	return isWriteVertically;
    }
    /**
	 * \brief This property bacground region of the text.
	 * 
	 * \returns TRUE if <b>YES</b>, FALSE if No.
	 */
    public boolean getFillTextRegion()
    {
    	return isFillTextRegion;
    }
    
    /**
  	 * \brief This property describes the font index of the text. See spec 5.16 font table.
  	 */
    public int[] getFontTableIndex()
    {
    	int[] fontIndex = new int[m_fontTableIndex.size()];
    	Integer[] iIndex = new Integer[m_fontTableIndex.size()];
    	m_fontTableIndex.toArray(iIndex);
    	for(int i=0;i<iIndex.length;i++)
    	{
    		fontIndex[i] = iIndex[i].intValue();
    	}
    	return fontIndex;
    }
    
    /**
  	 * \brief This property specifies the caption time of the text.
  	 */
    public int[] getCaptionTime()
    {
    	int[] cTime = new int [2];
    	cTime[0] = m_startTime;
    	cTime[1] = m_endTime;
    	return cTime;
    }

    //CFF TTML Style sheet.
    //Please refer to TIMED TEXT MARKUP LANGUAGE 1.0 SPEC
    //You can see these specs at http://www.w3.org/TR/ttaf1-dfxp

    
    //8.2.6 tts:displayAlign
    /**
     * \brief This enumeration determines the display alignment of CFF timed text (TTML) in content.
     * 
     * It corresponds to the \c tts:displayAlign attribute, and indicates how timed text should be
     * aligned vertically in blocks on the screen when they are displayed.
     * 
     * \since version 6.0
     */
    public enum TTML_DisplayAlign
    {
    	/** The timed text (TTML) should be aligned in the default alignment. */
    	Default(0),
    	/** The timed text (TTML) should be aligned vertically "before" the center of the display block. */
    	Before(1),
    	/** The timed text (TTML) should be centered vertically in the display block. */
    	Center(2), 
    	/** The block of timed text (TTML) should be aligned vertically "after" the center of the display block. */
    	After(3);
    	
    	private int m_value;
    	TTML_DisplayAlign(int value)
    	{
    		m_value = value;
    	}
    	public int getValue()
    	{
    		return m_value;
    	}
    	
    	public static TTML_DisplayAlign fromValue( int value ) {
			for( TTML_DisplayAlign item : values() ) {
				if( item.getValue() == value )
					return item;
			}
			return null;
		}
    }
    
    //8.2.10 tts:fontStyle
    /**
     * \brief This enumeration determines the font style of CFF timed text (TTML) in content.
     * 
     * It corresponds to the \c tts:fontStyle attribute, and indicates how timed text font should be displayed.
     * 
     * \since version 6.0
     */
    public enum TTML_Fontstyle
    {
    	/** The timed text (TTML) font should be displayed as default. */
    	Default(0),
    	/** The timed text (TTML) font should be displayed normally. */
    	Normal(1),
    	/** The timed text (TTML) font should be displayed in <i>italics</i>. */
    	Italic(2), 
    	/** The timed text (TTML) font should be displayed with a shear transformation at an oblique angle. */
    	Oblique(3);
    	
    	private int m_value;
    	private TTML_Fontstyle(int value)
    	{
    		m_value = value;
    	}
    	public int getValue()
    	{
    		return m_value;
    	}
    	
    	public static TTML_Fontstyle fromValue( int value ) {
			for( TTML_Fontstyle item : values() ) {
				if( item.getValue() == value )
					return item;
			}
			return null;
		}
    }
    
    //8.2.18 tts:textAlign
    /**
     * \brief This enumeration determines the horizontal alignment of timed text (TTML) in content.
     * 
     * It corresponds to the \c tts:textAlign attribute, and indicates how timed text should be aligned
     * horizontally in display blocks of the subtitles.
     * 
     * \since version 6.0
     */
    public enum TTML_TextAlign
    {
    	/** The timed text (TTML) should be displayed in the default alignment. */
    	Default(0),
    	/** The timed text (TTML) should be displayed aligned with the start of the text block.*/
		Start(1),
		/** The timed text (TTML) should be displayed aligned to the left of the text block.*/
		Left(2),
		/** The timed text (TTML) should be displayed aligned with the center of the text block.*/
		Center(3),
		/** The timed text (TTML) should be displayed aligned to the right of the text block.*/
		Right(4),
		/** The timed text (TTML) should be displayed aligned with the end of the text block.*/
		End(5);
		
		private int m_value;
    	private TTML_TextAlign(int value)
    	{
    		m_value = value;
    	}
    	public int getValue()
    	{
    		return m_value;
    	}
    	
    	public static TTML_TextAlign fromValue( int value ) {
			for( TTML_TextAlign item : values() ) {
				if( item.getValue() == value )
					return item;
			}
			return null;
		}
    }
    
    //8.2.21 tts:unicodeBidi
    /**
     * \brief This enumeration specifies a directional embedding or override of text direction for timed text (TTML) in content.
     * 
     * It corresponds to the \c tts:unicodeBidi attribute, and relates to how text will be displayed according
     * to the Unicode bidirectional algorithm.
     * 
     * \since version 6.0
     */
    public enum TTML_UnicodeBIDI
    {
    	/**No attribute was specified; the timed text (TTML) should be displayed as normal by default.*/
    	Default(0),
    	/** The timed text (TTML) should be displayed as normal.*/
		Normal(1),
		/** The timed text (TTML) should be displayed with embedded directionality. */
		Embed(2),
		/** The timed text (TTML) should be displayed overriding the normal directionality of the text script.
		 * This would allow, for example, the characters in Latin script (which are normally displayed left-to-right)
		 * to be displayed right-to-left instead.*/
		BidiOverride(3);
		
		private int m_value;
    	private TTML_UnicodeBIDI(int value)
    	{
    		m_value = value;
    	}
    	public int getValue()
    	{
    		return m_value;
    	}
    	
    	public static TTML_UnicodeBIDI fromValue( int value ) {
			for( TTML_UnicodeBIDI item : values() ) {
				if( item.getValue() == value )
					return item;
			}
			return null;
		}
    }
    
    //8.2.24 tts:writingMode
    /**
     * \brief  This enumeration specifies the mode in which timed text (TTML) in content will be displayed.
     * 
     * This corresponds to the \c tts:writingMode, where the modes indicate how timed text and 
     * timed text blocks will be arranged when displayed.  This allows different language text to be 
     * displayed for example vertically, or from right-to-left when required.
     * 
     * \since version 6.0
     */
    public enum TTML_WritingMode
    {
    	/** The timed text (TTML) should be displayed as lrtb (left-to-right, top-to-bottom) as default.*/
    	Default(0),
    	/** The timed text (TTML) should be displayed as left-to-right, top-to-bottom, as with standard Latin text.*/
		lrtb(1),
		/** The timed text (TTML) should be displayed right-to-left, top-to-bottom, as with Hebrew script.*/
		rltb(2),
		/** The timed text (TTML) should be displayed top-to-bottom, right-to-left.  This is for displaying vertically 
		 * oriented scripts, as in some Asian languages. */
		tbrl(3),
		/** The timed text (TTML) should be displayed top-to-bottom, left-to-right.  This is for displaying vertically 
		 * oriented scripts, as in some Asian languages. */
		tblr(4),
		/** The timed text (TTML) should be displayed left-to-right.*/
		lr(5),
		/** The timed text (TTML) should be displayed right-to-left.*/
		rl(6),
		/** The timed text (TTML) should be displayed top-to-bottom. */
		tb(7);
		
		private int m_value;
    	private TTML_WritingMode(int value)
    	{
    		m_value = value;
    	}
    	public int getValue()
    	{
    		return m_value;
    	}
    	
    	public static TTML_WritingMode fromValue( int value ) {
			for( TTML_WritingMode item : values() ) {
				if( item.getValue() == value )
					return item;
			}
			return null;
		}
    }
    
    //8.3.9 <length>
    /**
     * \brief This enumeration specifies which type of length is being used in 
     *        the style properties of a content's timed text (TTML).
     * 
     * It corresponds to the units in the \c <length> expression for timed text TTML style property values.
     * 
     * \since version 6.0
     */
    public enum TTML_LengthType
    {
    	/** No unit of length is specified, which is an error.*/
    	Default(0),
    	/** The length is expressed as a percent. */
		percent(1),
		/** The length is expressed in pixels. */
		px(2),
		/** The length is expressed in pixels, but is the font dimension length in the direction specified when
		 * relative to a font with a size expressed as two unequal length measures. */
		em(3),
		/** The length is expressed in cells.*/
		c(4); 
		
		private int m_value;
    	private TTML_LengthType(int value)
    	{
    		m_value = value;
    	}
    	public int getValue()
    	{
    		return m_value;
    	}
    	
    	public static TTML_LengthType fromValue( int value ) {
			for( TTML_LengthType item : values() ) {
				if( item.getValue() == value )
					return item;
			}
			return null;
		}
    }
    

    /**
     * \brief  This class describes the length of every timed text (TTML) attribute.
     * 
     * This will be determined based on the units of the "type" of length, as specified by
     * TTML_LengthType.
     * 
     * \since version 6.0
     */
    public class TTML_StyleLength
    {
    	public float length;
    	public TTML_LengthType lengthType;

		private TTML_StyleLength(float length, int type)
		{
			this.length = length;
			this.lengthType = TTML_LengthType.fromValue(type);
		}

		@Deprecated
    	private TTML_StyleLength(int length, int type)
    	{
    		this.length = (float)length;
    		this.lengthType = TTML_LengthType.fromValue(type);
    	}

		@Deprecated
    	public int getLength()
    	{
    		return (int)this.length;
    	}

		@Deprecated
    	public TTML_LengthType getType()
    	{
    		return lengthType;
    	}
    }
    
    //8.2.20 tts:textOutline
    /**
     * \brief  This class determines how timed text (TTML) in content should be displayed in text outline format. 
     * 
     * It corresponds to the \c tts:textOutline attribute, and can indicate the color and thickness of the outline, 
     * as well as the blur radius.
     * 
     * \since version 6.0
     */
    public class TTML_TextOutlineStyleLength
    {
    	int color;
    	TTML_StyleLength lengthType1;
    	TTML_StyleLength lengthType2;
    	
    	private TTML_TextOutlineStyleLength(int color,float lengthType1len, int lengthType1Type, float lengthType2len, int lengthType2Type)
    	{
    		this.color = color;
    		lengthType1 = new TTML_StyleLength(lengthType1len, lengthType1Type);
    		lengthType2 = new TTML_StyleLength(lengthType2len, lengthType2Type);
    	}

		private TTML_TextOutlineStyleLength(int color,TTML_StyleLength lengthType1, TTML_StyleLength lengthType2)
		{
			this.color = color;
			this.lengthType1 = lengthType1;
			this.lengthType2 = lengthType2;
		}

    	/**
    	 * \brief This method gets the color to be used to outline timed text (TTML) in Text Outline style, if present.
    	 */
    	public int getColor()
    	{
    		return this.color;
    	}
    	/**
    	 * \brief This method gets the thickness of the outline of timed text (TTML) in Text Outline style. 
    	 */
    	public TTML_StyleLength getType1()
    	{
    		return lengthType1;
    	}
    	/**
    	 * \brief  This method gets the blur radius of the outline of timed text (TTML) in Text Outline style, if present.
    	 */
    	public TTML_StyleLength getType2()
    	{
    		return lengthType2;
    	}
    }

	protected class TTMLRenderingData {
		int startTime;
		int endTime;
		int clearTime;

		int direction;
		int display;
		int showBackground;
		int visibility;
		int wrapOption;
		int overflow;
		int zIndex;
		int extentBackground;
        float opacity;

		int imageLen;
		byte[] image = null;

		String language;
		String fontFamily;

		TTML_DisplayAlign displayAlign = null;
		TTML_TextAlign textAlign = null;
		TTML_UnicodeBIDI unicodeBidi = null;
		TTML_WritingMode writingMode = null;

		TTML_StyleLength extentWidth = null;
		TTML_StyleLength extentHeight = null;

		TTML_StyleLength[] fontSize = null;
		TTML_StyleLength[] origin = null;
		TTML_StyleLength[] padding = null;

		TTML_TextOutlineStyleLength textOutline = null;

		public class TTMLNodeData {
			byte[] text;
			int bgColor;
			int fontColor;
			int fontWeight;

			TTML_Fontstyle fontStyle = null;
			int textDecoration;
		}

		ArrayList<TTMLNodeData> nodes = null;
	}

	TTMLRenderingData ttmlRenderingData = null;

    private int[] m_TTML_time = new int[3];

	/**
	 * \brief  This method gets the time information of the timed text (TTML) in the content.
	 *
	 * \returns  An int array with the time information of the timed text.
	 * The first index of the array is the start time to draw the timed text, the second is the final time to draw the timed text, and the third is the clear time.
	 *
	 * \since version 6.59
	 */

	public int[] getTTMLTimeData2Array() {
		if (null == ttmlRenderingData) {
			return null;
		}

		m_TTML_time[0] = ttmlRenderingData.startTime;
		m_TTML_time[1] = ttmlRenderingData.endTime;
		m_TTML_time[2] = ttmlRenderingData.clearTime;

		return m_TTML_time;
	}

	//Call from Native
	@SuppressWarnings("unused")
	private void setTTMLLanguagesInfo(String captionLanguage)
	{
		ttmlRenderingData.language = captionLanguage;
	}

	/**
	 * \brief  This method is used to handle the text data for timed text (TTML) in content.
	 * 
	 * \returns  A byte array with the text data for the content's timed text (TTML).
	 * 
	 * \since version 6.0
	 */
	
	public byte[] getTextDataforTTML()
	{
		byte[] result = null;

		if (null != ttmlRenderingData && null != ttmlRenderingData.nodes) {
			StringBuilder string = null;
			for (TTMLRenderingData.TTMLNodeData node : ttmlRenderingData.nodes) {
				if (null != node.text) {
					if (null == string) {
						try {
							string = new StringBuilder(new String(node.text, "UTF-8"));
						}catch(UnsupportedEncodingException ignored) {

						}
					} else {
						String text = null;
						try {
							text = new String(node.text, "UTF-8");
						}catch(UnsupportedEncodingException ignored) {

						}
						if(text != null){
							string.append(text);
						}
					}
				}
			}

			if (null != string) {
				result = string.toString().getBytes();
			}
		}

		return result;
	}
	
	//8.2.2 tts:backgroundColor
	/**
	 * \brief  This method gets the background color for timed text (TTML) in content.
	 * 
	 * It corresponds to the \c tts:backgroundColor attribute, and specifies the color of the background
	 * region on which timed text (TTML) should be displayed.
	 * 
	 * \returns  The background color of the timed text (TTML) as an \c integer.
	 * 
	 * \since version 6.0
	 */
	public int getBGColorforTTML()
	{
		int bgColor = 0;
		if (null != ttmlRenderingData) {
			if (!ttmlRenderingData.nodes.isEmpty()) {
				bgColor = ttmlRenderingData.nodes.get(0).bgColor;
			}
		}
		return bgColor;
	}
	
	//8.2.3 tts:color
	/**
	 * \brief  This method gets the font color for timed text (TTML) in content.
	 * 
	 * It corresponds to the \c tts:color attribute, and specifies the font color that should be
	 * displayed for timed text (TTML).
	 * 
	 * \returns  The foreground color of the timed text (TTML) as an \c integer.
	 * 
	 * \since version 6.0
	 */
	public int getFontColorforTTML()
	{
		int fontColor = 0;
		if (null != ttmlRenderingData) {
			if (!ttmlRenderingData.nodes.isEmpty()) {
				fontColor = ttmlRenderingData.nodes.get(0).fontColor;
			}
		}
		return fontColor;
	}
	
	//8.2.25 tts:zIndex
	/**
	 * \brief This method gets the zIndex for timed text (TTML) in content.
	 * 
	 * It corresponds to the \c tts:zIndex attribute, and defines the front-to-back order of timed
	 * text (TTML) region areas if they overlap.  For example, if several blocks of timed text (TTML) were
	 * to be staggered, layered, and displayed at once, the block with the zIndex of 2 would be in front (like the top
	 * sheet of paper in a pile), the block with zIndex of 1 would be next, and the 
	 * block with zIndex of 0 would be under the others (like the bottom sheet of paper in pile).
	 * 
	 * \returns  The \c zIndex of the timed text (TTML) as an \c int.
	 * 
	 * \since version 6.0
	 */
	public int getzIndexforTTML()
	{
		int zIndex = 0;
		if (null != ttmlRenderingData) {
			zIndex = ttmlRenderingData.zIndex;
		}
		return zIndex;
	}
	
	//8.2.13 tts:opacity. 0 to 1.
	/**
	 * \brief  This method gets the opacity of the timed text (TTML) in content.
	 * 
	 * It corresponds to the \c tts:opacity attribute, and specifies the opacity or transparency of
	 * a region of timed text (TTML) in content.
	 * 
	 * It should be a value between 0 and 1, where 0 is fully transparent and 1 is opaque.
	 * 
	 * \returns  The opacity of the timed text (TTML) as a \c float between 0 and 1.
	 * 
	 * \since version 6.0
	 */
	public float getOpacityforTTML()
	{
		float opacity = 0;
		if (null != ttmlRenderingData) {
            opacity = ttmlRenderingData.opacity;
		}
		return opacity;
	}
	
	//8.2.4 tts:direction
	/**
	 * \brief  This method determines the direction of timed text (TTML) in content.
	 * 
	 * It corresponds to the \c tts:direction attribute, which indicates whether timed text should
	 * be displayed from left-to-right or from right-to-left.
	 * 
	 * \returns  \c TRUE if text is to be displayed left-to-right like standard Latin script, or 
	 *           \c FALSE if it should be displayed right-to-left.
	 * 
	 * \since version 6.0
	 */
	public boolean isWriteLefttoRightforTTML()
	{
		boolean direction = false;
		if (null != ttmlRenderingData) {
			direction = ttmlRenderingData.direction > 0;
		}

		return direction;
	}
	
	//8.2.5 tts:display
	/**
	 * \brief  This method determines whether timed text (TTML) in content is displayed automatically or not.
	 * 
	 * It corresponds to the \c tts:display attribute, and allows certain text to be displayed in timed intervals
	 * while other text remains on the screen.
	 * 
	 * \returns  \c TRUE if timed text (TTML) is labeled \c auto, or \c FALSE if it is labeled \c none.
	 * 
	 * \since version 6.0
	 */
	public boolean isDisplayAutoforTTML()
	{
		boolean display = false;
		if (null != ttmlRenderingData) {
			display = ttmlRenderingData.display > 0;
		}

		return display;
	}
	
	//8.2.11 tts:fontWeight
	/**
	 * \brief  This method determines whether or not timed text (TTML) in content should be displayed in <b>bold</b>.
	 * 
	 * It corresponds to the \c tts:fontWeight attribute.
	 * 
	 * \returns  \c TRUE if the timed text (TTML) should be displayed in <b>bold</b> or \c FALSE if it should be
	 *           displayed normally.
	 * 
	 * \since version 6.0
	 */
	public boolean isBoldforTTML()
	{
		boolean bold = false;

		if (null != ttmlRenderingData) {
			if (!ttmlRenderingData.nodes.isEmpty()) {
				bold = ttmlRenderingData.nodes.get(0).fontWeight == 1;
			}
		}

		return bold;
	}
	
	//8.2.15 tts:overflow
	/**
	 * \brief  This method determines whether or not timed text (TTML) in content that overflows the displayed block
	 * should be visible or not.
	 * 
	 * It corresponds to the \c tts:overflow attribute, and determines whether timed text that overflows the display 
	 * region will be visible or not.
	 * 
	 * \returns  \c TRUE if overflowing timed text (TTML) should be visible, \c FALSE if the overflow should be hidden.
	 * 
	 * \since version 6.0
	 */
	public boolean isOverflowVisibleforTTML()
	{
		boolean overflow = false;

		if (null != ttmlRenderingData) {
			overflow = ttmlRenderingData.overflow == 1;
			}

		return overflow;
	}
	
	//8.2.17 tts:showBackground
	/**
	 * \brief  This method determines when the background of timed text (TTML) in content should be visible.
	 * 
	 * It corresponds to the \c tts:showBackground attribute, indicates whether the background should always be
	 * visible or should be visible only when there is timed text displayed.
	 * 
	 * \returns \c TRUE if the background should always be visible, or \c FALSE when background should only be visible
         * when text is displayed.
	 * 
	 * \since version 6.0
	 */
	public boolean isShowBackgroundforTTML()
	{
		boolean showBackground = false;

		if (null != ttmlRenderingData) {
			showBackground = ttmlRenderingData.showBackground == 1;
		}

		return showBackground;
	}
	
	//8.2.19 tts:textDecoration
	/**
	 * \brief  This method determines if any text decoration should be displayed in timed text (TTML) in content.
	 * 
	 * It corresponds to the \c tts:textDecoration attribute, and allows text features like \c underline, 
	 * \c overline, and \c lineThrough to be displayed in timed text (TTML).
	 * 
	 * \returns \c TRUE if text should be underlined, \c FALSE if there is no text decoration.
	 * 
	 * \since version 6.0
	 */
	public boolean isTextDecorationforTTML()
	{
		boolean textDecoration = false;

		if (null != ttmlRenderingData) {
			textDecoration = ttmlRenderingData.nodes.get(0).textDecoration == 1;
		}

		return textDecoration;
	}
	
	//8.2.22 tts:visibility
	/**
	 * \brief  This method determines whether or not timed text (TTML) in content should be displayed (visible) or hidden.
	 * 
	 * It corresponds to the \c tts:visibility attribute, and allows timed text to be displayed and hidden at different
	 * intervals as desired.
	 * 
	 * \returns \c TRUE when text should be visible, \c FALSE when it should be hidden.
	 * 
	 * \since version 6.0
	 */
	public boolean isVisibilityforTTML()
	{
		boolean visibility = false;

		if (null != ttmlRenderingData) {
			visibility = ttmlRenderingData.visibility == 1;
		}

		return visibility;
	}
	
	//8.2.23 tts:wrapOption
	/**
	 * \brief  This method determines whether timed text (TTML) in content should wrap automatically to the following line
	 *         or not.
	 * 
	 * It corresponds to the \c tts:wrapOption attribute and determines whether line wrapping will happen automatically 
	 * when timed text (TTML) reaches the end of the display region, or if the text will only wrap to the next line on
	 * included line breaks.
	 * 
	 * \returns \c TRUE if timed text (TTML) should wrap automatically within a displayed region, or \c FALSE if
	 *          it should only wrap on included line breaks.
	 * 
	 * \since version 6.0
	 */
	public boolean isWrapforTTML()
	{
		boolean wrapOption = false;

		if (null != ttmlRenderingData) {
			wrapOption = ttmlRenderingData.wrapOption == 1;
		}

		return wrapOption;
	}
	
	//8.2.8 tts:fontFamily
	/**
	 * \brief  This method determines the font family to be used for timed text (TTML) in content.
	 * 
	 * It corresponds to the \c tts:fontFamily attribute.
	 * 
	 * \returns  The font family to be used for timed text (TTML) as a \c String.
	 * 
	 * \since version 6.0
	 */
	public String getFontFamilyNameforTTML()
	{
		String fontFamily = null;
		if (null != ttmlRenderingData) {
			fontFamily = ttmlRenderingData.fontFamily;
		}
		return fontFamily;
	}
	
	//8.2.6 tts:displayAlign
	/**
	 * \brief  This method gets the display alignment for timed text (TTML) in content.
	 * 
	 * It corresponds to the \c tts:displayAlign attribute and returns the relevant alignment
	 * in the display region that is determined in the TTML_DisplayAlign enumeration.
	 * 
	 * \return  The display alignment, which will be one of:
	 *            - Default = 0
	 *            - Before = 1
	 *            - Center = 2
	 *            - After = 3
	 *            
	 * \see  TTML_DisplayAlign for more information.
	 * 
	 * \since version 6.0
	 */
	public TTML_DisplayAlign getDisplayAlignforTTML()
	{
		TTML_DisplayAlign displayAlign = null;
		if (null != ttmlRenderingData) {
			displayAlign = ttmlRenderingData.displayAlign;
		}
		return displayAlign;
	}
	
	//8.2.10 tts:fontStyle
	/**
	 * \brief This method gets the font style for timed text (TTML) in content.
	 * 
	 * It corresponds to the \c tts:fontStyle attribute and indicates how timed text should
	 * be displayed.
	 * 
	 * \returns The font style for the timed text (TTML) in content.  This will be one of:
	 *            - Default (0)
	 *            - Normal (1)
	 *            - Italic (2)
	 *            - Oblique (3)
	 *            
	 * \see  TTML_Fontstyle for more information.
	 * 
	 * \since version 6.0
	 */
	public TTML_Fontstyle getFontStyleforTTML()
	{
		TTML_Fontstyle fontstyle = null;
		if (null != ttmlRenderingData) {
			fontstyle = ttmlRenderingData.nodes.get(0).fontStyle;
		}
		return fontstyle;
	}
	
	//8.2.18 tts:textAlign
	/**
	 * \brief  This method gets the horizontal text alignment of timed text (TTML) in content.
	 * 
	 * It corresponds to the \c tts:textAlign attribute, and indicates how timed text should be aligned
	 * horizontally in the display region when displayed.
	 * 
	 * \returns  The horizontal text alignment as a TTML_TextAlign object, which will be one of:
	 *             - Default (0)
	 *             - Start (1)
	 *             - Left (2)
	 *             - Center (3)
	 *             - Right (4)
	 *             - End (5)
	 * 
	 * \see TTML_TextAlign for more information.
	 * 
	 * \since version 6.0
	 */
	public TTML_TextAlign getTextAlignforTTML()
	{
		TTML_TextAlign textAlign = null;
		if (null != ttmlRenderingData) {
			textAlign = ttmlRenderingData.textAlign;
		}
		return textAlign;
	}
	
	//8.2.21 tts:unicodeBidi
	/**
	 * \brief  This method gets the directionality of timed text (TTML) in content.
	 * 
	 * It corresponds to the \c tts:unicodeBidi attribute, and either defines and override of the Unicode directionality
	 * or a directional embedding.
	 * 
	 * \returns  The directionality of timed text (TTML) as a TTML_UnicodeBIDI.  This will be one of:
	 *             - Default (0)
	 *             - Normal (1)
	 *             - Embed (2)
	 *             - BidiOverride (3)
	 * 
	 * \see  TTML_UnicodeBIDI for more information.
	 * 
	 * \since version 6.0
	 */
	public TTML_UnicodeBIDI getUnicodeBIDIforTTML()
	{
		TTML_UnicodeBIDI unicodeBIDI = null;
		if (null != ttmlRenderingData) {
			unicodeBIDI = ttmlRenderingData.unicodeBidi;
		}
		return unicodeBIDI;
	}
	
	//8.2.24 tts:writingMode
	/**
	 * \brief  This method gets the writing mode of timed text (TTML) in content.
	 * 
	 * It corresponds to the \c tts:writingMode attribute and indicates how timed text (TTML) should be
	 * displayed in the text display region.
	 * 
	 * \returns  The writing mode of timed text (TTML) as a TTML_WritingMode object.  This will be one of:
	 *             - Default (0)
	 *             - Left-to-Right, Top-to-Bottom (1)
	 *             - Right-to-Left, Top-to-Bottom (2)
	 *             - Top-to-Bottom, Right-to-Left (3)
	 *             - Top-to-Bottom, Left-to-Right (4)
	 *             - Left-to-Right (5)
	 *             - Right-to-Left (6)
	 *             - Top-to-Bottom (7)
	 * 
	 * \see TTML_WritingMode for more information.
	 * 
	 * \since version 6.0
	 */
	public TTML_WritingMode getWritingModeforTTML()
	{
		TTML_WritingMode writingMode = null;
		if (null != ttmlRenderingData) {
			writingMode = ttmlRenderingData.writingMode;
		}
		return writingMode;
	}
	
	//8.2.7 tts:extent
	/**
	 * \brief  This method gets the width of the region area where timed text (TTML) in content is to be displayed.
	 * 
	 * It corresponds to the width dimension specified in \c tts:extent attribute, and may also be the width of the root container region.
	 * 
	 * Note that if the \c tts:extent 
	 * attribute is set to \c auto, the display region should be considered to have the same width as
	 * the root container extent.
	 * 
	 * \returns  The width of the text region as a TTML_StyleLength object.
	 * 
	 * \since version 6.0
	 */
	public TTML_StyleLength getExtentWidth()
	{
		TTML_StyleLength extentWidth = null;
		if (null != ttmlRenderingData) {
			extentWidth = ttmlRenderingData.extentWidth;
		}
		return extentWidth;
	}
	
	//8.2.7 tts:extent
	/**
	 * \brief  This method gets the height of the region area where timed text (TTML) in content is to be displayed.
	 * 
	 * It corresponds to the height dimension specified in \c tts:extent attribute, and may also be the height of the root container region.
	 * 
	 * Note that if the \c tts:extent 
	 * attribute is set to \c auto, the display region should be considered to have the same height as
	 * the root container extent.
	 * 
	 * \returns  The height of the text region as a TTML_StyleLength object.
	 * 
	 * \since version 6.0
	 */
	public TTML_StyleLength getExtentHeight()
	{
		TTML_StyleLength extentHeight = null;
		if (null != ttmlRenderingData) {
			extentHeight = ttmlRenderingData.extentHeight;
		}
		return extentHeight;
	}
	
	//8.2.9 tts:fontSize
	/**
	 * \brief  This method gets the font size of timed text (TTML) in content.
	 * 
	 * It corresponds to the \c tts:fontSize attribute, and indicates how large timed text (TTML) should
	 * be displayed.
	 * 
	 * \returns  The font size as a TTML_StyleLength array, where if only one length is specified, it is to be
	 *           used as the horizontal and vertical scaling of text glyph's square, and if two are specified, the
	 *           first length corresponds to the horizontal scaling and the second to the vertical scaling of the
	 *           text glyph.
	 * 
	 * \since version 6.0
	 */
	public TTML_StyleLength[] getFontSize()
	{
		TTML_StyleLength[] fontSize = null;
		if (null != ttmlRenderingData) {
			fontSize = ttmlRenderingData.fontSize;
		}
		return fontSize;
	}
	
	//8.2.12 tts:lineHeight
	/**
	 * \brief  This method gets the line height of timed text (TTML) in content.
	 * 
	 * It corresponds to the \c tts:lineHeight attribute, and can be used to vertically space out lines of
	 * timed text (TTML) when displayed.
	 * 
	 * \returns  The line height as a TTML_StyleLength object.
	 * 
	 * \since version 6.0
	 */
	public TTML_StyleLength getLineHeight()
	{
		//TODO:
		return null;
	}
	
	//8.2.14 tts:origin
	/**
	 * \brief  This method gets the origin of the text display region for timed text (TTML) in content with 
	 *         respect to the root container extent or external authoring context.
	 *  
	 *  This corresponds to the \c tts:origin attribute and will either be \c auto, where the origin can be
	 * treated as the same as the root container origin, or as two lengths, the first of which must be interpreted
	 * as the x coordinate and the second as the y coordinate of the origin of the text display region.
	 * 
	 * \returns  The origin of the text display region for timed text (TTML) as a TTML_StyleLength array.
	 * 
	 * \since version 6.0
	 */
	public TTML_StyleLength[] getOrigin()
	{
		TTML_StyleLength[] origin = null;
		if (null != ttmlRenderingData) {
			origin = ttmlRenderingData.origin;
		}
		return origin;
	}
	
	//8.2.16 tts:padding
	/**
	 * \brief  This method gets the padding to be included around timed text (TTML) in content.
	 * 
	 * It corresponds to the \c tts:padding attribute and can include up to four lengths, indicating the padding
	 * to be included around the timed text (TTML) when displayed.
	 * 
	 * If only one length is specified, it applies to all edges of the text display box.  If there are two
	 * lengths specified, the first applies to the "before" and "after" edges (the top and bottom edges of the text 
	 * box for typical Latin script) and the second to the "start" and "end" edges (or right and left edges for 
	 * typical Latin script).  If there are three length specifications, the first corresponds to the "before" edge, 
	 * the second to the "start" and "end" edges, and the third to the "after" edge.  Lastly, if four padding lengths 
	 * are provided, they should be applied to the before, end, after, and start edges in the corresponding order.
	 * 
	 * \returns  The available padding lengths as a TTML_StyleLength array.
	 * 
	 * \since version 6.0
	 */
	public TTML_StyleLength[] getPadding()
	{
		TTML_StyleLength[] padding = null;
		if (null != ttmlRenderingData) {
			padding = ttmlRenderingData.padding;
		}
		return padding;
	}
	
	//8.2.20 tts:textOutline
	/**
	 * \brief  This method gets the text outline style to be used to display timed text (TTML) in text outline.
	 * 
	 * It corresponds to the \c tts:textOutline attribute and is returned as a TTML_TextOutlineStyleLength object 
	 * including the color, outline thickness, and blur radius if present.
	 * 
	 * \returns  A TTML_TextOutlineStyleLength object with the relevant information to 
	 *           display timed text (TTML) in outline form.
	 * 
	 * \see TTML_TextOutlineStyleLength for more information.
	 * 
	 * \since version 6.0
	 */
	public TTML_TextOutlineStyleLength getTextOutline()
	{
		TTML_TextOutlineStyleLength textOutline = null;
		if (null != ttmlRenderingData) {
			textOutline = ttmlRenderingData.textOutline;
		}
		return textOutline;
	}

    //=========================================================================
    //WebVTT Rendering info
    //Please also see the WebVTT specifications at:  http://dev.w3.org/html5/webvtt/
    
    /**
     * \brief  This enumeration defines the text track cue span tags for how WebVTT text tracks should be displayed.
     *  
     * Additional details about WebVTT cue spans can be found in the WebVTT specifications at http://dev.w3.org/html5/webvtt/.
     * \since version 6.4
     * 
     */
    public enum WebVTT_CueSpanTag
    {
    	/** WebVTT cue span tag indicating the cue list. */
    	LIST(0),
    	/** WebVTT cue span tag indicating the cue class. */
    	CLASS(1),
    	/** WebVTT cue span tag indicating text should be displayed in italics. */
    	ITALIC(2),
    	/** WebVTT cue span tag indicating bold text should be displayed. */
    	BOLD(3),
    	/** WebVTT cue span tag indicating text should be displayed underlined. */
    	UNDERLINE(4),
    	/** WebVTT cue span tag indicating that the text represents a Ruby base and should be displayed with the RUBY_TEXT indicated by another cue span. */
    	RUBY(5),
    	/** WebVTT cue span tag indicating the ruby text to be displayed above the ruby base text (indicated by the RUBY element). */
    	RUBY_TEXT(6),
    	/** WebVTT cue span tag indicating the name of the voice of the cue text.  This means for example, if cues represented a conversation happening between
         two individuals (Kim and Jon), this cue span would be used to indicate who was speaking the associate cue ie \c kim or \c jon.*/
    	VOICE(7),
    	/** WebVTT cue span tag indicating the language of cue, and must be a valid BCP 47 language tag. */
    	LANGUAGE(8),
    	/** WebVTT cue span tag indicating the cue text. */
    	TEXT(9),
    	/** WebVTT cue span tag indicating the timestamp of the cue. */
    	TIMESTAMP(10),
    	/** Used when a WebVTT cue span tag is not recognized. */
    	UNKNOWN(11);
    	
    	private int m_value;
    	private WebVTT_CueSpanTag(int value)
    	{
    		m_value = value;
    	}
    	public int getValue()
    	{
    		return m_value;
    	}
    	
    	public static WebVTT_CueSpanTag fromValue( int value ) {
		for( WebVTT_CueSpanTag item : values() ) {
			if( item.getValue() == value )
				return item;
		}
		return null;
    	}
	}
    

    /**
     * \brief This enumeration determines the horizontal alignment of timed text (WebVTT) in content.
     * 
     * It corresponds to the WebVTT alignment cue setting and are relative to the text direction. This indicates how WebVTT cues should be aligned
     * horizontally in the text track regions of the content.
     * 
     * \since version 6.4
     */
    public enum WebVTT_TextAlign
    {
    	//sets WebVTT caption align position
        /** WebVTT cue captions are aligned in the default location. */
    	Default(0),
/** WebVTT cue captions are aligned at the start of the text track region.*/
		Start(1),
/** WebVTT cue captions are aligned in the middle of the text track region. */
		Middle(2),
/** WebVTT cue captions are aligned at the end of the text track region. */
		End(3),
/** WebVTT cue captions are aligned on the left in the text track region. */
		Left(4),
/** WebVTT cue captions are aligned on the right in the text track region. */
		Right(5);	
		
    	private int m_value;
    	private WebVTT_TextAlign(int value)
    	{
    		m_value = value;
    	}
    	public int getValue()
    	{
    		return m_value;
    	}
    	
    	public static WebVTT_TextAlign fromValue( int value ) {
			for( WebVTT_TextAlign item : values() ) {
				if( item.getValue() == value )
					return item;
			}
			return null;
		}
    }
   

    /**
     * \brief  This enumeration specifies how WebVTT captions in content will be displayed.
     * 
     * This allows different language text to be 
     * displayed for example vertically, or from right-to-left when required.
     * 
     * \since version 6.4
     */
    public enum WebVTT_WritingDirection
    {
    	Default(0),
        Horizontal(1),
        Vertical_Growing_Left(2),
        Vertical_Growing_Right(3);
		
	private int m_value;
    	private WebVTT_WritingDirection(int value)
    	{
    		m_value = value;
    	}
    	public int getValue()
    	{
    		return m_value;
    	}
    	
    	public static WebVTT_WritingDirection fromValue( int value ) {
        	for( WebVTT_WritingDirection item : values() ) {
        		if( item.getValue() == value )
        			return item;
        	}
        	return null;
        }
    }

	protected class WebVTTRenderingData {
		int startTime;
		int endTime;
		int clearTime;

		String mRegionID;
		String mLinePos;

		int mTextPosition;
		int mSize;
		int mSnapToLine;
		WebVTT_TextAlign alignType;
		WebVTT_WritingDirection direction;

		class WebVTTNodeData {
			String text;
			boolean mBold;
			boolean mItalic;
			boolean mUnderline;
		}

		ArrayList<WebVTTNodeData> nodes = null;
	}

	WebVTTRenderingData webVTTRenderingData = null;

    //Call from Native
	/**
	 * \brief  This method sets the text data of WebVTT text tracks in content.
	 *
         * \param textData  A byte array with the text data of the WebVTT text cue to be set.
         * \param byteLen  The length of the WebVTT text data in the parameter, \c textData.
         *
	 * \since version 6.5
	 * @deprecated Do not use.
	 */


	public void setTextForWebVTT(byte[] textData, int byteLen)
	{
	}

    //Call from Native  
        /**
         * \brief This method sets a String with the full HTML data about a WebVTT text track cue.
         * 
         * \param textData A byte array with the full HTML data about the WebVTT cue.
         * \param byteLen  The length of the HTML data in the parameter, \c textData.
         * 
         * \since version 6.5
		 * @deprecated Do not use.
         */
	public void setHtmlDataForWebVTT(byte[] textData, int byteLen)
	{
	}

    //method to get cue tag list
    /**
     * \brief  This method gets the list of WebVTT cue tags in WebVTT text tracks in content.
     * 
     * \returns A list of byte arrays with the WebVTT cue tags.
     * 
     * \since version 6.5
	 * @deprecated Do not use.
     */
    public List<byte[]> getCueTagListForWebVTT()
    {
    	return null;
    }
    
 	//method to get text list
    /**
     * \brief This method gets a list of the text strings in WebVTT text cues content.
     *
     * \returns A list of byte arrays containing the strings of WebVTT cue text.
     * \since version 6.5
	 * @deprecated Do not use.
     */
    public List<byte[]> getTextStrListForWebVTT()
    {
    	return null;
    }
    
    //method to get full caption string
    /**
     * \brief  This method gets the full text of a caption from WebVTT text tracks as a \c String.
     * \returns  The WebVTT cue text as a \c String.
     * \since version 6.5
	 *
     */
    public String getTextStringForWebVTT()
    {
		String string = null;
		if (null != webVTTRenderingData) {
			for (WebVTTRenderingData.WebVTTNodeData node : webVTTRenderingData.nodes) {
				if (null == string) {
					string = node.text;
				} else {
					string += node.text;
				}
			}
		}
    	return string;
    }
    /**
     * \brief  This method gets the HTML data for WebVTT text cues in WebVTT text tracks in content.
     * \returns A \c String including the HTML data of the specified WebVTT text cue.
     * \since version 6.5
	 * @deprecated Do not use.
     */
    public String getHtmlDataForWebVTT()
    {
    	return null;
    }

    /**  \brief  This method gets the starting timestamp for WebVTT text cue captions.
     *
     *  \returns The starting timestamp of the WebVTT text cue as an \c integer.
     *  \since version 6.6
     */
    public int getStartTimeStampForWebVTT() {
		int startTime = 0;
		if (null != webVTTRenderingData) {
			startTime = webVTTRenderingData.startTime;
		}
		return startTime;
	}
    /**  \brief  This method gets the ending timestamp for WebVTT text cue captions.
     *
     *  \returns The ending timestamp of the WebVTT text cue as an \c integer.
     *  \since version 6.6
     */
    public int getEndTimeStampForWebVTT() {
		int endTime = 0;
		if (null != webVTTRenderingData) {
			endTime = webVTTRenderingData.endTime;
		}
		return endTime;
	}

    /**
     * \brief This method gets the current timestamp for WebVTT text cue captions.
     * 
     * \returns  The current timestamp of WebVTT text cue captions, as an \c integer.
     * \since version 6.14
	 * @deprecated Do not use.
     */
    public int getCurrentTimeStampForWebVTT() {
		return 0;
	}

    /**
     * \brief  This method gets the ID of the current WebVTT text cue.
     * 
     * \returns The ID of the current WebVTT text cue, as an \c integer.
     * \since version 6.14
	 * @deprecated Do not use.
     */
    public int getCueIDForWebVTT(){ return 0; }
	/** 
	 * \brief  This method gets the region ID data of a WebVTT cue in content.
	 * 
	 * \returns  A String with region ID of the content's WebVTT text.
	 * 
	 * \since version 6.5
	 */	
    public String getRegionIDForWebVTT() {
		String regionID = "";
		if (null != webVTTRenderingData) {
			regionID = webVTTRenderingData.mRegionID;
		}
		return regionID;
	}
	/** 
	 * \brief  This method gets the line position data of a WebVTT cue.
	 * 
	 * \returns  A byte array with the line position data for the content's WebVTT.
	 * 
	 * \since version 6.5
	 */	
    public String getLinePositionForWebVTT() {
		String linePos = "";
		if (null != webVTTRenderingData) {
			linePos = webVTTRenderingData.mLinePos;
		}
		return linePos;
	}
	/** 
	 * \brief  This method gets the position data of a WebVTT cue.
	 * 
	 * \returns  A byte array with the position data for the content's WebVTT.
	 * 
	 * \since version 6.5
	 */	
    public int getTextPositionForWebVTT() {
		int textPosition = 0;
		if (null != webVTTRenderingData) {
			textPosition = webVTTRenderingData.mTextPosition;
		}
		return textPosition;
	}
	/** 
	 * \brief  This method gets the size data of a WebVTT cue.
	 * 
	 * \returns  A byte array with the cue size data for the content's WebVTT.
	 * 
	 * \since version 6.5
	 */	
    public int getSizeForWebVTT() {
		int size = 0;
		if (null != webVTTRenderingData) {
			size = webVTTRenderingData.mSize;
		}
		return size;
	}
	/** 
	 * \brief  This method gets the snap-to-line data of a WebVTT cue.
	 * 
	 * \returns  A byte array with the snap-to-line data for the content's WebVTT.
	 * 
	 * \since version 6.5
	 */	
    public int getSnapToLineForWebVTT() {
		int snapToLine = 0;
		if (null != webVTTRenderingData) {
			snapToLine = webVTTRenderingData.mSnapToLine;
		}
		return snapToLine;
	}
	/** 
	 * \brief  This method gets the align type data of a WebVTT cue.
	 * 
	 * \returns  A byte array with the align type data for the content's WebVTT.
	 * 
	 * \since version 6.5
	 */	
    public int getAlignTypeForWebVTT() {
		int alignType = 0;
		if (null != webVTTRenderingData) {
			alignType = webVTTRenderingData.alignType.getValue();
		}
		return alignType;
	}
	/** 
	 * \brief  This method gets the text direction data of a WebVTT cue.
	 * 
	 * \returns  A byte array with the text direction data for the content's WebVTT.
	 * 
	 * \since version 6.5
	 */	
    public int getDirectionForWebVTT() {
		int direction = 0;
		if (null != webVTTRenderingData) {
			direction = webVTTRenderingData.direction.getValue();
		}
		return direction;
	}
	/** 
	 * \brief  This method gets the region anchor data of a WebVTT cue.
	 * 
	 * \returns  A byte array with the region anchor data for the content's WebVTT.
	 * 
	 * \since version 6.5
	 * @deprecated Do not use.
	 */	
    public int[] getRegionAnchorForWebVTT(){ return null; }
	/** 
	 * \brief  This method gets the viewport anchor data of a WebVTT region.
	 * 
	 * \returns  A byte array with the viewport anchor data for the content's WebVTT.
	 * 
	 * \since version 6.5
	 * @deprecated Do not use.
	 */	
    public int[] getViewportAnchorForWebVTT(){ return null; }
	/** 
	 * \brief  This method gets the width data of a WebVTT region.
	 * 
	 * \returns  A byte array with the width data for the content's WebVTT.
	 * 
	 * \since version 6.5
	 * @deprecated Do not use.
	 */
    public int getWidthForWebVTT(){ return 0; }

	/** 
	 * \brief  This method gets the text data for a WebVTT cue.
	 * 
	 * \returns  A byte array with the text data for the content's WebVTT.
	 * 
	 * \since version 6.5
	 */	
	public byte[] getTextDataForWebVTT() {
		byte[] textData = null;

		String string = getTextStringForWebVTT();
		if (null != string) {
			textData = string.getBytes();
		}
		return textData;
	}
}

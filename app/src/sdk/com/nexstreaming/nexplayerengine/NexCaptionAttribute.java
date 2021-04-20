package com.nexstreaming.nexplayerengine;

import com.nexstreaming.nexplayerengine.NexClosedCaption.CaptionColor;

/**
 * @brief This class manages the properties of captions.
 */
public class NexCaptionAttribute {



	/** 
	 * Possible key value for <code>key</code> parameter of {@link NexCaptionAttribute#setValue(int, float) setValue(int, float)}.
	 * This key indicates the value to scale the caption text.
	 * 
	 * Possible Value : <b>0.5 ~ 2.0</b>
	 */
	public final static int FLOAT_SCALE_FACTOR  = 0x00000000;
	/**
	 * Possible key value for <code>key</code> parameter of {@link NexCaptionAttribute#setValue(int, float) setValue(int, float)}.
	 * This key indicates the uniform width of caption text.
	 */
	public final static int FLOAT_STROKE_WIDTH  = 0x00000001;

	/**
	 * Possible key value for <code>key</code> parameter of {@link NexCaptionAttribute#setValue(int, EdgeStyle) setValue(int, EdgeStyle)}.
	 * This key indicates the caption text edge style. 
	 *
	 * Available Edge Styles :
	 *      - NexCaptionAttribute.EdgeStyle.NONE
	 *      - NexCaptionAttribute.EdgeStyle.DROP_SHADOW
	 *      - NexCaptionAttribute.EdgeStyle.RAISED
	 *      - NexCaptionAttribute.EdgeStyle.DEPRESSED
	 *      - NexCaptionAttribute.EdgeStyle.UNIFORM
	 */
	public final static int EDGE_STYLE          = 0x00000010;

	/** 
	 * Possible key value for <code>key</code> parameter of {@link NexCaptionAttribute#setValue(int, CaptionColor) setValue(int, CaptionColor)}.
	 * This key indicates the caption text color. 
	 *
	 * @see NexClosedCaption.CaptionColor for available caption font colors.    
	 */
	public final static int COLOR_FONT          = 0x00000100;
	/**
	 * Possible key value for <code>key</code> parameter of {@link NexCaptionAttribute#setValue(int, CaptionColor) setValue(int, CaptionColor)}.
	 * This key indicates the caption text edge color. 
	 *
	 * @see NexClosedCaption.CaptionColorAvaliable for available font edge colors. 
	 */
	public final static int COLOR_EDGE          = 0x00000101;
	/**
	 * Possible key value for <code>key</code> parameter of {@link NexCaptionAttribute#setValue(int, CaptionColor) setValue(int, CaptionColor)}.
	 * This key indicates the caption text background color. 
	 *
	 * @see NexClosedCaption.CaptionColor for available background colors.    
	 */
	public final static int COLOR_BACKGROUND    = 0x00000102;
	/** 
	 * Possible key value for <code>key</code> parameter of {@link NexCaptionAttribute#setValue(int, CaptionColor) setValue(int, CaptionColor)}.
	 * This key indicates the caption window color.
	 *
	 * @see NexClosedCaption.CaptionColor for available caption window colors. 
	 */
	public final static int COLOR_WINDOW        = 0x00000103;

	/** 
	 * Possible key value for <code>key</code> parameter of {@link NexCaptionAttribute#setValue(int, int) setValue(int, int)}.
	 * This key indicates the caption text opacity.
	 *  
	 * Possible Value : <b>0 ~ 255</b>, where 0 is completely transparent and 255 is completely opaque. 
	 */
	public final static int OPACITY_FONT        = 0x00000200;
	/** 
	 * Possible key value for <code>key</code> parameter of {@link NexCaptionAttribute#setValue(int, int) setValue(int, int)}.
	 * This key indicates the caption text edge opacity.
	 *
	 * Possible Value : <b>0 ~ 255</b>, where 0 is completely transparent and 255 is completely opaque. 
	 */
	public final static int OPACITY_EDGE        = 0x00000201;
	/** Possible key value for <code>key</code> parameter of {@link NexCaptionAttribute#setValue(int, int) setValue(int, int)}.
	 * This key indicates the caption text background opacity. 
	 *
	 * Possible Value : <b>0 ~ 255</b>, where 0 is completely transparent and 255 is completely opaque.
	 */
	public final static int OPACITY_BACKGROUND  = 0x00000202;
	/** 
	 * Possible key value for <code>key</code> parameter of {@link NexCaptionAttribute#setValue(int, int) setValue(int, int)}.
	 * This key indicates the caption window opacity. 
	 *
	 * Possible Value : <b>0 ~ 255</b>, where 0 is completely transparent and 255 is completely opaque.
	 */
	public final static int OPACITY_WINDOW      = 0x00000203;

	/**
	 * @brief This enumeration defines the caption edge style. 
	 *
	 * These are available caption edge styles : 
	 * - <b>NONE </b>: Default.
	 * - <b>DROP_SHADOW </b>: Adds a drop shadow effect to caption text.
	 * - <b>RAISED </b>: Adds a raised effect to caption text. 
	 * - <b>DEPRESSED </b>: Adds a depressed effect to caption text. 
	 * - <b>UNIFORM </b>: Adds a uniform effect to caption text.
	 */
	public enum EdgeStyle {
		NONE, DROP_SHADOW, RAISED, DEPRESSED, UNIFORM;
	}

	protected float mScaleFactor        = 1.0f;
	protected float mStrokeWidth        = 1.0f;
	protected EdgeStyle mEdgeStyle      = EdgeStyle.NONE;

	protected int mFontOpacity          = 255;
	protected int mEdgeOpacity          = 255;
	protected int mBackgroundOpacity    = 255;
	protected int mWindowOpacity        = 255;

	protected CaptionColor mFontColor        = CaptionColor.WHITE;
	protected CaptionColor mEdgeColor        = CaptionColor.BLACK;
	protected CaptionColor mBackGroundColor  = CaptionColor.BLACK;
	protected CaptionColor mWindowColor      = CaptionColor.BLACK;

	/**
	 * @brief Constructor of NexCaptionAttribute. 
	 * 
	 */
	public NexCaptionAttribute() { }

	/**
	 * @brief Constructor of NexCaptionAttribute. 
	 * The attributes will be generated by initializing settings object values.  
	 *
	 * @param settings A \c NexCaptionAttribute object.
	 */
	public NexCaptionAttribute(NexCaptionAttribute settings) {
		mScaleFactor        = (Float)settings.getValue(FLOAT_SCALE_FACTOR);
		mStrokeWidth        = (Float)settings.getValue(FLOAT_STROKE_WIDTH);
		mEdgeStyle          = (EdgeStyle)settings.getValue(EDGE_STYLE);

		mFontColor          = (CaptionColor)settings.getValue(COLOR_FONT);
		mEdgeColor          = (CaptionColor)settings.getValue(COLOR_EDGE);
		mBackGroundColor    = (CaptionColor)settings.getValue(COLOR_BACKGROUND);
		mWindowColor        = (CaptionColor)settings.getValue(COLOR_WINDOW);

		mFontOpacity        = (Integer)settings.getValue(OPACITY_FONT);
		mEdgeOpacity        = (Integer)settings.getValue(OPACITY_EDGE);
		mBackgroundOpacity  = (Integer)settings.getValue(OPACITY_BACKGROUND);
		mWindowOpacity      = (Integer)settings.getValue(OPACITY_WINDOW);
	}

	/**
	 * @brief This method sets the value of an individual \c NexCaptionAttribute attribute.
	 *
	 * @param attr  The attribute to set.
	 * @param value The new value for the attribute.
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
	 *
	 * @since version 6.42
	 */
	public boolean setValue(int attr, CaptionColor value) {
		boolean ret = false;
		switch(attr) {
			case COLOR_FONT:
				mFontColor = value;
				ret = true;
				break;
			case COLOR_EDGE:
				mEdgeColor = value;
				ret = true;
				break;
			case COLOR_BACKGROUND:
				mBackGroundColor = value;
				ret = true;
				break;
			case COLOR_WINDOW:
				mWindowColor = value;
				ret = true;
				break;
		}
		return ret;
	}

	/**
	 * @brief This method sets the value of an individual \c NexCaptionAttribute attribute.
	 *
	 * @param attr  The attribute to set.
	 * @param value The new value for the attribute.
	 * @return  Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
	 * 
	 * @since version 6.42
	 */
	public boolean setValue(int attr, EdgeStyle value) {
		if(attr == EDGE_STYLE) {
			mEdgeStyle = value;
			return true;
		}
		return false;
	}

	/**
	 * @brief This method sets the value of an individual \c NexCaptionAttribute attribute.
	 *
	 * @param attr  The attribute to set.
	 * @param value The new value for the attribute.
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
	 * @since version 6.42
	 */
	public boolean setValue(int attr, int value) {
		boolean ret = false;
		switch (attr) {
			case FLOAT_SCALE_FACTOR:
				if ( ret = withinRangeOfScaleFactor((float)value) ) {
					mScaleFactor = value;
					ret = true;
				}
				break;
			case FLOAT_STROKE_WIDTH:
				mStrokeWidth = (float)value;
				ret = true;
				break;
			case OPACITY_FONT:
				if( ret = withinRangeOfOpacity(value) ) {
					mFontOpacity = value;
					ret = true;
				}
				break;
			case OPACITY_EDGE:
				if( ret = withinRangeOfOpacity(value) ) {
					mEdgeOpacity = value;
					ret = true;
				}
				break;
			case OPACITY_BACKGROUND:
				if( ret = withinRangeOfOpacity(value) ) {
					mBackgroundOpacity = value;
					ret = true;
				}
				break;
			case OPACITY_WINDOW:
				if( ret = withinRangeOfOpacity(value) ) {
					mWindowOpacity = value;
					ret = true;
				}
				break;
		}
		return ret;
	}

	/**
	 * @brief This method sets the value of an individual \c NexCaptionAttribute attribute.
	 *
	 * @param attr  The attribute to set.
	 * @param value The new value for the attribute.
	 *
	 * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
	 *
	 * @since version 6.42
	 */
	public boolean setValue(int attr, float value) {
		boolean ret = false;

		switch(attr) {
			case FLOAT_SCALE_FACTOR :
				if (ret = withinRangeOfScaleFactor(value)) {
					mScaleFactor = value;
					ret = true;
				}
				break;
			case FLOAT_STROKE_WIDTH:
				mStrokeWidth = value;
				ret = true;
				break;
		}
		return ret;
	}

	/**
	 * @brief This method gets the value of an individual \c NexCaptionAttribute attribute.
	 *
	 * @param attr The attribute to get.
	 * @return	   The value of the attribute.
	 *
	 * @since version 6.42
	 */
	public Object getValue(int attr) {
		Object ret = null;
		switch (attr) {
			case FLOAT_SCALE_FACTOR:
				ret = mScaleFactor;
				break;
			case FLOAT_STROKE_WIDTH :
				ret = mStrokeWidth;
				break;
			case COLOR_FONT:
				ret = mFontColor;
				break;
			case OPACITY_FONT:
				ret = mFontOpacity;
				break;
			case EDGE_STYLE:
				ret = mEdgeStyle;
				break;
			case COLOR_EDGE:
				ret = mEdgeColor;
				break;
			case OPACITY_EDGE:
				ret = mEdgeOpacity;
				break;
			case COLOR_BACKGROUND:
				ret = mBackGroundColor;
				break;
			case OPACITY_BACKGROUND:
				ret = mBackgroundOpacity;
				break;
			case COLOR_WINDOW:
				ret = mWindowColor;
				break;
			case OPACITY_WINDOW:
				ret = mWindowOpacity;
				break;
		}
		return ret;
	}

	protected boolean withinRangeOfOpacity(int value) {
		return ( value >= 0 && value <= 255 ) ? true : false;
	}

	protected boolean withinRangeOfScaleFactor(float value) {
		return ( value >= 0.5f && value <= 2.0f ) ? true : false;
	}
}

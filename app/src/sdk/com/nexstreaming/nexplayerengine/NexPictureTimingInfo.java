package com.nexstreaming.nexplayerengine;

/**
* \brief  This class allows NexPlayer&trade;&nbsp;to handle SEI picture timing information in H.264 content.
* 
* In summary, this class passes timing information about each video frame in H.264 content if the content provides
* SEI picture timing information.  NexPlayer&trade;&nbsp;passes an instance of this class through the
*  \link NexPlayer.IListener#onPictureTimingInfo onPictureTimingInfo \endlink listener.
* 
* In most cases, this information will include \c mFullTimestampFlag, \c mSeconds, \c mMinutes, and \c mHours.
* But also note that \c mSeconds, \c mMinutes, and \c mHours are valid <b>only</b> if \c mFullTimestampFlag is 1.
* 
* For more in depth information about SEI picture timing information, please see the H.264 specifications.
* 
* \see NexPlayer.IListener.onPictureTimingInfo for more information.
* 
* \since version 6.0.5
*/

public class NexPictureTimingInfo 
{
	/**
	 * \brief  This is the clock timestamp flag of SEI picture timing information in H.264 content.
	 * 
	 *  <b>Values:</b>
	 *    - 1 :  Indicates that several clock timestamp syntax elements are present and follow immediately.
	 *    - 0 :  Indicates that the related clock timestamp elements are <b>present</b>.
	 * 
	 * \since version 6.0.5
	 */
	public int		mClockTimeStampFlag;
	/**
	 * \brief  This is the scan type of the source material from SEI picture timing information in H.264 content.
	 * 
	 * <b>Values:</b>
	 *  - 0 : Original picture scan was progressive.
	 *  - 1 : Original picture scan was interlaced.
	 *  - 2 : Original picture scan is unknown.
	 *  - 3 : Reserved.
	 *  
	 * \since version 6.0.5
	 */
	public int		mCtType;
	
	/** 
	 * \brief  This is the \c nuit_field_based_flag in SEI picture timing information in H.264 content.
	 * 
	 * This value can be used to calculate the clock timestamp of H.264 video frames.
	 * 
	 * \since version 6.0.5
	 */
	public int		mNuitFieldBasedFlag;
	/**
	 * \brief  This is the \c counting_type value in SEI picture timing information in H.264 content.
	 * 
	 *  It indicates the method of dropping values of the \c n_frames in SEI picture timing information.
	 * 
	 * <b>Values:</b>
	 * 	- 0 : no dropping of \c n_frames count values and no use of \c time_offset
	 * 	- 1 : no dropping of \c n_frames count values
	 *  - 2 : dropping of individual zero values of \c n_frames count
	 *  - 3 : dropping of individual \c MaxFPS - 1 values of \c n_frames count
	 *  - 4 : dropping of the two lowest (value 0 and 1) \c n_frames counts when \c seconds_value is equal to 0 and \c minutes_value is not an integer multiple of 10
	 *  - 5 : dropping of unspecified individual \c n_frames count values
	 *  - 6 : dropping of unspecified numbers of unspecified \c n_frames count values
	 *  - 7..31 : Reserved.
	 *  
	 *  \since version 6.0.5
	 */
	public int 	mCountingType;
	/**
	 * \brief  This is the \c full_timestamp_flag value in SEI picture timing information in H.264 content.
	 * 
	 * <b>Values:</b>
	 *   - 0 : Indicates that the \c n_frames element is followed only by the \c seconds_flag
	 *   - 1 : Indicates that a full timestamp is included and that the \c n_frames element is followed by \c seconds_value, \c minutes_value, and \c hours_value.
	 *   
	 *   \since version 6.0.5
	 */
	public int 	mFullTimestampFlag;
	/**
	 * \brief  This is the discontinuity_flag value in SEI picture timing information in H.264 content.
	 * 
	 * Please see the H.264 specifications for details in how to interpret the values here.
	 * 
	 * <b>Values:</b>
	 *    - 0 : Continuous clock timestamps.
	 *    - 1 : Discontinuity in clock timestamps.
	 *    
	 *  \since version 6.0.5
	 */
	public int 	mDiscontinuityFlag;
	/**
	 * \brief  This is the \c cnt_dropped_flag value in SEI picture timing information in H.264 content.
	 * 
	 *  Based on the counting method set by \c mCountingType, this value specifies that one or more values of \c mNFrames 
	 *  should be skipped.
	 *  
	 *  \since version 6.0.5
	 */
	public int	mCountDroppedFlag;
	/**
	 * \brief  This is the \c n_frames value in SEI picture timing information in H.264 content.
	 * 
	 * It is used to determine the clock timestamp and is a frame-based counter.
	 * 
	 * \since version 6.0.5 */
	public int 	mNFrames;
	/**
	 * \brief  This is the \c seconds_value in SEI picture timing information in H.264 content.
	 * 
	 * <b>Values:</b> 0 to 59 (inclusive)
	 * 
	 * \since version 6.0.5
	 */
	public int 	mSeconds;
	/**  
	 * \brief  This is the \c minutes_value in SEI picture timing information in H.264 content.
	 * 
	 * <b>Values:</b> 0 to 59 (inclusive)
	 * 
	 * \since version 6.0.5
	 */

	public int 	mMinutes;
	/**\brief  This is the \c hours_value in SEI picture timing information in H.264 content.
	 * 
	 * <b>Values:</b> 0 to 23 (inclusive)
	 * 
	 * \since version 6.0.5
	 * */
	public int 	mHours;
	/**\brief  This is the \c time_offset value in SEI picture timing information in H.264 content.
	 * 
	 * It can be used to determine the clock timestamp.
	 * 
	 * \since version 6.0.5
	 * */
	public int	mTimeOffset;
    /** \brief  This provides the SEI picture timing information for H.264 content being played.
     *
     * \param clockTimeStampFlag The clock timestamp flag in SEI picture timing information for H.264 content, as an integer.
     * \param ctType	The scan type of the source material in SEI picture timing information for H.264 content, as an integer.
     * \param nuitFieldBasedFlag The \c nuit_field_based_flag value in SEI picture timing information for H.264 content.
     * \param countingType	The \c counting_type value in SEI picture timing information for H.264 content.
     * \param FullTimeStampFlag	The \c full_timestamp_flag value in SEI picture timing information for H.264 content.
     * \param discontinuityFlag	The \c discontinuity_flag value in SEI picture timing information for H.264 content.
     * \param countDroppedFlag	The \c cnt_dropped_flag value in SEI picture timing information for H.264 content.
	 * \param nFrames	The \c n_frames value in SEI picture timing information for H.264 content.
	 * \param seconds	The \c seconds_value in SEI picture timing information for H.264 content.
	 * \param minutes 	The \c minutes_value in SEI picture timing information for H.264 content.
	 * \param hours  The \c hours_value in SEI picture timing information for H.264 content.
	 * \param timeOffset The \c time_offset value in SEI picture timing information for H.264 content.
     */
	public NexPictureTimingInfo(int clockTimeStampFlag, int ctType, int nuitFieldBasedFlag, int countingType, int FullTimeStampFlag, int discontinuityFlag, int countDroppedFlag, 
						int nFrames, int seconds, int minutes, int hours, int timeOffset)
	{
		mClockTimeStampFlag = clockTimeStampFlag;
		mCtType = ctType;
		mNuitFieldBasedFlag = nuitFieldBasedFlag;
		mCountingType = countingType;
		mFullTimestampFlag = FullTimeStampFlag;
		mDiscontinuityFlag = discontinuityFlag;
		mCountDroppedFlag = countDroppedFlag;
		mNFrames = nFrames;
		mSeconds = seconds;
		mMinutes = minutes;
		mHours = hours;
		mTimeOffset = timeOffset;
	}
	
}

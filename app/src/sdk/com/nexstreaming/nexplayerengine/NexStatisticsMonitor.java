package com.nexstreaming.nexplayerengine;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static com.nexstreaming.nexplayerengine.NexEventProxy.INexEventReceiver;

/**
 *
 * \brief  This class defines a statistics monitoring module to be used during playback of HLS, DASH or SS content with NexPlayer&trade;.
 * 
 * Implementing this class in an application makes it easier to monitor different kinds of statistics at regular intervals 
 * during playback of HLS, DASH or SS content in order to for example have more details about player performance.
 * 
 * To monitor HLS, DASH or SS playback statistics, after NexPlayer&trade;&nbsp;is created and initialized but before content is opened,
 * an application must:
 *    -# First, create an instance of this \c NexStatisticsMonitor class,
 *    -# Next, set a listener to receive the events reporting statistics information with \c IStatisticsListener, 
 *    -# lastly, if desired, the time interval at which statistics are monitored can be changed with calls to \c setDuration.
 * 
 * Depending on what information about playback is needed, different statistics can be retrieved from NexPlayer&trade;, 
 * including general playback statistics (\c GeneralStatisticsMetric), statistics when initializing content (\c InitialStatisticsMetric),
 * HTTP statistics(\c HttpStatisticsMetric), and system statistics during playback (\c SystemStatisticsMetric).
 *
 * The available statistics are briefly summarized in the table below:
 *
 * \latexonly   
 * 	
 *      \begin{longtable}{|c|c|c|c|c|}
 *          \hline 
 *          Statistic Category & \multicolumn{2}{|c|}{Metric} & Data Type & Unit \\
 *          \hline
 *          STATISTICS\_GENERAL & \multicolumn{2}{|c|}{PLAY\_TIME\_SEC} & long & seconds \\ \cline{2-5}
 *                              & \multicolumn{2}{|c|}{BYTES\_RECEIVED} & long & Bytes \\ \cline{2-5}
 *                              & \multicolumn{2}{|c|}{CUR\_NETWORK\_BW\_BPS} & int & bps \\ \cline{2-5}
 *                              & \multicolumn{2}{|c|}{CUR\_TRACK\_BW\_BPS} & int & bps \\ \cline{2-5}
 *                              & \multicolumn{2}{|c|}{NUM\_SEG\_REQUESTS} & int & num \\ \cline{2-5}
 *                              & \multicolumn{2}{|c|}{NUM\_SEG\_RECEIVED} & int & num \\ \cline{2-5}
 *                              & \multicolumn{2}{|c|}{NUM\_SEG\_DOWN\_RATE} & int & num \\ \cline{2-5}
 *                              & \multicolumn{2}{|c|}{NUM\_SEG\_FAIL\_TO\_PARSE} & int & num \\ \cline{2-5}
 *                              & \multicolumn{2}{|c|}{NUM\_SEG\_IN\_BUFFER} & int & num \\ \cline{2-5}
 *                              & \multicolumn{2}{|c|}{NUM\_REQUEST\_ERRORS} & int & num \\ \cline{2-5}
 *                              & \multicolumn{2}{|c|}{NUM\_REQUEST\_TIMEOUT} & int & num \\ \cline{2-5}
 *                              & \multicolumn{2}{|c|}{NUM\_TRACK\_SWITCH\_UP} & int & num \\ \cline{2-5}
 *                              & \multicolumn{2}{|c|}{NUM\_TRACK\_SWITCH\_DOWN} & int & num \\ \cline{2-5}
 *                              & \multicolumn{2}{|c|}{NUM\_VIDEO\_FRAME\_RENDERED} & int & num \\ \cline{2-5}
 *                              & \multicolumn{2}{|c|}{NUM\_VIDEO\_FRAME\_DECODED} & int & num \\ \cline{2-5}
 *                              & \multicolumn{2}{|c|}{NUM\_HTTP\_REQUESTS} & int & num \\ \cline{2-5}   
 *          \hline
 *			\hline
 *          STATISTICS\_INITIAL & \multicolumn{2}{|c|}{NUM\_TRACK} & int & num \\ \cline{2-5}
 *                             & \multicolumn{2}{|c|}{NUM\_PREBUFFERED\_SEGMENTS} & int & num \\ \cline{2-5}
 *                             & \multicolumn{2}{|c|}{NUM\_REDIRECTS} & int & num \\ \cline{2-5}
 *                             & \multicolumn{2}{|c|}{MASTER\_PLAYLIST} & String & text \\ \cline{2-5}
 *                             & \multicolumn{2}{|c|}{MASTER\_PLAYLIST\_URL} & String & text \\ \cline{2-5}
 *                             & \multicolumn{2}{|c|}{INITIAL\_PLAYLIST} & String & text \\ \cline{2-5}
 *                             & \multicolumn{2}{|c|}{INITIAL\_PLAYLIST\_URL} & String & text \\ \cline{2-5}
 *                             & \multicolumn{2}{|c|}{START\_SEGMENT\_URL} & String & text \\ \cline{2-5}
 *                             & \multicolumn{2}{|c|}{CONTENT\_DURATION} & int & msec \\ \cline{2-5}
 *          \hline
 *			\hline
 *          STATISTICS\_HTTP & DOWN\_START & RESOURCE\_URL & String & text \\ \cline{3-5}
 *                           		     & & FILE\_TYPE & FileType & FileType \\ \cline{3-5}
 *                           			 & & SEG\_NO & int & num \\ \cline{3-5}
 *                           			 & & SEG\_DURATION & int & msec \\ \cline{3-5}
 *                           			 & & TRACK\_BW & int & bps \\ \cline{3-5}
 *                           			 & & MEDIA\_COMPOSITION & int & MediaType \\ \cline{2-5}
 *                           & CONNECT & RESOURCE\_URL & String & text \\ \cline{2-5}
 *                           & CONNECTED & RESOURCE\_URL & String & text \\ \cline{2-5}
 *                           & HEADER\_RECEIVED & RESOURCE\_URL & String & text \\ \cline{2-5}
 *                           & DATA\_RECEIVED & RESOURCE\_URL & String & text \\ \cline{3-5}
 *                           			    & & BYTE\_RECEIVED & long & Bytes \\ \cline{3-5}
 *                           			    & & CONTENT\_LENGTH & long & Bytes \\ \cline{2-5}
 *                           & DOWN\_END & RESOURCE\_URL & String & text \\ \cline{3-5}
 *                           		   & & BYTE\_RECEIVED & long & Bytes \\ \cline{2-5}
 *                           & ERROR & RESOURCE\_URL & String & text \\ \cline{2-5}
 *          \hline
 *			\hline
 *          STATISTICS\_SYSTEM & \multicolumn{2}{|c|}{CPU\_USAGE} & double & percentage (0 - 1) \\ \cline{2-5}
 *                             & \multicolumn{2}{|c|}{FREE\_MEMORY\_KB} & long & kilobytes \\ \cline{2-5}
 *          \hline
 *
 *      \end{longtable}
 * 		
 *  \endlatexonly
 *
 * Example Code for how to implement and use \c NexStatisticsMonitor:
 * \code
 // Create a new NexStatisticsMonitor in the application
NexStatisticMonitor mMonitor = new NexStatisticMonitor(mNexPlayer);

IStatisticsListener listener = 
{
     void onUpdated(int statisticsType, HashMap<IStatistic, Object> map) {
          if( statisticsType == STATISTICS_GENERAL ) {
               while ( IStatistics key in map ) {
                   GeneralStatisticsMetric key 
                         = (GeneralStatisticsMetric)map.getKey();
                    Object value = map.getValue();
               }
          }
          else if( statisticsType == STATISTICS_INITIAL ) {
               while ( IStatistics key in map ) {
                   InitialStatisticsMetric key 
                         = (InitialStatisticsMetric)map.getKey();
                    Object value = map.getValue();
               }
          }
          else if( statisticsType == STATISTICS_HTTP ) {
               while ( IStatistics key in map ) {
                   HttpStatisticsMetric key; 
                   HashMap<HttpStatisticsParamKey, Object> subMap;

                    key = (HttpStatisticsMetric)map.getKey();
                    subMap =  (HashMap<HttpStatisticsParamKey,Object>)map.getValue();

                    while(HttpStatisticsParamKey key in subMap) {
                        HttpStatisticsParamKey paramkey 
                              =  (HttpStatisticsParamKey)subMap.getKey();
                         Object value = entry.getValue();
                    }
               }
          }	

          else if ..
     }
}

// Set a listener for statistics-related events
mMonitor.setListener( listener );

// Set the time interval at which general playback statistics should be requested (ie every 5 seconds)
mMonitor.setDuration(NexStatisticsMonitor.GeneralStatistic, seconds);
// Set the time interval at which system statistics during playback should be requested.
mMonitor.setDuration(NexStatisticsMonitor.SystemStatistic, seconds);
\endcode 
 * 
 * \see setListener
 * \see setDuration
 * 
 * \since version 6.40
 */
public class NexStatisticsMonitor {

	/** A possible argument value for the parameter \c statisticsType in the \c onUpdated() and \c setDuration() methods
	 * for the type of statistics being requested and received, for general playback statistics.*/
	public static final int STATISTICS_GENERAL = 0;
	/** A possible argument value for the parameter \c statisticsType in the \c onUpdated() method
	 * for the type of statistics being requested and received, for initial statistics (statistics about when playback is initialized).*/
	public static final int STATISTICS_INITIAL = 1;
	/** A possible argument value for the parameter \c statisticsType in the \c onUpdated() method
	 * for the type of statistics being requested and received, for HTTP statistics.*/
	public static final int STATISTICS_HTTP = 2;
	/** A possible argument value for the parameter \c statisticsType in the \c onUpdated() and \c setDuration() methods
	 * for the type of statistics being requested and received, for system statistics.  */
	public static final int STATISTICS_SYSTEM = 3;

	private final int ENABLE_HTTP_STATISTICS = 581;

	/* interface */
	/** 
	 *
	 * \brief  This interface defines a set of statistics to be received from \c NexPlayer&trade;&nbsp;about HLS, DASH or SS playback.
	 * 
	 * Every set of statistics (general, initial, HTTP, and system statistics) implements this interface.
	 * 
	 * \see GeneralStatistics
	 * \see InitialStatistics
	 * \see HTTPStatistics
	 * \see SystemStatistics
	 * 
	 * \since version 6.40
	 */
	public interface IStatistics {
		/** This method gets a statistics code.*/
		public int getCode();
	}

	/**
	 * \brief This interface defines the listener that will be used to receive statistics related to playback.
	 * 
	 * Any listener created to receive statistics from NexPlayer&trade;&nbsp;must implement this interface.
	 * 
	 * Once an instance of \c NexStatisticsMonitor is created, a statistics listener implementing this interface
	 * can be set with \c setListener(IStatisticsListener listener).
	 * 
	 * \see setListener
	 * \since version 6.40
	 */
	public interface IStatisticsListener {
		/** 
		 *
		 * \brief  This method is called whenever statistics are updated and sent by NexPlayer&trade;.
		 * 
		 * The time interval at which general and system statistics are updated in \c NexStatisticsMonitor
		 * can be changed by calling the \c setDuration() method.  
		 * 
		 * \param statisticsType  The type of statistics being updated, as an integer.  This will be one of:
		 * 								- \c STATISTICS_GENERAL = 0, for general playback statistics,
		 * 								- \c STATISTICS_INITIAL = 1, for initial statistics when playback starts,
		 * 								- \c STATISTICS_HTTP = 2, for HTTP statistics during playback, or
		 * 								- \c STATISTICS_SYSTEM = 3, for system statistics during HLS, DASH or SS playback.
		 * \param map  The updated statistics object as a \c HashMap.
		 * 
		 * \see setDuration
		 * \since version 6.40
		 */
		void onUpdated(int statisticsType, HashMap<IStatistics, Object> map);
	}

	/**
	 *
	 * \brief  An enumeration of the possible types of files being handled by NexPlayer&trade;&nbsp; during HLS, DASH or SS playback.
	 * 
	 * These are possible values for the HTTP statistics parameter key, \c FILE_TYPE.
	 * 
	 * \since version 6.40
	 */
	public static enum FileType {
		/** The filetype is unknown. */
		UNKNOWN             (0),  
		/** A manifest file. */
		MANIFEST            (1),  
		/** A segment file. */
		SEGMENT             (2),  
		/** An initial segment file. */
		INITIAL_SEGMENT     (3), 
		/** A key file. */
		KEY                 (4),
		/** A Segment Index */
		SEGMENT_INDEX		(5);

		private int mCode;

		/** Sets the \c FileType. */
		FileType(int code) {
			mCode = code;
		}

		/** 
		 * Gets the \c FileType code as an integer.
		 * \returns The requested \c FileType code as an integer.
		 */
		public int getCode() { return mCode; }

		/**
		 * This method gets the \c FileType from the integer code of the \c FileType.
		 * 
		 * \returns  The \c FileType corresponding to the given integer code.  This will be one of:
		 *               - \c UNKNOWN for an unknown filetype,
		 *               - \c MANIFEST for a manifest file,
		 *               - \c SEGMENT for a segment file,
		 *               - \c INITIAL_SEGMENT for an initial segment file, or
		 *               - \c KEY for a key file.
		 * \since version 6.40
		 */
		public static FileType toFileType( int code ) {
			for( int i=0; i<FileType.values().length; i++ ) {
				if( FileType.values()[i].mCode == code )
					return FileType.values()[i];
			}
			return UNKNOWN;
		}
	}

	/**
	 *
	 * \brief  An enumeration defining the possible types of HLS, DASH or SS media can be played by NexPlayer&trade;.
	 * 
	 * These media types include:
	 *      - <b>\c NONE = 0 </b>:  No media file found. 
	 *      - <b> AUDIO = 1 </b>:  Audio content.
	 *      - <b> BASEVIDEO = 2 </b>:  Base video content.
	 *      - <b> TEXT = 4 </b>:  Text content or subtitles.
	 *      - <b> ENHANCEDVIDEO = 8 </b>:  Enhanced video content.
	 * 
	 * \see GeneralStatistics
	 * \see isMediaExist( MediaType mediaType, int mediaComposition )
	 * 
	 * \since version 6.40
	 */
	public static enum MediaType {
		NONE			(0x00000000),
		AUDIO			(0x00000001),
		BASEVIDEO		(0x00000002),
		TEXT			(0x00000004),
		ENHANCEDVIDEO	(0x00000008);

		private int         mCode;

		/**
		 * Sets the \c MediaType.
		 */
		MediaType(int code) {
			mCode = code;
		}

		/**
		 * Gets the integer code for the \c MediaType.
         *
         * @return The integer code for the specified \c MediaType.
         * \since version 6.40
		 */
		public int getCode() {
			return mCode;
		}

		/**
		 *  \brief  This method indicates whether or not the requested media exists in the current content.  
		 *  
		 *  This method can be used to check if the current content contains the specific media (\c AUDIO, \c BASEVIDEO, \c TEXT, \c ENHANCEDVIDEO)
		 *  based on the \c mediaComposition value received.
		 *  
		 *  \param mediaType  The type of media to check.  This will be one of:
		 *  						- \c AUDIO (0x00000001): for audio media.
		 *  						- \c BASEVIDEO (0x00000002): for base video.
		 *  						- \c TEXT (0x00000004): for text or subtitles.
		 *  						- \c ENHANCEDVIDEO (0x00000008): for enhanced video.
		 *  
		 *  \param mediaComposition  The composition of the specified media, as an integer.  
		 *  
		 *  \returns \c TRUE if the media indicated exists, or \c FALSE if it does not exist in the current content.
		 *  
		 *  \since version 6.40
		 */
		public static boolean isMediaExist( MediaType mediaType, int mediaComposition ) {
			return ( ( (mediaType.getCode() & mediaComposition) != 0 ) ? true : false );
		}
	}

	/**
	 *
	 * \brief An enumeration of the statistics-related errors possible when using the \c NexStatisticsMonitor.
	 * 
	 * \since version 6.40
	 */
	public static enum StatisticsError {
		/** Playback statistics were successfully retrieved without error.  */
		ERROR_NONE,  
		/** The duration set is invalid or cannot be applied to the statistics being requested. */
		ERROR_DURATION_INVALID,  
		/** The type of statistics being requested is invalid.  */
		ERROR_TYPE_INVALID,  
		/** An unknown error occurred when retrieving HLS, DASH or SS playback statistics.  */
		ERROR_UNKNOWN;  
	}

	/**
	 * 
	 * \brief This is an enumeration of the possible general statistics that can be requested during playback of HLS, DASH or SS content in NexPlayer&trade;.
	 * 
	 * The statistics defined here are for general playback of HLS, DASH or SS content.  If statistics about the initialization of content are required,
	 * please review \c InitialStatisticsMetric instead.
	 * 
	 * \see InitialStatisticsMetric
	 * \see IStatistics
	 * \see setDuration
	 * 
	 * \since version 6.40
	 */
	public static enum GeneralStatisticsMetric implements IStatistics {
		/** The current play time in seconds, as a \c long.  This is the same as the count of \c NEXPLAYER_EVENT_TIME.*/
		PLAY_TIME_SEC               ( 0x00000100 ), // Long, Cnt of NEXPLAYER_EVENT_TIME
		/** The bytes received, as a \c long.  This is the same as \c NexRTStreamInformation.mNumOfBytesRecv.*/
		BYTES_RECEIVED              ( 0x00000200 ), // Long, NexRTStreamInformation.mNumOfBytesRecv
		/** The current bandwidth of the network, in bps, as an integer.  This is the same as \c NexRTStreamInformation.mCurNetworkBw. */
		CUR_NETWORK_BW_BPS          ( 0x00000300 ), // Int, NexRTStreamInformation.mCurNetworkBw
		/** The current track bandwidth, in bps, as an integer. This is the same as \c NexRTStreamInformation.mCurTrackBw. */
		CUR_TRACK_BW_BPS            ( 0x00000400 ), // Int, NexRTStreamInformation.mCurTrackBw
		/** The current number of segment requests, as an integer.  This is the same as \c NexRTStreamInformation.mNumOfSegRequest. */
		NUM_SEG_REQUESTS            ( 0x00000500 ), // Int, NexRTStreamInformation.mNumOfSegRequest
		/** The current number of segments received, as an integer.  This is the same as \c NexRTStreamInformation.mNumOfSegReceived. */
		NUM_SEG_RECEIVED            ( 0x00000600 ), // Int, NexRTStreamInformation.mNumOfSegReceived
		/** The number of segments that have an actual read bitrate below the bitrate specified in the profile,
		 * where the read bitrate is the speed at which the segments are read from the network. 
		 * This is the same as \c NexRTStreamInformation.mNumOfSegDownRate. */
		NUM_SEG_DOWN_RATE           ( 0x00000700 ), // Int, NexRTStreamInformation.mNumOfSegDownRate
		/** The number of segment reads that were failed to be read and parsed, for example due to HTTP errors, as an integer.
		 * This is the same as \c NexRTStreamInformation.mNumOfSegFailToParse. */
		NUM_SEG_FAIL_TO_PARSE       ( 0x00000800 ), // Int, NexRTStreamInformation.mNumOfSegFailToParse
		/** The current number of segments in the buffer, as an integer.  This is the same as \c NexRTStreamInformation.mNumOfSegInBuffer. */
		NUM_SEG_IN_BUFFER           ( 0x00000900 ), // Int, NexRTStreamInformation.mNumOfSegInBuffer
		/** The current number of segments that the player failed to receive, as an integer.
		 *  This is the same as \c NexRTStreamInformation.mNumOfSegFailToReceive.*/
		NUM_REQUEST_ERRORS          ( 0x00000A00 ), // Int, NexRTStreamInformation.mNumOfSegFailToReceive
		/** The current number of segment reads that resulted in a timeout, as an integer.
		 * This is the same as \c NexRTStreamInformation.mNumOfSegTimeout.*/
		NUM_REQUEST_TIMEOUT         ( 0x00000B00 ), // Int, NexRTStreamInformation.mNumOfSegTimeout
		/**
		 * The number of times the content profile has been changed to a profile with a higher bitrate, as an integer.
		 * This is the same as \c NexRTStreamInformation.mNumOfTrackSwitchUp.*/
		NUM_TRACK_SWITCH_UP         ( 0x00000C00 ), // Int, NexRTStreamInformation.mNumOfTrackSwitchUp
		/**
		 * The number of times the content profile has been changed to a profile with a lower bitrate, as an integer.
		 * This is the same as \c NexRTStreamInformation.mNumOfTrackSwitchDown.
		 */
		NUM_TRACK_SWITCH_DOWN       ( 0x00000D00 ), // Int, NexRTStreamInformation.mNumOfTrackSwitchDown
		/**
		 * The current number of frames that have been successfully rendered, as an integer.
		 * This is the same as \c getContentInfoInt(NexPlayer.CONTENT_INFO_INDEX_VIDEO_RENDER_TOTAL_COUNT).
		 */
		NUM_VIDEO_FRAME_RENDERED    ( 0x00000E00 ), // Int, getContentInfoInt(NexPlayer.CONTENT_INFO_INDEX_VIDEO_RENDER_TOTAL_COUNT);
		/**
		 * The current number of frames that have been decoded, as an integer.
		 * This is the same as \c getContentInfoInt(NexPlayer.CONTENT_INFO_INDEX_VIDEO_CODEC_DECODING_TOTAL_COUNT).
		 */
		NUM_VIDEO_FRAME_DECODED     ( 0x00000F00 ), // Int, getContentInfoInt(NexPlayer.CONTENT_INFO_INDEX_VIDEO_CODEC_DECODING_TOTAL_COUNT);
		/**
		 * This current number of HTTP requests that have been made, as an integer.
		 * This is the same as the running count of \c onHttpRequest(NexPlayer mp, String msg) calls.
		 */
		NUM_HTTP_REQUESTS           ( 0x00001000 ); // Int, Cnt of onHttpRequest(NexPlayer mp, String msg)

		private int         mCode;
		/** Sets the general statistic metric.  */
		GeneralStatisticsMetric(int code) {
			mCode       = code;
		}
		/** Gets the general statistic code, as an integer.  
		 * 
		 * \returns The general statistics code requested.
		 */
		public int getCode() { return mCode; }
	}

	/**
	 *
	 * \brief  This is an enumeration of the possible initial statistics that can be requested when initializing 
	 *         playback of HLS, DASH or SS content in NexPlayer&trade;.
	 * 
	 * Since this statistics metric is only for monitoring the statistics related to the initialization of content, for
	 * more general playback statistics, monitor \c GeneralStatisticsMetric instead.
	 * 
	 * \see GeneralStatisticsMetric
	 * 
	 * \since version 6.40
	 */
	public static enum InitialStatisticsMetric implements IStatistics {
		/** The total number of tracks in the current content, as an integer. */
		NUM_TRACK                   ( 0x00000010 ), // Int, totalTrackCnt
		/** The total number of segments loaded in the buffer, as an integer.  
		 * This is the same as \c NexRTStreamInformation.mNumOfSegInBuffer.*/
		NUM_PREBUFFERED_SEGMENTS    ( 0x00000020 ), // Int, NexRTStreamInformation.mNumOfSegInBuffer
		/** The total number of redirects, as an integer.  This is the same as \c NexRTStreamInformation.mNumOfRedirect.*/
		NUM_REDIRECTS               ( 0x00000030 ), // Int, NexRTStreamInformation.mNumOfRedirect
		/** The full content of the master manifest playlist, as a \c String.  This is the same as \c NexRTStreamInformation.mMasterMpd.*/
		MASTER_PLAYLIST             ( 0x00000040 ), // String, NexRTStreamInformation.mMasterMpd
		/** The URL of the master manifest playlist, as a \c String.  This is the same as \c NexRTStreamInformation.mMasterMpdUrl.*/
		MASTER_PLAYLIST_URL         ( 0x00000050 ), // String, NexRTStreamInformation.mMasterMpdUrl
		/** The full content of the initial manifest playlist, as a \c String.  This is the same as \c NexRTStreamInformation.mInitialMpd.*/
		INITIAL_PLAYLIST            ( 0x00000060 ), // String, NexRTStreamInformation.mInitialMpd
		/** The actual URL (after all redirects) of the initial request for the manifest playlist, as a \c String.  
		 * This is the same as \c NexRTStreamInformation.mInitialMpdUrl.*/
		INITIAL_PLAYLIST_URL        ( 0x00000070 ), // String, NexRTStreamInformation.mInitialMpdUrl
		/** The description of the initially selected profile, as a \c String.  This is the same as \c NexRTStreamInformation.mStartSegUrl.*/
		START_SEGMENT_URL           ( 0x00000080 ), // String, NexRTStreamInformation.mStartSegUrl
		/** The total duration of the current content, as an integer.
		 * This is the same as \c NexContentInformation.mMediaDuration. */
		CONTENT_DURATION            ( 0x00000090 ); // Int, NexContentInformation.mMediaDuration

		private int         mCode;

		/** Sets the initial statistics metric.
		 */
		InitialStatisticsMetric(int code) {
			mCode       = code;
		}
		/** Gets the initial statistic code, as an integer. 
		 * \returns The initial statistics code, as an integer.
		 */
		public int getCode() { return mCode; }
	}

	/**
	 *
	 * \brief  This enumeration defines the possible HTTP statistics that can be requested during HLS, DASH or SS playback by NexPlayer&trade;.
	 * 
	 * For general playback statistics or system statistics during playback, please monitor \c GeneralStatisticsMetric or \c SystemStatisticsMetric
	 * instead.
	 * 
	 * \see GeneralStatisticsMetric
	 * \see SystemStatisticsMetric
	 * \see HttpStatisticsParamKey
	 * 
	 * \since version 6.40
	 */
	public static enum HttpStatisticsMetric implements IStatistics {
		DOWN_START      ( 0x00000000 ),
		CONNECT			( 0x00000001 ),
		CONNECTED		( 0x00000003 ),
		HEADER_RECEIVED ( 0x00000004 ),
		DATA_RECEIVED	( 0x00000005 ),
		DOWN_END		( 0x00000006 ),
		ERROR			( 0x00000007 );

		private int         mCode;

		/** Sets the HTTP statistics metric. */
		HttpStatisticsMetric(int code) {
			mCode = code;
		}
		/** Gets an HTTP statistic metric as an integer.
		 * \returns The requested HTTP statistic metric, as an integer.
		 */
		public int getCode() { return mCode; }
	}

	/**
	 *
	 * \brief  This enumeration defines the parameter key, for parameters related to HTTP statistics monitored
	 *         during HLS, DASH or SS playback.
	 * 
	 * \see HttpStatisticsMetric
	 * \since version 6.40
	 */
	public static enum HttpStatisticsParamKey {
		/** The resource URL, as a \c String.  A possible parameter for the HTTP statistics, \c DOWN_START, \c CONNECT, \c CONNECTED, 
		 * \c HEADER_RECEIVED, \c DATA_RECEIVED, \c DOWN_END, and \c ERROR.
		 */
		RESOURCE_URL        ( 0x00000000 ),
		/** The file type being received, as a \c FileType.  A parameter for the HTTP statistic, \c DOWN_START. */
		FILE_TYPE           ( 0x00000001 ),
		/** The current segment number, as an integer. A parameter for the HTTP statistic, \c DOWN_START.*/
		SEG_NO              ( 0x00000002 ),
		/** The duration of the current segment, as an integer.  A parameter for the HTTP statistic, \c DOWN_START. */
		SEG_DURATION        ( 0x00000003 ),
		/** The bandwidth of the current track, as an integer.  A parameter for the HTTP statistic, \c DOWN_START. */
		TRACK_BW            ( 0x00000004 ),
		/** The media composition of the current content, as an integer.  A parameter for the HTTP statistic, \c DOWN_START. */
		MEDIA_COMPOSITION   ( 0x00000005 ),
		/** The current number of bytes received, as a \c long.  A parameter for the HTTP statistics, \c DATA_RECEIVED and \c DOWN_END.*/
		BYTE_RECEIVED       ( 0x00000006 ),
		/** The current length of the HLS, DASH or SS content received so far, as a \c long.  A parameter for the HTTP statistic, \c DATA_RECEIVED.*/
		CONTENT_LENGTH      ( 0x00000007 ),
		/** The error code.  A parameter for the HTTP statistic, \c ERROR. */
		ERROR_CODE          ( 0x00000008 );

		private int mCode;

		/** Sets the HTTP statistics parameter key. */
		HttpStatisticsParamKey(int code) {
			mCode = code;
		}

		/** Gets the HTTP statistics parameter key as an integer code.
		 * \returns The HTTP statistics parameter key requested, as an integer.
		 */
		public final int getCode() { return mCode; }
	}

	/** 
	 *
	 * \brief  This is an enumeration of the possible system statistics that can be requested during playback of HLS, DASH or SS content in NexPlayer&trade;.
	 * 
	 * These statistics are only related to the system performance during playback.  For other statistics specifically about the HLS, DASH or SS
	 * content playback or HTTP statistics, monitor \c GeneralStatisticsMetric or \c HttpStatisticsMetric instead.
	 * 
	 * \see GeneralStatisticsMetric
	 * \see HttpStatisticsMetric
	 * \see setDuration
	 * 
	 * \since version 6.40
	 */
	public static enum SystemStatisticsMetric implements IStatistics {
		/** The current CPU usage, as a percentage, represented as a value between 0 and 1, 
		 * where zero is 0 percent and 1 is 100 percent.*/
		CPU_USAGE       ( 0x00010000 ),
		/** The current amount of memory free, in kilobytes, as a \c long.*/
		FREE_MEMORY_KB  ( 0x00020000 );

		/** Sets the system statistics metric. */
		SystemStatisticsMetric(int code) {mCode = code;}

		private int         mCode;
		
		/** Gets the system statistics code, as an integer. 
		 * \returns The system statistics code requested, as an integer.
		 */
		public int getCode() { return mCode; }
	}

	private StatisticsTimer mGeneralStatisticsMonitor;
	private StatisticsTimer mSystemStatisticsMonitor;

	private long mTotalPlayTime;
	private int mNumHttpReq;

	private INexEventReceiver   mEventReceiver;
	private NexPlayer           mNexPlayer;

	private IStatisticsListener mStatisticsListener;

	/**
	 *
	 * \brief  Defines the \c NexStatisticsMonitor module that an application can use to retrieve 
	 *         statistics about playback of HLS, DASH or SS content in NexPlayer&trade;.
	 *         
	 * \param np  The \c NexPlayer instance that the statistics monitor will receive events from.
	 * 
	 * \since version 6.40
	 */
	public NexStatisticsMonitor(NexPlayer np) {
		mNexPlayer = np;
		mNexPlayer.setProperties(ENABLE_HTTP_STATISTICS, 1);
		mGeneralStatisticsMonitor = new StatisticsTimer(STATISTICS_GENERAL);
		mSystemStatisticsMonitor = new StatisticsTimer(STATISTICS_SYSTEM);

		mStatisticsListener = null;
		mTotalPlayTime = 0;
		mNumHttpReq = 0;

		setupEventReceiver();
	}

	public NexStatisticsMonitor(NexPlayer np, boolean enableSystemMonitor) {
		mNexPlayer = np;
		mNexPlayer.setProperties(ENABLE_HTTP_STATISTICS, 1);
		mGeneralStatisticsMonitor = new StatisticsTimer(STATISTICS_GENERAL);

		if (enableSystemMonitor) {
			mSystemStatisticsMonitor = new StatisticsTimer(STATISTICS_SYSTEM);
		}

		mStatisticsListener = null;
		mTotalPlayTime = 0;
		mNumHttpReq = 0;

		setupEventReceiver();
	}

	private void setupEventReceiver() {
		mEventReceiver = new INexEventReceiver() {
			@Override
			public NexPlayerEvent[] eventsAccepted() {
				return new NexPlayerEvent[]{
						new NexPlayerEvent( NexPlayerEvent.NEXPLAYER_EVENT_ASYNC_CMD_COMPLETE ),
						new NexPlayerEvent( NexPlayerEvent.NEXPLAYER_EVENT_TIME ),
						new NexPlayerEvent( NexPlayerEvent.NEXPLAYER_EVENT_DEBUGINFO ),
						new NexPlayerEvent( NexPlayerEvent.NEXPLAYER_EVENT_ONHTTPSTATS ),
				};
			}

			@Override
			public void onReceive(NexPlayer nexplayer, NexPlayerEvent event) {
				if( isMonitoringEnabled() ) {
					if( event != null && (event.intArgs.length > 0 ) ) {
						if (event.what == NexPlayerEvent.NEXPLAYER_EVENT_ASYNC_CMD_COMPLETE) {
							handleEventAsyncCmdComplete(event);
						} else if (event.what == NexPlayerEvent.NEXPLAYER_EVENT_TIME) {
							handleEventEventTime(event);
						} else if (event.what == NexPlayerEvent.NEXPLAYER_EVENT_DEBUGINFO) {
							handleEventDebugInfo(event);
						} else if (event.what == NexPlayerEvent.NEXPLAYER_EVENT_ONHTTPSTATS) {
							handleEventOnHttpStats(event);
						}
					}
				}
			}
		};
		mNexPlayer.getEventProxy().registerReceiver(mEventReceiver);
	}

	private boolean isMonitoringEnabled() {
		return (mStatisticsListener != null) ? true : false;
	}

	private boolean isTimerActivated(int statisticsType) {
		if( statisticsType == STATISTICS_GENERAL ) {
			return mGeneralStatisticsMonitor.isActivated();
		}
		else if (null != mSystemStatisticsMonitor && statisticsType == STATISTICS_SYSTEM) {
			return mSystemStatisticsMonitor.isActivated();
		}
		return false;
	}

	private void handleEventAsyncCmdComplete(NexPlayerEvent event) {

		if (event.intArgs[0] == NexPlayer.NEXPLAYER_ASYNC_CMD_OPEN_STREAMING) {
			startTimer();

			if(mStatisticsListener != null) {
				HashMap<IStatistics, Object> map = new HashMap<IStatistics, Object>();
				NexPlayer.NexRTStreamInformation streamInfo = mNexPlayer.getRTStreamInfo();
				NexContentInformation contentInfo = mNexPlayer.getContentInfo();

				if( streamInfo != null ) {
					int totalTrackCnt = 0;
					int curStreamId = contentInfo.mCurrVideoStreamID;


					if (contentInfo.mMediaType == 1) {
						curStreamId = contentInfo.mCurrAudioStreamID;
					}
					for (int i = 0; i < contentInfo.mStreamNum; i++) {
						if (curStreamId == contentInfo.mArrStreamInformation[i].mID) {
							totalTrackCnt = contentInfo.mArrStreamInformation[i].mTrackCount;
						}
					}
					map.put(InitialStatisticsMetric.NUM_TRACK, totalTrackCnt);
					map.put(InitialStatisticsMetric.NUM_PREBUFFERED_SEGMENTS, new Long(streamInfo.mNumOfSegInBuffer).intValue());
					map.put(InitialStatisticsMetric.NUM_REDIRECTS, new Long(streamInfo.mNumOfRedirect).intValue());
					map.put(InitialStatisticsMetric.MASTER_PLAYLIST, streamInfo.mMasterMpd);
					map.put(InitialStatisticsMetric.MASTER_PLAYLIST_URL, streamInfo.mMasterMpdUrl);
					map.put(InitialStatisticsMetric.INITIAL_PLAYLIST, streamInfo.mInitialMpd);
					map.put(InitialStatisticsMetric.INITIAL_PLAYLIST_URL, streamInfo.mInitialMpdUrl);
					map.put(InitialStatisticsMetric.START_SEGMENT_URL, streamInfo.mStartSegUrl);
					map.put(InitialStatisticsMetric.CONTENT_DURATION, contentInfo.mMediaDuration);
				}
				mStatisticsListener.onUpdated(STATISTICS_INITIAL, map);
			}
		} else if (event.intArgs[0] == NexPlayer.NEXPLAYER_ASYNC_CMD_START_STREAMING) {
			if (!isMonitoringEnabled()) {
				startTimer();
			}
		} else if (event.intArgs[0] == NexPlayer.NEXPLAYER_ASYNC_CMD_STOP) {
			stopTimer();
		}
	}

	private void handleEventDebugInfo(NexPlayerEvent event) {
		if( event.intArgs[0] == NexPlayer.NEXPLAYER_DEBUGINFO_HTTP_REQUEST ) {
			mNumHttpReq++;
		}
	}

	private void handleEventEventTime(NexPlayerEvent event) {
		mTotalPlayTime++;
	}

	private void handleEventOnHttpStats(NexPlayerEvent event) {
		HashMap<IStatistics, Object> map = new HashMap<IStatistics, Object>();
		HashMap<HttpStatisticsParamKey, Object> param = new HashMap<HttpStatisticsParamKey, Object>();
		HashMap<Object, Object> httpInfo = (HashMap<Object, Object>)event.obj;

		if( httpInfo != null && mStatisticsListener != null) {
			if( event.intArgs[0] == HttpStatisticsMetric.DOWN_START.getCode() ) {
				// void onHttpDownStart(String resourceUrl, FileType fileType, int segNo, int segDur, int trackBw, int mediaComposition);
				param.put(HttpStatisticsParamKey.RESOURCE_URL, (String)httpInfo.get("resourceUrl"));
				param.put(HttpStatisticsParamKey.FILE_TYPE, FileType.toFileType( (Integer) httpInfo.get("fileType")));
				param.put(HttpStatisticsParamKey.SEG_DURATION, (Integer)httpInfo.get("segDur"));
				param.put(HttpStatisticsParamKey.SEG_NO, (Integer)httpInfo.get("segNo"));
				param.put(HttpStatisticsParamKey.TRACK_BW, (Integer)httpInfo.get("trackBw"));
				param.put(HttpStatisticsParamKey.MEDIA_COMPOSITION, (Integer)httpInfo.get("mediaType"));

				map.put(HttpStatisticsMetric.DOWN_START, param);
			}
			else if( event.intArgs[0] == HttpStatisticsMetric.CONNECT.getCode() ) {
				// void onHttpConnect(String resourceUrl);
				param.put(HttpStatisticsParamKey.RESOURCE_URL, (String)httpInfo.get("resourceUrl"));
				map.put(HttpStatisticsMetric.CONNECT, param);
			}
			else if( event.intArgs[0] == HttpStatisticsMetric.CONNECTED.getCode() ) {
				// void onHttpConnected(String resourceUrl);
				param.put(HttpStatisticsParamKey.RESOURCE_URL, (String)httpInfo.get("resourceUrl"));
				map.put(HttpStatisticsMetric.CONNECTED, param);
			}
			else if( event.intArgs[0] == HttpStatisticsMetric.HEADER_RECEIVED.getCode() ) {
				// void onHttpHeaderReceived(String resourceUrl);
				param.put(HttpStatisticsParamKey.RESOURCE_URL, (String)httpInfo.get("resourceUrl"));
				map.put(HttpStatisticsMetric.HEADER_RECEIVED, param);
			}
			else if( event.intArgs[0] == HttpStatisticsMetric.DATA_RECEIVED.getCode() ) {
				// void onHttpDataReceived(String resourceUrl, int byteReceived, int contentLength);
				param.put(HttpStatisticsParamKey.RESOURCE_URL, (String)httpInfo.get("resourceUrl"));
				param.put(HttpStatisticsParamKey.BYTE_RECEIVED, (Long)httpInfo.get("byteReceived"));
				param.put(HttpStatisticsParamKey.CONTENT_LENGTH, (Long)httpInfo.get("totalSize"));
				map.put(HttpStatisticsMetric.DATA_RECEIVED, param);
			}
			else if( event.intArgs[0] == HttpStatisticsMetric.DOWN_END.getCode() ) {
				// void onHttpDownEnd(String resourceUrl, int byteReceived);
				param.put(HttpStatisticsParamKey.RESOURCE_URL, (String)httpInfo.get("resourceUrl"));
				param.put(HttpStatisticsParamKey.CONTENT_LENGTH, (Long)httpInfo.get("totalSize"));
				map.put(HttpStatisticsMetric.DOWN_END, param);
			}
			else if( event.intArgs[0] == HttpStatisticsMetric.ERROR.getCode() ) {
				// void onHttpError(String resourceUrl, int errorCode);
				param.put(HttpStatisticsParamKey.RESOURCE_URL, (String)httpInfo.get("resourceUrl"));
				param.put(HttpStatisticsParamKey.ERROR_CODE, (Integer)httpInfo.get("errCode"));
				map.put(HttpStatisticsMetric.ERROR, param);
			}
			mStatisticsListener.onUpdated(STATISTICS_HTTP, map);
		}
	}
    /** 
	 *
     * \brief This method sets a listener to receive statistics events about HLS, DASH or SS playback in NexPlayer&trade;.
     * 
     * A statistics listener should be set after NexPlayer has been created and initialized, but before content playback
     * begins.
     * 
     * \param listener  The listener that will receive playback statistics events.
     * 
     * \see setDuration(int statisticsType, double seconds)
     * \since version 6.40
     */
	public void setListener(IStatisticsListener listener) {
		mStatisticsListener = listener;
	}
	
    /**
     *
     * \brief This method sets the interval of time at which playback statistics will be reported and sent (for HLS, DASH or SS content only).
     * 
     * After a statistics listener has been set, this method can be called to change the interval at which general playback
     * and system statistics are monitored at any time <em>before</em> starting playback. 
     * 
     * The default interval of time for monitoring general and system statistics during HLS, DASH or SS playback is 5000 ms or 5 seconds.
     * 
     * \note  The interval between statistics events can only be set for \c GeneralStatisticsMetric and \c SystemStatisticsMetric requests so 
     *        an error will be returned if any other \c statisticsType is indicated because initial statistics and HTTP statistics 
     *        are event-driven statistics.
     * 
     * 
     * \param statisticsType	The type of statistics to be retrieved at the interval set here.  This should be 
     *                          \c STATISTICS_GENERAL for general playback statistics or \c STATISTICS_SYSTEM for monitoring system 
     *                          statistics during playback.
     * \param seconds			The interval at which statistics will be monitored, in seconds, as a \c double.
     * 
     * \returns if successful or the relevant \c NexStatistics error code (for example, \c RETURN_ERROR_DURATION_INVALID .
     * 
     * \see GeneralStatistics
     * \see SystemStatistics
     * \see StatisticsErrror
     * 
     * \since version 6.40
     */
	public StatisticsError setDuration(int statisticsType, double seconds ) {

		if( statisticsType == STATISTICS_GENERAL) {
			return mGeneralStatisticsMonitor.setTaskDuration( toMilliseconds(seconds) );
		}
		else if(null != mSystemStatisticsMonitor && statisticsType == STATISTICS_SYSTEM) {
			return mSystemStatisticsMonitor.setTaskDuration( toMilliseconds(seconds) );
		}
		return StatisticsError.ERROR_TYPE_INVALID;
	}

	private int toMilliseconds(double seconds) {
		return new Double(seconds *1000).intValue();
	}

	private void startTimer() {
		if( isMonitoringEnabled() ) {
			if( !isTimerActivated(STATISTICS_GENERAL) ) {
				mGeneralStatisticsMonitor.startTimer();
			}
			if(null != mSystemStatisticsMonitor && !isTimerActivated(STATISTICS_SYSTEM)) {
				mSystemStatisticsMonitor.startTimer();
			}
		}
	}

	private void stopTimer() {
		if( isTimerActivated(STATISTICS_GENERAL) ) {
			mGeneralStatisticsMonitor.stopTimer();
		}
		if(null != mSystemStatisticsMonitor && isTimerActivated(STATISTICS_SYSTEM)) {
			mSystemStatisticsMonitor.stopTimer();
		}
		mTotalPlayTime = mNumHttpReq = 0;
	}

	private class StatisticsTimer extends Timer {

		private final Integer MAX_DURATION_MS;
		private final Integer MIN_DURATION_MS;

		private int             mType;
		private int             mTaskPeriod;
		private boolean         mActivated;
		private TimerTask       mTask;
		private NexSystemUtils  mSystemUtil;

		protected StatisticsTimer(int type) {
			mType = type;
			MIN_DURATION_MS = getMinDurDefaultMs();
			MAX_DURATION_MS = getMaxDurDefaultMs();
			mTaskPeriod = 5000;
			mActivated = false;
			mSystemUtil = new NexSystemUtils();
			mSystemUtil.run();
		}

		protected void startTimer() {
			setupTask();
			scheduleAtFixedRate( mTask, 0, mTaskPeriod);
			mActivated = true;
		}

		protected void stopTimer() {
			mTask.cancel();
			mActivated = false;
		}

		protected boolean isActivated() {
			return mActivated;
		}

		protected StatisticsError setTaskDuration(int milliseconds) {
			if( milliseconds >= MIN_DURATION_MS && milliseconds <= MAX_DURATION_MS ) {
				mTaskPeriod = milliseconds;
				return StatisticsError.ERROR_NONE;
			}
			return StatisticsError.ERROR_DURATION_INVALID;
		}

		private int getMinDurDefaultMs() {
			switch( mType ) {
				case STATISTICS_GENERAL : return 2000;
				case STATISTICS_SYSTEM : return 2000;
				default : return 2000;
			}
		}

		private int getMaxDurDefaultMs() {
			switch( mType ) {
				case STATISTICS_GENERAL : return 10000;
				case STATISTICS_SYSTEM : return 10000;
				default : return 10000;
			}
		}

		private void setupTask() {
			if( mType == STATISTICS_GENERAL ) {
				mTask = getGeneralTask();
			}
			else if( mType == STATISTICS_SYSTEM ) {
				mTask = getSystemTask();
			}
		}

		private TimerTask getGeneralTask() {
			return new TimerTask() {
				@Override
				public void run() {
					int playerState = mNexPlayer.getState();
					if( (playerState >= NexPlayer.NEXPLAYER_STATE_STOP) && (playerState <= NexPlayer.NEXPLAYER_STATE_PLAYxN) ) {
						if( mStatisticsListener != null ) {
							NexPlayer.NexRTStreamInformation streamInfo = mNexPlayer.getRTStreamInfo();
							HashMap<IStatistics, Object> map = new HashMap<IStatistics, Object>();

							if( streamInfo != null ) {
								map.put(GeneralStatisticsMetric.PLAY_TIME_SEC,             mTotalPlayTime);
								map.put(GeneralStatisticsMetric.BYTES_RECEIVED,            streamInfo.mNumOfBytesRecv);
								map.put(GeneralStatisticsMetric.CUR_NETWORK_BW_BPS,        new Long( streamInfo.mCurNetworkBw).intValue() );
								map.put(GeneralStatisticsMetric.CUR_TRACK_BW_BPS,          new Long( streamInfo.mCurTrackBw).intValue() );
								map.put(GeneralStatisticsMetric.NUM_SEG_REQUESTS,          new Long( streamInfo.mNumOfSegRequest).intValue() );
								map.put(GeneralStatisticsMetric.NUM_SEG_RECEIVED,          new Long( streamInfo.mNumOfSegReceived).intValue() );
								map.put(GeneralStatisticsMetric.NUM_SEG_DOWN_RATE,         new Long( streamInfo.mNumOfSegDownRate).intValue() );
								map.put(GeneralStatisticsMetric.NUM_SEG_FAIL_TO_PARSE,     new Long( streamInfo.mNumOfSegFailToParse).intValue() );
								map.put(GeneralStatisticsMetric.NUM_SEG_IN_BUFFER,         new Long( streamInfo.mNumOfSegInBuffer).intValue() );
								map.put(GeneralStatisticsMetric.NUM_REQUEST_ERRORS,        new Long( streamInfo.mNumOfSegFailToReceive).intValue() );
								map.put(GeneralStatisticsMetric.NUM_REQUEST_TIMEOUT,       new Long( streamInfo.mNumOfSegTimeout).intValue() );
								map.put(GeneralStatisticsMetric.NUM_TRACK_SWITCH_UP,     new Long( streamInfo.mNumOfTrackSwitchUp).intValue() );
								map.put(GeneralStatisticsMetric.NUM_TRACK_SWITCH_DOWN,     new Long( streamInfo.mNumOfTrackSwitchDown).intValue() );
								map.put(GeneralStatisticsMetric.NUM_VIDEO_FRAME_RENDERED,  mNexPlayer.getContentInfoInt(NexPlayer.CONTENT_INFO_INDEX_VIDEO_RENDER_TOTAL_COUNT) );
								map.put(GeneralStatisticsMetric.NUM_VIDEO_FRAME_DECODED,   mNexPlayer.getContentInfoInt(NexPlayer.CONTENT_INFO_INDEX_VIDEO_CODEC_DECODING_TOTAL_COUNT) );
								map.put(GeneralStatisticsMetric.NUM_HTTP_REQUESTS,         mNumHttpReq );
							}
							mStatisticsListener.onUpdated(STATISTICS_GENERAL, map);
						}
					}
					else {
						stopTimer();
					}
				}
			};
		}

		private TimerTask getSystemTask() {
			return new TimerTask() {
				@Override
				public void run() {
					int playerState = mNexPlayer.getState();
					if( (playerState >= NexPlayer.NEXPLAYER_STATE_STOP) && (playerState <= NexPlayer.NEXPLAYER_STATE_PLAYxN) ) {
						if( mStatisticsListener != null ) {
							HashMap<IStatistics, Object> map = new HashMap<IStatistics, Object>();
							map.put(SystemStatisticsMetric.CPU_USAGE,   new Double( mSystemUtil.getCPUUsage() ) );
							map.put(SystemStatisticsMetric.FREE_MEMORY_KB, new Long( mSystemUtil.getFreeMemory() ) );
							mStatisticsListener.onUpdated(STATISTICS_SYSTEM, map);
							mSystemUtil.run();
						}
					}
					else {
						stopTimer();
					}
				}
			};
		}
	}
}
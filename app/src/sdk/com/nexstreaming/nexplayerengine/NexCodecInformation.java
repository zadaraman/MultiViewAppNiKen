package com.nexstreaming.nexplayerengine;

/**
 * Stores and provides information about an individual codec used by the NexPlayer&trade;&nbsp;engine.
 * 
 * \since version 6.16
 * @author NexStreaming Corp.
 */
public final class NexCodecInformation
{
	/**
	 * The codec version as a \c String. E.g., "1.0" or "1.0.0" 
	 * 
	 * \since version 6.16
	 */
	public String mCodecVersion;
	
	/** The type of media
	 * 
	 * <b>Possible Values:</b>
	 *    - <b>1</b> : Audio codec
	 *    - <b>2</b> : Video codec
	 *
	 */
	public int mMediaType;
	
	/** The classification of the codec.
	 * 
	 * <b>Possible Values:</b>
	 *    - <b>0</b> : SW codec
	 *    - <b>1</b> : HW codec
	 *
	 */
	public int mCodecClass;

	/** 
	 * This can be one of the following video codec constants:
	 *    - \link NexContentInformation#NEXOTI_MPEG4V NEXOTI_MPEG4V\endlink 
	 *    - \link NexContentInformation#NEXOTI_H263 NEXOTI_H263\endlink 
	 *    - \link NexContentInformation#NEXOTI_H264 NEXOTI_H264\endlink 
	 *    - \link NexContentInformation#NEXOTI_WMV NEXOTI_WMV\endlink 
	 *    - \link NexContentInformation#NEXOTI_RV NEXOTI_RV\endlink
	 * 
	 * This can be one of the following audio codec constants:
	 *    - \link NexContentInformation#NEXOTI_AAC NEXOTI_AAC\endlink
	 *    - \link NexContentInformation#NEXOTI_AAC_GENERIC NEXOTI_AAC_GENERIC\endlink
	 *    - \link NexContentInformation#NEXOTI_AAC_PLUS NEXOTI_AAC_PLUS\endlink
	 *    - \link NexContentInformation#NEXOTI_MPEG2AAC NEXOTI_MPEG2AAC\endlink
	 *    - \link NexContentInformation#NEXOTI_MP3inMP4 NEXOTI_MP3inMP4\endlink
	 *    - \link NexContentInformation#NEXOTI_MP2 NEXOTI_MP2\endlink
	 *    - \link NexContentInformation#NEXOTI_MP3 NEXOTI_MP3\endlink
	 *    - \link NexContentInformation#NEXOTI_BSAC NEXOTI_BSAC\endlink
	 *    - \link NexContentInformation#NEXOTI_WMA NEXOTI_WMA\endlink
	 *    - \link NexContentInformation#NEXOTI_RA NEXOTI_RA\endlink
	 *    - \link NexContentInformation#NEXOTI_AC3 NEXOTI_AC3\endlink
 	 *    - \link NexContentInformation#NEXOTI_EC3 NEXOTI_EC3\endlink
	 *    - \link NexContentInformation#NEXOTI_AC4 NEXOTI_AC4\endlink
	 *    - \link NexContentInformation#NEXOTI_DRA NEXOTI_DRA\endlink
	 * Or (in future versions) one of the following speech codec constants:
	 *    - \link NexContentInformation#NEXOTI_AMR NEXOTI_AMR\endlink
	 *    - \link NexContentInformation#NEXOTI_EVRC NEXOTI_EVRC\endlink
	 *    - \link NexContentInformation#NEXOTI_QCELP NEXOTI_QCELP\endlink
	 *    - \link NexContentInformation#NEXOTI_QCELP_ALT NEXOTI_QCELP_ALT\endlink
	 *    - \link NexContentInformation#NEXOTI_SMV NEXOTI_SMV\endlink
	 *    - \link NexContentInformation#NEXOTI_AMRWB NEXOTI_AMRWB\endlink
	 *    - \link NexContentInformation#NEXOTI_G711 NEXOTI_G711\endlink
	 *    - \link NEXOTI_G723\endlink	 
	 */
	public int mCodecID; 

	/** 
	 * The CPU architecture information.
	 * 
	 * This is one of:
	 * - \link NexSystemInfo#NEX_SUPPORT_CPU_ARMV5 NEX_SUPPORT_CPU_ARMV5\endlink
	 * - \link NexSystemInfo#NEX_SUPPORT_CPU_ARMV6 NEX_SUPPORT_CPU_ARMV6\endlink
	 * - \link NexSystemInfo#NEX_SUPPORT_CPU_ARMV7 NEX_SUPPORT_CPU_ARMV7\endlink
	 */	
	public int mCpuInfo; 
	
	/**
	 * \brief The sole initializer for this class.
	 * 
	 * The arguments match the names of the relevant member variables, and
	 * are simply assigned on a 1-to-1 basis.
	 *
	 * @param strCodecVersion  Initializes mCodecVersion.
	 * @param iMediaType       Initializes mMediaType.
	 * @param iCodecClass      Initializes mCodecClass.
	 * @param iCodecID         Initializes mCodecID.
	 * @param iCpuInfo         Initializes mCpuInfo.
	 * 
	 */
	public NexCodecInformation(String strCodecVersion, int iMediaType, int iCodecClass, int iCodecID, int iCpuInfo)
	{
		mCodecVersion = strCodecVersion;
		mMediaType = iMediaType;
		mCodecClass = iCodecClass;
		mCodecID = iCodecID;
		mCpuInfo = iCpuInfo;
	}
}

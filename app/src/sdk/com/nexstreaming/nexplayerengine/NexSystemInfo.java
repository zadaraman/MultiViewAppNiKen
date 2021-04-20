package com.nexstreaming.nexplayerengine;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


/**
 * \brief  This class provides NexPlayer&trade;&nbsp;with information about the system 
 *         and device.  
 */
public class NexSystemInfo {
	private static String TAG = "NexSystemInfo";

    /** Return value of getPlatformInfo() for checking Android Version,   
     *  for a not supported platform.
     */
	public static final int NEX_SUPPORT_PLATFORM_NOTHING = 0x0;
    
    /** Return value of getPlatformInfo() for checking Android Version,
     * for Cupcake.  
     */
    public static final int NEX_SUPPORT_PLATFORM_CUPCAKE = 0x15;
    /** Return value of getPlatformInfo() for checking Android Version,  
     *  for Donut.
     */
    public static final int NEX_SUPPORT_PLATFORM_DONUT = 0x16;
    /** Return value of getPlatformInfo() for checking Android Version,
     *  for Eclair.  
     */
    public static final int NEX_SUPPORT_PLATFORM_ECLAIR = 0x21;
    /** Return value of getPlatformInfo() for checking Android Version,
     *  for Froyo.  
     */
    public static final int NEX_SUPPORT_PLATFORM_FROYO = 0x22;
    /** Return value of getPlatformInfo() for checking Android Version,
     *  for Gingerbread.  
     */
    public static final int NEX_SUPPORT_PLATFORM_GINGERBREAD = 0x30;
    /** Return value of getPlatformInfo() for checking Android Version,
     *  for Honeycomb.  
     */ 
    public static final int NEX_SUPPORT_PLATFORM_HONEYCOMB = 0x31;
    /**
     *  Return value of getPlatformInfo() for checking Android Version,
     *  for Ice Cream Sandwich.
     */    
    public static final int NEX_SUPPORT_PLATFORM_ICECREAM_SANDWICH = 0x40;
    /**
     *  Return value of getPlatformInfo() for checking Android Version,
     *  for Jelly Bean.
     */    
    public static final int NEX_SUPPORT_PLATFORM_JELLYBEAN = 0x41;
    /**
     *  Return value of getPlatformInfo() for checking Android Version,
     *  for Jelly Bean.
     */    
    public static final int NEX_SUPPORT_PLATFORM_JELLYBEAN3 = 0x43;
    /**
     *  Return value of getPlatformInfo() for checking Android Version,
     *  for KitKat.
     */ 
    public static final int NEX_SUPPORT_PLATFORM_KITKAT = 0x44;
    /**
     *  Return value of getPlatformInfo() for checking Android Version,
     *  for Lollipop.
     */ 
    public static final int NEX_SUPPORT_PLATFORM_LOLLIPOP = 0x50;
    /**
     *  Return value of getPlatformInfo() for checking Android Version,
     *  for Marshmallow.
     */ 
    public static final int NEX_SUPPORT_PLATFORM_MARSHMALLOW = 0x60;
	/**
	 *  Return value of getPlatformInfo() for checking Android Version,
	 *  for Nougat.
	 */
	public static final int NEX_SUPPORT_PLATFORM_NOUGAT = 0x70;
	/**
	 *  Return value of getPlatformInfo() for checking Android Version,
	 *  for OREO.
	 */
	public static final int NEX_SUPPORT_PLATFORM_OREO = 0x80;
    
    /** 
     * \brief This method returns the Android Version of the device platform.
     * 
     * \return Android Version; one of:
     * - <b> \link NexSystemInfo::NEX_SUPPORT_PLATFORM_CUPCAKE NEX_SUPPORT_PLATFORM_CUPCAKE\endlink</b>
     * - <b> \link NexSystemInfo::NEX_SUPPORT_PLATFORM_DONUT NEX_SUPPORT_PLATFORM_DONUT\endlink</b>
     * - <b> \link NexSystemInfo::NEX_SUPPORT_PLATFORM_ECLAIR NEX_SUPPORT_PLATFORM_ECLAIR\endlink</b> 
     * - <b> \link NexSystemInfo::NEX_SUPPORT_PLATFORM_FROYO NEX_SUPPORT_PLATFORM_FROYO\endlink</b>
     * - <b> \link NexSystemInfo::NEX_SUPPORT_PLATFORM_GINGERBREAD NEX_SUPPORT_PLATFORM_GINGERBREAD\endlink</b>
     * - <b> \link NexSystemInfo::NEX_SUPPORT_PLATFORM_HONEYCOMB NEX_SUPPORT_PLATFORM_HONEYCOMB\endlink</b>  
     * - <b> \link NexSystemInfo::NEX_SUPPORT_PLATFORM_ICECREAM_SANDWICH NEX_SUPPORT_PLATFORM_ICECREAM_SANDWICH\endlink</b>
     * - <b> \link NexSystemInfo::NEX_SUPPORT_PLATFORM_JELLYBEAN NEX_SUPPORT_PLATFORM_JELLYBEAN\endlink</b>
     * - <b> \link NexSystemInfo::NEX_SUPPORT_PLATFORM_MARSHMALLOW NEX_SUPPORT_PLATFORM_MARSHMALLOW\endlink</b>
	 * - <b> \link NexSystemInfo::NEX_SUPPORT_PLATFORM_NOUGAT NEX_SUPPORT_PLATFORM_NOUGAT\endlink</b>
	 * - <b> \link NexSystemInfo::NEX_SUPPORT_PLATFORM_OREO NEX_SUPPORT_PLATFORM_OREO\endlink</b>
     * - <b> \link NexSystemInfo::NEX_SUPPORT_PLATFORM_NOTHING NEX_SUPPORT_PLATFORM_NOTHING\endlink</b>  
     */
    public static int getPlatformInfo()
    {
		if (android.os.Build.VERSION.SDK_INT == 10000) {
			return NEX_SUPPORT_PLATFORM_LOLLIPOP;  //CUR_DEVELOPMENT = 10000, Magic version number for a current development build, which has not yet turned into an official release.
		}
        int iPlatform = 0;
        String strVersion =android.os.Build.VERSION.RELEASE;
        NexLog.d(TAG, "PLATFORM INFO: " + strVersion + " SDK: " + android.os.Build.VERSION.SDK_INT);
        switch( android.os.Build.VERSION.SDK_INT ) 
        {
        case 1: // BASE, October 2008: The original, first, version of Android.
        case 2: // BASE_1_1, February 2009: First Android update, officially called 1.1.
			iPlatform = NEX_SUPPORT_PLATFORM_LOLLIPOP;
        	break;
    	case 3 : //CUPCAKE, May 2009: Android 1.5.
        	iPlatform = NEX_SUPPORT_PLATFORM_CUPCAKE;
        	break;
        case 4 : //DONUT, September 2009: Android 1.6.
        	iPlatform = NEX_SUPPORT_PLATFORM_DONUT;
        	break;
        case 5 : //ECLAIR, November 2009: Android 2.0.
        case 6 : //ECLAIR_0_1, December 2009: Android 2.0.1.
        case 7 : //ECLAIR_MR1, January 2010: Android 2.1.
        	iPlatform = NEX_SUPPORT_PLATFORM_ECLAIR;
        	break;
        case 8 : //FROYO, June 2010: Android 2.2.
        	iPlatform = NEX_SUPPORT_PLATFORM_FROYO;
        	break;
        case 9 : //GINGERBREAD, November 2010: Android 2.3.
        case 10 : //GINGERBREAD_MR1, February 2011: Android 2.3.3.
        	iPlatform = NEX_SUPPORT_PLATFORM_GINGERBREAD;
        	break;
        case 11 : //HONEYCOMB, February 2011: Android 3.0.
        case 12 : //HONEYCOMB_MR1, May 2011: Android 3.1.
        case 13 : //HONEYCOMB_MR2, June 2011: Android 3.2.
        	iPlatform = NEX_SUPPORT_PLATFORM_HONEYCOMB;
        	break;
        case 14 : //ICE_CREAM_SANDWICH, October 2011: Android 4.0.
        case 15 : //ICE_CREAM_SANDWICH_MR1, December 2011: Android 4.0.3.
        	iPlatform = NEX_SUPPORT_PLATFORM_ICECREAM_SANDWICH;
        	break;
        case 16 : //JELLY_BEAN, June 2012: Android 4.1.
        case 17 : //JELLY_BEAN_MR1, Android 4.2: Moar jelly beans!
        	iPlatform = NEX_SUPPORT_PLATFORM_JELLYBEAN;
        	break;
        case 18 : //JELLY_BEAN_MR2, Android 4.3: Jelly Bean MR2, the revenge of the beans.
        	iPlatform = NEX_SUPPORT_PLATFORM_JELLYBEAN3;
        	break;
        case 19 : //KITKAT, Android 4.4: KitKat, another tasty treat.
        case 20 : //KITKAT_WATCH, Android 4.4W: KitKat for watches, snacks on the run.
        	iPlatform = NEX_SUPPORT_PLATFORM_KITKAT;
        	break;
        case 21 :
		case 22 :
        	iPlatform = NEX_SUPPORT_PLATFORM_LOLLIPOP;
        	break;
        case 23 :
        	iPlatform = NEX_SUPPORT_PLATFORM_MARSHMALLOW;
			break;
		case 24:
		case 25:
			iPlatform = NEX_SUPPORT_PLATFORM_NOUGAT;
			break;
		case 26:
			iPlatform = NEX_SUPPORT_PLATFORM_OREO;
			break;
        default :
        	iPlatform = NEX_SUPPORT_PLATFORM_OREO;
        	break;
        }

        return iPlatform;
    }
	/**
	 * \brief This extracts the device model name from system properties.
	 */
    public static String getDeviceInfo()
    {
        return android.os.Build.MODEL;
    }	
	
	private static String getCPUInfoField( String cpuInfo, String field_name ) {
		String findStr = "\n"+field_name+"\t: ";
		int stringStart = cpuInfo.indexOf(findStr);
		if( stringStart < 0 ) {
			findStr = "\n"+field_name+": ";
			stringStart = cpuInfo.indexOf(findStr);
			if( stringStart < 0 )
				return null;
		}
		int start = stringStart+findStr.length();
		int end = cpuInfo.indexOf("\n", start);
		return cpuInfo.substring(start, end);
	}
	
	// Reads the CPU info file from the current system
	private static String ReadCPUinfo() 
	{
		String result = "";
		if( new File("/proc/cpuinfo").exists())
		{
			try {
				BufferedReader brCpuInfo = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
	            String aLine;

	            if( brCpuInfo != null)
	            {
					while ((aLine = brCpuInfo.readLine()) != null) 
					{
						result = result + aLine + "\n";
					}
					brCpuInfo.close();
	            }
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}    
    
	/** Return value of getCPUInfo() for ARMV8.  
	 */
	public static final int NEX_SUPPORT_CPU_ARM64_V8A = 0x8;
	/** Return value of getCPUInfo() for ARMV7.  
	 */
	public static final int NEX_SUPPORT_CPU_ARMV7 = 0x7;
	/** Return value of getCPUInfo() for ARMV6.
	 */
	public static final int NEX_SUPPORT_CPU_ARMV6 = 0x6;
	/** Return value of getCPUInfo() for ARMV5.
	 */
	public static final int NEX_SUPPORT_CPU_ARMV5 = 0x5;
	
	/** Return value of getCPUInfo() for Intel x86 chipsets.
	 * \since version 6.3.1
	 */ 
	public static final int NEX_SUPPORT_CPU_X86 = 0x86;

	public static final int NEX_SUPPORT_CPU_X86_64 = 0x64;
	
	/** Allows the CPU architecture (whether it should be viewed as x86 or ARM) to be set externally. 
	 * Set this to \c TRUE to ignore internal settings and force set the CPU architecture to ARM, or \c FALSE to check whether the internal setting is set to ARM or x86. 
	 *
	 * \since version 6.22
	 *
	 */
	public static boolean x86Disabled = false;
	
	/** 
	 * \brief This method returns the CPU architecture information.
	 * 
	 * \return CPU architecture information; one of:
	 * - <b> \link NexSystemInfo::NEX_SUPPORT_CPU_ARMV5 NEX_SUPPORT_CPU_ARMV5\endlink</b>
	 * - <b> \link NexSystemInfo::NEX_SUPPORT_CPU_ARMV6 NEX_SUPPORT_CPU_ARMV6\endlink</b>
	 * - <b> \link NexSystemInfo::NEX_SUPPORT_CPU_ARMV7 NEX_SUPPORT_CPU_ARMV7\endlink</b>
	 * - <b> \link NexSystemInfo::NEX_SUPPORT_CPU_ARM64_V8A NEX_SUPPORT_CPU_ARM64_V8A\endlink</b>
	 * - <b> \link NexSystemInfo::NEX_SUPPORT_CPU_X86 NEX_SUPPORT_CPU_X86\endlink</b>
	 * - <b> \link NexSystemInfo::NEX_SUPPORT_CPU_X86_64 NEX_SUPPORT_CPU_X86_64\endlink</b> */
	public static int getCPUInfo()
	{
		int iCPUInfo = 0;
		String cpuInfo = ReadCPUinfo();
		String cpuArchitecture = getCPUInfoField(cpuInfo, "CPU architecture");
		String cpuABI = android.os.Build.CPU_ABI;
		NexLog.d(TAG, "cpu ABI: " + cpuABI);

		String cpuFeature = getCPUInfoField(cpuInfo, "Features");
		if (cpuArchitecture != null)
			NexLog.d(TAG, "cpuArchitecture: " + cpuArchitecture);
		else
			NexLog.e(TAG, "cpuArchitecture is null!!");
		if (cpuInfo != null)
			NexLog.d(TAG, "CPU INFO: " + cpuInfo);

		String strArch = System.getProperty("os.arch");

		if (strArch == null) {
			NexLog.d(TAG, "os.arch is null!");
			return NEX_SUPPORT_CPU_ARMV7;
		}

		if (strArch.contentEquals("i686") || strArch.contains("x86")) {
			NexLog.d(TAG, "CPU INFO arch: " + strArch);

			if (x86Disabled) {
				NexLog.d(TAG, "CPU Architecture is set from external");
				return NEX_SUPPORT_CPU_ARMV7;
			}
			if (cpuArchitecture != null) {
				if (cpuArchitecture.startsWith("7") || cpuArchitecture.startsWith("8")) {
					iCPUInfo = NEX_SUPPORT_CPU_ARMV7;
				} else {
					if (cpuABI.contentEquals("x86_64")) {
						iCPUInfo = NEX_SUPPORT_CPU_X86_64;
					} else {
						iCPUInfo = NEX_SUPPORT_CPU_X86;
					}
				}
			} else {
				NexLog.e(TAG, "Cannot get CPUINFO!!");
				if (cpuABI.contentEquals("x86_64")) {
					iCPUInfo = NEX_SUPPORT_CPU_X86_64;
				} else {
					iCPUInfo = NEX_SUPPORT_CPU_X86;
				}
			}
		} else if (strArch.contentEquals("AArch64") || "arm64-v8a".equals(cpuABI))
			iCPUInfo = NEX_SUPPORT_CPU_ARM64_V8A;
		else {
			if (cpuArchitecture == null) {
				NexLog.e(TAG, "Cannot get CPUINFO!!");
				return NEX_SUPPORT_CPU_ARMV7;
			}

			boolean bNeon = false;

			if (cpuFeature != null)
				bNeon = cpuFeature.contains("neon");

			if (cpuArchitecture.startsWith("7") || cpuArchitecture.startsWith("8")) {
				if (bNeon) {
					iCPUInfo = NEX_SUPPORT_CPU_ARMV7;
				} else {
					iCPUInfo = NEX_SUPPORT_CPU_ARMV6;
				}
			} else if (cpuArchitecture.startsWith("6")) {
				iCPUInfo = NEX_SUPPORT_CPU_ARMV6;
			} else {
				iCPUInfo = NEX_SUPPORT_CPU_ARMV5;
			}
		}
		return iCPUInfo;
	}

	public static int [] getExtraEncodinglist(Context context) {
		Intent intent = context.registerReceiver(null, new IntentFilter(AudioManager.ACTION_HDMI_AUDIO_PLUG));
		int[] ret = new int[]{0xFFFFFFFF};
		if (intent != null) {
			ret = intent.getIntArrayExtra(AudioManager.EXTRA_ENCODINGS);
			ret = ret != null ? ret : new int[]{0xFFFFFFFF};
		}
		return ret;
	}
}

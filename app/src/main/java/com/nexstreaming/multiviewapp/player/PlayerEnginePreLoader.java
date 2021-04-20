package com.nexstreaming.multiviewapp.player;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;

import com.nexstreaming.nexplayerengine.NexSystemInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class PlayerEnginePreLoader {

    private static boolean mSharedLibLoaded = false;
    private static String TAG = "NexPlayerEnginePreLoader";
    private static int mCodecMode = 3;

    PlayerEnginePreLoader() {
    }

    public static boolean isLoaded() {
        return mSharedLibLoaded;
    }


    public static int Load(String libPath, Context context, int nCodecMode) {
        Log.d(TAG, " Load () is called..  + " + nCodecMode);
        mSharedLibLoaded = false;
        Log.d(TAG, "Success Load Shared Library! LibPath:" + libPath);
        mCodecMode = nCodecMode;
        Log.d(TAG, "Success Load Shared Library! >> nCodecMode:" + nCodecMode);

        if (installDeviceDependentSharedLibrary(libPath, context)) {
            Log.d(TAG, "Success Load Shared Library!");
            mSharedLibLoaded = true;
            return 0;
        } else {
            Log.e(TAG, "failed installing shared libraries...");
            return 1;
        }
    }

    private static boolean installDeviceDependentSharedLibrary(String libPath, Context context) {
        boolean result = false;

        int iPlatform = NexSystemInfo.getPlatformInfo();

        ArrayList<String> dependentLib = new ArrayList<String>();
        ArrayList<String> assetList = new ArrayList<String>();
        int cpuType = NexSystemInfo.getCPUInfo();

        switch (cpuType) {
            case NexSystemInfo.NEX_SUPPORT_CPU_X86_64:
                assetList.add("x86_64");
                break;
            case NexSystemInfo.NEX_SUPPORT_CPU_X86:
                assetList.add("x86");
                break;
            case NexSystemInfo.NEX_SUPPORT_CPU_ARM64_V8A:
                assetList.add("arm64-v8a");
                break;
            case NexSystemInfo.NEX_SUPPORT_CPU_ARMV7:
                assetList.add("armeabi-v7a");
            case NexSystemInfo.NEX_SUPPORT_CPU_ARMV5:
            case NexSystemInfo.NEX_SUPPORT_CPU_ARMV6:
                assetList.add("armeabi");
                break;
            default:
                assetList.add("empty");
                break;
        }

        for (String asset : assetList) {
            if (_copyInternal(libPath, iPlatform, context, asset, dependentLib, cpuType, true)) {
                result = true;
                break;
            }
        }

        if ((System.getProperty("os.arch").contentEquals("i686") || System.getProperty("os.arch").contains("x86"))
                && "armeabi".equals(assetList.get(0))) {
            // when using arm emulator.
            result = _copyInternal(libPath, iPlatform, context, "x86", dependentLib, 0x86, false);
        }

        return result;
    }

    private static boolean _copyInternal(String libPath, int iPlatform, Context context, String fileWithAsset, ArrayList<String> dependentLib, int cpuInfo, boolean copyRalbodies) {
        // initializeSharedLibrary(dependentLib, Build.VERSION.SDK_INT, NexSystemInfo.getCPUInfo());
        initializeSharedLibrary(dependentLib, iPlatform, cpuInfo, copyRalbodies);

        try {
            Log.d(TAG, " copy start ! ");
            AssetManager manager = context.getAssets();
            Log.d(TAG, " fileWithAsset !: " + fileWithAsset);
            if (0 >= manager.list(fileWithAsset).length) {
                return false;
            }

            for (String file : manager.list(fileWithAsset)) {
                for (int i = 0; i < dependentLib.size(); i++) {
                    if (file.equals(dependentLib.get(i))) {
                        Log.d(TAG, " lib path : " + libPath);
                        copyAPKEntry2Path(manager, file, fileWithAsset, libPath + file);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /*Rex; depLib replaced with arraylist
    private static boolean initializeSharedLibrary(String[] depLib, int sdk, int cpu)
    */
    private static boolean initializeSharedLibrary(ArrayList<String> depLib, int sdk, int cpu, boolean copyRalbodies) {
        Log.d(TAG, " initializeSharedLibrary()  " + " current  : " + mCodecMode);
        ArrayList<String> codecLib = new ArrayList<String>();

        // Select Codec libraries by CodecMode value
        if (mCodecMode == 1) {
            //SW audio decoder
            switch (cpu) {
                case NexSystemInfo.NEX_SUPPORT_CPU_ARMV7:
                    codecLib.add("libnexcal_dolby_armv");
                case NexSystemInfo.NEX_SUPPORT_CPU_ARMV6:
                case NexSystemInfo.NEX_SUPPORT_CPU_ARMV5:
                    codecLib.add("libnexcal_aac_armv");
                    codecLib.add("libnexcal_h264_armv");
                    codecLib.add("libnexcal_hevc_armv");
                    codecLib.add("libnexcal_mp3_armv");
                    if (sdk >= NexSystemInfo.NEX_SUPPORT_PLATFORM_HONEYCOMB) //Starting from Honeycomb
                        codecLib.add("libnexcal_dts_armv");
                    codecLib.add("libnexcal_amr_armv");
                    codecLib.add("libnexcal_divx_armv");
                    codecLib.add("libnexcal_wma_armv");
                    codecLib.add("libnexcal_wmv_armv");
                    codecLib.add("libnexcal_mpeg2_armv");
                    break;
                case NexSystemInfo.NEX_SUPPORT_CPU_ARM64_V8A:
                    codecLib.add("libnexcal_aac_arm64-v8a.so");
                    codecLib.add("libnexcal_h264_arm64-v8a.so");
                    codecLib.add("libnexcal_mp3_arm64-v8a.so");
                    codecLib.add("libnexcal_hevc_arm64-v8a.so");
                    codecLib.add("libnexcal_amr_arm64-v8a.so");
                    codecLib.add("libnexcal_divx_arm64-v8a.so");
                    codecLib.add("libnexcal_wma_arm64-v8a.so");
                    codecLib.add("libnexcal_wmv_arm64-v8a.so");
                    codecLib.add("libnexcal_mpeg2_arm64-v8a.so");
                    break;
                case NexSystemInfo.NEX_SUPPORT_CPU_X86_64:
                    codecLib.add("libnexcal_aac_x86_64.so");
                    codecLib.add("libnexcal_h264_x86_64.so");
                    codecLib.add("libnexcal_mp3_x86_64.so");
                    break;
                case NexSystemInfo.NEX_SUPPORT_CPU_X86:
                    codecLib.add("libnexcal_aac_x86.so");
                    codecLib.add("libnexcal_h264_x86.so");
                    codecLib.add("libnexcal_mp3_x86.so");
                    break;
                default:
            }
        } else if (mCodecMode == 2) {
            //SW audio decoder
            if (cpu == NexSystemInfo.NEX_SUPPORT_CPU_ARMV7 ||
                    cpu == NexSystemInfo.NEX_SUPPORT_CPU_ARMV6 ||
                    cpu == NexSystemInfo.NEX_SUPPORT_CPU_ARMV5) {
                //HW((internal) audio decoder
                codecLib.add("libnexcal_in_amr_armv");
                codecLib.add("libnexcal_in_aac_armv");
                codecLib.add("libnexcal_in_mp3_armv");
            } else if (cpu == NexSystemInfo.NEX_SUPPORT_CPU_ARM64_V8A) {
                codecLib.add("libnexcal_in_amr_arm64-v8a.so");
                codecLib.add("libnexcal_in_aac_arm64-v8a.so");
                codecLib.add("libnexcal_in_mp3_arm64-v8a.so");
            } else if (cpu == NexSystemInfo.NEX_SUPPORT_CPU_X86_64) {
                codecLib.add("libnexcal_in_amr_x86_64.so");
                codecLib.add("libnexcal_in_aac_x86_64.so");
                codecLib.add("libnexcal_in_mp3_x86_64.so");
            } else {
                codecLib.add("libnexcal_in_amr_x86.so");
                codecLib.add("libnexcal_in_aac_x86.so");
                codecLib.add("libnexcal_in_mp3_x86.so");
            }
        } else {
            //SW audio decoder
            switch (cpu) {
                case NexSystemInfo.NEX_SUPPORT_CPU_ARMV7:
                    codecLib.add("libnexcal_dolby_armv");
                case NexSystemInfo.NEX_SUPPORT_CPU_ARMV6:
                case NexSystemInfo.NEX_SUPPORT_CPU_ARMV5:
                    codecLib.add("libnexcal_aac_armv");
                    codecLib.add("libnexcal_h264_armv");
                    codecLib.add("libnexcal_mp3_armv");
                    if (sdk >= NexSystemInfo.NEX_SUPPORT_PLATFORM_HONEYCOMB) //Starting from Honeycomb
                        codecLib.add("libnexcal_dts_armv");
                    codecLib.add("libnexcal_amr_armv");
                    codecLib.add("libnexcal_divx_armv");
                    codecLib.add("libnexcal_wma_armv");
                    codecLib.add("libnexcal_wmv_armv");
                    codecLib.add("libnexcal_mpeg2_armv");
                    //HW((internal) audio decoder
                    codecLib.add("libnexcal_in_amr_armv");
                    codecLib.add("libnexcal_in_aac_armv");
                    codecLib.add("libnexcal_in_mp3_armv");
                    codecLib.add("libnexcal_hevc_armv");
                    break;
                case NexSystemInfo.NEX_SUPPORT_CPU_ARM64_V8A:

                    codecLib.add("libnexcal_aac_arm64-v8a.so");
                    codecLib.add("libnexcal_h264_arm64-v8a.so");
                    codecLib.add("libnexcal_mp3_arm64-v8a.so");
                    codecLib.add("libnexcal_hevc_arm64-v8a.so");
                    codecLib.add("libnexcal_amr_arm64-v8a.so");
                    codecLib.add("libnexcal_divx_arm64-v8a.so");
                    codecLib.add("libnexcal_wma_arm64-v8a.so");
                    codecLib.add("libnexcal_wmv_arm64-v8a.so");
                    codecLib.add("libnexcal_mpeg2_arm64-v8a.so");
                    codecLib.add("libnexcal_in_amr_arm64-v8a.so");
                    codecLib.add("libnexcal_in_aac_arm64-v8a.so");
                    codecLib.add("libnexcal_in_mp3_arm64-v8a.so");
                    break;
                case NexSystemInfo.NEX_SUPPORT_CPU_X86_64:
                    codecLib.add("libnexcal_aac_x86_64.so");
                    codecLib.add("libnexcal_h264_x86_64.so");
                    codecLib.add("libnexcal_mp3_x86_64.so");
                    break;
                case NexSystemInfo.NEX_SUPPORT_CPU_X86:
                    codecLib.add("libnexcal_aac_x86.so");
                    codecLib.add("libnexcal_h264_x86.so");
                    codecLib.add("libnexcal_mp3_x86.so");
                    break;
                default:
            }
        }

        Log.d(TAG, "PreLoad. codecMode: " + mCodecMode);

        // chooses shared libraries dependent on cpu
        for (String codecName : codecLib) {
            if (cpu == NexSystemInfo.NEX_SUPPORT_CPU_ARMV7)
                depLib.add(codecName + "7.so");
            else if (cpu == NexSystemInfo.NEX_SUPPORT_CPU_ARMV6)
                depLib.add(codecName + "6.so");
            else if (cpu == NexSystemInfo.NEX_SUPPORT_CPU_ARMV5)
                depLib.add(codecName + "5.so");
            else {
                depLib.add(codecName);
            }
        }
        if (cpu == NexSystemInfo.NEX_SUPPORT_CPU_ARMV7) {
            depLib.add("libnexcal_closedcaption_arm-v7a.so");
            depLib.add("libnexcal_3gpp_arm-v7a.so");
            depLib.add("libnexcal_ttml_arm-v7a.so");
            depLib.add("libnexcal_webvtt_arm-v7a.so");
        } else if (cpu == NexSystemInfo.NEX_SUPPORT_CPU_ARMV6 ||
                cpu == NexSystemInfo.NEX_SUPPORT_CPU_ARMV5) {
            depLib.add("libnexcal_closedcaption.so");
            depLib.add("libnexcal_3gpp.so");
            depLib.add("libnexcal_ttml.so");
            depLib.add("libnexcal_webvtt.so");
        } else if (cpu == NexSystemInfo.NEX_SUPPORT_CPU_ARM64_V8A) {
            depLib.add("libnexcal_closedcaption_arm64-v8a.so");
            depLib.add("libnexcal_3gpp_arm64-v8a.so");
            depLib.add("libnexcal_ttml_arm64-v8a.so");
            depLib.add("libnexcal_webvtt_arm64-v8a.so");

        } else if (cpu == NexSystemInfo.NEX_SUPPORT_CPU_X86_64) {
            depLib.add("libnexcal_closedcaption_x86_64.so");
            depLib.add("libnexcal_3gpp_x86_64.so");
            depLib.add("libnexcal_ttml_x86_64.so");
            depLib.add("libnexcal_webvtt_x86_64.so");
        } else {
            depLib.add("libnexcal_closedcaption_x86.so");
            depLib.add("libnexcal_3gpp_x86.so");
            depLib.add("libnexcal_ttml_x86.so");
            depLib.add("libnexcal_webvtt_x86.so");
        }

        Log.d(TAG, "Codec Length size: " + depLib.size());
        Log.d(TAG, "SDK Version :" + sdk);

        if (copyRalbodies) {
            depLib.add("libnexralbody_audio.so");

            if (sdk <= NexSystemInfo.NEX_SUPPORT_PLATFORM_DONUT) //cupcake & donut
            {
                depLib.add("libnexralbody_video_cu.so");
            } else if (sdk > NexSystemInfo.NEX_SUPPORT_PLATFORM_DONUT && sdk <= NexSystemInfo.NEX_SUPPORT_PLATFORM_ECLAIR)    //eclair
            {
                depLib.add("libnexralbody_video_ec.so");
            } else if (sdk == NexSystemInfo.NEX_SUPPORT_PLATFORM_FROYO) //Honeycomb
            {
                depLib.add("libnexralbody_video_fr.so");
            } else if (sdk == NexSystemInfo.NEX_SUPPORT_PLATFORM_GINGERBREAD) //Gingerbread
            {
                depLib.add("libnexralbody_video_gb.so");
            } else if (sdk == NexSystemInfo.NEX_SUPPORT_PLATFORM_HONEYCOMB)    //Honeycomb
            {
                depLib.add("libnexralbody_video_opengl.so");
            } else if (sdk == NexSystemInfo.NEX_SUPPORT_PLATFORM_ICECREAM_SANDWICH) //Icecream
            {
                depLib.add("libnexralbody_video_nw.so");
            } else //jellybean
            {
                depLib.add("libnexralbody_video_nw.so");
                depLib.add("libnexralbody_video_opengl.so");
            }


            if (sdk >= NexSystemInfo.NEX_SUPPORT_PLATFORM_FROYO && sdk <= NexSystemInfo.NEX_SUPPORT_PLATFORM_GINGERBREAD) {
                depLib.add("libnexralbody_video_fr3.so");
            }


            if (mCodecMode == 2 || mCodecMode == 3 || mCodecMode == 4 || mCodecMode == 11 || mCodecMode == 12 || mCodecMode == 13) {
                if (sdk == NexSystemInfo.NEX_SUPPORT_PLATFORM_GINGERBREAD) {
                    depLib.add("libnexcal_oc_gb.so");
                    depLib.add("libnexral_surf_gb.so");
                } else if (sdk == NexSystemInfo.NEX_SUPPORT_PLATFORM_ICECREAM_SANDWICH) //Icecream sandwich
                {
                    depLib.add("libnexcal_oc_ics.so");
                    depLib.add("libnexral_nw_ics.so");
                } else {
                    depLib.add("libnexcal_oc_jb.so");
                    depLib.add("libnexral_nw_jb.so");
                    depLib.add("libnexcralbody_mc_jb.so");
                }
            }
        }
        Log.d(TAG, "Length size" + depLib.size());
        return true;
    }

    private static void copyAPKEntry2Path(AssetManager manager, String name, String cpuType, String path) {
        final int BUFFER_SIZE = 8192;
        InputStream inputstream = null;
        byte[] array = new byte[BUFFER_SIZE];
        int byteRead = 0;
        String strAssetPath = cpuType + "/";
        File file = new File(path);
        FileOutputStream fileoutput = null;

        int totalsize = 0;
        try {
            inputstream = manager.open(strAssetPath + name);
            if (null != inputstream) {
                while (true) {
                    byteRead = inputstream.read(array);
                    totalsize += byteRead;

                    try {
                        if (fileoutput == null)
                            fileoutput = new FileOutputStream(file);
                    } catch (Exception e) {
                        Log.d(TAG, " logerror");
                        Log.e(TAG, e.getMessage());
                    }

                    if (byteRead != -1) {
                        fileoutput.write(array, 0, byteRead);
                    } else {
                        break;
                    }
                }
                fileoutput.flush();
                fileoutput.close();
            }

        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }

        Log.d(TAG, "copyAPKEntry2Path end: " + name + " size:" + totalsize);
    }

    public static void deleteAPKAsset(Context context) {
        String libPath = context.getApplicationInfo().dataDir + "/";
        Log.d(TAG, "so files in " + libPath + " will be removed ");
        File dir = new File(libPath);
        File[] files = dir.listFiles();

        if (files == null) {
            Log.d(TAG, libPath + " is empty!");
            return;
        }

        for (File f : files) {// remove them all
            if (f.isFile() && f.getName().endsWith(".so")) {
                if (f.delete()) {
                    Log.d(TAG, f.getAbsolutePath() + " remove successful");
                } else {
                    Log.w(TAG, f.getAbsolutePath() + " remove failed");
                }
            }
        }

        mSharedLibLoaded = false;

    }


    public static String getEnginePath(Context context) {
        String engine = "libnexplayerengine.so";

        Context iContext = context.getApplicationContext();
        String strPath = iContext.getFilesDir().getAbsolutePath();

        String strLibPath = "";

        int iPackageNameLength = iContext.getPackageName().length();
        int iStartIndex = strPath.indexOf(iContext.getPackageName());

        strLibPath = strPath.substring(0, iStartIndex + iPackageNameLength) + "/";
        final ApplicationInfo applicationInfo = context.getApplicationInfo();
        String strNativePath = strLibPath;
        try {
            if (Build.VERSION.SDK_INT >= 9) {
                Field f = ApplicationInfo.class.getField("nativeLibraryDir");
                strNativePath = (String) f.get(applicationInfo) + "/";
            }
        } catch (Exception e) {

        }

        String ret = strNativePath + engine;
        return ret;
    }
}

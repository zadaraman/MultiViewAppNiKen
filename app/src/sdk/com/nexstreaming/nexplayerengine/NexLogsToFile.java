package com.nexstreaming.nexplayerengine;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NexLogsToFile implements Runnable {
    private static final String LOG_TAG = "NexLogsToFile";
    private static final String DEFAULT_LOGFILE_DIRECTORY = Environment.getExternalStorageDirectory().getPath() + "/NexPlayerSample/Logs/";
    private static final String TXT = ".txt";
    private static final String filenameBase = "%s_%s_log";

    private Process mCleanProcess = null;
    private Process mLogProcess = null;
    private String captureCommand = "logcat -v threadtime";

    public enum NexFileLogPreset {
        DEFAULT(0),
        FOR_PROTOCOL(1),
        FOR_ENGINE(2),
        FOR_AUDIO(3),
        FOR_VIDEO(4),
        FOR_DRM(5),
        FOR_CAPTION(6),
        FULL_LOGS(7);

        private int preset;

        NexFileLogPreset(int preset) {
            this.preset = preset;
        }

        public static NexFileLogPreset fromIntegerValue(int preset) {
            for (int i = 0; i < NexFileLogPreset.values().length; i++) {
                if (NexFileLogPreset.values()[i].preset == preset)
                    return NexFileLogPreset.values()[i];
            }
            return DEFAULT;
        }

        public int getIntegerCode() {
            return preset;
        }
    }

    public static final class Builder {
        final static int DEFAULT_LOG_LEVEL = 0;
        final static int UNSET_LOG_LEVEL = -1;
        final static int DEFAULT_BUFFER_SIZE = 50_000; // 50 mb
        final static int DEFAULT_MAX_FILE_COUNT = 20;

        private NexPlayer player;
        private int jniLogs;
        private int engineLogs;
        private int codecLogs;
        private int renderLogs;
        private int protocolLogs;

        private String filePath;
        private int bufferSize;
        private int fileCount;

        Builder (NexPlayer player) {
            this(player, DEFAULT_LOG_LEVEL, DEFAULT_LOG_LEVEL, UNSET_LOG_LEVEL, UNSET_LOG_LEVEL, UNSET_LOG_LEVEL, DEFAULT_LOGFILE_DIRECTORY, DEFAULT_BUFFER_SIZE, DEFAULT_MAX_FILE_COUNT);
        }

        Builder(NexPlayer player, int jniLogs, int engineLogs, int codecLogs, int renderLogs, int protocolLogs, String filePath, int bufferSize, int fileCount) {
            this.player = player;

            this.jniLogs = jniLogs;
            this.engineLogs = engineLogs;
            this.codecLogs = codecLogs;
            this.renderLogs = renderLogs;
            this.protocolLogs = protocolLogs;

            this.filePath = filePath;
            this.bufferSize = bufferSize;
            this.fileCount = fileCount;
        }

        public Builder setLogLevels(int jniLogs, int engineLogs, int codecLogs, int renderLogs, int protocolLogs) {
            this.jniLogs = jniLogs;
            this.engineLogs = engineLogs;
            this.codecLogs = codecLogs;
            this.renderLogs = renderLogs;
            this.protocolLogs = protocolLogs;
            return this;
        }

        public Builder setFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder seBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder seFileCount(int fileCount) {
            this.fileCount = fileCount;
            return this;
        }

        public Builder setPreset(NexFileLogPreset preset) {
            setLogLevels(1, -1, -1, -1, -1);
            switch (preset) {
                case FOR_PROTOCOL:
                    engineLogs = 2; protocolLogs = 0xF;
                    break;
                case FOR_ENGINE:
                    engineLogs = 3;
                    break;
                case FOR_AUDIO:
                    engineLogs = 3; renderLogs = 5;
                    break;
                case FOR_VIDEO:
                    engineLogs = 3; codecLogs = 5;
                    break;
                case FOR_DRM:
                    engineLogs = 2; codecLogs = 5;
                    break;
                case FOR_CAPTION:
                    engineLogs = 2;
                    break;
                case FULL_LOGS:
                    protocolLogs = 0xF; engineLogs = 4;
                    codecLogs = 5; renderLogs = 5;
                    break;
                default:
                    engineLogs = 0;
            }
            return this;
        }

        public NexLogsToFile build() {
            return new NexLogsToFile(player, jniLogs, engineLogs, codecLogs, renderLogs, protocolLogs, filePath, bufferSize, fileCount);
        }
    }

    NexLogsToFile(NexPlayer player, int jniLogs, int engineLogs, int codecLogs, int renderLogs, int protocolLogs, String directory, int bufferSize, int fileCount) {
        int debugLogs = (1<<28) | ((jniLogs & 0xF) << 24) | ((engineLogs & 0xF) << 20) | ((codecLogs & 0xF) << 16) | ((renderLogs & 0xF) << 12) | ((protocolLogs & 0xF) << 8);
		NexLog.d(LOG_TAG, "NexLogsToFile > debugLogs = " + debugLogs + " bufferSize : " + bufferSize + " fileCount : " + fileCount);
		
        player.setProperties(0x000D0001, debugLogs);

        if (0 < bufferSize)
        {
            captureCommand += " -r " + bufferSize;
        }

        if (0 < fileCount)
        {
            captureCommand += " -n " + fileCount;
        }

        String directoryApllied = null != directory ? directory : DEFAULT_LOGFILE_DIRECTORY;

        File directoryHandle = new File(directoryApllied);
        if (!directoryHandle.exists()) {
            directoryHandle.mkdirs();
        }

        String targetPath = directoryHandle.getAbsolutePath() + "/" + getFileName();
        captureCommand += " -f " + targetPath;
		//Log.d(LOG_TAG, "NexLogsToFile > targetPath = " + targetPath);
    }

    private String getFileName() {
        String pattern = "yyMMddHHmmss";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());

        String fileName = String.format(NexLogsToFile.filenameBase, currentDateAndTime, Build.MODEL);
        fileName = fileName.replace(" ","_");
        fileName = fileName.replace("\n","_");
        fileName += TXT;

        return fileName;
    }

    @Override
    public void run() {
        clean();
        try {
            NexLog.d(LOG_TAG, "run commands to save logs into file : " + captureCommand);
            mLogProcess = Runtime.getRuntime().exec(captureCommand);
        }
        catch (IOException e) {
            NexLog.e(LOG_TAG, "failed capture command");
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        kill();
        super.finalize();
    }

    public void clean() {
        try {
            mCleanProcess = Runtime.getRuntime().exec("logcat -c");
        }
        catch (IOException e) {
            NexLog.e(LOG_TAG, "failed clear command");
            e.printStackTrace();
        }
    }

    public void kill() {
        if (mLogProcess != null) {
            mLogProcess.destroy();
        }

        if (mCleanProcess != null) {
            mCleanProcess.destroy();
        }
    }
}

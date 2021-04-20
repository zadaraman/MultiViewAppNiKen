package com.nexstreaming.nexplayerengine;

/**
 * API for sending log output.
 * Generally, use the NexLog.v() NexLog.d() NexLog.i() NexLog.w() and NexLog.e() methods.
 * <p>
 * \since version 6.0.6
 */

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;


/**
 * \deprecated  For internal use only.  Please do not use.
 */
public class NexLog {

    private static final String TAG = "NexLog";

    /**
     * Socket name to be used for socket logging
     */
    public static String socketNameForLogs;

    /**
     * When this valus is true, logs are sent to the local socket instead of logcat.
     * Make sure socketNameForLogs is set before enabling this property
     */
    public static boolean useSocketForLogs;

    private static OutputStream mOutputstream;


    /**
     * Whether or not a log message should be sent to log output.
     * <p>
     * If this value is \c TRUE, the log message will be sent to log output.
     */
    public static boolean Debug = false;

    /**
     * This method sends a DEBUG log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg The message to be logged.
     */
    public static void d(String tag, String msg) {
        sendLog(tag, msg, Log.DEBUG);
    }

    /**
     * \brief  This method sends an ERROR log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg The message to be logged.
     */
    public static void e(String tag, String msg) {
        sendLog(tag, msg, Log.ERROR);
    }

    /**
     * \brief  This method sends an INFO log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg The message to be logged.
     */
    public static void i(String tag, String msg) {
        sendLog(tag, msg, Log.INFO);
    }

    /**
     * \brief  This message sends a VERBOSE log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg The message to be logged.
     */
    public static void v(String tag, String msg) {
        sendLog(tag, msg, Log.VERBOSE);
    }

    /**
     * \brief  This message sends a WARN log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg The message to be logged.
     */
    public static void w(String tag, String msg) {
        sendLog(tag, msg, Log.WARN);
    }

    private static void sendLog(String tag, String msg, int type) {
        if (!Debug)
            return;

        if (useSocketForLogs) {
            logWithSocket(tag, msg);
            return;
        }

        switch (type) {
            case Log.VERBOSE:
                Log.v(tag, msg);
                break;
            case Log.DEBUG:
                Log.d(tag, msg);
                break;
            case Log.INFO:
                Log.i(tag, msg);
                break;
            case Log.WARN:
                Log.w(tag, msg);
                break;
            case Log.ERROR:
                Log.e(tag, msg);
                break;
        }
    }


    public static void enableLoggingToAppSide(int value) {
        if (value == 0) {
            stopSocketLogging();
        } else {
            startSocketLogging();
        }
    }

    public static int logWithSocket(String tag, String msg) {
        String messageToSent = "[" + tag + "] " + msg + " ";
        try {
            if (messageToSent.length() > 1023) {
                messageToSent = messageToSent.substring(0, 1023);
            }
            mOutputstream.write(messageToSent.getBytes(), 0, messageToSent.length());
        } catch (IOException e) {
            e.printStackTrace();
            useSocketForLogs = false;
            return -1;
        }
        return messageToSent.length();
    }

    private static void stopSocketLogging() {
        if (mOutputstream == null) {
            return;
        }

        try {
            mOutputstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mOutputstream = null;
            useSocketForLogs = false;
        }
    }

    private static void startSocketLogging() {
        if (socketNameForLogs == null) {
            NexLog.e(TAG, "Can not enable socket logging, socket name is not set");
            return;
        }

        if (mOutputstream != null) {
            NexLog.e(TAG, "Socket logging is already enabled");
            return;
        }

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
            NexLog.e(TAG, "Socket logging is not supported in this version of Android");
            return;
        }

        LocalSocket mSendToSocket = new LocalSocket(LocalSocket.SOCKET_DGRAM);
        LocalSocketAddress mLocSockAddr = new LocalSocketAddress(socketNameForLogs, LocalSocketAddress.Namespace.FILESYSTEM);

        try {
            mSendToSocket.connect(mLocSockAddr);
            useSocketForLogs = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mOutputstream = mSendToSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

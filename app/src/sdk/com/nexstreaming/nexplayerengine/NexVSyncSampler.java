package com.nexstreaming.nexplayerengine;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Choreographer;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
final class NexVSyncSampler implements Choreographer.FrameCallback, Handler.Callback {

	private volatile long vsyncTimeNanos = 0;
	private volatile long prevVsyncTimeNanos = 0;
	private volatile int vsyncCountForInterval = 0;
	private volatile long calcVsyncTimeIntervalNanos = 0;
	private volatile long intervalSum = 0;

	private static final long CHOREOGRAPHER_SAMPLE_DELAY_MILLIS = 500;
	private static final double MAX_DIFF_FACTOR = 1.5;
	private static final String LOG_TAG = "NexVSyncSampler";

	private static final int MSG_CREATE  = 0;
	private static final int MSG_START = 1;
	private static final int MSG_STOP = 2;

	private final Handler handler;
	private final HandlerThread choreographerThread;
	private Choreographer choreographer;
	private boolean prepare = false;
	private int observerCount = 0;

	private NexVSyncSampler() {
		choreographerThread = new HandlerThread("ChoreographerOwner:Handler");
		choreographerThread.start();
		handler = new Handler(choreographerThread.getLooper(), this);
		create();
	}

	public void create() {
		NexLog.d(LOG_TAG, "called vsync create");

		vsyncTimeNanos = 0;
		prevVsyncTimeNanos = 0;
		vsyncCountForInterval = 0;
		intervalSum = 0;
		prepare = false;

		handler.sendEmptyMessage(MSG_CREATE);
	}

	public void start() {
		handler.sendEmptyMessage(MSG_START);
	}

	public void stop() {
		handler.sendEmptyMessage(MSG_STOP);
	}

	@Override
	public void doFrame(long vsyncTimeNs) {

		if (prepare) {
			if (0 < prevVsyncTimeNanos && vsyncTimeNs > prevVsyncTimeNanos) {
				long diff = vsyncTimeNs - prevVsyncTimeNanos;
				double MAX_DIFF = diff * MAX_DIFF_FACTOR;
				if (diff < MAX_DIFF) {
					vsyncCountForInterval++;

					intervalSum += diff;
					calcVsyncTimeIntervalNanos = intervalSum / vsyncCountForInterval;
					NexLog.d(LOG_TAG, "calculating cur vsyncTimeNs : " + vsyncTimeNs + " , prev vsyncTimeNanos : " + prevVsyncTimeNanos + " , diff = " + diff + " , interval sum : " + intervalSum + " count : " + vsyncCountForInterval + " , interval : " + calcVsyncTimeIntervalNanos);
				} else {
					NexLog.d(LOG_TAG, "too large vsync interval. ignore : " + diff + " , max diff : " + MAX_DIFF);
				}
			}

			if (30 <= vsyncCountForInterval) {
				prepare = false;
				NexLog.d(LOG_TAG, "+++ vsync count is bigger than 30. stop calculation +++");
			} else {
				prevVsyncTimeNanos = vsyncTimeNs;
				choreographer.postFrameCallback(this);
			}
		} else {
			choreographer.postFrameCallbackDelayed(this, CHOREOGRAPHER_SAMPLE_DELAY_MILLIS);
		}

		vsyncTimeNanos = vsyncTimeNs;
	}

	@Override
	public boolean handleMessage(Message message) {
		switch (message.what) {
			case MSG_CREATE: {
				createInternal();
				return true;
			}
			case MSG_START: {
				startInternal();
				return true;
			}
			case MSG_STOP: {
				stopInternal();
				return true;
			}
			default: {
				return false;
			}
		}
	}

	private void createInternal() {
		choreographer = Choreographer.getInstance();
		prepare = true;
		NexLog.d(LOG_TAG, "+++ start vsync interval calculation +++");
		choreographer.postFrameCallback(this);
	}

	private void startInternal() {
		observerCount++;

		if (observerCount == 1) {
			choreographer.postFrameCallback(this);
		}
	}

	private void stopInternal() {
		observerCount--;

		if (observerCount == 0) {
			choreographer.removeFrameCallback(this);
			vsyncTimeNanos = 0;
		}
	}
}
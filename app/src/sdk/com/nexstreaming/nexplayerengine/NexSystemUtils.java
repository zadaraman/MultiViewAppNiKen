package com.nexstreaming.nexplayerengine;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

class NexSystemUtils implements Runnable {
	private final String TAG = "NexSystemUtils";

	private Double	mCpuUsage;
	private long	mFreeMemory;

	CpuUsage mPreCpuUsage;

	private class CpuUsage {
		private int	mUser;
		private int	mUserNice;
		private int	mSystem;
		private int	mIdle;
		private int	mWait;
		private int	mHintr;
		private int	mSintr;
		
		private CpuUsage() {
			reset();
		}
		
		private void reset() {
			mUser = mUserNice = mSystem = mIdle = mWait = mHintr = mSintr = 0;
		}
	}
	
	protected NexSystemUtils() {
		Thread t = Thread.currentThread();
		t.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

		mPreCpuUsage = new CpuUsage();
		reset();
	}
	
	protected void reset() {
		mCpuUsage = 0.0;
		mFreeMemory = 0;
		mPreCpuUsage.reset(); 
	}
	
	protected Double getCPUUsage() {
		return mCpuUsage;
	}
	
	protected long getFreeMemory() {
		return mFreeMemory;
	}
	
	private void calculateFreeMemory(Runtime runtime) {
		String res = new String();		

		try {
			String cmd = "cat /proc/meminfo";
			Process process = runtime.exec(cmd);;
			InputStream is = process.getInputStream();
			BufferedReader br = new BufferedReader( new InputStreamReader(is) );
			
			int memFree = 0;
			int memCached = 0;
			int memSwapCached = 0; 
			
			if( br != null ) {
				while( ( res = br.readLine() ) != null ) {
					if( res.contains("MemFree:") ) {
						res = res.replaceAll("MemFree:", "").replaceAll("kB", "").trim();
						String[] myString = res.split(" ");
						if( myString != null ) {
							memFree += Integer.parseInt( myString[0] );
						}
					}
					else if( res.contains("SwapCached:") ) {
						res = res.replaceAll("SwapCached:", "").replaceAll("kB", "").trim();
						String[] myString = res.split(" ");

						if( myString != null ) {
							memSwapCached += Integer.parseInt( myString[0] );
						}
					}
					else if( res.contains("Cached:") ) {
						res = res.replaceAll("Cached:", "").replaceAll("kB", "").trim();
						String[] myString = res.split(" ");
						if( myString != null ) {
							memCached += Integer.parseInt( myString[0] );
						}
					}
				}
				if( memSwapCached == 0 ) {
					mFreeMemory = memFree + memCached;
				}
				else {
					mFreeMemory = memFree;
				}
			}
		}
		catch (Exception e){
			NexLog.e(TAG, "Unable to excute Proc command : " + e.getCause() + ", : " + e.getStackTrace() );
		}
	}
	
	private void calculateCpuUsage(Runtime runtime) {
		String res = new String();
		int coreNum = Runtime.getRuntime().availableProcessors();
		
		try {
			int coreCnt = 0;
			String cmd = "cat /proc/stat";
			Process process = runtime.exec(cmd);
			InputStream is = process.getInputStream();
			BufferedReader br = new BufferedReader( new InputStreamReader(is) );
			CpuUsage usageTotal = new CpuUsage();
			
			if( br != null ) {
				while( ( res = br.readLine() ) != null ) {
					for(int i=0; i<coreNum; i++) {
						String resString = "cpu" + i + " ";

						if( res.contains(resString) ) {
							res = res.replaceAll(resString, "cpu ").trim();
						}
					}
					if( res.contains( "cpu " ) ) {
						res = res.replaceAll("cpu ", "").trim();
						String[] myString = res.split(" ");
						usageTotal.mUser 		+= Integer.parseInt( myString[0] );
						usageTotal.mUserNice	+= Integer.parseInt( myString[1] );
						usageTotal.mSystem		+= Integer.parseInt( myString[2] );
						usageTotal.mIdle		+= Integer.parseInt( myString[3] );
						usageTotal.mWait		+= Integer.parseInt( myString[4] );
						usageTotal.mHintr		+= Integer.parseInt( myString[5] );
						usageTotal.mSintr		+= Integer.parseInt( myString[6] );
						coreCnt++;
					}
				}
				usageTotal.mUser		=  usageTotal.mUser / coreCnt;
				usageTotal.mUserNice	=  usageTotal.mUserNice / coreCnt;
				usageTotal.mSystem		=  usageTotal.mSystem / coreCnt;
				usageTotal.mIdle		=  usageTotal.mIdle / coreCnt;
				usageTotal.mWait		=  usageTotal.mWait / coreCnt;
				usageTotal.mHintr		=  usageTotal.mHintr / coreCnt;
				usageTotal.mSintr		=  usageTotal.mSintr / coreCnt;

				if( mPreCpuUsage.mUser != 0 ) {
					mCpuUsage = 1.0 - (double) (usageTotal.mIdle - mPreCpuUsage.mIdle ) / 
							( (usageTotal.mIdle - mPreCpuUsage.mIdle) +
							(usageTotal.mUser - mPreCpuUsage.mUser) +
							(usageTotal.mSystem - mPreCpuUsage.mSystem) +
							(usageTotal.mUserNice - mPreCpuUsage.mUserNice) +
							(usageTotal.mWait - mPreCpuUsage.mWait) +
							(usageTotal.mHintr - mPreCpuUsage.mHintr) +
							(usageTotal.mSintr - mPreCpuUsage.mSintr) );
				}
				mPreCpuUsage = usageTotal;
			}
		}
		catch (Exception e){
			e.fillInStackTrace();
		}
	}
	
	@Override
	public void run() {
		Runtime runtime = Runtime.getRuntime();
		calculateCpuUsage( runtime );
		calculateFreeMemory( runtime );
	}
}

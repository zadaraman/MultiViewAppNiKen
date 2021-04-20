package com.nexstreaming.nexplayerengine;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

class NexNetworkUtils {
	private static final String LOG_TAG = "NexNetworkUtils";
	private static final int BUFFER_SIZE = 1024;

	private String mDownloadPath = null;
	private String mDownloadedFile = null;
	private DownloadService mService = null;

	private STATE mState = STATE.NONE;
	private NetworkListener mListener = null;
	protected enum STATE {
		NONE, DOWNLOADING, DOWNLOADED
	}

	protected interface NetworkListener {
		void onDownloadComplete(String path, int result);
	}

	protected NexNetworkUtils(Context context, NetworkListener listener) {
		mDownloadPath = context.getFilesDir().getAbsolutePath() + File.separator;
		mListener = listener;
	}

	protected void setDownloadPath(String path) {
		if( !path.endsWith(File.separator) )
			path += File.separator;
		mDownloadPath = path;
	}

	protected String getDownloadPath() {
		return mDownloadPath;
	}

	protected static boolean isHttpURL(String path) {
		boolean ret = false;
		if( path != null )
			ret = path.toLowerCase().startsWith("http://") || path.toLowerCase().startsWith("https://");
		return ret;
	}

	class DownloadService implements Runnable {
		private final ExecutorService pool;
		private String downloadURL = null;
		private Future<DownloadData> mFuture = null;

		public DownloadService(String downloadURL)
				throws IOException {
			pool = Executors.newSingleThreadExecutor();
			this.downloadURL = downloadURL;
		}

		public void run() { // run the service
			mFuture = pool.submit(new Handler(downloadURL));
		}

		public void cancel() {
			pool.shutdown();

			try {
				// Wait a while for existing tasks to terminate
				if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
					pool.shutdownNow(); // Cancel currently executing tasks
					// Wait a while for tasks to respond to being cancelled
					if (!pool.awaitTermination(60, TimeUnit.SECONDS))
						System.err.println("Pool did not terminate");
				}
			} catch (InterruptedException ie) {
				// (Re-)Cancel if current thread also interrupted
				pool.shutdownNow();
				// Preserve interrupt status
				Thread.currentThread().interrupt();
			}
		}

		public DownloadData get() {
			DownloadData ret = null;

			if( mFuture != null ) {
				try {
					ret = mFuture.get(30, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}
			}

			return ret;
		}

		public boolean isCancelled() {
			boolean ret = false;
			if( mFuture != null )
				ret = mFuture.isCancelled();
			return ret;
		}
	}

	private class DownloadData {
		String path = null;
		int errorCode = 0;

		DownloadData(String path, int errorCode) {
			this.path = path;
			this.errorCode = errorCode;
		}
	}

	class Handler implements Callable<DownloadData> {
		private String downloadURL;
		private NexPlayer.NexErrorCode errorCode = NexPlayer.NexErrorCode.NONE;

		Handler(String downloadURL) { this.downloadURL = downloadURL; }

		public DownloadData call() {
			URL url = null;
			String saveFilePath = null;
			try {
				url = new URL(downloadURL);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

			try {
				if( url != null ) {
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					int responseCode = connection.getResponseCode();
					if ( responseCode == HttpURLConnection.HTTP_OK ) {
						String fileName = "";
						String disposition = connection.getHeaderField("Content-Disposition");

						if ( disposition != null ) {
							// extracts file name from header field
							int index = disposition.indexOf("filename=");
							if ( index > 0 ) {
								fileName = disposition.substring(index + 10,
										disposition.length() - 1);
							}
						} else {
							// extracts file name from URL
							String path = url.getPath();
							fileName = path.substring(path.lastIndexOf(File.separator) + 1, path.length());
						}

						// opens input stream from the HTTP connection
						InputStream inputStream = null;
						try {
							inputStream = connection.getInputStream();
						} catch (IOException e) {
							e.printStackTrace();
						}

						File file = new File(mDownloadPath);
						if( !file.exists() )
							file.mkdirs();
						saveFilePath = mDownloadPath + fileName;
						NexLog.d(LOG_TAG, "run saveFilePath : " + saveFilePath);

						// opens an output stream to save into file
						FileOutputStream outputStream = null;
						try {
							outputStream = new FileOutputStream(saveFilePath);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}

						int bytesRead;
						byte[] buffer = new byte[BUFFER_SIZE];
						if( inputStream != null && outputStream != null ) {
							while ((bytesRead = inputStream.read(buffer)) != -1) {
								outputStream.write(buffer, 0, bytesRead);
							}

							outputStream.close();
							inputStream.close();
						} else {
							errorCode = NexPlayer.NexErrorCode.UNKNOWN;
						}
					} else {
						errorCode = NexPlayer.NexErrorCode.ERROR_NETWORK_PROTOCOL;
						NexLog.d(LOG_TAG, "No file to download. Server replied HTTP code: " + responseCode);
					}
				} else {
					errorCode = NexPlayer.NexErrorCode.UNKNOWN;
				}
			} catch (IOException e) {
				errorCode = NexPlayer.NexErrorCode.UNKNOWN;
				e.printStackTrace();
			}

			return new DownloadData(saveFilePath, errorCode.getIntegerCode());
		}
	}

	protected void startDownload(String path) {
		NexLog.d(LOG_TAG, "startDownload path : " + path + " mState : " + mState);
		if( mState != STATE.DOWNLOADING ) {
			try {
				mService = new DownloadService(path);
				mService.run();

				mState = STATE.DOWNLOADING;

				NexLog.d(LOG_TAG, "mService.get()111");
				DownloadData data = mService.get();
				NexLog.d(LOG_TAG, "mService.get()222  data.path : " + data.path + " data.error : " + data.errorCode);
				if( data.errorCode == 0 ) {
					mDownloadedFile = data.path;
					mState = STATE.DOWNLOADED;
				} else {
					mState = STATE.NONE;
				}

				mListener.onDownloadComplete(mDownloadedFile, data.errorCode);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected String getDownloadedFile() {
		return mDownloadedFile;
	}

	protected STATE getState() {
		return mState;
	}

	protected void deleteDownloadedFile() {
		if( mDownloadedFile != null ) {
			deleteFile(new File(mDownloadedFile));
			mDownloadedFile = null;
		}
		mState = STATE.NONE;
	}

	private void deleteFile(File file) {
		if( file.exists() )
			file.delete();
	}

	protected void deleteAllDownloadedFile() {
		NexLog.d(LOG_TAG, "deleteAllDownloadedFile mDownloadPath : " + mDownloadPath);
		File file = new File(mDownloadPath);
		if( file.exists() ) {
			File[] fileArray = file.listFiles();
			for( File childFile : fileArray ) {
				NexLog.d(LOG_TAG, "deleteAllDownloadedFile childFile : " + childFile.getAbsolutePath());
				deleteFile(childFile);
			}
		}
		mState = STATE.NONE;
	}

	protected void cancelDownload() {
		NexLog.d(LOG_TAG, "cancelDownload");
		if( mService != null && ( mService.get() == null || !mService.isCancelled()) ) {
			mService.cancel();
			mState = STATE.NONE;
			mListener.onDownloadComplete(null, NexPlayer.NexErrorCode.UNKNOWN.getIntegerCode());
			deleteDownloadedFile();
		}
	}

	protected static boolean isIPv4Address(final String input) {
		return Pattern.compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$").matcher(input).matches();
	}

	protected static boolean isIPv6StdAddress(final String input) {
		return Pattern.compile( "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$" ).matcher(input).matches();
	}
}

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nexstreaming.nexplayerengine;

import android.annotation.TargetApi;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Utility methods for the demo application.
 */
@TargetApi(9)
public class NexHTTPUtil {

  public static final int TYPE_DASH = 0;
  public static final int TYPE_SS = 1;
  public static final int TYPE_HLS = 2;
  public static final int TYPE_MP4 = 3;
  public static final int TYPE_MP3 = 4;
  public static final int TYPE_M4A = 5;
  public static final int TYPE_WEBM = 6;
  public static final int TYPE_TS = 7;
  public static final int TYPE_AAC = 8;

  public static int RequestTimeoutMs = 30000; //30sec
  private static final CookieManager defaultCookieManager;

  private static final String TAG = "NexHTTPUtil";


  static {
    defaultCookieManager = new CookieManager();
    defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
  }

  public static byte[] executePost(String url, byte[] data, Map<String, String> requestProperties)
      throws IOException {
    HttpURLConnection urlConnection = null;
    try {
      NexLog.d(TAG, "Create URL Connection.");
      Uri uri = Uri.parse(url);
      urlConnection = (HttpURLConnection) new URL(uri.toString()).openConnection();
      urlConnection.setRequestMethod("POST");
      urlConnection.setDoOutput(data != null);
      urlConnection.setDoInput(true);

      urlConnection.setConnectTimeout(RequestTimeoutMs);
      urlConnection.setReadTimeout(RequestTimeoutMs);

      if (requestProperties != null) {
        for (Map.Entry<String, String> requestProperty : requestProperties.entrySet()) {
          String strKey =  requestProperty.getKey();
          String strValue = requestProperty.getValue();
          NexLog.d(TAG, "==> SetRequestProperty. Key(" + strKey + ")  Value(" + strValue + ")");
          urlConnection.setRequestProperty(strKey, strValue);
        }
      }

      if (data != null) {
        NexLog.e(TAG, "create BufferedOutputStream.  data:" + data.length);//.toString());
        OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
        NexLog.d(TAG, "create BufferedOutputStream. Done");
        try {
          out.write(data);
        } finally {
          NexLog.e(TAG, "write output buffer..");
          out.close();
        }
      }
      NexLog.d(TAG, "response code : " + urlConnection.getResponseCode());
      NexLog.d(TAG, "response header : " + urlConnection.getHeaderFields().toString());
      NexLog.d(TAG, "create BufferedInputStream.");
      //InputStream in = new BufferedInputStream(urlConnection.getInputStream());
      BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream());

      NexLog.d(TAG, "create BufferedInputStream. Done . Len :" + in.available());
        try {
          return convertInputStreamToByteArray((InputStream)in);
        } finally {
          in.close();
        }
    } catch (java.net.SocketTimeoutException e) {
      NexLog.e(TAG, "timeout. " + e.toString());
      return null;
    }
    finally {
      if (urlConnection != null) {
        urlConnection.disconnect();
      }
    }
  }

  private static byte[] convertInputStreamToByteArray(InputStream inputStream) throws IOException {
    byte[] bytes = null;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte data[] = new byte[1024];
    int count;
    while ((count = inputStream.read(data)) != -1) {
      bos.write(data, 0, count);
    }
    NexLog.d(TAG, "convertInputStreamToByteArray. write:" + bos.size());
    bos.flush();
    bos.close();
    inputStream.close();
    bytes = bos.toByteArray();

    NexLog.d(TAG, "convertInputStreamToByteArray. length:" + bytes.length);
    return bytes;
  }

  public static void setDefaultCookieManager() {
    CookieHandler currentHandler = CookieHandler.getDefault();
    if (currentHandler != defaultCookieManager) {
      CookieHandler.setDefault(defaultCookieManager);
    }
  }

}

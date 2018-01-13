package com.synature.mpos;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

public class DownloadService extends Service{

	public static final String TAG = DownloadService.class.getSimpleName();
	
	public static final int ERROR = -1;
	public static final int UPDATE_PROGRESS = 1;
	public static final int DOWNLOAD_COMPLETE = 2;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra("receiver");
		final String fileUrl = Utils.checkProtocal(intent.getStringExtra("fileUrl"));
		new Thread(new Runnable(){

			@Override
			public void run() {
				String fileName = getFileNameFromUrl(fileUrl);
				InputStream input = null;
				RandomAccessFile out = null;
				HttpURLConnection httpClient = null;
				try {
					File sdPath = Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
					File apk = new File(sdPath + File.separator + fileName);
					URL url = new URL(fileUrl);
					httpClient = (HttpURLConnection) url.openConnection();
					long totalFileSize = httpClient.getContentLength();
					long prevFileSize = 0;
					long downloaded = 0;
					if (apk.exists()) {
						prevFileSize = apk.length();
						if(totalFileSize > prevFileSize){
							httpClient.setRequestProperty("Range", "bytes="+ apk.length() + "-");
							
							httpClient.disconnect();
							httpClient = (HttpURLConnection) url.openConnection();
						}else if(totalFileSize == prevFileSize){
							Bundle resultData = new Bundle();
							resultData.putString("fileName", fileName);
							receiver.send(DOWNLOAD_COMPLETE, resultData);
							return;	
						}else{
							apk.delete();
						}
					}
					input = new BufferedInputStream(httpClient.getInputStream());
					byte buffer[] = new byte[1024];
					int count = 0;
					out = new RandomAccessFile(apk, "rwd");
					out.seek(out.length());
					while ((count = input.read(buffer, 0, 1024)) != -1) {
						out.write(buffer, 0, count);
						downloaded += count;
						Log.i(TAG, "downloaded: " + downloaded);
						if(totalFileSize > 0){
							int progress = (int) ((downloaded + prevFileSize) * 100 / totalFileSize); 
							Log.i(TAG, "Progress: " + progress + "%");
							Bundle resultData = new Bundle();
							resultData.putString("fileName", fileName);
							resultData.putInt("progress", progress);
							receiver.send(UPDATE_PROGRESS, resultData);
						}
					}
					Bundle resultData = new Bundle();
					resultData.putInt("progress", 100);
					resultData.putString("fileName", fileName);
					receiver.send(UPDATE_PROGRESS, resultData);

					resultData = new Bundle();
					resultData.putString("fileName", fileName);
					receiver.send(DOWNLOAD_COMPLETE, resultData);
				} catch (MalformedURLException e) {
					Bundle resultData = new Bundle();
					resultData.putString("msg", e.getMessage());
					receiver.send(ERROR, resultData);
				} catch (IOException e) {
					Bundle resultData = new Bundle();
					resultData.putString("msg", e.getMessage());
					receiver.send(ERROR, resultData);
				} finally {
					if (input != null) {
						try {
							input.close();
						} catch (IOException e) {
						}
					}
					if(out != null){
						try {
							out.close();
						} catch (IOException e) {
						}
					}
					if(httpClient != null){
						httpClient.disconnect();
						httpClient = null;
					}
					stopSelf();
				}
			}
			
		}).start();
		return START_NOT_STICKY;
	}

	private String getFileNameFromUrl(String url){
		String fileName = null;
		String[] segment = url.split("/");
		fileName = segment[segment.length - 1];
		return fileName;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}

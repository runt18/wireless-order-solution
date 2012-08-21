package com.wireless.lib.task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;

import com.wireless.protocol.Food;

public abstract class PicDownloadTask extends AsyncTask<Food, PicDownloadTask.Progress, PicDownloadTask.Progress[]>{
	
	public static class Progress{
		public final static int IN_QUEUE = 0;			//队列中，等待下载
		public final static int IN_PROGRESS = 1;		//下载中
		public final static int DOWNLOAD_SUCCESS = 2;	//下载完成
		public final static int DOWNLOAD_FAIL = 3;		//下载失败
		
		public Food food;
		public int progress;
		public int status;
		
		Progress(Food food, int status){
			this.food = food;
			this.status = status;
		}
	}

	private String mUrl;
	
	private Progress[] mResults;

	public PicDownloadTask(String url){
		mUrl = url;
	}
	
	@Override
	protected Progress[] doInBackground(Food... foods) {
		mResults = new Progress[foods.length];
		for(int i = 0; i < mResults.length; i++){
			mResults[i] = new Progress(foods[i], Progress.IN_QUEUE);
		}
		
		ByteArrayOutputStream picOutputStream = new ByteArrayOutputStream();
		
		for(Progress prog : mResults){
			
			//set the status to "IN_PROGRESS"
			prog.status = Progress.IN_PROGRESS;
			//notify to update the progress
			publishProgress(prog);
			
			HttpURLConnection conn = null;
			try{
				// open the http URL and create the input stream
				conn = (HttpURLConnection) new URL(mUrl + prog.food.image).openConnection();
				InputStream is = conn.getInputStream();
				// get the size to this image file
				int fileSize = conn.getContentLength();
				// create an array stream to store the image file
				picOutputStream = new ByteArrayOutputStream(fileSize);
				
				final int BUF_SIZE = 100 * 1024;
				byte[] buf = new byte[BUF_SIZE];
				int bytesToRead = 0;
				int recvSize = 0;
				while ((bytesToRead = is.read(buf, 0, BUF_SIZE)) != -1) {
					//write the image to picture output stream
					picOutputStream.write(buf, 0, bytesToRead);
					recvSize += bytesToRead;
					//set the download progress
					prog.progress = recvSize * 100 / fileSize;
					//notify to update the progress
					publishProgress(prog);
				}
				
				onProgressFinish(prog.food, picOutputStream);
				picOutputStream.close();
				picOutputStream = null;
				
				//set the status to "DOWNLOAD_SUCCESS"
				prog.status = Progress.DOWNLOAD_SUCCESS;
				//notify to update the progress
				publishProgress(prog);
				
			}catch(IOException e){
				//set the status to "DOWNLOAD_FAIL"
				prog.status = Progress.DOWNLOAD_FAIL;
				try{
					picOutputStream.close();
				}catch(IOException ex){}
				//notify to update the progress
				publishProgress(prog);
				
			}finally{
				if(conn != null){
					conn.disconnect();
				}
			}
		}
		
		return mResults;
	}
	
	protected abstract void onProgressFinish(Food food, ByteArrayOutputStream picOutputStream);
	
	@Override
    protected void onProgressUpdate(Progress... progress) {
		//TODO
    }
	
	@Override
	protected void onPostExecute(Progress[] result){
		//TODO
	}
	
}



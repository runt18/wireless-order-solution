package com.wireless.lib.task;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqOTAUpdate;
import com.wireless.pack.resp.RespOTAUpdate;
import com.wireless.pack.resp.RespOTAUpdate.OTA;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.sccon.ServerConnector;

public abstract class PicDownloadTask extends AsyncTask<Void, PicDownloadTask.Progress, PicDownloadTask.Progress[]>{
	
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

	private Progress[] mResults;

	private final List<Food> mFoodQueue;
	
	private final PinGen mPinGen;
	
	public PicDownloadTask(PinGen gen, List<Food> foodQueue){
		mFoodQueue = foodQueue;
		mPinGen = gen;
	}
	
	private String getPicRoot(PinGen gen){
		
		String rootUrl = null;
		HttpURLConnection conn = null;
		
	    try {
		   
		   //从服务器取得OTA的配置（IP地址和端口）
		   ProtocolPackage resp = ServerConnector.instance().ask(new ReqOTAUpdate(gen));
		   if(resp.header.type == Type.NAK){
			   throw new IOException("无法获取更新服务器信息，请检查网络设置");
		   }
		   //parse the ip address from the response
		   OTA ota = RespOTAUpdate.parse(resp.body);
		   
		   
//		   String otaIP = "10.0.2.2";
//		   int otaPort = 8080;
		   
		   conn = (HttpURLConnection)new URL("http://" + ota.getAddr() + ":" + ota.getPort() + "/web-term/QueryOTA.do?" + 
				   							 "funCode=2" + "&" + 
				   							 "pin=" + gen.getDeviceId() + "&" +
				   							 "model=" + gen.getDeviceType()).openConnection();

		   BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		   StringBuffer response = new StringBuffer();
		   String inputLine;
		   while((inputLine = reader.readLine()) != null){
			   response.append(inputLine);
		   }
		   reader.close();
		   
		   /**
		    * There are two parts within the OTA response.
		    * <result></br><pic_root>
		    */
		   String[] result = response.toString().split("</br>");
		   if(result.length >= 2){
			   if(Boolean.parseBoolean(result[0])){
				   rootUrl = result[1];
			   }
		   }
		   
	    }catch(IOException e){
	    	Log.e("PicDownloadTask", e.getMessage());	    	
	    	
	    }finally{
		   if(conn != null){
			   conn.disconnect();
		   }
	    }
	    
	    return rootUrl;
	}
	
	@Override
	protected Progress[] doInBackground(Void... args) {
		mResults = new Progress[mFoodQueue.size()];
		for(int i = 0; i < mResults.length; i++){
			mResults[i] = new Progress(mFoodQueue.get(i), Progress.IN_QUEUE);
		}
		
		ByteArrayOutputStream picOutputStream = null;
		
		String rootUrl = getPicRoot(mPinGen);
		
		for(Progress prog : mResults){
			
			//set the status to "IN_PROGRESS"
			prog.status = Progress.IN_PROGRESS;
			//notify to update the progress
			publishProgress(prog);
			
			HttpURLConnection conn = null;
			try{
				// open the http URL and create the input stream
				if(rootUrl != null){
					conn = (HttpURLConnection) new URL(rootUrl + prog.food.getImage()).openConnection();
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
					
				}else{
					throw new IOException();
				}
				
			}catch(IOException e){
				//set the status to "DOWNLOAD_FAIL"
				prog.status = Progress.DOWNLOAD_FAIL;
				if(picOutputStream != null){
					try{
						picOutputStream.close();
					}catch(IOException ex){
						
					}
				}
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
	
}



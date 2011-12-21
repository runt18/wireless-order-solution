package com.wireless.ui;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.Toast;

public class VersionService extends Service {
   ProgressDialog _pd;
   float _appVersion;
   String _uri;
   int _downLoadFileSize;
   int _fileSize;
   ProgressDialog pd;
   String _fileName;

  
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		/*
		 * 获取本地的版本号
		 */
		try {
			 PackageManager manager = VersionService.this.getPackageManager();
			 PackageInfo info = manager.getPackageInfo(VersionService.this.getPackageName(), 0);
			 _appVersion = new Float(info.versionName); // 版本名1.0
		} catch (Exception e) {
			 
		}
		
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
	   String uri = getString(R.string.versionurl); 
	   try {
		    HttpURLConnection conn = (HttpURLConnection)new URL(uri).openConnection();
			conn.setConnectTimeout(6* 1000);
			conn.setReadTimeout(20*1000);
			conn.setDoOutput(true);//允许输出
			conn.setDoInput(true);
			conn.setUseCaches(false);//不使用Cache
			conn.setRequestMethod("POST");	        
			conn.setRequestProperty("Connection", "Keep-Alive");//维持长连接
			conn.setRequestProperty("Charset", "UTF-8");
			DataInputStream is=new DataInputStream(conn.getInputStream());
			BufferedReader reader=new BufferedReader(new InputStreamReader(is,"utf-8"));
			String str="";
			while(reader.ready()){
				str+=reader.readLine();
			}
			
			String [] message = str.split("</br>");
			_uri = message[2];
			float version = Float.parseFloat(message[0].toString().trim());
			
			if( version > _appVersion ){
				new AlertDialog.Builder(this)
				.setTitle("提示")
				.setMessage(message[1])
				.setNeutralButton("确定",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,	int which){
								new ApkDownload().execute();
							}
						})
				.setNegativeButton("取消", null)
				.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
						return true;
						
					}
				}).show();

			}else{
				
			}
			
	} catch (Exception e) {
		//handler.sendEmptyMessage(2);
	}
	 
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
	
		super.onDestroy();
	}
	
	public class ApkDownload extends AsyncTask<Void, Void, String>{
		String path = "";
		File file = null;
		HttpURLConnection conn;
		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(VersionService.this);
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置风格为长进度条
			pd.setTitle("提示");//设置标题  
			pd.setMessage("正在下载中，请稍后");
			pd.setIndeterminate(false);//设置进度条是否为不明确  false 就是不设置为不明确  
			pd.setCancelable(true);//设置进度条是否可以按退回键取消
			pd.setProgress(0);
			pd.setMax(100);
			pd.incrementProgressBy(1); //增加和减少进度，这个属性必须的
			pd.show(); 
			super.onPreExecute();
		}
        
		@Override
		protected String doInBackground(Void... params) {
			_fileName = _uri.substring(_uri.lastIndexOf("/"), _uri.length());
			file=new File("/sdcard/"+_fileName);
			try {
				if(file.exists()){
					file.createNewFile();
				}
				conn=(HttpURLConnection)new URL(_uri).openConnection();
				conn.setConnectTimeout(6* 1000);
				conn.setReadTimeout(20*1000);
				conn.setDoOutput(true);//允许输出
				conn.setDoInput(true);
				conn.setUseCaches(false);//不使用Cache
				conn.setRequestMethod("POST");	        
				conn.setRequestProperty("Connection", "Keep-Alive");//维持长连接
				conn.setRequestProperty("Charset", "UTF-8");
				conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
				
				InputStream is = conn.getInputStream();
				_fileSize=conn.getContentLength();

				FileOutputStream fos = new FileOutputStream(file);
				_downLoadFileSize = 0;

				byte[] buff = new byte[1024*100];
				int rc = 0;
				while ((rc = is.read(buff, 0, 100)) > 0) {
					fos.write(buff,0,rc);
					_downLoadFileSize += rc;
					handler.sendEmptyMessage(0);
				}
			} catch (Exception e) {
				handler.sendEmptyMessage(3);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			handler.sendEmptyMessage(1);
			super.onPostExecute(result);
		}

		
		
	}
	
	private Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch (msg.what) {
			case 0:
				int result=_downLoadFileSize*100/_fileSize;  
			    pd.setProgress(result);
				break;

			case 1:
				pd.dismiss();
				File file = new File("/sdcard/"+_fileName);
				Intent intent = new Intent(Intent.ACTION_VIEW);  // 得到Intent对象，其Action为ACTION_VIEW.
				intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive"); // 同时Intent对象设置数据类型
				startActivity(intent);  
				break;
				

			case 2:
				Toast.makeText(VersionService.this, "网络连接超时，请重试", 0).show();
				break;
			case 3:
				pd.dismiss();
				Toast.makeText(VersionService.this, "网络连接超时，请重试", 0).show();
				break;
			}
			
		 }
		};

}

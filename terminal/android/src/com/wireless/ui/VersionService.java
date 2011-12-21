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
		 * ��ȡ���صİ汾��
		 */
		try {
			 PackageManager manager = VersionService.this.getPackageManager();
			 PackageInfo info = manager.getPackageInfo(VersionService.this.getPackageName(), 0);
			 _appVersion = new Float(info.versionName); // �汾��1.0
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
			conn.setDoOutput(true);//�������
			conn.setDoInput(true);
			conn.setUseCaches(false);//��ʹ��Cache
			conn.setRequestMethod("POST");	        
			conn.setRequestProperty("Connection", "Keep-Alive");//ά�ֳ�����
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
				.setTitle("��ʾ")
				.setMessage(message[1])
				.setNeutralButton("ȷ��",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,	int which){
								new ApkDownload().execute();
							}
						})
				.setNegativeButton("ȡ��", null)
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
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//���÷��Ϊ��������
			pd.setTitle("��ʾ");//���ñ���  
			pd.setMessage("���������У����Ժ�");
			pd.setIndeterminate(false);//���ý������Ƿ�Ϊ����ȷ  false ���ǲ�����Ϊ����ȷ  
			pd.setCancelable(true);//���ý������Ƿ���԰��˻ؼ�ȡ��
			pd.setProgress(0);
			pd.setMax(100);
			pd.incrementProgressBy(1); //���Ӻͼ��ٽ��ȣ�������Ա����
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
				conn.setDoOutput(true);//�������
				conn.setDoInput(true);
				conn.setUseCaches(false);//��ʹ��Cache
				conn.setRequestMethod("POST");	        
				conn.setRequestProperty("Connection", "Keep-Alive");//ά�ֳ�����
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
				Intent intent = new Intent(Intent.ACTION_VIEW);  // �õ�Intent������ActionΪACTION_VIEW.
				intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive"); // ͬʱIntent����������������
				startActivity(intent);  
				break;
				

			case 2:
				Toast.makeText(VersionService.this, "�������ӳ�ʱ��������", 0).show();
				break;
			case 3:
				pd.dismiss();
				Toast.makeText(VersionService.this, "�������ӳ�ʱ��������", 0).show();
				break;
			}
			
		 }
		};

}

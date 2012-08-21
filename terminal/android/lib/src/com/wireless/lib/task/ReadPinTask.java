package com.wireless.lib.task;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.lib.PinReader;

public class ReadPinTask extends AsyncTask<Void, Void, Long>{
	
	protected String mErrMsg;
	
	/**
	 * 从SDCard的指定位置读取Pin的值
	 */
	@Override
	protected Long doInBackground(Void... arg0) {
		
		long pin = 0;
		
		try{
			pin = Long.parseLong(PinReader.read(), 16);
		}catch(FileNotFoundException e){
			mErrMsg = "找不到PIN验证文件，请确认是否已插入验证用的SDCard";
		}catch(IOException e){
			mErrMsg = "读取PIN验证信息失败";
		}catch(NumberFormatException e){
			mErrMsg = "PIN验证信息的格式不正确";
		}
		
		return pin;
		
	}	

}

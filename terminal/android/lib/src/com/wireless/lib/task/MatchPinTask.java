package com.wireless.lib.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.os.AsyncTask;

import com.wireless.exception.DeviceError;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.Mode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.RequestPackage;
import com.wireless.parcel.Parcel;
import com.wireless.sccon.ServerConnector;
import com.wireless.util.DeviceUtil;

public class MatchPinTask extends AsyncTask<Void, Void, Void>{
	
	private final static String FILE_PATH = android.os.Environment.getExternalStorageDirectory().getPath() + "/digi-e/android/pin";

	private final Context mContext;
	
	protected String mErrMsg;
	
	public MatchPinTask(Context context){
		mContext = context;
	}
	
	/**
	 * 从SDCard的指定位置读取Pin的值
	 */
	@Override
	protected Void doInBackground(Void... arg0) {
		
		try{
			File pinFile = new File(FILE_PATH);
			if(pinFile.exists()){
				
				InputStreamReader in = new InputStreamReader(new FileInputStream(new File(FILE_PATH)));
				int val;
				StringBuilder pinValue = new StringBuilder();
				while((val = in.read()) != -1){
					pinValue.append((char)val);
				}
				in.close();
				
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqMatchPin(DeviceUtil.getDeviceId(mContext), pinValue.toString()){});
				if(resp.header.type == Type.ACK){
					pinFile.delete();
				}else{
					ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
					if(errCode.equals(DeviceError.DEVICE_ID_DUPLICATE)){
						throw new IOException("设备ID已存在");
					}
				}
			}			
			
		}catch(FileNotFoundException e){
			mErrMsg = "找不到PIN验证文件，请确认是否已插入验证用的SDCard";
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return null;
	}	

}

class ReqMatchPin extends RequestPackage{

	ReqMatchPin(final String deviceId, final String pin){
		super(null);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.MATCH_PIN;
		Parcel p = new Parcel();
		p.writeString(deviceId);
		p.writeString(pin);
		body = p.marshall();
	}	
}

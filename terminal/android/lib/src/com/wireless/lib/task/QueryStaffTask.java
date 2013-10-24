package com.wireless.lib.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.wireless.exception.DeviceError;
import com.wireless.exception.ErrorCode;
import com.wireless.exception.RestaurantError;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryStaff;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.staffMgr.Device;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;
import com.wireless.util.DeviceUtil;

public class QueryStaffTask extends AsyncTask<Void, Void, List<Staff>>{

	protected String mErrMsg;
	
	private final Context mContext;
	
	public QueryStaffTask(Context context){
		mContext = context;
	}
	
	/**
	 * 在新的线程中执行请求员工信息的操作
	 */
	@Override
	protected List<Staff> doInBackground(Void... args){
		
		List<Staff> staffs = new ArrayList<Staff>();
		try{

			String deviceId = DeviceUtil.getDeviceId(mContext);
			
			if(deviceId != null){
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryStaff(new Device(deviceId)));
				if(resp.header.type == Type.ACK){
					staffs.addAll(new Parcel(resp.body).readParcelList(Staff.CREATOR));
					
				}else{
					ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
					
					if(errCode.equals(DeviceError.DEVICE_NOT_EXIST)) {
						mErrMsg = "终端没有登记到餐厅，请联系管理人员。";
						
					}else if(errCode.equals(RestaurantError.RESTAURANT_EXPIRED)) {
						mErrMsg = "终端已过期，请联系管理人员。";
						
					}else{
						mErrMsg = "更新员工信息失败，请检查网络信号或重新连接。";
					}
					throw new IOException(mErrMsg);
				}
				
			}else{
				throw new IOException("无法获取设备ID");
			}
		}catch(IOException e){
			mErrMsg = e.getMessage();
			
		}catch(SecurityException e){
			mErrMsg = e.getMessage();
		}
		
		return Collections.unmodifiableList(staffs);
	}
}

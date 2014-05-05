package com.wireless.lib.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.DeviceError;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryStaff;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.staffMgr.Device;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;
import com.wireless.util.DeviceUtil;

public abstract class QueryStaffTask extends AsyncTask<Void, Void, List<Staff>>{

	private BusinessException mBusinessException;
	
	private final Context mContext;
	
	private final boolean mTestFlag;
	
	public QueryStaffTask(Context context){
		mContext = context;
		mTestFlag = false;
	}
	
	public QueryStaffTask(Context context, boolean testFlag){
		mContext = context;
		mTestFlag = testFlag;
	}
	
	/**
	 * 在新的线程中执行请求员工信息的操作
	 */
	@Override
	protected List<Staff> doInBackground(Void... args){
		
		List<Staff> staffs = new ArrayList<Staff>();
		try{

			String deviceId = mTestFlag ? "11111111" : DeviceUtil.getDeviceId(mContext);
			
			if(deviceId != null){
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryStaff(new Device(deviceId)));
				if(resp.header.type == Type.ACK){
					staffs.addAll(new Parcel(resp.body).readParcelList(Staff.CREATOR));
					
				}else{
					ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
					
					if(errCode.equals(DeviceError.DEVICE_NOT_EXIST)) {
						mBusinessException = new BusinessException("终端没有登记到餐厅，请联系管理人员");
					}else{
						mBusinessException = new BusinessException(errCode);
					}
				}
				
			}else{
				mBusinessException = new BusinessException("无法获取设备ID");
			}
			
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
			
		}catch(SecurityException e){
			mBusinessException = new BusinessException(e.getMessage());
		}
		
		return Collections.unmodifiableList(staffs);
	}
	
	@Override
	protected final void onPostExecute(List<Staff> staffs){
		if(mBusinessException != null){
			onFail(mBusinessException);
		}else{
			onSuccess(staffs);
		}
	}
	
	protected abstract void onSuccess(List<Staff> staffs);
	
	protected abstract void onFail(BusinessException e);
}

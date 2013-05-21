package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.excep.ProtocolException;
import com.wireless.pack.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqQueryMenu;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.menuMgr.FoodMenu;
import com.wireless.sccon.ServerConnector;

public class QueryMenuTask extends AsyncTask<Void, Void, FoodMenu>{

	protected ProtocolException mProtocolException;
	
	private final PinGen mPinGen;
	
	public QueryMenuTask(PinGen gen){
		mPinGen = gen;
	}
	
	/**
	 * 在新的线程中执行请求菜谱信息的操作
	 */
	@Override
	protected FoodMenu doInBackground(Void... args) {
		
		
		FoodMenu foodMenu = null;
		
		String errMsg;
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryMenu(mPinGen));
			if(resp.header.type == Type.ACK){
				foodMenu = new FoodMenu();
				foodMenu.createFromParcel(new Parcel(resp.body));
				
			}else{
				if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
					errMsg = "终端没有登记到餐厅，请联系管理人员。";
				}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
					errMsg = "终端已过期，请联系管理人员。";
				}else{
					errMsg = "菜谱下载失败，请检查网络信号或重新连接。";
				}
				mProtocolException = new ProtocolException(errMsg);
			}
		}catch(IOException e){
			mProtocolException = new ProtocolException(e.getMessage());
		}
		
		return foodMenu;
	}

}

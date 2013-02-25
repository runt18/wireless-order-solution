package com.wireless.util;

import java.io.IOException;

import com.wireless.pack.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryMenu;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.terminal.WirelessOrder;

public class QueryMenu extends Thread{
	
	private IQueryMenu _queryCallBack = null;
	
	public QueryMenu(IQueryMenu queryMenu){
		if(queryMenu == null){
			throw new IllegalArgumentException("菜谱下载失败，请检查网络信号或重新连接。");
		}
		_queryCallBack = queryMenu;
	}
	
	public void run(){
		
		ProtocolPackage resp = null;
		
		try{
			_queryCallBack.preQueryMenu();
			resp = ServerConnector.instance().ask(new ReqQueryMenu());	
			if(resp.header.type == Type.ACK){
				WirelessOrder.foodMenu = new FoodMenu();
				WirelessOrder.foodMenu.createFromParcel(new Parcel(resp.body));
				_queryCallBack.passMenu(resp);
				
			}else{
				String errMsg = null;
				if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
					errMsg = "终端没有登记到餐厅，请联系管理人员。";
				}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
					errMsg = "终端已过期，请联系管理人员。";
				} else {
					errMsg = "菜谱下载失败，请检查网络信号或重新连接。";
				}
				_queryCallBack.failMenu(resp, errMsg);
			}
			
		}catch(IOException e){
			_queryCallBack.failMenu(resp, e.getMessage());
			
		}catch(Exception e){
			_queryCallBack.failMenu(resp, e.getMessage());
			
		}finally{
			_queryCallBack.postQueryMenu();
		}
	}
}



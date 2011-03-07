package com.wireless.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import com.wireless.protocol.*;
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
				Vector menu = RespParser.parseQueryMenu(resp);
				WirelessOrder.FoodMenu.removeAllElements();
				Enumeration e = menu.elements();
				while(e.hasMoreElements()){
					WirelessOrder.FoodMenu.addElement(e.nextElement());
				}
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



package com.wireless.ui.main;

import java.io.IOException;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqQueryMenu;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Type;
import com.wireless.terminal.WirelessOrder;
import com.wireless.util.IQueryMenu;
import com.wireless.util.ServerConnector;

public class MenuDownloadPopup extends PopupScreen{
	
	private MenuDownloadPopup _self = this; 
	private Exception _excep = null;
	private IQueryMenu _queryMenu = null;
	
	MenuDownloadPopup(IQueryMenu queryMenu){
		super(new VerticalFieldManager());
		_queryMenu = queryMenu;
		add(new LabelField("下载菜谱中...请稍候"));
	}
	 
	protected void onUiEngineAttached(boolean attached){
		if(attached == true){
			new Thread(){
				private ProtocolPackage _resp = null;
				public void run(){
					try{
						_resp = ServerConnector.instance().ask(new ReqQueryMenu());	
						if(_resp.header.type == Type.ACK){
							
							WirelessOrder.foodMenu = null;
							WirelessOrder.foodMenu = RespParser.parseQueryMenu(_resp);

							if(_queryMenu != null){
								UiApplication.getUiApplication().invokeLater(new Runnable(){
									public void run(){
										_queryMenu.passMenu(_resp);
									}
								});
							}
						}else{
							if(_queryMenu != null){
								UiApplication.getUiApplication().invokeLater(new Runnable(){
									public void run(){
										String errMsg;
										if(_resp.header.reserved[0] == ErrorCode.TERMINAL_NOT_ATTACHED){
											errMsg = "终端没有登记到餐厅，请联系管理人员。";				
										}else if(_resp.header.reserved[0] == ErrorCode.TERMINAL_EXPIRED){
											errMsg = "终端已过期，请联系管理人员。";				
										}else{
											errMsg = "菜谱下载失败，请检查网络信号或重新连接。";
										}
										_queryMenu.failMenu(_resp, errMsg);
									}
								});
							}
						}
						
					}catch(IOException e){
						_excep = e;
						UiApplication.getUiApplication().invokeLater(new Runnable(){
							public void run(){
								if(_queryMenu != null){
									_queryMenu.failMenu(_resp, _excep.getMessage());
								}
							}
						});
					}catch(Exception e){
						_excep = e;
						UiApplication.getUiApplication().invokeLater(new Runnable(){
							public void run(){
								if(_queryMenu != null){
									_queryMenu.failMenu(_resp, _excep.getMessage());
								}
							}
						});						
					}finally{
						UiApplication.getUiApplication().invokeLater(new Runnable(){
							public void run(){
								UiApplication.getUiApplication().popScreen(_self);
							}
						});
					}				
				}
			}.start();
		}
	}
}


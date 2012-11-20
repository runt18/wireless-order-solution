package com.wireless.ui.networkset;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.protocol.ReqPing;
import com.wireless.terminal.Params;
import com.wireless.util.ServerConnector;

public class NetworkTestPopup extends PopupScreen{
	private NetworkTestPopup _self = this; 
	
	NetworkTestPopup(String netAddr, String netPort, String netAPN, String netUser, String netPwd){
		super(new VerticalFieldManager());
        ServerConnector.instance().setNetAddr(netAddr);
        ServerConnector.instance().setNetPort(netPort);
        ServerConnector.instance().setNetAPN(netAPN);
        ServerConnector.instance().setNetUser(netUser);
        ServerConnector.instance().setNetPwd(netPwd);
		add(new LabelField("网络连接中...请稍候"));
	}
	
	protected void onUiEngineAttached(boolean attached){
		if(attached == true){
			new Thread(){
				public void run(){
					try{
						ServerConnector.instance().ask(new ReqPing());	
	
						UiApplication.getUiApplication().invokeLater(new Runnable(){
							public void run(){
								Dialog.inform("网络连接成功");
							}
						});

					}catch(Exception e){
						UiApplication.getUiApplication().invokeLater(new Runnable(){
							public void run(){
								Dialog.alert("网络连接失败，请检查网络参数是否正确。");
							}
						});
					}finally{
						UiApplication.getUiApplication().invokeLater(new Runnable(){
							public void run(){
						        ServerConnector.instance().setNetAddr(Params.getParam(Params.NET_ADDR));
						        ServerConnector.instance().setNetPort(Params.getParam(Params.NET_PORT));
						        ServerConnector.instance().setNetAPN(Params.getParam(Params.NET_APN));
						        ServerConnector.instance().setNetUser(Params.getParam(Params.NET_USER));
						        ServerConnector.instance().setNetPwd(Params.getParam(Params.NET_PWD));
								UiApplication.getUiApplication().popScreen(_self);
							}
						});
					}				
				}
			}.start();
		}
	}
}


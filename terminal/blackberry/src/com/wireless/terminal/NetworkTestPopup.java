package com.wireless.terminal;

import com.wireless.protocol.*;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;

public class NetworkTestPopup extends PopupScreen{
	private NetworkTestPopup _self = this; 
	
	NetworkTestPopup(){
		super(new VerticalFieldManager());
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
								UiApplication.getUiApplication().popScreen(_self);
							}
						});
					}				
				}
			}.start();
		}
	}
}


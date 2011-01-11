package com.wireless.terminal;

import java.io.IOException;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import com.wireless.protocol.*;

class CancelOrderPopup2 extends PopupScreen{

	private CancelOrderPopup2 _self = this;
	private short _table = 0;
	private Exception _excep = null;
	
	CancelOrderPopup2(short table){
		super(new VerticalFieldManager());
		_table = table;
		add(new LabelField("提交" + _table + "号台删单信息...请稍候"));
	}
	
	protected void onUiEngineAttached(boolean attached){
		if(attached == true){
			new Thread(){
				public void run(){
					try{
						ProtocolPackage _resp = ServerConnector.instance().ask(new ReqCancelOrder(_table));
						if(_resp.header.type == Type.ACK){
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run(){
									Dialog.alert(_table + "号台删单成功");	
								}
							});
						}else{
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run(){
									Dialog.alert(_table + "号台删单失败");	
								}
							});
						}
					}catch(IOException e){
						_excep = e;
						UiApplication.getUiApplication().invokeLater(new Runnable(){
							public void run(){
								Dialog.alert(_excep.getMessage());
							}
						});
					}catch(Exception e){
						_excep = e;
						UiApplication.getUiApplication().invokeLater(new Runnable(){
							public void run(){
								Dialog.alert(_excep.toString());
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

package com.wireless.terminal;

import java.io.IOException;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import com.wireless.protocol.*;
import com.wireless.util.ServerConnector;

public class InsertOrderPopup extends PopupScreen{
	
	private InsertOrderPopup _self = this;
	private Order _reqOrder = null;
	private Exception _excep = null;
	
	public InsertOrderPopup(Order order){
		super(new VerticalFieldManager());
		_reqOrder = order;
		add(new LabelField("提交" + _reqOrder.tableID + "号台下单信息...请稍候"));
	}
	
	protected void onUiEngineAttached(boolean attached){
		if(attached == true){
			new Thread(){
				public void run(){
					try{
						ProtocolPackage _resp = ServerConnector.instance().ask(new ReqInsertOrder(_reqOrder));
						if(_resp.header.type == Type.ACK){
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run(){
									Dialog.alert(_reqOrder.tableID + "号台下单成功。");									
								}
							});
						}else{
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run(){
									Dialog.alert(_reqOrder.tableID + "号台下单失败，请重新提交下单。");
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

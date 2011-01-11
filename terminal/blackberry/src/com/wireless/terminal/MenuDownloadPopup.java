package com.wireless.terminal;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import com.wireless.protocol.*;

class MenuDownloadPopup extends PopupScreen{
	
	private MenuDownloadPopup _self = this; 
	private Exception _excep = null;
	private PostMenuDownload _postMenuDownload = null;
	
	MenuDownloadPopup(PostMenuDownload postMenuDownload){
		super(new VerticalFieldManager());
		_postMenuDownload = postMenuDownload;
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
							Vector menu = RespParser.parseQueryMenu(_resp);
							WirelessOrder.FoodMenu.removeAllElements();
							Enumeration e = menu.elements();
							while(e.hasMoreElements()){
								WirelessOrder.FoodMenu.addElement(e.nextElement());
							}
							if(_postMenuDownload != null){
								UiApplication.getUiApplication().invokeLater(new Runnable(){
									public void run(){
										_postMenuDownload.menuDownloadPass();
									}
								});
							}
						}else{
							if(_postMenuDownload != null){
								UiApplication.getUiApplication().invokeLater(new Runnable(){
									public void run(){
										if(_resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED){
											Dialog.alert("终端没有登记到餐厅，请联系管理人员。");				
										}else if(_resp.header.reserved == ErrorCode.TERMINAL_EXPIRED){
											Dialog.alert("终端已过期，请联系管理人员。");				
										}else{
											Dialog.alert("菜谱下载失败，请检查网络信号或重新连接。");
										}
										_postMenuDownload.menuDownloadFail();
									}
								});
							}
						}
						
					}catch(IOException e){
						_excep = e;
						UiApplication.getUiApplication().invokeLater(new Runnable(){
							public void run(){
								Dialog.alert(_excep.getMessage());
								if(_postMenuDownload != null){
									_postMenuDownload.menuDownloadFail();
								}
							}
						});
					}catch(Exception e){
						_excep = e;
						UiApplication.getUiApplication().invokeLater(new Runnable(){
							public void run(){
								Dialog.alert(_excep.toString());
								if(_postMenuDownload != null){
									_postMenuDownload.menuDownloadFail();
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

interface PostMenuDownload{
	public void menuDownloadPass();
	public void menuDownloadFail();
}
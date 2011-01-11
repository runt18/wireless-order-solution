package com.wireless.terminal;

import com.wireless.protocol.*;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

class QueryRestaurant extends PopupScreen{
	
	private QueryRestaurant _self = this;
	private PostQueryRestaurant _postQueryRestaurant = null;
	
	QueryRestaurant(PostQueryRestaurant postQueryRestaurant){
		super(new VerticalFieldManager());
		_postQueryRestaurant = postQueryRestaurant;
		add(new LabelField("更新餐厅信息...请稍候"));
	}
	
	protected void onUiEngineAttached(boolean attached){
		if(attached == true){
			new Thread(){
				private ProtocolPackage _resp = null;
				public void run(){
					try{
						_resp = ServerConnector.instance().ask(new ReqQueryRestaurant());	
						if(_resp.header.type == Type.ACK){
							if(_postQueryRestaurant != null){
								UiApplication.getUiApplication().invokeLater(new Runnable(){
									public void run(){
										_postQueryRestaurant.postQueryRestaurant(RespParser.parseQueryRestaurant(_resp));										
									}
								});
							}
						}else{
							if(_postQueryRestaurant != null){
								UiApplication.getUiApplication().invokeLater(new Runnable(){
									public void run(){
										_postQueryRestaurant.postQueryRestaurant(new Restaurant("餐厅名称"));										
									}
								});
							}						
						}
						
					}catch(Exception e){
						if(_postQueryRestaurant != null){
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run(){
									_postQueryRestaurant.postQueryRestaurant(new Restaurant("餐厅名称"));										
								}
							});
						}
						
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

interface PostQueryRestaurant{
	public void postQueryRestaurant(Restaurant restaurant); 
}
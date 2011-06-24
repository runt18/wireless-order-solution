package com.wireless.terminal;

import com.wireless.protocol.*;
import com.wireless.util.IQueryRestaurant;
import com.wireless.util.QueryRestaurant;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

class QueryRestaurantPopup extends PopupScreen implements IQueryRestaurant{
	
	private QueryRestaurantPopup _self = this;
	private ProtocolPackage _resp = null;
	private IQueryRestaurant _queryCallBack = null;
	
	QueryRestaurantPopup(IQueryRestaurant queryCallBack){
		super(new VerticalFieldManager());
		if(queryCallBack == null){
			throw new IllegalArgumentException();
		}
		_queryCallBack = queryCallBack;
		add(new LabelField("更新餐厅信息...请稍候"));
	}
	
	protected void onUiEngineAttached(boolean attached){
		if(attached){
			new QueryRestaurant(this).start();
		}
	}

	public void preQueryRestaurant() {
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run(){
				_queryCallBack.preQueryRestaurant();
			}
		});		
	}

	public void passQueryRestaurant(ProtocolPackage resp) {
		_resp = resp;
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run(){
				_queryCallBack.passQueryRestaurant(_resp);
			}
		});		
	}

	public void failQueryRestuarant(ProtocolPackage resp, String errMsg){
		_resp = resp;
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run(){
				_queryCallBack.passQueryRestaurant(_resp);
			}
		});
	}

	public void postQueryRestaurant() {
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run(){
				UiApplication.getUiApplication().popScreen(_self);
			}
		});		
	}
}

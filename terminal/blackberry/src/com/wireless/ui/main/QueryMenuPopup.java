package com.wireless.ui.main;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.protocol.ProtocolPackage;
import com.wireless.util.IQueryMenu;
import com.wireless.util.QueryMenu;

public class QueryMenuPopup extends PopupScreen
							implements IQueryMenu{
	
	private QueryMenuPopup _self = this;
	private IQueryMenu _queryCallBack = null;
	private ProtocolPackage _resp = null;
	private String _errMsg = null;
	
	public QueryMenuPopup(IQueryMenu queryMenu){
		super(new VerticalFieldManager());
		if(queryMenu == null){
			throw new IllegalArgumentException();
		}
		add(new LabelField("下载菜谱中...请稍候"));
		_queryCallBack = queryMenu;
	}
	
	protected void onUiEngineAttached(boolean attached){
		if(attached){
			new QueryMenu(this).start();
		}
	}

	public void preQueryMenu() {
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run(){
				_queryCallBack.preQueryMenu();
			}
		});
	}

	public void passMenu(ProtocolPackage resp) {
		_resp = resp;
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run(){
				_queryCallBack.passMenu(_resp);
			}
		});
	}

	public void failMenu(ProtocolPackage resp, String errMsg) {
		_resp = resp;
		_errMsg = errMsg;
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run(){
				_queryCallBack.failMenu(_resp, _errMsg);
			}
		});
		
	}

	public void postQueryMenu() {
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run(){
				UiApplication.getUiApplication().popScreen(_self);
				_queryCallBack.postQueryMenu();
			}
		});
	}
	
}

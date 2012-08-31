package com.wireless.ui.main;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.util.IOTAUpdate;
import com.wireless.util.OTAUpdate;

public class OTAUpdatePopup extends PopupScreen implements IOTAUpdate{

	private OTAUpdatePopup _self = this;
	private boolean _isSilent = true;
	private String _errMsg = null;
	private String _url = null;
	private String _ver = null;
	
	/**
	 * The constructor of OTA update pop up screen.
	 * @param isSlient If set silent flag to true, no messages prompt to user except new version update.
	 * 				   Otherwise, rich message would prompt to user, including the network problem, version status and so forth.
	 */
	public OTAUpdatePopup(boolean isSilent){
		super(new VerticalFieldManager());
		add(new LabelField("检查更新中...请稍候"));
		_isSilent = isSilent;
	}
	
	protected void onUiEngineAttached(boolean attached){
		if(attached){
			new OTAUpdate(this).start();
		}
	}

	public void preOTAUpdate() {
		
	}

	public void passOTAUpdate(String latestVer, String url4ota){
		_ver = latestVer;
		_url = url4ota;
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run(){
				if(_ver != null &&_url != null){
					Dialog.alert("新版本v" + _ver + "准备就绪，按“确定”开始升级");
					BrowserSession browserSession = Browser.getDefaultSession();
					// now launch the URL
					browserSession.displayPage(_url);
					browserSession.showBrowser();
					//UiApplication.getUiApplication().pushScreen(new OTAUpdateScreen("cmnet"));					
				}else{
					if(!_isSilent){
						Dialog.alert("目前程序已经是最新版本");
					}					
				}				
			}
		});
	}

	public void failOTAUpdate(String errMsg){
		_errMsg = errMsg;
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run(){
				if(!_isSilent){
					Dialog.alert(_errMsg);
				}
			}
		});
	}

	public void postOTAUpdate() {
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run(){
				UiApplication.getUiApplication().popScreen(_self);
			}
		});
	}
}

package com.wireless.terminal;


import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

import com.wireless.protocol.ProtocolPackage;
import com.wireless.ui.field.BlankSeparatorField;
import com.wireless.ui.field.ThrobberField;
import com.wireless.ui.main.OrderMainScreen;
import com.wireless.util.IOTAUpdate;
import com.wireless.util.IQueryMenu;
import com.wireless.util.IQueryPing;
import com.wireless.util.IQueryRestaurant;
import com.wireless.util.OTAUpdate;
import com.wireless.util.QueryMenu;
import com.wireless.util.QueryPing;
import com.wireless.util.QueryRestaurant;

public class StartupScreen extends MainScreen implements IQueryMenu,
														 IQueryRestaurant,
														 IOTAUpdate,
														 IQueryPing
{
	private boolean _isMenuOK = false;
	private String _errMsg = null;
	private String _ver = null;
	private String _url = null;
	private LabelField _acting = new LabelField("", Field.FIELD_HCENTER);
	private boolean _isStartup = false;
	
	public StartupScreen(boolean isStartup){
		_isStartup = isStartup;
	    int displayWidth = Display.getWidth();
	    int displayHeight = Display.getHeight();
	    int fieldSpacerSize = displayHeight / 24;
	    Bitmap splashLogo = Bitmap.getBitmapResource("splash-logo.png");
	    int throbberSize = displayWidth / 4;
	    int fontHeight = Font.getDefault().getHeight();
	    int spacerSize = (displayHeight / 2) - ((splashLogo.getHeight() + throbberSize + fontHeight) / 2) - fieldSpacerSize;
	    if(spacerSize < 0) { spacerSize = 0; }

	    add(new BlankSeparatorField(spacerSize));
	    add(new BitmapField(splashLogo, Field.FIELD_HCENTER));
	    add(new BlankSeparatorField(fieldSpacerSize));
	    add(new ThrobberField(throbberSize, Field.FIELD_HCENTER));
	    add(new BlankSeparatorField(fieldSpacerSize));	    

	    add(_acting);
	}

	protected void onUiEngineAttached(boolean attached){
		if(attached){		
			//here wait until system startup is done
			ApplicationManager myApp = ApplicationManager.getApplicationManager();
			while(myApp.inStartup()) {
				try {
					Thread.sleep(200);
				} catch (Exception e) {
					// Catch Exception
				}
			}
			
			if(_isStartup){
				new QueryPing(this, 15, 1000).start();
			}else{
				new OTAUpdate(this).start();
			}

		}else{
//			UiApplication.getApplication().invokeLater(new Runnable(){
//				public void run(){
					//UiApplication.getApplication().requestForeground();
					UiApplication.getUiApplication().pushScreen(new OrderMainScreen(_isMenuOK));
					if(_errMsg != null){
						Dialog.alert(_errMsg);
						
					}else if(_ver != null && _url != null){
						Dialog.alert("新版本v" + _ver + "准备就绪，按“确定”开始升级");
						BrowserSession browserSession = Browser.getDefaultSession();
						// now launch the URL
						browserSession.displayPage(_url);
						browserSession.showBrowser();						
					}
//				}
//			});
		}
	}

	/**
	 * Make the return disabled as the startup screen is on display.
	 */
	public boolean onClose(){
		return false;
	}
	
	/**
	 * Show message before performing query menu.
	 */
	public void preQueryMenu() {
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run(){
				_acting.setText("下载菜谱信息...请稍候");	
			}
		});
	}

	/**
	 * Assign the menu flag to OK and
	 * jumb to the main screen  if passing the menu download 
	 */
	public void passMenu(ProtocolPackage resp) {
		_isMenuOK = true;
	}

	/**
	 * Show the dialog to prompt the error message to user if fail to download the food menu.
	 * And then close the startup screen and jump to the main screen 
	 * with menu failure state and default restaurant info.
	 */
	public void failMenu(ProtocolPackage resp, String errMsg) {
		_errMsg = errMsg;
	}

	public void postQueryMenu() {
		// TODO Auto-generated method stub
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run(){
				close();
			}
		});
	}

	/**
	 * Show message before query restaurant.
	 */
	public void preQueryRestaurant() {
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run(){
				_acting.setText("更新餐厅信息...请稍候");			
			}
		});		
	}

	/**
	 * Log the restaurant info and perform to OTA update if passing the restaurant query.
	 */
	public void passQueryRestaurant(ProtocolPackage resp) {
	
	}

	/**
	 * Perform to OTA update even fail to passing the restaurant query.
	 */
	public void failQueryRestuarant(ProtocolPackage resp, String errMsg) {
		
	}

	/**
	 * Continue to perform menu query no matter succeed to query restaurant or not
	 */
	public void postQueryRestaurant() {
		new QueryMenu(this).start();		
	}

	/**
	 * Show message before perform OTA update.
	 */
	public void preOTAUpdate() {
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run(){
				_acting.setText("检查更新中...请稍候");	
			}
		});		
	}

	/**
	 * In the case a newer version is available,
	 * assign the latest version and the OTA url,
	 * and then close the startup screen and jump to the OTA screen.
	 * Otherwise perform to query the restaurant info.
	 */
	public void passOTAUpdate(String latestVer, String url4ota) {
		if(latestVer != null & url4ota != null){
			_ver = latestVer;
			_url = url4ota;
			UiApplication.getUiApplication().invokeLater(new Runnable(){
				public void run(){
					close();
				}
			});			
		}else{
			new QueryRestaurant(this).start();
		}
	}

	/**
	 * Continue to perform to query the restaurant info even if fail to OTA update.
	 */
	public void failOTAUpdate(String errMsg){
		new QueryRestaurant(this).start();
	}

	public void postOTAUpdate() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Show message before the preparing work
	 */
	public void prePing() {
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run(){
				_acting.setText("正在检测网络...请稍候");			
			}
		});
	}

	/**
	 * Perform OTA to check the new version if Ping passes.
	 */
	public void passPing(){
		new OTAUpdate(this).start();	
	}
	
	/**
	 * Jump to main screen and prompt user the error message if Ping failed.
	 */
	public void failPing(){
		_errMsg = "网络连接未成功，请检查网络信号或重新连接";
		UiApplication.getUiApplication().invokeLater(new Runnable(){
			public void run(){
				close();
			}
		});
	}	

	public void postPing() {
		// TODO Auto-generated method stub
	}
}




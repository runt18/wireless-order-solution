package com.wireless.ui.main;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.ApplicationManagerException;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqOTAUpdate;
import com.wireless.protocol.Type;
import com.wireless.terminal.Params;
import com.wireless.terminal.WirelessOrder;
import com.wireless.ui.field.TopBannerField;
import com.wireless.ui.field.VerifyPwd;
import com.wireless.ui.funcset.FuncSettingScreen;
import com.wireless.ui.funcset.IPostFuncSet;
import com.wireless.ui.networkset.IPostNetworkSet;
import com.wireless.ui.networkset.NetworkSettingScreen;
import com.wireless.util.IQueryMenu;
import com.wireless.util.IQueryRestaurant;
import com.wireless.util.ServerConnector;

/**
 * The order screen class to allow for user interaction.
 */
public class OrderMainScreen extends MainScreen	implements IQueryMenu, 
									   					   IQueryRestaurant,
									   					   IPostNetworkSet,
									   					   IPostFuncSet
{
	
	private ButtonField _newOrder = new ButtonField("      下单      ", ButtonField.CONSUME_CLICK);
	private ButtonField _updateOrder = new ButtonField("      改单      ", ButtonField.CONSUME_CLICK);
	private ButtonField _cancelOrder = new ButtonField("      删单      ", ButtonField.CONSUME_CLICK);
	private ButtonField _payOrder = new ButtonField("      结帐      ", ButtonField.CONSUME_CLICK); 	
	LabelField _restaurantName = new LabelField();
	RichTextField _restaurantInfo = new RichTextField(Field.NON_FOCUSABLE);
	private OrderMainScreen _self = this;
	
	// Constructor
	public OrderMainScreen(boolean isMenuOK){		
		ApplicationDescriptor descriptor = ApplicationDescriptor.currentApplicationDescriptor();
		setBanner(new TopBannerField("e点通(v" + descriptor.getVersion() + ")"));
		//setTitle("e点通(v" + descriptor.getVersion() + ")");
		if(WirelessOrder.restaurant.owner.length() != 0){
			_restaurantName.setText(WirelessOrder.restaurant.name + "(" + WirelessOrder.restaurant.owner + ")");
		}else{
			_restaurantName.setText(WirelessOrder.restaurant.name);
		}
		add(_restaurantName);
		add(new SeparatorField());
		
		VerticalFieldManager _vfm = new VerticalFieldManager(Manager.FIELD_HCENTER);
 
		_vfm.add(_newOrder);
		_vfm.add(_updateOrder);
		_vfm.add(_cancelOrder);
		_vfm.add(_payOrder);
		add(_vfm);
		
		if(isMenuOK){
			_newOrder.setEditable(true);
			_updateOrder.setEditable(true);
			_cancelOrder.setEditable(true);
			_payOrder.setEditable(true);			
		}else{
			_newOrder.setEditable(false);
			_updateOrder.setEditable(false);
			_cancelOrder.setEditable(false);
			_payOrder.setEditable(false);
		}
		
		add(new SeparatorField());
		_restaurantInfo.setText(WirelessOrder.restaurant.info);
		add(_restaurantInfo);		
		
		//Set the listener to new order button.
		_newOrder.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
				UiApplication.getUiApplication().pushScreen(new NewOrderPopup());
			}
		});
		
		//Set the listener to change order button
		_updateOrder.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
				UiApplication.getUiApplication().pushScreen(new UpdateOrderPopup());
			}
		});
		
		//Set the listener to cancel order button
		_cancelOrder.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
				int resp = VerifyPwd.ask(VerifyPwd.PWD_2);
				if(resp == VerifyPwd.VERIFY_PASS){
					UiApplication.getUiApplication().pushScreen(new CancelOrderPopup());
				}else if(resp == VerifyPwd.VERIFY_FAIL){
					Dialog.alert("你输入的权限密码不正确");
				}
			}			
		});
		
		//Set the listener to pay bill button
		_payOrder.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
				UiApplication.getUiApplication().pushScreen(new PayOrderPopup());
			}
		});
	}
	
	protected void makeMenu(Menu menu, int instance){
		menu.add(new MenuItem("帮助", 100, 1){
			public void run(){
				try{
					ProtocolPackage resp = ServerConnector.instance().ask(new ReqOTAUpdate());
					if(resp.header.type == Type.ACK){
						int yes_no = Dialog.ask(Dialog.D_YES_NO, "您将会浏览e点通无线点餐机的在线帮助文档，是否连接到因特网？");
						if(yes_no == Dialog.YES){
							//parse the IP address and port from the response
							String ipAddr = new Short((short)(resp.body[0] & 0xFF)) + "." + 
										new Short((short)(resp.body[1] & 0xFF)) + "." + 
										new Short((short)(resp.body[2] & 0xFF)) + "." + 
										new Short((short)(resp.body[3] & 0xFF));
							int port = (resp.body[4] & 0x000000FF) | ((resp.body[5] & 0x000000FF ) << 8);

							BrowserSession browserSession = Browser.getDefaultSession();
							// now launch the URL to browse the help doc
							browserSession.displayPage("http://" + ipAddr + ":" + port + WirelessOrder.pfInfo.getHelpUrl());
							browserSession.showBrowser();
						}
					}else{
						Dialog.alert("无法获取服务器信息，请检查网络设置");
					}
				}catch(java.io.IOException e){
					Dialog.alert(e.getMessage());
				}
			}
		});
		menu.add(new MenuItem("功能设置", 65536, 1){
			public void run(){
				UiApplication.getUiApplication().pushScreen(new FuncSettingScreen(_self));
			}
		});
		menu.add(new MenuItem("网络设置", 65536, 2){
			public void run(){
				UiApplication.getUiApplication().pushScreen(new NetworkSettingScreen(_self));
			}
		});
		menu.add(new MenuItem("检查更新", 65536, 3){
			public void run(){
				UiApplication.getUiApplication().pushScreen(new OTAUpdatePopup(false));
			}
		});
		menu.add(new MenuItem("更新菜谱", 131172, 1){
			public void run(){
				UiApplication.getUiApplication().pushScreen(new MenuDownloadPopup(_self));
			}
		});
		menu.add(new MenuItem("关于e点通", 196610, 1){
			public void run(){
				Dialog.alert("e点通(v" + ApplicationDescriptor.currentApplicationDescriptor().getVersion() +
							 ")\n版权所有(c) 2010 广州智易科技有限公司");
			}
		});
		menu.add(new MenuItem("关闭", 196610, 2){
			public void run(){
				close();
			}
		});
	}
	
	/**
	 * When entering the main screen, post a global event to exit eBootup process.
	 * When exit the main screen, launch eBootup if the auto startup option is set to ON.
	 */
	protected void onUiEngineAttached(boolean attached){
		if(attached){
			/**
			 * Post a global event to exit the eBootup process after enter the main screen,
			 * since we don't need the eBootup process in the case wireless order application is alive.
			 */
			RuntimeStore store = RuntimeStore.getRuntimeStore();
			try {
				Object obj = store.get(WirelessOrder.E_BOOTUP_PROC_ID);
				if(obj != null){
					int procID = Integer.parseInt(obj.toString());
					ApplicationManager.getApplicationManager().postGlobalEvent(procID, WirelessOrder.GLOBAL_EVENT_EXIT, 0, 0, null, null);		
					store.remove(WirelessOrder.E_BOOTUP_PROC_ID);
				}
				
			} catch(ControlledAccessException e) {
			}
		}else{
			
			WirelessOrder.foodMenu = null;
			/**
			 * A monitor would be launched as a system module after exit the main screen if
			 * the auto startup option is set to ON. The monitor is used to start the wireless
			 * order application automatically in power up action.
			 */
			Params.restore();
			RuntimeStore store = RuntimeStore.getRuntimeStore();
			if(Integer.parseInt(Params.getParam(Params.AUTO_STARTUP)) == Params.ON) {
				int modHandle = CodeModuleManager.getModuleHandle("WirelessOrderTerminal");
				ApplicationDescriptor[] apDes = CodeModuleManager.getApplicationDescriptors(modHandle);
				try {
					int procID = ApplicationManager.getApplicationManager().runApplication(apDes[0], false);
					try {
						store.put(WirelessOrder.E_BOOTUP_PROC_ID, new Integer(procID).toString());
					}catch(ControlledAccessException e){}
					
				}catch(ApplicationManagerException e) {

				}
			}			

		}
	} 
	
	/**
	 * Prompt user to be sure to exit the program
	 */
	protected boolean onSavePrompt(){
		return true;
	}
	
	public boolean onClose(){
		int resp = Dialog.ask(Dialog.D_YES_NO, "确认退出e点通?", Dialog.NO);
		if(resp == Dialog.YES){
			super.onClose();
			return true;			
		}else{
			return false;
		}
	}
	
	/**
	 * Reconnect the network after finishing the network setting.
	 * @param isDirty indicate whether the network parameters are modified or not
	 */
	public void postNetworkSet(boolean isDirty){
		if(isDirty){
			UiApplication.getUiApplication().pushScreen(new QueryMenuPopup(_self));	
		}
	}	

	/**
	 * Reconnect the network after finishing the function setting.
	 * @param isDirty indicate whether the function parameters are modified or not
	 */
	public void postFuncSet(boolean isDirty) {
		if(isDirty){
			UiApplication.getUiApplication().pushScreen(new QueryMenuPopup(_self));	
		}
	}

	public void preQueryMenu() {

	}

	/**
	 * Make the buttons enabled and download the restaurant info
	 * in the case download menu successfully.
	 */
	public void passMenu(ProtocolPackage resp) {
		_newOrder.setEditable(true);
		_updateOrder.setEditable(true);
		_cancelOrder.setEditable(true);
		_payOrder.setEditable(true);
		setFocus();
		UiApplication.getUiApplication().pushScreen(new QueryRestaurantPopup(_self));		
	}

	/**
	 * Make the buttons disabled in the case fail to download menu,
	 * since it doesn't make any sense if the menu doesn't exist. 
	 */
	public void failMenu(ProtocolPackage resp, String errMsg) {
		Dialog.alert(errMsg);
		_newOrder.setEditable(false);
		_updateOrder.setEditable(false);
		_cancelOrder.setEditable(false);
		_payOrder.setEditable(false);
		setFocus();		
	}

	
	public void postQueryMenu() {

	}

	public void preQueryRestaurant() {

	}

	/**
	 * Show the restaurant name, and then run OTA to check whether a newer version exist.
	 */
	public void passQueryRestaurant(ProtocolPackage resp) {
		if(WirelessOrder.restaurant.owner.length() != 0){
			_restaurantName.setText(WirelessOrder.restaurant.name + "(" + WirelessOrder.restaurant.owner + ")");
		}else{
			_restaurantName.setText(WirelessOrder.restaurant.name);
		}
		_restaurantInfo.setText(WirelessOrder.restaurant.info);
		UiApplication.getUiApplication().pushScreen(new OTAUpdatePopup(true));		
	}

	public void failQueryRestuarant(ProtocolPackage resp, String errMsg) {

	}

	public void postQueryRestaurant() {

	}
}










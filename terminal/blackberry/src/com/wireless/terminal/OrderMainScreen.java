package com.wireless.terminal;

import com.wireless.protocol.*;
import com.wireless.ui.neworder.ChangeOrderScreen;
import com.wireless.ui.neworder.NewOrderScreen;
import com.wireless.ui.payoder.PayOrderScreen;
import com.wireless.util.IQueryMenu;
import com.wireless.util.IQueryRestaurant;
import com.wireless.util.RespParser2;
import com.wireless.util.ServerConnector;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;

/**
 * The order screen class to allow for user interaction.
 */
public class OrderMainScreen extends MainScreen	implements IQueryMenu, 
									   					   IQueryRestaurant,
									   					   PostNetworkSet,
									   					   PostFuncSet
{
	
	private ButtonField _newOrder = new ButtonField("      下单      ", ButtonField.CONSUME_CLICK);
	private ButtonField _updateOrder = new ButtonField("      改单      ", ButtonField.CONSUME_CLICK);
	private ButtonField _cancelOrder = new ButtonField("      删单      ", ButtonField.CONSUME_CLICK);
	private ButtonField _payOrder = new ButtonField("      结帐      ", ButtonField.CONSUME_CLICK); 	
	LabelField _restaurantName = new LabelField();
	RichTextField _restaurantInfo = new RichTextField(Field.NON_FOCUSABLE);
	private OrderMainScreen _self = this;
	
	// Constructor
	public OrderMainScreen(boolean isMenuOK, Restaurant info){		
		ApplicationDescriptor descriptor = ApplicationDescriptor.currentApplicationDescriptor();
		setTitle("e点通(v" + descriptor.getVersion() + ")");
		if(info.owner.length() != 0){
			_restaurantName.setText(info.name + "(" + info.owner + ")");
		}else{
			_restaurantName.setText(info.name);
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
		_restaurantInfo.setText(info.info);
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
				UiApplication.getUiApplication().pushScreen(new CancelOrderPopup());
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
							browserSession.displayPage("http://" + ipAddr + ":" + port + "/help/bb8100.html");
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
				UiApplication.getUiApplication().pushScreen(new MenuDownloadPopup(null));
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
	public void passQueryRestaurant(ProtocolPackage resp, Restaurant info) {
		if(info.owner.length() != 0){
			_restaurantName.setText(info.name + "(" + info.owner + ")");
		}else{
			_restaurantName.setText(info.name);
		}
		_restaurantInfo.setText(info.info);
		UiApplication.getUiApplication().pushScreen(new OTAUpdatePopup(true));		
	}

	public void failQueryRestuarant(ProtocolPackage resp, String errMsg) {

	}

	public void postQueryRestaurant() {

	}
}

class NewOrderPopup extends PopupScreen
					implements FieldChangeListener, PostQueryOrder{
	
	private ButtonField _ok;
	private ButtonField _cancel;
	private EditField _tableID;
	private EditField _customNum;
	private NewOrderPopup _self = this;
	
	NewOrderPopup(){
		super(new VerticalFieldManager());
		add(new LabelField("输入下单的台号和人数", LabelField.USE_ALL_WIDTH | DrawStyle.LEFT));
		add(new SeparatorField());
		_tableID = new EditField("台号：", "", 4, TextField.NO_NEWLINE | TextField.NO_LEARNING | EditField.FILTER_NUMERIC);
		_customNum = new EditField("人数：", "", 2 , TextField.NO_NEWLINE | EditField.FILTER_NUMERIC);
		add(_tableID);
		add(_customNum);
		add(new SeparatorField());
		
		HorizontalFieldManager _hfm = new HorizontalFieldManager(Manager.FIELD_HCENTER);
		
		_ok = new ButtonField("确定", ButtonField.CONSUME_CLICK);
		_ok.setChangeListener(this);
		_cancel = new ButtonField("取消", ButtonField.CONSUME_CLICK);
		_cancel.setChangeListener(this);
		_hfm.add(_ok);
		_hfm.add(_cancel);
		add(_hfm);
	}
	
	public void fieldChanged(Field field, int context){
		if(field == _ok){
			execute();
		}else if(field == _cancel){
			close();
		}
	}
	
	protected boolean keyChar(char c, int status, int time){
		if(c == Characters.ESCAPE){
			close();
			return true;
		}else if(c == Characters.ENTER){
			execute();
			return true;
		}else{
			return super.keyChar(c, status, time);
		}
    }
	
	private void execute(){
		if(_tableID.getText().equals("")){
			Dialog.alert("请输入客人就餐的台号");
			_tableID.setFocus();
			return;
		}
		if(_customNum.getText().equals("")){
			Dialog.alert("请输入就餐的客人数量");
			_customNum.setFocus();
			return;
		}
		close();
		UiApplication.getUiApplication().pushScreen(new QueryOrderPopup(Short.parseShort(_tableID.getText()), 
																		Type.QUERY_ORDER_2, 
																		_self));		
	}
	
	public void postQueryOrder(ProtocolPackage response){
		if(response.header.type == Type.NAK){
			if(response.header.reserved == ErrorCode.ORDER_NOT_EXIST){
				UiApplication.getUiApplication().pushScreen(new NewOrderScreen(Short.parseShort(_tableID.getText()), 
																			Integer.parseInt(_customNum.getText())));
			}else if(response.header.reserved == ErrorCode.TABLE_NOT_EXIST){
				Dialog.alert(_tableID + "号台信息不存在");
				
			}else if(response.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED){
				Dialog.alert("终端没有登记到餐厅，请联系管理人员。");
				
			}else if(response.header.reserved == ErrorCode.TERMINAL_EXPIRED){
				Dialog.alert("终端已过期，请联系管理人员。");
				
			}else{
				Dialog.alert("未确定的异常错误(" + response.header.reserved + ")");
			}
		}else{
			Dialog.alert(_tableID + "号台已经下单");
		}
	}
}

class UpdateOrderPopup extends PopupScreen 
						implements FieldChangeListener, PostQueryOrder {
	private ButtonField _ok;
	private ButtonField _cancel;
	private EditField _tableID;
	private UpdateOrderPopup _self = this;
	
	UpdateOrderPopup(){
		super(new VerticalFieldManager());
		add(new LabelField("输入需要改单的台号", LabelField.USE_ALL_WIDTH | DrawStyle.LEFT));
		add(new SeparatorField());
		_tableID = new EditField("台号：", "", 4, TextField.NO_NEWLINE | TextField.NO_LEARNING | EditField.FILTER_NUMERIC);
		add(_tableID);
		add(new SeparatorField());
		_ok = new ButtonField("确定", ButtonField.CONSUME_CLICK);
		_ok.setChangeListener(this);
		_cancel = new ButtonField("取消", ButtonField.CONSUME_CLICK);
		_cancel.setChangeListener(this);
		HorizontalFieldManager _hfm = new HorizontalFieldManager(Manager.FIELD_HCENTER);
		_hfm.add(_ok);
		_hfm.add(_cancel);
		add(_hfm);
	}

	public void fieldChanged(Field field, int context){
		if(field == _ok){
			execute();
		}else if(field == _cancel){
			close();
		}
	}
	
	protected boolean keyChar(char c, int status, int time){
		if(c == Characters.ESCAPE){
			close();
			return true;
		}else if(c == Characters.ENTER){
			execute();
			return true;
		}else{
			return super.keyChar(c, status, time);
		}
    }
	
	private void execute(){
		if(_tableID.getText().equals("")){
			Dialog.alert("请输入需要改单的台号");
			_tableID.setFocus();
			return;
		}
		
		close();
		//Send command to get the bill according to the table ID
		//and then open the change order screen		
		UiApplication.getUiApplication().pushScreen(new QueryOrderPopup(Short.parseShort(_tableID.getText()), 
																		Type.QUERY_ORDER, 
																		_self));

	}
	
	public void postQueryOrder(ProtocolPackage response){
		if(response.header.type == Type.ACK){
			Order _order = RespParser2.parseQueryOrder2(response);
			UiApplication.getUiApplication().pushScreen(new ChangeOrderScreen(_order));
		}else{
			if(response.header.reserved == ErrorCode.ORDER_NOT_EXIST){
				Dialog.alert(_tableID + "号台还未下单");
			}else if(response.header.reserved == ErrorCode.TABLE_NOT_EXIST){
				Dialog.alert(_tableID + "号台信息不存在");
			}else if(response.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED){
				Dialog.alert("终端没有登记到餐厅，请联系管理人员。");				
			}else if(response.header.reserved == ErrorCode.TERMINAL_EXPIRED){
				Dialog.alert("终端已过期，请联系管理人员。");				
			}else{
				Dialog.alert("未确定的异常错误(" + response.header.reserved + ")");
			}
		}
	}
}

class CancelOrderPopup extends PopupScreen 
						implements FieldChangeListener, PostQueryOrder {
	private ButtonField _ok;
	private ButtonField _cancel;
	private EditField _tableID;
	private CancelOrderPopup _self = this;
	
	CancelOrderPopup(){
		super(new VerticalFieldManager());
		add(new LabelField("输入需要删单的台号", LabelField.USE_ALL_WIDTH | DrawStyle.LEFT));
		add(new SeparatorField());
		_tableID = new EditField("台号：", "", 4, TextField.NO_NEWLINE | TextField.NO_LEARNING | EditField.FILTER_NUMERIC);
		add(_tableID);
		add(new SeparatorField());
		_ok = new ButtonField("确定", ButtonField.CONSUME_CLICK);
		_ok.setChangeListener(this);
		_cancel = new ButtonField("取消", ButtonField.CONSUME_CLICK);
		_cancel.setChangeListener(this);
		HorizontalFieldManager _hfm = new HorizontalFieldManager(Manager.FIELD_HCENTER);
		_hfm.add(_ok);
		_hfm.add(_cancel);
		add(_hfm);
	}

	public void fieldChanged(Field field, int context){
		if(field == _ok){
			execute();
		}else if(field == _cancel){
			close();
		}
	}

	protected boolean keyChar(char c, int status, int time){
		if(c == Characters.ESCAPE){
			close();
			return true;
		}else if(c == Characters.ENTER){
			execute();
			return true;
		}else{
			return super.keyChar(c, status, time);
		}
	}

	private void execute(){
		if(_tableID.getText().equals("")){
			Dialog.alert("请输入需要删单的台号");
			_tableID.setFocus();
			return;
		}else{
			close();
			UiApplication.getUiApplication().pushScreen(new QueryOrderPopup(Short.parseShort(_tableID.getText()), 
																			Type.QUERY_ORDER_2,
																			_self));
		}
	}
	
	public void postQueryOrder(ProtocolPackage response){
		if(response.header.type == Type.ACK){
			UiApplication.getUiApplication().pushScreen(new CancelOrderPopup2(Short.parseShort(_tableID.getText())));
		}else{
			if(response.header.reserved == ErrorCode.ORDER_NOT_EXIST){
				Dialog.alert(_tableID + "号台还未下单");
			}else if(response.header.reserved == ErrorCode.TABLE_NOT_EXIST){
				Dialog.alert(_tableID + "号台信息不存在");
			}else if(response.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED){
				Dialog.alert("终端没有登记到餐厅，请联系管理人员。");				
			}else if(response.header.reserved == ErrorCode.TERMINAL_EXPIRED){
				Dialog.alert("终端已过期，请联系管理人员。");				
			}else{
				Dialog.alert("未确定的异常错误(" + response.header.reserved + ")");
			}
		}
	}
}

class PayOrderPopup extends PopupScreen 
					implements FieldChangeListener, PostQueryOrder {
	private ButtonField _ok;
	private ButtonField _cancel;
	private EditField _tableID;
	private PayOrderPopup _self = this;
	
	PayOrderPopup(){
		super(new VerticalFieldManager());
		add(new LabelField("输入需要结帐的台号", LabelField.USE_ALL_WIDTH | DrawStyle.LEFT));
		add(new SeparatorField());
		_tableID = new EditField("台号：", "", 4, TextField.NO_NEWLINE | TextField.NO_LEARNING | EditField.FILTER_NUMERIC);
		add(_tableID);
		add(new SeparatorField());
		_ok = new ButtonField("确定", ButtonField.CONSUME_CLICK);
		_ok.setChangeListener(this);
		_cancel = new ButtonField("取消", ButtonField.CONSUME_CLICK);
		_cancel.setChangeListener(this);
		HorizontalFieldManager _hfm = new HorizontalFieldManager(Manager.FIELD_HCENTER);
		_hfm.add(_ok);
		_hfm.add(_cancel);
		add(_hfm);
	}

	public void fieldChanged(Field field, int context){
		if(field == _ok){
			execute();
		}else if(field == _cancel){
			close();
		}
	}

	protected boolean keyChar(char c, int status, int time){
		if(c == Characters.ESCAPE){
			close();
			return true;
		}if(c == Characters.ENTER){
			execute();
			return true;
		}else{
			return super.keyChar(c, status, time);
		}
	}
	
	private void execute(){
		if(_tableID.getText().equals("")){
			Dialog.alert("请输入需要结帐的台号");
			_tableID.setFocus();
			return;
		}

		close();
		UiApplication.getUiApplication().pushScreen(new QueryOrderPopup(Short.parseShort(_tableID.getText()), 
																		Type.QUERY_ORDER,
																		_self));
	}
	
	public void postQueryOrder(ProtocolPackage response){
		if(response.header.type == Type.ACK){
			Order _order = RespParser2.parseQueryOrder2(response);
			UiApplication.getUiApplication().pushScreen(new PayOrderScreen(_order));
		}else{
			if(response.header.reserved == ErrorCode.ORDER_NOT_EXIST){
				Dialog.alert(_tableID + "号台还未下单");
			}else if(response.header.reserved == ErrorCode.TABLE_NOT_EXIST){
				Dialog.alert(_tableID + "号台信息不存在");
			}else if(response.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED){
				Dialog.alert("终端没有登记到餐厅，请联系管理人员。");				
			}else if(response.header.reserved == ErrorCode.TERMINAL_EXPIRED){
				Dialog.alert("终端已过期，请联系管理人员。");				
			}else{
				Dialog.alert("未确定的异常错误(" + response.header.reserved + ")");
			}
		}
	}
}



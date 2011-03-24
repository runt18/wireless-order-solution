package com.wireless.terminal;

import java.io.IOException;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import com.wireless.protocol.*;
import com.wireless.util.ServerConnector;

class PayOrderPopup2 extends PopupScreen{
	
	private PayOrderPopup2 _self = this;
	private short _table = 0;
	private int _totalPrice = 0;
	private Exception _excep = null;
	private ProtocolPackage _resp = null;
	private PostPayOrder _postPayOrder = null;
	private byte _errCode = ErrorCode.UNKNOWN;
	
	PayOrderPopup2(short table, int totalPrice, PostPayOrder postPayOrder){
		super(new VerticalFieldManager());
		_table = table;
		_totalPrice = totalPrice;
		_postPayOrder = postPayOrder;
		add(new LabelField("提交" + _table + "号台结帐信息...请稍候"));
	}
	
	protected void onUiEngineAttached(boolean attached){
		if(attached == true){
			new Thread(){
				public void run(){
					try{
						int tmp = Integer.parseInt((String)Params.getParam(Params.PRINT_ACTION));
						byte printType = Reserved.DEFAULT_CONF;
						if(tmp == Params.PRINT_SYNC){
							printType |= Reserved.PRINT_SYNC;
						}
						_resp = ServerConnector.instance().ask(new ReqPayOrder(_table, _totalPrice, printType));
						if(_resp.header.type == Type.ACK){
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run(){
									Dialog.alert(_table + "号台结帐成功");
									if(_postPayOrder != null){								
										_postPayOrder.payOrderPass();
									}
								}
							});
						}else{
							_errCode = _resp.header.reserved;
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run(){
									if(_errCode == ErrorCode.TABLE_NOT_EXIST){
										Dialog.alert(_table + "号台已被删除，请与餐厅负责人确认。");
									}else if(_errCode == ErrorCode.ORDER_NOT_EXIST){
										Dialog.alert(_table + "号台的账单已结帐或删除，请与餐厅负责人确认。");
									}else if(_errCode == ErrorCode.PRINT_FAIL){
										Dialog.alert(_table + "号结帐打印未成功，请与餐厅负责人确认。");
									}else{
										Dialog.alert(_table + "号台结帐未成功，请重新结帐");
									}
									if(_postPayOrder != null){
										_postPayOrder.payOrderFail();
									}
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

interface PostPayOrder{
	public void payOrderPass();
	public void payOrderFail();
}


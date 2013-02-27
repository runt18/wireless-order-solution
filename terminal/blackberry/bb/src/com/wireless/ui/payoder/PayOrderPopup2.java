package com.wireless.ui.payoder;

import java.io.IOException;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.pack.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPayOrder;
import com.wireless.protocol.Order;
import com.wireless.util.ServerConnector;

class PayOrderPopup2 extends PopupScreen{
	
	private PayOrderPopup2 _self = this;
	private Order _orderToPay = null;
	private Exception _excep = null;
	private ProtocolPackage _resp = null;
	private PostPayOrder _postPayOrder = null;
	private byte _errCode = ErrorCode.UNKNOWN;
	
	PayOrderPopup2(Order order, PostPayOrder postPayOrder){
		super(new VerticalFieldManager());
		_orderToPay = order;
		_postPayOrder = postPayOrder;
		add(new LabelField("提交" + _orderToPay.getDestTbl().getAliasId() + "号台结帐信息...请稍候"));
	}
	
	protected void onUiEngineAttached(boolean attached){
		if(attached == true){
			new Thread(){
				public void run(){
					try{
						_resp = ServerConnector.instance().ask(new ReqPayOrder(_orderToPay, ReqPayOrder.PAY_CATE_NORMAL));
						if(_resp.header.type == Type.ACK){
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run(){
									Dialog.alert(_orderToPay.getDestTbl().getAliasId() + "号台结帐成功");
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
										Dialog.alert(_orderToPay.getDestTbl().getAliasId() + "号台已被删除，请与餐厅负责人确认。");
									}else if(_errCode == ErrorCode.TABLE_IDLE){
										Dialog.alert(_orderToPay.getDestTbl().getAliasId() + "号台的账单已结帐或删除，请与餐厅负责人确认。");
									}else if(_errCode == ErrorCode.PRINT_FAIL){
										Dialog.alert(_orderToPay.getDestTbl().getAliasId() + "号结帐打印未成功，请与餐厅负责人确认。");
									}else{
										Dialog.alert(_orderToPay.getDestTbl().getAliasId() + "号台结帐未成功，请重新结帐");
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


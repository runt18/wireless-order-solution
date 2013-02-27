package com.wireless.ui.neworder;

import java.io.IOException;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.pack.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqInsertOrder;
import com.wireless.protocol.Order;
import com.wireless.util.ServerConnector;

class SubmitNewPopup extends PopupScreen{ 
	
	private SubmitNewPopup _self = this;
	private Order _reqOrder = null;
	private Exception _excep = null;
	private byte _errCode = ErrorCode.UNKNOWN;
	private PostSubmitOrder _postSubmitOrder = null;
	
	SubmitNewPopup(Order order, PostSubmitOrder postSubmitOrder){
		super(new VerticalFieldManager());
		
		_reqOrder = order;		
		_postSubmitOrder = postSubmitOrder;
		add(new LabelField("提交" + _reqOrder.getDestTbl().getAliasId() + "号台下单信息...请稍候"));

	}
	
	protected void onUiEngineAttached(boolean attached){
		if(attached == true){
			new Thread(){
				public void run(){
					try{

						ProtocolPackage _resp = ServerConnector.instance().ask(new ReqInsertOrder(_reqOrder, Type.INSERT_ORDER));
						if(_resp.header.type == Type.ACK){
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run(){
									Dialog.alert(_reqOrder.getDestTbl().getAliasId() + "号台下单成功。");									
									if(_postSubmitOrder != null)
										_postSubmitOrder.submitOrderPass();
								}
							});
						}else{
							_errCode = _resp.header.reserved;
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run(){
									if(_errCode == ErrorCode.MENU_EXPIRED){
										Dialog.alert("菜谱有更新，请更新菜谱后再重新下单。");
									}else if(_errCode == ErrorCode.TABLE_NOT_EXIST){
										Dialog.alert(_reqOrder.getDestTbl().getAliasId() + "号台信息不存在，请与餐厅负责人确认。");
									}else if(_errCode == ErrorCode.TABLE_BUSY){
										Dialog.alert(_reqOrder.getDestTbl().getAliasId() + "号台已经下单，请与餐厅负责人确认。");
									}else if(_errCode == ErrorCode.PRINT_FAIL){
										Dialog.alert(_reqOrder.getDestTbl().getAliasId() + "号台下单打印未成功，请与餐厅负责人确认。");
									}else if(_errCode == ErrorCode.EXCEED_GIFT_QUOTA){
										Dialog.alert("赠送的菜品已超出赠送额度，请与餐厅负责人确认。");
									}else{
										Dialog.alert(_reqOrder.getDestTbl().getAliasId() + "号台下单失败，请重新提交下单。");
									}									
									if(_postSubmitOrder != null){
										_postSubmitOrder.submitOrderFail();
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

interface PostSubmitOrder{
	public void submitOrderPass();
	public void submitOrderFail();
}

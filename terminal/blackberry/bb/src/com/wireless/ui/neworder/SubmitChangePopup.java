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

class SubmitChangePopup extends PopupScreen{
	
	private Order _reqOrder = null;
	private Exception _excep = null;
	//private byte _errCode = ErrorCode.UNKNOWN;
	private PostSubmitOrder _postSubmitOrder = null;
	private PopupScreen _self = this; 
	
	SubmitChangePopup(Order order, PostSubmitOrder postSubmitOrder){
		super(new VerticalFieldManager());		
		_reqOrder = order;		
		_postSubmitOrder = postSubmitOrder;		
		add(new LabelField("提交" + _reqOrder.getDestTbl().getAliasId() + "号台改单信息...请稍候"));
	}
	
	protected void onUiEngineAttached(boolean attached){
		if(attached == true){
			new Thread(){
				public void run(){
					try{
						//Update the current order normally
						ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(_reqOrder, Type.UPDATE_ORDER));
						if(resp.header.type == Type.ACK){
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run(){
									if(_reqOrder.getDestTbl().getAliasId() == _reqOrder.getSrcTbl().getAliasId()){
										Dialog.alert(_reqOrder.getDestTbl().getAliasId() + "号台改单成功。");
									}else{
										Dialog.alert(_reqOrder.getSrcTbl().getAliasId() + "号台转至" + 
												 	 _reqOrder.getDestTbl().getAliasId() + "号台，并改单成功。");
									}

									if(_postSubmitOrder != null){
										_postSubmitOrder.submitOrderPass();
									}
								}									
							});

						}else{
							throw new Exception(getErrMsg(_reqOrder.getDestTbl().getAliasId(), resp.header.reserved));									
						}
						
					}catch(IOException e){
						_excep = e;
						UiApplication.getUiApplication().invokeLater(new Runnable(){
							public void run(){
								Dialog.alert(_excep.getMessage());
								if(_postSubmitOrder != null){
									_postSubmitOrder.submitOrderFail();
								}
							}
						});
						if(_postSubmitOrder != null){
							_postSubmitOrder.submitOrderFail();
						}
					}catch(Exception e){
						_excep = e;
						UiApplication.getUiApplication().invokeLater(new Runnable(){
							public void run(){
								Dialog.alert(_excep.getMessage());
								if(_postSubmitOrder != null){
									_postSubmitOrder.submitOrderFail();
								}
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
	
	private String getErrMsg(int tableID, byte errCode){
		if(errCode == ErrorCode.MENU_EXPIRED){
			return "菜谱有更新，请更新菜谱后再重新改单。"; 
			
		}else if(errCode == ErrorCode.TABLE_NOT_EXIST){			
			return tableID + "号台信息不存在，请与餐厅负责人确认。";
			
		}else if(errCode == ErrorCode.TABLE_IDLE){			
			return tableID + "号台的账单已结帐或删除，请与餐厅负责人确认。";
			
		}else if(errCode == ErrorCode.TABLE_BUSY){
			return tableID + "号台已经下单。";
			
		}else if(errCode == ErrorCode.EXCEED_GIFT_QUOTA){
			return "赠送的菜品已超出赠送额度，请与餐厅负责人确认。";
			
		}else{
			return (tableID + "号台改单失败，请重新提交改单。");
		}
	}
}

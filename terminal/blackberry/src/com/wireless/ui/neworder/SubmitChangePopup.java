package com.wireless.ui.neworder;

import java.io.IOException;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqInsertOrder;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Type;
import com.wireless.terminal.Params;
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
		add(new LabelField("提交" + _reqOrder.table_id + "号台改单信息...请稍候"));
	}
	
	protected void onUiEngineAttached(boolean attached){
		if(attached == true){
			new Thread(){
				public void run(){
					try{
						byte printType = Reserved.DEFAULT_CONF;
						//check if the print sync or async
						int tmp = Integer.parseInt((String)Params.getParam(Params.PRINT_ACTION));
						if(tmp == Params.PRINT_SYNC){
							printType |= Reserved.PRINT_SYNC;
						}
						printType |= Reserved.PRINT_EXTRA_FOOD_2;
						printType |= Reserved.PRINT_CANCELLED_FOOD_2;
						printType |= Reserved.PRINT_TRANSFER_TABLE_2;
						//Update the current order normally
						ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(_reqOrder, Type.UPDATE_ORDER, printType));
						if(resp.header.type == Type.ACK){
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run(){
									if(_reqOrder.table_id == _reqOrder.originalTableID){
										Dialog.alert(_reqOrder.table_id + "号台改单成功。");
									}else{
										Dialog.alert(_reqOrder.originalTableID + "号台转至" + 
												 	 _reqOrder.table_id + "号台，并改单成功。");
									}

									if(_postSubmitOrder != null){
										_postSubmitOrder.submitOrderPass();
									}
								}									
							});

						}else{
							throw new Exception(getErrMsg(_reqOrder.table_id, resp.header.reserved));									
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

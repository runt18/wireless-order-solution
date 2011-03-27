package com.wireless.terminal;

import java.io.IOException;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import com.wireless.protocol.*;
import com.wireless.util.ServerConnector;

class SubmitOrderPopup extends PopupScreen{ 
	
	private SubmitOrderPopup _self = this;
	private Order _reqOrder = null;
	private Exception _excep = null;
	private byte _type = Type.INSERT_ORDER;
	private byte _errCode = ErrorCode.UNKNOWN;
	private PostSubmitOrder _postSubmitOrder = null;
	
	SubmitOrderPopup(Order order, byte type, PostSubmitOrder postSubmitOrder){
		super(new VerticalFieldManager());
		
		if(type != Type.INSERT_ORDER && type != Type.UPDATE_ORDER)
			throw new IllegalArgumentException();
		
		_reqOrder = order;		
		_type = type;
		_postSubmitOrder = postSubmitOrder;
		if(_type == Type.INSERT_ORDER)
			add(new LabelField("提交" + _reqOrder.tableID + "号台下单信息...请稍候"));
		else if(_type == Type.UPDATE_ORDER)
			add(new LabelField("提交" + _reqOrder.tableID + "号台改单信息...请稍候"));
	}
	
	protected void onUiEngineAttached(boolean attached){
		if(attached == true){
			new Thread(){
				public void run(){
					try{
						byte printType = Reserved.DEFAULT_CONF;
						//print both order and order order while inserting a new order
						if(_type == Type.INSERT_ORDER){
							printType |= Reserved.PRINT_ORDER_2 | Reserved.PRINT_ORDER_DETAIL_2;
						}
						//check if the print sync or async
						int tmp = Integer.parseInt((String)Params.getParam(Params.PRINT_ACTION));
						if(tmp == Params.PRINT_SYNC){
							printType |= Reserved.PRINT_SYNC;
						}
						ProtocolPackage _resp = ServerConnector.instance().ask(new ReqInsertOrder(_reqOrder, _type, printType));
						if(_resp.header.type == Type.ACK){
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run(){
									if(_type == Type.INSERT_ORDER)
										Dialog.alert(_reqOrder.tableID + "号台下单成功。");	
									else if(_type == Type.UPDATE_ORDER)
										Dialog.alert(_reqOrder.tableID + "号台改单成功。");
									
									if(_postSubmitOrder != null)
										_postSubmitOrder.submitOrderPass();
								}
							});
						}else{
							_errCode = _resp.header.reserved;
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run(){
									if(_type == Type.INSERT_ORDER){
										if(_errCode == ErrorCode.MENU_EXPIRED){
											Dialog.alert("菜谱有更新，请更新菜谱后再重新下单。");
										}else if(_errCode == ErrorCode.TABLE_NOT_EXIST){
											Dialog.alert(_reqOrder.tableID + "号台已被删除，请与餐厅负责人确认。");
										}else if(_errCode == ErrorCode.TABLE_HAS_PAID){
											Dialog.alert(_reqOrder.tableID + "号台已经下单，请与餐厅负责人确认。");
										}else if(_errCode == ErrorCode.PRINT_FAIL){
											Dialog.alert(_reqOrder.tableID + "号下单打印未成功，请与餐厅负责人确认。");
										}else{
											Dialog.alert(_reqOrder.tableID + "号台下单失败，请重新提交下单。");
										}
									}else if(_type == Type.UPDATE_ORDER){
										if(_errCode == ErrorCode.MENU_EXPIRED){
											Dialog.alert("菜谱有更新，请更新菜谱后再重新改单。"); 
										}else if(_errCode == ErrorCode.TABLE_NOT_EXIST){
											Dialog.alert(_reqOrder.tableID + "号台已被删除，请与餐厅负责人确认。");
										}else if(_errCode == ErrorCode.ORDER_NOT_EXIST){
											Dialog.alert(_reqOrder.tableID + "号台的账单已结帐或删除，请与餐厅负责人确认。");
										}else{
											Dialog.alert(_reqOrder.tableID + "号台改单失败，请重新提交改单。");
										}
									}
									
									if(_postSubmitOrder != null)
										_postSubmitOrder.submitOrderFail();
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

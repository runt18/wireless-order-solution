package com.wireless.ui.neworder;

import java.io.IOException;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import com.wireless.protocol.*;
import com.wireless.terminal.Params;
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
		add(new LabelField("提交" + _reqOrder.table_id + "号台下单信息...请稍候"));

	}
	
	protected void onUiEngineAttached(boolean attached){
		if(attached == true){
			new Thread(){
				public void run(){
					try{
						byte printType = Reserved.DEFAULT_CONF;
						//print both order and order order while inserting a new order
						printType |= Reserved.PRINT_ORDER_2 | Reserved.PRINT_ORDER_DETAIL_2;
						//check if the print sync or async
						int tmp = Integer.parseInt((String)Params.getParam(Params.PRINT_ACTION));
						if(tmp == Params.PRINT_SYNC){
							printType |= Reserved.PRINT_SYNC;
						}
						ProtocolPackage _resp = ServerConnector.instance().ask(new ReqInsertOrder(_reqOrder, Type.INSERT_ORDER, printType));
						if(_resp.header.type == Type.ACK){
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run(){
									Dialog.alert(_reqOrder.table_id + "号台下单成功。");									
									if(_postSubmitOrder != null)
										_postSubmitOrder.submitOrderPass();
								}
							});
						}else{
							_errCode = _resp.header.reserved[0];
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run(){
									if(_errCode == ErrorCode.MENU_EXPIRED){
										Dialog.alert("菜谱有更新，请更新菜谱后再重新下单。");
									}else if(_errCode == ErrorCode.TABLE_NOT_EXIST){
										Dialog.alert(_reqOrder.table_id + "号台已被删除，请与餐厅负责人确认。");
									}else if(_errCode == ErrorCode.TABLE_BUSY){
										Dialog.alert(_reqOrder.table_id + "号台已经下单，请与餐厅负责人确认。");
									}else if(_errCode == ErrorCode.PRINT_FAIL){
										Dialog.alert(_reqOrder.table_id + "号台下单打印未成功，请与餐厅负责人确认。");
									}else if(_errCode == ErrorCode.EXCEED_GIFT_QUOTA){
										Dialog.alert("赠送的菜品已超出赠送额度，请与餐厅负责人确认。");
									}else{
										Dialog.alert(_reqOrder.table_id + "号台下单失败，请重新提交下单。");
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

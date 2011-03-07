package com.wireless.terminal;

import java.io.IOException;

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import com.wireless.protocol.*;
import com.wireless.util.ServerConnector;

class QueryOrderPopup extends PopupScreen{
	
	private QueryOrderPopup _self = this;
	private short _tableID;
	private Exception _excep = null;
	private PostQueryOrder _postQueryOrder = null;
	private ProtocolPackage _resp;
	private byte _queryType = Type.QUERY_ORDER;
	
	QueryOrderPopup(short tableID, byte queryType, PostQueryOrder postQueryOrder){
		super(new VerticalFieldManager());
		if(queryType != Type.QUERY_ORDER && queryType != Type.QUERY_ORDER_2)
			throw new IllegalArgumentException();
		
		_queryType = queryType;
		_tableID = tableID;
		_postQueryOrder = postQueryOrder;
		add(new LabelField("查询" + tableID + "号台信息...请稍候"));
	}
	
	protected void onUiEngineAttached(boolean attached){
		if(attached == true){
			new Thread(){
				public void run(){
					try{
						if(_queryType == Type.QUERY_ORDER){
							_resp = ServerConnector.instance().ask(new ReqQueryOrder(_tableID));
						}else if(_queryType == Type.QUERY_ORDER_2){
							_resp = ServerConnector.instance().ask(new ReqQueryOrder2(_tableID));
						}
						UiApplication.getUiApplication().invokeLater(new Runnable(){
							public void run(){
								if(_postQueryOrder != null)
									_postQueryOrder.postQueryOrder(_resp);								
							}
						});
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

interface PostQueryOrder{
	void postQueryOrder(ProtocolPackage response);
}

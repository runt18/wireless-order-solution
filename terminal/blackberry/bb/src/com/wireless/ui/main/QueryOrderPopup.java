package com.wireless.ui.main;

import java.io.IOException;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqQueryOrder;
import com.wireless.protocol.ReqQueryOrder2;
import com.wireless.protocol.Type;
import com.wireless.util.ServerConnector;

public class QueryOrderPopup extends PopupScreen{
	
	private QueryOrderPopup _self = this;
	private int _tableID;
	private Exception _excep = null;
	private IPostQueryOrder _postQueryOrder = null;
	private ProtocolPackage _resp;
	private byte _queryType = Type.QUERY_ORDER;
	
	public QueryOrderPopup(int tableID, byte queryType, IPostQueryOrder postQueryOrder){
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



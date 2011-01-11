package com.wireless.terminal;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;

public class NetworkSettingScreen extends MainScreen{
	
	private EditField _netAddr = new EditField("服务器IP：", "", 15 , (TextField.NO_NEWLINE | EditField.FILTER_REAL_NUMERIC)){
		protected boolean navigationClick(int status, int time){
			return true;
		}
	};
	private EditField _netPort = new EditField("服务器端口: ", "", 5 , (TextField.NO_NEWLINE | EditField.FILTER_NUMERIC)){
		protected boolean navigationClick(int status, int time){
			return true;
		}
	};
	private EditField _netAPN = new EditField("APN: ", "", 20, TextField.NO_NEWLINE | TextField.NO_LEARNING){
		protected boolean navigationClick(int status, int time){
			return true;
		}
	};
	private EditField _netUser = new EditField("用户名: ", "", 20, TextField.NO_NEWLINE | TextField.NO_LEARNING){
		protected boolean navigationClick(int status, int time){
			return true;
		}
	};
	private PasswordEditField _netPwd = new PasswordEditField("密码: ", "", 20, TextField.NO_NEWLINE | TextField.NO_LEARNING){
		protected boolean navigationClick(int status, int time){
			return true;
		}
	};
	
	private PostNetworkSet _postNetworkSet = null;
	private boolean _isParamDirty = false;
	private PersistentObject _store = null;
	private NetParam _netParam = null;

	private String oldNetAddr;
	private String oldNetPort;
	private String oldNetAPN;
	private String oldNetUser;
	private String oldNetPwd;
	
	public NetworkSettingScreen(PostNetworkSet postNetworkSet){
		super(Screen.DEFAULT_CLOSE);
		_postNetworkSet = postNetworkSet;
		setTitle("网络设置");
		setDirty(true);
		
    	_store = PersistentStore.getPersistentObject(NetParam.PERSISTENT_NET_PARAM_ID);          
        // Synchronize on the PersistentObject so that no other object can
        // acquire the lock before we finish our commit operation.     
        synchronized(_store){         
            // get the network parameter from the storage
        	_netParam = (NetParam)_store.getContents();
        } 
		VerticalFieldManager vfm = new VerticalFieldManager();
		_netAddr.setText(_netParam.getParam(NetParam.NET_ADDR));
		vfm.add(_netAddr);
		_netPort.setText(_netParam.getParam(NetParam.NET_PORT));
		vfm.add(_netPort);
		_netAPN.setText(_netParam.getParam(NetParam.NET_APN));
		vfm.add(_netAPN);
		_netUser.setText(_netParam.getParam(NetParam.NET_USER));
		vfm.add(_netUser);
		_netPwd.setText(_netParam.getParam(NetParam.NET_PWD));
		vfm.add(_netPwd);
		add(vfm);
		add(new SeparatorField());
		
		oldNetAddr = _netAddr.getText();
		oldNetPort = _netPort.getText();
		oldNetAPN = _netAPN.getText();
		oldNetUser = _netUser.getText();
		oldNetPwd = _netPwd.getText();
	
		ButtonField testBtn = new ButtonField("测试", ButtonField.CONSUME_CLICK);
		ButtonField saveBtn = new ButtonField("保存", ButtonField.CONSUME_CLICK);
		ButtonField cancelBtn = new ButtonField("取消", ButtonField.CONSUME_CLICK);
		HorizontalFieldManager hfm = new HorizontalFieldManager(Manager.FIELD_HCENTER);
		hfm.add(testBtn);
		hfm.add(new LabelField("    "));
		hfm.add(saveBtn);
		hfm.add(new LabelField("    "));
		hfm.add(cancelBtn);
		add(hfm);
		
		//set the save button's listener
		saveBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
				if(oldNetAddr.equals(_netAddr.getText()) &&
						   oldNetPort.equals(_netPort.getText()) &&
						   oldNetAPN.equals(_netAPN.getText()) &&
						   oldNetUser.equals(_netUser.getText())&&
						   oldNetPwd.equals(_netPwd.getText())){
					
					_isParamDirty = false;
					
				}else{
					_netParam.setParam(NetParam.NET_ADDR, _netAddr.getText().trim());
					_netParam.setParam(NetParam.NET_PORT, _netPort.getText().trim());
					_netParam.setParam(NetParam.NET_APN, _netAPN.getText().trim());
					_netParam.setParam(NetParam.NET_USER, _netUser.getText().trim());
					_netParam.setParam(NetParam.NET_PWD, _netPwd.getText().trim());
			        // Synchronize on the PersistentObject so that no other object can
			        // acquire the lock before we finish our commit operation.     
			        synchronized(_store){         
			            // get the network parameter from the storage
			        	_store.setContents(_netParam);
			        	_store.commit();
			        } 
			        setDirty(false);					
					_isParamDirty = true;
					Dialog.inform("网络设置保存成功");
				}
			}
		});
		
		//set the cancel button's listener
		cancelBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
				setDirty(false);
				_isParamDirty = false;
				close();
			}
		});	
		
		//set the test button's listener
		testBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
		        ServerConnector.instance().setNetAddr(_netAddr.getText().trim());
		        ServerConnector.instance().setNetPort(_netPort.getText().trim());
		        ServerConnector.instance().setNetAPN(_netAPN.getText().trim());
		        ServerConnector.instance().setNetUser(_netUser.getText().trim());
		        ServerConnector.instance().setNetPwd(_netPwd.getText().trim());
				UiApplication.getUiApplication().pushScreen(new NetworkTestPopup());
			}
		});
	}
	
	protected boolean onSavePrompt(){
		if(oldNetAddr.equals(_netAddr.getText()) &&
		   oldNetPort.equals(_netPort.getText()) &&
		   oldNetAPN.equals(_netAPN.getText()) &&
		   oldNetUser.equals(_netUser.getText())&&
		   oldNetPwd.equals(_netPwd.getText())){
			
			_isParamDirty = false;
			return true;
			
		}else{
			int resp = Dialog.ask(Dialog.D_YES_NO, "网络设置未保存，确认取消?", Dialog.NO);
			if(resp == Dialog.YES){
				_isParamDirty = false;
				return true;
			}else{
				return false;
			}
		}
	}
	
	public boolean onClose(){
		if(super.onClose() == true){
	        //set the parameters to server connector
	        ServerConnector.instance().setNetAddr(_netParam.getParam(NetParam.NET_ADDR));
	        ServerConnector.instance().setNetPort(_netParam.getParam(NetParam.NET_PORT));
	        ServerConnector.instance().setNetAPN(_netParam.getParam(NetParam.NET_APN));
	        ServerConnector.instance().setNetUser(_netParam.getParam(NetParam.NET_USER));
	        ServerConnector.instance().setNetPwd(_netParam.getParam(NetParam.NET_PWD));
			if(_postNetworkSet != null){
				_postNetworkSet.postNetworkSet(_isParamDirty);
			}
			return true;
		}else{
			return false;
		}
	}
}

interface PostNetworkSet{
	/**
	 * Perform the action after network setting. 
	 * @param isDirty indicates whether the network parameters is modified
	 */
	public void postNetworkSet(boolean isDirty);
}

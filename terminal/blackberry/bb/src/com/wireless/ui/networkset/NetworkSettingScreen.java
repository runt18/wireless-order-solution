package com.wireless.ui.networkset;


import com.wireless.terminal.Params;
import com.wireless.ui.field.TopBannerField;
import com.wireless.util.ServerConnector;

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;

public class NetworkSettingScreen extends MainScreen{
	
	private EditField _netAddr = new EditField("服务器：", "", 30 , (TextField.NO_NEWLINE)){
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
	
	private IPostNetworkSet _postNetworkSet = null;
	private boolean _isParamDirty = false;

	public NetworkSettingScreen(IPostNetworkSet postNetworkSet){
		super(Screen.DEFAULT_CLOSE);
		_postNetworkSet = postNetworkSet;
		setBanner(new TopBannerField("网络设置"));
		//setTitle("网络设置");
		setDirty(true);
		
		//restore the net parameters from the persist storage
		Params.restore();
		
		VerticalFieldManager vfm = new VerticalFieldManager();
		_netAddr.setText(Params.getParam(Params.NET_ADDR));
		vfm.add(_netAddr);
		_netPort.setText(Params.getParam(Params.NET_PORT));
		vfm.add(_netPort);
		_netAPN.setText(Params.getParam(Params.NET_APN));
		vfm.add(_netAPN);
		_netUser.setText(Params.getParam(Params.NET_USER));
		vfm.add(_netUser);
		_netPwd.setText(Params.getParam(Params.NET_PWD));
		vfm.add(_netPwd);
		ButtonField testBtn = new ButtonField("测试网络", ButtonField.CONSUME_CLICK | Field.FIELD_RIGHT);
		vfm.add(testBtn);
		add(vfm);
		add(new SeparatorField());
		
		ButtonField okBtn = new ButtonField("确定", ButtonField.CONSUME_CLICK);
		ButtonField cancelBtn = new ButtonField("取消", ButtonField.CONSUME_CLICK);
		HorizontalFieldManager hfm = new HorizontalFieldManager(Manager.FIELD_HCENTER);
		hfm.add(okBtn);
		hfm.add(new LabelField("    "));
		hfm.add(cancelBtn);
		add(hfm);
		
		//set the save button's listener
		okBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
				saveParam();
				close();
				//Dialog.inform("网络设置保存成功");
			}
		});
		
		//set the cancel button's listener
		cancelBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
				setDirty(false);
				close();
			}
		});	
		
		//set the test button's listener
		testBtn.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
				UiApplication.getUiApplication().pushScreen(new NetworkTestPopup(_netAddr.getText(),
																				 _netPort.getText(),
																				 _netAPN.getText(),
																				 _netUser.getText(),
																				 _netPwd.getText()));
			}
		});
	}
	
	protected boolean onSavePrompt(){		
		if(isModified()){
			int resp = Dialog.ask(Dialog.D_SAVE, "网络设置已修改，是否保存?", Dialog.SAVE);
			if(resp == Dialog.SAVE){
				saveParam();
				return true;
			}else if(resp == Dialog.DISCARD){
				return true;
			}else if(resp == Dialog.CANCEL){
				return false;
			}else{
				return true;
			}
		}else{
			return true;			
		}
	}

	/**
	 * Invoke the call back function when exit the function setting screen
	 */
	protected void onUiEngineAttached(boolean attached){
		if(attached == false){
			if(_postNetworkSet != null){
				_postNetworkSet.postNetworkSet(_isParamDirty);
			}
		}
	}
	
	/**
	 * Perform to save the net function setting parameters
	 */
	private void saveParam(){
		if(isModified()){
			Params.setParam(Params.NET_ADDR, _netAddr.getText().trim());
			Params.setParam(Params.NET_PORT, _netPort.getText().trim());
			Params.setParam(Params.NET_APN, _netAPN.getText().trim());
			Params.setParam(Params.NET_USER, _netUser.getText().trim());
			Params.setParam(Params.NET_PWD, _netPwd.getText().trim());
			
			//store the net parameters to persist storage
			Params.store();
			_isParamDirty = true;
			
	        //set the parameters to server connector
	        ServerConnector.instance().setNetAddr(Params.getParam(Params.NET_ADDR));
	        ServerConnector.instance().setNetPort(Params.getParam(Params.NET_PORT));
	        ServerConnector.instance().setNetAPN(Params.getParam(Params.NET_APN));
	        ServerConnector.instance().setNetUser(Params.getParam(Params.NET_USER));
	        ServerConnector.instance().setNetPwd(Params.getParam(Params.NET_PWD));
	        
		}else{
			_isParamDirty = false;
		}
	}
	
	/**
	 * Check if the current parameters are the sames as the one stored in persist storage
	 * @return true if any parameters has been modified, otherwise false
	 */
	private boolean isModified(){
		if(_netAddr.getText().equals(Params.getParam(Params.NET_ADDR)) &&
				   _netPort.getText().equals(Params.getParam(Params.NET_PORT)) &&
				   _netAPN.getText().equals(Params.getParam(Params.NET_APN)) &&
				   _netUser.getText().equals(Params.getParam(Params.NET_USER))&&
				   _netPwd.getText().equals(Params.getParam(Params.NET_PWD)))
		{
			return false;
		}else{
			return true;
		}
	}
}



package com.wireless.terminal;

import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;

public class FuncSettingScreen extends MainScreen{
	
	private ObjectChoiceField _printParam = null;
	private ObjectChoiceField _connTimeout = null;
	private ObjectChoiceField _connType = null;
	private ButtonField _ok = null;
	private ButtonField _cancel = null;
	private ButtonField _restore = null;
	private PostFuncSet _postFuncSet = null; 
	private boolean _isDirty = false;
	
	public FuncSettingScreen(PostFuncSet postFuncSet){
		_postFuncSet = postFuncSet;
		setTitle("功能设置");

		//restore the parameters from the persist storage
		Params.restore();
        
		VerticalFieldManager vfm = new VerticalFieldManager();
		//add the PIN value
		vfm.add(new ObjectChoiceField("PIN:", new String[]{Integer.toHexString(DeviceInfo.getDeviceId()).toUpperCase()}, 0, Field.READONLY));

		//add the print parameter drop down list
		String[] values = {"异步", "同步"};
		_printParam = new ObjectChoiceField("后厨打印:", values, Integer.parseInt(Params.getParam(Params.PRINT_ACTION)));
		vfm.add(_printParam);
		//add the connection timeout drop down list
		values = new String[]{"10s", "15s", "20s"};
		_connTimeout = new ObjectChoiceField("连接超时:", values, Integer.parseInt(Params.getParam(Params.CONN_TIME_OUT)));
		vfm.add(_connTimeout);
		//add the connection type drop down list
		values = new String[]{"移动网络", "Wi-Fi"};
		String networkName = RadioInfo.getCurrentNetworkName();
		if(networkName != null){
			values[0] = networkName;
		}		                        
		_connType = new ObjectChoiceField("网络类型:", values, Integer.parseInt(Params.getParam(Params.CONN_TYPE)));
		vfm.add(_connType);
		//add the restore button
		_restore = new ButtonField("恢复默认值", Field.FIELD_RIGHT | ButtonField.CONSUME_CLICK);
		_restore.setChangeListener(new FieldChangeListener(){
			//the action to restore defaults
			public void fieldChanged(Field field, int context) {
				_printParam.setSelectedIndex(Params.PRINT_ASYNC);
				_connTimeout.setSelectedIndex(Params.CONN_TIMEOUT_10);
				_connType.setSelectedIndex(Params.CONN_MOBILE);
			}
		});
		vfm.add(_restore);
		add(vfm);
		add(new SeparatorField());
		//add the ok button
		HorizontalFieldManager hfm = new HorizontalFieldManager(Field.FIELD_HCENTER);
		_ok = new ButtonField("确定", ButtonField.CONSUME_CLICK);
		_ok.setChangeListener(new FieldChangeListener(){
			//the action to store the function parameters
			public void fieldChanged(Field field, int context){
				saveParam();
				close();
			}
		});		
		hfm.add(_ok);
		hfm.add(new LabelField("    "));
		//add the cancel button
		_cancel = new ButtonField("取消", ButtonField.CONSUME_CLICK);
		_cancel.setChangeListener(new FieldChangeListener(){
			public void fieldChanged(Field field, int context){
				setDirty(false);
				close();
			}
		});
		hfm.add(_cancel);
		add(hfm);
	}
	
	/**
	 * Perform to save all the function parameters to persist storage
	 */
	private void saveParam(){
		if(isModified()){
			_isDirty = true;
			Params.setParam(Params.PRINT_ACTION, Integer.toString(_printParam.getSelectedIndex()));
			Params.setParam(Params.CONN_TIME_OUT, Integer.toString(_connTimeout.getSelectedIndex()));
			Params.setParam(Params.CONN_TYPE, Integer.toString(_connType.getSelectedIndex()));
			//store the function parameters to persist storage
			Params.store();
			
	        ServerConnector.instance().setTimeout(_connTimeout.getSelectedIndex());
	        ServerConnector.instance().setConnType(_connType.getSelectedIndex());
		}else{
			_isDirty = false;
		}
	}
	
	/**
	 * Check if the function parameters set by user are the same as the ones stored in persist storage 
	 * @return true if any function parameter is modified, otherwise false 
	 */
	private boolean isModified(){
		return _printParam.getSelectedIndex() != Integer.parseInt(Params.getParam(Params.PRINT_ACTION)) ||
			   _connTimeout.getSelectedIndex() != Integer.parseInt(Params.getParam(Params.CONN_TIME_OUT)) ||
			   _connType.getSelectedIndex() != Integer.parseInt(Params.getParam(Params.CONN_TYPE));
	}
	

	protected boolean onSavePrompt(){
		if(isModified()){
			int resp = Dialog.ask(Dialog.D_SAVE, "功能设置已修改，是否保存?", Dialog.SAVE);
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
			if(_postFuncSet != null){
				_postFuncSet.postFuncSet(_isDirty);
			}
		}
	}
}

interface PostFuncSet{
	public void postFuncSet(boolean isDirty);
}

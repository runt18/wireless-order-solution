package com.wireless.ui.field;

import com.wireless.terminal.WirelessOrder;

import net.rim.device.api.crypto.MD5Digest;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class VerifyPwd extends PopupScreen implements FieldChangeListener{

	public final static int VERIFY_CANCEL = 0;
	public final static int VERIFY_PASS = 1;
	public final static int VERIFY_FAIL = 2;
	
	private static int _resp = VERIFY_CANCEL;
	private PasswordEditField _pwd;
	private ButtonField _ok;
	private ButtonField _cancel;
	
	private VerifyPwd(){
		super(new VerticalFieldManager());
		add(new LabelField("请输入权限密码", LabelField.USE_ALL_WIDTH | DrawStyle.LEFT));
		add(new SeparatorField());
		_pwd = new PasswordEditField();
		add(_pwd);
		add(new SeparatorField());
		_ok = new ButtonField("确定", ButtonField.CONSUME_CLICK);
		_ok.setChangeListener(this);
		_cancel = new ButtonField("取消", ButtonField.CONSUME_CLICK);
		_cancel.setChangeListener(this);
		HorizontalFieldManager _hfm = new HorizontalFieldManager(Manager.FIELD_HCENTER);
		_hfm.add(_ok);
		_hfm.add(_cancel);
		add(_hfm);
	}

	public static int ask(){
		if(WirelessOrder.restaurant.pwd2 != null){
			if(WirelessOrder.restaurant.pwd2.length() != 0){
				UiApplication.getUiApplication().pushModalScreen(new VerifyPwd());
				return _resp;
			}else{
				return VERIFY_PASS;
			}
		}else{
			return VERIFY_PASS;
		}		
	}

	public void fieldChanged(Field field, int context) {
		if(field == _ok) {			
			_resp = verify();
		} else if (field == _cancel) {
			_resp = VERIFY_CANCEL;
		}
		close();
	}

	protected boolean keyChar(char c, int status, int time) {
		if(c == Characters.ESCAPE) {
			_resp = VERIFY_CANCEL;
			close();
			return true;
		}else if(c == Characters.ENTER) {
			_resp = verify();
			close();
			return true;
		}else{
			return super.keyChar(c, status, time);
		}
	}
	
	private int verify(){
		MD5Digest digest = new MD5Digest();
		digest.reset();
		digest.update(_pwd.getText().getBytes());
		if(WirelessOrder.restaurant.pwd2.equals(toHexString(digest.getDigest()))){
			return VERIFY_PASS;
		}else{
			return VERIFY_FAIL;
		}			
	}
	
	/**
	 * Convert the md5 byte to hex string.
	 * @param md5Msg the md5 byte value
	 * @return the hex string to this md5 byte value
	 */
	private static String toHexString(byte[] md5Msg){
		StringBuffer hexString = new StringBuffer();
		for (int i=0; i < md5Msg.length; i++) {
			if(md5Msg[i] >= 0x00 && md5Msg[i] < 0x10){
				hexString.append("0").append(Integer.toHexString(0xFF & md5Msg[i]));
			}else{
				hexString.append(Integer.toHexString(0xFF & md5Msg[i]));					
			}
		}
		return hexString.toString();
	}
}

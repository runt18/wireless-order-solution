package com.wireless.dialog;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;

import android.widget.EditText;
import com.wireless.common.WirelessOrder;



public class AskPwdDialog{

	public static final int PWD_1 = 1;		//管理员密码
	public static final int PWD_3 = 2;		//店长权限密码
	public static final int PWD_5 = 3;		//退菜密码
	private int _pwdType = PWD_1;
	private EditText _pwdEdtTxt;
	private Context _context;
	
	public AskPwdDialog(Context context, int pwdType) {
	
		_pwdType = pwdType;
		_context = context;
		String title;
		if(_pwdType == PWD_1){
			title = "请输入管理员密码";
		}else if(_pwdType == PWD_3){
			title = "请输入店长权限密码";
		}else if(_pwdType == PWD_5){
			title = "请输入退菜权限密码";
		}else{
			title = "请输入管理员密码";
		}	
		/**
		 * 新建一个弹出框，然后初始化一个EditText
		 * */
    	final EditText editText = new EditText(context);
		editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		new AlertDialog.Builder(context)
			.setTitle(title)
			.setView(editText)
			.setNeutralButton("确定",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,	int which){
						MessageDigest digester;
						try {
							//Convert the password into MD5
							digester = MessageDigest.getInstance("MD5");
						    digester.update(_pwdEdtTxt.getText().toString().getBytes(), 0, _pwdEdtTxt.getText().toString().getBytes().length); 
						    /**
						     * 如果权限密码通过，则显示退菜数量Dialog，
						     * 否则提示密码输入错误
						     */
						    if(isPass(digester.digest())){
						    	onPwdPass(_context);				    	
						    }else{
						    	onPwdFail(_context);
						    }
						}catch(NoSuchAlgorithmException e) {
							
						} 
						
						
					}
				})
			.setNegativeButton("取消", null)
			.show();
	}
	
	
	
	/**
	 * The derived class should override this method to implement its own logic 
	 * in the case of passing password verification. 
	 * @param context
	 */
	protected void onPwdPass(Context context){
		
	}
	
	/**
	 * The derived class should override this method to implement its own logic 
	 * in the case of failure to password verification.
	 * The default implementation is show the dialog.
	 * @param context
	 */
	protected void onPwdFail(Context context){
    	new AlertDialog.Builder(context)
				.setTitle("提示")
				.setMessage("密码不正确，请重新输入")
				.setNeutralButton("确定", null)
				.show();
    	_pwdEdtTxt.selectAll();
	}
	
	/**
	 * Check to see whether the password is passed
	 * @param pwd the permission password in the form of MD5
	 * @return true if pass, otherwise return false
	 */
	private boolean isPass(byte[] pwd){
		String password = toHexString(pwd);
		if(_pwdType == PWD_1){
			return WirelessOrder.restaurant.pwd == null ? true : WirelessOrder.restaurant.pwd.equals(password);	
			
		}else if(_pwdType == PWD_3){
			if(WirelessOrder.restaurant.pwd == null ||
			   WirelessOrder.restaurant.pwd.equals(password)){
				return true;
			}else{
				return WirelessOrder.restaurant.pwd3.equals(password);
			}
			
		}else if(_pwdType == PWD_5){
			if(WirelessOrder.restaurant.pwd == null || WirelessOrder.restaurant.pwd3 == null ||
				WirelessOrder.restaurant.pwd.equals(password) || WirelessOrder.restaurant.pwd3.equals(password)){
				return true;
			}else{
				return WirelessOrder.restaurant.pwd5.equals(password);
			}			
		}else{			
			return false;
		}	
	}
	
	/**
	 * Convert the md5 byte to hex string.
	 * @param md5Msg the md5 byte value
	 * @return the hex string to this md5 byte value
	 */
	private String toHexString(byte[] md5Msg){
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

package com.wireless.dialog;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;



public class AskPwdDialog{

	public static final int PWD_1 = 1;		//����Ա����
	public static final int PWD_3 = 2;		//�곤Ȩ������
	public static final int PWD_5 = 3;		//�˲�����
	private int _pwdType = PWD_1;
	private Context _context;
	private EditText _pwdEdtTxt;
	
	public AskPwdDialog(Context context, int pwdType) {
	
		_pwdType = pwdType;
		_context = context;
		String title;
		if(_pwdType == PWD_1){
			title = "���������Ա����";
		}else if(_pwdType == PWD_3){
			title = "������곤Ȩ������";
		}else if(_pwdType == PWD_5){
			title = "�������˲�Ȩ������";
		}else{
			title = "���������Ա����";
		}	
		/**
		 * �½�һ��������Ȼ���ʼ��һ��EditText
		 **/
    	_pwdEdtTxt = new EditText(context);
    	_pwdEdtTxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		final AlertDialog askPwdDialog = new AlertDialog.Builder(context)
			.setTitle(title)
			.setView(_pwdEdtTxt)
			.setNeutralButton("ȷ��",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,	int which){					
						
					}
				})
			.setNegativeButton("ȡ��", null)
			.create();
		
		askPwdDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				askPwdDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						MessageDigest digester;
						try {
							//Convert the password into MD5
							digester = MessageDigest.getInstance("MD5");
						    digester.update(_pwdEdtTxt.getText().toString().getBytes(), 0, _pwdEdtTxt.getText().toString().getBytes().length); 
						    /**
						     * ���Ȩ������ͨ��������ʾ�˲�����Dialog��
						     * ������ʾ�����������
						     */
						    if(isPass(digester.digest())){
						    	askPwdDialog.dismiss();
						    	onPwdPass(_context);				    	
						    }else{
						    	onPwdFail(_context);
						    }
						}catch(NoSuchAlgorithmException e) {
							
						} 
					}						
				});			
			}
		});
		
		askPwdDialog.show();
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
    	Toast.makeText(_context, "���벻��ȷ������������", 0).show();
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

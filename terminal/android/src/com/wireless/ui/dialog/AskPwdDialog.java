package com.wireless.ui.dialog;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.ui.R;

public class AskPwdDialog extends Dialog{

	public static final int PWD_1 = 1;
	public static final int PWD_2 = 2;
	public static final int PWD_3 = 3;
	private int _pwdType = PWD_1;
	private EditText _pwdEdtTxt;
	private Context _context;
	
	public AskPwdDialog(Context context, int pwdType) {
		super(context, R.style.FullHeightDialog);
		_pwdType = pwdType;
		_context = context;
		View view = LayoutInflater.from(context).inflate(R.layout.alert, null);
		setContentView(view);
		//getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);

		String title;
		if(_pwdType == PWD_1){
			title = "����������";
		}else if(_pwdType == PWD_2){
			title = "������Ȩ������1";
		}else if(_pwdType == PWD_3){
			title = "������Ȩ������2";
		}else{
			title = "������Ȩ������";
		}			
		((TextView)view.findViewById(R.id.ordername)).setText(title);
		
		_pwdEdtTxt = (EditText)view.findViewById(R.id.mycount);
		((TextView)findViewById(R.id.table)).setText("���룺");
		Button okBtn = (Button)view.findViewById(R.id.confirm);
		okBtn.setText("ȷ��");
		okBtn.setOnClickListener(new View.OnClickListener() {				
			@Override
			public void onClick(View arg0) {
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
				    	onPwdPass(_context);				    	
				    }else{
				    	onPwdFail(_context);
				    }
				}catch(NoSuchAlgorithmException e) {
					
				} 
			}
		});
		
		Button cancle = (Button)view.findViewById(R.id.alert_cancel);
		cancle.setText("ȡ��");
		cancle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
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
				.setTitle("��ʾ")
				.setMessage("���벻��ȷ������������")
				.setNeutralButton("ȷ��", null)
				.show();
    	_pwdEdtTxt.selectAll();
	}
	
	/**
	 * Check to see whether the password is passed
	 * @param pwd the permission password in the form of MD5
	 * @return true if pass, otherwise return false
	 */
	private boolean isPass(byte[] pwd){
		if(_pwdType == PWD_1){
			if(WirelessOrder.restaurant.pwd.equals(toHexString(pwd))){
				return true;
			}else{
				return false;
			}		
			
		}else if(_pwdType == PWD_2){
			if(WirelessOrder.restaurant.pwd.equals(toHexString(pwd))){
				return true;
			}else{
				if(WirelessOrder.restaurant.pwd2.equals(toHexString(pwd))){
					return true;
				}else{
					return false;
				}
			}
			
		}else if(_pwdType == PWD_3){
			if(WirelessOrder.restaurant.pwd.equals(toHexString(pwd))){
				return true;
			}else{
				if(WirelessOrder.restaurant.pwd2.equals(toHexString(pwd))){
					return true;
				}else{
					if(WirelessOrder.restaurant.pwd3.equals(toHexString(pwd))){
						return true;
					}else{
						return false;
					}
				}
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

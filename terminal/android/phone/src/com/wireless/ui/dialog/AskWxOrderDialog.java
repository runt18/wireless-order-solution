package com.wireless.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.lib.task.QueryWxOrderTask;
import com.wireless.pack.req.ReqQueryWxOrder;
import com.wireless.parcel.WxOrderParcel;
import com.wireless.pojo.weixin.order.WxOrder;
import com.wireless.ui.QuickPickActivity;
import com.wireless.ui.R;

public class AskWxOrderDialog extends DialogFragment {
	
	public final static String TAG = "AskWxOrderDialog";
	
	private Activity mParentActivity;
	
	public static AskWxOrderDialog newInstance(){
		AskWxOrderDialog instance = new AskWxOrderDialog();
		return instance;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mParentActivity = activity;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.ask_wx_order_dialog, (ViewGroup)getActivity().getWindow().getDecorView(), false);

		final EditText wxOrderEdtTxt = (EditText)dialogView.findViewById(R.id.edtTxt_ask_WxOrder_dialog);

        // Request focus and show soft keyboard automatically
		wxOrderEdtTxt.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            	if(hasFocus){
            		wxOrderEdtTxt.post(new Runnable() {
	                    @Override
	                    public void run() {
	                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	                        imm.showSoftInput(wxOrderEdtTxt, InputMethodManager.SHOW_IMPLICIT);
	                    }
	                });
            	}
            }
        });

		dialogView.post(new Runnable(){
			@Override
			public void run() {
				wxOrderEdtTxt.requestFocus();
			}
		});
		
		return new AlertDialog.Builder(getActivity()).setTitle("请输入微信账单号")
				.setIcon(android.R.drawable.ic_dialog_info).setView(dialogView)
				.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(final DialogInterface dialog, int id) {
						ReqQueryWxOrder.Builder builder = new ReqQueryWxOrder.Builder();
						String[] orderIdList = wxOrderEdtTxt.getText().toString().split(",");
						for(String orderId : orderIdList){
							builder.add(Integer.parseInt(orderId.trim()));
						}
						new QueryWxOrderTask(WirelessOrder.loginStaff, builder) {
							
							private ProgressDialog mProgDialog;
							
							@Override
							protected void onPreExecute(){
								mProgDialog = ProgressDialog.show(getActivity(), "", "正在读取微信账单信息...请稍后", true);
							}
							
							@Override
							public void onSuccess(List<WxOrder> result) {
								try{
									ArrayList<WxOrderParcel> wxOrderParcels = new ArrayList<WxOrderParcel>(result.size());
									for(WxOrder src : result){
										wxOrderParcels.add(new WxOrderParcel(src));
									}
									Bundle bundle = new Bundle();
									bundle.putParcelableArrayList(WxOrderParcel.KEY_VALUE, wxOrderParcels);
									Intent intent = new Intent(mParentActivity, QuickPickActivity.class);
									intent.putExtras(bundle);
									mParentActivity.startActivity(intent);
									
								}finally{	
									mProgDialog.dismiss();
								}
							}
							
							@Override
							public void onFail(BusinessException e) {
								mProgDialog.dismiss();
								new AlertDialog.Builder(mParentActivity).setTitle("提示")
											   .setMessage(e.getMessage())
											   .setPositiveButton("确定", null)
											   .show();
							}
						}.execute();
			
					}
   			  
				}).setNegativeButton("取消", null).create();
	}
}

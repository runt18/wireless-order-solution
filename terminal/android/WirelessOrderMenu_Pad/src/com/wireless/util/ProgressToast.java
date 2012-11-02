package com.wireless.util;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.ordermenu.R;

public class ProgressToast {
	Toast mToast;

	public ProgressToast(Activity activity, String text) {
		mToast = new Toast(activity);
		
        LayoutInflater inflate = (LayoutInflater)
        		activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
		View layout = inflate.inflate(R.layout.toast, (ViewGroup) activity.findViewById(R.id.toast_layout_root));
		TextView tv = (TextView)layout.findViewById(R.id.textView_toast);
		tv.setText(text);
		mToast.setView(layout);  
	}
	
	public static ProgressToast show(Activity activity, String text)
	{
		ProgressToast toast = new ProgressToast(activity, text);
		toast.show();
		return toast;
	}
	
	public void show(){
		mToast.show();
	}
	
	public void cancel(){
		mToast.cancel();
		mToast = null;
	}
	
}

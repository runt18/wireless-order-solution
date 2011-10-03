package com.wireless.common;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.wireless.protocol.Food;
import com.wireless.ui.AppContext;
import com.wireless.ui.R;

public class Common {
	//单例模式
     private static Common common;
    public static List<Food> foodlist;
    
    
     
	 public static List<Food> getFoodlist() {
		return foodlist;
	}

	public static void setFoodlist(List<Food> foodlist) {
		Common.foodlist = foodlist;
	}

	public static Common getCommon() {
		if(common==null){
			foodlist=new ArrayList<Food>();
			common=new Common();
		}
		return common;
	}
	 
  //判断当前有没有网络
	public static boolean isNetworkAvailable(Activity mActivity) {
		Context context = mActivity.getApplicationContext();
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
	

	  /*
	   * 点菜弹出的dialog
	   * 
	   * */
	public void getorderFoods(Context context,final List<Food> list, final int position){
		final EditText mycount;
		
		View mView =LayoutInflater.from(context).inflate(R.layout.alert, null);
		final Dialog mDialog = new Dialog(context);
		mDialog.setContentView(mView);
		mDialog.getWindow().setTitle("请输入"+list.get(position).name+"的数量");
		mDialog.getWindow().setTitleColor(R.drawable.dialog_title_bg);
		mDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
		mDialog.show();
	    mycount = (EditText)mView.findViewById(R.id.mycount);
		//mycount.setGravity(Gravity.LEFT);
		mycount.setText("1");
		Button mButtonOK = (Button) mView.findViewById(R.id.confirm);
		mButtonOK.setText("确定");
		mButtonOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    Food food=list.get(position);
				food.setCount(Float.parseFloat(mycount.getText().toString()));
				foodlist.add(food);
				Log.e("", "tttt"+foodlist.size());
				mDialog.cancel();
			}
		});
		
		Button cancle = (Button) mView.findViewById(R.id.cancle);
		cancle.setText("取消");
		cancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.cancel();
			}
		});

		
	}
	
}

package com.wireless.common;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wireless.protocol.Food;
import com.wireless.protocol.Taste;
import com.wireless.ui.R;
import com.wireless.ui.TasteActivity;
import com.wireless.ui.TastesTbActivity;
import com.wireless.ui.orderActivity;

public class Common {
	//单例模式
     private static Common common;
    public static List<Food> foodlist;
   public  static  ProgressDialog dialog;
   orderActivity order;
   private int position;
   TasteActivity taste;


	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
     
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
	 * 统一的加载框
	 * */
	public static  void showDialog(Context context,String title) {
		dialog = new ProgressDialog(context);
		dialog.setMessage(title);
		dialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent keyEvent) {
				if (KeyEvent.KEYCODE_BACK == keyCode) {
					dialog.dismiss();
				}
				return true;
			}
		});
		dialog.show();
	}
	
	/*
	 * 點擊已訂菜單item显示的操作
	 * 
	 * */
	public void onitem(Context context,final List<Food> list, final int position){
		order=(orderActivity)context;
		View mView =LayoutInflater.from(context).inflate(R.layout.itemalert, null);
		final Dialog mDialog = new Dialog(context);
		mDialog.setContentView(mView);
		mDialog.getWindow().setTitle("请选择"+list.get(position).name+"的操作");
		mDialog.getWindow().setTitleColor(R.color.grey);
		mDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
		mDialog.show();
		TextView delete=(TextView)mView.findViewById(R.id.deletefood);
		RelativeLayout r1=(RelativeLayout)mView.findViewById(R.id.r1);
		r1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mDialog.cancel();
				//getdeleteFoods(order,list,position);
				order.Foodfunction(0, position);
			}
		});
		TextView taste=(TextView)mView.findViewById(R.id.taste);
		RelativeLayout r2=(RelativeLayout)mView.findViewById(R.id.r2);
		r2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mDialog.cancel();
				order.Foodfunction(1, position);
				
			}
		});
		
	    TextView isfug=(TextView)mView.findViewById(R.id.isfug);
	    if(list.get(position).hangStatus==Food.FOOD_NORMAL){
			isfug.setText("叫起");
		}else if(list.get(position).hangStatus==Food.FOOD_HANG_UP){
			isfug.setText("取消叫起");
		}
		RelativeLayout r3=(RelativeLayout)mView.findViewById(R.id.r3);
		r3.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(list.get(position).hangStatus == Food.FOOD_NORMAL){
					list.get(position).hangStatus = Food.FOOD_HANG_UP;
					mDialog.cancel();
    			}else if(list.get(position).hangStatus == Food.FOOD_HANG_UP){
    				list.get(position).hangStatus = Food.FOOD_NORMAL;
    				mDialog.cancel();
    			}

				
			}
		});
		
		
		Button mButtonOK = (Button) mView.findViewById(R.id.back);
		mButtonOK.setText("返回");
		
		mButtonOK.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 mDialog.cancel();
			}
		});
		
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
		mDialog.getWindow().setTitleColor(R.color.grey);
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
				Food food = list.get(position);

				boolean isExist = false;
				/**
				 * 遍历已点菜，查找是否存在与新点菜相同的菜品。
				 * 如果存在就把数量累加上去，否则就当作新菜品添加到已点菜中
				 */
				for(int i = 0; i < foodlist.size(); i++){
					if(foodlist.get(i).equals(food)){

						float count = food.getCount().floatValue() + Float.parseFloat(mycount.getText().toString());
						if(count > 255){
							//Dialog.alert(food.name + "最多只能点255份");
							food.setCount(new Float(255));
						}else{
							food.setCount(count);
						}

						isExist = true;
					}
				}

				if(!isExist){
					foodlist.add(food);
					food.setCount(Float.parseFloat(mycount.getText().toString()));
				}

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
	
	
	 /*
	   * 点菜弹出的删除菜dialog
	   * 
	   * */
	public void getdeleteFoods(final Context context,final List<Food> list, final int position){
		final EditText mycount;
		order=(orderActivity)context;
		View mView =LayoutInflater.from(context).inflate(R.layout.alert, null);
		final Dialog mDialog = new Dialog(context);
		mDialog.setContentView(mView);
		mDialog.getWindow().setTitle("请输入"+list.get(position).name+"的删除数量");
		mDialog.getWindow().setTitleColor(R.color.grey);
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
				Food food = list.get(position);
				float count;
				/**
				 * 遍历已点菜，查找是否存在与新点菜相同的菜品。
				 * 如果存在就把数量累加上去，否则就当作新菜品添加到已点菜中
				 */
				if(food.getCount().floatValue()==Float.parseFloat(mycount.getText().toString())){
					list.remove(food);
				}else if(food.getCount().floatValue()>Float.parseFloat(mycount.getText().toString())){
					count=food.getCount().floatValue()-Float.parseFloat(mycount.getText().toString());
					food.setCount(count);
				}else if(food.getCount().floatValue()<Float.parseFloat(mycount.getText().toString())){
					new AlertDialog.Builder(context).setTitle("提示").setMessage("你输入的删除数量大于你已点数量").setNeutralButton("确定", null).show();
				}

				order.onRestart();
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
	
	 /*
	   * 点菜弹出的添加口味dialog
	   * 
	   * */
	public void addtaste(final Context context,final List<Food> foodes, final List<Taste> list, final int position,final int tastepositon,String title,final TextView test,CheckBox checkbox){
		  int num;
          Food food=foodes.get(position);
          Taste taste=list.get(tastepositon);
          num=food.addTaste(taste);
          if(num<0){
        	  checkbox.setChecked(false);
        	  new AlertDialog.Builder(context).setTitle("提示").setMessage("你添加的口味已经大于3种").setNeutralButton("确定", null).show();
        	  
          }else{
        	  init(food,test);
          }
          
	}
	
	 /*
	   * 点菜弹出的删除口味dialog
	   * 
	   * */
	public void deletetaste(final Context context,final List<Food> foodes, final List<Taste> list, final int position,final int tastepositon,String title,final TextView test){
		  int num;
        Food food=foodes.get(position);
        Taste taste=list.get(tastepositon);
        num=food.removeTaste(taste);
        if(num<0){
      	  new AlertDialog.Builder(context).setTitle("提示").setMessage("你当前没有选择口味").setNeutralButton("确定", null).show();
        }else{
        	init(food,test);
        
           
        }
	}
	
	public void init(Food food,TextView test){
		if(food.tastePref.equals("无口味")){
  			test.setText(food.name+":");
  		}else{
  			test.setText(food.name+":"+food.tastePref);
  		}
	}
}

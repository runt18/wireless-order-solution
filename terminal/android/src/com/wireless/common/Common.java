package com.wireless.common;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.ui.ChgOrderActivity;
import com.wireless.ui.MainActivity;
import com.wireless.ui.OrderActivity;
import com.wireless.ui.R;

public class Common {
	//����ģʽ
     private static Common common;
    public static List<OrderFood> foodlist;
   public  static  ProgressDialog dialog;
   OrderActivity order;
   private int position;
   MainActivity main;
   ChgOrderActivity drop;
   public static List<Food> dropfoods;
   //�µ�̨��
   private String orderplatenum;
   //�ĵ�̨��
   private String dropplatenum;
   
   //�ѵ��
   private List<Food> alreadfoods;
   //�µ��
   private List<Food> newfoods;
   
   
   
   
   
   

	public List<Food> getAlreadfoods() {
	return alreadfoods;
}

public void setAlreadfoods(List<Food> alreadfoods) {
	this.alreadfoods = alreadfoods;
}

public List<Food> getNewfoods() {
	return newfoods;
}

public void setNewfoods(List<Food> newfoods) {
	this.newfoods = newfoods;
}

	public String getDropplatenum() {
	return dropplatenum;
}

public void setDropplatenum(String dropplatenum) {
	this.dropplatenum = dropplatenum;
}

	public String getOrderplatenum() {
	return orderplatenum;
}

public void setOrderplatenum(String orderplatenum) {
	this.orderplatenum = orderplatenum;
}

	public static List<Food> getDropfoods() {
	return dropfoods;
   }

   public static void setDropfoods(List<Food> dropfoods) {
	Common.dropfoods = dropfoods;
   }





	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
     
	 public static List<OrderFood> getFoodlist() {
		return foodlist;
	}

	public static void setFoodlist(List<OrderFood> foodlist) {
		Common.foodlist = foodlist;
	}

	public static Common getCommon() {
	
		if(common==null){
			foodlist=new ArrayList<OrderFood>();
			common=new Common();
		}
		return common;
	}
	
	

	 
  //�жϵ�ǰ��û������
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
	 * ͳһ�ļ��ؿ�
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
	 * �c����ӆ�ˆ�item��ʾ�Ĳ���
	 * 
	 * */
	public void onitem(Context context,final List<OrderFood> list, final int position){
		order=(OrderActivity)context;
		View mView =LayoutInflater.from(context).inflate(R.layout.item_alert, null);
		final Dialog mDialog = new Dialog(context,R.style.FullHeightDialog);
		mDialog.setContentView(mView);
//		mDialog.getWindow().setTitle("��ѡ��"+list.get(position).name+"�Ĳ���");
//		mDialog.getWindow().setTitleColor(R.color.grey);
		mDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
		mDialog.show();
		TextView ordername=(TextView)mView.findViewById(R.id.ordername);
		ordername.setText("��ѡ��"+list.get(position).name+"�Ĳ���");
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
		
	    TextView isfug=(TextView)mView.findViewById(R.id.item3Txt);
	    if(list.get(position).hangStatus==OrderFood.FOOD_NORMAL){
			isfug.setText("����");
		}else if(list.get(position).hangStatus==OrderFood.FOOD_HANG_UP){
			isfug.setText("ȡ������");
		}
		RelativeLayout r3=(RelativeLayout)mView.findViewById(R.id.r3);
		r3.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(list.get(position).hangStatus == OrderFood.FOOD_NORMAL){
					list.get(position).hangStatus = OrderFood.FOOD_HANG_UP;
					mDialog.cancel();
    			}else if(list.get(position).hangStatus == OrderFood.FOOD_HANG_UP){
    				list.get(position).hangStatus = OrderFood.FOOD_NORMAL;
    				mDialog.cancel();
    			}

				
			}
		});
		
		
		Button mButtonOK = (Button) mView.findViewById(R.id.back);
		mButtonOK.setText("����");
		
		mButtonOK.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 mDialog.cancel();
			}
		});
		
	}
	
	
	
	
	/*
	 * �µ��item��ʾ�Ĳ���
	 * 
	 * */
	public void expandonitem(final Context context,final  List<List<OrderFood>> lists, final int grouposition,final int childposition){
		drop=(ChgOrderActivity)context;
		View mView =LayoutInflater.from(context).inflate(R.layout.item_alert, null);
		final Dialog mDialog = new Dialog(context,R.style.FullHeightDialog);
		mDialog.setContentView(mView);
		mDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
		mDialog.show();
		TextView ordername=(TextView)mView.findViewById(R.id.ordername);
		ordername.setText("��ѡ��"+lists.get(grouposition).get(childposition).name+"�Ĳ���");
		TextView delete=(TextView)mView.findViewById(R.id.deletefood);
		RelativeLayout r1=(RelativeLayout)mView.findViewById(R.id.r1);
		r1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mDialog.cancel();
				//getdeleteFoods(order,list,position);
				Common.getCommon().setPosition(childposition);	
				Common.getCommon().getdeleteFoods(context, lists.get(grouposition),Common.getCommon().getPosition(), 1);
			}
		});
		TextView taste=(TextView)mView.findViewById(R.id.taste);
		RelativeLayout r2=(RelativeLayout)mView.findViewById(R.id.r2);
		r2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mDialog.cancel();
				drop.Foodfunction(childposition);
				
			}
		});
		
	    TextView isfug=(TextView)mView.findViewById(R.id.item3Txt);
	    if(lists.get(grouposition).get(childposition).hangStatus==OrderFood.FOOD_NORMAL){
			isfug.setText("����");
		}else if(lists.get(grouposition).get(childposition).hangStatus==OrderFood.FOOD_HANG_UP){
			isfug.setText("ȡ������");
		}
		RelativeLayout r3=(RelativeLayout)mView.findViewById(R.id.r3);
		r3.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(lists.get(grouposition).get(childposition).hangStatus == OrderFood.FOOD_NORMAL){
					lists.get(grouposition).get(childposition).hangStatus = OrderFood.FOOD_HANG_UP;
					mDialog.cancel();
    			}else if(lists.get(grouposition).get(childposition).hangStatus == OrderFood.FOOD_HANG_UP){
    				lists.get(grouposition).get(childposition).hangStatus = OrderFood.FOOD_NORMAL;
    				mDialog.cancel();
    			}

				
			}
		});
		
		
		Button mButtonOK = (Button) mView.findViewById(R.id.back);
		mButtonOK.setText("����");
		
		mButtonOK.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 mDialog.cancel();
			}
		});
		
	}

	  /*
	   * ��˵�����dialog
	   * 
	   * */
	public void getorderFoods(Context context,final List<Food> list, final int position){
		final EditText mycount;
		
		View mView =LayoutInflater.from(context).inflate(R.layout.alert, null);
		final Dialog mDialog = new Dialog(context,R.style.FullHeightDialog);
		mDialog.setContentView(mView);
		mDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
		mDialog.show();
		TextView ordername=(TextView)mView.findViewById(R.id.ordername);
		ordername.setText("������"+list.get(position).name+"������");
	    mycount = (EditText)mView.findViewById(R.id.mycount);
		//mycount.setGravity(Gravity.LEFT);
		mycount.setText("1");
		Button mButtonOK = (Button) mView.findViewById(R.id.confirm);
		mButtonOK.setText("ȷ��");
		mButtonOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				OrderFood food = new OrderFood(list.get(position));

				boolean isExist = false;
				/**
				 * �����ѵ�ˣ������Ƿ�������µ����ͬ�Ĳ�Ʒ��
				 * ������ھͰ������ۼ���ȥ������͵����²�Ʒ��ӵ��ѵ����
				 */
		
					for(int i = 0; i < foodlist.size(); i++){
						if(foodlist.get(i).equals(food)){
							float count = food.getCount().floatValue() + Float.parseFloat(mycount.getText().toString());
							if(count > 255){
								//Dialog.alert(food.name + "���ֻ�ܵ�255��");
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
		cancle.setText("ȡ��");
		cancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.cancel();
			}
		});

		
	}
	
	
	 /*
	   * ��˵�����ɾ����dialog
	   * 
	   * */
	public void getdeleteFoods(final Context context,final List<OrderFood> list, final int position,final int code){
		final EditText mycount;
		View mView =LayoutInflater.from(context).inflate(R.layout.alert, null);
		final Dialog mDialog = new Dialog(context,R.style.FullHeightDialog);
		mDialog.setContentView(mView);
		mDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
		mDialog.show();
		TextView ordername=(TextView)mView.findViewById(R.id.ordername);
		ordername.setText("������"+list.get(position).name+"��ɾ������");
	    mycount = (EditText)mView.findViewById(R.id.mycount);
		mycount.setText("1");
		Button mButtonOK = (Button) mView.findViewById(R.id.confirm);
		mButtonOK.setText("ȷ��");
		mButtonOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				OrderFood food = list.get(position);
				float count;
				/**
				 * �����ѵ�ˣ������Ƿ�������µ����ͬ�Ĳ�Ʒ��
				 * ������ھͰ������ۼ���ȥ������͵����²�Ʒ��ӵ��ѵ����
				 */
				if(food.getCount().floatValue()==Float.parseFloat(mycount.getText().toString())){
					list.remove(food);
				}else if(food.getCount().floatValue()>Float.parseFloat(mycount.getText().toString())){
					count=food.getCount().floatValue()-Float.parseFloat(mycount.getText().toString());
					food.setCount(count);
				}else if(food.getCount().floatValue()<Float.parseFloat(mycount.getText().toString())){
					new AlertDialog.Builder(context).setTitle("��ʾ").setMessage("�������ɾ�������������ѵ�����").setNeutralButton("ȷ��", null).show();
				}
                
				if(code==0){
					 order=(OrderActivity)context;
					 //order.onRestart();
				}else{
					drop=(ChgOrderActivity)context;
					//drop.onRestart();
				}
				
				 mDialog.cancel();
			}
		});
		
		Button cancle = (Button) mView.findViewById(R.id.alert_cancel);
		cancle.setText("ȡ��");
		cancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.cancel();
			}
		});

		
	}
	
//	/*
//	 * ����µ���ʱ��ĵ�����
//	 * 
//	 * */
//	public void order(final Context context,final int num){
//		final EditText mycount;
//		View mView =LayoutInflater.from(context).inflate(R.layout.alert, null);
//		final Dialog mDialog = new Dialog(context,R.style.FullHeightDialog);
//		mDialog.setContentView(mView);
//		mDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
//		mDialog.show();
//		TextView ordername=(TextView)mView.findViewById(R.id.ordername);
//		ordername.setText("������̨��:");
//	    mycount = (EditText)mView.findViewById(R.id.mycount);
//		Button mButtonOK = (Button) mView.findViewById(R.id.confirm);
//		mButtonOK.setText("ȷ��");
//		mButtonOK.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if(num==0){
//					main=(MainActivity)context;
//					main.order(mycount.getText().toString());
//					setOrderplatenum(mycount.getText().toString());
//					 mDialog.cancel();
//				}else{
//					main=(MainActivity)context;
//					main.drop(mycount.getText().toString());
//					setDropplatenum(mycount.getText().toString());
//					mDialog.cancel();
//				}
//				
//			}
//		});
//		
//		Button cancle = (Button) mView.findViewById(R.id.cancle);
//		cancle.setText("ȡ��");
//		cancle.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				mDialog.cancel();
//			}
//		});
//
//		
//	}
	

	
	
	 /*
	   * ��˵�������ӿ�ζdialog
	   * 
	   * */
	public void addtaste(final Context context,final List<OrderFood> foodes, final List<Taste> list, final int position,final int tastepositon,String title,final TextView test,CheckBox checkbox){
		  int num;
          OrderFood food=foodes.get(position);
          Taste taste=list.get(tastepositon);
          num=food.addTaste(taste);
          if(num<0){
        	  checkbox.setChecked(false);
        	  new AlertDialog.Builder(context).setTitle("��ʾ").setMessage("����ӵĿ�ζ�Ѿ�����3��").setNeutralButton("ȷ��", null).show();
        	  
          }else{
        	  init(food,test);
          }
          
	}
	
	 /*
	   * ��˵�����ɾ����ζdialog
	   * 
	   * */
	public void deletetaste(final Context context,final List<OrderFood> foodes, final List<Taste> list, final int position,final int tastepositon,String title,final TextView test){
		  int num;
        OrderFood food=foodes.get(position);
        Taste taste=list.get(tastepositon);
        num=food.removeTaste(taste);
        if(num<0){
      	  new AlertDialog.Builder(context).setTitle("��ʾ").setMessage("�㵱ǰû��ѡ���ζ").setNeutralButton("ȷ��", null).show();
        }else{
        	init(food,test);
           
        }
	}
	
	public void init(OrderFood food,TextView test){
		if(food.tastePref.equals("�޿�ζ")){
  			test.setText(food.name+":");
  		}else{
  			test.setText(food.name+":"+food.tastePref); 
  		}
	}
	
	
	
	
	/*
	 * �û�û����������ʱ�����˲�
	 * 
	 * */
	
	public void dropnopawrFoods(final Context context,final List<List<OrderFood>> childs,final int groupPosition, final int childPosition){
		final EditText counts;
		View mView =LayoutInflater.from(context).inflate(R.layout.alert, null);
		final Dialog myDialog = new Dialog(context,R.style.FullHeightDialog);
		myDialog.setContentView(mView);
		myDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
		myDialog.show();
		TextView ordername=(TextView)mView.findViewById(R.id.ordername);
		ordername.setText("������"+childs.get(groupPosition).get(childPosition).name+"���˲�����");
		counts = (EditText)mView.findViewById(R.id.mycount);
		//mycount.setGravity(Gravity.LEFT);
		counts.setText("1");
		Button mButtonOK = (Button) mView.findViewById(R.id.confirm);
		mButtonOK.setText("ȷ��");
		mButtonOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				OrderFood food = childs.get(groupPosition).get(childPosition);
				float count;
				/**
				 * �����ѵ�ˣ������Ƿ�������µ����ͬ�Ĳ�Ʒ��
				 * ������ھͰ������ۼ���ȥ������͵����²�Ʒ��ӵ��ѵ����
				 */
				if(food.getCount().floatValue()==Float.parseFloat(counts.getText().toString())){
					childs.get(groupPosition).remove(food);
				}else if(food.getCount().floatValue()>Float.parseFloat(counts.getText().toString())){
					count=food.getCount().floatValue()-Float.parseFloat(counts.getText().toString());
					food.setCount(count);
				}else if(food.getCount().floatValue()<Float.parseFloat(counts.getText().toString())){
					new AlertDialog.Builder(context).setTitle("��ʾ").setMessage("��������˲������������ѵ�����").setNeutralButton("ȷ��", null).show();
				}

				 //drop.init();
				 myDialog.cancel();
			}
		});
		
		Button cancle = (Button) mView.findViewById(R.id.cancle);
		cancle.setText("ȡ��");
		cancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				myDialog.cancel();
			}
		});

	
	}
}

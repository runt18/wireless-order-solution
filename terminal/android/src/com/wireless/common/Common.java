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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wireless.adapter.ExpandlistAdapter;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.SKitchen;
import com.wireless.protocol.Taste;
import com.wireless.ui.DropActivity;
import com.wireless.ui.KitchenActivity;
import com.wireless.ui.MainActivity;
import com.wireless.ui.R;
import com.wireless.ui.TasteActivity;
import com.wireless.ui.orderActivity;

public class Common {
	//����ģʽ
     private static Common common;
    public static List<Food> foodlist;
   public  static  ProgressDialog dialog;
   orderActivity order;
   private int position;
   TasteActivity taste;
   KitchenActivity kient;
   MainActivity main;
   DropActivity drop;
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

	public KitchenActivity getKient() {
	return kient;
}

public void setKient(KitchenActivity kient) {
	this.kient = kient;
}

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
	public void onitem(Context context,final List<Food> list, final int position){
		order=(orderActivity)context;
		View mView =LayoutInflater.from(context).inflate(R.layout.itemalert, null);
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
		
	    TextView isfug=(TextView)mView.findViewById(R.id.isfug);
	    if(list.get(position).hangStatus==Food.FOOD_NORMAL){
			isfug.setText("����");
		}else if(list.get(position).hangStatus==Food.FOOD_HANG_UP){
			isfug.setText("ȡ������");
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
//		mDialog.getWindow().setTitle("������"+list.get(position).name+"������");
//		mDialog.getWindow().setTitleColor(R.color.grey);
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
				Food food = list.get(position);

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
	public void getdeleteFoods(final Context context,final List<Food> list, final int position,final int code){
		final EditText mycount;
		View mView =LayoutInflater.from(context).inflate(R.layout.alert, null);
		final Dialog mDialog = new Dialog(context,R.style.FullHeightDialog);
		mDialog.setContentView(mView);
		mDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
		mDialog.show();
		TextView ordername=(TextView)mView.findViewById(R.id.ordername);
		ordername.setText("������"+list.get(position).name+"��ɾ������");
	    mycount = (EditText)mView.findViewById(R.id.mycount);
		//mycount.setGravity(Gravity.LEFT);
		mycount.setText("1");
		Button mButtonOK = (Button) mView.findViewById(R.id.confirm);
		mButtonOK.setText("ȷ��");
		mButtonOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Food food = list.get(position);
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
					 order=(orderActivity)context;
					 order.onRestart();
				}else{
					drop=(DropActivity)context;
					drop.onRestart();
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
	 * ����µ���ʱ��ĵ�����
	 * 
	 * */
	public void order(final Context context,final int num){
		final EditText mycount;
		View mView =LayoutInflater.from(context).inflate(R.layout.alert, null);
		final Dialog mDialog = new Dialog(context,R.style.FullHeightDialog);
		mDialog.setContentView(mView);
		mDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
		mDialog.show();
		TextView ordername=(TextView)mView.findViewById(R.id.ordername);
		ordername.setText("������̨��:");
	    mycount = (EditText)mView.findViewById(R.id.mycount);
		Button mButtonOK = (Button) mView.findViewById(R.id.confirm);
		mButtonOK.setText("ȷ��");
		mButtonOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(num==0){
					main=(MainActivity)context;
					main.order(mycount.getText().toString());
					setOrderplatenum(mycount.getText().toString());
					 mDialog.cancel();
				}else{
					main=(MainActivity)context;
					main.drop(mycount.getText().toString());
					setDropplatenum(mycount.getText().toString());
					mDialog.cancel();
				}
				
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
	 * ���ֳ����ֵĶ����˵�
	 * 
	 * */
	AlertDialog aler;
	public void showkichent(Context context,final List<SKitchen> perant,final List<List<Kitchen>> child){
		
	final AlertDialog.Builder mydialog=new  AlertDialog.Builder(context);
		View view=LayoutInflater.from(context).inflate(R.layout.expandablelistview, null);
		mydialog.setTitle("��ѡ�����");
		mydialog.setView(view);
//		mydialog.show();
		ExpandableListView mylistview=(ExpandableListView)view.findViewById(R.id.myExpandableListView);
		
		ExpandlistAdapter adapter=new ExpandlistAdapter(context,perant,child);
		mylistview.setAdapter(adapter);
		mylistview.setGroupIndicator(context.getResources().getDrawable( R.layout.expander_ic_folder));
		
		mylistview.setOnChildClickListener(new OnChildClickListener() {
			
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
		       Kitchen kitchen =child.get(groupPosition).get(childPosition);;
		        if(kient!=null){
		        	kient.getslect(kitchen);
		        	aler.dismiss();
		        }
               Log.e("", "llllllll"+kitchen.name);
                
				return true;
				
			}
		});
		mydialog.setNegativeButton("����", null).setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface arg0, int arg1,
					KeyEvent arg2) {
				// TODO Auto-generated method stub
				return true;
			}
		});
		
		 aler=mydialog.create();
		aler.show();
		
	}
	
	
	 /*
	   * ��˵�������ӿ�ζdialog
	   * 
	   * */
	public void addtaste(final Context context,final List<Food> foodes, final List<Taste> list, final int position,final int tastepositon,String title,final TextView test,CheckBox checkbox){
		  int num;
          Food food=foodes.get(position);
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
	public void deletetaste(final Context context,final List<Food> foodes, final List<Taste> list, final int position,final int tastepositon,String title,final TextView test){
		  int num;
        Food food=foodes.get(position);
        Taste taste=list.get(tastepositon);
        num=food.removeTaste(taste);
        if(num<0){
      	  new AlertDialog.Builder(context).setTitle("��ʾ").setMessage("�㵱ǰû��ѡ���ζ").setNeutralButton("ȷ��", null).show();
        }else{
        	init(food,test);
        
           
        }
	}
	
	public void init(Food food,TextView test){
		if(food.tastePref.equals("�޿�ζ")){
  			test.setText(food.name+":");
  		}else{
  			test.setText(food.name+":"+food.tastePref); 
  		}
	}
	
	
	/*
	 * �ĵ��˲˵�ʱ�򵯳���dialog
	 * 
	 * */
	
	
	public void dropFoods(final Context context,final List<List<Food>> childs,final int groupPosition, final int childPosition){
		final EditText mycount;
		drop=(DropActivity)context;
		View mView =LayoutInflater.from(context).inflate(R.layout.alert, null);
		final Dialog mDialog = new Dialog(context,R.style.FullHeightDialog);
		mDialog.setContentView(mView);
		mDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
		mDialog.show();
		TextView ordername=(TextView)mView.findViewById(R.id.ordername);
		ordername.setText("������"+childs.get(groupPosition).get(childPosition).name+"��ɾ������");
	    mycount = (EditText)mView.findViewById(R.id.mycount);
		//mycount.setGravity(Gravity.LEFT);
		mycount.setText("1");
		Button mButtonOK = (Button) mView.findViewById(R.id.confirm);
		mButtonOK.setText("ȷ��");
		mButtonOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Food food = childs.get(groupPosition).get(childPosition);
				float count;
				/**
				 * �����ѵ�ˣ������Ƿ�������µ����ͬ�Ĳ�Ʒ��
				 * ������ھͰ������ۼ���ȥ������͵����²�Ʒ��ӵ��ѵ����
				 */
				if(food.getCount().floatValue()==Float.parseFloat(mycount.getText().toString())){
					childs.get(groupPosition).remove(food);
				}else if(food.getCount().floatValue()>Float.parseFloat(mycount.getText().toString())){
					count=food.getCount().floatValue()-Float.parseFloat(mycount.getText().toString());
					food.setCount(count);
				}else if(food.getCount().floatValue()<Float.parseFloat(mycount.getText().toString())){
					new AlertDialog.Builder(context).setTitle("��ʾ").setMessage("�������ɾ�������������ѵ�����").setNeutralButton("ȷ��", null).show();
				}

				 drop.init();
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
}

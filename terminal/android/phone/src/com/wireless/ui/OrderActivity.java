package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.excep.BusinessException;
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Table;
import com.wireless.protocol.Taste;
import com.wireless.protocol.TasteGroup;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.ui.dialog.AskCancelAmountDialog;
import com.wireless.ui.dialog.AskCancelAmountDialog.OnAmountChangeListener;
import com.wireless.ui.dialog.AskPwdDialog;
import com.wireless.ui.view.OrderFoodListView;

public class OrderActivity extends Activity implements OnAmountChangeListener{
	// �б������ʾ��ǩ
	private static final String ITEM_FOOD_NAME = "item_food_name";
	private static final String ITEM_FOOD_SUM_PRICE = "item_new_food_price";
	private static final String ITEM_FOOD_COUNT = "item_food_count";
	private static final String ITEM_FOOD_OFFSET = "item_food_offset";
	private static final String ITEM_FOOD_TASTE = "item_food_taste";
	private static final String ITEM_THE_FOOD = "item_the_food";
	private static final String ITEM_IS_ORI_FOOD = "itemIsOriFood";
	private static final String ITEM_IS_OFFSET = "item_is_offset";

	private static final String ITEM_GROUP_NAME = "item_group_name";	
	
	
	private static final int MSG_REFRESH_LIST = 122;
	public static final int ALL_ORDER_REMARK = 123;
	protected boolean isHangUp = false;
	
	private FoodListHandler mFoodListHandler;
	private AsyncTask<FoodMenu, Void, Order> mQueryOrderTask;
	
	private ArrayList<OrderFood> mNewFoodList;
	private Order mOriOrder;
	private Taste[] mOldAllTastes;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.order_activity);
		
		/*
		 * "����"Button
		 */
		TextView title = (TextView) findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("�˵�");

		TextView left = (TextView) findViewById(R.id.textView_left);
		left.setText("����");
		left.setVisibility(View.VISIBLE);

		ImageButton backBtn = (ImageButton) findViewById(R.id.btn_left);
		backBtn.setVisibility(View.VISIBLE);
		backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onBackPressed();
			}
		});
		
		//set the table No
		((EditText)findViewById(R.id.editText_orderActivity_tableNum)).setText(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID));
		//set the default customer to 1
		((EditText)findViewById(R.id.editText_orderActivity_customerNum)).setText("1");
		
		TextView rightTxtView = (TextView)findViewById(R.id.textView_right);
		rightTxtView.setText("�ύ");
		rightTxtView.setVisibility(View.VISIBLE);
		
		/*
		 * �µ�"�ύ"Button
		 */
		ImageButton commitBtn = (ImageButton)findViewById(R.id.btn_right);
		commitBtn.setVisibility(View.VISIBLE);
		commitBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//�µ��߼�
				String tableIdString = ((EditText)findViewById(R.id.editText_orderActivity_tableNum)).getText().toString();
				
				//�����̨�ǿ��������������ʾ
				if(!tableIdString.equals("")){
						
					int tableAlias = Integer.parseInt(tableIdString);
					
					int customNum = 1;
					String custNumString = ((EditText)findViewById(R.id.editText_orderActivity_customerNum)).getText().toString();
					//�������Ϊ�գ���Ĭ��Ϊ1
					if(!custNumString.equals("")){
						customNum = Integer.parseInt(custNumString);
					}
					//�ĵ�
					if(mOriOrder != null){
						Order reqOrder = new Order(mOriOrder.foods);
						
						reqOrder.orderDate = mOriOrder.orderDate;
						reqOrder.setCustomNum(customNum);
						reqOrder.setSrcTbl(mOriOrder.getSrcTbl());
						reqOrder.setDestTbl(new Table(0, tableAlias, 0));
						
						//������µ�ˣ�����ӽ��˵�
						if(!mNewFoodList.isEmpty()){
							reqOrder.addFoods(mNewFoodList.toArray(new OrderFood[mNewFoodList.size()]));
						}
						//�ж��˵��Ƿ�Ϊ�ջ�ȫ���˲�
						if(reqOrder.foods.length != 0){
							//���ȫ���˲�����ʾ�յ�
							for (int i = 0; i < reqOrder.foods.length; i++) {
								if(reqOrder.foods[i].getCount() > 0f ){
									new CommitOrderTask(reqOrder).execute(Type.UPDATE_ORDER);
									break;
								}
								if(i == reqOrder.foods.length - 1){
									Toast.makeText(OrderActivity.this, "�벻Ҫ�ύ�յ�", Toast.LENGTH_SHORT).show();									
								}
							}
						} else {
							Toast.makeText(OrderActivity.this, "����δ��ˣ������µ���", Toast.LENGTH_SHORT).show();
						}
					//���µ�
					}else{
						Order reqOrder = new Order(mNewFoodList.toArray(new OrderFood[mNewFoodList.size()]), tableAlias, customNum);
						if(reqOrder.foods.length != 0){
							new CommitOrderTask(reqOrder).execute(Type.INSERT_ORDER);
						}else{
							Toast.makeText(OrderActivity.this, "����δ��ˣ������µ���", Toast.LENGTH_SHORT).show();
						}
					}
				} else {
					Toast.makeText(OrderActivity.this, "��������ȷ�Ĳ�̨��", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		//ִ��������¹����Ʒ
		new QuerySellOutTask().execute(WirelessOrder.foodMenu.foods);
		
		mFoodListHandler = new FoodListHandler(this);
		mNewFoodList = new ArrayList<OrderFood>();
		mQueryOrderTask = new QueryOrderTask(Integer.valueOf(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID))).execute(WirelessOrder.foodMenu);

		mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
		
		/*
		 * ѡ��ÿ����Ʒ�Ĳ���
		 */
		
		ExpandableListView listView = (ExpandableListView) findViewById(R.id.expandableListView_orderActivity);
		listView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				@SuppressWarnings("unchecked")
				HashMap<String,Object> map = (HashMap<String, Object>) v.getTag();
				if(map.containsKey(ITEM_IS_OFFSET)){
					return true;
				} else if(map.containsKey(ITEM_IS_ORI_FOOD)){
//					mSelectedPos = childPosition;
					new ExtOperDialg((OrderFood)map.get(ITEM_THE_FOOD), true).show();
					return true;
				} else {
					new ExtOperDialg((OrderFood)map.get(ITEM_THE_FOOD), false).show();
					return true;
				}
			}
		});
		
		listView.setOnScrollListener(new OnScrollListener() {				
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((findViewById(R.id.editText_orderActivity_tableNum)).getWindowToken(), 0);
			}
				
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
		});	
		
	}

	@Override
	public void onBackPressed() {
		if(!mNewFoodList.isEmpty()){			
			new AlertDialog.Builder(this)
			.setTitle("��ʾ")
			.setMessage("�˵���δ�ύ���Ƿ�ȷ���˳�?")
			.setNeutralButton("ȷ��",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which){
							finish();
						}
					})
			.setNegativeButton("ȡ��", null)
			.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
					return true;
				}
			}).show();
		}else{
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		mQueryOrderTask.cancel(true);
		super.onDestroy();
	}

	/*
	 * ��ʾ��˵��б��handler ������µ�˵���ʾ
	 */
	private static class FoodListHandler extends Handler {
		
		private static final String[] ITEM_TAGS = { 
			ITEM_FOOD_NAME,
			ITEM_FOOD_COUNT, 
			ITEM_FOOD_SUM_PRICE,
			ITEM_FOOD_TASTE
		};
		
		private static final int[] ITEM_TARGETS = {
			R.id.foodname,
			R.id.accountvalue,
			R.id.pricevalue,
			R.id.taste//��ζ��ʾ
		};
		
		private static final String[] GROUP_ITEM_TAGS = { ITEM_GROUP_NAME };
		private static final int[] GROUP_ITEM_ID = { R.id.grounname };
		
		private WeakReference<OrderActivity> mActivity;
		private ExpandableListView mListView;

		FoodListHandler(OrderActivity activity) {
			mActivity = new WeakReference<OrderActivity>(activity);
			
			mListView = (ExpandableListView) activity.findViewById(R.id.expandableListView_orderActivity);
		}

		@Override
		public void handleMessage(Message msg) {
			OrderActivity act = mActivity.get();
			
			List<Map<String, ?>> groupData = new ArrayList<Map<String, ?>>();
			List<List<Map<String, ?>>> childData =  new ArrayList<List<Map<String, ?>>>();
			
			HashMap<String, Object> map2 = new HashMap<String, Object>();
			map2.put(ITEM_GROUP_NAME, "�µ��");
			groupData.add(map2);
			
			List<Map<String, ?>> newFoodDatas = new ArrayList<Map<String,?>>();
			if(!act.mNewFoodList.isEmpty()){
				for(OrderFood f : act.mNewFoodList)
				{
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(ITEM_FOOD_NAME, f.name);
					map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
					map.put(ITEM_FOOD_SUM_PRICE, Util.CURRENCY_SIGN + Util.float2String2(f.calcPriceWithTaste()));
					map.put(ITEM_FOOD_TASTE, f.hasTaste() ? f.getTasteGroup().getTastePref() : TasteGroup.NO_TASTE_PREF);
					map.put(ITEM_THE_FOOD, f);
					newFoodDatas.add(map);
				}
			}
			childData.add(newFoodDatas);

			//������ѵ��
			if(act.mOriOrder != null && act.mOriOrder.foods.length != 0){
				List<Map<String, ?>> pickedFoodDatas = new ArrayList<Map<String,?>>();
				for(OrderFood f : act.mOriOrder.foods)
				{
					if(f.getCount() != 0f){
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put(ITEM_IS_ORI_FOOD, true);
						map.put(ITEM_FOOD_NAME, f.name);
						map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
						map.put(ITEM_FOOD_SUM_PRICE, Util.CURRENCY_SIGN + Util.float2String2(f.calcPriceWithTaste()));
						map.put(ITEM_FOOD_TASTE, f.hasTaste() ? f.getTasteGroup().getTastePref() : TasteGroup.NO_TASTE_PREF);
						map.put(ITEM_THE_FOOD, f);
						pickedFoodDatas.add(map);
					}
					if(f.getDelta() > 0f)
					{
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put(ITEM_IS_ORI_FOOD, true);
						map.put(ITEM_FOOD_NAME, f.name); 
						map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
						map.put(ITEM_FOOD_SUM_PRICE, Util.float2String2(f.calcPriceWithTaste()));
//						map.put(ITEM_FOOD_TASTE, f.hasTaste() ? f.getTasteGroup().getTastePref() : TasteGroup.NO_TASTE_PREF);
						map.put(ITEM_FOOD_TASTE, f.hasCancelReason() ? f.getCancelReason().getReason() : "û������˲�����");
						map.put(ITEM_THE_FOOD, f);
						map.put(ITEM_IS_OFFSET, true);
						map.put(ITEM_FOOD_OFFSET, Util.float2String2(f.getDelta()));
						pickedFoodDatas.add(map);
					}
				}
				childData.add(pickedFoodDatas);
				
				HashMap<String, Object> map1 = new HashMap<String, Object>();
				map1.put(ITEM_GROUP_NAME, "�ѵ��");
				map1.put(ITEM_IS_ORI_FOOD, true);
				groupData.add(map1);
			}
			
			FoodExpandableAdapter adapter = act.new FoodExpandableAdapter(act, 
					groupData, R.layout.dropgrounpitem, GROUP_ITEM_TAGS, GROUP_ITEM_ID, 
					childData, R.layout.order_activity_child_item, ITEM_TAGS, ITEM_TARGETS);
			
			mListView.setAdapter(adapter);
			
			for(int i=0;i< groupData.size();i++)
			{
				mListView.expandGroup(i);
			}
			calcTotal();
		}
		
		private void calcTotal(){
			OrderActivity act = mActivity.get();
			
			float totalPrice = 0;
			if(!act.mNewFoodList.isEmpty()){
				totalPrice += new Order(act.mNewFoodList.toArray(new OrderFood[act.mNewFoodList.size()])).calcTotalPrice();
				((TextView) act.findViewById(R.id.textView_orderActivity_newCount)).setText(String.valueOf(act.mNewFoodList.size()));
			}
			if(act.mOriOrder != null && act.mOriOrder.foods.length != 0){
				totalPrice += act.mOriOrder.calcTotalPrice();
				((TextView) act.findViewById(R.id.textView_orderActivity_pickedCount)).setText(String.valueOf(act.mOriOrder.foods.length));
			}
			
			((TextView) act.findViewById(R.id.textView_orderActivity_sumPirce)).setText(Util.float2String2((float)Math.round(totalPrice * 100) / 100));
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode == RESULT_OK){			
			if(requestCode == OrderFoodListView.PICK_TASTE){
				/**
				 * ��ζ�ı�ʱ֪ͨListView���и���
				 */
				FoodParcel foodParcel = data.getParcelableExtra(FoodParcel.KEY_VALUE);
				mNewFoodList.add(foodParcel);
				mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
				
			}else if(requestCode == OrderFoodListView.PICK_FOOD){
				/**
				 * ѡ�˸ı�ʱ֪ͨ�µ�˵�ListView���и���
				 */
				OrderParcel orderParcel = data.getParcelableExtra(OrderParcel.KEY_VALUE);
				for(OrderFood f: orderParcel.foods)
					mNewFoodList.add(f);
				mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
			}
			//ȫ����ע
			else if(requestCode ==  OrderActivity.ALL_ORDER_REMARK){
				FoodParcel foodParcel = data.getParcelableExtra(FoodParcel.KEY_VALUE);
				if(foodParcel.hasTaste()){
					Taste[] tempTastes = foodParcel.getTasteGroup().getNormalTastes();
					//Ϊ�����µ�˺��ѵ����ӿ�ζ
					for(OrderFood food : mNewFoodList){
						if(!food.hasTaste()){
							food.makeTasetGroup(tempTastes, null);
						}
						for(Taste taste: tempTastes){
							food.getTasteGroup().addTaste(taste);
						}
						
					}
					mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
					mOldAllTastes = tempTastes;
				}
			}
		}
	}
	
	class FoodExpandableAdapter extends SimpleExpandableListAdapter{
		private List<? extends Map<String, ?>> mGroupData;
		private List<? extends List<? extends Map<String, ?>>> mChildData;
		private PopupWindow mPopup;

		public FoodExpandableAdapter(Context context,
				List<? extends Map<String, ?>> groupData, int groupLayout,
				String[] groupFrom, int[] groupTo,
				List<? extends List<? extends Map<String, ?>>> childData,
				int childLayout, String[] childFrom, int[] childTo) {
			super(context, groupData, groupLayout, groupFrom, groupTo, childData,
					childLayout, childFrom, childTo);
			mGroupData = groupData;
			mChildData = childData;
		}

		
		@Override
		public int getChildrenCount(int groupPosition) {
			switch(groupPosition)
			{
			case 0:
				if(mNewFoodList.isEmpty())
					return 0;
				else break;
			case 1:
				if(mOriOrder == null || mOriOrder.foods.length == 0)
					return 0;
				else break;
			}
			return super.getChildrenCount(groupPosition);
		}


		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			View layout = super.getChildView(groupPosition, childPosition, isLastChild,
					convertView, parent);
			Map<String, ?> map = mChildData.get(groupPosition).get(childPosition);
			final OrderFood food = (OrderFood) map.get(ITEM_THE_FOOD);
			layout.setTag(map);
			
			//show the name to each food
			String status = "";
			if(food.isSpecial()){
				status = "��";
			}
			if(food.isRecommend()){
				if(status.length() == 0){
					status = "��";
				}else{
					status = status + ",��";
				}
			}
			if(food.isGift()){
				if(status.length() == 0){
					status = "��";
				}else{
					status = status + ",��";
				}
			}
			if(status.length() != 0){
				status = "(" + status + ")";
			}
			
			String tempStatus = null;
			if(food.isTemporary){
				tempStatus = "(��)";
			}else{
				tempStatus = "";
			}
			
			String hangStatus = null;
			if(food.hangStatus == OrderFood.FOOD_HANG_UP){
				hangStatus = "��";
			}else if(food.hangStatus == OrderFood.FOOD_IMMEDIATE){
				hangStatus = "��";
			}else{
				hangStatus = "";
			}
			if(hangStatus.length() != 0){
				hangStatus = "(" + hangStatus + ")";
			}
			
			String hurriedStatus = null;
			if(food.isHurried){
				hurriedStatus = "(��)";
			}else{
				hurriedStatus = "";
			}
			
			String comboStatus = null;
			if(food.isCombo()){
				comboStatus = "(��)";
			}else{
				comboStatus = "";
			}
			
			((TextView) layout.findViewById(R.id.foodname)).setText(comboStatus + tempStatus + hangStatus + hurriedStatus + food.name + status);
			
			//������µ��
			if(!map.containsKey(ITEM_IS_ORI_FOOD)){
				//"ɾ��"����			 
				ImageView delFoodImgView = (ImageView)layout.findViewById(R.id.deletefood);
				delFoodImgView.setTag(food);
				delFoodImgView.setBackgroundResource(R.drawable.delete_selector);
				
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(View v) {
						OrderFood food = (OrderFood) v.getTag();
						if(food != null)
							new AskCancelAmountDialog(OrderActivity.this, food,false).show();
					}
				};
				
				delFoodImgView.setOnClickListener(listener);
	
				//"����"����
				ImageView addTasteImgView = (ImageView)layout.findViewById(R.id.addtaste);
				addTasteImgView.setBackgroundResource(R.drawable.amount_selector);
				addTasteImgView.setTag(food);
				addTasteImgView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						final OrderFood food = (OrderFood) v.getTag();
						new AskOrderAmountDialog(food, false).show();							
					}
				});
			} 
			//�ѵ��
			else {
				ImageView cancelFoodImgView = (ImageView) layout.findViewById(R.id.deletefood);
				cancelFoodImgView.setBackgroundResource(R.drawable.tuicai_selector);
				
				ImageView addTasteImgView = (ImageView) layout.findViewById(R.id.addtaste);
				addTasteImgView.setBackgroundResource(R.drawable.cuicai_selector);
				Button restoreBtn = (Button) layout.findViewById(R.id.button_orderFoodListView_childItem_restore);
				//������˲�
				if(map.containsKey(ITEM_IS_OFFSET)){
					cancelFoodImgView.setVisibility(View.INVISIBLE);
					addTasteImgView.setVisibility(View.INVISIBLE);
					
					((TextView) layout.findViewById(R.id.accountvalue)).setText(Util.float2String2(food.getDelta()));
					layout.findViewById(R.id.view_OrderFoodListView_childItem).setVisibility(View.VISIBLE);
					//ȡ���˲˰�ť
					restoreBtn.setVisibility(View.VISIBLE); 
					restoreBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								food.addCount(food.getDelta());		
								food.setCancelReason(null);
								mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
							} catch (BusinessException e) {
								Toast.makeText(OrderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
							}
						}
					});
				//�����˲�
				} else {
					cancelFoodImgView.setVisibility(View.VISIBLE);
					addTasteImgView.setVisibility(View.VISIBLE);
					
					restoreBtn.setVisibility(View.INVISIBLE);
					//show the order amount to each food
					((TextView) layout.findViewById(R.id.accountvalue)).setText(Util.float2String2(food.getCount()));
					layout.findViewById(R.id.view_OrderFoodListView_childItem).setVisibility(View.INVISIBLE);
					//"�˲�"����
					cancelFoodImgView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							if(WirelessOrder.restaurant.pwd5 != null){
								/**
								 * ��ʾ�˲�Ȩ�����룬��֤ͨ�����������ʾɾ������Dialog
								 */
								new AskPwdDialog(OrderActivity.this, AskPwdDialog.PWD_5){
									@Override
									protected void onPwdPass(Context context){
										dismiss();
										new AskCancelAmountDialog(OrderActivity.this,food,true).show();
									}
								}.show();
							}else{
								new com.wireless.ui.dialog.AskCancelAmountDialog(OrderActivity.this,food,true).show();
							}
						}
					});
					//"�߲�"����
					addTasteImgView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							if(food.isHurried){
								food.isHurried = false;
								Toast.makeText(OrderActivity.this, "ȡ���߲˳ɹ�", Toast.LENGTH_SHORT).show();
								mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
							}else{
								food.isHurried = true;
								Toast.makeText(OrderActivity.this, "�߲˳ɹ�", Toast.LENGTH_SHORT).show();	
								mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
							}			
						}
					}); 
				}
			}
			return layout;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			View layout = super.getGroupView(groupPosition, isExpanded, convertView, parent);
			Map<String, ?> map = mGroupData.get(groupPosition);
			//������µ��
			if(!map.containsKey(ITEM_IS_ORI_FOOD)){
				/*
				 * �����˰�ť
				 */
				ImageView orderImg = (ImageView)layout.findViewById(R.id.orderimage);
				orderImg.setVisibility(View.VISIBLE);
				orderImg.setBackgroundResource(R.drawable.order_selector);
				
				orderImg.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						// ��ת��ѡ��Activity
						Intent intent = new Intent(OrderActivity.this, PickFoodActivity.class);
						startActivityForResult(intent, OrderFoodListView.PICK_FOOD);
					}
				});
				
				if(mPopup == null){
					View popupLayout = getLayoutInflater().inflate(R.layout.order_activity_operate_popup, null);
					mPopup = new PopupWindow(popupLayout, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					mPopup.setOutsideTouchable(true);
					mPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_small));
					mPopup.update();
					//ȫ������ť
					Button hangUpBtn = (Button) popupLayout.findViewById(R.id.button_orderActivity_operate_popup_callUp);
					if(isHangUp){
						hangUpBtn.setText("ȡ������");
					} else hangUpBtn.setText("����");
					
					hangUpBtn.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							if(isHangUp){
								for(OrderFood food:mNewFoodList){
									if(food.hangStatus == OrderFood.FOOD_HANG_UP){
										food.hangStatus = OrderFood.FOOD_NORMAL;
									}	
								}
								isHangUp = false; 
								mPopup.dismiss();
								mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
							}
							else if(mNewFoodList.size() > 0){
								new AlertDialog.Builder(OrderActivity.this)
									.setTitle("��ʾ")
									.setMessage("ȷ��ȫ��������?")
									.setNeutralButton("ȷ��", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog,	int which){
												for(int i = 0; i < mNewFoodList.size(); i++){
													OrderFood food = mNewFoodList.get(i);
													if(food.hangStatus == OrderFood.FOOD_NORMAL){
														food.hangStatus = OrderFood.FOOD_HANG_UP;
													}							
												}
												isHangUp = true;
												mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
												mPopup.dismiss();
											}
										})
										.setNegativeButton("ȡ��", null)
										.show();	
							}	
							else {
								Toast.makeText(OrderActivity.this, "û���µ�ˣ��޷�����", Toast.LENGTH_SHORT).show();
							}
						}
					});
					//ȫ����ע
					Button allRemarkBtn = (Button) popupLayout.findViewById(R.id.button_orderActivity_operate_popup_remark);
					if(mOldAllTastes != null)
						allRemarkBtn.setText("ȡ����ע");
					else allRemarkBtn.setText("��ע");
					allRemarkBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if(mOldAllTastes != null){
								for(OrderFood food : mNewFoodList){
									if(food.hasNormalTaste()){
										for(Taste t:mOldAllTastes){
											food.getTasteGroup().removeTaste(t); 
										}
									}
								}
								mOldAllTastes = null;
								mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
								mPopup.dismiss();
							}
							else if(!mNewFoodList.isEmpty()){
								Intent intent = new Intent(OrderActivity.this, PickTasteActivity.class);
								Bundle bundle = new Bundle(); 
								OrderFood dummyFood = new OrderFood();
								dummyFood.name = "ȫ����ע";
								bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(dummyFood));
								bundle.putString(PickTasteActivity.INIT_TAG, PickTasteActivity.TAG_TASTE);
								bundle.putBoolean(PickTasteActivity.PICK_ALL_ORDER_TASTE, true);
								intent.putExtras(bundle);
								startActivityForResult(intent, OrderActivity.ALL_ORDER_REMARK);
								mPopup.dismiss();
							} else {
								Toast.makeText(OrderActivity.this, "�˲�̨��δ��ˣ��޷���ӱ�ע", Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
				
				View orderOperateBtn = layout.findViewById(R.id.button_orderActivity_opera);
				orderOperateBtn.setVisibility(View.VISIBLE);
				orderOperateBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mPopup.showAsDropDown(v);
					}
				});

			} 
			else {
//				boolean hasHangupFood = false;
				final List<? extends Map<String, ?>> foods = mChildData.get(groupPosition);
				for(int i = 0; i < foods.size(); i++){
					@SuppressWarnings("unchecked")
					HashMap<String,Object> amap =  (HashMap<String, Object>) foods.get(i);
					OrderFood food = (OrderFood) amap.get(ITEM_THE_FOOD);
					if(food.hangStatus == OrderFood.FOOD_HANG_UP){
//						hasHangupFood = true;
						break;
					}
				}
				layout.findViewById(R.id.button_orderActivity_opera).setVisibility(View.GONE);
				
//				if(hasHangupFood){
					/**
					 * ���ȫ������ť
					 */
//					ImageView immediateImgView = (ImageView)layout.findViewById(R.id.orderimage);
//					immediateImgView.setVisibility(View.VISIBLE);
//					immediateImgView.setBackgroundResource(R.drawable.jiqi_selector);
//					immediateImgView.setOnClickListener(new View.OnClickListener() {				
//						@Override
//						public void onClick(View v) {						
//							if(foods.size() > 0){
//								new AlertDialog.Builder(OrderActivity.this)
//								.setTitle("��ʾ")
//								.setMessage("ȷ��ȫ��������?")
//								.setNeutralButton("ȷ��", new DialogInterface.OnClickListener() {
//										@Override
//										public void onClick(DialogInterface dialog,	int which){
//											for(int i = 0; i < foods.size(); i++){
//												@SuppressWarnings("unchecked")
//												HashMap<String,Object> amap =  (HashMap<String, Object>) foods.get(i);
//												OrderFood food = (OrderFood) amap.get(ITEM_THE_FOOD);
//												if(food.hangStatus == OrderFood.FOOD_HANG_UP){
//													food.hangStatus = OrderFood.FOOD_IMMEDIATE;
//												}								
//											}
//											mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
//										}
//									})
//									.setNegativeButton("ȡ��", null)
//									.show();	
//							}
//						}
//					});
//					((ImageView) layout.findViewById(R.id.operateimage)).setVisibility(View.INVISIBLE);
//				}else{
					/*
					 * ���û�н���Ĳ�Ʒ����ʾ����Button
					 */
					((ImageView)layout.findViewById(R.id.orderimage)).setVisibility(View.INVISIBLE);
					((ImageView) layout.findViewById(R.id.operateimage)).setVisibility(View.INVISIBLE);
//				}
			}
			return layout;
		}
		
	}
	
	/**
	 * �����Ʒ�б�����չ���� Dialog
	 */
	private class ExtOperDialg extends Dialog{

		
		ExtOperDialg(final OrderFood selectedFood, final boolean isOriFood) {
			super(OrderActivity.this, R.style.FullHeightDialog);
			setContentView(R.layout.item_alert);
			getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView)findViewById(R.id.ordername)).setText("��ѡ��" + selectedFood.name + "�Ĳ���");
			if(!isOriFood){
				/**
				 * �µ������չ����Ϊ"ɾ��"��"��ζ"��"����/ȡ������"����������
				 */
				//ɾ�˹���
				((TextView)findViewById(R.id.item1Txt)).setText("ɾ��");
				((RelativeLayout)findViewById(R.id.r1)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						dismiss();
						new AskCancelAmountDialog(OrderActivity.this,selectedFood,isOriFood).show();							
					}
				});
				
				//��ζ����
				((TextView)findViewById(R.id.item2Txt)).setText("��ζ");
				((RelativeLayout)findViewById(R.id.r2)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						onPickTaste(selectedFood);
						mNewFoodList.remove(selectedFood);
						dismiss();
					}
				});
				
				//����/ȡ������
				if(selectedFood.hangStatus == OrderFood.FOOD_NORMAL){
					((TextView)findViewById(R.id.item3Txt)).setText("����");						
				}else{
					((TextView)findViewById(R.id.item3Txt)).setText("ȡ������");
				}
				((RelativeLayout)findViewById(R.id.r3)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						if(selectedFood.hangStatus == OrderFood.FOOD_NORMAL){
							selectedFood.hangStatus = OrderFood.FOOD_HANG_UP;
							((TextView)findViewById(R.id.item3Txt)).setText("ȡ������");
							dismiss();
						}else{
							selectedFood.hangStatus = OrderFood.FOOD_NORMAL;
							((TextView)findViewById(R.id.item3Txt)).setText("����");		
							dismiss();
						}
					}
				});
				
				//����
				((RelativeLayout)findViewById(R.id.r4)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						dismiss();
						new AskOrderAmountDialog(selectedFood, isOriFood).show();							
					}
				});
				
				
			}else{
				/**
				 * �ѵ�˵���չ����Ϊ"�˲�"��"����"��"�߲�/ȡ���߲�"
				 */
				//�˲˹���
				((TextView)findViewById(R.id.item1Txt)).setText("�˲�");
				((RelativeLayout)findViewById(R.id.r1)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						dismiss();
						if(WirelessOrder.restaurant.pwd5 != null){
							new AskPwdDialog(getContext(), AskPwdDialog.PWD_5){							
								@Override
								protected void onPwdPass(Context context){
									dismiss();
									new AskCancelAmountDialog(OrderActivity.this,selectedFood,isOriFood).show();
								}
							}.show();
						}else{
							new AskCancelAmountDialog(OrderActivity.this,selectedFood,isOriFood).show(); 
						}
					}
				});
				
				//�����Ʒ�ǽ���״̬����ʾ"����"����
				if(selectedFood.hangStatus == OrderFood.FOOD_HANG_UP || selectedFood.hangStatus == OrderFood.FOOD_IMMEDIATE){
					if(selectedFood.hangStatus == OrderFood.FOOD_HANG_UP){
						((TextView)findViewById(R.id.item2Txt)).setText("����");						
					}else{
						((TextView)findViewById(R.id.item2Txt)).setText("���½���");
					}
					((RelativeLayout)findViewById(R.id.r2)).setOnClickListener(new View.OnClickListener() {						
						@Override
						public void onClick(View arg0) {
			    			if(selectedFood.hangStatus == OrderFood.FOOD_HANG_UP){
			    				selectedFood.hangStatus = OrderFood.FOOD_IMMEDIATE;
								((TextView)findViewById(R.id.item2Txt)).setText("����");
								dismiss();
			    				
			    			}else if(selectedFood.hangStatus == OrderFood.FOOD_IMMEDIATE){
			    				selectedFood.hangStatus = OrderFood.FOOD_HANG_UP;
								((TextView)findViewById(R.id.item2Txt)).setText("���½���");
								dismiss();
			    			}						
						}
					});
					
					//�߲�/ȡ���߲˹���
					if(selectedFood.isHurried){
						((TextView)findViewById(R.id.item3Txt)).setText("ȡ���߲�");							
					}else{
						((TextView)findViewById(R.id.item3Txt)).setText("�߲�");						
					}
					((RelativeLayout)findViewById(R.id.r3)).setOnClickListener(new View.OnClickListener() {						
						@Override
						public void onClick(View arg0) {
							if(selectedFood.isHurried){
								selectedFood.isHurried = false;
								((TextView)findViewById(R.id.item3Txt)).setText("�߲�");	
								dismiss();
								
							}else{
								selectedFood.isHurried = true;
								((TextView)findViewById(R.id.item3Txt)).setText("ȡ���߲�");	
								dismiss();
							}
						}
					});
					
				}else{
					
					//�߲�/ȡ���߲˹���
					if(selectedFood.isHurried){
						((TextView)findViewById(R.id.item2Txt)).setText("ȡ���߲�");							
					}else{
						((TextView)findViewById(R.id.item2Txt)).setText("�߲�");						
					}
					((RelativeLayout)findViewById(R.id.r2)).setOnClickListener(new View.OnClickListener() {						
						@Override
						public void onClick(View arg0) {
							if(selectedFood.isHurried){
								selectedFood.isHurried = false;
								((TextView)findViewById(R.id.item2Txt)).setText("�߲�");	
								dismiss();
					
							}else{
								selectedFood.isHurried = true;
								((TextView)findViewById(R.id.item2Txt)).setText("ȡ���߲�");	
								dismiss();
							}						
						}
					});
					
					((ImageView)findViewById(R.id.line3)).setVisibility(View.GONE);
					((ImageView)findViewById(R.id.line4)).setVisibility(View.GONE);
					((RelativeLayout)findViewById(R.id.r3)).setVisibility(View.GONE);
					//����
					((RelativeLayout)findViewById(R.id.r4)).setVisibility(View.GONE);
				}					
			}
			
			//����Button
			Button cancelBtn = (Button)findViewById(R.id.back);		
			cancelBtn.setOnClickListener(new View.OnClickListener() {					
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		}
		
		@Override
		protected void onStop(){
//			mAdapter.notifyDataSetChanged();
			mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
		}		
	}
	
	/**
	 * ��ʾ����������Dialog
	 */
	private class AskOrderAmountDialog extends Dialog{
	
		AskOrderAmountDialog(final OrderFood selectedFood, boolean isOriFood) {
			super(OrderActivity.this, R.style.FullHeightDialog);
			
			View view = LayoutInflater.from(getContext()).inflate(R.layout.alert, null);
			setContentView(view);
			//getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView)view.findViewById(R.id.ordername)).setText("������" + selectedFood.name + "������");
			
			((TextView)findViewById(R.id.table)).setText("������");
			//��������Ĭ��Ϊ�˲�Ʒ�ĵ������
			final EditText amountEdtTxt = (EditText)view.findViewById(R.id.mycount);
			amountEdtTxt.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					amountEdtTxt.selectAll();
				}
			});
			
			//"ȷ��"Button
			Button okBtn = (Button)view.findViewById(R.id.confirm);
			okBtn.setText("ȷ��");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try{
						float amount = Float.parseFloat(amountEdtTxt.getText().toString());
						if(amount > 255){
							Toast.makeText(getContext(), "�Բ���\"" + selectedFood.toString() + "\"���ֻ�ܵ�255��", Toast.LENGTH_SHORT).show();
						}else{
							selectedFood.setCount(amount);
							mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
							Toast.makeText(getContext(), "����\"" + selectedFood.toString() + "\"" + "����Ϊ" + amount + "��", Toast.LENGTH_LONG).show();
							dismiss();
						}							
						
					}catch(NumberFormatException e){
						Toast.makeText(getContext(), "�������������ʽ����ȷ������������", Toast.LENGTH_SHORT).show();
					}					
				}
			});
			
			//"ȡ��"Button
			Button cancelBtn = (Button)view.findViewById(R.id.alert_cancel);
			cancelBtn.setText("ȡ��");
			cancelBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
			
			//���������
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); 
	        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	        imm.showSoftInput(amountEdtTxt, 0); //��ʾ�����
	        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		}		
	}
	
	/*
	 * ѡ����Ӧ��Ʒ��"��ζ"��������ת����ζActivity���п�ζ����ӡ�ɾ������
	 */
	private void onPickTaste(OrderFood selectedFood) {
		if(selectedFood.isTemporary){
			Toast.makeText(this, "��ʱ�˲�����ӿ�ζ", Toast.LENGTH_SHORT).show();
		}else{
			Intent intent = new Intent(OrderActivity.this, PickTasteActivity.class);
			Bundle bundle = new Bundle();
			bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(selectedFood));
			intent.putExtras(bundle);
			startActivityForResult(intent, OrderFoodListView.PICK_TASTE);			
		}
	}
	private class CommitOrderTask extends com.wireless.lib.task.CommitOrderTask{

		private ProgressDialog mProgressDialog;

		public CommitOrderTask(Order reqOrder) {
			super(reqOrder);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = ProgressDialog.show(OrderActivity.this,"", "��ѯ" + mReqOrder.getDestTbl().getAliasId() + "���˵���Ϣ...���Ժ�");
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mProgressDialog.cancel();
			
			if(mBusinessException == null){
				if(mReqOrder.getSrcTbl().equals(mReqOrder.getDestTbl())){
					Toast.makeText(OrderActivity.this, mReqOrder.getDestTbl().getAliasId() + "�Ų�̨�µ��ɹ�", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(OrderActivity.this, mReqOrder.getSrcTbl().getAliasId() + "�Ų�̨�µ��ɹ�����ת��" + mReqOrder.getDestTbl().getAliasId() + "�Ų�̨", Toast.LENGTH_SHORT).show();
				}
				finish();
			}else{
				if(mOriOrder != null){

					if(mBusinessException.getErrCode() == ErrorCode.TABLE_IDLE){
						//����Ǹĵ������ҷ����ǲ�̨���еĴ���״̬��
						//����ʾ�û�������չ��ﳵ�е�ԭ�˵�
						new AlertDialog.Builder(OrderActivity.this)
							.setTitle("��ʾ")
							.setMessage(mReqOrder.getDestTbl().getAliasId() + "�Ų�̨�Ѿ����ʣ��ѵ����Ϣ��ˢ�£��µ����Ϣ���ᱣ��")
							.setNeutralButton("ȷ��",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,	int which){
										mOriOrder = null;
										mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
									}
								})
							.show();

						
					}else if(mBusinessException.getErrCode() == ErrorCode.ORDER_EXPIRED){
						//����Ǹĵ������ҷ������˵����ڵĴ���״̬��
						//����ʾ�û����������˵����ٴ�ȷ���ύ
						final Table destTbl = mReqOrder.destTbl;
						new AlertDialog.Builder(OrderActivity.this)
							.setTitle("��ʾ")
							.setMessage(mReqOrder.getDestTbl().getAliasId() + "�Ų�̨���˵���Ϣ�Ѿ����£��ѵ����Ϣ��ˢ�£��µ����Ϣ���ᱣ��")
							.setNeutralButton("ȷ��",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,	int which){
										new QueryOrderTask(destTbl.getAliasId()).execute(WirelessOrder.foodMenu);
									}
								})
							.show();
						
					}else{
						new AlertDialog.Builder(OrderActivity.this)
							.setTitle("��ʾ")
							.setMessage(mBusinessException.getMessage())
							.setNeutralButton("ȷ��", null)
							.show();
					}
				}else{
					if(mBusinessException.getErrCode() == ErrorCode.TABLE_BUSY){
						//��������µ������ҷ����ǲ�̨�Ͳ͵Ĵ���״̬��
						//����ʾ�û����������˵����ٴ�ȷ���ύ
						final Table destTbl = mReqOrder.destTbl;
						new AlertDialog.Builder(OrderActivity.this)
							.setTitle("��ʾ")
							.setMessage(mReqOrder.getDestTbl().getAliasId() + "�Ų�̨���˵���Ϣ�Ѿ����£��ѵ����Ϣ��ˢ�£��µ����Ϣ���ᱣ��")
							.setNeutralButton("ȷ��",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,	int which){
										new QueryOrderTask(destTbl.getAliasId()).execute(WirelessOrder.foodMenu);
									}
								})
							.show();
					}else{
						new AlertDialog.Builder(OrderActivity.this)
							.setTitle("��ʾ")
							.setMessage(mBusinessException.getMessage())
							.setNeutralButton("ȷ��", null)
							.show();
					}
				}
			}	
		}
		
		
	}
	
	/*
	 * ִ�������Ӧ��̨���˵���Ϣ 
	 */
	private class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{
		private ProgressDialog mProgressDialog;

		QueryOrderTask(int tableAlias){
			super(tableAlias);
			mProgressDialog = ProgressDialog.show(OrderActivity.this,"", "���ڶ�ȡ�˵������Ժ�", true);
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�����Ǩ�Ƶ��ĵ�ҳ��
		 */
		@Override
		protected void onPostExecute(Order order){
			mProgressDialog.dismiss();
			if(mBusinessException != null){
				if(mBusinessException.errCode != 12){
					new AlertDialog.Builder(OrderActivity.this).setTitle("�����˵�ʧ��")
					.setMessage(mBusinessException.getMessage())
					.setPositiveButton("ˢ��", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new QueryOrderTask(mTblAlias).execute(WirelessOrder.foodMenu);
						}
					})
					.setNegativeButton("�˳�", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).show();
				}
			}else{
				
				mOriOrder = order;
				
				mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
				/*
				 * �����˵��ɹ��������صĿؼ�
				 */
				//set date source to original food list view
				
				//set the table ID
				((EditText)findViewById(R.id.editText_orderActivity_tableNum)).setText(Integer.toString(mOriOrder.getDestTbl().getAliasId()));
				//set the amount of customer
				((EditText)findViewById(R.id.editText_orderActivity_customerNum)).setText(Integer.toString(mOriOrder.getCustomNum()));	
				//���¹����Ʒ
				new QuerySellOutTask().execute(WirelessOrder.foodMenu.foods);
			}			
		}		
	}
	
	
	/*
	 * ������¹����Ʒ
	 */
	private class QuerySellOutTask extends com.wireless.lib.task.QuerySellOutTask{
		@Override
		protected void onPostExecute(Food[] sellOutFoods){
			if(mErrMsg != null){
				Toast.makeText(OrderActivity.this, "�����Ʒ����ʧ��", Toast.LENGTH_SHORT).show();				
			}else{
				Toast.makeText(OrderActivity.this, "�����Ʒ���³ɹ�", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onAmountChange(OrderFood food, boolean isOriFood) {
		//�µ���У������Ʒ����Ϊ��ģ���ɾ��
		if(!isOriFood && food.getCount() <= 0){
			mNewFoodList.remove(food);
		}
		
		mFoodListHandler.sendEmptyMessage(MSG_REFRESH_LIST);
	}
}

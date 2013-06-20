package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pack.Type;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.tasteMgr.TasteGroup;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.PickFoodActivity;
import com.wireless.ui.PickTasteActivity;
import com.wireless.ui.R;
import com.wireless.ui.dialog.AskCancelAmountDialog;
import com.wireless.ui.dialog.AskCancelAmountDialog.OnCancelAmountChangedListener;
import com.wireless.ui.dialog.AskPwdDialog;
import com.wireless.ui.dialog.SetOrderAmountDialog;
import com.wireless.ui.dialog.SetOrderAmountDialog.OnAmountChangedListener;

public class OrderFoodFragment extends Fragment implements OnCancelAmountChangedListener,
														   OnAmountChangedListener{

	public final static int PICK_FOOD = 0;
	public final static int PICK_TASTE = 1;
	public final static int PICK_ALL_FOOD_TASTE = 2;
	
	public final static String TAG = "OrderFoodFragment";
	
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
	
	private QueryOrderTask mQueryOrderTask;
	
	private boolean isHangUp = false;
	
	private FoodListHandler mFoodListHandler;
	
	//ȫ����ע�Ŀ�ζ
	private List<Taste> mAllFoodTastes = new ArrayList<Taste>();
	
	//�µ��Ʒ��Ϣ
	private List<OrderFood> mNewFoodList = new ArrayList<OrderFood>();
	
	//�ѵ��Ʒ��Ϣ
	private Order mOriOrder;
	
	private final static String TBL_ALIAS_KEY = "TableAliasKey";

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
		
		private WeakReference<OrderFoodFragment> mFragment;
		private ExpandableListView mListView;

		FoodListHandler(OrderFoodFragment fragment) {
			mFragment = new WeakReference<OrderFoodFragment>(fragment);
			
			mListView = (ExpandableListView) fragment.getView().findViewById(R.id.expandableListView_orderActivity);
		}

		@Override
		public void handleMessage(Message msg) {
			OrderFoodFragment ofFgm = mFragment.get();
			
			List<Map<String, ?>> groupData = new ArrayList<Map<String, ?>>();
			List<List<Map<String, ?>>> childData =  new ArrayList<List<Map<String, ?>>>();
			
			HashMap<String, Object> groupMap = new HashMap<String, Object>();
			groupMap.put(ITEM_GROUP_NAME, "�µ��");
			groupData.add(groupMap);
			
			List<Map<String, ?>> newFoodDatas = new ArrayList<Map<String,?>>();
			if(!ofFgm.mNewFoodList.isEmpty()){
				for(OrderFood f : ofFgm.mNewFoodList){
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(ITEM_FOOD_NAME, f.getName());
					map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
					map.put(ITEM_FOOD_SUM_PRICE, NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(f.calcPriceWithTaste()));
					map.put(ITEM_FOOD_TASTE, f.hasTaste() ? f.getTasteGroup().getTastePref() : TasteGroup.NO_TASTE_PREF);
					map.put(ITEM_THE_FOOD, f);
					newFoodDatas.add(map);
				}
			}
			childData.add(newFoodDatas);

			//������ѵ��
			if(ofFgm.mOriOrder != null && ofFgm.mOriOrder.hasOrderFood()){
				
				groupMap = new HashMap<String, Object>();
				groupMap.put(ITEM_GROUP_NAME, "�ѵ��");
				groupMap.put(ITEM_IS_ORI_FOOD, true);
				groupData.add(groupMap);
				
				List<Map<String, ?>> pickedFoodDatas = new ArrayList<Map<String, ?>>();
				for(OrderFood f : ofFgm.mOriOrder.getOrderFoods()){
					if(f.getCount() != 0f){
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put(ITEM_IS_ORI_FOOD, true);
						map.put(ITEM_FOOD_NAME, f.getName());
						map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
						map.put(ITEM_FOOD_SUM_PRICE, NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(f.calcPriceWithTaste()));
						map.put(ITEM_FOOD_TASTE, f.hasTaste() ? f.getTasteGroup().getTastePref() : TasteGroup.NO_TASTE_PREF);
						map.put(ITEM_THE_FOOD, f);
						pickedFoodDatas.add(map);
					}
					
					if(f.getDelta() > 0f){
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put(ITEM_IS_ORI_FOOD, true);
						map.put(ITEM_FOOD_NAME, f.getName()); 
						map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
						map.put(ITEM_FOOD_SUM_PRICE, NumericUtil.float2String2(f.calcPriceWithTaste()));
						map.put(ITEM_FOOD_TASTE, f.hasCancelReason() ? f.getCancelReason().getReason() : "û���˲�ԭ��");
						map.put(ITEM_THE_FOOD, f);
						map.put(ITEM_IS_OFFSET, true);
						map.put(ITEM_FOOD_OFFSET, NumericUtil.float2String2(f.getDelta()));
						pickedFoodDatas.add(map);
					}
				}
				childData.add(pickedFoodDatas);
				
			}
			
			FoodExpandableAdapter adapter = ofFgm.new FoodExpandableAdapter(ofFgm.getActivity(), 
					groupData, R.layout.dropgrounpitem, GROUP_ITEM_TAGS, GROUP_ITEM_ID, 
					childData, R.layout.order_activity_child_item, ITEM_TAGS, ITEM_TARGETS);
			
			mListView.setAdapter(adapter);
			
			for(int i = 0; i < groupData.size(); i++){
				mListView.expandGroup(i);
			}
			
			calcTotal();
		}
		
		private void calcTotal(){
			OrderFoodFragment fragment = mFragment.get();
			
			float totalPrice = 0;
			if(!fragment.mNewFoodList.isEmpty()){
				totalPrice += new Order(fragment.mNewFoodList).calcTotalPrice();
				((TextView) fragment.getView().findViewById(R.id.textView_orderActivity_newCount)).setText(String.valueOf(fragment.mNewFoodList.size()));
			}
			if(fragment.mOriOrder != null && fragment.mOriOrder.hasOrderFood()){
				totalPrice += fragment.mOriOrder.calcTotalPrice();
				((TextView) fragment.getView().findViewById(R.id.textView_orderActivity_pickedCount)).setText(String.valueOf(fragment.mOriOrder.getOrderFoods().size()));
			}
			
			((TextView) fragment.getView().findViewById(R.id.textView_orderActivity_sumPirce)).setText(NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(totalPrice));
		}
	}
	
		private List<? extends Map<String, ?>> mGroupData;
		private class FoodExpandableAdapter extends SimpleExpandableListAdapter{
		private List<? extends List<? extends Map<String, ?>>> mChildData;
		private PopupWindow mPopup;

		public FoodExpandableAdapter(Context context,
									 List<? extends Map<String, ?>> groupData, int groupLayout,	String[] groupFrom, int[] groupTo,
									 List<? extends List<? extends Map<String, ?>>> childData, int childLayout, String[] childFrom, int[] childTo) {
			super(context, 
				  groupData, groupLayout, groupFrom, groupTo, 
				  childData, childLayout, childFrom, childTo);
			mGroupData = groupData;
			mChildData = childData;
		}
		
		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			View layout = super.getChildView(groupPosition, childPosition, isLastChild,	convertView, parent);
			Map<String, ?> map = mChildData.get(groupPosition).get(childPosition);
			final OrderFood food = (OrderFood) map.get(ITEM_THE_FOOD);
			layout.setTag(map);
			
			//show the name to each food
			String status = "";
			if(food.asFood().isSpecial()){
				status = "��";
			}
			if(food.asFood().isRecommend()){
				if(status.length() == 0){
					status = "��";
				}else{
					status = status + ",��";
				}
			}
			if(food.asFood().isGift()){
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
			if(food.isTemp()){
				tempStatus = "(��)";
			}else{
				tempStatus = "";
			}
			
			String hangStatus = null;
			if(food.isHangup()){
				hangStatus = "��";
			}else{
				hangStatus = "";
			}
			if(hangStatus.length() != 0){
				hangStatus = "(" + hangStatus + ")";
			}
			
			String hurriedStatus = null;
			if(food.isHurried()){
				hurriedStatus = "(��)";
			}else{
				hurriedStatus = "";
			}
			
			String comboStatus = null;
			if(food.asFood().isCombo()){
				comboStatus = "(��)";
			}else{
				comboStatus = "";
			}
			
			((TextView) layout.findViewById(R.id.foodname)).setText(comboStatus + tempStatus + hangStatus + hurriedStatus + food.getName() + status);
			
			//������µ��
			if(!map.containsKey(ITEM_IS_ORI_FOOD)){
				//"ɾ��"����			 
				ImageView delFoodImgView = (ImageView)layout.findViewById(R.id.deletefood);
				delFoodImgView.setTag(food);
				delFoodImgView.setBackgroundResource(R.drawable.delete_selector);
				
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(View v) {
						//TODO
						OrderFood food = (OrderFood) v.getTag();
						mNewFoodList.remove(food);
						Intent intent = new Intent(getActivity(), PickTasteActivity.class);
						Bundle bundle = new Bundle(); 
						bundle.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(food));
						intent.putExtras(bundle);
						startActivityForResult(intent, PICK_TASTE);
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
						SetOrderAmountDialog.newInstance((OrderFood)v.getTag(), getId()).show(getFragmentManager(), SetOrderAmountDialog.TAG);
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
					
					((TextView) layout.findViewById(R.id.accountvalue)).setText(NumericUtil.float2String2(food.getDelta()));
					layout.findViewById(R.id.view_OrderFoodListView_childItem).setVisibility(View.VISIBLE);
					//ȡ���˲˰�ť
					restoreBtn.setVisibility(View.VISIBLE); 
					restoreBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								food.addCount(food.getDelta());		
								food.setCancelReason(null);
								mFoodListHandler.sendEmptyMessage(0);
							} catch (BusinessException e) {
								Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
							}
						}
					});
				//�����˲�
				} else {
					cancelFoodImgView.setVisibility(View.VISIBLE);
					addTasteImgView.setVisibility(View.VISIBLE);
					
					restoreBtn.setVisibility(View.INVISIBLE);
					//show the order amount to each food
					((TextView) layout.findViewById(R.id.accountvalue)).setText(NumericUtil.float2String2(food.getCount()));
					layout.findViewById(R.id.view_OrderFoodListView_childItem).setVisibility(View.INVISIBLE);
					//"�˲�"����
					cancelFoodImgView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							if(WirelessOrder.restaurant.hasPwd5()){
								/**
								 * ��ʾ�˲�Ȩ�����룬��֤ͨ�����������ʾɾ������Dialog
								 */
								new AskPwdDialog(getActivity(), AskPwdDialog.PWD_5){
									@Override
									protected void onPwdPass(Context context){
										dismiss();
										AskCancelAmountDialog.newInstance(food, getId()).show(getFragmentManager(), AskCancelAmountDialog.TAG);
									}
								}.show();
							}else{
								AskCancelAmountDialog.newInstance(food, getId()).show(getFragmentManager(), AskCancelAmountDialog.TAG);
							}
						}
					});
					//"�߲�"����
					addTasteImgView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							if(food.isHurried()){
								food.setHurried(false);
								Toast.makeText(getActivity(), "ȡ���߲˳ɹ�", Toast.LENGTH_SHORT).show();
								mFoodListHandler.sendEmptyMessage(0);
							}else{
								food.setHurried(true);
								Toast.makeText(getActivity(), "�߲˳ɹ�", Toast.LENGTH_SHORT).show();	
								mFoodListHandler.sendEmptyMessage(0);
							}			
						}
					}); 
				}
			}
			return layout;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,	View convertView, ViewGroup parent) {
			View layout = super.getGroupView(groupPosition, isExpanded, convertView, parent);
			Map<String, ?> map = mGroupData.get(groupPosition);
			
			if(map.containsKey(ITEM_IS_ORI_FOOD)){
				
				/**
				 * �ѵ�˵�Group����Ҫ��ʾButton
				 */
				layout.findViewById(R.id.button_orderActivity_opera).setVisibility(View.GONE);
				((ImageView)layout.findViewById(R.id.orderimage)).setVisibility(View.INVISIBLE);
				((ImageView) layout.findViewById(R.id.operateimage)).setVisibility(View.INVISIBLE);
				
			}else{

				/**
				 * �µ�˵�Group��ʾ"���"��"ȫ��"Button
				 */
				//���Button
				ImageView orderImg = (ImageView)layout.findViewById(R.id.orderimage);
				orderImg.setVisibility(View.VISIBLE);
				orderImg.setBackgroundResource(R.drawable.order_selector);
				
				orderImg.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						// ��ת��ѡ��Activity
						Intent intent = new Intent(getActivity(), PickFoodActivity.class);
						startActivityForResult(intent, PICK_FOOD);
					}
				});
				
				if(mPopup == null){
					View popupLayout = getActivity().getLayoutInflater().inflate(R.layout.order_activity_operate_popup, null);
					mPopup = new PopupWindow(popupLayout, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					mPopup.setOutsideTouchable(true);
					mPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_small));
					mPopup.update();
					//ȫ������ť
					Button hangUpBtn = (Button) popupLayout.findViewById(R.id.button_orderActivity_operate_popup_callUp);
					if(isHangUp){
						hangUpBtn.setText("ȡ������");
					} else{
						hangUpBtn.setText("����");
					}
					
					hangUpBtn.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							if(isHangUp){
								for(int i = 0; i < mNewFoodList.size(); i++){
									mNewFoodList.get(i).setHangup(false);
								}
								isHangUp = false; 
								mPopup.dismiss();
								mFoodListHandler.sendEmptyMessage(0);
							}
							else if(mNewFoodList.size() > 0){
								new AlertDialog.Builder(getActivity())
									.setTitle("��ʾ")
									.setMessage("ȷ��ȫ��������?")
									.setNeutralButton("ȷ��", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog,	int which){
												for(int i = 0; i < mNewFoodList.size(); i++){
													mNewFoodList.get(i).setHangup(true);
												}
												isHangUp = true;
												mFoodListHandler.sendEmptyMessage(0);
												mPopup.dismiss();
											}
										})
										.setNegativeButton("ȡ��", null)
										.show();	
							}	
							else {
								Toast.makeText(getActivity(), "û���µ�ˣ��޷�����", Toast.LENGTH_SHORT).show();
							}
						}
					});
					
					//ȫ����ע
					Button allRemarkBtn = (Button) popupLayout.findViewById(R.id.button_orderActivity_operate_popup_remark);
					allRemarkBtn.setText("��ע");
					allRemarkBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if(!mNewFoodList.isEmpty()){
								Intent intent = new Intent(getActivity(), PickTasteActivity.class);
								Bundle bundle = new Bundle(); 
								OrderFood dummyFood = new OrderFood();
								dummyFood.asFood().setName("ȫ����ע");
								dummyFood.makeTasteGroup(mAllFoodTastes, null);
								bundle.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(dummyFood));
								bundle.putString(PickTasteActivity.INIT_TAG, PickTasteActivity.TAG_TASTE);
								bundle.putBoolean(PickTasteActivity.PICK_ALL_ORDER_TASTE, true);
								intent.putExtras(bundle);
								startActivityForResult(intent, PICK_ALL_FOOD_TASTE);
								mPopup.dismiss();
							} else {
								Toast.makeText(getActivity(), "�˲�̨��δ��ˣ��޷���ӱ�ע", Toast.LENGTH_SHORT).show();
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
			return layout;
		}
		
	}
	
	public static OrderFoodFragment newInstance(int tableAlias){
		OrderFoodFragment fgm = new OrderFoodFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(TBL_ALIAS_KEY, tableAlias);
		fgm.setArguments(bundle);
		return fgm;
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		final View view = inflater.inflate(R.layout.order_food_activity, null);
		
		/*
		 * "����"Button
		 */
		TextView title = (TextView)view.findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("�˵�");

		TextView left = (TextView)view.findViewById(R.id.textView_left);
		left.setText("����");
		left.setVisibility(View.VISIBLE);

		ImageButton backBtn = (ImageButton)view.findViewById(R.id.btn_left);
		backBtn.setVisibility(View.VISIBLE);
		backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getActivity().onBackPressed();
			}
		});
		
		//set the table No
		final EditText tblNoEditTxt = ((EditText)view.findViewById(R.id.editText_orderActivity_tableNum));
		tblNoEditTxt.setText(Integer.toString(getArguments().getInt(TBL_ALIAS_KEY)));
		//set the default customer to 1
		((EditText)view.findViewById(R.id.editText_orderActivity_customerNum)).setText("1");
		
		TextView rightTxtView = (TextView)view.findViewById(R.id.textView_right);
		rightTxtView.setText("�ύ");
		rightTxtView.setVisibility(View.VISIBLE);
		
		/*
		 * �µ�"�ύ"Button
		 */
		ImageButton commitBtn = (ImageButton)view.findViewById(R.id.btn_right);
		commitBtn.setVisibility(View.VISIBLE);
		commitBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//�µ��߼�
				String tableIdString = tblNoEditTxt.getText().toString();
				
				//�����̨�ǿ��������������ʾ
				if(tableIdString.trim().length() != 0){
						
					int tableAlias = Integer.parseInt(tableIdString);
					
					int customNum;
					String custNumString = ((EditText)view.findViewById(R.id.editText_orderActivity_customerNum)).getText().toString();
					//�������Ϊ�գ���Ĭ��Ϊ1
					if(custNumString.length() != 0){
						customNum = Integer.parseInt(custNumString);
					}else{
						customNum = 1;
					}
					
					//�ĵ�
					if(mOriOrder != null){
						Order reqOrder = new Order(mOriOrder.getOrderFoods());
						
						reqOrder.setId(mOriOrder.getId());
						reqOrder.setOrderDate(mOriOrder.getOrderDate());
						reqOrder.setCustomNum(customNum);
						reqOrder.setDestTbl(new Table(tableAlias));
						
						//������µ�ˣ�����ӽ��˵�
						if(!mNewFoodList.isEmpty()){
							reqOrder.addFoods(mNewFoodList);
						}
						
						//�ж��˵��Ƿ�Ϊ�ջ�ȫ���˲�
						if(reqOrder.hasOrderFood()){
							//���ȫ���˲�����ʾ�յ�
							boolean hasOrderFood = false;
							for (OrderFood of : reqOrder.getOrderFoods()) {
								if(of.getCount() > 0f ){
									hasOrderFood = true;
									break;
								}
							}
							if(hasOrderFood){
								new CommitOrderTask(reqOrder, Type.UPDATE_ORDER).execute();
							}else{
								Toast.makeText(getActivity(), "�벻Ҫ�ύ�յ�", Toast.LENGTH_SHORT).show();									
							}
							
						} else {
							Toast.makeText(getActivity(), "����δ��ˣ������µ���", Toast.LENGTH_SHORT).show();
						}
						
					//���µ�
					}else{
						Order reqOrder = new Order(mNewFoodList, tableAlias, customNum);
						if(reqOrder.hasOrderFood()){
							new CommitOrderTask(reqOrder, Type.INSERT_ORDER).execute();
						}else{
							Toast.makeText(getActivity(), "����δ��ˣ������µ���", Toast.LENGTH_SHORT).show();
						}
					}
				} else {
					Toast.makeText(getActivity(), "��������ȷ�Ĳ�̨��", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		ExpandableListView listView = (ExpandableListView)view.findViewById(R.id.expandableListView_orderActivity);
		
		//Hide the soft keyboard if perform to scroll list.
		listView.setOnScrollListener(new OnScrollListener() {				
			@Override
			public void onScrollStateChanged(AbsListView listView, int scrollState) {
				((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(tblNoEditTxt.getWindowToken(), 0);
			}
				
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
		});	
		
		return view;
	}
	
	@Override
	public void onActivityCreated (Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		//ִ��������¹����Ʒ
		new QuerySellOutTask().execute();
		
		mFoodListHandler = new FoodListHandler(this);
		
		mQueryOrderTask = new QueryOrderTask(Integer.valueOf(getArguments().getInt(TBL_ALIAS_KEY)));
		mQueryOrderTask.execute();

		mFoodListHandler.sendEmptyMessage(0);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mQueryOrderTask.cancel(true);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode == Activity.RESULT_OK){			
			if(requestCode == OrderFoodFragment.PICK_TASTE){
				//��ζ�ı�ʱ֪ͨListView���и���
				OrderFoodParcel foodParcel = data.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
				mNewFoodList.add(foodParcel.asOrderFood());
				
			}else if(requestCode == OrderFoodFragment.PICK_FOOD){
				//ѡ�˸ı�ʱ֪ͨ�µ�˵�ListView���и���
				OrderParcel orderParcel = data.getParcelableExtra(OrderParcel.KEY_VALUE);
				mNewFoodList.addAll(orderParcel.asOrder().getOrderFoods());
				
			}else if(requestCode == PICK_ALL_FOOD_TASTE){
				//ȫ����ע�ı�ʱ���������µ�˵Ŀ�ζ
				for(Taste t : mAllFoodTastes){
					for(OrderFood of : mNewFoodList){
						if(of.hasTaste()){
							of.getTasteGroup().removeTaste(t);
						}
					}
				}
				
				mAllFoodTastes.clear();
				
				OrderFoodParcel foodParcel = data.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
				if(foodParcel.asOrderFood().hasTaste()){
					mAllFoodTastes.addAll(foodParcel.asOrderFood().getTasteGroup().getNormalTastes());
					//Ϊ�����µ����ӿ�ζ
					for(OrderFood food : mNewFoodList){
						if(food.hasTaste()){
							for(Taste taste : mAllFoodTastes){
								food.getTasteGroup().addTaste(taste);
							}
						}else{
							food.makeTasteGroup(mAllFoodTastes, null);
						}						
					}
					mFoodListHandler.sendEmptyMessage(0);
				}
			}
			mFoodListHandler.sendEmptyMessage(0);
		}
	}
	
	public boolean hasNewOrderFood(){
		return !mNewFoodList.isEmpty();
	}
	
	private class CommitOrderTask extends com.wireless.lib.task.CommitOrderTask{

		private ProgressDialog mProgressDialog;

		public CommitOrderTask(Order reqOrder, byte type) {
			super(WirelessOrder.pinGen, reqOrder, type);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = ProgressDialog.show(getActivity(), "", "��ѯ" + mReqOrder.getDestTbl().getAliasId() + "���˵���Ϣ...���Ժ�");
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mProgressDialog.cancel();
			
			if(mBusinessException == null){
				Toast.makeText(getActivity(), mReqOrder.getDestTbl().getAliasId() + "�Ų�̨�µ��ɹ�", Toast.LENGTH_SHORT).show();
				getActivity().finish();
			}else{
				if(mOriOrder != null){

					if(mBusinessException.getErrCode().equals(ProtocolError.TABLE_IDLE)){
						//����Ǹĵ������ҷ����ǲ�̨���еĴ���״̬��
						//����ʾ�û�������չ��ﳵ�е�ԭ�˵�
						new AlertDialog.Builder(getActivity())
							.setTitle("��ʾ")
							.setMessage(mReqOrder.getDestTbl().getAliasId() + "�Ų�̨�Ѿ����ʣ��ѵ����Ϣ��ˢ�£��µ����Ϣ���ᱣ��")
							.setNeutralButton("ȷ��",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,	int which){
										mOriOrder = null;
										mFoodListHandler.sendEmptyMessage(0);
									}
								})
							.show();

						
					}else if(mBusinessException.getErrCode().equals(ProtocolError.ORDER_EXPIRED)){
						//����Ǹĵ������ҷ������˵����ڵĴ���״̬��
						//����ʾ�û����������˵����ٴ�ȷ���ύ
						final Table destTbl = mReqOrder.getDestTbl();
						new AlertDialog.Builder(getActivity())
							.setTitle("��ʾ")
							.setMessage(mReqOrder.getDestTbl().getAliasId() + "�Ų�̨���˵���Ϣ�Ѿ����£��ѵ����Ϣ��ˢ�£��µ����Ϣ���ᱣ��")
							.setNeutralButton("ȷ��",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,	int which){
										mQueryOrderTask = new QueryOrderTask(destTbl.getAliasId());
										mQueryOrderTask.execute();
									}
								})
							.show();
						
					}else{
						new AlertDialog.Builder(getActivity())
							.setTitle("��ʾ")
							.setMessage(mBusinessException.getMessage())
							.setNeutralButton("ȷ��", null)
							.show();
					}
				}else{
					if(mBusinessException.getErrCode().equals(ProtocolError.TABLE_BUSY)){
						//��������µ������ҷ����ǲ�̨�Ͳ͵Ĵ���״̬��
						//����ʾ�û����������˵����ٴ�ȷ���ύ
						final Table destTbl = mReqOrder.getDestTbl();
						new AlertDialog.Builder(getActivity())
							.setTitle("��ʾ")
							.setMessage(mReqOrder.getDestTbl().getAliasId() + "�Ų�̨���˵���Ϣ�Ѿ����£��ѵ����Ϣ��ˢ�£��µ����Ϣ���ᱣ��")
							.setNeutralButton("ȷ��",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,	int which){
										mQueryOrderTask = new QueryOrderTask(destTbl.getAliasId());
										mQueryOrderTask.execute();
									}
								})
							.show();
					}else{
						new AlertDialog.Builder(getActivity())
							.setTitle("��ʾ")
							.setMessage(mBusinessException.getMessage())
							.setNeutralButton("ȷ��", null)
							.show();
					}
				}
			}	
		}
	}
	
	/**
	 * ִ�������Ӧ��̨���˵���Ϣ 
	 */
	private class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{
		private ProgressDialog mProgressDialog;

		QueryOrderTask(int tableAlias){
			super(WirelessOrder.pinGen, tableAlias, WirelessOrder.foodMenu);
			mProgressDialog = ProgressDialog.show(getActivity(), "", "���ڶ�ȡ�˵������Ժ�", true);
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�����Ǩ�Ƶ��ĵ�ҳ��
		 */
		@Override
		protected void onPostExecute(Order order){
			
			mProgressDialog.dismiss();
			
			if(mBusinessException != null){
				if(!mBusinessException.getErrCode().equals(ProtocolError.ORDER_NOT_EXIST)){
					new AlertDialog.Builder(getActivity()).setTitle("�����˵�ʧ��")
					.setMessage(mBusinessException.getMessage())
					.setPositiveButton("ˢ��", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mQueryOrderTask = new QueryOrderTask(mTblAlias);
							mQueryOrderTask.execute();
						}
					})
					.setNegativeButton("�˳�", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							getActivity().finish();
						}
					}).show();
				}
			}else{
				
				mOriOrder = order;
				
				mFoodListHandler.sendEmptyMessage(0);
				/*
				 * �����˵��ɹ��������صĿؼ�
				 */
				//set date source to original food list view
				
				//set the table ID
				((EditText)getView().findViewById(R.id.editText_orderActivity_tableNum)).setText(Integer.toString(mOriOrder.getDestTbl().getAliasId()));
				//set the amount of customer
				((EditText)getView().findViewById(R.id.editText_orderActivity_customerNum)).setText(Integer.toString(mOriOrder.getCustomNum()));	
				//���¹����Ʒ
				new QuerySellOutTask().execute();
			}			
		}		
	}
	
	/**
	 * ������¹����Ʒ
	 */
	private class QuerySellOutTask extends com.wireless.lib.task.QuerySellOutTask{
		
		QuerySellOutTask(){
			super(WirelessOrder.pinGen, WirelessOrder.foodMenu.foods);
		}
		
		@Override
		protected void onPostExecute(Food[] sellOutFoods){
			if(mProtocolException != null){
				Toast.makeText(getActivity(), "�����Ʒ����ʧ��", Toast.LENGTH_SHORT).show();				
			}else{
				Toast.makeText(getActivity(), "�����Ʒ���³ɹ�", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onCancelAmountChanged(OrderFood food) {
		mFoodListHandler.sendEmptyMessage(0);
	}
	
	@Override
	public void onAmountChanged(OrderFood food) {
		//Remove the new food if the amount is zero.
		if(food.getCount() == 0){
			mNewFoodList.remove(food);
		}
		mFoodListHandler.sendEmptyMessage(0);
	}
}


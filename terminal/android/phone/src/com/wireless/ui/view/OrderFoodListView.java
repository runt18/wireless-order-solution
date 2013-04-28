package com.wireless.ui.view;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.excep.ProtocolException;
import com.wireless.pack.Type;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.protocol.TasteGroup;
import com.wireless.ui.R;
import com.wireless.ui.dialog.AskPwdDialog;

public class OrderFoodListView extends ExpandableListView{

	public final static int PICK_TASTE = 1;
	public final static int PICK_FOOD = 2;
	private static final String KEY_THE_FOOD = "keyTheFood";
	private static final String KEY_IS_OFFSET = "keyIsOffset";
	
	private OnOperListener mOperListener;
	private OnChangedListener mChgListener;
//	private OrderFood[] mSrcFoods;
	private Order mTmpOrder = new Order();
	private int mSelectedPos;
	private byte mType = Type.INSERT_ORDER;
	private BaseExpandableListAdapter mAdapter;
	private ArrayList<HashMap<String,Object>> mFoodsWithOffset = new ArrayList<HashMap<String,Object>>();
	private AllMarkClickListener mAllMarkClickListener;
	private Taste[] mOldAllTastes;

	public OrderFoodListView(Context context, AttributeSet attrs){
		super(context, attrs);
		/**
		 * 选择每个菜品的操作
		 */
		setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				@SuppressWarnings("unchecked")
				HashMap<String,Object> map = (HashMap<String, Object>) v.getTag();
				if(map.containsKey(KEY_IS_OFFSET)){
					return true;
				} else if(mType == Type.INSERT_ORDER){
					mSelectedPos = childPosition;
					new ExtOperDialg(mTmpOrder.getOrderFoods()[childPosition]).show();
					return true;
					
				}else if(mType == Type.UPDATE_ORDER){
					mSelectedPos = childPosition;
					new ExtOperDialg(mTmpOrder.getOrderFoods()[childPosition]).show();
					return true;
					
				}else{
					return false;
				}
			}
		});
	}
	
	/**
	 * 设置菜品操作的回调接口
	 * @param operListener
	 */
	public void setOperListener(OnOperListener operListener){
		mOperListener = operListener;
	}
	     
	/**
	 * 设置数据源变化的回调接口
	 * @param chgListener
	 */
	public void setChangedListener(OnChangedListener chgListener){
		mChgListener = chgListener;
	}
	
	public void addFood(OrderFood foodToAdd) throws ProtocolException{
		mTmpOrder.addFood(foodToAdd);
		refreshOffsetFoods(mTmpOrder.getOrderFoods());
		notifyDataChanged();
	}
	
	public void addFoods(OrderFood[] foodsToAdd){
		mTmpOrder.addFoods(foodsToAdd);
		refreshOffsetFoods(mTmpOrder.getOrderFoods());
		notifyDataChanged();
	}
	
	/**
	 * 此函数用于更新选中的菜品，比如"口味"操作，需要从TasteActivity将结果回传ListView，
	 * 这种情况就调用此函数来更新选中的菜品
	 * @param food
	 */
	public void setFood(OrderFood foodToSet){
		if(foodToSet != null && mAdapter != null){
			mTmpOrder.getOrderFoods()[mSelectedPos] = foodToSet;
			
			refreshOffsetFoods(mTmpOrder.getOrderFoods());
			mAdapter.notifyDataSetChanged();
		
		}else{
			throw new NullPointerException();
		}
	}
	
	public void setFoods(OrderFood[] foods){
		mTmpOrder.setOrderFoods(foods);
		refreshOffsetFoods(foods);
		notifyDataChanged();
	}
	
	private void refreshOffsetFoods(OrderFood[] foods){
		mFoodsWithOffset = new ArrayList<HashMap<String,Object>>();
		
		for(OrderFood food : foods)
		{
			if(food.getCount() > 0f){
				HashMap<String,Object> foodMap = new HashMap<String,Object>();
				foodMap.put(KEY_THE_FOOD, food);
				mFoodsWithOffset.add(foodMap);
			}
			if(food.getDelta() > 0f && mType == Type.UPDATE_ORDER)
			{
				HashMap<String,Object> offsetFoodMap = new HashMap<String,Object>();
				offsetFoodMap.put(KEY_THE_FOOD, food);
				offsetFoodMap.put(KEY_IS_OFFSET, true);
				mFoodsWithOffset.add(offsetFoodMap);
			}
		}
	}
	
	public void setAllTaste(Taste[] tastes){
		//为所有新点菜和已点菜添加口味
		for(HashMap<String, Object> map:mFoodsWithOffset){
			OrderFood food = (OrderFood) map.get(KEY_THE_FOOD);
			if(!food.hasTaste()){
				food.makeTasetGroup(tastes, null);
			}
			for(Taste taste: tastes){
				food.getTasteGroup().addTaste(taste);
			}
			
		}
		mAdapter.notifyDataSetChanged();
		mOldAllTastes = tastes;
	}
	/**
	 * 初始化控件
	 * @param type
	 * 			One of values blew.<br>
	 * 			Type.INSERT_ORDER - 新点菜
	 * 			Type.UPDATE_ORDER - 已点菜
	 */
	public void init(int type){
		if(type == Type.INSERT_ORDER){
			mType = Type.INSERT_ORDER;
		}else if(type == Type.UPDATE_ORDER){
			mType = Type.UPDATE_ORDER;
		}else{
			throw new IllegalArgumentException("The type is NOT valid.");
		}
		
		if(mType == Type.INSERT_ORDER){
			mAdapter = new Adapter("新点菜"){
				@Override
				public void notifyDataSetChanged(){
					trim();
					super.notifyDataSetChanged();
					if(mChgListener != null){
						mChgListener.onSourceChanged();
					}
				}
			};
		}else{
			mAdapter = new Adapter("已点菜"){
				@Override
				public void notifyDataSetChanged(){
					trim();
					super.notifyDataSetChanged();
					if(mChgListener != null){
						mChgListener.onSourceChanged();
					}
				}						
			};
		}
		setAdapter(mAdapter);	
		if(mChgListener != null){
			mChgListener.onSourceChanged();
		}
		
	}
	
	/**
	 * 取得List中的数据源（就是菜品的List信息）
	 * @return
	 * 		OrderFood的List
	 */
	public OrderFood[] getSourceData(){
		return mTmpOrder.getOrderFoods();
	}
	
	/**
	 * 在source data变化的时候，调用此函数来更新ListView的数据。
	 */
	private void notifyDataChanged(){
		if(mAdapter != null){
			mAdapter.notifyDataSetChanged();
		}
	}

	private void trim(){
		HashMap<OrderFood, OrderFood> foodMap = new HashMap<OrderFood, OrderFood>();
		for(OrderFood food : mTmpOrder.getOrderFoods()){
			if(foodMap.containsKey(food)){
				float amount = foodMap.get(food).getCount() + food.getCount();
				food.setCount((float)Math.round(amount * 100) / 100);
				foodMap.put(food, food);
			}else{
				foodMap.put(food, food);
			}			
		}
		if(mTmpOrder.getOrderFoods().length != foodMap.size()){
			mTmpOrder.setOrderFoods(foodMap.values().toArray(new OrderFood[foodMap.values().size()]));
		}
	}

	private class Adapter extends BaseExpandableListAdapter{

		private String mGroupTitle;
		private PopupWindow mPopup;
		private boolean isHangUp = false;
		
		public Adapter(String groupTitle){
			mGroupTitle = groupTitle;
		}
		
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return mFoodsWithOffset.get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			View view;
			if(convertView == null){
				view = View.inflate(getContext(), R.layout.order_activity_child_item, null);
			}else{
				view = convertView;
			}
			
			HashMap<String,Object> foodMap = mFoodsWithOffset.get(childPosition);
			final OrderFood food = (OrderFood) foodMap.get(KEY_THE_FOOD);
			view.setTag(foodMap);
			//show the name to each food
			String status = "";
			if(food.isSpecial()){
				status = "特";
			}
			if(food.isRecommend()){
				if(status.length() == 0){
					status = "荐";
				}else{
					status = status + ",荐";
				}
			}
			if(food.isGift()){
				if(status.length() == 0){
					status = "赠";
				}else{
					status = status + ",赠";
				}
			}
			if(status.length() != 0){
				status = "(" + status + ")";
			}
			
			String tempStatus = null;
			if(food.isTemp()){
				tempStatus = "(临)";
			}else{
				tempStatus = "";
			}
			
			String hangStatus = null;
			if(food.isHangup()){
				hangStatus = "叫";
			}else{
				hangStatus = "";
			}
			if(hangStatus.length() != 0){
				hangStatus = "(" + hangStatus + ")";
			}
			
			String hurriedStatus = null;
			if(food.isHurried()){
				hurriedStatus = "(催)";
			}else{
				hurriedStatus = "";
			}
			
			String comboStatus = null;
			if(food.isCombo()){
				comboStatus = "(套)";
			}else{
				comboStatus = "";
			}
			
			((TextView) view.findViewById(R.id.foodname)).setText(comboStatus + tempStatus + hangStatus + hurriedStatus + food.getName() + status);
			
			Button restoreBtn = (Button) view.findViewById(R.id.button_orderFoodListView_childItem_restore);
			//根据是否是退菜来显示
			if(foodMap.containsKey(KEY_IS_OFFSET)){ 
				((TextView) view.findViewById(R.id.accountvalue)).setText(NumericUtil.float2String2(food.getDelta()));
				view.findViewById(R.id.view_OrderFoodListView_childItem).setVisibility(View.VISIBLE);
				//取消退菜按钮
				restoreBtn.setVisibility(View.VISIBLE);
				restoreBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						HashMap<String,Object> map =  mFoodsWithOffset.get(childPosition);
						OrderFood food = (OrderFood)map.get(KEY_THE_FOOD);
						try {
							food.addCount(food.getDelta());						
							refreshOffsetFoods(mTmpOrder.getOrderFoods());
							mAdapter.notifyDataSetChanged();
						} catch (ProtocolException e) {
							Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					}
				});
			} else {
				restoreBtn.setVisibility(View.INVISIBLE);
				//show the order amount to each food
				((TextView) view.findViewById(R.id.accountvalue)).setText(NumericUtil.float2String2(food.getCount()));
				view.findViewById(R.id.view_OrderFoodListView_childItem).setVisibility(View.INVISIBLE);
			}
			//show the price to each food
			((TextView) view.findViewById(R.id.pricevalue)).setText(NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(food.calcPriceWithTaste()));
			//show the taste to each food
			((TextView)view.findViewById(R.id.taste)).setText(food.hasTaste() ? food.getTasteGroup().getTastePref() : TasteGroup.NO_TASTE_PREF);
			/**
			 * "新点菜"的ListView显示"删菜"和"口味"
			 * "已点菜"的ListView显示"退菜"和"催菜"
			 */
			if(mType == Type.INSERT_ORDER){
				//"删菜"操作			 
				ImageView delFoodImgView = (ImageView)view.findViewById(R.id.deletefood);
				delFoodImgView.setBackgroundResource(R.drawable.delete_selector);
				delFoodImgView.setOnClickListener(new View.OnClickListener() {
					/**
					 * "新点菜"时直接显示删菜数量Dialog
					 */
					@Override
					public void onClick(View v) {
						HashMap<String,Object> map =  mFoodsWithOffset.get(childPosition);
						OrderFood food = (OrderFood)map.get(KEY_THE_FOOD);
						new AskCancelAmountDialog(food,true).show();
					}
				});

				//"数量"操作
				ImageView addTasteImgView = (ImageView)view.findViewById(R.id.addtaste);
				addTasteImgView.setBackgroundResource(R.drawable.amount_selector);
				addTasteImgView.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						mSelectedPos = childPosition;
						HashMap<String,Object> map =  mFoodsWithOffset.get(childPosition);
						OrderFood food = (OrderFood)map.get(KEY_THE_FOOD);
						new AskCancelAmountDialog(food,false).show();
					}
				});
				
			}else{
				ImageView cancelFoodImgView = (ImageView)view.findViewById(R.id.deletefood);
				cancelFoodImgView.setBackgroundResource(R.drawable.tuicai_selector);
				
				ImageView addTasteImgView = (ImageView)view.findViewById(R.id.addtaste);
				addTasteImgView.setBackgroundResource(R.drawable.cuicai_selector);
				
				if(foodMap.containsKey(KEY_IS_OFFSET)){
					cancelFoodImgView.setVisibility(View.INVISIBLE);
					addTasteImgView.setVisibility(View.INVISIBLE);
				} else {
					cancelFoodImgView.setVisibility(View.VISIBLE);
					addTasteImgView.setVisibility(View.VISIBLE);
					
					//"退菜"操作
					cancelFoodImgView.setOnClickListener(new View.OnClickListener() {				
						@Override 
						public void onClick(View v) {
							if(WirelessOrder.restaurant.hasPwd5()){
								/**
								 * 提示退菜权限密码，验证通过的情况下显示删菜数量Dialog
								 */
								new AskPwdDialog(getContext(), AskPwdDialog.PWD_5){
									@Override
									protected void onPwdPass(Context context){
										dismiss();
										HashMap<String,Object> map =  mFoodsWithOffset.get(childPosition);
										OrderFood food = (OrderFood)map.get(KEY_THE_FOOD);
										new AskCancelAmountDialog(food,true).show();
									}
								}.show();
							}else{
								HashMap<String,Object> map =  mFoodsWithOffset.get(childPosition);
								OrderFood food = (OrderFood)map.get(KEY_THE_FOOD);
								new AskCancelAmountDialog(food,true).show();
							}
						}
					});
					//"催菜"操作
					addTasteImgView.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							if(food.isHurried()){
								food.setHurried(false);
								Toast.makeText(getContext(), "取消催菜成功", Toast.LENGTH_SHORT).show();
								mAdapter.notifyDataSetChanged();
					
							}else{
								food.setHurried(true);
								Toast.makeText(getContext(), "催菜成功", Toast.LENGTH_SHORT).show();	
								mAdapter.notifyDataSetChanged();
							}			
						}
					}); 
				}
			}
			return view;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mFoodsWithOffset.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mGroupTitle;
		}

		@Override
		public int getGroupCount() {
			return 1;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			View layout;
			if(convertView == null){
				layout = View.inflate(getContext(), R.layout.dropgrounpitem, null);
			}else{
				layout = convertView;
			}
			
			((TextView)layout.findViewById(R.id.grounname)).setText(mGroupTitle);
			
			/**
			 * "新点菜"的Group显示"点菜"Button
			 */
			if(mType == Type.INSERT_ORDER){
				
				if(mPopup == null){
					View popupLayout = View.inflate(getContext(), R.layout.order_activity_operate_popup, null);
					mPopup = new PopupWindow(popupLayout, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					mPopup.setOutsideTouchable(true);
					mPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_small));
					mPopup.update();
					//全单叫起按钮
					Button hangUpBtn = (Button) popupLayout.findViewById(R.id.button_orderActivity_operate_popup_callUp);
					hangUpBtn.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							if(isHangUp){
								for(HashMap<String, Object> map : mFoodsWithOffset){
									OrderFood food = (OrderFood) map.get(KEY_THE_FOOD);
									if(food.isHangup()){
										food.toggleHangup();
									}
								}
								isHangUp = false; 
								mPopup.dismiss();
								mAdapter.notifyDataSetChanged();
							}
							else if(mFoodsWithOffset.size() > 0){
								new AlertDialog.Builder(getContext())
									.setTitle("提示")
									.setMessage("确定全单叫起吗?")
									.setNeutralButton("确定", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog,	int which){
												for(HashMap<String, Object> map:mFoodsWithOffset){
													OrderFood food = (OrderFood) map.get(KEY_THE_FOOD);
													if(!food.isHangup()){
														food.toggleHangup();
													}
												}
												isHangUp = true;
												mAdapter.notifyDataSetChanged();
												mPopup.dismiss();
											}
										})
										.setNegativeButton("取消", null)
										.show();	
							}	
							else {
								Toast.makeText(getContext(), "没有新点菜，无法叫起", Toast.LENGTH_SHORT).show();
							}
						}
					});
					//全单备注
					Button allRemarkBtn = (Button) popupLayout.findViewById(R.id.button_orderActivity_operate_popup_remark);
					allRemarkBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if(mOldAllTastes != null){
								for(HashMap<String, Object> map:mFoodsWithOffset){
									OrderFood food = (OrderFood) map.get(KEY_THE_FOOD);
									if(food.hasNormalTaste()){
										for(Taste t:mOldAllTastes){
											food.getTasteGroup().removeTaste(t); 
										}
									}
								}
								mOldAllTastes = null;
								mAdapter.notifyDataSetChanged();
								mPopup.dismiss();
							}
							else if(!mFoodsWithOffset.isEmpty()){
								if(mAllMarkClickListener != null)
								{
									mAllMarkClickListener.allMarkClick();
								}
//								Intent intent = new Intent(getContext(), PickTasteActivity.class);
//								Bundle bundle = new Bundle(); 
//								OrderFood dummyFood = new OrderFood();
//								dummyFood.name = "全单备注";
//								bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(dummyFood));
//								bundle.putString(PickTasteActivity.INIT_TAG, PickTasteActivity.TAG_TASTE);
//								bundle.putBoolean(PickTasteActivity.PICK_ALL_ORDER_TASTE, true);
//								intent.putExtras(bundle);
//								startActivityForResult(intent, OrderActivity.ALL_ORDER_REMARK);
								mPopup.dismiss();
							} else {
								Toast.makeText(getContext(), "此餐台还未点菜，无法添加备注", Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
				
				View orderOperateBtn = layout.findViewById(R.id.button_orderActivity_opera);
				orderOperateBtn.setVisibility(View.VISIBLE);
				orderOperateBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Button allRemarkBtn = (Button) mPopup.getContentView().findViewById(R.id.button_orderActivity_operate_popup_remark);
						if(mOldAllTastes != null)
							allRemarkBtn.setText("取消备注");
						else allRemarkBtn.setText("备注");
						
						Button hangUpBtn = (Button) mPopup.getContentView().findViewById(R.id.button_orderActivity_operate_popup_callUp);
						if(isHangUp ){
							hangUpBtn.setText("取消叫起");
						} else hangUpBtn.setText("叫起");
						
						mPopup.showAsDropDown(v);
					}
				});
				
				/**
				 * 点击点菜按钮
				 */
				ImageView orderImg = (ImageView)layout.findViewById(R.id.orderimage);
				orderImg.setBackgroundResource(R.drawable.order_selector);
				
				orderImg.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						if(mOperListener != null){
							mOperListener.onPickFood();
						}
					}
				});
				/**
				 * 点击全单叫起按钮
				 */
//				ImageView hurriedImgView = (ImageView)layout.findViewById(R.id.operateimage);
//				hurriedImgView.setVisibility(View.GONE);
//				hurriedImgView.setBackgroundResource(R.drawable.jiaoqi_selector);
//				
//				hurriedImgView.setOnClickListener(new View.OnClickListener() {				
//					@Override
//					public void onClick(View v) {						
//						if(mFoodsWithOffset.size() > 0){
//							new AlertDialog.Builder(getContext())
//								.setTitle("提示")
//								.setMessage("确定全单叫起吗?")
//								.setNeutralButton("确定", new DialogInterface.OnClickListener() {
//										@Override
//										public void onClick(DialogInterface dialog,	int which){
//											for(int i = 0; i < mFoodsWithOffset.size(); i++){
//												HashMap<String,Object> map =  mFoodsWithOffset.get(i);
//												OrderFood food = (OrderFood)map.get(KEY_THE_FOOD);
//												if(food.hangStatus == OrderFood.FOOD_NORMAL){
//													food.hangStatus = OrderFood.FOOD_HANG_UP;
//												}							
//											}
//											mAdapter.notifyDataSetChanged();
//										}
//									})
//									.setNegativeButton("取消", null)
//									.show();	
//						}						
//					}
//				});
				
			}else{
				layout.findViewById(R.id.orderimage).setVisibility(View.INVISIBLE);
			}
			
//			if(isExpanded){
//				((ImageView)view.findViewById(R.id.arrow)).setBackgroundResource(R.drawable.point);
//			}else{
//				((ImageView)view.findViewById(R.id.arrow)).setBackgroundResource(R.drawable.point02);
//			}
			return layout;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			return true;
		}
		
	}
	
	/*
	 * 提示输入删除数量的Dialog
	 */
	private class AskCancelAmountDialog extends Dialog{
	
		AskCancelAmountDialog(final OrderFood oriFood, boolean isCancel) {
			super(OrderFoodListView.this.getContext(), R.style.FullHeightDialog);
			
			View view = LayoutInflater.from(getContext()).inflate(R.layout.alert, null);
			setContentView(view);
			//getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			
			((TextView)findViewById(R.id.table)).setText("数量：");
			//删除数量默认为此菜品的点菜数量
			final EditText cancelEdtTxt = (EditText)view.findViewById(R.id.mycount);			
			cancelEdtTxt.setText(NumericUtil.float2String2(oriFood.getCount()));
			//弹出后全选
			cancelEdtTxt.selectAll();
			
			cancelEdtTxt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cancelEdtTxt.selectAll();
				}
			});
			
			//弹出软键盘
           getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); 
           InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
           imm.showSoftInput(cancelEdtTxt, 0); //显示软键盘
           imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			
			//"确定"Button
			Button okBtn = (Button)view.findViewById(R.id.confirm);
			okBtn.setText("确定");
			
			if(isCancel){
				((TextView)view.findViewById(R.id.ordername)).setText("请输入" + oriFood.getName() + "的删除数量");
				okBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						try{
							float cancelAmount = Float.parseFloat(cancelEdtTxt.getText().toString());

							oriFood.removeCount(cancelAmount);
							//新点菜中，如果菜品数量为零的，则删除
							if(mType == Type.INSERT_ORDER && oriFood.getCount() <= 0){
								mTmpOrder.remove(oriFood);
							}
							
							refreshOffsetFoods(mTmpOrder.getOrderFoods());
							mAdapter.notifyDataSetChanged();
							
							dismiss();
							
						}catch(ProtocolException e){
							Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
							
						}catch(NumberFormatException e){
							Toast.makeText(getContext(), "你输入删菜数量不正确", Toast.LENGTH_LONG).show();
						}

					}
				});
			}
			else {
				((TextView)view.findViewById(R.id.ordername)).setText("设置" + oriFood.getName() + "的数量");
				okBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						float amount = Float.parseFloat(cancelEdtTxt.getText().toString());
						if(amount == 0f){
							Toast.makeText(OrderFoodListView.this.getContext(), "输入的数量不正确", Toast.LENGTH_SHORT).show();
						} else {
							oriFood.setCount(amount);
							refreshOffsetFoods(mTmpOrder.getOrderFoods());
							mAdapter.notifyDataSetChanged();
							
							dismiss();
						}
					}
				});
			}
			
			//"取消"Button
			Button cancelBtn = (Button)view.findViewById(R.id.alert_cancel);
			cancelBtn.setText("取消");
			cancelBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		}		
	}
	
	/**
	 * 提示输入数量的Dialog
	 */
	private class AskOrderAmountDialog extends Dialog{
	
		AskOrderAmountDialog(final OrderFood selectedFood) {
			super(OrderFoodListView.this.getContext(), R.style.FullHeightDialog);
			
			View view = LayoutInflater.from(getContext()).inflate(R.layout.alert, null);
			setContentView(view);
			//getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView)view.findViewById(R.id.ordername)).setText("请输入" + selectedFood.getName() + "的数量");
			
			((TextView)findViewById(R.id.table)).setText("数量：");
			//增加数量默认为此菜品的点菜数量
			final EditText amountEdtTxt = (EditText)view.findViewById(R.id.mycount);
			amountEdtTxt.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					amountEdtTxt.selectAll();
				}
			});
			
			//"确定"Button
			Button okBtn = (Button)view.findViewById(R.id.confirm);
			okBtn.setText("确定");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try{
						float amount = Float.parseFloat(amountEdtTxt.getText().toString());
						if(amount > 255){
							Toast.makeText(getContext(), "对不起，\"" + selectedFood.toString() + "\"最多只能点255份", Toast.LENGTH_SHORT).show();
						}else{
							selectedFood.setCount(amount);
							mAdapter.notifyDataSetChanged();
							Toast.makeText(getContext(), "设置\"" + selectedFood.toString() + "\"" + "数量为" + amount + "份", Toast.LENGTH_LONG).show();
							dismiss();
						}							
						
					}catch(NumberFormatException e){
						Toast.makeText(getContext(), "您输入的数量格式不正确，请重新输入", Toast.LENGTH_SHORT).show();
					}					
				}
			});
			
			//"取消"Button
			Button cancelBtn = (Button)view.findViewById(R.id.alert_cancel);
			cancelBtn.setText("取消");
			cancelBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
			
			//弹出软键盘
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); 
	        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	        imm.showSoftInput(amountEdtTxt, 0); //显示软键盘
	        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		}		
	}
	
	/**
	 * 点击菜品列表后的扩展功能 Dialog
	 */
	private class ExtOperDialg extends Dialog{

		
		ExtOperDialg(final OrderFood selectedFood) {
			super(OrderFoodListView.this.getContext(), R.style.FullHeightDialog);
			setContentView(R.layout.item_alert);
			getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			((TextView)findViewById(R.id.ordername)).setText("请选择" + selectedFood.getName() + "的操作");
			if(mType == Type.INSERT_ORDER){
				/**
				 * 新点菜是扩展功能为"删菜"、"口味"、"叫起/取消叫起"、“数量”
				 */
				//删菜功能
				((TextView)findViewById(R.id.item1Txt)).setText("删菜");
				((RelativeLayout)findViewById(R.id.r1)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						dismiss();
						new AskCancelAmountDialog(selectedFood,true).show();							
					}
				});
				
				//口味功能
				((TextView)findViewById(R.id.item2Txt)).setText("口味");
				((RelativeLayout)findViewById(R.id.r2)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						if(mOperListener != null){
							dismiss();
							mOperListener.onPickTaste(selectedFood);
						}
					}
				});
				
				//叫起/取消叫起
				if(selectedFood.isHangup()){
					((TextView)findViewById(R.id.item3Txt)).setText("取消叫起");
				}else{
					((TextView)findViewById(R.id.item3Txt)).setText("叫起");						
				}
				((RelativeLayout)findViewById(R.id.r3)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						selectedFood.toggleHangup();
						if(selectedFood.isHangup()){
							((TextView)findViewById(R.id.item3Txt)).setText("取消叫起");
						}else{
							((TextView)findViewById(R.id.item3Txt)).setText("叫起");						
						}
						dismiss();
					}
				});
				
				//数量
				((RelativeLayout)findViewById(R.id.r4)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						dismiss();
						new AskOrderAmountDialog(selectedFood).show();							
					}
				});
				
				
			}else{
				/**
				 * 已点菜的扩展功能为"退菜"、"催菜/取消催菜"
				 */
				//退菜功能
				((TextView)findViewById(R.id.item1Txt)).setText("退菜");
				((RelativeLayout)findViewById(R.id.r1)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						dismiss();
						if(WirelessOrder.restaurant.hasPwd5()){
							new AskPwdDialog(getContext(), AskPwdDialog.PWD_5){							
								@Override
								protected void onPwdPass(Context context){
									dismiss();
									new AskCancelAmountDialog(selectedFood,true).show();
								}
							}.show();
						}else{
							new AskCancelAmountDialog(selectedFood,true).show(); 
						}
					}
				});
					
				//催菜/取消催菜功能
				if(selectedFood.isHurried()){
					((TextView)findViewById(R.id.item2Txt)).setText("取消催菜");							
				}else{
					((TextView)findViewById(R.id.item2Txt)).setText("催菜");						
				}
				((RelativeLayout)findViewById(R.id.r2)).setOnClickListener(new View.OnClickListener() {						
					@Override
					public void onClick(View arg0) {
						if(selectedFood.isHurried()){
							selectedFood.setHurried(false);
							((TextView)findViewById(R.id.item2Txt)).setText("催菜");	
							dismiss();
				
						}else{
							selectedFood.setHurried(true);
							((TextView)findViewById(R.id.item2Txt)).setText("取消催菜");	
							dismiss();
						}						
					}
				});
				
				((ImageView)findViewById(R.id.line3)).setVisibility(View.GONE);
				((ImageView)findViewById(R.id.line4)).setVisibility(View.GONE);
				((RelativeLayout)findViewById(R.id.r3)).setVisibility(View.GONE);
				((RelativeLayout)findViewById(R.id.r4)).setVisibility(View.GONE);
			}
			
			//返回Button
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
			//trim(_selectedFood);
			mAdapter.notifyDataSetChanged();
		}		
	}
	public void setAllMarkClickListener(AllMarkClickListener l){
		mAllMarkClickListener = l;
	}
	
	public static interface OnChangedListener{
		public void onSourceChanged();
	}
	
	public static interface OnOperListener{
		public void onPickTaste(OrderFood selectedFood);
		public void onPickFood();
	}
	
	public static interface AllMarkClickListener{
		void allMarkClick();
	}
	
}

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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.Params;
import com.wireless.common.ShoppingCart;
import com.wireless.common.ShoppingCart.OnCommitListener;
import com.wireless.common.WirelessOrder;
import com.wireless.excep.BusinessException;
import com.wireless.fragment.OptionBarFragment;
import com.wireless.fragment.OptionBarFragment.OnOrderChangeListener;
import com.wireless.fragment.PickTasteFragment;
import com.wireless.fragment.PickTasteFragment.OnTasteChangeListener;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Table;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Util;
import com.wireless.util.imgFetcher.ImageFetcher;

public class PickedFoodActivity extends Activity implements
		OnOrderChangeListener, OnTasteChangeListener {
	public static final int ORDER_SUBMIT_RESULT = 234551;
	// 列表项的显示标签
	private static final String ITEM_FOOD_NAME = "item_food_name";
	private static final String ITEM_FOOD_ORI_PRICE = "item_ori_food_price";
	private static final String ITEM_FOOD_SUM_PRICE = "item_new_food_price";
	private static final String ITEM_FOOD_COUNT = "item_food_count";
	private static final String ITEM_FOOD_OFFSET = "item_food_offset";
	
	private static final String ITEM_IS_OFFSET = "item_is_offset";
	private static final String ITEM_IS_ORI_FOOD = "item_is_ori_food";
	// private static final String ITEM_FOOD_STATE = "item_food_state";
	private static final String ITEM_THE_FOOD = "theFood";

	private static final String ITEM_GROUP_NAME = "itemGroupName";

	private static final String[] GROUP_ITEM_TAGS = { ITEM_GROUP_NAME };

	private static final int[] GROUP_ITEM_ID = { R.id.textView_groupName_pickedFood_list_item };

	private static final String[] ITEM_TAGS = { ITEM_FOOD_NAME,
			ITEM_FOOD_ORI_PRICE, ITEM_FOOD_COUNT, ITEM_FOOD_SUM_PRICE
	// ITEM_FOOD_STATE
	};

	private static final int[] ITEM_ID = { R.id.textView_picked_food_name_item,
			R.id.textView_picked_food_price_item,
			R.id.textView_picked_food_count_item,
			R.id.textView_picked_food_sum_price
	// R.id.textView_picked_food_state_item
	};
	protected static final int CUR_NEW_FOOD_CHANGED = 388962;// 删菜标记
	protected static final int LIST_CHANGED = 878633;// 已点菜更新标记
	protected static final int CUR_PICKED_FOOD_CHANGED = 388963;

	private FoodHandler mFoodHandler;
	private FoodDetailHandler mFoodDataHandler;
	private TotalCountHandler mTotalCountHandler;
	
	private ExpandableListView mPickedFoodList;
	private OrderFood mCurFood;

	private ImageFetcher mImageFetcher;
	protected View mCurrentView;
//	private LinearLayout mPickedFoodList;

	/*
	 * 显示已点菜的列表的handler 负责更新已点菜的显示
	 */
	private static class FoodHandler extends Handler {
		private WeakReference<PickedFoodActivity> mActivity;

		FoodHandler(PickedFoodActivity activity) {
			mActivity = new WeakReference<PickedFoodActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg)
		{
			final PickedFoodActivity activity = mActivity.get();
			ShoppingCart sCart = ShoppingCart.instance();

			final List<Map<String, ?>> groupData = new ArrayList<Map<String, ?>>();
			final List<List<Map<String, ?>>> childData =  new ArrayList<List<Map<String, ?>>>();
//
			//若包含新点菜，则将新点菜添加进列表
			if(sCart.hasNewOrder()){
				List<OrderFood> newFoods = sCart.getNewFoods();
				
				List<Map<String, ?>> newFoodDatas = new ArrayList<Map<String,?>>();
				for(OrderFood f : newFoods)
				{
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(ITEM_FOOD_NAME, f.name);
					map.put(ITEM_FOOD_ORI_PRICE, String.valueOf(Util.float2String2(f.getPriceWithTaste())));
					map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
					map.put(ITEM_FOOD_SUM_PRICE, String.valueOf(Util.float2String2(f.calcPriceWithTaste())));
					map.put(ITEM_THE_FOOD, f);
					newFoodDatas.add(map);
				}
				childData.add(newFoodDatas);
				
				HashMap<String, Object> map2 = new HashMap<String, Object>();
				map2.put(ITEM_GROUP_NAME, "新点菜");
				groupData.add(map2);
			}

			//若包含菜单，则将已点菜添加进列表
			if(sCart.hasOriOrder()){
				List<OrderFood> oriFoods = ShoppingCart.instance().getOriFoods();
				List<Map<String, ?>> pickedFoodDatas = new ArrayList<Map<String,?>>();
				for(OrderFood f : oriFoods)
				{
					if(f.getCount() != 0f){
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put(ITEM_IS_ORI_FOOD, true);
						map.put(ITEM_FOOD_NAME, f.name);
						map.put(ITEM_FOOD_ORI_PRICE, Util.float2String2(f.getPriceWithTaste()));
						map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
						map.put(ITEM_FOOD_SUM_PRICE, Util.float2String2(f.calcPriceWithTaste()));
						map.put(ITEM_THE_FOOD, f);
						pickedFoodDatas.add(map);
					}
					if(f.getOffset() > 0f)
					{
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put(ITEM_IS_ORI_FOOD, true);
						map.put(ITEM_FOOD_NAME, f.name);
						map.put(ITEM_FOOD_ORI_PRICE, Util.float2String2(f.getPriceWithTaste()));
						map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
						map.put(ITEM_FOOD_SUM_PRICE, Util.float2String2(f.calcPriceWithTaste()));
						map.put(ITEM_THE_FOOD, f);
						map.put(ITEM_IS_OFFSET, true);
						map.put(ITEM_FOOD_OFFSET, Util.float2String2(f.getOffset()));
						pickedFoodDatas.add(map);
					}
				}
				childData.add(pickedFoodDatas);
				
				HashMap<String, Object> map1 = new HashMap<String, Object>();
				map1.put(ITEM_GROUP_NAME, "已点菜");
				groupData.add(map1);
			}
			activity.mTotalCountHandler.sendEmptyMessage(0);
			
			//创建ListView的adapter
			SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(activity.getApplicationContext(), 
					groupData, R.layout.picked_food_list_group_item, GROUP_ITEM_TAGS, GROUP_ITEM_ID, 
					childData, R.layout.picked_food_list_item, ITEM_TAGS, ITEM_ID){
				@Override
				public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
					Map<String, ?> map = childData.get(groupPosition).get(childPosition);
					final OrderFood orderFood = (OrderFood) map.get(ITEM_THE_FOOD);
					
					View layout = activity.getLayoutInflater().inflate(R.layout.picked_food_list_item, null);
					layout.setTag(map);
					
					//设置菜品基本数据的显示
					((TextView) layout.findViewById(R.id.textView_picked_food_name_item)).setText(orderFood.name);
					((TextView) layout.findViewById(R.id.textView_picked_food_price_item))
						.setText(Util.float2String2(orderFood.getPriceWithTaste()));

					//数量显示
					final Button countEditText = (Button) layout.findViewById(R.id.textView_picked_food_count_item);
					final TextView sumPriceTextView = (TextView) layout.findViewById(R.id.textView_picked_food_sum_price);
					countEditText.setText(Util.float2String2(orderFood.getCount()));
					sumPriceTextView.setText(Util.float2String2(orderFood.calcPriceWithTaste()));

					//催菜显示、叫起/即起、删除按钮 初始化
//					final TextView stateHurrySignal = (TextView) view.findViewById(R.id.textView_picked_food_state_hurry_item);
					final View stateHurrySignal = layout.findViewById(R.id.imageView_pickedFood_hurry_item);  	
//					final TextView statusHangUpTextView = (TextView) view.findViewById(R.id.textView_hangup_pickedFood_list_item);
					Button deleteBtn = (Button) layout.findViewById(R.id.button_delete_pickedFood_list_item);
					//催菜状态显示
					if(orderFood.isHurried)
						stateHurrySignal.setVisibility(View.VISIBLE);
					else stateHurrySignal.setVisibility(View.INVISIBLE);

					//已点菜部分
					if(map.containsKey(ITEM_IS_ORI_FOOD))
					{
						//删菜按钮
						//已点菜显示退菜
						deleteBtn.setText("退菜");
						deleteBtn.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								if(ShoppingCart.instance().hasStaff()){
									activity.new AskCancelAmountDialog(orderFood, AskCancelAmountDialog.RETREAT).show();
								}else{
									Toast.makeText(activity, "请先输入服务员账号再执行退菜操作", Toast.LENGTH_SHORT).show();
								}
							}
						});
						//催菜
						Button hurryBtn = (Button) layout.findViewById(R.id.button_operation_pickedFood_list_item);
						hurryBtn.setVisibility(View.VISIBLE);
						hurryBtn.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if(orderFood.isHurried){
									orderFood.isHurried = false;
									Toast.makeText(activity, orderFood.name+" 取消催菜成功", Toast.LENGTH_SHORT).show();
									stateHurrySignal.setVisibility(View.INVISIBLE);
								}else{
									orderFood.isHurried = true;
									Toast.makeText(activity, orderFood.name + "催菜成功", Toast.LENGTH_SHORT).show();
									stateHurrySignal.setVisibility(View.VISIBLE);
								}
							}
						});

						//如果是退菜
						if(map.containsKey(ITEM_IS_OFFSET))
						{
							hurryBtn.setVisibility(View.INVISIBLE);
							(layout.findViewById(R.id.view_pickedFood_cancel_line)).setVisibility(View.VISIBLE);
							countEditText.setText(map.get(ITEM_FOOD_OFFSET).toString());
							
							deleteBtn.setText("还原");
							deleteBtn.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									try {
										orderFood.addCount(orderFood.getOffset());
										activity.mFoodHandler.sendEmptyMessage(PickedFoodActivity.LIST_CHANGED);
									} catch (BusinessException e) {
										e.printStackTrace();
									}
								}
							});
						}
						else (layout.findViewById(R.id.view_pickedFood_cancel_line)).setVisibility(View.INVISIBLE);
						
//						//判断叫起状态，显示当前状态
//						if(orderFood.hangStatus == OrderFood.FOOD_HANG_UP)
//						{
//							statusHangUpTextView.setText("叫起");
//							statusHangUpTextView.setVisibility(View.VISIBLE);
//						}
//						else if(orderFood.hangStatus == OrderFood.FOOD_IMMEDIATE) {
//							statusHangUpTextView.setText("即起");
//							statusHangUpTextView.setVisibility(View.VISIBLE); 
//						} else 	statusHangUpTextView.setVisibility(View.INVISIBLE); 
						
						//数量加按钮
						((ImageButton) layout.findViewById(R.id.imageButton_plus_pickedFood_item)).setVisibility(View.INVISIBLE);
						((ImageButton) layout.findViewById(R.id.imageButton_minus_pickedFood_item)).setVisibility(View.INVISIBLE);
					//新点菜部分
					} else{
						//催菜
						((Button) layout.findViewById(R.id.button_operation_pickedFood_list_item)).setVisibility(View.INVISIBLE);
						//删除按钮
						deleteBtn.setText("删除");
						deleteBtn.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								activity.new AskCancelAmountDialog(orderFood, AskCancelAmountDialog.DELETE).show();
							}
						});
//						//显示叫起按钮
//						statusHangUpTextView.setText("叫起");
//						if(orderFood.hangStatus == OrderFood.FOOD_HANG_UP)
//							statusHangUpTextView.setVisibility(View.VISIBLE);
//						else statusHangUpTextView.setVisibility(View.INVISIBLE);
						//数量点击侦听
						countEditText.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(final View v) {
								//新建一个输入框
								final EditText editText = new EditText(activity);
								editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL | EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
								//创建对话框，并将输入框传入
								new AlertDialog.Builder(activity).setTitle("请输入修改数量")
									.setView(editText)
									.setNeutralButton("确定",new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											//设置新数值
											if(!editText.getText().toString().equals(""))
											{
												//如果等于0则提示
												 if(Float.valueOf(editText.getText().toString()).equals(0f)) {
													 Toast.makeText(activity, "输入的数值不正确，请重新输入", Toast.LENGTH_SHORT).show();
												 }
												 else {
													float num = Float.parseFloat(editText.getText().toString());
													countEditText.setText(Util.float2String2(num));
													orderFood.setCount(num);
													ShoppingCart.instance().replaceFood(orderFood);	
													sumPriceTextView.setText(Util.float2String2(orderFood.calcPriceWithTaste()));

													activity.mTotalCountHandler.sendEmptyMessage(0);
//													mTotalPrice += orderFood.getPriceWithTaste();
//													mTotalPriceTextView.setText(Util.float2String2(mTotalPrice));
													dialog.dismiss();
												 }
											//如果为空则直接消失
											} else if(editText.getText().toString().equals("")){
												dialog.dismiss();
											}
										}
									})
									.setNegativeButton("取消", null).show();
							}
						});
						//数量加按钮
						ImageButton plus = (ImageButton) layout.findViewById(R.id.imageButton_plus_pickedFood_item);
						plus.setVisibility(View.VISIBLE);
						plus.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								float curNum = Float.parseFloat(countEditText.getText().toString());
								countEditText.setText(Util.float2String2(++curNum));
								orderFood.setCount(curNum);
								sumPriceTextView.setText(Util.float2String2(orderFood.calcPriceWithTaste()));
								activity.mTotalCountHandler.sendEmptyMessage(0);
							}
						});
						//数量减按钮
						ImageButton minus = (ImageButton) layout.findViewById(R.id.imageButton_minus_pickedFood_item);
						minus.setVisibility(View.VISIBLE);
						minus.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View v) {
								float curNum = Float.parseFloat(countEditText.getText().toString());
								if(--curNum >= 1)
								{
									countEditText.setText(Util.float2String2(curNum));
									orderFood.setCount(curNum);
									sumPriceTextView.setText(Util.float2String2(orderFood.calcPriceWithTaste()));
									activity.mTotalCountHandler.sendEmptyMessage(0);
								}
							}
						});
					}
					
					//操作按钮
//					((Button) view.findViewById(R.id.button_operation_pickedFood_list_item)).setOnClickListener(new View.OnClickListener() {				
//						@Override
//						public void onClick(View v) {
//							//初始化弹出框
//							final PopupWindow popup = new PopupWindow(optionLayout,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
//							popup.setOutsideTouchable(true);
//							popup.setBackgroundDrawable(new BitmapDrawable());
//							popup.update();
//							
//							if(popup.isShowing())
//								popup.dismiss();
//							else {
//								popup.showAsDropDown(v, -40,0);
//								//催菜按钮
//								((Button) popup.getContentView().findViewById(R.id.button_hurry_option_popupWindow)).setOnClickListener(new View.OnClickListener() {
//									@Override
//									public void onClick(View v) {
//										if(orderFood.isHurried){
//											orderFood.isHurried = false;
//											Toast.makeText(activity, orderFood.name+" 取消催菜成功", Toast.LENGTH_SHORT).show();
//											stateHurrySignal.setVisibility(View.INVISIBLE);
//										}else{
//											orderFood.isHurried = true;
//											Toast.makeText(activity, orderFood.name + "催菜成功", Toast.LENGTH_SHORT).show();
//											stateHurrySignal.setVisibility(View.VISIBLE);
//										}
//										popup.dismiss();
//									}
//								});
//								//叫起 即起按钮
//								Button hangUpBtn = (Button) popup.getContentView().findViewById(R.id.button_hangUp_option_popupWindow);
//								//已点菜状态
//								if(groupData.size() == 1 && groupData.get(0).containsValue("已点菜") || groupData.size() == 2 && groupPosition == 0)
//								{
//									//若是叫起、即起状态，则显示按钮
//									if(orderFood.hangStatus == OrderFood.FOOD_HANG_UP || orderFood.hangStatus == OrderFood.FOOD_IMMEDIATE){
//										hangUpBtn.setVisibility(View.VISIBLE);
//										//根据不同状态设置侦听器
//										if(orderFood.hangStatus == OrderFood.FOOD_HANG_UP){
//											hangUpBtn.setText("即起");
//											hangUpBtn.setOnClickListener(new View.OnClickListener() {
//												@Override
//												public void onClick(View v) {
//													orderFood.hangStatus = OrderFood.FOOD_IMMEDIATE;
//													statusHangUpTextView.setText("即起");
//													popup.dismiss();
//												}
//											});
//										} else if(orderFood.hangStatus == OrderFood.FOOD_IMMEDIATE){
//											hangUpBtn.setText("叫起");
//											hangUpBtn.setOnClickListener(new View.OnClickListener() {
//												@Override
//												public void onClick(View v) {
//													orderFood.hangStatus = OrderFood.FOOD_HANG_UP;
//													statusHangUpTextView.setText("叫起");
//													popup.dismiss();
//												}
//											});
//										}
//									//若不是叫起状态则不显示
//									} else{
//										hangUpBtn.setVisibility(View.GONE);
//									}
//								//新点菜状态
//								} else {
//									hangUpBtn.setVisibility(View.VISIBLE);
//									hangUpBtn.setOnClickListener(new View.OnClickListener() {
//										@Override
//										public void onClick(View v) {
//											//根据当前状态改变hangstatus状态
//											if(orderFood.hangStatus == OrderFood.FOOD_NORMAL){
//												orderFood.hangStatus = OrderFood.FOOD_HANG_UP;
//												statusHangUpTextView.setVisibility(View.VISIBLE);
//											}else{
//												orderFood.hangStatus = OrderFood.FOOD_NORMAL;
//												statusHangUpTextView.setVisibility(View.INVISIBLE);
//											}
//											popup.dismiss();
//										}
//									});
//								}
//							}
//						}
//					}); 
					
					return layout;
				}
			};
			activity.mPickedFoodList.setAdapter(adapter);
			//展开所有列表
			for(int i=0;i<groupData.size();i++)
			{
				activity.mPickedFoodList.expandGroup(i);
			}
		
//			//默认第一个设置为选中
//			activity.mPickedFoodList.postDelayed(new Runnable(){
//				@Override
//				public void run() {
//					activity.mPickedFoodList.performItemClick(activity.mPickedFoodList.getChildAt(1), 1, 1);
//				}
//			}, 100);
			
			((ProgressBar) activity.findViewById(R.id.progressBar_pickedFood)).setVisibility(View.GONE);
		}
	}

	/*
	 * 负责显示右边菜品详情的handler
	 */
	private static class FoodDetailHandler extends Handler {
		private WeakReference<PickedFoodActivity> mActivity;
		private ImageView mFoodImageView;
		private TextView mNewTasteTextView;
		private TextView mNewTempTasteTextView;
		private ImageButton mTempTasteBtn;
		private ImageButton mPickTasteBtn;
		private RadioGroup mSpecRadioGroup;

		private int mCurrentViewId = 0;
		private View mPickedView;
		private View mNewView;
		private TextView mPickedTasteTextView;
		private TextView mTempTasteTextView;

		FoodDetailHandler(final PickedFoodActivity activity) {
			mActivity = new WeakReference<PickedFoodActivity>(activity);

			mFoodImageView = (ImageView) activity
					.findViewById(R.id.imageView_selected_food_pickedFood);

			mNewTempTasteTextView = (TextView) activity
					.findViewById(R.id.textView_pinzhu_foodDetail);
			mNewTasteTextView = (TextView) activity
					.findViewById(R.id.textView_pickedTaste_foodDetail);

			// 打开菜品选择对话框
			mPickTasteBtn = (ImageButton) activity
					.findViewById(R.id.button_pickTaste_foodDetail);
			mPickTasteBtn.setOnClickListener(activity.new PickedFoodOnClickListener(
							PickTasteFragment.FOCUS_TASTE));
			// 品注按钮
			mTempTasteBtn = (ImageButton) activity.findViewById(R.id.button_pinzhu_foodDetail);
			mTempTasteBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					final EditText tempEditText = new EditText(activity);
					tempEditText.setSingleLine();
					//如果有品注，则显示
					if(activity.mCurFood.hasTmpTaste())
					{
						tempEditText.setText(activity.mCurFood.getTasteGroup().getTmpTastePref());
						tempEditText.selectAll();
					}
					
					new AlertDialog.Builder(activity).setTitle("请输入品注:")
						.setView(tempEditText)
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
								if(!activity.mCurFood.hasTaste()){
									activity.mCurFood.makeTasteGroup();
								}
								if(!tempEditText.getText().toString().equals(""))
								{
									Taste tmpTaste = new Taste();
									tmpTaste.setPreference(tempEditText.getText().toString());
									activity.mCurFood.getTasteGroup().setTmpTaste(tmpTaste);
								} else {
									activity.mCurFood.getTasteGroup().setTmpTaste(null);
								}
								
								activity.onTasteChanged(activity.mCurFood);
							}
						})
						.setNegativeButton("取消", null).show();					
				}
			});

			mPickedView = activity
					.findViewById(R.id.relativeLayout_pickedFood_right_bottom);
			mNewView = activity
					.findViewById(R.id.layout_pickedFood_right_bottom);

			mSpecRadioGroup = (RadioGroup) activity
					.findViewById(R.id.radioGroup_foodDetail);
			
			//规格
			mSpecRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					//tasteGroup没有创建则先创建
					if(!activity.mCurFood.hasTaste()){
						activity.mCurFood.makeTasteGroup();
					} 
					//清楚旧规格
					for(Taste spec : WirelessOrder.foodMenu.specs){
						activity.mCurFood.getTasteGroup().removeTaste(spec);
					}
					
					switch(checkedId)
					{
					case R.id.radio0:
						activity.mCurFood.getTasteGroup().addTaste(WirelessOrder.foodMenu.specs[0]);
						break;
					case R.id.radio1:
						activity.mCurFood.getTasteGroup().addTaste(WirelessOrder.foodMenu.specs[1]);
						break;
					case R.id.radio2:
						break;
					}
					Message msg = new Message();
					msg.what = CUR_NEW_FOOD_CHANGED;
					handleMessage(msg);
					
					((TextView)activity.mCurrentView.findViewById(R.id.textView_picked_food_sum_price))
						.setText(Util.float2String2(activity.mCurFood.calcPriceWithTaste()));
					activity.mTotalCountHandler.sendEmptyMessage(0);

					((TextView) activity.mCurrentView.findViewById(R.id.textView_picked_food_price_item))
						.setText(Util.float2String2(activity.mCurFood.getPriceWithTaste()));
				}
			});
			//下面是已点菜的显示项
			mPickedTasteTextView = (TextView) activity
					.findViewById(R.id.textView_pickedFood_pickedView_taste);
			mTempTasteTextView = (TextView) activity.findViewById(R.id.textView_pickedFood_tempTaste);
		}

		@Override
		public void handleMessage(Message msg) {
			final PickedFoodActivity activity = mActivity.get();

			switch (msg.what) {
			case CUR_NEW_FOOD_CHANGED:
				if (mCurrentViewId != CUR_NEW_FOOD_CHANGED) {
					mNewView.setVisibility(View.VISIBLE);
					mPickedView.setVisibility(View.INVISIBLE);
					mCurrentViewId = CUR_NEW_FOOD_CHANGED;
				}
				// 设置菜品的各个数据

				if (activity.mCurFood.hasTmpTaste()) {
					mNewTempTasteTextView.setText(activity.mCurFood
							.getTasteGroup().getTmpTastePref());
				} else {
					mNewTempTasteTextView.setText("");
				}
				if (activity.mCurFood.hasNormalTaste()) {
					mNewTasteTextView.setText(activity.mCurFood.getTasteGroup()
							.getNormalTastePref());
				} else {
					mNewTasteTextView.setText("");
				}

				if(activity.mCurFood.getTasteGroup().hasSpec())
				{
					Taste[] specs = activity.mCurFood.getTasteGroup().getSpecs();
					for(int i=0;i< WirelessOrder.foodMenu.specs.length; i++){
						if(specs[0].equals(WirelessOrder.foodMenu.specs[i]))
						{
							((RadioButton)mSpecRadioGroup.getChildAt(i)).setChecked(true);
							break;
						}
					}
				}
//				// 清空品注
//				((ImageButton) activity
//						.findViewById(R.id.button_removeAllTaste))
//						.setOnClickListener(new OnClickListener() {
//							@Override
//							public void onClick(View v) {
//								activity.mCurFood.clearTasetGroup();
//								mNewTempTasteTextView.setText("");
//								mNewTasteTextView.setText("");
//							}
//						});
				break;
			case CUR_PICKED_FOOD_CHANGED:
				// 已点菜的显示
				if (mCurrentViewId != CUR_PICKED_FOOD_CHANGED) {
					mNewView.setVisibility(View.INVISIBLE);
					mPickedView.setVisibility(View.VISIBLE);
					mCurrentViewId = CUR_PICKED_FOOD_CHANGED;
				}

				// if(activity.mCurFood.hasTmpTaste()){
				// mNewTempTasteTextView.setText(activity.mCurFood.getTasteGroup().getTmpTastePref());
				// }else{
				// mNewTempTasteTextView.setText("");
				// }
				if (activity.mCurFood.hasNormalTaste()) {
					mPickedTasteTextView.setText(activity.mCurFood
							.getTasteGroup().getNormalTastePref());
				} else {
					mPickedTasteTextView.setText("");
				}

				if(activity.mCurFood.hasTmpTaste())
					mTempTasteTextView.setText(activity.mCurFood.getTasteGroup().getTmpTastePref());
				else mTempTasteTextView.setText("");
				
				break;
			}
			if(activity.mCurFood.image != null)
				activity.mImageFetcher.loadImage(activity.mCurFood.image, mFoodImageView);
			else mFoodImageView.setImageResource(R.drawable.null_pic);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.picked_food);

		mImageFetcher = new ImageFetcher(this, 300, 225);

		// 初始化handler
		mFoodHandler = new FoodHandler(this);
		mFoodDataHandler = new FoodDetailHandler(this);
		mTotalCountHandler = new TotalCountHandler(this);
		
		final OptionBarFragment optionbar = (OptionBarFragment) this.getFragmentManager().findFragmentById(
				R.id.bottombar_pickedFood);
		optionbar.setOnOrderChangeListener(this);

		mPickedFoodList = (ExpandableListView) findViewById(R.id.expandableListView_pickedFood);
//		mPickedFoodList = (LinearLayout) findViewById(R.id.linearLayout_pickedFood);

		((RelativeLayout) findViewById(R.id.relativeLayout_amount_foodDetailTab1))
				.setVisibility(View.GONE);
		((RelativeLayout) findViewById(R.id.relativeLayout_price_foodDetailTab1))
				.setVisibility(View.GONE);

		if (ShoppingCart.instance().hasTable()) {
			new QueryOrderTask(ShoppingCart.instance().getDestTable().aliasID).execute(WirelessOrder.foodMenu);
//			new com.wireless.lib.task.QueryOrderTask(ShoppingCart.instance().getDestTable().aliasID) {
//
//				private ProgressToast mToast;
//
//				@Override
//				protected void onPreExecute() {
//					mToast = ProgressToast.show(PickedFoodActivity.this, "查询"
//							+ mTblAlias + "号账单信息...请稍候");
//				}
//
//				@Override
//				protected void onPostExecute(Order order) {
//					mToast.cancel();
//					// 更新购物车
//					ShoppingCart.instance().setOriOrder(order);
//					mFoodHandler.sendEmptyMessage(LIST_CHANGED);
//				}
//			}.execute(WirelessOrder.foodMenu);

		} else {
			mFoodHandler.sendEmptyMessage(LIST_CHANGED);
		}

		//下单按钮 
		((Button) findViewById(R.id.imageButton_submit_pickedFood)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(final View v) {				
				try{
					
					ShoppingCart.instance().commit(new OnCommitListener(){
						
//						private ProgressToast mToast;
						private ProgressDialog mProgressDialog;
						@Override
						public void OnPreCommit(Order reqOrder) {
							mProgressDialog = ProgressDialog.show(PickedFoodActivity.this,"", "查询" + reqOrder.destTbl.aliasID + "号账单信息...请稍候");
						}

						@Override
						public void onPostCommit(Order reqOrder, BusinessException e) {
							mProgressDialog.cancel();
							if(e == null){
								//当读取到餐台锁定信息时,如果是锁定状态则不清除数据
								SharedPreferences pref = getSharedPreferences(Params.TABLE_ID, MODE_PRIVATE);
								if(!pref.contains(Params.TABLE_ID))
								{
									ShoppingCart.instance().clearTable();
								}
								//读取服务员锁定信息
								pref = getSharedPreferences(Params.PREFS_NAME, MODE_PRIVATE);
								if(!pref.contains(Params.IS_FIX_STAFF))
								{
									ShoppingCart.instance().clearStaff();
								}
								Toast.makeText(PickedFoodActivity.this, reqOrder.destTbl.aliasID + "号餐台下单成功", Toast.LENGTH_SHORT).show();

								ShoppingCart.instance().notifyFoodsChange();
								
								setResult(ORDER_SUBMIT_RESULT);
								
								v.postDelayed(new Runnable(){

									@Override
									public void run() {
										onBackPressed();
									}
								}, 100);
								
							}else{
								if(ShoppingCart.instance().hasOriOrder()){

									if(e.getErrCode() == ErrorCode.TABLE_IDLE){
										//如果是改单，并且返回是餐台空闲的错误状态，
										//则提示用户，并清空购物车中的原账单
										new AlertDialog.Builder(PickedFoodActivity.this)
											.setTitle("提示")
											.setMessage(reqOrder.destTbl.aliasID + "号餐台已经结帐，已点菜信息将刷新，新点菜信息将会保留")
											.setNeutralButton("确定",
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog,	int which){
														ShoppingCart.instance().setOriOrder(null);
														mFoodHandler.sendEmptyMessage(LIST_CHANGED);
													}
												})
											.show();

										
									}else if(e.getErrCode() == ErrorCode.ORDER_EXPIRED){
										//如果是改单，并且返回是账单过期的错误状态，
										//则提示用户重新请求账单，再次确认提交
										final Table destTbl = reqOrder.destTbl;
										new AlertDialog.Builder(PickedFoodActivity.this)
											.setTitle("提示")
											.setMessage(reqOrder.destTbl.aliasID + "号餐台的账单信息已经更新，已点菜信息将刷新，新点菜信息将会保留")
											.setNeutralButton("确定",
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog,	int which){
														new QueryOrderTask(destTbl.aliasID).execute(WirelessOrder.foodMenu);
													}
												})
											.show();
										
									}else{
										new AlertDialog.Builder(PickedFoodActivity.this)
											.setTitle("提示")
											.setMessage(e.getMessage())
											.setNeutralButton("确定", null)
											.show();
									}
								}else{
									if(e.getErrCode() == ErrorCode.TABLE_BUSY){
										//如果是新下单，并且返回是餐台就餐的错误状态，
										//则提示用户重新请求账单，再次确认提交
										final Table destTbl = reqOrder.destTbl;
										new AlertDialog.Builder(PickedFoodActivity.this)
											.setTitle("提示")
											.setMessage(reqOrder.destTbl.aliasID + "号餐台的账单信息已经更新，已点菜信息将刷新，新点菜信息将会保留")
											.setNeutralButton("确定",
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog,	int which){
														new QueryOrderTask(destTbl.aliasID).execute(WirelessOrder.foodMenu);
													}
												})
											.show();
									}else{
										new AlertDialog.Builder(PickedFoodActivity.this)
											.setTitle("提示")
											.setMessage(e.getMessage())
											.setNeutralButton("确定", null)
											.show();
									}
								}
							}								
						}						
					});
					
				}catch(BusinessException e){
					Toast.makeText(PickedFoodActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		//设置侦听
		//当点击菜品是改变右边菜品详情的显示
		mPickedFoodList.setOnChildClickListener(new OnChildClickListener(){
			@Override
			public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
				if(mCurrentView != null)
					mCurrentView.setBackgroundDrawable(null);
				view.setBackgroundColor(view.getResources().getColor(R.color.blue));
				
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) view.getTag();
				//点击后改变该项的颜色显示并刷新右边
				mCurrentView = view;
				mCurFood = (OrderFood) map.get(ITEM_THE_FOOD);
				
				if(map.containsKey(ITEM_IS_ORI_FOOD))
				{
					mFoodDataHandler.sendEmptyMessage(PickedFoodActivity.CUR_PICKED_FOOD_CHANGED);
				} else{
					mFoodDataHandler.sendEmptyMessage(PickedFoodActivity.CUR_NEW_FOOD_CHANGED);
				}
				return false;
			}
		});
		//默认右边无显示
		findViewById(R.id.layout_pickedFood_right_bottom).setVisibility(View.INVISIBLE);
	}

	protected void showDialog(String tab, final OrderFood food) {
		PickTasteFragment pickTasteFg = new PickTasteFragment();
		pickTasteFg.setOnTasteChangeListener(this);
		Bundle args = new Bundle();
		args.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(food));
		pickTasteFg.setArguments(args);
		pickTasteFg.show(getFragmentManager(), tab);
	}

	@Override
	protected void onDestroy() {
		mImageFetcher.clearCache();
		super.onDestroy();
	}

	// activity关闭后不再侦听购物车变化
	@Override
	public void onBackPressed() {
		ShoppingCart.instance().setOnTableChangeListener(null);
		mPickedFoodList.setOnChildClickListener(null);
		((OptionBarFragment) this.getFragmentManager().findFragmentById(R.id.bottombar_pickedFood))
			.setOnOrderChangeListener(null);
		super.onBackPressed();
	}

	private class AskCancelAmountDialog extends Dialog {
		static final String DELETE = "删除";
		static final String RETREAT = "退菜";

		AskCancelAmountDialog(final OrderFood selectedFood, final String method) {
			super(PickedFoodActivity.this);

			final Context context = PickedFoodActivity.this;
			View view = LayoutInflater.from(context).inflate(
					R.layout.delete_count_dialog, null);
			setContentView(view);
			this.setTitle("请输入" + method + "的数量");

			// 删除数量默认为此菜品的点菜数量
			final EditText countEdtTxt = (EditText) view
					.findViewById(R.id.editText_count_deleteCount);
			countEdtTxt.setText(Util.float2String2(selectedFood.getCount()));
			countEdtTxt.selectAll();
			// 增加数量
			((ImageButton) view
					.findViewById(R.id.imageButton_plus_deleteCount_dialog))
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (!countEdtTxt.getText().toString().equals("")) {
								float curNum = Float.parseFloat(countEdtTxt
										.getText().toString());
								countEdtTxt.setText(Util
										.float2String2(++curNum));
							}
						}
					});
			// 减少数量
			((ImageButton) findViewById(R.id.imageButton_minus_deleteCount_dialog))
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (!countEdtTxt.getText().toString().equals("")) {
								float curNum = Float.parseFloat(countEdtTxt.getText().toString());
								if (--curNum >= 1) {
									countEdtTxt.setText(Util.float2String2(curNum));
								}
							}
						}
					});

			// "确定"Button
			Button okBtn = (Button) view
					.findViewById(R.id.button_confirm_deleteCount);
			okBtn.setText("确定");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						float foodAmount = selectedFood.getCount();
						float cancelAmount = Float.parseFloat(countEdtTxt
								.getText().toString());

						if (foodAmount == cancelAmount) {
							/**
							 * 如果数量相等，则从列表中删除此菜
							 */
							if (method.equals(DELETE))
								ShoppingCart.instance().remove(selectedFood);
							else if (method.equals(RETREAT)) {
								selectedFood.removeCount(cancelAmount);
							}

							// 若完全没有菜式，则关闭该activity
							if (!ShoppingCart.instance().hasOrder()) {
								onBackPressed();
								PickedFoodActivity.this.onBackPressed();
							} else {
								mFoodHandler.sendEmptyMessage(PickedFoodActivity.LIST_CHANGED);
								dismiss();
								Toast.makeText(
										context,
										method + "\"" + selectedFood.toString()
												+ "\"" + cancelAmount + "份成功",
										Toast.LENGTH_SHORT).show();
							}

						} else if (foodAmount > cancelAmount) {
							/**
							 * 如果删除数量少于已点数量，则相应减去删除数量
							 */
							selectedFood.removeCount(cancelAmount);
							mFoodHandler
									.sendEmptyMessage(PickedFoodActivity.LIST_CHANGED);
							dismiss();
							Toast.makeText(
									context,
									method + "\"" + selectedFood.toString()
											+ "\"" + cancelAmount + "份成功",
									Toast.LENGTH_SHORT).show();

						} else {
							Toast.makeText(context,
									"输入的" + method + "数量大于已点数量, 请重新输入",
									Toast.LENGTH_SHORT).show();
						}

					} catch (NumberFormatException e) {
						Toast.makeText(context, "你输入的" + method + "数量不正确",
								Toast.LENGTH_SHORT).show();
					} catch (BusinessException e) {
						e.printStackTrace();
					}

				}
			});

			// "取消"Button
			Button cancelBtn = (Button) view
					.findViewById(R.id.button_cancel_deleteCount);
			cancelBtn.setText("取消");
			cancelBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		}
	}

	private class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask {
//		private ProgressToast mToast;

		public QueryOrderTask(int tableAlias) {
			super(tableAlias);
		}

		@Override
		protected void onPreExecute() {
//			mToast = ProgressToast.show(PickedFoodActivity.this, "查询" + mTblAlias + "号账单信息...请稍候");
		}

		@Override
		protected void onPostExecute(Order order) {
//			mToast.cancel();

			// 请求成功后设置购物车并更新
			ShoppingCart.instance().setOriOrder(order);
			mFoodHandler.sendEmptyMessage(LIST_CHANGED);
		}
	}

	@Override
	public void onTasteChanged(OrderFood food) {
		ShoppingCart.instance().getNewOrder().remove(mCurFood);
		mCurFood = food;
		try {
			ShoppingCart.instance().getNewOrder().addFood(mCurFood);
		} catch (BusinessException e) {
			e.printStackTrace();
		}
		mFoodDataHandler.sendEmptyMessage(PickedFoodActivity.CUR_NEW_FOOD_CHANGED);
		
		((TextView) mCurrentView.findViewById(R.id.textView_picked_food_price_item))
			.setText(Util.float2String2(mCurFood.getPriceWithTaste()));
		((TextView) mCurrentView.findViewById(R.id.textView_picked_food_sum_price))
			.setText(Util.float2String2(mCurFood.calcPriceWithTaste()));
		mTotalCountHandler.sendEmptyMessage(0);
	}

	class PickedFoodOnClickListener implements OnClickListener {
		private String mTab;

		public PickedFoodOnClickListener(String mTab) {
			this.mTab = mTab;
		}

		@Override
		public void onClick(View v) {
			showDialog(mTab, mCurFood);
		}
	}

	@Override
	public void onOrderChange(Order order) {
		mFoodHandler.sendEmptyMessage(PickedFoodActivity.LIST_CHANGED);
	}
	
	private static class TotalCountHandler extends Handler{
//		private WeakReference<PickedFoodActivity> mActivity;
		
		private TextView mTotalCountTextView;
		private TextView mTotalPriceTextView;
//		private float mTotalPrice = 0;
		
		public TotalCountHandler(PickedFoodActivity activity) {
//			mActivity = new WeakReference<PickedFoodActivity>(activity);
			mTotalCountTextView = (TextView) activity.findViewById(R.id.textView_total_count_pickedFood);
			mTotalPriceTextView = (TextView) activity.findViewById(R.id.textView_total_price_pickedFood);
		}
		@Override
		public void handleMessage(Message msg) {
			mTotalCountTextView.setText(Util.float2String2(ShoppingCart.instance().getTotalCount()));
			mTotalPriceTextView.setText(Util.float2String2(ShoppingCart.instance().getTotalPrice()));
		}
		
	}
}

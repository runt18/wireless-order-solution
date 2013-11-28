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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
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
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.fragment.OptionBarFragment;
import com.wireless.fragment.OptionBarFragment.OnOrderChangeListener;
import com.wireless.fragment.PickTasteFragment;
import com.wireless.fragment.PickTasteFragment.OnTasteChangeListener;
import com.wireless.ordermenu.R;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.util.SearchFoodHandler;
import com.wireless.util.SearchFoodHandler.OnFoodAddListener;
import com.wireless.util.imgFetcher.ImageFetcher;

public class SelectedFoodActivity extends Activity implements
		OnOrderChangeListener, OnTasteChangeListener, OnFoodAddListener {
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

	/*
	 * the tags use to build adapter
	 */
	private static final String[] GROUP_ITEM_TAGS = { ITEM_GROUP_NAME };

	private static final int[] GROUP_ITEM_ID = { R.id.textView_groupName_pickedFood_list_item };

	private static final String[] ITEM_TAGS = { ITEM_FOOD_NAME,
			ITEM_FOOD_ORI_PRICE, ITEM_FOOD_COUNT, ITEM_FOOD_SUM_PRICE
	};

	private static final int[] ITEM_ID = { R.id.textView_picked_food_name_item,
			R.id.textView_picked_food_price_item,
			R.id.textView_picked_food_count_item,
			R.id.textView_picked_food_sum_price
	};
	
	protected static final int LIST_CHANGED = 878633;//点菜列表更新标记
	
	protected static final int CUR_PICKED_FOOD_CHANGED = 388963;//已点菜的细节更新标记
	protected static final int CUR_NEW_FOOD_CHANGED = 388962;// 新点菜的细节更新标记

	/*
	 * this handler is use to refresh the food list 
	 */
	private FoodListHandler mFoodListHandler;
	//this handler is use to refresh the picked food's detail
	private FoodDetailHandler mFoodDetailHandler;
	//this handler is use to refresh the foods amount and total prices
	private TotalCountHandler mTotalCountHandler;
	//the handler is use to refresh the search result
	private SearchFoodHandler mSearchFoodHandler;
	//the food list
	private ExpandableListView mPickedFoodList;
	
	private OrderFood mCurrentFood;

	private ImageFetcher mImageFetcher;
	
	protected View mCurrentView;
	/*
	 * 显示已点菜的列表的handler 负责更新已点菜的显示
	 */
	private static class FoodListHandler extends Handler {
		private WeakReference<SelectedFoodActivity> mActivity;
		private List<Kitchen> mKitchens = new ArrayList<Kitchen>();

		FoodListHandler(SelectedFoodActivity activity) {
			mActivity = new WeakReference<SelectedFoodActivity>(activity);
			
			for(Kitchen kitchen : WirelessOrder.foodMenu.kitchens){
				if(kitchen.isAllowTemp()){
					mKitchens.add(kitchen);
				}
			}
		}

		/**
		 * package all new foods and original foods and set the ExpandableListAdapter
		 */
		@Override
		public void handleMessage(Message msg)
		{
			final SelectedFoodActivity activity = mActivity.get();
			ShoppingCart sCart = ShoppingCart.instance();

			final List<Map<String, ?>> groupData = new ArrayList<Map<String, ?>>();
			final List<List<Map<String, ?>>> childData =  new ArrayList<List<Map<String, ?>>>();
//
			HashMap<String, Object> map2 = new HashMap<String, Object>();
			map2.put(ITEM_GROUP_NAME, "新点菜");
			groupData.add(map2);
			
			List<Map<String, ?>> newFoodDatas = new ArrayList<Map<String,?>>();
			//若包含新点菜，则将新点菜添加进列表
			if(sCart.hasNewOrder()){
				List<OrderFood> newFoods = sCart.getNewFoods();
				
				for(OrderFood f : newFoods)
				{
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(ITEM_FOOD_NAME, f.getName());
					map.put(ITEM_FOOD_ORI_PRICE, String.valueOf(NumericUtil.float2String2(f.getUnitPriceWithTaste())));
					map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
					map.put(ITEM_FOOD_SUM_PRICE, String.valueOf(NumericUtil.float2String2(f.calcPriceWithTaste())));
					map.put(ITEM_THE_FOOD, f);
					newFoodDatas.add(map);
				}
			}
			childData.add(newFoodDatas);

			//若包含菜单，则将已点菜添加进列表
			if(sCart.hasOriOrder()){
				List<OrderFood> oriFoods = ShoppingCart.instance().getOriFoods();
				List<Map<String, ?>> pickedFoodDatas = new ArrayList<Map<String,?>>();
				for(OrderFood f : oriFoods)
				{
					if(f.getCount() != 0f){
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put(ITEM_IS_ORI_FOOD, true);
						map.put(ITEM_FOOD_NAME, f.getName());
						map.put(ITEM_FOOD_ORI_PRICE, NumericUtil.float2String2(f.getUnitPriceWithTaste()));
						map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
						map.put(ITEM_FOOD_SUM_PRICE, NumericUtil.float2String2(f.calcPriceWithTaste()));
						map.put(ITEM_THE_FOOD, f);
						pickedFoodDatas.add(map);
					}
					if(f.getDelta() > 0f)
					{
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put(ITEM_IS_ORI_FOOD, true);
						map.put(ITEM_FOOD_NAME, f.getName());
						map.put(ITEM_FOOD_ORI_PRICE, NumericUtil.float2String2(f.getUnitPriceWithTaste()));
						map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
						map.put(ITEM_FOOD_SUM_PRICE, NumericUtil.float2String2(f.calcPriceWithTaste()));
						map.put(ITEM_THE_FOOD, f);
						map.put(ITEM_IS_OFFSET, true);
						map.put(ITEM_FOOD_OFFSET, NumericUtil.float2String2(f.getDelta()));
						pickedFoodDatas.add(map);
					}
				}
				childData.add(pickedFoodDatas);
				
				HashMap<String, Object> map1 = new HashMap<String, Object>();
				map1.put(ITEM_GROUP_NAME, "已点菜");
				groupData.add(map1);
			}
			//refresh total prices display
			activity.mTotalCountHandler.sendEmptyMessage(0);
			
			//创建ListView的adapter
			SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(activity.getApplicationContext(), 
					groupData, R.layout.picked_food_list_group_item, GROUP_ITEM_TAGS, GROUP_ITEM_ID, 
					childData, R.layout.picked_food_list_item, ITEM_TAGS, ITEM_ID){
				
				@Override
				public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
					View layout =  super.getGroupView(groupPosition, isExpanded, convertView, parent);
					//initial groupView buttons
					EditText searchEditText = (EditText) layout.findViewById(R.id.editText_SelectedFood_listGroup_item_search);
					Button addTempFoodBtn = (Button) layout.findViewById(R.id.button_selectedFoodListGroup_item_tempFood);
					Button clearSearchBtn = (Button) layout.findViewById(R.id.button_selectedFoodListGroup_item_clear);
					//set search handler
					activity.mSearchFoodHandler = new SearchFoodHandler(activity, searchEditText, clearSearchBtn);
					activity.mSearchFoodHandler.setOnFoodAddListener(activity);
					
					switch(groupPosition){
					//new foods group item
					case 0:
						searchEditText.setVisibility(View.VISIBLE);
						addTempFoodBtn.setVisibility(View.VISIBLE);
						clearSearchBtn.setVisibility(View.VISIBLE);
						//添加临时菜按钮
						addTempFoodBtn.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								//如果可用厨房是空的，则提示
								if(mKitchens.isEmpty()){
									Toast.makeText(activity, "没有可添加临时菜的厨房", Toast.LENGTH_SHORT).show();
								//有可添加临时菜的厨房
								} else {
									final OrderFood food = new OrderFood();
									food.setTemp(true);
									final View tempLayout = activity.getLayoutInflater().inflate(R.layout.temp_item, null);
									final EditText foodNameText = (EditText) tempLayout.findViewById(R.id.editText_tempFood_item_name);
									final EditText foodPriceText = (EditText) tempLayout.findViewById(R.id.editText_tempFood_item_price);
									final EditText foodAmountText = (EditText) tempLayout.findViewById(R.id.editText_tempFood_item_amount);
									
									final TextView kitchenText = (TextView) tempLayout.findViewById(R.id.textView_tempFood_item_kitchen);
									//初始化为第一个厨房
									food.asFood().setKitchen(mKitchens.get(0));
									kitchenText.setText(food.getKitchen().getName());
									//点击选择厨房
									kitchenText.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View v) {
											//设置临时菜可用厨房弹出列表
											final ListPopupWindow popup = new ListPopupWindow(activity);
											
											popup.setAnchorView(kitchenText);
											popup.setAdapter(new BaseAdapter(){

												@Override
												public int getCount() {
													return mKitchens.size();
												}

												@Override
												public Object getItem( int position) {
													return mKitchens.get(position);
												}

												@Override
												public long getItemId( int position) {
													return position;
												}

												@Override
												public View getView(int position, View convertView, ViewGroup parent) {
													//show all available kitchen
													TextView layout = new TextView(activity);
													Kitchen kitchen = mKitchens.get(position);
													layout.setText(kitchen.getName());
													layout.setGravity(Gravity.CENTER_VERTICAL);
													layout.setHeight(54);
													layout.setTextSize(22f);
													
													layout.setTag(kitchen);
													return layout;
												}
											});
											//列表点击侦听
											popup.setOnItemClickListener(new OnItemClickListener() {
												@Override
												public void onItemClick(AdapterView<?> parent, View view,
														int position, long id) {
													Kitchen kitchen = (Kitchen) view.getTag();
													food.asFood().setKitchen(kitchen);
													kitchenText.setText(food.getKitchen().getName());
													popup.dismiss();
												}
											});
											
											popup.show();

										}
									});
									//默认弹出厨房列表
									kitchenText.post(new Runnable() {
										@Override
										public void run() {
											kitchenText.performClick();
										}
									});
									
									//设置临时菜对话框
									new AlertDialog.Builder(activity).setTitle("添加临时菜").setView(tempLayout)
										.setPositiveButton("确定", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												String foodName = foodNameText.getText().toString()
														.replace(",", ";").replace("，", "；").trim();
												//如果菜名不为空
												if(!foodName.equals("")){
													food.asFood().setName(foodName);
													
													String foodPrice = foodPriceText.getText().toString();
													//设置价格，默认为0
													if(!foodPrice.equals("")){
														food.asFood().setPrice(Float.valueOf(foodPrice));
													} else{
														food.asFood().setPrice(0f);
													}
													
													String foodAmount = foodAmountText.getText().toString();
													//设置数量，默认为1
													if(!foodAmount.equals("")){
														food.setCount(Float.valueOf(foodAmount));
													} else food.setCount(1f);
													
													try {
														//添加到购物车并更新列表
														ShoppingCart.instance().addFood(food);
														activity.mFoodListHandler.sendEmptyMessage(LIST_CHANGED);
													} catch (BusinessException e) {
														Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
													}
													
												} else {
													Toast.makeText(activity, "菜品未添加，请输入菜品名再添加", Toast.LENGTH_SHORT).show();
												}
											}
										})
										.setNegativeButton("取消", null)
										.show();
									
								}
							}
						});
						break;
					//original foods group item
					case 1:
						searchEditText.setVisibility(View.GONE);
						addTempFoodBtn.setVisibility(View.GONE);
						clearSearchBtn.setVisibility(View.GONE);
						break;
					}
					return layout;
				}

				/**
				 * if cart has no new foods , just hide the group item
				 */
				@Override
				public int getChildrenCount(int groupPosition) {
					switch(groupPosition)
					{
					case 0:
						if(!ShoppingCart.instance().hasNewOrder())
							return 0;
						else break;
					}
					return super.getChildrenCount(groupPosition);
				}

				@Override
				public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
					Map<String, ?> map = childData.get(groupPosition).get(childPosition);
					final OrderFood orderFood = (OrderFood) map.get(ITEM_THE_FOOD);
					
					View layout = activity.getLayoutInflater().inflate(R.layout.picked_food_list_item, null);
					layout.setTag(map);
					
					//设置菜品基本数据的显示
					((TextView) layout.findViewById(R.id.textView_picked_food_name_item)).setText(orderFood.getName());
					((TextView) layout.findViewById(R.id.textView_picked_food_price_item))
						.setText(NumericUtil.float2String2(orderFood.getUnitPriceWithTaste()));

					//数量显示
					final Button countEditText = (Button) layout.findViewById(R.id.textView_picked_food_count_item);
					final TextView sumPriceTextView = (TextView) layout.findViewById(R.id.textView_picked_food_sum_price);
					countEditText.setText(NumericUtil.float2String2(orderFood.getCount()));
					sumPriceTextView.setText(NumericUtil.float2String2(orderFood.calcPriceWithTaste()));

					//催菜显示、叫起/即起、删除按钮 初始化
					final View stateHurrySignal = layout.findViewById(R.id.imageView_pickedFood_hurry_item);  	
					Button deleteBtn = (Button) layout.findViewById(R.id.button_delete_pickedFood_list_item);
					//催菜状态显示
					if(orderFood.isHurried()){
						stateHurrySignal.setVisibility(View.VISIBLE);
					}else{
						stateHurrySignal.setVisibility(View.INVISIBLE);
					}

					//已点菜部分
					if(map.containsKey(ITEM_IS_ORI_FOOD)){
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
								if(orderFood.isHurried()){
									orderFood.setHurried(false);
									Toast.makeText(activity, orderFood.getName() + " 取消催菜成功", Toast.LENGTH_SHORT).show();
									stateHurrySignal.setVisibility(View.INVISIBLE);
								}else{
									orderFood.setHurried(true);
									Toast.makeText(activity, orderFood.getName() + "催菜成功", Toast.LENGTH_SHORT).show();
									stateHurrySignal.setVisibility(View.VISIBLE);
								}
							}
						});

						//如果是退菜
						if(map.containsKey(ITEM_IS_OFFSET)){
							
							hurryBtn.setVisibility(View.INVISIBLE);
							(layout.findViewById(R.id.view_pickedFood_cancel_line)).setVisibility(View.VISIBLE);
							countEditText.setText(map.get(ITEM_FOOD_OFFSET).toString());
							
							deleteBtn.setText("取消退菜");
							deleteBtn.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									try {
										orderFood.addCount(orderFood.getDelta());
										activity.mFoodListHandler.sendEmptyMessage(SelectedFoodActivity.LIST_CHANGED);
									} catch (BusinessException e) {
										e.printStackTrace();
									}
								}
							});
						}
						else (layout.findViewById(R.id.view_pickedFood_cancel_line)).setVisibility(View.INVISIBLE);
						
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
													countEditText.setText(NumericUtil.float2String2(num));
													orderFood.setCount(num);
													ShoppingCart.instance().replaceFood(orderFood);	
													sumPriceTextView.setText(NumericUtil.float2String2(orderFood.calcPriceWithTaste()));

													activity.mTotalCountHandler.sendEmptyMessage(0);
//													mTotalPrice += orderFood.getPriceWithTaste();
//													mTotalPriceTextView.setText(NumericUtil.float2String2(mTotalPrice));
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
								countEditText.setText(NumericUtil.float2String2(++curNum));
								orderFood.setCount(curNum);
								sumPriceTextView.setText(NumericUtil.float2String2(orderFood.calcPriceWithTaste()));
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
									countEditText.setText(NumericUtil.float2String2(curNum));
									orderFood.setCount(curNum);
									sumPriceTextView.setText(NumericUtil.float2String2(orderFood.calcPriceWithTaste()));
									activity.mTotalCountHandler.sendEmptyMessage(0);
								}
							}
						});
					}
					
					return layout;
				}
			};
			
			activity.mPickedFoodList.setAdapter(adapter);
			//展开所有列表
			for(int i=0;i<groupData.size();i++)
			{
				activity.mPickedFoodList.expandGroup(i);
			}
		
			((ProgressBar) activity.findViewById(R.id.progressBar_pickedFood)).setVisibility(View.GONE);
			
			
		}
	}

	/*
	 * 负责显示右边菜品详情的handler
	 */
	private static class FoodDetailHandler extends Handler {
		private WeakReference<SelectedFoodActivity> mActivity;
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

		FoodDetailHandler(final SelectedFoodActivity activity) {
			mActivity = new WeakReference<SelectedFoodActivity>(activity);

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
					if(activity.mCurrentFood.hasTmpTaste())
					{
						tempEditText.setText(activity.mCurrentFood.getTasteGroup().getTmpTastePref());
						tempEditText.selectAll();
					}
					//show the temp food input dialog
					new AlertDialog.Builder(activity).setTitle("请输入品注:")
						.setView(tempEditText)
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
								if(!activity.mCurrentFood.hasTaste()){
									activity.mCurrentFood.makeTasteGroup();
								}
								if(!tempEditText.getText().toString().equals(""))
								{
									Taste tmpTaste = new Taste();
									tmpTaste.setPreference(tempEditText.getText().toString());
									activity.mCurrentFood.getTasteGroup().setTmpTaste(tmpTaste);
								} else {
									activity.mCurrentFood.getTasteGroup().setTmpTaste(null);
								}
								
								activity.onTasteChanged(activity.mCurrentFood);
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
			
			//规格的显示
			mSpecRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					//tasteGroup没有创建则先创建
					if(!activity.mCurrentFood.hasTaste()){
						activity.mCurrentFood.makeTasteGroup();
					} 
					//清楚旧规格
					for(Taste spec : WirelessOrder.foodMenu.specs){
						activity.mCurrentFood.getTasteGroup().removeTaste(spec);
					}
					
					switch(checkedId)
					{
					case R.id.radio0:
						activity.mCurrentFood.getTasteGroup().addTaste(WirelessOrder.foodMenu.specs.get(0));
						break;
					case R.id.radio1:
						activity.mCurrentFood.getTasteGroup().addTaste(WirelessOrder.foodMenu.specs.get(1));
						break;
					case R.id.radio2:
						break;
					}
					Message msg = new Message();
					msg.what = CUR_NEW_FOOD_CHANGED;
					handleMessage(msg);
					//规格更改时显示更变
					((TextView)activity.mCurrentView.findViewById(R.id.textView_picked_food_sum_price))
						.setText(NumericUtil.float2String2(activity.mCurrentFood.calcPriceWithTaste()));
					activity.mTotalCountHandler.sendEmptyMessage(0);

					((TextView) activity.mCurrentView.findViewById(R.id.textView_picked_food_price_item))
						.setText(NumericUtil.float2String2(activity.mCurrentFood.getUnitPriceWithTaste()));
				}
			});
			//已点菜的显示项
			mPickedTasteTextView = (TextView) activity
					.findViewById(R.id.textView_pickedFood_pickedView_taste);
			mTempTasteTextView = (TextView) activity.findViewById(R.id.textView_pickedFood_tempTaste);
		}

		@Override
		public void handleMessage(Message msg) {
			final SelectedFoodActivity activity = mActivity.get();

			switch (msg.what) {
			case CUR_NEW_FOOD_CHANGED:
				if (mCurrentViewId != CUR_NEW_FOOD_CHANGED) {
					mNewView.setVisibility(View.VISIBLE);
					mPickedView.setVisibility(View.INVISIBLE);
					mCurrentViewId = CUR_NEW_FOOD_CHANGED;
				}
				// 设置菜品的各个数据

				if (activity.mCurrentFood.hasTmpTaste()) {
					mNewTempTasteTextView.setText(activity.mCurrentFood
							.getTasteGroup().getTmpTastePref());
				} else {
					mNewTempTasteTextView.setText("");
				}
				if (activity.mCurrentFood.hasNormalTaste()) {
					mNewTasteTextView.setText(activity.mCurrentFood.getTasteGroup()
							.getNormalTastePref());
				} else {
					mNewTasteTextView.setText("");
				}

				if(activity.mCurrentFood.getTasteGroup().hasSpec())
				{
					List<Taste> specs = activity.mCurrentFood.getTasteGroup().getSpecs();
					for(int i = 0; i < WirelessOrder.foodMenu.specs.size(); i++){
						if(specs.get(0).equals(WirelessOrder.foodMenu.specs.get(i)))
						{
							((RadioButton)mSpecRadioGroup.getChildAt(i)).setChecked(true);
							break;
						}
					}
				}
				break;
			case CUR_PICKED_FOOD_CHANGED:
				// 已点菜的显示
				if (mCurrentViewId != CUR_PICKED_FOOD_CHANGED) {
					mNewView.setVisibility(View.INVISIBLE);
					mPickedView.setVisibility(View.VISIBLE);
					mCurrentViewId = CUR_PICKED_FOOD_CHANGED;
				}

				if (activity.mCurrentFood.hasNormalTaste()) {
					mPickedTasteTextView.setText(activity.mCurrentFood
							.getTasteGroup().getNormalTastePref());
				} else {
					mPickedTasteTextView.setText("");
				}

				if(activity.mCurrentFood.hasTmpTaste())
					mTempTasteTextView.setText(activity.mCurrentFood.getTasteGroup().getTmpTastePref());
				else mTempTasteTextView.setText("");
				
				break;
			}
			//读取菜品图片
			if(activity.mCurrentFood.asFood().hasImage())
				activity.mImageFetcher.loadImage(activity.mCurrentFood.asFood().getImage(), mFoodImageView);
			else mFoodImageView.setImageResource(R.drawable.null_pic);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selected_food);
		mImageFetcher = new ImageFetcher(this, 400, 300);

		//根据不同的分辨率设置对话框大小 
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = 0;
		int height = 0;
		Log.i("SelectedFood", "density dpi : "+ dm.densityDpi);
		switch(dm.densityDpi){
		case DisplayMetrics.DENSITY_LOW:
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
	
			break;
		case DisplayMetrics.DENSITY_HIGH:
			 width = 600;
			 height = 450;
			break;
		case DisplayMetrics.DENSITY_XHIGH:
			width = 800;
			height = 600;
			break;
		}
		mImageFetcher.setImageSize(width, height);
		// 初始化handler
		mFoodListHandler = new FoodListHandler(this);
		mFoodDetailHandler = new FoodDetailHandler(this);
		mTotalCountHandler = new TotalCountHandler(this);
		
		//initial OptionBar
		OptionBarFragment optionbar = (OptionBarFragment) this.getFragmentManager().findFragmentById(R.id.bottombar_pickedFood);
		optionbar.setOnOrderChangeListener(this);

		mPickedFoodList = (ExpandableListView) findViewById(R.id.expandableListView_pickedFood);
//		mPickedFoodList = (LinearLayout) findViewById(R.id.linearLayout_pickedFood);

		((RelativeLayout) findViewById(R.id.relativeLayout_amount_foodDetailTab1))
				.setVisibility(View.GONE);
		((RelativeLayout) findViewById(R.id.relativeLayout_price_foodDetailTab1))
				.setVisibility(View.GONE);

		if (ShoppingCart.instance().hasTable()) {
			new QueryOrderTask(ShoppingCart.instance().getDestTable().getAliasId()).execute();

		} else {
			mFoodListHandler.sendEmptyMessage(LIST_CHANGED);
		}

		//下单按钮 
		((Button) findViewById(R.id.imageButton_submit_pickedFood)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(final View v) {				
				try{
					ShoppingCart.instance().commit(new OnCommitListener(){
						
						private ProgressDialog mProgressDialog;
						@Override
						public void OnPreCommit(Order reqOrder) {
							mProgressDialog = ProgressDialog.show(SelectedFoodActivity.this,"", "查询" + reqOrder.getDestTbl().getAliasId() + "号账单信息...请稍候");
						}

						@Override
						public void onPostCommit(Order reqOrder, BusinessException e) {
							mProgressDialog.cancel();
							if(e == null){
								//当读取到餐台锁定信息时,如果是锁定状态则不清除数据
								SharedPreferences pref = getSharedPreferences(Params.TABLE_ID, MODE_PRIVATE);
								if(!pref.contains(Params.TABLE_ID)){
									ShoppingCart.instance().clearTable();
								} else {
									ShoppingCart.instance().setOriOrder(reqOrder);
								}
								//读取服务员锁定信息
								pref = getSharedPreferences(Params.PREFS_NAME, MODE_PRIVATE);
								if(!pref.contains(Params.IS_FIX_STAFF)){
									ShoppingCart.instance().clearStaff();
								}
								
								Toast.makeText(SelectedFoodActivity.this, reqOrder.getDestTbl().getAliasId() + "号餐台下单成功", Toast.LENGTH_SHORT).show();
								
								v.postDelayed(new Runnable(){

									@Override
									public void run() {
										onBackPressed();
									}
								}, 100);
								
							}else{
								if(ShoppingCart.instance().hasOriOrder()){

									if(e.getErrCode().equals(ProtocolError.TABLE_IDLE)){
										//如果是改单，并且返回是餐台空闲的错误状态，
										//则提示用户，并清空购物车中的原账单
										new AlertDialog.Builder(SelectedFoodActivity.this)
											.setTitle("提示")
											.setMessage(reqOrder.getDestTbl().getAliasId() + "号餐台已经结帐，已点菜信息将刷新，新点菜信息将会保留")
											.setNeutralButton("确定",
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog,	int which){
														ShoppingCart.instance().setOriOrder(null);
														mFoodListHandler.sendEmptyMessage(LIST_CHANGED);
													}
												})
											.show();

										
									}else if(e.getErrCode().equals(ProtocolError.ORDER_EXPIRED)){
										//如果是改单，并且返回是账单过期的错误状态，
										//则提示用户重新请求账单，再次确认提交
										final Table destTbl = reqOrder.getDestTbl();
										new AlertDialog.Builder(SelectedFoodActivity.this)
											.setTitle("提示")
											.setMessage(reqOrder.getDestTbl().getAliasId() + "号餐台的账单信息已经更新，已点菜信息将刷新，新点菜信息将会保留")
											.setNeutralButton("确定",
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog,	int which){
														new QueryOrderTask(destTbl.getAliasId()).execute();
													}
												})
											.show();
										
									}else{
										new AlertDialog.Builder(SelectedFoodActivity.this)
											.setTitle("提示")
											.setMessage(e.getMessage())
											.setNeutralButton("确定", null)
											.show();
									}
								}else{
									if(e.getErrCode().equals(ProtocolError.TABLE_BUSY)){
										//如果是新下单，并且返回是餐台就餐的错误状态，
										//则提示用户重新请求账单，再次确认提交
										final Table destTbl = reqOrder.getDestTbl();
										new AlertDialog.Builder(SelectedFoodActivity.this)
											.setTitle("提示")
											.setMessage(reqOrder.getDestTbl().getAliasId() + "号餐台的账单信息已经更新，已点菜信息将刷新，新点菜信息将会保留")
											.setNeutralButton("确定",
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog,	int which){
														new QueryOrderTask(destTbl.getAliasId()).execute();
													}
												})
											.show();
									}else{
										new AlertDialog.Builder(SelectedFoodActivity.this)
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
					Toast.makeText(SelectedFoodActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		//设置侦听
		//当点击菜品时改变右边菜品详情的显示
		mPickedFoodList.setOnChildClickListener(new OnChildClickListener(){
			@SuppressWarnings("deprecation")
			@Override
			public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
				if(mCurrentView != null)
					mCurrentView.setBackgroundDrawable(null);
				view.setBackgroundColor(view.getResources().getColor(R.color.blue));
				
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) view.getTag();
				//点击后改变该项的颜色显示并刷新右边
				mCurrentView = view;
				mCurrentFood = (OrderFood) map.get(ITEM_THE_FOOD);
				
				if(map.containsKey(ITEM_IS_ORI_FOOD))
				{
					mFoodDetailHandler.sendEmptyMessage(SelectedFoodActivity.CUR_PICKED_FOOD_CHANGED);
				} else{
					mFoodDetailHandler.sendEmptyMessage(SelectedFoodActivity.CUR_NEW_FOOD_CHANGED);
				}
				return false;
			}
		});
		//默认右边无显示
		findViewById(R.id.layout_pickedFood_right_bottom).setVisibility(View.INVISIBLE);
	}

	/**
	 * display the picking taste dialog
	 * @param tab
	 * @param food
	 */
	protected void showDialog(String tab, final OrderFood food) {
		PickTasteFragment pickTasteFg = new PickTasteFragment();
		pickTasteFg.setOnTasteChangeListener(this);
		Bundle args = new Bundle();
		args.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(food));
		pickTasteFg.setArguments(args);
		pickTasteFg.show(getFragmentManager(), tab);
	}

	@Override
	protected void onDestroy() {
		mImageFetcher.clearCache();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// Activity关闭后不再侦听购物车变化
		ShoppingCart.instance().setOnTableChangeListener(null);
		mPickedFoodList.setOnChildClickListener(null);
		((OptionBarFragment) this.getFragmentManager().findFragmentById(R.id.bottombar_pickedFood)).setOnOrderChangeListener(null);
		super.onBackPressed();
	}

	/**
	 * 删菜、退菜的dialog
	 * @author ggdsn1
	 *
	 */
	private class AskCancelAmountDialog extends Dialog {
		static final String DELETE = "删除";
		static final String RETREAT = "退菜";

		AskCancelAmountDialog(final OrderFood selectedFood, final String method) {
			super(SelectedFoodActivity.this);

			final Context context = SelectedFoodActivity.this;
			View view = LayoutInflater.from(context).inflate(R.layout.delete_count_dialog, null);
			setContentView(view);
			this.setTitle("请输入" + method + "的数量");

			// 删除数量默认为此菜品的点菜数量
			final EditText countEdtTxt = (EditText) view.findViewById(R.id.editText_count_deleteCount);
			countEdtTxt.setText(NumericUtil.float2String2(selectedFood.getCount()));
			countEdtTxt.selectAll();
			
			// 增加数量
			((ImageButton) view.findViewById(R.id.imageButton_plus_deleteCount_dialog))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!countEdtTxt.getText().toString().equals("")) {
							float curNum = Float.parseFloat(countEdtTxt
									.getText().toString());
							countEdtTxt.setText(NumericUtil
									.float2String2(++curNum));
						}
					}
			});
			// 减少数量
			((ImageButton) findViewById(R.id.imageButton_minus_deleteCount_dialog)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!countEdtTxt.getText().toString().equals("")) {
						float curNum = Float.parseFloat(countEdtTxt.getText().toString());
						if (--curNum >= 1) {
							countEdtTxt.setText(NumericUtil.float2String2(curNum));
						}
					}
				}
			});

			// "确定"Button
			Button okBtn = (Button) view.findViewById(R.id.button_confirm_deleteCount);
			okBtn.setText("确定");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						float foodAmount = selectedFood.getCount();
						float cancelAmount = Float.parseFloat(countEdtTxt.getText().toString());

						if (foodAmount == cancelAmount) {
							/**
							 * 如果数量相等，则从列表中删除此菜
							 */
							if (method.equals(DELETE)){
								ShoppingCart.instance().remove(selectedFood, WirelessOrder.loginStaff);
							}else if (method.equals(RETREAT)) {
								selectedFood.removeCount(cancelAmount, WirelessOrder.loginStaff);
							}

							// 若完全没有菜式，则关闭该activity
							if (!ShoppingCart.instance().hasOrder()) {
								onBackPressed();
								SelectedFoodActivity.this.onBackPressed();
							} else {
								mFoodListHandler.sendEmptyMessage(SelectedFoodActivity.LIST_CHANGED);
								dismiss();
								Toast.makeText(context,	
											   method + "\"" + selectedFood.toString() + "\"" + cancelAmount + "份成功",
											   Toast.LENGTH_SHORT).show();
							}

						} else if (foodAmount > cancelAmount) {
							/**
							 * 如果删除数量少于已点数量，则相应减去删除数量
							 */
							selectedFood.removeCount(cancelAmount, WirelessOrder.loginStaff);
							mFoodListHandler.sendEmptyMessage(SelectedFoodActivity.LIST_CHANGED);
							dismiss();
							Toast.makeText(context,
										   method + "\"" + selectedFood.toString() + "\"" + cancelAmount + "份成功",
										   Toast.LENGTH_SHORT).show();

						} else {
							Toast.makeText(context,
										  "输入的" + method + "数量大于已点数量, 请重新输入",
										  Toast.LENGTH_SHORT).show();
						}

					} catch (NumberFormatException e) {
						Toast.makeText(context, "你输入的" + method + "数量不正确", Toast.LENGTH_SHORT).show();
					} catch (BusinessException e) {
						Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
					}

				}
			});

			// "取消"Button
			Button cancelBtn = (Button) view.findViewById(R.id.button_cancel_deleteCount);
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

		public QueryOrderTask(int tableAlias) {
			super(WirelessOrder.loginStaff, tableAlias, WirelessOrder.foodMenu);
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onPostExecute(Order order) {
			// 请求成功后设置购物车并更新
			ShoppingCart.instance().setOriOrder(order);
			mFoodListHandler.sendEmptyMessage(LIST_CHANGED);
		}
	}

	/**
	 * when the taste was changed, refresh the food in cart and total display
	 */
	@Override
	public void onTasteChanged(OrderFood food) {
		try {
			ShoppingCart.instance().remove(mCurrentFood, WirelessOrder.loginStaff);
			mCurrentFood = food;
			
			ShoppingCart.instance().addFood(mCurrentFood);
			
			mFoodDetailHandler.sendEmptyMessage(SelectedFoodActivity.CUR_NEW_FOOD_CHANGED);
			
			((TextView) mCurrentView.findViewById(R.id.textView_picked_food_price_item)).setText(NumericUtil.float2String2(mCurrentFood.getUnitPriceWithTaste()));
			((TextView) mCurrentView.findViewById(R.id.textView_picked_food_sum_price)).setText(NumericUtil.float2String2(mCurrentFood.calcPriceWithTaste()));
			
			mTotalCountHandler.sendEmptyMessage(0);
			
		} catch (BusinessException e) {
			Toast.makeText(SelectedFoodActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * according to the tab, show different tab in the {@link PickTasteFragment}
	 * @author ggdsn1
	 *
	 */
	class PickedFoodOnClickListener implements OnClickListener {
		private String mTab;

		public PickedFoodOnClickListener(String mTab) {
			this.mTab = mTab;
		}

		@Override
		public void onClick(View v) {
			showDialog(mTab, mCurrentFood);
		}
	}

	/**
	 * if the order changed ,refresh the food list
	 */
	@Override
	public void onOrderChanged(Order order) {
		mFoodListHandler.sendEmptyMessage(SelectedFoodActivity.LIST_CHANGED);
	}
	
	/**
	 * the handler which is use to refresh total prices and amount
	 * @author ggdsn1
	 *
	 */
	private static class TotalCountHandler extends Handler{
		
		private TextView mTotalCountTextView;
		private TextView mTotalPriceTextView;
		
		public TotalCountHandler(SelectedFoodActivity activity) {
			mTotalCountTextView = (TextView) activity.findViewById(R.id.textView_total_count_pickedFood);
			mTotalPriceTextView = (TextView) activity.findViewById(R.id.textView_total_price_pickedFood);
		}
		@Override
		public void handleMessage(Message msg) {
			mTotalCountTextView.setText(NumericUtil.float2String2(ShoppingCart.instance().getTotalCount()));
			mTotalPriceTextView.setText(NumericUtil.float2String2(ShoppingCart.instance().getTotalPrice()));
		}
		
	}

	/**
	 * when the temp food add, refresh the food list
	 */
	@Override
	public void onFoodAdd(Food food) {
		mFoodListHandler.sendEmptyMessage(SelectedFoodActivity.LIST_CHANGED);
	}
}

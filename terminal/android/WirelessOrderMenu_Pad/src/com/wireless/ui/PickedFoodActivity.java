package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
import com.wireless.protocol.Util;
import com.wireless.util.ProgressToast;
import com.wireless.util.imgFetcher.ImageFetcher;

public class PickedFoodActivity extends Activity implements OnOrderChangeListener, OnTasteChangeListener{
	//列表项的显示标签
	private static final String ITEM_FOOD_NAME = "item_food_name";
	private static final String ITEM_FOOD_PRICE = "item_food_price";
	private static final String ITEM_FOOD_COUNT = "item_food_count";
//	private static final String ITEM_FOOD_STATE = "item_food_state";
	private static final String ITEM_THE_FOOD = "theFood";
	
	private static final String ITEM_GROUP_NAME = "itemGroupName";

	private static final String[] GROUP_ITEM_TAGS = {
		ITEM_GROUP_NAME
	};
	
	private static final int[] GROUP_ITEM_ID = {
		R.id.textView_groupName_pickedFood_list_item
	};
	
	private static final String[] ITEM_TAGS = {
		ITEM_FOOD_NAME, 
		ITEM_FOOD_PRICE, 
		ITEM_FOOD_COUNT,
//		ITEM_FOOD_STATE 
	};
	
	private static final int[] ITEM_ID = {
		R.id.textView_picked_food_name_item,
		R.id.textView_picked_food_price_item,
		R.id.textView_picked_food_count_item,
//		R.id.textView_picked_food_state_item
	};
	protected static final int CUR_FOOD_CHANGED = 388962;//删菜标记
	protected static final int LIST_CHANGED = 878633;//已点菜更新标记
//	protected static final String CUR_FOOD_CHANGED = "cur_food_changed";
	
	private FoodHandler mFoodHandler;
	private FoodDetailHandler mFoodDataHandler;

	private ExpandableListView mPickedFoodList;
	private OrderFood mCurFood;
	
	private ImageFetcher mImageFetcher;


	/*
	 * 显示已点菜的列表的handler
	 * 负责更新已点菜的显示
	 */
	private static class FoodHandler extends Handler{
		private WeakReference<PickedFoodActivity> mActivity;
		private TextView mTotalCountTextView;
		private TextView mTotalPriceTextView;
		private float mTotalPrice = 0;

		FoodHandler(PickedFoodActivity activity)
		{
			mActivity = new WeakReference<PickedFoodActivity>(activity);
			mTotalCountTextView = (TextView) activity.findViewById(R.id.textView_total_count_pickedFood);
			mTotalPriceTextView = (TextView)  activity.findViewById(R.id.textView_total_price_pickedFood);
		}
		
		@Override
		public void handleMessage(Message msg)
		{
			final PickedFoodActivity activity = mActivity.get();
			ShoppingCart sCart = ShoppingCart.instance();

			int totalCount = 0;
			mTotalPrice = 0;

			final List<Map<String, ?>> groupData = new ArrayList<Map<String, ?>>();
			final List<List<Map<String, ?>>> childData =  new ArrayList<List<Map<String, ?>>>();
			//若包含菜单，则将已点菜添加进列表
			if(sCart.hasOriOrder()){
				List<OrderFood> oriFoods = ShoppingCart.instance().getOriFoods();
				totalCount += oriFoods.size();
				List<Map<String, ?>> pickedFoodDatas = new ArrayList<Map<String,?>>();
				for(OrderFood f : oriFoods)
				{
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(ITEM_FOOD_NAME, f.name);
					map.put(ITEM_FOOD_PRICE, String.valueOf(f.getPriceWithTaste()));
					map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
					map.put(ITEM_THE_FOOD, f);
					pickedFoodDatas.add(map);
//					mTotalPrice += f.getPriceWithTaste() * f.getCount(); 
				}
				childData.add(pickedFoodDatas);
				
				mTotalPrice = sCart.getOriOrder().calcPriceWithTaste();
				
				HashMap<String, Object> map1 = new HashMap<String, Object>();
				map1.put(ITEM_GROUP_NAME, "已点菜");
				groupData.add(map1);
			}
			//若包含新点菜，则将新点菜添加进列表
			if(sCart.hasNewOrder()){
				List<OrderFood> newFoods = sCart.getNewFoods();
				totalCount += newFoods.size();
				
				List<Map<String, ?>> newFoodDatas = new ArrayList<Map<String,?>>();
				for(OrderFood f : newFoods)
				{
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(ITEM_FOOD_NAME, f.name);
					map.put(ITEM_FOOD_PRICE, String.valueOf(f.getPriceWithTaste()));
					map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
					map.put(ITEM_THE_FOOD, f);
					newFoodDatas.add(map);

				}
				childData.add(newFoodDatas);
				mTotalPrice += sCart.getNewOrder().calcPriceWithTaste();
				
				HashMap<String, Object> map2 = new HashMap<String, Object>();
				map2.put(ITEM_GROUP_NAME, "新点菜");
				groupData.add(map2);
			}

			mTotalCountTextView.setText("" + totalCount);
			mTotalPriceTextView.setText(Util.float2String2(mTotalPrice));
			
			final View optionLayout = LayoutInflater.from(activity).inflate(R.layout.picked_food_option_popup_window, null);

			//创建ListView的adapter
			SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(activity.getApplicationContext(), 
					groupData, R.layout.picked_food_list_group_item, GROUP_ITEM_TAGS, GROUP_ITEM_ID, 
					childData, R.layout.picked_food_list_item, ITEM_TAGS, ITEM_ID){
				@Override
				public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
					View view = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
					
					final OrderFood orderFood = (OrderFood) childData.get(groupPosition).get(childPosition).get(ITEM_THE_FOOD);
					view.setTag(orderFood);
					//数量显示
					final Button countEditText = (Button) view.findViewById(R.id.textView_picked_food_count_item);
					countEditText.setText(Util.float2String2(orderFood.getCount()));
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
												mTotalPrice += orderFood.getPriceWithTaste();
												mTotalPriceTextView.setText(Util.float2String2(mTotalPrice));
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
					((ImageButton) view.findViewById(R.id.imageButton_plus_pickedFood_item)).setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							float curNum = Float.parseFloat(countEditText.getText().toString());
							countEditText.setText(Util.float2String2(++curNum));
							orderFood.setCount(curNum);
							mTotalPrice += orderFood.getPriceWithTaste();
							mTotalPriceTextView.setText(Util.float2String2(mTotalPrice));
 
						}
					});
					//数量减按钮
					((ImageButton) view.findViewById(R.id.imageButton_minus_pickedFood_item)).setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							float curNum = Float.parseFloat(countEditText.getText().toString());
							if(--curNum >= 1)
							{
								countEditText.setText(Util.float2String2(curNum));
								orderFood.setCount(curNum);
								mTotalPrice -= orderFood.getPriceWithTaste();
								mTotalPriceTextView.setText(Util.float2String2(mTotalPrice));
							}
						}
					});
					//催菜显示、叫起/即起、删除按钮 初始化
					final TextView stateHurryTextView = (TextView) view.findViewById(R.id.textView_picked_food_state_hurry_item);
					final TextView statusHangUpTextView = (TextView) view.findViewById(R.id.textView_hangup_pickedFood_list_item);
					Button deleteBtn = (Button) view.findViewById(R.id.button_delete_pickedFood_list_item);
					//催菜状态显示
					if(orderFood.isHurried)
						stateHurryTextView.setVisibility(View.VISIBLE);
					else stateHurryTextView.setVisibility(View.INVISIBLE);
					
					//已点菜部分
					if(groupData.size() == 1 && groupData.get(0).containsValue("已点菜") || groupData.size() == 2 && groupPosition == 0)
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
						//判断叫起状态，显示当前状态
						if(orderFood.hangStatus == OrderFood.FOOD_HANG_UP)
						{
							statusHangUpTextView.setText("叫起");
							statusHangUpTextView.setVisibility(View.VISIBLE);
						}
						else if(orderFood.hangStatus == OrderFood.FOOD_IMMEDIATE) {
							statusHangUpTextView.setText("即起");
							statusHangUpTextView.setVisibility(View.VISIBLE); 
						} else 	statusHangUpTextView.setVisibility(View.INVISIBLE); 
					//新点菜部分
					} else{
						//删除按钮
						deleteBtn.setText("删除");
						deleteBtn.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								activity.new AskCancelAmountDialog(orderFood, AskCancelAmountDialog.DELETE).show();
							}
						});
						//显示叫起按钮
						statusHangUpTextView.setText("叫起");
						if(orderFood.hangStatus == OrderFood.FOOD_HANG_UP)
							statusHangUpTextView.setVisibility(View.VISIBLE);
						else statusHangUpTextView.setVisibility(View.INVISIBLE); 
					}
					
					//操作按钮
					((Button) view.findViewById(R.id.button_operation_pickedFood_list_item)).setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							//初始化弹出框
							final PopupWindow popup = new PopupWindow(optionLayout,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
							popup.setOutsideTouchable(true);
							popup.setBackgroundDrawable(new BitmapDrawable());
							popup.update();
							
							if(popup.isShowing())
								popup.dismiss();
							else {
								popup.showAsDropDown(v, -40,0);
								//催菜按钮
								((Button) popup.getContentView().findViewById(R.id.button_hurry_option_popupWindow)).setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										if(orderFood.isHurried){
											orderFood.isHurried = false;
											Toast.makeText(activity, orderFood.name+" 取消催菜成功", Toast.LENGTH_SHORT).show();
											stateHurryTextView.setVisibility(View.INVISIBLE);
										}else{
											orderFood.isHurried = true;
											Toast.makeText(activity, orderFood.name + "催菜成功", Toast.LENGTH_SHORT).show();
											stateHurryTextView.setVisibility(View.VISIBLE);
										}
										popup.dismiss();
									}
								});
								//叫起 即起按钮
								Button hangUpBtn = (Button) popup.getContentView().findViewById(R.id.button_hangUp_option_popupWindow);
								//已点菜状态
								if(groupData.size() == 1 && groupData.get(0).containsValue("已点菜") || groupData.size() == 2 && groupPosition == 0)
								{
									//若是叫起、即起状态，则显示按钮
									if(orderFood.hangStatus == OrderFood.FOOD_HANG_UP || orderFood.hangStatus == OrderFood.FOOD_IMMEDIATE){
										hangUpBtn.setVisibility(View.VISIBLE);
										//根据不同状态设置侦听器
										if(orderFood.hangStatus == OrderFood.FOOD_HANG_UP){
											hangUpBtn.setText("即起");
											hangUpBtn.setOnClickListener(new View.OnClickListener() {
												@Override
												public void onClick(View v) {
													orderFood.hangStatus = OrderFood.FOOD_IMMEDIATE;
													statusHangUpTextView.setText("即起");
													popup.dismiss();
												}
											});
										} else if(orderFood.hangStatus == OrderFood.FOOD_IMMEDIATE){
											hangUpBtn.setText("叫起");
											hangUpBtn.setOnClickListener(new View.OnClickListener() {
												@Override
												public void onClick(View v) {
													orderFood.hangStatus = OrderFood.FOOD_HANG_UP;
													statusHangUpTextView.setText("叫起");
													popup.dismiss();
												}
											});
										}
									//若不是叫起状态则不显示
									} else{
										hangUpBtn.setVisibility(View.GONE);
									}
								//新点菜状态
								} else {
									hangUpBtn.setVisibility(View.VISIBLE);
									hangUpBtn.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View v) {
											//根据当前状态改变hangstatus状态
											if(orderFood.hangStatus == OrderFood.FOOD_NORMAL){
												orderFood.hangStatus = OrderFood.FOOD_HANG_UP;
												statusHangUpTextView.setVisibility(View.VISIBLE);
											}else{
												orderFood.hangStatus = OrderFood.FOOD_NORMAL;
												statusHangUpTextView.setVisibility(View.INVISIBLE);
											}
											popup.dismiss();
										}
									});
								}
							}
						}
					}); 
					
					return view;
				}
			};
			activity.mPickedFoodList.setAdapter(adapter);
			//展开所有列表
			for(int i=0;i<groupData.size();i++)
			{
				activity.mPickedFoodList.expandGroup(i);
			}
			//设置侦听
			//当点击菜品是改变右边菜品详情的显示
			activity.mPickedFoodList.setOnChildClickListener(new OnChildClickListener(){
				@Override
				public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
					if(parent.getTag() != null)
					{
						((View)(parent.getTag())).setBackgroundDrawable(null);
					}
					parent.setTag(view);
					//点击后改变该项的颜色显示并刷新右边
					activity.mCurFood = (OrderFood) view.getTag();
					activity.mFoodDataHandler.sendEmptyMessage(PickedFoodActivity.CUR_FOOD_CHANGED);
					
					view.setBackgroundColor(view.getResources().getColor(R.color.blue));
					return false;
				}
			});
			//默认第一个设置为选中
			//TODO 再修正一下点菜流程
			activity.mPickedFoodList.postDelayed(new Runnable(){
				@Override
				public void run() {
					activity.mPickedFoodList.performItemClick(activity.mPickedFoodList.getChildAt(1), 1, 1);
				}
			}, 200);
			
			((ProgressBar) activity.findViewById(R.id.progressBar_pickedFood)).setVisibility(View.GONE);
		}
	}
	/*
	 * 负责显示右边菜品详情的handler
	 */
	private static class FoodDetailHandler extends Handler{
		private WeakReference<PickedFoodActivity> mActivity;
		private ImageView mFoodImageView;
		private TextView mTasteTextView;
		private TextView mTempTasteTextView;
		private ImageButton mTempTasteBtn;
		private ImageButton mPickTasteBtn;
		private RadioGroup mRadioGroup;
		
		FoodDetailHandler(final PickedFoodActivity activity)
		{
			mActivity = new WeakReference<PickedFoodActivity>(activity);
			
			mFoodImageView = (ImageView) activity.findViewById(R.id.imageView_selected_food_pickedFood);
			
			mTempTasteTextView  = (TextView) activity.findViewById(R.id.textView_pinzhu_foodDetail);
			mTasteTextView = (TextView) activity.findViewById(R.id.textView_pickedTaste_foodDetail);
			
			//打开菜品选择对话框
			mPickTasteBtn = (ImageButton) activity.findViewById(R.id.button_pickTaste_foodDetail);
			mPickTasteBtn.setOnClickListener(activity.new PickedFoodOnClickListener(PickTasteFragment.FOCUS_TASTE));
			//品注按钮
			mTempTasteBtn = (ImageButton) activity.findViewById(R.id.button_pinzhu_foodDetail);
			mTempTasteBtn.setOnClickListener(activity.new PickedFoodOnClickListener(PickTasteFragment.FOCUS_NOTE));
			
			mRadioGroup = (RadioGroup) activity.findViewById(R.id.radioGroup_foodDetail);
		}
		
		@Override
		public void handleMessage(Message Msg)
		{
			final PickedFoodActivity activity = mActivity.get();
			//设置菜品的各个数据
			activity.mImageFetcher.loadImage(activity.mCurFood.image, mFoodImageView);
			
			if(activity.mCurFood.hasTmpTaste()){
				mTempTasteTextView.setText(activity.mCurFood.tasteGroup.getTmpTastePref());
			}else{
				mTempTasteTextView.setText("");
			}
			if(activity.mCurFood.hasNormalTaste()){
				mTasteTextView.setText(activity.mCurFood.tasteGroup.getNormalTastePref());
			}else{
				mTasteTextView.setText("");
			}
			
			//清空品注
			((ImageButton) activity.findViewById(R.id.button_removeAllTaste)).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					activity.mCurFood.tasteGroup = null;
					mTempTasteTextView.setText("");
					mTasteTextView.setText("");
				}
			});
			//TODO 添加规格的显示
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.picked_food);
		
		mImageFetcher = new ImageFetcher(this, 300, 225);

		//初始化handler
		mFoodHandler = new FoodHandler(this);
		mFoodDataHandler = new FoodDetailHandler(this);
		((OptionBarFragment)this.getFragmentManager().findFragmentById(R.id.bottombar_pickedFood)).setOnOrderChangeListener(this);
		
		mPickedFoodList = (ExpandableListView) findViewById(R.id.expandableListView_pickedFood);
		
		((RelativeLayout) findViewById(R.id.relativeLayout_amount_foodDetailTab1)).setVisibility(View.GONE);
		((RelativeLayout) findViewById(R.id.relativeLayout_price_foodDetailTab1)).setVisibility(View.GONE);
		
		if(ShoppingCart.instance().hasTable()){
			new com.wireless.lib.task.QueryOrderTask(ShoppingCart.instance().getDestTable().aliasID){
				
				private ProgressToast mToast;
				
				@Override
				protected void onPreExecute(){
					mToast = ProgressToast.show(PickedFoodActivity.this, "查询" + mTblAlias + "号账单信息...请稍候");
				}
				
				@Override
				protected void onPostExecute(Order order){
					mToast.cancel();				
					//更新购物车
					ShoppingCart.instance().setOriOrder(order);
					mFoodHandler.sendEmptyMessage(LIST_CHANGED);
				}
			}.execute(WirelessOrder.foodMenu);
			
		}else{
			mFoodHandler.sendEmptyMessage(LIST_CHANGED);
		}
		
		//下单按钮
		((Button) findViewById(R.id.imageButton_submit_pickedFood)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {				
				try{
					
					ShoppingCart.instance().commit(new OnCommitListener(){
						
						private ProgressToast mToast;
						
						@Override
						public void OnPreCommit(Order reqOrder) {
							mToast = ProgressToast.show(PickedFoodActivity.this, "查询" + reqOrder.destTbl.aliasID + "号账单信息...请稍候");
						}

						@Override
						public void onPostCommit(Order reqOrder, BusinessException e) {
							mToast.cancel();
							if(e == null){
								Toast.makeText(PickedFoodActivity.this, reqOrder.destTbl.aliasID + "号餐台下单成功", Toast.LENGTH_SHORT).show();
//								((OptionBarFragment) getFragmentManager().findFragmentById(R.id.bottombar_pickedFood)).onTableChanged(reqOrder.destTbl);

								finish();
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
	}
	
	protected void showDialog(String tab, final OrderFood food) {
		PickTasteFragment pickTasteFg = new PickTasteFragment();
		pickTasteFg.setOnTasteChangeListener(this);
		Bundle args = new Bundle();
		args.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(food));
		pickTasteFg.setArguments(args);
		pickTasteFg.show(getFragmentManager(), tab);
	}


	
	//activity关闭后不再侦听购物车变化
	@Override
	public void finish(){
		super.finish();
		ShoppingCart.instance().setOnTableChangeListener(null);
		mPickedFoodList.setOnChildClickListener(null);
	}
	
	@Override
	protected void onDestroy() {
        mImageFetcher.clearCache();
		super.onDestroy();
	}

	private class AskCancelAmountDialog extends Dialog{
		static final String DELETE = "删除";
		static final String RETREAT = "退菜";
		AskCancelAmountDialog(final OrderFood selectedFood, final String method) {
			super(PickedFoodActivity.this);
		
			final Context context = PickedFoodActivity.this;
			View view = LayoutInflater.from(context).inflate(R.layout.delete_count_dialog, null);
			setContentView(view);
			this.setTitle("请输入" + method + "的数量");

			//删除数量默认为此菜品的点菜数量
			final EditText countEdtTxt = (EditText)view.findViewById(R.id.editText_count_deleteCount);			
			countEdtTxt.setText("" + selectedFood.getCount());
			//增加数量
			((ImageButton) view.findViewById(R.id.imageButton_plus_deleteCount_dialog)).setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					float curNum = Float.parseFloat(countEdtTxt.getText().toString());
					countEdtTxt.setText("" + ++curNum);
				}
			});
			//减少数量
			((ImageButton) findViewById(R.id.imageButton_minus_deleteCount_dialog)).setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					float curNum = Float.parseFloat(countEdtTxt.getText().toString());
					if(--curNum >= 1)
					{
						countEdtTxt.setText("" + curNum);
					}
				}
			});
			
			//"确定"Button
			Button okBtn = (Button)view.findViewById(R.id.button_confirm_deleteCount);
			okBtn.setText("确定");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try{
						float foodAmount = selectedFood.getCount();
						float cancelAmount = Float.parseFloat(countEdtTxt.getText().toString());
						
						if(foodAmount == cancelAmount){
							/**
							 * 如果数量相等，则从列表中删除此菜
							 */
							if(method.equals(DELETE))
								ShoppingCart.instance().remove(selectedFood);
							else if(method.equals(RETREAT))
								ShoppingCart.instance().getOriOrder().remove(selectedFood);
							
							//若完全没有菜式，则关闭该activity
							if(!ShoppingCart.instance().hasOrder()){
								onBackPressed();
								PickedFoodActivity.this.onBackPressed();
							} else{
								mFoodHandler.sendEmptyMessage(PickedFoodActivity.LIST_CHANGED);
								dismiss();
								Toast.makeText(context, method+"\"" + selectedFood.toString() + "\"" + cancelAmount + "份成功", Toast.LENGTH_SHORT).show();
							}
							
						}else if(foodAmount > cancelAmount){
							/**
							 * 如果删除数量少于已点数量，则相应减去删除数量
							 */
							selectedFood.setCount(foodAmount - cancelAmount);
							mFoodHandler.sendEmptyMessage(PickedFoodActivity.LIST_CHANGED);
							dismiss();
							Toast.makeText(context, method+"\"" + selectedFood.toString() + "\"" + cancelAmount + "份成功", Toast.LENGTH_SHORT).show();
							
						}else{
							Toast.makeText(context, "输入的"+method+"数量大于已点数量, 请重新输入", Toast.LENGTH_SHORT).show();
						}
						
					}catch(NumberFormatException e){
						Toast.makeText(context, "你输入的"+method+"数量不正确", Toast.LENGTH_SHORT).show();
					}

				}
			});
			
			//"取消"Button
			Button cancelBtn = (Button)view.findViewById(R.id.button_cancel_deleteCount);
			cancelBtn.setText("取消");
			cancelBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		}		
	}
	
	
	
	private class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{
		private ProgressToast mToast;
		public QueryOrderTask(int tableAlias) {
			super(tableAlias);
		}
		
		@Override
		protected void onPreExecute(){
			mToast = ProgressToast.show(PickedFoodActivity.this, "查询" + mTblAlias + "号账单信息...请稍候");
		}
		
		@Override
		protected void onPostExecute(Order order){
			mToast.cancel();
			
			//请求成功后设置购物车并更新
			ShoppingCart.instance().setOriOrder(order);
			mFoodHandler.sendEmptyMessage(LIST_CHANGED);
		}
	}
	
	@Override
	public void onTasteChange(OrderFood food) {
		ShoppingCart.instance().getNewOrder().remove(mCurFood);
		mCurFood = food;
		try {
			ShoppingCart.instance().getNewOrder().addFood(mCurFood);
		} catch (BusinessException e) {
			e.printStackTrace();
		}
		mFoodHandler.sendEmptyMessage(LIST_CHANGED);
	}
	
	class PickedFoodOnClickListener implements OnClickListener{
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
}

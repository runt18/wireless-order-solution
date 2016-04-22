package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.common.ShoppingCart;
import com.wireless.common.ShoppingCart.OnCartChangedListener;
import com.wireless.common.ShoppingCart.OnCommitListener;
import com.wireless.common.ShoppingCart.OnPayListener;
import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FrontBusinessError;
import com.wireless.fragment.PickTasteFragment;
import com.wireless.fragment.PickTasteFragment.OnTasteChangeListener;
import com.wireless.ordermenu.R;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.util.SearchFoodHandler;
import com.wireless.util.SearchFoodHandler.OnFoodAddListener;
import com.wireless.util.imgFetcher.ImageFetcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SelectedFoodActivity extends Activity 
								  implements OnCartChangedListener, OnTasteChangeListener, OnFoodAddListener {
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

	private final List<Map<String, ?>> groupData = new ArrayList<Map<String, ?>>();
	private final List<List<Map<String, ?>>> childData =  new ArrayList<List<Map<String, ?>>>();
	
	/*
	 * the tags use to build adapter
	 */
	private static final String[] GROUP_ITEM_TAGS = { 
		ITEM_GROUP_NAME
	};

	private static final int[] GROUP_ITEM_ID = { 
		R.id.textView_groupName_pickedFood_list_item 
	};

	private static final String[] CHILD_ITEM_TAGS = { 
		ITEM_FOOD_NAME,
		ITEM_FOOD_ORI_PRICE, 
		ITEM_FOOD_COUNT, 
		ITEM_FOOD_SUM_PRICE
	};

	private static final int[] CHILD_ITEM_ID = { 
		R.id.textView_picked_food_name_item,
		R.id.textView_picked_food_price_item,
		R.id.textView_picked_food_count_item,
		R.id.textView_picked_food_sum_price
	};
	
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
	
	/**
	 * the handler which is use to refresh total prices and amount
	 * @author ggdsn1
	 */
	private static class TotalCountHandler extends Handler{
		
		private TextView mTotalPriceTextView;
		
		public TotalCountHandler(SelectedFoodActivity activity) {
			mTotalPriceTextView = (TextView) activity.findViewById(R.id.textView_total_price_pickedFood);
		}
		@Override
		public void handleMessage(Message msg) {
			mTotalPriceTextView.setText(NumericUtil.float2String2(ShoppingCart.instance().getTotalPrice()));
		}
		
	}
	
	/**
	 * 显示已点菜的列表的handler 负责更新已点菜的显示
	 */
	private static class FoodListHandler extends Handler {
		
		private WeakReference<SelectedFoodActivity> mActivity;

		FoodListHandler(SelectedFoodActivity activity) {
			mActivity = new WeakReference<SelectedFoodActivity>(activity);
		}

		/**
		 * package all new foods and original foods and set the ExpandableListAdapter
		 */
		@Override
		public void handleMessage(Message msg){
			final SelectedFoodActivity activity = mActivity.get();
			
			activity.mTotalCountHandler.sendEmptyMessage(0);
			
			prepareAdaptorData(activity);
			
			for(int i = 0; i < activity.mPickedFoodList.getExpandableListAdapter().getGroupCount(); i++){
//				if(activity.mPickedFoodList.collapseGroup(i)){
//					activity.mPickedFoodList.expandGroup(i);
//				}
				activity.mPickedFoodList.collapseGroup(i);
				activity.mPickedFoodList.expandGroup(i);
			}
		}
		
		private void prepareAdaptorData(SelectedFoodActivity activity){
			
			activity.groupData.clear();
			activity.childData.clear();
			
			HashMap<String, Object> map2 = new HashMap<String, Object>();
			map2.put(ITEM_GROUP_NAME, "新点菜");
			activity.groupData.add(map2);
			
			List<Map<String, ?>> newFoodDatas = new ArrayList<Map<String,?>>();
			//若包含新点菜，则将新点菜添加进列表
			if(ShoppingCart.instance().hasNewOrder()){
				for(OrderFood f : ShoppingCart.instance().getNewFoods()){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put(ITEM_FOOD_NAME, f.getName());
					map.put(ITEM_FOOD_ORI_PRICE, String.valueOf(NumericUtil.float2String2(f.calcUnitPrice())));
					map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
					map.put(ITEM_FOOD_SUM_PRICE, String.valueOf(NumericUtil.float2String2(f.calcPrice())));
					map.put(ITEM_THE_FOOD, f);
					newFoodDatas.add(map);
				}
			}
			activity.childData.add(newFoodDatas);


			//若包含菜单，则将已点菜添加进列表
			if(ShoppingCart.instance().hasOriOrder()){
				Map<String, Object> map1 = new HashMap<String, Object>();
				map1.put(ITEM_GROUP_NAME, "已点菜");
				activity.groupData.add(map1);
				List<Map<String, ?>> pickedFoodDatas = new ArrayList<Map<String,?>>();
				for(OrderFood f : ShoppingCart.instance().getOriFoods()){
					if(f.getCount() != 0f){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put(ITEM_IS_ORI_FOOD, true);
						map.put(ITEM_FOOD_NAME, f.getName());
						map.put(ITEM_FOOD_ORI_PRICE, NumericUtil.float2String2(f.calcUnitPrice()));
						map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
						map.put(ITEM_FOOD_SUM_PRICE, NumericUtil.float2String2(f.calcPrice()));
						map.put(ITEM_THE_FOOD, f);
						pickedFoodDatas.add(map);
					}
					if(f.getDelta() > 0f){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put(ITEM_IS_ORI_FOOD, true);
						map.put(ITEM_FOOD_NAME, f.getName());
						map.put(ITEM_FOOD_ORI_PRICE, NumericUtil.float2String2(f.calcUnitPrice()));
						map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
						map.put(ITEM_FOOD_SUM_PRICE, NumericUtil.float2String2(f.calcPrice()));
						map.put(ITEM_THE_FOOD, f);
						map.put(ITEM_IS_OFFSET, true);
						map.put(ITEM_FOOD_OFFSET, NumericUtil.float2String2(f.getDelta()));
						pickedFoodDatas.add(map);
					}
				}
				activity.childData.add(pickedFoodDatas);
			}
			
			//刷新Adapter显示
			((BaseExpandableListAdapter)activity.mPickedFoodList.getExpandableListAdapter()).notifyDataSetChanged();

		}
	}

	/**
	 * 负责显示右边菜品详情的handler
	 */
	private static class FoodDetailHandler extends Handler {
		private WeakReference<SelectedFoodActivity> mActivity;

		private Food mDisplayedFood;
		
		FoodDetailHandler(final SelectedFoodActivity activity) {
			mActivity = new WeakReference<SelectedFoodActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			final SelectedFoodActivity activity = mActivity.get();

			if(!activity.mCurrentFood.asFood().equals(mDisplayedFood)){
				mDisplayedFood = activity.mCurrentFood.asFood();
				//显示菜品图片
				mDisplayedFood = WirelessOrder.foodMenu.foods.find(mDisplayedFood);
				((TextView)activity.findViewById(R.id.txtView_selectedFoodName_pickedFood)).setText(mDisplayedFood.getName());
				((TextView)activity.findViewById(R.id.txtView_selectedFoodPrice_pickedFood)).setText(NumericUtil.float2String2(mDisplayedFood.getPrice()) + "元");
				ImageView foodImgView = (ImageView)activity.findViewById(R.id.imageView_selected_food_pickedFood);
				if(mDisplayedFood.hasImage()){
					activity.mImageFetcher.loadImage(mDisplayedFood.getImage().getImage(), foodImgView);
				}else{
					foodImgView.setImageResource(R.drawable.null_pic);
				}
			}
		}
	}

	private void initPickedFoodList(){

		//创建ListView的adapter
		SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(getApplicationContext(), 
				groupData, R.layout.picked_food_list_group_item, GROUP_ITEM_TAGS, GROUP_ITEM_ID, 
				childData, R.layout.picked_food_list_item, CHILD_ITEM_TAGS, CHILD_ITEM_ID){
			
			@Override
			public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
				View layout = super.getGroupView(groupPosition, isExpanded, convertView, parent);
				//initial groupView buttons
				EditText searchEditText = (EditText) layout.findViewById(R.id.editText_SelectedFood_listGroup_item_search);
				Button clearSearchBtn = (Button) layout.findViewById(R.id.button_selectedFoodListGroup_item_clear);
				//set search handler
				mSearchFoodHandler = new SearchFoodHandler(SelectedFoodActivity.this, searchEditText, clearSearchBtn);
				mSearchFoodHandler.setOnFoodAddListener(SelectedFoodActivity.this);
				
				switch(groupPosition){
				//new foods group item
				case 0:
					searchEditText.setVisibility(View.VISIBLE);
					//添加临时菜按钮
					break;
				//original foods group item
				case 1:
					searchEditText.setVisibility(View.GONE);
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
				if(groupPosition == 0){
					return ShoppingCart.instance().hasNewOrder() ? super.getChildrenCount(groupPosition) : 0;
				}else{
					return super.getChildrenCount(groupPosition);
				}
			}

			@Override
			public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
				Map<String, ?> map = childData.get(groupPosition).get(childPosition);
				final OrderFood orderFood = (OrderFood) map.get(ITEM_THE_FOOD);
				
				final View layout;
				if(convertView != null){
					layout = convertView;
				}else{
					layout = getLayoutInflater().inflate(R.layout.picked_food_list_item, parent, false);
				}
				
				layout.setTag(map);
				
				//高亮显示选中菜品
				if(orderFood.equals(mCurrentFood)){
					layout.setBackgroundColor(getResources().getColor(R.color.blue));
					//刷新右侧显示选中菜品的详情
					mFoodDetailHandler.sendEmptyMessage(0);
				}else{
					layout.setBackgroundColor(Color.WHITE);
				}
				
				//设置菜品基本数据的显示
				((TextView) layout.findViewById(R.id.textView_picked_food_name_item)).setText(orderFood.getName());
				((TextView) layout.findViewById(R.id.textView_picked_food_price_item)).setText(NumericUtil.float2String2(orderFood.calcUnitPrice()));

				//数量显示
				final Button countEditText = (Button) layout.findViewById(R.id.textView_picked_food_count_item);
				final TextView sumPriceTextView = (TextView) layout.findViewById(R.id.textView_picked_food_sum_price);
				countEditText.setText(NumericUtil.float2String2(orderFood.getCount()));
				sumPriceTextView.setText(NumericUtil.float2String2(orderFood.calcPrice()));

				Button button1 = (Button)layout.findViewById(R.id.button_operation_1_pickedFood_list_item);
				Button button2 = (Button)layout.findViewById(R.id.button_operation_2_pickedFood_list_item);
				
				if(map.containsKey(ITEM_IS_ORI_FOOD)){//已点菜部分
					
					if(map.containsKey(ITEM_IS_OFFSET)){ 
						
						//如果有退菜显示'取消退菜'
						button1.setVisibility(View.INVISIBLE);
						layout.findViewById(R.id.view_pickedFood_cancel_line).setVisibility(View.VISIBLE);
						countEditText.setText(map.get(ITEM_FOOD_OFFSET).toString());
						
						button2.setText("取消退菜");
						button2.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								orderFood.addCount(orderFood.getDelta());
								mFoodListHandler.sendEmptyMessage(0);
							}
						});
					}else{
						//没有退菜则显示'退菜'和'催菜'
						(layout.findViewById(R.id.view_pickedFood_cancel_line)).setVisibility(View.INVISIBLE);
						//已点菜显示退菜按钮
						button2.setText("退菜");
						button2.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								if(ShoppingCart.instance().hasStaff()){
									if(ShoppingCart.instance().getStaff().getRole().hasPrivilege(Privilege.Code.CANCEL_FOOD)){
										AskCancelAmountDialog.newInstance(orderFood).show(getFragmentManager(), AskCancelAmountDialog.TAG);
									}else{
										Toast.makeText(SelectedFoodActivity.this, "对不起, 您没有退菜权限哦", Toast.LENGTH_SHORT).show();
									}
								}else{
									Toast.makeText(SelectedFoodActivity.this, "请先输入服务员账号再执行退菜操作", Toast.LENGTH_SHORT).show();
								}
							}
						});
						
						//已点菜不显示显示催菜按钮
						button1.setVisibility(View.INVISIBLE);
					}
					
					//隐藏数量'+'和'='按钮
					((ImageButton) layout.findViewById(R.id.imageButton_plus_pickedFood_item)).setVisibility(View.INVISIBLE);
					((ImageButton) layout.findViewById(R.id.imageButton_minus_pickedFood_item)).setVisibility(View.INVISIBLE);
				
				} else{
					
					//新点菜中显示估清菜品
					if(orderFood.asFood().isSellOut()){
						layout.findViewById(R.id.imgView_sellOut_pickedFood_list_item).setVisibility(View.VISIBLE);
						layout.findViewById(R.id.view_pickedFood_cancel_line).setVisibility(View.VISIBLE);
					}else{
						layout.findViewById(R.id.imgView_sellOut_pickedFood_list_item).setVisibility(View.INVISIBLE);
						layout.findViewById(R.id.view_pickedFood_cancel_line).setVisibility(View.INVISIBLE);
					}
					
					//新点菜显示'口味'和'删除'
					//FIXME 暂时不显示'口味'Button
					button1.setVisibility(View.INVISIBLE);
					button1.setText("口味");
					button1.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							PickTasteFragment pickTasteFg = new PickTasteFragment();
							pickTasteFg.setOnTasteChangeListener(SelectedFoodActivity.this);
							Bundle args = new Bundle();
							args.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(orderFood));
							pickTasteFg.setArguments(args);
							pickTasteFg.show(getFragmentManager(), PickTasteFragment.FOCUS_TASTE);
						}
					});
					//'删除'Button
					button2.setText("删除");
					button2.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							new AlertDialog.Builder(SelectedFoodActivity.this).setTitle("确认删除" + orderFood.getName())
								.setNeutralButton("确定",new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										if(ShoppingCart.instance().delete(orderFood)){
											mFoodListHandler.sendEmptyMessage(0);
										}
									}
								})
								.setNegativeButton("取消", null).show();
						}
					});
					//数量点击侦听
					countEditText.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(final View v) {
							//新建一个输入框
							final EditText editText = new EditText(SelectedFoodActivity.this);
							editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL | EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
							//创建对话框，并将输入框传入
							new AlertDialog.Builder(SelectedFoodActivity.this).setTitle("请输入修改数量")
								.setView(editText)
								.setNeutralButton("确定",new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										//设置新数值
										if(!editText.getText().toString().trim().isEmpty()){
											//如果等于0则提示
											 if(Float.valueOf(editText.getText().toString()).equals(0f)) {
												 Toast.makeText(SelectedFoodActivity.this, "输入的数值不正确，请重新输入", Toast.LENGTH_SHORT).show();
											 }else{
												float num = Float.parseFloat(editText.getText().toString());
												countEditText.setText(NumericUtil.float2String2(num));
												orderFood.setCount(num);
												ShoppingCart.instance().replaceFood(orderFood);	
												mFoodListHandler.sendEmptyMessage(0);
											 }
										}	 
									}
								})
								.setNegativeButton("取消", null).show();
						}
					});
					
					//数量'+'按钮
					ImageButton plus = (ImageButton) layout.findViewById(R.id.imageButton_plus_pickedFood_item);
					plus.setVisibility(View.VISIBLE);
					plus.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							float curNum = Float.parseFloat(countEditText.getText().toString());
							orderFood.setCount(++curNum);
							mFoodListHandler.sendEmptyMessage(0);
						}
					});
					
					//数量'-'按钮
					ImageButton minus = (ImageButton) layout.findViewById(R.id.imageButton_minus_pickedFood_item);
					minus.setVisibility(View.VISIBLE);
					minus.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							float curNum = Float.parseFloat(countEditText.getText().toString());
							if(--curNum >= 1){
								orderFood.setCount(curNum);
								mFoodListHandler.sendEmptyMessage(0);
							}
						}
					});
				}
				
				return layout;
			}
		};
		
		mPickedFoodList.setAdapter(adapter);
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
		
		mPickedFoodList = (ExpandableListView) findViewById(R.id.expandableListView_pickedFood);
		initPickedFoodList();
		
		//刷新购物车的显示
		ShoppingCart.instance().refresh();

		if(ShoppingCart.instance().hasNewOrder()){
			mCurrentFood = ShoppingCart.instance().getNewFoods().get(0);
		}else{
			mCurrentFood = ShoppingCart.instance().getOriFoods().get(0);
		}
		
		//设置侦听点击菜品时改变右边菜品详情的显示
		mPickedFoodList.setOnChildClickListener(new OnChildClickListener(){
			@Override
			public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) view.getTag();
				//点击后改变该项的颜色显示并刷新右边
				mCurrentFood = (OrderFood) map.get(ITEM_THE_FOOD);
				//刷新界面显示
				mFoodListHandler.sendEmptyMessage(0);
				
				return false;
			}
		});
		
		//暂结按钮
		((Button)findViewById(R.id.imageButton_temp_payOrder_pickedFood)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(SelectedFoodActivity.this).setTitle("请确定呼叫服务员结账")
				   .setMessage("共点菜" + ShoppingCart.instance().getAllFoods().size() + "个，合计" + ShoppingCart.instance().getTotalPrice() + "元，确定结账？")
				   .setNeutralButton("确定", new DialogInterface.OnClickListener() {
					   @Override
					   public void onClick(DialogInterface dialog, int whichButton) {
							try {
								
								ShoppingCart.instance().pay(new OnPayListener(){
									
									private ProgressDialog mProgressDialog;

									@Override
									public void onPrePay(Order orderToPay) {
										mProgressDialog = ProgressDialog.show(SelectedFoodActivity.this, "", "正在暂结" + orderToPay.getDestTbl().getAliasId() + "号账单信息...请稍候");
									}
									
									@Override
									public void onSuccess(Order orderToPay) {
										mProgressDialog.dismiss();
										final AlertDialog alertDialog = new AlertDialog.Builder(SelectedFoodActivity.this)
											.setTitle("提示")
											.setMessage(orderToPay.getDestTbl().getName() + "结账成功，请等待服务员结账...")
											.setNegativeButton("点击重新开始点菜", null)
											.setCancelable(false)
											.create();
											alertDialog.show();
											alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
												@Override
												public void onClick(View v){
													//TODO
													if(ShoppingCart.instance().hasTable()){
														new com.wireless.lib.task.QueryTableStatusTask(WirelessOrder.loginStaff, new Table.Builder(ShoppingCart.instance().getDestTable().getId())){
															
															private ProgressDialog mProgressDialog;
															
															@Override
															public void onPreExecute(){
																mProgressDialog = ProgressDialog.show(SelectedFoodActivity.this, "", "正在检查餐台状态...请稍候");
															}
															
															@Override
															public void onSuccess(Table table){
																if(table.isIdle()){
																	alertDialog.dismiss();
																	onBackPressed();
																}else{
																	Toast.makeText(SelectedFoodActivity.this, "对不起，此餐台还未开台，不能点菜", Toast.LENGTH_SHORT).show();
																}
																mProgressDialog.dismiss();
															}
															
															@Override
															public void onFail(BusinessException e){
																Toast.makeText(SelectedFoodActivity.this, "对不起，此餐台还未开台，不能点菜", Toast.LENGTH_SHORT).show();
																mProgressDialog.dismiss();
															}
															
														}.execute();
														
													}
												}
											});
											
									}

									@Override
									public void onFail(BusinessException e) {
										mProgressDialog.dismiss();
										new AlertDialog.Builder(SelectedFoodActivity.this)
											.setTitle("提示")
											.setMessage(e.getMessage())
											.setNeutralButton("确定", null)
											.show();
									}
									
								});
							} catch (BusinessException e) {
								Toast.makeText(SelectedFoodActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
							}
					   }
				   })
				   .setNegativeButton("取消", null)
				   .show();
			}
		});
		
		//下单按钮 
		((Button) findViewById(R.id.imageButton_submit_pickedFood)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(final View v) {		
//				if(ShoppingCart.instance().getNewFoods().size() == 0){
//					Toast.makeText(SelectedFoodActivity.this, "您还没有点菜", Toast.LENGTH_SHORT).show();
//					return;
//				}
				new AlertDialog.Builder(SelectedFoodActivity.this).setTitle("请确定下单")
				   .setMessage("新点菜" + ShoppingCart.instance().getNewFoods().size() + "个，小计" + ShoppingCart.instance().getNewPrice() + "元，确定下单？")
				   .setNeutralButton("确定", new DialogInterface.OnClickListener() {
					   @Override
					   public void onClick(DialogInterface dialog, int whichButton) {
							try{

								ShoppingCart.instance().commit(new OnCommitListener(){
									
									private ProgressDialog mProgressDialog;
									@Override
									public void onPreCommit(Order reqOrder) {
										mProgressDialog = ProgressDialog.show(SelectedFoodActivity.this, "", "查询号账单信息...请稍候");
									}

									@Override
									public void onSuccess(Order reqOrder) {
										mProgressDialog.dismiss();
										
										Toast.makeText(SelectedFoodActivity.this, "下单成功", Toast.LENGTH_SHORT).show();
										
										v.postDelayed(new Runnable(){

											@Override
											public void run() {
												onBackPressed();
											}
										}, 100);
									}
									
									@Override
									public void onFail(BusinessException e){
										if(ShoppingCart.instance().hasOriOrder()){

											if(e.getErrCode().equals(FrontBusinessError.ORDER_EXPIRED)){
												//如果是改单，并且返回是账单过期的错误状态，
												//则提示用户重新请求账单，再次确认提交
												new AlertDialog.Builder(SelectedFoodActivity.this)
													.setTitle("提示")
													.setMessage(ShoppingCart.instance().getDestTable().getName() + "的账单信息已经更新，已点菜信息将刷新，新点菜信息将会保留")
													.setNeutralButton("确定",
														new DialogInterface.OnClickListener() {
															@Override
															public void onClick(DialogInterface dialog,	int which){
																ShoppingCart.instance().refresh();
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
											new AlertDialog.Builder(SelectedFoodActivity.this)
												.setTitle("提示")
												.setMessage(e.getMessage())
												.setNeutralButton("确定", null)
												.show();
										}
									}						
								});
								
							}catch(BusinessException e){
								Toast.makeText(SelectedFoodActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
							}
					   }
				   })
				   .setNegativeButton("取消", null)
				   .show();
			}
		});
		
	}

	@Override
	protected void onDestroy() {
		mImageFetcher.clearCache();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// Activity关闭后不再侦听购物车变化
		mPickedFoodList.setOnChildClickListener(null);
		super.onBackPressed();
	}

	/**
	 * 删菜、退菜的dialog
	 * @author ggdsn1
	 *
	 */
	public static class AskCancelAmountDialog extends DialogFragment {

		public final static String TAG = "AskCancelAmountDialog";
		
		public AskCancelAmountDialog() {
			
		}
		
		static AskCancelAmountDialog newInstance(OrderFood selectedFood) {  
			AskCancelAmountDialog fragment = new AskCancelAmountDialog();  
		    Bundle args = new Bundle();  
		    args.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(selectedFood));
		    fragment.setArguments(args);  
		    return fragment;  
		} 
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {  

			OrderFoodParcel orderFoodParcel = getArguments().getParcelable(OrderFoodParcel.KEY_VALUE);
			final OrderFood selectedFood = orderFoodParcel.asOrderFood();
			
			// 删除数量默认为此菜品的点菜数量
			View view = LayoutInflater.from(getActivity()).inflate(R.layout.delete_count_dialog, null, false);
			final EditText countEdtTxt = (EditText) view.findViewById(R.id.editText_count_deleteCount_dialog);
			countEdtTxt.setText(NumericUtil.float2String2(selectedFood.getCount()));
			countEdtTxt.selectAll();
			
			//'+'数量
			((ImageButton) view.findViewById(R.id.imageButton_plus_deleteCount_dialog))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!countEdtTxt.getText().toString().isEmpty()) {
							float curNum = Float.parseFloat(countEdtTxt.getText().toString());
							countEdtTxt.setText(NumericUtil.float2String2(++curNum));
						}
					}
			});
			//'-'数量
			((ImageButton)view.findViewById(R.id.imageButton_minus_deleteCount_dialog)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!countEdtTxt.getText().toString().isEmpty()) {
						float curNum = Float.parseFloat(countEdtTxt.getText().toString());
						if (--curNum >= 1) {
							countEdtTxt.setText(NumericUtil.float2String2(curNum));
						}
					}
				}
			});
			
			return 
				new AlertDialog.Builder(getActivity()).setTitle("请输入退菜数量")
				   .setView(view)
				   .setNeutralButton("确定", new DialogInterface.OnClickListener() {
					   @Override
					   public void onClick(DialogInterface dialog, int whichButton) {
							float foodAmount = selectedFood.getCount();
							float cancelAmount = Float.parseFloat(countEdtTxt.getText().toString());
							if (foodAmount >= cancelAmount) {
								try {
									ShoppingCart.instance().removeCount(WirelessOrder.loginStaff, selectedFood, cancelAmount);
								} catch (BusinessException e) {
									Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
								}
							}else{
								Toast.makeText(getActivity(), "输入的数量大于已点数量, 请重新输入", Toast.LENGTH_SHORT).show();
							}
					   }
				   })
				   .setNegativeButton("取消", null)
				   .create();
							   
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
			
			mFoodListHandler.sendEmptyMessage(0);
			
		} catch (BusinessException e) {
			Toast.makeText(SelectedFoodActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * if the order changed ,refresh the food list
	 */
	@Override
	public void onCartChanged(List<OrderFood> foodsInCart) {
		mFoodListHandler.sendEmptyMessage(0);
	}
	
	/**
	 * when the temp food add, refresh the food list
	 */
	@Override
	public void onFoodAdd(Food food) {
		mFoodListHandler.sendEmptyMessage(0);
	}
}

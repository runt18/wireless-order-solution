package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.ShoppingCart.OnCommitListener;
import com.wireless.common.ShoppingCart.OnTableChangeListener;
import com.wireless.common.WirelessOrder;
import com.wireless.excep.BusinessException;
import com.wireless.ordermenu.R;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Table;

public class PickedFoodActivity extends Activity implements OnTableChangeListener{
	//列表项的显示标签
	private static final String ITEM_FOOD_NAME = "item_food_name";
	private static final String ITEM_FOOD_PRICE = "item_food_price";
	private static final String ITEM_FOOD_COUNT = "item_food_count";
	private static final String ITEM_FOOD_STATE = "item_food_state";
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
		ITEM_FOOD_STATE 
	};
	
	private static final int[] ITEM_ID = {
		R.id.textView_picked_food_name_item,
		R.id.textView_picked_food_price_item,
		R.id.editText_picked_food_count_item,
		R.id.textView_picked_food_state_item
	};
	protected static final int CUR_FOOD_CHANGED = 388962;//删菜标记
	protected static final int LIST_CHANGED = 878633;//已点菜更新标记
	
	private FoodHandler mFoodHandler;
	private FoodDataHandler mFoodDataHandler;

	private OrderFood mCurOrderFood = new OrderFood();

	/*
	 * 显示已点菜的列表的handler
	 * 负责更新已点菜的显示
	 */
	private static class FoodHandler extends Handler{
		private WeakReference<PickedFoodActivity> mActivity;
		private ExpandableListView mPickedFoodList;
		private TextView mTotalCountTextView;
		private TextView mTotalPriceTextView;
		private float mTotalPrice = 0;

		FoodHandler(PickedFoodActivity activity)
		{
			mActivity = new WeakReference<PickedFoodActivity>(activity);
			mPickedFoodList = (ExpandableListView) activity.findViewById(R.id.expandableListView_pickedFood);
			mTotalCountTextView = (TextView) activity.findViewById(R.id.textView_total_count_pickedFood);
			mTotalPriceTextView = (TextView)  activity.findViewById(R.id.textView_total_price_pickedFood);
		}
		
		@Override
		public void handleMessage(Message msg)
		{
			final PickedFoodActivity activity = mActivity.get();
			ShoppingCart sCart = ShoppingCart.instance();
			//若完全没有菜式，则关闭该activity
			if(!sCart.hasExtraFoods() && !sCart.hasOrder())
				activity.finish();
			
			int totalCount = 0;
			mTotalPrice = 0;

			List<Map<String, ?>> groupData = new ArrayList<Map<String, ?>>();
			final List<List<Map<String, ?>>> childData =  new ArrayList<List<Map<String, ?>>>();
			//若包含菜单，则将已点菜添加进列表
			if(sCart.hasOrder()){
				List<OrderFood> pickedFoods = Arrays.asList(ShoppingCart.instance().getOriOrder().foods);
				activity.mCurOrderFood = pickedFoods.get(0);
				totalCount += pickedFoods.size();
				List<Map<String, ?>> pickedFoodDatas = new ArrayList<Map<String,?>>();
				for(OrderFood f:pickedFoods)
				{
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(ITEM_FOOD_NAME, f.name);
					map.put(ITEM_FOOD_PRICE, String.valueOf(f.getPriceWithTaste()));
					map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
//					map.put(ITEM_FOOD_STATE, f.status);
					map.put(ITEM_THE_FOOD, f);
					pickedFoodDatas.add(map);
					mTotalPrice += f.getPriceWithTaste() * f.getCount(); 

				}
				childData.add(pickedFoodDatas);
				
				HashMap<String, Object> map1 = new HashMap<String, Object>();
				map1.put(ITEM_GROUP_NAME, "已点菜");
				groupData.add(map1);
			}
			//若包含新点菜，则将新点菜添加进列表
			if(sCart.hasExtraFoods()){
				List<OrderFood> newFoods = sCart.getExtraFoods();
				activity.mCurOrderFood = newFoods.get(0);
				totalCount += newFoods.size();
				
				List<Map<String, ?>> newFoodDatas = new ArrayList<Map<String,?>>();
				for(OrderFood f:newFoods)
				{
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(ITEM_FOOD_NAME, f.name);
					map.put(ITEM_FOOD_PRICE, String.valueOf(f.getPriceWithTaste()));
					map.put(ITEM_FOOD_COUNT, String.valueOf(f.getCount()));
//					map.put(ITEM_FOOD_STATE, f.status);
					map.put(ITEM_THE_FOOD, f);
					newFoodDatas.add(map);
					mTotalPrice += f.getPriceWithTaste() * f.getCount(); 

				}
				childData.add(newFoodDatas);
				
				HashMap<String, Object> map2 = new HashMap<String, Object>();
				map2.put(ITEM_GROUP_NAME, "新点菜");
				groupData.add(map2);
			}

			mTotalCountTextView.setText(""+ totalCount);
			mTotalPriceTextView.setText("" + mTotalPrice);
			//创建listview的adapter
			SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(activity.getApplicationContext(), 
					groupData, R.layout.picked_food_list_group_item, GROUP_ITEM_TAGS, GROUP_ITEM_ID, 
					childData, R.layout.picked_food_list_item, ITEM_TAGS, ITEM_ID){
				@Override
				public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
					View view = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
					
					final OrderFood orderFood = (OrderFood) childData.get(groupPosition).get(childPosition).get(ITEM_THE_FOOD);
					view.setTag(orderFood);
					//数量输入框
					final EditText countEditText = (EditText) view.findViewById(R.id.editText_picked_food_count_item);
					countEditText.setText("" + orderFood.getCount());
					countEditText.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							//FIXME 修正item和edittext的焦点问题
							countEditText.requestFocus();
						}
					});
					//数量加按钮
					((ImageButton) view.findViewById(R.id.imageButton_plus_pickedFood_item)).setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							float curNum = Float.parseFloat(countEditText.getText().toString());
							countEditText.setText("" + ++curNum);
							orderFood.setCount(curNum);
							mTotalPrice += orderFood.getPriceWithTaste();
							mTotalPriceTextView.setText("" + mTotalPrice);

						}
					});
					//数量减按钮
					((ImageButton) view.findViewById(R.id.imageButton_minus_pickedFood_item)).setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							float curNum = Float.parseFloat(countEditText.getText().toString());
							if(--curNum >= 1)
							{
								countEditText.setText("" + curNum);
								orderFood.setCount(curNum);
								mTotalPrice -= orderFood.getPriceWithTaste();
								mTotalPriceTextView.setText("" + mTotalPrice);
							}
						}
					});
					return view;
				}
			};
			mPickedFoodList.setAdapter(adapter);
			//展开所有列表
			for(int i=0;i<groupData.size();i++)
			{
				mPickedFoodList.expandGroup(i);
			}
			//设置侦听
			//当点击菜品是改变右边菜品详情的显示
			mPickedFoodList.setOnChildClickListener(new OnChildClickListener(){
				@Override
				public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
					if(parent.getTag() != null)
					{
						((View)(parent.getTag())).setBackgroundDrawable(null);
					}
					parent.setTag(view);
					//点击后改变该项的颜色显示
					activity.mCurOrderFood =  (OrderFood) view.getTag();
					activity.mFoodDataHandler.sendEmptyMessage(PickedFoodActivity.CUR_FOOD_CHANGED);
					view.setBackgroundColor(view.getResources().getColor(R.color.blue));
					return false;
				}
			});
		}
	}
	/*
	 * 负责显示右边菜品详情的handler
	 */
	private static class FoodDataHandler extends Handler{
		private WeakReference<PickedFoodActivity> mActivity;
		private TextView mFoodNameTextView;
		private TextView mOriPriceTextView;
		private TextView mConPriceTextView;
		private TextView mDiscountTextView;
		private TextView mTasteTextView;
		private TextView mTempTasteTextView;
		FoodDataHandler(PickedFoodActivity activity)
		{
			mActivity = new WeakReference<PickedFoodActivity>(activity);
			mFoodNameTextView = (TextView) activity.findViewById(R.id.textView_food_name_pickedFood);
			mOriPriceTextView = (TextView) activity.findViewById(R.id.textView_ori_price_pickedFood);
			mConPriceTextView = (TextView) activity.findViewById(R.id.textView_con_price_pickedFood);
			mDiscountTextView = (TextView) activity.findViewById(R.id.textView_discount_pickedFood);
			mTasteTextView = (TextView) activity.findViewById(R.id.textView_taste_pickedFood);
			mTempTasteTextView = (TextView) activity.findViewById(R.id.textView_tempTaste_pickedFood);
		}
		
		@Override
		public void handleMessage(Message Msg)
		{
			final PickedFoodActivity activity = mActivity.get();
			//设置菜品的各个数据
			mFoodNameTextView.setText(activity.mCurOrderFood.name);
			mOriPriceTextView.setText(""+ activity.mCurOrderFood.getPriceWithTaste());
			mConPriceTextView.setText(""+ activity.mCurOrderFood.getPriceWithTaste() * activity.mCurOrderFood.getDiscount());
			mDiscountTextView.setText(""+ activity.mCurOrderFood.getDiscount());
			mTasteTextView.setText(activity.mCurOrderFood.getNormalTastePref());
			if(activity.mCurOrderFood.tmpTaste != null)
				mTempTasteTextView.setText(activity.mCurOrderFood.tmpTaste.getPreference());
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.picked_food);
		//初始化handler
		mFoodHandler = new FoodHandler(this);
		mFoodDataHandler = new FoodDataHandler(this);
		final ShoppingCart sCart = ShoppingCart.instance();
		sCart.setOnTableChangeListener(this);
		
		mFoodHandler.sendEmptyMessage(LIST_CHANGED);
		mFoodDataHandler.sendEmptyMessage(CUR_FOOD_CHANGED);
		//请求账单
		if(sCart.hasTable())
			new QueryOrderTask(sCart.getDestTable().aliasID).execute(WirelessOrder.foodMenu);
		
		//催菜按钮的行为
		((Button) findViewById(R.id.button_hurry_pickedFood)).setOnClickListener(new View.OnClickListener() {				
			@Override
			public void onClick(View v) {
				if(mCurOrderFood.isHurried){
					mCurOrderFood.isHurried = false;
					Toast.makeText(getApplicationContext(), "取消催菜成功", Toast.LENGTH_SHORT).show();
					//TODO 添加催菜的显示
				}else{
					mCurOrderFood.isHurried = true;
					Toast.makeText(getApplicationContext(), "催菜成功", Toast.LENGTH_SHORT).show();	
				}		
				mFoodDataHandler.sendEmptyMessage(CUR_FOOD_CHANGED);
				mFoodHandler.sendEmptyMessage(LIST_CHANGED);
			}
		}); 
		
		//删菜按钮
		((Button) findViewById(R.id.button_delete_pickedFood)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				new AskCancelAmountDialog(mCurOrderFood).show();
			}
		});
		
		//下单按钮
		((ImageButton) findViewById(R.id.imageButton_submit_pickedFood)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(!sCart.hasTable()){
					Toast.makeText(PickedFoodActivity.this, "请先设置餐台", Toast.LENGTH_SHORT).show();
				} 
				else if(!sCart.hasStaff()){
					Toast.makeText(PickedFoodActivity.this, "您还未设置服务员，暂时不能下单。", Toast.LENGTH_SHORT).show();
				} 
				else if(ShoppingCart.instance().hasFoods()){
					try {
						sCart.commit(new IOnCommitListener());
					} catch (BusinessException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	@Override
	public void onTableChange(Table table) {
		new QueryOrderTask(ShoppingCart.instance().getDestTable().aliasID).execute(WirelessOrder.foodMenu);
	}
	//activity关闭后不再侦听购物车变化
	@Override
	public void finish(){
		super.finish();
		ShoppingCart.instance().setOnTableChangeListener(null);
	}
	
	private class AskCancelAmountDialog extends Dialog{
		
		AskCancelAmountDialog(final OrderFood selectedFood) {
			super(PickedFoodActivity.this);
		
			final Context context = PickedFoodActivity.this;
			View view = LayoutInflater.from(context).inflate(R.layout.delete_count_dialog, null);
			setContentView(view);
			this.setTitle("请输入" + selectedFood.name + "的删除数量");
			
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
					if(--curNum >= 0)
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
							ShoppingCart.instance().remove(selectedFood);
							mFoodHandler.sendEmptyMessage(PickedFoodActivity.LIST_CHANGED);
							dismiss();
							Toast.makeText(context, "删除\"" + selectedFood.toString() + "\"" + cancelAmount + "份成功", Toast.LENGTH_LONG).show();
							
						}else if(foodAmount > cancelAmount){
							/**
							 * 如果删除数量少于已点数量，则相应减去删除数量
							 */
							selectedFood.setCount(foodAmount - cancelAmount);
							mFoodHandler.sendEmptyMessage(PickedFoodActivity.LIST_CHANGED);
							dismiss();
							Toast.makeText(context, "删除\"" + selectedFood.toString() + "\"" + cancelAmount + "份成功", Toast.LENGTH_LONG).show();
							
						}else{
							Toast.makeText(context, "输入的删除数量大于已点数量, 请重新输入", Toast.LENGTH_LONG).show();
						}
						
					}catch(NumberFormatException e){
						Toast.makeText(context, "你输入删菜数量不正确", Toast.LENGTH_LONG).show();
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
//		private ProgressDialog mProgressDialog;
		
		public QueryOrderTask(int tableAlias) {
			super(tableAlias);
		}
		
		@Override
		protected void onPreExecute(){
//			mProgressDialog = ProgressDialog.show(PickedFoodActivity.this, "", "查询" + mTblAlias + "号账单信息...请稍候", true);
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则迁移到改单页面
		 */
		@Override
		protected void onPostExecute(Order order){
			//make the progress dialog disappeared
//			mProgressDialog.dismiss();
			
			if(mBusinessException != null){
				
				/**
				 * 如果请求账单信息失败，则跳转回本页面
				 */
				new AlertDialog.Builder(PickedFoodActivity.this)
					.setTitle("提示")
					.setMessage(mBusinessException.getMessage())
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					})
					.show();
			} else{
				/**
				 * 请求账单成功则更新相关的控件
				 */
				ShoppingCart sCart = ShoppingCart.instance();
				sCart.setOriOrder(order);
			}		
			mFoodHandler.sendEmptyMessage(LIST_CHANGED);
			mFoodDataHandler.sendEmptyMessage(CUR_FOOD_CHANGED);
		}
	}
	
	/**
	 * 执行改单的提交请求
	 */
	private class IOnCommitListener implements OnCommitListener{
		private ProgressDialog mProgDialog;

		@Override
		public void OnPreCommit(Order reqOrder) {
			mProgDialog = ProgressDialog.show(PickedFoodActivity.this, "", "提交" + reqOrder.destTbl.aliasID + "号餐台的菜单信息...请稍候", true);
		}

		@Override
		public void onPostCommit(Order reqOrder, BusinessException e) {
			mProgDialog.dismiss();
			if(e != null){
				if(e.errCode == ErrorCode.ORDER_EXPIRED){
					/**
					 * 如果账单已经过期，提示用户两种选择：
					 * 1 - 下载最新的账单信息，并更新已点菜的内容
					 * 2 - 退出改单界面，重新进入
					 */
					new AlertDialog.Builder(PickedFoodActivity.this)
						.setTitle("提示")
						.setMessage(e.getMessage())
						.setPositiveButton("刷新", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
								new QueryOrderTask(ShoppingCart.instance().getDestTable().aliasID).execute(WirelessOrder.foodMenu);
							}
						})
						.setNeutralButton("退出", new DialogInterface.OnClickListener() {							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.show();	
					
				}else{
					/**
					 * Prompt user message if any error occurred.
					 */
					new AlertDialog.Builder(PickedFoodActivity.this)
					.setTitle("提示")
					.setMessage(e.getMessage())
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					}).show();					
				}
			}else{
				String promptMsg;
				if(reqOrder.destTbl.aliasID == reqOrder.srcTbl.aliasID){
					promptMsg = reqOrder.destTbl.aliasID + "号台下单成功。";
				}else{
					promptMsg = reqOrder.srcTbl.aliasID + "号台转至" + reqOrder.destTbl.aliasID + "号台，并下单成功。";
				}
				//TODO 更新底栏显示
				Toast.makeText(PickedFoodActivity.this, promptMsg, Toast.LENGTH_SHORT).show();
				new QueryOrderTask(ShoppingCart.instance().getDestTable().aliasID).execute(WirelessOrder.foodMenu);
				//return to the main activity and show the successful message
				PickedFoodActivity.this.finish();
			}
		}		

	}

}

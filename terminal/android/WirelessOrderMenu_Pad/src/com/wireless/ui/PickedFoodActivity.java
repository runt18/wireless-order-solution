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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart.OnCommitListener;
import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.excep.BusinessException;
import com.wireless.ordermenu.R;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Table;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;

public class PickedFoodActivity extends Activity {
	//列表项的显示标签
	private static final String ITEM_FOOD_NAME = "item_food_name";
	private static final String ITEM_FOOD_PRICE = "item_food_price";
	private static final String ITEM_FOOD_COUNT = "item_food_count";
	private static final String ITEM_FOOD_STATE = "item_food_state";
	
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
	
	private Order mOrder;
	private ArrayList<OrderFood> mOrderFoods = new ArrayList<OrderFood>();
	private FoodHandler mFoodHandler;
	private OrderFood mCurOrderFood;
	private FoodDataHandler mFoodDataHandler;
	private Table mTable;
	protected ShoppingCart mSCart;

	/*
	 * 显示已点菜的列表的handler
	 * 负责更新已点菜的显示
	 */
	private static class FoodHandler extends Handler{
		private WeakReference<PickedFoodActivity> mActivity;
		private ListView mPickedFoodList;
		private TextView mTotalCountTextView;
		private TextView mTotalPriceTextView;

		FoodHandler(PickedFoodActivity activity)
		{
			mActivity = new WeakReference<PickedFoodActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg)
		{
			final PickedFoodActivity activity = mActivity.get();
			//若未初始化，则先初始化
			if(mPickedFoodList == null)
				mPickedFoodList = (ListView) activity.findViewById(R.id.listView_pickedFood);
			if(mTotalCountTextView == null)
				mTotalCountTextView = (TextView) activity.findViewById(R.id.textView_total_count_pickedFood);
			if(mTotalPriceTextView == null)
				mTotalPriceTextView = (TextView)  activity.findViewById(R.id.textView_total_price_pickedFood);
			
			mTotalCountTextView.setText(""+ activity.mOrderFoods.size());
			
			//将所有已点菜装载并统计总价
			float totalPrice = 0;
			final List<Map<String, ?>> listContents = new ArrayList<Map<String, ?>>();
			for(OrderFood f: activity.mOrderFoods)
			{
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put(ITEM_FOOD_NAME, f.name);
				map.put(ITEM_FOOD_PRICE, f.getPrice());
				map.put(ITEM_FOOD_COUNT, f.getCount());
				map.put(ITEM_FOOD_STATE, f.status);
				listContents.add(map);
				totalPrice += f.getPriceWithTaste(); 
			}
			
			mTotalPriceTextView.setText("" + totalPrice);

			
			SimpleAdapter adapter = new SimpleAdapter(activity.getApplicationContext(), listContents ,R.layout.picked_food_list_item, ITEM_TAGS, ITEM_ID){
				@Override
				public View getView(int position, View convertView, ViewGroup parent){
					View view = super.getView(position, convertView, parent);
					view.setTag(activity.mOrderFoods.get(position));
					return view;
				}
			};
			mPickedFoodList.setAdapter(adapter);
			//设置侦听
			//当点击菜品是改变右边菜品详情的显示
			mPickedFoodList.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if(parent.getTag() != null)
					{
						((View)(parent.getTag())).setBackgroundDrawable(null);
					}
					parent.setTag(view);
					
					activity.mCurOrderFood =  (OrderFood) view.getTag();
					activity.mFoodDataHandler.sendEmptyMessage(PickedFoodActivity.CUR_FOOD_CHANGED);
					view.setBackgroundColor(view.getResources().getColor(R.color.blue));
				}
			});
		}
	}
	/*
	 * 负责显示右边菜品详情的handler
	 */
	private static class FoodDataHandler extends Handler{
		private WeakReference<PickedFoodActivity> mActivity;
		private boolean isInitialed = false;
		private TextView mFoodNameTextView;
		private TextView mOriPriceTextView;
		private TextView mConPriceTextView;
		private TextView mDiscountTextView;
		private TextView mTasteTextView;
		private TextView mTempTasteTextView;
		FoodDataHandler(PickedFoodActivity activity)
		{
			mActivity = new WeakReference<PickedFoodActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message Msg)
		{
			final PickedFoodActivity activity = mActivity.get();
			//若未初始化，则先初始化
			if(!isInitialed)
			{
				mFoodNameTextView = (TextView) activity.findViewById(R.id.textView_food_name_pickedFood);
				mOriPriceTextView = (TextView) activity.findViewById(R.id.textView_ori_price_pickedFood);
				mConPriceTextView = (TextView) activity.findViewById(R.id.textView_con_price_pickedFood);
				mDiscountTextView = (TextView) activity.findViewById(R.id.textView_discount_pickedFood);
				mTasteTextView = (TextView) activity.findViewById(R.id.textView_taste_pickedFood);
				mTempTasteTextView = (TextView) activity.findViewById(R.id.textView_tempTaste_pickedFood);
				if(mFoodNameTextView != null && mOriPriceTextView != null && mConPriceTextView != null)
					isInitialed = true;
				else return;
			}
			//设置菜品的各个数据
			mFoodNameTextView.setText(activity.mCurOrderFood.name);
			mOriPriceTextView.setText(""+ activity.mCurOrderFood.getPrice());
			mConPriceTextView.setText(""+ activity.mCurOrderFood.calcDiscountPrice());
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
		
		mFoodHandler = new FoodHandler(this);
		mFoodDataHandler = new FoodDataHandler(this);
		mSCart = ShoppingCart.instance();
		mTable = mSCart.getDestTable();
		new QueryOrderTask(mTable.aliasID).execute(WirelessOrder.foodMenu);
		//催菜按钮的行为
		Button hurryBtn = (Button) findViewById(R.id.button_hurry_pickedFood);
		hurryBtn.setOnClickListener(new View.OnClickListener() {				
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
		Button deleteBtn = (Button) findViewById(R.id.button_delete_pickedFood);
		deleteBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				new AskCancelAmountDialog(mCurOrderFood).show();
			}
		});
		
		//下单按钮
		ImageButton submitBtn = (ImageButton) findViewById(R.id.imageButton_submit_pickedFood);
		submitBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mOrderFoods.size() != 0){
					//FIXME 修改成持有order
					Order reqOrder = new Order(mOrderFoods.toArray(new OrderFood[mOrderFoods.size()]), mTable.aliasID, mTable.customNum);
					reqOrder.srcTbl.aliasID = mOrder.destTbl.aliasID;
					reqOrder.orderDate = mOrder.orderDate;
					try {
						mSCart.commit(reqOrder, new IOnCommitListener());
					} catch (BusinessException e) {
						if(e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED)
						{
							Toast.makeText(PickedFoodActivity.this, "您还未设置服务员，暂时不能下单。", Toast.LENGTH_SHORT).show();
						}
						e.printStackTrace();
					}
				}else{
					Toast.makeText(PickedFoodActivity.this, "您还未点菜，暂时不能下单。", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	private class AskCancelAmountDialog extends Dialog{
		
		AskCancelAmountDialog(final OrderFood selectedFood) {
			super(PickedFoodActivity.this);
			final Context context = PickedFoodActivity.this;
			View view = LayoutInflater.from(context).inflate(R.layout.delete_count_dialog, null);
			setContentView(view);
			this.setTitle("请输入" + selectedFood.name + "的删除数量");
			
			//删除数量默认为此菜品的点菜数量
			final EditText cancelEdtTxt = (EditText)view.findViewById(R.id.editText_count_deleteCount);			
			cancelEdtTxt.setText(Util.float2String2(selectedFood.getCount()));
			
			//"确定"Button
			Button okBtn = (Button)view.findViewById(R.id.button_confirm_deleteCount);
			okBtn.setText("确定");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try{
						float foodAmount = selectedFood.getCount();
						float cancelAmount = Float.parseFloat(cancelEdtTxt.getText().toString());
						
						if(foodAmount == cancelAmount){
							/**
							 * 如果数量相等，则从列表中删除此菜
							 */
							mOrderFoods.remove(selectedFood);
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
		private ProgressDialog mProgressDialog;
		
		public QueryOrderTask(int tableAlias) {
			super(tableAlias);
		}
		
		@Override
		protected void onPreExecute(){
			mProgressDialog = ProgressDialog.show(PickedFoodActivity.this, "", "查询" + mTblAlias + "号账单信息...请稍候", true);
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则迁移到改单页面
		 */
		@Override
		protected void onPostExecute(Order order){
			//make the progress dialog disappeared
			mProgressDialog.dismiss();
			
			if(mErrMsg != null){
				
				/**
				 * 如果请求账单信息失败，则跳转回本页面
				 */
				new AlertDialog.Builder(PickedFoodActivity.this)
					.setTitle("提示")
					.setMessage(mErrMsg)
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
				mOrder = order;
				mFoodHandler.sendEmptyMessage(LIST_CHANGED);
				mOrderFoods.clear();
				for(OrderFood f:mOrder.foods)
				{
					mOrderFoods.add(f);
				}
				mCurOrderFood = mOrder.foods[0];
				mFoodDataHandler.sendEmptyMessage(CUR_FOOD_CHANGED);
			}			
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
								new QueryOrderTask(mTable.aliasID).execute(WirelessOrder.foodMenu);
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
				//return to the main activity and show the successful message
				PickedFoodActivity.this.finish();
				String promptMsg;
				if(reqOrder.destTbl.aliasID == reqOrder.srcTbl.aliasID){
					promptMsg = reqOrder.destTbl.aliasID + "号台下单成功。";
				}else{
					promptMsg = reqOrder.srcTbl.aliasID + "号台转至" + reqOrder.destTbl.aliasID + "号台，并下单成功。";
				}
				Toast.makeText(PickedFoodActivity.this, promptMsg, Toast.LENGTH_SHORT).show();
			}
		}		

	}
}

package com.wireless.fragment;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.ShoppingCart.OnFoodsChangeListener;
import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Table;
import com.wireless.ui.PickedFoodActivity;
import com.wireless.util.OptionDialog;
import com.wireless.util.OptionDialog.OnStaffChangedListener;
import com.wireless.util.OptionDialog.OnTableChangedListener;

public class OptionBarFragment extends Fragment implements OnTableChangedListener, OnStaffChangedListener, 
									OnFoodsChangeListener{
	
	private OptionDialog mDialog;

	private static boolean TABLE_FIXED = false;
	private static boolean STAFF_FIXED = false;
	
	private BBarHandler mBBarRefleshHandler;
	
	private OnOrderChangeListener mOnOrderChangeListener;
	private Button mTableNumBtn;
	private Button mStaffBtn;
	
	private static class BBarHandler extends Handler{		
		
		private Button mTableNumBtn;
		private Button mCustomCntBtn;
		private Button mStaffBtn;
		private Button mSelectedFoodBtn;
		
		BBarHandler(OptionBarFragment fragment){
			mSelectedFoodBtn = (Button)fragment.getActivity().findViewById(R.id.button_pickedFood_bottomBar);
			mTableNumBtn = (Button)fragment.getActivity().findViewById(R.id.button_table_bottombar);
			mCustomCntBtn = (Button)fragment.getActivity().findViewById(R.id.button_people_bottomBar);
			mStaffBtn = (Button)fragment.getActivity().findViewById(R.id.button_server_bottomBar);
		}
		
		@Override
		public void handleMessage(Message msg){
			//BBar显示餐台号和人数
			Table destTbl =  ShoppingCart.instance().getDestTable();
			if(destTbl != null){
				mTableNumBtn.setText("" + destTbl.aliasID);
				mCustomCntBtn.setText("" + destTbl.customNum);
			}else{
				mTableNumBtn.setText("未设定");
				mCustomCntBtn.setText("" + 0);
			}
			
			//BBar显示已点菜的数量
			if(ShoppingCart.instance().hasOrder()){
				int size = ShoppingCart.instance().getAllFoods().size();
				mSelectedFoodBtn.setText("" + size);
			}else{
				mSelectedFoodBtn.setText("" + 0);
			}
			
			//BBar显示服务员姓名
			StaffTerminal staff = ShoppingCart.instance().getStaff();
			if(staff != null){
				mStaffBtn.setText(staff.name);
			}else{
				mStaffBtn.setText("未设定");
			}
		}
	}
	
	
    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  
	   View layout = inflater.inflate(R.layout.bottombar, container, false);
	   return layout;
   }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		init(this.getActivity());
	}

	@Override
	public void onStart(){
		super.onStart();		
		mBBarRefleshHandler = new BBarHandler(this);
		ShoppingCart.instance().setOnFoodsChangeListener(this);
		mBBarRefleshHandler.sendEmptyMessage(0);
		
		if(!STAFF_FIXED){
			mStaffBtn.setClickable(true);
			mDialog.setItemEnable(OptionDialog.ITEM_STAFF, true);
		}else {
			mStaffBtn.setClickable(false);
			mDialog.setItemEnable(OptionDialog.ITEM_STAFF, false);
		}
		
		if(!TABLE_FIXED){
			mTableNumBtn.setClickable(true);
			mDialog.setItemEnable(OptionDialog.ITEM_TABLE, true);
		}else {
			mTableNumBtn.setClickable(false);
			mDialog.setItemEnable(OptionDialog.ITEM_TABLE, false);
		}
		
	}
	
	public static boolean isTableFixed() {
		return TABLE_FIXED;
	}

	public static void setTableFixed(boolean tABLE_FIXED) {
		TABLE_FIXED = tABLE_FIXED;
	}

	public static boolean isStaffFixed() {
		return STAFF_FIXED;
	}

	public static void setStaffFixed(boolean sTAFF_FIXED) {
		STAFF_FIXED = sTAFF_FIXED;
	}

	/**
	 * 初始化BBar上的控件
	 */
	private void init(final Activity activity){
		//餐台选择Button
		mTableNumBtn = (Button) getActivity().findViewById(R.id.button_table_bottombar);
		//服务员选择Button
		mStaffBtn = (Button) getActivity().findViewById(R.id.button_server_bottomBar);
		//已点菜button
		Button mSelectedFoodBtn = (Button) getActivity().findViewById(R.id.button_pickedFood_bottomBar);
		//会员选择Button
//		ImageView vipImgView = (ImageView)activity.findViewById(R.id.imageView_vip);
//		vipImgView.setOnClickListener(new View.OnClickListener() {			
//			@Override
//			public void onClick(View v) {
//				if(mDialog == null)
//					initDialog(activity);
//				mTabHost.setCurrentTabByTag(TAB_PICK_VIP);
//				mDialog.show();
//			}
//		});
		mDialog = new OptionDialog(getActivity());
		mDialog.setOwnerActivity(getActivity());
		mDialog.setOnStaffChangeListener(this);
		mDialog.setOnTableChangedListener(this);
		
		//服务员
		mStaffBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				mDialog.show();
				mDialog.setCurrentItem(OptionDialog.ITEM_STAFF);
			}
		});
		//餐台
		mTableNumBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				mDialog.show();
				mDialog.setCurrentItem(OptionDialog.ITEM_TABLE);
			}
		});
		//已点菜
		mSelectedFoodBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(!(activity instanceof PickedFoodActivity) && ShoppingCart.instance().hasOrder()){
					Intent intent = new Intent(activity,PickedFoodActivity.class);
					activity.startActivity(intent);
				}
			}
		});
		
		//返回按钮
		Button backBtn = (Button) getActivity().findViewById(R.id.button_back_bottomBar);
		backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});
	}
	
	/**
	 * 餐台设置时的回调，根据餐台的状态来判断是否请求订单
	 */
	@Override
	public void onTableChanged(Table table) {
		if(mDialog != null)
			mDialog.dismiss();
		//对话框关闭后请求餐台状态，根据餐台的状态来判断是否请求订单
		new QueryTableStatusTask(table).execute();
	}
	/**
	 * cancel the back button
	 */
	public void setBackButtonDisable(){
		Button backBtn = (Button) getActivity().findViewById(R.id.button_back_bottomBar);
		backBtn.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * 服务员改变时的回调，判断登陆信息是否正确
	 */
	@Override
	public void onStaffChanged(StaffTerminal staff, String id, String pwd) {
		mBBarRefleshHandler.sendEmptyMessage(0);
	}
	
	/**
	 * 执行请求对应餐台的账单信息 
	 */
	private class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{
//		private ProgressDialog mProgDialog;
//		ProgressToast mToast;
		QueryOrderTask(int tableAlias){
			super(tableAlias);
		}
		
		/**
		 * 在执行请求删单操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
//			mProgDialog = ProgressDialog.show(OptionBarFragment.this.getActivity(), "", "查询" + mTblAlias + "号账单信息...请稍候", true);
//			mToast = ProgressToast.show(getActivity(), "查询" + mTblAlias + "号账单信息...请稍候", Toast.LENGTH_LONG);
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则迁移到改单页面
		 */
		@Override
		protected void onPostExecute(Order order){
			//make the progress dialog disappeared
//			mProgDialog.dismiss();
//			mToast.cancel();
			if(mBusinessException != null){
//				 new QueryOrderTask(this.mTblAlias).execute(WirelessOrder.foodMenu);
				/**
				 * 如果请求账单信息失败，则跳转回本页面
				 */
				new AlertDialog.Builder(OptionBarFragment.this.getActivity())
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
				onOrderChanged(order);
				mBBarRefleshHandler.sendEmptyMessage(0);
			}			
		}
		
		void onOrderChanged(Order order){
			ShoppingCart.instance().setOriOrder(order);
			if(mOnOrderChangeListener != null)
				mOnOrderChangeListener.onOrderChange(order);
		}
	}

	/*
	 * 请求获得餐台的状态
	 */
	private class QueryTableStatusTask extends com.wireless.lib.task.QueryTableStatusTask{
//		ProgressToast mToast;
		Table mTable;
		QueryTableStatusTask(Table table){
			super(table.aliasID);
			mTable = table;
		}
		
		@Override
		protected void onPreExecute(){
//			mToast = ProgressToast.show(getActivity(), "查询" + mTblAlias + "号餐台信息");
		}
		
		/*
		 * 如果相应的操作不符合条件（比如要改单的餐台还未下单），
		 * 则把相应信息提示给用户，否则根据餐台状态，分别跳转到下单或改单界面。
		 */
		@Override
		protected void onPostExecute(Byte tblStatus){
//			_progDialog.dismiss();
//			mToast.cancel();
			/*
			 * Prompt user message if any error occurred.
			 * Otherwise perform the corresponding action.
			 */
			if(mErrMsg != null){
				//对话框关闭后请求餐台状态，根据餐台的状态来判断是否请求订单
//				new QueryTableStatusTask(mTable).execute();
				new AlertDialog.Builder(getActivity())
				.setTitle("提示")
				.setMessage(mErrMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
				
			}else{			
				OnQueryTblStatus(tblStatus);
			}
		}	
		
		void OnQueryTblStatus(byte status){
			mTable.status = status;
			ShoppingCart.instance().setDestTable(mTable);	
			//根据餐台状态更新order和显示
			if(mTable.status == Table.TABLE_IDLE){		
				ShoppingCart.instance().setOriOrder(null);
				Toast.makeText(getActivity(), "该餐台尚未点菜", Toast.LENGTH_SHORT).show();
				//通知改变更新
				if(mOnOrderChangeListener != null)
					mOnOrderChangeListener.onOrderChange(null);
			}else if(mTable.status == Table.TABLE_BUSY){
				 new QueryOrderTask(mTable.aliasID).execute(WirelessOrder.foodMenu);
			}
		}
	}
	
	
	@Override
	public void onFoodsChange(List<OrderFood> newFoods) {
		mBBarRefleshHandler.sendEmptyMessage(0);
	}
	/**
	 * 订单改变的侦听器
	 */
	public interface OnOrderChangeListener{
		void onOrderChange(Order order);
	}
	
	public void setOnOrderChangeListener(OnOrderChangeListener l)
	{
		mOnOrderChangeListener = l;
	}

	public void setTable(int tableId) {
		for(Table t :WirelessOrder.tables)
			if(t.aliasID == tableId)
				onTableChanged(t);
	}

	public void setStaff(long staffPin) {
		for(StaffTerminal s:WirelessOrder.staffs)
			if(s.pin == staffPin)
				ShoppingCart.instance().setStaff(s);
	}
	
	//TODO 添加其他listener
//	public interface OnTableChangeListener{
//		void onTableChange(Table table);
//	}
}

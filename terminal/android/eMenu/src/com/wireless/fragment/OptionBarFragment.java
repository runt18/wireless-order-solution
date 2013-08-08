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
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.ShoppingCart.OnFoodsChangedListener;
import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.ui.MainActivity;
import com.wireless.ui.SelectedFoodActivity;
import com.wireless.util.OptionDialog;
import com.wireless.util.OptionDialog.OnStaffChangedListener;
import com.wireless.util.OptionDialog.OnTableChangedListener;

/**
 * this fragment contains a dialog to setting table and staff
 * <br/>
 * it also contains some common operations like setting 
 * people's amount and enter into {@link SelectedFoodActivity}
 * <br/><br/>
 * {@link TablePanelFragment} and {@link StaffPanelFragment} is sticked on the {@link OptionDialog}
 * @author ggdsn1
 * @see OptionDialog
 */
@SuppressWarnings("deprecation")
public class OptionBarFragment extends Fragment 
							   implements OnTableChangedListener, 
							   			  OnStaffChangedListener, 
							   			  OnFoodsChangedListener{
	
	public static final String TAG = "OptionBar";

	private OptionDialog mDialog;

	private static boolean TABLE_FIXED = false;
	private static boolean STAFF_FIXED = false;
	
	private BBarHandler mBBarRefleshHandler;
	
	private OnOrderChangeListener mOnOrderChangeListener;
	private Button mTableNumBtn;
	private Button mStaffBtn;

	protected int mOldCustomerNum;
	
	/**
	 * this handler is use to refresh the {@link OptionBarFragment} display,like table number,people amount,etc
	 */
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
				mTableNumBtn.setText("" + destTbl.getAliasId());
				mCustomCntBtn.setText("" + destTbl.getCustomNum());
			}else{
				mTableNumBtn.setText("未设定");
				mCustomCntBtn.setText("" + 0);
			}
			
			//BBar显示已点菜的数量
			if(ShoppingCart.instance().hasOrder()){
				mSelectedFoodBtn.setText("" + ShoppingCart.instance().getAllAmount());
			}else{
				mSelectedFoodBtn.setText("" + 0);
			}
			
			//BBar显示服务员姓名
			Staff staff = ShoppingCart.instance().getStaff();
			if(staff != null){
				mStaffBtn.setText(staff.getName());
			}else{
				mStaffBtn.setText("未设定");
			}
		}
	}
	
	/**
	 * it prepare the layout and setting people amount button 
	 */
    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  
	   View layout = inflater.inflate(R.layout.fragment_option_bar, container, false);
	   
	   //人数设定
	   layout.findViewById(R.id.button_people_bottomBar).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(ShoppingCart.instance().hasTable()){
					
					final EditText peopleCountEdit = new EditText(getActivity());
					peopleCountEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
					
					new AlertDialog.Builder(getActivity()).setTitle("设定人数")
					.setView(peopleCountEdit)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							int num = Integer.parseInt(peopleCountEdit.getText().toString());
							if(num == 0){
								Toast.makeText(getActivity(), "不能设置人数为0，请重新输入", Toast.LENGTH_SHORT).show();
							}
							else if(num > 255){
								Toast.makeText(getActivity(), "人数数量不能超过255，请重新输入", Toast.LENGTH_SHORT).show();
							}
							else {
								ShoppingCart.instance().getDestTable().setCustomNum((short) num);
								mBBarRefleshHandler.sendEmptyMessage(0);
							}
						}
					}) 
					.setNegativeButton("取消", null)
					.show();
				} else {
					Toast.makeText(getActivity(), "未设置餐台，无法更改人数", Toast.LENGTH_SHORT).show();
				}
			}
		});
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
		
		//decide whether need to fix staff/table or not
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
				//保存之前设定的人数
				Table table = ShoppingCart.instance().getDestTable();
				if(table != null && table.getCustomNum() > 1)
					mOldCustomerNum = table.getCustomNum();
				mDialog.show();
				mDialog.setCurrentItem(OptionDialog.ITEM_TABLE);
			}
		});
		
		//已点菜
		mSelectedFoodBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(!(activity instanceof SelectedFoodActivity) && ShoppingCart.instance().hasOrder()){
					Intent intent = new Intent(activity,SelectedFoodActivity.class);
					activity.startActivityForResult(intent, MainActivity.MAIN_ACTIVITY_RES_CODE);
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
		if(mOldCustomerNum > 0)
			table.setCustomNum(mOldCustomerNum);
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
	public void onStaffChanged(Staff staff, String id, String pwd) {
		mBBarRefleshHandler.sendEmptyMessage(0);
	}
	
	/**
	 * 执行请求对应餐台的账单信息 
	 */
	private class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{

		QueryOrderTask(int tableAlias, int oldCustomerNum){
			super(WirelessOrder.loginStaff, tableAlias, WirelessOrder.foodMenu);
		}
		
		/**
		 * 在执行请求删单操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则迁移到改单页面
		 */
		@Override
		protected void onPostExecute(Order order){
			if(mBusinessException != null){
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
				Log.i(TAG, "old customer number : " + mOldCustomerNum);
				if(mOldCustomerNum > 1)
					order.setCustomNum(mOldCustomerNum);
				onOrderChanged(order);
				mBBarRefleshHandler.sendEmptyMessage(0);
			}			
		}
		
		void onOrderChanged(Order order){
			ShoppingCart.instance().setOriOrder(order);
			if(mOnOrderChangeListener != null)
				mOnOrderChangeListener.onOrderChanged(order);
		}
	}

	/*
	 * 请求获得餐台的状态
	 */
	private class QueryTableStatusTask extends com.wireless.lib.task.QueryTableStatusTask{
		Table mTable;
		QueryTableStatusTask(Table table){
			super(WirelessOrder.loginStaff, table);
			mTable = table;
		}
		
		/*
		 * 如果相应的操作不符合条件（比如要改单的餐台还未下单），
		 * 则把相应信息提示给用户，否则根据餐台状态，分别跳转到下单或改单界面。
		 */
		@Override
		protected void onPostExecute(Table.Status tblStatus){
			/*
			 * Prompt user message if any error occurred.
			 * Otherwise perform the corresponding action.
			 */
			if(mErrMsg != null){
				//对话框关闭后请求餐台状态，根据餐台的状态来判断是否请求订单
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
		
		void OnQueryTblStatus(Table.Status status){
			int oldGuestNum = mTable.getCustomNum();
			mTable.setStatus(status);
			if(oldGuestNum > 1)
				mTable.setCustomNum(oldGuestNum);
			
			ShoppingCart.instance().setDestTable(mTable);	
			//根据餐台状态更新order和显示
			if(mTable.isIdle()){		
				ShoppingCart.instance().setOriOrder(null);
				Toast.makeText(getActivity(), "该餐台尚未点菜", Toast.LENGTH_SHORT).show();
				//通知改变更新
				if(mOnOrderChangeListener != null)
					mOnOrderChangeListener.onOrderChanged(null);
			}else if(mTable.isBusy()){
				 new QueryOrderTask(mTable.getAliasId(), oldGuestNum).execute();
			}
		}
	}
	
	
	@Override
	public void onFoodsChanged(List<OrderFood> newFoods) {
		mBBarRefleshHandler.sendEmptyMessage(0);
	}
	/**
	 * 订单改变的侦听器
	 */
	public static interface OnOrderChangeListener{
		public void onOrderChanged(Order order);
	}
	
	public void setOnOrderChangeListener(OnOrderChangeListener l)
	{
		mOnOrderChangeListener = l;
	}

	public void setTable(int tableId) {
		for(Table t :WirelessOrder.tables)
			if(t.getAliasId() == tableId)
				onTableChanged(t);
	}

	public void setStaff(int staffPin) {
		for(Staff s : WirelessOrder.staffs)
			if(s.getId() == staffPin)
				ShoppingCart.instance().setStaff(s);
	}
	
}

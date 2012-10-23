package com.wireless.fragment;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.Params;
import com.wireless.common.ShoppingCart;
import com.wireless.common.ShoppingCart.OnFoodsChangeListener;
import com.wireless.common.WirelessOrder;
import com.wireless.fragment.StaffPanelFragment.OnStaffChangedListener;
import com.wireless.fragment.TablePanelFragment.OnTableChangedListener;
import com.wireless.ordermenu.R;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;
import com.wireless.ui.PickedFoodActivity;

public class OptionBarFragment extends Fragment implements OnTableChangedListener, OnStaffChangedListener, 
									OnFoodsChangeListener{
//	public static final String CUR_TABLE = "current_table";
	//private static Table mTable;
	//private static int mCustomCount;
	//private static int mPickedFood;
	//private static StaffTerminal mStaff;
	
	private static final String TAB_PICK_TBL = "tab_pick_table";
	private static final String TAB_PICK_STAFF = "tab_pick_staff";
	private static final String TAB_PICK_VIP = "tab_pick_vip";
	
	private Dialog mDialog;
	private TabHost mTabHost;

	
	private BBarHandler  mBBarRefleshHandler;
	
	private static class BBarHandler extends Handler{		
		
		private TextView mSelectedFoodTextView;
		private TextView mTableNumTextView;
		private TextView mCustomCntTextView;
		private TextView mStaffTxtView;
		
		BBarHandler(OptionBarFragment fragment){
			mSelectedFoodTextView = (TextView)fragment.getActivity().findViewById(R.id.textView_selectedFood);
			mTableNumTextView = (TextView)fragment.getActivity().findViewById(R.id.txtView_table_count);
			mCustomCntTextView = (TextView)fragment.getActivity().findViewById(R.id.textView_peopCnt);
			mStaffTxtView = (TextView)fragment.getActivity().findViewById(R.id.textView_serverName);
		}
		
		@Override
		public void handleMessage(Message msg){
			//BBar显示餐台号和人数
			Table destTbl =  ShoppingCart.instance().getDestTable();
			if(destTbl != null){
				mTableNumTextView.setText("" + destTbl.aliasID);
				mCustomCntTextView.setText("" + destTbl.customNum);
			}else{
				mTableNumTextView.setText("未设定");
				mCustomCntTextView.setText("" + 0);
			}
			
			//BBar显示已点菜的数量
			if(ShoppingCart.instance().hasFoods()){
				mSelectedFoodTextView.setText("" + ShoppingCart.instance().getAllFoods().size());
			}else{
				mSelectedFoodTextView.setText("" + 0);
			}
			
			//BBar显示服务员姓名
			StaffTerminal staff = ShoppingCart.instance().getStaff();
			if(staff != null){
				mStaffTxtView.setText(staff.name);
			}else{
				mStaffTxtView.setText("未设定");
			}
		}
	}
	
	
    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  
	   return inflater.inflate(R.layout.bottombar, container, false);
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
	}
	
	/**
	 * 初始化BBar上的控件
	 */
	private void init(final Activity activity){
		//餐台选择Button
		ImageView pickTblImgView = (ImageView)activity.findViewById(R.id.imgView_set_table);
		pickTblImgView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(mDialog == null)
					initDialog(activity);
				mTabHost.setCurrentTabByTag(TAB_PICK_TBL);
				mDialog.show();
			}
		});

		//服务员选择Button
		ImageView pickStaffImgView = (ImageView)activity.findViewById(R.id.imageView_server);
		pickStaffImgView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(mDialog == null)
					initDialog(activity);
				mTabHost.setCurrentTabByTag(TAB_PICK_STAFF);
				mDialog.show();
			}
		});
		
		//会员选择Button
		ImageView vipImgView = (ImageView)activity.findViewById(R.id.imageView_vip);
		vipImgView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(mDialog == null)
					initDialog(activity);
				mTabHost.setCurrentTabByTag(TAB_PICK_VIP);
				mDialog.show();
			}
		});
		//已点菜button
		ImageView pickedFoodImgView = (ImageView) activity.findViewById(R.id.imageView_selectedFood);
		pickedFoodImgView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(!(activity instanceof PickedFoodActivity) && 
						(ShoppingCart.instance().getOriOrder() != null || !ShoppingCart.instance().getExtraFoods().isEmpty()))
				{
					Intent intent = new Intent(activity,PickedFoodActivity.class);
					activity.startActivity(intent);
				}
			}
		});
		
	}
	
	/**
	 * 初始化dialog所需要的数据
	 * 包括tabHost和各种editText
	 * 
	 * @param activity 调用这个dialog的activity
	 */
	private void initDialog(final Activity activity){
		View dialogLayout = activity.getLayoutInflater().inflate(R.layout.option_dialog, (ViewGroup)activity.findViewById(R.id.tab_dialog));
		
		mTabHost = (TabHost) dialogLayout.findViewById(R.id.tabhost);
		mTabHost.setup();
		
		mTabHost.addTab(mTabHost.newTabSpec(TAB_PICK_TBL).setIndicator("餐台设置").setContent(R.id.tab1));
		mTabHost.addTab(mTabHost.newTabSpec(TAB_PICK_STAFF).setIndicator("服务员设置").setContent(R.id.tab2));
//		mTabHost.addTab(mTabHost.newTabSpec("TAB_PICK_TBL2").setIndicator("餐台设置").setContent(R.id.tab3));
		
		mDialog = new Dialog(activity);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(dialogLayout);
		
		Window dialogWindow = mDialog.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.width = 940;
		dialogWindow.setAttributes(lp);
		
		((Button)mDialog.findViewById(R.id.button_tab1_cancel)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
			}
		});
		
		((Button)mDialog.findViewById(R.id.button_tab2_cancel)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
			}
		});
		
		((TablePanelFragment)getFragmentManager().findFragmentById(R.id.tab1)).setOnTableChangedListener(this);
		((StaffPanelFragment)getFragmentManager().findFragmentById(R.id.tab2)).setOnServerChangeListener(this);
	}
	
	
	/**
	 * 餐台设置时的回调，根据餐台的状态来判断是否请求订单
	 */
	@Override
	public void onTableChanged(Table table) {
		mDialog.dismiss();
		
		ShoppingCart.instance().setDestTable(table);		

		if(table.status == Table.TABLE_IDLE){		
			ShoppingCart.instance().setOriOrder(null);
			Toast.makeText(this.getActivity(), "该餐台尚未点菜", Toast.LENGTH_SHORT).show();
			
		}else if(table.status == Table.TABLE_BUSY){
			 new QueryOrderTask(table.aliasID){
				@Override
				void onOrderChanged(Order order) {
					ShoppingCart.instance().setOriOrder(order);
				}
			 }.execute(WirelessOrder.foodMenu);
		}

	}

	/**
	 * 服务员改变时的回调，判断登陆信息是否正确
	 */
	@Override
	public void onStaffChanged(final StaffTerminal staff, String id, String pwd) {
		try {
			//Convert the password into MD5
			MessageDigest digester = MessageDigest.getInstance("MD5");
			digester.update(pwd.getBytes(), 0, pwd.getBytes().length); 
		
			if(id.equals("")){
				Toast.makeText(getActivity(), "账号不能为空", Toast.LENGTH_SHORT).show();
			}else if(staff.pwd.equals(toHexString(digester.digest()))){
				
				ShoppingCart cart = ShoppingCart.instance(); 
				cart.setStaff(staff);
				
				//保存staff pin到文件里面
				Editor editor = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();//获取编辑器
				editor.putLong(Params.STAFF_PIN, staff.pin);
				//提交修改
				editor.commit();	
				((TextView) getActivity().findViewById(R.id.textView_serverName)).setText(staff.name);
				//set the pin generator according to the staff login
				ReqPackage.setGen(new PinGen(){
					@Override
					public long getDeviceId() {
						return staff.pin;
					}
					@Override
					public short getDeviceType() {
						return Terminal.MODEL_STAFF;
					}
					
				});
				mDialog.dismiss();
			}else{		
				Toast.makeText(getActivity(), "密码错误", Toast.LENGTH_SHORT).show();
			}
			
			mBBarRefleshHandler.sendEmptyMessage(0);

		}catch(NoSuchAlgorithmException e) {
			Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Convert the md5 byte to hex string.
	 * @param md5Msg the md5 byte value
	 * @return the hex string to this md5 byte value
	 */
	private String toHexString(byte[] md5Msg){
		StringBuffer hexString = new StringBuffer();
		for (int i=0; i < md5Msg.length; i++) {
			if(md5Msg[i] >= 0x00 && md5Msg[i] < 0x10){
				hexString.append("0").append(Integer.toHexString(0xFF & md5Msg[i]));
			}else{
				hexString.append(Integer.toHexString(0xFF & md5Msg[i]));					
			}
		}
		return hexString.toString();
	}
	
	/**
	 * 执行请求对应餐台的账单信息 
	 */
	private abstract class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{
		private ProgressDialog mProgDialog;
	
		QueryOrderTask(int tableAlias){
			super(tableAlias);
		}
		
		/**
		 * 在执行请求删单操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			mProgDialog = ProgressDialog.show(OptionBarFragment.this.getActivity(), "", "查询" + mTblAlias + "号账单信息...请稍候", true);
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则迁移到改单页面
		 */
		@Override
		protected void onPostExecute(Order order){
			//make the progress dialog disappeared
			mProgDialog.dismiss();
			
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
				onOrderChanged(order);
				mBBarRefleshHandler.sendEmptyMessage(0);
			}			
		}
		
		abstract void onOrderChanged(Order order);
	}


	@Override
	public void onFoodsChange(List<OrderFood> newFoods) {
		mBBarRefleshHandler.sendEmptyMessage(0);
	}

}

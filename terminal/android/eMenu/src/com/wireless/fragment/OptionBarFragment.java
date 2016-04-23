package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.List;

import com.wireless.common.Params;
import com.wireless.common.ShoppingCart;
import com.wireless.common.ShoppingCart.OnCartChangedListener;
import com.wireless.common.ShoppingCart.OnStaffChangedListener;
import com.wireless.common.ShoppingCart.OnTableChangedListener;
import com.wireless.ordermenu.R;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.ui.MainActivity;
import com.wireless.ui.SelectedFoodActivity;
import com.wireless.util.OptionDialogFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * this fragment contains a dialog to setting table and staff
 * <br/>
 * it also contains some common operations like setting 
 * people's amount and enter into {@link SelectedFoodActivity}
 * <br/><br/>
 * {@link TablePanelFragment} and {@link StaffPanelFragment} is sticked on the {@link OptionDialogFragment}
 * @author ggdsn1
 * @see OptionDialogFragment
 */
public class OptionBarFragment extends Fragment 
							   implements OnTableChangedListener, 
							   			  OnStaffChangedListener, 
							   			  OnCartChangedListener{
	
	public static final String TAG = "OptionBar";

	private BBarHandler mBBarRefleshHandler;
	
	private OnCartChangedListener mCartChangedListener;
	
	protected int mOldCustomerNum;
	
	/**
	 * this handler is use to refresh the {@link OptionBarFragment} display,like table number,people amount,etc
	 */
	private static class BBarHandler extends Handler{		
		
		private final WeakReference<OptionBarFragment> mFragment;
		
		BBarHandler(OptionBarFragment fragment){
			mFragment = new WeakReference<OptionBarFragment>(fragment);
		}
		
		@Override
		public void handleMessage(Message msg){
			
			Button selectedFoodBtn = (Button)mFragment.get().getActivity().findViewById(R.id.button_pickedFood_bottomBar);
			Button tableNumBtn = (Button)mFragment.get().getActivity().findViewById(R.id.button_table_bottombar);
			Button customCntBtn = (Button)mFragment.get().getActivity().findViewById(R.id.button_people_bottomBar);
			Button staffBtn = (Button)mFragment.get().getActivity().findViewById(R.id.button_server_bottomBar);

			//BBar显示餐台号和人数
			Table destTbl =  ShoppingCart.instance().getDestTable();
			if(destTbl != null){
				if(destTbl.getName().isEmpty()){
					tableNumBtn.setText("" + destTbl.getAliasId());
				}else{
					tableNumBtn.setText("" + destTbl.getName());
				}
				customCntBtn.setText("" + destTbl.getCustomNum());
			}else{
				tableNumBtn.setText("未设定");
				customCntBtn.setText("" + 0);
			}
			
			//BBar显示已点菜的数量
			if(ShoppingCart.instance().hasNewOrder()){
				selectedFoodBtn.setText(Integer.toString(ShoppingCart.instance().getNewAmount()));
			}else{
				selectedFoodBtn.setText("0");
			}
			
			//BBar显示服务员姓名
			if(ShoppingCart.instance().hasStaff()){
				staffBtn.setText(ShoppingCart.instance().getStaff().getName());
			}else{
				staffBtn.setText("未设定");
			}
		}
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
        	mCartChangedListener = (OnCartChangedListener)activity;
        }catch(ClassCastException ignored){
        	
        }
        
        //FIXME
		mBBarRefleshHandler = new BBarHandler(this);

		ShoppingCart.instance().setOnCartChangeListener(this);
		ShoppingCart.instance().setOnTableChangedListener(this);
		ShoppingCart.instance().setOnStaffChangedListener(this);
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
		mBBarRefleshHandler.sendEmptyMessage(0);
		
		ShoppingCart.instance().setOnCartChangeListener(this);
		ShoppingCart.instance().setOnTableChangedListener(this);
		ShoppingCart.instance().setOnStaffChangedListener(this);

	}
	
	/**
	 * 初始化BBar上的控件
	 */
	private void init(final Activity activity){
		
		//服务员Button
		((Button) getActivity().findViewById(R.id.button_server_bottomBar)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).getBoolean(Params.STAFF_FIXED, false)){
					Toast.makeText(getActivity(), "服务员已经锁定, 不能设置哦", Toast.LENGTH_SHORT).show();
				}else{
					OptionDialogFragment.newInstance(OptionDialogFragment.ITEM_STAFF).show(getFragmentManager(), OptionDialogFragment.TAG);
				}
			}
		});
		//餐台Button
		((Button) getActivity().findViewById(R.id.button_table_bottombar)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).getBoolean(Params.TABLE_FIXED, false)){
					Toast.makeText(getActivity(), "餐台已经锁定, 不能设置哦", Toast.LENGTH_SHORT).show();
				}else{
					//保存之前设定的人数
					Table table = ShoppingCart.instance().getDestTable();
					if(table != null && table.getCustomNum() > 1){
						mOldCustomerNum = table.getCustomNum();
					}
					OptionDialogFragment.newInstance(OptionDialogFragment.ITEM_TABLE).show(getFragmentManager(), OptionDialogFragment.TAG);
				}
			}
		});
		
		//已点菜Button
		((Button) getActivity().findViewById(R.id.button_pickedFood_bottomBar)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(!(activity instanceof SelectedFoodActivity)){
					if(ShoppingCart.instance().hasNewOrder()){
						Intent intent = new Intent(activity, SelectedFoodActivity.class);
						activity.startActivityForResult(intent, MainActivity.MAIN_ACTIVITY_RES_CODE);
						
					}else if(ShoppingCart.instance().hasTable()){
						ShoppingCart.instance().setDestTable(ShoppingCart.instance().getDestTable());
						if(ShoppingCart.instance().hasOriOrder()){
							Intent intent = new Intent(activity, SelectedFoodActivity.class);
							activity.startActivityForResult(intent, MainActivity.MAIN_ACTIVITY_RES_CODE);
						}else{
							Toast.makeText(getActivity(), "对不起，餐台还没开台，不能下单", Toast.LENGTH_SHORT).show();
						}
					}
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
	 * cancel the back button
	 */
	public void setBackButtonDisable(){
		Button backBtn = (Button) getActivity().findViewById(R.id.button_back_bottomBar);
		backBtn.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * 餐台设置时的回调，请求选中餐台相应的账单信息，并更新OptionBar
	 */
	@Override
	public void onTableChanged(Table table) {
		mBBarRefleshHandler.sendEmptyMessage(0);
	}
	
	/**
	 * 服务员改变时的回调，更新OptionBar
	 */
	@Override
	public void onStaffChanged(Staff staff) {
		mBBarRefleshHandler.sendEmptyMessage(0);
	}
	
	@Override
	public void onCartChanged(List<OrderFood> foodsInCart) {
		mBBarRefleshHandler.sendEmptyMessage(0);
		if(mCartChangedListener != null){
			mCartChangedListener.onCartChanged(foodsInCart);
		}
	}
	
}

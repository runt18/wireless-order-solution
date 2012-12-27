package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.fragment.AddressSettingFragment;
import com.wireless.fragment.OptionBarFragment;
import com.wireless.fragment.StaffPanelFragment;
import com.wireless.fragment.StaffPanelFragment.OnStaffChangedListener;
import com.wireless.fragment.TablePanelFragment;
import com.wireless.fragment.TablePanelFragment.OnTableChangedListener;
import com.wireless.ordermenu.R;
import com.wireless.parcel.TableParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Table;

public class SettingActivity extends Activity implements OnTableChangedListener, OnStaffChangedListener{

	//setting item flags
	private static final int ITEM_TABLE = 0;
	private static final int ITEM_STAFF = 1;
	private static final int ITEM_ADDRESS = 2;
	public static final int SETTING_RES_CODE = 131;
	public static final String FOODS_REFRESHED = "food_refreshed";
	
	//current item flag
	private int mCurrentItem = ITEM_TABLE;
	
	//fragment handler
	private SettingItemHandler mSettingItemHandler;
	
	private ListView mListView;
	private Table mTable;
	private StaffTerminal mStaff;
	private boolean isFoodChanged = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.setting);
		
		TextView versionText = (TextView)findViewById(R.id.textView_setting_version);
		try {
			versionText.setText("版本号：" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (Exception e) {
			versionText.setText("");
		}	
		
		//当读取到餐台锁定信息时,如果是锁定状态则还原数据
		SharedPreferences pref = getSharedPreferences(Params.TABLE_ID, MODE_PRIVATE);
		if(pref.contains(Params.TABLE_ID))
		{
			int tableId = pref.getInt(Params.TABLE_ID, Integer.MIN_VALUE);
			mTable = new Table();
			mTable.aliasID = tableId;
		}
		
		//读取服务员锁定信息
		pref = getSharedPreferences(Params.PREFS_NAME, MODE_PRIVATE);
		if(pref.contains(Params.IS_FIX_STAFF))
		{
			mStaff = new StaffTerminal();
			long staffPin = pref.getLong(Params.STAFF_PIN, -1);
			mStaff.pin = staffPin;
		}
		
		mSettingItemHandler = new SettingItemHandler(this);
		// 	刷新按钮
		((Button) findViewById(R.id.button_setting_refresh)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new QueryMenuTask().execute();
			}
		});
		
		mListView = (ListView) findViewById(R.id.listView_setting);
		//set adapter
		mListView.setAdapter(new BaseAdapter(){
			@Override
			public int getCount() {
				return 3;
			}

			@Override
			public Object getItem(int position) {
				return null;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = convertView;
				if(view == null)
				{
					final LayoutInflater inflater = getLayoutInflater();
					view = inflater.inflate(R.layout.setting_item, null);
				}
				
				Switch switchBtn = (Switch) view.findViewById(R.id.switch_setting_item);
				final TextView hintTextView = (TextView) findViewById(R.id.textView_setting_hint);
				TextView itemName = (TextView)view.findViewById(R.id.textView_setting_item_itemName);
				TextView itemIntro = (TextView)view.findViewById(R.id.textView_setting_item_item_intro);
				final View container = findViewById(R.id.setting_fgm_container);
				 //根据不同的项分别设置行为
				switch(position)
				{
				case ITEM_TABLE:
					itemName.setText("绑定餐台");
					itemIntro.setText("");
					switchBtn.setVisibility(View.VISIBLE);
					//设置侦听器
					switchBtn.setOnCheckedChangeListener(new OnCheckedChangeListener(){
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							if(isChecked)
							{
								//固定餐台
								OptionBarFragment.setTableFixed(true);
								//更改样式
								if(mCurrentItem == ITEM_TABLE)
								{
									mSettingItemHandler.sendEmptyMessage(ITEM_TABLE);
									container.setVisibility(View.VISIBLE);
									hintTextView.setVisibility(View.INVISIBLE);
								}
							}
							else {
								//解绑餐台
								OptionBarFragment.setTableFixed(false);
								clearTable();
								//更改样式
								if(mCurrentItem == ITEM_TABLE){
									container.setVisibility(View.GONE);
									hintTextView.setVisibility(View.VISIBLE);
									hintTextView.setText("如需绑定餐台，请打开绑定开关");
								}
							}
							buttonView.setTag(isChecked);
						}
					});
					//初始化的时候根据原有的信息设置固定或解绑
					if(OptionBarFragment.isTableFixed()){
						switchBtn.setChecked(true);
						switchBtn.setTag(true);
					} else {
						switchBtn.setChecked(false);
						switchBtn.setTag(false);
					}
					
					break;
				case ITEM_STAFF:
					//基本同上
					itemName.setText("绑定服务员");
					itemIntro.setText("");
					switchBtn.setVisibility(View.VISIBLE);

					switchBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							if(isChecked)
							{
								OptionBarFragment.setStaffFixed(true);
								//更改样式
								if(mCurrentItem == ITEM_STAFF)
								{
									mSettingItemHandler.sendEmptyMessage(ITEM_STAFF);
									container.setVisibility(View.VISIBLE);
									hintTextView.setVisibility(View.INVISIBLE);
								}
							}
							else{
								OptionBarFragment.setStaffFixed(false);
								clearStaff();
								//更改样式
								if(mCurrentItem == ITEM_STAFF){
									container.setVisibility(View.GONE);
									hintTextView.setVisibility(View.VISIBLE);
									hintTextView.setText("如需绑定服务员账号，请打开绑定开关");
								}
							}
							buttonView.setTag(isChecked);
						}
					});
					//根据原有的信息设置固定或解开
					if(OptionBarFragment.isStaffFixed()){
						switchBtn.setChecked(true);
						switchBtn.setTag(true);
					} else {
						switchBtn.setChecked(false);
						switchBtn.setTag(false);
					}
					
					break;
					
				case ITEM_ADDRESS:
					itemName.setText("IP地址设置");
					switchBtn.setVisibility(View.INVISIBLE);
					itemIntro.setText("");
					break;
				}
				return view;
			}
			
		});
		//列表被单击的侦听
		mListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//改变点击颜色
				if(parent.getTag() != null)
					((View)parent.getTag()).setBackgroundDrawable(null);
				view.setBackgroundColor(Color.CYAN);
				parent.setTag(view);
				//初始化
				Switch switchBtn = (Switch) view.findViewById(R.id.switch_setting_item);
				TextView hintTextView = (TextView) findViewById(R.id.textView_setting_hint);
				View container = findViewById(R.id.setting_fgm_container);
				container.setVisibility(View.GONE);
				//改变点击之后的显示
				switch(position){
				case ITEM_TABLE:
					mCurrentItem = ITEM_TABLE;
					if((Boolean)switchBtn.getTag() == false){
						hintTextView.setVisibility(View.VISIBLE);
						hintTextView.setText("如需绑定餐台，请打开绑定开关");
					} else{
						mSettingItemHandler.sendEmptyMessage(ITEM_TABLE);
						hintTextView.setVisibility(View.INVISIBLE);
						container.setVisibility(View.VISIBLE);
					}
					break;
				case ITEM_STAFF:
					mCurrentItem = ITEM_STAFF;
					if((Boolean)switchBtn.getTag() == false){
						hintTextView.setVisibility(View.VISIBLE);
						hintTextView.setText("如需绑定服务员账号，请打开绑定开关");
					} else{
						mSettingItemHandler.sendEmptyMessage(ITEM_STAFF);
						hintTextView.setVisibility(View.INVISIBLE);
						container.setVisibility(View.VISIBLE);
					}
					break;
				case ITEM_ADDRESS:
					mCurrentItem = ITEM_ADDRESS;
					mSettingItemHandler.sendEmptyMessage(ITEM_ADDRESS);
					hintTextView.setVisibility(View.INVISIBLE);
					container.setVisibility(View.VISIBLE);
					break;
				}
			}
		});
		
		mListView.postDelayed(new Runnable(){
			@Override
			public void run() {
				mListView.performItemClick(mListView.getChildAt(0), 0, 0);
			}
		}, 100);
	}
	
	private static class SettingItemHandler extends Handler{
		private WeakReference<SettingActivity> mActivity;
		//current_fragment 确保每次点击列表项时只调用一次transaction
		private int CURRENT_FRAGMENT = Integer.MAX_VALUE;
		
		SettingItemHandler(SettingActivity activity){
			mActivity = new WeakReference<SettingActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			//根据不同的msg显示不同的fragment
			SettingActivity activity = mActivity.get();
			FragmentTransaction fgTrans = activity.getFragmentManager().beginTransaction();
			switch(msg.what){
			case ITEM_TABLE:
				if(CURRENT_FRAGMENT != ITEM_TABLE)
				{
					TablePanelFragment tableFgm = new TablePanelFragment(); 
					tableFgm.setOnTableChangedListener(activity);
					fgTrans.replace(R.id.setting_fgm_container, tableFgm).commit();
					CURRENT_FRAGMENT = ITEM_TABLE;
				}
				break;
			case ITEM_STAFF:
				if(CURRENT_FRAGMENT != ITEM_STAFF)
				{
					StaffPanelFragment staffFgm = new StaffPanelFragment();
					staffFgm.setOnStaffChangeListener(activity);
					fgTrans.replace(R.id.setting_fgm_container, staffFgm).commit();
					CURRENT_FRAGMENT = ITEM_STAFF;
				}
				break;
			case ITEM_ADDRESS:
				if(CURRENT_FRAGMENT != ITEM_ADDRESS){
					AddressSettingFragment adrFgm = new AddressSettingFragment();
					fgTrans.replace(R.id.setting_fgm_container, adrFgm).commit();
					CURRENT_FRAGMENT = ITEM_ADDRESS;
				}
				break;
			}
		}
	}

	@Override
	public void onTableChanged(Table table) {
		mTable = table;
		Toast.makeText(this, "已设定餐台为："+ table.aliasID, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onBackPressed() {
		boolean tableReady = false,staffReady = false;
		final Intent intent = new Intent();
		final Bundle bundle = new Bundle();
		if(isFoodChanged)
		{
			bundle.putBoolean(SettingActivity.FOODS_REFRESHED, true);
		}
		//判断是否绑定餐台
		if(OptionBarFragment.isTableFixed())
		{
			//如果餐台为空
			if(mTable == null)
			{
				tableReady = false; 
			}
			else {
				
				bundle.putParcelable(TableParcel.KEY_VALUE, new TableParcel(mTable));
				
				tableReady = true;
			}
		} else tableReady = true;
		//判断服务员是否绑定
		if(OptionBarFragment.isStaffFixed()){
			if(mStaff == null)
				staffReady = false;
			else staffReady = true;
		} else staffReady = true;
//		
		//如果都为空
		if(!tableReady && !staffReady)
			new AlertDialog.Builder(this).setTitle("餐台和服务员都未绑定，是否退出？").
			setPositiveButton("退出", new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//还原设置并退出
					OptionBarFragment.setStaffFixed(false);
					OptionBarFragment.setTableFixed(false);
					clearTable();
					clearStaff();
					dialog.dismiss();
					intent.putExtras(bundle);
					setResult(SETTING_RES_CODE, intent);
					SettingActivity.super.onBackPressed();
				}
			}).
			setNegativeButton("返回", null).show();
		//如果餐台为空
		else if(!tableReady)
			new AlertDialog.Builder(this).setTitle("尚未选择餐台，是否退出？").
			setPositiveButton("退出", new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					OptionBarFragment.setTableFixed(false);
					clearTable();
					dialog.dismiss();
					intent.putExtras(bundle);
					setResult(SETTING_RES_CODE, intent);
					SettingActivity.super.onBackPressed();
				}
			}).
			setNegativeButton("返回", null).show();
		//服务员为空
		else if(!staffReady)
			new AlertDialog.Builder(this).setTitle("尚未绑定服务员账号，是否退出？").
			setPositiveButton("退出", new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					OptionBarFragment.setStaffFixed(false);
					clearStaff();
					dialog.dismiss();
					intent.putExtras(bundle);

					setResult(SETTING_RES_CODE, intent);
					SettingActivity.super.onBackPressed();
				}
			}).
			setNegativeButton("返回", null).show();
		//都不为空
		else {
			intent.putExtras(bundle);
			setResult(SETTING_RES_CODE, intent);
			super.onBackPressed();
		}
	}
	//清除餐台状态
	private void clearTable(){
		mTable = null;
		SharedPreferences pref = this.getSharedPreferences(Params.TABLE_ID, MODE_PRIVATE);
		if(pref != null && pref.contains(Params.TABLE_ID))
		{
			Editor editor = pref.edit();
			editor.clear();
			editor.commit();
		}
	}
	//清除服务员状态
	private void clearStaff(){
		mStaff = null;
		SharedPreferences pref = this.getSharedPreferences(Params.PREFS_NAME, MODE_PRIVATE);
		if(pref != null && pref.contains(Params.IS_FIX_STAFF))
		{
			Editor editor = pref.edit();
			editor.remove(Params.IS_FIX_STAFF);
			editor.commit();
		}
	}
	
	private class QueryMenuTask extends com.wireless.lib.task.QueryMenuTask{
		private ProgressDialog mToast;

		private Comparator<Food> mFoodComp = new Comparator<Food>() {
			@Override
			public int compare(Food lhs, Food rhs) {
				if(lhs.getAliasId() == rhs.getAliasId()){
					return 0;
				}else if(lhs.getAliasId() > rhs.getAliasId()){
					return 1;
				}else{
					return -1;
				}
			}
		};
		
		/**
		 * 执行菜谱请求操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			mToast = ProgressDialog.show(SettingActivity.this, "","正在下载菜谱...请稍候");
		}
		
		/**
		 * 执行菜谱请求操作
		 */
		@Override
		protected FoodMenu doInBackground(Void... arg0) {
			FoodMenu foodMenu = super.doInBackground(arg0);
			if(foodMenu != null){
				/**
				 * Filter the food without image and sort the food by alias id
				 */
				List<Food> validFoods = new ArrayList<Food>();
				for(Food food : foodMenu.foods){
					if(food.image != null && !food.isSellOut()){
						validFoods.add(food);
					}
				}
				Collections.sort(validFoods, mFoodComp);
				WirelessOrder.foods = validFoods.toArray(new Food[validFoods.size()]);
			}
			return foodMenu;
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果菜谱请求成功，则继续进行请求餐厅信息的操作。
		 */
		@Override
		protected void onPostExecute(FoodMenu foodMenu){
			mToast.cancel();
			WirelessOrder.foodMenu = foodMenu;
			isFoodChanged  = true;
			/**
			 * Prompt user message if any error occurred,
			 * otherwise continue to query restaurant info.
			 */
			if(mErrMsg != null){
				new AlertDialog.Builder(SettingActivity.this)
				.setTitle("提示")
				.setMessage(mErrMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				}).show();
			}
		}
	}

	@Override
	public void onStaffChanged(StaffTerminal staff, String id, String pwd) {
		mStaff = staff;
	}
}	

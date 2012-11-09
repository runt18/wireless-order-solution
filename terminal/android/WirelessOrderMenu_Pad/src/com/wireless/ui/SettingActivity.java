package com.wireless.ui;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.fragment.OptionBarFragment;
import com.wireless.fragment.StaffPanelFragment;
import com.wireless.fragment.TablePanelFragment;
import com.wireless.fragment.TablePanelFragment.OnTableChangedListener;
import com.wireless.ordermenu.R;
import com.wireless.parcel.TableParcel;
import com.wireless.protocol.Table;

public class SettingActivity extends Activity implements OnTableChangedListener{

	//setting item flags
	private static final int ITEM_TABLE = 0;
	private static final int ITEM_STAFF = 1;
	public static final int SETTING_RES_CODE = 131;
	
	//current item flag
	private int mCurrentItem = ITEM_TABLE;
	
	//fragment handler
	private SettingItemHandler mSettingItemHandler;
	
	private ListView mListView;
	private Table mTable;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.setting);
		
		if(ShoppingCart.instance().hasTable())
			mTable = ShoppingCart.instance().getDestTable();
		
		mSettingItemHandler = new SettingItemHandler(this);

		mListView = (ListView) findViewById(R.id.listView_setting);
		//set adapter
		mListView.setAdapter(new BaseAdapter(){
			@Override
			public int getCount() {
				return 2;
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
				final View container = findViewById(R.id.setting_fgm_container);
				 //根据不同的项分别设置行为
				switch(position)
				{
				case ITEM_TABLE:
					((TextView)view.findViewById(R.id.textView_setting_item_itemName)).setText("绑定餐台");
					((TextView)view.findViewById(R.id.textView_setting_item_item_intro)).setText("");
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
					((TextView)view.findViewById(R.id.textView_setting_item_itemName)).setText("绑定服务员");
					((TextView)view.findViewById(R.id.textView_setting_item_item_intro)).setText("");
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
					fgTrans.replace(R.id.setting_fgm_container, staffFgm).commit();
					CURRENT_FRAGMENT = ITEM_STAFF;
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
		//判断是否绑定餐台
		if(OptionBarFragment.isTableFixed())
		{
			//如果餐台为空
			if(mTable == null && !ShoppingCart.instance().hasTable())
			{
				tableReady = false;
			}
			else {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				
				bundle.putParcelable(TableParcel.KEY_VALUE, new TableParcel(mTable));
				intent.putExtras(bundle);
				
				setResult(SETTING_RES_CODE, intent);
				tableReady = true;
			}
		} else tableReady = true;
		//判断服务员是否绑定
		if(OptionBarFragment.isStaffFixed()){
			if(!ShoppingCart.instance().hasStaff())
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
					dialog.dismiss();
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
					dialog.dismiss();
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
					dialog.dismiss();
					SettingActivity.super.onBackPressed();
				}
			}).
			setNegativeButton("返回", null).show();
		//都不为空
		else super.onBackPressed();
	}
}	

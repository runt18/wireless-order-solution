package com.wireless.ui;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.FragmentTransaction;
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
	// FIXME 修正多次点击后死机问题
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.setting);
		
		mSettingItemHandler = new SettingItemHandler(this);

		mListView = (ListView) findViewById(R.id.listView_setting);
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
				 
				switch(position)
				{
				case ITEM_TABLE:
					((TextView)view.findViewById(R.id.textView_setting_item_itemName)).setText("绑定餐台");
					((TextView)view.findViewById(R.id.textView_setting_item_item_intro)).setText("");
					switchBtn.setOnCheckedChangeListener(new OnCheckedChangeListener(){
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							if(isChecked)
							{
								OptionBarFragment.setTableFixed(true);
							}
							else {
								OptionBarFragment.setTableFixed(false);
							}
							buttonView.setTag(isChecked);
						}
					});
					
					if(OptionBarFragment.isTableFixed()){
						switchBtn.setChecked(true);
						switchBtn.setTag(true);
					} else {
						switchBtn.setChecked(false);
						switchBtn.setTag(false);
					}
					
					switchBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if(mCurrentItem == ITEM_TABLE && ((Boolean)v.getTag()) == true)
							{
								mSettingItemHandler.sendEmptyMessage(ITEM_TABLE);
								container.setVisibility(View.VISIBLE);
								hintTextView.setVisibility(View.INVISIBLE);
							}else if(mCurrentItem == ITEM_TABLE && ((Boolean)v.getTag()) == false){
								container.setVisibility(View.GONE);
								hintTextView.setVisibility(View.VISIBLE);
							}
								
						}
					});
						
					break;
				case ITEM_STAFF:
					((TextView)view.findViewById(R.id.textView_setting_item_itemName)).setText("绑定服务员");
					((TextView)view.findViewById(R.id.textView_setting_item_item_intro)).setText("");
					switchBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							if(isChecked)
							{
								OptionBarFragment.setStaffFixed(true);
							}
							else{
								OptionBarFragment.setStaffFixed(false);
							}
							buttonView.setTag(isChecked);

						}
					});
					
					if(OptionBarFragment.isStaffFixed()){
						switchBtn.setChecked(true);
						switchBtn.setTag(true);
					} else {
						switchBtn.setChecked(false);
						switchBtn.setTag(false);
					}
					
					switchBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if(mCurrentItem == ITEM_STAFF && ((Boolean)v.getTag()) == true)
							{
								mSettingItemHandler.sendEmptyMessage(ITEM_STAFF);
								container.setVisibility(View.VISIBLE);
								hintTextView.setVisibility(View.INVISIBLE);
							}else if(mCurrentItem == ITEM_STAFF && ((Boolean)v.getTag()) == false){
								container.setVisibility(View.GONE);
								hintTextView.setVisibility(View.VISIBLE);
							}
						}
					});
					break;
				}
				return view;
			}
			
		});
		
		mListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//改变点击颜色
				if(parent.getTag() != null)
					((View)parent.getTag()).setBackgroundDrawable(null);
				view.setBackgroundColor(Color.CYAN);
				parent.setTag(view);
				
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
		
	}
	
	private static class SettingItemHandler extends Handler{
		private WeakReference<SettingActivity> mActivity;
		
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
				TablePanelFragment tableFgm = new TablePanelFragment(); 
				tableFgm.setOnTableChangedListener(activity);
				fgTrans.replace(R.id.setting_fgm_container, tableFgm).commit();
				break;
			case ITEM_STAFF:
				StaffPanelFragment staffFgm = new StaffPanelFragment();
				fgTrans.replace(R.id.setting_fgm_container, staffFgm).commit();
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
		if(OptionBarFragment.isTableFixed())
		{
			if(mTable == null)
				Toast.makeText(this, "您尚未选择餐台，请选择要绑定的餐台", Toast.LENGTH_SHORT).show();
			else {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				
				bundle.putParcelable(TableParcel.KEY_VALUE, new TableParcel(mTable));
				intent.putExtras(bundle);
				
				setResult(SETTING_RES_CODE, intent);
			}
		}
		super.onBackPressed();

		// TODO 增加服务员的判断条件
	}
}	

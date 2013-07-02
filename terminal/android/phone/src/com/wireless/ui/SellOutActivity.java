package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodList;

public class SellOutActivity extends Activity {
	
	
	private ListView mSellOutListView;
	private List<Food> mSellOutFoods;
	private SellOutFoodHandler mFoodHandler;
	//the sign to index which page
	private static final int SELLING_PAGE = 0;
	private static final int SELL_OUT_PAGE = 1;
	private int mCurrentPage = SELLING_PAGE;
	//
	private String mConditionFilter = "";
	
	private static class SellOutFoodHandler extends Handler{
		private WeakReference<SellOutActivity> mActivity;
		private TextView mHintText;

		SellOutFoodHandler(SellOutActivity activity)
		{
			mActivity = new WeakReference<SellOutActivity>(activity);
			mHintText = (TextView) activity.findViewById(R.id.textView_hintText);
		}

		@Override
		public void handleMessage(Message msg) {
			SellOutActivity activity = mActivity.get();
			SellOutFoodAdapter adapter = null;
			activity.findViewById(R.id.button1).setPressed(false);
			activity.findViewById(R.id.button2).setPressed(false);
			
			switch(activity.mCurrentPage){
			case SELLING_PAGE:
				//TODO
				adapter = activity.new SellOutFoodAdapter(WirelessOrder.foodMenu.foods.filter(activity.mConditionFilter));
				activity.findViewById(R.id.button1).setPressed(true);
				break;
			case SELL_OUT_PAGE:
				adapter = activity.new SellOutFoodAdapter(new FoodList(activity.mSellOutFoods).filter(activity.mConditionFilter));
				activity.findViewById(R.id.button2).setPressed(true);
				break;
			default:
				adapter = activity.new SellOutFoodAdapter(WirelessOrder.foodMenu.foods.filter(activity.mConditionFilter));
			}
			
			if(adapter == null || adapter.getCount() == 0)	{
				activity.mSellOutListView.setAdapter(activity.new SellOutFoodAdapter(new ArrayList<Food>()));
				mHintText.setVisibility(View.VISIBLE);
			}else {
				mHintText.setVisibility(View.GONE);
				activity.mSellOutListView.setAdapter(adapter);
			}
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_sell_out);
		
		/*
		 * 返回Button和标题
		 */
		TextView title = (TextView) findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("沽清列表");

		TextView left = (TextView) findViewById(R.id.textView_left);
		left.setText("返回");
		left.setVisibility(View.VISIBLE);

		ImageButton back = (ImageButton) findViewById(R.id.btn_left);
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		TextView rightText = (TextView) findViewById(R.id.textView_right);
		rightText.setText("提交");
		rightText.setVisibility(View.VISIBLE);
		
		View commitButton = findViewById(R.id.btn_right);
		commitButton.setVisibility(View.VISIBLE);
		
		commitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Dialog dialog = new AlertDialog.Builder(SellOutActivity.this).setTitle("确认修改")
						.setView(new ListView(getBaseContext()))
//						.setView(SellOutActivity.this.getLayoutInflater().inflate(R.layout.listview_sell_out, null))
						.setPositiveButton("确定", null)
						.setNegativeButton("取消", null).create();
				ListView listView = (ListView) dialog.findViewById(R.id.listView_sell_out);
				listView.setAdapter(new BaseAdapter() {
					
					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						View layout = convertView;
						if(layout == null)
						{
							layout = LayoutInflater.from(SellOutActivity.this)
									.inflate(R.layout.sell_out_activity_confirm_dialog_list_item, null);
						}
						TextView nameText = (TextView) layout.findViewById(R.id.textView1);
						View deleteButton = layout.findViewById(R.id.button1);
						//TODO 添加菜名显示
						
						deleteButton.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								//TODO 添加删除逻辑
							}
						});
						return layout;
					}
					
					@Override
					public long getItemId(int position) {
						return position;
					}
					
					@Override
					public Object getItem(int position) {
						return position;
					}
					
					@Override
					public int getCount() {
						// TODO Auto-generated method stub
						return 0;
					}
				});
				//TODO 为列表添加数据
				
				
			}
		});
		
		mFoodHandler = new SellOutFoodHandler(this);
		new QuerySellOutTask().execute();
		mSellOutListView = (ListView) findViewById(R.id.listView_sell_out);
		
		View sellingButton =  findViewById(R.id.button1);
		sellingButton.setPressed(true);
		//set listener, it will jump to specific page
		sellingButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mCurrentPage = SELLING_PAGE;
				mFoodHandler.sendEmptyMessage(0);
			}
		});
		
		View selloutButton = findViewById(R.id.button2);
		selloutButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mCurrentPage = SELL_OUT_PAGE;
				mFoodHandler.sendEmptyMessage(0);
			}
		});
		
		//set search text watcher
		final EditText searchEdit = (EditText) findViewById(R.id.autoCompleteTextView_search);
		
		searchEdit.addTextChangedListener(new TextWatcher(){
        	
        	Runnable mSrchHandler = new Runnable(){
        		@Override
        		public void run(){
    				mFoodHandler.sendEmptyMessage(0);
        		}
        	};
        	
			@Override 
			public void afterTextChanged(Editable s) {}
			
			@Override
			public void beforeTextChanged(CharSequence s,int start, int count, int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().trim().length() != 0){
					mConditionFilter  = s.toString().trim();
					
					searchEdit.removeCallbacks(mSrchHandler);
					
					//如果搜索编号，马上执行搜索，
					//否则延迟500ms执行搜索
				    if(Pattern.compile("[0-9]*").matcher(mConditionFilter).matches()){;   
				    	searchEdit.postDelayed(mSrchHandler, 500);				    
				    }else{
						mFoodHandler.sendEmptyMessage(0);
				    }
				}else{
					mConditionFilter = "";
					mFoodHandler.sendEmptyMessage(0);
				}
			}
		});
        
		//删除搜索条件按钮
		((ImageButton) findViewById(R.id.button_clear)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				searchEdit.setText("");
			}
		});
	}

	private class SellOutFoodAdapter extends BaseAdapter{
		private List<Food> mSellOutFoods;

		SellOutFoodAdapter(List<Food> sellOutFoods){
			mSellOutFoods = sellOutFoods;
		}
		
		@Override
		public int getCount() {
			return mSellOutFoods.size();
		}

		@Override
		public Object getItem(int position) {
			return mSellOutFoods.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if(convertView == null){
				view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_sellout_list_item, null);
			}else{
				view = convertView;
			}
			
			//设置菜名和价格
			((TextView)view.findViewById(R.id.textView1)).setText(mSellOutFoods.get(position).getName());
			((TextView)view.findViewById(R.id.textView2)).setText("￥" + mSellOutFoods.get(position).getPrice());
			
			return view;
		}
	}
	/*
	 * 请求沽清菜
	 */
	private class QuerySellOutTask extends com.wireless.lib.task.QuerySellOutTask{
		private ProgressDialog mDialog;

		QuerySellOutTask(){
			super(WirelessOrder.pinGen, WirelessOrder.foodMenu.foods);
		}
		
		@Override
		protected void onPreExecute() {
			mDialog = ProgressDialog.show(SellOutActivity.this, "", "正在更新沽清列表");
		}

		@Override
		protected void onPostExecute(Food[] sellOutFoods) {
			mSellOutFoods = Arrays.asList(sellOutFoods);
			mDialog.dismiss();
			mFoodHandler.sendEmptyMessage(0);
		}
	}
}

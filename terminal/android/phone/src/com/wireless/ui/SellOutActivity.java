package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodList;
import com.wireless.ui.dialog.SelloutCommitDialog;

public class SellOutActivity extends FragmentActivity {

	//the sign to index which page
	private static final int ON_SALE_PAGE = 0;
	private static final int SELL_OUT_PAGE = 1;

	private ListView mSellOutListView;
	//在售菜品
	private FoodList mSellOutFoods;
	//停售菜品
	private FoodList mOnSaleFoods;
	//将要停售的菜品
	private List<Food> mToSellout = new ArrayList<Food>();
	//将要开售的菜品
	private List<Food> mToOnSale = new ArrayList<Food>();
	
	private QuerySellOutTask mQuerySellOutTask;
	
	private FoodListHandler mFoodListHandler;
	//当前Tab
	private int mCurrentPage = ON_SALE_PAGE;
	//查找的条件
	private String mConditionFilter = "";
	
	private static class FoodListHandler extends Handler{
		private WeakReference<SellOutActivity> mActivity;
		private TextView mHintText;

		FoodListHandler(SellOutActivity activity){
			mActivity = new WeakReference<SellOutActivity>(activity);
			mHintText = (TextView) activity.findViewById(R.id.textView_hintText);
		}

		@Override
		public void handleMessage(Message msg) {
			
			SellOutActivity activity = mActivity.get();
			
			if(activity.mOnSaleFoods == null || activity.mSellOutFoods == null){
				return;
			}
			
			SellOutFoodAdapter adapter;
			activity.findViewById(R.id.button_OnSale_List).setPressed(false);
			activity.findViewById(R.id.button_Sellout_List).setPressed(false);
			
			//设置底部数量显示
			((TextView)activity.findViewById(R.id.txtView_amount_onSale)).setText("" + activity.mOnSaleFoods.size());
			((TextView)activity.findViewById(R.id.txtView_amount_sellOut)).setText("" + activity.mSellOutFoods.size());
			
			switch(msg.what){
			case ON_SALE_PAGE:
				activity.mCurrentPage = ON_SALE_PAGE;
				adapter = activity.new SellOutFoodAdapter(activity.mOnSaleFoods.filter(activity.mConditionFilter));
				activity.findViewById(R.id.button_OnSale_List).setPressed(true);
				break;
			case SELL_OUT_PAGE:
				activity.mCurrentPage = SELL_OUT_PAGE;
				adapter = activity.new SellOutFoodAdapter(activity.mSellOutFoods.filter(activity.mConditionFilter));
				activity.findViewById(R.id.button_Sellout_List).setPressed(true);
				break;
			default:
				activity.mCurrentPage = ON_SALE_PAGE;
				adapter = activity.new SellOutFoodAdapter(WirelessOrder.foodMenu.foods.filter(activity.mConditionFilter));
			}
			
			if(adapter.getCount() == 0)	{
				mHintText.setVisibility(View.VISIBLE);
			}else {
				mHintText.setVisibility(View.GONE);
			}
			
			activity.mSellOutListView.setAdapter(adapter);
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.sell_out_activity);
		
		//返回Button和标题
		TextView title = (TextView) findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("快速沽清");

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
		
		//"提交"按钮
		commitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!mToOnSale.isEmpty() || !mToSellout.isEmpty()){
					SelloutCommitDialog.newInstance(mToSellout, mToOnSale).show(getSupportFragmentManager(), SelloutCommitDialog.TAG);
				}else{
					Toast.makeText(SellOutActivity.this, "没有任何沽清或开售菜品信息更新", Toast.LENGTH_SHORT).show();
				}
			}
		});

		//初始化FoodListHandler
		mFoodListHandler = new FoodListHandler(this);

		//更新沽清菜品信息
		mQuerySellOutTask = new QuerySellOutTask();
		mQuerySellOutTask.execute();
		
		//"在售"Button
		View onSaleBtn = findViewById(R.id.button_OnSale_List);
		onSaleBtn.setPressed(true);
		onSaleBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mFoodListHandler.sendEmptyMessage(ON_SALE_PAGE);
			}
		});
		
		//"停售"Button
		View selloutBtn = findViewById(R.id.button_Sellout_List);
		selloutBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mFoodListHandler.sendEmptyMessage(SELL_OUT_PAGE);
			}
		});
		
		//set search text watcher
		final EditText searchEdit = (EditText) findViewById(R.id.autoCompleteTextView_search);
		
		searchEdit.addTextChangedListener(new TextWatcher(){
        	
        	Runnable mSrchHandler = new Runnable(){
        		@Override
        		public void run(){
    				mFoodListHandler.sendEmptyMessage(mCurrentPage);
        		}
        	};
        	
			@Override 
			public void afterTextChanged(Editable s) {}
			
			@Override
			public void beforeTextChanged(CharSequence s,int start, int count, int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().trim().length() != 0){
					mConditionFilter = s.toString().trim();
					
					searchEdit.removeCallbacks(mSrchHandler);
					
					//如果搜索编号，马上执行搜索，
					//否则延迟500ms执行搜索
				    if(Pattern.compile("[0-9]*").matcher(mConditionFilter).matches()){;   
				    	searchEdit.postDelayed(mSrchHandler, 500);				    
				    }else{
						mFoodListHandler.sendEmptyMessage(mCurrentPage);
				    }
				}else{
					mConditionFilter = "";
					mFoodListHandler.sendEmptyMessage(mCurrentPage);
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
		
		mSellOutListView = (ListView) findViewById(R.id.listView_sell_out);
		
		//滚动时隐藏soft-keyboard
		mSellOutListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		mQuerySellOutTask.cancel(true);
	}
	
	private class SellOutFoodAdapter extends BaseAdapter{
		private List<Food> mFoods;

		SellOutFoodAdapter(List<Food> sellOutFoods){
			mFoods = sellOutFoods;
		}
		
		@Override
		public int getCount() {
			return mFoods.size();
		}

		@Override
		public Object getItem(int position) {
			return mFoods.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View layout;
			if(convertView == null){
				layout = LayoutInflater.from(getApplicationContext()).inflate(R.layout.sellout_list_item, null);
			}else{
				layout = convertView;
			}
			
			final Food food = mFoods.get(position);
			//"停"or"售"Button
			final Button button = (Button)layout.findViewById(R.id.button_sellOut_listItem);
			
			if(mCurrentPage == ON_SALE_PAGE){
				if(mToSellout.indexOf(food) >= 0){
					layout.findViewById(R.id.view_huaxian_sellOut_listItem).setVisibility(View.VISIBLE);
				}else{
					layout.findViewById(R.id.view_huaxian_sellOut_listItem).setVisibility(View.GONE);
				}
				
			}else if(mCurrentPage == SELL_OUT_PAGE){
				if(mToOnSale.indexOf(food) >= 0){
					layout.findViewById(R.id.view_huaxian_sellOut_listItem).setVisibility(View.GONE);
				}else{
					layout.findViewById(R.id.view_huaxian_sellOut_listItem).setVisibility(View.VISIBLE);
				}
			}
			
			button.setOnClickListener(new OnClickListener(){

				public void onClick(View v) {
					if(mCurrentPage == ON_SALE_PAGE){
						if(mToSellout.indexOf(food) >= 0){
							mToSellout.remove(food);
							layout.findViewById(R.id.view_huaxian_sellOut_listItem).setVisibility(View.GONE);
						}else{
							mToSellout.add(food);
							layout.findViewById(R.id.view_huaxian_sellOut_listItem).setVisibility(View.VISIBLE);
						}
					}else if(mCurrentPage == SELL_OUT_PAGE){
						if(mToOnSale.indexOf(food) >= 0){
							mToOnSale.remove(food);
							layout.findViewById(R.id.view_huaxian_sellOut_listItem).setVisibility(View.VISIBLE);
						}else{
							mToOnSale.add(food);
							layout.findViewById(R.id.view_huaxian_sellOut_listItem).setVisibility(View.GONE);
						}
					}
					//mFoodListHandler.sendEmptyMessage(mCurrentPage);
				}
				
			});
			
			//设置菜名和价格
			((TextView)layout.findViewById(R.id.txtView_foodName_sellOut_listItem)).setText(mFoods.get(position).getName());
			((TextView)layout.findViewById(R.id.txtView_price_sellOut_listItem)).setText("￥" + mFoods.get(position).getPrice());
			
			return layout;
		}
	}
	
	/**
	 * 请求更新沽清菜品
	 */
	private class QuerySellOutTask extends com.wireless.lib.task.QuerySellOutTask{
		
		QuerySellOutTask(){
			super(WirelessOrder.loginStaff, WirelessOrder.foodMenu.foods);
		}
		
		@Override
		protected void onPostExecute(Food[] sellOutFoods){
			if(mProtocolException != null){
				Toast.makeText(SellOutActivity.this, "沽清菜品更新失败", Toast.LENGTH_SHORT).show();				
			}else{
				Toast.makeText(SellOutActivity.this, "沽清菜品更新成功", Toast.LENGTH_SHORT).show();
				List<Food> sellOut = new ArrayList<Food>();
				List<Food> onSale = new ArrayList<Food>();
				for(Food f : WirelessOrder.foodMenu.foods){
					if(f.isSellOut()){
						sellOut.add(f);
					}else{
						onSale.add(f);
					}
				}
				mSellOutFoods = new FoodList(sellOut);
				mOnSaleFoods = new FoodList(onSale);
				mFoodListHandler.sendEmptyMessage(mCurrentPage);
			}
		}
	}
}

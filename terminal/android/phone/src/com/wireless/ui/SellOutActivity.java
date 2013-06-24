package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.pojo.menuMgr.Food;

public class SellOutActivity extends Activity {
	
	private ListView mSellOutListView;
	private List<Food> mSellOutFoods;
	private SellOutFoodHandler mFoodHandler;
	
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
			SellOutFoodAdapter adapter = activity.new SellOutFoodAdapter(activity.mSellOutFoods);
			if(adapter.getCount() == 0)	{
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
				//TODO 添加弹出框
			}
		});
		
		mFoodHandler = new SellOutFoodHandler(this);
		new QuerySellOutTask().execute();
		mSellOutListView = (ListView) findViewById(R.id.listView_sell_out);
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
				view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.sell_out_item, null);
			}else{
				view = convertView;
			}
			
			//设置菜名和价格
			((TextView)view.findViewById(R.id.textView_name_sellOut)).setText(mSellOutFoods.get(position).getName());
			((TextView)view.findViewById(R.id.textView_price_sellOut)).setText("" + mSellOutFoods.get(position).getPrice());
			
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

package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

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
import com.wireless.protocol.Food;

public class SellOutActivity extends Activity {
	private static final int REFRESH_FOODS = 12386;
	
	private ListView mSellOutListView;
	private FoodHandler mFoodHandler;
	
	private static class FoodHandler extends Handler{
		private WeakReference<SellOutActivity> mActivity;

		FoodHandler(SellOutActivity activity)
		{
			mActivity = new WeakReference<SellOutActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			SellOutActivity activity = mActivity.get();
			activity.mSellOutListView.setAdapter(activity.new FoodAdapter(WirelessOrder.foodMenu.foods));
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.sell_out);
		
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
		
		mFoodHandler = new FoodHandler(this);
		new QuerySellOutTask().execute(WirelessOrder.foodMenu.foods);
		mSellOutListView = (ListView) findViewById(R.id.listView_sell_out);
	}

	class FoodAdapter extends BaseAdapter{
		private ArrayList<Food> mSellOutFoods;

		FoodAdapter(Food[] oriFoods){
			mSellOutFoods = new ArrayList<Food>();
			//选出停售的菜
			for(Food f:oriFoods)
			{
				if(f.isSellOut())
				{
					mSellOutFoods.add(f);
				}
			}
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
			if(convertView == null)
				view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.sell_out_item, null);
			else view = convertView;
			//设置菜名和价格
			((TextView)view.findViewById(R.id.textView_name_sellOut)).setText(mSellOutFoods.get(position).name);
			((TextView)view.findViewById(R.id.textView_price_sellOut)).setText("" + mSellOutFoods.get(position).getPrice());
			
			return view;
		}
	}
	/*
	 * 请求沽清菜
	 */
	private class QuerySellOutTask extends com.wireless.lib.task.QuerySellOutTask{
		private ProgressDialog mDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mDialog = ProgressDialog.show(SellOutActivity.this, "", "正在更新沽清列表");
		}

		@Override
		protected void onPostExecute(Food[] result) {
			super.onPostExecute(result);
			mDialog.dismiss();
			mFoodHandler.sendEmptyMessage(REFRESH_FOODS);
		}
	}
}

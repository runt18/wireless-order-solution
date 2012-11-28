package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.excep.BusinessException;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.util.imgFetcher.ImageFetcher;

public class RankListActivity extends Activity {
	private static final int REFRESH_RANK_LIST = 11;
	private static final String CURRENT_FOOD = "current_food";
	private static final short DEPT_ALL = Short.MAX_VALUE;
	
	private ImageFetcher mImageFetcher;
	private RankListHandler mRankListHandler;
	private ImageHandler mImageHandler;

	private Food[] mOriFoods;
	private ArrayList<Kitchen> mValidKitchens;
	
	private short mDeptFilter = Short.MAX_VALUE;
	private int mType;

	public static final String RANK_ACTIVITY_TYPE = "rankActivityType";
	public static final int TYPE_SELL = 1;
	public static final int TYPE_REC = 2;
	public static final int TYPE_SPCIAL = 3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rank_list);
		
		mImageFetcher = new ImageFetcher(this, 600, 600);
		mRankListHandler = new RankListHandler(this);
		mImageHandler = new ImageHandler(this);

		Intent intent = getIntent();
		mType = intent.getIntExtra(RANK_ACTIVITY_TYPE, 1);
		
		TextView logoText = (TextView) findViewById(R.id.textView_rankList_logo);
		switch(mType)
		{
		case TYPE_SELL:
			mOriFoods = new Food[WirelessOrder.foodMenu.foods.length];
			System.arraycopy(WirelessOrder.foodMenu.foods, 0, mOriFoods, 0,
					WirelessOrder.foodMenu.foods.length);
			logoText.setText("排行榜");
			break;
		case TYPE_REC:
			ArrayList<Food> recFoods = new ArrayList<Food>();
			for(Food f:WirelessOrder.foodMenu.foods)
			{
				if(f.isRecommend())
					recFoods.add(f);
			}
			mOriFoods = recFoods.toArray(new Food[recFoods.size()]);
			logoText.setText("主厨推荐");
			break;
		case TYPE_SPCIAL:
			ArrayList<Food> speFoods = new ArrayList<Food>();
			for(Food f:WirelessOrder.foodMenu.foods)
			{
				if(f.isSpecial())
					speFoods.add(f);
			}
			mOriFoods = speFoods.toArray(new Food[speFoods.size()]);
			logoText.setText("今日特价");
			break;
		}
		/*
		 * 将所有菜品进行按厨房编号进行排序
		 */

		Arrays.sort(mOriFoods, new Comparator<Food>() {
			@Override
			public int compare(Food food1, Food food2) {
				if (food1.kitchen.aliasID > food2.kitchen.aliasID) {
					return 1;
				} else if (food1.kitchen.aliasID < food2.kitchen.aliasID) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		/*
		 * 使用二分查找算法筛选出有菜品的厨房
		 */
		mValidKitchens = new ArrayList<Kitchen>();
		for (int i = 0; i < WirelessOrder.foodMenu.kitchens.length; i++) {
			Food keyFood = new Food();
			keyFood.kitchen.aliasID = WirelessOrder.foodMenu.kitchens[i].aliasID;
			int index = Arrays.binarySearch(mOriFoods, keyFood,
					new Comparator<Food>() {

						public int compare(Food food1, Food food2) {
							if (food1.kitchen.aliasID > food2.kitchen.aliasID) {
								return 1;
							} else if (food1.kitchen.aliasID < food2.kitchen.aliasID) {
								return -1;
							} else {
								return 0;
							}
						}
					});

			if (index >= 0) {
				mValidKitchens.add(WirelessOrder.foodMenu.kitchens[i]);
			}
		}
		/*
		 * 筛选出有菜品的部门
		 */
		ArrayList<Department> mValidDepts = new ArrayList<Department>();
		//设置"全部 "这个厨房
		mValidDepts.add(new Department("全部", DEPT_ALL,0,DEPT_ALL));
		
		for (int i = 0; i < WirelessOrder.foodMenu.depts.length; i++) {
			for (int j = 0; j < mValidKitchens.size(); j++) {
				if (WirelessOrder.foodMenu.depts[i].deptID == mValidKitchens.get(j).dept.deptID) {
					mValidDepts.add(WirelessOrder.foodMenu.depts[i]);
					break;
				}
			}
		}
		
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		final LinearLayout deptLayout = (LinearLayout) findViewById(R.id.linearLayout_dept_rankList);
	
		//为每个厨房添加按钮
		for(Department d:mValidDepts)
		{
			TextView textView = new TextView(this);
			textView.setLayoutParams(lp);
			textView.setText(d.name);
			textView.setGravity(Gravity.CENTER);
			textView.setTextSize(26f);
			textView.setTextColor(getResources().getColor(R.color.brown));
			textView.setBackgroundResource(R.drawable.rank_list_dept);
			
			deptLayout.addView(textView);
			textView.setTag(d);
			
			textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//设置点击效果
					if(deptLayout.getTag() != null)
						((View)deptLayout.getTag()).setBackgroundResource(R.drawable.rank_list_dept);
					v.setBackgroundResource(R.drawable.rank_list_dept_selected);
					deptLayout.setTag(v);
					
					Department dept = (Department) v.getTag();
					if(dept != null)
					{//刷新排行榜显示
						mDeptFilter = dept.deptID;
					}
					else mDeptFilter = DEPT_ALL;
					mRankListHandler.sendEmptyMessage(REFRESH_RANK_LIST);

				}
			});
		}
		deptLayout.getChildAt(0).performClick();
	}

    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
        mImageFetcher.clearCache();
    }
	
	private static class RankListHandler extends Handler{
		private WeakReference<RankListActivity> mActivity;
		private ListView mRankListView;
		
		RankListHandler(RankListActivity activity) {
			mActivity = new WeakReference<RankListActivity>(activity);
			mRankListView = (ListView)activity.findViewById(R.id.listView_rankList);
		}
		@Override
		public void handleMessage(Message msg) {
			final RankListActivity activity = mActivity.get();
			final ArrayList<Food> sortedFoods = new ArrayList<Food>();
			ArrayList<Food> allFoods = new ArrayList<Food>();
			allFoods.clear();
			sortedFoods.clear();
			//如果不是全部选项
			if(activity.mDeptFilter != RankListActivity.DEPT_ALL)
			{
				//根据条件筛选出厨房
				ArrayList<Kitchen> kitchens = new ArrayList<Kitchen>();
				for(Kitchen k:activity.mValidKitchens)
				{
					if(k.dept.deptID == activity.mDeptFilter ){
						kitchens.add(k);
					}
				}
				//筛选出这些厨房中包含的菜品
				for(Food f:activity.mOriFoods)
				{
					for(Kitchen k:kitchens)
						if(f.kitchen.aliasID == k.aliasID && f.image != null)
						{
							allFoods.add(f);
						}
				}
			//如果是全部选项
			} else {
				for(Food f:activity.mOriFoods)
				{
					if(f.image != null)
						allFoods.add(f);
				}
			}
			//将选出的菜品按频率排序
			switch(activity.mType){
				case TYPE_SELL:
					Collections.sort(allFoods, new Comparator<Food>(){
						@Override
						public int compare(Food lhs, Food rhs) {
							if(lhs.statistics.orderCnt > rhs.statistics.orderCnt)
								return -1;
							else if(lhs.statistics.orderCnt < rhs.statistics.orderCnt)
								return 1;
							else return 0;
						}
					});
					break;
				case TYPE_REC:
				case TYPE_SPCIAL:
					Collections.sort(allFoods, new Comparator<Food>(){
						@Override
						public int compare(Food lhs, Food rhs) {
							if(lhs.isHot() && !rhs.isHot()){
								return -1;
							}else if(!lhs.isHot() && rhs.isHot()){
								return 1;
							}else
								return 0;
						}
					});
			}

			//提取最多前10个菜品
			if(allFoods.size() >= 10)
				for(int i = 0;i<10;i++)
				{
					sortedFoods.add(allFoods.get(i));
				}
			else 
				for(Food f:allFoods)
				{
					sortedFoods.add(f);
				}
			
			mRankListView.setAdapter(new BaseAdapter(){

				@Override
				public int getCount() {
					return sortedFoods.size();
				}

				@Override
				public Object getItem(int position) {
					return sortedFoods.get(position);
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
						final LayoutInflater lf = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						view = lf.inflate(R.layout.rank_list_item, null);
					}
					Food food = sortedFoods.get(position);
					view.setTag(food);
					//显示排位信息
					TextView foodNameTextView = (TextView)view.findViewById(R.id.textView_name_rankList_item);
					if(food.name.length() > 7)
						foodNameTextView.setText(food.name.substring(0, 7));
					else foodNameTextView.setText(food.name);
					
					TextView numberTextView = (TextView)view.findViewById(R.id.textView_num_rankList_item);
					numberTextView.setText("" + ++position);
					
					int color = 0;
					switch(position)
					{
					case 1:
						color = activity.getResources().getColor(R.color.exRed);
						foodNameTextView.setTextColor(color);
						numberTextView.setTextColor(color);
						break;
					case 2:
						color = activity.getResources().getColor(R.color.red_orange);
						foodNameTextView.setTextColor(color);
						numberTextView.setTextColor(color);
						break;
					case 3:
						color = activity.getResources().getColor(R.color.orange);
						foodNameTextView.setTextColor(color);
						numberTextView.setTextColor(color);
						break;
					}
					return view;
				}
				
			});
			
			mRankListView.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Food food = (Food)view.getTag();
					Message msg = new Message();
					Bundle data = new Bundle();
					//将点击的菜品传给handler
					data.putParcelable(RankListActivity.CURRENT_FOOD, new FoodParcel(new OrderFood(food)));
					msg.setData(data);
					activity.mImageHandler.sendMessage(msg);
					//设置点击的显示
					if(parent.getTag() != null)
						((View)parent.getTag()).setBackgroundColor(activity.getResources().getColor(R.color.gray));
					view.setBackgroundColor(activity.getResources().getColor(R.color.blue));
					parent.setTag(view);
				}
			});
			//设置第一个显示
			mRankListView.postDelayed(new Runnable(){
				@Override
				public void run() {
					if(!mRankListView.getAdapter().isEmpty())
						mRankListView.performItemClick(mRankListView.getChildAt(0), 0, 0);
				}
			}, 100);
		}
	}
	
	private static class ImageHandler extends Handler{
//		private WeakReference<RankListActivity> mActivity;
		private ImageView mImageView;
		private ImageFetcher mFetcher;
		private Button addBtn;
		private TextView mPriceTextView;
		private View pickedHintView;
		private TextView mPickedText;
		private TextView mNameTextView;
		
		ImageHandler(final RankListActivity activity) {
//			mActivity = new WeakReference<RankListActivity>(activity);
			mImageView = (ImageView)activity.findViewById(R.id.imageView_rankList);
			mImageView.setScaleType(ScaleType.CENTER_CROP);
//			mImageView.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			mFetcher = new ImageFetcher(activity,470,400);
			
			mPriceTextView = (TextView) activity.findViewById(R.id.textView_rankList_price);
			mNameTextView = (TextView) activity.findViewById(R.id.textView_rankList_name);
			addBtn = (Button) activity.findViewById(R.id.button_rankList_add_dish);
			addBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					OrderFood food = (OrderFood) v.getTag();
					if(food != null){
						try {
							food.setCount(1f);
							ShoppingCart.instance().addFood(food);
							
							food = ShoppingCart.instance().getFood(food.getAliasId());
							
							pickedHintView.setVisibility(View.VISIBLE);
							mPickedText.setVisibility(View.VISIBLE);
							mPickedText.setText(Util.float2String2(food.getCount()));
							Toast.makeText(activity, "成功添加一份"+food.name, Toast.LENGTH_SHORT).show();
						} catch (BusinessException e) {
							e.printStackTrace();
						}
					}
				}
			});
			
			pickedHintView = activity.findViewById(R.id.textView_rankList_picked_hint);
			mPickedText = (TextView) activity.findViewById(R.id.textView_rankList_picked);
		}

		@Override
		public void handleMessage(Message msg) {
//			final RankListActivity activity = mActivity.get();
			//替换图片
			OrderFood food = msg.getData().getParcelable(RankListActivity.CURRENT_FOOD);
			if(food.image != null)
				mFetcher.loadImage(food.image, mImageView);
			else mImageView.setImageResource(R.drawable.null_pic);
			
			mNameTextView.setText(food.name);
			mPriceTextView.setText(Util.float2String2(food.getPrice()));
			addBtn.setTag(food);
			
			food = ShoppingCart.instance().getFood(food.getAliasId());
			if(food != null)
			{
				pickedHintView.setVisibility(View.VISIBLE);
				mPickedText.setText(Util.float2String2(food.getCount()));
				mPickedText.setVisibility(View.VISIBLE);
			} else {
				mPickedText.setVisibility(View.GONE);
				pickedHintView.setVisibility(View.GONE);
			}
			
		}
	}
}

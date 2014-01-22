package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
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
import com.wireless.exception.BusinessException;
import com.wireless.ordermenu.R;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.util.imgFetcher.ImageFetcher;

/**
 * RankList activity
 * <p>this activity can display in 3 model, the rank list model/recommend food model/special offer model.
 * <br/>
 * each model is different with title and some sort method might be different, but the integral showing is the same.
 * the default model is {@link #TYPE_SELL}, you may change the model using {@link Intent#putExtra(String, int)} before 
 * start this activity</p>
 *  
 * @author ggdsn1
 * @see #TYPE_REC
 * @see #TYPE_SELL
 * @see #TYPE_SPCIAL
 */
public class RankListActivity extends Activity {
	private static final int REFRESH_RANK_LIST = 11;
	private static final String CURRENT_FOOD = "current_food";
	private static final short DEPT_ALL = Short.MAX_VALUE;
	
	private ImageFetcher mImageFetcher;
	private RankListHandler mRankListHandler;
	private ImageHandler mImageHandler;

	private List<Food> mOriFoods;
//	private ArrayList<PKitchen> mValidKitchens;
	
	private short mDeptFilter = Short.MAX_VALUE;
	private int mType;

	public static final String RANK_ACTIVITY_TYPE = "rankActivityType";
	/**
	 * the type of rank list
	 */
	public static final int TYPE_SELL = 1;
	/**
	 * the type of recommend food
	 */
	public static final int TYPE_REC = 2;
	/**
	 * the type of special offer food
	 */
	public static final int TYPE_SPCIAL = 3;
	private static final String TAG = "RankListActivity";
	
	/**
	 * initial the main image and sort foods
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rank_list);
		
		mImageFetcher = new ImageFetcher(this, 0, 0);
		
		final ImageView mImageView = (ImageView)findViewById(R.id.imageView_rankList);
		//set the imageView's size
		mImageView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				if(mImageView.getHeight() > 0)
				{
					mImageFetcher.setImageSize(mImageView.getWidth(), mImageView.getHeight());
					mImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		});
		// initial handler
		mRankListHandler = new RankListHandler(this);
		mImageHandler = new ImageHandler(this);

		Intent intent = getIntent();
		mType = intent.getIntExtra(RANK_ACTIVITY_TYPE, 1);
		//show different logo by different type
		TextView logoText = (TextView) findViewById(R.id.textView_combo_food_intro);
		switch(mType)
		{
		case TYPE_SELL:
			mOriFoods = WirelessOrder.foods;
			logoText.setText("排行榜");
			break;
			
		case TYPE_REC:
			mOriFoods = new ArrayList<Food>();
			for(Food f : WirelessOrder.foods){
				if(f.isRecommend() && !f.isSellOut()){
					mOriFoods.add(f);
				}
			}
			logoText.setText("主厨推荐");
			break;
			
		case TYPE_SPCIAL:
			mOriFoods = new ArrayList<Food>();
			for(Food f : WirelessOrder.foods){
				if(f.isSpecial() && !f.isSellOut()){
					mOriFoods.add(f);
				}
			}
			logoText.setText("今日特价");
			break;
		}
		
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		final LinearLayout deptLayout = (LinearLayout) findViewById(R.id.linearLayout_dept_rankList);
	
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Log.i(TAG, "heightPixel is " + dm.heightPixels);
		
		float textSize = 26f;
		if(dm.heightPixels <= 600)
			textSize = 20f;
		
		//为每个厨房添加按钮
		for(Department d : WirelessOrder.foodMenu.depts)
		{
			TextView textView = new TextView(this);
			textView.setLayoutParams(lp);
			textView.setText(d.getName());
			textView.setGravity(Gravity.CENTER);
			textView.setTextSize(textSize);
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
						mDeptFilter = dept.getId();
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
	/**
	 * according to the kitchen , sort the matched foods and display
	 * @author ggdsn1
	 *
	 */
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
				for(Kitchen k : WirelessOrder.foodMenu.kitchens)
				{
					if(k.getDept().getId() == activity.mDeptFilter ){
						kitchens.add(k);
					}
				}
				//筛选出这些厨房中包含的菜品
				for(Food f:activity.mOriFoods)
				{
					for(Kitchen k:kitchens)
						if(f.getKitchen().equals(k) && f.hasImage())
						{
							allFoods.add(f);
						}
				}
			//如果是全部选项
			} else {
				for(Food f:activity.mOriFoods)
				{
					if(f.hasImage())
						allFoods.add(f);
				}
			}
			//将选出的菜品按频率排序
			switch(activity.mType){
				case TYPE_SELL:
					Collections.sort(allFoods, new Comparator<Food>(){
						@Override
						public int compare(Food lhs, Food rhs) {
							if(lhs.statistics.getOrderCnt() > rhs.statistics.getOrderCnt())
								return -1;
							else if(lhs.statistics.getOrderCnt() < rhs.statistics.getOrderCnt())
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
			//set the listAdapter
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
					if(food.getName().length() > 7)
						foodNameTextView.setText(food.getName().substring(0, 7));
					else foodNameTextView.setText(food.getName());
					
					TextView numberTextView = (TextView)view.findViewById(R.id.textView_num_rankList_item);
					numberTextView.setText("" + ++position);
					//top three will use different color to display
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
					data.putParcelable(RankListActivity.CURRENT_FOOD, new OrderFoodParcel(new OrderFood(food)));
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
	/**
	 * main imageView's image handler,according to the Selected food,display it's image and other properties
	 * @author ggdsn1
	 *
	 */
	private static class ImageHandler extends Handler{
		private WeakReference<RankListActivity> mActivity;
		private ImageView mImageView;
		private Button addBtn;
		private TextView mPriceTextView;
		private View pickedHintView;
		private TextView mPickedText;
		private TextView mNameTextView;
		
		ImageHandler(final RankListActivity activity) {
			mActivity = new WeakReference<RankListActivity>(activity);
			mImageView = (ImageView)activity.findViewById(R.id.imageView_rankList);
			mImageView.setScaleType(ScaleType.CENTER_CROP);
			
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
							
							food = ShoppingCart.instance().searchNewFoodByAlias(food.getAliasId());
							
							pickedHintView.setVisibility(View.VISIBLE);
							mPickedText.setVisibility(View.VISIBLE);
							mPickedText.setText(NumericUtil.float2String2(food.getCount()));
							Toast.makeText(activity, "成功添加一份"+food.getName(), Toast.LENGTH_SHORT).show();
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
			final RankListActivity activity = mActivity.get();
			//替换图片
			OrderFood food = ((OrderFoodParcel) msg.getData().getParcelable(RankListActivity.CURRENT_FOOD)).asOrderFood();
			
			if(food.asFood().hasImage()){
				activity.mImageFetcher.loadImage(food.asFood().getImage(), mImageView);
			}else{
				mImageView.setImageResource(R.drawable.null_pic);
			}
			
			mNameTextView.setText(food.getName());
			mPriceTextView.setText(NumericUtil.float2String2(food.getPrice()));
			addBtn.setTag(food);
			
			food = ShoppingCart.instance().searchFoodByAlias(food.getAliasId());
			if(food != null)
			{
				pickedHintView.setVisibility(View.VISIBLE);
				mPickedText.setText(NumericUtil.float2String2(food.getCount()));
				mPickedText.setVisibility(View.VISIBLE);
			} else {
				mPickedText.setVisibility(View.GONE);
				pickedHintView.setVisibility(View.GONE);
			}
			
		}
	}
}

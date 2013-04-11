package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.excep.ProtocolException;
import com.wireless.ordermenu.R;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.util.NumericUtil;
import com.wireless.util.imgFetcher.ImageFetcher;
/**
 *
 */
public class ComboFoodActivity extends Activity {
	
	private static final int REFRESH_COMBO_FOOD = 80001;
	private static final int REFRESH_CHILD_FOOD = 80002;
	
	private static final String COMBO_FOOD_KEY = "comboFoodKey";
	private static final String CHILD_FOOD_KEY = "childFoodKey";
	
	private ComboFoodHandler mComboFoodHandler;
	private ChildFoodHandler mChildFoodHandler;
	
	private ArrayList<Food> mComboFoods;
	private ImageFetcher mImageFetcher;
	
	private ImageView mFoodImageView;

	
	private static class ChildFoodHandler extends Handler{
		private WeakReference<ComboFoodActivity> mActivity;
		private TextView mChildFoodNameTextView;

		private ChildFoodHandler(ComboFoodActivity activity) {
			this.mActivity = new WeakReference<ComboFoodActivity>(activity);
			//initial
			mChildFoodNameTextView = (TextView) activity.findViewById(R.id.textView_childFoodName_comboFood);
		}

		@Override
		public void handleMessage(Message msg) {
			final ComboFoodActivity activity = mActivity.get();
			
			if(msg.what == REFRESH_CHILD_FOOD && msg.getData() != null)
			{
				final OrderFood food = msg.getData().getParcelable(CHILD_FOOD_KEY);
				//set child food title
				mChildFoodNameTextView.setText(food.getName());
				//set child food image
				activity.mImageFetcher.setImageSize(650, 400);
				activity.mFoodImageView.setScaleType(ScaleType.CENTER_CROP);
				if(food.image != null)
					activity.mImageFetcher.loadImage(food.image, activity.mFoodImageView);
				else activity.mFoodImageView.setImageResource(R.drawable.null_pic);
				
				activity.mFoodImageView.setTag(food);
			}
		}
	}
	
	private static class ComboFoodHandler extends Handler{
		private WeakReference<ComboFoodActivity> mActivity;
		private ListView mSpecificListView;
		private TextView mComboFoodNameTextView;
		private TextView mComboFoodPriceTextView;

		private ComboFoodHandler(ComboFoodActivity activity) {
			this.mActivity = new WeakReference<ComboFoodActivity>(activity);
			// initial
			mSpecificListView = (ListView) activity.findViewById(R.id.listView_comboFood);
			mComboFoodNameTextView = (TextView) activity.findViewById(R.id.textView_comboFoodName);
			mComboFoodPriceTextView = (TextView) activity.findViewById(R.id.textView_comboFood_price);
		}

		@Override
		public void handleMessage(Message msg) {
			final ComboFoodActivity activity = mActivity.get();
			if(msg.what == REFRESH_COMBO_FOOD)
			{
				//取得这个套餐
				OrderFood theFood = msg.getData().getParcelable(COMBO_FOOD_KEY);
				theFood.setCount(1f);
				mComboFoodNameTextView.setText(theFood.getName());
				mComboFoodPriceTextView.setText(NumericUtil.float2String2(theFood.calcPriceWithTaste()));
				  
				ArrayList<Food> childFoods = new ArrayList<Food>();
				ArrayList<Food> giftFoods = new ArrayList<Food>();
				//添加"主菜"标题头
				childFoods.add(new Food(Integer.MAX_VALUE, SpecificFoodAdapter.MAIN_FOOD_KEY));
				//将普通菜和赠送菜归类
				for(Food f : theFood.getChildFoods())
				{
					if(f.getName() != null)
					{
						if(!f.isGift())
							childFoods.add(f);
						else giftFoods.add(f);
					}
				}
				
				//添加赠送菜标题头
				if(!giftFoods.isEmpty())
				{
					childFoods.add(new Food(Integer.MAX_VALUE - 1, SpecificFoodAdapter.GIFT_FOOD_KEY));
					//将赠送菜添加
					childFoods.addAll(giftFoods);
				}
				mSpecificListView.setAdapter(activity.new SpecificFoodAdapter(childFoods));
				
				mSpecificListView.setOnItemClickListener(new OnItemClickListener(){
					@SuppressWarnings("deprecation")
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						Food food = (Food) view.getTag();
						if(food != null)
						{
							//将被点击的菜品传递给右边
							Bundle data = new Bundle();
							data.putParcelable(CHILD_FOOD_KEY, new OrderFoodParcel(new OrderFood(food)));
							
							Message msg = new Message();
							msg.what = REFRESH_CHILD_FOOD;
							msg.setData(data);
							
							activity.mChildFoodHandler.sendMessage(msg);
							//set on click style
							if(parent.getTag() != null)
								((View)parent.getTag()).setBackgroundDrawable(null);
							view.setBackgroundColor(activity.getResources().getColor(R.color.blue));
							parent.setTag(view);
						}
					}
				});
				//设置默认第一个选中
				mSpecificListView.postDelayed(new Runnable(){
					@Override
					public void run() {
						mSpecificListView.performItemClick(mSpecificListView.getChildAt(1), 1, 1);
					}
				}, 100);
			}
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.combo_food);
		
		mFoodImageView = (ImageView) findViewById(R.id.imageView_childImage_comboFood);

		mChildFoodHandler = new ChildFoodHandler(this);
		mComboFoodHandler = new ComboFoodHandler(this);
		
		mComboFoods = new ArrayList<Food>();
		//将套餐选出来
		for(Food f:WirelessOrder.foodMenu.foods)
		{
			if(f.isCombo())
			{
				mComboFoods.add(f);
			}
		}
		
		//设置套餐图层的内容 和大小
		mImageFetcher = new ImageFetcher(this,245,160);
		final LinearLayout comboFoodlayout = (LinearLayout) findViewById(R.id.linearLayout_comboFood);
		LayoutParams lp = new LayoutParams(245,160);
		
		for(Food f:mComboFoods)
		{
			//设置每个套餐的image view参数
			if(f.image != null)
			{
				ImageView image = new ImageView(this);
				image.setLayoutParams(lp);
				image.setScaleType(ScaleType.CENTER_CROP);
				mImageFetcher.loadImage(f.image, image);
				
				image.setTag(f);
				//添加到图层中
				comboFoodlayout.addView(image);
				
				image.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						//将选中的套餐传递给handler
						Food f = (Food) v.getTag();
						Message msg = new Message();
						msg.what = REFRESH_COMBO_FOOD;
						Bundle data = new Bundle();
						data.putParcelable(COMBO_FOOD_KEY, new OrderFoodParcel(new OrderFood(f)));
						msg.setData(data);
						mComboFoodHandler.sendMessage(msg);
						//设置被点击项的透明度和还原之前项透明度
						//FIXME 改成其它显示
						if(comboFoodlayout.getTag() != null)
							((View) comboFoodlayout.getTag()).setAlpha(1f);
						v.setAlpha(0.5f);
						comboFoodlayout.setTag(v);
					}
				});
			}
		}
		//默认第一个套餐选中
		if(!mComboFoods.isEmpty())
			comboFoodlayout.postDelayed(new Runnable(){
				@Override
				public void run() {
					comboFoodlayout.getChildAt(0).performClick();
				}
			}, 100);

		//set pick child food button listener
		((ImageButton) findViewById(R.id.imageButton_pickChildFood_comboFood)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OrderFood food = (OrderFood)mFoodImageView.getTag();
				try{
					//default count is 1
					food.setCount(1.0f);
					ShoppingCart.instance().addFood(food);
					Toast.makeText(ComboFoodActivity.this, food.getName()+" 已添加", Toast.LENGTH_SHORT).show();
				}catch(ProtocolException e){
					Toast.makeText(ComboFoodActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
		//set pick combo food button 
		((ImageButton) findViewById(R.id.imageButton_pickComboFood_comboFood)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//get combo food from layout
				Food food = ((Food)((View)comboFoodlayout.getTag()).getTag());
				
				OrderFood orderFood = new OrderFood(food);
				try{
					orderFood.setCount(1.0f);
					ShoppingCart.instance().addFood(orderFood);
					Toast.makeText(ComboFoodActivity.this, orderFood.getName()+" 已添加", Toast.LENGTH_SHORT).show();
				}catch(ProtocolException e){
					Toast.makeText(ComboFoodActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
		//set child image listener
		mFoodImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ComboFoodActivity.this, FoodDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel((OrderFood) mFoodImageView.getTag()));
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
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
    
	class SpecificFoodAdapter extends BaseAdapter{
		static final String MAIN_FOOD_KEY = "主菜";
		static final String GIFT_FOOD_KEY = "赠送";
		ArrayList<Food> mFoods;
		SpecificFoodAdapter(ArrayList<Food> childFoods)
		{
			mFoods = childFoods;
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
			View view = convertView;
			if(view == null)
			{
				final LayoutInflater lf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = lf.inflate(R.layout.combo_food_list_item, null);
			}
			//get current food 
			Food food = mFoods.get(position);
			// if the food is just title
			if(food.getName().equals(MAIN_FOOD_KEY) || food.getName().equals(GIFT_FOOD_KEY))
			{
				//FIXME 修改点击样式
				if(food.getName().equals(MAIN_FOOD_KEY))
					view.setBackgroundResource(R.drawable.combo_group_main);
				else view.setBackgroundResource(R.drawable.combo_group_gift);
				
				((TextView) view.findViewById(R.id.textView_name_combo_item)).setText("");
				view.setTag(null);
			}
			else {
				view.setBackgroundColor(getResources().getColor(R.color.gray));
				view.setTag(food);
				((TextView) view.findViewById(R.id.textView_name_combo_item)).setText(food.getName());
			}
			return view;
		}
		
	}
	
}

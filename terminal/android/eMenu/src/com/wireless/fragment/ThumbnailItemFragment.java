package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.wireless.common.ShoppingCart;
import com.wireless.excep.BusinessException;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.ui.FoodDetailActivity;

public class ThumbnailItemFragment extends ListFragment {
	private static final String DATA_SOURCE_FOODS = "dataSourceFoods";
	private static final String DATA_PARENT_ID = "data_parent_id";
	
	private ThumbnailFragment mParentFragment;

	private View mThePickedView;
	private boolean mIsLeft = true;

	public static ThumbnailItemFragment newInstance(List<OrderFood> srcFoods, int parentId){
		ThumbnailItemFragment fgm = new ThumbnailItemFragment();
		Bundle args = new Bundle();
		
		ArrayList<FoodParcel> foodParcels = new ArrayList<FoodParcel>();
		for(Food f: srcFoods){
			foodParcels.add(new FoodParcel(new OrderFood(f)));
		}
		args.putParcelableArrayList(DATA_SOURCE_FOODS, foodParcels);
		args.putInt(DATA_PARENT_ID, parentId);
		fgm.setArguments(args);
		return fgm;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View layout = inflater.inflate(R.layout.text_list_fgm_item, null);
        
		Bundle args = getArguments();
		int parentId = args.getInt(DATA_PARENT_ID);
		mParentFragment = (ThumbnailFragment) getFragmentManager().findFragmentById(parentId);
		
    	ArrayList<FoodParcel> foodParcels = args.getParcelableArrayList(DATA_SOURCE_FOODS);
    	int middleCount = foodParcels.size() / 2;
    	if(foodParcels.size() % 2 != 0)
    		middleCount++;
    	
    	ArrayList<ArrayList<OrderFood>> result = new ArrayList<ArrayList<OrderFood>>();
    	ArrayList<OrderFood> leftList = new ArrayList<OrderFood>();
    	ArrayList<OrderFood> rightList = new ArrayList<OrderFood>();

    	for (int i = 0; i < middleCount; i++) {
			FoodParcel foodParcel = foodParcels.get(i);
			leftList.add(foodParcel);
		}
    	for(int i= middleCount; i < foodParcels.size(); i++){
    		rightList.add(foodParcels.get(i));
    	}
    	result.add(leftList);
    	result.add(rightList);
    	
    	setListAdapter(new FoodAdapter(result));
		return layout;
	}

	@Override
	public void onStart() {
		super.onStart();
		
		if(mThePickedView != null)
		{
			refreshDisplay((OrderFood) mThePickedView.getTag(), mThePickedView, mIsLeft);
		}
	}

	class FoodAdapter extends BaseAdapter{
		private ArrayList<ArrayList<OrderFood>> mFoods = new ArrayList<ArrayList<OrderFood>>();
		
		FoodAdapter(ArrayList<ArrayList<OrderFood>> result) {
			mFoods = result;
		}

		@Override
		public int getCount() {
			return mFoods.get(0).size();
		}

		@Override
		public Object getItem(int position) {
			return mFoods.get(0).get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View layout;
			
			if (convertView == null) { 
				layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.thumbnail_fgm_item, null);
			} else {
				layout = convertView;
			}
			OrderFood food1  = mFoods.get(0).get(position);
//			layout.setTag(food1);
			
			//显示菜品图片
			ImageView foodImage = (ImageView) layout.findViewById(R.id.imageView_thumbnailFgm_item_foodImg1);
			foodImage.setScaleType(ScaleType.CENTER_CROP);
			mParentFragment.getImageFetcher().loadImage(food1.image, foodImage);
			
			//点菜按钮
			Button addBtn = (Button) layout.findViewById(R.id.button_thumbnailFgm_item_add1);
			addBtn.setTag(food1);
			addBtn.setOnClickListener(new AddDishOnClickListener(layout, true));
			
			//菜品详情
			Button detailBtn = (Button) layout.findViewById(R.id.button_thumbnailFgm_item_detail1);
			detailBtn.setTag(food1);
			detailBtn.setOnClickListener(new DetailOnClickListener(layout, true));
			
			refreshDisplay(food1, layout, true);
			
			OrderFood food2 = null;
			try{
				food2 = mFoods.get(1).get(position);
			} catch(IndexOutOfBoundsException e){
				
			}
			
			if(food2 != null){
			//显示菜品图片
				ImageView foodImage2 = (ImageView) layout.findViewById(R.id.imageView_thumbnailFgm_item_foodImg2);
				foodImage2.setScaleType(ScaleType.CENTER_CROP);
				mParentFragment.getImageFetcher().loadImage(food2.image, foodImage2);
				
				//点菜按钮
				Button addBtn2 = (Button) layout.findViewById(R.id.button_thumbnailFgm_item_add2);
				addBtn2.setTag(food2);
				addBtn2.setOnClickListener(new AddDishOnClickListener(layout, false));
				
				//菜品详情
				Button detailBtn2 = (Button) layout.findViewById(R.id.button_thumbnailFgm_item_detail2);
				detailBtn2.setTag(food2);
				detailBtn2.setOnClickListener(new DetailOnClickListener(layout, false));
				
				refreshDisplay(food2, layout, false);
			}
			return layout;
		}

		public List<ArrayList<OrderFood>> getList() {
			return mFoods;
		}
	}
	/*
	 * 更改菜品的显示
	 */
	private void refreshDisplay(OrderFood food1, View layout, boolean isLeft){
		OrderFood foodToShow = ShoppingCart.instance().getFood(food1.getAliasId());
		if(foodToShow == null){
			foodToShow = food1;
		}
		
		if(isLeft){
			if(foodToShow.getCount() != 0f){
				((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_pickedCount1)).setText(
						Util.float2String2(foodToShow.getCount()));
			} else {
				((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_pickedCount1)).setText("");
			}
			//price
			((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_price1)).setText(
					Util.float2String2(foodToShow.getPrice()));
			
			//显示菜品名称
			((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_foodName1)).setText(foodToShow.name);
		} else {
			if(foodToShow.getCount() != 0f){
				((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_pickedCount2)).setText(
						Util.float2String2(foodToShow.getCount()));
			} else {
				((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_pickedCount2)).setText("");
			}
			//price
			((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_price2)).setText(
					Util.float2String2(foodToShow.getPrice()));
			
			//显示菜品名称
			((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_foodName2)).setText(foodToShow.name);
		}
	}
	
	class AddDishOnClickListener implements OnClickListener{
		View mLayout;
		private boolean isLeft;
		
		public AddDishOnClickListener(View layout, boolean isLeft) {
			super();
			this.mLayout = layout;
			this.isLeft = isLeft;
		}

		public void setLayout(View layout) {
			this.mLayout = layout;
		}

		public boolean isLeft() {
			return isLeft;
		}

		public void setLeft(boolean isLeft) {
			this.isLeft = isLeft;
		}

		@Override
		public void onClick(View v) {
			OrderFood food = (OrderFood) v.getTag();
			if(food != null)
			{
				food.setCount(1f);
				try {
					ShoppingCart.instance().addFood(food);
					mLayout.setTag(food);
					refreshDisplay(food, mLayout, isLeft);
				} catch (BusinessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	class DetailOnClickListener implements OnClickListener{
		View mLayout;
		private boolean isLeft = true;
		
		public DetailOnClickListener(View mLayout, boolean isLeft) {
			super();
			this.mLayout = mLayout;
			this.isLeft = isLeft;
		}

		public void setLayout(View mLayout) {
			this.mLayout = mLayout;
		}

		@Override
		public void onClick(View v) {
			OrderFood food = (OrderFood) v.getTag();
			if(food != null){
				Intent intent = new Intent(getActivity(), FoodDetailActivity.class);
				Bundle bundle = new Bundle();
//				food.setCount(1f);
				
				bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(food));
				intent.putExtras(bundle);
				mLayout.setTag(food);
				mThePickedView = mLayout;
				mIsLeft  = isLeft;
				startActivity(intent);
			}
		}
	}
	public void setFoodHighLight(Food food) {
		getListView().requestFocusFromTouch();
		
		FoodAdapter adapter = (FoodAdapter) this.getListAdapter();
		List<ArrayList<OrderFood>> list = adapter.getList();
		for(ArrayList<OrderFood> subList: list){
			for (int i = 0; i < subList.size(); i++) {
				OrderFood f = subList.get(i);
				if(f.getAliasId() == food.getAliasId()){
					getListView().setSelection(i);
					return;
				}
			}
		}
	}
}

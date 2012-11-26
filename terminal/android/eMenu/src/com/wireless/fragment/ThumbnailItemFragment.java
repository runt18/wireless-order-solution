package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
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
import com.wireless.util.imgFetcher.ImageCache.ImageCacheParams;
import com.wireless.util.imgFetcher.ImageFetcher;

public class ThumbnailItemFragment extends Fragment {
	private static final String DATA_SOURCE_FOODS = "dataSourceFoods";
//	public ImageFetcher mImageFetcher;
	private static final String DATA_PARENT_ID = "data_parent_id";
	private ThumbnailFragment mParentFragment;

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
		final View layout = inflater.inflate(R.layout.thumbnail_item_fgm, null);
        
		Bundle args = getArguments();
		int parentId = args.getInt(DATA_PARENT_ID);
		mParentFragment = (ThumbnailFragment) getFragmentManager().findFragmentById(parentId);
		
    	ArrayList<FoodParcel> foodParcels = args.getParcelableArrayList(DATA_SOURCE_FOODS);
    	ArrayList<OrderFood> srcFoods = new ArrayList<OrderFood>();
    	for(FoodParcel foodParcel : foodParcels){
    		srcFoods.add(foodParcel);
    	}
    	
    	GridView gridView = (GridView) layout;
    	gridView.setAdapter(new FoodAdapter(srcFoods));
    	gridView.setNumColumns(2);
    	
		layout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				
				ImageView img = (ImageView) layout.findViewById(R.id.imageView_thumbnailFgm_item_foodImg);

				if(img.getHeight() > 0 && mParentFragment.getImageFetcher().getHeight() == 0)
				{
					mParentFragment.getImageFetcher().setImageSize(img.getWidth(), img.getHeight());
					layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				} else if(mParentFragment.getImageFetcher().getHeight() > 0)
				{
					layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		});
    	//TODO
//    	gridView.set
		return layout;
	}

	class FoodAdapter extends BaseAdapter{
		private ArrayList<OrderFood> mFoods = new ArrayList<OrderFood>();
		
		FoodAdapter(ArrayList<OrderFood> srcFoods) {
			mFoods = srcFoods;
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
			final View view;
//			Log.i("view","run");
			if (convertView == null) {
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.thumbnail_fgm_item, null);
			} else {
				view = convertView;
			}
			OrderFood food  = mFoods.get(position);
			view.setTag(food);
			
			refreshDisplay(food , view);
			
			//显示菜品图片
			ImageView foodImage = (ImageView) view.findViewById(R.id.imageView_thumbnailFgm_item_foodImg);
			foodImage.setScaleType(ScaleType.CENTER_CROP);
			mParentFragment.getImageFetcher().loadImage(food.image, foodImage);
			
			//点菜按钮
			Button addBtn = (Button) view.findViewById(R.id.button_thumbnailFgm_item_add);
			addBtn.setTag(food);
			addBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					OrderFood food = (OrderFood) v.getTag();
					if(food != null)
					{
						food.setCount(1f);
						try {
							ShoppingCart.instance().addFood(food);
							refreshDisplay(food, view);
						} catch (BusinessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
			return view;
		}
		
		private void refreshDisplay(OrderFood srcFood, View layout){
			OrderFood foodToShow = ShoppingCart.instance().getFood(srcFood.getAliasId());
			if(foodToShow == null)
				foodToShow = srcFood;
			
			if(foodToShow.getCount() != 0f){
				layout.findViewById(R.id.textView_thumbnailFgm_item_pickedHint).setVisibility(View.VISIBLE);
				((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_pickedCount)).setText(
						Util.float2String2(foodToShow.getCount()));
			} else {
				layout.findViewById(R.id.textView_thumbnailFgm_item_pickedHint).setVisibility(View.INVISIBLE);
				((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_pickedCount)).setText("");
			}
			//price
			((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_price)).setText(
					Util.float2String2(foodToShow.getPrice()));
			
			//显示菜品名称
			((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_foodName)).setText(foodToShow.name);
		}
	}
}

package com.wireless.fragment;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.wireless.common.ShoppingCart;
import com.wireless.excep.BusinessException;
import com.wireless.ordermenu.R;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.util.imgFetcher.ImageFetcher;
import com.wireless.util.imgFetcher.ImageCache.ImageCacheParams;

public class ThumbnailItemFragment extends Fragment {
	private static final String IMAGE_CACHE_DIR = "thumbs";

	public ImageFetcher mImageFetcher;

	public static ThumbnailItemFragment newInstance(){
		return null;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View layout = inflater.inflate(R.layout.thumbnail_item_fgm, null);
		
        ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(getActivity(), 0.25f);

		mImageFetcher = new ImageFetcher(getActivity(), 0, 0);

        mImageFetcher.addImageCache(getActivity().getFragmentManager(), cacheParams);
        
		layout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				ImageView img = (ImageView) layout.findViewById(R.id.imageView_thumbnailFgm_item_foodImg);
				if(img.getHeight() > 0)
				{
					mImageFetcher.setImageSize(img.getWidth(), img.getHeight());
					layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		});
		
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
			mImageFetcher.loadImage(food.image, foodImage);
			
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

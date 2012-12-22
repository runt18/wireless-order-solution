package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.OrderFood;
import com.wireless.util.imgFetcher.ImageCache.ImageCacheParams;
import com.wireless.util.imgFetcher.ImageFetcher;

public class TextListFragment extends Fragment{

	private static final String KEY_SOURCE_FOODS = "keySourceFoods";
	private ArrayList<List<Food>> mPackedValidFoodsList;
	private int mCountPerList = 10;
	private ImageFetcher mImageFetcher;

	private ViewPager mViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mImageFetcher = new ImageFetcher(getActivity(), 50);
        ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), 0.1f);
        mImageFetcher.addImageCache(getActivity().getFragmentManager(), cacheParams, "TextListFragment");
        
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.food_list_fgm, null);
		
		Bundle args = getArguments();
		
    	ArrayList<FoodParcel> foodParcels = args.getParcelableArrayList(KEY_SOURCE_FOODS);
    	final ArrayList<OrderFood> srcFoods = new ArrayList<OrderFood>();
    	for(FoodParcel foodParcel : foodParcels){
    		srcFoods.add(foodParcel);
    	}
    	
    	mViewPager = (ViewPager) layout.findViewById(R.id.viewPager_TextListFgm);
        mViewPager.setOffscreenPageLimit(2);
        
    	layout.post(new Runnable() {
			@Override
			public void run() {
				notifyDataSetChanged(srcFoods);		
			}
		});
    	
    	layout.findViewById(R.id.button_left).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				mFlipper.showPrevious();
			}
		});
    	
    	layout.findViewById(R.id.button_right).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				mFlipper.showNext();
			}
		});
		return layout;
	}

	public void notifyDataSetChanged(ArrayList<OrderFood> srcFoods){
		//将筛选出的菜品打包成List<List<T>>格式
		mPackedValidFoodsList = new ArrayList<List<Food>>();
		ArrayList<List<Food>> mSrcFoodsList = new ArrayList<List<Food>>();
		Kitchen lastKitchen = srcFoods.get(0).kitchen;
		List<Food> theKitchenList = new ArrayList<Food>();
		//将菜品按厨房分组
		for(int i=0;i<srcFoods.size();i++)
		{
			if(srcFoods.get(i).kitchen.equals(lastKitchen))
			{
				theKitchenList.add(srcFoods.get(i));
			}
			else{
				mSrcFoodsList.add(theKitchenList);
				theKitchenList = new ArrayList<Food>();
				lastKitchen = srcFoods.get(i).kitchen;
				theKitchenList.add(srcFoods.get(i));
			}
			if(i == srcFoods.size() - 1)
				mSrcFoodsList.add(theKitchenList);
		}
		
		int countPerPage = mCountPerList * 2;
		//遍历每个厨房菜品
		for(List<Food> kitchenList : mSrcFoodsList){
			int kitchenSize = kitchenList.size();
			//计算出页数
			int pageSize = (kitchenSize / countPerPage) + (kitchenSize % countPerPage == 0? 0:1);
			//把每一页的菜品装入
			for(int pageNum = 0; pageNum < pageSize; pageNum ++){
				ArrayList<Food> foodPerPage = new ArrayList<Food>();
				for(int i=0;i < countPerPage; i++){
					int realIndex = pageNum * countPerPage + i;
					if(realIndex < kitchenSize){
						foodPerPage.add(kitchenList.get(realIndex));
					} else break; 
				}
				mPackedValidFoodsList.add(foodPerPage);
			}
		}
		
		TextPagerAdapter adapter = new TextPagerAdapter(getFragmentManager(), mPackedValidFoodsList.size());
		mViewPager.setAdapter(adapter);
//		mViewPager.setAdapter(new TextPagerAdapter(getFragmentManager(), mPackedValidFoodsList.size());
//        final FragmentStatePagerAdapter mPagerAdapter = new FragmentStatePagerAdapter(getFragmentManager(), int size) {
//			private int mSize;
//			
//			
//			@Override
//			public int getCount() {
//				return mPackedValidFoodsList.size();
//			}
//			
//			@Override
//			public Fragment getItem(int position) {
//			}
//		};
		
//        mViewPager.post(new Runnable(){
//
//			@Override
//			public void run() {
//				mViewPager.setAdapter(mPagerAdapter);
//			}
//        });  
//		mFlipper.setAdapter(new BaseAdapter() {
//			
//			@Override
//			public View getView(int position, View convertView, ViewGroup parent) {
//				View layout = convertView;
//				if(layout == null){
//					layout = getActivity().getLayoutInflater()
//				}
//				//当前页的list
//				List<Food> allFoodlist = mPackedValidFoodsList.get(position);
//				
//				final List<Food> leftList;
//				List<Food> rightList = null;
//				//判断是否分为左右两个列表
//				if(allFoodlist.size()  > mCountPerList  ){
//					leftList = allFoodlist.subList(0, mCountPerList);
//					rightList = allFoodlist.subList(mCountPerList, allFoodlist.size());
//				} else {
//					leftList = allFoodlist;
//				}
//				//设置左右adapter
//				ListView leftView = (ListView) layout.findViewById(R.id.listView_foodListFgm_item_left);
//				leftView.setAdapter(new SubListAdapter(getActivity(), leftList, mImageFetcher));
//				
//				if(rightList != null){
//					ListView rightView = (ListView) layout.findViewById(R.id.listView_foodListFgm_item_right);
//					rightView.setAdapter(new SubListAdapter(getActivity(), rightList, mImageFetcher));
//				}
//				
//				return layout;
//			}
//			
//			@Override
//			public long getItemId(int position) {
//				return position;
//			}
//			
//			@Override
//			public Object getItem(int position) {
//				return mPackedValidFoodsList.get(position);
//			}
//			
//			@Override
//			public int getCount() {
//				return mPackedValidFoodsList.size();
//			}
//		});
	}

	public static TextListFragment newInstance(List<Food> list) {
		TextListFragment fgm = new TextListFragment();
		
		Bundle args = new Bundle();
		ArrayList<FoodParcel> foodParcels = new ArrayList<FoodParcel>();
		for(Food f: list){
			foodParcels.add(new FoodParcel(new OrderFood(f)));
		}
		args.putParcelableArrayList(KEY_SOURCE_FOODS, foodParcels);
		fgm.setArguments(args);
		
		return fgm;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mImageFetcher.clearCache();
	}
	
	public ImageFetcher getImageFetcher(){
		return mImageFetcher;
	}
	
	private class TextPagerAdapter extends FragmentStatePagerAdapter {

		private int mSize;

		public TextPagerAdapter(FragmentManager fm, int size) {
			super(fm);
			mSize = size;
		}

		@Override
		public Fragment getItem(int position) {
//			return null;
			return TextListItemFragment.newInstance(mPackedValidFoodsList.get(position), TextListFragment.this.getId(), mCountPerList);
		}

		@Override
		public int getCount() {
			return mSize;
		}
	}
}


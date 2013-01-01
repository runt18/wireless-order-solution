package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.OrderFood;
import com.wireless.util.SearchFoodHandler;
import com.wireless.util.SearchFoodHandler.OnSearchItemClickListener;
import com.wireless.util.imgFetcher.ImageFetcher;

public class TextListFragment extends Fragment implements OnSearchItemClickListener{

	private static final String KEY_SOURCE_FOODS = "keySourceFoods";
	private ArrayList<FoodHolder> mGroupedFoodHolders;
	private int mCountPerList = 20;
	private ImageFetcher mImageFetcher;

	private ViewPager mViewPager;		
	private SearchFoodHandler mSearchHandler;
	private TextView mKitchenText;
	private TextView mCurrentPageText;
	private TextView mTotalPageText;
	private EditText mSearchEditText;
	private OnTextListChangeListener mOnTextListChangeListener;
	
	protected int mCurrentPosition;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mImageFetcher = new ImageFetcher(getActivity(), 50);
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
        mViewPager.setOffscreenPageLimit(0);
        
    	layout.post(new Runnable() {
			@Override
			public void run() {
				notifyDataSetChanged(srcFoods);		
			}
		});
    	
    	mSearchEditText = (EditText) layout.findViewById(R.id.editText_TextListFgm);
		//搜索框
		mSearchHandler = new SearchFoodHandler(this, 
				mSearchEditText, 
				(Button) layout.findViewById(R.id.button_TextListFgm_clear));
		mSearchHandler.setOnSearchItemClickListener(this);
		
		mKitchenText = (TextView) layout.findViewById(R.id.textView_TextListFgm_kitchen);
		mCurrentPageText = (TextView) layout.findViewById(R.id.textView_TextListFgm_curPage);
		mTotalPageText = (TextView) layout.findViewById(R.id.textView_TextListFgm_totalPage);
		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		try{
			mOnTextListChangeListener = (OnTextListChangeListener) getActivity();
		} catch(ClassCastException e){
			Log.e("classCastException", "activity must implement the OnTextListChangeListener");
		}
	}

	public void notifyDataSetChanged(ArrayList<OrderFood> srcFoods){
		//将筛选出的菜品打包成List<List<T>>格式
		mGroupedFoodHolders = new ArrayList<FoodHolder>();
		ArrayList<List<OrderFood>> mSrcFoodsList = new ArrayList<List<OrderFood>>();
		Kitchen lastKitchen = srcFoods.get(0).kitchen;
		List<OrderFood> theKitchenList = new ArrayList<OrderFood>();
		//将菜品按厨房分组
		for(int i=0;i<srcFoods.size();i++)
		{
			if(srcFoods.get(i).kitchen.equals(lastKitchen))
			{
				theKitchenList.add(srcFoods.get(i));
			}
			else{
				mSrcFoodsList.add(theKitchenList);
				theKitchenList = new ArrayList<OrderFood>();
				lastKitchen = srcFoods.get(i).kitchen;
				theKitchenList.add(srcFoods.get(i));
			}
			if(i == srcFoods.size() - 1)
				mSrcFoodsList.add(theKitchenList);
		}
		
		int countPerPage = mCountPerList;
		//遍历每个厨房菜品
		for(List<OrderFood> kitchenList : mSrcFoodsList){
			int kitchenSize = kitchenList.size();
			//计算出页数
			int pageSize = (kitchenSize / countPerPage) + (kitchenSize % countPerPage == 0? 0:1);
			//把每一页的菜品装入
			for(int pageNum = 0; pageNum < pageSize; pageNum ++){
				ArrayList<OrderFood> foodPerPage = new ArrayList<OrderFood>();
				for(int i=0;i < countPerPage; i++){
					int realIndex = pageNum * countPerPage + i;
					if(realIndex < kitchenSize){
						foodPerPage.add(kitchenList.get(realIndex));
					} else break; 
				}
				FoodHolder holder = new FoodHolder(foodPerPage, pageNum, pageSize, foodPerPage.get(0).kitchen, foodPerPage.get(0));
				mGroupedFoodHolders.add(holder);
			}
		}
		
		mViewPager.setAdapter(new TextPagerAdapter(getFragmentManager(), mGroupedFoodHolders.size()));
		
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				if(mCurrentPosition != position){
					refreshDisplay(position);
					mCurrentPosition = position;
					
					if(mOnTextListChangeListener != null)
						mOnTextListChangeListener.onTextListChange(mGroupedFoodHolders.get(position).getThisKitchen(),mGroupedFoodHolders.get(position).getCaptainFood());
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
//				//隐藏键盘
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
				
				if(!mSearchEditText.getText().toString().equals(""))
					mSearchEditText.setText("");
				
				if(state == ViewPager.SCROLL_STATE_DRAGGING){
					mImageFetcher.setPauseWork(true);
				} else if(state == ViewPager.SCROLL_STATE_IDLE){
					mImageFetcher.setPauseWork(false);
				}
			}
		});
		
		refreshDisplay(0);
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
	
	@Override
	public void onSearchItemClick(Food food) {
		setPosByKitchen(food.kitchen);
	}
	
	public void setPosByKitchen(Kitchen kitchen){
		if(mGroupedFoodHolders == null){
			
		} else new AsyncTask<Kitchen, Void, Integer>() {

			@Override
			protected Integer doInBackground(Kitchen... params) {
				for (int i = 0; i < mGroupedFoodHolders.size(); i++) {
					FoodHolder holder = mGroupedFoodHolders.get(i);
					Kitchen theKitchen = holder.getThisKitchen();
					if(theKitchen.aliasID == params[0].aliasID){
						return i;
					}
				}
				return -1;
			}

			@Override
			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);
				if(result != -1 && mViewPager != null){
					setPosition(result);
				}
			}
		}.execute(kitchen);
	}
	
	public void setPosition(int position){
		if(mCurrentPosition != position){
			mViewPager.setCurrentItem(position, false);
		}
	}
	private void refreshDisplay(int position){
		FoodHolder holder = mGroupedFoodHolders.get(position);
		
		Kitchen kitchen = null;
		for(Kitchen k: WirelessOrder.foodMenu.kitchens){
			if(holder.getFoods().get(0).kitchen.aliasID == k.aliasID)
				kitchen = k;
		}
		if(kitchen != null)
			mKitchenText.setText(kitchen.name);
		else mKitchenText.setText(holder.getFoods().get(0).kitchen.aliasID);
		
		mCurrentPageText.setText(""+(holder.getCurrentPage()+1));
		mTotalPageText.setText(""+holder.getTotalPage());
	}
	
	private class TextPagerAdapter extends FragmentPagerAdapter {

		private int mSize;

		public TextPagerAdapter(FragmentManager fm, int size) {
			super(fm);
			mSize = size;
		}

		@Override
		public Fragment getItem(int position) {
			return TextListItemFragment.newInstance(mGroupedFoodHolders.get(position).getFoods(), TextListFragment.this.getId(), mCountPerList);
		}

		@Override
		public int getCount() {
			return mSize;
		}
	}

	public interface OnTextListChangeListener{
		void onTextListChange(Kitchen kitchen, OrderFood captainFood);
	}
	
	public void setOnTextListChangeListener(OnTextListChangeListener l){
		mOnTextListChangeListener = l;
	}
}

class FoodHolder {
	private ArrayList<OrderFood> mFoods;
	private int mCurrentPage;
	private int mTotalPage;
	private Kitchen mCurrentKitchen;
	private OrderFood mCaptainFood;
	
	public FoodHolder(ArrayList<OrderFood> mFoods, int mCurrentPage, int mTotalPage, Kitchen kitchen, OrderFood captainFood) {
		this.mFoods = mFoods;
		this.mCurrentPage = mCurrentPage;
		this.mTotalPage = mTotalPage;
		mCurrentKitchen = kitchen;
		mCaptainFood = captainFood;
	}

	public ArrayList<OrderFood> getFoods() {
		return mFoods;
	}

	public int getCurrentPage() {
		return mCurrentPage;
	}

	public int getTotalPage() {
		return mTotalPage;
	}

	public Kitchen getThisKitchen() {
		return mCurrentKitchen;
	}
	public OrderFood getCaptainFood(){
		return mCaptainFood;
	}
}
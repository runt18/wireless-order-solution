package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;

import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.lib.task.QuerySellOutTask;
import com.wireless.ordermenu.R;
import com.wireless.parcel.DepartmentTreeParcel;
import com.wireless.pojo.menuMgr.DepartmentTree;
import com.wireless.pojo.menuMgr.DepartmentTree.KitchenNode;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.util.SearchFoodHandler;
import com.wireless.util.SearchFoodHandler.OnSearchItemClickListener;
import com.wireless.util.imgFetcher.ImageCache.ImageCacheParams;
import com.wireless.util.imgFetcher.ImageFetcher;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

/**
 * this fragment is the main fragment of thumbnail display method, 
 * <br/> it contains a {@link ViewPager} so that can support landscape scrolling and display thumbnail bitmaps.
 * <br/> it also use {@link SearchFoodHandler} to support search operation
 * @author ggdsn1
 *
 */
public class ThumbnailFragment extends Fragment implements OnSearchItemClickListener {
	
	/**
	 * 保存文字模式中每页分类菜品的信息，
	 * 包括本页的菜品信息，当前页数、captainFood 
	 */
	private static class ThumbnailPager {
		
		public final static int MAX_AMOUNT = 6;
		
		private final List<Food> mFoods = new ArrayList<Food>();
		
		public ThumbnailPager(List<Food> foodList) {
			if(foodList.size() > MAX_AMOUNT){
				throw new IllegalArgumentException("The amount to food list exceeds " + MAX_AMOUNT);
			}
			if(foodList.size() == 0){
				throw new IllegalArgumentException("The amount to food list can NOT be zero.");
			}
			for(Food f : foodList){
				mFoods.add(f);
			}
		}
		
		public List<Food> getFoods() {
			return mFoods;
		}

		public Food getCaptainFood(){
			return mFoods.get(0);
		}
	}
	
//	private static final String KEY_SOURCE_FOODS = "keySourceFoods";
//	private static final int ITEM_AMOUNT_PER_PAGE = 6;
	
	private List<ThumbnailPager> mThumbPagers = new ArrayList<ThumbnailPager>();
	
	private ImageFetcher mImageFetcher;
	//the index which record the position of current food
	private int mCurrentPos;
	
	private ViewPager mViewPager;
	
	private OnThumbnailChangedListener mThumbnailChangedListener;
	
	//更新估清菜品Task
	private QuerySellOutTask sellOutTask;
	
//	List<Entry<List<OrderFood>, OrderFood>> mGroupedFoods = new ArrayList<Entry<List<OrderFood>, OrderFood>>();
	protected SearchFoodHandler mSearchHandler;
	
	/**
	 * when the ViewPager's page is changed, use it to tell the observer
	 * @author ggdsn1
	 *
	 */
	public static interface OnThumbnailChangedListener{
		public void onThumbnailChanged(List<Food> foodsToCurrentGroup, Food captainToCurrentGroup, int pos);
	}
	
	public void setThumbnailChangedListener(OnThumbnailChangedListener thumbnailChangedListener){
		mThumbnailChangedListener = thumbnailChangedListener;
	}
	
	/**
	 * Factory method to generate a new instance of the fragment.
	 * @param srcFoods, the foods to display
	 */
	public static ThumbnailFragment newInstance(DepartmentTree deptTree){
		ThumbnailFragment fgm = new ThumbnailFragment();
		
		Bundle args = new Bundle();
		args.putParcelable(DepartmentTreeParcel.KEY_VALUE, new DepartmentTreeParcel(deptTree));
		fgm.setArguments(args);
		
		return fgm;
	}

	/**
	 * <p> prepare the {@link ImageFetcher}</p>
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mImageFetcher = new ImageFetcher(getActivity(), 320, 300);
        ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), 0.1f);
        mImageFetcher.addImageCache(getActivity().getFragmentManager(), cacheParams, "ImgCache#ThumbnailFragment");
        
	}

	/**
	 * prepare the view of this fragment and initial {@link ViewPager} and {@link SearchFoodHandler}
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.thumbnail_fragment, container, false);
		
		Bundle bundle = getArguments();
		
		if(bundle != null){
			DepartmentTreeParcel deptTreeParcel = bundle.getParcelable(DepartmentTreeParcel.KEY_VALUE);
	    	notifyDataSetChanged(deptTreeParcel.asDeptTree());
		}
    	
    	mViewPager = (ViewPager) view.findViewById(R.id.viewPager_thumbnailFgm);
        mViewPager.setOffscreenPageLimit(0);
        
		final AutoCompleteTextView mSearchEditText = (AutoCompleteTextView) view.findViewById(R.id.editText_thumbnailFgm);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				
				//更新估清菜品的状态
				if(sellOutTask == null || sellOutTask.getStatus() == AsyncTask.Status.FINISHED){
					sellOutTask = new QuerySellOutTask(WirelessOrder.loginStaff, WirelessOrder.foods) {
						
						@Override
						public void onSuccess(List<Food> sellOutFoods) {
							for(ThumbnailPager pager : mThumbPagers){
								for(Food f : pager.getFoods()){
									f.setSellOut(false);
									f.setLimit(false);
									f.setLimitAmount(0);
									f.setLimitRemaing(0);
								}
							}
							
							for(Food sellOut : sellOutFoods){
								for(ThumbnailPager pager : mThumbPagers){
									for(Food f : pager.getFoods()){
										if(f.getFoodId() == sellOut.getFoodId()){
											f.setStatus(sellOut.getStatus());
											f.setStatus(sellOut.getStatus());
											if(f.isLimit()){
												f.setLimitAmount(sellOut.getLimitAmount());
												f.setLimitRemaing(sellOut.getLimitRemaing());
											}
											break;
										}
									}
								}
							}
						}
						
						@Override
						public void onFail(BusinessException arg0) {
							
						}
					};
					sellOutTask.execute();
				}
				
				mCurrentPos = position;
				if(mThumbnailChangedListener != null){
					mThumbnailChangedListener.onThumbnailChanged(mThumbPagers.get(position).getFoods(), mThumbPagers.get(position).getCaptainFood(), position);
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { 			
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				//隐藏键盘
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
				
				if(!mSearchEditText.getText().toString().equals("")){
					mSearchEditText.setText("");
				}
				
				if(state == ViewPager.SCROLL_STATE_DRAGGING){
					mImageFetcher.setPauseWork(true);
				} else if(state == ViewPager.SCROLL_STATE_IDLE){
					mImageFetcher.setPauseWork(false);
				}
			}
		});
        
		Button clearSearchBtn = (Button) view.findViewById(R.id.button_thumbnailFgm_clear);
		//搜索框
		mSearchHandler = new SearchFoodHandler(this, mSearchEditText, clearSearchBtn);
		mSearchHandler.setOnSearchItemClickListener(this);
		
		//设置ViewPager的Adapter
        mViewPager.post(new Runnable(){

			@Override
			public void run() {
				mViewPager.setAdapter(new FragmentStatePagerAdapter(getFragmentManager()) {
					
					@Override
					public int getCount() {
						return mThumbPagers.size();
					}
					
					@Override
					public Fragment getItem(int position) {
						return ThumbnailItemFragment.newInstance(mThumbPagers.get(position).getFoods(), ThumbnailFragment.this.getTag());
					}
					
					@Override
				    public int getItemPosition(Object object) {
				        return POSITION_NONE;
				    }
				});
			}
        }); 

		return view;
	}
	
	/**
	 * Fire the thumbnail adapter to refresh data
	 */
	public void refersh(){
		if(mViewPager != null){
			mViewPager.getAdapter().notifyDataSetChanged();
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		//activity must implements the OnThumbnailChangedListener
		try{
			mThumbnailChangedListener = (OnThumbnailChangedListener)getActivity();
		}catch(ClassCastException e){
			
		}
	}
	
    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        (getView().findViewById(R.id.editText_thumbnailFgm)).clearFocus();
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.clearCache();
    }
    
	
	/**
	 * Input the source data and this method will classify the foods and pack it into {@link TextFoodPager}
	 * @see TextFoodPager
	 * @param deptTree the department tree
	 */
	private void notifyDataSetChanged(DepartmentTree deptTree){
		for(KitchenNode kitchenNode : deptTree.asKitchenNodes()){
			List<Food> foodList = kitchenNode.getValue();
			//计算出页数
			int pageSize = (foodList.size() / ThumbnailPager.MAX_AMOUNT) + (foodList.size() % ThumbnailPager.MAX_AMOUNT == 0 ? 0 : 1);
			//把每一页的菜品装入Pager
			for(int i = 0; i < pageSize; i++){
				int start = i * ThumbnailPager.MAX_AMOUNT;
				int end = start + ThumbnailPager.MAX_AMOUNT;
				if(end > foodList.size()){
					end = foodList.size();
				}
				mThumbPagers.add(new ThumbnailPager(foodList.subList(start, end)));
			}
		}
	}
	
	public ImageFetcher getImageFetcher(){
		return mImageFetcher;
	}
	
	/**
	 * Set the show the page according to specific position.
	 * @param pos the position to set
	 */
	private void setPosition(int pos){
		if(mCurrentPos != pos && mViewPager != null){
			mViewPager.setCurrentItem(pos, false);
			mCurrentPos = pos;
		}
	}
	
	/**
	 * Set the page to show according to a specific kitchen.
	 * @param kitchen the kitchen to search
	 */
	public void setPosByKitchen(Kitchen kitchen){
		int pageNo = 0;
		for(ThumbnailPager pager : mThumbPagers){
			if(pager.getCaptainFood().getKitchen().equals(kitchen)){
				setPosition(pageNo);
				break;
			}
			pageNo++;
		}
		
	}
	
	/**
	 * Set the page to show according to a specific food.
	 * @param food the food to search
	 */
	public void setPosByFood(Food food){
		int pageNo = 0;
		for(ThumbnailPager pager : mThumbPagers){
			for(Food f : pager.getFoods()){
				if(f.getAliasId() == food.getAliasId()){
					setPosition(pageNo);
					return;
				}
			}
			pageNo++;
		}
	}
	
	@Override
	public void onSearchItemClick(final Food food) {
		if(food.hasImage()){
			this.setPosByFood(food);
			mViewPager.postDelayed(new Runnable() {
				@Override
				public void run() {
					//高亮选中的food
					ThumbnailItemFragment curFgm = (ThumbnailItemFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem());
					curFgm.setHighLightedByFood(food);
				}
			}, 400);
		}
		else {
			Toast toast = Toast.makeText(getActivity(), "此菜暂无图片可展示", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP | Gravity.END, 230, 100);
			toast.show();
		}
	}
}



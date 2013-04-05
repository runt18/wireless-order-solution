package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
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
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.PKitchen;
import com.wireless.util.SearchFoodHandler;
import com.wireless.util.SearchFoodHandler.OnSearchItemClickListener;
import com.wireless.util.imgFetcher.ImageFetcher;

/**
 * this fragment contains a {@link ViewPager} to manage all sub items({@link TextListItemFragment})<br/>
 * it use {@link ArrayList} to carry each pages ,each {@link FoodHolder} is a token of a page,
 * contains some details about the page,like foods, kitchen, captain food ,etc
 * @author ggdsn1
 * @see FoodHolder
 */
public class TextListFragment extends Fragment implements OnSearchItemClickListener{

	private static final String KEY_SOURCE_FOODS = "keySourceFoods";
	private List<FoodHolder> mGroupedFoodHolders;
	private int mCountPerList = 20;
	private ImageFetcher mImageFetcher;

	private ViewPager mViewPager;		
	private SearchFoodHandler mSearchHandler;
	private TextView mKitchenText;
	private TextView mCurrentPageText;
	private EditText mSearchEditText;
	private OnTextListChangedListener mOnTextListChangeListener;
	
	protected int mCurrentPosition;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mImageFetcher = new ImageFetcher(getActivity(), 50);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.food_list_fgm, null);
		
		Bundle args = getArguments();
		
    	ArrayList<FoodParcel> foodParcels = args.getParcelableArrayList(KEY_SOURCE_FOODS);
    	final ArrayList<OrderFood> srcFoods = new ArrayList<OrderFood>();
    	for(FoodParcel foodParcel : foodParcels){
    		srcFoods.add(foodParcel);
    	}
    	
    	mViewPager = (ViewPager) layout.findViewById(R.id.viewPager_TextListFgm);
        mViewPager.setOffscreenPageLimit(0);
        
		notifyDataSetChanged(srcFoods);		
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				if(mCurrentPosition != position){
					refreshDisplay(position);
					mCurrentPosition = position;
					
					if(mOnTextListChangeListener != null)
						mOnTextListChangeListener.onTextListChanged(mGroupedFoodHolders.get(position).getThisKitchen(),mGroupedFoodHolders.get(position).getCaptainFood());
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
		
		mViewPager.post(new Runnable() {
			
			@Override
			public void run() {
				refreshDisplay(0);
				((TextView) getView().findViewById(R.id.textView_textListFgm_sumPage)).setText("共" + mGroupedFoodHolders.size() + "页");
			}
		});
		
    	mSearchEditText = (EditText) layout.findViewById(R.id.editText_TextListFgm);
		//搜索框
		mSearchHandler = new SearchFoodHandler(this, mSearchEditText, (Button) layout.findViewById(R.id.button_TextListFgm_clear));
		mSearchHandler.setOnSearchItemClickListener(this); 
		
		mKitchenText = (TextView) layout.findViewById(R.id.textView_TextListFgm_kitchen);
		mCurrentPageText = (TextView) layout.findViewById(R.id.textView_TextListFgm_curPage);
		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		try{
			mOnTextListChangeListener = (OnTextListChangedListener) getActivity();
		} catch(ClassCastException e){
			Log.e("ClassCastException", "activity must implement the OnTextListChangeListener");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getView().post(new Runnable() {
			
			@Override
			public void run() {
				mViewPager.setAdapter(new TextPagerAdapter(getFragmentManager(), mGroupedFoodHolders.size()));		
			}
		});
		
	}

	/**
	 * input the source data and this method will classify the foods and pack it into {@link FoodHolder}
	 * @see FoodHolder
	 * @param srcFoods
	 */
	public void notifyDataSetChanged(List<OrderFood> srcFoods){
		//将筛选出的菜品打包成List<List<T>>格式
		mGroupedFoodHolders = new ArrayList<FoodHolder>();
		ArrayList<List<OrderFood>> mSrcFoodsList = new ArrayList<List<OrderFood>>();
		PKitchen lastKitchen = srcFoods.get(0).getKitchen();
		List<OrderFood> theKitchenList = new ArrayList<OrderFood>();
		//将菜品按厨房分组
		for(int i=0;i<srcFoods.size();i++)
		{
			if(srcFoods.get(i).getKitchen().equals(lastKitchen))
			{
				theKitchenList.add(srcFoods.get(i));
			}
			else{
				mSrcFoodsList.add(theKitchenList);
				theKitchenList = new ArrayList<OrderFood>();
				lastKitchen = srcFoods.get(i).getKitchen();
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
				FoodHolder holder = new FoodHolder(foodPerPage, pageNum, pageSize, foodPerPage.get(0).getKitchen(), foodPerPage.get(0));
				mGroupedFoodHolders.add(holder);
			}
		}
	}

	/**
	 * the factory method to build a new instance of {@link TextListFragment}
	 * @param list the source data of this fragment
	 * @return
	 */
	public static TextListFragment newInstance(List<Food> list) {
		TextListFragment fgm = new TextListFragment();
		
		Bundle args = new Bundle();
		ArrayList<FoodParcel> foodParcels = new ArrayList<FoodParcel>();
		for(Food f : list){
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
	
	/**
	 * when search item was clicked , it will jump to the selected food and high light this food
	 */
	@Override
	public void onSearchItemClick(final Food food) {
		setPositionByFood(food);
		
		mViewPager.postDelayed(new Runnable() {
			@Override
			public void run() {
				//高亮选中的food
				TextListItemFragment curFgm = (TextListItemFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem());
				curFgm.setFoodHighLight(food);
			}
		}, 400);
	}
	
	/**
	 * the position according to the food
	 * @param food
	 */
	public void setPositionByFood(Food food){
		if(mGroupedFoodHolders != null){
			int pos = -1;
			for (int i = 0; i < mGroupedFoodHolders.size(); i++) {
				FoodHolder holder = mGroupedFoodHolders.get(i);
				if(holder.getThisKitchen().getAliasId() == food.getKitchen().getAliasId()){
					ArrayList<OrderFood> mFoods = holder.getFoods();
					for (int j = 0; j < mFoods.size(); j++) {
						OrderFood f = mFoods.get(j);
						if(f.getAliasId() == food.getAliasId()){
							pos = i;
							break;
						}
					}
				}
			}
			
			if(pos != -1 && mViewPager != null){
				setPosition(pos);
			}
		}
	}
	/**
	 * 通过传入的厨房参数，设置当前显示的厨房
	 * @return 若包含该厨房则返回对应位置，否则返回负一
	 * @param kitchen
	 */
	public void setPositionByKitchen(PKitchen kitchen){
		if(mGroupedFoodHolders == null){
			
		} else {
				new AsyncTask<PKitchen, Void, Integer>() {
		
				@Override
				protected Integer doInBackground(PKitchen... params) {
					for (int i = 0; i < mGroupedFoodHolders.size(); i++) {
						FoodHolder holder = mGroupedFoodHolders.get(i);
						PKitchen theKitchen = holder.getThisKitchen();
						if(theKitchen.getAliasId() == params[0].getAliasId()){
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
	}
	
	public void setPosition(int position){
		if(mCurrentPosition != position){
			mViewPager.setCurrentItem(position, false);
		}
	}
	
	//更新标题栏的厨房名和厨房数量
	private void refreshDisplay(int position){
		FoodHolder holder = mGroupedFoodHolders.get(position);
		
		PKitchen kitchen = null;
		for(PKitchen k: WirelessOrder.foodMenu.kitchens){
			if(holder.getFoods().get(0).getKitchen().getAliasId() == k.getAliasId())
				kitchen = k;
		}
		if(kitchen != null){
			mKitchenText.setText(kitchen.getName());
		}else{
			mKitchenText.setText(Integer.toString(holder.getFoods().get(0).getKitchen().getAliasId()));
		}
//		
		mCurrentPageText.setText("第" + (position+1) + "页");
	}
	
	private class TextPagerAdapter extends FragmentStatePagerAdapter {

		private int mSize;

		public TextPagerAdapter(FragmentManager fm, int size) {
			super(fm);
			mSize = size;
		}

		@Override
		public Fragment getItem(int position) {
			return TextListItemFragment.newInstance(mGroupedFoodHolders.get(position).getFoods(), TextListFragment.this.getTag());
		}

		@Override
		public int getCount() {
			return mSize;
		}
	}

	public interface OnTextListChangedListener{
		void onTextListChanged(PKitchen kitchen, OrderFood captainFood);
	}
	
	public void setOnTextListChangeListener(OnTextListChangedListener l){
		mOnTextListChangeListener = l;
	}
}
/**
 * 厨房菜品的持有类
 * 保持当前厨房的总页数、当前页数、厨房实例和captainFood 
 * @author ggdsn1
 *
 */
class FoodHolder {
	private ArrayList<OrderFood> mFoods;
	private int mCurrentPage;
	private int mTotalPage;
	private PKitchen mCurrentKitchen;
	private OrderFood mCaptainFood;
	
	public FoodHolder(ArrayList<OrderFood> mFoods, int mCurrentPage, int mTotalPage, PKitchen kitchen, OrderFood captainFood) {
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

	public PKitchen getThisKitchen() {
		return mCurrentKitchen;
	}
	public OrderFood getCaptainFood(){
		return mCaptainFood;
	}
}
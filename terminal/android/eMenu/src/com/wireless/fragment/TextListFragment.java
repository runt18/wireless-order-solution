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
import com.wireless.protocol.DepartmentTree;
import com.wireless.protocol.DepartmentTree.KitchenNode;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.PKitchen;
import com.wireless.util.SearchFoodHandler;
import com.wireless.util.SearchFoodHandler.OnSearchItemClickListener;
import com.wireless.util.imgFetcher.ImageFetcher;

/**
 * This fragment contains a {@link ViewPager} to manage all sub items({@link TextListItemFragment})<br/>
 * it use {@link ArrayList} to carry each pages ,each {@link TextFoodPager} is a token of a page,
 * contains some details about the page,like foods, kitchen, captain food ,etc
 * @author ggdsn1
 * @see TextFoodPager
 */
public class TextListFragment extends Fragment implements OnSearchItemClickListener{

	public static interface OnTextListChangedListener{
		/**
		 * Called when the text page is changed.
		 * @param captainFood the captain food to this page
		 */
		void onTextListChanged(OrderFood captainFood);
	}
	
	/**
	 * 保存文字模式中每页分类菜品的信息，
	 * 包括本页的菜品信息，当前页数、captainFood 
	 */
	private static class TextFoodPager {
		
		public final static int MAX_AMOUNT = 20;
		
		private final List<OrderFood> mFoods = new ArrayList<OrderFood>();
		
		public TextFoodPager(List<Food> foodList) {
			if(foodList.size() > MAX_AMOUNT){
				throw new IllegalArgumentException("The amount to food list exceeds " + MAX_AMOUNT);
			}
			if(foodList.size() == 0){
				throw new IllegalArgumentException("The amount to food list can NOT be zero.");
			}
			for(Food f : foodList){
				mFoods.add(new OrderFood(f));
			}
		}
		
		public List<OrderFood> getFoods() {
			return mFoods;
		}

		public OrderFood getCaptainFood(){
			return mFoods.get(0);
		}
	}
	
	private static final String KEY_SOURCE_FOODS = "keySourceFoods";
	private List<TextFoodPager> mTextFoodPagers = new ArrayList<TextFoodPager>();

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
						mOnTextListChangeListener.onTextListChanged(mTextFoodPagers.get(position).getCaptainFood());
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
				((TextView) getView().findViewById(R.id.textView_textListFgm_sumPage)).setText("共" + mTextFoodPagers.size() + "页");
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
				mViewPager.setAdapter(new TextPagerAdapter(getFragmentManager(), mTextFoodPagers.size()));		
			}
		});
		
	}

	/**
	 * input the source data and this method will classify the foods and pack it into {@link TextFoodPager}
	 * @see TextFoodPager
	 * @param srcFoods
	 */
	private void notifyDataSetChanged(List<OrderFood> srcFoods){
		
		//将筛选出的菜品打包成List<List<T>>格式
		mTextFoodPagers.clear();
		List<List<OrderFood>> foodsToKitchen = new ArrayList<List<OrderFood>>();
		PKitchen lastKitchen = srcFoods.get(0).getKitchen();
		List<OrderFood> theKitchenList = new ArrayList<OrderFood>();
		//将菜品按厨房分组
		for(int i = 0; i < srcFoods.size(); i++){
			if(srcFoods.get(i).getKitchen().equals(lastKitchen)){
				theKitchenList.add(srcFoods.get(i));
				
			}else{
				foodsToKitchen.add(theKitchenList);
				theKitchenList = new ArrayList<OrderFood>();
				lastKitchen = srcFoods.get(i).getKitchen();
				theKitchenList.add(srcFoods.get(i));
			}
			if(i == srcFoods.size() - 1)
				foodsToKitchen.add(theKitchenList);
		}
		
		int countPerPage = TextFoodPager.MAX_AMOUNT;
		//遍历每个厨房菜品
		for(List<OrderFood> kitchenList : foodsToKitchen){
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
				TextFoodPager holder = new TextFoodPager(null);
				mTextFoodPagers.add(holder);
			}
		}
	}

	public void notifyDataSetChanged(DepartmentTree deptTree){
		for(KitchenNode kitchenNode : deptTree.asKitchenNodes()){
			List<Food> foodList = kitchenNode.getValue();
			//计算出页数
			int pageSize = (foodList.size() / TextFoodPager.MAX_AMOUNT) + (foodList.size() % TextFoodPager.MAX_AMOUNT == 0 ? 0 : 1);
			//把每一页的菜品装入Pager
			for(int i = 0; i < pageSize; i++){
				int start = i * TextFoodPager.MAX_AMOUNT;
				int end = start + TextFoodPager.MAX_AMOUNT;
				if(end > foodList.size()){
					end = foodList.size();
				}
				mTextFoodPagers.add(new TextFoodPager(foodList.subList(start, end)));
			}
		}
	}
	
	public void setOnTextListChangeListener(OnTextListChangedListener l){
		mOnTextListChangeListener = l;
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
	 * Jump to the pager which the captain food is located in.
	 * @param food the captain food of pager wants to jump to
	 */
	public void setPositionByFood(Food food){
		int pageNo = -1;
		for(TextFoodPager pager : mTextFoodPagers){
			for(Food f : pager.getFoods()){
				if(f.equals(food)){
					setPosition(pageNo);
					return;
				}
			}
			pageNo++;
		}
	}
	
	/**
	 * Jump to the pager which the kitchen of captain food is located in.
	 * @param kitchen the kitchen of captain food to pager wants to jump. 
	 */
	public void setPositionByKitchen(final PKitchen kitchen){
		new AsyncTask<Void, Void, Integer>() {

			@Override
			protected Integer doInBackground(Void... params) {
				int pageNo = 0;
				for(TextFoodPager pager : mTextFoodPagers){
					if(pager.getCaptainFood().getKitchen().equals(kitchen)){
						return pageNo;
					}
					pageNo++;
				}
				return pageNo;
			}
	
			@Override
			protected void onPostExecute(Integer pageNo) {
				setPosition(pageNo);
			}
		}.execute();
	}
	
	private void setPosition(int pageNo){
		if(mCurrentPosition != pageNo && mViewPager != null){
			mViewPager.setCurrentItem(pageNo, false);
		}
	}
	
	//更新标题栏的厨房名和厨房数量
	private void refreshDisplay(int position){
		TextFoodPager holder = mTextFoodPagers.get(position);
		
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
			return TextListItemFragment.newInstance(mTextFoodPagers.get(position).getFoods(), TextListFragment.this.getTag());
		}

		@Override
		public int getCount() {
			return mSize;
		}
	}


}


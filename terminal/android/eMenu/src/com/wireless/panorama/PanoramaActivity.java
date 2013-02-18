package com.wireless.panorama;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SearchView.OnSuggestionListener;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.BuildConfig;
import com.wireless.ordermenu.R;
import com.wireless.panorama.util.FoodGroupProvider;
import com.wireless.panorama.util.FramePager;
import com.wireless.panorama.util.LayoutArranger;
import com.wireless.panorama.util.SearchProvider;
import com.wireless.panorama.util.SystemUiHider;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.ui.ChooseModelActivity;
import com.wireless.util.NumericUtil;
import com.wireless.util.QueryFoodAssociationTaskImpl;
import com.wireless.util.QueryFoodAssociationTaskImpl.OnFoodClickListener;
import com.wireless.util.imgFetcher.ImageCache;
import com.wireless.util.imgFetcher.ImageCache.ImageCacheParams;
import com.wireless.util.imgFetcher.ImageFetcher;

/**
 * <h3>全景模式的activity</h3>
 * <p>此activity包含一个{@link ViewPager}来负责左右滑动并显示图片 </p>
 * <p>另外还包含{@link ActionBar} 和 弹出窗 {@link QueryFoodAssociationTask} 的部分逻辑</p>
 * 
 * <p>此activity 包含一个{@link LayoutArranger}，负责将获得的图层筛选并排序，再将所得的图层id传递给ViewPager的adapter类进行显示。
 * {@link PanoramaItemFragment} 负责显示图层相关的内容</p>
 * 
 * <p>
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * </p>
 * @see SystemUiHider
 * @see LayoutArranger
 * @see PanoramaItemFragment
 */
public class PanoramaActivity extends Activity implements OnFoodClickListener {
	public static final String TAG = "PanoramaActivity";
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS_SYSTEM_UI} milliseconds.
	 */ 
	private static boolean AUTO_HIDE = false;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */ 
	private static final int AUTO_HIDE_DELAY_MILLIS_SYSTEM_UI = 3000;

	/**
	 * default millis to hide the input method 
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS_INPUT = 3000;
	/** 
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	private static final String CURRENT_ITEM = "currentItem";

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	private ImageFetcher mImageFetcher;

	private FragmentPagerAdapter mAdapter;

	private ViewPager mViewPager;
	
	private AsyncTask<Department, Void, ArrayList<View>> mRefreshDeptFoodTask;

	/**
	 * 界面组织、安排的结构
	 */
	private LayoutArranger mLayoutArranger;

	private ArrayList<Kitchen> mKitchens;
	private Department mCurrentDept;
	private PopupWindow mComboPopup;
	private ImageFetcher mComboFetcher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.activity_panorama);
		
////////////////systemUi部分/////////////////////////
		final View controlsView = findViewById(R.id.panorama_content_controls);
		final View contentView = findViewById(R.id.viewPager_panorama);
	
		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
							}
							controlsView.animate().translationY(visible ? 0 : mControlsHeight).setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHideSystemUi(AUTO_HIDE_DELAY_MILLIS_SYSTEM_UI);
						}
					}
				});

		//底部返回键
//		findViewById(R.id.button_panorama_back).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				onBackPressed();
//			}
//		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.panorama_content_controls).setOnTouchListener(mDelayHideTouchListener);
////////////end systemUi //////////////////////////////////
		
/////////////////load layout////////////////////////
		mLayoutArranger = new LayoutArranger(this, getString(R.string.layout_packageName));
		mLayoutArranger.notifyFoodGroupsChanged(FoodGroupProvider.getInstance().getGroups());
////////////end load layout////////////////////////////

/////////////navigation data///////////////////////////////
		Intent intent = getIntent();
		//准备导航数据
		ArrayList<Integer> deptIds = intent.getIntegerArrayListExtra(ChooseModelActivity.KEY_DEPT_ID);
		ArrayList<Integer> kitchenIds = intent.getIntegerArrayListExtra(ChooseModelActivity.KEY_KITCHEN_ID);
		
		ArrayList<Department> depts = new ArrayList<Department>();
		for(Integer id : deptIds){
			for(Department d: WirelessOrder.foodMenu.depts){
				if(id == d.getId()){
					depts.add(d);
					break;
				}
			}
		}
		
		mKitchens = new ArrayList<Kitchen>();
		for(int id : kitchenIds){
			for(Kitchen k: WirelessOrder.foodMenu.kitchens){
				if(id == k.getAliasId()){
					mKitchens.add(k);
					break;
				}
			}
		}
///////////////end navigation data/////////////////
////////////imageFetcher and viewPager///////////////////// 
		ImageCache.ImageCacheParams cacheParams = new ImageCacheParams(this, "panorama");
		cacheParams.setMemCacheSizePercent(this, 0.25f);
		
		mImageFetcher = new ImageFetcher(this, 0);
		mImageFetcher.addImageCache(getFragmentManager(), cacheParams, "panorama");
		mImageFetcher.setImageFadeIn(true);
		
		mAdapter  = new FragmentPagerAdapter(getFragmentManager()) {
			
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				Object item = super.instantiateItem(container, position);
				
				return item;
			}

			@Override
			public int getCount() {
				return mLayoutArranger.getGroups().size();
			}
			
			@Override
			public Fragment getItem(int position) {
				return PanoramaItemFragment.newInstance(mLayoutArranger.getGroups().get(position));
			}
		};
		
		mCurrentDept = depts.get(0);
		
		mViewPager = (ViewPager) findViewById(R.id.viewPager_panorama);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setTag(depts.get(0));
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				int id = mLayoutArranger.getGroup(position).getCaptainFood().getKitchen().getDept().getId();
				//判断是否切换导航标
				ActionBar bar = getActionBar();
				for(int i = 0;i<bar.getTabCount(); i++){
					Object tag = bar.getTabAt(i).getTag();
					if(tag != null){
						Department dept = (Department) tag;
						if(dept.getId() == id && mCurrentDept.getId() != dept.getId()){
							mCurrentDept = dept;
							mViewPager.setTag(dept);
							bar.setSelectedNavigationItem(i);
							mRefreshDeptFoodTask = new RefreshDeptFoodTask().execute(dept);

							break;
						} 
					}
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				delayedHideSystemUi(AUTO_HIDE_DELAY_MILLIS_SYSTEM_UI);
			}
		});
		
////////////////////end viewPager////////////////////
		
		//添加导航
		ActionBar bar = getActionBar();
		for(Department d: depts){
			bar.addTab(bar.newTab().setTag(d).setText(d.getName()).setTabListener(new MyTabListener()));
		}
		
		//推荐菜弹出窗口
		mComboPopup = new PopupWindow(getLayoutInflater().inflate(R.layout.gallery_fgm_combo, null),
				640,LayoutParams.WRAP_CONTENT);
		mComboPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_small));
		mComboPopup.setOutsideTouchable(true);
		mComboFetcher = new ImageFetcher(this, 200,144);

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHideSystemUi(100);
		
		//set current item
		int currentItem = getIntent().getIntExtra(CURRENT_ITEM, -1);
		if(currentItem != -1){
			mViewPager.setCurrentItem(currentItem);
		}
		
		mRefreshDeptFoodTask = new RefreshDeptFoodTask().execute(mCurrentDept);

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
        if(mRefreshDeptFoodTask != null)
        	mRefreshDeptFoodTask.cancel(true);
    }
    
	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHideSystemUi(AUTO_HIDE_DELAY_MILLIS_SYSTEM_UI);
			}
			return false;
		}
	};

	Handler mHideSystemUiHandler = new Handler();
	Runnable mHideSystemUiRunnable = new Runnable() {
		@Override
		public void run() {
			if(BuildConfig.DEBUG){
				Log.i(TAG,"hideRunnable run");
			}
			mSystemUiHider.hide(); 
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHideSystemUi(int delayMillis) {
		if(BuildConfig.DEBUG){
			Log.i(TAG,"delayedHide() run");
		}
		mHideSystemUiHandler.removeCallbacks(mHideSystemUiRunnable);
		mHideSystemUiHandler.postDelayed(mHideSystemUiRunnable, delayMillis);
	}

	Handler mHideIMHandler = new Handler();
	Runnable mHideIMRunnable = new Runnable() {
		
		@Override
		public void run() {
			//隐藏键盘
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if(getCurrentFocus() != null)
				imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	};
	private void delayedHideInputMethod(int delayMillis){
		mHideIMHandler.removeCallbacks(mHideIMRunnable);
		mHideIMHandler.postDelayed(mHideIMRunnable, delayMillis);
	}
	
	private SearchView mSearchView;

	/**
	 * 生成panoramaActivity上actionbar的按钮和菜单
	 * 
	 * <p>当sdk大于3.0时，将使用searchview</p>
	 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.panorama_menu, menu);
        
        //配置searchView
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
        	SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        	mSearchView = (SearchView) menu.findItem(R.id.menu_panorama_search).getActionView();
        	mSearchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        	
        	mSearchView.setOnSearchClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(BuildConfig.DEBUG){
						Log.v("OnSearchClickListener", "onClick()");
					}
					//点击搜索时取消隐藏
					mHideSystemUiHandler.removeCallbacks(mHideSystemUiRunnable);
				}
			});
        	mSearchView.setSuggestionsAdapter(SearchProvider.getSuggestionsAdapter(PanoramaActivity.this));
        	//设置搜索侦听
        	mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
				String TAG = "OnQueryTextListener";
				@Override
				public boolean onQueryTextSubmit(String query) {
					return false;
				}
				
				/**
				 * 根据输入的text，将adapter里的cursor替换成搜索所得的cursor
				 */
				@Override
				public boolean onQueryTextChange(String newText) {
					if(BuildConfig.DEBUG){
						Log.v(TAG, "onQueryTextChange() "+ newText);
					}
					
					if(newText != null && !newText.equals("")){
						mSearchView.getSuggestionsAdapter().changeCursor(SearchProvider.getSuggestions(newText));
					}
					mHideSystemUiHandler.removeCallbacks(mHideSystemUiRunnable);

					delayedHideInputMethod(AUTO_HIDE_DELAY_MILLIS_INPUT);
					return false;
				}
			});
        	//设置选择suggestion选择的侦听
        	mSearchView.setOnSuggestionListener(new OnSuggestionListener() {
				String TAG = "OnSuggestionListener"; 
				@Override
				public boolean onSuggestionSelect(int position) {
					if(BuildConfig.DEBUG){
						Log.v(TAG, "selected " + position);
					}
					return true;
				}
				
				/**
				 * 根据点击位置，拿取对应菜品的id，并根据id查找对应菜品
				 * 若找到则跳转到相应页面
				 */
				@Override
				public boolean onSuggestionClick(int position) {
					if(BuildConfig.DEBUG){
						Log.v(TAG, "clicked " + position);
					}
					
					Cursor cursor = mSearchView.getSuggestionsAdapter().getCursor();
					if(cursor.moveToPosition(position)){
						int id = cursor.getInt(0);
						if(id != 0){
							Log.i(TAG, "id:"+id);
							for(Food f:WirelessOrder.foodMenu.foods){
								if(f.getAliasId() == id){
									setPositionByFood(f);
									//清空搜索
									mSearchView.setIconified(true);
									
									return true;
								} 
							}
							Toast.makeText(PanoramaActivity.this, "此菜暂无图片显示", Toast.LENGTH_SHORT).show();
						}
					}
					return true;
				}
			});
        	
        }
        //TODO 添加餐台、服务员设置和临时菜功能
        return true;
    }

    /**
     * 设置每个项点击时的动作
     */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_panorama_search:
			break;
		case android.R.id.home:
			onBackPressed();
			break;
		case R.id.menu_panorama_shoppingCart:
			startActivity(new Intent(this, PanoramaFoodSelectedActivity.class));
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * 清空搜索内容
	 */
	public void closeSearchView(){
		if(mSearchView != null)
			mSearchView.setIconified(true);
	}
	
	public ImageFetcher getImageFetcher() {
		return mImageFetcher;
	}

	/**
	 * 设置systemUi 隐藏或可见
	 * @param v
	 */
	public void toggleOnClick(View v) {
		if (TOGGLE_ON_CLICK) {
			mSystemUiHider.toggle();
		} else {
			mSystemUiHider.show();
		}
	}
	
	/**
	 * 设置是否开启自动隐藏功能
	 * @param hide
	 */
	public void setAutoHide(boolean hide){
		AUTO_HIDE = hide;
	}
	
	public LayoutArranger getLayoutArranger(){
		return mLayoutArranger;
	}
	
	public void setPositionByFood(Food food){
		//FIXME 修改成byKitchen
		//当底部菜品点击的时候，异步计算出对应的位置
		new AsyncTask<Food, Void, Integer>(){

			/**
			 * 异步计算位置，如果找到位置则返回位置
			 * 否则返回-1
			 */
			@Override
			protected Integer doInBackground(Food... params) {
				ArrayList<FramePager> groups = mLayoutArranger.getGroups();
				for (int j = 0; j < groups.size(); j++) {
					FramePager pager = groups.get(j);
					List<Food> allFoods = pager.getAllFoodsByList();
					for (int k = 0; k < allFoods.size(); k++) {
						Food f = allFoods.get(k);
						if(f.getAliasId() == params[0].getAliasId()){
							return j;
						}
					}
				}
				return -1;
			}
			
			/**
			 * 根据返回值，判断是否跳转
			 */
			@Override
			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);
				if(result != null && result != -1){
					if(result == mViewPager.getCurrentItem())
						Toast.makeText(PanoramaActivity.this, "该菜品已在当前页", Toast.LENGTH_SHORT).show();
					else mViewPager.setCurrentItem(result);
				} else {
					Toast.makeText(PanoramaActivity.this, "此菜暂无图片显示", Toast.LENGTH_SHORT).show();
				}
				delayedHideSystemUi(0);

			}
			
		}.execute(food);
	}
	
	public void setPositionByKitchen(Kitchen kitchen){
		//TODO 添加根据厨房跳转的功能
	}
	
	void addOnClick(Food food){
		QueryFoodAssociationTaskImpl queryFoodAssociationTaskImpl = new QueryFoodAssociationTaskImpl(
				food, this, findViewById(R.id.panorama_content_controls), mComboPopup, mComboFetcher);
		queryFoodAssociationTaskImpl.setOnFoodClickListener(this);
		queryFoodAssociationTaskImpl.execute(WirelessOrder.foodMenu);
	}
	/**
	 * actionBar 上tab的listener
	 */
	class MyTabListener implements TabListener{

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			Object tag = tab.getTag();
			//切换到对应部门的第一个选项,同时更新
			if(tag != null){
				Department dept = (Department) tag;
				for(int i = 0 ; i < mLayoutArranger.getGroups().size(); i++){
					Department d = mLayoutArranger.getGroup(i).getCaptainFood().getKitchen().getDept();
					if(d.getId() == dept.getId() && ((Department)mViewPager.getTag()) != dept){
						mViewPager.setCurrentItem(i);
						
						mRefreshDeptFoodTask = new RefreshDeptFoodTask().execute(dept);
						break;
					}
				}
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			Object tag = tab.getTag();
			//切换到对应部门的第一个选项
			if(tag != null){
				Department dept = (Department) tag;
				for(int i = 0 ; i < mLayoutArranger.getGroups().size(); i++){
					Department d = mLayoutArranger.getGroup(i).getCaptainFood().getKitchen().getDept();
					if(d.getId() == dept.getId()){
						mViewPager.setCurrentItem(i);
						break;
					}
				}
			}
		}
		
	}
	
	/**
	 * 异步计算，并排列需要显示的菜品，返回给主线程显示
	 * @author ggdsn1
	 *
	 */
	class RefreshDeptFoodTask extends AsyncTask<Department, Void, ArrayList<View>>{
		private ImageFetcher mImageFetcher = new ImageFetcher(PanoramaActivity.this,100,75);
		private int mCountToShow = 4;
		
		@Override
		protected ArrayList<View> doInBackground(Department... depts) {
			if(depts != null && depts.length != 0){
				//筛选出特别的菜品
				ArrayList<Food> matchedFoods = new ArrayList<Food>();
				for(Food f:WirelessOrder.foods){
					if(f.getKitchen().getDept() != null && f.getKitchen().getDept().getId() == depts[0].getId()){
						if(f.isHot() || f.isSpecial() || f.isRecommend())
							matchedFoods.add(f);
					}
				}
				
				if(!matchedFoods.isEmpty()){
					ArrayList<View> views = new ArrayList<View>();
					//根据菜品生成layout
					LayoutInflater inflater = LayoutInflater.from(PanoramaActivity.this);
					LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
					for (int i = 0; i < mCountToShow; i++) {
						final Food f = matchedFoods.get(i);
						View view = inflater.inflate(R.layout.panorama_bottom_item, null);
						view.setTag(f);
						view.setLayoutParams(params);
						
						//底部菜品点击的listener
						view.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								setPositionByFood(f);
							}
						});
						views.add(view);
					}
					return views;
				}
			}
			return null;
		}

		/**
		 * 将返回的layout添加到父layout上，并显示图片和文字
		 */
		@Override
		protected void onPostExecute(ArrayList<View> result) {
			super.onPostExecute(result);
			if(result != null && !result.isEmpty()){
				LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout_panorama);
				if(layout != null){
					layout.removeAllViews();
					for (int i = 0; i < mCountToShow ; i++) {
						View view = result.get(i);
						Food food = (Food) view.getTag();
						if(food != null){
							ImageView image = (ImageView) view.findViewById(R.id.imageView_panorama_bottom_item);
							mImageFetcher.loadImage(food.image, image);
							
							((TextView)view.findViewById(R.id.textView_panorama_bottom_item_name)).setText(food.getName());
							((TextView)view.findViewById(R.id.textView_panorama_bottom_item_price)).setText("￥" + NumericUtil.float2String2(food.getPrice()));
							
							TextView hintText = (TextView)view.findViewById(R.id.textView_panorama_bottom_item_hint);
							hintText.setVisibility(View.VISIBLE);
							if(food.isRecommend())
								hintText.setText("推荐");
							else if(food.isSpecial())
								hintText.setText("特价");
							else if(food.isHot())
								hintText.setText("热");
							layout.addView(view);
						}
					}
				}
			}
		}
	}

	@Override
	public void onFoodClick(Food food) {
		setPositionByFood(food);
	}
}


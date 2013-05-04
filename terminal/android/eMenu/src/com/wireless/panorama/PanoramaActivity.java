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
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SearchView.OnSuggestionListener;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.BuildConfig;
import com.wireless.ordermenu.R;
import com.wireless.panorama.util.FramePager;
import com.wireless.panorama.util.LayoutArranger;
import com.wireless.panorama.util.SearchProvider;
import com.wireless.panorama.util.SystemUiHider;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.protocol.Food;
import com.wireless.util.ExhibitPopupWindow;
import com.wireless.util.imgFetcher.ImageCache;
import com.wireless.util.imgFetcher.ImageCache.ImageCacheParams;
import com.wireless.util.imgFetcher.ImageFetcher;

/**
 * <h3>全景模式的activity</h3>
 * <p>此activity包含一个{@link ViewPager}来负责左右滑动并显示图片 </p>
 * <p>另外还包含{@link ActionBar} 和 弹出窗 {@link QueryFoodAssociationTaskImpl} 的部分逻辑</p>
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
public class PanoramaActivity extends Activity implements ExhibitPopupWindow.OnExhibitOperateListener {
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
	 * The handler to hide system UI.
	 */
	private Handler mHideSystemUiHandler = new Handler();
	
	private Runnable mHideSystemUiRunnable = new Runnable() {
		@Override
		public void run() {
			if(BuildConfig.DEBUG){
				Log.i(TAG, "hideRunnable run");
			}
			mSystemUiHider.hide(); 
		}
	};
	
	/**
	 * The handler to hide input method
	 */
	private Handler mHideIMHandler = new Handler();
	
	private Runnable mHideIMRunnable = new Runnable() {
		@Override
		public void run() {
			//Hide soft keyboard
			((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	};
	
	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	/**
	 * The search view on ActionBar
	 */
	private SearchView mSearchView;
	
	private ImageFetcher mImageFetcher;

	private FragmentPagerAdapter mAdapter;

	private ViewPager mViewPager;
	
	private AsyncTask<Department, Void, List<View>> mRefreshDeptFoodTask;

	/**
	 * 界面组织、安排的结构
	 */
	private LayoutArranger mLayoutArranger;

	private Department mCurrentDept;
	private ExhibitPopupWindow mComboPopup;

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
		mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
			// Cached values.
			private int mControlsHeight;
			private int mShortAnimTime;

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


		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.panorama_content_controls).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (AUTO_HIDE) {
					delayedHideSystemUi(AUTO_HIDE_DELAY_MILLIS_SYSTEM_UI);
				}
				return false;
			}
		});
		////////////end systemUi //////////////////////////////////
		
		/////////////////load layout////////////////////////
		mLayoutArranger = new LayoutArranger(this, getString(R.string.layout_packageName));
		mLayoutArranger.notifyFoodGroupsChanged(WirelessOrder.pagers);
		////////////end load layout////////////////////////////

		
		//准备ActionBar的导航数据
		
		
		//添加ActionBar中的导航
		ActionBar bar = getActionBar();
		for(Department d : WirelessOrder.foodMenu.depts){
			bar.addTab(bar.newTab().setTag(d).setText(d.getName()).setTabListener(new NaviTabListener()));
		}
		
		////////////imageFetcher and viewPager///////////////////// 
		ImageCache.ImageCacheParams cacheParams = new ImageCacheParams(this, 0.25f);
		
		mImageFetcher = new ImageFetcher(this, 0);
		mImageFetcher.addImageCache(getFragmentManager(), cacheParams, TAG);
		mImageFetcher.setImageFadeIn(true);
		
		mAdapter = new FragmentPagerAdapter(getFragmentManager()) {
			
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
		
		mCurrentDept = WirelessOrder.foodMenu.depts.get(0);
		
		mViewPager = (ViewPager)contentView;
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setTag(WirelessOrder.foodMenu.depts.get(0));
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				int id = mLayoutArranger.getGroup(position).getCaptainFood().getKitchen().getDept().getId();
				//判断是否切换导航标
				ActionBar bar = getActionBar();
				for(int i = 0; i < bar.getTabCount(); i++){
					Object tag = bar.getTabAt(i).getTag();
					if(tag != null){
						Department dept = (Department) tag;
						if(dept.getId() == id && mCurrentDept.getId() != dept.getId()){
							mCurrentDept = dept;
							mViewPager.setTag(dept);
							bar.setSelectedNavigationItem(i);
							
							if(mRefreshDeptFoodTask.getStatus() != AsyncTask.Status.FINISHED){
								mRefreshDeptFoodTask.cancel(true);
							}
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
		
		//创建RefreshDeptFoodTask的Task
		mRefreshDeptFoodTask = new RefreshDeptFoodTask();
		
		//推荐菜弹出窗口
		mComboPopup = new ExhibitPopupWindow(getLayoutInflater().inflate(R.layout.gallery_fgm_combo, null),
									  		 640,
									  		 LayoutParams.WRAP_CONTENT);
		mComboPopup.setOperateListener(this);
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
		if(mRefreshDeptFoodTask.getStatus() != AsyncTask.Status.FINISHED){
			mRefreshDeptFoodTask.cancel(true);
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
        if(mRefreshDeptFoodTask.getStatus() != AsyncTask.Status.FINISHED){
        	mRefreshDeptFoodTask.cancel(true);
        }
    }
    
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

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHideInputMethod(int delayMillis){
		mHideIMHandler.removeCallbacks(mHideIMRunnable);
		mHideIMHandler.postDelayed(mHideIMRunnable, delayMillis);
	}
	
	/**
	 * 生成{@link PanoramaActivity}上ActionBar的按钮和菜单
	 * 
	 * <p>当sdk大于3.0时，将使用SearchView</p>
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
        	
        	//设置SearchView的CursorAdaptor
        	mSearchView.setSuggestionsAdapter(SearchProvider.getSuggestionsAdapter(PanoramaActivity.this));
        	
        	//设置搜索侦听
        	mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
        		
				private final static String TAG = "OnQueryTextListener";
				
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
					
					if(newText != null && newText.trim().length() != 0){
						mSearchView.getSuggestionsAdapter().changeCursor(SearchProvider.getSuggestions(newText));
					}
					mHideSystemUiHandler.removeCallbacks(mHideSystemUiRunnable);

					delayedHideInputMethod(AUTO_HIDE_DELAY_MILLIS_INPUT);
					return false;
				}
			});
        	
        	//设置点击某个Suggestion的侦听
        	mSearchView.setOnSuggestionListener(new OnSuggestionListener() {
        		
				private final static String TAG = "OnSuggestionListener";
				
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
							if(BuildConfig.DEBUG){
								Log.i(TAG, "id:" + id);
							}
							for(Food f : WirelessOrder.foodMenu.foods){
								if(f.getAliasId() == id){
									setPositionByFood(f);
									//清空搜索
									mSearchView.setIconified(true);
									
									//remove auto hide input method and system ui once
									mSearchView.postDelayed(new Runnable() {
										@Override
										public void run() {
											mHideSystemUiHandler.removeCallbacks(mHideSystemUiRunnable);
											mHideIMHandler.removeCallbacks(mHideIMRunnable);

										}
									}, 1000);
									
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
        return true;
    }

    /**
     * according to different menuItem id, define different motions
     * the menuItem was define by the xml {@code panorama_menu.xml}
     */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		//the back button on the ActionBar
		case android.R.id.home:
			onBackPressed();
			break;
		//the bill item on the ActionBar
		case R.id.menu_panorama_shoppingCart:
			startActivity(new Intent(this, PanoramaFoodSelectedActivity.class));
			break;
		case R.id.menu_panorama_setTable:
			//TODO add set table function at here
			break;
		case R.id.menu_panorama_setStaff:
			//TODO add set staff function at here
			break;
		case R.id.menu_panorama_addTempDish:
			//TODO add temp food function at here
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
	
	/**
	 * Having the {@link ViewPager} jump to the position where the food at.
	 * @param food 
	 */
	public void setPositionByFood(Food food){
		//当底部菜品点击的时候，异步计算出对应的位置
		new AsyncTask<Food, Void, Integer>(){

			/**
			 * 异步计算位置，如果找到位置则返回位置
			 * 否则返回-1
			 */
			@Override
			protected Integer doInBackground(Food... params) {
				ArrayList<FramePager> groups = mLayoutArranger.getGroups();
				for (int i = 0; i < groups.size(); i++) {
					List<Food> allFoods = groups.get(i).getAllFoodsByList();
					for (int k = 0; k < allFoods.size(); k++) {
						if(allFoods.get(k).equals(params[0])){
							return i;
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
	
	/**
	 * Having the pop up window to show the associated foods.
	 * @param foodToAssociated the food to associated
	 */
	void showAssociatedFood(Food foodToAssociated){
		mComboPopup.showAssociatedFoods(findViewById(R.id.panorama_content_controls), 50, 20, foodToAssociated);
	}
	
	/**
	 * ActionBar上导航Tab的Listener
	 */
	class NaviTabListener implements TabListener{

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			Object tagFromTab = tab.getTag();
			//切换到对应部门的第一个选项,同时更新
			if(tagFromTab != null && mViewPager != null){
				Department dept = (Department) tagFromTab;
				for(int i = 0 ; i < mLayoutArranger.getGroups().size(); i++){
					Department d = mLayoutArranger.getGroup(i).getCaptainFood().getKitchen().getDept();
					if(d.getId() == dept.getId() && ((Department)mViewPager.getTag()).getId() != dept.getId()){
						mViewPager.setCurrentItem(i);
						if(mRefreshDeptFoodTask.getStatus() != AsyncTask.Status.FINISHED){
							mRefreshDeptFoodTask.cancel(true);
						}
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
	class RefreshDeptFoodTask extends AsyncTask<Department, Void, List<View>>{
		
		private ImageFetcher mImageFetcher = new ImageFetcher(PanoramaActivity.this, 100, 75);
		
		private final static int AMOUNT_TO_SHOW = 4;
		
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
					for (int i = 0; i < AMOUNT_TO_SHOW; i++) {
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
		 * 将返回的Layout添加到父Layout上，并显示图片和文字
		 */
		@Override
		protected void onPostExecute(List<View> result) {
			if(result != null && !result.isEmpty()){
				LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout_panorama);
				if(layout != null){
					layout.removeAllViews();
					for (int i = 0; i < AMOUNT_TO_SHOW ; i++) {
						View view = result.get(i);
						Food food = (Food) view.getTag();
						if(food != null){
							ImageView image = (ImageView) view.findViewById(R.id.imageView_panorama_bottom_item);
							mImageFetcher.loadImage(food.image, image);
							
							((TextView)view.findViewById(R.id.textView_panorama_bottom_item_name)).setText(food.getName());
							((TextView)view.findViewById(R.id.textView_panorama_bottom_item_price)).setText("￥" + NumericUtil.float2String2(food.getPrice()));
							
							TextView hintText = (TextView)view.findViewById(R.id.textView_panorama_bottom_item_hint);
							hintText.setVisibility(View.VISIBLE);
							
							if(food.isRecommend()){
								hintText.setText("推荐");
							}else if(food.isSpecial()){
								hintText.setText("特价");
							}else if(food.isHot()){
								hintText.setText("热销");
							}
							
							layout.addView(view);
						}
					}
				}
			}
		}
	}


	@Override
	public void onFoodClicked(Food clickedFood) {
		// Jump to pager the clicked food is located at.
		setPositionByFood(clickedFood);
	}
}


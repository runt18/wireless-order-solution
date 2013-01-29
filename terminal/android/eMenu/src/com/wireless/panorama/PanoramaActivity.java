package com.wireless.panorama;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.BuildConfig;
import com.wireless.ordermenu.R;
import com.wireless.panorama.util.FoodGroupProvider;
import com.wireless.panorama.util.LayoutArranger;
import com.wireless.panorama.util.SystemUiHider;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.NumericUtil;
import com.wireless.ui.ChooseModelActivity;
import com.wireless.util.imgFetcher.ImageCache;
import com.wireless.util.imgFetcher.ImageCache.ImageCacheParams;
import com.wireless.util.imgFetcher.ImageFetcher;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class PanoramaActivity extends Activity {
	public static final String TAG = "PanoramaActivity";
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */ 
	private static final int AUTO_HIDE_DELAY_MILLIS = 2000;

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
	
	private AsyncTask<Department, Void, ArrayList<Food>> mRefreshDeptFoodTask;

	/**
	 * 界面组织、安排的结构
	 */
	private LayoutArranger mLayoutArranger;

//	private KitchenHandler mKitchenHandler;

	private ArrayList<Kitchen> mKitchens;

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
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		//底部返回键
		findViewById(R.id.button_panorama_back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		// Set up the user interaction to manually show or hide the system UI.
//		contentView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				if (TOGGLE_ON_CLICK) {
//					mSystemUiHider.toggle();
//				} else {
//					mSystemUiHider.show();
//				}
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
						if(dept.getId() == id){
							mViewPager.setTag(dept);
							bar.setSelectedNavigationItem(i);
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
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
		});
		
////////////////////end viewPager////////////////////
		
		//添加导航
		ActionBar bar = getActionBar();
		for(Department d: depts){
			bar.addTab(bar.newTab().setTag(d).setText(d.getName()).setTabListener(new MyTabListener()));
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
		
		//set current item
		int currentItem = getIntent().getIntExtra(CURRENT_ITEM, -1);
		if(currentItem != -1){
			mViewPager.setCurrentItem(currentItem);
		}
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
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
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
	private void delayedHide(int delayMillis) {
		if(BuildConfig.DEBUG){
			Log.i(TAG,"delayedHide() run");
		}
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.panorama_menu, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_panorama_search:
			break;
		case android.R.id.home:
			onBackPressed();
			break;
		case R.id.menu_panorama_shoppingCart:
			startActivity(new Intent(this, PanoramaSelectedActivity.class));
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public ImageFetcher getImageFetcher() {
		return mImageFetcher;
	}

	public void onClick(View v) {
		if (TOGGLE_ON_CLICK) {
			mSystemUiHider.toggle();
		} else {
			mSystemUiHider.show();
		}
	}
	
	public LayoutArranger getLayoutArranger(){
		return mLayoutArranger;
	}
	
	public void setPositionByKitchen(Kitchen kitchen){
		
	}
	
	class MyTabListener implements TabListener{

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			Object tag = tab.getTag();
			//切换到对应部门的第一个选项
			if(tag != null){
				Department dept = (Department) tag;
				for(int i = 0 ; i < mLayoutArranger.getGroups().size(); i++){
					Department d = mLayoutArranger.getGroup(i).getCaptainFood().getKitchen().getDept();
					if(d.getId() == dept.getId() && ((Department)mViewPager.getTag()) != dept){
						mViewPager.setCurrentItem(i);
						
//						mRefreshDeptFoodTask = new RefreshDeptFoodTask().execute(dept);
						break;
					}
				}
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			
		}
		
	}
	
	class RefreshDeptFoodTask extends AsyncTask<Department, Void, ArrayList<Food>>{

		@Override
		protected ArrayList<Food> doInBackground(Department... params) {
			if(params != null && params.length != 0){
				//TODO
				ArrayList<Food> matchedFoods = new ArrayList<Food>();
				for(Food f:WirelessOrder.foods){
					if(f.getKitchen().getDept() != null && f.getKitchen().getDept().getId() == params[0].getId()){
						matchedFoods.add(f);
					}
				}
				return matchedFoods;
			}
			return null;
		}

		@Override
		protected void onPostExecute(ArrayList<Food> result) {
			super.onPostExecute(result);
			if(result != null && !result.isEmpty()){
				LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout_panorama);
				if(layout != null){
					LayoutInflater inflater = LayoutInflater.from(PanoramaActivity.this);
					layout.removeAllViews();
					LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
					for(Food f:result){
						View view = inflater.inflate(R.layout.panorama_bottom_item, layout);
						view.setLayoutParams(params);
						ImageView image = (ImageView) view.findViewById(R.id.imageView_panorama_bottom_item);
						getImageFetcher().loadImage(f.image, image);
						
						((TextView)view.findViewById(R.id.textView_panorama_bottom_item_name)).setText(f.getName());
						((TextView)view.findViewById(R.id.textView_panorama_bottom_item_price)).setText("￥" + NumericUtil.float2String2(f.getPrice()));
					}
				}
			}
		}
	}
}


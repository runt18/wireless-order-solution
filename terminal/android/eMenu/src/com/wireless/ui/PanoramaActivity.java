package com.wireless.ui;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.panorama.util.ImageArranger;
import com.wireless.panorama.util.SystemUiHider;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
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
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

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

	private ViewPager mPager;
	
	private List<OrderFood> mFoods = new ArrayList<OrderFood>();

	private ImageArranger mImageArranger;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView.animate().translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
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

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
////////////end systemUi //////////////////////////////////

/////////////////load layout////////////////////////
		mImageArranger = new ImageArranger(this, getString(R.string.layout_packageName));
////////////end load layout////////////////////////////
		
////////////imageFetcher and viewPager///////////////////// 
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		final int height = displayMetrics.heightPixels;
		final int width = displayMetrics.widthPixels;
		final int longest = (height > width ? height: width)/2;
		
		ImageCache.ImageCacheParams cacheParams = new ImageCacheParams(this, "panorama");
		cacheParams.setMemCacheSizePercent(this, 0.25f);
		
		mImageFetcher = new ImageFetcher(this, longest);
		mImageFetcher.addImageCache(getFragmentManager(), cacheParams, "panorama");
		mImageFetcher.setImageFadeIn(true);
		
		notifiyDataSetChange(WirelessOrder.foods);
		
		mAdapter  = new FragmentPagerAdapter(getFragmentManager()) {
			
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				Object item = super.instantiateItem(container, position);
				
				return item;
			}

			@Override
			public int getCount() {
				return mImageArranger.getGroups().size();
			}
			
			@Override
			public Fragment getItem(int position) {
				//FIXME 更改传入参数
				return PanoramaItemFragment.newInstance(mImageArranger.getGroups().get(position));
			}
		};
		
		mPager = (ViewPager) findViewById(R.id.viewPager_panorama);
		mPager.setAdapter(mAdapter);
		mPager.setOffscreenPageLimit(2);
		
////////////////////end viewPager////////////////////
		
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
			mPager.setCurrentItem(currentItem);
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
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.panorama_menu, menu);
        menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
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
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void notifiyDataSetChange(Food[] foods){
		List<OrderFood> newList = new ArrayList<OrderFood>();
		for(Food food: foods){
			if(food.image != null){
				newList.add(new OrderFood(food));
			}
		}
		notifyDataSetChange(newList);
	}
	
	public void notifyDataSetChange(List<OrderFood> foods){
		for(OrderFood food : foods){
			if(food.image != null)
				mFoods.add(food);
		}
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
	
	public ImageArranger getImageArranger(){
		return mImageArranger;
	}
	
}

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
import android.content.Context;
import android.content.Intent;
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
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.panorama.util.ImageArranger;
import com.wireless.panorama.util.SystemUiHider;
import com.wireless.protocol.Department;
import com.wireless.protocol.Kitchen;
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

	private ViewPager mViewPager;
	
	/**
	 * 界面组织、安排的结构
	 */
	private ImageArranger mImageArranger;

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
		mImageArranger = new ImageArranger(this, getString(R.string.layout_packageName));
////////////end load layout////////////////////////////

/////////////navigation data///////////////////////////////
//		mKitchenHandler = new KitchenHandler(this);
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

/////////////准备spinner//////////////////////////
//		Spinner deptSpinner = (Spinner) findViewById(R.id.spinner_panorama_dept);
//		Spinner kitchenSpinner = (Spinner) findViewById(R.id.spinner_panorama_kitchen);
		
//		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
//		deptSpinner.setLayoutParams(params);
//		kitchenSpinner.setLayoutParams(params);
//		//准备adapter
//		SpinnerAdapter deptAdapter = new SpinnerListAdapter(this, depts, SpinnerListAdapter.TYPE_DEPT, android.R.layout.simple_dropdown_item_1line);
//		deptSpinner.setAdapter(deptAdapter);
//		
//		//准备listener
//		deptSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
//
//			@Override
//			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//				if(view != null){
//					Department dept = (Department) view.getTag();
//					if(dept != null)
//						mKitchenHandler.sendEmptyMessage(dept.getId());
//				}
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> parent) {
//				
//			}
//		});
//		
//		kitchenSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
//
//			@Override
//			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//				//当菜品选择的时候，切换到对应的页面
//				//FIXME 有些选择无法切换，可能是数据源的问题
//				Kitchen kitchen = (Kitchen) view.getTag();
//				
//				ArrayList<Pager> groups = mImageArranger.getGroups();
//				for (int i = 0; i < groups.size(); i++) {
//					Pager p = groups.get(i);
//					if(p.getCaptainFood() != null && p.getCaptainFood().getKitchen().equals(kitchen)){
//						mViewPager.setCurrentItem(i, true);
//					}
//				}
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> parent) {
//				
//			}
//		});
		
////////////////////end spinner////////////////////////////

		
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
				return mImageArranger.getGroups().size();
			}
			
			@Override
			public Fragment getItem(int position) {
				return PanoramaItemFragment.newInstance(mImageArranger.getGroups().get(position));
			}
		};
		
		mViewPager = (ViewPager) findViewById(R.id.viewPager_panorama);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setTag(depts.get(0));
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				int id = mImageArranger.getGroup(position).getCaptainFood().getKitchen().getDept().getId();
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
//				delayedHide(300);
			}
		});
		
////////////////////end viewPager////////////////////
		
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
	
	public ImageArranger getImageArranger(){
		return mImageArranger;
	}
	
	public void setPositionByKitchen(Kitchen kitchen){
		
	}
	/*
	 * 分厨显示的handler
	 */
//	private static class KitchenHandler extends Handler{
//		private WeakReference<PanoramaActivity> mActivity;
//		
//		public KitchenHandler(PanoramaActivity act) {
//			mActivity = new WeakReference<PanoramaActivity>(act);
//		}
//
//		@Override
//		public void handleMessage(Message msg) {
//			PanoramaActivity act = mActivity.get();
//			//根据部门号重新设置厨房
//			ArrayList<Kitchen> curKitchens = new ArrayList<Kitchen>();
//			for(Kitchen k : act.mKitchens){
//				if(k.getDept().getId() == msg.what){
//					curKitchens.add(k);
//				}
//					
//			}
//			
////			Spinner kitchenSpinner = (Spinner) act.findViewById(R.id.spinner_panorama_kitchen);
////			SpinnerAdapter kcAdapter = new SpinnerListAdapter(act, curKitchens, SpinnerListAdapter.TYPE_KITCHEN, android.R.layout.simple_dropdown_item_1line);
////			kitchenSpinner.setAdapter(kcAdapter);
//		}
//	}
	
	class MyTabListener implements TabListener{

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			Object tag = tab.getTag();
			//切换到对应部门的第一个选项
			if(tag != null){
				Department dept = (Department) tag;
				for(int i = 0 ; i < mImageArranger.getGroups().size(); i++){
					Department d = mImageArranger.getGroup(i).getCaptainFood().getKitchen().getDept();
					if(d.getId() == dept.getId() && ((Department)mViewPager.getTag()) != dept){
						mViewPager.setCurrentItem(i);
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
}

class SpinnerListAdapter extends BaseAdapter{

	public static final int TYPE_DEPT = 1;
	public static final int TYPE_KITCHEN = 2;
	private List<?> mDatas;
	private int mType;
	private LayoutInflater mInflater;
	private int mResource;

	
	public SpinnerListAdapter(Context context, List<?> datas, int type,int Resource) {
		super();
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mDatas = datas;
		mType = type;
		mResource = Resource;
	}

	@Override
	public int getCount() {
		return mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, mResource);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, mResource);
	}
	
	private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
        View view;
        TextView text;

        if (convertView == null) {
            view = mInflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        try {
                //  If no custom field is assigned, assume the whole resource is a TextView
            text = (TextView) view;
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }

		switch(mType){
		case TYPE_DEPT:
			Department data = (Department) mDatas.get(position);
			text.setText(data.name);
			text.setTag(data);
			break;
		case TYPE_KITCHEN:
			Kitchen dataK = (Kitchen) mDatas.get(position);
			text.setText(dataK.getName());
			text.setTag(dataK);
			break;
		}
		
		return view;
	}

}

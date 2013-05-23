package com.wireless.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.ordermenu.R;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.util.imgFetcher.ImageFetcher;

/**
 * this {@link Handler} will display search results in a list. <br/>
 * you need to give this handler a search {@link EditText} and clear {@link Button}.<br/>
 * then it will use a {@link TextWatcher} to watcher the {@link EditText}'s change and display what it
 * found 
 * @author ggdsn1
 *
 */
public class SearchFoodHandler extends Handler{
	private List<Food> mSrcFoods;
	private String mFilterCond;
	protected ImageFetcher mFetcherForSearch;
	private EditText mSearchEditText;
	private Button mClearBtn;
	private OnSearchItemClickListener mOnSearchItemClickListener;
	private ListPopupWindow mPopup;
	private Context mContext;
	private OnFoodAddListener mOnFoodAddListener;
	
	private int mInitHeight = 0;

	private static final String ITEM_NAME = "item_name";
	private static final String ITEM_PRICE = "item_price";
	
	private static final int[] ITEM_ID = {
		R.id.textView_main_search_list_item_name,
		R.id.textView_main_search_list_item_price
	};
	
	private static final String[] ITEM_TAG = {
		ITEM_NAME,
		ITEM_PRICE
	};
	private static final String ITEM_THE_FOOD = "item_the_food";
	
	public SearchFoodHandler(Fragment fgm, EditText searchEditText, Button clearBtn) {
		init(fgm.getActivity(), searchEditText, clearBtn);
	}
	
	public SearchFoodHandler(Activity act, EditText searchEditText, Button clearBtn) {
		init(act, searchEditText, clearBtn);
	}
		
	private void init(Context context, EditText searchEditText, Button clearBtn) {
		mContext = context;
		mSrcFoods = WirelessOrder.foodMenu.foods;
		mFetcherForSearch = new ImageFetcher(context, 50);
		mSearchEditText = searchEditText;
		mClearBtn = clearBtn;
		
		mPopup = new ListPopupWindow(mContext);
		mPopup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NEEDED);
        
		//设置弹出框背景
		mPopup.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.main_search_list_bg));
		mPopup.setAnchorView(searchEditText);
		//清除输入按钮
		mClearBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSearchEditText.setText(""); 
				
				//隐藏键盘
				InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
				mPopup.dismiss();
			}
		});
		
		//侦听弹出框点击项
		mPopup.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Food food = (Food) view.getTag();
				//清空edittext数据
				mClearBtn.performClick();
				//若有图片则跳转到相应的大图
				if(!food.isSellOut()){
					if(mOnSearchItemClickListener != null){
						mOnSearchItemClickListener.onSearchItemClick(food);
					}
				} else {
					Toast.makeText(mContext, "此菜售罄，暂无图片", Toast.LENGTH_SHORT).show();
				
				}
				//隐藏键盘
				InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
				mPopup.dismiss();
			}
		});
		
		final SearchRunnable searchRun = new SearchRunnable(this);
		
		mSearchEditText.addTextChangedListener(new TextWatcher() 
		{
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				String mFilterCond = s.length() == 0 ? "" : s.toString().trim();
				mSearchEditText.removeCallbacks(searchRun);
				//延迟0毫秒显示结果
				if(!mFilterCond.equals("")){
					searchRun.setmFilterCond(mFilterCond);
					mSearchEditText.postDelayed(searchRun, 0);
				}
			}
		});
		
	}
	
	public void setmFilterCond(String mFilterCond) {
		this.mFilterCond = mFilterCond;
	}
	
	public void refreshSrcFoods(List<Food> srcFoods){
		mSrcFoods = srcFoods;
	}
	
	@Override
	public void handleMessage(Message msg){
		
		//将所有菜品进行条件筛选后存入adapter
		List<Food> tmpFoods;
		if(mFilterCond.length() != 0){
			tmpFoods = new ArrayList<Food>(mSrcFoods);
			Iterator<Food> iter = tmpFoods.iterator();
			while(iter.hasNext()){
				Food f = iter.next();
				String filerCond = mFilterCond.toLowerCase(Locale.getDefault());
				if(!(f.getName().toLowerCase(Locale.getDefault()).contains(filerCond) || 
				     f.getPinyin().contains(filerCond) || 
				     f.getPinyinShortcut().contains(filerCond))){
					iter.remove();
				}
				
				//Sort the food by sales amount after filtering.
				Collections.sort(tmpFoods, Food.BY_SALES);
			}				
		}else{
			tmpFoods = mSrcFoods;
		}
//		
		final List<Map<String,Object>> foodMaps = new ArrayList<Map<String,Object>>();
		for(Food f : tmpFoods){
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(ITEM_NAME, f.getName());
			map.put(ITEM_PRICE, NumericUtil.float2String2(f.getPrice()));
			map.put(ITEM_THE_FOOD, f);
			foodMaps.add(map);
		}
		
		// the adapter which is use to display results
		SimpleAdapter adapter = new SimpleAdapter(mContext, foodMaps, R.layout.main_search_list_item, ITEM_TAG, ITEM_ID){

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				Map<String, Object> map = foodMaps.get(position);
				Food food = (Food) map.get(ITEM_THE_FOOD);
				view.setTag(food);
				
				//售罄提示
				View sellOutHint = view.findViewById(R.id.imageView_main_list_item_selloutSignal);
				Button addBtn = (Button) view.findViewById(R.id.button_main_search_list_item_add);

				if(food.isSellOut()){
					sellOutHint.setVisibility(View.VISIBLE);
					addBtn.setVisibility(View.INVISIBLE);
				} else {
					//如果不是售罄，则添加点菜按钮侦听
					addBtn.setVisibility(View.VISIBLE);
					sellOutHint.setVisibility(View.INVISIBLE); 
					addBtn.setTag(food);
					addBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							Food food = (Food) v.getTag();
							
							if(food.isSellOut()){
								Toast toast = Toast.makeText(mContext, "此菜已售罄", Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.TOP|Gravity.RIGHT, 0, 100);
								toast.show();
							} else {
								try {
									OrderFood orderFood = new OrderFood(food);
									orderFood.setCount(1f);
									ShoppingCart.instance().addFood(orderFood);
									if(mOnFoodAddListener != null)
										mOnFoodAddListener.onFoodAdd(food);
									//显示添加提示
									Toast toast = Toast.makeText(mContext, food.getName() + " 已添加", Toast.LENGTH_SHORT);
									toast.setGravity(Gravity.TOP|Gravity.RIGHT, 0, 100);
									toast.show();
									
									mPopup.dismiss();
									mSearchEditText.setText("");
									//隐藏键盘
									InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
									
								} catch (BusinessException e) {
									e.printStackTrace();
								}
							}
						}
					});
				}
				//显示图片
				ImageView foodImage = (ImageView) view.findViewById(R.id.imageView_main_search_list_item);
				if(food.hasImage())
				{
					mFetcherForSearch.loadImage(food.getImage(), foodImage);
				} else foodImage.setImageResource(R.drawable.null_pic_small);
				

				return view;
			}
			
		};
		mPopup.setAdapter(adapter);
		if(mInitHeight > 0)
			mPopup.setHeight(mInitHeight);
		
		//显示列表
		mPopup.show();
		
		mPopup.getListView().post(new Runnable(){
			@Override
			public void run() {
				if(mPopup.getListView().getHeight() > 0 && mInitHeight <= 0){
					mInitHeight = mPopup.getListView().getHeight();
				}
			}
		});
		
		mPopup.getListView().setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//隐藏键盘
				InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
				mPopup.setHeight(LayoutParams.WRAP_CONTENT);
                mPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
                mPopup.show();
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});
	}
	
	public interface OnSearchItemClickListener{
		void onSearchItemClick(Food food);
	}
	public void setOnSearchItemClickListener(OnSearchItemClickListener l)
	{
		mOnSearchItemClickListener = l;
	}
	public interface OnFoodAddListener{
		void onFoodAdd(Food food);
	}
	public void setOnFoodAddListener(OnFoodAddListener l){
		mOnFoodAddListener = l;
	}
}


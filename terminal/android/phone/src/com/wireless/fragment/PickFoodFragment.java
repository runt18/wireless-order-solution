package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.Food;
import com.wireless.ui.R;
import com.wireless.ui.dialog.AskOrderAmountDialog;
import com.wireless.ui.dialog.AskOrderAmountDialog.OnFoodPickedListener;
import com.wireless.util.NumericUtil;

public class PickFoodFragment extends Fragment{
	private static final int REFRESH_FOODS = 43552;
	
	public static final String PICK_FOOD_FRAGMENT_TAG_NAME = "pickFoodFragmentTagName";
	public static final String PICK_FOOD_FRAGMENT_TAG = "pickFoodFragmentTag";
	public static final int PICK_FOOD_FRAGMENT_NUMBER = 87514;
	public static final int PICK_FOOD_FRAGMENT_SPELL = 87515;

	private FoodAdapter mAdapter;
	private FoodHandler mHandler ;
	private GridView mGridView;

	private String mFilterCond = "";
	
	private OnFoodPickedListener mFoodPickedListener;

	/**
	 * 设置点完某个菜品后的回调函数
	 * @param foodPickedListener
	 */
	public void setFoodPickedListener(OnFoodPickedListener foodPickedListener){
		mFoodPickedListener = foodPickedListener;
	}
	
	private static class FoodHandler extends Handler{
		private WeakReference<PickFoodFragment> mFragment;
		private List<Food> mSrcFoods;

		FoodHandler(PickFoodFragment fragment) {
			this.mFragment = new WeakReference<PickFoodFragment>(fragment);
			
			mSrcFoods = WirelessOrder.foodMenu.foods;
		}
		
		@Override
		public void handleMessage(Message msg){
			PickFoodFragment fragment = mFragment.get();
			//将所有菜品进行条件筛选后存入adapter
			
			List<Food> tmpFoods;
			if(fragment.mFilterCond.length() != 0){
				tmpFoods = new ArrayList<Food>(mSrcFoods);
				Iterator<Food> iter = tmpFoods.iterator();
				while(iter.hasNext()){
					Food f = iter.next();
					String filerCond = fragment.mFilterCond.toLowerCase();
					if(!(f.getName().toLowerCase().contains(filerCond) || 
					   f.getPinyin().contains(filerCond) || 
					   f.getPinyinShortcut().contains(filerCond) ||
					   String.valueOf(f.getAliasId()).startsWith(filerCond))){
						iter.remove();
					}				
				}	
				
				/**
				 * Sort the food by order count after filtering
				 */
				Collections.sort(tmpFoods, new Comparator<Food>(){
					public int compare(Food lhs, Food rhs) {
						if(lhs.statistics.orderCnt > rhs.statistics.orderCnt){
							return 1;
						}else if(lhs.statistics.orderCnt < rhs.statistics.orderCnt){
							return -1;
						}else{
							return 0;
						}
					}				
				});
				
			}else{
				tmpFoods = mSrcFoods;
			}
			
			fragment.mAdapter = fragment.new FoodAdapter(tmpFoods);
			fragment.mGridView.setAdapter(fragment.mAdapter);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mHandler = new FoodHandler(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pick_food_fragment, container, false);
		Bundle args = getArguments();
		
		//搜索框
        final EditText searchTxtView = (EditText)view.findViewById(R.id.editText_pickFoodFragment);
        searchTxtView.setHint(args.get(PICK_FOOD_FRAGMENT_TAG_NAME).toString());
//        searchTxtView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				searchTxtView.selectAll();
//			}
//		});
        
        mGridView = (GridView) view.findViewById(R.id.gridView_numberFragment);
        //设置点菜侦听
        mGridView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Food food = (Food) view.getTag();
				if(!food.isSellOut()){
					((TextView)view.findViewById(R.id.textView_sellout_pickFoodFgm_item)).setVisibility(View.GONE);
					new AskOrderAmountDialog(getActivity(), food, mFoodPickedListener, searchTxtView).show();
				}else{
					((TextView)view.findViewById(R.id.textView_sellout_pickFoodFgm_item)).setVisibility(View.VISIBLE);
					Toast.makeText(getActivity(), food.getName() + "已售罄", Toast.LENGTH_SHORT).show();
				}
			}
        });

        //设置输入类型
        if(args.getInt(PICK_FOOD_FRAGMENT_TAG) == PICK_FOOD_FRAGMENT_NUMBER)
        	searchTxtView.setInputType(InputType.TYPE_CLASS_NUMBER);
        else searchTxtView.setInputType(InputType.TYPE_CLASS_TEXT);
        
        searchTxtView.addTextChangedListener(new TextWatcher(){
        	
        	Runnable mSrchHandler = new Runnable(){
        		@Override
        		public void run(){
        			mHandler.sendEmptyMessage(REFRESH_FOODS);
        		}
        	};
        	
			@Override 
			public void afterTextChanged(Editable s) {}
			
			@Override
			public void beforeTextChanged(CharSequence s,int start, int count, int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().trim().length() != 0){
					mFilterCond  = s.toString().trim();
					
					searchTxtView.removeCallbacks(mSrchHandler);
					
					//如果搜索编号，马上执行搜索，
					//否则延迟500ms执行搜索
				    if(Pattern.compile("[0-9]*").matcher(mFilterCond).matches()){;   
				    	searchTxtView.postDelayed(mSrchHandler, 500);				    
				    }else{
						mHandler.sendEmptyMessage(REFRESH_FOODS);
				    }
				}else{
					mFilterCond = "";
					mHandler.sendEmptyMessage(REFRESH_FOODS);
				}
			}
		});
        
		//删除搜索条件按钮
		((ImageButton) view.findViewById(R.id.imageButton_delete_numberFragment)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				searchTxtView.setText("");
			}
		});
		
		/**
		 * 菜品List滚动时隐藏软键盘
		 */
		mGridView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(searchTxtView.getWindowToken(), 0);
//				mGridView.requestFocus();
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		
		//刷新菜品
		mHandler.sendEmptyMessage(REFRESH_FOODS);
        return view;
	}
	
	//关闭时清除侦听
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mGridView.setOnScrollListener(null);
	}

	//菜品显示的adapter
	private class FoodAdapter extends BaseAdapter{

		private List<Food> mFoods;

		FoodAdapter(List<Food> foods)
		{
			mFoods = foods;
		}
		@Override
		public int getCount() {
			return mFoods.size();
		}

		@Override
		public Object getItem(int position) {
			if(position < mFoods.size() && position >= 0){
				return mFoods.get(position);
			}else{
				return null;
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view ;
			if(convertView == null){
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pick_food_fragment_item, null);
			}else{
				view = convertView;
			}
			
			Food food = mFoods.get(position);
			view.setTag(food);
			//如果字数太长则从10截断
			if(food.getName().length() >= 10){
				((TextView) view.findViewById(R.id.textView_foodName_pickFoodFragment_item)).setText(food.getName().substring(0, 10));
			}else{
				((TextView) view.findViewById(R.id.textView_foodName_pickFoodFragment_item)).setText(food.getName());
			}

			((TextView) view.findViewById(R.id.textView_num_pickFoodFragment_item)).setText(Integer.toString(food.getAliasId()));
			((TextView) view.findViewById(R.id.textView_price_pickFoodFragment_item)).setText(NumericUtil.float2String2(food.getPrice()));
			
			//设置售罄的显示
			if(food.isSellOut()){
				((TextView)view.findViewById(R.id.textView_sellout_pickFoodFgm_item)).setVisibility(View.VISIBLE);
			}else{
				((TextView)view.findViewById(R.id.textView_sellout_pickFoodFgm_item)).setVisibility(View.INVISIBLE);
			}
			
//			LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.linearLayout_pickFood_fgm_item);
//			linearLayout.removeAllViews();
//			XmlResourceParser xrp = getResources().getXml(R.color.my_color);  
//			try {  
//			    ColorStateList csl = ColorStateList.createFromXml(getResources(), xrp);  
//			    tv.setTextColor(csl);  
//			} catch (Exception e) {  
//			}  
//			//赠
//			if(food.isGift()){
//				TextView text = new TextView(getActivity());
//				text.setText("赠");
//				text.setTextSize(16f);
//				text.setTextColor(Color.YELLOW);
//				linearLayout.addView(text);
//			}
//			//时
//			if(food.isCurPrice()){
//				TextView text = new TextView(getActivity());
//				text.setText("时");
//				text.setTextSize(16f);
//				text.setTextColor(Color.MAGENTA);
//				linearLayout.addView(text);
//			}
//			//推荐
//			if(food.isRecommend()){
//				TextView text = new TextView(getActivity());
//				text.setText("荐");
//				text.setTextSize(16f);
//				text.setTextColor(Color.CYAN);
//				linearLayout.addView(text);
//			}
//			//特
//			if(food.isSpecial()){
//				TextView text = new TextView(getActivity());
//				text.setText("特");
//				text.setTextSize(16f);
//				text.setTextColor(Color.GREEN);
//				linearLayout.addView(text);
//			}
//			//套
//			if(food.isCombo()){
//				TextView text = new TextView(getActivity());
//				text.setText("套");
//				text.setTextSize(16f);
//				text.setTextColor(Color.GREEN);
//				linearLayout.addView(text);
//			}
			 
			return view;
		}
	}
}

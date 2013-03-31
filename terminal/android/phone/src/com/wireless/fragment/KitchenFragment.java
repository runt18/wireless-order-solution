package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenuEx.UnmodifiableList;
import com.wireless.protocol.PDepartment;
import com.wireless.protocol.PKitchen;
import com.wireless.ui.R;
import com.wireless.ui.dialog.AskOrderAmountDialog;
import com.wireless.ui.dialog.AskOrderAmountDialog.OnFoodPickedListener;
import com.wireless.util.NumericUtil;

public class KitchenFragment extends Fragment {
	private static final int REFRESH_DEPTS = 112309;
	private static final int REFRESH_FOODS = 112310;

	private DepartmentHandler mDepartmentHandler;
	private KitchenHandler mKitchenHandler;
	
	private List<PKitchen> mValidKitchens;
	private ArrayList<PDepartment> mValidDepts;
	//private ArrayList<List<Food>> mPackedValidFoodsList;
	
	private short mDeptFilter = Short.MIN_VALUE;
	
	private ExpandableListView mXpListView;
	//private Food[] mOriFoods;
	
	private OnFoodPickedListener mFoodPickedListener;

//	public static interface OnFoodPickedListener{
//		/**
//		 * 当PickFoodListView选中菜品后，回调此函数通知Activity选中的Food信息
//		 * @param food 选中Food的信息
//		 */
//		public void onPicked(OrderFood food);
//		
//		/**
//		 * 当PickFoodListView选中菜品后，回调此函数通知Activity选中的Food信息，并跳转到口味Activity
//		 * @param food
//		 * 			选中Food的信息
//		 */
//		public void onPickedWithTaste(OrderFood food);
//	}
	/**
	 * 设置点完某个菜品后的回调函数
	 * @param foodPickedListener
	 */
	public void setFoodPickedListener(OnFoodPickedListener foodPickedListener){
		mFoodPickedListener = foodPickedListener;
	}
	
	private static class DepartmentHandler extends Handler{
		private WeakReference<KitchenFragment> mFragment;
		private LinearLayout mDeptLayout;

		DepartmentHandler(KitchenFragment fragment) {
			this.mFragment = new WeakReference<KitchenFragment>(fragment);
		}
		
		@Override
		public void handleMessage(Message msg)
		{
			final KitchenFragment fragment = mFragment.get();
			
			if(mDeptLayout == null)
				mDeptLayout = (LinearLayout)fragment.getView().findViewById(R.id.linearLayout_kitchenFragment);
			//添加所有部门
			mDeptLayout.removeAllViews();
			for(int i = 0; i < fragment.mValidDepts.size(); i++)
			{
				//解析跟图层
				RelativeLayout view = (RelativeLayout) LayoutInflater.from(fragment.getActivity()).inflate(R.layout.kitchen_fragment_dept_item, null);
				//解析子图层并设置颜色
				final RelativeLayout childView = (RelativeLayout) view.findViewById(R.id.relativeLayout_child_kcFgm);
				childView.setBackgroundResource(R.color.orange);
				//设置该项名称
				((TextView)view.findViewById(R.id.textView_kitchenFragment_dept_item)).setText(fragment.mValidDepts.get(i).getName());
				view.setTag(fragment.mValidDepts.get(i));
				//设置该项侦听器
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						//刷新厨房显示
						PDepartment dept = (PDepartment) v.getTag();
						fragment.mDeptFilter = dept.getId();
						fragment.mKitchenHandler.sendEmptyMessage(REFRESH_FOODS);
						//将前一项的外观设置为弹起状态
						if(mDeptLayout.getTag() != null)
						{
							((View)(mDeptLayout.getTag())).setBackgroundResource(R.color.orange);
						}
						mDeptLayout.setTag(childView);
						//设置该项的点击状态
						childView.setBackgroundResource(R.color.gold);
						//若关闭按钮显示，则取消显示
						ImageButton collapseBtn = (ImageButton) fragment.getView().findViewById(R.id.imageButton_collaps_kitchenFgm);
						if(collapseBtn.isShown())
							collapseBtn.setVisibility(View.GONE);
					}
				});
				mDeptLayout.addView(view);
				//设置第一项的外观
				if(i==0)
				{
					mDeptLayout.setTag(childView);
					childView.setBackgroundResource(R.color.gold);
				}
			}
		}
	}

	private static class KitchenHandler extends Handler{
		private WeakReference<KitchenFragment> mFragment;

		KitchenHandler(KitchenFragment fragment) {
			this.mFragment = new WeakReference<KitchenFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			KitchenFragment fragment = mFragment.get();

			HashMap<PKitchen, List<Food>> foodsByKitchen = new HashMap<PKitchen, List<Food>>();

			//根据条件筛选出要显示的厨房
			ArrayList<PKitchen> kitchens = new ArrayList<PKitchen>();
			for(PKitchen k : fragment.mValidKitchens){
				if(k.getDept().getId() == fragment.mDeptFilter){
					kitchens.add(k);
					foodsByKitchen.put(k, new ArrayList<Food>());
				}
			}
			
			//筛选出这些厨房中包含的菜品，并按销量排序
			for(Food f : WirelessOrder.foodMenu.foods){
				List<Food> foodsToEachKitchen = foodsByKitchen.get(f.getKitchen());
				if(foodsToEachKitchen != null){
					foodsToEachKitchen.add(f);
				}
			}
			
			//将每个厨房包含的菜品按销量排序
			for(List<Food> foodsToEachKitchen : foodsByKitchen.values()){
				Collections.sort(foodsToEachKitchen, new Comparator<Food>(){
					@Override
					public int compare(Food lhs, Food rhs) {
						if(lhs.statistics.orderCnt > rhs.statistics.orderCnt){
							return -1;
						}else if(lhs.statistics.orderCnt < rhs.statistics.orderCnt){
							return 1;
						}else{
							return 0;
						}
					}
				});
			}
			
			fragment.mXpListView.setAdapter(fragment.new KitchenExpandableListAdapter(foodsByKitchen.entrySet()));
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Comparator<Food> foodCompByKitchen = new Comparator<Food>(){
			@Override
			public int compare(Food food1, Food food2) {
				if (food1.getKitchen().getAliasId() > food2.getKitchen().getAliasId()) {
					return 1;
				} else if (food1.getKitchen().getAliasId() < food2.getKitchen().getAliasId()) {
					return -1;
				} else {
					return 0;
				}
			}
		};
		
		/*
		 * 将所有菜品进行按厨房编号进行排序
		 */
		UnmodifiableList<Food> foodsByKitchen = new UnmodifiableList<Food>(WirelessOrder.foodMenu.foods, foodCompByKitchen);

		
		/*
		 * 使用二分查找算法筛选出有菜品的厨房
		 */
		mValidKitchens = new ArrayList<PKitchen>();
		for(PKitchen k : WirelessOrder.foodMenu.kitchens){
			Food keyFood = new Food();
			keyFood.setKitchen(k);
			if(foodsByKitchen.find(keyFood) != null){
				mValidKitchens.add(k);
			}
		}
		
		/*
		 * 筛选出有菜品的部门
		 */
		mValidDepts = new ArrayList<PDepartment>();
		for(PDepartment d : WirelessOrder.foodMenu.depts){
			for(PKitchen k : mValidKitchens){
				if(k.getDept().equals(d)){
					mValidDepts.add(d);
					break;
				}
			}
		}
		
		mDeptFilter = mValidKitchens.get(0).getDept().getId();
		
		mDepartmentHandler = new DepartmentHandler(this);
		mKitchenHandler = new KitchenHandler(this);
	}
 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.kitchen_fragment, container, false);
		
		mXpListView = (ExpandableListView) view.findViewById(R.id.expandableListView_kitchenFragment);
		//关闭组按钮
		final ImageButton collapseBtn = (ImageButton) view.findViewById(R.id.imageButton_collaps_kitchenFgm);
		collapseBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//关闭当前组
				int groupPosition  = (Integer) collapseBtn.getTag();
				mXpListView.collapseGroup(groupPosition);
				mXpListView.smoothScrollToPosition(0);
			}
		});
		
		//设置group展开侦听器，每次只打开一项
		mXpListView.setOnGroupExpandListener(new OnGroupExpandListener() {
				@Override
				public void onGroupExpand( int groupPosition) {
					//关闭其它组
					int groupCount = mXpListView.getExpandableListAdapter().getGroupCount();
					for (int i = 0; i < groupCount; i++) {
						if (groupPosition != i) {
							mXpListView.collapseGroup(i);
						}
					}
					
					//显示关闭组按钮
					collapseBtn.setVisibility(View.VISIBLE);
					collapseBtn.setTag(groupPosition);
				}
			});
		
		mXpListView.setOnGroupCollapseListener(new OnGroupCollapseListener(){
			@Override
			public void onGroupCollapse(int groupPosition) {
				//组关闭时按钮消失
				collapseBtn.setVisibility(View.GONE);
			}
		});
		
		mDepartmentHandler.sendEmptyMessage(REFRESH_DEPTS);
		mKitchenHandler.sendEmptyMessage(REFRESH_FOODS);
		return view;
	}
	
	
	private class KitchenExpandableListAdapter extends BaseExpandableListAdapter{
		
		//每行显示的菜品数量
		private final int mEachRowAmount = 3;
		//数据源，保存了每个厨房持有的菜品
		private final List<Entry<PKitchen, List<Food>>> mFoodsByKitchen;
		
		KitchenExpandableListAdapter(Collection<Entry<PKitchen, List<Food>>> foodsByKitchen){
			
			this.mFoodsByKitchen = new ArrayList<Entry<PKitchen, List<Food>>>(foodsByKitchen);
			
			Collections.sort(this.mFoodsByKitchen, new Comparator<Entry<PKitchen, List<Food>>>(){

				@Override
				public int compare(Entry<PKitchen, List<Food>> lhs,	Entry<PKitchen, List<Food>> rhs) {
					if(lhs.getKey().getAliasId() > rhs.getKey().getAliasId()){
						return 1;
					}else if(lhs.getKey().getAliasId() < rhs.getKey().getAliasId()){
						return -1;
					}else{
						return 0;
					}
				}
				
			});
		}

		@Override
		public int getGroupCount() {
			return mFoodsByKitchen.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			int foodAmountToKitchen = mFoodsByKitchen.get(groupPosition).getValue().size();
			return foodAmountToKitchen / mEachRowAmount + (foodAmountToKitchen % mEachRowAmount == 0 ? 0 : 1);
					
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mFoodsByKitchen.get(groupPosition).getKey();
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			List<Food> foodsToKitchen = mFoodsByKitchen.get(groupPosition).getValue();
			int start = childPosition * mEachRowAmount;
			int end = start + mEachRowAmount;
			return foodsToKitchen.subList(start, end > foodsToKitchen.size() ? foodsToKitchen.size() : end);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			View view;
			if(convertView != null){
				view = convertView;
			}else{
				view = View.inflate(getActivity(), R.layout.kitchen_fragment_xplistview_group_item, null);
			}
			
			view.setBackgroundResource(R.drawable.kitchen_fragment_group_selector);
			
			//设置厨房名
			((TextView) view.findViewById(R.id.textView_name_kitchenFragment_xp_group_item))
				.setText(mFoodsByKitchen.get(groupPosition).getKey().getName());
			//设置厨房持有菜品数量
			((TextView) view.findViewById(R.id.textView_count_kitchenFragment_xp_group_item))
				.setText(Integer.toString(mFoodsByKitchen.get(groupPosition).getValue().size()));
			
			return view;
		}

		@Override
		public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild, 
								 final View convertView, final ViewGroup parent) {
			View view;
			if(convertView != null){
				view = convertView;
			}else{
				view = View.inflate(getActivity(), R.layout.kitchen_fragment_xp_listview_child_item, null);
			}
			
			//设置该行的GridView
			GridView gridView = (GridView) view.findViewById(R.id.gridView_kitchenFgm_xplv_child_item);
			gridView.setVerticalSpacing(0);
			
			List<Food> foodsToKitchen = mFoodsByKitchen.get(groupPosition).getValue();
			int start = childPosition * mEachRowAmount;
			int end = start + mEachRowAmount;
			gridView.setAdapter(new GridAdapter(foodsToKitchen.subList(start, end > foodsToKitchen.size() ? foodsToKitchen.size() : end)));
			
			//设置侦听器
			gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick( AdapterView<?> parent, View view, int position, long id) {
					Food food = (Food) view.getTag();
					if(!food.isSellOut()){
						((TextView)view.findViewById(R.id.textView_sellout_pickFoodFgm_item)).setVisibility(View.GONE);

						new AskOrderAmountDialog(getActivity(), food, mFoodPickedListener, null).show();
					}else{
						((TextView)view.findViewById(R.id.textView_sellout_pickFoodFgm_item)).setVisibility(View.VISIBLE);
						
						Toast.makeText(getActivity(), food.getName() + "已售罄", Toast.LENGTH_SHORT).show();
					}
				}
			});
			return view;
		}

		@Override
		public boolean isChildSelectable(int groupPosition,	int childPosition) {
			return false;
		}
	}

	
	private class GridAdapter extends BaseAdapter{
		private final List<Food> mFoods;
		
		public GridAdapter(List<Food> mFoods) {
			this.mFoods = mFoods;
		}

		@Override
		public int getCount() {
			return mFoods.size();
		}

		@Override
		public Object getItem(int position) {
			return mFoods.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view;
			if(convertView == null){
				view = View.inflate(getActivity(), R.layout.pick_food_fragment_item, null);
			}else{
				view = convertView;
			}
			
			Food food = mFoods.get(position);
			
			view.setTag(food);
			
			if(food.getName().length() >= 10){
				((TextView) view.findViewById(R.id.textView_foodName_pickFoodFragment_item)).setText(food.getName().substring(0, 10));
			}else{
				((TextView) view.findViewById(R.id.textView_foodName_pickFoodFragment_item)).setText(food.getName());
			}

			//设置该项的显示
			((TextView) view.findViewById(R.id.textView_num_pickFoodFragment_item)).setText(Integer.toString(food.getAliasId()));
			((TextView) view.findViewById(R.id.textView_price_pickFoodFragment_item)).setText(NumericUtil.float2String2(food.getPrice()));
			
			if(food.isSellOut())
				((TextView)view.findViewById(R.id.textView_sellout_pickFoodFgm_item)).setVisibility(View.VISIBLE);
			else {
				((TextView)view.findViewById(R.id.textView_sellout_pickFoodFgm_item)).setVisibility(View.GONE);
			}
			
			return view;
		}
		
	}
}



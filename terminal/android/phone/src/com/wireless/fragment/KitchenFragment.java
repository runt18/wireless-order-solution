package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.ui.R;
import com.wireless.ui.view.PinnedExpandableListView;
import com.wireless.ui.view.PinnedExpandableListView.PinnedExpandableHeaderAdapter;

public class KitchenFragment extends Fragment {
	private static final int REFRESH_DEPTS = 112309;
	private static final int REFRESH_FOODS = 112310;

	private DepartmentHandler mDepartmentHandler;
	private KitchenHandler mKitchenHandler;
	
	private ArrayList<Kitchen> mValidKitchens;
	private ArrayList<Department> mValidDepts;
	private ArrayList<List<Food>> mValidFoods;
	
	private PinnedExpandableListView mXpListView;
	
	private static class DepartmentHandler extends Handler{
		private WeakReference<KitchenFragment> mFragment;
		private LinearLayout mDeptLayout;

		DepartmentHandler(KitchenFragment fragment) {
			this.mFragment = new WeakReference<KitchenFragment>(fragment);
		}
		
		@Override
		public void handleMessage(Message msg)
		{
			KitchenFragment fragment = mFragment.get();
			
			if(mDeptLayout == null)
				mDeptLayout = (LinearLayout)fragment.getView().findViewById(R.id.linearLayout_kitchenFragment);
			//添加所有部门
			mDeptLayout.removeAllViews();
			for(Department d:fragment.mValidDepts)
			{
				RelativeLayout view = (RelativeLayout) LayoutInflater.from(fragment.getActivity()).inflate(R.layout.kitchen_fragment_dept_item, null);
				((TextView)view.findViewById(R.id.textView_kitchenFragment_dept_item)).setText(d.name);
				view.setTag(d);
				
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						//TODO 刷新厨房显示
//						Department dept = (Department) v.getTag();
					}
				});
				
				mDeptLayout.addView(view);
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

			fragment.mXpListView.setAdapter(fragment.new KitchenExpandableListAdapter());
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/**
		 * 将所有菜品进行按厨房编号进行排序
		 */
		Food[] tmpFoods = new Food[WirelessOrder.foodMenu.foods.length];
		System.arraycopy(WirelessOrder.foodMenu.foods, 0, tmpFoods, 0,
				WirelessOrder.foodMenu.foods.length);
		Arrays.sort(tmpFoods, new Comparator<Food>() {
			@Override
			public int compare(Food food1, Food food2) {
				if (food1.kitchen.aliasID > food2.kitchen.aliasID) {
					return 1;
				} else if (food1.kitchen.aliasID < food2.kitchen.aliasID) {
					return -1;
				} else {
					return 0;
				}
			}
		});

		/**
		 * 使用二分查找算法筛选出有菜品的厨房
		 */
		mValidKitchens = new ArrayList<Kitchen>();
		for (int i = 0; i < WirelessOrder.foodMenu.kitchens.length; i++) {
			Food keyFood = new Food();
			keyFood.kitchen.aliasID = WirelessOrder.foodMenu.kitchens[i].aliasID;
			int index = Arrays.binarySearch(tmpFoods, keyFood,
					new Comparator<Food>() {

						public int compare(Food food1, Food food2) {
							if (food1.kitchen.aliasID > food2.kitchen.aliasID) {
								return 1;
							} else if (food1.kitchen.aliasID < food2.kitchen.aliasID) {
								return -1;
							} else {
								return 0;
							}
						}
					});

			if (index >= 0) {
				mValidKitchens.add(WirelessOrder.foodMenu.kitchens[i]);
			}
		}
		/**
		 * 筛选出有菜品的部门
		 */
		mValidDepts = new ArrayList<Department>();
		for (int i = 0; i < WirelessOrder.foodMenu.depts.length; i++) {
			for (int j = 0; j < mValidKitchens.size(); j++) {
				if (WirelessOrder.foodMenu.depts[i].deptID == mValidKitchens.get(j).dept.deptID) {
					mValidDepts.add(WirelessOrder.foodMenu.depts[i]);
					break;
				}
			}
		}
		
		mValidFoods = new ArrayList<List<Food>>();
		Kitchen lastKitchen = tmpFoods[0].kitchen;
		List<Food> list = new ArrayList<Food>();
		for(int i=0;i<tmpFoods.length;i++)
		{
			if(tmpFoods[i].kitchen.equals(lastKitchen))
			{
				list.add(tmpFoods[i]);
			}
			else{
				mValidFoods.add(list);
				list = new ArrayList<Food>();
				lastKitchen = tmpFoods[i].kitchen;
				list.add(tmpFoods[i]);
			}
			if(i == tmpFoods.length-1)
				mValidFoods.add(list);
		}
		
		mDepartmentHandler = new DepartmentHandler(this);
		mKitchenHandler = new KitchenHandler(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.kitchen_fragment, container, false);
		
		mXpListView = (PinnedExpandableListView) view.findViewById(R.id.expandableListView_kitchenFragment);
		mXpListView.setHeaderView(getActivity().getLayoutInflater().inflate(R.layout.kitchen_fragment_xplistview_group_item_header, mXpListView, false));

		mDepartmentHandler.sendEmptyMessage(REFRESH_DEPTS);
		mKitchenHandler.sendEmptyMessage(REFRESH_FOODS);
		return view;
	}
	
	class KitchenExpandableListAdapter extends BaseExpandableListAdapter implements PinnedExpandableHeaderAdapter{

		@Override
		public int getGroupCount() {
			return mValidKitchens.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return 1;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mValidKitchens.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return mValidFoods.get(groupPosition);
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
			if(convertView != null)
				view = convertView;
			else view = View.inflate(getActivity(), R.layout.kitchen_fragment_xplistview_group_item, null);
			//设置厨房名
			((TextView) view.findViewById(R.id.textView_name_kitchenFragment_xp_group_item))
				.setText(mValidKitchens.get(groupPosition).name);
			//设置厨房菜数量
			((TextView) view.findViewById(R.id.textView_count_kitchenFragment_xp_group_item))
				.setText("" + mValidFoods.get(groupPosition).size());
			
			return view;
		}

		@Override
		public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild,
				final View convertView, final ViewGroup parent) {
			View view;
			if(convertView != null)
				view = convertView;
			else view = View.inflate(getActivity(), R.layout.kitchen_fragment_xp_listview_child_item, null);
			
			//FIXME 无效，修改
//			view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener(){
//				@Override
//				public void onGlobalLayout() {
//					View view = getChildView(groupPosition, childPosition, isLastChild,convertView, parent);
//					LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
//					view.setLayoutParams(lp);
//					
//				}
//			});
			GridView gridView = (GridView) view.findViewById(R.id.gridView_kitchenFgm_xplv_child_each_item);
			gridView.setAdapter(new FoodAdapter(mValidFoods.get(groupPosition)));
			
			return view;
		}

		@Override
		public boolean isChildSelectable(int groupPosition,
				int childPosition) {
			return false;
		}
		
		@Override
		public int getPinnedExpandableHeaderState(int groupPosition, int childPosition) {
			final int childCount = getChildrenCount(groupPosition);
			if(childPosition == childCount - 1){  
				return PINNED_HEADER_PUSHED_UP; 
			}
			else if(childPosition == -1 && !mXpListView.isGroupExpanded(groupPosition)){ 
				return PINNED_HEADER_GONE; 
			}
			else{
				return PINNED_HEADER_VISIBLE;
			}
		}

		@Override
		public void configurePinnedExpandableHeader(View header, int groupPosition,
				int childPosition, int alpha) {
//			Map<String,String> groupData = (Map<String,String>)this.getGroup(groupPosition);
//			((TextView)header.findViewById(R.id.groupto)).setText(groupData.get("g"));
		}
		private HashMap<Integer,Integer> groupStatusMap = new HashMap<Integer, Integer>();

		@Override
		public void setGroupClickStatus(int groupPosition, int status) {
			groupStatusMap.put(groupPosition, status);
		}
		
		@Override
		public int getGroupClickStatus(int groupPosition) {
			if(groupStatusMap.containsKey(groupPosition)){
				return groupStatusMap.get(groupPosition);
			}
			else{
				return 0;
			}
		}
	}
	
	class FoodAdapter extends BaseAdapter{
		List<Food> mFoods;
		
		FoodAdapter(List<Food> foods){
			mFoods = foods;
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
			View view;
			if(convertView != null)
				view = convertView;
			else view = View.inflate(getActivity(), R.layout.kitchen_fragment_xplistview_child_item_item, null);
			
			Food food = mFoods.get(position);
			view.setTag(food);
			
			((TextView) view.findViewById(R.id.textView_foodName_kcFgm_xpLsv_child_item_item)).setText(food.name);
			((TextView) view.findViewById(R.id.textView_foodNum_kcFgm_xpLsv_child_item_item)).setText("" + food.aliasID);
			((TextView) view.findViewById(R.id.textView_price_kcFgm_xpLsv_child_item_item)).setText("" + food.getPrice());
			return view;
		}
	}
}

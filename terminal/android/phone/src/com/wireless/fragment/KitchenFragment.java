package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
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
	
	private short mDeptFilter = Short.MIN_VALUE;
	
	private PinnedExpandableListView mXpListView;
	private Food[] mOriFoods;
	
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
			for(Department d:fragment.mValidDepts)
			{
				RelativeLayout view = (RelativeLayout) LayoutInflater.from(fragment.getActivity()).inflate(R.layout.kitchen_fragment_dept_item, null);
				((TextView)view.findViewById(R.id.textView_kitchenFragment_dept_item)).setText(d.name);
				view.setTag(d);
				
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						//TODO 刷新厨房显示
						Department dept = (Department) v.getTag();
						fragment.mDeptFilter = dept.deptID;
						
						fragment.mKitchenHandler.sendEmptyMessage(REFRESH_FOODS);
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
			//根据条件筛选出要显示的厨房
			ArrayList<Kitchen> kitchens = new ArrayList<Kitchen>();
			for(Kitchen k:fragment.mValidKitchens)
			{
				if(k.dept.deptID == fragment.mDeptFilter){
					kitchens.add(k);
				}
			}
			//筛选出这些厨房中包含的菜品
			ArrayList<Food> foods = new ArrayList<Food>();
			for(Food f:fragment.mOriFoods)
			{
				for(Kitchen k:kitchens)
					if(f.kitchen.aliasID == k.aliasID)
					{
						foods.add(f);
					}
			}
			//将筛选出的菜品打包成List<List<T>>格式
			ArrayList<List<Food>> tidyFoods = new ArrayList<List<Food>>();
			Kitchen lastKitchen = foods.get(0).kitchen;
			List<Food> list = new ArrayList<Food>();
			int size = foods.size();
			for(int i=0;i<size;i++)
			{
				Food f = foods.get(i);
				if(f.kitchen.equals(lastKitchen))
				{
					list.add(f);
				}
				else{
					tidyFoods.add(list);
					list = new ArrayList<Food>();
					lastKitchen = f.kitchen;
					list.add(f);
				}
				if(i == size - 1)
					tidyFoods.add(list);
			}
			
			fragment.mXpListView.setAdapter(fragment.new KitchenExpandableListAdapter(kitchens, tidyFoods, 4));
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/*
		 * 将所有菜品进行按厨房编号进行排序
		 */
		mOriFoods = new Food[WirelessOrder.foodMenu.foods.length];
		System.arraycopy(WirelessOrder.foodMenu.foods, 0, mOriFoods, 0,
				WirelessOrder.foodMenu.foods.length);
		Arrays.sort(mOriFoods, new Comparator<Food>() {
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
		/*
		 * 使用二分查找算法筛选出有菜品的厨房
		 */
		mValidKitchens = new ArrayList<Kitchen>();
		for (int i = 0; i < WirelessOrder.foodMenu.kitchens.length; i++) {
			Food keyFood = new Food();
			keyFood.kitchen.aliasID = WirelessOrder.foodMenu.kitchens[i].aliasID;
			int index = Arrays.binarySearch(mOriFoods, keyFood,
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
		/*
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
		//将筛选出的菜品打包成List<List<T>>格式
		mValidFoods = new ArrayList<List<Food>>();
		Kitchen lastKitchen = mOriFoods[0].kitchen;
		List<Food> list = new ArrayList<Food>();
		for(int i=0;i<mOriFoods.length;i++)
		{
			if(mOriFoods[i].kitchen.equals(lastKitchen))
			{
				list.add(mOriFoods[i]);
			}
			else{
				mValidFoods.add(list);
				list = new ArrayList<Food>();
				lastKitchen = mOriFoods[i].kitchen;
				list.add(mOriFoods[i]);
			}
			if(i == mOriFoods.length-1)
				mValidFoods.add(list);
		}
		
		
		mDeptFilter = mValidKitchens.get(0).dept.deptID;
		
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
		private SparseIntArray mGroupStatusMap ;
		private ArrayList<ArrayList<ArrayList<Food>>> mChilds;
		private ArrayList<Kitchen> mGroups;
		private int mRow = 4;
		
		public KitchenExpandableListAdapter(ArrayList<Kitchen> groups, List<List<Food>> rowChilds, int row) {
			super();
			mGroupStatusMap = new  SparseIntArray();
			mChilds = new ArrayList<ArrayList<ArrayList<Food>>>();
			mGroups = groups;
			mRow = row;
			/*
			 * 将每个分厨中的菜品分成4个一组存入subChild中，再将所有的subChild存入subChilds中，
			 * 最后将该subChilds存入mChilds中
			 */
			for(List<Food> l:rowChilds)
			{
				ArrayList<ArrayList<Food>>  subChilds = new ArrayList<ArrayList<Food>>();
				int i=0;
				ArrayList<Food> subChild = new ArrayList<Food>();
				for(int j=0;j<l.size();j++)
				{
					subChild.add(l.get(j));
					i++;
					if(i == 4 || j == l.size() -1){
						i = 0;
						subChilds.add(subChild);
						subChild = new ArrayList<Food>();
					}
				}
				mChilds.add(subChilds);
			}
		}

		@Override
		public int getGroupCount() {
			return mGroups.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mChilds.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mGroups.get(groupPosition);
		}

		@Override
		public ArrayList<Food> getChild(int groupPosition, int childPosition) {
			return mChilds.get(groupPosition).get(childPosition);
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
				.setText(mGroups.get(groupPosition).name);
			//设置厨房菜数量
			int size = mChilds.get(groupPosition).size();
			((TextView) view.findViewById(R.id.textView_count_kitchenFragment_xp_group_item))
				.setText("" + ((size - 1) * mRow  + mChilds.get(groupPosition).get(size -1).size()));
			
			return view;
		}

		@Override
		public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild,
				final View convertView, final ViewGroup parent) {
			View view;
			if(convertView != null)
				view = convertView;
			else view = View.inflate(getActivity(), R.layout.kitchen_fragment_xp_listview_child_item, null);
			//设置该行的gridview
			LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.linearLayout_kcFgm_xplv_child_child);
			linearLayout.removeAllViews();
			linearLayout.setWeightSum(4);
			ArrayList<Food> childFoods = mChilds.get(groupPosition).get(childPosition);
			
			for(Food k :childFoods)
			{
				View childView = View.inflate(getActivity(), R.layout.kitchen_fragment_xplistview_child_item_item, null);
				
				((TextView) childView.findViewById(R.id.textView_foodName_kitchenFgm_child_item_item)).setText(k.name);
				((TextView) childView.findViewById(R.id.textView_num_kitchenFgm_child_item_item)).setText("" + k.aliasID);
				((TextView) childView.findViewById(R.id.textView_price_kitchenFgm_child_item_item)).setText("" + k.getPrice());
				linearLayout.addView(childView);
			}
			
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
		/**
		 * 设置header的显示内容
		 */
		@Override
		public void configurePinnedExpandableHeader(View header, int groupPosition, int childPosition, int alpha) {
			Kitchen kitchen = (Kitchen) this.getGroup(groupPosition);
			((TextView)header.findViewById(R.id.textView_name_kitchenFragment_xp_group_header)).setText(kitchen.name);
			
			//设置厨房菜数量
			int size = mChilds.get(groupPosition).size();
			((TextView) header.findViewById(R.id.textView_count_kitchenFragment_xp_group_header))
				.setText("" + ((size - 1) * mRow + mChilds.get(groupPosition).get(size -1).size()));
			
		}
		
		@Override
		public void setGroupClickStatus(int groupPosition, int status) {
			mGroupStatusMap.put(groupPosition, status);
		}
		
		@Override
		public int getGroupClickStatus(int groupPosition) {
			if(mGroupStatusMap.get(groupPosition, -1) != -1){
				return mGroupStatusMap.get(groupPosition);
			}
			else{
				return 0;
			}
		}
	}

}

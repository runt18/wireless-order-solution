package com.wireless.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ListView;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;

public class ItemFragment extends Fragment{
	private ArrayList<Kitchen> mValidKitchens;
	private ArrayList<Department> mValidDepts;
	private List<List<Kitchen>> mKitchenChild;//分厨
	
	ExpandableListView mKitchenLstView;
	
	private static OnItemChangeListener onItemChangeListener;

	public interface OnItemChangeListener{
		void onItemChange(int position);
	}
	
	public static void setItemChangeListener(OnItemChangeListener l){
		onItemChangeListener = l;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		prepareDatas();
	}
	
	/**
	 * create the left fragment view
	 */
	@Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {  
	   View fragmentView = inflater.inflate(R.layout.item_layout, container,false);
	   
	   /*
	    * setup kitchen listview and it's adapter
	    */
	   mKitchenLstView = (ExpandableListView) fragmentView.findViewById(R.id.expandableListView1);
	   mKitchenLstView.setAdapter(new BaseExpandableListAdapter() {

			@Override
			public int getGroupCount() {
				return mValidDepts.size();
			}

			@Override
			public int getChildrenCount(int groupPosition) {
				return mKitchenChild.get(groupPosition).size();
			}

			@Override
			public Object getGroup(int groupPosition) {
				return mValidDepts.get(groupPosition);
			}

			@Override
			public Object getChild(int groupPosition, int childPosition) {
				return mKitchenChild.get(groupPosition).get(childPosition);
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
				return false;
			}

			/**
			 * setup department view
			 */
			@Override
			public View getGroupView(int groupPosition, boolean isExpanded,View convertView, ViewGroup parent) {
				View view;
				if (convertView != null) {
					view = convertView;
				} else {
					view = View.inflate(ItemFragment.this.getActivity(),
							R.layout.xpd_lstview_group, null);
				}

				((TextView) view.findViewById(R.id.kitchenGroup))
						.setText(mValidDepts.get(groupPosition).name);

				return view;
			}

			/**
			 * setup kitchen view
			 */
			@Override
			public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
				View view;
				if (convertView != null) {
					view = convertView;
				} else {
					view = View.inflate(ItemFragment.this.getActivity(), R.layout.xpd_lstview_child, null);
				}
				((TextView) view.findViewById(R.id.mychild)).setText(mKitchenChild.get(groupPosition).get(childPosition).name);
				return view;
			}

			@Override
			public boolean isChildSelectable(int groupPosition,	int childPosition) {
				return true;
			}
			
			@Override 
			public void onGroupCollapsed(int groupPosition)
			{
			}
		});
	   
	   return fragmentView;
   }
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		mKitchenLstView.setOnChildClickListener(new OnChildClickListener(){

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition,
					long id) {
				// TODO Auto-generated method stub

				onItemChangeListener.onItemChange(childPosition);
				return false;
			}
		});
	}
	
   private void prepareDatas(){
		  
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
		
		/**
		 * 筛选出每个部门中有菜品的厨房
		 */
		List<List<Kitchen>> kitchenChild = new ArrayList<List<Kitchen>>();
		for (int i = 0; i < mValidDepts.size(); i++) {
			List<Kitchen> kitchens = new ArrayList<Kitchen>();
			for (int j = 0; j < mValidKitchens.size(); j++) {
				if (mValidKitchens.get(j).dept.deptID == mValidDepts.get(i).deptID) {
					kitchens.add(mValidKitchens.get(j));
				}
			}
			kitchenChild.add(kitchens);
		}
		mKitchenChild = kitchenChild;
   }
}

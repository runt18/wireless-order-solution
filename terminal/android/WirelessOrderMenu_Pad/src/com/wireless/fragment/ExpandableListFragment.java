package com.wireless.fragment;

import java.util.ArrayList;
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
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.wireless.ordermenu.R;
import com.wireless.protocol.Department;
import com.wireless.protocol.Kitchen;

public class ExpandableListFragment extends Fragment{
	
	private List<Department> mGroups = new ArrayList<Department>();			//部门
	private List<List<Kitchen>> mChildren = new ArrayList<List<Kitchen>>();	//分厨
	
	private ExpandableListView mListView;
	private KitchenExpandableAdapter mAdapter;
	private Kitchen mCurrentKitchen;
	
	private OnItemChangeListener mOnItemChangeListener;

	public interface OnItemChangeListener{
		void onItemChange(Kitchen value);
	}
	
	public void setOnItemChangeListener(OnItemChangeListener l){
		mOnItemChangeListener = l;
	}
	
	/**
	 * 设置部门和厨房的数据源，并通知List进行更新
	 * @param groups
	 * @param children
	 */
	public void notifyDataChanged(List<Department> depts , List<Kitchen> kitchens){
		
		mGroups.clear();
		mChildren.clear();
		
		mGroups.addAll(depts);
		for(Department dept : depts){
			List<Kitchen> childKitchens = new ArrayList<Kitchen>();
			for(Kitchen kitchen : kitchens){
				if(kitchen.dept.equals(dept)){
					childKitchens.add(kitchen);
				}
			}
			mChildren.add(childKitchens);
		}
		mAdapter.notifyDataSetChanged();
	}
	/**
	 * 展开第一项
	 */
	public void performClick(int groupPosition){
		mListView.expandGroup(groupPosition);
		final int childPos = groupPosition+1;
		
//		//保存第一个数据
		mCurrentKitchen = mChildren.get(groupPosition).get(0);
		getView().postDelayed(new Runnable(){
			@Override
			public void run() {
				mListView.performItemClick(mListView.getChildAt(childPos), childPos, childPos);
			}
		}, 100);
	}
	/**
	 * 设置ListView显示某个特定的厨房
	 * @param kitchenToSet
	 */
	public void setPosition(final Kitchen kitchenToSet, boolean isClick){
			int[] positions = new int[2];
			int groupPos = 0;
			for(List<Kitchen> kitchens : mChildren){
				int childPos = 0;
				for(Kitchen kitchen : kitchens){
					if(kitchen.equals(kitchenToSet)){
						positions[0] = groupPos;
						positions[1] = childPos;
						break;
					}
					childPos++;
				}
				groupPos++;
			}
			int groupCount = mListView.getExpandableListAdapter().getGroupCount();
			
			for(int i=0;i<groupCount;i++)
			{
				if(mListView.isGroupExpanded(i))
				{
//					mListView.setTag(null);
					mListView.collapseGroup(i);
				}
			}
			//计算出回调的位置，模拟被点击
			mListView.expandGroup(positions[0]);
			int childPos = positions[0] + positions[1] + 1;
			mListView.setTag(isClick);
			mListView.performItemClick(mListView.getChildAt(childPos), childPos, childPos);
	}
	
	/**
	 * create the left fragment view
	 */
	@Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {  
	   View fragmentView = inflater.inflate(R.layout.item_layout, container, false);
	   //Setup kitchen list view and it's adapter.
	   mListView = (ExpandableListView) fragmentView.findViewById(R.id.expandableListView1);
	   mAdapter = new KitchenExpandableAdapter();
	   mListView.setAdapter(mAdapter);
	   
	   return fragmentView;
   }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		mListView.setOnChildClickListener(new OnChildClickListener(){
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				final Kitchen currentKitchen = mChildren.get(groupPosition).get(childPosition);
				if(!currentKitchen.equals(mCurrentKitchen))
				{
					mCurrentKitchen = currentKitchen;
					if(v != null)
					{	
						if(parent.getTag() != null && !(Boolean)parent.getTag())
						{
							//do nothing
							Log.i("do","nothing");
						}
						else{ 
							Log.i("clkcke","cx");
//							//通知侦听器改变
							mOnItemChangeListener.onItemChange(currentKitchen);
						}
						parent.setTag(null);

					}
				}

				return true;
			}
		});
		
		mListView.setOnGroupClickListener(new OnGroupClickListener(){
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				int groupCount = mListView.getExpandableListAdapter().getGroupCount();
				
				for(int i=0;i<groupCount;i++)
				{
					if(mListView.isGroupExpanded(i))
					{
						mListView.collapseGroup(i);
					}
				}
				//点击group时默认显示第一个
				mListView.expandGroup(groupPosition);
				int childPos = groupPosition +1;
				mListView.performItemClick(mListView.getChildAt(childPos), childPos, childPos);
				
				return true;
			}
		});
		
	}
    
    private class KitchenExpandableAdapter extends BaseExpandableListAdapter{

		@Override
		public int getGroupCount() {
			return mGroups.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mChildren.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mGroups.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return mChildren.get(groupPosition).get(childPosition);
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
				view = View.inflate(ExpandableListFragment.this.getActivity(),R.layout.xpd_lstview_group, null);
			}

			((TextView) view.findViewById(R.id.kitchenGroup)).setText(mGroups.get(groupPosition).name);

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
				view = View.inflate(ExpandableListFragment.this.getActivity(), R.layout.xpd_lstview_child, null);
			}
			Kitchen kitchen = mChildren.get(groupPosition).get(childPosition);
			((TextView) view.findViewById(R.id.mychild)).setText(kitchen.name);
			
			//更改点击显示样式
			if(mCurrentKitchen.equals(kitchen))
				view.setBackgroundColor(view.getResources().getColor(R.color.blue));
			else view.setBackgroundDrawable(null);
			return view;
		}

		@Override
		public boolean isChildSelectable(int groupPosition,	int childPosition) {
			return true;
		}
    }
}


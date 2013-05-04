package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.wireless.ordermenu.R;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.protocol.DepartmentTree.DeptNode;
import com.wireless.protocol.DepartmentTree.KitchenNode;

/**
 * This fragment contains a {@link ExpandableListView} and encapsulate a {@link ExpandableListAdapter}
 * <br/>
 * it use {@link #notifyDataChanged(List, List)} to set data 
 * @author ggdsn1
 *
 */
public class DepartmentTreeFragment extends Fragment{
	
	private List<DeptNode> mDeptNodes = new ArrayList<DeptNode>();
	
	private ExpandableListView mListView;
	private KitchenExpandableAdapter mAdapter;
	private Kitchen mCurrentKitchen;
	
	private OnKitchenChangedListener mOnKitchenChangeListener;

	public static interface OnKitchenChangedListener{
		void onKitchenChange(Kitchen currentKitchen);
	}
	
	public void setOnKitchenChangeListener(OnKitchenChangedListener l){
		mOnKitchenChangeListener = l;
	}
	
	/**
	 * 设置部门和厨房的数据源，并通知List进行更新
	 */
	public void notifyDataChanged(List<DeptNode> deptNodes){
		if(deptNodes != null){
			mDeptNodes = deptNodes;
			mAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * 设置ListView选中第一个厨房
	 * @return true if the clicked kitchen is found, otherwise return false;
	 */
	public boolean performClickFirstKitchen(){
		try{
			return performClickByKitchen(mDeptNodes.get(0).getValue().get(0).getKey());
		}catch(IndexOutOfBoundsException e){
			return false;
		}
	}
	
	/**
	 * 设置ListView选中某个特定的厨房
	 * @param clickedKitchen
	 * @return true if the clicked kitchen is found, otherwise return false;
	 */
	public boolean performClickByKitchen(final Kitchen clickedKitchen){
		
		boolean isFound = false;
		int groupPos = 0;
		int childPos = 0;
		for(DeptNode deptNode : mDeptNodes){
			childPos = 0;
			for(KitchenNode kitchenNode : deptNode.getValue()){
				if(kitchenNode.getKey().equals(clickedKitchen)){
					mCurrentKitchen = kitchenNode.getKey();
					isFound = true;
					break;
				}
				childPos++;
			}
			
			if(isFound){
				break;
			}
			
			groupPos++;
		}

		if(isFound){
			int groupCount = mListView.getExpandableListAdapter().getGroupCount();
			//收起所有部门
			for(int i = 0; i < groupCount; i++){
				if(mListView.isGroupExpanded(i)){
					mListView.collapseGroup(i);
				}
			}
			//展开厨房所在的部门
			mListView.expandGroup(groupPos);
			
			//计算出回调的位置，更改样式和当前厨房
			final int childViewIndex = groupPos + childPos + 1;
			
			mListView.post(new Runnable(){
				@Override
				public void run() {
					View curView = mListView.getChildAt(childViewIndex);
					if(curView != null){
						curView.setBackgroundColor(getResources().getColor(R.color.blue));
					}
				}
			});
			
			return true;
			
		}else{
			return false;

		}
		

	}
	
	/**
	 * it will find the specified item and return true,else return false
	 * @param kitchenToSearch
	 * @return
	 */
	public boolean containsKitchen(Kitchen kitchenToSearch){
		for(DeptNode deptNode : mDeptNodes){
			for(KitchenNode kitchenNode : deptNode.getValue()){
				if(kitchenNode.getKey().equals(kitchenToSearch)){
					return true;
				}
			}
		}
		return false;
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
	public void onActivityCreated(Bundle savedInstanceState){
		
		super.onActivityCreated(savedInstanceState);
		
		mListView.setOnChildClickListener(new OnChildClickListener(){
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				final Kitchen kitchenSelected = mDeptNodes.get(groupPosition).getValue().get(childPosition).getKey();
				if(!kitchenSelected.equals(mCurrentKitchen)){
					mCurrentKitchen = kitchenSelected;
					if(v != null){	
						//通知侦听器厨房发生了改变
						if(mOnKitchenChangeListener != null){
							mOnKitchenChangeListener.onKitchenChange(mCurrentKitchen);
						}
					}
				}
				return true;
			}
		});
		
		mListView.setOnGroupClickListener(new OnGroupClickListener(){
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				int groupCount = mListView.getExpandableListAdapter().getGroupCount();
				
				for(int i = 0; i < groupCount; i++){
					if(mListView.isGroupExpanded(i)){
						mListView.collapseGroup(i);
					}
				}
				
				//点击Group时默认显示第一个Kitchen
				mListView.expandGroup(groupPosition);
				int childPos = groupPosition + 1;
				mListView.performItemClick(mListView.getChildAt(childPos), childPos, childPos);
				
				return true;
			}
		});
		
	}
    
    public Kitchen getCurrentKitchen() {
		return mCurrentKitchen;
	}

	private class KitchenExpandableAdapter extends BaseExpandableListAdapter{

		@Override
		public int getGroupCount() {
			return mDeptNodes.size();
			//return mGroups.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mDeptNodes.get(groupPosition).getValue().size();
			//return mChildren.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mDeptNodes.get(groupPosition).getKey();
//			return mGroups.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return mDeptNodes.get(groupPosition).getValue().get(childPosition);
//			return mChildren.get(groupPosition).get(childPosition);
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
				view = View.inflate(DepartmentTreeFragment.this.getActivity(),R.layout.xpd_lstview_group, null);
			}

			//((TextView) view.findViewById(R.id.kitchenGroup)).setText(mGroups.get(groupPosition).getName());
			((TextView) view.findViewById(R.id.kitchenGroup)).setText(mDeptNodes.get(groupPosition).getKey().getName());
			return view;
		}

		/**
		 * setup kitchen view
		 */
		@SuppressWarnings("deprecation")
		@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			View view;
			if (convertView != null) {
				view = convertView;
			} else {
				view = View.inflate(DepartmentTreeFragment.this.getActivity(), R.layout.xpd_lstview_child, null);
			}
			
			//Kitchen kitchen = mChildren.get(groupPosition).get(childPosition);
			Kitchen kitchen = mDeptNodes.get(groupPosition).getValue().get(childPosition).getKey();
			((TextView) view.findViewById(R.id.mychild)).setText(kitchen.getName());
			
			//更改点击显示样式
			if(mCurrentKitchen.equals(kitchen)){
				view.setBackgroundColor(view.getResources().getColor(R.color.blue));
			}else{
				view.setBackgroundDrawable(null);
			}
			return view;
		}

		@Override
		public boolean isChildSelectable(int groupPosition,	int childPosition) {
			return true;
		}
    }
}


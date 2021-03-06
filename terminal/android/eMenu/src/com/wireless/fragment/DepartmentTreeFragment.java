package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.graphics.Color;
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
import com.wireless.pojo.menuMgr.DepartmentTree.DeptNode;
import com.wireless.pojo.menuMgr.DepartmentTree.KitchenNode;
import com.wireless.pojo.menuMgr.Kitchen;

/**
 * This fragment contains a {@link ExpandableListView} and encapsulate a {@link ExpandableListAdapter}
 * <br/>
 * it use {@link #notifyDataChanged(List, List)} to set data 
 * @author ggdsn1
 *
 */
public class DepartmentTreeFragment extends Fragment{
	//存储部门节点信息
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
		for(DeptNode deptNode : mDeptNodes){
			//childPos = 0;
			for(KitchenNode kitchenNode : deptNode.getValue()){
				if(kitchenNode.getKey().equals(clickedKitchen)){
					mCurrentKitchen = kitchenNode.getKey();
					isFound = true;
					break;
				}
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
	   View fragmentView = inflater.inflate(R.layout.fragment_department_tree, container, false);
	   //Setup kitchen list view and it's adapter.
	   mListView = (ExpandableListView) fragmentView.findViewById(R.id.eplv_deptTree_fgm);
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
				view = View.inflate(DepartmentTreeFragment.this.getActivity(),R.layout.listview_group_department_tree_fragment, null);
			}

			//((TextView) view.findViewById(R.id.kitchenGroup)).setText(mGroups.get(groupPosition).getName());
			((TextView) view.findViewById(R.id.kitchenGroup)).setText(mDeptNodes.get(groupPosition).getKey().getName());
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
				view = View.inflate(getActivity(), R.layout.listview_child_department_tree_fragment, null);
			}
			
			//Kitchen kitchen = mChildren.get(groupPosition).get(childPosition);
			Kitchen kitchen = mDeptNodes.get(groupPosition).getValue().get(childPosition).getKey();
			((TextView) view.findViewById(R.id.txtView_kitchenName_deptTree_fgm)).setText(kitchen.getName());
			
			//更改点击显示样式
			if(mCurrentKitchen.equals(kitchen)){
				view.setBackgroundColor(view.getResources().getColor(R.color.blue));
			}else{
				view.setBackgroundColor(Color.WHITE);
			}
			return view;
		}

		@Override
		public boolean isChildSelectable(int groupPosition,	int childPosition) {
			return true;
		}
    }
}


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
import com.wireless.protocol.PDepartment;
import com.wireless.protocol.PKitchen;

/**
 * this fragment contains a {@link ExpandableListView} and encapsulate a {@link ExpandableListAdapter}
 * <br/>
 * it use {@link #notifyDataChanged(List, List)} to set data 
 * @author ggdsn1
 *
 */
public class DepartmentTreeFragment extends Fragment{
	
	private List<PDepartment> mGroups = new ArrayList<PDepartment>();			//部门
	private List<List<PKitchen>> mChildren = new ArrayList<List<PKitchen>>();	//分厨
	
	private ExpandableListView mListView;
	private KitchenExpandableAdapter mAdapter;
	private PKitchen mCurrentKitchen;
	
	private OnItemChangedListener mOnItemChangeListener;

	public interface OnItemChangedListener{
		void onItemChange(PKitchen value);
	}
	
	public void setOnItemChangeListener(OnItemChangedListener l){
		mOnItemChangeListener = l;
	}
	
	/**
	 * 设置部门和厨房的数据源，并通知List进行更新
	 * @param groups
	 * @param children
	 */
	public void notifyDataChanged(List<PDepartment> depts , List<PKitchen> kitchens){
		if(depts != null && kitchens != null)
		{
			mGroups.clear();
			mChildren.clear();
			mGroups.addAll(depts);
			for(PDepartment dept : depts){
				List<PKitchen> childKitchens = new ArrayList<PKitchen>();
				for(PKitchen kitchen : kitchens){
					if(kitchen.getDept().equals(dept)){
						childKitchens.add(kitchen);
					}
				}
				mChildren.add(childKitchens);
			}
			mAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * 展开第一项
	 */
	public void performClickFirstItem(){
		try{
			mListView.expandGroup(0);
			final int childPos = 1;
			
			//保存第一个数据
			mCurrentKitchen = mChildren.get(0).get(0);
			getView().postDelayed(new Runnable(){
				@Override
				public void run() {
					mListView.performItemClick(mListView.getChildAt(childPos), childPos, childPos);
				}
			}, 100);
		}catch(IndexOutOfBoundsException e){
			
		}
	}
	
	/**
	 * it will find the specified item and return true,else return false
	 * @param kitchenToSet
	 * @return
	 */
	public boolean hasItem(PKitchen kitchenToSet){
		final int[] positions = new int[2];
		int groupPos = 0;
		for(List<PKitchen> kitchens : mChildren){
			int childPos = 0;
			for(PKitchen kitchen : kitchens){
				if(kitchen.equals(kitchenToSet)){
					positions[0] = groupPos;
					positions[1] = childPos;
					return true;
				}
				childPos++;
			}
			groupPos++;
		}
		return false;
	}
	
	/**
	 * 设置ListView显示某个特定的厨房
	 * @param kitchenToSet
	 */
	public void setPosition(final PKitchen kitchenToSet){
			final int[] positions = new int[2];
			int groupPos = 0;
			for(List<PKitchen> kitchens : mChildren){
				int childPos = 0;
				for(PKitchen kitchen : kitchens){
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
			
			for(int i = 0; i < groupCount; i++){
				if(mListView.isGroupExpanded(i)){
					mListView.collapseGroup(i);
				}
			}
			//计算出回调的位置，更改样式和当前厨房
			mListView.expandGroup(positions[0]);
			mCurrentKitchen = mChildren.get(positions[0]).get(positions[1]);
			
			mListView.post(new Runnable(){
				@Override
				public void run() {
					int childPos = positions[0] + positions[1] + 1;
					View curView = mListView.getChildAt(childPos);
					if(curView != null)
						curView.setBackgroundColor(getResources().getColor(R.color.blue));
				}
			});
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
				final PKitchen currentKitchen = mChildren.get(groupPosition).get(childPosition);
				if(!currentKitchen.equals(mCurrentKitchen))
				{
					mCurrentKitchen = currentKitchen;
					if(v != null)
					{	
						//通知侦听器改变
						if(mOnItemChangeListener != null)
							mOnItemChangeListener.onItemChange(currentKitchen);
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
    
    public PKitchen getCurrentKitchen() {
		return mCurrentKitchen;
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
				view = View.inflate(DepartmentTreeFragment.this.getActivity(),R.layout.xpd_lstview_group, null);
			}

			((TextView) view.findViewById(R.id.kitchenGroup)).setText(mGroups.get(groupPosition).getName());

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
			
			PKitchen kitchen = mChildren.get(groupPosition).get(childPosition);
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


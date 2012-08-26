package com.wireless.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

import com.wireless.ordermenu.R;
import com.wireless.protocol.Department;
import com.wireless.protocol.Kitchen;

public class ItemFragment extends Fragment{
	private List<Department> mGroups = new ArrayList<Department>();
	private List<List<Kitchen>> mChildren = new ArrayList<List<Kitchen>>();//分厨
	
	private ExpandableListView mListView;
	private KitchenExpandableAdapter mAdapter;
	
    private static OnItemChangeListener sDummyListener = new OnItemChangeListener(){
		@Override
		public void onItemChange(Kitchen value) {
		}
    };
	
	private static OnItemChangeListener mOnItemChangeListener = sDummyListener;

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
		mGroups = depts;
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
	
	public void setPosition(Kitchen kitchenToSet){
		int groupPos = 0;
		for(List<Kitchen> kitchens : mChildren){
			int childPos = 0;
			for(Kitchen kitchen : kitchens){
				if(kitchen.equals(kitchenToSet)){
					mListView.expandGroup(groupPos);
					mListView.setSelectedChild(groupPos, childPos, true);
					break;
				}
				childPos++;
			}
			groupPos++;
		}
	}
	
	//FIXME to be deleted
	public void setContent(List<Department> groups , List<List<Kitchen>> children){
		mGroups = groups;
		mChildren = children;
		mAdapter.notifyDataSetChanged();
	}
	
	//FIXME to be deleted
	public void setPosition(int groupPosition, int childPosition){
		mListView.expandGroup(groupPosition);
		mListView.setSelectedChild(groupPosition, childPosition, true);
	}
	
	/**
	 * create the left fragment view
	 */
	@Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {  
	   View fragmentView = inflater.inflate(R.layout.item_layout, container, false);
	   
	   /*
	    * setup kitchen listview and it's adapter
	    */
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
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition,
					long id) {
				Kitchen currentKitchen = mChildren.get(groupPosition).get(childPosition);
				mOnItemChangeListener.onItemChange(currentKitchen);
				return false;
			}
		});
	}
    
    class KitchenExpandableAdapter extends BaseExpandableListAdapter{

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
				view = View.inflate(ItemFragment.this.getActivity(),R.layout.xpd_lstview_group, null);
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
				view = View.inflate(ItemFragment.this.getActivity(), R.layout.xpd_lstview_child, null);
			}
			((TextView) view.findViewById(R.id.mychild)).setText(mChildren.get(groupPosition).get(childPosition).name);
			return view;
		}

		@Override
		public boolean isChildSelectable(int groupPosition,	int childPosition) {
			return true;
		}
    }
}


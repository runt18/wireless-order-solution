package com.wireless.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;

public class ItemFragment extends Fragment{
    private static final String STATE_ACTIVATED_GROUP = "activated_group";
    private static final String STATE_ACTIVATED_CHILD = "activated_child";
	
    private static OnItemChangeListener sDummyListener = new OnItemChangeListener(){
		@Override
		public void onItemChange(int value) {
		}
    };
    
	private List<Department> mGroups = new ArrayList<Department>();
	private List<List<Kitchen>> mChildren = new ArrayList<List<Kitchen>>();//分厨
	
	private ExpandableListView mListView;
	private KitchenExpandableAdapter mAdapter;
	
	private int mActivatedGroup,mActivatedChild;
	
	private static OnItemChangeListener mOnItemChangeListener = sDummyListener;

	public interface OnItemChangeListener{
		void onItemChange(int value);
	}
	
	public static void setItemChangeListener(OnItemChangeListener l){
		mOnItemChangeListener = l;
	}
	
	public void onUpdateContent(List<Department> groups , List<List<Kitchen>> children){
		mGroups = groups;
		mChildren = children;
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
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
	   mListView = (ExpandableListView) fragmentView.findViewById(R.id.expandableListView1);
	   mAdapter = new KitchenExpandableAdapter();
	   mListView.setAdapter(mAdapter);

	   return fragmentView;
   }
	
    @TargetApi(13)
	@Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_GROUP) && savedInstanceState.containsKey(STATE_ACTIVATED_CHILD)){
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_GROUP),savedInstanceState.getInt(STATE_ACTIVATED_CHILD));
        }
    }
    
    public void setActivatedPosition(int group , int child)
    {
    	//TODO 定义活动项样式改变的方法
    	mActivatedGroup = group;
    	mActivatedChild = child;
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
				short kitchenID = mChildren.get(groupPosition).get(childPosition).aliasID;
				Log.i("kitchenid",""+kitchenID);
				mOnItemChangeListener.onItemChange(kitchenID);
				return false;
			}
		});
	}
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		if(!(activity instanceof OnItemChangeListener))
			throw new IllegalStateException("Activity must implement fragment's OnItemChangeListener");
		mOnItemChangeListener = (OnItemChangeListener) activity;
	}
	
    @Override
    public void onDetach() {
        super.onDetach();
        mOnItemChangeListener = sDummyListener;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
            outState.putInt(STATE_ACTIVATED_GROUP, mActivatedGroup );
            outState.putInt(STATE_ACTIVATED_CHILD, mActivatedChild);
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
				view = View.inflate(ItemFragment.this.getActivity(),
						R.layout.xpd_lstview_group, null);
			}

			((TextView) view.findViewById(R.id.kitchenGroup))
					.setText(mGroups.get(groupPosition).name);

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


package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.List;

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
import com.wireless.pojo.dishesOrder.Food;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.DepartmentTree;
import com.wireless.pojo.menuMgr.DepartmentTree.DeptNode;
import com.wireless.pojo.menuMgr.DepartmentTree.KitchenNode;
import com.wireless.pojo.menuMgr.FoodList;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.R;
import com.wireless.ui.dialog.AskOrderAmountDialog;
import com.wireless.ui.dialog.AskOrderAmountDialog.OnFoodPickedListener;

public class KitchenFragment extends Fragment {

	private DepartmentRefreshHandler mDepartmentRefreshHandler;
	private KitchenRefreshHandler mKitchenRefreshHandler;
	
	private ExpandableListView mXpListView;
	
	private OnFoodPickedListener mFoodPickedListener;

	/**
	 * ���õ���ĳ����Ʒ��Ļص�����
	 * @param foodPickedListener
	 */
	public void setFoodPickedListener(OnFoodPickedListener foodPickedListener){
		mFoodPickedListener = foodPickedListener;
	}
	
	private static class DepartmentRefreshHandler extends Handler{
		private WeakReference<KitchenFragment> mFragment;
		private LinearLayout mDeptLayout;

		DepartmentRefreshHandler(KitchenFragment fragment) {
			this.mFragment = new WeakReference<KitchenFragment>(fragment);
		}
		
		@Override
		public void handleMessage(Message msg){
			
			final KitchenFragment fragment = mFragment.get();
			
			if(mDeptLayout == null){
				mDeptLayout = (LinearLayout)fragment.getView().findViewById(R.id.linearLayout_kitchenFragment);
			}
			
			//������в���
			mDeptLayout.removeAllViews();
			int i = 0;
			for(Department dept : WirelessOrder.foodMenu.depts){
				//������ͼ��
				RelativeLayout view = (RelativeLayout) LayoutInflater.from(fragment.getActivity()).inflate(R.layout.kitchen_fragment_dept_item, null);
				
				//������ͼ�㲢������ɫ
				final RelativeLayout childView = (RelativeLayout) view.findViewById(R.id.relativeLayout_child_kcFgm);
				childView.setBackgroundResource(R.color.orange);

				//���õ�һ��Ϊѡ��״̬
				if(i == 0){
					mDeptLayout.setTag(childView);
					childView.setBackgroundResource(R.color.gold);
				}
				i++;
				
				//���ø�������
				((TextView)view.findViewById(R.id.textView_kitchenFragment_dept_item)).setText(dept.getName());
				
				view.setTag(dept);
				
				//���ø���������
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						//ˢ�³�����ʾ
						Department dept = (Department)v.getTag();
						fragment.mKitchenRefreshHandler.sendEmptyMessage(dept.getId());
						
						//��ǰһ����������Ϊ����״̬
						if(mDeptLayout.getTag() != null){
							((View)(mDeptLayout.getTag())).setBackgroundResource(R.color.orange);
						}
						mDeptLayout.setTag(childView);
						//���ø���ĵ��״̬
						childView.setBackgroundResource(R.color.gold);
						
						//���رհ�ť��ʾ����ȡ����ʾ
						ImageButton collapseBtn = (ImageButton) fragment.getView().findViewById(R.id.imageButton_collaps_kitchenFgm);
						if(collapseBtn.isShown())
							collapseBtn.setVisibility(View.GONE);
					}
				});
				mDeptLayout.addView(view);
				
			}
		}
	}

	private static class KitchenRefreshHandler extends Handler{
		private WeakReference<KitchenFragment> mFragment;

		private DepartmentTree mDeptTree = WirelessOrder.foodMenu.foods.asDeptTree();
		
		KitchenRefreshHandler(KitchenFragment fragment) {
			this.mFragment = new WeakReference<KitchenFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			KitchenFragment fragment = mFragment.get();

			int deptIdToFilter = msg.what;
			
			//����������ɸѡ��Ҫ��ʾ�ĳ���, ����Ʒ����������
			for(DeptNode deptNode : mDeptTree.asDeptNodes(Food.BY_SALES)){
				if(deptNode.getKey().getId() == deptIdToFilter){
					fragment.mXpListView.setAdapter(fragment.new KitchenExpandableListAdapter(deptNode.getValue()));
					break;
				}
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDepartmentRefreshHandler = new DepartmentRefreshHandler(this);
		mKitchenRefreshHandler = new KitchenRefreshHandler(this);
	}
 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.kitchen_fragment, container, false);
		
		mXpListView = (ExpandableListView) view.findViewById(R.id.expandableListView_kitchenFragment);
		//�ر��鰴ť
		final ImageButton collapseBtn = (ImageButton) view.findViewById(R.id.imageButton_collaps_kitchenFgm);
		collapseBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//�رյ�ǰ��
				int groupPosition  = (Integer) collapseBtn.getTag();
				mXpListView.collapseGroup(groupPosition);
				mXpListView.smoothScrollToPosition(0);
			}
		});
		
		//����groupչ����������ÿ��ֻ��һ��
		mXpListView.setOnGroupExpandListener(new OnGroupExpandListener() {
			@Override
			public void onGroupExpand( int groupPosition) {
				//�ر�������
				int groupCount = mXpListView.getExpandableListAdapter().getGroupCount();
				for (int i = 0; i < groupCount; i++) {
					if (groupPosition != i) {
						mXpListView.collapseGroup(i);
					}
				}
				
				//��ʾ�ر��鰴ť
				collapseBtn.setVisibility(View.VISIBLE);
				collapseBtn.setTag(groupPosition);
			}
		});
		
		mXpListView.setOnGroupCollapseListener(new OnGroupCollapseListener(){
			@Override
			public void onGroupCollapse(int groupPosition) {
				//��ر�ʱ��ť��ʧ
				collapseBtn.setVisibility(View.GONE);
			}
		});
		
		mDepartmentRefreshHandler.sendEmptyMessage(0);
		mKitchenRefreshHandler.sendEmptyMessage(WirelessOrder.foodMenu.depts.get(0).getId());
		return view;
	}
	
	
	private class KitchenExpandableListAdapter extends BaseExpandableListAdapter{
		
		//ÿ����ʾ�Ĳ�Ʒ����
		private final int mEachRowAmount = 3;
		//����Դ��������ÿ���������еĲ�Ʒ
		private final List<KitchenNode> mKitchenNodes;
		
		KitchenExpandableListAdapter(List<KitchenNode> kitchenNodes){
			
			this.mKitchenNodes = kitchenNodes;
			
//			this.mFoodsByKitchen = new ArrayList<Entry<PKitchen, FoodList>>(foodsByKitchen.entrySet());
//			
//			//Ҫ��ʾ�ĳ������������
//			Collections.sort(this.mFoodsByKitchen, new Comparator<Entry<PKitchen, FoodList>>(){
//
//				@Override
//				public int compare(Entry<PKitchen, FoodList> lhs,	Entry<PKitchen, FoodList> rhs) {
//					if(lhs.getKey().getAliasId() > rhs.getKey().getAliasId()){
//						return 1;
//					}else if(lhs.getKey().getAliasId() < rhs.getKey().getAliasId()){
//						return -1;
//					}else{
//						return 0;
//					}
//				}
//				
//			});
		}

		@Override
		public int getGroupCount() {
			return mKitchenNodes.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			int foodAmountToKitchen = mKitchenNodes.get(groupPosition).getValue().size();
			return foodAmountToKitchen / mEachRowAmount + (foodAmountToKitchen % mEachRowAmount == 0 ? 0 : 1);
					
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mKitchenNodes.get(groupPosition).getKey();
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			FoodList foodsToKitchen = mKitchenNodes.get(groupPosition).getValue();
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
			
			//���ó�����
			((TextView) view.findViewById(R.id.textView_name_kitchenFragment_xp_group_item))
				.setText(mKitchenNodes.get(groupPosition).getKey().getName());
			//���ó������в�Ʒ����
			((TextView) view.findViewById(R.id.textView_count_kitchenFragment_xp_group_item))
				.setText(Integer.toString(mKitchenNodes.get(groupPosition).getValue().size()));
			
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
			
			//���ø��е�GridView
			GridView gridView = (GridView) view.findViewById(R.id.gridView_kitchenFgm_xplv_child_item);
			gridView.setVerticalSpacing(0);
			
			FoodList foodsToKitchen = mKitchenNodes.get(groupPosition).getValue();
			int start = childPosition * mEachRowAmount;
			int end = start + mEachRowAmount;
			gridView.setAdapter(new GridAdapter(foodsToKitchen.subList(start, end > foodsToKitchen.size() ? foodsToKitchen.size() : end)));
			
			//����������
			gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick( AdapterView<?> parent, View view, int position, long id) {
					Food food = (Food) view.getTag();
					if(!food.isSellOut()){
						((TextView)view.findViewById(R.id.textView_sellout_pickFoodFgm_item)).setVisibility(View.GONE);

						new AskOrderAmountDialog(getActivity(), food, mFoodPickedListener, null).show();
					}else{
						((TextView)view.findViewById(R.id.textView_sellout_pickFoodFgm_item)).setVisibility(View.VISIBLE);
						
						Toast.makeText(getActivity(), food.getName() + "������", Toast.LENGTH_SHORT).show();
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

			//���ø������ʾ
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



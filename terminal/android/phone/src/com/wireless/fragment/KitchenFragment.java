package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.DepartmentTree;
import com.wireless.pojo.menuMgr.DepartmentTree.DeptNode;
import com.wireless.pojo.menuMgr.DepartmentTree.KitchenNode;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodList;
import com.wireless.pojo.menuMgr.FoodUnit;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.R;
import com.wireless.ui.dialog.AskOrderAmountDialog;
import com.wireless.ui.dialog.AskOrderAmountDialog.ActionType;

public class KitchenFragment extends Fragment {

	private DepartmentRefreshHandler mDepartmentRefreshHandler;
	private KitchenRefreshHandler mKitchenRefreshHandler;
	
	private DepartmentTree mDeptTree;

	private ExpandableListView mXpListView;
	
	private static class BuildDepartmentHandler extends Handler{
		private WeakReference<KitchenFragment> mFragment;

		BuildDepartmentHandler(KitchenFragment fragment) {
			this.mFragment = new WeakReference<KitchenFragment>(fragment);
		}
		 
		@Override
		public void handleMessage(Message msg){
			
			final KitchenFragment fragment = mFragment.get();
			
			final LinearLayout deptLayout = (LinearLayout)fragment.getView().findViewById(R.id.linearLayout_top_kitchenFragment);
			
			//添加所有部门
			deptLayout.removeAllViews();
			for(final Department dept : fragment.mDeptTree.asDeptList()){
				//解析跟图层
				View view = LayoutInflater.from(fragment.getActivity()).inflate(R.layout.pick_food_by_kitchen_fgm_dept_item, (ViewGroup)fragment.getActivity().getWindow().getDecorView(), false);
				
				//设置该项名称
				((TextView)view.findViewById(R.id.txtView_name_kitchenFgm_dept_item)).setText(dept.getName());
				
				view.setTag(dept.getId());
				
				//设置该项侦听器
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						//刷新部门的显示
						fragment.mDepartmentRefreshHandler.sendEmptyMessage(dept.getId());
						
						//若关闭按钮显示，则取消显示
						ImageButton collapseBtn = (ImageButton) fragment.getView().findViewById(R.id.imageButton_collaps_kitchenFgm);
						if(collapseBtn.isShown()){
							collapseBtn.setVisibility(View.GONE);
						}
					}
				});
				deptLayout.addView(view);
				
			}
			
			//刷新部门的显示
			fragment.mDepartmentRefreshHandler.sendEmptyMessage(msg.what);

		}
	}
	
	private static class DepartmentRefreshHandler extends Handler{
		private WeakReference<KitchenFragment> mFragment;

		DepartmentRefreshHandler(KitchenFragment fragment) {
			this.mFragment = new WeakReference<KitchenFragment>(fragment);
		}
		
		@Override
		public void handleMessage(Message msg){
			
			final KitchenFragment fragment = mFragment.get();
			
			LinearLayout mDeptLayout = (LinearLayout)fragment.getView().findViewById(R.id.linearLayout_top_kitchenFragment);
			
			for(int i = 0; i < mDeptLayout.getChildCount(); i++){
				View deptView = mDeptLayout.getChildAt(i);
				View bgView = deptView.findViewById(R.id.txtView_bg_kitchenFgm_dept_item);
				
				if(msg.what == ((Short)deptView.getTag()).intValue()){
					//设置背景颜色
					bgView.setBackgroundResource(R.color.orange);
					//刷新厨房显示
					fragment.mKitchenRefreshHandler.sendEmptyMessage(msg.what);
				}else{
					bgView.setBackgroundResource(R.color.gold);
				}
			}
		}
	}

	private static class KitchenRefreshHandler extends Handler{
		private WeakReference<KitchenFragment> mFragment;

		KitchenRefreshHandler(KitchenFragment fragment) {
			this.mFragment = new WeakReference<KitchenFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			KitchenFragment fragment = mFragment.get();

			//根据条件筛选出要显示的厨房, 菜品按编号排序
			for(DeptNode deptNode : fragment.mDeptTree.asDeptNodes()){
				if(deptNode.getKey().getId() == msg.what){
					fragment.mXpListView.setAdapter(fragment.new KitchenExpandableListAdapter(deptNode.getValue()));
					break;
				}
			}
			
			//如果该部门只有一个厨房, 则显示该厨房的菜品
			if(fragment.mXpListView.getCount() == 1){
				fragment.mXpListView.expandGroup(0);
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
		View view = inflater.inflate(R.layout.pick_food_by_kitchen_fgm, container, false);
		
		mXpListView = (ExpandableListView) view.findViewById(R.id.expandableListView_kitchenFragment);
		
		//关闭组按钮
		final ImageButton collapseBtn = (ImageButton) view.findViewById(R.id.imageButton_collaps_kitchenFgm);
		collapseBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//关闭当前组
				int groupPosition = (Integer)collapseBtn.getTag();
				mXpListView.collapseGroup(groupPosition);
				mXpListView.smoothScrollToPosition(0);
			}
		});
		
		//设置group展开侦听器，每次只打开一项
		mXpListView.setOnGroupExpandListener(new OnGroupExpandListener() {
			@Override
			public void onGroupExpand(int groupPosition) {
				//关闭其它组
				int groupCount = mXpListView.getExpandableListAdapter().getGroupCount();
				for (int i = 0; i < groupCount; i++) {
					if (groupPosition != i) {
						mXpListView.collapseGroup(i);
					}
				}
				
				//显示关闭组按钮
				collapseBtn.setVisibility(View.VISIBLE);
				collapseBtn.setTag(groupPosition);
			}
		});
		
		mXpListView.setOnGroupCollapseListener(new OnGroupCollapseListener(){
			@Override
			public void onGroupCollapse(int groupPosition) {
				//组关闭时按钮消失
				collapseBtn.setVisibility(View.GONE);
			}
		});
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		mDeptTree = WirelessOrder.foodMenu.foods.asDeptTree();
		new BuildDepartmentHandler(this).sendEmptyMessage(mDeptTree.asDeptList().get(0).getId());
	}
	
	private class KitchenExpandableListAdapter extends BaseExpandableListAdapter{
		
		//每行显示的菜品数量
		private final int mEachRowAmount = 3;
		//数据源，保存了每个厨房持有的菜品
		private final List<KitchenNode> mKitchenNodes;
		
		KitchenExpandableListAdapter(List<KitchenNode> kitchenNodes){
			this.mKitchenNodes = kitchenNodes;
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
				view = View.inflate(getActivity(), R.layout.pick_food_by_kitchen_fgm_xplv_group_item, null);
			}
			
			view.setBackgroundResource(R.drawable.kitchen_fgm_group_selector);
			
			//设置厨房名
			((TextView) view.findViewById(R.id.textView_name_kitchenFragment_xp_group_item)).setText(mKitchenNodes.get(groupPosition).getKey().getName());
			//设置厨房持有菜品数量
			((TextView) view.findViewById(R.id.textView_count_kitchenFragment_xp_group_item)).setText(Integer.toString(mKitchenNodes.get(groupPosition).getValue().size()));
			
			return view;
		}

		@Override
		public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild, 
								 final View convertView, final ViewGroup parent) {
			View view;
			if(convertView != null){
				view = convertView;
			}else{
				view = View.inflate(getActivity(), R.layout.pick_food_by_kitchen_fgm_xplv_child_item, null);
			}
			
			//设置该行的GridView
			GridView gridView = (GridView) view.findViewById(R.id.gridView_kitchenFgm_xplv_child_item);
			gridView.setVerticalSpacing(0);
			
			FoodList foodsToKitchen = mKitchenNodes.get(groupPosition).getValue();
			int start = childPosition * mEachRowAmount;
			int end = start + mEachRowAmount;
			gridView.setAdapter(new PickFoodFragment.PickFoodAdapter(KitchenFragment.this.getActivity(), foodsToKitchen.subList(start, end > foodsToKitchen.size() ? foodsToKitchen.size() : end)));
			
			//设置侦听器
			gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick( AdapterView<?> parent, View view, int position, long id) {
					final Food food = (Food) view.getTag();
					if(food.isSellOut()){
						Toast.makeText(getActivity(), food.getName() + "已售罄", Toast.LENGTH_SHORT).show();
						
					}else if(food.isCurPrice()){
						final EditText currentPriceEdtTxt = new EditText(getActivity());
						currentPriceEdtTxt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
						Dialog currentPriceDialog = new AlertDialog.Builder(getActivity()).setTitle("请确定" + food.getName() + "的时价")
							.setView(currentPriceEdtTxt)
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									AskOrderAmountDialog.newInstance(food, FoodUnit.newInstance4CurPrice(Float.parseFloat(currentPriceEdtTxt.getText().toString())), ActionType.ADD, getId()).show(getFragmentManager(), AskOrderAmountDialog.TAG);
								}
							})
							.setNegativeButton("取消", null)
							.create();
						//弹出软键盘并全选输入框内容
						currentPriceDialog.setOnShowListener(new DialogInterface.OnShowListener() {
							@Override
							public void onShow(DialogInterface arg0) {
								currentPriceEdtTxt.setText(NumericUtil.float2String2(food.getPrice()));
								currentPriceEdtTxt.setSelection(0, currentPriceEdtTxt.getText().length());
		                        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(currentPriceEdtTxt, InputMethodManager.SHOW_IMPLICIT);
							}
						});
						currentPriceDialog.show();
						
					}else{
						AskOrderAmountDialog.newInstance(food, ActionType.ADD, getId()).show(getFragmentManager(), AskOrderAmountDialog.TAG);
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

}



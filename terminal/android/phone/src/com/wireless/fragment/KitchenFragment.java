package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
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
import com.wireless.protocol.Food;
import com.wireless.protocol.PDepartment;
import com.wireless.protocol.PKitchen;
import com.wireless.ui.R;
import com.wireless.ui.dialog.AskOrderAmountDialog;
import com.wireless.ui.dialog.AskOrderAmountDialog.OnFoodPickedListener;
import com.wireless.util.NumericUtil;

public class KitchenFragment extends Fragment {
	private static final int REFRESH_DEPTS = 112309;
	private static final int REFRESH_FOODS = 112310;

	private DepartmentHandler mDepartmentHandler;
	private KitchenHandler mKitchenHandler;
	
	private ArrayList<PKitchen> mValidKitchens;
	private ArrayList<PDepartment> mValidDepts;
	private ArrayList<List<Food>> mPackedValidFoodsList;
	
	private short mDeptFilter = Short.MIN_VALUE;
	
	private ExpandableListView mXpListView;
	private Food[] mOriFoods;
	
	private OnFoodPickedListener mFoodPickedListener;

//	public static interface OnFoodPickedListener{
//		/**
//		 * ��PickFoodListViewѡ�в�Ʒ�󣬻ص��˺���֪ͨActivityѡ�е�Food��Ϣ
//		 * @param food ѡ��Food����Ϣ
//		 */
//		public void onPicked(OrderFood food);
//		
//		/**
//		 * ��PickFoodListViewѡ�в�Ʒ�󣬻ص��˺���֪ͨActivityѡ�е�Food��Ϣ������ת����ζActivity
//		 * @param food
//		 * 			ѡ��Food����Ϣ
//		 */
//		public void onPickedWithTaste(OrderFood food);
//	}
	/**
	 * ���õ���ĳ����Ʒ��Ļص�����
	 * @param foodPickedListener
	 */
	public void setFoodPickedListener(OnFoodPickedListener foodPickedListener){
		mFoodPickedListener = foodPickedListener;
	}
	
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
			//������в���
			mDeptLayout.removeAllViews();
			for(int i=0;i<fragment.mValidDepts.size();i++)
			{
				//������ͼ��
				RelativeLayout view = (RelativeLayout) LayoutInflater.from(fragment.getActivity()).inflate(R.layout.kitchen_fragment_dept_item, null);
				//������ͼ�㲢������ɫ
				final RelativeLayout childView = (RelativeLayout) view.findViewById(R.id.relativeLayout_child_kcFgm);
				childView.setBackgroundResource(R.color.orange);
				//���ø�������
				((TextView)view.findViewById(R.id.textView_kitchenFragment_dept_item)).setText(fragment.mValidDepts.get(i).getName());
				view.setTag(fragment.mValidDepts.get(i));
				//���ø���������
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						//ˢ�³�����ʾ
						PDepartment dept = (PDepartment) v.getTag();
						fragment.mDeptFilter = dept.getId();
						fragment.mKitchenHandler.sendEmptyMessage(REFRESH_FOODS);
						//��ǰһ����������Ϊ����״̬
						if(mDeptLayout.getTag() != null)
						{
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
				//���õ�һ������
				if(i==0)
				{
					mDeptLayout.setTag(childView);
					childView.setBackgroundResource(R.color.gold);
				}
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
			//��������ɸѡ��Ҫ��ʾ�ĳ���
			ArrayList<PKitchen> kitchens = new ArrayList<PKitchen>();
			for(PKitchen k:fragment.mValidKitchens)
			{
				if(k.getDept().getId() == fragment.mDeptFilter){
					kitchens.add(k);
				}
			}
			//ɸѡ����Щ�����а����Ĳ�Ʒ
			ArrayList<Food> foods = new ArrayList<Food>();
			for(Food f:fragment.mOriFoods)
			{
				for(PKitchen k:kitchens)
					if(f.getKitchen().getAliasId() == k.getAliasId())
					{
						foods.add(f);
					}
			}
			//��ɸѡ���Ĳ�Ʒ�����List<List<T>>��ʽ
			ArrayList<List<Food>> tidyFoods = new ArrayList<List<Food>>();
			PKitchen lastKitchen = foods.get(0).getKitchen();
			List<Food> list = new ArrayList<Food>();
			int size = foods.size();
			for(int i=0;i<size;i++)
			{
				Food f = foods.get(i);
				if(f.getKitchen().equals(lastKitchen))
				{
					list.add(f);
				}
				else{
					tidyFoods.add(list);
					list = new ArrayList<Food>();
					lastKitchen = f.getKitchen();
					list.add(f);
				}
				if(i == size - 1)
					tidyFoods.add(list);
			}
			
			for(List<Food> aList : tidyFoods)
			{
				Collections.sort(aList, new Comparator<Food>(){
					@Override
					public int compare(Food lhs, Food rhs) {
						if(lhs.statistics.orderCnt > rhs.statistics.orderCnt)
							return -1;
						else if(lhs.statistics.orderCnt < rhs.statistics.orderCnt)
							return 1;
						else return 0;
					}
				});
			}
			
			fragment.mXpListView.setAdapter(fragment.new KitchenExpandableListAdapter(kitchens, tidyFoods, 3));
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/*
		 * �����в�Ʒ���а�������Ž�������
		 */
		mOriFoods = new Food[WirelessOrder.foodMenu.foods.length];
		System.arraycopy(WirelessOrder.foodMenu.foods, 0, mOriFoods, 0,
				WirelessOrder.foodMenu.foods.length);
		Arrays.sort(mOriFoods, new Comparator<Food>() {
			@Override
			public int compare(Food food1, Food food2) {
				if (food1.getKitchen().getAliasId() > food2.getKitchen().getAliasId()) {
					return 1;
				} else if (food1.getKitchen().getAliasId() < food2.getKitchen().getAliasId()) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		/*
		 * ʹ�ö��ֲ����㷨ɸѡ���в�Ʒ�ĳ���
		 */
		mValidKitchens = new ArrayList<PKitchen>();
		for (int i = 0; i < WirelessOrder.foodMenu.kitchens.length; i++) {
			Food keyFood = new Food();
			keyFood.getKitchen().setAliasId(WirelessOrder.foodMenu.kitchens[i].getAliasId());
			int index = Arrays.binarySearch(mOriFoods, keyFood,
					new Comparator<Food>() {

						public int compare(Food food1, Food food2) {
							if (food1.getKitchen().getAliasId() > food2.getKitchen().getAliasId()) {
								return 1;
							} else if (food1.getKitchen().getAliasId() < food2.getKitchen().getAliasId()) {
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
		 * ɸѡ���в�Ʒ�Ĳ���
		 */
		mValidDepts = new ArrayList<PDepartment>();
		for (int i = 0; i < WirelessOrder.foodMenu.depts.length; i++) {
			for (int j = 0; j < mValidKitchens.size(); j++) {
				if (WirelessOrder.foodMenu.depts[i].getId() == mValidKitchens.get(j).getDept().getId()) {
					mValidDepts.add(WirelessOrder.foodMenu.depts[i]);
					break;
				}
			}
		}
		//��ɸѡ���Ĳ�Ʒ�����List<List<T>>��ʽ
		mPackedValidFoodsList = new ArrayList<List<Food>>();
		PKitchen lastKitchen = mOriFoods[0].getKitchen();
		List<Food> theKitchenList = new ArrayList<Food>();
		for(int i=0;i<mOriFoods.length;i++)
		{
			if(mOriFoods[i].getKitchen().equals(lastKitchen))
			{
				theKitchenList.add(mOriFoods[i]);
			}
			else{
				mPackedValidFoodsList.add(theKitchenList);
				theKitchenList = new ArrayList<Food>();
				lastKitchen = mOriFoods[i].getKitchen();
				theKitchenList.add(mOriFoods[i]);
			}
			if(i == mOriFoods.length-1)
				mPackedValidFoodsList.add(theKitchenList);
		}
		
		
		mDeptFilter = mValidKitchens.get(0).getDept().getId();
		
		mDepartmentHandler = new DepartmentHandler(this);
		mKitchenHandler = new KitchenHandler(this);
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
		mDepartmentHandler.sendEmptyMessage(REFRESH_DEPTS);
		mKitchenHandler.sendEmptyMessage(REFRESH_FOODS);
		return view;
	}
	
	class KitchenExpandableListAdapter extends BaseExpandableListAdapter{
		private ArrayList<ArrayList<ArrayList<Food>>> mChilds;
		private ArrayList<PKitchen> mGroups;
		private int ROW = 4;
		
		public KitchenExpandableListAdapter(ArrayList<PKitchen> groups, List<List<Food>> rowChilds, int row) {
			super();
			mChilds = new ArrayList<ArrayList<ArrayList<Food>>>();
			mGroups = groups;
			ROW = row;
			/*
			 * ��ÿ���ֳ��еĲ�Ʒ�ֳ�ROW��һ�����subChild�У��ٽ����е�subChild����subChilds�У�
			 * ��󽫸�subChilds����mChilds��
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
					if(i == ROW || j == l.size() -1){
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
			
			view.setBackgroundResource(R.drawable.kitchen_fragment_group_selector);
			
			//���ó�����
			((TextView) view.findViewById(R.id.textView_name_kitchenFragment_xp_group_item))
				.setText(mGroups.get(groupPosition).getName());
			//���ó���������
			int size = mChilds.get(groupPosition).size();
			((TextView) view.findViewById(R.id.textView_count_kitchenFragment_xp_group_item))
				.setText("" + ((size - 1) * ROW  + mChilds.get(groupPosition).get(size -1).size()));
			
			return view;
		}

		@Override
		public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild,
				final View convertView, final ViewGroup parent) {
			View view;
			if(convertView != null)
				view = convertView;
			else view = View.inflate(getActivity(), R.layout.kitchen_fragment_xp_listview_child_item, null);
			//���ø��е�gridview
			GridView gridView = (GridView) view.findViewById(R.id.gridView_kitchenFgm_xplv_child_item);
			gridView.setVerticalSpacing(0);
			ArrayList<Food> childFoods = mChilds.get(groupPosition).get(childPosition);
			gridView.setAdapter(new GridAdapter(childFoods, getActivity()));
			//����������
			gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick( AdapterView<?> parent, View view, int position, long id) {
					Food food = (Food) view.getTag();
					if(!food.isSellOut()){
						((TextView)view.findViewById(R.id.textView_sellout_pickFoodFgm_item)).setVisibility(View.GONE);

						new AskOrderAmountDialog(getActivity(), food, mFoodPickedListener, null).show();
					}else{
						Toast.makeText(getActivity(), food.getName() + "������", Toast.LENGTH_SHORT).show();
						((TextView)view.findViewById(R.id.textView_sellout_pickFoodFgm_item)).setVisibility(View.VISIBLE);
					}
				}
			});
			return view;
		}

		@Override
		public boolean isChildSelectable(int groupPosition,
				int childPosition) {
			return false;
		}
	}

//	/*
//	 * ��ʾ������������Dialog
//	 */
//	private class AskOrderAmountDialog extends Dialog{
//
//		private OrderFood mSelectedFood;
//		
//		AskOrderAmountDialog(Food food) {
//			super(getActivity(), R.style.FullHeightDialog);
//			
//			mSelectedFood = new OrderFood(food);
//			 
//			setContentView(R.layout.order_confirm);
//			//����Ŀ�
//			((TextView)findViewById(R.id.orderTitleTxt)).setText("������" + mSelectedFood.name + "�ĵ������");
//			final EditText countEditText = (EditText)findViewById(R.id.amountEdtTxt);
//			countEditText.setText("1");
//			//���ʱȫѡ
//			countEditText.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					countEditText.selectAll();
//				}
//			});
//			//�����Ӱ�ť
//			((ImageButton) findViewById(R.id.button_plus_orderConfirm)).setOnClickListener(new View.OnClickListener(){
//
//				@Override
//				public void onClick(View v) {
//					if(!countEditText.getText().toString().equals(""))
//					{
//						float curNum = Float.parseFloat(countEditText.getText().toString());
//						countEditText.setText(Util.float2String2(++curNum));
//					}
//				}
//			});
//			//��������ť
//			((ImageButton) findViewById(R.id.button_minus_orderConfirm)).setOnClickListener(new View.OnClickListener(){
//
//				@Override
//				public void onClick(View v) {
//					if(!countEditText.getText().toString().equals(""))
//					{
//						float curNum = Float.parseFloat(countEditText.getText().toString());
//						if(--curNum >= 1.0f)
//						{
//							countEditText.setText(Util.float2String2(curNum));
//						}
//					}
//				}
//			});
//			
//			//"ȷ��"Button
//			Button okBtn = (Button)findViewById(R.id.orderConfirmBtn);
//			okBtn.setText("ȷ��");
//			okBtn.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {			
//					onPick(false);
//				}
//			});
//			
//			//"��ζ"Button
//			Button tasteBtn = (Button)findViewById(R.id.orderTasteBtn);
//			tasteBtn.setText("��ζ");
//			tasteBtn.setOnClickListener(new View.OnClickListener() {				
//				@Override
//				public void onClick(View arg0) {
//					onPick(true);
//				}
//			});
//			
//			//"ȡ��"Button
//			Button cancelBtn = (Button)findViewById(R.id.orderCancelBtn);
//			cancelBtn.setText("ȡ��");
//			cancelBtn.setOnClickListener(new View.OnClickListener(){
//				@Override
//				public void onClick(View v) {
//					dismiss();
//				}
//			});
//			
//			//"����"CheckBox
//			CheckBox hurriedChkBox = (CheckBox)findViewById(R.id.orderHurriedChk);
//			hurriedChkBox.setText("����");
//			hurriedChkBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){			
//				@Override
//				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//					if(isChecked){
//						mSelectedFood.hangStatus = OrderFood.FOOD_HANG_UP;
//						Toast.makeText(getActivity(), "����\"" + mSelectedFood.toString() + "\"", Toast.LENGTH_SHORT).show();
//					}else{
//						mSelectedFood.hangStatus = OrderFood.FOOD_NORMAL;
//						Toast.makeText(getActivity(), "ȡ������\"" + mSelectedFood.toString() + "\"", Toast.LENGTH_SHORT).show();
//					}
//					
//				}
//			});
//		}
//		
//		/**
//		 * 
//		 * @param selectedFood
//		 * @param pickTaste
//		 */
//		private void onPick(boolean pickTaste){
//			try{
//				float orderAmount = Float.parseFloat(((EditText)findViewById(R.id.amountEdtTxt)).getText().toString());
//				
//       			if(orderAmount > 255){
//       				Toast.makeText(getActivity(), "�Բ���\"" + mSelectedFood.toString() + "\"���ֻ�ܵ�255��", Toast.LENGTH_SHORT).show();
//       			}else{
//       				mSelectedFood.setCount(orderAmount);
//       				if(mFoodPickedListener != null){	
//       					if(pickTaste){
//       						mFoodPickedListener.onPickedWithTaste(mSelectedFood);
//       					}else{
//       						mFoodPickedListener.onPicked(mSelectedFood);
//       					}
//       				}
//					dismiss();
//       			}
//				
//			}catch(NumberFormatException e){
//				Toast.makeText(getActivity(), "�������������ʽ����ȷ������������", Toast.LENGTH_SHORT).show();
//			}
//		}
//	}
	
	private class GridAdapter extends BaseAdapter{
		private List<Food> mFoods;
		private Context mContext;
		
		public GridAdapter(List<Food> mFoods, Context mContext) {
			super();
			this.mFoods = mFoods;
			this.mContext = mContext;
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
				view = View.inflate(mContext, R.layout.pick_food_fragment_item, null);
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



package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.wireless.common.WirelessOrder;
import com.wireless.fragment.PopTasteFragment.OnTastePickedListener;
import com.wireless.fragment.PopTasteFragment.TasteAdapter;
import com.wireless.parcel.ComboOrderFoodParcel;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.dishesOrder.ComboOrderFood;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.tasteMgr.TasteCategory;
import com.wireless.ui.R;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TasteFragment extends Fragment {
	
	private OrderFood mSelectedFood;
	
	private ComboOrderFood mSelectedCombo;
	
	private OnTastePickedListener mTastePickedListener;
	
	private RefreshCategoryHandler mCategoryHandler;
	
	private RefreshTasteHandler mTasteHanlder;
	
	private static class RefreshTasteHandler extends Handler{
		private WeakReference<TasteFragment> mFragment;

		RefreshTasteHandler(TasteFragment fragment) {
			this.mFragment = new WeakReference<TasteFragment>(fragment);
		}
		
		@Override
		public void handleMessage(Message msg){
			final TasteFragment fragment = mFragment.get();
			
			//根据参数找出对应的TasteCategory
			TasteCategory selectedCategory = null;
			for(TasteCategory category : WirelessOrder.foodMenu.categorys){
				if(category.getId() == msg.what){
					selectedCategory = category;
					break;
				}
			}
			if(selectedCategory == null){
				selectedCategory = WirelessOrder.foodMenu.categorys.get(0);
			}
			
			//帅选出属于选中类型的口味
			List<Taste> tastes = new ArrayList<Taste>();
			for(Taste t : WirelessOrder.foodMenu.tastes){
				if(t.getCategory().equals(selectedCategory)){
					tastes.add(t);
				}
			}
			//显示选中类型的口味
			if(fragment.mSelectedFood != null){
				((GridView)fragment.getView().findViewById(R.id.gridView_taste_allTasteFgm)).setAdapter(
												new TasteAdapter(fragment.mSelectedFood, tastes, fragment.mTastePickedListener));
				
			}else if(fragment.mSelectedCombo != null){
				((GridView)fragment.getView().findViewById(R.id.gridView_taste_allTasteFgm)).setAdapter(
						new TasteAdapter(fragment.mSelectedCombo, tastes, fragment.mTastePickedListener));
			}
			
		}
	}
	
	private static class BuildCategoryHandler extends Handler{
		private WeakReference<TasteFragment> mFragment;

		BuildCategoryHandler(TasteFragment fragment) {
			this.mFragment = new WeakReference<TasteFragment>(fragment);
		}
		
		@Override
		public void handleMessage(Message msg){
			final TasteFragment fragment = mFragment.get();
			LinearLayout categoryLayout = (LinearLayout)fragment.getView().findViewById(R.id.linearLayout_category_allTasteFgm);
			
			//删除所有的口味类型
			categoryLayout.removeAllViews();
			
			for(final TasteCategory category : WirelessOrder.foodMenu.categorys){
				View view = LayoutInflater.from(fragment.getActivity()).inflate(R.layout.pick_taste_by_all_fgm_category_item, categoryLayout, false);

				//设置该项名称
				((TextView)view.findViewById(R.id.txtView_name_allTaste_category_item)).setText(category.getName());
				
				//设置该项侦听器
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						fragment.mCategoryHandler.sendEmptyMessage(category.getId());
					}
				});
				view.setTag(Integer.valueOf(category.getId()));
				categoryLayout.addView(view);
			}
			
			fragment.mCategoryHandler.sendEmptyMessage(msg.what);
		}
	}
	
	private static class RefreshCategoryHandler extends Handler{
		private WeakReference<TasteFragment> mFragment;

		RefreshCategoryHandler(TasteFragment fragment) {
			this.mFragment = new WeakReference<TasteFragment>(fragment);
		}
		
		@Override
		public void handleMessage(Message msg){
			final TasteFragment fragment = mFragment.get();
			
			LinearLayout categoryLayout = (LinearLayout)fragment.getView().findViewById(R.id.linearLayout_category_allTasteFgm);

			for(int i = 0; i < categoryLayout.getChildCount(); i++){
				
				View categoryItemView = categoryLayout.getChildAt(i);
				final View bgView = categoryItemView.findViewById(R.id.txtView_bg_allTaste_category_item);
				
				if(msg.what == ((Integer)categoryItemView.getTag()).intValue()){
					//设置背景颜色
					bgView.setBackgroundResource(R.color.orange);
					//刷新口味GridView
					fragment.mTasteHanlder.sendEmptyMessage(msg.what);
				}else{
					bgView.setBackgroundResource(R.color.gold);
				}
			}
		}
	}
	
	public static TasteFragment newInstance(ComboOrderFood cof){
		TasteFragment fgm = new TasteFragment();
		Bundle bundles = new Bundle();
		bundles.putParcelable(ComboOrderFoodParcel.KEY_VALUE, new ComboOrderFoodParcel(cof));
		fgm.setArguments(bundles);
		return fgm;
	}
	
	public static TasteFragment newInstance(OrderFood orderFood){
		TasteFragment fgm = new TasteFragment();
		Bundle bundles = new Bundle();
		bundles.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(orderFood));
		fgm.setArguments(bundles);
		return fgm;
	}
	
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the TastePickedListener so we can send events to the host
        	mTastePickedListener = (OnTastePickedListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString() + " must implement TastePickedListener");
        }
    }
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCategoryHandler = new RefreshCategoryHandler(this);
		mTasteHanlder = new RefreshTasteHandler(this);
	}
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pick_taste_by_all_fgm, container, false);
		
		OrderFoodParcel orderFoodParcel = getArguments().getParcelable(OrderFoodParcel.KEY_VALUE);
		if(orderFoodParcel != null){
			mSelectedFood = orderFoodParcel.asOrderFood();
		}
		
		ComboOrderFoodParcel comboParcel = getArguments().getParcelable(ComboOrderFoodParcel.KEY_VALUE);
		if(comboParcel != null){
			mSelectedCombo = comboParcel.asComboOrderFood();
		}
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		new BuildCategoryHandler(this).sendEmptyMessage(WirelessOrder.foodMenu.categorys.get(0).getId());
	}
}

package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.R;

public class PopTasteFragment extends Fragment{

	public static interface OnTastePickedListener{
		public void onTastePicked(Taste tasteToPick);
		public void onTasteRemoved(Taste tasteToRemove);
	}
	
	private OnTastePickedListener mTastePickedListener;
	
	public static PopTasteFragment newInstance(OrderFood orderFood){
		PopTasteFragment fgm = new PopTasteFragment();
		Bundle bundles = new Bundle();
		bundles.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(orderFood));
		fgm.setArguments(bundles);
		return fgm;
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the TastePickedListener so we can send events to the host
        	mTastePickedListener = (OnTastePickedListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement TastePickedListener");
        }
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pick_taste_by_pop_fgm, container, false);
		
		OrderFoodParcel orderFroodParcel = getArguments().getParcelable(OrderFoodParcel.KEY_VALUE);
		
		//显示常用口味
		((GridView)view.findViewById(R.id.gridView_tastes_popTastesFgm)).setAdapter(new TasteAdapter(orderFroodParcel.asOrderFood(), mTastePickedListener));
		
		return view;
	}
	
	//常用口味显示的adapter
	static class TasteAdapter extends BaseAdapter{

		List<Taste> mTastes;
		OrderFood mSelectedFood;
		OnTastePickedListener mTastePickedListener;
		
		TasteAdapter(OrderFood of, OnTastePickedListener tastePickedListener){
			mSelectedFood = of;
			mTastePickedListener = tastePickedListener;
			mTastes = new ArrayList<Taste>(of.asFood().getPopTastes());
		}
		
		TasteAdapter(OrderFood of, List<Taste> tastes, OnTastePickedListener tastePickedListener){
			mSelectedFood = of;
			mTastePickedListener = tastePickedListener;
			mTastes = new ArrayList<Taste>(tastes);
		}
		
		@Override
		public int getCount() {
			return mTastes.size();
		}

		@Override
		public Object getItem(int position) {
			return mTastes.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view;
			if(convertView == null){
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pick_taste_fgm_item, null);
			}else{
				view = convertView;
			}

			final Taste taste = mTastes.get(position);
			
			//设置口味的点击事件
			view.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					if(mSelectedFood.addTaste(taste)){
						
						if(mTastePickedListener != null){
							mTastePickedListener.onTastePicked(taste);
						}
					}else if(mSelectedFood.removeTaste(taste)){
						
						if(mTastePickedListener != null){
							mTastePickedListener.onTasteRemoved(taste);
						}
					}
					notifyDataSetChanged();
				}
				
			});
			
			//显示口味名称, 如果字数太长则从10截断
			if(taste.getPreference().length() >= 10){
				((TextView) view.findViewById(R.id.txtView_tasteName_pickTasteFgm_item)).setText(taste.getPreference().substring(0, 10));
			}else{
				((TextView) view.findViewById(R.id.txtView_tasteName_pickTasteFgm_item)).setText(taste.getPreference());
			}

			//显示口味价钱
			if(taste.isCalcByPrice()){
				((TextView) view.findViewById(R.id.txtView_currencySign_pickTasteFgm_item)).setText("￥");
				((TextView) view.findViewById(R.id.txtView_price_pickTasteFgm_item)).setText(NumericUtil.float2String2(taste.getPrice()));
			}else{
				((TextView) view.findViewById(R.id.txtView_currencySign_pickTasteFgm_item)).setText("%");
				((TextView) view.findViewById(R.id.txtView_price_pickTasteFgm_item)).setText(Integer.toString(NumericUtil.float2Int(taste.getRate())));
			}
			
			//显示背景颜色
			view.findViewById(R.id.imgView_bg_pickTasteFgm_item).setBackgroundResource(R.color.green);
			//高亮显示已有口味
			if(mSelectedFood.hasNormalTaste()){
				if(mSelectedFood.getTasteGroup().contains(taste)){
					view.findViewById(R.id.imgView_bg_pickTasteFgm_item).setBackgroundResource(R.color.yellow);
				}
			}
			
			return view;
		}
	}
}

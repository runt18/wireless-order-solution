package com.wireless.fragment;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.wireless.parcel.FoodParcel;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.R;

public class PopTasteFragment extends Fragment{

	public static PopTasteFragment newInstance(Food food){
		PopTasteFragment fgm = new PopTasteFragment();
		Bundle bundles = new Bundle();
		bundles.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(food));
		fgm.setArguments(bundles);
		return fgm;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pick_taste_by_pop_fgm, container, false);
		
		FoodParcel foodParcel = getArguments().getParcelable(FoodParcel.KEY_VALUE);
		
		//显示常用口味
		((GridView)view.findViewById(R.id.gridView_popTastes_pickTasteFragment)).setAdapter(new TasteAdapter(foodParcel.asFood().getPopTastes()));
		
		return view;
	}
	
	//常用口味显示的adapter
	private class TasteAdapter extends BaseAdapter{

		private List<Taste> mPopTastes;

		TasteAdapter(List<Taste> foods){
			mPopTastes = foods;
		}
		
		@Override
		public int getCount() {
			return mPopTastes.size();
		}

		@Override
		public Object getItem(int position) {
			return mPopTastes.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if(convertView == null){
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pick_food_by_pinyin_fgm_item, null);
			}else{
				view = convertView;
			}
			
			Taste food = mPopTastes.get(position);
			view.setTag(food);
			//如果字数太长则从10截断
			if(food.getPreference().length() >= 10){
				((TextView) view.findViewById(R.id.textView_foodName_pickFoodFragment_item)).setText(food.getPreference().substring(0, 10));
			}else{
				((TextView) view.findViewById(R.id.textView_foodName_pickFoodFragment_item)).setText(food.getPreference());
			}

			((TextView) view.findViewById(R.id.textView_price_pickFoodFragment_item)).setText(NumericUtil.float2String2(food.getPrice()));
			
			return view;
		}
	}
}

package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.ui.R;

public class TempFoodFragment extends Fragment {
	
	private TempFoodAdapter mTempFoodAdapter;
	
	private List<Kitchen> mKitchens = new ArrayList<Kitchen>();
	
	private static class ViewHolder{
		TextView kitchenTextView;
		EditText foodNameEditText;
		EditText amountEditText;
		ImageButton deleteBtn;
		
		boolean isInitialized(){
			if(kitchenTextView != null && foodNameEditText!= null && deleteBtn != null && amountEditText!= null)
				return true;
			else return false;
		}
		
		void refresh(OrderFood food){
			if(isInitialized()){
				kitchenTextView.setText(food.kitchen.name);
				foodNameEditText.setText(food.name);
				amountEditText.setText(Util.float2String2(food.getCount()));
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		for(Kitchen kitchen : WirelessOrder.foodMenu.kitchens){
			if(kitchen.isAllowTemp()){
				mKitchens.add(kitchen);
			}
		}
	}
 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.temp_food_fragment, null);
		
		final ListView tempFoodView = (ListView) view.findViewById(R.id.listView_tempFood_fgm) ;
		
		mTempFoodAdapter = new TempFoodAdapter();
		tempFoodView.setAdapter(mTempFoodAdapter);		

		//��Ӱ�ť
		((ImageView) view.findViewById(R.id.add)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mTempFoodAdapter.add();
				//��������view���ɺ��ô��ڵ���
				v.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener(){
					@Override
					public void onGlobalLayout() {
						View view = tempFoodView.getChildAt(mTempFoodAdapter.getCount() - 1).findViewById(R.id.textView_kitchen_tempFood_item);
						if(view.getHeight() > 0){
							view.performClick();
							view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
						}
					}
				});
			}
		});
		return view;
	}

	@Override
	public void onStop() {
		ArrayList<OrderFood> mTempFoods = mTempFoodAdapter.getFoods();
		for(OrderFood f:mTempFoods)
		{
			if(f.name != null && !f.name.equals(""))
			{
   				if(mFoodPickedListener != null){	
					mFoodPickedListener.onPicked(f);
					Log.i("food",f.name);
   				}
			}
		}
		// TODO Auto-generated method stub
		super.onStop();
	}

	private class TempFoodAdapter extends BaseAdapter{
		private ArrayList<OrderFood> mTempFoods;

		TempFoodAdapter() {
			mTempFoods = new ArrayList<OrderFood>();
		}
		
		void add(){
			OrderFood tmpFood = new OrderFood();
			tmpFood.isTemporary = true;
			tmpFood.aliasID = Util.genTempFoodID();
			tmpFood.hangStatus = OrderFood.FOOD_NORMAL;
			tmpFood.kitchen = new Kitchen();
			tmpFood.setCount(1f);
			mTempFoods.add(tmpFood);
			notifyDataSetChanged();
			((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(TempFoodFragment.this.getView().getWindowToken(), 0);
		}
		
		void remove(int position){
			mTempFoods.remove(position);
			notifyDataSetChanged();
		}
		
		ArrayList<OrderFood> getFoods(){
			return mTempFoods;
		}
		@Override
		public int getCount() {
			return mTempFoods.size();
		}

		@Override
		public Object getItem(int position) {
			return mTempFoods.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final OrderFood food = mTempFoods.get(position);

			final ViewHolder holder;
			final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			//��ʼ��view �� holder 
			if(view == null){
				view = inflater.inflate(R.layout.temp_food_item, null);
				holder = new ViewHolder();

				holder.foodNameEditText = (EditText)view.findViewById(R.id.editText_foodName_tempFood_item);
				holder.kitchenTextView = (TextView) view.findViewById(R.id.textView_kitchen_tempFood_item);
				holder.amountEditText = (EditText) view.findViewById(R.id.editText_tempFoodItem_amount);
				holder.deleteBtn = (ImageButton) view.findViewById(R.id.imageButton_delete_tempFood_item);
				view.setTag(holder);
				
			}else{
				holder = (ViewHolder) view.getTag();
			}
			
			//Ĭ�ϳ�ʼ��Ϊ��һ������
//			if(food.kitchen.dept.name == null)
//				food.kitchen.dept = mValidDepts.get(0);
			/**
			 * ������ֵ
			 */
			//������ʱ������ǰɾ���ı��������
			if(holder.foodNameEditText.getTag() != null){
				holder.foodNameEditText.removeTextChangedListener((TextWatcher)holder.foodNameEditText.getTag());
			}
			holder.foodNameEditText.setText(food.name);
			
			FoodNameWatcher nameWatcher = new FoodNameWatcher();
			nameWatcher.setFood(food, position);
			 
			holder.foodNameEditText.setTag(nameWatcher);
			holder.foodNameEditText.addTextChangedListener(nameWatcher);	
			//������ֵ
			if(holder.amountEditText.getTag() != null)
				holder.amountEditText.removeTextChangedListener((TextWatcher)holder.amountEditText.getTag());
			holder.amountEditText.setText(Util.float2String2(food.getCount()));
			
			FoodAmountWatcher amountWatcher = new FoodAmountWatcher();
			amountWatcher.setFood(food, position);
			
			holder.amountEditText.setTag(amountWatcher);
			holder.amountEditText.addTextChangedListener(amountWatcher);
			
			//������ֵ
			holder.kitchenTextView.setTag(holder);
			holder.kitchenTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//���õ�����
					final PopupWindow popWnd = new PopupWindow(
							inflater.inflate(R.layout.temp_food_fragment_popup_window, null),
							140, LayoutParams.WRAP_CONTENT, true){

						@Override
						public void dismiss() {
							if(food.kitchen.name == null)
							{
								food.kitchen = mKitchens.get(0);
								mTempFoods.set(position, food);
								holder.refresh(food);
							}
							super.dismiss();
						}
					};
					popWnd.update();
					popWnd.setOutsideTouchable(true);
					popWnd.setBackgroundDrawable(new BitmapDrawable());
					//����������� 
					ListView popListView = (ListView) popWnd.getContentView();
					popListView.setTag(v);
					popListView.setAdapter(new PopupAdapter(mKitchens));
					
					popListView.setOnItemClickListener(new OnItemClickListener(){
						@Override
						public void onItemClick(AdapterView<?> parent, View view, int p, long id) {
							TextView v = (TextView) parent.getTag();
							Kitchen kitchen = (Kitchen) view.getTag();
							
							ViewHolder holder  = (ViewHolder)v.getTag();
							food.kitchen = kitchen;
							mTempFoods.set(position, food);
							holder.refresh(food);
							popWnd.dismiss();
						}
					});
					//�����ʾ��������������Ϣ
					if(popWnd.isShowing())
						popWnd.dismiss();
					else{
						popWnd.showAsDropDown(v);
					}
				}
			});
			
			//ɾ����ť
			holder.deleteBtn.setTag(position);
			holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = (Integer) v.getTag();
					mTempFoodAdapter.remove(position);
					((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(TempFoodFragment.this.getView().getWindowToken(), 0);

				}
			});
				
			//ˢ��holder����ʾ
			holder.refresh(food);
			
			return view;
		}
		
		class FoodNameWatcher implements TextWatcher{
			private OrderFood mFood;
			private int mPosition;
			
//			public OrderFood getFood() {
//				return mFood;
//			}

			public void setFood(OrderFood mFood, int position) {
				this.mFood = mFood;
				mPosition = position;
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				mFood.name = s.toString().replace(",", ";").replace("��", "��").trim();
				mTempFoods.set(mPosition, mFood);
			}
		}
		
		class FoodAmountWatcher implements TextWatcher{
			private OrderFood mFood;
			private int mPosition;
			
			public void setFood(OrderFood mFood, int position) {
				this.mFood = mFood;
				mPosition = position;
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if(!s.toString().equals(""))
				{
					mFood.setCount(Float.valueOf(s.toString().replace(",", ";").replace("��", "��").trim()));
					mTempFoods.set(mPosition, mFood);
				}
			}
		}
	}
	
	private class PopupAdapter extends BaseAdapter{

		List<Kitchen> mKitchensAllowTemp;
		
		PopupAdapter(List<Kitchen> kitchensAllowTemp){
			mKitchensAllowTemp = kitchensAllowTemp;
		}
		
		@Override
		public int getCount() {
			return mKitchensAllowTemp.size();
		}

		@Override
		public Object getItem(int position) {
			return mKitchensAllowTemp.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;

			if(convertView == null){
				final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.temp_food_fragment_pop_list_item, null);
			}else{
				view = convertView;
			}
			
			Kitchen kitchen = mKitchensAllowTemp.get(position);
			TextView textView = (TextView) view;
			textView.setText(kitchen.name);
			textView.setTag(kitchen);
			
			return view;
		}
	}
	
	private OnFoodPickedListener mFoodPickedListener;

	public static interface OnFoodPickedListener{
		/**
		 * ��PickFoodListViewѡ�в�Ʒ�󣬻ص��˺���֪ͨActivityѡ�е�Food��Ϣ
		 * @param food ѡ��Food����Ϣ
		 */
		public void onPicked(OrderFood food);
		
		/**
		 * ��PickFoodListViewѡ�в�Ʒ�󣬻ص��˺���֪ͨActivityѡ�е�Food��Ϣ������ת����ζActivity
		 * @param food
		 * 			ѡ��Food����Ϣ
		 */
		public void onPickedWithTaste(OrderFood food);
	}
	/**
	 * ���õ���ĳ����Ʒ��Ļص�����
	 * @param foodPickedListener
	 */
	public void setFoodPickedListener(OnFoodPickedListener foodPickedListener){
		mFoodPickedListener = foodPickedListener;
	}
	
}

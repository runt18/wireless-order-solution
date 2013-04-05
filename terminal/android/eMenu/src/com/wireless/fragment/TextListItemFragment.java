package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.excep.ProtocolException;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.util.NumericUtil;
import com.wireless.util.imgFetcher.ImageFetcher;

/**
 * the {@link Fragment} for each page of {@link TextListFragment}, it will separate source food list into two 
 * list and displaying it at one list. more information, see {@link SubListAdapter}
 * 
 * @author ggdsn1
 * @see SubListAdapter
 */
public class TextListItemFragment extends ListFragment {
	private static final String DATA_SOURCE_FOODS = "dataSourceFoods";
	private static final String DATA_PARENT_TAG = "data_parent_id";
	
	public static Fragment newInstance(List<OrderFood> list, String parentTag) {
		TextListItemFragment fgm = new TextListItemFragment();
		
		Bundle args = new Bundle();
		
		ArrayList<FoodParcel> foodParcels = new ArrayList<FoodParcel>();
		for(OrderFood f: list){
			foodParcels.add(new FoodParcel(f));
		}
		args.putParcelableArrayList(DATA_SOURCE_FOODS, foodParcels);
		args.putString(DATA_PARENT_TAG, parentTag);
		fgm.setArguments(args);

		return fgm;
	}

	private TextListFragment mParentFragment;

	/**
	 * this will carry all {@link FoodParcel} and split one list into two.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.text_list_fgm_item, container, false);

		try{
			mParentFragment = (TextListFragment) getFragmentManager().findFragmentByTag(getArguments().getString(DATA_PARENT_TAG));
		}
		catch (ClassCastException e){
			
		}

		if(mParentFragment != null){
	    	ArrayList<FoodParcel> foodParcels = getArguments().getParcelableArrayList(DATA_SOURCE_FOODS);
	    	
	    	//将所有菜品一分为二，拆成左右俩个列表
	    	int middleCount = foodParcels.size() / 2;
	    	if(foodParcels.size() % 2 != 0)
	    		middleCount++;
	    	
	    	ArrayList<ArrayList<OrderFood>> result = new ArrayList<ArrayList<OrderFood>>();
	    	ArrayList<OrderFood> leftList = new ArrayList<OrderFood>();
	    	ArrayList<OrderFood> rightList = new ArrayList<OrderFood>();
	
	    	for (int i = 0; i < middleCount; i++) {
				FoodParcel foodParcel = foodParcels.get(i);
				leftList.add(foodParcel);
			}
	    	for(int i= middleCount; i < foodParcels.size(); i++){
	    		rightList.add(foodParcels.get(i));
	    	}
	    	result.add(leftList);
	    	result.add(rightList);
	    	
	    	//将整理后的结果传给adapter
			setListAdapter(new SubListAdapter(getActivity(), result, mParentFragment.getImageFetcher()));
		}
		return layout;
	}
	
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}


	/**
	 * 设置高亮的菜品，将对应的list项高亮
	 * @param food
	 */
	public void setFoodHighLight(Food food){
		getListView().requestFocusFromTouch();
		
		SubListAdapter adapter = (SubListAdapter) this.getListAdapter();
		List<ArrayList<OrderFood>> list = adapter.getList();
		for(ArrayList<OrderFood> subList: list){
			for (int i = 0; i < subList.size(); i++) {
				OrderFood f = subList.get(i);
				if(f.getAliasId() == food.getAliasId()){
					getListView().setSelection(i);
					return;
				}
			}
		}
	}
}

/**
 * the adapter to show text list<br/>
 * it will currently show two food in an item. the left list will decide the length of this list
 * <br/>
 * And if the last of right list is nothing, it wouldn't be display
 * @author ggdsn1
 *
 */
class SubListAdapter extends BaseAdapter{
	private Context mContext;
	private List<ArrayList<OrderFood>> mList;
	private ImageFetcher mImageFetcher;
	
	public SubListAdapter(Context mContext, ArrayList<ArrayList<OrderFood>> result, ImageFetcher fetcher) {
		super();
		this.mContext = mContext;
		this.mList = result;
		mImageFetcher = fetcher;
	}

	/**
	 * it will display food on the left and food on the right 
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View layout = convertView;
		if(layout == null)
			layout = LayoutInflater.from(mContext).inflate(R.layout.food_list_fgm_item_subitem, null);
		
		//设置第一个菜品
		OrderFood food1 = mList.get(0).get(position); 
		//菜名
		if(food1.getName().length() > 12) 
			((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_name1)).setText(food1.getName().substring(0,9));
		else ((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_name1)).setText(food1.getName());
		//价格显示
		((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_price1)).setText(NumericUtil.float2String2(food1.getPrice()));
		//点菜按钮
		Button addBtn1 = (Button)layout.findViewById(R.id.button_foodListFgm_item_subItem_add1);
		addBtn1.setTag(food1);
		addBtn1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					OrderFood food = (OrderFood)v.getTag();
					food.setCount(1f);
					ShoppingCart.instance().addFood(food);
					Toast.makeText(mContext, food.getName() + "一份，已添加", Toast.LENGTH_SHORT).show();
				} catch (ProtocolException e) {
					Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
		if(food1.image != null){
			mImageFetcher.loadImage(food1.image, ((ImageView)layout.findViewById(R.id.imageView_foodListFgm_item_subItem1)));
		} 
		
		//判断第二个菜品是否存在，若存在则设置相关项，否则隐藏按钮
		OrderFood food2 = null;
		try{
			food2 = mList.get(1).get(position);
		} catch(IndexOutOfBoundsException e){
			
		}
		if(food2 != null){
			layout.findViewById(R.id.relativeLayout_TextListItemFgm_subItem2).setVisibility(View.VISIBLE);
			if(food2.getName().length() > 12)
				((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_name2)).setText(food2.getName().substring(0,9));
			else ((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_name2)).setText(food2.getName());
			
			((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_price2)).setText(NumericUtil.float2String2(food2.getPrice()));
			Button addBtn2  = (Button) layout.findViewById(R.id.button_foodListFgm_item_subItem_add2);
			addBtn2.setTag(food2);
			addBtn2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						OrderFood food = (OrderFood)v.getTag();
						food.setCount(1f);
						ShoppingCart.instance().addFood(food);
						Toast.makeText(mContext, food.getName() + "一份，已添加", Toast.LENGTH_SHORT).show();
					} catch (ProtocolException e) {
						Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				}
			});
			if(food2.image != null){
				mImageFetcher.loadImage(food2.image, ((ImageView)layout.findViewById(R.id.imageView_foodListFgm_item_subItem2)));
			}
		} else {
			layout.findViewById(R.id.relativeLayout_TextListItemFgm_subItem2).setVisibility(View.GONE);
		}
		return layout;
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public Object getItem(int position) {
		return mList.get(0).get(position);
	}
	
	@Override
	public int getCount() {
		return mList.get(0).size();
	}

	public List<ArrayList<OrderFood>> getList() {
		return mList;
	}
	
	
}
package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.util.imgFetcher.ImageFetcher;

public class TextListItemFragment extends Fragment {
	private static final String DATA_SOURCE_FOODS = "dataSourceFoods";
	private static final String DATA_PARENT_ID = "data_parent_id";
	
	public static Fragment newInstance(List<OrderFood> list, int parentId, int countPerList) {
		TextListItemFragment fgm = new TextListItemFragment();
		
		Bundle args = new Bundle();
		
		ArrayList<FoodParcel> foodParcels = new ArrayList<FoodParcel>();
		for(OrderFood f: list){
			foodParcels.add(new FoodParcel(f));
		}
		args.putParcelableArrayList(DATA_SOURCE_FOODS, foodParcels);
		args.putInt(DATA_PARENT_ID, parentId);
		fgm.setArguments(args);
		
		return fgm;
	}

	private TextListFragment mParentFragment;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.food_list_fgm_item, null);
		
		Bundle args = getArguments();
		int parentId = args.getInt(DATA_PARENT_ID);
		mParentFragment = (TextListFragment) getFragmentManager().findFragmentById(parentId);
		
    	ArrayList<FoodParcel> foodParcels = args.getParcelableArrayList(DATA_SOURCE_FOODS);
    	ArrayList<OrderFood> srcFoods = new ArrayList<OrderFood>();
    	for(FoodParcel foodParcel : foodParcels){
    		srcFoods.add(foodParcel);
    	}
    	
    	GridView gridView = (GridView) layout.findViewById(R.id.gridView_foodListFgm_item);
    	gridView.setAdapter(new SubListAdapter(getActivity(), srcFoods, mParentFragment.getImageFetcher()));
		return layout;
	}
}
class SubListAdapter extends BaseAdapter{
	private Context mContext;
	private List<OrderFood> mList;
	private ImageFetcher mImageFetcher;
	
	public SubListAdapter(Context mContext, List<OrderFood> rightList, ImageFetcher fetcher) {
		super();
		this.mContext = mContext;
		this.mList = rightList;
		mImageFetcher = fetcher;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View layout = LayoutInflater.from(mContext).inflate(R.layout.food_list_fgm_item_subitem, null);
		Food food = mList.get(position);
		
		((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_name)).setText(food.name);
		((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_price)).setText(Util.float2String2(food.getPrice()));
		if(food.image != null){
			mImageFetcher.loadImage(food.image, ((ImageView)layout.findViewById(R.id.imageView_foodListFgm_item_subItem)));
		}
		//TODO 设置标签显示
		return layout;
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}
}
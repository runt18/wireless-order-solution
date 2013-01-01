package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.util.imgFetcher.ImageFetcher;

public class TextListItemFragment extends ListFragment {
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
	private AsyncTask<Void, Void, ArrayList<ArrayList<OrderFood>>> mViewTask;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.text_list_fgm_item, container, false);

		mParentFragment = (TextListFragment) getFragmentManager().findFragmentById(getArguments().getInt(DATA_PARENT_ID));

		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mViewTask = new AsyncTask<Void, Void, ArrayList<ArrayList<OrderFood>>>() {

			@Override
			protected ArrayList<ArrayList<OrderFood>> doInBackground(Void... params) {
		    	ArrayList<FoodParcel> foodParcels = getArguments().getParcelableArrayList(DATA_SOURCE_FOODS);
		    	
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
		    	return result;
			}

			@Override
			protected void onPostExecute(ArrayList<ArrayList<OrderFood>> result) {
				super.onPostExecute(result);
				View layout = getView();
				if(layout != null){
					setListAdapter(new SubListAdapter(getActivity(), result, mParentFragment.getImageFetcher()));
				}
			}
		}.execute();
	}

	@Override
	public void onDestroy() {
		mViewTask.cancel(true);
		super.onDestroy();
	}
	
}
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View layout = convertView;
		if(layout == null)
			layout = LayoutInflater.from(mContext).inflate(R.layout.food_list_fgm_item_subitem, null);
		
		Food food1 = mList.get(0).get(position);
		
		((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_name1)).setText(food1.name);
		((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_price1)).setText(Util.float2String2(food1.getPrice()));
		if(food1.image != null){
			mImageFetcher.loadImage(food1.image, ((ImageView)layout.findViewById(R.id.imageView_foodListFgm_item_subItem1)));
		} 
		Food food2 = null;
		try{
			food2 = mList.get(1).get(position);
		} catch(IndexOutOfBoundsException e){
			
		}
		if(food2 != null){
			layout.findViewById(R.id.relativeLayout_TextListItemFgm_subItem2).setVisibility(View.VISIBLE);
			((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_name2)).setText(food2.name);
			((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_price2)).setText(Util.float2String2(food2.getPrice()));
			if(food2.image != null){
				mImageFetcher.loadImage(food2.image, ((ImageView)layout.findViewById(R.id.imageView_foodListFgm_item_subItem2)));
			}
		} else {
			layout.findViewById(R.id.relativeLayout_TextListItemFgm_subItem2).setVisibility(View.GONE);
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
		return mList.get(0).get(position);
	}
	
	@Override
	public int getCount() {
		return mList.get(0).size();
	}
}
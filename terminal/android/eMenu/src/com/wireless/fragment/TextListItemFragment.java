package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
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
import com.wireless.exception.BusinessException;
import com.wireless.fragment.SubListAdapter.ListItem;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodUnit;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.util.imgFetcher.ImageFetcher;

/**
 * the {@link Fragment} for each page of {@link TextListFragment}, it will separate source food list into two 
 * list and displaying it at one list. more information, see {@link SubListAdapter}
 * 
 * @author ggdsn1
 * @see SubListAdapter
 */
public class TextListItemFragment extends ListFragment {
	private static final String DATA_PARENT_TAG = "data_parent_id";
	
	public static Fragment newInstance(List<Food> foodList, String parentTag) {
		TextListItemFragment fgm = new TextListItemFragment();
		
		Bundle args = new Bundle();
		
		ArrayList<FoodParcel> foodParcels = new ArrayList<FoodParcel>();
		for(Food f: foodList){
			foodParcels.add(new FoodParcel(f));
		}
		args.putParcelableArrayList(FoodParcel.KEY_VALUE, foodParcels);
		args.putString(DATA_PARENT_TAG, parentTag);
		fgm.setArguments(args);

		return fgm;
	}

	private TextListFragment mParentFragment;

	/**
	 * this will carry all {@link OrderFoodParcel} and split one list into two.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.fragment_text_list_item, container, false);

		try{
			mParentFragment = (TextListFragment) getFragmentManager().findFragmentByTag(getArguments().getString(DATA_PARENT_TAG));
		}
		catch (ClassCastException e){
			
		}

		if(mParentFragment != null){
	    	ArrayList<FoodParcel> foodParcels = getArguments().getParcelableArrayList(FoodParcel.KEY_VALUE);
	    	
	    	//将所有菜品一分为二，拆成左右俩个列表
	    	int middleCount = foodParcels.size() / 2;
	    	if(foodParcels.size() % 2 != 0)
	    		middleCount++;
	    	
	    	ArrayList<Food> leftList = new ArrayList<Food>();
	    	ArrayList<Food> rightList = new ArrayList<Food>();
	
	    	for(int i = 0; i < middleCount; i++) {
				FoodParcel foodParcel = foodParcels.get(i);
				leftList.add(foodParcel.asFood());
			}
	    	for(int i = middleCount; i < foodParcels.size(); i++){
	    		rightList.add(foodParcels.get(i).asFood());
	    	}
	    	
	    	//将整理后的结果传给adapter
			setListAdapter(new SubListAdapter(getActivity(), leftList, rightList, mParentFragment.getImageFetcher()));
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
	void setHighLightedByFood(Food food){
		getListView().requestFocusFromTouch();
		
		SubListAdapter adapter = (SubListAdapter) this.getListAdapter();
		int row = 0;
		for(ListItem item : adapter.getItems()){
			if(food.equals(item.getLeft()) || food.equals(item.getRight())){
				getListView().setSelection(row);
				return;
			}
			row++;
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
	
	static class ListItem{
		
		private final Food left;
		private final Food right;
		
		ListItem(Food left, Food right){
			this.left = left;
			this.right = right;
		}
		
		ListItem(Food left){
			this.left = left;
			this.right = null;
		}
		
		Food getLeft(){
			return this.left;
		}
		
		Food getRight(){
			return this.right;
		}
	}
	
	private Context mContext;
	private List<ListItem> mItems = new ArrayList<ListItem>();
	private ImageFetcher mImageFetcher;
	
	public SubListAdapter(Context context, List<Food> leftList, List<Food> rightList, ImageFetcher fetcher) {
		super();
		this.mContext = context;
		for(int i = 0; i < leftList.size(); i++){
			if(i >= rightList.size()){
				mItems.add(new ListItem(leftList.get(i)));
			}else{
				mItems.add(new ListItem(leftList.get(i), rightList.get(i)));
			}
		}
		this.mImageFetcher = fetcher;
	}

	/**
	 * it will display food on the left and food on the right 
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View layout;
		if(convertView == null){
			layout = LayoutInflater.from(mContext).inflate(R.layout.food_list_fgm_item_subitem, parent, false);
		}else{
			layout = convertView;
		}
		
		//设置左边菜品
		final Food leftFood = mItems.get(position).left;
		if(leftFood != null){
			//菜名
			if(leftFood.getName().length() > 12) {
				((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_name1)).setText(leftFood.getName().substring(0,9));
			}else{
				((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_name1)).setText(leftFood.getName());
			}
			//价格显示
			if(leftFood.hasFoodUnit()){
				((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_price1)).setText("多单位");
			}else{
				((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_price1)).setText(NumericUtil.float2String2(leftFood.getPrice()));
			}
			//是否停售
			if(leftFood.isSellOut()){
				layout.findViewById(R.id.imageView_sellOut_foodListFgm_subItem_left).setVisibility(View.VISIBLE);
			}else{
				layout.findViewById(R.id.imageView_sellOut_foodListFgm_subItem_left).setVisibility(View.GONE);
			}
			//点菜按钮
			Button addBtnLeft = (Button)layout.findViewById(R.id.button_foodListFgm_item_subItem_add1);
			addBtnLeft.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						final OrderFood of = new OrderFood(leftFood);
						of.setCount(1f);
						if(leftFood.hasFoodUnit()){
							List<String> items = new ArrayList<String>();
							for(FoodUnit unit : leftFood.getFoodUnits()){
								items.add(unit.toString());
							}
							new AlertDialog.Builder(mContext).setTitle(of.getName())
							   .setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog, int which) {
									try {
										of.setFoodUnit(leftFood.getFoodUnits().get(which));
										ShoppingCart.instance().addFood(of);
										Toast.makeText(mContext, of.getName() + "一份，已添加", Toast.LENGTH_SHORT).show();
									} catch (BusinessException e) {
										Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
									}
								}
							}).setNegativeButton("返回", null).show();
							
						}else{
							ShoppingCart.instance().addFood(of);
							Toast.makeText(mContext, of.getName() + "一份，已添加", Toast.LENGTH_SHORT).show();
						}
					} catch (BusinessException e) {
						Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				}
			});
			if(leftFood.hasImage()){
				mImageFetcher.loadImage(leftFood.getImage().getImage(), ((ImageView)layout.findViewById(R.id.imageView_foodListFgm_item_subItem1)));
			} 
		}
		
		
		//设置右边菜品
		final Food foodRight = mItems.get(position).right;
		//判断第二个菜品是否存在，若存在则设置相关项，否则隐藏按钮
		if(foodRight != null){
			
			layout.findViewById(R.id.relativeLayout_TextListItemFgm_subItem2).setVisibility(View.VISIBLE);
			if(foodRight.getName().length() > 12){
				((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_name2)).setText(foodRight.getName().substring(0,9));
			}else{
				((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_name2)).setText(foodRight.getName());
			}
			
			if(foodRight.hasFoodUnit()){
				((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_price2)).setText("多单位");
			}else{
				((TextView)layout.findViewById(R.id.textView_foodListFgm_item_subItem_price2)).setText(NumericUtil.float2String2(foodRight.getPrice()));
			}
			
			//是否停售
			if(foodRight.isSellOut()){
				layout.findViewById(R.id.imageView_sellOut_foodListFgm_subItem_right).setVisibility(View.VISIBLE);
			}else{
				layout.findViewById(R.id.imageView_sellOut_foodListFgm_subItem_right).setVisibility(View.GONE);
			}
			
			Button addBtnRight  = (Button) layout.findViewById(R.id.button_foodListFgm_item_subItem_add2);
			addBtnRight.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						OrderFood rightOrderFood = new OrderFood(foodRight);
						rightOrderFood.setCount(1f);
						ShoppingCart.instance().addFood(rightOrderFood);
						Toast.makeText(mContext, rightOrderFood.getName() + "一份，已添加", Toast.LENGTH_SHORT).show();
					} catch (BusinessException e) {
						Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				}
			});
			if(foodRight.hasImage()){
				mImageFetcher.loadImage(foodRight.getImage().getImage(), ((ImageView)layout.findViewById(R.id.imageView_foodListFgm_item_subItem2)));
			}
			
		}else {
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
		return mItems.get(position);
	}
	
	@Override
	public int getCount() {
		return mItems.size();
	}

	public List<ListItem> getItems(){
		return this.mItems;
	}
}
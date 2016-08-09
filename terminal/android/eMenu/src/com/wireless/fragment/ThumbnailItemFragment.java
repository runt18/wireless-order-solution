package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.exception.BusinessException;
import com.wireless.lib.task.CommitOrderTask;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodUnit;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.FoodDetailActivity;

public class ThumbnailItemFragment extends ListFragment {

	private static final String KEY_PARENT_FGM_TAG = "data_parent_id";

	private ThumbnailFragment mParentFragment;

//	private View mThePickedView;
//	private boolean mIsLeft = true;

	public static ThumbnailItemFragment newInstance(List<Food> srcFoods, String parentTag) {
		ThumbnailItemFragment fgm = new ThumbnailItemFragment();
		Bundle args = new Bundle();

		ArrayList<FoodParcel> foodParcels = new ArrayList<FoodParcel>();
		for (Food f : srcFoods) {
			foodParcels.add(new FoodParcel(f));
		}
		args.putParcelableArrayList(FoodParcel.KEY_VALUE, foodParcels);
		args.putString(KEY_PARENT_FGM_TAG, parentTag);
		fgm.setArguments(args);
		return fgm;
	}

	/**
	 * this method will create a layout for per page, it will separate source
	 * foods into two list and set an {@link FoodAdapter}
	 * 
	 * /　\./　\/\_　　 I Hand You 　 __{^\_ _}_　 )　}/^\　　　 A Rose...
	 * 　/　/\_/^\._}_/　//　/ (　(__{(@)}\__}.//_/__A___A______A_______A______A____
	 * 　\__/{/(_)\_}　)\\ \\---v----V-----V--Y----v---Y----- 　　(　 (__)_)_/　)\
	 * \>　　 　　 \__/　　 \__/\/\/ 　　　　\__,--'　　　　
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View layout = inflater.inflate(R.layout.fragment_text_list_item, container, false);

		Bundle args = getArguments();
		String parentTag = args.getString(KEY_PARENT_FGM_TAG);

		try {
			mParentFragment = (ThumbnailFragment) getFragmentManager()
					.findFragmentByTag(parentTag);
		} catch (ClassCastException e) {

		}

		if (mParentFragment != null) {

			List<FoodParcel> foodParcels = args.getParcelableArrayList(FoodParcel.KEY_VALUE);
			int middleCount = foodParcels.size() / 2;
			if (foodParcels.size() % 2 != 0){
				middleCount++;
			}

			List<Food> leftList = new ArrayList<Food>();
			List<Food> rightList = new ArrayList<Food>();

			for (int i = 0; i < middleCount; i++) {
				leftList.add(foodParcels.get(i).asFood());
			}

			for (int i = middleCount; i < foodParcels.size(); i++) {
				rightList.add(foodParcels.get(i).asFood());
			}

			setListAdapter(new FoodAdapter(getActivity(), leftList, rightList));

		}
		return layout;
	}

//	@Override
//	public void onStart() {
//		super.onStart();
//
//		if (mThePickedView != null) {
//			refreshDisplay((OrderFood) mThePickedView.getTag(), mThePickedView, mIsLeft);
//		}
//	}

	/**
	 * it will show two food in an item at once,<br/>
	 * the length is decide by the left list.
	 * 
	 * @author ggdsn1
	 * 
	 */
	class FoodAdapter extends BaseAdapter {

		private class ListItem {
			private final Food left;
			private final Food right;

			ListItem(Food left, Food right) {
				this.left = left;
				this.right = right;
			}

			ListItem(Food left) {
				this.left = left;
				this.right = null;
			}

			Food getLeft() {
				return this.left;
			}

			Food getRight() {
				return this.right;
			}
		}

		private final List<ListItem> mItems = new ArrayList<ListItem>();

		FoodAdapter(Context context, List<Food> leftList, List<Food> rightList){
			
			for(int i = 0; i < leftList.size(); i++){
				if(i >= rightList.size()){
					mItems.add(new ListItem(leftList.get(i)));
				}else{
					mItems.add(new ListItem(leftList.get(i), rightList.get(i)));
				}
			}
		}
		
		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public Object getItem(int position) {
			return mItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View layout;

			if (convertView == null) {
				layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.thumbnail_fgm_item, parent, false);
			}else{
				layout = convertView;
			}
			
			final Food leftFood = mItems.get(position).left;

			if(leftFood != null){
				// 显示菜品图片
				ImageView foodImage = (ImageView) layout.findViewById(R.id.imageView_thumbnailFgm_item_foodImg1);
				foodImage.setScaleType(ScaleType.CENTER_CROP);
				mParentFragment.getImageFetcher().loadImage(leftFood.getImage().getImage(), foodImage);

				if(leftFood.isSellOut()){
					layout.findViewById(R.id.imageView_sellOut_thumbnailFgm_item_add1).setVisibility(View.VISIBLE);
				}else{
					layout.findViewById(R.id.imageView_sellOut_thumbnailFgm_item_add1).setVisibility(View.GONE);
				}
				
				// 点菜按钮
				((Button)layout.findViewById(R.id.button_thumbnailFgm_item_add1)).setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						//TODO
						final OrderFood of = new OrderFood(leftFood);
						of.setCount(1f);
						showDialog(of, layout);
						
					}
				});
				
				// 菜品详情
				Button detailBtn = (Button) layout.findViewById(R.id.button_thumbnailFgm_item_detail1);
				detailBtn.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getActivity(), FoodDetailActivity.class);
						Bundle bundle = new Bundle();

						bundle.putParcelable(OrderFoodParcel.KEY_VALUE,	new OrderFoodParcel(new OrderFood(leftFood)));
						intent.putExtras(bundle);
						startActivity(intent);
					}
				});

				refreshDisplay(leftFood, layout, true);
			}


			final Food rightFood = mItems.get(position).right;

			if (rightFood != null) {
				layout.findViewById(R.id.relativeLayout_thumbnailFgm_item_foodImg2).setVisibility(View.VISIBLE);
				// 显示菜品图片
				ImageView rightFoodImgView = (ImageView) layout.findViewById(R.id.imageView_thumbnailFgm_item_foodImg2);
				rightFoodImgView.setScaleType(ScaleType.CENTER_CROP);
				mParentFragment.getImageFetcher().loadImage(rightFood.getImage().getImage(), rightFoodImgView);

				if(leftFood.isSellOut()){
					layout.findViewById(R.id.imageView_sellOut_thumbnailFgm_item_add2).setVisibility(View.VISIBLE);
				}else{
					layout.findViewById(R.id.imageView_sellOut_thumbnailFgm_item_add2).setVisibility(View.GONE);
				}
				
				// 点菜按钮
				((Button)layout.findViewById(R.id.button_thumbnailFgm_item_add2)).setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						//TODO
						OrderFood of = new OrderFood(rightFood);
						of.setCount(1f);
						showDialog(of, layout);
					}
				});

				// 菜品详情
				Button rightDetailBtn = (Button) layout.findViewById(R.id.button_thumbnailFgm_item_detail2);
				rightDetailBtn.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getActivity(), FoodDetailActivity.class);
						Bundle bundle = new Bundle();

						bundle.putParcelable(OrderFoodParcel.KEY_VALUE,	new OrderFoodParcel(new OrderFood(rightFood)));
						intent.putExtras(bundle);
						startActivity(intent);
					}
				});
				
				refreshDisplay(rightFood, layout, false);
				
			} else {
				layout.findViewById(R.id.relativeLayout_thumbnailFgm_item_foodImg2).setVisibility(View.GONE);
			}

			return layout;
		}

		public List<ListItem> getItems() {
			return this.mItems;
		}
		
		private void showDialog(final OrderFood of, final View layout){
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity())
				.setTitle(of.getName())   
	            .setNegativeButton("直接下单", new DialogInterface.OnClickListener() {   
	                @Override   
	                public void onClick(DialogInterface dialog, int which) {
	                	try {
	                		if(!ShoppingCart.instance().hasTable()){
	                			throw new BusinessException("您还未设置餐台，暂时不能提交");
	                		}else if(!ShoppingCart.instance().hasStaff()){
	                			throw new BusinessException("您还未设置服务员，暂时不能提交");
	                		}
	                		
	                		new CommitOrderTask(ShoppingCart.instance().getStaff() 
	                							,new Order.InsertBuilder(new Table.Builder(ShoppingCart.instance().getDestTable().getId()))
	                									  .add(of, ShoppingCart.instance().getStaff())
	                									  .setCustomNum(ShoppingCart.instance().getDestTable().getCustomNum())
	                									  .setForce(true)
	                							,PrintOption.DO_PRINT) {
								
	                			private ProgressDialog mProgressDialog;
	                			
	                			@Override
	                			protected void onPreExecute(){
	                				mProgressDialog = ProgressDialog.show(getActivity(), "", "正在执行下单...请稍候");
	                			}	
	                			
								@Override
								protected void onSuccess(Order order) {
									mProgressDialog.dismiss();		
									Toast.makeText(getActivity(), "直接下单成功", Toast.LENGTH_SHORT).show();
								}
								
								@Override
								protected void onFail(BusinessException e, Order order) {
									mProgressDialog.dismiss();
									Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
								}
							}.execute();
	                		
						} catch (BusinessException e) {
							Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
						}
	                }   
	            })  
	            .setNeutralButton("加入购物车", new DialogInterface.OnClickListener() {   
	                @Override   
	                public void onClick(DialogInterface dialog, int which) {   
						try {
							ShoppingCart.instance().addFood(of);
							refreshDisplay(of.asFood(), layout, true);
							
						} catch (BusinessException e) {
							Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
						}
	                }   
	            })
	            .setPositiveButton("取消", null);
			
			if(of.asFood().hasFoodUnit()){
				final List<String> items = new ArrayList<String>();
				for(FoodUnit unit : of.asFood().getFoodUnits()){
					items.add(unit.toString());
				}
				of.setFoodUnit(of.asFood().getFoodUnits().get(0));
				
				dialogBuilder.setSingleChoiceItems(items.toArray(new String[items.size()]), 0, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						of.setFoodUnit(of.asFood().getFoodUnits().get(which));
					}
				});
			}
			
			dialogBuilder.show();
		}
	}

	/**
	 * 更改菜品的显示
	 */
	private void refreshDisplay(Food foodToDisplay, View layout, boolean isLeft) {
		
		//Check to whether the order is ordered before.
		//If yes, update the order amount.
		OrderFood foodHasOrdered = ShoppingCart.instance().searchInNew(foodToDisplay);
		float orderAmount;
		if(foodHasOrdered != null){
			orderAmount = foodHasOrdered.getCount();
		}else{
			orderAmount = 0;
		}
		
		if (isLeft) {

			// 点菜数量
			if (orderAmount != 0f) {
				((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_pickedCount1)).setText(NumericUtil.float2String2(orderAmount));
			} else {
				((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_pickedCount1)).setText("");
			}

			// 菜品价钱
			if(foodToDisplay.hasFoodUnit()){
				((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_price1)).setText("多单位");
				layout.findViewById(R.id.textView_combo_name).setVisibility(View.INVISIBLE);
			}else{
				layout.findViewById(R.id.textView_combo_name).setVisibility(View.VISIBLE);
				((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_price1)).setText(NumericUtil.float2String2(foodToDisplay.getPrice()));
			}
			// 菜品名称
			((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_foodName1)).setText(foodToDisplay.getName());

		} else {
			
			// 点菜数量
			if(orderAmount != 0f) {
				((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_pickedCount2)).setText(NumericUtil.float2String2(orderAmount));
			} else {
				((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_pickedCount2)).setText("");
			}
			
			// 菜品价钱
			((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_price2)).setText(NumericUtil.float2String2(foodToDisplay.getPrice()));

			// 菜品名称
			((TextView) layout.findViewById(R.id.textView_thumbnailFgm_item_foodName2)).setText(foodToDisplay.getName());
		}
	}

	/**
	 * {@link OnClickListener} about add food button
	 * 
	 * @author ggdsn1
	 * 
	 */
	class AddDishOnClickListener implements OnClickListener {
		private View mLayout;
		private boolean isLeft;

		public AddDishOnClickListener(View layout, boolean isLeft) {
			super();
			this.mLayout = layout;
			this.isLeft = isLeft;
		}

		@Override
		public void onClick(View v) {
			Food food = (Food)v.getTag();
			if (food != null) {
				OrderFood of = new OrderFood(food);
				of.setCount(1f);
				try {
					ShoppingCart.instance().addFood(of);
					refreshDisplay(of.asFood(), mLayout, isLeft);
				} catch (BusinessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * click listener about food detail button
	 * 
	 * @author ggdsn1
	 * 
	 */
//	class DetailOnClickListener implements OnClickListener {
//		View mLayout;
//		private boolean isLeft = true;
//
//		public DetailOnClickListener(View mLayout, boolean isLeft) {
//			super();
//			this.mLayout = mLayout;
//			this.isLeft = isLeft;
//		}
//
//		public void setLayout(View mLayout) {
//			this.mLayout = mLayout;
//		}
//
//		@Override
//		public void onClick(View v) {
//			OrderFood food = (OrderFood) v.getTag();
//			if (food != null) {
//				Intent intent = new Intent(getActivity(), FoodDetailActivity.class);
//				Bundle bundle = new Bundle();
//
//				bundle.putParcelable(OrderFoodParcel.KEY_VALUE,	new OrderFoodParcel(food));
//				intent.putExtras(bundle);
//				mLayout.setTag(food);
//				mThePickedView = mLayout;
//				mIsLeft = isLeft;
//				startActivity(intent);
//			}
//		}
//	}

	/**
	 * High light the target food, if found
	 * @param food
	 */
	public void setHighLightedByFood(Food food) {
		getListView().requestFocusFromTouch();

		FoodAdapter adapter = (FoodAdapter)this.getListAdapter();
		int row = 0;
		for(FoodAdapter.ListItem item : adapter.getItems()){
			if(food.equals(item.getLeft()) || food.equals(item.getRight())){
				getListView().setSelection(row);
				return;
			}
			row++;
		}
	}
}

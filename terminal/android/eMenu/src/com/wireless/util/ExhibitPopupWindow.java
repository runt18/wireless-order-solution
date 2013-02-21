package com.wireless.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.excep.BusinessException;
import com.wireless.lib.task.QueryFoodAssociationTask;
import com.wireless.ordermenu.R;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.util.imgFetcher.ImageFetcher;

/**
 * The pop up window used to show sorts of contents, 
 * like associated foods, associated combo, associated order.
 * @author Ying.Zhang
 *
 */
public class ExhibitPopupWindow extends PopupWindow {

	public interface OnExhibitOperateListener{
		/**
		 * Called when any food display in exhibition pop up is clicked.
		 */
		public void onFoodClicked(Food clickedFood);
	}

	private OnExhibitOperateListener mExhibitOperListener;
	
	public ExhibitPopupWindow(View contentView, int width, int height){
		super(contentView, width, height);
		init();
	}
	
	public ExhibitPopupWindow(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}
	
	private void init(){
		setOutsideTouchable(true);
		setBackgroundDrawable(getContentView().getContext().getResources().getDrawable(R.drawable.popup_small));
	}
	
	public void setOperateListener(OnExhibitOperateListener l){
		this.mExhibitOperListener = l;
	}
	
	/**
	 * Show the associated foods by a specific food.
	 * @param anchor the view on which to pin the pop up window
	 * @param foodToAssociated the food to be associated
	 */
	public void showAssociatedFoods(final View anchor, Food foodToAssociated){
		showAssociatedFoods(anchor, 0, 0, foodToAssociated);
	}
	
	/**
	 * Show the associated foods by a specific food.
	 * @param anchor the view on which to pin the pop up window
	 * @param xoff the x offset
	 * @param yoff the y offset
	 * @param foodToAssociated the food to be associated
	 */
	public void showAssociatedFoods(final View anchor, final int xoff, final int yoff, Food foodToAssociated){
		new QueryFoodAssociationTask(foodToAssociated, false){
			
			private ImageFetcher mFetcher = new ImageFetcher(getContentView().getContext(), 200, 144);
			
			/**
			 * When query returned, it will generate views for each associated food, and display it in the {@link PopupWindow}
			 * <br/>
			 * finally, the {@link PopupWindow} will show base on the anchor view 
			 */
			@Override
			protected void onPostExecute(Food[] associatedFoods) {
				if(associatedFoods.length == 0){
					Toast.makeText(getContentView().getContext(), "此菜无关联菜", Toast.LENGTH_SHORT).show();
					
				} else {
					LayoutInflater inflater = LayoutInflater.from(getContentView().getContext());
					LinearLayout comboLayout = (LinearLayout)getContentView().findViewById(R.id.linearLayout_galleryFgm_combo);
					comboLayout.removeAllViews(); 
					//将所有关联菜添加
					for(Food food : associatedFoods){
						if(food.image != null){
							View foodView = inflater.inflate(R.layout.gallery_fgm_combo_item, null);
							TextView nameText = (TextView)foodView.findViewById(R.id.textView_galleryFgm_combo_item);
							
							nameText.setText(food.getName() + " ￥ " + food.getPrice());
							
							ImageView imgView = (ImageView) foodView.findViewById(R.id.imageView_galleryFgm_combo_item);
							imgView.setScaleType(ScaleType.CENTER_CROP);
							mFetcher.loadImage(food.image, imgView );
							
							comboLayout.addView(foodView);
							foodView.setTag(food);
							foodView.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									Food food = (Food) v.getTag();
									//如果有图片则跳转，没有则提示
									if(food.image != null){
										if(mExhibitOperListener != null){
											mExhibitOperListener.onFoodClicked(food);
										}
										dismiss();
										
									} else {
										Toast toast = Toast.makeText(getContentView().getContext(), "此菜暂无图片可展示", Toast.LENGTH_SHORT);
										toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
										toast.show();
									}
								}
							});

							//点菜按钮
							View addBtn = foodView.findViewById(R.id.button_galleryFgm_combo_item_add);
							addBtn.setTag(food);
							addBtn.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									Food food = (Food) v.getTag();
									try {
										ShoppingCart.instance().addFood(new OrderFood(food));
										Toast toast = Toast.makeText(getContentView().getContext(), "1份" + food.getName() + "已添加", Toast.LENGTH_SHORT);
										toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
										toast.show();
									} catch (BusinessException e) {
										Toast.makeText(getContentView().getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
									}
								}
							});
						}
					}
					
					//显示关联菜弹出框
					showAsDropDown(anchor, xoff, yoff);
					getContentView().post(new Runnable() {
						@Override
						public void run() {
							//滚回到第一个
							((HorizontalScrollView)getContentView().findViewById(R.id.horizontalScrollView_galleryFgm_combo)).smoothScrollTo(0, 0);		
						}
					});
				}				
			}
		}.execute(WirelessOrder.foodMenu);
			
	}
	
}

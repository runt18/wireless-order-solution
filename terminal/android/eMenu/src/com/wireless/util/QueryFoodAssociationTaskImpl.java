package com.wireless.util;

import android.content.Context;
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
import com.wireless.excep.BusinessException;
import com.wireless.lib.task.QueryFoodAssociationTask;
import com.wireless.ordermenu.R;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.util.imgFetcher.ImageFetcher;

/**
 * this task will query the food's association and display each food in the popup window
 * @author ggdsn1
 *
 */
public class QueryFoodAssociationTaskImpl extends QueryFoodAssociationTask {
	private View mViewToAnchor;
	private Context mContext;
	private PopupWindow mPopup;
	private ImageFetcher mFetcher;
	private OnFoodClickListener mOnFoodClickListener;
	
	/**
	 * the constructor of this task
	 * @param foodToAssociate the food which needs to associate
	 * @param isForceToQuery 
	 * @param context the context just use to show some toasts
	 * @param viewToAnchor the {@link PopupWindow} will display base on this view
	 * @param popup it will show when the query task return successfully
	 * @param fetcher the {@link ImageFetcher} is use to display images
	 */
	public QueryFoodAssociationTaskImpl(Food foodToAssociate, boolean isForceToQuery, 
			Context context, View viewToAnchor, PopupWindow popup, ImageFetcher fetcher) {
		super(foodToAssociate, isForceToQuery);
		mContext = context;
		mViewToAnchor = viewToAnchor;
		mPopup = popup;
		mFetcher = fetcher;
	}

	/**
	 * the constructor of this task
	 * @see #QueryFoodAssociationTaskImpl(Food, boolean, Context, View, PopupWindow, ImageFetcher)
	 * @param foodToAssociate
	 * @param context
	 * @param viewToAnchor
	 * @param popup
	 * @param fetcher
	 */
	public QueryFoodAssociationTaskImpl(Food foodToAssociate,
			Context context, View viewToAnchor, PopupWindow popup, ImageFetcher fetcher) {
		super(foodToAssociate);
		mContext = context;
		mViewToAnchor = viewToAnchor;
		mPopup = popup;
		mFetcher = fetcher;
	}

	/**
	 * when query returned, it will generate views for each associated food, and display it in the {@link PopupWindow}
	 * <br/>
	 * finally, the {@link PopupWindow} will show base on the anchor view 
	 */
	@Override
	protected void onPostExecute(Food[] result) {
		super.onPostExecute(result);
		if(result == null || result.length == 0){
			Toast.makeText(mContext, "此菜无关联菜", Toast.LENGTH_SHORT).show();
			//如果返回后依然是当前菜品
		} else if(mFoodToAssociate.equals(mFoodToAssociate)){
			LayoutInflater inflater = LayoutInflater.from(mContext);
			LinearLayout comboLayout = (LinearLayout) mPopup.getContentView().findViewById(R.id.linearLayout_galleryFgm_combo);
			comboLayout.removeAllViews(); 
			//将所有关联菜添加
			for(Food food : result){
				if(food.image != null){
					View foodView = inflater.inflate(R.layout.gallery_fgm_combo_item, null);
					TextView nameText = (TextView)foodView.findViewById(R.id.textView_galleryFgm_combo_item);
					
					if(food.getName() != null)
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
								if(mOnFoodClickListener != null)
									mOnFoodClickListener.onFoodClick(food);
								mPopup.dismiss();
							} else {
								Toast toast = Toast.makeText(mContext, "此菜暂无图片可展示", Toast.LENGTH_SHORT);
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
								Toast toast = Toast.makeText(mContext, "1份"+food.getName()+"已添加", Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
								toast.show();
							} catch (BusinessException e) {
								Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
			}
			//显示弹出框
			mPopup.showAsDropDown(mViewToAnchor, 50,20);
			mPopup.getContentView().post(new Runnable() {
				@Override
				public void run() {
					//滚回到第一个
					((HorizontalScrollView)mPopup.getContentView().findViewById(R.id.horizontalScrollView_galleryFgm_combo)).smoothScrollTo(0, 0);		
				}
			});
		}
	}
	
	/**
	 * when the associated food is clicked, use this to tell the container activity or fragment
	 * @author ggdsn1
	 *
	 */
	public interface OnFoodClickListener{
		void onFoodClick(Food food);
	}
	public void setOnFoodClickListener(OnFoodClickListener l){
		mOnFoodClickListener = l;
	}
}

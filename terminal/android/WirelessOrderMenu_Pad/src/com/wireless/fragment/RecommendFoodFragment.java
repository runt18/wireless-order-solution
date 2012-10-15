package com.wireless.fragment;

import java.util.ArrayList;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.ui.FullScreenActivity;

public class RecommendFoodFragment extends DialogFragment
		implements GalleryFragment.OnPicClickListener,
		GalleryFragment.OnPicChangedListener {

	private GalleryFragment mGalleryFragment;
	private OrderFood mOrderFood;
	private ArrayList<Food> mRecommendfoods;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置底部推荐菜的数据和显示
		mRecommendfoods = new ArrayList<Food>();
		for (Food f : WirelessOrder.foods) {
			if (f.isRecommend())
				mRecommendfoods.add(f);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(
				R.layout.recommend_food_dialog, container,
				false);

		final EditText countEditText = (EditText) view
				.findViewById(R.id.editText_count_rec_dialog);

		// 设置数量加减
		((ImageButton) view
				.findViewById(R.id.imageButton_plus_rec_dialog))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						float curNum = Float
								.parseFloat(countEditText
										.getText()
										.toString());
						countEditText
								.setText("" + ++curNum);
					}
				});

		// 设置数量减
		((ImageButton) view
				.findViewById(R.id.imageButton_minus_recommendDialog))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						float curNum = Float
								.parseFloat(countEditText
										.getText()
										.toString());
						if (--curNum >= 0) {
							countEditText.setText(""
									+ curNum);
						}
					}
				});
		// 点菜按钮
		((ImageButton) view
				.findViewById(R.id.imageButton_addFood_rec_dialog))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mOrderFood = new OrderFood(
								mRecommendfoods
										.get(mGalleryFragment
												.getSelectedPosition()));
						mOrderFood.setCount(Float
								.parseFloat(((EditText) view
										.findViewById(R.id.editText_count_rec_dialog))
										.getText()
										.toString()));
						ShoppingCart.instance().addFood(
								mOrderFood);
						Toast.makeText(getActivity(),
								mOrderFood.name + "已添加",
								Toast.LENGTH_SHORT).show();
					}
				});

		((ImageButton) view
				.findViewById(R.id.imageButton_amplify_rec_dialog))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(
								getActivity(),
								FullScreenActivity.class);
						Bundle bundle = new Bundle();
						bundle.putParcelable(
								FoodParcel.KEY_VALUE,
								new FoodParcel(mOrderFood));
						intent.putExtras(bundle);
						getActivity().startActivity(intent);
					}
				});

		// view.getViewTreeObserver().addOnGlobalLayoutListener(new
		// OnGlobalLayoutListener(){
		// @Override
		// public void onGlobalLayout() {
		// // TODO Auto-generated method stub
		// mGalleryFragment.notifyDataChanged(mRecommendfoods);
		// view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
		// }
		// });

		return view;
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		mGalleryFragment = (GalleryFragment) getFragmentManager()
				.findFragmentById(
						R.id.galleryFragment_rec_dialog);
		mGalleryFragment.notifyDataChanged(mRecommendfoods);
		// mGalleryFragment = GalleryFragment.newInstance(mRecommendfoods, 0.3f,
		// 8, ScaleType.CENTER_INSIDE);
		// FragmentTransaction fragmentTransaction =
		// getActivity().getFragmentManager().beginTransaction();
		// fragmentTransaction.replace(R.id.viewPager_container_recFood_dialog,
		// mGalleryFragment).commit();
		// mGalleryFragment.setOnPicClickListener(this);
		// mGalleryFragment.setOnPicChangedListener(this);
	}

	@Override
	public void onPicChanged(Food curFood, int position) {
		((TextView) getView().findViewById(
				R.id.textView_price_rec_dialog)).setText(""
				+ curFood.getPrice());
		((TextView) getView().findViewById(
				R.id.textView_food_name_recommend_dialog))
				.setText(curFood.name);
	}

	@Override
	public void onPicClick(Food food, int position) {
		// //当点击推荐菜时更新当前菜品
		float count = Float
				.parseFloat(((EditText) getView()
						.findViewById(
								R.id.editText_count_rec_dialog))
						.getText().toString());
		mOrderFood = new OrderFood(
				mRecommendfoods.get(position));
		mOrderFood.setCount(count);
		mOrderFood.tmpTaste = new Taste();
		mOrderFood.tmpTaste.setPreference("");

		Intent intent = getActivity().getIntent();
		Bundle bundle = new Bundle();
		bundle.putParcelable(FoodParcel.KEY_VALUE,
				new FoodParcel(mOrderFood));
		intent.replaceExtras(bundle);

		// mFoodImageView.setImageBitmap(mImgLoader.loadImage(mOrderFood.image));
		// mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
		// mDialog.dismiss();
	}

}

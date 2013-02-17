package com.wireless.panorama;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.excep.BusinessException;
import com.wireless.ordermenu.R;
import com.wireless.panorama.util.BackgroundPager;
import com.wireless.panorama.util.FramePager;
import com.wireless.panorama.util.LayoutArranger;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.util.imgFetcher.ImageFetcher;
/**
 * 该类仅仅负责对传入的数据进行显示
 * @author ggdsn1
 *
 */
public class PanoramaItemFragment extends Fragment{

    private static final String DATA_SOURCE_LARGE_FOODS = "dataSourceLargeFoods";
	private static final String DATA_SOURCE_SMALL_FOODS = "dataSourceSmallFoods";
	private static final String DATA_SOURCE_MEDIUM_FOODS = "dataSourceMediumFoods";
	private static final String DATA_SOURCE_TEXT_FOODS = "dataSourceTextFoods";
	
	private static final String DATA_LAYOUT_ID = "dataViewID";
	private static final String DATA_FRAME_ID = "dataFrameId";
	private static final String DATA_BACKGROUND_ID = "dataBackgroundId";
	
	private static final int TYPE_LARGE_FOOD = 1;
	private static final int TYPE_MEDIUM_FOOD = 2;
	private static final int TYPE_SMALL_FOOD = 3;
	private static final int TYPE_TEXT_FOOD = 4;
	
	/**
	 * 创建该fragment的实例，传入相应的数据
	 * @param group
	 * @return
	 */
	public static PanoramaItemFragment newInstance(FramePager group) {
        PanoramaItemFragment fgm = new PanoramaItemFragment();

        Bundle args = new Bundle();
        //如果有对应的菜品，则将其添加进bundle中
        if(group.hasLargeFoods()){
	        List<Food> largeList = Arrays.asList(group.getLargeFoods());
	        putParcelableArrayList(args, largeList, DATA_SOURCE_LARGE_FOODS);
        }
        
        if(group.hasMediumFoods()){
        	List<Food> mediumList = Arrays.asList(group.getMediumFoods());
        	putParcelableArrayList(args, mediumList, DATA_SOURCE_MEDIUM_FOODS);
        }
        
        if(group.hasSmallFoods()){
	        List<Food> smallList = Arrays.asList(group.getSmallFoods());
			putParcelableArrayList(args, smallList, DATA_SOURCE_SMALL_FOODS);
        }
        
        if(group.hasTextFoods()){
        	List<Food> textList = Arrays.asList(group.getTextFoods());
        	putParcelableArrayList(args, textList, DATA_SOURCE_TEXT_FOODS);
        }
        
        //添加图层id、边框id和背景id
		args.putInt(DATA_LAYOUT_ID, group.getLayoutId());
		if(group.hasFrameId())
			args.putInt(DATA_FRAME_ID, group.getFrameId());
		if(group instanceof BackgroundPager){
			BackgroundPager bgGroup = (BackgroundPager) group;
			if(bgGroup.hasBackgroundId())
				args.putInt(DATA_BACKGROUND_ID, bgGroup.getBackgroundId());
		}
		fgm.setArguments(args);

        return fgm;
    }

	//the method helps to put the foodList into parcel
	private static void putParcelableArrayList(Bundle args, List<Food> listToPut, String type){
		
		ArrayList<FoodParcel> foodParcels = new ArrayList<FoodParcel>();
		for(Food f: listToPut){
			foodParcels.add(new FoodParcel(new OrderFood(f)));
		}
		args.putParcelableArrayList(type, foodParcels);
	}
	
	private ImageFetcher mImageFetcher;
	
	public PanoramaItemFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layout = super.onCreateView(inflater, container, savedInstanceState);
		
		if(PanoramaActivity.class.isInstance(getActivity())){
			LayoutArranger arranger = ((PanoramaActivity) getActivity()).getLayoutArranger();
			
			//根据传入的ID号获取layout
			Context context = arranger.getContext(getString(R.string.layout_packageName));
			if(context != null){
				Bundle args = getArguments();
				int id = args.getInt(DATA_LAYOUT_ID);
				
				if(id >= 0){
					layout = inflater.inflate(context.getResources().getLayout(id), null);
					
					layout.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							((PanoramaActivity)getActivity()).toggleOnClick(v);	
							((PanoramaActivity)getActivity()).closeSearchView();
						}
					});
				}
			}
		}

		return layout;
	}

	/**
	 * 根据生成的layout，将图片摆放进去
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if(PanoramaActivity.class.isInstance(getActivity())){
			mImageFetcher = ((PanoramaActivity)getActivity()).getImageFetcher();
	    	ViewGroup layout = (ViewGroup) getView();
	    	
	    	if(layout != null){
				
				Bundle args = getArguments();
				
				LayoutArranger arranger = ((PanoramaActivity) getActivity()).getLayoutArranger();
				Context context = arranger.getContext(getString(R.string.layout_packageName));
				
				//设置layout背景
				int bgId = -1;
				bgId = args.getInt(DATA_BACKGROUND_ID);
				if(bgId > 0){
					Drawable bg = context.getResources().getDrawable(bgId);
					if(bg != null){
						layout.setBackgroundDrawable(bg);
					}
				}
				
				//根据layout和传入的菜品数据，加载图片和按钮等功能
				if(args.getParcelableArrayList(DATA_SOURCE_LARGE_FOODS) != null){
			    	ArrayList<FoodParcel> largeFoods = args.getParcelableArrayList(DATA_SOURCE_LARGE_FOODS);
			    	displayImages(context,largeFoods, TYPE_LARGE_FOOD);
				}
				if(args.getParcelableArrayList(DATA_SOURCE_SMALL_FOODS) != null){
			    	ArrayList<FoodParcel> smallFoods = args.getParcelableArrayList(DATA_SOURCE_SMALL_FOODS);
			    	displayImages(context,smallFoods, TYPE_SMALL_FOOD);
				}
				if(args.getParcelableArrayList(DATA_SOURCE_MEDIUM_FOODS) != null){
			    	ArrayList<FoodParcel> mediumFoods = args.getParcelableArrayList(DATA_SOURCE_MEDIUM_FOODS);
			    	displayImages(context, mediumFoods, TYPE_MEDIUM_FOOD);
				}
				if(args.getParcelableArrayList(DATA_SOURCE_TEXT_FOODS) != null){
			    	ArrayList<FoodParcel> textFoods = args.getParcelableArrayList(DATA_SOURCE_TEXT_FOODS);
			    	displayImages(context, textFoods, TYPE_TEXT_FOOD);
				}
	    	}
		}
	}

	/**
	 * 将每个菜品对应的imageView的tag名组合出来，并根据tag名找到对应的view，再用imageFetcher显示 
	 * 点菜等其它按钮的处理方法类似
	 * @param context
	 * @param foodList
	 * @param type
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void displayImages(Context context, List<? extends Food> foodList, int type){
		StringBuilder imageTagBuilder = new StringBuilder("imageView_");
		StringBuilder addButtonTagBuilder = new StringBuilder("button_add_");
		StringBuilder foodNameTagBuilder = new StringBuilder("textView_name_");
		switch(type){
		case TYPE_LARGE_FOOD:
			imageTagBuilder.append("l");
			addButtonTagBuilder.append("l");
			foodNameTagBuilder.append("l");
			break;
		case TYPE_MEDIUM_FOOD:
			imageTagBuilder.append("m");
			addButtonTagBuilder.append("m");
			foodNameTagBuilder.append("m");
			break;
		case TYPE_SMALL_FOOD:
			imageTagBuilder.append("s");
			addButtonTagBuilder.append("s");
			foodNameTagBuilder.append("s");
			break;
		case TYPE_TEXT_FOOD:
			imageTagBuilder.append("t");
			addButtonTagBuilder.append("t");
			foodNameTagBuilder.append("t");
			break;
		}
		

    	for(int i =0 ; i< foodList.size();i++){ 
    		int index = i;
    		index++;
    		
    		final Food food = foodList.get(i);
    		
    		if(i <= 3){
    			Log.i(food.getName(), "image: "+food.image);
    		}
    		String imageTag = imageTagBuilder.toString() + index;
    		String buttonTag = addButtonTagBuilder.toString() + index;
    		String foodNameTag = foodNameTagBuilder.toString() + index;
    		
    		final ImageView imageView = (ImageView) getView().findViewWithTag(imageTag);
    		imageView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				
				@Override
				public void onGlobalLayout() {
					if(imageView.getWidth() > 0 && imageView.getHeight() > 0){
						mImageFetcher.loadImage(food.image, imageView);
						imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
				}
			});

    		imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((PanoramaActivity)getActivity()).toggleOnClick(v);
					((PanoramaActivity)getActivity()).closeSearchView();
				}
			});
    		
    		TextView foodNameView = (TextView) getView().findViewWithTag(foodNameTag);
    		if(foodNameView != null)
    			foodNameView.setText(food.getName());
    		
    		//根据id拿去图片边框，并设置边框（背景）
    		int frameId = -1;
    		frameId = getArguments().getInt(DATA_FRAME_ID);
    		if(frameId > 0){
    			Drawable drawable = context.getResources().getDrawable(frameId);
	    		if(drawable != null)
	    			imageView.setBackgroundDrawable(drawable);
    		}
			//设置点菜按钮
    		View addBtn = getView().findViewWithTag(buttonTag);
    		if(addBtn != null){
        		
    			addBtn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						try {
							ShoppingCart.instance().addFood(new OrderFood(food));
							Toast.makeText(getActivity(), "已添加："+food.getName()+"1份", Toast.LENGTH_SHORT).show();
							((PanoramaActivity) getActivity()).addOnClick(food);
						} catch (BusinessException e) {
							Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					}
				});
    		}

    	}
	}
}

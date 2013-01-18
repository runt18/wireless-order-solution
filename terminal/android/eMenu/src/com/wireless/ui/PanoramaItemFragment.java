package com.wireless.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wireless.ordermenu.R;
import com.wireless.panorama.util.ImageArranger;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Pager;
import com.wireless.util.imgFetcher.ImageFetcher;

public class PanoramaItemFragment extends Fragment{

    private static final String DATA_SOURCE_LARGE_FOODS = "dataSourceLargeFoods";
	private static final String DATA_SOURCE_SMALL_FOODS = "dataSourceSmallFoods";
	private static final String DATA_LAYOUT_ID = "dataViewID";
	
	private static final int TYPE_LARGE_FOOD = 1;
	private static final int TYPE_MEDIUM_FOOD = 2;
	private static final int TYPE_SMALL_FOOD = 3;
	private static final int TYPE_TEXT_FOOD = 4;
	
	public static PanoramaItemFragment newInstance(Pager group) {
        PanoramaItemFragment fgm = new PanoramaItemFragment();

        Bundle args = new Bundle();
        
        if(group.hasLargeFoods()){
	        List<Food> largeList = Arrays.asList(group.getLargeFoods());
	        putParcelableArrayList(args, largeList, DATA_SOURCE_LARGE_FOODS);
        }
        
        if(group.hasSmallFoods()){
	        List<Food> smallList = Arrays.asList(group.getSmallFoods());
			putParcelableArrayList(args, smallList, DATA_SOURCE_SMALL_FOODS);
        }
        
		args.putInt(DATA_LAYOUT_ID, group.getLayoutId());
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
			ImageArranger arranger = ((PanoramaActivity) getActivity()).getImageArranger();
			
			Context context = arranger.getContext(getString(R.string.layout_packageName));
			if(context != null){
				Bundle args = getArguments();
				int id = args.getInt(DATA_LAYOUT_ID);
				
				if(id >= 0){
					layout = inflater.inflate(context.getResources().getLayout(id), null);
					
					layout.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							((PanoramaActivity)getActivity()).onClick(v);	
						}
					});
				}
			}
		}

		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if(PanoramaActivity.class.isInstance(getActivity())){
			mImageFetcher = ((PanoramaActivity)getActivity()).getImageFetcher();
	    	ViewGroup layout = (ViewGroup) getView();
	    	
	    	if(layout != null){
				
				Bundle args = getArguments();
				
				if(args.getParcelableArrayList(DATA_SOURCE_LARGE_FOODS) != null){
			    	ArrayList<FoodParcel> largeFoods = args.getParcelableArrayList(DATA_SOURCE_LARGE_FOODS);
			    	displayImages(largeFoods, TYPE_LARGE_FOOD);
				}
				if(args.getParcelableArrayList(DATA_SOURCE_SMALL_FOODS) != null){
			    	ArrayList<FoodParcel> smallFoods = args.getParcelableArrayList(DATA_SOURCE_SMALL_FOODS);
			    	displayImages(smallFoods, TYPE_SMALL_FOOD);
				}
	    	}
		}
	}

	private void displayImages(List<? extends Food> foodList, int type){
		StringBuilder firstTagBuilder = new StringBuilder("imageView_");
		switch(type){
		case TYPE_LARGE_FOOD:
			firstTagBuilder.append("l");
			break;
		case TYPE_MEDIUM_FOOD:
			firstTagBuilder.append("m");
			break;
		case TYPE_SMALL_FOOD:
			firstTagBuilder.append("s");
			break;
		case TYPE_TEXT_FOOD:
			firstTagBuilder.append("t");
			break;
		}
		
    	for(int i =0 ; i< foodList.size();i++){
    		int index = i;
    		index++;
    		
    		String tag = firstTagBuilder.toString() + index;

    		ImageView imageView = (ImageView) getView().findViewWithTag(tag);
    		mImageFetcher.loadImage(foodList.get(i).image, imageView);
    	}
	}
}

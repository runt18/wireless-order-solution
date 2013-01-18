package com.wireless.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wireless.ordermenu.R;
import com.wireless.panorama.util.ImageArranger;
import com.wireless.panorama.util.PanoramaGroup;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.util.imgFetcher.ImageFetcher;

public class PanoramaItemFragment extends Fragment{

    private static final String DATA_SOURCE_LARGE_FOODS = "dataSourceLargeFoods";
	private static final String DATA_LAYOUT_ID = "dataViewID";
	private static final String DATA_SOURCE_SMALL_FOODS = "dataSourceSmallFoods";

	public static PanoramaItemFragment newInstance(PanoramaGroup group) {
        PanoramaItemFragment fgm = new PanoramaItemFragment();

        Bundle args = new Bundle();
        
        List<Food> largeList = group.getLargeList();
        List<Food> smallList = group.getSmallList();
        
		ArrayList<FoodParcel> largeParcels = new ArrayList<FoodParcel>();
		ArrayList<FoodParcel> smallParcels = new ArrayList<FoodParcel>();
		
		for(Food f: largeList){
			largeParcels.add(new FoodParcel(new OrderFood(f)));
		}
		for(Food f: smallList){
			smallParcels.add(new FoodParcel(new OrderFood(f)));
		}
		args.putParcelableArrayList(DATA_SOURCE_LARGE_FOODS, largeParcels);
		args.putParcelableArrayList(DATA_SOURCE_SMALL_FOODS, smallParcels);
		
		args.putInt(DATA_LAYOUT_ID, group.getLayoutId());
		fgm.setArguments(args);

        return fgm;
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
//				int id = context.getResources().getIdentifier("b3s0", "layout", context.getPackageName());
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
		    	ArrayList<FoodParcel> largeFoods = args.getParcelableArrayList(DATA_SOURCE_LARGE_FOODS);
		    	ArrayList<FoodParcel> smallFoods = args.getParcelableArrayList(DATA_SOURCE_SMALL_FOODS);
		    	for(int i=0;i<largeFoods.size();i++){
		    		int index = i;
		    		index++;
		    		String tag = "imageView_l" + index;
		    		ImageView imageView = (ImageView) layout.findViewWithTag(tag);
			    	
		    		Log.i("large"," "+largeFoods.get(i).image);
		    		mImageFetcher.loadImage(largeFoods.get(i).image, imageView);
		    	}
		    	
		    	for(int i =0 ; i< smallFoods.size();i++){
		    		int index = i;
		    		index++;
		    		
		    		String tag = "imageView_s" + index;
		    		
		    		ImageView imageView = (ImageView) layout.findViewWithTag(tag);
		    		mImageFetcher.loadImage(smallFoods.get(i).image, imageView);
		    		Log.i("small"," "+smallFoods.get(i).image);
		    	}
	    	}
		}
	}

}

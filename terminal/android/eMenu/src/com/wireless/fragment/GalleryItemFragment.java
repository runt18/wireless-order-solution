/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wireless.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.util.imgFetcher.ImageWorker;



/**
 * This fragment will populate the children of the ViewPager from {@link GalleryFragment}.
 */
public class GalleryItemFragment extends Fragment {
	
    private static final String KEY_PARENT_TAG = "gallery_resource_id";
	private final static String KEY_IMAGE_SCALE_TYPE = "key_image_scale_type";

    private Food mFood;
    private ImageView mImageView;

    /**
     * Factory method to generate a new instance of the fragment along with the food and id to parent gallery fragment.
     *
     * @param food The food source associated with this fragment
     * @param parentTag The tag to parent gallery fragment 
     * @param scaleType The scale type to the image view
     * @return A new instance of GalleryItemFragment with the food and id to parent gallery fragment
     */
    public static GalleryItemFragment newInstance(Food food, String parentTag, ScaleType scaleType) {
        final GalleryItemFragment f = new GalleryItemFragment();

        final Bundle args = new Bundle();
        args.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(new OrderFood(food)));
        args.putString(KEY_PARENT_TAG, parentTag);
        args.putInt(KEY_IMAGE_SCALE_TYPE, scaleType.ordinal());
        f.setArguments(args);

        return f;
    }

    /**
     * Empty constructor as per the Fragment documentation
     */
    public GalleryItemFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate and locate the main ImageView
        View view = inflater.inflate(R.layout.image_detail_fragment, container, false);
        String parentGalleryTag = "";
        ScaleType scaleType = ScaleType.CENTER_CROP;
        
        Bundle bundle = getArguments();
        if(bundle != null){
        	mFood = bundle.getParcelable(FoodParcel.KEY_VALUE);
        	parentGalleryTag = bundle.getString(KEY_PARENT_TAG);
        	scaleType = ScaleType.values()[bundle.getInt(KEY_IMAGE_SCALE_TYPE)];
        }
        
        GalleryFragment gf = null;
        try{
        	 gf = (GalleryFragment)getActivity().getFragmentManager().findFragmentByTag(parentGalleryTag);
        } catch(ClassCastException e){
        	
        }
        if(gf != null){
        	
        	mImageView = (ImageView) view.findViewById(R.id.detailImgView);
            mImageView.setScaleType(scaleType);
            
            gf.getImgFetcher().loadImage(mFood.image, mImageView);
            mImageView.setTag(gf);
            mImageView.setOnClickListener(new OnClickListener() {
    			
    			@Override
    			public void onClick(View v) {
    				GalleryFragment gf = (GalleryFragment) v.getTag();
    				if(gf.mOnPicClickListener != null){
    					gf.mOnPicClickListener.onPicClick(mFood, gf.getSelectedPosition());
    				}
    			}
    		});        	
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mImageView != null) {
            // Cancel any pending image work
            ImageWorker.cancelWork(mImageView);
            mImageView.setImageDrawable(null);
        }
    }
}

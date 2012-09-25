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

import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.util.imgFetcher.ImageWorker;



/**
 * This fragment will populate the children of the ViewPager from {@link GalleryFragment}.
 */
public class ImageDetailFragment extends Fragment {
    private static final String PARENT_RES_ID = "gallery_resource_id";
    private Food mFood;
    private int mParentResId;
    private ImageView mImageView;

    /**
     * Factory method to generate a new instance of the fragment along with the food and id to parent gallery fragment.
     *
     * @param food The food source associated with this fragment
     * @param parentResId The id to parent gallery fragment 
     * @return A new instance of ImageDetailFragment with the food and id to parent gallery fragment
     */
    public static ImageDetailFragment newInstance(Food food, int parentResId) {
        final ImageDetailFragment f = new ImageDetailFragment();

        final Bundle args = new Bundle();
        args.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(new OrderFood(food)));
        args.putInt(PARENT_RES_ID, parentResId);
        f.setArguments(args);

        return f;
    }

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageDetailFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null){
        	mFood = bundle.getParcelable(FoodParcel.KEY_VALUE);
        	mParentResId = bundle.getInt(PARENT_RES_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate and locate the main ImageView
        final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
        mImageView = (ImageView) v.findViewById(R.id.detailImgView);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
 
        final GalleryFragment gf = (GalleryFragment)getActivity().getFragmentManager().findFragmentById(mParentResId);
        gf.getImgFetcher().loadImage(mFood.image, mImageView);
        
        mImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(gf.mOnPicClickListener != null){
					gf.mOnPicClickListener.onPicClicked(mFood, gf.getSelectedPosition());
				}
			}
		});
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

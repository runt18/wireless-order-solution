package com.wireless.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
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
        args.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(food));
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
        String parentGalleryTag;
        ScaleType scaleType = ScaleType.CENTER_CROP;
        GalleryFragment gf = null;
        
        Bundle bundle = getArguments();
        if(bundle != null){
        	FoodParcel foodParcel = bundle.getParcelable(FoodParcel.KEY_VALUE);
        	mFood = foodParcel.asFood();
        	parentGalleryTag = bundle.getString(KEY_PARENT_TAG);
        	scaleType = ScaleType.values()[bundle.getInt(KEY_IMAGE_SCALE_TYPE)];
			try {
				gf = (GalleryFragment) getActivity().getFragmentManager().findFragmentByTag(parentGalleryTag);
			} catch (ClassCastException e) {

			}
        }
        
        if(gf != null){
        	
        	mImageView = (ImageView) view.findViewById(R.id.detailImgView);
            mImageView.setScaleType(scaleType);
            
            gf.getImgFetcher().loadImage(mFood.getImage(), mImageView);
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

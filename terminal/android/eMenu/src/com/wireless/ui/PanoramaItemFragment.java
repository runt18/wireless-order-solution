package com.wireless.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PanoramaItemFragment extends Fragment{

    public static PanoramaItemFragment newInstance() {
        final PanoramaItemFragment f = new PanoramaItemFragment();

        final Bundle args = new Bundle();
        f.setArguments(args);

        return f;
    }
	
	public PanoramaItemFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

}

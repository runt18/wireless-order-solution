package com.wireless.util;

import com.wireless.ordermenu.R;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PickTasteFragment extends DialogFragment {
	@Override 
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pick_taste_dialog, container, false);
		return view;
	}
}

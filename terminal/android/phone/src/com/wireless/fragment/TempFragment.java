package com.wireless.fragment;

import com.wireless.ui.R;
import com.wireless.ui.view.TempListView;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.AbsListView.OnScrollListener;

public class TempFragment extends Fragment {

	private TempListView mTempLstView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View tempView = inflater.inflate(R.layout.temp, null);

		mTempLstView = (TempListView) tempView.findViewById(R.id.tempListView);
		mTempLstView.notifyDataChanged();

		// ¡Ÿ ±≤ÀÃÌº”
		((ImageView) tempView.findViewById(R.id.add))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mTempLstView.addTemp();
					}
				});
		

		/**
		 * Listπˆ∂Øµƒ ±∫Ú∆¡±Œ»Ìº¸≈Ã
		 */
		mTempLstView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				InputMethodManager input = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				input.hideSoftInputFromWindow(view.getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});
		return tempView;
	}

}

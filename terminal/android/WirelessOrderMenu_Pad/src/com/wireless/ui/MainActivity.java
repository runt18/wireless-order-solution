package com.wireless.ui;



import com.wireless.ui.ItemFragment.OnItemChangeListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class MainActivity extends Activity{
	ContentFragment mContentFragment;
	public static final String KEY_POSITION = "position";

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mContentFragment = (ContentFragment)getFragmentManager().findFragmentById(R.id.content);

		ItemFragment.setItemChangeListener(new OnItemChangeListener(){

			@Override
			public void onItemChange(int position) {
				// TODO Auto-generated method stub
				if(mContentFragment == null){
					Intent intent = new Intent(MainActivity.this,ContentActivity.class);
					Bundle bundle = new Bundle();
					bundle.putInt(KEY_POSITION, position);
					intent.putExtras(bundle);
					startActivity(intent);
				}else{
					mContentFragment.onUpdateContent(position);
				}
			}
		});
		
		
		ImageView amplifyImgView = (ImageView)findViewById(R.id.amplify_btn_imgView);
		amplifyImgView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("btn clicked");
			}
		});
		
		ImageView addDishImgView = (ImageView)findViewById(R.id.add_dish_imgView);
		addDishImgView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("add dish btn clicked");
			}
		});

	}
}

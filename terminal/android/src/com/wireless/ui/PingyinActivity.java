package com.wireless.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.wireless.adapter.FoodAdapter;

public class PingyinActivity extends Activity {
	private ListView myListView;
	private FoodAdapter adapter;
	private AppContext appcontext;
	private ImageView pinback;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pinyin);
		appcontext=(AppContext)getApplication();
		myListView=(ListView)findViewById(R.id.myListView);
		adapter=new FoodAdapter(PingyinActivity.this,appcontext.getFoodMenu().foods);
		myListView.setAdapter(adapter);
		
		pinback=(ImageView)findViewById(R.id.pinback);
		pinback.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		 });
	}
}

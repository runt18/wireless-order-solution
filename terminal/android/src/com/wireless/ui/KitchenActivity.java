package com.wireless.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;

import com.wireless.adapter.FoodAdapter;

public class KitchenActivity extends Activity {
 private ListView myListView;
	private FoodAdapter adapter;
	private AppContext appcontext;
	private ImageView ketback;
	private Spinner myspinner;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ketchen);
		appcontext=(AppContext)getApplication();
		myListView=(ListView)findViewById(R.id.myListView);
		myspinner=(Spinner)findViewById(R.id.Spinner01);
		
		adapter=new FoodAdapter(KitchenActivity.this,appcontext.getFoods());
		myListView.setAdapter(adapter);
		ketback=(ImageView)findViewById(R.id.ketback);
		
		ketback.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		 });
	}
      
}

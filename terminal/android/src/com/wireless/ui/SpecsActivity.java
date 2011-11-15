package com.wireless.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wireless.adapter.SpecAdapter;
import com.wireless.common.Common;
import com.wireless.protocol.Food;
import com.wireless.protocol.Taste;

public class SpecsActivity extends Activity {
	private ListView myListview;
	SpecAdapter adapter;
	private AppContext appcontext;
	private ImageView numback;
	List<Taste> speces;
	private EditText search;
	private TextView foodtaste;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.specs);
		
		Log.e("", "oncreate");
		appcontext = (AppContext) getApplication();
		appcontext.activityList.add(SpecsActivity.this);
		myListview=(ListView)findViewById(R.id.myListView);
		foodtaste=(TextView)findViewById(R.id.foodTasteTxtView);
	    Common.getCommon().init(Common.getCommon().getFoodlist().get(Common.getCommon().getPosition()), foodtaste);
		adapter=new SpecAdapter(SpecsActivity.this,appcontext.getSpecs(),foodtaste);
		myListview.setAdapter(adapter);
		
		
		
		
		speces=new ArrayList<Taste>();
		search=(EditText)findViewById(R.id.search);
		search.addTextChangedListener(watcher);
		
		numback=(ImageView)findViewById(R.id.numback);
		numback.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		
	}
	
	
	
	
	@Override
	protected void onResume() {
		Log.e("", "onResume");
		Common.getCommon().init(Common.getCommon().getFoodlist().get(Common.getCommon().getPosition()), foodtaste);
		super.onResume();
	}




	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.e("", "onStop");
		Common.getCommon().init(Common.getCommon().getFoodlist().get(Common.getCommon().getPosition()), foodtaste);
		super.onStop();
	}




	@Override
	public  void onRestart() {
		// TODO Auto-generated method stub
		Log.e("", "restart");
	    Common.getCommon().init(Common.getCommon().getFoodlist().get(Common.getCommon().getPosition()), foodtaste);
		super.onStart();
	}
	
	
   
	//  ‰»ÎøÚ µ ±º‡Ã˝
	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

	
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

			if (s.toString() == null || s.toString().equals("")) {
				adapter = new SpecAdapter(SpecsActivity.this,appcontext.getSpecs(),foodtaste);
				myListview.setAdapter(adapter);
			} else {
				speces.clear();
				for (int i = 0; i < appcontext.getSpecs().size(); i++) {
					if (String.valueOf(appcontext.getSpecs().get(i).alias_id).startsWith(s.toString().trim())) {
						speces.add(appcontext.getSpecs().get(i));
					}
				}
				
				adapter = new SpecAdapter(SpecsActivity.this, speces,foodtaste);
				myListview.setAdapter(adapter);

			}

		}

	};
}

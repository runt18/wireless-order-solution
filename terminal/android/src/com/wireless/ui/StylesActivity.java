package com.wireless.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.wireless.adapter.StylesAdapter;
import com.wireless.common.Common;
import com.wireless.protocol.Food;
import com.wireless.protocol.Taste;

public class StylesActivity extends Activity {
	private ListView myListview;
	StylesAdapter adapter;
	private AppContext appcontext;
	private ImageView numback;
	List<Taste> styles;
	private EditText search;
	private TextView foodtaste;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.style);
		
		appcontext = (AppContext) getApplication();
		appcontext.activityList.add(StylesActivity.this);
		myListview=(ListView)findViewById(R.id.myListView);
		foodtaste=(TextView)findViewById(R.id.foodtaste);
	    Common.getCommon().init(Common.getCommon().getFoodlist().get(Common.getCommon().getPosition()), foodtaste);
		adapter=new StylesAdapter(StylesActivity.this,appcontext.getStyles(),foodtaste);
		myListview.setAdapter(adapter);
		
		numback=(ImageView)findViewById(R.id.numback);
		
		styles=new ArrayList<Taste>();
		
		
		search=(EditText)findViewById(R.id.search);
		search.addTextChangedListener(watcher);
		numback.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
	}
	

     
	
	protected void onRestart() {
		// TODO Auto-generated method stub
	 Common.getCommon().init(Common.getCommon().getFoodlist().get(Common.getCommon().getPosition()), foodtaste);
		 super.onRestart();
	}

	
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		 Common.getCommon().init(Common.getCommon().getFoodlist().get(Common.getCommon().getPosition()), foodtaste);
		super.onResume();
	}




	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		 Common.getCommon().init(Common.getCommon().getFoodlist().get(Common.getCommon().getPosition()), foodtaste);
		super.onStop();
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
				adapter = new StylesAdapter(StylesActivity.this,appcontext.getStyles(),foodtaste);
				myListview.setAdapter(adapter);
			} else {
				styles.clear();
				for (int i = 0; i < appcontext.getStyles().size(); i++) {
					if (String.valueOf(appcontext.getStyles().get(i).alias_id).startsWith(s.toString().trim())) {
						styles.add(appcontext.getStyles().get(i));
					}
				}
				adapter = new StylesAdapter(StylesActivity.this, styles,foodtaste);
				myListview.setAdapter(adapter);

			}

		}

	};
}

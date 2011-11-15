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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wireless.adapter.TasteAdapter;
import com.wireless.common.Common;
import com.wireless.protocol.Food;
import com.wireless.protocol.Taste;

public class TasteActivity extends Activity {
private ListView myListview;
TasteAdapter adapter;
private AppContext appcontext;
private ImageView numback;
private EditText search;
List<Taste> tastes;
private TextView foodtaste;
public static Food _food;
String taste="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.taste);
		
		appcontext = (AppContext) getApplication();
		appcontext.activityList.add(TasteActivity.this);
		foodtaste=(TextView)findViewById(R.id.foodTasteTxtView);
	    Common.getCommon().init(Common.getCommon().getFoodlist().get(Common.getCommon().getPosition()), foodtaste);
			
		myListview=(ListView)findViewById(R.id.myListView);
		adapter=new TasteAdapter(TasteActivity.this,appcontext.getTastes(),foodtaste);
		myListview.setAdapter(adapter);
		
		tastes=new ArrayList<Taste>();
		numback=(ImageView)findViewById(R.id.numback);
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



	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
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
				adapter = new TasteAdapter(TasteActivity.this,appcontext.getTastes(),foodtaste);
				myListview.setAdapter(adapter);
			} else {
				tastes.clear();
				for (int i = 0; i < appcontext.getTastes().size(); i++) {
					if (String.valueOf(appcontext.getTastes().get(i).alias_id).startsWith(s.toString().trim())) {
						tastes.add(appcontext.getTastes().get(i));
					}
				}
				
				adapter = new TasteAdapter(TasteActivity.this, tastes,foodtaste);
				myListview.setAdapter(adapter);

			}

		}

	};
	
	
}

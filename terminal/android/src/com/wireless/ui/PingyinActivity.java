package com.wireless.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.wireless.adapter.FoodAdapter;
import com.wireless.protocol.Food;

public class PingyinActivity extends Activity {
	private ListView myListView;
	private FoodAdapter adapter;
	private AppContext appcontext;
	private ImageView pinback;
	private EditText searchpin;
	private List<Food> foodes;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pinyin);
		appcontext=(AppContext)getApplication();
		myListView=(ListView)findViewById(R.id.myListView);
		searchpin=(EditText)findViewById(R.id.searchpin);
		adapter=new FoodAdapter(PingyinActivity.this,appcontext.getFoods());
		myListView.setAdapter(adapter);
		
		foodes=new ArrayList<Food>();
		pinback=(ImageView)findViewById(R.id.pinback);
		InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);        
		imm.hideSoftInputFromWindow(searchpin.getWindowToken(), 0);
		searchpin.addTextChangedListener(watcher);  
		pinback.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		 });
	}
	

	// ‰»ÎøÚ µ ±º‡Ã˝
	private TextWatcher watcher = new TextWatcher(){   
		  
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
        		
            	  if(s.toString()==null||s.toString().equals("")){
            		  adapter=new FoodAdapter(PingyinActivity.this,appcontext.getFoods());
                  	  myListView.setAdapter(adapter);
            	  }else{
            		  foodes.clear();
            		  for(int i=0;i<appcontext.getFoods().size();i++){
                  		if(String.valueOf(appcontext.getFoods().get(i).pinyin).contains(s.toString())){
                  			Log.i("", "SSSSSSSS"+appcontext.getFoods().get(i).pinyin);
                  			foodes.add(appcontext.getFoods().get(i));
                  		}
                    }
            		  adapter=new FoodAdapter(PingyinActivity.this,foodes);
            		  myListView.setAdapter(adapter);
            		  Log.e("", foodes.size()+"");
            	  }
        
        	
        
               
        }   
           
    };  
}

package com.wireless.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.wireless.adapter.FoodAdapter;
import com.wireless.protocol.Food;

public class NumberActivity extends Activity {
	private ListView myListView; 
	private FoodAdapter adapter;
	private AppContext appcontext;
	private ImageView numback;
	private EditText search;
	private List<Food> foodes;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		
		setContentView(R.layout.number);
		appcontext=(AppContext)getApplication();
		foodes=new ArrayList<Food>();
		myListView=(ListView)findViewById(R.id.myListView);
		search=(EditText)findViewById(R.id.search);
		

		adapter=new FoodAdapter(NumberActivity.this,appcontext.getFoods());
		myListView.setAdapter(adapter);
		numback=(ImageView)findViewById(R.id.numback);
		
		search.addTextChangedListener(watcher);  
//		InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);        
//		imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
        
		numback.setOnClickListener(new View.OnClickListener() {
			
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
            		  adapter=new FoodAdapter(NumberActivity.this,appcontext.getFoods());
                  	  myListView.setAdapter(adapter);
            	  }else{
            		  foodes.clear();
            		  for(int i=0;i<appcontext.getFoods().size();i++){
                  		if(String.valueOf(appcontext.getFoods().get(i).alias_id).contains(s.toString())){
                  			foodes.add(appcontext.getFoods().get(i));
                  		}
                    }
            		  adapter=new FoodAdapter(NumberActivity.this,foodes);
            		  myListView.setAdapter(adapter);
            		
            	  }
        
        	
        
               
        }   
           
    };  
}

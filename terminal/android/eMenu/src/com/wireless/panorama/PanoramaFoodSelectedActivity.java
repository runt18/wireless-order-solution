package com.wireless.panorama;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.wireless.ordermenu.R;

/**
 * 
 * {@link PanoramaActivity} 的已点菜界面
 * 
 * @author ggdsn1
 *
 */
public class PanoramaFoodSelectedActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_panorama_selected);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		//返回按钮
		findViewById(R.id.button_panorama_back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	
}

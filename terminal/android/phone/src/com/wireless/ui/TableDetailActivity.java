package com.wireless.ui;

import android.app.Activity;
import android.os.Bundle;

public class TableDetailActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_detail);
		int id = getIntent().getIntExtra("ID", -1);
		System.out.println(id);
	}
}

package com.wireless.panorama.util;

import java.util.Locale;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.Food;

public class FoodSearchProvider extends ContentProvider {
	public static final String TAG = "FoodSearchProvider";
	
	//the provider's uri
	public static final String AUTHORITY = "com.wireless.panorama.util.FoodSearchProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	
	//search suggestions key
	public static final String KEY_FOOD_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
	public static final String KEY_FOOD_PRICE = SearchManager.SUGGEST_COLUMN_TEXT_2;
	
	private static final UriMatcher sURIMatcher = buildUriMatcher();
	public String[] colNames = {
			"_id", KEY_FOOD_NAME, KEY_FOOD_PRICE
	};
	private static UriMatcher buildUriMatcher(){
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, 2);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", 2);
		return matcher;
	}
	@Override
	public boolean onCreate() {
		
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		switch(sURIMatcher.match(uri)){
		case 2:
			if(selectionArgs == null){
				throw new IllegalArgumentException("selectionArgs must be provided for the Uri: " + uri);
			}
			return getSuggestions(selectionArgs[0]);
		}
		return null;
	}

	private Cursor getSuggestions(String query){
		query = query.toLowerCase(Locale.getDefault());
		
		MatrixCursor foodCursor = new MatrixCursor(colNames);
		for(Food f:WirelessOrder.foodMenu.foods){
			if(f.getName().toLowerCase(Locale.getDefault()).contains(query) || f.getPinyin().contains(query) || f.getPinyinShortcut().contains(query))
				foodCursor.addRow(new Object[]{f.getAliasId(), f.getName(), f.getPrice()});
		}
		
		return foodCursor;
	}
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}

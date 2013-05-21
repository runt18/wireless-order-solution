package com.wireless.panorama.util;

import java.util.Locale;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.pojo.dishesOrder.Food;

/**
 * @deprecated 暂时未用到这个类，需跟Uri,SearchView搭配使用。目前的处理没有用的uri
 * 
 * @author ggdsn1
 *
 */
@Deprecated 
public class FoodSearchProvider extends ContentProvider {
	public static final String TAG = "FoodSearchProvider";
	
	//the provider's uri
	public static final String AUTHORITY = "com.wireless.panorama.util.FoodSearchProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	
	//search suggestions key
	public static final String KEY_FOOD_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
	public static final String KEY_FOOD_PRICE = SearchManager.SUGGEST_COLUMN_TEXT_2;
	public static final String KEY_FOOD_ADD = SearchManager.SUGGEST_COLUMN_ICON_2;
	
	public String[] colNames = {
			BaseColumns._ID, KEY_FOOD_NAME, KEY_FOOD_PRICE, KEY_FOOD_ADD
	};
	
	//search codes
	private static final int SEARCH_SUGGEST_CODE = 2;
	
	//uri matcher
	private static final UriMatcher sURIMatcher = buildUriMatcher();
	private static UriMatcher buildUriMatcher(){
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST_CODE);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST_CODE);
		return matcher;
	}
	
	@Override
	public boolean onCreate() {
		//do nothing
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		switch(sURIMatcher.match(uri)){
		case SEARCH_SUGGEST_CODE:
			if(selectionArgs == null){
				throw new IllegalArgumentException("selectionArgs must be provided for the Uri: " + uri);
			}
			return getSuggestions(selectionArgs[0]);
		}
		return null;
	}

	public Cursor getSuggestions(String query){
		query = query.toLowerCase(Locale.getDefault());
		
		MatrixCursor foodCursor = new MatrixCursor(colNames);
		for(Food f:WirelessOrder.foodMenu.foods){
			if(f.getName().toLowerCase(Locale.getDefault()).contains(query) || f.getPinyin().contains(query) || f.getPinyinShortcut().contains(query))
				foodCursor.addRow(new Object[]{f.getAliasId(), f.getName(), f.getPrice(), R.drawable.main_search_list_add_selector});
		}
		
		return foodCursor;
	}
	
	@Override
	public String getType(Uri uri) {
		switch(sURIMatcher.match(uri)){
		case SEARCH_SUGGEST_CODE:
			return SearchManager.SUGGEST_MIME_TYPE;
		default:
			throw new IllegalStateException("Unknown Uri "+ uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException(TAG + " doesn't support the insert operation");
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException(TAG + " dosen't support the delete operation");
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException(TAG + " dosen't support the update operation");
	}

}

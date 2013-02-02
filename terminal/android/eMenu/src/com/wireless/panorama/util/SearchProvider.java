package com.wireless.panorama.util;

import java.util.Locale;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.excep.BusinessException;
import com.wireless.ordermenu.BuildConfig;
import com.wireless.ordermenu.R;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
/**
 * 此类为SearchView提供adapter或搜索数据，仅包含两个静态方法
 * @author ggdsn1
 *
 */
public class SearchProvider {
	
	//search suggestions key
	public static final String KEY_FOOD_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
	public static final String KEY_FOOD_PRICE = SearchManager.SUGGEST_COLUMN_TEXT_2;
	public static final String KEY_FOOD_ADD = SearchManager.SUGGEST_COLUMN_ICON_2;
	
	private static String[] ITEM_NAME = {
			BaseColumns._ID, KEY_FOOD_NAME, KEY_FOOD_PRICE, KEY_FOOD_ADD
	};
	
	private static int[] ITEM_ID = new int[]{
		0,
    		R.id.textView_main_search_list_item_name,
    		R.id.textView_main_search_list_item_price
    	};
	
	/**
	 * 返回一个cursorAdapter
	 * <p>该adapter可进行点菜操作，显示菜品名称和价格</p>
	 * @param context
	 * @return
	 */
	public static CursorAdapter getSuggestionsAdapter(Context context){
		return new SimpleCursorAdapter(context, R.layout.main_search_list_item, null,
				ITEM_NAME, ITEM_ID, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER){

					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						View layout = super.getView(position, convertView, parent);
						if(layout != null){

						View addBtn = layout.findViewById(R.id.button_main_search_list_item_add);
						//找到对应的cursor，并将id设为tag
						if(getCursor().moveToPosition(position)){
							addBtn.setTag(getCursor().getInt(0));
						}
						
						addBtn.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								int id = (Integer) v.getTag();
								
								if(id != 0){
									if(BuildConfig.DEBUG){
										Log.v("Suggetion add", "id : "+ id);
									}
									//根据id，找到对应的菜品，添加进购物车
									for(Food f:WirelessOrder.foodMenu.foods){
										if(f.getAliasId() == id){
											try {
												ShoppingCart.instance().addFood(new OrderFood(f));
												Toast.makeText(v.getContext(), "添加: " + f.getName() + "1份", Toast.LENGTH_SHORT).show();
											} catch (BusinessException e) {
												
											}
											break;
										}
									}
								}
							}
						});
					}
					return layout;
				}
		};
	}
	/**
	 * 根据请求，返回对应的菜品
	 * <p>支持拼音和名称</p>
	 * @param query
	 * @return
	 */
	public static Cursor getSuggestions(String query){
		query = query.toLowerCase(Locale.getDefault());
		
		MatrixCursor foodCursor = new MatrixCursor(ITEM_NAME);
		for(Food f:WirelessOrder.foodMenu.foods){
			if(f.getName().toLowerCase(Locale.getDefault()).contains(query) || f.getPinyin().contains(query) || f.getPinyinShortcut().contains(query))
				foodCursor.addRow(new Object[]{f.getAliasId(), f.getName(), f.getPrice(), R.drawable.main_search_list_add_selector});
		}
		
		return foodCursor;
	}
}

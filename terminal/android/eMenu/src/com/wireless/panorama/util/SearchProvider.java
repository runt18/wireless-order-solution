package com.wireless.panorama.util;

import java.util.Locale;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.excep.ProtocolException;
import com.wireless.ordermenu.BuildConfig;
import com.wireless.ordermenu.R;
import com.wireless.pojo.dishesOrder.Food;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.util.imgFetcher.ImageFetcher;
/**
 * 此类为SearchView提供adapter或搜索数据，仅包含两个静态方法。
 * <p>
 * {@link #getSuggestionsAdapter(Context)} 返回一个包装好的{@link SimpleCursorAdapter}
 * <br/>
 * <br/>
 * {@link #getSuggestions(String)} 根据请求返回匹配菜品的{@link Cursor}
 * @author ggdsn1
 *@see SimpleCursorAdapter
 *@see MatrixCursor
 */
public class SearchProvider {
	
	//search suggestions key
	public static final String KEY_FOOD_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
	public static final String KEY_FOOD_PRICE = SearchManager.SUGGEST_COLUMN_TEXT_2;
	public static final String KEY_FOOD_ADD = SearchManager.SUGGEST_COLUMN_ICON_2;
	
	private static String[] ITEM_NAME = {
		BaseColumns._ID, 
		KEY_FOOD_NAME, 
		KEY_FOOD_PRICE, 
		KEY_FOOD_ADD
	};
	
	private static int[] ITEM_ID = new int[]{
		0,
    	R.id.textView_main_search_list_item_name,
    	R.id.textView_main_search_list_item_price
    };
	
	/**
	 * 返回一个CursorAdapter
	 * <p>该adapter可进行点菜操作，显示菜品名称和价格</p>
	 * @param context
	 * @return
	 */
	public static CursorAdapter getSuggestionsAdapter(Context context){
		return new ImageSimpleCursorAdapter(context, 
											R.layout.main_search_list_item, 
											null,
											ITEM_NAME, 
											ITEM_ID, 
											CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER,
											new ImageFetcher(context, 50));
	}
	
	/**
	 * 根据请求，返回对应的菜品
	 * <p>支持拼音和名称，此处将匹配的菜品数组转化成一个{@link MatrixCursor} 作为返回值</p>
	 * @param query
	 * @return
	 */
	public static Cursor getSuggestions(String query){
		query = query.toLowerCase(Locale.getDefault());
		
		MatrixCursor foodCursor = new MatrixCursor(ITEM_NAME);
		for(Food f:WirelessOrder.foodMenu.foods){
			if(f.getName().toLowerCase(Locale.getDefault()).contains(query) || 
			   f.getPinyin().contains(query) || 
			   f.getPinyinShortcut().contains(query)){
				
				foodCursor.addRow(new Object[]{
									f.getAliasId(), 
									f.getName(), 
									f.getPrice(), 
									R.drawable.main_search_list_add_selector
								  });
			};
		}
		
		return foodCursor;
	}
	
}

class ImageSimpleCursorAdapter extends SimpleCursorAdapter {

	private ImageFetcher mImageFetcher;

	ImageSimpleCursorAdapter(Context context, int layout, Cursor c,	String[] from, int[] to, int flags, ImageFetcher fetcher) {
		super(context, layout, c, from, to, flags);
		mImageFetcher = fetcher;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View layout = super.getView(position, convertView, parent);
		if(layout != null){

			View addBtn = layout.findViewById(R.id.button_main_search_list_item_add);
			
			int id = getCursor().getInt(0);
			Food food = null;
			for(Food f : WirelessOrder.foodMenu.foods){
				if(f.getAliasId() == id){
					food = f;
					break;
				}
			}
			
			//售罄提示
			View sellOutHint = layout.findViewById(R.id.imageView_main_list_item_selloutSignal);

			if(food.isSellOut()){
				sellOutHint.setVisibility(View.VISIBLE);
				addBtn.setVisibility(View.INVISIBLE);
			} else {
				//如果不是售罄，则添加点菜按钮侦听
				addBtn.setVisibility(View.VISIBLE);
				sellOutHint.setVisibility(View.INVISIBLE); 
				
				//找到对应的cursor，并将id设为tag
				if(getCursor().moveToPosition(position)){
					addBtn.setTag(id);
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
							for(Food f : WirelessOrder.foodMenu.foods){
								if(f.getAliasId() == id){
									try {
										ShoppingCart.instance().addFood(new OrderFood(f));
										
										Toast toast = Toast.makeText(v.getContext(), f.getName() + " 已添加", Toast.LENGTH_SHORT);
										toast.setGravity(Gravity.TOP | Gravity.RIGHT, 0, 100);
										toast.show();
										
									} catch (ProtocolException e) {
										Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
									}
									break;
								}
							}
						}
					}
				});
			}
			//显示图片
			ImageView foodImage = (ImageView) layout.findViewById(R.id.imageView_main_search_list_item);
			if(food.hasImage()){
				mImageFetcher.loadImage(food.getImage(), foodImage);
			}else{
				foodImage.setImageResource(R.drawable.null_pic_small);
			}

		}
		return layout;
	}
}

package com.wireless.server;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.print.type.TypeContent;

public class PrinterLosses {

	private final static int LOSSES_AMOUNT = 50;
	
	private final static PrinterLosses ONLY_INSTANCE = new PrinterLosses();
	
	private final ConcurrentMap<Restaurant, List<TypeContent>> mPrinterLosses = new ConcurrentHashMap<Restaurant, List<TypeContent>>();
	
	private PrinterLosses(){
		
	}
	
	public static PrinterLosses instance(){
		return ONLY_INSTANCE;
	}
	
	public List<TypeContent> get(int restaurantId){
		return get(new Restaurant(restaurantId));
	}
	
	public List<TypeContent> get(Restaurant restaurant){
		List<TypeContent> result = mPrinterLosses.get(restaurant);
		if(result != null){
			return result;
		}else{
			return Collections.emptyList();
		}
	}
	
	public void add(int restaurantId, TypeContent content){
		add(new Restaurant(restaurantId), content);
	}
	
	public void add(Restaurant restaurant, TypeContent content){
		List<TypeContent> typeContents = mPrinterLosses.get(restaurant);
		if(typeContents != null){
			if(typeContents.size() >= LOSSES_AMOUNT){
				typeContents.remove(0);
			}
			typeContents.add(content);

		}else{
			typeContents = new CopyOnWriteArrayList<TypeContent>();
			typeContents.add(content);
			mPrinterLosses.put(restaurant, typeContents);
		}
	}
	
	public void remove(Restaurant restaurant){
		mPrinterLosses.remove(restaurant);
	}
	
	public Collection<Entry<Restaurant, List<TypeContent>>> stat(){
		return Collections.unmodifiableCollection(mPrinterLosses.entrySet());
	}
	
	public void clear(){
		mPrinterLosses.clear();
	}
	
}

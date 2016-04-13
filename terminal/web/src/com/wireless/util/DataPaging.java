package com.wireless.util;

import java.util.ArrayList;
import java.util.List;

public class DataPaging {
	
	/**
	 * 
	 * @param list
	 * @param isPaging
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	public static <T> List<T> getPagingData(List<T> list, boolean isPaging, int start, int limit) throws Exception{
		if(list != null && isPaging){
			int end = start + limit;
			end = end > list.size() ? list.size() : end;
			return new ArrayList<T>(list.subList(start, end));
		}else{
			return list;
		}
	}
	
	/**
	 * 
	 * @param list
	 * @param isPaging
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	public static <T> List<T> getPagingData(List<T> list, boolean isPaging, String start, String limit) throws Exception{
		if(start != null && limit != null){
			return DataPaging.getPagingData(list, isPaging, Integer.valueOf(start), Integer.valueOf(limit));
		}else{
			return list;
		}
	}
	
}

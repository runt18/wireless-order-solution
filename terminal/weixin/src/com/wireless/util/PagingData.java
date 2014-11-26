package com.wireless.util;

import java.util.ArrayList;
import java.util.List;

public class PagingData {
	
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
		List<T> root = null;
		if(list != null && isPaging){
			int dataIndex = start, dataSize = limit;
			root = new ArrayList<T>();
			dataSize = (dataIndex + dataSize) > list.size() ? dataSize - ((dataIndex + dataSize) - list.size()) : dataSize;
			for(int i = 0; i < dataSize; i++){
				root.add(list.get(dataIndex + i));
			}
		}else{
			root = list;
		}
		return root;
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
	public static <T> List<T> getPagingData(List<T> list, String isPaging, int start, int limit) throws Exception{
		return PagingData.getPagingData(list, Boolean.valueOf(isPaging), start, limit);
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
	public static <T> List<T> getPagingData(List<T> list, String isPaging, String start, String limit) throws Exception{
		return PagingData.getPagingData(list, Boolean.valueOf(isPaging), Integer.valueOf(start), Integer.valueOf(limit));
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
		return PagingData.getPagingData(list, isPaging, Integer.valueOf(start), Integer.valueOf(limit));
	}
	
}

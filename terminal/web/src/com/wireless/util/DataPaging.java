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
		if(isPaging != null)
			return DataPaging.getPagingData(list, Boolean.valueOf(isPaging), start, limit);
		else
			return list;
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
		if(isPaging != null && start != null && limit != null){
			if(isPaging.isEmpty()){
				isPaging = "false";
			}
			if(start.isEmpty()){
				start = "0";
			}
			return DataPaging.getPagingData(list, Boolean.valueOf(isPaging), Integer.valueOf(start), Integer.valueOf(limit));
		}else
			return list;
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
		if(start != null && limit != null)
			return DataPaging.getPagingData(list, isPaging, Integer.valueOf(start), Integer.valueOf(limit));
		else
			return list;
		
	}
	
}

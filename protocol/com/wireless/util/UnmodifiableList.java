package com.wireless.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class is a List implementation which is unmodifiable.
 * Optionally the element can be sorts by a comparator {@link Comparator} as list construction.
 * Once the elements are sorted, 
 * the method {@link #find(Object)} & {@link #containsElement(Object)}can be more faster since based on binary search.
 * 
 * @param <T>
 */
public class UnmodifiableList<T> extends AbstractList<T> {
	
	private final List<T> mList;
	
	private final Comparator<? super T> mComparator;
	
	public UnmodifiableList(List<T> listElem){
		this.mComparator = null;
		this.mList = new ArrayList<T>(listElem);
	}
	
	public UnmodifiableList(T[] arrayElem){
		this.mComparator = null;
		this.mList = Arrays.asList(arrayElem);
	}
	
	/**
	 * Construct a new instance using list and sorted elements by a given comparator.
	 * @param listElem
	 * @param comparator
	 */
    public UnmodifiableList(List<T> listElem, Comparator<? super T> comparator) {
    	this.mComparator = comparator;
    	this.mList = new ArrayList<T>(listElem);
    	if(this.mComparator != null){
    		Collections.sort(this.mList, this.mComparator);
    	}
    }
    
	/**
	 * Construct a new instance using array and sorted elements by a given comparator.
	 * @param listElem
	 * @param comparator
	 */
    public UnmodifiableList(T[] arrayElem, Comparator<? super T> comparator){
    	this.mList = new ArrayList<T>(Arrays.asList(arrayElem));
    	this.mComparator = comparator;
    	if(this.mComparator != null){
    		Collections.sort(this.mList, this.mComparator);
    	}
    }
    
    /**
     * Check if this list contains the given element. 
     * Using binary search if the comparator is defined, 
     * otherwise check each element in turn for equality with the specified element.
     * 
     * @param paramT
     * @return <code>true</code>, if the element is contained in this list;
     * <code>false</code>, otherwise.
     */
    public boolean containsElement(T paramT) {
    	if(mComparator != null){
    		return (Collections.binarySearch(this, paramT, mComparator) > -1);
    	}else{
    		return contains(paramT);
    	}
    }
    
    /**
     * Find the specified element according to a key element.
     * Using binary search if the comparator is defined, otherwise check each element in turn for equality with the specified element.
     * @param key the key to find the specified element
     * @return the element corresponding to the key or <code>null<code> if NOT found 
     */
    public T find(T key){
    	if(mComparator != null){
	    	int index = Collections.binarySearch(this, key, mComparator);
	    	if(index >= 0){
	    		return get(index);
	    	}else{
	    		return null;
	    	}
    	}else{
    		for(T elem : mList){
    			if(elem.equals(key)){
    				return elem;
    			}
    		}
    		return null;
    	}
    }
    
	@Override
	public T get(int location) {
		return mList.get(location);
	}
	
	@Override
	public int size() {
		return mList.size();
	}
}
package com.wireless.pojo.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
/**
 * This class is a List implementation which sorts the elements using the
 * comparator specified when constructing a new instance.
 * 
 * @param <T>
 */
public class SortedList<T> extends LinkedList<T> {
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Comparator used to sort the list.
     */
    private final Comparator<? super T> mComparator;
    
    /**
     * Construct a new instance with the list elements sorted in their
     * {@link java.lang.Comparable} natural ordering.
     */
    private SortedList() {
    	this.mComparator = null;
    }
    
    /**
     * Construct a new instance with the list elements sorted in their
     * {@link java.lang.Comparable} natural ordering.
     */
    public static <T> SortedList<T> newInstance(){
    	SortedList<T> instance = new SortedList<T>();
    	return instance;
    }
    
    /**
     * Construct a new instance with the specified list elements sorted in their
     * {@link java.lang.Comparable} natural ordering.
     * @param paramCollection
     * @return the new instance to sorted list
     */
    public static <T extends Comparable<? super T>> SortedList<T> newInstance(Collection<? extends T> paramCollection){
    	SortedList<T> instance = new SortedList<T>();
    	
    	for(T paramT : paramCollection){
    		instance.add(paramT);
    	}
    	
    	return instance;
    }
    
    /**
     * Construct a new instance with the specified list elements sorted using given comparator.
     * 
     * @param paramCollection
     * 
     * @param comparator
     */
    public static <T> SortedList<T> newInstance(Collection<? extends T> paramCollection, Comparator<? super T> comparator){
    	SortedList<T> instance = new SortedList<T>(comparator);
    	instance.addAll(paramCollection);
    	return instance;
    }
    

    /**
     * Construct a new instance using the given comparator.
     * 
     * @param comparator
     */
    private SortedList(Comparator<? super T> comparator) {
        this.mComparator = comparator;
    }
    
    /**
     * Construct a new instance using the given comparator.
     * 
     * @param comparator
     */
    public static <T> SortedList<T> newInstance(Comparator<? super T> comparator) {
    	SortedList<T> instance = new SortedList<T>(comparator);
    	return instance;
    }
    
    /**
     * Add a new entry to the list. The insertion point is calculated using the
     * comparator.
     * 
     * @param paramT
     */
    @Override
    public boolean add(T paramT) {
        int insertionPoint = Collections.binarySearch(this, paramT, mComparator);
        super.add((insertionPoint > -1) ? insertionPoint : (-insertionPoint) - 1, paramT);
        return true;
    }
    /**
     * Adds all elements in the specified collection to the list. Each element
     * will be inserted at the correct position to keep the list sorted.
     * 
     * @param paramCollection
     */
    @Override
    public boolean addAll(Collection<? extends T> paramCollection) {
        boolean result = false;
        for (T paramT:paramCollection) {
            result |= add(paramT);
        }
        return result;
    }
    
    /**
     * Check, if this list contains the given Element. 
     * 
     * @param Object the object to search
     * @return <code>true</code>, if the element is contained in this list;
     * <code>false</code>, otherwise.
     * @throws ClassCastException 
     * 			throws if the object to search can NOT cast to T
     */
    @SuppressWarnings("unchecked")
	@Override
    public boolean contains(Object obj){
    	return containsElement((T)obj);
    }
    
    
    /**
     * Check, if this list contains the given Element. This is faster than the
     * {@link #contains(Object)} method, since it is based on binary search.
     * 
     * @param paramT
     * @return <code>true</code>, if the element is contained in this list;
     * <code>false</code>, otherwise.
     */
    public boolean containsElement(T paramT) {
        return (Collections.binarySearch(this, paramT, mComparator) > -1);
    }
    
    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present based on binary search. 
     * @param paramT element to be removed from this list, if present
     * @return <tt>true</tt> if this list contained the specified element
     */
    public boolean removeElement(T paramT){
    	int index = indexOfElement(paramT);
    	if(index >= 0){
    		remove(index);
    		return true;
    	}else{
    		return false;
    	}
    }
    
    /**
     * Returns the index of the first occurrence of the specified element
     * in this list based on binary search, 
     * or (-(insertion point) - 1) if this list does not contain the element.
     *
     * @param ojb element to search for
     * @throws ClassCastException 
     * 			throws if the object passed is NOT the correct instance.
     * @return the index of the first occurrence of the specified element in
     *         this list, otherwise, (-(insertion point) - 1). 
     *         The insertion point is defined as the point at which the key would be inserted into the list: 
     *         the index of the first element greater than the key, 
     *         or list.size() if all elements in the list are less than the specified key.
     *         Note that this guarantees that the return value will be >= 0 if and only if the key is found.
     */
    @SuppressWarnings("unchecked")
	@Override
    public int indexOf(Object obj){
    	return indexOfElement((T)obj);
    }
    
    /**
     * Returns the index of the first occurrence of the specified element
     * in this list based on binary search, 
     * or (-(insertion point) - 1) if this list does not contain the element.
     *
     * @param paramT element to search for
     * @return the index of the first occurrence of the specified element in
     *         this list, otherwise, (-(insertion point) - 1). 
     *         The insertion point is defined as the point at which the key would be inserted into the list: 
     *         the index of the first element greater than the key, 
     *         or list.size() if all elements in the list are less than the specified key.
     *         Note that this guarantees that the return value will be >= 0 if and only if the key is found.
     */
    public int indexOfElement(T paramT){
    	return Collections.binarySearch(this, paramT, mComparator);
    }
    
    @Override
    public T get(int location){
    	if(location >= 0 && location < size()){
    		return super.get(location);
    	}else{
    		return null;
    	}
    }
    
    /**
     * Find the specified element according to a key element.
     * Using binary search if the comparator is defined, otherwise check each element in turn for equality with the specified element.
     * @param key the key to find the specified element
     * @return the element corresponding to the key or <code>null<code> if NOT found 
     */
    public T find(T key){
    	return get(indexOfElement(key));
    }
    
}
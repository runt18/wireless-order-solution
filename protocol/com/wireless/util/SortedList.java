package com.wireless.util;

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
    private final Comparator<? super T> comparator;
    
    /**
     * Construct a new instance with the list elements sorted in their
     * {@link java.lang.Comparable} natural ordering.
     */
    public SortedList() {
    	this.comparator = null;
    }
    
    /**
     * Construct a new instance with the specified list elements sorted in their
     * {@link java.lang.Comparable} natural ordering.
     * 
     * @param paramCollection
     */
    public SortedList(Collection<? extends T> paramCollection){
    	this();
    	addAll(paramCollection);
    }
    
    /**
     * Construct a new instance with the specified list elements sorted using given comparator.
     * 
     * @param paramCollection
     * 
     * @param comparator
     */
    public SortedList(Collection<? extends T> paramCollection, Comparator<? super T> comparator){
    	this.comparator = comparator;
    	addAll(paramCollection);
    }
    
    /**
     * Construct a new instance using the given comparator.
     * 
     * @param comparator
     */
    public SortedList(Comparator<? super T> comparator) {
        this.comparator = comparator;
    }
    /**
     * Add a new entry to the list. The insertion point is calculated using the
     * comparator.
     * 
     * @param paramT
     */
    @Override
    public boolean add(T paramT) {
        int insertionPoint = Collections.binarySearch(this, paramT, comparator);
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
     * Check, if this list contains the given Element. This is faster than the
     * {@link #contains(Object)} method, since it is based on binary search.
     * 
     * @param paramT
     * @return <code>true</code>, if the element is contained in this list;
     * <code>false</code>, otherwise.
     */
    public boolean containsElement(T paramT) {
        return (Collections.binarySearch(this, paramT, comparator) > -1);
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
     * @param paramT element to search for
     * @return the index of the first occurrence of the specified element in
     *         this list, otherwise, (-(insertion point) - 1). 
     *         The insertion point is defined as the point at which the key would be inserted into the list: 
     *         the index of the first element greater than the key, 
     *         or list.size() if all elements in the list are less than the specified key.
     *         Note that this guarantees that the return value will be >= 0 if and only if the key is found.

     */
    public int indexOfElement(T paramT){
    	return Collections.binarySearch(this, paramT, comparator);
    }
}
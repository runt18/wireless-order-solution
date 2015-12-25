package com.wireless.json;


public interface Jsonable {

	/**
	 * Flatten the object to a key-value map.
	 * @param flag additional flags about how the object should be flatten
	 * @return the key-value map to the object {@link JsonMap}
	 */
	public JsonMap toJsonMap(int flag);
	
	/**
	 * Create the object from key-value map.
	 * @param jm the key-value map to create object {@link JsonMap}
	 * @param flag additional flags about how the object should be created
	 */
	public void fromJsonMap(JsonMap jm, int flag);
	
    /**
     * Interface that must be implemented and provided as a public CREATOR
     * field that create a new instance of your Jsonable class.
     */
    public static interface Creator<T extends Jsonable>{
		/**
	     * Create the object.
		 * @return  an instance of the Jsonable class, with every entry initialized to null.
		 */
		 public T newInstance();
		 
	 }
	
}

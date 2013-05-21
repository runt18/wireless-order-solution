
package com.wireless.parcel;

/**
 * Interface for classes whose instances can be written to
 * and restored from a {@link Parcel}.  Classes implementing the Parcelable
 * interface must also have a static field called <code>CREATOR</code>, which
 * is an object implementing the {@link Parcelable.Creator Parcelable.Creator}
 * interface.
 * 
 * <p>A typical implementation of Parcelable is:</p>
 * 
 * <pre>
 * public class MyParcelable implements Parcelable {
 *     private int mData;
 *
 *     public void writeToParcel(Parcel dest, int flag) {
 *         dest.writeInt(mData);
 *     }
 *
 *	   public void createFromParcel(Parcel source){
 *		   mData = source.readInt();
 *	   }
 *
 *     public static final Parcelable.Creator MY_CREATOR
 *             = new Parcelable.Creator() {
 *             
 *         public Parcelable[] newInstance(int size) {
 *             return new MyParcelable[size];
 *         }
 *
 *         public Parcelable newInstance() {
 *             return new MyParcelable();
 *         }
 *     };
 * }</pre>
 */
public interface Parcelable {
    
    /**
     * Flatten this object in to a Parcel.
     * 
     * @param dest the Parcel in which the object should be written.
     * @param flag additional flags about how the object should be written.
     */
    public void writeToParcel(Parcel dest, int flag);

    /**
     * Create the Parcelable class from the given Parcel whose data had previously been written by
     * {@link Parcelable#writeToParcel Parcelable.writeToParcel()}.
     * 
     * @param source The Parcel to read the object's data from.
     */
    public void createFromParcel(Parcel source);
    
    /**
     * Interface that must be implemented and provided as a public CREATOR
     * field that create a new instance of your Parcelable class.
     */
    public interface Creator<T extends Parcelable>{
	    /**
		 * Create a new instance of the Parcelable class.
		 * 
		 * @return  an instance of the Parcelable class, with every entry initialized to null.
		 */
		 public T newInstance();
		 
        /**
         * Create a new array of the Parcelable class.
         * 
         * @param size Size of the array.
         * @return an array of the Parcelable class, with every entry
         * initialized to null.
         */
		 public T[] newInstance(int size);
	 }
	 
}

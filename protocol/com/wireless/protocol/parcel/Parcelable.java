
package com.wireless.protocol.parcel;

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
 *     public int describeContents() {
 *         return 0;
 *     }
 *
 *     public void writeToParcel(Parcel out, int flags) {
 *         out.writeInt(mData);
 *     }
 *
 *     public static final Parcelable.Creator&lt;MyParcelable&gt; CREATOR
 *             = new Parcelable.Creator&lt;MyParcelable&gt;() {
 *         public MyParcelable createFromParcel(Parcel in) {
 *             return new MyParcelable(in);
 *         }
 *
 *         public MyParcelable[] newArray(int size) {
 *             return new MyParcelable[size];
 *         }
 *     };
 *     
 *     private MyParcelable(Parcel in) {
 *         mData = in.readInt();
 *     }
 * }</pre>
 */
public interface Parcelable {
    
    /**
     * Flatten this object in to a Parcel.
     * 
     * @param dest The Parcel in which the object should be written.
     * @param flag Additional flags about how the object should be written.
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
    public interface Creator{
	    /**
		 * Create a new instance of the Parcelable class.
		 * 
		 * @return Returns an instance of the Parcelable class, with every entry
		 * initialized to null.
		 */
		 public Parcelable newInstance();
		 
        /**
         * Create a new array of the Parcelable class.
         * 
         * @param size Size of the array.
         * @return Returns an array of the Parcelable class, with every entry
         * initialized to null.
         */
		 public Parcelable[] newInstance(int size);
	 }
	 
}

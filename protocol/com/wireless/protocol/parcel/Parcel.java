package com.wireless.protocol.parcel;

import java.io.UnsupportedEncodingException;

public final class Parcel {

    /**
     * The minimum amount by which the capacity of a parcel will increase.
     * This tuning parameter controls a time-space tradeoff. This value (12)
     * gives empirically good results and is arguably consistent with the
     * RI's specified default initial capacity of 10: instead of 10, we start
     * with 0 (sans allocation) and jump to 12.
     */
    private static final int MIN_CAPACITY_INCREMENT = 12;
	
	private byte[] mRawData;
	
	// The total amount of space to this parcel.
	private int mDataCapacity;
	
	// The total amount of data contained in the parcel
	private int mDataSize;
	
	// The current position to this parcel
	private int mDataPosition;
	
	public Parcel(){
		 
	}
	
    /**
     * Returns the total amount of data contained in the parcel.
     */
    public int dataSize(){
    	return mDataSize;
    }
	
    /**
     * Returns the current position in the parcel data.  Never
     * more than {@link #dataSize}.
     */
    public int dataPosition(){
    	return mDataPosition;
    }
    
    /**
     * Returns the total amount of space in the parcel.  This is always
     * >= {@link #dataSize}.  The difference between it and dataSize() is the
     * amount of room left until the parcel needs to re-allocate its
     * data buffer.
     */
    public int dataCapacity(){
    	return mDataCapacity;
    }
    
	/**
	 * Returns the raw bytes of the parcel.
	 * @return the raw bytes to this parcel
	 */
	public byte[] marshall(){
		return trim();
	}
	
	/**
	 * Set the bytes in data to be the raw bytes of this Parcel.
	 * @param data the raw bytes to be marshal
	 */
	public void unmarshall(byte[] data){
		if(data != null){
			mRawData = data;
			mDataPosition = 0;
			mDataCapacity = mDataSize = data.length;
		}
	}

    /**
     * Read a byte value from the parcel at the current position.
     */
	public byte readByte(){
		mDataPosition += 1;
		return mRawData[mDataPosition];
	}
	
    /**
     * Write an integer value into the parcel at the current position,
     * growing data capacity if needed.
     */
	public void writeByte(byte val){
		allocate(1);
		mRawData[mDataPosition] = val;
		mDataPosition += 1;
	}
	
    /**
     * Read a short value from the parcel at the current position.
     */
	public int readShort(){
		mDataPosition += 2;
		return (mRawData[mDataPosition - 2] & 0x000000FF) |
			   ((mRawData[mDataPosition - 1] & 0x000000FF) << 8);
	}
	
    /**
     * Write a short value into the parcel at the current position,
     * growing data capacity if needed.
     */
	public void writeShort(int val){
		allocate(2);
		mRawData[mDataPosition] = (byte)(val & 0x000000FF);
		mRawData[mDataPosition + 1] = (byte)((val & 0x0000FF00) >> 8);
		mDataPosition += 2;
	}
	
    /**
     * Read an integer value from the parcel at the current position.
     */
	public int readInt(){
		mDataPosition += 4;
		return (mRawData[mDataPosition - 4] & 0x000000FF) |
			   ((mRawData[mDataPosition - 3] & 0x000000FF) << 8) |
			   ((mRawData[mDataPosition - 2] & 0x000000FF) << 16) |
			   ((mRawData[mDataPosition - 1] & 0x000000FF) << 24);
	}
	
    /**
     * Write an integer value into the parcel at the current position,
     * growing data capacity if needed.
     */
	public void writeInt(int val){
		allocate(4);
		mRawData[mDataPosition] = (byte)(val & 0x000000FF);
		mRawData[mDataPosition + 1] = (byte)((val & 0x0000FF00) >> 8);
		mRawData[mDataPosition + 2] = (byte)((val & 0x00FF0000) >> 16);
		mRawData[mDataPosition + 3] = (byte)((val & 0xFF000000) >> 24);
		mDataPosition += 4;
	}

    /**
     * Read a float value from the parcel at the current position.
     */
	public float readFloat(){
		mDataPosition += 4;
		int val = (mRawData[mDataPosition - 4] & 0x000000FF) |
				  ((mRawData[mDataPosition - 3] & 0x000000FF) << 8) |
				  ((mRawData[mDataPosition - 2] & 0x000000FF) << 16) |
				  ((mRawData[mDataPosition - 1] & 0x000000FF) << 24);
		return (float)val / 100;
	}
	
    /**
     * Write a float value into the parcel at the current position,
     * growing data capacity if needed.
     */
	public void writeFloat(float val){
		allocate(4);
		int valInt = Math.round(val * 100);
		mRawData[mDataPosition] = (byte)(valInt & 0x000000FF);
		mRawData[mDataPosition + 1] = (byte)((valInt & 0x0000FF00) >> 8);
		mRawData[mDataPosition + 2] = (byte)((valInt & 0x00FF0000) >> 16);
		mRawData[mDataPosition + 3] = (byte)((valInt & 0xFF000000) >> 24);
		mDataPosition += 4;
	}
	
    /**
     * Read a string value from the parcel at the current position.
     */
	public String readString(){
		
		// Get the length of string.
		int length = mRawData[mDataPosition];
		mDataPosition += 1;
		
		// Get the value of string.
		String val = null;
		try{
			val = new String(mRawData, mDataPosition, length, "UTF-16BE");
		}catch(UnsupportedEncodingException e){
			
		}
		mDataPosition += length;
		return val;
	}
	
    /**
     * Write a string value into the parcel at the current dataPosition(),
     * growing data capacity if needed.
     */
	public void writeString(String val){
		try{
			int length = val.length();
			byte[] bytesToString = val.getBytes("UTF-16BE");
			
			allocate(1 + bytesToString.length);
			
			// Assign the length of string
			mRawData[mDataPosition] = (byte)length;
			mDataPosition += 1;
		
			// Assign the value of string
			System.arraycopy(bytesToString, 0, mRawData, mDataPosition, length);
			mDataPosition += length;
			
		}catch(UnsupportedEncodingException e){
			
		}
	}
	
    /**
     * Read a particular object type from the parcel at the current dataPosition(). 
     * The object <em>must</em> have previously been written via {@link #writeTypedParcel} with the same
     * object type.
     *
     * @param dest The particular object to hold the result 
     * @see #writeTypedParcel
     */
	public <T extends Parcelable> void readTypedParcel(T dest){
		if(dest != null){
			dest.createFromParcel(this);
		}
	}
	
    /**
     * Write a particular object type to the parcel.
     *
     * @param src The particular object to be written
     */
	public <T extends Parcelable> void writeTypedParcel(T src, int flags){
		src.writeToParcel(this, flags);
	}
	
    /**
     * Read an array containing a particular object type from
     * the parcel at the current dataPosition(). The array <em>must</em> have
     * previously been written via {@link #writeTypedArray} with the same
     * object type.
     *
     * @param destArray The particular object array to hold the result 
     * @see #writeTypedArray
     */
	@SuppressWarnings("unchecked")
	public <T extends Parcelable> void readTypedArray(T[] destArray) {
        int amount = readInt();
        if (amount > 0) {
        
	        for (int i = 0; i < amount; i++) {
	            if (readInt() != 0) {
	            	if(destArray[i] == null){
	            		destArray[i] = (T)destArray[i].newInstance();
	            	}
	                readTypedParcel(destArray[i]);
	            }else{
	            	destArray[i] = null;
	            }
	        }
        }
    }
	
    /**
     * Flatten a heterogeneous array containing a particular object type into
     * the parcel, at
     * the current dataPosition() and growing dataCapacity() if needed.  The
     * type of the objects in the array must be one that implements Parcelable.
     * Unlike the {@link #writeParcelableArray} method, however, only the
     * raw data of the objects is written and not their type, so you must use
     * {@link #readTypedArray} with the correct corresponding
     * {@link Parcelable.Creator} implementation to unmarshall them.
     * @param <T>
     *
     * @param srcArray The array of objects to be written.
     * @param parcelableFlags Contextual flags as per
     * {@link Parcelable#writeToParcel(Parcel, int) Parcelable.writeToParcel()}.
     *
     * @see #readTypedArray
     */
    public <T extends Parcelable> void writeTypedArray(T[] srcArray, int parcelableFlags) {
        if (srcArray != null) {
            int length = srcArray.length;
            writeInt(length);
            for (int i = 0; i < length; i++) {
            	Parcelable item = srcArray[i];
                if (item != null) {
                    writeInt(1);
                    writeTypedParcel(item, parcelableFlags);
                } else {
                    writeInt(0);
                }
            }
        } else {
            writeInt(-1);
        }
    }
    
	/**
	 * 
	 * @param expectedBytes
	 */
	private void allocate(int expectedBytes){
		int expectedSize = mDataSize + expectedBytes;
		if(expectedSize <= mDataCapacity){			
			if(mRawData == null){		
				mDataCapacity = newCapacity(expectedSize);
				mRawData = new byte[mDataCapacity];
			}
			
		}else{
			mDataCapacity = newCapacity(expectedSize);
			byte[] tmp = mRawData;
			mRawData = new byte[mDataCapacity];
			System.arraycopy(tmp, 0, mRawData, 0, tmp.length);
			tmp = null;
		}
	}
	
    /**
     * Sets the capacity of this parcel to be the same as the current size.
     */
	private byte[] trim(){
		if(mDataSize != mDataCapacity && mRawData != null){
			byte[] tmp = mRawData;
			mRawData = new byte[mDataSize];
			mDataCapacity = mDataSize;
			System.arraycopy(tmp, 0, mRawData, 0, mDataSize);
			tmp = null;
		}
		return mRawData;
	}
	
    /**
     * This method controls the growth of parcel capacities.  It represents
     * a time-space tradeoff: we don't want to grow lists too frequently
     * (which wastes time and fragments storage), but we don't want to waste
     * too much space in unused excess capacity.
     */
    private static int newCapacity(int currentCapacity) {
        int increment = (currentCapacity < (MIN_CAPACITY_INCREMENT / 2) ?
                MIN_CAPACITY_INCREMENT : currentCapacity >> 1);
        return currentCapacity + increment;
    }
	
}

package com.wireless.parcel;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.pojo.util.NumericUtil;

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
	
	public Parcel(byte[] rawData){
		unmarshall(rawData);
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
	 * Read a boolean value(represented as 1 byte) from the parcel at the current position.
	 */
	public boolean readBoolean(){
		return readByte() == 1 ? true : false;
	}

    /**
     * Write a boolean value(represented as 1 byte) into the parcel at the current position,
     * growing data capacity if needed.
     */
	public void writeBoolean(boolean val){
		writeByte(val ? 1 : 0);
	}
	
    /**
     * Read 1-byte value from the parcel at the current position.
     */
	public short readByte(){
		mDataPosition += 1;
		return (short)(mRawData[mDataPosition - 1] & 0x00FF);
	}
	
    /**
     * Write 1-byte value into the parcel at the current position,
     * growing data capacity if needed.
     */
	public void writeByte(int val){
		allocate(1);
		mRawData[mDataPosition] = (byte)val;
		mDataPosition += 1;
	}
	
    /**
     * Read 2-byte value from the parcel at the current position.
     */
	public int readShort(){
		mDataPosition += 2;
		return (mRawData[mDataPosition - 2] & 0x000000FF) |
			   ((mRawData[mDataPosition - 1] & 0x000000FF) << 8);
	}
	
    /**
     * Write 2-byte value into the parcel at the current position,
     * growing data capacity if needed.
     */
	public void writeShort(int val){
		allocate(2);
		mRawData[mDataPosition] = (byte)(val & 0x000000FF);
		mRawData[mDataPosition + 1] = (byte)((val & 0x0000FF00) >> 8);
		mDataPosition += 2;
	}
	
    /**
     * Read 4-byte value from the parcel at the current position.
     */
	public int readInt(){
		mDataPosition += 4;
		return (mRawData[mDataPosition - 4] & 0x000000FF) |
			   ((mRawData[mDataPosition - 3] & 0x000000FF) << 8) |
			   ((mRawData[mDataPosition - 2] & 0x000000FF) << 16) |
			   ((mRawData[mDataPosition - 1] & 0x000000FF) << 24);
	}
	
    /**
     * Write -byte value into the parcel at the current position,
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
     * Read 8-byte value from the parcel at the current position.
     */
	public long readLong(){
		mDataPosition += 8;
		return (mRawData[mDataPosition - 8] &  0x00000000000000FFL) |
			   ((mRawData[mDataPosition - 7] & 0x00000000000000FFL) << 8) |
	   		   ((mRawData[mDataPosition - 6] & 0x00000000000000FFL) << 16) |
	   		   ((mRawData[mDataPosition - 5] & 0x00000000000000FFL) << 24) |
	   		   ((mRawData[mDataPosition - 4] & 0x00000000000000FFL) << 32) |
	   		   ((mRawData[mDataPosition - 3] & 0x00000000000000FFL) << 40) |
	   		   ((mRawData[mDataPosition - 2] & 0x00000000000000FFL) << 48) |
			   ((mRawData[mDataPosition - 1] & 0x00000000000000FFL) << 56);
	}
	
    /**
     * Write 8-byte value into the parcel at the current position,
     * growing data capacity if needed.
     */
	public void writeLong(long val){
		allocate(8);
		mRawData[mDataPosition] = (byte)(val & 0x00000000000000FFL);
		mRawData[mDataPosition + 1] = (byte)((val & 0x000000000000FF00L) >> 8);
		mRawData[mDataPosition + 2] = (byte)((val & 0x0000000000FF0000L) >> 16);
		mRawData[mDataPosition + 3] = (byte)((val & 0x00000000FF000000L) >> 24);
		mRawData[mDataPosition + 4] = (byte)((val & 0x000000FF00000000L) >> 32);
		mRawData[mDataPosition + 5] = (byte)((val & 0x0000FF0000000000L) >> 40);
		mRawData[mDataPosition + 6] = (byte)((val & 0x00FF000000000000L) >> 48);
		mRawData[mDataPosition + 7] = (byte)((val & 0xFF00000000000000L) >> 56);
		mDataPosition += 8;
	}
	
    /**
     * Read a float value from the parcel at the current position.
     */
	public float readFloat(){
		return (float)readInt() / 100;
	}
	
    /**
     * Write a float value into the parcel at the current position,
     * growing data capacity if needed.
     */
	public void writeFloat(float val){
		writeInt(NumericUtil.float2Int(val));
	}
	
    /**
     * Read a string value from the parcel at the current position.
     */
	public String readString(){
		if(readByte() != 0){
			// Get the length of string.
			int length = (mRawData[mDataPosition] & 0x000000FF) | ((mRawData[mDataPosition + 1] & 0x000000FF) << 8);
			mDataPosition += 2;
			
			// Get the value of string.
			String val = null;
			try{
				val = new String(mRawData, mDataPosition, length, "UTF-16BE");
			}catch(UnsupportedEncodingException e){
				
			}
			mDataPosition += length;
			return val;
			
		}else{
			return null;
		}
	}
	
    /**
     * Write a string value into the parcel at the current dataPosition(),
     * growing data capacity if needed.
     */
	public void writeString(String val){
		if(val != null){
			
			writeByte(1);
			
			byte[] bytesToString;
			try{
				bytesToString = val.getBytes("UTF-16BE");
			}catch(UnsupportedEncodingException e){
				bytesToString = new byte[0];
			}
			
			allocate(2 + bytesToString.length);
			
			// Assign the length of string
			mRawData[mDataPosition] = (byte)(bytesToString.length & 0x000000FF);
			mRawData[mDataPosition + 1] = (byte)((bytesToString.length & 0x0000FF00) >> 8);
			mDataPosition += 2;
		
			// Assign the value of string
			System.arraycopy(bytesToString, 0, mRawData, mDataPosition, bytesToString.length);
			mDataPosition += bytesToString.length;
			
		}else{
			writeByte(0);
		}
	}
	
    /**
     * Read a particular object type from the parcel at the current dataPosition(). 
     * The object <em>must</em> have previously been written via {@link #writeParcel} with the same
     * object type.
     *
     * @param creator The creator to the parcelable object you want to create
     * @return Returns the newly created Parcelable, or null if a null
     * 		   object has been written.
     * @see #writeParcel
     */
	public <T extends Parcelable> T readParcel(Parcelable.Creator<T> creator){
		boolean isNull = (readByte() == 0);
		if(creator != null && !isNull){
			T parcelObj = creator.newInstance();
			parcelObj.createFromParcel(this);
			return parcelObj;
		}else{
			return null;
		}
	}
	
    /**
     * Write a particular object type to the parcel.
     *
     * @param src The particular object to be written
     */
	public void writeParcel(Parcelable src, int flag){
		if(src != null){
			writeByte(1);
			src.writeToParcel(this, flag);
		}else{
			writeByte(0);
		}
	}
	
    /**
     * Read an array containing a particular object type from
     * the parcel at the current dataPosition(). The array <em>must</em> have
     * previously been written via {@link #writeParcelArray(Parcelable[], int)} with the same
     * object type.
     *
     * @param creator the creator to show how to create 
     * @return A newly created array containing objects with the same data
     *         as those that were previously written.
     * @see #writeParcelArray
     */
	public <T extends Parcelable> T[] readParcelArray(Parcelable.Creator<T> creator) {
		if(readByte() != 0){
			T[] destArray;
	        int amount = readShort();
	        if (amount > 0) {
	        	
	        	destArray = creator.newInstance(amount);
	        	for(int i = 0; i < destArray.length; i++){
	        		destArray[i] = readParcel(creator);
	        	}
	        	
	        }else{
	        	destArray = creator.newInstance(0);
	        }
	        
	        return destArray;
	        
		}else{
			return null;
		}
    }
	
    /**
     * Flatten a heterogeneous array containing a particular object type into
     * the parcel, at the current position and growing capacity if needed.  The
     * type of the objects in the array must be one that implements Parcelable.
     *
     * @param srcArray The array of objects to be written.
     * @param flag Contextual flags as per
     * {@link Parcelable#writeToParcel(Parcel, short) Parcelable.writeToParcel()}.
     *
     * @see #readParceldArray
     */
    public void writeParcelArray(Parcelable[] srcArray, int flag) {
        if (srcArray != null) {
        	
        	writeByte(1);
        	
            writeShort(srcArray.length);
            for (Parcelable item : srcArray) {
            	writeParcel(item, flag);
            }
        } else {
            writeByte(0);
        }
    }
    
    /**
     * Read list containing a particular object type from
     * the parcel at the current dataPosition(). The list <em>must</em> have
     * previously been written via {@link #writeParcelList(List, int)} with the same
     * object type.
     *
     * @param creator the creator to show how to create 
     * @return A newly created list containing objects with the same data
     *         as those that were previously written.
     */
    public <T extends Parcelable> List<T> readParcelList(Parcelable.Creator<T> creator){
		if(readByte() != 0){
			
			List<T> destList = new ArrayList<T>();
			
	        int amount = readShort();
	        for(int i = 0; i < amount; i++){
	        	destList.add(readParcel(creator));
	        }
	        
	        return destList;
	        
		}else{
			return null;
		}
    }
    
    /**
     * Flatten a heterogeneous list containing a particular object type into
     * the parcel, at the current position and growing capacity if needed.  The
     * type of the objects in the array must be one that implements Parcelable.
     *
     * @param srcList The list of objects to be written.
     * @param flag Contextual flags as per
     * {@link Parcelable#writeToParcel(Parcel, short) Parcelable.writeToParcel()}.
     *
     * @see #readParceldList
     */
    public void writeParcelList(List<? extends Parcelable> srcList, int flag){
    	if(srcList != null){
    		
    		writeByte(1);
    		
    		writeShort(srcList.size());
    		for(Parcelable item : srcList){
    			writeParcel(item, flag);
    		}
    	}else{
    		writeByte(0);
    	}
    }
    
	/**
	 * 
	 * @param expectedBytes
	 */
	private void allocate(int expectedBytes){
		
		int expectedSize = mDataSize + expectedBytes;
		
		if(mRawData != null){
			if(expectedSize > mDataCapacity){
				mDataCapacity = newCapacity(expectedSize);
				byte[] tmp = mRawData;
				mRawData = new byte[mDataCapacity];
				System.arraycopy(tmp, 0, mRawData, 0, tmp.length);
				tmp = null;
			}
		}else{
			mDataCapacity = newCapacity(expectedSize);
			mRawData = new byte[mDataCapacity];
		}
		
		mDataSize = expectedSize;
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
        int increment = (currentCapacity < (MIN_CAPACITY_INCREMENT / 2) ? MIN_CAPACITY_INCREMENT : currentCapacity >> 1);
        return currentCapacity + increment;
    }
	
}

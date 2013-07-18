package com.wireless.pojo.printScheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.exception.BusinessException;

public class Printer {

	private int mId;
	
	private final int mRestaurantId;
	
	private final String mName;
	
	private String mAlias;
	
	private final PStyle mStyle;
	
	private List<PrintFunc> mFuncs = new ArrayList<PrintFunc>();
	
	public static class InsertBuilder{
		private final String mName;
		private final PStyle mStyle;
		private final int mRestaurantId;
		private String mAlias;
		
		public InsertBuilder(String name, PStyle style, int restaurantId){
			mName = name;
			mStyle = style;
			mRestaurantId = restaurantId;
		}
		
		public InsertBuilder setAlias(String alias){
			mAlias = alias;
			return this;
		}
		
		public Printer build(){
			return new Printer(this);
		}
	}
	
	private Printer(InsertBuilder builder){
		this(builder.mName, builder.mStyle, builder.mRestaurantId);
		this.mAlias = builder.mAlias;
	}
	
	private Printer(String name, PStyle style, int restaurantId){
		this.mName = name;
		this.mStyle = style;
		this.mRestaurantId = restaurantId;
	}
	
	public String getAlias(){
		if(mAlias == null){
			mAlias = "";
		}
		return mAlias;
	}
	
	public String getName(){
		return mName;
	}
	
	public PStyle getStyle(){
		return mStyle;
	}

	public int getRestaurantId(){
		return mRestaurantId;
	}
	
	public int getId(){
		return mId;
	}
	
	public void setId(int id){
		mId = id;
	}
	
	/**
	 * Check to see whether the print function is contained in printer.
	 * @param func the print function to check
	 * @return true if the function is contained in printer, otherwise false
	 */
	public boolean contains(PrintFunc func){
		return mFuncs.contains(func);
	}

	/**
	 * Add the print function to printer
	 * @param func the function to add
	 * @throws BusinessException
	 * 			throws if the print function has exist before
	 */
	public void addFunc(PrintFunc func) throws BusinessException{
		if(contains(func)){
			throw new BusinessException("Printfunc " + func + " has exist before.");
		}else{
			mFuncs.add(func);
		}
	}
	
	/**
	 * Remove the specific print function from this printer.
	 * @param func the print function to remove
	 * @return true if succeed to function remove, otherwise false 
	 */
	public boolean removeFunc(PrintFunc func){
		return mFuncs.remove(func);
	}
	
	public List<PrintFunc> getPrintFuncs(){
		return Collections.unmodifiableList(mFuncs);
	}
	
	@Override
	public String toString(){
		return mName;
	}
	
	@Override
	public int hashCode(){
		int result = 17;
		result = result * 31 + mName.hashCode();
		result = result * 31 + mRestaurantId;
		return result;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Printer)){
			return false;
		}else{
			return mRestaurantId == ((Printer)obj).mRestaurantId && mName.equals(((Printer)obj).mName);
		}
	}
}

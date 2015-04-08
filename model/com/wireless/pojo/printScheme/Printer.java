package com.wireless.pojo.printScheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.exception.BusinessException;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class Printer implements Jsonable{

	private int mId;
	
	private int mRestaurantId;
	
	private String mName;
	
	private String mAlias;
	
	private PStyle mStyle;
	
	private boolean isEnabled;
	
	private List<PrintFunc> mFuncs = new ArrayList<PrintFunc>();
	
	public static class InsertBuilder{
		private final String mName;
		private final PStyle mStyle;
		private String mAlias;
		private boolean isEnabled = true;
		
		public InsertBuilder(String name, PStyle style){
			mName = name;
			mStyle = style;
		}
		
		public InsertBuilder setAlias(String alias){
			mAlias = alias;
			return this;
		}

		public InsertBuilder setEnabled(boolean isEnabled){
			this.isEnabled = isEnabled;
			return this;
		}
		
		public Printer build(){
			return new Printer(this);
		}
	}
	
	public static class UpdateBuilder{
		private String mName;
		private PStyle mStyle;
		private final int mPrinterId;
		private String mAlias;
		private int isEnabled = -1;
		
		public UpdateBuilder(int printerId){
			mPrinterId = printerId;
		}
		
		public boolean isNameChanged(){
			return this.mName != null;
		}
		
		public UpdateBuilder setName(String name){
			this.mName = name;
			return this;
		}
		
		public boolean isStyleChanged(){
			return this.mStyle != null;
		}
		
		public UpdateBuilder setStyle(PStyle style){
			this.mStyle = style;
			return this;
		}
		
		public boolean isAliasChanged(){
			return this.mAlias != null;
		}
		
		public UpdateBuilder setAlias(String alias){
			mAlias = alias;
			return this;
		}
		
		public boolean isEnabledChanged(){
			return this.isEnabled == -1;
		}
		
		public UpdateBuilder setEnabled(boolean isEnabled){
			this.isEnabled = isEnabled ? 1 : 0;
			return this;
		}
		
		public Printer build(){
			return new Printer(this);
		}
	}
	
	private Printer(InsertBuilder builder){
		this.mName = builder.mName;
		this.mStyle = builder.mStyle;
		this.isEnabled = builder.isEnabled;
		this.mAlias = builder.mAlias;
	}
	
	private Printer(UpdateBuilder builder){
		this.mId = builder.mPrinterId;
		this.mName = builder.mName;
		this.mStyle = builder.mStyle;
		this.isEnabled = builder.isEnabled == 1;
		this.mAlias = builder.mAlias;
	}
	
	public Printer(int id){
		this.mId = id;
	}
	
	public void setAlias(String alias){
		mAlias = alias;
	}
	
	public String getAlias(){
		if(mAlias == null){
			return "";
		}
		return mAlias;
	}
	
	public String getName(){
		if(mName == null){
			return "";
		}
		return mName;
	}
	
	public void setName(String name){
		mName = name;
	}
	
	public PStyle getStyle(){
		return mStyle;
	}

	public void setStyle(PStyle style){
		mStyle = style;
	}
	
	public void setRestaurantId(int restaurantId){
		this.mRestaurantId = restaurantId;
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
	
	public boolean isEnabled(){
		return this.isEnabled;
	}
	
	public void setEnabled(boolean isEnabled){
		this.isEnabled = isEnabled;
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
			throw new BusinessException("【" + func + "】 has exist before.");
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

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("printerId", this.mId);
		jm.putInt("restaurantId", this.mRestaurantId);
		jm.putString("name", this.mName);
		jm.putString("alias", this.mAlias);
		jm.putInt("styleValue", this.mStyle.getVal());
		jm.putString("styleText", this.mStyle.getDesc());
		jm.putBoolean("isEnabled", this.isEnabled);
		jm.putJsonableList("printFunc", this.mFuncs, 0);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}

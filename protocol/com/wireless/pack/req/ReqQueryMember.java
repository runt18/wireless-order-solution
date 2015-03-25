package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.staffMgr.Staff;

public class ReqQueryMember extends RequestPackage {

	public static class ExtraCond implements Parcelable{
		private int id;
		private String fuzzy;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public int getId(){
			return this.id;
		}
		
		public ExtraCond setFuzzyName(String fuzzy){
			this.fuzzy = fuzzy;
			return this;
		}
		
		public String getFuzzyName(){
			return this.fuzzy;
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flag) {
			dest.writeInt(this.id);
			dest.writeString(this.fuzzy);
		}

		@Override
		public void createFromParcel(Parcel source) {
			this.id = source.readInt();
			this.fuzzy = source.readString();
		}
		
		public final static Parcelable.Creator<ExtraCond> CREATOR = new Parcelable.Creator<ExtraCond>() {
			
			public ExtraCond[] newInstance(int size) {
				return new ExtraCond[size];
			}
			
			public ExtraCond newInstance() {
				return new ExtraCond();
			}
		};
	}
	
	public ReqQueryMember(Staff staff, ExtraCond extraCond){
		super(staff);
		header.mode = Mode.MEMBER;
		header.type = Type.QUERY_MEMBER;
		fillBody(extraCond, 0);
	}

}
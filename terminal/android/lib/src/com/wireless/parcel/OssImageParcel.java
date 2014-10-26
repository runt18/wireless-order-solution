package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.oss.OssImage;

public class OssImageParcel implements Parcelable{

	public static final String KEY_VALUE = OssImageParcel.class.getName();
	
	private final OssImage mSrcImage;
	
	public OssImageParcel(OssImage image){
		this.mSrcImage = image;
	}
	
	public OssImage asImage(){
		return this.mSrcImage;
	}

	private OssImageParcel(Parcel in){
		if(in.readInt() != 1){
			mSrcImage = new OssImage(in.readInt());
			mSrcImage.setImage(in.readString());
		}else{
			mSrcImage = null;
		}
	}
	
	public static final Parcelable.Creator<OssImageParcel> CREATOR = new Parcelable.Creator<OssImageParcel>() {
		@Override
		public OssImageParcel createFromParcel(Parcel in) {
			return new OssImageParcel(in);
		}

		@Override
		public OssImageParcel[] newArray(int size) {
			return new OssImageParcel[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if(mSrcImage == null){
			dest.writeInt(1);
		}else{
			dest.writeInt(0);
			dest.writeInt(mSrcImage.getId());
			dest.writeString(mSrcImage.getImage());
		}
	}


}

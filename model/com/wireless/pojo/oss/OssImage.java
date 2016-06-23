package com.wireless.pojo.oss;

import java.io.InputStream;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;

public class OssImage implements Jsonable, Parcelable{

	public static class Params{
		
		private static Params instance;
		
		private final String bucket;
		private final OSSParams ossParam;
		private Params(String bucket, OSSParams ossParam){
			this.bucket = bucket;
			this.ossParam = ossParam;
		}
		
		public static void init(String bucket, OSSParams ossParam){
			instance = new Params(bucket, ossParam);
		}
		
		public String getBucket(){
			return this.bucket;
		}
		
		public OSSParams getOssParam(){
			return this.ossParam;
		}
		
		public static Params instance(){
			if(instance != null){
				return instance;
			}else{
				throw new NullPointerException("The parameter to oss image has NOT been initalize.");
			}
		}
	}
	
	public static enum ImageType{
		JPG("jpg"),
		JPEG("jpeg"),
		PNG("png"),
		BMP("bmp"),
		GIF("gif");
		
		private final String suffix;
		
		ImageType(String suffix){
			this.suffix = suffix;
		}
		
		public String getSuffix(){
			return this.suffix;
		}
		
		public static ImageType valueOf(String val, int flag){
			for(ImageType type : values()){
				if(type.suffix.equalsIgnoreCase(val)){
					return type;
				}
			}
			throw new IllegalArgumentException("【" + val + "】的图片类型暂不支持");
		}
		
		@Override
		public String toString(){
			return suffix;
		}
	}
	
	public static enum Status{
		SINGLE(1, "未关联"),
		MARRIED(2, "已关联");
		
		private final int val;
		private final String desc;
		
		Status(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static Status valueOf(int val){
			for(Status status : values()){
				if(status.val == val){
					return status;
				}
			}
			throw new IllegalArgumentException("The status(val = " + val + ") pass is invalid.");
		}
		
		public int getVal(){
			return val;
		}
		
		@Override
		public String toString(){
			return desc;
		}
	}
	
	public static enum AssociatedType{
		SINGLE("独自关联"),
		MULTI("多个关联");
		
		private final String desc;
		
		private AssociatedType(String desc) {
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return desc;
		}
	}
	
	public static class InsertBuilder{
		private String image;
		private ImageType imgType;
		private InputStream istream;
		private Dimension thumbnail;
		
		private final OssImage.Type type;
		private final int associatedId;
		private final String associatedSerial;
		private final Status status;
		
		public InsertBuilder(OssImage.Type type){
			this.type = type;
			this.thumbnail = type.thumbnail;
			this.associatedId = 0;
			this.associatedSerial = null;
			this.status = Status.SINGLE;
		}
		
		public InsertBuilder(OssImage.Type type, int associatedId){ 
			this.type = type;
			this.thumbnail = type.thumbnail;
			this.associatedId = associatedId;
			this.associatedSerial = null;
			this.status = Status.MARRIED;
		}
		
		public InsertBuilder(OssImage.Type type, String associatedSerial){ 
			this.type = type;
			this.thumbnail = type.thumbnail;
			this.associatedId = 0;
			this.associatedSerial = associatedSerial;
			this.status = Status.MARRIED;
		}
		
		public InsertBuilder setImgResource(ImageType imgType, InputStream istream){
			this.image = null;
			this.imgType = imgType;
			this.istream = istream;
			return this;
		}
		
		public InsertBuilder setImgResource(String image, InputStream istream){
			this.image = image;
			this.imgType = ImageType.valueOf(image.substring(image.lastIndexOf(".") + 1, image.length()), 0);
			this.istream = istream;
			return this;
		}
		
		public InsertBuilder setThumbnailSize(Dimension dimension){
			this.thumbnail = dimension;
			return this;
		}
		
		public ImageType getImgType(){
			return this.imgType;
		}
		
		public InputStream getImgStream(){
			return this.istream;
		}
		
		public boolean hasImgResource(){
			return imgType != null && istream != null;
		}
		
		public boolean hasThumbnail(){
			return this.thumbnail != null;
		}
		
		public Dimension getThumbnailSize(){
			return this.thumbnail;
		}
		
		public OssImage build(){
			return new OssImage(this);
		}
		
	}
	
	public static class UpdateBuilder{
		private final int id;
		
		private ImageType imgType;
		private String image;
		private InputStream istream;
		
		private OssImage.Type type;
		private int associatedId;
		private String associatedSerial;
		private AssociatedType associatedType;
		private Dimension thumbnail;
		private Status status;
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public UpdateBuilder setAssociated(OssImage.Type type, int associatedId){ 
			return setAssociated(type, associatedId, AssociatedType.MULTI);
		}

		public UpdateBuilder setSingleAssociated(OssImage.Type type, int associatedId){ 
			return setAssociated(type, associatedId, AssociatedType.SINGLE);
		}
		
		public UpdateBuilder setAssociated(OssImage.Type type, int associatedId, AssociatedType associatedType){ 
			this.type = type;
			this.thumbnail = type.thumbnail;
			this.associatedId = associatedId;
			this.associatedSerial = null;
			this.associatedType = associatedType;
			this.status = Status.MARRIED;
			return this;
		}

		public boolean isSingleAssociated(){
			return associatedType == AssociatedType.SINGLE;
		}
		
		public UpdateBuilder setAssociated(OssImage.Type type, String associatedSerial){ 
			this.type = type;
			this.associatedId = 0;
			this.associatedSerial = associatedSerial;
			this.status = Status.MARRIED;
			return this;
		}
		
		public UpdateBuilder setImgResource(ImageType imgType, InputStream istream){
			this.imgType = imgType;
			this.istream = istream;
			this.image = null;
			return this;
		}
		
		public UpdateBuilder setImgResource(String image, InputStream istream){
			this.image = image;
			this.imgType = ImageType.valueOf(image.substring(image.lastIndexOf(".") + 1, image.length()), 0);
			this.istream = istream;
			return this;
		}
		
		public UpdateBuilder setThumbnailSize(Dimension dimension){
			this.thumbnail = dimension;
			return this;
		}
		
		public Dimension getThumbnailSize(){
			return this.thumbnail;
		}
		
		public boolean isThumbnailChanged(){ 
			return this.thumbnail != null;
		}
		
		public boolean isAssociatedChanged(){
			return this.type != null && (this.associatedId != 0 || this.associatedSerial != null);
		}
		
		public boolean isImgResourceChanged(){
			return this.imgType != null && istream != null;
		}
		
		public ImageType getImgType(){
			return this.imgType;
		}
		
		public InputStream getImgStream(){
			return this.istream;
		}
		
		public OssImage build(){
			return new OssImage(this);
		}
	}
	
	public static class UpdateBuilder4Html{
		private final OssImage.Type type;
		private final int associatedId;
		private final String associatedSerial;
		
		private String htmlBody;
		
		public UpdateBuilder4Html(OssImage.Type type, int associatedId){
			this.type = type;
			this.associatedId = associatedId;
			this.associatedSerial = null;
		}
		
		public UpdateBuilder4Html(OssImage.Type type, String associatedSerial){
			this.type = type;
			this.associatedId = 0;
			this.associatedSerial = associatedSerial;
		}

		public UpdateBuilder4Html setHtml(String html){
			this.htmlBody = html;
			return this;
		}
		
		public String getHtml(){
			return this.htmlBody;
		}
		
		public OssImage build(){
			return new OssImage(this);
		}
	}
	
	public static enum Type{
		WX_PROMOTION(1, "WxPromotion", 100, null, "微信优惠活动"),
		WX_FINANCE(2, "WxFinance", 100, null, "微信财务端"),
		WX_COUPON_TYPE(3, "WxCouponType", 100, null, "微信优惠券类型"),
		FOOD_IMAGE(4, "FoodImage", 300, new Dimension(500, 400), "菜品图片"),
		PROMOTION(6, "Promotion", 300, null, "优惠活动全图"),
		WX_LOGO(7, "WxLogo", 100, null, "微信餐厅Logo"),
		WX_INFO(8, "WxInfo", 300, null, "微信餐厅简介"),
		BILL_BOARD(9, "BillBoard", 300, null, "公告信息"),
		WX_REPLY(10, "WxAction", 300, null, "微信图文回复大图"),
		WX_REPLY_S(11, "WxAction", 300, null, "微信图文回复小图"),
		WX_REPRESENT(12, "WxRepresent", 300, null, "微信我要代言"),
		WX_CARD(13, "WxCard", 100, null, "微信会员卡"),
		THUMB_NAIL(100, "thumbnail", 100, null, "缩略图");
		
		private final int val;
		private final String dir;
		private final int size;			//unit is kb
		private final Dimension thumbnail;	//the default size of thumb nail
		private final String desc;
		
		Type(int val, String dir, int size, Dimension thumbnail, String desc){
			this.val = val;
			this.dir = dir;
			this.size = size;
			this.thumbnail = thumbnail;
			this.desc = desc;
		}
		
		public static Type valueOf(int val){
			for(Type type : values()){
				if(type.val == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The type(val = " + val + ") passed is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
		public String getDir(){
			return this.dir;
		}
		
		public int getSize(){
			return this.size * 1024;
		}
		
		public boolean hasThumbnail(){
			return this.thumbnail != null;
		}
		
		public Dimension getThumbnailSize(){
			return this.thumbnail;
		}
		
		@Override
		public String toString(){
			return desc;
		}
		
	}
	
	private int id;
	private String image;
	private int restaurantId;
	private Type type;
	private Status status;
	private int associatedId;
	private String associatedSerial;
	private long lastModified;
	private OssImage thumbnail;
	
	private OssImage(InsertBuilder builder){
		this.associatedId = builder.associatedId;
		this.associatedSerial = builder.associatedSerial;
		this.image = builder.image;
		this.type = builder.type;
		this.image = builder.image;
		this.status = builder.status;
	}
	
	private OssImage(UpdateBuilder builder){
		this.id = builder.id;
		this.associatedId = builder.associatedId;
		this.associatedSerial = builder.associatedSerial;
		this.image = builder.image;
		this.type = builder.type;
		this.image = builder.image;
		this.status = builder.status;
	}
	
	private OssImage(UpdateBuilder4Html builder){
		this.associatedId = builder.associatedId;
		this.associatedSerial = builder.associatedSerial;
		this.type = builder.type;
	}
	
	public OssImage(int id){
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getImage() {
		if(image == null){
			return "";
		}
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getObjectKey(){
		return type.getDir() + "/" + restaurantId + "/" + image;
	}
	
	public String getObjectUrl(){
		OssImage.Params param = OssImage.Params.instance();
		return "http://" + param.getBucket() + "." + param.getOssParam().OSS_OUTER_POINT + "/" + getObjectKey();
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setStatus(Status status){
		this.status = status;
	}
	
	public Status getStatus(){
		return this.status;
	}
	
	public int getAssociatedId() {
		return associatedId;
	}

	public void setAssociatedId(int associatedId) {
		this.associatedId = associatedId;
	}

	public String getAssociatedSerial(){
		if(associatedSerial == null){
			return "";
		}
		return associatedSerial;
	}
	
	public void setAssociatedSerial(String associatedSerial){
		this.associatedSerial = associatedSerial;
	}
	
	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
	
	public boolean hasThumbnail(){
		return thumbnail != null;
	}
	
	public void setThumbnail(OssImage thumbnail){
		this.thumbnail = thumbnail;
	}
	
	public OssImage getThumbnail(){
		return this.thumbnail;
	}
	
	@Override
	public int hashCode(){
		return this.id * 17 + 31;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof OssImage)){
			return false;
		}else{
			return this.id == ((OssImage)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return getObjectKey();
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("image", this.getObjectUrl());
		jm.putString("thumbnail", this.hasThumbnail() ? this.thumbnail.getObjectUrl() : "");
		jm.putInt("imageId", this.getId());
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeString(getImage());
	}

	@Override
	public void createFromParcel(Parcel source) {
		setImage(source.readString());
	}
	
	public final static Parcelable.Creator<OssImage> CREATOR = new Parcelable.Creator<OssImage>(){

		@Override
		public OssImage newInstance() {
			return new OssImage(0);
		}
		
		@Override
		public OssImage[] newInstance(int size){
			return new OssImage[size];
		}
		
	};
}

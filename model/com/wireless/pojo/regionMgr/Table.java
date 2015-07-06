package com.wireless.pojo.regionMgr;

import java.util.ArrayList;
import java.util.List;

import com.wireless.exception.BusinessException;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.util.NumericUtil;

public class Table implements Parcelable, Comparable<Table>, Jsonable{
	
	public final static byte TABLE_PARCELABLE_COMPLEX = 0;
	public final static byte TABLE_PARCELABLE_SIMPLE = 1;
	public final static byte TABLE_PARCELABLE_4_QUERY = 2;
	
	public static class Builder{
		private final int tableId;
		
		public Builder(int tableId){
			this.tableId = tableId;
		}
		
		public Table build(){
			return new Table(this);
		}
	}
	
	public static class AliasBuilder{
		private final int aliasId;
		
		public AliasBuilder(int aliasId){
			this.aliasId = aliasId;
		}
		
		public Table build(){
			return new Table(this);
		}
	}
	
	public static class TransferBuilder implements Parcelable{
		private Table srcTbl;
		private Table destTbl;
		
		private TransferBuilder(){
			
		}
		
		public TransferBuilder(Builder src, Builder dest){
			srcTbl = src.build();
			destTbl = dest.build();
		}
		
		public Table getSrcTbl(){
			return this.srcTbl;
		}
		
		public Table getDestTbl(){
			return this.destTbl;
		}

		@Override
		public void writeToParcel(Parcel dest, int flag) {
			dest.writeParcel(srcTbl, Table.TABLE_PARCELABLE_SIMPLE);
			dest.writeParcel(destTbl, Table.TABLE_PARCELABLE_SIMPLE);
		}

		@Override
		public void createFromParcel(Parcel source) {
			srcTbl = source.readParcel(Table.CREATOR);
			destTbl = source.readParcel(Table.CREATOR);
		}
		
		public final static Parcelable.Creator<TransferBuilder> CREATOR = new Parcelable.Creator<TransferBuilder>() {
			
			@Override
			public TransferBuilder[] newInstance(int size) {
				return new TransferBuilder[size];
			}
			
			@Override
			public TransferBuilder newInstance() {
				return new TransferBuilder();
			}
		};
		
		@Override
		public String toString(){
			return srcTbl.getName() + "->" + destTbl.getName();
		}

	}
	
	public static class BatchInsertBuilder{
		private final int start;
		private final int end;
		private final Region.RegionId regionId;
		
		private boolean skip4;
		private boolean skip7;
		
		public BatchInsertBuilder(int start, int end, Region.RegionId regionId){
			this.start = start;
			this.end = end;
			this.regionId = regionId;
		}
		
		public BatchInsertBuilder setSkip4(boolean onOff){
			this.skip4 = onOff;
			return this;
		}
		
		public BatchInsertBuilder setSkip7(boolean onOff){
			this.skip7 = onOff;
			return this;
		}
		
		public List<InsertBuilder> build(){
			List<InsertBuilder> result = new ArrayList<InsertBuilder>();
			for(int tableAlias = start; tableAlias <= end; tableAlias++){
				String alias = String.valueOf(tableAlias);
				int lastTblAlias = Integer.parseInt(alias.substring(alias.length() - 1));
				if(skip4 && lastTblAlias / 4 == 1 && lastTblAlias % 4 == 0){
					continue;
				}else if(skip7 && lastTblAlias / 7 == 1 && lastTblAlias % 7 == 0){
					continue;
				}else{
					result.add(new InsertBuilder(tableAlias, regionId));
				}
			}
			return result;
		}
	}
	
	public static class InsertBuilder4Feast{
		private final InsertBuilder builder;
		
		public InsertBuilder4Feast(){
			builder = new InsertBuilder(0, null);
			builder.category = Category.FEAST;
			builder.tableName = "酒席入账";
		}
		
		public Table build(){
			return new Table(this);
		}
	}
	
	public static class InsertBuilder4Fast{
		private final InsertBuilder builder;
		
		public InsertBuilder4Fast(int fastNo){
			if(fastNo < 0){
				throw new IllegalArgumentException("快餐号不能小于0");
			}
			builder = new InsertBuilder(0, null);
			builder.category = Category.FAST;
			builder.tableName = "快餐#" + fastNo;
		}
		
		public Table build(){
			return new Table(this);
		}
	}
	
	public static class InsertBuilder4Takeout{
		private final InsertBuilder builder;
		
		public InsertBuilder4Takeout(){
			builder = new InsertBuilder(0, null);
			builder.category = Category.TAKE_OUT;
		}
		
		public Table build(){
			return new Table(this);
		}
	}
	
	public static class InsertBuilder4Join{
		public static enum Suffix{
			A("A"), B("B"), C("C"), D("D"), E("E"), F("F"), G("G");
			
			private final String val;
			Suffix(String val){
				this.val = val;
			}
			
			public static Suffix valueOf(String val, int i){
				for(Suffix suffix : values()){
					if(suffix.val.equals(val)){
						return suffix;
					}
				}
				throw new IllegalArgumentException("the suffix(val = " + val + ") is invalid");
			}
			
			public String getVal(){
				return this.val;
			}
			
			@Override
			public String toString(){
				return val;
			}
		}
		private final Table parent;
		private final InsertBuilder builder;
		
		public InsertBuilder4Join(Table parent, Table.InsertBuilder4Join.Suffix suffix) throws BusinessException{
			if(parent.getCategory().isJoin()){
				throw new BusinessException("【" + parent.getName() + "】是已拆台状态，不能再进行拆台操作");
			}
			this.parent = parent;
			this.builder = new InsertBuilder(0, parent.getRegion().getId());
			this.builder.setTableName(parent.tableAlias + suffix.val + "(搭" + parent.getName() + ")");
			this.builder.category = Category.JOIN;
		}
		
		public InsertBuilder4Join(Table parent, String suffix) throws BusinessException{
			this(parent, Suffix.valueOf(suffix, 0));
		}
		
		public Table build(){
			return builder.build();
		}
		
		public Table parent(){
			return this.parent;
		}
	}
	
	/**
	 * The helper class to create the table object to perform insert {@link TableDao#insert)}
	 */
	public static class InsertBuilder{
		private final int tableAlias;
		private final Region.RegionId regionId;
		private String tableName;
		private int miniCost;
		private Category category = Category.NORMAL;
		
		InsertBuilder(int tableAlias, short regionId){
			this.tableAlias = tableAlias;
			this.regionId = Region.RegionId.valueOf(regionId);
		}

		public InsertBuilder(int tableAlias, Region.RegionId regionId){
			this.tableAlias = tableAlias;
			this.regionId = regionId;
		}

		
		public Table build(){
			return new Table(this);
		}
		
		public InsertBuilder setTableName(String tableName) {
			this.tableName = tableName;
			return this;
		}

		public InsertBuilder setMiniCost(int miniCost) {
			this.miniCost = miniCost;
			return this;
		}
	}
	
	/**
	 * The helper class to create the table object used in update {@link TableDao#}
	 */
	public static class UpdateBuilder{
		private final int tableId;
		
		private Region.RegionId regionId;
		private String tableName;
		private int miniCost = -1;
		
		public Table build(){
			return new Table(this);
		}
		
		public UpdateBuilder(int tableId){
			this.tableId = tableId;
		}

		public boolean isRegionChanged(){
			return this.regionId != null;
		}
		
		public UpdateBuilder setRegionId(Region.RegionId regionId) {
			this.regionId = regionId;
			return this;
		}

		public boolean isNameChanged(){
			return this.tableName != null;
		}
		
		public UpdateBuilder setTableName(String name) {
			this.tableName = name;
			return this;
		}

		public boolean isMiniCostChanged(){
			return this.miniCost >= 0;
		}
		
		public UpdateBuilder setMiniCost(int miniCost) {
			this.miniCost = miniCost;
			return this;
		}

	}
	
	/**
	 * 餐台类型
	 * 1 - 一般, 2 - 外卖， 3 - 拆台
	 */
	public static enum Category{
		NORMAL(1, "一般"),
		TAKE_OUT(2,	"外卖"),
		JOIN(3, "搭台"),
		FAST(4, "快餐"),
		FEAST(5, "酒席费");
		
		private final int val;
		private final String desc;
		
		Category(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return "category(val = " + val + ",desc = " + desc + ")";
		}
		
		public static Category valueOf(int val){
			for(Category category : values()){
				if(category.val == val){
					return category;
				}
			}
			throw new IllegalArgumentException("The category(val = " + val + ") is invalid.");
		}
		
		public int getVal(){
			return val;
		}
		
		public String getDesc(){
			return desc;
		}
		
		public boolean isNormal(){
			return this == NORMAL;
		}
		
		public boolean isTakeout(){
			return this == TAKE_OUT;
		}
		
		public boolean isJoin(){
			return this == JOIN;
		}
		
		public boolean isFast(){
			return this == FAST;
		}
		
		public boolean isFeast(){
			return this == FEAST;
		}
		
		public boolean isTemporary(){
			return this != NORMAL;
		}
	}
	
	/**
	 * 餐台状态
	 * 1 - 空闲, 2 - 就餐
	 */
	public static enum Status{
		IDLE(0, "空闲"),
		BUSY(1, "就餐");
		
		private final int val;
		private final String desc;
		
		Status(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return "status(" +
				   "val = " + val + 
				   ", desc = " + desc + ")";
		}
		
		public static Status valueOf(int val){
			for(Status status : values()){
				if(status.val == val){
					return status;
				}
			}
			throw new IllegalArgumentException("The table status(val = " + val + ") is invalid.");
		}
		
		public int getVal(){
			return val;
		}
		
		public String getDesc(){
			return desc;
		}
	}
	
	private int tableId;
	private int tableAlias;
	private int restaurantId;
	private String tableName;
	private float minimumCost;
	private int orderId;
	private int customNum;
	private Category category = Category.NORMAL;;
	private Status status = Status.IDLE;
	private Region region;
	private boolean tempPaidFlag = false;
	
	public Table(){
		
	}
	
	public Table(int id){
		this.tableId = id;
	}
	
	private Table(Builder builder){
		setId(builder.tableId);
	}
	
	private Table(AliasBuilder builder){
		setTableAlias(builder.aliasId);
	}
	
	private Table(InsertBuilder builder){
		setTableAlias(builder.tableAlias);
		setRegion(new Region(builder.regionId.getId(), null));
		setMinimumCost(builder.miniCost);
		setTableName(builder.tableName);
		setCategory(builder.category);
	}
	
	private Table(InsertBuilder4Takeout builder){
		setTableAlias(builder.builder.tableAlias);
		setMinimumCost(builder.builder.miniCost);
		setTableName(builder.builder.tableName);
		setCategory(builder.builder.category);
	}
	
	private Table(InsertBuilder4Fast builder){
		setTableAlias(builder.builder.tableAlias);
		setMinimumCost(builder.builder.miniCost);
		setTableName(builder.builder.tableName);
		setCategory(builder.builder.category);
	}
	
	private Table(InsertBuilder4Feast builder){
		setTableAlias(builder.builder.tableAlias);
		setTableName(builder.builder.tableName);
		setCategory(builder.builder.category);
	}
	
	private Table(UpdateBuilder builder){
		setMinimumCost(builder.miniCost);
		setRegion(new Region(builder.regionId.getId(), null));
		setId(builder.tableId);
		setTableName(builder.tableName);
	}
	
	public int getId() {
		return tableId;
	}
	
	public void setId(int tableId) {
		this.tableId = tableId;
	}
	
	public int getAliasId() {
		return tableAlias;
	}
	
	public void setTableAlias(int tableAlias) {
		this.tableAlias = tableAlias;
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public String getName() {
		if(tableName == null || tableName.trim().length() == 0){
			return tableAlias + "号台";
		}
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public float getMinimumCost() {
		return minimumCost;
	}
	
	public void setMinimumCost(float miniCost) {
		if(miniCost < 0 || miniCost > 65535){
			throw new IllegalArgumentException("The minimum cost(val = " + miniCost + ") exceed the range.");
		}
		this.minimumCost = miniCost;
	}
	
	public int getCustomNum() {
		return customNum;
	}
	
	public void setCustomNum(int customNum) {
		this.customNum = customNum;
	}
	
	public Category getCategory() {
		return category;
	}
	
	public void setCategory(Category category){
		this.category = category;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status){
		this.status = status;
	}
	
	public boolean isIdle(){
		return this.status == Status.IDLE;
	}
	
	public boolean isBusy(){
		return this.status == Status.BUSY;
	}
	
	public Region getRegion() {
		if(region == null){
			region = new Region(Region.RegionId.REGION_1.getId());
		}
		return region;
	}
	
	public void setRegion(Region region) {
		this.region = region;
	}

	public void setOrderId(int orderId){
		this.orderId = orderId;
	}
	
	public int getOrderId(){
		return this.orderId;
	}
	
	public boolean isTempPaid(){
		return this.tempPaidFlag;
	}
	
	public void setTempPaid(boolean onOff){
		this.tempPaidFlag = onOff;
	}
	
	@Override
	public String toString(){
		return "table(" +
			   "id = " + getId() + 
			   ", alias_id = " + getAliasId() +
			   ", restaurant_id = " + getRestaurantId() + ")";
	}
	
	@Override
	public int hashCode(){
		int result = 17;
		result = result * 31 + getAliasId();
		result = result * 31 + getRestaurantId();
		return result;
	}
	
	@Override 
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Table)){
			return false;
		}else{
			return getId() == ((Table)obj).getId();
		}
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == TABLE_PARCELABLE_SIMPLE){
			dest.writeInt(this.tableId);
			dest.writeShort(this.tableAlias);
			
		}else if(flag == TABLE_PARCELABLE_4_QUERY){
			dest.writeInt(this.tableId);
			dest.writeShort(this.tableAlias);
			dest.writeString(this.tableName);
			
		}else if(flag == TABLE_PARCELABLE_COMPLEX){
			dest.writeInt(this.tableId);
			dest.writeShort(this.tableAlias);
			dest.writeString(this.tableName);
			dest.writeParcel(this.region, Region.REGION_PARCELABLE_SIMPLE);
			dest.writeInt(NumericUtil.float2Int(this.minimumCost));
			dest.writeByte(this.status.getVal());
			dest.writeByte(this.category.getVal());
			dest.writeShort(this.customNum);
		}
	}

	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == TABLE_PARCELABLE_SIMPLE){
			this.tableId = source.readInt();
			this.tableAlias = source.readShort();
			
		}else if(flag == TABLE_PARCELABLE_4_QUERY){
			this.tableId = source.readInt();
			this.tableAlias = source.readShort();
			this.tableName = source.readString();
			
		}else if(flag == TABLE_PARCELABLE_COMPLEX){
			this.tableId = source.readInt();
			this.tableAlias = source.readShort();
			this.tableName = source.readString();
			this.region = source.readParcel(Region.CREATOR);
			this.minimumCost = NumericUtil.int2Float(source.readInt());
			this.status = Status.valueOf(source.readByte());
			this.category = Category.valueOf(source.readByte());
			this.customNum = source.readShort();
		}
	}
	
	public final static Parcelable.Creator<Table> CREATOR = new Parcelable.Creator<Table>() {
		
		@Override
		public Table[] newInstance(int size) {
			return new Table[size];
		}
		
		@Override
		public Table newInstance() {
			return new Table();
		}
	};

	@Override
	public int compareTo(Table another) {
		if(getAliasId() > another.getAliasId()){
			return 1;
		}else if(getAliasId() < another.getAliasId()){
			return -1;
		}else{
			return 0;
		}
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.tableId);
		jm.putInt("alias", this.tableAlias);
		jm.putInt("rid", this.restaurantId);
		jm.putString("name", this.getName());
		jm.putInt("customNum", this.customNum);
		jm.putFloat("minimumCost", this.minimumCost);
		jm.putInt("categoryValue", this.category.getVal());
		jm.putString("categoryText", this.category.getDesc());
		jm.putInt("statusValue", this.status.getVal());
		jm.putString("statusText", this.status.getDesc());
		jm.putJsonable("region", this.region, 0);
		jm.putBoolean("isTempPaid", this.isTempPaid());
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

}

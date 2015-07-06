package com.wireless.pojo.book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.exception.BusinessException;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;

public class Book implements Jsonable{

	public static class SeatBuilder{
		private final int id;
		private final List<Order.InsertBuilder> bookOrders = new ArrayList<Order.InsertBuilder>();
		
		public SeatBuilder(int id){
			this.id = id;
		}
		
		public SeatBuilder addOrder(Order.InsertBuilder builder){
			bookOrders.add(builder);
			return this;
		}
		
		public int getBookId(){
			return this.id;
		}
		
		public List<Order.InsertBuilder> getBookOrders(){
			return Collections.unmodifiableList(bookOrders);
		}
	}
	
	public static class ConfirmBuilder extends InsertBuilder4Manual{
		public ConfirmBuilder(int id){
			super.builder.setStatus(Status.CONFIRMED).setSource(null);
			super.setId(id);
		}
	}
	
	public static class InsertBuilder4Weixin{
		private final UpdateBuilder builder = new UpdateBuilder(0).setSource(Source.WEIXIN).setStatus(Status.CREATED);
		
		public InsertBuilder4Weixin setId(int id){
			builder.id = id;
			return this;
		}
		
		public InsertBuilder4Weixin setRegion(String region){
			builder.setRegion(region);
			return this;
		}
		
		public InsertBuilder4Weixin setBookDate(long date){
			if(date < System.currentTimeMillis()){
				throw new IllegalArgumentException("预订时间不能小于当前时间");
			}
			builder.setBookDate(date);
			return this;
		}
		
		public InsertBuilder4Weixin setBookDate(String date){
			builder.setBookDate(date);
			return this;
		}
		
		public InsertBuilder4Weixin setMember(Member member){
			builder.setMember(member);
			return this;
		}
		
		public InsertBuilder4Weixin setMember(String member){
			builder.setMember(member);
			return this;
		}
		
		public InsertBuilder4Weixin setTele(String tele){
			builder.setTele(tele);
			return this;
		}
		
		public InsertBuilder4Weixin setAmount(int amount){
			builder.setAmount(amount);
			return this;
		}
		
		public InsertBuilder4Weixin addOrderFood(OrderFood of, Staff staff) throws BusinessException{
			builder.addOrderFood(of, staff);
			return this;
		}
		
		public UpdateBuilder getBuilder(){
			return this.builder;
		}
		
		public Book build(){
			return builder.build();
		}
	}
	
	public static class InsertBuilder4Manual{
		private final UpdateBuilder builder = new UpdateBuilder(0).setSource(Source.MANUAL).setStatus(Status.CONFIRMED);
		
		public InsertBuilder4Manual setId(int id){
			builder.id = id;
			return this;
		}
		
		public InsertBuilder4Manual setBookDate(long date){
			if(date < System.currentTimeMillis()){
				throw new IllegalArgumentException("预订时间不能小于当前时间");
			}
			builder.setBookDate(date);
			return this;
		}
		
		public InsertBuilder4Manual setBookDate(String date){
			builder.setBookDate(date);
			return this;
		}
		
		public InsertBuilder4Manual setMember(Member member){
			builder.setMember(member);
			return this;
		}
		
		public InsertBuilder4Manual setMember(String member){
			builder.setMember(member);
			return this;
		}
		
		public InsertBuilder4Manual setTele(String tele){
			builder.setTele(tele);
			return this;
		}
		
		public InsertBuilder4Manual setAmount(int amount){
			builder.setAmount(amount);
			return this;
		}
		
		public InsertBuilder4Manual setStaff(Staff staff){
			builder.setStaff(staff);
			return this;
		}
		
		public InsertBuilder4Manual setCategory(String category){
			builder.setCategory(category);
			return this;
		}
		
		public InsertBuilder4Manual setReserved(int reserved){
			builder.setReserved(reserved);
			return this;
		}
		
		public InsertBuilder4Manual setComment(String comment){
			builder.setComment(comment);
			return this;
		}
		
		public InsertBuilder4Manual setMoney(float money){
			builder.setMoney(money);
			return this;
		}
		
		public InsertBuilder4Manual addTable(Table table){
			builder.addTable(table);
			return this;
		}
		
		public InsertBuilder4Manual addOrderFood(OrderFood of, Staff staff) throws BusinessException{
			builder.addOrderFood(of, staff);
			return this;
		}
		
		public UpdateBuilder getBuilder(){
			return this.builder;
		}
		
		public Book build(){
			return builder.build();
		}
	}
	
	public static class UpdateBuilder{
		private int id;
		private long bookDate;
		private int reserved;
		private String region;
		private String member;
		private int memberId;
		private String tele;
		private int amount;
		private Staff staff;
		private float money;
		private Source source;
		private Status status;
		private String category;
		private String comment;
		private final List<Table> tables = new ArrayList<Table>();
		private Order bookOrder;
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		UpdateBuilder setBookDate(String date){
			this.bookDate = DateUtil.parseDate(date);
			return this;
		}
		
		UpdateBuilder setBookDate(long date){
			this.bookDate = date;
			return this;
		}
		
		public boolean isBookDateChanged(){
			return this.bookDate != 0;
		}
		
		UpdateBuilder setReserved(int reserved){
			this.reserved = reserved;
			return this;
		}
		
		public boolean isReservedChanged(){
			return this.reserved != 0;
		}
		
		UpdateBuilder setRegion(String region){
			this.region = region;
			return this;
		}
		
		public boolean isRegionChanged(){
			return this.region != null;
		}
		
		UpdateBuilder setMember(Member member){
			this.member = member.getName();
			this.memberId = member.getId();
			this.tele = member.getTele();
			return this;
		}
		
		UpdateBuilder setMember(String member){
			this.member = member;
			return this;
		}
		
		public boolean isMemberChanged(){
			return this.member != null;
		}
		
		UpdateBuilder setTele(String tele){
			this.tele = tele;
			return this;
		}
		
		public boolean isTeleChanged(){
			return this.tele != null;
		}
		
		UpdateBuilder setAmount(int amount){
			this.amount = amount;
			return this;
		}
		
		public boolean isAmountChanged(){
			return this.amount != 0;
		}
		
		public boolean isMemberIdChanged(){
			return this.memberId != 0;
		}
		
		UpdateBuilder setStaff(Staff staff){
			this.staff = staff;
			return this;
		}
		
		public boolean isStaffChanged(){
			return this.staff != null;
		}
		
		UpdateBuilder setMoney(float money){
			this.money = money;
			return this;
		}
		
		public boolean isMoneyChanged(){
			return this.money != 0;
		}
		
		UpdateBuilder setCategory(String category){
			this.category = category;
			return this;
		}
		
		public boolean isCategoryChanged(){
			return this.category != null;
		}
		
		UpdateBuilder setSource(Source source){
			this.source = source;
			return this;
		}
		
		public boolean isSourceChanged(){
			return this.source != null;
		}
		
		UpdateBuilder setStatus(Status status){
			this.status = status;
			return this;
		}
		
		public boolean isStatusChanged(){
			return this.status != null;
		}
		
		UpdateBuilder addTable(Table table){
			this.tables.add(table);
			return this;
		}
		
		public boolean isTableChanged(){
			return !this.tables.isEmpty();
		}
		
		public UpdateBuilder addOrderFood(OrderFood of, Staff staff) throws BusinessException{
			if(this.bookOrder == null){
				this.bookOrder = new Order();
			}
			this.bookOrder.addFood(of, staff);
			return this;
		}
		
		public boolean isBookOrderChanged(){
			return this.bookOrder != null;
		}
		
		public UpdateBuilder setComment(String comment){
			this.comment = comment;
			return this;
		}
		
		public boolean isCommentChanged(){
			return this.comment != null;
		}
		
		public Book build(){
			return new Book(this);
		}
		
	}
	
	public static enum Source{
		WEIXIN(1, "微信"),
		MANUAL(2, "人工");
		
		private final int val;
		private final String desc;
		
		Source(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public int getVal(){
			return this.val;
		}
		
		public static Source valueOf(int val){
			for(Source source : values()){
				if(source.val == val){
					return source;
				}
			}
			throw new IllegalArgumentException("The source(val = " + val + ") is invalid.");
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	public static enum Status{
		CREATED(1, "待确认"),
		CONFIRMED(2, "已确认"),
		SEAT(3, "已入座");
		
		private final int val;
		private final String desc;
		
		Status(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public int getVal(){
			return this.val;
		}
		
		public static Status valueOf(int val){
			for(Status status : values()){
				if(status.val == val){
					return status;
				}
			}
			throw new IllegalArgumentException("The status(val = " + val + ") is invalid.");
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	public static enum Category{
		SINGLE("散台"),
		WEDDING("婚宴"),
		BIRTH("寿宴"),
		BABY("满月"),
		ANNUAL("年会"),
		FULL_YEAR("周岁"),
		START_SCHOOL("入学"),
		BIRTHDAY("生日");
		private final String desc;
		Category(String desc){
			this.desc = desc;
		}
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	private int id;
	private int restaurantId;
	private long bookDate;
	private int reserved;
	private String region;
	private String member;
	private int memberId;
	private String tele;
	private int amount;
	private long confirmDate;
	private Staff staff;
	private float money;
	private Source source;
	private Status status;
	private String category;
	private String comment;
	private final List<Table> tables = new ArrayList<Table>();
	private Order order;
	
	private Book(UpdateBuilder builder){
		this.id = builder.id;
		this.bookDate = builder.bookDate;
		this.reserved = builder.reserved;
		this.region = builder.region;
		this.member = builder.member;
		this.memberId = builder.memberId;
		this.tele = builder.tele;
		this.amount = builder.amount;
		this.staff = builder.staff;
		this.money = builder.money;
		this.source = builder.source;
		this.status = builder.status;
		this.category = builder.category;
		this.comment = builder.comment;
		setTables(builder.tables);
		setOrder(builder.bookOrder);
	}
	
	public Book(int id){
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public int getReserved(){
		return this.reserved;
	}
	
	public void setReserved(int reserved){
		this.reserved = reserved;
	}
	
	public long getBookDate() {
		return bookDate;
	}
	
	public void setBookDate(long bookDate) {
		this.bookDate = bookDate;
	}
	
	public String getRegion() {
		if(region == null){
			return "";
		}
		return region;
	}
	
	public void setRegion(String region) {
		this.region = region;
	}
	
	public String getMember() {
		if(member == null){
			return "";
		}
		return member;
	}
	
	public void setMember(String member) {
		this.member = member;
	}
	
	public int getMemberId() {
		return memberId;
	}
	
	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}
	
	public String getTele() {
		if(tele == null){
			return "";
		}
		return tele;
	}
	
	public void setTele(String tele) {
		this.tele = tele;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	public Staff getStaff() {
		return staff;
	}
	
	public void setStaff(Staff staff) {
		this.staff = staff;
	}
	
	public float getMoney() {
		return money;
	}
	
	public void setMoney(float money) {
		this.money = money;
	}
	
	public Source getSource() {
		return source;
	}
	
	public void setSource(Source source) {
		this.source = source;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public void setComment(String comment){
		this.comment = comment;
	}
	
	public String getComment(){
		if(this.comment == null){
			return "";
		}
		return this.comment;
	}
	
	public boolean isExpired(){
		return (System.currentTimeMillis() / 1000) > this.bookDate / 1000 + this.reserved;
	}
	
	public void setOrder(Order order){
		this.order = order;
	}
	
	public Order getOrder(){
		return this.order;
	}
	
	public boolean hasOrder(){
		return this.order != null;
	}
	
	public void setTables(List<Table> tables){
		if(tables != null){
			this.tables.clear();
			this.tables.addAll(tables);
		}
	}
	
	public List<Table> getTables(){
		return Collections.unmodifiableList(this.tables);
	}
	
	public void setConfirmDate(long confirmDate){
		this.confirmDate = confirmDate;
	}
	
	public long getConfirmDate(){
		return this.confirmDate;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putInt("restaurantId", this.restaurantId);
		jm.putString("bookDate", DateUtil.format(this.bookDate));
		jm.putInt("reserved", this.reserved / 60);
		jm.putString("region", this.region);
		jm.putString("member", this.member);
		jm.putInt("memberId", this.memberId);
		jm.putString("tele", this.tele);
		jm.putInt("amount", this.amount);
		jm.putString("staff", this.staff != null ? this.staff.getName() : "----");
		jm.putInt("staffId", this.staff != null ? this.staff.getId() : -1);
		jm.putFloat("money", this.money);
		jm.putInt("sourceValue", this.source.getVal());
		jm.putString("sourceDesc", this.source.toString());
		jm.putString("statusDesc", this.status.toString());
		jm.putInt("status", this.status.getVal());
		jm.putString("category", this.category);
		jm.putString("comment", this.comment);
		jm.putJsonableList("tables", this.tables, flag);
		jm.putBoolean("isExpired", this.isExpired());
		if(this.order != null){
			jm.putJsonable("order", this.order, flag);
		}
		return jm;
	}


	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		
	}
}

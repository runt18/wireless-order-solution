package com.wireless.pojo.book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.pojo.member.Member;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;

public class Book {

	public static class ConfirmBuilder{
		private final UpdateBuilder builder;
		
		public ConfirmBuilder(int id){
			builder = new UpdateBuilder(id).setStatus(Book.Status.CONFIRMED);
		}
		
		public ConfirmBuilder setStaff(Staff staff){
			builder.setStaff(staff);
			return this;
		}
		
		public ConfirmBuilder setCategory(String category){
			builder.setCategory(category);
			return this;
		}
		
		public ConfirmBuilder setReserved(int reserved){
			builder.setReserved(reserved);
			return this;
		}
		
		public ConfirmBuilder setComment(String comment){
			builder.setComment(comment);
			return this;
		}
		
		public ConfirmBuilder setMoney(float money){
			builder.setMoney(money);
			return this;
		}
		
		public ConfirmBuilder addTable(Table table){
			builder.addTable(table);
			return this;
		}
		
		public UpdateBuilder getBuilder(){
			return this.builder;
		}
		
		public Book build(){
			return builder.build();
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
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public UpdateBuilder setBookDate(String date){
			this.bookDate = DateUtil.parseDate(date);
			return this;
		}
		
		public UpdateBuilder setBookDate(long date){
			this.bookDate = date;
			return this;
		}
		
		public boolean isBookDateChanged(){
			return this.bookDate != 0;
		}
		
		public UpdateBuilder setReserved(int reserved){
			this.reserved = reserved;
			return this;
		}
		
		public boolean isReservedChanged(){
			return this.reserved != 0;
		}
		
		public UpdateBuilder setRegion(String region){
			this.region = region;
			return this;
		}
		
		public boolean isRegionChanged(){
			return this.region != null;
		}
		
		public UpdateBuilder setMember(Member member){
			this.member = member.getName();
			this.memberId = member.getId();
			this.tele = member.getTele();
			return this;
		}
		
		public UpdateBuilder setMember(String member){
			this.member = member;
			return this;
		}
		
		public boolean isMemberChanged(){
			return this.member != null;
		}
		
		public UpdateBuilder setTele(String tele){
			this.tele = tele;
			return this;
		}
		
		public boolean isTeleChanged(){
			return this.tele != null;
		}
		
		public UpdateBuilder setAmount(int amount){
			this.amount = amount;
			return this;
		}
		
		public boolean isAmountChanged(){
			return this.amount != 0;
		}
		
		public boolean isMemberIdChanged(){
			return this.memberId != 0;
		}
		
		public UpdateBuilder setStaff(Staff staff){
			this.staff = staff;
			return this;
		}
		
		public boolean isStaffChanged(){
			return this.staff != null;
		}
		
		public UpdateBuilder setMoney(float money){
			this.money = money;
			return this;
		}
		
		public boolean isMoneyChanged(){
			return this.money != 0;
		}
		
		public UpdateBuilder setCategory(String category){
			this.category = category;
			return this;
		}
		
		public boolean isCategoryChanged(){
			return this.category != null;
		}
		
		public UpdateBuilder setSource(Source source){
			this.source = source;
			return this;
		}
		
		public boolean isSourceChanged(){
			return this.source != null;
		}
		
		public UpdateBuilder setStatus(Status status){
			this.status = status;
			return this;
		}
		
		public boolean isStatusChanged(){
			return this.status != null;
		}
		
		public UpdateBuilder addTable(Table table){
			this.tables.add(table);
			return this;
		}
		
		public boolean isTableChanged(){
			return !this.tables.isEmpty();
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
		CREATED(1, "已创建"),
		CONFIRMED(2, "已确认"),
		FINISH(3, "已完成");
		
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
	private Staff staff;
	private float money;
	private Source source;
	private Status status;
	private String category;
	private String comment;
	private final List<Table> tables = new ArrayList<Table>();
	
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
		return (System.currentTimeMillis() / 1000) > this.bookDate + this.reserved;
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
}

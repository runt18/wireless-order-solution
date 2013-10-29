package com.wireless.pojo.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.util.DateUtil;

public class MemberOperation implements Jsonable {
	
	/**
	 * 操作类型
	 * 1-充值,  2-消费,  3-积分消费,  4-积分调整,  5-金额调整,	6-取款
	 * @author WuZY
	 */
	public static enum OperationType{
		CHARGE(		2,	1, 	"充值", 			"CZ"), 
		CONSUME(		1, 2, 	"消费", 			"XF"),
		POINT_CONSUME(3,	3, 	"积分消费",		"JFXF"),
		POINT_ADJUST(	3, 4, 	"积分调整", 		"JFTZ"),
		BALANCE_ADJUST(4,	5, 	"金额调整", 		"CZTZ"),
		TAKE_MONEY(	2, 6, 	"取款",  	    "QK");
		
		private final int type;
		private final int value;			//
		private final String name;			//
		private final String prefix; 	//流水号前缀
		
		OperationType(int type, int value, String name, String prefix){
			this.type = type;
			this.value = value;
			this.name = name;
			this.prefix = prefix;
		}
		
		@Override
		public String toString(){
			return this.getName();
		}
		
		public static OperationType valueOf(int val){
			for(OperationType ot : values()){
				if(ot.getValue() == val){
					return ot;
				}
			}
			
			throw new IllegalArgumentException("The operation value(val = " + val + ") passed is invalid.");
		}
		
		public static List<OperationType> typeOf(int type){
			List<OperationType> list = new ArrayList<OperationType>();
			for(OperationType ot : values()){
				if(ot.getType() == type){
					list.add(ot);
				}
			}
			return list;
			//throw new IllegalArgumentException("The operation value(val = " + type + ") passed is invalid.");
		}
		
		public int getValue() {
			return value;
		}
		
		public String getName() {
			return name;
		}
		
		public String getPrefix() {
			return prefix;
		}
		
		public int getType(){
			return type;
		}
	}
	
	/**
	 * 充值类型操作, 收款方式
	 * 1-现金  2-刷卡
	 * @author WuZY
	 */
	public static enum ChargeType{
		
		CASH( 			1, 	"现金", 	"XJ"),
		CREDIT_CARD( 	2, 	"刷卡", 	"SK");
		
		private final int value;
		private final String name;
		private final String prefix;
		
		ChargeType(int value, String name, String prefix){
			this.value = value;
			this.name = name;
			this.prefix = prefix;
		}
		
		@Override
		public String toString(){
			return this.name;
		}
		
		public static ChargeType valueOf(int val){
			for(ChargeType ct : values()){
				if(ct.getValue() == val){
					return ct;
				}
			}
			
			throw new IllegalArgumentException("The charge type(val = " + val + ") passed is invalid.");
		}
		
		public int getValue() {
			return value;
		}
		
		public String getName() {
			return name;
		}
		
		public String getPrefix() {
			return prefix;
		}
	}
	
	private int id;
	private int restaurantId;
	private long staffId;
	private String staffName;
	private Member member;
	private String seq;
	private long operateDate;
	private OperationType operateType;
	private float payMoney;
	private Order.PayType payType;
	private int orderId;
	private ChargeType chargeType;
	private float chargeMoney;
	private float takeMoney;
	private float deltaBaseMoney;
	private float deltaExtraMoney;
	private int deltaPoint;
	private float remainingBaseMoney;
	private float remainingExtraMoney;
	private int remainingPoint;
	private String comment;
	
	private MemberOperation(){
		
	}
	
	public static MemberOperation newMO(int memberId, String name, String mobile, String card){
		MemberOperation mo = new MemberOperation();
		Member member = new Member();
		member.setId(memberId);
		member.setName(name);
		member.setMobile(mobile);
		member.setMemberCard(card);
		mo.setMember(member);
		return mo;
	}
	
	public float getDeltaTotalMoney(){
		return this.deltaBaseMoney + this.deltaExtraMoney;
		
	}
	
	public float getRemainingTotalMoney(){
		return this.remainingExtraMoney + this.remainingBaseMoney;
	}
	
	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new LinkedHashMap<String, Object>();
		jm.put("id", this.id);
		jm.put("seq", this.seq);
		jm.put("rid", this.restaurantId);
		jm.put("staffId", this.staffId);
		jm.put("staffName", this.staffName);
		jm.put("orderId", orderId);
		jm.put("deltaBaseMoney", this.deltaBaseMoney);
		jm.put("deltaExtraMoney", this.deltaExtraMoney);
		jm.put("deltaPoint", deltaPoint);
		jm.put("remainingBaseMoney", this.remainingBaseMoney);
		jm.put("remainingExtraMoney", this.remainingExtraMoney);
		jm.put("remainingPoint", this.remainingPoint);
		jm.put("comment", this.comment);
		jm.put("deltaTotalMoney", this.getDeltaTotalMoney());
		jm.put("remainingTotalMoney", this.getRemainingTotalMoney());
		jm.put("operateDateFormat", DateUtil.format(this.operateDate));
		if(this.member != null){
			jm.put("member", this.member);			
		}
		if(this.operateType != null){
			jm.put("operateTypeText", this.operateType.getName());
			jm.put("operateTypeValue", this.operateType.getValue());
		}
		if(this.payType != null){
			jm.put("payTypeText", this.payType.getDesc());
			jm.put("payTypeValue", this.payType.getVal());
			jm.put("payMoney", this.payMoney);
		}
		if(this.chargeType != null){
			jm.put("chargeTypeText", this.chargeType.getName());
			jm.put("chargeTypeValue", this.chargeType.getValue());
			jm.put("chargeMoney", this.chargeMoney);
			jm.put("takeMoney", this.takeMoney);
		}
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
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
	public long getStaffID() {
		return staffId;
	}
	public void setStaffID(long staffId) {
		this.staffId = staffId;
	}
	public String getStaffName() {
		return staffName;
	}
	public void setStaffName(String staffName) {
		this.staffName = staffName;
	}
	
	public void setMember(Member member){
		if(member != null){
			this.member = member;
		}
	}
	
	public Member getMember(){
		return member;
	}
	
	public int getMemberId() {
		return this.member.getId();
	}
	
	public void setMemberId(int memberID) {
		this.member.setId(memberID);
	}
	
	public String getMemberCard(){
		return getMember().getMemberCard();
	}
	
	public void setMemberCard(String memberCard){
		getMember().setMemberCard(memberCard);
	}
	
	public String getMemberName(){
		return member.getName();
	}
	
	public void setMemberName(String name){
		getMember().setName(name);
	}
	
	public String getMemberMobile(){
		return member.getMobile();
	}
	
	public void setMemberMobile(String mobile){
		getMember().setMobile(mobile);
	}
	
	public String getOperateSeq() {
		return seq;
	}
	
	public void setOperateSeq(String seq) {
		this.seq = seq;
	}
	
	public String getOperateDateFormat() {
		return this.operateDate > 0 ? DateUtil.format(this.operateDate) : null;
	}
	public long getOperateDate() {
		return operateDate;
	}
	
	public void setOperateDate(long date) {
		this.operateDate = date;
	}
	public Integer getOperationTypeValue() {
		return this.operateType != null ? this.operateType.getValue() : null;
	}
	public String getOperationTypeText() {
		return this.operateType != null ? this.operateType.getName() : null;
	}
	public OperationType getOperationType() {
		return operateType;
	}
	
	public void setOperationType(int type) {
		this.operateType = OperationType.valueOf(type);
	}
	
	public void setOperationType(OperationType ot){
		this.operateType = ot;
	}
	
	public Integer getPayTypeValue(){
		return this.payType != null ? this.payType.getVal() : null;
	}
	
	public String getPayTypeText(){
		return this.payType != null ? this.payType.getDesc() : null;
	}
	
	public void setOrderId(int orderId){
		this.orderId = orderId;
	}
	
	public int getOrderId(){
		return this.orderId;
	}
	
	public Order.PayType getPayType(){
		return this.payType;
	}
	
	public void setPayType(Order.PayType type){
		this.payType = type;
	}
	
	public float getPayMoney() {
		return payMoney;
	}
	
	public void setPayMoney(float payMoney) {
		this.payMoney = payMoney;
	}
	
	public ChargeType getChargeType() {
		return chargeType;
	}
	
	public void setChargeType(ChargeType type){
		this.chargeType = type;
	}
	
	public void setChargeType(short chargeType) {
		this.chargeType = ChargeType.valueOf(chargeType);
	}
	
	public float getChargeMoney() {
		return chargeMoney;
	}
	public void setChargeMoney(float chargeMoney) {
		this.chargeMoney = chargeMoney;
	}
	public float getDeltaBaseMoney() {
		return deltaBaseMoney;
	}
	public void setDeltaBaseMoney(float deltaBaseMoney) {
		this.deltaBaseMoney = deltaBaseMoney;
	}
	
	public float getDeltaExtraMoney() {
		return deltaExtraMoney;
	}
	public void setDeltaExtraMoney(float deltaExtraMoney) {
		this.deltaExtraMoney = deltaExtraMoney;
	}
	public float getRemainingBaseMoney() {
		return remainingBaseMoney;
	}
	public void setRemainingBaseMoney(float remainingBaseMoney) {
		this.remainingBaseMoney = remainingBaseMoney;
	}
	public float getRemainingExtraMoney() {
		return remainingExtraMoney;
	}
	public void setRemainingExtraMoney(float remainingExtraMoney) {
		this.remainingExtraMoney = remainingExtraMoney;
	}
	public int getDeltaPoint() {
		return deltaPoint;
	}
	public void setDeltaPoint(int deltaPoint) {
		this.deltaPoint = deltaPoint;
	}
	
/*	public void setDeltaTotalMoney(float deltaTotalMoney){
	}*/
	public int getRemainingPoint() {
		return remainingPoint;
	}
	
	public float getTakeMoney() {
		return takeMoney;
	}

	public void setTakeMoney(float takeMoney) {
		this.takeMoney = takeMoney;
	}

	public void setRemainingPoint(int remainingPoint) {
		this.remainingPoint = remainingPoint;
	}
	public String getComment() {
		if(comment == null){
			comment = "";
		}
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	@Override
	public String toString(){
		return "member operation(" +
			   "id = " + getId() +
			   ",ot = " + getOperationType().getName() +
			   ",member_name = " + getMember().getName() +
			   ",mobile = " + getMemberMobile() +
			   ",member_card = " + getMemberCard() + ")";
	}
	
	@Override
	public int hashCode(){
		return 17 * 31 + getId();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof MemberOperation)){
			return false;
		}else{
			return getId() == ((MemberOperation)obj).getId();
		}
	}

}

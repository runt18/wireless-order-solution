package com.wireless.pojo.member;

import java.util.ArrayList;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.NumericUtil;

public class MemberOperation implements Jsonable {

	public static enum OperationCate{
		CONSUME_TYPE(1, "消费类型"),
		CHARGE_TYPE(2, "充值类型"),
		POINT_ADJUST_TYPE(3, "积分调整"),
		BALANCE_ADJUST_TYPE(4, "金额调整");
		
		OperationCate(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static OperationCate valueOf(int val) {
			for (OperationCate cate : values()) {
				if (cate.val == val) {
					return cate;
				}
			}
			throw new IllegalArgumentException("The operation category value(val = " + val + ") passed is invalid.");
		}
		
		private final int val;
		private final String desc;
		
		@Override
		public String toString(){
			return this.desc;
		}
	};
	
	/**
	 * 报表搜索大类(type) 1-消费反结账, 2-充值取款, 3-积分 
	 * 操作类型(value) 1-充值, 2-消费, 3-积分消费, 4-积分调整, 5-金额调整, 6-取款
	 * @author WuZY
	 */
	public static enum OperationType {

		CHARGE(OperationCate.CHARGE_TYPE, 1, "充值", "CZ"),
		CONSUME(OperationCate.CONSUME_TYPE, 2, "消费", "XF"), 
		POINT_CONSUME(OperationCate.POINT_ADJUST_TYPE, 3, "积分兑换", "JFXF"), 
		POINT_ADJUST(OperationCate.POINT_ADJUST_TYPE, 4, "积分调整", "JFTZ"),
		BALANCE_ADJUST(OperationCate.BALANCE_ADJUST_TYPE, 5, "金额调整", "CZTZ"), 
		REFUND(OperationCate.CHARGE_TYPE, 6, "取款", "QK"),
		RE_CONSUME(OperationCate.CONSUME_TYPE, 7, "反结账(消费)", "FJZ"),
		RE_CONSUME_RESTORE(OperationCate.CONSUME_TYPE, 8, "反结账(退款)", "FJZTK"),
		POINT_SUBSCRIBE(OperationCate.POINT_ADJUST_TYPE, 9, "关注赠送", "JF"),
		POINT_RECOMMEND(OperationCate.POINT_ADJUST_TYPE, 10, "推荐赠送", "TJ");

		private final OperationCate cate;
		private final int value; //
		private final String name; //
		private final String prefix; // 流水号前缀

		OperationType(OperationCate type, int value, String name, String prefix) {
			this.cate = type;
			this.value = value;
			this.name = name;
			this.prefix = prefix;
		}

		@Override
		public String toString() {
			return this.getName();
		}

		public static OperationType valueOf(int val) {
			for (OperationType ot : values()) {
				if (ot.getValue() == val) {
					return ot;
				}
			}
			throw new IllegalArgumentException("The operation value(val = " + val + ") passed is invalid.");
		}

		public static List<OperationType> typeOf(OperationCate cate) {
			final List<OperationType> result = new ArrayList<OperationType>();
			for (OperationType ot : values()) {
				if (ot.cate == cate) {
					result.add(ot);
				}
			}
			return result;
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

		public boolean isConsume(){
			return this.cate == OperationCate.CONSUME_TYPE;
		}
	}

	/**
	 * 充值类型操作, 收款方式 1-现金 2-刷卡
	 * 
	 * @author WuZY
	 */
	public static enum ChargeType {

		CASH(1, "现金", "XJ"),
		CREDIT_CARD(2, "刷卡", "SK"),
		SUBSCRIBE(3, "关注赠送", "GZ"),
		RECOMMEND(4, "推荐赠送", "TJ"),
		COMMISSION(5, "佣金赠送", "YJ");

		private final int value;
		private final String name;
		private final String prefix;

		ChargeType(int value, String name, String prefix) {
			this.value = value;
			this.name = name;
			this.prefix = prefix;
		}

		@Override
		public String toString() {
			return this.name;
		}

		public static ChargeType valueOf(int val) {
			for (ChargeType ct : values()) {
				if (ct.getValue() == val) {
					return ct;
				}
			}

			throw new IllegalArgumentException("The charge type(val = " + val
					+ ") passed is invalid.");
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
	private int branchId;
	private String branchName;
	private int staffId;
	private String staffName;
	private Member member;
	private String seq;
	private long operateDate;
	private OperationType operateType;
	private float payMoney;
	private PayType payType;
	private int orderId;
	private int couponId;
	private float couponMoney;
	private String couponName;
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

	private MemberOperation() {

	}

	public static MemberOperation newMO(int memberId, String name,
			String mobile, String card) {
		MemberOperation mo = new MemberOperation();
		Member member = new Member(memberId);
		member.setName(name);
		member.setMobile(mobile);
		member.setMemberCard(card);
		mo.setMember(member);
		return mo;
	}

	public float getDeltaTotalMoney() {
		return this.deltaBaseMoney + this.deltaExtraMoney;

	}

	public float getRemainingTotalMoney() {
		return this.remainingExtraMoney + this.remainingBaseMoney;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putString("seq", this.seq);
		jm.putInt("rid", this.restaurantId);
		jm.putInt("staffId", this.staffId);
		jm.putString("staffName", this.staffName);
		jm.putInt("orderId", this.orderId);
		jm.putInt("couponId", this.couponId);
		jm.putFloat("couponMoney", this.couponMoney);
		jm.putString("couponName", this.couponName);
		jm.putFloat("deltaBaseMoney", this.deltaBaseMoney);
		jm.putFloat("deltaExtraMoney", this.deltaExtraMoney);
		jm.putInt("deltaPoint", deltaPoint);
		jm.putFloat("remainingBaseMoney", this.remainingBaseMoney);
		jm.putFloat("remainingExtraMoney", this.remainingExtraMoney);
		jm.putInt("remainingPoint", this.remainingPoint);
		jm.putString("comment", this.comment);
		jm.putFloat("deltaTotalMoney", this.getDeltaTotalMoney());
		jm.putFloat("remainingTotalMoney", this.getRemainingTotalMoney());
		jm.putString("operateDateFormat", DateUtil.format(this.operateDate));
		jm.putJsonable("member", this.member, 0);
		if (this.operateType != null) {
			jm.putString("operateTypeText", this.operateType.getName());
			jm.putInt("operateTypeValue", this.operateType.getValue());
		}
		if (this.payType != null) {
			jm.putString("payTypeText", this.payType.getName());
			jm.putInt("payTypeValue", this.payType.getId());
			jm.putFloat("payMoney", this.payMoney);
		}
		if (this.chargeType != null) {
			jm.putString("chargeTypeText", this.chargeType.getName());
			jm.putInt("chargeTypeValue", this.chargeType.getValue());
			jm.putFloat("chargeMoney", this.chargeMoney);
			jm.putFloat("takeMoney", this.takeMoney);
			jm.putFloat("deltaCharge", this.deltaExtraMoney);
			if(this.deltaBaseMoney > 0){
				jm.putFloat("chargeRatio", NumericUtil.roundFloat(this.getDeltaTotalMoney() / this.deltaBaseMoney));
			}
		}

		jm.putInt("branchId", this.branchId);
		jm.putString("branchName", this.branchName);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {

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

	public void setBranchId(int branchId){
		this.branchId = branchId;
	}
	
	public int getBranchId(){
		return this.branchId;
	}
	
	public void setBranchName(String branchName){
		this.branchName = branchName;
	}
	
	public String getBranchName(){
		if(this.branchName == null){
			return "";
		}
		return this.branchName;
	}
	
	public int getStaffId() {
		return staffId;
	}

	public void setStaffId(int staffId) {
		this.staffId = staffId;
	}

	public String getStaffName() {
		if (staffName == null) {
			return "";
		}
		return staffName;
	}

	public void setStaffName(String staffName) {
		this.staffName = staffName;
	}

	public void setMember(Member member) {
		if (member != null) {
			this.member = member;
		}
	}

	public Member getMember() {
		return member;
	}

	public int getMemberId() {
		if (member != null) {
			return this.member.getId();
		} else {
			return 0;
		}
	}

	public void setMemberId(int memberId) {
		this.member.setId(memberId);
	}

	public String getMemberCard() {
		if (member != null) {
			return member.getMemberCard();
		} else {
			return "";
		}
	}

	public void setMemberCard(String memberCard) {
		getMember().setMemberCard(memberCard);
	}

	public String getMemberName() {
		if (member != null) {
			return member.getName();
		} else {
			return "";
		}
	}

	public void setMemberName(String name) {
		getMember().setName(name);
	}

	public String getMemberMobile() {
		if (member != null) {
			return member.getMobile();
		} else {
			return "";
		}
	}

	public void setMemberMobile(String mobile) {
		getMember().setMobile(mobile);
	}

	public String getOperateSeq() {
		return seq;
	}

	public void setOperateSeq(String seq) {
		this.seq = seq;
	}

	public long getOperateDate() {
		return operateDate;
	}

	public void setOperateDate(long date) {
		this.operateDate = date;
	}

	public OperationType getOperationType() {
		return operateType;
	}

	public void setOperationType(int type) {
		this.operateType = OperationType.valueOf(type);
	}

	public void setOperationType(OperationType ot) {
		this.operateType = ot;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getOrderId() {
		return this.orderId;
	}

	public PayType getPayType() {
		return this.payType;
	}

	public void setPayType(PayType type) {
		this.payType = type;
	}

	public float getPayMoney() {
		return payMoney;
	}

	public void setPayMoney(float payMoney) {
		this.payMoney = payMoney;
	}

	public void setCouponId(int couponId) {
		this.couponId = couponId;
	}

	public int getCouponId() {
		return this.couponId;
	}

	public String getCouponName() {
		if (couponName != null) {
			return this.couponName;
		} else {
			return "";
		}
	}

	public void setCoupnName(String name) {
		this.couponName = name;
	}

	public float getCouponMoney() {
		return this.couponMoney;
	}

	public void setCouponMoney(float couponMoney) {
		this.couponMoney = couponMoney;
	}

	public ChargeType getChargeType() {
		return chargeType;
	}

	public void setChargeType(ChargeType type) {
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
		if (comment == null) {
			return "";
		}
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "member operation(" + "id = " + getId() + ",ot = "
				+ getOperationType().getName() + ",member_name = "
				+ getMember().getName() + ",mobile = " + getMemberMobile()
				+ ",member_card = " + getMemberCard() + ")";
	}

	@Override
	public int hashCode() {
		return 17 * 31 + getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof MemberOperation)) {
			return false;
		} else {
			return getId() == ((MemberOperation) obj).getId();
		}
	}

}

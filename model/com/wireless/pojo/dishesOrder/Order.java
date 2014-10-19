package com.wireless.pojo.dishesOrder;import java.util.ArrayList;import java.util.Collections;import java.util.Comparator;import java.util.LinkedList;import java.util.List;import com.wireless.exception.BusinessException;import com.wireless.exception.StaffError;import com.wireless.json.JsonMap;import com.wireless.json.Jsonable;import com.wireless.parcel.Parcel;import com.wireless.parcel.Parcelable;import com.wireless.pojo.client.Member;import com.wireless.pojo.distMgr.Discount;import com.wireless.pojo.distMgr.DiscountPlan;import com.wireless.pojo.menuMgr.PricePlan;import com.wireless.pojo.regionMgr.Region;import com.wireless.pojo.regionMgr.Table;import com.wireless.pojo.serviceRate.ServicePlan;import com.wireless.pojo.staffMgr.Privilege;import com.wireless.pojo.staffMgr.Staff;import com.wireless.pojo.util.DateUtil;import com.wireless.pojo.util.NumericUtil;import com.wireless.pojo.util.SortedList;public class Order implements Parcelable, Jsonable{		public static class PayBuilder implements Parcelable{		private int orderId;				//账单编号		private int customNum = 1;			//就餐人数		private int erasePrice;				//抹数金额		private int discountId;				//折扣方案		private int pricePlanId;					//价格方案		private SettleType settleType;		//结账类型		private int memberId;				//会员信息		private float receivedCash;			//收款金额		private PayType payType;			//付款类型				private int servicePlanId;			//服务费方案Id		private String comment;				//备注		private int couponId;				//优惠券		private PrintOption printOption = PrintOption.DO_PRINT;		//是否打印		private boolean isTemp;				//是否暂结		private boolean sendSMS = false;	//是否发送短信通知				@Override		public void writeToParcel(Parcel dest, int flag) {			dest.writeInt(orderId);			dest.writeInt(customNum);			dest.writeInt(erasePrice);			dest.writeInt(discountId);			dest.writeByte(settleType.getVal());			dest.writeInt(memberId);			dest.writeFloat(receivedCash);			dest.writeByte(payType.getVal());			dest.writeInt(servicePlanId);			dest.writeString(comment);			dest.writeInt(couponId);			dest.writeByte(printOption.getVal());			dest.writeBoolean(isTemp);			dest.writeBoolean(sendSMS);			dest.writeInt(pricePlanId);		}		@Override		public void createFromParcel(Parcel source) {			orderId = source.readInt();			customNum = source.readInt();			erasePrice = source.readInt();			discountId = source.readInt();			settleType = SettleType.valueOf(source.readByte());			memberId = source.readInt();			receivedCash = source.readFloat();			payType = PayType.valueOf(source.readByte());			servicePlanId = source.readInt();			comment = source.readString();			couponId = source.readInt();			printOption = PrintOption.valueOf(source.readByte());			isTemp = source.readBoolean();			//FIXME to delete			if(memberId != 0){				sendSMS = source.readBoolean();				pricePlanId = source.readInt();			}		}				public final static Parcelable.Creator<PayBuilder> CREATOR = new Parcelable.Creator<PayBuilder>() {						@Override			public PayBuilder[] newInstance(int size) {				return new PayBuilder[size];			}						@Override			public PayBuilder newInstance() {				return new PayBuilder(0);			}		};				public static PayBuilder build(int orderId){			return new PayBuilder(orderId);		}				public static PayBuilder build(int orderId, PayType payType){			return new PayBuilder(orderId, payType);		}				public static PayBuilder build4Member(int orderId, Member member, PayType payType){			return new PayBuilder(orderId, member, payType, false);		}				public static PayBuilder build4Member(int orderId, Member member, PayType payType, boolean sendSms){			return new PayBuilder(orderId, member, payType, sendSms);		}				private PayBuilder(int orderId){			this.orderId = orderId;			this.settleType = SettleType.NORMAL;			this.payType = PayType.CASH;			this.memberId = 0;		}				private PayBuilder(int orderId, PayType payType){			this.orderId = orderId;			this.settleType = SettleType.NORMAL;			this.memberId = 0;			if(payType == PayType.MEMBER){				throw new IllegalArgumentException("普通结账不能使用'会员'付款方式");			}else{				this.payType = payType;			}		}				private PayBuilder(int orderId, Member member, PayType payType, boolean sendSMS){			this.orderId = orderId;			this.memberId = member.getId();			this.sendSMS = sendSMS;			if(member.getMemberType().isPoint()){				if(payType == PayType.CASH || payType == PayType.CREDIT_CARD){					this.settleType = SettleType.MEMBER;					this.payType = payType;				}else{					throw new IllegalArgumentException("积分会员只能使用'现金'或'刷卡'付款");				}			}else if(member.getMemberType().isCharge()){				this.settleType = SettleType.MEMBER;				this.payType = PayType.MEMBER;			}else{				throw new IllegalArgumentException();			}		}				public int getOrderId(){			return this.orderId;		}				public PayBuilder setCustomNum(int customNum){			if(customNum <= 0){				this.customNum = 1;			}else{				this.customNum = customNum;			}			return this;		}				public int getCustomNum(){			return this.customNum;		}				public PayBuilder setErasePrice(int erasePrice){			if(erasePrice < 0){				throw new IllegalArgumentException("加收服务费率的范围是'0.0 - 1.0'");			}else{				this.erasePrice = erasePrice;			}			return this;		}				public int getErasePrice(){			return this.erasePrice;		}				public PayBuilder setDiscountId(int discountId){			this.discountId = discountId;			return this;		}				public int getDiscountId(){			return this.discountId;		}				public boolean hasDiscount(){			return this.discountId != 0;		}				public void setPricePlanId(int pricePlanId){			this.pricePlanId = pricePlanId;		}				public int getPricePlanId(){			return this.pricePlanId;		}				public boolean hasPricePlan(){			return this.pricePlanId != 0;		}				public int getMemberId(){			return this.memberId;		}				public PayType getPaymentType(){			return this.payType;		}				public SettleType getSettleType(){			return this.settleType;		}				public PayBuilder setReceivedCash(float receviedCash){			if(receivedCash < 0){				throw new IllegalArgumentException();			}else{				this.receivedCash = receviedCash;			}			return this;		}				public float getReceivedCash(){			return this.receivedCash;		}				public PayBuilder setServicePlan(int servicePlanId){			this.servicePlanId = servicePlanId;			return this;		}				public int getServicePlanId(){			return this.servicePlanId;		}				public boolean hasServicePlan(){			return this.servicePlanId > 0;		}				public PayBuilder setComment(String comment){			this.comment = comment;			return this;		}				public String getComment(){			return this.comment;		}				public PayBuilder setCouponId(int couponId){			this.couponId = couponId;			return this;		}				public int getCouponId(){			return couponId;		}		public boolean hasCoupon(){			return this.couponId != 0;		}				public PayBuilder setPrintOption(PrintOption option){			this.printOption = option;			return this;		}				public PrintOption getPrintOption(){			return this.printOption;		}				public PayBuilder setTemp(boolean onOff){			this.isTemp = onOff;			return this;		}				public boolean isTemp(){			return isTemp;		}				public boolean isSendSMS(){			return sendSMS;		}	}		/**	 * 收款方式	 * 1-现金,  2-刷卡,  3-会员消费,  4-签单,  5-挂账	 */	public static enum PayType{				CASH(1, "现金"),					//现金		CREDIT_CARD(2, "刷卡"),			//刷卡		MEMBER(3, "会员余额"),			//会员		SIGN(4, "签单"),					//签单		HANG(5, "挂账");					//挂账				private final int value;				private final String desc;				public static PayType valueOf(int value){			for(PayType type : values()){				if(type.getVal() == value){					return type;				}			}			throw new IllegalArgumentException("The pay type(val = " + value + ") passed is invalid.");		}				PayType(int value, String desc){			this.value = value;			this.desc = desc;		}				public int getVal(){			return this.value;		}				public String getDesc(){			return this.desc;		}				@Override		public String toString(){			return "(type :" + value + ",desc : " + this.desc + ")";		}		}	/**	 * 结账类型	 * 1-会员,  2-普通	 */	public static enum SettleType{		NORMAL(1, "普通"), 		MEMBER(2, "会员");				private final int value;		private final String desc;				SettleType(int value, String text){			this.value = value;			this.desc = text;		}				public int getVal() {			return value;		}				public String getDesc() {			return desc;		}				public static SettleType valueOf(int value){			for(SettleType type : values()){				if(type.getVal() == value){					return type;				}			}			throw new IllegalArgumentException("The pay type(val = " + value + ") passed is invalid.");		}				@Override		public String toString(){			return "(type :" + value + ",text : " + this.desc + ")";		}	}	/**	 * 账单状态	 * 1-未结帐,  2-已结帐,  3-反结帐	 */	public static enum Status{		UNPAID(0, "未结帐"),		PAID(1, "已结帐"),		REPAID(2, "反结帐");				private final int val;		private final String desc;				Status(int val, String desc){			this.val = val;			this.desc = desc;		}				@Override		public String toString(){ 			return "status(val = " + val + ",desc = " + desc + ")";		}				public static Status valueOf(int val){			for(Status status : values()){				if(status.val == val){					return status;				}			}			throw new IllegalArgumentException("The status(val = " + val + ") is invalid.");		}				public int getVal(){			return val;		}				public String getDesc(){			return desc;		}	}		/**	 * 账单类型	 * 1-一般,  2-外卖,  3-拆台,  4-合单	 */	public static enum Category{		NORMAL(		1,	"一般"),		TAKE_OUT(	2,	"外卖"),		JOIN_TBL(	3, 	"拆台"),		MERGER_TBL(	4, 	"合单");		//MERGER_CHILD(5, "并台(子账单)");				private final int val;		private final String desc;				Category(int val, String desc){			this.val = val;			this.desc = desc;		}				@Override		public String toString(){			return "category(val = " + val + ",desc = " + desc + ")";		}				public static Category valueOf(int val){			for(Category category : values()){				if(category.val == val){					return category;				}			}			throw new IllegalArgumentException("The category(val = " + val + ") is invalid.");		}				public int getVal(){			return val;		}				public String getDesc(){			return desc;		}	}		//different kinds of order parcelable 	public final static byte ORDER_PARCELABLE_4_QUERY = 0;	public final static byte ORDER_PARCELABLE_4_COMMIT = 1;		//the id to this order	private int mId;			//the sequence id to this order	private int mSeqId;	//the restaurant id this order belong to	private int mRestaurantId;		//the waiter to this roder	private String mWaiter;	//the birth order date time	private long mBirthDate;	//the last modified order date time	private long mOrderDate;	//the category to this order	private Category mCategory = Category.NORMAL;		//the status to this order	private Status mStatus = Status.UNPAID;	//the settle type to this order	private SettleType mSettleType = SettleType.NORMAL;					//the pay type to this order	private PayType mPaymentType = PayType.CASH;						//the order foods	private List<OrderFood> mOrderFoods = new LinkedList<OrderFood>();	//the discount to this order	private Discount mDiscount;		//the destination table to this order	private Table mDestTbl;	//the custom number to this order	private int mCustomNum;				//the member operation id associated with this order	private int mMemberOperationId;	//the comment to this order	private String mComment;	//the erase price to this order	private int mErasePrice = 0;	//the repaid price to this order	private float mRepaidPrice = 0;	//the cash received from customer	private float mReceivedCash = 0;			//the service plan to this order	private ServicePlan mServicePlan;	//the price plan to this order	private PricePlan mPricePlan;	//the service rate to this order	private float mServiceRate = 0;	//the discount price to this order	private float mDiscountPrice = 0;	//the cancel price to this order	private float mCancelPrice = 0;	//the gift price to this order	private float mGiftPrice = 0;	//the total price to this order	private float mTotalPrice = 0;		//the coupon price to this order	private float mCouponPrice;	//如果客户没有设定尾数的处理方式，实收金额会等于合计金额,	//如果客户设定了尾数的处理方式，比如“抹零”、“四舍五入”等，实收金额就会根据不同处理方式变化	private float mActualPrice = 0;		public void setComment(String comment){		this.mComment = comment;	}		public String getComment(){		if(this.mComment == null){			return "";		}else{			return this.mComment;		}	}		public void setPaymentType(int payTypeVal){		this.mPaymentType = PayType.valueOf(payTypeVal);	}		public void setPaymentType(PayType payType){		this.mPaymentType = payType;	}		public PayType getPaymentType(){		return this.mPaymentType;	}		public boolean isPayByCash(){		return this.mPaymentType == PayType.CASH;	}		public boolean isPayByCreditCard(){		return this.mPaymentType == PayType.CREDIT_CARD;	}		public boolean isPayByMember(){		return this.mPaymentType == PayType.MEMBER;	}		public boolean isPayBySign(){		return this.mPaymentType == PayType.SIGN;	}		public boolean isPayByHang(){		return this.mPaymentType == PayType.HANG;	}		public void setSettleType(int settleTypeVal){		this.mSettleType = SettleType.valueOf(settleTypeVal);	}		public void setSettleType(SettleType settleType){		this.mSettleType = settleType;	}		public SettleType getSettleType(){		return this.mSettleType;	}		public boolean isSettledByNormal(){		return this.mSettleType == SettleType.NORMAL;	}		public boolean isSettledByMember(){		return this.mSettleType == SettleType.MEMBER;	}		public void setCategory(short categoryVal){		this.mCategory = Category.valueOf(categoryVal);	}		public void setCategory(Category category){		this.mCategory = category;	}		public Category getCategory(){		return mCategory;	}		public boolean isMerged(){		return mCategory == Category.MERGER_TBL;	}		public boolean isNormal(){		return mCategory == Category.NORMAL;	}		public boolean isJoined(){		return mCategory == Category.JOIN_TBL;	}		public boolean isTakeout(){		return mCategory == Category.TAKE_OUT;	}		public void setStatus(int statusVal){		this.mStatus = Status.valueOf(statusVal);	}		public void setStatus(Status status){		this.mStatus = status;	}		public Status getStatus(){		return mStatus;	}		public boolean isUnpaid(){		return mStatus == Status.UNPAID;	}		public boolean isPaid(){		return mStatus == Status.PAID;	}		public boolean isRepaid(){		return mStatus == Status.REPAID;	}		public void setOrderFoods(List<OrderFood> orderFoods){		if(orderFoods != null){			mOrderFoods.clear();			mOrderFoods.addAll(orderFoods);		}	}		public List<OrderFood> getOrderFoods(Comparator<OrderFood> comparator){		if(comparator != null){			List<OrderFood> orderFoods = SortedList.newInstance(mOrderFoods, comparator);			return orderFoods;		}else{			return Collections.unmodifiableList(mOrderFoods);		}	}		public List<OrderFood> getOrderFoods(){		return this.mOrderFoods;	}		public boolean hasOrderFood(){		return !mOrderFoods.isEmpty();	}		public int getMemberOperationId(){		return this.mMemberOperationId;	}		public void setMemberOperationId(int memberOperationId){		this.mMemberOperationId = memberOperationId;	}		public int getSeqId() {		return mSeqId;	}	public void setSeqId(int seqId) {		this.mSeqId = seqId;	}	public int getRestaurantId() {		return mRestaurantId;	}	public void setRestaurantId(int restaurantId) {		this.mRestaurantId = restaurantId;	}	public void setWaiter(String waiter){		this.mWaiter = waiter;	}		public String getWaiter(){		if(mWaiter == null){			return "";		}		return this.mWaiter;	}		public long getBirthDate() {		return mBirthDate;	}	public void setBirthDate(long birthDate) {		this.mBirthDate = birthDate;	}	public long getOrderDate() {		return mOrderDate;	}	public void setOrderDate(long orderDate) {		this.mOrderDate = orderDate;	}	public Region getRegion(){		if(this.mDestTbl == null){			this.mDestTbl= new Table(); 		}		return this.mDestTbl.getRegion();	}		public void setDestTbl(Table destTbl){		this.mDestTbl = destTbl;	}		public Table getDestTbl(){		if(this.mDestTbl == null){			this.mDestTbl= new Table(); 		}		return this.mDestTbl;	}		public void setCustomNum(int customNum){		this.mCustomNum = customNum;	}		public int getCustomNum(){		return mCustomNum;	}		public void setErasePrice(int erasePrice){		this.mErasePrice = erasePrice;	}		public int getErasePrice(){		return mErasePrice;	}		public void setServicePlan(ServicePlan servicePlan){		this.mServicePlan = servicePlan;	}		public ServicePlan getServicePlan(){		return this.mServicePlan;	}		public boolean hasServicePlan(){		return this.mServicePlan != null;	}		public void setPricePlan(PricePlan pricePlan){		this.mPricePlan = pricePlan;		for(OrderFood of : mOrderFoods){			of.setPricePlan(pricePlan);		}	}		public PricePlan getPricePlan(){		return this.mPricePlan;	}		public boolean hasPricePlan(){		return this.mPricePlan != null;	}		public float getServiceRate(){		return NumericUtil.roundFloat(mServiceRate);	}		public void setServiceRate(float rate){		if(rate < 0 || rate > 1){			throw new IllegalArgumentException("设置的服务费率不正确");		}		mServiceRate = rate;	}		public void setReceivedCash(float repaidPrice) {		mReceivedCash = repaidPrice;	}		public float getReceivedCash(){		return NumericUtil.roundFloat(mReceivedCash);	}	public void setRepaidPrice(float repaidPrice){		mRepaidPrice = repaidPrice;	}		public float getRepaidPrice(){		return NumericUtil.roundFloat(mRepaidPrice);	}		public void setDiscountPrice(float discountPrice){		mDiscountPrice = discountPrice;	}		public float getDiscountPrice(){		return NumericUtil.roundFloat(mDiscountPrice);	}		public void setCancelPrice(float cancelPrice){		mCancelPrice = cancelPrice;	}		public float getCancelPrice(){		return NumericUtil.roundFloat(mCancelPrice);	}		public void setGiftPrice(float giftPrice){		mGiftPrice = giftPrice;	}		public float getGiftPrice(){		return NumericUtil.roundFloat(mGiftPrice);	}		public void setTotalPrice(float totalPrice){		mTotalPrice = totalPrice;	}		public float getTotalPrice(){		return NumericUtil.roundFloat(mTotalPrice);	}		public void setCouponPrice(float couponPrice){		this.mCouponPrice = couponPrice;	}		public float getCouponPrice(){		return NumericUtil.roundFloat(mCouponPrice);	}		public void setActualPrice(float actualPrice){		this.mActualPrice = actualPrice;	}		public float getActualPrice(){		return NumericUtil.roundFloat(mActualPrice);	}		public Order(){			}		public Order(int id){		this.mId = id;	}		public Order(List<OrderFood> foods){		if(foods != null){			this.mOrderFoods.addAll(foods);		}	}		public Order(List<OrderFood> foods, int tableAlias, int customNum){		this(foods);		this.getDestTbl().setTableAlias(tableAlias);		this.mCustomNum = customNum;	}			public Order(int orderID, List<OrderFood> foods, int tableAlias, int customNum){		this(foods, tableAlias, customNum);		mId = orderID;	}		public Order(List<OrderFood> foods, short tableAlias, int customNum, float price){		this(foods, tableAlias, customNum);		setReceivedCash(price);	}		public Order(int orderID, List<OrderFood> foods, short tableAlias, int customNum, float price){		this(orderID, foods, tableAlias, customNum);		setReceivedCash(price);	}			public int getId(){		return mId;	}		public void setId(int id){		this.mId = id;	}		/**	 * Set the discount to this order.	 * The discount to each food is also be set. 	 * @param discount The discount to be set.	 */	public void setDiscount(Discount discount){		this.mDiscount = discount;		for(OrderFood of : mOrderFoods){			boolean isFound = false;			if(discount != null){				for(DiscountPlan dp : discount.getPlans()){					if(of.getKitchen().equals(dp.getKitchen())){						of.setDiscount(dp.getRate());						isFound = true;						break;					}				}			}			if(!isFound){				of.setDiscount(1);			}		}	}		/**	 * Get the discount to this order.	 * @return The discount to this order.	 */	public Discount getDiscount(){		if(this.mDiscount == null){			return Discount.EMPTY;		}		return this.mDiscount;	}			/**	 * Calculate the pure total price to this order as below.<br>	 * <pre>	 * for(each food){	 *    total += food.unitPrice * food.orderCount;	 * }	 * </pre>	 * @return the pure total price	 */	public float calcPureTotalPrice(){		float total = 0;		for(OrderFood of : mOrderFoods){			total += of.asFood().getPrice() * of.getCount();		}		return NumericUtil.roundFloat(total);	}		/**	 * Calculate the total price to this order as below.<br>	 * <pre>	 * for(each food){	 *    total += food.{@link OrderFood#calcPrice()}	 * }	 * total = total * (1 + serviceRate);	 * </pre>	 * @return the total price	 */	public float calcTotalPrice(){		float totalPrice = 0;		for(OrderFood of : mOrderFoods){			totalPrice += of.calcPrice();		}		return NumericUtil.roundFloat(totalPrice * (1 + getServiceRate()));	}		/**	 * Calculate the total price before discount to this order as below.<br>	 * <pre>	 * for(each food){	 *     total += food.{@link OrderFood#calcPriceBeforeDiscount()};	 * }	 * </pre>	 * @return The total price before discount to this order.	 */	public float calcPriceBeforeDiscount(){		float totalPrice = 0;		for(OrderFood of : mOrderFoods){			if(!of.isGift()){				totalPrice += of.calcPriceBeforeDiscount();			}		}		return NumericUtil.roundFloat(totalPrice);	}		/**	 * Calculate the total price of gifted foods.	 * <pre>	 * for(each food){	 *    total += food.{@link OrderFood#calcGiftPrice()}	 * }	 * @return the total price of gifted foods	 */	public float calcGiftPrice(){		float totalGifted = 0;		for(OrderFood of : mOrderFoods){			totalGifted += of.calcGiftPrice();		}		return NumericUtil.roundFloat(totalGifted);	}		/**	 * Calculate the total discount price.	 * <pre>	 * for(each food){	 *    total += food.{@link OrderFood#calcDiscountPrice()}	 * }	 * @return the total discount	 */	public float calcDiscountPrice(){		float totalDiscount = 0;		for(OrderFood of : mOrderFoods){			totalDiscount += of.calcDiscountPrice();		}		return NumericUtil.roundFloat(totalDiscount);	}		/**	 * Check to see whether the staff has the privilege to perform action on this food	 * @param of the food to check	 * @param staff the staff perform this action	 * @throws BusinessException	 * 			throws if the staff has no privilege to add food	 * 			throws if the staff has no privilege to present food	 */	private void checkPrivilege(OrderFood of, Staff staff) throws BusinessException{		if(!staff.getRole().hasPrivilege(Privilege.Code.ADD_FOOD)){			throw new BusinessException(StaffError.ORDER_NOT_ALLOW);					}else if(of.isGift() && !staff.getRole().hasPrivilege(Privilege.Code.GIFT)){			throw new BusinessException(StaffError.GIFT_NOT_ALLOW);		}	}		/**	 * Add the food to the list.	 * @param foodToAdd the food to add	 * @param staff the staff to add food	 * @throws BusinessException	 * 			throws if the order amount of the added food exceed MAX_ORDER_AMOUNT	 * 			throws if the staff has no privilege to add food	 * 			throws if the staff has no privilege to present food	 */	public void addFood(OrderFood foodToAdd, Staff staff) throws BusinessException{		checkPrivilege(foodToAdd, staff);				addFood(foodToAdd);	}		/**	 * Add the food to the list.	 * @param foodToAdd the food to add	 * @throws BusinessException	 * 			throws if the order amount of the added food exceed MAX_ORDER_AMOUNT	 */	private void addFood(OrderFood foodToAdd) throws BusinessException{		//Check to see whether the food to add is already contained. 		int index = mOrderFoods.indexOf(foodToAdd);				//如果新添加的菜品在原来菜品List中已经存在相同的菜品，则累加数量		//否则添加到菜品列表		if(index >= 0){			mOrderFoods.get(index).addCount(foodToAdd.getCount());		}else{			mOrderFoods.add(foodToAdd);		}	}		/**	 * Add the foods to list.	 * @param foodsToAdd the foods to add	 * @param the staff to add foods	 * @throws BusinessException 	 * 			throws if the staff has no privilege to add food	 * 			throws if the staff has no privilege to present food	 */	public void addFoods(List<OrderFood> foodsToAdd, Staff staff) throws BusinessException{		//Check to see whether the staff has privilege to present food.		for(OrderFood foodToAdd : foodsToAdd){			checkPrivilege(foodToAdd, staff);		}		addFoods(foodsToAdd);	}		/**	 * Add the foods to list.	 * @param foodsToAdd the foods to add	 */	private void addFoods(List<OrderFood> foodsToAdd){		for(OrderFood foodToAdd : foodsToAdd){			try{				addFood(foodToAdd);			}catch(BusinessException e){				mOrderFoods.get(mOrderFoods.indexOf(foodToAdd)).setCount(OrderFood.MAX_ORDER_AMOUNT);			}		}	}		/**	 * Delete the food from order without privilege check.	 * @param foodToDel the food to delete	 * @return true if the food to delete is found, otherwise false	 */	public boolean delete(OrderFood foodToDel){		if(foodToDel != null){			return mOrderFoods.remove(foodToDel);		}else{			return false;		}	}		/**	 * Remove the food.	 * @param foodToRemove the food to remove	 * @param staff the staff to remove the food	 * @return true if the food to remove is found, otherwise false.	 * @throws BusinessException 	 * 			throws if the staff does NOT own the cancel food privilege	 */	public boolean remove(OrderFood foodToRemove, Staff staff) throws BusinessException{		if(staff.getRole().hasPrivilege(Privilege.Code.CANCEL_FOOD)){			return mOrderFoods.remove(foodToRemove);		}else{			throw new BusinessException(StaffError.CANCEL_FOOD_NOT_ALLOW);		}	}	/**	 * Remove all the order foods from this order	 * @param staff the staff to remove foods	 * @throws BusinessException 	 * 			throws if the staff does NOT own the cancel food privilege	 */	public void removeAll(Staff staff) throws BusinessException{		if(staff.getRole().hasPrivilege(Privilege.Code.CANCEL_FOOD)){			mOrderFoods.clear();		}else{			throw new BusinessException(StaffError.CANCEL_FOOD_NOT_ALLOW);		}	}		/**	 * Replace with the food.	 * @param foodToReplace the food to replace	 * @return true if the food to replace exist before, otherwise false	 */	public boolean replace(OrderFood foodToReplace){		int index = mOrderFoods.indexOf(foodToReplace);		if(index >= 0){			mOrderFoods.set(index, foodToReplace);			return true;		}else{			return false;		}	}		/**	 * 	 */	public void trim(){		List<OrderFood> orderFoods = new ArrayList<OrderFood>(mOrderFoods);		mOrderFoods.clear();		addFoods(orderFoods);	}		public void copyFrom(Order src){		if(this != src && src != null){			this.setId(src.mId);			this.mActualPrice = src.mActualPrice;			this.mBirthDate = src.mBirthDate;			this.mCancelPrice = src.mCancelPrice;			this.mCategory = src.mCategory;			this.mCustomNum = src.mCustomNum;			this.mDestTbl = src.mDestTbl;			this.mDiscount = src.mDiscount;			this.mDiscountPrice = src.mDiscountPrice;			this.mErasePrice = src.mErasePrice;			this.mCouponPrice = src.mCouponPrice;			this.mGiftPrice = src.mGiftPrice;			this.mOrderDate = src.mOrderDate;			this.setOrderFoods(src.mOrderFoods);			this.mPaymentType = src.mPaymentType;			this.mSettleType = src.mSettleType;			this.mReceivedCash = src.mReceivedCash;			this.mRepaidPrice = src.mRepaidPrice;			this.mRestaurantId = src.mRestaurantId;			this.mWaiter = src.mWaiter;			this.mSeqId = src.mSeqId;			this.mServiceRate = src.mServiceRate;			this.mStatus = src.mStatus;			this.mTotalPrice = src.mTotalPrice;			this.mComment = src.mComment;		}	}		@Override	public int hashCode(){		return 17 + 31 * mId;	}		@Override	public boolean equals(Object obj){		if(obj == null || !(obj instanceof Order)){			return false;		}else{			return mId == ((Order)obj).mId;		}	}		@Override	public String toString(){		return "order(id = " + mId + ")";	}		@Override	public void writeToParcel(Parcel dest, int flag) {		dest.writeByte(flag);		if(flag == ORDER_PARCELABLE_4_QUERY){			dest.writeInt(this.mId);			dest.writeParcel(this.mDestTbl, Table.TABLE_PARCELABLE_SIMPLE);			dest.writeLong(this.mBirthDate);			dest.writeLong(this.mOrderDate);			dest.writeByte(this.mCategory.getVal());			dest.writeByte(this.mCustomNum);			dest.writeParcel(this.mDiscount, Discount.DISCOUNT_PARCELABLE_SIMPLE);			dest.writeParcelList(this.mOrderFoods, OrderFood.OF_PARCELABLE_4_QUERY);					}else if(flag == ORDER_PARCELABLE_4_COMMIT){			dest.writeInt(this.mId);			dest.writeParcel(this.mDestTbl, Table.TABLE_PARCELABLE_SIMPLE);			dest.writeLong(this.mOrderDate);			dest.writeByte(this.mCategory.getVal());			dest.writeByte(this.mCustomNum);			dest.writeParcelList(this.mOrderFoods, OrderFood.OF_PARCELABLE_4_COMMIT);					}	}	@Override	public void createFromParcel(Parcel source) {		short flag = source.readByte();		if(flag == ORDER_PARCELABLE_4_QUERY){			this.mId = source.readInt();			this.mDestTbl = source.readParcel(Table.CREATOR);			this.mBirthDate = source.readLong();			this.mOrderDate = source.readLong();			this.mCategory = Category.valueOf(source.readByte());			this.mCustomNum = source.readByte();			this.mDiscount = source.readParcel(Discount.CREATOR);			this.setOrderFoods(source.readParcelList(OrderFood.CREATOR));					}else if(flag == ORDER_PARCELABLE_4_COMMIT){			this.mId = source.readInt();			this.mDestTbl = source.readParcel(Table.CREATOR);			this.mOrderDate = source.readLong();			this.mCategory = Category.valueOf(source.readByte());			this.mCustomNum = source.readByte();			this.setOrderFoods(source.readParcelList(OrderFood.CREATOR));					}	}		public final static Parcelable.Creator<Order> CREATOR = new Parcelable.Creator<Order>() {				@Override		public Order[] newInstance(int size) {			return new Order[size];		}				@Override		public Order newInstance() {			return new Order();		}	};	public static enum Key4Json{		ORDER_ID("id", ""),		SEQ_ID("seqId", ""),		RESTAURANT_ID("rid", ""),		BIRTH_DATE("birthDate", ""),		BIRTH_DATE_TEXT("birthDateFormat", ""),		ORDER_DATE("orderDate", ""),		ORDER_DATE_TEXT("orderDateFormat", ""),		CATEGORY("categoryValue", ""),		CATEGORY_TEXT("categoryText", ""),		STATUS("statusValue", ""),		STATUS_TEXT("statusText", ""),		SETTLE_TYPE("settleTypeValue", ""),		SETTLE_TYPE_TEXT("settleTypeText", ""),		PAY_TYPE("payTypeValue", ""),		PAY_TYPE_TEXT("payTypeText", ""),		WAITER("waiter", ""),		DISCOUNT("discount", ""),		TABLE("table", ""),		TABLE_ALIAS("tableAlias", ""),		CUSTOM_NUM("customNum", ""),		COMMENT("comment", ""),		RECEIVED_CASH("receivedCash", ""),		ORDER_FOOD("orderFoods", "");						public final String key;		public final String desc;				Key4Json(String key, String desc){			this.key = key;			this.desc = desc;		}				@Override		public String toString(){			return "key = " + key + ",desc = " + desc;		}	}		@Override	public JsonMap toJsonMap(int flag) {		JsonMap jm = new JsonMap();		jm.putInt(Key4Json.ORDER_ID.key, this.mId);		jm.putInt(Key4Json.SEQ_ID.key, this.mSeqId);		jm.putInt(Key4Json.RESTAURANT_ID.key, this.mRestaurantId);		jm.putString(Key4Json.BIRTH_DATE.key, DateUtil.format(this.mBirthDate));		jm.putLong(Key4Json.ORDER_DATE.key, this.mOrderDate);		jm.putString(Key4Json.ORDER_DATE_TEXT.key, DateUtil.format(this.mOrderDate));		jm.putInt(Key4Json.CATEGORY.key, this.mCategory.getVal());		jm.putString(Key4Json.CATEGORY_TEXT.key, this.mCategory.getDesc());		jm.putInt(Key4Json.STATUS.key, this.mStatus.getVal());		jm.putString(Key4Json.STATUS_TEXT.key, this.mStatus.getDesc());		jm.putInt(Key4Json.SETTLE_TYPE.key, this.mSettleType.getVal());		jm.putString(Key4Json.SETTLE_TYPE_TEXT.key, this.mSettleType.getDesc());		jm.putInt(Key4Json.PAY_TYPE.key, this.mPaymentType.getVal());		jm.putString(Key4Json.PAY_TYPE_TEXT.key, this.mPaymentType.getDesc());		jm.putString(Key4Json.WAITER.key, this.mWaiter);				jm.putJsonable(Key4Json.DISCOUNT.key, this.mDiscount, 0);		jm.putJsonable(Key4Json.TABLE.key, this.mDestTbl, 0);		jm.putInt(Key4Json.TABLE_ALIAS.key, this.mDestTbl.getAliasId());		jm.putInt(Key4Json.CUSTOM_NUM.key, this.mCustomNum);				jm.putString(Key4Json.COMMENT.key, this.getComment().length() == 0 ? "----" : this.mComment);		jm.putFloat("repaidPrice", this.mRepaidPrice);		jm.putFloat(Key4Json.RECEIVED_CASH.key, this.mReceivedCash);		jm.putFloat("serviceRate", this.mServiceRate);		jm.putFloat("servicePlanId", hasServicePlan() ? getServicePlan().getPlanId() : 0);		jm.putFloat("discountPrice", this.mDiscountPrice);		jm.putFloat("cancelPrice", this.mCancelPrice);		jm.putFloat("giftPrice", this.mGiftPrice);		jm.putFloat("totalPrice", this.mTotalPrice);		jm.putInt("erasePrice", this.getErasePrice());		jm.putString("couponPrice", this.mCouponPrice > 0 ? Float.toString(this.mCouponPrice) : "----");		jm.putFloat("actualPrice", this.mActualPrice);		jm.putJsonableList(Key4Json.ORDER_FOOD.key, this.mOrderFoods, 0);				jm.putFloat("actualPriceBeforeDiscount", this.calcPriceBeforeDiscount());				return jm;	}	public final static int ORDER_JSONABLE_4_COMMIT = 1;	public final static int ORDER_JSONABLE_4_COMMIT_UPDATE = 7;		@Override	public void fromJsonMap(JsonMap jsonMap, int flag) {		if(flag == ORDER_JSONABLE_4_COMMIT){			//table alias...must			if(jsonMap.containsKey(Key4Json.TABLE_ALIAS.key)){				getDestTbl().setTableAlias(jsonMap.getInt(Key4Json.TABLE_ALIAS.key));			}else{				throw new IllegalStateException("提交账单缺少字段(" + Key4Json.TABLE_ALIAS + ")");			}					}else{			//order id for update...must			if(jsonMap.containsKey(Key4Json.ORDER_ID.key)){				setId(jsonMap.getInt(Key4Json.ORDER_ID.key));			}else{				throw new IllegalStateException("提交账单缺少字段(" + Key4Json.ORDER_ID + ")");			}			//table alias...must			if(jsonMap.containsKey(Key4Json.TABLE_ALIAS.key)){				getDestTbl().setTableAlias(jsonMap.getInt(Key4Json.TABLE_ALIAS.key));			}else{				throw new IllegalStateException("提交账单缺少字段(" + Key4Json.TABLE_ALIAS + ")");			}			//order date...must			if(jsonMap.containsKey(Key4Json.ORDER_DATE.key)){				setOrderDate(jsonMap.getLong(Key4Json.ORDER_DATE.key));			}else{				throw new IllegalStateException("提交账单缺少字段(" + Key4Json.ORDER_DATE + ")");			}		}		//category...optional		if(jsonMap.containsKey(Key4Json.CATEGORY.key)){			setCategory(Category.valueOf(jsonMap.getInt(Key4Json.CATEGORY.key)));		}else{			setCategory(Category.NORMAL);		}		//custom number...optional		setCustomNum(jsonMap.getInt(Key4Json.CUSTOM_NUM.key));		//order foods...optional		for(OrderFood of : jsonMap.getJsonableList(Key4Json.ORDER_FOOD.key, OrderFood.JSON_CREATOR, OrderFood.OF_JSONABLE_4_COMMIT)){			try {				this.addFood(of);			} catch (BusinessException ignored) {			}		}	}	public static Jsonable.Creator<Order> JSON_CREATOR = new Jsonable.Creator<Order>() {		@Override		public Order newInstance() {			return new Order(0);		}	};}
package com.wireless.pojo.dishesOrder;import java.util.ArrayList;import java.util.Collections;import java.util.HashMap;import java.util.LinkedHashMap;import java.util.LinkedList;import java.util.List;import java.util.Map;import com.wireless.exception.BusinessException;import com.wireless.exception.StaffError;import com.wireless.json.Jsonable;import com.wireless.parcel.Parcel;import com.wireless.parcel.Parcelable;import com.wireless.pojo.client.Member;import com.wireless.pojo.distMgr.Discount;import com.wireless.pojo.distMgr.DiscountPlan;import com.wireless.pojo.ppMgr.PricePlan;import com.wireless.pojo.regionMgr.Region;import com.wireless.pojo.regionMgr.Table;import com.wireless.pojo.staffMgr.Privilege;import com.wireless.pojo.staffMgr.Staff;import com.wireless.pojo.util.DateUtil;import com.wireless.pojo.util.NumericUtil;public class Order implements Parcelable, Jsonable{		/**	 * 收款方式	 * 1-现金,  2-刷卡,  3-会员消费,  4-签单,  5-挂账	 */	public static enum PayType{				CASH(1, "现金"),					//现金		CREDIT_CARD(2, "刷卡"),			//刷卡		MEMBER(3, "会员余额"),			//会员		SIGN(4, "签单"),					//签单		HANG(5, "挂账");					//挂账				private final int value;				private final String desc;				public static PayType valueOf(int value){			for(PayType type : values()){				if(type.getVal() == value){					return type;				}			}			throw new IllegalArgumentException("The pay type(val = " + value + ") passed is invalid.");		}				PayType(int value, String desc){			this.value = value;			this.desc = desc;		}				public int getVal(){			return this.value;		}				public String getDesc(){			return this.desc;		}				@Override		public String toString(){			return "(type :" + value + ",desc : " + this.desc + ")";		}		}	/**	 * 结账类型	 * 1-会员,  2-普通	 */	public static enum SettleType{		NORMAL(1, "普通"), 		MEMBER(2, "会员");				private final int value;		private final String desc;				SettleType(int value, String text){			this.value = value;			this.desc = text;		}				public int getVal() {			return value;		}				public String getDesc() {			return desc;		}				public static SettleType valueOf(int value){			for(SettleType type : values()){				if(type.getVal() == value){					return type;				}			}			throw new IllegalArgumentException("The pay type(val = " + value + ") passed is invalid.");		}				@Override		public String toString(){			return "(type :" + value + ",text : " + this.desc + ")";		}	}	/**	 * 账单状态	 * 1-未结帐,  2-已结帐,  3-反结帐	 */	public static enum Status{		UNPAID(0, "未结帐"),		PAID(1, "已结帐"),		REPAID(2, "反结帐");				private final int val;		private final String desc;				Status(int val, String desc){			this.val = val;			this.desc = desc;		}				@Override		public String toString(){ 			return "status(val = " + val + ",desc = " + desc + ")";		}				public static Status valueOf(int val){			for(Status status : values()){				if(status.val == val){					return status;				}			}			throw new IllegalArgumentException("The status(val = " + val + ") is invalid.");		}				public int getVal(){			return val;		}				public String getDesc(){			return desc;		}	}		/**	 * 账单类型	 * 1-一般,  2-外卖,  3-拆台,  4-并台,  5-并台(子账单)	 */	public static enum Category{		NORMAL(		1,	"一般"),		TAKE_OUT(	2,	"外卖"),		JOIN_TBL(	3, 	"拆台"),		MERGER_TBL(	4, 	"并台"),		MERGER_CHILD(5, "并台(子账单)");				private final int val;		private final String desc;				Category(int val, String desc){			this.val = val;			this.desc = desc;		}				@Override		public String toString(){			return "category(val = " + val + ",desc = " + desc + ")";		}				public static Category valueOf(int val){			for(Category category : values()){				if(category.val == val){					return category;				}			}			throw new IllegalArgumentException("The category(val = " + val + ") is invalid.");		}				public int getVal(){			return val;		}				public String getDesc(){			return desc;		}	}		//different kinds of order parcelable 	public final static byte ORDER_PARCELABLE_4_QUERY = 0;	public final static byte ORDER_PARCELABLE_4_COMMIT = 1;	public final static byte ORDER_PARCELABLE_4_PAY = 2;		//the id to this order	private int mId;			//the sequence id to this order	private int mSeqId;	//the restaurant id this order belong to	private int mRestaurantId;		//the birth order date time	private long mBirthDate;	//the last modified order date time	private long mOrderDate;	//the category to this order	private Category mCategory = Category.NORMAL;		//the status to this order	private Status mStatus = Status.UNPAID;	//the settle type to this order	private SettleType mSettleType = SettleType.NORMAL;					//the pay type to this order	private PayType mPaymentType = PayType.CASH;						//the order foods	private List<OrderFood> mOrderFoods = new LinkedList<OrderFood>();		//the discount to this order	private Discount mDiscount;		//the price plan to this order	private PricePlan mPricePlan;	//the sub orders	private List<Order> mChildOrders = new LinkedList<Order>();	//the destination table to this order	private Table mDestTbl;	//the custom number to this order	private int mCustomNum;				//the member associated with this order	private Member mMember;			//the member operation id associated with this order	private int mMemberOperationId;	//the comment to this order	private String mComment;		//the repaid price to this order	private float mRepaidPrice = 0;	//the cash received from customer	private float mReceivedCash = 0;					//the service rate to this order	private float mServiceRate = 0;	//the discount price to this order	private float mDiscountPrice = 0;	//the cancel price to this order	private float mCancelPrice = 0;	//the gift price to this order	private float mGiftPrice = 0;	//the total price to this order	private float mTotalPrice = 0;		//如果客户没有设定尾数的处理方式，实收金额会等于合计金额,	//如果客户设定了尾数的处理方式，比如“抹零”、“四舍五入”等，实收金额就会根据不同处理方式变化	private float mActualPrice = 0;		public void setComment(String comment){		this.mComment = comment;	}		public String getComment(){		if(this.mComment == null){			this.mComment = "";		}		return this.mComment;	}		public void setPaymentType(int payTypeVal){		this.mPaymentType = PayType.valueOf(payTypeVal);	}		public void setPaymentType(PayType payType){		this.mPaymentType = payType;	}		public PayType getPaymentType(){		return this.mPaymentType;	}		public boolean isPayByCash(){		return this.mPaymentType == PayType.CASH;	}		public boolean isPayByCreditCard(){		return this.mPaymentType == PayType.CREDIT_CARD;	}		public boolean isPayByMember(){		return this.mPaymentType == PayType.MEMBER;	}		public boolean isPayBySign(){		return this.mPaymentType == PayType.SIGN;	}		public boolean isPayByHang(){		return this.mPaymentType == PayType.HANG;	}		public void setSettleType(int settleTypeVal){		this.mSettleType = SettleType.valueOf(settleTypeVal);	}		public void setSettleType(SettleType settleType){		this.mSettleType = settleType;	}		public SettleType getSettleType(){		return this.mSettleType;	}		public boolean isSettledByNormal(){		return this.mSettleType == SettleType.NORMAL;	}		public boolean isSettledByMember(){		return this.mSettleType == SettleType.MEMBER;	}		public void setCategory(short categoryVal){		this.mCategory = Category.valueOf(categoryVal);	}		public void setCategory(Category category){		this.mCategory = category;	}		public Category getCategory(){		return mCategory;	}		public boolean isMerged(){		return mCategory == Category.MERGER_TBL;	}		public boolean isMergedChild(){		return mCategory == Category.MERGER_CHILD;	}		public boolean isNormal(){		return mCategory == Category.NORMAL;	}		public boolean isJoined(){		return mCategory == Category.JOIN_TBL;	}		public boolean isTakeout(){		return mCategory == Category.TAKE_OUT;	}		public void setStatus(int statusVal){		this.mStatus = Status.valueOf(statusVal);	}		public void setStatus(Status status){		this.mStatus = status;	}		public Status getStatus(){		return mStatus;	}		public boolean isUnpaid(){		return mStatus == Status.UNPAID;	}		public boolean isPaid(){		return mStatus == Status.PAID;	}		public boolean isRepaid(){		return mStatus == Status.REPAID;	}		public void setOrderFoods(List<OrderFood> orderFoods){		mOrderFoods.clear();		if(orderFoods != null){			mOrderFoods.addAll(orderFoods);		}	}		public List<OrderFood> getOrderFoods(){		return this.mOrderFoods;	}		public boolean hasOrderFood(){		return !mOrderFoods.isEmpty();	}		public Member getMember(){		return this.mMember;	}		public void setMember(Member member){		this.mMember = member;	}		public int getMemberOperationId(){		return this.mMemberOperationId;	}		public void setMemberOperationId(int memberOperationId){		this.mMemberOperationId = memberOperationId;	}		public int getSeqId() {		return mSeqId;	}	public void setSeqId(int seqId) {		this.mSeqId = seqId;	}	public int getRestaurantId() {		return mRestaurantId;	}	public void setRestaurantId(int restaurantId) {		this.mRestaurantId = restaurantId;	}	public long getBirthDate() {		return mBirthDate;	}	public void setBirthDate(long birthDate) {		this.mBirthDate = birthDate;	}	public long getOrderDate() {		return mOrderDate;	}	public void setOrderDate(long orderDate) {		this.mOrderDate = orderDate;	}	public Region getRegion(){		if(this.mDestTbl == null){			this.mDestTbl= new Table(); 		}		return this.mDestTbl.getRegion();	}		public void setDestTbl(Table destTbl){		this.mDestTbl = destTbl;	}		public Table getDestTbl(){		if(this.mDestTbl == null){			this.mDestTbl= new Table(); 		}		return this.mDestTbl;	}		public void setCustomNum(int customNum){		this.mCustomNum = customNum;	}		public int getCustomNum(){		return mCustomNum;	}		public List<Order> getChildOrder(){		return mChildOrders;	}		public void setChildOrder(List<Order> childOrders){		this.mChildOrders.clear();		if(childOrders != null){			this.mChildOrders.addAll(childOrders);		}	}		/**	 * Check if has the sub order.	 * @return true if has sub order, otherwise false	 */	public boolean hasChildOrder(){		return !mChildOrders.isEmpty();	}		/**	 * The erase price to this order	 */	int mErasePrice = 0;		public void setErasePrice(int erasePrice){		this.mErasePrice = erasePrice * 100;	}		public int getErasePrice(){		return mErasePrice / 100;	}		public float getServiceRate(){		return NumericUtil.roundFloat(mServiceRate);	}		public void setServiceRate(float rate){		if(rate < 0 || rate > 1){			throw new IllegalArgumentException("设置的服务费率不正确");		}		mServiceRate = rate;	}		public void setReceivedCash(float repaidPrice) {		mReceivedCash = repaidPrice;	}		public float getReceivedCash(){		return NumericUtil.roundFloat(mReceivedCash);	}	public void setRepaidPrice(float repaidPrice){		mRepaidPrice = repaidPrice;	}		public float getRepaidPrice(){		return NumericUtil.roundFloat(mRepaidPrice);	}		public void setDiscountPrice(float discountPrice){		mDiscountPrice = discountPrice;	}		public float getDiscountPrice(){		return NumericUtil.roundFloat(mDiscountPrice);	}		public void setCancelPrice(float cancelPrice){		mCancelPrice = cancelPrice;	}		public float getCancelPrice(){		return NumericUtil.roundFloat(mCancelPrice);	}		public void setGiftPrice(float giftPrice){		mGiftPrice = giftPrice;	}		public float getGiftPrice(){		return NumericUtil.roundFloat(mGiftPrice);	}		public void setTotalPrice(float totalPrice){		mTotalPrice = totalPrice;	}		public float getTotalPrice(){		return NumericUtil.roundFloat(mTotalPrice);	}		public void setActualPrice(float actualPrice){		this.mActualPrice = actualPrice;	}		public float getActualPrice(){		return NumericUtil.roundFloat(mActualPrice);	}		public Order(){			}		public Order(List<OrderFood> foods){		this.mOrderFoods.addAll(foods);	}		public Order(List<OrderFood> foods, int tableAlias, int customNum){		this(foods);		this.getDestTbl().setTableAlias(tableAlias);		this.mCustomNum = customNum;	}			public Order(int orderID, List<OrderFood> foods, int tableAlias, int customNum){		this(foods, tableAlias, customNum);		mId = orderID;	}		public Order(List<OrderFood> foods, short tableAlias, int customNum, float price){		this(foods, tableAlias, customNum);		setReceivedCash(price);	}		public Order(int orderID, List<OrderFood> foods, short tableAlias, int customNum, float price){		this(orderID, foods, tableAlias, customNum);		setReceivedCash(price);	}			public int getId(){		return mId;	}		public void setId(int id){		this.mId = id;	}		/**	 * Set the discount to this order.	 * The discount to each food is also be set. 	 * @param discount The discount to be set.	 */	public void setDiscount(Discount discount){		this.mDiscount = discount;		for(OrderFood of : mOrderFoods){			boolean isFound = false;			if(discount != null){				for(DiscountPlan dp : discount.getPlans()){					if(of.getKitchen().equals(dp.getKitchen())){						of.setDiscount(dp.getRate());						isFound = true;						break;					}				}			}			if(!isFound){				of.setDiscount(1);			}		}	}		/**	 * Get the discount to this order.	 * @return The discount to this order.	 */	public Discount getDiscount(){		if(this.mDiscount == null){			return new Discount.NotDiscountBuilder(getRestaurantId()).build();		}		return this.mDiscount;	}				public void setPricePlan(PricePlan pricePlan){		mPricePlan = pricePlan;	}		public PricePlan getPricePlan(){		return mPricePlan;	}	public boolean hasPricePlan(){		return mPricePlan == null ? false : mPricePlan.isValid();	}		/**	 * Calculate the pure total price to this order as below.<br>	 * <pre>	 * for(each food){	 *    total += food.unitPrice * food.orderCount;	 * }	 * </pre>	 * @return the pure total price	 */	public float calcPureTotalPrice(){		float total = 0;		for(OrderFood of : mOrderFoods){			total += of.getPrice() * of.getCount();		}		return NumericUtil.roundFloat(total);	}		/**	 * Calculate the total price to this order as below.<br>	 * <pre>	 * for(each food){	 *    if(!food.isGift()){	 *        total += (food.unitPrice + food.tastePrice) * discount * food.orderCount;	 *    }	 * }	 * total = total * (1 + serviceRate);	 * </pre>	 * @return the total price	 */	public float calcTotalPrice(){		float totalPrice = 0;		for(OrderFood of : mOrderFoods){			if(!of.asFood().isGift()){				totalPrice += of.calcPriceWithTaste();			}		}		return NumericUtil.roundFloat(totalPrice * (1 + getServiceRate()));	}		/**	 * Calculate the total price before discount to this order as below.<br>	 * <pre>	 * for(each food){	 *    if(!food.isGift()){	 *        total += (food.unitPrice + food.tastePrice) * food.orderCount;	 *    }	 * }	 * </pre>	 * @return The total price before discount to this order.	 */	public float calcPriceBeforeDiscount(){		float totalPrice = 0;		for(OrderFood of : mOrderFoods){			if(!of.asFood().isGift()){				totalPrice += of.calcPriceBeforeDiscount();			}		}		return NumericUtil.roundFloat(totalPrice);	}		/**	 * Calculate the total price of gifted foods.	 * @return the total price of gifted foods	 */	public float calcGiftPrice(){		float totalGifted = 0;		for(OrderFood of : mOrderFoods){			if(of.asFood().isGift()){				totalGifted += of.calcPriceWithTaste();			}		}		return NumericUtil.roundFloat(totalGifted);	}		/**	 * Calculate the total discount price.	 * @return the total discount	 */	public float calcDiscountPrice(){		float totalDiscount = 0;		for(OrderFood of : mOrderFoods){			totalDiscount += of.calcDiscountPrice();		}		return NumericUtil.roundFloat(totalDiscount);	}		/**	 * Add the food to the list.	 * @param foodToAdd The food to add	 * @throws BusinessException	 * 			Throws if the order amount of the added food exceed MAX_ORDER_AMOUNT	 */	public void addFood(OrderFood foodToAdd) throws BusinessException{		//Check to see whether the food to add is already contained. 		int index = mOrderFoods.indexOf(foodToAdd);				//如果新添加的菜品在原来菜品List中已经存在相同的菜品，则累加数量		//否则添加到菜品列表		if(index >= 0){			mOrderFoods.get(index).addCount(foodToAdd.getCount());		}else{			mOrderFoods.add(foodToAdd);		}	}		/**	 * Add the foods to list.	 * @param foodsToAdd The foods to add	 */	public void addFoods(List<OrderFood> foodsToAdd){		for(OrderFood foodToAdd : foodsToAdd){			try{				addFood(foodToAdd);			}catch(BusinessException e){				mOrderFoods.get(mOrderFoods.indexOf(foodToAdd)).setCount(OrderFood.MAX_ORDER_AMOUNT);			}		}	}		/**	 * Remove the food.	 * @param foodToRemove the food to remove	 * @param staff the staff to remove the food	 * @return true if the food to remove is found, otherwise false.	 * @throws BusinessException 	 * 			throws if the staff does NOT own the cancel food privilege	 */	public boolean remove(OrderFood foodToRemove, Staff staff) throws BusinessException{		if(staff.getRole().hasPrivilege(Privilege.Code.CANCEL_FOOD)){			return mOrderFoods.remove(foodToRemove);		}else{			throw new BusinessException(StaffError.PERMISSION_NOT_ALLOW);		}	}	/**	 * Remove all the order foods from this order	 * @param staff the staff to remove foods	 * @throws BusinessException 	 * 			throws if the staff does NOT own the cancel food privilege	 */	public void removeAll(Staff staff) throws BusinessException{		if(staff.getRole().hasPrivilege(Privilege.Code.CANCEL_FOOD)){			mOrderFoods.clear();		}else{			throw new BusinessException(StaffError.PERMISSION_NOT_ALLOW);		}	}		/**	 * Replace with the food.	 * @param foodToReplace the food to replace	 * @return true if the food to replace exist before, otherwise false	 */	public boolean replace(OrderFood foodToReplace){		int index = mOrderFoods.indexOf(foodToReplace);		if(index >= 0){			mOrderFoods.set(index, foodToReplace);			return true;		}else{			return false;		}	}		/**	 * 	 */	public void trim(){		List<OrderFood> orderFoods = new ArrayList<OrderFood>(mOrderFoods);		mOrderFoods.clear();		addFoods(orderFoods);	}		public void copyFrom(Order src){		if(this != src && src != null){			this.setId(src.mId);			this.mActualPrice = src.mActualPrice;			this.mBirthDate = src.mBirthDate;			this.mCancelPrice = src.mCancelPrice;			this.mCategory = src.mCategory;			this.setChildOrder(src.mChildOrders);			this.mCustomNum = src.mCustomNum;			this.mDestTbl = src.mDestTbl;			this.mDiscount = src.mDiscount;			this.mDiscountPrice = src.mDiscountPrice;			this.mErasePrice = src.mErasePrice;			this.mGiftPrice = src.mGiftPrice;			this.mOrderDate = src.mOrderDate;			this.setOrderFoods(src.mOrderFoods);			this.mPaymentType = src.mPaymentType;			this.mSettleType = src.mSettleType;			this.mPricePlan = src.mPricePlan;			this.mReceivedCash = src.mReceivedCash;			this.mRepaidPrice = src.mRepaidPrice;			this.mRestaurantId = src.mRestaurantId;			this.mSeqId = src.mSeqId;			this.mServiceRate = src.mServiceRate;			this.mStatus = src.mStatus;			this.mTotalPrice = src.mTotalPrice;			this.mMember = src.mMember;			this.mComment = src.mComment;		}	}		@Override	public int hashCode(){		return 17 + 31 * mId;	}		@Override	public boolean equals(Object obj){		if(obj == null || !(obj instanceof Order)){			return false;		}else{			return mId == ((Order)obj).mId;		}	}		@Override	public String toString(){		return "order(id = " + mId + ")";	}		@Override	public void writeToParcel(Parcel dest, int flag) {		dest.writeByte(flag);		if(flag == ORDER_PARCELABLE_4_QUERY){			dest.writeInt(this.mId);			dest.writeParcel(this.mDestTbl, Table.TABLE_PARCELABLE_SIMPLE);			dest.writeLong(this.mBirthDate);			dest.writeLong(this.mOrderDate);			dest.writeByte(this.mCategory.getVal());			dest.writeByte(this.mCustomNum);			dest.writeParcelList(this.mOrderFoods, OrderFood.OF_PARCELABLE_4_QUERY);					}else if(flag == ORDER_PARCELABLE_4_COMMIT){			dest.writeInt(this.mId);			dest.writeParcel(this.mDestTbl, Table.TABLE_PARCELABLE_SIMPLE);			dest.writeLong(this.mOrderDate);			dest.writeByte(this.mCategory.getVal());			dest.writeByte(this.mCustomNum);			dest.writeParcelList(this.mOrderFoods, OrderFood.OF_PARCELABLE_4_COMMIT);					}else if(flag == ORDER_PARCELABLE_4_PAY){			dest.writeInt(this.mId);			dest.writeParcel(this.mMember, Member.MEMBER_PARCELABLE_SIMPLE);			dest.writeParcel(this.mDestTbl, Table.TABLE_PARCELABLE_SIMPLE);			dest.writeShort(this.mCustomNum);			dest.writeFloat(this.mReceivedCash);			dest.writeByte(this.mSettleType.getVal());			dest.writeParcel(this.mDiscount, Discount.DISCOUNT_PARCELABLE_SIMPLE);			dest.writeParcel(this.mPricePlan, PricePlan.PP_PARCELABLE_SIMPLE);			dest.writeInt(this.mErasePrice);			dest.writeByte(this.mPaymentType.getVal());			dest.writeFloat(this.mServiceRate);			dest.writeString(this.mComment);		}	}	@Override	public void createFromParcel(Parcel source) {		short flag = source.readByte();		if(flag == ORDER_PARCELABLE_4_QUERY){			this.mId = source.readInt();			this.mDestTbl = source.readParcel(Table.CREATOR);			this.mBirthDate = source.readLong();			this.mOrderDate = source.readLong();			this.mCategory = Category.valueOf(source.readByte());			this.mCustomNum = source.readByte();			this.setOrderFoods(source.readParcelList(OrderFood.OF_CREATOR));					}else if(flag == ORDER_PARCELABLE_4_COMMIT){			this.mId = source.readInt();			this.mDestTbl = source.readParcel(Table.CREATOR);			this.mOrderDate = source.readLong();			this.mCategory = Category.valueOf(source.readByte());			this.mCustomNum = source.readByte();			this.setOrderFoods(source.readParcelList(OrderFood.OF_CREATOR));					}else if(flag == ORDER_PARCELABLE_4_PAY){			this.mId = source.readInt();			this.mMember = source.readParcel(Member.MEMBER_CREATOR);			this.mDestTbl = source.readParcel(Table.CREATOR);			this.mCustomNum = source.readShort();			this.mReceivedCash = source.readFloat();			this.mSettleType = SettleType.valueOf(source.readByte());			this.mDiscount = source.readParcel(Discount.DISCOUNT_CREATOR);			this.mPricePlan = (PricePlan)source.readParcel(PricePlan.PP_CREATOR);			this.mErasePrice = source.readInt();			this.mPaymentType = PayType.valueOf(source.readByte());			this.mServiceRate = source.readFloat();			this.mComment = source.readString();		}	}		public final static Parcelable.Creator<Order> CREATOR = new Parcelable.Creator<Order>() {				public Order[] newInstance(int size) {			return new Order[size];		}				public Order newInstance() {			return new Order();		}	};	@Override	public Map<String, Object> toJsonMap(int flag) {		HashMap<String, Object> jm = new LinkedHashMap<String, Object>();		jm.put("id", this.mId);		jm.put("seqId", this.mSeqId);		jm.put("rid", this.mRestaurantId);		jm.put("birthDateFormat", DateUtil.format(this.mBirthDate));		jm.put("orderDate", this.mOrderDate);		jm.put("orderDateFormat", DateUtil.format(this.mOrderDate));		jm.put("categoryValue", this.mCategory.getVal());		jm.put("categoryText", this.mCategory.getDesc());		jm.put("statusValue", this.mStatus.getVal());		jm.put("statusText", this.mStatus.getDesc());		jm.put("settleTypeValue", this.mSettleType.getVal());		jm.put("settleTypeText", this.mSettleType.getDesc());		jm.put("payTypeValue", this.mPaymentType.getVal());		jm.put("payTypeText", this.mPaymentType.getDesc());				jm.put("discount", this.mDiscount);		jm.put("pricePlan", this.mPricePlan);		jm.put("table", this.mDestTbl);		jm.put("member", this.mMember);		jm.put("customNum", this.mCustomNum);				jm.put("comment", this.mComment);		jm.put("repaidPrice", this.mRepaidPrice);		jm.put("receivedCash", this.mReceivedCash);		jm.put("serviceRate", this.mServiceRate);		jm.put("discountPrice", this.mDiscountPrice);		jm.put("cancelPrice", this.mCancelPrice);		jm.put("giftPrice", this.mGiftPrice);		jm.put("totalPrice", this.mTotalPrice);		jm.put("erasePrice", this.getErasePrice());		jm.put("actualPrice", this.mActualPrice);		jm.put("orderFoods", this.mOrderFoods);		jm.put("childOrders", this.mChildOrders);				jm.put("actualPriceBeforeDiscount", this.calcPriceBeforeDiscount());				return Collections.unmodifiableMap(jm);	}	@Override	public List<Object> toJsonList(int flag) {		return null;	}}
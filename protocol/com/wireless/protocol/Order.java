package com.wireless.protocol;import com.wireless.excep.ProtocolException;import com.wireless.protocol.parcel.Parcel;import com.wireless.protocol.parcel.Parcelable;import com.wireless.util.NumericUtil;public class Order implements Parcelable{		public final static byte ORDER_PARCELABLE_4_QUERY = 0;	public final static byte ORDER_PARCELABLE_4_COMMIT = 1;	public final static byte ORDER_PARCELABLE_4_PAY = 2;		// All kinds of status	public final static int STATUS_UNPAID = 0;	//未结帐	public final static int STATUS_PAID = 1;	//已结帐	public final static int STATUS_REPAID = 2;	//反结帐	int mStatus = STATUS_UNPAID;		// All kinds of settle type	public final static int SETTLE_BY_NORMAL = 1;		//普通方式结帐	public final static int SETTLE_BY_MEMBER = 2;		//会员方式结帐	int mSettleType = SETTLE_BY_NORMAL;					// All kinds of the payment type	public final static int PAYMENT_CASH = 1;			//现金方式付款	public final static int PAYMENT_CREDIT_CARD = 2;	//刷卡方式付款	public final static int PAYMENT_MEMBER = 3;			//会员方式付款	public final static int PAYMENT_SIGN = 4;			//签单方式付款	public final static int PAYMENT_HANG = 5;			//挂账方式付款	int mPaymentType = PAYMENT_CASH;						// All kinds of category	public final static short CATE_NORMAL = 1;			//一般	public final static short CATE_TAKE_OUT = 2;		//外卖	public final static short CATE_JOIN_TABLE = 3;		//拼台	public final static short CATE_MERGER_TABLE = 4;	//并台	public final static short CATE_MERGER_CHILD = 5;	//并台(子账单)	short mCategory = CATE_NORMAL;						//the category to this order		//the id to this order	int mId;			//the sequence id to this order	int mSeqId;	//the restaurant id this order belong to	int mRestaurantId;		//the birth order date time	long mBirthDate;	//the last modified order date time	long mOrderDate;	//the order foods	OrderFood[] mOrderFoods;		//the discount to this order	PDiscount mDiscount;		//the price plan to this order	PricePlan mPricePlan;	//the sub orders	Order[] mChildOrders;	//the region info to this order	PRegion mRegion;		//the destination table to this order	PTable mDestTbl;	//the custom number to this order	int mCustomNum;				//the member associated with this order	PMember mMember;			//the member operation id associated with this order	int mMemberOperationId;	//the comment to this order	String mComment;		//the repaid price to this order	int mRepaidPrice = 0;	//the cash received from customer	int mReceivedCash = 0;					//the service rate to this order	int mServiceRate = 0;	//the discount price to this order	int mDiscountPrice = 0;	//the cancel price to this order	int mCancelPrice = 0;		public void setComment(String comment){		this.mComment = comment;	}		public String getComment(){		if(this.mComment == null){			this.mComment = "";		}		return this.mComment;	}		public void setPaymentType(int payType){		this.mPaymentType = payType;	}		public int getPaymentType(){		return this.mPaymentType;	}		public boolean isPayByCash(){		return this.mPaymentType == PAYMENT_CASH;	}		public boolean isPayByCreditCard(){		return this.mPaymentType == PAYMENT_CREDIT_CARD;	}		public boolean isPayByMember(){		return this.mPaymentType == PAYMENT_MEMBER;	}		public boolean isPayBySign(){		return this.mPaymentType == PAYMENT_SIGN;	}		public boolean isPayByHang(){		return this.mPaymentType == PAYMENT_HANG;	}		public void setSettleType(int payType){		this.mSettleType = payType;	}		public int getSettleType(){		return this.mSettleType;	}		public boolean isSettledByNormal(){		return this.mSettleType == SETTLE_BY_NORMAL;	}		public boolean isSettledByMember(){		return this.mSettleType == SETTLE_BY_MEMBER;	}		public void setCategory(short category){		this.mCategory = category;	}		public short getCategory(){		return mCategory;	}		public boolean isMerged(){		return mCategory == CATE_MERGER_TABLE;	}		public boolean isMergedChild(){		return mCategory == CATE_MERGER_CHILD;	}		public boolean isNormal(){		return mCategory == CATE_NORMAL;	}		public boolean isJoined(){		return mCategory == CATE_JOIN_TABLE;	}		public boolean isTakeout(){		return mCategory == CATE_TAKE_OUT;	}		public void setStatus(int status){		this.mStatus = status;	}		public int getStatus(){		return mStatus;	}		public boolean isUnpaid(){		return mStatus == STATUS_UNPAID;	}		public boolean isPaid(){		return mStatus == STATUS_PAID;	}		public boolean isRepaid(){		return mStatus == STATUS_REPAID;	}		public void setOrderFoods(OrderFood[] orderFoods){		this.mOrderFoods = orderFoods;	}		public OrderFood[] getOrderFoods(){		if(this.mOrderFoods == null){			this.mOrderFoods = new OrderFood[0];		}		return this.mOrderFoods;	}		public boolean hasOrderFood(){		return this.mOrderFoods != null ? mOrderFoods.length != 0 : false;	}		public PMember getMember(){		return this.mMember;	}		public void setMember(PMember member){		this.mMember = member;	}		public int getMemberOperationId(){		return this.mMemberOperationId;	}		public void setMemberOperationId(int memberOperationId){		this.mMemberOperationId = memberOperationId;	}		public int getSeqId() {		return mSeqId;	}	public void setSeqId(int seqId) {		this.mSeqId = seqId;	}	public int getRestaurantId() {		return mRestaurantId;	}	public void setRestaurantId(int restaurantId) {		this.mRestaurantId = restaurantId;	}	public long getBirthDate() {		return mBirthDate;	}	public void setBirthDate(long birthDate) {		this.mBirthDate = birthDate;	}	public long getOrderDate() {		return mOrderDate;	}	public void setOrderDate(long orderDate) {		this.mOrderDate = orderDate;	}	public void setRegion(PRegion region){		this.mRegion = region;	}		public PRegion getRegion(){		if(this.mRegion == null){			this.mRegion = new PRegion();		}		return this.mRegion;	}		public void setDestTbl(PTable destTbl){		this.mDestTbl = destTbl;	}		public PTable getDestTbl(){		if(this.mDestTbl == null){			this.mDestTbl= new PTable(); 		}		return this.mDestTbl;	}		public void setCustomNum(int customNum){		this.mCustomNum = customNum;	}		public int getCustomNum(){		return mCustomNum;	}		public Order[] getChildOrder(){		return mChildOrders;	}		public void setChildOrder(Order[] childOrders){		mChildOrders = childOrders;	}		/**	 * Check if has the sub order.	 * @return true if has sub order, otherwise false	 */	public boolean hasChildOrder(){		return mChildOrders == null ? false : mChildOrders.length != 0;	}		/**	 * The erase price to this order	 */	int mErasePrice = 0;		public void setErasePrice(int erasePrice){		this.mErasePrice = erasePrice * 100;	}		public int getErasePrice(){		return mErasePrice / 100;	}		public Float getServiceRate(){		return NumericUtil.int2Float(mServiceRate);	}		public void setServiceRate(Float _rate){		mServiceRate = NumericUtil.float2Int(_rate);	}		public void setReceivedCash(Float price) {		mReceivedCash = NumericUtil.float2Int(price);	}		public Float getReceivedCash(){		return NumericUtil.int2Float(mReceivedCash);	}	public void setRepaidPrice(Float repaid){		mRepaidPrice = NumericUtil.float2Int(repaid);	}		public Float getRepaidPrice(){		return NumericUtil.int2Float(mRepaidPrice);	}		public void setDiscountPrice(Float discount){		mDiscountPrice = NumericUtil.float2Int(discount);	}		public Float getDiscountPrice(){		return NumericUtil.int2Float(mDiscountPrice);	}			public void setCancelPrice(Float cancel){		mCancelPrice = NumericUtil.float2Int(cancel);	}		public Float getCancelPrice(){		return NumericUtil.int2Float(mCancelPrice);	}		//the gift price to this order	int mGiftPrice = 0;		public void setGiftPrice(Float gift){		mGiftPrice = NumericUtil.float2Int(gift);	}		public Float getGiftPrice(){		return NumericUtil.int2Float(mGiftPrice);	}		//the total price to this order	int mTotalPrice = 0;			public void setTotalPrice(Float total){		mTotalPrice = NumericUtil.float2Int(total);	}		public Float getTotalPrice(){		return NumericUtil.int2Float(mTotalPrice);	}		/**	 * Here we use an integer to represent the actual price of the order.	 * 如果客户没有设定尾数的处理方式，实收金额会等于合计金额,	 * 如果客户设定了尾数的处理方式，比如“抹零”、“四舍五入”等，实收金额就会根据不同处理方式变化	 */	int mActualPrice = 0;		public void setActualPrice(Float actualPrice){		this.mActualPrice = NumericUtil.float2Int(actualPrice);	}		public Float getActualPrice(){		return NumericUtil.int2Float(mActualPrice);	}		public Order(){		mOrderFoods = new OrderFood[0];	}		public Order(OrderFood[] foods){		this.mOrderFoods = foods;	}		public Order(OrderFood[] foods, int tableAlias, int customNum){		this(foods);		this.getDestTbl().mAliasId = tableAlias;		this.mCustomNum = customNum;	}			public Order(int orderID, OrderFood[] foods, int tableAlias, int customNum){		this(foods, tableAlias, customNum);		mId = orderID;	}		public Order(OrderFood[] foods, short tableAlias, int customNum, Float price){		this(foods, tableAlias, customNum);		setReceivedCash(price);	}		public Order(int orderID, OrderFood[] foods, short tableAlias, int customNum, Float price){		this(orderID, foods, tableAlias, customNum);		setReceivedCash(price);	}			public int getId(){		return mId;	}		public void setId(int id){		this.mId = id;	}		/**	 * Set the discount to this order.	 * The discount to each food is also be set. 	 * @param discount The discount to be set.	 */	public void setDiscount(PDiscount discount){		this.mDiscount = discount;		for(int i = 0; i < mOrderFoods.length; i++){			boolean isFound = false;			if(discount.mPlans != null){				for(int j = 0; j < discount.mPlans.length; j++){					if(mOrderFoods[i].mKitchen.equals(discount.mPlans[j].mKitchen)){						mOrderFoods[i].setDiscountInternal(discount.mPlans[j].mRate);						isFound = true;						break;					}				}			}			if(!isFound){				mOrderFoods[i].setDiscountInternal(100);			}		}	}		/**	 * Get the discount to this order.	 * @return The discount to this order.	 */	public PDiscount getDiscount(){		if(this.mDiscount == null){			this.mDiscount = new PDiscount();		}		return this.mDiscount;	}				public void setPricePlan(PricePlan pricePlan){		mPricePlan = pricePlan;	}		public PricePlan getPricePlan(){		return mPricePlan;	}	public boolean hasPricePlan(){		return mPricePlan == null ? false : mPricePlan.isValid();	}		/**	 * Calculate the pure total price to this order as below.<br>	 * <pre>	 * for(each food){	 *    total += food.unitPrice * food.orderCount;	 * }	 * </pre>	 * @return the pure total price	 */	public Float calcPureTotalPrice(){		int total = 0;		for(int i = 0; i < mOrderFoods.length; i++){			total += ((mOrderFoods[i].mUnitPrice * mOrderFoods[i].getCountInternal()) / 100);		}		return NumericUtil.int2Float(total);	}		/**	 * Calculate the total price to this order as below.<br>	 * <pre>	 * for(each food){	 *    if(!food.isGift()){	 *        total += (food.unitPrice + food.tastePrice) * discount * food.orderCount;	 *    }	 * }	 * total = total * (1 + serviceRate);	 * </pre>	 * @return the total price	 */	public Float calcTotalPrice(){		int totalPrice = 0;		for(int i = 0; i < mOrderFoods.length; i++){			if(!mOrderFoods[i].isGift()){				totalPrice += mOrderFoods[i].calcPriceWithTasteInternal();			}		}		totalPrice = totalPrice * (100 + mServiceRate) / 100;				return NumericUtil.int2Float(totalPrice >= 0 ? totalPrice : 0);	}		/**	 * Calculate the total price before discount to this order as below.<br>	 * <pre>	 * for(each food){	 *    if(!food.isGift()){	 *        total += (food.unitPrice + food.tastePrice) * food.orderCount;	 *    }	 * }	 * </pre>	 * @return The total price before discount to this order.	 */	public Float calcPriceBeforeDiscount(){		int totalPrice = 0;		for(int i = 0; i < mOrderFoods.length; i++){			if(!mOrderFoods[i].isGift()){				totalPrice += mOrderFoods[i].calcPriceBeforeDiscountInternal();			}		}		return NumericUtil.int2Float(totalPrice);	}		/**	 * Calculate the total price of gifted foods.	 * @return the total price of gifted foods	 */	public Float calcGiftPrice(){		int totalGifted = 0;		for(int i = 0; i < mOrderFoods.length; i++){			if(mOrderFoods[i].isGift()){				totalGifted += mOrderFoods[i].calcPriceWithTasteInternal();			}		}		return NumericUtil.int2Float(totalGifted);	}		/**	 * Calculate the total discount price.	 * @return the total discount	 */	public Float calcDiscountPrice(){		int totalDiscount = 0;		for(int i = 0; i < mOrderFoods.length; i++){			totalDiscount += mOrderFoods[i].calcDiscountPriceInternal();		}		return NumericUtil.int2Float(totalDiscount);	}		/**	 * Add the food to the list.	 * @param foodToAdd The food to add	 * @throws ProtocolException	 * 			Throws if the order amount of the added food exceed MAX_ORDER_AMOUNT	 */	public void addFood(OrderFood foodToAdd) throws ProtocolException{		//如果新添加的菜品在原来菜品List中已经存在相同的菜品，则累加数量		boolean isExist = false;		for(int i = 0; i < mOrderFoods.length; i++){			if(foodToAdd.equals(mOrderFoods[i])){				mOrderFoods[i].addCountInternal(foodToAdd.getCountInternal());				isExist = true;				break;			}		}		//如果新添加的菜品在原来菜品List中没有相同的菜品，则添加到菜品列表		if(!isExist){			OrderFood[] newFoods = new OrderFood[mOrderFoods.length + 1];			System.arraycopy(mOrderFoods, 0, newFoods, 0, mOrderFoods.length);			newFoods[mOrderFoods.length] = foodToAdd;			mOrderFoods = newFoods;		}				}				/**	 * Add the foods to list.	 * @param foodsToAdd The foods to add	 */	public void addFoods(OrderFood[] foodsToAdd){				//克隆foodsToAdd		OrderFood[] extraFoods = new OrderFood[foodsToAdd.length];		System.arraycopy(foodsToAdd, 0, extraFoods, 0, extraFoods.length);				//遍历新添加的菜品List中，如果与原来菜品相同的，则累加数量		int nExist = 0;		for(int i = 0; i < extraFoods.length; i++){			for(int j = 0; j < mOrderFoods.length; j++){				if(extraFoods[i].equals(mOrderFoods[j])){					try{						mOrderFoods[j].addCountInternal(extraFoods[i].getCountInternal());					}catch(ProtocolException e){						mOrderFoods[j].setCountInternal(OrderFood.MAX_ORDER_AMOUNT);					}					extraFoods[i] = null;					nExist++;					break;				}			}		}				//新添加菜品List中，如果与原来菜品不相同的，则添加到菜品列表		OrderFood[] newFoods = new OrderFood[mOrderFoods.length + extraFoods.length - nExist];		System.arraycopy(mOrderFoods, 0, newFoods, 0, mOrderFoods.length);				int availPos = mOrderFoods.length; 		for(int i = 0; i < extraFoods.length; i++){			if(extraFoods[i] != null){				newFoods[availPos++] = extraFoods[i];			}		}		mOrderFoods = newFoods;	}		/**	 * Remove the food.	 * @param foodToRemove The food to remove.	 * @return true if the food to remove is found, otherwise false.	 */	public boolean remove(OrderFood foodToRemove){		boolean isExist = false;		for(int i = 0; i < mOrderFoods.length; i++){			if(mOrderFoods[i].equals(foodToRemove)){				mOrderFoods[i] = null;				isExist = true;				break;			}		}				if(isExist){			OrderFood[] newFoods = new OrderFood[mOrderFoods.length - 1];			int pos = 0;			for(int i = 0; i < mOrderFoods.length; i++){				if(mOrderFoods[i] != null){					newFoods[pos++] = mOrderFoods[i];				}			}			mOrderFoods = newFoods;		}				return isExist;			}	public void copyFrom(Order src){		if(this != src && src != null){			this.mId = src.mId;			this.mActualPrice = src.mActualPrice;			this.mBirthDate = src.mBirthDate;			this.mCancelPrice = src.mCancelPrice;			this.mCategory = src.mCategory;			this.mChildOrders = src.mChildOrders;			this.mCustomNum = src.mCustomNum;			this.mDestTbl = src.mDestTbl;			this.mDiscount = src.mDiscount;			this.mDiscountPrice = src.mDiscountPrice;			this.mErasePrice = src.mErasePrice;			this.mGiftPrice = src.mGiftPrice;			this.mOrderDate = src.mOrderDate;			this.mOrderFoods = src.mOrderFoods;			this.mPaymentType = src.mPaymentType;			this.mSettleType = src.mSettleType;			this.mPricePlan = src.mPricePlan;			this.mReceivedCash = src.mReceivedCash;			this.mRegion = src.mRegion;			this.mRepaidPrice = src.mRepaidPrice;			this.mRestaurantId = src.mRestaurantId;			this.mSeqId = src.mSeqId;			this.mServiceRate = src.mServiceRate;			this.mStatus = src.mStatus;			this.mTotalPrice = src.mTotalPrice;			this.mMember = src.mMember;			this.mComment = src.mComment;		}	}		public int hashCode(){		return 17 + 31 * mId;	}		public String toString(){		return "order(id = " + mId + ")";	}		public void writeToParcel(Parcel dest, int flag) {		dest.writeByte(flag);		if(flag == ORDER_PARCELABLE_4_QUERY){			dest.writeInt(this.mId);			dest.writeParcel(this.mDestTbl, PTable.TABLE_PARCELABLE_SIMPLE);			dest.writeLong(this.mBirthDate);			dest.writeLong(this.mOrderDate);			dest.writeByte(this.mCategory);			dest.writeByte(this.mCustomNum);			//dest.writeInt(this.mReceivedCash);			dest.writeParcelArray(this.mOrderFoods, OrderFood.OF_PARCELABLE_4_QUERY);					}else if(flag == ORDER_PARCELABLE_4_COMMIT){			dest.writeInt(this.mId);			dest.writeParcel(this.mDestTbl, PTable.TABLE_PARCELABLE_SIMPLE);			dest.writeLong(this.mOrderDate);			dest.writeByte(this.mCategory);			dest.writeByte(this.mCustomNum);			dest.writeParcelArray(this.mOrderFoods, OrderFood.OF_PARCELABLE_4_COMMIT);					}else if(flag == ORDER_PARCELABLE_4_PAY){			dest.writeInt(this.mId);			dest.writeParcel(this.mMember, PMember.MEMBER_PARCELABLE_SIMPLE);			dest.writeParcel(this.mDestTbl, PTable.TABLE_PARCELABLE_SIMPLE);			dest.writeShort(this.mCustomNum);			dest.writeInt(this.mReceivedCash);			dest.writeByte(this.mSettleType);			dest.writeParcel(this.mDiscount, PDiscount.DISCOUNT_PARCELABLE_SIMPLE);			dest.writeParcel(this.mPricePlan, PricePlan.PP_PARCELABLE_SIMPLE);			dest.writeInt(this.mErasePrice);			dest.writeByte(this.mPaymentType);			dest.writeInt(this.mServiceRate);			dest.writeString(this.mComment);		}	}	public void createFromParcel(Parcel source) {		short flag = source.readByte();		if(flag == ORDER_PARCELABLE_4_QUERY){			this.mId = source.readInt();			this.mDestTbl = (PTable)source.readParcel(PTable.TABLE_CREATOR);			this.mBirthDate = source.readLong();			this.mOrderDate = source.readLong();			this.mCategory = source.readByte();			this.mCustomNum = source.readByte();			//this.mReceivedCash = source.readInt();			Parcelable[] parcelables = source.readParcelArray(OrderFood.OF_CREATOR);			if(parcelables != null){				this.mOrderFoods = new OrderFood[parcelables.length];				for(int i = 0; i < mOrderFoods.length; i++){					mOrderFoods[i] = (OrderFood)parcelables[i];				}			}					}else if(flag == ORDER_PARCELABLE_4_COMMIT){			this.mId = source.readInt();			this.mDestTbl = (PTable)source.readParcel(PTable.TABLE_CREATOR);			this.mOrderDate = source.readLong();			this.mCategory = source.readByte();			this.mCustomNum = source.readByte();			Parcelable[] parcelables = source.readParcelArray(OrderFood.OF_CREATOR);			if(parcelables != null){				this.mOrderFoods = new OrderFood[parcelables.length];				for(int i = 0; i < mOrderFoods.length; i++){					mOrderFoods[i] = (OrderFood)parcelables[i];				}			}					}else if(flag == ORDER_PARCELABLE_4_PAY){			this.mId = source.readInt();			this.mMember = (PMember)source.readParcel(PMember.MEMBER_CREATOR);			this.mDestTbl = (PTable)source.readParcel(PTable.TABLE_CREATOR);			this.mCustomNum = source.readShort();			this.mReceivedCash = source.readInt();			this.mSettleType = source.readByte();			this.mDiscount = (PDiscount)source.readParcel(PDiscount.DISCOUNT_CREATOR);			this.mPricePlan = (PricePlan)source.readParcel(PricePlan.PP_CREATOR);			this.mErasePrice = source.readInt();			this.mPaymentType = source.readByte();			this.mServiceRate = source.readInt();			this.mComment = source.readString();		}	}		public final static Parcelable.Creator ORDER_CREATOR = new Parcelable.Creator() {				public Parcelable[] newInstance(int size) {			return new Order[size];		}				public Parcelable newInstance() {			return new Order();		}	};	}
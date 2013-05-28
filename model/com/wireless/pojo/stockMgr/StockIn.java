package com.wireless.pojo.stockMgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;

public class StockIn implements Jsonable{

	/**
	 * The helper class to create the StockIn object to perform insert
	 */
	public static class InsertBuilder{
		private final int restaurantId;
		private final String oriStockId;
		
		private int deptIn;
		private int deptOut;
		private int operatorId;
		private String operator;
		private long operateDate;
		private float amount;
		private float price;
		private String comment;
		private SubType subType;
		
		public InsertBuilder(int restaurantId, String oriStockId){
			this.restaurantId = restaurantId;
			this.oriStockId = oriStockId;
		}
		
		public StockIn build(){
			return new StockIn(this);
		}

		
		
		public int getRestaurantId() {
			return restaurantId;
		}

		public String getOriStockId() {
			return oriStockId;
		}

		

		public int getDeptIn() {
			return deptIn;
		}

		public InsertBuilder setDeptIn(int deptIn) {
			this.deptIn = deptIn;
			return this;
		}

		public int getDeptOut() {
			return deptOut;
		}

		public InsertBuilder setDeptOut(int deptOut) {
			this.deptOut = deptOut;
			return this;
		}

		public int getOperatorId() {
			return operatorId;
		}

		public InsertBuilder setOperatorId(int operatorId) {
			this.operatorId = operatorId;
			return this;
		}

		public String getOperator() {
			if(operator == null){
				operator = "";
			}
			return operator;
		}

		public InsertBuilder setOperator(String operator) {
			this.operator = operator;
			return this;
		}

		public long getOperateDate() {
			return operateDate;
		}

		public InsertBuilder setOperateDate(long operateDate) {
			this.operateDate = operateDate;
			return this;
		}

		public float getAmount() {
			return amount;
		}

		public InsertBuilder setAmount(float amount) {
			this.amount = amount;
			return this;
		}

		public float getPrice() {
			return price;
		}

		public InsertBuilder setPrice(float price) {
			this.price = price;
			return this;
		}

		public String getComment() {
			if(comment == null){
				comment = "";
			}
			return comment;
		}

		public InsertBuilder setComment(String comment) {
			this.comment = comment;
			return this;
		}

		public SubType getSubType() {
			return subType;
		}

		public void setSubType(SubType subType) {
			this.subType = subType;
		}
		
		public void setSubType(int val){
			this.subType = SubType.valueOf(val);
		}

	
	}
	/**
	 * The helper class to create the StockIn object used in update
	 */
	public static class UpdateBuilder{
		private final int id;
		
		private int approverId;
		private String approver;
		private long approverDate;
		private float amount;
		private float price;
		private Status status;
		
		public StockIn build(){
			return new StockIn(this);
		}
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public int getId() {
			return id;
		}

		public int getApproverId() {
			return approverId;
		}

		public UpdateBuilder setApproverId(int approverId) {
			this.approverId = approverId;
			return this;
		}

		public String getApprover() {
			if(approver == null){
				approver = "";
			}
			return approver;
		}

		public UpdateBuilder setApprover(String approver) {
			this.approver = approver;
			return this;
		}

		public long getApproverDate() {
			return approverDate;
		}

		public UpdateBuilder setApproverDate(long approverDate) {
			this.approverDate = approverDate;
			return this;
		}


		public float getAmount() {
			return amount;
		}

		public UpdateBuilder setAmount(float amount) {
			this.amount = amount;
			return this;
		}

		public float getPrice() {
			return price;
		}

		public UpdateBuilder setPrice(float price) {
			this.price = price;
			return this;
		}

		public Status getStatus() {
			return status;
		}

		public void setStatus(Status status) {
			this.status = status;
		}

		public void setStatus(int statusval){
			this.status = Status.valueOf(statusval);
		}
		
		
	}
	
	/**
	 * 库单状态
	 * 1 - 未审核，2 - 审核通过， 3 - 冲红
	 */
	public static enum Status{
		UNAUDIT(1, "未审核"), 
		AUDIT(2, "审核通过"),
		DELETE(3, "冲红");
		
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
			for (Status status : values()) {
				if(status.val == val){
					return status;
				}
			}
			throw new IllegalArgumentException("the stockIn status(val = " + val + ") is invalid");
		}
		
		public int getVal(){
			return val;
		}
		
		public String getDesc(){
			return desc;
		}
		
	}
	/**
	 * 货单大类
	 * 1-入库, 2-出库
	 */
	public static enum Type{
		STOCKIN(1, "入库"),
		STOCKOUT(2, "出库");
		
		private final int val;
		private final String desc;
		
		Type(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		@Override
		public String toString(){
			return "type(" +
				   "val = " + val + 
				   ", desc = " + desc + ")";
		}
		public static Type valueOf(int val){
			for (Type type : values()) {
				if(type.val == val){
					return type;
				}
			}
			throw new IllegalArgumentException("the stockIn type(val = " + val + ") is invalid");
		}
	} 
	
	/**
	 * 货单小类
	 *1-商品入库/出库, 2-商品调拨, 3-商品报溢/报损, 4-原料入库/出库, 5-原料调拨, 6-原料报溢/报损
	 */
	public static enum SubType{
		GOODSSTOCKIN(1, "商品入库", "商品出库"),
		GOODSTRANSFER(2, "商品入库调拨", "商品出库调拨"),
		GOODSSPILL(3, "商品报溢", "商品报损"),
		MATERIALSTOCKIN(4, "原料入库", "原料出库"),
		MATERIALTRANSFER(5, "原料入库调拨", "原料出库调拨"),
		MATERIALSPILL(6, "原料报溢", "原料报损");
		
		
		private final int val;
		private final String in;
		private final String out;
		
		SubType(int val, String desc, String desc2){
			this.val = val;
			this.in = desc;
			this.out = desc2;
		}
		
		@Override
		public String toString(){
			return "type(" +
				   "val = " + val + 
				   ", in = " + in + 
				   ", out = " + out + ")";
		}
		
		public static SubType valueOf(int val){
			for (SubType sType : values()) {
				if(sType.val == val){
					return sType;
				}
			}
			throw new IllegalArgumentException("the stockIn suTtype(val = " + val + ") is invalid");
		}
		
		public int getVal(){
			return val;
		}
		
		public String getIn(){
			return in;
		}
		
		public String getOut(){
			return out;
		}
		
	}
	
	private int id;
	private int restaurantId;
	private String oriStockId;
	private int approverId;
	private String approver;
	private long approverDate;
	private int deptIn;
	private int deptOut;
	private int operatorId;
	private String operator;
	private long operateDate;
	private float amount;
	private float price;
	private SubType subType;
	private Type type;
	private Status status = Status.UNAUDIT;
	private String comment;
	private List<StockInDetail> stockDetails = new ArrayList<StockInDetail>();
	

	public long getApproverDate() {
		return approverDate;
	}

	public void setApproverDate(long approverDate) {
		this.approverDate = approverDate;
	}

	public long getOperateDate() {
		return operateDate;
	}

	public void setOperateDate(long operateDate) {
		this.operateDate = operateDate;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = this.getCount();
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = this.getSum();
	}

	public List<StockInDetail> getStockDetails() {
		return stockDetails;
	}

	public void setStockDetails(List<StockInDetail> stockDetails) {
		if(stockDetails != null){
			this.stockDetails.clear();
			this.stockDetails.addAll(stockDetails);
		}
		
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

	public String getOriStockId() {
		if (oriStockId == null) {
			oriStockId = "";
		}
		return oriStockId;
	}

	public void setOriStockId(String oriStockId) {
		this.oriStockId = oriStockId;
	}

	public int getApproverId() {
		return approverId;
	}

	public void setApproverId(int approverId) {
		this.approverId = approverId;
	}

	public String getApprover() {
		if (approver == null) {
			approver = "";
		}
		return approver;
	}

	public void setApprover(String approver) {
		this.approver = approver;
	}


	public int getDeptIn() {
		return deptIn;
	}

	public void setDeptIn(int deptIn) {
		this.deptIn = deptIn;
	}

	public int getDeptOut() {
		return deptOut;
	}

	public void setDeptOut(int deptOut) {
		this.deptOut = deptOut;
	}

	public int getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(int operatorId) {
		this.operatorId = operatorId;
	}

	public String getOperator() {
		if (operator == null) {
			operator = "";
		}
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}


	public SubType getSubType() {
		return subType;
	}

	public void setSubType(SubType subType) {
		this.subType = subType;
	}
	
	public void setSubType(int val){
		this.subType = SubType.valueOf(val);
	}
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public void setType(int val){
		
		this.type = Type.valueOf(val);
	}

	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status){
		this.status = status;
	}

	public String getComment() {
		if (comment == null) {
			comment = "";
		}
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public StockIn(){}
	
	public StockIn(InsertBuilder build){
		setRestaurantId(build.getRestaurantId());
		setOriStockId(build.getOriStockId());
		setDeptIn(build.getDeptIn());
		setDeptOut(build.getDeptOut());
		setOperatorId(build.getOperatorId());
		setOperator(build.getOperator());
		setOperateDate(build.getOperateDate());
		setAmount(build.getAmount());
		setPrice(build.getPrice());
		setComment(build.getComment());
	}
	
	public StockIn(UpdateBuilder build){
		setId(build.getId());
		setApprover(build.getApprover());
		setApproverId(build.getApproverId());
		setAmount(build.getAmount());
		setPrice(build.getPrice());
		setStatus(build.getStatus());
	}
	
	public float getCount(){
		float count = 0;
		for (StockInDetail sDetail : this.stockDetails) {
			count += sDetail.getAmount();
		}
		return count;
	}
	
	public float getSum(){
		float sum = 0;
		for (StockInDetail sDetail : this.stockDetails) {
			sum += sDetail.getAmount()*sDetail.getPrice();
		}
		return sum;
	}
	
	
	@Override
	public String toString() {
		return "stockIn : id=" + id + 
				"oriId=" + getOriStockId() + 
				"approver=" + getApprover() + 
				"deptIn=" + deptIn + 
				"deptOut=" + deptOut + 
				"operator=" + getOperator() + 
				"amount=" + amount + 
				"status" + status;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj instanceof StockIn) {
			return false;
		} else {
			return id == ((StockIn) obj).id
					&& restaurantId == ((StockIn) obj).restaurantId
					&& oriStockId == ((StockIn) obj).oriStockId;
		}
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = result * 31 + id;
		result = result * 31 + restaurantId;
		return result;
	}

	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		jm.put("id", this.getId());
		jm.put("restaurantId", this.getRestaurantId());
		jm.put("oriStockId", this.getOriStockId());
		jm.put("approverId", this.getApproverId());
		jm.put("approver", this.getApprover());
		jm.put("approverDate", this.getApproverDate());
		jm.put("deptIn", this.getDeptIn());
		jm.put("deptOut", this.getDeptOut());
		jm.put("operatorId", this.getOperatorId());
		jm.put("operator", this.getOperator());
		jm.put("operateDate", this.getOperateDate());
		jm.put("amount", this.getAmount());
		jm.put("price", this.getPrice());
		jm.put("subTypeValue", this.getSubType().getVal());
		jm.put("subTypeText", this.getSubType().getIn());
		jm.put("subTypeText2", this.getSubType().getOut());
		jm.put("statusValue", this.getStatus().getVal());
		jm.put("statusText", this.getStatus().getDesc());
		jm.put("comment", this.getComment());
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		// TODO Auto-generated method stub
		return null;
	}


}

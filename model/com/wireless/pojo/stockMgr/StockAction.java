package com.wireless.pojo.stockMgr;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.supplierMgr.Supplier;
import com.wireless.pojo.util.DateUtil;

public class StockAction implements Jsonable{

	/**
	 * The helper class to create the StockIn object to perform insert
	 */
	public static class InsertBuilder{
		private final int restaurantId;
		private String oriStockId;
		private long oriStockIdDate;
		private Supplier supplier = new Supplier();
		private Department deptIn = new Department();
		private Department deptOut = new Department();
		private int operatorId;
		private String operator;
		private List<StockActionDetail> stockInDetails = new ArrayList<StockActionDetail>(); 
		private String comment;
		
		private CateType cateType ;
		private Status status = Status.UNAUDIT;
		private Type type;
		private SubType subType;
		
		private InsertBuilder(int restaurantId){
			this.restaurantId = restaurantId;
		}
		//入库采购
		public static InsertBuilder newStockIn(int restaurantId, long oriStockIdDate){
			InsertBuilder builder = new InsertBuilder(restaurantId);
			builder.setType(Type.STOCK_IN).setSubType(SubType.STOCK_IN).setOriStockIdDate(oriStockIdDate);
			return builder;
		}
		//入库调拨
		public static InsertBuilder newStockInTransfer(int restaurantId){
			InsertBuilder builder = new InsertBuilder(restaurantId);
			builder.setType(Type.STOCK_IN).setSubType(SubType.STOCK_IN_TRANSFER);
			return builder;
		}
		//报溢
		public static InsertBuilder newSpill(int restaurantId){
			InsertBuilder builder = new InsertBuilder(restaurantId);
			builder.setType(Type.STOCK_IN).setSubType(SubType.SPILL);
			return builder;
		}
		//退货
		public static InsertBuilder newStockOut(int restaurantId, long oriStockIdDate){
			InsertBuilder builder = new InsertBuilder(restaurantId);
			builder.setType(Type.STOCK_OUT).setSubType(SubType.STOCK_OUT).setOriStockIdDate(oriStockIdDate);
			return builder;
		}
		//出库调拨
		public static InsertBuilder newStockOutTransfer(int restaurantId){
			InsertBuilder builder = new InsertBuilder(restaurantId);
			builder.setType(Type.STOCK_OUT).setSubType(SubType.STOCK_OUT_TRANSFER);
			return builder;
		}
		//报损
		public static InsertBuilder newDamage(int restaurantId){
			InsertBuilder builder = new InsertBuilder(restaurantId);
			builder.setType(Type.STOCK_OUT).setSubType(SubType.DAMAGE);
			return builder;
		}
		//盘盈
		public static InsertBuilder newMore(int restaurantId){
			InsertBuilder builder = new InsertBuilder(restaurantId);
			builder.setType(Type.STOCK_IN).setSubType(SubType.MORE).setStatus(Status.AUDIT);
			return builder;
		}
		//盘亏
		public static InsertBuilder newLess(int restaurantId){
			InsertBuilder builder = new InsertBuilder(restaurantId);
			builder.setType(Type.STOCK_OUT).setSubType(SubType.LESS).setStatus(Status.AUDIT);
			return builder;
		}
		//消耗
		public static InsertBuilder newUseUp(int restaurantId){
			InsertBuilder builder = new InsertBuilder(restaurantId);
			builder.setType(Type.STOCK_OUT).setSubType(SubType.USE_UP);
			return builder;
		}
		public StockAction build(){
			return new StockAction(this);
		}
		
		public int getRestaurantId() {
			return restaurantId;
		}

		public String getOriStockId() {
			return oriStockId;
		}
		public InsertBuilder setOriStockId(String oriStockId){
			this.oriStockId = oriStockId;
			return this;
		}

		public List<StockActionDetail> getStockInDetails() {
			return stockInDetails;
		}

		
		public long getOriStockIdDate() {
			return oriStockIdDate;
		}

		public InsertBuilder setOriStockIdDate(long oriStockIdDate) {
			this.oriStockIdDate = oriStockIdDate;
			return this;
		}

		public InsertBuilder addDetail(StockActionDetail detail){
			this.stockInDetails.add(detail);
			return this;
		}



		public Department getDeptIn() {
			return deptIn;
		}
		
		
		public InsertBuilder setDeptIn(short i){
			this.deptIn.setId(i);
			return this;
		}

		
		public InsertBuilder setDeptIn(Department deptIn) {
			this.deptIn = deptIn;
			return this;
			
		}
		
		public InsertBuilder setDeptInName(String name){
			this.deptIn.setName(name);
			return this;
		}


		public Department getDeptOut() {
			return deptOut;
		}
		
		public InsertBuilder setDeptOut(short deptOut){
			this.deptOut.setId(deptOut);
			return this;
		}
		
		public InsertBuilder setDeptOutName(String name){
			this.deptOut.setName(name);
			return this;
		}

		public InsertBuilder setDeptOut(Department deptOut) {
			this.deptOut = deptOut;
			return this;
		}	

		public Supplier getSupplier() {
			return supplier;
		}

		public void setSupplier(Supplier supplier) {
			this.supplier = supplier;
		}
		
		public InsertBuilder setSupplierId(int supplierId){
			this.supplier.setSupplierid(supplierId);
			return this;
		}
		
		public InsertBuilder setSupplierName(String name){
			this.supplier.setName(name);
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
		
		public Status getStatus() {
			return status;
		}

		public void setStatus(Status status) {
			this.status = status;
		}
		
		public Type getType() {
			return type;
		}

		private InsertBuilder setType(Type type) {
			this.type = type;
			return this;
		}
		
		public SubType getSubType() {
			return subType;
		}

		private InsertBuilder setSubType(SubType subType) {
			this.subType = subType;
			return this;
		}
		
		public CateType getCateType() {
			return cateType;
		}

		public InsertBuilder setCateType(CateType cateType) {
			this.cateType = cateType;
			return this;
		}
		
		public InsertBuilder setCateType(int val){
			this.cateType = CateType.valueOf(val);
			return this;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof InsertBuilder)) {
				return false;
			} else {
				return restaurantId == ((InsertBuilder) obj).restaurantId
						&& subType == ((InsertBuilder) obj).subType;
			}
		}

		@Override
		public int hashCode() {
			int result = 17;
			result = result * 31 + restaurantId;
			return result;
		}
		@Override
		public String toString() {
			return "stockInsert : " +
					"deptIn=" + deptIn + 
					"deptOut=" + deptOut + 
					"stockInDetails =" + stockInDetails +
					"operator=" + getOperator() + 
					"status" + status;
		}
		

	
	}
	/**
	 * The helper class to create the StockIn object used in update
	 */
	public static class AuditBuilder{
		private final int id;
		
		private int approverId;
		private String approver;
		private Status status;
		
		public StockAction build(){
			return new StockAction(this);
		}
		
		public AuditBuilder(int id){
			this.id = id;
		}
		
		public static AuditBuilder newStockActionAudit(int id){
			AuditBuilder builder = new AuditBuilder(id);
			builder.status = Status.AUDIT;
			return builder;
		}
		
		public int getId() {
			return id;
		}

		public int getApproverId() {
			return approverId;
		}

		public AuditBuilder setApproverId(int approverId) {
			this.approverId = approverId;
			return this;
		}

		public String getApprover() {
			if(approver == null){
				approver = "";
			}
			return approver;
		}

		public AuditBuilder setApprover(String approver) {
			this.approver = approver;
			return this;
		}

		public Status getStatus() {
			return status;
		}

		public AuditBuilder setStatus(Status status) {
			this.status = status;
			return this;

		}

		public void setStatus(int statusval){
			this.status = Status.valueOf(statusval);
			
		}
		
		
	}
	
	/**
	 * 货品类型
	 * 1-商品, 2-原料
	 */
	public static enum CateType{
		GOOD(1, "商品"),
		MATERIAL(2, "原料");
		
		private int value;
		private String text;
		
		CateType(int value, String text){
			this.value = value;
			this.text = text;
		}
		public int getValue() {
			return value;
		}
		public String getText() {
			return text;
		}
		@Override
		public String toString(){
			return "CateType(" +
					"val = " + value +
					"text = " + text + ")";
		}
		
		public static CateType valueOf(int value){
			for(CateType temp : values()){
				if(temp.value == value){
					return temp;
				}
			}
			throw new IllegalArgumentException("The type value(val = " + value + ") passed is invalid.");
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
		STOCK_IN(1, "入库"),
		STOCK_OUT(2, "出库");
		
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
		public int getVal() {
			return val;
		}
		public String getDesc() {
			return desc;
		}
		
		
		
	} 
	
	/**
	 * 货单小类
	 *1-商品入库/出库, 2-商品调拨, 3-商品报溢/报损, 4-原料入库/出库, 5-原料调拨, 6-原料报溢/报损
	 */
	public static enum SubType{
/*		GOODS_STOCKIN(1, "商品入库", "商品出库"),
		GOODS_TRANSFER(2, "商品入库调拨", "商品出库调拨"),
		GOODS_SPILL(3, "商品报溢", "商品报损"),
		MATERIAL_STOCKIN(4, "原料入库", "原料出库"),
		MATERIAL_TRANSFER(5, "原料入库调拨", "原料出库调拨"),
		MATERIAL_SPILL(6, "原料报溢", "原料报损");*/

		STOCK_IN(1, "采购"),
		STOCK_IN_TRANSFER(2, "入库调拨"),
		SPILL(3, "报溢"),
		STOCK_OUT(4, "退货"),
		STOCK_OUT_TRANSFER(5, "出库调拨"),
		DAMAGE(6, "报损"),
		MORE(7, "盘盈"),
		LESS(8, "盘亏"),
		USE_UP(9, "消耗");
			
		private final int val;
		private final String text;

		
		SubType(int val, String text){
			this.val = val;
			this.text = text;
		}
		
		@Override
		public String toString(){
			return "type(" +
				   "val = " + val + 
				   ", text = " +text + ")";
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

		public String getText() {
			return text;
		}
		
		
	}
	
	private int id;
	private int restaurantId;
	private String oriStockId;
	private long birthDate;
	private long oriStockIdDate;
	private int approverId;
	private String approver;
	private long approverDate;
	private Department deptIn = new Department();
	private Department deptOut = new Department();
	private Supplier supplier = new Supplier();
	private int operatorId;
	private String operator;
	private float amount;
	private float price;
	private CateType cateType;
	private SubType subType;
	private Type type;
	private Status status = Status.UNAUDIT;
	private String comment;
	private List<StockActionDetail> stockDetails = new ArrayList<StockActionDetail>();
	

	public long getApproverDate() {
		return approverDate;
	}

	public void setApproverDate(long approverDate) {
		this.approverDate = approverDate;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public List<StockActionDetail> getStockDetails() {
		return stockDetails;
	}

	public void setStockDetails(List<StockActionDetail> stockDetails) {
		if(stockDetails != null){
			this.stockDetails.clear();
			this.stockDetails.addAll(stockDetails);
		}
		
	}
	
	public void addStockDetail(StockActionDetail sDetail){
		this.stockDetails.add(sDetail);
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
	
	
	public long getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(long birthDate) {
		this.birthDate = birthDate;
	}

	public long getOriStockIdDate() {
		return oriStockIdDate;
	}

	public void setOriStockIdDate(long oriStockIdDate) {
		this.oriStockIdDate = oriStockIdDate;
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



	public Department getDeptIn() {
		return deptIn;
	}

	public Department setDeptIn(Department deptIn) {
		this.deptIn = deptIn;
		return deptIn;
	}
	
	

	public Department getDeptOut() {
		return deptOut;
	}

	public Department setDeptOut(Department deptOut) {
		this.deptOut = deptOut;
		return deptOut;
	}
	
	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
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

	
	public CateType getCateType() {
		return cateType;
	}

	public void setCateType(CateType cateType) {
		this.cateType = cateType;
	}
	
	public void setCateType(int val){
		this.cateType = CateType.valueOf(val);
		
	}

	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status){
		this.status = status;
	}
	
	public void setStatus(int val){
		this.status = Status.valueOf(val);
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

	public StockAction(){}
	
	public StockAction(InsertBuilder build){
		setRestaurantId(build.getRestaurantId());
		setOriStockId(build.getOriStockId());
		setOriStockIdDate(build.getOriStockIdDate());
		setDeptIn (build.getDeptIn());
		setDeptOut(build.getDeptOut());
		setSupplier(build.getSupplier());
		setOperatorId(build.getOperatorId());
		setOperator(build.getOperator());
		setStockDetails(build.getStockInDetails());
		setCateType(build.getCateType());
		setType(build.getType());
		setSubType(build.getSubType());
		setStatus(build.getStatus());
		setComment(build.getComment());
	}
	
	public StockAction(AuditBuilder build){
		setId(build.getId());
		setApprover(build.getApprover());
		setApproverId(build.getApproverId());
		setStatus(build.getStatus());
	}
	
	public float getTotalAmount(){
		float count = 0;
		for (StockActionDetail sDetail : this.stockDetails) {
			count += sDetail.getAmount();
		}
		return count;
	}
	
	public float getTotalPrice(){
		float sum = 0;
		for (StockActionDetail sDetail : this.stockDetails) {
			sum += sDetail.getAmount() * sDetail.getPrice();
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
		if (obj == null || !(obj instanceof StockAction)) {
			return false;
		} else {
			return id == ((StockAction) obj).id
					&& restaurantId == ((StockAction) obj).restaurantId;
//					&& oriStockId == ((StockAction) obj).oriStockId;
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
		jm.put("id", this.id);
		jm.put("restaurantId", this.restaurantId);
		jm.put("birthDateFormat", DateUtil.format(this.birthDate));
		jm.put("oriStockId", this.oriStockId);
		jm.put("oriStockDateFormat", DateUtil.format(this.oriStockIdDate));
		jm.put("approverId", this.approverId);
		jm.put("approverName", this.approver);
		jm.put("approverDateFormat", DateUtil.format(this.getApproverDate()));
		jm.put("deptIn", this.deptIn);
		jm.put("deptOut", this.deptIn);
		jm.put("supplier", this.supplier);
		jm.put("operatorId", this.operatorId);
		jm.put("operatorName", this.operator);
		jm.put("amount", this.getTotalAmount());
		jm.put("price", this.getTotalPrice());
		jm.put("cateTypeValue", this.cateType.getValue());
		jm.put("cateTypeText", this.cateType.getText());
		jm.put("typeValue", this.type.getVal());
		jm.put("typeText", this.type.getDesc());
		jm.put("subTypeValue", this.subType.getVal());
		jm.put("subTypeText", this.subType.getText());
		jm.put("statusValue", this.status.getVal());
		jm.put("statusText", this.status.getDesc());
		jm.put("comment", this.comment);
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}

}

package com.wireless.pojo.stockMgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.util.DateUtil;

public class StockTake implements Jsonable {

	/**
	 * The helper class to create the StockTake object to perform insert
	 */
	public static class InsertStockTakeBuilder{
		private final int restaurantId ;
		private CateType cateType ;
		private Department dept = new Department();
		private Status status = Status.CHECKING;
		private int parentId;
		private int operatorId;
		private String operator;
		private long startDate;
		private String comment;
		private List<StockTakeDetail> stockTakeDetails = new ArrayList<StockTakeDetail>();
		

		
		public InsertStockTakeBuilder(int restaurantId){
			this.restaurantId = restaurantId;
		}
		
		public static InsertStockTakeBuilder newChecking(int restaurantId){
			InsertStockTakeBuilder builder = new InsertStockTakeBuilder(restaurantId);
			builder.status = Status.CHECKING;
			return builder;
		} 
		public StockTake build(){
			return new StockTake(this);
		}

		public Department getDept() {
			return dept;
		}

		public InsertStockTakeBuilder setDept(Department dept) {
			this.dept = dept;
			return this;
		}
		
		public InsertStockTakeBuilder setDeptId(int id){
			this.dept.setId((short) id);
			return this;
		}

		public int getOperatorId() {
			return operatorId;
		}

		public InsertStockTakeBuilder setOperatorId(int operatorId) {
			this.operatorId = operatorId;
			return this;
		}

		public CateType getCateType() {
			return cateType;
		}

		public InsertStockTakeBuilder setCateType(CateType cateType) {
			this.cateType = cateType;
			return this;
		}
		
		public InsertStockTakeBuilder setCateType(int val){
			this.cateType = CateType.valueOf(val);
			return this;
		}

		public Status getStatus() {
			return status;
		}

		public InsertStockTakeBuilder setStatus(Status status) {
			this.status = status;
			return this;
		}
		
		public InsertStockTakeBuilder setStatus(int val){
			this.status = Status.valueOf(val);
			return this;
		}

		public int getParentId() {
			return parentId;
		}

		public InsertStockTakeBuilder setParentId(int parentId) {
			this.parentId = parentId;
			return this;
		}

		public String getOperator() {
			if(operator ==null){
				operator = "";
			}
			return operator;
		}

		public InsertStockTakeBuilder setOperator(String operator) {
			this.operator = operator;
			return this;
		}

		public long getStartTime() {
			return startDate;
		}

		public InsertStockTakeBuilder setStartDate(long startTime) {
			this.startDate = startTime;
			return this;
		}

		public String getComment() {
			if(comment == null){
				comment = "";
			}
			return comment;
		}

		public InsertStockTakeBuilder setComment(String comment) {
			this.comment = comment;
			return this;
		}

		public int getRestaurantId() {
			return restaurantId;
		}

		public List<StockTakeDetail> getStockTakeDetails() {
			return stockTakeDetails;
		}

		public InsertStockTakeBuilder setStockTakeDetails(List<StockTakeDetail> stockTakeDetails) {
			this.stockTakeDetails = stockTakeDetails;
			return this;
		}	
		
		public InsertStockTakeBuilder addStockTakeDetail(StockTakeDetail tDetail){
			this.stockTakeDetails.add(tDetail);
			return this;
		}
		
		
		
	}
	/**
	 * The helper class to create the StockTake object used in update
	 */
	public static class UpdateBuilder{
		private final int id;
		private Status status;
		private int approverId;
		private String approver;
		private long finishDate;
		
		
		public static UpdateBuilder newAudit(int id){
			UpdateBuilder updateBuilder = new UpdateBuilder(id);
			updateBuilder.setStatus(Status.AUDIT);
			return updateBuilder;
		}
		
		public Status getStatus() {
			return status;
		}
		public UpdateBuilder setStatus(Status status) {
				this.status = status;
				return this;
		}
		public UpdateBuilder setStatus(int val){
			this.status = Status.valueOf(val);
			return this;
			
		}
		
		public int getApproverId() {
			return approverId;
		}
		public UpdateBuilder setApproverId(int approverId) {
			this.approverId = approverId;
			return this;
		}
		public String getApprover() {
			
			return approver;
		}
		public UpdateBuilder setApprover(String approver) {
			if(approver == null){
				approver = "";
			}
			this.approver = approver;
			return this;
		}
		public long getFinishDate() {
			return finishDate;
		}
		public UpdateBuilder setFinishDate(long finishDate) {
			this.finishDate = finishDate;
			return this;
		}
		public int getId() {
			return id;
		}
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public StockTake build(){
			return new StockTake(this);
		}
		
		
	}
	
	/**
	 * 盘点状态
	 * 1-盘点中, 2-盘点完成, 3-审核通过
	 */
	public static enum Status{
		CHECKING(1, "盘点中"),
		CHECKED(2, "盘点完成"),
		AUDIT(3, "审核通过");
		
		private final int val;
		private final String text;
		
		
		public int getVal() {
			return val;
		}
		public String getText() {
			return text;
		}
		Status(int val, String text){
			this.val = val;
			this.text = text;
		}
		@Override
		public String toString(){
			return "status(" +
					"val = " + this.val +
					"text = " + this.text + ")";
		}
		public static Status valueOf(int val){
			for (Status status : values()) {
				if(status.val == val){
					return status;
				}
			}
			throw new IllegalArgumentException("the stockIn status(val = " + val + ") is invalid");
			
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
	
	private int id;
	private int restaurantId;
	private Department dept = new Department();
	private CateType cateType ;
	private Status status = Status.CHECKING;
	private int parentId;
	private int operatorId;
	private String operator;
	private int approverId;
	private String approver;
	private long startDate;
	private long finishDate;
	private String comment;
	private List<StockTakeDetail> stockTakeDetails = new ArrayList<StockTakeDetail>();
	
	
	
	
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

	public Department getDept() {
		return dept;
	}

	public void setDept(Department dept) {
		this.dept = dept;
	}
	public void setDeptId(int id){
		this.dept.setId((short) id);
	}
	public void setDeptName(String name){
		this.dept.setName(name);
	}
	
	public int getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(int operatorId) {
		this.operatorId = operatorId;
	}

	public int getApproverId() {
		return approverId;
	}

	public void setApproverId(int approverId) {
		this.approverId = approverId;
	}

	public String getApprover() {
		return approver;
	}

	public void setApprover(String approver) {
		this.approver = approver;
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

	public void setStatus(Status status) {
		this.status = status;
	}
	public void setStatus(int val){
		this.status = Status.valueOf(val);
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public long getStartDate() {
		return startDate;
	}

	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}

	public long getFinishDate() {
		return finishDate;
	}

	public void setFinishDate(long finishDate) {
		this.finishDate = finishDate;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	
	public List<StockTakeDetail> getStockTakeDetails() {
		return stockTakeDetails;
	}

	public void setStockTakeDetails(List<StockTakeDetail> stockTakeDetails) {
		this.stockTakeDetails = stockTakeDetails;
	}
	
	public void addStockTakeDetail(StockTakeDetail tDetail){
		this.stockTakeDetails.add(tDetail);
	}

	public StockTake(){}
	
	public StockTake(InsertStockTakeBuilder builder){
		setRestaurantId(builder.getRestaurantId());
		setDept(builder.getDept());
		setCateType(builder.getCateType());
		setStatus(builder.getStatus());
		setParentId(builder.getParentId());
		setOperatorId(builder.getOperatorId());
		setOperator(builder.getOperator());
		setStartDate(builder.getStartTime());
		setComment(builder.getComment());
		setStockTakeDetails(builder.getStockTakeDetails());
	}
	
	public StockTake(UpdateBuilder builder){
		setId(builder.getId());
		setApproverId(builder.getApproverId());
		setApprover(builder.getApprover());
		setFinishDate(builder.getFinishDate());
	}
	@Override
	public String toString(){
		return "stockTake : id = " + id +
				"restaurantId = " + restaurantId +
				"deptId = " + dept.getId() +
				"cateType = " + cateType.value +
				"status = " + status.getText() +
				"operator = " + getOperator() +
				"startDate = " + DateUtil.format(startDate) +
				"finishDate = " + DateUtil.format(finishDate) +
				"comment = " + comment;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof StockTake)){
			return false;
		}else{
			return id == ((StockTake)obj).id
					&& restaurantId == ((StockTake)obj).restaurantId;
		}
	}
	
	@Override
	public int hashCode(){
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
		jm.put("deptId", this.getDept().getId());
		jm.put("deptName", this.getDept().getName());
		jm.put("cateType", this.getCateType().value);
		jm.put("status", this.getStatus().getText());
		jm.put("parentId", this.getParentId());
		jm.put("operatorId", this.getOperatorId());
		jm.put("operator", this.getOperator());
		jm.put("startDate", DateUtil.format(this.getStartDate()));
		jm.put("approverId", this.getApproverId());
		jm.put("approver", this.getApprover());
		jm.put("finishDate", DateUtil.format(this.getFinishDate()));
		jm.put("comment", this.getComment());
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		// TODO Auto-generated method stub
		return null;
	}

	
}

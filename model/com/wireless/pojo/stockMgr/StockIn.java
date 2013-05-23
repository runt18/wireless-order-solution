package com.wireless.pojo.stockMgr;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class StockIn {

	private int id;
	private int restaurantId;
	private String oriStockId;
	private int approverId;
	private String approver;
	private Date approverDate;
	private int deptIn;
	private int deptOut;
	private int operatorId;
	private String operator;
	private Date operateDate;
	private Float amount;
	private Float price;
	private int type;
	private int status;
	private String comment;
	private List<StockInDetail> sDetails = new ArrayList<StockInDetail>();

	public List<StockInDetail> getsDetails() {
		return sDetails;
	}

	public void setsDetails(List<StockInDetail> sDetails) {
		if (sDetails != null) {
			this.sDetails = sDetails;
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

	public Date getApproverDate() {
		return approverDate;
	}

	public void setApproverDate(Date approverDate) {
		this.approverDate = approverDate;
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

	public Date getOperateDate() {
		return operateDate;
	}

	public void setOperateDate(Date operateDate) {
		this.operateDate = operateDate;
	}

	public Float getAmount() {
		return amount;
	}

	public void setAmount(Float amount) {
		this.amount = amount;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
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

	public StockIn(int id, int restaurantId, String oriStockId, int approverId,
			String approver, Date approverDate, int deptIn, int deptOut,
			int operatorId, String operator, Date operateDate, Float amount,
			Float price, int type, int status, String comment) {
		this.id = id;
		this.restaurantId = restaurantId;
		this.oriStockId = oriStockId;
		this.approverId = approverId;
		this.approver = approver;
		this.approverDate = approverDate;
		this.deptIn = deptIn;
		this.deptOut = deptOut;
		this.operatorId = operatorId;
		this.operator = operator;
		this.operateDate = operateDate;
		this.amount = amount;
		this.price = price;
		this.type = type;
		this.status = status;
		this.comment = comment;
	}

}

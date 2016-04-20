package com.wireless.pojo.stockMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class StockDetailReport implements Jsonable {

	private StockAction stockAction;
	private StockActionDetail stockActionDetail;
	
	private boolean isSummary;
	private float totalStockInAmount;
	private float totalStockInMoney;
	private float totalStockOutAmount;
	private float totalStockOutMoney;
	
	public StockDetailReport(StockAction stockAction, StockActionDetail stockActionDetail){
		this.stockAction = stockAction;
		this.stockActionDetail = stockActionDetail;
	}
	
	public StockDetailReport(){
		
	}
	
	public void setStockAction(StockAction action){
		this.stockAction = action;
	}
	
	public void setStockActonDetail(StockActionDetail detail){
		this.stockActionDetail = detail;
	}
	
	public StockActionDetail getStockActionDetail(){
		return this.stockActionDetail;
	}
	
	public StockAction getStockAction(){
		return this.stockAction;
	}
	
	public void setSummary(boolean onOff){
		this.isSummary = onOff;
	}
	
	public void setTotalStockInAmount(float totalStockInAmount) {
		this.totalStockInAmount = totalStockInAmount;
	}

	public void setTotalStockInMoney(float totalStockInMoney) {
		this.totalStockInMoney = totalStockInMoney;
	}

	public void setTotalStockOutAmount(float totalStockOutAmount) {
		this.totalStockOutAmount = totalStockOutAmount;
	}

	public void setTotalStockOutMoney(float totalStockOutMoney) {
		this.totalStockOutMoney = totalStockOutMoney;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		
		JsonMap jm = new JsonMap();
		
		if(this.isSummary){
			//汇总数据
			jm.putString("dept", "----");
			jm.putString("stockInSubType", "----");
			jm.putFloat("stockInAmount", this.totalStockInAmount);
			jm.putFloat("stockInMoney", this.totalStockInMoney);
			jm.putString("stockOutSubType", "----");
			jm.putFloat("stockOutAmount", this.totalStockOutAmount);
			jm.putFloat("stockOutMoney", this.totalStockOutMoney);
			jm.putString("remaining", "--");
		}else{
			jm.putString("date", DateUtil.formatToDate(this.stockAction.getOriStockDate()));
			jm.putString("oriStockId", this.stockAction.getOriStockId());
			jm.putString("materialName", this.stockActionDetail.getName());
			jm.putString("supplier", this.stockAction.getSupplier().getName());
			
			if(this.stockAction.getType() == StockAction.Type.STOCK_IN){
				
				jm.putString("deptIn",	(!this.stockAction.getDeptIn().getName().isEmpty() && this.stockAction.getDeptIn().getName() != null ? this.stockAction.getDeptIn().getName() : "----"));
				jm.putString("deptOut",	(!this.stockAction.getDeptOut().getName().isEmpty() && this.stockAction.getDeptOut().getName() != null ? this.stockAction.getDeptOut().getName() : "----"));
				jm.putString("stockInSubType", this.stockAction.getSubType().getText());
				jm.putFloat("stockInAmount", this.stockActionDetail.getAmount());
				jm.putFloat("stockInMoney", this.stockActionDetail.getAmount() * this.stockActionDetail.getPrice());
				jm.putString("stockOutSubType", "----");
				jm.putString("stockOutAmount", "----");
				jm.putString("stockOutMoney", "----");
				
			}else if(this.stockAction.getType() == StockAction.Type.STOCK_OUT){
				jm.putString("deptIn",	(!this.stockAction.getDeptIn().getName().isEmpty() && this.stockAction.getDeptIn().getName() != null ? this.stockAction.getDeptIn().getName() : "----"));
				jm.putString("deptOut", (!this.stockAction.getDeptOut().getName().isEmpty() && this.stockAction.getDeptOut().getName() != null ? this.stockAction.getDeptOut().getName() : "----"));
				jm.putString("stockInSubType", "----");
				jm.putString("stockInAmount", "----");
				jm.putString("stockInMoney", "----");
				jm.putString("stockOutSubType", this.stockAction.getSubType() != null ? this.stockAction.getSubType().getText() : "---");
				jm.putFloat("stockOutAmount", this.stockActionDetail.getAmount());
				jm.putFloat("stockOutMoney", this.stockActionDetail.getAmount() * this.stockActionDetail.getPrice());
				
			}else{

				jm.putString("deptOut",	(!this.stockAction.getDeptOut().getName().isEmpty() && this.stockAction.getDeptOut().getName() != null ? this.stockAction.getDeptOut().getName() : "----"));
				jm.putString("deptIn",	(!this.stockAction.getDeptIn().getName().isEmpty() && this.stockAction.getDeptIn().getName() != null ? this.stockAction.getDeptIn().getName() : "----"));
				jm.putString("stockInSubType", "----");
				jm.putString("stockInAmount", "----");
				jm.putString("stockInMoney", "----");
				jm.putString("stockOutSubType", this.stockAction.getSubType() != null ? this.stockAction.getSubType().getText() : "---");
				jm.putFloat("stockOutAmount", this.stockActionDetail.getAmount());
				jm.putFloat("stockOutMoney", this.stockActionDetail.getAmount() * this.stockActionDetail.getPrice());
				
			}

			jm.putFloat("remaining", this.stockActionDetail.getRemaining());
			jm.putString("operater", this.stockAction.getApprover());
			
		}
		
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
}

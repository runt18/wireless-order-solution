package com.wireless.pojo.stockMgr;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.supplierMgr.Supplier;
import com.wireless.pojo.util.DateUtil;

public class StockAction implements Jsonable{

	/**
	 * The helper class to create the StockIn object to perform insert
	 */
	public static class InsertBuilder{
		private int restaurantId;
		private String oriStockId;
		private long oriStockIdDate;
		private Supplier supplier = new Supplier();
		private Department deptIn = new Department(0);
		private Department deptOut = new Department(0);
		private int operatorId;
		private String operator;
		private List<StockActionDetail> stockActionDetails = new ArrayList<StockActionDetail>(); 
		private String comment;
		private float actualPrice;
		private int stockInRestaurantId;
		private int stockOutRestaurantId;
		
		private MaterialCate.Type cateType;
		private Status status = Status.UNAUDIT;
		private Type type;
		private SubType subType;
		
		private InsertBuilder(){}
		private InsertBuilder(int restaurantId){
			this.restaurantId = restaurantId;
		}
		//入库采购
		public static InsertBuilder newStockIn(int restaurantId, long oriStockIdDate, float actualPrice){
			InsertBuilder builder = new InsertBuilder(restaurantId);
			builder.setType(Type.STOCK_IN)
					.setSubType(SubType.STOCK_IN)
					.setOriStockDate(oriStockIdDate)
					.setDeptOut((short) -1);
			
			builder.actualPrice = actualPrice;
			return builder;
		}
		//初始化库存
		public static InsertBuilder stockInit(int restaurantId, long oriStockIdDate){
			InsertBuilder builder = new InsertBuilder(restaurantId);
			builder.setType(Type.STOCK_IN)
					.setSubType(SubType.INIT)
					.setOriStockDate(oriStockIdDate)
					.setDeptOut((short) -1);
			
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
			builder.setType(Type.STOCK_IN).setSubType(SubType.SPILL).setDeptOut((short) -1);
			return builder;
		}
		//退货
		public static InsertBuilder newStockOut(int restaurantId, long oriStockIdDate, float actualPrice){
			InsertBuilder builder = new InsertBuilder(restaurantId);
			builder.setType(Type.STOCK_OUT).setSubType(SubType.STOCK_OUT).setOriStockDate(oriStockIdDate).setDeptIn((short) -1);
			builder.actualPrice = actualPrice;
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
			builder.setType(Type.STOCK_OUT).setSubType(SubType.DAMAGE).setDeptIn((short) -1);
			return builder;
		}
		//盘盈
		public static InsertBuilder newMore(int restaurantId){
			InsertBuilder builder = new InsertBuilder(restaurantId);
			builder.setType(Type.STOCK_IN).setSubType(SubType.MORE).setDeptOut((short) -1).setStatus(Status.AUDIT);
			return builder;
		}
		//盘亏
		public static InsertBuilder newLess(int restaurantId){
			InsertBuilder builder = new InsertBuilder(restaurantId);
			builder.setType(Type.STOCK_OUT).setSubType(SubType.LESS).setDeptIn((short) -1).setStatus(Status.AUDIT);
			return builder;
		}
		
		//消耗
		public static InsertBuilder newUseUp(int restaurantId, Department deptOut, MaterialCate.Type cateType){
			InsertBuilder builder = new InsertBuilder(restaurantId);
			builder.setType(Type.STOCK_OUT).setSubType(SubType.CONSUMPTION)
			       .setDeptOut(deptOut.getId()).setDeptOutName(deptOut.getName())
			       .setCateType(cateType)
			       .setOriStockDate(new Date().getTime())
			       .setDeptIn((short) -1);

			return builder;
		}
		
		//配送申请
		public static InsertBuilder newDistributionApply(){
			InsertBuilder builder = new InsertBuilder().setType(Type.STOCK_APPLY)
													   .setSubType(SubType.DISTRIBUTION_APPLY);
			return builder;
		}
		
		//配送发货
		public static InsertBuilder newDistributionSend(){
			InsertBuilder builder = new InsertBuilder().setType(Type.STOCK_OUT)
													   .setSubType(SubType.DISTRIBUTION_SEND);
			return builder;
		}
		//配送收货
		public static InsertBuilder newDistributionReceive(){
			InsertBuilder builder = new InsertBuilder().setType(Type.STOCK_IN)
													   .setSubType(SubType.DISTRIBUTION_RECEIVE);
			return builder;
		}
		//配送退货
		public static InsertBuilder newDistributionReturn(){
			InsertBuilder builder = new InsertBuilder().setType(Type.STOCK_OUT)
													   .setSubType(SubType.DISTRIBUTION_RETURN);
			return builder;
		}
		//配送回收
		public static InsertBuilder newDistributionRecovery(){
			InsertBuilder builder = new InsertBuilder().setType(Type.STOCK_IN)
													   .setSubType(SubType.DISTRIBUTION_RECOVERY);
			return builder;
		}
		
		public StockAction build(){
			return new StockAction(this);
		}

		public int getStockInRestaurantId() {
			return stockInRestaurantId;
		}
		
		public InsertBuilder setStockInRestaurantId(int stockInRestaurantId) {
			this.stockInRestaurantId = stockInRestaurantId;
			return this;
		}
		
		public int getStockOutRestaurantId() {
			return stockOutRestaurantId;
		}
		
		public InsertBuilder setStockOutRestaurantId(int stockOutRestaurantId) {
			this.stockOutRestaurantId = stockOutRestaurantId;
			return this;
		}
		
		public InsertBuilder setOriStockId(String oriStockId){
			this.oriStockId = oriStockId;
			return this;
		}

		private float getActualPrice() {
			return actualPrice;
		}

		public InsertBuilder setOriStockDate(long oriStockIdDate) {
			this.oriStockIdDate = oriStockIdDate;
			return this;
		}

		public InsertBuilder addDetail(StockActionDetail detail){
			this.stockActionDetails.add(detail);
			return this;
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

		public void setSupplier(Supplier supplier) {
			this.supplier = supplier;
		}
		
		public InsertBuilder setSupplierId(int supplierId){
			this.supplier.setId(supplierId);
			return this;
		}
		
		public InsertBuilder setSupplierName(String name){
			this.supplier.setName(name);
			return this;
		}
		
		public InsertBuilder setOperatorId(int operatorId) {
			this.operatorId = operatorId;
			return this;
		}

		private String getOperator() {
			if(operator == null){
				operator = "";
			}
			return operator;
		}

		public InsertBuilder setOperator(String operator) {
			this.operator = operator;
			return this;
		}

		public InsertBuilder setComment(String comment) {
			this.comment = comment;
			return this;
		}

		public void setStatus(Status status) {
			this.status = status;
		}

		private InsertBuilder setType(Type type) {
			this.type = type;
			return this;
		}
		
		private InsertBuilder setSubType(SubType subType) {
			this.subType = subType;
			return this;
		}
		
		public InsertBuilder setCateType(MaterialCate.Type cateType) {
			this.cateType = cateType;
			return this;
		}
		
		public InsertBuilder setCateType(int val){
			this.cateType = MaterialCate.Type.valueOf(val);
			return this;
		}
		
		public InsertBuilder setInitActualPrice(float actualPrice){
			this.actualPrice = actualPrice;
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
					"stockInDetails =" + stockActionDetails +
					"operator=" + getOperator() + 
					"status" + status;
		}
		

	
	}
	
	
	public static class UpdateBuilder{
		private int id;
		private String oriStockId;
		private long oriStockDate;
		private Supplier supplier = new Supplier();
		private Department deptIn = new Department(0);
		private Department deptOut = new Department(0);
		private int operatorId;
		private String operator;
		private List<StockActionDetail> stockActionDetails = new ArrayList<StockActionDetail>(); 
		private String comment;
		private float actualPrice;
		private MaterialCate.Type cateType ;
		private Status status = Status.UNAUDIT;
		private Type type;
		private SubType subType;
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		//入库采购
		public static UpdateBuilder newStockIn(int id, long oriStockIdDate, float actualPrice){
			UpdateBuilder builder = new UpdateBuilder(id);
			builder.setType(Type.STOCK_IN)
					.setSubType(SubType.STOCK_IN)
					.setOriStockDate(oriStockIdDate)
					.setDeptOut((short) -1);
			
			builder.actualPrice = actualPrice;
			return builder;
		}
		//初始化库存
		public static UpdateBuilder stockInit(int id, long oriStockIdDate){
			UpdateBuilder builder = new UpdateBuilder(id);
			builder.setType(Type.STOCK_IN)
					.setSubType(SubType.INIT)
					.setOriStockDate(oriStockIdDate)
					.setDeptOut((short) -1);
			
			return builder;
		}
		//入库调拨
		public static UpdateBuilder newStockInTransfer(int id){
			UpdateBuilder builder = new UpdateBuilder(id);
			builder.setType(Type.STOCK_IN).setSubType(SubType.STOCK_IN_TRANSFER);
			return builder;
		}
		//报溢
		public static UpdateBuilder newSpill(int id){
			UpdateBuilder builder = new UpdateBuilder(id);
			builder.setType(Type.STOCK_IN).setSubType(SubType.SPILL).setDeptOut((short) -1);
			return builder;
		}
		//退货
		public static UpdateBuilder newStockOut(int id, long oriStockIdDate, float actualPrice){
			UpdateBuilder builder = new UpdateBuilder(id);
			builder.setType(Type.STOCK_OUT).setSubType(SubType.STOCK_OUT).setOriStockDate(oriStockIdDate).setDeptIn((short) -1);
			builder.actualPrice = actualPrice;
			return builder;
		}
		//出库调拨
		public static UpdateBuilder newStockOutTransfer(int id){
			UpdateBuilder builder = new UpdateBuilder(id);
			builder.setType(Type.STOCK_OUT).setSubType(SubType.STOCK_OUT_TRANSFER);
			return builder;
		}
		//报损
		public static UpdateBuilder newDamage(int id){
			UpdateBuilder builder = new UpdateBuilder(id);
			builder.setType(Type.STOCK_OUT).setSubType(SubType.DAMAGE).setDeptIn((short) -1);
			return builder;
		}
		//盘盈
		public static UpdateBuilder newMore(int id){
			UpdateBuilder builder = new UpdateBuilder(id);
			builder.setType(Type.STOCK_IN).setSubType(SubType.MORE).setDeptOut((short) -1).setStatus(Status.AUDIT);
			return builder;
		}
		//盘亏
		public static UpdateBuilder newLess(int id){
			UpdateBuilder builder = new UpdateBuilder(id);
			builder.setType(Type.STOCK_OUT).setSubType(SubType.LESS).setDeptIn((short) -1).setStatus(Status.AUDIT);
			return builder;
		}
		//消耗
		public static UpdateBuilder newUseUp(int id, Department deptOut, MaterialCate.Type cateType){
			UpdateBuilder builder = new UpdateBuilder(id);
			builder.setType(Type.STOCK_OUT).setSubType(SubType.CONSUMPTION)
			       .setDeptOut(deptOut.getId()).setDeptOutName(deptOut.getName())
			       .setCateType(cateType)
			       .setOriStockDate(new Date().getTime())
			       .setDeptIn((short) -1);

			return builder;
		}
		
		//配送申请
		public static UpdateBuilder newDistributionApply(int id){
			UpdateBuilder builder = new UpdateBuilder(id).setType(Type.STOCK_APPLY)
														 .setSubType(SubType.DISTRIBUTION_APPLY);
			return builder;
		}
		//配送发货
		public static UpdateBuilder newDistributionSend(int id){
			UpdateBuilder builder = new UpdateBuilder(id).setType(Type.STOCK_OUT)
													   .setSubType(SubType.DISTRIBUTION_SEND);
			return builder;
		}
		//配送收货
		public static UpdateBuilder newDistributionReceive(int id){
			UpdateBuilder builder = new UpdateBuilder(id).setType(Type.STOCK_IN)
													   .setSubType(SubType.DISTRIBUTION_RECEIVE);
			return builder;
		}
		//配送退货
		public static UpdateBuilder newDistributionReturn(int id){
			UpdateBuilder builder = new UpdateBuilder(id).setType(Type.STOCK_OUT)
													   .setSubType(SubType.DISTRIBUTION_RETURN);
			return builder;
		}
		//配送回收
		public static UpdateBuilder newDistributionRecovery(int id){
			UpdateBuilder builder = new UpdateBuilder(id).setType(Type.STOCK_IN)
													   .setSubType(SubType.DISTRIBUTION_RECOVERY);
			return builder;
		}
		
		public StockAction build(){
			return new StockAction(this);
		}
		
		public UpdateBuilder setOriStockId(String oriStockId){
			this.oriStockId = oriStockId;
			return this;
		}
		
		public boolean isOriStockIdChange(){
			return this.oriStockId != null;
		}
		
		public boolean isStockActionDetailsChange(){
			return this.stockActionDetails != null;
		}
		
		private float getActualPrice() {
			return actualPrice;
		}
		
		public boolean isActutalPriceChange(){
			return this.actualPrice != 0;
		}
		
		public boolean isOriStockDateChange(){
			return this.oriStockDate != 0;
		}

		public UpdateBuilder setOriStockDate(long oriStockDate) {
			this.oriStockDate = oriStockDate;
			return this;
		}

		public UpdateBuilder addDetail(StockActionDetail detail){
			this.stockActionDetails.add(detail);
			return this;
		}
		
		public UpdateBuilder setDeptIn(short i){
			this.deptIn.setId(i);
			return this;
		}
		
		public boolean isDeptInChange(){
			return this.deptIn != null;
		}

		
		public UpdateBuilder setDeptIn(Department deptIn) {
			this.deptIn = deptIn;
			return this;
			
		}
		
		public UpdateBuilder setDeptInName(String name){
			this.deptIn.setName(name);
			return this;
		}

		public UpdateBuilder setDeptOut(short deptOut){
			this.deptOut.setId(deptOut);
			return this;
		}
		
		public boolean isDeptOutChange(){
			return this.deptOut != null;
		}
		
		public UpdateBuilder setDeptOutName(String name){
			this.deptOut.setName(name);
			return this;
		}

		public UpdateBuilder setDeptOut(Department deptOut) {
			this.deptOut = deptOut;
			return this;
		}	

		public void setSupplier(Supplier supplier) {
			this.supplier = supplier;
		}
		
		public boolean isSupplierChange(){
			return this.supplier != null;
		}
		
		public UpdateBuilder setSupplierId(int supplierId){
			this.supplier.setId(supplierId);
			return this;
		}
		
		public UpdateBuilder setSupplierName(String name){
			this.supplier.setName(name);
			return this;
		}

		public UpdateBuilder setOperatorId(int operatorId) {
			this.operatorId = operatorId;
			return this;
		}
		
		public boolean isOperatorIdChange(){
			return this.operatorId != 0;
		}
		
		public boolean isOperateChange(){
			return this.operator != null;
		}

		public UpdateBuilder setOperator(String operator) {
			this.operator = operator;
			return this;
		}

		public boolean isCommentChange(){
			return this.comment != null;
		}
		
		public UpdateBuilder setComment(String comment) {
			this.comment = comment;
			return this;
		}
		
		public Status getStatus() {
			return status;
		}

		public UpdateBuilder setStatus(Status status) {
			this.status = status;
			return this;
		}
		
		public boolean isStatusChange(){
			return this.status != null;
		}
		
		public Type getType() {
			return type;
		}

		private UpdateBuilder setType(Type type) {
			this.type = type;
			return this;
		}

		public boolean isSubTypeChange(){
			return this.subType != null;
		}

		private UpdateBuilder setSubType(SubType subType) {
			this.subType = subType;
			return this;
		}
		
		public UpdateBuilder setCateType(MaterialCate.Type cateType) {
			this.cateType = cateType;
			return this;
		}
		
		public boolean isCateTypeChange(){
			return this.cateType != null;
		}
		
		public UpdateBuilder setCateType(int val){
			this.cateType = MaterialCate.Type.valueOf(val);
			return this;
		}
		
		public UpdateBuilder setInitActualPrice(float actualPrice){
			this.actualPrice = actualPrice;
			return this;
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
		private long approverDate;
		
		public StockAction build(){
			return new StockAction(this);
		}
		
		private AuditBuilder(int id){
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

		public long getApproverDate() {
			return approverDate;
		}

		public AuditBuilder setStockInitApproverDate() {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MONTH, -1);
			
			String date = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.getActualMaximum(Calendar.DAY_OF_MONTH);
			this.approverDate = DateUtil.parseDate(date);
			return this;
		}
		
		

	}
	
	public static class ReAuditBuilder{

		private final int id;
		private String oriStockId;
		private long oriStockDate;
		private Supplier supplier = new Supplier();
		private Department deptIn = new Department(0);
		private Department deptOut = new Department(0);
		private int operatorId;
		private String operator;
		private List<StockActionDetail> stockActionDetails = new ArrayList<StockActionDetail>(); 
		private String comment;
		private float actualPrice;
		private MaterialCate.Type cateType ;
		private Status status = Status.UNAUDIT;
		private Type type;
		private SubType subType;
		
		public ReAuditBuilder(int id){
			this.id = id;
		}
		//入库采购
		public static ReAuditBuilder newStockIn(int id, long oriStockIdDate, float actualPrice){
			ReAuditBuilder builder = new ReAuditBuilder(id);
			builder.setType(Type.STOCK_IN)
					.setSubType(SubType.STOCK_IN)
					.setOriStockDate(oriStockIdDate)
					.setDeptOut((short) -1);
			
			builder.actualPrice = actualPrice;
			return builder;
		}
		//初始化库存
		public static ReAuditBuilder stockInit(int id, long oriStockIdDate){
			ReAuditBuilder builder = new ReAuditBuilder(id);
			builder.setType(Type.STOCK_IN)
					.setSubType(SubType.INIT)
					.setOriStockDate(oriStockIdDate)
					.setDeptOut((short) -1);
			
			return builder;
		}
		//入库调拨
		public static ReAuditBuilder newStockInTransfer(int id){
			ReAuditBuilder builder = new ReAuditBuilder(id);
			builder.setType(Type.STOCK_IN).setSubType(SubType.STOCK_IN_TRANSFER);
			return builder;
		}
		//报溢
		public static ReAuditBuilder newSpill(int id){
			ReAuditBuilder builder = new ReAuditBuilder(id);
			builder.setType(Type.STOCK_IN).setSubType(SubType.SPILL).setDeptOut((short) -1);
			return builder;
		}
		//退货
		public static ReAuditBuilder newStockOut(int id, long oriStockIdDate, float actualPrice){
			ReAuditBuilder builder = new ReAuditBuilder(id);
			builder.setType(Type.STOCK_OUT).setSubType(SubType.STOCK_OUT).setOriStockDate(oriStockIdDate).setDeptIn((short) -1);
			builder.actualPrice = actualPrice;
			return builder;
		}
		//出库调拨
		public static ReAuditBuilder newStockOutTransfer(int id){
			ReAuditBuilder builder = new ReAuditBuilder(id);
			builder.setType(Type.STOCK_OUT).setSubType(SubType.STOCK_OUT_TRANSFER);
			return builder;
		}
		//报损
		public static ReAuditBuilder newDamage(int id){
			ReAuditBuilder builder = new ReAuditBuilder(id);
			builder.setType(Type.STOCK_OUT).setSubType(SubType.DAMAGE).setDeptIn((short) -1);
			return builder;
		}
		//盘盈
		public static ReAuditBuilder newMore(int id){
			ReAuditBuilder builder = new ReAuditBuilder(id);
			builder.setType(Type.STOCK_IN).setSubType(SubType.MORE).setDeptOut((short) -1).setStatus(Status.AUDIT);
			return builder;
		}
		//盘亏
		public static ReAuditBuilder newLess(int id){
			ReAuditBuilder builder = new ReAuditBuilder(id);
			builder.setType(Type.STOCK_OUT).setSubType(SubType.LESS).setDeptIn((short) -1).setStatus(Status.AUDIT);
			return builder;
		}
		//消耗
		public static ReAuditBuilder newUseUp(int id, Department deptOut, MaterialCate.Type cateType){
			ReAuditBuilder builder = new ReAuditBuilder(id);
			builder.setType(Type.STOCK_OUT).setSubType(SubType.CONSUMPTION)
			       .setDeptOut(deptOut.getId()).setDeptOutName(deptOut.getName())
			       .setCateType(cateType)
			       .setOriStockDate(new Date().getTime())
			       .setDeptIn((short) -1);

			return builder;
		}
		
		//配送申请
		public static ReAuditBuilder newDistributionApply(int id){
			ReAuditBuilder builder = new ReAuditBuilder(id).setType(Type.STOCK_APPLY)
														   .setSubType(SubType.DISTRIBUTION_APPLY);
			return builder;
		}
		
		//配送发货
		public static ReAuditBuilder newDistributionSend(int id){
			ReAuditBuilder builder = new ReAuditBuilder(id).setType(Type.STOCK_OUT)
													   .setSubType(SubType.DISTRIBUTION_SEND);
			return builder;
		}
		//配送收货
		public static ReAuditBuilder newDistributionReceive(int id){
			ReAuditBuilder builder = new ReAuditBuilder(id).setType(Type.STOCK_IN)
													   .setSubType(SubType.DISTRIBUTION_RECEIVE);
			return builder;
		}
		//配送退货
		public static ReAuditBuilder newDistributionReturn(int id){
			ReAuditBuilder builder = new ReAuditBuilder(id).setType(Type.STOCK_OUT)
													   .setSubType(SubType.DISTRIBUTION_RETURN);
			return builder;
		}
		//配送回收
		public static ReAuditBuilder newDistributionRecovery(int id){
			ReAuditBuilder builder = new ReAuditBuilder(id).setType(Type.STOCK_IN)
													   .setSubType(SubType.DISTRIBUTION_RECOVERY);
			return builder;
		}
		
		public StockAction build(){
			return new StockAction(this);
		}
		
		public ReAuditBuilder setOriStockId(String oriStockId){
			this.oriStockId = oriStockId;
			return this;
		}
		
		public boolean isOriStockIdChange(){
			return this.oriStockId != null;
		}
		
		public boolean isStockActionDetailsChange(){
			return this.stockActionDetails != null;
		}
		
		public boolean isActutalPriceChange(){
			return this.actualPrice != 0;
		}
		
		public boolean isOriStockDateChange(){
			return this.oriStockDate != 0;
		}

		public ReAuditBuilder setOriStockDate(long oriStockDate) {
			this.oriStockDate = oriStockDate;
			return this;
		}

		public ReAuditBuilder addDetail(StockActionDetail detail){
			this.stockActionDetails.add(detail);
			return this;
		}
		
		public ReAuditBuilder setDeptIn(short i){
			this.deptIn.setId(i);
			return this;
		}
		
		public boolean isDeptInChange(){
			return this.deptIn != null;
		}

		
		public ReAuditBuilder setDeptIn(Department deptIn) {
			this.deptIn = deptIn;
			return this;
			
		}
		
		public ReAuditBuilder setDeptInName(String name){
			this.deptIn.setName(name);
			return this;
		}

		public ReAuditBuilder setDeptOut(short deptOut){
			this.deptOut.setId(deptOut);
			return this;
		}
		
		public boolean isDeptOutChange(){
			return this.deptOut != null;
		}
		
		public ReAuditBuilder setDeptOutName(String name){
			this.deptOut.setName(name);
			return this;
		}

		public ReAuditBuilder setDeptOut(Department deptOut) {
			this.deptOut = deptOut;
			return this;
		}	

		public ReAuditBuilder setSupplier(Supplier supplier) {
			this.supplier = supplier;
			return this;
		}
		
		public boolean isSupplierChange(){
			return this.supplier != null;
		}
		
		public ReAuditBuilder setSupplierId(int supplierId){
			this.supplier.setId(supplierId);
			return this;
		}
		
		public ReAuditBuilder setSupplierName(String name){
			this.supplier.setName(name);
			return this;
		}

		public ReAuditBuilder setOperatorId(int operatorId) {
			this.operatorId = operatorId;
			return this;
		}
		
		public boolean isOperatorIdChange(){
			return this.operatorId != 0;
		}
		
		public boolean isOperateChange(){
			return this.operator != null;
		}

		public ReAuditBuilder setOperator(String operator) {
			this.operator = operator;
			return this;
		}

		public boolean isCommentChange(){
			return this.comment != null;
		}
		
		public ReAuditBuilder setComment(String comment) {
			this.comment = comment;
			return this;
		}

		public ReAuditBuilder setStatus(Status status) {
			this.status = status;
			return this;
		}
		
		public boolean isStatusChange(){
			return this.status != null;
		}

		private ReAuditBuilder setType(Type type) {
			this.type = type;
			return this;
		}

		public boolean isSubTypeChange(){
			return this.subType != null;
		}

		private ReAuditBuilder setSubType(SubType subType) {
			this.subType = subType;
			return this;
		}
		
		public ReAuditBuilder setCateType(MaterialCate.Type cateType) {
			this.cateType = cateType;
			return this;
		}
		
		public boolean isCateTypeChange(){
			return this.cateType != null;
		}
		
		public ReAuditBuilder setCateType(int val){
			this.cateType = MaterialCate.Type.valueOf(val);
			return this;
		}
		
		public ReAuditBuilder setInitActualPrice(float actualPrice){
			this.actualPrice = actualPrice;
			return this;
		}
	
	}
	
	/**
	 * 库单状态
	 * 1 - 未审核，2 - 审核通过， 3 - 反审核，4 - 审核通过
	 */
	public static enum Status{
		UNAUDIT(1, "未审核"), 
		AUDIT(2, "审核通过"),
		RE_AUDIT(3, "反审核"),
		FINAL(4, "审核通过");
		
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
		STOCK_OUT(2, "出库"),
		STOCK_APPLY(3, "申请");
		
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
		STOCK_IN_TRANSFER(2, "领料"),
		SPILL(3, "其他入库"),
		STOCK_OUT(4, "退货"),
		STOCK_OUT_TRANSFER(5, "退料"),
		DAMAGE(6, "其他出库"),
		MORE(7, "盘盈"),
		LESS(8, "盘亏"),
		CONSUMPTION(9, "消耗"),
		INIT(10, "初始化"),
		DISTRIBUTION_SEND(11, "配送发货"),
		DISTRIBUTION_RECEIVE(12, "配送收货"),
		DISTRIBUTION_RETURN(13, "配送退货"),
		DISTRIBUTION_RECOVERY(14, "配送回收"),
		DISTRIBUTION_APPLY(15, "配送申请");
			
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
			throw new IllegalArgumentException("the stockIn subType(val = " + val + ") is invalid");
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
	private long oriStockDate;
	private int approverId;
	private String approver;
	private long approverDate;
	private Department deptIn = new Department(0);
	private Department deptOut = new Department(0);
	private Supplier supplier = new Supplier();
	private int operatorId;
	private String operator;
	private float amount;
	private float price;
	private float actualPrice;
	private MaterialCate.Type cateType;
	private SubType subType;
	private Type type;
	private Status status = Status.UNAUDIT;
	private String comment;
	private List<StockActionDetail> stockDetails = new ArrayList<StockActionDetail>();
	private Restaurant stockInRestaurant;
	private int stockInRestaurantId;
	private Restaurant stockOutRestaurant;
	private int stockOutRestaurantId;

	public int getStockInRestaurantId() {
		return stockInRestaurantId;
	}

	public void setStockInRestaurantId(int stockInRestaurantId) {
		this.stockInRestaurantId = stockInRestaurantId;
	}

	public int getStockOutRestaurantId() {
		return stockOutRestaurantId;
	}

	public void setStockOutRestaurantId(int stockOutRestaurantId) {
		this.stockOutRestaurantId = stockOutRestaurantId;
	}

	public Restaurant getStockInRestaurant() {
		return stockInRestaurant;
	}

	public void setStockInRestaurant(Restaurant stockInRestaurant) {
		this.stockInRestaurant = stockInRestaurant;
	}

	public Restaurant getStockOutRestaurant() {
		return stockOutRestaurant;
	}

	public void setStockOutRestaurant(Restaurant stockOutRestaurant) {
		this.stockOutRestaurant = stockOutRestaurant;
	}

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

	
	public float getActualPrice() {
		return actualPrice;
	}

	public void setActualPrice(float actualPrice) {
		this.actualPrice = actualPrice;
	}

	public List<StockActionDetail> getStockDetails() {
		return stockDetails;
	}

	public void setDetails(List<StockActionDetail> stockDetails) {
		if(stockDetails != null){
			this.stockDetails.clear();
			this.stockDetails.addAll(stockDetails);
		}
		
	}
	
	public void addDetail(StockActionDetail detail){
		this.stockDetails.add(detail);
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

	public long getOriStockDate() {
		return oriStockDate;
	}

	public void setOriStockDate(long oriStockDate) {
		this.oriStockDate = oriStockDate;
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

	
	public MaterialCate.Type getCateType() {
		return cateType;
	}

	public void setCateType(MaterialCate.Type cateType) {
		this.cateType = cateType;
	}
	
	public void setCateType(int val){
		this.cateType = MaterialCate.Type.valueOf(val);
		
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

	public StockAction(int id){
		this.id = id;
	}
	
	public StockAction(InsertBuilder build){
		setRestaurantId(build.restaurantId);
		setOriStockId(build.oriStockId);
		setOriStockDate(build.oriStockIdDate);
		setDeptIn (build.deptIn);
		setDeptOut(build.deptOut);
		setSupplier(build.supplier);
		setOperatorId(build.operatorId);
		setOperator(build.operator);
		setDetails(build.stockActionDetails);
		setCateType(build.cateType);
		setType(build.type);
		setSubType(build.subType);
		setStatus(build.status);
		setComment(build.comment);
		setStockInRestaurantId(build.stockInRestaurantId);
		setStockOutRestaurantId(build.stockOutRestaurantId);
		if(build.subType == SubType.STOCK_IN || build.subType == SubType.STOCK_OUT){
			setActualPrice(build.getActualPrice());
		}else{
			setActualPrice(getTotalPrice());
		}
	}
	
	public StockAction(UpdateBuilder build){
		setId(build.id);
		setOriStockId(build.oriStockId);
		setOriStockDate(build.oriStockDate);
		setDeptIn (build.deptIn);
		setDeptOut(build.deptOut);
		setSupplier(build.supplier);
		setOperatorId(build.operatorId);
		setOperator(build.operator);
		setDetails(build.stockActionDetails);
		setCateType(build.cateType);
		setType(build.type);
		setSubType(build.subType);
		setStatus(build.status);
		setComment(build.comment);
		if(build.subType == SubType.STOCK_IN || build.subType == SubType.STOCK_OUT){
			setActualPrice(build.getActualPrice());
		}else{
			setActualPrice(getTotalPrice());
		}
	}
	
	
	public StockAction(AuditBuilder build){
		setId(build.id);
		setApprover(build.approver);
		setApproverId(build.approverId);
		setApproverDate(build.approverDate);
		setStatus(build.status);
	}
	
	public StockAction(ReAuditBuilder build){
		setId(build.id);
		setOriStockId(build.oriStockId);
		setOriStockDate(build.oriStockDate);
		setDeptIn (build.deptIn);
		setDeptOut(build.deptOut);
		setSupplier(build.supplier);
		setOperatorId(build.operatorId);
		setOperator(build.operator);
		setDetails(build.stockActionDetails);
		setCateType(build.cateType);
		setType(build.type);
		setSubType(build.subType);
		setStatus(build.status);
		setComment(build.comment);
		if(build.subType == SubType.STOCK_IN || build.subType == SubType.STOCK_OUT || build.subType == SubType.DISTRIBUTION_SEND || build.subType == SubType.DISTRIBUTION_RECEIVE || build.subType == SubType.DISTRIBUTION_RETURN || build.subType == SubType.DISTRIBUTION_RECOVERY){
			setActualPrice(build.actualPrice);
		}else{
			setActualPrice(getTotalPrice());
		}
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
		if(this.subType == SubType.INIT || 
		   this.subType == SubType.STOCK_IN ||
		   this.subType == SubType.STOCK_OUT ||
		   this.subType == SubType.CONSUMPTION ||
		   this.subType == SubType.DISTRIBUTION_SEND || 
		   this.subType == SubType.DISTRIBUTION_RECEIVE ||
		   this.subType == SubType.DISTRIBUTION_RETURN || 
		   this.subType == SubType.DISTRIBUTION_RECOVERY){
			
			for (StockActionDetail sDetail : this.stockDetails) {
				sum += sDetail.getAmount() * sDetail.getPrice();
			}
		}
		return sum;
	}
	
	@Override
	public String toString() {
		return "stockIn : id=" + id + 
				"oriId=" + getOriStockId() + 
				"approver=" + getApprover() + 
				"deptIn=" + deptIn.getName() + 
				"deptOut=" + deptOut.getName() + 
				"operator=" + getOperator() + 
				"subtype =" + getSubType() +
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
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putInt("restaurantId", this.restaurantId);
		jm.putLong("birthDate", this.birthDate);
		jm.putString("birthDateFormat", DateUtil.formatToDate(this.birthDate));
		jm.putString("oriStockId", this.oriStockId);
		jm.putString("oriStockDateFormat", DateUtil.format(this.oriStockDate, DateUtil.Pattern.DATE.getPattern()));
		jm.putInt("approverId", this.approverId);
		jm.putString("approverName", this.approver);
		jm.putString("approverDateFormat", DateUtil.formatToDate(this.getApproverDate()));
		jm.putJsonable("deptIn", this.deptIn, Department.DEPT_JSONABLE_COMPLEX);
		jm.putString("stockInName",  this.deptIn.getName());
		jm.putJsonable("deptOut", this.deptOut, Department.DEPT_JSONABLE_COMPLEX);
		jm.putString("stockOutName",  this.deptOut.getName());
		jm.putJsonable("supplier", this.supplier, 0);
		jm.putString("supplierName", this.supplier.getName());
		jm.putInt("operatorId", this.operatorId);
		jm.putString("operatorName", this.operator);
		jm.putFloat("amount", this.amount);
		jm.putFloat("price", this.price);
		jm.putFloat("actualPrice", this.actualPrice);
		jm.putInt("cateTypeValue", this.cateType.getValue());
		jm.putString("cateTypeText", this.cateType.getText());
		jm.putInt("typeValue", this.type.getVal());
		jm.putString("typeText", this.type.getDesc());
		jm.putInt("subTypeValue", this.subType.getVal());
		jm.putString("subTypeText", this.subType.getText());
		jm.putInt("statusValue", this.status.getVal());
		jm.putString("statusText", this.status.getDesc());
		jm.putString("comment", this.comment);
		jm.putJsonableList("stockDetails", this.stockDetails, 0);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

}

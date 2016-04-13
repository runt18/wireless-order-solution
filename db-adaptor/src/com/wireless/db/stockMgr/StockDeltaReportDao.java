package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.SubType;
import com.wireless.pojo.stockMgr.StockReport;
import com.wireless.pojo.stockMgr.StockTakeDetail;

public class StockDeltaReportDao {


	
	public static int deltaReportCount(Staff term, String begin, String end, String dept, String extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return deltaReportCount(dbCon, term, begin, end, dept, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static int deltaReportCount(DBCon dbCon, Staff term, String begin, String end, String dept, String extraCond) throws SQLException, BusinessException{
		List<StockTakeDetail> stockTakeDetails = new ArrayList<StockTakeDetail>();
		stockTakeDetails = deltaReport(dbCon, term, begin, end, dept, extraCond, null);
		return stockTakeDetails.size();
		
	}
	
	public static List<StockTakeDetail> deltaReport(Staff term, String begin, String end, String dept, String extraCond, String orderClause) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return deltaReport(dbCon, term, begin, end, dept, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<StockTakeDetail> deltaReport(DBCon dbCon, Staff term, String begin, String end, String dept, String extraCond, String orderClause) throws SQLException, BusinessException{
		float actualFinalAmount;
		List<StockTakeDetail> stockTakeDetails = new ArrayList<StockTakeDetail>();
		List<StockReport> stockReports;
		if(dept.equals("-1")){
			stockReports = StockReportDao.getStockCollect(dbCon, term, begin, end, extraCond, orderClause);
		}else{
			stockReports = getStockCollect(dbCon, term, begin, end, dept, extraCond, orderClause);
		}
		
		for (StockReport stockReport : stockReports) {
			//在stockTake中加期初,期末,入库总数, 出库总数4个字段当做消耗差异表model
			StockTakeDetail stockTakeDetail = new StockTakeDetail();
			stockTakeDetail.setMaterialId(stockReport.getMaterial().getId());
			stockTakeDetail.setMaterialName(stockReport.getMaterial().getName());
			stockTakeDetail.setPrimeAmount(stockReport.getPrimeAmount());
			//实际期末数量（remaining）
			actualFinalAmount = stockReport.getFinalAmount();
			//期末数量
			stockTakeDetail.setEndAmount(actualFinalAmount);
			
			//理论消耗(销售量)
			float expectAmount = stockReport.getUseUp();
			stockTakeDetail.setExpectAmount(expectAmount);
			//实际消耗(期初+入库-出库-期末)	
			float actualAmount = stockReport.getPrimeAmount() + stockReport.getStockIn() + stockReport.getStockInTransfer() - stockReport.getStockOut() - stockReport.getStockOutTransfer() - actualFinalAmount;
			stockTakeDetail.setActualAmount(actualAmount);
			//差异(理论-实际)
			float deltaAmount = expectAmount - actualAmount;
			stockTakeDetail.setDeltaAmount(deltaAmount);
			
			//入库总数
			stockTakeDetail.setStockInTotal(stockReport.getStockIn() + stockReport.getStockInTransfer() + stockReport.getStockSpill());
			
			//出库总数
			stockTakeDetail.setStockOutTotal(stockReport.getStockOut() - stockReport.getStockOutTransfer() - stockReport.getStockDamage());

			stockTakeDetails.add(stockTakeDetail);
		}
		return stockTakeDetails;
	}
	
	
	public static List<StockReport> getStockCollect(DBCon dbCon, Staff term, String begin, String end, String dept, String extraCond, String orderClause) throws SQLException, BusinessException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try{
			sdf.parse(begin);
			sdf.parse(end);
		}catch(Exception e){
			throw new BusinessException("时间格式不对");
		}
		String sql = "SELECT S.sub_type, S.dept_in, S.dept_out, D.material_id, D.name, SUM(D.amount) as amount FROM ((" +
						Params.dbName + ".stock_action as S " +  
						" INNER JOIN " + Params.dbName + ".stock_action_detail as D ON S.id = D.stock_action_id) " +
						" INNER JOIN " + Params.dbName + ".material as M ON M.material_id = D.material_id) " +
						" INNER JOIN " + Params.dbName + ".material_cate as MC ON MC.cate_id = M.cate_id " +
						" WHERE S.restaurant_id = " + term.getRestaurantId() + 
						" AND S.status IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.RE_AUDIT.getVal() + ") "+
						" AND (S.dept_in = " + dept  + " OR S.dept_out = " + dept + ")" + 
						" AND S.ori_stock_date <= '" + end + " 23:59:59' AND S.ori_stock_date >= '" + begin + "'" +
						(extraCond == null ? "" : extraCond) +
						" GROUP BY S.sub_type, D.material_id " +
						(orderClause == null ? "" : orderClause);
						
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		Map<Integer, StockReport> result = new HashMap<Integer, StockReport>();
		
		StockReport stockReport;
		while(dbCon.rs.next()){
			float amount = dbCon.rs.getFloat("amount");
			int subType = dbCon.rs.getInt("sub_type");
			int materialId = dbCon.rs.getInt("material_id");
			int deptIn = dbCon.rs.getInt("dept_in");
			
			if(result.get(materialId) == null){
				stockReport = new StockReport();
				
				if(SubType.STOCK_IN.getVal() == subType){
					stockReport.setStockIn(amount);
				}else if(SubType.SPILL.getVal() == subType){
					stockReport.setStockSpill(amount);
				}else if(SubType.MORE.getVal() == subType){
					stockReport.setStockTakeMore(amount);
				}else if(SubType.STOCK_OUT.getVal() == subType){
					stockReport.setStockOut(amount);
				}else if(SubType.CONSUMPTION.getVal() == subType){
					stockReport.setConsumption(amount);
				}else if(SubType.DAMAGE.getVal() == subType){
					stockReport.setStockDamage(amount);
				}else if(SubType.LESS.getVal() == subType){
					stockReport.setStockTakeLess(amount);
				}else{
					if(Integer.parseInt(dept) == deptIn){
						stockReport.setStockInTransfer(amount);
						stockReport.setStockOutTransfer(0);
					}else{
						stockReport.setStockInTransfer(0);
						stockReport.setStockOutTransfer(amount);
					}
				}
				stockReport.getMaterial().setId(materialId);
				stockReport.getMaterial().setName(dbCon.rs.getString("name"));

				DBCon endAmountCon = new DBCon();
				try{
					endAmountCon.connect();
					String endAmount = "SELECT S.dept_in, S.sub_type, D.remaining, D.dept_in_remaining, D.dept_out_remaining, D.price FROM " + Params.dbName + ".stock_action as S " + 
							" INNER JOIN " + Params.dbName + ".stock_action_detail as D ON S.id = D.stock_action_id " +
							" WHERE S.restaurant_id = " + term.getRestaurantId() +
							" AND (S.dept_in = " + dept + " OR S.dept_out = " + dept + ")" +
							" AND S.ori_stock_date <= '" + end + " 23:59:59' AND D.material_id = " + materialId + 
							" AND S.status IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.RE_AUDIT.getVal() + ") "+
							
							" ORDER BY D.id DESC LIMIT 0,1";
					endAmountCon.rs = endAmountCon.stmt.executeQuery(endAmount);
					
					if(endAmountCon.rs.next()){
						int endDeptIn = endAmountCon.rs.getInt("dept_in");
						SubType actionSubType = SubType.valueOf(endAmountCon.rs.getInt("sub_type"));
						if(actionSubType == SubType.STOCK_IN ||  actionSubType == SubType.MORE || actionSubType == SubType.SPILL){
							stockReport.setFinalAmount(endAmountCon.rs.getFloat("dept_in_remaining"));
						}else if(actionSubType == SubType.STOCK_IN_TRANSFER || actionSubType == SubType.STOCK_OUT_TRANSFER){
							if(Integer.parseInt(dept) == endDeptIn){
								stockReport.setFinalAmount(endAmountCon.rs.getFloat("dept_in_remaining"));
							}else{
								stockReport.setFinalAmount(endAmountCon.rs.getFloat("dept_out_remaining"));
							}
						}else{
							stockReport.setFinalAmount(endAmountCon.rs.getFloat("dept_out_remaining"));
						}
						
						stockReport.setFinalPrice(endAmountCon.rs.getFloat("price"));
					}
				}finally{
					endAmountCon.disconnect();
				}

				DBCon primeAmountCon = new DBCon();
				try{
					primeAmountCon.connect();
					String primeAmount = "SELECT S.dept_in, S.sub_type, D.remaining, D.dept_in_remaining, D.dept_out_remaining FROM " + Params.dbName + ".stock_action as S " + 
							" INNER JOIN " + Params.dbName + ".stock_action_detail as D  ON S.id = D.stock_action_id " + 
							" WHERE S.restaurant_id = " + term.getRestaurantId() +
							" AND (S.dept_in = " + dept + " OR S.dept_out = " + dept + ")" +
							" AND S.ori_stock_date < '" + begin + "' AND D.material_id = " + materialId + 
							" AND S.status IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.RE_AUDIT.getVal() + ") "+
							" ORDER BY D.id DESC LIMIT 0,1";
		
					primeAmountCon.rs = primeAmountCon.stmt.executeQuery(primeAmount);
					if(primeAmountCon.rs.next()){
						int primeDeptIn = primeAmountCon.rs.getInt("dept_in");
						SubType actionSubType = SubType.valueOf(primeAmountCon.rs.getInt("sub_type"));
						if(actionSubType == SubType.STOCK_IN || actionSubType == SubType.MORE || actionSubType == SubType.SPILL){
							stockReport.setPrimeAmount(primeAmountCon.rs.getFloat("dept_in_remaining"));
						}else if(actionSubType == SubType.STOCK_IN_TRANSFER || actionSubType == SubType.STOCK_OUT_TRANSFER){
							if(Integer.parseInt(dept) == primeDeptIn){
								stockReport.setPrimeAmount(primeAmountCon.rs.getFloat("dept_in_remaining"));
							}else{
								stockReport.setPrimeAmount(primeAmountCon.rs.getFloat("dept_out_remaining"));
							}
						}else{
							stockReport.setPrimeAmount(primeAmountCon.rs.getFloat("dept_out_remaining"));
						}
					}else{
						stockReport.setPrimeAmount(0);
					}
				}finally{
					primeAmountCon.disconnect();
				}

				result.put(materialId, stockReport);
			}else{
				//如果已经material_id存在,则只需加subType的数量
				stockReport = result.get(dbCon.rs.getInt("material_id"));
				if(SubType.STOCK_IN.getVal() == subType){
					stockReport.setStockIn(amount);
				}else if(SubType.SPILL.getVal() == subType){
					stockReport.setStockSpill(amount);
				}else if(SubType.MORE.getVal() == subType){
					stockReport.setStockTakeMore(amount);
				}else if(SubType.STOCK_OUT.getVal() == subType){
					stockReport.setStockOut(amount);
				}else if(SubType.DAMAGE.getVal() == subType){
					stockReport.setStockDamage(amount);
				}else if(SubType.LESS.getVal() == subType){
					stockReport.setStockTakeLess(amount);
				}else if(SubType.CONSUMPTION.getVal() == subType){
					stockReport.setConsumption(amount);
				}else{
					if(Integer.parseInt(dept) == deptIn){
						stockReport.setStockInTransfer(stockReport.getStockInTransfer() + amount);
					}else{
						stockReport.setStockOutTransfer(stockReport.getStockOutTransfer() + amount);
					}
				}
			}		
		}
		if(result.values().size() > 0){
			return new ArrayList<StockReport>(result.values()); 
		}else{
			return Collections.emptyList();
		}
	}
	
	
	
}

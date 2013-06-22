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
import com.wireless.pojo.stockMgr.StockAction.CateType;
import com.wireless.pojo.stockMgr.StockAction.SubType;
import com.wireless.pojo.stockMgr.StockReport;
import com.wireless.protocol.Terminal;

public class StockReportDao {
	
	/**
	 * Get the list of StockReport according to beginDate, endDate.
	 * @param term
	 * 			the Terminal
	 * @param begin
	 * 			the begin Date
	 * @param end
	 * 			the end Date
	 * @return	the list of StockReport
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			if the form of time is not exactly
	 */
	public static List<StockReport> getStockCollectByTime(Terminal term, String begin, String end, String orderClause) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockCollect(dbCon, term, begin, end, null, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the list of StockReport according to beginDate, endDate and extraCond.
	 * @param term
	 * 			the Terminal
	 * @param begin
	 * 			the begin Date
	 * @param end
	 * 			the end Date
	 * @param extraCond
	 * 			the extra Condition
	 * @return	the list of StockReport
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			if the form of time is not exactly
	 */
	public static List<StockReport> getStockCollectByTypes(Terminal term, String begin, String end, CateType cateType, String orderClause) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockCollect(dbCon, term, begin, end, " AND cate_type = " + cateType.getValue(), orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Get the list of StockReport according to beginDate, endDate and extraCond.
	 * @param dbCon
	 * 			the dataBase connection
	 * @param term
	 * 			the Terminal
	 * @param begin
	 * 			the begin Date
	 * @param end
	 * 			the end Date
	 * @param extraCond
	 * 			the extra Condition
	 * @return	the list of StockReport
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			if the form of time is not exactly
	 */
	public static List<StockReport> getStockCollect(DBCon dbCon, Terminal term, String begin, String end, String extraCond, String orderClause) throws SQLException, BusinessException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try{
			sdf.parse(begin);
			sdf.parse(end);
		}catch(Exception e){
			throw new BusinessException("时间格式不对");
		}
		String sql = "SELECT S.sub_type, D.material_id, D.name, sum(D.amount) as amount FROM " +
						Params.dbName + ".stock_action as S " +  
						" INNER JOIN " + Params.dbName +".stock_action_detail as D ON S.id = D.stock_action_id " + 
						" WHERE ori_stock_date <= '" + end + "' AND ori_stock_date >= '" + begin + "'" +
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
			if(result.get(materialId) == null){
				stockReport = new StockReport();
				
				if(SubType.STOCK_IN.getVal() == subType){
					stockReport.setStockIn(amount);
				}else if(SubType.STOCK_IN_TRANSFER.getVal() == subType){
					stockReport.setStockInTransfer(amount);
				}else if(SubType.SPILL.getVal() == subType){
					stockReport.setStockSpill(amount);
				}else if(SubType.MORE.getVal() == subType){
					stockReport.setStockTakeMore(amount);
				}else if(SubType.STOCK_OUT.getVal() == subType){
					stockReport.setStockOutTransfer(amount);
				}else if(SubType.STOCK_OUT_TRANSFER.getVal() == subType){
					stockReport.setStockOutTransfer(amount);
				}else if(SubType.DAMAGE.getVal() == subType){
					stockReport.setStockDamage(amount);
				}else if(SubType.LESS.getVal() == subType){
					stockReport.setStockTakeLess(amount);
				}else{
					stockReport.setUseUp(amount);
				}
				stockReport.getMaterial().setId(materialId);
				stockReport.getMaterial().setName(dbCon.rs.getString("name"));

				DBCon endAmountCon = new DBCon();
				try{
					endAmountCon.connect();
					String endAmount = "SELECT D.remaining, D.price FROM " + Params.dbName + ".stock_action as S " + 
							" INNER JOIN " + Params.dbName + ".stock_action_detail as D " +  
							" ON S.id = D.stock_action_id WHERE ori_stock_date < '" + end + "' AND d.material_id = " + 
							materialId + " ORDER BY approve_date DESC LIMIT 0,1";
					endAmountCon.rs = endAmountCon.stmt.executeQuery(endAmount);
					
					if(endAmountCon.rs.next()){
						stockReport.setFinalAmount(endAmountCon.rs.getFloat("remaining"));
						stockReport.setFinalPrice(endAmountCon.rs.getFloat("price"));
					}
				}finally{
					endAmountCon.disconnect();
				}

				DBCon primeAmountCon = new DBCon();
				try{
					primeAmountCon.connect();
					String primeAmount = "SELECT D.remaining FROM " + Params.dbName + ".stock_action as S " + 
							" INNER JOIN " + Params.dbName + ".stock_action_detail as D " +  
							" ON S.id = D.stock_action_id WHERE ori_stock_date < '" + begin + "' AND d.material_id = " + 
							materialId + " ORDER BY approve_date DESC LIMIT 0,1";
		
					primeAmountCon.rs = primeAmountCon.stmt.executeQuery(primeAmount);
					if(primeAmountCon.rs.next()){
						stockReport.setPrimeAmount(primeAmountCon.rs.getFloat("remaining"));
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
				}else if(SubType.STOCK_IN_TRANSFER.getVal() == subType){
					stockReport.setStockInTransfer(amount);
				}else if(SubType.SPILL.getVal() == subType){
					stockReport.setStockSpill(amount);
				}else if(SubType.MORE.getVal() == subType){
					stockReport.setStockTakeMore(amount);
				}else if(SubType.STOCK_OUT.getVal() == subType){
					stockReport.setStockOutTransfer(amount);
				}else if(SubType.STOCK_OUT_TRANSFER.getVal() == subType){
					stockReport.setStockOutTransfer(amount);
				}else if(SubType.DAMAGE.getVal() == subType){
					stockReport.setStockDamage(amount);
				}else if(SubType.LESS.getVal() == subType){
					stockReport.setStockTakeLess(amount);
				}else{
					stockReport.setUseUp(amount);
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

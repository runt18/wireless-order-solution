package com.wireless.db.stockMgr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.stockMgr.StockAction.CateType;
import com.wireless.pojo.stockMgr.StockAction.SubType;
import com.wireless.pojo.stockMgr.StockReport;
import com.wireless.pojo.util.DateUtil;
import com.wireless.protocol.Terminal;

public class StockReportDao {
	
	
	public static List<StockReport> getStockCollectByTime(Terminal term, long begin, long end) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockCollect(dbCon, term, begin, end, null);
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
	 */
	public static List<StockReport> getStockCollectByTypes(Terminal term, long begin, long end, CateType cateType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockCollect(dbCon, term, begin, end, " AND cate_type = " + cateType.getValue());
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
	 */
	public static List<StockReport> getStockCollect(DBCon dbCon, Terminal term, long begin, long end, String extraCond) throws SQLException{
		String sql = "SELECT S.sub_type, D.material_id, D.name sum(D.amount) as amount FROM " +
						Params.dbName + ".stock_action as S " +  
						" INNER JOIN " + Params.dbName +".stock_action_detail as D ON S.id = D.stock_action_id " +  
						"GROUP BY S.sub_type, D.material_id" +
						"WHERE approve_date <= '" + DateUtil.format(end) + "' AND approve_date >= '" + DateUtil.format(begin) + "'" +
						(extraCond == null ? "" : extraCond);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		Map<Integer, StockReport> result = new HashMap<Integer, StockReport>();
		
		StockReport stockReport;
		while(dbCon.rs.next()){
			int amount = dbCon.rs.getInt("sub_type");
			if(result.get(dbCon.rs.getInt("material_id")) == null){
				stockReport = new StockReport();
				
				if(SubType.STOCK_IN.getVal() == amount){
					stockReport.setStockIn(amount);
				}else if(SubType.STOCK_IN_TRANSFER.getVal() == amount){
					stockReport.setStockInTransfer(amount);
				}else if(SubType.SPILL.getVal() == amount){
					stockReport.setStockSpill(amount);
				}else if(SubType.MORE.getVal() == amount){
					stockReport.setStockTakeMore(amount);
				}else if(SubType.STOCK_OUT.getVal() == amount){
					stockReport.setStockOutTransfer(amount);
				}else if(SubType.STOCK_OUT_TRANSFER.getVal() == amount){
					stockReport.setStockOutTransfer(amount);
				}else if(SubType.DAMAGE.getVal() == amount){
					stockReport.setStockDamage(amount);
				}else if(SubType.LESS.getVal() == amount){
					stockReport.setStockTakeLess(amount);
				}else{
					stockReport.setUseUp(amount);
				}
				stockReport.setMaterialId(dbCon.rs.getInt("material_id"));
				stockReport.setName(dbCon.rs.getString("name"));
				String primeAmount = "SELECT D.remaining FROM " + Params.dbName + ".stock_action as S " + 
						" INNER JOIN " + Params.dbName + ".stock_action_detail as D " +  
						" ON S.id = D.stock_action_id WHERE approve_date < '" + DateUtil.format(begin) + "' AND d.material_id = " + 
						dbCon.rs.getInt("material_id") + " ORDER BY approve_date DESC LIMIT 0,1";
	
				ResultSet primeRs = dbCon.stmt.executeQuery(primeAmount);
				if(primeRs.next()){
					stockReport.setPrimeAmount(primeRs.getFloat("remaining"));
				}
				primeRs.close();
				String endAmount = "SELECT D.remaining, D.price FROM " + Params.dbName + ".stock_action as S " + 
									" INNER JOIN " + Params.dbName + ".stock_action_detail as D " +  
									" ON S.id = D.stock_action_id WHERE approve_date < '" + DateUtil.format(end) + "' AND d.material_id = " + 
									dbCon.rs.getInt("material_id") + " ORDER BY approve_date DESC LIMIT 0,1";
				ResultSet endRs = dbCon.stmt.executeQuery(endAmount);
				if(endRs.next()){
					stockReport.setFinalAmount(endRs.getFloat("remaining"));
					stockReport.setFinalPrice(endRs.getFloat("price"));
				}
				endRs.close();
				result.put(dbCon.rs.getInt("material_id"), stockReport);
			}else{
				//如果已经material_id存在,则只需加subType的数量
				stockReport = result.get(dbCon.rs.getInt("material_id"));
				if(SubType.STOCK_IN.getVal() == amount){
					stockReport.setStockIn(amount);
				}else if(SubType.STOCK_IN_TRANSFER.getVal() == amount){
					stockReport.setStockInTransfer(amount);
				}else if(SubType.SPILL.getVal() == amount){
					stockReport.setStockSpill(amount);
				}else if(SubType.MORE.getVal() == amount){
					stockReport.setStockTakeMore(amount);
				}else if(SubType.STOCK_OUT.getVal() == amount){
					stockReport.setStockOutTransfer(amount);
				}else if(SubType.STOCK_OUT_TRANSFER.getVal() == amount){
					stockReport.setStockOutTransfer(amount);
				}else if(SubType.DAMAGE.getVal() == amount){
					stockReport.setStockDamage(amount);
				}else if(SubType.LESS.getVal() == amount){
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

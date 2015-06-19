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
	public static List<StockReport> getStockCollectByTime(Staff term, String begin, String end, String extraCond, String orderClause) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockCollect(dbCon, term, begin, end, extraCond, orderClause);
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
	public static List<StockReport> getStockCollectByTypes(Staff term, String begin, String end, String extraCond, String orderClause) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockCollect(dbCon, term, begin, end, extraCond, orderClause);
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
	public static List<StockReport> getStockCollect(DBCon dbCon, Staff term, String begin, String end, String extraCond, String orderClause) throws SQLException, BusinessException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try{
			sdf.parse(begin);
			sdf.parse(end);
		}catch(Exception e){
			throw new BusinessException("时间格式不对");
		}
		String sql = "SELECT S.sub_type, D.material_id, D.name, sum(D.amount) as amount, M.price FROM ((" +
						Params.dbName + ".stock_action as S " +  
						" INNER JOIN " + Params.dbName + ".stock_action_detail as D ON S.id = D.stock_action_id) " +
						" INNER JOIN " + Params.dbName + ".material as M ON M.material_id = D.material_id) " +
						" INNER JOIN " + Params.dbName + ".material_cate as MC ON MC.cate_id = M.cate_id " +
						" WHERE S.restaurant_id = " + term.getRestaurantId() + 
						" AND S.status IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.DELETE.getVal() + ") "+
						" AND S.ori_stock_date <= '" + end + " 23:59:59' AND S.ori_stock_date >= '" + begin + "'" +
						(extraCond == null ? "" : extraCond) +
						" GROUP BY S.sub_type, D.material_id " +
						(orderClause == null ? "" : orderClause);
						
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		Map<Integer, StockReport> result = new HashMap<Integer, StockReport>();
		
		StockReport stockReport;
		while(dbCon.rs.next()){
			float amount = dbCon.rs.getFloat("amount");
			float finalPrice = dbCon.rs.getFloat("price");
			int subType = dbCon.rs.getInt("sub_type");
			int materialId = dbCon.rs.getInt("material_id");
			
			if(result.get(materialId) == null){
				stockReport = new StockReport();
				
				if(SubType.STOCK_IN.getVal() == subType || SubType.INIT.getVal() == subType){
					stockReport.setStockIn(amount);
				}else if(SubType.STOCK_IN_TRANSFER.getVal() == subType){
					stockReport.setStockInTransfer(amount);
					stockReport.setStockOutTransfer(amount);
				}else if(SubType.SPILL.getVal() == subType){
					stockReport.setStockSpill(amount);
				}else if(SubType.MORE.getVal() == subType){
					stockReport.setStockTakeMore(amount);
				}else if(SubType.STOCK_OUT.getVal() == subType){
					stockReport.setStockOut(amount);
				}else if(SubType.STOCK_OUT_TRANSFER.getVal() == subType){
					stockReport.setStockInTransfer(amount);
					stockReport.setStockOutTransfer(amount);
				}else if(SubType.DAMAGE.getVal() == subType){
					stockReport.setStockDamage(amount);
				}else if(SubType.LESS.getVal() == subType){
					stockReport.setStockTakeLess(amount);
				}else{
					stockReport.setUseUp(amount);
				}
				stockReport.getMaterial().setId(materialId);
				stockReport.setFinalPrice(finalPrice);
				stockReport.getMaterial().setName(dbCon.rs.getString("name"));

				DBCon endAmountCon = new DBCon();
				try{
					endAmountCon.connect();
					String endAmount = "SELECT D.remaining, D.price FROM " + Params.dbName + ".stock_action as S " + 
							" INNER JOIN " + Params.dbName + ".stock_action_detail as D  ON S.id = D.stock_action_id " +
							" WHERE S.restaurant_id = " + term.getRestaurantId() +
							" AND S.ori_stock_date <= '" + end + " 23:59:59' AND D.material_id = " + materialId + 
							" AND S.status IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.DELETE.getVal() + ") "+
							" ORDER BY D.id DESC LIMIT 0,1";
					endAmountCon.rs = endAmountCon.stmt.executeQuery(endAmount);
					
					if(endAmountCon.rs.next()){
						stockReport.setFinalAmount(endAmountCon.rs.getFloat("remaining"));
						stockReport.setFinalMoney((float)Math.round((endAmountCon.rs.getFloat("remaining") * finalPrice) * 100) / 100);
					}
				}finally{
					endAmountCon.disconnect();
				}

				DBCon primeAmountCon = new DBCon();
				try{
					primeAmountCon.connect();
					String primeAmount = "SELECT D.remaining FROM " + Params.dbName + ".stock_action as S " + 
							" INNER JOIN " + Params.dbName + ".stock_action_detail as D  ON S.id = D.stock_action_id " + 
							" WHERE S.restaurant_id = " + term.getRestaurantId() +
							" AND S.ori_stock_date < '" + begin + "' AND D.material_id = " + materialId + 
							" AND S.status IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.DELETE.getVal() + ") "+
							" ORDER BY D.id DESC LIMIT 0,1";
		
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
				if(SubType.STOCK_IN.getVal() == subType || SubType.INIT.getVal() == subType){
					stockReport.setStockIn(amount);
				}else if(SubType.STOCK_IN_TRANSFER.getVal() == subType){
					stockReport.setStockInTransfer(amount);
					stockReport.setStockOutTransfer(amount);
				}else if(SubType.SPILL.getVal() == subType){
					stockReport.setStockSpill(amount);
				}else if(SubType.MORE.getVal() == subType){
					stockReport.setStockTakeMore(amount);
				}else if(SubType.STOCK_OUT.getVal() == subType){
					stockReport.setStockOut(amount);
				}else if(SubType.STOCK_OUT_TRANSFER.getVal() == subType){
					stockReport.setStockOutTransfer(amount);
					stockReport.setStockInTransfer(amount);
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
	
	public static List<StockReport> getStockCollectByDept(Staff term, String begin, String end, String extraCond, String orderClause, int deptId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockCollectByDept(dbCon, term, begin, end, extraCond, orderClause, deptId);
		}finally{
			dbCon.disconnect();
		}
	}
	public static List<StockReport> getStockCollectByDept(DBCon dbCon, Staff term, String begin, String end, String extraCond, String orderClause, int deptId) throws SQLException, BusinessException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try{
			sdf.parse(begin);
			sdf.parse(end);
		}catch(Exception e){
			throw new BusinessException("时间格式不对");
		}
		String sql = "SELECT S.sub_type, S.dept_in, S.dept_out, D.material_id, D.name, sum(D.amount) as amount, M.price FROM ((" +
						Params.dbName + ".stock_action as S " +  
						" INNER JOIN " + Params.dbName + ".stock_action_detail as D ON S.id = D.stock_action_id) " +
						" INNER JOIN " + Params.dbName + ".material as M ON M.material_id = D.material_id) " +
						" INNER JOIN " + Params.dbName + ".material_cate as MC ON MC.cate_id = M.cate_id " +
						" WHERE S.restaurant_id = " + term.getRestaurantId() + 
						" AND S.ori_stock_date <= '" + end + " 23:59:59' AND S.ori_stock_date >= '" + begin + "'" +
						(extraCond == null ? "" : extraCond) +
						" GROUP BY S.sub_type,S.dept_in, D.material_id " +
						(orderClause == null ? "" : orderClause);
						
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		Map<Integer, StockReport> result = new HashMap<Integer, StockReport>();
		
		StockReport stockReport;
		while(dbCon.rs.next()){
			float amount = dbCon.rs.getFloat("amount");
			float finalPrice = dbCon.rs.getFloat("price");
			int subType = dbCon.rs.getInt("sub_type");
			int materialId = dbCon.rs.getInt("material_id");
			int deptInId = dbCon.rs.getInt("dept_in");
			//int deptOutId = dbCon.rs.getInt("dept_out");
			
			if(result.get(materialId) == null){
				stockReport = new StockReport();
				
				if(SubType.STOCK_IN_TRANSFER.getVal() == subType || SubType.STOCK_OUT_TRANSFER.getVal() == subType){
					if(deptId == deptInId){
						stockReport.setStockInTransfer(amount);
					}else{
						stockReport.setStockOutTransfer(amount);
					}
				}else{
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
					}else{
						stockReport.setUseUp(amount);
					}
				}
				stockReport.getMaterial().setId(materialId);
				stockReport.setFinalPrice(finalPrice);
				stockReport.getMaterial().setName(dbCon.rs.getString("name"));

				DBCon endAmountCon = new DBCon();
				try{
					endAmountCon.connect();
					String endAmount = "SELECT S.dept_in, S.dept_out, D.remaining, D.price, D.dept_in_remaining, D.dept_out_remaining FROM " + Params.dbName + ".stock_action as S " + 
							" INNER JOIN " + Params.dbName + ".stock_action_detail as D  ON S.id = D.stock_action_id " +
							" WHERE S.restaurant_id = " + term.getRestaurantId() +
							" AND S.ori_stock_date <= '" + end + " 23:59:59' AND D.material_id = " + materialId + 
							" AND S.status IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.DELETE.getVal() + ") "+
							" AND (S.dept_in = " + deptId + " OR S.dept_out = " + deptId + ") " + 
							" ORDER BY D.id DESC LIMIT 0,1";
					endAmountCon.rs = endAmountCon.stmt.executeQuery(endAmount);
					
					if(endAmountCon.rs.next()){
						if(deptId == endAmountCon.rs.getInt("dept_in")){
							stockReport.setFinalAmount(endAmountCon.rs.getFloat("dept_in_remaining"));
						}else{
							stockReport.setFinalAmount(endAmountCon.rs.getFloat("dept_out_remaining"));
						}
						
						stockReport.setFinalMoney(stockReport.getFinalAmount() * finalPrice);
					}
				}finally{
					endAmountCon.rs.close();
					endAmountCon.disconnect();
				}

				DBCon primeAmountCon = new DBCon();
				try{
					primeAmountCon.connect();
					String primeAmount = "SELECT S.dept_in, S.dept_out, D.remaining, D.dept_in_remaining, D.dept_out_remaining FROM " + Params.dbName + ".stock_action as S " + 
							" INNER JOIN " + Params.dbName + ".stock_action_detail as D  ON S.id = D.stock_action_id " + 
							" WHERE S.restaurant_id = " + term.getRestaurantId() +
							" AND S.ori_stock_date <= '" + begin + "' AND D.material_id = " + materialId + 
							" AND S.status IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.DELETE.getVal() + ") "+
							" AND (S.dept_in = " + deptId + " OR S.dept_out = " + deptId + ") " + 
							" ORDER BY D.id DESC LIMIT 0,1";
		
					primeAmountCon.rs = primeAmountCon.stmt.executeQuery(primeAmount);
					if(primeAmountCon.rs.next()){
						if(deptId == primeAmountCon.rs.getInt("dept_in")){
							stockReport.setPrimeAmount(primeAmountCon.rs.getFloat("dept_in_remaining"));
						}else{
							stockReport.setPrimeAmount(primeAmountCon.rs.getFloat("dept_out_remaining"));
						}
					}else{
						stockReport.setPrimeAmount(0);
					}
				}finally{
					primeAmountCon.rs.close();
					primeAmountCon.disconnect();
				}

				result.put(materialId, stockReport);
			}else{
				//如果已经material_id存在,则只需加subType的数量
				stockReport = result.get(dbCon.rs.getInt("material_id"));
				
				if(SubType.STOCK_IN_TRANSFER.getVal() == subType || SubType.STOCK_OUT_TRANSFER.getVal() == subType){
					if(deptId == deptInId){
						stockReport.setStockInTransfer(amount);
					}else{
						stockReport.setStockOutTransfer(amount);
					}
				}else{
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
					}else{
						stockReport.setUseUp(amount);
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

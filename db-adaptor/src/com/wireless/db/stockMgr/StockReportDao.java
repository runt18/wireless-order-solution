package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DateRange;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.SubType;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.stockMgr.StockReport;

public class StockReportDao {
	
	public static class ExtraCond{
		private DateRange range;
		private int deptId = -1;
		private int materialId;
		private int materialCateId;
		private MaterialCate.Type materialCateType;
		
		public ExtraCond setMaterialCateType(MaterialCate.Type type){
			this.materialCateType = type;
			return this;
		}
		
		public ExtraCond setMaterialCate(int materialCateId){
			this.materialCateId = materialCateId;
			return this;
		}
		
		public ExtraCond setRange(String yyyymm) throws ParseException{
			Calendar c = Calendar.getInstance();
			c.setTime(new SimpleDateFormat("yyyy-MM").parse(yyyymm));
			int day = c.getActualMaximum(Calendar.DAY_OF_MONTH);
			this.range = new DateRange(yyyymm + "-01", yyyymm + "-" + day);
			return this;
		}
		
		public ExtraCond setRange(String begin, String end) throws ParseException{
			this.range = new DateRange(begin, end);
			return this;
		}
		
		public ExtraCond setDept(int deptId){
			this.deptId = deptId;
			return this;
		}
		
		public ExtraCond setMaterial(int materialId){
			this.materialId = materialId;
			return this;
		}
		
		@Override
		public String toString(){
			final StringBuilder extraCond = new StringBuilder();
			if(range != null){
				extraCond.append(" AND S.ori_stock_date BETWEEN '" + range.getOpeningFormat() + "' AND '" + range.getEndingFormat() + " 23:59:59'");
			}
			if(deptId != -1){
				extraCond.append(" AND (S.dept_in = " + deptId + " OR S.dept_out = " + deptId + ")");
			}
			if(materialId != 0){
				extraCond.append(" AND D.material_id = " + materialId);
			}
			if(materialCateId != 0){
				extraCond.append(" AND MC.cate_id = " + materialCateId);
			}
			if(materialCateType != null){
				extraCond.append(" AND MC.type = " + materialCateType.getValue());
			}
			return extraCond.toString();
		}
		
	}
	
	/**
	 * Get the list of StockReport according to extra condition {@link ExtraCond}
	 * @param staff
	 * 			the staff to perform this action
	 * @return	the list of StockReport
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<StockReport> getByCond(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the list of StockReport according to extra condition {@link ExtraCond}
	 * @param dbCon
	 * 			the dataBase connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return	the list of StockReport
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<StockReport> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		String sql;
		
		sql = " SELECT D.material_id, MAX(D.name) AS material_name, MAX(M.price) AS material_price " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.STOCK_IN.getVal() + " OR S.sub_type = " + StockAction.SubType.INIT.getVal() + " ,D.amount, 0)) AS stock_in " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.STOCK_IN_TRANSFER.getVal() + " , D.amount, 0)) AS stock_in_transfer " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.SPILL.getVal() + " , D.amount, 0)) AS stock_spill " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.MORE.getVal() + " , D.amount, 0)) AS stock_take_more " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.STOCK_OUT.getVal() + " , D.amount, 0)) AS stock_out " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.STOCK_OUT_TRANSFER.getVal() + " , D.amount, 0)) AS stock_out_transfer " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.DAMAGE.getVal() + " , D.amount, 0)) AS stock_damage " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.LESS.getVal() + " , D.amount, 0)) AS stock_take_less " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.CONSUMPTION.getVal() + " , D.amount, 0)) AS stock_consumption " +
			  " FROM " + Params.dbName + ".stock_action_detail D " +
			  " JOIN " + Params.dbName + ".stock_action S ON D.stock_action_id = S.id " +
			  " JOIN " + Params.dbName + ".material M ON M.material_id = D.material_id " +
 			  " JOIN " + Params.dbName + ".material_cate MC ON MC.cate_id = M.cate_id " +
			  " WHERE 1 = 1 " +
			  " AND S.restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : "") +
			  " GROUP BY D.material_id ";
		
		final List<StockReport> result = new ArrayList<>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			
			Material material = new Material(dbCon.rs.getInt("material_id"));
			material.setName(dbCon.rs.getString("material_name"));
			
			StockReport report = new StockReport();
			report.setMaterial(material);
			report.setFinalPrice(dbCon.rs.getFloat("material_price"));
			
			//入库采购
			report.setStockIn(dbCon.rs.getFloat("stock_in"));
			//入库调拨
			report.setStockInTransfer(dbCon.rs.getFloat("stock_in_transfer"));
			//入库报溢
			report.setStockSpill(dbCon.rs.getFloat("stock_spill"));
			//入库盘盈
			report.setStockTakeMore(dbCon.rs.getFloat("stock_take_more"));
			//出库退货
			report.setStockOut(dbCon.rs.getFloat("stock_out"));
			//出库调拨
			report.setStockOutTransfer(dbCon.rs.getFloat("stock_out_transfer"));
			//出库报损
			report.setStockDamage(dbCon.rs.getFloat("stock_damage"));
			//出库盘亏
			report.setStockTakeLess(dbCon.rs.getFloat("stock_take_less"));
			//出库消耗
			report.setConsumption(dbCon.rs.getFloat("stock_consumption"));
			result.add(report);
			
		}
		dbCon.rs.close();
		
//		//获取所有上月期初数量不为0的商品和原料
//		List<StockActionDetail> detailWithPrime = StockActionDetailDao.getByCond(dbCon, staff, new StockActionDetailDao.ExtraCond()
//																								.addStatus(StockAction.Status.AUDIT).addStatus(StockAction.Status.RE_AUDIT)
//																								.setOriStockDate(null, extraCond.range.getOpeningFormat()), 
//																								" GROUP BY D.material_id HAVING(D.dept_in_remaining) > 0 ");
//		//需要追加统计上月期初数量不为0，但是本月没有做过出入库任务的商品和原料
//		for(StockActionDetail detail : detailWithPrime){
//			
//			boolean isExist = false;
//			for(StockReport eachReport : result){
//				if(eachReport.getMaterial().getId() == detail.getMaterialId()){
//					isExist = true;
//				}
//			}
//			
//			if(!isExist){
//				try{
//					Material material = MaterialDao.getById(dbCon, staff, detail.getMaterialId());
//					
//					StockReport report = new StockReport();
//					report.setMaterial(material);
//					report.setFinalPrice(material.getPrice());
//					
//					result.add(report);
//				}catch(BusinessException ignored){
//					//ignored.printStackTrace();
//				}
//			}
//		}
			  
		for(StockReport report : result){
			final List<Department> depts = new ArrayList<>();
			if(extraCond.deptId != -1){
				depts.add(DepartmentDao.getById(dbCon, staff, extraCond.deptId));
			}else{
				depts.addAll(DepartmentDao.getDepartments4Inventory(dbCon, staff));
			}
			for(Department deptIn : depts){
				List<StockActionDetail> primeDetail = StockActionDetailDao.getByCond(dbCon, staff, new StockActionDetailDao.ExtraCond()
																		  .addStatus(StockAction.Status.AUDIT).addStatus(StockAction.Status.RE_AUDIT)
																		  .setOriStockDate(null, extraCond.range.getOpeningFormat())
																		  .setMaterial(report.getMaterial()) 
																		  .setDeptIn(deptIn), " ORDER BY D.id DESC LIMIT 0, 1 ");
				if(!primeDetail.isEmpty()){
					//期初数量
					report.setPrimeAmount(report.getPrimeAmount() + primeDetail.get(0).getRemaining());
					//期初金额
					report.setPrimeMoney(report.getPrimeMoney() + primeDetail.get(0).getRemaining() * report.getFinalPrice());
				
				}
				
				List<StockActionDetail> finalDetail = StockActionDetailDao.getByCond(dbCon, staff, new StockActionDetailDao.ExtraCond()
										.addStatus(StockAction.Status.AUDIT).addStatus(StockAction.Status.RE_AUDIT)
										.setOriStockDate(null, extraCond.range.getEndingFormat() + " 23:59:59 ")
										.setMaterial(report.getMaterial()), " ORDER BY D.id DESC LIMIT 0, 1 ");
				
				if(finalDetail.isEmpty()){
					//期末数量
					report.setFinalAmount(report.getFinalAmount() + finalDetail.get(0).getRemaining());
					//期末金额
					report.setFinalMoney(report.getFinalMoney() + finalDetail.get(0).getRemaining() * report.getFinalPrice());
				}			
			}

		}
		
		return result;
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
						" AND S.status IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.RE_AUDIT.getVal() + ") "+
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
					stockReport.setConsumption(amount);
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
							" AND S.status IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.RE_AUDIT.getVal() + ") "+
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
							" AND S.ori_stock_date < '" + begin + "'" + 
							" AND D.material_id = " + materialId + 
							" AND S.status IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.RE_AUDIT.getVal() + ") "+
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
					stockReport.setConsumption(amount);
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

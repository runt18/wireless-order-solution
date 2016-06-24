package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DateRange;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.stockMgr.StockReport;
import com.wireless.pojo.supplierMgr.Supplier;

public class StockReportDao {
	
	public static class ExtraCond{
		private DateRange range;
		private int deptId = -1;
		private int materialId;
		private int materialCateId;
		private MaterialCate.Type materialCateType;
		private int supplierId;
		
		
		public ExtraCond setMaterialCateType(MaterialCate.Type type){
			this.materialCateType = type;
			return this;
		}
		
		public ExtraCond setSupplier(Supplier supplier){
			this.supplierId = supplier.getId();
			return this;
		}
		
		public ExtraCond setSupplier(int supplierId){
			this.supplierId = supplierId;
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
			if(supplierId != 0){
				extraCond.append(" AND S.supplier_id = " + supplierId);
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
	public static List<StockReport> getByCond(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException, Exception{
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
	public static List<StockReport> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException, Exception{
//		String sql;
		
//		sql = " SELECT D.material_id, MAX(D.name) AS material_name " + 
//			  " ,MAX(M.price) AS material_price " +
//			  //" ,MAX(IFNULL(COST.cost, M.price)) AS material_price " +
//			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.STOCK_IN.getVal() + " ,D.amount, 0)) AS stock_in " +
//			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.STOCK_IN.getVal() + " ,D.amount * D.price, 0)) AS stock_in_money " +
//			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.INIT.getVal() + " ,D.amount, 0)) AS stock_init " +
//			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.STOCK_IN_TRANSFER.getVal() + (extraCond.deptId != -1 ? " AND S.dept_in = " + extraCond.deptId : "") + " , D.amount, 0)) AS stock_in_transfer " +
//			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.STOCK_IN_TRANSFER.getVal() + (extraCond.deptId != -1 ? " AND S.dept_out = " + extraCond.deptId : "") + " , D.amount, 0)) AS stock_out_transfer " +
//			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.SPILL.getVal() + " , D.amount, 0)) AS stock_spill " +
//			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.MORE.getVal() + " , D.amount, 0)) AS stock_take_more " +
//			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.STOCK_OUT.getVal() + " , D.amount, 0)) AS stock_out " +
//			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.STOCK_OUT.getVal() + " , D.amount * D.price, 0)) AS stock_out_money " +
//			  //" ,SUM(IF(S.sub_type = " + StockAction.SubType.STOCK_OUT_TRANSFER.getVal() + " , D.amount, 0)) AS stock_out_transfer " +
//			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.DAMAGE.getVal() + " , D.amount, 0)) AS stock_damage " +
//			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.LESS.getVal() + " , D.amount, 0)) AS stock_take_less " +
//			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.CONSUMPTION.getVal() + " , D.amount, 0)) AS stock_consumption " +
//			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.CONSUMPTION.getVal() + " , D.amount * D.price, 0)) AS stock_consume_price " +
//			  " FROM " + Params.dbName + ".stock_action_detail D " +
//			  " JOIN " + Params.dbName + ".stock_action S ON D.stock_action_id = S.id " +
//			  " JOIN " + Params.dbName + ".material M ON M.material_id = D.material_id " +
// 			  " JOIN " + Params.dbName + ".material_cate MC ON MC.cate_id = M.cate_id " +
//			  //" LEFT JOIN " + Params.dbName + ".monthly_cost COST ON M.material_id = COST.material_id " +
//			  //" LEFT JOIN " + Params.dbName + ".monthly_balance MB ON MB.id = COST.monthly_balance_id " +
//			  " WHERE 1 = 1 " +
//			  //( monthlyBalanceId > 0 ? " AND MB.id = " + monthlyBalanceId : "") +
////			  " AND MB.month BETWEEN '" + extraCond.range.getOpeningFormat() + "' AND '" + extraCond.range.getEndingFormat() + "'" +
//			  " AND S.restaurant_id = " + staff.getRestaurantId() +
//			  " AND S.status IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.RE_AUDIT.getVal() + ")" +
//			  (extraCond != null ? extraCond.toString() : "") +
//			  " GROUP BY D.material_id ";
//		
//		final List<StockReport> result = new ArrayList<>();
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		while(dbCon.rs.next()){
//			
//			Material material = new Material(dbCon.rs.getInt("material_id"));
//			material.setName(dbCon.rs.getString("material_name"));
//			
//			StockReport report = new StockReport();
//			report.setMaterial(material);
//			report.setFinalPrice(dbCon.rs.getFloat("material_price"));
//			
//			//期初建账数量
//			report.setPrimeAmount(dbCon.rs.getFloat("stock_init"));
//			report.setPrimeMoney(report.getPrimeAmount() * report.getFinalPrice());
//			report.setFinalAmount(report.getPrimeAmount());
//			report.setFinalMoney(report.getPrimeMoney());
//			
//			//入库采购
//			report.setStockIn(dbCon.rs.getFloat("stock_in"));
//			//入库采购金额
//			report.setStockInMoney(dbCon.rs.getFloat("stock_in_money"));
//			//入库调拨
//			report.setStockInTransfer(dbCon.rs.getFloat("stock_in_transfer"));
//			//入库报溢
//			report.setStockSpill(dbCon.rs.getFloat("stock_spill"));
//			//入库盘盈
//			report.setStockTakeMore(dbCon.rs.getFloat("stock_take_more"));
//			//出库退货
//			report.setStockOut(dbCon.rs.getFloat("stock_out"));
//			//出库退货金额
//			report.setStockOutMoney(dbCon.rs.getFloat("stock_out_money"));
//			//出库调拨
//			report.setStockOutTransfer(dbCon.rs.getFloat("stock_out_transfer"));
//			//出库报损
//			report.setStockDamage(dbCon.rs.getFloat("stock_damage"));
//			//出库盘亏
//			report.setStockTakeLess(dbCon.rs.getFloat("stock_take_less"));
//			//出库消耗
//			report.setConsumption(dbCon.rs.getFloat("stock_consumption"));
//			//销售金额
//			report.setComsumeMoney(dbCon.rs.getFloat("stock_consume_price"));
//			
//			result.add(report);
//			
//		}
//		dbCon.rs.close();
		
		
//		//获取本月之前期末数量不为0的商品和原料
//		List<StockActionDetail> detailWithPrime = StockActionDetailDao.getByCond(dbCon, staff, new StockActionDetailDao.ExtraCond()
//																								.addStatus(StockAction.Status.AUDIT).addStatus(StockAction.Status.RE_AUDIT)
//																								.setOriStockDate(null, extraCond.range.getOpeningFormat()), 
//																								" GROUP BY D.material_id HAVING(D.dept_in_remaining) > 0 ");
		
		final List<StockReport> result = getRangeStockByCond(dbCon, staff, extraCond);
		
		String sql;
		//获取本月之前期末数量不为0的商品和原料
		sql = " SELECT * FROM " + Params.dbName + ".stock_action_detail " +
		      " WHERE 1 = 1 " +
		      " AND id IN ( " +
			      " SELECT MAX(D.id) AS last_id FROM wireless_order_db.stock_action_detail D " +
			      " JOIN wireless_order_db.stock_action S ON D.stock_action_id = S.id " +
			      " JOIN wireless_order_db.material M ON D.material_id = M.material_id " + 
//			      " JOIN wireless_order_db.material_cate MC ON S.cate_type = MC.type AND MC.cate_id = M.cate_id " +
			      " WHERE 1 = 1 " + 
			      " AND S.`status` IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.RE_AUDIT.getVal() + ") " +
			      " AND S.restaurant_id = " + staff.getRestaurantId() +
			      " AND S.approve_date <= '" + extraCond.range.getEndingFormat() + "'" + 
			      (extraCond.deptId >= 0 ? " AND (S.dept_in = " + extraCond.deptId + " OR S.dept_out = " + extraCond.deptId + ")" : "") + 
			      (extraCond.materialCateType != null ? (" AND S.cate_type = " + extraCond.materialCateType.getValue()) : "") +
			      (extraCond.materialCateId != 0 ? (" AND M.cate_id = " + extraCond.materialCateId) : "") +
			      " GROUP BY D.material_id " +
		      ")" +
		      " AND remaining > 0 "; 

		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		//需要追加统计本月之前初数量不为0，但是本月没有做过出入库任务的商品和原料
		while(dbCon.rs.next()){
			boolean isExist = false;
			for(StockReport eachReport : result){
				if(eachReport.getMaterial().getId() == dbCon.rs.getInt("material_id")){
					isExist = true;
					break;
				}
			}
			
			if(!isExist){
				try {
					Material material = MaterialDao.getById(staff, dbCon.rs.getInt("material_id"));
					StockReport report = new StockReport();
					report.setMaterial(material);
					report.setFinalPrice(material.getPrice());
					
					result.add(report);
				} catch (BusinessException e) {
					e.printStackTrace();
				}
			}
		}
		
			  
		for(StockReport report : result){
			
			/**
			 * 获取最后一张已审核 或 反审核的物品库单 
			 */
			List<StockActionDetail> primeDetail = StockActionDetailDao.getByCond(dbCon, staff, new StockActionDetailDao.ExtraCond()
																					.addStatus(StockAction.Status.AUDIT).addStatus(StockAction.Status.RE_AUDIT)
																					.setOriDate(null, extraCond.range.getOpeningFormat())
																					.setMaterial(report.getMaterial())
																					.setDept(extraCond.deptId), " ORDER BY D.id DESC LIMIT 0, 1 ");
			if(!primeDetail.isEmpty()){
				float primeAmount = 0;
				if(extraCond.deptId == -1){
					primeAmount = primeDetail.get(0).getRemaining();
				}else{
					StockAction primeStock = StockActionDao.getById(dbCon, staff, primeDetail.get(0).getStockActionId());
					if(primeStock.getDeptIn().getId() == extraCond.deptId){
						primeAmount = primeDetail.get(0).getDeptInRemaining();
					}else if(primeStock.getDeptOut().getId() == extraCond.deptId){
						primeAmount = primeDetail.get(0).getDeptOutRemaining();
					}
				}
				//期初数量
				report.setPrimeAmount(primeAmount);
				
				
				//TODO
//				List<MonthlyBalance> monthlyBalance = MonthlyBalanceDao.getMonthlyBalance(staff, new MonthlyBalanceDao.ExtraCond().setRange(extraCond.range.getOpeningFormat()), " ORDER BY month DESC");
//				
//				if(monthlyBalance.size() > 0){
////					monthlyBalance.get(0);
//				}
				
				//期初金额
				report.setPrimeMoney(primeAmount * report.getFinalPrice());
			}
			
			
			/**
			 * 获取搜索时间内  最后一张库单来判定期末数
			 */
			List<StockActionDetail> finalDetail = StockActionDetailDao.getByCond(dbCon, staff, new StockActionDetailDao.ExtraCond()
																						.addStatus(StockAction.Status.AUDIT).addStatus(StockAction.Status.RE_AUDIT)
																						.setOriDate(null, extraCond.range.getEndingFormat() + " 23:59:59 ")
																						.setMaterial(report.getMaterial())
																						.setDept(extraCond.deptId), " ORDER BY D.id DESC LIMIT 0, 1 ");
			
			if(!finalDetail.isEmpty()){
				float finalAmount = 0;
				if(extraCond.deptId == -1){
					finalAmount = finalDetail.get(0).getRemaining();
				}else{
					StockAction finalStock = StockActionDao.getById(dbCon, staff, finalDetail.get(0).getStockActionId());
					if(finalStock.getDeptIn().getId() == extraCond.deptId){
						finalAmount = finalDetail.get(0).getDeptInRemaining();
					}else if(finalStock.getDeptOut().getId() == extraCond.deptId){
						finalAmount = finalDetail.get(0).getDeptOutRemaining();
					}
				}
				//期末数量
				report.setFinalAmount(finalAmount);
				//期末金额
				report.setFinalMoney(finalAmount * report.getFinalPrice());

				
				/**
				 * 判断是否有月结
				 */
				sql = " SELECT id FROM " + Params.dbName + ".monthly_balance " + 
						" WHERE 1 = 1 " +
						" AND month BETWEEN '" + extraCond.range.getOpeningFormat() + "' AND '" + extraCond.range.getEndingFormat() + "'" + 
						" ORDER BY id DESC LIMIT 1";
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				int monthlyBalanceId = 0;
				if(dbCon.rs.next()){
					monthlyBalanceId = dbCon.rs.getInt("id");
				}
				dbCon.rs.close();
				
				
				/**
				 * 如果有月结  就用月结记录的成本计算期初期末金额
				 */
				if(monthlyBalanceId > 0){
					sql = " SELECT M.month, M.restaurant_id, C.material_id, C.cost FROM " + Params.dbName + ".monthly_balance M " + 
							" JOIN " + Params.dbName + ".monthly_cost C ON M.id = C.monthly_balance_id " + 
							" WHERE 1 = 1 " + 
							" AND M.id = " + monthlyBalanceId + 
							" AND C.material_id = " + report.getMaterial().getId();
					
					dbCon.rs = dbCon.stmt.executeQuery(sql);
					if(dbCon.rs.next()){
						float cost = dbCon.rs.getFloat("cost");
						report.setFinalPrice(cost);
						report.setPrimeMoney(report.getPrimeAmount() * cost);
						report.setFinalMoney(report.getFinalAmount() * cost);
					}
				}
			}
		}
		
		return result;
	}
	
	
	
	
	
	/**
	 * get StockReport without primeMoney and finalMoney by extraCond in range
	 * @param dbCon
	 * @param staff
	 * @param extraCond
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 * @throws Exception
	 */
	public static List<StockReport> getRangeStockByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException, Exception{
		String sql;
		
		sql = " SELECT D.material_id, MAX(D.name) AS material_name " + 
			  " ,MAX(M.price) AS material_price " +
			  //" ,MAX(IFNULL(COST.cost, M.price)) AS material_price " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.STOCK_IN.getVal() + " ,D.amount, 0)) AS stock_in " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.STOCK_IN.getVal() + " ,D.amount * D.price, 0)) AS stock_in_money " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.INIT.getVal() + " ,D.amount, 0)) AS stock_init " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.STOCK_IN_TRANSFER.getVal() + (extraCond.deptId != -1 ? " AND S.dept_in = " + extraCond.deptId : "") + " , D.amount, 0)) AS stock_in_transfer " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.STOCK_IN_TRANSFER.getVal() + (extraCond.deptId != -1 ? " AND S.dept_out = " + extraCond.deptId : "") + " , D.amount, 0)) AS stock_out_transfer " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.SPILL.getVal() + " , D.amount, 0)) AS stock_spill " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.MORE.getVal() + " , D.amount, 0)) AS stock_take_more " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.STOCK_OUT.getVal() + " , D.amount, 0)) AS stock_out " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.STOCK_OUT.getVal() + " , D.amount * D.price, 0)) AS stock_out_money " +
			  //" ,SUM(IF(S.sub_type = " + StockAction.SubType.STOCK_OUT_TRANSFER.getVal() + " , D.amount, 0)) AS stock_out_transfer " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.DAMAGE.getVal() + " , D.amount, 0)) AS stock_damage " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.LESS.getVal() + " , D.amount, 0)) AS stock_take_less " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.CONSUMPTION.getVal() + " , D.amount, 0)) AS stock_consumption " +
			  " ,SUM(IF(S.sub_type = " + StockAction.SubType.CONSUMPTION.getVal() + " , D.amount * D.price, 0)) AS stock_consume_price " +
			  " FROM " + Params.dbName + ".stock_action_detail D " +
			  " JOIN " + Params.dbName + ".stock_action S ON D.stock_action_id = S.id " +
			  " LEFT JOIN " + Params.dbName + ".material M ON M.material_id = D.material_id " +
 			  " LEFT JOIN " + Params.dbName + ".material_cate MC ON MC.cate_id = M.cate_id " +
			  //" LEFT JOIN " + Params.dbName + ".monthly_cost COST ON M.material_id = COST.material_id " +
			  //" LEFT JOIN " + Params.dbName + ".monthly_balance MB ON MB.id = COST.monthly_balance_id " +
			  " WHERE 1 = 1 " +
			  //( monthlyBalanceId > 0 ? " AND MB.id = " + monthlyBalanceId : "") +
//			  " AND MB.month BETWEEN '" + extraCond.range.getOpeningFormat() + "' AND '" + extraCond.range.getEndingFormat() + "'" +
			  " AND S.restaurant_id = " + staff.getRestaurantId() +
			  " AND S.status IN (" + StockAction.Status.AUDIT.getVal() + "," + StockAction.Status.RE_AUDIT.getVal() + ")" +
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
			
			//期初建账数量
//			report.setPrimeAmount(dbCon.rs.getFloat("stock_init"));
//			report.setPrimeMoney(report.getPrimeAmount() * report.getFinalPrice());
//			report.setFinalAmount(report.getPrimeAmount());
//			report.setFinalMoney(report.getPrimeMoney());
			
			//入库采购
			report.setStockIn(dbCon.rs.getFloat("stock_in"));
			//入库采购金额
			report.setStockInMoney(dbCon.rs.getFloat("stock_in_money"));
			//入库调拨
			report.setStockInTransfer(dbCon.rs.getFloat("stock_in_transfer"));
			//入库报溢
			report.setStockSpill(dbCon.rs.getFloat("stock_spill"));
			//入库盘盈
			report.setStockTakeMore(dbCon.rs.getFloat("stock_take_more"));
			//出库退货
			report.setStockOut(dbCon.rs.getFloat("stock_out"));
			//出库退货金额
			report.setStockOutMoney(dbCon.rs.getFloat("stock_out_money"));
			//出库调拨
			report.setStockOutTransfer(dbCon.rs.getFloat("stock_out_transfer"));
			//出库报损
			report.setStockDamage(dbCon.rs.getFloat("stock_damage"));
			//出库盘亏
			report.setStockTakeLess(dbCon.rs.getFloat("stock_take_less"));
			//出库消耗
			report.setConsumption(dbCon.rs.getFloat("stock_consumption"));
			//销售金额
			report.setComsumeMoney(dbCon.rs.getFloat("stock_consume_price"));
			
			result.add(report);
			
		}
		dbCon.rs.close();
		return result;
	}
	
	
	/**
	 * 
	 * @param staff
	 * @param extraCond
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 * @throws Exception
	 */
	public static List<StockReport> getRangeStockByCond(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException, Exception{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			return getRangeStockByCond(dbCon, staff, extraCond);
		} finally {
			dbCon.disconnect();
		}
	}
	
}
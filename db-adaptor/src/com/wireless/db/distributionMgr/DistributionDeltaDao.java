package com.wireless.db.distributionMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.distributionMgr.DistributionDelta;
import com.wireless.pojo.distributionMgr.StockDistribution;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;

public class DistributionDeltaDao {
	
	public static class ExtraCond{
		private Integer materialId;
		private Integer distributionId;
		private Integer minDeltaAmount;
		private Integer maxDeltaAmount;
		private String beginDate;
		private String endDate;
		
		public ExtraCond setMinDeltaAmount(Integer minDeltaAmount) {
			this.minDeltaAmount = minDeltaAmount;
			return this;
		}

		public ExtraCond setMaxDeltaAmount(Integer maxDeltaAmount) {
			this.maxDeltaAmount = maxDeltaAmount;
			return this;
		}

		public ExtraCond setMaterialId(int materialId){
			this.materialId = materialId;
			return this;
		}
		
		public ExtraCond setDistributionId(int distributionId){
			this.distributionId = distributionId;
			return this;
		}
		
		public ExtraCond setRange(String beginDate, String endDate){
			this.beginDate = beginDate;
			this.endDate = endDate;
			return this;
		}
		
		@Override
		public String toString() {
			StringBuilder extraCond = new StringBuilder();
			if(materialId != null){
				extraCond.append(" AND (D.material_id = " + this.materialId + " OR D.associate_id = " + this.materialId + ")");
			}
			
			if(distributionId != null){
				extraCond.append(" AND SD.id = " + this.distributionId);
			}
			
			if(beginDate != null && endDate != null){
				extraCond.append(" AND S.ori_stock_date BETWEEN '" + this.beginDate + "' AND '" + this.endDate + "' ");
			}
			return extraCond.toString();
		}
	}
	
	public static List<DistributionDelta> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		
		String distributionSendSql = " SELECT SD.id AS send_id, D.material_id AS material_id, D.name " + 
									 " FROM " + Params.dbName + ".stock_distribution SD " + 
									 " JOIN " + Params.dbName + ".stock_action S ON SD.stock_action_id = S.id " +
									 " JOIN " + Params.dbName + ".stock_action_detail D ON SD.stock_action_id = D.stock_action_id " +
									 " WHERE 1 = 1 " +
									 " AND S.sub_type = " + StockAction.SubType.DISTRIBUTION_SEND.getVal() +
									 " AND S.status <> " + StockAction.Status.UNAUDIT.getVal() + 
									 (extraCond != null ? extraCond.toString() : "");
		
		String distributionReceiveSql = " SELECT SD.associate_id AS send_id, D.associate_id AS material_id, D.name " + 
									 	" FROM " + Params.dbName + ".stock_distribution SD " + 
									 	" JOIN " + Params.dbName + ".stock_action S ON SD.stock_action_id = S.id " +
									 	" JOIN " + Params.dbName + ".stock_action_detail D ON SD.stock_action_id = D.stock_action_id " +
									 	" WHERE 1 = 1 " +
									 	" AND sub_type = " + StockAction.SubType.DISTRIBUTION_RECEIVE.getVal() +
									 	" AND S.status <> " + StockAction.Status.UNAUDIT.getVal() + 
									 	(extraCond != null ? extraCond.toString() : "");
		
		String distributionReturnSql = " SELECT SD.associate_id AS send_id, D.associate_id AS material_id, D.name " + 
									   " FROM " + Params.dbName + ".stock_distribution SD " + 
									   " JOIN " + Params.dbName + ".stock_action S ON SD.stock_action_id = S.id " +
									   " JOIN " + Params.dbName + ".stock_action_detail D ON SD.stock_action_id = D.stock_action_id " +
									   " WHERE 1 = 1 " +
									   " AND sub_type = " + StockAction.SubType.DISTRIBUTION_RETURN.getVal() +
									   " AND S.status <> " + StockAction.Status.UNAUDIT.getVal() + 
									   (extraCond != null ? extraCond.toString() : "");
		
		String distributionRecoverySql = " SELECT SD.associate_id AS send_id, D.material_id AS material_id, D.name " + 
										 " FROM " + Params.dbName + ".stock_distribution SD " + 
										 " JOIN " + Params.dbName + ".stock_action S ON SD.stock_action_id = S.id " +
										 " JOIN " + Params.dbName + ".stock_action_detail D ON SD.stock_action_id = D.stock_action_id " +
										 " WHERE 1 = 1 " +
										 " AND sub_type = " + StockAction.SubType.DISTRIBUTION_RECOVERY.getVal() +
										 " AND S.status <> " + StockAction.Status.UNAUDIT.getVal() + 
										 (extraCond != null ? extraCond.toString() : "");
		
		
		String sql;
		sql = " SELECT TMP.send_id, TMP.material_id, MAX(TMP.name) AS name FROM (" + 
			  distributionSendSql + 
			  " UNION ALL " +
			  distributionReceiveSql + 
			  " UNION ALL " +
			  distributionReturnSql +
			  " UNION ALL " +
			  distributionRecoverySql + 
			  ") AS TMP " + 
			  " GROUP BY send_id, material_id";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<DistributionDelta> result = new ArrayList<>();
		while(dbCon.rs.next()){
			DistributionDelta distributionDelta = new DistributionDelta();
			distributionDelta.setSendId(dbCon.rs.getInt("send_id"));
			distributionDelta.setMaterial(new Material(dbCon.rs.getInt("material_id")));
			distributionDelta.getMaterial().setName(dbCon.rs.getString("name"));
			result.add(distributionDelta);
		}
		dbCon.rs.close();
		
		for(DistributionDelta distributionDelta : result){
			//发货单号&数量
			sql = " SELECT SD.id, D.amount FROM " + Params.dbName + ".stock_distribution SD " + 
				  " JOIN " + Params.dbName + ".stock_action_detail D ON SD.stock_action_id = D.stock_action_id " + 
				  " JOIN " + Params.dbName + ".stock_action S ON SD.stock_action_id = S.id " +
				  " WHERE 1 = 1 " + 
				  " AND S.sub_type = " + StockAction.SubType.DISTRIBUTION_SEND.getVal() +
				  " AND S.status <> " + StockAction.Status.UNAUDIT.getVal() + 
				  " AND SD.id = " + distributionDelta.getSendId() + 
				  " AND D.material_id = " + distributionDelta.getMaterial().getId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			
			while(dbCon.rs.next()){
				distributionDelta.addDistributionSends(new StockDistribution(dbCon.rs.getInt("id")));
				distributionDelta.setDistributionSendAmount(distributionDelta.getDistributionSendAmount() + dbCon.rs.getFloat("amount"));
			}
			dbCon.rs.close();
			
			//收货单号&数量
			sql = " SELECT SD.id, D.amount FROM " + Params.dbName + ".stock_distribution SD " + 
				  " JOIN " + Params.dbName + ".stock_action_detail D ON SD.stock_action_id = D.stock_action_id " + 
				  " JOIN " + Params.dbName + ".stock_action S ON SD.stock_action_id = S.id " +
				  " WHERE 1 = 1 " + 
				  " AND S.sub_type = " + StockAction.SubType.DISTRIBUTION_RECEIVE.getVal() +
				  " AND S.status <> " + StockAction.Status.UNAUDIT.getVal() + 
				  " AND SD.associate_id = " + distributionDelta.getSendId() + 
				  " AND D.associate_id = " + distributionDelta.getMaterial().getId();
				
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			while(dbCon.rs.next()){
				distributionDelta.addDistributionReceives(new StockDistribution(dbCon.rs.getInt("id")));
				distributionDelta.setDistributionReceiveAmount(distributionDelta.getDistributionReceiveAmount() + dbCon.rs.getFloat("amount"));
			}
			dbCon.rs.close();
			
			//退货单号&数量
			sql = " SELECT SD.id, D.amount FROM " + Params.dbName + ".stock_distribution SD " + 
				  " JOIN " + Params.dbName + ".stock_action_detail D ON SD.stock_action_id = D.stock_action_id " + 
				  " JOIN " + Params.dbName + ".stock_action S ON SD.stock_action_id = S.id " +
				  " WHERE 1 = 1 " + 
				  " AND S.sub_type = " + StockAction.SubType.DISTRIBUTION_RETURN.getVal() +
				  " AND S.status <> " + StockAction.Status.UNAUDIT.getVal() + 
				  " AND SD.associate_id = " + distributionDelta.getSendId() + 
				  " AND D.associate_id = " + distributionDelta.getMaterial().getId();
				
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			while(dbCon.rs.next()){
				//退货单号
				distributionDelta.addDistributionReturns(new StockDistribution(dbCon.rs.getInt("id")));
				//退货数量
				distributionDelta.setDistributionReturnAmount(distributionDelta.getDistributionReturnAmount() + dbCon.rs.getFloat("amount"));
			}
			dbCon.rs.close();
			
			//回收单号 数量
			sql = " SELECT SD.id, D.amount FROM " + Params.dbName + ".stock_distribution SD " + 
				  " JOIN " + Params.dbName + ".stock_action_detail D ON SD.stock_action_id = D.stock_action_id " + 
				  " JOIN " + Params.dbName + ".stock_action S ON SD.stock_action_id = S.id " +
				  " WHERE 1 = 1 " + 
				  " AND S.sub_type = " + StockAction.SubType.DISTRIBUTION_RECOVERY.getVal() +
				  " AND S.status <> " + StockAction.Status.UNAUDIT.getVal() + 
				  " AND SD.associate_id = " + distributionDelta.getSendId() + 
				  " AND D.material_id = " + distributionDelta.getMaterial().getId();
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			while(dbCon.rs.next()){
				distributionDelta.addDistributionRecoverys(new StockDistribution(dbCon.rs.getInt("id")));
				distributionDelta.setDistributionRecoveryAmount(distributionDelta.getDistributionRecoveryAmount() + dbCon.rs.getFloat("amount"));
			}
			dbCon.rs.close();
			
		}
		
		if(extraCond.minDeltaAmount != null && extraCond.maxDeltaAmount != null){
			Iterator<DistributionDelta> iter = result.iterator();
			while(iter.hasNext()){
				DistributionDelta delta = iter.next();
				float deltaAmount = Math.abs((delta.getDistributionSendAmount() - delta.getDistributionReceiveAmount()));
				if(deltaAmount < extraCond.minDeltaAmount || deltaAmount > extraCond.maxDeltaAmount){
					iter.remove();
				}
			}
		}else if(extraCond.minDeltaAmount != null && extraCond.maxDeltaAmount == null){
			Iterator<DistributionDelta> iter = result.iterator();
			while(iter.hasNext()){
				DistributionDelta delta = iter.next();
				float deltaAmount = Math.abs((delta.getDistributionSendAmount() - delta.getDistributionReceiveAmount()));
				if(deltaAmount < extraCond.minDeltaAmount){
					iter.remove();
				}
			}
		}else if(extraCond.minDeltaAmount == null && extraCond.maxDeltaAmount != null){
			Iterator<DistributionDelta> iter = result.iterator();
			while(iter.hasNext()){
				DistributionDelta delta = iter.next();
				float deltaAmount = Math.abs((delta.getDistributionSendAmount() - delta.getDistributionReceiveAmount()));
				if(deltaAmount > extraCond.maxDeltaAmount){
					iter.remove();
				}
			}
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param staff
	 * @param extraCond
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static List<DistributionDelta> getByCond(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		} finally{
			dbCon.disconnect();
		}
	}
	
//	public static void main(String[] args) throws SQLException, BusinessException {
//		Staff staff = StaffDao.getAdminByRestaurant(40);
//		System.out.println(DistributionDeltaDao.getByCond(staff, null));
//	}
}

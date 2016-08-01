package com.wireless.db.distributionMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.distributionMgr.DistributionDetailReport;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;

public class DistributionDetailReportDao {
	public static class ExtraCond{
		private String beginDate;
		private String endDate;
		private Integer associateId;
		private List<StockAction.SubType> subTypes = new ArrayList<>();
		private List<StockAction.SubType> exceptSubTypes = new ArrayList<>();
		private Integer materialId;
		private Integer stockOutRestaurant;
		private Integer stockInRestaurant;
		private Integer start;
		private Integer limit;
		private boolean isOnlyAmount;
		private List<StockAction.Type> types = new ArrayList<>();
		private String comment;
		private MaterialCate.Type materialType;
		private Integer materialCateId;
		private Integer fuzzyId;
		private boolean isOnlySum;
		
		public ExtraCond addExceptSubTypes(StockAction.SubType subType){
			this.exceptSubTypes.add(subType);
			return this;
		}
		
		public ExtraCond setIsOnlySum(boolean onOff){
			this.isOnlySum = onOff;
			return this;
		}
		
		public ExtraCond setFuzzyId(int fuzzyId){
			this.fuzzyId = fuzzyId;
			return this;
		}
		
		public ExtraCond setMaterialCateId(int cateId){
			this.materialCateId = cateId;
			return this;
		}
		
		public ExtraCond addType(StockAction.Type type){
			this.types.add(type);
			return this;
		}
		
		public ExtraCond setComment(String comment){
			this.comment = comment;
			return this;
		}
		
		public ExtraCond setMaterialType(MaterialCate.Type type){
			this.materialType = type;
			return this;
		}
		
		public ExtraCond setIsOnlyAmount(boolean onOff){
			this.isOnlyAmount = onOff;
			return this;
		}
		
		public ExtraCond setLimit(int start, int limit){
			this.start = start;
			this.limit = limit;
			return this;
		}
		
		public ExtraCond setBeginDate(String beginDate){
			this.beginDate = beginDate;
			return this;
		}
		
		public ExtraCond setEndDate(String endDate){
			this.endDate = endDate;
			return this;
		}
		
		public ExtraCond setAssociateId(Integer associateId){
			this.associateId = associateId;
			return this;
		}
		
		public ExtraCond setMaterialId(Integer materialId){
			this.materialId = materialId;
			return this;
		}
		
		public ExtraCond setStockOutRestaurant(Integer stockOutRestaurantId){
			this.stockOutRestaurant = stockOutRestaurantId;
			return this;
		}
		
		public ExtraCond setStockInRestaurant(Integer stockInRestaurantId){
			this.stockOutRestaurant = stockInRestaurantId;
			return this;
		}
		
		public ExtraCond addSubType(StockAction.SubType subType){
			subTypes.add(subType);
			return this;
		}
		
		@Override
		public String toString() {
			StringBuilder extraCond = new StringBuilder();
			if(this.associateId != null){
				extraCond.append(" AND SD.associate_id = " + this.associateId);
			}
			
			if(this.materialId != null){
				extraCond.append(" AND M.material_id = " + this.materialId);
			}
			
			if(this.stockInRestaurant != null){
				extraCond.append(" AND SD.stock_in_restaurant = " + this.stockInRestaurant);
			}
			
			if(this.stockOutRestaurant != null){
				extraCond.append(" AND SD.stock_out_restaurant = " + this.stockOutRestaurant);
			}
			
			if(this.fuzzyId != null){
				extraCond.append(" AND (SD.id LIKE '%" + this.fuzzyId + "%' OR SD.associate_id LIKE '%" + this.fuzzyId + "%' OR S.id LIKE '%" + this.fuzzyId +"%') ");
			}
			
			if(!this.subTypes.isEmpty()){
				StringBuilder subTypeVals = new StringBuilder();
				for(StockAction.SubType subType : subTypes){
					if(subTypeVals.length() == 0){
						subTypeVals.append(subType.getVal());
					}else{
						subTypeVals.append(", " + subType.getVal());
					}
				}
				if(subTypeVals.length() != 0){
					extraCond.append(" AND S.sub_type IN( " + subTypeVals.toString() + ") ");
				}
			}
			
			if(!this.exceptSubTypes.isEmpty()){
				StringBuilder subTypeVals = new StringBuilder();
				for(StockAction.SubType subType : exceptSubTypes){
					if(subTypeVals.length() == 0){
						subTypeVals.append(subType.getVal());
					}else{
						subTypeVals.append(", " + subType.getVal());
					}
				}
				if(subTypeVals.length() != 0){
					extraCond.append(" AND S.sub_type NOT IN( " + subTypeVals.toString() + ") ");
				}
			}
			
			if(this.beginDate != null && this.endDate != null){
				extraCond.append(" AND S.ori_stock_date BETWEEN '" + this.beginDate + "' AND '" + this.endDate + "' ");
				
			}else if(this.beginDate != null && this.endDate == null){
				extraCond.append(" AND S.ori_stock_date > '" + this.beginDate + "' ");
		
			}else if(this.endDate != null && this.beginDate == null){
				extraCond.append(" AND S.ori_stock_date < '" + this.endDate + "' ");
			
			}
			
			if(this.comment != null){
				extraCond.append(" AND S.comment LIKE '%" + this.comment + "%' ");
			}

			if(this.materialType != null){
				extraCond.append(" AND MC.type = " + this.materialType.getValue());
			}

			if(this.materialCateId != null){
				extraCond.append(" AND MC.cate_id = " + this.materialCateId);
			}
			
			if(!this.types.isEmpty()){
				StringBuilder typeVals = new StringBuilder();
				for(StockAction.Type type : types){
					if(typeVals.length() == 0){
						typeVals.append(type.getVal());
					}else{
						typeVals.append(", " + type.getVal());
					}
				}
				
				if(typeVals.length() != 0){
					extraCond.append(" AND S.type IN( " + typeVals.toString() + ") ");
				}
			}
			return extraCond.toString();
		}
		
	}
	
	/**
	 * get the DistirbutionDetailReport by extraCond
	 * @param staff
	 * @param extraCond
	 * @param orderCaluse
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static List<DistributionDetailReport> getByCond(Staff staff, ExtraCond extraCond, String orderCaluse) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond, orderCaluse);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * get the DistirbutionDetailReport by extraCond
	 * @param dbCon
	 * @param staff
	 * @param extraCond
	 * @param orderCaluse
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static List<DistributionDetailReport> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderCaluse) throws SQLException, BusinessException{
		String sql;
		sql = " SELECT " +
			  (extraCond.isOnlySum ? 
			  " SUM(IF(S.type = " + StockAction.Type.STOCK_IN.getVal() + ", D.amount, 0)) AS stock_in_amount " + 
			  ", SUM(IF(S.type = " + StockAction.Type.STOCK_IN.getVal() + ", D.amount * D.price, 0)) AS stock_in_price " +
			  ", SUM(IF(S.type = " + StockAction.Type.STOCK_OUT.getVal() + ", D.amount, 0)) AS stock_out_amount " + 
			  ", SUM(IF(S.type = " + StockAction.Type.STOCK_OUT.getVal() + ", D.amount * D.price, 0)) AS stock_out_price ":
			  (extraCond.isOnlyAmount ? " COUNT(*) " : " D.stock_action_id, SD.id, SD.associate_id, S.sub_type, S.ori_stock_date, S.operator_id, S.operator, SD.stock_out_restaurant, SD.stock_in_restaurant, D.price, M.name, M.material_id " + 
			  ", IF(S.type = " + StockAction.Type.STOCK_IN.getVal() + ", D.amount, 0) AS stock_in_amount " +
			  ", IF(S.type = " + StockAction.Type.STOCK_IN.getVal() + ", D.amount * D.price, 0) AS stock_in_price " +
			  ", IF(S.type = " + StockAction.Type.STOCK_OUT.getVal() + ", D.amount, 0) AS stock_out_amount " +
			  ", IF(S.type = " + StockAction.Type.STOCK_OUT.getVal() + ", D.amount * D.price, 0) AS stock_out_price ")) + 
			  " FROM " + Params.dbName + ".stock_action_detail D " +
			  " JOIN " + Params.dbName + ".stock_action S ON S.id = D.stock_action_id " + 
			  " JOIN " + Params.dbName + ".stock_distribution SD ON SD.stock_action_id = S.id " + 
			  " LEFT JOIN " + Params.dbName + ".material M ON D.material_id = M.material_id " +
			  " LEFT JOIN " + Params.dbName + ".material_cate MC ON M.cate_id = MC.cate_id " +
			  " WHERE 1 = 1 " + 
			  " AND S.restaurant_id = " + staff.getRestaurantId() + 
			  " AND S.status NOT IN(" + StockAction.Status.UNAUDIT.getVal() + ")" +
			  (extraCond != null ? extraCond.toString() : "") +
			  (extraCond.start != null && extraCond.limit != null ? " LIMIT " + extraCond.start + ", " + extraCond.limit : "") +
			  (orderCaluse != null ? orderCaluse : " ORDER BY D.id DESC");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<DistributionDetailReport> result;
		
		if(extraCond.isOnlyAmount){
			if(dbCon.rs.next()){
				result = Collections.nCopies(dbCon.rs.getInt(1), null);
			}else{
				result = Collections.emptyList();
			}
		}else if(extraCond.isOnlySum){
			result = new ArrayList<>();
			while(dbCon.rs.next()){
				DistributionDetailReport detailReport = new DistributionDetailReport();
				detailReport.setStockInAmount(dbCon.rs.getFloat("stock_in_amount"));
				detailReport.setStockInMoney(dbCon.rs.getFloat("stock_in_price"));
				detailReport.setStockOutAmount(dbCon.rs.getFloat("stock_out_amount"));
				detailReport.setStockOutMoney(dbCon.rs.getFloat("stock_out_price"));
				
				result.add(detailReport);
			}
		}else{
			result = new ArrayList<>();
			while(dbCon.rs.next()){
				DistributionDetailReport detailReport = new DistributionDetailReport();
				detailReport.setId(dbCon.rs.getInt("id"));
				if(dbCon.rs.getTimestamp("ori_stock_date") != null){
					detailReport.setOriStockDate(dbCon.rs.getTimestamp("ori_stock_date").getTime());
				}
				detailReport.setAssociateId(dbCon.rs.getInt("associate_id"));
				detailReport.setMaterial(new Material(dbCon.rs.getInt("material_id")));
				detailReport.getMaterial().setName(dbCon.rs.getString("name"));
				detailReport.setSubType(StockAction.SubType.valueOf(dbCon.rs.getInt("sub_type")));
				detailReport.setStockInRestaurant(new Restaurant(dbCon.rs.getInt("stock_in_restaurant")));
				detailReport.setStockOutRestaurant(new Restaurant(dbCon.rs.getInt("stock_out_restaurant")));
				detailReport.setStockActionId(dbCon.rs.getInt("stock_action_id"));
				detailReport.setSubType(StockAction.SubType.valueOf(dbCon.rs.getInt("sub_type")));
				detailReport.setOperatorId(dbCon.rs.getInt("operator_id"));
				detailReport.setOperator(dbCon.rs.getString("operator"));
				detailReport.setStockInAmount(dbCon.rs.getFloat("stock_in_amount"));
				detailReport.setStockInMoney(dbCon.rs.getFloat("stock_in_price"));
				detailReport.setStockOutAmount(dbCon.rs.getFloat("stock_out_amount"));
				detailReport.setStockOutMoney(dbCon.rs.getFloat("stock_out_price"));
				
				result.add(detailReport);
			}
			dbCon.rs.close();
			
			for(DistributionDetailReport detail : result){
				detail.setStockInRestaurant(RestaurantDao.getById(dbCon, detail.getStockInRestaurant().getId()));
				detail.setStockOutRestaurant(RestaurantDao.getById(dbCon, detail.getStockOutRestaurant().getId()));
			}
		}
		return result;
	}
}

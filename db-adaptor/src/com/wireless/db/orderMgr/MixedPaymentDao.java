package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.Map.Entry;

import com.wireless.db.DBCon;
import com.wireless.db.DBTbl;
import com.wireless.db.Params;
import com.wireless.pojo.dishesOrder.MixedPayment;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;

public class MixedPaymentDao {

	public static class ExtraCond{
		private final String mixedPaymentTbl;
		private final int orderId;
		
		public ExtraCond(DateType dateType, Order order){
			this(dateType, order.getId());
		}
		
		public ExtraCond(DateType dateType, int orderId){
			DBTbl dbTbl = new DBTbl(dateType);
			this.mixedPaymentTbl = dbTbl.mixedTbl;
			this.orderId = orderId;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(orderId != 0){
				extraCond.append(" AND order_id = " + orderId);
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Insert the mixed payment according to specific builder {@link MixedPayment#InsertBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the mixed payment builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	static void insert(DBCon dbCon, Staff staff, MixedPayment.InsertBuilder builder) throws SQLException{
		MixedPayment mixedPayment = builder.build();
		
		deleteByCond(dbCon, staff, new ExtraCond(DateType.TODAY, mixedPayment.getOrderId()));
		
		for(Entry<PayType, Float> entry : mixedPayment.getPayments().entrySet()){
			String sql;
			sql = " INSERT INTO " + Params.dbName + ".mixed_payment" +
				  " (order_id, pay_type_id, price) VALUES( " +
				  mixedPayment.getOrderId() + "," +
				  entry.getKey().getId() + "," +
				  entry.getValue().floatValue() +
				  " ) ";
			dbCon.stmt.executeUpdate(sql);
		}
	}
	
	/**
	 * Get the mixed payment according to extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return 
	 * @throws SQLException
	 */
	static MixedPayment getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		
		MixedPayment result = new MixedPayment();
		
		result.setOrderId(extraCond.orderId);
		
		String sql;
		sql = " SELECT PT.pay_type_id, IFNULL(PT.name, '其他') AS pay_type_name, MP.price FROM " + Params.dbName + "." + extraCond.mixedPaymentTbl + " MP " +
			  " LEFT JOIN " + Params.dbName + ".pay_type PT ON PT.pay_type_id = MP.pay_type_id " +
			  " WHERE MP.order_id = " + extraCond.orderId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			PayType payType = new PayType(dbCon.rs.getInt("pay_type_id"));
			payType.setName(dbCon.rs.getString("pay_type_name"));
			result.add(payType, dbCon.rs.getFloat("price"));
		}
		dbCon.rs.close();
		
		return result; 
	}
	
	/**
	 * Delete the mixed payment to specific extra condition {@link ExtraCond}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param orderId
	 * 			
	 * @return
	 * @throws SQLException
	 */
	static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " DELETE FROM " + Params.dbName + "." + extraCond.mixedPaymentTbl + " WHERE 1 = 1 " + (extraCond != null ? extraCond.toString() : "");
		return dbCon.stmt.executeUpdate(sql);
	}

	public static class ArchiveResult{
		private final DBTbl fromTbl;
		private final DBTbl toTbl;
		public final int amount;
		public ArchiveResult(DateType archiveFrom, DateType archiveTo, int amount) {
			this.fromTbl = new DBTbl(archiveFrom);
			this.toTbl = new DBTbl(archiveTo);
			this.amount = amount;
		}
		public int getAmount(){
			return this.amount;
		}
		@Override
		public String toString(){
			return amount + " mixed payment record(s) are moved from '" + fromTbl.mixedTbl + "' to '" + toTbl.mixedTbl + "'";
		}
	}

	/**
	 * Archive the mixed payment record to specific order.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param order
	 * 			the mixed payment record to this order
	 * @param archiveFrom
	 * 			archive from {@link DateType}
	 * @param archiveTo
	 * 			archive to {@link DateType}
	 * @return the archive result {@link ArchiveResult}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static ArchiveResult archive(DBCon dbCon, Staff staff, Order order, DateType archiveFrom, DateType archiveTo) throws SQLException{
		DBTbl fromTbl = new DBTbl(archiveFrom);
		DBTbl toTbl = new DBTbl(archiveTo);
		final String item = "order_id, pay_type_id, price";
		String sql;
		sql = " INSERT INTO " + Params.dbName + "." + toTbl.mixedTbl +
			  " ( " + item + " ) " +
			  " SELECT order_id, pay_type_id, price FROM " + Params.dbName + "." + fromTbl.mixedTbl + " WHERE order_id = " + order.getId();
		return new ArchiveResult(archiveFrom, archiveTo, dbCon.stmt.executeUpdate(sql));
	}
	
}

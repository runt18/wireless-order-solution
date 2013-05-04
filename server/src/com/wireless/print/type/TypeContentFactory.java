package com.wireless.print.type;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberOperationDao;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.orderMgr.QueryOrderDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.shift.QueryShiftDao;
import com.wireless.db.system.SystemDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberType.Attribute;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.print.PType;
import com.wireless.protocol.Order;
import com.wireless.protocol.Terminal;

public class TypeContentFactory {
	
	private final static TypeContentFactory mInstance = new TypeContentFactory();
	
	private TypeContentFactory(){
		
	}
	 
	public static TypeContentFactory instance(){
		return mInstance;
	}
	
	public TypeContent createSummaryContent(PType printType, Terminal term, Order order) throws SQLException{
		if(order.hasOrderFood()){
			DBCon dbCon = new DBCon();
			try{
				dbCon.connect();
				List<Department> depts = DepartmentDao.getDepartments(dbCon, term, null, null);
				return new SummaryTypeContent(printType, term, order, depts);
			}finally{
				dbCon.disconnect();
			}
		}else{
			return null;
		}
	}
	
	public TypeContent createSummaryContent(PType printType, Terminal term, int orderId) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			Order order = QueryOrderDao.execByID(orderId, QueryOrderDao.QUERY_TODAY);
			if(order.hasOrderFood()){
				List<Department> depts = DepartmentDao.getDepartments(dbCon, term, null, null);
				return new SummaryTypeContent(printType, term, order, depts);
			}else{
				return null;
			}
		}finally{
			dbCon.disconnect();
		}
	}
	
	public TypeContent createDetailContent(PType printType, Terminal term, Order order) throws SQLException{
		if(order.hasOrderFood()){
			return new DetailTypeContent(printType, term, order);
		}else{
			return null;
		}
	}
	
	public TypeContent createDetailContent(PType printType, Terminal term, int orderId) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			Order order = QueryOrderDao.execByID(orderId, QueryOrderDao.QUERY_TODAY);
			if(order.hasOrderFood()){
				return new DetailTypeContent(printType, term, order);
			}else{
				return null;
			}
		}finally{
			dbCon.disconnect();
		}
	}
	
	public TypeContent createTransContent(PType printType, Terminal term, int orderId, Table srcTbl, Table destTbl){
		if(srcTbl.equals(destTbl)){
			return null;
		}else{
			return new TransTypeContent(printType, term, orderId, srcTbl, destTbl);
		}
	}
	
	public TypeContent createReceiptContent(PType printType, Terminal term, Order order) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			Restaurant restaurant = RestaurantDao.queryById(term);
//			int receiptStyle = QuerySetting.exec(dbCon, term.restaurantID).getReceiptStyle();
			int receiptStyle = SystemDao.getSetting(dbCon, term.restaurantID).getReceiptStyle();
			
			return new ReceiptTypeContent(printType, order, term.owner, receiptStyle, restaurant);
			
		}finally{
			dbCon.disconnect();
		}
	}

	public TypeContent createReceiptContent(PType printType, Terminal term, int orderId) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			Restaurant restaurant = RestaurantDao.queryById(term);
//			int receiptStyle = QuerySetting.exec(dbCon, term.restaurantID).getReceiptStyle();
			int receiptStyle = SystemDao.getSetting(dbCon, term.restaurantID).getReceiptStyle();
			
			Order order = QueryOrderDao.execByID(orderId, QueryOrderDao.QUERY_TODAY);
			
			return new ReceiptTypeContent(printType, order, term.owner, receiptStyle, restaurant);
			
		}finally{
			dbCon.disconnect();
		}
	}

	public TypeContent createShiftContent(PType printType, Terminal term, long onDuty, long offDuty) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			ShiftDetail shiftDetail;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			dbCon.connect();
			if(printType == PType.PRINT_DAILY_SETTLE_RECEIPT || printType == PType.PRINT_HISTORY_DAILY_SETTLE_RECEIPT ||
					printType == PType.PRINT_HISTORY_SHIFT_RECEIPT){
				/*
				 * Get the details to daily settlement from history ,
				 * since records to today has been moved to history before printing daily settlement receipt. 
				 */
				shiftDetail = QueryShiftDao.exec(term, sdf.format(onDuty), sdf.format(offDuty), QueryShiftDao.QUERY_HISTORY);
				
			}else{
				shiftDetail = QueryShiftDao.exec(term, sdf.format(onDuty), sdf.format(offDuty), QueryShiftDao.QUERY_TODAY);
			}
			
			return new ShiftTypeContent(printType, shiftDetail, term.owner);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	public TypeContent createMemberReceiptContent(PType printType, Terminal term, int memberOperationId) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			MemberOperation mo = MemberOperationDao.getTodayById(dbCon, memberOperationId);
			
			if(mo != null){
				Member member = MemberDao.getMemberById(mo.getMemberID());
				//Print the member receipt only if member type belongs to charge.
				if(member.getMemberType().getAttribute() == Attribute.CHARGE){
					
					mo.setMember(MemberDao.getMemberById(mo.getMemberID()));
					Restaurant restaurant = RestaurantDao.queryById(term);
					
					return new MemberReceiptTypeContent(restaurant, term.owner, mo, printType); 
					
				}else{
					return null;
				}
				
			}else{
				return null;
			}
			
			
		}finally{
			dbCon.disconnect();
		}
	}
}


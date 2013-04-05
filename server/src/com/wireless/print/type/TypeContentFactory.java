package com.wireless.print.type;

import java.sql.SQLException;
import java.text.SimpleDateFormat;

import com.wireless.db.DBCon;
import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberOperationDao;
import com.wireless.db.frontBusiness.QueryMenu;
import com.wireless.db.frontBusiness.QueryRestaurant;
import com.wireless.db.frontBusiness.QuerySetting;
import com.wireless.db.orderMgr.QueryOrderDao;
import com.wireless.db.shift.QueryShiftDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.print.PType;
import com.wireless.protocol.Order;
import com.wireless.protocol.PDepartment;
import com.wireless.protocol.Restaurant;
import com.wireless.protocol.Table;
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
				PDepartment[] depts = QueryMenu.queryDepartments(dbCon, "AND DEPT.restaurant_id=" + term.restaurantID, null);
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
				PDepartment[] depts = QueryMenu.queryDepartments(dbCon, "AND DEPT.restaurant_id=" + term.restaurantID, null);
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
			Restaurant restaurant = QueryRestaurant.exec(dbCon, term.restaurantID);
			int receiptStyle = QuerySetting.exec(dbCon, term.restaurantID).getReceiptStyle();
			
			return new ReceiptTypeContent(printType, order, term.owner, receiptStyle, restaurant);
			
		}finally{
			dbCon.disconnect();
		}
	}

	public TypeContent createReceiptContent(PType printType, Terminal term, int orderId) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			Restaurant restaurant = QueryRestaurant.exec(dbCon, term.restaurantID);
			int receiptStyle = QuerySetting.exec(dbCon, term.restaurantID).getReceiptStyle();
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
				mo.setMember(MemberDao.getMemberById(mo.getMemberID()));
				Restaurant restaurant = QueryRestaurant.exec(dbCon, term.restaurantID);
				
				return new MemberReceiptTypeContent(restaurant, term.owner, mo, printType); 
				
			}else{
				return null;
			}
			
			
		}finally{
			dbCon.disconnect();
		}
	}
}


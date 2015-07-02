package com.wireless.Actions.weixin.query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.member.MemberDao;
import com.wireless.db.member.MemberLevelDao;
import com.wireless.db.member.MemberOperationDao;
import com.wireless.db.member.MemberTypeDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.member.MemberLevel;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.util.SQLUtil;

public class WXQueryMemberOperationAction extends DispatchAction{
	
	/**
	 * 近5条消费记录
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward consumeDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(getData(1, request.getParameter("fid"), request.getParameter("oid")).toString());
		return null;
	}
	
	/**
	 * 近5条充值记录
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward chargeDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(getData(2, request.getParameter("fid"), request.getParameter("oid")).toString());
		return null;
	}
	
	/**
	 * 获取充值和消费的最近记录
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward chargeAndPointTitle(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(getGeneral(request.getParameter("fid"), request.getParameter("oid")).toString());
		return null;
	}	
	
	/**
	 * 近5条优惠券使用记录
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward couponConsumeDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(getData(3, request.getParameter("fid"), request.getParameter("oid")).toString());
		return null;
	}
	
	/**
	 * 获取用户操作记录
	 * @param type 数据类型 1:近5条消费记录 2:近5条充值记录 3:近5条优惠券使用记录
	 * @param restaurantSerial
	 * @param memberSerial
	 * @return
	 */
	private synchronized static JObject getData(int type, String restaurantSerial, String memberSerial){
		JObject jobject = new JObject();
		// 
		if(type != 1 && type != 2 && type != 3){
			jobject.initTip(false, "操作失败, 数据请求类型未知.");
			return jobject;
		}
		
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			
			List<MemberOperation> details = new ArrayList<MemberOperation>();
			Map<Object, Object> params = new HashMap<Object, Object>();
			
			// 获取餐厅编号
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(dbCon, restaurantSerial);
			//
			Staff staff = StaffDao.getAdminByRestaurant(dbCon, rid);
			// 获取会员编号
			int mid = MemberDao.getByWxSerial(dbCon, staff, memberSerial).getId();
			
			MemberOperationDao.ExtraCond extraCond4Today = new MemberOperationDao.ExtraCond(DateType.TODAY);
			MemberOperationDao.ExtraCond extraCond4History = new MemberOperationDao.ExtraCond(DateType.HISTORY);
			
			extraCond4Today.addMember(mid);
			extraCond4History.addMember(mid);
			// 查询条件(核心)
			if(type == 1){
				// 获取今日消费记录 
				extraCond4Today.addOperationType(MemberOperation.OperationType.CONSUME);
				extraCond4History.addOperationType(MemberOperation.OperationType.CONSUME);
			}else if(type == 2){
				// 获取今日充值记录 
				extraCond4Today.addOperationType(MemberOperation.OperationType.CHARGE);
				extraCond4History.addOperationType(MemberOperation.OperationType.CHARGE);
			}else if(type == 3){
				// 获取今日优惠券使用记录
				extraCond4Today.addOperationType(MemberOperation.OperationType.CONSUME).setContainsCoupon(true);
				extraCond4History.addOperationType(MemberOperation.OperationType.CONSUME).setContainsCoupon(true);
			}
			
			// 查询记录数
			int queryCount = 5;
			
			String orderClause = " ORDER BY MO.operate_date DESC " + " LIMIT " + 0 + "," + queryCount;
			//
			details = MemberOperationDao.getByCond(staff, extraCond4Today, orderClause);
			// 当日数据不足查询记录数时, 获取历史数据填充满
			if(details.size() < 5){
				params.put(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT, queryCount - details.size());
				orderClause = " ORDER BY MO.operate_date DESC " + " LIMIT " + 0 + "," + (queryCount - details.size());
				details.addAll(MemberOperationDao.getByCond(staff, extraCond4History, orderClause));
			}
			jobject.setRoot(details);
		}catch(BusinessException e){	
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			if(dbCon != null) dbCon.disconnect();
		}
		return jobject;
	}
	
	private synchronized static JObject getGeneral(String restaurantSerial, String memberSerial){
		JObject jobject = new JObject();
		
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			
			List<MemberOperation> chargeDetail = new ArrayList<MemberOperation>();
			List<MemberOperation> consumeDetail = new ArrayList<MemberOperation>();
			List<MemberOperation> couponDetail = new ArrayList<MemberOperation>();
			
			// 获取餐厅编号
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(dbCon, restaurantSerial);
			//
			Staff staff = StaffDao.getAdminByRestaurant(dbCon, rid);
			// 获取会员编号
			int mid = MemberDao.getByWxSerial(dbCon, staff, memberSerial).getId();
			
			// 查询条件(核心)
			
			MemberOperationDao.ExtraCond extraCond4Today = new MemberOperationDao.ExtraCond(DateType.TODAY);
			MemberOperationDao.ExtraCond extraCond4History = new MemberOperationDao.ExtraCond(DateType.HISTORY);
			
			extraCond4Today.addMember(mid);
			extraCond4History.addMember(mid);
			
			extraCond4Today.addOperationType(MemberOperation.OperationType.CHARGE);
			extraCond4History.addOperationType(MemberOperation.OperationType.CHARGE);
			
			// 查询记录数
			int queryCount = 1;		
			String orderClause = " ORDER BY MO.operate_date DESC " + " LIMIT " + 0 + "," + queryCount;

			
			
			//
			chargeDetail = MemberOperationDao.getByCond(staff, extraCond4Today, orderClause);
			// 当日数据不足查询记录数时, 获取历史数据填充满
			if(chargeDetail.size() < 1){
				chargeDetail.addAll(MemberOperationDao.getByCond(staff, extraCond4History, orderClause));
			}
			
			
			//查询消费记录
			extraCond4Today.clearOperationType().addOperationType(MemberOperation.OperationType.CONSUME);
			extraCond4History.clearOperationType().addOperationType(MemberOperation.OperationType.CONSUME);
			
			consumeDetail = MemberOperationDao.getByCond(staff, extraCond4Today, orderClause);
			// 当日数据不足查询记录数时, 获取历史数据填充满
			if(consumeDetail.size() < 1){
				consumeDetail.addAll(MemberOperationDao.getByCond(staff, extraCond4History, orderClause));
			}
			
			//查询优惠劵
			
			extraCond4Today.setContainsCoupon(true);
			extraCond4History.setContainsCoupon(true);
			
			couponDetail = MemberOperationDao.getByCond(staff, extraCond4Today, orderClause);
			// 当日数据不足查询记录数时, 获取历史数据填充满
			if(couponDetail.size() < 1){
				couponDetail.addAll(MemberOperationDao.getByCond(staff, extraCond4History, orderClause));
			}
			
			final MemberOperation charge_mo;
			if(!chargeDetail.isEmpty()){
				charge_mo = chargeDetail.get(0);
			}else{
				charge_mo = null;
			}
			
			final MemberOperation consume_mo;
			
			if(!consumeDetail.isEmpty()){
				consume_mo = consumeDetail.get(0);
			}else{
				consume_mo = null;
			}
			
			final MemberOperation coupon_mo;
			
			if(!couponDetail.isEmpty()){
				coupon_mo = couponDetail.get(0);
			}else{
				coupon_mo = null;
			}			
			
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("nearByCharge", charge_mo != null? NumericUtil.float2String2(charge_mo.getChargeMoney()) : (-1 + ""));
					jm.putString("nearByConsume", consume_mo != null? NumericUtil.float2String2(consume_mo.getPayMoney()) : (-1 + ""));
					jm.putString("couponConsume", coupon_mo != null? NumericUtil.float2String2(coupon_mo.getPayMoney()) : (-1 + ""));
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
			
		}catch(BusinessException e){	
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			if(dbCon != null) dbCon.disconnect();
		}
		return jobject;
	}	
	
	/**
	 * 现有优惠券
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward hasCouponDetails(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		JObject jobject = new JObject();
//		String imageBrowseDefaultFile = this.getServlet().getInitParameter("imageBrowseDefaultFile");
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			
			String restaurantSerial = request.getParameter("fid"), memberSerial = request.getParameter("oid");
			// 获取餐厅编号
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(dbCon, restaurantSerial);
			//
			Staff staff = StaffDao.getAdminByRestaurant(dbCon, rid);
			// 获取会员编号
			int mid = MemberDao.getByWxSerial(dbCon, staff, memberSerial).getId();
			
			List<Coupon> couponList = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setMember(mid).setStatus(Coupon.Status.DRAWN), null);
			for(int i = 0; i < couponList.size(); i++){
				couponList.set(i, CouponDao.getById(dbCon, staff, couponList.get(i).getId()));
			}
			
			jobject.setRoot(couponList);
		}catch(BusinessException e){	
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			if(dbCon != null){
				dbCon.disconnect();
			}
			response.getWriter().print(jobject.toString(Coupon.COUPON_JSONABLE_COMPLEX));
		}
		
		return null;
	}
	
	/**
	 * 获取会员等级highchart数据
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward chart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String webMemberLevelChart = request.getParameter("webMemberLevelChart");
		JObject jobject = new JObject();
		List<String> ydata = new ArrayList<String>();
		List<Float> data = new ArrayList<Float>();
		List<MemberLevel> memberLevelList = new ArrayList<MemberLevel>(); 
		try{
			String pin = (String) request.getAttribute("pin");
			String rid = request.getParameter("rid");
			Staff staff;
			if(pin != null && !pin.isEmpty()){
				staff = StaffDao.verify(Integer.parseInt(pin));
			}else{
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(rid));
			}
			memberLevelList = MemberLevelDao.get(staff);
			
			List<MemberType> memberTypeList = MemberTypeDao.getByCond(staff, null, null);
			
			for (int j = 0; j < memberLevelList.size(); j++) {
					ydata.add("{y:0, level : \'" +  memberLevelList.get(j).getMemberType().getName() 
								+ "\', x:" + memberLevelList.get(j).getPointThreshold() 
								+ ", marker: {symbol:\'diamond\'}, status : 1"+ (data.isEmpty()?", first:true":"") +"}" );
					data.add((float) memberLevelList.get(j).getPointThreshold());
			}
			
			if(webMemberLevelChart != null && !webMemberLevelChart.isEmpty()){
				int levelCount = data.size();
				int delta = memberTypeList.size() - data.size();
				if(memberTypeList.size() > data.size()){
					for (int i = 0; i < delta; i++) {
						StringBuilder y = new StringBuilder();
						y.append("{y : 0, level : \'等级" + ((i+1)+levelCount) + "\'");
						if(data.size() > 0){
							y.append(",x:" + (data.get(levelCount + i - 1)*1.5 + 1.3));
						}else{
							y.append(",x:0");
						}
						if(i == 0){
							y.append(", marker:{symbol:\'circle\'}");
							y.append(", status : 2");
							y.append(", color : \'maroon\'");
							y.append(data.isEmpty()?", first:true":"");
						}else{
							y.append(", marker:{symbol:\'square\'}");
							y.append(", status : 3");
							y.append(", color : \'Gray\'");
						}
						y.append("}");
						ydata.add(y.toString());
						data.add((float) (data.size() > 0 ? (data.get(levelCount + i - 1)*1.5 + 1.3) : 0));
					}
				}
			}
			
			final String chart = "{\"data\":"+ ydata +" }";
			
			List<Jsonable> js = new ArrayList<>();
			for (final MemberLevel ml : memberLevelList) {
				js.add(new Jsonable() {
					
					@Override
					public JsonMap toJsonMap(int flag) {
						JsonMap jm = new JsonMap();
						jm.putJsonable(ml, flag);
						jm.putFloat("exchangeRate", ml.getMemberType().getExchangeRate());
						jm.putFloat("chargeRate", ml.getMemberType().getChargeRate());
						jm.putJsonable("discount", ml.getMemberType().getDefaultDiscount(), flag);
						return jm;
					}
					
					@Override
					public void fromJsonMap(JsonMap jsonMap, int flag) {
						
					}
				});
			}
			
			jobject.setRoot(js);
			
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("chart", chart);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}	
	
}

package com.wireless.Actions.weixin.query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.wireless.db.member.represent.RepresentChainDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.promotion.CouponOperationDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WxMemberDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.member.MemberLevel;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberOperation.ChargeType;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.member.represent.RepresentChain;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.CouponOperation;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.util.SQLUtil;

public class WXQueryMemberOperationAction extends DispatchAction{
	
	/**
	 * 获取5条最新佣金记录
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward commissionDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		final String fid = request.getParameter("fid");
		final String oid = request.getParameter("oid");
		final JObject jObject = new JObject();
		
		try {
			final Staff staff = StaffDao.getAdminByRestaurant(WxRestaurantDao.getRestaurantIdByWeixin(fid));
			
			MemberOperationDao.ExtraCond extraCondToday = new MemberOperationDao.ExtraCond(DateType.TODAY).setChargeType(ChargeType.COMMISSION).addMember(MemberDao.getById(staff, WxMemberDao.getBySerial(staff, oid).getMemberId()));
		
			MemberOperationDao.ExtraCond extraCondHistory = new MemberOperationDao.ExtraCond(DateType.HISTORY).setChargeType(ChargeType.COMMISSION).addMember(MemberDao.getById(staff, WxMemberDao.getBySerial(staff, oid).getMemberId()));
		
			List<MemberOperation> result = MemberOperationDao.getByCond(staff, extraCondToday, " ORDER BY MO.id DESC LIMIT 5 ");
			
			for(MemberOperation operation : result){
				int orderId = 0;
				if(!operation.getComment().isEmpty()){
					orderId = Integer.parseInt(operation.getComment().split(",")[0].split(":")[1]);
				}
				if(orderId != 0){
					List<MemberOperation> fansConsumption = MemberOperationDao.getByCond(staff, new MemberOperationDao.ExtraCond(DateType.TODAY).setOrder(orderId), null);
					if(!fansConsumption.isEmpty()){
						operation.setMember(fansConsumption.get(0).getMember());
					}
				}
			}
			
			
			//当单日记录不足5条 查看历史记录
			if(result.size() < 5){
				List<MemberOperation> historyResult = MemberOperationDao.getByCond(staff, extraCondHistory, " ORDER BY MO.id DESC LIMIT 5 ");
				
				for(MemberOperation operation : historyResult){
					int orderId = 0;
					if(!operation.getComment().isEmpty()){
						orderId = Integer.parseInt(operation.getComment().split(",")[0].split(":")[1]);
					}
					if(orderId != 0){
						List<MemberOperation> fansConsumption = MemberOperationDao.getByCond(staff, new MemberOperationDao.ExtraCond(DateType.HISTORY)
																													.addOperationType(MemberOperation.OperationType.CONSUME)
																													.setOrder(orderId), null);
						if(!fansConsumption.isEmpty()){
							operation.setMember(fansConsumption.get(0).getMember());
						}
					}
				}
				
				result.addAll(historyResult);
			}
			
			if(result.size() > 5){
				result = result.subList(0, 4);
			}
			
			
			jObject.setRoot(result);
		} catch (BusinessException | SQLException e) {
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
	
	/**
	 *  近期5条代言记录
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward recommendDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		final String fid = request.getParameter("fid");
		final String oid = request.getParameter("oid");
		final JObject jObject = new JObject();
		
		try{
			final RepresentChainDao.ExtraCond extraCond = new RepresentChainDao.ExtraCond();
			final Staff staff = StaffDao.getAdminByRestaurant(WxRestaurantDao.getRestaurantIdByWeixin(fid));
			
			if(oid != null && !oid.isEmpty()){
				int recommendFuzzyId = WxMemberDao.getBySerial(staff, oid).getMemberId();
				extraCond.setReferrer(MemberDao.getById(staff, recommendFuzzyId));
			}
			
			final List<RepresentChain> representChainList = RepresentChainDao.getByCond(staff, extraCond);
			final List<RepresentChain> result = new ArrayList<>();
			
			if(representChainList.size() > 5){
				for(RepresentChain chain : representChainList){
					result.add(chain);
					if(result.size() == 5){
						break;
					}else{
						continue;
					}
				}
			}else{
				result.addAll(representChainList);
			}
			
			Collections.sort(result, new Comparator<RepresentChain>() {
				@Override
				public int compare(RepresentChain arg0, RepresentChain arg1) {
					if(arg0.getSubscribeDate() > arg1.getSubscribeDate()){
						return -1;
					}else if(arg0.getSubscribeDate() < arg1.getSubscribeDate()){
						return 1;
					}else{
						return 0;
					}
				}
			});
			
			jObject.setRoot(result);
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
	
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
	public ActionForward recent(ActionMapping mapping, ActionForm form,
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
	public ActionForward couponConsumeDetails(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		final JObject jObject = new JObject();
		final String fid  = request.getParameter("fid");
		final String oid = request.getParameter("oid");
		
		try{
			final CouponOperationDao.ExtraCond extraCond = new CouponOperationDao.ExtraCond();
			
			final Staff staff = StaffDao.getAdminByRestaurant(WxRestaurantDao.getRestaurantIdByWeixin(fid));
			
			if(oid != null && !oid.isEmpty()){
				extraCond.setMember(WxMemberDao.getBySerial(staff, oid).getMemberId());
			}
			
			
			//获取优惠券的操作记录
			final List<CouponOperation> result = CouponOperationDao.getByCond(staff, extraCond.setOperateType(CouponOperation.OperateType.USE));

			
			if(result.size() > 5){
				List<CouponOperation> returnList = new ArrayList<>();
				for(CouponOperation co : result){
					returnList.add(co);
					if(returnList.size() == 5){
						break;
					}else{
						continue;
					}
				}
				jObject.setRoot(returnList);
			}else{
				jObject.setRoot(result);
			}
			
		
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
	
	/**
	 * 获取用户操作记录
	 * @param type 数据类型 1:近5条消费记录 2:近5条充值记录 3:近5条优惠券使用记录
	 * @param restaurantSerial
	 * @param memberSerial
	 * @return
	 */
	private JObject getData(int type, String restaurantSerial, String memberSerial){
		final JObject jObject = new JObject();
		
		try{
			
			List<MemberOperation> details = new ArrayList<MemberOperation>();
			Map<Object, Object> params = new HashMap<Object, Object>();
			
			// 获取餐厅编号
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(restaurantSerial);
			//
			Staff staff = StaffDao.getAdminByRestaurant(rid);
			// 获取会员编号
			int mid = MemberDao.getByWxSerial(staff, memberSerial).getId();
			
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
			jObject.setRoot(details);
		}catch(BusinessException | SQLException e){	
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}
		return jObject;
	}
	
	private JObject getGeneral(String restaurantSerial, String memberSerial){
		JObject jobject = new JObject();
		
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			
			List<MemberOperation> chargeDetail = new ArrayList<MemberOperation>();
			List<MemberOperation> consumeDetail = new ArrayList<MemberOperation>();
			
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
			
			
			//查询已用优惠劵的数量
			final int usedCouponAmount = CouponOperationDao.getByCond(staff, new CouponOperationDao.ExtraCond().addOperation(CouponOperation.OperateType.USE)).size();
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("nearByCharge", charge_mo != null? NumericUtil.float2String2(charge_mo.getChargeMoney()) : (-1 + ""));
					jm.putString("nearByConsume", consume_mo != null? NumericUtil.float2String2(consume_mo.getPayMoney()) : (-1 + ""));
					jm.putInt("couponConsume", usedCouponAmount);
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
			
			List<Coupon> couponList = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setMember(mid).setStatus(Coupon.Status.ISSUED), null);
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

package com.wireless.Actions.weixin.query;

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
import com.wireless.db.client.member.MemberOperationDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WeixinMemberDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.util.OSSParams;
import com.wireless.util.OSSUtil;
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
			int rid = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, restaurantSerial);
			// 获取会员编号
			int mid = WeixinMemberDao.getBoundMemberIdByWeixin(dbCon, memberSerial, restaurantSerial);
			//
			Staff staff = StaffDao.getByRestaurant(dbCon, rid).get(0);
			
			// 查询条件(核心)
			String extra = "";
			if(type == 1){
				// 获取今日消费记录 
				extra = " AND MO.member_id = " + mid + " AND MO.operate_type = " + MemberOperation.OperationType.CONSUME.getValue();
			}else if(type == 2){
				// 获取今日充值记录 
				extra = " AND MO.member_id = " + mid + " AND MO.operate_type = " + MemberOperation.OperationType.CHARGE.getValue();
			}else if(type == 3){
				// 获取今日优惠券使用记录 
				extra = " AND MO.member_id = " + mid + " AND MO.operate_type = " + MemberOperation.OperationType.CONSUME.getValue() + " AND MO.coupon_id > 0 ";
			}
			
			// 查询记录数
			int queryCount = 5;
			// 
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			params.put(SQLUtil.SQL_PARAMS_ORDERBY, " ORDER BY MO.operate_date DESC ");
			params.put(SQLUtil.SQL_PARAMS_LIMIT_OFFSET, 0);
			params.put(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT, queryCount);
			//
			details = MemberOperationDao.getToday(dbCon, staff, params);
			// 当日数据不足查询记录数时, 获取历史数据填充满
			if(details.size() < 5){
				params.put(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT, queryCount - details.size());
				details.addAll(MemberOperationDao.getHistory(dbCon, staff, params));
			}
			jobject.setRoot(details);
		}catch(BusinessException e){	
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
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
			Map<Object, Object> params = new HashMap<Object, Object>();
			
			// 获取餐厅编号
			int rid = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, restaurantSerial);
			// 获取会员编号
			int mid = WeixinMemberDao.getBoundMemberIdByWeixin(dbCon, memberSerial, restaurantSerial);
			//
			Staff staff = StaffDao.getByRestaurant(dbCon, rid).get(0);
			
			// 查询条件(核心)
			String extra = "";
			extra = " AND MO.member_id = " + mid + " AND MO.operate_type = " + MemberOperation.OperationType.CHARGE.getValue();
			
			// 查询记录数
			int queryCount = 1;
			// 
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			params.put(SQLUtil.SQL_PARAMS_ORDERBY, " ORDER BY MO.operate_date DESC ");
			params.put(SQLUtil.SQL_PARAMS_LIMIT_OFFSET, 0);
			params.put(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT, queryCount);
			//
			chargeDetail = MemberOperationDao.getToday(dbCon, staff, params);
			// 当日数据不足查询记录数时, 获取历史数据填充满
			if(chargeDetail.size() < 1){
				params.put(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT, queryCount - chargeDetail.size());
				chargeDetail.addAll(MemberOperationDao.getHistory(dbCon, staff, params));
			}
			
			
			//查询消费记录
			
			extra = " AND MO.member_id = " + mid + " AND MO.operate_type = " + MemberOperation.OperationType.CONSUME.getValue();
			// 
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			params.put(SQLUtil.SQL_PARAMS_ORDERBY, " ORDER BY MO.operate_date DESC ");
			params.put(SQLUtil.SQL_PARAMS_LIMIT_OFFSET, 0);
			params.put(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT, queryCount);
			//
			consumeDetail = MemberOperationDao.getToday(dbCon, staff, params);
			// 当日数据不足查询记录数时, 获取历史数据填充满
			if(consumeDetail.size() < 1){
				params.put(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT, queryCount - consumeDetail.size());
				consumeDetail.addAll(MemberOperationDao.getHistory(dbCon, staff, params));
			}
			
			//查询优惠劵
			
			extra = " AND MO.member_id = " + mid + " AND MO.operate_type = " + MemberOperation.OperationType.CONSUME.getValue() + " AND MO.coupon_id > 0 ";
			// 
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			params.put(SQLUtil.SQL_PARAMS_ORDERBY, " ORDER BY MO.operate_date DESC ");
			params.put(SQLUtil.SQL_PARAMS_LIMIT_OFFSET, 0);
			params.put(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT, queryCount);
			//
			couponDetail = MemberOperationDao.getToday(dbCon, staff, params);
			// 当日数据不足查询记录数时, 获取历史数据填充满
			if(couponDetail.size() < 1){
				params.put(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT, queryCount - couponDetail.size());
				couponDetail.addAll(MemberOperationDao.getHistory(dbCon, staff, params));
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
			jobject.initTip(e);
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
	public ActionForward hasCouponDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		JObject jobject = new JObject();
		String imageBrowseDefaultFile = this.getServlet().getInitParameter("imageBrowseDefaultFile");
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			
			String restaurantSerial = request.getParameter("fid"), memberSerial = request.getParameter("oid");
			// 获取餐厅编号
			int rid = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, restaurantSerial);
			// 获取会员编号
			int mid = WeixinMemberDao.getBoundMemberIdByWeixin(dbCon, memberSerial, restaurantSerial);
			//
			Staff staff = StaffDao.getByRestaurant(dbCon, rid).get(0);
			
//			List<CouponType> couponTypeList = CouponTypeDao.get(dbCon, staff);
			List<Coupon> couponList = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setMember(mid).setStatus(Coupon.Status.DRAWN), null);
			
			//获取所有优惠券
/*			List<Map<String, Object>> root = new ArrayList<Map<String, Object>>(), asItems;
			Map<String, Object> item = null;
			for(CouponType listTemp : couponTypeList){
				item = new HashMap<String, Object>(listTemp.toJsonMap(0));
				asItems = new ArrayList<Map<String, Object>>();
				for(Coupon itemTemp : couponList){
					if(itemTemp.getCouponType().getId() == listTemp.getId()){
						asItems.add(itemTemp.toJsonMap(0));
					}
				}
				item.put("items", asItems);
				root.add(item);
			}
			
			jobject.getOther().put("root", root);*/
			for (Coupon temp : couponList) {
				if(temp.getCouponType().hasImage()){
					temp.getCouponType().setImage(("http://" + OSSUtil.BUCKET_IMAGE + "." + OSSParams.instance().OSS_OUTER_POINT + "/" + temp.getRestaurantId() + "/" + temp.getCouponType().getImage()));
				}else{
					temp.getCouponType().setImage(imageBrowseDefaultFile);
				}
			}
			
			jobject.setRoot(couponList);
		}catch(BusinessException e){	
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			if(dbCon != null) dbCon.disconnect();
			response.getWriter().print(jobject.toString(Coupon.ST_PARCELABLE_COMPLEX));
		}
		
		return null;
	}
	
}

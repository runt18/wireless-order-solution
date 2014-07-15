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
import com.wireless.db.coupon.CouponDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WeixinMemberDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.coupon.Coupon;
import com.wireless.pojo.staffMgr.Staff;
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
			Staff staff = StaffDao.getStaffs(dbCon, rid).get(0);
			
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
			Staff staff = StaffDao.getStaffs(dbCon, rid).get(0);
			
//			List<CouponType> couponTypeList = CouponTypeDao.get(dbCon, staff);
			List<Coupon> couponList = CouponDao.getAvailByMember(staff, mid);
			
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
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
}

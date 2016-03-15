package com.wireless.Actions.weixin.operate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.member.MemberDao;
import com.wireless.db.member.TakeoutAddressDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.menuMgr.FoodUnitDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.TableError;
import com.wireless.exception.WxRestaurantError;
import com.wireless.json.JObject;
import com.wireless.listener.SessionListener;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPrintContent;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.TakeoutAddress;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.order.WxOrder;
import com.wireless.sccon.ServerConnector;
import com.wireless.ws.watier.WxWaiter;
import com.wireless.ws.watier.WxWaiterServerPoint;

public class WxOperateOrderAction extends DispatchAction {
	
	/**
	 * 微信自助扫码下单
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward self(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		//final String oid = request.getParameter("oid");
		final String fid = request.getParameter("fid");
		final String wxOrderId = request.getParameter("wid");
		final String tableAlias = request.getParameter("tableAlias");
		final String branchId = request.getParameter("branchId");
		final String qrCode = request.getParameter("qrCode");		
		final JObject jObject = new JObject();
		try{
			final int rid;
			if(branchId != null && !branchId.isEmpty()){
				rid = Integer.parseInt(branchId);
			}else{
				rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			}	
			
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			
			//检查二维码是否正确
			if(qrCode != null && !qrCode.isEmpty()){
				final Staff staff4QrCode = StaffDao.getAdminByRestaurant(WxRestaurantDao.getRestaurantIdByWeixin(fid));
				if(WxRestaurantDao.getByCond(staff4QrCode, new WxRestaurantDao.ExtraCond().setQrCode(qrCode), null).isEmpty()){
					throw new BusinessException("扫描的二维码不正确");
				}
			}
			
			final WxOrder.UpdateBuilder builder = new WxOrder.UpdateBuilder(Integer.parseInt(wxOrderId));
			
			//检查餐台号是否存在
			if(tableAlias != null && !tableAlias.isEmpty()){
				try{
					builder.setTable(TableDao.getByAlias(staff, Integer.parseInt(tableAlias)));
				}catch(BusinessException e){
					if(e.equals(TableError.TABLE_NOT_EXIST)){
						throw new BusinessException("对不起，您输入的餐台号不存在");
					}else{
						throw e;
					}
				}
			}
			
			WxOrderDao.update(staff, builder);
			
			//打印微信账单
			ProtocolPackage resp = ServerConnector.instance().ask(ReqPrintContent.buildWxOrder(staff, Integer.parseInt(wxOrderId)).build());
			if(resp.header.type == Type.ACK){
				jObject.initTip(true, "自助扫码下单成功(完成打印)");
			}else{
				jObject.initTip(true, "自助扫码下单成功");
			}
			
			//web socket通知Touch微信下单
	        WxWaiter waiter = WxWaiterServerPoint.getWxWaiter(staff.getRestaurantId());
	        if(waiter != null){
	        	waiter.send(new WxWaiter.Msg4WxOrder(WxOrderDao.getById(staff, Integer.parseInt(wxOrderId))));
	        }
	        
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 微信自助单餐下单
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insertOrder(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		String oid = request.getParameter("oid");
		String fid = request.getParameter("fid");
		final String branchId = request.getParameter("branchId");
		final String foods = request.getParameter("foods");
		final String tableAlias = request.getParameter("tableAlias");
		final String comment = request.getParameter("comment");
		final String qrCode = request.getParameter("qrCode");
		final String sessionId = request.getParameter("sessionId");
		final JObject jObject = new JObject();
		try{
			
			if(sessionId != null && !sessionId.isEmpty()){
				HttpSession session = SessionListener.sessions.get(sessionId);
				if(session != null){
					oid = (String)session.getAttribute("oid");
					fid = (String)session.getAttribute("fid");
				}else{
					throw new BusinessException(WxRestaurantError.WEIXIN_SESSION_TIMEOUT);
				}
			}
			
			final int rid;
			if(branchId != null && !branchId.isEmpty()){
				rid = Integer.parseInt(branchId);
			}else{
				rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			}				
			final Staff staff = StaffDao.getAdminByRestaurant(rid);

			//检查二维码是否正确
			if(qrCode != null && !qrCode.isEmpty()){
				final Staff staff4QrCode = StaffDao.getAdminByRestaurant(WxRestaurantDao.getRestaurantIdByWeixin(fid));
				if(WxRestaurantDao.getByCond(staff4QrCode, new WxRestaurantDao.ExtraCond().setQrCode(qrCode), null).isEmpty()){
					throw new BusinessException("扫描的二维码不正确");
				}
			}
			
			final WxOrder.InsertBuilder4Inside builder = new WxOrder.InsertBuilder4Inside(oid);

			//检查餐台号是否存在
			if(tableAlias != null && !tableAlias.isEmpty()){
				try{
					builder.setTable(TableDao.getByAlias(staff, Integer.parseInt(tableAlias)));
				}catch(BusinessException e){
					if(e.equals(TableError.TABLE_NOT_EXIST)){
						throw new BusinessException("对不起，您输入的餐台号不存在");
					}else{
						throw e;
					}
				}
			}
			
			if(comment != null && !comment.isEmpty()){
				builder.setComment(comment);
			}
			
			if(foods != null && !foods.isEmpty()){
				for (String of : foods.split("&")) {
					String orderFoods[] = of.split(",");
					OrderFood orderFood = new OrderFood(FoodDao.getById(staff, Integer.parseInt(orderFoods[0])));
					orderFood.setCount(Float.parseFloat(orderFoods[1]));
					//foodunit多单位
					if(orderFoods.length > 2 && Integer.parseInt(orderFoods[2]) != 0){
						orderFood.setFoodUnit(FoodUnitDao.getById(staff, Integer.parseInt(orderFoods[2])));
					}
					
					builder.add(orderFood);
				}
			}
			
			final int wxOrderId = WxOrderDao.insert(staff, builder);
			jObject.setExtra(WxOrderDao.getById(staff, wxOrderId));
			
		}catch(BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}

	public ActionForward takeoutCommit(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		try{
			String oid = request.getParameter("oid");
			String fid = request.getParameter("fid");
			String foods = request.getParameter("foods");
//			String payment = request.getParameter("payment");
			String name = request.getParameter("name");
			String phone = request.getParameter("phone");
			String address = request.getParameter("address");
			String oldAddress = request.getParameter("oldAddress");
			
			int addressId;
			
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			
			Staff mStaff = StaffDao.getAdminByRestaurant(rid);
			
			final Member member = MemberDao.getByWxSerial(mStaff, oid);
			
			if(!phone.isEmpty() && !address.isEmpty()){
				TakeoutAddress.InsertBuilder builder = new TakeoutAddress.InsertBuilder(member, address, phone, name);
				addressId = TakeoutAddressDao.insert(mStaff, builder);
			}else{
				addressId = Integer.parseInt(oldAddress);
			}
			
			WxOrder.InsertBuilder4Takeout insertBuilder = new WxOrder.InsertBuilder4Takeout(oid, TakeoutAddressDao.getById(mStaff, addressId));
			
			for (String of : foods.split("&")) {
				String orderFoods[] = of.split(",");
				OrderFood orderFood = new OrderFood(FoodDao.getById(mStaff, Integer.parseInt(orderFoods[0])));
				orderFood.setCount(Float.parseFloat(orderFoods[1]));
				
				insertBuilder.add(orderFood);
			}
			
			
			WxOrderDao.insert(mStaff, insertBuilder);
			
			jobject.initTip(true, "下单成功, 可以在我的外卖中查看");
		}catch(BusinessException e){
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
	
	/**
	 * 获取相应微信会员的自助店内订单
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String oid = request.getParameter("oid");
		String fid = request.getParameter("fid");
		final String orderType = request.getParameter("type");
		final String id = request.getParameter("id");
		final String status = request.getParameter("status");
		final String sessionId = request.getParameter("sessionId");
		final JObject jObject = new JObject();
		
		try {
			if(sessionId != null && !sessionId.isEmpty()){
				HttpSession session = SessionListener.sessions.get(sessionId);
				if(session != null){
					oid = (String)session.getAttribute("oid");
					fid = (String)session.getAttribute("fid");
				}else{
					throw new BusinessException(WxRestaurantError.WEIXIN_SESSION_TIMEOUT);
				}
			}
			
			final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			
			final WxOrderDao.ExtraCond extraCond = new WxOrderDao.ExtraCond().setWeixin(oid);
			
			if(orderType != null && !orderType.isEmpty()){
				extraCond.setType(WxOrder.Type.valueOf(Integer.parseInt(orderType)));
			}else{
				extraCond.setType(WxOrder.Type.INSIDE);
			}

			if(status != null && !status.isEmpty()){
				extraCond.addStatus(WxOrder.Status.valueOf(Integer.parseInt(status)));
			}
			
			if(id != null && !id.isEmpty()){
				extraCond.setId(Integer.parseInt(id));
			}
			
			final List<WxOrder> result = new ArrayList<WxOrder>();
			//集团下需要显示所有门店的订单
			if(staff.isGroup()){
				for(Restaurant branches : RestaurantDao.getById(staff.getRestaurantId()).getBranches()){
					result.addAll(WxOrderDao.getByCond(StaffDao.getAdminByRestaurant(branches.getId()), extraCond, null));
				}
			}
			
			result.addAll(WxOrderDao.getByCond(staff, extraCond, null));
			
			//按下单日期降序显示
			Collections.sort(result, new Comparator<WxOrder>(){
				@Override
				public int compare(WxOrder o1, WxOrder o2) {
					if(o1.getBirthDate() > o2.getBirthDate()){
						return -1;
					}else if(o1.getBirthDate() < o2.getBirthDate()){
						return 1;
					}else{
						return 0;
					}
				}
			});
			
			for (WxOrder wxOrder : result) {
				if(wxOrder.getStatus() == WxOrder.Status.COMMITTED || wxOrder.getStatus() == WxOrder.Status.ORDER_ATTACHED){
					wxOrder.addFoods(WxOrderDao.getById(StaffDao.getAdminByRestaurant(wxOrder.getRestaurantId()), wxOrder.getId()).getFoods());
				}
				if(WxOrder.Type.valueOf(Integer.parseInt(orderType)) == WxOrder.Type.TAKE_OUT){
					wxOrder.setTakoutAddress(TakeoutAddressDao.getById(StaffDao.getAdminByRestaurant(wxOrder.getRestaurantId()), wxOrder.getTakeoutAddress().getId()));
				}
			}
			
			jObject.setRoot(result);
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
}

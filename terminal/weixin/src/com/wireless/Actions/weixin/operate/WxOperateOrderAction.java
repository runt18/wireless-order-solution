package com.wireless.Actions.weixin.operate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.member.MemberDao;
import com.wireless.db.member.TakeoutAddressDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.TakeoutAddress;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.order.WxOrder;

public class WxOperateOrderAction extends DispatchAction {
	
	/**
	 * 
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
		final JObject jObject = new JObject();
		try{
			final String oid = request.getParameter("oid");
			final String fid = request.getParameter("fid");
			final String foods = request.getParameter("foods");
			
			final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			
			final WxOrder.InsertBuilder4Inside builder = new WxOrder.InsertBuilder4Inside(oid);
			
			if(foods != null && !foods.isEmpty()){
				for (String of : foods.split("&")) {
					String orderFoods[] = of.split(",");
					OrderFood orderFood = new OrderFood(FoodDao.getById(staff, Integer.parseInt(orderFoods[0])));
					orderFood.setCount(Float.parseFloat(orderFoods[1]));
					
					builder.add(orderFood);
				}
			}
			
			final int wxOrderId = WxOrderDao.insert(staff, builder);
			
			final WxOrder wxOrder = WxOrderDao.getById(staff, wxOrderId);
			
			jObject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable("order", wxOrder, 0);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
			jObject.initTip(true, "下单成功,请呼叫服务员确认。");
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
}

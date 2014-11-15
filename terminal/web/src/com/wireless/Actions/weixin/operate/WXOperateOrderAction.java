package com.wireless.Actions.weixin.operate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.order.WxOrder;

public class WXOperateOrderAction extends DispatchAction {
	
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
		JObject jobject = new JObject();
		try{
			String oid = request.getParameter("oid");
			String fid = request.getParameter("fid");
			String foods = request.getParameter("foods");
			
			int rid = WeixinRestaurantDao.getRestaurantIdByWeixin(fid);
			
			Staff mStaff = StaffDao.getAdminByRestaurant(rid);
			
			WxOrder.InsertBuilder4Inside insertBuilder = new WxOrder.InsertBuilder4Inside(oid);
			
			for (String of : foods.split("&")) {
				String orderFoods[] = of.split(",");
				OrderFood orderFood = new OrderFood(FoodDao.getById(mStaff, Integer.parseInt(orderFoods[0])));
				orderFood.setCount(Float.parseFloat(orderFoods[1]));
				
				insertBuilder.add(orderFood);
			}
			int wxOrderId = WxOrderDao.insert(mStaff, insertBuilder);
			
			final WxOrder wxOrder = WxOrderDao.getById(mStaff, wxOrderId);
			
			jobject.setExtra(new Jsonable(){

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
			jobject.initTip(true, "下单成功,请呼叫服务员确认。");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}

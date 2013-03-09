package com.wireless.Actions.dishesOrder.orderGroup;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.VerifyPin;
import com.wireless.db.orderMgr.OrderGroupDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class UpdateOrderGroupAction extends DispatchAction{
	
	/**
	 * 修改团体组餐台信息
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward updateTable(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String restaurantID = request.getParameter("restaurantID");
			String pin = request.getParameter("pin");
			String rq = request.getParameter("tables");
			String otype = request.getParameter("otype");
			String pid = request.getParameter("parentID");
			if(pin == null){
				jobject.initTip(false, "操作失败, 验证终端有效信息错误, 请联系管理员.");
			}
			if(otype == null){
				jobject.initTip(false, "操作失败, 获取操作类型错误, 请联系管理员.");
			}
			if(jobject.isSuccess()){
				Table[] tg = null;
				Table item;
				JSONArray tableArray = JSONArray.fromObject(rq);
				if(tableArray.size() > 0){
					tg = new Table[tableArray.size()];
					for(int i = 0; i < tableArray.size(); i++){
						item = new Table();
						item.setTableId(tableArray.getJSONObject(i).getInt("id"));
						item.setAliasId(tableArray.getJSONObject(i).getInt("alias"));
						item.setRestaurantId(Integer.valueOf(restaurantID));
						tg[i] = item;
					}
					if(otype.equals("0")){
						int orderId = OrderGroupDao.insert(VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF), tg);
						jobject.initTip(true, "操作成功, 已合并团体餐桌信息.");
						jobject.getOther().put("orderID", orderId);
					}else if(otype.equals("1")){
						OrderGroupDao.update(VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF), Integer.valueOf(pid), tg);
						jobject.initTip(true, "操作成功, 已修改团体餐桌信息.");
					}
				}
			}
		} catch (BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.errCode, e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		} finally {
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
	
	/**
	 * 修改团体组账单信息
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward updateOrder(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = request.getParameter("pin");
			String restaurant = request.getParameter("restaurantID");
			String type = request.getParameter("type");
			String foodsString = request.getParameter("foods");
			
			JSONArray ja = JSONArray.fromObject(foodsString);
			List<Order> ol = (ArrayList<Order>) JSONArray.toList(ja, Order.class);
			List<OrderFood> ofl = null;
			
			Terminal term = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			com.wireless.protocol.Order parentOrder = new com.wireless.protocol.Order();
			com.wireless.protocol.Order[] childOrders = new com.wireless.protocol.Order[ol.size()];
			com.wireless.protocol.OrderFood[] coFoods = null;
			for(int i = 0; i < childOrders.length; i++){
				childOrders[i] = new com.wireless.protocol.Order();
				ofl = ol.get(i).getOrderFoods();
				if(ofl != null && ofl.size() > 0){
					coFoods = new com.wireless.protocol.OrderFood[ofl.size()];
					com.wireless.protocol.OrderFood tmpOrderFoods = null;
					for(int k = 0; k < ofl.size(); k++){
						tmpOrderFoods = new com.wireless.protocol.OrderFood();
						tmpOrderFoods.setAliasId((int) ofl.get(k).getAliasID());
						
						
						coFoods[i] = tmpOrderFoods;
//						coFoods[i] = (com.wireless.protocol.OrderFood) OrderFood.changeToOther(ofl.get(k), com.wireless.protocol.OrderFood.class);
					}
				}
				childOrders[i].setOrderFoods(coFoods);
			}
			
			if(type.equals("insert")){
				OrderGroupDao.insert(term, parentOrder);
			}else if(type.equals("update")){
				OrderGroupDao.update(term, parentOrder);
			}
			
//			Table[] tableToGrouped = new Table[0];
//			com.wireless.protocol.Terminal term = null;
//			OrderGroupDao.insert(term, tableToGrouped);
//			OrderFood[] od = (OrderFood[]) JSONObject.toBean(bean, OrderFood.class);
//			List list = (List) JSONObject.toBean(bean, List.class);
			
//			System.out.println("ol.size():  " + ol.size());
			
		} catch (BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.errCode, e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		} finally {
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
	
}

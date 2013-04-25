package com.wireless.Actions.dishesOrder.orderGroup;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.orderMgr.OrderGroupDao;
import com.wireless.exception.BusinessException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqPrintContent;
import com.wireless.protocol.PTable;
import com.wireless.protocol.Terminal;
import com.wireless.sccon.ServerConnector;
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
				PTable[] tg = null;
				PTable item;
				JSONArray tableArray = JSONArray.fromObject(rq);
				if(tableArray.size() > 0){
					tg = new PTable[tableArray.size()];
					for(int i = 0; i < tableArray.size(); i++){
						item = new PTable();
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
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
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
//			String restaurantID = request.getParameter("restaurantID");
			String type = request.getParameter("type");
			String ordersString = request.getParameter("orders");
			String parentOrderID = request.getParameter("parentOrderID");
			
			final Terminal term = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			
			JSONArray ja = JSONArray.fromObject(ordersString);
			
			JSONArray jaItem = null, tgContent = null;
			JSONObject orderObj = null, foodObj = null, tasteGroup = null, ttObj = null;
			com.wireless.protocol.Order parentOrder = new com.wireless.protocol.Order();
			com.wireless.protocol.Order[] orderItemSet = new com.wireless.protocol.Order[ja.size()];
			com.wireless.protocol.Order orderItem = null;
			com.wireless.protocol.OrderFood[] ofSet = null;
			com.wireless.protocol.OrderFood of = null;
			
			for(int i = 0; i < ja.size(); i++){
				// 生成账单对象
				orderObj = ja.getJSONObject(i);
				
				// 获取单张账单菜品数据集信息
				jaItem = orderObj.getJSONArray("foods");
				orderItem = new com.wireless.protocol.Order();
				ofSet = new com.wireless.protocol.OrderFood[jaItem.size()];
				for(int k = 0; k < jaItem.size(); k++){
					
					foodObj = jaItem.getJSONObject(k);
					// 生成菜品数据集对象以及相关信息
					of = new com.wireless.protocol.OrderFood();
					
					of.setName(foodObj.getString("foodName"));
					of.setAliasId(foodObj.getInt("aliasID"));
					of.setCount(Float.valueOf(foodObj.get("count").toString()));
//					of.getKitchen().setAliasId(Short.valueOf(foodObj.get("kitchenID").toString()));
//					of.setDiscount(0f);
					of.setHangup(foodObj.getBoolean("isHangup"));
					
					tasteGroup = foodObj.getJSONObject("tasteGroup");
					tgContent = tasteGroup.getJSONArray("normalTasteContent");
					ttObj = tasteGroup.getJSONObject("tempTaste");
					com.wireless.protocol.Taste tasteToAdd = null, tmpTaste = null; 
					if(tgContent != null && tgContent.size() > 0){
						of.makeTasteGroup();
						for(int ti = 0; ti < tgContent.size(); ti++){
							tasteToAdd = new com.wireless.protocol.Taste();
							tasteToAdd.setTasteId(tgContent.getJSONObject(ti).getInt("tasteID"));
							tasteToAdd.setAliasId(tgContent.getJSONObject(ti).getInt("tasteAliasID"));
							tasteToAdd.setCategory((short) tgContent.getJSONObject(ti).getInt("tasteCategory"));
							of.getTasteGroup().addTaste(tasteToAdd);							
						}
					}
					if(ttObj != null && !ttObj.toString().equals("null")){
						tmpTaste = new com.wireless.protocol.Taste();
						tmpTaste.setTasteId(ttObj.getInt("tasteID"));
						tmpTaste.setAliasId(ttObj.getInt("tasteAliasID"));
						tmpTaste.setPreference(ttObj.getString("tasteName"));
						tmpTaste.setPrice(Float.valueOf(ttObj.getString("tastePrice")));
						of.getTasteGroup().setTmpTaste(tmpTaste);
					}
					
					ofSet[k] = of;
				}
				orderItem.setId(orderObj.get("orderID") == null ? 0 : orderObj.getInt("orderID"));
				orderItem.getDestTbl().setAliasId(orderObj.getInt("tableAlias"));
				orderItem.getDestTbl().setTableId(orderObj.getInt("tableID"));
				orderItem.setOrderFoods(ofSet);
				orderItemSet[i] = orderItem;
			}
			parentOrder.setId(Integer.valueOf(parentOrderID));
			// 设置账单组子账单信息
			parentOrder.setChildOrder(orderItemSet);
			
			if(type.equals("insert")){
				OrderGroupDao.insert(term, parentOrder);
				jobject.initTip(true, "操作成功, 已下单");
			}else if(type.equals("update")){
				OrderGroupDao.update(term, parentOrder);
				jobject.initTip(true, "操作成功, 已改单");
			}
			ReqPrintContent reqPrintContent = ReqPrintContent.buildReqPrintSummary(
					new PinGen() {
						@Override
						public short getDeviceType() {
							return Terminal.MODEL_STAFF;
						}
						@Override
						public long getDeviceId() {
							return term.pin;
						}
					},
					parentOrder.getId());	
			if(reqPrintContent != null){
				ProtocolPackage resp = ServerConnector.instance().ask(reqPrintContent);
				if(resp.header.type != Type.ACK){
					jobject.setMsg(jobject.getMsg() + "但打印失败.");
				}
			}
		} catch(BusinessException e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		} catch(IOException e) {
			e.printStackTrace();
			jobject.setMsg(jobject.getMsg() + "但打印失败.");
		} catch(Exception e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		} finally {
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
	
}

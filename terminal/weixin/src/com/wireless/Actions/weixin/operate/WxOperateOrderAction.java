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
import com.wireless.db.menuMgr.FoodUnitDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.TableError;
import com.wireless.json.JObject;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPrintContent;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.TakeoutAddress;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.order.WxOrder;
import com.wireless.sccon.ServerConnector;

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
			final String tableAlias = request.getParameter("tableAlias");
			final String qrCode = request.getParameter("qrCode");
			
			final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			
			final Staff staff = StaffDao.getAdminByRestaurant(rid);

			//检查二维码是否正确
			if(qrCode != null && !qrCode.isEmpty()){
				if(WxRestaurantDao.getByCond(staff, new WxRestaurantDao.ExtraCond().setQrCode(qrCode), null).isEmpty()){
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
			
			if(foods != null && !foods.isEmpty()){
				for (String of : foods.split("&")) {
					String orderFoods[] = of.split(",");
					OrderFood orderFood = new OrderFood(FoodDao.getById(staff, Integer.parseInt(orderFoods[0])));
					orderFood.setCount(Float.parseFloat(orderFoods[1]));
					if(orderFoods.length > 2){
						orderFood.setFoodUnit(FoodUnitDao.getById(staff, Integer.parseInt(orderFoods[2])));
					}
					
					builder.add(orderFood);
				}
			}
			
			final int wxOrderId = WxOrderDao.insert(staff, builder);
			jObject.setExtra(WxOrderDao.getById(staff, wxOrderId));
			
			//打印微信账单
			ProtocolPackage resp = ServerConnector.instance().ask(ReqPrintContent.buildWxOrder(staff, wxOrderId).build());
			if(resp.header.type == Type.ACK){
				jObject.initTip(true, "下单成功(完成打印)");
			}else{
				jObject.initTip(true, "下单成功");
			}
			
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

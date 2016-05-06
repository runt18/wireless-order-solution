package com.wireless.Actions.weixin.operate;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.beeCloud.BeeCloud;
import com.wireless.beeCloud.Bill;
import com.wireless.db.member.MemberDao;
import com.wireless.db.member.TakeoutAddressDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.menuMgr.FoodUnitDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.system.SystemDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.exception.TableError;
import com.wireless.exception.WxRestaurantError;
import com.wireless.json.JObject;
import com.wireless.listener.SessionListener;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqInsertOrder;
import com.wireless.pack.req.ReqOrderDiscount;
import com.wireless.pack.req.ReqPayOrder;
import com.wireless.pack.req.ReqPrintContent;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.TakeoutAddress;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.system.Setting;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.weixin.order.WxOrder;
import com.wireless.sccon.ServerConnector;
import com.wireless.ws.waiter.WxWaiter;
import com.wireless.ws.waiter.WxWaiterServerPoint;

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
		String fid = request.getParameter("fid");
		final String wxOrderId = request.getParameter("wid");
		final String tableAlias = request.getParameter("tableAlias");
		String branchId = request.getParameter("branchId");
		final String qrCode = request.getParameter("qrCode");		
		final String sessionId = request.getParameter("sessionId");
		final JObject jObject = new JObject();
		try{
			
			if(sessionId != null && !sessionId.isEmpty()){
				HttpSession session = SessionListener.sessions.get(sessionId);
				if(session != null){
					fid = (String)session.getAttribute("fid");
					branchId = (String)session.getAttribute("branchId");
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
	 * 微信计算账单的信息
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward calcOrder(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		final String sessionId = request.getParameter("sessionId");
		final String foods = request.getParameter("foods");
		final String coupons = request.getParameter("coupons");
		final JObject jObject= new JObject();
		try{
			final HttpSession session = SessionListener.sessions.get(sessionId);
			if(session != null){
				final String branchId = (String)session.getAttribute("branchId");
				final String oid = (String)session.getAttribute("oid");
				final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
				Order order = new Order(0);
				
				if(foods != null && !foods.isEmpty()){
					for (String of : foods.split("&")) {
						String orderFoods[] = of.split(",");
						OrderFood orderFood = new OrderFood(FoodDao.getById(staff, Integer.parseInt(orderFoods[0])));
						orderFood.setCount(Float.parseFloat(orderFoods[1]));
						//food unit多单位
						if(orderFoods.length > 2 && Integer.parseInt(orderFoods[2]) != 0){
							orderFood.setFoodUnit(FoodUnitDao.getById(staff, Integer.parseInt(orderFoods[2])));
						}
						order.addFood(orderFood, staff);
					}
				}
				
				//设置折扣
				Member member = MemberDao.getByWxSerial(staff, oid);
				order.setDiscount(member.getMemberType().getDefaultDiscount());

				//使用coupon
				if(coupons != null && !coupons.isEmpty()){
					float couponPrice = 0;
					for(String couponId : coupons.split(",")){
						couponPrice += CouponDao.getById(staff, Integer.parseInt(couponId)).getCouponType().getPrice();
					}
					order.setCouponPrice(couponPrice);
				}
				
				float totalPrice = order.calcTotalPrice();
				order.setTotalPrice(totalPrice);
				float actualPrice = 0;
				//Get the setting.
				Setting setting = SystemDao.getByCond(staff, null).get(0).getSetting();
				//Deal with the decimal according to setting.
				if(setting.getPriceTail().isDecimalCut()){
					//小数抹零
					actualPrice = Float.valueOf(totalPrice).intValue();
				}else if(setting.getPriceTail().isDecimalRound()){
					//四舍五入
					actualPrice = Math.round(totalPrice);
				}else{
					//不处理
					actualPrice = totalPrice;
				}

				//Minus the erase & coupon price.
				actualPrice = actualPrice - order.getCouponPrice();
				actualPrice = actualPrice > 0 ? actualPrice : 0;
				
				order.setActualPrice(actualPrice);
				
				jObject.setRoot(order);
			}else{
				//TODO
			}
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
	/**
	 * 微信直接下单
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		final String sessionId = request.getParameter("sessionId");
		final String foods = request.getParameter("foods");
		final String tableAlias = request.getParameter("tableAlias");
		final String force = request.getParameter("force");
		final JObject jObject= new JObject();
		
		try{
			final HttpSession session = SessionListener.sessions.get(sessionId);
			if(session != null){
				final String branchId = (String)session.getAttribute("branchId");
				//FIXME 
				final String oid = (String)session.getAttribute("oid");
				final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
				
				final Order.InsertBuilder builder = new Order.InsertBuilder(new Table.Builder(TableDao.getByAlias(staff, Integer.parseInt(tableAlias)).getId()));
				if(foods != null && !foods.isEmpty()){
					for (String of : foods.split("&")) {
						String orderFoods[] = of.split(",");
						OrderFood orderFood = new OrderFood(FoodDao.getById(staff, Integer.parseInt(orderFoods[0])));
						orderFood.setCount(Float.parseFloat(orderFoods[1]));
						//food unit多单位
						if(orderFoods.length > 2 && Integer.parseInt(orderFoods[2]) != 0){
							orderFood.setFoodUnit(FoodUnitDao.getById(staff, Integer.parseInt(orderFoods[2])));
						}
						builder.add(orderFood, staff);
					}
				}

				if(force != null && !force.isEmpty() && Boolean.parseBoolean(force)){
					builder.setForce(true);
				}
				
				final ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(staff, builder, PrintOption.DO_PRINT));
				if(resp.header.type == Type.ACK){
					jObject.initTip(true, ("下单成功."));
				}else{
					ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
					jObject.initTip(false, errCode.getCode(), errCode.getDesc());
				}
				
			}else{
				throw new BusinessException(WxRestaurantError.WEIXIN_SESSION_TIMEOUT);
			}
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	
	
	/**
	 * 微信支付下单
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward wxPayOrder(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		final String sessionId = request.getParameter("sessionId");
		final String cost = request.getParameter("cost");
		final String foods = request.getParameter("foods");
		final String tableAlias = request.getParameter("tableAlias");
		final JObject jObject = new JObject();
		try {
			final HttpSession session = SessionListener.sessions.get(sessionId);
			if(session != null){
				final String branchId = (String)session.getAttribute("branchId");
				final String oid = (String)session.getAttribute("oid");
				final String fid = (String)session.getAttribute("fid");
				final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
				final Member member = MemberDao.getByWxSerial(staff, oid);
				final Restaurant restaurant = RestaurantDao.getById(WxRestaurantDao.getRestaurantIdByWeixin(fid));
				final Table table = TableDao.getByAlias(staff, Integer.parseInt(tableAlias));
				
				if(table.isBusy()){
					Order order = OrderDao.getById(staff, table.getOrderId(), DateType.TODAY);
					if(!order.getOrderFoods().isEmpty()){
						throw new BusinessException("餐桌上已经有点菜,不能进行微信支付下单");
					}
					
				}
				
				
				if(restaurant.hasBeeCloud()){
					BeeCloud app = BeeCloud.registerApp(restaurant.getBeeCloudAppId(), restaurant.getBeeCloudAppSecret());
					final String billNo = System.currentTimeMillis() + "";
					Bill.Response beeCloudResponse = app.bill().ask(new Bill.Request().setChannel(Bill.Channel.WX_JSAPI)
																									  .setOpenId(oid)
																									  .setTotalFee((int)((Float.valueOf(cost) * 100)))
//																									  .setTotalFee(1)
																									  .setBillNo(billNo)
																									  .setTitle(restaurant.getName() + "微信支付"), 
					new Callable<ProtocolPackage>(){
						@Override
						public ProtocolPackage call() throws Exception {
							
							final Order.InsertBuilder builder = new Order.InsertBuilder(new Table.Builder(table.getId()));
							try{
								if(foods != null && !foods.isEmpty()){
									for (String of : foods.split("&")) {
										String orderFoods[] = of.split(",");
										OrderFood orderFood = new OrderFood(FoodDao.getById(staff, Integer.parseInt(orderFoods[0])));
										orderFood.setCount(Float.parseFloat(orderFoods[1]));
										//food unit多单位
										if(orderFoods.length > 2 && Integer.parseInt(orderFoods[2]) != 0){
											orderFood.setFoodUnit(FoodUnitDao.getById(staff, Integer.parseInt(orderFoods[2])));
										}
										builder.add(orderFood, staff);
									}
								}
								builder.setForce(true);
								ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(staff, builder, PrintOption.DO_PRINT));
								if(resp.header.type == Type.ACK){
									//TODO 账单注入此微信会员
									Order.DiscountBuilder discountBuilder = Order.DiscountBuilder.build4Member(table.getOrderId(), member);
									resp = ServerConnector.instance().ask(new ReqOrderDiscount(staff, discountBuilder));
									if(resp.header.type == Type.NAK){
										throw new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
									}

									//TODO 用微信支付的方式结账
									Order.PayBuilder payBuilder = Order.PayBuilder.build4Member(table.getOrderId(), PayType.WX, true);
									resp = ServerConnector.instance().ask(new ReqPayOrder(staff, payBuilder));
									if(resp.header.type == Type.NAK){
										throw new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
									}
								}else{
									throw new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
								}
								return resp;
							}catch(BusinessException | SQLException | IOException e){
								System.out.println(e.getMessage());
								return null;
							}
						}
					});
					if(beeCloudResponse.isOk()){
						jObject.setExtra(beeCloudResponse);
					}else{
						throw new BusinessException(beeCloudResponse.getErrDetail() + "," + beeCloudResponse.getResultMsg());
					}
				}else{
					throw new BusinessException("对不起，你的公众号还没开通微信支付");
				}
			}else{
				throw new BusinessException("微信支付金额不能小于0");
			}
		}catch(BusinessException | SQLException e){
			jObject.initTip(e);
			e.printStackTrace();
		} finally {
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
		String branchId = request.getParameter("branchId");
		final String foods = request.getParameter("foods");
		final String tableAlias = request.getParameter("tableAlias");
		final String comment = request.getParameter("comment");
		final String qrCode = request.getParameter("qrCode");
		final String sessionId = request.getParameter("sessionId");
		final String print = request.getParameter("print");
		final String orderId = request.getParameter("orderId");
		final JObject jObject = new JObject();
		try{
			
			if(sessionId != null && !sessionId.isEmpty()){
				HttpSession session = SessionListener.sessions.get(sessionId);
				if(session != null){
					oid = (String)session.getAttribute("oid");
					fid = (String)session.getAttribute("fid");
					branchId = (String)session.getAttribute("branchId");
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
			
			if(orderId != null && !orderId.isEmpty()){
				builder.setOrder(Integer.parseInt(orderId));
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
			
			final WxOrder wxOrder = WxOrderDao.getById(staff, WxOrderDao.insert(staff, builder));
			jObject.setExtra(wxOrder);
			
			if(print != null && !print.isEmpty() && Boolean.parseBoolean(print)){
				//打印微信账单
				ProtocolPackage resp = ServerConnector.instance().ask(ReqPrintContent.buildWxOrder(staff, wxOrder.getId()).build());
				if(resp.header.type == Type.ACK){
					jObject.initTip(true, "自助扫码下单成功(完成打印)");
				}else{
					jObject.initTip(true, "自助扫码下单成功");
				}
			}
			
			//web socket通知Touch微信下单
	        WxWaiter waiter = WxWaiterServerPoint.getWxWaiter(staff.getRestaurantId());
	        if(waiter != null){
	        	waiter.send(new WxWaiter.Msg4WxOrder(wxOrder));
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
		String branchId = request.getParameter("branchId");
		final String orderType = request.getParameter("type");
		final String id = request.getParameter("id");
		final String status = request.getParameter("status");
		final String includeBranch = request.getParameter("includeBranch");
		final String sessionId = request.getParameter("sessionId");
		final String orderId = request.getParameter("orderId");
		final JObject jObject = new JObject();
		
		try {
			if(sessionId != null && !sessionId.isEmpty()){
				HttpSession session = SessionListener.sessions.get(sessionId);
				if(session != null){
					oid = (String)session.getAttribute("oid");
					fid = (String)session.getAttribute("fid");
					branchId = (String)session.getAttribute("branchId");
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
			
			final WxOrderDao.ExtraCond extraCond = new WxOrderDao.ExtraCond().setWeixin(oid);
			
			if(orderId != null && !orderId.isEmpty()){
				extraCond.setOrder(Integer.valueOf(orderId));
			}
			
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
			if(staff.isGroup() && includeBranch != null && !includeBranch.isEmpty() && Boolean.parseBoolean(includeBranch)){
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
//				if(WxOrder.Type.valueOf(Integer.parseInt(orderType)) == WxOrder.Type.TAKE_OUT){
//					wxOrder.setTakoutAddress(TakeoutAddressDao.getById(StaffDao.getAdminByRestaurant(wxOrder.getRestaurantId()), wxOrder.getTakeoutAddress().getId()));
//				}
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

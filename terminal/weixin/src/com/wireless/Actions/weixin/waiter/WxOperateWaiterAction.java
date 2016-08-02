package com.wireless.Actions.weixin.waiter;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.marker.weixin.api.QRCode;
import org.marker.weixin.api.Token;
import org.marker.weixin.auth.AuthParam;
import org.marker.weixin.auth.AuthorizerToken;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.wireless.Actions.weixin.WxHandleMessage;
import com.wireless.db.DBCon;
import com.wireless.db.member.MemberDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.orderMgr.PayOrder;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WxMemberDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WxRestaurantError;
import com.wireless.json.JObject;
import com.wireless.listener.SessionListener;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPrintContent;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.WxMember;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;
import com.wireless.sccon.ServerConnector;
import com.wireless.ws.waiter.WxWaiter;
import com.wireless.ws.waiter.WxWaiterServerPoint;

public class WxOperateWaiterAction extends DispatchAction{

	/**
	 * 呼叫结账
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward callPay(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String orderId = request.getParameter("orderId");
		final String payType = request.getParameter("payType");
		final String sessionId = request.getParameter("sessionId");
		final String tableId = request.getParameter("tableId");
		final JObject jObject= new JObject();
		try{
			final HttpSession session = SessionListener.sessions.get(sessionId);
			if(session != null){
				final String branchId = (String)session.getAttribute("branchId");
				final String oid = (String)session.getAttribute("oid");
				final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
				
				final WxMember wxMember = WxMemberDao.getBySerial(staff, oid);
				final Member member = MemberDao.getById(staff, wxMember.getMemberId());
				
				Order order;
				if(orderId != null && !orderId.isEmpty()){
					order = OrderDao.getById(staff, Integer.parseInt(orderId), DateType.TODAY);
				}else if(tableId != null && !tableId.isEmpty()){
					order = OrderDao.getByTableId(staff, Integer.parseInt(tableId));
				}else{
					throw new BusinessException("没有账单Id或者餐桌Id");
				}
				
				
				//web socket通知Touch呼叫结账
		        WxWaiter waiter = WxWaiterServerPoint.getWxWaiter(staff.getRestaurantId());
		        if(waiter != null){
		        	waiter.send(new WxWaiter.Msg4CallPay(order.getDestTbl().getName(), payType));
		        }
				
		        //打印呼叫结账单
		        try{
		        	ServerConnector.instance().ask(ReqPrintContent.buildWxCallPay(staff, order.getId(), member.getId(), payType).build());
		        }catch(IOException ignored){
		        	ignored.printStackTrace();
		        }
		        
			}else{
				throw new BusinessException(WxRestaurantError.WEIXIN_SESSION_TIMEOUT);
			}
	        
		}catch(BusinessException | SQLException e){
			jObject.initTip(e);
			e.printStackTrace();
			
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	
	/**
	 * 获取餐桌信息
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getTableStatus(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String tableId = request.getParameter("tableId");
		final String sessionId = request.getParameter("sessionId");
		final JObject jObject = new JObject();
		String fid = request.getParameter("fid");
		try {
			Table table;
			if(sessionId != null && !sessionId.isEmpty()){
				final HttpSession session = SessionListener.sessions.get(sessionId);
				if(session != null){
					fid = (String)session.getAttribute("fid");
				}else{
					throw new BusinessException(WxRestaurantError.WEIXIN_SESSION_TIMEOUT);
				}
			}
			final Staff staff = StaffDao.getWxByRestaurant(WxRestaurantDao.getRestaurantIdByWeixin(fid));
			
			if(tableId != null && !tableId.isEmpty()){
				table = TableDao.getById(staff, Integer.valueOf(tableId));
				jObject.setRoot(table);
			}else{
				throw new BusinessException("餐桌号不能为空");
			}
		}catch(SQLException | BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
		} finally {
			response.getWriter().print(jObject.toString());
		}
		
		
		return null;
	}
	
	
	/**
	 * 获取账单信息
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getOrder(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String orderId = request.getParameter("orderId");
		final String sessionId = request.getParameter("sessionId");
		final String tableId = request.getParameter("tableId");
		
		final JObject jObject= new JObject();
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			final HttpSession session = SessionListener.sessions.get(sessionId);
			if(session != null){
				final String branchId = (String)session.getAttribute("branchId");
				final Staff staff = StaffDao.getAdminByRestaurant(dbCon, Integer.parseInt(branchId));
				//jObject.setRoot(OrderDao.getById(staff, Integer.parseInt(orderId), DateType.TODAY));
				
				if(orderId != null && !orderId.isEmpty()){
					jObject.setRoot(PayOrder.calc(dbCon, staff, Order.PayBuilder.build4Normal(Integer.parseInt(orderId))));
					
				}else if(tableId != null && !tableId.isEmpty()){
					Table table = TableDao.getById(dbCon, staff, Integer.parseInt(tableId));
					if(table.isBusy()){
						final Order order = PayOrder.calc(dbCon, staff, Order.PayBuilder.build4Normal(table.getOrderId()));
						for(int i = 0; i < order.getOrderFoods().size(); i++){
							final Food f = order.getOrderFoods().get(i).asFood();
							f.copyFrom(FoodDao.getById(dbCon, staff, f.getFoodId()));
						}
						jObject.setRoot(order);
					}else{
						throw new BusinessException("餐桌为空闲状态");
					}
				}else{
					throw new BusinessException("入口不对,不存在账单号或餐桌号");
				}
				
			}else{
				throw new BusinessException(WxRestaurantError.WEIXIN_SESSION_TIMEOUT);
			}
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
			dbCon.disconnect();
		}
		return null;
	}

	/**
	 * 微信店小二二维码内容
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	public ActionForward qrCode(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String restaurantId = request.getParameter("restaurantId");
		final String orderId = request.getParameter("orderId");
		final JObject jObject= new JObject();
		try{
			final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(restaurantId));
			final Order order = OrderDao.getById(staff, Integer.parseInt(orderId), DateType.TODAY);
			final WxRestaurant wxRestaurant;
			if(staff.isBranch()){
				wxRestaurant = WxRestaurantDao.get(StaffDao.getAdminByRestaurant(staff.getGroupId()));
			}else{
				wxRestaurant = WxRestaurantDao.get(staff);
			}
			
			//生成带账单ID的微信二维码
			final String qrCodeUrl = new QRCode().setTemp(String.valueOf(WxHandleMessage.QrCodeParam.newWaiter(order.getId()).sceneId()))
												 .setExpired(14400)	//4 hour
												 .createUrl(Token.newInstance(AuthorizerToken.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, wxRestaurant.getWeixinAppId(), wxRestaurant.getRefreshToken())));
												 //.createUrl(Token.newInstance(FinanceWeixinAction.APP_ID, FinanceWeixinAction.APP_SECRET));
			//获取此二维码的内容
            LuminanceSource source = new BufferedImageLuminanceSource(toBufferedImage(ImageIO.read(new URL(qrCodeUrl)).getScaledInstance(400, 400, Image.SCALE_SMOOTH)));  
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));  
            @SuppressWarnings("serial")
			Result qrCode = new MultiFormatReader().decode(bitmap, new HashMap<DecodeHintType, Object>(){{ put(DecodeHintType.CHARACTER_SET, "GBK"); }});
            
            response.getWriter().print(qrCode.getText());
            
		}catch(BusinessException | SQLException | NotFoundException | IOException e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 打印微信小二
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	public ActionForward print(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String callback = request.getParameter("callback");
		final String restaurantId = request.getParameter("restaurantId");
		final String orderId = request.getParameter("orderId");
		final JObject jObject= new JObject();
		try{
			final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(restaurantId));
			final Order order = OrderDao.getById(staff, Integer.parseInt(orderId), DateType.TODAY);
			final WxRestaurant wxRestaurant;
			if(staff.isBranch()){
				wxRestaurant = WxRestaurantDao.get(StaffDao.getAdminByRestaurant(staff.getGroupId()));
			}else{
				wxRestaurant = WxRestaurantDao.get(staff);
			}
			
			//生成带账单ID的微信二维码
			final String qrCodeUrl = new QRCode().setTemp(String.valueOf(WxHandleMessage.QrCodeParam.newWaiter(order.getId()).sceneId()))
												 .setExpired(14400)	//4 hour
												 .createUrl(Token.newInstance(AuthorizerToken.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, wxRestaurant.getWeixinAppId(), wxRestaurant.getRefreshToken())));
												 //.createUrl(Token.newInstance(FinanceWeixinAction.APP_ID, FinanceWeixinAction.APP_SECRET));
			//获取此二维码的内容
            LuminanceSource source = new BufferedImageLuminanceSource(toBufferedImage(ImageIO.read(new URL(qrCodeUrl)).getScaledInstance(400, 400, Image.SCALE_SMOOTH)));  
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));  
            @SuppressWarnings("serial")
			Result qrCode = new MultiFormatReader().decode(bitmap, new HashMap<DecodeHintType, Object>(){{ put(DecodeHintType.CHARACTER_SET, "GBK"); }});
            
            //System.out.println(qrCode.getText());
            
            ProtocolPackage resp = ServerConnector.instance().ask(ReqPrintContent.buildWxWaiter(staff, order.getId(), qrCode.getText()).build());
            
            if(resp.header.type == Type.ACK){
            	jObject.initTip(true, "打印微信小二成功");
            }else{
            	jObject.initTip(false, "打印微信小二失败");
            }
            
		}catch(BusinessException | SQLException | NotFoundException | IOException e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			if(callback != null && !callback.isEmpty()){
				response.getWriter().print(callback + "(" + jObject.toString() + ")");
			}else{
				response.getWriter().print(jObject.toString());
			}
		}
		return null;
	}
	
	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img The Image to be converted
	 * @return The converted BufferedImage
	 */
	private BufferedImage toBufferedImage(Image img){
	    if (img instanceof BufferedImage){
	        return (BufferedImage) img;
	    }

	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
}

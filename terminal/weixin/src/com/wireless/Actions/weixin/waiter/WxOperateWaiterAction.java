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
import com.wireless.db.member.MemberDao;
import com.wireless.db.orderMgr.OrderDao;
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
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;
import com.wireless.sccon.ServerConnector;
import com.wireless.ws.watier.WxWaiter;
import com.wireless.ws.watier.WxWaiterServerPoint;

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

		final JObject jObject= new JObject();
		try{
			final HttpSession session = SessionListener.sessions.get(sessionId);
			if(session != null){
				final String fromId = (String)session.getAttribute("fid");
				final String oid = (String)session.getAttribute("oid");
				final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fromId);
				final Staff staff = StaffDao.getAdminByRestaurant(rid);
				
				final WxMember wxMember = WxMemberDao.getBySerial(staff, oid);
				final Member member = MemberDao.getById(staff, wxMember.getMemberId());
				final Order order = OrderDao.getById(staff, Integer.parseInt(orderId), DateType.TODAY);
				
				//web socket通知Touch呼叫结账
		        WxWaiter waiter = WxWaiterServerPoint.getWxWaiter(staff.getRestaurantId());
		        if(waiter != null){
		        	waiter.send(new WxWaiter.Msg4CallPay(order.getDestTbl().getName(), payType));
		        }
				
		        //TODO 打印账单
		       
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

		final JObject jObject= new JObject();
		try{
			final HttpSession session = SessionListener.sessions.get(sessionId);
			if(session != null){
				final String fromId = (String)session.getAttribute("fid");
				final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fromId);
				final Staff staff = StaffDao.getAdminByRestaurant(rid);
				jObject.setRoot(OrderDao.getById(staff, Integer.parseInt(orderId), DateType.TODAY));
				
			}else{
				throw new BusinessException(WxRestaurantError.WEIXIN_SESSION_TIMEOUT);
			}
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
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
		final String tableId = request.getParameter("tableId");
		final JObject jObject= new JObject();
		try{
			final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(restaurantId));
			Order order = OrderDao.getByTableId(staff, Integer.parseInt(tableId));
			WxRestaurant wxRestaurant = WxRestaurantDao.get(staff);
			
			//生成带账单ID的微信二维码
			final String qrCodeUrl = new QRCode().setSceneId(order.getId())
												 .setExpired(14400)
												 .createUrl(Token.newInstance(AuthorizerToken.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, wxRestaurant.getWeixinAppId(), wxRestaurant.getRefreshToken())));
			
			//获取此二维码的内容
            LuminanceSource source = new BufferedImageLuminanceSource(toBufferedImage(ImageIO.read(new URL(qrCodeUrl)).getScaledInstance(400, 400, Image.SCALE_SMOOTH)));  
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));  
            @SuppressWarnings("serial")
			Result qrCode = new MultiFormatReader().decode(bitmap, new HashMap<DecodeHintType, Object>(){{ put(DecodeHintType.CHARACTER_SET, "GBK"); }});
            
            System.out.println(qrCode.getText());
            
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

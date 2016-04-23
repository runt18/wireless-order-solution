package com.wireless.Actions.weixin.represent;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.marker.weixin.api.QRCode;
import org.marker.weixin.api.Token;
import org.marker.weixin.api.User;
import org.marker.weixin.auth.AuthParam;
import org.marker.weixin.auth.AuthorizerToken;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.wireless.Actions.weixin.WxHandleMessage;
import com.wireless.db.member.represent.RepresentDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WxMemberDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.member.WxMember;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;

public class WxOperateRepresentAction extends DispatchAction{
	
	/**
	 * 获取推荐人的信息
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward referrer(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String fid = request.getParameter("fid");
		final String oid = request.getParameter("oid");
		
		final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
		final Staff staff = StaffDao.getAdminByRestaurant(rid);
		final WxRestaurant wxRestaurant = WxRestaurantDao.get(staff);
		
		final JObject jObject = new JObject();
		
		try {
			User referrer = User.newInstance(Token.newInstance(wxRestaurant.getWeixinAppId(), wxRestaurant.getWeixinAppSecret()), oid);
			jObject.setRoot(referrer);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			response.getWriter().println(jObject);
		}
		return null;
	}
	
	/**
	 * 生成【我要代言】的关系链二维码
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward qrCode(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String fromId = request.getParameter("fid");
		final String oid = request.getParameter("oid");
		
		final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fromId);
		final Staff staff = StaffDao.getAdminByRestaurant(rid);
		final WxMember referrer = WxMemberDao.getBySerial(staff, oid);
		final WxRestaurant wxRestaurant = WxRestaurantDao.get(staff);
		
		final JObject jObject = new JObject();
		
		try{
			//生成带推荐人ID的【我要代言】二维码
			final String qrCodeUrl = new QRCode().setSceneId(WxHandleMessage.QrCodeParam.newRepresent(referrer.getMemberId()).sceneId())
												 .setExpired(24 * 3600 * 30)	//30天
												 .createUrl(Token.newInstance(AuthorizerToken.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, wxRestaurant.getWeixinAppId(), wxRestaurant.getRefreshToken())));
												 //.createUrl(Token.newInstance(FinanceWeixinAction.APP_ID, FinanceWeixinAction.APP_SECRET));
			//获取此二维码的内容
	        LuminanceSource source = new BufferedImageLuminanceSource(toBufferedImage(ImageIO.read(new URL(qrCodeUrl)).getScaledInstance(400, 400, Image.SCALE_SMOOTH)));  
	        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));  
	        @SuppressWarnings("serial")
			Result qrCode = new MultiFormatReader().decode(bitmap, new HashMap<DecodeHintType, Object>(){{ put(DecodeHintType.CHARACTER_SET, "GBK"); }});
        
	        jObject.initTip(qrCode.getText());
	        
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}finally{
			response.getWriter().print(jObject.toString());
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
	
	
	/**
	 * 获取餐厅的代言设置
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String fid = request.getParameter("fid");
		final JObject jObject = new JObject();
		try{
			final Staff staff = StaffDao.getAdminByRestaurant(WxRestaurantDao.getRestaurantIdByWeixin(fid));
			
			final RepresentDao.ExtraCond extraCond = new RepresentDao.ExtraCond();
			
			jObject.setRoot(RepresentDao.getByCond(staff, extraCond));
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
}

package com.wireless.Actions.weixin.auth;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.marker.weixin.api.Button;
import org.marker.weixin.api.Menu;
import org.marker.weixin.api.Token;
import org.marker.weixin.auth.AuthParam;
import org.marker.weixin.auth.AuthorizationInfo;
import org.marker.weixin.auth.AuthorizerInfo;
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
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;
import com.wireless.pojo.weixin.restaurant.WxRestaurant.QrCodeStatus;

public class WxAuthAction extends Action {
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try{
			Restaurant restaurant = RestaurantDao.getById(Integer.parseInt(request.getParameter("rid")));
			
			AuthorizationInfo authorizationInfo = AuthorizationInfo.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, request.getParameter("auth_code"));
			//System.out.println(authorizationInfo);
			
			AuthorizerInfo authorizerInfo = AuthorizerInfo.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, authorizationInfo.getAuthorizerAppId());
			//System.out.println(authorizerInfo);
			
			//Parse the qr code url from image.
            System.out.println(authorizerInfo.getQrCodeUrl());
            LuminanceSource source = new BufferedImageLuminanceSource(toBufferedImage(ImageIO.read(new URL(authorizerInfo.getQrCodeUrl())).getScaledInstance(400, 400, Image.SCALE_SMOOTH)));  
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));  
            @SuppressWarnings("serial")
			Result qrCode = new MultiFormatReader().decode(bitmap, new HashMap<DecodeHintType, Object>(){{ put(DecodeHintType.CHARACTER_SET, "GBK"); }});
            
			WxRestaurantDao.update(StaffDao.getAdminByRestaurant(restaurant.getId()), 
									   new WxRestaurant.UpdateBuilder().setWeixinAppId(authorizerInfo.getAppId())
									   								   .setWxSerial(authorizerInfo.getUserName())
								   									   .setNickName(authorizerInfo.getNickName())
								   									   .setHeadImgUrl(authorizerInfo.getHeadImg())
								   									   .setQrCodeUrl(authorizerInfo.getQrCodeUrl())
								   									   .setQrCode(qrCode.getText())
								   									   .setQrCodeStatus(QrCodeStatus.NORMAL)
								   									   .setRefreshToken(authorizationInfo.getAuthorizerRefreshToken()));
			
			AuthorizerToken authorizerToken = AuthorizerToken.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, authorizationInfo.getAuthorizerAppId(), authorizationInfo.getAuthorizerRefreshToken());
			
			//Make the menu.
			Menu menu = new Menu();
			Menu.delete(Token.newInstance(authorizerToken));
			menu.set1stButton(new Button.ClickBuilder("餐厅简介", WxHandleMessage.EventKey.INTRO_EVENT_KEY.getKey()).build());
			menu.set2ndButton(new Button.ScanMsgBuilder("扫一扫", WxHandleMessage.EventKey.SCAN_EVENT_KEY.getKey()).build());
			
			menu.set3rdButton(new Button.ClickBuilder("我的", "AAA")
							.addChild(new Button.ClickBuilder("优惠活动", WxHandleMessage.EventKey.PROMOTION_EVENT_KEY.getKey()))
							.addChild(new Button.ClickBuilder("我的订单", WxHandleMessage.EventKey.ORDER_EVENT_KEY.getKey()))
							.addChild(new Button.ClickBuilder("我的会员卡", WxHandleMessage.EventKey.MEMBER_EVENT_KEY.getKey()))
							//.addChild(new Button.ClickBuilder("我的大转盘", WeiXinHandleMessage.ZHUAN_EVENT_KEY))
							.build());
			if(menu.create(Token.newInstance(authorizerToken)).isOk()){
				response.getWriter().print(restaurant.getName() + "授权成功");
			}else{
				response.getWriter().print(restaurant.getName() + "授权失败");
			}
			
		}catch(BusinessException | SQLException e){
			response.getWriter().print(e.getMessage());
		}
		
		return null;
	}

	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img The Image to be converted
	 * @return The converted BufferedImage
	 */
	private static BufferedImage toBufferedImage(Image img){
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
	
	public static void main(String[] args) throws MalformedURLException, IOException, NotFoundException{
		final String qrCodeUrl = "http://mmbiz.qpic.cn/mmbiz/icAic8iciciadf7EcPuKicqpEuiaicVfRib6AEdiacHZHuW0rnnAOaChhdrkAHmMHvCcTmTqerqhUUSiaBnFibnyoEfeRKao5Q/0";
		LuminanceSource source = new BufferedImageLuminanceSource(toBufferedImage(ImageIO.read(new URL(qrCodeUrl)).getScaledInstance(400, 400, Image.SCALE_SMOOTH)));  
		//LuminanceSource source = new BufferedImageLuminanceSource(ImageIO.read(new URL(qrCodeUrl)));  
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));  
		
		@SuppressWarnings("serial")
		Result qrCode = new MultiFormatReader().decode(bitmap, new HashMap<DecodeHintType, Object>(){{ put(DecodeHintType.CHARACTER_SET, "GBK"); }});
		
		System.out.println(qrCode.getText());
//		AuthParam.APP_ID = "wx12a2c67dac231fad";
//		AuthParam.APP_SECRET = "0c79e1fa963cd80cc0be99b20a18faeb";
//		final String ticket = "{\"appId\":\"wx12a2c67dac231fad\",\"infoType\":\"component_verify_ticket\",\"ticket\":\"ticket@@@Zk620TOtZZYyAZdopZCpWDu6wqKZgcAK-LDX0vM8Oip6P3e_p_AGV3ql-ppP2QsHu0ngm1mlFkxljy8CF4RjNw\"}";
//		final String appId = "wx42baa103149e1c64";
//		final String refreshToken = "refreshtoken@@@-DOp6MRlWGgHOK-7SnXNSuGTT9ilCdVPvHwEuSR2qXE";
//        ComponentVerifyTicket verifiyTicket = JObject.parse(ComponentVerifyTicket.JSON_CREATOR, 0, ticket);
//        ComponentAccessToken accessToken = ComponentAccessToken.newInstance(verifiyTicket);
//        
//		AuthorizerToken authorizerToken = AuthorizerToken.newInstance(accessToken, appId, refreshToken);
//		//System.out.println(authorizerToken);
//		
//		//Make the menu.
//		Menu menu = new Menu();
//		org.marker.weixin.api.Status status = Menu.delete(Token.newInstance(authorizerToken));
//		if(status.isOk()){
//			System.out.println("菜单删除成功");
//		}else{
//			System.out.println("菜单删除失败," + status.toString());
//		}
//		menu.set1stButton(new Button.ClickBuilder("餐厅导航", WeiXinHandleMessage.NAVI_EVENT_KEY).build());
//		menu.set2ndButton(new Button.ScanMsgBuilder("扫一扫", WeiXinHandleMessage.SCAN_EVENT_KEY).build());
//		
//		menu.set3rdButton(new Button.ClickBuilder("我的", "AAA")
//						.addChild(new Button.ClickBuilder("优惠活动", WeiXinHandleMessage.PROMOTION_EVENT_KEY))
//						.addChild(new Button.ClickBuilder("我的订单", WeiXinHandleMessage.ORDER_EVENT_KEY))
//						.addChild(new Button.ClickBuilder("我的会员卡", WeiXinHandleMessage.MEMBER_EVENT_KEY))
//						//.addChild(new Button.ClickBuilder("我的大转盘", WeiXinHandleMessage.ZHUAN_EVENT_KEY))
//						.build());
//		
//		status = menu.create(Token.newInstance(authorizerToken));
//		if(status.isOk()){
//			System.out.println("菜单更新成功");
//		}else{
//			System.out.println("菜单更新失败," + status.toString());
//		}
	}
	
}

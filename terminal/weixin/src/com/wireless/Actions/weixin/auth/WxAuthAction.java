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
import org.maker.weixin.auth.AuthorizationInfo;
import org.maker.weixin.auth.AuthorizerInfo;
import org.maker.weixin.auth.AuthorizerToken;
import org.maker.weixin.auth.ComponentAccessToken;
import org.maker.weixin.auth.ComponentVerifyTicket;
import org.marker.weixin.api.Button;
import org.marker.weixin.api.Menu;
import org.marker.weixin.api.Token;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.wireless.Actions.weixin.WeiXinHandleMessage;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
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
            LuminanceSource source = new BufferedImageLuminanceSource(toBufferedImage(ImageIO.read(new URL(authorizerInfo.getQrCodeUrl())).getScaledInstance(420, 420, Image.SCALE_SMOOTH)));  
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
			//System.out.println(authorizerToken);
			
			//Make the menu.
			Menu menu = new Menu();
			Menu.delete(Token.newInstance(authorizerToken));
			menu.set1stButton(new Button.ClickBuilder("餐厅导航", WeiXinHandleMessage.NAVI_EVENT_KEY).build());
			menu.set2ndButton(new Button.ScanMsgBuilder("扫一扫", WeiXinHandleMessage.SCAN_EVENT_KEY).build());
			
			menu.set3rdButton(new Button.ClickBuilder("我的", "AAA")
							.addChild(new Button.ClickBuilder("优惠活动", WeiXinHandleMessage.PROMOTION_EVENT_KEY))
							.addChild(new Button.ClickBuilder("我的订单", WeiXinHandleMessage.ORDER_EVENT_KEY))
							.addChild(new Button.ClickBuilder("我的会员卡", WeiXinHandleMessage.MEMBER_EVENT_KEY))
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
//		final String qrCodeUrl = "http://mmbiz.qpic.cn/mmbiz/E6E9icibhDaqDwMlicviaNx3wDHD8ehYYUyEV5icWMpn0zKU6hAMlgGibtdrHjUcxcBCvMiaiakWStuOUibthTupKaQWrAA/0";
//		LuminanceSource source = new BufferedImageLuminanceSource(toBufferedImage(ImageIO.read(new URL(qrCodeUrl)).getScaledInstance(420, 420, Image.SCALE_SMOOTH)));  
//		//LuminanceSource source = new BufferedImageLuminanceSource(ImageIO.read(new URL(qrCodeUrl)));  
//		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));  
//		
//		@SuppressWarnings("serial")
//		Result qrCode = new MultiFormatReader().decode(bitmap, new HashMap<DecodeHintType, Object>(){{ put(DecodeHintType.CHARACTER_SET, "GBK"); }});
//		
//		System.out.println(qrCode.getText());
		final String ticket = "";
		final String appId = "";
		final String refreshToken = "";
        AuthParam.COMPONENT_VERIFY_TICKET = JObject.parse(ComponentVerifyTicket.JSON_CREATOR, 0, ticket);
        AuthParam.COMPONENT_ACCESS_TOKEN = ComponentAccessToken.newInstance(AuthParam.COMPONENT_VERIFY_TICKET);
        
		AuthorizerToken authorizerToken = AuthorizerToken.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, appId, refreshToken);
		//System.out.println(authorizerToken);
		
		//Make the menu.
		Menu menu = new Menu();
		Menu.delete(Token.newInstance(authorizerToken));
		menu.set1stButton(new Button.ClickBuilder("餐厅导航", WeiXinHandleMessage.NAVI_EVENT_KEY).build());
		menu.set2ndButton(new Button.ScanMsgBuilder("扫一扫", WeiXinHandleMessage.SCAN_EVENT_KEY).build());
		
		menu.set3rdButton(new Button.ClickBuilder("我的", "AAA")
						.addChild(new Button.ClickBuilder("优惠活动", WeiXinHandleMessage.PROMOTION_EVENT_KEY))
						.addChild(new Button.ClickBuilder("我的订单", WeiXinHandleMessage.ORDER_EVENT_KEY))
						.addChild(new Button.ClickBuilder("我的会员卡", WeiXinHandleMessage.MEMBER_EVENT_KEY))
						//.addChild(new Button.ClickBuilder("我的大转盘", WeiXinHandleMessage.ZHUAN_EVENT_KEY))
						.build());
		
		if(menu.create(Token.newInstance(authorizerToken)).isOk()){
			System.out.println("菜单更新成功");
		}else{
			System.out.println("菜单更新失败");
		}
	}
	
}

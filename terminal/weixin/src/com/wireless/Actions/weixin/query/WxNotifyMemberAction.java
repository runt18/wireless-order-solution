package com.wireless.Actions.weixin.query;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.marker.weixin.api.Template;
import org.marker.weixin.api.Template.Keyword;
import org.marker.weixin.api.Token;
import org.marker.weixin.auth.AuthParam;
import org.marker.weixin.auth.AuthorizerToken;

import com.wireless.db.member.MemberOperationDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WxMemberDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.WxMember;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;

public class WxNotifyMemberAction extends DispatchAction{
	
	//发送账单模板信息
	public ActionForward bill(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String orderId = request.getParameter("orderId");
		final String staffId = request.getParameter("staffId");
		final Staff staff = StaffDao.getById(Integer.parseInt(staffId));
		final Order order = OrderDao.getById(staff, Integer.parseInt(orderId), DateType.TODAY);
		
		final MemberOperation lastOperation = MemberOperationDao.getLastConsumptionByOrder(staff, order);
		
		final WxRestaurant wxRestaurant = WxRestaurantDao.get(staff);
		if(wxRestaurant.hasPaymentTemplate() && wxRestaurant.hasQrCode()){
			final WxMember wxMember = WxMemberDao.getByMember(staff, order.getMemberId());
//			System.out.println("serial:" + wxMember.getSerial());
//			System.out.println("weixin app id:" + wxRestaurant.getWeixinAppId());
//			System.out.println("weixin app secret:" + wxRestaurant.getWeixinAppSecret());
//			String appId = "wx49b3278a8728ff76";
//			String appSecret = "0ba130d87e14a1a37e20c78a2b0ee3ba";
//			final Token token = Token.newInstance(appId, appSecret);
			final AuthorizerToken authorizerToken = AuthorizerToken.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, wxRestaurant.getWeixinAppId(), wxRestaurant.getRefreshToken());
			final Token token = Token.newInstance(authorizerToken);
			Template.send(token, new Template.Builder()
					.setToUser(wxMember.getSerial())
					//.setToUser("odgTwt4tqd_mu-q9EY02rqHrp_M0")
					.setTemplateId(wxRestaurant.getPaymentTemplate())
					.addKeyword(new Keyword("first", "您好，消费已成功"))
					.addKeyword(new Keyword("keyword1", NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(lastOperation.getPayMoney())))
					.addKeyword(new Keyword("keyword2", wxRestaurant.getNickName()))
					.addKeyword(new Keyword("keyword3", Integer.toString(lastOperation.getDeltaPoint())))
					.addKeyword(new Keyword("keyword4", Integer.toString(lastOperation.getRemainingPoint())))
					.addKeyword(new Keyword("keyword5", NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(lastOperation.getRemainingTotalMoney()))));
		}	
		
		return null;
	}
}

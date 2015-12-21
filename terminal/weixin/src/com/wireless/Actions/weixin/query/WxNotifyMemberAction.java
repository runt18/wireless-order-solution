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
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;

public class WxNotifyMemberAction extends DispatchAction{
	
	//发送充值模板信息
	public ActionForward charge(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String moId = request.getParameter("moId");
		final String staffId = request.getParameter("staffId");
		final Staff staff = StaffDao.getById(Integer.parseInt(staffId));
		final MemberOperation mo = MemberOperationDao.getById(staff, DateType.TODAY, Integer.parseInt(moId));
		final WxRestaurant wxRestaurant = WxRestaurantDao.get(staff);
		if(wxRestaurant.hasChargeTemplate() && wxRestaurant.hasQrCode()){
			final WxMember wxMember = WxMemberDao.getByMember(staff, mo.getMemberId());
			//String appId = "wx49b3278a8728ff76";
			//String appSecret = "0ba130d87e14a1a37e20c78a2b0ee3ba";
			//final Token token = Token.newInstance(appId, appSecret);
			final AuthorizerToken authorizerToken = AuthorizerToken.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, wxRestaurant.getWeixinAppId(), wxRestaurant.getRefreshToken());
			final Token token = Token.newInstance(authorizerToken);
			/**
			 * {{first.DATA}}
			 * 店面：{{keyword1.DATA}}
			 * 充值时间：{{keyword2.DATA}}
			 * 充值金额：{{keyword3.DATA}}
			 * 赠送金额：{{keyword4.DATA}}
			 * 可用余额：{{keyword5.DATA}}
			 * {{remark.DATA}}
			 */
			Template.send(token, new Template.Builder()
					.setToUser(wxMember.getSerial())
					.setTemplateId(wxRestaurant.getChargeTemplate())
					.addKeyword(new Keyword("first", "亲爱的会员，充值已成功"))
					.addKeyword(new Keyword("keyword1", wxRestaurant.getNickName()))		//餐厅名称
					.addKeyword(new Keyword("keyword2", DateUtil.format(mo.getOperateDate())))	//充值时间
					.addKeyword(new Keyword("keyword3", NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(mo.getChargeMoney())))	//充值金额
					.addKeyword(new Keyword("keyword4", NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(mo.getDeltaBaseMoney() + mo.getDeltaExtraMoney() - mo.getChargeMoney())))				//赠送金额
					.addKeyword(new Keyword("keyword5", NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(mo.getRemainingTotalMoney())))  //可用余额
					.addKeyword(new Keyword("remark", "如有疑问，请联系商家客服。"))
					);
		}	
		return null;
	}
	
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
//			String appId = "wx49b3278a8728ff76";
//			String appSecret = "0ba130d87e14a1a37e20c78a2b0ee3ba";
//			final Token token = Token.newInstance(appId, appSecret);
			final AuthorizerToken authorizerToken = AuthorizerToken.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, wxRestaurant.getWeixinAppId(), wxRestaurant.getRefreshToken());
			final Token token = Token.newInstance(authorizerToken);
			/**
			 * {{first.DATA}}
			 * 消费金额：{{keyword1.DATA}}
			 * 消费门店：{{keyword2.DATA}}
			 * 获得积分：{{keyword3.DATA}}
			 * 当前积分：{{keyword4.DATA}}
			 * 当前余额：{{keyword5.DATA}}
			 * {{remark.DATA}}
			 */
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

package com.wireless.Actions.weixin;

import java.sql.SQLException;
import java.util.Date;

import org.marker.weixin.DefaultSession;
import org.marker.weixin.HandleMessageListener;
import org.marker.weixin.msg.Data4Item;
import org.marker.weixin.msg.Msg;
import org.marker.weixin.msg.Msg4Event;
import org.marker.weixin.msg.Msg4Image;
import org.marker.weixin.msg.Msg4ImageText;
import org.marker.weixin.msg.Msg4Link;
import org.marker.weixin.msg.Msg4Location;
import org.marker.weixin.msg.Msg4Text;
import org.marker.weixin.msg.Msg4Video;
import org.marker.weixin.msg.Msg4Voice;

import com.wireless.Actions.init.InitServlet;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.weixin.member.WeixinMemberDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.util.DateUtil;

public class WeiXinRestaurantHandleMessageAdapter implements HandleMessageListener {
	private static final String WEIXIN_BASE_SERVER = InitServlet.getConfig().getInitParameter("weixin_callback_address");
	
	public static final String WEIXIN_INDEX = WEIXIN_BASE_SERVER + "/weixin/order/index.html";
	public static final String WEIXIN_FOOD = WEIXIN_BASE_SERVER + "/weixin/order/food.html";
	public static final String WEIXIN_RFOOD = WEIXIN_BASE_SERVER + "/weixin/order/rfood.html";
	public static final String WEIXIN_ABOUT = WEIXIN_BASE_SERVER + "/weixin/order/about.html";
	public static final String WEIXIN_SALES = WEIXIN_BASE_SERVER + "/weixin/order/sales.html";
	public static final String WEIXIN_MEMBER = WEIXIN_BASE_SERVER + "/weixin/order/member.html";
	
	public static final String WEIXIN_FOOD_ICON = WEIXIN_BASE_SERVER + "/weixin/order/images/icon_food.png";
	public static final String WEIXIN_RFOOD_ICON = WEIXIN_BASE_SERVER + "/weixin/order/images/icon_rfood.png";
	public static final String WEIXIN_ABOUT_ICON = WEIXIN_BASE_SERVER + "/weixin/order/images/icon_about.png";
	public static final String WEIXIN_SALES_ICON = WEIXIN_BASE_SERVER + "/weixin/order/images/icon_sales.png";
	public static final String WEIXIN_MEMBER_ICON = WEIXIN_BASE_SERVER + "/weixin/order/images/icon_member.png";
	
	public static final String COMMAND_HELP = "H";
	public static final String COMMAND_MENU = "M";
	
	private Restaurant restaurant;
	private Msg4Text text;
	private Msg4ImageText imageText;
	private Data4Item dataItem;
	private Msg msg;
	private int rid;
	private DefaultSession session;
	private String account;
	
	public WeiXinRestaurantHandleMessageAdapter(DefaultSession session, String account){
		this.session = session;
		this.account = account;
	}
	
	/**
	 * 初始化单次消息处理内容
	 * @param msg
	 * @throws SQLException
	 * @throws BusinessException
	 */
	private void init(Msg msg) throws SQLException, BusinessException{
		if(msg == null){
			throw new NullPointerException("当前时间: " + DateUtil.format(new Date()) + "\n 错误: 接收公众平台回发信息失败.");
		}else{
			this.msg = msg;
		}
		// 绑定餐厅和公众平台信息
		WeixinRestaurantDao.bind(msg.getToUserName(), account);
		// 获取公众平台对应餐厅信息
		if(restaurant == null){
			rid = WeixinRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName());
			restaurant = RestaurantDao.getById(rid);
		}
		
	}
	/**
	 * 初始化文本回复信息对象
	 */
	private void initText(){
		text = new Msg4Text();
		text.setFromUserName(msg.getToUserName());
		text.setToUserName(msg.getFromUserName());
	}
	/**
	 * 初始化图片文本回复信息对象
	 */
	private void initImageText(){
		imageText = new Msg4ImageText();
		imageText.setFromUserName(msg.getToUserName());
		imageText.setToUserName(msg.getFromUserName()); 
		imageText.setCreateTime(msg.getCreateTime());
	}
	private String createUrl(String url){
		StringBuilder target = new StringBuilder();
		target.append(url).append("?_d=" + (int)(Math.random() * 1000000))
			.append("&m=").append(imageText.getToUserName())
			.append("&r=").append(imageText.getFromUserName());
		return target.toString();
	}
	
	/**
	 * 解释用户对话操作指令
	 * @param order
	 * @throws BusinessException 
	 * @throws SQLException 
	 */
	private void explainOrder(String order) throws SQLException, BusinessException{
		if(order == null){
			return;
		}else{
			order = order.trim().toLowerCase();
		}
		if("1".equals(order) || COMMAND_MENU.equals(order.toUpperCase())){
			initImageText();
			
			String logo;
			try {
				logo = WeixinRestaurantDao.getLogo(rid);
				if(logo == null || logo.trim().isEmpty()){
					logo = InitServlet.getConfig().getInitParameter("imageBrowseDefaultFile");
				}else{
					logo = "http://" + InitServlet.getConfig().getInitParameter("oss_bucket_image")
							+ "." + InitServlet.getConfig().getInitParameter("oss_outer_point") 
							+ "/" + logo;
				}
			} catch (SQLException e) {
				System.out.println("获取餐厅LOGO信息失败.");
				logo = InitServlet.getConfig().getInitParameter("imageBrowseDefaultFile");
			}
			
			dataItem = new Data4Item(" ", " ", logo, createUrl(WEIXIN_INDEX)); 
			imageText.addItem(dataItem);
			
//			dataItem = new Data4Item();
//			dataItem.setTitle("主页");
//			dataItem.setUrl(createUrl(WEIXIN_INDEX));
//			imageText.addItem(dataItem);
			
			dataItem = new Data4Item();
			dataItem.setTitle("自助点餐");
			dataItem.setUrl(createUrl(WEIXIN_FOOD));
			dataItem.setPicUrl(WEIXIN_FOOD_ICON);
			imageText.addItem(dataItem);
			
			dataItem = new Data4Item();
			dataItem.setTitle("特色菜品");
			dataItem.setUrl(createUrl(WEIXIN_RFOOD));
			dataItem.setPicUrl(WEIXIN_RFOOD_ICON);
			imageText.addItem(dataItem);
			
			dataItem = new Data4Item();
			try{
				WeixinMemberDao.getBoundMemberIdByWeixin(msg.getFromUserName(), msg.getToUserName());
/*				Staff staff = StaffDao.getByRestaurant(rid).get(0);
				List<Coupon> couponList = CouponDao.getAvailByMember(staff, memberId);
				if(couponList.size() > 0){
					dataItem.setTitle("会员资料 | 您有新优惠劵!");
				}else{
					dataItem.setTitle("会员资料");
				}*/
				dataItem.setTitle("会员资料");
			}catch(BusinessException e){
				dataItem.setTitle("会员资料 | 请绑定会员");
			}
			
			dataItem.setUrl(createUrl(WEIXIN_MEMBER));
			dataItem.setPicUrl(WEIXIN_MEMBER_ICON);
			imageText.addItem(dataItem);
			
			dataItem = new Data4Item();
			dataItem.setTitle("优惠信息");
			dataItem.setUrl(createUrl(WEIXIN_SALES));
			dataItem.setPicUrl(WEIXIN_SALES_ICON);
			imageText.addItem(dataItem);
			
			dataItem = new Data4Item();
			dataItem.setTitle("餐厅简介");
			dataItem.setUrl(createUrl(WEIXIN_ABOUT));
			dataItem.setPicUrl(WEIXIN_ABOUT_ICON);
			imageText.addItem(dataItem);
			
			
			session.callback(imageText);
		}else if(COMMAND_HELP.equals(order.toUpperCase())){
			initText();
			text.setContent("餐厅编号:"+restaurant.getId()+"\nToUserName(openID:公众号):"+msg.getToUserName()+"\nFromUserName(开发者微信号:手机微信号):"+msg.getFromUserName());
			session.callback(text);
		}else{
			initText();
			text.setContent(new StringBuilder().append("未知命令\n")
					.append("回复【h】获取帮助信息\n")
					.append("回复【m】获得主菜单")
					.toString());
			session.callback(text);
		}
	}
	
	/**
	 * 文本信息, 操作请求指令集
	 */
	public void onTextMsg(Msg4Text msg) {
//		System.out.println("微信餐厅收到消息：" + msg.getContent());
		try{
			init(msg);
			explainOrder(msg.getContent());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 推送事件, 关注或取消关注使用
	 */
	public void onEventMsg(Msg4Event msg) {
		try{
			init(msg);
			if(msg.getEvent().equals(Msg4Event.SUBSCRIBE)){
				//System.out.println("微信餐厅->添加关注: " + msg.getToUserName());
				WeixinMemberDao.interest(msg.getToUserName(), msg.getFromUserName());
				explainOrder(COMMAND_MENU);
			}else if(msg.getEvent().equals(Msg4Event.UNSUBSCRIBE)){
//				System.out.println("微信餐厅->取消关注: " + msg.getToUserName());
				WeixinMemberDao.cancel(msg.getFromUserName(), msg.getToUserName());
			}else{
//				System.out.println("微信餐厅->菜单回调事件, 推送指令: " + msg.getEventKey());
				explainOrder(msg.getEventKey());
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void onImageMsg(Msg4Image msg4image) { }

	@Override
	public void onLinkMsg(Msg4Link msg4link) { }

	@Override
	public void onLocationMsg(Msg4Location msg4location) { }

	@Override
	public void onErrorMsg(int i) { }

	@Override
	public void onVideoMsg(Msg4Video arg0) { }

	@Override
	public void onVoiceMsg(Msg4Voice arg0) { }
	
}

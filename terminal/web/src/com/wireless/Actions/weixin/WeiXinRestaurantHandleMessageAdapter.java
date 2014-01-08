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

import com.wireless.db.DBCon;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.util.DateUtil;

public class WeiXinRestaurantHandleMessageAdapter implements HandleMessageListener {
	public static final String WEIXIN_INDEX = "http://42.121.54.177/web-term/weixin/order/index.html";
	public static final String WEIXIN_FOOD = "http://42.121.54.177/web-term/weixin/order/food.html";
	public static final String WEIXIN_RFOOD = "http://42.121.54.177/web-term/weixin/order/rfood.html";
	public static final String WEIXIN_ABOUT = "http://42.121.54.177/web-term/weixin/order/about.html";
	public static final String WEIXIN_SALES = "http://42.121.54.177/web-term/weixin/order/sales.html";
	public static final String WEIXIN_MEMBER = "http://42.121.54.177/web-term/weixin/order/member.html";
	
	public static final String COMMAND_HELP = "H";
	public static final String COMMAND_MENU = "M";
	
	private DBCon dbCon;
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
		if(msg == null) throw new NullPointerException("当前时间: " + DateUtil.format(new Date()) + "\n 错误: 接收公众平台回发信息失败.");
		else this.msg = msg;
//		System.out.println("FromID: " + msg.getToUserName());
//		System.out.println("OpenID: " + msg.getFromUserName());
//		System.out.println("account: " + account);
		if(dbCon == null){
			dbCon = new DBCon();
			dbCon.connect();
		}
		// 绑定餐厅和公众平台信息
		WeixinRestaurantDao.bind(dbCon, msg.getToUserName(), account);
		// 获取公众平台对应餐厅信息
		if(restaurant == null){
			rid = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, msg.getToUserName());
			restaurant = RestaurantDao.getById(dbCon, rid);
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
	 */
	private void explainOrder(String order){
		if(order == null) return;
		else order = order.trim().toLowerCase();
		if("1".equals(order) || COMMAND_MENU.equals(order.toUpperCase())){
			initImageText();
			
			dataItem = new Data4Item("logo", "最新优惠信息", "http://42.121.54.177/web-term/weixin/title-image.jpg", WEIXIN_SALES); 
			imageText.addItem(dataItem);
			
			dataItem = new Data4Item();
			dataItem.setTitle("主页");
			dataItem.setUrl(createUrl(WEIXIN_INDEX));
			imageText.addItem(dataItem);
			
			dataItem = new Data4Item();
			dataItem.setTitle("自助点餐");
			dataItem.setUrl(createUrl(WEIXIN_FOOD));
			imageText.addItem(dataItem);
			
			dataItem = new Data4Item();
			dataItem.setTitle("特色菜品");
			dataItem.setUrl(createUrl(WEIXIN_RFOOD));
			imageText.addItem(dataItem);
			
			dataItem = new Data4Item();
			dataItem.setTitle("优惠信息");
			dataItem.setUrl(createUrl(WEIXIN_SALES));
			imageText.addItem(dataItem);
			
			dataItem = new Data4Item();
			dataItem.setTitle("餐厅简介");
			dataItem.setUrl(createUrl(WEIXIN_ABOUT));
			imageText.addItem(dataItem);
			
			dataItem = new Data4Item();
			dataItem.setTitle("会员资料");
			dataItem.setUrl(createUrl(WEIXIN_MEMBER));
			imageText.addItem(dataItem);
			
			session.callback(imageText);
		}else if(COMMAND_HELP.equals(order.toUpperCase())){
			initText();
			text.setContent("餐厅编号:"+restaurant.getId()+"\nToUserName(openID):"+msg.getFromUserName()+"\nFromUserName(开发者微信号):"+msg.getToUserName());
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
		System.out.println("微信餐厅收到消息：" + msg.getContent());
		try{
			init(msg);
			explainOrder(msg.getContent());
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			safeClose();
		}
	}
	
	/**
	 * 推送事件, 关注或取消关注使用
	 */
	public void onEventMsg(Msg4Event msg) {
		try{
			init(msg);
			if(msg.getEvent().equals(Msg4Event.SUBSCRIBE)){
				System.out.println("微信餐厅->添加关注: " + msg.getToUserName());
				initText();
				text.setContent(new StringBuilder().append("谢谢您的支持.\n")
						.append("回复【h】获取帮助信息\n")
						.append("回复【m】获得主菜单")
						.toString());
				session.callback(text);
			}else if(msg.getEvent().equals(Msg4Event.UNSUBSCRIBE)){
				System.out.println("微信餐厅->取消关注: " + msg.getToUserName());
			}else{
				System.out.println("微信餐厅->菜单回调事件, 推送指令: " + msg.getEventKey());
				explainOrder(msg.getEventKey());
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			safeClose();
		}
	}
	
	private void safeClose(){
		if(dbCon != null) dbCon.disconnect();
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

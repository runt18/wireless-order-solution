package com.wireless.Actions.weixin;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.swing.ImageIcon;
import javax.xml.parsers.ParserConfigurationException;

import org.marker.weixin.HandleMessageAdapter;
import org.marker.weixin.msg.Data4Item;
import org.marker.weixin.msg.Msg;
import org.marker.weixin.msg.Msg4Event;
import org.marker.weixin.msg.Msg4Event.Event;
import org.marker.weixin.msg.Msg4Head.MsgType;
import org.marker.weixin.msg.Msg4ImageText;
import org.marker.weixin.msg.Msg4Text;
import org.marker.weixin.session.WxSession;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.alibaba.fastjson.JSON;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.book.BookDao;
import com.wireless.db.member.MemberDao;
import com.wireless.db.member.represent.RepresentDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.promotion.PromotionDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.action.WxKeywordDao;
import com.wireless.db.weixin.action.WxMenuActionDao;
import com.wireless.db.weixin.member.WxMemberDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.book.Book;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.represent.Represent;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.pojo.weixin.action.WxKeyword;
import com.wireless.pojo.weixin.action.WxMenuAction;
import com.wireless.pojo.weixin.order.WxOrder;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;

public class WxHandleMessage extends HandleMessageAdapter {
	
	public static enum QrCodeType{
		WAITER(1, "微信店小二"),
		TEMP_PAY(2, "账单暂结"),
		REPRESENT(3, "我要代言");
		
		public final int val;
		public final String desc;
		QrCodeType(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static QrCodeType valueOf(int val){
			for(QrCodeType type : values()){
				if(val == type.val){
					return type;
				}
			}
			throw new IllegalArgumentException("The qr code type(val = " + val + ") is invalid.");
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	public static class QrCodeParam{
		private final QrCodeType type;
		private final int param;
		
		private QrCodeParam(QrCodeType type, int param){
			this.type = type;
			this.param = param;
		}
		
		public static QrCodeParam newWaiter(int orderId){
			return new QrCodeParam(QrCodeType.WAITER, orderId);
		}
		
		public static QrCodeParam newTempPay(int orderId){
			return new QrCodeParam(QrCodeType.TEMP_PAY, orderId);
		}
		
		public static QrCodeParam newRepresent(int memberId){
			return new QrCodeParam(QrCodeType.REPRESENT, memberId);
		}
		
		public static QrCodeParam parse(String qrParam){
			return new QrCodeParam(
					QrCodeType.valueOf(Integer.parseInt(qrParam.substring(0, 1))),
					Integer.parseInt(qrParam.substring(1)));
		}
		
		public int sceneId(){
			return Integer.parseInt(toString());
		}
		
		@Override
		public String toString(){
			return type.val + "" + param;
		}
	}
	
	private final String WEIXIN_INDEX;
	private final String WEIXIN_FOOD;
	private final String WEIXIN_BOOK;
	private final String WEIXIN_RFOOD;
	private final String WEIXIN_ABOUT;
	private final String WEIXIN_MEMBER;
	private final String WEIXIN_COUPON;
	private final String WEIXIN_ORDER;
	private final String WEIXIN_DIANPING;
	private final String WEIXIN_SCANNING;
	private final String WEIXIN_SCANNING_RESULT;
	private final String WEIXIN_WAITER;
	private final String WEIXIN_REPRESENT;
	
	private final String WEIXIN_FOOD_ICON;
	private final String WEIXIN_BOOK_ICON;
	private final String WEIXIN_RFOOD_ICON;
	private final String WEIXIN_ABOUT_ICON;
	private final String WEIXIN_DIANPING_ICON;
	private final String WEIXIN_DEFAULT_LOGO;
	
	private final HttpServletRequest request;
	
	public static enum EventKey{
		SELF_ORDER_EVENT_KEY("self_order_event_key", "自助点餐"),
		SELF_BOOK_EVENT_KEY("self_book_event_key", "自助预订"),
		INTRO_EVENT_KEY("intro_event_key","餐厅简介"),
		STAR_EVENT_KEY("star_event_key", "明星菜品"),
		NAVI_EVENT_KEY("navi_event_key", "餐厅导航"),
		PROMOTION_EVENT_KEY("promotion_event_key", "优惠活动"),
		MEMBER_EVENT_KEY("member_event_key", "我的会员卡"),
		ORDER_EVENT_KEY("order_event_key", "我的订单"),
		MY_QRCODE_EVENT_KEY("my_qrcode_event_key", "我的二维码"),
		SCAN_EVENT_KEY("scan_event_key", "扫一扫"),
		I_WANT_REPRESENT("i_want_represent", "我要代言");
		
		EventKey(String val, String desc){
			this.val = val;
			this.desc = desc;
		}
		private final String val;
		private final String desc;
		
		public String getKey(){
			return this.val;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	public WxHandleMessage(HttpServletRequest request, WxSession session, String root){
		super(session);
		
		this.request = request;
		
		this.WEIXIN_INDEX = root + "/weixin/order/index.html";
		this.WEIXIN_FOOD = root + "/weixin/order/branches.html?redirect_url=food.html";
		this.WEIXIN_BOOK = root + "/weixin/order/branches.html?redirect_url=book.html";
		this.WEIXIN_RFOOD = root + "/weixin/order/rfood.html";
		this.WEIXIN_ABOUT = root + "/weixin/order/about.html";
		this.WEIXIN_MEMBER = root + "/weixin/order/member.html";
		this.WEIXIN_COUPON = root + "/weixin/order/sales.html";
		this.WEIXIN_ORDER = root + "/weixin/order/orderList.html";
		this.WEIXIN_DIANPING = root + "/weixin/order/dianping.html";
		this.WEIXIN_SCANNING = root + "/weixin/order/scan.html";
		this.WEIXIN_SCANNING_RESULT = root + "/weixin/order/scanResult.html";
		this.WEIXIN_WAITER = root + "/weixin/order/waiter.html";
		//TODO
		this.WEIXIN_REPRESENT = root + "/weixin/order/representCard.html";
		
		this.WEIXIN_FOOD_ICON = root + "/weixin/order/images/icon_food.png";
		this.WEIXIN_BOOK_ICON = root + "/weixin/order/images/icon_book.jpg";
		this.WEIXIN_RFOOD_ICON = root + "/weixin/order/images/icon_rfood.png";
		this.WEIXIN_ABOUT_ICON = root + "/weixin/order/images/icon_about.png";
		this.WEIXIN_DIANPING_ICON = root + "/weixin/order/images/dianping1.png";
		this.WEIXIN_DEFAULT_LOGO = "http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/WxLogo/default.jpg";
	}
	
	private Msg appendUrlParam(Msg from, Msg msg4Action){
		if(msg4Action.getHead().getMsgType() == MsgType.MSG_TYPE_IMAGE_TEXT){
			for(Data4Item item : ((Msg4ImageText)msg4Action).getItems()){
				if(item.hasUrl()){
					item.setUrl(createUrl(from, item.getUrl()) + "&wecha_id=" + from.getFromUserName());
				}
			}
		}
		return msg4Action;
	}
	
	private String createUrl(Msg msg, String url){
		StringBuilder s = new StringBuilder();
		s.append(url);
		if(!url.contains("?")){
			s.append("?1=1");
		}
		return s.append("&_d=" + System.currentTimeMillis())
				.append("&m=" + msg.getFromUserName())
				.append("&r=" + msg.getToUserName())
				//.append("&time=" + System.currentTimeMillis())
				.toString();
		
	}
	
	private String createUrl4Session(String url, HttpSession session){
		StringBuilder s = new StringBuilder();
		s.append(url);
		if(!url.contains("?")){
			s.append("?1=1");
		}
		return s.append("&sessionId=" + session.getId()).toString();
	}
	
	private Msg createNavi(Msg msg){
		
		Msg4ImageText naviItem = new Msg4ImageText(msg);

		Data4Item mainItem;
		Restaurant restaurant = null;
		try {
			//restaurant = RestaurantDao.getByAccount(account);
			restaurant = RestaurantDao.getById(WxRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName()));
			WxRestaurant wr = WxRestaurantDao.get(StaffDao.getAdminByRestaurant(restaurant.getId()));
			if(wr.getWeixinLogo() != null){
				mainItem = new Data4Item(restaurant.getName(), "", wr.getWeixinLogo().getObjectUrl(), createUrl(msg, WEIXIN_INDEX)); 
			}else{
				mainItem = new Data4Item(restaurant != null ? restaurant.getName() : "", "点击查看主页", WEIXIN_DEFAULT_LOGO, createUrl(msg, WEIXIN_INDEX));
			}
		} catch (SQLException | BusinessException e) {
			mainItem = new Data4Item(restaurant != null ? restaurant.getName() : "", "点击查看主页", WEIXIN_DEFAULT_LOGO, createUrl(msg, WEIXIN_INDEX));
		}
		
		naviItem.addItem(mainItem);
		
		Data4Item item4Order = new Data4Item();
		item4Order.setTitle("自助点餐");
		item4Order.setUrl(createUrl(msg, WEIXIN_FOOD)+ "&e=" + WxOrder.Type.INSIDE.getVal());
		item4Order.setPicUrl(WEIXIN_FOOD_ICON);
		naviItem.addItem(item4Order);
		
		Data4Item item4Book = new Data4Item();
		item4Book.setTitle("自助预订");
		item4Book.setUrl(createUrl(msg, WEIXIN_BOOK));
		item4Book.setPicUrl(WEIXIN_BOOK_ICON);
		naviItem.addItem(item4Book);		
		
		Data4Item specialFoodItem = new Data4Item();
		specialFoodItem.setTitle("特色菜品");
		specialFoodItem.setUrl(createUrl(msg, WEIXIN_RFOOD));
		specialFoodItem.setPicUrl(WEIXIN_RFOOD_ICON);
		naviItem.addItem(specialFoodItem);
		
		
		if(restaurant.getDianpingId() > 0 && hasDianping(restaurant.getDianpingId())){
			Data4Item dianpingItem = new Data4Item();
			dianpingItem.setTitle("大众团购");
			dianpingItem.setUrl(createUrl(msg, WEIXIN_DIANPING));
			dianpingItem.setPicUrl(WEIXIN_DIANPING_ICON);
			naviItem.addItem(dianpingItem);			
		}
		
		Data4Item item4TakeOut = new Data4Item();
		item4TakeOut.setTitle("外卖点餐");
		item4TakeOut.setUrl(createUrl(msg, WEIXIN_FOOD) + "&e=" + WxOrder.Type.TAKE_OUT.getVal());
		item4TakeOut.setPicUrl(WEIXIN_FOOD_ICON);
//		naviItem.addItem(item4TakeOut);		


		Data4Item intrcItem = new Data4Item();
		intrcItem.setTitle("餐厅简介");
		intrcItem.setUrl(createUrl(msg, WEIXIN_ABOUT));
		intrcItem.setPicUrl(WEIXIN_ABOUT_ICON);
		naviItem.addItem(intrcItem);
		
		return naviItem;
		
	}
	
	/**
	 * 文本信息, 关键字回复
	 */
	@Override
	public void onTextMsg(Msg4Text msg) {
		try {
			final Staff staff = StaffDao.getAdminByRestaurant(WxRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName()));
			final List<WxKeyword> matched = WxKeywordDao.getByCond(staff, new WxKeywordDao.ExtraCond().setKeyword(msg.getContent()));
			final int actionId;
			if(matched.isEmpty()){
				actionId = WxKeywordDao.getByCond(staff, new WxKeywordDao.ExtraCond().setType(WxKeyword.Type.EXCEPTION)).get(0).getActionId();
			}else{
				actionId = matched.get(0).getActionId();
			}
			if(actionId != 0){
				session.callback(new WxMenuAction.MsgProxy(msg.getHead(), WxMenuActionDao.getById(staff, actionId)).toMsg());
			}else{
				session.callback(createNavi(msg));
			}
		} catch (SQLException | BusinessException | SAXException | IOException | ParserConfigurationException e) {
			session.callback(new Msg4Text(msg, e.getMessage()));
		}
	}
	
	/**
	 * 推送事件, 关注或取消关注，菜单回复使用
	 */
	@Override
	public void onEventMsg(Msg4Event msg) {
		try{
			// 绑定餐厅和公众平台信息
			//WxRestaurantDao.bind(msg.getToUserName(), RestaurantDao.getById(WxRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName())).getAccount());
			
			if(msg.getEvent() == Event.SUBSCRIBE){
				//会员关注
				Staff staff = StaffDao.getAdminByRestaurant(WxRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName()));
				WxMemberDao.interest(staff, msg.getFromUserName());
				if(msg.getTicket().isEmpty()){
					final List<WxMenuAction> reply = WxMenuActionDao.getByCond(staff, new WxMenuActionDao.ExtraCond().setCate(WxMenuAction.Cate.SUBSCRIBE_REPLY));
					if(reply.isEmpty()){
						session.callback(createNavi(msg));
					}else{
						session.callback(appendUrlParam(msg, new WxMenuAction.MsgProxy(msg.getHead(), reply.get(0)).toMsg()));
					}
	
				}else{
					//扫描带参二维码
					processQrCode(msg);

				}				
			}else if(msg.getEvent() == Event.UNSUBSCRIBE){
				//会员取消关注
				//WeixinMemberDao.cancel(msg.getFromUserName(), msg.getToUserName());
				
			}else if(msg.getEvent() == Event.SCAN){
				//扫描带参二维码
				processQrCode(msg);
				
			}else if(msg.getEvent() == Event.CLICK){
				final int restaurantId = WxRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName());
				final Staff staff = StaffDao.getAdminByRestaurant(restaurantId);

				WxRestaurant wxRestaurant = WxRestaurantDao.get(staff);
				String picUrl;
				if(wxRestaurant.hasWeixinLogo()){
					picUrl = wxRestaurant.getWeixinLogo().getObjectUrl();
				}else{
					picUrl = "";
				}
				if(msg.getEventKey().equals(EventKey.NAVI_EVENT_KEY.val)){
					//餐厅导航
					session.callback(createNavi(msg));
					
				}else if(msg.getEventKey().equals(EventKey.SELF_BOOK_EVENT_KEY.val)){
					//自助预订
					session.callback(new Msg4ImageText(msg).addItem(new Data4Item("自助预订", "点击去预订", picUrl, createUrl(msg, WEIXIN_BOOK))));
					
				}else if(msg.getEventKey().equals(EventKey.SELF_ORDER_EVENT_KEY.val)){
					//自助点餐
					session.callback(new Msg4ImageText(msg).addItem(new Data4Item("自助点餐", "点击去自助点餐", picUrl, createUrl(msg, WEIXIN_FOOD))));
					
				}else if(msg.getEventKey().equals(EventKey.INTRO_EVENT_KEY.val)){
					//餐厅简介
					session.callback(new Msg4ImageText(msg).addItem(new Data4Item("餐厅简介", "点击去餐厅简介", picUrl, createUrl(msg, WEIXIN_ABOUT))));
					
				}else if(msg.getEventKey().equals(EventKey.STAR_EVENT_KEY.val)){
					//明星菜品
					session.callback(new Msg4ImageText(msg).addItem(new Data4Item("明星菜品", "点击去明星菜品", picUrl, createUrl(msg, WEIXIN_RFOOD))));
					
				}else if(msg.getEventKey().equals(EventKey.MY_QRCODE_EVENT_KEY.val)){
					//我的二维码
					Member member = MemberDao.getByWxSerial(StaffDao.getAdminByRestaurant(WxRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName())), msg.getFromUserName());
					final String qrCodeUrl = "http://qr.liantu.com/api.php?text=" + member.getWeixin().getCard() + "&w=70";
					session.callback(new Msg4ImageText(msg).addItem(new Data4Item("我的二维码", "扫描二维码完成会员注入", qrCodeUrl, qrCodeUrl)));
					
				}else if(msg.getEventKey().equals(EventKey.PROMOTION_EVENT_KEY.val)){
					//最新优惠
					
					final List<Promotion> promotions = PromotionDao.getByCond(staff, new PromotionDao.ExtraCond().setStatus(Promotion.Status.PROGRESS));
					
					if(promotions.isEmpty()){
						session.callback(new Msg4ImageText(msg).addItem(new Data4Item("亲。。。暂时还没有优惠活动哦", "请留意我们的微信优惠通知哦", "", "")));
					}else{
						Msg4ImageText couponItem = new Msg4ImageText(msg);
						if(promotions.size() == 1){
							Promotion promotion = PromotionDao.getById(staff, promotions.get(0).getId());
							//只有一个优惠活动
							StringBuilder desc = new StringBuilder();
							//活动时间
							desc.append("活动结束时间：" + promotion.getDateRange().getEndingFormat()).append("\n");
							
							desc.append("\n点击查看优惠活动详情>>>>");
							
							couponItem.addItem(new Data4Item(promotion.getTitle(), 
															 desc.toString(), 
															 promotion.getCouponType().hasImage() ? promotion.getCouponType().getImage().getObjectUrl() : "", 
															 createUrl(msg, WEIXIN_COUPON) + "&pid=" + promotion.getId()));
							
						}else{
							
							//多个优惠活动
							for(int i = 0; i < promotions.size(); i++){
								promotions.set(i, PromotionDao.getById(staff, promotions.get(i).getId()));
							}
							
							Collections.sort(promotions, new Comparator<Promotion>(){
								@Override
								public int compare(Promotion p1, Promotion p2) {
									if(p1.getDateRange().getOpeningTime() < p2.getDateRange().getOpeningTime()){
										return -1;
									}else if(p1.getDateRange().getOpeningTime() > p2.getDateRange().getOpeningTime()){
										return 1;
									}else{
										return 0;
									}
								}
								
							});
							for(Promotion promotion : promotions){
								if(promotion.getCouponType().hasImage()){
									picUrl = promotion.getCouponType().getImage().getObjectUrl();
								}else{
									picUrl = "";
								}
								couponItem.addItem(new Data4Item(promotion.getTitle(), "", 
												   picUrl, createUrl(msg, WEIXIN_COUPON) + "&pid=" + promotion.getId()));
							}
						}
						session.callback(couponItem);
					}
					
				}else if(msg.getEventKey().equals(EventKey.MEMBER_EVENT_KEY.val)){
					//我的会员卡
					try{
						Member member = MemberDao.getByWxSerial(StaffDao.getAdminByRestaurant(WxRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName())), msg.getFromUserName());
						StringBuilder title = new StringBuilder();
						title.append("亲爱的" + member.getName());
						if(member.getTotalBalance() > 0){
							title.append("，您的余额" + NumericUtil.float2String2(member.getTotalBalance()) + "元");
						}
						if(member.getTotalPoint() > 0){
							title.append("，可用积分" + NumericUtil.float2String2(member.getTotalPoint()) + "分");
						}
						
						final String qrCodeUrl = "http://qr.liantu.com/api.php?text=" + member.getWeixin().getCard() + "&w=70";

						session.callback(new Msg4ImageText(msg).addItem(new Data4Item(title.toString(), "1、店员扫描我的二维码完成会员注入\r\n2、点击查看您的更多信息>>>", qrCodeUrl, createUrl(msg, WEIXIN_MEMBER))));
						
					}catch(BusinessException | SQLException e){
						//session.callback(new Msg4ImageText(msg).addItem(new Data4Item("亲。。。您的微信会员卡还未激活哦 :-(", "点击激活您的微信会员卡", "", createUrl(msg, WEIXIN_MEMBER))));
						session.callback(new Msg4ImageText(msg).addItem(new Data4Item(e.getMessage(), "", "", "")));
					}
						
				}else if(msg.getEventKey().equals(EventKey.ORDER_EVENT_KEY.val)){
					
					final List<WxOrder> orders = WxOrderDao.getByCond(staff, new WxOrderDao.ExtraCond().setWeixin(msg.getFromUserName()).addStatus(WxOrder.Status.COMMITTED), " ORDER BY birth_date DESC");
					
					//只显示当月的预订订单
					final BookDao.ExtraCond extraCond = new BookDao.ExtraCond();
					SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyy-MM-dd");
					String begin = yyyymmdd.format(new Date());
					Calendar c = Calendar.getInstance();
					c.add(Calendar.MONTH, 1);
					String end = yyyymmdd.format(c.getTime());
					
					extraCond.setBookRange(new DutyRange(begin, end));
					
					//只显示【创建】状态的预订订单
					extraCond.addStatus(Book.Status.CREATED);
					final List<Book> books = BookDao.getByCond(staff, extraCond);
					
					final StringBuilder title = new StringBuilder();
					String description = "";
					
					if(orders.isEmpty() && books.isEmpty()){
						description = "点击去自助点餐";
						session.callback(new Msg4ImageText(msg).addItem(new Data4Item("暂无订单", description, "", createUrl(msg, WEIXIN_FOOD))));
					}else{
						title.append("您有");
						if(!orders.isEmpty()){
							title.append("微信订单" + orders.size() + "张");
						}
						if(!books.isEmpty()){
							if(title.length() > 0){
								title.append(",");
							}
							title.append("微信预订" + books.size() + "张");
						}
						session.callback(new Msg4ImageText(msg).addItem(new Data4Item(title.toString(), description, "", createUrl(msg, WEIXIN_ORDER))));
					}
					
				}else if(msg.getEventKey().equals(EventKey.SCAN_EVENT_KEY.val)){
					//我的二维码
					session.callback(new Msg4ImageText(msg).addItem(new Data4Item("点击此处开始扫描", "点我扫描支付二维码", "", createUrl(msg, WEIXIN_SCANNING))));
					
				}else if(msg.getEventKey().equals(EventKey.I_WANT_REPRESENT.val)){
					//TODO 我要代言
					final Represent represent = RepresentDao.getByCond(staff, null).get(0);
					final Restaurant restaurant = RestaurantDao.getById(staff.getRestaurantId());
					String title = represent.getTitle().isEmpty() ? "我要代言" : represent.getTitle();
					String desc = ("【代言规则】：成功将代言海报中【$(restaurant)】的二维码分享给您的好友，好友扫描此二维码成为您的粉丝，" +
								  "即可获得$(recommend_money)元的充值赠额和$(recommend_point)的赠送积分。" +
								  "\r\n点击去分享代言海报>>>>>")
								  .replace("$(restaurant)", restaurant.getName())
								  .replace("$(recommend_money)", Float.toString(represent.getRecommentMoney()))
								  .replace("$(recommend_point)", Integer.toString(represent.getRecommendPoint()));
					session.callback(new Msg4ImageText(msg).addItem(new Data4Item(title, desc, picUrl, createUrl(msg, WEIXIN_REPRESENT))));
					
				}else{
					
					Msg msg4Action = new WxMenuAction.MsgProxy(msg.getHead(), WxMenuActionDao.getById(staff, Integer.parseInt(msg.getEventKey()))).toMsg();
					session.callback(appendUrlParam(msg, msg4Action));
					
				}
				
			}else if(msg.getEvent() == Event.SCAN_WAIT_MSG){
				if(msg.getEventKey().equals(EventKey.SCAN_EVENT_KEY.val)){
					Staff staff = StaffDao.getAdminByRestaurant(WxRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName()));
					Member member = MemberDao.getById(staff, WxMemberDao.getBySerial(staff, msg.getFromUserName()).getMemberId());
					int orderId = Integer.parseInt(msg.getScanResult().substring(msg.getScanResult().indexOf("?") + 1));
					OrderDao.discount(staff, Order.DiscountBuilder.build4Member(orderId, member));
					Order order = OrderDao.getById(staff, orderId, DateType.TODAY);
					StringBuilder body = new StringBuilder();
					body.append("账单号：" + order.getId()).append("，")
						.append("原价：" + NumericUtil.float2String2(order.calcPureTotalPrice()) + "元").append("，")
						.append("会员价：" + NumericUtil.float2String2(order.calcTotalPrice()) + "元").append("，")
						.append("点击查看账单详情");
					session.callback(new Msg4ImageText(msg).addItem(new Data4Item(member.getName() + "信息读取成功", body.toString(), "", createUrl(msg, WEIXIN_SCANNING_RESULT) + "&orderId=" + order.getId())));
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
			session.callback(new Msg4ImageText(msg).addItem(new Data4Item("操作失败", e.getMessage(), "", "")));
		}
	}
	
	private void processQrCode(Msg4Event msg) throws SQLException, BusinessException{
		QrCodeParam qrParam;
		if(msg.getEvent() == Event.SUBSCRIBE){
			qrParam = QrCodeParam.parse(msg.getEventKey().replace("qrscene_", ""));
		}else{
			qrParam = QrCodeParam.parse(msg.getEventKey());
		}
		if(qrParam.type == QrCodeType.WAITER){
			//扫描带参二维码,进入微信店小二
			final int groupId = WxRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName());
			final int orderId = Integer.parseInt(msg.getEventKey().substring(1));
			final int branchId = getRestaurantByOrder(orderId);
			final Staff staff = StaffDao.getAdminByRestaurant(branchId);
			WxRestaurant wxRestaurant = WxRestaurantDao.get(StaffDao.getAdminByRestaurant(groupId));
			final String picUrl;
			if(wxRestaurant.hasWeixinLogo()){
				picUrl = wxRestaurant.getWeixinLogo().getObjectUrl();
			}else{
				picUrl = "";
			}
			
			HttpSession httpSession = request.getSession(true);
			httpSession.setAttribute("fid", msg.getToUserName());
			httpSession.setAttribute("oid", msg.getFromUserName());
			final Order order = OrderDao.getById(staff, orderId, DateType.TODAY);
			httpSession.setAttribute("branchId", Integer.toString(branchId));
			httpSession.setMaxInactiveInterval(3600 * 2);	//2 hour
			final StringBuilder desc = new StringBuilder().append("账单号：" + order.getId()).append("\r\n")
														  .append("餐台：" + order.getDestTbl().getName()).append("\r\n")
														  .append("开台时间：" + DateUtil.format(order.getBirthDate())).append("\r\n")
														  .append("服务员 ：" + order.getWaiter()).append("\r\n")
														  .append("点击去微信店小二，可自助浏览菜品信息，呼叫服务，自助下单");
			session.callback(new Msg4ImageText(msg).addItem(
					new Data4Item("微信店小二(" + RestaurantDao.getById(branchId).getName() + ")", desc.toString(), picUrl,
								  createUrl4Session(WEIXIN_WAITER + "?orderId=" + orderId + "&branchId=" + branchId, httpSession))));
			
		}else if(qrParam.type == QrCodeType.REPRESENT){
			//扫描【我要代言】的带参二维码，生成推荐关系链
			final int rid = WxRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName());
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			final Member referrer = MemberDao.getById(staff, Integer.parseInt(msg.getEventKey().substring(1)));
			final Member subscriber = MemberDao.getByWxSerial(staff, msg.getFromUserName());
			final Represent represent = RepresentDao.getByCond(staff, null).get(0);
			
			MemberDao.chain(staff, new Member.ChainBuilder(subscriber, referrer));

			final WxRestaurant wxRestaurant = WxRestaurantDao.get(staff);
			final String picUrl;
			if(wxRestaurant.hasWeixinLogo()){
				picUrl = wxRestaurant.getWeixinLogo().getObjectUrl();
			}else{
				picUrl = "";
			}
			String desc = ("通过$(referrer)的推荐，您已成为$(restaurant)的会员，并获得$(recommend_money)的充值赠额和$(recommend_point)的赠送积分。" +
						  "\r\n" +
					      "点击去会员中心查看详情>>>>")
					      .replace("$(referrer)", referrer.getName())
					      .replace("$(restaurant)", RestaurantDao.getById(rid).getName())
					      .replace("$(recommend_money)", Float.toString(represent.getRecommentMoney()))
					      .replace("$(recommend_point)", Integer.toString(represent.getRecommendPoint()));
			session.callback(new Msg4ImageText(msg).addItem(new Data4Item("关注有礼", desc, picUrl, createUrl(msg, WEIXIN_MEMBER))));
		}
	}
	
	/**
	 *获取大众点评数据
	 */
	private String HttpRequest(String requestUrl) {
        StringBuffer sb = new StringBuffer();
        InputStream ips = getInputStream(requestUrl);
        InputStreamReader isreader = null;
        try {
            isreader = new InputStreamReader(ips, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = new BufferedReader(isreader);
        String temp = null;
        try {
            while ((temp = bufferedReader.readLine()) != null) {
                sb.append(temp);
            }
            bufferedReader.close();
            isreader.close();
            ips.close();
            ips = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
	
    private InputStream getInputStream(String requestUrl) {
        URL url = null;
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            url = new URL(requestUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            conn.connect();
 
            in = conn.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return in;
    }	
    
    private boolean hasDianping(int businessId){
    	String appkey = "6373481645";  
    	String secret = "21dcd218a828460bbea7d1977d7140a8";  
//    	String apiUrl = "http://api.dianping.com/v1/deal/get_deals_by_business_id";  
    	  
    	// 创建参数表  
    	Map<String, String> paramMap = new HashMap<String, String>();  
    	paramMap.put("city", "广州");  
    	paramMap.put("business_id",businessId+"");
    	  
    	// 对参数名进行字典排序  
    	String[] keyArray = paramMap.keySet().toArray(new String[0]);  
    	Arrays.sort(keyArray);  
    	  
    	// 拼接有序的参数名-值串  
    	StringBuilder stringBuilder = new StringBuilder();  
    	stringBuilder.append(appkey);  
    	for (String key : keyArray)  
    	{  
    	    stringBuilder.append(key).append(paramMap.get(key));  
    	}  
    	  
    	stringBuilder.append(secret);  
    	String codes = stringBuilder.toString();  
    	String sign = org.apache.commons.codec.digest.DigestUtils.shaHex(codes).toUpperCase();    	
    	
		String data = HttpRequest("http://api.dianping.com/v1/deal/get_deals_by_business_id?appkey=6373481645&sign="+sign+"&business_id=" + businessId +"&city=%E5%B9%BF%E5%B7%9E");
		
		if(JSON.parseObject(data).getString("status").equals("OK") && !JSON.parseObject(data).getString("count").equals("0")){
			return true;
		}else{
			return false;
		}    	
    }

    private int getRestaurantByOrder(int orderId) throws SQLException{
    	DBCon dbCon = new DBCon();
    	try{
    		dbCon.connect();
    		String sql = " SELECT restaurant_id FROM " + Params.dbName + ".order WHERE id = " + orderId;
    		dbCon.rs = dbCon.stmt.executeQuery(sql);
    		int restaurantId = 0;
    		if(dbCon.rs.next()){
    			restaurantId = dbCon.rs.getInt("restaurant_id");
    		}
    		dbCon.rs.close();
    		return restaurantId;
    	}finally{
    		dbCon.disconnect();
    	}
    }
    
    public static void main(String[] args) throws Exception {
        //1.jpg是你的 主图片的路径
        //InputStream is = new FileInputStream("1.jpg");

        //通过JPEG图象流创建JPEG数据流解码器
        //JPEGImageDecoder jpegDecoder = JPEGCodec.createJPEGDecoder(is);

        //解码当前JPEG数据流，返回BufferedImage对象
        //BufferedImage buffImg = jpegDecoder.decodeAsBufferedImage();
        BufferedImage buffImg = ImageIO.read(new File("d:\\waiterTimeoutPhoto.jpg"));
        //得到画笔对象
        Graphics g = buffImg.getGraphics();

        //创建你要附加的图象。

        //2.jpg是你的小图片的路径
        ImageIcon imgIcon = new ImageIcon("d:\\qrcode.jpg");

        //得到Image对象。
        Image img = imgIcon.getImage();

        //将小图片绘到大图片上。
        //5,300 .表示你的小图片在大图片上的位置。
        g.drawImage(img, 5, buffImg.getHeight() - imgIcon.getIconHeight(), null);

        //设置颜色。
        g.setColor(Color.BLACK);
       
        //最后一个参数用来设置字体的大小
        Font f = new Font("宋体",Font.BOLD,30);

        g.setFont(f);

        //10,20 表示这段文字在图片上的位置(x,y) .第一个是你设置的内容。
        //g.drawImage(ImageIO.read(new File("d:\\qrcode.jpg")), 10, 100, 100, 100, null);
        //g.drawString("默哀555555。。。。。。。",10,30);

        g.dispose();

        FileOutputStream os = new FileOutputStream("union.jpg");

        //创键编码器，用于编码内存中的图象数据。
        //JPEGImageEncoder en = JPEGCodec.createJPEGEncoder(os);
        //en.encode(buffImg);
        saveAsJPEG(100, buffImg, 1f, os);

        //is.close();
        os.close();
        
//        createImage("中华人民共和国",new Font("宋体",Font.BOLD,18),new File("d:/a.png"));
//        createImage("中华人民",new Font("黑体",Font.BOLD,30),new File("d:/a1.png"));
//        createImage("中华人民共和国",new Font("黑体",Font.PLAIN,24),new File("d:/a2.png"));
    }
    
    
    /** 
     * 以JPEG编码保存图片 
     * @param dpi  分辨率 
     * @param image_to_save  要处理的图像图片 
     * @param JPEGcompression  压缩比 
     * @param fos 文件输出流 
     * @throws IOException 
     */  
    public static void saveAsJPEG(Integer dpi ,BufferedImage image_to_save, float JPEGcompression, FileOutputStream fos) throws IOException {  
            
        //useful documentation at http://docs.oracle.com/javase/7/docs/api/javax/imageio/metadata/doc-files/jpeg_metadata.html  
        //useful example program at http://johnbokma.com/java/obtaining-image-metadata.html to output JPEG data  
        
        //old jpeg class  
        //com.sun.image.codec.jpeg.JPEGImageEncoder jpegEncoder  =  com.sun.image.codec.jpeg.JPEGCodec.createJPEGEncoder(fos);  
        //com.sun.image.codec.jpeg.JPEGEncodeParam jpegEncodeParam  =  jpegEncoder.getDefaultJPEGEncodeParam(image_to_save);  
        
        // Image writer  
//      JPEGImageWriter imageWriter = (JPEGImageWriter) ImageIO.getImageWritersBySuffix("jpeg").next();  
        ImageWriter imageWriter  =   ImageIO.getImageWritersBySuffix("jpg").next();  
        ImageOutputStream ios  =  ImageIO.createImageOutputStream(fos);  
        imageWriter.setOutput(ios);  
        //and metadata  
        IIOMetadata imageMetaData  =  imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(image_to_save), null);  
           
           
        if(dpi !=  null && !dpi.equals("")){  
               
             //old metadata  
            //jpegEncodeParam.setDensityUnit(com.sun.image.codec.jpeg.JPEGEncodeParam.DENSITY_UNIT_DOTS_INCH);  
            //jpegEncodeParam.setXDensity(dpi);  
            //jpegEncodeParam.setYDensity(dpi);  
        
            //new metadata  
            Element tree  =  (Element) imageMetaData.getAsTree("javax_imageio_jpeg_image_1.0");  
            Element jfif  =  (Element)tree.getElementsByTagName("app0JFIF").item(0);  
            jfif.setAttribute("Xdensity", Integer.toString(dpi) );  
            jfif.setAttribute("Ydensity", Integer.toString(dpi));  
               
        }  
        
        
        if(JPEGcompression >= 0 && JPEGcompression <= 1f){  
        
            //old compression  
            //jpegEncodeParam.setQuality(JPEGcompression,false);  
        
            // new Compression  
            JPEGImageWriteParam jpegParams  =  (JPEGImageWriteParam) imageWriter.getDefaultWriteParam();  
            jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);  
            jpegParams.setCompressionQuality(JPEGcompression);  
        
        }  
        
        //old write and clean  
        //jpegEncoder.encode(image_to_save, jpegEncodeParam);  
        
        //new Write and clean up  
        imageWriter.write(imageMetaData, new IIOImage(image_to_save, null, null), null);  
        ios.close();  
        imageWriter.dispose();  
        
    }  
	//根据str,font的样式以及输出文件目录
	public static void createImage(String str, Font font, File outFile) throws Exception{
		//获取font的样式应用在str上的整个矩形
		Rectangle2D r = font.getStringBounds(str, new FontRenderContext(AffineTransform.getScaleInstance(1, 1),false,false));
		int unitHeight = (int)Math.floor(r.getHeight());//获取单个字符的高度
		//获取整个str用了font样式的宽度这里用四舍五入后+1保证宽度绝对能容纳这个字符串作为图片的宽度
		int width = (int)Math.round(r.getWidth())+1;
		int height = unitHeight+3;//把单个字符的高度+3保证高度绝对能容纳字符串作为图片的高度
		//创建图片
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_BGR);
		Graphics g=image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);//先用白色填充整张图片,也就是背景
		g.setColor(Color.black);//在换成黑色
		g.setFont(font);//设置画笔字体
		g.drawString(str, 0, font.getSize());//画出字符串
		g.dispose();
		ImageIO.write(image, "png", outFile);//输出png图片
	}
    
}

package com.wireless.Actions.weixin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.marker.weixin.HandleMessageAdapter;
import org.marker.weixin.msg.Data4Item;
import org.marker.weixin.msg.Msg;
import org.marker.weixin.msg.Msg4Event;
import org.marker.weixin.msg.Msg4Event.Event;
import org.marker.weixin.msg.Msg4Head.MsgType;
import org.marker.weixin.msg.Msg4ImageText;
import org.marker.weixin.msg.Msg4Text;
import org.marker.weixin.session.WxSession;

import com.alibaba.fastjson.JSON;
import com.wireless.db.member.MemberDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.promotion.PromotionDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WxMemberDao;
import com.wireless.db.weixin.menuAction.WxMenuAction;
import com.wireless.db.weixin.menuAction.WxMenuActionDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.pojo.weixin.order.WxOrder;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;

public class WxHandleMessage extends HandleMessageAdapter {
	
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
	private final String wEIXIN_SCANNING_RESULT;
	
	private final String WEIXIN_FOOD_ICON;
	private final String WEIXIN_BOOK_ICON;
	private final String WEIXIN_RFOOD_ICON;
	private final String WEIXIN_ABOUT_ICON;
	private final String WEIXIN_DIANPING_ICON;
	private final String WEIXIN_DEFAULT_LOGO;
	
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
		SCAN_EVENT_KEY("scan_event_key", "扫一扫");
		
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
	
	public WxHandleMessage(WxSession session, String root){
		super(session);
		this.WEIXIN_INDEX = root + "/weixin/order/index.html";
		this.WEIXIN_FOOD = root + "/weixin/order/food.html";
		this.WEIXIN_BOOK = root + "/weixin/order/book.html";
		this.WEIXIN_RFOOD = root + "/weixin/order/rfood.html";
		this.WEIXIN_ABOUT = root + "/weixin/order/about.html";
		this.WEIXIN_MEMBER = root + "/weixin/order/member.html";
		this.WEIXIN_COUPON = root + "/weixin/order/sales.html";
		this.WEIXIN_ORDER = root + "/weixin/order/orderList.html";
		this.WEIXIN_DIANPING = root + "/weixin/order/dianping.html";
		this.WEIXIN_SCANNING = root + "/weixin/order/scan.html";
		this.wEIXIN_SCANNING_RESULT = root + "/weixin/order/scanResult.html";
		
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
				.append("&time=" + System.currentTimeMillis())
				.toString();
		
	}
	
//	private Msg createWelcome(Msg msg) throws SQLException, BusinessException{
//		int restaurantId = WxRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName());
//		List<Promotion> welcome = PromotionDao.getByCond(StaffDao.getAdminByRestaurant(restaurantId), 
//											   new PromotionDao.ExtraCond().setType(Promotion.Type.WELCOME).addStatus(Promotion.Status.PROGRESS));
//		
//		//检查是否有欢迎活动，没有则显示导航页，有则显示欢迎活动
//		if(welcome.isEmpty()){
//			throw new BusinessException("没有创建欢迎活动", PromotionError.PROMOTION_NOT_EXIST);
//		}else{
//			Promotion promotion = welcome.get(0);
//			StringBuilder desc = new StringBuilder();
//			//活动时间
//			desc.append("活动时间：" + promotion.getDateRange().getOpeningFormat() + " 至 " + promotion.getDateRange().getEndingFormat()).append("\n");
//			//活动规则
//			final String rule;
//			if(promotion.getRule() == Promotion.Rule.ONCE){
//				rule = "活动期内单次消费积分满" + promotion.getPoint() + "即可领取【" + promotion.getCouponType().getName() + "】";
//			}else if(promotion.getRule() == Promotion.Rule.TOTAL){
//				rule = "活动期内累计消费积分满" + promotion.getPoint() + "即可领取【" + promotion.getCouponType().getName() + "】";
//			}else if(promotion.getRule() == Promotion.Rule.FREE){
//				rule = "活动期内免费领取【" + promotion.getCouponType().getName() + "】";
//			}else{
//				rule = "";
//			}
//			desc.append("\n亲。。。在活动期间内激活会员账号即可参与【" + promotion.getTitle() + "】活动" + (!rule.isEmpty() ? "，" : "") + rule).append("\n");
//			Staff staff = StaffDao.getAdminByRestaurant(restaurantId);
//			List<Coupon> coupons = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setMember(MemberDao.getByWxSerial(staff, msg.getFromUserName())).setPromotionType(Promotion.Type.WELCOME), null);			
//			return new Msg4ImageText(msg).addItem(new Data4Item(promotion.getTitle() + "(火热进行中...)", desc.toString(), 
//					 					   								  promotion.hasImage() ? promotion.getImage().getObjectUrl() : "", 
//					 					   								  createUrl(msg, WEIXIN_COUPON) + "&cid=" + coupons.get(0).getId()));
//		}
//	}
	
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
		item4Book.setTitle("预订");
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
	 * 文本信息, 操作请求指令集
	 */
	@Override
	public void onTextMsg(Msg4Text msg) {
		// 绑定餐厅和公众平台信息
		//WxRestaurantDao.bind(msg.getToUserName(), RestaurantDao.getById(WxRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName())).getAccount());
		if(msg.getContent().equalsIgnoreCase("M")){
			session.callback(createNavi(msg));
		}else{
			session.callback(new Msg4Text(msg, "回复【m】获取餐厅导航"));
		}
	}
	
	/**
	 * 推送事件, 关注或取消关注使用
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
				final List<WxMenuAction> reply = WxMenuActionDao.getByCond(staff, new WxMenuActionDao.ExtraCond().setCate(WxMenuAction.Cate.SUBSCRIBE_REPLY));
				if(reply.isEmpty()){
					session.callback(createNavi(msg));
				}else{
					session.callback(appendUrlParam(msg, new WxMenuAction.MsgProxy(msg.getHead(), reply.get(0)).toMsg()));
				}
				
			}else if(msg.getEvent() == Event.UNSUBSCRIBE){
				//会员取消关注
				//WeixinMemberDao.cancel(msg.getFromUserName(), msg.getToUserName());
				
			}else if(msg.getEvent() == Event.CLICK){

				if(msg.getEventKey().equals(EventKey.NAVI_EVENT_KEY.val)){
					//餐厅导航
					session.callback(createNavi(msg));
					
				}else if(msg.getEventKey().equals(EventKey.SELF_BOOK_EVENT_KEY.val)){
					//自助预订
					session.callback(new Msg4ImageText(msg).addItem(new Data4Item("自助预订", "点击去预订", "", createUrl(msg, WEIXIN_BOOK))));
					
				}else if(msg.getEventKey().equals(EventKey.SELF_ORDER_EVENT_KEY.val)){
					//自助点餐
					session.callback(new Msg4ImageText(msg).addItem(new Data4Item("自助点餐", "点击去自助点餐", "", createUrl(msg, WEIXIN_FOOD))));
					
				}else if(msg.getEventKey().equals(EventKey.INTRO_EVENT_KEY.val)){
					//餐厅简介
					session.callback(new Msg4ImageText(msg).addItem(new Data4Item("餐厅简介", "点击去餐厅简介", "", createUrl(msg, WEIXIN_ABOUT))));
					
				}else if(msg.getEventKey().equals(EventKey.STAR_EVENT_KEY.val)){
					//明星菜品
					session.callback(new Msg4ImageText(msg).addItem(new Data4Item("明星菜品", "点击去明星菜品", "", createUrl(msg, WEIXIN_RFOOD))));
					
				}else if(msg.getEventKey().equals(EventKey.MY_QRCODE_EVENT_KEY.val)){
					//我的二维码
					Member member = MemberDao.getByWxSerial(StaffDao.getAdminByRestaurant(WxRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName())), msg.getFromUserName());
					final String qrCodeUrl = "http://qr.liantu.com/api.php?text=" + member.getWeixin().getCard();
					session.callback(new Msg4ImageText(msg).addItem(new Data4Item("我的二维码", "扫描二维码完成会员注入", qrCodeUrl + "&w=70", qrCodeUrl)));
					
				}else if(msg.getEventKey().equals(EventKey.PROMOTION_EVENT_KEY.val)){
					//最新优惠
					final int restaurantId = WxRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName());
					
					final Staff staff = StaffDao.getAdminByRestaurant(restaurantId);
					
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
								final String picUrl;
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
					//会员信息
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
						
						session.callback(new Msg4ImageText(msg).addItem(new Data4Item(title.toString(), "点击查看您的更多信息", "", createUrl(msg, WEIXIN_MEMBER))));
						
					}catch(BusinessException | SQLException e){
						//session.callback(new Msg4ImageText(msg).addItem(new Data4Item("亲。。。您的微信会员卡还未激活哦 :-(", "点击激活您的微信会员卡", "", createUrl(msg, WEIXIN_MEMBER))));
						session.callback(new Msg4ImageText(msg).addItem(new Data4Item(e.getMessage(), "", "", "")));
					}
						
				}else if(msg.getEventKey().equals(EventKey.ORDER_EVENT_KEY.val)){
					int restaurantId = WxRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName());
					
					Staff staff = StaffDao.getAdminByRestaurant(restaurantId);
					
					List<WxOrder> orders = WxOrderDao.getByCond(staff, new WxOrderDao.ExtraCond().setWeixin(msg.getFromUserName()).addStatus(WxOrder.Status.COMMITTED), " ORDER BY birth_date DESC");
					
					String title, description = "";
					
					if(!orders.isEmpty()){
						title = "您的最新订单号是: " + orders.get(0).getCode(); 
						description = "点击查看所有订单";
						session.callback(new Msg4ImageText(msg).addItem(new Data4Item(title, description, "", createUrl(msg, WEIXIN_ORDER))));
					}else{
						description = "点击去自助点餐";
						session.callback(new Msg4ImageText(msg).addItem(new Data4Item("暂无订单", description, "", createUrl(msg, WEIXIN_FOOD))));
					}
					
				}else if(msg.getEventKey().equals(EventKey.SCAN_EVENT_KEY.val)){
					session.callback(new Msg4ImageText(msg).addItem(new Data4Item("点击此处开始扫描", "点我扫描支付二维码", "", createUrl(msg, WEIXIN_SCANNING))));
					
				}else{
					int restaurantId = WxRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName());
					
					Staff staff = StaffDao.getAdminByRestaurant(restaurantId);
					Msg msg4Action = new WxMenuAction.MsgProxy(msg.getHead(), WxMenuActionDao.getById(staff, Integer.parseInt(msg.getEventKey()))).toMsg();
					session.callback(appendUrlParam(msg, msg4Action));
					
					//wecha_id={wechat_id}
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
					session.callback(new Msg4ImageText(msg).addItem(new Data4Item(member.getName() + "信息读取成功", body.toString(), "", createUrl(msg, wEIXIN_SCANNING_RESULT) + "&orderId=" + order.getId())));
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
			session.callback(new Msg4ImageText(msg).addItem(new Data4Item("操作失败", e.getMessage(), "", "")));
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
	
}

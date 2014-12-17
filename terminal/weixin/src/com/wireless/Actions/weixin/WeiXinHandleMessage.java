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

import org.marker.weixin.DefaultSession;
import org.marker.weixin.HandleMessageAdapter;
import org.marker.weixin.msg.Data4Item;
import org.marker.weixin.msg.Msg;
import org.marker.weixin.msg.Msg4Event;
import org.marker.weixin.msg.Msg4Event.Event;
import org.marker.weixin.msg.Msg4ImageText;
import org.marker.weixin.msg.Msg4Text;

import com.alibaba.fastjson.JSON;
import com.wireless.db.member.MemberDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.promotion.PromotionDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WxMemberDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PromotionError;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.pojo.weixin.order.WxOrder;
import com.wireless.pojo.weixin.restaurant.WeixinRestaurant;

public class WeiXinHandleMessage extends HandleMessageAdapter {
	
	private final String WEIXIN_INDEX;
	private final String WEIXIN_FOOD;
	private final String WEIXIN_RFOOD;
	private final String WEIXIN_ABOUT;
	private final String WEIXIN_MEMBER;
	private final String WEIXIN_COUPON;
	private final String WEIXIN_ORDER;
	private final String WEIXIN_DIANPING;
	
	private final String WEIXIN_FOOD_ICON;
	private final String WEIXIN_RFOOD_ICON;
	private final String WEIXIN_ABOUT_ICON;
	private final String WEIXIN_DIANPING_ICON;
	private final String WEIXIN_DEFAULT_LOGO;
	
	public final static String NAVI_EVENT_KEY = "navi_event_key";
	public final static String PROMOTION_EVENT_KEY = "promotion_event_key";
	public final static String MEMBER_EVENT_KEY = "member_event_key";
	public final static String ORDER_EVENT_KEY = "order_event_key";
	public final static String ZHUAN_EVENT_KEY = "zhuan_event_key";
	
	private final DefaultSession session;
	private final String account;
	
	public WeiXinHandleMessage(DefaultSession session, String account, String root){
		this.session = session;
		this.account = account;
		this.WEIXIN_INDEX = root + "/weixin/order/index.html";
		this.WEIXIN_FOOD = root + "/weixin/order/food.html";
		this.WEIXIN_RFOOD = root + "/weixin/order/rfood.html";
		this.WEIXIN_ABOUT = root + "/weixin/order/about.html";
		this.WEIXIN_MEMBER = root + "/weixin/order/member.html";
		this.WEIXIN_COUPON = root + "/weixin/order/sales.html";
		this.WEIXIN_ORDER = root + "/weixin/order/orderList.html";
		this.WEIXIN_DIANPING = root + "/weixin/order/dianping.html";
		
		this.WEIXIN_FOOD_ICON = root + "/weixin/order/images/icon_food.png";
		this.WEIXIN_RFOOD_ICON = root + "/weixin/order/images/icon_rfood.png";
		this.WEIXIN_ABOUT_ICON = root + "/weixin/order/images/icon_about.png";
		this.WEIXIN_DIANPING_ICON = root + "/weixin/order/images/dianping1.png";
		this.WEIXIN_DEFAULT_LOGO = "http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/WxLogo/default.jpg";
	}
	
	private String createUrl(Msg msg, String url){
		return new StringBuilder()
					.append(url)
					.append("?_d=" + System.currentTimeMillis())
					.append("&m=").append(msg.getFromUserName())
					.append("&r=").append(msg.getToUserName()).toString();
	}
	
	private Msg createWelcome(Msg msg) throws SQLException, BusinessException{
		int restaurantId = WeixinRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName());
		List<Promotion> welcome = PromotionDao.getByCond(StaffDao.getAdminByRestaurant(restaurantId), 
											   new PromotionDao.ExtraCond().setType(Promotion.Type.WELCOME).addStatus(Promotion.Status.PROGRESS));
		
		//检查是否有欢迎活动，没有则显示导航页，有则显示欢迎活动
		if(welcome.isEmpty()){
			throw new BusinessException("没有创建欢迎活动", PromotionError.PROMOTION_NOT_EXIST);
		}else{
			Promotion promotion = welcome.get(0);
			StringBuilder desc = new StringBuilder();
			//活动时间
			desc.append("活动时间：" + promotion.getDateRange().getOpeningFormat() + " 至 " + promotion.getDateRange().getEndingFormat()).append("\n");
			//活动规则
			final String rule;
			if(promotion.getRule() == Promotion.Rule.ONCE){
				rule = "活动期内单次消费积分满" + promotion.getPoint() + "即可领取【" + promotion.getCouponType().getName() + "】";
			}else if(promotion.getRule() == Promotion.Rule.TOTAL){
				rule = "活动期内累计消费积分满" + promotion.getPoint() + "即可领取【" + promotion.getCouponType().getName() + "】";
			}else if(promotion.getRule() == Promotion.Rule.FREE){
				rule = "活动期内免费领取【" + promotion.getCouponType().getName() + "】";
			}else{
				rule = "";
			}
			desc.append("\n亲。。。在活动期间内激活会员账号即可参与【" + promotion.getTitle() + "】活动" + (!rule.isEmpty() ? "，" : "") + rule).append("\n");
			Staff staff = StaffDao.getAdminByRestaurant(restaurantId);
			List<Coupon> coupons = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setMember(MemberDao.getByWxSerial(staff, msg.getFromUserName())).setPromotionType(Promotion.Type.WELCOME), null);			
			return new Msg4ImageText(msg).addItem(new Data4Item(promotion.getTitle() + "(火热进行中...)", desc.toString(), 
					 					   								  promotion.hasImage() ? promotion.getImage().getObjectUrl() : "", 
					 					   								  createUrl(msg, WEIXIN_COUPON) + "&e=" + coupons.get(0).getId()));
		}
	}
	
	private Msg createNavi(Msg msg){
		
		Msg4ImageText naviItem = new Msg4ImageText(msg);

		Data4Item mainItem;
		Restaurant restaurant = null;
		try {
			restaurant = RestaurantDao.getByAccount(account);
			WeixinRestaurant wr = WeixinRestaurantDao.get(StaffDao.getAdminByRestaurant(restaurant.getId()));
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
		item4Order.setUrl(createUrl(msg, WEIXIN_FOOD));
		item4Order.setPicUrl(WEIXIN_FOOD_ICON);
		naviItem.addItem(item4Order);
		
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
		try{
			// 绑定餐厅和公众平台信息
			WeixinRestaurantDao.bind(msg.getToUserName(), account);
			if(msg.getContent().equalsIgnoreCase("M")){
				session.callback(createNavi(msg));
			}else{
				session.callback(new Msg4Text(msg, "回复【m】获取餐厅导航"));
			}
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 推送事件, 关注或取消关注使用
	 */
	@Override
	public void onEventMsg(Msg4Event msg) {
		try{
			// 绑定餐厅和公众平台信息
			WeixinRestaurantDao.bind(msg.getToUserName(), account);
			
			if(msg.getEvent() == Event.SUBSCRIBE){
				//会员关注
				Staff staff = StaffDao.getAdminByRestaurant(WeixinRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName()));
				WxMemberDao.interest(staff, msg.getFromUserName());
				try{
					session.callback(createWelcome(msg));
				}catch(BusinessException e){
					if(e.getErrCode() == PromotionError.PROMOTION_NOT_EXIST){
						session.callback(createNavi(msg));
					}else{
						throw e;
					}
				}
				
			}else if(msg.getEvent() == Event.UNSUBSCRIBE){
				//会员取消关注
				//WeixinMemberDao.cancel(msg.getFromUserName(), msg.getToUserName());
				
			}else if(msg.getEvent() == Event.CLICK){

				if(msg.getEventKey().equals(NAVI_EVENT_KEY)){
					//餐厅导航
					session.callback(createNavi(msg));
					
				}else if(msg.getEventKey().equals(PROMOTION_EVENT_KEY)){
					//最新优惠
					int restaurantId = WeixinRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName());
					
					Staff staff = StaffDao.getAdminByRestaurant(restaurantId);
					
					List<Coupon> coupons = CouponDao.getByCond(staff, 
															   new CouponDao.ExtraCond().addPromotionStatus(Promotion.Status.PUBLISH)
															   							.addPromotionStatus(Promotion.Status.PROGRESS)
															   							.setPromotionType(Promotion.Type.NORMAL)
															   							.setMember(MemberDao.getByWxSerial(staff, msg.getFromUserName())), null);
					
					if(coupons.isEmpty()){
						session.callback(new Msg4ImageText(msg).addItem(new Data4Item("亲。。。暂时还没有优惠活动哦", "请留意我们的微信优惠通知哦", "", "")));
					}else{
						Msg4ImageText couponItem = new Msg4ImageText(msg);
						if(coupons.size() == 1){
							//只有一个优惠活动
							Coupon coupon = CouponDao.getById(staff, coupons.get(0).getId());
							StringBuilder desc = new StringBuilder();
							//活动时间
							desc.append("活动时间：" + coupon.getPromotion().getDateRange().getOpeningFormat() + " 至 " + coupon.getPromotion().getDateRange().getEndingFormat()).append("\n");
							//活动规则
							String rule;
							if(coupon.getPromotion().getRule() == Promotion.Rule.ONCE){
								rule = "活动期内单次消费积分满" + coupon.getPromotion().getPoint() + "即可领取【" + coupon.getName() + "】";
							}else if(coupon.getPromotion().getRule() == Promotion.Rule.TOTAL){
								rule = "活动期内累计消费积分满" + coupon.getPromotion().getPoint() + "即可领取【" + coupon.getName() + "】";
							}else if(coupon.getPromotion().getRule() == Promotion.Rule.FREE){
								rule = "活动期内免费领取【" + coupon.getName() + "】";
							}else{
								rule = "";
							}
							if(!rule.isEmpty()){
								desc.append("活动规则：" + rule).append("\n");
							}
							//温馨提示
							String tip = "";
							if(coupon.getDrawProgress().isOk()){
								tip = "亲。。。你已符合优惠券领取条件，马上点击领取【" + coupon.getName() + "】";
							}else{
								if(coupon.getPromotion().getRule() == Promotion.Rule.ONCE && coupon.getDrawProgress().getPoint() != 0){
									tip = "亲。。。您最近最多的单次消费积分是" + coupon.getDrawProgress().getPoint() + "，要加油哦:-)";
								}else if(coupon.getPromotion().getRule() == Promotion.Rule.TOTAL && coupon.getDrawProgress().getPoint() != 0){
									tip = "亲。。。您最近累计消费积分是" + coupon.getDrawProgress().getPoint() + "，要加油哦:-)";
								}else{
									tip = "";
								}
							}
							if(!tip.isEmpty()){
								desc.append("温馨提示：" + tip).append("\n");
							}
							
							desc.append("\n点击查看优惠活动详情>>>>");
							
							final String progress;
							if(coupon.getPromotion().getStatus() == Promotion.Status.PROGRESS){
								progress = "(火热进行中...)";
							}else{
								progress = "(敬请期待...)";
							}
							couponItem.addItem(new Data4Item(coupon.getPromotion().getTitle() + progress, 
															 desc.toString(), 
															 coupon.getPromotion().getImage().getObjectUrl(), 
															 createUrl(msg, WEIXIN_COUPON) + "&e=" + coupon.getId()));
							
						}else{
							
							//多个优惠活动
							for(int i = 0; i < coupons.size(); i++){
								coupons.set(i, CouponDao.getById(staff, coupons.get(i).getId()));
							}
							
							Collections.sort(coupons, new Comparator<Coupon>(){

								@Override
								public int compare(Coupon c1, Coupon c2) {
									if(c1.getPromotion().getStatus() != c2.getPromotion().getStatus()){
										if(c1.getPromotion().getStatus() == Promotion.Status.PROGRESS){
											return -1;
										}else if(c2.getPromotion().getStatus() == Promotion.Status.PROGRESS){
											return -1;
										}else{
											return 0;
										}
									}else{
										return 0;
									}
								}
								
							});
							for(Coupon coupon : coupons){
								final String progress;
								if(coupon.getPromotion().getStatus() == Promotion.Status.PROGRESS){
									progress = "(火热进行中...)";
								}else{
									progress = "(敬请期待...)";
								}
								final String picUrl;
								if(coupon.getPromotion().hasImage()){
									picUrl = coupon.getPromotion().getImage().getObjectUrl();
								}else{
									picUrl = "";
								}
								couponItem.addItem(new Data4Item(coupon.getPromotion().getTitle() + progress, "", 
												   picUrl, createUrl(msg, WEIXIN_COUPON) + "&e=" + coupon.getId()));
							}
						}
						session.callback(couponItem);
					}
					
				}else if(msg.getEventKey().equals(MEMBER_EVENT_KEY)){
					//会员信息
					try{
						Member member = MemberDao.getByWxSerial(StaffDao.getAdminByRestaurant(WeixinRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName())), msg.getFromUserName());
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
						
				}else if(msg.getEventKey().equals(ORDER_EVENT_KEY)){
					int restaurantId = WeixinRestaurantDao.getRestaurantIdByWeixin(msg.getToUserName());
					
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
					
				}else if(msg.getEventKey().equals(ZHUAN_EVENT_KEY)){
					session.callback(new Msg4ImageText(msg).addItem(new Data4Item("您有一次抽奖机会", "点击开始玩大转盘", "", "http://www.weixinrs.com/wx/xydzp0-5840.html?&wid=5165")));					
				}
			}
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
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

package com.wireless.Actions.weixin.finance;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.TextAnchor;
import org.marker.weixin.HandleMessageAdapter;
import org.marker.weixin.MySecurity;
import org.marker.weixin.api.Button;
import org.marker.weixin.api.Menu;
import org.marker.weixin.api.Token;
import org.marker.weixin.api.User;
import org.marker.weixin.msg.Data4Item;
import org.marker.weixin.msg.Msg;
import org.marker.weixin.msg.Msg4Event;
import org.marker.weixin.msg.Msg4Event.Event;
import org.marker.weixin.msg.Msg4ImageText;
import org.marker.weixin.msg.Msg4Text;
import org.marker.weixin.session.WxSession;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.billStatistics.CalcBillStatisticsDao;
import com.wireless.db.oss.OssImageDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.shift.ShiftDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.finance.WeixinFinanceDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.IncomeByEachDay;
import com.wireless.pojo.billStatistics.IncomeByPay.PaymentIncome;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.NumericUtil;

public class FinanceWeixinAction extends Action {
	
	public final static String APP_ID = "wx6fde9cd2c7fc791e";
	public final static String APP_SECRET = "0a360a43b80e3a334e5e52da706a3134";
	
	private final static int WEIXIN_CONTENT_LENGTH = 34;
	
	private final static String PROMPT_HELP_MSG = "用管理员账号登陆后台，按图片提示，扫描验证二维码绑定您的餐厅";
	private final static String PROMPT_HELP_PIC_URL = "http://wx.e-tones.net/web-term/images/interestHelp.jpg?" + System.currentTimeMillis();
	
	//TOKEN 是你在微信平台开发模式中设置的哦
	public static final String TOKEN = "xxx";

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if(request.getMethod().equalsIgnoreCase("get")){
			doGet(request, response);
		}else if(request.getMethod().equalsIgnoreCase("post")){
			doPost(request, response);
		}
		return null;
	}
	
	/**
	 * 处理微信服务器验证
	 */
	private void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String signature = request.getParameter("signature");	// 微信加密签名
		String timestamp = request.getParameter("timestamp");	// 时间戳
		String nonce = request.getParameter("nonce");			// 随机数
		String echostr = request.getParameter("echostr");		// 随机字符串

		// 重写toString方法，得到三个参数的拼接字符串
		List<String> list = new ArrayList<String>(3) {
			private static final long serialVersionUID = 2621444383666420433L;
			public String toString() {
				return this.get(0) + this.get(1) + this.get(2);
			}
		};
		list.add(TOKEN);
		list.add(timestamp);
		list.add(nonce);
		// 排序
		Collections.sort(list);
		// SHA-1加密
		String tmpStr = new MySecurity().encode(list.toString(), MySecurity.SHA_1);
		Writer out = response.getWriter();
		if (signature.equals(tmpStr)) {
			// 请求验证成功，返回随机码
			out.write(echostr);
		} else {
			out.write("");
		}
		out.flush();
		out.close();
	}
	
	/**
	 * 处理微信服务器发过来的各种消息，包括：文本、图片、地理位置、音乐等等
	 */
	private void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		InputStream is = request.getInputStream();
		OutputStream os = response.getOutputStream();
		
		WxSession session = WxSession.newInstance(); 
		
		try{
			session.addOnHandleMessageListener(new HandleMessageAdapter(session){
				
				@Override
				public void onEventMsg(final Msg4Event msg){
					if(msg.getEvent() == Event.SUBSCRIBE){
						if(msg.getTicket().isEmpty()){
							session.callback(new Msg4ImageText(msg).addItem(new Data4Item("欢迎关注志易云服务", PROMPT_HELP_MSG, PROMPT_HELP_PIC_URL, "")));
						}else{
							session.callback(doBinding(msg, msg.getEventKey().replace("qrscene_", "")));
						}
						
					}else if(msg.getEvent() == Event.UNSUBSCRIBE){
						try{
							WeixinFinanceDao.cancel(msg.getFromUserName());
						}catch(SQLException ignored){}
						
					}else if(msg.getEvent() == Event.CLICK){
						
						if(msg.getEventKey().equals(TODAY_EVENT_KEY)){
							//即时战报
							session.callback(doToday(msg));
							
						}else if(msg.getEventKey().equals(YESTERDAY_EVENT_KEY)){
							//最近日结
							session.callback(doYesterday(msg));
							
						}else if(msg.getEventKey().equals(THIS_WEEK_EVENT_KEY)){
							//本周周报
							session.callback(doThisWeek(msg));
							
						}else if(msg.getEventKey().equals(LAST_WEEK_EVENT_KEY)){
							//上周周报
							session.callback(doLastweek(msg));
							
						}else if(msg.getEventKey().equals(THIS_MONTH_EVENT_KEY)){
							//本月月报
							try {
								session.callback(Token.newInstance(APP_ID, APP_SECRET), new Callable<Msg>(){
									@Override
									public Msg call() throws Exception {
										return doThisMonth(msg);
									}
									
								});
							} catch (IOException e) {
								session.callback(new Msg4Text(msg, e.getMessage()));
							}
							
						}else if(msg.getEventKey().equals(LAST_MONTH_EVENT_KEY)){
							//上月月报
							try {
								session.callback(Token.newInstance(APP_ID, APP_SECRET), new Callable<Msg>(){
									@Override
									public Msg call() throws Exception {
										return doLastMonth(msg);
									}
									
								});
							} catch (IOException e) {
								session.callback(new Msg4Text(msg, e.getMessage()));
							}
						}
					}
				}
				
				@Override
				public void onTextMsg(Msg4Text msg) {
					
					String[] opeCodes = msg.getContent().split(" ");
					
					if(opeCodes[0].equals("bd")){
						//绑定微信号和餐厅
						session.callback(doBinding(msg, opeCodes[1], opeCodes[2]));
						
					}else if(opeCodes[0].equals("rj")){
						//查看最近日结信息
						session.callback(doYesterday(msg));
						
					}else if(opeCodes[0].equals("yy")){
						//查看今日关注
						session.callback(doThisWeek(msg));
						
					}else{
						//显示使用说明
						session.callback(doManual(msg));
					}
					
				}
				
				@Override
				public void onErrorMsg(int errorCode) {
				}
			});
			
			//处理微信消息 
			session.process(is, os);
			
		}finally{
			//如果不调用close方法，将会出现响应数据串到其它Servlet中。
			session.close();
		}
	}
	
	/**
	 * 绑定微信号和餐厅
	 * @param msg
	 * @param account
	 * @param pwd
	 * @return
	 */
	private Msg doBinding(Msg msg, String restaurantId){
		Msg4Text rmsg =	new Msg4Text(msg);

		try{
			Restaurant restaurant = WeixinFinanceDao.bind(msg.getFromUserName(), Integer.parseInt(restaurantId));
			Token token = Token.newInstance(APP_ID, APP_SECRET);
			User user = User.newInstance(token, msg.getFromUserName());
			user.remark(token, restaurant.getName());
			rmsg.setContent("亲爱的" + user.getNickName() + "，您已成功绑定【" + restaurant.getName() + "】\n");
			
		}catch(BusinessException | SQLException | NumberFormatException e){
			rmsg.setContent("对不起，您要绑定的餐厅不存在");
			
		}catch(IOException ignored){
			
		}
		
		return rmsg;
	}
	
	/**
	 * 绑定微信号和餐厅
	 * @param msg
	 * @param account
	 * @param pwd
	 * @return
	 */
	private Msg doBinding(Msg4Text msg, String account, String pwd){
		
		Msg4Text rmsg =	new Msg4Text(msg);

		try{
			WeixinFinanceDao.bind(msg.getFromUserName(), account, pwd);
			Restaurant restaurant = RestaurantDao.getByAccount(account);
			rmsg.setContent("恭喜您, 已成功绑定【" + restaurant.getName() + "】:-)");
			return rmsg;
			
		}catch(BusinessException e){
			rmsg.setContent("对不起，您输入的帐号或密码不匹配:-(");
			return rmsg;
			
		}catch(SQLException e){
			rmsg.setContent("对不起，绑定未成功:-(");
			return rmsg;
		}
	}
	
	private Msg doToday(Msg msg){
		
		try{
			int restaurantId = WeixinFinanceDao.getRestaurantIdByWeixin(msg.getFromUserName());
			return new Msg4Text(msg, (makeShiftContent("即时战报", RestaurantDao.getById(restaurantId), ShiftDao.getTodayDaily(StaffDao.getAdminByRestaurant(restaurantId)))));
			
		}catch(BusinessException e){
			return new Msg4ImageText(msg).addItem(new Data4Item(e.getMessage(), PROMPT_HELP_MSG, PROMPT_HELP_PIC_URL, ""));
			
		}catch(SQLException | ParseException e){
			return new Msg4Text(msg, "对不起，暂时查询不到即时战报的信息:-(");
		}
		
	}
	
	/**
	 * 查看最近日结信息
	 * @param msg
	 * @return
	 */
	private Msg doYesterday(Msg msg){
		
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			
			int restaurantId = WeixinFinanceDao.getRestaurantIdByWeixin(dbCon, msg.getFromUserName());
			
			String sql;
			sql = " SELECT on_duty, off_duty FROM " + Params.dbName + ".daily_settle_history WHERE " +
				  " restaurant_id = " + restaurantId +
				  " ORDER BY id DESC LIMIT 1 ";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			long onDuty, offDuty;
			if(dbCon.rs.next()){
				onDuty = dbCon.rs.getTimestamp("on_duty").getTime();
				offDuty = dbCon.rs.getTimestamp("off_duty").getTime();
			}else{
				throw new BusinessException("");
			}
			
			dbCon.rs.close();
			
			ShiftDetail detail = ShiftDao.getByRange(dbCon, StaffDao.getAdminByRestaurant(restaurantId), 
				    								 new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY).setDutyRange(new DutyRange(onDuty, offDuty)));

			return new Msg4Text(msg, makeShiftContent("最近日结", RestaurantDao.getById(restaurantId), detail));
			
		}catch(BusinessException e){
			return new Msg4ImageText(msg).addItem(new Data4Item(e.getMessage(), PROMPT_HELP_MSG, PROMPT_HELP_PIC_URL, ""));
			
		}catch(SQLException | ParseException e){
			return new Msg4Text(msg, "对不起，暂时查询不到最近日结的信息:-(");
			
		}finally{
			if(dbCon != null){
				dbCon.disconnect();
			}
		}
		
	}
	
	private String makeShiftContent(String title, Restaurant restaurant, ShiftDetail detail) throws ParseException{
		StringBuilder content = new StringBuilder();
		content.append(centerAligned(title) + "\n");
		content.append("-------------------\n");
		content.append("餐厅:" + restaurant.getName() + "\n");
		content.append("开始时间:" + new SimpleDateFormat("M月d日 HH:mm").format(DateUtil.parseDate(detail.getOnDuty())) + "\n");
		content.append("结束时间:" + new SimpleDateFormat("M月d日 HH:mm").format(DateUtil.parseDate(detail.getOffDuty())) + "\n");
		
		content.append("-------------------\n")
			   .append(grid3Item(new String[]{"收款", "账单数", "实收"}, new int[]{10, 20}) + "\n");
		
		for(PaymentIncome income : detail.getIncomeByPay().getPaymentIncomes()){
			if(income.getActual() > 0){
				content.append(grid3Item(new String[]{income.getPayType().getName(), Integer.toString(income.getAmount()), NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(income.getActual())}, new int[]{10, 16}) + "\n");
			}
		}
		
		content.append("-------------------\n").append(grid2Item("部门", "销售额", 18)).append("\n");
		for(IncomeByDept incomeByDept : detail.getDeptIncome()){
			content.append(grid2Item(incomeByDept.getDept().getName(), NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(incomeByDept.getIncome()), 16)).append("\n");
		}

		StringBuilder operationContent = new StringBuilder();
		if(detail.getEraseAmount() != 0 || detail.getEraseIncome() != 0){
			operationContent.append(grid3Item(new String[]{"抹数", Integer.toString(detail.getEraseAmount()), NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(detail.getEraseIncome())}, new int[]{10, 16}) + "\n");
		}
		if(detail.getDiscountAmount() != 0 || detail.getDiscountIncome() != 0){
			operationContent.append(grid3Item(new String[]{"折扣", Integer.toString(detail.getDiscountAmount()), NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(detail.getDiscountIncome())}, new int[]{10, 16}) + "\n");
		}
		if(detail.getGiftAmount() != 0 || detail.getGiftIncome() != 0){
			operationContent.append(grid3Item(new String[]{"赠送", Integer.toString(detail.getGiftAmount()), NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(detail.getGiftIncome())}, new int[]{10, 16}) + "\n");
		}
		if(detail.getCancelAmount() != 0 || detail.getCancelIncome() != 0){
			operationContent.append(grid3Item(new String[]{"退菜", Integer.toString(detail.getCancelAmount()), NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(detail.getCancelIncome())}, new int[]{10, 16}) + "\n");
		}
		if(detail.getPaidAmount() != 0 || detail.getPaidIncome() != 0){
			operationContent.append(grid3Item(new String[]{"反结账", Integer.toString(detail.getPaidAmount()), NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(detail.getPaidIncome())}, new int[]{10, 16}) + "\n");
		}
		if(detail.getServiceAmount() != 0 || detail.getServiceIncome() != 0){
			operationContent.append(grid3Item(new String[]{"服务费", Integer.toString(detail.getServiceAmount()), NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(detail.getServiceIncome())}, new int[]{10, 16}) + "\n");
		}
		if(detail.getCouponAmount() != 0 || detail.getCouponIncome() != 0){
			operationContent.append(grid3Item(new String[]{"优惠券", Integer.toString(detail.getCouponAmount()), NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(detail.getCouponIncome())}, new int[]{10, 16}) + "\n");
		}
		
		if(operationContent.length() != 0){
			content.append("-------------------\n")
				   .append(grid3Item(new String[]{"操作", "账单数", "金额"}, new int[]{10, 20}) + "\n")
				   .append(operationContent);
		}
		
		content.append("-------------------\n")
			   .append("实收总额：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(detail.getTotalActual()));
		
		if(detail.getIncomeByCharge().getChargeAmount() > 0){
			content.append("\n")
			   .append("-------------------\n")
			   .append("充值次数：" + detail.getIncomeByCharge().getChargeAmount()).append("\n")
			   .append("充值实收：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(detail.getIncomeByCharge().getTotalActualCharge())).append("\n")
			   .append("充值实充：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(detail.getIncomeByCharge().getTotalAccountCharge()));
		}
		
		if(detail.getIncomeByCharge().getRefundAmount() > 0){
			content.append("\n")
			   .append("-------------------\n")
			   .append("取款次数：" + detail.getIncomeByCharge().getRefundAmount()).append("\n")
			   .append("取款实退：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(detail.getIncomeByCharge().getTotalActualRefund())).append("\n")
			   .append("充值实扣：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(detail.getIncomeByCharge().getTotalAccountRefund()));
		}
		
		return content.toString();
	}
	
	/**
	 * 生成本月月报
	 * @param msg
	 * @return
	 */
	private Msg doThisMonth(Msg msg){
		
		try{
			Calendar c = Calendar.getInstance();
			c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1);
			long beginDate = c.getTimeInMillis();
			
			c.setTimeInMillis(System.currentTimeMillis());
			long endDate = c.getTimeInMillis();
			
			int restaurantId = WeixinFinanceDao.getRestaurantIdByWeixin(msg.getFromUserName());
			
			ShiftDetail detail = ShiftDao.getByRange(StaffDao.getAdminByRestaurant(restaurantId), 
													 new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY)
													 		.setDutyRange(new DutyRange(DateUtil.format(beginDate, DateUtil.Pattern.DATE), DateUtil.format(endDate, DateUtil.Pattern.DATE))));
			
			return new Msg4Text(msg, makeShiftContent("本月月报", RestaurantDao.getById(restaurantId), detail));
			
		}catch(SQLException | BusinessException | ParseException e){
			return new Msg4Text(msg, "对不起，暂时查询不到本月周报的信息:-(");
			
		}
		
	}
	
	/**
	 * 生成上月月报
	 * @param msg
	 * @return
	 */
	private Msg doLastMonth(Msg msg){
		
		try{
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MONTH, -1);
			c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1);
			long beginDate = c.getTimeInMillis();
			
			c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.getActualMaximum(Calendar.DAY_OF_MONTH));
			long endDate = c.getTimeInMillis();
			
			int restaurantId = WeixinFinanceDao.getRestaurantIdByWeixin(msg.getFromUserName());
			
			ShiftDetail detail = ShiftDao.getByRange(StaffDao.getAdminByRestaurant(restaurantId), 
													 new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY)
													 		.setDutyRange(new DutyRange(DateUtil.format(beginDate, DateUtil.Pattern.DATE), DateUtil.format(endDate, DateUtil.Pattern.DATE))));
			
			return new Msg4Text(msg, makeShiftContent("上月月报", RestaurantDao.getById(restaurantId), detail));
			
		}catch(SQLException | BusinessException | ParseException e){
			return new Msg4Text(msg, "对不起，暂时查询不到上月周报的信息:-(");
			
		}
		
	}
	
	/**
	 * 显示使用说明
	 * @param msg
	 * @return
	 */
	private Msg doManual(Msg4Text msg){
		Msg4Text rmsg =	new Msg4Text(msg);
		StringBuilder content = new StringBuilder();
		content.append(centerAligned("使用说明") + "\n");
		content.append("--------1--------\n")
			   .append("描述：绑定餐厅\n")
			   .append("输入：bd  帐号名  密码\n");
		content.append("--------2--------\n")
			   .append("描述：查看最近日结\n")
			   .append("输入：rj\n");
		content.append("--------3--------\n")
		   	   .append("描述：查看本周营业信息\n")
		   	   .append("输入：yy\n");

		rmsg.setContent(content.toString());
		return rmsg;
	}
	
	/**
	 * 生成本周周报
	 * @param msg
	 * @return
	 */
	private Msg doThisWeek(Msg msg) {

		Msg4ImageText mit = new Msg4ImageText(msg);
		
		try{
			int restaurantId = WeixinFinanceDao.getRestaurantIdByWeixin(msg.getFromUserName());
			Staff staff = StaffDao.getAdminByRestaurant(restaurantId);
			Calendar c = Calendar.getInstance();
			c.setFirstDayOfWeek(Calendar.MONDAY);
			// first day of this week
			c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
			long beginDate = c.getTimeInMillis();
			
			c.setTimeInMillis(System.currentTimeMillis());
			// the day before now of this week
			if(c.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY){
				c.add(Calendar.DAY_OF_WEEK, -1);
			}
			long endDate = c.getTimeInMillis();
			
			List<IncomeByEachDay> incomes = CalcBillStatisticsDao.calcIncomeByEachDay(staff, 
														new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY).setDutyRange(
																new DutyRange(DateUtil.format(beginDate, DateUtil.Pattern.DATE),
																			  DateUtil.format(endDate, DateUtil.Pattern.DATE))));
			
			final String associatedSerial = "trend_chart_thisWeek_" + msg.getFromUserName();
			final String fileNameJpg = associatedSerial + ".jpg";
			
			ByteArrayOutputStream bosJpg = new ByteArrayOutputStream();
			ChartUtilities.writeChartAsJPEG(bosJpg, 1, createChart(msg, incomes), 360, 280, null);
			
			ByteArrayInputStream bisJpg = new ByteArrayInputStream(bosJpg.toByteArray());
			
			OssImageDao.delete(staff, new OssImageDao.ExtraCond().setAssociated(OssImage.Type.WX_FINANCE, associatedSerial));
			int	ossImageId = OssImageDao.insert(staff, new OssImage.InsertBuilder(OssImage.Type.WX_FINANCE, associatedSerial).setImgResource(fileNameJpg, bisJpg));
			
			String picUrl = OssImageDao.getById(staff, ossImageId).getObjectUrl() + "?" + System.currentTimeMillis();
			
			bosJpg.close();
	    	bisJpg.close();
			
			Data4Item d1 = new Data4Item("本周(" + new SimpleDateFormat("M月d日").format(beginDate) + " - " + new SimpleDateFormat("M月d日").format(endDate) + ")", 
										 "走势图", picUrl, "");
			
			Data4Item d2 = new Data4Item("最近一天营业额：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(incomes.get(incomes.size() - 1).getIncomeByPay().getTotalActual()), 
										 "", "http://wx.e-tones.net/web-term/images/recentIncome.png", ""); 

			
			float averageIncome = 0;
			for(IncomeByEachDay incomeEachDay : incomes){
				averageIncome += incomeEachDay.getIncomeByPay().getTotalActual();
			}
			averageIncome = averageIncome / incomes.size();
			Data4Item d3 = new Data4Item("本周平均营业额：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(averageIncome), 
					 					 "", "http://wx.e-tones.net/web-term/images/averageIncome.png", ""); 
			      
			mit.addItem(d1);
			mit.addItem(d2);
			mit.addItem(d3);

			return mit;
			
		} catch (Exception e) {
			return new Msg4ImageText(msg).addItem(new Data4Item(e.getMessage(), PROMPT_HELP_MSG, PROMPT_HELP_PIC_URL, ""));
		}
	}
	
	/**
	 * 生成上周周报
	 * @param msg
	 * @return
	 */
	private Msg doLastweek(Msg msg) {

		Msg4ImageText mit = new Msg4ImageText(msg);
		
		try{
			int restaurantId = WeixinFinanceDao.getRestaurantIdByWeixin(msg.getFromUserName());
			Staff staff = StaffDao.getAdminByRestaurant(restaurantId);
			
			Calendar c = Calendar.getInstance();
			c.setFirstDayOfWeek(Calendar.MONDAY);
		    // last week
	        c.add(Calendar.WEEK_OF_YEAR, -1);
	        // first day of last week
	        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
			long beginDate = c.getTimeInMillis();
			
			c.setTimeInMillis(System.currentTimeMillis());
	        // first day of this week
	        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
	        // last day of previous week
	        c.add(Calendar.DAY_OF_MONTH, -1);
			long endDate = c.getTimeInMillis();
			
			List<IncomeByEachDay> incomes = CalcBillStatisticsDao.calcIncomeByEachDay(staff, 
														new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY)
															.setDutyRange(new DutyRange(
																DateUtil.format(beginDate, DateUtil.Pattern.DATE),
																DateUtil.format(endDate, DateUtil.Pattern.DATE))));
			
			final String associatedSerial = "trend_chart_lastWeek_" + msg.getFromUserName();
			final String fileNameJpg = associatedSerial + ".jpg";
			
			ByteArrayOutputStream bosJpg = new ByteArrayOutputStream();
			ChartUtilities.writeChartAsJPEG(bosJpg, 1, createChart(msg, incomes), 360, 280, null);
			
			ByteArrayInputStream bisJpg = new ByteArrayInputStream(bosJpg.toByteArray());

			OssImageDao.delete(staff, new OssImageDao.ExtraCond().setAssociated(OssImage.Type.WX_FINANCE, associatedSerial));
			int	ossImageId = OssImageDao.insert(staff, new OssImage.InsertBuilder(OssImage.Type.WX_FINANCE, associatedSerial).setImgResource(fileNameJpg, bisJpg));
			
			String picUrl = OssImageDao.getById(staff, ossImageId).getObjectUrl() + "?" + System.currentTimeMillis();
			
			bosJpg.close();
	    	bisJpg.close();
			
			Data4Item d1 = new Data4Item("上周(" + new SimpleDateFormat("M月d日").format(beginDate) + " - " + new SimpleDateFormat("M月d日").format(endDate) + ")",
									     "走势图", picUrl, "");
			
			
			float averageIncome = 0;
			for(IncomeByEachDay incomeEachDay : incomes){
				averageIncome += incomeEachDay.getIncomeByPay().getTotalActual();
			}
			averageIncome = averageIncome / incomes.size();
			Data4Item d2 = new Data4Item("上周平均营业额：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(averageIncome), 
					 					 "", "http://wx.e-tones.net/web-term/images/averageIncome.png", ""); 
			      
			mit.addItem(d1);
			mit.addItem(d2);

			return mit;
			
		} catch (Exception e) {
			return new Msg4ImageText(msg).addItem(new Data4Item(e.getMessage(), PROMPT_HELP_MSG, PROMPT_HELP_PIC_URL, ""));
		}
	}
	
	/**
	 * 创建数据集合
	 * 
	 * @return CategoryDataset对象
	 * @throws SQLException 
	 * @throws BusinessException 
	 * @throws ParseException 
	 */
	public CategoryDataset createDataSet(Msg msg, List<IncomeByEachDay> incomes) throws SQLException, BusinessException, ParseException {
			
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -5);
		// 实例化DefaultCategoryDataset对象
		DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
		for(IncomeByEachDay incomeByEachDay : incomes){
			dataSet.addValue(incomeByEachDay.getTotalActual(),	//类别
							 "最近5天营业额",					//图例名称
							 new SimpleDateFormat("d号").format(DateUtil.parseDate(incomeByEachDay.getDate(), DateUtil.Pattern.DATE)));
		}
		
		return dataSet;
	}

	/**
	 * 生成制图对象
	 * 
	 * @return JFreeChart对象
	 * @throws ParseException 
	 * @throws BusinessException 
	 * @throws SQLException 
	 */
	public JFreeChart createChart(Msg msg, List<IncomeByEachDay> incomes) throws SQLException, BusinessException, ParseException {
		// 字体
		final Font PLOT_FONT = new Font("宋体", Font.BOLD, 15);
		
		JFreeChart chart = null;
		chart = ChartFactory.createLineChart("", // 图表标题
				"", // X轴标题
				"", // Y轴标题
				createDataSet(msg, incomes), // 绘图数据集
				PlotOrientation.VERTICAL, // 绘制方向
				true, // 是否显示图例
				true, // 是否采用标准生成器
				false // 是否生成超链接
				);

		// 设置标题字体
		chart.getTitle().setFont(new Font("隶书", Font.BOLD, 15));
		// 设置图例类别字体
		chart.getLegend().setItemFont(new Font("宋体", Font.BOLD, 15));
		// 设置背景色
		chart.setBackgroundPaint(new Color(255, 229, 204));
		// 获取绘图区对象
		CategoryPlot plot = chart.getCategoryPlot();
		// 设置横轴字体
		plot.getDomainAxis().setLabelFont(PLOT_FONT);
		// 设置坐标轴标尺值字体
		plot.getDomainAxis().setTickLabelFont(PLOT_FONT);
		// 设置纵轴字体
		plot.getRangeAxis().setLabelFont(PLOT_FONT);
		// 设置绘图区背景色
		plot.setBackgroundPaint(new Color(255, 255, 204));
		// 设置水平方向背景线颜色
		plot.setRangeGridlinePaint(Color.BLACK);
		// 设置是否显示水平方向背景线,默认值为true
		plot.setRangeGridlinesVisible(true);
		// 设置垂直方向背景线颜色
		plot.setDomainGridlinePaint(Color.BLACK);
		// 设置是否显示垂直方向背景线,默认值为false
		plot.setDomainGridlinesVisible(true);
		
		// 获取折线对象
		LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
		//设置曲线的描画颜色
		renderer.setSeriesPaint(0, new Color(51, 51, 255));
		//设置曲线是否显示数据点  
		renderer.setBaseShapesVisible(true);
		//设置数据点是否实心
		renderer.setBaseShapesFilled(true);
		//设置数据点的形状
		renderer.setSeriesShape(0, new Ellipse2D.Double(-3.0f, -3.0f, 6.0f, 6.0f));
		// set the stroke for this series...
		renderer.setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, null, 0.0f));
		//设置曲线是否显示数据点  
		renderer.setBaseItemLabelsVisible(true);   
		renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.TOP_LEFT)); 
		renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		renderer.setBaseItemLabelFont(new Font("Dialog", 1, 14)); 
		plot.setRenderer(renderer);
		
        // change the margin at the top of the range axis...
        final ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setUpperMargin(0.5);
		
		return chart;
	}
	
	private String centerAligned(String content){
		String var = "$(space_left)$(value)";
		try{
			/**
			 * Calculate the amount of spaces to left,
			 * and replace the $(space_left) with it.
			 */
			int leftSpaceAmt = (WEIXIN_CONTENT_LENGTH - content.getBytes("GBK").length) / 2;
			StringBuilder space = new StringBuilder();
			for(int i = 0; i < leftSpaceAmt; i++){
				space.append(" ");
			}
			var = var.replace("$(space_left)", space);
			
			//replace the $(title)
			var = var.replace("$(value)", content);
			
		}catch(UnsupportedEncodingException e){}
		
		return var;
	}
	
	@SuppressWarnings("unused")
	private String rightAligned(String content){
		String var = "$(space_left)$(value)";
		try{
			/**
			 * Calculate the amount of spaces to left,
			 * and replace the $(space_left) with it.
			 */
			int leftSpaceAmt = WEIXIN_CONTENT_LENGTH - content.getBytes("GBK").length;
			StringBuilder space = new StringBuilder();
			for(int i = 0; i < leftSpaceAmt; i++){
				space.append(" ");
			}
			var = var.replace("$(space_left)", space);
			
			//replace the $(title)
			var = var.replace("$(value)", content);

			//var = new String(var.getBytes("GBK"), "GBK");
						
		}catch(UnsupportedEncodingException e){}
		
		return var;
	}
	
	private String grid3Item(String[] items, int[] pos){
		try{
			StringBuilder space1 = new StringBuilder();
			int nSpace = pos[0] - items[0].getBytes("GBK").length;
			for(int i = 0; i < nSpace; i++){
				space1.append(" ");
			}
			
			StringBuilder space2 = new StringBuilder();
			nSpace = pos[1] - items[0].getBytes("GBK").length - space1.length() - items[1].getBytes("GBK").length;
			for(int i = 0; i < nSpace; i++){
				space2.append(" ");
			}
			
			return items[0] + space1 + items[1] + space2 + items[2];
			
		}catch(UnsupportedEncodingException e){
			return "Unsupported Encoding";
		}
	}
	
	private String grid2Item(String item1, String item2, int pos){
		try{
			
			int nSpace;
			if(pos < 0){
				nSpace = WEIXIN_CONTENT_LENGTH - 
						 item1.getBytes("GBK").length - 
						 item2.getBytes("GBK").length - 1;
			}else{
				nSpace = pos - item1.getBytes("GBK").length;
			}
			StringBuilder space = new StringBuilder();
			for(int i = 0; i < nSpace; i++){
				space.append(" ");
			}
			
			return item1 + space + item2;
			
		}catch(UnsupportedEncodingException e){
			return "Unsupported Encoding";
		}
	}
	
	private final static String TODAY_EVENT_KEY = "today";
	private final static String YESTERDAY_EVENT_KEY = "yesterday";
	private final static String THIS_WEEK_EVENT_KEY = "this_week";
	private final static String LAST_WEEK_EVENT_KEY = "last_week";
	private final static String THIS_MONTH_EVENT_KEY = "this_month";
	private final static String LAST_MONTH_EVENT_KEY = "last_month";
	
	public static void main(String[] args) throws IOException{
		Token token = Token.newInstance(APP_ID, APP_SECRET);
		
		System.out.println(Menu.delete(token));

		Menu menu = new Menu();
		
		Button b1 = new Button.ClickBuilder("日报", "AAA")
							.addChild(new Button.ClickBuilder("即时战报", TODAY_EVENT_KEY))
							.addChild(new Button.ClickBuilder("最近日结", YESTERDAY_EVENT_KEY))
							.build();
		menu.set1stButton(b1);
		
		Button b2 = new Button.ClickBuilder("周报", "BBB")
							.addChild(new Button.ClickBuilder("本周报表", THIS_WEEK_EVENT_KEY))
							.addChild(new Button.ClickBuilder("上周报表", LAST_WEEK_EVENT_KEY))
							.build();
		menu.set2ndButton(b2);

		Button b3 = new Button.ViewBuilder("营业报表", "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx6fde9cd2c7fc791e&redirect_uri=http://wx.e-tones.net/wx-term/weixin/order/linkToStatistics.html&response_type=code&scope=snsapi_base&state=1#wechat_redirect")
//							.addChild(new Button.ClickBuilder("本月报表", THIS_MONTH_EVENT_KEY))
//							.addChild(new Button.ClickBuilder("上月报表", LAST_MONTH_EVENT_KEY))
							.build();
		menu.set3rdButton(b3);
		
		System.out.println(menu.create(token));
	}
}

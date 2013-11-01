package com.wireless.servlets.weixin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileOutputStream;
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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.marker.weixin.DefaultSession;
import org.marker.weixin.HandleMessageAdapter;
import org.marker.weixin.MySecurity;
import org.marker.weixin.msg.Data4Item;
import org.marker.weixin.msg.Msg;
import org.marker.weixin.msg.Msg4Event;
import org.marker.weixin.msg.Msg4ImageText;
import org.marker.weixin.msg.Msg4Text;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.billStatistics.QueryIncomeStatisticsDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.shift.QueryShiftDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.finance.WeixinFinanceDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.IncomeByEachDay;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DateType;
import com.wireless.util.OSSUtil;

public class FinanceWeixinServlet extends HttpServlet {

	private final static int WEIXIN_CONTENT_LENGTH = 12;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String imgTmpPath;
	
	private String ossUrlDomain;
	
	//TOKEN 是你在微信平台开发模式中设置的哦
	public static final String TOKEN = "xxx";

	@Override
	public void init(ServletConfig servletConfig) throws ServletException{
		imgTmpPath = servletConfig.getInitParameter("image_tmp_path");
	}
	
	/**
	 * 处理微信服务器验证
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
	 * 
	 * 
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		InputStream is = request.getInputStream();
		OutputStream os = response.getOutputStream();
		ossUrlDomain = request.getSession().getServletContext().getInitParameter("oss_url_domain");
		
		final DefaultSession session = DefaultSession.newInstance(); 
		
		session.addOnHandleMessageListener(new HandleMessageAdapter(){
			
			@Override
			public void onEventMsg(Msg4Event msg){
				if(msg.getEvent().equals(Msg4Event.SUBSCRIBE)){
					Msg4Text rmsg =	new Msg4Text();
					rmsg.setFromUserName(msg.getToUserName());
					rmsg.setToUserName(msg.getFromUserName());
					rmsg.setContent("欢迎关注智易云平台:-)\n" + 
									"请输入下面命令绑定餐厅\n" +
									"bd 帐号 密码");
					session.callback(rmsg);
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
					session.callback(doCheckRecentDailySettlement(msg));
					
				}else if(opeCodes[0].equals("jr")){
					//查看今日关注
					session.callback(doFocusedStatistics(msg));
					
				}else{
					//显示使用说明
					session.callback(doManual(msg));
				}
				
				//回复一条消息
//				Data4Item d1 = new Data4Item("蘑菇建站系统", "测试描述", "http://cms.yl-blog.com/themes/blue/images/logo.png", "cms.yl-blog.com"); 
//				Data4Item d2 = new Data4Item("雨林博客", "测试描述", "http://www.yl-blog.com/template/ylblog/images/logo.png", "www.yl-blog.com"); 
//				      
//				Msg4ImageText mit = new Msg4ImageText();
//				mit.setFromUserName(msg.getToUserName());
//				mit.setToUserName(msg.getFromUserName()); 
//				mit.setCreateTime(msg.getCreateTime());
//				mit.addItem(d1);
//				mit.addItem(d2);
//				mit.setFuncFlag("0");  
//				session.callback(mit);
				
			}
		});
		
		//必须调用这两个方法
        //如果不调用close方法，将会出现响应数据串到其它Servlet中。
		session.process(is, os);//处理微信消息 
		session.close();//关闭Session
	}
	
	/**
	 * 绑定微信号和餐厅
	 * @param msg
	 * @param account
	 * @param pwd
	 * @return
	 */
	private Msg doBinding(Msg4Text msg, String account, String pwd){
		
		Msg4Text rmsg =	new Msg4Text();
		rmsg.setFromUserName(msg.getToUserName());
		rmsg.setToUserName(msg.getFromUserName());

		try{
			WeixinFinanceDao.bindRestaurant(msg.getFromUserName(), account, pwd);
			Restaurant restaurant = RestaurantDao.getByAccount(account);
			rmsg.setContent("恭喜您, 已成功绑定'" + restaurant.getName() + "':-)\n" +
						    "1 - 绑定餐厅\n" + "bd 帐号 密码\n" +
							"2 - 查看最近日结信息\n" + "rj\n" +
						    "3 - 查看今日关注信息\n" + "jr");
			return rmsg;
			
		}catch(BusinessException e){
			rmsg.setContent("对不起，您输入的帐号或密码不匹配:-(");
			return rmsg;
			
		}catch(SQLException e){
			rmsg.setContent("对不起，绑定未成功:-(");
			return rmsg;
		}
	}
	
	/**
	 * 查看最近日结信息
	 * @param msg
	 * @return
	 */
	private Msg doCheckRecentDailySettlement(Msg4Text msg){
		Msg4Text rmsg =	new Msg4Text();
		rmsg.setFromUserName(msg.getToUserName());
		rmsg.setToUserName(msg.getFromUserName());
		
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
			if(dbCon.rs.next()){
				long onDuty = dbCon.rs.getTimestamp("on_duty").getTime();
				long offDuty = dbCon.rs.getTimestamp("off_duty").getTime();
				
				dbCon.rs.close();
				
				ShiftDetail detail = QueryShiftDao.exec(dbCon, StaffDao.getStaffs(dbCon, restaurantId).get(0), 
													    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(onDuty), 
													    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(offDuty), 
														DateType.HISTORY);
				StringBuilder content = new StringBuilder();
				content.append(centerAligned("最近日结") + "\n");
				content.append("-----------------\n");
				content.append("餐厅:" + RestaurantDao.getById(dbCon, restaurantId).getName() + "\n");
				content.append("开始时间:" + new SimpleDateFormat("M月d日 HH:mm").format(onDuty) + "\n");
				content.append("结束时间:" + new SimpleDateFormat("M月d日 HH:mm").format(offDuty) + "\n");
				content.append("-----------------\n")
					   .append(grid3Item(new String[]{"收款", "账单数", "金额"}, new int[]{10, 20}) + "\n")
					   .append(grid3Item(new String[]{"现金", Integer.toString(detail.getCashAmount()), Float.toString(detail.getCashActualIncome())}, new int[]{10, 20}) + "\n")
					   .append(grid3Item(new String[]{"刷卡", Integer.toString(detail.getCreditCardAmount()), Float.toString(detail.getCreditActualIncome())}, new int[]{10, 20}) + "\n");
				content.append("-----------------\n")
					   .append(rightAligned("实收总额:" + Float.toString(detail.getTotalActual())) + "\n");
				
				rmsg.setContent(content.toString());
				
			}else{
				throw new BusinessException("");
			}

			
		}catch(SQLException e){
			rmsg.setContent("对不起，暂时查询不到最近日结的信息:-(");
			
		}catch (BusinessException e) {
			rmsg.setContent("对不起，暂时查询不到最近日结的信息:-(");
			
		}finally{
			if(dbCon != null){
				dbCon.disconnect();
			}
		}
		
		return rmsg;
	}
	
	/**
	 * 显示使用说明
	 * @param msg
	 * @return
	 */
	private Msg doManual(Msg4Text msg){
		Msg4Text rmsg =	new Msg4Text();
		rmsg.setFromUserName(msg.getToUserName());
		rmsg.setToUserName(msg.getFromUserName());
		StringBuilder content = new StringBuilder();
		content.append("       使用说明     \n");
		content.append("--------1--------\n")
			   .append("描述：绑定餐厅\n")
			   .append("输入：bd  帐号名  密码\n");
		content.append("--------2--------\n")
			   .append("描述：查看最近日结\n")
			   .append("输入：rj 或 日结\n");
		content.append("--------3--------\n")
		   	   .append("描述：查看今天关注\n")
		   	   .append("输入：jr 或 今日\n");

		rmsg.setContent(content.toString());
		return rmsg;
	}
	
	/**
	 * 生成今日关注信息
	 * @param msg
	 * @return
	 * @throws IOException 
	 */
	private Msg doFocusedStatistics(Msg4Text msg) {

		Msg4ImageText mit = new Msg4ImageText();
		
		FileOutputStream fosJpg = null;
		
		File fileJpg = null;
		
		try{
			final String fileNameJpg = "trend_chart_" + msg.getFromUserName() + ".jpg";
			
			fileJpg = new File(imgTmpPath + fileNameJpg);
			fosJpg = new FileOutputStream(fileJpg);
			ChartUtilities.writeChartAsJPEG(fosJpg, 1, createChart(msg), 320, 280, null);
			
			final String bucketName = "weixin-finance";
			
			OSSUtil.uploadFile(bucketName, fileNameJpg, fileJpg, null);
			
			Data4Item d1 = new Data4Item("最近5天营业额", "走势图", "http://" + bucketName + "." + ossUrlDomain + "/" + fileNameJpg + "?" + System.currentTimeMillis(), ""); 
			Data4Item d2 = new Data4Item("雨林博客", "测试描述", "http://www.yl-blog.com/template/ylblog/images/logo.png", "www.yl-blog.com"); 
			      
			mit.setFromUserName(msg.getToUserName());
			mit.setToUserName(msg.getFromUserName()); 
			mit.setCreateTime(msg.getCreateTime());
			mit.addItem(d1);
			mit.addItem(d2);

			return mit;
			
		} catch (Exception e) {
			Msg4Text rmsg =	new Msg4Text();
			rmsg.setFromUserName(msg.getToUserName());
			rmsg.setToUserName(msg.getFromUserName());
			//rmsg.setContent(e.getMessage());
			rmsg.setContent("对不起,暂时不能生成今日关注信息哦:-(");
			return rmsg;
			
		}finally{
			try {
				if(fosJpg != null){
					fosJpg.close();
				}
			} catch (IOException ignored) {	}
			
			if(fileJpg != null){
				fileJpg.delete();
			}
			
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
	public CategoryDataset createDataSet(Msg4Text msg) throws SQLException, BusinessException, ParseException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			int restaurantId = WeixinFinanceDao.getRestaurantIdByWeixin(dbCon, msg.getFromUserName());

			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, -5);
			//FIXME
			List<IncomeByEachDay> incomes = QueryIncomeStatisticsDao.getIncomeByEachDay(StaffDao.getStaffs(restaurantId).get(0), 
														//DateUtil.format(c.getTimeInMillis(), DateUtil.Pattern.DATE),
														//DateUtil.format(System.currentTimeMillis(), DateUtil.Pattern.DATE));
														"2013-08-1",
														"2013-08-5");
			// 实例化DefaultCategoryDataset对象
			DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
			for(IncomeByEachDay incomeByEachDay : incomes){
				dataSet.addValue(incomeByEachDay.getTotalActual(),	//类别
								 "最近5天营业额",					//图例名称
								 new SimpleDateFormat("d号").format(DateUtil.parseDate(incomeByEachDay.getDate(), DateUtil.Pattern.DATE)));
			}
			
			return dataSet;
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * 生成制图对象
	 * 
	 * @return JFreeChart对象
	 * @throws ParseException 
	 * @throws BusinessException 
	 * @throws SQLException 
	 */
	public JFreeChart createChart(Msg4Text msg) throws SQLException, BusinessException, ParseException {
		// 字体
		final Font PLOT_FONT = new Font("宋体", Font.BOLD, 15);
		
		JFreeChart chart = null;
		chart = ChartFactory.createLineChart("", // 图表标题
				"", // X轴标题
				"", // Y轴标题
				createDataSet(msg), // 绘图数据集
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
				space.append('\b');
			}
			var = var.replace("$(space_left)", space);
			
			//replace the $(title)
			var = var.replace("$(value)", content);
			
		}catch(UnsupportedEncodingException e){}
		
		return var;
	}
	
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
				space.append("\b");
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
}

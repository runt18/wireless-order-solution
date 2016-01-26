package com.wireless.Actions.restaurantMgr;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.token.TokenDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.restaurantMgr.Module.Code;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.restaurantMgr.Restaurant.InsertBuilder;
import com.wireless.pojo.restaurantMgr.Restaurant.RecordAlive;
import com.wireless.pojo.restaurantMgr.Restaurant.UpdateBuilder;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.token.Token;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;
import com.wireless.pojo.weixin.restaurant.WxRestaurant.QrCodeStatus;

public class OperateRestaurantAction extends DispatchAction {
	
	public ActionForward insert(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		JObject jobject = new JObject();
		String account = request.getParameter("account");
		String name = request.getParameter("name");
		String pwd = request.getParameter("pwd");
		String info = request.getParameter("info");
		String tele1 = request.getParameter("tele1");
		String tele2 = request.getParameter("tele2");
		String dianping = request.getParameter("dianping");
		String address = request.getParameter("address");
		String recordAlive = request.getParameter("recordAlive");
		String expireDate = request.getParameter("expireDate");
		String moduleCheckeds = request.getParameter("moduleCheckeds");
		//String activeness = request.getParameter("activeness");
		try{
			Restaurant.InsertBuilder builder = new InsertBuilder(account, name, DateUtil.parseDate(expireDate), pwd)
												.setAddress(address)
												.setRecordAlive(RecordAlive.valueOf(Integer.parseInt(recordAlive)))
												.setRestaurantInfo(info)
												.setTele1(tele1)
												.setTele2(tele2);
			
			if(dianping != null && !dianping.isEmpty()){
				builder.setDianpingId(Integer.parseInt(dianping));
			}
			
			if(moduleCheckeds != null && !moduleCheckeds.isEmpty() ){
				String[] modules = moduleCheckeds.split(",");
				for (String module : modules) {
					builder.addModule(Code.valueOf(Integer.parseInt(module)));
				}
			}
			
			RestaurantDao.insert(builder);
			jobject.initTip(true, "添加成功");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
		
	}
	
	public ActionForward update(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		final String id = request.getParameter("id");
		final String account = request.getParameter("account");
		final String name = request.getParameter("name");
		final String pwd = request.getParameter("pwd");
		final String info = request.getParameter("info");
		final String tele1 = request.getParameter("tele1");
		final String tele2 = request.getParameter("tele2");
		final String dianping = request.getParameter("dianping");
		final String address = request.getParameter("address");
		final String recordAlive = request.getParameter("recordAlive");
		final String expireDate = request.getParameter("expireDate");
		final String moduleCheckeds = request.getParameter("moduleCheckeds");
		final String branches = request.getParameter("branches");
		
		final JObject jObject = new JObject();
		try{
			final Restaurant.UpdateBuilder builder = new UpdateBuilder(Integer.parseInt(id));
			
			//账户
			if(account != null && !account.isEmpty()){
				builder.setAccount(account);
			}
			
			//餐厅名称
			if(name != null && !name.isEmpty()){
				builder.setRestaurantName(name);
			}
			
			//电话1
			if(tele1 != null && !tele1.isEmpty()){
				builder.setTele1(tele1);
			}
			
			//电话2
			if(tele2 != null && !tele2.isEmpty()){
				builder.setTele2(tele2);
			}
			
			//地址
			if(address != null && !address.isEmpty()){
				builder.setAddress(address);
			}
			
			//账单有效期
			if(recordAlive != null && !recordAlive.isEmpty()){
				builder.setRecordAlive(RecordAlive.valueOf(Integer.parseInt(recordAlive)));
			}
			
			//过期时间
			if(expireDate != null && !expireDate.isEmpty()){
				builder.setExpireDate(DateUtil.parseDate(expireDate));
			}
			
			//餐厅信息
			if(info != null && !info.isEmpty()){
				builder.setRestaurantInfo(info);
			}
			
			if(dianping != null && !dianping.isEmpty()){
				builder.setDianpingId(Integer.parseInt(dianping));
			}else{
				builder.setDianpingId(0);
			}
			
			//密码
			if(pwd != null && !pwd.isEmpty()){
				builder.setPwd(pwd);
			}
			
			//模块
			if(moduleCheckeds != null && !moduleCheckeds.isEmpty() ){
				for (String module : moduleCheckeds.split(",")) {
					builder.addModule(Code.valueOf(Integer.parseInt(module)));
				}
			}
			
			//连锁分店
			if(branches != null && !branches.isEmpty()){
				for(String branchId : branches.split(",")){
					builder.addBranch(Integer.parseInt(branchId));
				}
			}else{
				builder.clearBranch();
			}
			
			RestaurantDao.update(builder);
			
			jObject.initTip(true, "修改成功");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
		
	}

	/**
	 * 设置是否在账单打印二维码
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward updateRestaurantPrintCode(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		DBCon dbCon = new DBCon();
		PrintWriter out = response.getWriter();
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		String printCode = request.getParameter("printCode");
		try{
			WxRestaurant.UpdateBuilder builder = new WxRestaurant.UpdateBuilder().setQrCodeStatus(printCode.equals("true")?QrCodeStatus.NORMAL:QrCodeStatus.HIDDEN);
			WxRestaurantDao.update(StaffDao.verify(Integer.parseInt(pin)), builder);
		} catch(BusinessException e) {
			e.printStackTrace();
			jobject.initTip(e);
		} finally {
			dbCon.disconnect();
			out.print(jobject.toString());
		}
		return null;
	}	
	
	/**
	 * 设置餐厅地址和电话
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward systemUpdate(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		DBCon dbCon = new DBCon();
		PrintWriter out = response.getWriter();
		JObject jobject = new JObject();
		try{

			String restaurant_info = request.getParameter("restaurant_info");
			String address = request.getParameter("address");
			String tele1 = request.getParameter("tel1");
			String tele2 = request.getParameter("tel2");
			String id = (String) request.getAttribute("restaurantID");
			
			Restaurant restaurant = RestaurantDao.getById(Integer.parseInt(id));
			
			Restaurant.UpdateBuilder builder = new UpdateBuilder(Integer.parseInt(id))
														.setAccount(restaurant.getAccount())
														.setTele1(tele1)
														.setTele2(tele2)
														.setAddress(address)
														.setRestaurantInfo(restaurant_info)
														.setRecordAlive(Restaurant.RecordAlive.valueOfSeconds(restaurant.getRecordAlive()))
														.setRestaurantName(restaurant.getName());
			
			RestaurantDao.update(builder);
			
			jobject.initTip(true, "操作成功!");
		} catch(BusinessException e) {
			e.printStackTrace();
			jobject.initTip(e);
		} finally {
			dbCon.disconnect();
			out.print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 修改微信餐厅简介
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward updateInfo(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		try{
			String info = request.getParameter("info");
			int rid = Integer.parseInt(request.getAttribute("restaurantID").toString());
			
			if(!info.isEmpty()){
				WxRestaurantDao.update(StaffDao.getAdminByRestaurant(rid), new WxRestaurant.UpdateBuilder().setWeixinInfo(info));
			}

			jobject.initTip(true, "操作成功, 已修改微信餐厅简介信息.");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getInfo(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		try{
			final String info = WxRestaurantDao.get(StaffDao.getAdminByRestaurant(Integer.valueOf(request.getAttribute("restaurantID").toString()))).getWeixinInfo();
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("info", info);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getLogo(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		try{
			OssImage image = WxRestaurantDao.get(StaffDao.getAdminByRestaurant(Integer.valueOf(request.getAttribute("restaurantID").toString()))).getWeixinLogo();
			final String logo;
			if(image != null){
				logo = image.getObjectUrl();
			}else{
				logo = this.getServlet().getInitParameter("imageBrowseDefaultFile");
			}
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("logo", logo);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
			jobject.setSuccess(true);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 上传微信餐厅logo
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward updateLogo(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		String logo = request.getParameter("logo");
		String pin = (String) request.getAttribute("pin");
		JObject jobject = new JObject();
		try{
			WxRestaurant.UpdateBuilder builder = new WxRestaurant.UpdateBuilder();
			builder.setWeixinLogo(Integer.parseInt(logo));
			
			WxRestaurantDao.update(StaffDao.verify(Integer.parseInt(pin)), builder);
			
			jobject.initTip(true, "上传ＬＯＧＯ成功");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 生成动态码
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward tokenCode(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String resId = request.getParameter("resId");
		JObject jobject = new JObject();
		try{
			int restaurantId = Integer.parseInt(resId);
			int tokenId = TokenDao.insert(new Token.InsertBuilder(restaurantId));
			Token tempToken = TokenDao.getById(tokenId); 
			final int code = tempToken.getCode();	
			
			//为restaurant加上验证码个数
			int used = 0, unUsed = 0;
			List<Token> codes = new ArrayList<>();
			List<Token> tokens = TokenDao.getByCond(new TokenDao.ExtraCond().setRestaurant(restaurantId));
			for (Token token : tokens) {
				if(token.getStatus() == Token.Status.TOKEN){
					used ++;
				}else if(token.getStatus() == Token.Status.DYN_CODE && !token.isCodeExpired()){
					unUsed ++;
					codes.add(token);
				}
			}
			final int usedCode = used, unUsedCode = unUsed;
			
			jobject.setRoot(codes);
			jobject.setExtra(new Jsonable() {
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putInt("code", code);
					jm.putInt("usedCode", usedCode);
					jm.putInt("unUsedCode", unUsedCode);
					return jm;
				}
				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
			});
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 获取可用验证码
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getCodes(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String resId = request.getParameter("resId");
		JObject jobject = new JObject();
		try{
			int restaurantId = Integer.parseInt(resId);
			
			List<Token> codes = new ArrayList<>();
			for (Token token : TokenDao.getByCond(new TokenDao.ExtraCond().setRestaurant(restaurantId))) {
				if(token.getStatus() == Token.Status.DYN_CODE && !token.isCodeExpired()){
					codes.add(token);
				}
			}
			
			jobject.setRoot(codes);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 获取绑定后的微信餐厅信息
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward restInfo(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		String rid = request.getParameter("rid");
		try{
			Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(rid));
			WxRestaurant rest = WxRestaurantDao.get(staff);
			jobject.setRoot(rest);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}	
	
	
	
	
	
	
}

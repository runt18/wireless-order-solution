package com.wireless.Actions.client.member;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.member.MemberCondDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberCond;
import com.wireless.pojo.member.MemberCond.RangeType;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.staffMgr.Staff;

public class OperateMemberCondAction extends DispatchAction{
	/**
	 * 新增会员分析条件
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String name = request.getParameter("name");
		final String memberType = request.getParameter("memberType");
		final String memberCondMinConsume = request.getParameter("memberCondMinConsume");
		final String memberCondMaxConsume = request.getParameter("memberCondMaxConsume");
		final String memberCondMinAmount = request.getParameter("memberCondMinAmount");
		final String memberCondMaxAmount = request.getParameter("memberCondMaxAmount");
		final String memberCondMinBalance = request.getParameter("memberCondMinBalance");
		final String memberCondMaxBalance = request.getParameter("memberCondMaxBalance");
		final String memberCondDateRegion = request.getParameter("memberCondDateRegion");
		final String memberCondBeginDate = request.getParameter("memberCondBeginDate");
		final String memberCondEndDate = request.getParameter("memberCondEndDate");
		final String minLastConsumption = request.getParameter("minLastConsumption");
		final String maxLastConsumption = request.getParameter("maxLastConsumption");
		final String sex = request.getParameter("sex");
		final String ageVal = request.getParameter("age");
		final String isRaw = request.getParameter("isRaw");
		final String minCharge = request.getParameter("memberCondMinCharge");
		final String maxCharge = request.getParameter("memberCondMaxCharge");
		final JObject jObject = new JObject(); 
		
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			final MemberCond.InsertBuilder builder = new MemberCond.InsertBuilder(name);
			final RangeType rangeType;
			
			if(memberCondDateRegion != null && !memberCondDateRegion.isEmpty()){
				rangeType = RangeType.valueOf(Integer.parseInt(memberCondDateRegion));
			}else{
				rangeType = MemberCond.RangeType.LAST_1_MONTH;
			}
			
			//设置时间段
			builder.setRangeType(rangeType);
			if(rangeType == RangeType.USER_DEFINE){
				builder.setRange(memberCondBeginDate, memberCondEndDate);
			}
			//会员类型
			if(memberType != null && !memberType.isEmpty() && !memberType.equals("-1")){
				builder.setMemberType(new MemberType(Integer.parseInt(memberType)));
			}
			//消费金额
			if(memberCondMinConsume != null && !memberCondMinConsume.isEmpty() && memberCondMaxConsume != null && !memberCondMaxConsume.isEmpty()){
				builder.setConsumeMoney(Float.parseFloat(memberCondMinConsume), Float.parseFloat(memberCondMaxConsume));
			}
			//消费次数
			if(memberCondMinAmount != null && !memberCondMinAmount.isEmpty() && memberCondMaxAmount != null && !memberCondMaxAmount.isEmpty()){
				builder.setConsumeAmount(Integer.parseInt(memberCondMinAmount), Integer.parseInt(memberCondMaxAmount));
			}
			//余额
			if(memberCondMinBalance != null && !memberCondMinBalance.isEmpty() && memberCondMaxBalance != null && !memberCondMaxBalance.isEmpty()){
				builder.setBalance(Float.parseFloat(memberCondMinBalance), Float.parseFloat(memberCondMaxBalance));
			}
			
			//距离最近消费天数
			if(minLastConsumption != null && !minLastConsumption.isEmpty() && maxLastConsumption != null && !maxLastConsumption.isEmpty()){
				builder.setLastConsumption(Integer.parseInt(minLastConsumption), Integer.parseInt(maxLastConsumption));
			}
			
			//性别
			if(sex != null && !sex.isEmpty()){
				builder.setSex(Member.Sex.valueOf(Integer.parseInt(sex)));
			}
			
			//是否Raw
			if(isRaw != null && !isRaw.isEmpty()){
				builder.setRaw(Boolean.parseBoolean(isRaw));
			}
			
			//年龄段
			if(ageVal != null && !ageVal.isEmpty()){
				for(String age : ageVal.split(",")){
					builder.addAge(Member.Age.valueOf(Integer.parseInt(age)));
				}
			}
			
			//充值金额
			if(minCharge != null && !minCharge.isEmpty() && maxCharge != null && !maxCharge.isEmpty()){
				builder.setCharge(Float.parseFloat(minCharge), Float.parseFloat(maxCharge));
			}
			
			MemberCondDao.insert(staff, builder);
			
			jObject.initTip(true, "添加成功");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
	
	/**
	 * 修改会员分析条件
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String id = request.getParameter("id");
		final String name = request.getParameter("name");
		final String memberType = request.getParameter("memberType");
		final String memberCondMinConsume = request.getParameter("memberCondMinConsume");
		final String memberCondMaxConsume = request.getParameter("memberCondMaxConsume");
		final String memberCondMinAmount = request.getParameter("memberCondMinAmount");
		final String memberCondMaxAmount = request.getParameter("memberCondMaxAmount");
		final String memberCondMinBalance = request.getParameter("memberCondMinBalance");
		final String memberCondMaxBalance = request.getParameter("memberCondMaxBalance");
		final String memberCondDateRegion = request.getParameter("memberCondDateRegion");
		final String memberCondBeginDate = request.getParameter("memberCondBeginDate");
		final String memberCondEndDate = request.getParameter("memberCondEndDate");
		final String minLastConsumption = request.getParameter("minLastConsumption");
		final String maxLastConsumption = request.getParameter("maxLastConsumption");
		final String sex = request.getParameter("sex");
		final String ageVal = request.getParameter("age");
		final String isRaw = request.getParameter("isRaw");
		final String minCharge = request.getParameter("memberCondMinCharge");
		final String maxCharge = request.getParameter("memberCondMaxCharge");
		final JObject jObject = new JObject(); 
		
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			final MemberCond.UpdateBuilder builder = new MemberCond.UpdateBuilder(Integer.parseInt(id)).setName(name);
			
			RangeType rangeType = RangeType.valueOf(Integer.parseInt(memberCondDateRegion));
			
			//设置时间段
			builder.setRangeType(rangeType);
			if(rangeType == RangeType.USER_DEFINE){
				if(memberCondBeginDate != null && !memberCondBeginDate.isEmpty() && memberCondEndDate != null && !memberCondEndDate.isEmpty()){
					builder.setRange(memberCondBeginDate, memberCondEndDate);
				}
			}
			
			if(memberType != null && !memberType.isEmpty()){
				if(memberType.equals("-1")){
					builder.setMemberType(null);
				}else{
					builder.setMemberType(new MemberType(Integer.parseInt(memberType)));
				}
			}
			//消费金额
			if(memberCondMinConsume != null && !memberCondMinConsume.isEmpty() && memberCondMaxConsume != null && !memberCondMaxConsume.isEmpty()){
				builder.setConsumeMoney(Float.parseFloat(memberCondMinConsume), Float.parseFloat(memberCondMaxConsume));
			}
			//消费次数
			if(memberCondMinAmount != null && !memberCondMinAmount.isEmpty() && memberCondMaxAmount != null && !memberCondMaxAmount.isEmpty()){
				builder.setConsumeAmount(Integer.parseInt(memberCondMinAmount), Integer.parseInt(memberCondMaxAmount));
			}
			//余额
			if(memberCondMinBalance != null && !memberCondMinBalance.isEmpty() && memberCondMaxBalance != null && !memberCondMaxBalance.isEmpty()){
				builder.setBalance(Float.parseFloat(memberCondMinBalance), Float.parseFloat(memberCondMaxBalance));
			}
			//距离最近消费天数
			if(minLastConsumption != null && !minLastConsumption.isEmpty() && maxLastConsumption != null && !maxLastConsumption.isEmpty()){
				builder.setLastConsumption(Integer.parseInt(minLastConsumption), Integer.parseInt(maxLastConsumption));
			}
			
			//性别
			if(sex != null && !sex.isEmpty()){
				builder.setSex(Member.Sex.valueOf(Integer.parseInt(sex)));
			}
			
			//是否Raw
			if(isRaw != null && !isRaw.isEmpty()){
				builder.setRaw(Boolean.parseBoolean(isRaw));
			}
			
			//年龄段
			if(ageVal != null && !ageVal.isEmpty()){
				if(ageVal.equals("-1")){
					builder.clearAge();
				}else{
					for(String age : ageVal.split(",")){
						builder.addAge(Member.Age.valueOf(Integer.parseInt(age)));
					}
				}
			}
			
			//充值金额
			if(minCharge != null && !minCharge.isEmpty() && maxCharge != null && !maxCharge.isEmpty()){
				builder.setCharge(Float.parseFloat(minCharge), Float.parseFloat(maxCharge));
			}
			
			MemberCondDao.update(staff, builder);
			
			jObject.initTip(true, "修改成功");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}	
	
	
	/**
	 * 新增会员分析条件
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward memberCondTree(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		JObject jobject = new JObject(); 
		StringBuilder typeNode = new StringBuilder();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			List<MemberCond> list = MemberCondDao.getByCond(staff, null);
			MemberCond item = null;
			for(int i = 0; i < list.size(); i++){
				item = list.get(i);
				if(i > 0){
					typeNode.append(",");
				}
				typeNode.append("{")
						.append("text:'" + item.getName() + "'")
						.append(",leaf:true")
						.append(",id:" + item.getId())
						.append("}");				
			}
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print("[" + typeNode.toString() + "]");
		}
		
		return null;
	}
	
	/**
	 * 通过id获取
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		JObject jobject = new JObject(); 
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			MemberCondDao.ExtraCond extraCond = new MemberCondDao.ExtraCond();
			
			if(request.getParameter("id") != null && !request.getParameter("id").isEmpty()){
				extraCond.setId(Integer.parseInt(request.getParameter("id")));
			}
			
			jobject.setRoot(MemberCondDao.getByCond(staff, extraCond));
			
		}catch(BusinessException | SQLException e){
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
	
	/**
	 * 通过id获取
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getById(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final JObject jObject = new JObject(); 
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			final String id = request.getParameter("id");
			jObject.setRoot(MemberCondDao.getById(staff, Integer.parseInt(id)));
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
	
	/**
	 * 删除条件
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward deleteById(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final JObject jObject = new JObject(); 
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			final String id = request.getParameter("id");
			MemberCondDao.deleteById(staff, Integer.parseInt(id));
			jObject.initTip(true, "删除成功");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
	
}

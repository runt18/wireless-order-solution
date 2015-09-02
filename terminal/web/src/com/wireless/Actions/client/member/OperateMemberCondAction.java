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
		String name = request.getParameter("name");
		String memberType = request.getParameter("memberType");
		String memberCondMinConsume = request.getParameter("memberCondMinConsume");
		String memberCondMaxConsume = request.getParameter("memberCondMaxConsume");
		String memberCondMinAmount = request.getParameter("memberCondMinAmount");
		String memberCondMaxAmount = request.getParameter("memberCondMaxAmount");
		String memberCondMinBalance = request.getParameter("memberCondMinBalance");
		String memberCondMaxBalance = request.getParameter("memberCondMaxBalance");
		String memberCondDateRegion = request.getParameter("memberCondDateRegion");
		String memberCondBeginDate = request.getParameter("memberCondBeginDate");
		String memberCondEndDate = request.getParameter("memberCondEndDate");
		JObject jobject = new JObject(); 
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			MemberCond.InsertBuilder insertBuilder = new MemberCond.InsertBuilder(name);
			int minAmount = 0, maxAmount = 0;
			float minConsume = 0, maxConsume = 0, minBalance = 0, maxBalance = 0;
			
			RangeType rangeType = RangeType.valueOf(Integer.parseInt(memberCondDateRegion));
			
			//设置时间段
			insertBuilder.setRangeType(rangeType);
			if(rangeType == RangeType.USER_DEFINE){
				insertBuilder.setRange(memberCondBeginDate, memberCondEndDate);
			}
			
			if(memberType != null && !memberType.isEmpty() && !memberType.equals("-1")){
				insertBuilder.setMemberType(new MemberType(Integer.parseInt(memberType)));
			}
			if(memberCondMinConsume != null && !memberCondMinConsume.isEmpty()){
				minConsume = Float.parseFloat(memberCondMinConsume);
			}
			if(memberCondMaxConsume != null && !memberCondMaxConsume.isEmpty()){
				maxConsume = Float.parseFloat(memberCondMaxConsume);
			}
			if(memberCondMinAmount != null && !memberCondMinAmount.isEmpty()){
				minAmount = Integer.parseInt(memberCondMinAmount);
			}
			if(memberCondMaxAmount != null && !memberCondMaxAmount.isEmpty()){
				maxAmount = Integer.parseInt(memberCondMaxAmount);
			}
			if(memberCondMinBalance != null && !memberCondMinBalance.isEmpty()){
				minBalance = Float.parseFloat(memberCondMinBalance);
			}
			if(memberCondMaxBalance != null && !memberCondMaxBalance.isEmpty()){
				maxBalance = Float.parseFloat(memberCondMaxBalance);
			}
			//设置区间
			insertBuilder.setBalance(minBalance, maxBalance).setConsumeAmount(minAmount, maxAmount).setConsumeMoney(minConsume, maxConsume);
			
			MemberCondDao.insert(staff, insertBuilder);
			
			jobject.initTip(true, "添加成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
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
	 * 修改会员分析条件
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String id = request.getParameter("id");
		String name = request.getParameter("name");
		String memberType = request.getParameter("memberType");
		String memberCondMinConsume = request.getParameter("memberCondMinConsume");
		String memberCondMaxConsume = request.getParameter("memberCondMaxConsume");
		String memberCondMinAmount = request.getParameter("memberCondMinAmount");
		String memberCondMaxAmount = request.getParameter("memberCondMaxAmount");
		String memberCondMinBalance = request.getParameter("memberCondMinBalance");
		String memberCondMaxBalance = request.getParameter("memberCondMaxBalance");
		String memberCondDateRegion = request.getParameter("memberCondDateRegion");
		String memberCondBeginDate = request.getParameter("memberCondBeginDate");
		String memberCondEndDate = request.getParameter("memberCondEndDate");
		JObject jobject = new JObject(); 
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			MemberCond.UpdateBuilder updateBuilder = new MemberCond.UpdateBuilder(Integer.parseInt(id)).setName(name);
			int minAmount = 0, maxAmount = 0;
			float minConsume = 0, maxConsume = 0, minBalance = 0, maxBalance = 0;
			
			RangeType rangeType = RangeType.valueOf(Integer.parseInt(memberCondDateRegion));
			
			//设置时间段
			updateBuilder.setRangeType(rangeType);
			if(rangeType == RangeType.USER_DEFINE){
				updateBuilder.setRange(memberCondBeginDate, memberCondEndDate);
			}
			
			if(memberType != null && !memberType.isEmpty()){
				if(memberType.equals("-1")){
					updateBuilder.setMemberType(null);
				}else{
					updateBuilder.setMemberType(new MemberType(Integer.parseInt(memberType)));
				}
			}
			if(memberCondMinConsume != null && !memberCondMinConsume.isEmpty()){
				minConsume = Float.parseFloat(memberCondMinConsume);
			}
			if(memberCondMaxConsume != null && !memberCondMaxConsume.isEmpty()){
				maxConsume = Float.parseFloat(memberCondMaxConsume);
			}
			if(memberCondMinAmount != null && !memberCondMinAmount.isEmpty()){
				minAmount = Integer.parseInt(memberCondMinAmount);
			}
			if(memberCondMaxAmount != null && !memberCondMaxAmount.isEmpty()){
				maxAmount = Integer.parseInt(memberCondMaxAmount);
			}
			if(memberCondMinBalance != null && !memberCondMinBalance.isEmpty()){
				minBalance = Float.parseFloat(memberCondMinBalance);
			}
			if(memberCondMaxBalance != null && !memberCondMaxBalance.isEmpty()){
				maxBalance = Float.parseFloat(memberCondMaxBalance);
			}
			//设置区间
			updateBuilder.setBalance(minBalance, maxBalance).setConsumeAmount(minAmount, maxAmount).setConsumeMoney(minConsume, maxConsume);
			
			MemberCondDao.update(staff, updateBuilder);
			
			jobject.initTip(true, "修改成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
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
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
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
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
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
		
		JObject jobject = new JObject(); 
		try{
			Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			String id = request.getParameter("id");
			MemberCond cond = MemberCondDao.getById(staff, Integer.parseInt(id));
			jobject.setRoot(cond);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
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
	 * 删除条件
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward deleteById(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		JObject jobject = new JObject(); 
		try{
			Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			String id = request.getParameter("id");
			MemberCondDao.deleteById(staff, Integer.parseInt(id));
			jobject.initTip(true, "删除成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
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

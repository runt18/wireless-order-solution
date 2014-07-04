package com.wireless.Actions.client.member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;
import com.wireless.util.SQLUtil;

public class QueryMemberAction extends DispatchAction {
	
	public ActionForward count(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String memberType = request.getParameter("memberTypes");
		String[] memberTypes = memberType.split(",");
		String pin = (String) request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		List<Jsonable> mts = new ArrayList<Jsonable>();
		String extra = "";
		JObject jobject = new JObject();
		int sum = 0;
		try{
			for (int i = 0; i < memberTypes.length; i++) {
				extra += " AND M.restaurant_id = " + staff.getRestaurantId();
				extra += " AND M.member_type_id = " + memberTypes[i];
				Map<Object, Object> paramsSet = new HashMap<Object, Object>();
				paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
				final MemberType mt = MemberTypeDao.getMemberTypeById(staff, Integer.parseInt(memberTypes[i]));
				final int mc = MemberDao.getMemberCount(paramsSet);
				mts.add(new Jsonable() {
					
					@Override
					public JsonMap toJsonMap(int flag) {
						JsonMap jm = new JsonMap();
						jm.putString("name", mt.getName());
						jm.putInt("memberCount", mc);
						return jm;
					}

					@Override
					public void fromJsonMap(JsonMap jsonMap, int flag) {
						
					}
				});
				sum += mc;
				extra = "";
			}
			final int mTotal = sum;
			mts.add(new Jsonable() {
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("name", "总数");
					jm.putInt("memberCount", mTotal);
					return jm;
				}
				
				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
			});
			
			jobject.setRoot(mts);
		}catch(BusinessException e){
			e.printStackTrace();
		}catch(SQLException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 搜索
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		List<Member> list = null;
//		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String extraCond = " ", orderClause = " ";
			String id = request.getParameter("id");
			String memberType = request.getParameter("memberType");
			String memberTypeAttr = request.getParameter("memberTypeAttr");
			String name = request.getParameter("name");
			String memberCardOrMobile = request.getParameter("memberCardOrMobile");
//			String totalBalance = request.getParameter("usedBalance");
			String usedBalance = request.getParameter("usedBalance");
			String consumptionAmount = request.getParameter("consumptionAmount");
			
//			String point = request.getParameter("point");
//			String usedPoint = request.getParameter("usedPoint");
			String usedBalanceEqual = request.getParameter("usedBalanceEqual");
			String consumptionAmountEqual = request.getParameter("consumptionAmountEqual");
			
			if(id != null && !id.trim().isEmpty() && Integer.valueOf(id.trim()) > 0){
				extraCond += (" AND M.member_id = " + id);
			}else{
				if(memberType != null && !memberType.trim().isEmpty())
					extraCond += (" AND M.member_type_id = " + memberType);
				if(name != null && !name.trim().isEmpty())
					extraCond += (" AND M.name like '%" + name.trim() + "%'");
				
				if(memberCardOrMobile != null && !memberCardOrMobile.trim().isEmpty())
					extraCond += (" AND (M.member_card like '%" + memberCardOrMobile.trim() + "%'  OR M.mobile like '%" + memberCardOrMobile.trim() + "%')");
					
/*				if(totalBalance != null && !totalBalance.trim().isEmpty())
					extraCond += (" AND (M.base_balance + M.extra_balance) " + so + totalBalance);*/
				
				if(usedBalance != null && !usedBalance.trim().isEmpty())
					extraCond += (" AND M.used_balance " + usedBalanceEqual + usedBalance);
				
				if(consumptionAmount != null && !consumptionAmount.trim().isEmpty())
					extraCond += (" AND M.consumption_amount " + consumptionAmountEqual + consumptionAmount);
				
/*				if(usedPoint != null && !usedPoint.trim().isEmpty())
					extraCond += (" AND M.total_point " + so + usedPoint);
				
				if(point != null && !point.trim().isEmpty())
					extraCond += (" AND M.point " + so + point);*/
			}
			
			orderClause = " ORDER BY M.member_id ";
			
			Map<Object, Object> paramsSet = new HashMap<Object, Object>();
			paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
			paramsSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
/*			if(isPaging != null && isPaging.trim().equals("true")){
				countSet = new HashMap<Object, Object>();
				countSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
				jobject.setTotalProperty(MemberDao.getMemberCount(countSet));
				// 分页
				orderClause += " LIMIT " + start + "," + limit;
			}*/
			list = MemberDao.getByCond(staff, extraCond, orderClause);
			List<Member> newList = new ArrayList<Member>(list);  
			if(memberTypeAttr != null && !memberTypeAttr.trim().isEmpty()){
				newList.clear();
				if(Integer.parseInt(memberTypeAttr) == MemberType.Attribute.INTERESTED.getVal()){
					newList.addAll(MemberDao.getInterestedMember(staff, extraCond));
				}else{
					List<Member> attrMember = new ArrayList<Member>();  
					for (Member member : list) {
						if(member.getMemberType().getAttribute().getVal() == Integer.parseInt(memberTypeAttr)){
							attrMember.add(member);
						};
					}
					newList.addAll(attrMember);
				}
			}
			jobject.setTotalProperty(newList.size());
			newList = DataPaging.getPagingData(newList, true, start, limit);
			
			jobject.setRoot(newList);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
			
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}

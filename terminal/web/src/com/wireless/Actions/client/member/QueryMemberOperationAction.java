package com.wireless.Actions.client.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberOperationDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberOperation.OperationType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.SQLUtil;
import com.wireless.util.WebParams;

public class QueryMemberOperationAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String isPaging = request.getParameter("isPaging");
		List<MemberOperation> list = null;
		try{
			
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String restaurantID = (String)request.getAttribute("restaurantID");
			String dataSource = request.getParameter("dataSource");
			String memberMobile = request.getParameter("memberMobile");
			String memberCard = request.getParameter("memberCard");
			String memberName = request.getParameter("memberName");
			String memberType = request.getParameter("memberType");
			String operateType = request.getParameter("operateType");
			String detailOperate = request.getParameter("detailOperate");
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			String total = request.getParameter("total");
			
			String extraCond = null, orderClause = null;
			extraCond = " AND MO.restaurant_id = " + restaurantID;
			
			if(memberMobile != null && !memberMobile.trim().isEmpty()){
				extraCond += (" AND MO.member_mobile like '%" + memberMobile.trim() + "%'");
			}
			if(memberCard != null && !memberCard.trim().isEmpty()){
				extraCond += (" AND MO.member_card like '%" + memberCard.trim() + "%'");
			}
			if(memberName != null && !memberName.trim().isEmpty()){
				extraCond += (" AND MO.member_name like '%" + memberName.trim() + "%'");
			}
			if(memberType != null && !memberType.trim().isEmpty()){
				extraCond += (" AND M.member_type_id = " + memberType);
			}

			if(detailOperate != null && !detailOperate.trim().isEmpty() && Integer.valueOf(detailOperate) > 0){
				extraCond += (" AND MO.operate_type = " + detailOperate);
			}else{
				if(operateType != null && !operateType.trim().isEmpty() && Integer.valueOf(operateType) > 0){
					List<OperationType> types = OperationType.typeOf(Integer.parseInt(operateType));
					String extra = "";
					for (int i = 0; i < types.size(); i++) {
						if(i == 0){
							extra += " MO.operate_type = " + types.get(i).getValue();
						}else{
							extra += " OR MO.operate_type = " + types.get(i).getValue();
						}
					}
					if(Integer.parseInt(operateType) == OperationType.POINT_ADJUST.getType()){
						extra += " OR MO.operate_type = " + OperationType.CONSUME.getValue();
					}
					extraCond += " AND(" + extra + ")";
				}
			}
			
			orderClause = " ORDER BY MO.operate_date ";
			
			Map<Object, Object> paramsSet = new HashMap<Object, Object>(), countSet = null;
			if(isPaging != null && isPaging.trim().equals("true")){
				countSet = new HashMap<Object, Object>();
				if(dataSource.equalsIgnoreCase("today")){
					countSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
					countSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
					jobject.setTotalProperty(MemberOperationDao.getTodayCount(countSet));
				}else if(dataSource.equalsIgnoreCase("history")){
					if(onDuty != null && !onDuty.trim().isEmpty() && offDuty != null && !offDuty.trim().isEmpty()){
						extraCond += (" AND MO.operate_date >= '" + onDuty + "'");
						extraCond += (" AND MO.operate_date <= '" + offDuty + "'");
					}
					countSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
					countSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
					jobject.setTotalProperty(MemberOperationDao.getHistoryCount(countSet));
				}
				paramsSet.put(SQLUtil.SQL_PARAMS_LIMIT_OFFSET, start);
				paramsSet.put(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT, limit);
			}
			paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
			paramsSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
			
			if(dataSource.equalsIgnoreCase("today")){
				list = MemberOperationDao.getToday(paramsSet);
			}else if(dataSource.equalsIgnoreCase("history")){
				list = MemberOperationDao.getHistory(paramsSet);
			}
			
			if(list != null && !list.isEmpty()){
				MemberOperation sum = MemberOperation.newMO(-10, "", "", "");
				sum.setChargeType(list.get(0).getChargeType());
				sum.setComment(list.get(0).getComment());
				sum.setOperationType(list.get(0).getOperationType());
				sum.setPayType(list.get(0).getPayType());
				sum.setOperateSeq(list.get(0).getOperateSeq());
				sum.setStaffName(list.get(0).getStaffName());
				for(MemberOperation temp : list){
					temp.setMember(MemberDao.getMemberById(staff, temp.getMemberId()));
					
					sum.setDeltaBaseMoney(temp.getDeltaBaseMoney() + sum.getDeltaBaseMoney());
					sum.setDeltaExtraMoney(temp.getDeltaExtraMoney() + sum.getDeltaExtraMoney());
					sum.setChargeMoney(temp.getChargeMoney() + sum.getChargeMoney());
					sum.setPayMoney(temp.getPayMoney() + sum.getPayMoney());
					sum.setDeltaPoint(temp.getDeltaPoint() + sum.getDeltaPoint());
				}
				
				if(total != null){
					sum.setMember(list.get(0).getMember());
					list.add(sum);
				}
				
	
			}
			jobject.setRoot(list);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
				
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}

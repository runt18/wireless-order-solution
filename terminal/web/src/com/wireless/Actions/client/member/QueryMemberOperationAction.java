package com.wireless.Actions.client.member;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.member.MemberDao;
import com.wireless.db.member.MemberOperationDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.member.MemberOperation.OperationType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DateType;

public class QueryMemberOperationAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String isPaging = request.getParameter("isPaging");
		try{
			
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String dataSource = request.getParameter("dataSource");
			
			String memberType = request.getParameter("memberType");
			
			String fuzzy = request.getParameter("fuzzy");
			
			String operateType = request.getParameter("operateType");
			String detailOperate = request.getParameter("detailOperate");
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			String total = request.getParameter("total");
			
			MemberOperationDao.ExtraCond extraCond = null;
			
			DateType dy;
			
			if(dataSource.equalsIgnoreCase("today")){
				dy = DateType.TODAY;
			}else{
				dy = DateType.HISTORY;
			}

			if(operateType != null && !operateType.trim().isEmpty() && Integer.valueOf(operateType) > 0){
				if(Integer.valueOf(operateType) == OperationType.CONSUME.getType()){
					extraCond = new MemberOperationDao.ExtraCond4Consume(dy);
				}else{
					extraCond = new MemberOperationDao.ExtraCond(dy);
				}
			}else{
				extraCond = new MemberOperationDao.ExtraCond(dy);
			}

			if(memberType != null && !memberType.trim().isEmpty()){
				extraCond.setMemberType(Integer.parseInt(memberType));
			}
			
			if(fuzzy != null && !fuzzy.trim().isEmpty()){
				List<Member> members = MemberDao.getByCond(staff, new MemberDao.ExtraCond().setFuzzyName(fuzzy), null);
				extraCond.addMember(-1);
				for (Member member : members) {
					extraCond.addMember(member);
				}
			}
			

			if(detailOperate != null && !detailOperate.trim().isEmpty() && Integer.valueOf(detailOperate) > 0){
				extraCond.addOperationType(OperationType.valueOf(detailOperate));
			}else{
				if(operateType != null && !operateType.trim().isEmpty() && Integer.valueOf(operateType) > 0){
					for(OperationType type : OperationType.typeOf(Integer.parseInt(operateType))){
						extraCond.addOperationType(type);
					}
				}
			}
			
			String orderClause = " ORDER BY MO.operate_date DESC " ;
			
			if(isPaging != null && isPaging.trim().equals("true")){
				if(onDuty != null && !onDuty.trim().isEmpty() && offDuty != null && !offDuty.trim().isEmpty()){
					extraCond.setOperateDate(new DutyRange(onDuty, offDuty));
				}
				jobject.setTotalProperty(MemberOperationDao.getAmountByCond(staff, extraCond));
				
				orderClause += " LIMIT " + start + "," + limit;
			}
			
			final List<MemberOperation> list = MemberOperationDao.getByCond(staff, extraCond, orderClause);
			
			if(!list.isEmpty()){
				MemberOperation sum = MemberOperation.newMO(-10, "", "", "");
				sum.setChargeType(list.get(0).getChargeType());
				sum.setComment(list.get(0).getComment());
				sum.setOperationType(list.get(0).getOperationType());
				sum.setPayType(list.get(0).getPayType());
				sum.setOperateSeq(list.get(0).getOperateSeq());
				sum.setStaffName(list.get(0).getStaffName());
				for(MemberOperation temp : list){
					List<Member> members = MemberDao.getByCond(staff, new MemberDao.ExtraCond().setId(temp.getMemberId()), null);
					
					if(members.isEmpty()){
						MemberType delteMT = new MemberType(0);
						delteMT.setName("已删除类型");
						temp.getMember().setMemberType(delteMT);
					}else{
						temp.setMember(members.get(0));
					}
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

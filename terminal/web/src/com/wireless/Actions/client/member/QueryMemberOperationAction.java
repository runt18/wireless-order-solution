package com.wireless.Actions.client.member;

import java.sql.SQLException;
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
import com.wireless.pojo.member.MemberOperation.OperationCate;
import com.wireless.pojo.member.MemberOperation.OperationType;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.util.DataPaging;

public class QueryMemberOperationAction extends Action{

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final JObject jObject = new JObject();
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String isPaging = request.getParameter("isPaging");
		final String dataSource = request.getParameter("dataSource");
		
		final String memberType = request.getParameter("memberType");
		
		final String fuzzy = request.getParameter("fuzzy");
		
		final String payType = request.getParameter("payType");
		//现金,刷卡,签单
		final String chargeType = request.getParameter("chargeType");
		//消费类型,充值类型,积分,金额调整
		final String operateType = request.getParameter("operateType");
		//充值,取款,积分消费,反结账...
		final String detailOperate = request.getParameter("detailOperate");
		final String onDuty = request.getParameter("onDuty");
		final String offDuty = request.getParameter("offDuty");
		final String total = request.getParameter("total");
		try{
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final DateType dateType;
			if(dataSource.equalsIgnoreCase("today")){
				dateType = DateType.TODAY;
			}else{
				dateType = DateType.HISTORY;
			}
			
			final MemberOperationDao.ExtraCond extraCond;

			if(operateType != null && !operateType.trim().isEmpty() && Integer.valueOf(operateType) > 0){
				if(OperationCate.valueOf(Integer.valueOf(operateType)) == OperationCate.CONSUME_TYPE){
					extraCond = new MemberOperationDao.ExtraCond4Consume(dateType);
				}else{
					extraCond = new MemberOperationDao.ExtraCond(dateType);
				}
			}else{
				extraCond = new MemberOperationDao.ExtraCond(dateType);
			}

			if(memberType != null && !memberType.trim().isEmpty()){
				extraCond.setMemberType(Integer.parseInt(memberType));
			}
			
			if(payType != null && !payType.isEmpty() && !payType.equals("-1")){
				extraCond.setPayType(Integer.parseInt(payType));
			}
			
			if(chargeType != null && !chargeType.isEmpty() && !chargeType.equals("-1")){
				extraCond.setChargeType(Integer.parseInt(chargeType));
			}
			
			if(branchId != null && !branchId.isEmpty() && !branchId.equals("-1")){
				extraCond.setBranch(Integer.parseInt(branchId));
			}
			
			if(fuzzy != null && !fuzzy.trim().isEmpty()){
				List<Member> members = MemberDao.getByCond(staff, new MemberDao.ExtraCond().setFuzzyName(fuzzy), null);
				extraCond.addMember(-1);
				for (Member member : members) {
					extraCond.addMember(member);
				}
			}
			
			//如果没选择小分类,则选择所有的小分类
			if(detailOperate != null && !detailOperate.trim().isEmpty() && Integer.valueOf(detailOperate) > 0){
				extraCond.addOperationType(OperationType.valueOf(Integer.parseInt(detailOperate)));
			}else{
				if(operateType != null && !operateType.trim().isEmpty() && Integer.valueOf(operateType) > 0){
					for(OperationType type : OperationType.typeOf(OperationCate.valueOf(Integer.parseInt(operateType)))){
						extraCond.addOperationType(type);
					}
				}
			}
			
			String orderClause = " ORDER BY MO.id DESC " ;
			
			if(isPaging != null && isPaging.trim().equals("true")){
				if(onDuty != null && !onDuty.trim().isEmpty() && offDuty != null && !offDuty.trim().isEmpty()){
					extraCond.setOperateDate(new DutyRange(onDuty, offDuty));
				}
				jObject.setTotalProperty(MemberOperationDao.getAmountByCond(staff, extraCond));
				
//				orderClause += " LIMIT " + start + "," + limit;
			}
			
			List<MemberOperation> list = MemberOperationDao.getByCond(staff, extraCond, orderClause);
			
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
						delteMT.setName("已删除会员");
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
				
				list = DataPaging.getPagingData(list, Boolean.parseBoolean(isPaging), start, limit);
				
				if(total != null){
					sum.setMember(list.get(0).getMember());
					list.add(sum);
				}
			}
			jObject.setRoot(list);
			
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

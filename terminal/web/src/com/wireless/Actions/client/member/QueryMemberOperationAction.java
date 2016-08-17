package com.wireless.Actions.client.member;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.member.MemberOperationDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberOperation.OperationCate;
import com.wireless.pojo.member.MemberOperation.OperationType;
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
		final String pointChanged = request.getParameter("pointChanged");
		//充值实收和充额差异
		final String minDeltaCharge = request.getParameter("minDeltaCharge");
		final String maxDeltaCharge = request.getParameter("maxDeltaCharge");
		//充值比例
		final String minChargeRate = request.getParameter("minChargeRate");
		final String maxChargeRate = request.getParameter("maxChargeRate");
		
		//base money
		final String minDeltaBaseMoney = request.getParameter("minDeltaBase");
		final String maxDeltaBaseMoney = request.getParameter("maxDeltaBase");
		
		//total Money
		final String minDeltaTotalMoney = request.getParameter("minDeltaTotal");
		final String maxDeltaToTalMoney = request.getParameter("maxDeltaTotal");
		
		//是否按日结区间计算
		final String calcByDuty = request.getParameter("calcByDuty");
		
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
			
			if(calcByDuty != null && !calcByDuty.isEmpty() && Boolean.parseBoolean(calcByDuty)){
				extraCond.setCalcByDuty(true);
			}
			
			if(pointChanged != null && !pointChanged.isEmpty()){
				extraCond.setPointChange(Boolean.parseBoolean(pointChanged));
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
				extraCond.setFuzzy(fuzzy);
			}
			
			if(minDeltaCharge != null && !minDeltaCharge.isEmpty()){
				extraCond.setMinDeltaCharge(Float.parseFloat(minDeltaCharge));
			}
			
			if(maxDeltaCharge != null && !maxDeltaCharge.isEmpty()){
				extraCond.setMaxDeltaCharge(Float.parseFloat(maxDeltaCharge));
			}
			
			if(minChargeRate != null && !minChargeRate.isEmpty()){
				extraCond.setMinChargeRate(Float.parseFloat(minChargeRate));
			}
			
			if(maxChargeRate != null && !maxChargeRate.isEmpty()){
				extraCond.setMaxChargeRage(Float.parseFloat(maxChargeRate));
			}
			
			if(minDeltaBaseMoney != null && !minDeltaBaseMoney.isEmpty()){
				extraCond.setMinDeltaBaseMoney(Float.parseFloat(minDeltaBaseMoney));
			}
			
			if(maxDeltaBaseMoney != null && !maxDeltaBaseMoney.isEmpty()){
				extraCond.setMaxDeltaBaseMoney(Float.parseFloat(maxDeltaBaseMoney));
			}
			
			if(minDeltaTotalMoney != null && !minDeltaTotalMoney.isEmpty()){
				extraCond.setMinDeltaTotalMoney(Float.parseFloat(minDeltaTotalMoney));
			}

			if(maxDeltaToTalMoney != null && !maxDeltaToTalMoney.isEmpty()){
				extraCond.setMaxDeltaTotalMoney(Float.parseFloat(maxDeltaToTalMoney));
			}
			//如果没选择小分类,则选择所有的小分类
			if(detailOperate != null && !detailOperate.trim().isEmpty() && Integer.valueOf(detailOperate) > 0){
				extraCond.setOperationType(OperationType.valueOf(Integer.parseInt(detailOperate)));
			}else{
				if(operateType != null && !operateType.trim().isEmpty() && Integer.valueOf(operateType) > 0){
					for(OperationType type : OperationType.typeOf(OperationCate.valueOf(Integer.parseInt(operateType)))){
						extraCond.addOperationType(type);
					}
				}
			}
			
			if(isPaging != null && isPaging.trim().equals("true")){
				if(onDuty != null && !onDuty.trim().isEmpty() && offDuty != null && !offDuty.trim().isEmpty()){
					extraCond.setOperateDate(new DutyRange(onDuty, offDuty));
				}
				jObject.setTotalProperty(MemberOperationDao.getByCond(staff, ((MemberOperationDao.ExtraCond)extraCond.clone()).setOnlyAmount(true), null).size());
				
			}
			
			List<MemberOperation> list = MemberOperationDao.getByCond(staff, extraCond, null);
			
			if(!list.isEmpty()){
				MemberOperation sum = MemberOperation.newMO(-10, "", "", "");
				sum.setChargeType(list.get(0).getChargeType());
				sum.setComment(list.get(0).getComment());
				sum.setOperationType(list.get(0).getOperationType());
				sum.setPayType(list.get(0).getPayType());
				sum.setOperateSeq(list.get(0).getOperateSeq());
				sum.setStaffName(list.get(0).getStaffName());
				for(MemberOperation temp : list){
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

package com.wireless.Actions.couponMgr;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.coupon.CouponDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.coupon.Coupon;

public class OperateCouponAction extends DispatchAction{

	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String typeId = request.getParameter("typeId");
		String memberTypes = request.getParameter("memberTypes");
		
		String pin = (String) request.getAttribute("pin");
		String[] memberType = null;
		JObject jobject = new JObject();
		try{
			Coupon.InsertAllBuilder builder = new Coupon.InsertAllBuilder(Integer.parseInt(typeId));
			memberType = memberTypes.split(",");
			for (int i = 0; i < memberType.length; i++) {
				List<Member> list = MemberDao.getByCond(StaffDao.verify(Integer.parseInt(pin)), " AND M.member_type_id = " + memberType[i], null);
				for (Member m : list) {
					builder.addMemberId(m.getId());
				}
			}
			CouponDao.insertAll(StaffDao.verify(Integer.parseInt(pin)), builder);
			
			jobject.initTip(true, "发放成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
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

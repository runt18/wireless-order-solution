package com.wireless.Actions.client.memberLevel;

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

import com.wireless.db.client.member.MemberLevelDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.client.MemberLevel;
import com.wireless.pojo.staffMgr.Staff;

public class QueryMemberLevelAction extends DispatchAction{

	public ActionForward chart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		List<String> xAxis = new ArrayList<String>();
		List<Integer> data = new ArrayList<Integer>();
		List<MemberLevel> memberLevelList = new ArrayList<MemberLevel>(); 
		try{
			String pin = (String) request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			memberLevelList = MemberLevelDao.getMemberLevels(staff);
			for (int i = 0; i < memberLevelList.size(); i++) {
				String x = "\'" + memberLevelList.get(i).getMemberType().getName() + "\'";
				xAxis.add(x);
				data.add(memberLevelList.get(i).getPointThreshold());
			}
			
			String chart = "{\"xAxis\":"+ xAxis +", \"data\":"+ data +" }";
			
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("chart", chart);
			
			jobject.setRoot(memberLevelList);
			jobject.setOther(map);
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

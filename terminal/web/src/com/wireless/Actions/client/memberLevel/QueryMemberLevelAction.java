package com.wireless.Actions.client.memberLevel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.member.MemberLevelDao;
import com.wireless.db.member.MemberTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.member.MemberLevel;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.staffMgr.Staff;

public class QueryMemberLevelAction extends DispatchAction{

	public ActionForward chart(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String webMemberLevelChart = request.getParameter("webMemberLevelChart");
		JObject jobject = new JObject();
		List<String> ydata = new ArrayList<String>();
		List<Float> data = new ArrayList<Float>();
		List<MemberLevel> memberLevelList = new ArrayList<MemberLevel>(); 
		try{
			String pin = (String) request.getAttribute("pin");
			String rid = request.getParameter("rid");
			Staff staff;
			if(pin != null && !pin.isEmpty()){
				staff = StaffDao.verify(Integer.parseInt(pin));
			}else{
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(rid));
			}
			memberLevelList = MemberLevelDao.get(staff);
			
			List<MemberType> memberTypeList = MemberTypeDao.getByCond(staff, null, null);
			
			for (int j = 0; j < memberLevelList.size(); j++) {
					ydata.add("{y:0, level : \'" +  memberLevelList.get(j).getMemberType().getName() 
								+ "\', x:" + memberLevelList.get(j).getPointThreshold() 
								+ ", marker: {symbol:\'diamond\'}, status : 1"+ (data.isEmpty()?", first:true":"") +"}" );
					data.add((float) memberLevelList.get(j).getPointThreshold());
			}
			
			if(webMemberLevelChart != null && !webMemberLevelChart.isEmpty()){
				int levelCount = data.size();
				int delta = memberTypeList.size() - data.size();
				if(memberTypeList.size() > data.size()){
					for (int i = 0; i < delta; i++) {
						StringBuilder y = new StringBuilder();
						y.append("{y : 0, level : \'等级" + ((i+1)+levelCount) + "\'");
						if(data.size() > 0){
							y.append(",x:" + (data.get(levelCount + i - 1)*1.5 + 1.3));
						}else{
							y.append(",x:0");
						}
						if(i == 0){
							y.append(", marker:{symbol:\'circle\'}");
							y.append(", status : 2");
							y.append(", color : \'maroon\'");
							y.append(data.isEmpty()?", first:true":"");
						}else{
							y.append(", marker:{symbol:\'square\'}");
							y.append(", status : 3");
							y.append(", color : \'Gray\'");
						}
						y.append("}");
						ydata.add(y.toString());
						data.add((float) (data.size() > 0 ? (data.get(levelCount + i - 1)*1.5 + 1.3) : 0));
					}
				}
			}
			
			final String chart = "{\"data\":"+ ydata +" }";
			
			List<Jsonable> js = new ArrayList<>();
			for (final MemberLevel ml : memberLevelList) {
				js.add(new Jsonable() {
					
					@Override
					public JsonMap toJsonMap(int flag) {
						JsonMap jm = new JsonMap();
						jm.putJsonable(ml, flag);
						jm.putFloat("exchangeRate", ml.getMemberType().getExchangeRate());
						jm.putFloat("chargeRate", ml.getMemberType().getChargeRate());
						jm.putJsonable("discount", ml.getMemberType().getDefaultDiscount(), flag);
						return jm;
					}
					
					@Override
					public void fromJsonMap(JsonMap jsonMap, int flag) {
						
					}
				});
			}
			
			jobject.setRoot(js);
			
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("chart", chart);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
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

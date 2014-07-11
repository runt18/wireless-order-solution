package com.wireless.Actions.client.memberType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberLevelDao;
import com.wireless.db.client.member.MemberTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WeixinMemberDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberLevel;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.staffMgr.Staff;

public class QueryMemberTypeAction extends DispatchAction {
	
	/**
	 * 
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
		List<MemberType> list = new ArrayList<MemberType>();
		
		try{
			
			String pin = (String)request.getAttribute("pin");
			
			String name = request.getParameter("name");
			String attr = request.getParameter("attr");
			
			String extraCond = "";
			if(name != null && !name.trim().isEmpty()){
				extraCond += (" AND MT.name like '%" + name.trim() + "%' ");
			}
			
			if(attr != null && !attr.trim().isEmpty()){
				extraCond += (" AND MT.attribute = " + attr);
			}
			
			list = MemberTypeDao.getMemberType(StaffDao.verify(Integer.parseInt(pin)), extraCond, " ORDER BY MT.member_type_id ");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			jobject.setTotalProperty(list.size());
			jobject.setRoot(list);
			
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward tree(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		StringBuilder tsb = new StringBuilder();
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			List<MemberType> list = MemberTypeDao.getMemberType(StaffDao.verify(Integer.parseInt(pin)), null, " ORDER BY MT.member_type_id ");
			List<MemberLevel> levelList = MemberLevelDao.getMemberLevels(staff);
			List<Member> interestedMembers = MemberDao.getInterestedMember(staff, null);
			MemberType item = null;
			StringBuilder typeNode = new StringBuilder(), levelNode = new StringBuilder();
			typeNode.append("{")
					.append("text:'会员类型'")
					.append(", MemberTypeId : -1")
					.append(",expanded:true")
					.append(",children:[");
			StringBuilder change = new StringBuilder(), point = new StringBuilder(), interested = new StringBuilder();
			change.append("{")
				  .append("text:'充值属性'")
				  .append(",attr:"+MemberType.Attribute.CHARGE.getVal())
				  .append(",children:[");
			point.append("{")
				 .append("text:'积分属性'")
				 .append(",attr:"+MemberType.Attribute.POINT.getVal())
				 .append(",expanded:true")
				 .append(",children:[");
			boolean hc = false, hp = false;
			for(int i = 0; i < list.size(); i++){
				item = list.get(i);
				if(item.getAttribute() == MemberType.Attribute.CHARGE){
					change.append(hc ? "," : "");
					children(item, change);
					if(!hc){hc = true;}
				}else if(item.getAttribute() == MemberType.Attribute.POINT){
					point.append(hp ? "," : "");
					children(item, point);
					if(!hp){hp = true;}
				}
			}
			change.append("]}");
			point.append("]}");

			
			typeNode.append(change).append(",").append(point).append("]");
			typeNode.append("}");
			tsb.append("[").append(typeNode);
			if(!levelList.isEmpty()){
				levelNode.append("{")
				 .append("text:'会员级别'")
				 .append(",children:[");
				for (int i = 0; i < levelList.size(); i++) {
					if(i > 0){
						levelNode.append(",");
					}
					levelNode.append("{")
							.append("text:'" + levelList.get(i).getMemberType().getName() + "  (Lv"+ (i+1) +", "+ levelList.get(i).getPointThreshold() +"分)'")
							.append(",leaf:true")
							.append(",memberTypeId:" + levelList.get(i).getMemberType().getId())
							.append(",memberTypeName:'" + levelList.get(i).getMemberType().getName() + "'")
							.append("}");
				}
				levelNode.append("]}");
				tsb.append(",").append(levelNode);
			}
			if(!interestedMembers.isEmpty()){
				interested.append("{")
				 .append("text:'关注的会员'")
				 .append(",attr:"+MemberType.Attribute.INTERESTED.getVal())
				 .append(",leaf:true")
				 .append("}");
				tsb.append(",").append(interested);
			}
			tsb.append("]");
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(tsb.toString());
		}
		return null;
	}
	
	private StringBuilder children(MemberType item, StringBuilder sb){
		sb.append("{")
			.append("text:'" + item.getName() + "'")
			.append(",leaf:true")
			.append(",memberTypeId:" + item.getId())
			.append(",memberTypeName:'" + item.getName() + "'")
			.append("}");
		return sb;
	}
	
	public ActionForward notBelongType(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String) request.getAttribute("pin");
		JObject jobject = new JObject();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			List<MemberType> list = MemberTypeDao.getNotBelongMemberType(staff);
			jobject.setRoot(list);
			jobject.setTotalProperty(list.size());
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
	
	public ActionForward getMemberLevel(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		try{

			final List<Jsonable> root = new ArrayList<Jsonable>();
			final JsonMap extra = new JsonMap();
			String openId = request.getParameter("oid");
			String formId = request.getParameter("fid");
			int rid = WeixinRestaurantDao.getRestaurantIdByWeixin(formId);
			int mid = WeixinMemberDao.getBoundMemberIdByWeixin(openId, formId);
			Staff staff = StaffDao.getAdminByRestaurant(rid); 
			List<MemberLevel> list = MemberLevelDao.getMemberLevels(staff);
			
			for (final MemberLevel ml : list) {
				final List<Member> mlists = MemberDao.getByCond(staff, new MemberDao.ExtraCond().setMemberType(ml.getMemberType().getId()).setId(mid), null);
				if(mlists.size() > 0){
					extra.putJsonable("member", mlists.get(0), 0);
				}
				root.add(new Jsonable() {
					@Override
					public JsonMap toJsonMap(int flag) {
						JsonMap jm = new JsonMap();
						jm.putInt("pointThreshold", ml.getPointThreshold());
						jm.putString("memberTypeName", ml.getMemberType().getName());
						jm.putJsonable("memberType", ml.getMemberType(), 0);
						jm.putBoolean("inLevel", mlists.size() != 0 ? true : false);
						return jm;
					}
					
					@Override
					public void fromJsonMap(JsonMap jsonMap, int flag) {
						
					}
				});
				
			}
			
			jobject.setRoot(root);
			jobject.setTotalProperty(root.size());
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					return extra;
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

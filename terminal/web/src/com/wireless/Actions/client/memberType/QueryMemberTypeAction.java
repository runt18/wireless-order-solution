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

import com.wireless.db.member.MemberDao;
import com.wireless.db.member.MemberLevelDao;
import com.wireless.db.member.MemberTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberLevel;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.staffMgr.Staff;

public class QueryMemberTypeAction extends DispatchAction {
	
	/**
	 * 获取会员类型
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward normal(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String memberTypeId = request.getParameter("memberTypeId");
		final String branchId = request.getParameter("branchId");
		final String name = request.getParameter("name");
		final String attr = request.getParameter("attr");
		final String type = request.getParameter("type");
		
		final JObject jObject = new JObject();
		
		try{
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			final MemberTypeDao.ExtraCond extraCond = new MemberTypeDao.ExtraCond();
			
			if(memberTypeId != null && !memberTypeId.isEmpty()){
				extraCond.setId(Integer.parseInt(memberTypeId));
			}
			
			if(name != null && !name.trim().isEmpty()){
				extraCond.setName(name);
			}
			
			if(attr != null && !attr.trim().isEmpty()){
				extraCond.setAttribute(MemberType.Attribute.valueOf(attr));
			}
			if(type != null && !type.isEmpty()){
				extraCond.setType(MemberType.Type.valueOf(type));
			}
			
			final List<MemberType> result = MemberTypeDao.getByCond(staff, extraCond, " ORDER BY MT.member_type_id ");
			
			jObject.setTotalProperty(result.size());
			jObject.setRoot(result);
			
		}catch(BusinessException e){
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
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward tree(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final StringBuilder typeNode = new StringBuilder();
		
		try{
			final List<MemberType> list = MemberTypeDao.getByCond(StaffDao.verify(Integer.parseInt(pin)), null, " ORDER BY MT.member_type_id ");
			
			for(int i = 0; i < list.size(); i++){
				MemberType item = list.get(i);
				if(i > 0){
					typeNode.append(",");
				}
				typeNode.append("{")
				.append("text:'" + item.getName() + "'")
				.append(",leaf:true")
				.append(",memberTypeId:" + item.getId())
				.append(",memberTypeName:'" + item.getName() + "'")
				.append(",type:" + item.getType().getVal())
				.append(",chargeRate:" + item.getChargeRate())
				.append(",exchangeRate:" + item.getExchangeRate())
				.append(",initialPoint:" + item.getInitialPoint())
				.append(",attributeValue:" + item.getAttribute().getVal())
				.append(",desc:'" + item.getDesc() + "'")
				.append(",pricePlans:[" + priceChildren(item.getPrices()) + "]" )
				.append(",pricePlan:" + (item.getDefaultPrice() != null?item.getDefaultPrice().getId() : -1))
				.append(",discounts:[" + children(item.getDiscounts()) + "]" )
				.append(",discount:" + item.getDefaultDiscount().getId())
				.append("}");				
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print("[" + typeNode.toString() + "]");
		}
		return null;
	}
	
	private StringBuilder children(List<Discount> items){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < items.size(); i++) {
			if(i > 0){
				sb.append(",");
			}
			sb.append("{")
			.append("discountID:" + items.get(i).getId())
			.append(",text:'" + items.get(i).getName() + "'")
			.append("}");
		}
		return sb;	

	}	
	
	private StringBuilder priceChildren(List<PricePlan> items){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < items.size(); i++) {
			if(i > 0){
				sb.append(",");
			}
			sb.append("{")
			.append("id:" + items.get(i).getId())
			.append(",name:'" + items.get(i).getName() + "'")
			.append("}");
		}
		return sb;	

	}		
	
	public ActionForward notBelongType(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			jobject.initTip4Exception(e);
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
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(formId);
			Staff staff = StaffDao.getAdminByRestaurant(rid); 
			List<MemberLevel> list = MemberLevelDao.get(staff);
			
			for (final MemberLevel ml : list) {
				final List<Member> mlists = MemberDao.getByCond(staff, new MemberDao.ExtraCond().setMemberType(ml.getMemberType().getId()).setWeixinSerial(openId), null);
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
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}

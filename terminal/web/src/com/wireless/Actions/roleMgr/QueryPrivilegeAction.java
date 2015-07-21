package com.wireless.Actions.roleMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.menuMgr.PricePlanDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.PrivilegeDao;
import com.wireless.db.staffMgr.RoleDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.restaurantMgr.Module;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Page;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Privilege.Cate;
import com.wireless.pojo.staffMgr.Privilege.Code;
import com.wireless.pojo.staffMgr.Privilege4Price;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;

public class QueryPrivilegeAction extends DispatchAction{

	/**
	 * 根据角色获取权限
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward normal(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		String roleId = request.getParameter("roleId");
		List<Privilege> rolePrivilege = new ArrayList<Privilege>();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			//根据id获取role
			Role role = RoleDao.getyById(staff, Integer.parseInt(roleId));
			if(role != null){
				rolePrivilege = role.getPrivileges();
			}
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			if(!rolePrivilege.isEmpty()){
				jobject.setTotalProperty(rolePrivilege.size());
				jobject.setRoot(rolePrivilege);
				
			}
			response.getWriter().print(jobject.toString());
		}
		
		return null;
		
	}
	
	private static class CateNode implements Entry<Cate, List<Privilege>>, Comparable<CateNode>{

		private final Cate key;
		private final List<Privilege> value;
		
		public CateNode(Cate key, List<Privilege> value){
			this.key = key;
			
			this.value = value;
		}
		
		@Override
		public Cate getKey() {
			return key;
		}

		@Override
		public List<Privilege> getValue() {
			return value;
		}

		@Override
		public List<Privilege> setValue(List<Privilege> value) {
			throw new UnsupportedOperationException("The dept node is immutable");
		}

		@Override
		public int compareTo(CateNode o) {
			if(this.key.getDisplayId() < o.key.getDisplayId()){
				return -1;
			}else if(this.key.getDisplayId() > o.key.getDisplayId()){
				return 1;
			}else{
				return 0;
			}
		}
		
	}
	
	/**
	 * 未选中角色时的所有权限列表
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward tree(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		String pin = (String) request.getAttribute("pin");
		StringBuilder tree = new StringBuilder();
		List<Privilege> root = null;
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			root = PrivilegeDao.getByCond(staff, null);
			if(root.size() > 0){
				List<CateNode> cates = new ArrayList<>();
				//前台cate
				List<Privilege> front_ps = new ArrayList<>();
				CateNode front = new CateNode(Privilege.Cate.FRONT_BUSINESS, front_ps);
				cates.add(front);
				//后台cate
				List<Privilege> basic_ps = new ArrayList<>();
				CateNode basic = new CateNode(Privilege.Cate.BASIC, basic_ps);
				cates.add(basic);
				//库存cate
				List<Privilege> inventory_ps = new ArrayList<>();
				CateNode inventory = new CateNode(Privilege.Cate.INVENTORY, inventory_ps);
				cates.add(inventory);
				//历史cate
				List<Privilege> history_ps = new ArrayList<>();
				CateNode history = new CateNode(Privilege.Cate.HISTORY, history_ps);
				cates.add(history);
				//会员cate
				List<Privilege> member_ps = new ArrayList<>();
				CateNode member = new CateNode(Privilege.Cate.MEMBER, member_ps);
				cates.add(member);
				//系统cate
				List<Privilege> system_ps = new ArrayList<>();
				CateNode system = new CateNode(Privilege.Cate.SYSTEM, system_ps);
				cates.add(system);
				//微信cate
				List<Privilege> weixin_ps = new ArrayList<>();
				CateNode weixin = new CateNode(Privilege.Cate.WEIXIN, weixin_ps);
				cates.add(weixin);
				//短信cate
				List<Privilege> sms_ps = new ArrayList<>();
				CateNode sms = new CateNode(Privilege.Cate.SMS, sms_ps);
				cates.add(sms);
				
				Collections.sort(cates);
				
				for (Privilege p : root) {
					if(p.getCate() == Privilege.Cate.FRONT_BUSINESS){
						front_ps.add(p);
					}else if(p.getCate() == Privilege.Cate.BASIC){
						basic_ps.add(p);
					}else if(p.getCate() == Privilege.Cate.INVENTORY){
						inventory_ps.add(p);
					}else if(p.getCate() == Privilege.Cate.HISTORY){
						history_ps.add(p);
					}else if(p.getCate() == Privilege.Cate.MEMBER){
						member_ps.add(p);
					}else if(p.getCate() == Privilege.Cate.SYSTEM){
						system_ps.add(p);
					}else if(p.getCate() == Privilege.Cate.WEIXIN){
						weixin_ps.add(p);
					}else if(p.getCate() == Privilege.Cate.SMS){
						sms_ps.add(p);
					}
				}
				
				
				tree.append("[");
				for (int i = 0; i < cates.size(); i++) {
					if(i > 0){
						tree.append(",");
					}
					tree.append("{");
					tree.append("leaf:false");
					StringBuilder children = new StringBuilder();
					//foreach权限种类
					for (int j = 0; j < cates.get(i).getValue().size(); j++) {
						Privilege p = cates.get(i).getValue().get(j);
						if(j>0){
							children.append(",");
						}
						children.append("{");
						
						//前台权限里包含折扣和价格方案
						if(p.getCode() == Code.DISCOUNT){
							children.append("leaf:false");
							StringBuilder grandson = new StringBuilder();
							
							for (int k = 0; k < p.getDiscounts().size(); k++) {
								if(k>0){
									grandson.append(",");
								}
								grandson.append("{");
								grandson.append("checked:false");
								grandson.append(",leaf:true");
								grandson.append(",text:'" + p.getDiscounts().get(k).getName() + "'");
								grandson.append(",discountId:'" + p.getDiscounts().get(k).getId() + "'");
								grandson.append(",isDiscount:true");
								grandson.append("}");
							}
							
							children.append(",children : [" + grandson.toString() + "]");
						}else if(p.getCode() == Code.PRICE_PLAN){
							List<PricePlan> list = PricePlanDao.getByCond(staff, null);
							if(!list.isEmpty()){
								children.append("leaf:false");
								StringBuilder grandson = new StringBuilder();
								
								for (int k = 0; k < list.size(); k++) {
									if(k>0){
										grandson.append(",");
									}
									grandson.append("{");
									grandson.append("checked:false");
									grandson.append(",leaf:true");
									grandson.append(",text:'" + list.get(k).getName() + "'");
									grandson.append("}");
								}
								children.append(",children : [" + grandson.toString() + "]");
							}else{
								children.append("leaf:true");
								children.append(",hidden:true");
							}
							
						}else{
							children.append("leaf:true");
						}
						
						children.append(",checked:false");
						children.append(",text:'" + p.getCode().getDesc() + "'");
						children.append(",pId:'" + p.getId() + "'");
						children.append("}");
					}
					tree.append(",children : [" + children.toString() + "]");
					tree.append(",checked:false");
					
					tree.append(",text:'" + cates.get(i).getKey().getDesc() + "'");
					tree.append("}");
				}
				tree.append("]");
				
				
				
				
/*				tree.append("[");
				for (int i = 0; i < root.size(); i++) {
					if(i > 0){
						tree.append(",");
					}
					tree.append("{");
					if(root.get(i).getCode() == Code.DISCOUNT){
						tree.append("leaf:false");
						StringBuilder children = new StringBuilder();
						
						for (int j = 0; j < root.get(i).getDiscounts().size(); j++) {
							if(j>0){
								children.append(",");
							}
							children.append("{");
							children.append("checked:false");
							children.append(",leaf:true");
							children.append(",text:'" + root.get(i).getDiscounts().get(j).getName() + "'");
							children.append(",discountId:'" + root.get(i).getDiscounts().get(j).getId() + "'");
							children.append(",isDiscount:true");
							children.append("}");
						}
						tree.append(",children : [" + children.toString() + "]");
					}else if(root.get(i).getCode() == Code.PRICE_PLAN){
						List<PricePlan> list = PricePlanDao.getByCond(staff, null);
						if(!list.isEmpty()){
							tree.append("leaf:false");
							StringBuilder children = new StringBuilder();
							
							for (int j = 0; j < list.size(); j++) {
								if(j>0){
									children.append(",");
								}
								children.append("{");
								children.append("checked:false");
								children.append(",leaf:true");
								children.append(",text:'" + list.get(j).getName() + "'");
								children.append("}");
							}
							tree.append(",children : [" + children.toString() + "]");
						}else{
							tree.append("leaf:true");
							tree.append(",hidden:true");
						}
						
					}else{
						tree.append("leaf:true");
					}
					tree.append(",checked:false");
					
					tree.append(",text:'" + root.get(i).getCode().getDesc() + "'");
					tree.append(",pId:'" + root.get(i).getId() + "'");
					tree.append("}");
				}
				tree.append("]");*/
				
				
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(tree.toString());
		}
		return null;
		
	}	
	
	/**
	 * 系统功能列表树
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public ActionForward pageTree(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception, SQLException, BusinessException{
		
		String pin = (String) request.getAttribute("pin");
		StringBuilder tree = new StringBuilder();
		
		Restaurant restaurant = RestaurantDao.getById(Integer.parseInt((String)request.getAttribute("restaurantID")));
		List<Module> modules = restaurant.getModules();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			tree.append("[");
			
			List<Privilege> privileges = new ArrayList<Privilege>(RoleDao.getyById(staff, staff.getRole().getId()).getPrivileges());
			Collections.sort(privileges, Privilege.BY_CATE);
			
			for (Privilege privilege : privileges) {
				
				if(privilege.getCode() == Code.BASIC){
					tree.append("{");
					tree.append("leaf:false");
					tree.append(",expanded:true");
					tree.append(",text:'后台'");
					tree.append(",id:'basicMgr'");
					tree.append(",cls:'tFont'");
					StringBuilder children = new StringBuilder();
					for (int i = 0; i < Page.Basic.values().length; i++) {
						if(i > 0){
							children.append(",");
						}
						if(Page.Basic.values()[i].getUrl().isEmpty()){
							
						}
						children.append("{leaf:true," + "icon:'" + Page.Basic.values()[i].getImage() +"'" + ", text:'" + Page.Basic.values()[i].getDesc() + "', mId:'" +Page.Basic.values()[i].getMgrId() + "', cls:'font', url:'" + Page.Basic.values()[i].getUrl() + "'}");
					}

					tree.append(",children : [" + children.toString() + "]");
					tree.append("},");
				}else if(privilege.getCode() == Code.MEMBER_CHECK){
					tree.append("{");
					tree.append("leaf:false");
					tree.append(",id:'memberMgr'");
					Module mModule = new Module(Module.Code.MEMBER);
					if(modules.indexOf(mModule) < 0){
						tree.append(",expanded:false");
						tree.append(",text:'微信会员(未开通)'");
						tree.append(",cls:'unModuleFont'");
						tree.append(",listeners : {beforeexpand : function(){warnModule('会员模块未开通, 只能录入50条信息');}}");
					}else{
						tree.append(",expanded:true");
						tree.append(",text:'微信会员'");
						tree.append(",cls:'tFont'");
					}

					StringBuilder children = new StringBuilder();
					for (int i = 0; i < Page.Member.values().length; i++) {
						if(i > 0){
							children.append(",");
						}
						children.append("{leaf:true," + "icon:'" + Page.Member.values()[i].getImage() +"'" + ", text:'" + Page.Member.values()[i].getDesc() + "', mId:'" +Page.Member.values()[i].getMgrId() + "', cls:'font', url:'" + Page.Member.values()[i].getUrl() + "'}");
					}
					tree.append(",children : [" + children.toString() + "]");
					tree.append("},");
				}else if(privilege.getCode() == Code.HISTORY){
					tree.append("{");
					tree.append("leaf:false");
					tree.append(",expanded:true");
					tree.append(",id:'historyMgr'");
					tree.append(",text:'统计'");
					tree.append(",cls:'tFont'");
					StringBuilder children = new StringBuilder();
					for (int i = 0; i < Page.History.values().length; i++) {
						if(i > 0){
							children.append(",");
						}
						children.append("{leaf:true," + "icon:'" + Page.History.values()[i].getImage() +"'" + ", text:'" + Page.History.values()[i].getDesc() + "', mId:'" +Page.History.values()[i].getMgrId() + "', cls:'font', url:'" + Page.History.values()[i].getUrl() + "'}");
					}

					tree.append(",children : [" + children.toString() + "]");
					tree.append("},");
				}else if(privilege.getCode() == Code.INVENTORY){
					tree.append("{");
					tree.append("leaf:false");
					tree.append(",expanded:false");
					tree.append(",id:'stockMgr'");
					Module mModule = new Module(Module.Code.INVENTORY);
					if(modules.indexOf(mModule) < 0){
						tree.append(",text:'库存(未开通)'");
						tree.append(",cls:'unModuleFont'");
						tree.append(",listeners : {beforeexpand : function(){warnModule('库存模块未开通, 只能录入50条信息');}}");
					}else{
						tree.append(",text:'库存'");
						tree.append(",cls:'tFont'");
					}
					StringBuilder children = new StringBuilder();
					for (int i = 0; i < Page.Stock.values().length; i++) {
						if(i > 0){
							children.append(",");
						}
						children.append("{leaf:true," + (Page.Stock.values()[i].getUrl().isEmpty()?"id:'" +Page.Stock.values()[i].getMgrId() + "'," : "") + " text:'" + Page.Stock.values()[i].getDesc() + "'," + "icon:'" + Page.Stock.values()[i].getImage() +"'" + ", mId:'" +Page.Stock.values()[i].getMgrId() + "', cls:'font', url:'" + Page.Stock.values()[i].getUrl() + "'}");
					}

					tree.append(",children : [" + children.toString() + "]");
					tree.append("},");
				}else if(privilege.getCode() == Code.SYSTEM){
					tree.append("{");
					tree.append("leaf:false");
					tree.append(",expanded:true");
					tree.append(",id:'systemMgr'");
					tree.append(",text:'系统'");
					tree.append(",cls:'tFont'");
					StringBuilder children = new StringBuilder();
					for (int i = 0; i < Page.System.values().length; i++) {
						if(i > 0){
							children.append(",");
						}
						children.append("{leaf:true," + "icon:'" + Page.System.values()[i].getImage() +"'" + ", text:'" + Page.System.values()[i].getDesc() + "', mId:'" +Page.System.values()[i].getMgrId() + "', cls:'font', url:'" + Page.System.values()[i].getUrl() + "'}");
					}
					tree.append(",children : [" + children.toString() + "]");
					tree.append("},");
				}
/*				else if(privilege.getCode() == Code.WEIXIN){
					tree.append("{");
					tree.append("leaf:false");
					tree.append(",expanded:true");
					tree.append(",id:'weixinMgr'");
					tree.append(",text:'微信'");
					tree.append(",cls:'tFont'");
					StringBuilder children = new StringBuilder();
					for (int i = 0; i < Page.Weixin.values().length; i++) {
						if(i > 0){
							children.append(",");
						}
						children.append("{leaf:true," + "icon:'" + Page.Weixin.values()[i].getImage() +"'" + ", text:'" + Page.Weixin.values()[i].getDesc() + "', mId:'" +Page.Weixin.values()[i].getMgrId() + "', cls:'font', url:'" + Page.Weixin.values()[i].getUrl() + "'}");
					}
					tree.append(",children : [" + children.toString() + "]");
					tree.append("},");
				}*/
				else if(privilege.getCode() == Code.SMS){
					tree.append("{");
					tree.append("leaf:false");
					tree.append(",expanded:true");
					tree.append(",id:'smsMgr'");
					tree.append(",text:'短信'");
					tree.append(",cls:'tFont'");
					StringBuilder children = new StringBuilder();
					for (int i = 0; i < Page.Sms.values().length; i++) {
						if(i > 0){
							children.append(",");
						}
						children.append("{leaf:true," + "icon:'" + Page.Sms.values()[i].getImage() +"'" + ", text:'" + Page.Sms.values()[i].getDesc() + "', mId:'" +Page.Sms.values()[i].getMgrId() + "', cls:'font', url:'" + Page.Sms.values()[i].getUrl() + "'}");
					}
					tree.append(",children : [" + children.toString() + "]");
					tree.append("},");
				}
			}
			tree.append("]");
			
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(tree.toString());
		}
		return null;
	}
	
	/**
	 * 根据角色获取显示对应权限
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward roleTree(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		String pin = (String) request.getAttribute("pin");
		String roleId = request.getParameter("roleId");
		StringBuilder tree = new StringBuilder();
		List<Privilege> root = null;
		List<Privilege> rolePrivilege = new ArrayList<Privilege>();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			//根据id获取role
			Role role = RoleDao.getyById(staff, Integer.parseInt(roleId));
			if(role != null){
				rolePrivilege = role.getPrivileges();
			}
			//获取所有权限和不是会员类型的所有折扣
			root = PrivilegeDao.getByCond(staff, null);
			if(root.size() > 0){
				
				List<CateNode> cates = new ArrayList<>();
				//前台cate
				List<Privilege> front_ps = new ArrayList<>();
				CateNode front = new CateNode(Privilege.Cate.FRONT_BUSINESS, front_ps);
				cates.add(front);
				//后台cate
				List<Privilege> basic_ps = new ArrayList<>();
				CateNode basic = new CateNode(Privilege.Cate.BASIC, basic_ps);
				cates.add(basic);
				//库存cate
				List<Privilege> inventory_ps = new ArrayList<>();
				CateNode inventory = new CateNode(Privilege.Cate.INVENTORY, inventory_ps);
				cates.add(inventory);
				//历史cate
				List<Privilege> history_ps = new ArrayList<>();
				CateNode history = new CateNode(Privilege.Cate.HISTORY, history_ps);
				cates.add(history);
				//会员cate
				List<Privilege> member_ps = new ArrayList<>();
				CateNode member = new CateNode(Privilege.Cate.MEMBER, member_ps);
				cates.add(member);
				//系统cate
				List<Privilege> system_ps = new ArrayList<>();
				CateNode system = new CateNode(Privilege.Cate.SYSTEM, system_ps);
				cates.add(system);
				//微信cate
				List<Privilege> weixin_ps = new ArrayList<>();
				CateNode weixin = new CateNode(Privilege.Cate.WEIXIN, weixin_ps);
				cates.add(weixin);
				//短信cate
				List<Privilege> sms_ps = new ArrayList<>();
				CateNode sms = new CateNode(Privilege.Cate.SMS, sms_ps);
				cates.add(sms);
				
				for (Privilege p : root) {
					if(p.getCate() == Privilege.Cate.FRONT_BUSINESS){
						front_ps.add(p);
					}
					else if(p.getCate() == Privilege.Cate.BASIC){
						basic_ps.add(p);
					}else if(p.getCate() == Privilege.Cate.INVENTORY){
						inventory_ps.add(p);
					}else if(p.getCate() == Privilege.Cate.HISTORY){
						history_ps.add(p);
					}else if(p.getCate() == Privilege.Cate.MEMBER){
						member_ps.add(p);
					}else if(p.getCate() == Privilege.Cate.SYSTEM){
						system_ps.add(p);
					}else if(p.getCate() == Privilege.Cate.WEIXIN){
						weixin_ps.add(p);
					}else if(p.getCate() == Privilege.Cate.SMS){
						sms_ps.add(p);
					}
				}
				
				
				tree.append("[");
				for (int i = 0; i < cates.size(); i++) {
					if(i > 0){
						tree.append(",");
					}
					tree.append("{");
					tree.append("leaf:false");
					StringBuilder children = new StringBuilder();
					//foreach权限种类
					for (int j = 0; j < cates.get(i).getValue().size(); j++) {
						Privilege p = cates.get(i).getValue().get(j);
						if(j>0){
							children.append(",");
						}
						children.append("{");
						
						
						int index = rolePrivilege.indexOf(p);
						//如果选中role包含此权限, 则勾选上
						if(index >= 0){
							//前台权限里包含折扣和价格方案
							if(p.getCode() == Code.DISCOUNT){
								children.append("leaf:false");
								StringBuilder grandson = new StringBuilder();
								
								for (int k = 0; k < p.getDiscounts().size(); k++) {
									if(k>0){
										grandson.append(",");
									}
									grandson.append("{");
									grandson.append("leaf:true");
									grandson.append(",text:'" + p.getDiscounts().get(k).getName() + "'");
									//对应折扣选中
									if(rolePrivilege.get(index).getDiscounts().isEmpty()){
										if(p.getDiscounts().get(k).isReserved()){
											grandson.append(",disabled:true");
										}
										grandson.append(",checked:true");
									}else{
										int disIndex = rolePrivilege.get(index).getDiscounts().indexOf(p.getDiscounts().get(k));
										if(disIndex >= 0){
											if(p.getDiscounts().get(k).isReserved()){
												grandson.append(",disabled:true");
											}
											grandson.append(",checked:true");
										}else{
											grandson.append(",checked:false");
										}
									}
									
									
									grandson.append(",discountId:'" + p.getDiscounts().get(k).getId() + "'");
									grandson.append(",isDiscount:true");
									grandson.append("}");
									
								}
								
								children.append(",children : [" + grandson.toString() + "]");
							}else if(p.getCode() == Code.PRICE_PLAN){
								
								List<PricePlan> plans = ((Privilege4Price)p).getPricePlans();
								if(plans.isEmpty()){
									children.append("leaf:true");
									children.append(",hidden:true");
								}else{
									children.append("leaf:false");
									
									StringBuilder grandson = new StringBuilder();
									for (int k = 0; k < plans.size(); k++) {
										if(k>0){
											grandson.append(",");
										}
										grandson.append("{");
										grandson.append("leaf:true");
										grandson.append(",text:'" + plans.get(k).getName() + "'");			
																
										int planIndex = ((Privilege4Price)(rolePrivilege.get(index))).getPricePlans().indexOf(plans.get(k));
										if(planIndex >= 0){
											grandson.append(",checked:true");
										}else{
											grandson.append(",checked:false");
										}
										grandson.append(",isPricePlan:true");
										grandson.append(",planId:'" + plans.get(k).getId() + "'");
										grandson.append("}");									
									}
									children.append(",children : [" + grandson.toString() + "]");
								}
								
							}else{
								children.append("leaf:true");
							}
							children.append(",checked:true");
						}else{//如果选中的角色没有此权限,则前面的checkbox不勾选
							//前台权限里包含折扣和价格方案
							if(p.getCode() == Code.DISCOUNT){
								children.append("leaf:false");
								StringBuilder grandson = new StringBuilder();
								
								for (int k = 0; k < p.getDiscounts().size(); k++) {
									if(k>0){
										grandson.append(",");
									}
									grandson.append("{");
									grandson.append("checked:false");
									grandson.append(",leaf:true");
									grandson.append(",text:'" + p.getDiscounts().get(k).getName() + "'");
									grandson.append(",discountId:'" + p.getDiscounts().get(k).getId() + "'");
									grandson.append(",isDiscount:true");
									grandson.append("}");
								}
								
								children.append(",children : [" + grandson.toString() + "]");
							}else if(p.getCode() == Code.PRICE_PLAN){
//								List<PricePlan> list = PricePlanDao.getByCond(staff, null);
								List<PricePlan> list = ((Privilege4Price)root.get(i)).getPricePlans();
								if(!list.isEmpty()){
									children.append("leaf:false");
									StringBuilder grandson = new StringBuilder();
									
									for (int k = 0; k < list.size(); k++) {
										if(k>0){
											grandson.append(",");
										}
										grandson.append("{");
										grandson.append("checked:false");
										grandson.append(",leaf:true");
										grandson.append(",text:'" + list.get(k).getName() + "'");
										grandson.append("}");
									}
									children.append(",children : [" + grandson.toString() + "]");
								}else{
									children.append("leaf:true");
									children.append(",hidden:true");
								}
								
							}else{
								children.append("leaf:true");
							}
							children.append(",checked:false");
						}

						children.append(",text:'" + p.getCode().getDesc() + "'");
						children.append(",pId:'" + p.getId() + "'");
						children.append(",code:" + p.getCode().getVal());
						children.append("}");
					}
					tree.append(",children : [" + children.toString() + "]");
					tree.append(",checked:true");
					
					tree.append(",text:'" + cates.get(i).getKey().getDesc() + "'");
					tree.append("}");
				}
				tree.append("]");
				
/*				
				tree.append("[");
				for (int i = 0; i < root.size(); i++) {
					if(i > 0){
						tree.append(",");
					}
					tree.append("{");

					int index = rolePrivilege.indexOf(root.get(i));
					//如果选中role包含此权限, 则勾选上
					if(index >= 0){
						if(root.get(i).getCode() == Code.DISCOUNT){
							
							tree.append("leaf:false");
							StringBuilder children = new StringBuilder();
							
							for (int j = 0; j < root.get(i).getDiscounts().size(); j++) {
								if(j>0){
									children.append(",");
								}
								children.append("{");
								children.append("leaf:true");
								children.append(",text:'" + root.get(i).getDiscounts().get(j).getName() + "'");
								//转折
								if(rolePrivilege.get(index).getDiscounts().isEmpty()){
									if(root.get(i).getDiscounts().get(j).isReserved()){
										children.append(",disabled:true");
									}
									children.append(",checked:true");
								}else{
									int disIndex = rolePrivilege.get(index).getDiscounts().indexOf(root.get(i).getDiscounts().get(j));
									if(disIndex >= 0){
										if(root.get(i).getDiscounts().get(j).isReserved()){
											children.append(",disabled:true");
										}
										children.append(",checked:true");
									}else{
										children.append(",checked:false");
									}
								}

								children.append(",isDiscount:true");
								children.append(",discountId:'" + root.get(i).getDiscounts().get(j).getId() + "'");
								children.append("}");
							}
							tree.append(",children : [" + children.toString() + "]");
						}else if(root.get(i).getCode() == Code.PRICE_PLAN){
							List<PricePlan> plans = ((Privilege4Price)root.get(i)).getPricePlans();
							if(plans.isEmpty()){
								tree.append("leaf:true");
								tree.append(",hidden:true");
							}else{
								tree.append("leaf:false");
								StringBuilder children = new StringBuilder();
								for (int j = 0; j < plans.size(); j++) {
									if(j>0){
										children.append(",");
									}
									children.append("{");
									children.append("leaf:true");
									children.append(",text:'" + plans.get(j).getName() + "'");			
															
									int planIndex = ((Privilege4Price)(rolePrivilege.get(index))).getPricePlans().indexOf(plans.get(j));
									if(planIndex >= 0){
										children.append(",checked:true");
									}else{
										children.append(",checked:false");
									}
									children.append(",isPricePlan:true");
									children.append(",planId:'" + plans.get(j).getId() + "'");
									children.append("}");									
								}
								tree.append(",children : [" + children.toString() + "]");
							}
						}else{
							tree.append("leaf:true");
						}
						tree.append(",checked:true");
					}else{
						//如果选中的角色没有此权限,则前面的checkbox不勾选
						if(root.get(i).getCode() == Code.DISCOUNT){
							tree.append("leaf:false");
							StringBuilder children = new StringBuilder();
							
							for (int j = 0; j < root.get(i).getDiscounts().size(); j++) {
								if(j>0){
									children.append(",");
								}
								children.append("{");
								children.append("leaf:true");
								children.append(",text:'" + root.get(i).getDiscounts().get(j).getName() + "'");
								if(root.get(i).getDiscounts().get(j).isReserved()){
									children.append(",checked:true");
									children.append(",disabled:true");
								}else{
									children.append(",checked:false");
								}
								
								children.append(",discountId:'" + root.get(i).getDiscounts().get(j).getId() + "'");
								children.append(",isDiscount:true");
								children.append("}");
							}
							tree.append(",children : [" + children.toString() + "]");
						}else if(root.get(i).getCode() == Code.PRICE_PLAN){
							List<PricePlan> plans = ((Privilege4Price)root.get(i)).getPricePlans();
							if(plans.isEmpty()){
								tree.append("leaf:true");
								tree.append(",hidden:true");
							}else{
								tree.append("leaf:false");
								StringBuilder children = new StringBuilder();
								for (int j = 0; j < plans.size(); j++) {
									if(j>0){
										children.append(",");
									}
									children.append("{");
									children.append("leaf:true");
									children.append(",text:'" + plans.get(j).getName() + "'");			
									children.append(",checked:false");
									children.append(",isPricePlan:true");
									children.append(",planId:'" + plans.get(j).getId() + "'");
									children.append("}");									
								}
								tree.append(",children : [" + children.toString() + "]");								
							}
						}else{
							tree.append("leaf:true");
						}
						tree.append(",checked:false");
					}
					
					
					tree.append(",text:'" + root.get(i).getCode().getDesc() + "'");
					tree.append(",pId:'" + root.get(i).getId() + "'");
					tree.append(",code:" + root.get(i).getCode().getVal());
					tree.append("}");
				}
				tree.append("]");
				*/
				
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(tree.toString());
		}
		return null;
		
	}
}

package com.wireless.Actions.orderMgr;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.frontBusiness.PayOrder;
import com.wireless.db.frontBusiness.UpdateOrder;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.Order.PayType;
import com.wireless.pojo.dishesOrder.Order.SettleType;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.util.Util;

public class UpdateOrderAction2 extends Action{
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		int orderId = 0;
		
		try {
			// 解决后台中文传到前台乱码
			
			out = response.getWriter();
			
			/**
			 * The parameters looks like below.
			 * e.g. pin=0x1 & orderID=40 & category=1 & customNum=2 & payType=1 & 
			 * 		discountType=1 & payManner=1 & serviceRate=0 & 
			 * 		foods={[1102,2,2,4,1]，[1103,2,2,4,0.9]，...}
			 * 
			 * pin : the pin the this terminal
			 * 
			 * orderID : the id to this order
			 * 
			 * category : "1" means "一般"
			 * 			  "2" means "外卖"
			 * 			  "3" means "并台"
			 * 			  "4" means "拼台"
			 * 
			 * customNum : the custom number to this order, ranges from 1 through 255
			 * 
			 * payType : "1" means pay in "一般", 
			 * 			 "2" means pay in "会员" 
			 * 
			 * discountType : "1" means to pay using discount 1
			 * 				  "2" means to pay using discount 2
			 * 				  "3" means to pay using discount 3
			 * 
			 * payManner : "1" means "现金"
			 * 			   "2" means "刷卡"
			 * 			   "3" means "会员卡"
			 * 			   "4" means "挂账"
			 * 			   "5" means "签单"
			 * 
			 * serviceRate : the service percent rate to this order,
			 * 				 this parameter is optional.
			 * 
			 * memberID : the id to member, 
			 * 			  this parameter is optional, only takes effect while the pay type is "会员" 
			 * 
			 * comment : the comment to this order
			 *           this parameter is optional,
			 *           No need to pass this parameter if no comment input. 
			 *           
			 * foods : the food string whose format looks like below.
			 * 			{[是否临时菜(false),菜品1编号,菜品1数量,口味1编号,厨房1编号,菜品1折扣,2nd口味1编号,3rd口味1编号]，
			 * 			 [是否临时菜(false),菜品2编号,菜品2数量,口味2编号,厨房2编号,菜品2折扣,2nd口味1编号,3rd口味1编号]，...
			 * 			 [是否临时菜(true),临时菜1编号,临时菜1名称,临时菜1数量,临时菜1单价]，
			 * 			 [是否临时菜(true),临时菜1编号,临时菜1名称,临时菜1数量,临时菜1单价]...}			 
			 **/
			String pin = (String)request.getAttribute("pin");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin), Privilege.Code.RE_PAYMENT);
			
			//get the id to this order
			orderId = Integer.parseInt(request.getParameter("orderID"));
			Order orderToUpdate = new Order(orderId);
			
			//get the pay type to this order
			SettleType settleType = SettleType.valueOf(Integer.parseInt(request.getParameter("payType")));
			orderToUpdate.setSettleType(settleType);	
			//get the pay manner to this order
			PayType payType = PayType.valueOf(Integer.parseInt(request.getParameter("payManner")));
			orderToUpdate.setPaymentType(payType);

			Order.PayBuilder payBuilder;
			//Get the member id if the pay type is "会员"
			if(settleType == Order.SettleType.MEMBER){
				Member member = MemberDao.getById(staff, Integer.valueOf(request.getParameter("memberID")));
				payBuilder = Order.PayBuilder.build4Member(orderId, member, payType);
			}else{
				payBuilder = Order.PayBuilder.build(orderId, payType);
			}
			
			//get the food string to this order
			orderToUpdate.setOrderFoods(Util.toFoodArray(request.getParameter("foods")));
			
			//get the category to this order
			orderToUpdate.setCategory(Short.parseShort(request.getParameter("category")));
			
			//get the custom number to this order
			orderToUpdate.setCustomNum(Integer.parseInt(request.getParameter("customNum")));
			payBuilder.setCustomNum(Integer.parseInt(request.getParameter("customNum")));
			
			//get the discount type to this order
			payBuilder.setDiscountId(Integer.parseInt(request.getParameter("discountID")));
			
			//get the service rate to this order
			payBuilder.setServiceRate(NumericUtil.int2Float(Integer.parseInt(request.getParameter("serviceRate"))));

			//get the erasePrice rate to this order
			payBuilder.setErasePrice(Integer.valueOf(request.getParameter("erasePrice")));
			
			//get the table alias to this order
			orderToUpdate.getDestTbl().setTableAlias(Integer.valueOf(request.getParameter("tableAlias")));

			//Get the first 20 characters of the comment
			String comment = request.getParameter("comment");
			if(comment != null){
				payBuilder.setComment(comment.substring(0, comment.length() < 20 ? comment.length() : 20));
			}
			
			UpdateOrder.execById(staff, orderToUpdate);
			PayOrder.pay(staff, payBuilder);
			
			jsonResp = jsonResp.replace("$(result)", "true");	
			jsonResp = jsonResp.replace("$(value)", orderId + "号账单修改成功");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");		
			jsonResp = jsonResp.replace("$(value)", e.getDesc());	
			
		}catch(IOException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "账单修改失败，请重新确认");
		}finally{
			out.write(jsonResp);
		}
		
		return null;
		
	}
}

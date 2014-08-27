package com.wireless.Actions.weixin.finance;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.marker.weixin.api.QRCode;
import org.marker.weixin.api.Token;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.staffMgr.Staff;

public class FinanceQRCodeAction extends Action {
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
		final String codeUrl = new QRCode().setSceneId(staff.getRestaurantId()).createUrl(Token.newInstance(FinanceWeixinAction.APP_ID, FinanceWeixinAction.APP_SECRET));
		JObject jObj = new JObject();
		jObj.setExtra(new Jsonable(){

			@Override
			public JsonMap toJsonMap(int flag) {
				JsonMap jm = new JsonMap();
				jm.putString("qrcode_url", codeUrl);
				return jm;
			}

			@Override
			public void fromJsonMap(JsonMap jsonMap, int flag) {
				
			}
		});
		response.getWriter().print(jObj.toString());
		return null;
	}
}

package com.wireless.Actions.dailySettle;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.DailySettleDao;
import com.wireless.db.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.pack.ErrorCode;
import com.wireless.protocol.Terminal;
import com.wireless.util.JObject;

public class DailySettleExecAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();

		PrintWriter out = null;

		JObject jObj = new JObject();		

		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();

			String pin = request.getParameter("pin");

			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);


			DailySettleDao.exec(term);
			

			jObj.initTip(true, term.owner + "日结成功");
			

			dbCon.rs.close();

		} catch (BusinessException e) {
			e.printStackTrace();
			if (e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
				jObj.initTip(false, "没有获取到餐厅信息，请重新确认");
				
			} else if (e.errCode == ErrorCode.TERMINAL_EXPIRED) {
				jObj.initTip(false, "终端已过期，请重新确认");

			} else {
				jObj.initTip(false, "没有获取到信息，请重新确认");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			jObj.initTip(false, "数据库请求发生错误，请确认网络是否连接正常");

		} catch (IOException e) {
			e.printStackTrace();
			jObj.initTip(false, "数据库请求发生错误，请确认网络是否连接正常");

		} finally {
			dbCon.disconnect();

			JSONObject obj = JSONObject.fromObject(jObj);
			
			out.write(obj.toString());
		}

		return null;
	}
}

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
import com.wireless.exception.ProtocolError;
import com.wireless.protocol.Terminal;
import com.wireless.util.JObject;

public class DailySettleCheckAction extends Action {
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

			/**
			 */

			String pin = request.getParameter("pin");

			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);


			int[] restOrderID = DailySettleDao.check(dbCon, term);
			if (restOrderID.length != 0) {
				jObj.initTip(false, "您还有"
										+ restOrderID.length
										+ "张账单未进行交班操作，日结将会有可能导致统计信息不准确，建议先进行交班操作，再执行日结。确定要进行日结吗？");
			} else {
				jObj.initTip(true, "NoUnShift");
			}			

			dbCon.rs.close();

		} catch (BusinessException e) {
			e.printStackTrace();
			if (e.getErrCode() == ProtocolError.TERMINAL_NOT_ATTACHED) {
				jObj.initTip(false, "没有获取到餐厅信息，请重新确认");

			} else if (e.getErrCode() == ProtocolError.TERMINAL_EXPIRED) {
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
			
			// System.out.println(outputJson);

			out.write(JSONObject.fromObject(jObj).toString());
		}

		return null;
	}
}

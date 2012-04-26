package com.wireless.Actions.dailySettle;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.DailySettle;

import com.wireless.db.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Terminal;

public class DailySettleCheckAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();

		PrintWriter out = null;

		List resultList = new ArrayList();
		HashMap rootMap = new HashMap();
		boolean isError = false;

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

			HashMap resultMap = new HashMap();

			int[] restOrderID = DailySettle.check(dbCon, term);
			if (restOrderID.length != 0) {
				resultMap
						.put("text",
								"您还有"
										+ restOrderID.length
										+ "张账单未进行交班操作，日结将会有可能导致统计信息不准确，建议先进行交班操作，再执行日结。确定要进行日结吗？");
				resultMap.put("message", "normal");
			} else {
				resultMap.put("text", "NoUnShift");
				resultMap.put("message", "normal");
			}
			
			resultList.add(resultMap);

			dbCon.rs.close();

		} catch (BusinessException e) {
			e.printStackTrace();
			HashMap resultMap = new HashMap();
			if (e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
				resultMap.put("message", "没有获取到餐厅信息，请重新确认");

			} else if (e.errCode == ErrorCode.TERMINAL_EXPIRED) {
				resultMap.put("message", "终端已过期，请重新确认");

			} else {
				resultMap.put("message", "没有获取到信息，请重新确认");
			}
			resultList.add(resultMap);
			isError = true;
		} catch (SQLException e) {
			e.printStackTrace();
			HashMap resultMap = new HashMap();
			resultMap.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMap);
			isError = true;

		} catch (IOException e) {
			e.printStackTrace();
			HashMap resultMap = new HashMap();
			resultMap.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMap);
			isError = true;

		} finally {
			dbCon.disconnect();

			rootMap.put("root", resultList);

			JsonConfig jsonConfig = new JsonConfig();
			JSONObject obj = JSONObject.fromObject(rootMap, jsonConfig);
			String outputJson = obj.toString();

			// System.out.println(outputJson);

			out.write(outputJson);
		}

		return null;
	}
}

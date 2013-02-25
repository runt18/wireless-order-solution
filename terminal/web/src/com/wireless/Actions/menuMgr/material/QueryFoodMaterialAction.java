package com.wireless.Actions.menuMgr.material;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.pack.ErrorCode;
import com.wireless.pojo.menuMgr.FoodMaterial;
import com.wireless.protocol.Terminal;
import com.wireless.util.JObject;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class QueryFoodMaterialAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();
		PrintWriter out = null;
		
		List resultList = new ArrayList();
		FoodMaterial item = null;
		JObject jobject = new JObject();
		
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();

			/**
			 * The parameters looks like below. 1st example, filter the order
			 * whose id equals 321 pin=0x1 & type=1 & ope=1 & value=321 2nd
			 * example, filter the order date greater than or equal 2011-7-14
			 * 14:30:00 pin=0x1 & type=3 & ope=2 & value=2011-7-14 14:30:00
			 * 
			 * pin : the pin the this terminal type : the type is one of the
			 * values below. 0 - 全部全部 1 - 编号 2 - 名称 3 - 拼音 4 - 价格 5 - 厨房 ope :
			 * the operator is one of the values below. 1 - 等于 2 - 大于等于 3 - 小于等于
			 * value : the value to search, the content is depending on the type
			 * isSpecial : additional condition. isRecommend : additional
			 * condition. isFree : additional condition. isStop : additional
			 * condition.
			 */

			String pin = request.getParameter("pin");

			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF);

			String foodID = request.getParameter("foodID");

			String sql = " SELECT DISTINCT A.material_id, B.material_alias, B.name materialName, A.consumption, B.cate_id, C.name cateName, D.price "
					+ " FROM "
					+ Params.dbName + ".food_material A, "
					+ Params.dbName + ".material B, "
					+ Params.dbName + ".material_cate C, "
					+ Params.dbName + ".material_dept D "
					+ " WHERE A.restaurant_id = "
					+ term.restaurantID
					+ " AND A.restaurant_id = B.restaurant_id"
					+ " AND A.material_id = B.material_id "
					+ " AND B.restaurant_id = C.restaurant_id"
					+ " AND B.cate_id = C.cate_id "
					+ " AND B.restaurant_id = D.restaurant_id"
					+ " AND B.material_id = D.material_id "
					+ " AND A.food_id = " + foodID
					+ " ORDER BY D.price DESC";

			dbCon.rs = dbCon.stmt.executeQuery(sql);

			while (dbCon.rs.next()) {
				item = new FoodMaterial();
				item.setMaterialID(dbCon.rs.getInt("material_id"));
				item.setMaterialAliasID(dbCon.rs.getInt("material_alias"));
				item.setCateID(dbCon.rs.getInt("cate_id"));
				item.setCateName(dbCon.rs.getString("cateName"));
				item.setMaterialName(dbCon.rs.getString("materialName"));
				item.setConsumption(dbCon.rs.getFloat("consumption"));
				item.setPrice(dbCon.rs.getFloat("price"));
				resultList.add(item);
			}

			dbCon.rs.close();

		} catch (BusinessException e) {
			e.printStackTrace();
			jobject.setCode(e.errCode);
			jobject.setSuccess(false);
			if (e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
				jobject.setMsg("没有获取到餐厅信息，请重新确认");
			} else if (e.errCode == ErrorCode.TERMINAL_EXPIRED) {
				jobject.setMsg("终端已过期，请重新确认");
			} else {
				jobject.setMsg("没有获取到信息，请重新确认");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			jobject.setSuccess(false);
			jobject.setMsg("数据库请求发生错误，请确认网络是否连接正常");
		} catch (IOException e) {
			e.printStackTrace();
			jobject.setSuccess(false);
			jobject.setMsg("数据库请求发生错误，请确认网络是否连接正常");
		} finally {
			dbCon.disconnect();
			
			jobject.setTotalProperty(resultList.size());
			jobject.setRoot(resultList);
			
			JSONObject obj = JSONObject.fromObject(jobject);
			
			out.write(obj.toString());

		}
		return null;
	}
}

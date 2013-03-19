package com.wireless.Actions.regionMgr;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.Params;

public class QueryRegionTree2ComboboxAction extends Action {

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setContentType("text/json; charset=utf-8");
		
		PrintWriter out = response.getWriter();
		
		String restaurantID = request.getParameter("restaurantID");
		
		StringBuffer jsonSB = new StringBuffer();
		DBCon dbCon = new DBCon();
		try {
			 String sql ="SELECT restaurant_id, region_id, name FROM "+Params.dbName+".region WHERE restaurant_id = 37 ORDER BY region_id";
			 dbCon.connect();
			 dbCon.rs = dbCon.stmt.executeQuery(sql);
			 int index = 0;
			 while(dbCon.rs !=null && dbCon.rs.next()){
				jsonSB.append(index > 0 ? "," : "");
				jsonSB.append("{");
				jsonSB.append("cID : '"+dbCon.rs.getInt("region_id")+"'");
				jsonSB.append(",");
				jsonSB.append("cNAME : '" + dbCon.rs.getString("name") + "'");
				jsonSB.append("}");
				index++;
			 }
			 dbCon.rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			dbCon.disconnect();
			out.print("["+jsonSB.toString()+"]");
		}

		
		
		
		
		
		// out.print("[{cID : 1,cNAME : '一楼大厅'}," +
		// "{cID : 2,cNAME : '二楼大厅'}," +
		// "{cID : 3,cNAME : '三楼大厅'}," +
		// "{cID : 4,cNAME : '四楼大厅'}," +
		// "{cID : 5,cNAME : '五楼大厅'}," +
		// "{cID : 6,cNAME : '六楼大厅'}," +
		// "{cID : 7,cNAME : '七楼大厅'}," +
		// "{cID : 8,cNAME : '皇家大厅'}," +
		// "{cID : 9,cNAME : '天下客栈'}," +
		// "{cID : 10,cNAME : '外卖区'}," +
		// "{cID : 11,cNAME : '大包间'}," +
		// "{cID : 12,cNAME : '二楼小厅'}," +
		// "{cID : 13,cNAME : '总统套间'}]");
		// out.flush();
		// out.close();
		return null;
	}
}

package com.wireless.Actions.regionMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.json.JsonPackage;
import com.wireless.json.Jsonable;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Test;
import com.wireless.protocol.Terminal;

public class QueryRegionTreeAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		//StringBuffer tree = new StringBuffer();
		String tree="";
		try{
			String pin = request.getParameter("pin");
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			
			List<Region> list = RegionDao.getRegions(term, " AND REGION.restaurant_id = " + term.restaurantID +" ", null);
			//Test t=new Test();
			int t1=2013;
			
			if(!list.isEmpty()){
				/*tree.append("[");
				for(int i = 0; i < list.size(); i++){
					Region temp = list.get(i);
					tree.append(i > 0 ? "," : "");
					tree.append("{");
					tree.append("leaf:" + true);
					tree.append(",");
					tree.append("regionID:" + temp.getRegionId());
					tree.append(",");
					tree.append("regionName:'" + temp.getName() + "'");
					tree.append(",");
					tree.append("restaurantID:" + temp.getRestaurantId());
					tree.append(",");
					tree.append("t1:" +t1);
					tree.append(",");
					tree.append("t2:" +"{'id':1111111111}");
					tree.append(",");
					System.out.println("+----++++++"+temp.getT().getId());
					System.out.println("+----"+new JsonPackage(temp.getT(),temp.REGION_JSONABLE_LEAF, Jsonable.Type.PAIR));
					tree.append("t3:" +new JsonPackage(temp.getT(),temp.REGION_JSONABLE_LEAF, Jsonable.Type.PAIR));
					tree.append(",");
					tree.append("text:'" + temp.getName() + "'");
					tree.append("}");
				
				}
				
				tree.append("]");*/
			
			//tree=new JsonPackage(regions, Region.REGION_JSONABLE_LEAF, Jsonable.Type.PAIR);
			tree = new JsonPackage(list, Region.REGION_JSONABLE_LEAF, Jsonable.Type.PAIR).toString();
			System.out.println(tree);	
			}		
			
			
		}catch(Exception e){
			e.printStackTrace();
			
		}finally{
			response.getWriter().print(tree);
		}
		
/*		tree.append("[{text:'01',children:[{text:'01-01',leaf:true},{text:'nihao',leaf:true}]},{text:'wozhidao',leaf:true,children : [{text:'子子2',leaf:true},{text:'zizi3',leaf:true}]},{text:'again'}]");
		response.getWriter().print(tree.toString());*/
		return null;
	}
}

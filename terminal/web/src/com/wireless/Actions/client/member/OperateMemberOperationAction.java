package com.wireless.Actions.client.member;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.member.MemberOperation;

public class OperateMemberOperationAction extends DispatchAction{

	public ActionForward getMemberOperationType(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jObject = new JObject();
		
		
		
		final List<Jsonable> operations = new ArrayList<>();
		
		for(final MemberOperation.OperationType operate : MemberOperation.OperationType.values()){
			operations.add(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					// TODO Auto-generated method stub
					JsonMap jm = new JsonMap();
					jm.putInt("value", operate.getValue());
					jm.putString("name", operate.toString());
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jm, int flag) {
					// TODO Auto-generated method stub
					
				}
			});
			
		}
		
		jObject.setRoot(new Jsonable(){
			@Override
			public JsonMap toJsonMap(int flag) {
				JsonMap jm = new JsonMap();
				jm.putJsonableList("operateType", operations, 0);
				return jm;
			}

			@Override
			public void fromJsonMap(JsonMap jm, int flag) {
			}
			
		});
		
		response.getWriter().print(jObject.toString());
		
		return null;
	}
	
	
	
}

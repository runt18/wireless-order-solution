package com.wireless.Actions.downloadQrcode;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;

public class DownLoadQrcodeAction extends DispatchAction{
	
	public ActionForward downLoad(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception{
		final String qrCodeUrl = request.getParameter("qrcode");
		final String tableName = request.getParameter("tableName");
		final JObject jObject = new JObject();
		
		
		try{
			if(qrCodeUrl != null && !qrCodeUrl.isEmpty()){
				URL url = new URL(qrCodeUrl);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(5*1000);
				InputStream inStream = conn.getInputStream();
				
			    ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
		        byte[] buffer = new byte[1024];  
		        int len = 0;  
		        while( (len=inStream.read(buffer)) != -1 ){  
		            outStream.write(buffer, 0, len);  
		        }  
		       
				
				final byte[] bytesToQrCode = outStream.toByteArray();
				
				if(bytesToQrCode != null && bytesToQrCode.length > 0){
					String fileName = tableName + ".jpg";
					response.setContentType("application/octet-stream;");
					response.addHeader("Content-Disposition","attachment;filename=" + fileName);
					OutputStream os = response.getOutputStream();
					os.write(bytesToQrCode);
				    inStream.close(); 
				    os.flush();
				    os.close();
				}else{
					throw new BusinessException("没有从该链接获取到内容");
				}
				
			}
			jObject.initTip(true, "修改成功");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
			response.getWriter().print(jObject.toString());
		}
		
		 
		return null;
	}
	
	

}

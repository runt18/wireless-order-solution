package com.wireless.Actions.imagesMgr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.Part;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.oss.OssImageDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.staffMgr.Staff;

public class OperateImageAction extends DispatchAction{

	public ActionForward upload(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String)request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		String ossType = request.getParameter("ossType");
		
		JObject jobject = new JObject();
		
		MultipartParser parser = new MultipartParser(request, (3000 * 1024), true, true);
		
		Part imagePart = parser.readNextPart();
		
		int ossImageId = 0;
		
		try{
			if(imagePart != null) 
	        {
	            if(imagePart.isFile())
	            {
	            	FilePart filePart = (FilePart)imagePart;
	                String fileName = filePart.getFileName();
	                
	                String imageType = fileName.substring(fileName.lastIndexOf(".") + 1);
	                
	            	ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
	        		filePart.writeTo(tempStream);
	        		
	        		InputStream uploadStream = new ByteArrayInputStream(tempStream.toByteArray());
	        		
	    			OssImage.InsertBuilder builder = new OssImage.InsertBuilder(OssImage.Type.valueOf(Integer.parseInt(ossType)))
							 .setImgResource(OssImage.ImageType.valueOf(imageType, 0), uploadStream);
	    			
	    			ossImageId = OssImageDao.insert(staff, builder);    
	    			
	    			OssImage image = OssImageDao.getById(staff, ossImageId);
	    			jobject.setRoot(image);
	    			
	    			jobject.initTip(true, "操作成功, 上传图片信息成功!");
	            }
	        }
			
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{

			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward deleteFoodImg(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String pin = (String)request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		JObject jobject = new JObject();
		
		String foodId = request.getParameter("foodId");
		
		Food.UpdateBuilder builder = new Food.UpdateBuilder(Integer.parseInt(foodId));
		builder.setImage(null);
		
		try{
			FoodDao.update(staff, builder);
			jobject.initTip(true, "图片删除成功");
		}catch(SQLException e){
			jobject.initTip(e);
		}catch(Exception e){
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	

}

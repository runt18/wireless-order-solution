package com.wireless.Actions.couponMgr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.aliyun.common.utils.IOUtils;
import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.Part;
import com.wireless.db.promotion.CouponTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.promotion.CouponType;
import com.wireless.pojo.promotion.CouponType.InsertBuilder;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.OSSUtil;
import com.wireless.util.WebParams;

public class OperateCouponTypeAction extends DispatchAction{

	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String typeName = request.getParameter("typeName");
		String price = request.getParameter("price");
		String date = request.getParameter("date");
		String desc = request.getParameter("desc");
		String image = request.getParameter("image");
		
		String pin = (String) request.getAttribute("pin");
		JObject jobject = new JObject();
		try{
			CouponTypeDao.insert(StaffDao.verify(Integer.parseInt(pin)), new InsertBuilder(typeName, Float.parseFloat(price))
																			.setImage(image)
																			.setExpired(Long.parseLong(date))
																			.setComment(desc));
			jobject.initTip(true, "添加成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String typeId = request.getParameter("typeId");
		String typeName = request.getParameter("typeName");
		String date = request.getParameter("date");
		String desc = request.getParameter("desc");
		String image = request.getParameter("image");
		
		String pin = (String) request.getAttribute("pin");
		JObject jobject = new JObject();
		try{
			CouponTypeDao.update(StaffDao.verify(Integer.parseInt(pin)), new CouponType.UpdateBuilder(Integer.parseInt(typeId), typeName)
																			.setImage(image)
																			.setExpired(Long.parseLong(date))
																			.setComment(desc));
			jobject.initTip(true, "修改成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	public ActionForward delete(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String typeId = request.getParameter("typeId");
		
		String pin = (String) request.getAttribute("pin");
		JObject jobject = new JObject();
		try{
			CouponTypeDao.delete(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(typeId));
			jobject.initTip(true, "删除成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	
	public ActionForward updateCouponImg(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {	
		
		
		JObject jobject = new JObject();
		
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String restaurantID = (String)request.getAttribute("restaurantID");
			String couponTypeId = request.getParameter("couponTypeId");
			
			if(couponTypeId != null && !couponTypeId.isEmpty()){
				CouponType coupon = CouponTypeDao.getById(staff, Integer.parseInt(couponTypeId));
				
				// 获取菜品原图信息,用于更新图片成功之后删除原文件,否则保留原文件
				if(coupon.getImage() != null && !coupon.getImage().isEmpty())
					OSSUtil.deleteImage(restaurantID + "/" + coupon.getImage());
			}

			
				
				// 上传图片最大尺寸,单位:KB
				String imgMaxSizeParame = this.getServlet().getInitParameter(WebParams.IMAGE_UPLOAD_MAX_SIZE);
				int imgMaxSize = imgMaxSizeParame != null && !imgMaxSizeParame.trim().isEmpty() ? Integer.valueOf(imgMaxSizeParame) : WebParams.IMAGE_UPLOAD_MAX_SIZE_DEFAULT;
				String[] imgType = this.getServlet().getInitParameter(WebParams.IMAGE_UPLOAD_TYPE).split(",");
				String encoding = "UTF-8";
				
				MultipartParser parser = null;
				try{
					parser = new MultipartParser(request, (imgMaxSize * 1024), true, true, encoding);
				}catch(Exception e){
					jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9995, "操作失败, 请检查图片大小是否超过<" + imgMaxSize + "KB>或图片内容是否可用.");
					return null;
				}
				Part imgaePart;
				String imagePath = null;
				while((imgaePart = parser.readNextPart()) != null) 
		        {
		            if(imgaePart.isFile())
		            {
		                FilePart filePart = (FilePart)imgaePart;
		                String fileName = filePart.getFileName();
		                if(fileName != null)
		                {
		                	// 过滤图片类型
		                	int index = fileName.lastIndexOf(".");
		                	String type = fileName.substring(index + 1, fileName.length());
		                	boolean cs = false;
		                	for(String temp : imgType){
		                		if(temp.toLowerCase().equals(type.toLowerCase())){
		                			cs = true;
		                			break;
		                		}
		                	}
		                	if(!cs){
		                		jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9994, "操作失败, 不支持该类型图片!");
		                		return null;
		                	}
		                	
		                	// 生成新文件名
		                	String date = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
		                	String newFileName = "";
		                	
		                	newFileName = "coupon" + date;   // 新文件前缀部分,需要格式化
		                	
		                	MessageDigest md = MessageDigest.getInstance("MD5");
		                	md.update(newFileName.getBytes(encoding));
		                	
		                	StringBuilder sb = new StringBuilder();
		                	byte[] byteArray = md.digest();
		                	int bi = 0;
		                	for(int offset = 0; offset < byteArray.length; offset++){
		                		bi = byteArray[offset];
		                		if(bi < 0)
		                			bi += 256;
		                		if(bi < 16)
		                			sb.append("0");
		                		sb.append(Integer.toHexString(bi));
		                	}
		                	
		                	newFileName = sb.toString().substring(8,24) + "." + type.toLowerCase();  // 组合新文件名,统一文件后缀为小写
		                	
		                	// 生成新图片相关信息
		                	ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
		                	InputStream uploadStream = null;
		                	try{
		                		// 还原原始图片
		                		filePart.writeTo(tempStream);
		                		uploadStream = new ByteArrayInputStream(tempStream.toByteArray());
		                		jobject.initTip(true, "操作成功, 读取上传图片信息成功!");
		                		// 原始图片
		                		OSSUtil.uploadImage(uploadStream, restaurantID + "/" + newFileName);
		                	}catch(Exception e){
		                		jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9993, "操作失败, 读取上传图片信息失败, 请联系客服人员!");
		                		e.printStackTrace();
		                		return null;
		                	}finally{
		                		IOUtils.safeClose(tempStream);
		                		IOUtils.safeClose(uploadStream);
		                	}
		    				
		                	imagePath = newFileName;
		                }
		            }
		        }
			final String image = imagePath;
			jobject.initTip(true, WebParams.TIP_TITLE_DEFAULT, 1111, "操作成功, 上传图片信息成功!");
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("imagePath", image);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(IOException e){	
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9998, "操作失败, 服务器未能处理图片信息, 请联系客服人员!");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 更新图片信息失败, 请联系客服人员!");
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}

}

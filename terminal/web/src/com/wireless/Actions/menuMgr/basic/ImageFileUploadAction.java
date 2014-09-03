package com.wireless.Actions.menuMgr.basic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.aliyun.common.utils.IOUtils;
import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.Part;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.CompressImage;
import com.wireless.util.OSSUtil;
import com.wireless.util.WebParams;

public class ImageFileUploadAction extends Action{
	
	public static final String CI_PRIEX = "small_";
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {	
		
		
		JObject jobject = new JObject();
		
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String restaurantID = (String) request.getAttribute("restaurantID");
			String foodID = request.getParameter("foodID");
			String otype = request.getParameter("otype");
			
			if(restaurantID == null){
				restaurantID = (String)request.getAttribute("restaurantID");
			}
			
			try{
				Integer.parseInt(restaurantID);
				Integer.parseInt(foodID);
			}catch(Exception e){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9997, "操作失败, 获取餐厅信息或菜品信息失败.");
				return null;
			}
			
			try{
				Integer.parseInt(otype);
			}catch(Exception e){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9996, "操作失败, 未设置图片操作类型.");
				return null;
			}
			List<Food> root = new ArrayList<Food>();
			
			Food fb = FoodDao.getPureById(staff, Integer.parseInt(foodID));
			
			// 获取菜品原图信息,用于更新图片成功之后删除原文件,否则保留原文件
			String oldName = fb.getImage();
			
			// 删除图片
			if(Integer.parseInt(otype) == 1){
				fb.setImage(this.getServlet().getInitParameter(WebParams.IMAGE_BROWSE_DEFAULT_FILE));
				root.clear();
				root.add(fb);
            	
				FoodDao.update(staff, new Food.UpdateBuilder(Integer.parseInt(foodID)).setImage(null));
            	// 删除原始图片
            	OSSUtil.deleteImage(restaurantID + "/" + oldName);
            	// 删除原始缩略图
            	OSSUtil.deleteImage(restaurantID + "/" + CI_PRIEX + oldName);
            	
			}else if(Integer.parseInt(otype) == 0){
				
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
		                	
		                	newFileName = foodID + date;   // 新文件前缀部分,需要格式化
		                	
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
		                		// 缩略图
		                		CompressImage ci = new CompressImage();
		                		uploadStream = ci.imageZoomOut(new ByteArrayInputStream(tempStream.toByteArray()), 500, 400);
		                		OSSUtil.uploadImage(uploadStream, restaurantID + "/" + CI_PRIEX + newFileName);
		                	}catch(Exception e){
		                		jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9993, "操作失败, 读取上传图片信息失败, 请联系客服人员!");
		                		e.printStackTrace();
		                		return null;
		                	}finally{
		                		IOUtils.safeClose(tempStream);
		                		IOUtils.safeClose(uploadStream);
		                	}
		                	
		                    // 更新菜品数据库信息 
		                    try{
		                    	FoodDao.update(staff, new Food.UpdateBuilder(Integer.parseInt(foodID)).setImage(newFileName));
		                    	// 删除原图片
			        			OSSUtil.deleteImage(restaurantID + "/" + oldName);
			        			OSSUtil.deleteImage(restaurantID + "/" + CI_PRIEX + oldName);
		                    }catch(Exception e){
		                    	jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9991, "操作失败, 更新编号为 " + foodID + " 的菜品图片信息失败!");
		                    	e.printStackTrace();
		                    	return null;
		                    }
		                    
		                    fb.setImage("http://" + getServlet().getInitParameter("oss_bucket_image")
		                    		+ "." + getServlet().getInitParameter("oss_outer_point") 
		                    		+ "/" + fb.getRestaurantId() 
		                    		+ "/" + newFileName);
		                    root.clear();
		    				root.add(fb);
		                }else{
		                	fb.setImage(this.getServlet().getInitParameter("imageBrowseDefaultFile"));
		                	root.clear();
		    				root.add(fb);
		                	
		    				FoodDao.update(staff, new Food.UpdateBuilder(Integer.parseInt(foodID)).setImage(null));
		                	OSSUtil.deleteImage(restaurantID + "/" + oldName);
		                	OSSUtil.deleteImage(restaurantID + "/" + CI_PRIEX + oldName);
		                }
		            }
		        }
			}
			
			jobject.initTip(true, WebParams.TIP_TITLE_DEFAULT, 1111, "操作成功, 已更新菜品图片信息成功!");
			jobject.setRoot(root);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(IOException e){	
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9998, "操作失败, 服务器未能处理图片信息, 请联系客服人员!");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 更新菜品图片信息失败, 请联系客服人员!");
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
}

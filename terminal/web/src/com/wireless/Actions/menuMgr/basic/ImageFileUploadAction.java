package com.wireless.Actions.menuMgr.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.Part;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.protocol.Food;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

@SuppressWarnings({"unchecked"})
public class ImageFileUploadAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {	
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		
		try{
//			System.out.println("--------------------------------------------------");
			
			String restaurantID = request.getParameter("restaurantID");
			String foodID = request.getParameter("foodID");
			String otype = request.getParameter("otype");
			
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
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9996, "操作失败, 获取图片操作类型失败.");
				return null;
			}
			
			Food fb = new Food();
			fb.setRestaurantId(Integer.parseInt(restaurantID));
			fb.setFoodId(Integer.parseInt(foodID));
			
			// 获取菜品原图信息,用于更新图片成功之后删除原文件,否则保留原文件
			fb = FoodDao.getFoodBasicImage(fb);
			String oldName = fb.getImage();
			
			// 获取图片操作路径(物理路径)
			String imageUploadPath = this.getServlet().getInitParameter(WebParams.IMAGE_UPLOAD_PATH);
			
			// 删除图片
			if(Integer.parseInt(otype) == 1){
				
				fb.setImage(this.getServlet().getInitParameter(WebParams.IMAGE_BROWSE_DEFAULT_FILE));
                jobject.getRoot().add(fb);
            	
            	FoodDao.updateFoodImageName(Integer.parseInt(restaurantID), Integer.parseInt(foodID), null);
            	deleteImage(imageUploadPath + File.separator + restaurantID + File.separator + oldName);
            	
			}else if(Integer.parseInt(otype) == 0){
				
				File actualImageFile = new File(imageUploadPath + File.separator + restaurantID);
				if(!actualImageFile.exists()){
					// 创建存放图片的新路径(物理路径)
					if(!actualImageFile.mkdirs()){
						jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9999, "操作失败, 生成文件上传路径失败.");
						return null;
					}
//					System.out.println("操作成功,已自动创建生产环境新文件夹.");
//					System.out.println("生产环境文件夹路径: " + actualImageFile);
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
				Part part1;
				while((part1 = parser.readNextPart()) != null) 
		        {
		            if(part1.isFile())
		            {
		                FilePart filePart = (FilePart)part1;
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
		                	
		                	StringBuffer sb = new StringBuffer();
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
		                	String op = actualImageFile.getPath() + File.separator + fileName;
		                	String ap = actualImageFile.getPath() + File.separator + newFileName;
		                	
		                	try{
		                		// 还原原始图片
		                		filePart.writeTo(actualImageFile);
		                		jobject.initTip(true, "操作成功, 读取上传图片信息成功!");
		                	}catch(Exception e){
		                		jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9993, "操作失败, 读取上传图片信息失败, 请联系客服人员!");
		                		deleteImage(op);
		                		e.printStackTrace();
		                		return null;
		                	}
		                    try{
		                    	copyImage(op, ap);
		                    }catch(Exception e){
		                    	jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9992, "操作失败, 复制上传文件至生产环境目录失败, 请联系客服人员!");
		                    	deleteImage(op);
		                    	deleteImage(ap);
		                    	e.printStackTrace();
		                    	return null;
		                    }	                  
		                    
		                    // 删除原始上传文件
		                    actualImageFile = new File(op);
		                    actualImageFile.delete();
		                    
		                    // 更新菜品数据库信息 
		                    try{
		                    	FoodDao.updateFoodImageName(Integer.parseInt(restaurantID), Integer.parseInt(foodID), newFileName);
		                    }catch(Exception e){
		                    	jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9991, "操作失败, 更新编号为 " + foodID + " 的菜品图片信息失败!");
		                    	deleteImage(op);
		                    	deleteImage(ap);
		                    	e.printStackTrace();
		                    	return null;
		                    }
		                    fb.setImage(this.getServlet().getInitParameter("imageBrowsePath") + "/" + fb.getRestaurantId() + "/" + newFileName);
		                    jobject.getRoot().add(fb);
		                    
		                    // 新文件操作成功后删除原图片
		        			deleteImage(imageUploadPath + File.separator + restaurantID + File.separator + oldName);
		                    
		                }else{
		                	fb.setImage(this.getServlet().getInitParameter("imageBrowseDefaultFile"));
			                jobject.getRoot().add(fb);
		                	
		                	FoodDao.updateFoodImageName(Integer.parseInt(restaurantID), Integer.parseInt(foodID), null);
		                	deleteImage(imageUploadPath + File.separator + restaurantID + File.separator + oldName);
		                }
		            }
		        }
				
			}
			
			jobject.initTip(true, WebParams.TIP_TITLE_DEFAULT, 1111, "操作成功, 已更新菜品图片信息成功!");
			
		}catch(IOException e){	
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9998, "操作失败, 服务器未能处理图片信息, 请联系客服人员!");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 更新菜品图片信息失败, 请联系客服人员!");
		}finally{
			JSONObject josn = JSONObject.fromObject(jobject);
			response.getWriter().print(josn.toString());
		}
		
		return null;
	}
	
	/**
	 * operation
	 * @param op
	 * @param np
	 * @throws Exception
	 */
	private void copyImage(String op, String np) throws Exception{
		if(op == null || np == null || op.trim().length() == 0 || np.trim().length() == 0)
			throw new Exception();
		
		FileInputStream inStream = null;
        FileOutputStream outStream = null;
        
    	try{
    		inStream = new FileInputStream(op);
    		outStream = new FileOutputStream(np);
    		int byteread = 0;
            byte[] buffer = new byte[8192];
        	while ((byteread = inStream.read(buffer)) != -1){  
        		outStream.write(buffer,  0,  byteread);  
        	}
    	}catch(Exception e){
    		throw e;
    	}finally{
    		if(outStream != null){
        		outStream.flush();
        		outStream.close();	                    		
        	}
        	if(inStream != null)
        		inStream.close();
    	}
	}
	
	/**
	 * 
	 * @param path
	 */
	private void deleteImage(String path){
		File temp = new File(path);
		if(temp.exists()){
			temp.delete();
		}
	}
}

package com.wireless.Actions.weixin.operate;

import org.apache.struts.actions.DispatchAction;

public class WXOperateMaterialAction extends DispatchAction {
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
/*	public ActionForward upload(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			int rid = Integer.valueOf(request.getAttribute("restaurantID").toString());
			MultipartParser parser = null;
			try{
				parser = new MultipartParser(request, (100 * 1024), true, true);
			}catch(Exception e){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9995, "操作失败, 请检查图片大小是否超过<" + 100 + "KB>.");
				return null;
			}
			Part imgaePart;
			String key = "";
			while((imgaePart = parser.readNextPart()) != null) 
	        {
	            if(imgaePart.isFile())
	            {
	            	FilePart filePart = (FilePart)imgaePart;
	                String fileName = filePart.getFileName();
	                if(fileName != null){
		            	String date = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
		            	key = "WXMaterial/" + rid + "/" + date + (int)(Math.random() * 1000) + "." + fileName.substring(fileName.lastIndexOf(".") + 1);
		            	
		            	ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
	                	InputStream uploadStream = null;
	                	
	                	try{
	                		// 还原原始图片
	                		filePart.writeTo(tempStream);
	                		uploadStream = new ByteArrayInputStream(tempStream.toByteArray());
	                		jobject.initTip(true, "操作成功, 读取上传图片信息成功!");
	                		
	                		// 记录图片素材信息
	                		//WeixinRestaurantDao.addImageMaterial(Integer.valueOf(rid), key);
	                		
	                		// 原始图片
	                		OSSUtil.uploadImage(uploadStream, key);
	                		jobject.initTip(true, "操作成功, 上传图片信息成功!");
	                		final String url = "http://" + getServlet().getInitParameter("oss_bucket_image") +
	                						   "." + getServlet().getInitParameter("oss_outer_point") + "/" + key;
	                		jobject.setExtra(new Jsonable(){

								@Override
								public JsonMap toJsonMap(int flag) {
									JsonMap jm = new JsonMap();
									jm.putString("url", url);
									return jm;
								}

								@Override
								public void fromJsonMap(JsonMap jsonMap, int flag) {
									
								}
	                			
	                		});
	                	}catch(Exception e){
	                		jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9993, "操作失败, 读取上传图片信息失败, 请联系客服人员!");
	                		e.printStackTrace();
	                		return null;
	                	}finally{
	                		IOUtils.safeClose(tempStream);
	                		IOUtils.safeClose(uploadStream);
	                	}
	                }
	            }
	        }
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	*//**
	 * 上传logo
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 *//*
	public ActionForward updateLogo(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			int rid = Integer.valueOf(request.getAttribute("restaurantID").toString());
			String oldImg = WeixinRestaurantDao.get(StaffDao.getAdminByRestaurant(rid)).getWeixinInfo();
			MultipartParser parser = null;
			try{
				parser = new MultipartParser(request, (100 * 1024), true, true);
			}catch(Exception e){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9995, "操作失败, 请检查图片大小是否超过<" + 100 + "KB>.");
				return null;
			}
			Part imgaePart;
			String key = "";
			while((imgaePart = parser.readNextPart()) != null) 
	        {
	            if(imgaePart.isFile())
	            {
	            	FilePart filePart = (FilePart)imgaePart;
	                String fileName = filePart.getFileName();
	                if(fileName != null){
		            	String date = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
		            	key = "WXMaterial/" + rid + "/" + date + (int)(Math.random() * 1000) + "." + fileName.substring(fileName.lastIndexOf(".") + 1);
		            	
		            	ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
	                	InputStream uploadStream = null;
	                	
	                	try{
	                		// 还原原始图片
	                		filePart.writeTo(tempStream);
	                		uploadStream = new ByteArrayInputStream(tempStream.toByteArray());
	                		jobject.initTip(true, "操作成功, 读取上传图片信息成功!");

	                		
	                		// 记录图片素材信息
	                		WeixinRestaurantDao.update(StaffDao.getAdminByRestaurant(rid), new WeixinRestaurant.UpdateBuilder().setWeixinLogo(key));
	                		
	                		// 原始图片
	                		OSSUtil.uploadImage(uploadStream, key);
	                		jobject.initTip(true, "操作成功, 上传图片信息成功!");
	                		final String url = "http://" + getServlet().getInitParameter("oss_bucket_image") +
	                						   "." + getServlet().getInitParameter("oss_outer_point") + "/" + key;
	                		jobject.setExtra(new Jsonable(){

								@Override
								public JsonMap toJsonMap(int flag) {
									JsonMap jm = new JsonMap();
									jm.putString("url", url);
									return jm;
								}

								@Override
								public void fromJsonMap(JsonMap jsonMap, int flag) {
									
								}
	                			
	                		});
	                		
	                		// 更新成功后删除原图片
	                		OSSUtil.deleteImage(oldImg);
	                	}catch(Exception e){
	                		jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9993, "操作失败, 读取上传图片信息失败, 请联系客服人员!");
	                		e.printStackTrace();
	                		return null;
	                	}finally{
	                		IOUtils.safeClose(tempStream);
	                		IOUtils.safeClose(uploadStream);
	                	}
	                }
	            }
	        }
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}*/
	
	
}

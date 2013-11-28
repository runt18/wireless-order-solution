package com.wireless.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.aliyun.common.utils.IOUtils;
import com.aliyun.openservices.ClientException;
import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.OSSException;
import com.aliyun.openservices.oss.internal.OSSUtils;
import com.aliyun.openservices.oss.model.CannedAccessControlList;
import com.aliyun.openservices.oss.model.GetObjectRequest;
import com.aliyun.openservices.oss.model.ListObjectsRequest;
import com.aliyun.openservices.oss.model.OSSObjectSummary;
import com.aliyun.openservices.oss.model.ObjectMetadata;

public class OSSUtil {
	
	private OSSUtil(){}
	
    public static String BUCKET_IMAGE;
    private static OSSClient imgClientInner;
//    private static OSSClient imgClientOuter;
    private static ListObjectsRequest imgListRequest;
    
    private static OSSClient clientInner;
    
    /**
     * 初始化客户端连接池基础信息
     * @param id
     * @param key
     * @param bucketImage
     */
    public static void init(OSSParams params, String bucketImage){
    	BUCKET_IMAGE = bucketImage;
    	clientInner = new OSSClient("http://" + params.OSS_INNER_POINT, params.ACCESS_OSS_ID, params.ACCESS_OSS_KEY);
    	System.out.println("信息: 其他文件处理客户端内网连接池初始化成功.");
    	imgClientInner = new OSSClient("http://" + params.OSS_INNER_POINT, params.ACCESS_OSS_ID, params.ACCESS_OSS_KEY);
    	System.out.println("信息: 图片处理客户端内网连接池初始化成功.");
    	ensureBucketImage();
    	System.out.println("信息: BUCKET_IMAGE 初始化成功.");
    }
    
    /**
     * 
     * @param bucketName
     * @throws OSSException
     * @throws ClientException
     */
	public static void ensureBucket(String bucketName) throws OSSException, ClientException{
    	ensureBucket(clientInner, bucketName);
    }
	
	/**
     * 创建自定义 bucket
     * @param bucketName
     * @throws OSSException
     * @throws ClientException
     */
	public static void ensureBucket(OSSClient client, String bucketName) throws OSSException, ClientException{
    	if (!client.doesBucketExist(bucketName)){
    		client.createBucket(bucketName);
    		client.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
		}
    }
    
    /**
     * 
     * @throws OSSException
     * @throws ClientException
     */
	private static void ensureBucketImage() throws OSSException, ClientException{
		if(imgClientInner == null)
			throw new NullPointerException("错误: 未初始化客户端内网连接池.");
		ensureBucket(imgClientInner, BUCKET_IMAGE);
		imgClientInner.setBucketAcl(BUCKET_IMAGE, CannedAccessControlList.PublicRead);
		imgListRequest = new ListObjectsRequest(BUCKET_IMAGE);
		imgListRequest.setMaxKeys(500);
	}
	
	/**
     * 设置上传文件类型
     * @param om
     * @param key
     * @return
     */
    static ObjectMetadata checkContentType(ObjectMetadata om, String key){
    	if(om == null)
    		om = new ObjectMetadata();
    	if(key.toLowerCase().endsWith(".jpg")){
			om.setContentType("image/jpeg");					
		}else if(key.toLowerCase().endsWith(".gif")){
			om.setContentType("image/gif");
		}else if(key.toLowerCase().endsWith(".png")){
			om.setContentType("image/png");
		}
    	return om;
    }
    
	/**
	 * 
	 * @param client
	 * @param bucketName
	 * @param key
	 * @param file
	 * @param objectMeta
	 * @throws OSSException
	 * @throws ClientException
	 * @throws NullPointerException
	 * @throws IOException 
	 */
    public static void uploadFile(OSSClient client, String bucketName, String key, File file, ObjectMetadata objectMeta) 
    		throws OSSException, ClientException, NullPointerException, IOException{
    	if(file != null && file.exists()){
    		ensureBucket(client, bucketName);
    		if(objectMeta == null)
    			objectMeta = new ObjectMetadata();
    		objectMeta.setContentLength(file.length());
    		InputStream inputStream = new FileInputStream(file);
    		client.putObject(bucketName, key, inputStream, objectMeta);
    		IOUtils.safeClose(inputStream);
    	}else{
    		throw new NullPointerException("错误: 上传文件不能为空.");
    	}
    }
    
    public static void uploadFile(OSSClient client, String bucketName, String key, String path) 
    		throws OSSException, ClientException, NullPointerException, IOException{
    	uploadFile(client, bucketName, key, new File(path), null);
    }
    
    public static void uploadFile(String bucketName, String key, File file, ObjectMetadata objectMeta) 
    		throws OSSException, ClientException, NullPointerException, IOException{
    	uploadFile(clientInner, bucketName, key, file, objectMeta);
    }
    
    public static void uploadFile(String bucketName, String key, String path) 
    		throws OSSException, ClientException, NullPointerException, IOException{
    	uploadFile(clientInner, bucketName, key, new File(path), null);
    }
    
    public static void upload(InputStream is, String bucketName, String key) throws IOException{
    	ObjectMetadata objectMeta = new ObjectMetadata();
    	objectMeta.setContentLength(is.available());
    	ensureBucket(clientInner, bucketName);
    	clientInner.putObject(bucketName, key, is, objectMeta);
    }
    
    /**
     * 上传图片
     * @param file	图片文件
     * @throws OSSException
     * @throws ClientException
     * @throws IOException 
     */
    public static void uploadImage(File file, String key) 
    		throws OSSException, ClientException, IOException{
		if(file != null && file.exists()){
			if(file.isFile()){
				ObjectMetadata objectMeta = new ObjectMetadata();
				checkContentType(objectMeta, file.getName());
				objectMeta.setContentLength(file.length());
				uploadFile(imgClientInner, BUCKET_IMAGE, key != null && !key.trim().isEmpty() ? key : file.getName(), file, objectMeta);
			}
		}
	}
    public static void uploadImage(String path) 
    		throws OSSException, ClientException, IOException{
    	uploadImage(path, null);
    }
    public static void uploadImage(String path, String key) 
    		throws OSSException, ClientException, IOException{
    	uploadImage(new File(path), key);
	}
    
    /**
     * 上传图片
     * @param fis	图片文件流
     * @param key	图片名称
     * @throws OSSException
     * @throws ClientException
     * @throws IOException
     */
    public static void uploadImage(InputStream fis, String key) 
    		throws OSSException, ClientException, IOException{
    	ObjectMetadata objectMeta = new ObjectMetadata();
    	objectMeta.setContentLength(fis.available());
		checkContentType(objectMeta, key);
    	imgClientInner.putObject(BUCKET_IMAGE, key, fis, objectMeta);
	}
	
    /**
     * 删除图片
     * @param key
     */
    public static void deleteImage(String key) throws OSSException, ClientException{
    	imgClientInner.deleteObject(BUCKET_IMAGE, key);
    }
    
    /**
     * 删除 BUCKET_IMAGE
     * @throws Exception
     */
    public static void deleteBucketToImage() throws Exception{
    	deleteBucket(imgClientInner, BUCKET_IMAGE);
    }
    
    /**
     * 删除指定 Bucket
     * @param client
     * @param bucketName
     * @throws Exception
     */
    public static void deleteBucket(OSSClient client, String bucketName) throws Exception{
    	if(client.doesBucketExist(bucketName)){
    		ListObjectsRequest lor = new ListObjectsRequest(bucketName);
    		lor.setMaxKeys(500);
    		List<OSSObjectSummary> listDeletes = client.listObjects(lor).getObjectSummaries();
    		if(listDeletes.size() > 0){
    			int success = 0;
    			for (int i = 0; i < listDeletes.size(); i++) {
    				String objectName = listDeletes.get(i).getKey();
    				try{
    					client.deleteObject(bucketName, objectName);
    					System.out.println("删除文件成功: " + objectName);
    					success++;
    				}catch(Exception e){
    					System.out.println("删除文件失败: " + objectName);
    				}
    			}
    			System.out.println("bucket<" + bucketName + "> 删除文件成功数: " + success);
    		}
    		if(client.listObjects(lor).getObjectSummaries().size() > 0){
    			deleteBucket(client, bucketName);
    		}else{
    			client.deleteBucket(bucketName);
    			System.out.println("bucket<" + bucketName + "> 删除成功.");
    		}
    	}
    }
    
    /**
     * 获取图片流
     * @param key
     * @return
     * 	返回图片流信息, 使用后需要手动清理
     * @throws Exception
     */
    public static InputStream getImage(String key) throws Exception{
    	return imgClientInner.getObject(new GetObjectRequest(BUCKET_IMAGE, key)).getObjectContent();
    }
    
    public static ByteArrayOutputStream changeStreamToOut(InputStream in) throws IOException{
    	BufferedInputStream temp = new BufferedInputStream(in);
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
        int ch;
        while ((ch = temp.read()) != -1) {   
        	out.write(ch);   
        }
        return out;
    }
    
    /**
     * 
     * @param key
     * @return
     * @throws Exception
     */
    public static OutputStream getImageToOutputStream(String key) throws Exception{
    	return changeStreamToOut(getImage(key));
    }
    
    /**
     * 下载图片
     * @param key
     * 	OSS文件名
     * @param keyAlias
     * 	下载文件别名,可以为空,默认为OSS文件名
     * @param filePath
     * 	存在路径,可以为空,默认保存至项目根目录下 aliyunDownload 文件夹
     * @throws Exception
     */
    public static void downloadImage(String key, String keyAlias, String filePath) throws Exception{
    	OutputStream outputStream = null;
        InputStream inputStream = null;
    	try{
    		inputStream = getImage(key);
    		File file = filePath == null || filePath.trim().isEmpty() ? new File("." + File.separator + "aliyunDownload") : new File(filePath);
    		if(!file.exists()){
    			if(file.mkdirs()){
    				System.out.println("下载文件存放路径创建成功: " + file.getAbsolutePath());
    			}
    		}
    		file = new File(file.getAbsoluteFile() + File.separator + (keyAlias == null || keyAlias.trim().isEmpty() ? key : keyAlias));
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
            int bufSize = 1024;
            byte buffer[] = new byte[bufSize];
            int bytesRead;
            while((bytesRead = inputStream.read(buffer)) > -1) 
                outputStream.write(buffer, 0, bytesRead);
            System.out.println("下载成功, 文件: " + key);
        }catch(IOException e){
            throw new ClientException(OSSUtils.OSS_RESOURCE_MANAGER.getString("CannotReadContentStream"), e);
        }catch(Exception e){
        	throw new Exception("下载文件失败, <" + key + "> 该文件不存在或已删除.");
        }finally{
        	IOUtils.safeClose(outputStream);
        	IOUtils.safeClose(inputStream);
        }
    }
    
    /**
     * 下载图片
     * @param key OSS文件名
     * @throws Exception
     */
    public static void downloadImage(String key) throws Exception{
    	downloadImage(key, null, null);
    }
    
}

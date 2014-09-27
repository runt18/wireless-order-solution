package com.wireless.db.oss;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.model.CannedAccessControlList;
import com.aliyun.openservices.oss.model.ObjectMetadata;
import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.OssImageError;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.staffMgr.Staff;

public class OssImageDao {

	public static class ExtraCond{
		private int id;
		private int associatedId;
		private String associatedSerial;
		private OssImage.Type type;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setAssociated(OssImage.Type type, int associatedId){
			this.type = type;
			this.associatedId = associatedId;
			this.associatedSerial = null;
			return this;
		}
		
		public ExtraCond setAssociated(OssImage.Type type, String associatedSerial){
			this.type = type;
			this.associatedId = 0;
			this.associatedSerial = associatedSerial;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND oss_image_id = " + id);
			}
			if(associatedId != 0){
				extraCond.append(" AND associated_id = " + associatedId);
			}
			if(associatedSerial != null){
				extraCond.append(" AND associated_serial = '" + associatedSerial + "' AND associated_serial_crc = CRC32('" + associatedSerial + "')");
			}
			if(type != null){
				extraCond.append(" AND type = " + type.getVal());
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Insert a new image according to specific builder {@link OssImage#InsertBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder {@link OssImage#InsertBuilder}
	 * @return the id to oss image just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws IOException
	 * 			throws if the image failed to upload to oss storage
	 * @throws BusinessException
	 * 			throws if the image resource does NOT exist
	 */
	public static int insert(Staff staff, OssImage.InsertBuilder builder) throws SQLException, IOException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new image according to specific builder {@link OssImage#InsertBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder {@link OssImage#InsertBuilder}
	 * @return the id to oss image just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws IOException
	 * 			throws if the image failed to upload to oss storage
	 * @throws BusinessException
	 * 			throws if the image resource does NOT exist
	 */
	public static int insert(DBCon dbCon, Staff staff, OssImage.InsertBuilder builder) throws SQLException, IOException, BusinessException{
		if(builder.hasImgResource()){
			
			OssImage ossImage = builder.build();
			
	    	//Upload the image.
	    	upload(staff, ossImage, builder.getImgType(), builder.getImgStream());
			
			String sql;
			sql = " INSERT INTO " + Params.dbName + ".oss_image " +
				  " (restaurant_id, image, image_crc, type, associated_id, associated_serial, associated_serial_crc, status, last_modified) " +
				  " VALUES (" +
				  staff.getRestaurantId() + "," +
				  "'" + ossImage.getImage() + "'," +
				  "CRC32('" + ossImage.getImage() + "')," +
				  ossImage.getType().getVal() + "," +
				  ossImage.getAssociatedId() + "," +
				  "'" + ossImage.getAssociatedSerial() + "'," +
				  "CRC32('" + ossImage.getAssociatedSerial() + "')," +
				  ossImage.getStatus().getVal() + "," +
				  " NOW() " +
				  ")";
			
			dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			dbCon.rs = dbCon.stmt.getGeneratedKeys();
			int id;
			if(dbCon.rs.next()){
				id = dbCon.rs.getInt(1);
			}else{
				throw new SQLException("Failed to generated the oss image id.");
			}
			return id;
		}else{
			throw new BusinessException(OssImageError.OSS_IMAGE_RESOURCE_NOT_EXIST);
		}
	}
	
	/**
	 * Update the oss image.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the oss update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the oss image to update does NOT exist
	 * @throws IOException
	 * 			throws if failed to put image to oss storage
	 */
	public static void update(Staff staff, OssImage.UpdateBuilder builder) throws SQLException, BusinessException, IOException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			update(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(SQLException | BusinessException | IOException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the oss image.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the oss update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the oss image to update does NOT exist
	 * @throws IOException
	 * 			throws if failed to put image to oss storage
	 */
	public static void update(DBCon dbCon, Staff staff, OssImage.UpdateBuilder builder) throws SQLException, BusinessException, IOException{
		OssImage ossImage = builder.build();

		OssImage oriImage = getById(dbCon, staff, ossImage.getId());

		String sql;
		sql = " UPDATE " + Params.dbName + ".oss_image SET " +
			  " oss_image_id = " + ossImage.getId() +
			  (builder.isAssociatedChanged() ? " ,type = " + ossImage.getType().getVal() +
					  						   " ,associated_id = " + ossImage.getAssociatedId() +
					  						   " ,associated_serial = '" + ossImage.getAssociatedSerial() + "'" +
					  						   " ,associated_serial_crc = CRC32('" + ossImage.getAssociatedSerial() + "')" : "") +
					  						   " ,status = " + ossImage.getStatus().getVal() + 
			  (builder.isImageNameChanged() ? " ,image = '" + ossImage.getImage() + "' ,image_crc = CRC32('" + ossImage.getImage() + "')" : "") +
			  " ,last_modified = NOW() " +
			  " WHERE oss_image_id = " + ossImage.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(OssImageError.OSS_IMAGE_NOT_EXIST);
		}
		
		if(builder.isImgResourceChanged()){
			//Delete the original image from oss storage.
			OSSClient ossClient = new OSSClient("http://" + OssImage.Params.instance().getOssParam().OSS_INNER_POINT, 
					    OssImage.Params.instance().getOssParam().ACCESS_OSS_ID, 
					    OssImage.Params.instance().getOssParam().ACCESS_OSS_KEY);
			ossClient.deleteObject(OssImage.Params.instance().getBucket(), oriImage.getObjectKey());
			
			//Upload the new image to oss storage.
			ossImage = getById(dbCon, staff, ossImage.getId());
			upload(staff, ossImage, builder.getImgType(), builder.getImgStream());
			
		}

	}
	
	private static void upload(Staff staff, OssImage ossImage, OssImage.ImageType imgType, InputStream istream) throws IOException{
		if(ossImage.getImage().trim().isEmpty()){
			ossImage.setImage(new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis()) + (int)(Math.random() * 1000) + "." + imgType);
		}
		ossImage.setRestaurantId(staff.getRestaurantId());
		
    	ObjectMetadata objectMeta = new ObjectMetadata();
    	objectMeta.setContentLength(istream.available());
    	
    	if(imgType == OssImage.ImageType.JPEG){
    		objectMeta.setContentType("image/jpeg");					
		}else if(imgType == OssImage.ImageType.GIF){
			objectMeta.setContentType("image/gif");
		}else if(imgType == OssImage.ImageType.PNG){
			objectMeta.setContentType("image/png");
		}
    	
		OSSClient ossClient = new OSSClient("http://" + OssImage.Params.instance().getOssParam().OSS_INNER_POINT, 
					 						    OssImage.Params.instance().getOssParam().ACCESS_OSS_ID, 
					 						    OssImage.Params.instance().getOssParam().ACCESS_OSS_KEY);
		//Check to see whether or not the bucket exist.
    	if (!ossClient.doesBucketExist(OssImage.Params.instance().getBucket())){
    		ossClient.createBucket(OssImage.Params.instance().getBucket());
    		ossClient.setBucketAcl(OssImage.Params.instance().getBucket(), CannedAccessControlList.PublicRead);
		}
    	//Upload the image.
    	ossClient.putObject(OssImage.Params.instance().getBucket(), ossImage.getObjectKey(), istream, objectMeta);
	}
	
    /**
     * Get the oss image according to specific id
     * @param staff
     * 			the staff to perform this action
     * @param extraCond
     * 			the extra condition
     * @return the list result to oss image
     * @throws SQLException
     * 			throws if failed to execute any SQL statement
     * @throws BusinessException
     * 			
     */
    public static OssImage getById(Staff staff, int id) throws SQLException, BusinessException{
    	DBCon dbCon = new DBCon();
    	try{
    		dbCon.connect();
    		return getById(dbCon, staff, id);
    	}finally{
    		dbCon.disconnect();
    	}
    }
    /**
     * Get the oss image according to specific id
     * @param dbCon
     * 			the database connection
     * @param staff
     * 			the staff to perform this action
     * @param extraCond
     * 			the extra condition
     * @return the list result to oss image
     * @throws SQLException
     * 			throws if failed to execute any SQL statement
     * @throws BusinessException
     * 			
     */
    public static OssImage getById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
    	List<OssImage> result = getByCond(dbCon, staff, new ExtraCond().setId(id));
    	if(result.isEmpty()){
    		throw new BusinessException(OssImageError.OSS_IMAGE_NOT_EXIST);
    	}else{
    		return result.get(0);
    	}
    }
    
    /**
     * Get the oss image according to extra condition {@link extraCond}
     * @param staff
     * 			the staff to perform this action
     * @param extraCond
     * 			the extra condition
     * @return the list result to oss image
     * @throws SQLException
     * 			throws if failed to execute any SQL statement
     */
    public static List<OssImage> getByCond(Staff staff, ExtraCond extraCond) throws SQLException{
    	DBCon dbCon = new DBCon();
    	try{
    		dbCon.connect();
    		return getByCond(dbCon, staff, extraCond);
    	}finally{
    		dbCon.disconnect();
    	}
    }
    
    /**
     * Get the oss image according to extra condition {@link extraCond}
     * @param dbCon
     * 			the database connection
     * @param staff
     * 			the staff to perform this action
     * @param extraCond
     * 			the extra condition
     * @return the list result to oss image
     * @throws SQLException
     * 			throws if failed to execute any SQL statement
     */
    public static List<OssImage> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
    	String sql;
    	sql = " SELECT oss_image_id, restaurant_id, image, type, associated_id, associated_serial, status, last_modified FROM " +
    		  Params.dbName + ".oss_image" +
    		  " WHERE 1 = 1 " +
    		  " AND restaurant_id = " + staff.getRestaurantId() +
    		  (extraCond != null ? extraCond.toString() : "");
    	dbCon.rs = dbCon.stmt.executeQuery(sql);
    	
    	List<OssImage> result = new ArrayList<OssImage>();
    	while(dbCon.rs.next()){
    		OssImage ossImage = new OssImage(dbCon.rs.getInt("oss_image_id"));
    		ossImage.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
    		ossImage.setAssociatedId(dbCon.rs.getInt("associated_id"));
    		ossImage.setAssociatedSerial(dbCon.rs.getString("associated_serial"));
    		ossImage.setImage(dbCon.rs.getString("image"));
    		ossImage.setType(OssImage.Type.valueOf(dbCon.rs.getInt("type")));
    		ossImage.setStatus(OssImage.Status.valueOf(dbCon.rs.getInt("status")));
    		ossImage.setLastModified(dbCon.rs.getLong("last_modified"));
    		result.add(ossImage);
    	}
    	dbCon.rs.close();
    	
    	return result;
    }
    
    /**
     * Delete the oss image according to extra condition {@link ExtraCond}.
     * @param staff
     * 			the staff to perform this action
     * @param extraCond
     * 			the extra condition.
     * @throws SQLException
     * 			throws if failed to execute any SQL statement
     */
    public static void delete(Staff staff, ExtraCond extraCond) throws SQLException{
    	DBCon dbCon = new DBCon();
    	try{
    		dbCon.connect();
    		delete(dbCon, staff, extraCond);
    	}finally{
    		dbCon.disconnect();
    	}
    }
    
    /**
     * Delete the oss image according to extra condition {@link ExtraCond}.
     * @param dbCon
     * 			the database connection
     * @param staff
     * 			the staff to perform this action
     * @param extraCond
     * 			the extra condition.
     * @throws SQLException
     * 			throws if failed to execute any SQL statement
     */
    public static void delete(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
    	for(OssImage ossImage : getByCond(dbCon, staff, extraCond)){
    		new OSSClient("http://" + OssImage.Params.instance().getOssParam().OSS_INNER_POINT, 
  					 	  OssImage.Params.instance().getOssParam().ACCESS_OSS_ID, 
  					 	  OssImage.Params.instance().getOssParam().ACCESS_OSS_KEY).deleteObject(OssImage.Params.instance().getBucket(), ossImage.getObjectKey());
    		String sql;
    		sql = " DELETE FROM " + Params.dbName + ".oss_image WHERE oss_image_id = " + ossImage.getId();
    		dbCon.stmt.executeUpdate(sql);
    	}
    }
    
}

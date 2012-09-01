package com.wireless.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

@SuppressWarnings("serial")
public class FileUploadForm extends ActionForm{
	
    private List<FormFile> files = new ArrayList<FormFile>(); 
    
    /**
     * 
     * @param i
     * @return
     */
    public FormFile getFile(int i) {
    	if(files.size() == 0 || i > files.size()){
    		return null;
    	}else{
    		return files.get(i);
    	}
    }  
    
    /**
     * 
     * @param i
     * @param myFile
     */
    public void setFile(int i, FormFile myFile){  
        if (myFile.getFileSize() > 0) 
        {  
        	files.add(myFile);  
        }  
    }
    
    /***
     * 
     * @param myFile
     */
    public void setFile(FormFile myFile){  
        if (myFile.getFileSize() > 0) 
        {  
        	files.add(myFile);  
        }  
    }
    
    /**
     *  获得上传文件的个数 
     * @return
     */
    public int getFileCount() {  
        return files.size();  
    }
    
    /**
     * 
     * @return
     */
	public List<FormFile> getFiles() {
		return files;
	}
	
	/**
	 * 
	 * @param files
	 */
	public void setFiles(List<FormFile> files) {
		this.files = files;
	} 
    
    
}

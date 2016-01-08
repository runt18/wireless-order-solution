package com.wireless.test.db.weixin.action;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.marker.weixin.msg.Data4Item;
import org.marker.weixin.msg.Msg;
import org.marker.weixin.msg.Msg4Head;
import org.marker.weixin.msg.Msg4Head.MsgType;
import org.marker.weixin.msg.Msg4ImageText;
import org.marker.weixin.msg.Msg4Text;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.OSSException;
import com.wireless.db.oss.OssImageDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.action.WxMenuActionDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.OssImageError;
import com.wireless.exception.WxMenuError;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.oss.OssImage.ImageType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.action.WxMenuAction;
import com.wireless.test.db.TestInit;
import com.wireless.test.db.oss.TestOssImage;

public class TestWxMenuAction {
	
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, SQLException, BusinessException{
		TestInit.init();
		mStaff = StaffDao.getAdminByRestaurant(40);
	}
	
	@Test
	public void testWxMenuAction4ImageText() throws SQLException, BusinessException, SAXException, IOException, ParserConfigurationException, TransformerException, InterruptedException{
		int actionId = 0;
		
		OSSClient ossClient = new OSSClient("http://" + OssImage.Params.instance().getOssParam().OSS_INNER_POINT, 
				OssImage.Params.instance().getOssParam().ACCESS_OSS_ID, 
				OssImage.Params.instance().getOssParam().ACCESS_OSS_KEY);
		int ossImageId = 0;
		
		try{
			String fileName = System.getProperty("user.dir") + "/src/" + TestOssImage.class.getPackage().getName().replaceAll("\\.", "/") + "/test.jpg";
			
			//Test to insert a new weixin image text msg action.
			ossImageId = OssImageDao.insert(mStaff, new OssImage.InsertBuilder(OssImage.Type.WX_ACTION_IMAGE, 1)
																.setImgResource(ImageType.JPG, new FileInputStream(new File(fileName))));
			OssImage image = OssImageDao.getById(mStaff, ossImageId);
			WxMenuAction.InsertBuilder4ImageText insert4ImageText = new WxMenuAction.InsertBuilder4ImageText(new Data4Item("测试Title", "测试Description", image.getObjectUrl(), "http://www.baidu.com"), WxMenuAction.Cate.NORMAL);
			actionId = WxMenuActionDao.insert(mStaff, insert4ImageText);
			
			WxMenuAction expected = insert4ImageText.build();
			expected.setId(actionId);
			WxMenuAction actual = WxMenuActionDao.getById(mStaff, actionId);
			
			compare(expected, actual);
			
			//Test the transfer between wx menu action and msg.
//			Msg4Head header = new Msg4Head();
//			header.setFromUserName("head_from_user");
//			header.setToUserName("head_to_user");
//			
//			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
//			Msg msg = new WxMenuAction.MsgProxy(header, actual).toMsg();
//			if(msg.getHead().getMsgType() == MsgType.MSG_TYPE_IMAGE_TEXT){
//				System.out.println(((Msg4ImageText)msg));
//			}
//			msg.write(document);
//			Transformer transformer = TransformerFactory.newInstance().newTransformer();
//			transformer.transform(new DOMSource(document), new StreamResult(new OutputStreamWriter(System.out, "utf-8")));
			//-------------------------------------------------------------------------------------------------
			
			//Test to update the msg action to text.
			WxMenuAction.UpdateBuilder4Text update4Text = new WxMenuAction.UpdateBuilder4Text(actionId, "测试修改");
			WxMenuActionDao.update(mStaff, update4Text);
			
			//Check if the original oss image is deleted. 
			try{
				OssImageDao.getById(mStaff, ossImageId);
				Assert.assertTrue("failed to delete the oss image", false);
			}catch(BusinessException e){
				Assert.assertEquals("failed to delete oss image", OssImageError.OSS_IMAGE_NOT_EXIST, e.getErrCode());
			}
			//Check to see whether the image has been removed from oss storage.
			for(Data4Item item : ((Msg4ImageText)new WxMenuAction.MsgProxy(actual).toMsg()).getItems()){
				try{
					ossClient.getObject(OssImage.Params.instance().getBucket(), item.getPicUrl());
					Assert.assertTrue("failed to delete the image from aliyun oss storage", false);
				}catch(OSSException ignored){
				}
			}
			
			expected = update4Text.builder().build();
			actual = WxMenuActionDao.getById(mStaff, actionId);
			expected.setCate(actual.getCate());
			compare(expected, actual);
			//-------------------------------------------------------------------------------------------------
			
			Thread.sleep(1000);
			//Test to update the msg action to image text.
			ossImageId = OssImageDao.insert(mStaff, new OssImage.InsertBuilder(OssImage.Type.WX_ACTION_IMAGE, 1)
																.setImgResource(ImageType.JPG, new FileInputStream(new File(fileName))));
			image = OssImageDao.getById(mStaff, ossImageId);
			WxMenuAction.UpdateBuilder4ImageText update4ImageText = new WxMenuAction.UpdateBuilder4ImageText(actionId, new Data4Item("测试Title", "测试Description", image.getObjectUrl(), "http://www.baidu.com"));
			WxMenuActionDao.update(mStaff, update4ImageText);
			
			expected = update4ImageText.builder().build();
			actual = WxMenuActionDao.getById(mStaff, actionId);
			expected.setCate(actual.getCate());
			compare(expected, actual);
			//-------------------------------------------------------------------------------------------------
			
		}finally{
			if(actionId != 0){
				WxMenuAction oriAction = WxMenuActionDao.getById(mStaff, actionId);

				WxMenuActionDao.deleteById(mStaff, actionId);
				try{
					WxMenuActionDao.getById(mStaff, actionId);
					Assert.assertTrue("failed to delete the weixin menu action", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the weixin menu action", WxMenuError.WEIXIN_MENU_ACTION_NOT_EXIST, e.getErrCode());
				}
				
				if(oriAction.getMsgType() == MsgType.MSG_TYPE_IMAGE_TEXT && ossImageId != 0){
					//Check to see whether the oss image has been deleted from database.
					try{
						OssImageDao.getById(mStaff, ossImageId);
						Assert.assertTrue("failed to delete the oss image", false);
					}catch(BusinessException e){
						Assert.assertEquals("failed to delete oss image", OssImageError.OSS_IMAGE_NOT_EXIST, e.getErrCode());
					}
					//Check to see whether the image has been removed from oss storage.
					for(Data4Item item : ((Msg4ImageText)new WxMenuAction.MsgProxy(oriAction).toMsg()).getItems()){
						try{
							ossClient.getObject(OssImage.Params.instance().getBucket(), item.getPicUrl());
							Assert.assertTrue("failed to delete the image from aliyun oss storage", false);
						}catch(OSSException ignored){
						}
					}
				}
			}
		}
	}
	
	@Test
	public void testWxMenuAction4Text() throws SQLException, BusinessException, SAXException, IOException, ParserConfigurationException, TransformerException{
		int actionId = 0;
		try{
			//Test to insert a new weixin text msg action.
			WxMenuAction.InsertBuilder4Text insert4Text = new WxMenuAction.InsertBuilder4Text("测试内容", WxMenuAction.Cate.NORMAL);
			actionId = WxMenuActionDao.insert(mStaff, insert4Text);
			
			WxMenuAction expected = insert4Text.build();
			expected.setId(actionId);
			WxMenuAction actual = WxMenuActionDao.getById(mStaff, actionId);
			
			compare(expected, actual);
			
			//Test the transfer between wx menu action and msg.
			Msg4Head header = new Msg4Head();
			header.setFromUserName("head_from_user");
			header.setToUserName("head_to_user");
			
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Msg msg = new WxMenuAction.MsgProxy(header, actual).toMsg();
			if(msg.getHead().getMsgType() == MsgType.MSG_TYPE_TEXT){
				System.out.println(((Msg4Text)msg).getContent());
			}
			msg.write(document);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(document), new StreamResult(new OutputStreamWriter(System.out, "utf-8")));
			
			//Test to update the msg action just inserted.
			WxMenuAction.UpdateBuilder4Text update4Text = new WxMenuAction.UpdateBuilder4Text(actionId, "测试修改");
			WxMenuActionDao.update(mStaff, update4Text);
			
			expected = update4Text.builder().build();
			actual = WxMenuActionDao.getById(mStaff, actionId);
			expected.setCate(actual.getCate());
			compare(expected, actual);
			
		}finally{
			if(actionId != 0){
				WxMenuActionDao.deleteById(mStaff, actionId);
				try{
					WxMenuActionDao.getById(mStaff, actionId);
					Assert.assertTrue("failed to delete the weixin menu action", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the weixin menu action", WxMenuError.WEIXIN_MENU_ACTION_NOT_EXIST, e.getErrCode());
				}
			}
		}
	}
	
	private void compare(WxMenuAction expected, WxMenuAction actual){
		Assert.assertEquals("wx menu action id", expected.getId(), actual.getId());
		Assert.assertEquals("wx menu action msg type", expected.getMsgType().toString(), actual.getMsgType().toString());
		Assert.assertEquals("wx menu action", expected.getAction(), actual.getAction());
		Assert.assertEquals("wx menu cate", expected.getCate(), actual.getCate());
	}
}

package com.wireless.test.db.weixin.menuAction;

import java.beans.PropertyVetoException;
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
import org.marker.weixin.msg.Msg4Head;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.menuAction.WxMenuAction;
import com.wireless.db.weixin.menuAction.WxMenuActionDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WxMenuError;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestWxMenuAction {
	
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, SQLException, BusinessException{
		TestInit.init();
		mStaff = StaffDao.getAdminByRestaurant(40);
	}
	
	@Test
	public void testWxMenuAction() throws SQLException, BusinessException, SAXException, IOException, ParserConfigurationException, TransformerException{
		int actionId = 0;
		try{
			//Test to insert a new weixin text msg action.
			WxMenuAction.InsertBuilder4Text insert4Text = new WxMenuAction.InsertBuilder4Text("测试内容");
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
			new WxMenuAction.Msg4Action(header, actual).write(document);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(document), new StreamResult(new OutputStreamWriter(System.out, "utf-8")));
			
			//Test to update the msg action just inserted.
			WxMenuAction.UpdateBuilder4Text update4Text = new WxMenuAction.UpdateBuilder4Text(actionId, "测试修改");
			WxMenuActionDao.update(mStaff, update4Text);
			
			expected = update4Text.build();
			actual = WxMenuActionDao.getById(mStaff, actionId);
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
		Assert.assertEquals("wx menu action msg type", expected.getMsgType().getType(), actual.getMsgType().getType());
		Assert.assertEquals("wx menu action", expected.getAction(), actual.getAction());
	}
}

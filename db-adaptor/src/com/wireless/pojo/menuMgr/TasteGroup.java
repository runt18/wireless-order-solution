package com.wireless.pojo.menuMgr;

import java.util.ArrayList;
import java.util.List;

import com.wireless.protocol.Taste;

public class TasteGroup {
	private int groupID;
	private TasteBasic normalTaste;
	private TasteBasic tempTaste;
	private List<TasteBasic> normalTasteContent;
	
	public TasteGroup() {
		this.normalTaste = new TasteBasic();
		this.tempTaste = new TasteBasic();
		this.normalTasteContent = new ArrayList<TasteBasic>();
	}
	
	public TasteGroup(TasteBasic normalTaste, TasteBasic tempTaste, List<TasteBasic> normalTasteContent) {
		this.normalTaste = normalTaste;
		this.tempTaste = tempTaste;
		this.normalTasteContent = normalTasteContent;
	}
	public int getGroupID() {
		return groupID;
	}
	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}
	public TasteBasic getNormalTaste() {
		return normalTaste;
	}
	public void setNormalTaste(TasteBasic normalTaste) {
		this.normalTaste = normalTaste;
	}
	public TasteBasic getTempTaste() {
		return tempTaste;
	}
	public void setTempTaste(TasteBasic tempTaste) {
		this.tempTaste = tempTaste;
	}
	public List<TasteBasic> getNormalTasteContent() {
		return normalTasteContent;
	}
	public void setNormalTasteContent(List<TasteBasic> normalTasteContent) {
		this.normalTasteContent = normalTasteContent;
	}
	// add taste
	public void addTaste(TasteBasic tb){
		this.normalTasteContent.add(tb);
	}
	
	/**
	 * 
	 * @param pojo
	 * @param clazz
	 * @return
	 */
	public static Object changeToOther(TasteGroup pojo, Class<?> clazz){
		Object obj = null;
		if(clazz.equals(com.wireless.protocol.TasteGroup.class)){
			com.wireless.protocol.TasteGroup pt = new com.wireless.protocol.TasteGroup();
			TasteBasic tt = pojo.getTempTaste();
			List<TasteBasic> ntc = pojo.getNormalTasteContent();
			
			pt.setTmpTaste((Taste) TasteBasic.changeToOther(tt, com.wireless.protocol.TasteGroup.class));
			com.wireless.protocol.Taste[] ptntc = new com.wireless.protocol.Taste[ntc.size()];
			for(int i = 0; i < ntc.size(); i++){
				ptntc[i] = (Taste) TasteBasic.changeToOther(ntc.get(i), com.wireless.protocol.TasteGroup.class);
			}
			obj = pt;
		}
		return obj;
	}
}

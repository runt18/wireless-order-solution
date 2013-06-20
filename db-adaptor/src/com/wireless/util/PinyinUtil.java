package com.wireless.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public final class PinyinUtil {

    /** 
     * 获取汉字串拼音首字母，英文字符不变 
     * 
     * @param chinese 汉字串 
     * @return 汉语拼音首字母 
     */ 
	public static String cn2FirstSpell(String chinese) {
		
		HanyuPinyinOutputFormat pinyinOutputFormat = new HanyuPinyinOutputFormat();
		pinyinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		pinyinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		
		char[] arr = chinese.toCharArray();
		
		StringBuilder pinyinBuf = new StringBuilder();
		pinyinBuf.delete(0, pinyinBuf.length());
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > 128) {
				try {
					String[] result = PinyinHelper.toHanyuPinyinStringArray(arr[i],	pinyinOutputFormat);
					if (result != null) {
						pinyinBuf.append(result[0].charAt(0));
					}
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			} else {
				pinyinBuf.append(arr[i]);
			}
		}
		return pinyinBuf.toString().replaceAll("\\W", "").trim();
	}

    /** 
     * 获取汉字串拼音，英文字符不变 
     * 
     * @param chinese 汉字串 
     * @return 汉语拼音 
     */ 
    public static String cn2Spell(String chinese) {
    	
    	HanyuPinyinOutputFormat pinyinOutputFormat = new HanyuPinyinOutputFormat();
    	pinyinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
    	pinyinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    	
		char[] arr = chinese.toCharArray();
		StringBuilder pinyinBuf = new StringBuilder();
		pinyinBuf.delete(0, pinyinBuf.length());
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > 128) {
				try {
					String[] result = PinyinHelper.toHanyuPinyinStringArray(arr[i], pinyinOutputFormat);
					if(result != null){
						pinyinBuf.append(result[0]);
					}
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			} else {
				pinyinBuf.append(arr[i]);
			}
		}
		return pinyinBuf.toString();
    } 

}

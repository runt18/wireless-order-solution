package com.wireless.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

final public class PinyinUtil {

	private static StringBuffer mPinyinBuf = new StringBuffer();
	private static HanyuPinyinOutputFormat mPinyinOutputFormat = new HanyuPinyinOutputFormat();
	
    /** 
     * 获取汉字串拼音首字母，英文字符不变 
     * 
     * @param chinese 汉字串 
     * @return 汉语拼音首字母 
     */ 
	public static String cn2FirstSpell(String chinese) {
		
		mPinyinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		mPinyinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		
		char[] arr = chinese.toCharArray();
		
		mPinyinBuf.delete(0, mPinyinBuf.length());
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > 128) {
				try {
					String[] result = PinyinHelper.toHanyuPinyinStringArray(arr[i],	mPinyinOutputFormat);
					if (result != null) {
						mPinyinBuf.append(result[0].charAt(0));
					}
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			} else {
				mPinyinBuf.append(arr[i]);
			}
		}
		return mPinyinBuf.toString().replaceAll("\\W", "").trim();
	}

    /** 
     * 获取汉字串拼音，英文字符不变 
     * 
     * @param chinese 汉字串 
     * @return 汉语拼音 
     */ 
    public static String cn2Spell(String chinese) {
    	
		mPinyinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		mPinyinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    	
		char[] arr = chinese.toCharArray();
		mPinyinBuf.delete(0, mPinyinBuf.length());
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > 128) {
				try {
					String[] result = PinyinHelper.toHanyuPinyinStringArray(arr[i], mPinyinOutputFormat);
					if(result != null){
						mPinyinBuf.append(result[0]);
					}
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			} else {
				mPinyinBuf.append(arr[i]);
			}
		}
		return mPinyinBuf.toString();
    } 

}

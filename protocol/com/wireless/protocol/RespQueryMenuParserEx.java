package com.wireless.protocol;

import java.util.Arrays;
import java.util.Comparator;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class RespQueryMenuParserEx {
	
	private static StringBuffer mPinyinBuf = new StringBuffer();
	private static HanyuPinyinOutputFormat mPinyinOutputFormat = new HanyuPinyinOutputFormat();
	
	/**
	 * Parse the response associated with query menu request.
	 * @param response the protocol package return from ProtocolConnector's ask() function
	 * @return the vector containing the food instance
	 */
	public static FoodMenu parse(ProtocolPackage response){
		FoodMenu foodMenu = RespQueryMenuParser.parse(response);
		
		Comparator<Taste> tasteComp = new Comparator<Taste>(){
			@Override
			public int compare(Taste taste1, Taste taste2) {
				return taste1.compare(taste2);
			}
		};
		
		Arrays.sort(foodMenu.tastes, tasteComp);
		Arrays.sort(foodMenu.styles, tasteComp);
		Arrays.sort(foodMenu.specs, tasteComp);
		
		Comparator<Food> foodComp = new Comparator<Food>(){
			@Override
			public int compare(Food arg0, Food arg1) {
				if(arg0.aliasID > arg1.aliasID){
					return 1;
				}else if(arg0.aliasID < arg1.aliasID){
					return -1;
				}else{
					return 0;
				}
			}			
		};
		
		mPinyinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		mPinyinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

		
		Arrays.sort(foodMenu.foods, foodComp);
		for(int i = 0; i < foodMenu.foods.length; i++){
	
			foodMenu.foods[i].setPinyin(cn2Spell(foodMenu.foods[i].name));
			foodMenu.foods[i].setPinyinShortcut(cn2FirstSpell(foodMenu.foods[i].name));
			
			if(foodMenu.foods[i].isCombo()){
				for(int j = 0; j < foodMenu.foods[i].childFoods.length; j++){
					int index = Arrays.binarySearch(foodMenu.foods, foodMenu.foods[i].childFoods[j], foodComp);
					if(index >= 0){
						foodMenu.foods[i].childFoods[j] = foodMenu.foods[index];
					}
				}
			}
			
			if(foodMenu.foods[i].popTastes != null){
				for(int j = 0; j < foodMenu.foods[i].popTastes.length; j++){
					int index;
					index = Arrays.binarySearch(foodMenu.tastes, foodMenu.foods[i].popTastes[j], tasteComp);
					if(index >= 0){
						foodMenu.foods[i].popTastes[j] = foodMenu.tastes[index];
						continue;
					}
					
					index = Arrays.binarySearch(foodMenu.styles, foodMenu.foods[i].popTastes[j], tasteComp);
					if(index >= 0){
						foodMenu.foods[i].popTastes[j] = foodMenu.styles[index];
						continue;
					}
					
					index = Arrays.binarySearch(foodMenu.specs, foodMenu.foods[i].popTastes[j], tasteComp);
					if(index >= 0){
						foodMenu.foods[i].popTastes[j] = foodMenu.specs[index];
						continue;
					}
				}
			}
		}		
		
		return foodMenu;
		
	}

    /** 
     * 获取汉字串拼音首字母，英文字符不变 
     * 
     * @param chinese 汉字串 
     * @return 汉语拼音首字母 
     */ 
	public static String cn2FirstSpell(String chinese) {
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

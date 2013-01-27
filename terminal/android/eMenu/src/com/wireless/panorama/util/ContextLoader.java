package com.wireless.panorama.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
/**
 * 根据包名获取所有符合的context
 * @author ggdsn1
 *
 */
public class ContextLoader {

	/**
	 * get all context matched by packageName
	 * @param packageName
	 * @return List<Context>, it might be empty
	 */
	public static List<Context> getPackageContexts(Context context, String packageName){
		ArrayList<PackageInfo> layoutList = new ArrayList<PackageInfo>();
		List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);
		for(PackageInfo p: packs){
			
			if(isMatchedPackage(p, packageName)){
				layoutList.add(p);
			}
		}
		
		ArrayList<Context> contexts = new ArrayList<Context>();
		for(PackageInfo layoutPack : layoutList){
			try{
				Context aContext = context.createPackageContext(layoutPack.packageName, Context.CONTEXT_IGNORE_SECURITY);
				contexts.add(aContext);
			} catch(NameNotFoundException e){
				
			}
		}
		
		return contexts;
	}
	
	public static boolean isMatchedPackage(Context context, String regex){
		return isMatchedPackage(context.getPackageName(), regex);
	}
	
	/**
	 * 判断是否是匹配的包
	 * @param regex 要匹配的包名
	 */
	public static boolean isMatchedPackage(PackageInfo packageInfo, String regex)
	{
		return isMatchedPackage(packageInfo.packageName, regex);
	}
	
	/**
	 * 判断是否是匹配的包
	 * @param regex 要匹配的包名
	 */
	public static boolean isMatchedPackage(String packageName, String regex)
	{
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(packageName);
		return matcher.find();
	}
	
}

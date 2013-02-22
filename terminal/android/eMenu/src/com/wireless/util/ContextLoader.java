package com.wireless.util;

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
	 * Get all context to layout package matched by package name
	 * @param packageName
	 * @return List<Context>, it might be empty
	 */
	public static List<Context> getPackageContexts(Context context, String packageName){
		ArrayList<PackageInfo> layoutPackages = new ArrayList<PackageInfo>();
		List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);
		for(PackageInfo p: packs){
			
			if(isMatchedPackage(p, packageName)){
				layoutPackages.add(p);
			}
		}
		
		ArrayList<Context> contextToLayoutPackages = new ArrayList<Context>();
		for(PackageInfo layoutPack : layoutPackages){
			try{
				contextToLayoutPackages.add(context.createPackageContext(layoutPack.packageName, Context.CONTEXT_IGNORE_SECURITY));
			} catch(NameNotFoundException e){
				
			}
		}
		
		return contextToLayoutPackages;
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

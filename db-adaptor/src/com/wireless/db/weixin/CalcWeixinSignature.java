package com.wireless.db.weixin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CalcWeixinSignature {
	
	private static String bytes2Hex(byte[] bts) {
		StringBuilder des = new StringBuilder();
		String tmp = null;
		for (int i = 0; i < bts.length; i++) {
			tmp = Integer.toHexString(bts[i] & 0xFF);
			if (tmp.length() == 1) {
				des.append("0");
			}
			des.append(tmp);
		}
		return des.toString();
	}
	
	public static String calc(String token, String timestamp, String nonce) throws NoSuchAlgorithmException{
		// 重写toString方法，得到三个参数的拼接字符串
		List<String> list = new ArrayList<String>(3) {
			private static final long serialVersionUID = 2621444383666420433L;
			public String toString() {
				return this.get(0) + this.get(1) + this.get(2);
			}
		};
		list.add(token);
		list.add(timestamp);
		list.add(nonce);
		// 排序
		Collections.sort(list);
		// SHA-1加密
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(list.toString().getBytes());
		
		return bytes2Hex(md.digest());
	}
}

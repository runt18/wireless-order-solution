package com.wireless.Actions.orderMgr;

import com.wireless.protocol.Order;

public class Util {
	static String toOrderCate(int type){
		if(type == Order.CATE_NORMAL){
			return "一般";
		}else if(type == Order.CATE_JOIN_TABLE){
			return "拼台";
		}else if(type == Order.CATE_MERGER_TABLE){
			return "并台";
		}else if(type == Order.CATE_TAKE_OUT){
			return "外卖";
		}else{
			return "一般";
		}
	}
	
	static String toPayManner(int manner){
		if(manner == Order.MANNER_CASH){
			return "现金";
		}else if(manner == Order.MANNER_CREDIT_CARD){
			return "刷卡";
		}else if(manner == Order.MANNER_HANG){
			return "挂账";
		}else if(manner == Order.MANNER_MEMBER){
			return "会员卡";
		}else if(manner == Order.MANNER_SIGN){
			return "签单";
		}else{
			return "现金";
		}
	}
}

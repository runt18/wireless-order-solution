package com.wireless.exception;

public class PromotionError extends ErrorEnum{

	/**
	 *  code range : 7200 - 7249 
	 */
	public static final ErrorCode PROMOTION_NOT_EXIST = build(7200, "你查找的优惠活动不存在");
	public static final ErrorCode PROMOTION_START_DATE_EXCEED_NOW = build(7201, "活动的开始日期应在当前日期之后");
	public static final ErrorCode PROMOTION_CREATE_NOT_ALLOW = build(7202, "优惠活动不能创建");
	public static final ErrorCode PROMOTION_DELETE_NOT_ALLOW = build(7203, "优惠活动不能删除");
	public static final ErrorCode PROMOTION_PUBLISH_NOT_ALLOW = build(7204, "优惠活动不能发布");
	public static final ErrorCode PROMOTION_FINISH_NOT_ALLOW = build(7205, "优惠活动不能结束");
	public static final ErrorCode PROMOTION_UPDATE_NOT_ALLOW = build(7206, "优惠活动不能修改");
	
	public static final ErrorCode COUPON_TYPE_NOT_EXIST = build(7231, "操作失败, 该优惠券类型不存在");
	public static final ErrorCode COUPON_NOT_EXIST = build(7232, "操作失败, 该优惠券不存在");
	public static final ErrorCode COUPON_EXPIRED = build(7233, "操作失败, 该优惠券已过期");
	public static final ErrorCode COUPON_DRAW_NOT_ALLOW = build(7234, "优惠券不能领取");
	public static final ErrorCode COUPON_CREATE_NOT_ALLOW = build(7235, "优惠券不能创建");
	public static final ErrorCode COUPON_USE_NOT_ALLOW = build(7236, "优惠券不能使用");
	public static final ErrorCode COUPON_ISSUE_NOT_ALLOW = build(7237, "优惠券不能发送");
	
	private PromotionError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.PROMOTION, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.PROMOTION, code);
	}
}

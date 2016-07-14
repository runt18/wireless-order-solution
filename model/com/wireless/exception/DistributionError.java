package com.wireless.exception;

public class DistributionError extends ErrorEnum{
	
	//5200
	public static final ErrorCode DISTRIBUTION_TYPE_NOMAP = build(5200, "操作失败,配送单和库单类别不一致");
	public static final ErrorCode DISTRIBUTION_NOT_EXIST = build(5201, "操作失败,库单不存在");
	public static final ErrorCode DISTRIBUTION_NOT_AUDIT = build(5202, "操作失败,未审核的库单不能引用");
	public static final ErrorCode RESTAURANT_NOT_GROUP = build(5203, "操作失败,出库门店和入库门店不是连锁");
	public static final ErrorCode MATERIAL_NOT_MAP = build(5204, "操作失败,没有匹配货品,请进行配送同步操作");
	
	private DistributionError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.DISTRIBUTION, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.DISTRIBUTION, code);
	}
}

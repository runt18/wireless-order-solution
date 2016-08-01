package com.wireless.exception;

public class DistributionError extends ErrorEnum{
	
	//5200
	public static final ErrorCode DISTRIBUTION_TYPE_NOMAP = build(5200, "操作失败,配送单和库单类别不一致");
	public static final ErrorCode DISTRIBUTION_NOT_EXIST = build(5201, "操作失败,库单不存在");
	public static final ErrorCode DISTRIBUTION_NOT_AUDIT = build(5202, "操作失败,未审核的库单不能引用");
	public static final ErrorCode RESTAURANT_NOT_GROUP = build(5203, "操作失败,出库门店和入库门店不是连锁");
	public static final ErrorCode MATERIAL_NOT_MAP = build(5204, "操作失败,没有匹配货品,请进行配送同步操作");
	public static final ErrorCode MATERIAL_GROUP_EXIST = build(5205, "操作失败,货品已删除或是分店独有");
	public static final ErrorCode RECEIVE_BUILD_ERR = build(5206, "操作失败,收货单只能是连锁下的门店才能建立");
	public static final ErrorCode SEND_BUILD_ERR = build(5207, "操作失败,发货单只能是连锁的总部才能建立");
	public static final ErrorCode RETURN_BUILD_ERR = build(5208, "操作失败,退货单只能是连锁下的门店才能建立");
	public static final ErrorCode RECOVERY_BUILD_ERR = build(5209, "操作失败,回收单只能是连锁下的总部才能建立");
	public static final ErrorCode APPLY_BUILD_ERR = build(5210,"操作失败,申请单只能是联络下的门店才能建立");
	
	private DistributionError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.DISTRIBUTION, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.DISTRIBUTION, code);
	}
}

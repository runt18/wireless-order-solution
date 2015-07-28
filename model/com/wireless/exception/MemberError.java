package com.wireless.exception;

public class MemberError extends ErrorEnum{
	/** 
	 *  codeRange : 9600 - 9799
	 * 	member : 			9750 - 9799
	 * 	memberCard :		9700 - 9749
	 * 	memberOperation :	9650 - 9699
	 * 	memberType   :		9620 - 9649
	 *  memberLevel  :      9600 - 9619
	 *  coupon :			
	 */
	/* member 9750 - 9799 */
	public static final ErrorCode UPDATE_FAIL = build(9798, "操作失败, 修改会员资料失败, 请检查数据内容是否正确.");
	public static final ErrorCode UPDATE_POINT = build(9793, "操作失败, 修改会员积分信息失败.");
	public static final ErrorCode MEMBER_NOT_EXIST = build(9792, "操作失败, 查找的会员不存在.");
	public static final ErrorCode EXCEED_POINT = build(9791, "操作失败, 积分余额不足.");
	public static final ErrorCode EXCEED_BALANCE = build(9790, "操作失败, 会员余额不足.");
	public static final ErrorCode MOBLIE_DUPLICATED = build(9789, "操作失败, 该手机号码已存在, 请重新输入.");
	public static final ErrorCode MEMBER_CARD_DUPLICATED = build(9789, "操作失败, 该会员卡已存在, 请重新输入.");
	public static final ErrorCode ADJUST_POINT_FAIL = build(9788, "操作失败, 会员积分调整失败, 请检查数据格式.");
	public static final ErrorCode CONSUME_POINT_FAIL = build(9787, "操作失败, 会员积分消费失败, 请检查数据格式.");
	public static final ErrorCode BIND_FAIL = build(9786, "操作失败, 会员绑定失败.");
	public static final ErrorCode REFUND_FAIL = build(975, "操作失败, 取款不成功.");
	
	/* memberOperation 9650 - 9699 */
	public static final ErrorCode OPERATION_INSERT = build(9699, "操作失败, 添加充值操作日志失败, 请联系客服人员!");
	public static final ErrorCode OPERATION_DELETE = build(9698, "操作失败, 删除充值操作日志失败, 请联系客服人员!");
	public static final ErrorCode OPERATION_UPDATE = build(9697, "操作失败, 修改充值操作日志失败, 请联系客服人员!");
	public static final ErrorCode OPERATION_NOT_EXIST = build(9696, "操作失败, 查询不到对应的操作日志, 请联系客服人员!");
	
	/* memberType 9620 - 9649 */
	public static final ErrorCode TYPE_INSERT = build(9649, "操作失败, 添加会员类型信息失败, 请检查数据内容是否正确.");
	public static final ErrorCode MEMBER_TYPE_NOT_EXIST = build(9648, "操作失败, 该会员类型不存在或已被删除");
	public static final ErrorCode TYPE_UPDATE = build(9647, "操作失败, 修改充值操作日志失败, 请检查数据内容是否正确.");
	public static final ErrorCode TYPE_SET_ORDER_DISCOUNT = build(9647, "操作失败, 设置会员类型全单折扣信息失败, 未知错误.");
	public static final ErrorCode TYPE_DELETE_FAIL_BECAUSE_MEMBER_NOT_EMPTY = build(9646, "操作失败, 该类型下已有会员, 不允许删除.");
	public static final ErrorCode TYPE_DELETE_FAIL_BECAUSE_LEVEL_IN_USED = build(9616, "操作失败, 该类型在会员升级路线图使用, 不允许删除.");
	
	/* memberType 9600 - 9619 */
	public static final ErrorCode MEMBER_LEVEL_NOT_EXIST = build(9619, "操作失败, 该会员等级不存在或已被删除");
	public static final ErrorCode MEMBER_TYPE_BELONG = build(9618, "操作失败, 此会员类型已属于别的等级,不能选择");
	public static final ErrorCode MEMBER_LEVEL_LESS_POINT = build(9617, "操作失败, 积分必须大于低等级的");
	public static final ErrorCode MEMBER_LEVEL_MORE_POINT = build(9616, "操作失败, 积分必须小于高等级的");
	public static final ErrorCode MEMBER_LEVEL_HIGHEST = build(9617, "操作失败, 积分必须最高等级的");

	public static final ErrorCode TAKE_OUT_ADDRESS_NOT_EXIST = build(9617, "外卖地址不存在");
	
	private MemberError(){
		
	}
	static ErrorCode build(int code, String desc){
		return build(ErrorType.MEMBER, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.MEMBER, code);
	}
}

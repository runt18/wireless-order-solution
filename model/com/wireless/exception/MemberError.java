package com.wireless.exception;

public class MemberError extends ErrorEnum{
	/** 
	 * codeRange : 9600 - 9799
	 * 	member : 			9750 - 9799
	 * 	memberCard :		9700 - 9749
	 * 	memberOperation :	9650 - 9699
	 * 	memberType :		9600 - 9649
	 */
	/* member 9750 - 9799 */
	public static final ErrorCode UNKNOWN = build(ErrorEnum.UNKNOWN_CODE, "操作失败, 未知错误.");
	public static final ErrorCode INSERT_FAIL = build(9799, "操作失败, 添加新会员资料失败, 请检查数据内容是否正确.");
	public static final ErrorCode UPDATE_FAIL = build(9798, "操作失败, 修改会员资料失败, 请检查数据内容是否正确.");
	public static final ErrorCode DELETE_FAIL = build(9797, "操作失败, 删除会员资料失败, 请检查数据内容是否正确.");
	public static final ErrorCode REMOVE_CLIENT = build(9796, "操作失败, 删除客户资料绑定失败.");
	public static final ErrorCode BINDING_CLIENT = build(9795, "操作失败, 绑定客户资料失败.");
	public static final ErrorCode UPDATE_BALANCE = build(9794, "操作失败, 修改会员金额信息失败.");
	public static final ErrorCode UPDATE_POINT = build(9793, "操作失败, 修改会员积分信息失败.");
	public static final ErrorCode MEMBER_NOT_EXIST = build(9794, "查找的会员不存在");
	public static final ErrorCode EXCEED_POINT = build(9795, "积分余额不足够");
	public static final ErrorCode EXCEED_BALANCE = build(9796, "会员余额不足够");
	
	/* memberCard 9700 - 9749 */
	public static final ErrorCode CARD_INSERT_FAIL = build(9749, "操作失败, 添加新会员卡信息失败, 请尝试更换其他会员卡.");
	public static final ErrorCode CARD_DELETE_FAIL = build(9748, "操作失败, 删除会员卡信息失败, 请检查数据内容正确.");
	public static final ErrorCode CARD_UPDATE_FAIL = build(9747, "操作失败, 修改会员卡信息失败, 请检查数据内容正确.");
	public static final ErrorCode CARD_UPDATE_STATUS = build(9746, "操作失败, 修改会员卡状态失败.");
	public static final ErrorCode CARD_STATUS_IS_ACTIVE = build(9745, "操作失败, 该会员卡已被使用.");
	public static final ErrorCode CARD_STATUS_IS_DISABLE = build(9744, "操作失败, 该会员卡已被禁用.");
	public static final ErrorCode CARD_STATUS_IS_LOST = build(9743, "操作失败, 该会员卡已被挂失.");
	public static final ErrorCode CARD_IS_EQUAL = build(9742, "操作失败, 新旧卡一样, 无需修改.");
	
	/* memberOperation 9650 - 9699 */
	public static final ErrorCode OPERATION_INSERT = build(9699, "操作失败, 添加充值操作日志失败, 请联系客服人员!");
	public static final ErrorCode OPERATION_DELETE = build(9698, "操作失败, 删除充值操作日志失败, 请联系客服人员!");
	public static final ErrorCode OPERATION_UPDATE = build(9697, "操作失败, 修改充值操作日志失败, 请联系客服人员!");
	public static final ErrorCode OPERATION_SEARCH = build(9696, "操作失败, 查询不到对应的操作日志, 请联系客服人员!");
	
	/* memberType 9600 - 9649 */
	public static final ErrorCode TYPE_INSERT = build(9649, "操作失败, 添加会员类型信息失败, 请检查数据内容是否正确.");
	public static final ErrorCode TYPE_DELETE = build(9648, "操作失败, 修改充值操作日志失败, 该类型不存在或已被删除");
	public static final ErrorCode TYPE_UPDATE = build(9647, "操作失败, 修改充值操作日志失败, 请检查数据内容是否正确.");
	public static final ErrorCode TYPE_SET_ORDER_DISCOUNT = build(9647, "操作失败, 设置会员类型全单折扣信息失败, 未知错误.");
	public static final ErrorCode TYPE_DELETE_ISNOT_EMPTY = build(9646, "操作失败, 该类型下已有会员, 不允许删除.");
	
	private MemberError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.MEMBER, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.MEMBER, code);
	}
}

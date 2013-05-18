package com.wireless.exception;

public class ClientError extends ErrorEnum{
	/* code range : 9500 - 9599 */
	public static final ErrorCode INSERT_FAIL = build(9599, "操作失败, 未添加新客户资料, 请检查数据内容是否合法.");
	public static final ErrorCode DELETE_FAIL = build(9598, "操作失败, 未删除客户资料, 请检查数据内容是否合法.");
	public static final ErrorCode UPDATE_FAIL = build(9597, "操作失败, 未修改客户资料, 请检查数据内容是否合法.");
	public static final ErrorCode UPDATE_MEMBER_FAIL = build(9596, "操作失败, 更新客户已关联的会员账号信息失败.");
	public static final ErrorCode DELETE_MEMBER_FAIL = build(9595, "操作失败, 清除客户已关联的会员账号信息失败.");
	
	public static final ErrorCode TYPE_INSERT_FAIL = build(9594, "操作失败, 插入新客户类型信息失败, 请检查数据内容是否合法.");
	public static final ErrorCode TYPE_DELETE_FAIL = build(9593, "操作失败, 删除客户类型信息失败, 该类型不存在或已被删除.");
	public static final ErrorCode TYPE_UPDATE_FAIL = build(9592, "操作失败, 修改客户类型信息失败, 未知错误.");
	public static final ErrorCode TYPE_UPDATE_FAIL_IS_PARENT = build(9591, "操作失败, 该类型下还包含其他子类型.");
	public static final ErrorCode TYPE_UPDATE_FAIL_HAS_CLIENT = build(9590, "操作失败, 该类型下还有客户.");
	
	private ClientError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.PROTOCOL, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.PROTOCOL, code);
	}
	
}

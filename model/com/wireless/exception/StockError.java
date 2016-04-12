package com.wireless.exception;

public class StockError extends ErrorEnum{

	/**
	 * codeRange : 7750 - 7999
	 * stockAction :  7950 - 7999
	 * monthlyBalance : 7900 - 7949
	 * stockTake : 7850 - 7899
	 * stockActionDetail, stockTakeDetail : 7800 - 7849
	 * materialDept : 7750 - 7799
	 */
	/*stockAction 7950 - 7999*/
	public static final ErrorCode STOCKACTION_TIME_EARLIER = build(7999, "操作失败, 时间要小于工作月最后一天.");
	public static final ErrorCode STOCKACTION_TIME_LATER = build(7998, "操作失败, 时间要大于最后一次盘点时间或月结时间.");
	public static final ErrorCode STOCKACTION_AUDIT = build(7997, "操作失败, 审核失败, 此库单不存在.");
	public static final ErrorCode STOCKACTION_UNAUDIT = build(7996, "操作失败, 新增盘点失败, 还有未审核的库存单.");
	public static final ErrorCode STOCKACTION_CHECKING = build(7995, "操作失败, 正在盘点中, 不能审核库单.");
	public static final ErrorCode STOCK_ACTION_NOT_EXIST = build(7994, "操作失败, 删除失败, 此库单不存在.");
	public static final ErrorCode STOCKACTION_UPDATE = build(7992, "操作失败, 修改失败, 此库单不存在.");
	public static final ErrorCode STOCKACTION_CURRENTMONTH_UPDATE = build(7991, "操作失败, 月结失败, 还有未审核的库存单.");
	public static final ErrorCode STOCKACTION_INSERT = build(7990, "操作失败, 添加失败, 正在盘点中,不能新增库单.");
	public static final ErrorCode STOCK_ACTION_DETAIL_NOT_EXIST = build(7990, "操作失败, 此库存明细单不存在.");
	/*stockTake 7850 - 7899*/
	public static final ErrorCode STOCKTAKE_SELECT = build(7899, "操作失败, 查找失败, 此盘点单不存在.");
	public static final ErrorCode STOCKTAKE_DELETE = build(7898, "操作失败, 删除失败, 此盘点单不存在.");
	public static final ErrorCode STOCKTAKE_AUDIT = build(7897, "操作失败, 审核失败, 此盘点单不存在.");
	public static final ErrorCode STOCKTAKE_BALANCE = build(7896, "操作失败, 并无盘亏或盘盈.");
	public static final ErrorCode STOCKTAKE_UPDATE = build(7895, "操作失败, 修改失败, 此盘点单不存在.");
	public static final ErrorCode STOCKTAKE_SOMUCH = build(7894, "操作失败, 审核失败, 此部门下没有这么多原料.");
	public static final ErrorCode STOCKTAKE_UPDATE_AUDIT = build(7893, "操作失败, 修改失败, 此盘点单已审核.");
	public static final ErrorCode STOCKTAKE_BEFORE_INSERT = build(7892, "操作失败, 添加失败, 工作时间不是当前时间, 不能盘点.");
	public static final ErrorCode STOCKTAKE_CURRENTMONTH_UPDATE = build(7891, "操作失败, 月结失败, 还有未审核的盘点单.");
	public static final ErrorCode STOCKTAKE_HAVE_EXIST = build(7890, "操作失败, 添加失败, 此部门同样类别的货品正在盘点,无需重复添加.");
	/*stockTakeDetail 7800 - 7849*/
	public static final ErrorCode STOCKTAKE_DETAIL_SELECT = build(7849, "操作失败, 查找失败, 此盘点明细单不存在.");
	public static final ErrorCode STOCKTAKE_DETAIL_DELETE = build(7848, "操作失败, 删除失败, 此盘点明细单不存在.");
	public static final ErrorCode STOCKTAKE_DETAIL_UPDATE = build(7847, "操作失败, 修改失败, 此盘点明细单不存在.");
	public static final ErrorCode STOCKTAKE_NOT_MATERIAL = build(7846, "操作失败, 添加失败, 选了小类时,只能盘点这个小类下的原料或商品.");
	public static final ErrorCode STOCKTAKE_DETAIL_NOT_STOCKTAKE = build(7845, "操作失败, 审核失败, 还有盘漏的货品.");
	public static final ErrorCode STOCKTAKE_NOT_MATERIAL_TYPE = build(7844, "操作失败, 添加失败, 只能盘点原料或商品其中一个下的货品.");
	public static final ErrorCode STOCKACTION_DETAIL_SELECT = build(7829, "操作失败, 查找失败, 此库存明细单不存在.");
	public static final ErrorCode STOCKACTION_DETAIL_UPDATE = build(7928, "操作失败, 修改失败, 此库存明细单不存在.");
	public static final ErrorCode STOCKACTION_DETAIL_DELETE = build(7927, "操作失败, 删除失败, 此库存明细单不存在.");
	/*materialDept 7750 - 7799*/
	public static final ErrorCode MATERIAL_DEPT_ADD = build(7799, "操作失败, 查找失败, 此部门下还没添加这个原料.");
	public static final ErrorCode MATERIAL_DEPT_UPDATE = build(7798, "操作失败, 修改失败, 部门与材料没有这个匹配记录.");
	public static final ErrorCode MATERIAL_DEPT_EXIST = build(7797, "操作失败, 添加失败, 同部门之间不需要调拨.");
	public static final ErrorCode MATERIAL_DEPT_UPDATE_EXIST = build(7796, "操作失败, 修改失败, 同部门之间不需要调拨.");
	/*monthlyBalance 7900 - 7949*/
	public static final ErrorCode MONTHLY_BALANCE_NOT_EXIST = build(7949, "操作失败, 查找失败, 无此月结记录");
	public static final ErrorCode MONTHLY_BALANCE_ADD = build(7948, "操作失败, 添加失败, 数据库异常");
	public static final ErrorCode NOT_MONTHLY_BALANCE = build(7947, "当前月份不能月结, 请于下月初进行操作");
	
	private StockError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.STOCK, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.STOCK, code);
	}
}

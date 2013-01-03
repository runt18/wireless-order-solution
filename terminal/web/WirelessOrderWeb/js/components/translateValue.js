// 庫存類型code到描述轉換
var TYPE_CONSUME = 0; // 消耗
var TYPE_WEAR = 1; // 报损
var TYPE_SELL = 2; // 销售
var TYPE_RETURN = 3; // 退货
var TYPE_OUT_WARE = 4; // 出仓
var TYPE_INCOME = 5; // 入库
var TYPE_OUT = 6; // 调出
var TYPE_IN = 7; // 调入
var TYPE_CHECK = 8; // 盘点

function inventoryTypeCode2Descr(typeCode) {
	var typeN = "";
	if (typeCode == TYPE_CONSUME) {
		typeN = "消耗";
	} else if (typeCode == TYPE_WEAR) {
		typeN = "报损";
	} else if (typeCode == TYPE_SELL) {
		typeN = "销售";
	} else if (typeCode == TYPE_RETURN) {
		typeN = "退货";
	} else if (typeCode == TYPE_OUT_WARE) {
		typeN = "出仓";
	} else if (typeCode == TYPE_INCOME) {
		typeN = "入库";
	} else if (typeCode == TYPE_OUT) {
		typeN = "调出";
	} else if (typeCode == TYPE_IN) {
		typeN = "调入";
	} else if (typeCode == TYPE_CHECK) {
		typeN = "盘点";
	}
	return typeN;
}

// 餐台狀態code到描述轉換
var TABLE_IDLE = 0; // 空桌
var TABLE_BUSY = 1; // 占用

function tableStatusCode2Descr(code) {
	var descr = "";
	if (code == TABLE_IDLE) {
		descr = "空桌";
	} else if (code == TABLE_BUSY) {
		descr = "占用";
	}
	return descr;
}

// 餐台類型code到描述轉換
var CATE_NULL = 0; // 非就餐
var CATE_NORMAL = 1; // 一般
var CATE_TAKE_OUT = 2; // 外卖
var CATE_JOIN_TABLE = 3; // 拆台
var CATE_MERGER_TABLE = 4; // 并台
var CATE_GROUP_TABLE = 5; // 团体台

function tableCateCode2Descr(code) {
	var descr = "";
	if (code == CATE_NORMAL) {
		descr = "一般";
	} else if (code == CATE_TAKE_OUT) {
		descr = "外卖";
	} else if (code == CATE_JOIN_TABLE) {
		descr = "拆台";
	} else if (code == CATE_MERGER_TABLE) {
		descr = "并台";
	}else if (code == CATE_GROUP_TABLE) {
		descr = "团体桌";
	} else if (code == CATE_NULL) {
		descr = "非就餐";
	}
	return descr;
}

// 是否反結帳（is_paid）code到描述轉換
var NORMAL_PAY = 0;
var COUNTER_PAY = 1;

function norCounPayCode2Descr(code) {
	var descr = "";
	if (code == NORMAL_PAY) {
		descr = "否";
	} else if (code == COUNTER_PAY) {
		descr = "是";
	}
	return descr;
}
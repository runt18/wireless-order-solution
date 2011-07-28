function billQuery(in_queryTpye, in_operator, in_condition) {

	Ext.Ajax.request({
		url : "../QueryToday.do",
		params : {
			"pin" : pin,
			"type" : in_queryTpye,
			"ope" : in_operator,
			"value" : in_condition
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				var josnData = resultJSON.data;
				var billList = josnData.split("，");
				billsData.length = 0;
				for ( var i = 0; i < billList.length; i++) {
					var billInfo = billList[i]
							.substr(1, billList[i].length - 2).split(",");
					// 格式：["账单号","台号","日期","类型","结帐方式","金额","实收","台号2","就餐人数","最低消","服务费率","会员编号","会员姓名","账单备注","赠券金额","结帐类型","折扣类型"]
					// 后台格式：["账单号","台号","日期","类型","结帐方式","金额","实收","台号2","就餐人数","最低消","服务费率","会员编号","会员姓名","账单备注","赠券金额","结帐类型","折扣类型"]
					billsData.push([
							billInfo[0].substr(1, billInfo[0].length - 2),// 账单号
							billInfo[1].substr(1, billInfo[1].length - 2),// 台号
							billInfo[2].substr(1, billInfo[2].length - 2),// 日期
							billInfo[3].substr(1, billInfo[3].length - 2),// 类型
							billInfo[4].substr(1, billInfo[4].length - 2), // 结帐方式
							billInfo[5].substr(1, billInfo[5].length - 2), // 金额
							billInfo[6].substr(1, billInfo[6].length - 2), // 实收
							billInfo[7].substr(1, billInfo[7].length - 2), // 台号2
							billInfo[8].substr(1, billInfo[8].length - 2), // 就餐人数
							billInfo[9].substr(1, billInfo[9].length - 2), // 最低消
							billInfo[10].substr(1, billInfo[10].length - 2), // 服务费率
							billInfo[11].substr(1, billInfo[11].length - 2), // 会员编号
							billInfo[12].substr(1, billInfo[12].length - 2), // 会员姓名
							billInfo[13].substr(1, billInfo[13].length - 2), // 账单备注
							billInfo[14].substr(1, billInfo[14].length - 2), // 赠券金额
							billInfo[15].substr(1, billInfo[15].length - 2), // 结帐类型
							billInfo[16].substr(1, billInfo[16].length - 2) // 折扣类型
					]);
				}
				billsStore.reload();
			}
		},
		failure : function(response, options) {
		}
	});
};

function billQueryHandler() {
	var queryTpye = document.getElementById("type").value;
	var queryOperator = document.getElementById("operator").value;
	var queryValue = document.getElementById("condition").value;

	billQuery(queryTpye, queryOperator, queryValue);

};


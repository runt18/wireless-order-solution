﻿function billQuery(in_queryTpye, in_operator, in_condition) {

	Ext.Ajax
			.request({
				url : "../../QueryHistory.do",
				params : {
					"pin" : pin,
					"type" : in_queryTpye,
					"ope" : in_operator,
					"value" : in_condition
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON
							.decode(response.responseText);
					if (resultJSON.success == true) {
						var josnData = resultJSON.data;
						if (josnData != "") {
							var billList = josnData.split("，");
							billsData.length = 0;
							for ( var i = 0; i < billList.length; i++) {
								var billInfo = billList[i].substr(1,
										billList[i].length - 2).split(",");
								// 格式：["账单号","台号","日期","类型","结帐方式","金额","实收","台号2","就餐人数","最低消","服务费率","会员编号","会员姓名","账单备注","赠券金额","结帐类型","折扣类型","服务员",是否反結帳]
								// 后台格式：["账单号","台号","日期","类型","结帐方式","金额","实收","台号2","就餐人数","最低消","服务费率","会员编号","会员姓名","账单备注","赠券金额","结帐类型","折扣类型","服务员",是否反結帳]
								billsData.push([
										billInfo[0].substr(1,
												billInfo[0].length - 2),// 账单号
										billInfo[1].substr(1,
												billInfo[1].length - 2),// 台号
										billInfo[2].substr(1,
												billInfo[2].length - 2),// 日期
										billInfo[3].substr(1,
												billInfo[3].length - 2),// 类型
										billInfo[4].substr(1,
												billInfo[4].length - 2), // 结帐方式
										billInfo[5].substr(1,
												billInfo[5].length - 2), // 金额
										billInfo[6].substr(1,
												billInfo[6].length - 2), // 实收
										billInfo[7].substr(1,
												billInfo[7].length - 2), // 台号2
										billInfo[8].substr(1,
												billInfo[8].length - 2), // 就餐人数
										billInfo[9].substr(1,
												billInfo[9].length - 2), // 最低消
										billInfo[10].substr(1,
												billInfo[10].length - 2), // 服务费率
										billInfo[11].substr(1,
												billInfo[11].length - 2), // 会员编号
										billInfo[12].substr(1,
												billInfo[12].length - 2), // 会员姓名
										billInfo[13].substr(1,
												billInfo[13].length - 2), // 账单备注
										billInfo[14].substr(1,
												billInfo[14].length - 2), // 赠券金额
										billInfo[15].substr(1,
												billInfo[15].length - 2), // 结帐类型
										billInfo[16].substr(1,
												billInfo[16].length - 2), // 折扣类型
										billInfo[17].substr(1,
												billInfo[17].length - 2), // 服务员
										billInfo[18] // 是否反結帳
								]);

							}

							// sum the prices
							var sumShouldPay = 0;
							var sumActualPay = 0;
							for ( var i = 0; i < billsData.length; i++) {
								sumShouldPay = sumShouldPay
										+ parseFloat(billsData[i][5]);
								sumActualPay = sumActualPay
										+ parseFloat(billsData[i][6]);
							}
							document.getElementById("shouldPaySum").innerHTML = sumShouldPay
									.toFixed(2);
							document.getElementById("actualPaySum").innerHTML = sumActualPay
									.toFixed(2);

						} else {
							billsData.length = 0;
						}
						billsStore.reload();
					}
				},
				failure : function(response, options) {
				}
			});
};

function billQueryHandler() {
	var queryTpye = filterTypeComb.getValue();
	if (queryTpye == "全部") {
		queryTpye = 0;
	}

	var queryOperator = operatorComb.getValue();
	if (queryOperator == "等于") {
		queryOperator = 1;
	}

	var queryValue = "";
	if (conditionType == "text" && queryTpye != 0) {
		queryValue = searchForm.findById("conditionText").getValue();
	} else if (conditionType == "number") {
		queryValue = searchForm.findById("conditionNumber").getValue();
	} else if (conditionType == "date") {
		queryValue = new Date();
		queryValue = searchForm.findById("conditionDate").getValue();
		// queryValue = queryValue.format("H:i:s");
	} else if (conditionType == "tableTypeComb") {
		queryValue = searchForm.findById("tableTypeComb").getValue();
		if (queryValue == "一般") {
			queryValue = 1;
		}
	} else if (conditionType == "payTypeComb") {
		queryValue = searchForm.findById("payTypeComb").getValue();
		if (queryValue == "现金") {
			queryValue = 1;
		}
	}
	// alert(queryTpye + " , " + queryOperator + " , " + queryValue);

	var isInputValid = true;
	if (conditionType == "text" && queryTpye != 0) {
		isInputValid = searchForm.findById("conditionText").isValid();
	} else if (conditionType == "number") {
		isInputValid = searchForm.findById("conditionNumber").isValid();
	} else if (conditionType == "date") {
		isInputValid = searchForm.findById("conditionDate").isValid();
	} else if (conditionType == "tableTypeComb") {
		isInputValid = searchForm.findById("tableTypeComb").isValid();
	} else if (conditionType == "payTypeComb") {
		isInputValid = searchForm.findById("payTypeComb").isValid();
	}

	if (isInputValid) {

		billQuery(queryTpye, queryOperator, queryValue);
	}

	currRowIndex = -1;

};

var billListRefresh = function() {
	var discountValue = billGenModForm.getForm().findField("discountRadio")
			.getGroupValue();
	discountType = 0;
	// 获取discountType
	if (discountValue == "discount1") {
		discountType = 1;
	} else if (discountValue == "discount2") {
		discountType = 2;
	} else {
		discountType = 3;
	}
	// 一般、会员、0、1
	// 获取payType
	var payTypeValue = billGenModForm.findById("payTpye").getValue();
	var discountIndex = -1;
	// discountData [厨房编号,一般折扣1,一般折扣2,一般折扣3,会员折扣1,会员折扣2,会员折扣3]
	if ((payTypeValue == "一般" || payTypeValue == 0) && discountType == 1) {
		discountIndex = 1;
		payType = 1;
	} else if ((payTypeValue == "一般" || payTypeValue == 0) && discountType == 2) {
		discountIndex = 2;
		payType = 1;
	} else if ((payTypeValue == "一般" || payTypeValue == 0) && discountType == 3) {
		discountIndex = 3;
		payType = 1;
	} else if ((payTypeValue == "会员" || payTypeValue == 1) && discountType == 1) {
		discountIndex = 4;
		payType = 2;
	} else if ((payTypeValue == "会员" || payTypeValue == 1) && discountType == 2) {
		discountIndex = 5;
		payType = 2;
	} else if ((payTypeValue == "会员" || payTypeValue == 1) && discountType == 3) {
		discountIndex = 6;
		payType = 2;
	}

	// 显示
	for ( var i = 0; i < orderedData.length; i++) {
		var KitchenNum = orderedData[i][7];
		var discountRate = 1;
		for ( var j = 0; j < discountData.length; j++) {
			if (KitchenNum == discountData[j][0]) {
				discountRate = discountData[j][discountIndex];
			}
		}
		// alert(orderedData[i][12]);
		if (orderedData[i][9] == "true" || orderedData[i][12] == "true"
				|| orderedData[i][18] == "true") {
			// 特价，送，臨時菜 不打折
			orderedData[i][13] = "1.0";
		} else {
			// 非 特价，送，臨時菜
			orderedData[i][13] = discountRate;
		}
	}
	orderedStore.reload();
};

function billQuery(in_queryTpye, in_operator, in_condition, in_additionalFilter) {

	Ext.Ajax
			.request({
				url : "../../QueryHistory.do",
				params : {
					"pin" : pin,
					"type" : in_queryTpye,
					"ope" : in_operator,
					"value" : in_condition,
					"havingCond" : in_additionalFilter,
					"isPaging" : false
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON
							.decode(response.responseText);
					var rootData = resultJSON.root;
					if (rootData.length != 0) {
						if (rootData[0].message == "normal") {
							billJSON = rootData.slice(0);

							// 格式：["账单号","台号","日期","类型","结帐方式","金额","实收","台号2","就餐人数","最低消","服务费率","会员编号","会员姓名","账单备注","赠券金额","结帐类型","折扣类型","服务员",是否反結帳,"服务员",
							// 是否反結帳, 是否折扣, 是否赠送, 是否退菜, "流水号"]
							// 后台格式：["账单号","台号","日期","类型","结帐方式","金额","实收","台号2","就餐人数","最低消","服务费率","会员编号","会员姓名","账单备注","赠券金额","结帐类型","折扣类型","服务员",是否反結帳,"服务员",
							// 是否反結帳, 是否折扣, 是否赠送, 是否退菜, "流水号"]
//							for ( var i = 0; i < billJSON.length; i++) {
//								billsData.push([ billJSON[i].orderID,// 账单号
//								billJSON[i].tableAlias,// 台号
//								billJSON[i].orderDate,// 日期
//								billJSON[i].orderCategory,// 类型
//								billJSON[i].payManner, // 结帐方式
//								billJSON[i].totalPrice, // 金额
//								billJSON[i].actualIncome, // 实收
//								billJSON[i].table2Alias, // 台号2
//								billJSON[i].customerNum, // 就餐人数
//								billJSON[i].minCost, // 最低消
//								billJSON[i].serviceRate, // 服务费率
//								billJSON[i].giftPrice, // 会员编号
//								billJSON[i].member, // 会员姓名
//								billJSON[i].comment, // 账单备注
//								billJSON[i].giftPrice, // 赠券金额
//								billJSON[i].payType, // 结帐类型
//								billJSON[i].discountType, // 折扣类型
//								billJSON[i].staff, // 服务员
//								billJSON[i].isPaid, // 是否反結帳
//								billJSON[i].isDiscount, // 是否折扣
//								billJSON[i].isGift, // 是否赠送
//								billJSON[i].isCancel, // 是否退菜
//								billJSON[i].seqID // 流水号
//								]);
//
//							}

							// sum the prices
							var sumShouldPay = 0;
							var sumActualPay = 0;
							for ( var i = 0; i < billJSON.length; i++) {
								sumShouldPay = sumShouldPay
										+ parseFloat(billJSON[i].totalPrice);
								sumActualPay = sumActualPay
										+ parseFloat(billJSON[i].actualIncome);
							}
							document.getElementById("shouldPaySum").innerHTML = sumShouldPay
									.toFixed(2);
							document.getElementById("actualPaySum").innerHTML = sumActualPay
									.toFixed(2);

						} else {
							Ext.MessageBox.show({
								msg : rootData[0].message,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						}
					}
				}
			});
	// if (resultJSON.success == true) {
	// var josnData = resultJSON.data;
	// if (josnData != "") {
	// var billList = josnData.split("，");
	// billsData.length = 0;
	// for ( var i = 0; i < billList.length; i++) {
	// var billInfo = billList[i].substr(1,
	// billList[i].length - 2).split(",");
	// //
	// 格式：["账单号","台号","日期","类型","结帐方式","金额","实收","台号2","就餐人数","最低消","服务费率","会员编号","会员姓名","账单备注","赠券金额","结帐类型","折扣类型","服务员",是否反結帳,"服务员",
	// // 是否反結帳, 是否折扣, 是否赠送, 是否退菜, "流水号"]
	// //
	// 后台格式：["账单号","台号","日期","类型","结帐方式","金额","实收","台号2","就餐人数","最低消","服务费率","会员编号","会员姓名","账单备注","赠券金额","结帐类型","折扣类型","服务员",是否反結帳,"服务员",
	// // 是否反結帳, 是否折扣, 是否赠送, 是否退菜, "流水号"]
	// billsData.push([
	// billInfo[0].substr(1,
	// billInfo[0].length - 2),// 账单号
	// billInfo[1].substr(1,
	// billInfo[1].length - 2),// 台号
	// billInfo[2].substr(1,
	// billInfo[2].length - 2),// 日期
	// billInfo[3].substr(1,
	// billInfo[3].length - 2),// 类型
	// billInfo[4].substr(1,
	// billInfo[4].length - 2), // 结帐方式
	// billInfo[5].substr(1,
	// billInfo[5].length - 2), // 金额
	// billInfo[6].substr(1,
	// billInfo[6].length - 2), // 实收
	// billInfo[7].substr(1,
	// billInfo[7].length - 2), // 台号2
	// billInfo[8].substr(1,
	// billInfo[8].length - 2), // 就餐人数
	// billInfo[9].substr(1,
	// billInfo[9].length - 2), // 最低消
	// billInfo[10].substr(1,
	// billInfo[10].length - 2), // 服务费率
	// billInfo[11].substr(1,
	// billInfo[11].length - 2), // 会员编号
	// billInfo[12].substr(1,
	// billInfo[12].length - 2), // 会员姓名
	// billInfo[13].substr(1,
	// billInfo[13].length - 2), // 账单备注
	// billInfo[14].substr(1,
	// billInfo[14].length - 2), // 赠券金额
	// billInfo[15].substr(1,
	// billInfo[15].length - 2), // 结帐类型
	// billInfo[16].substr(1,
	// billInfo[16].length - 2), // 折扣类型
	// billInfo[17].substr(1,
	// billInfo[17].length - 2), // 服务员
	// billInfo[18], // 是否反結帳
	// billInfo[19], // 是否折扣
	// billInfo[20], // 是否赠送
	// billInfo[21], // 是否退菜
	// billInfo[22].substr(1,
	// billInfo[22].length - 2) // 流水号
	// ]);
	//
	// }
	//
	// // sum the prices
	// var sumShouldPay = 0;
	// var sumActualPay = 0;
	// for ( var i = 0; i < billsData.length; i++) {
	// sumShouldPay = sumShouldPay
	// + parseFloat(billsData[i][5]);
	// sumActualPay = sumActualPay
	// + parseFloat(billsData[i][6]);
	// }
	// document.getElementById("shouldPaySum").innerHTML = sumShouldPay
	// .toFixed(2);
	// document.getElementById("actualPaySum").innerHTML = sumActualPay
	// .toFixed(2);
	//
	// } else {
	// billsData.length = 0;
	// }
	// billsStore.reload();
	// }
	// },
	// failure : function(response, options) {
	// }
	// }
	// );
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
	if (conditionType == "text" && queryTpye != 0
			&& queryTpye != 9) {
		queryValue = searchForm.findById("conditionText")
				.getValue();
	} else if (conditionType == "number") {
		queryValue = searchForm.findById("conditionNumber")
				.getValue();
	} else if (conditionType == "date") {
		var dateFormated = new Date();
		queryValue = searchForm.findById("conditionDate")
				.getValue();
		dateFormated = queryValue;
		queryValue = dateFormated.format('Y-m-d');
		// queryValue = queryValue + " 00:00:00";
	} else if (conditionType == "tableTypeComb") {
		queryValue = searchForm.findById("tableTypeComb")
				.getValue();
		if (queryValue == "一般") {
			queryValue = 1;
		}
	} else if (conditionType == "payTypeComb") {
		queryValue = searchForm.findById("payTypeComb")
				.getValue();
		if (queryValue == "现金") {
			queryValue = 1;
		}
	}

	// -- 獲取額外過濾條件--
	var additionFilter = 0;
	if (billsQueryCondPanel.getForm().findField(
			"conditionRadio") != null) {
		var conditionRadio = billsQueryCondPanel.getForm()
				.findField("conditionRadio")
				.getGroupValue();
		if (conditionRadio == "all") {
			additionFilter = 0;
		} else if (conditionRadio == "isPaid") {
			additionFilter = 1;
		} else if (conditionRadio == "discount") {
			additionFilter = 2;
		} else if (conditionRadio == "gift") {
			additionFilter = 3;
		} else if (conditionRadio == "return") {
			additionFilter = 4;
		}
	}

	var isInputValid = true;
	if (conditionType == "text" && queryTpye != 0 && queryTpye != 9) {
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

		billsStore.reload({
			params : {
				"start" : 0,
				"limit" : billRecordCount,
				"pin" : pin,
				"type" : queryTpye,
				"ope" : queryOperator,
				"value" : queryValue,
				"havingCond" : additionFilter,
				"isPaging" : true,
				"queryType" : "normal"
			}
		});
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

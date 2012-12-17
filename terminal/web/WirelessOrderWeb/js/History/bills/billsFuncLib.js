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
					"isPaging" : false,
					"queryType" : "normal"
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON
							.decode(response.responseText);
					var rootData = resultJSON.root;
					if (rootData.length != 0) {
						if (rootData[0].message == "normal") {
							billJSON = rootData.slice(0);
							
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

//		billsStore.reload({
//			params : {
//				"start" : 0,
//				"limit" : billRecordCount,
//				"pin" : pin,
//				"type" : queryTpye,
//				"ope" : queryOperator,
//				"value" : queryValue,
//				"havingCond" : additionFilter,
//				"isPaging" : true,
//				"queryType" : "normal"
//			}
//		});
		
		queryType = "normal";
		billsStore.reload({
			params : {
				"start" : 0,
				"limit" : billRecordCount
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

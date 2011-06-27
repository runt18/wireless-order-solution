var paySubmit = function(submitType) {
	var canSubmit = true;
	// var actualPrice = checkOutForm.findById("actualCount").getValue();
	var actualPrice = document.getElementById("actualCount").value;
	var countPriceString = document.getElementById("totalCount").innerHTML;
	var shouldPay = document.getElementById("shouldPay").innerHTML;
	var countPrice = countPriceString.substr(countPriceString.indexOf("￥") + 1,
			countPriceString.length - countPriceString.indexOf("￥") - 7);
	var submitPrice = -1;

	var payManner = -1;
	var tempPay;

	// 现金
	if (submitType == 1) {
		submitPrice = shouldPay;
	} else {
		submitPrice = countPrice;
	}

	// 暂结，调整参数
	if (submitType == 6) {
		tempPay = "true";
		payManner = 1;
	} else {
		tempPay = "";
		payManner = submitType;
	}

	// 会员卡结帐，检查余额；现金校验
	if (submitType == 3 && parseFloat(countPrice) > parseFloat(mBalance)
			&& payType == 2) {
		Ext.Msg.alert("", "<b>会员卡余额小于合计金额，不能结帐！</b>");
		canSubmit = false;
	} else if (submitType == 1
			&& parseFloat(actualPrice) < parseFloat(countPrice)) {
		Ext.Msg.alert("", "<b>现金实收金额小于合计金额，不能结帐！</b>");
		canSubmit = false;
	}

	var Request = new URLParaQuery();
	if (canSubmit) {
		Ext.Ajax.request({
			url : "../PayOrder.do",
			params : {
				"pin" : Request["pin"],
				"tableID" : Request["tableNbr"],
				"cashIncome" : submitPrice,
				"payType" : payType,
				"discountType" : discountType,
				"payManner" : payManner,
				"tempPay" : tempPay,
				"memberID" : actualMemberID,
				"comment" : checkOutForm.findById("remark").getValue()
			},
			success : function(response, options) {
				var resultJSON = Ext.util.JSON.decode(response.responseText);
				if (resultJSON.success == true) {
					var dataInfo = resultJSON.data;
					Ext.MessageBox.show({
						msg : dataInfo,
						width : 300,
						buttons : Ext.MessageBox.OK,
						fn : function() {
							var Request = new URLParaQuery();
							if (submitType != 6) {
								location.href = "TableSelect.html?pin="
										+ Request["pin"] + "&restaurantID="
										+ restaurantID;
							}
						}
					});
				} else {
					var dataInfo = resultJSON.data;
					Ext.MessageBox.show({
						msg : dataInfo,
						width : 300,
						buttons : Ext.MessageBox.OK
					});
				}
			},
			failure : function(response, options) {
			}
		});
	}
};

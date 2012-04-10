var paySubmit = function(submitType) {

	var canSubmit = true;
	// var actualPrice = checkOutForm.findById("actualCount").getValue();
	var actualPrice = document.getElementById("actualCount").value;
	var countPrice = document.getElementById("totalCount").innerHTML;
	var shouldPay = document.getElementById("shouldPay").innerHTML;
	var serviceRate = document.getElementById("serviceCharge").value;
	var submitPrice = -1;

	var payManner = -1;
	var tempPay;

	// 现金
	if (submitType == 1) {
		submitPrice = actualPrice;
	} else {
		submitPrice = originalTotalCount;
	}

	// 暂结，调整参数
	if (submitType == 6) {
		tempPay = "true";
		payManner = 1;
	} else {
		tempPay = "";
		payManner = submitType;
	}

	if (serviceRate < 0 || serviceRate > 100) {
		checkOutForm.buttons[0].setDisabled(false);
		checkOutForm.buttons[1].setDisabled(false);
		checkOutForm.buttons[2].setDisabled(false);
		checkOutForm.buttons[3].setDisabled(false);
		checkOutForm.buttons[4].setDisabled(false);
		checkOutForm.buttons[5].setDisabled(false);
		Ext.Msg.alert("", "<b>服务费率范围是0%至100%！</b>");
		canSubmit = false;
	}

	// 会员卡结帐，检查余额；现金校验
	if (submitType == 3 && parseFloat(countPrice) > parseFloat(mBalance)
			&& payType == 2) {
		checkOutForm.buttons[0].setDisabled(false);
		checkOutForm.buttons[1].setDisabled(false);
		checkOutForm.buttons[2].setDisabled(false);
		checkOutForm.buttons[3].setDisabled(false);
		checkOutForm.buttons[4].setDisabled(false);
		checkOutForm.buttons[5].setDisabled(false);
		Ext.Msg.alert("", "<b>会员卡余额小于合计金额，不能结帐！</b>");
		canSubmit = false;
	} else if (submitType == 1
			&& parseFloat(actualPrice) < parseFloat(shouldPay)) {
		checkOutForm.buttons[0].setDisabled(false);
		checkOutForm.buttons[1].setDisabled(false);
		checkOutForm.buttons[2].setDisabled(false);
		checkOutForm.buttons[3].setDisabled(false);
		checkOutForm.buttons[4].setDisabled(false);
		checkOutForm.buttons[5].setDisabled(false);
		Ext.Msg.alert("", "<b>实缴金额小于应收金额，不能结帐！</b>");
		canSubmit = false;
	}

	var Request = new URLParaQuery();
	if (canSubmit) {
		Ext.Ajax.request({
			url : "../../PayOrder.do",
			params : {
				"pin" : Request["pin"],
				"tableID" : Request["tableNbr"],
				"cashIncome" : submitPrice,
				"payType" : payType,
				"discountType" : discountType,
				"payManner" : payManner,
				"tempPay" : tempPay,
				"memberID" : actualMemberID,
				"comment" : checkOutForm.findById("remark").getValue(),
				"serviceRate" : serviceRate
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
					checkOutForm.buttons[0].setDisabled(false);
					checkOutForm.buttons[1].setDisabled(false);
					checkOutForm.buttons[2].setDisabled(false);
					checkOutForm.buttons[3].setDisabled(false);
					checkOutForm.buttons[4].setDisabled(false);
					checkOutForm.buttons[5].setDisabled(false);
				} else {

					checkOutForm.buttons[0].setDisabled(false);
					checkOutForm.buttons[1].setDisabled(false);
					checkOutForm.buttons[2].setDisabled(false);
					checkOutForm.buttons[3].setDisabled(false);
					checkOutForm.buttons[4].setDisabled(false);
					checkOutForm.buttons[5].setDisabled(false);

					var dataInfo = resultJSON.data;
					Ext.MessageBox.show({
						msg : dataInfo,
						width : 300,
						buttons : Ext.MessageBox.OK
					});
				}
			},
			failure : function(response, options) {
				checkOutForm.buttons[0].setDisabled(false);
				checkOutForm.buttons[1].setDisabled(false);
				checkOutForm.buttons[2].setDisabled(false);
				checkOutForm.buttons[3].setDisabled(false);
				checkOutForm.buttons[4].setDisabled(false);
				checkOutForm.buttons[5].setDisabled(false);

				Ext.MessageBox.show({
					msg : "Unknow page error",
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		});
	}

};

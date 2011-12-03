var getMemberInfo = function(memberNbr) {

	var Request = new URLParaQuery();
	Ext.Ajax
			.request({
				url : "../QueryMember.do",
				params : {
					"pin" : Request["pin"],
					"memberID" : memberNbr
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON
							.decode(response.responseText);
					if (resultJSON.success == true) {
						// 1,update the menber info
						var josnData = resultJSON.data;
						var memberList = josnData.split(",");
						var menberName = memberList[0].substr(1,
								memberList[0].length - 2);
						var menberPhone = memberList[1].substr(1,
								memberList[1].length - 2);
						var menberBalance = memberList[2];
						mBalance = menberBalance;
						document.getElementById("memberNbr").innerHTML = memberNbr
								+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
						document.getElementById("memberName").innerHTML = menberName
								+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
						document.getElementById("memberPhone").innerHTML = menberPhone
								+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
						document.getElementById("memberBalance").innerHTML = menberBalance
								+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

						// 2,display the menber info panel
						checkOutForm.findById("memberInfoPanel").show();

						// 3,hide the menber number input window
						memberNbrInputWin.hide();

						// 4,mark the menber id
						actualMemberID = memberNbr;

						// 5,refresh the general bill info
						checkOurListRefresh();
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

};

var checkOurListRefresh = function() {
	var discountValue = checkOutForm.getForm().findField("discountRadio")
			.getGroupValue();
	discountType = 0;
	if (discountValue == "discount1") {
		discountType = 1;
	} else if (discountValue == "discount2") {
		discountType = 2;
	} else {
		discountType = 3;
	}
	// 一般、0、1
	var payTypeValue = checkOutForm.findById("payTpye").getValue();
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
	} else if (payTypeValue == 1 && discountType == 1) {
		discountIndex = 4;
		payType = 2;
	} else if (payTypeValue == 1 && discountType == 2) {
		discountIndex = 5;
		payType = 2;
	} else if (payTypeValue == 1 && discountType == 3) {
		discountIndex = 6;
		payType = 2;
	}

	// 显示
	checkOutDataDisplay.length = 0;
	for ( var i = 0; i < checkOutData.length; i++) {
		var KitchenNum = checkOutData[i][0];
		var discountRate = 1;
		for ( var j = 0; j < discountData.length; j++) {
			if (KitchenNum == discountData[j][0]) {
				discountRate = discountData[j][discountIndex];
			}
		}

		var tastePrice = checkOutData[i][9];
		// var tastePrice = 0;
		// for ( var j = 0; j < dishTasteData.length; j++) {
		// if (dishTasteData[j][0] == checkOutData[i][2]) {
		// tastePrice = parseFloat(dishTasteData[j][1].substr(1,
		// dishTasteData[j][1].length - 1));
		// }
		// }

		// 总价 = （原料价 * 折扣率 + 口味价）* 数量
		var price;
		if (checkOutData[i][5] == "true" || checkOutData[i][8] == "true") {
			// 特价，送 不打折
			price = parseFloat(checkOutData[i][4].substring(1))
					* checkOutData[i][3];
		} else {
			// 非特价

			price = (parseFloat(checkOutData[i][10]) * discountRate + parseFloat(tastePrice))
					* checkOutData[i][3];
		}
		var priceDisplay = "￥" + price.toFixed(2);

		// 送 -- 折扣率 --1
		if (checkOutData[i][8] == "true" || checkOutData[i][5] == "true") {
			checkOutDataDisplay.push([ checkOutData[i][1], checkOutData[i][2],
					checkOutData[i][3], checkOutData[i][4],
					parseFloat("1").toFixed(2), priceDisplay, // 实价
					checkOutData[i][5],// 特
					checkOutData[i][6],// 荐
					checkOutData[i][7], // 停
					checkOutData[i][8], // 送
					checkOutData[i][11] // 時
			]);
		} else {
			checkOutDataDisplay.push([ checkOutData[i][1], checkOutData[i][2],
					checkOutData[i][3], checkOutData[i][4],
					parseFloat(discountRate).toFixed(2), priceDisplay, // 实价
					checkOutData[i][5],// 特
					checkOutData[i][6],// 荐
					checkOutData[i][7], // 停
					checkOutData[i][8], // 送
					checkOutData[i][11] // 時
			]);
		}
	}

	// 根据“特荐停”重新写菜名
	for ( var i = 0; i < checkOutDataDisplay.length; i++) {
		if (checkOutDataDisplay[i][6] == "true") {
			// 特
			checkOutDataDisplay[i][0] = checkOutDataDisplay[i][0]
					+ "<img src='../images/icon_tip_te.gif'></img>";
		}
		if (checkOutDataDisplay[i][7] == "true") {
			// 荐
			checkOutDataDisplay[i][0] = checkOutDataDisplay[i][0]
					+ "<img src='../images/icon_tip_jian.gif'></img>";
		}
		if (checkOutDataDisplay[i][8] == "true") {
			// 停
			checkOutDataDisplay[i][0] = checkOutDataDisplay[i][0]
					+ "<img src='../images/icon_tip_ting.gif'></img>";
		}
		if (checkOutDataDisplay[i][9] == "true") {
			// 送
			checkOutDataDisplay[i][0] = checkOutDataDisplay[i][0]
					+ "<img src='../images/forFree.png'></img>";
		}
		if (checkOutDataDisplay[i][10] == "true") {
			// 時
			checkOutDataDisplay[i][0] = checkOutDataDisplay[i][0]
					+ "<img src='../images/currPrice.png'></img>";
		}
	}

	checkOutStore.reload();

	// 算总价
	var totalCount = 0;
	var forFreeCount = 0;
	for ( var i = 0; i < checkOutDataDisplay.length; i++) {
		var singleCount = parseFloat(checkOutDataDisplay[i][5].substr(1));
		if (checkOutDataDisplay[i][9] == "true") {
			// forFreeCount = forFreeCount + singleCount;
			// for free count dont need discount
			forFreeCount = forFreeCount
					+ parseFloat(checkOutData[i][4].substring(1))
					* checkOutData[i][3];
		} else {
			totalCount = totalCount + singleCount;
		}
	}
	totalCount = totalCount.toFixed(2);
	forFreeCount = forFreeCount.toFixed(2);
	originalTotalCount = totalCount;

	document.getElementById("totalCount").innerHTML = totalCount;
	document.getElementById("forFree").innerHTML = forFreeCount;
	// //document.getElementById("actualCount").value = totalCount;
	// document.getElementById("shouldPay").innerHTML = totalCount;
	// var sPay = document.getElementById("shouldPay").innerHTML;
	// if (restaurantData[0][5] == 2) {
	// sPay = sPay.substr(0, sPay.indexOf(".")) + ".00";
	// } else if (restaurantData[0][5] == 3) {
	// sPay = parseFloat(sPay).toFixed(0) + ".00";
	// }
	// document.getElementById("shouldPay").innerHTML = sPay;
	// document.getElementById("change").innerHTML =
	// (parseFloat(totalCount)-parseFloat(sPay)).toFixed(2);
	// // checkOutForm.findById("actualCount").setValue(totalCount);

	moneyCount("radio");

};
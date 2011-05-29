﻿var getMemberInfo = function(memberNbr) {

	var Request = new URLParaQuery();
	Ext.Ajax.request({
		url : "../QueryMember.do",
		params : {
			"pin" : Request["pin"],
			"memberID" : memberNbr
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				// 1,update the menber info
				var josnData = resultJSON.data;
				var memberList = josnData.split(",");
				var menberName = memberList[0].substr(1,
						memberList[0].length - 2);
				var menberPhone = memberList[1].substr(1,
						memberList[1].length - 2);
				document.getElementById("memberNbr").innerHTML = memberNbr
						+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
				document.getElementById("memberName").innerHTML = menberName
						+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
				document.getElementById("memberPhone").innerHTML = menberPhone
						+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

				// 2,display the menber info panel
				checkOutForm.findById("memberInfoPanel").show();

				// 3,hide the menber number input window
				memberNbrInputWin.hide();

				// 4,mark the menber id
				actualMemberID = memberNbr;
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

		var price = parseFloat(checkOutData[i][4].substring(1)) * discountRate
				* checkOutData[i][3];
		var priceDisplay = checkOutData[i][4].substring(0, 1)
				+ price.toFixed(2);

		checkOutDataDisplay.push([ checkOutData[i][1], checkOutData[i][2],
				checkOutData[i][3], checkOutData[i][4], discountRate,
				priceDisplay ]);
	}

	checkOutStore.reload();

	// 算总价
	var totalCount = 0;
	for ( var i = 0; i < checkOutDataDisplay.length; i++) {
		var singleCount = parseFloat(checkOutDataDisplay[i][5].substr(1));
		totalCount = totalCount + singleCount;
	}
	totalCount = totalCount.toFixed(2);
	if (document.getElementById("totalCount") != undefined
			&& checkOutForm.findById("actualCount") != undefined) {
		document.getElementById("totalCount").innerHTML = "<div style='font-size:18px;font-weight:bold;'>合计：       ￥"
				+ totalCount + "</div>";
		checkOutForm.findById("actualCount").setValue(totalCount);
	}
};
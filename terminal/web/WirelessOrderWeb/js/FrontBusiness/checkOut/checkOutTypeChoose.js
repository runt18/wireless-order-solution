var getMemberInfo = function(memberNbr) {

	var Request = new URLParaQuery();
	Ext.Ajax.request({
		url : "../../QueryMember.do",
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
				var menberName = memberList[0].substr(1, memberList[0].length - 2);
				var menberPhone = memberList[1].substr(1, memberList[1].length - 2);
				var menberBalance = memberList[2];
				mBalance = menberBalance;
				Ext.getDom("memberNbr").innerHTML = memberNbr + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
				Ext.getDom("memberName").innerHTML = menberName + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
				Ext.getDom("memberPhone").innerHTML = menberPhone + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
				Ext.getDom("memberBalance").innerHTML = menberBalance + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

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
		failure : function(response, options) { }
	});
};

var checkOurListRefresh = function() {
	var discountValue = checkOutForm.getForm().findField("discountRadio").getGroupValue();
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
	checkOutDataDisplay.root = [];
	if(typeof(checkOutData.root) == 'undefined'){
		return;
	}
	
	for ( var i = 0; i < checkOutData.root.length; i++) {
		var tpItem = checkOutData.root[i];
		var KitchenNum = tpItem.kitchenId;
		var discountRate = 1;
		for ( var j = 0; j < discountData.length; j++) {
			if (KitchenNum == discountData[j][0]) {
				discountRate = discountData[j][discountIndex];
			}
		}
		
		// 特送臨 -- 折扣率 --1
		if (tpItem.special == true || tpItem.gift == true || tpItem.temporary == true) {
			tpItem.discount = parseFloat("1").toFixed(2);
		} else {
			tpItem.discount = parseFloat(discountRate).toFixed(2);
		}
		
		if(tpItem.special == true || tpItem.gift == true){
			// 特价和赠送菜品不打折
			tpItem.totalPrice = parseFloat(tpItem.unitPrice * tpItem.count);
		}else{
			tpItem.totalPrice = parseFloat((tpItem.unitPrice * tpItem.discount + tpItem.tastePrice) * tpItem.count);
		}
				
		checkOutDataDisplay.root.push(tpItem);
	}

	// 根据“特荐停”重新写菜名
	for ( var i = 0; i < checkOutDataDisplay.root.length; i++) {
		var tpItem = checkOutDataDisplay.root[i];		
		tpItem.foodName = (tpItem.foodName.indexOf('<') > 0 ? tpItem.foodName.substring(0, tpItem.foodName.indexOf('<')) : tpItem.foodName);
		if (tpItem.special == true) {
			// 特
			tpItem.foodName = tpItem.foodName + "<img src='../../images/icon_tip_te.png'></img>";
		}
		if (tpItem.recommed == true) {
			// 荐
			tpItem.foodName = tpItem.foodName + "<img src='../../images/icon_tip_jian.png'></img>";
		}
		if (tpItem.soldout == true) {
			// 停
			tpItem.foodName = tpItem.foodName + "<img src='../../images/icon_tip_ting.png'></img>";
		}
		if (tpItem.gift == true) {
			// 赠
			tpItem.foodName = tpItem.foodName + "<img src='../../images/forFree.png'></img>";
		}
		if (tpItem.currPrice == true) {
			// 時
			tpItem.foodName = tpItem.foodName + "<img src='../../images/currPrice.png'></img>";
		}
		if (tpItem.temporary == true) {
			// 臨
			tpItem.foodName = tpItem.foodName + "<img src='../../images/tempDish.png'></img>";
		}
	}
	
	checkOutStore.reload();
	
	// 算总价
	var totalCount = 0;
	var forFreeCount = 0;
	for ( var i = 0; i < checkOutDataDisplay.root.length; i++) {
//		var singleCount = parseFloat(checkOutDataDisplay[i][5].substr(1));
		var tpItem = checkOutDataDisplay.root[i];
		var singleCount = parseFloat(tpItem.totalPrice);
		if (tpItem.gift == true) {
//			forFreeCount = forFreeCount + parseFloat(checkOutData[i][4].substring(1)) * checkOutData[i][3];
			forFreeCount = forFreeCount + parseFloat(tpItem.discount) * tpItem.totalPrice;
		} else {
			totalCount = totalCount + singleCount;
		}
	}
	totalCount = totalCount.toFixed(2);
	forFreeCount = forFreeCount.toFixed(2);
	originalTotalCount = totalCount;
	
	document.getElementById("totalCount").innerHTML = totalCount;
	document.getElementById("forFree").innerHTML = forFreeCount;
	
	moneyCount("radio");

};
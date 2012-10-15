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

checkOurListRefresh = function() {
	
//	var discountValue = checkOutForm.getForm().findField("discountRadio").getGroupValue();
	
	if(typeof(checkOutData.root) == 'undefined'){
		return;
	}
	
	var discount = Ext.getCmp('comboDiscount');
	
	checkOutDataDisplay.length = 0;
	checkOutDataDisplay.root = [];
	
	for(var i = 0; i < checkOutData.root.length; i++) {
		var tpItem = checkOutData.root[i];
		
		// 特价,赠送,临时      不打折
		if(tpItem.special == true || tpItem.gift == true  || tpItem.temporary == true){
			tpItem.discount = parseFloat(1).toFixed(2);
		}else{
//			if (discountValue == "discount1") {
//				discountType = 1;
//				tpItem.discount = parseFloat(tpItem.kitchen.discount1).toFixed(2);	
//			} else if (discountValue == "discount2") {
//				discountType = 2;
//				tpItem.discount = parseFloat(tpItem.kitchen.discount2).toFixed(2);			
//			} else {
//				discountType = 3;
//				tpItem.discount = parseFloat(tpItem.kitchen.discount3).toFixed(2);
//			}
			tpItem.discount = parseFloat(1).toFixed(2);
			for(var di = 0; di < discountPlanData.root.length; di++){
				if(discount.getValue() != -1 && discountPlanData.root[di].discount.id == discount.getValue() 
						&& discountPlanData.root[di].kitchen.kitchenID == tpItem.kitchen.kitchenID){
					tpItem.discount = parseFloat(discountPlanData.root[di].rate).toFixed(2);
					break;
				}
			}
		}
		
		tpItem.totalPrice = parseFloat((tpItem.unitPrice + tpItem.tastePrice ) * tpItem.discount * tpItem.count);
		
		checkOutDataDisplay.root.push(tpItem);
	}
	
	checkOutStore.loadData(checkOutDataDisplay);
	
	// 算总价
	var totalCount = 0;
	var forFreeCount = 0;
	for ( var i = 0; i < checkOutDataDisplay.root.length; i++) {
		var tpItem = checkOutDataDisplay.root[i];
		var singleCount = parseFloat(tpItem.totalPrice);
		if (tpItem.gift == true) {
			forFreeCount = forFreeCount + parseFloat(tpItem.discount) * singleCount;
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
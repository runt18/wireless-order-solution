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

//---------------------------------------------------
checkOurListRefresh = function() {
	if(checkOutData.root == null || typeof(checkOutData.root) == 'undefined'){
		return;
	}
	// 重载价格方案信息
	checkPricePlan();
	
	var discount = Ext.getCmp('comboDiscount');
	
	checkOutDataDisplay.length = 0;
	checkOutDataDisplay.root = [];
	
	for(var i = 0; i < checkOutData.root.length; i++) {
		var tpItem = checkOutData.root[i];
		
		// 特价,赠送,临时      不打折
//		if(tpItem.special == true || tpItem.gift == true  || tpItem.temporary == true){
		if(tpItem.special == true || tpItem.gift == true){
			tpItem.discount = parseFloat(1).toFixed(2);
		}else{
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
	
	moneyCount();
};

// ---------------------------------------------------
checkPricePlan = function(){
	var ppid = Ext.getCmp('comboPricePlan').getValue();
	for(var i = 0; i < pricePlanData.root.length; i++){
		if(eval(ppid == pricePlanData.root[i]['id'])){
			var ppList = pricePlanData.root[i]['items'];
			for( var k = 0; k < checkOutDataDisplay.root.length; k++){
				for(var j = 0; j < ppList.length; j++){
					if(eval(checkOutDataDisplay.root[k]['foodID'] == ppList[j]['foodID'])){
						checkOutDataDisplay.root[k]['unitPrice'] = ppList[j]['unitPrice'];
						break;
					}
				}
			}
			break;
		}
	}
};
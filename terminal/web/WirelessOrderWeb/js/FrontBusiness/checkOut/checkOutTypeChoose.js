var getMemberInfo = function(memberNbr) {

	var Request = new URLParaQuery();
	Ext.Ajax.request({
		url : "../../QueryMember.do",
		params : {
			"pin" : Request["pin"],
			'dataSource' : 'normal',
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
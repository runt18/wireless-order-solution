function loadAllMaterial() {
	materialComboData = [];
}

// on page load function
function menuMgrOnLoad() {

	// update the operator name
	getOperatorName(pin, "../../");
	
	// 獲取廚房信息，以便顯示廚房描述
	// 后台：[厨房编号,"厨房名称",一般折扣1,一般折扣2,一般折扣3,会员折扣1,会员折扣2,会员折扣3]
	// 前台：kitchenData：[厨房编号,厨房名称]
	Ext.Ajax.request({
		url : "../../QueryMenu.do",
		params : {
			pin : pin,
			restaurantID : restaurantID,
			type : 3
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				kitchenTypeData = resultJSON.root;
				for(var i = 0; i < kitchenTypeData.length; i++){
					if(kitchenTypeData[i].kitchenAliasID == 253){
						kitchenTypeData.splice(i,1);
					}
				}
			} else {
				Ext.MessageBox.show({
					msg : resultJSON.msg,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		},
		failure : function(response, options) {
			
		}
	});
};

function loadFoodMaterial() {
	// 關聯食材
	var foodID = menuStore.getAt(currRowIndex).get("foodID");

	Ext.Ajax.request({
		url : "../../QueryFoodMaterial.do",
		params : {
			"pin" : pin,
			"foodID" : foodID
		},
		success : function(response, options) {
			var jsonResult = Ext.util.JSON.decode(response.responseText);
			if(eval(jsonResult.success) == true){
				materialStore.loadData(jsonResult);
			}else{
				Ext.MessageBox.show({
					title : '错误提示',
					msg : String.format('错误号:{0},错误信息:{1}', jsonResult.errCode, jsonResult.errMsg),
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		},
		failure : function(response, options) {
			var jsonResult = Ext.util.JSON.decode(response.responseText);
			Ext.MessageBox.show({
				title : '错误提示',
				msg : String.format('错误号:{0},错误信息:{1}', jsonResult.errCode, jsonResult.errMsg),
				width : 300,
				buttons : Ext.MessageBox.OK
			});
		}
	});
}
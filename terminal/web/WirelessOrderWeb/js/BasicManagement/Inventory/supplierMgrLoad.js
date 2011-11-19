function loadAllsupplier() {
	supplierData = [];
	Ext.Ajax.request({
		url : "../../QuerySupplierMgr.do",
		params : {
			"pin" : pin,
			"type" : 0,
			"value" : "",
			"isPaging" : false
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			// 格式：[供應商ID，供應商別名，供應商名称]
			// 后台格式：[供應商ID，供應商別名，名稱，電話，聯係人，地址]
			var rootData = resultJSON.root;
			if (rootData[0].message == "normal") {
				for ( var i = 0; i < rootData.length; i++) {
					supplierData
							.push([ rootData[i].supplierID,
									rootData[i].supplierAlias,
									rootData[i].supplierName ]);
				}
			} else {
				Ext.MessageBox.show({
					msg : rootData[0].message,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		},
		failure : function(response, options) {
			Ext.MessageBox.show({
				msg : " Unknown page error ",
				width : 300,
				buttons : Ext.MessageBox.OK
			});
		}
	});
}

// on page load function
function supplierMgrOnLoad() {

	// update the operator name
	getOperatorName(pin, "../../");

	searchForm.remove("conditionText");

	loadAllsupplier();
};

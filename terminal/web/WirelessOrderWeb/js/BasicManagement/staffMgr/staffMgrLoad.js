﻿function loadAllStaff() {
	tasteData = {};
	Ext.Ajax.request({
		url : "../../QueryStaff.do",
		params : {
			"restaurantID" : restaurantID,
			"type" : 0,
			"isPaging" : false,
			"isCombo" : false
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			// 后台：[]
			var rootData = resultJSON.root;
			if (rootData.length != 0) {
				if (rootData[0].message == "normal") {
					staffData = rootData.slice(0);

					// 我也說不清這裡為什麽要刷一次，就是找個藉口刷新一下表格
					staffStore.reload({
						params : {
							start : 0,
							limit : pageRecordCount
						}
					});
				} else {
					Ext.MessageBox.show({
						msg : rootData[0].message,
						width : 300,
						buttons : Ext.MessageBox.OK
					});
				}
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
function staffMgrOnLoad() {

	// update the operator name
	getOperatorName(pin, "../../");

	searchForm.remove("conditionText");

	loadAllStaff();
};

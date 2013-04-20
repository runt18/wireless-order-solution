function loadAllTaste() {
	tasteData = {};
	Ext.Ajax.request({
		url : "../../QueryTaste.do",
		params : {
			"pin" : pin,
			"type" : "0",
			"isPaging" : false,
			"isCombo" : false
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			var rootData = resultJSON.root;
			if (rootData.length != 0) {
				if (rootData[0].message == "normal") {
					tasteData = rootData.slice(0);
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
function tasteMgrOnLoad() {
	getOperatorName(pin, "../../");
	loadAllTaste();
};

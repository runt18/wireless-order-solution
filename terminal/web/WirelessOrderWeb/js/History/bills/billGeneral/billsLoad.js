function loadAddKitchens() {
	kitchenMultSelectData = [];
	Ext.Ajax.request({
		url : "../../QueryKitchen.do",
		params : {
			"dataSource" : "normal",
			"pin" : pin,
			"isPaging" : false
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			var rootData = resultJSON.root;
			for ( var i = 0; i < rootData.length; i++) {
				kitchenMultSelectData.push([
				    rootData[i].kitchenAlias,
					rootData[i].kitchenName, 
					rootData[i].kitchenID 
				]);
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
function billHistoryOnLoad() {
	// data init
	loadAddKitchens();
};
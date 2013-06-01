function loadAddKitchens() {
	kitchenMultSelectData = [];
	Ext.Ajax.request({
		url : "../../QueryKitchen.do",
		params : {
			"data" : "normal",
			"pin" : pin,
			"isPaging" : false
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			var rootData = resultJSON.root;
			if (rootData[0].message == "normal") {
				for ( var i = 0; i < rootData.length; i++) {
					kitchenMultSelectData.push([
					    rootData[i].kitchenAlias,
						rootData[i].kitchenName, 
						rootData[i].kitchenID 
					]);
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

function loadDepartment() {
	/*
	deptMultSelectData = [];
	Ext.Ajax.request({
		url : "../../QueryDepartment.do",
		params : {
			"pin" : pin,
			"isPaging" : false,
			"isCombo" : false
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			var rootData = resultJSON.root;
			if (rootData.length != 0) {
				if (rootData[0].message == "normal") {
					for ( var i = 0; i < rootData.length; i++) {
						deptMultSelectData.push([
						    rootData[i].deptID,
							rootData[i].deptName 
						]);
					}
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
	*/
}

function loadAllStaff() {
	staffData = [];
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
			// 格式：[ID，Name, Alias]
			var rootData = resultJSON.root;
			if (rootData[0].message == "normal") {
				for ( var i = 0; i < rootData.length; i++) {
					staffData.push([
					    rootData[i].staffID,
						rootData[i].staffName, 
						rootData[i].staffAlias 
					]);
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
function billHistoryOnLoad() {
	getOperatorName(pin, "../../");

	// data init
	loadAddKitchens();
	loadDepartment();
	loadAllStaff();

};
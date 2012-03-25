function loadDepartment() {
	departmentData = [];
	Ext.Ajax.request({
		url : "../../QueryDepartment.do",
		params : {
			"pin" : pin,
			"isPaging" : false,
			"isCombo" : false
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			// 格式：[部門编号，部門名称]
			// 后台格式：[部門编号，部門名称]
			var rootData = resultJSON.root;
			if (rootData[0].message == "normal") {
				for ( var i = 0; i < rootData.length; i++) {
					departmentData.push([ rootData[i].deptID,
							rootData[i].deptName ]);
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

function loadAddKitchens() {
	kitchenMultSelectData = [];
	Ext.Ajax.request({
		url : "../../QueryKitchenMgr.do",
		params : {
			"pin" : pin,
			"isPaging" : false
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			// 格式：[分廚編號，名稱，分廚別名]
			// 后台格式：[分廚編號，名稱，一般折扣１，一般折扣２，一般折扣３，會員折扣１，會員折扣２，會員折扣３，部門]
			var rootData = resultJSON.root;
			if (rootData[0].message == "normal") {
				for ( var i = 0; i < rootData.length; i++) {
					kitchenMultSelectData
							.push([ rootData[i].kitchenAlias,
									rootData[i].kitchenName,
									rootData[i].kitchenID ]);
				}
				kitchenStore.reload();
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
function kitchenMgrOnLoad() {

	// update the operator name
	getOperatorName(pin, "../../");

	loadDepartment();
	departmentStore.reload();

	loadAddKitchens();
};

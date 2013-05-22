// 獲取所有菜品
function loadAllDishes() {
	dishMultSelectData = {};
	Ext.Ajax.request({
		url : "../../QueryMenu.do",
		params : {
			pin : pin,
			restaurantID : restaurantID,
			type : 1
		},
		success : function(response, options) {
			var resultJSON = Ext.decode(response.responseText);
			if (resultJSON.success == true) {
				dishMultSelectData = resultJSON;
			}
		},
		failure : function(response, options) {
		}
	});
}

function loadAddKitchens() {
	kitchenMultSelectData = [];
	Ext.Ajax.request({
		url : "../../QueryKitchenMgr.do",
		params : {
			"data" : "normal",
			"pin" : pin,
			"isPaging" : false
		},
		success : function(response, options) {
			var resultJSON = Ext.decode(response.responseText);
			// 格式：[分廚編號，名稱，分廚別名]
			// 后台格式：[分廚編號，名稱，一般折扣１，一般折扣２，一般折扣３，會員折扣１，會員折扣２，會員折扣３，部門]
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
	deptMultSelectData = [];
	Ext.Ajax.request({
		url : "../../QueryDepartment.do",
		params : {
			"pin" : pin,
			"isPaging" : false,
			"isCombo" : false
		},
		success : function(response, options) {
			var resultJSON = Ext.decode(response.responseText);
			// 格式：[部門编号，部門名称]
			// 后台格式：[部門编号，部門名称]
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
			var resultJSON = Ext.decode(response.responseText);
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
/**
 * 
 */
function loadShiftDuty(){
	Ext.Ajax.request({
		url : '../../DutyRangeStat.do',
		params : {
			dataSource : 'today',
			pin : pin
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			var bd = {root:[]};
			for(var i = 0; i < jr.root.length; i++){
				bd.root.push(jr.root[i]);
				bd.root[i].duty = jr.root[i].onDutyFormat + salesSubSplitSymbol + jr.root[i].offDutyFormat;
				bd.root[i].displayMsg = (jr.root[i].onDutyFormat + ' -- ' + jr.root[i].offDutyFormat + ' (' + jr.root[i].staff.name + ')');
			}
			shiftDutyOfToday = bd;
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
		}
	});
}

// on page load function
function billsOnLoad() {
	pin = Request["pin"];

	// update the operator name
	getOperatorName(pin, "../../");

	// data init
	loadAllDishes();
	loadAddKitchens();
	loadDepartment();
	loadAllStaff();
	loadShiftDuty();
};
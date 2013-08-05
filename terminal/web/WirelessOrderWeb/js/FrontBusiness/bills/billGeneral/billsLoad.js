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
		url : "../../QueryKitchen.do",
		params : {
			dataSource : "normal",
			pin : pin
		},
		success : function(response, options) {
			var resultJSON = Ext.decode(response.responseText);
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
			var rootData = resultJSON.root;
			if (resultJSON.msg == "normal") {
				for ( var i = 0; i < rootData.length; i++) {
					staffData.push([
					    rootData[i].staffID,
						rootData[i].staffName, 
						rootData[i].staffAlias 
					]);
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

function billsOnLoad() {
	loadAllDishes();
	loadAddKitchens();
	loadShiftDuty();
};
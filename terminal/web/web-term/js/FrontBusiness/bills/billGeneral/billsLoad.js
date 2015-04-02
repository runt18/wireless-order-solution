function loadAllDishes() {
	dishMultSelectData = {};
	Ext.Ajax.request({
		url : "../../QueryMenu.do",
		params : {
			isCookie : true,
			dataSource : 'foods',
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
			isCookie : true,
			dataSource : "normal"
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
	$.ajax({
		url : '../../DutyRangeStat.do',
		type : 'post',
		async:false,
		data : {dataSource : 'today'},
		success : function(jr, status, xhr){
			var bd = {root:[]};
			for(var i = 0; i < jr.root.length; i++){
				bd.root.push(jr.root[i]);
				bd.root[i].duty = jr.root[i].onDutyFormat + salesSubSplitSymbol + jr.root[i].offDutyFormat;
				bd.root[i].displayMsg = (jr.root[i].onDutyFormat + ' -- ' + jr.root[i].offDutyFormat + ' (' + jr.root[i].staffName + ')');
			}
			shiftDutyOfToday = bd;
			duty = createStatGridTabDutyFn({
				data : shiftDutyOfToday,
				listeners : {
					select : function(){
						Ext.getCmp('salesKitchenSubBtnSearch').handler();
					}
				}
			});
		},
		error : function(request, status, err){
			Ext.ux.showMsg(request.responseText);
		}
	}); 
	
}

function loadPayment(){
	$.ajax({
		url : '../../PaymentStat.do',
		type : 'post',
		async:false,
		data : {dataSource : 'today'},
		success : function(jr, status, xhr){
			paymentOfToday = jr;
		},
		error : function(request, status, err){
			Ext.ux.showMsg(request.responseText);
		}
	}); 
	
}

function billsOnLoad() {
	loadAllDishes();
	loadAddKitchens();
	loadShiftDuty();
	
};

Ext.onReady(function(){
	
	var orderId = Ext.WindowMgr.getActive().orderId;
 	var queryType = Ext.WindowMgr.getActive().queryType;
 	var foodStatus = Ext.WindowMgr.getActive().foodStatus;
 	var branchId = Ext.WindowMgr.getActive().branchId;
 	var deptId = Ext.WindowMgr.getActive().deptID;
 	var kitchenId = Ext.WindowMgr.getActive().kitchenID;
 	var foodId = Ext.WindowMgr.getActive().foodId;
 	var regionId = Ext.WindowMgr.getActive().regionId;
 	var staffId = Ext.WindowMgr.getActive().staffID;
 	var beginDate = Ext.WindowMgr.getActive().beginDate;
 	var endDate = Ext.WindowMgr.getActive().endDate;
 	var opening = Ext.WindowMgr.getActive().opening;
 	var ending = Ext.WindowMgr.getActive().ending;
 	
 	
 	
	var billDetailpageRecordCount = 15;
	var billDetailGrid;
	function detailIsPaidRenderer(v){
		return eval(v) ? '是' : '否';
	}
	
	function detailIsGiftRenderer(v){
		return eval(v) ? '是' : '否';
	}
	function load(){
		billDetailGrid.getStore().load({
			params : {
				start : 0,
				limit : billDetailpageRecordCount
			}
		});
	}

	billDetailGrid = createGridPanel(
		'',
		'',
		'',
	    '',
	    '../../QueryDetail.do',
	    [
		    [true, false, false, false], 
		    ['日期','orderDateFormat',100],
		    ['名称','name',100],
		    ['账单号', 'orderId', 100],
		    ['单价','unitPrice',60, 'right', Ext.ux.txtFormat.gridDou],
		    ['数量','count', 60, 'right', Ext.ux.txtFormat.gridDou], 
		    ['口味','tasteGroup.tastePref'],
		    ['口味价钱','tasteGroup.tastePrice', 60, 'right', Ext.ux.txtFormat.gridDou],
		    ['厨房','kitchen.name', 60],
		    ['操作类型','operation', 60],
		    ['反结账','isRepaid', 60, 'center', detailIsPaidRenderer],
		    ['赠送','isGift', 60, 'center', detailIsGiftRenderer],
		    ['服务员','waiter', 60],
		    ['退菜原因', 'cancelReason.reason']
		],
		['orderDateFormat', 'name', 'orderId', 'unitPrice', 'count', 'discount',
		 'tasteGroup.tastePref', 'tasteGroup.tastePrice', 'kitchen.name', 'waiter', 'cancelReason.reason', 
		 'isGift', 'isReturn', 'isRepaid','isTransfer', 'isCommission', 'operation'],
	    [ ['orderID', orderId], ['queryType', queryType ? queryType : 'history'], ['branchId', branchId], ['deptID', deptId ? deptId : ''], ['kitchenID', kitchenId ? kitchenId : ''],
	    ['foodId' , foodId ? foodId : ''], ['regionId', regionId ? regionId : ''], ['staffID', staffId ? staffId : ''], ['beginDate', beginDate ? beginDate : ''],
	    ['endDate', endDate ? endDate : ''], ['opening', opening ? opening : ''], ['ending', ending ? ending : '']],
	    billDetailpageRecordCount,
	    ''
	);
	billDetailGrid.frame = false;
	billDetailGrid.border = false;
	billDetailGrid.getStore().on('load', function(store, records, options){
		var sumRow;
		for(var i = 0; i < records.length; i++){
			if(records[i].get(foodStatus)){
				
				sumRow = billDetailGrid.getView().getRow(i);
				sumRow?sumRow.style.backgroundColor = 'salmon' : '';
			}
		}
	});
	new Ext.Panel({
		renderTo : 'divOrderDetail',
		frame : false,
		border : false,
		layout : 'fit',
		width : 1085,
		height : 400,
		items : [billDetailGrid]
	});
	load();
});
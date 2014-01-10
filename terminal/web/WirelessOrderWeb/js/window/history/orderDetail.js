var billDetailpageRecordCount = 15;
var billDetailGrid;
function detailIsPaidRenderer(v){
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
Ext.onReady(function(){
	billDetailGrid = createGridPanel(
		'',
		'',
		'',
	    '',
	    '../../QueryDetail.do',
	    [
		    [true, false, false, false], 
		    ['日期','orderDateFormat',100],
		    ['名称','name',130],
		    ['单价','unitPrice',60, 'right', 'Ext.ux.txtFormat.gridDou'],
		    ['数量','count', 60, 'right', 'Ext.ux.txtFormat.gridDou'], 
		    ['口味','tasteGroup.tastePref'],
		    ['口味价钱','tasteGroup.tastePrice', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
		    ['厨房','kitchen.name', 60],
		    ['反结账','isRepaid', 60, 'center', 'detailIsPaidRenderer'],
		    ['服务员','waiter', 60],
		    ['退菜原因', 'cancelReason.reason']
		],
		['orderDateFormat', 'name', 'unitPrice', 'count', 'discount',
		 'tasteGroup.tastePref', 'tasteGroup.tastePrice', 'kitchen.name', 'waiter', 'cancelReason.reason', 'isGift', 'isReturn', 'isRepaid', 'isCommission'],
	    [ ['orderID', orderId], ['queryType', queryType != 'null'? queryType : 'History']],
	    billDetailpageRecordCount,
	    ''
	);
	billDetailGrid.frame = false;
	billDetailGrid.border = false;
	billDetailGrid.getStore().on('load', function(store, records, options, res){
		var sumRow;
		for(var i = 0; i < records.length; i++){
			if(records[i].get(foodStatus)){
				sumRow = billDetailGrid.getView().getRow(i);
				sumRow.style.backgroundColor = 'salmon';
			}
		}
	});
/*		sumRow = null;
		// 汇总
		var jr = Ext.decode(res.responseText);
		if(jr.root.length > 0){
			store.add(new OrderFoodRecord({
				orderDateFormat : '汇总',
				unitPrice : jr.other.sum.totalPrice,
				count : jr.other.sum.totalCount
			}));
		}
		var gv = billDetailGrid.getView();
		var sumRow = gv.getRow(store.getCount()-1);
		sumRow.style.backgroundColor = '#DDD';			
		sumRow.style.fontWeight = 'bold';
		gv.getCell(store.getCount()-1, 1).style.fontSize = '15px';
		gv.getCell(store.getCount()-1, 1).style.color = 'green';
		gv.getCell(store.getCount()-1, 1).style.fontWeight = 'bold';
		gv.getCell(store.getCount()-1, 3).style.fontSize = '15px';
		gv.getCell(store.getCount()-1, 3).style.color = 'green';
		gv.getCell(store.getCount()-1, 4).style.fontSize = '15px';
		gv.getCell(store.getCount()-1, 4).style.color = 'green';
		gv.getCell(store.getCount()-1, 2).innerHTML = '';
		gv.getCell(store.getCount()-1, 5).innerHTML = '';
		gv.getCell(store.getCount()-1, 6).innerHTML = '';
		gv.getCell(store.getCount()-1, 7).innerHTML = '';
		gv.getCell(store.getCount()-1, 8).innerHTML = '';
	});*/
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

Ext.onReady(function(){

	var orderID = Ext.WindowMgr.getActive().orderId;
	var queryType = Ext.WindowMgr.getActive().queryType;
	var branchId = Ext.WindowMgr.getActive().branchId;
	
	var history_viewBillGrid;
	var viewBillGenPanel;
	
	function setOrderDetail(orderDetail){
		Ext.getDom('billIDBV').innerHTML = orderID;
		Ext.getDom('billTypeBV').innerHTML = orderDetail['categoryText'];
		Ext.getDom('tableNbrBV').innerHTML = orderDetail['tableAlias'];
		Ext.getDom('personNbrBV').innerHTML = orderDetail['customNum'];
		Ext.getDom('billDateBV').innerHTML = orderDetail['orderDateFormat'];
		Ext.getDom('payTypeBV').innerHTML = orderDetail['settleTypeText'];
		Ext.getDom('payMannerBV').innerHTML = orderDetail['payTypeText'];
		Ext.getDom('serviceRateBV').innerHTML = (orderDetail['serviceRate']*100) + '％';
		Ext.getDom('serviceStaffBV').innerHTML = orderDetail['waiter'];
		Ext.getDom('forFreeBV').innerHTML = '￥' + orderDetail['giftPrice'].toFixed(2);
		Ext.getDom('shouldPayBV').innerHTML = '￥' + orderDetail['totalPrice'].toFixed(2);
		Ext.getDom('actrualPayBV').innerHTML = '￥' + orderDetail['actualPrice'].toFixed(2);
		Ext.getDom('discountBV').innerHTML = '￥' + orderDetail['discountPrice'].toFixed(2);
		Ext.getDom('erasePuotaPriceBV').innerHTML = '￥' + orderDetail['erasePrice'].toFixed(2);
		Ext.getDom('cancelPriceBV').innerHTML = '￥' + orderDetail['cancelPrice'].toFixed(2);
		var couponPrice = isNaN(orderDetail['couponPrice'])?0:orderDetail['couponPrice'];
		Ext.getDom('couponBV').innerHTML = '￥' + couponPrice.toFixed(2);
		
		if(orderDetail['mixedPayment'] && orderDetail['mixedPayment'].payTypes.length > 0){
			var mixedPayTypes = '(';
			for (var i = 0; i < orderDetail['mixedPayment'].payTypes.length; i++) {
				if(i > 0){
					mixedPayTypes += ', ';
				}
				mixedPayTypes += orderDetail['mixedPayment'].payTypes[i].name + ':' + orderDetail['mixedPayment'].payTypes[i].money + '元';
			}
			mixedPayTypes += ')';
			Ext.getDom('billDetail_mixedPay').innerHTML = mixedPayTypes;
		}
		
	}
	
	function load(){
	
		Ext.Ajax.request({
			url : '../../QueryOrder.do',
			params : {
				orderID : orderID,
				queryType : queryType ? queryType : 'history',
				branchId : branchId
			},
			success : function(response, options) {
				var jr = Ext.decode(response.responseText);
				if (jr.success == true) {
					setOrderDetail(jr.other.order);
					history_viewBillGrid.getStore().loadData({root:jr.other.order.orderFoods});
				} else {
					Ext.ux.showMsg(jr);
				}
			},
			failure : function(response, options) {
				var jr = Ext.decode(response.responseText);
				Ext.ux.showMsg(jr);	
			}
		});
	}
	
	function viewBill_formatFoodName(record, iname, name){
		var img = '';
		if(record.get('isGift'))
			img += '&nbsp;<img src="../../images/forFree.png"></img>';
		
		record.set(iname, record.get(name) + img);
		record.commit();		
	}
	
	history_viewBillGrid = createGridPanel(
		'',
		'已点菜',
		'',
	    '',
	    '',
	    [
		    [true, false, false, false], 
		    ['菜名', 'displayFoodName', 130] , 
		    ['口味', 'tasteGroup.tastePref', 100],
		    ['数量', 'count', 50, 'right', Ext.ux.txtFormat.gridDou],
		    ['折扣', 'discount', 50, 'right', Ext.ux.txtFormat.gridDou],
		    ['金额', 'totalPrice', 100, 'right', Ext.ux.txtFormat.gridDou]
		],
		OrderFoodRecord.getKeys(),
	    [],
	    0
	);

	history_viewBillGrid.frame = false;
	history_viewBillGrid.border = false;
	history_viewBillGrid.region = 'center';
	history_viewBillGrid.getStore().on('load', function(thiz, records){
		for(var i = 0; i < records.length; i++){
			viewBill_formatFoodName(records[i], 'displayFoodName', 'name');
//			Ext.ux.formatFoodName(records[i], 'displayFoodName', 'name');
		}
	});
	
	
	viewBillGenPanel = new Ext.Panel({
		region : 'north',
		height : 120,
		frame : true,
		border : false,
		items : [new Ext.Panel({
			xtype : 'panel',
			layout : 'column',
			//height : Ext.isIE ? 90 : 110 ,
			height : 110,
			defaults : {
				columnWidth : .23,
				defaults : {
					xtype : 'panel',
					html : '----'
				}
			},
			items : [{
				items : [{
					cls : 'cLeft',
					html : '账单号:'
				}]
			}, {
				items : [{
					cls : 'left',
					id : 'billIDBV'
				}]
			},{
				items : [{
					cls : 'cLeft',
					html : '类型:'
				}]
			}, {
				items : [{
					cls : 'left',
					id : 'billTypeBV'
				}]
			}, {
				columnWidth : 1
			}, {
				items : [{
					cls : 'cLeft',
					html : '台号:'
				}]
			}, {
				items : [{
					cls : 'left',
					id : 'tableNbrBV'
				}]
			},{
				items : [{
					cls : 'cLeft',
					html : '人数:'
				}]
			}, {
				items : [{
					cls : 'left',
					id : 'personNbrBV'
				}]
			}, {
				columnWidth : 1
			},{
				items : [{
					cls : 'cLeft',
					html : '结账方式:'
				}]
			}, {
				items : [{
					cls : 'left',
					id : 'payTypeBV'
				}]
			},{
				items : [{
					cls : 'cLeft',
					html : '付款方式:'
				}]
			}, {
				items : [{
					cls : 'left',
					id : 'payMannerBV'
				}]
			}, {
				columnWidth : 1
			},{
				items : [{
					cls : 'cLeft',
					html : '服务人员:'
				}]
			}, {
				items : [{
					cls : 'left',
					id : 'serviceStaffBV'
				}]
			},{
				items : [{
					cls : 'cLeft',
					html : '服务费:'
				}]
			}, {
				items : [{
					cls : 'left',
					id : 'serviceRateBV'
				}]
			}, {
				columnWidth : 1
			}, {
				items : [{
					cls : 'cLeft',
					html : '日期:'
				}]
			}, {
				columnWidth : .7,
				items : [{
					cls : 'left',
					id : 'billDateBV'
				}]
			}]
		})]
	});
	var viewBillAddPanel = new Ext.Panel({
		region : 'south',
		height : 120,
		frame : true,
		border : false,
		items : [new Ext.Panel({
			xtype : 'panel',
			layout : 'column',
			//height : Ext.isIE ? 90 : 110 ,
			height : 120,
			defaults : {
				columnWidth : .16,
				defaults : {
					xtype : 'panel',
					html : '----'
				}
			},
			items : [{
				items : [{
					cls : 'cLeft',
					html : '抹数:'
				}]
			}, {
				items : [{
					cls : 'left',
					id : 'erasePuotaPriceBV'
				}]
			},{
				items : [{
					cls : 'cLeft',
					html : '赠送:'
				}]
			}, {
				items : [{
					cls : 'left',
					id : 'forFreeBV'
				}]
			}, {
				items : [{
					cls : 'cLeft',
					html : '优惠劵:'
				}]
			}, {
				items : [{
					cls : 'left',
					id : 'couponBV'
				}]
			},{
				columnWidth : 1
			},{
				items : [{
					cls : 'cLeft',
					html : '退菜:'
				}]
			}, {
				items : [{
					cls : 'left',
					id : 'cancelPriceBV'
				}]
			},  {
				items : [{
					cls : 'cLeft',
					html : '折扣:'
				}]
			}, {
				items : [{
					cls : 'left',
					id : 'discountBV'
				}]
			}, {
				items : [{
					cls : 'cLeft',
					html : '原价:'
				}]
			}, {
				items : [{
					cls : 'left',
					id : 'shouldPayBV'
				}]
			}, {
				columnWidth : 1
			}, {
				columnWidth : .6,
				items : [{
					html : '&nbsp;'
				}]
			},{
				items : [{
					style : 'color:#15428B;text-align:left;font-size: 25px;margin-bottom: 3px;',
					html : '实收:'
				}]
			}, {
				columnWidth : 0.24,
				items : [{
					style : 'text-align:left;font-size: 25px;margin-bottom: 3px;color:green',
					id : 'actrualPayBV'
				}]
			}, {
				columnWidth : 1,
				items : [{
					id : 'billDetail_mixedPay',
					style : 'color:#15428B;text-align:left;font-size: 15px;margin-bottom: 3px;float:right;',
					html : ''
				}]
			}]
		})]
	});


	new Ext.Panel({
		renderTo : 'divViewBillDetail',
		layout : 'border',
		width : 500,
		height : 500,
		frame : false,
		border : false,
		items : [ viewBillGenPanel, history_viewBillGrid, viewBillAddPanel ]
	});
	
	load();
});
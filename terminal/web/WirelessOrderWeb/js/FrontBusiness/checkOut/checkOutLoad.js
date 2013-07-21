/**
 * 加载折扣方案信息
 */
function loadDiscountData(_c){
	if(_c == null || typeof _c == 'undefined'){
		_c = {};
	}
	Ext.Ajax.request({
		url : '../../QueryDiscountTree.do',
		params : {
			pin : pin,
			restaurantID : restaurantID
		},
		success : function(response, options) {
			var jr = eval(response.responseText);
			checkOutForm.buttons[7].setDisabled(false);
			discountData = jr;
			var discount = Ext.getCmp('comboDiscount');
			var defaultsID = '';
			discount.store.loadData({root:discountData});
			
			// 设置默认显示折扣方案
			for(var i = 0; i < discountData.length; i++){
				if(eval(discountData[i].isDefault == 1)){
					defaultsID = discountData[i].discountID;
					break;
				}else if(eval(discountData[i].status == 2)){
					defaultsID = discountData[i].discountID;
				}
			}
			if(defaultsID != null && typeof defaultsID != 'undefined' && defaultsID != ''){
				discount.setValue(defaultsID);
				discount.fireEvent('select', discount, null, null);
			}
		},
		failure : function(response, options) {
			checkOutForm.buttons[7].setDisabled(false);
			Ext.ux.showMsg(Ext.decode(response.responseText));
		},
		callback : _c.callback
	});
}

/**
 * 加载价格方案信息
 */ 
function loadPricePlanData(_c){
	if(_c == null || typeof _c == 'undefined'){
		_c = {};
	}
	Ext.Ajax.request({
		url : '../../QueryFoodPricePlanByOrder.do',
		params : {
			restaurantID : restaurantID,
			idList : typeof  checkOutData.other != 'undefined' ? checkOutData.other.idList : ''
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				pricePlanData = jr;		
				Ext.getCmp('comboPricePlan').store.loadData(pricePlanData);
				for(var i = 0; i < pricePlanData.root.length; i++){
					if(pricePlanData.root[i]['statusValue'] == 1){
						var pp = Ext.getCmp('comboPricePlan');
						pp.setValue(pricePlanData.root[i]['id']);
						calcPricePlanID = pricePlanData.root[i]['id'];
						break;
					}
				}
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(res, pot){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		},
		callback : _c.callback
	});	
};

/**
 * 加载账单基础汇总信息
 */
function loadOrderBasicMsg(){
	document.getElementById("serviceCharge").value = orderMsg.serviceRate * 100;
	var actualCount = document.getElementById("actualCount").value;
	document.getElementById("totalCount").innerHTML = parseFloat(orderMsg.totalPrice).toFixed(2);
	document.getElementById("shouldPay").innerHTML = parseFloat(orderMsg.actualPrice).toFixed(2);
	document.getElementById("forFree").innerHTML = parseFloat(orderMsg.giftPrice).toFixed(2);
	document.getElementById("spanCancelFoodAmount").innerHTML = parseFloat(orderMsg.cancelPrice).toFixed(2);
	var change = '0.00';
//	alert(Ext.encode(orderMsg))
	if(actualCount == '' || actualCount < orderMsg.acturalPrice){
		document.getElementById("actualCount").value = parseFloat(orderMsg.actualPrice).toFixed(2);
	}else{
		change = parseFloat(actualCount - orderMsg.actualPrice).toFixed(2);
	}
	document.getElementById("change").innerHTML = change;
	Ext.getCmp('numCustomNum').setValue(orderMsg.customNum >= 0 ? orderMsg.customNum : 0);
	if(eval(orderMsg.category != 4)){
		Ext.getCmp('numCustomNum').setDisabled(false);
	}
	if(eval(orderMsg.category != 4) && eval(orderMsg.cancelPrice > 0)){
		Ext.getDom('spanSeeCancelFoodAmount').style.visibility = 'inherit';		
	}
//	checkOutCenterPanel.setTitle('结账 -- 账单号:<font color="red">' + orderMsg.id + '</font>');
//	if(orderMsg.category != 4){
//		checkOutCenterPanel.setTitle(checkOutCenterPanel.title + ' -- 餐桌号:<font color="red">' + orderMsg.tableAlias + '</font>');
//	}
	
	checkOutMainPanel.setTitle('结账 -- 账单号:<font color="red">' + orderMsg.id + '</font>');
	if(orderMsg.category != 4){
		checkOutMainPanel.setTitle(checkOutMainPanel.title + ' -- 餐桌号:<font color="red">' + orderMsg.table.alias + '</font>');
	}
}

/**
 * 加载系统设置
 */
function loadSystemSetting(_c){
	if(_c == null || typeof _c == 'undefined'){
		_c = {};
	}
	Ext.Ajax.request({
		url : '../../QuerySystemSetting.do',
		params : {
			"restaurantID" : restaurantID
		},
		success : function(response, options) {
			var jr = Ext.decode(response.responseText);
			if (jr.success == true) {
				restaurantData = jr.other.systemSetting;
				if(restaurantData.setting.eraseQuota > 0){
					Ext.getDom('div_showEraseQuota').style.display = 'block';
					Ext.getDom('font_showEraseQuota').innerHTML = parseFloat(restaurantData.setting.eraseQuota).toFixed(2);
				}else{
					Ext.getDom('div_showEraseQuota').style.display = 'none';
					Ext.getDom('font_showEraseQuota').innerHTML = '';
				}
			} else {
				jr.success = true;
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(response,options) {
			Ext.ux.showMsg(Ext.decode(jr));
		},
		callback : _c.callback
	});
}

/**
 * 加载单张餐桌账单信息
 */
function loadTableData(_c){
	if(_c == null || typeof _c == 'undefined'){
		_c = {};
	}
	var eraseQuota = document.getElementById("txtEraseQuota").value;
	var serviceRate = document.getElementById("serviceCharge").value;
	var customNum = document.getElementById("numCustomNum").value;
	eraseQuota = typeof eraseQuota != 'undefined' && eval(eraseQuota >= 0) ? eraseQuota : 0;
	serviceRate = typeof serviceRate != 'undefined' && eval(serviceRate >= 0) ? serviceRate : 0;
	customNum = typeof customNum != 'undefined' && eval(customNum > 0) ? customNum : 0;
	if(serviceRate > 100){
		serviceRate = 100;
		document.getElementById("serviceCharge").value = 100;
	}
	Ext.Ajax.request({
		url : "../../QueryOrder.do",
		params : {
			pin : pin,
			restaurantID : restaurantID,
			tableID : tableID,
			orderID : orderID,
			calc : true,
			discountID : calcDiscountID,
			pricePlanID : calcPricePlanID,
			eraseQuota : eraseQuota,
			serviceRate : serviceRate,
			customNum : customNum
		},
		success : function(response, options) {
			var jr = Ext.decode(response.responseText);
			checkOutForm.buttons[7].setDisabled(false);
			if (jr.success == true) {
				setFormButtonStatus(false);
				// 加载已点菜
				checkOutData = jr;
				// 加载价格方案
//				loadPricePlanData();
				// 加载显示账单基础信息
				orderMsg = jr.other.order;
				loadOrderBasicMsg();
				// 
				checkOutGrid.getStore().loadData(checkOutData);
			} else {
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(response, options) { 
			checkOutForm.buttons[7].setDisabled(false);
			Ext.ux.showMsg(Ext.decode(response.responseText));
		},
		callback : _c.callback
	});
}

/**
 * 加载餐桌组数据
 */
function loadTableGroupData(_c){
	if(_c == null || typeof _c == 'undefined'){
		_c = {};
	}
	var eraseQuota = document.getElementById("txtEraseQuota").value;
	var serviceRate = document.getElementById("serviceCharge").value;
	var customNum = document.getElementById("numCustomNum").value;
	eraseQuota = typeof eraseQuota != 'undefined' && eval(eraseQuota >= 0) ? eraseQuota : 0;
	serviceRate = typeof serviceRate != 'undefined' && eval(serviceRate >= 0) ? serviceRate : 0;
	customNum = typeof customNum != 'undefined' && eval(customNum > 0) ? customNum : 0;
	if(serviceRate > 100){
		serviceRate = 100;
		document.getElementById("serviceCharge").value = 100;
	}
	Ext.Ajax.request({
		url : "../../QueryOrderGroup.do",
		params : {
			queryType : 0, ////////
			pin : pin,
			restaurantID : restaurantID,
			status : 0,
			tableID : tableID,
			orderID : orderID,
			calc : true,
			discountID : calcDiscountID,
			pricePlanID : calcPricePlanID,
			eraseQuota : eraseQuota,
			serviceRate : serviceRate,
			customNum : customNum
		},
		success : function(response, options) {
			var jr = Ext.decode(response.responseText);
			checkOutForm.buttons[7].setDisabled(false);
			if (jr.success == true) {
				setFormButtonStatus(false);
				checkOutForm.buttons[0].setDisabled(true);
				// 加载已点菜
				checkOutData = jr;
				// 加载价格方案
//				loadPricePlanData();
				// 加载账单基础汇总信息
				orderMsg = jr.other.order;
				loadOrderBasicMsg();
				// 生成账单组信息
				var activeTab = null;
				var tabItemsID = 'tabItemsID';
				for(var i = 0; i < jr.root.length; i++){
					tableID = jr.root[i].tableAlias;
					for(var j = 0; j < jr.root[i].orderFoods.length; j++){
						jr.root[i].orderFoods[j].displayFoodName = '';
					}
					var tempID = (tabItemsID + jr.root[i].tableID);
					var cs = true;
					tableGroupTab.items.each(function(at){
						if(at.getId() == tempID){
							cs = false;
							return false;
						}
					});
					if(cs){
						var gp = createGridPanel(
							tempID,
						    ('餐桌编号:' + jr.root[i].tableAlias),
						    '',
						    '',
						    '',
						    [
							    [true, false, false, false], 
							    ['菜名', 'displayFoodName', 230] , 
							    ['口味', 'tastePref', 130] , 
							    ['口味价钱', 'tastePrice', 70, 'right', 'Ext.ux.txtFormat.gridDou'],
							    ['数量', 'count', 70, 'right', 'Ext.ux.txtFormat.gridDou'],
							    ['单价', 'unitPrice', 70, 'right', 'Ext.ux.txtFormat.gridDou'],
							    ['折扣率', 'discount', 70, 'right', 'Ext.ux.txtFormat.gridDou'],
							    ['总价', 'totalPrice', 80, 'right', 'Ext.ux.txtFormat.gridDou'],
							    ['时间', 'orderDateFormat', 130],
							    ['服务员', 'waiter', 80]
							],
//							['displayFoodName', 'foodName', 'tastePref', 'tastePrice', 'count', 'unitPrice',
//							 'discount', 'totalPrice', 'orderDateFormat', 'waiter', 'special','recommend',
//							 'weight', 'stop','gift','currPrice','combination','temporary','tmpTastePrice'],
							OrderFoodRecord.getKeys(),
						    [['restaurantID', restaurantID]],
						    30,
						    ''
						);
						gp.frame = false;
						gp.getStore().on('load', function(thiz, records){
							for(var ti = 0; ti < records.length; ti++){
								Ext.ux.formatFoodName(records[ti], 'displayFoodName', 'foodName');
							}
						});
						gp.getStore().loadData({
							root : jr.root[i].orderFoods
						});
						tableGroupTab.add(gp);
						if(i==0){
							activeTab = gp;
						}
					}else{
						Ext.getCmp(tempID).getStore().loadData({
							root : jr.root[i].orderFoods
						});
					}
				}
				if(tableGroupTab.getActiveTab() == null){
					tableGroupTab.setActiveTab(activeTab);
					checkOutForm.doLayout();			
				}else{
					tableGroupTab.fireEvent('tabchange', tableGroupTab, tableGroupTab.getActiveTab());
				}
			} else {
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(response, options) { 
			checkOutForm.buttons[7].setDisabled(false);
			Ext.ux.showMsg(Ext.decode(response.responseText));
		},
		callback : _c.callback
	});
}

var timerCheckCount = 0;
function refreshCheckOutData(_c){
	if(timerCheckCount < 10*10){
		// 加载参数检查定时器
		if(typeof calcDiscountID == 'undefined' || typeof calcPricePlanID == 'undefined'){
			if(timerCheckParams == null || typeof timerCheckParams == 'undefined'){
				timerCheckParams = setInterval(function(){
					refreshCheckOutData();
				}, 100);
			}
			timerCheckCount++;
			return;
		}else{
			if(timerCheckParams != null || typeof timerCheckParams == 'undefined'){
				clearInterval(timerCheckParams);
			}
			timerCheckParams = null;
			timerCheckCount = 0;
		}
		
		if(eval(category == 4)){
			loadTableGroupData(_c);
		}else{
			loadTableData(_c);
		}
	}else{
		Ext.Msg.show({
			title : '错误',
			msg : '加载数据错误, 请刷新后重试'
		});
	}
}

function checkOutOnLoad() {	
	getOperatorName(pin, "../../");
	// 加载系统设置
	loadSystemSetting();
	// 加载折扣方案
	loadDiscountData();
	// 加载价格方案
	loadPricePlanData();
	// 加载主数据体
//	refreshCheckOutData();

};

createCancelFoodDetail = function(_data){
	if(_data == null || typeof _data == 'undefined' || typeof _data.root == 'undefined' || _data.root.length == 0){
		return;
	}
	
	cancelFoodDetailData = {totalProperty:0, root:[]};
	var cancelFoodPrice = 0.00, sumAmount = 0.00;
	
	var item = null;
	for(var i = 0; i < _data.root.length ; i++){		
		item = _data.root[i];
		if(typeof(item.amount) != 'undefined' && parseFloat(item.amount) < 0){
			item.amount = Math.abs(item.amount);
			item.backFoodPrice = Math.abs(parseFloat(item['unit_price'] * item.amount));			
			cancelFoodDetailData.root.push(item);
			cancelFoodPrice += parseFloat(item.backFoodPrice);
			sumAmount += parseFloat(item.amount);
		}
	}
	
	if(cancelFoodDetailData.root.length <= 0){
		return;
	}
	cancelFoodPrice = cancelFoodPrice.toFixed(2);
	sumAmount = sumAmount.toFixed(2);
	cancelFoodDetailData.root.push({
		food_name : '汇总',		
		amount : sumAmount,
		backFoodPrice : cancelFoodPrice
	});
	cancelFoodDetailData.totalProperty = cancelFoodDetailData.root.length;
	
	Ext.getDom('spanCancelFoodAmount').innerHTML = cancelFoodPrice;
};

var showCancelFoodDetailWin = null;
showCancelFoodDetail = function(){
	if(!showCancelFoodDetailWin){
		var grid = new Ext.grid.GridPanel({
			id : 'showCancelFoodDetailWinGrid',
			border : false,
			stripeRows : true,
			animate : false,
			animCollapse : true,
			autoScroll : true,
			loadMask : { msg: '数据请求中，请稍后...' },
			cm : new Ext.grid.ColumnModel([
			    new Ext.grid.RowNumberer(),
			    {header:'名称', dataIndex:'food_name', width:180},
			    {header:'日期', dataIndex:'order_date', width:130},			    
			    {header:'单价', dataIndex:'unit_price', width:80, align:'right', renderer:Ext.ux.txtFormat.gridDou},
			    {header:'退菜数量', dataIndex:'amount', width:80, align:'right', renderer:Ext.ux.txtFormat.gridDou},
			    {header:'退菜金额', dataIndex:'backFoodPrice', width:80, align:'right', renderer:Ext.ux.txtFormat.gridDou},
			    {header:'厨房', dataIndex:'kitchen', width:100},
			    {header:'服务员', dataIndex:'waiter', width:70},
			    {header:'备注', dataIndex:'comment', width:120}
			]),
			ds : new Ext.data.Store({
				proxy : new Ext.data.MemoryProxy(cancelFoodDetailData),
				reader : new Ext.data.JsonReader({
					totalProperty : 'totalProperty',
					root : 'root'
				}, [
				 	{name:'food_name'},
				 	{name:'order_date'},
				 	{name:'unit_price'},
				 	{name:'amount'},
				 	{name:'backFoodPrice'},
				 	{name:'kitchen'},
				 	{name:'waiter'},
				 	{name:'comment'}
				]),
				listeners : {
					
				}
			})
		});		
		
		showCancelFoodDetailWin = new Ext.Window({
			title : '退菜明细',
			width : 900,
			height : 350,
			resizable : false,
			modal : true,
			closable : false,
			constrainHeader : true,
			draggable : false,
			layout : 'fit',
			items : [grid],
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					showCancelFoodDetailWin.hide();
				}
			}],
			listeners : {
				show : function(){
					// 退菜明细
					Ext.Ajax.request({
						url : '../../QueryDetail.do',
						params : {
							time : new Date(),
							limit : 200,
							start : 0,
							pin : pin,
							queryType : 'TodayByTbl',
							tableAlias : tableID,
							restaurantID : restaurantID
						},
						success : function(response, options){
							createCancelFoodDetail(Ext.decode(response.responseText));
							Ext.getCmp('showCancelFoodDetailWinGrid').getStore().loadData(cancelFoodDetailData);
						},
						failure : function(res, options){
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}
		});
		
		grid.getStore().on('load', function(store){
			var count = store.getCount();
			if(count > 0){
				var sumRow = grid.getView().getRow(count - 1);
				sumRow.style.backgroundColor = '#EEEEEE';
				sumRow.style.color = 'green';
				for(var i = 0; i < grid.getColumnModel().getColumnCount(); i++){
					if(i == 0 || 3 || 4){
						var item = grid.getView().getCell(count-1, i);
						item.style.fontSize = '15px';
						item.style.fontWeight = 'bold';
						item = null;
					}
				}
			}
		});
	}
	showCancelFoodDetailWin.show();
};



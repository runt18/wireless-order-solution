/**
 * 加载折扣方案信息
 */
function loadDiscountData(){
	Ext.Ajax.request({
		url : '../../QueryDiscountTree.do',
		params : {
			pin : pin,
			restaurantID : restaurantID
		},
		success : function(response, options) {
			var jr = eval(response.responseText);
			discountData = jr;
			// 获取折扣方案分厨折扣
			Ext.Ajax.request({
				url : '../../QueryDiscountPlan.do',
				params : {
					pin : pin,
					restaurantID : restaurantID
				},
				success : function(res, opt){
					var jr = Ext.util.JSON.decode(res.responseText);
					discountPlanData = {root:[]};
					for(var i = 0; i < jr.root.length; i++){
						if(jr.root[i].rate > 0 && jr.root[i].rate < 1){
							discountPlanData.root.push(jr.root[i]);
						}
					}
					
					var discount = Ext.getCmp('comboDiscount');
					discount.store.loadData({root:discountData});
					
					// 设置默认显示折扣方案
					for(var i = 0; i < discountData.length; i++){
						if(eval(discountData[i].isDefault == true)){
							discount.setValue(discountData[i].discountID);
							break;
						}else if(eval(discountData[i].status == 2)){
							discount.setValue(discountData[i].discountID);
						}
					}
				
					// 3,请求口味
//					Ext.Ajax.request({
//						url : "../../QueryMenu.do",
//						params : {
//							pin : Request["pin"],
//							restaurantID : restaurantID,
//							type : 2
//						},
//						success : function(response,options) {
//							var resultTasteJSON = Ext.util.JSON.decode(response.responseText);
//							if (resultTasteJSON.success == true) {
//								dishTasteData = resultTasteJSON;									
//								
//								for(var i = 0; i < checkOutData.root.length; i++) {
//									checkOutDataDisplay.root.push(checkOutData.root[i]);
//								}
//								
//								for(var i = 0; i < checkOutDataDisplay.root.length; i++) {
//									var tpItem = checkOutDataDisplay.root[i];
//									
//									if(tpItem.special == true || tpItem.gift == true){
//										tpItem.discount = parseFloat(1).toFixed(2);
//									}else{
//										tpItem.discount = parseFloat(1).toFixed(2);
//										for(var di = 0; di < discountPlanData.root.length; di++){
//											if(discount.getValue() != -1 && discountPlanData.root[di].discount.id == discount.getValue() 
//													&& discountPlanData.root[di].kitchen.kitchenID == tpItem.kitchen.kitchenID){
//												tpItem.discount = parseFloat(discountPlanData.root[di].rate).toFixed(2);
//												break;
//											}
//										}
//									}
//									
//									tpItem.totalPrice = parseFloat((tpItem.unitPrice + tpItem.tastePrice) * tpItem.discount * tpItem.count);
//									
//									checkOutDataDisplay.root[i] = tpItem;
//								}
//								
//								checkOutStore.loadData(checkOutDataDisplay);
//								
//								// 4,算总价
//								var totalCount = 0;
//								var forFreeCount = 0;
//								for ( var i = 0; i < checkOutDataDisplay.root.length; i++) {
//									var tpItem = checkOutDataDisplay.root[i];
//									var singleCount = parseFloat(tpItem.totalPrice);
//									if (tpItem.gift == true) {
//										forFreeCount = forFreeCount + parseFloat(tpItem.discount) * tpItem.totalPrice;
//									} else {
//										totalCount = totalCount + singleCount;
//									}
//								}
//								
//								totalCount = totalCount.toFixed(2);
//								forFreeCount = forFreeCount.toFixed(2);
//								originalTotalCount = totalCount;
////								document.getElementById("totalCount").innerHTML = totalCount;
////								document.getElementById("forFree").innerHTML = forFreeCount;
////								document.getElementById("shouldPay").innerHTML = totalCount;
//								// 4,（尾数处理）
//								Ext.Ajax.request({
//									url : '../../QuerySystemSetting.do',
//									params : {
//										"restaurantID" : restaurantID
//									},
//									success : function(response, options) {
//										var resultJSON = Ext.decode(response.responseText);
//										if (resultJSON.success == true) {
//											restaurantData = resultJSON.other.systemSetting;
//											
//											if(restaurantData.setting.eraseQuotaStatus){
//												Ext.getDom('div_showEraseQuota').style.display = 'block';
//												Ext.getDom('font_showEraseQuota').innerHTML = parseFloat(restaurantData.setting.eraseQuota).toFixed(2);
//											}else{
//												Ext.getDom('div_showEraseQuota').style.display = 'none';
//												Ext.getDom('font_showEraseQuota').innerHTML = '';
//											}
//											
//											var sPay = document.getElementById("shouldPay").innerHTML;
//											
//											// 5,最低消费处理
//											var minCost = Request["minCost"];
//											if (parseFloat(minCost) > parseFloat(sPay)) {
//												sPay = minCost;
//												Ext.MessageBox.show({
//													msg : "消费额小于最低消费额，是否继续结帐？",
//													width : 300,
//													buttons : Ext.MessageBox.YESNO,
//													fn : function(btn) {
//														if (btn == "no") {
//															location.href = "TableSelect.html?pin=" + pin
//																			+ "&restaurantID="
//																			+ restaurantID;
//														}
//													}
//												});
//											}
//
//											// 6,尾数处理
//											if (restaurantData.setting.priceTail == 1) {
//												sPay = sPay.substr(0, sPay.indexOf(".")) + ".00";
//											} else if (restaurantData.setting.priceTail == 2) {
//												sPay = parseFloat(sPay).toFixed(0) + ".00";
//											}
//											
////											document.getElementById("shouldPay").innerHTML = sPay;
////											document.getElementById("actualCount").value = sPay;
////											document.getElementById("change").innerHTML = "0.00";
//
////											moneyCount("");
//
//										} else {
//											Ext.ux.showMsg(resultJSON);
//										}
//									},
//									failure : function(response,options) {
//										
//									}
//								});
//							} else {
//								var dataTasteInfo = resultTasteJSON.data;
//								Ext.MessageBox.show({
//									msg : dataTasteInfo,
//									width : 300,
//									buttons : Ext.MessageBox.OK
//								});
//							}
//						},
//						failure : function(response,options) { 
//							
//						}
//					});
				},
				failure : function(res, pot){
					Ext.ux.showMsg({
						success : true,
						code : 9999,
						msg : '加载分厨折扣信息失败.'
					});
				}
			});
		},
		failure : function(response, options) {
			Ext.ux.showMsg(Ext.decode(response.responseText));
		}
	});
}

/**
 * 加载价格方案信息
 */ 
function loadPricePlanData(){
	if(Ext.getCmp('comboPricePlan').store.getCount() == 0){
		Ext.Ajax.request({
			url : '../../QueryFoodPricePlanByOrder.do',
			params : {
				restaurantID : restaurantID,
				idList : checkOutData.other.idList
			},
			success : function(res, opt){
				var jr = Ext.decode(res.responseText);
				if(jr.success){
					pricePlanData = jr;		
					Ext.getCmp('comboPricePlan').store.loadData(pricePlanData);
					for(var i = 0; i < pricePlanData.root.length; i++){
						if(pricePlanData.root[i]['status'] == 1){
							var pp = Ext.getCmp('comboPricePlan');
							pp.setValue(pricePlanData.root[i]['id']);
						}
					}
				}else{
					Ext.ux.showMsg(jr);
				}
			},
			failure : function(res, pot){
				Ext.ux.showMsg(Ext.decode(res.responseText));
			}
		});
	}
};

/**
 * 加载账单基础汇总信息
 */
function loadOrderBasicMsg(){
	Ext.getCmp('numPersonCount').setValue(orderMsg.customNum > 0 ? orderMsg.customNum : 1);
	document.getElementById("serviceCharge").value = orderMsg.serviceRate * 100;
	document.getElementById("txtEraseQuota").value = orderMsg.erasePuotaPrice;
	document.getElementById("actualCount").value = parseFloat(orderMsg.acturalPrice).toFixed(2);
	document.getElementById("totalCount").innerHTML = parseFloat(orderMsg.totalPrice).toFixed(2);
	document.getElementById("shouldPay").innerHTML = parseFloat(orderMsg.acturalPrice).toFixed(2);
	document.getElementById("forFree").innerHTML = parseFloat(orderMsg.giftPrice).toFixed(2);
	document.getElementById("spanCancelFoodAmount").innerHTML = parseFloat(orderMsg.cancelPrice).toFixed(2);
	document.getElementById("change").innerHTML = '0.00';
	if(eval(orderMsg.category != 4) && eval(orderMsg.cancelPrice > 0)){
		Ext.getDom('spanSeeCancelFoodAmount').style.visibility = 'inherit';		
	}
}

/**
 * 加载系统设置
 */
function loadSystemSetting(){
	Ext.Ajax.request({
		url : '../../QuerySystemSetting.do',
		params : {
			"restaurantID" : restaurantID
		},
		success : function(response, options) {
			var jr = Ext.decode(response.responseText);
			if (jr.success == true) {
				restaurantData = jr.other.systemSetting;
				if(restaurantData.setting.eraseQuotaStatus){
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
		}
	});
}

/**
 * 加载单张餐桌账单信息
 */
function loadTableData(){
	Ext.Ajax.request({
		url : "../../QueryOrder.do",
		params : {
			pin : pin,
			restaurantID : restaurantID,
			tableID : tableID,
			orderID : orderID,
			calc : true,
			discountID : calcDiscountID,
			pricePlanID : calcPricePlanID
		},
		success : function(response, options) {
			var jr = Ext.decode(response.responseText);
			if (jr.success == true) {
				// 加载已点菜
				checkOutData = jr;
				orderMsg = jr.other.order;
				// 加载价格方案
				loadPricePlanData();
				// 加载显示账单基础信息
				loadOrderBasicMsg();
				// 
				checkOutGrid.getStore().loadData(checkOutData);
			} else {
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(response, options) { 
			Ext.ux.showMsg(Ext.decode(response.responseText));
		}
	});
}

/**
 * 加载餐桌组数据
 */
function loadTableGroupData(){
	Ext.Ajax.request({
		url : "../../QueryOrderGroup.do",
		params : {
			queryType : 0, ////////
			pin : pin,
			restaurantID : restaurantID,
			tableID : tableID,
			orderID : orderID,
			calc : true,
			discountID : calcDiscountID,
			pricePlanID : calcPricePlanID
		},
		success : function(response, options) {
			var jr = Ext.decode(response.responseText);
			if (jr.success == true) {
				// 加载已点菜
				checkOutData = jr;
				
			} else {
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(response, options) { 
			Ext.ux.showMsg(Ext.decode(response.responseText));
		}
	});
}

function refreshCheckOutData(){
	if(eval(category == 4)){
		loadTableGroupData();
	}else{
		loadTableData();
	}
}

function checkOutOnLoad() {	
	getOperatorName(pin, "../../");
	
	// 加载系统设置
	loadSystemSetting();
	// 加载折扣方案
	loadDiscountData();
	
	refreshCheckOutData();
	

};

//function moneyCount(opt) {
//	var actualPay = document.getElementById("actualCount").value;
//	var minCost = Request["minCost"];
//	var serviceRate = document.getElementById("serviceCharge").value;
//	var totalCount = document.getElementById("totalCount").innerHTML;
//	var eraseQuota = document.getElementById("txtEraseQuota").value;
//
//	var totalCount_out = "0.00";
//	var shouldPay_out = "0.00";
//	var change_out = "0.00";
//
//	if (serviceRate < 0 || serviceRate > 100) {
//		Ext.MessageBox.show({
//			msg : "服务费率范围是0%至100%！",
//			width : 300,
//			buttons : Ext.MessageBox.OK
//		});
//	}else {
//		if (restaurantData != null && typeof restaurantData != 'undefined' && restaurantData != {} && restaurantData != '' ) {
//			
//			// “应收”加上服务费
//			if (parseFloat(totalCount) < parseFloat(minCost)) {
//				shouldPay_out = parseFloat(minCost) * (1 + parseFloat(serviceRate) / 100);
//			} else {
//				shouldPay_out = parseFloat(originalTotalCount) * (1 + parseFloat(serviceRate) / 100);
//			}
//
//			// “应收”尾数处理
//			if (restaurantData.setting.priceTail == 1) {
//				if ((shouldPay_out + "").indexOf(".") != -1) {
//					shouldPay_out = (shouldPay_out + "").substr(0, (shouldPay_out + "").indexOf(".")) + ".00";
//				} else {
//					shouldPay_out = parseFloat(shouldPay_out).toFixed(2);
//				}
//			} else if (restaurantData.setting.priceTail == 2) {
//				shouldPay_out = parseFloat(shouldPay_out).toFixed(2);
//			} else {
//				shouldPay_out = parseFloat(shouldPay_out).toFixed(2);
//			}
//
//			// “合计”加上服务费
//			totalCount_out = (parseFloat(originalTotalCount) * (1 + parseFloat(serviceRate) / 100)).toFixed(2);
//			
//			if(restaurantData.setting.priceTail == 1){
//				shouldPay_out = parseFloat(parseInt(totalCount_out)).toFixed(2);
//			}else if(restaurantData.setting.priceTail == 2){
//				shouldPay_out = parseFloat(parseFloat(totalCount_out).toFixed(0)).toFixed(2);
//			}else{
//				shouldPay_out = parseFloat(totalCount_out).toFixed(2);
//			}
//			
//			// 必须在基础计算操作第一时间做判断 
//			eraseQuota = parseFloat(eraseQuota).toFixed(2);
//			
//			if(!isNaN(eraseQuota)){
//				eraseQuota = eraseQuota < 0.00 ? 0.00 : eraseQuota;
//				eraseQuota = eraseQuota > parseFloat(restaurantData.setting.eraseQuota) ? restaurantData.setting.eraseQuota : eraseQuota;
//				eraseQuota = eraseQuota > parseFloat(shouldPay_out) ? shouldPay_out : eraseQuota;
//			}else{
//				eraseQuota = 0;
//			}
//			// 实收金额 = (总计 -> 尾数处理) - 抹数金额;
//			shouldPay_out -= eraseQuota;
//			// “找零”计算
//			if (actualPay != "" && actualPay != "0.00") {
//				change_out = "0.00";
//				actualPay = eval(actualPay >= shouldPay_out) ? actualPay : shouldPay_out;
//				change_out = parseFloat(actualPay - shouldPay_out).toFixed(2);
//			}
//			
//			document.getElementById("totalCount").innerHTML = parseFloat(totalCount_out).toFixed(2);
//			document.getElementById("shouldPay").innerHTML = parseFloat(shouldPay_out).toFixed(2);
//			document.getElementById("change").innerHTML = parseFloat(change_out).toFixed(2);
//			document.getElementById("actualCount").value = parseFloat(actualPay).toFixed(2);
//			document.getElementById("txtEraseQuota").value = parseFloat(eraseQuota).toFixed(0);
//		}
//	}
//};

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
			width : 900,
			height : 300,
			stripeRows : true,
			animate : false,
			animCollapse : true,
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
			    {header:'备注', dataIndex:'comment', width:150}
			]),
			ds : new Ext.data.Store({
				proxy : new Ext.data.MemoryProxy(cancelFoodDetailData),
				reader : new Ext.data.JsonReader({
					totalProperty : 'totalProperty',
					root : 'root'
				},
				[
				 	{name:'food_name'},
				 	{name:'order_date'},
				 	{name:'unit_price'},
				 	{name:'amount'},
				 	{name:'backFoodPrice'},
				 	{name:'kitchen'},
				 	{name:'waiter'},
				 	{name:'comment'}
				])
			})
		});		
		
		showCancelFoodDetailWin = new Ext.Window({
			title : '退菜明细',
			resizable : false,
			modal : true,
			closable : false,
			constrainHeader : true,
			draggable : false,
			items : [grid],
			buttons : [{
				text : '退出',
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



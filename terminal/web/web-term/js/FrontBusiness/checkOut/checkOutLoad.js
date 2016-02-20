/**
 * 加载折扣方案信息
 */
function loadDiscountData(_c){
	if(_c == null || typeof _c == 'undefined'){
		_c = {};
	}
	Ext.Ajax.request({
		url : '../../QueryDiscount.do',
		params : {
			dataSource : 'role'
		},
		success : function(response, options) {
			var jr = Ext.decode(response.responseText);
			discountData = jr.root;
			var discount = Ext.getCmp('comboDiscount');
			discount.store.loadData({root:discountData});
			
			// 设置默认显示折扣方案
			for(var i = 0; i < discountData.length; i++){
				if(eval(discountData[i].isDefault == 1)){
					defaultsID = discountData[i].id;
					break;
				}else if(eval(discountData[i].status == 2)){
					defaultsID = discountData[i].id;
				}
			}
		},
		failure : function(response, options) {
			Ext.ux.showMsg(Ext.decode(response.responseText));
		},
		callback : _c.callback
	});
}

function checkDot(c)
{
	var r= /^[+-]?[1-9]?[0-9]*\.[0-9]*$/;
	return r.test(c);
}

/**
 * 加载账单基础汇总信息
 */
function loadOrderBasicMsg(){
	calcDiscountID = orderMsg.discount.id; // i
//	document.getElementById('spanDisplayCurrentDiscount').innerHTML = orderMsg.discount.name;
	
	document.getElementById('spanDisplayCurrentServiceRate').innerHTML = (orderMsg.serviceRate*100)+'%';
	document.getElementById("totalCount").innerHTML = checkDot(orderMsg.totalPrice)?parseFloat(orderMsg.totalPrice).toFixed(2) : orderMsg.totalPrice;
	document.getElementById("shouldPay").innerHTML = checkDot(orderMsg.actualPrice)?parseFloat(orderMsg.actualPrice).toFixed(2) : orderMsg.actualPrice;
	document.getElementById("forFree").innerHTML = checkDot(orderMsg.giftPrice)?parseFloat(orderMsg.giftPrice).toFixed(2) : orderMsg.giftPrice;
	document.getElementById("spanCancelFoodAmount").innerHTML = checkDot(orderMsg.cancelPrice)?parseFloat(orderMsg.cancelPrice).toFixed(2) : orderMsg.cancelPrice;
	document.getElementById("discountPrice").innerHTML = checkDot(orderMsg.discountPrice)?parseFloat(orderMsg.discountPrice).toFixed(2) : orderMsg.discountPrice;
	
//	var change = '0.00';
	
//	if(actualCount == '' || actualCount < orderMsg.actualPrice){
//		document.getElementById("actualCount").value = parseFloat(orderMsg.actualPrice).toFixed(2);
//	}else{
//		change = parseFloat(actualCount - orderMsg.actualPrice).toFixed(2);
//	}
//	document.getElementById("actualCount").value = parseFloat(orderMsg.actualPrice).toFixed(2);
	
//	document.getElementById("change").innerHTML = change;
	Ext.getCmp('numCustomNum').setValue(orderMsg.customNum > 0 ? orderMsg.customNum : 1);
	if(eval(orderMsg.category != 4)){
//		Ext.getCmp('numCustomNum').setDisabled(false);
	}
	if(eval(orderMsg.category != 4) && eval(orderMsg.cancelPrice > 0)){
		Ext.getDom('spanSeeCancelFoodAmountOperate').style.display = 'none';
		Ext.getDom('spanSeeCancelFoodAmount').style.display = 'block';		
	}
	
	var sumFoodCount = 0;
	for(var i = 0; i < checkOutData.root.length; i++){
		sumFoodCount += checkOutData.root[i].count;
	}
//	document.getElementById("spanSumFoodCount").innerHTML = sumFoodCount.toFixed(2);;
	var payOrderTitle = '结账 -- 账单号:<font color="red">' + orderMsg.id + '</font> ' + (orderMsg.isWeixinOrder?'(<span id="showWeixinOrder" style="font-size:15px;font-weight:bold;color:green;text-decoration:underline">微信账单</span>)' : '');
	if(orderMsg.category != 4){
		payOrderTitle += (' -- 餐桌号:<font color="red" size=3>' + orderMsg.table.alias + '</font>&nbsp;' + (tableDate.name?'<font color="red" size=3>(' + tableDate.name +')</font>' :''));
	}
	payOrderTitle += '&nbsp;&nbsp;&nbsp;<font size=3>当前折扣:</font> <font style="color:green;font-size:15px;font-weight:bold;">'+ orderMsg.discount.name + '</font>'
	if(orderMsg.discounter){
		payOrderTitle += '&nbsp;&nbsp;&nbsp;<font size=3>折扣人:</font> <font style="color:green;font-size:15px;font-weight:bold;">'+ orderMsg.discounter + '</font>';
		payOrderTitle += '&nbsp;&nbsp;&nbsp;<font size=3>折扣时间:</font> <font style="color:green;font-size:15px;font-weight:bold;">'+ orderMsg.discountDate + '</font>';
	}
	checkOutMainPanel.setTitle(payOrderTitle);
	
	Ext.getCmp('txtEraseQuota').setValue();
	
	$('#showWeixinOrder').hover(function(){
		if(!weixinOrderDetailWin.loadOrder){
			loadWeixinOrderDetail();
			weixinOrderDetailWin.loadOrder = true;
		}
		weixinOrderDetailWin.setPosition($('#showWeixinOrder').position().left + 475, $('#showWeixinOrder').position().top + 60);
		weixinOrderDetailWin.show();		
		
	}, function(){
		weixinOrderDetailWin.hide();
	});	
	
}


//加载微信账单的基本信息
function loadWeixinOrderWin(){
	if(!weixinOrderDetailWin){
		weixinOrderDetailWin = new Ext.Window({
			id : 'checkOut_weixinOrder',
			closable : false, //是否可关闭
			resizable : false, //大小调整
			width : 480,	
			items : []	
		});
	}
}

function loadWeixinOrderDetail(){
	for (var i = 0; i < weixinOrders.length; i++) {
		var foods = '';
		for (var j = 0; j < weixinOrders[i].foods.length; j++) {
			if(foods){
				foods += "，";
			}
			foods += (weixinOrders[i].foods[j].name + ' (' + weixinOrders[i].foods[j].count + ')');
		}
		
		var item = {
			layout : 'column',
			frame : true,
			border : true,
		 	defaults : {
		 		labelWidth : 60,
		 		labelAlign : 'right',
		 		style : 'font-size:15px;margin-bottom:5px;'
		 	},				
			items : [{
				columnWidth : .11,
				xtype : 'label',
				text : '时间:'
			},{
				columnWidth : .40,
				xtype : 'label',
				style : 'font-size:15px;font-weight:bold;color:green;',
				text : weixinOrders[i].date
			},{
				columnWidth : 0.11,
				xtype : 'label',
				text : '会员:'
			},{
				columnWidth : 0.38,
				xtype : 'label',
				style : 'font-size:15px;font-weight:bold;color:green;',
				text : weixinOrders[i].member.name + (weixinOrders[i].member.weixinCard?' (' + weixinOrders[i].member.weixinCard +')' : '')
			},{
				columnWidth : 1
			},
			{
				columnWidth : 0.1,
				xtype : 'label',
				text : '菜品:'
			},{
				columnWidth : 0.9,
				xtype : 'label',
				style : 'font-size:15px;font-weight:bold;',
				text : foods
			}]			
		}
		
		weixinOrderDetailWin.add(item);
	}
	weixinOrderDetailWin.doLayout();
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
		success : function(response, options) {
			var jr = Ext.decode(response.responseText);
			if (jr.success == true) {
				restaurantData = jr.other.systemSetting;
				if(restaurantData.setting.eraseQuota > 0){
					
					$('#trEraseQuota').show();
					Ext.getDom('div_showEraseQuota').style.display = 'block';
					Ext.getDom('font_showEraseQuota').innerHTML = restaurantData.setting.eraseQuota;
				}else{
//					Ext.getDom('trEraseQuota').style.display = 'none';
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
	var servicePlan = Ext.getCmp("comboServicePlan").getValue();
	var customNum = Ext.getCmp("numCustomNum").getValue();
	eraseQuota = typeof eraseQuota != 'undefined' && eval(eraseQuota >= 0) ? eraseQuota : 0;
	customNum = typeof customNum != 'undefined' && eval(customNum > 0) ? customNum : 1;
	Ext.Ajax.request({
		url : "../../QueryOrderByCalc.do",
		params : {
			tableID : tableID,
			orderID : orderID,
			calc : typeof _c.calc == 'boolean' ? _c.calc : true,
			discountID : calcDiscountID,
//			pricePlanID : calcPricePlanID,
//			eraseQuota : eraseQuota,
			customNum : customNum,
			servicePlan : servicePlan
		},
		success : function(response, options) {
			var jr = Ext.decode(response.responseText);
//			checkOutForm.buttons[7].setDisabled(false);
			if (jr.success == true) {
				setFormButtonStatus(false);
				// 加载已点菜
				checkOutData = jr;
				checkOutData.root = jr.other.order.orderFoods;
				// 加载显示账单基础信息
				orderMsg = jr.other.order;
				
				//赋值总额用于抹数计算
				checkOut_actualPrice = jr.other.order.actualPrice;
				
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

var timerCheckCount = 0;
function refreshCheckOutData(_c){
	if(timerCheckCount < 10*10){
		// 加载参数检查定时器
		if(typeof calcDiscountID == 'undefined'){
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
	new Ext.form.NumberField({
		id : 'txtEraseQuota',
		width : 95,
		height : 30,
		style : 'font-size:20px;background: #f9f9c0 repeat-x 0 0;',
		renderTo : 'spanHasEraseQuota',
		listeners : {
			'render': {
			    fn: function(c){
			        c.getEl().on(
			            'keyup',
			            function() {
			            		var shouldPay = checkOut_actualPrice - c.getEl().dom.value;
			            		shouldPay = shouldPay < 0 ? 0 : shouldPay;
			            		Ext.getDom('shouldPay').innerHTML = checkDot(shouldPay)?shouldPay.toFixed(2) : shouldPay;
			            }
			        );
			    },
			    scope: this
			 
			}						
		}
	});
	getOperatorName("../../", function(staff){
		// 加载折扣方案
		loadDiscountData({
			staff : staff
		});		
	});
	// 加载系统设置
	loadSystemSetting();
	// 加载价格方案
//	loadPricePlanData();
	//
	loadTableData({
		calc : false
	});
};

function createCancelFoodDetail(_data){
	if(_data == null || typeof _data == 'undefined' || typeof _data.root == 'undefined' || _data.root.length == 0){
		return;
	}
	cancelFoodDetailData = {totalProperty:0, root:[]};
	var cancelFoodPrice = 0.00, sumAmount = 0.00;
	
	var item = null;
	for(var i = 0; i < _data.root.length ; i++){		
		item = _data.root[i];
		if(typeof(item.count) != 'undefined' && parseFloat(item.count) < 0){
			item.count = Math.abs(item.count);
			item.totalPrice = Math.abs(parseFloat(item['unitPrice'] * item.count));			
			cancelFoodDetailData.root.push(item);
			cancelFoodPrice += parseFloat(item.totalPrice);
			sumAmount += parseFloat(item.count);
		}
	}
	
	if(cancelFoodDetailData.root.length <= 0){
		return;
	}
	cancelFoodPrice = cancelFoodPrice.toFixed(2);
	sumAmount = sumAmount.toFixed(2);
	cancelFoodDetailData.totalProperty = cancelFoodDetailData.root.length;
	
//	Ext.getDom('spanCancelFoodAmount').innerHTML = cancelFoodPrice;
};

var showCancelFoodDetailWin = null;
showCancelFoodDetail = function(){
	if(!showCancelFoodDetailWin){
		var grid = createGridPanel(
			'showCancelFoodDetailWinGrid',
			'',
			'',
			'',
			'',
			[
			    [true,false,false,false],
			    ['日期','orderDateFormat',130],
			    ['名称','name',180],
			    ['单价','unitPrice', 80, 'right', 'Ext.ux.txtFormat.gridDou'],
			    ['退菜数量','count', 80, 'right', 'Ext.ux.txtFormat.gridDou'], 
			    ['退菜金额','totalPrice', 80, 'right', 'Ext.ux.txtFormat.gridDou'],
			    ['厨房','kitchen.name', 80],
			    ['服务员','waiter', 80],
			    ['退菜原因', 'cancelReason.reason'],
			    ['备注','comment', 120]
			],
			OrderFoodRecord.getKeys(),
			[],
			'',
			''
		);
		grid.frame = false;
		showCancelFoodDetailWin = new Ext.Window({
			title : '退菜明细',
			width : 998,
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
				show : function(thiz){
					thiz.center();
					// 退菜明细
					Ext.Ajax.request({
						url : '../../QueryDetail.do',
						params : {
							time : new Date(),
							limit : 200,
							start : 0,
							queryType : 'TodayByTbl',
							tableID : tableID
						},
						success : function(response, options){
							createCancelFoodDetail(Ext.decode(response.responseText));
							grid.getStore().loadData(cancelFoodDetailData);
						},
						failure : function(res, options){
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}
		});
		
//		grid.getStore().on('load', function(store){
//			var count = store.getCount();
//			if(count > 0){
//				var sumRow = grid.getView().getRow(count - 1);
//				sumRow.style.backgroundColor = '#EEEEEE';
//				sumRow.style.color = 'green';
//				for(var i = 0; i < grid.getColumnModel().getColumnCount(); i++){
//					if(i == 0 || 3 || 4){
//						var item = grid.getView().getCell(count-1, i);
//						item.style.fontSize = '15px';
//						item.style.fontWeight = 'bold';
//						item = null;
//					}
//				}
//			}
//		});
	}
	showCancelFoodDetailWin.show();
};

function fnRemberIsFastOrInput(type){
	if(type){
		showInputReciptWin();
	}else{
		paySubmit(1);
	}	
}


function fnMixedPay(){
	isMixedPay = true;
	if(!mixedPayWin){
		mixedPayWin = new Ext.Window({
			title : '混合结账',
			id : 'checkOut_mixedPay',
			closable : false, //是否可关闭
			resizable : false, //大小调整
			modal : true,
			width : 260,			
			items : [{
				id : 'mixedPayTypePanel',
				layout : 'column',
				frame : true,
				border : true,
			 	defaults : {
			 		xtype : 'panel',
			 		layout : 'form',
			 		labelWidth : 90
			 	},
			 	items : []
		 	}],
			bbar : [{
				text : '暂结',
				id : 'btnTempMixedPayInputRecipt',
				iconCls : 'btn_save',
				handler : function(e){
					var mixedPayMoney = Ext.get('shouldPay').dom.innerHTML;
 	    			for(var pay in payMoneyCalc){
 	    				if(typeof payMoneyCalc[pay] != 'boolean'){
 	    					mixedPayMoney = (mixedPayMoney * 10000 - payMoneyCalc[pay] * 10000)/10000; 
 	    				}
 	    			}					
					
					if(mixedPayMoney != 0){
						Ext.example.msg('提示', '混合结账的金额不等于账单的实收金额');
					}else{
						payTypeCash ='';
						for (var i = 0; i < payTypeData.length; i++) {
							var checked = document.getElementById('chbForPayType' + payTypeData[i].id).checked;
							if(checked && Ext.getCmp('numForPayType'+payTypeData[i].id).getValue()){
								if(payTypeCash){
									payTypeCash += '&';
								}
								payTypeCash += (payTypeData[i].id + ',' + Ext.getCmp('numForPayType'+payTypeData[i].id).getValue());  
							}
						}						
						
						paySubmit(101);
					}
				}
			},'->',{
				text : '结账',
				id : 'btnMixedPayInputRecipt',
				iconCls : 'btn_app',
				handler : function(e){
					var mixedPayMoney = Ext.get('shouldPay').dom.innerHTML;
 	    			for(var pay in payMoneyCalc){
 	    				if(typeof payMoneyCalc[pay] != 'boolean'){
 	    					mixedPayMoney = (mixedPayMoney * 10000 - payMoneyCalc[pay] * 10000)/10000; 
 	    				}
 	    			}					
					
					if(mixedPayMoney != 0){
						Ext.example.msg('提示', '混合结账的金额不等于账单的实收金额');
					}else{
						payTypeCash ='';
						for (var i = 0; i < payTypeData.length; i++) {
							var checked = document.getElementById('chbForPayType' + payTypeData[i].id).checked;
							if(checked && Ext.getCmp('numForPayType'+payTypeData[i].id).getValue()){
								if(payTypeCash){
									payTypeCash += '&';
								}
								payTypeCash += (payTypeData[i].id + ',' + Ext.getCmp('numForPayType'+payTypeData[i].id).getValue());  
							}
						}						
						
						paySubmit(100);
						mixedPayWin.hide();					
					}
				}				
			},{
				text : '关闭',
				id : 'btnCloseInputReciptWin',
				iconCls : 'btn_close',
				handler : function(e){
					mixedPayWin.hide();
					payMoneyCalc = {};
				}				
			}],
			keys : [{
				 key : Ext.EventObject.ENTER,
				 fn : function(){ 
					 Ext.getCmp('btnMixedPayInputRecipt').handler();
				 },
				 scope : this 
			}],
			listeners : {
				hide : function(){
					isMixedPay = false;
				}
			}
		});
	}

	Ext.getCmp('mixedPayTypePanel').removeAll();
	
	for (var i = 0; i < payTypeData.length; i++) {
		var checkBoxId = 'chbForPayType' + payTypeData[i].id,  numberfieldId = 'numForPayType' + payTypeData[i].id;
		Ext.getCmp('mixedPayTypePanel').add({
	 		columnWidth : .12,	
	 	    items : [{
	 	    	xtype : 'checkbox',
	 	    	id : checkBoxId,
	 	    	relativePrice : numberfieldId,
	 	    	style : 'height: 20px;width: 20px;vertical-align: middle;',
	 	    	hideLabel : true,
	 	    	listeners : {
	 	    		render : function(thiz){
	 	    			thiz.getEl().dom.parentNode.style.paddingTop = '5px';
	 	    		},
	 	    		check : function(checkbox, checked){
	 	    			var numForAlias = Ext.getCmp(checkbox.relativePrice);
						if(checked){
							payMoneyCalc[checkbox.relativePrice] = true;
							
		 	    			var mixedPayMoney = Ext.get('shouldPay').dom.innerHTML;
		 	    			for(var pay in payMoneyCalc){
		 	    				if(typeof payMoneyCalc[pay] != 'boolean'){
		 	    					mixedPayMoney = (mixedPayMoney * 10000 - payMoneyCalc[pay] * 10000)/10000; 
		 	    				}
		 	    			}
		 	    			numForAlias.setValue(mixedPayMoney < 0? 0 : mixedPayMoney);							
							
							numForAlias.enable();
							numForAlias.focus(true, 100);
						}else{
							payMoneyCalc[checkbox.relativePrice] = false;
							
							numForAlias.disable();
							numForAlias.setValue();		
							numForAlias.clearInvalid();
						}
					},
					//解决第一次点击无效
					focus : function(thiz){
						var numForAlias = Ext.getCmp(thiz.relativePrice);
						if(document.getElementById(thiz.id).checked){
							
							numForAlias.disable();
						}else{
							numForAlias.enable();
							numForAlias.focus(true, 100);
						}
					}
	 	    	}
	 	    }]			 		
	 	});				
	 	
		Ext.getCmp('mixedPayTypePanel').add({
	 		columnWidth : .88,
	 		items : [{
	 			xtype : 'numberfield',
	 	    	id : numberfieldId,
	 	    	fieldLabel : '<font style="font-size:20px;text-align:right">' + payTypeData[i].name +'</font>',
	 	    	style : 'text-align:right;font-size:19px;height:20px;',
	 	    	decimalPrecision : 2,
	 	    	allowBlank : false,
	 	    	maxValue : 99999.99,
	 	    	minValue : 0.00,
	 	    	width : 85,
	 	    	disabled : true,
	 	    	validator : function(v){
	 	    		if(v >= 0.00 && v <= 99999.99){
	 	    	    	return true;
	 	    	    }else{
	 	    	    	return '价格需在 0.00  至 99999.99 之间!';
	 	    	    }
	 	    	},
	 	    	listeners : {
	 	    		blur : function(thiz){
	 	    			if(thiz.getValue()){
	 	    				payMoneyCalc[thiz.id] = thiz.getValue();
	 	    			}
	 	    		}
	 	    	}
	 		}]		
		});			 	
	}

	Ext.getCmp('mixedPayTypePanel').doLayout();
	
	mixedPayWin.show();
	
}

function showInputReciptWin(){
	
	if(!inputReciptWin){
		inputReciptWin = new Ext.Window({
			id : 'checkOut_inputRecipt',
			closable : false, //是否可关闭
			resizable : false, //大小调整
			modal : true,
			width : 300,			
			items : [{
				layout : 'form',
				frame : true,
				border : true,
				labelWidth : 120,
				labelAlign : 'right',
				items : [{
					xtype : 'textfield',
					id : 'txtShouldToRecipt',
					width : 130,
					fieldLabel : '<font style="font-size:25px;text-align:right">消费金额</font>',
					style : 'font-size:26px;height:30px;',
					disabled : true
				},{
					xtype : 'numberfield',
					id : 'txtInputRecipt',
					width : 130,
					fieldLabel : '<font style="font-size:25px;">输入收款</font>',
					allowBlank : false,
					style : 'font-size:26px;height:30px;',
					enableKeyEvent : true,
					validator : function(v){
						if(Ext.util.Format.trim(v).length > 0){
							if((v - Ext.getCmp('txtShouldToRecipt').getValue()) >= 0){
								return true;
							}else{
								return '收款金额不能小于消费金额';
							}
						}else{
							return '收款不允许为空';
						}
					},
					listeners : {
						'render': function(c){
						        c.getEl().on(
						            'keyup',
						            function() {
						            	if((c.getEl().dom.value - Ext.getCmp('txtShouldToRecipt').getValue()) > -1){
						            		Ext.getCmp('txtReciptReturn').setValue(eval(c.getEl().dom.value - Ext.getCmp('txtShouldToRecipt').getValue()));
						            	}else{
						            		Ext.getCmp('txtReciptReturn').setValue();
						            	}
						            	 
						            }
						        );
								new Ext.KeyMap('txtInputRecipt', [{
									key : 13,
									scope : this,
									fn : function(){
										Ext.getCmp('btnMixedPayInputRecipt').handler();
									}
								}]);						        
						    }
						    
					}
					
				},{
					xtype : 'numberfield',
					id : 'txtReciptReturn',
					width : 130,
					fieldLabel : '<font style="font-size:25px;text-align:right">找零</font>',
					style : 'font-size:26px;height:30px;',
					disabled : true
				}]				
			}],
			bbar : ['->',{
				text : '结账',
				id : 'btnMixedPayInputRecipt',
				iconCls : 'btn_save',
				handler : function(e){
					if(!Ext.getCmp('txtInputRecipt').isValid()){
						return;
					}
					paySubmit(1);
					inputReciptWin.hide();
				}				
			},{
				text : '关闭',
				id : 'btnCloseInputReciptWin',
				iconCls : 'btn_close',
				handler : function(e){
					inputReciptWin.hide();
				}				
			}],
			listeners : {
				hide : function(){
					Ext.getCmp('txtShouldToRecipt').setValue();
					Ext.getCmp('txtInputRecipt').setValue();
					Ext.getCmp('txtReciptReturn').setValue();
					Ext.getCmp('txtInputRecipt').clearInvalid();				
				}
			}		
		});
	}
	inputReciptWin.show();
	Ext.getCmp('txtShouldToRecipt').setValue(Ext.get('shouldPay').dom.innerHTML);
	Ext.getCmp('txtInputRecipt').focus(true, 100);
}



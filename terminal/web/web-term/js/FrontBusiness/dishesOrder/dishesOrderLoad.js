function tableStuLoad() {
	if(isFree){
		orderPanel.setTitle('已点菜列表 -- 操作类型: <font color="red">新下单</font>');
	}else{
		orderPanel.setTitle('已点菜列表 -- 操作类型: <font color="red">改单</font>');
	}
	if(!isRepaid){
		orderPanel.setTitle(orderPanel.title + String.format(' -- 餐台号: <font color="red" size=3>{0}</font>&nbsp;<font color="red" size=3>{1}</font>', tableAliasID, tableDate.name));
	}
};
// loading taste 
function tasteOnLoad() {
	Ext.Ajax.request({
		url : '../../QueryMenu.do',
		params : {
			dataSource : 'tastes',
			type : 2
		},
		success : function(response, options) {
			var rj = Ext.decode(response.responseText);
			if (rj.success == true) {
				tasteMenuData.root = [];
				allTasteData.root = [];
				ggTasteData.root = [];
				
				for(var i = 0; i < rj.root.length; i++){
					tasteMenuData.root.push(rj.root[i]);
					if(rj.root[i].taste.cateStatusValue == 2){
						allTasteData.root.push(rj.root[i]);
					}else if(rj.root[i].taste.cateStatusValue == 1){
						ggTasteData.root.push(rj.root[i]);
					}
				}
				allTasteGridForTabPanel.getStore().loadData(allTasteData);
				ggForTabPanel.getStore().loadData(ggTasteData);
			}
		},
		failure : function(response, options) {
			Ext.ux.showMsg(Ext.decode(response.responseText));
		}
	});
};

/**
 * 加载单张账单信息
 */
function loadSingleOrderData(resultJSON){
	
		// 加载普通账单信息
		if (resultJSON.success == true) {
			orderSingleData = resultJSON;
			if(isRepaid){
				Ext.Ajax.request({
					url : '../../QuerySystemSetting.do',
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						sysSetting = jr.other.systemSetting;
						var eraseQuota = parseInt(sysSetting.setting.eraseQuota);
						
						if(eraseQuota > 0){
							Ext.getDom('fontShowEraseQuota').innerHTML = eraseQuota.toFixed(2);
							Ext.getCmp('numErasePrice').setDisabled(false);
						}else{
							Ext.getDom('fontShowEraseQuota').innerHTML = 0.00;
							Ext.getCmp('numErasePrice').setDisabled(true);
						}
					},
					failure : function(res, opt) { 
						Ext.ux.showMsg(Ext.decode(res.responseText));
					}
				});
				
				initOrderSingleUI({
					callBack : function(grid, c){
						grid.order = orderSingleData.other.order;
						grid.order.orderFoods = orderSingleData.root;
						grid.getStore().loadData(orderSingleData);
					}
				});
				// 加载账单基础信息
				Ext.getCmp('txtSettleTypeFormat').setValue(orderSingleData.other.order.settleTypeText);
				Ext.getCmp('numErasePrice').setValue(orderSingleData.other.order.erasePrice);
				Ext.getCmp('serviceRate').setText((orderSingleData.other.order.serviceRate * 100) + '%');
				Ext.getCmp('repaid_comboServicePlan').setValue(orderSingleData.other.order.servicePlanId);
				
				if(orderSingleData.other.order.payTypeValue == 100){
					for (var j = 0; j < orderSingleData.other.order.mixedPayment.payTypes.length; j++) {
						var temp_payType = orderSingleData.other.order.mixedPayment.payTypes[j];
						for (var i = 0; i < repaid_payType.length; i++) {
							if(Ext.getDom('repaid_chbForPayType' + repaid_payType[i].id).value == temp_payType.pId){
								Ext.getCmp('repaid_chbForPayType' + repaid_payType[i].id).setValue(true);
								Ext.getCmp('repaid_numForPayType' + repaid_payType[i].id).enable();
								Ext.getCmp('repaid_numForPayType' + repaid_payType[i].id).setValue(temp_payType.money);
								break;
							}
						}						
					}
				}

				//会员反结账, 根据会员类型获取折扣
				var getDisParam = {dataSource : 'role'};
				//FIXME
/*				if(orderType == 'member'){
					getDisParam = {dataSource : 'getByMemberType', memberTypeId : re_member.memberType.id}
				}*/
				Ext.Ajax.request({
					url : '../../QueryDiscount.do',
					params : getDisParam,
					success : function(res, opt) {
						var jr = Ext.decode(res.responseText);
						discountData = jr.root
						
						var discount = Ext.getCmp('comboDiscount');
						discount.store.loadData(jr);
						discount.setValue(orderSingleData.other.order.discount.id);
						//利用缓时来选中收款方式, 否则不能选中
						var payManner = document.getElementsByName('radioPayType');
						for(var i = 0; i < payManner.length; i++){
							if(payManner[i].value == orderSingleData.other.order.payTypeValue){
								payManner[i].checked = true;
								break;
							}
						}
						orderSingleGridPanel.getStore().loadData({root:orderSingleGridPanel.order.orderFoods});
					},
					failure : function(res, opt) {
						Ext.MessageBox.show({
							title : '警告',
							msg : '加载折扣方案信息失败.',
							width : 300
						});
					}
				});
				
				Ext.Ajax.request({
					url : '../../QueryServicePlan.do',
					params : {dataSource : 'planTree'},
					success : function(res, opt) {
						servicePlanData = eval(res.responseText);
						var servicePlan = Ext.getCmp('repaid_comboServicePlan');
						servicePlan.store.loadData(servicePlanData);
						servicePlan.setValue(orderSingleData.other.order.servicePlanId);
					},
					failure : function(res, opt) {
						Ext.MessageBox.show({
							title : '警告',
							msg : '加载方案信息失败.',
							width : 300
						});
					}
				});				
				//会员价格方案
				if(orderType != 'common'){
					
					var memberType = re_member.memberType, pricePlanCbo = Ext.getCmp('repaid_txtPricePlanForPayOrder'), 
					couponCbo = Ext.getCmp('repaid_couponForPayOrder');
					if(memberType.pricePlans && memberType.pricePlans.length > 0){
						Ext.getCmp('box4RepaidPricePlan').show();
						var mpo_pricePlanData = memberType.pricePlans; 
						memberType.pricePlans.unshift({id : '-1', name : '普通价'});
						pricePlanCbo.store.loadData(mpo_pricePlanData);
						pricePlanCbo.setValue(memberType.pricePlan.id);
					}else{
						Ext.getCmp('box4RepaidPricePlan').hide();
					}		
					
					if(re_member.coupons){
						Ext.getCmp('box4RepaidCoupon').show();
						var list = [[-1,'不使用']];
						for (var i = 0; i < re_member.coupons.length; i++) {
							list.push([re_member.coupons[i].couponId, re_member.coupons[i].couponType.name]);
						}
						couponCbo.store.loadData(list);
						if(orderSingleData.other.order.coupon){
							couponCbo.setValue(orderSingleData.other.order.coupon.couponId);
						}
					}else{
						Ext.getCmp('box4RepaidCoupon').hide();	
					}
				}
				
				
			}else{
				// 更新菜品状态为已点菜
				refreshOrderFoodDataType(orderSingleData.root);
				// 初始化界面
				initOrderSingleUI({
					callBack : function(grid, c){
						grid.order = orderSingleData.other.order;
						grid.order.orderFoods = orderSingleData.root;
						grid.getStore().loadData(orderSingleData);
						orderGroupDisplayRefresh({
							control : grid
						});
					}
				});
			}
		} else {
			initOrderSingleUI();
		}
}


/**
 * 初始化菜品数量设置菜单
 */
function initMenuForOperationFoodCount(){
	var menuOperationFoodCount = new Ext.menu.Menu({
		id : 'menuOperationFoodCount',
		hideOnClick : false,
		items : [new Ext.Panel({
			frame : true,
			width : 150,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				labelWidth : 30,
				items : [{
					xtype : 'numberfield',
					id : 'numOperationFoodCount',
					fieldLabel : '数量',
					width : 80
				}]
			}],
			bbar : ['->', {
				text : '确定',
				id : 'btnSaveOperationFoodCount',
				iconCls : 'btn_save',
				handler : function(e){
					var count = Ext.getCmp('numOperationFoodCount');
					if(!count.isValid()){
						return;
					}
					Ext.getCmp('btnCancelOperationFoodCount').handler();
					orderFoodCountOperationHandler({
						otype : 1,
						count : count.getValue()
					});
				}
			}, {
				text : '关闭',
				id : 'btnCancelOperationFoodCount',
				iconCls : 'btn_close',
				handler : function(e){
					Ext.menu.MenuMgr.get('menuOperationFoodCount').hide();
				}
			}]
		})],
		listeners : {
			show : function(){
				var count = Ext.getCmp('numOperationFoodCount');
				count.setValue();
				count.clearInvalid();
				count.focus.defer(100, count);
			}
		},
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnSaveOperationFoodCount').handler();
			}
		}, {
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnCancelOperationFoodCount').handler();
			}
		}]
	});
	menuOperationFoodCount.render(document.body);
}

/**
 * 创建账单菜品列表上工具栏
 */
function createOrderFoodGridPanelTbar(){
	var tbar = new Ext.Toolbar({
		height : 26,
		listeners : {
			render : function(e){
				e.add({
					text : '选择口味',
					hidden : isRepaid,
					iconCls : 'icon_tb_taste',
					handler : function(){
						orderTasteRendererHandler();
					}
				}, '-', {
					text : '数量+1',
					iconCls : 'btn_add',
					handler : function(){
						if(!checkSselectedData()){
							return;
						}
						orderFoodCountOperationHandler({
							otype : 0,
							count : 1
						});
					}
				}, '-', {
					text : '数量-1',
					iconCls : 'btn_delete',
					handler : function(){
						if(!checkSselectedData()){
							return;
						}
						orderFoodCountOperationHandler({
							otype : 0,
							count : -1
						});
					}
				}, '-', {
					text : '数量设置',
					id : 'btnOperationFoodCount',
					iconCls : 'icon_tb_setting',
					handler : function(e){
						if(!checkSselectedData()){
							return;
						}
						orderFoodCountRendererHandler({
							x : e.getEl().getX(),
							y : (e.getEl().getY() + e.getEl().getHeight())
						});
					}
				}, '-', {
					text : '删除菜品',
					iconCls : 'btn_cancel',
					handler : function(){
						orderDeleteFoodOperationHandler();
					}
				});
				if(!isFree && !isRepaid){
					e.add('-', {
						text : '补打总单',
						iconCls : 'icon_tb_print_all',
						handler : function(){
							var tempMask = new Ext.LoadMask(document.body, {
								msg : '正在请求操作, 请稍候...',
								remove : true
							});
							tempMask.show();
							Ext.Ajax.request({
								url : '../../PrintOrder.do',
								params : {
									'tableID' : tableDate.id,
									'printType' : 14
								},
								success : function(response, options) {
									tempMask.hide();
									var jr = Ext.decode(response.responseText);
									Ext.example.msg(jr.title, jr.msg);
								},
								failure : function(response, options) {
									tempMask.hide();
									Ext.ux.showMsg(Ext.decode(response.responseText));
								}
							});
						}
					}, '-', {
						text : '补打明细',
						iconCls : 'icon_tb_print_detail',
						handler : function(){
							Ext.Msg.show({
								msg : '是否补打账单明细?',
								icon : Ext.MessageBox.QUESTION,
								buttons : Ext.Msg.YESNO,
								fn : function(btn){
									if(btn == 'yes'){
										var tempMask = new Ext.LoadMask(document.body, {
											msg : '正在请求操作, 请稍候...',
											remove : true
										});
										tempMask.show();
										Ext.Ajax.request({
											url : '../../PrintOrder.do',
											params : {
												'tableID' : tableDate.id,
												'printType' : 2
											},
											success : function(response, options) {
												tempMask.hide();
												var jr = Ext.decode(response.responseText);
												Ext.example.msg(jr.title, jr.msg);
											},
											failure : function(response, options) {
												tempMask.hide();
												Ext.ux.showMsg(Ext.decode(response.responseText));
											}
										});
									}
								}
							});
						}
					});
				}
				//FIXME 注释了isGroup
/*				if(isGroup){
					e.add('-', {
						xtype : 'tbtext',
						text : '&nbsp;账单操作范围:&nbsp;'
					}, {
						xtype : 'radio',
						name : 'radioOrderGroupOperationScope',
						boxLabel : '全组&nbsp;',
						checked : true,
						inputValue : 1
					}, {
						xtype : 'radio',
						name : 'radioOrderGroupOperationScope',
						boxLabel : '单张&nbsp;',
						inputValue : 2
					});
				}*/
			}
		}
	});
	return tbar;
}

/**
 * 初始化单张账单信息
 */
function initOrderSingleUI(_c){
	_c = _c != null && typeof _c != 'undefined' ? _c : {};
	if(!orderSingleGridPanel){
		orderSingleGridPanel = createGridPanel(
			'orderSingleGridPanel',
			'',
			'',
			'',
			'',
			[
			    [true, false, false, false],
			    ['菜名', 'displayFoodName', 200] , 
				['口味', 'tasteGroup.tastePref', 180, '', 'orderOrderGridPanelTasteRenderer'], 
				['数量', 'count', 130, 'right', 'foodCountAddOrDeleteRenderer'],
				['单价', 'unitPrice', 80, 'right', 'Ext.ux.txtFormat.gridDou'],
				['下单时间', 'orderDateFormat', 150],
				['服务员', 'waiter', 80],
				['操作','operate', 80, 'cneter', 'orderGiftRenderer']
			],
			OrderFoodRecord.getKeys(),
			[],
			0,
			'',
			createOrderFoodGridPanelTbar()
		);
		orderSingleGridPanel.order = {orderFoods:[]};
		orderSingleGridPanel.getStore().on('load', function(thiz, records){
			for(var i = 0; i < records.length; i++){
				Ext.ux.formatFoodName(records[i], 'displayFoodName', 'name', 1);
			}
			for (var j = 0; j < giftRender.checkeds.length; j++) {
				$('#'+giftRender.checkeds[j]).attr('checked', 'checked');
			}
			
		});
		orderPanel.add(orderSingleGridPanel);
		orderPanel.doLayout();
	}
	// 执行回调函数
	if(typeof _c.callBack != 'undefined'){
		_c.callBack(orderSingleGridPanel, _c);
	}
}

/**
 * 初始化账单组UI
 */
function initOrderGroupUI(_c){
	_c = _c != null && typeof _c != 'undefined' ? _c : {};
	if(!orderGroupGridTabPanel){
		orderGroupGridTabPanel  = new Ext.TabPanel({
			frame : true,
			enableTabScroll : true,
			activeTab : 0,
			tbar : createOrderFoodGridPanelTbar()
		});
		orderPanel.add(orderGroupGridTabPanel);
		orderPanel.doLayout();
	}
	// 执行回调函数
	if(typeof _c.callBack != 'undefined'){
		_c.callBack(orderGroupGridTabPanel, _c);
	}
}

/**
 * 初始化检查
 */
function initPasswordWin(){
	var winValidPassword = Ext.getCmp('winValidPassword');
	if(!winValidPassword){
		var numCount = new Ext.form.NumberField({
			xtype : 'numberfield',
			id : 'numCancelOrderFoodCount',
			width : 100,
			fieldLabel : '数量',
			validator : function(v){
				if(v >= 0 && v <= 255){
					return true;
				}else{
					return '菜品数量在 1 ~ 255 之间';
				}
			}
		});
		var txtPassword = new Ext.form.TextField({
			xtype : 'textfield',
			inputType : 'password',
			id : '',
			width : 100,
			fieldLabel : '密码'
		});	
		winValidPassword = new Ext.Window({
			id : 'winValidPassword',
			renderTo : document.body,
			title : '&nbsp;',
			modal : true,
			resizable : false,
			closable : false,
			width : 180,
			height : 95,
			layout : 'fit',
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				labelWidth : 40,
				items : [numCount]
			}],
			bbar : ['->', {
				text : '确定',
				iconCls : 'btn_save',
				handler : function(){
					if(!numCount.isValid()){
						return;
					}
					if (isGroup) {
						orderOrderDeleteFoodOperationHandler({
							count : (numCount.getValue() * -1)
						});
					}else{
						orderSingleDeleteFoodOperationHandler({
							grid : orderSingleGridPanel,
							count : (numCount.getValue() * -1)
						});
					}
					winValidPassword.hide();
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					winValidPassword.hide();
				}
			}],
			listeners : {
				show : function(thiz){
					var data = false;
					if (isGroup) {
						data = Ext.ux.getSelData(orderGroupGridTabPanel.getActiveTab());
					}else{
						data = Ext.ux.getSelData(orderSingleGridPanel);
					}
					numCount.setValue(data.count);
					txtPassword.setValue();
					
					numCount.clearInvalid();
					txtPassword.clearInvalid();
				}
			}
		});
	}
}

/**
 * 初始化
 */
function loadOrderData() { 
	// 
	initMenuForOperationFoodCount();
	loadSingleOrderData(primaryOrderData);
	
};

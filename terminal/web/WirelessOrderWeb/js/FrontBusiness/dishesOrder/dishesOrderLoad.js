function tableStuLoad() {
	if(isFree){
		orderPanel.setTitle('已点菜列表 -- 操作类型: <font color="red">新下单</font>');
	}else{
		orderPanel.setTitle('已点菜列表 -- 操作类型: <font color="red">改单</font>');
	}
	if (isGroup) {
//		centerPanel.setTitle(centerPanel.title);
	}else{
//		centerPanel.setTitle(centerPanel.title + String.format(' -- 餐台号: <font color="red">{0}</font>', tableAliasID));
		orderPanel.setTitle(orderPanel.title + String.format(' -- 餐台号: <font color="red">{0}</font>', tableAliasID));
	}
};

// loading taste 
function tasteOnLoad() {
	Ext.Ajax.request({
		url : '../../QueryMenu.do',
		params : {
			isCookie : true,
			dataSource : 'tastes',
			restaurantID : restaurantID,
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
					if(rj.root[i].taste.cateValue == 0){
						allTasteData.root.push(rj.root[i]);
					}else if(rj.root[i].taste.cateValue == 2){
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
function loadSingleOrderData(){
	if(tableStatus == 1){
		// 加载普通账单信息
		Ext.Ajax.request({
			url : '../../QueryOrder.do',
			params : {
				
				'tableID' : tableAliasID
			},
			success : function(response, options) {
				var resultJSON = Ext.util.JSON.decode(response.responseText);
				if (resultJSON.success == true) {
					orderSingleData = resultJSON;
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
				} else {
					Ext.MessageBox.show({
						msg : resultJSON.msg,
						width : 300,
						buttons : Ext.MessageBox.OK
					});
				}
			},
			failure : function(response, options) {
				
			}
		});
	}else{
		initOrderSingleUI();
	}
}

/**
 * 加载账单组信息
 */
function loadOrderGroupData(){
	Ext.Ajax.request({
		url : '../../QueryOrderGroup.do',
		params : {
			isCookie : true,
			'restaurantID' : restaurantID,
			'queryType' : 0,
			'childTableAliasID' : tableAliasID,
			'hasFood' : true
		},
		success : function(response, options) {
			var jr = Ext.decode(response.responseText);
			if(jr.success){
				orderGroupData = jr;
				initOrderGroupUI({
					callBack : function(grid, e){
						var og = orderGroupData.root;
						var activeTab=null;
						for(var i = 0; i < og[0].childOrder.length; i++){
							var tempItem = og[0].childOrder[i];
							var tempID = 'orderGridItemID';
							tempID = tempID + '_' + tempItem.tableID;
							var orderGridItem = Ext.getCmp(tempID);
							if(!orderGridItem){
								orderGridItem = createGridPanel(
									tempID,
									'',
									'',
									'',
									'',
									[
									 [true, false, false, false], 
									 ['菜名', 'displayFoodName', 200] , 
									 ['口味', 'tastePref', 160] , 
									 ['数量', 'count', 130, 'right', 'foodCountAddOrDeleteRenderer'],
									 ['单价', 'unitPrice', 80, 'right', 'Ext.ux.txtFormat.gridDou'],
									 ['下单时间', 'orderDateFormat', 150],
									 ['服务员', 'waiter', 80],
									 ['操作', 'operation', , 'center', 'orderOrderGridPanelRenderer']
									 ],
									 ['seqID', 'displayFoodName', 'foodName', 'foodID', 'aliasID', 'tastePref', 'tastePrice', 'tasteGroup', 'isHangup', 'discount',
									  'count', 'unitPrice', 'acturalPrice', 'discount', 'totalPrice', 'orderDateFormat', 'waiter', 'special', 'soldout', 'dataType',
									  'weight', 'stop', 'gift', 'hot', 'recommend', 'currPrice', 'combination', 'temporary','tmpTastePrice', 'dataType'],
									  [],
									  0,
									  ''
								);
								orderGridItem.frame = false;
								orderGridItem.getStore().on('load', function(thiz, records){
									for(var ti = 0; ti < records.length; ti++){
										Ext.ux.formatFoodName(records[ti], 'displayFoodName', 'foodName');
									}
								});
								orderGridItem.render(document.body, 0);
								orderGridItem.setTitle('账单号:'+tempItem.id);
								orderGroupGridTabPanel.add(orderGridItem);
								activeTab = i == 0 ? orderGridItem : activeTab;
							}
							// 设置已点菜状态
							refreshOrderFoodDataType(tempItem.orderFoods);
							orderGridItem.order = tempItem;
							orderGridItem.getStore().loadData({
								root : tempItem.orderFoods
							});
							orderGroupDisplayRefresh({
								control : orderGridItem
							});
							if(activeTab != null && orderGroupGridTabPanel.getActiveTab() == null){
								orderGroupGridTabPanel.setActiveTab(activeTab);
							}
						}
					}
				});
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(response, options) {
			Ext.ux.showMsg(Ext.decode(response.responseText));
		}
	});
}

/**
 * 初始化菜品数量设置菜单
 */
function initMenuForOperationFoodCount(){
	var menuOperationFoodCount = new Ext.menu.Menu({
		id : 'menuOperationFoodCount',
		hideOnClick : false,
		items : [new Ext.menu.Adapter(new Ext.Panel({
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
					width : 80,
					validator : function(v){
						if(v >= 1 && v <= 255){
							return true;
						}else{
							return '菜品数量在 1 ~ 255 之间.';
						}
					} 
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
		}), {hideOnClick : false})],
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
				if(!isFree && !isGroup){
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
									isCookie : true,
									'tableID' : tableAliasID,
									'printType' : 1
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
												isCookie : true,
												'tableID' : tableAliasID,
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
				if(isGroup){
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
				}
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
				['服务员', 'waiter', 80]
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
				Ext.ux.formatFoodName(records[i], 'displayFoodName', 'name');
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
				if(v >= 1 && v <= 255){
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
			title : '验证密码',
			modal : true,
			resizable : false,
			closable : false,
			width : 180,
			height : 125,
			layout : 'fit',
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				labelWidth : 40,
				items : [numCount, txtPassword]
			}],
			bbar : ['->', {
				text : '确定',
				iconCls : 'btn_save',
				handler : function(){
					if(!numCount.isValid()){
						return;
					}
					var pwdTrans;
					if(txtPassword.getRawValue() == ''){
						pwdTrans = '';
					}else{
						pwdTrans = MD5(txtPassword.getValue());
					}
					var mask = new Ext.LoadMask(document.body, {
					    msg  : '正在验证密码, 请稍等......',
					    disabled : false,
					    removeMask : true
					});
					
					mask.show();
					Ext.Ajax.request({
						url : '../../VerifyPwd.do',
						params : {
							isCookie : true,
							'type' : 5,
							'pwd' : pwdTrans
						},
						success : function(response, options) {
							var jr = Ext.decode(response.responseText);
							if(jr.success){
								winValidPassword.hide();
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
							}else{
								Ext.MessageBox.show({
									msg : jr.data,
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
							mask.hide();
						},
						failure : function(response, options) {
							var jr = Ext.decode(response.responseText);
							mask.hide();
							Ext.MessageBox.show({
								msg : jr.data,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						}
					});
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
	
	if (isGroup) {
		loadOrderGroupData();
	}else{
		//	加载数据
		loadSingleOrderData();
	}
};

var orderGroupWin;
function loadDataForOrderGroup(_c){
	_c = _c != null && typeof _c != 'undefined' ? _c : {};
	var tableOrderLoadingMask = new Ext.LoadMask(document.body, {
		msg : '餐桌数据加载中, 请稍后.......',
		disabled : false,
		remove : true
	});
	tableOrderLoadingMask.show();
	Ext.Ajax.request({
		url : '../../QueryOrderGroup.do',
		params : {
			restaurantID : restaurantID,
			queryType : 0,
			status : 0
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				tableOrderLoadingMask.hide();
				var cgData = {root:[]}, egData = {root:[]};
				for(var i = 0; i < jr.root.length; i++){
					if(jr.root[i].category == 4){
						for(var k = 0; k < jr.root[i].childOrder.length; k++){
							egData.root.push({
								parentID : jr.root[i].id,
								tableID : jr.root[i].childOrder[k].tableID,
								tableAlias : jr.root[i].childOrder[k].tableAlias,
								tableName : jr.root[i].childOrder[k].tableName,
								tableStatus : jr.root[i].childOrder[k].tableStatus
							});
						}
					}else{
						// 团体操作不加载单张已点菜餐台
						if(_c.otype != 1){
							cgData.root.push({
								parentID : null,
								tableID : jr.root[i].tableID,
								tableAlias : jr.root[i].tableAlias,
								tableName : jr.root[i].tableName,
								tableStatus : jr.root[i].tableStatus
							});
						}
					}
				}
				Ext.getCmp('westGridPanel').getStore().removeAll();
				Ext.getCmp('orderGroupCenterGridPanel').getStore().loadData(cgData);
				Ext.getCmp('eastGridPanel').getStore().loadData(egData);
				
				if(typeof _c.callBack == 'function'){
					_c.callBack(jr);
				}
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(res, opt){
			tableOrderLoadingMask.hide();
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
};

westGridPanelDelete = function(v){
	var gp = Ext.getCmp('westGridPanel');
	gp.getStore().each(function(r){
		if(r.get('tableAlias') == v){
			gp.getStore().remove(r);
			gp.getView().refresh();
			return false;
		}
	});
};
centerGridPanelInsert = function(ri){
	var gp = Ext.getCmp('orderGroupCenterGridPanel');
	gp.fireEvent('rowdblclick', gp, ri);
};
eastGridPanelInsertGroup = function(ri){
	var gp = Ext.getCmp('eastGridPanel');
	gp.fireEvent('rowdblclick', gp, ri);
};

allGridPanelTableStatusRenderer = function(v, md, r, ri, ci, store){
	return eval(v == 0) ? '否' : '是';
};

westGridPanelOperationRenderer = function(v, md, r, ri, ci, store){
	return ''
		   + '<a href="javascript:westGridPanelDelete('+r.get('tableAlias')+');">删除</a>';
};
centerGridPanelOperationRenderer = function(v, md, r, ri, ci, store){
	return ''
	   + '<a href="javascript:centerGridPanelInsert('+ri+');">添加</a>';
};
eastGridPanelOperationRenderer = function(v, md, r, ri, ci, store){
	var isPay = orderGroupWin.otype == 2;
	return ''
		   + (isPay ? '<a href="javascript:eastGridPanelInsertGroup('+ri+');">添加该组</a>' : '')
		   + '&nbsp;&nbsp;'
		   + '<a href="javascript:Ext.getCmp(\'btnCancelTableGroup\').handler();">取消该组</a>';
};

function oOrderGroup(_c){
	_c = _c != null && typeof _c != 'undefined' ? _c : {};
	if(typeof _c.type != 'number'){
		return;
	}
	
	if(!orderGroupWin){
		var westGridPanel = createGridPanel(
			'westGridPanel',
			'已选择餐桌',
			'',
			250,
			'',
			[
				[true, false, false, false], 
				['编号', 'tableAlias', 60],
//				['名称', 'tableName', 100],
				['已开台', 'tableStatus', 70, 'center', 'allGridPanelTableStatusRenderer'],
				['操作', 'operation', 60, 'center', 'westGridPanelOperationRenderer']
			],
			['tableID', 'tableAlias', 'tableName', 'tableStatus'],
			[['isPaging', false], ['restaurantID', restaurantID], ['pin', pin]],
			30,
			''
		);
		westGridPanel.region = 'west';
		
		var centerGridPanelTbar = new Ext.Toolbar({
			height : 26,
			items : [{
				xtype : 'tbtext',
				text : '编号:'
			}, {
				xtype : 'numberfield',
				id : 'numSreachAliasForOrderGroup',
				style : 'text-align:left;',
//				maxValue : 65535,
//				mixValue : 0,
				width : 80,
				validator : function(v){
					if(v >= 0 && v <= 65535){
						orderGroupKeyupHandler();
						return true;
					}else{
						return '餐桌编号范围在 0 至 65535之间.';
					}
				}
			}, '->', {
				text : '刷新',
//				hidden : true,
				iconCls : 'btn_refresh',
				handler : function(){
					Ext.getCmp('numSreachAliasForOrderGroup').setValue();
					orderGroupKeyupHandler();
				}
			}]
		});
		var centerGridPanel = createGridPanel(
			'orderGroupCenterGridPanel',
			'普通餐桌',
			'',
			'',
			'',
			[
				[true, false, false, false], 
				['编号', 'tableAlias', 60],
//				['名称', 'tableName', 100],
				['已开台', 'tableStatus', 70, 'center', 'allGridPanelTableStatusRenderer'],
				['操作', 'operation', 60, 'center', 'centerGridPanelOperationRenderer']
			],
			['tableID', 'tableAlias', 'tableName', 'tableStatus'],
			[['isPaging', false], ['restaurantID', restaurantID], ['pin', pin]],
			30,
			'',
			centerGridPanelTbar
		);
		centerGridPanel.region = 'center';
		centerGridPanel.on('rowdblclick', function(thiz, ri, e){
			var sr = thiz.getStore().getAt(ri);
			var check = true;
			westGridPanel.getStore().each(function(r){
				if(orderGroupWin.otype == 1){
					if(eval(r.get('parentID') > 0)){
						Ext.example.msg('提示', '已添加团组信息, 请重新选择或重置信息后继续操作.');
						check = false;
						return false;
					}
				}
				if(r.get('tableID') == sr.get('tableID')){
					Ext.example.msg('提示', '已添加该餐桌信息, 请重新选择.');
					check = false;
					return false;
				}
			});
			if(check){
				westGridPanel.getStore().insert(0, sr);
				westGridPanel.getView().refresh();
				thiz.getStore().remove(sr);
				thiz.getView().refresh();
			}
		});
		
		var eastPanelTbar = new Ext.Toolbar({
			height : 26,
			items : ['->', {
				text : '展开/收缩',
				iconCls : 'icon_tb_toggleAllGroups',
				handler : function(){
					eastPanel.getView().toggleAllGroups();
				}
			}, {
				text : '取消',
				id : 'btnCancelTableGroup',
				iconCls : 'btn_delete',
				handler : function(){
					if(eastPanel.getStore().getCount() == 0){
//						Ext.example.msg('提示', '操作错误, 没有团体餐桌, 请确认后重试.');
						return;
					}
					var sd = Ext.ux.getSelData(eastPanel);
					if(!sd){
						Ext.example.msg('提示', '操作错误, 请选择一个团体餐桌后重试.');
						return;
					}
					Ext.Msg.show({
						title : '提示',
						msg : '是否取消选中餐桌组信息?',
						icon : Ext.Msg.QUESTION,
						buttons : Ext.Msg.YESNO,
						fn : function(e){
							if(e == 'yes'){
								var loading = new Ext.LoadMask(document.body, {
								    msg  : '正在更新餐台组信息, 请稍等......',
								    disabled : false,
								    removeMask : true
								});
								loading.show();
								Ext.Ajax.request({
									url : '../../CancelOrderGroup.do',
									params : {
										pin : pin,
										orderID : sd.parentID
									},
									success : function(res, opt){
										loading.hide();
										var jr = Ext.decode(res.responseText);
										if(jr.success){
											Ext.getCmp('btnResettingOrderGroup').handler();
										}
										Ext.ux.showMsg(jr);
									},
									failure : function(res, opt){
										loading.hide();
										Ext.ux.showMsg(Ext.decode(res.responseText));
									}
								});
							}
						}
					});
				}
			}]
		});
		
		var eastPanel = createGridPanel(
			'eastGridPanel',
			'团体餐桌',
			'',
			310,
			'',
			[
				[true, false, false, false], 
				['编号', 'tableAlias', 60],
//				['名称', 'tableName', 120],
				['操作', 'operation', 130, 'center', 'eastGridPanelOperationRenderer'],
				['parentID', 'parentID', 10]
			],
			['tableID', 'tableAlias', 'tableName', 'parentID'],
			[['isPaging', false], ['restaurantID', restaurantID], ['pin', pin]],
			30,
			{ name : 'parentID', hide : true, sort : 'tableAlias' },
			eastPanelTbar
		);
		eastPanel.region = 'east';
		eastPanel.view.groupTextTpl = '餐桌数量:{[values.rs.length]}';
		eastPanel.on('rowdblclick', function(thiz, ri, e){
			if(orderGroupWin.otype != 2){
				return false;
			}
			var sr = thiz.getStore().getAt(ri);
			var check = true;
			if(westGridPanel.getStore().getCount() == 0){
				westGridPanel.getStore().each(function(r){
					if(typeof r.get('parentID') == 'number'){
						if(r.get('parentID') == sr.get('parentID')){
							Ext.example.msg('提示', '已添加该餐桌组信息, 请重新选择.');
						}else{
							Ext.example.msg('提示', '已添加其他餐桌组信息, 请重新选择.');
						}
						check = false;
						return false;
					}
				});
			}else{
				Ext.example.msg('提示', '已选择其他餐桌信息, 不允许再操作组信息, 请重新选择或重置后继续操作.');
				check = false;
				return false;
			}
			if(check){
				for(var i = thiz.getStore().getCount() - 1; i >= 0; i--){
					if(thiz.getStore().getAt(i).get('parentID') == sr.get('parentID')){
						westGridPanel.getStore().insert(0, thiz.getStore().getAt(i));
						westGridPanel.getView().refresh();
						thiz.getStore().remove(thiz.getStore().getAt(i));
					}
				}
				thiz.getView().refresh();
			}
		});
		
		orderGroupWin = new Ext.Window({
			otype : 1,
			title : '&nbsp;',
			modal : true,
			closable : false,
			resizable : false,
			width : 800,
			height : 450,
			layout : 'border',
			items : [westGridPanel, centerGridPanel, eastPanel],
			bbar : [{
				xtype : 'tbtext',
				text : '说明:暂不支持并台改单, 并台下单必须是为已开台餐桌.'
			},
			/*{
				xtype : 'tbtext',
				text : '操作类型:&nbsp;'
			}, {
				xtype : 'radio',
				id : 'orderGroupOtypeRadioForDC',
				name : 'orderGroupOtypeRadio',
				boxLabel : '<font color="red">点菜</font>',
				inputValue : 1,
				listeners : {
					render : function(e){
						Ext.getDom(e.getId()).onclick = function(){
							if(e.getValue()){
								orderGroupWin.setTitle('并台点菜');
								var btnOperationOrderGroup = Ext.getCmp('btnOperationOrderGroup');
								if(btnOperationOrderGroup && typeof btnOperationOrderGroup != 'undefined'){
									btnOperationOrderGroup.setText('点菜');
									orderGroupWin.otype = 1;
								}
								loadDataForOrderGroupHandler({
									otype : orderGroupWin.otype
								});
							}
						};
					}
				}
			}, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			}, {
				xtype : 'radio',
				id : 'orderGroupOtypeRadioForJZ',
				name : 'orderGroupOtypeRadio',
				boxLabel : '<font color="red">结账</font>',
				inputValue : 2,
				listeners : {
					render : function(e){
						Ext.getDom(e.getId()).onclick = function(){
							if(e.getValue()){
								orderGroupWin.setTitle('并台结账');
								var btnOperationOrderGroup = Ext.getCmp('btnOperationOrderGroup');
								if(btnOperationOrderGroup && typeof btnOperationOrderGroup != 'undefined'){
									btnOperationOrderGroup.setText('结账');
									orderGroupWin.otype = 2;
								}
								loadDataForOrderGroupHandler({
									otype : orderGroupWin.otype
								});
							}
						};
					}
				}
			}, */'->', {
				text : '重置',
				id : 'btnResettingOrderGroup',
				iconCls : 'btn_refresh',
				handler : function(){
					loadDataForOrderGroupHandler({
						otype : orderGroupWin.otype
					});
				}
			}, {
				text : '&nbsp;',
				id : 'btnOperationOrderGroup',
				iconCls : 'btn_save',
				handler : function(e){
					if(westGridPanel.getStore().getCount() == 0){
						Ext.example.msg('提示', '请选择餐桌后再操作.');
						return;
					}
					var tables = [], otype = 0, parentID = 0;
					westGridPanel.getStore().each(function(r){
						tables.push({
							id : r.get('tableID'),
							alias : r.get('tableAlias')
						});
						if(otype == 0 && typeof r.get('parentID') == 'number' && eval(r.get('parentID') > 0)){
							parentID = r.get('parentID');
							otype = 1;
						}
					});
					var btnSave = e;
					var btnCancel = Ext.getCmp('btnCancelOperationOrderGroup');
					btnSave.setDisabled(true);
					btnCancel.setDisabled(true);
					Ext.Ajax.request({
						url : '../../UpdateOrderGroup.do',
						params : {
							dataSource : 'updateTable',
							pin : pin,
							restaurantID : restaurantID,
							otype : otype,
							tables : Ext.encode(tables),
							parentID : parentID
						},
						success : function(res, opt){
							btnSave.setDisabled(false);
							btnCancel.setDisabled(false);
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								if(orderGroupWin.otype == 1){
									location.href = "OrderMain.html?"
										+ "&pin=" + pin
										+ "&restaurantID=" + restaurantID
										+ "&category=" + 4
										+ "&tableAliasID=" + tables[0].alias
										+ "&orderID=" + jr.other.orderID
										+ "&ts=" + 1;  // 团体操作暂定为都是改单操作
								}else if(orderGroupWin.otype == 2){
									location.href = "CheckOut.html?"
										+ "orderID=" + (otype == 1 ? parentID : jr.other.orderID)
										+ "&pin=" + pin
										+ "&restaurantID=" + restaurantID
										+ "&category=" + 4;
								}
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt){
							btnSave.setDisabled(false);
							btnCancel.setDisabled(false);
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}, {
				text : '关闭',
				id : 'btnCancelOperationOrderGroup',
				iconCls : 'btn_close',
				handler : function(){
					orderGroupWin.hide();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					orderGroupWin.hide();
				}
			}],
			listeners : {
				beforeshow : function(thiz){
					var btnOperationOrderGroup = Ext.getCmp('btnOperationOrderGroup');
					if(thiz.otype == 1){
//						eastPanel.hide();
						orderGroupWin.setTitle('并台点菜');
						if(btnOperationOrderGroup && typeof btnOperationOrderGroup != 'undefined'){
							btnOperationOrderGroup.setText('点菜');
						}
						loadDataForOrderGroupHandler({
							otype : orderGroupWin.otype
						});
					}else if(thiz.otype == 2){
//						eastPanel.show();
						orderGroupWin.setTitle('合单结账');
						if(btnOperationOrderGroup && typeof btnOperationOrderGroup != 'undefined'){
							btnOperationOrderGroup.setText('结账');
						}
						loadDataForOrderGroupHandler({
							otype : orderGroupWin.otype
						});
					}
				},
				show : function(thiz){
					thiz.center();
//					Ext.getCmp('orderGroupOtypeRadioForDC').setValue(true);
//					Ext.getDom('orderGroupOtypeRadioForDC').onclick();
				},
				hide : function(){
					westGridPanel.getStore().removeAll();
					centerGridPanel.getStore().removeAll();
					eastPanel.getStore().removeAll();
				}
			}
		});
	}
	orderGroupWin.otype = _c.type;
	orderGroupWin.show();
	
};


function loadDataForOrderGroupHandler(_c){
	_c = _c != null && typeof _c != 'undefined' ? _c : {};
	
	if(_c.otype == 1){
		// 团体点菜操作
		// 加载最新团体餐桌信息
		loadDataForOrderGroup({
			otype : _c.otype,
			callBack : function(ogData){
				// 加载所有餐桌信息
				getData({
					callBack : function(tData){
						var normalTemp = {root:[]};
						// 过滤空闲餐桌信息
						for(var i = 0; i < tData.root.length; i++){
							if(eval(tData.root[i].status == 0)){
								normalTemp.root.push({
									parentID : null,
									tableID : tData.root[i].tableId,
									tableAlias : tData.root[i].aliasId,
									tableName : tData.root[i].name,
									tableStatus : tData.root[i].status
								});
							}
						}
						//
						Ext.getCmp('orderGroupCenterGridPanel').getStore().loadData(normalTemp);
					}
				});
			}
		});
	}else if(_c.otype == 2){
		// 团体结账操作
		loadDataForOrderGroup();
	}
}

function orderGroupKeyupHandler(){
	var alias = Ext.getCmp('numSreachAliasForOrderGroup').getRawValue();
	var centerGridPanel = Ext.getCmp('orderGroupCenterGridPanel');
	var store = centerGridPanel.getStore();
	var searchData = {root:[]}, orderByData = [], otherData = [];
	if(alias.length > 0){
		for(var i = 0; i < store.getCount(); i++){
			if((store.getAt(i).data.tableAlias+'').indexOf(alias) >= 0){
				orderByData.push(store.getAt(i).data);	    						
			}else{
				otherData.push(store.getAt(i).data);
			}
		}
		for(var i = 0; i < orderByData.length; i++){
			searchData.root.push(orderByData[i]);
		}
		for(var i = 0; i < otherData.length; i++){
			searchData.root.push(otherData[i]);
		}
		store.loadData(searchData);
	}
	var selRow;
	for(var i = 0; i < store.getCount(); i++){
		selRow = centerGridPanel.getView().getRow(i);
		if(i < orderByData.length){
			selRow.style.backgroundColor = '#FFFF00';
		}else{
			selRow.style.backgroundColor = '#FFFFFF';
		}
	}
};

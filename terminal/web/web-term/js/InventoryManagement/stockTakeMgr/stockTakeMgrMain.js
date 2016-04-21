//-----------------lib.js--------
function audit(){
	var data = Ext.ux.getSelData(stockTakeGrid);
	data['statusValue'] = 3;
	updateStockTakeHandler();
}

function setCateValue(materialCateId){
	Ext.getCmp('comboMaterialCateId').setValue(materialCateId);
}
/**
 * 
 * @param c
 * @returns
 */
function operateStockTakeDate(c){
	if(c == null || c.otype == null || typeof c.otype == 'undefined')
		return;
	
	var id = Ext.getCmp('hideStockTakeId');
	var dept = Ext.getCmp('comboStockTakeDept');
	var cate = Ext.getCmp('comboMaterialCate');
//	var cateId = Ext.getCmp('comboMaterialCateId');
	var comment = Ext.getCmp('txtStockTakeComment');
	var approver = Ext.getCmp('txtStockTakeApprover');
	var approverDate = Ext.getCmp('txtStockTakeApproverDate');
	var operator = Ext.getCmp('txtStockTakeOperator');
	var operatorDate = Ext.getCmp('txtStockTakeOperatorDate');
	
	if(c.otype == Ext.ux.otype['set']){
		var data = typeof c.data == 'undefined' ? {} : c.data;
		var deptData = typeof data.dept == 'undefined' ? {} : data.dept;
		var materialCate = typeof data['materialCate'] == 'undefined' ? {} : data['materialCate'];
		if(typeof data['cateTypeValue'] == 'undefined'){
			cate.setValue(1);
		}else{
			cate.setValue(data['cateTypeValue']);
		}
		//触发小类别变动
		cate.fireEvent('select', cate);
		
		id.setValue(data['id']);
		dept.setValue(deptData['id']);
		
		comment.setValue(data['comment']);
		if(data['statusValue'] == 2){
			approver.setValue(data['approver']);
			approverDate.setValue(data['finishDateFormat']);
		}else{
			approver.setValue();
			approverDate.setValue();
		}
		
		operator.setValue(data['operator']);
		operatorDate.setValue(data['startDateFormat']);
		

		var gs = Ext.getCmp('stockTakeWinCenter').getStore();
		gs.removeAll();
		if(typeof data.detail != 'undefined' && data.detail.length > 0){
			for(var i = 0; i < data.detail.length; i++){
				var temp = data.detail[i];
				gs.add(new StockTakeDetailRecord({
					'material.name' : temp['material']['name'],
					material : {
						id : temp['material']['id'],
						name : temp['material']['name']
					},
					expectAmount : temp['expectAmount'],
					actualAmount : temp['actualAmount']
				}));
			}
		}
		if(typeof data.dept != 'undefined' && typeof data.cateTypeValue != 'undefined' && typeof data.materialCate != 'undefined'){
			stockTakeWin.takeContent = {
				dept : data.dept.id,
				cateType : data.cateTypeValue,
				cateId : data.materialCate.id
			};
		}else{
			stockTakeWin.takeContent = {
				dept : '',
				cateType : '',
				cateId : ''
			};
		}
		//FIXME 小类别combo选中,异步来不及缓冲数据, 设置时间暂停
		setTimeout("setCateValue("+materialCate["id"]+")", 600);
		
	}else if(c.otype == Ext.ux.otype['get']){
		
	}
}
/**
 * 新增盘点任务
 */
function insertStockTakeHandler(){
//FIXME 不需要验证当前月份	
//	Ext.Ajax.request({
//		url : '../../OperateStockTake.do',
//		params : {
//			dataSource : 'checkCurrentMonth'
//			
//		},
//		success : function(res, opt){
//			var jr = Ext.decode(res.responseText);
//			if(jr.success){
//
//			}else{
//				Ext.ux.showMsg(jr);
//			}
//		},
//		failure : function(res, opt){
//			Ext.ux.showMsg(Ext.decode(res.responseText));
//		}
//	});
	
	Ext.Ajax.request({
		url : '../../OperateStockTake.do',
		params : {
			dataSource : 'checkStockAction'
			
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				stockTakeWin.otype = Ext.ux.otype['insert'];
				stockTakeWin.show();
				stockTakeWin.setTitle('新增盘点任务');
				stockTakeWin.center();
				
				operateStockTakeDate({
					otype : Ext.ux.otype['set']
				});
				var dept = Ext.getCmp('comboStockTakeDept');
				var cate = Ext.getCmp('comboMaterialCate');
				var cateId = Ext.getCmp('comboMaterialCateId');
				dept.setDisabled(false);
				cate.setDisabled(false);
				cateId.setDisabled(false);
				
				Ext.getCmp('btnAuditStockTake').hide();
				Ext.getCmp('btnSaveStockTake').show();
				Ext.getCmp('btnExportStockTake').show();
			}else{
				jr['icon'] = Ext.Msg.WARNING; 
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});	

	
}
/**
 * 修改盘点任务
 */
function updateStockTakeHandler(){
	var data = Ext.ux.getSelData(stockTakeGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选择一条记录.');
		return;
	}
	
	var dept = Ext.getCmp('comboStockTakeDept');
	var cate = Ext.getCmp('comboMaterialCate');
	var cateId = Ext.getCmp('comboMaterialCateId');
	dept.setDisabled(true);
	cate.setDisabled(true);
	cateId.setDisabled(true);
	Ext.getCmp('btnExportStockTake').show();
	if(data['statusValue'] == 1){
		stockTakeWin.otype = Ext.ux.otype['update'];
		stockTakeWin.show();
		stockTakeWin.setTitle('修改盘点任务');
		stockTakeWin.center();
		Ext.getCmp('btnSaveStockTake').show();
		Ext.getCmp('btnAuditStockTake').hide();
		//Ext.getCmp('stockTakeWinWest').setDisabled(false);
	}else if(data['statusValue'] == 2){
		stockTakeWin.otype = Ext.ux.otype['select'];
		stockTakeWin.show();
		stockTakeWin.setTitle('查看盘点任务');
		stockTakeWin.center();
		Ext.getCmp('btnAuditStockTake').hide();
		Ext.getCmp('btnSaveStockTake').hide();
		Ext.getCmp('btnExportStockTake').hide();
	}else{
		data['statusValue'] = 1;
		stockTakeWin.otype = Ext.ux.otype['select'];
		stockTakeWin.show();
		stockTakeWin.setTitle('查看盘点任务');
		stockTakeWin.center();
		Ext.getCmp('btnAuditStockTake').show();
		Ext.getCmp('btnSaveStockTake').hide();
		Ext.getCmp('btnExportStockTake').hide();
	}
	operateStockTakeDate({
		otype : Ext.ux.otype['set'],
		data : data
	});
	loadOperateMaterial({selectType:1});
}
/**
 * 取消盘点任务
 */
function cancelStockTakeHandler(){
	var data = Ext.ux.getSelData(stockTakeGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选择一条记录.');
		return;
	}
	Ext.Msg.show({
		title : '重要',
		msg : '是否取消盘点任务',
		buttons : Ext.Msg.YESNO,
		icon: Ext.Msg.QUESTION,
		fn : function(e){
			if(e == 'yes'){
				Ext.Ajax.request({
					url : '../../OperateStockTake.do',
					params : {
						'dataSource' : 'cancel',
						
						id : data['id']
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('btnSearchForStockTake').handler();
						}else{
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.decode(res.responseText));
					}
				});
			}
		}
	});
}

/**
 * 
 * @param c
 */
function auditStockTakeHandlerCenter(c){
	Ext.Msg.show({
		title : '重要',
		msg : c.msg,
		buttons : Ext.Msg.YESNO,
		icon: Ext.Msg.QUESTION,
		fn : function(e){
			if(e == 'yes'){
				Ext.Ajax.request({
					url : '../../OperateStockTake.do',
					params : {
						'dataSource' : 'audit',
						
						id : c.data['id']
					},
					success : function(res, opt){
						
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							stockTakeWin.hide();
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('btnSearchForStockTake').handler();
						}else{
							if(jr.code == 7845){
								var auditStockTakeWin = Ext.getCmp('auditStockTakeWin');
								if(!auditStockTakeWin){
									auditStockTakeWin = new Ext.Window({
										title : '该盘点任务有盘漏物品, 请选择处理方式',
										width : 300,
										closable : false,
										resizable : false,
										modal : true,
										buttonAlign : 'center',
										bodyStyle : 'font-size:20px;',
										html : [
										    '忽略: 漏盘物品库存量不改变',
										    '清零: 漏盘物品库存量变为0',
										    '取消: 继续录入盘点单'
										].join('<br>'),
										buttons : [{
											text : '忽略',
											handler : function(){
												auditStockTakeWin.hide();
												operateMissDetail({
													miss : 1,
													data : c.data
												});
											}
										}, {
											text : '清零',
											handler : function(){
												auditStockTakeWin.hide();
												operateMissDetail({
													miss : 0,
													data : c.data
												});
											}
										},  {
											text : '取消',
											handler : function(){
												auditStockTakeWin.hide();
											}
										}]
									});
								}
								auditStockTakeWin.show();
								auditStockTakeWin.center();
							}else{
								Ext.ux.showMsg(jr);								
							}
						}
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.decode(res.responseText));
					}
				});
			}else{
				if(c.isCheck){
					Ext.getCmp('btnSearchForStockTake').handler();
				}
			}
		}
	});
}

/**
 * 审核盘点任务
 */
function auditStockTakeHandler(){
	var data = Ext.ux.getSelData(stockTakeGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选择一条记录.');
		return;
	}
	auditStockTakeHandlerCenter({
		msg : '是否审核盘点任务',
		data : data
	});
}

/**
 * 处理盘漏货品
 * @param c
 */
function operateMissDetail(c){
	var mask = new Ext.LoadMask(document.body, {
		msg : '数据正在处理中, 请稍候......',
		removeMask : true
	});
	mask.show();
	Ext.Ajax.request({
		url : '../../OperateStockTake.do',
		params : {
			'dataSource' : 'miss',
			
			id : c.data['id'],
			miss : c.miss
		},
		success : function(res, opt){
			mask.hide();
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				auditStockTakeHandlerCenter({
					msg : '盘漏货品已处理, 是否继续盘点?',
					data : c.data,
					isCheck : true
				});
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(res, opt){
			mask.hide();
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
	 
}

function setAmountForStockTakeDetail(c){
	var data = Ext.ux.getSelData(Ext.getCmp('stockTakeWinCenter'));
	for(var i = 0; i < Ext.getCmp('stockTakeWinCenter').getStore().getCount(); i++){
		var temp = Ext.getCmp('stockTakeWinCenter').getStore().getAt(i);
		if(temp.get('material.name') == data['material.name']){
			if(c.otype == Ext.ux.otype['set']){
				temp.set('actualAmount', c.amount);
			}else{
				var na = temp.get('actualAmount') + c.amount;
				if(na < 0){
					temp.set('actualAmount', 0);
				}else{
					temp.set('actualAmount', na);
				}
			}
			break;
		}
	}
}
//-----------
//--------load
function stockTakeGridOperateRenderer(v, m, r, ri, ci, s){
	if(r.get('statusValue') == 1){
		return ''
			+ '<a href="javascript:updateStockTakeHandler();">修改</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href="javascript:audit();">审核</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href="javascript:cancelStockTakeHandler();">取消</a>';
	}else{
		return '<a href="javascript:updateStockTakeHandler();">查看</a>';
		
	}
}

function actualStockTakeCount(event){
	if(!menuOperateActualAmount){
		initDetailActualAmountMenu();
	}
	menuOperateActualAmount.showAt([event.clientX, event.clientY]);
}

function actualAmountRenderer(v, m, r, ri, ci, s){
	if(stockTakeWin.otype == Ext.ux.otype['select']){
		return Ext.ux.txtFormat.gridDou(r.get('actualAmount'));
	}else{
		return Ext.ux.txtFormat.gridDou(r.get('actualAmount'))
		+ '<a href="javascript:setAmountForStockTakeDetail({amount:1});"><img src="../../images/btnAdd.gif" title="数量+1"/></a>&nbsp;'
		+ '<a href="javascript:setAmountForStockTakeDetail({amount:-1});"><img src="../../images/btnDelete.png" title="数量-1"/></a>&nbsp;'
		+ '<a href="javascript:" onClick="actualStockTakeCount(event)"><img src="../../images/icon_tb_setting.png" title="设置实际盘点数量"/></a>&nbsp;';		
	}
}
function shortageAmountRenderer(v, m, r, ri, ci, s){
	var deltaAmount = r.get('actualAmount') - r.get('expectAmount');
	deltaAmount = deltaAmount < 0 ? Ext.ux.txtFormat.gridDou(Math.abs(deltaAmount)) : 0;
	return "<font color='red' size='4'>" + deltaAmount + "</font>";
}
function overageAmountRenderer(v, m, r, ri, ci, s){
	var deltaAmount = r.get('actualAmount') - r.get('expectAmount');
	deltaAmount = deltaAmount > 0 ? Ext.ux.txtFormat.gridDou(Math.abs(deltaAmount)) : 0;
	return "<font color='green' size='4'>" + deltaAmount + "</font>";
}

function initGrid(){
	var stockTakeGridTbar = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '刷新',
			id : 'btnSearchForStockTake',
			iconCls : 'btn_refresh',
			handler : function(){
				
				var gs = stockTakeGrid.getStore();
				gs.load({
					params : {
						start : 0,
						limit : GRID_PADDING_LIMIT_20
					}
				});
			}
		}]
	});
	stockTakeGrid = createGridPanel(
		'stockTakeGrid',
		'盘点任务列表',
		'',
		'',
		'../../QueryStockTake.do',
		[
			[true, false, false, true],
			['盘点日期', 'startDateFormat'],
			['仓库', 'dept.name'],
			['货品类型', 'materialCate.name'],
			['盘点状态', 'statusText'],
			['审核人', 'approver'],
			['审核时间', 'finishDateFormat', null, null, function(v, m, r, ri, ci, s){
				if(r.get("statusValue") == 2){
					return r.get("finishDateFormat")
				}else{
					return "";
				}}
			],
			['备注', 'comment'],
			['操作', 'operate', ,'center', 'stockTakeGridOperateRenderer']
		],
		StockTakeRecord.getKeys(),
		[['restaurantId', restaurantID], ['dataSource', 'normal']],
		GRID_PADDING_LIMIT_20,
		'',
		stockTakeGridTbar
	);
	stockTakeGrid.region = 'center';
	stockTakeGrid.on('render', function(){
		Ext.getCmp('btnSearchForStockTake').handler();
	});
	stockTakeGrid.on('rowdblclick', function(){
		updateStockTakeHandler();
	});
	stockTakeGrid.getStore().on('load', function(store, records, options){
		var sumRow;
		for(var i = 0; i < records.length; i++){
			if(eval(records[i].get('statusValue') == 2)){
				sumRow = stockTakeGrid.getView().getRow(i);
				sumRow.style.backgroundColor = '#DDD';
				sumRow = null;
			}
		}
		sumRow = null;
	});
}

function initWin(){
	var stockTakeWinNorth = {
		region : 'north',
		height : 120,
		frame : true,
		items : [{
    		id : 'displayPanelForStockTakeTitle',
    		height : 30,
    		bodyStyle : 'font-size:18px;text-align:center;',
    		html : '原料盘点单'
    	}, {
			items : [{
				xtype : 'hidden',
				id : 'hideStockTakeId'
			}]
    	}, {
    		xtype : 'panel',
    		layout : 'column',
    		defaults : {
    			xtype : 'form',
    			layout : 'form',
    			style : 'width:218px;',
    			labelWidth : 60,
    			columnWidth : .25,
    			defaults : { width : 120 }
    		},
    		items : [{
    			items : [{
    				xtype : 'combo',
    				id : 'comboStockTakeDept',
    				fieldLabel : '部门',
    				readOnly : false,
    				forceSelection : true,
    				store : new Ext.data.JsonStore({
    					url: '../../OperateDept.do?',
    					baseParams : {
    						dataSource : 'getByCond',
    						inventory : true
    					},
    					root : 'root',
    					fields : DeptRecord.getKeys()
    				}),
    				valueField : 'id',
    				displayField : 'name',
    				typeAhead : true,
    				mode : 'local',
    				triggerAction : 'all',
    				selectOnFocus : true,
    				allowBlank : false,
    				blankText : '盘点仓库不允许为空.',
    				listeners : {
    					select : function(thiz){
    						checkTakeContentChange('dept', thiz);
    					},
    					render : function(thiz){
    						thiz.store.load();
    					}
    				}
    			}]
    		}, {
    			items : [{
    				xtype : 'combo',
    				id : 'comboMaterialCate',
    				fieldLabel : '货品类型',
    				readOnly : false,
    				forceSelection : true,
    				store : new Ext.data.SimpleStore({
    					data : winParams.cate,
    					fields : ['value', 'text']
    				}),
    				valueField : 'value',
    				displayField : 'text',
    				typeAhead : true,
    				mode : 'local',
    				triggerAction : 'all',
    				selectOnFocus : true,
    				allowBlank : false,
    				listeners : {
    					select : function(thiz){
    						checkTakeContentChange('cateType', thiz);
    					}
    				}
    			}]
    		}, {
    			items : [{
    				xtype : 'combo',
    				id : 'comboMaterialCateId',
    				fieldLabel : '类别',
    				forceSelection : true,
    				store : new Ext.data.JsonStore({
    					url : '../../QueryMaterialCate.do',
    					root : 'root',
    					baseParams : {
    						dataSource : 'normal',
    						restaurantID : restaurantID
    					},
    					fields : MaterialCateRecord.getKeys(),
	    				listeners : {
	    					load : function(thiz, records, opts){
	    						if(records.length > 0){
									var PersonRecord = Ext.data.Record.create([
								         {name : 'id'},
								         {name : 'typeValue'},
								         {name : 'name'},
								         {name : 'rid'}				
									]);
									var newRecord= new PersonRecord({typeValue: -1,id: -1,name: "全部",rid: -1});   
									thiz.insert(0,newRecord); 	    							
	    							
	    							stockTakeWin.cateId = -1;
	    							Ext.getCmp('comboMaterialCateId').setValue(-1);
	    							
	    							if(!stockTakeWin.otype == Ext.ux.otype['update']){
										//触发小类别变动
										checkTakeContentChange('cateId', Ext.getCmp('comboMaterialCateId'));   	    							
	    							}
 							
	    						}
	    						
	    					}
	    				}
    				}),
    				valueField : 'id',
    				displayField : 'name',
    				typeAhead : true,
    				mode : 'local',
    				triggerAction : 'all',
    				selectOnFocus : true,
    				listeners : {
    					select : function(thiz){
    						stockTakeWin.cateId = thiz.getValue();
    						checkTakeContentChange('cateId', thiz);
    					}
    				}
    			}]
    		}, {
    			columnWidth : 1,
    			items : [{
    				xtype : 'textfield',
    				fieldLabel : '备注',
    				width : 774,
    				id : 'txtStockTakeComment'
    			}]
    		}, {
    			items : [{
    				xtype : 'textfield',
    				id : 'txtStockTakeApprover',
    				fieldLabel : '审核人',
    				disabled : true
    			}]
    		}, {
    			items : [{
    				xtype : 'textfield',
    				id : 'txtStockTakeApproverDate',
    				fieldLabel : '审核时间',
    				disabled : true
    			}]
    		}, {
    			items : [{
    				xtype : 'textfield',
    				id : 'txtStockTakeOperator',
    				fieldLabel : '盘点人',
    				disabled : true
    			}]
    		}, {
    			items : [{
    				xtype : 'textfield',
    				id : 'txtStockTakeOperatorDate',
    				fieldLabel : '盘点时间',
    				disabled : true
    			}]
    		}]
    	}]
	};
	
	var stockTakeWinCenter = createGridPanel(
		'stockTakeWinCenter',
		'货品列表',
		'',
		'',
		'../../QueryMaterial.do',
		[
			[true, false, false, false], 
			['品名', 'material.name', 130],
			['盘点数', 'actualAmount',,'right', 'actualAmountRenderer'],
			['账面数', 'expectAmount',80,'right', 'Ext.ux.txtFormat.gridDou'],
			['盘亏数', 'deltaAmount',80,'right', 'shortageAmountRenderer'],
			['盘盈数', 'deltaAmount',80,'right', 'overageAmountRenderer']
		],
		StockTakeDetailRecord.getKeys(),
		[['isPaging', true],  ['dataSource', 'stockTakeDetail'], ['restaurantId', restaurantID], ['stockStatus', 3]],
		GRID_PADDING_LIMIT_20,
		''
	);
	stockTakeWinCenter.region = 'center';
	
	if(!stockTakeWin){
		stockTakeWin = new Ext.Window({
			takeContent : {
				dept : '',
				cateType : '',
				cateId : ''
			},
			title : '查看盘点任务',
			width : 900,
			height : 500,
			modal : true,
			resize : false,
			closable : false,
			layout : 'border',
			items : [stockTakeWinNorth, stockTakeWinCenter],
			bbar : ['->',{
				text : '导出盘点',
				id : 'btnExportStockTake',
				iconCls : 'icon_tb_exoprt_excel',
				handler : function(){
					var dept = Ext.getCmp('comboStockTakeDept');
					var cate = Ext.getCmp('comboMaterialCate');
					var cateId = Ext.getCmp('comboMaterialCateId');
					
					if(!dept.isValid()){
							return;
					}
					
					var url = '../../{0}?dataSource={1}&cateId={2}&deptId={3}&cateType={4}';
					url = String.format(
							url, 
							'ExportHistoryStatisticsToExecl.do', 
							'stockTakeDetail',
							cateId.getValue() > 0 && cateId.getRawValue() ? cateId.getValue() : '-1',
							dept.getValue(),
							cate.getValue()
					);
					window.location = url;
					
				}
			}, {
				text : '保存',
				id : 'btnSaveStockTake',
				iconCls : 'btn_save',
				handler : function(){
					var id = Ext.getCmp('hideStockTakeId');
					var dept = Ext.getCmp('comboStockTakeDept');
					var cate = Ext.getCmp('comboMaterialCate');
					var cateId = Ext.getCmp('comboMaterialCateId');
					var comment = Ext.getCmp('txtStockTakeComment');
					
					if(!dept.isValid() || !cate.isValid() || !cateId.isValid()){
						return;
					}
					
					var detail = '';
					for(var i = 0; i < stockTakeWinCenter.getStore().getCount(); i++){
						var temp = stockTakeWinCenter.getStore().getAt(i);
						if(i > 0){
							detail += '<sp>';
						}
						detail += (temp.get('material')['id'] + '<spst>' + temp.get('actualAmount'));
					}
					if(detail == ''){
						Ext.example.msg('提示', '操作失败, 请填写盘点货品信息.');
						return;
					}
					
					var btnSave = Ext.getCmp('btnSaveStockTake');
					var btnCancel = Ext.getCmp('btnCancelStockTake');
					
					btnSave.setDisabled(true);
					btnCancel.setDisabled(true);
					Ext.Ajax.request({
						url : '../../OperateStockTake.do',
						params : {
							dataSource : stockTakeWin.otype.toLowerCase(),
							
							id : id.getValue(),
							dept : dept.getValue(),
							cateType : cate.getValue(),
							cateId : cateId.getValue() > 0 && cateId.getRawValue() ? cateId.getValue() : '',
							comment : comment.getValue(),
							detail : detail
						},
						success : function(res, opt){
							btnSave.setDisabled(false);
							btnCancel.setDisabled(false);
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								stockTakeWin.hide();
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnSearchForStockTake').handler();
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
			},{
				text : '审核',
				id : 'btnAuditStockTake',
				iconCls : 'btn_save',
				handler : function(){
					var data = Ext.ux.getSelData(stockTakeGrid);
					auditStockTakeHandlerCenter({
						msg : '是否审核盘点任务',
						data : data
					});
				}
			}, {
				text : '取消',
				id : 'btnCancelStockTake',
				iconCls : 'btn_cancel',
				handler : function(){
					stockTakeWin.hide();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					stockTakeWin.hide();
				}
			}],
			listeners : {
				hide : function(){
					stockTakeWin.takeContent = {
						dept : '',
						cateType : '',
						cateId : ''
					};
	/*				var material = Ext.getCmp('comboSelectMaterialForStockTake');
					material.setValue();
					material.store.removeAll();
					material.store.loadData({root:[]});
	    			Ext.getCmp('numActualAmountForStockAction').setValue();*/
				},
				show : function(thiz){
					thiz.center();
				}
			}
		});
	}
}
/**
 * 
 */
function initDetailActualAmountMenu(){
	menuOperateActualAmount = new Ext.menu.Menu({
			id : 'menuOperateActualAmount',
			hideOnClick : false,
			items : [new Ext.Panel({
				frame : false,
				width : 150,
				items : [{
					xtype : 'form',
					layout : 'form',
					frame : true,
					labelWidth : 30,
					items : [{
						xtype : 'numberfield',
						id : 'numOperateActualAmount',
						fieldLabel : '数量',
						width : 80,
						validator : function(v){
							if(v >= 1 && v <= 65535){
								return true;
							}else{
								return '菜品数量在 1 ~ 65535 之间.';
							}
						} 
					}]
				}],
				bbar : ['->', {
					text : '确定',
					id : 'btnSaveOperateActualAmount',
					iconCls : 'btn_save',
					handler : function(e){
						var amount = Ext.getCmp('numOperateActualAmount');
						if(!amount.isValid()){
							return;
						}
						Ext.getCmp('stockTakeWinCenter').getSelectionModel().getSelected().set('actualAmount', amount.getValue());
						menuOperateActualAmount.hide();
					}
				}, {
					text : '关闭',
					iconCls : 'btn_close',
					handler : function(e){
						menuOperateActualAmount.hide();
					}
				}]
			})],
			listeners : {
				show : function(){
					var amount = Ext.getCmp('numOperateActualAmount');
					amount.setValue(Ext.getCmp('stockTakeWinCenter').getSelectionModel().getSelected().get('actualAmount'));
					amount.clearInvalid();
					amount.focus.defer(100, amount);
				}
			},
			keys : [{
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp('btnSaveOperateActualAmount').handler();
				}
			}, {
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					menuOperateActualAmount.hide();
				}
			}]
		});
	menuOperateActualAmount.render(document.body);
}
/**
 * 检查用户操作后数据变化
 * @param type
 * @param e
 * @returns {Boolean}
 */
function checkTakeContentChange(type, e){
	var content = stockTakeWin.takeContent;
	if(content == null || content == {}){
		return true;
	}
	var detailStore = Ext.getCmp('stockTakeWinCenter').getStore();
	if(type == 'dept'){
		if(content.dept == ''){
			content.dept = e.getValue();
			loadOperateMaterial({selectType : 0});
		}else{
			if(content.dept != e.getValue()){
				if(detailStore.getCount() > 0){
					Ext.Msg.show({
						title : '重要',
						msg : '盘点部门已更变, 确定将清空货品重新填写.',
						buttons : Ext.Msg.YESNO,
						fn : function(btn){
							if(btn == 'yes'){
								detailStore.removeAll();
								content.dept = e.getValue();
								loadOperateMaterial({selectType : 0});
							}else{
								e.setValue(content.dept);
							}
						}
					});
				}else{
					content.dept = e.getValue();
					loadOperateMaterial({selectType : 0});
				}
			}
		}
	}else if(type == 'cateType'){
		var cateId = Ext.getCmp('comboMaterialCateId');
		//var cateType = Ext.getCmp('comboMaterialCate');
		if(content.cateType == ''){
			content.cateType = e.getValue();
			
//			cateId.setValue();
			cateId.store.baseParams['type'] = e.getValue();
			cateId.store.load();
		}else{
			if(content.cateType != e.getValue()){
				if(detailStore.getCount() > 0){
					Ext.Msg.show({
						title : '重要',
						msg : '货品类型已更变, 确定将清空货品重新填写.',
						buttons : Ext.Msg.YESNO,
						fn : function(btn){
							if(btn == 'yes'){
								detailStore.removeAll();
								loadOperateMaterial({selectType : 0});
								
								content.cateType = e.getValue();
								content.cateId = '';
//								cateId.setValue();
								cateId.store.baseParams['type'] = e.getValue();
								cateId.store.load();
								
								
							}else{
								e.setValue(content.cateType);
							}
						}
					});
				}else{
					content.cateType = e.getValue();
					content.cateId = '';
					cateId.setValue();
					cateId.store.baseParams['type'] = e.getValue();
					cateId.store.load();
				}
			}
		}
	}else if(type == 'cateId'){
		if(content.cateId == ''){
			content.cateId = e.getValue();
			loadOperateMaterial({selectType : 0});
		}else{
			if(content.cateId != e.getValue()){
				if(detailStore.getCount() > 0){
					Ext.Msg.show({
						title : '重要',
						msg : '货品类别已更改, 确定将清空货品重新填写.',
						buttons : Ext.Msg.YESNO,
						fn : function(btn){
							if(btn == 'yes'){
								detailStore.removeAll();
								content.cateId = e.getValue();
								loadOperateMaterial({selectType : 0});
							}else{
								e.setValue(content.cateId);
							}
						}
					});
				}else{
					content.cateId = e.getValue();
					loadOperateMaterial({selectType : 0});
				}
			}
		}
	}
}
/**
 * 加载库存基础信息
 */
function loadOperateMaterial(c){
	var mstore = Ext.getCmp('stockTakeWinCenter').store;
	if(c.selectType == 1){
/*		var sn = Ext.getCmp('stockTakeGrid').getSelectionModel().getSelected();
		Ext.MessageBox.alert(sn.date.id);
		mstore.baseParams['stockTakeId'] = sn.date.id;
		mstore.load();*/
	}else{
		var content = stockTakeWin.takeContent;
		if((content.dept >= 0 || content.dept != '') && Ext.getCmp('comboMaterialCate').getValue() != ''){
			mstore.baseParams['deptId'] = content.dept;
			mstore.baseParams['cateId'] = content.cateId?content.cateId:-1;
			mstore.baseParams['cateType'] = Ext.getCmp('comboMaterialCate').getValue();
			mstore.load();
			
			//解决货物小类第一次打开不加载值的问题
			mstore.on('load', function(){
				Ext.getCmp('comboMaterialCateId').setValue(stockTakeWin.cateId);
			});
			
			if(!stockTakeWin.takeContent.cateType){
				stockTakeWin.takeContent.cateType = Ext.getCmp('comboMaterialCate').getValue();
			}
		}else{
			mstore.removeAll();
		}

	}
	//Ext.getCmp('comboSelectMaterialForStockTake').setValue();
}

//----------

var btnAddStockTake = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddStockTake.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '新建盘点任务',
	handler : function(btn){
		insertStockTakeHandler();
	}
});


Ext.onReady(function(){
	//
	initGrid();
	
	new Ext.Panel({
		title : '盘点任务管理',
		renderTo : 'divStockTake',
		width : parseInt(Ext.getDom('divStockTake').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divStockTake').parentElement.style.height.replace(/px/g,'')),
		frame : true,
		layout : 'border',
		items : [stockTakeGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [btnAddStockTake]
		})
	});
	
	//
	initWin();
	Ext.getCmp('comboStockTakeDept').store.load();
	Ext.getCmp('comboMaterialCateId').store.load();
	//
//	initDetailActualAmountMenu();
});
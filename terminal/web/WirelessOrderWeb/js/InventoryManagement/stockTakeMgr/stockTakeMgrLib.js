function audit(){
	var data = Ext.ux.getSelData(stockTakeGrid);
	data['statusValue'] = 3;
	updateStockTakeHandler();
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
	var cateId = Ext.getCmp('comboMaterialCateId');
	var comment = Ext.getCmp('txtStockTakeComment');
	var approver = Ext.getCmp('txtStockTakeApprover');
	var approverDate = Ext.getCmp('txtStockTakeApproverDate');
	var operator = Ext.getCmp('txtStockTakeOperator');
	var operatorDate = Ext.getCmp('txtStockTakeOperatorDate');
	
	if(c.otype == Ext.ux.otype['set']){
		var data = typeof c.data == 'undefined' ? {} : c.data;
		var deptData = typeof data.dept == 'undefined' ? {} : data.dept;
		var materialCate = typeof data['materialCate'] == 'undefined' ? {} : data['materialCate'];
		id.setValue(data['id']);
		dept.setValue(deptData['id']);
		
		cateId.setValue(materialCate['id']);
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
		
		if(typeof data['cataTypeValue'] == 'undefined'){
			cate.setValue(1);
		}else{
			cate.setValue(data['cataTypeValue']);
		}
		var gs = Ext.getCmp('stockTakeWinCenter').getStore();
		gs.removeAll();
		if(typeof data.detail != 'undefined' && data.detail.length > 0){
//			alert(Ext.encode(data.detail[0]))
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
	}else if(c.otype == Ext.ux.otype['get']){
		
	}
}
/**
 * 新增盘点任务
 */
function insertStockTakeHandler(){
	Ext.Ajax.request({
		url : '../../OperateStockTake.do',
		params : {
			dataSource : 'checkCurrentMonth',
			
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				Ext.Ajax.request({
					url : '../../OperateStockTake.do',
					params : {
						dataSource : 'checkStockAction',
						
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
							cate.fireEvent('select', cate);
							
							Ext.getCmp('btnAuditStockTake').hide();
							Ext.getCmp('btnSaveStockTake').show();
							//Ext.getCmp('stockTakeWinWest').setDisabled(false);
							//loadOperateMaterial();
						}else{
							jr['icon'] = Ext.Msg.WARNING; 
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.decode(res.responseText));
					}
				});

			}else{
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
		//Ext.getCmp('stockTakeWinWest').setDisabled(true);
	}else{
		data['statusValue'] = 1;
		stockTakeWin.otype = Ext.ux.otype['select'];
		stockTakeWin.show();
		stockTakeWin.setTitle('查看盘点任务');
		stockTakeWin.center();
		Ext.getCmp('btnSaveStockTake').hide();
		Ext.getCmp('btnAuditStockTake').show();
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

function deleteServicePlanHandler(){
	var sn = Ext.ux.getSelNode(servicePlanTree);
	if(!sn || sn.attributes.planId == -1){
		Ext.example.msg('提示', '请选中一个方案再进行操作.');
		return;
	}
	Ext.Msg.confirm(
		'提示',
		'是否删除方案:&nbsp;<font color="red">' + sn.text + '</font>',
		function(e){
			if(e == 'yes'){
				Ext.Ajax.request({
					url : '../../OperateServicePlan.do',
					params : {
						dataSource : 'delete',
						planId : sn.attributes.planId
					},
					success : function(res, opt){
						var jr = Ext.util.JSON.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('btnRefreshServicePlan').handler();
						}else{
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
					}
				});
			}
		},
		this
	);
}

function servicePlanOperationHandler(c){
	if(c == null || typeof(c) == 'undefined' || typeof(c.type) == 'undefined'){
		return;
	}
	
	var id = Ext.getCmp('hideServicePlanId');
	var name = Ext.getCmp('txtServicePlanName');
	var isDefault = Ext.getCmp('chbServicePlanIsDefault');
	name.setDisabled(false);
	if(c.type == dmObj.operation.insert){
		isDefault.setValue(false);
		name.setValue();
		name.focus(true, 100);
		addServicePlanWin.setTitle('添加方案');
	}else if(c.type == dmObj.operation.update){
		
		var sn = Ext.ux.getSelNode(servicePlanTree);
		if(!sn || sn.attributes.planId == -1){
			Ext.example.msg('提示', '请选中一个方案再进行操作.');
			return;
		}
		id.setValue(sn.attributes.planId); 
		isDefault.setValue(eval(sn.attributes.isDefault));
		name.setValue(sn.attributes.planName); 
		name.focus(true, 100);
		if(sn.attributes.type == 2){
			name.setDisabled(true);
		}else{
			name.setDisabled(false);
		}
		
		addServicePlanWin.setTitle('修改方案');
	}
	
	addServicePlanWin.show();
	addServicePlanWin.center();
	addServicePlanWin.operationType = c.type;
};

function serviceRateOperationHandler(c){
	if(c == null || typeof(c) == 'undefined' || typeof(c.type) == 'undefined'){
		return;
	}
	
	var region = Ext.getCmp('hideServiceRateRegion');
	var rate = Ext.getCmp('numSetServiceRate');
	
	var tempRoot = {root:[]};
	for(var i = 0; i < service_programData.root.length; i++){
		if(eval(service_programData.root[i].status != 2)){
			tempRoot.root.push(service_programData.root[i]);
		}
	}
	
	if(c.type == dmObj.operation.insert){
		serviceRate_addProgramWin.setTitle('添加服务费率');
	}else if(c.type == dmObj.operation.update){
		var sd = Ext.ux.getSelData(serviceRateGrid);
		if(!sd){
			Ext.example.msg('提示', '请选中一个服务费率再进行操作.');
			return;
		}
		rate.setValue(sd['rate']);
		region.setValue(sd['region']['id']);
		serviceRate_addProgramWin.setTitle('修改服务费率--- ' + sd['region']['name']);
	}
	
	rate.clearInvalid();
	
	serviceRate_addProgramWin.show();
	serviceRate_addProgramWin.center();
	serviceRate_addProgramWin.operationType = c.type;
	rate.focus(true, 100);
};

function updateServiceRateOperationHandler(){
	serviceRateOperationHandler({
		type : dmObj.operation.update
	});
};


function serviceRateOperationRenderer(){
	return '<a href="javascript:updateServiceRateOperationHandler()">修改</a>';
};

var servicePlanTree;
var serviceRateGrid;
var serviceRate_addProgramWin;
var addServicePlanWin;
var updateServiceRateWin;
var servicePlan_obj = {treeId : 'servicePlan_tree', option : [{name:'修改', fn:"servicePlanOperationHandler({type:dmObj.operation.update})"},{name:'删除', fn:"deleteServicePlanHandler()"}]};
Ext.onReady(function(){
	
	var servicePlanTreeTbar = new Ext.Toolbar({
		items : ['->',{
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				servicePlanOperationHandler({
					type : dmObj.operation.insert
				});
			}
		},{
			text : '刷新',
			id : 'btnRefreshServicePlan',
			iconCls : 'btn_refresh',
			handler : function(){
				servicePlanTree.getRootNode().reload();
				Ext.getDom('serviceRateNameShowType').innerHTML = '------------';
			}
		}]
	});
	
	servicePlanTree = new Ext.tree.TreePanel({
		id : 'servicePlan_tree',
		title : '方案',
		region : 'west',
		width : 200,
		border : true,
		rootVisible : true,
		autoScroll : true,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部方案',
			planId : -1,
			loader : new Ext.tree.TreeLoader({
				dataUrl : '../../QueryServicePlan.do',
				baseParams : {dataSource : 'planTree'}
			}),
			listeners : {
				expand : function(thiz){
					var rn = servicePlanTree.getRootNode().childNodes;
					var node = null;
					if(rn.length == 0){
						servicePlanTree.getRootNode().getUI().hide();
					}else{
						for(var i = (rn.length - 1); i >= 0; i--){
							if(rn[i].attributes.status == 2){
								node = rn[i];
							}
						}
					}
	        		if(node != null){
	        			node.select();
	        			node.fireEvent('click', node);
	        			node.fireEvent('dblclick', node);
	        			
					}	
				}
			}
		}),		
		tbar : servicePlanTreeTbar,
		listeners : {
			load : function(thiz){

					var rn = servicePlanTree.getRootNode().childNodes;
					if(rn.length == 0){
						servicePlanTree.getRootNode().getUI().hide();
					}else{
						for(var i = (rn.length - 1); i >= 0; i--){
							if(rn[i].attributes.type == 2){
								rn[i].setText('<font color=\"#808080\">' + rn[i].attributes.planName + '&nbsp;(系统保留)</font>');
							}
							if(rn[i].attributes.status == 2){
								rn[i].setText('<font color=\"red\">' + rn[i].attributes.planName + '&nbsp;(默认方案)</font>');
							}
							service_programData.root[i] = {
								planId : rn[i].attributes.planId,
								planName : rn[i].attributes.planName,
								status : rn[i].attributes.status
							};
						}
						servicePlanTree.getRootNode().getUI().show();
					}				
				
			},
			click : function(e){
				if(e.attributes.planId != -1){
					Ext.getDom('serviceRateNameShowType').innerHTML = e.attributes.planName;
					Ext.getCmp('btnSearchServiceRate').handler();
				}
			}
		}
	});	
	
	var serviceRateGridTbar = new Ext.Toolbar({
		items : [{
			xtype : 'tbtext',
			text : String.format(Ext.ux.txtFormat.typeName, '方案', 'serviceRateNameShowType', '------------')
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		}, '->', {
			text : '搜索',
			id : 'btnSearchServiceRate',
			iconCls : 'btn_search',
			hidden : true,
			handler : function(){
				var sn = servicePlanTree.getSelectionModel().getSelectedNode();
				
				var dgs = serviceRateGrid.getStore();
				dgs.baseParams['planId'] = (sn == null || !sn || sn.attributes.planId == -1 ? '' : sn.attributes.planId);
				dgs.load({
					params : {
						start : 0,
						limit : 50
					}
				});
			}
		},{
			text : '一键修改服务费率',
			iconCls : 'btn_edit_all',
			handler : function(){
				updateServiceRateWin.show();
				Ext.getCmp('numUpdateAllRateByServicePlan').focus(true, 100);
			}
		}]
	});
	
	serviceRateGrid = createGridPanel(
		'serviceRateGrid',
		'服务费率',
		'',
		'',
		'../../QueryServicePlan.do',
		[
			[true, false, false, true], 
			['区域', 'region.name'], 
			['服务费率', 'rate', 50, 'right'],
			['操作', 'operation', , 'center', 'serviceRateOperationRenderer']
		],
		['rate', 'rateId', 'planId', 'region', 'region.name'],
		[['dataSource' , 'getRates']],
		GRID_PADDING_LIMIT_20,
		'',
		serviceRateGridTbar
	);	
	serviceRateGrid.region = 'center';
	serviceRateGrid.on('rowdblclick', function(){
		updateServiceRateOperationHandler();
	});
	
	new Ext.Panel({
		renderTo : 'divService',
		width : parseInt(Ext.getDom('divService').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divService').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',
		frame : true,
		items : [servicePlanTree, serviceRateGrid]
	});
	
	serviceRate_addProgramWin = Ext.getCmp('serviceRate_addProgramWin');
	if(!serviceRate_addProgramWin){
		serviceRate_addProgramWin = new Ext.Window({
			id : 'serviceRate_addProgramWin',
			title : '添加服务费率',
			closable : false,
			resizable : false,
			modal : true,
			width : 200,
			items : [{
				xtype : 'panel',
			    layout : 'column',
			    frame : true,
			    defaults : {
			    	xtype : 'panel',
			    	layout : 'form'
			    	
			    },
			    items : [{
			    	columnWidth : 1,
			    	labelWidth : 65,
			    	items : [{
						id : 'hideServiceRateRegion',
						xtype : 'hidden'				
			    	}]
			    },{
			    	columnWidth : 0.7,
			    	labelWidth : 65,
			    	items : [{
						xtype : 'numberfield',
						id : 'numSetServiceRate',
						width : 50,
						style : 'text-align:right;',
						fieldLabel : '服务费率'
			    	}]
			    },{
			    	columnWidth : 0.2,
			    	style : 'color:#15428B;vertical-align: middle;margin-top:4px;',
			    	html : '%'
			    }]
			}],
			bbar : [{
				xtype : 'tbtext',
				text : ' '
			}, '->', {
				text : '保存',
				id : 'btnSaveServiceRate',
				iconCls : 'btn_save',
				handler : function(e){					
					var sn = servicePlanTree.getSelectionModel().getSelectedNode();
					var planId = sn.attributes.planId;
					
					var region = Ext.getCmp('hideServiceRateRegion');
					var rate = Ext.getCmp('numSetServiceRate').getValue();
					
					//判断是否有效
					if(!region.isValid()){
						return;
					}
					
					var btnSave = Ext.getCmp('btnSaveServiceRate');
					var btnCancel = Ext.getCmp('btnCancelServiceRate');
					btnSave.setDisabled(true);
					btnCancel.setDisabled(true);
					
					Ext.Ajax.request({
						url : '../../OperateServicePlan.do',
						params : {
							planId : planId,
							regionId : region.getValue(),
							rate : (rate == ''? 0 : rate/100),
							dataSource : 'operateRate'
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								serviceRate_addProgramWin.hide();
								serviceRateGrid.getStore().reload();
								Ext.example.msg(jr.title, jr.msg);
							}else{
								Ext.ux.showMsg(jr);
							}
							btnSave.setDisabled(false);
							btnCancel.setDisabled(false);
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
							btnSave.setDisabled(false);
							btnCancel.setDisabled(false);
						}
					});
					
					
				}
			}, {
				text : '关闭',
				id : 'btnCancelServiceRate',
				iconCls : 'btn_close',
				handler : function(e){
					serviceRate_addProgramWin.hide();
				}
			}],
			listeners : {
				show : function(){
					Ext.getCmp('numSetServiceRate').clearInvalid();
				}
			},
			keys : [{
				 key : Ext.EventObject.ENTER,
				 fn : function(){ 
					 Ext.getCmp('btnSaveServiceRate').handler();
				 },
				 scope : this 
			 }]
		});
	}
	
	if(!addServicePlanWin){
		addServicePlanWin = new Ext.Window({
			title : '添加服务方案',
			closable : false,
			resizable : false,
			modal : true,
			width : 235,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				labelWidth : 65,
				width : 234,
				defaults : {
					width : 120
				},
				items : [ {
 	    	    	xtype : 'textfield',
 	    	    	id : 'txtServicePlanName',
 	    	    	fieldLabel : '方案名称',
 	    	    	allowBlank : false,
 	    	    	selectOnFocus : true
 	    	    },{
					xtype : 'checkbox',
					id : 'chbServicePlanIsDefault',
					hideLabel : true,
					checked : true,
					boxLabel : '默认方案',
					listeners : {
						check : function(thiz){
							
						}
					}
				},{
					xtype : 'hidden',
					id : 'hideServicePlanId'
				}]
			}],
			bbar : [{
				xtype : 'tbtext',
				text : ' '
			}, '->', {
				text : '保存',
				id : 'btnSaveServicePlan',
				iconCls : 'btn_save',
				handler : function(e){					
					var name = Ext.getCmp('txtServicePlanName');
					var servicePlanId = Ext.getCmp('hideServicePlanId');
					var isDeafault = Ext.getCmp('chbServicePlanIsDefault');
					
					var dataSource = '';
					
					if(!name.isValid()){
						return;
					}
					
					if(addServicePlanWin.operationType == dmObj.operation.insert){
						dataSource = 'insert';
					}else if(addServicePlanWin.operationType == dmObj.operation.update){
						dataSource = 'update';
					}else{
						return;
					}
					
					var save = Ext.getCmp('btnSaveServicePlan');
					var cancel = Ext.getCmp('btnCancelServicePlan');
					
					save.setDisabled(true);
					cancel.setDisabled(true);
					
					Ext.Ajax.request({
						url : '../../OperateServicePlan.do',
						params : {
							name : name.getValue(), 
							servicePlanId : servicePlanId.getValue(),
							isDeafault : isDeafault.getValue(),
							dataSource : dataSource
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								addServicePlanWin.hide();
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnRefreshServicePlan').handler();
							}else{
								Ext.ux.showMsg(jr);
							}
							save.setDisabled(false);
							cancel.setDisabled(false);
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
							save.setDisabled(false);
							cancel.setDisabled(false);
						}
					});
				}
			}, {
				text : '关闭',
				id : 'btnCancelServicePlan',
				iconCls : 'btn_close',
				handler : function(e){
					addServicePlanWin.hide();
				}
			}],
			keys : [{
				 key : Ext.EventObject.ENTER,
				 fn : function(){ 
					 Ext.getCmp('btnSaveServicePlan').handler();
				 },
				 scope : this 
			 }]
		});
	}
	updateServiceRateWin = new Ext.Window({
		title : '一键修改服务费率',
		closable : false,
		resizable : false,
		modal : true,
		width : 250,
		items : [{
				xtype : 'panel',
			    layout : 'column',
			    frame : true,
			    defaults : {
			    	xtype : 'panel',
			    	layout : 'form'
			    	
			    },
			    items : [{
			    	columnWidth : 1,
			    	labelWidth : 65,
			    	items : [{
			    	    xtype : 'combo',
			    	    id : 'comboUpdateAllRateByService',
			    	    fieldLabel : '方案名称',
			    	    width : 130,
			    	    store : new Ext.data.JsonStore({
			    	    	root : 'root',
							fields : [ 'planId', 'planName' ]
						}),
						valueField : 'planId',
						displayField : 'planName',
						mode : 'local',
						triggerAction : 'all',
						typeAhead : true,
						selectOnFocus : true,
						forceSelection : true,
						allowBlank : false,
						readOnly : false
			    	}]
			    },{
			    	columnWidth : 0.55,
			    	labelWidth : 65,
			    	items : [{
						xtype : 'numberfield',
						id : 'numUpdateAllRateByServicePlan',
						width : 50,
						style : 'text-align:right;',
						fieldLabel : '服务费率'
			    	}]
			    },{
			    	columnWidth : 0.2,
			    	style : 'color:#15428B;vertical-align: middle;margin-top:4px;',
			    	html : '%'
			    }]
			}],
		bbar : [ '->', {
			text : '保存',
			id : 'btnSaveUpdateServicePlanRate',
			iconCls : 'btn_save',
			handler : function(e){
				var servicePlan = Ext.getCmp('comboUpdateAllRateByService');
				var rate = Ext.getCmp('numUpdateAllRateByServicePlan');
				
				if(!servicePlan.isValid()){
					return;
				}
				
				Ext.Msg.confirm(
					'提示', 
					'是否将服务方案"<font color="red">'+servicePlan.getRawValue()+'</font>"下所有区域服务率修改为"<font color="red"> '+rate.getValue()+'% </font>"?',
					function(e){
						if(e == 'yes'){
							var save = Ext.getCmp('btnSaveUpdateServicePlanRate');
							var cancel = Ext.getCmp('btnCancelUpdateServicePlanRate');
							
							save.setDisabled(true);
							cancel.setDisabled(true);
							
							Ext.Ajax.request({
								url : '../../OperateServicePlan.do',
								params : {
									dataSource : 'updateAllRate',
									planId : servicePlan.getValue(),
									rate : (rate.getValue() == ''? 0 : rate.getValue()/100)
								},
								success : function(res, opt){
									var jr = Ext.util.JSON.decode(res.responseText);
									if(jr.success){
										updateServiceRateWin.hide();
										Ext.example.msg(jr.title, jr.msg);
										var treeRoot = servicePlanTree.getRootNode().childNodes;
										for(var i = 0; i < treeRoot.length; i++){
											if(treeRoot[i].attributes.planId  == servicePlan.getValue()){
												treeRoot[i].select();
												treeRoot[i].fireEvent('click', treeRoot[i]);
												break;
											}
										}
									}else{
										Ext.ux.showMsg(jr);
									}
									save.setDisabled(false);
									cancel.setDisabled(false);
								},
								failure : function(res, opt){
									Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
									save.setDisabled(false);
									cancel.setDisabled(false);
								}
							});
						}
					}
				);
			}
		}, {
			text : '关闭',
			id : 'btnCancelUpdateServicePlanRate',
			iconCls : 'btn_close',
			handler : function(e){
				updateServiceRateWin.hide();
			}
		}],
		listeners : {
			show : function(){
				var servicePlan = Ext.getCmp('comboUpdateAllRateByService');
//				rate.setValue(1.00);
//				var tempRoot = {root:[]};
//				for(var i = 0; i < service_programData.root.length; i++){
//					tempRoot.root.push(service_programData.root[i]);
//				}
				servicePlan.store.loadData(service_programData);
				
				var node = Ext.ux.getSelNode(servicePlanTree);
				if(node){
					servicePlan.setValue(node.attributes.planId);
				}else{
					servicePlan.setValue();
				}
				servicePlan.clearInvalid();
			}
		},
		keys : [{
			 key : Ext.EventObject.ENTER,
			 fn : function(){ 
				 Ext.getCmp('btnSaveUpdateServicePlanRate').handler();
			 },
			 scope : this 
		 }]
	});	
	showFloatOption(servicePlan_obj);	
});

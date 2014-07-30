
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
		if(!sn || sn.attributes.discountID == -1){
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
	
	addServicePlanWin.operationType = c.type;
	addServicePlanWin.center();
	addServicePlanWin.show();
};

function serviceRateOperationHandler(c){
	if(c == null || typeof(c) == 'undefined' || typeof(c.type) == 'undefined'){
		return;
	}
	
	var region = Ext.getCmp('cboServiceRateRegion');
	var rate = Ext.getCmp('numServiceRate');
	
	var tempRoot = {root:[]};
	for(var i = 0; i < programData.root.length; i++){
		if(eval(programData.root[i].status != 2)){
			tempRoot.root.push(programData.root[i]);
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
		serviceRate_addProgramWin.setTitle('修改服务费率');
	}
	
	rate.clearInvalid();
	
	serviceRate_addProgramWin.operationType = c.type;
	serviceRate_addProgramWin.center();
	serviceRate_addProgramWin.show();
};

function updateServiceRateOperationHandler(){
	serviceRateOperationHandler({
		type : dmObj.operation.update
	});
};


function discountIsDefaultRenderer(val, md, record){
	return eval(record.get('discount.status') == 1) ? '是' : '否';
};

function serviceRateOperationRenderer(){
	return '<a href="javascript:updateServiceRateOperationHandler()">修改</a>';
};

var servicePlanTree;
var serviceRateGrid;
var serviceRate_addProgramWin;
var addServicePlanWin;
var updateDiscountRateWin;
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
							programData.root[i] = {
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
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				serviceRateOperationHandler({
					type : dmObj.operation.insert
				});
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
						id : 'cboServiceRateRegion',
						xtype : 'combo',
						fieldLabel : '所属区域',
						forceSelection : true,
						width : 130,
						store : new Ext.data.SimpleStore({
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
							render : function(thiz){
								var data = [];
								Ext.Ajax.request({
									url : '../../QueryRegion.do',
									params : {
										dataSource : 'normal'
									},									
									success : function(res, opt){
										var jr = Ext.decode(res.responseText);
										for(var i = 0; i < jr.root.length; i++){
											data.push([jr.root[i]['id'], jr.root[i]['name']]);
										}
										thiz.store.loadData(data);
										thiz.setValue(jr.root[0]['id']);
									},
									fialure : function(res, opt){
										thiz.store.loadData(data);
									}
								});
							}
						}					
			    	}]
			    },{
			    	columnWidth : 0.55,
			    	labelWidth : 65,
			    	items : [{
						xtype : 'numberfield',
						id : 'numServiceRate',
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
					
					var region = Ext.getCmp('cboServiceRateRegion');
					var rate = Ext.getCmp('numServiceRate').getValue();
					
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
					Ext.getCmp('numServiceRate').clearInvalid();
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
		serviceRate_addProgramWin.render(document.body);
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
		addServicePlanWin.render(document.body);
	}
	
	showFloatOption(servicePlan_obj);	
});

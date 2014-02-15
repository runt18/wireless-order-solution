
function deleteDiscountPlanHandler(){
	var sn = Ext.ux.getSelNode(programTree);
	if(!sn || sn.attributes.discountID == -1){
		Ext.example.msg('提示', '请选中一个方案再进行操作.');
		return;
	}
	Ext.Msg.confirm(
		'提示',
		'是否删除方案:&nbsp;<font color="red">' + sn.text + '</font>',
		function(e){
			if(e == 'yes'){
				Ext.Ajax.request({
					url : '../../DeleteDiscount.do',
					params : {
						restaurantID : restaurantID,
						discountID : sn.attributes.discountID
					},
					success : function(res, opt){
						var jr = Ext.util.JSON.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('btnRefreshProgramTree').handler();
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

function programOperationHandler(c){
	if(c == null || typeof(c) == 'undefined' || typeof(c.type) == 'undefined'){
		return;
	}
	
	var id = Ext.getCmp('numDiscountID');
	var name = Ext.getCmp('txtDiscountName');
	var rate = Ext.getCmp('numDiscountRate');
	var isDefault = Ext.getCmp('chbIsDefault');
	var status = Ext.getCmp('hideDiscountStatus');
	
	if(c.type == dmObj.operation.insert){
		Ext.getDom('numDiscountID').parentElement.parentElement.style.display = 'none';
		Ext.getDom('numDiscountRate').parentElement.parentElement.style.display = 'block';
		
		rate.setValue(1.00);
		isDefault.setValue(false);
		name.setValue();
		name.focus(true, 100);
		addProgramWin.setTitle('添加方案');
	}else if(c.type == dmObj.operation.update){
		Ext.getDom('numDiscountID').parentElement.parentElement.style.display = 'block';
		Ext.getDom('numDiscountRate').parentElement.parentElement.style.display = 'none';
		
		var sn = Ext.ux.getSelNode(programTree);
		if(!sn || sn.attributes.discountID == -1){
			Ext.example.msg('提示', '请选中一个方案再进行操作.');
			return;
		}
		id.setValue(sn.attributes.discountID); 
		isDefault.setValue(eval(sn.attributes.isDefault));
		status.setValue(sn.attributes.status);
		name.setValue(sn.attributes.discountName); 
		name.focus(null, 100);
		if(sn.attributes.status == 2 || sn.attributes.status == 3){
			name.setDisabled(true);
		}else{
			name.setDisabled(false);
		}
		
		addProgramWin.setTitle('修改方案');
	}
	
	addProgramWin.operationType = c.type;
	addProgramWin.center();
	addProgramWin.show();
};

function disocuntOperationHandler(c){
	if(c == null || typeof(c) == 'undefined' || typeof(c.type) == 'undefined'){
		return;
	}
	
	var rate = Ext.getCmp('numKitchenRate');
	var discountId = Ext.getCmp('hideDiscountID');
	var kitchenId = Ext.getCmp('hideKitchenID');
	
	var tempRoot = {root:[]};
	for(var i = 0; i < programData.root.length; i++){
		if(eval(programData.root[i].status != 2)){
			tempRoot.root.push(programData.root[i]);
		}
	}
	
	if(c.type == dmObj.operation.insert){
		rate.setValue(1.00);
		addDiscountWin.setTitle('添加分厨折扣');
	}else if(c.type == dmObj.operation.update){
		var sd = Ext.ux.getSelData(discountGrid);
		if(!sd){
			Ext.example.msg('提示', '请选中一个分厨折扣再进行操作.');
			return;
		}
		rate.setValue(sd['rate']);
		discountId.setValue(sd['discount']['id']);
		kitchenId.setValue(sd['kitchen']['id']);
		addDiscountWin.setTitle('修改分厨折扣 --- ' + sd['kitchen']['name']);
	}
	
	rate.clearInvalid();
	
	addDiscountWin.operationType = c.type;
	addDiscountWin.center();
	addDiscountWin.show();
};

function updateDisocuntOperationHandler(){
	disocuntOperationHandler({
		type : dmObj.operation.update
	});
};


function discountIsDefaultRenderer(val, md, record){
	return eval(record.get('discount.status') == 1) ? '是' : '否';
};

function discountOperationRenderer(){
	return '<a href="javascript:updateDisocuntOperationHandler()">修改</a>';
};

function discountGroupTextTpl(rs){
	return '部门:'+rs[0].get('dept.name');
}

var programTree;
var discountGrid;
var addProgramWin;
var addDiscountWin;
var updateDiscountRateWin;
var discount_obj = {treeId : 'discount_tree', option : [{name:'修改', fn:"programOperationHandler({type:dmObj.operation.update})"},{name:'删除', fn:"deleteDiscountPlanHandler()"}]};
Ext.onReady(function(){
	Ext.Ajax.request({
		url : '../../QueryKitchen.do',
		params : {
			dataSource : 'normal',
			restaurantID : restaurantID,
			isPaging : false
		},
		success : function(res, opt){
			discountData = Ext.decode(res.responseText);
		}
	});
	
	var programTreeTbar = new Ext.Toolbar({
		items : ['->',{
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				programOperationHandler({
					type : dmObj.operation.insert
				});
			}
		},{
			text : '刷新',
			id : 'btnRefreshProgramTree',
			iconCls : 'btn_refresh',
			handler : function(){
				programTree.getRootNode().reload();
				Ext.getDom('discountNameShowType').innerHTML = '------------';
			}
		}]
	});
	
	programTree = new Ext.tree.TreePanel({
		id : 'discount_tree',
		title : '方案',
		region : 'west',
		width : 200,
		border : true,
		rootVisible : true,
		autoScroll : true,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryDiscountTree.do',
			baseParams : {
				
				restaurantID : restaurantID
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部方案',
	        leaf : false,
	        border : true,
	        discountID : '-1',
	        listeners : {
	        	expand : function(thiz){
	        		var rn = programTree.getRootNode().childNodes;
	        		if(addProgramWin.operationType == dmObj.operation.update){
	        			for(var i = (rn.length - 1); i >= 0; i--){
		        			if(eval(rn[i].attributes.discountID == addProgramWin.updateProgram.discountID)){
		        				rn[i].select();
		        				rn[i].fireEvent('click', rn[i]);
		        				rn[i].fireEvent('dblclick', rn[i]);
		        				break;
							}
						}
	        		}else{
		        		var node = null, maxDiscountID = 0;
		        		for(var i = (rn.length - 1); i >= 0; i--){
		        			if(eval(rn[i].attributes.discountID > maxDiscountID)){
		        				maxDiscountID = rn[i].attributes.discountID;
								node = rn[i];
							}
						}
		        		if(node != null){
		        			node.select();
		        			node.fireEvent('click', node);
							node.fireEvent('dblclick', node);
						}
	        		}
	        		addProgramWin.operationType = null;
	        	}
	        }
		}),
		tbar : programTreeTbar,
		listeners : {
			load : function(thiz){
				var rn = programTree.getRootNode().childNodes;
				if(rn.length == 0){
					programTree.getRootNode().getUI().hide();
				}else{
					for(var i = (rn.length - 1); i >= 0; i--){
						if(rn[i].attributes.type == 2){
							rn[i].setText('<font color=\"#808080\">' + rn[i].attributes.discountName + '&nbsp;(系统保留)</font>');
						}
						if(rn[i].attributes.status == 2){
							rn[i].setText('<font color=\"red\">' + rn[i].attributes.discountName + '&nbsp;(默认方案)</font>');
						}
						programData.root[i] = {
							discountID : rn[i].attributes.discountID,
							discountName : rn[i].attributes.discountName,
							status : rn[i].attributes.status
						};
					}
					programTree.getRootNode().getUI().show();
				}
			},
			click : function(e){
				if(e.attributes.discountID != -1){
					Ext.getDom('discountNameShowType').innerHTML = e.attributes.discountName;
					Ext.getCmp('btnSearchDiscountPlan').handler();
				}
			}
		}
	});	
	
	var discountGridTbar = new Ext.Toolbar({
		items : [{
			xtype : 'tbtext',
			text : String.format(Ext.ux.txtFormat.typeName, '方案', 'discountNameShowType', '------------')
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		}, '->', {
			text : '搜索',
			id : 'btnSearchDiscountPlan',
			iconCls : 'btn_search',
			hidden : true,
			handler : function(){
				var sn = programTree.getSelectionModel().getSelectedNode();
				
				var dgs = discountGrid.getStore();
				dgs.baseParams['discountID'] = (sn == null || !sn || sn.attributes.discountID == -1 ? '' : sn.attributes.discountID);
				dgs.load({
					params : {
						start : 0,
						limit : 50
					}
				});
			}
		}
/*		,{
			text : '展开/收缩',
			iconCls : 'icon_tb_toggleAllGroups',
			handler : function(){
				discountGrid.getView().toggleAllGroups();
			}
		}*/
		,{
			text : '一键修改折扣率',
			iconCls : 'btn_edit_all',
			handler : function(){
				updateDiscountRateWin.show();
				Ext.getCmp('numUpdateAllRateByDiscountPlan').focus(true, 100);
			}
		}]
	});
	
	discountGrid = createGridPanel(
		'discountGrid',
		'分厨折扣',
		'',
		'',
		'../../QueryDiscountPlan.do',
		[
			[true, false, false, true], 
			['分厨名称', 'kitchen.name'], 
			['折扣率', 'rate', 50, 'right' , 'Ext.ux.txtFormat.gridDou'],
			['操作', 'operation', '', 'center', 'discountOperationRenderer'],
			 ['dept.id','dept.id', 10]
		],
		DiscountPlanRecord.getKeys().concat(['dept','dept.id', 'dept.name']),
		'',
		'',
		{
			name : 'dept.id',
			hide : true
		},
		discountGridTbar
	);	
	discountGrid.region = 'center';
	discountGrid.on('rowdblclick', function(){
		updateDisocuntOperationHandler();
	});
	discountGrid.view = new Ext.grid.GroupingView({   
        forceFit:true,   
        groupTextTpl : '{[discountGroupTextTpl(values.rs)]}'
    });
	
	new Ext.Panel({
		renderTo : 'divDiscount',
		width : parseInt(Ext.getDom('divDiscount').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divDiscount').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',
		frame : true,
		items : [programTree, discountGrid]
	});
	
	addProgramWin = Ext.getCmp('dm_addProgramWin');
	if(!addProgramWin){
		addProgramWin = new Ext.Window({
			id : 'dm_addProgramWin',
			title : '添加方案',
			closable : false,
			resizable : false,
			modal : true,
			width : 243,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				width : 242,
				labelWidth : 65,
				items : [{
					xtype : 'hidden',
					id : 'hideDiscountStatus'
				}, {
					xtype : 'numberfield',
					id : 'numDiscountID',
					width : 130,
					fieldLabel : '方案编号',
					allowBlank : false,
					disabled : true,
					emptyText : '不允许操作方案编号.'
				}, {
					xtype : 'textfield',
					id : 'txtDiscountName',
					width : 130,
					fieldLabel : '方案名称',
					//不允许为空
					allowBlank : false,
					blankText : '方案编号不允许为空.....'
					//验证器
/*					validator : function(v){
						if(Ext.util.Format.trim(v).length > 0){
							return true;
						}else{
							return '方案编号空.';
						}
					}*/
				}, {
					xtype : 'numberfield',
					id : 'numDiscountRate',
					width : 130,
					fieldLabel : '默认折扣',
					value : 1.00,
					validator : function(v){
						if(v >= 0.00 && v <= 1.00){
							return true;
						}else{
							return '折扣率在 0.00 至 1.00 之间,如 8.8 折输入 0.88 ';
						}
					}
				},{
					xtype : 'label',
					width : 200,
					style : 'color:green;font-szie:14px;',
					text : '说明: 如果是八折, 直接输入0.8即可'
				},{
					xtype : 'checkbox',
					id : 'chbIsDefault',
					hideLabel : true,
					checked : true,
					boxLabel : '默认方案',
					listeners : {
						check : function(thiz){
							
						}
					}
				}]
			}],
			bbar : [{
				xtype : 'tbtext',
				text : ' '
			}, '->', {
				text : '保存',
				id : 'btnSaveProgram',
				iconCls : 'btn_save',
				handler : function(e){					
					var id = Ext.getCmp('numDiscountID');
					var name = Ext.getCmp('txtDiscountName');
					var rate = Ext.getCmp('numDiscountRate');
					var isDefault = Ext.getCmp('chbIsDefault');
					
					var actionURL = '';
					//判断是否有效
					if(!name.isValid() || !rate.isValid()){
						return;
					}
					
					if(addProgramWin.operationType == dmObj.operation.insert){
						actionURL = '../../InsertDiscount.do';
					}else if(addProgramWin.operationType == dmObj.operation.update){
						actionURL = '../../UpdateDiscount.do';
					}else{
						return;
					}
					
					var btnSave = Ext.getCmp('btnSaveProgram');
					var btnCancel = Ext.getCmp('btnCancelProgram');
					btnSave.setDisabled(true);
					btnCancel.setDisabled(true);
					
					Ext.Ajax.request({
						url : actionURL,
						params : {
							restaurantID : restaurantID,
							discountID : id.getValue(),
							discountName : name.getValue(),
							rate : rate.getValue(),
							isDefault : eval(isDefault.getValue() == true) ? true : ''
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								addProgramWin.updateProgram = {discountID:id.getValue()};
								addProgramWin.hide();
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnRefreshProgramTree').handler();
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
				id : 'btnCancelProgram',
				iconCls : 'btn_close',
				handler : function(e){
					addProgramWin.hide();
				}
			}],
			listeners : {
				show : function(){
					Ext.getCmp('txtDiscountName').clearInvalid();
				}
			},
			keys : [{
				 key : Ext.EventObject.ENTER,
				 fn : function(){ 
					 Ext.getCmp('btnSaveProgram').handler();
				 },
				 scope : this 
			 }]
		});
		addProgramWin.render(document.body);
	}
	
	if(!addDiscountWin){
		addDiscountWin = new Ext.Window({
			title : '添加分厨折扣',
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
					width : 130
				},
				items : [ {
 	    	    	xtype : 'numberfield',
 	    	    	id : 'numKitchenRate',
 	    	    	fieldLabel : '折扣率',
 	    	    	allowBlank : true,
 	    	    	selectOnFocus : true,
 	    	    	value : 1.00,
					validator : function(v){
						if(v >= 0.00 && v <= 1.00){
							return true;
						}else{
							return '折扣率在 0.00 至 1.00 之间,如 8.8 折输入 0.88 ';
						}
					}
 	    	    },{
					xtype : 'label',
					width : 200,
					style : 'color:green;font-szie:14px;',
					text : '说明: 如果是八折, 直接输入0.8即可'
				},{
					xtype : 'hidden',
					id : 'hideDiscountID'
				}, {
					xtype : 'hidden',
					id : 'hideKitchenID'
				}]
			}],
			bbar : [{
				xtype : 'tbtext',
				text : ' '
			}, '->', {
				text : '保存',
				id : 'btnSaveDiscount',
				iconCls : 'btn_save',
				handler : function(e){					
					var rate = Ext.getCmp('numKitchenRate');
					var discountId = Ext.getCmp('hideDiscountID');
					var kitchenId = Ext.getCmp('hideKitchenID');
					
					var actionURL = '';
					
					if(!rate.isValid()){
						return;
					}
					
					if(addDiscountWin.operationType == dmObj.operation.insert){
						actionURL = '../../InsertDiscountPlan.do';
					}else if(addDiscountWin.operationType == dmObj.operation.update){
						actionURL = '../../UpdateDiscountPlan.do';
					}else{
						return;
					}
					
					var save = Ext.getCmp('btnSaveDiscount');
					var cancel = Ext.getCmp('btnCancelDiscount');
					
					save.setDisabled(true);
					cancel.setDisabled(true);
					
					Ext.Ajax.request({
						url : actionURL,
						params : {
							discountId : discountId.getValue(), 
							kitchenId : kitchenId.getValue(),
							rate : rate.getValue()
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								addDiscountWin.hide();
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnSearchDiscountPlan').handler();
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
				id : 'btnCancelDiscount',
				iconCls : 'btn_close',
				handler : function(e){
					addDiscountWin.hide();
				}
			}],
			listeners : {
				show : function(){
					var rate = Ext.get("numKitchenRate");
					rate.focus.defer(100, rate);
				}
			},
			keys : [{
				 key : Ext.EventObject.ENTER,
				 fn : function(){ 
					 Ext.getCmp('btnSaveDiscount').handler();
				 },
				 scope : this 
			 }]
		});
		addDiscountWin.render(document.body);
	}
	
	updateDiscountRateWin = new Ext.Window({
		title : '一键修改折扣率',
		closable : false,
		resizable : false,
		modal : true,
		width : 230,
		items : [{
			xtype : 'form',
			layout : 'form',
			frame : true,
			labelWidth : 65,
			items : [ {
	    	    xtype : 'combo',
	    	    id : 'comboUpdateAllRateByDiscount',
	    	    fieldLabel : '方案名称',
	    	    width : 130,
	    	    store : new Ext.data.JsonStore({
	    	    	root : 'root',
					fields : [ 'discountID', 'discountName' ]
				}),
				valueField : 'discountID',
				displayField : 'discountName',
				mode : 'local',
				triggerAction : 'all',
				typeAhead : true,
				selectOnFocus : true,
				forceSelection : true,
				allowBlank : false,
				readOnly : true
	    	}, {
	    		xtype : 'numberfield',
	    		id : 'numUpdateAllRateByDiscountPlan',
	    	    fieldLabel : '折扣率',
	    	    width : 130,
	    	    allowBlank : true,
	    	    selectOnFocus : true,
	    	    value : 1.00,
				validator : function(v){
					if(v >= 0.00 && v <= 1.00){
						return true;
					}else{
						return '折扣率在 0.00 至 1.00 之间,如 8.8 折输入 0.88 ';
					}
				}
	    	},{
				xtype : 'label',
				width : 200,
				style : 'color:green;font-szie:14px;',
				text : '说明: 如果是八折, 直接输入0.8即可'
			}]
		}],
		bbar : [ '->', {
			text : '保存',
			id : 'btnSaveUpdateDiscountPlanRate',
			iconCls : 'btn_save',
			handler : function(e){
				var discount = Ext.getCmp('comboUpdateAllRateByDiscount');
				var rate = Ext.getCmp('numUpdateAllRateByDiscountPlan');
				
				if(!discount.isValid() || !rate.isValid()){
					return;
				}
				
				Ext.Msg.confirm(
					'提示', 
					'是否将折扣方案"<font color="red">'+discount.getRawValue()+'</font>"下所有厨房折扣率修改为"<font color="red">'+rate.getValue()+'</font>"?',
					function(e){
						if(e == 'yes'){
							var save = Ext.getCmp('btnSaveUpdateDiscountPlanRate');
							var cancel = Ext.getCmp('btnCancelUpdateDiscountPlanRate');
							
							save.setDisabled(true);
							cancel.setDisabled(true);
							
							Ext.Ajax.request({
								url : '../../UpdateDiscountPlanRate.do',
								params : {
									discountID : discount.getValue(),
									rate : rate.getValue()
								},
								success : function(res, opt){
									var jr = Ext.util.JSON.decode(res.responseText);
									if(jr.success){
										updateDiscountRateWin.hide();
										Ext.example.msg(jr.title, jr.msg);
										var treeRoot = programTree.getRootNode().childNodes;
										for(var i = 0; i < treeRoot.length; i++){
											if(treeRoot[i].attributes.discountID == discount.getValue()){
												treeRoot[i].select();
												treeRoot[i].fireEvent('click', treeRoot[i]);
												break;
											}
										}
										Ext.getCmp('btnSearchDiscountPlan').handler();
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
			id : 'btnCancelUpdateDiscountPlanRate',
			iconCls : 'btn_close',
			handler : function(e){
				updateDiscountRateWin.hide();
			}
		}],
		listeners : {
			show : function(){
				var discount = Ext.getCmp('comboUpdateAllRateByDiscount');
				var rate = Ext.getCmp('numUpdateAllRateByDiscountPlan');
				rate.setValue(1.00);
				var tempRoot = {root:[]};
				for(var i = 0; i < programData.root.length; i++){
					tempRoot.root.push(programData.root[i]);
				}
				discount.store.loadData(tempRoot);
				
				var node = Ext.ux.getSelNode(programTree);
				if(node){
					discount.setValue(node.attributes.discountID);
				}else{
					discount.setValue();
				}
				discount.clearInvalid();
			}
		},
		keys : [{
			 key : Ext.EventObject.ENTER,
			 fn : function(){ 
				 Ext.getCmp('btnSaveUpdateDiscountPlanRate').handler();
			 },
			 scope : this 
		 }]
	});
	showFloatOption(discount_obj);	
});

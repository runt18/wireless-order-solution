var btnAddProgram = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddProgram.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加方案',
	handler : function(e){
		programOperationHandler({
			type : dmObj.operation.insert
		});
	}
});

var btnAddDiscount = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddDiscount.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加分厨折扣',
	handler : function(e){
		disocuntOperationHandler({
			type : dmObj.operation.insert
		});
	}
});

var pushBackBut = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn){
		location.href = 'BasicMgrProtal.html?restaurantID=' + restaurantID + '&pin=' + pin;
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '登出',
	handler : function(btn){
		
	}
});

programOperationHandler = function(c){
	if(c == null || typeof(c) == 'undefined' || typeof(c.type) == 'undefined'){
		return;
	}
	
	var id = Ext.getCmp('numDiscountID');
	var name = Ext.getCmp('txtDiscountName');
	var level = Ext.getCmp('numDisplayLevel');
	var rate = Ext.getCmp('numDiscountRate');
	var isAuto = Ext.getCmp('chbIsAuto');
	var isDefault = Ext.getCmp('chbIsDefault');
	var status = Ext.getCmp('hideDiscountStatus');
	
	if(c.type == dmObj.operation.insert){
		Ext.getDom('numDiscountID').parentElement.parentElement.style.display = 'none';
		Ext.getDom('numDiscountRate').parentElement.parentElement.style.display = 'block';
		Ext.getDom('chbIsAuto').parentElement.parentElement.parentElement.style.display = 'block';
		
		name.setValue();
		level.setValue(0);
		rate.setValue(1.00);
		isAuto.setValue(true);
		isDefault.setValue(false);
		addProgramWin.setTitle('添加方案');
	}else if(c.type == dmObj.operation.update){
		Ext.getDom('numDiscountID').parentElement.parentElement.style.display = 'block';
		Ext.getDom('numDiscountRate').parentElement.parentElement.style.display = 'none';
		Ext.getDom('chbIsAuto').parentElement.parentElement.parentElement.style.display = 'none';
		
		var sn = programTree.getSelectionModel().getSelectedNode();
		if(!sn || sn.attributes.discountID == -1){
			Ext.example.msg('提示', '请选中一个方案再进行操作.');
			return;
		}
		id.setValue(sn.attributes.discountID); 
		name.setValue(sn.attributes.discountName); 
		level.setValue(sn.attributes.level); 
		isAuto.setValue(true);
		isDefault.setValue(eval(sn.attributes.isDefault));
		status.setValue(sn.attributes.status);
		
		if(sn.attributes.status == 2 || sn.attributes.status == 3){
			name.setDisabled(true);
			level.setDisabled(true);
		}else{
			name.setDisabled(false);
			level.setDisabled(false);
		}
		
		addProgramWin.setTitle('修改方案');
	}
	
	addProgramWin.operationType = c.type;
	addProgramWin.center();
	addProgramWin.show();
};

disocuntOperationHandler = function(c){
	if(c == null || typeof(c) == 'undefined' || typeof(c.type) == 'undefined'){
		return;
	}
	
	var program = Ext.getCmp('comboProgram');
	var kitchen = Ext.getCmp('comboKitchen');
	var rate = Ext.getCmp('numKitchenRate');
	var planID = Ext.getCmp('hideDiscountPlanID');
	
	var tempRoot = {root:[]};
	for(var i = 0; i < programData.root.length; i++){
		if(eval(programData.root[i].status != 2)){
			tempRoot.root.push(programData.root[i]);
		}
	}
	program.store.loadData(tempRoot);
	
	kitchen.store.loadData(discountData);
	
	if(c.type == dmObj.operation.insert){
		program.setValue();
		kitchen.setValue();
		rate.setValue(1.00);
		program.setDisabled(false);
		kitchen.setDisabled(false);
		addDiscountWin.setTitle('添加分厨折扣');
	}else if(c.type == dmObj.operation.update){
		var sd = Ext.ux.getSelData(discountGrid.getId());
		if(!sd){
			Ext.example.msg('提示', '请选中一个分厨折扣再进行操作.');
			return;
		}
		program.setValue(sd['discount.id']);
		kitchen.setValue(sd['kitchen.kitchenID']);
		rate.setValue(sd['rate']);
		planID.setValue(sd['planID']);
		program.setDisabled(true);
		kitchen.setDisabled(true);
		addDiscountWin.setTitle('修改分厨折扣');
	}
	
	program.clearInvalid();
	kitchen.clearInvalid();
	rate.clearInvalid();
	
	addDiscountWin.operationType = c.type;
	addDiscountWin.center();
	addDiscountWin.show();
};

updateDisocuntOperationHandler = function(){
	disocuntOperationHandler({
		type : dmObj.operation.update
	});
};

deleteDisocuntOperationHandler = function(){
	var sd = Ext.ux.getSelData(discountGrid.getId());
	if(!sd){
		Ext.example.msg('提示', '请选中一个分厨折扣再进行操作.');
		return;
	}
	Ext.Msg.confirm(
		'提示',
		'是否删除分厨折扣',
		function(e){
			if(e == 'yes'){
				Ext.Ajax.request({
					url : '../../DeleteDiscountPlan.do',
					params : {
						planID : sd['planID']
					},
					success : function(res, opt){
						var jr = Ext.util.JSON.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('btnSearchDiscountPlan').handler();
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
};

discountIsDefaultRenderer = function(val, md, record){
	return eval(record.get('discount.status') == 1) ? '是' : '否';
};

discountOperationRenderer = function(){
	return '<a href="javascript:updateDisocuntOperationHandler()">修改</a>'
		   + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		   + '<a href="javascript:deleteDisocuntOperationHandler()">删除</a>';
};

var programTree;
var discountGrid;
var addProgramWin;
var addDiscountWin;
var updateDiscountRateWin;
Ext.onReady(function(){
	Ext.BLANK_IMAGE_URL = '../../extjs/resources/images/default/s.gif';
	Ext.QuickTips.init();
	
	var programTreeTbar = new Ext.Toolbar({
		items : [{
			xtype : 'tbtext',
			text : ' '
		}, '->', {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(){
				programOperationHandler({
					type : dmObj.operation.update
				});		
			}
		}, {
			text : '删除',
			iconCls : 'btn_delete',
			handler : function(){
				var sn = programTree.getSelectionModel().getSelectedNode();
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
		}, {
			text : '刷新',
			id : 'btnRefreshProgramTree',
			iconCls : 'btn_refresh',
			handler : function(){
				programTree.getRootNode().reload();
				Ext.getDom('discountNameShowType').innerHTML = '------------';
				Ext.getCmp('txtSearchKitchenName').setValue();
//				Ext.getCmp('btnSearchDiscountPlan').handler();
			}
		}]
	});
	
	programTree = new Ext.tree.TreePanel({
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
				pin : pin,
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
						if(rn[i].attributes.status == 1 || rn[i].attributes.status == 3){
							rn[i].setText('<font color=\"red\">' + rn[i].attributes.discountName + '&nbsp;(默认方案)</font>');
						}else if(rn[i].attributes.status == 2){
							rn[i].setText('<font color=\"#808080\">' + rn[i].attributes.discountName + '&nbsp;(系统保留)</font>');
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
				if(e.attributes.discountID != -1)
					Ext.getDom('discountNameShowType').innerHTML = e.attributes.discountName;
			},
			dblclick : function(e){
				if(e.attributes.discountID != -1)
					Ext.getCmp('btnSearchDiscountPlan').handler();
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
		}, {
			xtype : 'tbtext', 
			text : '分厨名称:'
		}, {
	    	xtype : 'textfield',
	    	id : 'txtSearchKitchenName'
	    }, '->', {
			text : '搜索',
			id : 'btnSearchDiscountPlan',
			iconCls : 'btn_search',
			handler : function(){
				var sn = programTree.getSelectionModel().getSelectedNode();
				var kitcheName = Ext.getCmp('txtSearchKitchenName');
				
				var dgs = discountGrid.getStore();
				dgs.baseParams['discountID'] = (sn == null || !sn || sn.attributes.discountID == -1 ? '' : sn.attributes.discountID);
				dgs.baseParams['kitchenName'] = kitcheName.getValue();
				dgs.load({
					params : {
						start : 0,
						limit : 50
					}
				});
			}
		}, {
			text : '重置',
			iconCls : 'btn_refresh',
			handler : function(){
				programTree.getSelectionModel().clearSelections();
				Ext.getDom('discountNameShowType').innerHTML = '------------';
				Ext.getCmp('txtSearchKitchenName').setValue();
				Ext.getCmp('btnSearchDiscountPlan').handler();
			}
		}, {
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				disocuntOperationHandler({
					type : dmObj.operation.insert
				});
			}
		}, {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(){
				updateDisocuntOperationHandler();
			}
		}, {
			text : '删除',
			iconCls : 'btn_delete',
			handler : function(){
				deleteDisocuntOperationHandler();
			}
		}, {
			text : '一键修改折扣率',
			iconCls : 'btn_edit_all',
			handler : function(){
				updateDiscountRateWin.show();
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
			[true, false, true, true], 
			['方案编号', 'discount.id'] , 
			['方案名称', 'discount.name'],
//			['默认方案', 'discount.status', 50, 'center', 'discountIsDefaultRenderer'], 
			['分厨名称', 'kitchen.kitchenName'], 
			['折扣率', 'rate', 50, 'right' , 'Ext.ux.txtFormat.gridDou'],
			['操作', 'operation', '', 'center', 'discountOperationRenderer']
		],
		['discount.id', 'discount.name', 'discount.status', 'kitchen.kitchenID', 'kitchen.kitchenName', 'rate', 'planID'],
		[['pin',pin], ['isPaging', true], ['restaurantID', restaurantID]],
		50,
		'',
		discountGridTbar
	);	
	discountGrid.region = 'center';
	
	var centerPanel = new Ext.Panel({
		title : '折扣方案管理',
		region : 'center',
		layout : 'border',
		frame : true,
		items : [programTree, discountGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
			    btnAddProgram,
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
			    btnAddDiscount,
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
			    '->', 
			    pushBackBut, 
			    {
					text : '&nbsp;&nbsp;&nbsp;',
					disabled : true
				}, 
				logOutBut 
			]
		})
	});
	
	new Ext.Viewport({
		layout : 'border',
		id : 'viewport',
		items : [{
			region : 'north',
			bodyStyle : 'background-color:#DFE8F6;',
			html : '<h4 style="padding:10px;font-size:150%;float:left;">无线点餐网页终端</h4><div id="optName" class="optName"></div>',
			height : 50,
			border : false,
			margins : '0 0 0 0'
		},
		centerPanel,
		{
			region : 'south',
			height : 30,
			frame : true,
			html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
		} ]
	});
	
	if(!addProgramWin){
		addProgramWin = new Ext.Window({
			title : '添加方案',
			closable : false,
			resizable : false,
			modal : true,
			width : 230,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
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
					id : 'numDisplayLevel',
					width : 130,
					fieldLabel : '显示等级',
					allowBlank : false,
					value : 0
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
				}, {
					xtype : 'checkbox',
					id : 'chbIsAuto',
					//隐藏标签
					hideLabel : true,
					checked : true,
					boxLabel : '自动生成所有厨房默认折扣',
					listeners : {
						check : function(thiz){
							var rate = Ext.getCmp('numDiscountRate');
							rate.setValue(1.00);
							if(thiz.getValue()){
								rate.setDisabled(false);
							}else{
								rate.setDisabled(true);
							}
						}
					}
				}, {
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
					var level = Ext.getCmp('numDisplayLevel');
					var rate = Ext.getCmp('numDiscountRate');
					var isAuto = Ext.getCmp('chbIsAuto');
					var isDefault = Ext.getCmp('chbIsDefault');
					var status = Ext.getCmp('hideDiscountStatus');
					
					var actionURL = '';
					//判断是否有效
					if(!name.isValid() || !level.isValid() || !rate.isValid()){
						return;
					}
					
					if(addProgramWin.operationType == dmObj.operation.insert){
						actionURL = '../../InsertDiscount.do';
					}else if(addProgramWin.operationType == dmObj.operation.update){
						actionURL = '../../UpdateDiscount.do';
						if(eval(isDefault.getValue() == true)){
							if(status.getValue() == 2)
								status.setValue(3);
							else
								status.setValue(1);
						}else{
							if(status.getValue() == 3)
								status.setValue(2);
							else
								status.setValue(0);
						}
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
							level : level.getValue(),
							rate : rate.getValue(),
							isAuto : isAuto.getValue(),
							isDefault : isDefault.getValue(),
							status : status.getValue()
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
	}
	
	if(!addDiscountWin){
		addDiscountWin = new Ext.Window({
			title : '添加分厨折扣',
			closable : false,
			resizable : false,
			modal : true,
			width : 230,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				labelWidth : 65,
				items : [{
					xtype : 'hidden',
					id : 'hideDiscountPlanID'
				}, {
 	    	    	xtype : 'combo',
 	    	    	id : 'comboProgram',
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
 	    	    	xtype : 'combo',
 	    	    	id : 'comboKitchen',
 	    	    	fieldLabel : '分厨名称',
 	    	    	width : 130,
 	    	    	store : new Ext.data.JsonStore({
 	    	    		root : 'root',
						fields : [ 'kitchenID', 'kitchenName' ]
					}),
					valueField : 'kitchenID',
					displayField : 'kitchenName',
					mode : 'local',
					triggerAction : 'all',
					typeAhead : true,
					selectOnFocus : true,
					forceSelection : true,
					allowBlank : false,
					readOnly : true
 	    	    }, {
 	    	    	xtype : 'numberfield',
 	    	    	id : 'numKitchenRate',
 	    	    	width : 130,
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
					var discountID = Ext.getCmp('comboProgram');
					var kitchenID = Ext.getCmp('comboKitchen');
					var rate = Ext.getCmp('numKitchenRate');
					var planID = Ext.getCmp('hideDiscountPlanID');
					
					var actionURL = '';
					
					if(!discountID.isValid() || !kitchenID.isValid() || !rate.isValid()){
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
							planID : planID.getValue(), 
							discountID : discountID.getValue(),
							kitchenID : kitchenID.getValue(),
							rate : rate.getValue()
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								addDiscountWin.hide();
								Ext.example.msg(jr.title, jr.msg);
								var treeRoot = programTree.getRootNode().childNodes;
								for(var i = 0; i < treeRoot.length; i++){
									if(treeRoot[i].attributes.discountID == discountID.getValue()){
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
									restaurantID : restaurantID, 
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
				var tempRoot = {root:[]};
				for(var i = 0; i < programData.root.length; i++){
					if(eval(programData.root[i].status != 2)){
						tempRoot.root.push(programData.root[i]);
					}
				}
				discount.store.loadData(tempRoot);
				discount.setValue();
				rate.setValue(1.00);
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
	
//	addProgramWin.setPosition(addProgramWin.width * -1 -100, 100);
//	addProgramWin.show();
//	addProgramWin.hide();
//	
//	addDiscountWin.setPosition(addDiscountWin.width * -1 -100, 100);
//	addDiscountWin.show();
//	addDiscountWin.hide();
	if(addProgramWin != null && typeof addProgramWin != 'undefined')
		addProgramWin.render(document.body);
	if(addDiscountWin != null && typeof addDiscountWin != 'undefined')
		addDiscountWin.render(document.body);
	
});
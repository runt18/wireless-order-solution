//----------lib.js------
/**
 * 初始化原料类别操作窗口
 */
function initOperateMaterialCateWin(){
	if(!operateMaterialCateWin){
		var cateId = new Ext.form.Hidden({
			id : 'hideMaterialCateId'
		});
		var cateName = new Ext.form.TextField({
			id : 'txtMaterialCateName',
			fieldLabel : '名称',
			width : 130,
			allowBlank : false
		});
		operateMaterialCateWin = new Ext.Window({
			id : 'operateMaterialCateWin',
			title : '&nbsp;',
			modal : true,
			resizable : false,
			closable : false,
			width : 250,
			items : [{
				xtype : 'form',
				layout : 'form',
				width : 250,
				labelWidth : 65,
				frame : true,
				items : [cateId, cateName]
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					operateMaterialCateWin.hide();
				}
			}, {
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp('btnAddMaterialCate').handler();
				}
			}],
			bbar : [{
				text : '应用',
				id : 'btnAddMoreMaterialCate',
				iconCls : 'btn_app',
				handler : function(){
					var dataSource = "";
					if(operateMaterialCateWin.otype == Ext.ux.otype['insert']){
						dataSource = 'insert';
					}else if(operateMaterialCateWin.otype == Ext.ux.otype['update']){
						dataSource = 'update';
					}else{
						return;
					}
					if(!cateName.isValid()){
						return;
					}
					Ext.Ajax.request({
						url : '../../OperateMaterialCate.do',
						params : {
							dataSource : dataSource,
							restaurantID : restaurantID,
							cateId : cateId.getValue(),
							cateType : operateMaterialCateWin.cateType,
							name : cateName.getValue()
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								if(dataSource == 'update'){
									operateMaterialCateWin.hide();
									operateMaterialCateWin.cateType = '';
									materialCateTree.getRootNode().reload();
								}else{
									Ext.getCmp('txtMaterialCateName').setValue();
									Ext.getCmp('txtMaterialCateName').focus(true, 100);
								}
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt) {
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			},
			'->', {
				text : '保存',
				id : 'btnAddMaterialCate',
				iconCls : 'btn_save',
				handler : function(){
					var dataSource = "";
					if(operateMaterialCateWin.otype == Ext.ux.otype['insert']){
						dataSource = 'insert';
					}else if(operateMaterialCateWin.otype == Ext.ux.otype['update']){
						dataSource = 'update';
					}else{
						return;
					}
					if(!cateName.isValid()){
						return;
					}
					Ext.Ajax.request({
						url : '../../OperateMaterialCate.do',
						params : {
							dataSource : dataSource,
							restaurantID : restaurantID,
							cateId : cateId.getValue(),
							cateType : operateMaterialCateWin.cateType,
							name : cateName.getValue()
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								operateMaterialCateWin.hide();
								operateMaterialCateWin.cateType = '';
								materialCateTree.getRootNode().reload();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt) {
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					//Ext.getCmp('btnAddMaterialCate').setText('保存');
					operateMaterialCateWin.hide();
					materialCateTree.getRootNode().reload();
				}
			}],
			listeners : {
				show : function(thiz){
					thiz.center();
				}
			}
		});
	}
}
/**
 * 操作原料类别
 * @param c
 */
function operateMaterialCateHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.otype == 'undefined'){
		return;
	}
	initOperateMaterialCateWin();
	
	operateMaterialCateWin.otype = c.otype;
	
	var cateId = Ext.getCmp('hideMaterialCateId');
	var cateName = Ext.getCmp('txtMaterialCateName');
	if(c.otype == Ext.ux.otype['insert']){
		operateMaterialCateWin.setTitle('添加原料类别');
		operateMaterialCateWin.show();
		cateId.setValue();
		cateName.setValue();
		cateName.clearInvalid();
		cateName.focus(true, 100);
	}else if(c.otype == Ext.ux.otype['update']){
		var sn = Ext.ux.getSelNode(materialCateTree);
		if(!sn || sn.attributes.cateID == -1){
			Ext.example.msg('提示', '请选中一个原料类别再进行操作.');
			return;
		}
		operateMaterialCateWin.setTitle('修改类别');
		operateMaterialCateWin.show();
		operateMaterialCateWin.cateType = sn.attributes.type;
		cateId.setValue(sn.attributes.cateId);
		cateName.setValue(sn.attributes.name);
		cateName.focus(true, 100);
	}else if(c.otype == Ext.ux.otype['delete']){
		var sn = Ext.ux.getSelNode(materialCateTree);
		if(!sn || sn.attributes.cateID == -1){
			Ext.example.msg('提示', '请选中一个原料类别再进行操作.');
			return;
		}
		Ext.Msg.show({
			title : '重要',
			msg : '是否删除类别?',
			icon: Ext.MessageBox.QUESTION,
			buttons : Ext.Msg.YESNO,
			fn : function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../OperateMaterialCate.do',
						params : {
							dataSource : 'delete',
							restaurantID : restaurantID,
							cateId : sn.attributes.cateId
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								materialCateTree.getRootNode().reload();
								Ext.example.msg(jr.title, jr.msg);
								operateMaterialCateWin.hide();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt) {
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}
		});
	}
}

/**
 * 初始化原料信息操作窗口
 */
function initOperateMaterialWin(){
	if(!operateMaterialWin){
		var materialId = new Ext.form.Hidden({
			id : 'hideMaterialId'
		});
		var materialName = new Ext.form.TextField({
			id : 'txtMaterialName',
			fieldLabel : '名称',
			allowBlank : false
		});
		var materialPrice = new Ext.form.TextField({
			id : 'txtMaterialPrice',
			fieldLabel : '参考成本',
			allowBlank : false
		});		
		
		var alarmAmount = new Ext.form.TextField({
			id : 'alarmAmount_textfield_basicMgrMain',
			fieldLabel : '预警数量',
			allowBlank : true
		});
		
		var initMaterialCate = new Ext.form.ComboBox({
			id : 'txtMaterialCate',
			fieldLabel : '所属类别',
		    store : new Ext.data.SimpleStore({
				fields : ['cateId', 'cateName']
			}),
			valueField : 'cateId',
			displayField : 'cateName',
			mode : 'local',
			triggerAction : 'all',
			typeAhead : true,
			selectOnFocus : true,
			forceSelection : true,
			readOnly : false,
			allowBlank : false
		});
		operateMaterialWin = new Ext.Window({
			id : 'operateMaterialWin',
			title : '&nbsp;',
			modal : true,
			resizable : false,
			closable : false,
			width : 300,
			items : [{
				xtype : 'form',
				layout : 'form',
				labelWidth : 65,
				width : 300,
				frame : true,
				defaults : {
					width : 130
				},
				items : [materialId, materialName, materialPrice, alarmAmount, initMaterialCate]
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					operateMaterialWin.hide();
				}
			}, {
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp('btnAddMaterial').handler();
				}
			}],
			bbar : [ {
				text : '上一个',
		    	id : 'btnPreviousFoodMaterial',
		    	iconCls : 'btn_previous',
		    	tooltip : '加载上一道菜品相关信息',
		    	handler : function(){
		    		inventory_materialBasicGrid.getSelectionModel().selectPrevious();
		    		
		    		var materialId = Ext.getCmp('hideMaterialId');
		    		var materialName = Ext.getCmp('txtMaterialName');
		    		var materialCate = Ext.getCmp('txtMaterialCate');
		    		var materialPrice = Ext.getCmp('txtMaterialPrice');
		    		var alarmAmount = Ext.getCmp('alarmAmount_textfield_basicMgrMain');
		    		
		    		var data = Ext.ux.getSelData(inventory_materialBasicGrid);

		    		operateMaterialWin.cateType = data['cateType'];
		    		
		    		if(data['cateType'] == 1){
		    			Ext.getCmp('txtMaterialCate').store.loadData(materialGoodCateData);
		    			materialId.setValue(data['id']);
		    			materialName.setValue(data['name']);
		    			materialPrice.setValue(data['price']);
		    			materialCate.setValue(data['cateId']);	
		    			alarmAmount.setValue(data['alarmAmount']);
		    			
		    			materialPrice.focus(true, 100);
		    		}else{
		    			Ext.getCmp('txtMaterialCate').store.loadData(materialCateData);
		    			materialId.setValue(data['id']);
		    			materialName.setValue(data['name']);
		    			materialCate.setValue(data['cateId']);	
		    			materialPrice.setValue(data['price']);
		    			alarmAmount.setValue(data['alarmAmount']);
		    			
		    			materialName.focus(true, 100);
		    		}
		    		operateMaterialWin.cateId = data['cateId'];
		    		
		    		Ext.getCmp('btnPreviousFoodMaterial').setDisabled(!inventory_materialBasicGrid.getSelectionModel().hasPrevious());
		    		Ext.getCmp('btnNextFoodMaterial').setDisabled(false);
		    	}
		    }, {
		    	text : '下一个',
		    	id : 'btnNextFoodMaterial',
		    	iconCls : 'btn_next',
		    	tooltip : '加载下一道菜品相关信息',
		    	handler : function(){
		    		inventory_materialBasicGrid.getSelectionModel().selectNext();
		    		var materialId = Ext.getCmp('hideMaterialId');
		    		var materialName = Ext.getCmp('txtMaterialName');
		    		var materialCate = Ext.getCmp('txtMaterialCate');
		    		var materialPrice = Ext.getCmp('txtMaterialPrice');
		    		var alarmAmount = Ext.getCmp('alarmAmount_textfield_basicMgrMain');
		    		
		    		var data = Ext.ux.getSelData(inventory_materialBasicGrid);

		    		operateMaterialWin.cateType = data['cateType'];
		    		
		    		if(data['cateType'] == 1){
		    			Ext.getCmp('txtMaterialCate').store.loadData(materialGoodCateData);
		    			materialId.setValue(data['id']);
		    			materialName.setValue(data['name']);
		    			materialPrice.setValue(data['price']);
		    			materialCate.setValue(data['cateId']);	
		    			alarmAmount.setValue(data['alarmAmount']);
		    		}else{
		    			Ext.getCmp('txtMaterialCate').store.loadData(materialCateData);
		    			materialId.setValue(data['id']);
		    			materialName.setValue(data['name']);
		    			materialCate.setValue(data['cateId']);	
		    			materialPrice.setValue(data['price']);
		    			alarmAmount.setValue(data['alarmAmount']);
		    		}
		    		
		    		if(operateMaterialWin.otype == Ext.ux.otype['insert']){
		    			materialName.focus(true, 100);
		    		}else{
		    			materialPrice.focus(true, 100);
		    		}
		    		
		    		operateMaterialWin.cateId = data['cateId'];
		    		
		    		Ext.getCmp('btnPreviousFoodMaterial').setDisabled(false);
		    		Ext.getCmp('btnNextFoodMaterial').setDisabled(!inventory_materialBasicGrid.getSelectionModel().hasNext());
		    	}
		    },'->', {
				text : '保存并下一个',
				id : 'btnAddMaterial',
				iconCls : 'btn_save',
				handler : function(){
					var dataSource = "";
					if(operateMaterialWin.otype == Ext.ux.otype['insert']){
						dataSource = 'insert';
					}else if(operateMaterialWin.otype == Ext.ux.otype['update']){
						dataSource = 'update';
					}else{
						return;
					}
					if(!materialName.isValid() || !initMaterialCate.isValid() || !materialPrice.isValid()){
						return;
					}
					Ext.Ajax.request({
						url : '../../OperateMaterial.do',
						params : {
							dataSource : dataSource,
							
							restaurantID : restaurantID,
							id : materialId.getValue(),
							price : materialPrice.getValue(),
							name : materialName.getValue(),
							alarmAmount : alarmAmount.getValue(), 
							cateId : initMaterialCate.getValue(),
							cType : operateMaterialWin.cateType
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								if(dataSource == 'update'){
//									operateMaterialWin.hide();
//									operateMaterialWin.cateType = '';
									//下一个
									Ext.getCmp('btnNextFoodMaterial').handler();
								}else{
									Ext.getCmp('txtMaterialName').setValue();
									Ext.getCmp('txtMaterialPrice').setValue();
									
									
									Ext.getCmp('txtMaterialName').focus(true, 100);
									Ext.getCmp('txtMaterialPrice').clearInvalid();
									Ext.getCmp('txtMaterialName').clearInvalid();
									Ext.getCmp('alarmAmount_textfield_basicMgrMain').clearInvalid();
								}
								
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt) {
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					operateMaterialWin.hide();
					Ext.getCmp('btnSearchMaterial').handler();
				}
			}],
			listeners : {
				render : function(){
					initMaterialCate.store.loadData(materialCateData);
				},
				show : function(thiz){
					thiz.center();
				},
				hide : function(){
					Ext.getCmp('txtMaterialName').setValue();
					Ext.getCmp('txtMaterialPrice').setValue();
					
					Ext.getCmp('txtMaterialName').clearInvalid();
					Ext.getCmp('txtMaterialPrice').clearInvalid();
					Ext.getCmp('alarmAmount_textfield_basicMgrMain').clearInvalid();
					
					Ext.getCmp('btnPreviousFoodMaterial').setDisabled(false);
					Ext.getCmp('btnNextFoodMaterial').setDisabled(false);
				}
			}
		});
	}
}

/**
 * 操作原料
 * @param c
 */
function operateMaterialHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.otype == 'undefined'){
		return;
	}
	initOperateMaterialWin();
	
	operateMaterialWin.otype = c.otype;
	
	var materialId = Ext.getCmp('hideMaterialId');
	var materialName = Ext.getCmp('txtMaterialName');
	var materialCate = Ext.getCmp('txtMaterialCate');
	var materialPrice = Ext.getCmp('txtMaterialPrice');
	var alarmAmount = Ext.getCmp('alarmAmount_textfield_basicMgrMain');
	
	var btnPreviousFoodMaterial = Ext.getCmp('btnPreviousFoodMaterial');
	var btnNextFoodMaterial = Ext.getCmp('btnNextFoodMaterial');
	
	if(c.otype == Ext.ux.otype['insert']){
		materialCate.store.loadData(materialGoodCateData.concat(materialCateData));
		
		materialId.setValue();
		materialName.setValue();
		materialCate.setValue();
		materialName.clearInvalid();
		materialCate.clearInvalid();
		materialCate.setDisabled(false);
		
		btnPreviousFoodMaterial.setVisible(false);
		btnNextFoodMaterial.setVisible(false);
		
		operateMaterialWin.setTitle('');
		materialName.show();
		materialCate.show();
		
		var node = materialCateTree.getSelectionModel().getSelectedNode();
		if(node && typeof node.attributes.cateId != 'undefined' && node.attributes.cateId != -1 && node.attributes.type != 1) {
			
			setTimeout(function(){
				materialCate.setValue(node.attributes.cateId);
			}, 250);
		}
		
		operateMaterialWin.show();
		operateMaterialWin.center();		
		
		materialName.focus(true, 100);
	}else if(c.otype == Ext.ux.otype['update']){
		var data = Ext.ux.getSelData(inventory_materialBasicGrid);
		if(!data){
			Ext.example.msg('提示', '请选中一条原料信息再进行操作.');
			return;
		}
		btnPreviousFoodMaterial.setVisible(true);
		btnNextFoodMaterial.setVisible(true);
		
		operateMaterialWin.show();

		operateMaterialWin.cateType = data['cateType'];
		
		if(data['cateType'] == 1){
			materialCate.store.loadData(materialGoodCateData);
			materialId.setValue(data['id']);
			materialName.setValue(data['name']);
			materialPrice.setValue(data['price']);
			alarmAmount.setValue(data['alarmAmount']);
			materialCate.setValue(data['cateId']);	
			
			materialPrice.focus(true, 100);
		}else{
			materialCate.store.loadData(materialCateData);
			materialId.setValue(data['id']);
			materialName.setValue(data['name']);
			materialCate.setValue(data['cateId']);
			alarmAmoutn.setValue(data['alarmAmount']);
			materialPrice.setValue(data['price']);
			
			materialName.focus(true, 100);
		}
		operateMaterialWin.cateId = data['cateId'];
		
		operateMaterialWin.center();		
		
	}else if(c.otype == Ext.ux.otype['delete']){
		var data = Ext.ux.getSelData(inventory_materialBasicGrid);
		if(!data){
			Ext.example.msg('提示', '请选中一条原料信息再进行操作.');
			return;
		}
		Ext.Msg.show({
			title : '重要',
			msg : '是否删除物品信息?',
			icon: Ext.MessageBox.QUESTION,
			buttons : Ext.Msg.YESNO,
			fn : function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../OperateMaterial.do',
						params : {
							dataSource : 'delete',
							restaurantID : restaurantID,
							id : data['id']
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnSearchMaterial').handler();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt) {
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}
		});
	}
}
//---------

//------------load
function materialBasicGridOperateRenderer(){
	return ''
		+ '<a href=\"javascript:operateMaterialHandler({otype:Ext.ux.otype[\'update\']})">修改</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		+ '<a href=\"javascript:operateMaterialHandler({otype:Ext.ux.otype[\'delete\']})">删除</a>';
}
function initControl(){
	var materialCateTreeTbae = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				operateMaterialCateHandler({otype:Ext.ux.otype['insert']});
			}
		}, {
			text : '设置商品',
			iconCls : 'btn_edit',
			handler : function(){
				fnSetGoodWin();
				setGoodWin.show();
			}
		}, {
			text : '刷新',
			iconCls : 'btn_refresh',
			handler : function(){
				materialCateTree.getRootNode().reload();
			}
		}]
	});
	materialCateTree = new Ext.tree.TreePanel({
		id : 'materialCateTree',
		title : '类别信息',
		region : 'west',
		width : 200,
		border : true,
		rootVisible : false,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		tbar : materialCateTreeTbae,
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryMaterialCate.do',
			baseParams : {
				dataSource : 'tree',
				restaurantID : restaurantID
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部',
	        leaf : false,
	        border : true,
	        cateId : -1,
	        name : '全部',
	        listeners : {
	        	load : function(thiz, records){
	        		materialCateData = [];
	        		materialGoodCateData = [];
	        		Ext.Ajax.request({
	        			url : '../../QueryMaterialCate.do',
	        			params : {dataSource : 'normal'},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							materialCateData.length = 0;
							materialGoodCateData.length = 0;
							for (var i = 0; i < jr.root.length; i++) {
								if(jr.root[i].typeValue == 2){
									materialCateData.push([jr.root[i].id, jr.root[i].name]);
								}else{
									materialGoodCateData.push([jr.root[i].id, jr.root[i].name]);
								}
								
							}
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
	        		});
	        		Ext.getCmp('btnSearchMaterial').handler();
	        	}
	        }
		}),
		listeners : {
			click : function(e){
				Ext.getDom('displayQueryMaterialCate').innerHTML = e.text;
				
				Ext.getCmp('btnSearchMaterial').handler();
				if(e.attributes.cateId == -1){
					return false;
				}				
				
				if(e.attributes.type == 2){
					inventory_materialBasicGrid.getColumnModel().setHidden(3,true);  
				}else{
					inventory_materialBasicGrid.getColumnModel().setHidden(3,false);
				}
			}
		}
	});
	
	var materialBasicGridTbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : String.format(Ext.ux.txtFormat.typeName, '类别', 'displayQueryMaterialCate', '----')
		}, {
			xtype : 'tbtext',
			text : '物品名称:'
		}, {
			xtype : 'textfield',
			id : 'txtSearchForMaterialName',
			width : 100
		}, '->', {
			text : '刷新',
			id : 'btnSearchMaterial',
			iconCls : 'btn_refresh',
			handler : function(){
				var sn = materialCateTree.getSelectionModel().getSelectedNode();
				var name = Ext.getCmp('txtSearchForMaterialName');
				var gs = inventory_materialBasicGrid.getStore();
				gs.baseParams['cateType'] = (sn == null || !sn || sn.attributes.cate == -1 ? '' : sn.attributes.cate);
				gs.baseParams['cateId'] = (sn == null || !sn || sn.attributes.cateId == -1 ? '' : sn.attributes.cateId);
				gs.baseParams['name'] = name.getValue();
				gs.load({
					params : {
						start : 0,
						limit : GRID_PADDING_LIMIT_20
					}
				});
			}
		}, {
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				operateMaterialHandler({otype:Ext.ux.otype['insert']});
			}
		}, {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(){
				operateMaterialHandler({otype:Ext.ux.otype['update']});
			}
		}, {
			text : '删除',
			iconCls : 'btn_delete',
			handler : function(){
				operateMaterialHandler({otype:Ext.ux.otype['delete']});
			}
		}]
	});
	inventory_materialBasicGrid = createGridPanel(
		'inventory_materialBasicGrid',
		'原料信息',
		'',
		'',
		'../../QueryMaterial.do',
		[
			[true, false, false, true], 
			['物品名称', 'name', 150],
			['所属类别', 'cateName'],
			['对应菜品', 'belongFood', 220],
			['总数量', 'stock',,'right'],
			['单位成本', 'price',,'right'],
			['状态', 'statusText',,'center'],
			['最后修改人', 'lastModStaff'],
			['最后修改时间', 'lastModDateFormat', 150],
			['操作', 'operate', 150, 'center', 'materialBasicGridOperateRenderer']
		],
		['id', 'name', 'belongFood', 'cateId', 'cateName','cateType', 'stock', 'price', 'statusValue', 'statusText',
		 'lastModStaff', 'lastModDate', 'lastModDateFormat', 'isGood'],
		[['isPaging', true],  ['restaurantID', restaurantID], ['dataSource', 'normal']],
		GRID_PADDING_LIMIT_25,
		'',
		materialBasicGridTbar
	);
	inventory_materialBasicGrid.region = 'center';
	inventory_materialBasicGrid.on('rowdblclick', function(){
		operateMaterialHandler({otype:Ext.ux.otype['update']});		
	});
	inventory_materialBasicGrid.keys = [{
		 key : Ext.EventObject.ENTER,
		 fn : function(){ 
			 Ext.getCmp('btnSearchMaterial').handler();
		 },
		 scope : this 
	}];
	
}
	

//--------------
var btnAddMaterialCate = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddMaterialCate.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加原料类别',
	handler : function(btn){
		operateMaterialCateHandler({otype:Ext.ux.otype['insert']});
	}
});

var btnAddMaterial = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddMaterial.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加原料',
	handler : function(btn){
		operateMaterialHandler({otype:Ext.ux.otype['insert']});
	}
});


var material_selectGoodTbar = new Ext.Toolbar({
	items : [{xtype : 'tbtext', text : '类型:'},{
			id : 'material_goodCombo',
			xtype : 'combo',
			readOnly : false,
			forceSelection : true,
			value : -1,
			width : 100,
			store : new Ext.data.SimpleStore({
				fields : ['id', 'name']
			}),
			valueField : 'id',
			displayField : 'name',
			listeners : {
				render : function(thiz){
					var data = [[-1,'全部']];
					Ext.Ajax.request({
						url : '../../QueryKitchen.do',
						params : {dataSource : 'normal', flag : 'simple'},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							for(var i = 0; i < jr.root.length; i++){
								data.push([jr.root[i]['id'], jr.root[i]['name']]);
							}
							thiz.store.loadData(data);
							thiz.setValue(-1);
						},
						failure : function(res, opt){
							thiz.store.loadData(data);
							thiz.setValue(-1);
						}
					});
				},
				select : function(){
					Ext.getCmp('material_btnSearchGood').handler();
				}
			},
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true
		}, {xtype : 'tbtext', text : '&nbsp;&nbsp;&nbsp;'},
		{xtype : 'tbtext', text : '名称:'},{
			xtype : 'textfield',
			id : 'material_goodName'
		},'->',	 
	{
		text : '刷新',
		id : 'material_btnSearchGood',
		iconCls : 'btn_refresh',
		handler : function(){
			var goodType = Ext.getCmp('material_goodCombo');
			
			var gs = selectGoodGrid.getStore();	
			
			gs.baseParams['kitchen'] = goodType.getValue();
			gs.baseParams['name'] = Ext.getCmp('material_goodName').getValue();
			
			gs.load({
				params : {
					start : 0,
					limit : 200
				}
			});	
/*			gs.on('load', function(store, records, options){
				material_goodList = '';
				if(records.length == 0){
					Ext.example.msg('提示', '此菜品不存在或已设置为商品');
					Ext.getCmp('material_goodName').focus(true, 100);
				}else{
					for (var i = 0; i < records.length; i++) {
						if(i > 0){
							material_goodList += ",";
						}
						material_goodList += records[i].get('id');
					}				
				}

			});	*/	
		}
	}]		
});

var selectGoodGrid = createGridPanel(
	'',
	'',
	480,
	400,
	'../../QueryMaterial.do',
	[
		[true, false, false, true],
		['名称', 'name']
	],
	['id','name'],
	[['isPaging', true], ['dataSource', 'selectToBeGood']],
	200,
	'',
	[material_selectGoodTbar]
);	

selectGoodGrid.getStore().on('load', function(store, records, options){
	material_goodList = '';
	if(records.length == 0){
		Ext.example.msg('提示', '此菜品不存在或已设置为商品');
		Ext.getCmp('material_goodName').focus(true, 100);
	}else{
		for (var i = 0; i < records.length; i++) {
			if(i > 0){
				material_goodList += ",";
			}
			material_goodList += records[i].get('id');
		}				
	}

});
selectGoodGrid.keys = [{
	 key : Ext.EventObject.ENTER,
	 fn : function(){ 
		 Ext.getCmp('material_btnSearchGood').handler();
	 },
	 scope : this 
}];


function fnSetGoodWin(){
	
	
	if(!setGoodWin){
		setGoodWin = new Ext.Window({
			id : 'setGoodWin',
			title : '选择菜品',
			modal : true,
			resizable : false,
			closable : false,
			width : 400,
			layout : 'fit',
			items : [selectGoodGrid],	
			bbar : ['->', {
				text : '设置为商品',
				iconCls : 'btn_save',
				handler : function(){
					Ext.Ajax.request({
						url : '../../OperateMaterial.do',
						params : {dataSource : 'setToBeGood', material_goodList : material_goodList},
						success : function(res, opt){
							setGoodWin.hide();
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								materialCateTree.getRootNode().reload();
								Ext.example.msg(jr.title, jr.msg);
							}
						},
						failure : function(res, opt){
							Ext.example.msg(Ext.decode(res.responseText));
						}
					});					
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					setGoodWin.hide();
				}
			}],
			listeners : {
				hide : function(thiz){
					Ext.getCmp('material_goodCombo').setValue(-1);	
					selectGoodGrid.getStore().removeAll();
					Ext.getCmp('material_goodName').setValue();
				}
			}
		});		
	}

}

var material_bar = {treeId : 'materialCateTree',option : [{name : '修改', fn : "operateMaterialCateHandler({otype:Ext.ux.otype['update']})"}, {name : '删除', fn : "operateMaterialCateHandler({otype:Ext.ux.otype['delete']})"}]};

Ext.onReady(function(){
	
	initControl();
	
	new Ext.Panel({
		renderTo : 'divMaterial',
		id : 'materialPanel',
		layout : 'border',
		//width : parseInt(Ext.getDom('divMaterial').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divMaterial').parentElement.style.height.replace(/px/g,'')),
		frame : true,
		items : [materialCateTree, inventory_materialBasicGrid]
/*		tbar : new Ext.Toolbar({
			height : 55,
			items : [btnAddMaterialCate, {
			    xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			}, btnAddMaterial]
		})*/
	});
	
	showFloatOption(material_bar);
});
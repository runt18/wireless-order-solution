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
							name : cateName.getValue()
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								if(dataSource == 'update'){
									operateMaterialCateWin.hide();
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
							name : cateName.getValue()
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								operateMaterialCateWin.hide();
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
		operateMaterialCateWin.setTitle('修改原料类别');
		operateMaterialCateWin.show();
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
			width : 250,
			items : [{
				xtype : 'form',
				layout : 'form',
				labelWidth : 65,
				width : 250,
				frame : true,
				defaults : {
					width : 130
				},
				items : [materialId, materialName,materialPrice, initMaterialCate]
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
			bbar : [{
				text : '应用',
				id : 'btnAddMoreMaterial',
				iconCls : 'btn_app',
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
							cateId : initMaterialCate.getValue(),
							cType : operateMaterialWin.cateType
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								if(dataSource == 'update'){
									operateMaterialWin.hide();
									operateMaterialWin.cateType = '';
									Ext.getCmp('btnSearchMaterial').handler();
								}else{
									Ext.getCmp('txtMaterialName').setValue();
									Ext.getCmp('txtMaterialPrice').setValue();
									
									Ext.getCmp('txtMaterialPrice').clearInvalid();
									Ext.getCmp('txtMaterialName').focus(true, 100);
									
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
			},'->', {
				text : '保存',
				id : 'btnAddMaterial',
				iconCls : 'btn_save',
				handler : function(){
					var dataSource = "";
					var cateId;
					if(operateMaterialWin.otype == Ext.ux.otype['insert']){
						dataSource = 'insert';
						if(!materialName.isValid() || !initMaterialCate.isValid() || !materialPrice.isValid()){
							return;
						}						
						cateId = initMaterialCate.getValue();
					}else if(operateMaterialWin.otype == Ext.ux.otype['update']){
						dataSource = 'update';
						if(!materialPrice.isValid()){
							return;
						}		
						cateId = operateMaterialWin.cateId;
					}else{
						return;
					}
					Ext.Ajax.request({
						url : '../../OperateMaterial.do',
						params : {
							dataSource : dataSource,
							price : materialPrice.getValue(),
							restaurantID : restaurantID,
							id : materialId.getValue(),
							name : materialName.getValue(),
							cateId : cateId,
							cType : operateMaterialWin.cateType
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								operateMaterialWin.cateType = '';
								Ext.example.msg(jr.title, jr.msg);
								operateMaterialWin.hide();
								Ext.getCmp('btnSearchMaterial').handler();
								operateMaterialWin.cateId = null;
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
				show : function(thiz){
					thiz.center();
					initMaterialCate.store.loadData(materialCateData);
				},
				hide : function(){
					Ext.getCmp('txtMaterialName').setValue();
					Ext.getCmp('txtMaterialPrice').setValue();
					
					Ext.getCmp('txtMaterialName').clearInvalid();
					Ext.getCmp('txtMaterialPrice').clearInvalid();
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
	if(c.otype == Ext.ux.otype['insert']){
		
		materialId.setValue();
		materialName.setValue();
		materialCate.setValue();
		materialName.clearInvalid();
		materialCate.clearInvalid();
		materialCate.setDisabled(false);
		
		operateMaterialWin.setTitle('');
		materialName.show();
		materialCate.show();
		materialName.getEl().up('.x-form-item').setDisplayed(true);
		materialCate.getEl().up('.x-form-item').setDisplayed(true);		
		
		var node = materialCateTree.getSelectionModel().getSelectedNode();
		if(node && typeof node.attributes.cateId != 'undefined' && node.attributes.cateId != -1) {
			materialCate.setValue(node.attributes.cateId);
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
		
		operateMaterialWin.show();

		operateMaterialWin.cateType = data['cateType'];
		
		if(data['cateType'] == 1){
			operateMaterialWin.setTitle('商品名称 -- ' + data['name']);
			operateMaterialWin.cateId = data['cateId'];
			materialId.setValue(data['id']);
			materialPrice.setValue(data['price']);
			materialName.hide();
			materialCate.hide();
			
			materialName.getEl().up('.x-form-item').setDisplayed(false);
			materialCate.getEl().up('.x-form-item').setDisplayed(false);
			
			materialPrice.focus(true, 100);
		}else{
			operateMaterialWin.setTitle('');
			materialName.show();
			materialCate.show();
			materialName.getEl().up('.x-form-item').setDisplayed(true);
			materialCate.getEl().up('.x-form-item').setDisplayed(true);
			materialId.setValue(data['id']);
			materialName.setValue(data['name']);
			materialCate.setValue(data['cateId']);	
			materialPrice.setValue(data['price']);
			
			materialName.focus(true, 100);
		}
		
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
	        	load : function(thiz){
	        		materialCateData = [];
	        		Ext.Ajax.request({
	        			url : '../../QueryMaterialCate.do',
	        			params : {dataSource : 'normal', type : 2},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							for (var i = 0; i < jr.root.length; i++) {
								materialCateData.push([jr.root[i].id, jr.root[i].name]);
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
			text : '搜索',
			id : 'btnSearchMaterial',
			iconCls : 'btn_search',
			handler : function(){
				var sn = materialCateTree.getSelectionModel().getSelectedNode();
				var name = Ext.getCmp('txtSearchForMaterialName');
				var gs = inventory_materialBasicGrid.getStore();
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
			['物品名称', 'reName', 300],
			['所属类别', 'cateName'],
			['总数量', 'stock'],
			['单位成本', 'price'],
			['状态', 'statusText'],
			['最后修改人', 'lastModStaff'],
			['最后修改时间', 'lastModDateFormat', 150],
			['操作', 'operate', 150, 'center', 'materialBasicGridOperateRenderer']
		],
		['id', 'name', 'reName', 'cateId', 'cateName','cateType', 'stock', 'price', 'statusValue', 'statusText',
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
	
	inventory_materialBasicGrid.getStore().on('load', function(thiz, records){
		
		for(var i = 0; i < records.length; i++){
			var record = records[i];
			var type = record.get('isGood');
			if(!type){
				record.set('reName', record.get('name'));
				record.commit();
			}
		}
			
	});	
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
		text : '搜索',
		id : 'material_btnSearchGood',
		iconCls : 'btn_search',
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
			
			gs.on('load', function(store, records, options){
				material_goodList = '';
				for (var i = 0; i < records.length; i++) {
					if(i > 0){
						material_goodList += ",";
					}
					material_goodList += records[i].get('id');
				}
			});		
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

var material_bar = {treeId : 'materialCateTree',operateTree:Ext.ux.operateTree_material, mult : [{type : 2, option :[{name : '修改', fn : "operateMaterialCateHandler({otype:Ext.ux.otype['update']})"}, {name : '删除', fn : "operateMaterialCateHandler({otype:Ext.ux.otype['delete']})"}]}, 
											{type : 1, option :[{name : '删除', fn : "operateMaterialCateHandler({otype:Ext.ux.otype['delete']})"}]} ]};

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
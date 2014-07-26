//-----------------load
var taste_filterTypeComb = new Ext.form.ComboBox({
	fieldLabel : '过滤',
	forceSelection : true,
	width : 100,
	value : 0,
	id : 'tasteFilter',
	store : new Ext.data.SimpleStore({
		fields : [ 'value', 'text' ],
		data : [[0, '全部'],[2, '价格'],[3, '名称']]
	}),
	valueField : 'value',
	displayField : 'text',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	allowBlank : false,
	listeners : {
		select : function(combo, record, index) {
			var oCombo = Ext.getCmp('operator');
			var ct = Ext.getCmp('txtSearchForTextField');
			var cn = Ext.getCmp('txtSearchForNumberField');
			
			if(index == 0){
				// 全部
				oCombo.setVisible(false);
				ct.setVisible(false);
				cn.setVisible(false);
				conditionType = '';
			}else if(index == 1){
				oCombo.setVisible(true);
				ct.setVisible(false);
				cn.setVisible(true);
				oCombo.setValue(1);
				cn.setValue();
				conditionType = cn.getId();
			}else if(index == 2){
				oCombo.setVisible(false);
				ct.setVisible(true);
				cn.setVisible(false);
				ct.setValue();
				conditionType = ct.getId();
			}else if(index == 3){
				oCombo.setVisible(false);
				ct.setVisible(false);
				cn.setVisible(false);
			}
			
		}
	}
});



function initTasteCateOperatorWin(){
	tasteCateOperatorWin = Ext.getCmp('taste_tasteCateOperatorWin');
	if(!tasteCateOperatorWin){
		tasteCateOperatorWin = new Ext.Window({
			id : 'taste_tasteCateOperatorWin',
			title : '添加',
			width : 280,
			closable : false,
			resizable : false,
			modal : true,
			items : [{
				layout : 'form',
				labelWidth : 60,
				width : 280,
				border : false,
				frame : true,
				items : [{
					xtype : 'textfield',
					fieldLabel : '名称',
					id : 'txtTasteCateName',
					allowBlank : false,
					width : 160,
					validator : function(v){
						if(Ext.util.Format.trim(v).length > 0){
							return true;
						}else{
							return '名称不能为空.';
						}
					},
					listeners : {
						focus : function(e){
							e.focus(true, 100);
						}
					}
				},{
					xtype:'hidden',
					id:'hideTasteCateId'
				}]
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					tasteCateOperatorWin.hide();
				}
			}, {
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp('taste_btnSave').handler();
				}
			
			}],
			bbar : ['->', {
				text : '保存',
				id : 'taste_btnSave',
				iconCls : 'btn_save',
				handler : function(){
					var tasteCateName = Ext.getCmp('txtTasteCateName');
					if(!tasteCateName.isValid()){
						return;
					}
					Ext.Ajax.request({
						url : '../../OperateTasteCate.do',
						params : {
							dataSource : tasteCateOperatorWin.otype,
							tasteCateName : tasteCateName.getValue(),
							categoryId : Ext.getCmp('hideTasteCateId').getValue()
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.ux.showMsg(jr);
								tmm_tasteTree.getRootNode().reload();
								tasteCateOperatorWin.hide();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}, {
				text : '取消',
				id : 'btnCancelTasteCateWin',
				iconCls : 'btn_cancel',
				handler : function(){
					tasteCateOperatorWin.hide();
				}
			}]
			
		});
	}
}

function initTasteOperatorWin(){
	tasteOperatorWin = Ext.getCmp('taste_tasteOperatorWin');
	if(!tasteOperatorWin){
		tasteOperatorWin = new Ext.Window({
			id : 'taste_tasteOperatorWin',
			title : '添加',
			width : 310,
			closeAction : 'hide',
			closable : false,
			resizable : false,
			modal : true,
			items : [{
				layout : 'form',
				labelWidth : 60,
				width : 310,
				border : false,
				frame : true,
				items : [{
					xtype : 'textfield',
					fieldLabel : '名称',
					id : 'txtTasteName',
					allowBlank : false,
					width : 210,
					listeners : {
						focus : function(e){
							e.focus(true, 100);
						}
					},
					validator : function(v){
						if(Ext.util.Format.trim(v).length > 0){
							return true;
						}else{
							return '名称不能为空.';
						}
					}
				}, {
					xtype : 'combo',
					fieldLabel : '类型',
					forceSelection : true,
					width : 210,
//					value : '规格',
					id : 'comboTasteCate',
					store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ],
						data : tasteTypeData
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					allowBlank : false,
					readOnly : false,
					listeners : {
						select : function(e){
							var data = Ext.ux.getSelData(tasteGrid);
							var tastePrice = Ext.getCmp('numTastePrice');
							var tasteRate = Ext.getCmp('panelTasteRate');
//							var displayCalc = Ext.getCmp('txtDisplayCalc');
							if(data['typeValue'] == 1){
								tastePrice.setDisabled(true);
								tasteRate.show();
								tastePrice.setValue(0);
//								displayCalc.setValue('按比例');
								
							}else{
								tastePrice.setDisabled(false);
								tasteRate.hide();
//								displayCalc.setValue('按价格');
							}
						},
						render : function(e){
							e.store.loadData(tasteTypeData);
						}
					}
				}, {
					xtype : 'numberfield',
					fieldLabel : '加收价钱',
					id : 'numTastePrice',
					value : 0.00,
					allowBlank : false,
					width : 210
				}, 
/*					{
					xtype : 'numberfield',
					fieldLabel : '比例',
					id : 'numTasteRate',
					value : 0.00,
					allowBlank : false,
					width : 170,
					validator : function(v) {
						if (v < 0.00 || v > 9.99) {
							return '比例范围是 0.00 至 9.99';
						} else {
							return true;
						}
					}
				}, */
/*				{
					xtype : 'textfield',
					id : 'txtDisplayCalc',
					fieldLabel : '计算方式',
					readOnly : false,
					disabled : true,
					width : 170,
					value : '按价格'
				}, */
				{
					xtype:'hidden',
					id:'hideTasteId'
				},{
					layout : 'column',
					id : 'panelTasteRate',
					frame : true,
					border : false,
					frame : false,
					defaults : {
						columnWidth : .20,
						labelWidth : 40
					},
					items : [{
						columnWidth : .25,
						xtype : 'label',
						labelWidth : 60,
						html : '加收比率:&nbsp; '
					},{
						columnWidth : .18,
						items : [{
							xtype : 'radio',
							name : 'tasteRate',
							id : 'rdoFifty',
							inputValue : 1,
							hideLabel : true,
//							checked : true,
							boxLabel : '50%'
						}]
					}, {
						
						items : [{
							xtype : 'radio',
							name : 'tasteRate',
							inputValue : 2,
							hideLabel : true,
							boxLabel : '100%'
						}]
					},{
						columnWidth : .15,
						items : [{
							xtype : 'radio',
							id : 'radioTasteRate',
							name : 'tasteRate',
							inputValue : 3,
							hideLabel : true,
							boxLabel : '其他',
							listeners : {
								check  : function(thiz, checked){
									var tasteRate = Ext.getCmp('numTasteRateOther');
									if(checked){
										tasteRate.enable();
										tasteRate.focus();
									}else{
										tasteRate.disable();
										tasteRate.setValue();
										tasteRate.clearInvalid();
									}
								}
							}
						}]
					},{
						columnWidth : .12,
						items : [{
							xtype : 'numberfield',
							id : 'numTasteRateOther',
							allowBlank : false,
							disabled : true,
							width : 35,
							validator : function(v) {
								if (v < 0.00 || v > 100) {
									return '比例范围是 0.00 至 100';
								} else {
									return true;
								}
							}
						}]
					},{
						columnWidth : .1,
						items : [{
							xtype : 'tbtext',
							style : 'padding: 4px 0px 2px 2px;',
							text : '%'
						}]
					}]
						
				}]
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					tasteOperatorWin.hide();
				}
			}, {
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp('btnSaveTaste').handler();
				}
			}],
			bbar : [ '->',  {
				text : '保存',
		    	id : 'btnSaveTaste',
		    	iconCls : 'btn_save',
				handler : function() {
					var otherTasteRate = 0;
					var tasteId = Ext.getCmp('hideTasteId');
					var tasteName = Ext.getCmp('txtTasteName');
					var tastePrice = Ext.getCmp('numTastePrice');
					var numTasteRate = Ext.getCmp('numTasteRateOther');
					var tasteCate = Ext.getCmp('comboTasteCate');
					var tasteRate = document.getElementsByName('tasteRate');
					
					if(!tasteCate.isValid() || !tasteName.isValid()){
						return;
					}
					if(tasteCate.getValue() == 0){
						if(!tastePrice.isValid()){
							return;
						}
					}
					if(!numTasteRate.isValid()){
						return;
					}
					
					if(Ext.getCmp('panelTasteRate').isVisible()){
						for ( var i = 0; i < tasteRate.length; i++) {
							if(tasteRate[i].checked){
								if(tasteRate[i].value == 1){
									otherTasteRate = 0.5;
								}else if(tasteRate[i].value == 2){
									otherTasteRate = 1;
								}else{
									otherTasteRate = eval(numTasteRate.getValue() / 100);
								};
							}
						}
						
					}
					
					var btnSave = Ext.getCmp('btnSaveTaste');
					var btnCancel = Ext.getCmp('btnCancelTasteWin');
					btnSave.setDisabled(true);
					btnCancel.setDisabled(true);
					Ext.Ajax.request({
						url : '../../OperateTaste.do',
						params : {
							'dataSource' : tasteOperatorWin.otype.toLowerCase(),
							
							id : tasteId.getValue(),
							name : tasteName.getValue(),
							price : tastePrice.getValue(),
							rate : otherTasteRate,
							cate : tasteCate.getValue()
						},
						success : function(res, opt) {
							btnSave.setDisabled(false);
							btnCancel.setDisabled(false);
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								tasteOperatorWin.hide();
								Ext.example.msg(jr.title, jr.msg);
								if(tasteOperatorWin.otype.toLowerCase() == 'insert'){
									var bToolBar = Ext.getCmp('taste_grid').getBottomToolbar();
									var totalCount = Ext.getCmp('taste_grid').getStore().getTotalCount();
									bToolBar.changePage(Math.ceil(totalCount/GRID_PADDING_LIMIT_20));
								}else{
									tasteGrid.getStore().reload();
								}
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt) {
							btnSave.setDisabled(false);
							btnCancel.setDisabled(false);
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}, {
				text : '取消',
				id : 'btnCancelTasteWin',
				iconCls : 'btn_cancel',
				handler : function() {
					tasteOperatorWin.hide();
				}
			}]
		});
	}

}

//-------------
var tasteAddBut = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddForBigBar.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加口味',
	handler : function(btn) {
		tasteInsertHandler();
	}
});

function tasteGridRenderer(value, cellmeta, record, rowIndex, columnIndex, store) {
	var operate = '<a href=\"javascript:tasteUpdateHandler();\">修改</a>';
	if(record.get('typeValue') != 1){
		operate += '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';
		operate += '<a href=\"javascript:tasteDeleteHandler()\">删除</a>';
	}
	return operate;
};

function tasteRateFormat(v){
	return (v * 100) + '%';
}

function initTasteGrid(){
	var tasteGridTbar = new Ext.Toolbar({
		items : [ {
			xtype : 'tbtext',
			text : String.format(
				Ext.ux.txtFormat.typeName,
				'类型','lblTasteCateName','全部'
			)
		},{ 
			xtype:'tbtext',
			text:'过滤:'
		}, taste_filterTypeComb, { 
			xtype:'tbtext', 
			text:'&nbsp;&nbsp;'
		}, {
			xtype : 'combo',
			id : 'operator',
	    	hidden : true,
	    	forceSelection : true,
	    	width : 100,
	    	value : 0,
	    	store : new Ext.data.SimpleStore({
	    		fields : [ 'value', 'text' ],
	    		data : [[1, '等于'], [2, '大于等于'], [3, '小于等于']]
	    	}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			readOnly : false
	    }, { 
	    	xtype:'tbtext', 
	    	text:'&nbsp;&nbsp;'
	    }, {
	    	xtype : 'textfield',
			id : 'txtSearchForTextField',
			hidden : true,
			width : 120
		}, {
			xtype: 'numberfield',
			id : 'txtSearchForNumberField',
			hidden : true,
			width : 120
		},'->',{
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				tasteInsertHandler();
			}
		}, {
			text : '搜索',				
			id : 'btnSerachForTasteBasic',
			iconCls : 'btn_search',
			handler : function(e){
				var node = tmm_tasteTree.getSelectionModel().getSelectedNode();
				var oCombo = Ext.getCmp('operator');
				var st = Ext.getCmp('txtSearchForTextField');
				var sn = Ext.getCmp('txtSearchForNumberField');
//				var cate = Ext.getCmp('comboSearchForTasteType');
				var gs = tasteGrid.getStore();
				gs.baseParams['ope'] = oCombo.getValue();
				
				gs.baseParams['alias'] = taste_filterTypeComb.getValue() == 1 ? sn.getValue() : '';
				gs.baseParams['price'] = taste_filterTypeComb.getValue() == 2 ? sn.getValue() : '';
				gs.baseParams['name'] = taste_filterTypeComb.getValue() == 3 ? st.getValue() : '';
				gs.baseParams['cate'] = node?node.id : '';					
				
				gs.load({
					params : {
						start : 0,
						limit : GRID_PADDING_LIMIT_20
					}
				});
			}
	    }]
	});
	tasteGrid = createGridPanel(
		'taste_grid',
		'',
		'',
		'',
		'../../QueryTaste.do',
		[
			[true, false, false, true], 
			['名称', 'name'],
			['价格', 'price',,'right','Ext.ux.txtFormat.gridDou'],
			['比例', 'rate',,'right','tasteRateFormat'],
			['计算方式', 'calcText',,'right'],
			['类型', 'cateText',,'right'],
			['操作','operate', 200 ,'center','tasteGridRenderer']
		],
		TasteRecord.getKeys(),
		[['isPaging', true], ['restaurantID', restaurantID]],
		GRID_PADDING_LIMIT_20,
		'',
		tasteGridTbar
	);	
	tasteGrid.region = 'center';
	tasteGrid.on('render', function(thiz){
		Ext.getCmp('btnSerachForTasteBasic').handler();
	});
	tasteGrid.on('rowdblclick', function(thiz){
		tasteUpdateHandler();
	});
	
}

function tableBasicGridOperateRenderer(v, m, r, ri, ci, s){
	return ''
		+ '<a href="javascript:updateTableBasicHandler();">修改</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;'
		+ '<a href="javascript:deleteTableBasicHandler();">删除</a>';
}
function initGrid(){
	var tableBasicGridTbar = new Ext.Toolbar({
		items : [{
			xtype : 'tbtext',
			text : String.format(Ext.ux.txtFormat.typeName, '区域', 'displaySearchRegion', '----')
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;编号:'
		}, {
			xtype : 'numberfield',
			id : 'numSearchForTableAlias',
			width : 100,
			style : 'text-align:left;'
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;名称:'
		}, {
			xtype : 'textfield',
			id : 'txtSearchForTableName',
			width : 100
		}, '->', {
				text : '添加',
				iconCls : 'btn_add',
				handler : function(){
					insertTableBasicHandler();
				}
			},{
			text : '搜索',
			id : 'btnSearchForTable',
			iconCls : 'btn_search',
			handler : function(){
				var gs = tableBasicGrid.getStore();
				gs.baseParams['regionId'] = '';
				gs.baseParams['alias'] = Ext.getCmp('numSearchForTableAlias').getValue();
				gs.baseParams['name'] = Ext.getCmp('txtSearchForTableName').getValue();
				gs.load({
					params : {
						start : 0,
						limit : tableBasicGrid.getBottomToolbar().pageSize
					}
				});
			}
		}]
	});
	tableBasicGrid = createGridPanel(
		'tableBasicGrid',
		'餐台管理',
		'',
		'',
		'../../QueryTable.do',
		[
			[false, false, false, true], 
			['餐台编号', 'alias'],
			['区域', 'region.name'],
			['餐台名称', 'name'],
			['最低消费', 'minimumCost',,'right', 'Ext.ux.txtFormat.gridDou'],
			['服务费率', 'serviceRate',,'right', 'Ext.ux.txtFormat.gridDou'],
			['就餐人数', 'customNum',,'right'],
			['餐台状态', 'statusText',,'center'],
			['操作', 'operate', 200 ,'center', 'tableBasicGridOperateRenderer']
		],
		TableRecord.getKeys(),
		[['isPaging', true], ['restaurantID', restaurantID],  ['dataSource', 'normal']],
		GRID_PADDING_LIMIT_20,
		'',
		tableBasicGridTbar
	);	
	tableBasicGrid.region = 'center';
	tableBasicGrid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp('btnSearchForTable').handler();
		}
	}];
	
	tableBasicGrid.on('render', function(){
		Ext.getCmp('btnSearchForTable').handler();
	});
	tableBasicGrid.on('rowdblclick', function(){
		updateTableBasicHandler();
	});
	
	
}
Ext.onReady(function() {
	initTasteGrid();
//	initGrid();
	initTasteOperatorWin();
	initTasteCateOperatorWin();
	tmm_tasteTree = new Ext.tree.TreePanel({
		title : '口味分组',
		id : 'tmm_tasteTree',
		region : 'west',
		width : 200,
		rootVisible : true,
		border : true,
		frame : true,
//		enableDD : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryTasteCate.do',
			baseParams : {
				dataSource : 'tree'
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部',
			tasteCateName : '全部',
			leaf : false,
			id : -1
		}),
		tbar : [ '->', {
			text : '添加',
			iconCls : 'btn_add',
			handler : function(e) {
				tasteCateOperateHandler({otype : 'insert'});
			}
		},{
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(e) {
				tasteCateOperateHandler({otype : 'update'});
			}
		},{
			text : '删除',
			iconCls : 'btn_delete',
			handler : function(e) {
				tasteCateOperateHandler({otype : 'delete'});
			}
		}],
		listeners : {
			load : function(){
				var nodes = tmm_tasteTree.getRootNode().childNodes;
				tasteTypeData = [];
				for (var i = 0; i < nodes.length; i++) {
					if(nodes[i].attributes.status == 1){
						nodes[i].setText('<font color=\"#808080\">' + nodes[i].attributes.tasteCateName + '&nbsp;(系统保留)</font>');
					}else{
						tasteTypeData.push([nodes[i].id, nodes[i].attributes.tasteCateName]);
					}	
				}
				Ext.getCmp('comboTasteCate').store.loadData(tasteTypeData);
			},
			click : function(e){
				Ext.getCmp('btnSerachForTasteBasic').handler();
				Ext.getDom('lblTasteCateName').innerHTML = e.attributes.tasteCateName;
				if(e.attributes.tasteCateName == '规格'){
					tastem_add = false;
				}else{
					tastem_add = true;
				}
			}
		}
	});
	
	new Ext.Panel({
//		title : '口味管理',
		renderTo : 'divTaste',
		layout : 'border',
		frame : true,
		width : parseInt(Ext.getDom('divTaste').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divTaste').parentElement.style.height.replace(/px/g,'')),
		items : [tmm_tasteTree, tasteGrid],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){	
				Ext.getCmp('btnSerachForTasteBasic').handler(); 
			}
		}]
	});
});

var bar = {treeId : 'tmm_tasteTree', option :[{name : '修改', fn : "tasteCateOperateHandler({otype:'update'})"}, {name : '删除', fn : "tasteCateOperateHandler({otype:'delete'})"}]};
showFloatOption(bar);

/**
 * 
 */
function tasteDeleteHandler() {
	var data = Ext.ux.getSelData(tasteGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条数据再进行操作.');
		return;
	}
	Ext.MessageBox.show({
		msg : '是否删除: ' + data['name'],
		buttons : Ext.MessageBox.YESNO,
		icon: Ext.MessageBox.QUESTION,
		fn : function(btn) {
			if (btn == 'yes') {
				Ext.Ajax.request({
					url : '../../OperateTaste.do',
					params : {
						'dataSource' : 'delete',
						id : data['id']
					},
					success : function(response, options) {
						var jr = Ext.decode(response.responseText);
						if (jr.success == true) {							
							Ext.example.msg('提示', String.format(Ext.ux.txtFormat.deleteSuccess, data['name']));
							Ext.getCmp('btnSerachForTasteBasic').handler();
						} else {
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(response, options) {
						Ext.ux.showMsg(Ext.decode(response.responseText));
					}
				});
			}
		}
	});
};


function tasteCateOperateHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.otype == 'undefined'){
		return;
	}
	var cateId = Ext.getCmp('hideTasteCateId');
	var cateName = Ext.getCmp('txtTasteCateName');
	
	if(c.otype == 'insert'){
		tasteCateOperatorWin.otype = 'insert';
		tasteCateOperatorWin.show();
		tasteCateOperatorWin.center();
		tasteCateOperatorWin.setTitle("添加新口味类型");
		cateId.setValue();
		cateName.setValue();
		cateName.clearInvalid();
		cateName.focus();
	}else if(c.otype == 'update'){
		var tn = Ext.ux.getSelNode(tmm_tasteTree);
		if(!tn || tn.id == -1){
			Ext.example.msg('提示', '操作失败, 请选中一条数据再进行操作.');
			return;
		}else{
			if(tn.attributes.status == 1){
				Ext.example.msg('提示', '系统保留,不能修改.');
				return;
			}
		}
		
		tasteCateOperatorWin.otype = 'update';
		tasteCateOperatorWin.show();
		tasteCateOperatorWin.center();
		tasteCateOperatorWin.setTitle("修改口味类型");
		cateId.setValue(tn.id);
		cateName.setValue(tn.attributes.tasteCateName);
		cateName.focus();
	}else if(c.otype == 'delete'){
		var tn = Ext.ux.getSelNode(tmm_tasteTree);
		if(!tn){
			Ext.example.msg('提示', '操作失败, 请选中一条数据再进行操作.');
			return;
		}else{
			if(tn.attributes.status == 1){
				Ext.example.msg('提示', '系统保留,不能删除.');
				return;
			}
			Ext.Msg.confirm(
				'提示',
				'是否删除: ' + tn.text,
				function(e){
					if(e == 'yes'){
						Ext.Ajax.request({
							url : '../../OperateTasteCate.do',
							params : {
								dataSource : 'delete',
								categoryId : tn.id
							},
							success : function(res, opt){
								var jr = Ext.util.JSON.decode(res.responseText);
								if(jr.success){
									Ext.example.msg(jr.title, String.format(Ext.ux.txtFormat.deleteSuccess, tn.text));
									tmm_tasteTree.getRootNode().reload();
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
	}
}

function tasteInsertHandler(){
	if(!tastem_add){
		Ext.example.msg('提示', '操作失败, 规格不能添加.');
		return;
	}
	tasteOperatorWin.otype = Ext.ux.otype['insert'];
	tasteOperatorWin.show();
	
	tasteOperatorWin.setTitle("添加新口味信息");
	operatorWinData({
		otype : Ext.ux.otype['set'],
		data : {}
	});
	tasteOperatorWin.center();
}
function tasteUpdateHandler(){
	var data = Ext.ux.getSelData(tasteGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条数据再进行操作.');
		return;
	}
	tasteOperatorWin.otype = Ext.ux.otype['update'];
	tasteOperatorWin.show();
	
	tasteOperatorWin.setTitle("修改口味信息 -- " + data['name']);
	operatorWinData({
		otype : Ext.ux.otype['set'],
		data : data
	});
	tasteOperatorWin.center();
}

function operatorWinData(c){
	if(c == null || typeof c == 'undefined' || typeof c.otype == 'undefined'){
		return;
	}
	var tasteId = Ext.getCmp('hideTasteId');
	var tasteName = Ext.getCmp('txtTasteName');
	var tastePrice = Ext.getCmp('numTastePrice');
	var tasteCate = Ext.getCmp('comboTasteCate');
	var tasteRate = document.getElementsByName('tasteRate');
	
	if(c.otype == Ext.ux.otype['set']){
		tasteId.setValue(c.data['id']);
		tasteName.setValue(c.data['name']);
		tasteCate.setValue(typeof c.data['cateValue'] == 'undefined' ? (Ext.ux.getSelNode(tmm_tasteTree)?(Ext.ux.getSelNode(tmm_tasteTree).id != '-1'?Ext.ux.getSelNode(tmm_tasteTree).id:'') : '') : c.data['cateValue']);
		tasteCate.fireEvent('select', tasteCate);
		if(c.data['typeValue'] == 1){
			tasteCate.getEl().up('.x-form-item').setDisplayed(false);
		}else{
			tasteCate.getEl().up('.x-form-item').setDisplayed(true);
		}
		tastePrice.setValue(typeof c.data['price'] == 'undefined' ? 0 : c.data['price']);
		
		if(typeof c.data['rate'] == 'undefined'){
			document.getElementById('rdoFifty').checked = true;
		}else{
			document.getElementById('radioTasteRate').checked = true;
			Ext.getCmp('radioTasteRate').fireEvent('check', Ext.getCmp('radioTasteRate'), true);
			Ext.getCmp('numTasteRateOther').setValue(c.data['rate'] * 100);
		}
		
		tasteName.clearInvalid();
		tastePrice.clearInvalid();
		tasteCate.clearInvalid();
		tasteName.focus();
	}else if(c.otype == Ext.ux.otype['get']){
		c.data = {
			id : tasteId.getValue(),
			name : tasteName.getValue(),
			price : tastePrice.getValue(),
			rate : tasteRate.getValue(),
			cateValue : tasteCate.getValue()
		};
		return data;
	}
}
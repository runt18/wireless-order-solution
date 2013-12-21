String.prototype.trim=function() {  
    return this.replace(/(^\s*)|(\s*$)/g,'');  
};  
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
			var tasteTypeComb = Ext.getCmp('comboSearchForTasteType');
			
			if(index == 0){
				// 全部
				oCombo.setVisible(false);
				ct.setVisible(false);
				cn.setVisible(false);
				tasteTypeComb.setVisible(false);
				conditionType = '';
			}else if(index == 1){
				oCombo.setVisible(true);
				ct.setVisible(false);
				cn.setVisible(true);
				tasteTypeComb.setVisible(false);
				oCombo.setValue(1);
				cn.setValue();
				conditionType = cn.getId();
			}else if(index == 2){
				oCombo.setVisible(false);
				ct.setVisible(true);
				cn.setVisible(false);
				tasteTypeComb.setVisible(false);
				ct.setValue();
				conditionType = ct.getId();
			}else if(index == 3){
				oCombo.setVisible(false);
				ct.setVisible(false);
				cn.setVisible(false);
				tasteTypeComb.setVisible(true);
				
				tasteTypeComb.setValue(tasteTypeData[0][0]);
				conditionType = tasteTypeComb.getId();
			}
			
		}
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

function initTasteGrid(){
	var tasteGridTbar = new Ext.Toolbar({
		height : 26,
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
	    	value : 1,
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
	    	allowBlank : false
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
			style: 'text-align: left;',
			hidden : true,
			width : 120
		},  {
			xtype : 'combo',
			id : 'comboSearchForTasteType',
			hidden : true,
			forceSelection : true,
			width : 120,
			value : "规格",
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
			allowBlank : false
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
			handler : function(nodeId){
				var oCombo = Ext.getCmp('operator');
				var st = Ext.getCmp('txtSearchForTextField');
				var sn = Ext.getCmp('txtSearchForNumberField');
//				var cate = Ext.getCmp('comboSearchForTasteType');
				var gs = tasteGrid.getStore();
				gs.baseParams['ope'] = oCombo.getValue();
				
				gs.baseParams['alias'] = taste_filterTypeComb.getValue() == 1 ? sn.getValue() : '';
				gs.baseParams['price'] = taste_filterTypeComb.getValue() == 2 ? sn.getValue() : '';
				gs.baseParams['name'] = taste_filterTypeComb.getValue() == 3 ? st.getValue() : '';
				gs.baseParams['cate'] = nodeId?nodeId : '';					
				
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
		'',
		'',
		'',
		'',
		'../../QueryTaste.do',
		[
			[true, false, false, true], 
			['名称', 'name'],
			['价格', 'price',,'right','Ext.ux.txtFormat.gridDou'],
			['比例', 'rate',,'right','Ext.ux.txtFormat.gridDou'],
			['计算方式', 'calcText',,'center'],
			['类型', 'cateText',,'center'],
			['操作', '',,'center','tasteGridRenderer']
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
	
	tmm_tasteTree = new Ext.tree.TreePanel({
		title : '口味分组',
		id : 'tmm_tasteTree',
		region : 'west',
		width : 200,
		rootVisible : true,
		border : true,
		autoScroll : true,
		frame : true,
		enableDD : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部',
			id : -1,
			loader : new Ext.tree.TreeLoader({
				dataUrl : '../../QueryTasteCate.do',
				baseParams : {
					dataSource : 'tree'
				}
			})
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
				tastem_selectedId = e.id;
				Ext.getCmp('btnSerachForTasteBasic').handler(e.id);
				
				if(e.attributes.status == 1){
					tastem_add = false;
					Ext.getDom('lblTasteCateName').innerHTML = e.attributes.tasteCateName;
				}else{
					tastem_add = true;
					Ext.getDom('lblTasteCateName').innerHTML = e.text;
				}
			},
			enddrag : function(t,n,e){
				var cateB;
				if(n.nextSibling != null){
					cateB = n.nextSibling.id;
				}else{
					cateB = n.previousSibling.id;
				}
				Ext.Ajax.request({
					url : '../../OperateTasteCate.do',
					params : {
						cateA : n.id,
						cateB : cateB,
						dataSource : 'swap'
					},
					success : function(res, opt){
						tmm_tasteTree.getRootNode().reload();
					},
					failure : function(res, opt){
						Ext.ux.show(Ext.decode(res.responseText));
					}
				});
				
			}
		}
	});
	
}

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
					if(!tasteCateName.isValid() || !tasteCateName.getValue().trim()){
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
			width : 280,
			closeAction : 'hide',
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
					id : 'txtTasteName',
					allowBlank : false,
					width : 160,
					listeners : {
						focus : function(e){
							e.focus(true, 100);
						}
					}
				}, {
					xtype : 'combo',
					fieldLabel : '类型',
					forceSelection : true,
					width : 160,
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
					readOnly : true,
					listeners : {
						select : function(e){
							var tastePrice = Ext.getCmp('numTastePrice');
							var tasteRate = Ext.getCmp('numTasteRate');
							var displayCalc = Ext.getCmp('txtDisplayCalc');
							if(e.getEl().dom.value == '规格'){
								tastePrice.setDisabled(true);
								tasteRate.setDisabled(false);
								tastePrice.setValue(0);
								displayCalc.setValue('按比例');
								
							}else{
								tastePrice.setDisabled(false);
								tasteRate.setDisabled(true);
								tasteRate.setValue(0);
								displayCalc.setValue('按价格');
							}
						},
						render : function(e){
							e.store.loadData(tasteTypeData);
						}
					}
				}, {
					xtype : 'numberfield',
					fieldLabel : '价格',
					id : 'numTastePrice',
					value : 0.00,
					allowBlank : false,
					width : 160
				}, {
					xtype : 'numberfield',
					fieldLabel : '比例',
					id : 'numTasteRate',
					value : 0.00,
					allowBlank : false,
					width : 160,
					validator : function(v) {
						if (v < 0.00 || v > 9.99) {
							return '比例范围是 0.00 至 9.99';
						} else {
							return true;
						}
					}
				}, {
					xtype : 'textfield',
					id : 'txtDisplayCalc',
					fieldLabel : '计算方式',
					readOnly : true,
					disabled : true,
					width : 160,
					value : '按价格'
				}, {
					xtype:'hidden',
					id:'hideTasteId'
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
					var tasteId = Ext.getCmp('hideTasteId');
					var tasteName = Ext.getCmp('txtTasteName');
					var tastePrice = Ext.getCmp('numTastePrice');
					var tasteRate = Ext.getCmp('numTasteRate');
					var tasteCate = Ext.getCmp('comboTasteCate');
					
					if(!tasteCate.isValid() || !tasteName.isValid() || !tasteName.getValue().trim()){
						return;
					}
					if(tasteCate.getValue() == 0){
						if(!tastePrice.isValid()){
							return;
						}
					}else if(tasteCate.getValue() == 2){
						if(!tasteRate.isValid()){
							return;
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
							rate : tasteRate.getValue(),
							cate : tasteCate.getValue()
						},
						success : function(res, opt) {
							btnSave.setDisabled(false);
							btnCancel.setDisabled(false);
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								tasteOperatorWin.hide();
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnSerachForTasteBasic').handler(tastem_selectedId);
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

Ext.onReady(function() {
	initTasteGrid();
	initTasteOperatorWin();
	initTasteCateOperatorWin();
	
	new Ext.Panel({
//		title : '口味管理',
		renderTo : 'divTaste',
		width : parseInt(Ext.getDom('divTaste').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divTaste').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',
		frame : true,
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

function showFloatOption(obj_b){
	//记录节点的位置和鼠标位置
	var nodex=0,x=0;
	var offset;
	//生成浮动bar
	for (var i = 0; i < obj_b.option.length; i++) {
		if(i > 0){
			$("#div_floatBar").append('|&nbsp;');
		}
		$("#div_floatBar").append('<a href="javascript:void(0)" onclick='+obj_b.option[i].fn+'>'+ obj_b.option[i].name +'</a>&nbsp;');
	}
	//把bar加到tree上
	$("#"+obj_b.treeId).mouseover(function(){
		$("#"+obj_b.treeId).find("li").find("li").mouseover(function(){
			tastem_nodeId = $(this).find("div").attr("ext:tree-node-id");
			offset = $(this).find("a").offset();
			nodex = offset.left-18;
			x = (offset.left+$(this).find("a").width()+100);
			$('#div_floatBar').css({left :offset.left+$(this).find("a").width(), top : (offset.top-2)});
			$('#div_floatBar').show();
		});
		
		$(document).mousemove(function(event){
			if(event.clientX > x || event.clientX < nodex){
				$('#div_floatBar').hide();
				tastem_nodeId ="";
			}
		});

	});
}
$(function(){
	var bar = {treeId : 'tmm_tasteTree', option :[{name : '修改', fn : "tasteCateOperateHandler({otype:'update'})"}, {name : '删除', fn : "tasteCateOperateHandler({otype:'delete'})"}]};
	showFloatOption(bar);
});

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
		msg : '确定删除?',
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
							Ext.example.msg('提示', jr.msg);
							Ext.getCmp('btnSerachForTasteBasic').handler(tastem_selectedId);
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
		var tn = tmm_tasteTree.getNodeById(tastem_nodeId==""?tastem_selectedId:tastem_nodeId);
		if(!tn){
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
		var tn = tmm_tasteTree.getNodeById(tastem_nodeId==""?tastem_selectedId:tastem_nodeId);
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
									Ext.example.msg(jr.title, '<font style="color:red">'+tn.text+'</font>&nbsp;删除成功');
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
	tasteOperatorWin.center();
	tasteOperatorWin.setTitle("添加新口味信息");
	operatorWinData({
		otype : Ext.ux.otype['set'],
		data : {}
	});
}
function tasteUpdateHandler(){
	var data = Ext.ux.getSelData(tasteGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条数据再进行操作.');
		return;
	}
	tasteOperatorWin.otype = Ext.ux.otype['update'];
	tasteOperatorWin.show();
	tasteOperatorWin.center();
	tasteOperatorWin.setTitle("修改口味信息 -- " + data['name']);
	operatorWinData({
		otype : Ext.ux.otype['set'],
		data : data
	});
}

function operatorWinData(c){
	if(c == null || typeof c == 'undefined' || typeof c.otype == 'undefined'){
		return;
	}
	var tasteId = Ext.getCmp('hideTasteId');
	var tasteName = Ext.getCmp('txtTasteName');
	var tastePrice = Ext.getCmp('numTastePrice');
	var tasteRate = Ext.getCmp('numTasteRate');
	var tasteCate = Ext.getCmp('comboTasteCate');
	
	if(c.otype == Ext.ux.otype['set']){
		tasteId.setValue(c.data['id']);
		tasteName.setValue(c.data['name']);
		tasteCate.setValue(typeof c.data['cateValue'] == 'undefined' ? tastem_selectedId : c.data['cateValue']);
		tasteCate.fireEvent('select', tasteCate);
		if(c.data['typeValue'] == 1){
			tasteCate.setDisabled(true);
			tasteCate.setValue('规格');
		}else{
			tasteCate.setDisabled(false);
		}
		tastePrice.setValue(typeof c.data['price'] == 'undefined' ? 0 : c.data['price']);
		tasteRate.setValue(typeof c.data['rate'] == 'undefined' ? 0 : c.data['rate']);
		
		tasteName.clearInvalid();
		tastePrice.clearInvalid();
		tasteRate.clearInvalid();
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
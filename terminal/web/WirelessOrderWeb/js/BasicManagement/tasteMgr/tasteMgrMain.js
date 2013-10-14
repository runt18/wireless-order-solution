//-----------------load
var filterTypeComb = new Ext.form.ComboBox({
	fieldLabel : '过滤',
	forceSelection : true,
	width : 100,
	value : 0,
	id : 'filter',
	store : new Ext.data.SimpleStore({
		fields : [ 'value', 'text' ],
		data : [[0, '全部'],[2, '价格'],[3, '名称'], [4, '类型']]
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
				tasteTypeComb.store.loadData(tasteTypeData);
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
			xtype:'tbtext',
			text:'过滤:'
		}, filterTypeComb, { 
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
			value : tasteTypeData[0][0],
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
		},'->', {
			text : '搜索',				
			id : 'btnSerachForTasteBasic',
			iconCls : 'btn_search',
			handler : function(){
				var oCombo = Ext.getCmp('operator');
				var st = Ext.getCmp('txtSearchForTextField');
				var sn = Ext.getCmp('txtSearchForNumberField');
				var cate = Ext.getCmp('comboSearchForTasteType');
				var gs = tasteGrid.getStore();
				gs.baseParams['ope'] = oCombo.getValue();
				
				gs.baseParams['alias'] = filterTypeComb.getValue() == 1 ? sn.getValue() : '';
				gs.baseParams['price'] = filterTypeComb.getValue() == 2 ? sn.getValue() : '';
				gs.baseParams['name'] = filterTypeComb.getValue() == 3 ? st.getValue() : '';
				gs.baseParams['cate'] = filterTypeComb.getValue() == 4 ? cate.getValue() : '';					
				
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
	tasteGrid.on('render', function(thiz){
		Ext.getCmp('btnSerachForTasteBasic').handler();
	});
	tasteGrid.on('rowdblclick', function(thiz){
		tasteUpdateHandler();
	});
	
}

function initTasteOperatorWin(){
	tasteOperatorWin = new Ext.Window({
		title : '添加',
		width : 260,
		closeAction : 'hide',
		closable : false,
		resizable : false,
		modal : true,
		items : [{
			layout : 'form',
			labelWidth : 60,
			border : false,
			frame : true,
			items : [{
				xtype:'hidden',
				id:'hideTasteId'
			}, 
			{
				xtype : 'textfield',
				fieldLabel : '名称',
				id : 'txtTasteName',
				allowBlank : false,
				width : 160
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
				xtype : 'combo',
				fieldLabel : '类型',
				forceSelection : true,
				width : 160,
				value : tasteTypeData[0][0],
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
						tastePrice.setValue(0);
						tasteRate.setValue(0);
						if(e.getValue() == 0){
							tastePrice.setDisabled(false);
							tasteRate.setDisabled(true);
							displayCalc.setValue('按价格');
						}else if(e.getValue() == 2){
							tastePrice.setDisabled(true);
							tasteRate.setDisabled(false);
							displayCalc.setValue('按比例');
						}
					}
				}
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
							Ext.getCmp('btnSerachForTasteBasic').handler();
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
	
	new Ext.Panel({
		title : '口味管理',
		renderTo : 'divTaste',
		width : parseInt(Ext.getDom('divTaste').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divTaste').parentElement.style.height.replace(/px/g,'')),
		layout : 'fit',
		frame : true,
		items : [tasteGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			    tasteAddBut
			]
		}),
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){	
				Ext.getCmp('btnSerachForTasteBasic').handler(); 
			}
		}]
	});
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

function tasteInsertHandler(){
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
		tasteCate.setValue(typeof c.data['cateValue'] == 'undefined' ? 0 : c.data['cateValue']);
		tasteCate.fireEvent('select', tasteCate);
		if(c.data['typeValue'] == 1){
			tasteCate.setDisabled(true);
		}else{
			tasteCate.setDisabled(false);
		}
		tastePrice.setValue(typeof c.data['price'] == 'undefined' ? 0 : c.data['price']);
		tasteRate.setValue(typeof c.data['rate'] == 'undefined' ? 0 : c.data['rate']);
		
		tasteName.clearInvalid();
		tastePrice.clearInvalid();
		tasteRate.clearInvalid();
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
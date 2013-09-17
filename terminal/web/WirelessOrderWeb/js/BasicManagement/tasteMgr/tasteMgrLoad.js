var filterTypeComb = new Ext.form.ComboBox({
	fieldLabel : '过滤',
	forceSelection : true,
	width : 100,
	value : 0,
	id : 'filter',
	store : new Ext.data.SimpleStore({
		fields : [ 'value', 'text' ],
		data : [[0, '全部'],[1, '编号'],[2, '价格'],[3, '名称'], [4, '类型']]
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
			}else if(index == 1 || index == 2){
				oCombo.setVisible(true);
				ct.setVisible(false);
				cn.setVisible(true);
				tasteTypeComb.setVisible(false);
				oCombo.setValue(1);
				cn.setValue();
				conditionType = cn.getId();
			}else if(index == 3){
				oCombo.setVisible(false);
				ct.setVisible(true);
				cn.setVisible(false);
				tasteTypeComb.setVisible(false);
				ct.setValue();
				conditionType = ct.getId();
			}else if(index == 4){
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
				xtype : 'numberfield',
				fieldLabel : '编号',
				id : 'numTasteAlias',
				allowBlank : false,
				width : 160,
				disabled : true,
				validator : function(v) {
					if (v < 1 || v > 65535 || eval(v.indexOf('.')) != -1) {
						return '自定口味编号范围在 1 至 65535 之间, 且为整数.';
					} else {
						return true;
					}
				}
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
				var tasteAlias = Ext.getCmp('numTasteAlias');
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

function tasteMgrOnLoad() {
	
};

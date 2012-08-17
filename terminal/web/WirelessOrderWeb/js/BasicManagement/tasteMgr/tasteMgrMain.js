﻿var operatorData = [[ '1', '等于' ], [ '2', '大于等于' ], [ '3', '小于等于' ]];
var filterTypeData = [[ '0', '全部' ], [ '1', '编号' ], [ '2', '名称' ], [ '3', '价格' ], [ '4', '类型' ]];

// ----------------- 添加口味  --------------------
// 計算方式　－－　添加
var calAddData = [ [ 0, '按价格' ], [ 1, '按比例' ] ];

var calAddStore = new Ext.data.SimpleStore({
	fields : [ 'value', 'text' ],
	data : calAddData
});

var calAddComb = new Ext.form.ComboBox({
	fieldLabel : '计算方式',
	forceSelection : true,
	width : 160,
	value : calAddData[0][0],
	id : 'calAddComb',
	store : calAddStore,
	valueField : 'value',
	displayField : 'text',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	allowBlank : false
});

// 類別 －－ 增加
var typeAddData = [ [ 0, '口味' ], [ 2, '规格' ] ];

var typeAddStore = new Ext.data.SimpleStore({
	fields : [ 'value', 'text' ],
	data : typeAddData
});

var typeAddComb = new Ext.form.ComboBox({
	fieldLabel : '类别',
	forceSelection : true,
	width : 160,
	value : typeAddData[0][0],
	id : 'typeAddComb',
	store : typeAddStore,
	valueField : 'value',
	displayField : 'text',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	allowBlank : false
});

tasteAddWin = new Ext.Window({
	layout : 'fit',
	title : '添加',
	width : 260,
	height : 235,
	closeAction : 'hide',
	closable : false,
	resizable : false,
	items : [ {
		layout : 'form',
		id : 'tasteAddForm',
		labelWidth : 60,
		border : false,
		frame : true,
		items : [ {
			xtype : 'numberfield',
			fieldLabel : '编号',
			id : 'tasteAddNumber',
			allowBlank : false,
			width : 160
		}, {
			xtype : 'textfield',
			fieldLabel : '名称',
			id : 'tasteAddName',
			allowBlank : false,
			width : 160
		}, {
			xtype : 'numberfield',
			fieldLabel : '价格',
			id : 'tasteAddPrice',
			value : 0.00,
			// allowBlank : false,
			width : 160
		}, {
			xtype : 'numberfield',
			fieldLabel : '比例',
			id : 'tasteAddRate',
			value : 0.00,
			// allowBlank : false,
			width : 160,
			validator : function(v) {
				if (v < 0.00 || v > 9.99) {
					return '比例范围是0.00至9.99！';
				} else {
					return true;
				}
			}
		}, calAddComb, typeAddComb ]
	} ],
	buttons : [
	    {
	    	text : '确定',
			handler : function() {
				if (Ext.getCmp('tasteAddNumber').isValid() && Ext.getCmp('tasteAddName').isValid()) {
					var tasteAddNumber = Ext.getCmp('tasteAddNumber').getValue();
						var tasteAddName = Ext.getCmp('tasteAddName').getValue();
						var tasteAddPrice = Ext.getCmp('tasteAddPrice').getValue();
						if (tasteAddPrice == '') {
							tasteAddPrice = 0;
						}
						var tasteAddRate = Ext.getCmp('tasteAddRate').getValue();
						if (tasteAddRate == '') {
							tasteAddRate = 0;
						}

						var calAdd = calAddComb.getValue();
						var typeAdd = typeAddComb.getValue();

						var isDuplicate = false;
						for ( var i = 0; i < tasteData.length; i++) {
							if (tasteAddNumber == tasteData[i].tasteAlias) {
								isDuplicate = true;
							}
						}

						if (!isDuplicate) {
							tasteAddWin.hide();
							isPrompt = false;

							Ext.Ajax.request({
								url : '../../InsertTaste.do',
								params : {
									'pin' : pin,
									'tasteNumber' : tasteAddNumber,
									'tasteName' : tasteAddName,
									'tastePrice' : tasteAddPrice,
									'tasteRate' : tasteAddRate,
									'cal' : calAdd,
									'type' : typeAdd
								},
								success : function(response, options) {
									var resultJSON = Ext.util.JSON.decode(response.responseText);
									if (resultJSON.success == true) {										
										tasteStore.load({
											params : {
												start : 0,
												limit : pageRecordCount
											}
										});

										Ext.example.msg('提示', resultJSON.data);
									} else {
										var dataInfo = resultJSON.data;
										Ext.MessageBox.show({
											msg : dataInfo,
											width : 300,
											buttons : Ext.MessageBox.OK
										});
									}
								},
								failure : function(response, options) {
									
								}
							});
						} else {
							Ext.MessageBox.show({
								msg : '该口味编号已存在！',
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						}

					}

				}
			}, {
				text : '取消',
				handler : function() {
					tasteAddWin.hide();
					isPrompt = false;
				}
			} ],
	listeners : {
		'show' : function(thiz) {

			Ext.getCmp('tasteAddNumber').setValue('');
			tasteAddWin.findById('tasteAddNumber').clearInvalid();

			Ext.getCmp('tasteAddName').setValue('');
			Ext.getCmp('tasteAddName').clearInvalid();

			Ext.getCmp('tasteAddPrice').setValue(0.00);
			Ext.getCmp('tasteAddRate').setValue(0.00);

			calAddComb.setValue(calAddData[0][0]);
			calAddComb.clearInvalid();

			typeAddComb.setValue(typeAddData[0][0]);
			typeAddComb.clearInvalid();

			var f = Ext.get('tasteAddNumber');
			f.focus.defer(100, f);

		}
	}
});

// --------------------------------------------------------------------------
var tasteAddBut = new Ext.ux.ImageButton({
	imgPath : '../../images/material_add.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加口味',
	handler : function(btn) {
		if (!isPrompt) {
			tasteAddWin.show();
			isPrompt = true;
		}
	}
});

// --
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn) {

		var isChange = false;
		tasteGrid.getStore().each(
				function(record) {
					if (record.isModified('tasteName') == true
							|| record.isModified('tastePrice') == true
							|| record.isModified('tasteRate') == true
							|| record.isModified('tasteCalc') == true
							|| record.isModified('tasteCategory') == true) {
						isChange = true;
					}
				});

		if (isChange) {
			Ext.MessageBox.show({
				msg : '修改尚未保存，是否确认返回？',
				width : 300,
				buttons : Ext.MessageBox.YESNO,
				fn : function(btn) {
					if (btn == 'yes') {
						location.href = 'BasicMgrProtal.html?restaurantID='
								+ restaurantID + '&pin=' + pin;
					}
				}
			});
		} else {
			location.href = 'BasicMgrProtal.html?restaurantID=' + restaurantID
					+ '&pin=' + pin;
		}

	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '登出',
	handler : function(btn) {
	}
});

var filterTypeComb = new Ext.form.ComboBox({
	fieldLabel : '过滤',
	forceSelection : true,
	width : 100,
	value : '全部',
	id : 'filter',
	store : new Ext.data.SimpleStore({
		fields : [ 'value', 'text' ],
		data : filterTypeData
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
			var ct = Ext.getCmp('conditionText');
			var cn = Ext.getCmp('conditionNumber');
			var tasteTypeComb = Ext.getCmp('tasteTypeComb');
			
			if(index == 0){
				// 全部
				oCombo.setVisible(false);
				ct.setVisible(false);
				cn.setVisible(false);
				tasteTypeComb.setVisible(false);
				conditionType = '';
			}else if(index == 1 || index == 3){
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
			}else if(index == 4){
				oCombo.setVisible(false);
				ct.setVisible(false);
				cn.setVisible(false);
				tasteTypeComb.setVisible(true);
				tasteTypeComb.store.loadData(typeAddData);
				tasteTypeComb.setValue(typeAddData[0][0]);
				conditionType = tasteTypeComb.getId();
			}
			
		}
	}
});

// operator function
function tasteDeleteHandler(rowIndex) {
	Ext.MessageBox.show({
		msg : '确定删除？',
		width : 300,
		buttons : Ext.MessageBox.YESNO,
		fn : function(btn) {
			if (btn == 'yes') {
				var tasteID = tasteStore.getAt(rowIndex).get('tasteID');

				Ext.Ajax.request({
					url : '../../DeleteTaste.do',
					params : {
						'pin' : pin,
						'tasteID' : tasteID
					},
					success : function(response, options) {
						var resultJSON = Ext.util.JSON.decode(response.responseText);
						if (resultJSON.success == true) {							
							tasteStore.load({
								params : {
									start : 0,
									limit : pageRecordCount
								}
							});
							Ext.example.msg('提示', resultJSON.data);
						} else {
							var dataInfo = resultJSON.data;
							Ext.MessageBox.show({
								msg : dataInfo,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						}
					},
					failure : function(response, options) {
					}
				});
			}
		}
	});
};

var calModStore = new Ext.data.SimpleStore({
	fields : [ 'value', 'text' ],
	data : calAddData
});

var calModComb = new Ext.form.ComboBox({
	// fieldLabel : '计算方式',
	forceSelection : true,
	width : 160,
	value : calAddData[0][0],
	id : 'calModComb',
	store : calModStore,
	valueField : 'value',
	displayField : 'text',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	allowBlank : false
});

var typeModStore = new Ext.data.SimpleStore({
	fields : [ 'value', 'text' ],
	data : typeAddData
});

var typeModComb = new Ext.form.ComboBox({
	// fieldLabel : '类别',
	forceSelection : true,
	width : 160,
	value : typeAddData[0][0],
	id : 'typeModComb',
	store : typeModStore,
	valueField : 'value',
	displayField : 'text',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	allowBlank : false
});

function tasteOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	return '<center>'
		   + '<a href=\"javascript:tasteDeleteHandler(' + rowIndex + ')\">删除</a>'
		   + '</center>';
};

// 1，表格的数据store
var tasteStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : '../../QueryTaste.do'
	}),
	reader : new Ext.data.JsonReader(Ext.ux.readConfig, [ {
		name : 'tasteID'
	}, {
		name : 'tasteAlias'
	}, {
		name : 'tasteName'
	}, {
		name : 'tastePrice'
	}, {
		name : 'tasteRate'
	}, {
		name : 'tasteCategory'
	}, {
		name : 'tasteCalc'
	}, {
		name : 'operator'
	}, {
		name : 'message'
	} ])
});

// 2，栏位模型
var tasteColumnModel = new Ext.grid.ColumnModel([ 
    new Ext.grid.RowNumberer(), 
    {
		header : '编号',
		sortable : true,
		dataIndex : 'tasteAlias',
		width : 80
	}, {
		header : '名称',
		sortable : true,
		dataIndex : 'tasteName',
		width : 100,
		editor : new Ext.form.TextField({
			// allowBlank : false,
			selectOnFocus : true,
			allowNegative : false
		})
	}, {
		header : '价格（￥）',
		sortable : true,
		dataIndex : 'tastePrice',
		width : 80,
		align : 'right',
		renderer :Ext.ux.txtFormat.gridDou,
		editor : new Ext.form.NumberField({
			selectOnFocus : true
		})
	}, {
		header : '比例',
		sortable : true,
		dataIndex : 'tasteRate',
		width : 80,
		align : 'right',
		renderer :Ext.ux.txtFormat.gridDou,
		editor : new Ext.form.NumberField({
			selectOnFocus : true
		})
	}, {
		header : '计算方式',
		sortable : true,
		dataIndex : 'tasteCalc',
		width : 100,
		editor : calModComb,
		renderer : function(value, cellmeta, record) {
			var calDesc = '';
			for ( var i = 0; i < calAddData.length; i++) {
				if (calAddData[i][0] == value) {
					calDesc = calAddData[i][1];
				}
			}
			return calDesc;
		}
	}, {
		header : '类型',
		sortable : true,
		dataIndex : 'tasteCategory',
		width : 100,
		editor : typeModComb,
		renderer : function(value, cellmeta, record) {
			var typeDesc = '';
			for ( var i = 0; i < typeAddData.length; i++) {
				if (typeAddData[i][0] == value) {
					typeDesc = typeAddData[i][1];
				}
			}
			return typeDesc;
		}
	}, {
		header : '<center>操作</center>',
		sortable : true,
		dataIndex : 'operator',
		width : 180,
		renderer : tasteOpt
	}
]);

// -------------- layout ---------------
var tasteGrid;
Ext.onReady(function() {
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();

	// ---------------------表格--------------------------
	tasteGrid = new Ext.grid.EditorGridPanel({
		xtype : 'grid',
		anchor : '99%',
		region : 'center',
		frame : true,
		trackMouseOver : true,
		margins : '0 0 0 0',
		ds : tasteStore,
		cm : tasteColumnModel,
		sm : new Ext.grid.RowSelectionModel({
			singleSelect : true
		}),
		viewConfig : {
			forceFit : true
		},
		listeners : {
			rowclick : function(thiz, rowIndex, e) {
				currRowIndex = rowIndex;
			}
		},
		tbar : [
//		    '->',
		    { xtype:'tbtext', text:'过滤:'},
		    filterTypeComb,
		    { xtype:'tbtext', text:'&nbsp;&nbsp;'},
		    {
		    	xtype : 'combo',
		    	hidden : true,
		    	forceSelection : true,
		    	width : 100,
		    	value : operatorData[0][0],
		    	id : 'operator',
		    	store : new Ext.data.SimpleStore({
		    		fields : [ 'value', 'text' ],
		    		data : operatorData
		    	}),
		    	valueField : 'value',
		    	displayField : 'text',
		    	typeAhead : true,
		    	mode : 'local',
		    	triggerAction : 'all',
		    	selectOnFocus : true,
		    	allowBlank : false
		    },
		    { xtype:'tbtext', text:'&nbsp;&nbsp;'},
		    {
				xtype : 'textfield',
				id : 'conditionText',
				hidden : true,
				width : 120
			}, 
			{
				xtype: 'numberfield',
				id : 'conditionNumber',
				style: 'text-align: left;',
				hidden : true,
				width : 120
			},
			{
				xtype : 'combo',
				hidden : true,
				forceSelection : true,
				width : 120,
				value : typeAddData[0][0],
				id : 'tasteTypeComb',
				store : new Ext.data.SimpleStore({
		    		fields : [ 'value', 'text' ]
		    	}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				allowBlank : false
			},
		    '->',
		    {
				text : '搜索',				
				iconCls : 'btn_search',
				id : 'btnSerach',
				handler : function(){
					tasteStore.load({
						params : {
							start : 0,
							limit : pageRecordCount
						}
					});
				}
		    },
		    { xtype:'tbtext', text:'&nbsp;'},
		    {
		    	text : '保存修改',
				tooltip : '保存修改',
				iconCls : 'btn_save',
				handler : function() {
					// 修改記錄格式:id field_separator name
					// field_separator phone field_separator contact
					// field_separator address record_separator id
					// field_separator name field_separator phone
					// field_separator contact field_separator
					// address
					var modfiedArr = [];
					tasteGrid.getStore().each(function(record){
						if (record.isModified('tasteName') == true
								|| record.isModified('tastePrice') == true
								|| record.isModified('tasteRate') == true
								|| record.isModified('tasteCalc') == true
								|| record.isModified('tasteCategory') == true) {
							modfiedArr.push(record.get('tasteID')
									+ ' field_separator '
									+ record.get('tasteName')
									+ ' field_separator '
									+ record.get('tastePrice')
									+ ' field_separator '
									+ record.get('tasteRate')
									+ ' field_separator '
									+ record.get('tasteCalc')
									+ ' field_separator '
									+ record.get('tasteCategory'));
						}
					});

					if (modfiedArr.length != 0) {
						var toolbar = tasteGrid.getBottomToolbar();
						currPageIndex = toolbar.readPage(toolbar.getPageData());

						var modTastes = '';
						for ( var i = 0; i < modfiedArr.length; i++) {
							modTastes = modTastes + modfiedArr[i] + ' record_separator ';
						}
						modTastes = modTastes.substring(0, modTastes.length - 18);
						
						Ext.Ajax.request({
							url : '../../UpdateTaste.do',
							params : {
								pin : pin,
								modTastes : modTastes
							},
							success : function(response, options) {
								var resultJSON = Ext.util.JSON.decode(response.responseText);
								if (resultJSON.success == true) {
									tasteStore.load({
										params : {
											start : 0,
											limit : pageRecordCount
										}
									});
									Ext.example.msg('提示', resultJSON.data);
								} else {
									var dataInfo = resultJSON.data;
									Ext.MessageBox.show({
										msg : dataInfo,
										width : 300,
										buttons : Ext.MessageBox.OK
									});
								}
							},
							failure : function(response, options) {
								
							}
						});
					}
				}
		    }
		],
		bbar : new Ext.PagingToolbar({
			pageSize : pageRecordCount,
			store : tasteStore,
			displayInfo : true,
			displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
			emptyMsg : '没有记录'
		}),
		autoScroll : true,
		loadMask : { msg : '数据加载中，请稍等...' },
		listeners : {
			render : function(thiz) {
				tasteStore.reload({
					params : {
						start : 0,
						limit : pageRecordCount
					}
				});
			}
		}
	});
	
	tasteGrid.getStore().on('beforeload', function() {
		
		var queryType = Ext.getCmp('filter').getValue();
		var searchValue = Ext.getCmp(conditionType);
		var queryOperator = 0, queryValue = '';
		
		if(queryType == '全部' || queryType == 0 || !searchValue || searchValue.getValue().toString().trim() == '' ){	
			queryType = 0;
			queryValue = '';
		}else{
			queryOperator = Ext.getCmp('operator').getValue();
			if (queryOperator == '等于') {
				queryOperator = 1;
			}
			queryValue = searchValue.getValue().toString().trim();
		}
		
		this.baseParams = {
			'pin' : pin,
			'type' : queryType,
			'ope' : queryOperator,
			'value' : queryValue,
			'isPaging' : true,
			'isCombo' : false
		};
	});
	
	tasteGrid.getStore().on('load', function() {
		if (tasteGrid.getStore().getTotalCount() != 0) {
			var msg = this.getAt(0).get('message');
			if (msg != 'normal') {
				Ext.MessageBox.show({
					msg : msg,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
				this.removeAll();
			} else {
				
			}
		}
	});
	
	var centerPanel = new Ext.Panel({
		title : '口味管理',
		region : 'center',
		layout : 'fit',
		frame : true,
		items : [tasteGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			    tasteAddBut,
			    { xtype:'tbtext', text : '&nbsp;&nbsp;&nbsp;&nbsp;' },
				'->', 
				pushBackBut, 
				{ xtype:'tbtext', text : '&nbsp;&nbsp;&nbsp;&nbsp;' }, 
				logOutBut 
			]
		}),
		keys : [
		    {
		    	key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){	
					Ext.getCmp('btnSerach').handler(); 
				}
			}
		],
	});
	
	new Ext.Viewport({
		layout : 'border',
		id : 'viewport',
		items : [
		    {
		    	region : 'north',
		    	bodyStyle : 'background-color:#A9D0F5',
				html : '<h4 style="padding:10px;font-size:150%;float:left;">无线点餐网页终端</h4><div id="optName" class="optName"></div>',
				height : 50,
				margins : '0 0 0 0'
			},
			centerPanel,
			{
				region : 'south',
				height : 30,
				layout : 'form',
				frame : true,
				border : false,
				html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
			}
		]
	});

});

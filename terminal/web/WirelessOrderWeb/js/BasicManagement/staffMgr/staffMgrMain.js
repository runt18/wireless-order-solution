var operatorData = [ [ '1', '等于' ], [ '2', '大于等于' ], [ '3', '小于等于' ] ];

// ----------------- 添加員工  --------------------
// 窗口 －－ 增加
staffAddWin = new Ext.Window({
	layout : 'fit',
	title : '添加员工',
	width : 260,
	height : 241,
	closeAction : 'hide',
	closalble : false,
	modal : true,
	resizable : false,
	items : [{
		layout : 'form',
		id : 'staffAddWin',
		labelWidth : 60,
		border : false,
		frame : true,
		items : [{
			xtype : 'numberfield',
			fieldLabel : '编号',
			id : 'staffAddNumber',
			allowBlank : false,
			width : 160
		}, {
			xtype : 'textfield',
			fieldLabel : '姓名',
			id : 'staffAddName',
			allowBlank : false,
			width : 160
		}, {
			xtype : 'numberfield',
			fieldLabel : '赠送额度',
			id : 'staffAddQuota',
			allowBlank : false,
			disabled : true,
			width : 160,
			validator : function(v) {
				if (v < 0.00 || v > 99999.99) {
					return '赠送额度范围是0.00至99999.99！';
				} else {
					return true;
				}
			}
		}, {
			xtype : 'checkbox',
			id : 'noQuotaLimitAdd',
			fieldLabel : '无限制',
			value : true,
			listeners : {
				'check' : function(thiz, checked) {
					if(checked){
						staffAddWin.findById('staffAddQuota').disable();
					}else{
						staffAddWin.findById('staffAddQuota').enable();
					}
				}
			}
		}, {
			xtype : 'textfield',
			inputType : 'password',
			fieldLabel : '密码',
			id : 'staffAddPwd',
			width : 160
		}, {
			xtype : 'textfield',
			inputType : 'password',
			fieldLabel : '确认密码',
			id : 'staffAddPwdConfirm',			
			width : 160
		}, {
			html : '<div style="margin-top:4px"><font id="errorMsgAdd" style="color:red;"> </font></div>'
		}]
	}],
	buttons : [{
		text : '确定',
		handler : function(){
			if (Ext.getCmp('staffAddNumber').isValid()
					&& Ext.getCmp('staffAddName').isValid()
					&& Ext.getCmp('staffAddQuota').isValid()) {

				var staffAddNumber = Ext.getCmp('staffAddNumber').getValue();
				var staffAddName = Ext.getCmp('staffAddName').getValue();
				var staffAddPwd = Ext.getCmp('staffAddPwd').getValue();
				var staffAddPwdCon = Ext.getCmp('staffAddPwdConfirm').getValue();
				var staffAddQuota = Ext.getCmp('staffAddQuota').getValue();
				
				if (staffAddQuota == null) {
					staffAddQuota = 0;
				}
				
				var isNoLimit = Ext.getCmp('noQuotaLimitAdd').getValue();
				if (isNoLimit == true) {
					staffAddQuota = -1;
				}
				
				var isDuplicate = false;
				for ( var i = 0; i < staffData.length; i++) {
					if(staffAddNumber == staffData[i].staffAlias){
						isDuplicate = true;
					}
				}
				
				if(!isDuplicate){
					if(staffAddPwd == staffAddPwdCon){
						staffAddWin.hide();
						
						Ext.Ajax.request({
							url : '../../InsertStaff.do',
							params : {
								'pin' : pin,
								'staffNumber' : staffAddNumber,
								'staffName' : staffAddName,
								'staffPwd' : staffAddPwd,
								'staffQuota' : staffAddQuota
							},
							success : function(response, options) {
								var resultJSON = Ext.util.JSON.decode(response.responseText);
								if(resultJSON.success == true){
//									loadAllStaff();
									staffStore.load({
										params : {
											start : 0,
											limit : pageRecordCount
										}
									});
									
									var dataInfo = resultJSON.data;
									Ext.MessageBox.show({
										msg : dataInfo,
										width : 300,
										buttons : Ext.MessageBox.OK
									});
								}else{
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
					}else{
						Ext.getDom('errorMsgAdd').innerHTML = '确认密码不一致';
					}
				}else{
					Ext.MessageBox.show({
						msg : '该员工编号已存在！',
						width : 300,
						buttons : Ext.MessageBox.OK
					});
				}
			}
		}
	}, {
		text : '取消',
		handler : function(){
			staffAddWin.hide();
		}
	}],
	listeners : {
		'show' : function(thiz){
			Ext.getCmp('staffAddNumber').setValue('');
			Ext.getCmp('staffAddNumber').clearInvalid();

			Ext.getCmp('staffAddName').setValue('');
			Ext.getCmp('staffAddName').clearInvalid();

			Ext.getCmp('staffAddPwd').setValue('');
			Ext.getCmp('staffAddPwd').clearInvalid();

			Ext.getCmp('staffAddPwdConfirm').setValue('');
			Ext.getCmp('staffAddPwdConfirm').clearInvalid();

			Ext.getCmp('staffAddQuota').setValue(0);
			Ext.getCmp('staffAddQuota').clearInvalid();
			Ext.getCmp('staffAddQuota').disable();

			Ext.getCmp('noQuotaLimitAdd').setValue(true);

			Ext.getDom('errorMsgAdd').innerHTML = ' ';

			var f = Ext.get('staffAddNumber');
			f.focus.defer(100, f); 
		}
	}
});

var changePwdWin = new Ext.Window({
	layout : 'fit',
	title : '重置密码',
	width : 265,
	height : 150,
	closeAction : 'hide',
	closable : false,
	resizable : false,
	modal : true,
	items : [{
		layout : 'form',
		id : 'changePwdWin',
		labelWidth : 70,
		border : false,
		frame : true,
		items : [{
			xtype : 'textfield',
			inputType : 'password',
			fieldLabel : '原密码',
			id : 'origPwdCP',
			width : 160
		}, {
			xtype : 'textfield',
			inputType : 'password',
			fieldLabel : '新密码',
			id : 'newPwdCP',
			width : 160
		}, {
			xtype : 'textfield',
			inputType : 'password',
			fieldLabel : '确认新密码',
			id : 'confirmNewPwdCP',
			width : 160
		}, {
			html : '<div style="margin-top:4px"><font id="errorMsgChangePwd" style="color:red;"> </font></div>'
		}]
	}],
	bbar : [
		'->',
		{
			text : '保存',
	    	id : 'btnSaveUpdatePassWord',
	    	iconCls : 'btn_save',
			handler : function() {
				var origPwd = Ext.getCmp('origPwdCP').getValue();
				var newPwd = Ext.getCmp('newPwdCP').getValue();
				var confirmPwd = Ext.getCmp('confirmNewPwdCP').getValue();
				
				var staffID = staffStore.getAt(currRowIndex).get('staffID');
				var password = staffStore.getAt(currRowIndex).get('staffPassword');
				
				if(password == MD5(origPwd)){
					if (newPwd == confirmPwd){
						changePwdWin.hide();
						
						Ext.Ajax.request({
							url : '../../ResetStaffPassword.do',
							params : {
								'pin' : pin,
								'staffID' : staffID,
								'newPwd' : hex_md5(newPwd)
							},
							success : function(response, options) {
								var resultJSON = Ext.util.JSON.decode(response.responseText);
								if (resultJSON.success == true) {
									staffStore.load({
										params : {
											start : 0,
											limit : pageRecordCount
										}
									});
									
									var dataInfo = resultJSON.data;
									Ext.MessageBox.show({
										msg : dataInfo,
										width : 300,
										buttons : Ext.MessageBox.OK
									});
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
					Ext.example.msg('提示', '操作失败, 两次密码已一致, 请重新输入.');
				}
			} else {
				Ext.example.msg('提示', '操作失败, 原密码不正确, 请重新输入.');
			}
		}
	}, {
		text : '关闭',
		id : 'btnCloseUpdatePassWord',
		iconCls : 'btn_close',
		handler : function() {
			changePwdWin.hide();
		}
	}
	],
	listeners : {
		'show' : function(thiz) {
			Ext.getCmp('origPwdCP').setValue('');
			Ext.getCmp('newPwdCP').setValue('');
			Ext.getCmp('confirmNewPwdCP').setValue('');

			Ext.getDom('errorMsgChangePwd').innerHTML = ' ';

			var f = Ext.get('origPwdCP');
			f.focus.defer(100, f); // 为什么这样才可以！？！？
		}
	}
});

// --------------------------------------------------------------------------
var staffAddBut = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddForBigBar.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加员工',
	handler : function(btn) {
		staffAddWin.show();
		staffAddWin.center();
	}
});

// --
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn){
		var isChange = false;
		staffGrid.getStore().each(function(record) {
			if (record.isModified('staffName') == true || record.isModified('staffQuota') == true) {
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
						location.href = 'BasicMgrProtal.html?restaurantID=' + restaurantID + '&pin=' + pin;
					}
				}
			});
		} else {
			location.href = 'BasicMgrProtal.html?restaurantID=' + restaurantID + '&pin=' + pin;
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

// ----------------- dymatic searchForm -----------------
var filterTypeData = [ [ '0', '全部' ], [ '1', '编号' ], [ '2', '姓名' ] ];
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
	readOnly : true,
	listeners : {
		select : function(combo, record, index) {
			
			var oCombo = Ext.getCmp('operator');
			var ct = Ext.getCmp('conditionText');
			var cn = Ext.getCmp('conditionNumber');
			
			if(index == 0){
				oCombo.setVisible(false);
				ct.setVisible(false);
				cn.setVisible(false);
				conditionType = '';
			}else if(index == 1){
				oCombo.setVisible(true);
				cn.setVisible(true);
				ct.setVisible(false);
				oCombo.setValue(1);
				cn.setValue();
				conditionType = cn.getId();
			}else if(index == 2){
				oCombo.setVisible(false);
				cn.setVisible(false);
				ct.setVisible(true);
				cn.setValue();
				conditionType = ct.getId();
			}
			
		}
	}
});




var searchForm = new Ext.Panel({
	border : false,
	width : 130,
	id : 'searchForm',
	items : [{
		xtype : 'textfield',
		hideLabel : true,
		id : 'conditionText',
		allowBlank : false,
		width : 120
	}]
});

// operator function
function staffDeleteHandler(rowIndex){
	Ext.MessageBox.show({
		msg : '确定删除？',
		width : 300,
		buttons : Ext.MessageBox.YESNO,
		fn : function(btn) {
			if (btn == 'yes') {
				var staffID = staffStore.getAt(rowIndex).get('staffID');

				Ext.Ajax.request({
					url : '../../DeleteStaff.do',
					params : {
						'pin' : pin,
						'staffID' : staffID
					},
					success : function(response, options) {
						var resultJSON = Ext.util.JSON.decode(response.responseText);
						if (resultJSON.success == true) {
//							loadAllStaff();
							staffStore.load({
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
						var resultJSON = Ext.util.JSON.decode(response.responseText);
						Ext.MessageBox.show({
							msg : resultJSON.data,
							width : 300,
							buttons : Ext.MessageBox.OK
						});
					}
				});
			}
		}
	});
};

function changePwdHandler(rowIndex) {
	changePwdWin.show();
};

function staffOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	if(eval(record.get('type') == 1)){
		return '系统保留';
	}else{
		return ''
		+ '<a href="javascript:staffDeleteHandler(' + rowIndex + ')">删除</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		+ '<a href="javascript:changePwdHandler(' + rowIndex + ')">重置密码</a>'
		+ '';
	}
};

// 1，表格的数据store
var staffStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : '../../QueryStaff.do'
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : 'totalProperty',
		root : 'root'
	}, [ {
		name : 'staffID'
	}, {
		name : 'staffAlias'
	}, {
		name : 'staffName'
	}, {
		name : 'staffPassword'
	}, {
		name : 'terminalID'
	}, {
		name : 'staffGift'
	}, {
		name : 'staffQuota'
	}, {
		name : 'quotaOrig'
	}, {
		name : 'noLimit'
	}, {
		name : 'operator'
	}, {
		name : 'message'
	}, {
		name : 'type'
	} ])
});

var noLimitCheckColumn = new Ext.grid.CheckColumn({
	header : '是否限制额度',
	dataIndex : 'noLimit',
	align : 'center',
	width : 60,
	renderer : function(v, p, record) {
		if(eval(record.get('type') == 1)){
			return '系统保留';
		}else{
			p.css += ' x-grid3-check-col-td';
			return '<div id="' + this.id + '" class="x-grid3-check-col'
					+ (v ? '-on' : '') + '">&#160;</div>';
		}
	}
});

// 2，栏位模型
var staffColumnModel = new Ext.grid.ColumnModel([ new Ext.grid.RowNumberer(), {
	header : '编号',
	sortable : true,
	dataIndex : 'staffAlias',
	width : 80
}, {
	header : '名称',
	sortable : true,
	dataIndex : 'staffName',
	width : 100,
	editor : new Ext.form.TextField({
		allowBlank : false,
		allowNegative : false,
		selectOnFocus : true
	})
}, {
	header : '已赠送（￥）',
	sortable : true,
	dataIndex : 'staffGift',
	width : 100
}, {
	header : '赠送额度（￥）',
	sortable : true,
	dataIndex : 'staffQuota',
	width : 100,
	editor : new Ext.form.NumberField({
		allowBlank : false,
		selectOnFocus : true,
		validator : function(v) {
			if (v < 0.00 || v > 99999.99) {
				return '赠送额度范围是0.00至99999.99！';
			} else {
				return true;
			}
		}
	}),
	renderer : function(v, params, record) {
		if (v < 0) {
			return '无限制';
		} else {
			return v;
		}
	}
},
noLimitCheckColumn, 
{
	header : '操作',
	dataIndex : 'operator',
	width : 150,
	align : 'center',
	renderer : staffOpt
} ]);

// -------------- layout ---------------
var staffGrid;
Ext.onReady(function() {
	// 解决ext中文传入后台变问号问题
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();

	// ---------------------表格--------------------------
	staffGrid = new Ext.grid.EditorGridPanel({
		// title : '员工',
		xtype : 'grid',
		anchor : '100%',
		region : 'center',
		frame : true,
		margins : '0 0 0 0',
		ds : staffStore,
		cm : staffColumnModel,
		plugins : noLimitCheckColumn,
		clicksToEdit : 2,
		trackMouseOver : true,
		sm : new Ext.grid.RowSelectionModel({
			singleSelect : true
		}),
		viewConfig : {
			forceFit : true
		},
		listeners : {
			'rowclick' : function(thiz, rowIndex, e) {
				currRowIndex = rowIndex;
			},
			'render' : function(thiz) {
				// alert('here');
				staffStore.load({
					params : {
						start : 0,
						limit : pageRecordCount
					}
				});
			},
			celldblclick : function(thiz, rowIndex, columIndex, e){
				var record = thiz.getStore().getAt(rowIndex);
				if(record.get('type') == 1){
					Ext.example.msg('提示','系统保留用户为<font color="red">管理员</font>,不允许修改任何信息.');
					return false;
				}
			}
		},
		tbar : [
		    { xtype:'tbtext', text:'过滤:'},
		    { xtype:'tbtext', text:'&nbsp;&nbsp;'},
		    filterTypeComb,
		    { xtype:'tbtext', text:'&nbsp;&nbsp;'},
		    {
		    	xtype : 'combo',
		    	hideLabel : true,
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
		    	allowBlank : false,
		    	hidden : true,
		    	readOnly : true
		    }, 
		    { xtype:'tbtext', text:'&nbsp;&nbsp;'},
		    {
				xtype : 'numberfield',
				hideLabel : true,
				id : 'conditionNumber',
				width : 120,
				hidden : true
			}, 
			{ xtype:'tbtext', text:'&nbsp;&nbsp;'},
			{
		    	xtype : 'textfield',
				hideLabel : true,
				id : 'conditionText',
				width : 120,
				hidden : true
			},
		    '->',
			{
				text : '搜索',				
				iconCls : 'btn_search',
				id : 'btnSerach',
				handler : function(){
					staffStore.load({
						params : {
							start : 0,
							limit : pageRecordCount
						}
					});
				}
			}, {
				text : '保存修改',
				iconCls : 'btn_save',
				handler : function() {
					// 修改記錄格式:id field_separator name
					// field_separator phone field_separator contact
					// field_separator address record_separator id
					// field_separator name field_separator phone
					// field_separator contact field_separator
					// address
					var modfiedArr = [];
					staffGrid.getStore().each(function(record) {
						if (record.isModified('staffName') == true || record.isModified('staffQuota') == true) {
							modfiedArr.push(record.get('staffID')
									+ ' field_separator '
									+ record.get('terminalID')
									+ ' field_separator '
									+ record.get('staffName')
									+ ' field_separator '
									+ record.get('staffQuota'));
						}
					});
					
					if (modfiedArr.length != 0) {					
						var toolbar = staffGrid.getBottomToolbar();
						currPageIndex = toolbar.readPage(toolbar.getPageData());
						
						var modStaffs = '';
					for ( var i = 0; i < modfiedArr.length; i++) {
						modStaffs = modStaffs + modfiedArr[i] + ' record_separator ';
					}
					modStaffs = modStaffs.substring(0, modStaffs.length - 18);
					
					Ext.Ajax.request({
						url : '../../UpdateStaff.do',
						params : {
							'pin' : pin,
							'modStaffs' : modStaffs
						},
						success : function(response, options) {
							var resultJSON = Ext.util.JSON.decode(response.responseText);
							if (resultJSON.success == true) {
								staffStore.load({
									params : {
										start : (currPageIndex - 1) * pageRecordCount,
										limit : pageRecordCount
									}
								});
								Ext.example.msg('提示', resultJSON.data);
							}else{
								var dataInfo = resultJSON.data;
								Ext.MessageBox.show({
									msg : dataInfo,
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						},
						failure : function(response, options) {
							var resultJSON = Ext.util.JSON.decode(response.responseText);
							Ext.MessageBox.show({
								msg : resultJSON.data,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						}
					});
				}
			}
		}
		],
		bbar : new Ext.PagingToolbar({
			pageSize : pageRecordCount,
			store : staffStore,
			displayInfo : true,
			displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
			emptyMsg : '没有记录'
		}),
		autoScroll : true,
		loadMask : { msg : '数据加载中，请稍等...' }
	});
	
	staffGrid.getStore().on('beforeload', function() {
		
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
			'restaurantID' : restaurantID,
			'type' : queryType,
			'ope' : queryOperator,
			'value' : queryValue,
			'isPaging' : true,
			'isCombo' : false
		};
	});

	// 为store配置load监听器(即load完后动作)
	staffGrid.getStore().on('load', function() {
		if (staffGrid.getStore().getTotalCount() != 0) {
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

	staffGrid.on('beforeedit', function(e){
		if (e.record.get('noLimit') == true && e.field == 'staffQuota') {
			e.cancel = true;
		}
	});
	
	staffGrid.on('afteredit', function(e){
		if (e.field == 'noLimit') {
			if (e.record.get('noLimit') == true) {
				e.record.set('staffQuota', -1);
			}else{
				if (e.record.get('quotaOrig') > 0) {
					e.record.set('staffQuota', e.record.get('quotaOrig'));
				}else{
					e.record.set('staffQuota', 0);
				}
			}
		}
	});
	
	// ---------------------end 表格--------------------------
	var centerPanel = new Ext.Panel({
		title : '员工管理',
		region : 'center',
		layout : 'fit',
		frame : true,
		items : [ staffGrid ],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [ 
			    staffAddBut,
			    {
			    	text : '&nbsp;&nbsp;&nbsp;',
					disabled : true
				}, 
				'->', 
				pushBackBut, 
				{
					text : '&nbsp;&nbsp;&nbsp;',
					disabled : true
				}, 
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
		]
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
			border : false,
			html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
		}]
	});

});

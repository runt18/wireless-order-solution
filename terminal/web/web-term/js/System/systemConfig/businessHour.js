//--------------------退菜原因--------------
var bmObj = {};
bmObj.operation = {
	'insert' : 'INSERT',
	'update' : 'UPDATE',
	'select' : 'SELECT',
	'delete' : 'DELETE',
	'set' : 'SET',
	'get' : 'GET'
};
function businessHourRenderer (){
	return ''
		   + '<a href="javascript:updateBusinessHourHandler()">修改</a>'
		   + '&nbsp;&nbsp;&nbsp;&nbsp;'
		   + '<a href="javascript:deleteBusinessHourHandler()">删除</a>';
}
updateBusinessHourHandler = function(){
	businessHourOperationHandler({
		type : bmObj.operation['update']
	});
};

deleteBusinessHourHandler = function(){
	businessHourOperationHandler({
		type : bmObj.operation['delete']
	});
};

businessHourOperationHandler = function(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	obusinessHour.otype = c.type;
	
	if(c.type == bmObj.operation['insert']){
		oBusinessHourData({
			type : bmObj.operation['set']
		});
		obusinessHour.setTitle('添加市别');
		obusinessHour.show();
		businessHourWin.syncSize();
		businessHourWin.doLayout();
		Ext.getCmp('txtBusinessHourName').focus(true, 100);
	}else if(c.type == bmObj.operation['update']){
		var sd = Ext.ux.getSelData(businessHourGrid);
		if(!sd){
			Ext.example.msg('提示', '请选中一个市别进行操作.');
			obusinessHour.hide();
			businessHourWin.doLayout();
			return;
		}
		oBusinessHourData({
			type : bmObj.operation['set'],
			data : sd
		});
		obusinessHour.setTitle('修改市别');
		obusinessHour.show();
		businessHourWin.syncSize();
		businessHourWin.doLayout();
	}else if(c.type == bmObj.operation['delete']){
		var sd = Ext.ux.getSelData(businessHourGrid);
		if(!sd){
			Ext.example.msg('提示', '请选中一个市别进行操作.');
			obusinessHour.hide();
			businessHourWin.doLayout();
			return;
		}
		Ext.Msg.show({
			title : '重要',
			msg : '是否删除' + '<font style="color:red">&nbsp;' + sd['name'] + '</font>',
			buttons :Ext.Msg.YESNO,
			icon: Ext.MessageBox.QUESTION,
			fn : function(btn){
				if(btn == 'yes'){
					Ext.Ajax.request({
						url : '../../OperateBusinessHour.do',
						params : {
							id : sd['id'],
							dataSource : 'delete'
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnCloseBusinessHourPanel').handler();
								Ext.getCmp('btnRefreshBHGrid').handler();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}
		});
	}else{
		Ext.example.msg('错误', '未知操作类型, 请联系管理员');
	}
};

oBusinessHourData = function(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var id = Ext.getCmp('numBusinessHourId');
	var name = Ext.getCmp('txtBusinessHourName');
	var apmBegin = Ext.getCmp('comboBusinessBeginApm');
	var openingHour = Ext.getCmp('comboBusinessBeginHour');
	var openingMin = Ext.getCmp('comboBusinessBeginMin');
	
	var apmEnd = Ext.getCmp('comboBusinessEndApm');
	var endingHour = Ext.getCmp('comboBusinessEndHour');
	var endingMin = Ext.getCmp('comboBusinessEndMin');
	
	
	if(c.type == bmObj.operation['set']){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		id.setValue(data['id']);
		name.setValue(data['name']);
		if(typeof data['opening'] != 'undefined'){
			beginTimes = data['opening'].split(':');
			endTimes = data['ending'].split(':');
			
			if(eval(beginTimes[0]) > 12){
				apmBegin.setValue(1);
				var openingHourValue = eval(beginTimes[0]) - 12;
				openingHourValue = openingHourValue > 9 ? openingHourValue+'' : '0'+openingHourValue;
				openingHour.setValue(openingHourValue);			
			}else{
				apmBegin.setValue(0);
				openingHour.setValue(beginTimes[0]);
			}
			
			if(eval(endTimes[0]) > 12){
				apmEnd.setValue(1);
				var endingHourValue = eval(endTimes[0]) - 12;
				endingHourValue = endingHourValue > 9 ? endingHourValue+'' : '0'+endingHourValue;
				endingHour.setValue(endingHourValue);		
			}else{
				apmEnd.setValue(0);
				endingHour.setValue(endTimes[0]);
			}
			
			openingMin.setValue(beginTimes[1]);
			
			endingMin.setValue(endTimes[1]);
			
		}else{
			openingHour.setValue('00');
			endingHour.setValue('00');
			openingMin.setValue('00');
			endingMin.setValue('00');
			apmBegin.setValue(0);
			apmEnd.setValue(0);
			
		}
		

		
	}else if(c.type == bmObj.operation['get']){
		openingHour = openingHour.getValue();
		endingHour = endingHour.getValue();
		
		if(apmBegin.getValue() == 1){
			openingHour = eval(openingHour) + 12;
		}
		
		if(apmEnd.getValue() == 1){
			endingHour = eval(endingHour) + 12;
		}
		data.id = id.getValue();
		
		data.opening = openingHour + ':' + openingMin.getValue();
		
		data.ending = endingHour + ':' + endingMin.getValue();
		
		data.name = name.getValue();
			
		c.data = data;
	}
	name.clearInvalid();
	return c;
};
function initBusinessHourWin(){
	var businessHourGridTbar = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '刷新',
			id : 'btnRefreshBHGrid',
			iconCls : 'btn_refresh',
			handler : function(){
				obusinessHour.hide();
				businessHourWin.doLayout();
				businessHourGrid.getStore().reload();
			}
		}, '-', {
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				businessHourOperationHandler({
					type : bmObj.operation['insert']
				});
			}
		}, '-', {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(){
				updateBusinessHourHandler();
			}
		}, '-', {
			text : '删除',
			iconCls : 'btn_delete',
			handler : function(){
				deleteBusinessHourHandler();
			}
		}]
	});
	businessHourGrid = createGridPanel(
		'clientBasicGrid',
		'',
		'',
		'',
		'../../OperateBusinessHour.do',
		[
			[true, false, false, false], 
			['市别', 'name', 60],
			['开始时间', 'opening'],
			['结束时间', 'ending'],
			['操作', 'operation',, 'center', 'businessHourRenderer']
		],
		['id', 'name', 'restaurantId', 'opening', 'ending'],
		[['dataSource', 'getByCond']],
		0,
		'',
		businessHourGridTbar
	);
	businessHourGrid.region = 'center';
	
	obusinessHour = new Ext.Panel({
		title : '&nbsp;',
		hidden : true,
		frame : true,
		region : 'south',
		layout : 'column',
		autoHeight : true,
		defaults : {
			xtype : 'form',
			layout : 'form',
			labelWidth : 35
		},
		items : [{
	    		columnWidth : .12,
		    	labelWidth : 40,
		    	items : [{
		    		xtype : 'tbtext',
		    		style : 'margin-top:2px;',
					text : '市别名:'
				}]
		    },{
			columnWidth : .3,
			items : [{
				xtype : 'textfield',
				id : 'txtBusinessHourName',
				width : 100,
				hideLabel : true,
//				fieldLabel : '市别名称',
				allowBlank : false,
				blankText : '市别名称不能为空.',
				validator : function(v){
					if(Ext.util.Format.trim(v).length > 0){
						return true;
					}else{
						return '市别名称不能为空.';
					}
				}
			}]
			}, {
				columnWidth : 1
			},{
	    		columnWidth : .12,
		    	labelWidth : 40,
		    	items : [{
		    		xtype : 'tbtext',
		    		style : 'margin-top:2px;',
					text : '时间段:'
				}]
		    }, {
		    	columnWidth : .12,
		    	items : [{
		    		xtype : 'combo',
		    		width : 50,
		    		value : 0,
		    		id : 'comboBusinessBeginApm',
		    		forceSelection : true,
					hideLabel : true,
		    		store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ],
						data : [[0, '上午'], [1, '下午' ]]
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					allowBlank : false,
					readOnly : false
		    	}]
		    	
		    },{
		    	columnWidth : .1,
	    		items : [{
					xtype : 'combo',
					forceSelection : true,
					hideLabel : true,
					width : 40,
					value : '00',
					id : 'comboBusinessBeginHour',
					store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ],
						data : [['00', '00' ], ['01', '01' ], ['02', '02' ], ['03', '03' ], ['04', '04' ], ['05', '05'], ['06', '06'], ['07', '07'], ['08', '08'], ['09', '09'], ['10', '10'], ['11', '11'], ['12', '12']]
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					allowBlank : false,
					readOnly : false
				}]
		    }, {
		    	columnWidth : 0.04,
		    	items : [{
		    		xtype : 'tbtext',
		    		style : 'color:red;margin-top:2px;',
		    		text : '时'
		    	}]
		    },{
		    	columnWidth : .1,
	    		items : [{
					xtype : 'combo',
					hideLabel : true,
					forceSelection : true,
					width : 40,
					value : '30',
					id : 'comboBusinessBeginMin',
					store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ],
						data : [['00', '00'], ['30', '30']]
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					allowBlank : false,
					readOnly : false
				}]
		    }, {
		    	columnWidth : .05,
		    	items : [{
		    		xtype : 'tbtext',
		    		style : 'color:red;margin-top:3px;',
		    		text : '分'
		    	}]
		    },{
		    	columnWidth : .06,
		    	items : [{
		    		xtype : 'tbtext',
		    		style : 'color:blue;margin-top:3px;',
		    		text : '至'
		    	}]
		    },{
		    	columnWidth : .12,
		    	items : [{
		    		xtype : 'combo',
		    		width : 50,
		    		value : 0,
		    		id : 'comboBusinessEndApm',
		    		forceSelection : true,
					hideLabel : true,
		    		store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ],
						data : [[0, '上午'], [1, '下午' ]]
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					allowBlank : false,
					readOnly : false
		    	
		    	}]
		    	
		    },{
		    	columnWidth : .1,
	    		items : [{
					xtype : 'combo',
					forceSelection : true,
					hideLabel : true,
					width : 40,
					value : '00',
					id : 'comboBusinessEndHour',
					store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ],
						data : [['00', '00' ], ['01', '01' ], ['02', '02' ], ['03', '03' ], ['04', '04' ], ['05', '05'], ['06', '06'], ['07', '07'], ['08', '08'], ['09', '09'], ['10', '10'], ['11', '11'], ['12', '12']]
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					allowBlank : false,
					readOnly : false
				}]
		    }, {
		    	columnWidth : 0.04,
		    	items : [{
		    		xtype : 'tbtext',
		    		style : 'color:red;margin-top:2px;',
		    		text : '时'
		    	}]
		    },{
		    	columnWidth : .1,
	    		items : [{
					xtype : 'combo',
					hideLabel : true,
					forceSelection : true,
					width : 40,
					value : '00',
					id : 'comboBusinessEndMin',
					store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ],
						data : [['00', '00'], ['30', '30']]
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					allowBlank : false,
					readOnly : false
				}]
		    }, {
		    	columnWidth : .05,
		    	items : [{
		    		xtype : 'tbtext',
		    		style : 'color:red;margin-top:3px;',
		    		text : '分'
		    	}]
		    },{
		    	xtype : 'hidden',
		    	id : 'numBusinessHourId'
		    }],
		buttonAlign : 'center',
		buttons : [{
			text : '保存',
			handler : function(){
				var txtBusinessHour = Ext.getCmp('txtBusinessHourName');
				if(!txtBusinessHour.isValid()){
					return;
				}
				var businessHour = oBusinessHourData({
					type :  bmObj.operation['get']
				}).data;
				var dataSource = '';
				if(obusinessHour.otype == bmObj.operation['insert']){
					dataSource = 'insert';
					(delete businessHour['id']);
				}else if(obusinessHour.otype == bmObj.operation['update']){
					dataSource = 'update';
				}else{
					return;
				}
				Ext.Ajax.request({
					url : '../../OperateBusinessHour.do',
					params : {
						dataSource : dataSource,
						id : businessHour['id'],
						name : businessHour['name'],
						opening : businessHour['opening'],
						ending : businessHour['ending']
					},
					success : function(res, opt){
						var jr = Ext.util.JSON.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('btnCloseBusinessHourPanel').handler();
							Ext.getCmp('btnRefreshBHGrid').handler();
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
			id : 'btnCloseBusinessHourPanel',
			handler : function(){
				obusinessHour.hide();
				businessHourWin.doLayout();
			}
		}]
	});
	
	businessHourWin = new Ext.Window({
		title : '市别管理',
		modal : true,
		resizable : false,
		closable : false,
		draggable : true,
		width : 450,
		height : 390,
		layout : 'border',
		items : [businessHourGrid, obusinessHour],
		bbar : ['->', {
			text : '关闭',
			iconCls : 'btn_close',
			handler : function(){
				businessHourWin.hide();
			}
		}],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				businessHourWin.hide();
			}
		}],
		listeners : {
			beforeshow : function(){
				obusinessHour.hide();
			}
		}
	});
};
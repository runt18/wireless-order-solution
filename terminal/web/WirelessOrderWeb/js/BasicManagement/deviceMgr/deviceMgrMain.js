
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn){
		location.href = 'BasicMgrProtal.html?' + strEncode("restaurantID="+restaurantID, "mi");
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

var addDeviceBut = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddSupplier.png',
	imgwidth : 50,
	imgHeight : 50,
	tooltip : '添加',
	handler : function(){
		operateDevice({otype : 'insert'});
	}
});


var addDeviceWin = new Ext.Window({
	title : '添加设备',
	id : 'deviceAddWin',
	closable : false,
	resizable : false,
	modal : true,
	autoHeight : true,
	width : 290,
	bbar : ['->', {
		text : '保存',
		id : 'btnAddDevice',
		iconCls : 'btn_save',
		handler : function(){
			var pin = Ext.getCmp('txtDeviceId');
			var rId = Ext.getCmp('txtDeviceRId');
			if(pin.isValid() && rId.isValid()){
				var valid = null;
				var model = null;
				var id = '';
				var status = document.getElementsByName("status");
				var device = document.getElementsByName("device");
				var dataSource;
				for ( var i = 0; i < status.length; i++) {
					if(status[i].checked){
						valid = status[i].value;
					}
				}
				
				for ( var i = 0; i < device.length; i++) {
					if(device[i].checked){
						model = device[i].value;
					}
				}
				
				if(addDeviceWin.operationType == 'insert'){
					dataSource = 'insert';
				}else{
					dataSource = 'update';
					id = Ext.getCmp('grid').getSelectionModel().getSelected().data.id;
				}
				
				Ext.Ajax.request({
					url : '../../OperateDevice.do',
					params : {
						dataSource : dataSource,
						id : id,
						deviceId : pin.getValue(),
						rId : rId.getValue(),
						status : valid,
						model : model
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							addDeviceWin.hide();
							Ext.getCmp('grid').store.reload();
						}else{
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(res, opt){
						
					}
				});
				
				
				
			}
		}
		
	}, {
		text : '取消',
		id : 'btnCloseDevice',
		iconCls : 'btn_close',
		handler : function(){
			addDeviceWin.hide();
		}
	}],
	items : [{
		layout : 'form',
		id : 'deviceForm',
		border : false,
		frame : true,
		labelWidth : 75,
		labelAlign : 'right',
		items : [{
			xtype : 'textfield',
			id : 'txtId',
			hideLabel : true,
			hidden : true
		}, {
			xtype : 'textfield',
			id : 'txtDeviceId',
			fieldLabel : 'PIN',
			width : 140,
			allowBlank : false
		}, {
			xtype : 'textfield',
			id : 'txtDeviceRId',
			fieldLabel : '餐厅编号',
			width : 140,
			allowBlank : false
		}, {
			layout : 'column',
			frame : true,
			border : false,
			frame : false,
			defaults : {
				columnWidth : .33,
				labelWidth : 40,
			},
			items : [{
				columnWidth : .3,
				xtype : 'label',
				labelWidth : 75,
				html : '状态:&nbsp; '
			},{
				items : [{
					xtype : 'radio',
					name : 'status',
					id : 'rdoRecordAlive',
					inputValue : 2,
					hideLabel : true,
					checked : true,
					boxLabel : '启用'
				}]
			}, {
				items : [{
					xtype : 'radio',
					name : 'status',
					inputValue : 1,
					hideLabel : true,
					boxLabel : '停用'
				}]
			}]
				
		},{
			layout : 'column',
			frame : true,
			border : false,
			frame : false,
			defaults : {
				columnWidth : .21,
				labelWidth : 40,
			},
			items : [{
				columnWidth : .3,
				xtype : 'label',
				labelWidth : 75,
				html : '型号:&nbsp; '
			},{
				columnWidth : .28,
				items : [{
					xtype : 'radio',
					name : 'device',
					id : 'rdoAndroid',
					inputValue : 1,
					hideLabel : true,
					checked : true,
					boxLabel : 'Android'
				}]
			}, {
				items : [{
					xtype : 'radio',
					name : 'device',
					inputValue : 2,
					hideLabel : true,
					boxLabel : 'IOS'
				}]
			},{
				items : [{
					xtype : 'radio',
					name : 'device',
					inputValue : 3,
					hideLabel : true,
					boxLabel : 'WP'
				}]
			}]
				
		}]
	}]
});



function operateDevice(c){
	if(c.otype != 'undefind'){
		if(c.otype == 'insert'){
			addDeviceWin.show();
			addDeviceWin.operationType = c.otype;
			addDeviceWin.setTitle('添加终端');
		}else if(c.otype == 'update'){
			addDeviceWin.show();
			addDeviceWin.operationType = c.otype;
			addDeviceWin.setTitle('修改终端');
			var ss = Ext.getCmp('grid').getSelectionModel().getSelected();
			Ext.getCmp('txtDeviceId').setValue(ss.data.deviceId);
			Ext.getCmp('txtDeviceRId').setValue(ss.data.restaurantId);
			
			var status = document.getElementsByName("status");
			for ( var i = 0; i < status.length; i++) {
				if(status[i].value == ss.data.statusValue){
					status[i].checked = true;
				}
			}
			
			var model = document.getElementsByName("device");
			for ( var i = 0; i < model.length; i++) {
				if(model[i].value == ss.data.modelValue){
					model[i].checked = true;
				}
			}
			
		}else{
			Ext.Msg.confirm('提示', '是否删除此设备', function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../OperateDevice.do',
						params : {
							id : Ext.getCmp('grid').getSelectionModel().getSelected().data.id,
							dataSource : 'delete'
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('grid').store.reload();
							}
						},
						failure : function(){
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			});
			

		}
		
	}
}

function optDevice(){
	return "<a href = \"javascript:operateDevice({otype:'update'})\">" + "<img src='../../images/Modify.png' />修改</a>" 
			+ "&nbsp;&nbsp;"
			+ "<a href=\"javascript:operateDevice({otype : 'delete'})\">" + "<img src='../../images/del.png' />删除</a>";
}

var ds = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : '../../QueryDevice.do'
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : 'totalProperty',
		root : 'root'
	}, [{
		name : 'id'
	},{
		name : 'restaurantId'
	},{
		name : 'restaurantText'
	},{
		name : 'deviceId'
	},{
		name : 'deviceIdCrc'
	},{
		name : 'modelValue'
	},{
		name : 'modelText'
	},{
		name : 'statusValue'
	},{
		name : 'statusText'
	}])
});

ds.load();

var cm = new Ext.grid.ColumnModel([
	new Ext.grid.RowNumberer(),
	{header : '设备编号', dataIndex : 'deviceId', width : 200},
	{header : '所属餐厅', dataIndex : 'restaurantText', width : 200},
	{header : '型号', dataIndex : 'modelText', width : 200},
	{header : '状态', dataIndex : 'statusText', width : 200},
	{header : '操作', dataIndex : 'optDevice', id : 'optDevice', renderer : optDevice, width : 200}
]);
var deviceGrid;
Ext.onReady(function(){
	Ext.BLANK_IMAGE_URL = '../../extjs/resources/images/default/s.gif';
	Ext.QuickTips.init();
	
	deviceGrid = new Ext.grid.GridPanel({
		title : 'Digi-e管理中心 - 终端管理',
		id : 'grid',
		height : '500',
		region : 'center',
		border : true,
		frame : true,
		cm : cm,
		store : ds,
		//autoExpandColumn : 'optDevice',
/*		viewConfig : {
			forceFit : true
		},
		*/
		tbar : new Ext.Toolbar({
			items : [{
				xtype : 'tbtext',
				text : '餐厅编号: '
			},{
				xtype : 'textfield',
				id : 'restaurantId',
				width : 120
			}, '->', {
				id : 'btnSearch',
				text : '搜索',
				iconCls : 'btn_search',
				handler : function(){
					var store = deviceGrid.getStore();
					store.baseParams['rId'] = Ext.getCmp('restaurantId').getValue();
					store.load();
				}
			}]
		}),
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnSearch').handler();
			}
		}]

	});
	
	var devicePanel = new Ext.Panel({
		title : '管理中心',
		region : 'center',
		layout : 'border',
		frame : true,
		items : [deviceGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
			    addDeviceBut,
			    '->',
			    pushBackBut, 
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
				logOutBut 
			]
		}),
	});
	
	new Ext.Viewport({
		layout : 'border',
		id : 'viewport',
		items : [{
			region : 'north',
			bodyStyle : 'background-color:#DFE8F6;',
			html : "<h4 style='padding:10px;font-size:150%;float:left;'>无线点餐网页终端</h4>" +
				"<div id='optName' class='optName'></div>",
			height : 50,
			border : false,
			margins : '0 0 0 0'
		}, devicePanel, {
			region : 'south',
			height : 30,
			frame : true,
			html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
		}]
	});
});
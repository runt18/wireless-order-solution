var pushBackBut = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn){
		location.href = 'BasicMgrProtal.html?'+strEncode("restaurantID=" + restaurantID, "mi");
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

var restaurantAddWin = new Ext.Window({
	title : '创建餐厅',
	id : 'restaurantAddWin',
	closable : false,
	resizable : false,
	modal : true,
	autoHeight : true,
	bbar : ['->', {
		text : '保存',
		id : 'btnAddRestaurant',
		iconCls : 'btn_save',
		handler : function(){
			var pwd = Ext.getCmp('txtPwd');
			var confirmPwd = Ext.getCmp('txtConfirmPwd');
			var name = Ext.getCmp('txtName');
			var account = Ext.getCmp('txtAccount');
			var expireDate = Ext.getCmp('dataExpireDate');
			if(pwd.isValid() && confirmPwd.isValid() && name.isValid() && account.isValid() && expireDate.isValid()){
				var dataSource = '';
				var rId = '';
				var tele1 = Ext.getCmp('txtTele1').getValue();
				var tele2 = Ext.getCmp('txtTele2').getValue();
				var address = Ext.getCmp('txtAddress').getValue();
				var recordAlive = '';
				var info = Ext.getCmp('txtInfo').getValue();
				
				var dates = document.getElementsByName('recordAlive');
				for ( var i = 0; i < dates.length; i++) {
					if(dates[i].checked){
						recordAlive = dates[i].value;
					}
				}
				if(restaurantAddWin.operationType == 'insert'){
					dataSource = 'insert';
				}else if(restaurantAddWin.operationType == 'update'){
					dataSource = 'update';
					rId = Ext.getCmp('grid').getSelectionModel().getSelected().data.id;
				}
				
				Ext.Ajax.request({
					url : '../../OperateRestaurant.do',
					params : {
						id : rId,
						account : account.getValue(),
						name : name.getValue(),
						pwd : pwd.getValue(),
						info : info,
						tele1 : tele1,
						tele2 : tele2,
						address : address,
						recordAlive : recordAlive,
						expireDate : expireDate.getValue().format('Y-m-d'),
						dataSource : dataSource
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							restaurantAddWin.hide();
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('grid').store.reload();
						}
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.decode(res.responseText));
					}
				
				});

			}
		}
	},{
		text : '取消',
		id : 'btnCloseRestaurant',
		iconCls : 'btn_close',
		handler : function(){
			restaurantAddWin.hide();
		}
	}],
	items : [{
		layout : 'form',
		id : 'restaurantForm',
		border : false,
		frame : true,
		labelWidth : 75,
		labelAlign : 'right',
		items : [{
			xtype : 'textfield',
			id : 'txtRestaurantId',
			hideLabel : true,
			hidden : true
		},{
			xtype : 'textfield',
			fieldLabel : '账户名',
			id : 'txtAccount',
			width : 140,
			allowBlank : false
		},{
			xtype : 'textfield',
			fieldLabel : '管理员密码',
			id : 'txtPwd',
			width : 140,
			inputType : 'password',
			
			allowBlank : false
			
		},{
			xtype : 'textfield',
			fieldLabel : '确认密码',
			id : 'txtConfirmPwd',
			inputType : 'password',
			width : 140,
			allowBlank : false,
			validator : function(v){
				if(Ext.getCmp('txtPwd').getValue() == v){
					return true;
				}else{
					return '密码不一致';
				}
			}
			
		},{
			xtype : 'textfield',
			fieldLabel : '餐厅名',
			id : 'txtName',
			width : 140,
			allowBlank : false
		},{
			xtype : 'textfield',
			fieldLabel : '电话1',
			width : 140,
			id : 'txtTele1',
			regex : Ext.ux.RegText.phone.reg,
			regexText : Ext.ux.RegText.phone.error,
			disabled : true
			
		},{
			xtype : 'textfield',
			fieldLabel : '电话2',
			width : 140,
			id : 'txtTele2',
			regex : Ext.ux.RegText.phone.reg,
			regexText : Ext.ux.RegText.phone.error,
			disabled : true
				
		},{
			xtype : 'textfield',
			fieldLabel : '地址',
			width : 220,
			id : 'txtAddress',
			
		},{
			xtype : 'datefield',
			fieldLabel : '账号有效期',
			id : 'dataExpireDate',
			format : 'Y-m-d',
			allowBlank : false
		},{
			layout : 'column',
			frame : true,
			border : false,
			frame : false,
			defaults : {
				columnWidth : .18,
				labelWidth : 40,
			},
			items : [{
				columnWidth : .25,
				xtype : 'label',
				html : '账单有效期:&nbsp; '
			},{
				items : [{
					xtype : 'radio',
					name : 'recordAlive',
					id : 'rdoRecordAlive',
					inputValue : 2,
					hideLabel : true,
					checked : true,
					boxLabel : '90天'
				}]
			}, {
				items : [{
					xtype : 'radio',
					name : 'recordAlive',
					inputValue : 3,
					hideLabel : true,
					boxLabel : '180天&nbsp;'
				}]
			},{
				items : [{
					xtype : 'radio',
					name : 'recordAlive',
					inputValue : 4,
					hideLabel : true,
					boxLabel : '1年'
				}]
			},{
				items : [{
					xtype : 'radio',
					name : 'recordAlive',
					inputValue : 1,
					hideLabel : true,
					boxLabel : '无期限'
				}]
			}]
				
		},{
			xtype : 'textarea',
			fieldLabel : '餐厅信息',
			width : 220,
			id : 'txtInfo'
		}]
	}],
	listeners : {
		show : function(){
			Ext.getCmp('txtAccount').setValue('');
			Ext.getCmp('txtAccount').clearInvalid();
			Ext.getCmp('txtPwd').setValue('');
			Ext.getCmp('txtPwd').clearInvalid();
			Ext.getCmp('txtConfirmPwd').setValue('');
			Ext.getCmp('txtConfirmPwd').clearInvalid();
			Ext.getCmp('txtName').setValue('');
			Ext.getCmp('txtName').clearInvalid();
			Ext.getCmp('dataExpireDate').setValue('');
			Ext.getCmp('dataExpireDate').clearInvalid();
			
			Ext.getCmp('txtTele1').setValue('');
			Ext.getCmp('txtTele2').setValue('');
			Ext.getCmp('txtAddress').setValue('');
			Ext.getCmp('txtInfo').setValue('');
			Ext.getCmp('rdoRecordAlive').checked = true;
		}
	},
	keys : [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp('btnAddRestaurant').handler();
		}
	}]
});



function optRestaurant(){
	return ''
	+ "<a href = \"javascript:optRestaurantHandler({otype:'update'})\">" + "<img src='../../images/Modify.png'/>修改</a>";
}

function hideAddress(v){
	if(v.length > 10){
		return v.substring(0,10)+'...';
	}else{
		return v;
	}
}

function optRestaurantHandler(c){
	if(c.otype != 'undefined'){
		if(c.otype == 'insert'){
			restaurantAddWin.setTitle('创建餐厅');
			restaurantAddWin.operationType = c.otype;
			restaurantAddWin.show();
			restaurantAddWin.center();
			Ext.getCmp('txtAccount').focus(true, 100);
		}else if(c.otype == 'update'){
			var data = Ext.getCmp('grid').getSelectionModel().getSelected().data;
			restaurantAddWin.setTitle('修改餐厅');
			restaurantAddWin.operationType = c.otype;
			restaurantAddWin.show();
			
			Ext.getCmp('txtRestaurantId').setValue(data.id);
			Ext.getCmp('txtAccount').setValue(data.account);
			Ext.getCmp('txtPwd').setValue(encrypt);
			Ext.getCmp('txtConfirmPwd').setValue(encrypt);
			Ext.getCmp('txtName').setValue(data.name);
			Ext.getCmp('txtTele1').setValue(data.tele1);
			Ext.getCmp('txtTele2').setValue(data.tele2);
			Ext.getCmp('txtAddress').setValue(data.address);
			Ext.getCmp('dataExpireDate').setValue(data.expireDate);
			Ext.getCmp('txtAccount').focus(true, 100);
			var dates = document.getElementsByName('recordAlive');
			for ( var i = 0; i < dates.length; i++) {
				if(dates[i].value == data.recordAliveValue){
					dates[i].checked = true;
				}
			}
			
		}
	}
	
	
}
var restaurantPanel;
Ext.onReady(function(){
	Ext.BLANK_IMAGE_URL = '../../extjs/resources/images/default/s.gif';
	Ext.QuickTips.init();
	
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({
			url : '../../QueryRestaurants.do'
		}),
		reader : new Ext.data.JsonReader({
			totalProperty : 'totalProperty',
			root : 'root'
		}, [{
			name : 'id'
		},{
			name : 'account'
		},{
			name : 'name'
		},{
			name : 'info'
		},{
			name : 'tele1'
		},{
			name : 'tele2'
		},{
			name : 'address'
		},{
			name : 'liveness'
		},{
			name : 'recordAliveValue'
		},{
			name : 'recordAliveText'
		},{
			name : 'birthDate'
		},{
			name : 'expireDate'
		}])
	});
	
	ds.load();
	
	var cm = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header : '餐厅编号', dataIndex : 'id', width : 80},
		{header : '账户名', dataIndex : 'account'},
		{header : '创建时间', dataIndex : 'birthDate'},
		{header : '账号有效期', dataIndex : 'expireDate'},
		{header : '餐厅名', dataIndex : 'name', width : 150},
		{header : '活跃度', dataIndex : 'liveness'},
		{header : '电话1', dataIndex : 'tele1'},
		{header : '电话2', dataIndex : 'tele2'},
		{header : '地址', dataIndex : 'address', renderer : hideAddress},
		{header : '餐厅信息', dataIndex : 'info'},
		{header : '账单有效期', dataIndex : 'recordAliveText'},
		{header : '操作', dataIndex : 'optRestaurant', id : 'optRestaurant', align : 'center', renderer : optRestaurant}
		
	]);
	
	restaurantPanel = new Ext.grid.GridPanel({
		title : 'Digi-e管理中心 - 餐厅信息',
		id : 'grid',
		region : 'center',
		border : true,
		frame : true,
		cm : cm,
		store : ds,
		autoExpandColumn : 'optRestaurant',
		tbar : new Ext.Toolbar({
			items : [{
				xtype : 'tbtext',
				text : '餐厅名: '
			},{
				xtype : 'textfield',
				id : 'txtSearchName',
				width : 120,
			},{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			},{
				xtype : 'tbtext',
				text : '有效期: '
			},{
				xtype : 'checkbox',
				id : 'chkExpireDate'
			},{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			},{
				xtype : 'tbtext',
				text : '活跃度: '
			},{
				xtype : 'checkbox',
				id : 'chkAlive'
			}, '->', {
				id : 'btnSearch',
				text : '搜索',
				iconCls : 'btn_search',
				handler : function(){
					var store = restaurantPanel.getStore();
					var expireDate = null, alive = null;
					if(Ext.getCmp('chkExpireDate').checked){
						expireDate = 'true';
					}
					if(Ext.getCmp('chkAlive').checked){
						alive = 'true';
					}
					
					store.baseParams['account'] = Ext.getCmp('txtSearchName').getValue();
					store.load({
						params : {
							expireDate : expireDate,
							alive : alive
						}
					});
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
	
	var centerPanel = new Ext.Panel({
		title : 'Digi-e管理中心',
		id : 'mgrPanel',
		region : 'center',
		layout : 'border',
		frame : true,
		items : [restaurantPanel],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [{
				text : '创建餐厅',
				handler : function(){
					optRestaurantHandler({otype : 'insert'});
				}
			},'->',
			    pushBackBut, 
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
				logOutBut 
			]
		})
	});
	
	new Ext.Viewport({
		layout : 'border',
		id : 'viewport',
		items : [{
			region : 'north',
			bodyStyle : 'background-color:#DFE8F6;',
			html : "<h4 style='padding:10px;font-size:150%;float:left;'>无线点餐网页终端</h4><div id='optName' class='optName'></div>",
			height : 50,
			border : false,
			margins : '0 0 0 0'
		}, centerPanel, {
			region : 'south',
			height : 30,
			frame : true,
			html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
		}]
	});
});
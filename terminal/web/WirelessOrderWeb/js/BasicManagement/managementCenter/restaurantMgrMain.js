var pushBackBut = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn){
		location.href = '../MgrCenter.html';
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

var addRestaurant = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddRestaurant.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加餐厅',
	handler : function(btn){
		optRestaurantHandler({otype : 'insert'});
	}
});

function getChecked(checkBoxs){
	var checkeds = "";
	for (var i = 0; i < checkBoxs.length; i++) {
		if(checkBoxs[i].checked){
			if(checkeds == ""){
				checkeds += checkBoxs[i].value;
			}else{
				checkeds += "," + checkBoxs[i].value;
			}
		}
	}
	return checkeds;
}
function getRestaurantModules(){
	if(document.getElementsByName('modules').length == 0){
		for (var i = 0; i < moduleData.length; i++) {
			var c = {items : [{
				xtype : "checkbox", 
				name : "modules",
				boxLabel : moduleData[i].desc, 
				hideLabel : true, 
				//checked  : moduleData[i].code == 1000?true:false,
				inputValue :  moduleData[i].code
			}]};
			Ext.getCmp('formRestaurantModule').add(c);
			//solveIE自动换行时格式错乱
			if((i+1)%4 == 0){
				Ext.getCmp('formRestaurantModule').add({columnWidth : 1});
			}
			Ext.getCmp('formRestaurantModule').doLayout();
		}
	}
}


function initModulesData(){
	Ext.Ajax.request({
		url : '../../QueryModule.do',
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				moduleData = jr.root;
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	
	});
}

var restaurantAddWin = new Ext.Window({
	title : '创建餐厅',
	id : 'restaurantAddWin',
	closable : false,
	resizable : false,
	modal : true,
	autoHeight : true,
	width : 360,
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
						pwd : pwd.getValue() == encrypt ? '' : pwd.getValue(),
						info : info,
						tele1 : tele1,
						tele2 : tele2,
						address : address,
						recordAlive : recordAlive,
						expireDate : expireDate.getValue().format('Y-m-d'),
						moduleCheckeds : getChecked(document.getElementsByName('modules')),
						dataSource : dataSource
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							restaurantAddWin.hide();
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('grid').store.reload();
						}else{
							jr['icon'] = Ext.MessageBox.WARNING;
							Ext.ux.showMsg(jr);
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
			regex : Ext.ux.RegText.tel.reg,
			regexText : Ext.ux.RegText.tel.error
			
		},{
			xtype : 'textfield',
			fieldLabel : '电话2',
			width : 140,
			id : 'txtTele2',
			regex : Ext.ux.RegText.tel.reg,
			regexText : Ext.ux.RegText.tel.error
				
		},{
			xtype : 'textfield',
			fieldLabel : '地址',
			width : 220,
			id : 'txtAddress'
			
		},{
			xtype : 'datefield',
			fieldLabel : '账号有效期',
			id : 'dataExpireDate',
			format : 'Y-m-d',
			width : 100,
			allowBlank : false
		},{
			layout : 'column',
			frame : true,
			border : false,
			frame : false,
			defaults : {
				columnWidth : .18,
				labelWidth : 40
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

					boxLabel : '90天'
				}]
			}, {
				items : [{
					xtype : 'radio',
					name : 'recordAlive',
					inputValue : 3,
					hideLabel : true,
					checked : true,
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
		},{
			xtype : 'panel',
			layout : 'column',
			id : 'formRestaurantModule',
			frame : true,
			width : 330,
			defaults : {
				columnWidth : .25,
				layout : 'form',
				labelWidth : 80
			},
			items : [{
				columnWidth : 1,
				xtype : 'label',
				style : 'text-align:left;padding-bottom:3px;margin-left:20px;',
				text : '授权模块:'
			}]
					
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
		},
		beforeshow : function(){
			getRestaurantModules();
		},
		hide : function(){
			var modules = document.getElementsByName('modules');
			for (var i = 0; i < modules.length; i++) {
				if(modules[i].checked){
					modules[i].checked = false;
				}
			}
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
	if(v.length > 7){
		return v.substring(0,7)+'...';
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
			var date = new Date();
			date.setFullYear(date.getFullYear() + 1);
			Ext.getCmp('dataExpireDate').setValue(date);
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
			Ext.getCmp('txtInfo').setValue(data.info);
			Ext.getCmp('txtAccount').focus(true, 100);
			var dates = document.getElementsByName('recordAlive');
			for ( var i = 0; i < dates.length; i++) {
				if(dates[i].value == data.recordAliveValue){
					dates[i].checked = true;
				}
			}
			
			var modules = document.getElementsByName('modules');
			for (var i = 0; i < data.modules.length; i++) {
				for (var j = 0; j < modules.length; j++) {
					if(data.modules[i].code == modules[j].value){
						modules[j].checked = true;
					}
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
		},{
			name : 'modules'
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
		{header : '餐厅信息', dataIndex : 'info', renderer : hideAddress},
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
				listeners : {
					focus : function(thiz){
						thiz.focus(true, 100);
					}
				}
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
					
					store.baseParams['name'] = Ext.getCmp('txtSearchName').getValue();
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
		title : '管理中心',
		id : 'mgrPanel',
		region : 'center',
		layout : 'border',
		frame : true,
		items : [restaurantPanel],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
				addRestaurant,
				'->',
			    pushBackBut, 
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'}
			]
		})
	});
	
	new Ext.Viewport({
		layout : 'border',
		id : 'viewport',
		items : [{
			region : 'north',
			bodyStyle : 'background-color:#DFE8F6;',
			html : "<h4 style='padding:10px;font-size:150%;float:left;'>Digi-e管理中心</h4><div id='optName' class='optName'></div>" +
					"<div id='divLoginOut' class='loginOut' style='width: 40px;height: 41px;'><img id='btnLoginOut' src='../../images/ResLogout.png' width='40' height='40' /> </div>",
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
	Ext.get('btnLoginOut').on('click', function(){
		Ext.Ajax.request({
			url : '../../LoginOut.do',
			success : function(){
				location.href = '../LoginAdmin.html';
			},
			failure : function(){
				
			}
		});
    });
    
    initModulesData();
});
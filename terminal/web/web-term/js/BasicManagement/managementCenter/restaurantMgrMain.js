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
		params : {
			dataSource : 'getModules'
		},
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
				var dianping = Ext.getCmp('txtDianping').getValue();
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
						dianping : dianping,
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
			xtype : 'numberfield',
			fieldLabel : '大众点评ID',
			width : 140,
			id : 'txtDianping'
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
			Ext.getCmp('txtDianping').setValue();
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



function optRestaurant(v, m, r, ri, ci, s){
	var operate =  "<a href = \"javascript:optRestaurantHandler({otype:'update'})\">" + "<img src='../../images/Modify.png'/>修改</a>"
					+ "&nbsp;&nbsp;<a href = \"javascript:displayCodeHandle()\">" + "<img src='../../images/Modify.png'/>生成验证码</a>";
	
	if(r.get('typeVal') != "3"){
		operate += "&nbsp;&nbsp;<a href = \"javascript:chainSet()\">" + "<img src='../../images/Modify.png'/>连锁设置</a>"
	}
	
	
	return operate;
}

//连锁设置
var chainGrid;
function chainSet(){
	//获取当前选中栏的信息
	var tn = Ext.ux.getSelData(restaurantPanel);
	
	//连锁管理的头部工具栏
	var chainTbar = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				chainManagePanel.setTitle('添加价格方案');
				Ext.getCmp('branchesId_number_chain').setValue("");
				chainManagePanel.show();
				chainGridPanel.syncSize();
				chainGridPanel.doLayout();
			}
		}]
	});
	
	//当前选中栏是否有门店 ? 有 : 无
	var data;
	if(tn.branches){
		data=tn.branches;
	}else{
		data = [];
	}
	
	//门店grid
    var chainStore = new Ext.data.JsonStore({data:data,fields:["id", "name"]});
    chainGrid = new Ext.grid.GridPanel({
    	region : 'center',
        columns : [{header:"门店编号",dataIndex:"id"},
                   {header:"门店账号",dataIndex : "name"},
                   {header:"操作", dataIndex : 'operation', renderer : chainGridOperation}],
        store : chainStore,
    }); 
    
//  //添加单个记录
//    var ss ='1001';
//    var name = '说的';
//    
//    var data = { 'account': ss,'name': name  };
//
//    var p = new chainStore.recordType(data,data.id);
//    
//    chainStore.add(p);
//
//    var datar = new Array();
//    var jsonDataEncode = "";
//    var records = grid.getStore().getRange();
//    for (var i = 0; i < records.length; i++) {
//        datar.push(records[i].data);
//    }
//    jsonDataEncode = Ext.util.JSON.encode(datar);
//    console.log(jsonDataEncode);
    
	var chainManagePanel = new Ext.Panel({
		title : '&nbsp;',
		hidden : true,
		frame : true,
		region : 'south',
		layout : 'column',
		autoHeight : true,
		defaults : {
			xtype : 'form',
			layout : 'form',
			labelWidth : 55
		},
		items : [{
			columnWidth : 1,
			items : [{
				xtype : 'numberfield',
				width : 220,
				id : 'branchesId_number_chain',
				fieldLabel : '门店编号',
				allowBlank : false,
				blankText : '门店编号不能为空.',
				validator : function(v){
					if(Ext.util.Format.trim(v).length > 0){
						return true;
					}else{
						return '门店编号不能为空.';
					}
				}
			}]
		}],	
		buttonAlign : 'center',
		buttons : [{
			text : '保存',
			handler : function(){
				var id = Ext.getCmp('branchesId_number_chain').getValue();
				
				 Ext.Ajax.request({
					url : '../../QueryRestaurants.do',
					params : {
						id : id
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success && jr.root.length > 0){
							var data = {'id' : id, 'name' : jr.root[0].name};
						    var store =  new chainStore.recordType(data);
						    chainGrid.getStore().add(store);
						    chainManagePanel.hide();
							chainGridPanel.doLayout();	
						}else{
							Ext.ux.showMsg({success : true, title : '提示',msg : '没有此餐厅'});
						}
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.decode(res.responseText));
					}
				});
			}
		}, {
			text : '取消',
			handler : function(){
				chainManagePanel.hide();
				chainGridPanel.doLayout();
			}
		}]
	});
    
    
	
	//连锁设置的窗口
	var chainGridPanel = new Ext.Window({
		modal : true,
		resizable : false,
		closable : false,
		draggable : false,
		width : 330,
		height : 390,
		layout : 'border',
		items : [chainGrid, chainManagePanel],
		tbar : chainTbar,
		bbar : ['->',{
			text : '保存',
			iconCls : 'btn_add',
			handler : function(){
				//将grid的数据变成json对象
			    var data = [];
			    var jsonDataEncode = "";
			    var records = chainGrid.getStore().getRange();
			    for (var i = 0; i < records.length; i++) {
			        data.push(records[i].data);
			    }
			    jsonDataEncode = Ext.util.JSON.encode(data);
			    
			    //将json对象变成js对象
			    var chainJson = JSON.parse(jsonDataEncode);
			    
			    var branchesId = [];
			    for(var i = 0; i < chainJson.length; i++){
			    	branchesId.push(chainJson[i].id);
			    }
			    console.log(branchesId);
			    
			    Ext.Ajax.request({
					url : '../../OperateRestaurant.do',
					params : {
						dataSource : 'update',
						id : tn.id,
						branches : branchesId.join(','),
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							chainGridPanel.hide();
							chainManagePanel.hide();
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('grid').store.reload();
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
			text : '关闭',
			iconCls : 'btn_close',
			handler : function(){
				chainGridPanel.close();
			}
		}],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				chainGridPanel.close();	
			}
		}]
	});
	chainGridPanel.show();
	chainGridPanel.setTitle('连锁管理---' + tn.name);

}

function chainGridOperation(){
	return "<a href = \"javascript:deleteChain()\">" + "<img src='../../images/Modify.png'/>删除</a>";
}

//删除门店
function deleteChain(){
	chainGrid.getStore().removeAt(chainGrid.getStore().indexOf(chainGrid.getSelectionModel().getSelected()));
}

function optSms(v){
	return ''
	+ "<a href = \"javascript:optSmsHandler()\">" + "<img src='../../images/Modify.png'/>"+ v +"</a>";
}

function unUsedCode(v){
	return ''
	+ "<a href = \"javascript:displayCodesHandler()\">" + "<img src='../../images/search.gif'/>"+ v +"</a>";
}	

function hideAddress(v){
	if(v.length > 7){
		return v.substring(0,7)+'...';
	}else{
		return v;
	}
}
function changeAdjustSmsLabel(label){
	Ext.query('label[for="numAdjustSms"]')[0].innerHTML = label+':';
}

function optSmsHandler(){
	var data = Ext.ux.getSelData(restaurantPanel);
	var numAdjustSms = new Ext.form.NumberField({
			xtype : 'numberfield',
			id : 'numAdjustSms',
			fieldLabel : '',
			style : 'color:red;',
			width : 100,
			allowBlank : false,
			blankText : '不能为空, 0 则取消操作.',
			validator : function(value){
				var adjust = document.getElementsByName('radioAdjustSms');
				for(var i=0; i< adjust.length; i++){
					if(adjust[i].checked){
						adjust = adjust[i].value;
						break;
					}
				}
				if(adjust == 2){
					if(Math.abs(value) > data['smsRemain']){
						Ext.getCmp('numAdjustSms').setValue(data['smsRemain']);
					}
					return true;
				}else{
					return true;
				}
			},
			listeners : {
				render : function(){
					Ext.getCmp('radioAdjustSmsIncrease').setValue(true);
				}
			}
		});
	if(!adjustSmsWin){
		adjustSmsWin = new Ext.Window({
			title : '&nbsp;',
			modal : true,
			closable : false,
			resizable : false,
			width : 200,
			height : 146,
			layout : 'fit',
			frame : true,
			items : [{
				layout : 'column',
				frame : true,
				defaults : {
					columnWidth : .33,
					layout : 'form',
					labelWidth : 60
				},
				items : [{
					items : [{
						xtype : 'radio',
						id : 'radioAdjustSmsIncrease',
						name : 'radioAdjustSms',
						inputValue : 1,
						hideLabel : true,
						boxLabel : '增加',
						listeners : {
							check : function(e){
								if(e.getValue()){
									changeAdjustSmsLabel('增加短信');
								}
							}
						}
					}]
				}, {
					items : [{
						xtype : 'radio',
						name : 'radioAdjustSms',
						inputValue : 2,
						hideLabel : true,
						boxLabel : '减少',
						listeners : {
							check : function(e){
								if(e.getValue()){
									changeAdjustSmsLabel('减少短信');
								}
							}
						}
					}]
				}, {
					columnWidth : 1,
					items : [{
						xtype : 'textfield',
						id : 'numMemberSmsForNow',
						fieldLabel : '当前短信',
						style : 'color:green;',
						width : 100,
						disabled : true
					}]
				}, {
					columnWidth : 1,
					items : [numAdjustSms]
				},{
					xtype : 'hidden',
					id : 'smsRestaurantId'
				}]
			}],
			bbar : ['->', {
				text : '保存',
				iconCls : 'btn_save',
				handler : function(){
					if(!numAdjustSms.isValid()){
						return;
					}
					if(numAdjustSms.getValue() == 0){
						adjustSmsWin.hide();
						Ext.example.msg('提示', '你输入的短信为0, 无需调整');
						return;
					}
					var adjust = document.getElementsByName('radioAdjustSms');
					for(var i=0; i< adjust.length; i++){
						if(adjust[i].checked){
							adjust = adjust[i].value;
							break;
						}
					}
					var dataSource = "";
					if(adjust == 1){
						dataSource = "add";
					}else{
						dataSource = "reduce";
					}
					Ext.Ajax.request({
						url : '../../OperateSms.do',
						params : {
							restaurantId : Ext.getCmp('smsRestaurantId').getValue(),
							count : numAdjustSms.getValue(),
							dataSource : dataSource
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								adjustSmsWin.hide();
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnSearch').handler();
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
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					adjustSmsWin.hide();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					adjustSmsWin.hide();
				}
			}],
			listeners : {
				hide : function(){
					numAdjustSms.setValue();
					Ext.getCmp('radioAdjustSmsIncrease').setValue(true);
					numAdjustSms.clearInvalid();
				}
			}
		});
	}
	adjustSmsWin.show();
	adjustSmsWin.setTitle('短信调整');
	Ext.getCmp('numMemberSmsForNow').setValue(data['smsRemain']);
	Ext.getCmp('smsRestaurantId').setValue(data['id']);
	numAdjustSms.focus(true, 100);
}

function setWeixinMenu(){
	var data = Ext.ux.getSelData(restaurantPanel);
	window.open('weixinMenuMgr.html?rid='+data.id);	
}


function displayCodeHandle(){
	var data = Ext.ux.getSelData(restaurantPanel);
	if(!displayCodeWin){
		displayCodeWin = new Ext.Window({
			title : '&nbsp;',
			modal : true,
			closable : false,
			resizable : false,
			width : 200,
			height : 146,
			layout : 'fit',
			frame : true,
			items : [{
				layout : 'column',
				frame : true,
				defaults : {
					columnWidth : .25,
					layout : 'form',
					labelWidth : 60
				},
				items : [ {
					columnWidth : 1,
					style : 'text-align:center;',
					items : [{
						xtype : 'label',
						id : 'numMemberSmsForNow',
						style : 'color:green;font-size:25px;',
						text : 0
					}]
				}, {
					columnWidth : 1,
					items : [{
						xtype : 'label',
						html : '&nbsp;',
					}]
				},{
					items : [{
						xtype : 'label',
						text : '已使用:',
					}]
				}, {
					items : [{
						xtype : 'label',
						id : 'numUsedCode',
						style : 'color:green;',
						text : 0,
					}]
				},{
					items : [{
						xtype : 'label',
						text : '未使用:',
					}]
				}, {
					columnWidth : .23,
					items : [{
						xtype : 'label',
						id : 'numUnUsedCode',
						style : 'color:green;',
						text : 0,
					}]
				}]
			}],
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					displayCodeWin.hide();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					displayCodeWin.hide();
				}
			}],
			listeners : {
				hide : function(){
				}
			}
		});
	}
	
	displayCodeWin.setTitle('验证码生成 -- ' + data['name']);
	//获取验证码
	Ext.Ajax.request({
		url : '../../OperateRestaurant.do',
		params : {
			resId : data.id,
			dataSource : 'tokenCode'
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				Ext.getCmp('numMemberSmsForNow').setText(jr.other.code);
				Ext.getCmp('numUsedCode').setText(jr.other.usedCode + '');
				Ext.getCmp('numUnUsedCode').setText(jr.other.unUsedCode + '');
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});		
	
	displayCodeWin.show();
}


function displayCodesHandler(){
	var data = Ext.ux.getSelData(restaurantPanel);
	if(!displayCodesWin){
		displayCodesWin = new Ext.Window({
			title : '&nbsp;',
			modal : true,
			closable : false,
			resizable : false,
			width : 200,
			minHeight : 100,
			autoHeight : true,
//			layout : 'fit',
			frame : true,
			items : [{
				layout : 'form',
				frame : true,
				minHeight : 150,
				style : 'text-align:center',
				id : 'loadUnUsedCodesCmp',
				items : []
			}],
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					displayCodesWin.hide();
					Ext.getCmp('loadUnUsedCodesCmp').removeAll();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					displayCodesWin.hide();
					Ext.getCmp('loadUnUsedCodesCmp').removeAll();
				}
			}]
		});
	}
	
	displayCodesWin.setTitle('可用验证码 -- ' + data['name']);
	//获取验证码
	Ext.Ajax.request({
		url : '../../OperateRestaurant.do',
		params : {
			resId : data.id,
			dataSource : 'getCodes'
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success && jr.root.length > 0){
				for (var i = 0; i < jr.root.length; i++) {
					var c = {items : [{
						xtype : 'label',
						style : 'color:green;font-size:25px;',
						text : jr.root[i].code
					}]};
					Ext.getCmp('loadUnUsedCodesCmp').add(c);
				}
				Ext.getCmp('loadUnUsedCodesCmp').doLayout();
				
				displayCodesWin.show();
			}else{
				Ext.example.msg(jr.title, "无可用验证码");
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});		
	
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
			Ext.getCmp('txtDianping').setValue(!data.dianping? "" : data.dianping);
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
		},{
			name : 'smsRemain'
		},{
			name : 'moduleDescs'
		}, {
			name : 'dianping'
		}, {
			name : 'usedCode'
		}, {
			name : 'unUsedCode'
		}, {
			name : 'qrCode'
		},{
			name : 'typeVal'
		},{
			name : 'branches'
		},{
			name : 'typeText'
		}])
	});
	
	var cm = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header : '餐厅编号', dataIndex : 'id',width : 40},
		{header : '餐厅类型', dataIndex : 'typeText',width : 70},
		{header : '账户名', dataIndex : 'account',width : 70},
		{header : '创建时间', dataIndex : 'birthDate'},
		{header : '账号有效期', dataIndex : 'expireDate'},
		{header : '餐厅名', dataIndex : 'name', width : 150},
		{header : '活跃度', dataIndex : 'liveness'},
		{header : '账单有效期', dataIndex : 'recordAliveText'},
		{header : '授权模块', dataIndex : 'moduleDescs',width : 180},
		{header : '短信', dataIndex : 'smsRemain', align : 'right', renderer : optSms},
		{header : '已用验证码', dataIndex : 'usedCode', align : 'right'},
		{header : '可用验证码', dataIndex : 'unUsedCode', align : 'right', renderer:unUsedCode},
		{header : '操作', dataIndex : 'optRestaurant', id : 'optRestaurant',width : 300, align : 'center', renderer : optRestaurant}
		
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
		loadMask : {msg : '加载中.....'},
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
    ds.load();
});
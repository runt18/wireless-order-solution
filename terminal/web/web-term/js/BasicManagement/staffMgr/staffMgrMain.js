
function setParentNodeCheckState(node){
	var parentNode = node.parentNode;  
	if (parentNode != null) {  
		var checkBox = parentNode.getUI().checkbox;  
		var isAllChildChecked = true;  
		var someChecked = false; 
		var childCount = parentNode.childNodes.length;  
		for (var i = 0; i < childCount; i++) {  
			var child = parentNode.childNodes[i]; 
			if (child.attributes.checked) { 
				someChecked = true; 
			}	else if (child.getUI().checkbox.indeterminate == true && child.getUI().checkbox.checked == false) { 
				someChecked = true;  
				isAllChildChecked = false; 
				break; 
			}else { 
				isAllChildChecked = false; 
			}  
		}
		
		if (isAllChildChecked && someChecked) {
			parentNode.attributes.checked = true;
			if (checkBox != null) {
				checkBox.indeterminate = false;
				checkBox.checked = true;
			}
			
		}else if (someChecked) {
			parentNode.attributes.checked = false;
			if (checkBox != null) {
				checkBox.indeterminate = true;
				checkBox.checked = false;
			}
			
		}else{
			parentNode.attributes.checked = false;
			if (checkBox != null) {
				checkBox.indeterminate = false;
				checkBox.checked = false;
			}
		}
		this.setParentNodeCheckState(parentNode);
		
	}

}

if(!changePwdWin){
	changePwdWin = new Ext.Window({
		layout : 'fit',
		title : '重置密码',
		width : 280,
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
				id : 'txtOldpwd',
				width : 160
			}, {
				xtype : 'textfield',
				inputType : 'password',
				fieldLabel : '新密码',
				id : 'txtNewPwd',
				width : 160,
				allowBlank : false
			}, {
				xtype : 'textfield',
				inputType : 'password',
				fieldLabel : '确认新密码',
				id : 'txtConfirmNewPwd',
				width : 160,
				allowBlank : false
			}, {
				html : '<div style="margin-top:4px"><font id="errorMsgChangePwd" style="color:red;"> </font></div>'
			}]
		}],
		bbar : [ '->', {
			text : '保存',
		    	id : 'btnSaveUpdatePassWord',
		    	iconCls : 'btn_save',
				handler : function() {
					if(Ext.getCmp('txtNewPwd').isValid() && Ext.getCmp('txtConfirmNewPwd').isValid()){
						var oldPwd = Ext.getCmp('txtOldpwd').getValue();
						var newPwd = Ext.getCmp('txtNewPwd').getValue();
						var confirmPwd = Ext.getCmp('txtConfirmNewPwd').getValue();
						var ss = Ext.getCmp('staffGrid').getSelectionModel().getSelected();
						
						var staffID = staffStore.getAt(currRowIndex).get('staffID');
						//var password = staffStore.getAt(currRowIndex).get('staffPassword');
						
//						if(password == MD5(oldPwd)){
							if (newPwd == confirmPwd){
								changePwdWin.hide();
								
								Ext.Ajax.request({
									url : '../../UpdateStaff.do',
									params : {
										'oldPwd' : MD5(oldPwd),
										'staffId' : staffID,
										'staffPwd' : newPwd
									},
									success : function(response, options) {
										var jr = Ext.util.JSON.decode(response.responseText);
										if (jr.success) {
											staffStore.load({
												params : {
													start : 0,
													limit : pageRecordCount
												}
											});
											
											Ext.ux.showMsg(jr);
										} else {
											Ext.ux.showMsg(jr);
										}
								},
								failure : function(response, options) {
									
								}
							});
						} else {
							Ext.example.msg('提示', '操作失败, 两次密码不一致, 请重新输入.');
						}
/*					} else {
						Ext.example.msg('提示', '操作失败, 原密码不正确, 请重新输入.');
					}*/
				}
			}
		}, {
			text : '关闭',
			id : 'btnCloseUpdatePassWord',
			iconCls : 'btn_close',
			handler : function() {
				changePwdWin.hide();
			}
		}],
		listeners : {
			'show' : function(thiz) {
				Ext.getCmp('txtOldpwd').setValue('');
				Ext.getCmp('txtNewPwd').setValue('');
				Ext.getCmp('txtNewPwd').clearInvalid();
				Ext.getCmp('txtConfirmNewPwd').setValue('');
				Ext.getCmp('txtConfirmNewPwd').clearInvalid();
				Ext.getCmp('txtOldpwd').focus(true, 100); 
			}
		}
	});
}


// --------------------------------------------------------------------------
/*var staffAddBut = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddForBigBar.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加员工',
	handler : function(btn) {
		staffAddWin.show();
		staffAddWin.center();
	}
});*/

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
						location.href = 'SystemProtal.html?'+ strEncode('restaurantID=' + restaurantID, KEYS);
					}
				}
			});
		} else {
			location.href = 'SystemProtal.html?'+ strEncode('restaurantID=' + restaurantID, KEYS);
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

var addStaff = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddSaff.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加员工',
	handler : function(){
		operateStaff({otype : 'insert'});
	}
});

var addRole = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddRole.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加角色',
	handler : function(){
		operateRole({otype : 'insert'});
	}
});

function deleteStaff(){
	var ss = Ext.getCmp('staffGrid').getSelectionModel().getSelected();
	if(ss != null){
		Ext.Msg.confirm(
			'提示',
			'是否刪除' + ss.data.staffName + '?',
			function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../DeleteStaff.do',
						params : {
							isCookie : true,
							staffId : ss.data.staffID
						},
						success : function(res, opt){
							Ext.getCmp('staffGrid').store.reload();
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
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
		);

	}
}

function changePwdHandler(rowIndex) {
	changePwdWin.show();
};

function staffOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	if(eval(record.get('typeValue') == 2)){
		return '<a href="javascript:changePwdHandler(' + rowIndex + ')"><img src="../../images/Modify.png"/>修改</a>';
	}else{
		return "<a href = \"javascript:operateStaff({otype:'update'})\">" + "<img src='../../images/Modify.png'/>修改</a>"
		 +"&nbsp;&nbsp;&nbsp;"
		 + "<a href=\"javascript:void(0);\" onclick=\"deleteStaff()\">" + "<img src='../../images/del.png'/>删除</a>";
	}
};
function roleOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	if(eval(record.get('categoryValue')) == 1 || eval(record.get('categoryValue') == 2)){
		return '系统保留';
	}else{
		return ''
		+ "<a href = \"javascript:operateRole({otype:'update'})\">" + "<img src='../../images/Modify.png'/>修改</a>"
		+ '&nbsp;&nbsp;&nbsp;'
		+ "<a href = \"javascript:operateRole({otype:'delete'})\">" + "<img src='../../images/del.png'/>删除</a>";
	}
};

// 1，员工的数据store
var staffStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({method:'post', url : '../../QueryStaff.do'}),
	reader : new Ext.data.JsonReader({
		totalProperty : 'totalProperty',
		root : 'root'
	}, [ {
		name : 'staffID'
	}, {
		name : 'staffName'
	}, {
		name : 'mobile'
	}, {
		name : 'restaurantId'
	},{
		name : 'typeValue'
	},{
		name : 'typeText'
	},{
		name : 'role'
	},{
		name : 'roleName'
	} ]),
	baseParams:{
		"restaurantID" : restaurantID,
		"type" : 0,
		"isPaging" : false,
		"isCombo" : false,
		"hasDetail" : true
	}
});
staffStore.load({
	params : {
		start : 0,
		limit : pageRecordCount
	}
});

// 2，员工列模型
var staffColumnModel = new Ext.grid.ColumnModel([ 
     new Ext.grid.RowNumberer(), 
    {header : '员工名称', dataIndex : 'staffName', width : 150},
    {header : '联系电话', dataIndex : 'mobile', hidden: true},
    {header : '角色', dataIndex : 'roleName', width : 150},
	{
    	id : 'staffOpt',
		header : '操作',
		dataIndex : 'staffOpt',
		width : 200,
		align : 'center',
		renderer : staffOpt
	} ]);

var roleArray = []; 
//角色列模型
var roleModel = new Ext.grid.ColumnModel([
      {header : '角色名称', dataIndex : 'name', width : 90},
      {header : '操作', id : 'roleOpt', dataIndex : 'roleOpt', width : 150, align : 'center', renderer : roleOpt}
]);
//角色Store
var roleStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : '../../QueryRole.do',
		method : 'post'
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : 'totalProperty',
		root : 'root'
	}, [
		{name : 'id'},
		{name : 'restaurantId'},
		{name : 'name'},
		{name : 'categoryValue'},
		{name : 'categoryText'},
		{name : 'typeValue'},
		{name : 'typeText'}
	]),
	listeners : {
		load : function(s, r, o){
			roleArray = [];
			for (var i = 1; i < r.length; i++) {
				roleArray.push([r[i].get('id'), r[i].get('name')]);
			}
			if(Ext.getCmp('combChooseRole')){
				Ext.getCmp('combChooseRole').store.loadData(roleArray);
			}
		}
	}
});
roleStore.load();

var chooseRoleComb = new Ext.form.ComboBox({
	fieldLabel : '选择角色',
	forceSelection : true,
	width : 160,
	id : 'combChooseRole',
	store : new Ext.data.SimpleStore({
		fields : [ 'id', 'name']
	}),
	valueField : 'id',
	displayField : 'name',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	allowBlank : false,
	readOnly : false
});

var referRoleComb = new Ext.form.ComboBox({
	fieldLabel : '参照角色',
	forceSelection : true,
	width : 130,
	id : 'combReferRole',
	store : roleStore,
	valueField : 'id',
	displayField : 'name',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	readOnly : false,
	listeners : {
		render : function(thiz){
			roleStore.reload();
		}	
	}
});

var searchRoleComb = new Ext.form.ComboBox({
	forceSelection : true,
	width : 120,
	id : 'combSearchRole',
	store : roleStore,
	valueField : 'id',
	displayField : 'name',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	readOnly : false,
	hidden : true,
	listeners : {
		select : function(){
			Ext.getCmp('btnSearch').handler();
		
		}
	}
});


//权限Store
var privilegeStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		method:'post',
		url : '../../QueryPrivilege.do'
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : 'totalProperty',
		root : 'root'
	}, [
		{name : 'id'},
		{name : 'codeValue'},
		{name : 'codeText'},
		{name : 'discounts'}
	])
});

//privilegeStore.load();
//权限复选框
var privilegeSM = new Ext.grid.CheckboxSelectionModel({
	handleMouseDown : Ext.emptyFn,
	dataIndex : 'codeValue'
});

//权限列模型
var privilegeModel = new Ext.grid.ColumnModel([
      {header : '权限码', dataIndex : 'codeValue', width : 80},
      {header : '描述', dataIndex : 'codeText', width : 100},
      privilegeSM
]);

function operateStaff(c){
	if(c.otype == 'undefined'){
		return;
	}
	
	if(c.otype == 'insert'){
		staffAddWin.setTitle('添加员工');
		staffAddWin.operationType = c.otype;
		staffAddWin.show();
		staffAddWin.center();
		Ext.getCmp('txtStaffName').focus(true, 100);
		
	}else if(c.otype == 'update'){
		var ss = Ext.getCmp('staffGrid').getSelectionModel().getSelected();
		staffAddWin.show();
		staffAddWin.operationType = c.otype;
		staffAddWin.setTitle('修改员工');
		Ext.getCmp('txtStaffName').focus(true, 100);
		Ext.getCmp('txtStaffName').setValue(ss.data.staffName);
		var psw = Ext.getCmp('txtStaffPwd');
		var confirmPsw = Ext.getCmp('txtStaffPwdConfirm');
		Ext.getCmp('txtPhone').setValue(ss.data.mobile);
		Ext.getCmp('txtStaffId').setValue(ss.data.staffID);
		psw.setValue(encrypt);
		confirmPsw.setValue(encrypt);
		Ext.getCmp('combChooseRole').setValue(ss.data.role.id);
		
	}
}

function operateRole(c){
	if(c.otype == 'undefined'){
		return;
	}
	
	if(c.otype == 'insert'){
		roleAddWin.show();
		roleAddWin.operationType = c.otype;
		roleAddWin.setTitle('添加角色');
		Ext.getCmp('txtRoleName').focus(true, 100);
	}else if(c.otype == 'update'){
		roleAddWin.show();
		Ext.getCmp('txtRoleName').focus(true, 100);
		roleAddWin.operationType = c.otype;
		roleAddWin.setTitle('修改角色');
		var ss = roleGrid.getSelectionModel().getSelected();
		Ext.getCmp('txtRoleName').setValue(ss.data.name);
		Ext.getCmp('txtRoleId').setValue(ss.data.id);
		Ext.getCmp('combReferRole').setValue(ss.data.categoryText);
		
		Ext.getCmp('combReferRole').disable();
		
	}else if(c.otype == 'delete'){
		var ss = Ext.getCmp('roleGrid').getSelectionModel().getSelected();
		if(ss != null){
			Ext.Msg.confirm(
				'提示',
				'是否刪除角色?',
				function(e){
					if(e == 'yes'){
						Ext.Ajax.request({
							url : '../../OperateRole.do',
							params : {
								isCookie : true,
								roleId : ss.data.id,
								dataSource : 'delete'
							},
							success : function(res, opt){
								Ext.getCmp('roleGrid').store.reload();
								var jr = Ext.decode(res.responseText);
								if(jr.success){
									Ext.example.msg(jr.title, jr.msg);
									Ext.getDom('roleTbarName').innerHTML = '角色权限';
									privilegeTree.loader.baseParams = {dataSource : 'tree'};
									privilegeTree.getRootNode().reload();
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
			);
		}
	}
}

//----------------- 添加員工  --------------------
if(!staffAddWin){
	staffAddWin = new Ext.Window({
		title : '添加员工',
		width : 280,
		closeAction : 'hide',
		modal : true,
		resizable : false,
		closable : false,
		items : [{
			layout : 'form',
			id : 'staffAddWin',
			width : 280,
			labelWidth : 60,
			border : false,
			frame : true,
			items : [{
				xtype : 'textfield',
				id : 'txtStaffId',
				hideLabel : true,
				hidden : true
			},{
				xtype : 'textfield',
				fieldLabel : '姓名',
				id : 'txtStaffName',
				allowBlank : false,
				width : 160
			}, {
				xtype : 'textfield',
				inputType : 'password',
				fieldLabel : '密码',
				id : 'txtStaffPwd',
				allowBlank : false,
				width : 160,
				listeners : {
					focus : function(thiz){
						thiz.focus(true, 100);
					}
				}
			}, {
				xtype : 'textfield',
				inputType : 'password',
				fieldLabel : '确认密码',
				id : 'txtStaffPwdConfirm',	
				allowBlank : false,
				width : 160,
				listeners : {
					focus : function(thiz){
						thiz.focus(true, 100);
					}
				}
			}, {
				xtype : 'textfield',
				id : 'txtPhone',
				fieldLabel : '员工电话',
				width : 160,
				regex : Ext.ux.RegText.phone.reg,
				regexText : Ext.ux.RegText.phone.error,
				disabled : true
			}, chooseRoleComb]
		}],
		bbar : ['->',{
			text : '确定',
			id : 'btnSaveStaff',
			iconCls : 'btn_save',
			handler : function(){
				
				if (Ext.getCmp('txtStaffName').isValid()
						&& Ext.getCmp('txtStaffPwd').isValid()
						&& Ext.getCmp('txtStaffPwdConfirm').isValid() && Ext.getCmp('txtPhone').isValid()) {
	
					var staffName = Ext.getCmp('txtStaffName').getValue();
					var staffAddPwd = Ext.getCmp('txtStaffPwd').getValue();
					var staffAddPwdCon = Ext.getCmp('txtStaffPwdConfirm').getValue();
					var roleId = Ext.getCmp('combChooseRole').getValue();
					var tele = Ext.getCmp('txtPhone').getValue();
					var staffId = Ext.getCmp('txtStaffId').getValue();
					var url = '';
					
					if(staffAddWin.operationType == 'insert'){
						url = 'InsertStaff.do';
					}else if(staffAddWin.operationType == 'update'){
						url = 'UpdateStaff.do';
					}
					
					if(staffAddPwd == staffAddPwdCon){
						if(staffAddPwd == encrypt){
							staffAddPwd = '';
						}
						Ext.Ajax.request({
							url : '../../' + url,
							params : {
								'staffName' : staffName,
								'staffPwd' : staffAddPwd,
								'roleId' : roleId,
								'tele' : tele,
								'staffId' : staffId
							},
							success : function(response, options) {
								var jr = Ext.util.JSON.decode(response.responseText);
								if(jr.success){
									staffStore.load({
										params : {
											start : 0,
											limit : pageRecordCount
										}
									});
	
									Ext.ux.showMsg(jr);
									staffAddWin.hide();
								}else{
									Ext.ux.showMsg(jr);
								}
							},
							failure : function(response, options) {
								
							}
						});
					}else{
						Ext.MessageBox.show({
							title : '提示',
							msg : '确认密码不一致',
							icon : Ext.MessageBox.WARNING,
							buttons : Ext.Msg.OK,
							closable : false
						});
					}
	
				}else{
					return
				}
			}
		}, {
			text : '取消',
			id : 'btnCloseStaff',
			iconCls : 'btn_close',
			handler : function(){
				staffAddWin.hide();
			}
		}],
		listeners : {
			'show' : function(thiz){
				Ext.getCmp('txtStaffName').setValue('');
				Ext.getCmp('txtStaffName').clearInvalid();
	
				Ext.getCmp('txtStaffPwd').setValue('');
				Ext.getCmp('txtStaffPwd').clearInvalid();
	
				Ext.getCmp('txtStaffPwdConfirm').setValue('');
				Ext.getCmp('txtStaffPwdConfirm').clearInvalid();
				
				Ext.getCmp('combChooseRole').setValue('');
				Ext.getCmp('combChooseRole').clearInvalid();
				
				Ext.getCmp('txtPhone').setValue('');
	
			}
		},
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp("btnSaveStaff").handler();
			}
		}]
	});
	
}


if(!roleAddWin){
	roleAddWin = new Ext.Window({
		title : '添加角色',
		width : 280,
		modal : true,
		resizable : false,
		closable : false,
		items : [{
			layout : 'form',
			id : 'roleAddWin',
			labelWidth : 60,
			width : 280,
			border : false,
			frame : true,
			items : [{
				xtype : 'textfield',
				id : 'txtRoleId',
				hideLabel : true,
				hidden : true
			},{
				xtype : 'textfield',
				fieldLabel : '角色名称',
				id : 'txtRoleName',
				allowBlank : false,
				width : 130
			}, referRoleComb]
		}],
		bbar : ['->',{
			text : '确定',
			id : 'btnAddRole',
			iconCls : 'btn_save',
			handler : function(){
				if(Ext.getCmp('txtRoleName').isValid()){
					var roleId = Ext.getCmp('txtRoleId').getValue();
					var roleName = Ext.getCmp('txtRoleName').getValue();
					var roleModelId = Ext.getCmp('combReferRole').getValue();
					var dataSource = '';
					if(roleAddWin.operationType == 'insert'){
						dataSource = 'insert';
					}else if(roleAddWin.operationType == 'update'){
						dataSource = 'update';
					}
					Ext.Ajax.request({
						url : '../../OperateRole.do',
						params : {
							dataSource : dataSource,
							roleName : roleName,
							roleId : roleId, 
							modelId : roleModelId
						},
						success : function(res, opt){
							Ext.getCmp('roleGrid').store.reload();
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								roleAddWin.hide();
								Ext.example.msg(jr.title, jr.msg);
								if(dataSource == 'update'){
									Ext.getCmp('staffGrid').store.reload();
									Ext.getCmp('roleGrid').getSelectionModel().selectFirstRow();
								}else{
									Ext.getCmp('roleGrid').getSelectionModel().selectLastRow();
								}
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
		},{
			text : '取消',
			id : 'btnClose',
			iconCls : 'btn_close',
			handler : function(){
				roleAddWin.hide();
			}
		}],
		listeners : {
			show : function(){
				Ext.getCmp('txtRoleName').setValue('');
				Ext.getCmp('txtRoleName').clearInvalid();
				
				Ext.getCmp('combReferRole').setValue('');
				Ext.getCmp('combReferRole').clearInvalid();
				Ext.getCmp('combReferRole').enable();
			}
		},
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp("btnAddRole").handler();
			}
		}]
		
	});
}


var spm_filterComb = new Ext.form.ComboBox({
	forceSelection : true,
	width : 100,
	value : '全部',
	id : 'spm_comboFilter',
	store : new Ext.data.SimpleStore({
		fields : [ 'value', 'text' ],
		data : filterTypeDate
	}),
	valueField : 'value',
	displayField : 'text',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	allowBlank : false,
	readOnly : false,
	listeners : {
		select : function(combo, record, index){
			var name = Ext.getCmp('txtSearchName');
			
			if(index == 0){
				name.setVisible(false);
				name.setValue('');
				searchRoleComb.setVisible(false);
				searchRoleComb.setValue('');
				
			}else if (index == 1){
				name.setValue('');
				searchRoleComb.setValue('');
				name.setVisible(true);
				searchRoleComb.setVisible(false);
				
			}else if(index == 2){
				name.setValue('');
				searchRoleComb.setValue('');
				name.setVisible(false);
				searchRoleComb.setVisible(true);
			};
		}
	}
	
});
// -------------- layout ---------------
var staffGrid, roleGrid, privilegeTree;
var selectChange = false;
var selected, beforeSelected = null;
Ext.onReady(function() {
	staffGrid = new Ext.grid.GridPanel({
		xtype : 'grid',
		id : 'staffGrid',
		width : 500,
		region : 'west',
		frame : true,
		ds : staffStore,
		cm : staffColumnModel,
		autoExpandColumn : 'staffOpt',
		autoScroll : true,
		loadMask : { msg : '数据加载中，请稍等...' },
		viewConfig : {
			forceFit : true
		},
		listeners : {
			'rowclick' : function(thiz, rowIndex, e) {
				currRowIndex = rowIndex;
			},
			celldblclick : function(thiz, rowIndex, columIndex, e){
				var record = thiz.getStore().getAt(rowIndex);
				if(record.get('type') == 1){
					Ext.example.msg('提示','系统保留用户为<font color="red">管理员</font>,不允许修改任何信息.');
					return false;
				}
			}
		},
		tbar : [{
				xtype : 'tbtext',
				text : '过滤: '
			},{xtype : 'tbtext',text : '&nbsp;&nbsp;'},
			spm_filterComb,
			{xtype : 'tbtext',text : '&nbsp;&nbsp;'},
			{
				xtype : 'textfield',
				id : 'txtSearchName',
				width : 120,
				hidden : true
			},searchRoleComb,'->', {
				id : 'btnSearch',
				text : '搜索',
				iconCls : 'btn_search',
				handler : function(){
					var store = staffGrid.getStore();
					store.baseParams['name'] = Ext.getCmp('txtSearchName').getValue();
					store.baseParams['cate'] = Ext.getCmp('combSearchRole').getValue();
					store.load();
				}
			},{
			text : '添加',				
			iconCls : 'btn_add',
			id : 'tbarAddStaff',
			handler : function(){
				operateStaff({otype : 'insert'});
			}
		}],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnSearch').handler();
			}
		}]

	});
	
	roleGrid = new Ext.grid.GridPanel({
		xtype : 'grid',
		id : 'roleGrid',
		anchor : '100%',
		region : 'center',
		frame : true,
		autoExpandColumn : 'roleOpt',
		ds : roleStore,
		cm : roleModel,
		tbar : [{
			xtype : 'tbtext',
			text : '角色管理'
		},'->',{
			text : '添加',				
			iconCls : 'btn_add',
			id : 'tbarAddRole',
			handler : function(){
				operateRole({otype : 'insert'});
			}
		}],
		listeners : {
			rowclick : function(thiz, rowIndex, e){
				selected = thiz.getStore().getAt(rowIndex).get('id');
				if(selectChange){
					Ext.Msg.confirm('提示', '是否保存权限的修改?', function(e){
						if(e == "yes"){
							Ext.getCmp('savePrivilage').handler();
						}else{
							selectChange = false;
							Ext.getDom('change').innerHTML = '&nbsp;';
						}
						beforeSelected = selected;
						if(thiz.getStore().getAt(rowIndex).get('name') == '管理员' || thiz.getStore().getAt(rowIndex).get('name') == '老板'){
							privilegeTree.disable();
						}else{
							privilegeTree.enable();
						}
						Ext.getDom('roleTbarName').innerHTML = thiz.getStore().getAt(rowIndex).get('name'); 
						
						privilegeTree.loader.dataUrl = "../../QueryPrivilege.do";
						privilegeTree.loader.baseParams = {dataSource : 'roleTree', roleId : thiz.getStore().getAt(rowIndex).get('id')};
						privilegeTree.getRootNode().reload();
						
					});
				}else{
					if(thiz.getStore().getAt(rowIndex).get('name') == '管理员' || thiz.getStore().getAt(rowIndex).get('name') == '老板'){
						privilegeTree.disable();
					}else{
						privilegeTree.enable();
					}
					Ext.getDom('roleTbarName').innerHTML = thiz.getStore().getAt(rowIndex).get('name'); 
					
					privilegeTree.loader.dataUrl = "../../QueryPrivilege.do";
					privilegeTree.loader.baseParams = {dataSource : 'roleTree', roleId : thiz.getStore().getAt(rowIndex).get('id')};
					privilegeTree.getRootNode().reload();
					beforeSelected = selected;
				}
			}
		}
	});
	
	privilegeTree = new Ext.tree.TreePanel({
		id : 'privilegeTree',   
		region : 'east',
		width : '18%',
		border : false,
		rootVisible : true,
		autoScroll : true,
		frame : true,
		
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		root : new Ext.tree.AsyncTreeNode({
            expanded: true,
            text : '权限列表',
            code : '-2',
            leaf : false
           // checked : false,
		}),
		listeners : {
			'checkchange':function(node, checked){
				selectChange = true;
				Ext.getDom('change').innerHTML = '*';
				node.expand(); 
				node.attributes.checked = checked; 
				node.eachChild(function(child) { 
					child.ui.toggleCheck(checked);  
					child.attributes.checked = checked; 
					child.fireEvent('checkchange', child, checked); 
				}); 
				setParentNodeCheckState(node);
				//取消所有折扣, 默认选中无折扣
				if(node.attributes.text == '折扣'){
					if(!node.firstChild.attributes.checked){
						node.firstChild.getUI().checkbox.checked = true;
						node.firstChild.fireEvent('checkchange', node.firstChild, true); 
					}
				}
			}
		},
		tbar :	[{
			xtype : 'tbtext',
			text : String.format(
					Ext.ux.txtFormat.tbarName,
					'change', '&nbsp;','roleTbarName','角色权限'
			)
		}, {
			xtype : 'tbtext',
			text : '&nbsp;'
		},{
			text : '刷新',
			iconCls : 'btn_refresh',
			handler : function(){
				privilegeTree.getRootNode().reload();
			}
		}, '->',{
			text : '保存',
			id : 'savePrivilage',
			iconCls : 'btn_save',
			handler : function(){
				selectChange = false;
				Ext.getDom('change').innerHTML = '&nbsp;';
				if(roleGrid.getSelectionModel().getSelected()){
					var checkedNodes = privilegeTree.getChecked();
					var discount = '', pricePlan = '', privilege = '';
					for(var i=0;i<checkedNodes.length;i++){
						if(checkedNodes[i].attributes.isDiscount){
							if(discount != ''){
								discount += ',';
							}
							discount += checkedNodes[i].attributes.discountId;
						}else if(checkedNodes[i].attributes.isPricePlan){
							if(pricePlan != ''){
								pricePlan += ',';
							}
							pricePlan += checkedNodes[i].attributes.planId;						
						}else if(checkedNodes[i].attributes.pId){
							if(privilege != ''){
								privilege += ',';
							}
							privilege += checkedNodes[i].attributes.code;
						}
						
					}
					Ext.Ajax.request({
						url : '../../OperateRole.do',
						params : {
							isCookie : true,
							dataSource : 'updatePrivilege',
							discounts : discount,
							pricePlans : pricePlan,
							privileges : privilege,
							roleId : beforeSelected == null?roleGrid.getSelectionModel().getSelected().data.id : beforeSelected
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
							}else{
								Ext.example.msg(jr.title, jr.msg);
							}
							
						},
						failure : function(res, opt){
							var jr = Ext.decode(res.responseText);
							
							Ext.ux.showMsg(jr);
						}
					});
				}else{
					Ext.example.msg('提示', '还未选择角色');
				}
			}
		}
		 ]
	});
	
	privilegeTree.loader = new Ext.tree.TreeLoader({
		dataUrl : '../../QueryPrivilege.do',
		baseParams : {
			restaurantID : restaurantID,
			dataSource : 'tree'
		},
		listeners : {
			load : function(thiz, node, res){
				node.eachChild(function(child) { 
					
/*					var checked = child.attributes.checked;
					child.ui.toggleCheck(checked);  
					child.attributes.checked = checked;*/
					if(child.hasChildNodes()){
						child.expand();
						child.eachChild(function(childSon) { 
/*							checked = childSon.attributes.checked;
							childSon.ui.toggleCheck(checked);  
							childSon.attributes.checked = checked;*/
							setParentNodeCheckState(childSon);
							if(childSon.hasChildNodes()){
								childSon.expand();
								childSon.eachChild(function(grandSon){
									setParentNodeCheckState(grandSon);
								})
							}
						});
					}
				}); 
			}
		}
	});
	
	new Ext.Panel({
		renderTo : 'divStaff',
		//width : parseInt(Ext.getDom('divStaff').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divStaff').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',
		items : [ staffGrid, roleGrid, privilegeTree ],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [addStaff, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;&nbsp;'
			}, addRole
			]
		})
	});

});

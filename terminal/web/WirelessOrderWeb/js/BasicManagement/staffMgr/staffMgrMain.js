
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
	bbar : [
		'->',
		{
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
					var password = staffStore.getAt(currRowIndex).get('staffPassword');
					
					if(password == MD5(oldPwd)){
						if (newPwd == confirmPwd){
							changePwdWin.hide();
							
							Ext.Ajax.request({
								url : '../../UpdateStaff.do',
								params : {
									'staffName' : '管理员',
									'staffId' : staffID,
									'staffPwd' : newPwd,
									'roleId' : ss.data.role.id
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
						Ext.example.msg('提示', '操作失败, 两次密码已一致, 请重新输入.');
					}
				} else {
					Ext.example.msg('提示', '操作失败, 原密码不正确, 请重新输入.');
				}
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
			Ext.getCmp('txtOldpwd').setValue('');
			
			
			Ext.getCmp('txtNewPwd').setValue('');
			Ext.getCmp('txtNewPwd').clearInvalid();
			Ext.getCmp('txtConfirmNewPwd').setValue('');
			Ext.getCmp('txtConfirmNewPwd').clearInvalid();
			var f = Ext.get('txtOldpwd');
			f.focus.defer(true, 100); 
		}
	}
});

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
						location.href = 'SystemProtal.html?'+ strEncode('restaurantID=' + restaurantID, 'mi');
					}
				}
			});
		} else {
			location.href = 'SystemProtal.html?'+ strEncode('restaurantID=' + restaurantID, 'mi');
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
	imgPath : '../../images/btnAddSupplier.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加员工',
	handler : function(){
		operateStaff({otype : 'insert'});
	}
});

var addRole = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddSupplier.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加角色',
	handler : function(){
		operateRole({otype : 'insert'});
	}
});





/*var searchForm = new Ext.Panel({
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
};*/

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
	},{
		name : 'staffPassword'
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
		"isPaging" : true,
		"isCombo" : false
	}
});
staffStore.load({
	params : {
		start : 0,
		limit : pageRecordCount
	}
});

/*var noLimitCheckColumn = new Ext.grid.CheckColumn({
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
});*/

// 2，员工列模型
var staffColumnModel = new Ext.grid.ColumnModel([ 
     new Ext.grid.RowNumberer(), 
    {header : '员工名称', dataIndex : 'staffName'},
    {header : '联系电话', dataIndex : 'mobile'},
    {header : '角色', dataIndex : 'roleName'},
	{
    	id : 'staffOpt',
		header : '操作',
		dataIndex : 'staffOpt',
		width : 150,
		align : 'center',
		renderer : staffOpt
	} ]);

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
	])
});
roleStore.load();

var chooseRoleComb = new Ext.form.ComboBox({
	fieldLabel : '选择角色',
	forceSelection : true,
	width : 160,
	id : 'combChooseRole',
	store : roleStore,
	valueField : 'id',
	displayField : 'name',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	allowBlank : false,
	readOnly : true,
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
	readOnly : true,
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
staffAddWin = new Ext.Window({
	title : '添加员工',
	width : 260,
	closeAction : 'hide',
	modal : true,
	resizable : false,
	closable : false,
	items : [{
		layout : 'form',
		id : 'staffAddWin',
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
			regexText : Ext.ux.RegText.phone.error
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


var roleAddWin = new Ext.Window({
	title : '添加角色',
	width : 260,
	modal : true,
	resizable : false,
	closable : false,
	items : [{
		layout : 'form',
		id : 'roleAddWin',
		labelWidth : 60,
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
							Ext.getCmp('roleGrid').getSelectionModel().selectFirstRow();
							//Ext.getCmp('roleGrid').getSelectionModel().selectLastRow();
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
// -------------- layout ---------------
var staffGrid, roleGrid, privilegeTree;
Ext.onReady(function() {
	// 解决ext中文传入后台变问号问题
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();

	// ---------------------表格--------------------------
	staffGrid = new Ext.grid.GridPanel({
		xtype : 'grid',
		id : 'staffGrid',
		anchor : '100%',
		width : '60%',
		region : 'west',
		frame : true,
		margins : '0 0 0 0',
		ds : staffStore,
		cm : staffColumnModel,
		autoExpandColumn : 'staffOpt',
		autoScroll : true,
		loadMask : { msg : '数据加载中，请稍等...' },
		//plugins : noLimitCheckColumn,
/*		sm : new Ext.grid.RowSelectionModel({
			singleSelect : true
		}),*/
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
			text : '员工管理'
		},'->',
		{
			text : '添加',				
			iconCls : 'btn_add',
			id : 'tbarAddStaff',
			handler : function(){
				operateStaff({otype : 'insert'});
			}
		}],
		bbar : new Ext.PagingToolbar({
			pageSize : pageRecordCount,
			store : staffStore,
			displayInfo : true,
			displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
			emptyMsg : '没有记录'
		})

	});
	
	roleGrid = new Ext.grid.GridPanel({
		xtype : 'grid',
		id : 'roleGrid',
		anchor : '100%',
		width : '20%',
		region : 'center',
		frame : true,
		margins : '0 0 0 0',
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
				if(thiz.getStore().getAt(rowIndex).get('name') == '管理员' || thiz.getStore().getAt(rowIndex).get('name') == '老板'){
					privilegeTree.disable();
				}else{
					privilegeTree.enable();
				}
				Ext.getDom('roleTbarName').innerHTML = thiz.getStore().getAt(rowIndex).get('name'); 
				
				privilegeTree.loader.dataUrl = "../../QueryPrivilege.do";
				privilegeTree.loader.baseParams = {dataSource : 'roleTree', roldId : thiz.getStore().getAt(rowIndex).get('id')};
				privilegeTree.getRootNode().reload();
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
            pId : '-2',
            leaf : false,
           // checked : false,
		}),
		listeners : {
			'checkchange':function(node, checked){
				node.expand(); 
				node.attributes.checked = checked; 
				node.eachChild(function(child) { 
					child.ui.toggleCheck(checked);  
					child.attributes.checked = checked; 
					child.fireEvent('checkchange', child, checked); 
				}); 
				setParentNodeCheckState(node);
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
					'roleTbarName','角色权限'
			)
		}, {
			text : '刷新',
			iconCls : 'btn_refresh',
			handler : function(){
				privilegeTree.getRootNode().reload();
			}
		}, '->',{
			text : '保存',
			iconCls : 'btn_save',
			handler : function(){
				if(roleGrid.getSelectionModel().getSelected()){
					var checkedNodes = privilegeTree.getChecked();
					var discount = '', privilege = '';
					for(var i=0;i<checkedNodes.length;i++){
						if(checkedNodes[i].attributes.isDiscount){
							if(discount != ''){
								discount += ',';
							}
							discount += checkedNodes[i].attributes.discountId;
						}else{
							if(privilege != ''){
								privilege += ',';
							}
							privilege += checkedNodes[i].attributes.pId;
						}
						
					}
					Ext.Ajax.request({
						url : '../../OperateRole.do',
						params : {
							dataSource : 'updatePrivilege',
							discounts : discount,
							privileges : privilege,
							roleId : roleGrid.getSelectionModel().getSelected().data.id
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
					
					var checked = child.attributes.checked;
					child.ui.toggleCheck(checked);  
					child.attributes.checked = checked;
					//alert("chii"+child.childNodes.length);
					if(child.hasChildNodes()){
						child.expand();
						child.eachChild(function(childSon) { 
							checked = childSon.attributes.checked;
							childSon.ui.toggleCheck(checked);  
							childSon.attributes.checked = checked;
							setParentNodeCheckState(childSon);
						});
					}
				}); 
			}
		}
	});
	
	
/*	staffGrid.getStore().on('beforeload', function() {
		
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
	});*/

	// 为store配置load监听器(即load完后动作)
/*	staffGrid.getStore().on('load', function() {
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
	});*/

/*	staffGrid.on('beforeedit', function(e){
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
	});*/
	
	// ---------------------end 表格--------------------------
	var centerPanel = new Ext.Panel({
		title : '员工操作',
		region : 'center',
		layout : 'border',
		frame : true,
		items : [ staffGrid, roleGrid, privilegeTree ],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [addStaff, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;&nbsp;'
			}, addRole,'->', 
				pushBackBut, 
				{
					xtype : 'tbtext',
					text : '&nbsp;&nbsp;&nbsp;'
				}, 
				logOutBut 
			]
		})
	});
	getOperatorName("../../");
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

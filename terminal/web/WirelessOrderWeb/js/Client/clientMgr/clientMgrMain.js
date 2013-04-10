var btnInsertClientType = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddClientType.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加客户类型',
	handler : function(e){
		insertClientTypeHandler();
	}
});

var btnInsertClient = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddClient.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加客户',
	handler : function(e){
		insertClientHandler();
	}
});

var btnPushBack = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(e){
		location.href = './ClientMain.html?restaurantID=' + restaurantID + '&isNewAccess=false&' + '&pin=' + pin;
	}
});

var btnLogOut =  new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '登出',
	handler : function(e){
		
	}
});

/**********************************************************************/
insertClientTypeHandler = function(){
	clientTypeOperationHandler({
		type : cmObj.operation['insert']
	});
};

updateClientTypeHandler = function(){
	clientTypeOperationHandler({
		type : cmObj.operation['update']
	});
};

deleteClientTypeHandler = function(){
	clientTypeOperationHandler({
		type : cmObj.operation['delete']
	});
};

clientTypeOperationHandler = function(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	clientTypeWin.otype = c.type;
	
	var clientTypeID = Ext.getCmp('numClientTypeID');
	var clientTypeName = Ext.getCmp('txtClientTypeName');
	var clientTypeParentID = Ext.getCmp('triggerClientTypeParentID');	
	
	if(c.type == cmObj.operation['insert']){
		clientTypeID.getEl().dom.parentElement.parentElement.style.display = 'none';
		
		clientTypeID.setValue();
		clientTypeName.setValue();
		clientTypeParentID.setValue();
		
		clientTypeID.clearInvalid();
		clientTypeName.clearInvalid();
		clientTypeParentID.clearInvalid();
		
		clientTypeWin.setTitle('添加客户类型');
		clientTypeWin.show();
		clientTypeWin.center();
	}else if(c.type == cmObj.operation['update']){
		var sn = clientTypeTree.getSelectionModel().getSelectedNode();
		if(!sn || sn.attributes.clientTypeID == -1){
			Ext.example.msg('提示', '请选中一个客户类型再进行操作.');
			return;
		}
		clientTypeID.getEl().dom.parentElement.parentElement.style.display = 'block';
		
		clientTypeID.setValue(sn.attributes.clientTypeID);
		clientTypeName.setValue(sn.attributes.clientTypeName);
		clientTypeParentID.setValue(sn);
		
		clientTypeWin.setTitle('修改客户类型');
		clientTypeWin.show();
		clientTypeWin.center();
	}else if(c.type == cmObj.operation['delete']){
		var sn = clientTypeTree.getSelectionModel().getSelectedNode();
		if(!sn || sn.attributes.clientTypeID == -1){
			Ext.example.msg('提示', '请选中一个客户类型再进行操作.');
			return;
		}
		if(sn.childNodes.length > 0){
			Ext.example.msg('提示', '该类型下还有子类型,不允许删除.');
			return;
		}
		Ext.Msg.show({
			title : '提示',
			msg : '是否删除客户类型?',
			buttons : Ext.Msg.YESNO,
			fn : function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../DeleteClientType.do',
						params : {
							restaurantID : restaurantID,
							typeID : sn.attributes.clientTypeID
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								clientTypeWin.hide();
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnRefreshClientTypeTree').handler();
								var parantID = Ext.getCmp('triggerClientTypeParentID');
								var typeByClient = Ext.getCmp('tirggerClietnTypeByClient');
								if(typeof parantID.reload != 'undefined')
									parantID.reload();
								if(typeof typeByClient.reload != 'undefined')
									typeByClient.reload();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
						}
					});
				}
			}
		});
	}
};


/**********************************************************************/
insertClientHandler = function(){
	clientOperationHandler({
		type : cmObj.operation['insert']
	});
};

updateClientHandler = function(){
	clientOperationHandler({
		type : cmObj.operation['update']
	});
};

deleteClientHandler = function(){
	clientOperationHandler({
		type : cmObj.operation['delete']
	});
};

clientOperationHandler = function(c){
	
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	clientWin.otype = c.type;
	
	if(c.type == cmObj.operation['insert']){
		bindClientData({
			sex:0
		});
		clientWin.setTitle('添加客户资料');
		clientWin.show();
		clientWin.center();
	}else if(c.type == cmObj.operation['update']){
		var sd = Ext.ux.getSelData(clientBasicGrid.getId());
		if(!sd){
			Ext.example.msg('提示', '请选中一个客户再进行操作.');
			return;
		}
		bindClientData(sd);
		clientWin.setTitle('修改客户资料');
		clientWin.show();
		clientWin.center();
	}else if(c.type == cmObj.operation['delete']){
		var sd = Ext.ux.getSelData(clientBasicGrid.getId());
		if(!sd){
			Ext.example.msg('提示', '请选中一个客户再进行操作.');
			return;
		}
		Ext.Msg.show({
			title : '提示',
			msg : ((sd.memberAccount > 0 ? '<font color="red">该客户已关联会员账号!</font><br/>' : '') + '是否删除客户?'),
			buttons : Ext.Msg.YESNO,
			fn : function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../DeleteClient.do',
						params : {
							restaurantID : restaurantID,
							clientID : sd.clientID
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnSearchClient').handler();
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
	}
	
};

/**
 * 
 */
bindClientData = function(data){
	var clientID = Ext.getCmp('munClientID');
	var clientName = Ext.getCmp('txtClientName');
	var clientType = Ext.getCmp('tirggerClietnTypeByClient');
	var clientSex = Ext.getCmp('comboClientSex');
	var clientMobile = Ext.getCmp('txtClientMobile');
	var clientTele = Ext.getCmp('txtClientTele');
	var clientBirthday = Ext.getCmp('dateClientBirthday');
	var clietnIDCard = Ext.getCmp('txtClientIDCard');
	var clientCompany = Ext.getCmp('txtClientCompany');
	var clientTastePref = Ext.getCmp('txtClientTastePref');
	var clietTaboo = Ext.getCmp('txtClientTaboo');
	var clientContactAddress = Ext.getCmp('txtClientContactAddress');
	var clientComment = Ext.getCmp('txtClientComment');
	
	clientID.setValue(data['clientID']);
	clientName.setValue(data['name']);
	clientType.setValue(data['clientType.typeID']);
	clientSex.setValue(data['sex']);
	clientMobile.setValue(data['mobile']);
	clientTele.setValue(data['tele']);
	if(typeof data['birthdayFormat'] != 'undefined' && data['birthdayFormat'].length > 0){
		clientBirthday.setValue(new Date(data['birthdayFormat'].replace(/-/g, '/')));
	}else{
		clientBirthday.setValue();
	}
	clietnIDCard.setValue(data['IDCard']);
	clientCompany.setValue(data['company']);
	clientTastePref.setValue(data['tastePref']);
	clietTaboo.setValue(data['taboo']);
	clientContactAddress.setValue(data['contactAddress']);
	clientComment.setValue(data['comment']);
	
	clientName.clearInvalid();
	clientType.clearInvalid();
	clientSex.clearInvalid();
	clientMobile.clearInvalid();
};

/**********************************************************************/
var clientTypeTreeTbar = new Ext.Toolbar({
	items : ['->', {
		text : '修改',
		iconCls : 'btn_edit',
		handler : function(e){
			updateClientTypeHandler();
		}
	}, {
		text : '删除',
		iconCls : 'btn_delete',
		handler : function(e){
			deleteClientTypeHandler();
		}
	}, {
		text : '刷新',
		id : 'btnRefreshClientTypeTree',
		iconCls : 'btn_refresh',
		handler : function(e){
			clientTypeTree.getSelectionModel().clearSelections();
			clientTypeTree.getRootNode().reload();
			Ext.getDom('clientTypeShowType').innerHTML = '----';
			clientBasicGrid.getStore().baseParams['searchClientType'] = null;
			Ext.getCmp('btnSearchClient').handler();
		}
	}]
});

var clientTypeTree = new Ext.tree.TreePanel({
	title : '客户类型',
	region : 'west',
	width : 200,
	border : true,
	rootVisible : true,
	autoScroll : true,
	frame : true,
	bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
	loader : new Ext.tree.TreeLoader({
		dataUrl : '../../QueryClientTypeTree.do',
		baseParams : {
			restaurantID : restaurantID
		}
	}),
	root : new Ext.tree.AsyncTreeNode({
		expanded : true,
		text : '全部类型',
        leaf : false,
        border : true,
        clientTypeID : -1
	}),
	tbar : clientTypeTreeTbar,
    listeners : {
    	click : function(e){
    		Ext.getDom('clientTypeShowType').innerHTML = e.text;
    	},
    	dblclick : function(e){
    		Ext.getCmp('btnSearchClient').handler();
    	}
    }
});

/**********************************************************************/
var clientBasicGridTbar = new Ext.Toolbar({
	items : [{
		xtype : 'tbtext',
		text : String.format(Ext.ux.txtFormat.typeName, '客户类型', 'clientTypeShowType', '----')
	}, {
		xtype : 'tbtext',
		text : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
	}, {
		xtype : 'tbtext',
		text : '过滤:'
	}, {
		xtype : 'combo',
		id : 'comboSearchType',
		readOnly : true,
		forceSelection : true,
		width : 90,
		value : 0,
		store : new Ext.data.SimpleStore({
			fields : [ 'value', 'text' ],
			data : [[0, '全部'], [1, '客户名称'], [2, '公司'], [3, '手机号码'], [4, '性别']]
		}),
		valueField : 'value',
		displayField : 'text',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			select : function(thiz, record, index){
				var textValue = Ext.getCmp('txtSearchTextValue');
				var numberValue = Ext.getCmp('txtSearchNumberValue');
				var sexValue = Ext.getCmp('comboSearchClientSex');
				
				if(index == 0){
					textValue.setVisible(false);
					numberValue.setVisible(false);
					sexValue.setVisible(false);
					cmObj.searchValue = '';
				}else if(index == 1 || index == 2){
					textValue.setVisible(true);
					numberValue.setVisible(false);
					sexValue.setVisible(false);
					cmObj.searchValue = textValue.getId();
				}else if(index == 3){
					textValue.setVisible(false);
					numberValue.setVisible(true);
					sexValue.setVisible(false);
					numberValue.setValue();
					cmObj.searchValue = numberValue.getId();
				}else if(index == 4){
					textValue.setVisible(false);
					numberValue.setVisible(false);
					sexValue.setVisible(true);
					sexValue.setValue(0);
					cmObj.searchValue = sexValue.getId();
				}
			}
		}
	}, {
		xtype : 'tbtext',
		text : '&nbsp;&nbsp;'
	}, {
		xtype : 'textfield',
		id : 'txtSearchTextValue',
		hidden : true,
		width : 150
	}, {
		xtype : 'numberfield',
		id : 'txtSearchNumberValue',
		style : 'text-align:left;',
		hidden : true,
		width : 150
	}, {
		xtype : 'combo',
		id : 'comboSearchClientSex',
		hidden : true,
		readOnly : true,
		forceSelection : true,
		width : 90,
		value : 0,
		store : new Ext.data.SimpleStore({
			fields : [ 'value', 'text' ],
			data : [[0,'男'], [1, '女']]
		}),
		valueField : 'value',
		displayField : 'text',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true			
	},
	'->', 
	{
		text : '搜索',
		id : 'btnSearchClient',
		iconCls : 'btn_search',
		handler : function(e){
			var clientTypeNode = clientTypeTree.getSelectionModel().getSelectedNode();
			var searchType = Ext.getCmp('comboSearchType');
			var searchValue = Ext.getCmp(cmObj.searchValue);
			var st, sv;
			st = searchType.getValue();
			if(st == 0){
				sv = '';
			}else{
				if(st == 4){
					sv = searchValue.getValue();
				}else{
					sv = Ext.util.Format.trim(searchValue.getValue());
				}
			}
			
			var gs = clientBasicGrid.getStore();
			gs.baseParams['searchType'] = st;
			gs.baseParams['searchValue'] = sv;
			if(clientTypeNode != null && typeof clientTypeNode.attributes.clientTypeID == 'number'){
				gs.baseParams['searchClientType'] = clientTypeNode.attributes.clientTypeID;
			}
			gs.load({
				params : {
					start : 0,
					limit : 30
				}
			});
		}
	}, {
		text : '重置',
		iconCls : 'btn_refresh',
		handler : function(e){
			clientTypeTree.getSelectionModel().clearSelections();
			Ext.getDom('clientTypeShowType').innerHTML = '----';
			Ext.getCmp('comboSearchType').setValue(0);
			Ext.getCmp('comboSearchType').fireEvent('select', null, null, 0);
			clientBasicGrid.getStore().baseParams['searchClientType'] = null;
			Ext.getCmp('btnSearchClient').handler();
		}
	}, {
		text : '添加',
		iconCls : 'btn_add',
		handler : function(e){
			insertClientHandler();
		}
	}, {
		text : '修改',
		iconCls : 'btn_edit',
		handler : function(e){
			updateClientHandler();
		}
	}, {
		text : '删除',
		iconCls : 'btn_delete',
		handler : function(e){
			deleteClientHandler();
		}
	}]
});

clientOperationRenderer = function(){
	return ''
	       + '<a href="javascript:updateClientHandler()">修改</a>'
	       + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
	       + '<a href="javascript:deleteClientHandler()">删除</a>';
};

var clientBasicGrid = createGridPanel(
	'clientBasicGrid',
	'客户信息',
	'',
	'',
	'../../QueryClient.do',
	[
		[true, false, true, true], 
//		['客户编号', 'clientID', 70],
		['客户名称', 'name'],
		['客户类别', 'clientType.name'],
		['性别', 'sexDisplay', 70],
//		['会员账号', 'memberAccount', 70],
		['手机', 'mobile'],
		['电话', 'tele'],
		['公司', 'company', 150],
		['操作', 'operation', 200, 'center', 'clientOperationRenderer']
	],
	['clientID', 'name', 'clientType.name', 'clientType.typeID', 'birthdayFormat', 'birthday',
	 'memberAccount', 'sexDisplay', 'sex', 'mobile', 'tele', 'company', 
	 'tastePref', 'clientID', 'taboo', 'comment', 'contactAddress', 'IDCard'],
	[['pin',pin], ['isPaging', true], ['restaurantID', restaurantID]],
	30,
	'',
	clientBasicGridTbar
);	
clientBasicGrid.region = 'center';
clientBasicGrid.keys = [{
	key : Ext.EventObject.ENTER,
	scope : this,
	fn : function(){ 
		Ext.getCmp('btnSearchClient').handler();
	}
}];
clientBasicGrid.on('rowdblclick', function(){
	updateClientHandler();
});

/**********************************************************************/
var clientTypeWin;
var clientWin;
Ext.onReady(function(){
	
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();

	var centerPanel = new Ext.Panel({
		title : '客户资料管理',
		region : 'center',
		layout : 'border',
		items : [clientTypeTree, clientBasicGrid],
		frame : true,
		autoScroll : true,
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'}, 
			    btnInsertClientType,
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'}, 
			    btnInsertClient,
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    '->',
			    btnPushBack,
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    btnLogOut
			]
		})
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
			layout : 'form',
			frame : true,
			border : false,
			html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
		}]
	 });
	 
	 clientMgrInit();
	 
});


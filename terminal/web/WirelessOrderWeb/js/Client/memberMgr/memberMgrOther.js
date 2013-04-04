function checkSelect(e){
	var eventType = typeof(e), event;
	if(e == null || eventType == 'undefined'){
		/**
		 * 获取选择
		 */
		for(var i = 0; i < mObj.ctSelect.idList.length; i++){
			var temp = Ext.getCmp(mObj.ctSelect.idList[i]);
			if(temp.getValue()){
				return temp;
			}
		}
	}else{
		if(eventType == 'string'){
			event = e;
		}else if(eventType == 'object'){
			event = e.getId();
		}else{
			return undefined;
		}
		/**
		 * 切换选择
		 */
		for(var i = 0; i < mObj.ctSelect.idList.length; i++){
			if(mObj.ctSelect.idList[i] != event)
				Ext.getCmp(mObj.ctSelect.idList[i]).setValue(false);
		}
		
		var btnBindClient = Ext.getCmp('btnBindClient');
		var numberClientID = Ext.getCmp('numberClientID');
		var memberBasicPanel = Ext.getCmp('memberBasicPanel');
		var clientStatus = false;
		
		if(event == mObj.ctSelect.radioBJM.id){
			// 不记名
			btnBindClient.setVisible(false);
			clientStatus = true;
		}else if(event == mObj.ctSelect.radioXJ.id){
			// 新建
			btnBindClient.setVisible(false);
			clientStatus = false;
		}else if(event == mObj.ctSelect.radioBD.id){
			// 绑定
			btnBindClient.setVisible(true);
			clientStatus = true;
		}
		//
		for(var i = 0; i < memberBasicPanel.items.length; i++){
			var item = memberBasicPanel.items.get(i).items.get(0);
			if(typeof item != 'undefined' && item.clientMsg == true){
				item.setDisabled(false);
				item.setValue();
				item.clearInvalid();
				item.setDisabled(clientStatus);
			}
		}
		numberClientID.setDisabled(false);
		numberClientID.setValue();
		numberClientID.setDisabled(true);
		// 加载原数据
		if(memberBasicWin.otype == mObj.operation['update']){
			var data = Ext.ux.getSelData(memberBasicGrid);
			var client = data ? data.client : {};
			if((event == mObj.ctSelect.radioBJM.id && eval(client.level == 1))
					|| (event == mObj.ctSelect.radioBD.id && eval(client.level == 0))){
				// 
				var level = client.level;
				client.level = null;
				operationClientDataToMember({
					type : mObj.operation['set'],
					data : client
				});
				client.level = level;
			}
		}
	}
};	

function xyClientGridRenderer(){
	return '<a href="javascript:btnBindClientHandler()">绑定</a>';
};

function createClientHandler(){
	if(clientWin != null || typeof clientWin != 'undefined'){
		return;
	}
	var xyClientGridTbar = new Ext.Toolbar({
		items : [{
			xtype : 'tbtext',
			text : '过滤:'
		}, {
			xtype : 'combo',
			id : 'comboClientSearchType',
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
						mObj.client.searchValue = '';
					}else if(index == 1 || index == 2){
						textValue.setVisible(true);
						numberValue.setVisible(false);
						sexValue.setVisible(false);
						mObj.client.searchValue = textValue.getId();
					}else if(index == 3){
						textValue.setVisible(false);
						numberValue.setVisible(true);
						sexValue.setVisible(false);
						numberValue.setValue();
						mObj.client.searchValue = numberValue.getId();
					}else if(index == 4){
						textValue.setVisible(false);
						numberValue.setVisible(false);
						sexValue.setVisible(true);
						sexValue.setValue(0);
						mObj.client.searchValue = sexValue.getId();
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
			id : 'btnSearchClientByBindWin',
			iconCls : 'btn_search',
			handler : function(e){
				var searchType = Ext.getCmp('comboClientSearchType');
				var searchValue = Ext.getCmp(mObj.client.searchValue);
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
				
				var gs = xyClientGrid.getStore();
				gs.baseParams['searchType'] = st;
				gs.baseParams['searchValue'] = sv;
				gs.load({
					params : {
						start : 0,
						limit : 30
					}
				});
			}
		}]
	});
	
	xyClientGrid = createGridPanel(
		'xyClientGrid',
		'',
		'',
		'',
		'../../QueryClient.do',
		[
			[true, false, false, true], 
			['客户编号', 'clientID', 100],
			['客户名称', 'name', 100],
			['客户类别', 'clientType.name'],
			['性别', 'sexDisplay', 70],
			['手机', 'mobile'],
			['电话', 'tele'],
			['公司', 'company', 150],
			['操作', 'operation', 100, 'center', 'xyClientGridRenderer']
		],
		['clientID', 'name', 'clientType', 'clientTypeID', 'clientType.name', 'birthdayFormat', 'birthday',
		 'memberAccount', 'sexDisplay', 'sex', 'mobile', 'tele', 'company', 
		 'tastePref', 'taboo', 'comment', 'contactAddress', 'IDCard'],
		[['pin',pin], ['isPaging', true], ['restaurantID', restaurantID]],
		30,
		'',
		xyClientGridTbar
	);	
	xyClientGrid.border = false;
	xyClientGrid.frame = false;
	xyClientGrid.on('rowdblclick', function(){
		btnBindClientHandler();
	});
	
	clientWin = new Ext.Window({
		title : '绑定客户资料',
		closable : false,
		modal : true,
		resizable : false,
		layout : 'fit',
		height : 400,
		width : 800,
		items : [xyClientGrid],
		bbar : ['->', {
			text : '绑定',
			id : 'btnFindClientAndBind',
			iconCls : 'btn_save',
			handler : function(e){
				btnBindClientHandler();
			}
		}, {
			text : '关闭',
			iconCls : 'btn_close',
			handler : function(e){
				clientWin.hide();
			}
		}],
		listeners : {
			show : function(){
				Ext.getCmp('btnSearchClientByBindWin').handler();
			}
		},
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				clientWin.hide();
			}
		}, {
			key : Ext.EventObject.ENTER,
			scpoe : this,
			fn : function(){
				Ext.getCmp('btnFindClientAndBind').handler();
			}
		}]
	});
};

/**
 * 
 */
function btnBindClientHandler(){
	var data = Ext.ux.getSelData(xyClientGrid);
	if(!data){
		Ext.example.msg('提示', '请选中一个需绑定的客户.');
		return;
	}
	operationClientDataToMember({
		type : mObj.operation['set'],
		data : data
	});
	clientWin.hide();
	clientWin.bindClientData = data;
};

/**
 * 
 */
function operationClientDataToMember(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var clientID = Ext.getCmp('numberClientID');
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
	if(c.type == mObj.operation['set']){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		if(eval(data['level'] == 0)){
			Ext.getCmp(mObj.ctSelect.radioBD.id).setValue(true);
		}else if(eval(data['level'] == 1)){
			Ext.getCmp(mObj.ctSelect.radioBJM.id).setValue(true);
		}
		clientID.setValue(data['clientID']);
		clientName.setValue(data['name']);
		clientSex.setValue(data['sex']);
		clientMobile.setValue(data['mobile']);
		clientTele.setValue(data['tele']);
		clietnIDCard.setValue(data['IDCard']);
		clientCompany.setValue(data['company']);
		clientTastePref.setValue(data['tastePref']);
		clietTaboo.setValue(data['taboo']);
		clientContactAddress.setValue(data['contactAddress']);
		clientComment.setValue(data['comment']);
		clientBirthday.setValue(typeof data['birthday'] == 'undefined' ? undefined : new Date(data['birthday']));
		clientType.setValue(data['clientTypeID']);
	}else if(c.type == mObj.operation['get']){
		data = {
			clientID : clientID.getValue(),
			name : clientName.getValue(),
			clientType : {
				typeID : clientType.getValue()
			},
			sex : clientSex.getValue(),
			mobile : clientMobile.getValue(),
			tele : clientTele.getValue(),
			birthday : clientBirthday.getValue() != '' ? clientBirthday.getValue().getTime() : '',
			IDCard : clietnIDCard.getValue(),
			company : clientCompany.getValue(),
			tastePref : clientTastePref.getValue(),
			taboo : clietTaboo.getValue(),
			contactAddress : clientContactAddress.getValue(),
			comment : clientComment.getValue()
		};
		c.data = data;
	}
	clientName.clearInvalid();
	clientType.clearInvalid();
	return c;
};

/**
 * 
 */
function operationMemberData(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var memberID = Ext.getCmp('numberMemberID');
	var memberCardAliasID = Ext.getCmp('numberMemberCardAliasID');
	var memberType = Ext.getCmp('comboMemberType');
	var point = Ext.getCmp('numberMmeberPoint');
	var totalBalance = Ext.getCmp('numberTotalBalance');
	var baseBalance = Ext.getCmp('numberBaseBalance');
	var extraBalance = Ext.getCmp('numberExtraBalance');
	var lastStaff = Ext.getCmp('txtLastStaff');
	var lastModDate = Ext.getCmp('txtLastModDate');
	var comment = Ext.getCmp('txtOperationComment');
	var status = Ext.getCmp('comboMemberStatus');
	if(c.type == mObj.operation['set']){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		memberID.setValue(data['id']);
		memberCardAliasID.setValue(data['memberCard.aliasID']);
		memberType.setValue(data['memberTypeID']);
		point.setValue(data['point']);
		totalBalance.setValue(data['totalBalance']);
		baseBalance.setValue(data['baseBalance']);
		extraBalance.setValue(data['extraBalance']);
		lastStaff.setValue(data['staff.name']);
		lastModDate.setValue(data['lastModDateFormat']);
		comment.setValue(data['comment']);
		status.setValue(data['statusValue']);
	}else if(c.type == mObj.operation['get']){
		data = {
			id : memberID.getValue(),
			memberType : {
				typeID : memberType.getValue()
			},
			memberCard : {
				aliasID : memberCardAliasID.getValue()
			},
			status : status.getValue()
		};
		c.data = data;
	}
	memberCardAliasID.clearInvalid();
	memberType.clearInvalid();
	status.clearInvalid();
	return c;
};

/**
 * 
 */
function operationMemberBasicMsg(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	if(c.type == mObj.operation['set']){
		data = c.data;
		operationMemberData({
			type : c.type,
			data : data
		});
		operationClientDataToMember({
			type : c.type,
			data : data['client']
		});
	}else if(c.type == mObj.operation['get']){
		data = operationMemberData({
			type : c.type
		}).data;
		data.client = operationClientDataToMember({
			type : c.type
		}).data;
		c.data = data;
	}
	return c;
}
clientMgrInitWin = function(){
	clientTypeWin = new Ext.Window({
		title : '&nbsp;',
		closable : false,
		resizable : false,
		modal : true,
		width : 230,
		items : [{
			xtype : 'form',
			layout : 'form',
			frame : true,
			labelWidth : 70,
			labelAlign : 'right',
			defaults : {
				width : 110
			},
			items : [{
				xtype : 'numberfield',
				id : 'numClientTypeID',
				fieldLabel : '类型编号' + Ext.ux.txtFormat.xh,
				disabled : true
			}, {
				xtype : 'textfield',
				id : 'txtClientTypeName',
				fieldLabel : '类型名称' + Ext.ux.txtFormat.xh,
				allowBlank : false,
				blankText : '客户类型名称不允许为空.',
				validator : function(v){
					if(Ext.util.Format.trim(v).length > 0){
						return true;
					}else{
						return '客户类型名称不允许为空.';
					}
				}
			}, getClientTypeTrigger({
				id : 'triggerClientTypeParentID',
				fieldLabel : '设为子类',
//				rootVisible : false,
				allowBlank : true,
				rootText : '不设置'
			})]
		}],
		bbar : ['->', {
			text : '保存',
			id : 'btnSaveOperationClientType',
			iconCls : 'btn_save',
			handler : function(e){
				var clientTypeID = Ext.getCmp('numClientTypeID');
				var clientTypeName = Ext.getCmp('txtClientTypeName');
				var clientTypeParentID = Ext.getCmp('triggerClientTypeParentID');
				var actionURL = '';
				
				if(!clientTypeName.isValid()){
					return;
				}
				
				if(clientTypeWin.otype == cmObj.operation['insert']){
					actionURL = '../../InsertClientType.do';
				}else if(clientTypeWin.otype == cmObj.operation['update']){
					actionURL = '../../UpdateClientType.do';
				}else{
					return;
				}
				
				var save = Ext.getCmp('btnSaveOperationClientType');
				var close = Ext.getCmp('btnCloseOperationClientType');
				
				save.setDisabled(true);
				close.setDisabled(true);
				
				Ext.Ajax.request({
					url : actionURL,
					params : {
						restaurantID : restaurantID,
						typeID : clientTypeID.getValue(),
						typeName : clientTypeName.getValue(),
						typeParentID : clientTypeParentID.getRawValue() == '' ? -1 : clientTypeParentID.getValue()
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
						save.setDisabled(false);
						close.setDisabled(false);
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
						save.setDisabled(false);
						close.setDisabled(false);
					}
				});
			}
		}, {
			text : '关闭',
			id : 'btnCloseOperationClientType',
			iconCls : 'btn_close',
			handler : function(e){
				clientTypeWin.hide();
			}
		}],
		listeners : {
			render : function(){
				
			},
			show : function(){
				Ext.getCmp('btnSaveOperationClientType').setDisabled(false);
				Ext.getCmp('btnCloseOperationClientType').setDisabled(false);
			}
		},
		keys : [{
			key : Ext.EventObject.ENTER,
			fn : function(){ 
				Ext.getCmp('btnSaveOperationClientType').handler();
			},
			scope : this 
		}]
	});
	clientTypeWin.render(document.body);
	
	var clientBaiscForm = new Ext.Panel({
		xtype : 'panel',
		frame : true,
		border : false,
		layout : 'column',
		defaults : {
			xtype : 'form',
			layout : 'form',
			labelWidth : 80,
			labelAlign : 'right',
			columnWidth : .33,
			defaults : {
				xtype : 'textfield',
				width : 110
			}
		},
		items : [{
			items : [{
				xtype : 'hidden',
				id : 'munClientID',
				fieldLabel : '客户编号' + Ext.ux.txtFormat.xh,
				disabled : true,
				readOnly : true
			}]
		}, {
			items : [{
				xtype : 'textfield',
				id : 'txtClientName',
				fieldLabel : '客户名称' + Ext.ux.txtFormat.xh,
				allowBlank : false,
				blankText : '客户名称不允许为空.',
				validator : function(v){
					if(Ext.util.Format.trim(v).length > 0){
						return true;
					}else{
						return '客户名称不允许为空.';
					}
				}
			}]
		}, {
			items : [
			getClientTypeTrigger({
				id : 'tirggerClietnTypeByClient',
				fieldLabel : '客户类别' + Ext.ux.txtFormat.xh,
				rootVisible : false,
				allowBlank : false,
				blankText : '客户类别不允许为空.'
			})]
		}, {
			xtype : 'form',
			layout : 'form',
			items : [{
				xtype : 'combo',
				id : 'comboClientSex',
				fieldLabel : '性别' + Ext.ux.txtFormat.xh,
				readOnly : true,
				forceSelection : true,
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
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				id : 'txtClientMobile',
				fieldLabel : '手机' + Ext.ux.txtFormat.xh,
				allowBlank : false,
				regex : Ext.ux.RegText.phone.reg,
				regexText : Ext.ux.RegText.phone.error
			}]
		}, {
			items : [{
				xtype : 'textfield',
				id : 'txtClientTele',
				fieldLabel : '电话'
			}]
		}, {
			items : [{
				xtype : 'datefield',
				id : 'dateClientBirthday',
				fieldLabel : '生日',
				format : 'Y-m-d',
				readOnly : true
			}]
		}, {
			items : [{
				xtype : 'textfield',
				id : 'txtClientIDCard',
				fieldLabel : '身份证'
			}]
		}, {
			items : [{
				xtype : 'textfield',
				id : 'txtClientCompany',
				fieldLabel : '公司'
			}]
		}, {
			items : [{
				xtype : 'textfield',
				id : 'txtClientTastePref',
				fieldLabel : '口味'
			}]
		}, {
			items : [{
				xtype : 'textfield',
				id : 'txtClientTaboo',
				fieldLabel : '忌讳'
			}]
		}, {
			columnWidth : 1,
			items : [{
				xtype : 'textfield',
				id : 'txtClientContactAddress',
				fieldLabel : '联系地址',
				width : 520
			}]
		}, {
			columnWidth : 1,
			items : [{
				xtype : 'textfield',
				id : 'txtClientComment',
				fieldLabel : '备注',
				width : 520
			}]
		}]
	});
		
	clientWin = new Ext.Window({
		title : '&nbsp;',
		closable : false,
		resizable : false,
		modal : true,
		width : 650,
		items : [clientBaiscForm],
		bbar : ['->', {
			text : '保存',
			id : 'btnSaveOperationClient',
			iconCls : 'btn_save',
			handler : function(e){
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
				
				var actionURL = '';
				
				if(!clientName.isValid() || !clientType.isValid() || !clientMobile.isValid() || !clientSex.isValid()){
					return;
				}
				
				if(clientWin.otype == cmObj.operation['insert']){
					actionURL = '../../InsertClient.do';
				}else if(clientWin.otype == cmObj.operation['update']){
					actionURL = '../../UpdateClient.do';
				}else{
					return;
				}
				
				var save = Ext.getCmp('btnSaveOperationClient');
				var close = Ext.getCmp('btnCloseOperationClient');
				
				Ext.Ajax.request({
					url : actionURL,
					params : {
						restaurantID : restaurantID,
						clientID : clientID.getValue(),
						clientName : clientName.getValue(),
						clientType : clientType.getValue(),
						clientSex : clientSex.getValue(),
						clientMobile : clientMobile.getValue(),
						clientTele : clientTele.getValue(),
						clientBirthday : clientBirthday.getValue() != '' ? clientBirthday.getValue().format('Y-m-d 00:00:00') : '',
						clietnIDCard : clietnIDCard.getValue(),
						clientCompany : clientCompany.getValue(),
						clientTastePref : clientTastePref.getValue(),
						clietTaboo : clietTaboo.getValue(),
						clientContactAddress : clientContactAddress.getValue(),
						clientComment : clientComment.getValue()
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							clientWin.hide();
							clientBasicGrid.getStore().reload();
						}else{
							Ext.ux.showMsg(jr);
						}
						
						save.setDisabled(false);
						close.setDisabled(false);
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
						save.setDisabled(false);
						close.setDisabled(false);
					}
				});
				
			}
		}, {
			text : '关闭',
			id : 'btnCloseOperationClient',
			iconCls : 'btn_close',
			handler : function(e){
				clientWin.hide();
			}
		}],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){ 
				clientWin.hide();
			}
		}, {
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){ 
				Ext.getCmp('btnSaveOperationClient').handler();
			}
		}]
	});
	clientWin.render(document.body);
};

clientMgrInit = function(){
	getOperatorName(pin, '../../');
	clientMgrInitWin();
//	Ext.Ajax.request({
//		url : '../../QueryClientTypeTree.do',
//		params : {
//			restaurantID : restaurantID
//		},
//		success : function(res, opt){
//			alert(res.responseText);
//		}
//	});
};

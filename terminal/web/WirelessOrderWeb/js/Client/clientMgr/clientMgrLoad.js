getClientTypeTrigger = function(c){
	 var trigger = new Ext.form.TriggerField({
		editable : false,
	    fieldLabel : c.fieldLabel,
	    id : c.id,
	    readOnly : true,
	    allowBlank : false,
	    width : c.width == null || typeof c.width == 'undefined' ? '' : c.width,
	    blankText : '该项不能为空.',
	    validator : function(v){
			if(Ext.util.Format.trim(v).length > 0){
				return true;
			}else{
				return '该项不能为空.';
			}
		},
	    onTriggerClick : function(){
	    	this.menu.show(this.el, 'tl-bl?');
	    },
	    listeners : {
	    	beforerender : function(){
	    		
	    	},
	    	render : function(){
	    		
	    		this.setValue = function(v){
	    			this.tree.setValue(v);
	    		};
	    		this.getValue = function(){
	    			return this.value;
	    		};
	    		this.reload = function(){
	    			if(typeof this.tree.getRootNode() != 'undefined'){
	    				this.tree.getRootNode().reload();
	    			}
	    		};
	    		this.tree = new Ext.tree.TreePanel({
	 	    		width : 200,
		    	   	height : 280,
		    	   	autoScroll : true,
	 	    		rootVisible : typeof c.rootVisible == 'boolean' ? c.rootVisible : true,
	 	    		frame : true,
	 	    		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
	 	    		loader : new Ext.tree.TreeLoader({
	 	    			dataUrl : '../../QueryClientTypeTree.do',
	 	    			baseParams : {
	 	    				restaurantID : restaurantID
	 	    			}
	 	    		}),
	 	    		root : new Ext.tree.AsyncTreeNode({
	 	    			text : '全部类型',
	 	    			leaf : false,
	 	    			border : true,
	 	    			clientTypeID : -1,
	 	    			expanded : true
	 	    		}),
	 	    		listeners : {
	 	    			render : function(){
	 	    				this.bindValue = function(c){
	 	    					this.ownerCt.setRawValue(c.text);
		    	    			this.ownerCt.value = c.value;
	 	    				};
	 	    				this.findChildNode = function(node, val){
	 	    					if(node.attributes.clientTypeID == val){
	 	    						node.select();
	 	    						this.bindValue({
			    	    				text : node.text,
			    	    				value : node.attributes.clientTypeID
			    	    			});
	 	    					}else{
	 	    						for(var i = 0; i < node.childNodes.length; i++){
	 	    							var rn = node.childNodes[i];
	 	    							if(rn.attributes.clientTypeID == val){
	 	    								rn.select();
	 	    								this.bindValue({
	 	    									text : rn.text,
	 	    									value : rn.attributes.clientTypeID
	 	    								});
	 	    								break;
	 	    							}else if(rn.childNodes.length > 0){
	 	    								this.findChildNode(rn, val);
	 	    							}
	 	    						}
	 	    					}
	 	    				};
	 	    				this.checkValue = function(node, vn){
	 	    					if(node.parentNode != null){
	 	    						var pn = node.parentNode;
	 	    						if(pn.attributes.clientTypeID == vn.attributes.clientTypeID){
	 	    							return true;
	 	    						}else{
	 	    							return this.checkValue(pn, vn);
	 	    						}
	 	    					}
	 	    				};
	 	    				this.setValue = function(val){
	 	    					this.getSelectionModel().clearSelections();
	 	    					if(typeof val == 'undefined'){
	 	    						this.snode = null;
	 	    						this.bindValue({
			    	    				text : null,
			    	    				value : null
			    	    			});
	 	    					}else if(typeof val == 'string' && Ext.util.Format.trim(val).length > 0){
	 	    						this.snode = null;
	 	    						this.findChildNode(this.getRootNode(), val);
	 	    					}else if(typeof val == 'number'){
	 	    						this.snode = null;
	 	    						this.findChildNode(this.getRootNode(), val);
	 	    					}else if(typeof val == 'object'){
	 	    						this.snode = val;
	 	    						this.findChildNode(this.getRootNode(), val.attributes.clientParentTypeID);
	 	    					}
	 	    				};
	 	    				this.getValue = function(){
	 	    					return this.ownerCt.value;
	 	    				};
	 	    			},
	 	    			click : function(e){
	 	    				if(this.snode != null && typeof this.snode != 'undefined'){
	 	    					if(eval(e.attributes.clientTypeID != this.snode.attributes.clientTypeID) && this.checkValue(e, this.snode) != true){
			    	    			this.bindValue({
			    	    				text : e.text,
			    	    				value : e.attributes.clientTypeID
			    	    			});
			    	    			this.ownerCt.menu.hide();
		    	    			}else{
		    	    				Ext.example.msg('提示', '不能选择自身或其子类型.');
		    	    			}
	 	    				}else{
	 	    					this.bindValue({
		    	    				text : e.text,
		    	    				value : e.attributes.clientTypeID
		    	    			});
		    	    			this.ownerCt.menu.hide();
	 	    				}
	 	    			}
	 	    		}
	    		});
	  	    	this.tree.ownerCt = this;
	  	    		
	  	    	this.menu = new Ext.menu.Menu({
	   	    		items : [new Ext.menu.Adapter(this.tree)],
	   	    		listeners : {
	   	    			show : function(){
	   	    				
	   	    			}
	   	    		}
	   	    	});
	   	    	this.menu.ownerCt = this;
	    		
	    		this.menu.show(this.el, 'tb-bl?');
	    	}
	    }
	 });
	 return trigger;
};

clientMgrInitWin = function(){
	if(!clientTypeWin){
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
				labelWidth : 65,
				defaults : {
					width : 130
				},
				items : [{
					xtype : 'numberfield',
					id : 'numClientTypeID',
					fieldLabel : '类型编号',
					disabled : true
				}, {
					xtype : 'textfield',
					id : 'txtClientTypeName',
					fieldLabel : '类型名称',
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
					fieldLabel : '归属大类'
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
					
					if(!clientTypeName.isValid() || !clientTypeParentID.isValid()){
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
							typeParentID : clientTypeParentID.getValue()
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
		clientTypeWin.show();
		clientTypeWin.hide();
	}
	
	if(!clientWin){
		var clientBaiscForm = {
			xtype : 'panel',
			frame : true,
			border : false,
			layout : 'column',
			defaults : {
				labelWidth : 65
			},
			items : [{
				columnWidth : .33,
				xtype : 'form',
				layout : 'form',
				items : [{
					xtype : 'numberfield',
					id : 'munClientID',
					fieldLabel : '客户编号' + Ext.ux.txtFormat.xh,
					width : 110,
					disabled : true,
					readOnly : true
				}]
			}, {
				columnWidth : .33,
				xtype : 'form',
				layout : 'form',
				items : [{
					xtype : 'textfield',
					id : 'txtClientName',
					fieldLabel : '客户名称' + Ext.ux.txtFormat.xh,
					width : 110,
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
				columnWidth : .33,
				xtype : 'form',
				layout : 'form',
				items : [
				getClientTypeTrigger({
					id : 'tirggerClietnTypeByClient',
					fieldLabel : '客户类别' + Ext.ux.txtFormat.xh,
					width : 110,
					rootVisible : false,
					allowBlank : false,
					blankText : '客户类别不允许为空.'
				})]
			}, {
				columnWidth : .33,
				xtype : 'form',
				layout : 'form',
				items : [{
					xtype : 'combo',
					id : 'comboClientSex',
					fieldLabel : '性别',
					readOnly : true,
					forceSelection : true,
					width : 110,
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
				columnWidth : .33,
				xtype : 'form',
				layout : 'form',
				items : [{
					xtype : 'numberfield',
					id : 'txtClientMobile',
					fieldLabel : '手机',
					width : 110
//					maxLength : 11,
//					maxLengthText : '请输入11位手机号码',
//					minLength : 11,
//					minLengthText : '请输入11位手机号码'
				}]
			}, {
				columnWidth : .33,
				xtype : 'form',
				layout : 'form',
				items : [{
					xtype : 'textfield',
					id : 'txtClientTele',
					fieldLabel : '电话',
					width : 110
				}]
			}, {
				columnWidth : .33,
				xtype : 'form',
				layout : 'form',
				items : [{
					xtype : 'datefield',
					id : 'dateClientBirthday',
					fieldLabel : '生日',
					format : 'Y-m-d',
					readOnly : true,
					width : 110
				}]
			}, {
				columnWidth : .33,
				xtype : 'form',
				layout : 'form',
				items : [{
					xtype : 'textfield',
					id : 'txtClientIDCard',
					fieldLabel : '身份证',
					width : 110
				}]
			}, {
				columnWidth : .33,
				xtype : 'form',
				layout : 'form',
				items : [{
					xtype : 'textfield',
					id : 'txtClientCompany',
					fieldLabel : '公司',
					width : 110
				}]
			}, {
				columnWidth : .33,
				xtype : 'form',
				layout : 'form',
				items : [{
					xtype : 'textfield',
					id : 'txtClientTastePref',
					fieldLabel : '口味',
					width : 110
				}]
			}, {
				columnWidth : .33,
				xtype : 'form',
				layout : 'form',
				items : [{
					xtype : 'textfield',
					id : 'txtClientTaboo',
					fieldLabel : '忌讳',
					width : 110
				}]
			}, {
				columnWidth : 1,
				xtype : 'form',
				layout : 'form',
				items : [{
					xtype : 'textfield',
					id : 'txtClientContactAddress',
					fieldLabel : '联系地址',
					width : 490
				}]
			}, {
				columnWidth : 1,
				xtype : 'form',
				layout : 'form',
				items : [{
					xtype : 'textfield',
					id : 'txtClientComment',
					fieldLabel : '备注',
					width : 490
				}]
			}]
		};
		
		clientWin = new Ext.Window({
			title : '&nbsp;',
			closable : false,
			resizable : false,
			modal : true,
			width : 600,
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
					
					if(!clientName.isValid() || !clientType.isValid()){
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
				key : Ext.EventObject.ENTER,
				fn : function(){ 
					Ext.getCmp('btnSaveOperationClient').handler();
				},
				scope : this 
			}]
		});
		
		clientWin.setPosition(clientWin.width * -1 -100, 0);
		clientWin.show();
		clientWin.hide();
		Ext.getCmp('tirggerClietnTypeByClient').menu.hide();
	}
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

// *************整体布局*************
var dishesOrderImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/InsertOrder.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "点菜",
	handler : function(btn) {
		if (selectedTable != "") {
			var tableIndex = -1;
			for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
				if (tableStatusListTSDisplay[i].aliasId == selectedTable) {
					tableIndex = i;
					break;
				}
			}
			if (tableStatusListTSDisplay[tableIndex].status == TABLE_BUSY) {
				var category = "X";
				var tableNbr = "XXX";
				if (tableStatusListTSDisplay[tableIndex].category == CATE_NORMAL) {
					category = CATE_NORMAL;
					tableNbr = selectedTable;
				} else if (tableStatusListTSDisplay[tableIndex].category == CATE_TAKE_OUT) {
					category = CATE_TAKE_OUT;
					tableNbr = selectedTable;
				} else if (tableStatusListTSDisplay[tableIndex].category == CATE_JOIN_TABLE) {
					category = CATE_JOIN_TABLE;
					tableNbr = selectedTable;
				} else if (tableStatusListTSDisplay[tableIndex].category == CATE_MERGER_TABLE) {
					category = CATE_MERGER_TABLE;
					var tblArray = getMergeTable(selectedTable);
					tableNbr = tblArray[0];
					tableNbr2 = tblArray[1];
				} else if (tableStatusListTSDisplay[tableIndex].category == CATE_GROUP_TABLE) {
					category = CATE_GROUP_TABLE;
					tableNbr = selectedTable;
				}

				var minCost;
				var serviceRate;
				if (category != CATE_MERGER_TABLE) {
					minCost = tableStatusListTSDisplay[tableIndex].minimumCost;
					serviceRate = tableStatusListTSDisplay[tableIndex].serviceRate;
				} else {
					minCost = getMaxMinCostMT(selectedTable);
					serviceRate = getMaxSerRateMT(selectedTable);
				}

				location.href = "OrderMain.html?"
						+ "pin=" + pin
						+ "&restaurantID=" + restaurantID
						+ "&tableAliasID=" + tableNbr
						+ "&ts=1" 
						+ "&personCount=" + tableStatusListTSDisplay[tableIndex].customNum
						+ "&category=" + category 
						+ "&minCost=" + minCost
						+ "&serviceRate=" + serviceRate;
			} else if (tableStatusListTSDisplay[tableIndex].status == TABLE_IDLE) {
				location.href = "OrderMain.html?"
						+ "pin=" + pin
						+ "&restaurantID=" + restaurantID
						+ "&ts=0"
						+ "&tableAliasID=" + selectedTable
						+ "&personCount=1"
						+ "&category=" + CATE_NORMAL
						+ "&tableNbr2=0"
						+ "&minCost=" + tableStatusListTSDisplay[tableIndex].minimumCost
						+ "&serviceRate=" + tableStatusListTSDisplay[tableIndex].serviceRat;

			}
		}else{
			Ext.example.msg('提示', '<font color="green">操作失败, 请先选择餐台.</font>');
		}
	}
});

var checkOutImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/PayOrder.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "结账",
	handler : function(btn) {
		if (selectedTable != "") {
			var tableIndex = -1;
			for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
				if (tableStatusListTSDisplay[i].aliasId == selectedTable) {
					tableIndex = i;
					break;
				}
			}
			if (tableStatusListTSDisplay[tableIndex].status == TABLE_IDLE) {
				Ext.example.msg('提示', '<font color="red">操作失败, 此桌没有下单, 不能结账, 请重新确认.</font>');
			} else {
				location.href = "CheckOut.html?"
						+ "tableID=" + selectedTable
						+ "&pin=" + pin 
						+ "&restaurantID=" + restaurantID;
			}
		}else{
			Ext.example.msg('提示', '<font color="green">操作失败, 请先选择餐台.</font>');
		}
	}
});
				
var orderDeleteImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/DeleteOrder.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "删单",
	handler : function(btn) {
		if (selectedTable != "") {
			var tableIndex = -1;
			for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
				if (tableStatusListTSDisplay[i].aliasId == selectedTable) {
					tableIndex = i;
					break;
				}
			}
			if (tableStatusListTSDisplay[tableIndex].status == TABLE_IDLE) {
				Ext.example.msg('提示', '<font color="red">操作失败, 此桌没有下单, 不能删单.</font>');
			} else {
				dishPushBackWin.show();
			}
		}else{
			Ext.example.msg('提示', '<font color="green">操作失败, 请先选择餐台.</font>');
		}
	}
});				

var tableChangeImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/TableChange.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "转台",
	handler : function(btn) {
		if (selectedTable != "") {
			var tableIndex = -1;
			for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
				if (tableStatusListTSDisplay[i].aliasId == selectedTable) {
					tableIndex = i;
				}
			}
			if (tableStatusListTSDisplay[tableIndex].status == TABLE_BUSY
					&& tableStatusListTSDisplay[tableIndex].category == CATE_NORMAL) {
				tableChangeWin.show();
			} else {
				if (tableStatusListTSDisplay[tableIndex].status != TABLE_BUSY) {
					Ext.example.msg('提示', '<font color="red">操作失败, 空台不能转台.</font>');
				} else {
					Ext.example.msg('提示', '<font color="red">操作失败, 该台不允许转台.</font>');
				}
			}
		}else{
			Ext.example.msg('提示', '<font color="green">操作失败, 请先选择餐台.</font>');
		}
	}
});				

var tableSepImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/TableSeparate.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "并台",
	handler : function(btn) {
		oOrderGroup();
	}
});				
		
var selTabContentGrid = null;
var selTabContentWin = null;
var btnOrderDetail = new Ext.ux.ImageButton({
	imgPath : '../../images/TableOrderDetail.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '查看明细',
	handler : function(e) {
		if (selectedTable != '') {
			var selTabContent = null;
			for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
				if (tableStatusListTSDisplay[i].aliasId == selectedTable) {
					selTabContent = tableStatusListTSDisplay[i];
				}
			}
			if(parseInt(selTabContent.status) != 1){
				return;
			}
			
			var pageSize = 300;
			if(!selTabContentGrid){
				selTabContentGrid = createGridPanel(
					'selTabConten_grid',
					'',
					400,
					'',
					'../../QueryDetail.do?tiem=' + new Date(),
					[
					    [true,false,false,false],
					    ['日期','order_date',100],
					    ['名称','food_name',130],
					    ['单价','unit_price',60, 'right', 'Ext.ux.txtFormat.gridDou'],
					    ['数量','amount', 60, 'right', 'Ext.ux.txtFormat.gridDou'], 
					    ['折扣','discount',60, 'right', 'Ext.ux.txtFormat.gridDou'],
					    ['口味','taste_pref'],
					    ['口味价钱','taste_price', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
					    ['厨房','kitchen', 60],
					    ['服务员','waiter', 60],
					    ['退菜原因', 'cancelReason']
					],
					['order_date','food_name','unit_price','amount','discount','taste_pref',
					 'taste_price','kitchen','waiter','comment', 'cancelReason',
					 'isPaid','isDiscount','isGift','isReturn','message'],
					 [['pin', pin], ['queryType', 'TodayByTbl'], ['tableAlias', selTabContent.aliasId], ['restaurantID', restaurantID]],
					pageSize,
					'',
					null,
					null
				);
				selTabContentGrid.frame = false;
				selTabContentGrid.border = false;
				selTabContentGrid.getStore().on('load', function(store, records, options){
					var sumRow;
					for(var i = 0; i < records.length; i++){
						if(eval(records[i].get('amount') < 0)){
							sumRow = selTabContentGrid.getView().getRow(i);
							sumRow.style.backgroundColor = '#FF0000';
						}
					}
					sumRow = null;
				});
			}
			
			if(!selTabContentWin){
				selTabContentWin = new Ext.Window({
					title : '',
					resizable : false,
					closable : false,
					constrainHeader : true,
					modal : true,
					width : 1150,
					items : [selTabContentGrid],
					bbar : ['->', {
						text : '刷新',
						iconCls : 'btn_refresh',
						handler : function(){
							selTabContentGrid.getStore().reload();
						}
					}, {
						text : '关闭',
						iconCls : 'btn_close',
						handler : function(){
							selTabContentWin.hide();
						}
					}],
					keys : [{
						key : Ext.EventObject.ESC,
						scope : this,
						fn : function(){
							selTabContentWin.hide();
						}
					}],
					listeners : {
						hide : function(thiz){
							selTabContentGrid.getStore().removeAll();
						}
					}
				});
			}
			
			selTabContentWin.setTitle(
					String.format('餐台号:&nbsp;<font color="red">{0}</font>&nbsp;&nbsp;&nbsp;餐台名:&nbsp;<font color="red">{1}</font>&nbsp;&nbsp;&nbsp;用餐人数:&nbsp;<font color="red">{2}</font>'
					,selectedTable, selTabContent.name, selTabContent.customNum));
			selTabContentWin.show(true);
			var tpStore = selTabContentGrid.getStore();
			tpStore.baseParams.tableAlias = selTabContent.aliasId;
			tpStore.baseParams.pin = pin;
			tpStore.baseParams.queryType = 'TodayByTbl';
			tpStore.baseParams.restaurantID = restaurantID;
			tpStore.load({
				params : {
					limit : pageSize,
					start : 0
				}
			});
			
		}else{
			Ext.example.msg('提示', '<font color="green">操作失败, 请先选择餐台.</font>');
		}
	}
});		

var btnMemberRecharge = new Ext.ux.ImageButton({
	imgPath : "../../images/btnMemberRecharge.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "会员充值",
	handler : function(btn) {
		var table_memberRechargeWin = Ext.getCmp('table_memberRechargeWin');
		if(!table_memberRechargeWin){
			table_memberRechargeWin = new Ext.Window({
				id : 'table_memberRechargeWin',
				title : '会员充值',
				closable : false,
				modal : true,
				resizable : false,
				width : 650,
				height : 430,
				keys : [{
					key : Ext.EventObject.ESC,
					scope : this,
					fn : function(){
						table_memberRechargeWin.hide();
					}
				}],
				listeners : {
					hide : function(thiz){
						thiz.body.update('');
					},
					show : function(thiz){
						thiz.center();
						thiz.load({
							url : '../window/client/recharge.jsp',
							scripts : true
						});
					}
				},
				bbar : [{
					xtype : 'checkbox',
					id : 'ts_chbPrintRecharge',
					checked : true,
					boxLabel : '打印充值信息'
				}, '->', {
					text : '充值',
					iconCls : 'icon_tb_recharge',
					handler : function(e){
						rechargeControlCenter({
							reload : true,
							isPrint : Ext.getCmp('ts_chbPrintRecharge').getValue(),
							callback : function(_c){
								
							}
						});
					}
				}, '-', {
					text : '关闭',
					iconCls : 'btn_close',
					handler : function(e){
						table_memberRechargeWin.hide();
					}
				}]
			});
		}
		table_memberRechargeWin.show();
	}
});

var btnControlMember = new Ext.ux.ImageButton({
	imgPath : "../../images/btnAddMember.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "添加会员",
	handler : function(){
		var ts_controlMemberWin = Ext.getCmp('ts_controlMemberWin');
		if(!ts_controlMemberWin){
			ts_controlMemberWin = new Ext.Window({
				title : '添加会员',
				width : 650,
				height : 414,
				modal : true,
				resizable : false,
				closable : false,
				listeners : {
					hide : function(thiz){
						thiz.body.update();
					},
					show : function(thiz){
						thiz.center();
						thiz.load({
							url : '../window/client/controlMember.jsp',
							scripts : true,
							params : {
								
							}
						});
					}
				},
				keys : [{
					key : Ext.EventObject.ESC,
					scope : this,
					fn : function(){
						ts_controlMemberWin.hide();
					}
				}],
				bbar : ['->', {
					text : '保存',
					id : 'btnSaveControlMemberBasicMsg',
					iconCls : 'btn_save',
					handler : function(e){
						if(typeof operateMemberHandler != 'function'){
							Ext.example.msg('提示', '操作失败, 请求异常, 请尝试刷新页面后重试.');
						}else{
							var btnClose = Ext.getCmp('btnCloseControlMemberBasicMsg');
							operateMemberHandler({
								type : 'INSERT',
								setButtonStatus : function(s){
									e.setDisabled(s);
									btnClose.setDisabled(s);
								},
								callback : function(memberData, c, res){
									if(res.success){
										ts_controlMemberWin.hide();
										Ext.example.msg(res.title, res.msg);
									}else{
										Ext.ux.showMsg(res);
									}
								}
							});							
						}
					}
				}, {
					text : '关闭',
					id : 'btnCloseControlMemberBasicMsg',
					iconCls : 'btn_close',
					handler : function(){
						ts_controlMemberWin.hide();
					}
				}]
			});
		}
		ts_controlMemberWin.show();
	}
});

var btnQueryConsumeDetail = new Ext.ux.ImageButton({
	imgPath : '../../images/btnConsumeDetail.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '消费明细',
	handler : function(e){
		var ts_queryMemberOperationWin = Ext.getCmp('ts_queryMemberOperationWin');
		if(!ts_queryMemberOperationWin){
			ts_queryMemberOperationWin = new Ext.Window({
				id : 'ts_queryMemberOperationWin',
				title : '会员操作明细',
				modal : true,
				closable : false,
				resizable : false,
				width : 1000,
				height : 500,
				keys : [{
					key : Ext.EventObject.ESC,
					scope : this,
					fn : function(){
						ts_queryMemberOperationWin.hide();
					}
				}],
				bbar : ['->', {
					text : '关闭',
					iconCls : 'btn_close',
					handler : function(e){
						ts_queryMemberOperationWin.hide();
					}
				}],
				listeners : {
					hide : function(thiz){
						thiz.body.update('');
					},
					show : function(thiz){
						thiz.center();
						thiz.load({
							url : '../window/client/memberOperation.jsp',
							scripts : true,
							params : {
								modal : false
							}
						});
					}
				}
			});
		}
		ts_queryMemberOperationWin.show();
	}
});

var pushBackBut = new Ext.ux.ImageButton({
	imgPath : "../../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn) {
		location.href = "FrontBusinessProtal.html?restaurantID="
				+ restaurantID + "&pin=" + pin;
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : "../../images/ResLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "登出",
	handler : function(btn) {
	}
});	

getIsPaidDisplay = function(_val){
	return eval(_val) == true ? '是' : '否';
};

var regionTree, dishPushBackWin, tableChangeWin;
Ext.onReady(function() {
	// 解决ext中文传入后台变问号问题
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();
	
	// table change pop window
	tableChangeWin = new Ext.Window({
		title : '转台',
		layout : "fit",
		width : 300,
		height : 100,
		closable : false,
		resizable : false,
		modal : true,
		items : [{
			xtype : 'panel',
			layout : 'column',
			frame : true,
			defaults : {
				columnWidth : .5,
				xtype : 'form',
				labelWidth : 30,
				border : false,
			},
			items : [{
				items : [{
					xtype : 'numberfield',
					disabled : true,
					fieldLabel : '原台',
					id : 'tableChangeOutput',
					width : 80
				}]
			}, {
				items : [{
					xtype : 'numberfield',
					fieldLabel : '转至',
					id : 'tableChangeInput',
					width : 80
				}]
			}]
		}],
		bbar : ['->', {
			text : "确定",
			iconCls : 'btn_save',
			id : 'btnTableChange',
			handler : function() {
				var inputTableNbr = tableChangeWin.findById("tableChangeInput").getValue();
				if (inputTableNbr != "") {
					if(inputTableNbr == selectedTable){
						Ext.example.msg('提示', '<font color="red">操作失败, 原台和转至新台一样, 请重新输入新台台号.</font>');
						return;
					}
					var tableIndex = -1;
					for( var i = 0; i < tableStatusListTS.length; i++){
						if (tableStatusListTS[i].aliasId == inputTableNbr) {
							tableIndex = i;
							break;
						}
					}
					
					if(tableIndex == -1){
						Ext.example.msg('提示', '<font color="red">操作失败, 您输入的台号不存在, 请重新输入.</font>');
					}else if(tableStatusListTS[tableIndex].status == TABLE_BUSY) {
						Ext.example.msg('提示', '<font color="red">操作失败, 您输入的台号为就餐状态, 不能转台, 请重新输入.</font>');
					} else {
						var btnSave = Ext.getCmp('btnTableChange');
						var btnCancel = Ext.getCmp('btnCancelTableChange');
						btnSave.setDisabled(true);
						btnCancel.setDisabled(true);
						Ext.Ajax.request({
							url : "../../TransTable.do",
							params : {
								"pin" : pin,
								"oldTableAlias" : selectedTable,
								"newTableAlias" : inputTableNbr
							},
							success : function(response, options) {
								var jr = Ext.decode(response.responseText);
								if (jr.success == true){
									getData();
									Ext.example.msg('提示', '<font color="red">'+jr.msg+'</font>');
									tableChangeWin.hide();
									
								} else {
									jr.callBack = function(){
										location.reload();
									};
									Ext.ux.showMsg(jr);
								}
								btnSave.setDisabled(false);
								btnCancel.setDisabled(false);
							},
							failure : function(response, options) {
								var jr = Ext.decode(response.responseText);
								Ext.ux.showMsg(jr);
								btnSave.setDisabled(false);
								btnCancel.setDisabled(false);
							}
						});
					}
				}else{
					Ext.example.msg('提示', '<font color="red">操作失败, 请输入新台台号.</font>');
				}
			}
		}, {
			text : "关闭",
			id : 'btnCancelTableChange',
			iconCls : 'btn_close',
			handler : function(){
				tableChangeWin.hide();
			}
		}],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnTableChange').handler();
			}
		}, {
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				tableChangeWin.handler();
			}
		}],
		listeners : {
			show : function(thiz){
				tableChangeWin.findById("tableChangeOutput").setValue(selectedTable);
				tableChangeWin.findById("tableChangeInput").setValue("");
				var f = Ext.get("tableChangeInput");
				f.focus.defer(100, f); // 万恶的EXT！为什么这样才可以！？！？
			}
		}
	});
	
	// ***************tableSelectNorthPanel******************
	// soft key board
	var softKBKeyHandlerTS = function(relateItemId, number) {
		var currValue = tableSelectNorthPanel.findById(relateItemId).getValue();
		tableSelectNorthPanel.findById(relateItemId).setValue(currValue + "" + number);
	};
	
	softKeyBoardTS = new Ext.Window({
		layout : "fit",
		width : 117,
		height : 118,
		closeAction : "hide",
		resizable : false,
		// closable : false,
		x : 56,
		y : 146,
		items : [{
			layout : "form",
			labelSeparator : '：',
			labelWidth : 40,
			frame : true,
			buttonAlign : "left",
			items : [{
				layout : "column",
				border : false,
				items : [{
					layout : "form",
					width : 30,
					border : false,
					items : [{
						text : "1",
						xtype : "button",
						handler : function() {
							softKBKeyHandlerTS("tableNumber", "1");
							tableKeyboardSelect();
						}
					}]
				}, {
					layout : "form",
					width : 30,
					border : false,
					items : [{
						text : "2",
						xtype : "button",
						handler : function() {
							softKBKeyHandlerTS("tableNumber", "2");
							tableKeyboardSelect();
						}
					}]
				}, {
					layout : "form",
					width : 30,
					border : false,
					items : [{
						text : "3",
						xtype : "button",
						handler : function() {
							softKBKeyHandlerTS("tableNumber", "3");
							tableKeyboardSelect();
						}
					}]
				}, {
					layout : "form",
					width : 30,
					border : false,
					items : [{
						text : "4",
						xtype : "button",
						handler : function(){
							softKBKeyHandlerTS("tableNumber", "4");
							tableKeyboardSelect();
						}
					}]
				}, {
					layout : "form",
					width : 30,
					border : false,
					items : [{
						text : "5",
						xtype : "button",
						handler : function(){
							softKBKeyHandlerTS("tableNumber", "5");
							tableKeyboardSelect();
						}
					}]
				}, {
					layout : "form",
					width : 30,
					border : false,
					items : [ {
						text : "6",
						xtype : "button",
						handler : function(){
							softKBKeyHandlerTS("tableNumber", "6");
							tableKeyboardSelect();
						}
					} ]
				}, {
					layout : "form",
					width : 30,
					border : false,
					items : [ {
						text : "7",
						xtype : "button",
						handler : function() {
							softKBKeyHandlerTS("tableNumber", "7");
							tableKeyboardSelect();
						}
					} ]
				}, {
					layout : "form",
					width : 30,
					border : false,
					items : [ {
						text : "8",
						xtype : "button",
						handler : function() {
							softKBKeyHandlerTS("tableNumber", "8");
							tableKeyboardSelect();
						}
					} ]
				}, {
					layout : "form",
					width : 30,
					border : false,
					items : [ {
						text : "9",
						xtype : "button",
						handler : function() {
							softKBKeyHandlerTS("tableNumber", "9");
							tableKeyboardSelect();
						}
					} ]
				}, {
					layout : "form",
					width : 30,
					border : false,
					items : [ {
						text : "0",
						xtype : "button",
						handler : function() {
							softKBKeyHandlerTS("tableNumber", "0");
							tableKeyboardSelect();
						}
					} ]
				}, {
					layout : "form",
					width : 60,
					border : false,
					items : [ {
						text : "&nbsp;清 空&nbsp;",
						xtype : "button",
						handler : function() {
							tableSelectNorthPanel.findById("tableNumber").setValue("");
							tableKeyboardSelect();
						}
					} ]
				} ]
			} ]
		} ],
		listeners : {
			show : function(thiz) {
				var f = Ext.get("tableNumber");
				f.focus.defer(100, f);
			}
		}
	});
	
	dishPushBackWin = new Ext.Window({
		title : '删单',
		layout : "fit",
		width : 170,
		height : 100,
		closeAction : "hide",
		closable : false,
		resizable : false,
		modal : true,
		items : [ {
			layout : "form",
			labelWidth : 30,
			border : false,
			frame : true,
			items : [ {
				xtype : "textfield",
				inputType : "password",
				fieldLabel : "密码",
				id : "dishPushBackPwd",
				width : 100
			} ]
		} ],
		bbar : ['->', {
			text : "确定",
			id : 'btnDeleteTableOrder',
			iconCls : 'btn_save',
			handler : function() {
				var dishPushBackPwd = Ext.getCmp("dishPushBackPwd").getValue();
				Ext.getCmp("dishPushBackPwd").setValue("");
				var pwdTrans;
				if (dishPushBackPwd != "") {
					pwdTrans = MD5(dishPushBackPwd);
				} else {
					pwdTrans = dishPushBackPwd;
				}
				var btnSave = Ext.getCmp('btnDeleteTableOrder');
				var btnCancel = Ext.getCmp('btnCancelDeleteTableOrder');
				btnSave.setDisabled(true);
				btnCancel.setDisabled(true);
//				dishPushBackWin.hide();
				Ext.Ajax.request({
					url : "../../VerifyPwd.do",
					params : {
						"pin" : Request["pin"],
						"type" : 1,
						"pwd" : pwdTrans
					},
					success : function(response, options) {
						var resultJSON = Ext.decode(response.responseText);
						if (resultJSON.success == true) {
							Ext.Ajax.request({
								url : "../../CancelOrder.do",
								params : {
									"pin" : pin,
									"tableAlias" : selectedTable
								},
								success : function(response, options) {
									var resultJSON1 = Ext.decode(response.responseText);
									if (resultJSON1.success == true) {
										Ext.MessageBox.show({
											msg : resultJSON1.data,
											width : 300,
											buttons : Ext.MessageBox.OK,
											fn : function() {
												location.reload();
											}
										});
									} else {
										Ext.MessageBox.show({
											msg : resultJSON.data,
											width : 300,
											buttons : Ext.MessageBox.OK
										});
									}
								},
								failure : function(response, options) {
									
								}
							});
							
							Ext.MessageBox.show({
								msg : resultJSON.data,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						} else {
							Ext.MessageBox.show({
								msg : resultJSON.data,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						}
						btnSave.setDisabled(false);
						btnCancel.setDisabled(false);
					},
					failure : function(response, options){
						btnSave.setDisabled(false);
						btnCancel.setDisabled(false);
					}
				});
			}
		}, {
			text : "关闭",
			id : 'btnCancelDeleteTableOrder',
			iconCls : 'btn_close',
			handler : function() {
				dishPushBackWin.hide();
				dishPushBackWin.findById("dishPushBackPwd").setValue("");
			}
		}],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnDeleteTableOrder').handler();
			}
		}],
		listeners : {
			show : function(thiz) {
				var f = Ext.get("dishPushBackPwd");
				f.focus.defer(100, f);
			}
		}
	});	

	var tableSelectNorthPanel = new Ext.form.FormPanel({
		region : "north",
		frame : true,
		height : 45,
		labelSeparator : '：',
		labelWidth : 30,
		border : false,
		items : [ {
			border : false,
			layout : "form",
			items : [ {
				layout : "column",
				border : false,
				anchor : '98%',
				labelSeparator : '：',
				items : [ {
					layout : "form",
					width : 185,
					labelWidth : 30,
					style : "padding-top:7px;padding-left:15px;",
					border : false,
					items : [ {
						xtype : "numberfield",
						fieldLabel : "<b>桌号</b>",
						name : "tableNumber",
						id : "tableNumber",
						anchor : "90%",
						listeners : {
							focus : function(thiz) {
								softKeyBoardTS.show();
							}
						}
					} ]
				}, {
					width : 800,
					contentEl : "tableSumInfo"
				} ]
			} ]
		} ]
	});

	// ***************tableSelectCenterPanel******************
	var tableSelectCenterPanel = new Ext.Panel({
		region : "center",
		layout : "border",
		border : false,
		bodyStyle : "background-color:#d8ebef;",
		items : [ {
			region : "center",
			border : false,
			bodyStyle : "background-color:#d8ebef;",
			contentEl : "tableDisplay",
			autoScroll : true
		}, {
			region : "south",
			border : false,
			bodyStyle : "background-color:#d8ebef;",
			height : 60,
			contentEl : "tableListPageCount"
		}, {
			region : "north",
			border : false,
			bodyStyle : "background-color:#d8ebef;",
			height : 40,
			contentEl : "tableListRegionInfo"
		} ]

	});			

	// ***************tableSelectEastPanel******************
	regionTree = new Ext.tree.TreePanel({
		id : 'regionTree',
		autoScroll : true,
		animate : true,
		root : new Ext.tree.AsyncTreeNode({
			id : "regionTreeRoot",
			text : "全部区域",
			regionID : -1,
			loader : new Ext.tree.TreeLoader({
				url : "../../QueryRegionTree.do",
				baseParams : {
					"pin" : pin
				},
				listeners : {
					load : function(){
						regionTree.expandAll();
					}
				}
			}),
		}),
		rootVisible : true,
		border : false,
		lines : true,
		collapsed : false,
		containerScroll : true,
		listeners : {
			'click' : function(node, event) {
				selectedStatus = null;
				node.attributes.tableStatus = null;
				tableListReflash(node);
			}
		}
	});			

	var tableSelectWestPanel = new Ext.Panel({
		region : "west",
		title : "区域",
		width : 160,
		border : true,
		collapsible : true,
		collapsed : false,
		titleCollapse : true,
		split : true,
		items : regionTree
	});			

	var centerTabPanel = new Ext.Panel({
		region : "center",
		tbar : new Ext.Toolbar({
			height : 55,
			items : [ {
				text : "&nbsp;&nbsp;&nbsp;",
				xtype : 'tbtext'
			}, 
			dishesOrderImgBut, 
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext'}, 
			checkOutImgBut, 
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext'}, 
			orderDeleteImgBut, 
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext'}, 
			tableChangeImgBut, 
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext'}, 
			tableSepImgBut, 
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext' },
			btnOrderDetail,
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext' },
			btnMemberRecharge,
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext' },
			btnControlMember,
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext' },
			btnQueryConsumeDetail,
			"->",
			pushBackBut, 
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext' }, 
			logOutBut ]
		}),
		layout : "border",
		items : [ tableSelectNorthPanel, tableSelectCenterPanel, tableSelectWestPanel]
	});

	new Ext.Viewport({
		layout : "border",
		id : "viewport",
		items : [{
			region : "north",
			bodyStyle : "background-color:#DFE8F6;",
			html : "<h4 style='padding:10px;font-size:150%;float:left;'>无线点餐网页终端</h4><div id='optName' class='optName'></div>",
			height : 50,
			border : false,
			margins : '0 0 0 0'
		},
		centerTabPanel,
		{
			region : "south",
			height : 30,
			layout : "form",
			frame : true,
			border : false,
			html : "<div style='font-size:11pt; text-align:center;'><b>版权所有(c) 2011 智易科技</b></div>"
		} ]
	});
});

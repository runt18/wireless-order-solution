
var dishesOrderImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/InsertOrder.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "点菜",
	handler : function(btn) {
		if (selectedTable != "") {
			var temp = null;
			for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
				temp = tableStatusListTSDisplay[i];
				if (temp.alias == selectedTable) {
					if (temp.statusValue == TABLE_BUSY) {
						setDynamicKey("OrderMain.html", 'restaurantID=' + restaurantID
								+ "&tableAliasID=" + temp.alias
								+ "&ts=1" 
								+ "&personCount=" + temp.customNum
								+ "&category=" + temp.categoryValue);
					} else if (temp.statusValue == TABLE_IDLE) {
						setDynamicKey("OrderMain.html", 'restaurantID=' + restaurantID
								+ "&ts=0"
								+ "&tableAliasID=" + selectedTable
								+ "&category=" + CATE_NORMAL);
					}
					break;
				}
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
			var temp = null;
			for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
				temp = tableStatusListTSDisplay[i];
				if (temp.alias == selectedTable) {
					if (temp.statusValue == TABLE_IDLE) {
						Ext.example.msg('提示', '<font color="red">操作失败, 此桌没有下单, 不能结账, 请重新确认.</font>');
					} else {
						setDynamicKey("CheckOut.html", 'restaurantID=' + restaurantID
								+ "&tableID=" + selectedTable);
					}
					break;
				}
			}
		}else{
			Ext.example.msg('提示', '<font color="green">操作失败, 请先选择餐台.</font>');
		}
	}
});
				
/*var orderDeleteImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/DeleteOrder.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "删单",
	handler : function(btn) {
		if (selectedTable != "") {
			var temp = null;
			for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
				temp = tableStatusListTSDisplay[i];
				if (temp.alias == selectedTable) {
					if (temp.statusValue == TABLE_IDLE) {
						Ext.example.msg('提示', '<font color="red">操作失败, 此桌没有下单, 不能删单.</font>');
					} else {
						Ext.Ajax.request({
							url : "../../CancelOrder.do",
							params : {
								isCookie : true,
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
					}
					break;
				}
			}
		}else{
			Ext.example.msg('提示', '<font color="green">操作失败, 请先选择餐台.</font>');
		}
	}
});	*/			

var tableChangeImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/TableChange.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "转台",
	handler : function(btn) {
		if (selectedTable != "") {
			var temp = null;
			for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
				temp = tableStatusListTSDisplay[i];
				if (temp.alias == selectedTable) {
					if (temp.statusValue == TABLE_BUSY
							&& temp.categoryValue == CATE_NORMAL) {
						tableChangeWin.show();
					} else {
						if (temp.statusValue != TABLE_BUSY) {
							Ext.example.msg('提示', '<font color="red">操作失败, 空台不能转台.</font>');
						} else {
							Ext.example.msg('提示', '<font color="red">操作失败, 该台不允许转台.</font>');
						}
					}
					break;
				}
			}
		}else{
			Ext.example.msg('提示', '<font color="green">操作失败, 请先选择餐台.</font>');
		}
	}
});				

var tableSepImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/btnInsertOrderGroup.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "并台下单",
	handler : function(btn) {
		Ext.Msg.show({
			title : '温馨提示',
			msg : '此功能正在维护中, 请稍候再试.谢谢.',
			icon: Ext.MessageBox.WARNING,
			buttons: Ext.Msg.OK
		});
		return;
		oOrderGroup({
			type : 1
		});
	}
});

var btnPayOrderGroup = new Ext.ux.ImageButton({
	imgPath : "../../images/btnPayOrderGroup.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "合单结账",
	handler : function(btn) {
		Ext.Msg.show({
			title : '温馨提示',
			msg : '此功能正在维护中, 请稍候再试.谢谢.',
			icon: Ext.MessageBox.WARNING,
			buttons: Ext.Msg.OK
		});
		return;
		/**/
		oOrderGroup({
			type : 2
		});
	}
});

function jiaoBanDaYin(e){
	var tempMask = new Ext.LoadMask(document.body, {
		msg : '正在打印请稍候.......',
		remove : true
	});
	tempMask.show();
	Ext.Ajax.request({
		url : "../../PrintOrder.do",
		params : {
			onDuty : dutyRange.onDutyFormat,
			offDuty : dutyRange.offDutyFormat,
			'printType' : e == null ? 4 : 5
		},
		success : function(response, options) {
			tempMask.hide();
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			Ext.example.msg('提示', (resultJSON.msg + (omsg.length > 0 ? ('<br/>'+omsg) : '')));
			if(omsg.length > 0)
				businessStatWin.destroy();
			omsg = '';
		},
		failure : function(response, options) {
			tempMask.hide();
			Ext.ux.showMsg(Ext.decode(response.responseText));
		}
	});
}

function shiftButHandler(){
	businessStatWin = new Ext.Window({
		title : '<font style="color:green;">交班表</font> -- 交班人 : ' + document.getElementById("optName").innerHTML,
//		id : 'businessDetailWin',
		width : 885,
		height : 580,
		closable : false,
		modal : true,
		resizable : false,	
		layout: 'fit',
		bbar : ['->', {
			text : "交班",
			icon: '../../images/user.png',
			id : 'btnJiaoBan',
			handler : function() {
				dutyRange = getDutyRange();
				Ext.MessageBox.show({
					msg : "确认进行交班？",
					width : 300,
					buttons : Ext.MessageBox.YESNO,
					fn : function(btn){
						if(btn == "yes"){
							Ext.Ajax.request({
								url : "../../DoShift.do",
								success : function(response, options) {
									var resultJSON = Ext.util.JSON.decode(response.responseText);
									if (resultJSON.success == true) {
										omsg = resultJSON.msg;
										dutyRange = resultJSON.other.dutyRange;
										jiaoBanDaYin(null);
									} else {
										Ext.MessageBox.show({
											msg : resultJSON.msg,
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
			}
		}, {
			text : '预打',
			id : 'btnJiaoBanDaYin',
			icon: '../../images/printShift.png',
			handler : function(e){
				dutyRange = getDutyRange();
				jiaoBanDaYin(e);
			}
		},{
			text : '关闭',
			iconCls : 'btn_close',
			handler : function(){
				businessStatWin.destroy();
			}
		}],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				businessStatWin.destroy();
			}
		}],
		listeners : {
			hide : function(thiz){
				thiz.body.update('');
			},
			show : function(thiz){
				thiz.load({
					autoLoad : false,
					url : '../window/history/businessStatistics.jsp',
					scripts : true,
					nocache : true,
					text : '功能加载中, 请稍后......',
					params : {
						queryPattern : 4,
						queryType : 0
					}
				});
			}
		}
	});
	businessStatWin.show();
	businessStatWin.center();
}

function riJieDaYin(e){

	var tempMask = new Ext.LoadMask(document.body, {
		msg : '正在打印请稍候.......',
		remove : true
	});
	tempMask.show();
	Ext.Ajax.request({
		url : "../../PrintOrder.do",
		params : {
			onDuty : dutyRange.onDutyFormat,
			offDuty : dutyRange.offDutyFormat,
			'printType' : e == null ? 6 : 5
		},
		success : function(response, options) {
			tempMask.hide();
			var jr = Ext.util.JSON.decode(response.responseText);
			Ext.example.msg('提示', (jr.msg + (omsg.length > 0 ? ('<br/>'+omsg) : '')));
			if(omsg.length > 0)
				businessStatWin.destroy();
			omsg = '';
		},
		failure : function(response, options) {
			tempMask.hide();
			Ext.ux.showMsg(Ext.decode(response.responseText));
		}
	});
}

function dailySettleButHandler(){
	businessStatWin = new Ext.Window({
		title : '<font style="color:green;">日结表</font> -- 日结人 : ' + document.getElementById("optName").innerHTML,
//		id : 'businessDetailWin',
		width : 885,
		height : 580,
		closable : false,
		modal : true,
		resizable : false,	
		layout: 'fit',
		bbar : ['->',{
			text : "日结",
			id : 'btnRiJie',
			icon: '../../images/user.png',
			handler : function() {
				dutyRange = getDutyRange();
				Ext.MessageBox.show({
					msg : "确认进行日结？",
					width : 300,
					buttons : Ext.MessageBox.YESNO,
					fn : function(btn) {
						if (btn == "yes") {
							businessStatWin.destroy();
							
							// 未交班帳單檢查
							Ext.Ajax.request({
								url : "../../DailySettleCheck.do",
								success : function(response, options) {
									var resultJSON = Ext.util.JSON.decode(response.responseText);
									if (eval(resultJSON.success == true)) {
										doDailySettle();
									} else {
										document.getElementById("unShiftBillWarnMsg").innerHTML = resultJSON.msg;
										unShiftBillWarnWin.show();
									}
								},
								failure : function(response, options) {
									Ext.MessageBox.show({
										msg : " Unknown page error ",
										width : 300,
										buttons : Ext.MessageBox.OK
									});
								}
							});
						}
					}
				});
			}
		}, {
			text : '预打',
			id : 'btnRiJieDaYin',
			icon: '../../images/printShift.png',
			handler : function(e){
				dutyRange = getDutyRange();
				riJieDaYin(e);
			}
		}, {
			text : '关闭',
			iconCls : 'btn_close',
			handler : function(){
				businessStatWin.destroy();
			}
		}],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				businessStatWin.destroy();
			}
		}],
		listeners : {
			hide : function(thiz){
				thiz.body.update('');
			},
			show : function(thiz){
				thiz.load({
					autoLoad : false,
					url : '../window/history/businessStatistics.jsp',
					scripts : true,
					nocache : true,
					text : '功能加载中, 请稍后......',
					params : {
						queryPattern : 4,
						queryType : 1
					}
				});
			}
		}
	});
	businessStatWin.show();
	businessStatWin.center();
}


var shiftBut = new Ext.ux.ImageButton({
	imgPath : "../../images/shift.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "交班",
	handler : function(btn) {
		shiftButHandler();
	}
});

var dailySettleBut = new Ext.ux.ImageButton({
	imgPath : "../../images/dailySettle.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "日结",
	handler : function(btn) {
		dailySettleButHandler();
	}
});

var billsBut = new Ext.ux.ImageButton({
	imgPath : "../../images/bill.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "账单",
	handler : function(btn) {
		location.href = "Bills.html";
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
				if (tableStatusListTSDisplay[i].alias == selectedTable) {
					selTabContent = tableStatusListTSDisplay[i];
					break;
				}
			}
			if(parseInt(selTabContent.statusValue) != 1){
				Ext.example.msg('提示', '<font color="green">操作失败, 该台非就餐状态, 无操作明细.</font>');
				return;
			}
			
			var pageSize = 300;
			if(!selTabContentGrid){
				selTabContentGrid = createGridPanel(
					'selTabConten_grid',
					'',
					400,
					'',
					'../../QueryDetail.do',
					[
					    [true,false,false,false],
					    ['日期','orderDateFormat',100],
					    ['名称','displayFoodName',130],
					    ['单价','unitPrice',60, 'right', 'Ext.ux.txtFormat.gridDou'],
					    ['数量','count', 60, 'right', 'Ext.ux.txtFormat.gridDou'], 
//					    ['折扣','discount',60, 'right', 'Ext.ux.txtFormat.gridDou'],
					    ['口味','tasteGroup.tastePref'],
					    ['口味价钱','tasteGroup.tastePrice', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
					    ['厨房','kitchen.name', 60],
					    ['服务员','waiter', 60],
					    ['退菜原因', 'cancelReason.reason']
					],
					OrderFoodRecord.getKeys(),
					[ ['queryType', 'TodayByTbl'], ['tableAlias', selTabContent.alias], ['restaurantID', restaurantID]],
					pageSize,
					''
				);
				selTabContentGrid.frame = false;
				selTabContentGrid.border = false;
				
				selTabContentGrid.getStore().on('load', function(store, records, options){
					store.add(new Ext.data.Record({'orderDateFormat':'汇总', 'name':'-----', 'unitPrice':0, 'count':0, 'tasteGroup.tastePref':'', 'tasteGroup.tastePrice':'', 'kitchen.name':'', 'waiter': '', 'cancelReason.reason':''}));
					var sumRow;
					for (var i = 0; i < store.getCount(); i++) {
						Ext.ux.formatFoodName(store.getAt(i), 'displayFoodName', 'name');
					}
					if(store.getCount() > 0){
						Ext.Ajax.request({
							url : '../../QueryOrderByCalc.do',
							params : {tableID : selectedTable, calc : false},
							success : function(res, opt){
								var jr = Ext.decode(res.responseText);
								
								sumRow = selTabContentGrid.getView().getRow(store.getCount() - 1);	
								sumRow.style.backgroundColor = '#EEEEEE';			
								for(var i = 0; i < selTabContentGrid.getColumnModel().getColumnCount(); i++){
									var sumCell = selTabContentGrid.getView().getCell(store.getCount() - 1, i);
									sumCell.style.fontSize = '15px';
									sumCell.style.fontWeight = 'bold';
									sumCell.style.color = 'green';
								}
								selTabContentGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '';
								selTabContentGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '';
								selTabContentGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '';
								selTabContentGrid.getView().getCell(store.getCount()-1, 4).innerHTML = '';
								selTabContentGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '';
								selTabContentGrid.getView().getCell(store.getCount()-1, 6).innerHTML = '';
								selTabContentGrid.getView().getCell(store.getCount()-1, 7).innerHTML = '';
								selTabContentGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '总金额 :';
								selTabContentGrid.getView().getCell(store.getCount()-1, 9).innerHTML = jr.other.order.actualPrice.toFixed(2);
							},
							failure : function(){
							
							}
						});
						
						

					}
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
			selTabContentWin.show();
			selTabContentWin.center();
			var tpStore = selTabContentGrid.getStore();
			tpStore.baseParams.tableAlias = selTabContent.alias;
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
				width : 680,
				height : 350,
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
								table_memberRechargeWin.hide();
							}
						});
					}
				}, {
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
		if(!ts_controlMemberWin){
			ts_controlMemberWin = new Ext.Window({
				title : '添加会员',
				width : 650,
				height : 296,
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
								otype : 'insert'
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
		if(!ts_queryMemberOperationWin){
			ts_queryMemberOperationWin = new Ext.Window({
				id : 'ts_queryMemberOperationWin',
				title : '会员操作明细',
				modal : true,
				closable : false,
				resizable : false,
				width : 1200,
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

var btnMemberPointConsume = new Ext.ux.ImageButton({
	imgPath : "../../images/btnMemberPointConsume.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "积分消费",
	handler : function(btn) {
		if(!memberPointConsumeWin){
			memberPointConsumeWin = new Ext.Window({
				title : '会员积分消费',
				closable : false,
				modal : true,
				resizable : false,
				width : 200,
				items : [{
					xtype : 'panel',
					frame : true,
					defaults : {
						xtype : 'form',
						labelWidth : 60,
						defaults : {
							width : 100
						}
					},
					items : [{
						items : [{
							xtype : 'textfield',
							id : 'numMemberNameForConsumePoint',
							fieldLabel : '会员名称',
							disabled : true
						}]
					}, {
						items : [{
							xtype : 'textfield',
							id : 'numMemberTypeForConsumePoint',
							fieldLabel : '会员类型 ',
							disabled : true
						}]
					}, {
						items : [{
							xtype : 'numberfield',
							id : 'numMemberMobileForConsumePoint',
							fieldLabel : '手机号码',
							regex : Ext.ux.RegText.mobile.reg,
							regexText : Ext.ux.RegText.mobile.error,
							listeners : {
								render : function(thiz){
									new Ext.KeyMap(thiz.getId(), [{
										key : Ext.EventObject.ENTER,
										scope : this,
										fn : function(){
											memberPointConsume({otype:1, read:1});
										}
									}]);
								}
							}
						}]
					}, {
						items : [{
							xtype : 'numberfield',
							id : 'numMemberCardForConsumePoint',
							fieldLabel : '会员卡',
							listeners : {
								render : function(thiz){
									new Ext.KeyMap(thiz.getId(), [{
										key : Ext.EventObject.ENTER,
										scope : this,
										fn : function(){
											memberPointConsume({otype:1, read:2});
										}
									}]);
								}
							}
						}]
					}, {
						xtype : 'panel',
						html : '<input type="button" value="读手机号码" onClick="memberPointConsume({otype:1, read:1})">'
							+ '<input type="button" value="读会员卡" onClick="memberPointConsume({otype:1, read:2})">'
					}, {
						items : [{
							xtype : 'numberfield',
							id : 'numMemberPointForConsumePoint',
							fieldLabel : '当前积分',
							disabled : true
						}]
					}, {
						items : [{
							xtype : 'numberfield',
							id : 'numConsumePointForConsumePoint',
							fieldLabel : '消费积分',
							allowBlank : false,
							validator : function(v){
								if(memberPointConsumeWin.member == null){
									return false;
								}else{
									if(v > 0 && Math.abs(v) < memberPointConsumeWin.member['point']){
										return true;
									}else{
										return '请输入大于0, 小于当前积分的消费积分['+memberPointConsumeWin.member['point']+']的数值.';
									}
								}
							}
						}]
					}]
				}],
				keys : [{
					key : Ext.EventObject.ESC,
					scope : this,
					fn : function(){
						memberPointConsumeWin.hide();
					}
				}],
				bbar : ['->', {
					text : '保存',
					iconCls : 'btn_save',
					handler : function(){
						memberPointConsume({otype:2});
					}
				}, {
					text : '关闭',
					iconCls : 'btn_close',
					handler : function(){
						memberPointConsumeWin.hide();
					}
				}],
				listeners : {
					hide : function(){
						memberPointConsumeWin.member == null;
						memberPointConsumeWinSetData();
						Ext.getCmp('numConsumePointForConsumePoint').setValue();
						Ext.getCmp('numConsumePointForConsumePoint').clearInvalid();
					}
				}
			});
			
		}
		memberPointConsumeWin.show();
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : "../../images/ResLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "登出",
	handler : function(btn) {
		Ext.Ajax.request({
			url : '../../LoginOut.do',
			success : function(){
				location.href = '../Login.html';
			},
			failure : function(){
				
			}
		});
	}
});	

getIsPaidDisplay = function(_val){
	return eval(_val) == true ? '是' : '否';
};

var regionTree, dishPushBackWin, tableChangeWin;

//-------------------交班, 日结-----------

doDailySettle = function() {
	Ext.Ajax.request({
		url : "../../DailySettleExec.do",
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (eval(resultJSON.success == true)) {
				omsg = resultJSON.msg;
				dutyRange = resultJSON.other.dutyRange;
				riJieDaYin(null);
			} else {
				Ext.ux.showMsg(resultJSON);
			}
		},
		failure : function(response, options) {
			Ext.MessageBox.show({
				msg : " Unknown page error ",
				width : 300,
				buttons : Ext.MessageBox.OK
			});
		}
	});
};
var unShiftBillWarnWin = new Ext.Window({
	layout : "fit",
	width : 300,
	height : 200,
	closeAction : "hide",
	resizable : false,
	closable : false,
	items : [ {
		border : false,
		frame : true,
		contentEl : "unShiftBillWarnDiv"
	} ],
	buttonAlign : 'center',
	buttons : [ {
		text : "确定",
		handler : function() {
			unShiftBillWarnWin.hide();
			
			
			doDailySettle();
		}
	}, {
		text : "取消",
		handler : function() {
			unShiftBillWarnWin.hide();
			
		}
	} ]
});

//-----------------------------------
var keyboardFN;
Ext.onReady(function() {
	getOperatorName('../../', function(staff){
    	//restaurantID = staff.restaurantId;
    });

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
				border : false
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
						if (tableStatusListTS[i].alias == inputTableNbr) {
							tableIndex = i;
							break;
						}
					}
					
					if(tableIndex == -1){
						Ext.example.msg('提示', '<font color="red">操作失败, 您输入的台号不存在, 请重新输入.</font>');
					}else if(tableStatusListTS[tableIndex].statusValue == TABLE_BUSY) {
						Ext.example.msg('提示', '<font color="red">操作失败, 您输入的台号为就餐状态, 不能转台, 请重新输入.</font>');
					} else {
						var btnSave = Ext.getCmp('btnTableChange');
						var btnCancel = Ext.getCmp('btnCancelTableChange');
						btnSave.setDisabled(true);
						btnCancel.setDisabled(true);
						Ext.Ajax.request({
							url : "../../TransTable.do",
							params : {
								
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
				Ext.Ajax.request({
					url : "../../VerifyPwd.do",
					params : {
						"type" : 1,
						"pwd" : pwdTrans
					},
					success : function(response, options) {
						var resultJSON = Ext.decode(response.responseText);
						if (resultJSON.success == true) {
							Ext.Ajax.request({
								url : "../../CancelOrder.do",
								params : {
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
		labelWidth : 30,
		border : false,
		items : [{
			contentEl : "tableSumInfo"
		}]
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
			regionId : -1,
			loader : new Ext.tree.TreeLoader({
				url : "../../QueryRegion.do",
				baseParams : {
					dataSource : 'tree'
				},
				listeners : {
					load : function(){
						regionTree.expandAll();
					}
				}
			})
		}),
		rootVisible : true,
		border : false,
		lines : true,
		collapsed : false,
		containerScroll : true,
		listeners : {
			click : function(node, event) {
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
//			orderDeleteImgBut, 
//			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext'}, 
			tableChangeImgBut, 
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext'}, 
			tableSepImgBut, 
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext' },
			btnPayOrderGroup,
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext' },
			btnOrderDetail,
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext' },
			btnMemberRecharge,
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext' },
			btnControlMember,
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext' },
			btnQueryConsumeDetail,
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext' },			
			btnMemberPointConsume,
			"->",
			billsBut,
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext' },			
			shiftBut,
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext' },			
			dailySettleBut,
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext' },		
			logOutBut ]
		}),
		layout : "border",
		items : [ tableSelectNorthPanel, tableSelectCenterPanel, tableSelectWestPanel]
	});

	initMainView(null, centerTabPanel, null);
	
	keyboardFN = function(){
		var inputAliasWin = Ext.getCmp('inputAliasWin');
		if(!inputAliasWin){
			var alias = new Ext.form.NumberField({
				xtype : 'numberfield',
				hideLabel : true,
				style : 'line-height:100px;font-size:100px;font-weight:bold;text-align:center;color:red;',
				width : 350,
				height : 110,
				maxValue : 65535,
				minValue : 1,
				allowBlank : false,
				listeners : {
					render : function(thiz){
						thiz.getEl().dom.setAttribute("maxLength", 5);
					}
				}
			});
			var btnPlus = new Ext.Button({
				text : '点菜(+)',
				handler : function(){
					if (alias.isValid()) {
						var temp = null, has = false;
						for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
							temp = tableStatusListTSDisplay[i];
							if (temp.alias == alias.getValue()) {
								if (temp.statusValue == TABLE_BUSY) {
									setDynamicKey("OrderMain.html", 'restaurantID=' + restaurantID
											+ "&ts=" + TABLE_BUSY
											+ "&tableAliasID=" + alias.getValue()
											+ "&category=" + CATE_NORMAL);
								} else if (temp.statusValue == TABLE_IDLE) {
									setDynamicKey("OrderMain.html", 'restaurantID=' + restaurantID
											+ "&ts=0"
											+ "&tableAliasID=" + alias.getValue()
											+ "&category=" + CATE_NORMAL);
//										alias.selectText();
//										Ext.example.msg('提示', '该餐台已结账, 请重新输入.');
								}
								has = true;
								break;
							}
						}
						if(!has){
							alias.selectText();
							Ext.example.msg('提示', '该餐台号不存在, 请重新输入.');
						}
					}else{
						alias.selectText();
						Ext.example.msg('提示', '请先输入正确餐台号(1~65535).');
					}
				},
				listeners : {
					render : function(thiz){
						thiz.getEl().setWidth(100, true);
					}
				}
			});
			var btnSave = new Ext.Button({
				text : '结账(ENTER)',
				handler : function(){
					if (alias.isValid()) {
						var temp = null, has = false;
						for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
							temp = tableStatusListTSDisplay[i];
							if (temp.alias == alias.getValue()) {
								if (temp.statusValue == TABLE_BUSY) {
									setDynamicKey("CheckOut.html", 'restaurantID=' + restaurantID
												+ "&tableID=" + alias.getValue());
								} else if (temp.statusValue == TABLE_IDLE) {
									alias.selectText();
									Ext.example.msg('提示', '该餐台已结账, 请重新输入.');
								}
								has = true;
								break;
							}
						}
						if(!has){
							alias.selectText();
							Ext.example.msg('提示', '该餐台号不存在, 请重新输入.');
						}
					}else{
						alias.selectText();
						Ext.example.msg('提示', '请先输入正确餐台号(1~65535).');
					}
				},
				listeners : {
					render : function(thiz){
						thiz.getEl().setWidth(100, true);
					}
				}
			});
			var btnClose = new Ext.Button({
				text : '关闭(ESC)',
				handler : function(){ inputAliasWin.hide(); },
				listeners : {
					render : function(thiz){
						thiz.getEl().setWidth(100, true);
					}
				}
			});
			inputAliasWin = new Ext.Window({
				id : 'inputAliasWin',
				title : '请输入餐台编号',
				modal : true,
				resiza : false,
				closable : false,
				items : [{
					layout : 'form',
					frame : true,
					items : [alias],
					buttonAlign : 'center',
					buttons : [btnPlus, btnSave, btnClose]
				}],
				keys : [{
					key : Ext.EventObject.ESC,
					scope : this,
					fn : function(){
						inputAliasWin.hide();
					}
				}, {
					key : Ext.EventObject.ENTER,
					scope : this,
					fn : function(){
						btnSave.handler();
					}
				}, {
					key : 107,
					scope : this,
					fn : function(){
						btnPlus.handler();
					}
				}],
				listeners : {
					show : function(){
						alias.setValue();
						alias.clearInvalid();
						alias.focus(alias, 100);
					}
				}
			});
		}
		inputAliasWin.show();
	};
	
	new Ext.KeyMap(document.body, [{
		key: 107,
		scope : this,
		fn: function(){
			keyboardFN();
		}
	}]);
});

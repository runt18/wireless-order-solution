
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

var shiftBut = new Ext.ux.ImageButton({
	imgPath : "../../images/shift.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "交班",
	handler : function(btn) {
		Ext.Ajax.request({
			url : '../../QueryDailySettleByNow.do',
			params : {
				isCookie : true,
				queryType : 0
			},
			success : function(res, opt){
				var jr = Ext.util.JSON.decode(res.responseText);
				if(jr.success){
					shiftCheckDate = jr;
					shiftCheckDate.otype = 0;
					dailySettleCheckTableWin.show();
					dailySettleCheckTableWin.center();
				}else{
					Ext.Msg.show({
						title : '错误',
						msg : '加载交班信息失败.'
					});
				}
			},
			failure : function(res, opt){
				Ext.Msg.show({
					title : '错误',
					msg : '加载交班信息失败.'
				});
			}
		});
	}
});

var dailySettleBut = new Ext.ux.ImageButton({
	imgPath : "../../images/dailySettle.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "日结",
	handler : function(btn) {
			Ext.Ajax.request({
				url : '../../QueryDailySettleByNow.do',
				params : {
					isCookie : true,
					queryType : 1
				},
				success : function(res, opt){
					var jr = Ext.util.JSON.decode(res.responseText);
					if(jr.success){
						shiftCheckDate = jr;
						shiftCheckDate.otype = 1;
						dailySettleCheckTableWin.show();
						dailySettleCheckTableWin.center();
					}else{
						Ext.Msg.show({
							title : '错误',
							msg : '加载日结信息失败.'
						});
					}
				},
				failure : function(res, opt){
					Ext.Msg.show({
						title : '错误',
						msg : '加载日结信息失败.'
					});
				}
			});
	}
});

var billsBut = new Ext.ux.ImageButton({
	imgPath : "../../images/bill.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "账单",
	handler : function(btn) {
		location.href = "Bills.html";
/*		var front_billsWin = Ext.getCmp('front_billsWin');
		if(!front_billsWin){
			front_billsWin = new Ext.Window({
				id : 'front_billsWin',
				title : '当日账单管理',
				closable : false,
				modal : true,
				resizable : false,
				width : 1080,
				//height : 350,
				listeners : {
					hide : function(thiz){
						thiz.body.update('');
					},
					show : function(thiz){
						thiz.center();
						thiz.load({
							url : 'Bills.html',
							scripts : true
						});
					}
				},
				bbar : ['->',{
					text : '关闭',
					iconCls : 'btn_close',
					handler : function(e){
						front_billsWin.hide();
					}
				}]
			});
		}
		front_billsWin.show();*/
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
					    ['名称','name',130],
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
				selTabContentGrid.getStore().on('load', function(store, records, options, res){
					var sumRow;
					for(var i = 0; i < records.length; i++){
						if(eval(records[i].get('count') < 0)){
							sumRow = selTabContentGrid.getView().getRow(i);
							sumRow.style.backgroundColor = '#FF0000';
						}
					}
					sumRow = null;
					// 汇总
					var jr = Ext.decode(res.responseText);
					if(jr.root.length > 0){
						store.add(new OrderFoodRecord({
							orderDateFormat : '汇总',
							unitPrice : jr.other.sum.totalPrice,
							count : jr.other.sum.totalCount
						}));
					}
					var gv = selTabContentGrid.getView();
					var sumRow = gv.getRow(store.getCount()-1);
					sumRow.style.backgroundColor = '#DDD';			
					sumRow.style.color = 'green';
					gv.getCell(store.getCount()-1, 1).style.fontSize = '15px';
					gv.getCell(store.getCount()-1, 1).style.fontWeight = 'bold';
					gv.getCell(store.getCount()-1, 3).style.fontSize = '15px';
					gv.getCell(store.getCount()-1, 3).style.fontWeight = 'bold';
					gv.getCell(store.getCount()-1, 4).style.fontSize = '15px';
					gv.getCell(store.getCount()-1, 4).style.fontWeight = 'bold';
					gv.getCell(store.getCount()-1, 2).innerHTML = '';
					gv.getCell(store.getCount()-1, 5).innerHTML = '';
					gv.getCell(store.getCount()-1, 6).innerHTML = '';
					gv.getCell(store.getCount()-1, 7).innerHTML = '';
					gv.getCell(store.getCount()-1, 8).innerHTML = '';
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
		params : {
			isCookie : true
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (eval(resultJSON.success == true)) {
				omsg = resultJSON.msg;
				Ext.getCmp('btnRiJieDaYin').handler(null);
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
var dailySettleCheckDetpGrid = new Ext.grid.GridPanel({
	layout : "fit",
	ds : new Ext.data.JsonStore({
		fields : ['deptName', 'deptDiscount', 'deptGift', 'deptAmount']
	}),
	cm : new Ext.grid.ColumnModel([
	    new Ext.grid.RowNumberer(), 
	    {
	    	header : "部门",
	        dataIndex : "deptName",
	        width : 100
	    }, {
	    	header : "折扣",
	    	dataIndex : "deptDiscount",
	    	width : 100,
	    	align : 'right',
	    	renderer : function(val){
	    		return val.toFixed(2);
	        }
	    }, {
	    	header : "赠送",
	    	dataIndex : "deptGift",
	        width : 100,
	        align : 'right',
	        renderer : function(val){
	            return val.toFixed(2);
	        }
	    }, {
	    	header : "金额",
	    	dataIndex : "deptAmount",
	    	width : 100,
	    	align : 'right',
	    	renderer : function(val){
	    		return val.toFixed(2);
	        }
	    } 
	]),
	viewConfig : {
		forceFit : true
	}
});
var dailySettleCheckDetpPanel = new Ext.Panel({
	region : "center",
	layout : "fit",
	frame : true,
	items : dailySettleCheckDetpGrid
});

var dailySettleCheckTablePanel = new Ext.Panel({
	frame : true,
	region : "north",
	height : 440,
	items : [ {
		border : false,
		contentEl : "shiftCheckTableDiv"
	} ]
});


var dailySettleCheckTableWin = new Ext.Window({
	layout : "border",
	width : 450,
	height : 600,
	closeAction : "hide",
	resizable : false,
	closable : false,
	modal : true,
	items : [ dailySettleCheckTablePanel, dailySettleCheckDetpPanel ],
	buttonAlign : 'center',
	buttons : [{
		text : "交班",
		id : 'btnJiaoBan',
		handler : function() {
			Ext.MessageBox.show({
				msg : "确认进行交班？",
				width : 300,
				buttons : Ext.MessageBox.YESNO,
				fn : function(btn){
					if(btn == "yes"){
						Ext.Ajax.request({
							url : "../../DoShift.do",
							params : {
								isCookie : true,
								onDuty : shiftCheckDate.onDuty,
								offDuty : shiftCheckDate.offDuty
							},
							success : function(response, options) {
								var resultJSON = Ext.util.JSON.decode(response.responseText);
								if (resultJSON.success == true) {
									omsg = resultJSON.data;
									Ext.getCmp('btnJiaoBanDaYin').handler(null);
								} else {
									Ext.MessageBox.show({
										msg : resultJSON.data,
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
		text : '打印',
		id : 'btnJiaoBanDaYin',
		handler : function(e){
			var tempMask = new Ext.LoadMask(document.body, {
				msg : '正在打印请稍候.......',
				remove : true
			});
			tempMask.show();
			Ext.Ajax.request({
				url : "../../PrintOrder.do",
				params : {
					onDuty : shiftCheckDate.onDuty,
					offDuty : shiftCheckDate.offDuty,
					isCookie : true,
					'printType' : e == null ? 4 : 5
				},
				success : function(response, options) {
					tempMask.hide();
					var resultJSON = Ext.util.JSON.decode(response.responseText);
					Ext.example.msg('提示', (resultJSON.msg + (omsg.length > 0 ? ('<br/>'+omsg) : '')));
					if(omsg.length > 0)
						dailySettleCheckTableWin.hide();
					omsg = '';
				},
				failure : function(response, options) {
					tempMask.hide();
					Ext.ux.showMsg(Ext.decode(response.responseText));
				}
			});
		}
	}, {
		text : "日结",
		id : 'btnRiJie',
		handler : function() {
			Ext.MessageBox.show({
				msg : "确认进行日结？",
				width : 300,
				buttons : Ext.MessageBox.YESNO,
				fn : function(btn) {
					if (btn == "yes") {
						dailySettleCheckTableWin.hide();
						
						// 未交班帳單檢查
						Ext.Ajax.request({
							url : "../../DailySettleCheck.do",
							params : {
								isCookie : true
							},
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
		text : '打印',
		id : 'btnRiJieDaYin',
		handler : function(e){
			var tempMask = new Ext.LoadMask(document.body, {
				msg : '正在打印请稍候.......',
				remove : true
			});
			tempMask.show();
			Ext.Ajax.request({
				url : "../../PrintOrder.do",
				params : {
					isCookie : true,
					onDuty : shiftCheckDate.onDuty,
					offDuty : shiftCheckDate.offDuty,
					'printType' : e == null ? 6 : 5
				},
				success : function(response, options) {
					tempMask.hide();
					var jr = Ext.util.JSON.decode(response.responseText);
					Ext.example.msg('提示', (jr.msg + (omsg.length > 0 ? ('<br/>'+omsg) : '')));
					if(omsg.length > 0)
						dailySettleCheckTableWin.hide();
					omsg = '';
				},
				failure : function(response, options) {
					tempMask.hide();
					Ext.ux.showMsg(Ext.decode(response.responseText));
				}
			});
		}
	}, {
		text : "关闭",
		handler : function(){
			dailySettleCheckTableWin.hide();
		}
	}],
	listeners : {
		show : function(thiz){
			var btnJiaoBan = Ext.getCmp('btnJiaoBan');
			var btnJiaoBanDaYin = Ext.getCmp('btnJiaoBanDaYin');
			var btnRiJie = Ext.getCmp('btnRiJie');
			var btnRiJieDaYin = Ext.getCmp('btnRiJieDaYin');
			if(shiftCheckDate.otype == 0){
				Ext.getDom('shiftTitle').innerHTML = '交班表';
				btnJiaoBan.setVisible(true);
				btnJiaoBanDaYin.setVisible(true);
				btnRiJie.setVisible(false);
				btnRiJieDaYin.setVisible(false);
			}else if(shiftCheckDate.otype == 1){
				Ext.getDom('shiftTitle').innerHTML = '日结表';
				btnJiaoBan.setVisible(false);
				btnJiaoBanDaYin.setVisible(false);
				btnRiJie.setVisible(true);
				btnRiJieDaYin.setVisible(true);
			}else{
				thiz.hide();
				return;
			}
			
			Ext.getDom('shiftOperatorCheck').innerHTML = Ext.getDom('optName').innerHTML;
			Ext.getDom('shiftBillCountCheck').innerHTML = shiftCheckDate.allBillCount;
			Ext.getDom('shiftStartTimeCheck').innerHTML = shiftCheckDate.onDuty;
			Ext.getDom('shiftEndTimeCheck').innerHTML = shiftCheckDate.offDuty;
			
			Ext.getDom('billCount1Check').innerHTML = shiftCheckDate.cashBillCount;
			Ext.getDom('amount1Check').innerHTML = shiftCheckDate.cashAmount.toFixed(2);
			Ext.getDom('actual1Check').innerHTML = shiftCheckDate.cashActual.toFixed(2);
			
			Ext.getDom('billCount2Check').innerHTML = shiftCheckDate.creditBillCount;
			Ext.getDom('amount2Check').innerHTML = shiftCheckDate.creditAmount.toFixed(2);
			Ext.getDom('actual2Check').innerHTML = shiftCheckDate.creditActual.toFixed(2);
			
			Ext.getDom('billCount3Check').innerHTML = shiftCheckDate.memberBillCount;
			Ext.getDom('amount3Check').innerHTML = shiftCheckDate.memberAmount.toFixed(2);
			Ext.getDom('actual3Check').innerHTML = shiftCheckDate.memberActual.toFixed(2);
			
			Ext.getDom('billCount4Check').innerHTML = shiftCheckDate.signBillCount;
			Ext.getDom('amount4Check').innerHTML = shiftCheckDate.signAmount.toFixed(2);
			Ext.getDom('actual4Check').innerHTML = shiftCheckDate.signActual.toFixed(2);
			
			Ext.getDom('billCount5Check').innerHTML = shiftCheckDate.hangBillCount;
			Ext.getDom('amount5Check').innerHTML = shiftCheckDate.hangAmount.toFixed(2);
			Ext.getDom('actual5Check').innerHTML = shiftCheckDate.hangActual.toFixed(2);
			
			Ext.getDom('billCountSumCheck').innerHTML = shiftCheckDate.allBillCount;
			Ext.getDom('amountSumCheck').innerHTML = (shiftCheckDate.cashAmount 
														+ shiftCheckDate.creditAmount
														+ shiftCheckDate.memberAmount
														+ shiftCheckDate.signAmount+
														+ shiftCheckDate.hangAmount).toFixed(2);
			Ext.getDom('actualSumCheck').innerHTML = (shiftCheckDate.cashActual
														+ shiftCheckDate.creditActual
														+ shiftCheckDate.memberActual
														+ shiftCheckDate.signActual
														+ shiftCheckDate.hangActual).toFixed(2);
			
			Ext.getDom('eraseAmountCheck').innerHTML = shiftCheckDate.eraseAmount.toFixed(2);
			Ext.getDom('eraseIncomeCheck').innerHTML = shiftCheckDate.eraseBillCount;
			
			Ext.getDom('discountAmountCheck').innerHTML = shiftCheckDate.discountAmount.toFixed(2);
			Ext.getDom('discountBillCountCheck').innerHTML = shiftCheckDate.discountBillCount;
			
			Ext.getDom('giftAmountCheck').innerHTML = shiftCheckDate.discountAmount.toFixed(2);
			Ext.getDom('giftBillCountCheck').innerHTML = shiftCheckDate.discountBillCount;
			
			Ext.getDom('giftAmountCheck').innerHTML = shiftCheckDate.giftAmount.toFixed(2);
			Ext.getDom('giftBillCountCheck').innerHTML = shiftCheckDate.giftBillCount;
			
			Ext.getDom('returnAmountCheck').innerHTML = shiftCheckDate.returnAmount.toFixed(2);
			Ext.getDom('returnBillCountCheck').innerHTML = shiftCheckDate.returnBillCount;
			
			Ext.getDom('repayAmountCheck').innerHTML = shiftCheckDate.repayAmount.toFixed(2);
			Ext.getDom('repayBillCountCheck').innerHTML = shiftCheckDate.repayBillCount;
			
			Ext.getDom('serviceAmountCheck').innerHTML = shiftCheckDate.serviceAmount.toFixed(2);
			
			dailySettleCheckDetpGrid.getStore().loadData(shiftCheckDate.deptInfos);
		}
	}
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
					dataSource : 'tree',
					isCookie : true
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
	//getOperatorName("../../");
	
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

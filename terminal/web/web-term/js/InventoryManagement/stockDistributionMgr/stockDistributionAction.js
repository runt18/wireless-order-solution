Ext.onReady(function() {
	
	var billsGrid = null;
	var actionType = {
		INSERT :　{dataSource : 'insert', desc : '添加'},
		UPDATE : {dataSource : 'update', desc : '修改'},
		DELETE : {dataSource : 'delete', desc : '删除'},
		GETBYCOND : {dataSource : 'getByCond', desc : '查询'},
		AUDIT : {dataSource : 'audit', desc : '审核'},
		REAUDIT : {dataSource : 'reAudit', desc : '反审核'}
	}
	
	var stockType = {
		STOCKIN : {val : 1, desc : '入库'},
		STOCKOUT : {val : 2, desc : '出库'},
		APPLY : {val : 3, desc : '申请'}
	}
	
	var restaurantType = {
		RESTAURANT : {val : 1, desc : "餐厅"},
		GROUP : {val : 2, desc : "集团"},
		BRANCE : {val : 3, desc : "门店"}
	}
	
	var stockOut = [[-1, '全部'], [11, '配送发货'], [13, '配送退货']];
	var stockIn = [[-1, '全部'], [12, '配送收货'], [14, '配送回收']];
	var associateStatus = [[-1, '全部'], [1, '未绑定'], [2, '已绑定']];
	var stockActionStatus = [[-1, '全部'], [1, '未审核'], [2, '审核通过'], [3, '反审核']];

	//库单录入功能
	function operateStockDistribution(param, type, callback){
		
		if(type == actionType.INSERT.dataSource && !param.detail){
			Ext.example.msg('错误提示', '库单录入货品不能为空');
			return;
		}
		
		Ext.Ajax.request({
			url : '../../OperateStockDistribution.do',
			params : {
				dataSource : type,
				subType : param.subType,
				stockInRestaurant : param.stockInRestaurant,
				stockOutRestaurant : param.stockOutRestaurant,
				comment : param.comment,
				oriId : param.oriId,
				oriDate : param.oriDate,
				associateId : param.associateId,
				detail : param.detail,
				cateType : param.cateType,
				stockActionId : param.stockActionId,
				id : param.id,
				actualPrice : param.actualPrice,
				stockType : param.stockType
			},
			success : function(res, opt){
				var jr = Ext.decode(res.responseText);
				if(jr.success){
					Ext.example.msg('成功提示', jr.msg);
					if(callback){
						callback(jr);
					}
					Ext.getCmp('searchBtn_distribution').handler();
				}else if(jr.code == 5204){ //没有对应货品
					
					var checkWin;
					checkWin = new Ext.Window({
						height : 150,
						width : 250,
						resizable : false,
						modal : true,
						closable : false,
						title : '是否进行配送同步',
						layout : 'form',
						items : [{
							xtype : 'tbtext',
							text : '<span style="color:red;width:80%;margin:10px auto;text-align:center;display:block;font-weight:bold;font-size:14px;">配送同步会复制总店的货品到分店</span>'
						}, {
							xtype : 'textfield',
							fieldLabel : '请填入"ok"确认',
							labelStyle : 'color : red',
							id : 'checkInit_textfield_stockDistribution'
						}],
						bbar : ['->', {
							xtype : 'button',
							text : '确认',
							iconCls : 'btn_save',
							handler : function(){
								if(Ext.getCmp('checkInit_textfield_stockDistribution').getValue() != 'ok'){
									Ext.example.msg('错误提示', '请输入"ok"字样确认初始化');
									return;
								}
								
								$.ajax({
									url : '../../OperateStockDistribution.do',
									type : 'post',
									dataType : 'json',
									data : {
										dataSource : 'sync'				
									},
									success : function(data, status, req){
										Ext.example.msg((data.success ? '成功提示' : '错误提示'), data.msg);
										if(data.success){
											Ext.getCmp('btnClose_stockDistribution').handler();
										}
									},
									error : function(req, status, err){
										Ext.example.msg('错误提示', '同步失败');
									}
								});
								
							}
						}, {
							xtype : 'button',
							text : '取消',
							id : 'btnClose_stockDistribution',
							iconCls : 'btn_close',
							handler : function(){
								checkWin.hide();
								$('#' + checkWin.id).remove();
							}
						}]
					});
					checkWin.show();
					
				}else{
//					Ext.example.msg('错误提示', jr.msg);
					
					Ext.ux.showMsg({
						msg : jr.msg,
						title : '错误提示',
						code : jr.code
					});
				}
				
			},
			failure : function(res, opt){
//				Ext.example.msg('错误提示', Ext.decode(res.responseText));
				var jr = Ext.decode(res.responseText);
				Ext.ux.showMsg({
					msg : jr.msg,
					title : '错误提示',
					code : jr.code
				});
			}
		});
	}
	
	//配送申请
	var distributionApplyAction = new Ext.ux.ImageButton({
		imgPath : "../../images/discountStatis.png",
		imgWidth : 50,
		imgHeight : 50,
		tooltip : "配送申请",
		handler : function(btn) {
			var stockDistributionApply;
			stockDistributionApply = Ext.stockDistributionAction.newInstance({
				stockType : Ext.stockDistributionAction.stockType.STOCK_APPLY.val,
				subType : Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_APPLY.val,
				callback : function(param){
					if(!param.stockOutRestaurant || !param.stockInRestaurant){
						Ext.example.msg('错误提示', '单据没有出货门店或没有收货门店');		
						return;
					}
					operateStockDistribution(param, actionType.INSERT.dataSource, function(){
						stockDistributionApply.close();
						Ext.getCmp('stockType_distribution').setValue(stockType.APPLY.val);
						Ext.getCmp('stockType_distribution').fireEvent('select');
						Ext.getCmp('beginDate_combo_distribution').setValue(param.oriDate);
						Ext.getCmp('beginEnd_combo_distribution').setValue(param.oriDate);
						Ext.getCmp('subType_distritbuion').setValue('');
						Ext.getCmp('stockInRestaurant_distribution').setValue(param.stockInRestaurant);
						Ext.getCmp('stockOutRestaurant_distribution').setValue(param.stockOutRestaurant);
						Ext.getCmp('subType_distritbuion').fireEvent('select');
					});
				}
			}).open();
		}
	});
	
	
	//配送发货
	var distributionSendAction = new Ext.ux.ImageButton({
		imgPath : "../../images/discountStatis.png",
		imgWidth : 50,
		imgHeight : 50,
		tooltip : "配送发货",
		handler : function(btn) {
			//@Ext.stockDistributionAction type distribution_send '配送发货'
			var stockDistributionSend;
			stockDistributionSend = Ext.stockDistributionAction.newInstance({
				subType : Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_SEND.val,
				isAssociate : true,
				callback : function(param){
					if(!param.stockOutRestaurant || !param.stockInRestaurant){
						Ext.example.msg('错误提示', '单据没有出货门店或没有收货门店');		
						return;
					}
					operateStockDistribution(param, actionType.INSERT.dataSource, function(){
						stockDistributionSend.close();
						Ext.getCmp('stockType_distribution').setValue(stockType.STOCKOUT.val);
						Ext.getCmp('stockType_distribution').fireEvent('select');
						Ext.getCmp('beginDate_combo_distribution').setValue(param.oriDate);
						Ext.getCmp('beginEnd_combo_distribution').setValue(param.oriDate);
						Ext.getCmp('subType_distritbuion').setValue(Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_SEND.val);
						Ext.getCmp('stockInRestaurant_distribution').setValue(param.stockInRestaurant);
						Ext.getCmp('stockOutRestaurant_distribution').setValue(param.stockOutRestaurant);
						Ext.getCmp('subType_distritbuion').fireEvent('select');
					});
				}
			}).open();
		}
	});
	
	//配送收货
	var distributionReceiveAction = new Ext.ux.ImageButton({
		imgPath : "../../images/paymentRecord.png",
		imgWidth : 50,
		imgHeight : 50,
		tooltip : "配送收货",
		handler : function(btn) {
			var stockDistributionReceive;
			stockDistributionReceive = Ext.stockDistributionAction.newInstance({
				subType : Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECEIVE.val,
				isAssociate : true,
				callback : function(param){
					if(!param.stockOutRestaurant || !param.stockInRestaurant){
						Ext.example.msg('错误提示', '单据没有出货门店或没有收货门店');		
						return;
					}
					operateStockDistribution(param, actionType.INSERT.dataSource, function(){
						stockDistributionReceive.close();
						Ext.getCmp('stockType_distribution').setValue(stockType.STOCKIN.val);
						Ext.getCmp('stockType_distribution').fireEvent('select');
						Ext.getCmp('beginDate_combo_distribution').setValue(param.oriDate);
						Ext.getCmp('beginEnd_combo_distribution').setValue(param.oriDate);
						Ext.getCmp('subType_distritbuion').setValue(Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECEIVE.val);
						Ext.getCmp('stockInRestaurant_distribution').setValue(param.stockInRestaurant);
						Ext.getCmp('stockOutRestaurant_distribution').setValue(param.stockOutRestaurant);
						Ext.getCmp('subType_distritbuion').fireEvent('select');
					});
				}
			}).open();
		}
	});
	
	var distributionReturnAction = new Ext.ux.ImageButton({
		imgPath : "../../images/shiftStatis.png",
		imgWidth : 50,
		imgHeight : 50,
		tooltip : "配送退货",
		handler : function(btn) {
			var stockDistributionReturn;
			stockDistributionReturn = Ext.stockDistributionAction.newInstance({
				subType : Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RETURN.val,
				isAssociate : true,
				callback : function(param){
					if(!param.stockOutRestaurant || !param.stockInRestaurant){
						Ext.example.msg('错误提示', '单据没有出货门店或没有收货门店');		
						return;
					}
					operateStockDistribution(param, actionType.INSERT.dataSource, function(){
						stockDistributionReturn.close();
						Ext.getCmp('stockType_distribution').setValue(stockType.STOCKOUT.val);
						Ext.getCmp('stockType_distribution').fireEvent('select');
						Ext.getCmp('beginDate_combo_distribution').setValue(param.oriDate);
						Ext.getCmp('beginEnd_combo_distribution').setValue(param.oriDate);
						Ext.getCmp('subType_distritbuion').setValue(Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RETURN.val);
						Ext.getCmp('stockInRestaurant_distribution').setValue(param.stockInRestaurant);
						Ext.getCmp('stockOutRestaurant_distribution').setValue(param.stockOutRestaurant);
						Ext.getCmp('subType_distritbuion').fireEvent('select');
					});
				}
			}).open();
		}
	});
	
	
	
	var distributionRecoveryAction = new Ext.ux.ImageButton({
		imgPath : "../../images/dailySettleStatis.png",
		imgWidth : 50,
		imgHeight : 50,
		tooltip : "配送回收",
		handler : function(btn) {
			var stockDistributionRecovery;
			stockDistributionRecovery = Ext.stockDistributionAction.newInstance({
				subType : Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECOVERY.val,
				isAssociate : true,
				callback : function(param){
					if(!param.stockOutRestaurant || !param.stockInRestaurant){
						Ext.example.msg('错误提示', '单据没有出货门店或没有收货门店');		
						return;
					}
					operateStockDistribution(param, actionType.INSERT.dataSource, function(){
						stockDistributionRecovery.close();
						Ext.getCmp('stockType_distribution').setValue(stockType.STOCKIN.val);
						Ext.getCmp('stockType_distribution').fireEvent('select');
						Ext.getCmp('beginDate_combo_distribution').setValue(param.oriDate);
						Ext.getCmp('beginEnd_combo_distribution').setValue(param.oriDate);
						Ext.getCmp('subType_distritbuion').setValue(Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECOVERY.val);
						Ext.getCmp('stockInRestaurant_distribution').setValue(param.stockInRestaurant);
						Ext.getCmp('stockOutRestaurant_distribution').setValue(param.stockOutRestaurant);
						Ext.getCmp('subType_distritbuion').fireEvent('select');
					});
				}
			}).open();
		}
	});
	
	var dateBegin = new Ext.form.DateField({
		id : 'beginDate_combo_distribution',
		xtype : 'datefield',
		format : 'Y-m-d',
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	var dateEnd = new Ext.form.DateField({
		id : 'beginEnd_combo_distribution',
		xtype : 'datefield',
		format : 'Y-m-d',
		maxValue : new Date(),
		readyOnly : false,
		allowBlank : false
	});
	
	var dateCombo;
	dateCombo = Ext.ux.createDateCombo({
		beginDate : dateBegin,
		endDate : dateEnd,
		callback : function(){
			Ext.getCmp('searchBtn_distribution').handler();
		}
	});
	
	var distributionToolbar;
	distributionToolbar = new Ext.Toolbar({
		height : 28,
		items : [{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;日期：'
		},
		dateCombo,'　',
		dateBegin,{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;至&nbsp;&nbsp;'
		},
		dateEnd, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;货单类型:'
		}, {
			xtype : 'combo',
			id : 'stockType_distribution',
			readOnly : false,
			forceSelection : true,
			width : 60,
			value : 1,
			store : new Ext.data.SimpleStore({
				data : [[stockType.STOCKIN.val, stockType.STOCKIN.desc], [stockType.STOCKOUT.val, stockType.STOCKOUT.desc],[stockType.APPLY.val, stockType.APPLY.desc]],
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			listeners : {
				select : function(thiz){
					var subType = Ext.getCmp('subType_distritbuion');
					if(Ext.getCmp('stockType_distribution').getValue() == 1){
						subType.store.loadData(stockIn);
						subType.setValue(-1);
					}else if(Ext.getCmp('stockType_distribution').getValue() == 2){
						subType.store.loadData(stockOut);
						subType.setValue(-1);
					}else{
						subType.store.loadData([]);
						subType.setValue();						
					}
//					Ext.getCmp('searchBtn_distribution').handler();
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;业务类型:'
		}, {
			xtype : 'combo',
			id : 'subType_distritbuion',
			readOnly : false,
			forceSelection : true,
			width : 90,
			value : -1,
			store : new Ext.data.SimpleStore({
				data : stockIn,
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(){
					Ext.getCmp('searchBtn_distribution').handler();
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;审核状态:'
		}, {
			xtype : 'combo',
			id : 'stockActionStatus_distirbution',
			readOnly : false,
			forceSelection : true,
			width : 90,
			value : -1,
			store : new Ext.data.SimpleStore({
				data : stockActionStatus,
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(){
					Ext.getCmp('searchBtn_distribution').handler();
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;绑定状态:'
		}, {
			xtype : 'combo',
			id : 'associateStatus_distribution',
			readOnly : false,
			forceSelection : true,
			width : 90,
			value : -1,
			store : new Ext.data.SimpleStore({
				data : associateStatus,
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(){
					Ext.getCmp('searchBtn_distribution').handler();
				}
			}
		}, '->', {
			xtype : 'button',
			text : '搜索',
			iconCls : 'btn_search',
			id : 'searchBtn_distribution',
			handler : function(){
				var distributionStore = distributionGridPanel.store;
				distributionStore.baseParams['stockType'] = Ext.getCmp('stockType_distribution').getValue();
				distributionStore.baseParams['subType'] = Ext.getCmp('subType_distritbuion').getValue();
				distributionStore.baseParams['actionStatus'] = Ext.getCmp('stockActionStatus_distirbution').getValue();
				distributionStore.baseParams['distributionStatus'] = Ext.getCmp('associateStatus_distribution').getValue();
				distributionStore.baseParams['stockOutRestaurant'] = Ext.getCmp('stockOutRestaurant_distribution').getValue();
				distributionStore.baseParams['stockInRestaurant'] = Ext.getCmp('stockInRestaurant_distribution').getValue();
				distributionStore.baseParams['fuzzId'] = Ext.getCmp('fuzzId_distribution').getValue();
				distributionStore.baseParams['comment'] = Ext.getCmp('comment_distribution').getValue();
				distributionStore.baseParams['beginDate'] = Ext.getCmp('beginDate_combo_distribution').getValue();
				distributionStore.baseParams['endDate'] = Ext.getCmp('beginEnd_combo_distribution').getValue();
				distributionStore.load();
			}
		}]
	});
	
	var distributionToolbar2;
	distributionToolbar2 = new Ext.Toolbar({
		height : 28,
		items : [{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;出库门店：'
		}, {
			xtype : 'combo',
			id : 'stockOutRestaurant_distribution',
			readOnly : false,
			forceSelection : true,
			width : 90,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				render : function(thiz){
					var data = [];
					$.ajax({
						url : '../../OperateRestaurant.do',
						type : 'post',
						dataType : 'json',
						data : {
							dataSource : 'getByCond',
							byId : true
						},
						success : function(res, status, req){
							if(res.success){
								if(res.root[0].typeVal == restaurantType.GROUP.val){
									data.push([-1, '全部']);
									data.push([res.root[0].id, res.root[0].name]);
									res.root[0].branches.forEach(function(el, index){
										data.push([el.id, el.name]);
									});
									thiz.store.loadData(data);
									thiz.setValue(-1);
								}else if(res.root[0].typeVal == restaurantType.BRANCE.val){
									data.push([-1, '全部']);
									data.push([res.root[0].id, res.root[0].name]);
									$.ajax({
										url : '../../OperateRestaurant.do',
										type : 'post',
										dataType : 'json',
										data : {
											dataSource : 'getGroupRestaurant'
										},
										success : function(response){
											if(response.success){
												data.push([response.root[0].id, response.root[0].name]);
												thiz.store.loadData(data);
											}
										},
										error : function(request){
										
										}
									});
									thiz.store.loadData(data);
									thiz.setValue(-1);
								}else{
									data.push([res.root[0].id, res.root[0].name]);
									thiz.store.loadData(data);
									thiz.setValue(res.root[0].id);
								}
							}
						},
						error : function(req, staus, err){
						
						}
					});
				},
				select : function(){
					Ext.getCmp('searchBtn_distribution').handler();
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;入库门店：'
		}, {
			xtype : 'combo',
			id : 'stockInRestaurant_distribution',
			readOnly : false,
			forceSelection : true,
			width : 90,
			store : new Ext.data.SimpleStore({
				data : associateStatus,
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				render : function(thiz){
					var data = [];
					$.ajax({
						url : '../../OperateRestaurant.do',
						type : 'post',
						dataType : 'json',
						data : {
							dataSource : 'getByCond',
							byId : true
						},
						success : function(res, status, req){
							if(res.success){
								if(res.root[0].typeVal == restaurantType.GROUP.val){
									data.push([-1, '全部']);
									data.push([res.root[0].id, res.root[0].name]);
									res.root[0].branches.forEach(function(el, index){
										data.push([el.id, el.name]);
									});
									thiz.store.loadData(data);
									thiz.setValue(-1);
								}else if(res.root[0].typeVal == restaurantType.BRANCE.val){
									data.push([-1, '全部']);
									data.push([res.root[0].id, res.root[0].name]);
									$.ajax({
										url : '../../OperateRestaurant.do',
										type : 'post',
										dataType : 'json',
										data : {
											dataSource : 'getGroupRestaurant'
										},
										success : function(response){
											if(response.success){
												data.push([response.root[0].id, response.root[0].name]);
												thiz.store.loadData(data);
											}
										},
										error : function(request){
										
										}
									});
									thiz.store.loadData(data);
									thiz.setValue(-1);
								}else{
									data.push([res.root[0].id, res.root[0].name]);
									thiz.store.loadData(data);
									thiz.setValue(res.root[0].id);
								}
							}
						},
						error : function(req, staus, err){
						
						}
					});
				},
				select : function(){
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;货单编号/原始单号：'
		}, {
			xtype : 'textfield',
			id : 'fuzzId_distribution',
			width : 100
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;备注：'
		}, {
			xtype : 'textfield',
			id : 'comment_distribution',
			width : 200	
		}]
	});
	
	function operations(param){
		if(param.statusValue == 1){
				return ''
				+ '<a href="javascript:void(0);" data-type="exportExcelStockAction">导出</a>'
				+ '&nbsp;&nbsp;&nbsp;&nbsp;'
				+ '<a href="javascript:void(0);" data-type="updateStockAtion">修改</a>'
				+ '&nbsp;&nbsp;&nbsp;&nbsp;'
				+ '<a href="javascript:void(0);" data-type="auditStockActionHandler">审核</a>'
				+ '&nbsp;&nbsp;&nbsp;&nbsp;'
				+ '<a href="javascript:void(0)" data-type="deleteStockActionHandler">删除</a>';
		}else if(param.statusValue == 4){
			return ''
				+ '<a href="javascript:void(0);" data-type="exportExcelStockAction">导出</a>'
				+ '&nbsp;&nbsp;&nbsp;&nbsp;'
				+ '<a href="javascript:void(0);" data-type="showStockAction">查看</a>';
		}else{
			return ''
			+ '<a href="javascript:void(0);" data-type="isableToReAudit">反审核</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href="javascript:void(0);" data-type="exportExcelStockAction">导出</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href="javascript:void(0);" data-type="showStockAction">查看</a>';
		}
	}
	
	function auditStockAction(){
		var selected = distributionGridPanel.getSelectionModel().getSelected();
		Ext.Msg.show({
			title : '重要',
			msg : '是否审核库存单',
			buttons : Ext.Msg.YESNO,
			icon: Ext.Msg.QUESTION,
			fn : function(e){
				if(e == 'yes'){
					$.ajax({
						url : '../../OperateStockDistribution.do',
						type : 'post',
						dataType : 'json',
						data : {
							id : selected.id,
							dataSource : 'audit'				
						},
						success : function(data, status, req){
							if(data.success){
								Ext.example.msg('成功提示', data.msg);
								Ext.getCmp('searchBtn_distribution').handler();
							}else{
								Ext.example.msg('失败提示', '审核失败');
							}
						},
						error : function(req, status, err){
							Ext.example.msg('失败提示', '审核失败');
						}
					});
				}
			}
		});
	}
	
	function exportExcel(){
		var sn = distributionGridPanel.getSelectionModel().getSelected();
		var url = '../../{0}?pin={1}&id={2}&dataSource={3}';
		url = String.format(
			url,
			'ExportHistoryStatisticsToExecl.do',
			-10,
			sn.json.stockAction.id,
			'stockAction'
		);
		window.location = url;
	}
	
	function showStockAction(){
		var selected = distributionGridPanel.getSelectionModel().getSelected().json;
		$.ajax({
			url : '../../QueryStockAction.do',
			type : 'post',
			dataType : 'json',
			data : {
				id : selected.stockAction.id,
				containsDetails : true,
				isWithOutSum : true,
				isDistribution :true
			},
			success : function(res, status, req){
				if(res.success){
					var stockDistributionShower;
					stockDistributionShower = Ext.stockDistributionAction.newInstance({
						subType : selected.stockAction.subTypeValue,
						cateType : selected.stockAction.cateTypeValue,
						oriId : selected.stockAction.oriStockId,
						oriDate : selected.stockAction.oriStockDateFormat,
						comment : selected.stockAction.comment,
						appover : selected.stockAction.approverName,
						appoverDate : selected.stockAction.approverDateFormat,
						operator : selected.stockAction.operatorName,
						operateDate : selected.stockAction.birthDateFormat,
						associateId : selected.associateId,
						actionType : selected.stockAction.typeValue,
						stockActionId : selected.stockAction.id,
						stockInRestaurant : selected.stockInRestaurant.id,
						stockOutRestaurant : selected.stockOutRestaurant.id,
						details : res.root[0].stockDetails,
						actualPrice : res.root[0].actualPrice,
						isOnlyShow : true,
						callback : function(param){
							Ext.example.msg('温磬提示', '查看模式下不能修改库单');		
						}
					}).open();
				}
			},
			error : function(req, status, err){
			
			}
		});
	}
	
	function updateStockAtion(){
		var selected = distributionGridPanel.getSelectionModel().getSelected().json;
		$.ajax({
			url : '../../QueryStockAction.do',
			type : 'post',
			dataType : 'json',
			data : {
				id : selected.stockAction.id,
				containsDetails : true,
				isWithOutSum : true,
				isDistribution :true
			},
			success : function(res, status, req){
				if(res.success){
					var stockDistributionShower;
					stockDistributionShower = Ext.stockDistributionAction.newInstance({
						subType : selected.stockAction.subTypeValue,
						cateType : selected.stockAction.cateTypeValue,
						oriId : selected.stockAction.oriStockId,
						oriDate : selected.stockAction.oriStockDateFormat,
						comment : selected.stockAction.comment,
						appover : selected.stockAction.approverName,
						appoverDate : selected.stockAction.approverDateFormat,
						operator : selected.stockAction.operatorName,
						operateDate : selected.stockAction.birthDateFormat,
						associateId : selected.associateId ? selected.associateId : '',
						actionType : selected.stockAction.typeValue,
						stockActionId : selected.stockAction.id,
						stockInRestaurant : selected.stockInRestaurant.id,
						stockOutRestaurant : selected.stockOutRestaurant.id,
						details : res.root[0].stockDetails,
						actualPrice : res.root[0].actualPrice,
						id : selected.id,
						callback : function(param){
							if(!param.stockOutRestaurant || !param.stockInRestaurant){
								Ext.example.msg('错误提示', '单据没有出货门店或没有收货门店');		
								return;
							}
							operateStockDistribution(param, actionType.UPDATE.dataSource, function(){
								stockDistributionShower.close();
							});
						}
					}).open();
				}
			},
			error : function(req, status, err){
			
			}
		});
	}
	
	function deleteStockActionHandler(){
		var selected = distributionGridPanel.getSelectionModel().getSelected().json;
		
		Ext.Msg.show({
			title : '重要',
			msg : '是否删除库存单',
			buttons : Ext.Msg.YESNO,
			icon: Ext.Msg.QUESTION,
			fn : function(e){
				if(e == 'yes'){
					$.ajax({
						url : '../../OperateStockDistribution.do',
						type : 'post',
						dataType : 'json',
						data : {
							id : selected.id,
							dataSource : 'deleteById'				
						},
						success : function(data, status, req){
							if(data.success){
								Ext.example.msg('成功提示', data.msg);
								Ext.getCmp('searchBtn_distribution').handler();
							}else{
								Ext.example.msg('失败提示', '删除失败');
							}
						},
						error : function(req, status, err){
							Ext.example.msg('失败提示', '删除失败');
						}
					});
				}
			}
		});
	}
	
	function isableToReAudit(){
		var selected = distributionGridPanel.getSelectionModel().getSelected().json;
		$.ajax({
			url : '../../QueryStockAction.do',
			type : 'post',
			dataType : 'json',
			data : {
				id : selected.stockAction.id,
				containsDetails : true,
				isWithOutSum : true,
				isDistribution :true
			},
			success : function(res, status, req){
				if(res.success){
					var stockDistributionShower;
					stockDistributionShower = Ext.stockDistributionAction.newInstance({
						subType : selected.stockAction.subTypeValue,
						cateType : selected.stockAction.cateTypeValue,
						oriId : selected.stockAction.oriStockId,
						oriDate : selected.stockAction.oriStockDateFormat,
						comment : selected.stockAction.comment,
						appover : selected.stockAction.approverName,
						appoverDate : selected.stockAction.approverDateFormat,
						operator : selected.stockAction.operatorName,
						operateDate : selected.stockAction.birthDateFormat,
						associateId : selected.associateId ? selected.associateId : '',
						actionType : selected.stockAction.typeValue,
						stockActionId : selected.stockAction.id,
						stockInRestaurant : selected.stockInRestaurant.id,
						stockOutRestaurant : selected.stockOutRestaurant.id,
						details : res.root[0].stockDetails,
						actualPrice : res.root[0].actualPrice,
						id : selected.id,
						callback : function(param){
							if(!param.stockOutRestaurant || !param.stockInRestaurant){
								Ext.example.msg('错误提示', '单据没有出货门店或没有收货门店');		
								return;
							}
							operateStockDistribution(param, actionType.REAUDIT.dataSource, function(){
								stockDistributionShower.close();
							});
						}
					}).open();
				}
			},
			error : function(req, status, err){
			
			}
		});
	}
	
	
	var colModel;
	colModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{
			header : '日期',
			dataIndex : 'stockAction.oriStockDateFormat'
		},
		{
			header : '配送单号',
			dataIndex : 'id'		
		}, {
			header : '关联单号',
			dataIndex : 'associateId',
			renderer : function(data){
				return data > 0 ? data : '----';
			}
		},
		{
			header : '库单类型',
			dataIndex : 'stockAction.subTypeText'
		},
		{
			header : '出货门店',
			dataIndex : 'stockOutRestaurant.name'
		},
		{
			header : '收货门店',
			dataIndex : 'stockInRestaurant.name'
		},
		{
			header : '库单状态',
			dataIndex : 'statusText'		
		},{
			header : '绑定状态',
			dataIndex : 'stockAction.statusText'
		},
		{
			header : '操作',
			width : 200,
			align : 'center',
			dataIndex : 'operation',
			renderer : function(data, colCls, json){
				return operations(json.json.stockAction);
			}
		}
	]);
	
	var store;
	store = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url : '../../OperateStockDistribution.do'}),
		reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root'}, [{
			name : 'stockAction.oriStockDateFormat'
		},{
			name : 'id'
		},{
			name : 'associateId'
		},{
			name : 'stockAction.subTypeText'
		},{
			name : 'stockOutRestaurant.name'
		},{
			name : 'stockInRestaurant.name'
		},{
			name : 'statusText'
		},{
			name : 'stockAction.statusText'
		}]),
		baseParams : {
			start : 0,
			limit : 20,
			isWithOutSum : false,
			dataSource : 'getByCond',
			isHistory : true
		}
	});
	
	var pagingbar
	pagingbar = new Ext.PagingToolbar({
		pageSize : 20,
		store : store,
		displayInfo : true,
		displayMsg : '显示第{0} 条到{1} 条记录，共{2}条',
		emptyMsg : '没有记录'
	}); 
	
	var distributionGridPanel;
	distributionGridPanel = new Ext.grid.GridPanel({
		id : 'container_gridPanel_distribution',
		frame : false,
		height : 400,
		style : {
			'width' : '100%'
		},
		viewConfig : {
			forceFit : true
		},
		cm : colModel,
		loadMask : {
			msg : '数据加载中,请稍后....'
		},
		tbar : [],
		store : store,
		bbar : pagingbar,
		keys : {
			key : 13, //enter键
			scope : this,
			fn : function(){
				Ext.getCmp('searchBtn_distribution').handler();
			}
		},
		listeners : {
			render : function(){
				distributionToolbar.render(distributionGridPanel.tbar);
				distributionToolbar2.render(distributionGridPanel.tbar);
				store.load();
			},
			dblclick : function(){
				showStockAction();
			}
		}
	});
	
	
	new Ext.Panel({
		renderTo : 'mainContainer_div_stockDistribution',
		width : parseInt(Ext.getDom('mainContainer_div_stockDistribution').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('mainContainer_div_stockDistribution').parentElement.style.height.replace(/px/g,'')),
		//region : 'center',
		layout : 'fit',
		items : [distributionGridPanel],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			{xtype:'tbtext',text:'&nbsp'},
			distributionApplyAction,
			{xtype:'tbtext',text:'&nbsp;'},
			distributionSendAction,
			{xtype:'tbtext',text:'&nbsp;'},
			distributionReceiveAction,
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			distributionReturnAction, 
			{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
			distributionRecoveryAction
			]
		})
	});
	
	//绑定操作
	store.on('load', function(){
		//审核
		$('[data-type=auditStockActionHandler]').click(function(){
			auditStockAction();
		});
		
		//导出
		$('[data-type=exportExcelStockAction]').click(function(){
			exportExcel();
		});
		
		//查看
		$('[data-type=showStockAction]').click(function(){
			showStockAction();
		});
		
		//删除
		$('[data-type=deleteStockActionHandler]').click(function(){
			deleteStockActionHandler();
		});
		
		//删除
		$('[data-type=updateStockAtion]').click(function(){
			updateStockAtion();
		});
		
		//反审核
		$('[data-type=isableToReAudit]').click(function(){
			isableToReAudit();
		});
	});
	
	dateCombo.setValue(1);
	dateCombo.fireEvent('select');
});


var kitchenMultSelectDS = new Ext.data.SimpleStore({
	fields : [ "retrunValue", "displayText" ],
	data : []
});

kitchenStatWin = new Ext.Window(
		{
			title : "分厨统计",
			width : 450,
			height : 380,
			closeAction : "hide",
			resizable : false,
			layout : "anchor",
			items : [ {
				border : false,
				anchor : "right 10%",
				items : [ {
					layout : "column",
					border : false,
					frame : true,
					anchor : "98%",
					items : [ {
						layout : "form",
						border : false,
						labelSeparator : ' ',
						width : 200,
						labelWidth : 30,
						items : [ {
							xtype : "timefield",
							format : "H:i:s",
							id : "begDateMStatKitchen",
							width : 150,
							fieldLabel : "时间"
						} ]
					}, {
						layout : "form",
						border : false,
						labelSeparator : ' ',
						width : 200,
						labelWidth : 30,
						items : [ {
							xtype : "timefield",
							format : "H:i:s",
							id : "endDateMStatKitchen",
							width : 150,
							fieldLabel : "至"
						} ]
					} ]
				} ]
			}, {
				layout : "form",
				border : false,
				frame : true,
				hideLabels : true,
				anchor : "right 91%",
				items : [ {
					xtype : "itemselector",
					name : "kitchenMultSelectMStat",
					id : "kitchenMultSelectMStat",
					fromStore : kitchenMultSelectDS,
					dataFields : [ "retrunValue", "displayText" ],
					toData : [ [ "", "" ] ],
					msHeight : 250,
					valueField : "retrunValue",
					displayField : "displayText",
					imagePath : "../../extjs/multiselect/images/",
					toLegend : "已选择分厨",
					fromLegend : "可选择分厨"
				} ]
			} ],
			buttons : [
					{
						text : "全选",
						handler : function() {
							var i = kitchenStatWin
									.findById("kitchenMultSelectMStat").fromMultiselect.view;
							i.selectRange(0, i.store.length);
							kitchenStatWin.findById("kitchenMultSelectMStat")
									.fromTo();
							kitchenStatWin.findById("kitchenMultSelectMStat").toMultiselect.view
									.clearSelections();
						}
					},
					{
						text : "清空",
						handler : function() {
							kitchenStatWin.findById("kitchenMultSelectMStat")
									.reset();
						}
					},
					{
						text : "确定",
						handler : function() {

							var selectCount = kitchenStatWin
									.findById("kitchenMultSelectMStat").toMultiselect.store
									.getCount();

							if (selectCount != 0) {
								isPrompt = false;
								kitchenStatWin.hide();

								var selectKitchens = "";
								for ( var i = 0; i < selectCount; i++) {
									var selectItem = kitchenStatWin
											.findById("kitchenMultSelectMStat").toMultiselect.store
											.getAt(i).get("retrunValue");
									// if (selectItem != "") {
									selectKitchens = selectKitchens
											+ selectItem + ",";
									// }
								}
								// 去掉最后一个逗号
								selectKitchens = selectKitchens.substring(0,
										selectKitchens.length - 1);

								// 保存条件
								var dateFormated = new Date();
								kitchenStaticBeginDate = kitchenStatWin
										.findById("begDateMStatKitchen")
										.getValue();
								if (kitchenStaticBeginDate != "") {
									kitchenStaticBeginDate = dateFormated
											.format('Y-m-d')
											+ " " + kitchenStaticBeginDate;
								}

								kitchenStaticEndDate = kitchenStatWin.findById(
										"endDateMStatKitchen").getValue();
								if (kitchenStaticEndDate != "") {
									kitchenStaticEndDate = dateFormated
											.format('Y-m-d')
											+ " " + orderStaticEndDate;
								}

								kitchenStaticString = selectKitchens;

								isPrompt = true;
								kitchenStatResultWin.show();

							} else {
								Ext.MessageBox.show({
									msg : "至少需要选择一个分厨进行统计",
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							kitchenStatWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					// kitchenMultSelectData = [];
					kitchenStatWin.findById("kitchenMultSelectMStat").reset();
					kitchenStatWin.findById("begDateMStatKitchen").setValue("");
					kitchenStatWin.findById("endDateMStatKitchen").setValue("");

					kitchenMultSelectDS.loadData(kitchenMultSelectData);
				}
			}
		});

// 结果框
// 前台：[日期，廚房名稱，現金，銀行卡，會員卡，掛賬，簽單，合計]
var kitchenStatResultStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../kitchenStatistics.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "kitchenName"
	}, {
		name : "cash"
	}, {
		name : "bankCard"
	}, {
		name : "memberCard"
	}, {
		name : "credit"
	}, {
		name : "sign"
	}, {
		name : "discount"
	}, {
		name : "gift"
	}, {
		name : "total"
	}, {
		name : "message"
	} ])
});

// 2，栏位模型
var kitchenStatResultColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "名称",
			sortable : true,
			dataIndex : "kitchenName",
			width : 100
		}, {
			header : "现金（￥）",
			sortable : true,
			dataIndex : "cash",
			width : 80
		}, {
			header : "银行卡（￥）",
			sortable : true,
			dataIndex : "bankCard",
			width : 80
		}, {
			header : "会员卡（￥）",
			sortable : true,
			dataIndex : "memberCard",
			width : 80
		}, {
			header : "挂账（￥）",
			sortable : true,
			dataIndex : "credit",
			width : 80
		}, {
			header : "签单（￥）",
			sortable : true,
			dataIndex : "sign",
			width : 80
		}, {
			header : "折扣（￥）",
			sortable : true,
			dataIndex : "discount",
			width : 80
		}, {
			header : "赠送（￥）",
			sortable : true,
			dataIndex : "gift",
			width : 80
		}, {
			header : "合计（￥）",
			sortable : true,
			dataIndex : "total",
			width : 100
		} ]);

var kitchenStatResultGrid = new Ext.grid.GridPanel({
	xtype : "grid",
	anchor : "99%",
	border : false,
	ds : kitchenStatResultStore,
	cm : kitchenStatResultColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	viewConfig : {
		forceFit : true
	},
	bbar : new Ext.PagingToolbar({
		pageSize : kitchenStaticRecordCount,
		store : kitchenStatResultStore,
		displayInfo : true,
		displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
		emptyMsg : "没有记录"
	}),
	autoScroll : true,
	loadMask : {
		msg : "数据加载中，请稍等..."
	}
});

// 为store配置beforeload监听器
kitchenStatResultGrid.getStore().on('beforeload', function() {

	// 输入查询条件参数
	this.baseParams = {
		"pin" : pin,
		"kitchenAlias" : kitchenStaticString,
		"dateBegin" : kitchenStaticBeginDate,
		"dateEnd" : kitchenStaticEndDate,
		"StatisticsType" : "Today"
	};

});

// 为store配置load监听器(即load完后动作)
kitchenStatResultGrid
		.getStore()
		.on(
				'load',
				function() {
					if (kitchenStatResultGrid.getStore().getTotalCount() != 0) {
						var msg = this.getAt(0).get("message");
						if (msg != "normal") {
							Ext.MessageBox.show({
								msg : msg,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
							this.removeAll();
						} else {
							kitchenStatResultGrid
									.getStore()
									.each(
											function(record) {
												// 廚房顯示
												for ( var i = 0; i < kitchenMultSelectData.length; i++) {
													if (record
															.get("kitchenName") == kitchenMultSelectData[i][0]) {
														record
																.set(
																		"kitchenName",
																		kitchenMultSelectData[i][1]);
													}
													if (record
															.get("kitchenName") == "SUM") {
														record.set(
																"kitchenName",
																"");
													}
												}

												// 提交，去掉修改標記
												record.commit();
											});
						}
					}
				});

kitchenStatResultWin = new Ext.Window({
	title : "统计结果",
	width : 800,
	height : 370,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : kitchenStatResultGrid,
	buttons : [ {
		text : "退出",
		handler : function() {
			isPrompt = false;
			kitchenStatResultWin.hide();
		}
	} ],
	listeners : {
		"show" : function(thiz) {
			kitchenStatResultGrid.getStore().reload({
				params : {
					start : 0,
					limit : kitchenStaticRecordCount
				}
			});
		}
	}
});

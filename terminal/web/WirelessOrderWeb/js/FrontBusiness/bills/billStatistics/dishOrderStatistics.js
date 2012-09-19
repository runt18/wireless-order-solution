var dishMultSelectDS = new Ext.data.JsonStore({
	root : 'root',
	fields : [ "foodAliasID", "foodName" ]
});

menuStatWin = new Ext.Window(
		{
			title : "点菜统计",
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
							id : "begDateMStatOrder",
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
							id : "endDateMStatOrder",
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
					name : "dishMultSelectMStat",
					id : "dishMultSelectMStat",
					fromStore : dishMultSelectDS,
					dataFields : [ "foodAliasID", "foodName" ],
					toData : [ [ "", "" ] ],
					msHeight : 250,
					valueField : "foodAliasID",
					displayField : "foodName",
					imagePath : "../../extjs/multiselect/images/",
					toLegend : "已选择菜品",
					fromLegend : "可选择菜品"
				} ]
			} ],
			buttons : [
					{
						text : "全选",
						handler : function() {
							var i = menuStatWin.findById("dishMultSelectMStat").fromMultiselect.view;
							i.selectRange(0, i.store.length);
							menuStatWin.findById("dishMultSelectMStat").fromTo();
							menuStatWin.findById("dishMultSelectMStat").toMultiselect.view.clearSelections();
						}
					},
					{
						text : "清空",
						handler : function() {
							menuStatWin.findById("dishMultSelectMStat").reset();
						}
					},
					{
						text : "确定",
						handler : function() {

							var selectCount = Ext.getCmp("dishMultSelectMStat").toMultiselect.store.getCount();

							if (selectCount != 0) {
								isPrompt = false;
								menuStatWin.hide();

								var selectDishes = "";
								for ( var i = 0; i < selectCount; i++) {
									var selectItem = Ext.getCmp("dishMultSelectMStat").toMultiselect.store.getAt(i).get("foodAliasID");
									if (selectItem != "") {
										selectDishes = selectDishes + selectItem + ",";
									}
								}
								// 去掉最后一个逗号
								selectDishes = selectDishes.substring(0, selectDishes.length - 1);

								// 保存条件
								var dateFormated = new Date();
								orderStaticBeginDate = menuStatWin.findById("begDateMStatOrder").getValue();
								orderStaticEndDate = menuStatWin.findById("endDateMStatOrder").getValue();
								
								orderStaticBeginDate =  dateFormated.format('Y-m-d') + " " + (orderStaticBeginDate == '' ? '00:00:00' : orderStaticBeginDate);
								
								orderStaticEndDate =  dateFormated.format('Y-m-d') + " " + (orderStaticEndDate == '' ? '23:59:59' : orderStaticEndDate);
								
//								if(orderStaticBeginDate != "") {
//									orderStaticBeginDate = dateFormated.format('Y-m-d') + " " + orderStaticBeginDate;
//								} 
//
//								if(orderStaticEndDate != "") {
//									orderStaticEndDate = dateFormated.format('Y-m-d') + " " + orderStaticEndDate;
//								}
								
								orderStaticDishesString = selectDishes;
								
								isPrompt = true;
								menuStatResultWin.show();

							} else {
								Ext.MessageBox.show({
									msg : "至少需要选择一个菜品进行统计",
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							menuStatWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {
					menuStatWin.findById("dishMultSelectMStat").reset();
					menuStatWin.findById("begDateMStatOrder").setValue("");
					menuStatWin.findById("endDateMStatOrder").setValue("");
					dishMultSelectDS.loadData(dishMultSelectData);
				},
				"hide" : function(thiz) {
					isPrompt = false;
				}
			}
		});

// 结果框
// 后台：[菜品ｉｄ，菜品名稱，菜品單價，是否臨時，總數量，總價格]
var menuStatResultStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../MenuStatistics.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "dishNumber"
	}, {
		name : "dishName"
	}, {
		name : "dishPrice"
	}, {
		name : "isTemp"
	}, {
		name : "kitchenAlias"
	}, {
		name : "kitchenDisplay"
	}, {
		name : "dishCount"
	}, {
		name : "dishTotalPrice"
	}, {
		name : "message"
	} ])
});

// 2，栏位模型
var menuStatResultColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "编号",
			sortable : true,
			dataIndex : "dishNumber",
			width : 80
		}, {
			header : "菜名",
			sortable : true,
			dataIndex : "dishName",
			width : 100
		}, {
			header : "厨房",
			sortable : true,
			dataIndex : "kitchenDisplay",
			width : 80
		}, {
			header : "数量",
			sortable : true,
			dataIndex : "dishCount",
			width : 60
		}, {
			header : "金额（￥）",
			sortable : true,
			dataIndex : "dishTotalPrice",
			width : 80
		} ]);

var menuStatResultGrid = new Ext.grid.GridPanel({
	// title : "菜品",
	xtype : "grid",
	anchor : "99%",
	border : false,
	ds : menuStatResultStore,
	cm : menuStatResultColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	bbar : new Ext.PagingToolbar({
		pageSize : dishesStaticRecordCount,
		store : menuStatResultStore,
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
menuStatResultGrid.getStore().on('beforeload', function() {

	// 输入查询条件参数
	this.baseParams = {
		"pin" : pin,
		"foodAlias" : orderStaticDishesString,
		"dateBegin" : orderStaticBeginDate,
		"dateEnd" : orderStaticEndDate,
		"StatisticsType" : "Today"
	};

});

// 为store配置load监听器(即load完后动作)
menuStatResultGrid
		.getStore()
		.on(
				'load',
				function() {
					if (menuStatResultGrid.getStore().getTotalCount() != 0) {
						var msg = this.getAt(0).get("message");
						if (msg != "normal") {
							Ext.MessageBox.show({
								msg : msg,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
							this.removeAll();
						} else {
							menuStatResultGrid
									.getStore()
									.each(
											function(record) {
												// 廚房顯示
												for ( var i = 0; i < kitchenMultSelectData.length; i++) {
													if (record
															.get("kitchenAlias") == kitchenMultSelectData[i][0]) {
														record
																.set(
																		"kitchenDisplay",
																		kitchenMultSelectData[i][1]);
													}
													if (record
															.get("kitchenAlias") == "SUM") {
														record.set(
																"kitchenAlias",
																"");
													}
												}

												// 提交，去掉修改標記
												record.commit();
											});
						}
					}
				});

menuStatResultWin = new Ext.Window({
	title : "点菜统计",
	width : 450,
	height : 380,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : menuStatResultGrid,
	buttons : [ {
		text : "退出",
		handler : function() {
			isPrompt = false;
			menuStatResultWin.hide();
		}
	} ],
	listeners : {
		"show" : function(thiz) {
			menuStatResultGrid.getStore().reload({
				params : {
					start : 0,
					limit : dishesStaticRecordCount
				}
			});
		},
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});
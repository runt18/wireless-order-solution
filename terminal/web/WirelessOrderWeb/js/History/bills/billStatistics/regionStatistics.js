var regionMultSelectDS = new Ext.data.SimpleStore({
	fields : [ "retrunValue", "displayText" ],
	data : []
});

regionStatWin = new Ext.Window(
		{
			title : "区域统计",
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
							id : "begDateMStatRegion",
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
							id : "endDateMStatRegion",
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
					name : "regionMultSelectMStat",
					id : "regionMultSelectMStat",
					fromStore : regionMultSelectDS,
					dataFields : [ "retrunValue", "displayText" ],
					toData : [ [ "", "" ] ],
					msHeight : 250,
					valueField : "retrunValue",
					displayField : "displayText",
					imagePath : "../../extjs/multiselect/images/",
					toLegend : "已选择区域",
					fromLegend : "可选择区域"
				} ]
			} ],
			buttons : [
					{
						text : "全选",
						handler : function() {
							var i = regionStatWin
									.findById("regionMultSelectMStat").fromMultiselect.view;
							i.selectRange(0, i.store.length);
							regionStatWin.findById("regionMultSelectMStat")
									.fromTo();
							regionStatWin.findById("regionMultSelectMStat").toMultiselect.view
									.clearSelections();
						}
					},
					{
						text : "清空",
						handler : function() {
							regionStatWin.findById("regionMultSelectMStat")
									.reset();
						}
					},
					{
						text : "确定",
						handler : function() {

							var selectCount = regionStatWin
									.findById("regionMultSelectMStat").toMultiselect.store
									.getCount();

							if (selectCount != 0) {
								isPrompt = false;
								regionStatWin.hide();

								var selectRegions = "";
								for ( var i = 0; i < selectCount; i++) {
									var selectItem = regionStatWin
											.findById("regionMultSelectMStat").toMultiselect.store
											.getAt(i).get("retrunValue");
									// if (selectItem != "") {
									selectRegions = selectRegions + selectItem
											+ ",";
									// }
								}
								// 去掉最后一个逗号
								selectRegions = selectRegions.substring(0,
										selectRegions.length - 1);

								// 保存条件
								var dateFormated = new Date();
								regionStaticBeginDate = regionStatWin.findById(
										"begDateMStatRegion").getValue();
								if (regionStaticBeginDate != "") {
									regionStaticBeginDate = dateFormated
											.format('Y-m-d')
											+ " " + regionStaticBeginDate;
								}

								regionStaticEndDate = regionStatWin.findById(
										"endDateMStatRegion").getValue();
								if (regionStaticEndDate != "") {
									regionStaticEndDate = dateFormated
											.format('Y-m-d')
											+ " " + regionStaticEndDate;
								}

								regionStaticString = selectRegions;

								isPrompt = true;
								regionStatResultWin.show();

							} else {
								Ext.MessageBox.show({
									msg : "至少需要选择一个区域进行统计",
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							regionStatWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					regionStatWin.findById("regionMultSelectMStat").reset();
					regionStatWin.findById("begDateMStatRegion").setValue("");
					regionStatWin.findById("endDateMStatRegion").setValue("");

					regionMultSelectDS.loadData(regionMultSelectData);
				}
			}
		});

// 结果框
// 前台：[日期，廚房名稱，現金，銀行卡，會員卡，掛賬，簽單，合計]
var regionStatResultStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../regionStatistics.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "regionID"
	}, {
		name : "regionDisplay"
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
var regionStatResultColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "名称",
			sortable : true,
			dataIndex : "regionDisplay",
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

var regionStatResultGrid = new Ext.grid.GridPanel({
	xtype : "grid",
	anchor : "99%",
	border : false,
	ds : regionStatResultStore,
	cm : regionStatResultColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	viewConfig : {
		forceFit : true
	},
	bbar : new Ext.PagingToolbar({
		pageSize : regionStaticRecordCount,
		store : regionStatResultStore,
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
regionStatResultGrid.getStore().on('beforeload', function() {

	// 输入查询条件参数
	this.baseParams = {
		"pin" : pin,
		"regionIDs" : regionStaticString,
		"dateBegin" : regionStaticBeginDate,
		"dateEnd" : regionStaticEndDate,
		"StatisticsType" : "Today"
	};

});

// 为store配置load监听器(即load完后动作)
regionStatResultGrid
		.getStore()
		.on(
				'load',
				function() {
					if (regionStatResultGrid.getStore().getTotalCount() != 0) {
						var msg = this.getAt(0).get("message");
						if (msg != "normal") {
							Ext.MessageBox.show({
								msg : msg,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
							this.removeAll();
						} else {
							regionStatResultGrid
									.getStore()
									.each(
											function(record) {
												// 區域顯示
												for ( var i = 0; i < regionMultSelectData.length; i++) {
													if (record.get("regionID") == regionMultSelectData[i][0]) {
														record
																.set(
																		"regionDisplay",
																		regionMultSelectData[i][1]);
													}
													if (record.get("regionID") == "SUM") {
														record
																.set(
																		"regionDisplay",
																		"");
													}
												}

												// 提交，去掉修改標記
												record.commit();
											});
						}
					}
				});

regionStatResultWin = new Ext.Window({
	title : "统计结果",
	width : 800,
	height : 370,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : regionStatResultGrid,
	buttons : [ {
		text : "退出",
		handler : function() {
			isPrompt = false;
			regionStatResultWin.hide();
		}
	} ],
	listeners : {
		"show" : function(thiz) {
			regionStatResultGrid.getStore().reload({
				params : {
					start : 0,
					limit : regionStaticRecordCount
				}
			});
		}
	}
});

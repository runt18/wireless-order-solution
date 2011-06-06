// --------------check-out center panel-----------------
// 1，数据
// 格式:[菜名，口味，数量，单价，一般折扣率，会员折扣率，总价]
//checkOutData = [];
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 2, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 3, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 4, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 5, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 2, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 3, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 4, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 5, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "100%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "100%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "100%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "100%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "100%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "100%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "100%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 2, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 3, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 4, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 5, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
//checkOutData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
//
//var nonMemberTotalCount = 0.0;
//var nonMemberSingleCountArray = [];
//var memberTotalCount = 0.0;
//var memberSingleCountArray = [];
//for ( var i = 0; i < checkOutData.length; i++) {
//	// non member
//	var singleCount = parseFloat(checkOutData[i][3].substring(1))
//			* (parseFloat(checkOutData[i][4].substring(0,
//					checkOutData[i][4].length)) / 100) * checkOutData[i][2];
//	singleCount = singleCount.toFixed(2);
//	// nonMemberSingleCountArray.push(checkOutData[i][3] + " × "
//	// + checkOutData[i][4] + " × " + checkOutData[i][2] + " = " + "￥<b>"
//	// + singleCount + "</b>");
//	nonMemberSingleCountArray.push("￥<b>" + singleCount + "</b>");
//	nonMemberTotalCount = parseFloat(nonMemberTotalCount)
//			+ parseFloat(singleCount);
//	nonMemberTotalCount = nonMemberTotalCount.toFixed(2);
//
//	// member
//	singleCount = parseFloat(checkOutData[i][3].substring(1))
//			* (parseFloat(checkOutData[i][5].substring(0,
//					checkOutData[i][5].length)) / 100) * checkOutData[i][2];
//	singleCount = singleCount.toFixed(2);
//	// memberSingleCountArray.push(checkOutData[i][3] + " × "
//	// + checkOutData[i][5] + " × " + checkOutData[i][2] + " = " + "￥<b>"
//	// + singleCount + "</b>");
//	memberSingleCountArray.push("￥<b>" + singleCount + "</b>");
//	memberTotalCount = parseFloat(memberTotalCount) + parseFloat(singleCount);
//	memberTotalCount = memberTotalCount.toFixed(2);
//}
//
//checkOutDataDisplay = [];
//for ( var i = 0; i < checkOutData.length; i++) {
//	checkOutDataDisplay.push([ checkOutData[i][0], checkOutData[i][1],
//			checkOutData[i][2], checkOutData[i][3], checkOutData[i][4],
//			nonMemberSingleCountArray[i] ]);
//}
//
//checkOutDataDisplay.push([
//		"",
//		"",
//		"",
//		"",
//		"<div style='font-size:18px;font-weight:bold;'>合计</div>",
//		"<div style='font-size:18px;font-weight:bold;'>￥" + nonMemberTotalCount
//				+ "</div>" ]);

// 2，表格的数据store
var checkOutStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(checkOutDataDisplay),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "dishName"
	}, {
		name : "dishTaste"
	}, {
		name : "dishCount"
	}, {
		name : "dishPrice"
	}, {
		name : "dishDiscount"
	}, {
		name : "dishTotalPrice"
	} ])
});

checkOutStore.reload();

// 3，栏位模型
var checkOutColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "菜名",
			sortable : true,
			dataIndex : "dishName",
			id : "dishNameCOCM",
			width : 290
		}, {
			header : "口味",
			sortable : true,
			dataIndex : "dishTaste",
			width : 160
		}, {
			header : "数量",
			sortable : true,
			dataIndex : "dishCount",
			width : 160
		}, {
			header : "单价",
			sortable : true,
			dataIndex : "dishPrice",
			width : 160
		}, {
			header : "打折率",
			sortable : true,
			dataIndex : "dishDiscount",
			width : 160
		}, {
			header : "总价",
			sortable : true,
			dataIndex : "dishTotalPrice",
			width : 160
		} ]);

// 4，表格
var checkOutGrid = new Ext.grid.GridPanel({
	title : "菜式",
	width : 1000,
	style : "margin:0 auto",
	xtype : "grid",
	ds : checkOutStore,
	cm : checkOutColumnModel,
	autoExpandColumn : "dishNameCOCM"
});

// member number input pop window
var memberNbrInputWin = new Ext.Window({
	layout : "fit",
	width : 240,
	height : 100,
	closeAction : "hide",
	buttonAlign : "center",
	resizable : false,
	items : [ {
		layout : "form",
		labelWidth : 60,
		border : false,
		frame : true,
		items : [ {
			xtype : "numberfield",
			fieldLabel : "会员证号",
			id : "memberNbrInput",
			width : 140
		} ]
	} ],
	buttons : [
			{
				text : "确定",
				handler : function() {
					var memberNbr = memberNbrInputWin
							.findById("memberNbrInput").getValue();
					if (memberNbr != "") {

						getMemberInfo(memberNbr);
						checkOurListRefresh();

					}
				}
			}, {
				text : "取消",
				handler : function() {
					memberNbrInputWin.hide();
				}
			} ],
	listeners : {
		beforehide : function(thiz) {
			if (!checkOutForm.findById("memberInfoPanel").isVisible()) {
				discountKindComb.setValue("一般");
				checkOurListRefresh();
				actualMemberID = -1;
			}
		}
	}
});

// membership select comb
var discountKindData = [ [ "0", "一般" ], [ "1", "会员" ] ];

var discountKindComb = new Ext.form.ComboBox({
	fieldLabel : "结账方式",
	labelStyle : "font-size:14px;font-weight:bold;",
	forceSelection : true,
	value : "一般",
	id : "payTpye",
	store : new Ext.data.SimpleStore({
		fields : [ "value", "text" ],
		data : discountKindData
	}),
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false,
	listeners : {
		select : function(combo, record, index) {
			if (record.get("text") == "一般") {
				checkOurListRefresh();
				// hide the member info
				checkOutForm.findById("memberInfoPanel").hide();
			} else {
				memberNbrInputWin.show();
			}

		}
	}
});

var checkOutForm = new Ext.form.FormPanel(
		{
			frame : true,
			border : false,
			items : [
					{
						layout : "column",
						border : false,
						items : [ {
							html : "<div>&nbsp;&nbsp;</div>",
							id : "placeHolderCOF1",
							width : 150
						}, {
							layout : "form",
							border : false,
							labelSeparator : '：',
							labelWidth : 30,
							width : 300,
							items : [ discountKindComb ]
						}, {
							// columnWidth : .25,
							width : 80,
							layout : 'form',
							labelWidth : 0,
							labelSeparator : '',
							hideLabels : true,
							border : false,
							items : [ {
								xtype : 'radio',
								// fieldLabel : '是否连带2A',
								boxLabel : '折扣1',
								checked : true,
								name : 'discountRadio',
								inputValue : 'discount1',
								anchor : '95%',
								listeners : {
									check : function(thiz, newValue, oldValue) {
										// alert("1");
										checkOurListRefresh();
									}
								}
							} ]
						}, {
							// columnWidth : .25,
							width : 80,
							layout : 'form',
							labelWidth : 0,
							labelSeparator : '',
							hideLabels : true,
							border : false,
							items : [ {
								xtype : 'radio',
								// fieldLabel : '',
								boxLabel : '折扣2',
								name : 'discountRadio',
								inputValue : 'discount2',
								anchor : '95%',
								listeners : {
									check : function(thiz, newValue, oldValue) {
										// alert("2");
										checkOurListRefresh();
									}
								}
							} ]
						}, {
							// columnWidth : .25,
							width : 80,
							layout : 'form',
							labelWidth : 0,
							labelSeparator : '',
							hideLabels : true,
							border : false,
							items : [ {
								xtype : 'radio',
								// fieldLabel : '',
								boxLabel : '折扣3',
								name : 'discountRadio',
								inputValue : 'discount3',
								anchor : '95%',
								listeners : {
									check : function(thiz, newValue, oldValue) {
										// alert("3");
										checkOurListRefresh();
									}
								}
							} ]
						} ]
					},
					{
						layout : "column",
						border : false,
						items : [
								{
									html : "<div>&nbsp;&nbsp;</div>",
									id : "placeHolderCOF2",
									hidden : true,
									width : 150
								},
								{
									layout : "fit",
									id : "memberInfoPanel",
									width : 1000,
									contentEl : "memberInfo",
									hidden : true,
									listeners : {
										hide : function(thiz) {
											checkOutForm.findById(
													"placeHolderCOF2").hide();
											// gridHeightOffset = 80;
										},
										show : function(thiz) {
											checkOutForm.findById(
													"placeHolderCOF2").show();
											// gridHeightOffset = 120;
										}
									}
								} ]
					},
					checkOutGrid,
					{
						layout : "column",
						border : false,
						items : [
								{
									html : "<div>&nbsp;&nbsp;</div>",
									id : "placeHolderCOF3",
									// hidden : true,
									width : 150
								},
								{
									layout : "form",
									border : false,
									labelSeparator : '：',
									labelWidth : 40,
									width : 250,
									items : [ {
										html : "<div style='font-size:18px;font-weight:bold;'>合计：       ￥100</div>",
										// fieldLabel : "合计",
										id : "totalCount"
									} ]
								},
								{
									layout : "form",
									border : false,
									labelSeparator : '：',
									labelWidth : 80,
									width : 300,
									items : [ {
										xtype : "textfield",
										fieldLabel : "<span style='font-size:18px;font-weight:bold;'>实收</span>",
										id : "actualCount"
									} ]
								} ]
					}, {
						layout : "column",
						border : false,
						items : [ {
							html : "<div>&nbsp;&nbsp;</div>",
							id : "placeHolderCOF4",
							// hidden : true,
							width : 150
						}, {
							layout : "form",
							border : false,
							labelSeparator : '：',
							labelWidth : 40,
							width : 1000,
							items : [ {
								xtype : "textfield",
								fieldLabel : "备注",
								id : "remark",
								anchor : "%99"
							} ]
						} ]
					} ],
			buttons : [
					{
						text : "现金结账",
						handler : function() {
							paySubmit(1);
						}
					},
					{
						text : "刷卡结账",
						handler : function() {
							paySubmit(2);
						}
					},
					{
						text : "会员卡结账",
						// hidden : true,
						handler : function() {
							paySubmit(3);
						}
					},
					{
						text : "签单",
						// hidden : true,
						handler : function() {
							paySubmit(4);
						}
					},
					{
						text : "挂账",
						// hidden : true,
						handler : function() {
							paySubmit(5);
						}
					},
					{
						text : "返回",
						handler : function() {
							var Request = new URLParaQuery();
							location.href = "TableSelect.html?pin="
									+ Request["pin"] + "&restaurantID="
									+ restaurantID;
						}
					} ],
			listeners : {
				afterlayout : function(thiz) {
					checkOutGrid.setHeight(thiz.getInnerHeight() - 30);
					thiz.findById("placeHolderCOF1").setWidth(
							(thiz.getInnerWidth() - 1000) / 2);
					thiz.findById("placeHolderCOF2").setWidth(
							(thiz.getInnerWidth() - 1000) / 2);
					thiz.findById("placeHolderCOF3").setWidth(
							(thiz.getInnerWidth() - 1000) / 2);
					thiz.findById("placeHolderCOF4").setWidth(
							(thiz.getInnerWidth() - 1000) / 2);
				}
			}
		});

var checkOutCenterPanel = new Ext.Panel({
	region : "center",
	id : "checkOutCenterPanel",
	layout : "fit",
	items : [ checkOutForm ]
});

// --------------check-out north panel-----------------
var checkOutNorthPanel = new Ext.Panel({
	id : "checkOutNorthPanel",
	region : "north",
	title : "<div style='font-size:18px;padding-left:2px'>结账<div>",
	height : 75,
	border : false,
	layout : "form",
	frame : true,
	contentEl : "tableStatusCO"
});

Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// alert(window.location.href
			// .substr(window.location.href.indexOf("=") + 1));
			// *************整体布局*************
			var centerPanelCO = new Ext.Panel({
				id : "centerPanelDO",
				region : "center",
				border : false,
				margins : "0 5 0 0",
				layout : "border",
				items : [ checkOutCenterPanel, checkOutNorthPanel ]
			});

			var viewport = new Ext.Viewport(
					{
						layout : "border",
						items : [
								{
									region : "north",
									html : "<div style='padding:10px; background-color:#A9D0F5'><h4 style='font-size:150%'>无线点餐网页终端<h4></div>",
									height : 50,
									margins : "0 0 5 0"
								},
								centerPanelCO,
								{
									region : "south",
									height : 30,
									layout : "form",
									frame : true,
									border : false,
									html : "<div style='font-size:11pt; text-align:center;'><b>版权所有(c) 2011 智易科技</b></div>"
								} ]
					});

			// -------------------- 浏览器大小改变 -------------------------------
			// 1,调整colDisplayFormUQ中表格的高度
			Ext.EventManager
					.onWindowResize(function() {
						checkOutGrid.setHeight(checkOutCenterPanel
								.getInnerHeight()
								- gridHeightOffset);

						if (checkOutCenterPanel.getInnerWidth() < 1000) {
							checkOutGrid.setWidth(checkOutCenterPanel
									.getInnerWidth() - 20);
						} else {
							checkOutGrid.setWidth(1000);
						}
					});
		});

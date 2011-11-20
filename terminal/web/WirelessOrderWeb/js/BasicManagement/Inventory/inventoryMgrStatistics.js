//----------------- 入庫統計 --------------------
// 条件框
var inventoryInStatMSDS = new Ext.data.SimpleStore({
	fields : [ "retrunValue", "displayText" ],
	data : []
});

var inStatSupplierComboStore = new Ext.data.SimpleStore({
	fields : [ "value", "text" ],
	data : []
});

var inStatSupplierCombo = new Ext.form.ComboBox({
	fieldLabel : "供应商",
	forceSelection : true,
	width : 150,
	value : "全部",
	id : "inStatSupplierCombo",
	store : inStatSupplierComboStore,
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false
});

inventoryInStatWin = new Ext.Window(
		{
			title : "入库统计",
			width : 450,
			height : 430,
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
						labelWidth : 50,
						items : [ {
							xtype : "datefield",
							id : "begDateInStat",
							width : 120,
							fieldLabel : "日期"
						} ]
					}, {
						layout : "form",
						border : false,
						labelSeparator : ' ',
						width : 200,
						labelWidth : 50,
						items : [ {
							xtype : "datefield",
							id : "endDateInStat",
							width : 120,
							fieldLabel : "至"
						} ]
					} ]
				} ]
			}, {
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
						labelSeparator : '：',
						width : 200,
						labelWidth : 50,
						items : inStatSupplierCombo
					} ]
				} ]
			}, {
				layout : "column",
				border : false,
				anchor : "right 15%",
				autoScroll : true,
				frame : true,
				items : [ {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept1InStat",
					// fieldLabel : departmentData[0][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept2InStat",
					// fieldLabel : departmentData[1][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept3InStat",
					// fieldLabel : departmentData[2][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept4InStat",
					// fieldLabel : departmentData[3][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept5InStat",
					// fieldLabel : departmentData[4][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept6InStat",
					// fieldLabel : departmentData[5][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept7InStat",
					// fieldLabel : departmentData[6][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept8InStat",
					// fieldLabel : departmentData[7][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept9InStat",
					// fieldLabel : departmentData[8][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept10InStat",
					// fieldLabel : departmentData[9][1]
					} ]
				} ]
			}, {
				layout : "form",
				border : false,
				frame : true,
				hideLabels : true,
				anchor : "right 56%",
				items : [ {
					xtype : "itemselector",
					name : "materialInStatMultSelect",
					id : "materialInStatMultSelect",
					fromStore : inventoryInStatMSDS,
					dataFields : [ "retrunValue", "displayText" ],
					toData : [ [ "", "" ] ],
					msHeight : 173,
					valueField : "retrunValue",
					displayField : "displayText",
					imagePath : "../../extjs/multiselect/images/",
					toLegend : "已选择食材",
					fromLegend : "可选择食材"
				} ]
			}, {
				layout : "column",
				border : false,
				anchor : "right 10%",
				frame : true,
				items : [ {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 110,
					labelWidth : 60,
					items : [ {
						xtype : "checkbox",
						id : "detailInStat",
						fieldLabel : "特价"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 110,
					labelWidth : 60,
					items : [ {
						xtype : "checkbox",
						id : "sumByMaterialInStat",
						fieldLabel : "推荐"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 110,
					labelWidth : 60,
					items : [ {
						xtype : "checkbox",
						id : "sumByDeptInStat",
						fieldLabel : "赠送"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 110,
					labelWidth : 60,
					items : [ {
						xtype : "checkbox",
						id : "sumBySuplierInStat",
						fieldLabel : "停售"
					} ]
				} ]
			} ],
			buttons : [
					{
						text : "全选",
						handler : function() {
							var i = inventoryInStatWin
									.findById("materialInStatMultSelect").fromMultiselect.view;
							i.selectRange(0, i.store.length);
							inventoryInStatWin.findById(
									"materialInStatMultSelect").fromTo();
							inventoryInStatWin
									.findById("materialInStatMultSelect").toMultiselect.view
									.clearSelections();
						}
					},
					{
						text : "清空",
						handler : function() {
							inventoryInStatWin.findById(
									"materialInStatMultSelect").reset();
						}
					}, {
						text : "确定",
						handler : function() {
							//
							// var selectCount = kitchenStatWin
							// .findById("kitchenMultSelectMStat").toMultiselect.store
							// .getCount();
							//
							// if (selectCount != 0) {
							// kitchenStatWin.hide();
							//
							// var selectKitchens = "";
							// for ( var i = 0; i < selectCount; i++) {
							// var selectItem = kitchenStatWin
							// .findById("kitchenMultSelectMStat").toMultiselect.store
							// .getAt(i).get("retrunValue");
							// // if (selectItem != "") {
							// selectKitchens = selectKitchens
							// + selectItem + ",";
							// // }
							// }
							// // 去掉最后一个逗号
							// selectKitchens = selectKitchens.substring(0,
							// selectKitchens.length - 1);
							//
							// // 保存条件
							// kitchenStaticBeginDate = kitchenStatWin
							// .findById("begDateMStat").getValue();
							// if (kitchenStaticBeginDate != "") {
							// var dateFormated = new Date();
							// dateFormated = kitchenStaticBeginDate;
							// kitchenStaticBeginDate = dateFormated
							// .format('Y-m-d');
							// }
							//
							// kitchenStaticEndDate = kitchenStatWin.findById(
							// "endDateMStat").getValue();
							// if (kitchenStaticEndDate != "") {
							// var dateFormated = new Date();
							// dateFormated = kitchenStaticEndDate;
							// kitchenStaticEndDate = dateFormated
							// .format('Y-m-d');
							// }
							//
							// kitchenStaticString = selectKitchens;
							//
							// kitchenStatResultWin.show();
							//
							// } else {
							// Ext.MessageBox.show({
							// msg : "至少需要选择一个分厨进行统计",
							// width : 300,
							// buttons : Ext.MessageBox.OK
							// });
							// }
						}
					}, {
						text : "取消",
						handler : function() {
							inventoryInStatWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					inventoryInStatWin.findById("materialInStatMultSelect")
							.reset();
					inventoryInStatWin.findById("begDateInStat").setValue("");
					inventoryInStatWin.findById("endDateInStat").setValue("");

					inStatSupplierCombo.setValue("全部");

					inventoryInStatMSDS.loadData(materialComboData);
					inStatSupplierComboStore.loadData(supplierComboData);

					// 神技！動態改變form中component的label！！！
					inventoryInStatWin.findById("dept1InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[0][1]
							+ ":";
					inventoryInStatWin.findById("dept2InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[1][1]
							+ ":";
					inventoryInStatWin.findById("dept3InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[2][1]
							+ ":";
					inventoryInStatWin.findById("dept4InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[3][1]
							+ ":";
					inventoryInStatWin.findById("dept5InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[4][1]
							+ ":";
					inventoryInStatWin.findById("dept6InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[5][1]
							+ ":";
					inventoryInStatWin.findById("dept7InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[6][1]
							+ ":";
					inventoryInStatWin.findById("dept8InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[7][1]
							+ ":";
					inventoryInStatWin.findById("dept9InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[8][1]
							+ ":";
					inventoryInStatWin.findById("dept10InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[9][1]
							+ ":";

				}
			}
		});

// // 结果框
// // 前台：[日期，廚房名稱，現金，銀行卡，會員卡，掛賬，簽單，合計]
// var kitchenStatResultStore = new Ext.data.Store({
// proxy : new Ext.data.HttpProxy({
// url : "../../kitchenStatistics.do"
// }),
// reader : new Ext.data.JsonReader({
// totalProperty : "totalProperty",
// root : "root"
// }, [ {
// name : "statDate"
// }, {
// name : "kitchenName"
// }, {
// name : "cash"
// }, {
// name : "bankCard"
// }, {
// name : "memberCard"
// }, {
// name : "credit"
// }, {
// name : "sign"
// }, {
// name : "total"
// }, {
// name : "message"
// } ])
// });
//
// // 2，栏位模型
// var kitchenStatResultColumnModel = new Ext.grid.ColumnModel([
// new Ext.grid.RowNumberer(), {
// header : "日期",
// sortable : true,
// dataIndex : "statDate",
// width : 80
// }, {
// header : "名称",
// sortable : true,
// dataIndex : "kitchenName",
// width : 100
// }, {
// header : "现金（￥）",
// sortable : true,
// dataIndex : "cash",
// width : 80
// }, {
// header : "银行卡（￥）",
// sortable : true,
// dataIndex : "bankCard",
// width : 80
// }, {
// header : "会员卡（￥）",
// sortable : true,
// dataIndex : "memberCard",
// width : 80
// }, {
// header : "挂账（￥）",
// sortable : true,
// dataIndex : "credit",
// width : 80
// }, {
// header : "签单（￥）",
// sortable : true,
// dataIndex : "sign",
// width : 80
// }, {
// header : "合计（￥）",
// sortable : true,
// dataIndex : "total",
// width : 100
// } ]);
//
// var kitchenStatResultGrid = new Ext.grid.GridPanel({
// xtype : "grid",
// anchor : "99%",
// border : false,
// ds : kitchenStatResultStore,
// cm : kitchenStatResultColumnModel,
// sm : new Ext.grid.RowSelectionModel({
// singleSelect : true
// }),
// viewConfig : {
// forceFit : true
// },
// bbar : new Ext.PagingToolbar({
// pageSize : kitchenStaticRecordCount,
// store : kitchenStatResultStore,
// displayInfo : true,
// displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
// emptyMsg : "没有记录"
// }),
// autoScroll : true,
// loadMask : {
// msg : "数据加载中，请稍等..."
// }
// });
//
// // 为store配置beforeload监听器
// kitchenStatResultGrid.getStore().on('beforeload', function() {
//
// // 输入查询条件参数
// this.baseParams = {
// "pin" : pin,
// "kitchenIDs" : kitchenStaticString,
// "dateBegin" : kitchenStaticBeginDate,
// "dateEnd" : kitchenStaticEndDate
// };
//
// });
//
// // 为store配置load监听器(即load完后动作)
// kitchenStatResultGrid
// .getStore()
// .on(
// 'load',
// function() {
// if (kitchenStatResultGrid.getStore().getTotalCount() != 0) {
// var msg = this.getAt(0).get("message");
// if (msg != "normal") {
// Ext.MessageBox.show({
// msg : msg,
// width : 300,
// buttons : Ext.MessageBox.OK
// });
// this.removeAll();
// } else {
// kitchenStatResultGrid
// .getStore()
// .each(
// function(record) {
// // 廚房顯示
// for ( var i = 0; i < kitchenMultSelectData.length; i++) {
// if (record
// .get("kitchenName") == kitchenMultSelectData[i][0]) {
// record
// .set(
// "kitchenName",
// kitchenMultSelectData[i][1]);
// }
// if (record
// .get("kitchenName") == "SUM") {
// record.set(
// "kitchenName",
// "");
// }
// }
//
// // 提交，去掉修改標記
// record.commit();
// });
// }
// }
// });
//
// kitchenStatResultWin = new Ext.Window({
// title : "统计结果",
// width : 800,
// height : 370,
// closeAction : "hide",
// resizable : false,
// layout : "fit",
// items : kitchenStatResultGrid,
// buttons : [ {
// text : "退出",
// handler : function() {
// kitchenStatResultWin.hide();
// }
// } ],
// listeners : {
// "show" : function(thiz) {
// kitchenStatResultGrid.getStore().reload({
// params : {
// start : 0,
// limit : kitchenStaticRecordCount
// }
// });
// }
// }
// });

﻿// ----------------- 入庫 --------------------
inventoryInWin = new Ext.Window(
		{
			layout : "fit",
			// title : "入庫 -- ",
			width : 260,
			height : 190,
			closeAction : "hide",
			resizable : false,
			items : [ {
				layout : "form",
				id : "inventoryInForm",
				labelWidth : 60,
				border : false,
				frame : true,
				items : [ {
					xtype : "numberfield",
					fieldLabel : "数量",
					id : "inventoryInCount",
					allowBlank : false,
					width : 160
				}, {
					xtype : "numberfield",
					fieldLabel : "价格",
					id : "inventoryInPrice",
					allowBlank : false,
					width : 160
				}, {
					xtype : "datefield",
					fieldLabel : "日期",
					id : "inventoryInDate",
					allowBlank : false,
					width : 160
				} ]
			} ],
			buttons : [
					{
						text : "确定",
						handler : function() {

							if (inventoryInWin.findById("inventoryInCount")
									.isValid()
									&& inventoryInWin.findById(
											"inventoryInPrice").isValid()
									&& inventoryInWin.findById(
											"inventoryInDate").isValid()
									&& inventoryInWin.findById(
											"departmentCombIn").isValid()
									&& inventoryInWin.findById("supplierComb")
											.isValid()) {

								var inventoryInCount = inventoryInWin.findById(
										"inventoryInCount").getValue();
								var inventoryInPrice = inventoryInWin.findById(
										"inventoryInPrice").getValue();

								var inventoryInDate = inventoryInWin.findById(
										"inventoryInDate").getValue();
								var dateFormated = new Date();
								dateFormated = inventoryInDate;
								inventoryInDate = dateFormated.format('Y-m-d');

								var department = inventoryInWin.findById(
										"departmentCombIn").getValue();
								for ( var i = 0; i < departmentData.length; i++) {
									if (department == departmentData[i][1]) {
										department = departmentData[i][0];
									}
								}

								var supplier = inventoryInWin.findById(
										"supplierComb").getValue();
								for ( var i = 0; i < supplierData.length; i++) {
									if (supplier == supplierData[i][2]) {
										supplier = supplierData[i][0];
									}
								}

								var material = materialGrid.getStore().getAt(
										currRowIndex).get("materialID");

								var staff = document.getElementById("optName").innerHTML;

								isPrompt = false;
								inventoryInWin.hide();

								// type: 0 : 消耗 1 : 报损 2 : 销售 3 : 退货 4 : 入库 5 :
								// 调出 6 : 调入 7 : 盘点
								Ext.Ajax.request({
									url : "../../InventoryIn.do",
									params : {
										"pin" : pin,
										"supplierID" : supplier,
										"materialID" : material,
										"price" : inventoryInPrice,
										"date" : inventoryInDate,
										"deptID" : department,
										"amount" : inventoryInCount,
										"staff" : staff,
										"type" : 4
									},
									success : function(response, options) {
										var resultJSON = Ext.util.JSON
												.decode(response.responseText);
										if (resultJSON.success == true) {
											// materialStore
											// .reload({
											// params : {
											// start : 0,
											// limit : materialPageRecordCount
											// }
											// });

											var dataInfo = resultJSON.data;
											Ext.MessageBox.show({
												msg : dataInfo,
												width : 300,
												buttons : Ext.MessageBox.OK
											});
										} else {
											var dataInfo = resultJSON.data;
											Ext.MessageBox.show({
												msg : dataInfo,
												width : 300,
												buttons : Ext.MessageBox.OK
											});
										}
									},
									failure : function(response, options) {
									}
								});

							}

						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							inventoryInWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					inventoryInWin.setTitle("入库 -- "
							+ materialGrid.getStore().getAt(currRowIndex).get(
									"materialName"));

					inventoryInWin.findById("inventoryInCount").setValue("");
					inventoryInWin.findById("inventoryInCount").clearInvalid();

					inventoryInWin.findById("inventoryInPrice").setValue("");
					inventoryInWin.findById("inventoryInPrice").clearInvalid();

					inventoryInWin.findById("inventoryInDate").setValue("");
					inventoryInWin.findById("inventoryInDate").clearInvalid();

					inventoryInWin.findById("inventoryInForm").remove(
							"departmentCombIn");
					inventoryInWin.findById("inventoryInForm").remove(
							"supplierComb");

					var departmentCombIn = new Ext.form.ComboBox({
						fieldLabel : "部门",
						forceSelection : true,
						width : 160,
						value : departmentData[0][1],
						id : "departmentCombIn",
						store : new Ext.data.SimpleStore({
							fields : [ "value", "text" ],
							data : departmentData
						}),
						valueField : "value",
						displayField : "text",
						typeAhead : true,
						mode : "local",
						triggerAction : "all",
						selectOnFocus : true,
						allowBlank : false
					});

					var supplierComb = new Ext.form.ComboBox({
						fieldLabel : "供应商",
						forceSelection : true,
						width : 160,
						value : supplierData[0][2],
						id : "supplierComb",
						store : new Ext.data.SimpleStore({
							fields : [ "value", "alias", "text" ],
							data : supplierData
						}),
						valueField : "value",
						displayField : "text",
						typeAhead : true,
						mode : "local",
						triggerAction : "all",
						selectOnFocus : true,
						allowBlank : false
					});

					inventoryInWin.findById("inventoryInForm").add(
							"departmentCombIn");
					inventoryInWin.findById("inventoryInForm").add(
							"supplierComb");

					inventoryInWin.doLayout();

					var f = Ext.get("inventoryInCount");
					f.focus.defer(100, f); // 为什么这样才可以！？！？

				}
			}
		});

// ----------------- 出庫 --------------------
var outReasonData = [ [ 1, "报损" ], [ 2, "销售" ], [ 3, "退货" ] ];

inventoryOutWin = new Ext.Window(
		{
			layout : "fit",
			width : 260,
			height : 190,
			closeAction : "hide",
			resizable : false,
			items : [ {
				layout : "form",
				id : "inventoryOutForm",
				labelWidth : 60,
				border : false,
				frame : true,
				items : [ {
					xtype : "numberfield",
					fieldLabel : "数量",
					id : "inventoryOutCount",
					allowBlank : false,
					width : 160
				}, {
					xtype : "numberfield",
					fieldLabel : "价格",
					id : "inventoryOutPrice",
					allowBlank : false,
					width : 160
				}, {
					xtype : "datefield",
					fieldLabel : "日期",
					id : "inventoryOutDate",
					allowBlank : false,
					width : 160
				} ]
			} ],
			buttons : [
					{
						text : "确定",
						handler : function() {

							if (inventoryOutWin.findById("inventoryOutCount")
									.isValid()
									&& inventoryOutWin.findById(
											"inventoryOutPrice").isValid()
									&& inventoryOutWin.findById(
											"inventoryOutDate").isValid()
									&& inventoryOutWin.findById(
											"departmentCombOut").isValid()
									&& inventoryOutWin.findById("outReason")
											.isValid()) {

								var inventoryOutCount = inventoryOutWin
										.findById("inventoryOutCount")
										.getValue();
								var inventoryOutPrice = inventoryOutWin
										.findById("inventoryOutPrice")
										.getValue();

								var inventoryOutDate = inventoryOutWin
										.findById("inventoryOutDate")
										.getValue();
								var dateFormated = new Date();
								dateFormated = inventoryOutDate;
								inventoryOutDate = dateFormated.format('Y-m-d');

								var department = inventoryOutWin.findById(
										"departmentCombOut").getValue();
								for ( var i = 0; i < departmentData.length; i++) {
									if (department == departmentData[i][1]) {
										department = departmentData[i][0];
									}
								}

								var outReason = inventoryOutWin.findById(
										"outReason").getValue();
								for ( var i = 0; i < outReasonData.length; i++) {
									if (outReason == outReasonData[i][1]) {
										outReason = outReasonData[i][0];
									}
								}

								var material = materialGrid.getStore().getAt(
										currRowIndex).get("materialID");

								var staff = document.getElementById("optName").innerHTML;

								isPrompt = false;
								inventoryOutWin.hide();

								// type: 0 : 消耗 1 : 报损 2 : 销售 3 : 退货 4 : 入库 5 :
								// 调出 6 : 调入 7 : 盘点
								Ext.Ajax.request({
									url : "../../InventoryOut.do",
									params : {
										"pin" : pin,
										"materialID" : material,
										"price" : inventoryOutPrice,
										"date" : inventoryOutDate,
										"deptID" : department,
										"amount" : inventoryOutCount,
										"staff" : staff,
										"type" : outReason
									},
									success : function(response, options) {
										var resultJSON = Ext.util.JSON
												.decode(response.responseText);
										if (resultJSON.success == true) {

											var dataInfo = resultJSON.data;
											Ext.MessageBox.show({
												msg : dataInfo,
												width : 300,
												buttons : Ext.MessageBox.OK
											});
										} else {
											var dataInfo = resultJSON.data;
											Ext.MessageBox.show({
												msg : dataInfo,
												width : 300,
												buttons : Ext.MessageBox.OK
											});
										}
									},
									failure : function(response, options) {
									}
								});

							}

						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							inventoryOutWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					inventoryOutWin.setTitle("出库 -- "
							+ materialGrid.getStore().getAt(currRowIndex).get(
									"materialName"));

					inventoryOutWin.findById("inventoryOutCount").setValue("");
					inventoryOutWin.findById("inventoryOutCount")
							.clearInvalid();

					inventoryOutWin.findById("inventoryOutPrice").setValue("");
					inventoryOutWin.findById("inventoryOutPrice")
							.clearInvalid();

					inventoryOutWin.findById("inventoryOutDate").setValue("");
					inventoryOutWin.findById("inventoryOutDate").clearInvalid();

					inventoryOutWin.findById("inventoryOutForm").remove(
							"departmentCombOut");
					inventoryOutWin.findById("inventoryOutForm").remove(
							"outReason");

					var departmentCombOut = new Ext.form.ComboBox({
						fieldLabel : "部门",
						forceSelection : true,
						width : 160,
						value : departmentData[0][1],
						id : "departmentCombOut",
						store : new Ext.data.SimpleStore({
							fields : [ "value", "text" ],
							data : departmentData
						}),
						valueField : "value",
						displayField : "text",
						typeAhead : true,
						mode : "local",
						triggerAction : "all",
						selectOnFocus : true,
						allowBlank : false
					});

					var outReason = new Ext.form.ComboBox({
						fieldLabel : "出库原因",
						forceSelection : true,
						width : 160,
						value : "报损",
						id : "outReason",
						store : new Ext.data.SimpleStore({
							fields : [ "value", "text" ],
							data : outReasonData
						}),
						valueField : "value",
						displayField : "text",
						typeAhead : true,
						mode : "local",
						triggerAction : "all",
						selectOnFocus : true,
						allowBlank : false
					});

					inventoryOutWin.findById("inventoryOutForm").add(
							"departmentCombOut");
					inventoryOutWin.findById("inventoryOutForm").add(
							"outReason");

					inventoryOutWin.doLayout();

					var f = Ext.get("inventoryOutCount");
					f.focus.defer(100, f); // 为什么这样才可以！？！？

				}
			}
		});

// ----------------- 調撥 --------------------
inventoryChangeWin = new Ext.Window(
		{
			layout : "fit",
			width : 260,
			height : 190,
			closeAction : "hide",
			resizable : false,
			items : [ {
				layout : "form",
				id : "inventoryChangeForm",
				labelWidth : 60,
				border : false,
				frame : true,
				items : [ {
					xtype : "numberfield",
					fieldLabel : "数量",
					id : "inventoryChangeCount",
					allowBlank : false,
					width : 160
				}, {
					xtype : "datefield",
					fieldLabel : "日期",
					id : "inventoryChangeDate",
					allowBlank : false,
					width : 160
				} ]
			} ],
			buttons : [
					{
						text : "确定",
						handler : function() {

							if (inventoryChangeWin.findById(
									"inventoryChangeCount").isValid()
									&& inventoryChangeWin.findById(
											"inventoryChangeDate").isValid()
									&& inventoryChangeWin.findById(
											"departmentCombChangeOut")
											.isValid()
									&& inventoryChangeWin.findById(
											"departmentCombChangeIn").isValid()) {

								var inventoryChangeCount = inventoryChangeWin
										.findById("inventoryChangeCount")
										.getValue();

								var inventoryChangeDate = inventoryChangeWin
										.findById("inventoryChangeDate")
										.getValue();
								var dateFormated = new Date();
								dateFormated = inventoryChangeDate;
								inventoryChangeDate = dateFormated
										.format('Y-m-d');

								var departmentOut = inventoryChangeWin
										.findById("departmentCombChangeOut")
										.getValue();
								for ( var i = 0; i < departmentData.length; i++) {
									if (departmentOut == departmentData[i][1]) {
										departmentOut = departmentData[i][0];
									}
								}

								var departmentIn = inventoryChangeWin.findById(
										"departmentCombChangeIn").getValue();
								for ( var i = 0; i < departmentData.length; i++) {
									if (departmentIn == departmentData[i][1]) {
										departmentIn = departmentData[i][0];
									}
								}

								var material = materialGrid.getStore().getAt(
										currRowIndex).get("materialID");

								var staff = document.getElementById("optName").innerHTML;

								isPrompt = false;
								inventoryChangeWin.hide();

								// type: 0 : 消耗 1 : 报损 2 : 销售 3 : 退货 4 : 入库 5 :
								// 调出 6 : 调入 7 : 盘点
								Ext.Ajax.request({
									url : "../../InventoryChange.do",
									params : {
										"pin" : pin,
										"materialID" : material,
										"date" : inventoryChangeDate,
										"deptIDOut" : departmentOut,
										"deptIDIn" : departmentIn,
										"amount" : inventoryChangeCount,
										"staff" : staff
									},
									success : function(response, options) {
										var resultJSON = Ext.util.JSON
												.decode(response.responseText);
										if (resultJSON.success == true) {

											var dataInfo = resultJSON.data;
											Ext.MessageBox.show({
												msg : dataInfo,
												width : 300,
												buttons : Ext.MessageBox.OK
											});
										} else {
											var dataInfo = resultJSON.data;
											Ext.MessageBox.show({
												msg : dataInfo,
												width : 300,
												buttons : Ext.MessageBox.OK
											});
										}
									},
									failure : function(response, options) {
									}
								});

							}

						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							inventoryChangeWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					inventoryChangeWin.setTitle("调拨 -- "
							+ materialGrid.getStore().getAt(currRowIndex).get(
									"materialName"));

					inventoryChangeWin.findById("inventoryChangeCount")
							.setValue("");
					inventoryChangeWin.findById("inventoryChangeCount")
							.clearInvalid();

					inventoryChangeWin.findById("inventoryChangeDate")
							.setValue("");
					inventoryChangeWin.findById("inventoryChangeDate")
							.clearInvalid();

					inventoryChangeWin.findById("inventoryChangeForm").remove(
							"departmentCombChangeOut");
					inventoryChangeWin.findById("inventoryChangeForm").remove(
							"departmentCombChangeIn");

					var departmentCombChangeOut = new Ext.form.ComboBox({
						fieldLabel : "调出部门",
						forceSelection : true,
						width : 160,
						value : departmentData[0][1],
						id : "departmentCombChangeOut",
						store : new Ext.data.SimpleStore({
							fields : [ "value", "text" ],
							data : departmentData
						}),
						valueField : "value",
						displayField : "text",
						typeAhead : true,
						mode : "local",
						triggerAction : "all",
						selectOnFocus : true,
						allowBlank : false
					});

					var departmentCombChangeIn = new Ext.form.ComboBox({
						fieldLabel : "调入部门",
						forceSelection : true,
						width : 160,
						value : departmentData[0][1],
						id : "departmentCombChangeIn",
						store : new Ext.data.SimpleStore({
							fields : [ "value", "text" ],
							data : departmentData
						}),
						valueField : "value",
						displayField : "text",
						typeAhead : true,
						mode : "local",
						triggerAction : "all",
						selectOnFocus : true,
						allowBlank : false
					});

					inventoryChangeWin.findById("inventoryChangeForm").add(
							"departmentCombChangeOut");
					inventoryChangeWin.findById("inventoryChangeForm").add(
							"departmentCombChangeIn");

					inventoryChangeWin.doLayout();

					var f = Ext.get("inventoryChangeCount");
					f.focus.defer(100, f); // 为什么这样才可以！？！？

				}
			}
		});
// -------------------------------------------------------------------------------------------------------
// ----------------- 入庫統計 --------------------
var inventInStatDS = new Ext.data.SimpleStore({
	fields : [ "retrunValue", "displayText" ],
	data : []
});

var inventInStatWin = new Ext.Window(
		{
			title : "入库统计",
			width : 450,
			height : 500,
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
							xtype : "datefield",
							id : "begDateIn",
							width : 150,
							fieldLabel : "日期"
						} ]
					}, {
						layout : "form",
						border : false,
						labelSeparator : ' ',
						width : 200,
						labelWidth : 30,
						items : [ {
							xtype : "datefield",
							id : "endDateIn",
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
					name : "InMultSelect",
					id : "InMultSelect",
					fromStore : inventInStatDS,
					dataFields : [ "retrunValue", "displayText" ],
					toData : [ [ "", "" ] ],
					msHeight : 250,
					valueField : "retrunValue",
					displayField : "displayText",
					imagePath : "../../extjs/multiselect/images/",
					toLegend : "已选择食材",
					fromLegend : "可选择食材"
				} ]
			} ],
			buttons : [
					{
						text : "全选",
						handler : function() {
							var i = inventInStatWin.findById("InMultSelect").fromMultiselect.view;
							i.selectRange(0, i.store.length);
							inventInStatWin.findById("InMultSelect").fromTo();
							inventInStatWin.findById("InMultSelect").toMultiselect.view
									.clearSelections();
						}
					},
					{
						text : "清空",
						handler : function() {
							inventInStatWin.findById("InMultSelect").reset();
						}
					},
					{
						text : "确定",
						handler : function() {

							var selectCount = inventInStatWin
									.findById("InMultSelect").toMultiselect.store
									.getCount();

							if (selectCount != 0) {
								isPrompt = false;
								inventInStatWin.hide();

								var selectMaterials = "";
								for ( var i = 0; i < selectCount; i++) {
									var selectItem = inventInStatWin
											.findById("InMultSelect").toMultiselect.store
											.getAt(i).get("retrunValue");
									if (selectItem != "") {
										selectMaterials = selectMaterials
												+ selectItem + ",";
									}
								}
								// 去掉最后一个逗号
								selectMaterials = selectMaterials.substring(0,
										selectMaterials.length - 1);

								// 保存条件
								var begDateIn = inventInStatWin.findById(
										"begDateIn").getValue();
								if (begDateIn != "") {
									var dateFormated = new Date();
									dateFormated = begDateIn;
									begDateIn = dateFormated.format('Y-m-d');
								}

								var endDateIn = inventInStatWin.findById(
										"endDateIn").getValue();
								if (endDateIn != "") {
									var dateFormated = new Date();
									dateFormated = endDateIn;
									endDateIn = dateFormated.format('Y-m-d');
								}

								var materialsString = selectMaterials;

								inventInStatWin.show();

							} else {
								Ext.MessageBox.show({
									msg : "至少需要选择一个 食材进行统计",
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							inventInStatWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					// kitchenMultSelectData = [];
					inventInStatWin.findById("InMultSelect").reset();
					inventInStatWin.findById("begDateIn").setValue("");
					inventInStatWin.findById("begDateOut").setValue("");

					InMultSelect.loadData(InMultSelectData);
				}
			}
		});

// ----------------- 添加食材 --------------------
materialAddWin = new Ext.Window({
	layout : "fit",
	title : "添加食材",
	width : 260,
	height : 190,
	closeAction : "hide",
	resizable : false,
	items : [ {
		layout : "form",
		id : "materialAddForm",
		labelWidth : 60,
		border : false,
		frame : true,
		items : [ {
			xtype : "numberfield",
			fieldLabel : "编号",
			id : "materialAddNumber",
			allowBlank : false,
			width : 160
		}, {
			xtype : "textfield",
			fieldLabel : "名称",
			id : "materialAddName",
			allowBlank : false,
			width : 160
		}, {
			xtype : "numberfield",
			fieldLabel : "预警阀值",
			id : "materialAddWarning",
			allowBlank : false,
			width : 160
		}, {
			xtype : "numberfield",
			fieldLabel : "危险阀值",
			id : "materialAddDanger",
			allowBlank : false,
			width : 160
		} ]
	} ],
	buttons : [
			{
				text : "确定",
				handler : function() {

					if (materialAddWin.findById("materialAddNumber").isValid()
							&& materialAddWin.findById("materialAddName")
									.isValid()
							&& materialAddWin.findById("materialAddWarning")
									.isValid()
							&& materialAddWin.findById("materialAddDanger")
									.isValid()) {

						var materialAddNumber = materialAddWin.findById(
								"materialAddNumber").getValue();
						var materialAddName = materialAddWin.findById(
								"materialAddName").getValue();
						var materialAddWarning = materialAddWin.findById(
								"materialAddWarning").getValue();
						var materialAddDanger = materialAddWin.findById(
								"materialAddDanger").getValue();

						var isDuplicate = false;
						for ( var i = 0; i < materialData.length; i++) {
							if (materialAddNumber == materialData[i][1]) {
								isDuplicate = true;
							}
						}

						if (!isDuplicate) {
							materialAddWin.hide();

							Ext.Ajax.request({
								url : "../../InsertMaterial.do",
								params : {
									"pin" : pin,
									"materialAlias" : materialAddNumber,
									"materialName" : materialAddName,
									"materialWarning" : materialAddWarning,
									"materialDanger" : materialAddDanger
								},
								success : function(response, options) {
									var resultJSON = Ext.util.JSON
											.decode(response.responseText);
									if (resultJSON.success == true) {
										materialStore.reload({
											params : {
												start : 0,
												limit : materialPageRecordCount
											}
										});

										var dataInfo = resultJSON.data;
										Ext.MessageBox.show({
											msg : dataInfo,
											width : 300,
											buttons : Ext.MessageBox.OK
										});
									} else {
										var dataInfo = resultJSON.data;
										Ext.MessageBox.show({
											msg : dataInfo,
											width : 300,
											buttons : Ext.MessageBox.OK
										});
									}
								},
								failure : function(response, options) {
								}
							});
						} else {
							Ext.MessageBox.show({
								msg : "该食材编号已存在！",
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						}

					}

				}
			}, {
				text : "取消",
				handler : function() {
					materialAddWin.hide();
				}
			} ],
	listeners : {
		"show" : function(thiz) {

			loadAllMaterial();

			materialAddWin.findById("materialAddNumber").setValue("");
			materialAddWin.findById("materialAddNumber").clearInvalid();

			materialAddWin.findById("materialAddName").setValue("");
			materialAddWin.findById("materialAddName").clearInvalid();

			materialAddWin.findById("materialAddWarning").setValue("");
			materialAddWin.findById("materialAddWarning").clearInvalid();

			materialAddWin.findById("materialAddDanger").setValue("");
			materialAddWin.findById("materialAddDanger").clearInvalid();

			var f = Ext.get("materialAddNumber");
			f.focus.defer(100, f); // 为什么这样才可以！？！？

		}
	}
});
// -------------------------------------------------------------------------------------------------------
// --------------------------------------------------------------------------
// 入库统计 出库统计 调拨统计 消耗统计 盘点统计 库存统计 添加食材
var inStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/dishAdd.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "入库统计",
	handler : function(btn) {
		// supplierAddWin.show();
	}
});

var outStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/dishAdd.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "出库统计",
	handler : function(btn) {
		// supplierAddWin.show();
	}
});

var changeStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/dishAdd.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "调拨统计",
	handler : function(btn) {
		// supplierAddWin.show();
	}
});

var costStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/dishAdd.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "消耗统计",
	handler : function(btn) {
		// supplierAddWin.show();
	}
});

var checkStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/dishAdd.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "盘点统计",
	handler : function(btn) {
		// supplierAddWin.show();
	}
});

var inventoryStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/dishAdd.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "库存统计",
	handler : function(btn) {
		// supplierAddWin.show();
	}
});

var materialAddBut = new Ext.ux.ImageButton({
	imgPath : "../../images/dishAdd.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "添加食材",
	handler : function(btn) {
		materialAddWin.show();
	}
});

// --
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : "../../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn) {
		location.href = "InventoryProtal.html?restaurantID=" + restaurantID
				+ "&pin=" + pin;
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

// ----------------- dymatic searchForm -----------------
// combom
var filterTypeData = [ [ "0", "全部" ], [ "1", "编号" ], [ "2", "名称" ],
		[ "3", "库存量" ], [ "4", "价格" ], [ "5", "预警阀值" ], [ "6", "危险阀值" ] ];
var filterTypeComb = new Ext.form.ComboBox({
	fieldLabel : "过滤",
	forceSelection : true,
	width : 100,
	value : "全部",
	id : "filter",
	store : new Ext.data.SimpleStore({
		fields : [ "value", "text" ],
		data : filterTypeData
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

			// ------------------dymatic field-------------------
			var conditionText = new Ext.form.TextField({
				hideLabel : true,
				id : "conditionText",
				allowBlank : false,
				width : 120
			});

			var conditionNumber = new Ext.form.NumberField({
				hideLabel : true,
				id : "conditionNumber",
				allowBlank : false,
				width : 120
			});

			// ------------------remove field-------------------
			if (conditionType == "text") {
				searchForm.remove("conditionText");
			} else if (conditionType == "number") {
				searchForm.remove("conditionNumber");
			}

			// ------------------ add field -------------------
			operatorComb.setDisabled(false);
			// [ "0", "全部" ], [ "1", "编号" ], [ "2", "名称" ],
			// [ "3", "库存量" ], [ "4", "价格" ], [ "5", "预警阀值" ], [ "6", "危险阀值" ]
			if (index == 0) {
				// 全部
				// searchForm.add(conditionText);
				operatorComb.setValue("等于");
				operatorComb.setDisabled(true);
				conditionType = "text";
			} else if (index == 1) {
				// 编号
				searchForm.add(conditionNumber);
				conditionType = "number";
			} else if (index == 2) {
				// 名称
				searchForm.add(conditionText);
				operatorComb.setValue("等于");
				operatorComb.setDisabled(true);
				conditionType = "text";
			} else if (index == 3) {
				// 库存量
				searchForm.add(conditionNumber);
				conditionType = "number";
			} else if (index == 4) {
				// 价格
				searchForm.add(conditionNumber);
				conditionType = "number";
			} else if (index == 5) {
				// 预警阀值
				searchForm.add(conditionNumber);
				conditionType = "number";
			} else if (index == 6) {
				// 危险阀值
				searchForm.add(conditionNumber);
				conditionType = "number";
			}

			materialQueryCondPanel.doLayout();

		}
	}
});

var operatorData = [ [ "1", "等于" ], [ "2", "大于等于" ], [ "3", "小于等于" ] ];
var operatorComb = new Ext.form.ComboBox({
	hideLabel : true,
	forceSelection : true,
	width : 100,
	value : "等于",
	id : "operator",
	disabled : true,
	store : new Ext.data.SimpleStore({
		fields : [ "value", "text" ],
		data : operatorData
	}),
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false
});

var searchForm = new Ext.Panel({
	border : false,
	width : 130,
	id : "searchForm",
	items : [ {
		xtype : "textfield",
		hideLabel : true,
		id : "conditionText",
		allowBlank : false,
		width : 120
	} ]
});

// ----------------- dymatic checkbox Form -----------------

// panel
var materialQueryCondPanel = new Ext.form.FormPanel({
	region : "north",
	// border : false,
	height : 23,
	bodyStyle : "margin-top:5px;",
	items : [ {
		layout : "column",
		border : false,
		anchor : "98%",
		items : [ {
			layout : "form",
			labelWidth : 40,
			border : false,
			labelSeparator : '：',
			width : 150,
			items : filterTypeComb
		}, {
			layout : "form",
			border : false,
			width : 110,
			items : operatorComb
		}, searchForm, {
			layout : 'form',
			border : false,
			width : 70,
			items : [ {
				xtype : "button",
				hideLabel : true,
				id : "srchBtn",
				text : "搜索",
				width : 100,
				listeners : {
					"click" : function(thiz, e) {
						materialStore.reload({
							params : {
								start : 0,
								limit : materialPageRecordCount
							}
						});
					}
				}
			} ]
		} ]
	} ]
});

// operator function
function inHandler(rowIndex) {
	if (!isPrompt) {
		inventoryInWin.show();
		isPrompt = true;
	}
}

function outHandler(rowIndex) {
	if (!isPrompt) {
		inventoryOutWin.show();
		isPrompt = true;
	}
}

function changeHandler(rowIndex) {
	if (!isPrompt) {
		inventoryChangeWin.show();
		isPrompt = true;
	}
}

function checkHandler(rowIndex) {

}

function materialDeleteHandler(rowIndex) {
	Ext.MessageBox.show({
		msg : "确定删除？",
		width : 300,
		buttons : Ext.MessageBox.YESNO,
		fn : function(btn) {
			if (btn == "yes") {
				var materialID = materialStore.getAt(rowIndex)
						.get("materialID");

				Ext.Ajax.request({
					url : "../../DeleteMaterial.do",
					params : {
						"pin" : pin,
						"materialID" : materialID
					},
					success : function(response, options) {
						var resultJSON = Ext.util.JSON
								.decode(response.responseText);
						if (resultJSON.success == true) {
							materialStore.reload({
								params : {
									start : 0,
									limit : materialPageRecordCount
								}
							});

							var dataInfo = resultJSON.data;
							Ext.MessageBox.show({
								msg : dataInfo,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						} else {
							var dataInfo = resultJSON.data;
							Ext.MessageBox.show({
								msg : dataInfo,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						}
					},
					failure : function(response, options) {
					}
				});
			}
		}
	});
};

function materialOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	return "<center>" + "<a href=\"javascript:inHandler(" + rowIndex + ")\">"
			+ "<img src='../../images/del.png'/>入库</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:outHandler(" + rowIndex + ")\">"
			+ "<img src='../../images/del.png'/>出库</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:changeHandler(" + rowIndex + ")\">"
			+ "<img src='../../images/del.png'/>调拨</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:checkHandler(" + rowIndex + ")\">"
			+ "<img src='../../images/del.png'/>盘点</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:materialDeleteHandler(" + rowIndex + ")\">"
			+ "<img src='../../images/del.png'/>删除</a>" + "</center>";
};

// 1，表格的数据store
var materialStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../QueryMaterialMgr.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "materialID"
	}, {
		name : "materialAlias"
	}, {
		name : "materialName"
	}, {
		name : "storage"
	}, {
		name : "price"
	}, {
		name : "warningNbr"
	}, {
		name : "dangerNbr"
	}, {
		name : "operator"
	}, {
		name : "message"
	} ])
});

// menuStore.reload();

// 2，栏位模型
var materialColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "编号",
			sortable : true,
			dataIndex : "materialAlias",
			width : 60
		}, {
			header : "名称",
			sortable : true,
			dataIndex : "materialName",
			width : 80,
			editor : new Ext.form.TextField({
				allowBlank : false,
				allowNegative : false
			})
		}, {
			header : "库存量",
			sortable : true,
			dataIndex : "storage",
			width : 80
		}, {
			header : "价格（￥）",
			sortable : true,
			dataIndex : "price",
			width : 80
		}, {
			header : "预警阀值",
			sortable : true,
			dataIndex : "warningNbr",
			width : 80,
			editor : new Ext.form.NumberField({
				allowBlank : false,
				allowNegative : false
			})
		}, {
			header : "危险阀值",
			sortable : true,
			dataIndex : "dangerNbr",
			width : 80,
			editor : new Ext.form.NumberField({
				allowBlank : false,
				allowNegative : false
			})
		}, {
			header : "<center>操作</center>",
			sortable : true,
			dataIndex : "operator",
			width : 250,
			renderer : materialOpt
		} ]);

// -------------- layout ---------------
var materialGrid;
Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// ---------------------表格--------------------------
			materialGrid = new Ext.grid.EditorGridPanel(
					{
						// title : "供应商",
						xtype : "grid",
						anchor : "99%",
						region : "center",
						frame : true,
						margins : '0 5 0 0',
						ds : materialStore,
						cm : materialColumnModel,
						sm : new Ext.grid.RowSelectionModel({
							singleSelect : true
						}),
						viewConfig : {
							forceFit : true
						},
						listeners : {
							rowclick : function(thiz, rowIndex, e) {
								currRowIndex = rowIndex;
							}
						},
						tbar : [ {
							text : '保存修改',
							tooltip : '保存修改',
							iconCls : 'save',
							handler : function() {
								// 修改記錄格式:id field_separator name
								// field_separator phone field_separator contact
								// field_separator address record_separator id
								// field_separator name field_separator phone
								// field_separator contact field_separator
								// address
								var modfiedArr = [];
								materialGrid
										.getStore()
										.each(
												function(record) {
													if (record
															.isModified("materialName") == true
															|| record
																	.isModified("dangerNbr") == true
															|| record
																	.isModified("warningNbr") == true) {
														modfiedArr
																.push(record
																		.get("materialID")
																		+ " field_separator "
																		+ record
																				.get("materialName")
																		+ " field_separator "
																		+ record
																				.get("warningNbr")
																		+ " field_separator "
																		+ record
																				.get("dangerNbr"));
													}
												});

								if (modfiedArr.length != 0) {
									// 獲取分頁表格的當前頁碼！神技！！！
									var toolbar = materialGrid
											.getBottomToolbar();
									currPageIndex = toolbar.readPage(toolbar
											.getPageData());

									var modMaterials = "";
									for ( var i = 0; i < modfiedArr.length; i++) {
										modMaterials = modMaterials
												+ modfiedArr[i]
												+ " record_separator ";
									}
									modMaterials = modMaterials.substring(0,
											modMaterials.length - 18);

									Ext.Ajax
											.request({
												url : "../../UpdateMaterial.do",
												params : {
													"pin" : pin,
													"modMaterials" : modMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);
													if (resultJSON.success == true) {
														materialStore
																.reload({
																	params : {
																		start : (currPageIndex - 1)
																				* materialPageRecordCount,
																		limit : materialPageRecordCount
																	}
																});

														var dataInfo = resultJSON.data;
														Ext.MessageBox
																.show({
																	msg : dataInfo,
																	width : 300,
																	buttons : Ext.MessageBox.OK
																});
													} else {
														var dataInfo = resultJSON.data;
														Ext.MessageBox
																.show({
																	msg : dataInfo,
																	width : 300,
																	buttons : Ext.MessageBox.OK
																});
													}
												},
												failure : function(response,
														options) {
												}
											});
								}
							}
						} ],
						bbar : new Ext.PagingToolbar({
							pageSize : materialPageRecordCount,
							store : materialStore,
							displayInfo : true,
							displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
							emptyMsg : "没有记录"
						}),
						autoScroll : true,
						loadMask : {
							msg : "数据加载中，请稍等..."
						},
						listeners : {
							"render" : function(thiz) {
								materialStore.reload({
									params : {
										start : 0,
										limit : materialPageRecordCount
									}
								});
							},
							"rowclick" : function(thiz, rowIndex, e) {
								currRowIndex = rowIndex;
							}
						}
					});

			// 为store配置beforeload监听器
			materialGrid.getStore()
					.on(
							'beforeload',
							function() {

								var queryTpye = filterTypeComb.getValue();
								if (queryTpye == "全部") {
									queryTpye = 0;
								}

								var queryOperator = operatorComb.getValue();
								if (queryOperator == "等于") {
									queryOperator = 1;
								}

								var queryValue = "";
								if (conditionType == "text" && queryTpye != 0) {
									queryValue = searchForm.findById(
											"conditionText").getValue();
									if (!searchForm.findById("conditionText")
											.isValid()) {
										return false;
									}
								} else if (conditionType == "number") {
									queryValue = searchForm.findById(
											"conditionNumber").getValue();
									if (!searchForm.findById("conditionNumber")
											.isValid()) {
										return false;
									}
								}

								// 输入查询条件参数
								this.baseParams = {
									"pin" : pin,
									"type" : queryTpye,
									"ope" : queryOperator,
									"value" : queryValue,
									"isPaging" : true
								};

							});

			// 为store配置load监听器(即load完后动作)
			materialGrid.getStore().on('load', function() {
				if (materialGrid.getStore().getTotalCount() != 0) {
					var msg = this.getAt(0).get("message");
					if (msg != "normal") {
						Ext.MessageBox.show({
							msg : msg,
							width : 300,
							buttons : Ext.MessageBox.OK
						});
						this.removeAll();
					} else {
						// materialGrid
						// .getStore()
						// .each(
						// function(record) {
						// // 廚房顯示
						// for ( var i = 0; i < kitchenTypeData.length; i++) {
						// if (record
						// .get("kitchen") == kitchenTypeData[i][0]) {
						// record
						// .set(
						// "kitchenDisplay",
						// kitchenTypeData[i][1]);
						// }
						// }
						//
						// });
					}
				}
			});
			// ---------------------end 表格--------------------------

			var centerPanel = new Ext.Panel({
				region : "center",
				layout : "fit",
				frame : true,
				items : [ {
					layout : "border",
					title : "<div style='font-size:20px;'>库存管理<div>",
					items : [ materialQueryCondPanel, materialGrid ]
				} ],
				tbar : new Ext.Toolbar({
					height : 55,
					items : [ inStatBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, outStatBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, changeStatBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, costStatBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, checkStatBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, inventoryStatBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, materialAddBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, "->", pushBackBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, logOutBut ]
				})
			});

			var viewport = new Ext.Viewport(
					{
						layout : "border",
						id : "viewport",
						items : [
								{
									region : "north",
									bodyStyle : "background-color:#A9D0F5",
									html : "<h4 style='padding:10px;font-size:150%;float:left;'>无线点餐网页终端</h4><div id='optName' class='optName'></div>",
									height : 50,
									margins : '0 0 5 0'
								},
								centerPanel,
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
			// Ext.EventManager.onWindowResize(function() {
			// // obj.style[attr]
			// document.getElementById("wrap").style["height"] =
			// (tableSelectCenterPanel
			// .getInnerHeight() - 100)
			// + "px";
			// });
		});

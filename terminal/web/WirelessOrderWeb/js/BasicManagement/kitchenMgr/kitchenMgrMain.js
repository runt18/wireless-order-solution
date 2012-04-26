// --------------------------------------------------------------------------
var departmentStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../QueryDepartment.do?pin=" + pin
				+ "&isPaging=false&isCombo=true"
	}),
	reader : new Ext.data.JsonReader({
		root : 'root'
	}, [ {
		name : 'deptID',
		mapping : 'deptID'
	}, {
		name : 'deptName',
		mapping : 'deptName'
	} ])
});

var departmentComb = new Ext.form.ComboBox({
	forceSelection : true,
	id : "departmentComb",
	store : departmentStore,
	valueField : "deptID",
	displayField : "deptName",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false
});

// --
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : "../../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn) {
		var isChange = false;
		kitchenGrid.getStore().each(
				function(record) {
					if (record.isModified("kitchenName") == true
							|| record.isModified("normalDiscount1") == true
							|| record.isModified("normalDiscount2") == true
							|| record.isModified("normalDiscount3") == true
							|| record.isModified("memberDiscount1") == true
							|| record.isModified("memberDiscount2") == true
							|| record.isModified("memberDiscount3") == true
							|| record.isModified("department") == true) {

						isChange = true;

					}
				});

		if (isChange) {
			Ext.MessageBox.show({
				msg : "修改尚未保存，是否确认返回？",
				width : 300,
				buttons : Ext.MessageBox.YESNO,
				fn : function(btn) {
					if (btn == "yes") {
						location.href = "BasicMgrProtal.html?restaurantID="
								+ restaurantID + "&pin=" + pin;
					}
				}
			});
		} else {
			location.href = "BasicMgrProtal.html?restaurantID=" + restaurantID
					+ "&pin=" + pin;
		}
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

// 1，表格的数据store
var kitchenStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../QueryKitchenMgr.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "kitchenID"
	}, {
		name : "kitchenAlias"
	}, {
		name : "kitchenName"
	}, {
		name : "normalDiscount1"
	}, {
		name : "normalDiscount2"
	}, {
		name : "normalDiscount3"
	}, {
		name : "memberDiscount1"
	}, {
		name : "memberDiscount2"
	}, {
		name : "memberDiscount3"
	}, {
		name : "department"
	}, {
		name : "message"
	} ])
});

// menuStore.reload();

// 2，栏位模型
var kitchenColumnModel = new Ext.grid.ColumnModel([ new Ext.grid.RowNumberer(),
// {
// header : "编号",
// sortable : true,
// dataIndex : "kitchenID",
// width : 100
// },
{
	header : "名称",
	sortable : true,
	dataIndex : "kitchenName",
	width : 140,
	editor : new Ext.form.TextField({
		allowBlank : false,
		selectOnFocus : true,
		allowNegative : false
	})
}, {
	header : "一般折扣1",
	sortable : true,
	dataIndex : "normalDiscount1",
	width : 140,
	editor : new Ext.form.NumberField({
		allowBlank : false,
		selectOnFocus : true,
		allowNegative : false,
		validator : function(v) {
			if (v <= 0.0 || v > 1) {
				return "折扣范围是0.0至1.0！";
			} else {
				return true;
			}
		}
	})
}, {
	header : "一般折扣2",
	sortable : true,
	dataIndex : "normalDiscount2",
	width : 140,
	editor : new Ext.form.NumberField({
		allowBlank : false,
		selectOnFocus : true,
		allowNegative : false,
		validator : function(v) {
			if (v <= 0.0 || v > 1) {
				return "折扣范围是0.0至1.0！";
			} else {
				return true;
			}
		}
	})
}, {
	header : "一般折扣3",
	sortable : true,
	dataIndex : "normalDiscount3",
	width : 140,
	editor : new Ext.form.NumberField({
		allowBlank : false,
		selectOnFocus : true,
		allowNegative : false,
		validator : function(v) {
			if (v <= 0.0 || v > 1) {
				return "折扣范围是0.0至1.0！";
			} else {
				return true;
			}
		}
	})
}, {
	header : "会员折扣1",
	sortable : true,
	dataIndex : "memberDiscount1",
	width : 140,
	editor : new Ext.form.NumberField({
		allowBlank : false,
		selectOnFocus : true,
		allowNegative : false,
		validator : function(v) {
			if (v <= 0.0 || v > 1) {
				return "折扣范围是0.0至1.0！";
			} else {
				return true;
			}
		}
	})
}, {
	header : "会员折扣2",
	sortable : true,
	dataIndex : "memberDiscount2",
	width : 140,
	editor : new Ext.form.NumberField({
		allowBlank : false,
		selectOnFocus : true,
		allowNegative : false,
		validator : function(v) {
			if (v <= 0.0 || v > 1) {
				return "折扣范围是0.0至1.0！";
			} else {
				return true;
			}
		}
	})
}, {
	header : "会员折扣3",
	sortable : true,
	dataIndex : "memberDiscount3",
	width : 140,
	editor : new Ext.form.NumberField({
		allowBlank : false,
		selectOnFocus : true,
		allowNegative : false,
		validator : function(v) {
			if (v <= 0.0 || v > 1) {
				return "折扣范围是0.0至1.0！";
			} else {
				return true;
			}
		}
	})
}, {
	header : "部门",
	sortable : true,
	dataIndex : "department",
	width : 100,
	editor : departmentComb,
	renderer : function(value, cellmeta, record) {
		var deptDesc = "";
		for ( var i = 0; i < departmentData.length; i++) {
			if (departmentData[i][0] == value) {
				deptDesc = departmentData[i][1];
			}
		}
		return deptDesc;
	}
} ]);

// -------------- layout ---------------
var kitchenGrid;
Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// ---------------------表格--------------------------
			kitchenGrid = new Ext.grid.EditorGridPanel(
					{
						title : "分厨",
						xtype : "grid",
						anchor : "99%",
						region : "center",
						frame : true,
						margins : '0 5 0 0',
						ds : kitchenStore,
						cm : kitchenColumnModel,
						sm : new Ext.grid.RowSelectionModel({
							singleSelect : true
						}),
						// viewConfig : {
						// forceFit : true
						// },
						listeners : {
							"rowclick" : function(thiz, rowIndex, e) {
								currRowIndex = rowIndex;
							}
						},
						tbar : [ {
							text : '保存修改',
							tooltip : '保存修改',
							iconCls : 'save',
							handler : function() {

								// 修改記錄格式:id field_separator name
								// field_separator
								// phone field_separator contact field_separator
								// address
								// record_separator id field_separator name
								// field_separator phone field_separator contact
								// field_separator address
								var modfiedArr = [];
								kitchenGrid
										.getStore()
										.each(
												function(record) {
													if (record
															.isModified("kitchenName") == true
															|| record
																	.isModified("normalDiscount1") == true
															|| record
																	.isModified("normalDiscount2") == true
															|| record
																	.isModified("normalDiscount3") == true
															|| record
																	.isModified("memberDiscount1") == true
															|| record
																	.isModified("memberDiscount2") == true
															|| record
																	.isModified("memberDiscount3") == true
															|| record
																	.isModified("department") == true) {
														modfiedArr
																.push(record
																		.get("kitchenID")
																		+ " field_separator "
																		+ record
																				.get("kitchenName")
																		+ " field_separator "
																		+ record
																				.get("normalDiscount1")
																		+ " field_separator "
																		+ record
																				.get("normalDiscount2")
																		+ " field_separator "
																		+ record
																				.get("normalDiscount3")
																		+ " field_separator "
																		+ record
																				.get("memberDiscount1")
																		+ " field_separator "
																		+ record
																				.get("memberDiscount2")
																		+ " field_separator "
																		+ record
																				.get("memberDiscount3")
																		+ " field_separator "
																		+ record
																				.get("department"));
													}
												});

								if (modfiedArr.length != 0) {
									// 獲取分頁表格的當前頁碼！神技！！！
									var toolbar = kitchenGrid
											.getBottomToolbar();
									currPageIndex = toolbar.readPage(toolbar
											.getPageData());

									var modKitchens = "";
									for ( var i = 0; i < modfiedArr.length; i++) {
										modKitchens = modKitchens
												+ modfiedArr[i]
												+ " record_separator ";
									}
									modKitchens = modKitchens.substring(0,
											modKitchens.length - 18);

									Ext.Ajax
											.request({
												url : "../../UpdateKitchen.do",
												params : {
													"pin" : pin,
													"modKitchens" : modKitchens
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);
													if (resultJSON.success == true) {
														kitchenStore
																.reload({
																	params : {
																		start : (currPageIndex - 1)
																				* kitchenPageRecordCount,
																		limit : kitchenPageRecordCount
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
							pageSize : kitchenPageRecordCount,
							store : kitchenStore,
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
								kitchenStore.reload({
									params : {
										start : 0,
										limit : kitchenPageRecordCount
									}
								});
							}
						}
					});

			// 为store配置beforeload监听器
			kitchenGrid.getStore().on('beforeload', function() {

				// 输入查询条件参数
				this.baseParams = {
					"pin" : pin,
					"isPaging" : true
				};

			});

			// 为store配置load监听器(即load完后动作)
			kitchenGrid.getStore().on('load', function() {
				if (kitchenGrid.getStore().getTotalCount() != 0) {
					var msg = this.getAt(0).get("message");
					if (msg != "normal") {
						Ext.MessageBox.show({
							msg : msg,
							width : 300,
							buttons : Ext.MessageBox.OK
						});
						this.removeAll();
					} else {
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
					title : "<div style='font-size:20px;'>分厨管理<div>",
					items : kitchenGrid
				} ],
				tbar : new Ext.Toolbar({
					height : 55,
					items : [ "->", pushBackBut, {
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

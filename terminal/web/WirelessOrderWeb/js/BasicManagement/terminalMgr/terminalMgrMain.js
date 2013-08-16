// ----------------- 設定 --------------------
//settingWin = new Ext.Window(
//		{
//			layout : "fit",
//			title : "设定",
//			width : 245,
//			height : 145,
//			closeAction : "hide",
//			resizable : false,
//			items : [ {
//				layout : "form",
//				labelWidth : 60,
//				border : false,
//				frame : true,
//				items : [ {
//					xtype : "textfield",
//					fieldLabel : "持有人",
//					id : "ownerSet",
//					allowBlank : false,
//					width : 150
//				}, {
//					xtype : "numberfield",
//					fieldLabel : "赠送额度",
//					id : "giftQuotaSet",
//					allowBlank : false,
//					width : 150,
//					// minValue : 0,
//					// maxValue : 99999.99
//					validator : function(v) {
//						if (v < 0.00 || v > 99999.99) {
//							return "赠送额度范围是0.00至99999.99！";
//						} else {
//							return true;
//						}
//					}
//				}, {
//					xtype : "checkbox",
//					id : "noQuotaLimitSet",
//					fieldLabel : "无限制",
//					listeners : {
//						"check" : function(thiz, checked) {
//							if (checked) {
//								settingWin.findById("giftQuotaSet").disable();
//							} else {
//								settingWin.findById("giftQuotaSet").enable();
//							}
//						}
//					}
//				} ]
//			} ],
//			buttons : [
//					{
//						text : "确定",
//						handler : function() {
//							if (settingWin.findById("giftQuotaSet").isValid()
//									&& settingWin.findById("ownerSet")
//											.isValid()) {
//								// 獲取分頁表格的當前頁碼！神技！！！
//								var toolbar = terminalGrid.getBottomToolbar();
//								currPageIndex = toolbar.readPage(toolbar
//										.getPageData());
//
//								isPrompt = false;
//								settingWin.hide();
//
//								var giftQuota = settingWin.findById(
//										"giftQuotaSet").getValue();
//								var quotaUnlimit = settingWin.findById(
//										"noQuotaLimitSet").getValue();
//								if (quotaUnlimit == true) {
//									giftQuota = -1;
//								}
//								var staff = settingWin.findById("ownerSet")
//										.getValue();
//								var terminalID = terminalStore.getAt(
//										currRowIndex).get("terminalID");
//
//								Ext.Ajax.request({
//									url : "../../SetTerminal.do",
//									params : {
//										
//										"terminalID" : terminalID,
//										"staff" : staff,
//										"giftQuota" : giftQuota
//									},
//									success : function(response, options) {
//										var resultJSON = Ext.util.JSON
//												.decode(response.responseText);
//										if (resultJSON.success == true) {
//											terminalStore.reload({
//												params : {
//													start : (currPageIndex - 1)
//															* pageRecordCount,
//													limit : pageRecordCount
//												}
//											});
//
//											var dataInfo = resultJSON.data;
//											Ext.MessageBox.show({
//												msg : dataInfo,
//												width : 300,
//												buttons : Ext.MessageBox.OK
//											});
//										} else {
//											var dataInfo = resultJSON.data;
//											Ext.MessageBox.show({
//												msg : dataInfo,
//												width : 300,
//												buttons : Ext.MessageBox.OK
//											});
//										}
//									},
//									failure : function(response, options) {
//									}
//								});
//							}
//						}
//					}, {
//						text : "取消",
//						handler : function() {
//							isPrompt = false;
//							settingWin.hide();
//						}
//					} ],
//			listeners : {
//				"show" : function(thiz) {
//					settingWin.findById("ownerSet").setValue(
//							terminalStore.getAt(currRowIndex).get("ownerName"));
//					settingWin.findById("ownerSet").clearInvalid();
//
//					var quota = terminalStore.getAt(currRowIndex).get(
//							"giftQuota");
//					if (quota < 0) {
//						settingWin.findById("giftQuotaSet").setValue(0);
//						settingWin.findById("giftQuotaSet").disable();
//						settingWin.findById("noQuotaLimitSet").setValue(true);
//					} else {
//						settingWin.findById("giftQuotaSet").setValue(
//								terminalStore.getAt(currRowIndex).get(
//										"giftQuota"));
//						settingWin.findById("giftQuotaSet").enable();
//						settingWin.findById("noQuotaLimitSet").setValue(false);
//					}
//					settingWin.findById("giftQuotaSet").clearInvalid();
//
//					var f = Ext.get("ownerSet");
//					f.focus.defer(100, f); // 为什么这样才可以！？！？
//				},
//				"hide" : function(thiz) {
//					isPrompt = false;
//				}
//			}
//		});

// --
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : "../../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn) {
		location.href = "BasicMgrProtal.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
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

var termSetHandler = function(rowIndex) {
	if (!isPrompt) {
		isPrompt = true;
		settingWin.show();
	}
};

// function terminalOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
// return "<center><a href=\"javascript:termSetHandler(" + rowIndex + ")\">"
// + "<img src='../../images/Modify.png'/>设定</a>" + "</center>";
// };

// 1，表格的数据store
var terminalStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../QueryTerminal.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "terminalID"
	}, {
		name : "pin"
	}, {
		name : "modelID"
	}, {
		name : "modelName"
	}, {
		name : "ownerName"
	}, {
		name : "expireDate"
	}, {
		name : "giftAmount"
	}, {
		name : "giftQuota"
	}, {
		name : "quotaOrig"
	}, {
		name : "noLimit"
	}, {
		name : "operator"
	}, {
		name : "message"
	} ])
});

// menuStore.reload();

var noLimitCheckColumn = new Ext.grid.CheckColumn({
	header : "无额度限制",
	dataIndex : "noLimit",
	width : 30
});

// 2，栏位模型
var terminalColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "机型",
			sortable : true,
			dataIndex : "modelName",
			width : 100
		}, {
			header : "持有人",
			sortable : true,
			dataIndex : "ownerName",
			width : 100,
			editor : new Ext.form.TextField({
				// allowBlank : false,
				allowNegative : false,
				selectOnFocus : true
			})
		}, {
			header : "PIN",
			sortable : true,
			dataIndex : "pin",
			width : 100,
			renderer : function(v, params, record) {
				return "0x" + (v.toString(16) + "").toUpperCase();
			}
		}, {
			header : "有效期",
			sortable : true,
			dataIndex : "expireDate",
			width : 100
		}, {
			header : "已赠送（￥）",
			sortable : true,
			dataIndex : "giftAmount",
			width : 100
		}, {
			header : "赠送额度（￥）",
			sortable : true,
			dataIndex : "giftQuota",
			width : 100,
			editor : new Ext.form.NumberField({
				allowBlank : false,
				selectOnFocus : true,
				validator : function(v) {
					if (v < 0.00 || v > 99999.99) {
						return "赠送额度范围是0.00至99999.99！";
					} else {
						return true;
					}
				}
			}),
			renderer : function(v, params, record) {
				if (v < 0) {
					return "无限制";
				} else {
					return v;
				}
			}
		}, noLimitCheckColumn
// {
// header : "<center>操作</center>",
// sortable : true,
// dataIndex : "operator",
// width : 100,
// renderer : terminalOpt
// }
]);

// -------------- layout ---------------
var terminalGrid;
Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// ---------------------表格--------------------------
			terminalGrid = new Ext.grid.EditorGridPanel(
					{
						xtype : "grid",
						anchor : "99%",
						region : "center",
						frame : true,
						margins : '0 5 0 0',
						ds : terminalStore,
						cm : terminalColumnModel,
						plugins : noLimitCheckColumn,
						clicksToEdit : 2,
						sm : new Ext.grid.RowSelectionModel({
							singleSelect : true
						}),
						viewConfig : {
							forceFit : true
						},
						bbar : new Ext.PagingToolbar({
							pageSize : pageRecordCount,
							store : terminalStore,
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
								terminalStore.reload({
									params : {
										start : 0,
										limit : pageRecordCount
									}
								});
							},
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
								// field_separator phone field_separator contact
								// field_separator address record_separator id
								// field_separator name field_separator phone
								// field_separator contact field_separator
								// address
								var modfiedArr = [];
								terminalGrid
										.getStore()
										.each(
												function(record) {
													if (record
															.isModified("ownerName") == true
															|| record
																	.isModified("giftQuota") == true) {
														modfiedArr
																.push(record
																		.get("terminalID")
																		+ " field_separator "
																		+ record
																				.get("ownerName")
																		+ " field_separator "
																		+ record
																				.get("giftQuota"));
													}
												});

								if (modfiedArr.length != 0) {
									// 獲取分頁表格的當前頁碼！神技！！！
									var toolbar = terminalGrid
											.getBottomToolbar();
									currPageIndex = toolbar.readPage(toolbar
											.getPageData());

									var modTernimials = "";
									for ( var i = 0; i < modfiedArr.length; i++) {
										modTernimials = modTernimials
												+ modfiedArr[i]
												+ " record_separator ";
									}
									modTernimials = modTernimials.substring(0,
											modTernimials.length - 18);

									Ext.Ajax
											.request({
												url : "../../SetTerminal.do",
												params : {
													
													"modTernimials" : modTernimials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);
													if (resultJSON.success == true) {
														// loadAllTaste();
														terminalStore
																.reload({
																	params : {
																		start : (currPageIndex - 1)
																				* pageRecordCount,
																		limit : pageRecordCount
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
					});

			// 为store配置beforeload监听器
			terminalGrid.getStore().on('beforeload', function() {

				// 输入查询条件参数
				this.baseParams = {
					
					"isPaging" : true,
					"isCombo" : false
				};

			});

			// 为store配置load监听器(即load完后动作)
			terminalGrid.getStore().on('load', function() {
				if (terminalGrid.getStore().getTotalCount() != 0) {
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

			terminalGrid.on("beforeedit", function(e) {
				if (e.record.get("noLimit") == true && e.field == "giftQuota") {
					e.cancel = true;
				}
			});

			terminalGrid.on("afteredit",
					function(e) {
						if (e.field == "noLimit") {
							if (e.record.get("noLimit") == true) {
								e.record.set("giftQuota", -1);
							} else {
								if (e.record.get("quotaOrig") > 0) {
									e.record.set("giftQuota", e.record
											.get("quotaOrig"));
								} else {
									e.record.set("giftQuota", 0);
								}
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
					title : "<div style='font-size:20px;'>终端管理<div>",
					items : terminalGrid
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
									bodyStyle : "background-color:#DFE8F6;",
									html : "<h4 style='padding:10px;font-size:150%;float:left;'>无线点餐网页终端</h4><div id='optName' class='optName'></div>",
									height : 50,
									border : false,
									margins : '0 0 0 0'
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

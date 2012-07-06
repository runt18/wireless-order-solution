// 条件框

var analysisCurrFrom = new Ext.form.FormPanel({
	border : false,
	anchor : "right 13%",
	id : "analysisCurrFrom",
	items : [ {
		layout : "column",
		border : false,
		frame : true,
		items : [ {
			layout : "form",
			border : false,
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "当前库存",
				checked : true,
				name : 'analysisCurr',
				inputValue : 'current'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "出入库汇总",
				// checked : true,
				name : 'analysisCurr',
				inputValue : 'inOutSum'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 125,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "库存分析",
				// checked : true,
				name : 'analysisCurr',
				inputValue : 'analysis'
			} ]
		} ]
	} ]
});

inventoryAnalysisWin = new Ext.Window(
		{
			title : "库存分析",
			width : 450,
			height : 310,
			closeAction : 'hide',
			resizable : false,
			closable : false,
			layout : "anchor",
			items : [ {
				border : false,
				anchor : "right 15%",
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
							id : "begDateInvenAnalysis",
							width : 120,
							fieldLabel : "日期",
							format : 'Y-m-d',
							readOnly : true,
							listeners : {
								blur : function(){									
									Ext.ux.checkDateForBeginAndEnd(true, 'begDateInvenAnalysis', 'endDateInvenAnalysis');
								}
							}
						} ]
					}, {
						layout : "form",
						border : false,
						labelSeparator : ' ',
						width : 200,
						labelWidth : 50,
						items : [ {
							xtype : "datefield",
							id : "endDateInvenAnalysis",
							width : 120,
							fieldLabel : "至",
							format : 'Y-m-d',
							readOnly : true,
							listeners : {
								blur : function(){									
									Ext.ux.checkDateForBeginAndEnd(false, 'begDateInvenAnalysis', 'endDateInvenAnalysis');
								}
							}
						} ]
					} ]
				} ]
			}, {
				layout : "column",
				title : "食材种类",
				border : false,
				anchor : "100% 37%",
				autoScroll : true,
				frame : true,
				items : [ {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate1InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate2InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate3InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate4InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate5InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate6InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate7InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate8InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate9InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate10InvenAna"
					} ]
				} ]
			}, {
				layout : "column",
				title : "部门",
				border : false,
				anchor : "100% 37%",
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
						id : "dept1InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept2InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept3InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept4InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept5InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept6InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept7InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept8InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept9InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept10InvenAna"
					} ]
				} ]
			}, analysisCurrFrom ],
			buttons : [
					{
						text : "确定",
						handler : function() {
							
							Ext.ux.checkDateForBeginAndEnd(true, 'begDateInvenAnalysis', 'endDateInvenAnalysis');
							Ext.ux.checkDateForBeginAndEnd(false, 'begDateInvenAnalysis', 'endDateInvenAnalysis');
							
							isPrompt = false;
							inventoryAnalysisWin.hide();							
							
							
							
							// -- 獲取時間 --
							var beginDate = inventoryAnalysisWin.findById("begDateInvenAnalysis").getValue();
							if (beginDate != "") {
								var dateFormated = new Date();
								dateFormated = beginDate;
								beginDate = dateFormated.format('Y-m-d');
							}
							
							var endDate = inventoryAnalysisWin.findById("endDateInvenAnalysis").getValue();
							if (endDate != "") {
								var dateFormated = new Date();
								dateFormated = endDate;
								endDate = dateFormated.format('Y-m-d');
							}

							// -- 獲取食材種類--
							var materialCates = "";
							if (inventoryAnalysisWin.findById("cate1InvenAna").getValue() == true) {
								materialCates = materialCates + materialCateComboData[0][0] + ",";
							}
							if (inventoryAnalysisWin.findById("cate2InvenAna").getValue() == true) {
								materialCates = materialCates + materialCateComboData[1][0] + ",";
							}
							if (inventoryAnalysisWin.findById("cate3InvenAna").getValue() == true) {
								materialCates = materialCates + materialCateComboData[2][0] + ",";
							}
							if (inventoryAnalysisWin.findById("cate4InvenAna").getValue() == true) {
								materialCates = materialCates + materialCateComboData[3][0] + ",";
							}
							if (inventoryAnalysisWin.findById("cate5InvenAna").getValue() == true) {
								materialCates = materialCates + materialCateComboData[4][0] + ",";
							}
							if (inventoryAnalysisWin.findById("cate6InvenAna").getValue() == true) {
								materialCates = materialCates + materialCateComboData[5][0] + ",";
							}
							if (inventoryAnalysisWin.findById("cate7InvenAna").getValue() == true) {
								materialCates = materialCates + materialCateComboData[6][0] + ",";
							}
							if (inventoryAnalysisWin.findById("cate8InvenAna").getValue() == true) {
								materialCates = materialCates + materialCateComboData[7][0] + ",";
							}
							if (inventoryAnalysisWin.findById("cate9InvenAna").getValue() == true) {
								materialCates = materialCates + materialCateComboData[8][0] + ",";
							}
							if (inventoryAnalysisWin.findById("cate10InvenAna").getValue() == true) {
								materialCates = materialCates + materialCateComboData[9][0] + ",";
							}

							if (materialCates != "") {
								materialCates = materialCates.substring(0, materialCates.length - 1);
							}

							// -- 獲取部門 --
							var departments = "";
							var departmentsName = '';
							if (inventoryAnalysisWin.findById("dept1InvenAna").getValue() == true) {
								departments = departments + "0,";
								departmentsName += '仓管部,';
							}
							if (inventoryAnalysisWin.findById("dept2InvenAna").getValue() == true) {
								departments = departments + "1,";
								departmentsName += '中厨部,';
							}
							if (inventoryAnalysisWin.findById("dept3InvenAna").getValue() == true) {
								departments = departments + "2,";
								departmentsName += '名档,';
							}
							if (inventoryAnalysisWin.findById("dept4InvenAna").getValue() == true) {
								departments = departments + "3,";
								departmentsName += '烟酒,';
							}
							if (inventoryAnalysisWin.findById("dept5InvenAna").getValue() == true) {
								departments = departments + "4,";
								departmentsName += '部门4,';
							}
							if (inventoryAnalysisWin.findById("dept6InvenAna").getValue() == true) {
								departments = departments + "5,";
								departmentsName += '部门5,';
							}
							if (inventoryAnalysisWin.findById("dept7InvenAna").getValue() == true) {
								departments = departments + "6,";
								departmentsName += '部门6,';
							}
							if (inventoryAnalysisWin.findById("dept8InvenAna").getValue() == true) {
								departments = departments + "7,";
								departmentsName += '部门7,';
							}
							if (inventoryAnalysisWin.findById("dept9InvenAna").getValue() == true) {
								departments = departments + "8,";
								departmentsName += '部门8,';
							}
							if (inventoryAnalysisWin.findById("dept10InvenAna").getValue() == true) {
								departments = departments + "9,";
								departmentsName += '部门9,';
							}

							if (departments != "") {
								departments = departments.substring(0, departments.length - 1);
							}
							departmentsName = departmentsName != '' ?   departmentsName.substring(0, departmentsName.length - 1) : '全部部门';
							
							var winTitleForDept = '{0}-<font color="red">{1}</font>';
							var winTitleForDate = '&nbsp;&nbsp;&nbsp;&nbsp;{0}&nbsp;至&nbsp;{1}';
							var winTitleDate = String.format(winTitleForDate, beginDate, endDate);
							// -- 獲取統計類型 --
							var staticType = analysisCurrFrom.getForm().findField("analysisCurr").getGroupValue();
							
							if (staticType == "current") {
								isPrompt = true;
								inventAnaCurrResultWin.show();
								if(beginDate != '' && endDate != ''){
									inventAnaCurrResultWin.setTitle(
										String.format(winTitleForDept, '当前库存', departmentsName)+
										winTitleDate
									);
								}else{
									inventAnaCurrResultWin.setTitle(String.format(winTitleForDept,'当前库存',departmentsName));
								}
								var tpSumPrice = 0.00, tpAmount = 0;
								// ＃＃＃＃＃＃＃＃＃＃＃＃當前庫存＃＃＃＃＃＃＃＃＃＃＃＃
								Ext.Ajax.request({
											url : "../../InventoryAnalysisCurrent.do",
											params : {
												"pin" : pin,
												"beginDate" : beginDate,
												"endDate" : endDate,
												"materialCates" : materialCates,
												"departments" : departments
											},
											success : function(response, options) {
												
												var resultJSON = Ext.util.JSON.decode(response.responseText);

												var rootData = resultJSON.root;
//												alert(response.responseText);
												if (rootData[0].message == "normal") {
													currInventoryResultData = rootData.slice(0);
													currInvenAnaGrid.getStore().loadData(currInventoryResultData);
													
													for(var i = 0; i < currInvenAnaGrid.getStore().data.length; i++){
														tpSumPrice += (parseFloat(currInventoryResultData[i].sumPrice));
														tpAmount += parseInt(currInventoryResultData[i].amount);
													}													
													if (rootData[0].materialCateID == "NO_DATA") {
														currInvenAnaGrid.getStore().removeAll();
													}													
												} else {
													Ext.MessageBox.show({
																msg : rootData[0].message,
																width : 300,
																buttons : Ext.MessageBox.OK
															});
													
												}
												Ext.getDom('txtCurrInvenAnaDisplayAmount').innerHTML = tpAmount;
												Ext.getDom('txtCurrInvenAnaDisplaySumPrice').innerHTML = tpSumPrice.toFixed(2);
											},
											failure : function(response,options) {
												Ext.getDom('txtCurrInvenAnaDisplayAmount').innerHTML = tpAmount;
												Ext.getDom('txtCurrInvenAnaDisplaySumPrice').innerHTML = tpSumPrice.toFixed(2);
												Ext.MessageBox.show({
															msg : " Unknown page error ",
															width : 300,
															buttons : Ext.MessageBox.OK
														});
											}
										});

							} else if (staticType == "inOutSum") {
								isPrompt = true;
								invenAnaInOutSumResultWin.show();
								if(beginDate != '' && endDate != ''){
									invenAnaInOutSumResultWin.setTitle(
										String.format(winTitleForDept, '出入库统计', departmentsName)+
										winTitleDate
									);
								}else{
									invenAnaInOutSumResultWin.setTitle(String.format(winTitleForDept, '出入库统计', departmentsName));
								}
								
								var tpInCount=0, tpInTotalPrice=0.00, tpReturnCount=0, tpReturnTotalPrice=0.00, tpOutCount=0, tpOutPrice=0.00, tpCostCount=0, tpTotalPrice=0.00;
								// ＃＃＃＃＃＃＃＃＃＃＃＃出入庫統計＃＃＃＃＃＃＃＃＃＃＃＃
								Ext.Ajax.request({
											url : "../../InventoryAnalysisInOutSum.do",
											params : {
												"pin" : pin,
												"beginDate" : beginDate,
												"endDate" : endDate,
												"materialCates" : materialCates,
												"departments" : departments
											},
											success : function(response, options) {
												var resultJSON = Ext.util.JSON.decode(response.responseText);
//												alert(response.responseText);
												var rootData = resultJSON.root;
												if (rootData[0].message == "normal") {

													inOutSumResultData = rootData.slice(0);
													var itemsData = inOutSumGrid.getStore();
													itemsData.loadData(inOutSumResultData);
													if (rootData[0].materialCateID == "NO_DATA") {
														itemsData.removeAll();
													}
													
													for(var i = 0; i < itemsData.data.length; i++){
//														tpInCount += parseInt(itemsData.getAt(i).get('inCount'));
														tpInTotalPrice += parseFloat(itemsData.getAt(i).get('inTotalPrice'));
//														tpReturnCount += parseInt(itemsData.getAt(i).get('returnCount'));
														tpReturnTotalPrice += parseFloat(itemsData.getAt(i).get('returnTotalPrice'));
//														tpOutCount += parseInt(itemsData.getAt(i).get('outCount'));
														tpOutPrice += parseFloat(itemsData.getAt(i).get('outPrice'));
//														tpCostCount += parseInt(itemsData.getAt(i).get('costCount'));
														tpTotalPrice += parseFloat(itemsData.getAt(i).get('costTotalPrice'));
													};
												} else {
													Ext.MessageBox.show({
																msg : rootData[0].message,
																width : 300,
																buttons : Ext.MessageBox.OK
															});
												}
//												Ext.getDom('txtInOutDisplayInCount').innerHTML = tpInCount;
												Ext.getDom('txtInOutDisplayInTotalPrice').innerHTML = tpInTotalPrice.toFixed(2);
//												Ext.getDom('txtInOutDisplayReturnCount').innerHTML = tpReturnCount;
												Ext.getDom('txtInOutDisplayReturnTotalPrice').innerHTML = tpReturnTotalPrice.toFixed(2);
//												Ext.getDom('txtInOutDisplayOutCount').innerHTML = tpOutCount;
												Ext.getDom('txtInOutDisplayOutPrice').innerHTML = tpOutPrice.toFixed(2);
//												Ext.getDom('txtInOutDisplayCostCount').innerHTML = tpCostCount;
												Ext.getDom('txtInOutDisplayCostTotalPrice').innerHTML = tpTotalPrice.toFixed(2);																	
												
											},
											failure : function(response, options) {
//												Ext.getDom('txtInOutDisplayInCount').innerHTML = tpInCount;
												Ext.getDom('txtInOutDisplayInTotalPrice').innerHTML = tpInTotalPrice.toFixed(2);
//												Ext.getDom('txtInOutDisplayReturnCount').innerHTML = tpReturnCount;
												Ext.getDom('txtInOutDisplayReturnTotalPrice').innerHTML = tpReturnTotalPrice.toFixed(2);
//												Ext.getDom('txtInOutDisplayOutCount').innerHTML = tpOutCount;
												Ext.getDom('txtInOutDisplayOutPrice').innerHTML = tpOutPrice.toFixed(2);
//												Ext.getDom('txtInOutDisplayCostCount').innerHTML = tpCostCount;
												Ext.getDom('txtInOutDisplayCostTotalPrice').innerHTML = tpTotalPrice.toFixed(2);	
												Ext.MessageBox.show({
															msg : " Unknown page error ",
															width : 300,
															buttons : Ext.MessageBox.OK
														});
											}
										});

							} else if (staticType == "analysis") {
								isPrompt = true;
								inventAnalysisResultWin.show();
								if(beginDate != '' && endDate != ''){
									inventAnalysisResultWin.setTitle(
										String.format(winTitleForDept, '库存分析', departmentsName) +
										winTitleDate										
									);
								}else{
									inventAnalysisResultWin.setTitle(String.format(winTitleForDept, '库存分析', departmentsName));
								}
								
								var tpInCount=0, tpCountEnd=0, tpTotalPrice=0.00, tpCountBegin=0, tpReturnCount=0,
									tpOutCount=0, tpLostCount=0, tpCostCount=0, tpChangeInCount=0, tpChangeOutCount=0, tpCheckCount=0;
								// ＃＃＃＃＃＃＃＃＃＃＃＃庫存分析＃＃＃＃＃＃＃＃＃＃＃＃
								Ext.Ajax.request({
											url : "../../InventoryAnalysis.do",
											params : {
												"pin" : pin,
												"beginDate" : beginDate,
												"endDate" : endDate,
												"materialCates" : materialCates,
												"departments" : departments
											},
											success : function(response, options) {
												var resultJSON = Ext.util.JSON.decode(response.responseText);

												var rootData = resultJSON.root;
												if (rootData[0].message == "normal") {
													var tpStore = inventoryAnalysisGrid.getStore();
													invenAnaResultData = rootData.slice(0);
													tpStore.loadData(invenAnaResultData);
													if (rootData[0].materialCateID == "NO_DATA") {
														tpStore.removeAll();
													}
													for(var i = 0; i < tpStore.data.getCount(); i++){
//														tpCountBegin += parseInt(invenAnaResultData[i].countBegin);
//														tpInCount += parseInt(invenAnaResultData[i].inCount);
//														tpReturnCount += parseInt(invenAnaResultData[i].returnCount);
//														tpOutCount += parseInt(invenAnaResultData[i].outCount);
//														tpLostCount += parseInt(invenAnaResultData[i].lostCount);
//														tpCostCount += parseInt(invenAnaResultData[i].costCount);
//														tpChangeInCount += parseInt(invenAnaResultData[i].changeInCount);
//														tpChangeOutCount += parseInt(invenAnaResultData[i].changeOutCount);
//														tpCheckCount += parseInt(invenAnaResultData[i].checkCount);
//														tpCountEnd += parseInt(invenAnaResultData[i].countEnd);
														tpTotalPrice += parseFloat(invenAnaResultData[i].totalPrice);
													}
												} else {
													Ext.MessageBox.show({
																msg : rootData[0].message,
																width : 300,
																buttons : Ext.MessageBox.OK
															});
												}
//												Ext.getDom('txtIADisplayCountBegin').innerHTML = tpCountBegin;
//												Ext.getDom('txtIADisplayInCount').innerHTML = tpInCount;
//												Ext.getDom('txtIADisplayReturnCount').innerHTML = tpReturnCount;
//												Ext.getDom('txtIADisplayOutCount').innerHTML = tpOutCount;
//												Ext.getDom('txtIADisplayLostCount').innerHTML = tpLostCount;
//												Ext.getDom('txtIADisplayCostCount').innerHTML = tpCostCount;
//												Ext.getDom('txtIADisplayChangeInCount').innerHTML = tpChangeInCount;
//												Ext.getDom('txtIADisplayChangeOutCount').innerHTML = tpChangeOutCount;
//												Ext.getDom('txtIADisplayCheckCount').innerHTML = tpCheckCount;
//												Ext.getDom('txtIADisplayCountEnd').innerHTML = tpCountEnd;
												Ext.getDom('txtIADisplayTotalPrice').innerHTML = tpTotalPrice.toFixed(2);
												
													
													
//												Ext.getDom('').innerHTML = '';
											},
											failure : function(response, options) {
												Ext.getDom('txtIADisplayTotalPrice').innerHTML = tpTotalPrice.toFixed(2);
												Ext.MessageBox.show({
															msg : " Unknown page error ",
															width : 300,
															buttons : Ext.MessageBox.OK
														});
											}
										});

							}
						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							inventoryAnalysisWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					inventoryAnalysisWin.findById("begDateInvenAnalysis").setValue("");
					inventoryAnalysisWin.findById("endDateInvenAnalysis").setValue("");

					// 神技！動態改變form中component的label！！！
					inventoryAnalysisWin.findById("cate1InvenAna").el.parent().parent().parent().first().dom.innerHTML = materialCateComboData[0][1] + ":";
					inventoryAnalysisWin.findById("cate2InvenAna").el.parent().parent().parent().first().dom.innerHTML = materialCateComboData[1][1] + ":";
					inventoryAnalysisWin.findById("cate3InvenAna").el.parent().parent().parent().first().dom.innerHTML = materialCateComboData[2][1] + ":";
					inventoryAnalysisWin.findById("cate4InvenAna").el.parent().parent().parent().first().dom.innerHTML = materialCateComboData[3][1] + ":";
					inventoryAnalysisWin.findById("cate5InvenAna").el.parent().parent().parent().first().dom.innerHTML = materialCateComboData[4][1] + ":";
					inventoryAnalysisWin.findById("cate6InvenAna").el.parent().parent().parent().first().dom.innerHTML = materialCateComboData[5][1] + ":";
					inventoryAnalysisWin.findById("cate7InvenAna").el.parent().parent().parent().first().dom.innerHTML = materialCateComboData[6][1] + ":";
					inventoryAnalysisWin.findById("cate8InvenAna").el.parent().parent().parent().first().dom.innerHTML = materialCateComboData[7][1]+ ":";
					inventoryAnalysisWin.findById("cate9InvenAna").el.parent().parent().parent().first().dom.innerHTML = materialCateComboData[8][1] + ":";
					inventoryAnalysisWin.findById("cate10InvenAna").el.parent().parent().parent().first().dom.innerHTML = materialCateComboData[9][1] + ":";

					inventoryAnalysisWin.findById("cate1InvenAna").setValue(false);
					inventoryAnalysisWin.findById("cate2InvenAna").setValue(false);
					inventoryAnalysisWin.findById("cate3InvenAna").setValue(false);
					inventoryAnalysisWin.findById("cate4InvenAna").setValue(false);
					inventoryAnalysisWin.findById("cate5InvenAna").setValue(false);
					inventoryAnalysisWin.findById("cate6InvenAna").setValue(false);
					inventoryAnalysisWin.findById("cate7InvenAna").setValue(false);
					inventoryAnalysisWin.findById("cate8InvenAna").setValue(false);
					inventoryAnalysisWin.findById("cate9InvenAna").setValue(false);
					inventoryAnalysisWin.findById("cate10InvenAna").setValue(false);

					inventoryAnalysisWin.findById("dept1InvenAna").el.parent().parent().parent().first().dom.innerHTML = departmentData[0][1] + ":";
					inventoryAnalysisWin.findById("dept2InvenAna").el.parent().parent().parent().first().dom.innerHTML = departmentData[1][1] + ":";
					inventoryAnalysisWin.findById("dept3InvenAna").el.parent().parent().parent().first().dom.innerHTML = departmentData[2][1] + ":";
					inventoryAnalysisWin.findById("dept4InvenAna").el.parent().parent().parent().first().dom.innerHTML = departmentData[3][1] + ":";
					inventoryAnalysisWin.findById("dept5InvenAna").el.parent().parent().parent().first().dom.innerHTML = departmentData[4][1] + ":";
					inventoryAnalysisWin.findById("dept6InvenAna").el.parent().parent().parent().first().dom.innerHTML = departmentData[5][1] + ":";
					inventoryAnalysisWin.findById("dept7InvenAna").el.parent().parent().parent().first().dom.innerHTML = departmentData[6][1] + ":";
					inventoryAnalysisWin.findById("dept8InvenAna").el.parent().parent().parent().first().dom.innerHTML = departmentData[7][1] + ":";
					inventoryAnalysisWin.findById("dept9InvenAna").el.parent().parent().parent().first().dom.innerHTML = departmentData[8][1] + ":";
					inventoryAnalysisWin.findById("dept10InvenAna").el.parent().parent().parent().first().dom.innerHTML = departmentData[9][1] + ":";

					inventoryAnalysisWin.findById("dept1InvenAna").setValue(false);
					inventoryAnalysisWin.findById("dept2InvenAna").setValue(false);
					inventoryAnalysisWin.findById("dept3InvenAna").setValue(false);
					inventoryAnalysisWin.findById("dept4InvenAna").setValue(false);
					inventoryAnalysisWin.findById("dept5InvenAna").setValue(false);
					inventoryAnalysisWin.findById("dept6InvenAna").setValue(false);
					inventoryAnalysisWin.findById("dept7InvenAna").setValue(false);
					inventoryAnalysisWin.findById("dept8InvenAna").setValue(false);
					inventoryAnalysisWin.findById("dept9InvenAna").setValue(false);
					inventoryAnalysisWin.findById("dept10InvenAna").setValue(false);

					analysisCurrFrom.getForm().findField("analysisCurr").setValue("current");

				},
				"hide" : function(thiz) {
					isPrompt = false;
				}
			}
		});

// 结果框 -- 當前庫存
// 前台：[食材种类 食材 价格 库存量 小计]
// --------------------------------------------------------------------------------------------------------
var currInvenAnaReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'materialCateID',
		type : 'int'
	}, {
		name : 'materialCateName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'singlePrice',
		type : 'float'
	}, {
		name : 'amount',
		type : 'int'
	}, {
		name : 'sumPrice',
		type : 'float'
	}, {
		name : 'materialID'
	}, {
		name : 'materialName',
		type : 'string'
	} ]

});

// define a custom summary function
// Ext.grid.GroupSummary.Calculations['sumPrice'] = function(v, record, field) {
// // return parseFloat(
// // (parseFloat(v).toFixed(2) + (record.data.singlePrice * record.data.amount)
// // .toFixed(2))).toFixed(2);
// return parseFloat(v).toFixed(2) + record.data.sumPrice;
// };

var currInvenAnaSummary = new Ext.grid.GroupSummary();

var currInvenAnaGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : currInvenAnaReader,
		data : [],
		sortInfo : {
			field : 'materialName',
			direction : "ASC"
		},
		groupField : 'materialCateName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "食材种类",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		hideable : false,
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'materialCateName'
	}, {
		header : "食材",
		width : 25,
		sortable : true,
		dataIndex : 'materialName'
	}, {
		id : 'singlePrice',
		header : "价格",
		width : 20,
		sortable : true,
		groupable : false,
		dataIndex : 'singlePrice'
//	 ,
//	 summaryType : 'sum'
	}, {
		header : "数量",
		width : 20,
		sortable : true,
		dataIndex : 'amount',
		summaryType : 'sum'
	}, {
		header : "小计",
		width : 20,
		sortable : false,
		dataIndex : 'sumPrice',
		// summaryType : 'sumPrice',
		summaryType : 'sum'
	// ,
	// renderer : function(v, params, record) {
	// return (record.data.singlePrice * record.data.amount).toFixed(2);
	// }
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : currInvenAnaSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false,
	bbar : new Ext.Toolbar({
		height : 23,
		items : [
		    {xtype:'tbtext', text:Ext.ux.txtFormat.barSum},
		    '-',
			'->',
			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg, '数量', 'txtCurrInvenAnaDisplayAmount', 0)},
			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg, '金额', 'txtCurrInvenAnaDisplaySumPrice', 0.00)}
		]
	})
	    
	
});

// --------------------------------------------------------------------------------------------------------

var inventAnaCurrResultWin = new Ext.Window({
	title : "当前库存",
	width : 400,
	height : 500,
	closeAction : 'hide',
	resizable : false,
	closable : false,
	layout : "fit",
	items : currInvenAnaGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			inventAnaCurrResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// --------------------------------------------------------------------------------------------------------
// --------------------------------------------------------------------------------------------------------

// 结果框 -- 出入庫匯總
// 前台：[食材种类 食材 进货数量 进货价格 进货金额 退货数量 退货价格 退货金额 出仓数量 出仓价格 出仓金额 报损数量 报损价格 报损金额]
var inOutSumReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'materialCateID',
		type : 'int'
	}, {
		name : 'materialCateName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'inCount',
		type : 'float'
	}, {
		name : 'inPrice',
		type : 'float'
	}, {
		name : 'inTotalPrice',
		type : 'float'
	}, {
		name : 'returnCount',
		type : 'float'
	}, {
		name : 'returnPrice',
		type : 'float'
	}, {
		name : 'returnTotalPrice',
		type : 'float'
	}, {
		name : 'outCount',
		type : 'float'
	}, {
		name : 'outPrice',
		type : 'float'
	}, {
		name : 'outTotalPrice',
		type : 'float'
	}, {
		name : 'costCount',
		type : 'float'
	}, {
		name : 'costPrice',
		type : 'float'
	}, {
		name : 'costTotalPrice',
		type : 'float'
	}, {
		name : 'materialID'
	}, {
		name : 'materialName',
		type : 'string'
	} ]

});

var inOutSumSummary = new Ext.grid.GroupSummary();

var inOutSumGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : inOutSumReader,
		data : [],
		sortInfo : {
			field : 'materialName',
			direction : "ASC"
		},
		groupField : 'materialCateName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "食材种类",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		hideable : false,
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'materialCateName'
	}, {
		header : "食材",
		width : 25,
		sortable : true,
		dataIndex : 'materialName'
	}, {
		header : "进货数量",
		width : 20,
		sortable : true,
		dataIndex : 'inCount',
		summaryType : 'sum'
	}, {
		header : "进货价格",
		width : 20,
		sortable : true,
		groupable : false,
		dataIndex : 'inPrice'
//		,summaryType : 'sum'
	}, {
		header : "进货金额",
		width : 20,
		sortable : false,
		dataIndex : 'inTotalPrice',
		summaryType : "sum"
	}, {
		header : "退货数量",
		width : 20,
		sortable : true,
		dataIndex : 'returnCount',
		summaryType : 'sum'
	}, {
		header : "退货价格",
		width : 20,
		sortable : true,
		groupable : false,
		dataIndex : 'returnPrice'
//		,summaryType : 'sum'
	}, {
		header : "退货金额",
		width : 20,
		sortable : false,
		dataIndex : 'returnTotalPrice',
		summaryType : "sum"
	}, {
		header : "出仓数量",
		width : 20,
		sortable : true,
		dataIndex : 'outCount',
		summaryType : 'sum'
	}, {
		header : "出仓价格",
		width : 20,
		sortable : true,
		groupable : false,
		dataIndex : 'outPrice'
//		,summaryType : 'sum'
	}, {
		header : "出仓金额",
		width : 20,
		sortable : false,
		dataIndex : 'outTotalPrice',
		summaryType : "sum"
	}, {
		header : "报损数量",
		width : 20,
		sortable : true,
		dataIndex : 'costCount',
		summaryType : 'sum'
	}, {
		header : "报损价格",
		width : 20,
		sortable : true,
		groupable : false,
		dataIndex : 'costPrice'
//		,summaryType : 'sum'
	}, {
		header : "报损金额",
		width : 20,
		sortable : false,
		dataIndex : 'costTotalPrice',
		summaryType : "sum"
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : inOutSumSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false,
	bbar : new Ext.Toolbar({
		height : 23,
		items : [
			{xtype:'tbtext', text:Ext.ux.txtFormat.barSum},
		    '-',
			'->',
//			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'进货数量','txtInOutDisplayInCount',0)},
			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'进货金额','txtInOutDisplayInTotalPrice',0.00)},
//			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'退货数量','txtInOutDisplayReturnCount',0)},
			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'退货金额','txtInOutDisplayReturnTotalPrice',0.00)},
//			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'出仓数量','txtInOutDisplayOutCount',0)},
			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'出仓金额','txtInOutDisplayOutPrice',0.00)},
//			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'报损数量','txtInOutDisplayCostCount',0)},
			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'报损金额','txtInOutDisplayCostTotalPrice',0.00)}
		]
	})
});

// --------------------------------------------------------------------------------------------------------

var invenAnaInOutSumResultWin = new Ext.Window({
	title : "出入库汇总",
	width : 1200,
	height : 500,
	closeAction : 'hide',
	resizable : false,
	closable : false,
	layout : "fit",
	items : inOutSumGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			invenAnaInOutSumResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

//-----------------------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------------------
// 结果框 -- 庫存分析
// 食材种类	食材	期初数量	进货数量	退货数量	出仓数量	报损数量	消耗数量	调入数量	调出数量	盘点损益	期末数量	价格	金额
var inventoryAnalysisReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'materialCateID',
		type : 'int'
	}, {
		name : 'materialCateName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'countBegin',
		type : 'float'
	}, {
		name : 'inCount',
		type : 'float'
	}, {
		name : 'returnCount',
		type : 'float'
	}, {
		name : 'outCount',
		type : 'float'
	}, {
		name : 'lostCount',
		type : 'float'
	}, {
		name : 'costCount',
		type : 'float'
	}, {
		name : 'changeInCount',
		type : 'float'
	}, {
		name : 'changeOutCount',
		type : 'float'
	}, {
		name : 'checkCount',
		type : 'float'
	}, {
		name : 'countEnd',
		type : 'float'
	}, {
		name : 'price',
		type : 'float'
	}, {
		name : 'totalPrice',
		type : 'float'
	}, {
		name : 'materialID'
	}, {
		name : 'materialName',
		type : 'string'
	} ]

});


var inventoryAnalysisSummary = new Ext.grid.GroupSummary();

var inventoryAnalysisGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : inventoryAnalysisReader,
		data : [],
		sortInfo : {
			field : 'materialName',
			direction : "ASC"
		},
		groupField : 'materialCateName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "食材种类",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		hideable : false,
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'materialCateName'
	}, {
		header : "食材",
		width : 25,
		sortable : true,
		dataIndex : 'materialName'
	}, {
		header : "期初数量",
		width : 20,
		sortable : true,
		dataIndex : 'countBegin',
		summaryType : 'sum'
	}, {
		header : "进货数量",
		width : 20,
		sortable : true,
		groupable : false,
		dataIndex : 'inCount',
		summaryType : 'sum'
	}, {
		header : "退货数量",
		width : 20,
		sortable : false,
		dataIndex : 'returnCount',
		summaryType : "sum"
	}, {
		header : "出仓数量",
		width : 20,
		sortable : true,
		dataIndex : 'outCount',
		summaryType : 'sum'
	}, {
		header : "报损数量",
		width : 20,
		sortable : true,
		groupable : false,
		dataIndex : 'lostCount',
		summaryType : 'sum'
	}, {
		header : "消耗数量",
		width : 20,
		sortable : false,
		dataIndex : 'costCount',
		summaryType : "sum"
	}, {
		header : "调入数量",
		width : 20,
		sortable : true,
		dataIndex : 'changeInCount',
		summaryType : 'sum'
	}, {
		header : "调出数量",
		width : 20,
		sortable : true,
		groupable : false,
		dataIndex : 'changeOutCount',
		summaryType : 'sum'
	}, {
		header : "盘点损益",
		width : 20,
		sortable : false,
		dataIndex : 'checkCount',
		summaryType : "sum"
	}, {
		header : "期末数量",
		width : 20,
		sortable : true,
		dataIndex : 'countEnd',
		summaryType : 'sum'
	}, {
		header : "价格",
		width : 20,
		sortable : true,
		groupable : false,
		dataIndex : 'price'
//		,summaryType : 'sum'
	}, {
		header : "金额",
		width : 20,
		sortable : false,
		dataIndex : 'totalPrice',
		summaryType : "sum"
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : inventoryAnalysisSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false,
	bbar : new Ext.Toolbar({
		height : 23,
		items : [
		    {xtype:'tbtext', text:Ext.ux.txtFormat.barSum},
		    '-',
		    '->',
//		    {xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'期初数量','txtIADisplayCountBegin',0)},
//			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'进货数量','txtIADisplayInCount',0)},
//			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'退货数量','txtIADisplayReturnCount',0)},
//			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'出仓数量','txtIADisplayOutCount',0)},
//			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'报损数量','txtIADisplayLostCount',0)},
//			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'消耗数量','txtIADisplayCostCount',0)},
//			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'调入数量','txtIADisplayChangeInCount',0)},
//			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'调出数量','txtIADisplayChangeOutCount',0)},
//			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'盘点损益','txtIADisplayCheckCount',0)},
//			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'期末数量','txtIADisplayCountEnd',0)},
			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg,'金额','txtIADisplayTotalPrice',0.00)}
		]
	})
});

// --------------------------------------------------------------------------------------------------------

var inventAnalysisResultWin = new Ext.Window({
	title : "库存分析",
	width : 1200,
	height : 500,
	closeAction : 'hide',
	resizable : false,
	closable : false,
	layout : "fit",
	items : inventoryAnalysisGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			inventAnalysisResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});
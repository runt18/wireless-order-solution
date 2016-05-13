Ext.onReady(function(){
	
	function showEraseOrder(orderID){
		eraseViewBillWin = new Ext.Window({
			layout : 'fit',
			title : '查看账单',
			width : 510,
			height : 550,
			resizable : false,
			closable : false,
			modal : true,
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function() {
					eraseViewBillWin.destroy();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					eraseViewBillWin.destroy();
				}
			}],
			listeners : {
				show : function(thiz) {
					var sd = Ext.ux.getSelData(eraseStatisticsGrid);
					thiz.load({
						url : '../window/history/viewBillDetail.jsp', 
						scripts : true,
						method : 'post'
					});
					thiz.center();	
					thiz.orderId = sd.id;
					thiz.branchId = sd.rid;
					thiz.queryType = 'History';
				}
			}
		});
		eraseViewBillWin.show();
		eraseViewBillWin.center();
	}
	
	var erase_beginDate = new Ext.form.DateField({
		id : 'beginDate_combo_erase',
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(true, thiz.getId(), endDate.getId());
			}
		}
	});
	
	var erase_endDate = new Ext.form.DateField({
		id : 'endDate_combo_erase',
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(false, beginDate.getId(), thiz.getId());
			}
		}
	});
	
	function initEraseGrid(){
	
		erase_dateCombo = Ext.ux.createDateCombo({
			beginDate : erase_beginDate,
			endDate : erase_endDate,
			callback : function(){
				Ext.getCmp('erase_btnSearch').handler();
			}
		});
		
		var erase_combo_staffs = new Ext.form.ComboBox({
			id : 'erase_combo_staffs',
			readOnly : false,
			forceSelection : true,
			width : 80,
			listWidth : 120,
			store : new Ext.data.SimpleStore({
				fields : ['staffID', 'staffName']
			}),
			valueField : 'staffID',
			displayField : 'staffName',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				render : function(thiz){
					var data = [[-1,'全部']];
					Ext.Ajax.request({
						url : '../../QueryStaff.do',
						params : {privileges : '1002'},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							for(var i = 0; i < jr.root.length; i++){
								data.push([jr.root[i]['staffID'], jr.root[i]['staffName']]);
							}
							thiz.store.loadData(data);
							thiz.setValue(-1);
						},
						fialure : function(res, opt){
							thiz.store.loadData(data);
							thiz.setValue(-1);
						}
					});
				},
				select : function(){
					Ext.getCmp('erase_btnSearch').handler();
				}
			}
		});
		
		var erase_deptCombo = new Ext.form.ComboBox({
			id : 'erase_deptCombo',
			forceSelection : true,
			width : 90,
			value : -1,
			store : new Ext.data.SimpleStore({
				fields : ['id', 'name']
			}),
			valueField : 'id',
			displayField : 'name',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			readOnly : false,
			listeners : {
				render : function(thiz){
					var data = [[-1,'全部']];
					Ext.Ajax.request({
						url : '../../QueryDeptTree.do',
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							for(var i = 0; i < jr.length; i++){
								data.push([jr[i]['deptID'], jr[i]['text']]);
							}
							thiz.store.loadData(data);
							thiz.setValue(-1);
							
						},
						fialure : function(res, opt){
							thiz.store.loadData(data);
							thiz.setValue(-1);
						}
					});				
				},
				select : function(){
					Ext.getCmp('erase_btnSearch').handler();
				}
			}
		});	
		
		//加载门店
		var branch_combo_eraseStatistics = new Ext.form.ComboBox({
			id : 'branch_combo_eraseStatistics',
			readOnly : false,
			forceSelection : true,
			width : 123,
			listWidth :120,
			store : new Ext.data.SimpleStore({
				fields : ['id', 'name']
			}),
			valueField : 'id',
			displayField : 'name',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				render : function(thiz){
					var data = [];
					Ext.Ajax.request({
						url : '../../OperateRestaurant.do',
						params : {
							dataSource : 'getByCond',
							id : restaurantID
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							
							if(jr.root[0].typeVal != '2'){
								var data = [];
								data.push([jr.root[0]['id'], jr.root[0]['name']]);
								thiz.store.loadData(data);
								thiz.setValue(jr.root[0].id);
								thiz.fireEvent('select');
							}else{
								var data = [[-1, '全部']];
								data.push([jr.root[0]['id'], jr.root[0]['name'] + '(集团)']);
								
								for(var i = 0; i < jr.root[0].branches.length; i++){
									data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
								}
								
								thiz.store.loadData(data);
								thiz.setValue(-1);
								thiz.fireEvent('select');
							}
						}
					});
				},
				select : function(isJump){
					if(branch_combo_eraseStatistics.getValue() == -1){
						Ext.getCmp('erase_comboBusinessHour').setDisabled(true);
						Ext.getCmp('erase_comboBusinessHour').setValue(-1);
						
						erase_combo_staffs.setDisabled(true);
						erase_combo_staffs.setValue(-1);
						
						erase_deptCombo.setDisabled(true);
						erase_deptCombo.setValue(-1);
						
					}else{
						//加载员工
						var staff = [[-1, '全部']];
						Ext.Ajax.request({
							url : '../../QueryStaff.do',
							params : {
								branchId : branch_combo_eraseStatistics.getValue()
							},
							success : function(res, opt){
								var jr = Ext.decode(res.responseText);
								
								for(var i = 0; i < jr.root.length; i++){
									staff.push([jr.root[i]['staffID'], jr.root[i]['staffName']]);
								}
								
								erase_combo_staffs.setDisabled(false);
								erase_combo_staffs.store.loadData(staff);
								erase_combo_staffs.setValue(-1);
							}
						});
						
						//加载部门
						var dept = [[-1, '全部']];
						Ext.Ajax.request({
							url : '../../OperateDept.do',
							params : {
								dataSource : 'getByCond',
								branchId : branch_combo_eraseStatistics.getValue()
							},
							success : function(res, opt){
								var jr = Ext.decode(res.responseText);
								for(var i = 0; i < jr.root.length; i++){
									dept.push([jr.root[i]['id'], jr.root[i]['name']]);
								}
								
								erase_deptCombo.setDisabled(false);
								erase_deptCombo.store.loadData(dept);
								erase_deptCombo.setValue(-1);
							}
						});
						
						//加载市别
						var hour = [[-1, '全部']];
						Ext.Ajax.request({
							url : '../../OperateBusinessHour.do',
							params : {
								dataSource : 'getByCond',
								branchId : branch_combo_eraseStatistics.getValue()
							},
							success : function(res, opt){
								var jr = Ext.decode(res.responseText);
								
								for(var i = 0; i < jr.root.length; i++){
									hour.push([jr.root[i]['id'], jr.root[i]['name'], jr.root[i]['opening'], jr.root[i]['ending']]);
								}
								
								hour.push([-2, '自定义']);
								
								Ext.getCmp('erase_comboBusinessHour').setDisabled(false);
								Ext.getCmp('erase_comboBusinessHour').store.loadData(hour);
								Ext.getCmp('erase_comboBusinessHour').setValue(-1);
							}
						});
					}
					
					if(!isJump){
						Ext.getCmp('erase_btnSearch').handler();
					}
				}
			}
		});
		
		var eraseStatisticsGridTbarItem = [{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;员工选择: '
			},erase_combo_staffs,{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;部门选择: '
			}, erase_deptCombo ,{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;门店选择: '
			}, branch_combo_eraseStatistics, '->',{
				text : '搜索',
				id : 'erase_btnSearch',
				iconCls : 'btn_search',
				handler : function(e){
					if(!erase_beginDate.isValid() || !erase_endDate.isValid()){
						return;
					}
					
					var businessHour;
					if(erase_hours){
						businessHour = erase_hours;
					}else{
						businessHour = Ext.ux.statistic_oBusinessHourData({type : 'get', statistic : 'erase_'}).data;
					}	
					
					var store = eraseStatisticsGrid.getStore();
					store.baseParams['dataSource'] = 'normal',
					store.baseParams['beginDate'] = Ext.util.Format.date(erase_beginDate.getValue(), 'Y-m-d 00:00:00');
					store.baseParams['endDate'] = Ext.util.Format.date(erase_endDate.getValue(), 'Y-m-d 23:59:59');
					store.baseParams['staffID'] = erase_combo_staffs.getValue();
					store.baseParams['deptID'] = erase_deptCombo.getValue();
					store.baseParams['opening'] = businessHour.opening;
					store.baseParams['ending'] = businessHour.ending;	
					store.baseParams['branchId'] = branch_combo_eraseStatistics.getValue();
					
					store.load({
						params : {
							start : 0,
							limit : GRID_PADDING_LIMIT_20
						}
					});
					
					if(erase_deptCombo.getValue() && erase_deptCombo.getValue() != -1){
						titleEraseDeptName = Ext.getCmp('erase_deptCombo').getEl().dom.value + ' -- ';
					}else{
						titleEraseDeptName = '';
					}
					
					if(erase_combo_staffs.getValue() && erase_combo_staffs.getValue() != -1){
						titleEraseStaffName = ' 操作员 : ' + Ext.getCmp('erase_combo_staffs').getEl().dom.value;
					}else{
						titleEraseStaffName = '';
					}
					
					requestParams = {
						dataSource : 'getDetailChart',
						dateBeg : Ext.util.Format.date(erase_beginDate.getValue(), 'Y-m-d 00:00:00'),
						dateEnd : Ext.util.Format.date(erase_endDate.getValue(), 'Y-m-d 23:59:59'),
						deptID : erase_deptCombo.getValue(),
						staffId : erase_combo_staffs.getValue(),
						opening : businessHour.opening,
						ending : businessHour.ending,
						branchId : branch_combo_eraseStatistics.getValue()
					};
					erase_chartLoadMarsk.show();
					Ext.Ajax.request({
						url : '../../QueryEraseStatistics.do',
						params : requestParams,
						success : function(res, opt){
							erase_chartLoadMarsk.hide();
							
							var jr = Ext.decode(res.responseText);
							showEraseOrderChart(jr);
						},
						failure : function(res, opt){
						
						}
					});	
					
					if(typeof erase_staffPieChart != 'undefined' && typeof eraseStaffChartPanel.hasRender != 'undefined'){
						erase_getStaffChartData();
						erase_staffPieChart = erase_loadStaffPieChart(eraseDetailsStatPanel.otype);
						erase_staffColumnChart = erase_loadStaffColumnChart(eraseDetailsStatPanel.otype);
						erase_staffPieChart.setSize(eraseStatChartTabPanel.getWidth()*0.4, erase_panelDrag ? eraseStatChartTabPanel.getHeight() - erase_cutAfterDrag : eraseStatChartTabPanel.getHeight()-erase_cutChartHeight);
						erase_staffColumnChart.setSize(eraseStatChartTabPanel.getWidth()*0.6, erase_panelDrag ? eraseStatChartTabPanel.getHeight() - erase_cutAfterDrag : eraseStatChartTabPanel.getHeight()-erase_cutChartHeight);
						
					}
					if(typeof erase_deptPieChart != 'undefined' && typeof eraseDeptChartPanel.hasRender != 'undefined'){
						erase_getDeptChartData();
						erase_deptPieChart = erase_loadDeptPieChart(eraseDetailsStatPanel.otype);
						erase_deptColumnChart = erase_loadDeptColumnChart(eraseDetailsStatPanel.otype);
						erase_deptPieChart.setSize(eraseStatChartTabPanel.getWidth()*0.4, erase_panelDrag ? eraseStatChartTabPanel.getHeight() - erase_cutAfterDrag : eraseStatChartTabPanel.getHeight()-erase_cutChartHeight);
						erase_deptColumnChart.setSize(eraseStatChartTabPanel.getWidth()*0.6, erase_panelDrag ? eraseStatChartTabPanel.getHeight() - erase_cutAfterDrag : eraseStatChartTabPanel.getHeight()-erase_cutChartHeight);
					}					
					
				}
			},'-', {
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				if(!erase_beginDate.isValid() || !erase_endDate.isValid()){
					return;
				}
				var url = '../../{0}?beginDate={1}&endDate={2}&staffID={3}&deptID={4}&dataSource={5}&branchId={6}';
				url = String.format(
						url, 
						'ExportHistoryStatisticsToExecl.do', 
						Ext.util.Format.date(erase_beginDate.getValue(), 'Y-m-d 00:00:00'),
						Ext.util.Format.date(erase_endDate.getValue(), 'Y-m-d 23:59:59'),
						erase_combo_staffs.getValue(),
						erase_deptCombo.getValue(),
						'eraseStatisticsList',
						branch_combo_eraseStatistics.getValue()
				);
				window.location = url;
			}
		}];
		
		var eraseStatisticsGridTbar = Ext.ux.initTimeBar({beginDate:erase_beginDate, endDate:erase_endDate,dateCombo:erase_dateCombo, tbarType : 1, statistic : 'erase_', callback : function businessHourSelect(){erase_hours = null;}}).concat(eraseStatisticsGridTbarItem);
		
		eraseStatisticsGrid = createGridPanel(
			'',
			'',
			'',
			'',
			'../../QueryEraseStatistics.do',
			[[true, false, false, true],
			 ['门店名称', 'restaurantName'],
			 ['日期','orderDateFormat'], 
			 ['账单号', 'id', null, null, function(v){
			 	return '<a class="orderLinkId">' + v + '</a>';
			 }],
	         ['抹数额','erasePrice', null, 'right', Ext.ux.txtFormat.gridDou], 
	         ['实收金额','actualPrice', null,'right', Ext.ux.txtFormat.gridDou],
	         ['操作人','waiter'], 
	         ['备注','comment', 200]
			],
			['orderDateFormat', 'restaurantName', 'id', 'erasePrice', 'actualPrice', 'waiter', 'comment', 'rid'],
			[ ['dataSource', 'getDetail']],
			GRID_PADDING_LIMIT_20,
			null,
			[eraseStatisticsGridTbar]
		);
		eraseStatisticsGrid.region = 'center';
		eraseStatisticsGrid.getStore().on('load', function(store, records, options){
			if(store.getCount() > 0){
				var sumRow = eraseStatisticsGrid.getView().getRow(store.getCount()-1);	
				sumRow.style.backgroundColor = '#EEEEEE';			
				
				for(var i = 0; i < eraseStatisticsGrid.getColumnModel().getColumnCount(); i++){
					var sumRow = eraseStatisticsGrid.getView().getCell(store.getCount()-1, i);
					sumRow.style.fontSize = '15px';
					sumRow.style.fontWeight = 'bold';
					sumRow.style.color = 'green';
				}
				eraseStatisticsGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
				eraseStatisticsGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
				eraseStatisticsGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
				eraseStatisticsGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '--';
				eraseStatisticsGrid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
				
				$('#eraseStatisticsPanel').find('.orderLinkId').each(function(index, element){
        			element.onclick = function(){
        				showEraseOrder($(element).text());
        			}
        		});
			}
		});
		//
		eraseDetailsStatPanel = new Ext.Panel({
			title : '抹数明细',
			layout : 'border',
			region : 'center',
			items : [eraseStatisticsGrid],
			listeners : {
				bodyresize : function(e, w, h){
					if(typeof erasePanelHeight != 'undefined'){
						var chartHeight = erase_tabPanelHeight + (erasePanelHeight - h);
						
						eraseStatChartTabPanel.getEl().setTop((h+30)) ;
						
						erase_changeChartWidth(w,chartHeight-erase_cutAfterDrag);
						
						if(erase_panelDrag){
							eraseStatChartTabPanel.setHeight(chartHeight);
						}
						eraseStatChartTabPanel.doLayout();					
					}
				}
			}
		});	
	
	}

	function erase_changeChartWidth(w,h){
		if(eval($('div:visible[data-type=eraseChart]').attr('data-value'))){
			if($('div:visible[data-type=eraseChart]').length == 1){
				eval($('div:visible[data-type=eraseChart]').attr('data-value')).setSize(w, h+20);
			}else if($('div:visible[data-type=eraseChart]').length > 1){
				eval($($('div:visible[data-type=eraseChart]')[0]).attr('data-value')).setSize(w*0.4, h);
				eval($($('div:visible[data-type=eraseChart]')[1]).attr('data-value')).setSize(w*0.6, h);				
			}
		}	
	}

	function showEraseOrderChart(jdata){
		var dateBegin = Ext.util.Format.date(Ext.getCmp('beginDate_combo_erase').getValue(), 'Y-m-d');
		var dateEnd = Ext.util.Format.date(Ext.getCmp('endDate_combo_erase').getValue(), 'Y-m-d');
		
		var hourBegin = Ext.getCmp('erase_txtBusinessHourBegin').getEl().dom.textContent;
		var hourEnd = Ext.getCmp('erase_txtBusinessHourEnd').getEl().dom.textContent;
		
		var chartData = eval('(' + jdata.other.chart + ')');
		erase_detailChart = new Highcharts.Chart({
			plotOptions : {
				line : {
					cursor : 'pointer',
					dataLabels : {
						enabled : true,
						style : {
							fontWeight: 'bold', 
							color: 'green' 
						}
					},
					events : {
						click : function(e){
	//						loadBusinessStatistic(e.point.category);
						}
					}
				}
			},
	        chart: {  
	        	renderTo: 'divEraseDetailChart'
	    	}, 
	        title: {
	            text: '<b>'+titleEraseDeptName+'抹数额走势图（'+dateBegin+ '至' +dateEnd+'）'+hourBegin+ ' - ' + hourEnd + titleEraseStaffName+'</b>'
	        },
	        labels: {
	        	items : [{
	        		html : '<b>总抹数金额:' + chartData.totalMoney + ' 元</b><br><b>日均抹数金额:' + chartData.avgMoney + ' 元</b><br><b>日均抹数数量:' + chartData.avgCount + ' 份</b>',
		        	style : {left :/*($('#businessReceiptsChart').width()*0.80)*/'0px', top: '0px'}
	        	}]
	        },
	        xAxis: {
	            categories: chartData.xAxis,
	            labels : {
	            	formatter : function(){
	            		return this.value.substring(5, 10);
	            	}
	            }
	        },
	        yAxis: {
	        	min: 0,
	            title: {
	                text: '金额 (元)'
	            },
	            plotLines: [{
	                value: 0,
	                width: 2,
	                color: '#808080'
	            }]
	        },
	        tooltip: {
	            formatter: function() {
	                return '<b>' + this.series.name + '</b><br/>'+
	                    this.x.substring(0, 10) +': '+ '<b>'+this.y+'</b> ';
	            }
	        },
	        series : chartData.ser,
	        exporting : {
	        	enabled : true
	        },
	        credits : {
	        	enabled : false
	        }
		});
		
		if(eraseDetailChartPanel && eraseDetailChartPanel.isVisible()){
			eraseDetailChartPanel.show();
		}
	}

	var erase_getStaffChartData = function(){
		requestParams.dataSource = 'getStaffChart';
		$.ajax({
			url : '../../QueryEraseStatistics.do',
			type : 'post',
			async : false,
			data : requestParams,
			success : function(jr, status, xhr){
				erase_chartLoadMarsk.hide();
				resetChartDate(erase_staffChartData);
				
				for (var i = 0; i < jr.root.length; i++) {
					erase_staffChartData.chartPriceData.data.push([jr.root[i].eraseStaff, jr.root[i].erasePrice]);
					erase_staffChartData.chartAmountData.data.push([jr.root[i].eraseStaff, jr.root[i].eraseAmount]);
					erase_staffChartData.priceColumnChart.xAxis.push(jr.root[i].eraseStaff);
					erase_staffChartData.priceColumnChart.yAxis.data.push({y : jr.root[i].erasePrice, color : colors[i]}); 
					erase_staffChartData.amountColumnChart.yAxis.data.push({y : jr.root[i].eraseAmount, color : colors[i]});
				}
			},
			failure : function(res, opt){
			
			}
		});
	};
	
	var erase_loadStaffPieChart = function(type){
		var content = {};
		content.title = type==1?'员工抹数数量比例图':'员工抹数金额比例图';
		content.unit = type==1?'份':'元';
		content.series = type==1?erase_staffChartData.chartAmountData:erase_staffChartData.chartPriceData;
		
		return newPieChart({rt: 'divEraseStaffPieChart', title : content.title, unit: content.unit, series: content.series});	
	};
	
	var erase_loadStaffColumnChart = function(type){
		var content = {};
		content.title = type==1?'员工抹数账数量柱状图':'员工抹数金额柱状图';
		content.unit = type==1?'份':'元';
		content.series = type==1?erase_staffChartData.amountColumnChart.yAxis : erase_staffChartData.priceColumnChart.yAxis;	
	    
	    return newColumnChart({
	    	rt: 'divEraseStaffColumnChart', title : content.title, unit: content.unit, series: content.series, xAxis:erase_staffChartData.priceColumnChart.xAxis	
	    });	
	};

	//按员工汇总抹数数量的柱状和饼图
	$('#staffByAmount_span_eraseChart').click(function(){
		erase_fnChangeStaffChart(this, 1);
	});
	
	//按员工汇总抹数金额的柱状和饼图
	$('#staffByFee_span_eraseChart').click(function(){
		erase_fnChangeStaffChart(this, 0);
	});
	
	function erase_fnChangeStaffChart(thiz, v){
		$(thiz).find('input').attr('checked', 'checked');
		erase_staffPieChart = erase_loadStaffPieChart(v);
		erase_staffColumnChart = erase_loadStaffColumnChart(v);
		erase_staffPieChart.setSize(eraseStatChartTabPanel.getWidth()*0.4, erase_panelDrag ? eraseStatChartTabPanel.getHeight() - erase_cutAfterDrag : eraseStatChartTabPanel.getHeight()-erase_cutChartHeight);
		erase_staffColumnChart.setSize(eraseStatChartTabPanel.getWidth()*0.6, erase_panelDrag ? eraseStatChartTabPanel.getHeight() - erase_cutAfterDrag : eraseStatChartTabPanel.getHeight()-erase_cutChartHeight);
		
		eraseDetailsStatPanel.otype = v;
	}

	var eraseViewBillWin;
	var erase_cutAfterDrag = 190, erase_cutBeforeDrag = 40, erase_hours;
	var titleEraseDeptName, titleEraseStaffName;
	var erase_detailChart, erase_staffPieChart, erase_staffColumnChart, erase_deptPieChart, erase_deptColumnChart;
	
	var eraseDetailChartPanel, eraseStatChartTabPanel;
	var erasePanelHeight, erase_tabPanelHeight;
	var erase_panelDrag = false, colors = Highcharts.getOptions().colors;
	var erase_staffChartData = Wireless.chart.initChartData({priceName:'员工抹数金额', countName:'员工抹数数量'});
					            
	var erase_chartLoadMarsk, erase_dateCombo ;

	initEraseGrid();
	
	eraseDetailChartPanel = new Ext.Panel({
		title : '抹数走势',
		contentEl : 'divEraseDetailChart',
		listeners : {
			show : function(thiz){
				//thiz.getEl(): 刚打开页面时thiz.getWidth无效
				if(erase_detailChart && typeof thiz.getEl() != 'undefined'){
					erase_detailChart.setSize(thiz.getWidth(), erase_panelDrag ? eraseStatChartTabPanel.getHeight() - 170 : eraseStatChartTabPanel.getHeight()-30);
				}
			}
		}		
	});
	
	eraseStaffChartPanel = new Ext.Panel({
		title : '按员工汇总',
		contentEl : 'divEraseStaffCharts',
		listeners : {
			render : function(thiz){
				thiz.hasRender = true;
			}
		}
	});	
	
	eraseStatChartTabPanel = new Ext.TabPanel({
		region : 'south',
		height : 430,
		items : [eraseDetailChartPanel, eraseStaffChartPanel],
		listeners : {
			render : function(thiz){
				thiz.setActiveTab(eraseDetailChartPanel);
			}
		}
	});

	eraseStaffChartPanel.addListener('show', function(){
		var charts = Wireless.chart.initChartPanel({
			divLeftShowChart : 'divEraseStaffPieChart',
			divRightShowChart : 'divEraseStaffColumnChart',
			generalName : 'eraseStaff_',
			getChartData : erase_getStaffChartData,
			leftChartLoad : erase_loadStaffPieChart,
			rightChartLoad : erase_loadStaffColumnChart,
			panelDrag : true,
			leftChart : erase_staffPieChart,
			rightChart : erase_staffColumnChart,
			loadType : 1,
			tabPanel : eraseStatChartTabPanel,
			cutAfterDrag : erase_cutAfterDrag,
			cutBeforeDrag : erase_cutBeforeDrag		
			
		});	
		erase_staffPieChart = charts.pie;
		erase_staffColumnChart = charts.column;
		
		charts = null;
	});	
	
	new Ext.Panel({
		renderTo : 'divEraseStatistics',//渲染到
		id : 'eraseStatisticsPanel',
		//solve不跟随窗口的变化而变化
		width : parseInt(Ext.getDom('divEraseStatistics').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divEraseStatistics').parentElement.style.height.replace(/px/g,'')),
		layout:'border',
		frame : true, //边框
		//子集
		items : [eraseDetailsStatPanel, eraseStatChartTabPanel]
	});
	
	erasePanelHeight = eraseDetailChartPanel.getHeight();
	
	erase_tabPanelHeight = eraseStatChartTabPanel.getHeight();	
	
	
	
	var erase_rz = new Ext.Resizable(eraseDetailsStatPanel.getEl(), {
        wrap: true, //在构造Resizable时自动在制定的id的外边包裹一层div
        minHeight:100, //限制改变的最小的高度
        pinned:false, //控制可拖动区域的显示状态，false是鼠标悬停在拖拉区域上才出现
        handles: 's',//设置拖拉的方向（n,s,e,w,all...）
        listeners : {
        	resize : function(thiz, w, h, e){
        		erase_panelDrag = true;
        	}
        }
    });
    erase_rz.on('resize', eraseDetailsStatPanel.syncSize, eraseDetailsStatPanel);//注册事件(作用:将调好的大小传个scope执行)
	
	erase_chartLoadMarsk = new Ext.LoadMask(eraseStatChartTabPanel.getEl().dom, {
	    msg  : '数据统计中，请稍候......',
	    disabled : false
	});	
	
    var erase_totalHeight = Ext.getCmp('eraseStatisticsPanel').getHeight();
    
    eraseDetailsStatPanel.setHeight(erase_totalHeight*0.4);
    eraseDetailsStatPanel.getEl().parent().setHeight(erase_totalHeight*0.4);
    
    eraseStatChartTabPanel.setHeight(erase_totalHeight*0.6);	
    
    erase_rz.resizeTo(eraseDetailsStatPanel.getWidth(), erase_totalHeight*0.4);	
    
    erase_dateCombo.setValue(1);
	erase_dateCombo.fireEvent('select', erase_dateCombo, null, 1);	
});

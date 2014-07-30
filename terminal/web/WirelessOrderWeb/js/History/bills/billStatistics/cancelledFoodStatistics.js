var cfdsGrid;
var CANCELL_FOOD_PAGE_LIMIT = 22;

function cancellFood_showBillDetailWin(){
	cancellFoodOrderDetailWin = new Ext.Window({
		layout : 'fit',
		width : 1100,
		height : 440,
		closable : false,
		resizable : false,
		modal : true,
		bbar : ['->', {
			text : '关闭',
			iconCls : 'btn_close',
			handler : function() {
				cancellFoodOrderDetailWin.destroy();
			}
		} ],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				cancellFoodOrderDetailWin.destroy();
			}
		}],
		listeners : {
			show : function(thiz) {
				var sd = Ext.ux.getSelData(cfdsGrid);
				thiz.load({
					url : '../window/history/orderDetail.jsp', 
					scripts : true,
					params : {
						orderId : sd.orderId,
						foodStatus : 'isReturn'
					},
					method : 'post'
				});
				thiz.center();	
			}
		}
	});
}
function cancellFood_billDetailHandler(orderID) {
	cancellFood_showBillDetailWin();
	cancellFoodOrderDetailWin.show();
	cancellFoodOrderDetailWin.setTitle('账单号: ' + orderID);
	cancellFoodOrderDetailWin.center();
};

function linkOrderId(v){
	return '<a href=\"javascript:cancellFood_billDetailHandler('+ v +')\">'+ v +'</a>';
}
function cancelFoodDetailsStatPanelInit(){
	
	var beginDate = new Ext.form.DateField({
		id : 'cancel_dateSearchDateBegin',
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
	var endDate = new Ext.form.DateField({
		id : 'cancel_dateSearchDateEnd',
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
	cancel_dateCombo = Ext.ux.createDateCombo({
		beginDate : beginDate,
		endDate : endDate,
		callback : function(){
			Ext.getCmp('cancel_btnSearch').handler();
		}
	});
	var reasonCombo = new Ext.form.ComboBox({
		forceSelection : true,
		width : 90,
		value : -1,
		store : new Ext.data.SimpleStore({
			fields : ['id', 'reason']
		}),
		valueField : 'id',
		displayField : 'reason',
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
					url : '../../QueryCancelReason.do',
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						for(var i = 0; i < jr.root.length; i++){
							data.push([jr.root[i]['id'], jr.root[i]['reason']]);
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
				Ext.getCmp('cancel_btnSearch').handler();
			}
		}
	});
	
	var cancel_deptCombo = new Ext.form.ComboBox({
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
				Ext.getCmp('cancel_btnSearch').handler();
			}
		}
	});
	
	var cancel_combo_staffs = new Ext.form.ComboBox({
		id : 'cancel_combo_staffs',
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
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						for(var i = 0; i < jr.root.length; i++){
							data.push([jr.root[i]['staffID'], jr.root[i]['staffName']]);
						}
						thiz.store.loadData(data);
						thiz.setValue(-1);
						
						if(sendToPageOperation){
							cancel_setStatisticsDate();
						}else{
							cancel_dateCombo.setValue(1);
							cancel_dateCombo.fireEvent('select', cancel_dateCombo, null, 1);			
						}							
					},
					fialure : function(res, opt){
						thiz.store.loadData(data);
						thiz.setValue(-1);
					}
				});
			},
			select : function(){
				Ext.getCmp('cancel_btnSearch').handler();
			}
		}
	});
	
	var cfdsGridDateTbar = Ext.ux.initTimeBar({beginDate:beginDate, endDate:endDate,dateCombo:cancel_dateCombo, tbarType : 0, statistic : 'cancel_'});
	
	var cfdsGridTbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype:'tbtext',
			text:'&nbsp;退菜原因:'
		}, reasonCombo, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;部门: '
		},cancel_deptCombo, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;员工: '
		},cancel_combo_staffs, {
			xtype:'tbtext',
			text:'&nbsp;'
		}, '->', {
			text : '搜索',
			id : 'cancel_btnSearch',
			iconCls : 'btn_search',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				if(bd == '' && ed == ''){
					cancel_dateCombo.setValue(0);
					cancel_dateCombo.fireEvent('select',cancel_dateCombo,null,0);
					return;
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				var opening, ending;
				var businessHour = Ext.ux.statistic_oBusinessHourData({type : 'get', statistic : 'cancel_'}).data;
				if(businessHour.businessHourType != -1){
					opening = businessHour.opening;
					ending = businessHour.ending;
				}else{
					opening = '';
					ending = '';
				}					
				var gs = cfdsGrid.getStore();
				gs.baseParams['deptID'] = cancel_deptCombo.getValue();
				gs.baseParams['dateBeg'] = beginDate.getValue().format('Y-m-d 00:00:00');
				gs.baseParams['dateEnd'] = endDate.getValue().format('Y-m-d 23:59:59');
				gs.baseParams['reasonID'] = reasonCombo.getValue();
				gs.baseParams['staffID'] = cancel_combo_staffs.getValue();
				gs.baseParams['opening'] = opening;
				gs.baseParams['ending'] = ending;
				gs.load({
					params : {
						start : 0,
						limit : CANCELL_FOOD_PAGE_LIMIT
					}
				});
				
				if(cancel_deptCombo.getValue() && cancel_deptCombo.getValue() != -1){
					titleCancelDeptName = cancel_deptCombo.getEl().dom.value + ' -- ';
				}else{
					titleCancelDeptName = '';
				}
				
				if(cancel_combo_staffs.getValue() && cancel_combo_staffs.getValue() != -1){
					titleCancelStaffName = ' 操作员 : '+ cancel_combo_staffs.getEl().dom.value ;
				}else{
					titleCancelStaffName = '';
				}				
				
				cancel_requestParams = {
					dataSource : 'getDetailChart',
					dateBeg : beginDate.getValue().format('Y-m-d 00:00:00'),
					dateEnd : endDate.getValue().format('Y-m-d 23:59:59'),
					deptID : cancel_deptCombo.getValue(),
					reasonID : reasonCombo.getValue(),
					staffID : cancel_combo_staffs.getValue(),
					opening : opening,
					ending : ending
				};
				cancel_chartLoadMarsk.show();
				
				Ext.Ajax.request({
					url : '../../QueryCancelledFood.do',
					params : cancel_requestParams,
					success : function(res, opt){
						cancel_chartLoadMarsk.hide();
						var jr = Ext.decode(res.responseText);
						showCancelDetailChart(jr);
					},
					failure : function(res, opt){
					
					}
				});
				
				//is(":visible"):第一次切换tab才去生成chart
				if(typeof reasonChart != 'undefined' && typeof cancelledReasonChartPanel.hasRender != 'undefined'){
					getRaasonChartData();
					if($('#divCancelledReasonColumnChart').is(':visible')){
						loadReasonColumnChart();
						reasonColumnChart.setSize(cancelFoodStatChartTabPanel.getWidth(), cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
					}else{
						loadReasonChart();
						loadPriceReasonChart();
						reasonChart.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
						reasonChart_price.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);							
					}
				}
				
				if(typeof cancel_staffChart != 'undefined' && typeof cancelledStaffChartPanel.hasRender != 'undefined'){
					cancel_getStaffChartData();
					if($('#divCancelledStaffColumnChart').is(':visible')){
						cancel_loadStaffColumnChart();
						cancel_staffColumnChart.setSize(cancelFoodStatChartTabPanel.getWidth(), cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
					}else{
						cancel_loadStaffChart();
						cancel_loadAmountStaffChart();
						cancel_staffChart.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
						cancel_staffChart_amount.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
					}
				}
				
				if(typeof cancel_deptChart != 'undefined' && typeof cancelledDeptChartPanel.hasRender != 'undefined'){
					cancel_getDeptChartData();
					if($('#divCancelledDeptColumnChart').is(':visible')){
						cancel_loadDeptColumnChart();
						cancel_deptChart.setSize(cancelFoodStatChartTabPanel.getWidth(), cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
					}else{
						cancel_loadDeptChart();
						cancel_loadAmountDeptChart();
						cancel_deptChart.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
						cancel_deptChart_amount.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);						
					} 
					
				}
			}
		},{
			text : '导出',
			id : 'btnExportExcel',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				if(bd == '' && ed == '' ){
					Ext.example.msg('提示', '未选择日期, 无法导出数据');
					return;
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				
				var url = '../../{0}?deptID={1}&dateBeg={2}&dateEnd={3}&reasonID={4}&dataSource={5}&staffID={6}';
				
				url = String.format(
					url,
					'ExportHistoryStatisticsToExecl.do',
					cancel_deptCombo.getValue(),
					beginDate.getValue().format('Y-m-d 00:00:00'),
					endDate.getValue().format('Y-m-d 23:59:59'),
					reasonCombo.getValue(),
					'cancelledFood',
					cancel_combo_staffs.getValue()
				);
				window.location = url;

			}
		}]
	});
	cfdsGrid = createGridPanel(
		'',
		'',
		'',
		'',
		'../../QueryCancelledFood.do',
		[[true, false, false, true], 
		 ['日期','orderDateFormat',150], 
		 ['菜名','name',180],
         ['部门','kitchen.dept.name'], 
         ['账单号', 'orderId',,,'linkOrderId'],
         ['单价','unitPrice',,'right','Ext.ux.txtFormat.gridDou'],
         ['退菜数量','count',,'right','Ext.ux.txtFormat.gridDou'], 
         ['退菜金额','totalPrice',,'right','Ext.ux.txtFormat.gridDou'],		              
         ['操作人','waiter'], 
         ['退菜原因','cancelReason.reason', 200]
		],
		['orderDateFormat', 'name', 'kitchen.dept.name', 'orderId', 'unitPrice', 'count', 'totalPrice', 'waiter', 'cancelReason.reason'],
		[ ['dataSource', 'getDetail']],
		CANCELL_FOOD_PAGE_LIMIT,
		null,
		[cfdsGridTbar, cfdsGridDateTbar]
	);
	cfdsGrid.region = 'center';

	cfdsGrid.getStore().on('load', function(store, records, options){
		if(store.getCount() > 0){
			var sumRow = cfdsGrid.getView().getRow(store.getCount()-1);	
			
			var sumData = store.getAt(store.getCount()-1);
			
			sumRow.style.backgroundColor = '#EEEEEE';			
			
			for(var i = 0; i < cfdsGrid.getColumnModel().getColumnCount(); i++){
				var sumRow = cfdsGrid.getView().getCell(store.getCount()-1, i);
				sumRow.style.fontSize = '15px';
				sumRow.style.fontWeight = 'bold';
				sumRow.style.color = 'green';
			}
			cfdsGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
			cfdsGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
			cfdsGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
			cfdsGrid.getView().getCell(store.getCount()-1, 4).innerHTML = '--';
			cfdsGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '--';
			cfdsGrid.getView().getCell(store.getCount()-1, 7).innerHTML = sumData.get('unitPrice').toFixed(2);
			cfdsGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
			cfdsGrid.getView().getCell(store.getCount()-1, 9).innerHTML = '--';
		}
	});
	//
	cancelFoodDetailsStatPanel = new Ext.Panel({
		title : '明细汇总',
		layout : 'border',
		region : 'center',
		items : [cfdsGrid],
		listeners : {
			bodyresize : function(e, w, h){
				if(typeof cancelPanelHeight != 'undefined'){
					var chartHeight = cancelTabPanelHeight+ (cancelPanelHeight - h);
					
					cancelFoodStatChartTabPanel.getEl().setTop((h+30)) ;
					
					cancel_changeChartWidth(w,chartHeight-60);
					
					if(cancel_panelDrag){
						cancelFoodStatChartTabPanel.setHeight(chartHeight);
					}
					cancelFoodStatChartTabPanel.doLayout();					
				}
			}
		}
	});
}

function cancel_changeChartWidth(w,h){
	if(eval($('div:visible[data-type=chart]').attr('data-value'))){
		if($('div:visible[data-type=chart]').length == 1){
			eval($('div:visible[data-type=chart]').attr('data-value')).setSize(w, h);
		}else if($('div:visible[data-type=chart]').length > 1){
			eval($($('div:visible[data-type=chart]')[0]).attr('data-value')).setSize(w/2, h);
			eval($($('div:visible[data-type=chart]')[1]).attr('data-value')).setSize(w/2, h);				
		}
	}
}

function getRaasonChartData(){
	cancel_requestParams.dataSource = 'getReasonChart';
	$.ajax({
		url : '../../QueryCancelledFood.do',
		type : 'post',
		async : false,
		data : cancel_requestParams,
		success : function(jr, status, xhr){
			reasonChartData.chartData.data = [];
			reasonChartData.chartData1.data = [];
			reasonChartData.reasonColumnChart.xAxis = [];
			reasonChartData.reasonColumnChart.yAxis.data = [];
			reasonChartData.reasonColumnChart.yAxis1.data = [];
			for (var i = 0; i < jr.root.length; i++) {
				reasonChartData.chartData.data.push([jr.root[i].reason, jr.root[i].cancelAmount]);
				reasonChartData.chartData1.data.push([jr.root[i].reason, jr.root[i].cancelPrice]);
				reasonChartData.reasonColumnChart.xAxis.push(jr.root[i].reason);
				reasonChartData.reasonColumnChart.yAxis.data.push({y : jr.root[i].cancelAmount, color : colors[i]});
				reasonChartData.reasonColumnChart.yAxis1.data.push({y : jr.root[i].cancelPrice, color : colors[i]});
				
			}
		},
		failure : function(res, opt){
		
		}
	});
	
}

function cancel_getStaffChartData(){
	cancel_requestParams.dataSource = 'getStaffChart';
	$.ajax({
		url : '../../QueryCancelledFood.do',
		type : 'post',
		async : false,
		data : cancel_requestParams,
		success : function(jr, status, xhr){
			cancel_staffChartData.chartData.data = [];
			cancel_staffChartData.chartAmountData.data = [];
			
			cancel_staffChartData.staffColumnChart.xAxis = [];
			cancel_staffChartData.staffColumnChart.yAxis.data = [];
			cancel_staffChartData.staffColumnChart.yAxisAmount.data = [];
			for (var i = 0; i < jr.root.length; i++) {
				cancel_staffChartData.chartData.data.push([jr.root[i].cancelStaff, jr.root[i].cancelPrice]);
				cancel_staffChartData.chartAmountData.data.push([jr.root[i].cancelStaff, jr.root[i].cancelAmount]);
				cancel_staffChartData.staffColumnChart.xAxis.push(jr.root[i].cancelStaff);
				cancel_staffChartData.staffColumnChart.yAxis.data.push({y : jr.root[i].cancelPrice, color : colors[i]}); 
				cancel_staffChartData.staffColumnChart.yAxisAmount.data.push({y : jr.root[i].cancelAmount, color : colors[i]});
			}
		},
		failure : function(res, opt){
		
		}
	});
	
}

function cancel_getDeptChartData(){
	cancel_requestParams.dataSource = 'getDeptChart';
	$.ajax({
		url : '../../QueryCancelledFood.do',
		type : 'post',
		async : false,
		data : cancel_requestParams,
		success : function(jr, status, xhr){
			cancel_deptChartData.chartData.data = [];
			cancel_deptChartData.chartAmountData.data = [];
			
			cancel_deptChartData.deptColumnChart.xAxis = [];
			cancel_deptChartData.deptColumnChart.yAxis.data = [];
			cancel_deptChartData.deptColumnChart.yAxisAmount.data = [];
			for (var i = 0; i < jr.root.length; i++) {
				cancel_deptChartData.chartData.data.push([jr.root[i].cancelDept.name, jr.root[i].cancelPrice]);
				cancel_deptChartData.chartAmountData.data.push([jr.root[i].cancelDept.name, jr.root[i].cancelAmount]);
				
				cancel_deptChartData.deptColumnChart.xAxis.push(jr.root[i].cancelDept.name);
				cancel_deptChartData.deptColumnChart.yAxis.data.push({y : jr.root[i].cancelPrice, color : colors[i]}); 
				cancel_deptChartData.deptColumnChart.yAxisAmount.data.push({y : jr.root[i].cancelAmount, color : colors[i]});
			}
		},
		failure : function(res, opt){
		
		}
	});
	
}


function loadReasonColumnChart(){
 	reasonColumnChart = new Highcharts.Chart({
        chart: {
            type: 'column',
            renderTo : 'divCancelledReasonColumnChart'
        },
        title: {
            text: '退菜原因柱状图'
        },
        xAxis: {
            categories: reasonChartData.reasonColumnChart.xAxis
        },
        yAxis: {
            min: 0,
            title: {
                text: '数量 / 金额'
            }
        },
        tooltip: {
            pointFormat: '<table><tbody><tr><td style="color:red;padding:0">{series.name}: </td><td style="padding:0"><b>{point.y} </b></td></tr></tbody></table>',
            shared: true,
            useHTML: true
        },
        plotOptions: {
            column: {
                pointPadding: 0.2,
                borderWidth: 0
            }
        },
        series: [reasonChartData.reasonColumnChart.yAxis, reasonChartData.reasonColumnChart.yAxis1],
        credits : {
        	enabled : false
        }
    });	
}

function cancel_loadStaffColumnChart(){
 	cancel_staffColumnChart = new Highcharts.Chart({
        chart: {
            type: 'column',
            renderTo : 'divCancelledStaffColumnChart'
        },
        title: {
            text: '员工退菜柱状图'
        },
        xAxis: {
            categories: cancel_staffChartData.staffColumnChart.xAxis
        },
        yAxis: {
            min: 0,
            title: {
                text: '金额 / 数量'
            }
        },
        tooltip: {
            pointFormat: '<table><tbody><tr><td style="color:red;padding:0">{series.name}: </td><td style="padding:0"><b>{point.y} </b></td></tr></tbody></table>',
            shared: true,
            useHTML: true
        },
        plotOptions: {
            column: {
                pointPadding: 0.2,
                borderWidth: 0              
            }
        },
        series: [cancel_staffChartData.staffColumnChart.yAxis, cancel_staffChartData.staffColumnChart.yAxisAmount],
        credits : {
        	enabled : false
        }
    });	
}

function cancel_loadDeptColumnChart(){
 	cancel_deptChart = new Highcharts.Chart({
        chart: {
            type: 'column',
            renderTo : 'divCancelledDeptColumnChart'
        },
        title: {
            text: '部门退菜柱状图'
        },
        xAxis: {
            categories: cancel_deptChartData.deptColumnChart.xAxis
        },
        yAxis: {
            min: 0,
            title: {
                text: '金额 / 数量'
            }
        },
        tooltip: {
            pointFormat: '<table><tbody><tr><td style="color:red;padding:0">{series.name}: </td><td style="padding:0"><b>{point.y} </b></td></tr></tbody></table>',
            shared: true,
            useHTML: true
        },
        plotOptions: {
            column: {
                pointPadding: 0.2,
                borderWidth: 0
            }
        },
        series: [cancel_deptChartData.deptColumnChart.yAxis, cancel_deptChartData.deptColumnChart.yAxisAmount],
        credits : {
        	enabled : false
        }
    });	
}

function loadReasonChart(){
	reasonChart = new Highcharts.Chart({
	    chart: {
	    	renderTo : 'divCancelledCountReasonChart',
	        plotBackgroundColor: null,
	        plotBorderWidth: null,
	        plotShadow: false
	    },
	    title: {
	        text: '原因比例图'
	    },
	    credits : {
	    	enabled : false
	    },
	    tooltip: {
		    pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
	    },
	    plotOptions: {
	        pie: {
	            allowPointSelect: true,
	            cursor: 'pointer',
	            dataLabels: {
	                enabled: true,
	                color: '#000000',
	                connectorColor: '#000000',
	                format: '<b>{point.name}</b>: {point.y} 份'
	            }
	        }
	    },
	    series: [reasonChartData.chartData]
	});				
	
}

function loadPriceReasonChart(){
	reasonChart_price = new Highcharts.Chart({
	    chart: {
	    	renderTo : 'divCancelledPriceReasonChart',
	        plotBackgroundColor: null,
	        plotBorderWidth: null,
	        plotShadow: false
	    },
	    title: {
	        text: '原因金额比例图'
	    },
	    tooltip: {
		    pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
	    },
	    plotOptions: {
	        pie: {
	            allowPointSelect: true,
	            cursor: 'pointer',
	            dataLabels: {
	                enabled: true,
	                color: '#000000',
	                connectorColor: '#000000',
	                format: '<b>{point.name}</b>: {point.y} 元'
	            }
	        }
	    },
	    series: [reasonChartData.chartData1],
        credits : {
        	enabled : false
        }
	});				
	
}

function cancel_loadStaffChart(){
	
	cancel_staffChart = new Highcharts.Chart({
	    chart: {
	    	renderTo : 'divCancelledPriceStaffChart',
	        plotBackgroundColor: null,
	        plotBorderWidth: null,
	        plotShadow: false
	    },
	    title: {
	        text: '员工退菜金额比例图'
	    },
	    tooltip: {
		    pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
	    },
	    plotOptions: {
	        pie: {
	            allowPointSelect: true,
	            cursor: 'pointer',
	            dataLabels: {
	                enabled: true,
	                color: '#000000',
	                connectorColor: '#000000',
	                format: '<b>{point.name}</b>: {point.y} 元'
	            }
	        }
	    },
	    series: [cancel_staffChartData.chartData],
        credits : {
        	enabled : false
        }
	});				
}


function cancel_loadAmountStaffChart(){
	
	cancel_staffChart_amount = new Highcharts.Chart({
	    chart: {
	    	renderTo : 'divCancelledCountStaffChart',
	        plotBackgroundColor: null,
	        plotBorderWidth: null,
	        plotShadow: false
	    },
	    title: {
	        text: '员工退菜数量比例图'
	    },
	    tooltip: {
		    pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
	    },
	    plotOptions: {
	        pie: {
	            allowPointSelect: true,
	            cursor: 'pointer',
	            dataLabels: {
	                enabled: true,
	                color: '#000000',
	                connectorColor: '#000000',
	                format: '<b>{point.name}</b>: {point.y} 份'
	            }
	        }
	    },
	    series: [cancel_staffChartData.chartAmountData],
        credits : {
        	enabled : false
        }
	});				
}

function cancel_loadDeptChart(){
	cancel_deptChart = new Highcharts.Chart({

	    chart: {
	    	renderTo : 'divCancelledCountDeptChart',
	        plotBackgroundColor: null,
	        plotBorderWidth: null,
	        plotShadow: false
	    },
	    title: {
	        text: '部门退菜金额比例图'
	    },
	    tooltip: {
		    pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
	    },
	    plotOptions: {
	        pie: {
	            allowPointSelect: true,
	            cursor: 'pointer',
	            dataLabels: {
	                enabled: true,
	                color: '#000000',
	                connectorColor: '#000000',
	                format: '<b>{point.name}</b>: {point.y} 元'
	            }
	        }
	    },
	    series: [cancel_deptChartData.chartData],
        credits : {
        	enabled : false
        }
	});				
}

function cancel_loadAmountDeptChart(){
	cancel_deptChart_amount = new Highcharts.Chart({

	    chart: {
	    	renderTo : 'divCancelledPriceDeptChart',
	        plotBackgroundColor: null,
	        plotBorderWidth: null,
	        plotShadow: false
	    },
	    title: {
	        text: '部门退菜数量比例图'
	    },
	    tooltip: {
		    pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
	    },
	    plotOptions: {
	        pie: {
	            allowPointSelect: true,
	            cursor: 'pointer',
	            dataLabels: {
	                enabled: true,
	                color: '#000000',
	                connectorColor: '#000000',
	                format: '<b>{point.name}</b>: {point.y} 份'
	            }
	        }
	    },
	    series: [cancel_deptChartData.chartAmountData],
        credits : {
        	enabled : false
        }
	});				
}

function showCancelDetailChart(jdata){
	var dateBegin = Ext.getCmp('cancel_dateSearchDateBegin').getValue().format('Y-m-d');
	var dateEnd = Ext.getCmp('cancel_dateSearchDateEnd').getValue().format('Y-m-d');
	
	var hourBegin = Ext.getCmp('cancel_txtBusinessHourBegin').getEl().dom.textContent;
	var hourEnd = Ext.getCmp('cancel_txtBusinessHourEnd').getEl().dom.textContent;
	
	var chartData = eval('(' + jdata.other.chart + ')');
	cancel_detailChart = new Highcharts.Chart({
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
						loadBusinessStatistic(e.point.category);
					}
				}
			}
		},
        chart: {  
        	renderTo: 'divCancelledDetailChart'
    	}, 
        title: {
            text: '<b>'+ titleCancelDeptName +'退菜走势图（'+dateBegin+ '至' +dateEnd+'）'+hourBegin+ ' - ' + hourEnd + titleCancelStaffName +'</b>'
        },
        labels: {
        	items : [{
        		html : '<b>总退菜金额: ' + chartData.totalMoney + ' 元</b><br><b>日均退菜额: ' + chartData.avgMoney + ' 元</b><br><b>日均退菜量: ' + chartData.avgCount + ' 份</b>',
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
	
	if(cancelledDetailChartPanel && cancelledDetailChartPanel.isVisible()){
		cancelledDetailChartPanel.show();
	}
}

var cancel_setStatisticsDate = function(){
	if(sendToPageOperation){
		Ext.getCmp('cancel_dateSearchDateBegin').setValue(sendToStatisticsPageBeginDate);
		Ext.getCmp('cancel_dateSearchDateEnd').setValue(sendToStatisticsPageEndDate);		
		Ext.getCmp('cancel_btnSearch').handler();
		sendToPageOperation = false;
	}
};

var cancel_requestParams, cancel_panelDrag=false, cancelPanelHeight, cancelTabPanelHeight;
var cancelFoodStatChartTabPanel, reasonChart, cancel_detailChart, cancel_staffChart, cancel_staffChart_amount, cancel_staffColumnChart, cancel_deptChart, reasonChart_price, reasonColumnChart, cancel_deptChart_amount;
var cancelledDetailChartPanel, cancelledReasonChartPanel, cancelledStaffChartPanel, cancelledDeptChartPanel;
var colors = Highcharts.getOptions().colors;
var reasonChartData = {chartData : {type : 'pie', name : '比例', data : []}, 
					chartData1 : {type : 'pie', name : '比例', data : []},
					reasonColumnChart : {xAxis : [], 
						yAxis : {name : '退菜原因数量', 
						data : [],
						dataLabels: {
			                enabled: true,
			                color: 'green',
			                align: 'center',
			                style: {
			                    fontSize: '13px',
			                    fontFamily: 'Verdana, sans-serif',
			                    fontWeight : 'bold'
			                },
			                format: '{point.y} 份'
			            }},
			            yAxis1 : {name : '退菜原因金额', 
						data : [],
						dataLabels: {
			                enabled: true,
			                color: 'green',
			                align: 'center',
			                style: {
			                    fontSize: '13px',
			                    fontFamily: 'Verdana, sans-serif',
			                    fontWeight : 'bold'
			                },
			                format: '{point.y} 元'
			            }}
			            }};
var cancel_staffChartData = {chartData : {type : 'pie', name : '比例', data : []},
							chartAmountData : {type : 'pie', name : '比例', data : []},
							staffColumnChart : {xAxis : [], 
							yAxis : {name : '员工退菜金额', data : [],
							dataLabels: {
				                enabled: true,
				                color: 'green',
				                align: 'center',
				                style: {
				                    fontSize: '13px',
				                    fontFamily: 'Verdana, sans-serif',
				                    fontWeight : 'bold'
				                },
				                format: '{point.y} 元'
				            }},
				            yAxisAmount : {name : '员工退菜数量', data : [],
							dataLabels: {
				                enabled: true,
				                color: 'green',
				                align: 'center',
				                style: {
				                    fontSize: '13px',
				                    fontFamily: 'Verdana, sans-serif',
				                    fontWeight : 'bold'
				                },
				                format: '{point.y} 份'
				            }}}};
var cancel_deptChartData = {chartData : {type : 'pie', name : '比例', data : []}, 
							chartAmountData : {type : 'pie', name : '比例', data : []},
							deptColumnChart : {xAxis : [], 
							yAxis : {name : '部门退菜金额', data : [],
							dataLabels: {
				                enabled: true,
				                color: 'green',
				                align: 'center',
				                style: {
				                    fontSize: '13px',
				                    fontFamily: 'Verdana, sans-serif',
				                    fontWeight : 'bold'
				                },
				                format: '{point.y} 元'
				            }},
				            yAxisAmount : {name : '部门退菜数量', data : [],
							dataLabels: {
				                enabled: true,
				                color: 'green',
				                align: 'center',
				                style: {
				                    fontSize: '13px',
				                    fontFamily: 'Verdana, sans-serif',
				                    fontWeight : 'bold'
				                },
				                format: '{point.y} 份'
				            }}}};
				            
var titleCancelStaffName, titleCancelDeptName;		


var cancel_chartLoadMarsk, cancel_dateCombo;
Ext.onReady(function(){
	cancelFoodDetailsStatPanelInit();
	
	cancelledDetailChartPanel = new Ext.Panel({
		title : '退菜走势',
		contentEl : 'divCancelledDetailChart',
		listeners : {
			show : function(thiz){
				//thiz.getEl(): 刚打开页面时thiz.getWidth无效
				if(cancel_detailChart && typeof thiz.getEl() != 'undefined'){
					cancel_detailChart.setSize(thiz.getWidth(), cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
				}
			}
		}		
	});
	
	cancelledReasonChartPanel = new Ext.Panel({
		title : '原因汇总',
		contentEl : 'divCancelledReasonCharts',
		height : 320,
		listeners : {
			show : function(thiz){
				if($('#divCancelledReasonColumnChart').is(":visible")){
					reasonChart.setSize(thiz.getWidth(), cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
				}else if($('#divCancelledPriceReasonChart').is(":visible")){
					reasonChart.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
					reasonChart_price.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
				}else{
					$('#divCancelledReasonChartChange').show();
					$('#divCancelledCountReasonChart').show();
					$('#divCancelledPriceReasonChart').show();
				}
				if(!reasonChart){
					getRaasonChartData();
					loadReasonChart();
					loadPriceReasonChart();
					//第一次加载时thiz.El()未渲染
					reasonChart.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
					reasonChart_price.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
				}
			},
			render : function(thiz){
				thiz.hasRender = true;
			}
		}
	});
	
	cancelledStaffChartPanel = new Ext.Panel({
		title : '员工退菜',
		contentEl : 'divCancelledStaffCharts',
		listeners : {
			show : function(thiz){
				if($('#divCancelledStaffColumnChart').is(":visible")){
					cancel_staffColumnChart.setSize(thiz.getWidth(), cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
				}else if($('#divCancelledPriceStaffChart').is(":visible")){
					cancel_staffChart.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
					cancel_staffChart_amount.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);					
				}else{
					$('#divCancelledStaffChartChange').show();
					$('#divCancelledPriceStaffChart').show();
					$('#divCancelledCountStaffChart').show();
				}
				if(!cancel_staffChart){
					cancel_getStaffChartData();
					cancel_loadStaffChart();
					cancel_loadAmountStaffChart();
					cancel_staffChart.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
					cancel_staffChart_amount.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
				}
			},
			render : function(thiz){
				thiz.hasRender = true;
			}
		}
	});	
	
	cancelledDeptChartPanel = new Ext.Panel({
		title : '部门退菜',
		contentEl : 'divCancelledDeptCharts',
		listeners : {
			show : function(thiz){
				if($('#divCancelledDeptColumnChart').is(":visible")){
					cancel_deptChart.setSize(thiz.getWidth(), cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
				}else if($('#divCancelledPriceDeptChart').is(":visible")){
					cancel_deptChart.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
					cancel_deptChart_amount.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);				
				}else{
					$('#divCancelledDeptChartChange').show();
					$('#divCancelledCountDeptChart').show();
					$('#divCancelledPriceDeptChart').show();
				}
				if(!cancel_deptChart){
					cancel_getDeptChartData();
					cancel_loadDeptChart();
					cancel_loadAmountDeptChart();
					cancel_deptChart.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
					cancel_deptChart_amount.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
				}
			},
			render : function(thiz){
				thiz.hasRender = true;
			}
		}
	});	
	
	cancelFoodStatChartTabPanel = new Ext.TabPanel({
		region : 'south',
		height : 430,
		items : [cancelledDetailChartPanel, cancelledReasonChartPanel, cancelledStaffChartPanel, cancelledDeptChartPanel],
		listeners : {
			render : function(thiz){
				thiz.setActiveTab(cancelledDetailChartPanel);
			}
		}
	});
	
	
	
	new Ext.Panel({
		renderTo : 'divCancelledFood',//渲染到
		id : 'cancelledFoodPanel',
		//solve不跟随窗口的变化而变化
		width : parseInt(Ext.getDom('divCancelledFood').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divCancelledFood').parentElement.style.height.replace(/px/g,'')),
		layout:'border',
		frame : true, //边框
		//子集
		items : [cancelFoodDetailsStatPanel, cancelFoodStatChartTabPanel]
	});
	
	
	cancelPanelHeight = cancelFoodDetailsStatPanel.getHeight();
	
	cancelTabPanelHeight = cancelFoodStatChartTabPanel.getHeight();
	
	var cancel_rz = new Ext.Resizable(cancelFoodDetailsStatPanel.getEl(), {
        wrap: true, //在构造Resizable时自动在制定的id的外边包裹一层div
        minHeight:100, //限制改变的最小的高度
        pinned:false, //控制可拖动区域的显示状态，false是鼠标悬停在拖拉区域上才出现
        handles: 's',//设置拖拉的方向（n,s,e,w,all...）
        listeners : {
        	resize : function(thiz, w, h, e){
        		cancel_panelDrag = true;
        	}
        }
    });
    cancel_rz.on('resize', cancelFoodDetailsStatPanel.syncSize, cancelFoodDetailsStatPanel);//注册事件(作用:将调好的大小传个scope执行)
	
	
	
	$('#divCancelledReasonChartChange').toggle(
		function(){
			$('#divCancelledCountReasonChart').hide();
			$('#divCancelledPriceReasonChart').hide();
			
			$('#divCancelledReasonColumnChart').show();
			loadReasonColumnChart();
			reasonColumnChart.setSize(cancelFoodStatChartTabPanel.getWidth(), cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
		},
		function(){
			$('#divCancelledReasonColumnChart').hide();
			
			$('#divCancelledCountReasonChart').show();
			$('#divCancelledPriceReasonChart').show();
			loadReasonChart();
			loadPriceReasonChart();
			reasonChart.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
			reasonChart_price.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
		}		
	);
	
	$('#divCancelledStaffChartChange').toggle(
		function(){
			$('#divCancelledPriceStaffChart').hide();
			$('#divCancelledCountStaffChart').hide();
			
			$('#divCancelledStaffColumnChart').show();
			cancel_loadStaffColumnChart();
			cancel_staffColumnChart.setSize(cancelFoodStatChartTabPanel.getWidth(), cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
		},
		function(){
			$('#divCancelledStaffColumnChart').hide();
			
			$('#divCancelledPriceStaffChart').show();
			$('#divCancelledCountStaffChart').show();
			cancel_loadStaffChart();
			cancel_loadAmountStaffChart();
			cancel_staffChart.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
			cancel_staffChart_amount.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
		}		
	);
	
	$('#divCancelledDeptChartChange').toggle(
		function(){
			$('#divCancelledPriceDeptChart').hide();
			$('#divCancelledCountDeptChart').hide();
			
			$('#divCancelledDeptColumnChart').show();
			cancel_loadDeptColumnChart();
			cancel_deptChart.setSize(cancelFoodStatChartTabPanel.getWidth(), cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
		},
		function(){
			$('#divCancelledDeptColumnChart').hide();
			
			$('#divCancelledPriceDeptChart').show();
			$('#divCancelledCountDeptChart').show();
			cancel_loadDeptChart();
			cancel_loadAmountDeptChart();
			cancel_deptChart.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
			cancel_deptChart_amount.setSize(cancelFoodStatChartTabPanel.getWidth()/2, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - 60 : cancelFoodStatChartTabPanel.getHeight()-30);
		}		
	);
	
	cancel_chartLoadMarsk = new Ext.LoadMask(cancelFoodStatChartTabPanel.getEl().dom, {
	    msg  : '数据统计中，请稍候......',
	    disabled : false
	});	
	
    var cancel_totalHeight = Ext.getCmp('cancelledFoodPanel').getHeight();
    
    cancelFoodDetailsStatPanel.setHeight(cancel_totalHeight*0.4);
    cancelFoodDetailsStatPanel.getEl().parent().setHeight(cancel_totalHeight*0.4);
    
    cancelFoodStatChartTabPanel.setHeight(cancel_totalHeight*0.6);	
    
    cancel_rz.resizeTo(cancelFoodDetailsStatPanel.getWidth(), cancelFoodDetailsStatPanel.getHeight());
	
	Ext.getCmp('cancelledFood').updateStatisticsDate = cancel_setStatisticsDate;
});

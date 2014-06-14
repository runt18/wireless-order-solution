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
	var dateCombo = Ext.ux.createDateCombo({
		beginDate : beginDate,
		endDate : endDate,
		callback : function(){
			Ext.getCmp('btnSearchForCancelFoodDetailsStat').handler();
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
				Ext.getCmp('btnSearchForCancelFoodDetailsStat').handler();
			}
		}
	});
	
	var deptCombo = new Ext.form.ComboBox({
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
				Ext.getCmp('btnSearchForCancelFoodDetailsStat').handler();
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
					},
					fialure : function(res, opt){
						thiz.store.loadData(data);
						thiz.setValue(-1);
					}
				});
			},
			select : function(){
				Ext.getCmp('btnSearchForCancelFoodDetailsStat').handler();
			}
		}
	});
	
	var cfdsGridDateTbar = Ext.ux.initTimeBar({beginDate:beginDate, endDate:endDate,dateCombo:dateCombo, tbarType : 0, statistic : 'cancel_'});
	
	var cfdsGridTbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype:'tbtext',
			text:'&nbsp;退菜原因:'
		}, reasonCombo, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;部门: '
		},deptCombo, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;员工: '
		},cancel_combo_staffs, {
			xtype:'tbtext',
			text:'&nbsp;'
		}, '->', {
			text : '搜索',
			id : 'btnSearchForCancelFoodDetailsStat',
			iconCls : 'btn_search',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				if(bd == '' && ed == ''){
					dateCombo.setValue(0);
					dateCombo.fireEvent('select',dateCombo,null,0);
					return;
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				var gs = cfdsGrid.getStore();
				gs.baseParams['deptID'] = deptCombo.getValue();
				gs.baseParams['dateBeg'] = beginDate.getValue().format('Y-m-d 00:00:00');
				gs.baseParams['dateEnd'] = endDate.getValue().format('Y-m-d 23:59:59');
				gs.baseParams['reasonID'] = reasonCombo.getValue();
				gs.baseParams['staffID'] = cancel_combo_staffs.getValue();
				gs.load({
					params : {
						start : 0,
						limit : CANCELL_FOOD_PAGE_LIMIT
					}
				});
				
				requestParams = {
					dataSource : 'getDetailChart',
					dateBeg : beginDate.getValue().format('Y-m-d 00:00:00'),
					dateEnd : endDate.getValue().format('Y-m-d 23:59:59'),
					deptID : deptCombo.getValue(),
					reasonID : reasonCombo.getValue(),
					staffID : cancel_combo_staffs.getValue()					
				};
				
				Ext.Ajax.request({
					url : '../../QueryCancelledFood.do',
					params : requestParams,
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						showCancelDetailChart(jr);
					},
					failure : function(res, opt){
					
					}
				});
				
				//is(":visible"):第一次切换tab才去生成chart
				if(typeof reasonChart != 'undefined' && typeof cancelledReasonChartPanel.hasRender != 'undefined'){
					getRaasonChartData();
//					if(cancelledReasonChartPanel.isVisible()){
						$('#divCancelledReasonColumnChart').is(':visible')? loadReasonColumnChart() : loadReasonChart();
						reasonChart.setSize(cancelFoodStatChartTabPanel.getWidth(), cancelFoodStatChartTabPanel.getHeight()-30);
/*					}else{
						loadReasonColumnChart();
						loadReasonChart();
					}*/
					
				}
				
				if(typeof staffChart != 'undefined' && typeof cancelledStaffChartPanel.hasRender != 'undefined'){
					getStaffChartData();
					$('#divCancelledStaffColumnChart').is(':visible')? loadStaffColumnChart() : loadStaffChart();
					staffChart.setSize(cancelFoodStatChartTabPanel.getWidth(), cancelFoodStatChartTabPanel.getHeight()-30);
				}
				
				if(typeof deptChart != 'undefined' && typeof cancelledDeptChartPanel.hasRender != 'undefined'){
					getDeptChartData();
					$('#divCancelledDeptColumnChart').is(':visible')? loadDeptColumnChart() : loadDeptChart();
					deptChart.setSize(cancelFoodStatChartTabPanel.getWidth(), cancelFoodStatChartTabPanel.getHeight()-30);
				}
			}
		},{
			text : '导出',
			id : 'btnExportExcel',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				var sn = cfdsTree.getSelectionModel().getSelectedNode();
				if(bd == '' && ed == '' ){
					Ext.example.msg('提示', '未选择日期, 无法导出数据');
					return;
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				
				var url = '../../{0}?deptID={1}&dateBeg={2}&dateEnd={3}&reasonID={4}&dataSource={5}&isPaging=false&qtype=2&otype=0&dtype=1';
				
				url = String.format(
					url,
					'ExportHistoryStatisticsToExecl.do',
					(sn != null ? sn.attributes.deptID : -1),
					beginDate.getValue().format('Y-m-d 00:00:00'),
					endDate.getValue().format('Y-m-d 23:59:59'),
					reasonCombo.getValue(),
					'cancelledFood'
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
	cfdsGrid.on('render', function(){
		dateCombo.setValue(1);
		dateCombo.fireEvent('select', dateCombo, null, 1);
	});
/*	cfdsGrid.getStore().on('load', function(store, records, options){
		if(store.getCount() > 0){
			var sumRow = cfdsGrid.getView().getRow(store.getCount()-1);	
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
			cfdsGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
			cfdsGrid.getView().getCell(store.getCount()-1, 9).innerHTML = '--';
		}
	});*/
	//
	cancelFoodDetailsStatPanel = new Ext.Panel({
		title : '明细汇总',
		layout : 'border',
		region : 'center',
		items : [/*cfdsTree, */cfdsGrid],
		listeners : {
			bodyresize : function(e, w, h){
				if(typeof businessPanelHeight != 'undefined'){
				
					var chartHeight;
					if(h < businessPanelHeight){
						chartHeight = 300 + (businessPanelHeight - h);
					}else{
						chartHeight = 300 + (h - businessPanelHeight);
					}
					changeChartWidth(w,(chartHeight-30));
					
					cancelFoodStatChartTabPanel.getEl().setTop((h+30)) ;
					
					if(panelDrag){
						cancelFoodStatChartTabPanel.setHeight(chartHeight);
					}
					
	//				receivablesStatResultGrid.getEl().parent().setWidth(w);
					cancelFoodDetailsStatPanel.doLayout();
				}
			}
		}
	});
}

function changeChartWidth(w,h){
	eval($('div:visible[data-type=chart]').attr('data-value')).setSize(w, h);
}

function getRaasonChartData(){
	requestParams.dataSource = 'getReasonChart';
	$.ajax({
		url : '../../QueryCancelledFood.do',
		type : 'post',
		async : false,
		data : requestParams,
		success : function(jr, status, xhr){
			reasonChartData.chartData.data = [];
			reasonChartData.reasonColumnChart.xAxis = [];
			reasonChartData.reasonColumnChart.yAxis.data = [];
			for (var i = 0; i < jr.root.length; i++) {
				reasonChartData.chartData.data.push([jr.root[i].reason, jr.root[i].cancelAmount]);
				reasonChartData.reasonColumnChart.xAxis.push(jr.root[i].reason);
				reasonChartData.reasonColumnChart.yAxis.data.push({y : jr.root[i].cancelAmount, color : colors[i]}); 
			}
		},
		failure : function(res, opt){
		
		}
	});
	
}

function getStaffChartData(){
	requestParams.dataSource = 'getStaffChart';
	$.ajax({
		url : '../../QueryCancelledFood.do',
		type : 'post',
		async : false,
		data : requestParams,
		success : function(jr, status, xhr){
			staffChartData.chartData.data = [];
			staffChartData.staffColumnChart.xAxis = [];
			staffChartData.staffColumnChart.yAxis.data = [];
			for (var i = 0; i < jr.root.length; i++) {
				staffChartData.chartData.data.push([jr.root[i].cancelStaff, jr.root[i].cancelPrice]);
				staffChartData.staffColumnChart.xAxis.push(jr.root[i].cancelStaff);
				staffChartData.staffColumnChart.yAxis.data.push({y : jr.root[i].cancelPrice, color : colors[i]}); 
			}
		},
		failure : function(res, opt){
		
		}
	});
	
}

function getDeptChartData(){
	requestParams.dataSource = 'getDeptChart';
	$.ajax({
		url : '../../QueryCancelledFood.do',
		type : 'post',
		async : false,
		data : requestParams,
		success : function(jr, status, xhr){
			deptChartData.chartData.data = [];
			deptChartData.deptColumnChart.xAxis = [];
			deptChartData.deptColumnChart.yAxis.data = [];
			for (var i = 0; i < jr.root.length; i++) {
				deptChartData.chartData.data.push([jr.root[i].cancelDept.name, jr.root[i].cancelPrice]);
				deptChartData.deptColumnChart.xAxis.push(jr.root[i].cancelDept.name);
				deptChartData.deptColumnChart.yAxis.data.push({y : jr.root[i].cancelPrice, color : colors[i]}); 
			}
		},
		failure : function(res, opt){
		
		}
	});
	
}


function loadReasonColumnChart(){
 	reasonChart = new Highcharts.Chart({
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
                text: '数量 (份)'
            }
        },
        tooltip: {
            pointFormat: '<table><tbody><tr><td style="color:red;padding:0">数量: </td><td style="padding:0"><b>{point.y} 份</b></td></tr></tbody></table>',
            shared: true,
            useHTML: true
        },
        plotOptions: {
            column: {
                pointPadding: 0.2,
                borderWidth: 0
            }
        },
        series: [reasonChartData.reasonColumnChart.yAxis]
    });	
}

function loadStaffColumnChart(){
 	staffChart = new Highcharts.Chart({
        chart: {
            type: 'column',
            renderTo : 'divCancelledStaffColumnChart'
        },
        title: {
            text: '员工退菜金额柱状图'
        },
        xAxis: {
            categories: staffChartData.staffColumnChart.xAxis
        },
        yAxis: {
            min: 0,
            title: {
                text: '金额 (份)'
            }
        },
        tooltip: {
            pointFormat: '<table><tbody><tr><td style="color:red;padding:0">金额: </td><td style="padding:0"><b>{point.y} 份</b></td></tr></tbody></table>',
            shared: true,
            useHTML: true
        },
        plotOptions: {
            column: {
                pointPadding: 0.2,
                borderWidth: 0
            }
        },
        series: [staffChartData.staffColumnChart.yAxis]
    });	
}

function loadDeptColumnChart(){
 	deptChart = new Highcharts.Chart({
        chart: {
            type: 'column',
            renderTo : 'divCancelledDeptColumnChart'
        },
        title: {
            text: '部门退菜金额柱状图'
        },
        xAxis: {
            categories: deptChartData.deptColumnChart.xAxis
        },
        yAxis: {
            min: 0,
            title: {
                text: '金额 (份)'
            }
        },
        tooltip: {
            pointFormat: '<table><tbody><tr><td style="color:red;padding:0">金额: </td><td style="padding:0"><b>{point.y} 份</b></td></tr></tbody></table>',
            shared: true,
            useHTML: true
        },
        plotOptions: {
            column: {
                pointPadding: 0.2,
                borderWidth: 0
            }
        },
        series: [deptChartData.deptColumnChart.yAxis]
    });	
}

function loadReasonChart(){
	reasonChart = new Highcharts.Chart({
	    chart: {
	    	renderTo : 'divCancelledReasonChart',
	        plotBackgroundColor: null,
	        plotBorderWidth: null,
	        plotShadow: false
	    },
	    title: {
	        text: '原因比例图'
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
	                format: '<b>{point.name}</b>: {point.percentage:.1f} %'
	            }
	        }
	    },
	    series: [reasonChartData.chartData]
	});				
	
}

function loadStaffChart(){
	
	staffChart = new Highcharts.Chart({
	    chart: {
	    	renderTo : 'divCancelledStaffChart',
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
	                format: '<b>{point.name}</b>: {point.percentage:.1f} %'
	            }
	        }
	    },
	    series: [staffChartData.chartData]
	});				
}

function loadDeptChart(){
	deptChart = new Highcharts.Chart({
	    chart: {
	    	renderTo : 'divCancelledDeptChart',
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
	                format: '<b>{point.name}</b>: {point.percentage:.1f} %'
	            }
	        }
	    },
	    series: [deptChartData.chartData]
	});				
}

function showCancelDetailChart(jdata){
	var dateBegin = Ext.getCmp('cancel_dateSearchDateBegin').getValue().format('Y-m-d');
	var dateEnd = Ext.getCmp('cancel_dateSearchDateEnd').getValue().format('Y-m-d');
	
	var hourBegin = Ext.getCmp('cancel_txtBusinessHourBegin').getEl().dom.textContent;
	var hourEnd = Ext.getCmp('cancel_txtBusinessHourEnd').getEl().dom.textContent;
	
	var chartData = eval('(' + jdata.other.chart + ')');
	detailChart = new Highcharts.Chart({
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
            text: '<b>退菜走势图（'+dateBegin+ '至' +dateEnd+'）'+hourBegin+ ' - ' + hourEnd + '</b>'
        },
        labels: {
        	items : [{
        		html : '<b>总退菜金额:' + chartData.totalMoney + ' 元</b><br><b>日均退菜额:' + chartData.avgMoney + ' 元</b>',
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
                    this.x +': '+ '<b>'+this.y+'</b> ';
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
}

var requestParams;
var cancelFoodStatChartTabPanel, reasonChart, detailChart, staffChart, deptChart;
var cancelledReasonChartPanel, cancelledStaffChartPanel, cancelledDeptChartPanel;
var colors = Highcharts.getOptions().colors;
var reasonChartData = {chartData : {type : 'pie', name : '比例', data : []}, reasonColumnChart : {xAxis : [], yAxis : {name : '退菜原因', data : []}}};
var staffChartData = {chartData : {type : 'pie', name : '比例', data : []}, staffColumnChart : {xAxis : [], yAxis : {name : '员工退菜', data : []}}};
var deptChartData = {chartData : {type : 'pie', name : '比例', data : []}, deptColumnChart : {xAxis : [], yAxis : {name : '部门退菜', data : []}}};
Ext.onReady(function(){
	cancelFoodDetailsStatPanelInit();
	
	var cancelledDetailChartPanel = new Ext.Panel({
		title : '退菜走势',
		contentEl : 'divCancelledDetailChart',
		listeners : {
			show : function(thiz){
				//thiz.getEl(): 刚打开页面时thiz.getWidth无效
				if(detailChart && typeof thiz.getEl() != 'undefined'){
					detailChart.setSize(thiz.getWidth(), thiz.getHeight());
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
				if($('#divCancelledReasonChart').is(":visible") || $('#divCancelledReasonColumnChart').is(":visible")){
					reasonChart.setSize(thiz.getWidth(), thiz.getHeight());
				}else{
					$('#divCancelledReasonChartChange').show();
					$('#divCancelledReasonChart').show();
				}
				if(!reasonChart){
					getRaasonChartData();
					loadReasonChart();
					//第一次加载时thiz.El()未渲染
					reasonChart.setSize(cancelFoodStatChartTabPanel.getWidth(), cancelFoodStatChartTabPanel.getHeight()-30);
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
				if($('#divCancelledStaffChart').is(":visible") || $('#divCancelledStaffColumnChart').is(":visible")){
					staffChart.setSize(thiz.getWidth(), thiz.getHeight());
				}else{
					$('#divCancelledStaffChartChange').show();
					$('#divCancelledStaffChart').show();
				}
				if(!staffChart){
					getStaffChartData();
					loadStaffChart();
					
					staffChart.setSize(cancelFoodStatChartTabPanel.getWidth(), cancelFoodStatChartTabPanel.getHeight()-30);
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
				if($('#divCancelledDeptChart').is(":visible") || $('#divCancelledDeptColumnChart').is(":visible")){
					deptChart.setSize(thiz.getWidth(), thiz.getHeight());
				}else{
					$('#divCancelledDeptChartChange').show();
					$('#divCancelledDeptChart').show();
				}
				if(!deptChart){
					getDeptChartData();
					loadDeptChart();
					
					deptChart.setSize(cancelFoodStatChartTabPanel.getWidth(), cancelFoodStatChartTabPanel.getHeight()-30);
				}
			},
			render : function(thiz){
				thiz.hasRender = true;
			}
		}
	});	
	
	cancelFoodStatChartTabPanel = new Ext.TabPanel({
		region : 'south',
		height : 330,
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
	
	
	businessPanelHeight = cancelFoodDetailsStatPanel.getHeight();
	
	var rz = new Ext.Resizable(cancelFoodDetailsStatPanel.getEl(), {
        wrap: true, //在构造Resizable时自动在制定的id的外边包裹一层div
        minHeight:100, //限制改变的最小的高度
        pinned:false, //控制可拖动区域的显示状态，false是鼠标悬停在拖拉区域上才出现
        handles: 's',//设置拖拉的方向（n,s,e,w,all...）
        listeners : {
        	resize : function(thiz, w, h, e){
        		panelDrag = true;
        	}
        }
    });
    rz.on('resize', cancelFoodDetailsStatPanel.syncSize, cancelFoodDetailsStatPanel);//注册事件(作用:将调好的大小传个scope执行)
	
	
	
	$('#divCancelledReasonChartChange').toggle(
		function(){
			$('#divCancelledReasonChart').hide();
			
			$('#divCancelledReasonColumnChart').show();
			loadReasonColumnChart();
			reasonChart.setSize(cancelFoodStatChartTabPanel.getWidth(), cancelFoodStatChartTabPanel.getHeight()-30);
		},
		function(){
			$('#divCancelledReasonColumnChart').hide();
			
			$('#divCancelledReasonChart').show();
			loadReasonChart();
			reasonChart.setSize(cancelFoodStatChartTabPanel.getWidth(), cancelFoodStatChartTabPanel.getHeight()-30);
		}		
	);
	
	$('#divCancelledStaffChartChange').toggle(
		function(){
			$('#divCancelledStaffChart').hide();
			
			$('#divCancelledStaffColumnChart').show();
			loadStaffColumnChart();
			staffChart.setSize(cancelFoodStatChartTabPanel.getWidth(), cancelFoodStatChartTabPanel.getHeight()-30);
		},
		function(){
			$('#divCancelledStaffColumnChart').hide();
			
			$('#divCancelledStaffChart').show();
			loadStaffChart();
			staffChart.setSize(cancelFoodStatChartTabPanel.getWidth(), cancelFoodStatChartTabPanel.getHeight()-30);
		}		
	);
	$('#divCancelledDeptChartChange').toggle(
		function(){
			$('#divCancelledDeptChart').hide();
			
			$('#divCancelledDeptColumnChart').show();
			loadDeptColumnChart();
			deptChart.setSize(cancelFoodStatChartTabPanel.getWidth(), cancelFoodStatChartTabPanel.getHeight()-30);
		},
		function(){
			$('#divCancelledDeptColumnChart').hide();
			
			$('#divCancelledDeptChart').show();
			loadDeptChart();
			deptChart.setSize(cancelFoodStatChartTabPanel.getWidth(), cancelFoodStatChartTabPanel.getHeight()-30);
		}		
	);	
	
/*	$('.changeChart').click(function(){
		eval($('div:visible[data-type=chart]').attr('data-value')).setSize(cancelFoodStatChartTabPanel.getWidth(), cancelFoodStatChartTabPanel.getHeight()-30);
	});*/
});

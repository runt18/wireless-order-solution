//初始化区域combo
function initRegionCombo(statistic){
	var combo = {
		xtype : 'combo',
		forceSelection : true,
		width : 90,
		value : -1,
		id : statistic+'comboRegion',
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
					url : '../../QueryRegion.do',
					params : {
						dataSource : 'normal'
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						for(var i = 0; i < jr.root.length; i++){
							data.push([jr.root[i]['id'], jr.root[i]['name']]);
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
			select : function(thiz, record, index){
				Ext.getCmp(statistic+'btnSearch').handler();
			}
		}
	};
	return combo;
}
function gift_linkOrderId(v){
	if(!isNaN(v)){
		return '<a href=\"javascript:gift_showBillDetailWin('+ v +')\">'+ v +'</a>';
	}else{
		return v;
	}
}

function gift_showBillDetailWin(orderID){
	giftViewBillWin = new Ext.Window({
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
				giftViewBillWin.destroy();
			}
		}],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				giftViewBillWin.destroy();
			}
		}],
		listeners : {
			show : function(thiz) {
				var sd = Ext.ux.getSelData(grid_giftStatistics);
				thiz.load({
					url : '../window/history/viewBillDetail.jsp', 
					scripts : true,
					params : {
						orderId : sd.orderId,
						queryType : 'History'
					},
					method : 'post'
				});
				thiz.center();	
			}
		}
	});
	giftViewBillWin.show();
	giftViewBillWin.center();
}
function initGiftStatisticsGrid(){
	var beginDate = new Ext.form.DateField({
		id : 'gift_dateSearchDateBegin',
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(true, thiz.getId(), endDate.getId());
			}
		}
	});
	var endDate = new Ext.form.DateField({
		id : 'gift_dateSearchDateEnd',
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(false, beginDate.getId(), thiz.getId());
			}
		}
	});
	gift_dateCombo = Ext.ux.createDateCombo({
		beginDate : beginDate,
		endDate : endDate,
		callback : function(){
			Ext.getCmp('giftStatistic_btnSearch').handler();
		}
	});
	
	var gift_combo_staffs = new Ext.form.ComboBox({
		id : 'gift_combo_staffs',
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
					params : {privileges : '1003'},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						for(var i = 0; i < jr.root.length; i++){
							data.push([jr.root[i]['staffID'], jr.root[i]['staffName']]);
						}
						thiz.store.loadData(data);
						thiz.setValue(-1);

						if(sendToPageOperation){
							gift_setStatisticsDate();
						}else{
							gift_dateCombo.setValue(1);
							gift_dateCombo.fireEvent('select', gift_dateCombo, null, 1);				
						}							
					},
					fialure : function(res, opt){
						thiz.store.loadData(data);
						thiz.setValue(-1);
					}
				});
			},
			select : function(){
				Ext.getCmp('giftStatistic_btnSearch').handler();
			}
		}
	});
	
	var grid_giftStatisticsTbar = new Ext.Toolbar({
		items : [{
			xtype:'tbtext',
			text:'日期:'
		}, gift_dateCombo, {
			xtype:'tbtext',
			text:'&nbsp;'
		}, beginDate, {
			xtype:'tbtext',
			text:'&nbsp;至&nbsp;'
		}, endDate, {
			xtype:'tbtext',
			text:'&nbsp;'
		},{
			xtype : 'tbtext',
			text : '区域:'
		}, initRegionCombo('giftStatistic_'),
		{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;菜品名称:'
		},{
			xtype : 'textfield',
			id : 'gift_foodName',
			width : 100
		},{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;赠送人:'
		},gift_combo_staffs, '->', {
			text : '搜索',
			id : 'giftStatistic_btnSearch',
			iconCls : 'btn_search',
			handler : function(){
				if(!beginDate.isValid() || !endDate.isValid()){
					return;
				}
				var gs = grid_giftStatistics.getStore();
				gs.baseParams['onDuty'] = beginDate.getValue().format('Y-m-d 00:00:00');
				gs.baseParams['offDuty'] = endDate.getValue().format('Y-m-d 23:59:59');
				gs.baseParams['region'] = Ext.getCmp('giftStatistic_comboRegion').getValue();
				gs.baseParams['foodName'] = Ext.getCmp('gift_foodName').getValue();
				gs.baseParams['giftStaffId'] = gift_combo_staffs.getValue();
				gs.load({
					params : {
						start : 0,
						limit : GRID_PADDING_LIMIT_20
					}
				});
			
				if(gift_combo_staffs.getValue() && gift_combo_staffs.getValue() != -1){
					titleGiftStaffName = ' 操作员 : ' + gift_combo_staffs.getEl().dom.value;
				}else{
					titleGiftStaffName = '';
				}				
			
				requestParams = {
					dataSource : 'getDetailChart',
					dateBeg : beginDate.getValue().format('Y-m-d 00:00:00'),
					dateEnd : endDate.getValue().format('Y-m-d 23:59:59'),
					region : Ext.getCmp('giftStatistic_comboRegion').getValue(),
					giftStaffId : gift_combo_staffs.getValue(),
					foodName : Ext.getCmp('gift_foodName').getValue()
				};
				gift_chartLoadMarsk.show();
				Ext.Ajax.request({
					url : '../../QueryGiftStatistic.do',
					params : requestParams,
					success : function(res, opt){
						gift_chartLoadMarsk.hide();
						
						var jr = Ext.decode(res.responseText);
						showGiftDetailChart(jr);
					},
					failure : function(res, opt){
					
					}
				});	
				
				if(typeof gift_staffPieChart != 'undefined' && typeof giftStaffChartPanel.hasRender != 'undefined'){
					gift_getStaffChartData();
					gift_loadStaffAmountChart(giftStaffChartPanel.otype);
					gift_loadStaffColumnChart(giftStaffChartPanel.otype);
					gift_staffPieChart.setSize(giftStatChartTabPanel.getWidth()/2, gift_panelDrag ? giftStatChartTabPanel.getHeight() - 65 : giftStatChartTabPanel.getHeight()-30);
					gift_staffColumnChart.setSize(giftStatChartTabPanel.getWidth()/2, gift_panelDrag ? giftStatChartTabPanel.getHeight() - 65 : giftStatChartTabPanel.getHeight()-30);					
					
				}
				
				if(typeof gift_deptPieChart != 'undefined' && typeof giftDeptChartPanel.hasRender != 'undefined'){
					gift_getDeptChartData();
					gift_loadDeptAmountChart(giftDeptChartPanel.otype);
					gift_loadDeptColumnChart(giftDeptChartPanel.otype);
					gift_deptPieChart.setSize(giftStatChartTabPanel.getWidth()/2, gift_panelDrag ? giftStatChartTabPanel.getHeight() - 65 : giftStatChartTabPanel.getHeight()-30);
					gift_deptColumnChart.setSize(giftStatChartTabPanel.getWidth()/2, gift_panelDrag ? giftStatChartTabPanel.getHeight() - 65 : giftStatChartTabPanel.getHeight()-30);					
					
				}
				
				
			}
		}]
	});
	
	grid_giftStatistics = createGridPanel(
		'',
		'',
		'',
	    '',
	    '../../QueryGiftStatistic.do',
	    [
		    [true, false, false, false], 
		    ['单据编号','orderId',100,,'gift_linkOrderId'],
		    ['赠送日期','orderDateFormat',100],
		    ['菜品名称','name', 100], 
		    ['数量','count', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
		    ['单价','unitPrice', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
		    ['总价','actualPrice', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
		    ['赠送人','waiter', ,'center']
		],
		['orderId', 'orderDateFormat', 'name', 'count', 'unitPrice', 'actualPrice', 'waiter'],
	    [ ['dataSource', 'normal']],
	    GRID_PADDING_LIMIT_20,
	    '',
	    grid_giftStatisticsTbar
	);
	
	grid_giftStatistics.frame = false;
	grid_giftStatistics.border = false;
	grid_giftStatistics.region = 'center';
	
	giftDetailsStatPanel = new Ext.Panel({
		title : '赠送明细',
		layout : 'border',
		region : 'center',
		items : [grid_giftStatistics],
		listeners : {
			bodyresize : function(e, w, h){
				if(typeof giftPanelHeight != 'undefined'){
					var chartHeight = gift_tabPanelHeight + (giftPanelHeight - h);
					
					giftStatChartTabPanel.getEl().setTop((h+30)) ;
					
					gift_changeChartWidth(w,chartHeight-70);
					
					if(gift_panelDrag){
						giftStatChartTabPanel.setHeight(chartHeight);
					}
					giftStatChartTabPanel.doLayout();					
				}
			}
		}
	});	
//	grid_giftStatistics.on('render', function(){
//		dateCombo.setValue(1);
//		dateCombo.fireEvent('select', dateCombo, null, 1);
//	});
}

function gift_changeChartWidth(w,h){
	if(eval($('div:visible[data-type=giftChart]').attr('data-value'))){
		if($('div:visible[data-type=giftChart]').length == 1){
			eval($('div:visible[data-type=giftChart]').attr('data-value')).setSize(w, h);
		}else if($('div:visible[data-type=giftChart]').length > 1){
			eval($($('div:visible[data-type=giftChart]')[0]).attr('data-value')).setSize(w/2, h);
			eval($($('div:visible[data-type=giftChart]')[1]).attr('data-value')).setSize(w/2, h);				
		}
	}	
}

function showGiftDetailChart(jdata){
	var dateBegin = Ext.getCmp('gift_dateSearchDateBegin').getValue().format('Y-m-d');
	var dateEnd = Ext.getCmp('gift_dateSearchDateEnd').getValue().format('Y-m-d');
	
/*	var hourBegin = Ext.getCmp('gift_txtBusinessHourBegin').getEl().dom.textContent;
	var hourEnd = Ext.getCmp('gift_txtBusinessHourEnd').getEl().dom.textContent;*/
	
	var chartData = eval('(' + jdata.other.chart + ')');
	gift_detailChart = new Highcharts.Chart({
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
        	renderTo: 'divGiftDetailChart'
    	}, 
        title: {
            text: '<b>赠送走势图（'+dateBegin+ '至' +dateEnd+'）'+titleGiftStaffName+'</b>'
        },
        labels: {
        	items : [{
        		html : '<b>总赠送金额:' + chartData.totalMoney + ' 元</b><br><b>日均赠送金额:' + chartData.avgMoney + ' 元</b><br><b>日均赠送数量:' + chartData.avgCount + ' 份</b>',
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
	
	if(giftDetailChartPanel && giftDetailChartPanel.isVisible()){
		giftDetailChartPanel.show();
	}
}

function gift_getStaffChartData(){
	requestParams.dataSource = 'getStaffChart';
	$.ajax({
		url : '../../QueryGiftStatistic.do',
		type : 'post',
		async : false,
		data : requestParams,
		success : function(jr, status, xhr){
			gift_chartLoadMarsk.hide();
			gift_staffChartData.chartPriceData.data = [];
			gift_staffChartData.chartAmountData.data = [];
			gift_staffChartData.staffPriceColumnChart.xAxis = [];
			gift_staffChartData.staffAmountColumnChart.xAxis = [];
			gift_staffChartData.staffPriceColumnChart.yAxis.data = [];
			gift_staffChartData.staffAmountColumnChart.yAxisAmount.data = [];
			
			
			for (var i = 0; i < jr.root.length; i++) {
				gift_staffChartData.chartPriceData.data.push([jr.root[i].giftStaff, jr.root[i].giftPrice]);
				gift_staffChartData.chartAmountData.data.push([jr.root[i].giftStaff, jr.root[i].giftAmount]);
				gift_staffChartData.staffAmountColumnChart.xAxis.push(jr.root[i].giftStaff);
				gift_staffChartData.staffPriceColumnChart.xAxis.push(jr.root[i].giftStaff);
				gift_staffChartData.staffPriceColumnChart.yAxis.data.push({y : jr.root[i].giftPrice, color : colors[i]}); 
				gift_staffChartData.staffAmountColumnChart.yAxisAmount.data.push({y : jr.root[i].giftAmount, color : colors[i]});
			}
		},
		failure : function(res, opt){
		
		}
	});
	
}


function gift_getDeptChartData(){
	requestParams.dataSource = 'getDeptChart';
	$.ajax({
		url : '../../QueryGiftStatistic.do',
		type : 'post',
		async : false,
		data : requestParams,
		success : function(jr, status, xhr){
			gift_chartLoadMarsk.hide();
			gift_deptChartData.chartPriceData.data = [];
			gift_deptChartData.chartAmountData.data = [];
			gift_deptChartData.deptPriceColumnChart.xAxis = [];
			gift_deptChartData.deptAmountColumnChart.xAxis = [];
			gift_deptChartData.deptPriceColumnChart.yAxis.data = [];
			gift_deptChartData.deptAmountColumnChart.yAxisAmount.data = [];
			
			
			for (var i = 0; i < jr.root.length; i++) {
				gift_deptChartData.chartPriceData.data.push([jr.root[i].giftDept.name, jr.root[i].giftPrice]);
				gift_deptChartData.chartAmountData.data.push([jr.root[i].giftDept.name, jr.root[i].giftAmount]);
				gift_deptChartData.deptAmountColumnChart.xAxis.push(jr.root[i].giftDept.name);
				gift_deptChartData.deptPriceColumnChart.xAxis.push(jr.root[i].giftDept.name);
				gift_deptChartData.deptPriceColumnChart.yAxis.data.push({y : jr.root[i].giftPrice, color : colors[i]}); 
				gift_deptChartData.deptAmountColumnChart.yAxisAmount.data.push({y : jr.root[i].giftAmount, color : colors[i]});
			}
		},
		failure : function(res, opt){
		
		}
	});
	
}

//type=0:金额, type=1:数量
function gift_loadStaffAmountChart(type){
	gift_staffPieChart = new Highcharts.Chart({
	    chart: {
	    	renderTo : 'divGiftStaffAmountPieChart',
	        plotBackgroundColor: null,
	        plotBorderWidth: null,
	        plotShadow: false
	    },
	    title: {
	        text: type==1?'员工赠送数量比例图':'员工赠送金额比例图'
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
	                format: '<b>{point.name}</b>: {point.y} '+(type==1?'份':'元')
	            }
	        }
	    },
	    series: [(type==1?gift_staffChartData.chartAmountData:gift_staffChartData.chartPriceData)],
        credits : {
        	enabled : false
        }
	});				
}

function gift_loadStaffColumnChart(type){
 	gift_staffColumnChart = new Highcharts.Chart({
        chart: {
            type: 'column',
            renderTo : 'divGiftStaffAmountColumnChart'
        },
        title: {
            text: type==1?'员工赠送数量柱状图':'员工赠送金额柱状图'
        },
        xAxis: {
            categories: gift_staffChartData.staffPriceColumnChart.xAxis
        },
        yAxis: {
            min: 0,
            title: {
                text: type==1?'数量 (份)':'金额 (元)'
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
        series: [(type==1?gift_staffChartData.staffAmountColumnChart.yAxisAmount : gift_staffChartData.staffPriceColumnChart.yAxis) ],
        credits : {
        	enabled : false
        }
    });	
	giftStaffChartPanel.otype = type;
}

//type=0:金额, type=1:数量
function gift_loadDeptAmountChart(type){
	gift_deptPieChart = new Highcharts.Chart({
	    chart: {
	    	renderTo : 'divGiftDeptPieChart',
	        plotBackgroundColor: null,
	        plotBorderWidth: null,
	        plotShadow: false
	    },
	    title: {
	        text: type==1?'部门赠送数量比例图':'部门赠送金额比例图'
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
	                format: '<font style="font-weight:bold;font-size:15px;">{point.name}</font>: {point.y} '+(type==1?'份':'元')
	            }
	        }
	    },
	    series: [(type==1?gift_deptChartData.chartAmountData : gift_deptChartData.chartPriceData)],
        credits : {
        	enabled : false
        }
	});		
}

function gift_loadDeptColumnChart(type){
 	gift_deptColumnChart = new Highcharts.Chart({
        chart: {
            type: 'column',
            renderTo : 'divGiftDeptColumnChart'
        },
        title: {
            text: type==1?'部门赠送数量柱状图':'部门赠送金额柱状图'
        },
        xAxis: {
            categories: gift_deptChartData.deptPriceColumnChart.xAxis
        },
        yAxis: {
            min: 0,
            title: {
                text: type==1?'数量 (份)':'金额 (元)'
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
        series: [(type==1?gift_deptChartData.deptAmountColumnChart.yAxisAmount : gift_deptChartData.deptPriceColumnChart.yAxis) ],
        credits : {
        	enabled : false
        }
    });	
    giftDeptChartPanel.otype = type;
}

function fnChangeStaffChart(v){
	gift_loadStaffAmountChart(v);
	gift_loadStaffColumnChart(v);
	gift_staffPieChart.setSize(giftStatChartTabPanel.getWidth()/2, gift_panelDrag ? giftStatChartTabPanel.getHeight() - 65 : giftStatChartTabPanel.getHeight()-30);
	gift_staffColumnChart.setSize(giftStatChartTabPanel.getWidth()/2, gift_panelDrag ? giftStatChartTabPanel.getHeight() - 65 : giftStatChartTabPanel.getHeight()-30);
}

function fnChangeDeptChart(v){
	gift_loadDeptAmountChart(v);
	gift_loadDeptColumnChart(v);
	gift_deptPieChart.setSize(giftStatChartTabPanel.getWidth()/2, gift_panelDrag ? giftStatChartTabPanel.getHeight() - 65 : giftStatChartTabPanel.getHeight()-30);
	gift_deptColumnChart.setSize(giftStatChartTabPanel.getWidth()/2, gift_panelDrag ? giftStatChartTabPanel.getHeight() - 65 : giftStatChartTabPanel.getHeight()-30);
}

var gift_setStatisticsDate = function(){
	if(sendToPageOperation){
		Ext.getCmp('gift_dateSearchDateBegin').setValue(sendToStatisticsPageBeginDate);
		Ext.getCmp('gift_dateSearchDateEnd').setValue(sendToStatisticsPageEndDate);		
		Ext.getCmp('giftStatistic_btnSearch').handler();
		sendToPageOperation = false;		
	}

};

var giftDetailsStatPanel, giftStatChartTabPanel, giftDetailChartPanel, giftStaffChartPanel, giftDeptChartPanel;
var gift_detailChart, gift_staffPieChart, gift_staffColumnChart, gift_deptPieChart, gift_deptColumnChart;
var requestParams, gift_dateCombo;
var gift_panelDrag = false, colors = Highcharts.getOptions().colors, gift_chartLoadMarsk;
var giftPanelHeight, gift_tabPanelHeight;
var titleGiftStaffName;

var gift_staffChartData = {chartPriceData : {type : 'pie', name : '比例', data : []}, 
							chartAmountData : {type : 'pie', name : '比例', data : []},
							staffPriceColumnChart : {xAxis : [], 
							yAxis : {name : '员工赠送金额', data : [],
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
				            }}},
				            staffAmountColumnChart : {xAxis : [], 
							yAxisAmount : {name : '员工赠送数量', data : [],
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
var gift_deptChartData = {chartPriceData : {type : 'pie', name : '比例', data : []}, 
							chartAmountData : {type : 'pie', name : '比例', data : []},
							deptPriceColumnChart : {xAxis : [], 
							yAxis : {name : '部门赠送金额', data : [],
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
				            }}},
				            deptAmountColumnChart : {xAxis : [], 
							yAxisAmount : {name : '部门赠送数量', data : [],
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
				            
Ext.onReady(function(){
	
	giftDetailChartPanel = new Ext.Panel({
		title : '赠送走势',
		contentEl : 'divGiftDetailChart',
		listeners : {
			show : function(thiz){
				//thiz.getEl(): 刚打开页面时thiz.getWidth无效
				if(gift_detailChart && typeof thiz.getEl() != 'undefined'){
					gift_detailChart.setSize(thiz.getWidth(), gift_panelDrag ? giftStatChartTabPanel.getHeight() - 60 : giftStatChartTabPanel.getHeight()-30);
				}
			}
			
		}		
	});

	giftStaffChartPanel = new Ext.Panel({
		title : '按员工汇总',
		contentEl : 'divGiftStaffCharts',
		listeners : {
			show : function(thiz){
				if($('#divGiftStaffAmountColumnChart').is(":visible")){
					gift_staffPieChart.setSize(giftStatChartTabPanel.getWidth()/2, gift_panelDrag ? giftStatChartTabPanel.getHeight() - 65 : giftStatChartTabPanel.getHeight()-40);
					gift_staffColumnChart.setSize(giftStatChartTabPanel.getWidth()/2, gift_panelDrag ? giftStatChartTabPanel.getHeight() - 65 : giftStatChartTabPanel.getHeight()-40);				
				}else{
					$('#divGiftStaffChartChange').show();
					$('#divGiftStaffAmountPieChart').show();
					$('#divGiftStaffAmountColumnChart').show();
				}
				if(!gift_staffPieChart){
					gift_getStaffChartData();
					gift_loadStaffAmountChart(1);
					gift_loadStaffColumnChart(1);
					gift_staffPieChart.setSize(giftStatChartTabPanel.getWidth()/2, gift_panelDrag ? giftStatChartTabPanel.getHeight() - 65 : giftStatChartTabPanel.getHeight()-40);
					gift_staffColumnChart.setSize(giftStatChartTabPanel.getWidth()/2, gift_panelDrag ? giftStatChartTabPanel.getHeight() - 65 : giftStatChartTabPanel.getHeight()-40);
				}
			},
			render : function(thiz){
				thiz.hasRender = true;
			}
		}
	});		
	
	giftDeptChartPanel = new Ext.Panel({
		title : '按部门汇总',
		contentEl : 'divGiftDeptCharts',
		listeners : {
			show : function(thiz){
				if($('#divGiftDeptPieChart').is(":visible")){
					gift_deptPieChart.setSize(giftStatChartTabPanel.getWidth()/2, gift_panelDrag ? giftStatChartTabPanel.getHeight() - 65 : giftStatChartTabPanel.getHeight()-40);
					gift_deptColumnChart.setSize(giftStatChartTabPanel.getWidth()/2, gift_panelDrag ? giftStatChartTabPanel.getHeight() - 65 : giftStatChartTabPanel.getHeight()-40);				
				}else{
					$('#divGiftDeptChartChange').show();
					$('#divGiftDeptPieChart').show();
					$('#divGiftDeptColumnChart').show();
				}
				if(!gift_deptPieChart){
					gift_getDeptChartData();
					gift_loadDeptAmountChart(0);
					gift_loadDeptColumnChart(0);
					gift_deptPieChart.setSize(giftStatChartTabPanel.getWidth()/2, gift_panelDrag ? giftStatChartTabPanel.getHeight() - 65 : giftStatChartTabPanel.getHeight()-40);
					gift_deptColumnChart.setSize(giftStatChartTabPanel.getWidth()/2, gift_panelDrag ? giftStatChartTabPanel.getHeight() - 65 : giftStatChartTabPanel.getHeight()-40);
				}
			},
			render : function(thiz){
				thiz.hasRender = true;
			}
		}		
	});
	
	giftStatChartTabPanel = new Ext.TabPanel({
		title : '图形报表',
//		collapsible : true,
//		titleCollapse : true,
		region : 'south',
		height : 430,
		items : [giftDetailChartPanel, giftStaffChartPanel, giftDeptChartPanel],
		listeners : {
			render : function(thiz){
				thiz.setActiveTab(giftDetailChartPanel);
			},
			collapse : function(){
				giftDetailsStatPanel.getEl().parent().setHeight(giftPanelHeight+gift_tabPanelHeight);
			},
			expand : function(thiz){
				gift_rz.resizeTo(thiz.getWidth(), giftPanelHeight);
			}
		}
	});	
	
	initGiftStatisticsGrid();
	
	new Ext.Panel({
		renderTo : 'divGiftStatistics',
		id : 'giftStatistics',
		frame : false,
		layout : 'border',
		width : parseInt(Ext.getDom('divGiftStatistics').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divGiftStatistics').parentElement.style.height.replace(/px/g,'')),
		items : [giftDetailsStatPanel, giftStatChartTabPanel]
	});
	
	gift_chartLoadMarsk = new Ext.LoadMask(giftStatChartTabPanel.getEl().dom, {
	    msg  : '数据统计中，请稍候......',
	    disabled : false
	});	
	
	giftPanelHeight = giftDetailsStatPanel.getHeight();
	
	gift_tabPanelHeight = giftStatChartTabPanel.getHeight();	
	
	var gift_rz = new Ext.Resizable(giftDetailsStatPanel.getEl(), {
        wrap: true, //在构造Resizable时自动在制定的id的外边包裹一层div
        minHeight:100, //限制改变的最小的高度
        pinned:false, //控制可拖动区域的显示状态，false是鼠标悬停在拖拉区域上才出现
        handles: 's',//设置拖拉的方向（n,s,e,w,all...）
        listeners : {
        	resize : function(thiz, w, h, e){
        		gift_panelDrag = true;
        	}
        }
    });
    gift_rz.on('resize', giftDetailsStatPanel.syncSize, giftDetailsStatPanel);//注册事件(作用:将调好的大小传个scope执行)
    
    
    
    var gift_totalHeight = Ext.getCmp('giftStatistics').getHeight();
    
    giftDetailsStatPanel.setHeight(gift_totalHeight*0.4);
    giftDetailsStatPanel.getEl().parent().setHeight(gift_totalHeight*0.4);
    
    giftStatChartTabPanel.setHeight(gift_totalHeight*0.6);
    
    gift_rz.resizeTo(giftDetailsStatPanel.getWidth(), giftDetailsStatPanel.getHeight());
    
	Ext.getCmp('giftStatistics').updateStatisticsDate = gift_setStatisticsDate;	
	
	
});
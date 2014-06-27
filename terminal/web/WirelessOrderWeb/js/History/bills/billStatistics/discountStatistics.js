

function initDiscountGrid(){
	var beginDate = new Ext.form.DateField({
		id : 'discount_dateSearchDateBegin',
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
		id : 'discount_dateSearchDateEnd',
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
			Ext.getCmp('btnSearchForDiscountStatistics').handler();
		}
	});
	
	var discount_combo_staffs = new Ext.form.ComboBox({
		id : 'discount_combo_staffs',
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
				Ext.getCmp('btnSearchForDiscountStatistics').handler();
			}
		}
	});
	
	var discount_deptCombo = new Ext.form.ComboBox({
		id : 'discount_deptCombo',
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
				Ext.getCmp('btnSearchForDiscountStatistics').handler();
			}
		}
	});	
	
	var discountStatisticsGridTbar = new Ext.Toolbar({
		items : [{
				xtype : 'tbtext',
				text : '日期:'
			}, dateCombo, {
				xtype : 'tbtext',
				text : '&nbsp;'
			}, beginDate , {
				xtype : 'tbtext',
				text : '&nbsp;至&nbsp;'
			}, endDate, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;员工: '
			},discount_combo_staffs,{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;部门: '
			}, discount_deptCombo ,'->',{
				text : '搜索',
				id : 'btnSearchForDiscountStatistics',
				iconCls : 'btn_search',
				handler : function(e){
					if(!beginDate.isValid() || !endDate.isValid()){
						return;
					}
					var store = discountStatisticsGrid.getStore();
					store.baseParams['dataSource'] = 'normal',
					store.baseParams['beginDate'] = beginDate.getValue().format('Y-m-d 00:00:00');
					store.baseParams['endDate'] = endDate.getValue().format('Y-m-d 23:59:59');
					store.baseParams['staffID'] = discount_combo_staffs.getValue();
					store.baseParams['deptID'] = discount_deptCombo.getValue();
					store.load({
						params : {
							start : 0,
							limit : GRID_PADDING_LIMIT_20
						}
					});
					
					if(discount_deptCombo.getValue() && discount_deptCombo.getValue() != -1){
						titleDiscountDeptName = Ext.getCmp('discount_deptCombo').getEl().dom.value + ' -- ';
					}else{
						titleDiscountDeptName = '';
					}
					
					if(discount_combo_staffs.getValue() && discount_combo_staffs.getValue() != -1){
						titleDiscountStaffName = ' 操作员 : ' + Ext.getCmp('discount_combo_staffs').getEl().dom.value;
					}else{
						titleDiscountStaffName = '';
					}
					
					requestParams = {
						dataSource : 'getDetailChart',
						dateBeg : beginDate.getValue().format('Y-m-d 00:00:00'),
						dateEnd : endDate.getValue().format('Y-m-d 23:59:59'),
						deptID : discount_deptCombo.getValue(),
						staffId : discount_combo_staffs.getValue()					
					};
					
					Ext.Ajax.request({
						url : '../../QueryDiscountStatistics.do',
						params : requestParams,
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							showDiscountDetailChart(jr);
						},
						failure : function(res, opt){
						
						}
					});	
					
					if(typeof discount_staffChart != 'undefined' && typeof discountStaffChartPanel.hasRender != 'undefined'){
						discount_getStaffChartData();
						if($('#divDiscountStaffColumnChart').is(':visible')){
							discount_loadStaffColumnChart();
							discount_staffChart.setSize(discountStatChartTabPanel.getWidth(), discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
						}else{
							discount_loadStaffChart();
							discount_loadAmountStaffChart();
							discount_staffChart.setSize(discountStatChartTabPanel.getWidth()/2, discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
							discount_staffChart_amount.setSize(discountStatChartTabPanel.getWidth()/2, discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);						
						}
						
					}
					if(typeof discount_deptChart != 'undefined' && typeof discountDeptChartPanel.hasRender != 'undefined'){
						discount_getDeptChartData();
						if($('#divDiscountDeptColumnChart').is(':visible')){
							discount_loadDeptColumnChart();
							discount_deptChart.setSize(discountStatChartTabPanel.getWidth(), discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
						}else{
							discount_loadDeptChart();
							discount_loadAmountDeptChart();
							discount_deptChart.setSize(discountStatChartTabPanel.getWidth()/2, discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
							discount_deptChart_amount.setSize(discountStatChartTabPanel.getWidth()/2, discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
						}
						
					}					
					
				}
			},'-', {
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				if(!beginDate.isValid() || !endDate.isValid()){
					return;
				}
				var url = '../../{0}?beginDate={1}&endDate={2}&staffID={3}&deptID={4}&dataSource={5}';
				url = String.format(
						url, 
						'ExportHistoryStatisticsToExecl.do', 
						beginDate.getValue().format('Y-m-d 00:00:00'),
						endDate.getValue().format('Y-m-d 23:59:59'),
						discount_combo_staffs.getValue(),
						discount_deptCombo.getValue(),
						'discountStatisticsList'
				);
				window.location = url;
			}
		}]
	});
	
	
	discountStatisticsGrid = createGridPanel(
		'',
		'',
		'',
		'',
		'../../QueryDiscountStatistics.do',
		[[true, false, false, true], 
		 ['日期','orderDateFormat'], 
		 ['账单号', 'id'],
         ['折扣额','discountPrice',,'right','Ext.ux.txtFormat.gridDou'], 
         ['实收金额','actualPrice',,'right','Ext.ux.txtFormat.gridDou'],
         ['操作人','waiter'], 
         ['备注','comment', 200]
		],
		['orderDateFormat', 'id', 'discountPrice', 'actualPrice', 'waiter', 'comment'],
		[ ['dataSource', 'getDetail']],
		GRID_PADDING_LIMIT_20,
		null,
		[discountStatisticsGridTbar]
	);
	discountStatisticsGrid.region = 'center';
	discountStatisticsGrid.on('render', function(){
		dateCombo.setValue(1);
		dateCombo.fireEvent('select', dateCombo, null, 1);
	});
	discountStatisticsGrid.getStore().on('load', function(store, records, options){
		if(store.getCount() > 0){
			var sumRow = discountStatisticsGrid.getView().getRow(store.getCount()-1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			
			for(var i = 0; i < discountStatisticsGrid.getColumnModel().getColumnCount(); i++){
				var sumRow = discountStatisticsGrid.getView().getCell(store.getCount()-1, i);
				sumRow.style.fontSize = '15px';
				sumRow.style.fontWeight = 'bold';
				sumRow.style.color = 'green';
			}
			discountStatisticsGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
			discountStatisticsGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
			discountStatisticsGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '--';
			discountStatisticsGrid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
		}
	});
	//
	discountDetailsStatPanel = new Ext.Panel({
		title : '折扣明细',
		layout : 'border',
		region : 'center',
		items : [discountStatisticsGrid],
		listeners : {
			bodyresize : function(e, w, h){
				if(typeof discountPanelHeight != 'undefined'){
					var chartHeight = discount_tabPanelHeight + (discountPanelHeight - h);
					
					discountStatChartTabPanel.getEl().setTop((h+30)) ;
					
					discount_changeChartWidth(w,chartHeight+30);
					
					if(discount_panelDrag){
						discountStatChartTabPanel.setHeight(chartHeight+70);
					}
					discountStatChartTabPanel.doLayout();					
				}
			}
		}
	});	

}

function discount_changeChartWidth(w,h){
	if(eval($('div:visible[data-type=discountChart]').attr('data-value'))){
		if($('div:visible[data-type=discountChart]').length == 1){
			eval($('div:visible[data-type=discountChart]').attr('data-value')).setSize(w, h);
		}else if($('div:visible[data-type=discountChart]').length > 1){
			eval($($('div:visible[data-type=discountChart]')[0]).attr('data-value')).setSize(w/2, h);
			eval($($('div:visible[data-type=discountChart]')[1]).attr('data-value')).setSize(w/2, h);				
		}
	}	
}

function showDiscountDetailChart(jdata){
	var dateBegin = Ext.getCmp('discount_dateSearchDateBegin').getValue().format('Y-m-d');
	var dateEnd = Ext.getCmp('discount_dateSearchDateEnd').getValue().format('Y-m-d');
	
/*	var hourBegin = Ext.getCmp('discount_txtBusinessHourBegin').getEl().dom.textContent;
	var hourEnd = Ext.getCmp('discount_txtBusinessHourEnd').getEl().dom.textContent;*/
	
	var chartData = eval('(' + jdata.other.chart + ')');
	discount_detailChart = new Highcharts.Chart({
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
        	renderTo: 'divDiscountDetailChart'
    	}, 
        title: {
            text: '<b>'+titleDiscountDeptName+'折扣额走势图（'+dateBegin+ '至' +dateEnd+'）'+titleDiscountStaffName+'</b>'
        },
        labels: {
        	items : [{
        		html : '<b>总折扣金额:' + chartData.totalMoney + ' 元</b><br><b>日均折扣额:' + chartData.avgMoney + ' 元</b>',
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
	
	if(discountDetailChartPanel && discountDetailChartPanel.isVisible()){
		discountDetailChartPanel.show();
	}
}

function discount_getStaffChartData(){
	requestParams.dataSource = 'getStaffChart';
	$.ajax({
		url : '../../QueryDiscountStatistics.do',
		type : 'post',
		async : false,
		data : requestParams,
		success : function(jr, status, xhr){
			discount_staffChartData.chartData.data = [];
			discount_staffChartData.chartAmountData.data = [];
			discount_staffChartData.staffColumnChart.xAxis = [];
			discount_staffChartData.staffColumnChart.yAxis.data = [];
			discount_staffChartData.staffColumnChart.yAxisAmount.data = [];
			for (var i = 0; i < jr.root.length; i++) {
				discount_staffChartData.chartData.data.push([jr.root[i].staffName, jr.root[i].discountPrice]);
				discount_staffChartData.chartAmountData.data.push([jr.root[i].staffName, jr.root[i].discountAmount]);
				discount_staffChartData.staffColumnChart.xAxis.push(jr.root[i].staffName);
				discount_staffChartData.staffColumnChart.yAxis.data.push({y : jr.root[i].discountPrice, color : colors[i]}); 
				discount_staffChartData.staffColumnChart.yAxisAmount.data.push({y : jr.root[i].discountAmount, color : colors[i]});
			}
		},
		failure : function(res, opt){
		
		}
	});
	
}

function discount_loadStaffChart(){
	
	discount_staffChart = new Highcharts.Chart({
	    chart: {
	    	renderTo : 'divDiscountPriceStaffChart',
	        plotBackgroundColor: null,
	        plotBorderWidth: null,
	        plotShadow: false
	    },
	    title: {
	        text: '员工折扣金额比例图'
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
	    series: [discount_staffChartData.chartData],
        credits : {
        	enabled : false
        }
	});				
}

function discount_loadAmountStaffChart(){
	
	discount_staffChart_amount = new Highcharts.Chart({
	    chart: {
	    	renderTo : 'divDiscountAmountStaffChart',
	        plotBackgroundColor: null,
	        plotBorderWidth: null,
	        plotShadow: false
	    },
	    title: {
	        text: '员工折扣数量比例图'
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
	    series: [discount_staffChartData.chartAmountData],
        credits : {
        	enabled : false
        }
	});				
}

function discount_loadStaffColumnChart(){
 	discount_staffChart = new Highcharts.Chart({
        chart: {
            type: 'column',
            renderTo : 'divDiscountStaffColumnChart'
        },
        title: {
            text: '员工折扣金额柱状图'
        },
        xAxis: {
            categories: discount_staffChartData.staffColumnChart.xAxis
        },
        yAxis: {
            min: 0,
            title: {
                text: '金额 (元)'
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
        series: [discount_staffChartData.staffColumnChart.yAxis, discount_staffChartData.staffColumnChart.yAxisAmount],
        credits : {
        	enabled : false
        }
    });	
}

function discount_getDeptChartData(){
	requestParams.dataSource = 'getDeptChart';
	$.ajax({
		url : '../../QueryDiscountStatistics.do',
		type : 'post',
		async : false,
		data : requestParams,
		success : function(jr, status, xhr){
			discount_deptChartData.chartData.data = [];
			discount_deptChartData.chartAmountData.data = [];
			discount_deptChartData.deptColumnChart.xAxis = [];
			discount_deptChartData.deptColumnChart.yAxis.data = [];
			discount_deptChartData.deptColumnChart.yAxisAmount.data = [];
			for (var i = 0; i < jr.root.length; i++) {
				discount_deptChartData.chartData.data.push([jr.root[i].discountDept.name, jr.root[i].discountPrice]);
				discount_deptChartData.chartAmountData.data.push([jr.root[i].discountDept.name, jr.root[i].discountAmount]);
				discount_deptChartData.deptColumnChart.xAxis.push(jr.root[i].discountDept.name);
				discount_deptChartData.deptColumnChart.yAxis.data.push({y : jr.root[i].discountPrice, color : colors[i]}); 
				discount_deptChartData.deptColumnChart.yAxisAmount.data.push({y : jr.root[i].discountAmount, color : colors[i]});
			}
		},
		failure : function(res, opt){
		
		}
	});
	
}

function discount_loadDeptColumnChart(){
 	discount_deptChart = new Highcharts.Chart({
        chart: {
            type: 'column',
            renderTo : 'divDiscountDeptColumnChart'
        },
        title: {
            text: '部门折扣柱状图'
        },
        xAxis: {
            categories: discount_deptChartData.deptColumnChart.xAxis
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
        series: [discount_deptChartData.deptColumnChart.yAxis, discount_deptChartData.deptColumnChart.yAxisAmount],
        credits : {
        	enabled : false
        }
    });	
}

function discount_loadDeptChart(){
	discount_deptChart = new Highcharts.Chart({
	    chart: {
	    	renderTo : 'divDiscountPriceDeptChart',
	        plotBackgroundColor: null,
	        plotBorderWidth: null,
	        plotShadow: false
	    },
	    title: {
	        text: '部门折扣金额比例图'
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
	    series: [discount_deptChartData.chartData],
        credits : {
        	enabled : false
        }
	});				
}

function discount_loadAmountDeptChart(){
	discount_deptChart_amount = new Highcharts.Chart({
	    chart: {
	    	renderTo : 'divDiscountAmountDeptChart',
	        plotBackgroundColor: null,
	        plotBorderWidth: null,
	        plotShadow: false
	    },
	    title: {
	        text: '部门折扣数量比例图'
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
	    series: [discount_deptChartData.chartAmountData],
        credits : {
        	enabled : false
        }
	});				
}

var titleDiscountDeptName, titleDiscountStaffName;
var discount_detailChart, discount_staffChart, discount_staffChart_amount, discount_deptChart, discount_deptChart_amount;

var discountDetailChartPanel, discountStatChartTabPanel;
var discountPanelHeight, discount_tabPanelHeight;
var discount_panelDrag = false, colors = Highcharts.getOptions().colors;
var discount_staffChartData = {chartData : {type : 'pie', name : '比例', data : []}, 
							chartAmountData : {type : 'pie', name : '比例', data : []},
							staffColumnChart : {xAxis : [], 
							yAxis : {name : '员工折扣金额', data : [],
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
							yAxisAmount : {name : '员工折扣数量', data : [],
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
var discount_deptChartData = {chartData : {type : 'pie', name : '比例', data : []}, 
							chartAmountData : {type : 'pie', name : '比例', data : []},
							deptColumnChart : {xAxis : [], 
							yAxis : {name : '部门折扣金额', data : [],
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
							yAxisAmount : {name : '部门折扣数量', data : [],
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
	initDiscountGrid();
	
	discountDetailChartPanel = new Ext.Panel({
		title : '折扣走势',
		contentEl : 'divDiscountDetailChart',
		listeners : {
			show : function(thiz){
				//thiz.getEl(): 刚打开页面时thiz.getWidth无效
				if(discount_detailChart && typeof thiz.getEl() != 'undefined'){
					discount_detailChart.setSize(thiz.getWidth(), discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
				}
			}
		}		
	});
	
	discountStaffChartPanel = new Ext.Panel({
		title : '员工折扣',
		contentEl : 'divDiscountStaffCharts',
		listeners : {
			show : function(thiz){
				if($('#divDiscountStaffColumnChart').is(":visible")){
					discount_staffChart.setSize(thiz.getWidth(), discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
				}else if($('#divDiscountAmountStaffChart').is(":visible")){
					discount_staffChart.setSize(discountStatChartTabPanel.getWidth()/2, discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
					discount_staffChart_amount.setSize(discountStatChartTabPanel.getWidth()/2, discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);				
				}else{
					$('#divDiscountStaffChartChange').show();
					$('#divDiscountAmountStaffChart').show();
					$('#divDiscountPriceStaffChart').show();
				}
				if(!discount_staffChart){
					discount_loadStaffChart();
					discount_loadAmountStaffChart();
					discount_staffChart.setSize(discountStatChartTabPanel.getWidth()/2, discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
					discount_staffChart_amount.setSize(discountStatChartTabPanel.getWidth()/2, discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
				}
			},
			render : function(thiz){
				thiz.hasRender = true;
			}
		}
	});	
	
	discountDeptChartPanel = new Ext.Panel({
		title : '部门折扣',
		contentEl : 'divDiscountDeptCharts',
		listeners : {
			show : function(thiz){
				if($('#divDiscountDeptColumnChart').is(":visible")){
					discount_deptChart.setSize(thiz.getWidth(), discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
				}else if($('#divDiscountAmountDeptChart').is(":visible")){
					discount_deptChart.setSize(discountStatChartTabPanel.getWidth()/2, discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
					discount_deptChart_amount.setSize(discountStatChartTabPanel.getWidth()/2, discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);				
				}else{
					$('#divDiscountDeptChartChange').show();
					$('#divDiscountAmountDeptChart').show();
					$('#divDiscountPriceDeptChart').show();
				}
				if(!discount_deptChart){
					discount_getDeptChartData();
					discount_loadDeptChart();
					discount_loadAmountDeptChart();
					discount_deptChart.setSize(discountStatChartTabPanel.getWidth()/2, discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
					discount_deptChart_amount.setSize(discountStatChartTabPanel.getWidth()/2, discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
				}
			},
			render : function(thiz){
				thiz.hasRender = true;
			}
		}
	});		
	


	discountStatChartTabPanel = new Ext.TabPanel({
		region : 'south',
		height : 330,
		items : [discountDetailChartPanel, discountStaffChartPanel, discountDeptChartPanel],
		listeners : {
			render : function(thiz){
				thiz.setActiveTab(discountDetailChartPanel);
			}
		}
	});
	
	new Ext.Panel({
		renderTo : 'divDiscountStatistics',//渲染到
		id : 'discountStatisticsPanel',
		//solve不跟随窗口的变化而变化
		width : parseInt(Ext.getDom('divDiscountStatistics').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divDiscountStatistics').parentElement.style.height.replace(/px/g,'')),
		layout:'border',
		frame : true, //边框
		//子集
		items : [discountDetailsStatPanel, discountStatChartTabPanel]
	});
	
	discountPanelHeight = discountDetailChartPanel.getHeight();
	
	discount_tabPanelHeight = discountStatChartTabPanel.getHeight();	
	
	
	
	var discount_rz = new Ext.Resizable(discountDetailsStatPanel.getEl(), {
        wrap: true, //在构造Resizable时自动在制定的id的外边包裹一层div
        minHeight:100, //限制改变的最小的高度
        pinned:false, //控制可拖动区域的显示状态，false是鼠标悬停在拖拉区域上才出现
        handles: 's',//设置拖拉的方向（n,s,e,w,all...）
        listeners : {
        	resize : function(thiz, w, h, e){
        		discount_panelDrag = true;
        	}
        }
    });
    discount_rz.on('resize', discountDetailsStatPanel.syncSize, discountDetailsStatPanel);//注册事件(作用:将调好的大小传个scope执行)
	
	
	$('#divDiscountStaffChartChange').toggle(
		function(){
			$('#divDiscountAmountStaffChart').hide();
			$('#divDiscountPriceStaffChart').hide();
			
			$('#divDiscountStaffColumnChart').show();
			discount_loadStaffColumnChart();
			discount_staffChart.setSize(discountStatChartTabPanel.getWidth(), discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
		},
		function(){
			$('#divDiscountStaffColumnChart').hide();
			
			$('#divDiscountAmountStaffChart').show();
			$('#divDiscountPriceStaffChart').show();
			discount_loadStaffChart();
			discount_loadAmountStaffChart();
			discount_staffChart.setSize(discountStatChartTabPanel.getWidth()/2, discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
			discount_staffChart_amount.setSize(discountStatChartTabPanel.getWidth()/2, discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
		}		
	);
	$('#divDiscountDeptChartChange').toggle(
		function(){
			$('#divDiscountAmountDeptChart').hide();
			$('#divDiscountPriceDeptChart').hide();
			
			$('#divDiscountDeptColumnChart').show();
			discount_loadDeptColumnChart();
			discount_deptChart.setSize(discountStatChartTabPanel.getWidth(), discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
		},
		function(){
			$('#divDiscountDeptColumnChart').hide();
			
			$('#divDiscountAmountDeptChart').show();
			$('#divDiscountPriceDeptChart').show();
			discount_loadDeptChart();
			discount_loadAmountDeptChart();
			discount_deptChart.setSize(discountStatChartTabPanel.getWidth()/2, discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
			discount_deptChart_amount.setSize(discountStatChartTabPanel.getWidth()/2, discount_panelDrag ? discountStatChartTabPanel.getHeight() - 60 : discountStatChartTabPanel.getHeight()-30);
		}		
	);	
	
	
});
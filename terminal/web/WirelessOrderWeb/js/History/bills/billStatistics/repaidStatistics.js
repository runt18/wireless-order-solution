
var repaid_beginDate = new Ext.form.DateField({
	xtype : 'datefield',		
	format : 'Y-m-d',
	width : 100,
	maxValue : new Date(),
	readOnly : false,
	allowBlank : false
});
var repaid_endDate = new Ext.form.DateField({
	xtype : 'datefield',
	format : 'Y-m-d',
	width : 100,
	maxValue : new Date(),
	readOnly : false,
	allowBlank : false
});
var repaid_dateCombo = Ext.ux.createDateCombo({
	beginDate : repaid_beginDate,
	endDate : repaid_endDate,
	callback : function(){
		Ext.getCmp('btnSearchForRepaidStatistics').handler();
	}
});

var repaid_combo_staffs = new Ext.form.ComboBox({
	id : 'repaid_combo_staffs',
	readOnly : false,
	forceSelection : true,
	width : 103,
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
			Ext.getCmp('btnSearchForRepaidStatistics').handler();
		}
	}
});

function res_billDetailHandler(orderID) {
	res_showBillDetailWin();
	repaidOrderDetailWin.show();
	repaidOrderDetailWin.setTitle('账单号: ' + orderID);
	repaidOrderDetailWin.center();
};

function linkOrderId(v){
	return '<a href=\"javascript:res_billDetailHandler('+ v +')\">'+ v +'</a>';
}

function showRepaidDetailChart(jdata){
	var dateBegin = repaid_beginDate.getValue().format('Y-m-d');
	var dateEnd = repaid_endDate.getValue().format('Y-m-d');
	
	
	var chartData = eval('(' + jdata.other.chart + ')');
	repaidDetailChart = new Highcharts.Chart({
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
        	renderTo: 'divRepaidDetailChart'
    	}, 
        title: {
            text: '<b>反结账走势图（'+dateBegin+ '至' +dateEnd+'）'+titleRepaidStaffName+'</b>'
        },
        labels: {
        	items : [{
        		html : '<b>总反结账金额:' + chartData.totalMoney + ' 元</b><br><b>日均反结账额:' + chartData.avgMoney + ' 元</b><br><b>日均反结账量:' + chartData.avgCount + ' 份</b>',
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
                    this.x.substring(0, 11) +': '+ '<b>'+this.y+' 元</b> ';
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
	
	if(repaidDetailChartPanel && repaidDetailChartPanel.isVisible()){
		repaidDetailChartPanel.show();
	}
}

function repaid_loadStaffChart(){
	repaidStaffChart = new Highcharts.Chart({
	    chart: {
	    	renderTo : 'divRepaidStaffChart',
	        plotBackgroundColor: null,
	        plotBorderWidth: null,
	        plotShadow: false
	    },
	    title: {
	        text: '员工反结账金额比例图'
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
	    series: [repaid_staffChartData.chartData],
        credits : {
        	enabled : false
        }
	});				
}

function repaid_loadAmountStaffChart(){
	repaidStaffChart_amount = new Highcharts.Chart({
	    chart: {
	    	renderTo : 'divRepaidAmountStaffChart',
	        plotBackgroundColor: null,
	        plotBorderWidth: null,
	        plotShadow: false
	    },
	    title: {
	        text: '员工反结账数量比例图'
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
	    series: [repaid_staffChartData.chartAmountData],
        credits : {
        	enabled : false
        }
	});				
}

function repaid_loadStaffColumnChart(){
 	repaidStaffChart = new Highcharts.Chart({
        chart: {
            type: 'column',
            renderTo : 'divRepaidStaffColumnChart'
        },
        title: {
            text: '员工反结账柱状图'
        },
        xAxis: {
            categories: repaid_staffChartData.staffColumnChart.xAxis
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
        series: [repaid_staffChartData.staffColumnChart.yAxis, repaid_staffChartData.staffColumnChart.yAxisAmount],
        credits : {
        	enabled : false
        }
    });	
}

function repaid_getStaffChartData(){
	requestParams.dataSource = 'getStaffChart';
	$.ajax({
		url : '../../QueryRepaidStatistics.do',
		type : 'post',
		async : false,
		data : requestParams,
		success : function(jr, status, xhr){
			repaid_chartLoadMarsk.hide();
			repaid_staffChartData.chartData.data = [];
			repaid_staffChartData.chartAmountData.data = [];
			repaid_staffChartData.staffColumnChart.xAxis = [];
			repaid_staffChartData.staffColumnChart.yAxis.data = [];
			repaid_staffChartData.staffColumnChart.yAxisAmount.data = [];
			for (var i = 0; i < jr.root.length; i++) {
				repaid_staffChartData.chartData.data.push([jr.root[i].staffName, jr.root[i].repaidPrice]);
				repaid_staffChartData.chartAmountData.data.push([jr.root[i].staffName, jr.root[i].repaidAmount]);
				repaid_staffChartData.staffColumnChart.xAxis.push(jr.root[i].staffName);
				repaid_staffChartData.staffColumnChart.yAxis.data.push({y : jr.root[i].repaidPrice, color : colors[i]}); 
				repaid_staffChartData.staffColumnChart.yAxisAmount.data.push({y : jr.root[i].repaidAmount, color : colors[i]});
			}
		},
		failure : function(res, opt){
		
		}
	});
	
}
function repaid_changeChartWidth(w,h){
	if(eval($('div:visible[data-type=repaidChart]').attr('data-value'))){
		if($('div:visible[data-type=repaidChart]').length == 1){
			eval($('div:visible[data-type=repaidChart]').attr('data-value')).setSize(w, h);
		}else if($('div:visible[data-type=repaidChart]').length > 1){
			eval($($('div:visible[data-type=repaidChart]')[0]).attr('data-value')).setSize(w/2, h);
			eval($($('div:visible[data-type=repaidChart]')[1]).attr('data-value')).setSize(w/2, h);				
		}
	}	
}
function initGrid(){
	var cm = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header : '反结账时间', dataIndex : 'orderDateFormat'},
		{header : '人员', dataIndex : 'operateStaff'},
		{header : '账单号', dataIndex : 'orderId', renderer : linkOrderId},
		{header : '原应收', dataIndex : 'oldTotalPrice', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '原实收', dataIndex : 'oldActualPrice', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '反结账金额', dataIndex : 'repaidPrice', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '现应收', dataIndex : 'totalPrice', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '现实收', dataIndex : 'actualPrice', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '付款方式', dataIndex : 'payTypeText'}
	]);
	
	cm.defaultSortable = true;
	
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url : '../../QueryRepaidStatistics.do'}),
		reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root'}, [
		{name : 'orderDateFormat'},
		{name : 'operateStaff'},
		{name : 'orderId'},
		{name : 'oldTotalPrice'},
		{name : 'oldActualPrice'},
		{name : 'repaidPrice'},
		{name : 'totalPrice'},
		{name : 'actualPrice'},
		{name : 'payTypeValue'},
		{name : 'payTypeText'}
		])
		
	});
	
	var repaidStatisticsTbar = new Ext.Toolbar({
		items : [{
				xtype : 'tbtext',
				text : '日期:'
			}, repaid_dateCombo, {
				xtype : 'tbtext',
				text : '&nbsp;'
			}, repaid_beginDate , {
				xtype : 'tbtext',
				text : '&nbsp;至&nbsp;'
			}, repaid_endDate, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			},{
				xtype : 'tbtext',
				text : '操作人员:'
			}, repaid_combo_staffs, '->', {
				text : '搜索',
				id : 'btnSearchForRepaidStatistics',
				iconCls : 'btn_search',
				handler : function(e){
					if(!repaid_beginDate.isValid() || !repaid_endDate.isValid()){
						return;
					}
					var store = repaidStatisticsGrid.getStore();
					store.baseParams['dataSource'] = 'normal',
					store.baseParams['beginDate'] = repaid_beginDate.getValue().format('Y-m-d 00:00:00');
					store.baseParams['endDate'] = repaid_endDate.getValue().format('Y-m-d 23:59:59');
					store.baseParams['staffId'] = repaid_combo_staffs.getValue();
					store.load({
						params : {
							start : 0,
							limit : limitCount
						}
					});
					
					if(repaid_combo_staffs.getValue() && repaid_combo_staffs.getValue() != -1){
						titleRepaidStaffName = '操作人 : ' + repaid_combo_staffs.getEl().dom.value;
					}else{
						titleRepaidStaffName = '';
					}
					
					requestParams = {
						dataSource : 'getDetailChart',
						dateBeg : repaid_beginDate.getValue().format('Y-m-d 00:00:00'),
						dateEnd : repaid_endDate.getValue().format('Y-m-d 23:59:59'),
						staffID : repaid_combo_staffs.getValue()					
					};
					
					repaid_chartLoadMarsk.show();
					
					Ext.Ajax.request({
						url : '../../QueryRepaidStatistics.do',
						params : requestParams,
						success : function(res, opt){
							repaid_chartLoadMarsk.hide();
							var jr = Ext.decode(res.responseText);
							showRepaidDetailChart(jr);
						},
						failure : function(res, opt){
						
						}
					});		
					
					if(typeof repaidStaffChart != 'undefined'){
						repaid_getStaffChartData();
						if($('#divRepaidStaffColumnChart').is(':visible')){
							repaid_loadStaffColumnChart();
							repaidStaffChart.setSize(repaidStatChartTabPanel.getWidth(), repaid_panelDrag ? repaidStatChartTabPanel.getHeight() - 60 : repaidStatChartTabPanel.getHeight()-30);
						}else{
							repaid_loadStaffChart();
							repaid_loadAmountStaffChart();
							repaidStaffChart.setSize(repaidStatChartTabPanel.getWidth()/2, repaid_panelDrag ? repaidStatChartTabPanel.getHeight() - 60 : repaidStatChartTabPanel.getHeight()-30);
							repaidStaffChart_amount.setSize(repaidStatChartTabPanel.getWidth()/2, repaid_panelDrag ? repaidStatChartTabPanel.getHeight() - 60 : repaidStatChartTabPanel.getHeight()-30);							
						}
					}						
					
				}
			},'-', {
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				if(!repaid_beginDate.isValid() || !repaid_endDate.isValid()){
					return;
				}
				var url = '../../{0}?beginDate={1}&endDate={2}&staffId={3}&dataSource={4}';
				url = String.format(
						url, 
						'ExportHistoryStatisticsToExecl.do', 
						repaid_beginDate.getValue().format('Y-m-d 00:00:00'), 
						repaid_endDate.getValue().format('Y-m-d 23:59:59'),
						repaid_combo_staffs.getValue(),
						'repaidStatisticsList'
					);
				window.location = url;
			}
		}]
	});
	var pagingBar = new Ext.PagingToolbar({
	   pageSize : limitCount,	//显示记录条数
	   store : ds,	//定义数据源
	   displayInfo : true,	//是否显示提示信息
	   displayMsg : "显示第 {0} 条到 {1} 条记录，共 {2} 条",
	   emptyMsg : "没有记录"
	});
	
	repaidStatisticsGrid = new Ext.grid.GridPanel({
		id : 'repaid_grid',
	    //height : '500',
	    border : false,
	    frame : false,
	    store : ds,
	    cm : cm,
	    viewConfig : {
	    	forceFit : true
	    },
	    loadMask : {
	    	msg : "数据加载中，请稍等..."
	    },
	    tbar : repaidStatisticsTbar,
	    bbar : pagingBar
	});
	repaidStatisticsGrid.region = 'center';
	
	repaidStatisticsGrid.getStore().on('load', function(store, records, options){
		
		if(store.getCount() > 0){
			var sumRow = repaidStatisticsGrid.getView().getRow(store.getCount() - 1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			for(var i = 0; i < repaidStatisticsGrid.getColumnModel().getColumnCount(); i++){
				var sumCell = repaidStatisticsGrid.getView().getCell(store.getCount() - 1, i);
				sumCell.style.fontSize = '15px';
				sumCell.style.fontWeight = 'bold';	
				sumCell.style.color = 'green';
			}
			repaidStatisticsGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
			repaidStatisticsGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
			repaidStatisticsGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
			repaidStatisticsGrid.getView().getCell(store.getCount()-1, 4).innerHTML = '--';
			repaidStatisticsGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '--';
			repaidStatisticsGrid.getView().getCell(store.getCount()-1, 7).innerHTML = '--';
			repaidStatisticsGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
			repaidStatisticsGrid.getView().getCell(store.getCount()-1, 9).innerHTML = '--';
		}
	});
	
	repaidDetailPanel = new Ext.Panel({
		title : '反结账明细',
		layout:'border',
		region : 'center',
		frame : false, //边框
		//子集
		items : [repaidStatisticsGrid],
		listeners : {
			bodyresize : function(e, w, h){
				if(typeof repaidPanelHeight != 'undefined'){
					var chartHeight = repaid_tabPanelHeight + (repaidPanelHeight - h);
					
					repaidStatChartTabPanel.getEl().setTop((h+30)) ;
					
					repaid_changeChartWidth(w,chartHeight-60);
					
					if(repaid_panelDrag){
						repaidStatChartTabPanel.setHeight(chartHeight);
					}
				}
			}			
		}
	});		
	
	
}
function res_showBillDetailWin(){
	repaidOrderDetailWin = new Ext.Window({
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
				repaidOrderDetailWin.destroy();
			}
		} ],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				repaidOrderDetailWin.destroy();
			}
		}],
		listeners : {
			show : function(thiz) {
				var sd = Ext.ux.getSelData(repaidStatisticsGrid);
				thiz.load({
					url : '../window/history/orderDetail.jsp', 
					scripts : true,
					params : {
						orderId : sd.orderId,
						foodStatus : 'isRepaid'
					},
					method : 'post'
				});
				thiz.center();	
			}
		}
	});
}

var repaid_setStatisticsDate = function(){
	if(sendToPageOperation){
		repaid_beginDate.setValue(sendToStatisticsPageBeginDate);
		repaid_endDate.setValue(sendToStatisticsPageEndDate);		
		Ext.getCmp('btnSearchForRepaidStatistics').handler();
		sendToPageOperation = false;
	}
};

var requestParams, repaid_tabPanelHeight, repaidPanelHeight;
var colors = Highcharts.getOptions().colors, repaid_panelDrag = false;
var repaidStaffChart, repaidDetailChart, repaidStaffChart_amount;
var repaidDetailPanel, repaidDetailChartPanel, repaidStaffChartPanel, repaidStatChartTabPanel;
var repaid_staffChartData = {chartData : {type : 'pie', name : '比例', data : []}, 
							chartAmountData : {type : 'pie', name : '比例', data : []}, 
							staffColumnChart : {xAxis : [], 
							yAxis : {name : '员工反结账金额', data : [],
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
							yAxisAmount : {name : '员工反结账数量', data : [],
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

var titleRepaidStaffName;		
var repaid_chartLoadMarsk;
Ext.onReady(function(){

	initGrid();
	
	repaidDetailChartPanel = new Ext.Panel({
		title : '反结账走势',
		contentEl : 'divRepaidDetailChart',
		listeners : {
			show : function(thiz){
				//thiz.getEl(): 刚打开页面时thiz.getWidth无效
				if(repaidDetailChart && typeof thiz.getEl() != 'undefined'){
					repaidDetailChart.setSize(thiz.getWidth(), repaid_panelDrag ? repaidStatChartTabPanel.getHeight() - 60 : repaidStatChartTabPanel.getHeight()-30);
				}
			}
		}		
	});
	
	repaidStaffChartPanel = new Ext.Panel({
		title : '员工反结账',
		contentEl : 'divRepaidStaffCharts',
		listeners : {
			show : function(thiz){
				if($('#divRepaidStaffColumnChart').is(":visible")){
					repaidStaffChart.setSize(thiz.getWidth(), repaid_panelDrag ? repaidStatChartTabPanel.getHeight() - 60 : repaidStatChartTabPanel.getHeight()-30);
				}else if($('#divRepaidStaffChart').is(":visible")){
					repaidStaffChart.setSize(repaidStatChartTabPanel.getWidth()/2, repaid_panelDrag ? repaidStatChartTabPanel.getHeight() - 60 : repaidStatChartTabPanel.getHeight()-30);
					repaidStaffChart_amount.setSize(repaidStatChartTabPanel.getWidth()/2, repaid_panelDrag ? repaidStatChartTabPanel.getHeight() - 60 : repaidStatChartTabPanel.getHeight()-30);				
				}else{
					$('#divRepaidStaffChartChange').show();
					$('#divRepaidStaffChart').show();
					$('#divRepaidAmountStaffChart').show();
				}
				if(!repaidStaffChart){
					repaid_getStaffChartData();
					repaid_loadStaffChart();
					repaid_loadAmountStaffChart();
					repaidStaffChart.setSize(repaidStatChartTabPanel.getWidth()/2, repaid_panelDrag ? repaidStatChartTabPanel.getHeight() - 60 : repaidStatChartTabPanel.getHeight()-30);
					repaidStaffChart_amount.setSize(repaidStatChartTabPanel.getWidth()/2, repaid_panelDrag ? repaidStatChartTabPanel.getHeight() - 60 : repaidStatChartTabPanel.getHeight()-30);
				}
			},
			render : function(thiz){
				thiz.hasRender = true;
			}
		}
	});	
	
	repaidStatChartTabPanel = new Ext.TabPanel({
		region : 'south',
		height : 430,
		items : [repaidDetailChartPanel, repaidStaffChartPanel],
		listeners : {
			render : function(thiz){
				thiz.setActiveTab(repaidDetailChartPanel);
			}
		}
	});	
	


	new Ext.Panel({
		renderTo : 'divRepaidStatistics',//渲染到
		id : 'repaidStatisticsPanel',
		//solve不跟随窗口的变化而变化
		width : parseInt(Ext.getDom('divRepaidStatistics').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divRepaidStatistics').parentElement.style.height.replace(/px/g,'')),
		layout:'border',
		frame : true, //边框
		//子集
		items : [repaidDetailPanel, repaidStatChartTabPanel]
	});
	
	repaidPanelHeight = repaidDetailPanel.getHeight();
	
	repaid_tabPanelHeight = repaidStatChartTabPanel.getHeight();
	var repaid_rz = new Ext.Resizable(repaidDetailPanel.getEl(), {
        wrap: true, //在构造Resizable时自动在制定的id的外边包裹一层div
        minHeight:100, //限制改变的最小的高度
        pinned:false, //控制可拖动区域的显示状态，false是鼠标悬停在拖拉区域上才出现
        handles: 's',//设置拖拉的方向（n,s,e,w,all...）
        listeners : {
        	resize : function(thiz, w, h, e){
        		repaid_panelDrag = true;
        	}
        }
    });
    repaid_rz.on('resize', repaidDetailPanel.syncSize, repaidDetailPanel);//注册事件(作用:将调好的大小传个scope执行)
//	repaidStatisticsGrid.getStore().load();
	$('#divRepaidStaffChartChange').toggle(
		function(){
			$('#divRepaidStaffChart').hide();
			$('#divRepaidAmountStaffChart').hide();
			
			$('#divRepaidStaffColumnChart').show();
			repaid_loadStaffColumnChart();
			repaidStaffChart.setSize(repaidStatChartTabPanel.getWidth(), repaid_panelDrag ? repaidStatChartTabPanel.getHeight() - 60 : repaidStatChartTabPanel.getHeight()-30);
		},
		function(){
			$('#divRepaidStaffColumnChart').hide();
			
			$('#divRepaidStaffChart').show();
			$('#divRepaidAmountStaffChart').show();
			repaid_loadStaffChart();
			repaid_loadAmountStaffChart();
			repaidStaffChart.setSize(repaidStatChartTabPanel.getWidth()/2, repaid_panelDrag ? repaidStatChartTabPanel.getHeight() - 60 : repaidStatChartTabPanel.getHeight()-30);
			repaidStaffChart_amount.setSize(repaidStatChartTabPanel.getWidth()/2, repaid_panelDrag ? repaidStatChartTabPanel.getHeight() - 60 : repaidStatChartTabPanel.getHeight()-30);
		}		
	);  
	
	repaid_chartLoadMarsk = new Ext.LoadMask(repaidStatChartTabPanel.getEl().dom, {
	    msg  : '数据统计中，请稍候......',
	    disabled : false
	});
	
	if(sendToPageOperation){
		repaid_setStatisticsDate();
	}else{
		repaid_dateCombo.setValue(1);
		repaid_dateCombo.fireEvent('select', repaid_dateCombo, null, 1);			
	}	
	
	Ext.getCmp('repaidStatistics').updateStatisticsDate = repaid_setStatisticsDate;
});
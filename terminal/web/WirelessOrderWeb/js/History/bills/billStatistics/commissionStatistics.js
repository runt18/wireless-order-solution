function comi_billDetailHandler(orderID) {
	comi_showBillDetailWin();
	commissionOrderDetailWin.show();
	commissionOrderDetailWin.setTitle('账单号: ' + orderID);
	commissionOrderDetailWin.center();
};

function linkOrderId(v){
	return '<a href=\"javascript:comi_billDetailHandler('+ v +')\">'+ v +'</a>';
}

function comi_showBillDetailWin(){
	commissionOrderDetailWin = new Ext.Window({
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
				commissionOrderDetailWin.destroy();
			}
		} ],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				commissionOrderDetailWin.destroy();
			}
		}],
		listeners : {
			show : function(thiz) {
				var sd = Ext.ux.getSelData(commissionStatisticsGrid);
				thiz.load({
					url : '../window/history/orderDetail.jsp', 
					scripts : true,
					params : {
						orderId : sd.orderId,
						foodStatus : 'isCommission'
					},
					method : 'post'
				});
				thiz.center();	
			}
		}
	});
}
function commissionDetailInit(){
	var commission_beginDate = new Ext.form.DateField({
		id : 'commission_dateSearchDateBegin',
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	var commission_endDate = new Ext.form.DateField({
		id : 'commission_dateSearchDateEnd',
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	var commission_dateCombo = Ext.ux.createDateCombo({
		beginDate : commission_beginDate,
		endDate : commission_endDate,
		callback : function(){
			Ext.getCmp('btnSearchForCommissionStatistics').handler();
		}
	});
	var commission_deptCombo = new Ext.form.ComboBox({
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
				Ext.getCmp('btnSearchForCommissionStatistics').handler();
			}
		}
	});	
	var commission_combo_staffs = new Ext.form.ComboBox({
		id : 'commission_combo_staffs',
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
				Ext.getCmp('btnSearchForCommissionStatistics').handler();
			}
		}
	});
	//---------------------grid
	var cm = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header : '日期', dataIndex : 'orderDateFormat'},
		{header : '菜名', dataIndex : 'foodName'},
		{header : '部门', dataIndex : 'dept'},
		{header : '账单号', dataIndex : 'orderId', renderer : linkOrderId},
		{header : '单价', dataIndex : 'unitPrice', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '数量', dataIndex : 'amount', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '总额', dataIndex : 'totalPrice', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '提成', dataIndex : 'commission', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '人员', dataIndex : 'staffName'}
	]);
	
	cm.defaultSortable = true;
	
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url : '../../QueryCommissionStatistics.do'}),
		reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root'}, [
		{name : 'orderDateFormat'},
		{name : 'foodName'},
		{name : 'dept'},
		{name : 'orderId'},
		{name : 'unitPrice'},
		{name : 'amount'},
		{name : 'totalPrice'},
		{name : 'commission'},
		{name : 'staffName'}
		])
		
	});
	
	var commissionStatisticsTbar = new Ext.Toolbar({
		items : [{
				xtype : 'tbtext',
				text : '日期:'
			}, commission_dateCombo, {
				xtype : 'tbtext',
				text : '&nbsp;'
			}, commission_beginDate , {
				xtype : 'tbtext',
				text : '&nbsp;至&nbsp;'
			}, commission_endDate, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			},{
				xtype : 'tbtext',
				text : '操作人员:'
			}, commission_combo_staffs, 
			{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;部门:'
			},commission_deptCombo ,'->', {
				text : '搜索',
				id : 'btnSearchForCommissionStatistics',
				iconCls : 'btn_search',
				handler : function(e){
					if(!commission_beginDate.isValid() || !commission_endDate.isValid()){
						return;
					}
					var store = commissionStatisticsGrid.getStore();
					store.baseParams['dataSource'] = 'normal';
					store.baseParams['beginDate'] = commission_beginDate.getValue().format('Y-m-d 00:00:00');
					store.baseParams['endDate'] = commission_endDate.getValue().format('Y-m-d 23:59:59');
					store.baseParams['staffId'] = commission_combo_staffs.getValue();
					store.baseParams['deptId'] = commission_deptCombo.getValue();
					store.load({
						params : {
							start : 0,
							limit : limitCount
						}
					});
					
					requestParams = {
						dataSource : 'getDetailChart',
						dateBeg : commission_beginDate.getValue().format('Y-m-d 00:00:00'),
						dateEnd : commission_endDate.getValue().format('Y-m-d 23:59:59'),
						deptID : commission_deptCombo.getValue(),
						staffID : commission_combo_staffs.getValue()					
					};
					
					Ext.Ajax.request({
						url : '../../QueryCommissionStatistics.do',
						params : requestParams,
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							showCommissionDetailChart(jr);
						},
						failure : function(res, opt){
						
						}
					});
					
					if(typeof commissionStaffChart != 'undefined'){
						commission_getStaffChartData();
						console.log(1111);
						$('#divCommissionStaffColumnChart').is(':visible')? commission_loadStaffColumnChart() : commission_loadStaffChart();
						console.log($('#divCommissionStaffColumnChart').is(':visible') + ", " + commissionStatChartTabPanel.getHeight());
						commissionStaffChart.setSize(commissionStatChartTabPanel.getWidth(), panelDrag ? commissionStatChartTabPanel.getHeight() - 60 : commissionStatChartTabPanel.getHeight()-30);
					}					
					
					
				}
			},'-', {
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				if(!commission_beginDate.isValid() || !commission_endDate.isValid()){
					return;
				}
				var url = '../../{0}?beginDate={1}&endDate={2}&staffId={3}&deptId={4}&dataSource={5}';
				url = String.format(
						url, 
						'ExportHistoryStatisticsToExecl.do', 
						commission_beginDate.getValue().format('Y-m-d 00:00:00'), 
						commission_endDate.getValue().format('Y-m-d 23:59:59'),
						commission_combo_staffs.getValue(),
						commission_deptCombo.getValue(),
						'commissionStatisticsList'
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
	
	commissionStatisticsGrid = new Ext.grid.GridPanel({
		id : 'commission_grid',
	    //height : '500',
	    border : true,
	    frame : true,
	    store : ds,
	    cm : cm,
	    viewConfig : {
	    	forceFit : true
	    },
	    loadMask : {
	    	msg : "数据加载中，请稍等..."
	    },
	    tbar : commissionStatisticsTbar,
	    bbar : pagingBar
	});
	commissionStatisticsGrid.region = 'center';
	commissionStatisticsGrid.on('render', function(){
		commission_dateCombo.setValue(1);
		commission_dateCombo.fireEvent('select', commission_dateCombo, null, 1);
	});
	commissionStatisticsGrid.getStore().on('load', function(store, records, options){
		
		if(store.getCount() > 0){
			var sumRow = commissionStatisticsGrid.getView().getRow(store.getCount() - 1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			for(var i = 0; i < commissionStatisticsGrid.getColumnModel().getColumnCount(); i++){
				var sumCell = commissionStatisticsGrid.getView().getCell(store.getCount() - 1, i);
				sumCell.style.fontSize = '15px';
				sumCell.style.fontWeight = 'bold';	
				sumCell.style.color = 'green';
			}
			commissionStatisticsGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
			commissionStatisticsGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
			commissionStatisticsGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
			commissionStatisticsGrid.getView().getCell(store.getCount()-1, 4).innerHTML = '--';
			commissionStatisticsGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '--';
			commissionStatisticsGrid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
			commissionStatisticsGrid.getView().getCell(store.getCount()-1, 9).innerHTML = '--';
		}
	});
	
	commissionDetailPanel = new Ext.Panel({
		title : '提成明细',
		layout:'border',
		region : 'center',
		frame : false, //边框
		//子集
		items : [commissionStatisticsGrid],
		listeners : {
			bodyresize : function(e, w, h){
				if(typeof commissionPanelHeight != 'undefined'){
				
					var chartHeight;
					
					chartHeight = commissionTabPanelHeight + (commissionPanelHeight - h);
					
					commissionStatChartTabPanel.getEl().setTop((h+30)) ;
					
					commission_changeChartWidth(w,chartHeight-60);
					
					if(panelDrag){
						commissionStatChartTabPanel.setHeight(chartHeight);
					}
					commissionStatChartTabPanel.doLayout();
				}
			}			
		}
	});	
}

function commission_changeChartWidth(w,h){
	if(eval($('div:visible[data-type=commissionChart]').attr('data-value'))){
		eval($('div:visible[data-type=commissionChart]').attr('data-value')).setSize(w, h);
	}
	
}

function showCommissionDetailChart(jdata){
	var dateBegin = Ext.getCmp('commission_dateSearchDateBegin').getValue().format('Y-m-d');
	var dateEnd = Ext.getCmp('commission_dateSearchDateEnd').getValue().format('Y-m-d');
	
//	var hourBegin = Ext.getCmp('commission_txtBusinessHourBegin').getEl().dom.textContent;
//	var hourEnd = Ext.getCmp('commission_txtBusinessHourEnd').getEl().dom.textContent;
	
	var chartData = eval('(' + jdata.other.chart + ')');
	commissionDetailChart = new Highcharts.Chart({
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
        	renderTo: 'divCommissionDetailChart'
    	}, 
        title: {
            text: '<b>提成走势图（'+dateBegin+ '至' +dateEnd+'）</b>'
        },
        labels: {
        	items : [{
        		html : '<b>总提成金额:' + chartData.totalMoney + ' 元</b><br><b>日均提成额:' + chartData.avgMoney + ' 元</b>',
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
	
	if(commissionDetailChartPanel && commissionDetailChartPanel.isVisible()){
		commissionDetailChartPanel.show();
	}
}

function commission_getStaffChartData(){
	requestParams.dataSource = 'getStaffChart';
	$.ajax({
		url : '../../QueryCommissionStatistics.do',
		type : 'post',
		async : false,
		data : requestParams,
		success : function(jr, status, xhr){
			commissionStaffChartData.chartData.data = [];
			commissionStaffChartData.staffColumnChart.xAxis = [];
			commissionStaffChartData.staffColumnChart.yAxis.data = [];
			for (var i = 0; i < jr.root.length; i++) {
				commissionStaffChartData.chartData.data.push([jr.root[i].staffName, jr.root[i].commissionPrice]);
				commissionStaffChartData.staffColumnChart.xAxis.push(jr.root[i].staffName);
				commissionStaffChartData.staffColumnChart.yAxis.data.push({y : jr.root[i].commissionPrice, color : colors[i]}); 
			}
		},
		failure : function(res, opt){
		
		}
	});
	
}


function commission_loadStaffChart(){
	
	commissionStaffChart = new Highcharts.Chart({
	    chart: {
	    	renderTo : 'divCommissionStaffChart',
	        plotBackgroundColor: null,
	        plotBorderWidth: null,
	        plotShadow: false
	    },
	    title: {
	        text: '员工提成金额比例图'
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
	    series: [commissionStaffChartData.chartData]
	});				
}


function commission_loadStaffColumnChart(){
 	commissionStaffChart = new Highcharts.Chart({
        chart: {
            type: 'column',
            renderTo : 'divCommissionStaffColumnChart'
        },
        title: {
            text: '员工提成金额柱状图'
        },
        xAxis: {
            categories: commissionStaffChartData.staffColumnChart.xAxis
        },
        yAxis: {
            min: 0,
            title: {
                text: '金额 (元)'
            }
        },
        tooltip: {
            pointFormat: '<table><tbody><tr><td style="color:red;padding:0">金额: </td><td style="padding:0"><b>{point.y} 元</b></td></tr></tbody></table>',
            shared: true,
            useHTML: true
        },
        plotOptions: {
            column: {
                pointPadding: 0.2,
                borderWidth: 0
            }
        },
        series: [commissionStaffChartData.staffColumnChart.yAxis]
    });	
}

var requestParams, panelDrag = false;
var commissionStatChartTabPanel;
var commissionDetailChart, commissionStaffChartPanel;
var commissionDetailChart, commissionStaffChart;
var colors = Highcharts.getOptions().colors;
var commissionStaffChartData = {chartData : {type : 'pie', name : '比例', data : []}, staffColumnChart : {xAxis : [], yAxis : {name : '员工提成', data : [],
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
				            }}}};
Ext.onReady(function(){
	commissionDetailInit();
	
	commissionDetailChartPanel = new Ext.Panel({
		title : '提成走势',
		contentEl : 'divCommissionDetailChart',
		listeners : {
			show : function(thiz){
				//thiz.getEl(): 刚打开页面时thiz.getWidth无效
				if(commissionDetailChart && typeof thiz.getEl() != 'undefined'){
					commissionDetailChart.setSize(thiz.getWidth(), panelDrag ? commissionStatChartTabPanel.getHeight() - 60 : commissionStatChartTabPanel.getHeight()-30);
				}
			}
		}		
	});	
	
	commissionStaffChartPanel = new Ext.Panel({
		title : '员工提成',
		contentEl : 'divCommissionStaffCharts',
		listeners : {
			show : function(thiz){
				if($('#divCommissionStaffChart').is(":visible") || $('#divCommissionStaffColumnChart').is(":visible")){
					commissionStaffChart.setSize(thiz.getWidth(), panelDrag ? commissionStatChartTabPanel.getHeight() - 60 : commissionStatChartTabPanel.getHeight()-30);
				}else{
					$('#divCommissionStaffChartChange').show();
					$('#divCommissionStaffChart').show();
				}
				if(!commissionStaffChart){
					commission_getStaffChartData();
					commission_loadStaffChart();
					
					commissionStaffChart.setSize(commissionStatChartTabPanel.getWidth(), commissionStatChartTabPanel.getHeight()-30);
				}
			},
			render : function(thiz){
				thiz.hasRender = true;
			}
		}
	});		
	
	commissionStatChartTabPanel = new Ext.TabPanel({
		region : 'south',
		height : 330,
		items : [commissionDetailChartPanel, commissionStaffChartPanel],
		listeners : {
			render : function(thiz){
				thiz.setActiveTab(commissionDetailChartPanel);
			}
		}
	});
	new Ext.Panel({
		renderTo : 'divCommissionStatistics',//渲染到
		id : 'commissionStatisticsPanel',
		//solve不跟随窗口的变化而变化
		width : parseInt(Ext.getDom('divCommissionStatistics').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divCommissionStatistics').parentElement.style.height.replace(/px/g,'')),
		border : false,
		layout : 'border',
		//子集
		items : [commissionDetailPanel, commissionStatChartTabPanel]
	});
	
	commissionPanelHeight = commissionDetailPanel.getHeight();
	
	commissionTabPanelHeight = commissionStatChartTabPanel.getHeight();
	
	var commissionRZ = new Ext.Resizable(commissionDetailPanel.getEl(), {
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
    commissionRZ.on('resize', commissionDetailPanel.syncSize, commissionDetailPanel);//注册事件(作用:将调好的大小传个scope执行)
	
	
	
	$('#divCommissionStaffChartChange').toggle(
		function(){
			$('#divCommissionStaffChart').hide();
			
			$('#divCommissionStaffColumnChart').show();
			commission_loadStaffColumnChart();
			commissionStaffChart.setSize(commissionStatChartTabPanel.getWidth(), panelDrag ? commissionStatChartTabPanel.getHeight() - 60 : commissionStatChartTabPanel.getHeight()-30);
		},
		function(){
			$('#divCommissionStaffColumnChart').hide();
			
			$('#divCommissionStaffChart').show();
			commission_loadStaffChart();
			commissionStaffChart.setSize(commissionStatChartTabPanel.getWidth(), panelDrag ? commissionStatChartTabPanel.getHeight() - 60 : commissionStatChartTabPanel.getHeight()-30);
		}		
	);	
//	commissionStatisticsGrid.getStore().load();
});
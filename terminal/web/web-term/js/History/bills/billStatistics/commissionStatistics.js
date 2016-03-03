
Ext.onReady(function(){
	
	var commissionStatisticsGrid, commissionDeptTree, commissionTotalGrid, commissionTotalDeptTree, commissionOrderDetailWin;
	var commissionDetailPanel, commissionTotalPanel;
	var limitCount = 20;
	
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
	
	function commissionDetailInit(){
	
		commission_dateCombo = Ext.ux.createDateCombo({
			beginDate : commission_beginDate,
			endDate : commission_endDate,
			callback : function(){
				Ext.getCmp('commission_btnSearch').handler();
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
							
							if(sendToPageOperation){
								commission_setStatisticsDate();
							}else{
								commission_dateCombo.setValue(1);
								commission_dateCombo.fireEvent('select', commission_dateCombo, null, 1);			
							}		
							
						},
						fialure : function(res, opt){
							thiz.store.loadData(data);
							thiz.setValue(-1);
						}
					});		
				},
				select : function(){
					Ext.getCmp('commission_btnSearch').handler();
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
						params : {privileges : '1000'},
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
					Ext.getCmp('commission_btnSearch').handler();
				}
			}
		});
		
		//门店选择
		var branch_combo_commission = new Ext.form.ComboBox({
			readOnly : false,
			forceSelection : true,
			width : 123,
			listWidth : 120,
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
								data.push([jr.root[0]['id'], jr.root[0]['name']]);
							}else{
								data.push([jr.root[0]['id'], jr.root[0]['name'] + '(集团)']);
								
								for(var i = 0; i < jr.root[0].branches.length; i++){
									data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
								}
							}
							
							thiz.store.loadData(data);
							thiz.setValue(jr.root[0].id);
							thiz.fireEvent('select');
						}
					});
				},
				select : function(){
					//加载部门
					var dept = [[-1, '全部']];
					Ext.Ajax.request({
						url : '../../OperateDept.do',
						params : {
							dataSource : 'getByCond',
							branchId : branch_combo_commission.getValue()
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							
							for(var i = 0; i < jr.root.length; i++){
								dept.push([jr.root[i]['id'], jr.root[i]['name']]);
							}
							
							commission_deptCombo.store.loadData(dept);
							commission_deptCombo.setValue(-1);
						}
					});
					
					//加载操作人员
					var staff = [[-1, '全部']];
					Ext.Ajax.request({
						url : '../../QueryStaff.do',
						params : {
							branchId : branch_combo_commission.getValue()
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							
							for(var i = 0; i < jr.root.length; i++){
								staff.push([jr.root[i]['staffID'], jr.root[i]['staffName']]);
							}
							
							commission_combo_staffs.store.loadData(staff);
							commission_combo_staffs.setValue(-1);
						}
					});
					
					//加载市别
					var hour = [[-1, '全部']];
					Ext.Ajax.request({
						url : '../../OperateBusinessHour.do',
						params : {
							dataSource : 'getByCond',
							branchId : branch_combo_commission.getValue()
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							
							for(var i = 0; i < jr.root.length; i++){
								hour.push([jr.root[i]['id'], jr.root[i]['name'], jr.root[i]['opening'], jr.root[i]['ending']]);
							}
							
							hour.push([-2, '自定义']);
							
							Ext.getCmp('commission_comboBusinessHour').store.loadData(hour);
							Ext.getCmp('commission_comboBusinessHour').setValue(-1);
						}
					});
					
					Ext.getCmp('commission_btnSearch').handler();
				}
			}
		});
		
		
		
		
		
		//---------------------grid
		var cm = new Ext.grid.ColumnModel([
			new Ext.grid.RowNumberer(),
			{header : '日期', dataIndex : 'orderDateFormat'},
			{header : '菜名', dataIndex : 'foodName'},
			{header : '部门', dataIndex : 'dept'},
			{header : '账单号', dataIndex : 'orderId', renderer : function(v){
				return '<a class="orderLinkId">' + v + '</a>';
			}},
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
		
		var commissionStatisticsTbarItem = [{
				xtype : 'tbtext',
				text : '操作人员:'
			}, commission_combo_staffs, 
			{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;部门选择:'
			},commission_deptCombo ,{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;门店选择:'
			}, branch_combo_commission, '->', {
				text : '搜索',
				id : 'commission_btnSearch',
				iconCls : 'btn_search',
				handler : function(e){
					if(!commission_beginDate.isValid() || !commission_endDate.isValid()){
						return;
					}
					
					var businessHour;
					if(commission_hours){
						businessHour = commission_hours;
					}else{
						businessHour = Ext.ux.statistic_oBusinessHourData({type : 'get', statistic : 'commission_'}).data;
					}					
					
					var store = commissionStatisticsGrid.getStore();
					store.baseParams['dataSource'] = 'normal';
					store.baseParams['beginDate'] = Ext.util.Format.date(commission_beginDate.getValue(), 'Y-m-d 00:00:00');
					store.baseParams['endDate'] = Ext.util.Format.date(commission_endDate.getValue(), 'Y-m-d 23:59:59');
					store.baseParams['staffId'] = commission_combo_staffs.getValue();
					store.baseParams['deptId'] = commission_deptCombo.getValue();
					store.baseParams['opening'] = businessHour.opening;
					store.baseParams['ending'] = businessHour.ending;	
					store.baseParams['branchId'] = branch_combo_commission.getValue();
					
					store.load({
						params : {
							start : 0,
							limit : limitCount
						}
					});
					
					if(commission_deptCombo.getValue() && commission_deptCombo.getValue() != -1){
						titleCommissionDeptName = commission_deptCombo.getEl().dom.value + " -- "  ;
					}else{
						titleCommissionDeptName = '';
					}
	
					if(commission_combo_staffs.getValue() && commission_combo_staffs.getValue() != -1){
						titleCommissionStaffName = '员工 : '+ commission_combo_staffs.getEl().dom.value ;
					}else{
						titleCommissionStaffName = '';
					}
					
					requestParams = {
						dataSource : 'getDetailChart',
						dateBeg : Ext.util.Format.date(commission_beginDate.getValue(), 'Y-m-d 00:00:00'),
						dateEnd : Ext.util.Format.date(commission_endDate.getValue(), 'Y-m-d 23:59:59'),
						deptID : commission_deptCombo.getValue(),
						staffID : commission_combo_staffs.getValue(),
						opening : businessHour.opening,
						ending : businessHour.ending,
						branchId : branch_combo_commission.getValue()
					};
					
					
					commission_chartLoadMarsk.show();
					Ext.Ajax.request({
						url : '../../QueryCommissionStatistics.do',
						params : requestParams,
						success : function(res, opt){
							commission_chartLoadMarsk.hide();
							var jr = Ext.decode(res.responseText);
							showCommissionDetailChart(jr);
						},
						failure : function(res, opt){
						
						}
					});
					
					if(typeof commission_staffPieChart != 'undefined'){
						commission_getStaffChartData();
						commission_staffPieChart = commission_loadStaffPieChart(commissionStaffChartPanel.otype);
						commission_staffColumnChart = commission_loadStaffColumnChart(commissionStaffChartPanel.otype);
						commission_staffPieChart.setSize(commissionStatChartTabPanel.getWidth()*0.4, commission_panelDrag ? commissionStatChartTabPanel.getHeight() - commission_cutAfterDrag : commissionStatChartTabPanel.getHeight()-30);
						commission_staffColumnChart.setSize(commissionStatChartTabPanel.getWidth()*0.6, commission_panelDrag ? commissionStatChartTabPanel.getHeight() - commission_cutAfterDrag : commissionStatChartTabPanel.getHeight()-30);
					}					
					
				}
			},'-', {
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				if(!commission_beginDate.isValid() || !commission_endDate.isValid()){
					return;
				}
				var url = '../../{0}?beginDate={1}&endDate={2}&staffId={3}&deptId={4}&dataSource={5}&branchId={6}';
				url = String.format(
						url, 
						'ExportHistoryStatisticsToExecl.do', 
						Ext.util.Format.date(commission_beginDate.getValue(), 'Y-m-d 00:00:00'), 
						Ext.util.Format.date(commission_endDate.getValue(), 'Y-m-d 23:59:59'),
						commission_combo_staffs.getValue(),
						commission_deptCombo.getValue(),
						'commissionStatisticsList',
						branch_combo_commission.getValue()
				);
				window.location = url;
			}
		}];
		
		var commissionStatisticsTbar = Ext.ux.initTimeBar({beginDate:commission_beginDate, endDate:commission_endDate,dateCombo:commission_dateCombo, tbarType : 1, statistic : 'commission_', callback : function businessHourSelect(){commission_hours = null;}}).concat(commissionStatisticsTbarItem);
		
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
		
		commissionStatisticsGrid.getStore().on('load', function(store, records, options){
			
			function showCommissionDetail(orderID){
				var commissionOrderDetailWin;
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
				
				comi_showBillDetailWin();
				commissionOrderDetailWin.show();
				commissionOrderDetailWin.setTitle('账单号: ' + orderID);
				commissionOrderDetailWin.center();
			}
			
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
				
				$('#commissionStatisticsPanel').find('.orderLinkId').each(function(index, element){
        			element.onclick = function(){
        				showCommissionDetail($(element).text());
        			}
        		});
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
						
						commission_changeChartWidth(w,chartHeight-commission_cutAfterDrag);
						
						if(commission_panelDrag){
							commissionStatChartTabPanel.setHeight(chartHeight);
						}
						commissionStatChartTabPanel.doLayout();
					}
				}			
			}
		});	
	}

	//按员工汇总提成数量的柱状和饼图
	$('#staffByAmount_span_commissionChart').click(function(){
		commission_fnChangeStaffChart(this, 1);
	});

	//按员工汇总提成金额的柱状和饼图
	$('#staffByFee_span_commissionChart').click(function(){
		commission_fnChangeStaffChart(this, 0);
	});
	
	function commission_fnChangeStaffChart(thiz, v){
		$(thiz).find('input').attr('checked', 'checked');
		commission_staffPieChart = commission_loadStaffPieChart(v);
		commission_staffColumnChart = commission_loadStaffColumnChart(v);
		commission_staffPieChart.setSize(commissionStatChartTabPanel.getWidth()*0.4, commission_panelDrag ? commissionStatChartTabPanel.getHeight() - commission_cutAfterDrag : commissionStatChartTabPanel.getHeight()-30);
		commission_staffColumnChart.setSize(commissionStatChartTabPanel.getWidth()*0.6, commission_panelDrag ? commissionStatChartTabPanel.getHeight() - commission_cutAfterDrag : commissionStatChartTabPanel.getHeight()-30);
		
		commissionStaffChartPanel.otype = v;
	}
	
	function commission_changeChartWidth(w,h){
		if(eval($('div:visible[data-type=commissionChart]').attr('data-value'))){
			if($('div:visible[data-type=commissionChart]').length == 1){
				eval($('div:visible[data-type=commissionChart]').attr('data-value')).setSize(w, h);
			}else if($('div:visible[data-type=commissionChart]').length > 1){
				eval($($('div:visible[data-type=commissionChart]')[0]).attr('data-value')).setSize(w*0.4, h);
				eval($($('div:visible[data-type=commissionChart]')[1]).attr('data-value')).setSize(w*0.6, h);				
			}
		}
		
	}

	function showCommissionDetailChart(jdata){
		var dateBegin = Ext.util.Format.date(Ext.getCmp('commission_dateSearchDateBegin').getValue(), 'Y-m-d');
		var dateEnd = Ext.util.Format.date(Ext.getCmp('commission_dateSearchDateEnd').getValue(), 'Y-m-d');
		
		var hourBegin = Ext.getCmp('commission_txtBusinessHourBegin').getEl().dom.textContent;
		var hourEnd = Ext.getCmp('commission_txtBusinessHourEnd').getEl().dom.textContent;
		
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
	            text: '<b>'+ titleCommissionDeptName +'提成走势图（'+dateBegin+ '至' +dateEnd+'）'+hourBegin+ ' - ' + hourEnd + titleCommissionStaffName +'</b>'
	        },
	        labels: {
	        	items : [{
	        		html : '<b>总提成金额:' + chartData.totalMoney + ' 元</b><br><b>日均提成金额:' + chartData.avgMoney + ' 元</b><br><b>日均提成数量:' + chartData.avgCount + ' 份</b>',
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
		
		if(commissionDetailChartPanel && commissionDetailChartPanel.isVisible()){
			commissionDetailChartPanel.show();
		}
	}

	var commission_getStaffChartData = function(){
		requestParams.dataSource = 'getStaffChart';
		$.ajax({
			url : '../../QueryCommissionStatistics.do',
			type : 'post',
			async : false,
			data : requestParams,
			success : function(jr, status, xhr){
				resetChartDate(commissionStaffChartData);
				
				for (var i = 0; i < jr.root.length; i++) {
					if(jr.root[i].commissionPrice != 0)
						commissionStaffChartData.chartPriceData.data.push([jr.root[i].staffName, jr.root[i].commissionPrice]);
						
					if(jr.root[i].commissionAmount != 0)	
						commissionStaffChartData.chartAmountData.data.push([jr.root[i].staffName, jr.root[i].commissionAmount]);
					commissionStaffChartData.priceColumnChart.xAxis.push(jr.root[i].staffName);
					commissionStaffChartData.priceColumnChart.yAxis.data.push({y : jr.root[i].commissionPrice, color : colors[i]}); 
					commissionStaffChartData.amountColumnChart.yAxis.data.push({y : jr.root[i].commissionAmount, color : colors[i]});
				}
			},
			failure : function(res, opt){
			
			}
		});
	};


	var commission_loadStaffPieChart = function(type){
		
		var content = {};
		content.title = type==1?'员工提成数量比例图':'员工提成金额比例图';
		content.unit = type==1?'份':'元';
		content.series = type==1?commissionStaffChartData.chartAmountData:commissionStaffChartData.chartPriceData;
		
		return newPieChart({rt: 'divCommissionStaffPieChart', title : content.title, unit: content.unit, series: content.series});			
	};
	
	
	var commission_loadStaffColumnChart = function(type){
		var content = {};
		content.title = type==1?'员工提成数量柱状图':'员工提成金额柱状图';
		content.unit = type==1?'份':'元';
		content.series = type==1?commissionStaffChartData.amountColumnChart.yAxis : commissionStaffChartData.priceColumnChart.yAxis;	
	    
	    return newColumnChart({
	    	rt: 'divCommissionStaffColumnChart', title : content.title, unit: content.unit, series: content.series, xAxis:commissionStaffChartData.priceColumnChart.xAxis	
	    });	
	};

	var commission_setStatisticsDate = function(){
		if(sendToPageOperation){
			commission_beginDate.setValue(sendToStatisticsPageBeginDate);
			commission_endDate.setValue(sendToStatisticsPageEndDate);	
			
			commission_hours = sendToStatisticsPageHours;
			
			Ext.getCmp('commission_btnSearch').handler();
			
			Ext.getCmp('commission_txtBusinessHourBegin').setText('<font style="color:green; font-size:20px">'+commission_hours.openingText+'</font>');
			Ext.getCmp('commission_txtBusinessHourEnd').setText('<font style="color:green; font-size:20px">'+commission_hours.endingText+'</font>');
			Ext.getCmp('commission_comboBusinessHour').setValue(commission_hours.hourComboValue);	
			
			sendToPageOperation = false;
		}
	};

	var commission_cutAfterDrag = 70, commission_cutBeforeDrag = 40, commission_hours;
	var requestParams, commission_panelDrag = false;
	var commissionStatChartTabPanel;
	var commissionDetailChart, commissionStaffChartPanel;
	var commissionDetailChart, commission_staffPieChart, commission_staffColumnChart;
	var colors = Highcharts.getOptions().colors;
	var commissionStaffChartData = Wireless.chart.initChartData({priceName: '员工提成金额', countName: '员工提成数量'});
	
	var titleCommissionDeptName, titleCommissionStaffName;
	var commission_chartLoadMarsk, commission_dateCombo;

	commissionDetailInit();
	
	commissionDetailChartPanel = new Ext.Panel({
		title : '提成走势',
		contentEl : 'divCommissionDetailChart',
		listeners : {
			show : function(thiz){
				//thiz.getEl(): 刚打开页面时thiz.getWidth无效
				if(commissionDetailChart && typeof thiz.getEl() != 'undefined'){
					commissionDetailChart.setSize(thiz.getWidth(), commission_panelDrag ? commissionStatChartTabPanel.getHeight() - 60 : commissionStatChartTabPanel.getHeight()-30);
				}
			}
		}		
	});	
	
	commissionStaffChartPanel = new Ext.Panel({
		title : '按员工汇总',
		contentEl : 'divCommissionStaffCharts',
		listeners : {
			render : function(thiz){
				thiz.hasRender = true;
			}
		}
	});		
	
	commissionStatChartTabPanel = new Ext.TabPanel({
		region : 'south',
		height : 430,
		items : [commissionDetailChartPanel, commissionStaffChartPanel],
		listeners : {
			render : function(thiz){
				thiz.setActiveTab(commissionDetailChartPanel);
			}
		}
	});
	
	commissionStaffChartPanel.addListener('show', function(){
		var charts = Wireless.chart.initChartPanel({
			divLeftShowChart : 'divCommissionStaffPieChart',
			divRightShowChart : 'divCommissionStaffColumnChart',
			generalName : 'commissionStaff_',
			getChartData : commission_getStaffChartData,
			leftChartLoad : commission_loadStaffPieChart,
			rightChartLoad : commission_loadStaffColumnChart,
			panelDrag : true,
			leftChart : commission_staffPieChart,
			rightChart : commission_staffColumnChart,
			loadType : 1,
			tabPanel : commissionStatChartTabPanel,
			cutAfterDrag : commission_cutAfterDrag,
			cutBeforeDrag : commission_cutBeforeDrag		
			
		});	
		commission_staffPieChart = charts.pie;
		commission_staffColumnChart = charts.column;
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
	
	commission_chartLoadMarsk = new Ext.LoadMask(commissionStatChartTabPanel.getEl().dom, {
	    msg  : '数据统计中，请稍候......',
	    disabled : false
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
        		commission_panelDrag = true;
        	}
        }
    });
    commissionRZ.on('resize', commissionDetailPanel.syncSize, commissionDetailPanel);//注册事件(作用:将调好的大小传个scope执行)
	
    var commission_totalHeight = Ext.getCmp('commissionStatisticsPanel').getHeight();
    
    commissionDetailPanel.setHeight(commission_totalHeight*0.45);
    commissionDetailPanel.getEl().parent().setHeight(commission_totalHeight*0.45);
    
    commissionStatChartTabPanel.setHeight(commission_totalHeight*0.55);	
    
    commissionRZ.resizeTo(commissionDetailPanel.getWidth(), commission_totalHeight*0.45);
    
    Ext.getCmp('commissionStatistics').updateStatisticsDate = commission_setStatisticsDate;
    
});
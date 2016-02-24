
Ext.onReady(function(){
	var cancelFoodStatWin, cancelFoodStatWinTabPanel, cancelFoodDetailsStatPanel, cancelFoodByDeptStatPanel, cancelFoodByReasonStatPanel;
	var cfdsGrid;
	var cancel_FOOD_PAGE_LIMIT = 22;
	
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
						url : '../../OperateCancelReason.do',
						params : {
							dataSource : 'getByCond'
						},
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
						params : {privileges : '1001'},
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
		
		var cfdsGridDateTbar = Ext.ux.initTimeBar({beginDate:beginDate, endDate:endDate,dateCombo:cancel_dateCombo, tbarType : 0, statistic : 'cancel_', callback : function businessHourSelect(){cancel_hours = null;}});
		
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
					
					var businessHour;
					if(cancel_hours){
						businessHour = cancel_hours;
					}else{
						businessHour = Ext.ux.statistic_oBusinessHourData({type : 'get', statistic : 'cancel_'}).data;
					}
					
					if(businessHour.businessHourType != -1){
						opening = businessHour.opening;
						ending = businessHour.ending;
					}else{
						opening = '';
						ending = '';
					}					
					var gs = cfdsGrid.getStore();
					gs.baseParams['deptID'] = cancel_deptCombo.getValue();
					gs.baseParams['dateBeg'] = Ext.util.Format.date(beginDate.getValue(), 'Y-m-d 00:00:00');
					gs.baseParams['dateEnd'] = Ext.util.Format.date(endDate.getValue(), 'Y-m-d 23:59:59');
					gs.baseParams['reasonID'] = reasonCombo.getValue();
					gs.baseParams['staffID'] = cancel_combo_staffs.getValue();
					gs.baseParams['opening'] = opening;
					gs.baseParams['ending'] = ending;
					gs.load({
						params : {
							start : 0,
							limit : cancel_FOOD_PAGE_LIMIT
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
						dateBeg : Ext.util.Format.date(beginDate.getValue(), 'Y-m-d 00:00:00'),
						dateEnd : Ext.util.Format.date(endDate.getValue(), 'Y-m-d 23:59:59'),
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
					if(typeof cancel_reasonPieChart != 'undefined' && typeof cancelledReasonChartPanel.hasRender != 'undefined'){
						cancel_getRaasonChartData();
						cancel_reasonPieChart = cancel_loadReasonPieChart(cancelFoodDetailsStatPanel.otype);
						cancel_reasonColumnChart = cancel_loadReasonColumnChart(cancelFoodDetailsStatPanel.otype);
						cancel_reasonPieChart.setSize(cancelFoodStatChartTabPanel.getWidth()*0.4, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - cancel_cutAfterDrag : cancelFoodStatChartTabPanel.getHeight()-cancel_cutChartHeight);
						cancel_reasonColumnChart.setSize(cancelFoodStatChartTabPanel.getWidth()*0.6, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - cancel_cutAfterDrag : cancelFoodStatChartTabPanel.getHeight()-cancel_cutChartHeight);
					}
					
					if(typeof cancel_staffPieChart != 'undefined' && typeof cancelledStaffChartPanel.hasRender != 'undefined'){
						cancel_getStaffChartData();
						cancel_staffPieChart = cancel_loadStaffPieChart(cancelFoodDetailsStatPanel.otype);
						cancel_staffColumnChart = cancel_loadStaffColumnChart(cancelFoodDetailsStatPanel.otype);
						cancel_staffPieChart.setSize(cancelFoodStatChartTabPanel.getWidth()*0.4, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - cancel_cutAfterDrag : cancelFoodStatChartTabPanel.getHeight()-cancel_cutChartHeight);
						cancel_staffColumnChart.setSize(cancelFoodStatChartTabPanel.getWidth()*0.6, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - cancel_cutAfterDrag : cancelFoodStatChartTabPanel.getHeight()-cancel_cutChartHeight);
					}
					
					if(typeof cancel_deptPieChart != 'undefined' && typeof cancelledDeptChartPanel.hasRender != 'undefined'){
						cancel_getDeptChartData();
						cancel_deptPieChart = cancel_loadDeptPieChart(cancelFoodDetailsStatPanel.otype);
						cancel_deptColumnChart = cancel_loadDeptColumnChart(cancelFoodDetailsStatPanel.otype);
						cancel_deptPieChart.setSize(cancelFoodStatChartTabPanel.getWidth()*0.4, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - cancel_cutAfterDrag : cancelFoodStatChartTabPanel.getHeight()-cancel_cutChartHeight);
						cancel_deptColumnChart.setSize(cancelFoodStatChartTabPanel.getWidth()*0.6, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - cancel_cutAfterDrag : cancelFoodStatChartTabPanel.getHeight()-cancel_cutChartHeight);
						
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
						Ext.util.Format.date(beginDate.getValue(), 'Y-m-d 00:00:00'),
						Ext.util.Format.date(endDate.getValue(), 'Y-m-d 23:59:59'),
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
	         ['账单号', 'orderId', null, null, function(v){
	         	return '<a class="orderLinkId">' + v + '</a>';
	         }],
	         ['单价','unitPrice', null, 'right', Ext.ux.txtFormat.gridDou],
	         ['退菜数量','count', null, 'right', Ext.ux.txtFormat.gridDou], 
	         ['退菜金额','totalPrice', null, 'right', Ext.ux.txtFormat.gridDou],		              
	         ['操作人','waiter'], 
	         ['退菜原因','cancelReason.reason', 200]
			],
			['orderDateFormat', 'name', 'kitchen.dept.name', 'orderId', 'unitPrice', 'count', 'totalPrice', 'waiter', 'cancelReason.reason'],
			[ ['dataSource', 'getDetail']],
			cancel_FOOD_PAGE_LIMIT,
			null,
			[cfdsGridTbar, cfdsGridDateTbar]
		);
		cfdsGrid.region = 'center';
	
		cfdsGrid.getStore().on('load', function(store, records, options){
			
			function showCancelDetail(orderID){
				var cancellFoodOrderDetailWin;
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
								thiz.load({
									url : '../window/history/orderDetail.jsp', 
									scripts : true,
									params : {
										orderId : orderID,
										foodStatus : 'isReturn'
									},
									method : 'post'
								});
								thiz.center();	
							}
						}
					});
				}
				cancellFood_showBillDetailWin();
				cancellFoodOrderDetailWin.show();
				cancellFoodOrderDetailWin.setTitle('账单号: ' + orderID);
				cancellFoodOrderDetailWin.center();
			}
			
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
				
				$('#cancelledFoodPanel').find('.orderLinkId').each(function(index, element){
        			element.onclick = function(){
        				showCancelDetail($(element).text());
        			}
        		});
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
						
						cancel_changeChartWidth(w,chartHeight-cancel_cutAfterDrag);
						
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
		if(eval($('div:visible[data-type=cancelChart]').attr('data-value'))){
			if($('div:visible[data-type=cancelChart]').length == 1){
				eval($('div:visible[data-type=cancelChart]').attr('data-value')).setSize(w, h);
			}else if($('div:visible[data-type=cancelChart]').length > 1){
				eval($($('div:visible[data-type=cancelChart]')[0]).attr('data-value')).setSize(w*0.4, h);
				eval($($('div:visible[data-type=cancelChart]')[1]).attr('data-value')).setSize(w*0.6, h);				
			}
		}
	}

	var cancel_getRaasonChartData = function(){
		cancel_requestParams.dataSource = 'getReasonChart';
		$.ajax({
			url : '../../QueryCancelledFood.do',
			type : 'post',
			async : false,
			data : cancel_requestParams,
			success : function(jr, status, xhr){
				resetChartDate(reasonChartData);
				for (var i = 0; i < jr.root.length; i++) {
					if(jr.root[i].cancelAmount != 0)
						reasonChartData.chartPriceData.data.push([jr.root[i].reason, jr.root[i].cancelAmount]);
					if(jr.root[i].cancelPrice != 0)	
						reasonChartData.chartAmountData.data.push([jr.root[i].reason, jr.root[i].cancelPrice]);
					reasonChartData.priceColumnChart.xAxis.push(jr.root[i].reason);
					reasonChartData.amountColumnChart.yAxis.data.push({y : jr.root[i].cancelAmount, color : colors[i]});
					reasonChartData.priceColumnChart.yAxis.data.push({y : jr.root[i].cancelPrice, color : colors[i]});
					
				}
			},
			failure : function(res, opt){
			
			}
		});
	};

	var cancel_getStaffChartData = function(){
		cancel_requestParams.dataSource = 'getStaffChart';
		$.ajax({
			url : '../../QueryCancelledFood.do',
			type : 'post',
			async : false,
			data : cancel_requestParams,
			success : function(jr, status, xhr){
				resetChartDate(cancel_staffChartData);
				for (var i = 0; i < jr.root.length; i++) {
					if(jr.root[i].cancelPrice != 0)
						cancel_staffChartData.chartPriceData.data.push([jr.root[i].cancelStaff, jr.root[i].cancelPrice]);
					if(jr.root[i].cancelAmount != 0)
						cancel_staffChartData.chartAmountData.data.push([jr.root[i].cancelStaff, jr.root[i].cancelAmount]);
					cancel_staffChartData.priceColumnChart.xAxis.push(jr.root[i].cancelStaff);
					cancel_staffChartData.priceColumnChart.yAxis.data.push({y : jr.root[i].cancelPrice, color : colors[i]}); 
					cancel_staffChartData.amountColumnChart.yAxis.data.push({y : jr.root[i].cancelAmount, color : colors[i]});
				}
			},
			failure : function(res, opt){
			
			}
		});
	};

	var cancel_getDeptChartData = function(){
		cancel_requestParams.dataSource = 'getDeptChart';
		$.ajax({
			url : '../../QueryCancelledFood.do',
			type : 'post',
			async : false,
			data : cancel_requestParams,
			success : function(jr, status, xhr){
				resetChartDate(cancel_deptChartData);
				for (var i = 0; i < jr.root.length; i++) {
					if(jr.root[i].cancelPrice != 0)
						cancel_deptChartData.chartPriceData.data.push([jr.root[i].cancelDept.name, jr.root[i].cancelPrice]);
					if(jr.root[i].cancelAmount != 0)
						cancel_deptChartData.chartAmountData.data.push([jr.root[i].cancelDept.name, jr.root[i].cancelAmount]);
					
					cancel_deptChartData.priceColumnChart.xAxis.push(jr.root[i].cancelDept.name);
					cancel_deptChartData.priceColumnChart.yAxis.data.push({y : jr.root[i].cancelPrice, color : colors[i]}); 
					cancel_deptChartData.amountColumnChart.yAxis.data.push({y : jr.root[i].cancelAmount, color : colors[i]});
				}
			},
			failure : function(res, opt){
			
			}
		});
	};
	
	var cancel_loadReasonColumnChart = function(type){
		var content = {};
		content.title = type==1?'退菜原因数量柱状图':'退菜原因金额柱状图';
		content.unit = type==1?'份':'元';
		content.series = type==1?reasonChartData.amountColumnChart.yAxis : reasonChartData.priceColumnChart.yAxis;	
	    
	    return newColumnChart({
	    	rt: 'divCancelReasonColumnChart', title : content.title, unit: content.unit, series: content.series, xAxis:reasonChartData.priceColumnChart.xAxis	
	    });	
	};

	var cancel_loadStaffColumnChart = function(type){
		
		var content = {};
		content.title = type==1?'员工退菜数量柱状图':'员工退菜金额柱状图';
		content.unit = type==1?'份':'元';
		content.series = type==1?cancel_staffChartData.amountColumnChart.yAxis : cancel_staffChartData.priceColumnChart.yAxis;	
	    
	    return newColumnChart({
	    	rt: 'divCancelStaffAmountColumnChart', title : content.title, unit: content.unit, series: content.series, xAxis:cancel_staffChartData.priceColumnChart.xAxis	
	    });
	};

	var cancel_loadDeptColumnChart = function(type){
		var content = {};
		content.title = type==1?'部门退菜数量柱状图':'部门退菜金额柱状图';
		content.unit = type==1?'份':'元';
		content.series = type==1?cancel_deptChartData.amountColumnChart.yAxis : cancel_deptChartData.priceColumnChart.yAxis;	
	    
	    return newColumnChart({
	    	rt: 'divCancelDeptColumnChart', title : content.title, unit: content.unit, series: content.series, xAxis:cancel_deptChartData.priceColumnChart.xAxis	
	    });	
	};

	var cancel_loadReasonPieChart = function(type){
		
		var content = {};
		content.title = type==1?'退菜原因数量比例图':'退菜原因金额比例图';
		content.unit = type==1?'份':'元';
		content.series = type==1?reasonChartData.chartAmountData:reasonChartData.chartPriceData;
		
		return newPieChart({rt: 'divCancelReasonPieChart', title : content.title, unit: content.unit, series: content.series});	
	};
	
	var cancel_loadStaffPieChart = function(type){
		var content = {};
		content.title = type==1?'员工退菜数量比例图':'员工退菜金额比例图';
		content.unit = type==1?'份':'元';
		content.series = type==1?cancel_staffChartData.chartAmountData:cancel_staffChartData.chartPriceData;
		
		return newPieChart({rt: 'divCancelStaffAmountPieChart', title : content.title, unit: content.unit, series: content.series});	
	};

	var cancel_loadDeptPieChart = function(type){
		var content = {};
		content.title = type==1?'部门退菜数量比例图':'部门退菜金额比例图';
		content.unit = type==1?'份':'元';
		content.series = type==1?cancel_deptChartData.chartAmountData:cancel_deptChartData.chartPriceData;
		
		return newPieChart({rt: 'divCancelDeptPieChart', title : content.title, unit: content.unit, series: content.series});
	};

	function showCancelDetailChart(jdata){
		var dateBegin = Ext.util.Format.date(Ext.getCmp('cancel_dateSearchDateBegin').getValue(), 'Y-m-d');
		var dateEnd = Ext.util.Format.date(Ext.getCmp('cancel_dateSearchDateEnd').getValue(), 'Y-m-d');
		
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

	function cancel_fnChangeReasonChart(thiz, v){
		$(thiz).find('input').attr('checked', 'checked');
		
		cancel_reasonPieChart = cancel_loadReasonPieChart(v);
		cancel_reasonColumnChart = cancel_loadReasonColumnChart(v);
		cancel_reasonPieChart.setSize(cancelFoodStatChartTabPanel.getWidth()*0.4, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - cancel_cutAfterDrag : cancelFoodStatChartTabPanel.getHeight()-cancel_cutChartHeight);
		cancel_reasonColumnChart.setSize(cancelFoodStatChartTabPanel.getWidth()*0.6, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - cancel_cutAfterDrag : cancelFoodStatChartTabPanel.getHeight()-cancel_cutChartHeight);
		
		cancelFoodDetailsStatPanel.otype = v;
	}
	
	function cancel_fnChangeDeptChart(thiz, v){
		$(thiz).find('input').attr('checked', 'checked');
		cancel_deptPieChart = cancel_loadDeptPieChart(v);
		cancel_deptColumnChart = cancel_loadDeptColumnChart(v);
		cancel_deptPieChart.setSize(cancelFoodStatChartTabPanel.getWidth()*0.4, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - cancel_cutAfterDrag : cancelFoodStatChartTabPanel.getHeight()-cancel_cutChartHeight);
		cancel_deptColumnChart.setSize(cancelFoodStatChartTabPanel.getWidth()*0.6, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - cancel_cutAfterDrag : cancelFoodStatChartTabPanel.getHeight()-cancel_cutChartHeight);
		
		cancelFoodDetailsStatPanel.otype = v;
	}
	function cancel_fnChangeStaffChart(thiz, v){
		$(thiz).find('input').attr('checked', 'checked');
		cancel_staffPieChart = cancel_loadStaffPieChart(v);
		cancel_staffColumnChart = cancel_loadStaffColumnChart(v);
		cancel_staffPieChart.setSize(cancelFoodStatChartTabPanel.getWidth()*0.4, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - cancel_cutAfterDrag : cancelFoodStatChartTabPanel.getHeight()-cancel_cutChartHeight);
		cancel_staffColumnChart.setSize(cancelFoodStatChartTabPanel.getWidth()*0.6, cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - cancel_cutAfterDrag : cancelFoodStatChartTabPanel.getHeight()-cancel_cutChartHeight);
		
		cancelFoodDetailsStatPanel.otype = v;
	}

	var cancel_setStatisticsDate = function(){
		if(sendToPageOperation){
			Ext.getCmp('cancel_dateSearchDateBegin').setValue(sendToStatisticsPageBeginDate);
			Ext.getCmp('cancel_dateSearchDateEnd').setValue(sendToStatisticsPageEndDate);	
			
			cancel_hours = sendToStatisticsPageHours;
			
			Ext.getCmp('cancel_btnSearch').handler();
			
			Ext.getCmp('cancel_txtBusinessHourBegin').setText('<font style="color:green; font-size:20px">'+cancel_hours.openingText+'</font>');
			Ext.getCmp('cancel_txtBusinessHourEnd').setText('<font style="color:green; font-size:20px">'+cancel_hours.endingText+'</font>');
			Ext.getCmp('cancel_comboBusinessHour').setValue(cancel_hours.hourComboValue);		
			
			sendToPageOperation = false;
		}
	};
	var cancel_cutAfterDrag=70, cancel_cutBeforeDrag=40;
	var cancel_requestParams, cancel_panelDrag=false, cancelPanelHeight, cancelTabPanelHeight, cancel_hours;
	var cancel_detailChart, cancel_staffPieChart, cancel_staffColumnChart, cancel_deptPieChart, cancel_deptColumnChart, cancel_reasonPieChart, cancel_reasonColumnChart;
	var cancelledDetailChartPanel, cancelledReasonChartPanel, cancelledStaffChartPanel, cancelledDeptChartPanel, cancelFoodStatChartTabPanel;
	var colors = Highcharts.getOptions().colors;
	var reasonChartData = Wireless.chart.initChartData({priceName:'退菜原因金额', countName:'退菜原因数量'});
	var cancel_staffChartData = Wireless.chart.initChartData({priceName:'员工退菜金额', countName:'员工退菜数量'});
	var cancel_deptChartData = Wireless.chart.initChartData({priceName:'部门退菜金额', countName:'部门退菜数量'});
					            
	var titleCancelStaffName, titleCancelDeptName;		


	var cancel_chartLoadMarsk, cancel_dateCombo;

	cancelFoodDetailsStatPanelInit();
	
	cancelledDetailChartPanel = new Ext.Panel({
		title : '退菜走势',
		contentEl : 'divCancelledDetailChart',
		listeners : {
			show : function(thiz){
				//thiz.getEl(): 刚打开页面时thiz.getWidth无效
				if(cancel_detailChart && typeof thiz.getEl() != 'undefined'){
					cancel_detailChart.setSize(thiz.getWidth(), cancel_panelDrag ? cancelFoodStatChartTabPanel.getHeight() - (cancel_cutAfterDrag-20) : cancelFoodStatChartTabPanel.getHeight()-30);
				}
			}
		}		
	});
	
	cancelledReasonChartPanel = new Ext.Panel({
		title : '按原因汇总',
		contentEl : 'divCancelledReasonCharts',
		height : 320,
		listeners : {
			render : function(thiz){
				thiz.hasRender = true;
			}
		}
	});
	
	cancelledStaffChartPanel = new Ext.Panel({
		title : '按员工汇总',
		contentEl : 'divCancelledStaffCharts',
		listeners : {
			render : function(thiz){
				thiz.hasRender = true;
			}
		}
	});	
	
	cancelledDeptChartPanel = new Ext.Panel({
		title : '按部门汇总',
		contentEl : 'divCancelledDeptCharts',
		listeners : {
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
	
	cancelledReasonChartPanel.addListener('show', function(){
		var charts = Wireless.chart.initChartPanel({
			divLeftShowChart : 'divCancelReasonPieChart',
			divRightShowChart : 'divCancelReasonColumnChart',
			generalName : 'cancelReason_',
			getChartData : cancel_getRaasonChartData,
			leftChartLoad : cancel_loadReasonPieChart,
			rightChartLoad : cancel_loadReasonColumnChart,
			panelDrag : true,
			leftChart : cancel_reasonPieChart,
			rightChart : cancel_reasonColumnChart,
			loadType : 0,
			tabPanel : cancelFoodStatChartTabPanel,
			cutAfterDrag : cancel_cutAfterDrag,
			cutBeforeDrag : cancel_cutBeforeDrag		
			
		});	
		cancel_reasonPieChart = charts.pie;
		cancel_reasonColumnChart = charts.column;
		
		charts = null;
	});	
	
	cancelledStaffChartPanel.addListener('show', function(){
		var charts = Wireless.chart.initChartPanel({
			divLeftShowChart : 'divCancelStaffAmountPieChart',
			divRightShowChart : 'divCancelStaffAmountColumnChart',
			generalName : 'cancelStaff_',
			getChartData : cancel_getStaffChartData,
			leftChartLoad : cancel_loadStaffPieChart,
			rightChartLoad : cancel_loadStaffColumnChart,
			panelDrag : true,
			leftChart : cancel_staffPieChart,
			rightChart : cancel_staffColumnChart,
			loadType : 1,
			tabPanel : cancelFoodStatChartTabPanel,
			cutAfterDrag : cancel_cutAfterDrag,
			cutBeforeDrag : cancel_cutBeforeDrag		
			
		});	
		cancel_staffPieChart = charts.pie;
		cancel_staffColumnChart = charts.column;
		
		charts = null;
	});		
	
	cancelledDeptChartPanel.addListener('show', function(){
		var charts = Wireless.chart.initChartPanel({
			divLeftShowChart : 'divCancelDeptPieChart',
			divRightShowChart : 'divCancelDeptColumnChart',
			generalName : 'cancelDept_',
			getChartData : cancel_getDeptChartData,
			leftChartLoad : cancel_loadDeptPieChart,
			rightChartLoad : cancel_loadDeptColumnChart,
			panelDrag : true,
			leftChart : cancel_deptPieChart,
			rightChart : cancel_deptColumnChart,
			loadType : 0,
			tabPanel : cancelFoodStatChartTabPanel,
			cutAfterDrag : cancel_cutAfterDrag,
			cutBeforeDrag : cancel_cutBeforeDrag		
			
		});	
		cancel_deptPieChart = charts.pie;
		cancel_deptColumnChart = charts.column;
		
		charts = null;
		
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
	
	cancel_chartLoadMarsk = new Ext.LoadMask(cancelFoodStatChartTabPanel.getEl().dom, {
	    msg  : '数据统计中，请稍候......',
	    disabled : false
	});	
	
    var cancel_totalHeight = Ext.getCmp('cancelledFoodPanel').getHeight();
    
    cancelFoodDetailsStatPanel.setHeight(cancel_totalHeight*0.45);
    cancelFoodDetailsStatPanel.getEl().parent().setHeight(cancel_totalHeight*0.45);
    
    cancelFoodStatChartTabPanel.setHeight(cancel_totalHeight*0.55);	
    
    cancel_rz.resizeTo(cancelFoodDetailsStatPanel.getWidth(), cancel_totalHeight*0.45);
	
	Ext.getCmp('cancelledFood').updateStatisticsDate = cancel_setStatisticsDate;
});

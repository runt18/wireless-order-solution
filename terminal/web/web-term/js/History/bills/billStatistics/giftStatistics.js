
Ext.onReady(function(){
	
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
						url : '../../OperateRegion.do',
						params : {
							dataSource : 'getByCond'
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
	
	function initGiftStatisticsGrid(){
		var beginDate = new Ext.form.DateField({
			id : 'beginDate_combo_giftStatistics',
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
			id : 'endDate_combo_giftStatistics',
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
		
		//门店选择
		var branch_combo_gift = new Ext.form.ComboBox({
			id : 'branch_combo_gift',
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
				select : function(isJump){
					//加载赠送人
					var staff = [[-1, '全部']];
					Ext.Ajax.request({
						url : '../../QueryStaff.do',
						params : {
							branchId : branch_combo_gift.getValue()
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							
							for(var i = 0; i < jr.root.length; i++){
								staff.push([jr.root[i]['staffID'], jr.root[i]['staffName']]);
							}
							
							gift_combo_staffs.store.loadData(staff);
							gift_combo_staffs.setValue(-1);
						}
					});
					
					//加载区域
					var region = [[-1, '全部']];
					Ext.Ajax.request({
						url : '../../OperateRegion.do',
						params : {
							dataSource : 'getByCond',
							branchId : branch_combo_gift.getValue()
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							for(var i = 0; i < jr.root.length; i++){
								region.push([jr.root[i]['id'], jr.root[i]['name']]);
							}
							Ext.getCmp('giftStatistic_comboRegion').store.loadData(region);
							Ext.getCmp('giftStatistic_comboRegion').setValue(-1);
						}
					});
					
					//加载市别
					var hour = [[-1, '全部']];
					Ext.Ajax.request({
						url : '../../OperateBusinessHour.do',
						params : {
							dataSource : 'getByCond',
							branchId : branch_combo_gift.getValue()
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							
							for(var i = 0; i < jr.root.length; i++){
								hour.push([jr.root[i]['id'], jr.root[i]['name'], jr.root[i]['opening'], jr.root[i]['ending']]);
							}
							
							hour.push([-2, '自定义']);
							
							Ext.getCmp('giftStatistic_comboBusinessHour').store.loadData(hour);
							Ext.getCmp('giftStatistic_comboBusinessHour').setValue(-1);
						}
					});
					
					if(!isJump){
						Ext.getCmp('giftStatistic_btnSearch').handler();
					}
				
				}
			}
		});
		
		var grid_giftStatisticsTbar = new Ext.Toolbar({
			items : [{
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
			},gift_combo_staffs, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;门店选择:'
			},branch_combo_gift, '->', {
				text : '搜索',
				id : 'giftStatistic_btnSearch',
				iconCls : 'btn_search',
				handler : function(){
					if(!beginDate.isValid() || !endDate.isValid()){
						return;
					}
					
					var businessHour;
					if(giftStatistic_hours){
						businessHour = giftStatistic_hours;
					}else{
						businessHour = Ext.ux.statistic_oBusinessHourData({type : 'get', statistic : 'giftStatistic_'}).data;
					}					
					
					var gs = grid_giftStatistics.getStore();
					gs.baseParams['onDuty'] = Ext.util.Format.date(beginDate.getValue(), 'Y-m-d 00:00:00');
					gs.baseParams['offDuty'] = Ext.util.Format.date(endDate.getValue(), 'Y-m-d 23:59:59');
					gs.baseParams['region'] = Ext.getCmp('giftStatistic_comboRegion').getValue();
					gs.baseParams['foodName'] = Ext.getCmp('gift_foodName').getValue();
					gs.baseParams['giftStaffId'] = gift_combo_staffs.getValue();
					gs.baseParams['opening'] = businessHour.opening;
					gs.baseParams['ending'] = businessHour.ending;
					gs.baseParams['branchId'] = branch_combo_gift.getValue();
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
						dateBeg : Ext.util.Format.date(beginDate.getValue(), 'Y-m-d 00:00:00'),
						dateEnd : Ext.util.Format.date(endDate.getValue(), 'Y-m-d 23:59:59'),
						region : Ext.getCmp('giftStatistic_comboRegion').getValue(),
						giftStaffId : gift_combo_staffs.getValue(),
						foodName : Ext.getCmp('gift_foodName').getValue(),
						opening : businessHour.opening,
						ending : businessHour.ending,
						branchId : branch_combo_gift.getValue()
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
						gift_staffPieChart = gift_loadStaffPieChart(giftStaffChartPanel.otype);
						gift_staffColumnChart = gift_loadStaffColumnChart(giftStaffChartPanel.otype);
						gift_staffPieChart.setSize(giftStatChartTabPanel.getWidth()*0.4, gift_panelDrag ? giftStatChartTabPanel.getHeight() - gift_cutAfterDrag : giftStatChartTabPanel.getHeight()-gift_cutBeforeDrag);
						gift_staffColumnChart.setSize(giftStatChartTabPanel.getWidth()*0.6, gift_panelDrag ? giftStatChartTabPanel.getHeight() - gift_cutAfterDrag : giftStatChartTabPanel.getHeight()-gift_cutBeforeDrag);					
						
					}
					
					if(typeof gift_deptPieChart != 'undefined' && typeof giftDeptChartPanel.hasRender != 'undefined'){
						gift_getDeptChartData();
						gift_deptPieChart = gift_loadDeptPieChart(giftDeptChartPanel.otype);
						gift_deptColumnChart = gift_loadDeptColumnChart(giftDeptChartPanel.otype);
						gift_deptPieChart.setSize(giftStatChartTabPanel.getWidth()*0.4, gift_panelDrag ? giftStatChartTabPanel.getHeight() - gift_cutAfterDrag : giftStatChartTabPanel.getHeight()-gift_cutBeforeDrag);
						gift_deptColumnChart.setSize(giftStatChartTabPanel.getWidth()*0.6, gift_panelDrag ? giftStatChartTabPanel.getHeight() - gift_cutAfterDrag : giftStatChartTabPanel.getHeight()-gift_cutBeforeDrag);					
						
					}
					
					
				}
			},'-', {
				text : '导出',
				iconCls : 'icon_tb_exoprt_excel',
				handler : function(){
					
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
			    ['单据编号', 'orderId', 100, null, function(v){
			    	return '<a class="orderLinkId">' + v + '</a>';
			    }],
			    ['赠送日期', 'orderDateFormat',100],
			    ['菜品名称', 'name', 100], 
			    ['数量','count', 60, 'right', Ext.ux.txtFormat.gridDou],
			    ['单价','unitPrice', 60, 'right', Ext.ux.txtFormat.gridDou],
			    ['总价','actualPrice', 60, 'right', Ext.ux.txtFormat.gridDou],
			    ['赠送人','waiter', null,'center']
			],
			['orderId', 'orderDateFormat', 'name', 'count', 'unitPrice', 'actualPrice', 'waiter'],
		    [ ['dataSource', 'normal']],
		    GRID_PADDING_LIMIT_20,
		    '',
		    [grid_giftStatisticsTbar, Ext.ux.initTimeBar({beginDate:beginDate, endDate:endDate,dateCombo:gift_dateCombo,statistic : 'giftStatistic_',tbarType: 0, callback : function businessHourSelect(){giftStatistic_hours = null;}})]
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
		grid_giftStatistics.getStore().on('load', function(store, records, options){
			
			function showGiftOrder(orderID){
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
			
			$('#giftStatisticsPanel').find('.orderLinkId').each(function(index, element){
        		element.onclick = function(){
        			showGiftOrder($(element).text());
        		}
        	});
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
				eval($($('div:visible[data-type=giftChart]')[0]).attr('data-value')).setSize(w*0.4, h);
				eval($($('div:visible[data-type=giftChart]')[1]).attr('data-value')).setSize(w*0.6, h);				
			}
		}	
	}

	function showGiftDetailChart(jdata){
		var dateBegin = Ext.util.Format.date(Ext.getCmp('beginDate_combo_giftStatistics').getValue(), 'Y-m-d');
		var dateEnd = Ext.util.Format.date(Ext.getCmp('endDate_combo_giftStatistics').getValue(), 'Y-m-d');
		
		var hourBegin = Ext.getCmp('giftStatistic_txtBusinessHourBegin').getEl().dom.textContent;
		var hourEnd = Ext.getCmp('giftStatistic_txtBusinessHourEnd').getEl().dom.textContent;
		
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
	            text: '<b>赠送走势图（'+dateBegin+ '至' +dateEnd+'）'+hourBegin+ ' - ' + hourEnd + titleGiftStaffName+'</b>'
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

	var gift_getStaffChartData = function(){
		requestParams.dataSource = 'getStaffChart';
		$.ajax({
			url : '../../QueryGiftStatistic.do',
			type : 'post',
			async : false,
			data : requestParams,
			success : function(jr, status, xhr){
				gift_chartLoadMarsk.hide();
				resetChartDate(gift_staffChartData);
				
				
				for (var i = 0; i < jr.root.length; i++) {
					if(jr.root[i].giftPrice != 0){
						gift_staffChartData.chartPriceData.data.push([jr.root[i].giftStaff, jr.root[i].giftPrice]);
					}
					if(jr.root[i].giftAmount != 0){
						gift_staffChartData.chartAmountData.data.push([jr.root[i].giftStaff, jr.root[i].giftAmount]);
					}
					
					gift_staffChartData.priceColumnChart.xAxis.push(jr.root[i].giftStaff);
					gift_staffChartData.priceColumnChart.yAxis.data.push({y : jr.root[i].giftPrice, color : colors[i]}); 
					gift_staffChartData.amountColumnChart.yAxis.data.push({y : jr.root[i].giftAmount, color : colors[i]});
				}
			},
			failure : function(res, opt){
			
			}
		});
		
	};


	var gift_getDeptChartData = function(){
		requestParams.dataSource = 'getDeptChart';
		$.ajax({
			url : '../../QueryGiftStatistic.do',
			type : 'post',
			async : false,
			data : requestParams,
			success : function(jr, status, xhr){
				gift_chartLoadMarsk.hide();
				resetChartDate(gift_deptChartData);
				
				
				for (var i = 0; i < jr.root.length; i++) {
					if(jr.root[i].giftPrice != 0){
						gift_deptChartData.chartPriceData.data.push([jr.root[i].giftDept.name, jr.root[i].giftPrice]);
					}
					if(jr.root[i].giftAmount != 0){
						gift_deptChartData.chartAmountData.data.push([jr.root[i].giftDept.name, jr.root[i].giftAmount]);
					}
					gift_deptChartData.priceColumnChart.xAxis.push(jr.root[i].giftDept.name);
					gift_deptChartData.priceColumnChart.yAxis.data.push({y : jr.root[i].giftPrice, color : colors[i]}); 
					gift_deptChartData.amountColumnChart.yAxis.data.push({y : jr.root[i].giftAmount, color : colors[i]});
				}
			},
			failure : function(res, opt){
			
			}
		});
		
	};

	//type=0:金额, type=1:数量
	var gift_loadStaffPieChart = function(type){
		
		var content = {};
		content.title = type==1?'员工赠送数量比例图':'员工赠送金额比例图';
		content.unit = type==1?'份':'元';
		content.series = type==1?gift_staffChartData.chartAmountData:gift_staffChartData.chartPriceData;
		
		return newPieChart({rt: 'divGiftStaffAmountPieChart', title : content.title, unit: content.unit, series: content.series});			
	};

	var gift_loadStaffColumnChart = function(type){
		var content = {};
		content.title = type==1?'员工赠送数量柱状图':'员工赠送金额柱状图';
		content.unit = type==1?'份':'元';
		content.series = type==1?gift_staffChartData.amountColumnChart.yAxis : gift_staffChartData.priceColumnChart.yAxis;	
	    
	    return newColumnChart({
	    	rt: 'divGiftStaffAmountColumnChart', title : content.title, unit: content.unit, series: content.series, xAxis:gift_staffChartData.priceColumnChart.xAxis	
	    });	
		giftStaffChartPanel.otype = type;
	};

	//type=0:金额, type=1:数量
	var gift_loadDeptPieChart = function(type){
		
		var content = {};
		content.title = type==1?'部门赠送数量比例图':'部门赠送金额比例图';
		content.unit = type==1?'份':'元';
		content.series = type==1?gift_deptChartData.chartAmountData:gift_deptChartData.chartPriceData;
		
		return newPieChart({rt: 'divGiftDeptPieChart', title : content.title, unit: content.unit, series: content.series});		
	};
	
	var gift_loadDeptColumnChart = function(type){
		var content = {};
		content.title = type==1?'部门赠送数量柱状图':'部门赠送金额柱状图';
		content.unit = type==1?'份':'元';
		content.series = type==1?gift_deptChartData.amountColumnChart.yAxis : gift_deptChartData.priceColumnChart.yAxis;	
	    
	    return newColumnChart({
	    	rt: 'divGiftDeptColumnChart', title : content.title, unit: content.unit, series: content.series, xAxis:gift_deptChartData.priceColumnChart.xAxis	
	    });	
	   
	    giftDeptChartPanel.otype = type;
	};
	
	//按员工汇总赠送数量的柱状和饼图
	$('#staffByAmount_span_giftChart').click(function(){
		gift_fnChangeStaffChart(this, 1);
	});
	
	//按员工汇总赠送金额的柱状和饼图
	$('#staffByFee_span_giftChart').click(function(){
		gift_fnChangeStaffChart(this, 0);
	});
	
	function gift_fnChangeStaffChart(thiz, v){
		$(thiz).find('input').attr('checked', 'checked');
		gift_staffPieChart = gift_loadStaffPieChart(v);
		gift_staffColumnChart = gift_loadStaffColumnChart(v);
		gift_staffPieChart.setSize(giftStatChartTabPanel.getWidth()*0.4, gift_panelDrag ? giftStatChartTabPanel.getHeight() - gift_cutAfterDrag : giftStatChartTabPanel.getHeight()-gift_cutBeforeDrag);
		gift_staffColumnChart.setSize(giftStatChartTabPanel.getWidth()*0.6, gift_panelDrag ? giftStatChartTabPanel.getHeight() - gift_cutAfterDrag : giftStatChartTabPanel.getHeight()-gift_cutBeforeDrag);
	}
	
	//按部门汇总赠送金额的柱状和饼图
	$('#deptByFee_span_giftChart').click(function(){
		gift_fnChangeDeptChart(this, 0);
	});
	
	//按部门汇总赠送数量的柱状和饼图
	$('#deptByAmount_span_giftChart').click(function(){
		gift_fnChangeDeptChart(this, 1);
	});
	
	function gift_fnChangeDeptChart(thiz, v){
		$(thiz).find('input').attr('checked', 'checked');
		gift_deptPieChart = gift_loadDeptPieChart(v);
		gift_deptColumnChart = gift_loadDeptColumnChart(v);
		gift_deptPieChart.setSize(giftStatChartTabPanel.getWidth()*0.4, gift_panelDrag ? giftStatChartTabPanel.getHeight() - gift_cutAfterDrag : giftStatChartTabPanel.getHeight()-gift_cutBeforeDrag);
		gift_deptColumnChart.setSize(giftStatChartTabPanel.getWidth()*0.6, gift_panelDrag ? giftStatChartTabPanel.getHeight() - gift_cutAfterDrag : giftStatChartTabPanel.getHeight()-gift_cutBeforeDrag);
	}

	var grid_giftStatistics, giftViewBillWin;
	var gift_cutAfterDrag=75, gift_cutBeforeDrag=70, giftStatistic_hours;
	var giftDetailsStatPanel, giftStatChartTabPanel, giftDetailChartPanel, giftStaffChartPanel, giftDeptChartPanel;
	var gift_detailChart, gift_staffPieChart, gift_staffColumnChart, gift_deptPieChart, gift_deptColumnChart;
	var requestParams, gift_dateCombo;
	var gift_panelDrag = false, colors = Highcharts.getOptions().colors, gift_chartLoadMarsk;
	var giftPanelHeight, gift_tabPanelHeight;
	var titleGiftStaffName;
	
	var gift_staffChartData = Wireless.chart.initChartData({priceName:'员工赠送金额', countName:'员工赠送数量'});
	var gift_deptChartData = Wireless.chart.initChartData({priceName:'部门赠送金额', countName:'部门赠送数量'});
				            

	
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
			render : function(thiz){
				thiz.hasRender = true;
			}
		}
		
		
	});		
	
	giftDeptChartPanel = new Ext.Panel({
		title : '按部门汇总',
		contentEl : 'divGiftDeptCharts',
		listeners : {
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
	
	giftStaffChartPanel.addListener('show', function(){
		var charts = Wireless.chart.initChartPanel({
			divLeftShowChart : 'divGiftStaffAmountPieChart',
			divRightShowChart : 'divGiftStaffAmountColumnChart',
			generalName : 'giftStaff_',
			getChartData : gift_getStaffChartData,
			leftChartLoad : gift_loadStaffPieChart,
			rightChartLoad : gift_loadStaffColumnChart,
			panelDrag : true,
			leftChart : gift_staffPieChart,
			rightChart : gift_staffColumnChart,
			loadType : 1,
			tabPanel : giftStatChartTabPanel,
			cutAfterDrag : gift_cutAfterDrag,
			cutBeforeDrag : gift_cutBeforeDrag		
			
		});	
		gift_staffPieChart = charts.pie;
		gift_staffColumnChart = charts.column;
		
		charts = null;
	});	
	
	giftDeptChartPanel.addListener('show', function(){
		var charts = Wireless.chart.initChartPanel({
			divLeftShowChart : 'divGiftDeptPieChart',
			divRightShowChart : 'divGiftDeptColumnChart',
			generalName : 'giftDept_',
			getChartData : gift_getDeptChartData,
			leftChartLoad : gift_loadDeptPieChart,
			rightChartLoad : gift_loadDeptColumnChart,
			panelDrag : true,
			leftChart : gift_deptPieChart,
			rightChart : gift_deptColumnChart,
			loadType : 1,
			tabPanel : giftStatChartTabPanel,
			cutAfterDrag : gift_cutAfterDrag,
			cutBeforeDrag : gift_cutBeforeDrag		
			
		});	
		gift_deptPieChart = charts.pie;
		gift_deptColumnChart = charts.column;
		
		charts = null;
		
	});		
	
	
	new Ext.Panel({
		renderTo : 'divGiftStatistics',
		id : 'giftStatisticsPanel',
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
    
    var gift_totalHeight = Ext.getCmp('giftStatisticsPanel').getHeight();
    
    giftDetailsStatPanel.setHeight(gift_totalHeight*0.4);
    giftDetailsStatPanel.getEl().parent().setHeight(gift_totalHeight*0.4);
    
    giftStatChartTabPanel.setHeight(gift_totalHeight*0.6);
    
    gift_rz.resizeTo(giftDetailsStatPanel.getWidth(), gift_totalHeight*0.4);

    gift_dateCombo.setValue(1);
    gift_dateCombo.fireEvent('select', gift_dateCombo, null, 1);
	
});
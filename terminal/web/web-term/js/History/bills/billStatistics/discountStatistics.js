
Ext.onReady(function(){

	var discount_beginDate = new Ext.form.DateField({
		id : 'beginDate_combo_discountStatistics',
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(true, thiz.getId(), discount_endDate.getId());
			}
		}
	});
	var discount_endDate = new Ext.form.DateField({
		id : 'endDate_combo_discountStatistics',
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(false, discount_beginDate.getId(), thiz.getId());
			}
		}
	});
	
	function initDiscountGrid(){
	
		discount_dateCombo = Ext.ux.createDateCombo({
			beginDate : discount_beginDate,
			endDate : discount_endDate,
			callback : function(){
				Ext.getCmp('search_btn_discountStatistics').handler();
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
					Ext.getCmp('search_btn_discountStatistics').handler();
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
					Ext.getCmp('search_btn_discountStatistics').handler();
				}
			}
		});	
		
		//加载门店
		var branch_combo_discount = new Ext.form.ComboBox({
			id : 'branch_combo_discount',
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
					if(branch_combo_discount.getValue() ==  -1){
						Ext.getCmp('discount_comboBusinessHour').setDisabled(true);
						Ext.getCmp('discount_comboBusinessHour').setValue(-1);
						
						discount_combo_staffs.setDisabled(true);
						discount_combo_staffs.setValue(-1);
						
						discount_deptCombo.setDisabled(true);
						discount_deptCombo.setValue(-1);
						
						
					
					}else{
						//加载员工
						var staff = [[-1, '全部']];
						Ext.Ajax.request({
							url : '../../QueryStaff.do',
							params : {
								branchId : branch_combo_discount.getValue()
							},
							success : function(res, opt){
								var jr = Ext.decode(res.responseText);
								
								for(var i = 0; i < jr.root.length; i++){
									staff.push([jr.root[i]['staffID'], jr.root[i]['staffName']]);
								}
								
								discount_combo_staffs.setDisabled(false);
								discount_combo_staffs.store.loadData(staff);
								discount_combo_staffs.setValue(-1);
							}
						});
						
						//加载部门
						var region = [[-1, '全部']];
						Ext.Ajax.request({
							url : '../../OperateRegion.do',
							params : {
								dataSource : 'getByCond',
								branchId : branch_combo_discount.getValue()
							},
							success : function(res, opt){
								var jr = Ext.decode(res.responseText);
								for(var i = 0; i < jr.root.length; i++){
									region.push([jr.root[i]['id'], jr.root[i]['name']]);
								}
								
								discount_deptCombo.setDisabled(false);
								discount_deptCombo.store.loadData(region);
								discount_deptCombo.setValue(-1);
							}
						});
						
						//加载市别
						var hour = [[-1, '全部']];
						Ext.Ajax.request({
							url : '../../OperateBusinessHour.do',
							params : {
								dataSource : 'getByCond',
								branchId : branch_combo_discount.getValue()
							},
							success : function(res, opt){
								var jr = Ext.decode(res.responseText);
								
								for(var i = 0; i < jr.root.length; i++){
									hour.push([jr.root[i]['id'], jr.root[i]['name'], jr.root[i]['opening'], jr.root[i]['ending']]);
								}
								
								hour.push([-2, '自定义']);
								
								Ext.getCmp('discount_comboBusinessHour').setDisabled(false);
								Ext.getCmp('discount_comboBusinessHour').store.loadData(hour);
								Ext.getCmp('discount_comboBusinessHour').setValue(-1);
							}
						});
						
					}
					
					//是否跳转
					if(!isJump){
						Ext.getCmp('search_btn_discountStatistics').handler();
					}
				}
			}
		});
		
		var discountStatisticsGridTbarItem = [{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;员工选择: '
			},discount_combo_staffs,{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;区域选择: '
			}, discount_deptCombo , {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;门店选择:'
			}, branch_combo_discount, '->',{
				text : '搜索',
				id : 'search_btn_discountStatistics',
				iconCls : 'btn_search',
				handler : function(e){
					if(!discount_beginDate.isValid() || !discount_endDate.isValid()){
						return;
					}
					
					var businessHour = Ext.ux.statistic_oBusinessHourData({type : 'get', statistic : 'discount_'}).data;
					
					var store = discountStatisticsGrid.getStore();
					store.baseParams['dataSource'] = 'normal',
					store.baseParams['beginDate'] = Ext.util.Format.date(discount_beginDate.getValue(), 'Y-m-d 00:00:00');
					store.baseParams['endDate'] = Ext.util.Format.date(discount_endDate.getValue(), 'Y-m-d 23:59:59');
					store.baseParams['staffID'] = discount_combo_staffs.getValue();
					store.baseParams['deptID'] = discount_deptCombo.getValue();
					store.baseParams['opening'] = businessHour.opening != '00:00' ? businessHour.opening : null;
					store.baseParams['ending'] = businessHour.ending != '00:00' ? businessHour.ending : null;	
					store.baseParams['branchId'] = branch_combo_discount.getValue();
					
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
						dateBeg : Ext.util.Format.date(discount_beginDate.getValue(), 'Y-m-d 00:00:00'),
						dateEnd : Ext.util.Format.date(discount_endDate.getValue(), 'Y-m-d 23:59:59'),
						deptID : discount_deptCombo.getValue(),
						staffId : discount_combo_staffs.getValue(),
						opening : businessHour.opening != '00:00' ? businessHour.opening : null,
						ending : businessHour.ending != '00:00' ? businessHour.ending : null,
						branchId : branch_combo_discount.getValue()
					};
					discount_chartLoadMarsk.show();
					Ext.Ajax.request({
						url : '../../QueryDiscountStatistics.do',
						params : requestParams,
						success : function(res, opt){
							discount_chartLoadMarsk.hide();
							
							var jr = Ext.decode(res.responseText);
							showDiscountDetailChart(jr);
						},
						failure : function(res, opt){
						
						}
					});	
					
					if(typeof discount_staffPieChart != 'undefined' && typeof discountStaffChartPanel.hasRender != 'undefined'){
						discount_getStaffChartData();
						discount_staffPieChart = discount_loadStaffPieChart(discountDetailsStatPanel.otype);
						discount_staffColumnChart = discount_loadStaffColumnChart(discountDetailsStatPanel.otype);
						discount_staffPieChart.setSize(discountStatChartTabPanel.getWidth()*0.4, discount_panelDrag ? discountStatChartTabPanel.getHeight() - discount_cutAfterDrag : discountStatChartTabPanel.getHeight()-discount_cutChartHeight);
						discount_staffColumnChart.setSize(discountStatChartTabPanel.getWidth()*0.6, discount_panelDrag ? discountStatChartTabPanel.getHeight() - discount_cutAfterDrag : discountStatChartTabPanel.getHeight()-discount_cutChartHeight);
						
					}
					if(typeof discount_deptPieChart != 'undefined' && typeof discountDeptChartPanel.hasRender != 'undefined'){
						discount_getDeptChartData();
						discount_deptPieChart = discount_loadDeptPieChart(discountDetailsStatPanel.otype);
						discount_deptColumnChart = discount_loadDeptColumnChart(discountDetailsStatPanel.otype);
						discount_deptPieChart.setSize(discountStatChartTabPanel.getWidth()*0.4, discount_panelDrag ? discountStatChartTabPanel.getHeight() - discount_cutAfterDrag : discountStatChartTabPanel.getHeight()-discount_cutChartHeight);
						discount_deptColumnChart.setSize(discountStatChartTabPanel.getWidth()*0.6, discount_panelDrag ? discountStatChartTabPanel.getHeight() - discount_cutAfterDrag : discountStatChartTabPanel.getHeight()-discount_cutChartHeight);
					}					
					
				}
			},'-', {
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				if(!discount_beginDate.isValid() || !discount_endDate.isValid()){
					return;
				}
				var url = '../../{0}?beginDate={1}&endDate={2}&staffID={3}&deptID={4}&dataSource={5}&branchId={6}';
				url = String.format(
						url, 
						'ExportHistoryStatisticsToExecl.do', 
						Ext.util.Format.date(discount_beginDate.getValue(), 'Y-m-d 00:00:00'),
						Ext.util.Format.date(discount_endDate.getValue(), 'Y-m-d 23:59:59'),
						discount_combo_staffs.getValue(),
						discount_deptCombo.getValue(),
						'discountStatisticsList',
						branch_combo_discount.getValue()
				);
				window.location = url;
			}
		}];
		
		var discountStatisticsGridTbar = Ext.ux.initTimeBar({beginDate:discount_beginDate, endDate:discount_endDate,dateCombo:discount_dateCombo, tbarType : 1, statistic : 'discount_', callback : function businessHourSelect(){}}).concat(discountStatisticsGridTbarItem);
		
		discountStatisticsGrid = createGridPanel(
			'',
			'',
			'',
			'',
			'../../QueryDiscountStatistics.do',
			[[true, false, false, true], 
			 ['门店名称', 'restaurantName', 60],
			 ['日期','orderDateFormat'], 
			 ['账单号', 'id', null, null, function(v){
			 	return '<a class="orderLinkId">' + v + '</a>';
			 }],
	         ['折扣额', 'discountPrice', null, 'right', Ext.ux.txtFormat.gridDou], 
	         ['实收金额', 'actualPrice', null, 'right', Ext.ux.txtFormat.gridDou],
	         ['操作人', 'discounter'], 
	         ['备注', 'comment', 200]
			],
			['restaurantName', 'orderDateFormat', 'id', 'discountPrice', 'actualPrice', 'waiter', 'comment', 'rid'],
			[ ['dataSource', 'getDetail']],
			GRID_PADDING_LIMIT_20,
			null,
			[discountStatisticsGridTbar]
		);
		discountStatisticsGrid.region = 'center';
		discountStatisticsGrid.getStore().on('load', function(store, records, options){
			
			function showDicountDetail(orderID){
				var discountViewBillWin;
				discountViewBillWin = new Ext.Window({
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
							discountViewBillWin.destroy();
						}
					}],
					keys : [{
						key : Ext.EventObject.ESC,
						scope : this,
						fn : function(){
							discountViewBillWin.destroy();
						}
					}],
					listeners : {
						show : function(thiz) {
							var sd = Ext.ux.getSelData(discountStatisticsGrid);
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
				discountViewBillWin.show();
				discountViewBillWin.center();
			}
			
			if(store.getCount() > 0){
				var sumRow = discountStatisticsGrid.getView().getRow(store.getCount()-1);	
				sumRow.style.backgroundColor = '#EEEEEE';			
				
				for(var i = 0; i < discountStatisticsGrid.getColumnModel().getColumnCount(); i++){
					var sumRow = discountStatisticsGrid.getView().getCell(store.getCount()-1, i);
					sumRow.style.fontSize = '15px';
					sumRow.style.fontWeight = 'bold';
					sumRow.style.color = 'green';
				}
				discountStatisticsGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';		//门店名称
				discountStatisticsGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';		//日期
				discountStatisticsGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';		//账单号
				//discountStatisticsGrid.getView().getCell(store.getCount()-1, 4).innerHTML = '--';		//折扣额
				//discountStatisticsGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '--';		//实收金额
				discountStatisticsGrid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';		//操作人
				discountStatisticsGrid.getView().getCell(store.getCount()-1, 7).innerHTML = '--';		//备注
				
				$('#discountStatisticsPanel').find('.orderLinkId').each(function(index, element){
        			element.onclick = function(){
        				showDicountDetail($(element).text());
        			}
        		});
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
						
						discount_changeChartWidth(w,chartHeight-discount_cutAfterDrag);
						
						if(discount_panelDrag){
							discountStatChartTabPanel.setHeight(chartHeight);
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
				eval($('div:visible[data-type=discountChart]').attr('data-value')).setSize(w, h+20);
			}else if($('div:visible[data-type=discountChart]').length > 1){
				eval($($('div:visible[data-type=discountChart]')[0]).attr('data-value')).setSize(w*0.4, h);
				eval($($('div:visible[data-type=discountChart]')[1]).attr('data-value')).setSize(w*0.6, h);				
			}
		}	
	}

	function showDiscountDetailChart(jdata){
		var dateBegin = Ext.util.Format.date(Ext.getCmp('beginDate_combo_discountStatistics').getValue(), 'Y-m-d');
		var dateEnd = Ext.util.Format.date(Ext.getCmp('endDate_combo_discountStatistics').getValue(), 'Y-m-d');
		
		var hourBegin = Ext.getCmp('discount_txtBusinessHourBegin').getEl().dom.textContent;
		var hourEnd = Ext.getCmp('discount_txtBusinessHourEnd').getEl().dom.textContent;
		
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
	            text: '<b>'+titleDiscountDeptName+'折扣额走势图（'+dateBegin+ '至' +dateEnd+'）'+hourBegin+ ' - ' + hourEnd + titleDiscountStaffName+'</b>'
	        },
	        labels: {
	        	items : [{
	        		html : '<b>总折扣金额:' + chartData.totalMoney + ' 元</b><br><b>日均折扣金额:' + chartData.avgMoney + ' 元</b><br><b>日均折扣数量:' + chartData.avgCount + ' 份</b>',
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

	var discount_getStaffChartData = function(){
		requestParams.dataSource = 'getStaffChart';
		$.ajax({
			url : '../../QueryDiscountStatistics.do',
			type : 'post',
			async : false,
			data : requestParams,
			success : function(jr, status, xhr){
				discount_chartLoadMarsk.hide();
				resetChartDate(discount_staffChartData);
				
				for (var i = 0; i < jr.root.length; i++) {
					discount_staffChartData.chartPriceData.data.push([jr.root[i].staffName, jr.root[i].discountPrice]);
					discount_staffChartData.chartAmountData.data.push([jr.root[i].staffName, jr.root[i].discountAmount]);
					discount_staffChartData.priceColumnChart.xAxis.push(jr.root[i].staffName);
					discount_staffChartData.priceColumnChart.yAxis.data.push({y : jr.root[i].discountPrice, color : colors[i]}); 
					discount_staffChartData.amountColumnChart.yAxis.data.push({y : jr.root[i].discountAmount, color : colors[i]});
				}
			},
			failure : function(res, opt){
			
			}
		});
	};

	var discount_loadStaffPieChart = function(type){
		var content = {};
		content.title = type==1?'员工折扣数量比例图':'员工折扣金额比例图';
		content.unit = type==1?'份':'元';
		content.series = type==1?discount_staffChartData.chartAmountData:discount_staffChartData.chartPriceData;
		
		return newPieChart({rt: 'divDiscountStaffPieChart', title : content.title, unit: content.unit, series: content.series});	
	};
	
	var discount_loadStaffColumnChart = function(type){
		var content = {};
		content.title = type==1?'员工折扣账数量柱状图':'员工折扣金额柱状图';
		content.unit = type==1?'份':'元';
		content.series = type==1?discount_staffChartData.amountColumnChart.yAxis : discount_staffChartData.priceColumnChart.yAxis;	
	    
	    return newColumnChart({
	    	rt: 'divDiscountStaffColumnChart', title : content.title, unit: content.unit, series: content.series, xAxis:discount_staffChartData.priceColumnChart.xAxis	
	    });	
	};
	
	var discount_getDeptChartData = function(){
		requestParams.dataSource = 'getDeptChart';
		$.ajax({
			url : '../../QueryDiscountStatistics.do',
			type : 'post',
			async : false,
			data : requestParams,
			success : function(jr, status, xhr){
				resetChartDate(discount_deptChartData);
				for (var i = 0; i < jr.root.length; i++) {
					if(jr.root[i].discountPrice != 0)
						discount_deptChartData.chartPriceData.data.push([jr.root[i].discountDept.name, jr.root[i].discountPrice]);
					if(jr.root[i].discountAmount != 0)
						discount_deptChartData.chartAmountData.data.push([jr.root[i].discountDept.name, jr.root[i].discountAmount]);
					discount_deptChartData.priceColumnChart.xAxis.push(jr.root[i].discountDept.name);
					discount_deptChartData.priceColumnChart.yAxis.data.push({y : jr.root[i].discountPrice, color : colors[i]}); 
					discount_deptChartData.amountColumnChart.yAxis.data.push({y : jr.root[i].discountAmount, color : colors[i]});
				}
			},
			failure : function(res, opt){
			
			}
		});
		
	};
	
	var discount_loadDeptColumnChart = function(type){
		var content = {};
		content.title = type==1?'部门折扣数量柱状图':'部门折扣金额柱状图';
		content.unit = type==1?'份':'元';
		content.series = type==1?discount_deptChartData.amountColumnChart.yAxis : discount_deptChartData.priceColumnChart.yAxis;	
	    
	    return newColumnChart({
	    	rt: 'divDiscountDeptColumnChart', title : content.title, unit: content.unit, series: content.series, xAxis:discount_deptChartData.priceColumnChart.xAxis	
	    });	
	};
	
	var discount_loadDeptPieChart = function(type){
		var content = {};
		content.title = type==1?'部门折扣数量比例图':'部门折扣金额比例图';
		content.unit = type==1?'份':'元';
		content.series = type==1?discount_deptChartData.chartAmountData:discount_deptChartData.chartPriceData;
		
		return newPieChart({rt: 'divDiscountDeptPieChart', title : content.title, unit: content.unit, series: content.series});
	};
	
	//按员工汇总折扣数量的柱状和饼图
	$('#staffByAmount_span_discountChart').click(function(){
		discount_fnChangeStaffChart(this, 1);
	});
	
	//按员工汇总折扣金额的柱状和饼图
	$('#staffByFee_span_discountChart').click(function(){
		discount_fnChangeStaffChart(this, 0);
	});
	
	function discount_fnChangeStaffChart(thiz, v){
		$(thiz).find('input').attr('checked', 'checked');
		discount_staffPieChart = discount_loadStaffPieChart(v);
		discount_staffColumnChart = discount_loadStaffColumnChart(v);
		discount_staffPieChart.setSize(discountStatChartTabPanel.getWidth()*0.4, discount_panelDrag ? discountStatChartTabPanel.getHeight() - discount_cutAfterDrag : discountStatChartTabPanel.getHeight()-discount_cutChartHeight);
		discount_staffColumnChart.setSize(discountStatChartTabPanel.getWidth()*0.6, discount_panelDrag ? discountStatChartTabPanel.getHeight() - discount_cutAfterDrag : discountStatChartTabPanel.getHeight()-discount_cutChartHeight);
		
		discountDetailsStatPanel.otype = v;
	}
	
	//按部门汇总折扣数量的柱状和饼图
	$('#deptByFee_span_discountChart').click(function(){
		discount_fnChangeDeptChart(this, 0);
	});
	
	//按部门汇总折扣金额的柱状和饼图
	$('#deptByAmount_span_discountChart').click(function(){
		discount_fnChangeDeptChart(this, 1);
	});
	
	function discount_fnChangeDeptChart(thiz, v){
		$(thiz).find('input').attr('checked', 'checked');
		discount_deptPieChart = discount_loadDeptPieChart(v);
		discount_deptColumnChart = discount_loadDeptColumnChart(v);
		discount_deptPieChart.setSize(discountStatChartTabPanel.getWidth()*0.4, discount_panelDrag ? discountStatChartTabPanel.getHeight() - discount_cutAfterDrag : discountStatChartTabPanel.getHeight()-discount_cutChartHeight);
		discount_deptColumnChart.setSize(discountStatChartTabPanel.getWidth()*0.6, discount_panelDrag ? discountStatChartTabPanel.getHeight() - discount_cutAfterDrag : discountStatChartTabPanel.getHeight()-discount_cutChartHeight);
		
		discountDetailsStatPanel.otype = v;
	}
	
	var discount_cutAfterDrag = 190, discount_cutBeforeDrag = 40;
	var titleDiscountDeptName, titleDiscountStaffName;
	var discount_detailChart, discount_staffPieChart, discount_staffColumnChart, discount_deptPieChart, discount_deptColumnChart;
	
	var discountDetailChartPanel, discountStatChartTabPanel;
	var discountPanelHeight, discount_tabPanelHeight;
	var discount_panelDrag = false, colors = Highcharts.getOptions().colors;
	var discount_staffChartData = Wireless.chart.initChartData({priceName:'员工折扣金额', countName:'员工折扣数量'});
	var discount_deptChartData = Wireless.chart.initChartData({priceName:'部门折扣金额', countName:'部门折扣数量'});
					            
	var discount_chartLoadMarsk, discount_dateCombo ;

	initDiscountGrid();
	
	discountDetailChartPanel = new Ext.Panel({
		title : '折扣走势',
		contentEl : 'divDiscountDetailChart',
		listeners : {
			show : function(thiz){
				//thiz.getEl(): 刚打开页面时thiz.getWidth无效
				if(discount_detailChart && typeof thiz.getEl() != 'undefined'){
					discount_detailChart.setSize(thiz.getWidth(), discount_panelDrag ? discountStatChartTabPanel.getHeight() - 170 : discountStatChartTabPanel.getHeight()-30);
				}
			}
		}		
	});
	
	discountStaffChartPanel = new Ext.Panel({
		title : '按员工汇总',
		contentEl : 'divDiscountStaffCharts',
		listeners : {
			render : function(thiz){
				thiz.hasRender = true;
			}
		}
	});	
	
	discountDeptChartPanel = new Ext.Panel({
		title : '按部门汇总',
		contentEl : 'divDiscountDeptCharts',
		listeners : {
			render : function(thiz){
				thiz.hasRender = true;
			}
		}
	});		
	


	discountStatChartTabPanel = new Ext.TabPanel({
		region : 'south',
		height : 430,
		items : [discountDetailChartPanel, discountStaffChartPanel, discountDeptChartPanel],
		listeners : {
			render : function(thiz){
				thiz.setActiveTab(discountDetailChartPanel);
			}
		}
	});

	discountStaffChartPanel.addListener('show', function(){
		var charts = Wireless.chart.initChartPanel({
			divLeftShowChart : 'divDiscountStaffPieChart',
			divRightShowChart : 'divDiscountStaffColumnChart',
			generalName : 'discountStaff_',
			getChartData : discount_getStaffChartData,
			leftChartLoad : discount_loadStaffPieChart,
			rightChartLoad : discount_loadStaffColumnChart,
			panelDrag : true,
			leftChart : discount_staffPieChart,
			rightChart : discount_staffColumnChart,
			loadType : 1,
			tabPanel : discountStatChartTabPanel,
			cutAfterDrag : discount_cutAfterDrag,
			cutBeforeDrag : discount_cutBeforeDrag		
			
		});	
		discount_staffPieChart = charts.pie;
		discount_staffColumnChart = charts.column;
		
		charts = null;
	});	
	
	discountDeptChartPanel.addListener('show', function(){
		var charts = Wireless.chart.initChartPanel({
			divLeftShowChart : 'divDiscountDeptPieChart',
			divRightShowChart : 'divDiscountDeptColumnChart',
			generalName : 'discountDept_',
			getChartData : discount_getDeptChartData,
			leftChartLoad : discount_loadDeptPieChart,
			rightChartLoad : discount_loadDeptColumnChart,
			panelDrag : true,
			leftChart : discount_deptPieChart,
			rightChart : discount_deptColumnChart,
			loadType : 1,
			tabPanel : discountStatChartTabPanel,
			cutAfterDrag : discount_cutAfterDrag,
			cutBeforeDrag : discount_cutBeforeDrag		
			
		});	
		discount_deptPieChart = charts.pie;
		discount_deptColumnChart = charts.column;
		
		charts = null;
		
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
	
	discount_chartLoadMarsk = new Ext.LoadMask(discountStatChartTabPanel.getEl().dom, {
	    msg  : '数据统计中，请稍候......',
	    disabled : false
	});	
	
    var discount_totalHeight = Ext.getCmp('discountStatisticsPanel').getHeight();
    
    discountDetailsStatPanel.setHeight(discount_totalHeight*0.4);
    discountDetailsStatPanel.getEl().parent().setHeight(discount_totalHeight*0.4);
    
    discountStatChartTabPanel.setHeight(discount_totalHeight*0.6);	
    
    discount_rz.resizeTo(discountDetailsStatPanel.getWidth(), discount_totalHeight*0.4);	
	

    discount_dateCombo.setValue(1);
	discount_dateCombo.fireEvent('select', discount_dateCombo, null, 1);			
//	Ext.getCmp('discountStatistics').updateStatisticsDate = discount_setStatisticsDate;
});

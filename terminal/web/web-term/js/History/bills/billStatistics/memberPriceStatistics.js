Ext.onReady(function(){
	var memberPricePanelHeight = null;
	
	var memberPriceTabPanelHeight =null;
	
	var memberPricePanelDrag = false;
	
	var memberPriceDetailChart = null;
	
	var beginDate = new Ext.form.DateField({
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	var endDate = new Ext.form.DateField({
		xtype : 'datafield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	var dataCombo = Ext.ux.createDateCombo({
		beginDate : beginDate,
		endDate : endDate,
		callback : function(){
			Ext.getCmp('memberPrice_btnSearch').handler();
		}
	});
	
	//设置显示时间
	var hours = null;
	function setStatisticsDate(){
		if(sendToPageOperation){
			beginDate.setValue(sendToStatisticsPageBeginDate);
			endDate.setValue(sendToStatisticsPageEndDate);
			
			hours = sendToStatisticsPageHours;
			
			Ext.getCmp('memberPrice_btnSearch').handler();
			
			Ext.getCmp('memberPrice_txtBusinessHourBegin').setText('<font style="color:green; font-size:20px">' + hours.openingText + '</font>');
			Ext.getCmp('memberPrice_txtBusinessHourEnd').setText('<font style="color:green; font-size:20px">' + hours.endingText + '</font>');
			Ext.getCmp('memberPrice_comboBusinessHour').setValue(hours.hourComboValue);	
			
			sendToPageOperation = false;
		}
	};
	
	
	//员工的select
	var memeberPriceStaff= new Ext.form.ComboBox({
		id : 'memberPriceStaff',
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
				var data = [[-1, '全部']];
				Ext.Ajax.request({
					url : '../../QueryStaff.do',
					params : {privileges : '1004'},
					success : function(res, opt){
						var jr =Ext.decode(res.responseText);
						for(var i = 0; i < jr.root.length; i++){
							data.push([jr.root[i]['staffID'], jr.root[i]['staffName']]);
						}
						thiz.store.loadData(data);
						thiz.setValue(-1);
						
						if(sendToPageOperation){
							setStatisticsDate();
						}else{
							dataCombo.setValue(1);
							dataCombo.fireEvent('select', dataCombo, null, 1);
						}
					},
					fialure : function(res, opt){
						thiz.store.loadData(data);
						thiz.setValue(-1);
					}
					
				});
			},
			select : function(){
				Ext.getCmp('memberPrice_btnSearch').handler();
			}
		}
	});
	
	//toolbar
	var StatisticsTbarItem = [{
		xtype : 'tbtext',
		text : '操作人员:'
	}, memeberPriceStaff, '->', {
		text : '搜索',
		id: 'memberPrice_btnSearch',
		iconCls : 'btn_search',
		handler : function(e){
			if(!beginDate.isValid() || !endDate.isValid){
				return;
			}
			
			if(hours){
				businessHour = hours;
			}else{
				businessHour = Ext.ux.statistic_oBusinessHourData({type : 'get', statistic : 'memberPrice_'}).data;
			}
			
			var store = memberPriceGrid.getStore();
			store.baseParams['dataSource'] = 'normal',
			store.baseParams['beginDate'] = Ext.util.Format.date(beginDate.getValue(), 'Y-m-d 00:00:00');
			store.baseParams['endDate'] = Ext.util.Format.date(endDate.getValue(), 'Y-m-d 23:59:59');
			store.baseParams['staffId'] = memeberPriceStaff.getValue() < 0 ? '' : memeberPriceStaff.getValue();
			store.baseParams['opening'] = businessHour.opening;
			store.baseParams['ending'] = businessHour.ending;		
			store.load({
				params : {
					start : 0,
					limit : 20
				}
			});
			
			if(memeberPriceStaff.getValue() && memeberPriceStaff.getValue() != -1){
				StaffName = '操作人 : ' + memeberPriceStaff.getEl().dom.value;
			}else{
				StaffName = '';
			}
			
			requestParams = {
					dataSource : 'chart4IncomeByEachDay',
					dateBeg : Ext.util.Format.date(beginDate.getValue(), 'Y-m-d 00:00:00'),
					dateEnd : Ext.util.Format.date(endDate.getValue(), 'Y-m-d 23:59:59'),
					staffID : memeberPriceStaff.getValue(),
					opening : businessHour.opening,
					ending : businessHour.ending
				};
				
				
				Ext.Ajax.request({
					url : '../../QueryMemberPriceStatistics.do',
					params : requestParams,
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						showMemberPriceDetailChart(jr);
					}
				});		
				
				if(typeof repaid_staffPieChart != 'undefined'){
					repaid_getStaffChartData();
					repaid_staffPieChart = repaid_loadStaffPieChart(repaidStaffChartPanel.otype);
					repaid_staffColumnChart = repaid_loadStaffColumnChart(repaidStaffChartPanel.otype);
					repaid_staffPieChart.setSize(repaidStatChartTabPanel.getWidth()*0.4, memberPricePanelDrag ? repaidStatChartTabPanel.getHeight() - repaid_cutAfterDrag : repaidStatChartTabPanel.getHeight()-30);
					repaid_staffColumnChart.setSize(repaidStatChartTabPanel.getWidth()*0.6, memberPricePanelDrag ? repaidStatChartTabPanel.getHeight() - repaid_cutAfterDrag : repaidStatChartTabPanel.getHeight()-30);
				}						
			
		}
	}];
	
	//头部
	var statisticsTbar = Ext.ux.initTimeBar({
		beginDate : beginDate, 
		endDate : endDate,
		dateCombo : dataCombo, 
		tbarType : 1, 
		statistic : 'memberPrice_', 
		callback : function businessHourSelect(){
			hours = null;}
	}).concat(StatisticsTbarItem);
	
	
	
	
	//memberPriceGrid的栏目
	var cm = new Ext.grid.ColumnModel([
	      new Ext.grid.RowNumberer(),                             
          {header : '日期', dataIndex : 'orderDateFormat'},
          {header : '账单号', dataIndex : 'id', align : 'right', renderer : memberPricelinkOrderId},
          {header : '会员价', dataIndex : 'actualPrice', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
          {header : '原价', dataIndex : 'purePrice', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
          {header : '操作人', dataIndex : 'waiter', align : 'right'},
	      {header : '备注', dataIndex : 'comment'}
    ]);
	
	//默认排序
	cm.defaultSortable = true;
	
	//memberPriceGrid的数据源
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url : '../../QueryMemberPriceStatistics.do'}),
		reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root', idProperty : ''},[
           {name : 'orderDateFormat'},
           {name : 'id'},
           {name : 'actualPrice'},
           {name : 'purePrice'},
           {name : 'waiter'},
           {name : 'comment'}
        ]),
        listeners : {
        	load : function(store, records, options){
        		if(store.getCount() > 0){
        			var sumRow = memberPriceGrid.getView().getRow(store.getCount() - 1);	
        			sumRow.style.backgroundColor = '#EEEEEE';			
        			for(var i = 0; i < memberPriceGrid.getColumnModel().getColumnCount(); i++){
        				var sumCell = memberPriceGrid.getView().getCell(store.getCount() - 1, i);
        				sumCell.style.fontSize = '15px';
        				sumCell.style.fontWeight = 'bold';	
        				sumCell.style.color = 'green';
        			}
        			memberPriceGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
        			memberPriceGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
        			memberPriceGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '--';
        		}
        		$('#memberPriceStatistics_div_mpStatistics').find('.memberPriceLinkId').each(function(index, element){
        			element.onclick = function(){
        				memberPriceShowDetail($(element).val());
        			};
        		});
        	}
        }
	});
	
	var pagingBar = new Ext.PagingToolbar({
		pageSize : 20,
		store : ds,
		displayInfo : true,
		displayMsg : "显示第{0} 条到 {1} 条记录, 共 {2}条",
		emptyMsg : " 没有记录"
	});
	
	//memberPriceGrid表格
	var memberPriceGrid = new Ext.grid.GridPanel({
		id : 'memberPrice_grid',
		border : false,
		frame : false,
		store : ds,
		cm : cm,
		viewConfig : {
			forceFit : true
		},
		loadMask : {
			msg : "数据加载中,请稍后..."
		},
		tbar : statisticsTbar,
		bbar : pagingBar
	});
	
	//定义memberPriceGrid位置
	memberPriceGrid.region = 'center';
	
	var memberPrice_cutAfterDrag = 70;
	var memberPricedetailPanel = new Ext.Panel({
		title : '会员价明细',
		layout : 'border',
		region : 'center',
		frame : true,
		items : [memberPriceGrid],
		listeners : {
			bodyresize : function(e, w, h){
				if(typeof memberPricePanelHeight != 'undefined'){
					var chartHeight = memberPriceTabPanelHeight + (memberPricePanelHeight - h);
					
					memberPriceChartPanel.getEl().setTop((h+30)) ;
					
					memberPrice_changeChartWidth(w,chartHeight-memberPrice_cutAfterDrag);
					
					if(memberPricePanelDrag){
						memberPriceChartPanel.setHeight(chartHeight);
					}
				}
			}			
		}
	}); 
	
	function memberPrice_changeChartWidth(w,h){
		if(eval($('div:visible[data-type=memberPriceChart]').attr('data-value'))){
			if($('div:visible[data-type=memberPriceChart]').length == 1){
				eval($('div:visible[data-type=memberPriceChart]').attr('data-value')).setSize(w, h);
			}else if($('div:visible[data-type=memberPriceChart]').length > 1){
				eval($($('div:visible[data-type=memberPriceChart]')[0]).attr('data-value')).setSize(w*0.4, h);
				eval($($('div:visible[data-type=memberPriceChart]')[1]).attr('data-value')).setSize(w*0.6, h);				
			}
		}	
	}
	
	
	function memberPricelinkOrderId(v){
		return '<a class="memberPriceLinkId">'+ v +'</a>';
	}

	function memberPriceShowDetail(orderID){
		memberPriceWin = new Ext.Window({
			layout : 'fit',
			title : '查看会员价账单',
			width : 510,
			height : 550,
			resizable : false,
			closable : false,
			modal : true,
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function() {
					memberPriceWin.destroy();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					memberPriceWin.destroy();
				}
			}],
			listeners : {
				show : function(thiz) {
					var sd = Ext.ux.getSelData(memberPriceGrid);
					thiz.load({
						url : '../window/history/viewBillDetail.jsp', 
						scripts : true,
						params : {
							orderId : sd.id,
							queryType : 'History'
						},
						method : 'post'
					});
					thiz.center();	
				}
			}
		});
		memberPriceWin.show();
		memberPriceWin.center();
	}
	
	
	var memberPriceDetailChartPanel = new Ext.Panel({
		title : '会员价走势',
		contentEl : 'mpDetailChart_div_mpStatistics',
		listeners : {
			show : function(thiz){
				//thiz.getEl(): 刚打开页面时thiz.getWidth无效
				if(memberPriceDetailChart && typeof thiz.getEl() != 'undefined'){
					memberPriceDetailChart.setSize(thiz.getWidth(), memberPricePanelDrag ? memberPriceChartPanel.getHeight() - 60 : memberPriceChartPanel.getHeight()-30);
				}
			}
		}
	});
	
	
	
	var memberPriceChartPanel = new Ext.TabPanel({
		region : 'south',
		height : 430,
		items : [memberPriceDetailChartPanel],
		listeners : {
			render : function(thiz){
				thiz.setActiveTab(memberPriceDetailChartPanel);
			}
		}
	});
	
	//show会员价报表走势
	function showMemberPriceDetailChart(jdata){
		var dateBegin = Ext.util.Format.date(beginDate.getValue(), 'Y-m-d');
		var dateEnd = Ext.util.Format.date(endDate.getValue(), 'Y-m-d');
		
		var hourBegin = Ext.getCmp('memberPrice_txtBusinessHourBegin').getEl().dom.textContent;
		var hourEnd = Ext.getCmp('memberPrice_txtBusinessHourEnd').getEl().dom.textContent;
		
		var chartData = eval('(' + jdata.other.chart + ')');
		
		memberPriceDetailChart = new Highcharts.Chart({
			plotOptions : {
				line : {
					cursor : 'pointer',
					dataLabels : {
						enabled : true,
						style : {
							fontWeight : 'bold',
							color : 'green'
						}
					},
					events : {
						click : function(e){
							loadBusinessStatistic(e.point.category);
						}
					}
				}
			},
			chart : {
				renderTo : 'mpDetailChart_div_mpStatistics'
			},
			title : {
				text : '<b>会员价走势图(' + dateBegin + ' 至 ' + dateEnd + ')' + hourBegin + ' - ' + hourEnd + '' + StaffName + '</b>'
			},
			labels : {
				items : [{
					html : '<b>会员价总金额:' + chartData.totalMoney + '元</b><br><b>日均会员价额:' + chartData.avgMoney + '元</b><br><b>日均会员价量:' + chartData.avgCount + '份</b>',
					style : {left : '0px', top : '0px'}
				}]
			},
			xAxis : {
				categories : chartData.xAxis,
				labels : {
					formatter : function(){
						return this.value.substring(5, 10);
					}
				}
			},
			yAxis : {
				min : 0,
				title : {
					text : '金额(元)'
				},
				plotLines : [{
					value : 0,
					width : 2,
					color : '#808080'
				}]
			},
			tooltip : {
				formatter : function(){
					return '<b>' + this.series.name + '</b><br/>' + this.x.substring(0, 11) + ': ' + '<b>' + this.y + '元</b>';
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
		
		if(memberPriceDetailChartPanel && memberPriceDetailChartPanel.isVisible()){
			memberPriceDetailChartPanel.show();
		}
		
	}
	

	new Ext.Panel({
		renderTo : 'memberPriceStatistics_div_mpStatistics',
		id : 'memberPriceStatisticsPanel',
		width : parseInt(Ext.getDom('memberPriceStatistics_div_mpStatistics').parentElement.style.width.replace(/px/g, '')),
		height : parseInt(Ext.getDom('memberPriceStatistics_div_mpStatistics').parentElement.style.height.replace(/px/g, '')),
		layout : 'border',
		frame : true,
		items : [memberPricedetailPanel, memberPriceChartPanel]
	});
	
	memberPricePanelHeight = memberPricedetailPanel.getHeight();
	memberPriceTabPanelHeight = memberPriceChartPanel.getHeight();
	
	var memeberPriceRz = new Ext.Resizable(memberPricedetailPanel.getEl(), {
		wrap : true,
		minHeight : 100,
		pinned : false,
		handles : 's',
		listeners : {
			resize : function(thiz, w, h, e){
				memberPricePanelDrag = true;
			}
		}
	});
	
	memeberPriceRz.on('resize', memberPricedetailPanel.syncSize, memberPricedetailPanel);//注册事件(作用:将调好的大小传个scope执行)
	
	var memberPrice_totalHeight = Ext.getCmp('memberPriceStatisticsPanel').getHeight();
	
	memberPricedetailPanel.setHeight(memberPrice_totalHeight*0.4);
	memberPricedetailPanel.getEl().parent().setHeight(memberPrice_totalHeight*0.4);
    
	memberPriceChartPanel.setHeight(memberPrice_totalHeight*0.6);	
    
	memeberPriceRz.resizeTo(memberPricedetailPanel.getWidth(), memberPrice_totalHeight*0.4);
	
	Ext.getCmp('memberPriceStatistics').updateStatisticsDate = setStatisticsDate;
	
	
		
		
});
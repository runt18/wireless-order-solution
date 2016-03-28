Ext.onReady(function(){
	var hours;
	var highChart;
	var chartPanel;
	
	var beginDate = new Ext.form.DateField({
		id : 'beginDate_combo_passengerFlow',
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	var endDate = new Ext.form.DateField({
		id : 'endDate_combo_passengerFlow',
		xtype : 'datefield',
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
			Ext.getCmp('passengerFlow_btnSearch').handler();
		}
	});
	
	
	//区域选择
	var regionSelect_combo_passengerFlow = new Ext.form.ComboBox({
		id : 'regionSelect_combo_passengerFlow',
		readOnly : false,
		forceSelection : true,
		width : 103,
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
				var data = [[-1, '全部']];
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
					failure : function(res, opt){
						thiz.store.loadData({root : [{id : -1, name : '全部'}]});
						thiz.setValue(-1);
					}
				});
			},
			select : function(isJump){
				
				Ext.getCmp('passengerFlow_btnSearch').handler();
			}
		}
	});
	
	//门店选择
	var branchSelect_combo_passengerFlow = new Ext.form.ComboBox({
		id : 'branchSelect_combo_passengerFlow',
		readOnly : false,
		forceSelection : true,
		width : 103,
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
					}
				});
			},
			select : function(isJump){
				//加载区域
				var data = [[-1, '全部']];
				Ext.Ajax.request({
					url : '../../OperateRegion.do',
					params :{
						dataSource : 'getByCond',
						branchId : branchSelect_combo_passengerFlow.getValue()
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						for(var i = 0; i < jr.root.length; i++){
							data.push([jr.root[i]['id'], jr.root[i]['name']]);
						}
						
						regionSelect_combo_passengerFlow.store.loadData(data);
						regionSelect_combo_passengerFlow.setValue(-1);
					}
				});
				
				//加载市别
				var hour = [[-1, '全部']];
				Ext.Ajax.request({
					url : '../../OperateBusinessHour.do',
					params : {
						dataSource : 'getByCond',
						branchId : branchSelect_combo_passengerFlow.getValue()
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						
						for(var i = 0; i < jr.root.length; i++){
							hour.push([jr.root[i]['id'], jr.root[i]['name'], jr.root[i]['opening'], jr.root[i]['ending']]);
						}
						
						hour.push([-2, '自定义']);
						
						Ext.getCmp('passengerFlow_comboBusinessHour').store.loadData(hour);
						Ext.getCmp('passengerFlow_comboBusinessHour').setValue(-1);
					
						if(!isJump){
							Ext.getCmp('businessReceipt_btnSearch').handler();	
						}
					
					}
				});
			}
		}
		
	});
	
	
	//工具栏2
	var passengerFlowTbarFor2 = [{
		xtype : 'tbtext',
		width : 10
	}, {
		xtype : 'tbtext',
		text : '区域选择'},
	{
		xtype : 'tbtext',
		text : '&nbsp;&nbsp;'
	}, regionSelect_combo_passengerFlow, {
		xtype : 'tbtext',
		text : '&nbsp;&nbsp;'
	}, {
		xtype : 'tbtext',
		text : '门店选择'
	}, branchSelect_combo_passengerFlow, '->', {
		text : '搜索',
		id : 'passengerFlow_btnSearch',
		iconCls : 'btn_search',
		handler : function(){

			var dateBegin = Ext.getCmp('beginDate_combo_passengerFlow');
			var dateEnd = Ext.getCmp('endDate_combo_passengerFlow');
			if(!dateBegin.isValid() || !dateEnd.isValid()){
				return;
			}
			var data;
			if(hours){
				data = hours;
			}else{
				data = Ext.ux.statistic_oBusinessHourData({type : 'get', statistic : 'passengerFlow_'}).data;
			}
			
			var regionId;
			var branchId = branchSelect_combo_passengerFlow.getValue();
			if(regionSelect_combo_passengerFlow.getValue() == '-1'){
				regionId = '';
			}else{
				regionId = regionSelect_combo_passengerFlow.getValue();
			}
			
			initPassengerFlowData({
				dateBegin : Ext.util.Format.date(dateBegin.getValue(), 'Y-m-d 00:00:00'),
				dateEnd : Ext.util.Format.date(dateEnd.getValue(), 'Y-m-d 23:59:59'),
				opening : data.opening,
				ending : data.ending,
				regionId : regionId,
				branchId : branchId
			});
		}
	},'-',{
		text : '导出',
		iconCls : 'icon_tb_exoprt_excel',
//		handler : function(){
//			var onDuty = Ext.getCmp('beginDate_combo_passengerFlow');
//			var offDuty = Ext.getCmp('endDate_combo_passengerFlow');
//			var regionId;
//			var branchId = branchSelect_combo_passengerFlow.getValue();
//			if(regionSelect_combo_passengerFlow.getValue() == '-1'){
//				regionId = '';
//			}else{
//				regionId = regionSelect_combo_passengerFlow.getValue();
//			}
//			
//			var url = '../../{0}?pin={1}&restaurantID={2}&dataSource={3}&onDuty={4}&offDuty={5}&region={6}&branchId={7}';
//			url = String.format(
//					url, 
//					'ExportHistoryStatisticsToExecl.do', 
//					-10, 
//					restaurantID, 
//					'passengerFlow',
//					Ext.util.Format.date(onDuty.getValue(), 'Y-m-d 00:00:00'),
//					Ext.util.Format.date(offDuty.getValue(), 'Y-m-d 23:59:59'),
//					regionId,
//					branchId
//				);
//			
//			window.location = url;
//		}
	}];
	
	
	
	//couponGrid的栏目
	var cm = new Ext.grid.ColumnModel([
	    new Ext.grid.RowNumberer(),
	    {header : '操作日期', dataIndex : 'offDutyToDate'},
	    {header : '实收', dataIndex : 'totalActual'},
	    {header : '人数', dataIndex : 'customerAmount'},
	    {header : '人均消费', dataIndex : 'averageCost', renderer : getAvergeCost}
   ]);	
	
	//默认排序
	cm.defaultSortable = true;
	
	var ds = new Ext.data.Store({
		pageSize : 5,
		proxy : new Ext.data.HttpProxy({url : '../../BusinessReceiptsStatistics.do'}),
		reader : new Ext.data.JsonReader({totalProperty : ' totalProperty', root : 'root'}, [
		      {name : 'offDutyToDate'},
		      {name : 'totalActual'},
		      {name : 'customerAmount'},
		      {name : 'averageCost'}
         ])
	})
	
	ds.baseParams['dataSource'] = 'normal';
	ds.load({params : {start : 0, limit : 5}});
	var passengerFlowTbar = Ext.ux.initTimeBar({
		beginDate : beginDate,
		endDate : endDate,
		dateCombo : dataCombo,
		tbarType : 1,
		statistic : 'passengerFlow_',
		callback : function businessHourSelect(){
			hours = null;
		}
	}).concat(passengerFlowTbarFor2);
	

	
	var pagingBar = new Ext.PagingToolbar({
		pageSize : 5,
		store : ds,
		displayInfo : true,
		displayMsg : "显示第{0} 条到 {1} 条记录, 共 {2}条",
		emptyMsg : " 没有记录"
	});
	
	var passengerFlowStatistics = new Ext.grid.GridPanel({
		border : false,
		frame : false,
		scroll : false,
		cm : cm,
		sm : new Ext.grid.RowSelectionModel({
			singleSelect : true
		}),
		store : ds,
		viewConfig : {
			forceFit : true
		},
		loadMask : {
			msg : '数据加载中,请稍后....'
		},
		tbar : passengerFlowTbar,
		bbar : pagingBar,
		listeners : {
			bodyresize : function(){}
		}
	});
	
	passengerFlowStatistics.doLayout();
	
	chartPanel = new Ext.Panel({
		contentEl : 'passengerFlowChart_div_passengerFlowStatistics',
		region : 'south'
	});
	
	passengerFlowStatistics.region = 'center';
	
	function getAvergeCost(value, cssMeta, record){
		if(record.json.totalActual){
			return (record.json.totalActual / record.json.customerAmount).toFixed(2);
		}else{
			return 0;
		}
	}
	
	var couponDetailPanel = new Ext.Panel({
		title : '客流统计',
		layout : 'border',
		region : 'center',
		frame : true,
		items : [passengerFlowStatistics, chartPanel]
	});
	
	
	new Ext.Panel({
		renderTo : 'passengerFlow_div_passenferFlowStatistics',
		id : 'couponStatisticsPanel',
		width : parseInt(Ext.getDom('passengerFlow_div_passenferFlowStatistics').parentElement.style.width.replace(/px/g, '')),
		height : parseInt(Ext.getDom('passengerFlow_div_passenferFlowStatistics').parentElement.style.height.replace(/px/g, '')),
		layout : 'border',
		frame : true,
		items : [couponDetailPanel]
	});
		
	
	
	function initPassengerFlowData(c){
		
		var tempLoadMask = new Ext.LoadMask(document.body, {
			msg : '正在获取信息, 请稍候......',
			remove : true
		});
		tempLoadMask.show();
		Ext.Ajax.request({
			url : '../../BusinessReceiptsStatistics.do',
			params : {
				includingChart : true,
				dataSource : 'normal',
				isPaging : true,
				dateBegin : c.dateBegin,
				dateEnd : c.dateEnd,
				opening : c.opening,
				ending : c.ending,
				region : c.regionId,
				branchId : c.branchId
			},
			success : function(res, opt){
				tempLoadMask.hide();
				var jr = Ext.decode(res.responseText);
				if(jr.success){
					passengerFlowStatistics.getStore().loadData(jr);
					Ext.get('passengerFlow_div_passenferFlowStatistics').setHeight(couponDetailPanel.getHeight());
					showChart(jr);
				}else{
					Ext.ux.showMsg(jr);
				}
			},
			failure : function(res, opt){
				tempLoadMask.hide();
				Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
			}
		});
	}
	
	
	//TODO改变宽度
	//图表
	function showChart(data){
		var dateBegin = Ext.util.Format.date(Ext.getCmp('beginDate_combo_passengerFlow').getValue(), 'Y-m-d');
		var dateEnd = Ext.util.Format.date(Ext.getCmp('endDate_combo_passengerFlow').getValue(), 'Y-m-d');
		
		var hourBegin = Ext.getCmp('passengerFlow_txtBusinessHourBegin').getEl().dom.textContent;
		var hourEnd = Ext.getCmp('passengerFlow_txtBusinessHourEnd').getEl().dom.textContent;
		var chartData = eval('(' + data.other.chart + ')');
		highChart = new Highcharts.Chart({
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
//							loadBusinessStatistic(e.point.category);
						}
					}
				}
			},
			chart: {  
	        	renderTo: 'passengerFlowChart_div_passengerFlowStatistics'
	    	}, 
	    	title: {
	            text: '<b>客流走势图（'+dateBegin+ '至' +dateEnd+'）'+hourBegin+ ' - ' + hourEnd + '</b>'
	        },
	        labels: {
	        	items : [{
	        		html : '<b>总营业额:' + chartData.totalMoney + ' 元</b><br><b>日均收入:' + chartData.avgMoney + ' 元</b><br><b>总客流量:' + chartData.totalCustomer + ' 人</b><br><b>日均客流:' + chartData.avgCustomer + ' 人</b>',
		        	style : {left :/*($('#businessReceiptsChart').width()*0.80)*/'0px', top: '0px'}
	        	}]
	        },
	        xAxis: {
	            categories: chartData.xAxis,
	            labels : {
	            	formatter : function(){
	            		return this.value.substring(5);
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
	                return '<b>' + this.series.name + '</b><br/>'+ this.x +': '+ '<b>'+this.y+'</b> ';
	            }
	        },
	        series : [{data : chartData.ser[0].data, name : chartData.ser[0].name, visible : false}, {data : chartData.ser[1].data, name : chartData.ser[1].name, visible : false} , {data : chartData.ser[2].data, name : chartData.ser[2].name}],
	        exporting : {
	        	enabled : true
	        },
	        credits : {
	        	enabled : false
	        }
		});
	}
	
	dataCombo.setValue(1);
	dataCombo.fireEvent('select', dataCombo, null, 1);
});
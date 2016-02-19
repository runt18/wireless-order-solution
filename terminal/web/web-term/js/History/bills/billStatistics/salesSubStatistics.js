
var SalesState = {
	loadChartRender : function(){
		return '<a href="javascript:Ext.getCmp(\'deptStatistic_btnSearch\').handler(true)">查看走势图</a>';
	}
};

$(function(){
	
	var salesSubQueryType = 0;
	var salesSubDeptId = -1;
	var SALESSUB_PAGE_LIMIT = 22;
	var titleDeptName, titleRegionName, selectDeptId;
	var deptStatPanelGrid, salesSub_hours;
	var salesSubWin, salesSubWinTabPanel, orderFoodStatPanel, orderFoodStatPanelDeptTree, kitchenStatPanel, deptStatPanel;
	
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
	function orderFoodStatPanelInit(){
		orderFoodStatPanelDeptTree = new Ext.tree.TreePanel({
			id : 'orderFoodStatPanelDeptTree',
			region : 'west',
			rootVisible : true,
			frame : true,
			width : 150,	
			animate : true,
			bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
			loader:new Ext.tree.TreeLoader({    
				dataUrl:'../../QueryDeptTree.do?time='+new Date(),
		        baseParams : {
		        	'restaurantID' : restaurantID
				}
		    }),
			root: new Ext.tree.AsyncTreeNode({
				expanded : true,
	            text : '全部',
	            leaf : false,
	            deptID : '-1'
			}),
			tbar : new Ext.Toolbar({
				height : 26,
				items : []
			}),
	        listeners : {
	        	load : function(){
	        		var treeRoot = orderFoodStatPanelDeptTree.getRootNode().childNodes;
	        		for(var i = (treeRoot.length - 1); i >= 0; i--){
						if(treeRoot[i].attributes.deptID == 253){
							orderFoodStatPanelDeptTree.getRootNode().removeChild(treeRoot[i]);
						}
					}
	        	},
	        	click : function(e){
	        		Ext.getDom('lab_salesSubDept_food').innerHTML = e.text;
	        		salesSubDeptId = e.attributes.deptID;
	        		Ext.getCmp('foodStatistic_btnSearch').handler();
	        	}
	        }
		});
		var beginDate = new Ext.form.DateField({
			xtype : 'datefield',		
			format : 'Y-m-d',
			width : 90,
			readOnly : false,
			maxValue : new Date(),
			listeners : {
				blur : function(thiz){									
					Ext.ux.checkDuft(true, thiz.getId(), endDate.getId());
				}
			}
		});
		var endDate = new Ext.form.DateField({
			xtype : 'datefield',
			format : 'Y-m-d',
			width : 90,
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
				Ext.getCmp('foodStatistic_btnSearch').handler();
			}
		});
		var foodName = new Ext.form.TextField({
			width : 100
		});
		
		var foodSale_combo_staffs = new Ext.form.ComboBox({
			id : 'foodSale_combo_staffs',
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
	//					params : {privileges : '1001'},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							for(var i = 0; i < jr.root.length; i++){
								data.push([jr.root[i]['staffID'], jr.root[i]['staffName']]);
							}
							thiz.store.loadData(data);
							thiz.setValue(-1);
							
							if(sendToPageOperation){
								cancel_setStatisticsDate();
							}
	//						else{
	//							cancel_dateCombo.setValue(1);
	//							cancel_dateCombo.fireEvent('select', cancel_dateCombo, null, 1);			
	//						}							
						},
						fialure : function(res, opt){
							thiz.store.loadData(data);
							thiz.setValue(-1);
						}
					});
				},
				select : function(){
					Ext.getCmp('foodStatistic_btnSearch').handler();
				}
			}
		});
	
		var orderFoodStatPanelGridTbarItem = [{
				xtype : 'tbtext',
				text : String.format(Ext.ux.txtFormat.typeName, '部门', 'lab_salesSubDept_food', '----')
			},
		    {xtype:'tbtext',text:'&nbsp;&nbsp;菜品:'}, foodName,
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '区域:'},
			initRegionCombo('foodStatistic_'),
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '员工:'},
			foodSale_combo_staffs,
		    '->', {
			text : '搜索',
			iconCls : 'btn_search',
			id : 'foodStatistic_btnSearch',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				if(bd == '' && ed == ''){
					dateCombo.setValue(0);
					dateCombo.fireEvent('select',dateCombo,null,0);
					return;
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				var gs = orderFoodStatPanelGrid.getStore();
				var data = Ext.ux.statistic_oBusinessHourData({type : 'get', statistic : 'foodStatistic_'}).data;
				gs.baseParams['dateBeg'] = beginDate.getRawValue();
				gs.baseParams['dateEnd'] = endDate.getRawValue();
				gs.baseParams['deptID'] = salesSubDeptId;
				gs.baseParams['foodName'] = foodName.getValue();
				gs.baseParams['region'] = Ext.getCmp("foodStatistic_comboRegion").getValue();
				gs.baseParams['staffId'] = foodSale_combo_staffs.getValue();
				gs.baseParams['opening'] = data.opening;
				gs.baseParams['ending'] = data.ending;
				gs.load({
					params : {
						start : 0,
						limit : SALESSUB_PAGE_LIMIT
					}
				});
			}
		}, '-', {
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				if(bd == '' && ed == ''){
					dateCombo.setValue(0);
					dateCombo.fireEvent('select',dateCombo,null,0);
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				
				var opening, ending;
				var businessHour = Ext.ux.statistic_oBusinessHourData({type : 'get', statistic : 'foodStatistic_'}).data;
				if(parseInt(businessHour.businessHourType) != -1){
					opening = businessHour.opening;
					ending = businessHour.ending;
				}else{
					opening = '';
					ending = '';
				}
				
				var url = '../../{0}?region={1}&dataSource={2}&onDuty={3}&offDuty={4}&deptID={5}&foodName={6}&opening={7}&ending={8}';
				url = String.format(
						url, 
						'ExportHistoryStatisticsToExecl.do', 
						Ext.getCmp("foodStatistic_comboRegion").getValue(), 
						'salesFoodDetail',
						beginDate.getValue().format('Y-m-d 00:00:00'),
						endDate.getValue().format('Y-m-d 23:59:59'),
						salesSubDeptId,
						foodName.getValue(),
						opening,
						ending
					);
				window.location = url;
			}
		}];
		
		var orderFoodStatPanelGridTbar = new Ext.Toolbar({
			height : 26,
			items : orderFoodStatPanelGridTbarItem.concat()
		});
		
		orderFoodStatPanelGrid = createGridPanel(
			'',
			'',
			'',
			'',
			'../../SalesSubStatistics.do',
			[[true, false, false, true], 
	         ['菜品','food.name', 150], 
	         ['销量','salesAmount','','right','Ext.ux.txtFormat.gridDou'], 
	         ['营业额','income',,'right','Ext.ux.txtFormat.gridDou'], 
	         ['口味总额','tasteIncome','','right','Ext.ux.txtFormat.gridDou'], 
	         ['折扣额','discount',,'right','Ext.ux.txtFormat.gridDou'], 
	         ['赠送额','gifted',,'right','Ext.ux.txtFormat.gridDou'],
	         ['单位成本','avgCost','','right','Ext.ux.txtFormat.gridDou'],
	         ['成本','cost','','right','Ext.ux.txtFormat.gridDou'], 
	         ['成本率','costRate','','right','Ext.ux.txtFormat.gridDou'], 
	         ['毛利','profit','','right','Ext.ux.txtFormat.gridDou'], 
	         ['毛利率','profitRate','','right','Ext.ux.txtFormat.gridDou']
	         //['均价','avgPrice','','right','Ext.ux.txtFormat.gridDou'], 
			],
			SalesSubStatRecord.getKeys().concat(['food', 'food.name']),
			[ ['isPaging', true], ['dataType', 1], ['queryType', 1]],
			SALESSUB_PAGE_LIMIT,
			'',
			[orderFoodStatPanelGridTbar, Ext.ux.initTimeBar({beginDate:beginDate, endDate:endDate,dateCombo:dateCombo,statistic : 'foodStatistic_',tbarType: 0})]
		);
		orderFoodStatPanelGrid.keys = [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('foodStatistic_btnSearch').handler();
			}
		}];
		orderFoodStatPanelGrid.region = 'center';
		orderFoodStatPanelGrid.on('render', function(){
			if(sendToPageOperation){
				dateCombo.setValue(1);
				dateCombo.fireEvent('select', dateCombo, null, 1);		
			}
	
		});
		orderFoodStatPanelGrid.getStore().on('load', function(store, records, options){
			if(store.getCount() > 0){
				var sumRow = orderFoodStatPanelGrid.getView().getRow(store.getCount()-1);	
				sumRow.style.backgroundColor = '#EEEEEE';			
				for(var i = 0; i < orderFoodStatPanelGrid.getColumnModel().getColumnCount(); i++){
					var sumRow = orderFoodStatPanelGrid.getView().getCell(store.getCount()-1, i);
					sumRow.style.fontSize = '15px';
					sumRow.style.fontWeight = 'bold';
					sumRow.style.color = 'green';
				}
				//单位成本
				orderFoodStatPanelGrid.getView().getCell(store.getCount() - 1, 7).innerHTML = '--';
				//成本
				//orderFoodStatPanelGrid.getView().getCell(store.getCount() - 1, 8).innerHTML = '--';
				//成本率
				orderFoodStatPanelGrid.getView().getCell(store.getCount() - 1, 9).innerHTML = '--';
				//毛利
				//orderFoodStatPanelGrid.getView().getCell(store.getCount() - 1, 10).innerHTML = '--';
				//毛利率
				orderFoodStatPanelGrid.getView().getCell(store.getCount() - 1, 11).innerHTML = '--';
			}
		});
		orderFoodStatPanel = new Ext.Panel({
			title : '菜品统计',
			layout : 'border',
			items : [orderFoodStatPanelDeptTree, orderFoodStatPanelGrid]
		});	
	}
	
	function kitchenGroupTextTpl(rs){
		return '部门:'+rs[0].get('dept.name');
	}
	
	function kitchenStatPanelInit(){
		var beginDate = new Ext.form.DateField({
			xtype : 'datefield',		
			format : 'Y-m-d',
			width : 90,
			maxValue : new Date(),
			readOnly : false,
			listeners : {
				blur : function(thiz){									
					Ext.ux.checkDuft(true, thiz.getId(), endDate.getId());
				}
			}
		});
		var endDate = new Ext.form.DateField({
			xtype : 'datefield',
			format : 'Y-m-d',
			width : 90,
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
				Ext.getCmp('kitchenStatistic_btnSearch').handler();
			}
		});
		
		var kitchenStatPanelGridTbarItem = [
				{xtype : 'tbtext', text : '区域:'},
				initRegionCombo('kitchenStatistic_'),'->', {
				text : '展开/收缩',
				iconCls : 'icon_tb_toggleAllGroups',
				handler : function(){
					kitchenStatPanelGrid.getView().toggleAllGroups();
				}
			}, '-', {
				text : '搜索',
				id : 'kitchenStatistic_btnSearch',
				iconCls : 'btn_search',
				handler : function(){
					var bd = beginDate.getValue();
					var ed = endDate.getValue();
					if(bd == '' && ed == ''){
						endDate.setValue(new Date());
						Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
					}else if(bd != '' && ed == ''){
						Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
					}else if(bd == '' && ed != ''){
						Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
					}
					var gs = kitchenStatPanelGrid.getStore();
					var data = Ext.ux.statistic_oBusinessHourData({type : 'get', statistic : 'kitchenStatistic_'}).data;
					gs.baseParams['dateBeg'] = beginDate.getRawValue();
					gs.baseParams['dateEnd'] = endDate.getRawValue();
					gs.baseParams['region'] = Ext.getCmp("kitchenStatistic_comboRegion").getValue();
					gs.baseParams['opening'] = data.opening;
					gs.baseParams['ending'] = data.ending;
					gs.load();
					kitchenStatPanelGrid.getView().expandAllGroups();
				}
			}, '-', {
				text : '导出',
		//			hidden : true,
				iconCls : 'icon_tb_exoprt_excel',
				handler : function(){
					var bd = beginDate.getValue();
					var ed = endDate.getValue();
					if(bd == '' && ed == ''){
						dateCombo.setValue(0);
						dateCombo.fireEvent('select',dateCombo,null,0);
					}else if(bd != '' && ed == ''){
						Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
					}else if(bd == '' && ed != ''){
						Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
					}
					var opening, ending;
					var businessHour = Ext.ux.statistic_oBusinessHourData({type : 'get', statistic : 'kitchenStatistic_'}).data;
					if(parseInt(businessHour.businessHourType) != -1){
						opening = businessHour.opening;
						ending = businessHour.ending;
					}else{
						opening = '';
						ending = '';
					}
					
					var url = '../../{0}?region={1}&dataSource={2}&onDuty={3}&offDuty={4}&opening={5}&ending={6}';
					url = String.format(
							url, 
							'ExportHistoryStatisticsToExecl.do', 
							Ext.getCmp("kitchenStatistic_comboRegion").getValue(), 
							'salesByKitchen',
							beginDate.getValue().format('Y-m-d 00:00:00'),
							endDate.getValue().format('Y-m-d 23:59:59'),
							opening,
							ending
						);
					window.location = url;
				}
			}];
		
		var kitchenStatPanelGridTbar = new Ext.Toolbar({
			height : 26,
			items : [Ext.ux.initTimeBar({beginDate:beginDate, endDate:endDate,dateCombo:dateCombo, tbarType : 1, statistic : 'kitchenStatistic_'}).concat(kitchenStatPanelGridTbarItem)]
		});
		
		kitchenStatPanelGrid = createGridPanel(
			'',
			'',
			'',
			'',
			'../../SalesSubStatistics.do',
			[[true, false, false, false], 
		     ['分厨','kitchen.name'], 
		     ['营业额','income',,'right','Ext.ux.txtFormat.gridDou'], 
		     ['折扣额','discount',,'right','Ext.ux.txtFormat.gridDou'], 
		     ['赠送额','gifted',,'right','Ext.ux.txtFormat.gridDou'],
		     ['成本','cost','','right','Ext.ux.txtFormat.gridDou'], 
	         ['成本率','costRate','','right','Ext.ux.txtFormat.gridDou'], 
	         ['毛利','profit','','right','Ext.ux.txtFormat.gridDou'], 
	         ['毛利率','profitRate','','right','Ext.ux.txtFormat.gridDou'],
		     ['dept.id','dept.id', 10]
			],
			SalesSubStatRecord.getKeys().concat(['dept', 'dept.id', 'dept.name', 'kitchen', 'kitchen.name']),
			[['dataType', 1], ['queryType', 2]],
			SALESSUB_PAGE_LIMIT,
			{
				name : 'dept.id',
				hide : true,
				sort : 'dept.id'
			},
			kitchenStatPanelGridTbar
		);
		kitchenStatPanelGrid.view = new Ext.grid.GroupingView({   
	        forceFit:true,   
	        groupTextTpl : '{[kitchenGroupTextTpl(values.rs)]}'
	    });
		kitchenStatPanelGrid.on('render', function(){
			dateCombo.setValue(1);
			dateCombo.fireEvent('select', dateCombo, null, 1);
		});
		kitchenStatPanel = new Ext.Panel({
			title : '分厨统计',
			layout : 'fit',
			items : [kitchenStatPanelGrid]
		});	
	}
	
	function loadDeptStatisticChartData(c){
		var tempLoadMask = new Ext.LoadMask(document.body, {
			msg : '正在生成走势图, 请稍候......',
			remove : true
		});
		tempLoadMask.show();
		Ext.Ajax.request({
			url : '../../SaleStatisticChart.do',
			params : {
				dateBeg : c.dateBeg,
				dateEnd : c.dateEnd,
				region : c.region,
				opening : c.opening,
				ending : c.ending,
				deptId : c.deptId
			},
			success : function(res, opt){
				var jdata = Ext.util.JSON.decode(res.responseText);
				if(jdata.success){
					tempLoadMask.hide();
					showDeptStatisticChart(jdata);
				}
				
			},
			failure : function(res, opt){
				tempLoadMask.hide();
				Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
			}
		});
	}

	function showDeptStatisticChart(jdata){
		
		var dateBegin = Ext.getCmp('deptStatistic_dateSearchDateBegin').getValue().format('Y-m-d');
		var dateEnd = Ext.getCmp('deptStatistic_dateSearchDateEnd').getValue().format('Y-m-d');
		
		var hourBegin = Ext.getCmp('deptStatistic_txtBusinessHourBegin').getEl().dom.textContent;
		var hourEnd = Ext.getCmp('deptStatistic_txtBusinessHourEnd').getEl().dom.textContent;
		
		var chartData = eval('(' + jdata.other.chart + ')');
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
	//						each(e.point.category);
						}
					}
				}
			},
	        chart: {  
	        	renderTo: 'divSalesSubStatisticDeptChart'
	    	}, 
	        title: {
	            text: '<b>'+ titleDeptName +' -- 营业走势图（'+dateBegin+ '至' +dateEnd+'）'+hourBegin+ ' - ' + hourEnd + titleRegionName + '</b>'
	        },
	        labels: {
	        	items : [{
	        		html : '<b>总营业额:' + chartData.totalMoney + ' 元</b><br><b>日均收入:' + chartData.avgMoney + ' 元</b>',
		        	style : {left :'0px', top: '0px'}
	        	}]
	        },
	        xAxis: {
	            categories: chartData.xAxis,
	            labels : {
	            	formatter : function(){
	            		return this.value.substring(5,10);
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
	//	        	crosshairs: true,
	            formatter: function() {
	                return '<b>' + this.series.name + '</b><br/>'+
	                    this.x.substring(0,10) +': '+ '<b>'+this.y+'</b> ';
	            }
	        },
	//	        series : [{  
	//	            name: 'aaaaaa',  
	//	            data: [6, 9, 2, 7, 13, 21, 10]
	//	        }],
	        series : chartData.ser,
	        exporting : {
	        	enabled : true
	        },
	        credits : {
	        	enabled : false
	        }
		});	
	}
	
	function deptStatPanelInit(){
		
		var southDeptChart = new Ext.Panel({
			id : 'southDeptChartPanel',
			region : 'south',
			title : '走势图',
			collapsible : true,
			collapsed : true,
			contentEl : 'divSalesSubStatisticDeptChart'
		});
		
		
		var beginDate = new Ext.form.DateField({
			id : 'deptStatistic_dateSearchDateBegin',
			xtype : 'datefield',		
			format : 'Y-m-d',
			width : 90,
			maxValue : new Date(),
			readOnly : false,
			listeners : {
				blur : function(thiz){									
					Ext.ux.checkDuft(true, thiz.getId(), endDate.getId());
				}
			}
		});
		var endDate = new Ext.form.DateField({
			id : 'deptStatistic_dateSearchDateEnd',
			xtype : 'datefield',
			format : 'Y-m-d',
			width : 90,
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
				Ext.getCmp('deptStatistic_btnSearch').handler();
			}
		});
		
		var deptStatPanelGridTbarItem = [
			{xtype : 'tbtext', text : '区域:'},
			initRegionCombo('deptStatistic_'),
			'->', {
				text : '搜索',
				id : 'deptStatistic_btnSearch',
				iconCls : 'btn_search',
				handler : function(e){
					var bd = beginDate.getValue();
					var ed = endDate.getValue();
					if(bd == '' && ed == ''){
						endDate.setValue(new Date());
						Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
					}else if(bd != '' && ed == ''){
						Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
					}else if(bd == '' && ed != ''){
						Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
					}
					
					var data;
					if(salesSub_hours){
						data = salesSub_hours;
					}else{
						data = Ext.ux.statistic_oBusinessHourData({type : 'get', statistic : 'deptStatistic_'}).data;
					}				
					
					if(typeof e != 'undefined' && typeof e == 'boolean'){
						Ext.getCmp('southDeptChartPanel').expand();
						var gn = Ext.ux.getSelData(deptStatPanelGrid);
						titleDeptName = gn.dept.name;
						selectDeptId = gn.dept.id;
						loadDeptStatisticChartData({dateBeg : beginDate.getRawValue() + ' 00:00:00', 
							dateEnd : endDate.getRawValue() + ' 23:59:59', 
							region : Ext.getCmp("deptStatistic_comboRegion").getValue(),
							opening : data.opening,
							ending : data.ending,
							deptId : selectDeptId});
					}else{
	//					if(!sendToPageOperation){
	//						Ext.getCmp('southDeptChartPanel').collapse();
	//					}
	//					titleDeptName = '';
						titleRegionName = Ext.getCmp("deptStatistic_comboRegion").getValue() == -1 ? '' : ', ' + Ext.getCmp('deptStatistic_comboRegion').getEl().dom.value;
						var gs = deptStatPanelGrid.getStore();
						gs.baseParams['dateBeg'] = beginDate.getRawValue();
						gs.baseParams['dateEnd'] = endDate.getRawValue();
						gs.baseParams['region'] = Ext.getCmp("deptStatistic_comboRegion").getValue();
						gs.baseParams['opening'] = data.opening;
						gs.baseParams['ending'] = data.ending;
						
						gs.load();			
						
						if(typeof selectDeptId != 'undefined'){
							Ext.getCmp('southDeptChartPanel').expand();
							loadDeptStatisticChartData({dateBeg : beginDate.getRawValue() + ' 00:00:00', 
								dateEnd : endDate.getRawValue() + ' 23:59:59', 
								region : Ext.getCmp("deptStatistic_comboRegion").getValue(),
								opening : data.opening,
								ending : data.ending,
								deptId : selectDeptId});						
						}
					}
				}
			}, '-', {
				text : '导出',
	//			hidden : true,
				iconCls : 'icon_tb_exoprt_excel',
				handler : function(){
					var bd = beginDate.getValue();
					var ed = endDate.getValue();
					if(bd == '' && ed == ''){
						dateCombo.setValue(0);
						dateCombo.fireEvent('select',dateCombo,null,0);
					}else if(bd != '' && ed == ''){
						Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
					}else if(bd == '' && ed != ''){
						Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
					}
					
					var opening, ending;
					var businessHour = Ext.ux.statistic_oBusinessHourData({type : 'get', statistic : 'deptStatistic_'}).data;
					if(parseInt(businessHour.businessHourType) != -1){
						opening = businessHour.opening;
						ending = businessHour.ending;
					}else{
						opening = '';
						ending = '';
					}				
					
					
					var url = '../../{0}?region={1}&dataSource={2}&onDuty={3}&offDuty={4}&opening={5}&ending={6}';
					url = String.format(
							url, 
							'ExportHistoryStatisticsToExecl.do', 
							Ext.getCmp("deptStatistic_comboRegion").getValue(), 
							'salesByDept',
							beginDate.getValue().format('Y-m-d 00:00:00'),
							endDate.getValue().format('Y-m-d 23:59:59'),
							opening,
							ending
						);
					window.location = url;
				}
			}];
		
		deptStatPanelGridTbar = new Ext.Toolbar({
			height : 26,
			items : [Ext.ux.initTimeBar({dateCombo:dateCombo, beginDate: beginDate, endDate:endDate, statistic : 'deptStatistic_', callback : function businessHourSelect(){salesSub_hours = null;}}).concat(deptStatPanelGridTbarItem)]
		});
		
		deptStatPanelGrid = createGridPanel(
			'',
			'',
			'',
			'',
			'../../SalesSubStatistics.do',
			[[true, false, false, false], 
		     ['部门','dept.name'],
		     ['营业额','income',,'right','Ext.ux.txtFormat.gridDou'], 
		     ['折扣额','discount',,'right','Ext.ux.txtFormat.gridDou'], 
		     ['赠送额','gifted',,'right','Ext.ux.txtFormat.gridDou'],
		     ['成本','cost','','right','Ext.ux.txtFormat.gridDou'], 
	         ['成本率','costRate','','right','Ext.ux.txtFormat.gridDou'], 
	         ['毛利','profit','','right','Ext.ux.txtFormat.gridDou'], 
	         ['毛利率','profitRate','','right','Ext.ux.txtFormat.gridDou'],
	         ['操作','operateChart','','center','SalesState.loadChartRender']
			],
			SalesSubStatRecord.getKeys().concat(['dept', 'dept.name', 'dept.id']),
			[['dataType', 1], ['queryType', 0]],
			SALESSUB_PAGE_LIMIT,
			null,
			[deptStatPanelGridTbar]
		);
		deptStatPanelGrid.region = "center";
		deptStatPanelGrid.getStore().on('load', function(store, records, options){
			if(store.getCount() > 0){
				var sumRow = deptStatPanelGrid.getView().getRow(store.getCount()-1);	
				sumRow.style.backgroundColor = '#EEEEEE';			
				for(var i = 0; i < deptStatPanelGrid.getColumnModel().getColumnCount(); i++){
					var sumRow = deptStatPanelGrid.getView().getCell(store.getCount()-1, i);
					sumRow.style.fontSize = '15px';
					sumRow.style.fontWeight = 'bold';		
					sumRow.style.color = 'green';
				}
				deptStatPanelGrid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
				deptStatPanelGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
				deptStatPanelGrid.getView().getCell(store.getCount()-1, 9).innerHTML = '--';
			}
			
			if(sendToPageOperation){
				Ext.getCmp('southDeptChartPanel').expand();
				titleDeptName = sendToStatisticsPageDeptName;
				loadDeptStatisticChartData({dateBeg : beginDate.getRawValue() + ' 00:00:00', 
					dateEnd : endDate.getRawValue() + ' 23:59:59', 
					region : Ext.getCmp("deptStatistic_comboRegion").getValue(),
					opening : data.opening,
					ending : data.ending,
					deptId : sendToStatisticsPageDeptId});	
			
				sendToPageOperation = false;		
			}
		});
		deptStatPanelGrid.on('render', function(){
			if(!sendToPageOperation){
				dateCombo.setValue(1);
				dateCombo.fireEvent('select', dateCombo, null, 1);		
			}
		});
		
		
		deptStatPanel = new Ext.Panel({
			title : '部门统计',
			layout : 'border',
			items : [deptStatPanelGrid, southDeptChart],
			listeners : {
				show : function(){
					$('#divSalesSubStatisticDeptChart').show();
				}
			}
		});
		
		
	}
	
	function salesSubWinTabPanelInit(){
		if(!orderFoodStatPanel){
			orderFoodStatPanelInit();		
		}
		if(!kitchenStatPanel){
			kitchenStatPanelInit();		
		}
		if(!deptStatPanel){
			deptStatPanelInit();		
		}
		
		salesSubWinTabPanel = new Ext.TabPanel({
			xtype : 'tabpanel',
			region : 'center',
			frame : true,
			activeTab : 0,
			border : false,
			items : [orderFoodStatPanel,kitchenStatPanel,deptStatPanel],
			listeners : {
				tabchange : function(thiz, tab){
				
				}
			}
		});	
	}
	
	var saleSub_setStatisticsDate = function(){
		if(sendToPageOperation){
			Ext.getCmp('deptStatistic_dateSearchDateBegin').setValue(sendToStatisticsPageBeginDate);
			Ext.getCmp('deptStatistic_dateSearchDateEnd').setValue(sendToStatisticsPageEndDate);
			
			salesSub_hours = sendToStatisticsPageHours;
			
			if(typeof salesSub_hours.deptId != 'undefined'){
				titleDeptName = salesSub_hours.deptName;
				selectDeptId = salesSub_hours.deptId;
			}		
			
			salesSubWinTabPanel.setActiveTab(deptStatPanel);
	
			Ext.getCmp('deptStatistic_btnSearch').handler();
			
			Ext.getCmp('deptStatistic_comboBusinessHour').setValue(sendToStatisticsPageHours.hourComboValue);
			
			if(typeof salesSub_hours.opening != 'undefined'){
				Ext.getCmp('deptStatistic_txtBusinessHourBegin').setText('<font style="color:green; font-size:20px">'+salesSub_hours.openingText+'</font>');
				Ext.getCmp('deptStatistic_txtBusinessHourEnd').setText('<font style="color:green; font-size:20px">'+salesSub_hours.endingText+'</font>');		
			}		
			sendToPageOperation = false;		
		}	
	};
	
	Ext.onReady(function(){
		if(!salesSubWinTabPanel){
			salesSubWinTabPanelInit();
		}
		
		new Ext.Panel({
			renderTo : 'divSalesSubStatistics',//渲染到
			id : 'salesSubStatisticsPanel',
			//solve不跟随窗口的变化而变化
			width : parseInt(Ext.getDom('divSalesSubStatistics').parentElement.style.width.replace(/px/g,'')),
			height : parseInt(Ext.getDom('divSalesSubStatistics').parentElement.style.height.replace(/px/g,'')),
			layout:'fit',
			frame : true, //边框
			//子集
			items : [salesSubWinTabPanel]
		});
								
		if(sendToPageOperation){
			saleSub_setStatisticsDate();		
		}else{
			salesSubWinTabPanel.setActiveTab(orderFoodStatPanel);
		}
		
		Ext.getCmp('salesSubStatistics').updateStatisticsDate = saleSub_setStatisticsDate;
		
	});
	
});

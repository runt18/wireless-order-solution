
$(function(){

	var receivablesStaticRecordCount = 93;
	var highChart;
	
	var businessPanelHeight = 0, panelDrag = false;
	
	var southPanel;
	
	var tempLoadMask = new Ext.LoadMask(document.body, {
		msg : '正在获取信息, 请稍候......',
		remove : true
	});
	
	
	function initBusinessReceipsData(c){
		tempLoadMask.show();
		Ext.Ajax.request({
			url : '../../BusinessReceiptsStatistics.do',
			params : {
				includingChart : true,
				dataSource : 'normal',
				isPaging : true,
				StatisticsType : 'History',
				dateBegin : c.dateBegin,
				dateEnd : c.dateEnd,
				opening : c.opening,
				ending : c.ending,
				region : c.regionId,
				branchId : c.branchId
			},
			success : function(res, opt){
				tempLoadMask.hide();
				var jr = Ext.util.JSON.decode(res.responseText);
				if(jr.success){
					receivablesStatResultGrid.getStore().loadData(jr);
					Ext.get('businessReceiptsChart').setHeight(southPanel.getHeight());
					recipe_showChart(jr);
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
	
	function newDate(str) { 
		str = str.split('-'); 
		var date = new Date(); 
		date.setUTCFullYear(str[0], str[1] - 1, str[2]); 
		date.setUTCHours(0, 0, 0, 0); 
		return date; 
	} 
	
	function loadBusinessStatistic(x){
		var date = newDate(x).getTime();
		businessStatWin = new Ext.Window({
			title : '营业统计 -- <font style="color:green;">历史</font>',
			width : 885,
			height : 580,
			closable : false,
			modal : true,
			resizable : false,	
			layout: 'fit',
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					businessStatWin.destroy();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					businessStatWin.destroy();
				}
			}],
			listeners : {
				hide : function(thiz){
					thiz.body.update('');
				},
				show : function(thiz){
					thiz.load({
						autoLoad : false,
						url : '../window/history/businessStatistics.jsp',
						scripts : true,
						nocache : true,
						text : '功能加载中, 请稍后......'
					});
					
					thiz.dataSource = 'history';
					thiz.dutyRange = 'range';
					thiz.offDuty = date;
					thiz.onDuty = date;
					thiz.queryPattern = 3;
					thiz.branchId = Ext.getCmp('branchSelect_combo_businessReceips').getValue();
				}
			}
		});
		businessStatWin.show();
		businessStatWin.center();
	}
	
	function recipe_showChart(jdata){
		var dateBegin = Ext.util.Format.date(Ext.getCmp('beginDate_combo_receipts').getValue(), 'Y-m-d');
		var dateEnd = Ext.util.Format.date(Ext.getCmp('endDate_combo_receipts').getValue(), 'Y-m-d');
		
		var hourBegin = Ext.getCmp('businessReceipt_txtBusinessHourBegin').getEl().dom.textContent;
		var hourEnd = Ext.getCmp('businessReceipt_txtBusinessHourEnd').getEl().dom.textContent;
		
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
							loadBusinessStatistic(e.point.category);
						}
					}
				}
			},
	        chart: {  
	        	renderTo: 'businessReceiptsChart'
	    	}, 
	        title: {
	            text: '<b>营业走势图（'+dateBegin+ '至' +dateEnd+'）'+hourBegin+ ' - ' + hourEnd + '</b>'
	        },
	        labels: {
	        	items : [{
	        		html : '<b>总营业额:' + chartData.totalMoney + ' 元</b><br><b>日均收入:' + chartData.avgMoney + ' 元</b><br><b>日均账单:' + chartData.avgCount + ' 张</b>',
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
	//	        	crosshairs: true,
	            formatter: function() {
	                return '<b>' + this.series.name + '</b><br/>'+
	                    this.x +': '+ '<b>'+this.y+'</b> ';
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
	
	var receivablesStatResultGrid, receipts_dateCombo;
	function initBusinessReceipsGrid(c){
		
		if(c.data == null || typeof c.data == 'undefined'){
			c.data = {successProperty : true, totalProperty:2, root: []};
		}
		
		var receivablesStatResultStoreRecords = [{
				name : "totalIncome"
			}, {
				name : 'offDutyToDate'
			}, {
				name : "orderAmount"
		}];
			
		var receivablesStatResultStoreRecords2 = [{
				name : "paidIncome"
			}, {
				name : "discountIncome"
			}, {
				name : "giftIncome"
			}, {
				name : "cancelIncome"
			}, {
				name : "eraseAmount"
			}, {
				name : "eraseIncome"
			}, {
				name : "couponAmount"
			}, {
				name : "couponIncome"
			}, {
				name : "offDuty"
			}, {
				name : "totalActual"
			}, {
				name : "totalActualCharge"
			}, {
				name : "totalActualRefund"
		}];
		
		var receivablesStatResultColumnModelRecords = [
			new Ext.grid.RowNumberer(), {
				header : '日期',
				dataIndex : 'offDutyToDate',
				width : 100
			}, {
				header : '应收',
				dataIndex : 'totalIncome',
				renderer : Ext.ux.txtFormat.gridDou,
				align : 'right',
				width : 100
			}, {
				header : '实收',
				dataIndex : 'totalActual',
				renderer : Ext.ux.txtFormat.gridDou,
				align : 'right',
				width : 100
			}, {
				header : '账单数',
				dataIndex : 'orderAmount',
				align : 'right',
				width : 70
		}];
		
		for (var i = 0; i < business_receipts_payType.length; i++) {
			receivablesStatResultStoreRecords.push({name : 'payType'+business_receipts_payType[i].id});
			receivablesStatResultColumnModelRecords.push({
				header : business_receipts_payType[i].name,
				dataIndex : 'payType'+business_receipts_payType[i].id,
				renderer : Ext.ux.txtFormat.gridDou,
				align : 'right',
				width : 100
			});
		}
		
		var receivablesStatResultStore = new Ext.data.Store({
			proxy : new Ext.data.MemoryProxy(c.data),
			reader : new Ext.data.JsonReader({
				totalProperty : "totalProperty",
				root : "root"
			},
			receivablesStatResultStoreRecords.concat(receivablesStatResultStoreRecords2)),
			listeners : {
				load : function(store, records, options){
					if(store.getCount() > 0){
		     			var sumRow = receivablesStatResultGrid.getView().getRow(store.getCount() - 1);
		     			sumRow.style.backgroundColor = '#EEEEEE';
						for(var i = 0; i < receivablesStatResultGrid.getColumnModel().getColumnCount(); i++){
							var sumCell = receivablesStatResultGrid.getView().getCell(store.getCount() -1, i);
							sumCell.style.fontSize = '15px';
		    				sumCell.style.fontWeight = 'bold';
		    				sumCell.style.color= 'green';
						}
						
						receivablesStatResultGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
		     			
		     		}
				}
			}
		});
		
	
		var receivablesStatResultColumnModelRecords2 = [{
				header : '折扣',
				dataIndex : 'discountIncome',
				renderer : Ext.ux.txtFormat.gridDou,
				align : 'right',
				width : 70
			}, {
				header : '赠送',
				dataIndex : 'giftIncome',
				renderer : Ext.ux.txtFormat.gridDou,
				align : 'right',
				width : 70
			}, {
				header : '退菜',
				dataIndex : 'cancelIncome',
				renderer : Ext.ux.txtFormat.gridDou,
				align : 'right',
				width : 70
			}, {
				header : '抹数',
				dataIndex : 'eraseIncome',
				renderer : Ext.ux.txtFormat.gridDou,
				align : 'right',
				width : 70
			}, {
				header : '反结帐',
				dataIndex : 'paidIncome',
				renderer : Ext.ux.txtFormat.gridDou,
				align : 'right',
				width : 70
			}, {
				header : '优惠劵',
				dataIndex : 'couponIncome',
				renderer : Ext.ux.txtFormat.gridDou,
				align : 'right',
				width : 70
			}, {
				header : '会员充值',
				dataIndex : 'totalActualCharge',
				renderer : Ext.ux.txtFormat.gridDou,
				align : 'right',
				width : 90
			}, {
				header : '会员退款',
				dataIndex : 'totalActualRefund',
				renderer : Ext.ux.txtFormat.gridDou,
				align : 'right',
				width : 90
		}];
		// 2，栏位模型
		var receivablesStatResultColumnModel = new Ext.grid.ColumnModel(receivablesStatResultColumnModelRecords.concat(receivablesStatResultColumnModelRecords2));
		
		var receipts_beginDate = new Ext.form.DateField({
			xtype : 'datefield',	
			id : 'beginDate_combo_receipts',
			format : 'Y-m-d',
			width : 100,
			maxValue : new Date(),
			readOnly : false,
			allowBlank : false
		});
		var receipts_endDate = new Ext.form.DateField({
			xtype : 'datefield',
			id : 'endDate_combo_receipts',
			format : 'Y-m-d',
			width : 100,
			maxValue : new Date(),
			readOnly : false,
			allowBlank : false
		});
		receipts_dateCombo = Ext.ux.createDateCombo({
			width : 90,
			beginDate : receipts_beginDate,
			endDate : receipts_endDate,
			callback : function(){
				Ext.getCmp('businessReceipt_btnSearch').handler();
			}
		});
		
		//区域选择
		var regionSelect_combo_businessReceips = new Ext.form.ComboBox({
			id : 'regionSelect_combo_businessReceips',
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
						},
						failure : function(res, opt){
							thiz.store.loadData({root:[{id:-1, name:'全部'}]});
							thiz.setValue(-1);
						}
					});
				},
				select : function(){
					Ext.getCmp('businessReceipt_btnSearch').handler();
				}
			}
		});
		
		//门店选择
		var branchSelect_combo_businessReceips = new Ext.form.ComboBox({
			id : 'branchSelect_combo_businessReceips',
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
//					var data = [];
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
							}else{
								var data = [[-1, '全部']];
								data.push([jr.root[0]['id'], jr.root[0]['name'] + '(集团)']);
								
								for(var i = 0; i < jr.root[0].branches.length; i++){
									data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
								}
								
								thiz.store.loadData(data);
								thiz.setValue(-1);
							}
							
							
							thiz.fireEvent('select');
						},
						failure : function(res, opt){
							thiz.store.loadData({root:[{id:-1, name:'全部'}]});
							thiz.setValue(-1);
						}
					});
				},
				select : function(isJump){
					
					if(branchSelect_combo_businessReceips.getValue() == -1){
						var data = [];
						
						regionSelect_combo_businessReceips.store.loadData(data);
						regionSelect_combo_businessReceips.setValue('');
						regionSelect_combo_businessReceips.setDisabled(true);
						
						Ext.getCmp('businessReceipt_comboBusinessHour').store.loadData(data);
						Ext.getCmp('businessReceipt_comboBusinessHour').setValue('');
						Ext.getCmp('businessReceipt_comboBusinessHour').setDisabled(true);
						
					}else{
						//加载区域
						var data = [[-1,'全部']];
						Ext.Ajax.request({
							url : '../../OperateRegion.do',
							params : {
								dataSource : 'getByCond',
								branchId : branchSelect_combo_businessReceips.getValue()
							},
							success : function(res, opt){
								var jr = Ext.decode(res.responseText);
								for(var i = 0; i < jr.root.length; i++){
									data.push([jr.root[i]['id'], jr.root[i]['name']]);
								}
								regionSelect_combo_businessReceips.store.loadData(data);
								regionSelect_combo_businessReceips.setValue(-1);
								regionSelect_combo_businessReceips.setDisabled(false);
								
							}
						});
						
						//加载市别
						var hour = [[-1, '全部']];
						Ext.Ajax.request({
							url : '../../OperateBusinessHour.do',
							params : {
								dataSource : 'getByCond',
								branchId : branchSelect_combo_businessReceips.getValue()
							},
							success : function(res, opt){
								var jr = Ext.decode(res.responseText);
								
								for(var i = 0; i < jr.root.length; i++){
									hour.push([jr.root[i]['id'], jr.root[i]['name'], jr.root[i]['opening'], jr.root[i]['ending']]);
								}
								
								hour.push([-2, '自定义']);
								
								Ext.getCmp('businessReceipt_comboBusinessHour').store.loadData(hour);
								Ext.getCmp('businessReceipt_comboBusinessHour').setValue(-1);
								Ext.getCmp('businessReceipt_comboBusinessHour').setDisabled(false);
							}
						});
					}
					
					if(!isJump){
						Ext.getCmp('businessReceipt_btnSearch').handler();	
					}
					
				}
			}
		});
		
		
		
		
		
		var businessReceiptGridTbarItem = [
        {xtype : 'tbtext', text : '&nbsp;&nbsp;'}, {
			xtype : 'tbtext',
			text : '区域选择:'
		}, regionSelect_combo_businessReceips,
		{xtype : 'tbtext', text : '&nbsp;&nbsp;'}, {
			xtype : 'tbtext',
			text : '门店选择'
		}, branchSelect_combo_businessReceips, '->', {
			text : '搜索',
			id : 'businessReceipt_btnSearch',
			iconCls : 'btn_search',
			handler : function(){
				var dateBegin = Ext.getCmp('beginDate_combo_receipts');
				var dateEnd = Ext.getCmp('endDate_combo_receipts');
				
				if(!dateBegin.isValid() || !dateEnd.isValid()){
					return;
				}
				var data;
				if(receipt_hours){
					data = receipt_hours;
				}else{
					data = Ext.ux.statistic_oBusinessHourData({type : 'get', statistic : 'businessReceipt_'}).data;
				}
				
				var regionId;
				var branchId = branchSelect_combo_businessReceips.getValue();
				if(regionSelect_combo_businessReceips.getValue() == '-1'){
					regionId = '';
				}else{
					regionId = regionSelect_combo_businessReceips.getValue();
				}
				
				
				initBusinessReceipsData({
					dateBegin : Ext.util.Format.date(dateBegin.getValue(), 'Y-m-d 00:00:00'), 
					dateEnd :Ext.util.Format.date(dateEnd.getValue(), 'Y-m-d 23:59:59'),
					opening : branchId == -1 ? '' : data.opening,
					ending : branchId == -1 ? '' : data.ending,
					regionId : regionId,
					branchId : branchId
				});
				
			}
		}, '-', {
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				var onDuty = Ext.getCmp('beginDate_combo_receipts');
				var offDuty = Ext.getCmp('endDate_combo_receipts');
				var regionId;
				var branchId = branchSelect_combo_businessReceips.getValue();
				if(regionSelect_combo_businessReceips.getValue() == '-1'){
					regionId = '';
				}else{
					regionId = regionSelect_combo_businessReceips.getValue();
				}
				
				var url = '../../{0}?pin={1}&restaurantID={2}&dataSource={3}&onDuty={4}&offDuty={5}&region={6}&branchId={7}';
				url = String.format(
						url, 
						'ExportHistoryStatisticsToExecl.do', 
						-10, 
						restaurantID, 
						'businessReceips',
						Ext.util.Format.date(onDuty.getValue(), 'Y-m-d 00:00:00'),
						Ext.util.Format.date(offDuty.getValue(), 'Y-m-d 23:59:59'),
						regionId,
						branchId
					);
				
				window.location = url;
			}
		}];
		
		var businessReceiptGridTbar = new Ext.Toolbar({
			height : 26,
			items : [Ext.ux.initTimeBar({beginDate:receipts_beginDate, endDate:receipts_endDate,dateCombo:receipts_dateCombo, tbarType : 1, statistic : 'businessReceipt_', callback : function businessHourSelect(){receipt_hours = null;}}).concat(businessReceiptGridTbarItem)]
		});
		
		
		receivablesStatResultGrid = new Ext.grid.GridPanel({
			xtype : "grid",
		//	frame : true,
			border : false,
			region : 'center',
			ds : receivablesStatResultStore,
			autoScroll : true,
			loadMask : {
				msg : "数据加载中，请稍等..."
			},
			cm : receivablesStatResultColumnModel,
			sm : new Ext.grid.RowSelectionModel({
				singleSelect : true
			}),
			listeners : {
				dblclick : function(){
					var data = Ext.ux.getSelData(receivablesStatResultGrid);
					loadBusinessStatistic(data['offDutyToDate']);
				},
				bodyresize : function(e, w, h){
					var chartHeight;
					if(h < businessPanelHeight){
						chartHeight = 250 + (businessPanelHeight - h);
					}else{
						chartHeight = 250 + (h - businessPanelHeight);
					}
					changeChartWidth(w,chartHeight);
					
					southPanel.getEl().setTop((h+55)) ;
					
					if(panelDrag){
						southPanel.setHeight(chartHeight);
					}
					
					receivablesStatResultGrid.getEl().parent().setWidth(w);
					receivablesStatResultGrid.doLayout();
					
				}
			},
			tbar : businessReceiptGridTbar,
			bbar : new Ext.PagingToolbar({
				pageSize : receivablesStaticRecordCount,
				store : receivablesStatResultStore,
				displayInfo : true,
				displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
				emptyMsg : "没有记录"
			})
		});
	}
	
	function changeChartWidth(w,h){
		if(highChart != undefined){
			highChart.setSize(w, h);
		}
		
	}
	
	var receipt_hours;
	var business_receipts_payType;

	Ext.onReady(function(){
		//获取总共的付款方式, 动态生成columnModel & Store的Records
		$.ajax({
			url : '../../OperatePayType.do',
			type : 'post',
			async:false,
			data : {
				dataSource : 'getByCond',
				designed : true,
				extra : true,
				member : true
			},
			success : function(jr, status, xhr){
				business_receipts_payType = jr.root;
			},
			error : function(request, status, err){
			}
		}); 	
		
		southPanel = new Ext.Panel({
			contentEl : 'businessReceiptsChart',
			region : 'south'
		});
		
		initBusinessReceipsGrid({data : null});
		
		new Ext.Panel({
			renderTo : 'divBusinessReceiptStatistics',//渲染到
			id : 'businessReceiptStatisticsPanel',
			//solve不跟随窗口的变化而变化
			width : parseInt(Ext.getDom('divBusinessReceiptStatistics').parentElement.style.width.replace(/px/g,'')),
			height : parseInt(Ext.getDom('divBusinessReceiptStatistics').parentElement.style.height.replace(/px/g,'')),
			layout:'border',
			frame : true, //边框
			items : [receivablesStatResultGrid,southPanel]
		});
		
		businessPanelHeight = receivablesStatResultGrid.getHeight();
		
		var rz = new Ext.Resizable(receivablesStatResultGrid.getEl(), {
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
	    rz.on('resize', receivablesStatResultGrid.syncSize, receivablesStatResultGrid);//注册事件(作用:将调好的大小传个scope执行)
		
		
		receipts_dateCombo.setValue(1);
		receipts_dateCombo.fireEvent('select', null, null, 1);			
	});

});
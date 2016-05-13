
Ext.onReady(function(){
	var mcus_grid, mcus_search_memberType
		,mcus_search_onDuty, mcus_search_offDuty, mcus_search_memberName, mcus_search_dateCombo;
	var mcus_modal = true;
	
	var mcus_highChart, mcus_PanelHeight = 0, mcus_panelDrag = false;
	
	var mcus_southPanel;
	
	function mcus_showChart(){
		var dateBegin = Ext.util.Format.date(mcus_search_onDuty.getValue(), 'Y-m-d');
		var dateEnd = Ext.util.Format.date(mcus_search_offDuty.getValue(), 'Y-m-d');
		
		var chartData;
		$.ajax({
			url : '../../QueryMemberStatistics.do',
			type : 'post',
			dataType : 'json',
			data : {
				dataSource :'consumeStatistics',
				dateBegin : dateBegin,
				dateEnd : dateEnd + " 23:59:59",
				branchId : Ext.getCmp('branch_combo_memberConsume').getValue()
			},
			async : false,
			success : function(data){
				chartData = eval('(' + data.other.businessChart + ')');
			},
			error : function(xhr){
			}
		});
		
		
		mcus_highChart = new Highcharts.Chart({
			plotOptions : {
				line : {
					cursor : 'pointer',
					dataLabels : {
						enabled : true,
						style : {
							fontWeight: 'bold', 
							color: 'green' 
						}
					}
				}
			},
	        chart: {  
	        	renderTo: 'memberConsumeChart'
	    	}, 
	        title: {
	            text: '<b>会员消费走势图（'+ dateBegin +'至'+ dateEnd +'）</b>'
	        },
	        labels: {
	        	items : [{
	        		html : '<b>日均消费额:' + chartData.avgMoney + ' 元</b><br><b>日均消费次数:' + chartData.avgCount + ' 次</b>',
		        	style : {left :/*($('#memberConsumeChart').width()*0.80)*/'0px', top: '0px'}
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
	
	
	function mcus_initBusinessReceipsGrid(c){
		
		mcus_search_memberType = new Ext.form.ComboBox({
			id : 'mcus_search_memberType',
			width : 90,
			forceSelection : true,
			readOnly : false,
			store : new Ext.data.JsonStore({
				root : 'root',
				fields : ['id', 'name']
			}),
			valueField : 'id',
			displayField : 'name',
			listClass : ' x-menu ',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				render : function(thiz){
					Ext.Ajax.request({
						url : '../../QueryMemberType.do?',
						params : {
							isCookie : true,
							dataSource : 'normal',
							restaurantID : restaurantID
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								jr.root.unshift({id:-1, name:'全部'});
							}else{
								Ext.example.msg('异常', '会员类型数据加载失败, 请联系客服人员.');
							}
							thiz.store.loadData(jr);
							thiz.setValue(-1);
							
						},
						failure : function(res, opt){
							thiz.store.loadData({root:[{typeId:-1, name:'全部'}]});
							thiz.setValue(-1);
						}
					});
				},
				select : function(){
					mcus_searchMemberOperation();
				}
			}
		});
		mcus_search_onDuty = new Ext.form.DateField({
			xtype : 'datefield',
			width : 100,
			format : 'Y-m-d',
			maxValue : new Date(),
			hideParent : true,
			hidden : mcus_modal ? false : true,
			readOnly : false,
			allowBlank : false
		});
		mcus_search_offDuty = new Ext.form.DateField({
			xtype : 'datefield',
			width : 100,
			format : 'Y-m-d',
			maxValue : new Date(),
			hideParent : true,
			hidden : mcus_modal ? false : true,
			readOnly : false,
			allowBlank : false
		});
		mcus_search_dateCombo = Ext.ux.createDateCombo({
			beginDate : mcus_search_onDuty,
			endDate : mcus_search_offDuty,
			callback : function(){
				mcus_searchMemberOperation();
			}
		});
		mcus_search_memberName = new Ext.form.TextField({
			xtype : 'textfield',
			width : 100
			
		});
		
		//门店选择
		var branch_combo_memberConsume = new Ext.form.ComboBox({
			id : 'branch_combo_memberConsume',
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
								data.push([jr.root[0]['id'], jr.root[0]['name']]);
							}else{
								data.push([-1, '全部'], [jr.root[0]['id'], jr.root[0]['name'] + '(集团)']);
								
								for(var i = 0; i < jr.root[0].branches.length; i++){
									data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
								}
							}
							
							
							thiz.store.loadData(data);
							
							if(jr.root[0].typeVal != '2'){
								thiz.setValue(jr.root[0].id);
							}else{
								thiz.setValue(-1);
							}
							
							thiz.fireEvent('select');
						}
					});
				},
				select : function(){
					if(branch_combo_memberConsume.getValue() == -1){
						Ext.getCmp('memberConsume_comboPayType').disable();
					}else{
						Ext.getCmp('memberConsume_comboPayType').enable();
						//加载收款方式
						var payType = [[-1, '全部']];
						Ext.Ajax.request({
							url : '../../OperatePayType.do',
							params : {
								dataSource : 'getByCond',
								branchId : branch_combo_memberConsume.getValue()
							},
							success : function(res, opt){
								var jr = Ext.decode(res.responseText);
								
								jr.root.unshift({id:-1, name:'全部'});
								Ext.getCmp('memberConsume_comboPayType').getStore().loadData(jr.root);
							}
						});
					}
					Ext.getCmp('memberConsume_comboPayType').setValue(-1);
					
					Ext.getCmp('memberConsume_btn_search').handler();
					
				}
			}
		});
		
		var mcus_mo_tbar = new Ext.Toolbar({
			height : 26,
			items : [{ 
				xtype : 'tbtext', 
				text : (mcus_modal ? '&nbsp;&nbsp;日期:&nbsp;' : ' ')
			}, mcus_search_dateCombo, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			}, mcus_search_onDuty, { 
				xtype : 'tbtext',
				text : (mcus_modal ? '&nbsp;至&nbsp;' : ' ')
			}, mcus_search_offDuty, 
			{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;会员类型:'
			}, mcus_search_memberType, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;收款方式:'			
			},{
				xtype : 'combo',
				forceSelection : true,
				width : 80,
				id : 'memberConsume_comboPayType',
				store : new Ext.data.JsonStore({
					fields : [ 'id', 'name' ]
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
						Ext.Ajax.request({
							url : '../../OperatePayType.do',
							params : {
								dataSource : 'getByCond'
							},
							success : function(res, opt){
								var jr = Ext.decode(res.responseText);
								if(jr.success){
									jr.root.unshift({id:-1, name:'全部'});
									thiz.store.loadData(jr.root);
									thiz.setValue(-1);
								}
							},
							failure : function(res, opt){
								thiz.store.loadData({root:[{typeId:-1, name:'全部'}]});
								thiz.setValue(-1);
							}
						});
					},
					select : function(){
						mcus_searchMemberOperation();
					}
				}				
			}, 
			{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;手机号/卡号/会员名称:'
			}, mcus_search_memberName, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;门店选择 :'
			}, branch_combo_memberConsume, '->', {
				text : '搜索',
				id : 'memberConsume_btn_search',
				iconCls : 'btn_search',
				handler : function(e){
					mcus_searchMemberOperation();
				}
			}, {
				text : '重置',
				iconCls : 'btn_refresh',
				handler : function(e){
					mcus_search_memberType.setValue(-1);
					mcus_search_memberName.setValue();
					mcus_searchMemberOperation();
				}
				
			}, '-', {
					text : '导出',
					iconCls : 'icon_tb_exoprt_excel',
					handler : function(){
	/*					var onDuty = '', offDuty = '';
						onDuty = Ext.util.Format.date(mcus_search_onDuty.getValue(), 'Y-m-d 00:00:00');
						offDuty = Ext.util.Format.date(mcus_search_offDuty.getValue(), 'Y-m-d 23:59:59');
						
						var memberType = mcus_search_memberType.getRawValue() != '' ? mcus_search_memberType.getValue() : '';
						var url = '../../{0}?memberType={1}&dataSource={2}&onDuty={3}&offDuty={4}&fuzzy={5}&dataSources={6}&detailOperate={7}&payType={8}';
						url = String.format(
								url, 
								'ExportHistoryStatisticsToExecl.do', 
								memberType > 0 ? memberType : '', 
								'rechargeDetail',
								onDuty,
								offDuty,
								mcus_search_memberName.getValue(),
								'history',
								1,
								Ext.getCmp('memberConsume_comboPayType').getValue()
							);
						window.location = url;*/
						
						
						var onDuty = '', offDuty = '';
						onDuty = Ext.util.Format.date(mcus_search_onDuty.getValue(), 'Y-m-d 00:00:00');
						offDuty = Ext.util.Format.date(mcus_search_offDuty.getValue(), 'Y-m-d 23:59:59');
						
						var memberType = mcus_search_memberType.getRawValue() != '' ? mcus_search_memberType.getValue() : '';
						var url = '../../{0}?memberType={1}&dataSource={2}&onDuty={3}&offDuty={4}&fuzzy={5}&dataSources={6}&operateType=1&payType={7}&branchId={8}';
						url = String.format(
								url, 
								'ExportHistoryStatisticsToExecl.do', 
								memberType > 0 ? memberType : '', 
								'consumeDetail',
								onDuty,
								offDuty,
								mcus_search_memberName.getValue(),
								'history',
								Ext.getCmp('memberConsume_comboPayType').getValue(),
								Ext.getCmp('branch_combo_memberConsume').getValue()
							);
						window.location = url;
					}
				}]
		});
		mcus_grid = createGridPanel(
			'',
			'',
			'',
			'',
			'../../QueryMemberOperation.do',
			[
				[true, false, false, true],
				['日期', 'operateDateFormat'],
				['操作类型', 'operateTypeText', 90, 'center'],
				['会员名称', 'member.name', 60],
				['手机号码', 'member.mobile', 60],
				['会员卡号', 'member.memberCard', 60],
				['操作门店', 'branchName', 60],
				['消费金额', 'payMoney', 60, 'right', Ext.ux.txtFormat.gridDou],
				['剩余金额', 'remainingTotalMoney', 60, 'right', Ext.ux.txtFormat.gridDou],
				['付款方式', 'payTypeText'],
				['变动积分', 'deltaPoint', 60, 'right'],
				['剩余积分', 'remainingPoint', 60, 'right'],
				['操作人', 'staffName', 90, 'center']
			],
			MemberOperationRecord.getKeys(),
			[ ['isPaging', true]],
			GRID_PADDING_LIMIT_20,
			'',
			mcus_mo_tbar
		);
		mcus_grid.region = "center";
		mcus_grid.frame = false;
		mcus_grid.border = false;
		mcus_grid.on('render', function(thiz){
			mcus_search_dateCombo.setValue(1);
			mcus_search_dateCombo.fireEvent('select', mcus_search_dateCombo, null, 1);
		});
		
	
		mcus_grid.getStore().on('load', function(store, records, options){
			if(store.getCount() > 0){
				var sumRow = mcus_grid.getView().getRow(store.getCount() - 1);	
				sumRow.style.backgroundColor = '#EEEEEE';			
				for(var i = 0; i < mcus_grid.getColumnModel().getColumnCount(); i++){
					var sumCell = mcus_grid.getView().getCell(store.getCount() - 1, i);
					sumCell.style.fontSize = '15px';
					sumCell.style.fontWeight = 'bold';
					sumCell.style.color = 'green';
				}
				mcus_grid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
				mcus_grid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
				mcus_grid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
				mcus_grid.getView().getCell(store.getCount()-1, 4).innerHTML = '--';
				mcus_grid.getView().getCell(store.getCount()-1, 5).innerHTML = '--';
				mcus_grid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
				mcus_grid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
				mcus_grid.getView().getCell(store.getCount()-1, 9).innerHTML = '--';
				mcus_grid.getView().getCell(store.getCount()-1, 10).innerHTML = '--';
				mcus_grid.getView().getCell(store.getCount()-1, 11).innerHTML = '--';
			}
		});
	}

	function mcus_searchMemberOperation(){
		var onDuty = '', offDuty = '';
		onDuty = Ext.util.Format.date(mcus_search_onDuty.getValue(), 'Y-m-d 00:00:00');
		offDuty = Ext.util.Format.date(mcus_search_offDuty.getValue(), 'Y-m-d 23:59:59');
		
		var memberType = mcus_search_memberType.getRawValue() != '' ? mcus_search_memberType.getValue() : '';
		
		var gs = mcus_grid.getStore();
		gs.baseParams['dataSource'] = 'history';
		gs.baseParams['memberType'] = memberType > 0 ? memberType : '';
		gs.baseParams['fuzzy'] = mcus_search_memberName.getValue();
		//操作大类为消费
		gs.baseParams['operateType'] = 1;
		//收款方式
		gs.baseParams['payType'] = Ext.getCmp('memberConsume_comboPayType').getValue();
		gs.baseParams['onDuty'] = onDuty;
		gs.baseParams['offDuty'] = offDuty;
		gs.baseParams['total'] = true;
		gs.baseParams['branchId'] = Ext.getCmp('branch_combo_memberConsume').getValue();
		gs.load({
			params : {
				start : 0,
				limit : GRID_PADDING_LIMIT_20
			}
		});
		//每日充值统计
		mcus_showChart();
	}
	
	function mcus_changeChartWidth(w,h){
		if(mcus_highChart != undefined){
			mcus_highChart.setSize(w, h);
		}
		
	}


	
	mcus_southPanel = new Ext.Panel({
		contentEl : 'memberConsumeChart',
		region : 'south'
	});
	
	mcus_initBusinessReceipsGrid({data : null});
	
	new Ext.Panel({
		renderTo : 'divMemberConsumeStatistics',//渲染到
		id : 'businessReceiptStatisticsPanel',
		//solve不跟随窗口的变化而变化
		width : parseInt(Ext.getDom('divMemberConsumeStatistics').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divMemberConsumeStatistics').parentElement.style.height.replace(/px/g,'')),
		layout:'border',
		frame : true, //边框
		items : [mcus_grid,mcus_southPanel]
	});
	
	mcus_PanelHeight = mcus_grid.getHeight();
	
	var mcus_rz = new Ext.Resizable(mcus_grid.getEl(), {
        wrap: true, //在构造Resizable时自动在制定的id的外边包裹一层div
        minHeight:100, //限制改变的最小的高度
        pinned:false, //控制可拖动区域的显示状态，false是鼠标悬停在拖拉区域上才出现
        handles: 's',//设置拖拉的方向（n,s,e,w,all...）
        listeners : {
        	resize : function(thiz, w, h, e){
        		mcus_panelDrag = true;
        	}
        }
    });
    mcus_rz.on('resize', mcus_grid.syncSize, mcus_grid);//注册事件(作用:将调好的大小传个scope执行)
	
	mcus_grid.on('bodyresize', function(e, w, h){
		var chartHeight;
		if(h < mcus_PanelHeight){
			chartHeight = 250 + (mcus_PanelHeight - h);
		}else{
			chartHeight = 250 + (h - mcus_PanelHeight);
		}
		mcus_changeChartWidth(w,chartHeight);
		
		if(mcus_southPanel.getEl()){
			mcus_southPanel.getEl().setTop((h+55)) ;
		}
		
		if(mcus_panelDrag){
			mcus_southPanel.setHeight(chartHeight);
		}
		
		mcus_grid.getEl().parent().setWidth(w);
		mcus_grid.doLayout();
		
	});
	
});

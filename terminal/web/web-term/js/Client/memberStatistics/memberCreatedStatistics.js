
Ext.onReady(function(){
	
	var mcrs_grid, mcrs_search_memberType
		,mcrs_search_onDuty, mcrs_search_offDuty, mcrs_search_memberName, mcrs_search_dateCombo;
	var mcrs_modal = true;
	
	var mcrs_highChart, mcrs_PanelHeight = 0, mcrs_panelDrag = false;
	
	var mcrs_southPanel;
	
	function mcrs_showChart(){
		var dateBegin = Ext.util.Format.date(mcrs_search_onDuty.getValue(), 'Y-m-d');
		var dateEnd = Ext.util.Format.date(mcrs_search_offDuty.getValue(), 'Y-m-d');
		
		var chartData;
		$.ajax({
			url : '../../QueryMemberStatistics.do',
			type : 'post',
			dataType : 'json',
			data : {
				dataSource :'createdStatistics',
				dateBegin : dateBegin,
				dateEnd : dateEnd + " 23:59:59",
				branchId :Ext.getCmp('branch_combo_memberCreate').getValue()
			},
			async : false,
			success : function(data){
				chartData = eval('(' + data.other.businessChart + ')');
			},
			error : function(xhr){
			}
		});
		
		
		mcrs_highChart = new Highcharts.Chart({
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
	        	renderTo: 'memberCreatedChart'
	    	}, 
	        title: {
	            text: '<b>会员开卡走势图（'+ dateBegin +'至'+ dateEnd +'）</b>'
	        },
	        labels: {
	        	items : [{
	        		html : '<b>日均开卡数:' + chartData.avgCount + ' 个</b>',
		        	style : {left :/*($('#memberCreatedChart').width()*0.80)*/'0px', top: '0px'}
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
	                text: '数量 (个)'
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


	function mcrs_initBusinessReceipsGrid(c){
		
		mcrs_search_memberType = new Ext.form.ComboBox({
			id : 'mcrs_search_memberType',
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
					mcrs_searchMemberOperation();
				}
			}
		});
		mcrs_search_onDuty = new Ext.form.DateField({
			xtype : 'datefield',
			width : 100,
			format : 'Y-m-d',
			maxValue : new Date(),
			hideParent : true,
			hidden : mcrs_modal ? false : true,
			readOnly : false,
			allowBlank : false
		});
		mcrs_search_offDuty = new Ext.form.DateField({
			xtype : 'datefield',
			width : 100,
			format : 'Y-m-d',
			maxValue : new Date(),
			hideParent : true,
			hidden : mcrs_modal ? false : true,
			readOnly : false,
			allowBlank : false
		});
		mcrs_search_dateCombo = Ext.ux.createDateCombo({
			beginDate : mcrs_search_onDuty,
			endDate : mcrs_search_offDuty,
			callback : function(){
				mcrs_searchMemberOperation();
			}
		});
		mcrs_search_memberName = new Ext.form.TextField({
			xtype : 'textfield',
			width : 100
			
		});
		
		//门店选择
		var branch_combo_memberCreate = new Ext.form.ComboBox({
			id : 'branch_combo_memberCreate',
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
								data.push([null, '全部'], [jr.root[0]['id'], jr.root[0]['name'] + '(集团)']);
								
								for(var i = 0; i < jr.root[0].branches.length; i++){
									data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
								}
							}
							
							thiz.store.loadData(data);
							thiz.setValue(null);
							thiz.fireEvent('select');
						}
					});
				},
				select : function(){
					mcrs_searchMemberOperation();
				}
			}
		});
		
		
		var mcrs_mo_tbar = new Ext.Toolbar({
			height : 26,
			items : [{ 
				xtype : 'tbtext', 
				text : (mcrs_modal ? '&nbsp;&nbsp;日期:&nbsp;' : ' ')
			}, mcrs_search_dateCombo, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			}, mcrs_search_onDuty, { 
				xtype : 'tbtext',
				text : (mcrs_modal ? '&nbsp;至&nbsp;' : ' ')
			}, mcrs_search_offDuty, 
			{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;会员类型:'
			}, mcrs_search_memberType, 
			{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;手机号/卡号/会员名称:'
			}, mcrs_search_memberName, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;门店选择:'
			}, branch_combo_memberCreate, '->', {
				text : '搜索',
				iconCls : 'btn_search',
				handler : function(e){
					mcrs_searchMemberOperation();
				}
			}, {
				text : '重置',
				iconCls : 'btn_refresh',
				handler : function(e){
					mcrs_search_memberType.setValue(-1);
					mcrs_search_memberName.setValue();
					branch_combo_memberCreate.setValue(null);
					mcrs_searchMemberOperation();
				}
				
			}, '-', {
					text : '导出',
					iconCls : 'icon_tb_exoprt_excel',
					handler : function(){
						var onDuty = '', offDuty = '';
						onDuty = Ext.util.Format.date(mcrs_search_onDuty.getValue(), 'Y-m-d 00:00:00');
						offDuty = Ext.util.Format.date(mcrs_search_offDuty.getValue(), 'Y-m-d 23:59:59');
						
						var memberType = mcrs_search_memberType.getRawValue() != '' ? mcrs_search_memberType.getValue() : '';
						var url = '../../{0}?memberType={1}&dataSource={2}&dateBegin={3}&dateEnd={4}&memberCardOrMobileOrName={5}&create=true&branchId={6}';
						url = String.format(
								url, 
								'ExportHistoryStatisticsToExecl.do', 
								memberType > 0 ? memberType : '', 
								'memberList',
								onDuty,
								offDuty,
								mcrs_search_memberName.getValue(),
								branch_combo_memberCreate.getValue()
							);
						window.location = url;
					}
				}]
		});
	/*	mcrs_grid = createGridPanel(
			'mcrs_grid',
			'',
			'',
			'',
			'../../QueryMemberOperation.do',
			[
				[true, false, false, true],
				['日期', 'operateDateFormat'],
				['操作类型', 'operateTypeText', 90, 'center'],
				['会员名称', 'member.name', 60],
				['实收金额', 'deltaBaseMoney', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
				['账户充额', 'deltaTotalMoney', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
				['剩余金额', 'remainingTotalMoney'],
				['充值方式', 'chargeTypeText'],
				['操作人', 'staffName', 90, 'center'],
			],
			MemberOperationRecord.getKeys(),
			[ ['isPaging', true]],
			GRID_PADDING_LIMIT_20,
			'',
			mcrs_mo_tbar
		);*/
		
		mcrs_grid = createGridPanel(
				'',
				'',
				'',
				'',
				'../../QueryMemberStatistics.do',
				[
					[true, false, false, true],
					['名称', 'name'],
					['类型', 'memberType.name'],
					['创建时间','createDateFormat'],
					['消费次数', 'consumptionAmount', null,'right', Ext.ux.txtFormat.gridDou],
					['消费总额', 'totalConsumption', null,'right', Ext.ux.txtFormat.gridDou],
					['累计积分', 'totalPoint', null, 'right', Ext.ux.txtFormat.gridDou],
					['当前积分', 'point', null, 'right', Ext.ux.txtFormat.gridDou],
					['总充值额', 'totalCharge', null, 'right'],
					['账户余额', 'totalBalance', null, 'right'],
					['手机号码', 'mobile', 125],
					['会员卡号', 'memberCard', 125]
				],
				MemberBasicRecord.getKeys(),
				[['isPaging', true],  ['dataSource', 'createdMember']],
				GRID_PADDING_LIMIT_20,
				'',
				[mcrs_mo_tbar]
			);	
		mcrs_grid.region = "center";
		mcrs_grid.frame = false;
		mcrs_grid.border = false;
		mcrs_grid.on('render', function(thiz){
			mcrs_search_dateCombo.setValue(1);
			mcrs_search_dateCombo.fireEvent('select', mcrs_search_dateCombo, null, 1);
		});
		
	
	/*	mcrs_grid.getStore().on('load', function(store, records, options){
			if(store.getCount() > 0){
				var sumRow = mcrs_grid.getView().getRow(store.getCount() - 1);	
				sumRow.style.backgroundColor = '#EEEEEE';			
				for(var i = 0; i < mcrs_grid.getColumnModel().getColumnCount(); i++){
					var sumCell = mcrs_grid.getView().getCell(store.getCount() - 1, i);
					sumCell.style.fontSize = '15px';
					sumCell.style.fontWeight = 'bold';
					sumCell.style.color = 'green';
				}
				mcrs_grid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
				mcrs_grid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
				mcrs_grid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
				mcrs_grid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
				mcrs_grid.getView().getCell(store.getCount()-1, 7).innerHTML = '--';
				mcrs_grid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
			}
		});*/
	}

	function mcrs_searchMemberOperation(){
		var onDuty = '', offDuty = '';
		onDuty = Ext.util.Format.date(mcrs_search_onDuty.getValue(), 'Y-m-d 00:00:00');
		offDuty = Ext.util.Format.date(mcrs_search_offDuty.getValue(), 'Y-m-d 23:59:59');
		
		var memberType = mcrs_search_memberType.getRawValue() != '' ? mcrs_search_memberType.getValue() : '';
		
		var gs = mcrs_grid.getStore();
		gs.baseParams['memberType'] = memberType > 0 ? memberType : '';
		gs.baseParams['memberCardOrMobileOrName'] = mcrs_search_memberName.getValue();
		gs.baseParams['dateBegin'] = onDuty;
		gs.baseParams['dateEnd'] = offDuty;
		gs.baseParams['branchId'] = Ext.getCmp('branch_combo_memberCreate').getValue();
		gs.load({
			params : {
				start : 0,
				limit : GRID_PADDING_LIMIT_20
			}
		});
		//每日充值统计
		mcrs_showChart();
	}

	function mcrs_changeChartWidth(w,h){
		if(mcrs_highChart != undefined){
			mcrs_highChart.setSize(w, h);
		}
	}
	
	mcrs_southPanel = new Ext.Panel({
		contentEl : 'memberCreatedChart',
		region : 'south'
	});
	
	mcrs_initBusinessReceipsGrid({data : null});
	
	new Ext.Panel({
		renderTo : 'divMemberCreatedStatistics',//渲染到
		id : 'businessReceiptStatisticsPanel',
		//solve不跟随窗口的变化而变化
		width : parseInt(Ext.getDom('divMemberCreatedStatistics').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divMemberCreatedStatistics').parentElement.style.height.replace(/px/g,'')),
		layout:'border',
		frame : true, //边框
		items : [mcrs_grid,mcrs_southPanel]
	});
	
	mcrs_PanelHeight = mcrs_grid.getHeight();
	
	var mcrs_rz = new Ext.Resizable(mcrs_grid.getEl(), {
        wrap: true, //在构造Resizable时自动在制定的id的外边包裹一层div
        minHeight:100, //限制改变的最小的高度
        pinned:false, //控制可拖动区域的显示状态，false是鼠标悬停在拖拉区域上才出现
        handles: 's',//设置拖拉的方向（n,s,e,w,all...）
        listeners : {
        	resize : function(thiz, w, h, e){
        		mcrs_panelDrag = true;
        	}
        }
    });
    mcrs_rz.on('resize', mcrs_grid.syncSize, mcrs_grid);//注册事件(作用:将调好的大小传个scope执行)
	
	mcrs_grid.on('bodyresize', function(e, w, h){
		var chartHeight;
		if(h < mcrs_PanelHeight){
			chartHeight = 250 + (mcrs_PanelHeight - h);
		}else{
			chartHeight = 250 + (h - mcrs_PanelHeight);
		}
		mcrs_changeChartWidth(w,chartHeight);
		
		if(mcrs_southPanel.getEl()){
			mcrs_southPanel.getEl().setTop((h+55)) ;
		}
		
		if(mcrs_panelDrag){
			mcrs_southPanel.setHeight(chartHeight);
		}
		
		mcrs_grid.getEl().parent().setWidth(w);
		mcrs_grid.doLayout();
		
	});
	
});

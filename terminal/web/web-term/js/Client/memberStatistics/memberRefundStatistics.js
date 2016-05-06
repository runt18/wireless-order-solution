

Ext.onReady(function(){
	var mrs_grid, mrs_search_memberType
		,mrs_search_onDuty, mrs_search_offDuty, mrs_search_memberName, mrs_search_dateCombo;
	var mrs_modal = true;
	
	var mrs_highChart, mrs_PanelHeight = 0, mrs_panelDrag = false;
	
	var mrs_southPanel;

	function mrs_showChart(){
		var dateBegin = Ext.util.Format.date(mrs_search_onDuty.getValue(), 'Y-m-d');
		var dateEnd = Ext.util.Format.date(mrs_search_offDuty.getValue(), 'Y-m-d');
		
		var chartData;
		$.ajax({
			url : '../../QueryMemberStatistics.do',
			type : 'post',
			dataType : 'json',
			data : {
				dataSource :'refundStatistics',
				dateBegin : dateBegin,
				dateEnd : dateEnd + " 23:59:59",
				branchId : Ext.getCmp('branch_combo_memberRefund').getValue()
			},
			async : false,
			success : function(data){
				chartData = eval('(' + data.other.businessChart + ')');
			},
			error : function(xhr){
			}
		});
		
		
		mrs_highChart = new Highcharts.Chart({
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
	        	renderTo: 'memberRefundChart'
	    	}, 
	        title: {
	            text: '<b>会员取款走势图（'+ dateBegin +'至'+ dateEnd +'）</b>'
	        },
	        labels: {
	        	items : [{
	        		html : '<b>日均取款额:' + chartData.avgMoney + ' 元</b><br><b>日均取款次数:' + chartData.avgCount + ' 次</b>',
		        	style : {left :/*($('#memberRefundChart').width()*0.80)*/'0px', top: '0px'}
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


	function mrs_initBusinessReceipsGrid(c){
		
		mrs_search_memberType = new Ext.form.ComboBox({
			id : 'mrs_search_memberType',
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
					mrs_searchMemberOperation();
				}
			}
		});
		mrs_search_onDuty = new Ext.form.DateField({
			id : 'beginDate_combo_memberRefund',
			xtype : 'datefield',
			width : 100,
			format : 'Y-m-d',
			maxValue : new Date(),
			hideParent : true,
			hidden : mrs_modal ? false : true,
			readOnly : false,
			allowBlank : false
		});
		mrs_search_offDuty = new Ext.form.DateField({
			id : 'endDate_combo_memberRefund',
			xtype : 'datefield',
			width : 100,
			format : 'Y-m-d',
			maxValue : new Date(),
			hideParent : true,
			hidden : mrs_modal ? false : true,
			readOnly : false,
			allowBlank : false
		});
		mrs_search_dateCombo = Ext.ux.createDateCombo({
			beginDate : mrs_search_onDuty,
			endDate : mrs_search_offDuty,
			callback : function(){
				mrs_searchMemberOperation();
			}
		});
		mrs_search_memberName = new Ext.form.TextField({
			xtype : 'textfield',
			width : 100
			
		});
		
		//门店选择
		var branch_combo_memberRefund = new Ext.form.ComboBox({
			id : 'branch_combo_memberRefund',
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
								data.push([null, '全部'], [jr.root[0]['id'], jr.root[0]['name'] + '(集团)']);
								
								for(var i = 0; i < jr.root[0].branches.length; i++){
									data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
								}
							}
							
							
							thiz.store.loadData(data);
							
							if(jr.root[0].typeVal != '2'){
								thiz.setValue(jr.root[0].id);
							}else{
								thiz.setValue(null);
							}
						}
					});
				},
				select : function(isJump, record, index, isJump){
					if(branch_combo_memberRefund.getValue() == null){
						Ext.getCmp('memberRefund_comboPayType').disable();
					}else{
						Ext.getCmp('memberRefund_comboPayType').enable();
						//加载收款方式
						var payType = [[-1, '全部']];
						Ext.Ajax.request({
							url : '../../OperatePayType.do',
							params : {
								dataSource : 'getByCond',
								branchId : branch_combo_memberRefund.getValue()
							},
							success : function(res, opt){
								var jr = Ext.decode(res.responseText);
								
								jr.root.unshift({id:-1, name:'全部'});
								Ext.getCmp('memberRefund_comboPayType').getStore().loadData(jr.root);
							}
						});
					}
					Ext.getCmp('memberRefund_comboPayType').setValue(-1);
					
					if(!isJump){
						Ext.getCmp('memberRefundSearchBtn').handler();
					}
					
				}
			}
		});
		
		var mrs_mo_tbar = new Ext.Toolbar({
			height : 26,
			items : [{ 
				xtype : 'tbtext', 
				text : (mrs_modal ? '&nbsp;&nbsp;日期:&nbsp;' : ' ')
			}, mrs_search_dateCombo, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			}, mrs_search_onDuty, { 
				xtype : 'tbtext',
				text : (mrs_modal ? '&nbsp;至&nbsp;' : ' ')
			}, mrs_search_offDuty, 
			{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;会员类型:'
			}, mrs_search_memberType, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;收款方式:'			
			},{
				xtype : 'combo',
				forceSelection : true,
				width : 80,
				id : 'memberRefund_comboPayType',
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
							url : '../../OperatePayType.do?',
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
						mrs_searchMemberOperation();
					}
				}				
			}, 
			{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;手机号/卡号/会员名称:'
			}, mrs_search_memberName, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;门店选择:'
			}, branch_combo_memberRefund, '->', {
				text : '搜索',
				id : 'memberRefundSearchBtn',
				iconCls : 'btn_search',
				handler : function(e){
					mrs_searchMemberOperation();
				}
			}, {
				text : '重置',
				iconCls : 'btn_refresh',
				handler : function(e){
					mrs_search_memberType.setValue(-1);
					mrs_search_memberName.setValue();
					branch_combo_memberRefund.setValue(null);
					mrs_searchMemberOperation();
				}
				
			}, '-', {
					text : '导出',
					iconCls : 'icon_tb_exoprt_excel',
					handler : function(){
						var onDuty = '', offDuty = '';
						onDuty = Ext.util.Format.date(mrs_search_onDuty.getValue(), 'Y-m-d 00:00:00');
						offDuty = Ext.util.Format.date(mrs_search_offDuty.getValue(), 'Y-m-d 23:59:59');
						
						var memberType = mrs_search_memberType.getRawValue() != '' ? mrs_search_memberType.getValue() : '';
						var url = '../../{0}?memberType={1}&dataSource={2}&onDuty={3}&offDuty={4}&fuzzy={5}&dataSources={6}&detailOperate={7}&payType={8}&isRefund=true&branchId={9}';
						url = String.format(
								url, 
								'ExportHistoryStatisticsToExecl.do', 
								memberType > 0 ? memberType : '', 
								'rechargeDetail',
								onDuty,
								offDuty,
								mrs_search_memberName.getValue(),
								'history',
								6,
								Ext.getCmp('memberRefund_comboPayType').getValue(),
								Ext.getCmp('branch_combo_memberRefund').getValue()
							);
						window.location = url;
					}
				}]
		});
		mrs_grid = createGridPanel(
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
				['操作门店', 'branchName', 60],
				['手机号码', 'member.mobile', 60],
				['会员卡号', 'member.memberCard', 60],
				['实退金额', 'deltaBaseMoney', 60, 'right', Ext.ux.txtFormat.gridDou],
				['账户退额', 'deltaTotalMoney', 60, 'right', Ext.ux.txtFormat.gridDou],
				['剩余金额', 'remainingTotalMoney', null, 'right', Ext.ux.txtFormat.gridDou],
				['退款方式', 'chargeTypeText'],
				['操作人', 'staffName', 90, 'center']
			],
			MemberOperationRecord.getKeys(),
			[ ['isPaging', true]],
			GRID_PADDING_LIMIT_20,
			'',
			mrs_mo_tbar
		);
		mrs_grid.region = "center";
		mrs_grid.frame = false;
		mrs_grid.border = false;
		mrs_grid.on('render', function(thiz){
			mrs_search_dateCombo.setValue(1);
			mrs_search_dateCombo.fireEvent('select', mrs_search_dateCombo, null, 1);		
		});
		
	
		mrs_grid.getStore().on('load', function(store, records, options){
			if(store.getCount() > 0){
				var sumRow = mrs_grid.getView().getRow(store.getCount() - 1);	
				sumRow.style.backgroundColor = '#EEEEEE';			
				for(var i = 0; i < mrs_grid.getColumnModel().getColumnCount(); i++){
					var sumCell = mrs_grid.getView().getCell(store.getCount() - 1, i);
					sumCell.style.fontSize = '15px';
					sumCell.style.fontWeight = 'bold';
					sumCell.style.color = 'green';
				}
				mrs_grid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
				mrs_grid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
				mrs_grid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
				mrs_grid.getView().getCell(store.getCount()-1, 4).innerHTML = '--';
				mrs_grid.getView().getCell(store.getCount()-1, 5).innerHTML = '--';
				mrs_grid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
				mrs_grid.getView().getCell(store.getCount()-1, 9).innerHTML = '--';
				mrs_grid.getView().getCell(store.getCount()-1, 10).innerHTML = '--';
			}
		});
	}

	function mrs_searchMemberOperation(){
		var onDuty = '', offDuty = '';
		onDuty = Ext.util.Format.date(mrs_search_onDuty.getValue(), 'Y-m-d 00:00:00');
		offDuty = Ext.util.Format.date(mrs_search_offDuty.getValue(), 'Y-m-d 23:59:59');
		
		var memberType = mrs_search_memberType.getRawValue() != '' ? mrs_search_memberType.getValue() : '';
		
		var gs = mrs_grid.getStore();
		gs.baseParams['dataSource'] = 'history';
		gs.baseParams['memberType'] = memberType > 0 ? memberType : '';
		gs.baseParams['fuzzy'] = mrs_search_memberName.getValue();
		//操作小类为取款
		gs.baseParams['detailOperate'] = 6;
		//收款方式
		gs.baseParams['chargeType'] = Ext.getCmp('memberRefund_comboPayType').getValue();
		gs.baseParams['onDuty'] = onDuty;
		gs.baseParams['offDuty'] = offDuty;
		gs.baseParams['total'] = true;
		gs.baseParams['branchId'] = Ext.getCmp('branch_combo_memberRefund').getValue();
		
		gs.load({
			params : {
				start : 0,
				limit : GRID_PADDING_LIMIT_20
			}
		});
		//每日充值统计
		mrs_showChart();
	}

	function mrs_changeChartWidth(w,h){
		if(mrs_highChart != undefined){
			mrs_highChart.setSize(w, h);
		}
		
	}
	
	mrs_southPanel = new Ext.Panel({
		contentEl : 'memberRefundChart',
		region : 'south'
	});
	
	mrs_initBusinessReceipsGrid({data : null});
	
	new Ext.Panel({
		renderTo : 'divMemberRefundStatistics',//渲染到
		id : 'businessReceiptStatisticsPanel',
		//solve不跟随窗口的变化而变化
		width : parseInt(Ext.getDom('divMemberRefundStatistics').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divMemberRefundStatistics').parentElement.style.height.replace(/px/g,'')),
		layout:'border',
		frame : true, //边框
		items : [mrs_grid,mrs_southPanel]
	});
	
	mrs_PanelHeight = mrs_grid.getHeight();
	
	var mrs_rz = new Ext.Resizable(mrs_grid.getEl(), {
        wrap: true, //在构造Resizable时自动在制定的id的外边包裹一层div
        minHeight:100, //限制改变的最小的高度
        pinned:false, //控制可拖动区域的显示状态，false是鼠标悬停在拖拉区域上才出现
        handles: 's',//设置拖拉的方向（n,s,e,w,all...）
        listeners : {
        	resize : function(thiz, w, h, e){
        		mrs_panelDrag = true;
        	}
        }
    });
    mrs_rz.on('resize', mrs_grid.syncSize, mrs_grid);//注册事件(作用:将调好的大小传个scope执行)
	
	mrs_grid.on('bodyresize', function(e, w, h){
		var chartHeight;
		if(h < mrs_PanelHeight){
			chartHeight = 250 + (mrs_PanelHeight - h);
		}else{
			chartHeight = 250 + (h - mrs_PanelHeight);
		}
		mrs_changeChartWidth(w,chartHeight);
		
		if(mrs_southPanel.getEl()){
			mrs_southPanel.getEl().setTop((h+55)) ;
		}
		
		if(mrs_panelDrag){
			mrs_southPanel.setHeight(chartHeight);
		}
		
		mrs_grid.getEl().parent().setWidth(w);
		mrs_grid.doLayout();
		
	});
	
	setTimeout(function(){
		Ext.getCmp('memberRefundSearchBtn').handler();
	}, 200);
});

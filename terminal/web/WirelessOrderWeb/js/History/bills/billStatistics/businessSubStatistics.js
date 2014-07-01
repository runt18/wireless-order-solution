

var businessSubGeneralPanel;

function businessSub_initRegionCombo(statistic){
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

function businessSub_changeChartWidth(w, h){
	if(businessSub_highChart){
		businessSub_highChart.setSize(w, h);
	}
}

function businessSub_showChart(c){
	var hourBegin = Ext.getCmp('businessSub_txtBusinessHourBegin').getEl().dom.textContent;
	var hourEnd = Ext.getCmp('businessSub_txtBusinessHourEnd').getEl().dom.textContent;
	
	var chartData = eval('(' + c.jdata.other.businessChart + ')');
	businessSub_highChart = new Highcharts.Chart({
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
        	renderTo: 'divBusinessSubStatisticsDetailChart'
    	}, 
        title: {
            text: '<b>营业走势图（'+c.dateBegin+ '至' +c.dateEnd+'）'+hourBegin+ ' - ' + hourEnd + businessSub_titleRegionName + '</b>'
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
	
	if(businessSub_chartPanel && businessSub_chartPanel.isVisible()){
		businessSub_chartPanel.show();
	}
}
var businessSub_dateCombo;
var businessSub_trModel = '<tr>'
			+ '<th>{0}</th>'
			+ '<td class="text_right">{1}</td>'
			+ '<td class="text_right">{2}</td>'
			+ '<td class="text_right">{3}</td>'
			+ '</tr>';
var businessSub_titleRegionName = '', businessSub_panelDrag = false;
var businessSub_chartPanel, businessSub_generalPanelHeight, businessSub_chartPanelHeight, businessSub_highChart;
Ext.onReady(function(){
	var businessSub_beginDate = new Ext.form.DateField({
		xtype : 'datefield',	
		id : 'businessSub_dateSearchDateBegin',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	var businessSub_endDate = new Ext.form.DateField({
		xtype : 'datefield',
		id : 'businessSub_dateSearchDateEnd',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	businessSub_dateCombo = Ext.ux.createDateCombo({
		width : 90,
		beginDate : businessSub_beginDate,
		endDate : businessSub_endDate,
		callback : function(){
			Ext.getCmp('businessSub_btnSearch').handler();
		}
	});
	
	var bssifLoadMarsk = new Ext.LoadMask(document.body, {
	    msg  : '数据统计中，请稍候......',
	    disabled : false
	});	
	
	var businessSubStatisticsTbarItem = [
		{xtype : 'tbtext', text : '区域:'},
		businessSub_initRegionCombo('businessSub_'),
		'->', {
		text : '搜索',
		id : 'businessSub_btnSearch',
		iconCls : 'btn_search',
		handler : function(){
			var dateBegin = Ext.getCmp('businessSub_dateSearchDateBegin');
			var dateEnd = Ext.getCmp('businessSub_dateSearchDateEnd');
			
			var region = Ext.getCmp('businessSub_comboRegion');
			
			if(!dateBegin.isValid() || !dateEnd.isValid()){
				return;
			}
			var data = Ext.ux.statistic_oBusinessHourData({type : 'get', statistic : 'businessSub_'}).data;
			
			if(region.getValue() != -1){
				businessSub_titleRegionName = " 区域 : " + region.getEl().dom.value;
			}
			
			bssifLoadMarsk.show();
			Ext.Ajax.request({
				url : '../../BusinessStatistics.do',
				params : {
					onDuty : dateBegin.getValue().format('Y-m-d 00:00:00'),
					offDuty : dateEnd.getValue().format('Y-m-d 23:59:59'),	
					opening : data.opening,
					ending : data.ending,
					chart : true,
					region : region.getValue(),
					dutyRange : 'range',
					dataSource : 'history'
				},
				success : function(res, opt){
					bssifLoadMarsk.hide();	
					var jr = Ext.decode(res.responseText);
					
					var businessSub_business = jr.other.business;
					
					var deptStat = businessSub_business.deptStat;
					
					
					var trContent='<tr>'
					  + '<th class="table_title text_center">部门汇总</th>'
					  + '<th class="table_title text_center">折扣总额</th>'
					  + '<th class="table_title text_center">赠送总额</th>'
					  + '<th class="table_title text_center">应收总额</th>'
					  + '</tr>';
					if(deptStat.length > 0){
						for(var i = 0; i < deptStat.length; i++){
							var temp = deptStat[i];
							trContent += (String.format(businessSub_trModel, 
									temp.dept.name, 
									temp.discountPrice.toFixed(2), 
									temp.giftPrice.toFixed(2), 
									temp.income.toFixed(2)
								)
							);
						}						
					}
						
				
					
					Ext.getDom('bssiCashAmount').innerHTML = businessSub_business.cashAmount;
					Ext.getDom('bssiCashIncome').innerHTML = businessSub_business.cashIncome.toFixed(2);
					Ext.getDom('bssiCashIncome2').innerHTML = businessSub_business.cashIncome2.toFixed(2);
					
					Ext.getDom('bssiCreditCardAmount').innerHTML = businessSub_business.creditCardAmount;
					Ext.getDom('bssiCreditCardIncome').innerHTML = businessSub_business.creditCardIncome.toFixed(2);
					Ext.getDom('bssiCreditCardIncome2').innerHTML = businessSub_business.creditCardIncome2.toFixed(2);
					
					Ext.getDom('bssiMemeberCardAmount').innerHTML = businessSub_business.memberAmount;
					Ext.getDom('bssiMemeberCardIncome').innerHTML = businessSub_business.memberIncome.toFixed(2);
					Ext.getDom('bssiMemeberCardIncome2').innerHTML = businessSub_business.memberActual.toFixed(2);
					
					Ext.getDom('bssiSignAmount').innerHTML = businessSub_business.signAmount;
					Ext.getDom('bssiSignIncome').innerHTML = businessSub_business.signIncome.toFixed(2);
					Ext.getDom('bssiSignIncome2').innerHTML = businessSub_business.signIncome2.toFixed(2);
					
					Ext.getDom('bssiHangAmount').innerHTML = businessSub_business.hangAmount;
					Ext.getDom('bssiHangIncome').innerHTML = businessSub_business.hangIncome.toFixed(2);
					Ext.getDom('bssiHangIncome2').innerHTML = businessSub_business.hangIncome2.toFixed(2);
					//
					Ext.getDom('bssiSumAmount').innerHTML = businessSub_business.orderAmount;
					Ext.getDom('bssiSumIncome').innerHTML = businessSub_business.totalIncome.toFixed(2);
					Ext.getDom('bssiSumIncome2').innerHTML = businessSub_business.totalActual.toFixed(2);
					
					Ext.getDom('bssiEraseAmount').innerHTML = businessSub_business.eraseAmount;
					Ext.getDom('bssiEraseIncome').innerHTML = businessSub_business.eraseIncome.toFixed(2);
					
					Ext.getDom('bssiDiscountAmount').innerHTML = businessSub_business.discountAmount;
					Ext.getDom('bssiDiscountIncome').innerHTML = businessSub_business.discountIncome.toFixed(2);
					
					Ext.getDom('bssiGiftAmount').innerHTML = businessSub_business.giftAmount;
					Ext.getDom('bssiGiftIncome').innerHTML = businessSub_business.giftIncome.toFixed(2);
					
					Ext.getDom('bssiCouponAmount').innerHTML = businessSub_business.couponAmount;
					Ext.getDom('bssiCouponIncome').innerHTML = businessSub_business.couponIncome.toFixed(2);
					
					Ext.getDom('bssiCancelAmount').innerHTML = businessSub_business.cancelAmount;
					Ext.getDom('bssiCancelIncome').innerHTML = businessSub_business.cancelIncome.toFixed(2);
					
					Ext.getDom('bssiPaidAmount').innerHTML = businessSub_business.paidAmount;
					Ext.getDom('bssiPaidIncome').innerHTML = businessSub_business.paidIncome.toFixed(2);
					
					Ext.getDom('bssiServiceAmount').innerHTML = businessSub_business.serviceAmount;
					Ext.getDom('bssiServiceIncome').innerHTML = businessSub_business.serviceIncome.toFixed(2);	
					
					Ext.getDom('bussiMemberChargeByCash').innerHTML = businessSub_business.memberChargeByCash.toFixed(2);
					Ext.getDom('bussiMemberChargeByCard').innerHTML = businessSub_business.memberChargeByCard.toFixed(2);
					Ext.getDom('bussiMemberAccountCharge').innerHTML = businessSub_business.memberAccountCharge.toFixed(2);
					
					Ext.getDom('bussiMemberRefund').innerHTML = businessSub_business.memberRefund.toFixed();
					Ext.getDom('bussiMemberAccountRefund').innerHTML = businessSub_business.memberAccountRefund.toFixed(2);
					
					Ext.getDom('businessStatisticsDeptGeneral').innerHTML = trContent;
					
					
					businessSub_showChart({
						jdata : jr,
						dateBegin : dateBegin.getValue().format('Y-m-d'), 
						dateEnd :dateEnd.getValue().format('Y-m-d')
					});
				},
				failure : function(res, opt){
					Ext.ux.showMsg(Ext.decode(res.responseText));
					bssifLoadMarsk.hide();				
				}
			
			});
			

			
		}
	}, '-', {
		text : '导出',
		id : 'total_exoprt_excel',
		iconCls : 'icon_tb_exoprt_excel',
		handler : function(){
			var paramsOnDuty = '', paramsOffDuty;
			if(queryPattern == 1){
				if(!beginDate.isValid() || !endDate.isValid()){
					return;
				}
				paramsOnDuty = beginDate.getValue().format('Y-m-d 00:00:00');
				paramsOffDuty = endDate.getValue().format('Y-m-d 23:59:59');
			}else if(queryPattern == 2){
				paramsOnDuty = onDuty.format('Y-m-d H:i:s');
				paramsOffDuty = offDuty.format('Y-m-d H:i:s');
			}else{
				return;
			}
			var url = '../../{0}?pin={1}&restaurantID={2}&dataSource={3}&onDuty={4}&offDuty={5}&queryPattern={6}&dataType={7}&isCookie=true';
			url = String.format(
					url, 
					'ExportHistoryStatisticsToExecl.do', 
					-10, 
					restaurantID, 
					'business',
					paramsOnDuty,
					paramsOffDuty,
					queryPattern,
					dataSource
				);
			window.location = url;
		}
	}];
	
	var businessSubStatisticsTbar = new Ext.Toolbar({
		height : 26,
		items : [Ext.ux.initTimeBar({beginDate:businessSub_beginDate, endDate:businessSub_endDate,dateCombo:businessSub_dateCombo, tbarType : 1, statistic : 'businessSub_'}).concat(businessSubStatisticsTbarItem)]
	});	
	
	businessSubGeneralPanel = new Ext.Panel({
		id : 'businessStatisticsDIVPanel',
		border : false,
		region : 'center',
		layout : 'border',
		tbar : businessSubStatisticsTbar,
		autoScroll : true,
		items : [{
			region : 'west',
			frame : true,
			width : '50%',
			contentEl : 'businessStatisticsSummary'
		},{
			region : 'center',
			frame : true,
			autoScroll : true,
			id : 'businessStatisticsSummaryInformationCenterPanel',
			width : '50%',
			contentEl : 'businessStatisticsTotals'
		}],
		listeners : {
			bodyresize : function(e, w, h){
				if(typeof businessSub_generalPanelHeight != 'undefined'){
					var chartHeight = businessSub_chartPanelHeight + (businessSub_generalPanelHeight - h);
					
					businessSub_chartPanel.getEl().setTop((h+30)) ;
					
					businessSub_changeChartWidth(w,chartHeight - 30);
					
					if(businessSub_panelDrag){
						businessSub_chartPanel.setHeight(chartHeight);
					}
					businessSub_chartPanel.doLayout();					
				}
			}
		}		
	});
	
	businessSub_chartPanel = new Ext.Panel({
		region : 'south',
		frame : true,
		contentEl : 'divBusinessSubStatisticsDetailChart',
		listeners : {
			show : function(thiz){
				//thiz.getEl(): 刚打开页面时thiz.getWidth无效
				if(businessSub_highChart && typeof thiz.getEl() != 'undefined'){
					businessSub_highChart.setSize(thiz.getWidth(), businessSub_panelDrag ? businessSub_chartPanel.getHeight() - 30 : businessSub_chartPanel.getHeight());
				}				
			}
		}
	});
	
	new Ext.Panel({
		renderTo : 'divBusinessSubStatistics',//渲染到
		id : 'businessSubStatisticsPanel',
		//solve不跟随窗口的变化而变化
		width : parseInt(Ext.getDom('divBusinessSubStatistics').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divBusinessSubStatistics').parentElement.style.height.replace(/px/g,'')),
		layout:'border',
		frame : true, //边框
		//子集
		items : [businessSubGeneralPanel, businessSub_chartPanel]		
	});
	
	var businessSub_rz = new Ext.Resizable(businessSubGeneralPanel.getEl(), {
        wrap: true, //在构造Resizable时自动在制定的id的外边包裹一层div
        minHeight:100, //限制改变的最小的高度
        pinned:false, //控制可拖动区域的显示状态，false是鼠标悬停在拖拉区域上才出现
        handles: 's',//设置拖拉的方向（n,s,e,w,all...）
        listeners : {
        	resize : function(thiz, w, h, e){
        		businessSub_panelDrag = true;
        	}
        }
    });
    businessSub_rz.on('resize', businessSubGeneralPanel.syncSize, businessSubGeneralPanel);//注册事件(作用:将调好的大小传个scope执行)	
    
	
	businessSub_generalPanelHeight = businessSubGeneralPanel.getHeight();
	
	businessSub_chartPanelHeight = businessSub_chartPanel.getHeight();	
	
	businessSub_dateCombo.setValue(1);
	businessSub_dateCombo.fireEvent('select', businessSub_dateCombo, null, 1);	
});
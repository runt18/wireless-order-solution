var pageWidth = window.screen.width;

initChartData = function(c){
	c = c || {};
	return {chartPriceData : {type : 'pie', name : '比例', data : []}, 
				chartAmountData : {type : 'pie', name : '比例', data : []},
				
				priceColumnChart : {xAxis : [], 
					yAxis : {name : c.priceName, data : []}
				},
		        
		        amountColumnChart : {xAxis : [], 
				yAxis : {name : c.countName, data : [],
				dataLabels: {
		            enabled: true,
		            color: 'green',
		            align: 'center',
		            style: {
		                fontSize: '13px',
		                fontFamily: 'Verdana, sans-serif',
		                fontWeight : 'bold'
		            },
		            format: '{point.y} 份'
		        }}}};
};

function newPieChart2(c){
	return new Highcharts.Chart({
        chart: {
        	renderTo : c.rt,
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false
        },
        title: {
            text: c.title
        },
        tooltip: {
        	enabled : c.tooltip ? true : false,
    	    pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
        },
        
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true,
                    color: '#000000',
                    connectorColor: '#000000',
	                format: '<b>{point.name}</b>: {point.y} '+c.unit
                }
            },            
            series: {
                cursor: 'pointer',
                point: {
                    events: {
                        click: function (event) {
                        	var point = this
                        	if(c.clickHander){
                        		c.clickHander(point);
                        	}
                        }
                    }
                }
            } 
        },
        series: [c.series],
        credits : {
        	enabled : false
        }
    })	
}

function newColumnChart2(c){
	return new Highcharts.Chart({                                           
        chart: {                                                           
            type: 'bar',
            renderTo : c.rt
        },                                                                 
        title: {                                                           
            text: c.title                   
        },                                                                 
        xAxis: {                                                           
            categories: c.xAxis,
            title: {                                                       
                text: null                                                 
            },
            labels: {
                formatter: function () {
                	if(c.dateFormat){
                    	return this.value.substring(5);
                	}else{
                		return this.value;
                	}
                }
            }                                                              
        },                                                                 
        yAxis: {                                                           
            min: 0,                                                        
            title: {                                                       
                text: '金额 (元)',                             
                align: 'high'                                              
            },                                                             
            labels: {                                                      
                overflow: 'justify'                                        
            }                                                              
        },                                                                 
        tooltip: {                   
        	enabled : false,
            valueSuffix: ' millions'                                       
        },                                                                 
        plotOptions: {                                                     
            bar: {                                                         
                dataLabels: {                                              
                    enabled: true                                          
                }
            },            
            series: {
                cursor: 'pointer',
                point: {
                    events: {
                        click: function () {
                        	var point = this;
                        	if(c.clickHander){
                        		c.clickHander(point);
                        	}
                            //alert('类型: ' + this.category + ', 金额: ' + this.y);
                        }
                    }
                }
            }                                                              
        },                                                                 
        legend: {         
        	enabled : false,
            layout: 'vertical',                                            
            align: 'right',                                                
            verticalAlign: 'top',                                          
            x: -40,                                                        
            y: 100,                                                        
            floating: true,                                                
            borderWidth: 1,                                                
            backgroundColor: '#FFFFFF',                                    
            shadow: true                                                   
        },                                                                 
        credits: {                                                         
            enabled: false                                                 
        },                                                                 
        series: [c.series],
        credits : {
        	enabled : false
        }                                                             
    })	
}

function getBusinessStatisticsData(c){
	c = c || {}
	var begin = c.begin ? c.begin : $('#beginDate').html() + ' 00:00:00';
	var end = c.end ? c.end : $('#endDate').html() + ' 23:59:59';
	
	Util.lm.show();
	
	$.ajax({
		url : '../../WXQueryBusinessStatistics.do',
		type : 'post',
		data : {
			fid : Util.mp.fid,
			onDuty:begin,
			offDuty:end,
			chart:c.chart ? true : false,
			region:-1,
			dutyRange:"range",
			dataSource:"history"			
		},
		dataType : 'json',
		success : function(rt){
			if(rt.success){
				Util.lm.hide();
				
				var orderType = rt.other.business;
				var deptStatistics = rt.other.business.deptStat;
				var receiveStatistics = rt.other.business.paymentIncomes;
				
				var receiveChartData = initChartData();
				receiveStatistics.forEach(function(e){  
				    receiveChartData.chartPriceData.data.push([e.payType, e.total]);
				}) 
				//收款
				newPieChart2({rt: 'receivePieChart', title : '收款方式比例图', unit: '元', series: receiveChartData.chartPriceData}).setSize(pageWidth-10, pageWidth-10);
				
				var deptGeneralPieChartData = initChartData();
				deptStatistics.forEach(function(e){  
				    deptGeneralPieChartData.chartPriceData.data.push([e.dept.name, e.income]);
				}) 
				//部门
				newPieChart2({rt: 'deptGeneralPieChart', title : '部门汇总比例图', unit: '元', series: deptGeneralPieChartData.chartPriceData, clickHander : function(point){
			       $.mobile.changePage("#singleReportMgr",
			        	    { transition: "fade" });
			        $('#reportName').html(point.name +"报表");
			        
			        Util.lm.show();
			        //厨房
			        $.post('../../WXQueryBusinessStatistics.do', {
			        	dataSource : 'deptSaleStatistic',
						fid : Util.mp.fid,
						dateBeg:begin,
						dateEnd:end,
						region:-1,
						dataType:1,
						queryType:2,
						deptName : point.name
			        }, function(rt){
			        	Util.lm.hide();
			        	
						var kitchenPieChartData = initChartData();
						rt.root.forEach(function(e){  
							kitchenPieChartData.chartPriceData.data.push([e.kitchen.name, e.income]);
						}) 
			        	
			        	newPieChart2({rt: 'singleReportChart', title : "厨房金额比例图", unit: "元", series: kitchenPieChartData.chartPriceData}).setSize(pageWidth-10, pageWidth-10);
					    
			        },'json');
			        
			        Util.lm.show();
			        //菜品top10
			        $.post('../../WXQueryBusinessStatistics.do', {
			        	dataSource : 'deptSaleStatistic',
						fid : Util.mp.fid,
						dateBeg:begin,
						dateEnd:end,
						region:-1,
						dataType:1,
						queryType:1,
						deptName : point.name
			        }, function(rt){
			        	Util.lm.hide();
			        	
			        	//设置搜索出来的菜品的排序依据, 按销量
			        	var searchFoodCompare = function (obj1, obj2) {
			        		return -(obj1.salesAmount - obj2.salesAmount);
			        	} 
			        	rt.root = rt.root.slice(0, 10);
			        	rt.root.sort(searchFoodCompare);
			        	
			        	var html = [];
			        	for (var i = 0; i < rt.root.length; i++) {
							html.push('<tr><td>{index}</td><td>{name}</td><td>{count}</td><td style="text-align:right;">{money}</td></tr>'.format({
								index : i+1,
								name : rt.root[i].food.name,
								money : rt.root[i].income.toFixed(2),
								count : rt.root[i].salesAmount
							}));
						}
			        	$('#table4KitchenTop10').html(html.join(''));
						
			        	//不显示选择隐藏列按钮
			        	$('.ui-table-columntoggle-btn').hide();
			        	$('#display4KitchenTop10').show();
			        },'json');
			        
				}}).setSize(pageWidth-10, pageWidth-10);				
				
				var orderTypeColumnChartData = initChartData();
				orderTypeColumnChartData.priceColumnChart.xAxis = ['抹数','折扣', '赠送', '退菜', '反结账', '提成', '服务费收入'];
				orderTypeColumnChartData.priceColumnChart.yAxis.data = [orderType.eraseIncome, orderType.discountIncome, orderType.giftIncome, orderType.cancelIncome, orderType.paidIncome, 0, orderType.serviceIncome];
				
				//操作类型
				newColumnChart2({
				  	rt: 'orderTypeColumnChart', title : '操作类型条形图', series: orderTypeColumnChartData.priceColumnChart.yAxis, xAxis:orderTypeColumnChartData.priceColumnChart.xAxis, clickHander : function(point){
				  		if(point.category == "服务费收入"){
				  			return;
				  		}
				  		
				        $.mobile.changePage("#singleReportMgr",
				        	    { transition: "fade" });
				        $('#reportName').html(point.category+ "报表");
				        
				        if(point.category == "折扣"){
				        	$.post('../../WXQueryBusinessStatistics.do', {
				        		dataSource:"getDiscountStaffChart",
				        		fid : Util.mp.fid,
				        		dateBeg:begin,
				        		dateEnd:end,
				        		deptID:-1,
				        		staffID:-1
				        	}, function(rt){
				        		
								var discountStaffPieChartData = initChartData();
								rt.root.forEach(function(e){  
									discountStaffPieChartData.chartPriceData.data.push([e.staffName, e.discountPrice]);
								}) 
					        	
					        	newPieChart2({rt: 'singleReportChart', title : "员工折扣金额比例图", unit: "元", series: discountStaffPieChartData.chartPriceData}).setSize(pageWidth-10, pageWidth-10);				        		
				        	});
				        	
				        	$.post('../../WXQueryBusinessStatistics.do', {
				        		dataSource:"getDiscountDeptChart",
				        		fid : Util.mp.fid,
				        		dateBeg:begin,
				        		dateEnd:end,
				        		deptID:-1,
				        		staffID:-1
				        	}, function(rt){
				        		
								var discountDeptPieChartData = initChartData();
								rt.root.forEach(function(e){  
									discountDeptPieChartData.chartPriceData.data.push([e.discountDept.name, e.discountPrice]);
								}) 
					        	
					        	newPieChart2({rt: 'secondReportChart', title : "部门折扣金额比例图", unit: "元", series: discountDeptPieChartData.chartPriceData}).setSize(pageWidth-10, pageWidth-10);				        		
				        	});
				        }else if(point.category == "赠送"){
				        	$.post('../../WXQueryBusinessStatistics.do', {
				        		dataSource:"getGiftStaffChart",
				        		fid : Util.mp.fid,
				        		dateBeg:begin,
				        		dateEnd:end,
				        		deptID:-1,
				        		staffID:-1
				        	}, function(rt){
				        		
								var giftStaffPieChartData = initChartData();
								rt.root.forEach(function(e){  
									giftStaffPieChartData.chartPriceData.data.push([e.giftStaff, e.giftPrice]);
								}) 
					        	
					        	newPieChart2({rt: 'singleReportChart', title : "员工赠送金额比例图", unit: "元", series: giftStaffPieChartData.chartPriceData}).setSize(pageWidth-10, pageWidth-10);				        		
				        	});
				        	
				        	$.post('../../WXQueryBusinessStatistics.do', {
				        		dataSource:"getGiftDeptChart",
				        		fid : Util.mp.fid,
				        		dateBeg:begin,
				        		dateEnd:end,
				        		deptID:-1,
				        		staffID:-1
				        	}, function(rt){
				        		
								var giftDeptPieChartData = initChartData();
								rt.root.forEach(function(e){  
									giftDeptPieChartData.chartPriceData.data.push([e.giftDept.name, e.giftPrice]);
								}) 
					        	
					        	newPieChart2({rt: 'secondReportChart', title : "部门折扣金额比例图", unit: "元", series: giftDeptPieChartData.chartPriceData}).setSize(pageWidth-10, pageWidth-10);				        		
				        	});
				        }else if(point.category == "退菜"){
				        	$.post('../../WXQueryBusinessStatistics.do', {
				        		dataSource:"getStaffChart",
				        		fid : Util.mp.fid,
				        		dateBeg:begin,
				        		dateEnd:end,
				        		deptID:-1,
				        		reasonID:-1,
				        		staffID:-1
				        	}, function(rt){
				        		
								var cancelStaffPieChartData = initChartData();
								rt.root.forEach(function(e){  
									cancelStaffPieChartData.chartPriceData.data.push([e.cancelStaff, e.cancelPrice]);
								}) 
					        	
					        	newPieChart2({rt: 'singleReportChart', title : "员工退菜金额比例图", unit: "元", series: cancelStaffPieChartData.chartPriceData}).setSize(pageWidth-10, pageWidth-10);				        		
				        	});
				        	
				        	$.post('../../WXQueryBusinessStatistics.do', {
				        		dataSource:"getReasonChart",
				        		fid : Util.mp.fid,
				        		dateBeg:begin,
				        		dateEnd:end,
				        		deptID:-1,
				        		reasonID:-1,
				        		staffID:-1
				        	}, function(rt){
				        		
								var cancelReasonPieChartData = initChartData();
								rt.root.forEach(function(e){  
									cancelReasonPieChartData.chartPriceData.data.push([e.reason, e.cancelPrice]);
								}) 
					        	
					        	newPieChart2({rt: 'secondReportChart', title : "退菜原因金额比例图", unit: "元", series: cancelReasonPieChartData.chartPriceData}).setSize(pageWidth-10, pageWidth-10);				        		
				        	});
				        }else if(point.category == "反结账"){
				        	$.post('../../WXQueryBusinessStatistics.do', {
				        		dataSource:"getRepaidStaffChart",
				        		fid : Util.mp.fid,
				        		dateBeg:begin,
				        		dateEnd:end,
				        		staffID:-1
				        	}, function(rt){
				        		
								var repaidStaffPieChartData = initChartData();
								rt.root.forEach(function(e){  
									repaidStaffPieChartData.chartPriceData.data.push([e.staffName, e.repaidPrice]);
								}) 
					        	
					        	newPieChart2({rt: 'singleReportChart', title : "员工赠送金额比例图", unit: "元", series: repaidStaffPieChartData.chartPriceData}).setSize(pageWidth-10, pageWidth-10);				        		
				        	});
				        	
				        }else if(point.category == "提成"){
				        	$.post('../../WXQueryBusinessStatistics.do', {
				        		dataSource:"getStaffChart",
				        		fid : Util.mp.fid,
				        		dateBeg:begin,
				        		dateEnd:end,
				        		deptID:-1,
				        		staffID:-1
				        	}, function(rt){
				        		
								var CommissionStaffPieChartData = initChartData();
								rt.root.forEach(function(e){  
									CommissionStaffPieChartData.chartPriceData.data.push([e.staffName, e.commissionPrice]);
								}) 
					        	
					        	newPieChart2({rt: 'singleReportChart', title : "员工提成金额比例图", unit: "元", series: CommissionStaffPieChartData.chartPriceData}).setSize(pageWidth-10, pageWidth-10);				        		
				        	});
				        	
				        }
				        
				  	}	
				}).setSize(pageWidth-10, pageWidth-10);					
				
				var memberOpeColumnChartData = initChartData();
				memberOpeColumnChartData.priceColumnChart.xAxis = ['充值', '退款'];
				memberOpeColumnChartData.priceColumnChart.yAxis.data = [orderType.memberChargeByCash, orderType.memberRefund];
				
				//会员
				newColumnChart2({
					rt: 'memberOpePieChart', title : '会员操作条形图', series: memberOpeColumnChartData.priceColumnChart.yAxis, xAxis:memberOpeColumnChartData.priceColumnChart.xAxis	
				}).setSize(pageWidth-10, pageWidth-10);
				
				
				if(rt.other.businessChart){
						
					var dailyBusinessStatistic = JSON.parse(rt.other.businessChart);
					var dailyBusinessStatisticColumnChartData = initChartData();
					dailyBusinessStatisticColumnChartData.priceColumnChart.xAxis = dailyBusinessStatistic.xAxis;
					dailyBusinessStatisticColumnChartData.priceColumnChart.yAxis.data = dailyBusinessStatistic.ser[0].data; 
					//每日营业报表条形图
					newColumnChart2({
					  	rt: 'dailyBusinessStatisticColumnChart', title : "每日营业统计", series: dailyBusinessStatisticColumnChartData.priceColumnChart.yAxis, xAxis:dailyBusinessStatisticColumnChartData.priceColumnChart.xAxis, dateFormat: true, clickHander : function(point){
				            $.mobile.changePage("#dailyBusinessStatisticMgr",
				            	    { transition: "fade" });
				            $('#businessStatisticTitle').html("每日营业统计(" + point.category + ")" );
				            
				            Util.lm.show();
				            $.post('../../WXQueryBusinessStatistics.do', {
				    			fid : Util.mp.fid,
				    			onDuty:point.category + " 00:00:00",
				    			offDuty:point.category + " 23:59:59",
				    			region:-1,
				    			dutyRange:"range",
				    			dataSource:"history"	
				            }, function(rt){
								Util.lm.hide();
								
								var daily_orderType = rt.other.business;
								var daily_deptStatistics = rt.other.business.deptStat;
								var daily_receiveStatistics = rt.other.business.paymentIncomes;
								
								var daily_receiveChartData = initChartData();
								daily_receiveStatistics.forEach(function(e){  
									daily_receiveChartData.chartPriceData.data.push([e.payType, e.total]);
								}) 
								//收款
								newPieChart2({rt: 'daily_receivePieChart', title : '收款方式比例图', unit: '元', series: daily_receiveChartData.chartPriceData}).setSize(pageWidth-10, pageWidth-10);
								
								var daily_deptGeneralPieChartData = initChartData();
								daily_deptStatistics.forEach(function(e){  
									daily_deptGeneralPieChartData.chartPriceData.data.push([e.dept.name, e.income]);
								}) 
								//部门
								newPieChart2({rt: 'daily_deptGeneralPieChart', title : '部门汇总比例图', unit: '元', series: daily_deptGeneralPieChartData.chartPriceData}).setSize(pageWidth-10, pageWidth-10);
								
								var daily_orderTypeColumnChartData = initChartData();
								daily_orderTypeColumnChartData.priceColumnChart.xAxis = ['抹数','折扣', '赠送', '退菜', '反结账', '提成', '服务费收入'];
								daily_orderTypeColumnChartData.priceColumnChart.yAxis.data = [daily_orderType.eraseIncome, daily_orderType.discountIncome, daily_orderType.giftIncome, daily_orderType.cancelIncome, daily_orderType.paidIncome, 0, daily_orderType.serviceIncome];
								
						
								var daily_memberOpeColumnChartData = initChartData();
								daily_memberOpeColumnChartData.priceColumnChart.xAxis = ['充值', '退款'];
								daily_memberOpeColumnChartData.priceColumnChart.yAxis.data = [daily_orderType.memberChargeByCash, daily_orderType.memberRefund];
								

								setTimeout(function(){
									//操作类型
									newColumnChart2({
									  	rt: 'daily_orderTypeColumnChart', title : '操作类型条形图', series: daily_orderTypeColumnChartData.priceColumnChart.yAxis, xAxis:daily_orderTypeColumnChartData.priceColumnChart.xAxis
									}).setSize(pageWidth-10, pageWidth-10);	
									//会员
									newColumnChart2({
										rt: 'daily_memberOpePieChart', title : '会员操作条形图', series: daily_memberOpeColumnChartData.priceColumnChart.yAxis, xAxis:daily_memberOpeColumnChartData.priceColumnChart.xAxis	
									}).setSize(pageWidth-10, pageWidth-10);	
									
								}, 250);
								
								
				            });
				            
					  	}	
					}).setSize(pageWidth-10, pageWidth-10);							
					
				}
				
			}
		},
		error : function(xhr){
			Util.lm.hide();
		}
	});
}

$(function () {
	//默认调用本周
	changeDate(8);
	
});

function changeTime(thiz){
	changeDate($(thiz).val());
}

function changeDate(value){
	var now = new Date(); //当前日期 
	var nowDate = now.getDate(); //当前日
	var nowDayOfWeek = now.getDay(); //今天本周的第几天 
	var nowMonth = now.getMonth(); //当前月 0开始
	var nowYear = now.getFullYear(); //当前年 
	
	if(value == 0){//今天
		
	}else if(value == 1){//前一天
		now.setDate(now.getDate()-1);
		dateEnd.setValue(now);
	}else if(value == 2){//最近7天
		now.setDate(now.getDate()-7);
	}else if(value == 3){//最近一个月
		now.setMonth(now.getMonth()-1);
	}else if(value == 4){//最近三个月
		now.setMonth(now.getMonth()-3);
		//为避免当天无数据显示, 删除当天 
		var nowWeek = new Date();
		if(nowDayOfWeek != 1){
			nowWeek.setDate(nowWeek.getDate()-1);
		}
		$('#endDate').html(nowWeek.getFullYear() + "-" + (nowWeek.getMonth()+1) + "-" + nowWeek.getDate());
	}else if(value == 5){//本周
		now.setDate(now.getDate() - (nowDayOfWeek - 1));
		//为避免当天无数据显示, 删除当天 
		var nowWeek = new Date();
		if(nowDayOfWeek != 1){
			nowWeek.setDate(nowWeek.getDate()-1);
		}
		$('#endDate').html(nowWeek.getFullYear() + "-" + (nowWeek.getMonth()+1) + "-" + nowWeek.getDate());
		
	}else if(value == 6){//上周
		now.setDate(now.getDate() - nowDayOfWeek);
		dateEnd.setValue(now);
		now.setDate(now.getDate() - 6);
	}else if(value == 7){//本月
		//为避免当天无数据显示, 删除当天 
		if(now.getDate() != 1){
			var nowWeek = new Date();
			nowWeek.setDate(nowWeek.getDate()-1);
			//dateEnd.setValue(nowWeek);
			$('#endDate').html(nowWeek.getFullYear() + "-" + (nowWeek.getMonth()+1) + "-" + nowWeek.getDate());
		}					
		now.setDate(now.getDate() - (now.getDate() -1));
		
	}else if(value == 8){//上个月
		//FIXME 月份加减遇12时
		var nowWeek = new Date(nowYear, nowMonth-1, getMonthDays(nowMonth-1));
		$('#endDate').html(nowWeek.getFullYear() + "-" + (nowWeek.getMonth()+1) + "-" + nowWeek.getDate());
		now = new Date(nowYear, nowMonth-1, 1);
	}else if(value == 9){//最近半年
		now.setMonth(now.getMonth()-6);
	}else if(value == 10){//无限期
		now = "";
	}
	$('#beginDate').html(now.getFullYear() + "-" + (now.getMonth()+1) + "-" + now.getDate());
	//dateBegin.setValue(now);
	//dateBegin.clearInvalid();
	getBusinessStatisticsData({chart : true});
	
}
//获得某月的天数 
function getMonthDays(myMonth){ 
	var now = new Date(); //当前日期 
	var nowDay = now.getDate(); //当前日 
	var nowMonth = now.getMonth(); //当前月 
	var nowYear = now.getYear(); //当前年 
	nowYear += (nowYear < 2000) ? 1900 : 0; //
	var monthStartDate = new Date(nowYear, myMonth, 1); 
	var monthEndDate = new Date(nowYear, myMonth + 1, 1); 
	var days = (monthEndDate - monthStartDate)/(1000 * 60 * 60 * 24); 
	return days; 
}
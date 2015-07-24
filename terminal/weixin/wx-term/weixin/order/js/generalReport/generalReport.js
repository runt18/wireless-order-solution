var Request = new Util_urlParaQuery();
var rid = Request["rid"];
var pageWidth ;

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
        	margin: c.titleMargin ? c.titleMargin : 15,
            text: c.title
        },
        subtitle: {
            text: c.subtitle ? c.subtitle : '',
    		style : {
    			color: 'blue',
                fontWeight: 'bold'
    		}
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
        subtitle: {
            text: c.subtitle ? c.subtitle : '',
    		style : {
    			color: 'blue',
                fontWeight: 'bold'
    		}
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
                text: c.unit?c.unit:'金额 (元)',                             
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

var orderTypeStatisticParam;
function getBusinessStatisticsData(c){
	c = c || {}
	var begin = c.begin ? c.begin : $('#beginDate').html() + ' 00:00:00';
	var end = c.end ? c.end : $('#endDate').html() + ' 23:59:59';
	
	Util.lm.show();
	
	$.ajax({
		url : '../../WXQueryBusinessStatistics.do',
		type : 'post',
		data : {
			oid : Util.mp.oid,
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
				var memberStatistics = rt.other.memberStatistics;
				
				var receiveChartData = initChartData();
				receiveStatistics.forEach(function(e){  
					if(e.total > 0){
						receiveChartData.chartPriceData.data.push([e.payType, e.total]);
					}
				}) 
				//收款
				newPieChart2({rt: 'receivePieChart', title : '收款方式统计', unit: '元', series: receiveChartData.chartPriceData}).setSize(pageWidth-10, pageWidth+10);
				
				var deptGeneralPieChartData = initChartData();
				deptStatistics.forEach(function(e){  
				    deptGeneralPieChartData.chartPriceData.data.push([e.dept.name, e.income]);
				}) 
				//部门
				newPieChart2({rt: 'deptGeneralPieChart', title : '各部门营业统计', subtitle : '点击饼图查看各厨房营业', unit: '元', series: deptGeneralPieChartData.chartPriceData, clickHander : function(point){
			       $.mobile.changePage("#singleReportMgr",
			        	    { transition: "fade" });
			        $('#reportName').html(point.name +"统计("+ $('#beginDate').text() + " ~ " + $('#endDate').text() + ")");
			        //部门只需一个饼图 & 隐藏开关
			        $('#secondReportChart').hide();
			        $('#hr4SecondReportChart').hide();
			        $("#statisticToggle").hide();
			        
			        Util.lm.show();
			        //厨房
			        $.post('../../WXQueryBusinessStatistics.do', {
			        	dataSource : 'deptSaleStatistic',
						oid : Util.mp.oid,
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
			        	
			        	newPieChart2({rt: 'singleReportChart', title : "各厨房营业统计", unit: "元", series: kitchenPieChartData.chartPriceData}).setSize(pageWidth-10, pageWidth+30);
					    
			        },'json');
			        
			        Util.lm.show();
			        //菜品top10
			        $.post('../../WXQueryBusinessStatistics.do', {
			        	dataSource : 'deptSaleStatistic',
						oid : Util.mp.oid,
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
								name : rt.root[i].food.name.substring(0, 8),
								money : rt.root[i].income.toFixed(2),
								count : rt.root[i].salesAmount
							}));
						}
			        	$('#table4KitchenTop10').html(html.join(''));
						
			        	//不显示选择隐藏列按钮
			        	$('.ui-table-columntoggle-btn').hide();
			        	$('#display4KitchenTop10').show();
			        },'json');
			        
				}}).setSize(pageWidth-10, pageWidth+10);				
				
				var orderTypeColumnChartData = initChartData();
				orderTypeColumnChartData.priceColumnChart.xAxis = ['抹数','折扣', '赠送', '退菜', '反结账', '服务费收入'];
				orderTypeColumnChartData.priceColumnChart.yAxis.data = [orderType.eraseIncome, orderType.discountIncome, orderType.giftIncome, orderType.cancelIncome, Math.abs(orderType.paidIncome), orderType.serviceIncome];
				
				//操作类型
				newColumnChart2({
				  	rt: 'orderTypeColumnChart', title : '操作类型统计', subtitle : '点击条形图查看详情', series: orderTypeColumnChartData.priceColumnChart.yAxis, xAxis:orderTypeColumnChartData.priceColumnChart.xAxis, clickHander : function(point){
				  		if(point.category == "服务费收入" || point.category == "抹数"){
				  			return;
				  		}
				  		//操作类型需要一个或者两个饼图 & 开关 & 隐藏菜品top10
				  		$("#statisticToggle").show();
				  		$('#secondReportChart').hide();
				  		$('#hr4SecondReportChart').hide();
				  		$('#display4KitchenTop10').hide();
				  		//开关复位
				  		var myswitch = $('input[name="priceOrAmount"]:checked').val();
				  		if(myswitch > 0){
				  			$('input[name="priceOrAmount"]:eq(0)').attr("checked",true).checkboxradio("refresh");
				  			$('input[name="priceOrAmount"]:eq(1)').attr("checked",false).checkboxradio("refresh");
				  		}
				  		
				        $.mobile.changePage("#singleReportMgr",
				        	    { transition: "fade" });
				        $('#reportName').html(point.category+ "报表");
				        
				        orderTypeStatisticParam = {
				        	priceOrAmount : "金额",
				        	unit : "元",
				        	switchType : 0,
				        	point : point,
				        	begin : begin,
				        	end : end
				        }
				        
				        orderTpyeSwitch(orderTypeStatisticParam);
				  	}	
				}).setSize(pageWidth, pageWidth * 1.5);					
				
				var memberOpeColumnChartData = initChartData();
				memberOpeColumnChartData.priceColumnChart.xAxis = ['消费', '充值', '退款'];
				memberOpeColumnChartData.priceColumnChart.yAxis.data = [memberStatistics.totalConsume, memberStatistics.totalCharge, memberStatistics.totalRefund];
				
				//会员
				newColumnChart2({
					rt: 'memberOpePieChart', title : '会员消费|会员充值|退款统计', series: memberOpeColumnChartData.priceColumnChart.yAxis, xAxis:memberOpeColumnChartData.priceColumnChart.xAxis	, 
					clickHander : function(point){
				        $.mobile.changePage("#dailyMemberStatisticMgr",
				        	    { transition: "fade" });
			  			$('#memberStatisticsTotalTitle').html("总金额");
			  			$('#memberStatisticsAvgTitle').html("日均额");
			  			//清空chart
			  			$('#daily_memberStatisticsColumnChart').html('');
				        
						var memberEachDays = rt.other.memberStatistics.memberEachDays;
						var member_xAxis = [], member_yAxis = [];
						
						Util.lm.show();
				  		if(point.category == "消费"){
				  			$('#memberStatisticTitle').html("会员每日消费统计");
				  			$('#memberStatisticsTotalMoney').html(memberStatistics.totalConsume + "元");
				  			$('#memberStatisticsAvgMoney').html(memberStatistics.avgConsume + "元");
				  			
				  			for (var i = 0; i < memberEachDays.length; i++) {
				  				member_xAxis.push(memberEachDays[i].date.substring(5));
				  				member_yAxis.push(memberEachDays[i].memberConsumption);
							}
				  		}else if(point.category == "充值"){
				  			$('#memberStatisticTitle').html("会员每日充值统计");
				  			$('#memberStatisticsTotalMoney').html(memberStatistics.totalCharge + "元");
				  			$('#memberStatisticsAvgMoney').html(memberStatistics.avgCharge + "元");
				  			
				  			for (var i = 0; i < memberEachDays.length; i++) {
				  				member_xAxis.push(memberEachDays[i].date.substring(5));
				  				member_yAxis.push(memberEachDays[i].memberCharge);
							}
				  		}else if(point.category == "退款"){
				  			$('#memberStatisticTitle').html("会员每日退款统计");
				  			$('#memberStatisticsTotalMoney').html(memberStatistics.totalRefund + "元");
				  			$('#memberStatisticsAvgMoney').html(memberStatistics.avgRefund + "元");
				  			
				  			for (var i = 0; i < memberEachDays.length; i++) {
				  				member_xAxis.push(memberEachDays[i].date.substring(5));
				  				member_yAxis.push(memberEachDays[i].memberRefund);
							}
				  		}
				  		
						var memberEachDayColumnChartData = initChartData();
						memberEachDayColumnChartData.priceColumnChart.xAxis = member_xAxis;
						memberEachDayColumnChartData.priceColumnChart.yAxis.data = member_yAxis;
						
						var reportHeight = pageWidth;
						if(member_xAxis.length > 14){
							reportHeight = member_xAxis.length *30;
						}
						
						setTimeout(function(){
							//会员每日统计
							newColumnChart2({
								rt: 'daily_memberStatisticsColumnChart', title : '会员每日统计', series: memberEachDayColumnChartData.priceColumnChart.yAxis, 
								xAxis:memberEachDayColumnChartData.priceColumnChart.xAxis	
							}).setSize(pageWidth, reportHeight);
							Util.lm.hide();
						}, 2500);
						
				  	}
				}).setSize(pageWidth-10, pageWidth-10);
				
				
				var memberCreateColumnChartData = initChartData();
				memberCreateColumnChartData.priceColumnChart.xAxis = ['开卡数'];
				memberCreateColumnChartData.priceColumnChart.yAxis.data = [memberStatistics.totalCreated];
				
				//会员开卡
				newColumnChart2({
					rt: 'memberCreateChart', title : '会员开卡', series: memberCreateColumnChartData.priceColumnChart.yAxis, 
					xAxis:memberCreateColumnChartData.priceColumnChart.xAxis, unit:'数量(张)',
					clickHander : function(point){
				        $.mobile.changePage("#dailyMemberStatisticMgr",
				        	    { transition: "fade" });
						var memberEachDays = rt.other.memberStatistics.memberEachDays;
						var member_xAxis = [], member_yAxis = [];
			  			$('#memberStatisticTitle').html("会员每日开卡统计");
			  			$('#memberStatisticsTotalTitle').html("总开卡数");
			  			$('#memberStatisticsAvgTitle').html("日均开卡数");
			  			//清空chart
			  			$('#daily_memberStatisticsColumnChart').html('');
			  			
			  			$('#memberStatisticsTotalMoney').html(memberStatistics.totalCreated + "张");
			  			$('#memberStatisticsAvgMoney').html(memberStatistics.avgCreated + "张");
				  			
			  			Util.lm.show();
			  			for (var i = 0; i < memberEachDays.length; i++) {
			  				member_xAxis.push(memberEachDays[i].date.substring(5));
			  				member_yAxis.push(memberEachDays[i].memberCreate);
						}
				  		
						var memberEachDayColumnChartData = initChartData();
						memberEachDayColumnChartData.priceColumnChart.xAxis = member_xAxis;
						memberEachDayColumnChartData.priceColumnChart.yAxis.data = member_yAxis;
						
						var reportHeight = pageWidth;
						if(member_xAxis.length > 14){
							reportHeight = member_xAxis.length *30;
						}
						
						setTimeout(function(){
							//会员每日统计
							newColumnChart2({
								rt: 'daily_memberStatisticsColumnChart', title : '会员每日统计', series: memberEachDayColumnChartData.priceColumnChart.yAxis, 
								xAxis:memberEachDayColumnChartData.priceColumnChart.xAxis, unit:'数量(张)'	
							}).setSize(pageWidth, reportHeight);
							Util.lm.hide();
						}, 2500);
				  	}
				}).setSize(pageWidth-10, pageWidth-10);
				
				if(rt.other.businessChart){
						
					var dailyBusinessStatistic = JSON.parse(rt.other.businessChart);
					var dailyBusinessStatisticColumnChartData = initChartData();
					dailyBusinessStatisticColumnChartData.priceColumnChart.xAxis = dailyBusinessStatistic.xAxis.splice(0, 31);
					dailyBusinessStatisticColumnChartData.priceColumnChart.yAxis.data = dailyBusinessStatistic.ser[0].data.splice(0, 31); 
					
					//总额 & 日均
					$("#businessTotalMoney").text(dailyBusinessStatistic.totalMoney);
					$("#businessAvgMoney").text(dailyBusinessStatistic.avgMoney);
					$("#businessAvgCount").text(dailyBusinessStatistic.avgCount);
					
					var reportHeight = pageWidth;
					if(dailyBusinessStatisticColumnChartData.priceColumnChart.xAxis.length > 7){
						reportHeight = dailyBusinessStatisticColumnChartData.priceColumnChart.xAxis.length *30;
					}
					
					//每日营业报表条形图
					newColumnChart2({
					  	rt: 'dailyBusinessStatisticColumnChart', title : "每日营业统计", subtitle : '点击条形图查看详情', series: dailyBusinessStatisticColumnChartData.priceColumnChart.yAxis, xAxis:dailyBusinessStatisticColumnChartData.priceColumnChart.xAxis, dateFormat: true, clickHander : function(point){
				            $.mobile.changePage("#dailyBusinessStatisticMgr",
				            	    { transition: "fade" });
				            $('#businessStatisticTitle').html("每日营业统计(" + point.category + ")" );
				            
				            Util.lm.show();
				            $.post('../../WXQueryBusinessStatistics.do', {
				    			oid : Util.mp.oid,
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
									if(e.total > 0){
										daily_receiveChartData.chartPriceData.data.push([e.payType, e.total]);
									}
								}) 
								//收款
								newPieChart2({rt: 'daily_receivePieChart', title : '收款方式统计', unit: '元', series: daily_receiveChartData.chartPriceData}).setSize(pageWidth-10, pageWidth+30);
								
								var daily_deptGeneralPieChartData = initChartData();
								daily_deptStatistics.forEach(function(e){  
									if(e.income > 0){
										daily_deptGeneralPieChartData.chartPriceData.data.push([e.dept.name, e.income]);
									}
								}) 
								//部门
								newPieChart2({rt: 'daily_deptGeneralPieChart', title : '各部门营业统计', unit: '元', series: daily_deptGeneralPieChartData.chartPriceData}).setSize(pageWidth-10, pageWidth+30);
								
								var daily_orderTypeColumnChartData = initChartData();
								daily_orderTypeColumnChartData.priceColumnChart.xAxis = ['抹数','折扣', '赠送', '退菜', '反结账', '服务费收入'];
								daily_orderTypeColumnChartData.priceColumnChart.yAxis.data = [daily_orderType.eraseIncome, daily_orderType.discountIncome, daily_orderType.giftIncome, daily_orderType.cancelIncome, Math.abs(daily_orderType.paidIncome), daily_orderType.serviceIncome];
								
						
								var daily_memberOpeColumnChartData = initChartData();
								daily_memberOpeColumnChartData.priceColumnChart.xAxis = ['充值', '退款'];
								daily_memberOpeColumnChartData.priceColumnChart.yAxis.data = [daily_orderType.memberChargeByCash, daily_orderType.memberRefund];
								

								setTimeout(function(){
									//操作类型
									newColumnChart2({
									  	rt: 'daily_orderTypeColumnChart', title : '操作类型统计', series: daily_orderTypeColumnChartData.priceColumnChart.yAxis, xAxis:daily_orderTypeColumnChartData.priceColumnChart.xAxis
									}).setSize(pageWidth-10, pageWidth * 1.5);	
									//会员
									newColumnChart2({
										rt: 'daily_memberOpePieChart', title : '会员充值|退款统计', series: daily_memberOpeColumnChartData.priceColumnChart.yAxis, xAxis:daily_memberOpeColumnChartData.priceColumnChart.xAxis	
									}).setSize(pageWidth-10, pageWidth-10);	
									
								}, 250);
								
								
				            });
				            
					  	}	
					}).setSize(pageWidth-10, reportHeight);							
					
				}
				
			}
		},
		error : function(xhr){
			Util.lm.hide();
		}
	});
}

$(function () {
	$.post('../../WXInterface.do', {
		dataSource : 'getRestaurant',
		rid : rid
	}, function(rt){
		if(rt.success){
			$('#generalTitle').text('营业统计(' + rt.root[0].name + ')');
		}
	}, "json")
	pageWidth = $(window).width();
	//默认调用前一日
	$("#selectTimes").val(1).selectmenu('refresh');
	changeDate(1);
	
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
		$('#endDate').html(now.getFullYear() + "-" + (now.getMonth()+1) + "-" + now.getDate());
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
		$('#endDate').html(now.getFullYear() + "-" + (now.getMonth()+1) + "-" + now.getDate());
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

//操作类型饼图
function orderTpyeSwitch(c){
	Util.lm.show();
    if(c.point.category == "折扣"){
    	$.post('../../WXQueryBusinessStatistics.do', {
    		dataSource:"getDiscountStaffChart",
    		oid : Util.mp.oid,
    		dateBeg:c.begin,
    		dateEnd:c.end,
    		deptID:-1,
    		staffID:-1
    	}, function(rt){
    		Util.lm.hide();
			var discountStaffPieChartData = initChartData();
			rt.root.forEach(function(e){  
				discountStaffPieChartData.chartPriceData.data.push([e.staffName, e.discountPrice]);
				discountStaffPieChartData.chartAmountData.data.push([e.staffName, e.discountAmount]);
			}) 
        	
        	newPieChart2({rt: 'singleReportChart', title : "按员工折扣汇总", unit: c.unit, series: c.switchType == 0 ? discountStaffPieChartData.chartPriceData : discountStaffPieChartData.chartAmountData}).setSize(pageWidth-10, pageWidth + 30);				        		
    	});
    	
    	$.post('../../WXQueryBusinessStatistics.do', {
    		dataSource:"getDiscountDeptChart",
    		oid : Util.mp.oid,
    		dateBeg:c.begin,
    		dateEnd:c.end,
    		deptID:-1,
    		staffID:-1
    	}, function(rt){
    		
			var discountDeptPieChartData = initChartData();
			rt.root.forEach(function(e){  
				discountDeptPieChartData.chartPriceData.data.push([e.discountDept.name, e.discountPrice]);
				discountDeptPieChartData.chartAmountData.data.push([e.discountDept.name, e.discountAmount]);
			}) 
        	
        	newPieChart2({rt: 'secondReportChart', title : "按部门折扣汇总", titleMargin : 50, unit: c.unit, series: c.switchType == 0 ? discountDeptPieChartData.chartPriceData : discountDeptPieChartData.chartAmountData}).setSize(pageWidth-10, pageWidth + 30);
			$('#secondReportChart').show();
			$('#hr4SecondReportChart').show();
    	});
    }else if(c.point.category == "赠送"){
    	Util.lm.show();
    	$.post('../../WXQueryBusinessStatistics.do', {
    		dataSource:"getGiftStaffChart",
    		oid : Util.mp.oid,
    		dateBeg:c.begin,
    		dateEnd:c.end,
    		deptID:-1,
    		staffID:-1
    	}, function(rt){
    		Util.lm.hide();
			var giftStaffPieChartData = initChartData();
			rt.root.forEach(function(e){  
				giftStaffPieChartData.chartPriceData.data.push([e.giftStaff, e.giftPrice]);
				giftStaffPieChartData.chartAmountData.data.push([e.giftStaff, e.giftAmount]);
			}) 
        	
        	newPieChart2({rt: 'singleReportChart', title : "按员工赠送汇总", unit: c.unit, series: c.switchType == 0 ?giftStaffPieChartData.chartPriceData : giftStaffPieChartData.chartAmountData}).setSize(pageWidth-10, pageWidth + 30);				        		
    	});
    	
    	$.post('../../WXQueryBusinessStatistics.do', {
    		dataSource:"getGiftDeptChart",
    		oid : Util.mp.oid,
    		dateBeg:c.begin,
    		dateEnd:c.end,
    		deptID:-1,
    		staffID:-1
    	}, function(rt){
    		
			var giftDeptPieChartData = initChartData();
			rt.root.forEach(function(e){  
				giftDeptPieChartData.chartPriceData.data.push([e.giftDept.name, e.giftPrice]);
				giftDeptPieChartData.chartAmountData.data.push([e.giftDept.name, e.giftAmount]);
			}) 
        	
        	newPieChart2({rt: 'secondReportChart', title : "按部门赠送汇总", titleMargin : 50, unit: c.unit, series: c.switchType == 0 ? giftDeptPieChartData.chartPriceData : giftDeptPieChartData.chartAmountData }).setSize(pageWidth-10, pageWidth + 30);
			$('#secondReportChart').show();
			$('#hr4SecondReportChart').show();
    	});
    }else if(c.point.category == "退菜"){
    	Util.lm.show();
    	$.post('../../WXQueryBusinessStatistics.do', {
    		dataSource:"getCancelStaffChart",
    		oid : Util.mp.oid,
    		dateBeg:c.begin,
    		dateEnd:c.end,
    		deptID:-1,
    		reasonID:-1,
    		staffID:-1
    	}, function(rt){
    		Util.lm.hide();
			var cancelStaffPieChartData = initChartData();
			rt.root.forEach(function(e){  
				cancelStaffPieChartData.chartPriceData.data.push([e.cancelStaff, e.cancelPrice]);
				cancelStaffPieChartData.chartAmountData.data.push([e.cancelStaff, e.cancelAmount]);
			}) 
        	
        	newPieChart2({rt: 'singleReportChart', title : "按退菜人员汇总", unit: c.unit, series: c.switchType == 0 ?cancelStaffPieChartData.chartPriceData:cancelStaffPieChartData.chartAmountData}).setSize(pageWidth-10, pageWidth + 30);				        		
    	});
    	
    	$.post('../../WXQueryBusinessStatistics.do', {
    		dataSource:"getCancelReasonChart",
    		oid : Util.mp.oid,
    		dateBeg:c.begin,
    		dateEnd:c.end,
    		deptID:-1,
    		reasonID:-1,
    		staffID:-1
    	}, function(rt){
    		
			var cancelReasonPieChartData = initChartData();
			rt.root.forEach(function(e){  
				cancelReasonPieChartData.chartPriceData.data.push([e.reason, e.cancelPrice]);
				cancelReasonPieChartData.chartAmountData.data.push([e.reason, e.cancelAmount]);
			}) 
        	
        	newPieChart2({rt: 'secondReportChart', title : "按退菜原因汇总", titleMargin : 50, unit: c.unit, series: c.switchType == 0 ?cancelReasonPieChartData.chartPriceData:cancelReasonPieChartData.chartAmountData}).setSize(pageWidth-10, pageWidth + 30);
			$('#secondReportChart').show();
			$('#hr4SecondReportChart').show();
    	});
    }else if(c.point.category == "反结账"){
    	Util.lm.show();
    	$.post('../../WXQueryBusinessStatistics.do', {
    		dataSource:"getRepaidStaffChart",
    		oid : Util.mp.oid,
    		dateBeg:c.begin,
    		dateEnd:c.end,
    		staffID:-1
    	}, function(rt){
    		Util.lm.hide();
			var repaidStaffPieChartData = initChartData();
			rt.root.forEach(function(e){  
				repaidStaffPieChartData.chartPriceData.data.push([e.staffName, e.repaidPrice]);
				repaidStaffPieChartData.chartAmountData.data.push([e.staffName, e.repaidAmount]);
			}) 
        	
        	newPieChart2({rt: 'singleReportChart', title : "按员工反结账汇总", unit: c.unit, series: c.switchType == 0 ?repaidStaffPieChartData.chartPriceData :repaidStaffPieChartData.chartAmountData}).setSize(pageWidth-10, pageWidth + 30);				        		
    	});
    	
    }else if(c.point.category == "提成"){
    	Util.lm.show();
    	$.post('../../WXQueryBusinessStatistics.do', {
    		dataSource:"getCommissionStaffChart",
    		oid : Util.mp.oid,
    		dateBeg:c.begin,
    		dateEnd:c.end,
    		deptID:-1,
    		staffID:-1
    	}, function(rt){
    		Util.lm.hide();
			var commissionStaffPieChartData = initChartData();
			rt.root.forEach(function(e){  
				commissionStaffPieChartData.chartPriceData.data.push([e.staffName, e.commissionPrice]);
				commissionStaffPieChartData.chartAmountData.data.push([e.staffName, e.commissionAmount]);
			}) 
        	
        	newPieChart2({rt: 'singleReportChart', title : "按员工提成汇总", unit: c.unit, series: c.switchType == 0 ?commissionStaffPieChartData.chartPriceData:commissionStaffPieChartData.chartAmountData}).setSize(pageWidth-10, pageWidth + 30);				        		
    	});
    	
    }	
	
}

function displayPriceOrAmount(){
	$('input[name="priceOrAmount"]').each(function(){
		if(this.checked){
			orderTypeStatisticParam.priceOrAmount = this.value == 0? "金额" : "数量";
			orderTypeStatisticParam.unit = this.value == 0? "元" : "份";
			orderTypeStatisticParam.switchType = this.value;
			console.log(orderTypeStatisticParam)
			orderTpyeSwitch(orderTypeStatisticParam);
		}
	});
}



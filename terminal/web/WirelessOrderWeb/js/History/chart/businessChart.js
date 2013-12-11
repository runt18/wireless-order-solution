var details;
var series;
var businessStatWin;
function print(obj){
    try{
        seen = [];
        json = JSON.stringify(obj, function(key, val) {
           if (typeof val == "object") {
                if (seen.indexOf(val) >= 0) return;
                seen.push(val);
            }
            return val;
        });
        return json;
    }catch(e){
        return e;
    }
}
function NewDate(str) { 
	str = str.split('-'); 
	var date = new Date(); 
	date.setUTCFullYear(str[0], str[1] - 1, str[2]); 
	date.setUTCHours(0, 0, 0, 0); 
	return date; 
} 

function each(x){
	var date = NewDate(x).getTime();
	businessStatWin = new Ext.Window({
		title : '营业统计 -- <font style="color:green;">历史</font>',
		id : 'businessDetailWin',
		width : 885,
		height : 555,
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
					text : '功能加载中, 请稍后......',
					params : {
						dataSource : 'history',
						dutyRange : "range",
						offDuty : date,
						onDuty : date,
						queryPattern : 3
					}
				});
			}
		}
	});
	businessStatWin.show();
	businessStatWin.center();
/*	$.each(details, function(i){

		if(details[i].offDutyToDate == x){
			$("tr").remove();
			var secendTr = "<tr><td>日期:</td><td>" + x + "</td><td>应收:</td><td>" + details[i].totalIncome + "元</td><td>实收:</td><td>" + details[i].totalActual + "元</td><td>账单数:</td><td>" + details[i].orderAmount + "</td>"
					+ "<td>现金</td><td>" + details[i].cashIncome2 + "元</td><td>刷卡:</td><td>" + details[i].creditCardIncome2 + "元</td><td>会员:</td><td>" + details[i].memberActual + "元</td></tr>"
					+ "<tr><td>挂账:</td><td>" + details[i].hangIncome2 + "元</td>"
					+ "<td>签单:</td><td>" + details[i].signIncome2 + "元</td><td>折扣:</td><td>" + details[i].discountIncome + "元</td><td>赠送:</td><td>" + details[i].giftIncome + "元</td><td>退菜:</td><td>" + details[i].cancelIncome + "元</td>"
					+ "<td>抹数:</td><td>" + details[i].eraseIncome + "元</td><td>反结账:</td><td>" + details[i].paidIncome + "元</td></tr>";
				
			$("#table_showDetail").append(secendTr);
			$("#div_showDetail").show();
		}
	});*/
}

function showChart(time){
	$("#loading").show();
	$.post('../../BusinessReceiptsStatistics.do', {dataSource : 'chart', time : time==null?7:time}, function(data){
		$("#loading").hide();
		var jdata = $.parseJSON(data);
		details = jdata.root;
		//console.debug(jdata.other.chart)
		var chartData = eval('(' + jdata.other.chart + ')');
		new Highcharts.Chart({
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
							each(e.point.category);
						}
					}
				}
			},
	        chart: {  
	        	renderTo: 'container'
	    	}, 
	        title: {
	            text: '<b>营业走势图</b>'
	        },
	        labels: {
	        	items : [{
	        		html : '<b>总营业额:' + chartData.totalMoney + ' 元</b><br><b>日均收入:' + chartData.avgMoney + ' 元</b><br><b>日均账单:' + chartData.avgCount + ' 张</b>',
		        	style : {left :($('#container').width()*0.80), top: '0px'}
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
                    return '<b>' + this.series.name + '</b><br/>'+
                        this.x +': '+ '<b>'+this.y+'</b> ';
                }
	        },
//	        legend: {
//	            layout: 'vertical',
//	            align: 'right',
//	            verticalAlign: 'middle',
//	            borderWidth: 0
//	        },
//	        series : [{  
//	            name: chartData.ser.name,  
//	            data: chartData.ser.data
//	        }],
	        series : chartData.ser,
	        exporting : {
	        	enabled : false
	        },
	        credits : {
	        	enabled : false
	        }
		});
	});
}

$(function () {
	$("#a_seven").click();
	$("#a_seven").css({color:"red", textDecoration: "underline"});
	$(".zi").click(function(){
		$(".zi").css({color:"blue", textDecoration: "none"});
		$(this).css({color:"red", textDecoration: "underline"});
	});

});

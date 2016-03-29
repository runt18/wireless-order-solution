//var details;
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
function newDate(str) { 
	str = str.split('-'); 
	var date = new Date(); 
	date.setUTCFullYear(str[0], str[1] - 1, str[2]); 
	date.setUTCHours(0, 0, 0, 0); 
	return date; 
} 

function each(x){
	var date = newDate(x).getTime();
	businessStatWin = new Ext.Window({
		title : '营业统计 -- <font style="color:green;">历史</font>',
		id : 'businessDetailWin',
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
					text : '功能加载中, 请稍后......',
					params : {
						dataSource : 'history',
						dutyRange : "range",
						offDuty : date,
						onDuty : date,
						queryPattern : 3
					}
				});
				
				thiz.dataSource = 'history';
				thiz.dutyRange = 'range';
				thiz.offDuty = date;
				thiz.onDuty = date;
				thiz.queryPattern = 3
			}
		}
	});
	businessStatWin.show();
	businessStatWin.center();
}

function showChart(time){
	$("#loading").show();
	$.post('../../BusinessReceiptsStatistics.do', {dataSource : 'chart', time : time==null?7:time}, function(jdata){
		$("#loading").hide();
//		var jdata = $.parseJSON(data);
//		details = jdata.root;
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
//	        	crosshairs: true,
                formatter: function() {
                    return '<b>' + this.series.name + '</b><br/>'+
                        this.x +': '+ '<b>'+this.y+'</b> ';
                }
	        },
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

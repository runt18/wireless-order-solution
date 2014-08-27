var Wireless = {
	ux : {staffGift : false},
	chart : {}
};

function isEmptyObject(obj){
    for(var n in obj){
		return false;
    }
    return true; 
} 

Wireless.chart.initChartPanel = function(c){
	if($('#' + c.divLeftShowChart).is(":visible")){
		c.leftChart.setSize(c.tabPanel.getWidth()*0.4, c.panelDrag ? c.tabPanel.getHeight() - c.cutAfterDrag : c.tabPanel.getHeight()-c.cutBeforeDrag);
		c.rightChart.setSize(c.tabPanel.getWidth()*0.6, c.panelDrag ? c.tabPanel.getHeight() - c.cutAfterDrag : c.tabPanel.getHeight()-c.cutBeforeDrag);				
	}else{
		$('#'+c.generalName+'DivChartChange').show();
		$('#'+c.divLeftShowChart).show();
		$('#'+c.divRightShowChart).show();
	}
	
	if(!c.leftChart || !isEmptyObject(c.leftChart)){
		c.getChartData();
		c.leftChart = c.leftChartLoad(c.loadType);
		c.rightChart = c.rightChartLoad(c.loadType);
		c.leftChart.setSize(c.tabPanel.getWidth()*0.4, c.panelDrag ? c.tabPanel.getHeight() - c.cutAfterDrag : c.tabPanel.getHeight()-c.cutBeforeDrag);
		c.rightChart.setSize(c.tabPanel.getWidth()*0.6, c.panelDrag ? c.tabPanel.getHeight() - c.cutAfterDrag : c.tabPanel.getHeight()-c.cutBeforeDrag);
	}
	
	return {pie:c.leftChart, column:c.rightChart};
};


function newPieChart(c){
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
	        }
	    },
	    series: [c.series],
        credits : {
        	enabled : false
        }
	});	

}
function newColumnChart(c){
	return  new Highcharts.Chart({
        chart: {
            type: 'column',
            renderTo : c.rt
        },
        title: {
            text: c.title
        },
        xAxis: {
            categories: c.xAxis
        },
        yAxis: {
            min: 0,
            title: {
                text: c.yAxis
            }
        },
        tooltip: {
            pointFormat: '<table><tbody><tr><td style="color:red;padding:0">{series.name}: </td><td style="padding:0"><b>{point.y} </b></td></tr></tbody></table>',
            shared: true,
            useHTML: true
        },
        plotOptions: {
            column: {
                pointPadding: 0.2,
                borderWidth: 0
            }
        },
        series: [c.series],
        credits : {
        	enabled : false
        }
    });	
}


Wireless.chart.initChartData = function(c){
	return {chartPriceData : {type : 'pie', name : '比例', data : []}, 
				chartAmountData : {type : 'pie', name : '比例', data : []},
				
				priceColumnChart : {xAxis : [], 
				yAxis : {name : c.priceName, data : [],
				dataLabels: {
		            enabled: true,
		            color: 'green',
		            align: 'center',
		            style: {
		                fontSize: '13px',
		                fontFamily: 'Verdana, sans-serif',
		                fontWeight : 'bold'
		            },
		            format: '{point.y} 元'
		        }}},
		        
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

function resetChartDate(chartData){
	chartData.chartPriceData.data = [];
	chartData.chartAmountData.data = [];
	chartData.priceColumnChart.xAxis = [];
	chartData.amountColumnChart.xAxis = [];
	chartData.priceColumnChart.yAxis.data = [];
	chartData.amountColumnChart.yAxis.data = [];	
}






















var foodModel = {
	isTemporary : 'isTemporary',
	id : 'id',
	foodName : 'foodName',
	unitPrice : 'unitPrice',
	count : 'count',
	tasteGroup : 'tasteGroup',
	kitchen : 'kitchen',
	isHangup : 'isHangup',
	cancelReason : 'cancelReason'
};


var orderDataModel = {
	tableAlias : null,
	customNum : null,
	orderFoods : null,
	categoryValue : null,
	id : null,
	orderDate : null
};

/**
 * 清除为空的字段
 * @param {} obj
 * @return {}
 */
Wireless.ux.commitOrderData = function(obj){
	for(var s in obj){
		if(typeof obj[s] == 'object' && obj[s] != null){
			Wireless.ux.commitOrderData(obj[s]);
		}else if (obj[s] == null){
			delete obj[s];
		};
	}
	return obj;
};

Wireless.ux.createOrder = function(c){
	
	var foodPara = '';
	var temp = null;
	for ( var i = 0; i < c.orderFoods.length; i++) {
		temp = c.orderFoods[i];
		foodPara += ( i > 0 ? '<<sh>>' : '');
		if (temp.isTemporary) {
			// 临时菜
			var foodName = temp.name;
			foodName = foodName.indexOf('<') > 0 ? foodName.substring(0,foodName.indexOf('<')) : foodName;
			foodPara = foodPara 
					+ '[' 
					+ 'true' + '<<sb>>'// 是否临时菜(true)
					+ temp.id + '<<sb>>' // 临时菜1编号
					+ foodName + '<<sb>>' // 临时菜1名称
					+ temp.count + '<<sb>>' // 临时菜1数量
					+ temp.unitPrice + '<<sb>>' // 临时菜1单价(原料單價)
					+ (typeof temp.isHangup != 'undefined' ?  temp.isHangup : false) +'<<sb>>' // 菜品状态,暂时没用
					+ (typeof temp.dataType != 'undefined'? temp.dataType : c.dataType) + '<<sb>>' // 菜品操作状态 1:已点菜 2:新点菜 3:反结账
					+ temp.kitchen.id + '<<sb>>'	// 临时菜出单厨房
					+ (typeof temp.cancelReason != 'undefined' ?  temp.cancelReason : 0) //退菜原因
					+ ']';
		}else{
			// 普通菜
			var normalTaste = '', tmpTaste = '' , tasteGroup = temp.tasteGroup;
			for(var j = 0; j < tasteGroup.normalTasteContent.length; j++){
				var t = tasteGroup.normalTasteContent[j];
				normalTaste += ((j > 0 ? '<<stnt>>' : '') + (t.id + '<<stb>>' + t.cateValue + '<<stb>>' + t.cateStatusValue));
			}
			if(tasteGroup.tmpTaste != null && typeof tasteGroup.tmpTaste != 'undefined'){
				if(eval(tasteGroup.tmpTaste.id >= 0))
					tmpTaste = tasteGroup.tmpTaste.price + '<<sttt>>' + tasteGroup.tmpTaste.name  + '<<sttt>>' + tasteGroup.tmpTaste.id+ '<<sttt>>' + tasteGroup.tmpTaste.alias; 				
			}
			foodPara = foodPara 
					+ '['
					+ 'false' + '<<sb>>' // 是否临时菜(false)
					+ temp.id + '<<sb>>' // 菜品1编号
					+ temp.count + '<<sb>>' // 菜品1数量
					+ (normalTaste + ' <<st>> ' + tmpTaste) + '<<sb>>'
					+ temp.kitchen.id + '<<sb>>'// 厨房1编号
					+ '1' + '<<sb>>' // 菜品1折扣
//					+ temp.dataType  // 菜品操作状态 1:已点菜 2:新点菜 3:反结账
					+ (typeof temp.isHangup != 'undefined' ?  temp.isHangup : false) + '<<sb>>'
					+ (typeof temp.cancelReason != 'undefined' ?  temp.cancelReason : 0) //退菜原因
					+ ']';
		}
	}	
	temp = null;
	return '{' + foodPara + '}';
};
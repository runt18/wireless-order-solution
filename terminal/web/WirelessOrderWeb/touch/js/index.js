$(function(){
	
	$.getScript('./js/tableSelect/tsLoad.js');
	
	$.getScript('./js/tableSelect/tsMain.js');
	
	$.getScript('./js/createOrder/coLoad.js');
	
	$.getScript('./js/createOrder/coMain.js');
});

/**
 * 
 * @param c
 */
function toggleContentDisplay(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	var el = $('#'+c.renderTo);
	if(!el){return;}
	if($.trim(c.type) == 'show'){
		if(el.hasClass('content-hide')){
			el.removeClass('content-hide');
		}
		el.addClass('content-show');
	}else if($.trim(c.type) == 'hide'){
		el.addClass('content-hide');
	}
}
//返回指定格式的日期时间函数
function myDate(){  
    var date = new Date();
    var weekday = ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"];                          
    var year = date.getFullYear() + "年";
    var month = ((date.getMonth() + 1) < 10 ? "0" + (date.getMonth() + 1):  (date.getMonth() + 1))+ "月";
    var today = (date.getDate() < 10 ? "0"+date.getDate() :  date.getDate()) + "日";
    var week = "(" + weekday[date.getDay()] + ")";
    var time = date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();       
    var myDate = year + " " + month + " " + today + "  " + time + " " + week;
    return myDate;
} 
//存放账单菜品数组
var uoFood = [];
//存放退菜的数组
var uoCancelFoods = [];
//存放菜品相关信息
var uoOther;
//存放行号id
var selectigRow = "";
//存放退菜原因数据
var cancelReasonData = [];
//数字键盘输入框的显示值
var inputNumValUO = "";
//数字键盘输入框id
var inputNumIdUO;
//存放退菜数目
var count;
//选中的退菜原因
//var selectingReasonName = "";
//选中的退菜id
var selectingReasonId = "";
var selectingCancelReason;

$(function(){
	
});

/**
 * 初始化菜单数据，存放在uoFood数组中
 * @param {object} data 餐桌对象
 */
function initOrderData(c){
	// 加载菜单数据
	$.ajax({
		url : '../QueryOrder.do',
		type : 'post',
		data : {
			restaurantID : restaurantID,
			tableID : c.table.alias,			
		},
//		async : false,
		success : function(data, status, xhr){
			uoFood = [];
			if(data.success){
				uo.order = data.other.order;
				uo.table = c.table;
				uo.order.orderFoods = data.root;
				for(x in data.root){
					uoFood.push(data.root[x]);
				}
				uoOther = data.other;
				showNorthForUpdateOrder();
				showOrder();
				showDescForUpdateOrder();
			    c.createrOrder == 'createrOrder' ? co.show({
					table : uo.table,
					order : uo.order,
					callback : function(){
						initTableData();
					}}) : null;
			}else{
				Util.msg.alert({
					title : data.title,
					msg : data.msg, 
					time : 3,
				});
			}
		},
		error : function(request, status, err){
			Util.msg.alert({
				title : '温馨提示',
				msg : '初始化菜单数据失败.', 
				time : 3,
			});
		}
	});	
}

/**
 * 初始化退菜原因数据，存在cancelReasonData数组中
 */
function initCancelReason(){
	$.ajax({
		url : '../QueryCancelReason.do',
		type : 'post',
		dataType : 'json',
		data : {
			restaurantID : restaurantID,
		},
		success : function(data, status, xhr){
			cancelReasonData = data.root;
		},
		error : function(request, status, err){
			Util.msg.alert({
				title : '温馨提示',
				msg : '初始化退菜原因失败.', 
				time : 3,
			});
		}
	});
}

/**
 * 初始化页头信息（账单号，餐台号，餐台名，用餐人数）
 */
function showNorthForUpdateOrder(){
	var html = "";
	var tableName;
	if(uoOther.order.table.name == ""){
		tableName = uoOther.order.table.alias + "号桌";
	}else{
		tableName = uoOther.order.table.name;
	}
	var customNum = uoOther.order.customNum;
	html = "<div><span style = 'margin : 10px 250px 10px 10px; font-size : 24px; color : red; font-weight : bold;'>已点菜页面</span>" +
			"<span style = 'margin: 10px;'>账单号：" + uoOther.order.id + " </span>" +
			"<span style = 'margin: 10px;'>餐台号：" + uoOther.order.table.alias + " </span>" +
			"<span style = 'margin: 10px;'>餐台名： " + tableName + "</span>" +
			"<span style = 'margin: 10px;' id = 'customNumForUO'>用餐人数：" + customNum + "</span>" +			
		"</div>";
	$("#divNorthForUpdateOrder").html(html);
}

/**
 * 初始化菜单信息
 */
function showOrder(){
	var html = "<tr>" +
				"<th style = 'width: 4%'></th>" +
				"<th style = 'width: 24%'>菜名</th>" +
				"<th style = 'width: 6%'>数量</th>" +
				"<th style = 'width: 22%'>口味</th>" +
				"<th style = 'width: 6%'>单价</th>" +
				"<th style = 'width: 6%'>总价</th>" +
				"<th style = 'width: 10%'>时间</th>" +
				"<th>操作</th>" +
				"<th>服务员</th>" +
			"</tr>";
	for(var i = 0; i < uoFood.length; i++){
		html += Templet.uo.orderFood.format({
			dataIndex : i + 1,
			id : uoFood[i].id,
			name : uoFood[i].name + (uoFood[i].isGift?'&nbsp;[<font style="font-weight:bold;">已赠送</font>]':''),
			count : uoFood[i].count.toFixed(2),
			tastePref : uoFood[i].tasteGroup.tastePref,
			actualPrice : (uoFood[i].actualPrice + uoFood[i].tasteGroup.tastePrice).toFixed(2),
			totalPrice : uoFood[i].totalPrice.toFixed(2),
			orderDateFormat : uoFood[i].orderDateFormat,
			waiter : uoFood[i].waiter 
		});
	}
	$("#divCenterForUpdateOrder table").html(html);
	//为退菜按钮绑定退菜事件
	$(".cancelFoodBtn").bind("click", function(){
		cancelFood(this);
	});
}

/**
 * 初始化页尾信息（菜品数量，消费总额）
 */
function showDescForUpdateOrder(){
	var html = "";
	html = "<div>" +
	"<span style = 'margin-left: 500px;'>菜品数量：" + uoFood.length + "</span>" +
	"<span style = 'margin-left: 50px;'>消费总额：</span>" + "<span id = 'spanTotalPriceUO'></span>" +	
	"</div>";
	$("#divDescForUpdateOrder").html(html);
	$("#spanTotalPriceUO").html(uo.getTotalPriceUO().toFixed(2) + "元");
}

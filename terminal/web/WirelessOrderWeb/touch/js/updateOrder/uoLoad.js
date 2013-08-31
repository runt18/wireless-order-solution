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

/**
 * 初始化菜单数据，存放在uoFood数组中
 * @param {object} data 餐桌对象
 */
function initOrderData(data){
	// 加载菜单数据
	$.ajax({
		url : '../QueryOrder.do',
		type : 'post',
		data : {
			pin : pin,
			restaurantID : restaurantID,
			tableID : data.alias,			
		},
		success : function(data, status, xhr){
			uoFood = [];
			if(data.success){
				for(x in data.root){
					uoFood.push(data.root[x]);
				}
				uoOther = data.other;
				showNorthForUpdateOrder();
				showOrder();
				showDescForUpdateOrder();
			}else{
				alert('初始化菜单数据失败.');
			}
		},
		error : function(request, status, err){
			alert('初始化菜单数据失败.');
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
		data : {
			pin : pin,
			restaurantID : restaurantID,
		},
		success : function(data, status, xhr){
			cancelReasonData = [];
			data = JSON.parse(data);
			for(x in data.root){
				cancelReasonData.push(data.root[x]);
			}
		},
		error : function(request, status, err){
			alert('初始化退菜原因失败.');
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
	html = "<div>" +
			"<span style ='margin: 10px;'>账单号：" + uoOther.order.id + " </span>" +
			"<span style ='margin: 10px;'>餐台号：" + uoOther.order.table.alias + " </span>" +
			"<span style ='margin: 10px;'>餐台名： " + tableName + "</span>" +
			"<span style ='margin: 10px;' id = 'customNumForUO'>用餐人数：" + customNum + "</span>" +			
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
			alias : uoFood[i].alias,
			name : uoFood[i].name,
			count : uoFood[i].count.toFixed(2),
			tastePref : uoFood[i].tasteGroup.tastePref,
			actualPrice : uoFood[i].actualPrice.toFixed(2),
			totalPrice : uoFood[i].totalPrice.toFixed(2),
			orderDateFormat : uoFood[i].orderDateFormat,
			waiter : uoFood[i].waiter 
		});
	}
	$("#divCenterForUpdateOrder table").html(html);
	
	//设置鼠标移到退菜按钮上的移进移出效果
	$(".cancelFoodBtn").mouseover(function(){
		$(this).css("backgroundColor", "#FFD700");
	});
	$(".cancelFoodBtn").mouseout(function(){
		$(this).css("backgroundColor", "#75B2F4");
	});	
	
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
	$("#spanTotalPriceUO").html(getTotalPriceUO().toFixed(2) + "元");
}

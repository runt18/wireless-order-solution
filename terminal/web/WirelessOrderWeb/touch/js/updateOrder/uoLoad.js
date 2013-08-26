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
				alert('初始化菜品数据失败.');
			}
		},
		
		error : function(request, status, err){
			alert('初始化菜品数据失败.');
		}
	});	
}

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

//计算消费总额
function getTotalPriceUO(){
	var totalPriceUO = 0;
	for(x in uoFood){
		totalPriceUO += uoFood[x].count * uoFood[x].actualPrice;
	}
	return totalPriceUO;
}
function selectUOFood(o){
	selectigRow = o.id;
}
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
			"<span style ='margin: 10px;' id = 'customNumForUO'>用餐人数：" + customNum + " </span>" +			
		"</div>";
	$("#divNorthForUpdateOrder").html(html);
}
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
//	var n = 1;
//	for(x in uoFood){
//		html += "<tr id = 'truoFood" + n + "' onclick = 'selectUOFood(this)'>" + 
//				"<td>" + n + "</td>" +
//				"<td>" + uoFood[x].name + "</td>" +
//				"<td>" + uoFood[x].count.toFixed(1) + "</td>" +
//				"<td>" + uoFood[x].tasteGroup.tastePref + "</td>" +
//				"<td>" + uoFood[x].actualPrice.toFixed(2) + "</td>" +
//				"<td>" + uoFood[x].totalPrice.toFixed(2) + "</td>" +
//				"<td>" + uoFood[x].orderDateFormat + "</td>" +
//				"<td><input type = 'button' value = '退菜' " + 
//				"class = 'cancelFoodBtn' id = 'btnuo" + n + "' /></td>" +
//				"<td>" + uoFood[x].waiter + "</td></tr>";
//		n++;
//	}
	for(var i = 0; i < uoFood.length; i++){
		html += Templet.uo.orderFood.format({
			dataIndex : i,
			id : uoFood[i].id,
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
	
	$(".cancelFoodBtn").bind("click", function(){
		cancelFood(this);
	});
}
function showDescForUpdateOrder(){
	var html = "";
	html = "<div>" +
	"<span style = 'margin-left: 500px;'>菜品数量：" + uoFood.length + "</span>" +
	"<span style = 'margin-left: 50px;'>消费总额：</span>" + "<span id = 'spanTotalPriceUO'>" + "</span>" +	
	"</div>";
	$("#divDescForUpdateOrder").html(html);
	$("#spanTotalPriceUO").html(getTotalPriceUO() + "元");
}
function findFoodByAlias(c){
	var food = null;
	for(x in uoFood){
		if(uoFood[x].alias == c){
			food = uoFood;
			break;
		}
	}
	return food;
}





















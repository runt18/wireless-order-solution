

/**
 * 初始化菜品数据
 */
function initFoodData(){
	// 加载菜品数据
	$.ajax({
		url : '../QueryMenu.do',
		type : 'post',
		data : {
			dataSource : 'foods',
			pin : pin,
			restaurantID : restaurantID
		},
		success : function(data, status, xhr){
			if(data.success){
				for(var i = 0; i < data.root.length; i++){
					data.root[i].kitchenId = data.root[i].kitchen.id;
					data.root[i].deptId = data.root[i].kitchen.dept.id;
					delete data.root[i].kitchen;
				}
				localStorage.setItem('foods', JSON.stringify(data));
				foodData = data;
				
				$.getScript('./js/createOrder/coLoad.js');
				$.getScript('./js/createOrder/coMain.js');
				
				// 加载部门数据,分厨数据
				$.ajax({
					url : '../QueryMenu.do',
					type : 'post',
					data : {
						dataSource : 'kitchens',
						pin : pin,
						restaurantID : restaurantID
					},
					success : function(data, status, xhr){
						if(data.success){
							kitchenData = {totalProperty:data.root.length, root:data.root.slice(0)};
							kitchenFoodData = {totalProperty:data.root.length, root:data.root.slice(0)};
							var tempFoodData = foodData.root.slice(0);
							deptData.root.push(kitchenData.root[0].dept);
							for(var j = 0; j < kitchenFoodData.root.length; j++){
								var temp = kitchenFoodData.root[j];
								temp.foods = [];
								for(var i = 0; i < tempFoodData.length; i++){
									if(tempFoodData[i].kitchenId == temp.id){
										temp.foods.push(tempFoodData[i]);
									}
								}
								var hasDept = false;
								for(var i = 0; i < deptData.root.length; i++){
									if(deptData.root[i].id == temp.dept.id){
										hasDept = true;
									}
								}
								if(!hasDept){
									deptData.root.push(temp.dept);
								}
							}
							deptData.totalProperty = deptData.root.length;
							deptData.root.unshift({
								id : -1,
								name : '全部部门'
							});
							kitchenData.root.unshift({
								id : -1,
								name : '全部分厨',
								dept : {
									id : -1
								},
								foods : foodData.root.slice(0)
							});
							kitchenFoodData.root.unshift({
								id : -1,
								name : '全部分厨',
								dept : {
									id : -1
								},
								foods : foodData.root.slice(0)
							});
							// 清理多余数据
							for(var i = kitchenFoodData.root.length - 1; i >= 0; i--){
								if(kitchenFoodData.root[i].foods.length <= 0){
									for(var k = kitchenData.root.length - 1; k >= 0; k--){
										if(kitchenData.root[k].id == kitchenFoodData.root[i].id){
											kitchenFoodData.root.splice(i, 1);
											kitchenData.root.splice(k, 1);
											break;
										}
									}
								}
							}
							localStorage.setItem('dept', JSON.stringify(deptData));
							localStorage.setItem('kitchen', JSON.stringify(kitchenData));
						}else{
							alert('初始化分厨数据失败.');
						}
					},
					error : function(request, status, err){
						alert('初始化分厨数据失败.');
					}
				});
			}else{
				alert('初始化菜品数据失败.');
			}
		},
		error : function(request, status, err){
			alert('初始化菜品数据失败.');
		}
	});
}

/**
 * onload
 */
$(function(){
	initFoodData();
	
	$.getScript('./js/tableSelect/tsLoad.js');
	
	$.getScript('./js/tableSelect/tsMain.js');
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
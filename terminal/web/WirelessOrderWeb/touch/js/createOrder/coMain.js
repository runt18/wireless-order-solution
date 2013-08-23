/**
 * 分厨选菜
 */
cr.findFoodByKitchen = function(c){
	c = c == null || typeof c == 'undefined' ? {} : c;
	//
	var sl = $('#divSelectKitchenForOrder > div[data-type=kitchen-select]');
	for(var i = 0; i < sl.length; i++){
		$(sl[i]).removeClass('div-deptOrKitchen-select');
	}
	$(c.event).addClass('div-deptOrKitchen-select');
	
	var tempFoodData = [];
	var temp = null;
	if(c.kitchenId == -1){
		var dl = $('.div-deptOrKitchen-select[data-type=dept-select]');
		if(dl.length == 0){
			for(var i = 0; i < kitchenData.root.length; i++){
				tempFoodData = tempFoodData.concat(kitchenData.root[i].foods);
			}
		}else{
			for(var i = 0; i < kitchenData.root.length; i++){
				temp = kitchenData.root[i];
				if(temp.dept.id == parseInt(dl[0].getAttribute('data-value'))){
					tempFoodData = tempFoodData.concat(temp.foods);					
				}
			}
		}
	}else{
		for(var i = 0; i < kitchenData.root.length; i++){
			temp = kitchenData.root[i];
			if(typeof c.kitchenId == 'number' && c.kitchenId != -1){
				if(temp.id == c.kitchenId){
					tempFoodData = tempFoodData.concat(temp.foods);
				}
			}else{
				tempFoodData.concat();
			}
		}
	}
	temp = null;
	// 
	cr.fd.init({
		data : tempFoodData
	});
	cr.fd.getFirstPage();
};
/**
 * 
 */
cr.show = function(c){
	toggleContentDisplay({
		type:'show', 
		renderTo:'divCreateOrder'
	});
	cr.initDeptContent();
	var defaults = $('#divSelectDeptForOrder > div[data-value=-1]');
	defaults[0].click();
	
	$('#divCFCONewFood').css('height', $('#divCenterForCreateOrde').height());
	
	//alert(c.table)
};
/**
 * 
 */
cr.hide = function(c){
	toggleContentDisplay({
		type:'hide', 
		renderTo:'divCreateOrder'
	});
};
/**
 * 添加菜品
 */
cr.addFood = function(){
	cr.operateFoodCount({
		count : 1
	});
};
/**
 * 减少菜品
 */
cr.cutFood = function(){
	cr.operateFoodCount({
		count : -1
	});
};
/**
 * 删除菜品
 */
cr.deleteFood = function(){
	cr.operateFoodCount({
		otype : 'delete'
	});
};
/**
 * 操作菜品数量
 */
cr.operateFoodCount = function(c){
	var foodContent = $('#divCFCONewFood > div[class*=div-newFood-select]');
//	alert(foodContent.length)
	if(foodContent.length != 1){
		alert('请选中一道菜品')
		return;
	}
	var data = cr.newFood[foodContent.attr('data-index')];
	if(typeof c.otype == 'string'){
		if(c.otype.toLowerCase() == 'delete'){
			cr.newFood.splice(foodContent.attr('data-index'), 1);
		}else if(c.otype.toLowerCase() == 'set'){
			data.count = c.count;
		}
	}else{
		var nc = data.count + c.count;
		if(nc <= 0){
			cr.newFood.splice(foodContent.attr('data-index'), 1);
		}else{
			data.count = nc;
		}
	}
	//
	cr.initNewFoodContent({
		data : data
	});
};

/**
 * 口味操作
 */
cr.operateFoodTaste = function(c){
//	alert('cr.operateFoodTaste ')
	var t = $('#divOperateBoxForFoodTaste');
	t.addClass('dialong-show');
//	t.removeClass('dialong-show');
//	toggleContentDisplay({
//		type:'show', 
//		renderTo:'divOperateBoxForFoodTaste'
//	});
};


function aaaaaaa(){
	$('#divOperateBoxForFoodTaste').addClass("dialong-hide")
}


























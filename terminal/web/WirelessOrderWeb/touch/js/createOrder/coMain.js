/**
 * 分厨选菜
 */
co.findFoodByKitchen = function(c){
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
	co.fp.init({
		data : tempFoodData
	});
	co.fp.getFirstPage();
};
/**
 * 
 */
co.show = function(c){
	toggleContentDisplay({
		type:'show', 
		renderTo:'divCreateOrder'
	});
	co.initDeptContent();
	var defaults = $('#divSelectDeptForOrder > div[data-value=-1]');
	defaults[0].click();
	
	$('#divCFCONewFood').css('height', $('#divCenterForCreateOrde').height());
	
	//alert(c.table)
};
/**
 * 
 */
co.hide = function(c){
	toggleContentDisplay({
		type:'hide', 
		renderTo:'divCreateOrder'
	});
};
/**
 * 添加菜品
 */
co.addFood = function(){
	co.operateFoodCount({
		count : 1
	});
};
/**
 * 减少菜品
 */
co.cutFood = function(){
	co.operateFoodCount({
		count : -1
	});
};
/**
 * 删除菜品
 */
co.deleteFood = function(){
	co.operateFoodCount({
		otype : 'delete'
	});
};
/**
 * 操作菜品数量
 */
co.operateFoodCount = function(c){
	var foodContent = $('#divCFCONewFood > div[class*=div-newFood-select]');
	if(foodContent.length != 1){
		alert('请选中一道菜品');
		return;
	}
	var data = co.newFood[foodContent.attr('data-index')];
	if(typeof c.otype == 'string'){
		if(c.otype.toLowerCase() == 'delete'){
			co.newFood.splice(foodContent.attr('data-index'), 1);
		}else if(c.otype.toLowerCase() == 'set'){
			data.count = c.count;
		}
	}else{
		var nc = data.count + c.count;
		if(nc <= 0){
			co.newFood.splice(foodContent.attr('data-index'), 1);
		}else{
			data.count = nc;
		}
	}
	//
	co.initNewFoodContent({
		data : data
	});
};


/*** -------------------------------------------------- ***/

/**
 * 口味操作
 */
co.ot.show = function(){
	var sf = $('#divCFCONewFood > div[class*=div-newFood-select]');
	if(sf.length != 1){
		alert('请选中一道菜品');
		return;
	}
	var data = co.newFood[sf.attr('data-index')];
	
	Util.dialongDisplay({
		type : 'show',
		renderTo : 'divOperateBoxForFoodTaste'
	});
	
	if(typeof co.ot.ctp == 'undefined'){
		co.ot.initBarForCommomTaste();
	}
	if(typeof co.ot.atp == 'undefined'){
		co.ot.initBarForAllTaste();
	}
	if(typeof co.ot.ggp == 'undefined'){
		co.ot.initBarForGuiGe();
	}
	
	co.ot.changeTaste({
		foodData : data,
		type : 1,
		event : $('#divCFOTTasteChange > div[data-value=1]'),
		change : true
	});
};

/**
 * 
 */
co.ot.changeTaste = function(c){
	if(c == null || typeof c.type != 'number'){
		return;
	}
	var ac = $('#divCFOTTasteChange > div');
	for(var i = 0; i < ac.length; i++){
		$(ac[i]).removeClass('div-deptOrKitchen-select');
	}
	$('#divCFOTTasteChange > div[data-value='+c.type+']').addClass('div-deptOrKitchen-select');
	
	if(c.type == 1){
		// 常用口味
		co.ot.tp = co.ot.ctp;
		if(typeof c.foodData == 'undefined'){
			c.foodData = co.newFood[$('#divCFCONewFood > div[class*=div-newFood-select]').attr('data-index')];
		}
		$.ajax({
			url : '../QueryFoodTaste.do',
			type : 'post',
			data : {
				foodID : c.foodData.id,
				pin : pin,
				restaurantID : restaurantID
			},
			success : function(data, status, xhr){
				if(data.success && data.root.length > 0){
					co.ot.ctp.init({
						data : data.root.slice(0)
					});
					co.ot.tp.getFirstPage();
				}else{
					co.ot.tp.clearContent();
					if(c.change){
						co.ot.changeTaste({
							type : 2,
							foodData : c.foodData
						});
					}
				}
			},
			error : function(request, status, err){
				alert('加载菜品常用口味数据失败.');
			}
		});
	}else if(c.type == 2){
		// 所有口味
		co.ot.tp = co.ot.atp;
		co.ot.tp.getFirstPage();
	}else if(c.type == 3){
		// 规格
		co.ot.tp = co.ot.ggp;
		co.ot.tp.getFirstPage();
	}
};





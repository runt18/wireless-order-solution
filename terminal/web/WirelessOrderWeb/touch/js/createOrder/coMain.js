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
	
	co.table = c.table;
	co.order = typeof c.order != 'undefined' ? c.order : null;
	co.callback = typeof c.callback == 'function' ? c.callback : null;
	$('#divNFCOTableBasicMsg').html('<div>{alias}</div><div>{name}</div>'.format({
		alias : co.table.alias,
		name : co.table.name
	}));
};
/**
 * 菜品操作返回
 */
co.back = function(c){
	toggleContentDisplay({
		type:'hide', 
		renderTo:'divCreateOrder'
	});
	//
	co.table = null;
	co.newFood = [];
	co.callback = null;
	$('#divCFCONewFood').html('');
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
		Util.msg.alert({
			msg : '请选中一道菜品'
		});
		return;
	}
	var data = co.newFood[foodContent.attr('data-index')];
	if(typeof c.otype == 'string'){
		if(c.otype.toLowerCase() == 'delete'){
			Util.msg.alert({
				title : '重要',
				msg : '是否删除菜品?',
				button : 'YESBACK',
				fn : function(btn){
					if(btn == 'yes'){
						co.newFood.splice(foodContent.attr('data-index'), 1);
						co.initNewFoodContent({
							data : data
						});
					}
				}
			});
			return;
		}else if(c.otype.toLowerCase() == 'set'){
			data.count = c.count;
		}
	}else{
		var nc = data.count + c.count;
		if(nc <= 0){
			co.operateFoodCount({
				otype : 'delete'
			});
		}else{
			data.count = nc;
		}
	}
	//
	co.initNewFoodContent({
		data : data
	});
};
/**
 * 叫起
 * params
 *  type: 1:全单叫起 2:单个叫起
 */
co.foodHangup = function(c){
	if(c == null || typeof c.type != 'number'){
		return;
	}
	if(c.type == 1){
		var isHangup = true;
		for(var i = 0; i < co.newFood.length; i++){
//			co.newFood[i].isHangup = typeof co.newFood[i].isHangup != 'boolean' ? true : co.newFood[i].isHangup ? co.newFood[i].isHangup : !co.newFood[i].isHangup;
			if(i == 0){
				isHangup = typeof co.newFood[i].isHangup != 'boolean' ? true : !co.newFood[0].isHangup;
			}
			co.newFood[i].isHangup = isHangup;
		}
		co.initNewFoodContent();
	}else if(c.type == 2){
		var foodContent = $('#divCFCONewFood > div[class*=div-newFood-select]');
		if(foodContent.length != 1){
			Util.msg.alert({
				msg : '请选中一道菜品'
			});
			return;
		}
		var data = co.newFood[foodContent.attr('data-index')];
		data.isHangup = typeof data.isHangup != 'boolean' ? true : !data.isHangup;;
		co.initNewFoodContent({
			data : data
		});
	}
	
};

/*** -------------------------------------------------- ***/

/**
 * 口味操作
 */
co.ot.show = function(){
	var sf = $('#divCFCONewFood > div[class*=div-newFood-select]');
	if(sf.length != 1){
		Util.msg.alert({
			msg : '请选中一道菜品'
		});
		return;
	}
	var foodData = co.newFood[sf.attr('data-index')];
	co.ot.foodData = foodData;
	
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
		co.ot.initBarForCate();
	}
	co.ot.changeTaste({
		foodData : co.ot.foodData,
		type : 1,
		event : $('#divCFOTTasteChange > div[data-value=1]'),
		change : true
	});
	//
	co.ot.newTaste = typeof co.ot.foodData.tasteGroup == 'undefined' ? [] : co.ot.foodData.tasteGroup.normalTasteContent.slice(0);
	co.ot.initNewTasteContent();
};
/**
 * 口味操作返回
 */
co.ot.back = function(){
	Util.dialongDisplay({
		type:'hide', 
		renderTo:'divOperateBoxForFoodTaste'
	});
	co.ot.foodData = null;
	co.ot.newTaste = [];
	$('#divCFOTHasTasteContent').html('');
};

/**
 * 切换口味选择
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
		$.ajax({
			url : '../QueryFoodTaste.do',
			type : 'post',
			data : {
				foodID : co.ot.foodData.id,
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
		co.ot.tp = co.ot.catep;
		co.ot.tp.getFirstPage();
	}
};

/**
 * 添加新口味
 */
co.ot.insertTaste = function(c){
	if(c == null || typeof c.tasteId != 'number'){
		return;
	}
	var has = false;
	var data = {};
	for(var i = 0; i < co.ot.tp.getPageData().length; i++){
		if(co.ot.tp.getPageData()[i].taste.id == c.tasteId){
			data = co.ot.tp.getPageData()[i].taste;
			// 
			for(var j = 0; j < co.ot.newTaste.length; j++){
				if(co.ot.newTaste[j].id == data.id){
					has = true;
					break;
				}
			}
			break;
		}
	}
	if(!has){
		co.ot.newTaste.push(data);
		data = null;
	}
	// 
	co.ot.initNewTasteContent();
};
/**
 * 删除口味
 */
co.ot.deleteTaste = function(c){
	if(c == null || typeof c.tasteId != 'number'){
		return;
	}
	for(var i = 0; i < co.ot.newTaste.length; i++){
		if(co.ot.newTaste[i].id == c.tasteId){
			co.ot.newTaste.splice(i, 1);
			break;
		}
	}
	//
	co.ot.initNewTasteContent();
};

/**
 * 保存口味
 */
co.ot.save = function(c){
	var tasteGroup = co.ot.foodData.tasteGroup;
	
	if(typeof tasteGroup == 'undefined'){
		tasteGroup = {
			normalTaste : {}
		};
	}
	if(typeof tasteGroup.normalTaste == 'undefined'){
		tasteGroup.normalTaste = {};
	}
	
	tasteGroup.normalTaste.name = '';
	tasteGroup.normalTaste.price = 0;
	tasteGroup.normalTasteContent = [];
	
	var temp = null;
	for(var i = 0; i < co.ot.newTaste.length; i++){
		temp = co.ot.newTaste[i];
		tasteGroup.normalTasteContent.push(temp);
		tasteGroup.normalTaste.name += (i > 0 ? ',' + temp.name : temp.name);
		if(temp.cateValue == 0){
			tasteGroup.normalTaste.price += temp.price;
		}else if(temp.cateValue == 2){
			tasteGroup.normalTaste.price += co.ot.foodData.price * temp.rate;
		}
	}
	tasteGroup.tastePref = tasteGroup.normalTaste.name;
	for(var i = 0; i < co.newFood.length; i++){
		if(co.newFood[i].id == co.ot.foodData.id){
			co.newFood[i].tasteGroup = tasteGroup;
			break;
		}
	}
	co.initNewFoodContent({
		data : co.ot.foodData
	});
	//
	co.ot.back();
};

/*** -------------------------------------------------- ***/
/**
 * 账单提交
 */
co.submit = function(){
//	alert(JSON.stringify(co.order.orderFoods))
//	return;
	if(co.newFood == null || typeof co.newFood == 'undefined' || co.newFood.length == 0){
		Util.msg.alert({
			title : '温馨提示',
			msg : '请选择菜品后再继续操作.', 
			fn : function(btn){
				
			}
		});
		return;
	}
	
	var foodData = [], isFree = true;
	if(co.table.statusValue == 1){
		isFree = false;
		foodData = co.newFood.slice(0).concat(co.order.orderFoods.slice(0));
	}else{
		isFree = true;
		foodData = co.newFood.slice(0);
	}
//	alert('foodData.length:  '+foodData.length)
//	return;
	
	var foods = '';
	var item = null;
	for ( var i = 0; i < foodData.length; i++) {
		item = foodData[i];
		foods += ( i > 0 ? '<<sh>>' : '');
		if (item.isTemporary) {
			// 临时菜
			foods = foods 
					+ '['
					+ 'true' + '<<sb>>'
					+ item.alias + '<<sb>>'
					+ item.name + '<<sb>>'
					+ item.count + '<<sb>>'
					+ item.unitPrice + '<<sb>>'
					+ '<<sb>>'
					+ (typeof item.isHangup != 'boolean' ? false : item.isHangup) + '<<sb>>'
					+ item.kitchen.alias
					+ ']';
		}else{
			// 普通菜
			var normalTaste = '', tmpTaste = '' , tasteGroup = item.tasteGroup;
			for(var j = 0; j < tasteGroup.normalTasteContent.length; j++){
				var t = tasteGroup.normalTasteContent[j];
				normalTaste += ((j > 0 ? '<<stnt>>' : '') + (t.id + '<<stb>>' + t.alias + '<<stb>>' + t.cateValue));
			}
			if(tasteGroup.tmpTaste != null && typeof tasteGroup.tmpTaste != 'undefined'){
				if(eval(tasteGroup.tmpTaste.id >= 0))
					tmpTaste = tasteGroup.tmpTaste.price + '<<sttt>>' + tasteGroup.tmpTaste.name  + '<<sttt>>' + tasteGroup.tmpTaste.id+ '<<sttt>>' + tasteGroup.tmpTaste.alias; 				
			}
			foods = foods 
					+ '['
					+ 'false' + '<<sb>>'
					+ item.alias + '<<sb>>'
					+ item.count + '<<sb>>'
					+ (normalTaste + ' <<st>> ' + tmpTaste) + '<<sb>>'
					+ item.kitchen.alias + '<<sb>>'
					+ '1' + '<<sb>>'
					+ (typeof item.isHangup != 'boolean' ? false : item.isHangup)
					+ ']';
		}
	}
	item = null;
	foods = '{' + foods + '}';
	
	Util.LM.show();
	$.ajax({
		url : '../InsertOrder.do',
		type : 'post',
		data : {
			'pin' : pin,
			'tableID' : co.table.alias,
			'customNum' : co.table.customNum,
			'type' : isFree ? 1 : 2,
			'foods' : foods,
			'category' :  co.table.categoryValue
			,'orderID' : isFree ? '' : co.order.id
			,'orderDate' : isFree ? '' : co.order.orderDate
		},
		success : function(data, status, xhr) {
			Util.LM.hide();
			if (data.success == true) {
				Util.msg.alert({
					title : data.title,
					msg : data.msg, 
					fn : function(btn){
						if(co.callback != null && typeof co.callback == 'function'){
							co.callback();
							co.back();
						}
					}
				});
			} else {
				Util.msg.alert({
					title : data.title,
					msg : data.msg, 
					fn : function(btn){
						
					}
				});
			}
		},
		error : function(request, status, err) {
			Util.LM.hide();
			Util.msg.alert({
				title : '错误',
				msg : err
			});
		}
	});
};


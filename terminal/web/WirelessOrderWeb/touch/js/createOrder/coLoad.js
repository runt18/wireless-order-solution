/**
 * 初始化新点菜区域
 */
co.initNewFoodContent = function(c){
	c = c == null ? {} : c;
	if(typeof c.record != 'undefined'){
		co.newFood.push(c.record);
		c.data = c.record;
	}
	var html = [], sumCount = 0, sumPrice = 0;
	var temp = null, tempUnitPrice = 0;
	for(var i = 0; i < co.newFood.length; i++){
		temp = co.newFood[i];
		tempUnitPrice = typeof temp.tasteGroup.price != 'number' ? 0 : parseFloat(temp.unitPrice + temp.tasteGroup.price);
		sumCount += temp.count;
		sumPrice += temp.count * tempUnitPrice;
		html.push(Templet.co.newFood.format({
			dataIndex : i,
			id : temp.id,
			name : temp.name,
			count : temp.count.toFixed(2),
			unitPrice : tempUnitPrice.toFixed(2),
			totalPrice : tempUnitPrice.toFixed(2),
			isHangup : typeof temp.isHangup == 'boolean' && temp.isHangup ? '叫起' : '',
			tasteDisplay : typeof temp.tasteGroup == 'undefined' 
				|| typeof temp.tasteGroup.normalTasteContent == 'undefined' 
					|| temp.tasteGroup.normalTasteContent.length <= 0 ? '' : temp.tasteGroup.tastePref
		}));
	}
	temp = null;
	tempUnitPrice = null;
	if(sumCount > 0){
		$('#divDescForCreateOrde div:first').html('总数量:{count}, 合计:{price}'.format({
			count : sumCount.toFixed(2),
			price : sumPrice.toFixed(2)
		}));		
	}else{
		$('#divDescForCreateOrde div:first').html('');
	}
	
	$('#divCFCONewFood').html(html.join(''));
	if(c.data != null && typeof c.data != 'undefined'){
		var select = $('#divCFCONewFood > div[data-value='+c.data.id+']');
		if(select.length > 0){
			select.addClass('div-newFood-select');
			getDom('divCFCONewFood').scrollTop = getDom('divCFCONewFood').scrollHeight / co.newFood.length * select.attr('data-index');
		}else{
			getDom('divCFCONewFood').scrollTop = 0;
		}
	}else{
		getDom('divCFCONewFood').scrollTop = 0;
	}
};

/**
 * 初始化部门选择
 * @param c
 */
co.initDeptContent = function(c){
	var dc = getDom('divSelectDeptForOrder');
	var html = '';
	for(var i = 0; i < deptData.root.length; i++){
		html += Templet.co.dept.format({
			value : deptData.root[i].id,
			text : deptData.root[i].name
		});
	}
	dc.innerHTML = html;
};
/**
 * 初始化分厨选择
 * @param c
 */
co.initKitchenContent = function(c){
	c = c == null || typeof c == 'undefined' ? {} : c;
	//
	var sl = $('div[data-type=dept-select]');
	for(var i = 0; i < sl.length; i++){
		$(sl[i]).removeClass('div-deptOrKitchen-select');
	}
	$(c.event).addClass('div-deptOrKitchen-select');
	// 
	var kc = getDom('divSelectKitchenForOrder');
	var html = Templet.co.kitchen.format({
		value : -1,
		text : '全部分厨'
	});
	var tempFoodData = []; // 菜品数据
	var temp = null;
	for(var i = 0; i < kitchenData.root.length; i++){
		temp = kitchenData.root[i];
		if(typeof c.deptId == 'number' && c.deptId != -1){
			if(temp.dept.id == c.deptId){
				html += Templet.co.kitchen.format({
					value : temp.id,
					text : temp.name
				});
				tempFoodData = tempFoodData.concat(temp.foods);
			}
		}else{
			if(temp.dept.id != -1){
				html += Templet.co.kitchen.format({
					value : temp.id,
					text : temp.name
				});
				tempFoodData = tempFoodData.concat(temp.foods);
			}
		}
	}
	temp = null;
	kc.innerHTML = html;
	//
	co.fp.init({
		data : tempFoodData
	});
	co.fp.getFirstPage();
};

/*** -------------------------------------------------- ***/

/**
 * 初始化常用口味数据操作工具
 */
co.ot.initBarForCommomTaste = function(){
	co.ot.ctp = new Util.padding({
		renderTo : 'divCFOTTasteSelectContent',
		displayId : 'divDescForOperateTaste-padding-msg',
		templet : function(c){
			return Templet.co.boxSelectTaste.format({
				dataIndex : c.dataIndex,
				id : c.data.taste.id,
				name : c.data.taste.name,
				mark : c.data.taste.cateStatusValue == 2 ? '¥' : c.data.taste.cateStatusValue == 1 ? '比例' : '',
				markText : c.data.taste.cateStatusValue == 2 ? c.data.taste.price : c.data.taste.cateStatusValue == 1 ? c.data.taste.rate : '0.00'
			});
		}
	});
};
/**
 * 初始化所有口味数据操作工具
 */
co.ot.initBarForAllTaste = function(){
	var data = [];
	for(var i = 0; i < tasteData.root.length; i++){
//		if(tasteData.root[i].taste.cateStatusValue == 1){
			data.push(tasteData.root[i]);
//		}
	}
	co.ot.atp = new Util.padding({
		data : data,
		renderTo : 'divCFOTTasteSelectContent',
		displayId : 'divDescForOperateTaste-padding-msg',
		templet : function(c){
			return Templet.co.boxSelectTaste.format({
				dataIndex : c.dataIndex,
				id : c.data.taste.id,
				name : c.data.taste.name,
				mark : c.data.taste.cateStatusValue == 2 ? '¥' : c.data.taste.cateStatusValue == 1 ? '比例' : '',
				markText : c.data.taste.cateStatusValue == 2 ? c.data.taste.price : c.data.taste.cateStatusValue == 1 ? c.data.taste.rate : '0.00'
			});
		}
	});
	data = null;
};
/**
 * 初始化口味组数据操作工具
 */
co.ot.initBarForTasteCategory = function(){
	var data = [];
	if(tasteData.root.length > 0){
		data.push({
			id : tasteData.root[0].taste.cateValue,
			name : tasteData.root[0].taste.cateText,
			items : []
		});
	}
	var has = true, temp = {};
	for(var i = 0; i < tasteData.root.length; i++){
		has = false;
		for(var k = 0; k < data.length; k++){
			if(tasteData.root[i].taste.cateValue == data[k].id){
				data[k].items.push(tasteData.root[i]);
				has = true;
				break;
			}
		}
		if(!has){
			temp = {
				id : tasteData.root[i].taste.cateValue,
				name : tasteData.root[i].taste.cateText,
				items : []
			};
			temp.items.push(tasteData.root[i]);
			data.push(temp);
		}
	}
	co.ot.tctp = new Util.padding({
		data : data,
		renderTo : 'divCFOTTasteSelectContent',
		displayId : 'divDescForOperateTaste-padding-msg',
		templet : function(c){
			return Templet.co.boxTasteCategory.format({
				dataIndex : c.dataIndex,
				id : c.data.id,
				name : c.data.name
			});
		}
	});
	data = null;
};

/**
 * 
 */
co.ot.initNewTasteContent = function(){
	var html = '', temp = null;
	for(var i = 0; i < co.ot.newTaste.length; i++){
		temp = co.ot.newTaste[i];
		html += Templet.co.boxNewTaste.format({
			id : temp.id,
			name : temp.name,
			mark : temp.cateStatusValue == 1 ? '¥' : temp.cateStatusValue == 2 ? '比例' : '',
			markText : temp.cateStatusValue == 1 ? temp.price : temp.cateStatusValue == 2 ? temp.rate : '0.00'
		});
	}
	temp = null;
	getDom('divCFOTHasTasteContent').innerHTML = html;
};

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
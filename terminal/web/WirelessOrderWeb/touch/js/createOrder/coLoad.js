
// 菜品分页包
co.fp = new Util.padding({
	renderTo : 'divCFCOAllFood',
	templet : function(c){
		return Templet.co.boxFood.format({
			dataIndex : c.dataIndex,
			id : c.data.id,
			name : c.data.name,
			unitPrice : c.data.unitPrice
		});
	}
});

/**
 * 初始化新点菜区域
 */
co.initNewFoodContent = function(c){
	var html = [];
	var temp = null;
	for(var i = 0; i < co.newFood.length; i++){
		temp = co.newFood[i];
		html.push(Templet.co.newFood.format({
			dataIndex : i,
			id : temp.id,
			name : temp.name,
			count : temp.count.toFixed(2),
			unitPrice : temp.unitPrice.toFixed(2),
			totalPrice : (temp.count * temp.unitPrice).toFixed(2),
			tasteDisplay : '口味一,口味二,口味三'
		}));
	}
	temp = null;
	$('#divCFCONewFood').html(html.join(''));
	if(c.data != null){
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
 * 点菜
 */
co.insertFood = function(c){
	if(c == null || typeof c.foodId != 'number'){
		return;
	}
	//
	var data = null;
	for(var i = 0; i < co.fp.getPageData().length; i++){
		if(co.fp.getPageData()[i].id == c.foodId){
			data = co.fp.getPageData()[i];
			break;
		}
	}
	if(data == null){
		alert('添加菜品失败, 程序异常, 请刷新后重试或联系客服人员');
		return;
	}
	//
	var has = false;
	for(var i = 0; i < co.newFood.length; i++){
		if(co.newFood[i].id == data.id){
			has = true;
			co.newFood[i].count++;
			break;
		}
	}
	if(!has){
		data.count = 1;
		co.newFood.push(data);
	}
	//
	co.initNewFoodContent({
		data : data
	});
};

/**
 * 选中菜品
 */
co.selectNewFood = function(c){
	if(c == null || typeof c.foodId != 'number'){
		return;
	}
	
	//
	var sl = $('div[data-type=newFood-select]');
	for(var i = 0; i < sl.length; i++){
		$(sl[i]).removeClass('div-newFood-select');
	}
	$(c.event).addClass('div-newFood-select');
	
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

// 菜品分页包
cr.fd = {
	renderTo : 'divCFCOAllFood',
	dom : null,
	start : 0,
	limit : 20,
	data : [],
	length : 0,
	pageData : [],
	isEmpty : function(){
		return this.data == null || this.data.length <= 0;
	},
	init : function(c){
		if(c == null || typeof c.data != 'object'){
			return;
		}
		this.dom = getDom(this.renderTo);
		this.dom.innerHTML = '';
		var ch = this.dom.clientHeight, cw = this.dom.clientWidth;
		this.limit = parseInt((ch / (70 + 5 + 3 * 2))) * parseInt((cw / (90 + 5 + 3 * 2)));
		//
		this.data = c.data;
		//
		this.length = this.data.length;
	},
	initFoodContent : function(c){
		this.pageData = [];
		if(!this.isEmpty()){
			var html = '';
			var start = this.start, limit = this.start + this.limit > this.data.length ? this.start + this.limit - this.data.length : this.limit;
			var temp = null;
			for(var i = 0; i < limit; i++){
				temp = this.data[start+i];
				this.pageData.push(temp);
				if(temp != null){
					html += Templet.cr.boxFood.format({
						dataIndex : i,
						id : temp.id,
						name : temp.name,
						unitPrice : temp.unitPrice
					});				
				}
			}
			temp = null;
			this.dom.innerHTML = html;
		}
	},
	getFirstPage : function(){
		this.start = 0;
		this.initFoodContent();
	},
	getLastPage : function(){
		this.start = this.data.length - this.data.length % this.limit;
		this.initFoodContent();
	},
	getNextPage : function(){
		this.start += this.limit;
		if(this.start > this.data.length){
			this.start -= this.limit;
			return;
		}
		this.initFoodContent();
	},
	getPreviousPage : function(){
		this.start -= this.limit;
		if(this.start < 0){
			this.start += this.limit;
			return;
		}
		this.initFoodContent();
	}
};
/**
 * 初始化新点菜区域
 */
cr.initNewFoodContent = function(c){
	var html = [];
	var temp = null;
	for(var i = 0; i < cr.newFood.length; i++){
		temp = cr.newFood[i];
		html.push(Templet.cr.newFood.format({
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
			getDom('divCFCONewFood').scrollTop = getDom('divCFCONewFood').scrollHeight / cr.newFood.length * select.attr('data-index');
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
cr.insertFood = function(c){
	if(c == null || typeof c.foodId != 'number'){
		return;
	}
	//
	var data = null;
	for(var i = 0; i < cr.fd.pageData.length; i++){
		if(cr.fd.pageData[i].id == c.foodId){
			data = cr.fd.pageData[i];
			break;
		}
	}
	if(data == null){
		alert('添加菜品失败, 程序异常, 请刷新后重试或联系客服人员');
		return;
	}
	//
	var has = false;
	for(var i = 0; i < cr.newFood.length; i++){
		if(cr.newFood[i].id == data.id){
			has = true;
			cr.newFood[i].count++;
			break;
		}
	}
	if(!has){
		data.count = 1;
		cr.newFood.push(data);
	}
	//
	cr.initNewFoodContent({
		data : data
	});
};

/**
 * 选中菜品
 */
cr.selectNewFood = function(c){
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
cr.initDeptContent = function(c){
	var dc = getDom('divSelectDeptForOrder');
	var html = '';
	for(var i = 0; i < deptData.root.length; i++){
		html += Templet.cr.dept.format({
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
cr.initKitchenContent = function(c){
	c = c == null || typeof c == 'undefined' ? {} : c;
	//
	var sl = $('div[data-type=dept-select]');
	for(var i = 0; i < sl.length; i++){
		$(sl[i]).removeClass('div-deptOrKitchen-select');
	}
	$(c.event).addClass('div-deptOrKitchen-select');
	// 
	var kc = getDom('divSelectKitchenForOrder');
	var html = Templet.cr.kitchen.format({
		value : -1,
		text : '全部分厨'
	});
	var tempFoodData = []; // 菜品数据
	var temp = null;
	for(var i = 0; i < kitchenData.root.length; i++){
		temp = kitchenData.root[i];
		if(typeof c.deptId == 'number' && c.deptId != -1){
			if(temp.dept.id == c.deptId){
				html += Templet.cr.kitchen.format({
					value : temp.id,
					text : temp.name
				});
				tempFoodData = tempFoodData.concat(temp.foods);
			}
		}else{
			if(temp.dept.id != -1){
				html += Templet.cr.kitchen.format({
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
	cr.fd.init({
		data : tempFoodData
	});
	cr.fd.getFirstPage();
};

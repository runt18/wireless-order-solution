// 下单操作包
var cr = {
	table : {},
	newFood : []
};

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
		this.limit = parseInt((ch / (70 + 5))) * parseInt((cw / (90 + 5)));
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
					html += ('<div data-value='+temp.id+' class="divCFCOAllFood-main-box" onClick="cr.insertFood({foodId:'+temp.id+'})">' 
							+ temp.name 
							+ '<div>¥:' + temp.unitPrice + '</div>'
							+ '</div>');					
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
		alert('程序异常!')
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
	var html = [];
	var temp = null;
	for(var i = 0; i < cr.newFood.length; i++){
		temp = cr.newFood[i];
		html.push(Templet.cr.newFood.format({
			id : temp.id,
			name : temp.name,
			count : temp.count.toFixed(2),
			unitPrice : temp.unitPrice.toFixed(2),
			totalPrice : (temp.count * temp.unitPrice).toFixed(2)
		}));
	}
	temp = null;
	$('#divCFCONewFood').html(html.join(''));
	var select = $('#divCFCONewFood > div[data-value='+data.id+']');
	select.addClass('div-newFood-select');
//	alert(select[0].offsetHeight+'  :  '+select[0].offsetTop+'  :  '+$('#divCFCONewFood')[0].scrollHeight)
	$('#divCFCONewFood')[0].scrollTop = select[0].offsetTop;
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
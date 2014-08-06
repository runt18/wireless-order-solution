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
		
		var orderFoodHtmlData = {
			dataIndex : i,
			id : temp.id,
			name : temp.name,
			count : temp.count.toFixed(2),
			unitPrice : tempUnitPrice.toFixed(2),
			totalPrice : tempUnitPrice.toFixed(2),
			isHangup : typeof temp.isHangup == 'boolean' && temp.isHangup ? '叫起' : '',
			isTemporary : typeof temp.isTemporary == 'boolean' && temp.isTemporary ? '临时菜' : '',
			isGift : typeof temp.isGift == 'boolean' && temp.isGift ? '赠送' : ''
		};
		//临时口味
		if(typeof temp.tasteGroup.tmpTaste != 'undefined'){
			orderFoodHtmlData.tasteDisplay = temp.isTemporary == true ? '' : temp.tasteGroup.tastePref;
		}else{
			orderFoodHtmlData.tasteDisplay = typeof temp.tasteGroup == 'undefined' 
				|| typeof temp.tasteGroup.normalTasteContent == 'undefined' 
					|| temp.tasteGroup.normalTasteContent.length <= 0 || temp.isTemporary == true ? '' : temp.tasteGroup.tastePref;
		}
		
		html.push(Templet.co.newFood.format(orderFoodHtmlData));
		
		orderFoodHtmlData = null;
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
	
	co.deptPagingLimit = deptData.root.length > 11 ? 10 : 11;
	
	var limit = deptData.root.length >= co.deptPagingStart + co.deptPagingLimit ? co.deptPagingLimit : co.deptPagingLimit - (co.deptPagingStart + co.deptPagingLimit - deptData.root.length);
	
	if(deptData.root.length > 0){
		for (var i = 0; i < limit ; i++) {
			html += Templet.co.dept.format({
				value : deptData.root[co.deptPagingStart + i].id,
				text : deptData.root[co.deptPagingStart + i].name
			});
		}
	}	
		//FIXME 部门分页
	if(deptData.root.length > 11){
		html += '<div id="divDeptPagingPrevious" onClick="co.deptGetPreviousPage()" style="line-height:60px;width:60px !important;" class="button-base-paging">上页</div>' +
				'<div id="divDeptPagingNext" onClick="co.deptGetNextPage()" style="line-height:60px; margin-left: 1px;" class="button-base-paging">下页</div>';
	}	
	dc.innerHTML = html;
};

co.deptGetNextPage = function(){
	co.deptPagingStart += co.deptPagingLimit;
	if(co.deptPagingStart > deptData.root.length){
		co.deptPagingStart -= co.deptPagingLimit;
		return;
	}
	co.initDeptContent();
};

co.deptGetPreviousPage = function(){
	co.deptPagingStart -= co.deptPagingLimit;
	if(co.deptPagingStart < 0){
		co.deptPagingStart += co.deptPagingLimit;
		return;
	}
	co.initDeptContent();
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

	co.kitchenPagingStart = 0;
	co.kitchenPagingData = [];
	var tempFoodData = []; // 菜品数据
	var temp = null;
	for(var i = 0; i < kitchenData.root.length; i++){
		temp = kitchenData.root[i];
		if(typeof c.deptId == 'number' && c.deptId != -1){
			if(temp.dept.id == c.deptId){
				co.kitchenPagingData.push({
					value : temp.id,
					text : temp.name
				});
				tempFoodData = tempFoodData.concat(temp.foods);
				
			}
		}else{
			if(temp.dept.id != -1){
				co.kitchenPagingData.push({
					value : temp.id,
					text : temp.name
				});
			}
			tempFoodData = foodData.root;
		}
	}
	temp = null;
	
	//FIXME
	co.showKitchenPaging();
	//
	co.fp.init({
		data : tempFoodData
	});
	co.fp.getFirstPage();
};

co.kitchenGetNextPage = function(){
	co.kitchenPagingStart += co.kitchenPagingLimit;
	if(co.kitchenPagingStart > co.kitchenPagingData.length){
		co.kitchenPagingStart -= co.kitchenPagingLimit;
		return;
	}
	co.showKitchenPaging();
};

co.kitchenGetPreviousPage = function(){
	co.kitchenPagingStart -= co.kitchenPagingLimit;
	if(co.kitchenPagingStart < 0){
		co.kitchenPagingStart += co.kitchenPagingLimit;
		return;
	}
	co.showKitchenPaging();
};

//显示厨房分页
co.showKitchenPaging = function(){
	var kc = getDom('divSelectKitchenForOrder');
	var html = Templet.co.kitchen.format({
		value : -1,
		text : '全部分厨'
	});	
	
	//FIXME 厨房分页
	if(co.kitchenPagingData.length > 9){
		co.kitchenPagingLimit = 8;
		$('#divKitchenPagingNext').show();
		$('#divKitchenPagingPrevious').show();
	}else{
		$('#divKitchenPagingNext').hide();
		$('#divKitchenPagingPrevious').hide();
	}	
	
	var limit = co.kitchenPagingData.length >= co.kitchenPagingStart + co.kitchenPagingLimit ? co.kitchenPagingLimit : co.kitchenPagingLimit - (co.kitchenPagingStart + co.kitchenPagingLimit -co.kitchenPagingData.length);
	
	if(co.kitchenPagingData.length > 0){
		for (var i = 0; i < limit ; i++) {
			html += Templet.co.kitchen.format({
				value : co.kitchenPagingData[co.kitchenPagingStart + i].value,
				text : co.kitchenPagingData[co.kitchenPagingStart + i].text
			});
		}
	}
	kc.innerHTML = html;	
	

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
		//临时口味, 再次点击时是修改而不是删除
		if(temp.isTemp){
			//菜品只能有一个临时菜
			co.ot.tasteId = temp.id;
			html += Templet.co.boxNewTempTaste.format({
				id : temp.id,
				name : temp.name,
				mark : temp.cateStatusValue == 2 ? '¥' : temp.cateStatusValue == 1 ? '比例' : '',
				markText : temp.cateStatusValue == 2 ? temp.price : temp.cateStatusValue == 1 ? temp.rate : '0.00'
			});			
		}else{
			html += Templet.co.boxNewTaste.format({
				id : temp.id,
				name : temp.name,
				mark : temp.cateStatusValue == 2 ? '¥' : temp.cateStatusValue == 1 ? '比例' : '',
				markText : temp.cateStatusValue == 2 ? temp.price : temp.cateStatusValue == 1 ? temp.rate : '0.00'
			});
		}
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
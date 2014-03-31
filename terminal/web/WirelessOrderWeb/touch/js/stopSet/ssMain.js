/**
 * 
 */
ss.insertFood = function(c){
	if(c == null || typeof c.foodId != 'number'){
		return;
	}
	//
	var data = null;
	for(var i = 0; i < foodDataBase.root.length; i++){
		if(foodDataBase.root[i].id == c.foodId){
			//返回连接空数组的副本
			data = (foodDataBase.root.concat()[i]);
			break;
		}
	}
	if(data == null){
		alert('添加菜品失败, 程序异常, 请刷新后重试或联系客服人员');
		return;
	}
	
	var has = false;
	//
	if(c.type == 'sellOut'){
		for(var i = 0; i < ss.newFood.length; i++){
			if(ss.newFood[i].id == data.id){
				has = true;
				break;
			}
		}
		if(!has){
			data.count = 1;
			ss.newFood.push(data);
		}
	}else{
		for(var i = 0; i < ss.cancelSellOutFood.length; i++){
			if(ss.cancelSellOutFood[i].id == data.id){
				has = true;
				break;
			}
		}
		if(!has){
			data.count = 1;
			ss.cancelSellOutFood.push(data);
		}
	}

	//
	ss.initNewFoodContent({
		data : data,
		type : c.type
	});
	data = null;
	if(typeof c.callback == 'function'){
		c.callback();
	}
};

/**
 * 
 * @param {} c
 */
ss.initNewFoodContent = function(c){
	c = c == null ? {} : c;
	if(typeof c.record != 'undefined'){
		ss.newFood.push(c.record);
		c.data = c.record;
	}
	var html = [], sumCount = 0;
	var temp = null;
	var newFood = [];
	ss.newFoodType = c.type;
	if(c.type == 'sellOut'){
		newFood = ss.newFood.concat();
	}else{
		newFood = ss.cancelSellOutFood.concat();
	}
	for(var i = 0; i < newFood.length; i++){
		temp = newFood[i];
		sumCount += temp.count;
		html.push(Templet.ss.newFood.format({
			dataIndex : i,
			id : temp.id,
			name : temp.name,
			unitPrice : temp.unitPrice.toFixed(2)
		}));
	}
	temp = null;
	if(sumCount > 0){
		$('#divDescForStopSet div:first').html('总数量:' + sumCount.toFixed(2));		
	}else{
		$('#divDescForStopSet div:first').html('');
	}
	
	$('#divNewFoodForStopSet').html(html.join(''));
	if(c.data != null && typeof c.data != 'undefined'){
		var select = $('#divNewFoodForStopSet > div[data-value='+c.data.id+']');
		if(select.length > 0){
			select.addClass('div-newFood-select');
			getDom('divNewFoodForStopSet').scrollTop = getDom('divNewFoodForStopSet').scrollHeight / ss.newFood.length * select.attr('data-index');
		}else{
			getDom('divNewFoodForStopSet').scrollTop = 0;
		}
	}else{
		getDom('divNewFoodForStopSet').scrollTop = 0;
	}
};

/**
 * 
 * @param {} c
 */
ss.selectNewFood = function(c){
	if(c == null || typeof c.foodId != 'number'){
		return;
	}
	ss.deleteSelectedFood({foodId : c.foodId});
/*	var sl = $('div[data-type=newFood-select]');
	for(var i = 0; i < sl.length; i++){
		$(sl[i]).removeClass('div-newFood-select');
	}
	$(c.event).addClass('div-newFood-select');*/
};


ss.soldOut = function(c){
	Util.LM.show();
	var foodIds = '';
	var dataSource = '';
	if(c.type == true){
		if(ss.newFood == null || typeof ss.newFood == 'undefined' || ss.newFood.length == 0){
			Util.msg.alert({
				title : '温馨提示',
				msg : '请选择未沽清的菜品后再继续操作.'
			});
			Util.LM.hide();
			return;
		}
		
		
		dataSource = 'sellOut';
		for (var i = 0; i < ss.newFood.length; i++) {
			foodIds += (i == 0 ? '' : ',');
			foodIds += ss.newFood[i].id;
		}
	}else{
		if(ss.cancelSellOutFood == null || typeof ss.cancelSellOutFood == 'undefined' || ss.cancelSellOutFood.length == 0){
			Util.msg.alert({
				title : '温馨提示',
				msg : '请选择已沽清的菜品后再继续操作.'
			});
			Util.LM.hide();
			return;
			
		}
		dataSource = 'deSellOut';
		for (var i = 0; i < ss.cancelSellOutFood.length; i++) {
			foodIds += (i == 0 ? '' : ',');
			foodIds += ss.cancelSellOutFood[i].id;
		}
	}
	$.ajax({
		url : '../OperateSellOutFood.do',
		type : 'post',
		data : {
			dataSource : dataSource,
			foodIds : foodIds
		},
		success : function(data, status, xhr) {
			Util.LM.hide();
			if (data.success == true) {
				Util.msg.alert({
					title : data.title,
					msg : data.msg,
					time : 3,
					fn : function(btn){
						ss.back();
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


ss.deleteSelectedFood = function(c){
	if(c == null || typeof c.foodId != 'number'){
		return;
	}
	if(ss.newFoodType == 'sellOut'){
		for(var i = 0; i < ss.newFood.length; i++){
			if(ss.newFood[i].id == c.foodId){
				ss.newFood.splice(i, 1);
				break;
			}
		}
	}else{
		for(var i = 0; i < ss.cancelSellOutFood.length; i++){
			if(ss.cancelSellOutFood[i].id == c.foodId){
				ss.cancelSellOutFood.splice(i, 1);
				break;
			}
		}
	}

	//
	ss.initNewFoodContent({
		type : ss.newFoodType
	});
};

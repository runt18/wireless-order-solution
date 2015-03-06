//估清
var ss = {
	newFood : [],
	cancelSellOutFood : [],
	allStopFood : [],
	extra : '',//显示菜品的条件, 沽清或开售
	iteratorData : []//符合条件的菜品
},
	/**
	 * 元素模板
	 */
	stopSellFoodTemplet = '<a data-role="button" data-corners="false" data-inline="true" class="food-style" data-value={id} onclick="{click}">' +
								'<div style="height: 70px;">{name}<br>￥{unitPrice}' +
								'</div>'+
							'</a>',
	
	dept4StopSellCmpTemplet = '<a href="javascript: ss.initKitchenContent({deptId:{id}})" data-role="button" data-inline="true" class="deptKitBtnFont" data-type="dept4StopSellCmp" data-value="{id}" >{name}</a>',
	kitchen4StopSellCmpTemplet = '<a data-role="button" data-inline="true" class="deptKitBtnFont" data-type="kitchen4StopSellCmp" data-value={id} onclick="ss.findFoodByKitchen({event:this, kitchenId:{id}})">{name}</a>',
	
	orderFood4StopSellCmpTemplet = '<li data-icon=false data-index={dataIndex} data-theme="c" data-value={id} data-type="orderFood4StopSellCmp" onclick="ss.selectNewFood({event:this, foodId:{id}})" ><a >'+
										'<h1 style="font-size:20px;">{name}</h1>' +
										'<div>' +
											'<span style="float: right;">￥{unitPrice}</span>' +
										'</div>' +
										'</a></li>';

//初始化分页
ss.init = function(){
	if(!this.initFlag===true){
		this.initFlag = true;
		
		ss.stoptp = new Util.to.padding({
			renderTo : 'foods4StopSellCmp',
			displayId : 'foods4StopSellCmp-padding-msg',
			templet : function(c){
				return stopSellFoodTemplet.format({
					id : c.data.id,
					name : c.data.name,
					unitPrice : c.data.unitPrice,
					click : 'ss.insertFood({foodId:'+c.data.id+', type:\'deSellOut\'})'
				});
			}
		});
		ss.normaltp = new Util.to.padding({
			renderTo : 'foods4StopSellCmp',
			displayId : 'foods4StopSellCmp-padding-msg',
			templet : function(c){
				return stopSellFoodTemplet.format({
					id : c.data.id,
					name : c.data.name,
					unitPrice : c.data.unitPrice,
					click : 'ss.insertFood({foodId:'+c.data.id+', type:\'sellOut\'})'
				});
			}
		});
		ss.tp = ss.normaltp;
	}
};
/**
 * 更新沽清菜列表
 * @param {} c
 */
ss.updateData = function(c){
	Util.LM.show();
	$.ajax({
		url : '../QueryMenu.do',
		type : 'post',
		data : {
			dataSource : 'stop'
		},
		success : function(data, status, xhr) {
			ss.stoptp.init({
				data : data.root
			});
		},
		error : function(request, status, err) {
			Util.LM.hide();
			Util.msg.alert({
				title : '错误',
				msg : '加载菜品出错, 请刷新页面',
				renderTo : 'stopSellMgr'
			});
		}
	});
	
	$.ajax({
		url : '../QueryMenu.do',
		type : 'post',
		data : {
			dataSource : 'unStop'
		},
		success : function(data, status, xhr) {
			Util.LM.hide();
			ss.normaltp.init({
				data : data.root
			});
		},
		error : function(request, status, err) {
			Util.LM.hide();
			Util.msg.alert({
				title : '错误',
				msg : '加载菜品出错, 请刷新页面',
				renderTo : 'stopSellMgr'
			});
		}
	});
	
};

/**
 * 部门初始化
 */
ss.initDeptContent = function(){
	var dc = $("#depts4StopSellCmp");
	var html = ['<a href="javascript: ss.initKitchenContent({deptId:-1})" data-role="button" data-inline="true" class="deptKitBtnFont" data-value="-1" data-type="dept4StopSellCmp">全部部门</a>'];
	
	ss.deptPagingStart = 0;
	
	//真实宽度
	var usefullWidth = document.body.clientWidth - 220;
	//每行显示部门的个数
	var displayDeptCount =  parseInt(usefullWidth / 88);	
	
	ss.deptPagingLimit = of.depts.root.length > displayDeptCount ? displayDeptCount-1 : displayDeptCount;
	
	var limit = of.depts.root.length >= ss.deptPagingStart + ss.deptPagingLimit ? ss.deptPagingLimit : ss.deptPagingLimit - (ss.deptPagingStart + ss.deptPagingLimit - of.depts.root.length);

	
	if(of.depts.root.length > 0){
		for (var i = 0; i < limit; i++) {
			html.push(dept4StopSellCmpTemplet.format({
				id : of.depts.root[ss.deptPagingStart + i].id,
				name : of.depts.root[ss.deptPagingStart + i].name
			}));
		}
	}	
	
	//显示分页按钮
	if(of.depts.root.length > displayDeptCount){
		html.push('<a href="javascript:ss.deptGetPreviousPage()" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">L</a>' +
				'<a href="javascript:ss.deptGetNextPage()" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">R</a>');
	}	
	$("#depts4StopSellCmp").html(html.join("")).trigger('create').trigger('refresh');		
	
	
};

/**
 * 部门分页
 * @param c
 */
ss.deptGetNextPage = function(){
	ss.deptPagingStart += ss.deptPagingLimit;
	if(ss.deptPagingStart > of.depts.root.length){
		ss.deptPagingStart -= ss.deptPagingLimit;
		return;
	}
	ss.initDeptContent();
};

/**
 * 部门分页
 * @param c
 */
ss.deptGetPreviousPage = function(){
	ss.deptPagingStart -= ss.deptPagingLimit;
	if(ss.deptPagingStart < 0){
		ss.deptPagingStart += ss.deptPagingLimit;
		return;
	}
	ss.initDeptContent();
};

/**
 * 初始化厨房
 */
ss.initKitchenContent = function(c){
	c = c == null || typeof c == 'undefined' ? {} : c;
	//
	var sl = $('#depts4StopSellCmp a[data-type=dept4StopSellCmp]');
	sl.attr('data-theme', 'c');
	for(var i = 0; i < sl.length; i++){
		if($(sl[i]).attr('data-value') == c.deptId){
			$(sl[i]).attr('data-theme', 'b');
		}
	}
	sl.buttonMarkup( "refresh" );
	
	ss.kitchenPagingStart = 0;
	ss.kitchenPagingData = [];
	var tempFoodData = []; // 菜品数据
	var temp = null;
	for(var i = 0; i < of.kitchens.root.length; i++){
		temp = of.kitchens.root[i];
		if(typeof c.deptId == 'number' && c.deptId != -1){
			if(temp.dept.id == c.deptId){
				ss.kitchenPagingData.push({
					id : temp.id,
					name : temp.name
				});
				tempFoodData = tempFoodData.concat(temp.foods);
			}
		}else{
			if(temp.dept.id != -1){
				ss.kitchenPagingData.push({
					id : temp.id,
					name : temp.name
				});
			}
			tempFoodData = of.foodList;
		}
	}
	temp = null;
	
	ss.showKitchenPaging();
	ss.iteratorData = tempFoodData;
	ss.showFoodByCond();	
	
	
	if(ss.searchFooding){
		//关闭搜索
		closeSearchFood();
	}	
	
};


//显示厨房分页
ss.showKitchenPaging = function(){
	var kc = $("#kitchens4StopSellCmp");
	var html = ['<a onclick="ss.findFoodByKitchen({event:this, kitchenId:-1})" data-role="button" data-inline="true" data-type="kitchen4StopSellCmp" data-value=-1 class="deptKitBtnFont">全部厨房</a>'];

	//真实宽度
	var usefullWidth = document.body.clientWidth - 220;
	//每行显示厨房的个数
	var displayKitchenCount =  parseInt(usefullWidth / 88);
	
	ss.kitchenPagingLimit = ss.kitchenPagingData.length > displayKitchenCount ? displayKitchenCount-1 : displayKitchenCount;
	
	var limit = ss.kitchenPagingData.length >= ss.kitchenPagingStart + ss.kitchenPagingLimit ? ss.kitchenPagingLimit : ss.kitchenPagingLimit - (ss.kitchenPagingStart + ss.kitchenPagingLimit -ss.kitchenPagingData.length);
	
	if(ss.kitchenPagingData.length > 0){
		for (var i = 0; i < limit ; i++) {
			html.push(kitchen4StopSellCmpTemplet.format({
				id : ss.kitchenPagingData[ss.kitchenPagingStart + i].id,
				name : ss.kitchenPagingData[ss.kitchenPagingStart + i].name
			}));
		}
	}
	//显示分页按钮
	if(ss.kitchenPagingData.length > displayKitchenCount){
		html.push('<a href="javascript:ss.kitchenGetPreviousPage()" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">L</a>' +
				'<a href="javascript:ss.kitchenGetNextPage()" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">R</a>');
	}	
	kc.html(html.join("")).trigger('create').trigger('refresh');

};

/**
 * 但div还没设置完成高度时不断刷新
 */
function keepLoadFoodData(){
	
	if(!$('#foods4StopSellCmp').html()){
		ss.initKitchenContent({deptId:-1});
	}else{
		clearInterval(ss.loadFoodDateAction);
	}
}

/**
 * 厨房分页
 */
ss.kitchenGetNextPage = function(){
	ss.kitchenPagingStart += ss.kitchenPagingLimit;
	if(ss.kitchenPagingStart > ss.kitchenPagingData.length){
		ss.kitchenPagingStart -= ss.kitchenPagingLimit;
		return;
	}
	ss.showKitchenPaging();
};
ss.kitchenGetPreviousPage = function(){
	ss.kitchenPagingStart -= ss.kitchenPagingLimit;
	if(ss.kitchenPagingStart < 0){
		ss.kitchenPagingStart += ss.kitchenPagingLimit;
		return;
	}
	ss.showKitchenPaging();
};


/**
 * 根据条件显示菜品
 */
ss.showFoodByCond = function(c){
	if(ss.extra == ''){
		return;
	}
	
	var showFoodDatas = [];
	for (var i = 0; i < ss.iteratorData.length; i++) {
		var tempFoodData = ss.iteratorData[i];
		if(eval(ss.extra)){
			showFoodDatas.push(tempFoodData);
		}
	}
	ss.tp.init({
		data : showFoodDatas
	});
	ss.tp.getFirstPage();
	
};


/**
 * 根据厨房显示菜品
 */
ss.findFoodByKitchen = function(c){
	c = c == null || typeof c == 'undefined' ? {} : c;
	//
	var sl = $('#kitchens4StopSellCmp > a[data-type=kitchen4StopSellCmp]');
	sl.attr('data-theme', 'c');
	if(c.event){
		$(c.event).attr('data-theme', 'b');
	}
	
	sl.buttonMarkup( "refresh" );
	
	var tempFoodData = [];
	var temp = null;
	if(c.kitchenId == -1){
		var dl = $('#depts4StopSellCmp a[data-theme=b]');
		if(dl.length == 0 || parseInt(dl[0].getAttribute('data-value')) == -1){
			for(var i = 0; i < of.kitchens.root.length; i++){
				tempFoodData = tempFoodData.concat(of.kitchens.root[i].foods);
			}
		}else{
			for(var i = 0; i < of.kitchens.root.length; i++){
				temp = of.kitchens.root[i];
				if(temp.dept.id == parseInt(dl[0].getAttribute('data-value'))){
					tempFoodData = tempFoodData.concat(temp.foods);		
				}
			}
		}
	}else{
		for(var i = 0; i < of.kitchens.root.length; i++){
			temp = of.kitchens.root[i];
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
	ss.iteratorData = tempFoodData;
	ss.showFoodByCond();
	
	if(ss.searchFooding){
		//关闭搜索
		closeSearchFood();
	}		
	
};

/**
 * 沽清入口
 */
ss.entry = function(){
	ss.init();
	
	ss.updateData();
	
	ss.initDeptContent();
	
	ss.initKitchenContent({deptId : -1});
	
	setTimeout("ss.searchData({event:$('#divBtnSellFood'), isStop:false})", 400);
};


ss.back = function(){
	location.href = '#tableSelectMgr';
	
	ss.newFood = [];
	ss.cancelSellOutFood = [];
	ss.callback = null;
	ss.initNewFoodContent();
	
	$('.tableStatus').attr('data-theme', 'c').removeClass('ui-btn-up-e').addClass('ui-btn-up-c');
	ss.extra = '';
};

/**
 * 点击头部导航栏显示相关菜品
 */
ss.searchData = function(c){
	ss.newFood = [];
	ss.cancelSellOutFood = [];
	ss.initNewFoodContent();
	ss.extra = '';
	if(c.isStop === true){
		ss.tp = ss.stoptp;
	}else{
		ss.tp = ss.normaltp;
	}
	
	var sells = $('.tableStatus');
	sells.attr('data-theme', 'c').removeClass('ui-btn-up-e').addClass('ui-btn-up-c');
	$(c.event).attr('data-theme', 'e').removeClass('ui-btn-up-c').addClass('ui-btn-up-e');
	
	if(c.isStop != null && c.isStop === true){
		ss.extra += '(tempFoodData.status & 1 << 2) != 0';
	}else{
		ss.extra += '(tempFoodData.status & 1 << 2) == 0';
	}
	ss.showFoodByCond();
	
};


/**
 * 选中菜品
 */
ss.insertFood = function(c){
	if(c == null || typeof c.foodId != 'number'){
		return;
	}
	//
	var data = null;
	for(var i = 0; i < of.foodList.length; i++){
		if(of.foodList[i].id == c.foodId){
			//返回连接空数组的副本
			data = (of.foodList.concat()[i]);
			break;
		}
	}
	if(data == null){
		Util.msg.alert({
			msg : '添加菜品失败, 程序异常, 请刷新后重试或联系客服人员',
			topTip : true
		});
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
 * 生成菜品列表
 * @param {} c
 */
ss.initNewFoodContent = function(c){
	c = c == null ? {} : c;
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
		html.push(orderFood4StopSellCmpTemplet.format({
			dataIndex : i,
			id : temp.id,
			name : temp.name,
			unitPrice : temp.unitPrice.toFixed(2)
		}));
	}
	temp = null;
	if(sumCount > 0){
		$('#count4StopSellFoods').html('总数量:' + sumCount);		
	}else{
		$('#count4StopSellFoods').html('');
	}
	
	$('#toStopSellFoodCmp').html(html.join(''));
	
	$('#toStopSellFoodCmp').listview("refresh"); 
	
	//刷新界面后重新选中点的菜
	if(c.data != null && typeof c.data != 'undefined'){
		var select = $('#toStopSellFoodCmp > li[data-value='+c.data.id+']');
		if(select.length > 0){
			select.attr('data-theme', 'e').removeClass('ui-btn-up-c').addClass('ui-btn-up-e');
			$('#divFoods4StopSellCmp').animate({
				scrollTop: document.getElementById('divFoods4StopSellCmp').scrollHeight / newFood.length * select.attr('data-index')
			}, 'fast');
		}else{
			$('#divFoods4StopSellCmp').animate({scrollTop: 0}, 'fast');
		}
	}else{
		$('#divFoods4StopSellCmp').animate({scrollTop: 0}, 'fast');
	}	
};

/**
 * 沽清菜品
 */
ss.soldOut = function(c){
	var foodIds = '';
	var dataSource = '';
	if(c.type == true){
		if(ss.newFood == null || typeof ss.newFood == 'undefined' || ss.newFood.length == 0){
			Util.msg.alert({
				msg : '请选择在售的菜品后再继续操作.',
				renderTo : 'stopSellMgr'
			});
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
				msg : '请选择沽清的菜品后再继续操作.',
				renderTo : 'stopSellMgr'
			});
			return;
			
		}
		dataSource = 'deSellOut';
		for (var i = 0; i < ss.cancelSellOutFood.length; i++) {
			foodIds += (i == 0 ? '' : ',');
			foodIds += ss.cancelSellOutFood[i].id;
		}
	}
	Util.LM.show();
	
	$.ajax({
		url : '../OperateSellOutFood.do',
		type : 'post',
		data : {
			dataSource : dataSource,
			foodIds : foodIds
		},
		success : function(data, status, xhr) {
			Util.LM.hide();
			if (data.success) {
				Util.msg.alert({
					topTip : true,
					msg : data.msg
				});
				//重新加载更改后的foodData
				initFoodData();
				ss.back();
			} else {
				Util.msg.alert({
					title : data.title,
					msg : data.msg, 
					renderTo : 'stopSellMgr'
				});
			}
		},
		error : function(request, status, err) {
			Util.LM.hide();
			Util.msg.alert({
				title : '错误',
				msg : '数据操作失误, 请刷新页面后重试',
				renderTo : 'stopSellMgr'
			});
		}
	});
	
};

/**
 * 去除选中菜品
 * @param {} c
 */
ss.selectNewFood = function(c){
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

	ss.initNewFoodContent({
		type : ss.newFoodType
	});
};




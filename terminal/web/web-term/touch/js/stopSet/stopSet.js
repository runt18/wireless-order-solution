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
	//菜品
	stopSellFoodTemplet = '<a data-role="button" data-corners="false" data-inline="true" class="food-style" data-index={dataIndex} data-value={id} onclick="{click}">' +
								'<div style="height: 70px;">{name}<br>￥{unitPrice}' +
								'<div class="food-status-limit {limitStatus}">' +
									'<font color="orange">限: {foodLimitAmount}</font><br>' +
									'<font color="green">剩: {foodLimitRemain}</font>' +
								'</div>'+	
								
								'</div>'+
							'</a>',
	//部门
	dept4StopSellCmpTemplet = '<a href="javascript: ss.initKitchenContent({deptId:{id}})" data-role="button" data-inline="true" class="deptKitBtnFont" data-type="dept4StopSellCmp" data-value="{id}" >{name}</a>',
	//厨房
	kitchen4StopSellCmpTemplet = '<a data-role="button" data-inline="true" class="deptKitBtnFont" data-type="kitchen4StopSellCmp" data-value={id} onclick="ss.findFoodByKitchen({event:this, kitchenId:{id}})">{name}</a>',
	//选中菜列表
	orderFood4StopSellCmpTemplet = '<li data-icon=false data-index={dataIndex} data-theme="c" data-value={id} data-type="orderFood4StopSellCmp" onclick="ss.selectNewFood({event:this, foodId:{id}})" ><a >'+
										'<h1 style="font-size:20px;">{name}</h1>' +
										'<div>' +
											'<span style="float: right;">￥{unitPrice}</span>' +
										'</div>' +
										'</a></li>';

/**
 * 沽清入口
 */
ss.entry = function(){
	ss.init();
	
	//更新沽清菜 & 限量沽清菜
	of.updataSelloutFoods();
	
	ss.initDeptContent();
	
	ss.initKitchenContent({deptId : -1});
	
	/**
	 * 没加载到菜品时不断刷新
	 */	
	var index = 0;	
	ss.loadFoodDateAction = window.setInterval(function(){
		//默认选中第一个, 显示在售菜品
		if($('#foods4StopSellCmp').find("a").length > 0){
			clearInterval(ss.loadFoodDateAction);
			if(index == 0){
				ss.searchData({event:$('#divBtnSellFood')[0], isStop:false});
			}
			Util.LM.hide();
		}else{
			index ++;
			Util.LM.show();
			ss.searchData({event:$('#divBtnSellFood')[0], isStop:false});
		}
	}, 500);
	
};

/**
 * 返回餐台界面
 */
ss.back = function(){
	location.href = '#tableSelectMgr';
	
	ss.newFood = [];
	ss.cancelSellOutFood = [];
	ss.callback = null;
	ss.initNewFoodContent();
	
	$('.tableStatus').attr('data-theme', 'c').removeClass('ui-btn-up-e').addClass('ui-btn-up-c');
	ss.extra = '';
};

//沽清选择匹配
ss.s = {
	file : null,
	fileValue : null,
	init : function(c){
		this.file = document.getElementById(c.file);
		if(typeof this.file.oninput != 'function'){
			this.file.oninput = function(e){
				ss.s.fileValue = ss.s.file.value;
				var data = null, temp = null;
				if(ss.s.fileValue.trim().length > 0){
					data = [];
					//所有菜品
					temp = ss.showFoodByCond.showFoodDatas.slice(0);
					for(var i = 0; i < temp.length; i++){
						if(temp[i].name.indexOf(ss.s.fileValue.trim()) != -1){
							data.push(temp[i]);
						}
					}				
				}
					
				ss.s.foodPaging.data(
					data ? data.sort(function (obj1, obj2) {
											    var val1 = obj1.status;
											    var val2 = obj2.status;
											    if ((val1 & 1 << 10) < (val2 & 1 << 10)) {
											        return 1;
											    } else if ((val1 & 1 << 10) > (val2 & 1 << 10)) {
											        return -1;
											    } else {
											        return 0;
											    }            
											}) : ss.showFoodByCond.showFoodDatas.slice(0)
				);
				data = null;
				temp = null;
			};
		}
		
		if(!ss.s.foodPaging){
			ss.s.foodPaging = new WirelessOrder.Padding({
				renderTo : $('#foods4StopSellCmp'),
				itemLook : function(index, item){
					return stopSellFoodTemplet.format({
						dataIndex : index,
						id : item.id,
						name : item.name,
						unitPrice : item.unitPrice,
						limitStatus : ((item.status & 1 << 10) != 0 || item.foodLimitAmount > 0) ? '' : 'none',
						foodLimitAmount : item.foodLimitAmount,
						foodLimitRemain : item.foodLimitRemain
					});
				},
				itemClick : function(index, item){
					ss.insertFood({foodId : item.id, type : ss.status == 'stop' ? 'deSellOut' : 'sellOut'});
				},
				onPageChanged : function(){
					//FIXME .food-status-font中position:absolute不起作用
					setTimeout(function(){
						$(".food-status-limit").css("position", "absolute");
					}, 250);				
				}
			});		
		}
		return this.file;
	},
	valueBack : function(){
		if(this.file.value){
			this.file.value = this.file.value.substring(0, this.file.value.length - 1);
			this.file.oninput(this.file);			
		}
		this.file.focus();
	},
	onInput : function(){
		this.file.oninput(this.file);		
	},
	select : function(){
		this.file.select();
	},
	clear : function(){
		this.file.value = '';
		this.file.oninput(this.file);
		this.file.select();
	},
	callback : function(){
		ss.s.clear();
	},
	fireEvent : function(){
		ss.s.onInput();
	}
};	

/**
 * 搜索菜品
 * @param ope
 */
function searchSelloutFood(ope){
	if(ope=='on'){
		if(!ss.s.init({file : 'searchSelloutFoodInput'})){
			Util.msg.alert({
				renderTo : 'stopSellMgr',
				msg : '程序异常, 搜索功能无法使用, 请刷新页面后重试.',
				time : 2
			});
			return;
		}
		ss.searchFooding = true;
		
		$('#normalOperateFood4StopSellCmp').hide();
		$('#searchSelloutFoodCmp').show();	
		
		//临时菜输入框弹出手写板控件
		HandWritingAttacher.instance().attach($('#searchSelloutFoodInput')[0], function(attachTo, value){
			$(attachTo).focus();
		});
		$('#searchSelloutFoodInput').focus();
		
	}else{
		var kitchen = $('#kitchens4StopSellCmp > a[data-theme=b]');
		if(kitchen.length > 0){
			kitchen.click();
		}else{
			kitchen = $('#kitchens4StopSellCmp a[data-value=-1]')[0];
			kitchen.onclick();
		}
		kitchen = null;		
		
		closeSearchSelloutFood();
	}
}
/**
 * 关闭搜索
 */
function closeSearchSelloutFood(){
	ss.searchFooding = false;	
	
	$('#searchSelloutFoodInput').val('');
	
	$('#normalOperateFood4StopSellCmp').show();
	$('#searchSelloutFoodCmp').hide();
}



//初始化分页
ss.init = function(){
	if(!this.initFlag===true){
		this.initFlag = true;
		
		ss.stoptp = new WirelessOrder.Padding({
			renderTo : $('#foods4StopSellCmp'),
			displayTo : $('#foods4StopSellCmp-padding-msg'),
			itemLook : function(index, item){
				return stopSellFoodTemplet.format({
					dataIndex : index,
					id : item.id,
					name : item.name,
					unitPrice : item.unitPrice,
					limitStatus : ((item.status & 1 << 10) != 0 || item.foodLimitAmount > 0) ? '' : 'none',
					foodLimitAmount : item.foodLimitAmount,
					foodLimitRemain : item.foodLimitRemain
				});
			},
			itemClick : function(index, item){
				ss.insertFood({foodId : item.id, type : 'deSellOut'});
			},
			onPageChanged : function(){
				//FIXME .food-status-limit中position:absolute不起作用
				setTimeout(function(){
					$(".food-status-limit").css("position", "absolute");
				}, 250);	
			}
		});
		ss.normaltp = new WirelessOrder.Padding({
			renderTo : $('#foods4StopSellCmp'),
			displayTo : $('#foods4StopSellCmp-padding-msg'),
			itemLook : function(index, item){
				return stopSellFoodTemplet.format({
					dataIndex : index,
					id : item.id,
					name : item.name,
					unitPrice : item.unitPrice,
					limitStatus : (item.status & 1 << 10) != 0 ? '' : 'none',
					foodLimitAmount : item.foodLimitAmount,
					foodLimitRemain : item.foodLimitRemain
				});
			},
			itemClick : function(index, item){
				ss.insertFood({foodId : item.id, type : 'sellOut'});
			},
			onPageChanged : function(){
				//FIXME .food-status-limit中position:absolute不起作用
				setTimeout(function(){
					$(".food-status-limit").css("position", "absolute");
				}, 250);
			}
		});
		
		ss.tp = ss.normaltp;
	}
};
/**
 * 更新沽清菜列表
 * @param {} c
 */
//ss.updateData = function(c){
//	Util.LM.show();
//	$.ajax({
//		url : '../QueryMenu.do',
//		type : 'post',
//		async : false,
//		data : {
//			dataSource : 'stop'
//		},
//		success : function(data, status, xhr) {
//			ss.stoptp.init({
//				data : data.root.sort(of.foodOrderByStatus)
//			});
//		},
//		error : function(request, status, err) {
//			Util.LM.hide();
//			Util.msg.alert({
//				title : '错误',
//				msg : '加载菜品出错, 请刷新页面',
//				renderTo : 'stopSellMgr'
//			});
//		}
//	});
//	
//	$.ajax({
//		url : '../QueryMenu.do',
//		type : 'post',
//		async : false,
//		data : {
//			dataSource : 'unStop'
//		},
//		success : function(data, status, xhr) {
//			Util.LM.hide();
//			ss.normaltp.init({
//				data : data.root.sort(of.foodOrderByStatus)
//			});
//		},
//		error : function(request, status, err) {
//			Util.LM.hide();
//			Util.msg.alert({
//				title : '错误',
//				msg : '加载菜品出错, 请刷新页面',
//				renderTo : 'stopSellMgr'
//			});
//		}
//	});
//	
//};

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
	
	ss.deptPagingLimit = WirelessOrder.depts.length > displayDeptCount ? displayDeptCount-1 : displayDeptCount;
	
	var limit = WirelessOrder.depts.length >= ss.deptPagingStart + ss.deptPagingLimit ? ss.deptPagingLimit : ss.deptPagingLimit - (ss.deptPagingStart + ss.deptPagingLimit - WirelessOrder.depts.length);

	
	if(WirelessOrder.depts.length > 0){
		for (var i = 0; i < limit; i++) {
			html.push(dept4StopSellCmpTemplet.format({
				id : WirelessOrder.depts[ss.deptPagingStart + i].id,
				name : WirelessOrder.depts[ss.deptPagingStart + i].name
			}));
		}
	}	
	
	//显示分页按钮
	if(WirelessOrder.depts.length > displayDeptCount){
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
	if(ss.deptPagingStart > WirelessOrder.depts.length){
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
	for(var i = 0; i < WirelessOrder.kitchens.length; i++){
		temp = WirelessOrder.kitchens[i];
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
			tempFoodData = WirelessOrder.foods;
		}
	}
	temp = null;
	
	ss.showKitchenPaging();
	ss.iteratorData = tempFoodData.sort(ss.foodOrderByStatus);
	
	ss.showFoodByCond();	
	
	
	if(ss.searchFooding){
		//关闭搜索
	closeSearchSelloutFood();	
}	
	
};


/**
 * 显示厨房分页
 */
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
	ss.showFoodByCond.showFoodDatas = [];
	for (var i = 0; i < ss.iteratorData.length; i++) {
		var tempFoodData = ss.iteratorData[i];
		if(eval(ss.extra)){
			ss.showFoodByCond.showFoodDatas.push(tempFoodData);
		}
	}
	ss.tp.data(ss.showFoodByCond.showFoodDatas);
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
			for(var i = 0; i < WirelessOrder.kitchens.length; i++){
				tempFoodData = tempFoodData.concat(WirelessOrder.kitchens[i].foods);
			}
		}else{
			for(var i = 0; i < WirelessOrder.kitchens.length; i++){
				temp = WirelessOrder.kitchens[i];
				if(temp.dept.id == parseInt(dl[0].getAttribute('data-value'))){
					tempFoodData = tempFoodData.concat(temp.foods);		
				}
			}
		}
	}else{
		for(var i = 0; i < WirelessOrder.kitchens.length; i++){
			temp = WirelessOrder.kitchens[i];
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
		closeSearchSelloutFood();	
	}		
	
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
		ss.status = 'stop';
		ss.tp = ss.stoptp;
	}else{
		ss.status = 'sell';
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
 * 打开限量沽清
 */
ss.openFoodLimitCmp = function(){
	setTimeout(function(){
		$('#inputOrderFoodLimitCountSet').val(ss.insertFood.remianAmount).select();
	}, 250);
	
	$('#orderFoodLimitCmp').popup('open');
	$('#orderFoodLimitCmp').parent().addClass("pop").addClass("in");	
};

/**
 * 关闭限量沽清
 */
ss.closeFoodLimitCmp = function(){
	$('#orderFoodLimitCmp').popup('close');
	
	$('#inputOrderFoodLimitCountSet').val("");
};

/**
 * 设置剩余数量
 */
ss.setFoodLimitRemaining = function(c){
	var amount = $('#inputOrderFoodLimitCountSet');
	if(!amount.val()){
		Util.msg.tip('请填写数量');
		amount.focus();
		return;
	}	
	
	$.ajax({
		url : '../OperateOrderFood.do',
		type : 'post',
		dataType : 'json',
		data : {
			dataSource : 'updateFoodLimit',
			foodId : ss.insertFood.foodId,
			amount : amount.val()
		},
		success : function(rt){
			if(rt.success){
				ss.closeFoodLimitCmp();
				ss.entry();
				Util.msg.tip("剩余数量修改成功");
				
			}else{
				Util.msg.tip(rt.msg);
			}
		},
		error : function(xhr){
			
		}
	});
	
	
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
	for(var i = 0; i < WirelessOrder.foods.length; i++){
		if(WirelessOrder.foods[i].id == c.foodId){
			//返回连接空数组的副本
			data = (WirelessOrder.foods.concat()[i]);
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
	
	//是否限量菜品
	if((data.status & 1 << 10) != 0){
		ss.insertFood.foodId = c.foodId;
		ss.insertFood.remianAmount = data.foodLimitRemain;
		ss.openFoodLimitCmp();
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
				//关闭搜索
				closeSearchSelloutFood();
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

/**
 * 限量沽清重置
 */
ss.resetFoodLimit = function(){

	Util.msg.alert({
		msg : '是否重置限量菜品?',
		renderTo : 'stopSellMgr',
		buttons : 'yesback',
		certainCallback : function(){
			Util.LM.show();
			$.post('../OperateSellOutFood.do', {dataSource : 'resetFoodLimit'}, function(rt){
				if(rt.success){
					ss.entry();
					Util.msg.tip(rt.msg);
				}else{
					Util.msg.alert({
						renderTo : 'stopSellMgr',
						msg : '重置失败'
					});
				}
			}).error(function() {
				Util.LM.hide();
				Util.msg.alert({
					msg : '操作失败, 请联系客服',
					renderTo : 'orderFoodListMgr'
				});		
			});
			
		},
		returnCallback : function(){
			delete uo.printDetailPatchAction;
		}
	});
	

};




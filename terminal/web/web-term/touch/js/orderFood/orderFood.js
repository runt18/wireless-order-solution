
var of = {
	deptPaging : {},	
	deptPagingStart : 0,
	ot : {
		tasteGroupPagingStart : 0,
		choosedTastes : [],
		allBill : false
	},
	selectedOrderFood : {},
	commonTastes : [],
	calculator : {},
	newFood : []
};

of.searchFoodCompare = function (obj1, obj2) {
    var val1 = obj1.foodCnt;
    var val2 = obj2.foodCnt;
    if (val1 < val2) {
        return -1;
    } else if (val1 > val2) {
        return 1;
    } else {
        return 0;
    }            
} 


//餐台选择匹配
of.s = {
	file : null,
	fileValue : null,
	init : function(c){
		this.file = document.getElementById(c.file);
		if(typeof this.file.oninput != 'function'){
			this.file.oninput = function(e){
				of.s.fileValue = of.s.file.value;
				var data = null, temp = null;
				if(of.s.fileValue.trim().length > 0){
					data = [];
					temp = of.foodList.slice(0);
					for(var i = 0; i < temp.length; i++){
						if(temp[i].name.indexOf(of.s.fileValue.trim()) != -1){
							data.push(temp[i]);
						}
					}				
				}
				
				data = data.sort(of.searchFoodCompare);
				
				of.s.foodPaging.init({
					data : data,
					callback : function(){
						of.s.foodPaging.getFirstPage();
					}
				});
				data = null;
				temp = null;
			};
		}
		
		if(!of.s.foodPaging){
			of.s.foodPaging = Util.to.padding({
				renderTo : "foodsCmp",
				templet : function(c){
					return foodCmpTemplet.format({
						id : c.data.id,
						name : c.data.name.substring(0, 10),
						unitPrice : c.data.unitPrice,
						click : 'of.insertFood({foodId:' + c.data.id + ', callback:of.s.callback})',
						sellout : (c.data.status & 1 << 2) != 0 ? '停' : '',
						gift : (c.data.status & 1 << 3) != 0 ? '赠' : ''					
					});
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
		of.s.clear();
	},
	fireEvent : function(){
		of.s.onInput();
	}
};

window.onresize = function(){
	//动态高度
	$('#orderFoodCenterCmp').height(document.body.clientHeight - 210);
	document.getElementById('foodsCmp').style.height = (document.body.clientHeight - 210)+'px';
}


var tastesDate = [];

var deptCmpTemplet = '<a href="javascript: of.initKitchenContent({deptId:{id}})" data-role="button" data-inline="true" class="deptKitBtnFont" data-type="deptCmp" data-value="{id}" >{name}</a>';
var kitchenCmpTemplet = '<a data-role="button" data-inline="true" class="deptKitBtnFont" data-type="kitchenCmp" data-value={id} onclick="of.findFoodByKitchen({event:this, kitchenId:{id}})">{name}</a>';	
var foodCmpTemplet = '<a data-role="button" data-corners="false" data-inline="true" class="food-style" data-value={id} onclick="{click}">' +
						'<div style="height: 70px;">{name}<br>￥{unitPrice}' +
							'<div style="position:absolute;right:0;bottom:0;font-size:20px;"><font color="FireBrick">{sellout}</font><font style="color:green;">{gift}</font></div>'+
						'</div>'+
					  '</a>'
var orderFoodCmpTemplet = '	<li data-icon={isGift} data-index={dataIndex} data-theme="c" data-value={id} data-type="orderFoodCmp" onclick="of.selectNewFood({event:this, foodId:{id}})" ><a >'+
								'<h1 style="font-size:20px;">{name}</h1>' +
								'<span style="color:green;">{tasteDisplay}</span>' +
								'<div>' +
									'<span style="float: left;color: red;">{foodStatus}</span>' +
									'<span style="float: right;">￥{unitPrice}  X <font color="green">{count}</font></span>' +
								'</div>' +
							'</a></li>';

var tasteCmpTemplet = '<a onclick="{click}" data-role="button" data-corners="false" data-inline="true" class="tasteCmp" data-index={index} data-value={id} data-theme={theme}><div>{name}<br>{price}</div></a>';

var choosedTasteCmpTemplet = '<a onclick="removeTaste({event: this, id: {id}})" data-role="button" data-corners="false" data-inline="true" class="tasteCmp" data-index={index} data-value={id}><div>{name}<br>￥{price}</div></a>';

var tasteGroupCmpTemplet = '<a data-role="button" data-inline="true" class="tastePopTopBtn" data-value={id} data-index={index} data-theme="{theme}" onclick="initTasteCmp({event:this, id:{id}})">{name}</a>';



/**
 * 设置当前输入框
 * @param id
 */
function setInput(id, callback){
	focusInput = id;
	if(callback){
		callback();
	}
}	

/**
 * 初始化部门选择
 * @param c
 */
of.initDeptContent = function(){
	var dc = $("#deptsCmp");
	var html = '<a href="javascript: of.initKitchenContent({deptId:-1})" data-role="button" data-inline="true" class="deptKitBtnFont" data-value="-1" data-type="deptCmp">全部部门</a>';
	
	of.deptPagingLimit = of.depts.root.length > 9 ? 8 : 9;
	
	var limit = of.depts.root.length >= of.deptPagingStart + of.deptPagingLimit ? of.deptPagingLimit : of.deptPagingLimit - (of.deptPagingStart + of.deptPagingLimit - of.depts.root.length);
	
	
	if(of.depts.root.length > 0){
		for (var i = 0; i < limit; i++) {
			html += deptCmpTemplet.format({
				id : of.depts.root[of.deptPagingStart + i].id,
				name : of.depts.root[of.deptPagingStart + i].name
			});
		}
	}	
		//FIXME 部门分页
	if(of.depts.root.length > 9){
		html += '<a href="javascript:of.deptGetPreviousPage()" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">L</a>' +
				'<a href="javascript:of.deptGetNextPage()" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">R</a>';
	}	
	$("#deptsCmp").html(html).trigger('create').trigger('refresh');	
};

/**
 * 部门分页
 * @param c
 */
of.deptGetNextPage = function(){
	of.deptPagingStart += of.deptPagingLimit;
	if(of.deptPagingStart > of.depts.root.length){
		of.deptPagingStart -= of.deptPagingLimit;
		return;
	}
	of.initDeptContent();
};

/**
 * 部门分页
 * @param c
 */
of.deptGetPreviousPage = function(){
	of.deptPagingStart -= of.deptPagingLimit;
	if(of.deptPagingStart < 0){
		of.deptPagingStart += of.deptPagingLimit;
		return;
	}
	of.initDeptContent();
};


/**
 * 初始化分厨选择
 * @param c
 */
of.initKitchenContent = function(c){
	c = c == null || typeof c == 'undefined' ? {} : c;
	//
	var sl = $('#deptsCmp a[data-type=deptCmp]');
	sl.attr('data-theme', 'c');
	for(var i = 0; i < sl.length; i++){
		if($(sl[i]).attr('data-value') == c.deptId){
			$(sl[i]).attr('data-theme', 'b');
		}
	}
	sl.buttonMarkup( "refresh" );
	
	of.kitchenPagingStart = 0;
	of.kitchenPagingData = [];
	var tempFoodData = []; // 菜品数据
	var temp = null;
	for(var i = 0; i < of.kitchens.root.length; i++){
		temp = of.kitchens.root[i];
		if(typeof c.deptId == 'number' && c.deptId != -1){
			if(temp.dept.id == c.deptId){
				of.kitchenPagingData.push({
					id : temp.id,
					name : temp.name
				});
				tempFoodData = tempFoodData.concat(temp.foods);
			}
		}else{
			if(temp.dept.id != -1){
				of.kitchenPagingData.push({
					id : temp.id,
					name : temp.name
				});
			}
			tempFoodData = of.foodList;
		}
	}
	temp = null;
	
	//FIXME 厨房分页
	of.showKitchenPaging();
	
	if(!of.foodPaging){
		of.foodPaging = new Util.to.padding({
			renderTo : "foodsCmp",
			data : tempFoodData,
			displayId : 'foodPagingDesc',
			templet : function(c){
				return foodCmpTemplet.format({
					id : c.data.id,
					name : c.data.name.substring(0, 10),
					unitPrice : c.data.unitPrice,
					click : 'of.insertFood({foodId:' + c.data.id + '})',
					sellout : (c.data.status & 1 << 2) != 0 ? '停' : '',
					gift : (c.data.status & 1 << 3) != 0 ? '赠' : ''					
				});
			}
		});			
	}else{
		of.foodPaging.init({
			data : tempFoodData
		});
	}
	of.foodPaging.getFirstPage();
	
	if(of.searchFooding){
		//关闭搜索
		closeSearchFood();
	}	
};

/**
 * 但div还没设置完成高度时不断刷新
 */
function keepLoadFoodData(){
	if(!$('#foodsCmp').html()){
		of.initKitchenContent({deptId:-1});
	}else{
		clearInterval(of.loadFoodDateAction);
	}
}

of.kitchenGetNextPage = function(){
	of.kitchenPagingStart += of.kitchenPagingLimit;
	if(of.kitchenPagingStart > of.kitchenPagingData.length){
		of.kitchenPagingStart -= of.kitchenPagingLimit;
		return;
	}
	of.showKitchenPaging();
};

of.kitchenGetPreviousPage = function(){
	of.kitchenPagingStart -= of.kitchenPagingLimit;
	if(of.kitchenPagingStart < 0){
		of.kitchenPagingStart += of.kitchenPagingLimit;
		return;
	}
	of.showKitchenPaging();
};

//显示厨房分页
of.showKitchenPaging = function(){
	var kc = $("#kitchensCmp");
	var html = '<a onclick="of.findFoodByKitchen({event:this, kitchenId:-1})" data-role="button" data-inline="true" data-type="kitchenCmp" data-value=-1 class="deptKitBtnFont">全部厨房</a>';
	
	of.kitchenPagingLimit = of.kitchenPagingData.length > 9 ? 8 : 9;
	
	var limit = of.kitchenPagingData.length >= of.kitchenPagingStart + of.kitchenPagingLimit ? of.kitchenPagingLimit : of.kitchenPagingLimit - (of.kitchenPagingStart + of.kitchenPagingLimit -of.kitchenPagingData.length);
	
	if(of.kitchenPagingData.length > 0){
		for (var i = 0; i < limit ; i++) {
			html += kitchenCmpTemplet.format({
				id : of.kitchenPagingData[of.kitchenPagingStart + i].id,
				name : of.kitchenPagingData[of.kitchenPagingStart + i].name
			});
		}
	}
		//FIXME 部门分页
	if(of.kitchenPagingData.length > 9){
		html += '<a href="javascript:of.kitchenGetPreviousPage()" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">L</a>' +
				'<a href="javascript:of.kitchenGetNextPage()" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">R</a>';
	}	
	kc.html(html).trigger('create').trigger('refresh');

};

/**
 * 加载点菜页面数据
 */
of.show = function(c){
	
	of.table = c.table;
	of.order = typeof c.order != 'undefined' ? c.order : null;
	of.afterCommitFn = typeof c.callback == 'function' ? c.callback : null;
	
	//加载菜品数据
	toOrderFoodPage(of.table);	
};



/**
 * 分厨选菜
 */
of.findFoodByKitchen = function(c){
	c = c == null || typeof c == 'undefined' ? {} : c;
	//
	var sl = $('#kitchensCmp > a[data-type=kitchenCmp]');
	sl.attr('data-theme', 'c');
	if(c.event){
		$(c.event).attr('data-theme', 'b');
	}
	
	sl.buttonMarkup( "refresh" );
	
	var tempFoodData = [];
	var temp = null;
	if(c.kitchenId == -1){
		var dl = $('#deptsCmp a[data-theme=b]');
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
	of.foodPaging.init({
		data : tempFoodData
	});
	of.foodPaging.getFirstPage();
	
	if(of.searchFooding){
		//关闭搜索
		closeSearchFood();
	}	
};

/**
 * 点菜
 */
of.insertFood = function(c){
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
			renderTo : 'orderFoodMgr'
		});
		return;
	}else{
		if((data.status & 1 << 2) != 0){
		 	Util.msg.alert({
				msg : '此菜品已停售!',
				topTip : true
			}); 
			return;
		}
	}
	
	//获取菜品常用口味
	$.post('../QueryFoodTaste.do', {foodID:c.foodId}, function(jr){
		if(jr.success && jr.root.length > 0){
			of.commonTastes = jr.root;
			foodCommonTasteLoad();
		}else{
			$('#divFoodTasteFloat').hide();
		}		
	});
	
	//
	var has = false;
	for(var i = 0; i < of.newFood.length; i++){
		if(of.newFood[i].id == data.id){
			has = true;
			of.newFood[i].count++;
			of.selectedOrderFood = of.newFood[i];
			break;
		}
	}
	if(!has){
		data.count = 1;
		data.isHangup = false;
		data.tasteGroup = {
			tastePref : '无口味',
			price : 0,
			normalTasteContent : []
		};
		of.newFood.push(data);
		
		//选中菜品
		of.selectedOrderFood = data;
	}
	//
	of.initNewFoodContent({
		data : data
	});
	data = null;

	if(typeof c.callback == 'function'){
		c.callback();
	}
};

/**
 * 初始化新点菜区域
 */
of.initNewFoodContent = function(c){
	c = c == null ? {} : c;
	//添加临时菜
	if(typeof c.record != 'undefined'){
		of.newFood.push(c.record);
		c.data = c.record;
	}
	var html = [], sumCount = 0, sumPrice = 0;
	var temp = null, tempUnitPrice = 0;
	for(var i = 0; i < of.newFood.length; i++){
		temp = of.newFood[i];
		tempUnitPrice = typeof temp.tasteGroup.price != 'number' ? 0 : parseFloat(temp.unitPrice + temp.tasteGroup.price);
		
		sumCount += temp.count;
		sumPrice += temp.count * tempUnitPrice;
		
		var foodStatus = '';
		if(typeof temp.isHangup == 'boolean' && temp.isHangup){
			foodStatus += '叫起';
		}
		
		if(typeof temp.isTemporary == 'boolean' && temp.isTemporary){
			if(foodStatus){
				foodStatus += '，临时菜';
			}else{
				foodStatus += '临时菜';
			}
		}
		var orderFoodHtmlData = {
			dataIndex : i,
			id : temp.id,
			name : temp.name,
			count : temp.count,
			unitPrice : tempUnitPrice.toFixed(2),
			totalPrice : tempUnitPrice.toFixed(2),
			foodStatus : foodStatus,
			isGift : typeof temp.isGift == 'boolean' && temp.isGift ? 'forFree' : 'false'
		};
		//临时口味
		if(typeof temp.tasteGroup.tmpTaste != 'undefined'){
			orderFoodHtmlData.tasteDisplay = temp.isTemporary == true ? '' : temp.tasteGroup.tastePref;
		}else{
			orderFoodHtmlData.tasteDisplay = typeof temp.tasteGroup == 'undefined' 
				|| typeof temp.tasteGroup.normalTasteContent == 'undefined' 
					|| temp.tasteGroup.normalTasteContent.length <= 0 
						|| temp.isTemporary == true ? '' : temp.tasteGroup.tastePref;
		}
		
		html.push(orderFoodCmpTemplet.format(orderFoodHtmlData));
		
		orderFoodHtmlData = null;
	}
	temp = null;
	tempUnitPrice = null;
	if(sumCount > 0){
		$('#divDescForCreateOrde div:first').html('总数量:{count}, 合计:￥{price}'.format({
			count : sumCount.toFixed(2),
			price : sumPrice.toFixed(2)
		}));		
	}else{
		$('#divDescForCreateOrde div:first').html('');
	}
	
	$('#orderFoodsCmp').html(html.join(''));
	
	$('#orderFoodsCmp').listview("refresh"); 
	
	//刷新界面后重新选中点的菜
	if(c.data != null && typeof c.data != 'undefined'){
		var select = $('#orderFoodsCmp > li[data-value='+c.data.id+']');
		if(select.length > 0){
			select.attr('data-theme', 'e').removeClass('ui-btn-up-c').addClass('ui-btn-up-e');
			$('#divOrderFoodsCmp').animate({
				scrollTop: document.getElementById('divOrderFoodsCmp').scrollHeight / of.newFood.length * select.attr('data-index')
			}, 'fast');
		}else{
			$('#divOrderFoodsCmp').animate({scrollTop: 0}, 'fast');
		}
	}else{
		$('#divOrderFoodsCmp').animate({scrollTop: 0}, 'fast');
	}
};

/**
 * 选中菜品
 */
of.selectNewFood = function(c){
	if(c == null || typeof c.foodId != 'number'){
		return;
	}
	var orderFood = $(c.event);
	
	//选中菜品
	var sl = $('#orderFoodsCmp li[data-type=orderFoodCmp]');
	sl.attr('data-theme', 'c').removeClass('ui-btn-up-e').addClass('ui-btn-up-c');
	orderFood.attr('data-theme', 'e').removeClass('ui-btn-up-c').addClass('ui-btn-up-e');
	
	of.selectedOrderFood = of.newFood[orderFood.attr('data-index')];
};

/**
 * 操作菜品数量
 */
of.operateFoodCount = function(c){
	var foodContent = $('#orderFoodsCmp > li[data-theme=e]');
	if(foodContent.length != 1){
		Util.msg.alert({
			msg : '请选中一道菜品',
			topTip : true
		});
		return;
	}
	var data = of.newFood[foodContent.attr('data-index')];
	if(typeof c.otype == 'string'){
		if(c.otype.toLowerCase() == 'delete'){
			Util.msg.alert({
				title : '重要',
				msg : '是否去除该菜品?',
				buttons : 'YESBACK',
				renderTo : 'orderFoodMgr',
				certainCallback : function(btn){
					if(btn == 'yes'){
						of.newFood.splice(foodContent.attr('data-index'), 1);
						of.initNewFoodContent({
							data : data
						});
					}
				}
			});
			return;
		}else if(c.otype.toLowerCase() == 'set'){
			of.calculator = $('#calculator4orderFoodCount');
			
			$('#orderFoodCountSet').popup('open');
			$('#orderFoodCountSet').parent().addClass("pop").addClass("in");
			
			firstTimeInput = true;
			$('#inputOrderFoodCountSet').val(data.count);
			$('#inputOrderFoodCountSet').focus();
			$('#inputOrderFoodCountSet').select();
			
			of.selectedOrderFood = data;
			
			return;
		}
	}else{
		var nc = data.count + c.count;
		if(nc <= 0){
			of.operateFoodCount({
				otype : 'delete'
			});
		}else{
			data.count = nc;
		}
	}
	
	of.initNewFoodContent({
		data : data
	});
};

/**
 * 添加菜品
 */
of.addFood = function(){
	of.operateFoodCount({
		count : 1
	});
};
/**
 * 减少菜品
 */
of.cutFood = function(){
	of.operateFoodCount({
		count : -1
	});
};
/**
 *设置菜品数量
 */
of.setFood = function(){
	of.operateFoodCount({
		otype : 'set'
	});

};
of.saveForSetFood = function(c){
	var count = parseFloat($("#" + focusInput).val());
	
	//FIXME 关闭后才能判断数量
	$('#orderFoodCountSet').popup({  
	    afterclose: function (event, ui) {  
	    	if(count <= 0){
				of.operateFoodCount({
					otype : 'delete'
				});
			}else{
				var data = of.selectedOrderFood;
				data.count = count;
				of.initNewFoodContent({
					data : data
				});
			}	
	    }  
	});	
	
	$('#orderFoodCountSet').popup('close');

};
/**
 * 删除菜品
 */
of.deleteFood = function(){
	of.operateFoodCount({
		otype : 'delete'
	});
};

/**
 * 叫起
 * params
 *  type: 1:全单叫起 2:单个叫起
 */
of.foodHangup = function(c){
	if(c == null || typeof c.type != 'number'){
		return;
	}
	
	if(c.type == 1){
		//关闭更多控件
		$('#orderFoodOtherOperateCmp').popup('close');
		
		var isHangup;
		for(var i = 0; i < of.newFood.length; i++){
			if(i == 0){
				isHangup = typeof of.newFood[i].isHangup != 'boolean' ? true : !of.newFood[0].isHangup;
			}
			of.newFood[i].isHangup = isHangup;
		}
		of.initNewFoodContent();
	}else if(c.type == 2){
		var foodContent = $('#orderFoodsCmp > li[data-theme=e]');
		if(foodContent.length != 1){
			Util.msg.alert({
				msg : '请选中一道菜品',
				topTip : true
			});
			return;
		}
		var data = of.newFood[foodContent.attr('data-index')];
		data.isHangup = typeof data.isHangup != 'boolean' ? true : !data.isHangup;;
		of.initNewFoodContent({
			data : data
		});			

	}
};

/**
 * 赠送菜品
 */
of.giftFood = function(c){
	var foodContent = $('#orderFoodsCmp > li[data-theme=e]');
	if(foodContent.length != 1){
		Util.msg.alert({
			msg : '请选中一道菜品',
			topTip : true
		});
		return;
	}
	var data = of.newFood[foodContent.attr('data-index')];
	if((data.status& 1 << 3) == 0){
		Util.msg.alert({
			msg : '此菜品不可赠送',
			topTip : true
		});
		return;	
	}
	
	data.isGift = typeof data.isGift != 'boolean' ? true : !data.isGift;
	of.initNewFoodContent({
		data : data
	});
};

/**
 * 口味操作 
 * type : 1全单, 2单个口味
 */
function operateOrderFoodTaste(c){
//	//去除动作标记
//	delete of.allBillTasteAction	
	var foodContent = $('#orderFoodsCmp > li[data-theme=e]');
	if(c.type == 2 && foodContent.length != 1){
		Util.msg.alert({
			msg : '请选中一道菜品',
			topTip : true
		});
		return;
	}else if(c.type == 1 && of.newFood.length <= 0){
		Util.msg.alert({
			msg : '还未选择菜品',
			topTip : true
		});
		return;		
	}	
	
	of.ot.allBill = c.type;
	var selectedOrderFood = typeof of.newFood[foodContent.attr('data-index')] != 'undefined' ? of.newFood[foodContent.attr('data-index')] : of.newFood[0];
	
	if(c.type == 2 && typeof selectedOrderFood.isTemporary == 'boolean' && selectedOrderFood.isTemporary){
		Util.msg.alert({
			msg : '临时菜不能选择口味.',
			topTip : true
		});
		return;
	}
	//全单口味
	of.selectedOrderFood = (c.type == 1 ? {} :selectedOrderFood);
	
	var foodTasteGroup = [];
	//普通口味时
	if(c.type == 2){
		//获取常用口味组
		if(of.commonTastes.length <= 0){
			of.commonTastes = of.allTastes;
		}
		//关闭弹出常用口味 && 手写板
		closeFoodCommonTaste();
		if(of.searchFooding){
			YBZ_win.close();
		}
		
		for (var i = 0; i < of.tasteGroups.length; i++) {
			if(of.tasteGroups[i].id == -10){//表示常用
				of.tasteGroups[i].name = '常用口味';
				of.tasteGroups[i].items = of.commonTastes;
			}
		}
		
/*		if(!commonTastesGroup){
			of.tasteGroups.unshift({
				id : -10,
				items : of.commonTastes				
			});
		}*/
		
		//赋值给口味数据组
		tastesDate = of.commonTastes;
		of.ot.tasteGroupClick = true;
		
		if(typeof of.selectedOrderFood.tasteGroup != 'undefined'){
			var tg = of.selectedOrderFood.tasteGroup.normalTasteContent.slice(0);
			
			for (var i = 0; i < tg.length; i++) {
				foodTasteGroup.push({taste:tg[i]});
			}
			
			//临时口味
			if(typeof of.selectedOrderFood.tasteGroup.tmpTaste != 'undefined'){
				foodTasteGroup.push({taste : of.selectedOrderFood.tasteGroup.tmpTaste});
			}
			
			tg = null;
		}	
	}else{
		//关闭更多控件
		$('#orderFoodOtherOperateCmp').popup('close');
		
		for (var i = 0; i < of.tasteGroups.length; i++) {
			if(of.tasteGroups[i].id == -10){//表示常用
				of.tasteGroups[i].name = '所有口味';
				of.tasteGroups[i].items = of.allTastes;
			}
		}		
/*		if(of.ot.allBillTaste){
			foodTasteGroup = foodTasteGroup.concat(of.ot.allBillTaste);
			tastesDate = foodTasteGroup;
		}else{
			tastesDate = of.allTastes;
		}*/
		if(of.ot.allBillTaste){
			foodTasteGroup = foodTasteGroup.concat(of.ot.allBillTaste);
		}
		tastesDate = of.allTastes;			
	}
	
	//全单口味则只把全单类型的放入
	of.ot.choosedTastes = foodTasteGroup;
	
	//初始化口味操作组件
	initTasteGroupCmp();
	initTasteCmp();
	initChoosedTasteCmp();
	
	//单项口味不用延迟
	if(c.type == 2){
		$('#orderFoodTasteCmp').popup('open');
		$('#orderFoodTasteCmp').parent().addClass("pop").addClass("in");		
	}else{
		setTimeout(function(){
			$('#orderFoodTasteCmp').popup('open');
			$('#orderFoodTasteCmp').parent().addClass("pop").addClass("in");
		},250);		
	}
	

};
/**
 * 口味操作返回
 */
of.ot.back = function(){
	$('#orderFoodTasteCmp').popup('close');
	//清空临时口味id
	of.ot.tasteId = null;
	of.selectedOrderFood = null;
	of.ot.choosedTastes = [];
	$('#divDescForChooseTaste').html('');
	
	if(of.searchFooding){
		YBZ_open(document.getElementById('searchFoodInput'));
		$('#searchFoodInput').focus();		
	}
};

/**
 * 选中口味
 * @param c
 */
function chooseTaste(c){
	var currentTaste = $(c.event);
	var tdata;
	for (var i = 0; i < tastesDate.length; i++) {
		if(tastesDate[i].taste.id == c.id){
			tdata = tastesDate[i];
		}
	}
	
	if(currentTaste.attr('data-theme') == 'e'){
		currentTaste.attr('data-theme', 'c').removeClass('ui-btn-up-e').addClass('ui-btn-up-c');
		for (var i = 0; i < of.ot.choosedTastes.length; i++) {
			if(of.ot.choosedTastes[i].taste.id == parseInt(currentTaste.attr('data-value'))){
				of.ot.choosedTastes.splice(i, 1);
				break;
			}
		}
	}else{
		currentTaste.attr('data-theme', 'e').removeClass('ui-btn-up-c').addClass('ui-btn-up-e');
		of.ot.choosedTastes.push(tdata);
	}
	//刷新已点口味 & 口味 
	initChoosedTasteCmp();
	$('#tastesCmp a').buttonMarkup( "refresh" );
}

/**
 * 初始化已选口味
 */
function initChoosedTasteCmp(){
	var html="";
	for (var i = 0; i < of.ot.choosedTastes.length; i++) {
		if(i > 0){
			html+= ", ";
		}
		html += of.ot.choosedTastes[i].taste.name;
	}
	
	$('#divDescForChooseTaste').html(html);
}

/**
 * 初始化未选口味数据
 * @param c
 */
function initTasteCmp(c){
	//动态改变口味数据
	if(c && c.event){
		var tGroup = $(c.event);
		var glist = $('#tasteGroupCmp a');
		tastesDate = of.tasteGroups[parseInt(tGroup.attr('data-index'))].items;
		
		//刷新样式
		glist.attr('data-theme', 'b');
		tGroup.attr('data-theme', 'e').removeClass('ui-btn-up-b').addClass('ui-btn-up-e');
		glist.buttonMarkup("refresh");
		of.ot.tasteGroupClick = true;
	}
	of.ot.tastePaging = Util.to.padding({
		renderTo : "tastesCmp",
		data : tastesDate,
		displayId : 'tastePagingDesc',
		templet : function(c){
			//默认不选中
			var theme = "c";
			//当从口味组进入时, 恢复选中状态
			if(of.ot.tasteGroupClick){
				for (var k = 0; k < of.ot.choosedTastes.length; k++) {
					if(c.data.taste.id == of.ot.choosedTastes[k].taste.id){
						theme = "e";
						break;
					}
				}
			}			
			return tasteCmpTemplet.format({
				index : c.index,
				id : c.data.taste.id,
				name : c.data.taste.name,
				click : "chooseTaste({event: this, id: "+ c.data.taste.id +"})",
				price : c.data.taste.calcValue == 1?(c.data.taste.rate * 100) + '%' : ('￥'+ c.data.taste.price),
				theme : theme//是否选中
			});
		}
	});	
	of.ot.tastePaging.getFirstPage();
}

/**
 * 口味分页
 */
function tasteCmpNextPage(){
	of.ot.tasteGroupClick = true;
	of.ot.tastePaging.getNextPage();
}
/**
 * 口味分页
 */
function tasteCmpPrePage(){
	of.ot.tasteGroupClick = true;
	of.ot.tastePaging.getPreviousPage();
}

/**
 * 初始化口味组
 */
function initTasteGroupCmp(c){
	
	of.ot.tasteGroupPagingLimit = of.tasteGroups.length > 7 ? 6 : 7;
	
	var limit = of.tasteGroups.length >= of.ot.tasteGroupPagingStart + of.ot.tasteGroupPagingLimit ? of.ot.tasteGroupPagingLimit : of.ot.tasteGroupPagingLimit - (of.ot.tasteGroupPagingStart + of.ot.tasteGroupPagingLimit - of.tasteGroups.length);
	
	var html = "";
	if(of.tasteGroups.length > 0){
		for (var i = 0; i < limit; i++) {
			html += tasteGroupCmpTemplet.format({
				index : i,
				id : of.tasteGroups[of.ot.tasteGroupPagingStart + i].id,
				name : of.tasteGroups[of.ot.tasteGroupPagingStart + i].name,
				theme : of.tasteGroups[of.ot.tasteGroupPagingStart + i].id == -10 && of.ot.allBill ==2 ? "e" : "b"
			});
		}
	}	
	
	if(of.tasteGroups.length > 7){
		html += '<a onclick="tasteGroupGetPreviousPage()" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="tasteGroupPage">L</a>' +
				'<a onclick="tasteGroupGetNextPage()" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="tasteGroupPage">R</a>';
	}	
	
	$("#tasteGroupCmp").html(html).trigger('create');	
};

/**
 * 口味组分页
 * @param c
 */
tasteGroupGetNextPage = function(){
	of.ot.tasteGroupPagingStart += of.ot.tasteGroupPagingLimit;
	if(of.ot.tasteGroupPagingStart > of.tasteGroups.length){
		of.ot.tasteGroupPagingStart -= of.ot.tasteGroupPagingLimit;
		return;
	}
	initTasteGroupCmp();
};

/**
 * 口味组分页
 * @param c
 */
tasteGroupGetPreviousPage = function(){
	of.ot.tasteGroupPagingStart -= of.ot.tasteGroupPagingLimit;
	if(of.ot.tasteGroupPagingStart < 0){
		of.ot.tasteGroupPagingStart += of.ot.tasteGroupPagingLimit;
		return;
	}
	initTasteGroupCmp();
};

function clone(myObj){
	  if(typeof(myObj) != 'object') return myObj;
	  if(myObj == null) return myObj;
	  
	  var myNewObj = new Object();
	  
	  for(var i in myObj)
	    myNewObj[i] = clone(myObj[i]);
	  
	  return myNewObj;
	}
/**
 * 保存口味
 */
of.ot.saveOrderFoodTaste = function(){
	var tasteGroup = of.selectedOrderFood.tasteGroup;
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
	if(typeof tasteGroup.tmpTaste != 'undefined'){
		delete tasteGroup.tmpTaste;
	}
	
	var temp = null;
	for(var i = 0; i < of.ot.choosedTastes.length; i++){
		temp = of.ot.choosedTastes[i].taste;
		if(of.ot.allBill == 1){
			temp.allBill = true;
		}
		//临时口味
		if(temp.isTemp){
			tasteGroup.tmpTaste = temp;
			tasteGroup.normalTaste.name += (i > 0 ? ',' + temp.name : temp.name);
			tasteGroup.normalTaste.price += parseInt(temp.price);
		}else{
			tasteGroup.normalTasteContent.push(temp);
			tasteGroup.normalTaste.name += (i > 0 ? ',' + temp.name : temp.name);
			if(temp.cateStatusValue == 2){
				tasteGroup.normalTaste.price += temp.price;
			}else if(temp.cateStatusValue == 1){
				tasteGroup.normalTaste.price += of.selectedOrderFood.unitPrice * temp.rate;
			}
		}
	}
	tasteGroup.tastePref = tasteGroup.normalTaste.name;
	tasteGroup.price = tasteGroup.normalTaste.price;
	
	if(of.ot.allBill == 1){
		of.ot.allBillTaste = of.ot.choosedTastes;
		for(var i = 0; i < of.newFood.length; i++){
			
			if(typeof of.newFood[i].isTemporary == 'undefined'){//临时菜无口味
				
				//全单口味应该是拼接普通口味
				if(of.newFood[i].tasteGroup.tastePref == "无口味"){
					of.newFood[i].tasteGroup.tastePref = "";
				} 
				//全单口味重新赋值
				for (var j = 0; j < of.newFood[i].tasteGroup.normalTasteContent.length; j++) {
					//与新全单口味相同的口味去除
					for (var m = 0; m < of.ot.choosedTastes.length; m++) {
						if(of.newFood[i].tasteGroup.normalTasteContent[j].id == of.ot.choosedTastes[m].taste.id){
							of.newFood[i].tasteGroup.normalTasteContent.splice(j, 1);
						}
						
						if(of.newFood[i].tasteGroup.normalTasteContent.length <= 0 || !of.newFood[i].tasteGroup.normalTasteContent[j]){
							break;
						}
					}						
				}
				
				if(of.newFood[i].tmpTaste){
					delete of.newFood[i].tmpTaste;
				}
				
				of.newFood[i].tasteGroup.tastePref = '';
				of.newFood[i].tasteGroup.price = 0;
				
				for (var k = 0; k < of.newFood[i].tasteGroup.normalTasteContent.length; k++) {
					
					if(!of.newFood[i].tasteGroup.normalTasteContent[k].allBill){
						if(of.newFood[i].tasteGroup.tastePref){
							of.newFood[i].tasteGroup.tastePref += ', ';
						}
						of.newFood[i].tasteGroup.tastePref +=  of.newFood[i].tasteGroup.normalTasteContent[k].name;
						of.newFood[i].tasteGroup.price += of.newFood[i].tasteGroup.normalTasteContent[k].price;
					}

				}
				
				of.newFood[i].tasteGroup.tastePref += of.newFood[i].tasteGroup.tastePref.trim().length > 0  ? (tasteGroup.tastePref.trim().length>0 ? "," + tasteGroup.tastePref : '') : tasteGroup.tastePref;
				of.newFood[i].tasteGroup.price += tasteGroup.price;
				
				of.newFood[i].tasteGroup.normalTasteContent = of.newFood[i].tasteGroup.normalTasteContent.concat(tasteGroup.normalTasteContent);
				
				//如果有临时口味则加上
				if(tasteGroup.tmpTaste){
					of.newFood[i].tasteGroup.tmpTaste = clone(tasteGroup.tmpTaste);
				}
			}
		}
		
		of.initNewFoodContent();
		
	}else if(of.ot.allBill == 2){
		for(var i = 0; i < of.newFood.length; i++){
			if(of.newFood[i].id == of.selectedOrderFood.id){
				of.newFood[i].tasteGroup = tasteGroup;
				break; 
			}
		}
		of.initNewFoodContent({
			data : of.selectedOrderFood
		});
	}
	
	tasteGroup = null;
	
	of.ot.back();
	
};

/**
 * 回删单个字
 * @param id
 */
function deleteSingleWord(id){
	var string = $('#'+id);
	string.val(string.val().substring(0, string.val().length - 1));
	string.focus();
}

/**
 * 添加临时口味
 */
function addTempTaste(){
	
	var foodContent = $('#orderFoodsCmp > li[data-theme=e]');
	if(foodContent.length != 1){
		Util.msg.alert({
			msg : '请选中一道菜品',
			topTip : true
		});
		return;
	}
	
	//是否选中键盘
	if(getcookie('isNeedNumKeyboard') == 'true'){
		$('#isKeyboard4Taste').attr('checked', true);
	}else{
		$('#isKeyboard4Taste').attr('checked', false);
	}
	
	if(getcookie('isNeedWriter') == 'true'){
		$('#isWriter4Taste').attr('checked', true);
	}else{
		$('#isWriter4Taste').attr('checked', false);
	}		
	
	of.selectedOrderFood = of.newFood[foodContent.attr('data-index')];
	
	$('#addTempTasteCmp').show();
	$('#addTempTasteCmp').css('top', '150px');
	$('#addTempTasteCmpShadow').show();
	
	//关闭弹出常用口味
	closeFoodCommonTaste();
	
//	if(of.searchFooding){
//		YBZ_open(document.getElementById('tempTasteName'));
//		$('#tempTasteWriterOn')[0].selectedIndex = 1;
//		$('#tempTasteWriterOn').slider('refresh');
//	}
	
	focusInput = "tempTastePrice";
	
	if(of.selectedOrderFood.tasteGroup.tmpTaste){
		$('#tempTasteName').val(of.selectedOrderFood.tasteGroup.tmpTaste.name);
		$('#tempTastePrice').val(of.selectedOrderFood.tasteGroup.tmpTaste.price);
		
		of.ot.updateTempTaste = true;
	}else{
		$('#tempTasteName').val('');
		$('#tempTastePrice').val('');
		
		of.ot.updateTempTaste = false;
	}	
	
	$('#tempTasteName').focus();
}

function closeTempTaste(){
	$('#addTempTasteCmp').hide();
	$('#addTempTasteCmpShadow').hide();
	
	$('#tempTasteName').val('');
	$('#tempTastePrice').val('');	
	
//	var numSelect = $(".numberKeyboard");
//	for (var i = 0; i < numSelect.length; i++) {
//		numSelect[i].selectedIndex = 0;
//	}
//	numSelect.slider('refresh'); 
//	
//	var ybzSelect = $(".handWriteCmp");
//	for (var i = 0; i < ybzSelect.length; i++) {
//		ybzSelect[i].selectedIndex = 0;
//	}	
//	ybzSelect.slider('refresh'); 	
	
	//关闭键盘
	$('#numberKeyboard').hide();	
	//搜索点菜中则重新打开手写
	if(of.searchFooding){
		YBZ_open(document.getElementById('searchFoodInput'));
		$('#searchFoodInput').focus();		
	}else{
		//关闭手写板
		if(YBZ_win){
			YBZ_win.close();	
		}		
	}		
}

/**
 * 保存临时口味
 */
function saveTempTaste(){
	of.ot.allBill = 2;
	var name = $('#tempTasteName').val();
	var price = $('#tempTastePrice').val();
	
	if(price == ''){
		price = 0;
	}
	
	of.ot.choosedTastes = [];
	
	for (var i = 0; i < of.selectedOrderFood.tasteGroup.normalTasteContent.length; i++) {
		of.ot.choosedTastes.push({
			taste : of.selectedOrderFood.tasteGroup.normalTasteContent[i]
		});
	}
	
	//当临时口味是修改状态时
	if(of.ot.updateTempTaste){
		if(of.selectedOrderFood.tasteGroup.tmpTaste){
			//如果名称为空则是删除
			if(name){
				of.selectedOrderFood.tasteGroup.tmpTaste.name = name;
				of.selectedOrderFood.tasteGroup.tmpTaste.price = price;
				
				of.ot.choosedTastes.push({taste : of.selectedOrderFood.tasteGroup.tmpTaste});
			}else{
				delete of.selectedOrderFood.tasteGroup.tmpTaste;
			}
			of.ot.saveOrderFoodTaste();
		}
	}else{
		if(of.ot.tasteId){
			of.ot.updateTempTaste = true;
			saveTempTaste();
		}else{
			var tasteId = -11;
			var tempTasteData = {
				id : tasteId,
				cateStatusValue : 2,
				name : name,
				price : price,
				isTemp : true
			};	
			
			of.ot.choosedTastes.push({taste : tempTasteData});
			
			of.ot.saveOrderFoodTaste();
		}
	}
	of.ot.updateTempTaste = false;	
	of.ot.tasteId = null;
	
	closeTempTaste();
}

/**
 * 搜索菜品
 * @param ope
 */
function searchFood(ope){
	if(ope=='on'){
		if(!of.s.init({file : 'searchFoodInput'})){
			Util.msg.alert({
				renderTo : 'orderFoodMgr',
				msg : '程序异常, 搜索功能无法使用, 请刷新页面后重试.',
				time : 2
			});
			return;
		}
		
		YBZ_open(document.getElementById('searchFoodInput'));
		
		$('#normalOperateFoodCmp').hide();
		$('#searchFoodCmp').show();	
		$('#searchFoodInput').focus();
		
		of.searchFooding = true;
	}else{
		
		
		var kitchen = $('#kitchensCmp > a[data-theme=b]');
		if(kitchen.length > 0){
			kitchen.click();
		}else{
			kitchen = $('#kitchensCmp > a[data-value=-1]')[0];
			kitchen.onclick();
		}
		kitchen = null;		
		
		closeSearchFood();
	}

}

function closeSearchFood(){
	of.searchFooding = false;	
	
	$('#searchFoodInput').val('');
	
	YBZ_win.close();
	$('#normalOperateFoodCmp').show();
	$('#searchFoodCmp').hide();
	
	
}

/**
 * 临时菜操作
 */
of.tf = {
	selectedKitchen : null,
	initTempKitchen : function(){
		var html = '';
		for (var i = 0; i < of.tempKitchens.length; i++) {
			html += '<li class="tempFoodKitchen" onclick="of.tf.tempFoodSelectKitchen({event:this, id:' + of.tempKitchens[i].id +'})"><a>' + of.tempKitchens[i].name +'</a></li>';
		}
		$('#tempFoodKitchensCmp').html(html).trigger('create');
		$('#tempFoodKitchensCmp').listview('refresh');
	}
}

/**
 * 添加临时菜
 */
function addTempFood(){
	of.tf.initTempKitchen();
	//默认选中第一个厨房
	of.tf.selectedKitchen = of.tempKitchens[0].id
    	
	focusInput = "tempFoodPrice";
	
	//是否选中键盘
	if(getcookie('isNeedNumKeyboard') == 'true'){
		$('#isKeyboard4Food').attr('checked', true);
	}else{
		$('#isKeyboard4Food').attr('checked', false);
	}
	
	if(getcookie('isNeedWriter') == 'true'){
		$('#isWriter4Food').attr('checked', true);
	}else{
		$('#isWriter4Food').attr('checked', false);
	}	
	
	//默认厨房
	$('#lab4TempKitchen').text(of.tempKitchens[0].name);
	
	$('#addTempFoodCmp').show();
	$('#addTempFoodCmpShadow').show();
	
//	if(of.searchFooding){
//		YBZ_open(document.getElementById('tempFoodName'));
//		$('#tempFoodWriterOn')[0].selectedIndex = 1;
//		$('#tempFoodWriterOn').slider('refresh');
//	}	
	
	$('#tempFoodName').focus();
}

/**
 * 关闭临时菜
 */
of.tf.closeTempFood = function(){
	$('#addTempFoodCmp').hide();
	$('#addTempFoodCmpShadow').hide();	
	$('#tempFoodName').val('');
	$('#tempFoodPrice').val('');
	$('#tempFoodCount').val(1);
	
//	var numSelect = $(".numberKeyboard");
//	for (var i = 0; i < numSelect.length; i++) {
//		numSelect[i].selectedIndex = 0;
//	}
//	numSelect.slider('refresh'); 
//	
//	var ybzSelect = $(".handWriteCmp");
//	for (var i = 0; i < ybzSelect.length; i++) {
//		ybzSelect[i].selectedIndex = 0;
//	}	
//	ybzSelect.slider('refresh'); 
	
	//关闭键盘
	$('#numberKeyboard').hide();	
	//搜索点菜中则重新打开手写
	if(of.searchFooding){
		YBZ_open(document.getElementById('searchFoodInput'));
		$('#searchFoodInput').focus();		
	}else{
		//关闭手写板
		if(YBZ_win){
			YBZ_win.close();	
		}		
	}	
	
}

/**
 * 临时菜选择分厨
 * @param c
 */
of.tf.tempFoodSelectKitchen = function(c){
	this.selectedKitchen = c.id;
	$('#lab4TempKitchen').text($(c.event).text());
	$('#popupTempFoodKitchensCmp').popup('close');
};

/**
 * 保存临时菜
 */
of.tf.saveTempFood = function(){
	var name = $('#tempFoodName');
	var price = $('#tempFoodPrice');
	var count = $('#tempFoodCount');
	
	if(!name.val()){
		Util.msg.alert({
			topTip : true,
			msg : '请临时菜名称'
		});		
		name.focus();
		return;
	}
	
	if(!price.val()){
		Util.msg.alert({
			topTip : true,
			msg : '请填写价格'
		});
		price.focus();
		return;	
	}else if(isNaN(price.val()) || parseFloat(price.val()) < 0){
		Util.msg.alert({
			topTip : true,
			msg : '请填写正确的价格'
		});
		price.focus();
		return;		
	}

	if(!count.val()){
		count.val(1)
	}else if(isNaN(count.val()) || parseFloat(count.val()) < 1){
		Util.msg.alert({
			topTip : true,
			msg : '请填写正确的数量'
		});
		return;		
	}	
	
	
	of.initNewFoodContent({
		record : {
			isTemporary : true,
			id : (new Date().getTime()+'').substring(5, 9),
			alias : (new Date().getTime()+'').substring(5, 9),
			name : name.val(),
			count : parseFloat(count.val()),
			unitPrice : parseFloat(price.val()),
			isHangup : false,
			kitchen : {
				id : this.selectedKitchen
			},
			tasteGroup : {
				tastePref : '无口味',
				price : 0,
				normalTasteContent : []
			}
		}
	});	
	
	of.tf.closeTempFood();
}

//常用口味
function foodCommonTasteLoad(){
	of.ot.choosedTastes = [];
	var html = '';
	for (var i = 0; i < of.commonTastes.length; i++) {
		html += tasteCmpTemplet.format({
			index : i,
			id : of.commonTastes[i].taste.id,
			name : of.commonTastes[i].taste.name,
			click : "chooseOrderFoodCommonTaste({event: this, id: "+ of.commonTastes[i].taste.id +"})",
			price : of.commonTastes[i].taste.price,
			theme : "c"
		});		
	}
	
	html += '<a onclick="operateOrderFoodTaste({type:2})" data-role="button" data-corners="false" data-inline="true" class="moreTasteCmp" data-theme="b">更多口味</a>' +
			'<a onclick="addTempTaste()" data-role="button" data-corners="false" data-inline="true" class="moreTasteCmp" data-theme="b">手写口味</a>';
	
	$("#divFloatFoodTastes").html(html).trigger('create');	
	
	$('#txtChooosedFoodName').text(of.selectedOrderFood.name);
	
	//在搜索时, 口味显示在上方
	if(of.searchFooding){
		$('#divFoodTasteFloat').css({top : '130px', bottom : 'initial'});
	}else{
		$('#divFoodTasteFloat').css({top : 'initial', bottom : '90px'});
	}
	
	$('#divFoodTasteFloat').show();
}

//关闭常用口味
function closeFoodCommonTaste(){
	$('#divFoodTasteFloat').hide();
	$("#divFloatFoodTastes").html('');	
}

//常用口味选中
function chooseOrderFoodCommonTaste(c){
	var currentTaste = $(c.event);
	var tdata = of.commonTastes[parseInt(currentTaste.attr('data-index'))];
	
	if(currentTaste.attr('data-theme') == 'e'){
		currentTaste.attr('data-theme', 'c').removeClass('ui-btn-up-e').addClass('ui-btn-up-c');
		for (var i = 0; i < of.ot.choosedTastes.length; i++) {
			if(of.ot.choosedTastes[i].taste.id == parseInt(currentTaste.attr('data-value'))){
				of.ot.choosedTastes.splice(i, 1);
				break;
			}
		}
	}else{
		currentTaste.attr('data-theme', 'e').removeClass('ui-btn-up-c').addClass('ui-btn-up-e');
		of.ot.choosedTastes.push(tdata);
	}

	$("#divFloatFoodTastes a").buttonMarkup( "refresh" );
	
	var tasteGroup = of.selectedOrderFood.tasteGroup;
	
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
	if(typeof tasteGroup.tmpTaste != 'undefined'){
		delete tasteGroup.tmpTaste;
	}
	
	var temp = null;
	for(var i = 0; i < of.ot.choosedTastes.length; i++){
		temp = of.ot.choosedTastes[i].taste;
		if(of.ot.allBill == 1){
			temp.allBill = true;
		}
		//临时口味
		if(temp.isTemp){
			tasteGroup.tmpTaste = temp;
			tasteGroup.normalTaste.name += (i > 0 ? ',' + temp.name : temp.name);
			tasteGroup.normalTaste.price += parseInt(temp.price);
		}else{
			tasteGroup.normalTasteContent.push(temp);
			tasteGroup.normalTaste.name += (i > 0 ? ',' + temp.name : temp.name);
			if(temp.cateStatusValue == 2){
				tasteGroup.normalTaste.price += temp.price;
			}else if(temp.cateStatusValue == 1){
				tasteGroup.normalTaste.price += of.selectedOrderFood.unitPrice * temp.rate;
			}
		}
	}
	tasteGroup.tastePref = tasteGroup.normalTaste.name;
	tasteGroup.price = tasteGroup.normalTaste.price;	
	
	for(var i = 0; i < of.newFood.length; i++){
		if(of.newFood[i].id == of.selectedOrderFood.id){
			of.newFood[i].tasteGroup = tasteGroup;
			break; 
		}
	}
	
	of.initNewFoodContent({
		data : of.selectedOrderFood
	});	
	
	tasteGroup = null;
}

function scrolldown(c){
	if(!c){
		c = {};
	}
	var dom = document.getElementById('divOrderFoodsCmp');
	if(dom.scrollHeight == dom.scrollTop){
	}else if(dom.scrollHeight - dom.scrollTop < 50){
		dom.scrollTop = dom.scrollHeight;
	}else{
		$('#divOrderFoodsCmp').animate({scrollTop: dom.scrollTop + (typeof c.size == 'number' ? c.size : 50 * 3)}, 'fast');
	}	
}

/**
 * 打开助记码
 */
of.openAliasOrderFood = function(){
	setTimeout(function(){
		$('#txtFoodAlias').focus();
	}, 300);
	
	$('#orderFoodByAliasCmp').popup('open');
	$('#orderFoodByAliasCmp').parent().addClass("pop").addClass("in");	
	

}

/**
 * 关闭助记码
 */
of.closeAliasOrderFood = function(){
	$('#orderFoodByAliasCmp').popup('close');
	
	$('#txtFoodAlias').val("");
}

/**
 * 助记码点菜
 */
of.findByAliasAction = function(c){
	var alias = $('#txtFoodAlias');
	if(!alias.val()){
		Util.msg.alert({
			msg : '请填写助记码',
			topTip : true
		});
		alias.focus();
		return;
	}
	
	var data = null, temp = null;
	temp = of.foodList.slice(0);
	for(var i = 0; i < temp.length; i++){
		if(temp[i].alias == alias.val()){
			data = temp[i];
		}
	}
	if(data == null){
		Util.msg.alert({
			topTip : true,
			msg : '此编码无对应菜品'
		});
		alias.focus();
	}else{
		of.insertFood({foodId : data.id});
	}
	alias.val("");
	data = null;
	temp = null;
};


/**
 * 下单不打印
 */
of.orderWithNoPrint = function(){
	$('#orderOtherOperateCmp').popup('close');
	setTimeout(function(){
		of.submit({notPrint : true});	
	}, 250);
}


/**
 * 账单提交
 */
of.submit = function(c){
	if(of.newFood == null || typeof of.newFood == 'undefined' || of.newFood.length == 0){
		Util.msg.alert({
			topTip : true,
			msg : '请选择菜品后再继续操作.'
		});
		return;
	}
	
	var foodData = [], isFree = true;
	if(of.table.statusValue == 1){
		isFree = false;
		foodData = of.newFood.slice(0).concat(of.order.orderFoods.slice(0));
	}else{
		isFree = true;
		foodData = of.newFood.slice(0);
	}
	
	Util.LM.show();
	
	orderDataModel.tableID = of.table.id;
	orderDataModel.customNum = of.table.customNum;
	orderDataModel.orderFoods = (typeof c.commitType != 'undefined'? of.newFood.slice(0) : foodData);
	orderDataModel.categoryValue =  of.table.categoryValue;
	if(!isFree){
		orderDataModel.id = of.order.id;
		orderDataModel.orderDate = of.order.orderDate;
	}
	
	$.ajax({
		url : '../InsertOrder.do',
		type : 'post',
		data : {
			commitOrderData : JSON.stringify(Wireless.ux.commitOrderData(orderDataModel)),
			type : (typeof c.commitType != 'undefined'? c.commitType : isFree ? 1 : 7),
			notPrint : c.notPrint
		},
		success : function(data, status, xhr) {
			//下单成功时才出现倒数, 否则提示是否强制提交
			if (data.success == true) {
					if(typeof c.tempPrint != 'undefined'){
						$.ajax({
							url : '../QueryOrderByCalc.do',
							type : 'post',
							data : {
								calc:false,
								serviceRate:0,
								tableID:of.table.id
							},
							dataType : 'text',
							success : function(results, status, xhr){
								if(typeof results == "string"){
									results = eval("(" + results + ")");
								}
								
								if(results.success){
									$.ajax({
										url : '../PayOrder.do',
										type : 'post',
										data : {
											orderID : results.other.order.id,
											cashIncome : '-1',
											tempPay : true,
											isPrint : typeof c.isPrint == 'boolean' ? c.isPrint : true
										},
										dataType : 'text',
										success : function(result, status, xhr){
											Util.LM.hide();
											if(typeof result == "string"){
												result = eval("(" + result + ")");
											}
											if(result.success){
												Util.msg.alert({
													topTip : true,
													msg : result.data,
												});
												//暂结应该是没有回调方法的
												console.log('暂结')
//												if(of.afterCommitFn != null && typeof of.afterCommitFn == 'function'){
//													of.afterCommitFn();
//												}
				//								initOrderData({table : uo.table});
											}else{
												Util.msg.alert({
													title : '错误',
													renderTo : 'orderFoodMgr',
													msg : result.data,
													time : 3
												});
											}
										},
										error : function(xhr, status, err){
											Util.LM.hide();
											Util.msg.alert({
												title : '错误',
												renderTo : 'orderFoodMgr',
												msg : err,
												time : 3
											});
										}
									});
								}else{
									Util.LM.hide();
									Util.msg.alert({
										title : '错误',
										renderTo : 'orderFoodMgr',
										msg : results.data,
										time : 3
									});
								}
							},
							error : function(xhr, status, err){
								Util.LM.hide();
								Util.msg.alert({
									title : '错误',
									renderTo : 'orderFoodMgr',
									msg : err,
									time : 3
								});
							}
						});

					}else{
						of.newFood = [];
						Util.LM.hide();
						if(of.searchFooding){
							//关闭搜索
							closeSearchFood();
						}	
						
						Util.msg.alert({
							msg : data.msg,
							topTip : true,
						});
						
						if(of.afterCommitFn != null && typeof of.afterCommitFn == 'function'){
								of.afterCommitFn();
						}else{//没有回调函数直接退回主界面
							uo.back();
						}						
					}
			} else {
				Util.LM.hide();
				Util.msg.alert({
					title : data.title,
					renderTo : 'orderFoodMgr',
					msg : data.msg, 
					buttons : 'YESBACK',
					btnEnter : '继续提交',
					fn : function(btn){
						if(btn == 'yes'){
							c.commitType = 23;
							of.submit(c);
						}
					}
				});

			}
		},
		error : function(request, status, err) {
			Util.LM.hide();
			Util.msg.alert({
				title : '错误',
				renderTo : 'orderFoodMgr',
				msg : err
			});
		}
	});
};


function isNeedWriter(c){
	
	if($(c.event).attr("checked") == true){
		setcookie('isNeedWriter', 'true');
	}else{
		delcookie('isNeedWriter')
	}
	
	$(c.event).checkboxradio('refresh');
}

function isNeedNumKeyboard(c){
	$('#isKeyboard4Taste').checkboxradio('refresh');
}

//点菜界面数据对象
var of = {
	table : {},
	order : {},
	deptPaging : {},	
	deptPagingStart : 0,
	ot : {
		tasteGroupPagingStart : 0,
		choosedTastes : [],
		allBill : false
	},
	selectedOrderFood : {},
	commonTastes : [],
	multiPrices : [],
	calculator : {},
	newFood : []
},
	//不同条件下选出的口味
	tastesDate = [],

	//口味动态弹出时鼠标范围
	mouseOutFoodSelect = false,	
	/**
	 * 元素模板
	 */
	//部门列表
	deptCmpTemplet = '<a href="javascript: of.initKitchenContent({deptId:{id}})" data-role="button" data-inline="true" class="deptKitBtnFont" data-type="deptCmp" data-value="{id}" >{name}</a>',
	//厨房列表
	kitchenCmpTemplet = '<a data-role="button" data-inline="true" class="deptKitBtnFont" data-type="kitchenCmp" data-value={id} onclick="of.findFoodByKitchen({event:this, kitchenId:{id}})">{name}</a>',
	//菜品列表
	foodCmpTemplet = '<a data-role="button" data-corners="false" data-inline="true" class="food-style" data-value={id} onclick="{click}">' +
							'<div style="height: 70px;">{name}<br>￥{unitPrice}' +
								'<div class="food-status-font">' +
									'<font color="orange">{weigh}</font>' +
									'<font color="blue">{currPrice}</font>' +
									'<font color="FireBrick">{sellout}</font>' +
									'<font color="green">{gift}</font>' +
								'</div>'+
							'</div>'+
						  '</a>',
	//已点菜列表					  
	orderFoodCmpTemplet = '	<li data-icon={isGift} data-index={dataIndex} data-unique={unique} data-theme="c" data-value={id} data-type="orderFoodCmp" onclick="of.selectNewFood({event:this, foodId:{id}})" ><a >'+
									'<h1 style="font-size:20px;">{name}</h1>' +
									'<span style="color:green;">{tasteDisplay}</span>' +
									'<div>' +
										'<span style="float: left;color: red;">{foodStatus}</span>' +
										'<span style="float: right;color: blue;margin-left:5px;">{multiPriceUnit}</span>' +
										'<span style="float: right;">￥{unitPrice}  X <font color="green">{count}</font></span>' +
									'</div>' +
								'</a></li>',
	//口味列表
	tasteCmpTemplet = '<a onclick="{click}" data-role="button" data-corners="false" data-inline="true" class="tasteCmp" data-index={index} data-value={id} data-theme={theme}><div>{name}<br>{price}</div></a>',
	//选中口味
	choosedTasteCmpTemplet = '<a onclick="removeTaste({event: this, id: {id}})" data-role="button" data-corners="false" data-inline="true" class="tasteCmp" data-index={index} data-value={id}><div>{name}<br>￥{price}</div></a>',
	//口味组
	tasteGroupCmpTemplet = '<a data-role="button" data-inline="true" class="tastePopTopBtn" data-value={id} data-index={index} data-theme="{theme}" onclick="initTasteCmp({event:this, id:{id}})">{name}</a>';
	//多单位
	multiPriceCmpTemplet = '<a onclick="{click}" data-role="button" data-corners="false" data-inline="true" class="multiPriceCmp" data-index={index} data-value={id} data-theme={theme}><div>{multiPrice}</div></a>',
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
				
				if(data){
					data = data.sort(of.searchFoodCompare);
				}
				
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
						currPrice : (c.data.status & 1 << 4) != 0 ? '时' : '',		
						gift : (c.data.status & 1 << 3) != 0 ? '赠' : ''	,
						weigh : (c.data.status & 1 << 7) != 0 ? '称' : ''				
					});
				},
				pagedCallBack : function(){
					//FIXME .food-status-font中position:absolute不起作用
					setTimeout(function(){
						$(".food-status-font").css("position", "absolute");
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
		of.s.clear();
	},
	fireEvent : function(){
		of.s.onInput();
	}
};


//设置搜索出来的菜品的排序依据, 按点击次数
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
	var html = ['<a href="javascript: of.initKitchenContent({deptId:-1})" data-role="button" data-inline="true" class="deptKitBtnFont" data-value="-1" data-type="deptCmp">全部部门</a>'];
	
	//真实宽度
	var usefullWidth = document.body.clientWidth - 220;
	//每行显示部门的个数
	var displayDeptCount =  parseInt(usefullWidth / 88);	
	
	of.deptPagingLimit = of.depts.root.length > displayDeptCount ? displayDeptCount-1 : displayDeptCount;
	
	var limit = of.depts.root.length >= of.deptPagingStart + of.deptPagingLimit ? of.deptPagingLimit : of.deptPagingLimit - (of.deptPagingStart + of.deptPagingLimit - of.depts.root.length);
	
	
	if(of.depts.root.length > 0){
		for (var i = 0; i < limit; i++) {
			var dName = of.depts.root[of.deptPagingStart + i].name;
			html.push(deptCmpTemplet.format({
				id : of.depts.root[of.deptPagingStart + i].id,
				name : dName.length > 4? dName.substring(0, 4) : dName
			}));
		}
	}	
	//显示部门分页按钮
	if(of.depts.root.length > displayDeptCount){
		html.push('<a href="javascript:of.deptGetPreviousPage()" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">L</a>' +
				'<a href="javascript:of.deptGetNextPage()" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">R</a>');
	}	
	$("#deptsCmp").html(html.join("")).trigger('create').trigger('refresh');	
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
	
	//显示厨房分页
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
					currPrice : (c.data.status & 1 << 4) != 0 ? '时' : '',		
					gift : (c.data.status & 1 << 3) != 0 ? '赠' : ''	,
					weigh : (c.data.status & 1 << 7) != 0 ? '称' : ''					
				});
			},
			pagedCallBack : function(){
				//FIXME .food-status-font中position:absolute不起作用
				setTimeout(function(){
					$(".food-status-font").css("position", "absolute");
				}, 250);				
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
 * 厨房分页
 */
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

/**
 * 显示厨房分页
 */
of.showKitchenPaging = function(){
	var kc = $("#kitchensCmp");
	var html = ['<a onclick="of.findFoodByKitchen({event:this, kitchenId:-1})" data-role="button" data-inline="true" data-type="kitchenCmp" data-value=-1 class="deptKitBtnFont">全部厨房</a>'];
	
	//真实宽度
	var usefullWidth = document.body.clientWidth - 220;
	//每行显示厨房的个数
	var displayKitchenCount =  parseInt(usefullWidth / 88);
	
	of.kitchenPagingLimit = of.kitchenPagingData.length > displayKitchenCount ? displayKitchenCount-1 : displayKitchenCount;
	
	var limit = of.kitchenPagingData.length >= of.kitchenPagingStart + of.kitchenPagingLimit ? of.kitchenPagingLimit : of.kitchenPagingLimit - (of.kitchenPagingStart + of.kitchenPagingLimit -of.kitchenPagingData.length);
	
	if(of.kitchenPagingData.length > 0){
		for (var i = 0; i < limit ; i++) {
			var kName = of.kitchenPagingData[of.kitchenPagingStart + i].name;
			html.push(kitchenCmpTemplet.format({
				id : of.kitchenPagingData[of.kitchenPagingStart + i].id,
				name : kName.length > 4? kName.substring(0, 4) : kName
			}));
		}
	}
	
	//显示分页按钮
	if(of.kitchenPagingData.length > displayKitchenCount){
		html.push('<a href="javascript:of.kitchenGetPreviousPage()" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">L</a>' +
				'<a href="javascript:of.kitchenGetNextPage()" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">R</a>');
	}	
	kc.html(html.join("")).trigger('create').trigger('refresh');

};


/**
 * 但div还没设置完成高度时不断刷新
 */
function keepLoadFoodData(){
	if(!$('#foodsCmp').html()){
		Util.LM.show();
		$('#foodsCmp').html('加载菜品中...')
	}else{
		of.initKitchenContent({deptId:-1});
		clearInterval(of.loadFoodDateAction);
		Util.LM.hide();
	}
}



/**
 * 加载点菜页面数据
 */
of.show = function(c){
	
	of.table = c.table;
	of.order = typeof c.order != 'undefined' ? c.order : null;
	of.afterCommitCallback = typeof c.callback == 'function' ? c.callback : null;
	//清空选中的全单口味
	of.ot.allBillTaste && delete of.ot.allBillTaste;
	//更新沽清
	of.updataSelloutFoods();
	//加载菜品数据
	of.toOrderFoodPage(of.table);	
};
/**
 * 展示菜品数据
 */
of.toOrderFoodPage = function(table){
	//去点餐界面
	location.href = '#orderFoodMgr';

	$('#divNFCOTableBasicMsg').html(table.alias + '<br>' + table.name);
	
	of.table = table;
	of.newFood = [];
	
	//渲染数据
	of.initDeptContent();
	
	//第一次加载不成功, 继续加载直到显示
	var index = 0;
	of.loadFoodDateAction = window.setInterval(function(){
		if($('#foodsCmp').find("a").length > 0){
			clearInterval(of.loadFoodDateAction);
			if(index == 0){
				of.initKitchenContent({deptId:-1});
			}
			Util.LM.hide();
		}else{
			index ++;
			Util.LM.show();
			of.initKitchenContent({deptId:-1});
		}
	}, 400);
	
	of.initNewFoodContent();
}
/**
 * 每次入点菜界面时更新沽清菜品
 */
of.updataSelloutFoods = function(){
	Util.LM.show();
	$.ajax({
		url : '../QueryMenu.do',
		type : 'post',
		async:false,
		dataType : 'json',
		data : {
			dataSource : 'stop'
		},
		success : function(result, status, xhr){
			if(result.success){
				var stopFoods = result.root;
				for (var j = 0; j < of.foodList.length; j++) {
					of.foodList[j].status &= ~(1 << 2);
					for (var i = 0; i < stopFoods.length; i++) {
						if(of.foodList[j].id == stopFoods[i].id){
							of.foodList[j].status |= (1 << 2);
							break;
						}
					}
				}
				Util.LM.hide();
			}else{
				Util.LM.hide();
			}
		},
		error : function(request, status, err){
			Util.LM.hide();
			Util.msg.alert({
				renderTo : 'orderFoodMgr',
				msg : '更新出错, 请联系客服'
			});
		}
	}); 
}


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
	var foodData = null;
	//如果菜品是时价属性
	if(!c.foodUnitPriceInputed){
		for(var i = 0; i < of.foodList.length; i++){
			if(of.foodList[i].id == c.foodId){
				//返回连接空数组的副本
				foodData = Util.clone(of.foodList[i]);
				break;
			}
		}
		if(foodData == null){
		 	Util.msg.alert({
				msg : '添加菜品失败, 程序异常, 请刷新后重试或联系客服人员',
				renderTo : 'orderFoodMgr'
			});
			return;
		}else{
			if((foodData.status & 1 << 2) != 0){
				Util.msg.tip('此菜品已停售!'); 
				return;
			}
		}
		
		if((foodData.status & 1 << 4) != 0){
			c.foodData = foodData;
			of.openFoodUnitPriceWin(c);
			return;
		}
	}else{
		foodData = c.foodData;
	}

	
	//获取菜品常用口味
	$.post('../QueryFoodTaste.do', {foodID:c.foodId}, function(jr){
		if(jr.success){
			of.commonTastes = jr.root;
			//获取菜品多单位
			$.post('../QueryMenu.do', {dataSource:'getMultiPrices',foodId:c.foodId}, function(result){
				if(result.success){
					of.multiPrices = result.root;
					
					if(of.commonTastes.length == 0 && of.multiPrices.length == 0){
						$('#divFoodTasteFloat').hide();
					}else{
						foodCommonTasteLoad();
					}
					
				}	
				foodData = null;
			});
		}	
	});
	
	//
	var has = false;
	for(var i = 0; i < of.newFood.length; i++){
		//对比是否同一个菜
		if(of.newFood[i].id == foodData.id){
			//再对比口味 & 赠送属性 & 单位 & 时价
			if(of.newFood[i].tasteGroup.normalTasteContent.length == 0 && !of.newFood[i].isGift && !of.newFood[i].foodUnit && (of.newFood[i].status & 1 << 4) == 0){
				has = true;
				of.newFood[i].count++;
				of.selectedOrderFood = of.newFood[i];
				//重新赋值唯一标示
				foodData.unique = of.newFood[i].unique;
				break;				
			}

		}
	}
	if(!has){
		foodData.count = 1;
		foodData.isHangup = false;
		if((foodData.status & 1 << 4) != 0){
			foodData.isCurrPrice = true;
		}
		foodData.tasteGroup = {
			tastePref : '无口味',
			price : 0,
			normalTasteContent : []
		};
		//生成唯一标示
		foodData.unique = new Date().getTime();
		
		of.newFood.push(foodData);
		
		//最新添加的作为选中菜品
		of.selectedOrderFood = of.newFood[of.newFood.length -1];
	}
	
	//
	of.initNewFoodContent({
		data : foodData
	});
//	foodData = null;

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
		//添加唯一标示
		c.record.unique = new Date().getTime();
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
			unique : temp.unique,
			id : temp.id,
			name : temp.name,
			count : temp.count,
			unitPrice : tempUnitPrice.toFixed(2),
			totalPrice : tempUnitPrice.toFixed(2),
			foodStatus : foodStatus,
			isGift : typeof temp.isGift == 'boolean' && temp.isGift ? 'forFree' : 'false',
			multiPriceUnit : temp.foodUnit? "/"+temp.foodUnit.unit:""
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
		$('#divDescForCreateOrde div:first').html('总数量:<font color="green">{count}</font>, 合计:<font color="green">￥{price}</font>'.format({
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
		var select = $('#orderFoodsCmp > li[data-unique='+c.data.unique+']');
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
		Util.msg.tip('请选中一道菜品');
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
						of.selectedOrderFood = null;
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
 *打开菜品数量
 */
of.setFood = function(){
	of.operateFoodCount({
		otype : 'set'
	});

};
/**
 *设置菜品数量
 */
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
				//重新设置数量
				of.selectedOrderFood.count = count;
				of.initNewFoodContent({
					data : of.selectedOrderFood
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
 * 修改时价
 */
of.updateFoodUnitPrice = function(){
	$('#orderFoodOtherOperateCmp').popup('close');	
	if((of.selectedOrderFood.status & 1 << 4) != 0){
		of.saveForFoodUnitPrice.updatePrice = true;
		setTimeout(function(){
			of.openFoodUnitPriceWin({foodData : of.selectedOrderFood});
		}, 250);
	}else{
		Util.msg.tip('此菜品不能设置时价');
	}
}

/**
 * 打开时价
 */
of.openFoodUnitPriceWin = function(c){
	$('#orderFoodUnitPriceSet').popup('open');
	$('#orderFoodUnitPriceSet').parent().addClass("pop").addClass("in");
	
	firstTimeInput = true;
	
	of.openFoodUnitPriceWin.param = c;
	setTimeout(function(){
		$('#inputOrderFoodUnitPriceSet').val(c.foodData.unitPrice);
		$('#inputOrderFoodUnitPriceSet').focus();
		$('#inputOrderFoodUnitPriceSet').select();
	}, 200);

	
};
/**
 * 输入时价
 */
of.saveForFoodUnitPrice = function(c){
	var unitPrice = parseFloat($("#" + focusInput).val());
	if(of.saveForFoodUnitPrice.updatePrice){
		//删除标志位
		delete of.saveForFoodUnitPrice.updatePrice;
		//重新设置时价
		of.selectedOrderFood.unitPrice = unitPrice;
		of.initNewFoodContent({
			data : of.selectedOrderFood
		});		
	}else{
		//设置时价
		of.openFoodUnitPriceWin.param.foodData.unitPrice = unitPrice;
		//标示为已输入时价状态
		of.openFoodUnitPriceWin.param.foodUnitPriceInputed = true;
		
		setTimeout(function(){
			of.insertFood(of.openFoodUnitPriceWin.param);
		}, 250);		
	}


	$('#orderFoodUnitPriceSet').popup('close');
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
			Util.msg.tip('请选中一道菜品');
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
		Util.msg.tip('请选中一道菜品');
		return;
	}
	var data = of.newFood[foodContent.attr('data-index')];
	
	if((data.status & 1 << 3) == 0){
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
 * 修改多单位
 */
of.updateUnitPrice = function(c){
	//关闭更多控件
	$('#orderFoodOtherOperateCmp').popup('close');	
	
	//获取菜品多单位
	$.post('../QueryMenu.do', {dataSource:'getMultiPrices',foodId:of.selectedOrderFood.id}, function(result){
		if(result.success && result.root.length > 0){
			//隐藏常用口味
			$('#collapsibleCommonTaste').hide();
			
			var html = [];
			for (var i = 0; i < result.root.length; i++) {
				html.push(multiPriceCmpTemplet.format({
					index : i,
					id : result.root[i].id,
					click : "of.chooseOrderFoodUnit({event: this, id: "+ result.root[i].id +", unit: '"+ result.root[i].unit +"', price: "+ result.root[i].price +"})",
					multiPrice : '¥' + result.root[i].price + " / " + result.root[i].unit,
					theme : of.selectedOrderFood.foodUnit && of.selectedOrderFood.foodUnit.id == result.root[i].id?"e":"c"
				}));		
			}
			
			$("#divFloatFoodMultiPrices").html(html.join("")).trigger('create');	
			$('#collapsibleMultiPrice').show();
			$('#collapsibleMultiPrice').trigger("expand");
			
			$('#txtChooosedFoodName').text(of.selectedOrderFood.name);
			
			//在搜索时, 口味显示在上方
			if(of.searchFooding){
				$('#divFoodTasteFloat').css({top : '130px', bottom : 'initial'});
			}else{
				$('#divFoodTasteFloat').css({top : 'initial', bottom : '90px'});
			}
			
			$('#divFoodTasteFloat').show();							
		}else{
			Util.msg.tip('此菜品无其他单位');
		}		
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
	
	//存放选中口味
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
		if(of.ot.allBillTaste){
			foodTasteGroup = foodTasteGroup.concat(of.ot.allBillTaste);
			of.ot.tasteGroupClick = true;
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
		},300);		
	}
	

};
/**
 * 口味操作返回
 */
of.ot.back = function(){
	$('#orderFoodTasteCmp').popup('close');
	//清空临时口味id
	of.ot.tasteId = null;
//	of.selectedOrderFood = null;
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
	
	var html = [];
	if(of.tasteGroups.length > 0){
		for (var i = 0; i < limit; i++) {
			html.push(tasteGroupCmpTemplet.format({
				index : i,
				id : of.tasteGroups[of.ot.tasteGroupPagingStart + i].id,
				name : of.tasteGroups[of.ot.tasteGroupPagingStart + i].name,
				theme : of.tasteGroups[of.ot.tasteGroupPagingStart + i].id == -10 && of.ot.allBill ==2 ? "e" : "b"
			}));
		}
	}	
	
	if(of.tasteGroups.length > 7){
		html.push('<a onclick="tasteGroupGetPreviousPage()" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="tasteGroupPage">L</a>' +
				'<a onclick="tasteGroupGetNextPage()" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="tasteGroupPage">R</a>');
	}	
	
	$("#tasteGroupCmp").html(html.join("")).trigger('create');	
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
					of.newFood[i].tasteGroup.tmpTaste = Util.clone(tasteGroup.tmpTaste);
				}
			}
		}
		
		of.initNewFoodContent();
		
	}else if(of.ot.allBill == 2){
		for(var i = 0; i < of.newFood.length; i++){
			//用唯一标示替换id
			if(of.newFood[i].unique == of.selectedOrderFood.unique){
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
	$('#shadowForPopup').show();
	
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

/**
 * 关闭临时口味
 */
function closeTempTaste(){
	$('#addTempTasteCmp').hide();
	$('#shadowForPopup').hide();
	
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
		
		of.searchFooding = true;
		
		YBZ_open(document.getElementById('searchFoodInput'));
		
		$('#normalOperateFoodCmp').hide();
		$('#searchFoodCmp').show();	
		
		setTimeout(function(){
			$('#searchFoodInput').focus();
		}, 250);
		
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
/**
 * 关闭搜索
 */
function closeSearchFood(){
	of.searchFooding = false;	
	
	$('#searchFoodInput').val('');
	
	$('#normalOperateFoodCmp').show();
	$('#searchFoodCmp').hide();
	
	YBZ_win.close();
}

/**
 * 临时菜操作
 */
of.tf = {
	selectedKitchen : null,
	initTempKitchen : function(){
		var html = [];
		for (var i = 0; i < of.tempKitchens.length; i++) {
			html.push('<li class="tempFoodKitchen" onclick="of.tf.tempFoodSelectKitchen({event:this, id:' + of.tempKitchens[i].id +'})"><a>' + of.tempKitchens[i].name +'</a></li>');
		}
		$('#tempFoodKitchensCmp').html(html.join("")).trigger('create');
		$('#tempFoodKitchensCmp').listview('refresh');
	}
}

/**
 * 弹出添加临时菜
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
	$('#shadowForPopup').show();
	
	$('#tempFoodName').focus();
	
}

/**
 * 关闭临时菜
 */
of.tf.closeTempFood = function(){
	$('#addTempFoodCmp').hide();
	$('#shadowForPopup').hide();	
	$('#tempFoodName').val('');
	$('#tempFoodPrice').val('');
	$('#tempFoodCount').val(1);
	
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
 * @param c event:this, id
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

/**
 * 弹出动态常用口味
 */
function foodCommonTasteLoad(){
	
	if(of.commonTastes.length > 0){
		of.ot.choosedTastes = [];
		var html = [];
		for (var i = 0; i < of.commonTastes.length; i++) {
			html.push(tasteCmpTemplet.format({
				index : i,
				id : of.commonTastes[i].taste.id,
				name : of.commonTastes[i].taste.name,
				click : "chooseOrderFoodCommonTaste({event: this, id: "+ of.commonTastes[i].taste.id +"})",
				price : of.commonTastes[i].taste.price,
				theme : "c"
			}));		
		}
		
		html.push('<a onclick="operateOrderFoodTaste({type:2})" data-role="button" data-corners="false" data-inline="true" class="moreTasteCmp" data-theme="b">更多口味</a>' +
				'<a onclick="addTempTaste()" data-role="button" data-corners="false" data-inline="true" class="moreTasteCmp" data-theme="b">手写口味</a>');
		
		$("#divFloatFoodTastes").html(html.join("")).trigger('create');		
		
		$('#collapsibleCommonTaste').show();
		$('#collapsibleCommonTaste').trigger("expand");
	}else{
		$('#collapsibleCommonTaste').hide();
	}
	
	if(of.multiPrices.length > 0){
		var html = [];
		for (var i = 0; i < of.multiPrices.length; i++) {
			html.push(multiPriceCmpTemplet.format({
				index : i,
				id : of.multiPrices[i].id,
				click : "of.chooseOrderFoodUnit({event: this, id: "+ of.multiPrices[i].id +", unit: '"+ of.multiPrices[i].unit +"', price: "+ of.multiPrices[i].price +"})",
				multiPrice : '¥' + of.multiPrices[i].price + " / " + of.multiPrices[i].unit,
				theme : i == 0 ? "e" : "c"
			}));		
		}
		
		$("#divFloatFoodMultiPrices").html(html.join("")).trigger('create');	
		$('#collapsibleMultiPrice').show();
		$('#collapsibleMultiPrice').trigger("expand");
		
		//默认选中一个单位
		for(var i = 0; i < of.newFood.length; i++){
			//用唯一标示替代id
			if(of.newFood[i].unique == of.selectedOrderFood.unique){
				of.newFood[i].foodUnit = of.multiPrices[0];
				of.newFood[i].unitPrice = of.multiPrices[0].price;
				break; 
			}
		}	
		of.initNewFoodContent({
			data : of.selectedOrderFood
		});			
	}else{
		$('#collapsibleMultiPrice').hide();
	}
	

	$('#txtChooosedFoodName').text(of.selectedOrderFood.name);
	
	//在搜索时, 口味显示在上方
	if(of.searchFooding){
		$('#divFoodTasteFloat').css({top : '130px', bottom : 'initial'});
	}else{
		$('#divFoodTasteFloat').css({top : 'initial', bottom : '90px'});
	}
	
	$('#divFoodTasteFloat').show();
}

/**
 * 关闭常用口味
 */
function closeFoodCommonTaste(){
	$('#divFoodTasteFloat').hide();
	$("#divFloatFoodTastes").html('');	
}

/**
 * 常用口味选中
 * @param c event:当前dom, id
 */
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
		//用唯一标示替代id
		if(of.newFood[i].unique == of.selectedOrderFood.unique){
			of.newFood[i].tasteGroup = tasteGroup;
			break; 
		}
	}
	
	of.initNewFoodContent({
		data : of.selectedOrderFood
	});	
	
	tasteGroup = null;
}

/**
 * 选择多单位
 * @param c event:当前dom, id
 */
of.chooseOrderFoodUnit = function(c){
	var currentUnit = $(c.event);
	
	var foodUnit = null;
	
	if(currentUnit.attr('data-theme') == 'e'){
		currentUnit.attr('data-theme', 'c').removeClass('ui-btn-up-e').addClass('ui-btn-up-c');
		foodUnit = null;
	}else{
		$("#divFloatFoodMultiPrices a").attr('data-theme', 'c').removeClass('ui-btn-up-e').addClass('ui-btn-up-c');
		currentUnit.attr('data-theme', 'e').removeClass('ui-btn-up-c').addClass('ui-btn-up-e');
		foodUnit = {
			id : c.id,
			unit : c.unit,
			price : c.price
		};
	}

	$("#divFloatFoodMultiPrices a").buttonMarkup( "refresh" );
	
	
	for(var i = 0; i < of.newFood.length; i++){
		//用唯一标示替代id
		if(of.newFood[i].unique == of.selectedOrderFood.unique){
			of.newFood[i].foodUnit = foodUnit;
			if(foodUnit){
				of.newFood[i].unitPrice = foodUnit.price;
			}else{
				//菜品设回原价
				for (var j = 0; j < of.foodList.length; j++) {
					if(of.newFood[i].id == of.foodList[j].id){
						of.newFood[i].unitPrice = of.foodList[j].unitPrice;
					}
				}
			}
			break; 
		}
	}	
	
	of.initNewFoodContent({
		data : of.selectedOrderFood
	});		
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
 * 下单并且结账
 */
of.orderAndPay = function(){
	//设置无论从哪个界面进入点菜, 下单后都会去结账界面
	of.afterCommitCallback = function(){
		showPaymentMgr({table:of.table});
	};
	of.submit({notPrint : false});	
}

/**
 * 先送
 */
of.orderBefore = function(){
	of.orderBeforeCallback = function(){
		//清空已点菜
		$('#orderFoodsCmp').html('');
		//清空状态栏
		$('#divDescForCreateOrde div:first').html('');
		$('#orderFoodsCmp').listview('refresh');
		//更新餐台
		if(of.order == null){
			initTableData();
		}
		//更新账单
		$.post('../QueryOrderByCalc.do', {tableID : of.table.id}, function(result){
			of.table.statusValue == 1;
			of.order = result.other.order;
			delete of.orderBeforeCallback;
		});
		
	};
	
	$('#orderOtherOperateCmp').popup('close');
	setTimeout(function(){
		of.submit({notPrint : false});	
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
			if (data.success) {
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
//												if(of.afterCommitCallback != null && typeof of.afterCommitCallback == 'function'){
//													of.afterCommitCallback();
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
						
						//从已点菜进入时, 返回已点菜界面
						if(of.afterCommitCallback != null && typeof of.afterCommitCallback == 'function'){
							
							//先送则停留在本页面
							if(of.orderBeforeCallback){
								//标示为先送返回
								uo.fromBack = true;
								of.orderBeforeCallback();
							}else{
								//去除表示
								delete uo.fromBack;
								of.afterCommitCallback();
							}
						}else{//从主界面进入
							//先送则停留在本页面
							if(of.orderBeforeCallback){
								of.orderBeforeCallback();
							}else{
								uo.back();
							}
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

/**
 * 是否需要手写板和数字键盘
 * @param c
 */
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


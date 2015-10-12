var Request = new Util_urlParaQuery();
//1:pos端	2:touch		3:试用 
var systemStatus = Request["status"]?parseInt(Request["status"]):2;

//点菜界面数据对象
var of = {
	table : {},
	order : {},
	ot : {
		tasteGroupPagingStart : 0,
		choosedTastes : [],
		allBill : false
	},
	selectedOrderFood : {},
	commonTastes : [],
	multiPrices : [],
	calculator : {},
	newFood : [],
	//从哪个功能进入点菜
	orderFoodOperateType : 'normal'
},
	//不同条件下选出的口味
	tastesDate = [],

	//口味动态弹出时鼠标范围
	mouseOutFoodSelect = false,	
	/**
	 * 元素模板
	 */
	
	
	//菜品列表
	foodCmpTemplet = '<a data-role="button" data-corners="false" data-inline="true" class="food-style" data-value={id} onclick="{click}">' +
							'<div style="height: 70px;">{name}<br>￥{unitPrice}' +
								'<div class="food-status-font {commonStatus}">' +
									'<font color="orange">{weigh}</font>' +
									'<font color="blue">{currPrice}</font>' +
									'<font color="FireBrick">{sellout}</font>' +
									'<font color="green">{gift}</font>' +
								'</div>'+
								'<div class="food-status-limit {limitStatus}">' +
									'<font color="orange">限: {foodLimitAmount}</font><br>' +
									'<font color="green">剩: {foodLimitRemain}</font>' +
								'</div>'+								
								
							'</div>'+
						  '</a>',
	//已点菜列表					  
	orderFoodCmpTemplet = '	<li data-icon={isGift} data-index={dataIndex} data-unique={unique} data-theme="c" data-value={id} data-type="orderFoodCmp" onclick="of.selectNewFood({event:this, foodId:{id}})" ><a >'+
									'<h1 style="font-size:20px;">{name}</h1>' +
									'<div class="{hasComboFood}"><ul data-role="listview" data-inset="false" class="div4comboFoodList">{comboFoodList}</ul></div><br>' +
									'<span style="color:green;">{tasteDisplay}</span>' +
									'<div>' +
										'<span style="float: left;color: red;">{foodStatus}</span>' +
										'<span style="float: right;color: blue;margin-left:5px;">{multiPriceUnit}</span>' +
										'<span style="float: right;">￥{unitPrice}  X <font color="lime">{count}</font></span>' +
									'</div>' +
								'</a></li>',
	//套菜列表							
	comboFoodLiTemplet = '<li class="ui-li ui-li-static">┕{name}<font color="blue">{unit}</font> X <font color="lime">{amount}</font><font color="green"> {tastes}</font></li>',
	//口味列表
	tasteCmpTemplet = '<a onclick="{click}" data-role="button" data-corners="false" data-inline="true" class="tasteCmp" data-index={index} data-value={id} data-theme={theme}><div>{name}<br>{price}</div></a>',
	//选中口味
	choosedTasteCmpTemplet = '<a onclick="removeTaste({event: this, id: {id}})" data-role="button" data-corners="false" data-inline="true" class="tasteCmp" data-index={index} data-value={id}><div>{name}<br>￥{price}</div></a>',
	//口味组
	tasteGroupCmpTemplet = '<a data-role="button" data-inline="true" class="tastePopTopBtn" data-value={id} data-index={index} data-theme="{theme}" onclick="initTasteCmp({event:this, id:{id}})">{name}</a>';
	//套菜组
	comboFoodGroupCmpTemplet = '<a data-role="button" data-inline="true" class="comboFoodPopTopBtn" data-value={id} data-index={index} data-theme="{theme}" onclick="initComboFoodTasteCmp({event:this, id:{id}, isComboFood : {isComboFood}})"><div>{name}</div></a>';
	//多单位
	multiPriceCmpTemplet = '<a onclick="{click}" data-role="button" data-corners="false" data-inline="true" class="multiPriceCmp" data-index={index} data-value={id} data-theme={theme}><div>{multiPrice}</div></a>',

	
	
/**
 * 入口, 加载点菜页面数据
 */
of.entry = function(c){
	//去点餐界面
	location.href = '#orderFoodMgr';
	
	//设置点菜界面操作类型
	of.orderFoodOperateType = c.orderFoodOperateType;
	//清空选中的全单口味
	of.ot.allBillTaste && delete of.ot.allBillTaste;
	
	of.order = null;
	of.table = null;
	if(c.table){
		Util.LM.show();
		$.ajax({
			url : '../QueryTable.do',
			type : 'post',
			data : {
				tableID : !c.table.alias || c.table.alias == 0 ? c.table.id : '', 
				alias : c.table.alias
			},
			success : function(data, status, xhr){
				if(data.success && data.root.length > 0){
					of.table = data.root[0];
					of.table.comment = c.comment;

					//获取沽清菜品的数据
					$.post('../QueryMenu.do', {dataSource : 'stopAndLimit'}, function(result){
						if(result.success){
							updateSellout(result.root);
							//触发entry事件创建部门厨房按钮
							$('#orderFoodMgr').trigger('entry', of.table);
						}
					});
					
					if(of.table.statusValue == 1){
						//餐台是就餐状态下需要获取原有的账单信息
						$.post('../QueryOrderByCalc.do', {tableID : c.table.id}, function(result){
							if(result.success){
								of.order = result.other.order;
							}
						});
					}
				}else{
					Util.msg.alert({
						renderTo : 'orderFoodMgr',
						msg : '没有找到此餐台信息'
					});
				}
				Util.LM.hide();
			},
			error : function(request, status, err){
				Util.LM.hide();
				Util.msg.alert({
					renderTo : 'orderFoodMgr',
					msg : err
				});
			}
		});
	}else{
		//获取沽清菜品的数据
		$.post('../QueryMenu.do', {dataSource : 'stopAndLimit'}, function(result){
			if(result.success){
				updateSellout(result.root);
				//触发entry事件创建部门厨房按钮
				$('#orderFoodMgr').trigger('entry', of.table);
			}
		});
	}
	

	function updateSellout(selloutFoods){
		for (var j = 0; j < of.foodList.length; j++) {
			//先把菜品全部变为不停售的, 因为可能之前是停售的, 现在不停售了
			of.foodList[j].status &= ~(1 << 2);
			for (var i = 0; i < selloutFoods.length; i++) {
				if(of.foodList[j].id == selloutFoods[i].id){
					if(selloutFoods[i].foodLimitRemain == 0){
						of.foodList[j].status |= (1 << 2);
					}
					
					//更新限量沽清剩余
					if((of.foodList[j].status & 1 << 10) != 0 || selloutFoods[i].foodLimitAmount > 0){
						//设置菜品为限量沽清属性
						of.foodList[j].status |= (1 << 10);
						of.foodList[j].foodLimitAmount = selloutFoods[i].foodLimitAmount;
						of.foodList[j].foodLimitRemain = selloutFoods[i].foodLimitRemain;
					}
					break;
				}
			}
		}
	}

};
	

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
			dataSource : 'stopAndLimit'
		},
		success : function(result, status, xhr){
			if(result.success){
				var stopFoods = result.root;
				for (var j = 0; j < of.foodList.length; j++) {
					//先把菜品全部变为不停售的, 因为可能之前是停售的, 现在不停售了
					of.foodList[j].status &= ~(1 << 2);
					for (var i = 0; i < stopFoods.length; i++) {
						if(of.foodList[j].id == stopFoods[i].id){
							if(stopFoods[i].foodLimitRemain == 0){
								of.foodList[j].status |= (1 << 2);
							}
							
							//更新限量沽清剩余
							if((of.foodList[j].status & 1 << 10) != 0 || stopFoods[i].foodLimitAmount > 0){
								//设置菜品为限量沽清属性
								of.foodList[j].status |= (1 << 10);
								of.foodList[j].foodLimitAmount = stopFoods[i].foodLimitAmount;
								of.foodList[j].foodLimitRemain = stopFoods[i].foodLimitRemain;
							}
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
		
		//是否为套菜
		foodData.combo = [];
		if((foodData.status & 1 << 5) != 0){
			//获取对应套菜
			$.ajax({
				url : '../QueryFoodCombination.do',
				type : 'post',
				async:false,
				dataType : 'json',
				data : {
					foodID:foodData.id
				},
				success : function(rt, status, xhr){
					if(rt.success && rt.root.length > 0){
						//组合子菜给套菜
						for (var j = 0; j < rt.root.length; j++) {
							
							foodData.combo.push({
								comboFood : rt.root[j],
								tasteGroup : {
									normalTasteContent : [],
									tastePref : ''
								}
							});
						}
					}
				},
				error : function(request, status, err){
					alert(request.msg);
				}
			}); 
		}		
		
		of.newFood.push(foodData);
		
		//最新添加的作为选中菜品
		of.selectedOrderFood = of.newFood[of.newFood.length -1];
	}
	
	//
	of.initNewFoodContent({
		data : foodData
	});
	
	//获取菜品常用口味
	$.post('../QueryFoodTaste.do', {foodID:c.foodId}, function(jr){
		if(jr.success){
			of.commonTastes = jr.root;
			//获取菜品多单位
			$.post('../QueryMenu.do', {dataSource:'getMultiPrices',foodId:c.foodId}, function(result){
				if(result.success){
					of.multiPrices = result.root;
					
					if((foodData.status & 1 << 5) != 0){
						comboFoodTasteUnitLoad();							
					}else{
						if(of.commonTastes.length == 0 && of.multiPrices.length == 0){
							$('#divFoodTasteFloat').hide();
						}else{
							foodCommonTasteLoad();
						}
					}
				}	
				foodData = null;
			});
		}	
	});	

	if(typeof c.callback == 'function'){
		c.callback();
	}
	
	//判断拼音键盘是否显示来清空
	if($("#orderPinyinCmp").is(":visible")){
		$('#pinyinVal_a_orderFood').click();	
		$('#handDel_a_orderFood').click();	
	}
	
	//判断手写键盘是否显示来重写
	if($("#orderHandCmp").is(":visible")){
		$('#rewrite_a_orderFood').click();
		$('#handDel_a_orderFood').click();
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
		sumCount += temp.count;
		//称重属性是整个菜加口味价钱, 不是每份菜
		if((temp.status & 1 << 7) != 0){
			tempUnitPrice = temp.unitPrice;
			var tasteGroupPrice = typeof temp.tasteGroup.price != 'number' ? 0 :  temp.tasteGroup.price;
			sumPrice += (temp.count * tempUnitPrice) + tasteGroupPrice;
		}else{
			if(typeof temp.tasteGroup.price == 'number'){
				tempUnitPrice = parseFloat(temp.unitPrice + temp.tasteGroup.price);
			}
			if(typeof temp.tasteGroup.tastePrice == 'number'){
				tempUnitPrice = parseFloat(temp.unitPrice + temp.tasteGroup.tastePrice);
			}
			
			sumPrice += temp.count * tempUnitPrice;
		}
		
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
		
		var comboFoodLi = [];
		//是否为套菜
		if((temp.status & 1 << 5) != 0){
			for (var j = 0; j < temp.combo.length; j++) {
				//列出套菜对应的子菜品
				comboFoodLi.push(comboFoodLiTemplet.format({
					name : temp.combo[j].comboFood.name,
					//有单位时使用单位名
					unit : temp.combo[j].foodUnit ? ' /' + temp.combo[j].foodUnit.unit : '',
					amount : temp.combo[j].comboFood.amount,
					tastes : temp.combo[j].tasteGroup.tastePref ? ('—' + temp.combo[j].tasteGroup.tastePref) : ''
				}));
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
			multiPriceUnit : temp.foodUnit? "/"+temp.foodUnit.unit:"",
			hasComboFood : comboFoodLi.length == 0 ? 'none' : '',
			comboFoodList : comboFoodLi.join("")
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
		$('#orderFoodsCmp').listview("refresh");
	}else{
		
		$('#divOrderFoodsCmp').animate({scrollTop: 0}, 'fast');
	}
		
	
};

/**
 * 打开分席
 */
of.openSplitOrderWin = function(c){
	$('#splitOrderCount').val("");
	$('#orderFoodOtherOperateCmp').popup('close');	
	
	
	setTimeout(function(){
		$('#splitOrderWin').popup('open');
		$('#splitOrderWin').parent().addClass("pop").addClass("in");
		
		setTimeout(function(){
			$('#splitOrderCount').focus();
		}, 200);
	}, 250);

};

/**
 * 账单分席上
 */
of.saveForSplitOrder = function(){
	var count = $('#splitOrderCount').val();
	var temp;
	for(var i = 0; i < of.newFood.length; i++){
		temp = of.newFood[i];
		//转换为int
		temp.count =  parseInt(count) * temp.count;
		if(!temp.tasteGroup.normalTaste){
			temp.tasteGroup.normalTaste = {
				name : '',
				price : 0
			};
		}
		
		if(typeof temp.tasteGroup.tmpTaste != 'undefined'){
			//如果已经是分席上, 则截取前段拼上分席
			var splitName;
			var index = temp.tasteGroup.tmpTaste.name.indexOf("席上");
			if(index > 0){
				splitName = temp.tasteGroup.tmpTaste.name.substring(0, temp.tasteGroup.tmpTaste.name.indexOf("分 "));
				splitName += (splitName?", 分 " + count + " 席上" : "分 " + count + " 席上");
			}else{//如果不是则直接拼上分席
				splitName = temp.tasteGroup.tmpTaste.name + ", 分 " + count + " 席上";
			}
			
			//口味显示分席
			var newstr=temp.tasteGroup.normalTaste.name.replace(temp.tasteGroup.tmpTaste.name,splitName);  
			temp.tasteGroup.tmpTaste.name = splitName;
			temp.tasteGroup.normalTaste.name = newstr;
			temp.tasteGroup.tastePref = temp.tasteGroup.normalTaste.name;			
		}else{
			var tempTasteData = {
				name : "分 " + count + " 席上",
				id : -11,
				cateStatusValue : 2,
				price : 0,
				isTemp : true
			};
			//口味显示分席
			temp.tasteGroup.tmpTaste = tempTasteData;
			temp.tasteGroup.normalTaste.name += (temp.tasteGroup.normalTaste.name ? ',' + tempTasteData.name : tempTasteData.name);
			temp.tasteGroup.tastePref = temp.tasteGroup.normalTaste.name;
		}		
	}
	
	of.initNewFoodContent();
	
	$('#splitOrderWin').popup('close');
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
};

/**
 * 打开时价
 */
of.openFoodUnitPriceWin = function(c){
	$('#orderFoodUnitPriceSet').popup('open');
	$('#orderFoodUnitPriceSet').parent().addClass("pop").addClass("in");
	
	
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
	//去除动作标记
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
	
	//套菜
	if((of.selectedOrderFood.status & 1 << 5) != 0 && !c.comboFoodMoreTaste){
		//获取对应菜品的常用口味并加载
		$.ajax({
			url : '../QueryFoodTaste.do',
			type : 'post',
			data : {foodID:of.selectedOrderFood.id},
			async : false,
			dataType : 'json',
			success : function(rt){
				 of.commonTastes = rt.root; 
				 //获取对应菜品的多单位并加载				
				$.ajax({
					url : '../QueryMenu.do',
					type : 'post',
					data : {dataSource:"getMultiPrices", foodId:of.selectedOrderFood.id},
					async : false,
					dataType : 'json',
					success : function(rt){
						 of.multiPrices = rt.root; 
					},
					error : function(rt){}
				});
			},
			error : function(rt){}
		});
		
		mouseOutFoodSelect = false;
		comboFoodTasteUnitLoad();
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

			//是否为子菜
			if((of.selectedOrderFood.status & 1 << 5) != 0 && chooseOrderFoodCommonTaste.curComboFoodId){
				for (var j = 0; j < of.selectedOrderFood.combo.length; j++) {
					var comboOrderFood = of.selectedOrderFood.combo[j];
					if(chooseOrderFoodCommonTaste.curComboFoodId == comboOrderFood.comboFood.id){
						//获取子菜的tasteGroup
						var tg = comboOrderFood.tasteGroup.normalTasteContent.slice(0);
						
						for (var i = 0; i < tg.length; i++) {
							foodTasteGroup.push({taste:tg[i]});
						}
						
						//临时口味
						if(typeof comboOrderFood.tasteGroup.tmpTaste != 'undefined'){
							foodTasteGroup.push({taste : comboOrderFood.tasteGroup.tmpTaste});
						}
						
					}
				}
				
			}else{
				//获取主菜或非套菜的tasteGroup
				var tg = of.selectedOrderFood.tasteGroup.normalTasteContent.slice(0);
				
				for (var i = 0; i < tg.length; i++) {
					foodTasteGroup.push({taste:tg[i]});
				}
				
				//临时口味
				if(typeof of.selectedOrderFood.tasteGroup.tmpTaste != 'undefined'){
					foodTasteGroup.push({taste : of.selectedOrderFood.tasteGroup.tmpTaste});
				}				
			}
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
//		$('#orderFoodTasteCmp').parent().addClass("pop").addClass("in");		
	}else{
		//先关闭更多再打开全单口味
		setTimeout(function(){
			$('#orderFoodTasteCmp').popup('open');
			//$('#orderFoodTasteCmp').parent().addClass("pop").addClass("in");
		},500);		
	}
	

}
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
		of.tasteGroups.forEach(function(e){
			if(e.id == tGroup.data("value")){
				tastesDate = e.items;
			}
		});
		
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



function comboFoodTasteUnitLoad(){
	of.ot.comboFoodPagingStart = 0;
	
	of.comboFoodGroups = [];
	
	of.comboFoodGroups.push({
		id : of.selectedOrderFood.id,
		name : of.selectedOrderFood.name,
		status : of.selectedOrderFood.status,
		isComboFood : false
	});
	
	for (var i = 0; i < of.selectedOrderFood.combo.length; i++) {
		of.selectedOrderFood.combo[i].comboFood.isComboFood = true;
		of.comboFoodGroups.push(of.selectedOrderFood.combo[i].comboFood);
	}
	
	initComboFoodGroupCmp();
	
	$('#divComboFoodFloat').show();
	
	//关闭可能的动态口味
	closeFoodCommonTaste();
	
}

/**
 * 关闭套菜口味单位
 */
function closeComboFoodTasteUnit(){
	$('#divComboFoodFloat').hide();
	$("#divComboFoodTastes").html('');	
	$("#divComboFoodMultiPrices").html('');
}

/**
 * 初始化套菜组
 */
function initComboFoodGroupCmp(){
	
	of.ot.comboFoodGroupPagingLimit = of.comboFoodGroups.length > 7 ? 6 : 7;
	
	var limit = of.comboFoodGroups.length >= of.ot.comboFoodPagingStart + of.ot.comboFoodGroupPagingLimit ? of.ot.comboFoodGroupPagingLimit : of.ot.comboFoodGroupPagingLimit - (of.ot.comboFoodPagingStart + of.ot.comboFoodGroupPagingLimit - of.comboFoodGroups.length);
	
	var html = [];
	if(of.comboFoodGroups.length > 0){
		for (var i = 0; i < limit; i++) {
			var theme = "b";
			if((of.comboFoodGroups[of.ot.comboFoodPagingStart + i].status & 1 << 5) != 0){
				theme = "e";
			}
			
			html.push(comboFoodGroupCmpTemplet.format({
				index : i,
				id : of.comboFoodGroups[of.ot.comboFoodPagingStart + i].id,
				name : of.comboFoodGroups[of.ot.comboFoodPagingStart + i].name,
				isComboFood : of.comboFoodGroups[of.ot.comboFoodPagingStart + i].isComboFood,
				theme : theme
			}));
		}
		
	}	
	
	if(of.comboFoodGroups.length > 7){
		html.push('<a onclick="comboFoodGroupGetPreviousPage()" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="tasteGroupPage">L</a>' +
				'<a onclick="comboFoodGroupGetNextPage()" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="tasteGroupPage">R</a>');
	}	
	
	$("#comboFoodsGroupCmp").html(html.join("")).trigger('create');	
	
	//更新口味和单位的状态
	//菜品下单时已获取常用口味和单位
	initComboFoodTasteCmp();
	
/*	
	//获取对应菜品的常用口味并加载
	 $.post('../QueryFoodTaste.do', {foodID:mainFoodId}, function(result){
		 of.commonTastes = result.root; 
		 //获取对应菜品的多单位并加载
		 $.post('../QueryMenu.do', {dataSource:"getMultiPrices", foodId:mainFoodId}, function(result){
			 of.multiPrices = result.root; 
			//口味
			initComboFoodCommentTaste();
			//多单位
			initComboFoodMultiPrice();
		 }, 'json');	
	 }, 'json');
	 */
}
/**
 * 套菜口味选中
 * @param c
 */
function initComboFoodTasteCmp(c){
	//动态改变口味数据
	if(c && c.event){//切换子菜组时
		var tGroup = $(c.event);
		var glist = $('#comboFoodsGroupCmp a');
		
		 
		if(c.isComboFood){
			chooseOrderFoodCommonTaste.curComboFoodId = c.id;
			//切换子菜时更新已选口味
			for (var j = 0; j < of.selectedOrderFood.combo.length; j++) {
				var comboFood = of.selectedOrderFood.combo[j];
				if(c.id == comboFood.comboFood.id){
					of.ot.choosedTastes.length = 0;
					for (var k = 0; k < comboFood.tasteGroup.normalTasteContent.length; k++) {
						of.ot.choosedTastes.push({
							taste : comboFood.tasteGroup.normalTasteContent[k]
						});
					}
				}
			}
			
		}else{
			//去除子菜标识
			delete chooseOrderFoodCommonTaste.curComboFoodId;
			//切换子菜时更新已选口味
			of.ot.choosedTastes.length = 0;
			for (var k = 0; k < of.selectedOrderFood.tasteGroup.normalTasteContent.length; k++) {
				of.ot.choosedTastes.push({
					taste : of.selectedOrderFood.tasteGroup.normalTasteContent[k]
				});
			}
		}		
		
		//获取对应菜品的常用口味并加载
		 $.post('../QueryFoodTaste.do', {foodID:c.id}, function(result){
			 of.commonTastes = result.root; 
			 initComboFoodCommentTaste();
		 }, 'json');
		 
		 //获取对应菜品的多单位并加载
		 $.post('../QueryMenu.do', {dataSource:"getMultiPrices", foodId:c.id}, function(result){
			 of.multiPrices = result.root; 
			 initComboFoodMultiPrice();
		 }, 'json');
		
		//刷新样式
		glist.attr('data-theme', 'b');
		tGroup.attr('data-theme', 'e').removeClass('ui-btn-up-b').addClass('ui-btn-up-e');
		glist.buttonMarkup("refresh");

	}else{//选中已点菜的套菜时
		
		//去除子菜标识
		delete chooseOrderFoodCommonTaste.curComboFoodId;
		//选中时更新已选口味
		of.ot.choosedTastes.length = 0;
		for (var k = 0; k < of.selectedOrderFood.tasteGroup.normalTasteContent.length; k++) {
			of.ot.choosedTastes.push({
				taste : of.selectedOrderFood.tasteGroup.normalTasteContent[k]
			});
		}
		
		for (var j = 0; j < of.selectedOrderFood.combo.length; j++) {
			var comboFood = of.selectedOrderFood.combo[j];
			 
			 $.ajax({
				 url : '../QueryMenu.do',
				 type : 'post',
				 dataType : 'json',
				 data : {
					 dataSource:"getMultiPrices", foodId:comboFood.comboFood.id
				 },
				 async : false,
				 success : function(result){
					 if(result.root.length > 0){
						 comboFood.foodUnit = result.root[0]; 
					 }					 
				 },
				 error : function(){}
			 });
		}
		
		of.initNewFoodContent({
			data : of.selectedOrderFood
		});	
		
		initComboFoodCommentTaste();
		
		initComboFoodMultiPrice();
	}
}


/**
 * 加载套菜口味
 */
function initComboFoodCommentTaste(){
	if(of.commonTastes.length > 0){
//		of.ot.choosedTastes = [];
		var html = [];
		for (var i = 0; i < of.commonTastes.length; i++) {
			var theme = "c";
			for (var j = 0; j < of.ot.choosedTastes.length; j++) {
				if(of.commonTastes[i].taste.id == of.ot.choosedTastes[j].taste.id){
					theme = "e";
				}
			}
			
			html.push(tasteCmpTemplet.format({
				index : i,
				id : of.commonTastes[i].taste.id,
				name : of.commonTastes[i].taste.name,
				click : "chooseOrderFoodCommonTaste({event: this, id: "+ of.commonTastes[i].taste.id +"})",
				price : of.commonTastes[i].taste.calcValue == 1?(of.commonTastes[i].taste.rate * 100) + '%' : ('￥'+ of.commonTastes[i].taste.price),
				theme : theme
			}));		
		}
		
		var comboTasteId = new Date().getTime() + '_combo';
		html.push('<a onclick="operateOrderFoodTaste({type:2, comboFoodMoreTaste:true})" data-role="button" data-corners="false" data-inline="true" class="moreTasteCmp" data-theme="b">更多口味</a>' +
				'<a id="'+ comboTasteId +'" data-role="button" data-corners="false" data-inline="true" class="moreTasteCmp" data-theme="b">手写口味</a>');
		
		$("#divComboFoodTastes").html(html.join("")).trigger('create');		

		//打开套菜子菜手写口味
		 $('#' + comboTasteId).click(function(){
		 	$('#addTaste_a_orderFood').click();
		 });
		
		$('#collapsibleComboFoodTaste').show();
		$('#collapsibleComboFoodTaste').trigger("expand");
	}else{
		var html = [];
		html.push('<a onclick="operateOrderFoodTaste({type:2, comboFoodMoreTaste:true})" data-role="button" data-corners="false" data-inline="true" class="moreTasteCmp" data-theme="b">更多口味</a>' +
				'<a id="'+ comboTasteId +'" data-role="button" data-corners="false" data-inline="true" class="moreTasteCmp" data-theme="b">手写口味</a>');

		$("#divComboFoodTastes").html(html.join("")).trigger('create');		
		
		//打开套菜手写口味
		 $('#' + comboTasteId).click(function(){
		 	$('#addTaste_a_orderFood').click();
		 });
		
		$('#collapsibleComboFoodTaste').show();
		$('#collapsibleComboFoodTaste').trigger("expand");		
	}		
}


/**
 * 加载套菜多单位
 */
function initComboFoodMultiPrice(){
	if(of.multiPrices.length > 0){
		//存储第几个单位用于显示
		var id4Choosed;
		//没有单位则默认选中第一个单位
		for(var i = 0; i < of.newFood.length; i++){
			//用唯一标示替代id
			if(of.newFood[i].unique == of.selectedOrderFood.unique){
				//是否为子菜
				if(chooseOrderFoodCommonTaste.curComboFoodId){
					for (var j = 0; j < of.newFood[i].combo.length; j++) {
						var comboFood = of.newFood[i].combo[j].comboFood;
						//若有单位
						id4Choosed = of.newFood[i].combo[j].foodUnit ? of.newFood[i].combo[j].foodUnit.id : 0;
						
						if(comboFood.id == chooseOrderFoodCommonTaste.curComboFoodId && !of.newFood[i].combo[j].foodUnit){
							of.newFood[i].combo[j].foodUnit = of.multiPrices[0];
							
							id4Choosed = of.multiPrices[0].id;
							break; 
						}
					}
				}else if(!of.newFood[i].foodUnit){
					of.newFood[i].foodUnit = of.multiPrices[0];
					of.newFood[i].unitPrice = of.multiPrices[0].price;
					
					id4Choosed = of.multiPrices[0].id;
				}else{
					//有单位
					id4Choosed = of.newFood[i].foodUnit.id;
					break; 
				}
				
			}
		}	
		
		var html = [];
		for (var i = 0; i < of.multiPrices.length; i++) {
			html.push(multiPriceCmpTemplet.format({
				index : i,
				id : of.multiPrices[i].id,
				click : "of.chooseOrderFoodUnit({event: this, id: "+ of.multiPrices[i].id +", unit: '"+ of.multiPrices[i].unit +"', price: "+ of.multiPrices[i].price +"})",
				multiPrice : '¥' + of.multiPrices[i].price + " / " + of.multiPrices[i].unit,
				theme : of.multiPrices[i].id == id4Choosed ? "e" : "c"
			}));		
		}
		
		$("#divComboFoodMultiPrices").html(html.join("")).trigger('create');	
		$('#collapsibleComboFoodMultiPrice').show();
		$('#collapsibleComboFoodMultiPrice').trigger("expand");
		
		
		of.initNewFoodContent({
			data : of.selectedOrderFood
		});				
		
	}else{
		$("#divComboFoodMultiPrices").html("");	
		$('#collapsibleComboFoodMultiPrice').hide();
	}		
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
}

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
	var tasteGroup;
	//是否为子菜
	if((of.selectedOrderFood.status & 1 << 5) != 0 && chooseOrderFoodCommonTaste.curComboFoodId){
		for (var j = 0; j < of.selectedOrderFood.combo.length; j++) {
			var comboOrderFood = of.selectedOrderFood.combo[j];
			if(chooseOrderFoodCommonTaste.curComboFoodId == comboOrderFood.comboFood.id){
				//获取子菜的tasteGroup
				tasteGroup = comboOrderFood.tasteGroup;
				break;
			}
		}
	}else{
		//获取主菜的tasteGroup
		tasteGroup = of.selectedOrderFood.tasteGroup;
	}
	
	
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
			//用唯一标示替代id
			if(of.newFood[i].unique == of.selectedOrderFood.unique){
				//是否为子菜
				if((of.newFood[i].status & 1 << 5) != 0  && chooseOrderFoodCommonTaste.curComboFoodId){
					for (var j = 0; j < of.newFood[i].combo.length; j++) {
						var comboOrderFood = of.newFood[i].combo[j];
						if(chooseOrderFoodCommonTaste.curComboFoodId == comboOrderFood.comboFood.id){
							//获取子菜的tasteGroup
							comboOrderFood.tasteGroup = tasteGroup;
						}
					}				
				}else{
					of.newFood[i].tasteGroup = tasteGroup;
				}
				break; 
			}
		}		
		
/*		for(var i = 0; i < of.newFood.length; i++){
			//用唯一标示替换id
			if(of.newFood[i].unique == of.selectedOrderFood.unique){
				of.newFood[i].tasteGroup = tasteGroup;
				break; 
			}
		}*/
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
//				price : of.commonTastes[i].taste.price,
				price : of.commonTastes[i].taste.calcValue == 1?(of.commonTastes[i].taste.rate * 100) + '%' : ('￥'+ of.commonTastes[i].taste.price),
				theme : "c"
			}));		
		}
		
		var CommonTasteId = new Date().getTime();
		html.push('<a onclick="operateOrderFoodTaste({type:2})" data-role="button" data-corners="false" data-inline="true" class="moreTasteCmp" data-theme="b">更多口味</a>' +
				'<a id="' + CommonTasteId + '" data-role="button" data-corners="false" data-inline="true" class="moreTasteCmp" data-theme="b">手写口味</a>');
		
		$("#divFloatFoodTastes").html(html.join("")).trigger('create');		
		
		//动态口味打开手写口味
		 $('#' + CommonTasteId).click(function(){
		 	$('#addTaste_a_orderFood').click();
		 });
		
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
	
	 if($("#orderPinyinCmp").is(":hidden") && $("#orderHandCmp").is(":hidden")){
		 $('#divFoodTasteFloat').css({top : 'initial', bottom : '90px'});
	 }else{
		 $('#divFoodTasteFloat').css({top : 'initial', bottom : '48.5%'});
	 }
 	
	$('#divFoodTasteFloat').show();
	//关闭可能的套菜弹出
	closeComboFoodTasteUnit();
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
	
	//设置选中口味组
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
	$("#divComboFoodTastes a").buttonMarkup( "refresh" );
	
	var combo = [
	     		{
	     			comboFood:{
	     				id : 59287,
	     				name : '菜2',
	     				amount : 1
	     			},	
	     			tasteGroup : {
	     				tastePref: "加快,少辣",
	     				tastePrice: 0,
	     				normalTaste : {
	     					name : '打包, 加快'
	     				},
	     				normalTasteContent : [{
	     					alias: 1169,
	     					calcText: "按价格",
	     					calcValue: 0,
	     					cateStatusText: "口味",
	     					cateStatusValue: 2,
	     					cateText: "口味",
	     					cateValue: 156,
	     					id: 1169,
	     					name: "加快",
	     					price: 0,
	     					rank: 0,
	     					rate: 0,
	     					rid: 40,
	     					typeText: "一般",
	     					typeValue: 0					
	     				}]
	     			}
	     		},
	     		{
	     			comboFood:{
	     				id : 59287,
	     				name : '菜2',
	     				amount : 1
	     			},	
	     			tasteGroup : {
	     				normalTaste : {
	     					name : '加辣, 中牌'
	     				},
	     				normalTasteContent :[],
	     				tastePref : ""
	     			}
	     		}];
	
	
	var tasteGroup;
	//是否为子菜
	if((of.selectedOrderFood.status & 1 << 5) != 0 && chooseOrderFoodCommonTaste.curComboFoodId){
		for (var j = 0; j < of.selectedOrderFood.combo.length; j++) {
			var comboOrderFood = of.selectedOrderFood.combo[j];
			if(chooseOrderFoodCommonTaste.curComboFoodId == comboOrderFood.comboFood.id){
				//获取子菜的tasteGroup
				tasteGroup = comboOrderFood.tasteGroup;
			}
		}
	}else{
		//获取主菜的tasteGroup
		tasteGroup = of.selectedOrderFood.tasteGroup;
	}
	
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
	
	//更新菜品现有口味信息
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
			//是否为子菜
			if((of.newFood[i].status & 1 << 5) != 0  && chooseOrderFoodCommonTaste.curComboFoodId){
				for (var j = 0; j < of.newFood[i].combo.length; j++) {
					var comboOrderFood = of.newFood[i].combo[j];
					if(chooseOrderFoodCommonTaste.curComboFoodId == comboOrderFood.comboFood.id){
						//获取子菜的tasteGroup
						comboOrderFood.tasteGroup = tasteGroup;
					}
				}				
			}else{
				of.newFood[i].tasteGroup = tasteGroup;
			}
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
	
	if(currentUnit.attr('data-theme') != 'e'){
		
		$("#divFloatFoodMultiPrices a").attr('data-theme', 'c').removeClass('ui-btn-up-e').addClass('ui-btn-up-c');
		$("#divComboFoodMultiPrices a").attr('data-theme', 'c').removeClass('ui-btn-up-e').addClass('ui-btn-up-c');
		
		currentUnit.attr('data-theme', 'e').removeClass('ui-btn-up-c').addClass('ui-btn-up-e');
		var foodUnit = {
			id : c.id,
			unit : c.unit,
			price : c.price
		};
		$("#divFloatFoodMultiPrices a").buttonMarkup( "refresh" );
		
		
		for(var i = 0; i < of.newFood.length; i++){
			//用唯一标示替代id
			if(of.newFood[i].unique == of.selectedOrderFood.unique){
				//是否为子菜
				if(chooseOrderFoodCommonTaste.curComboFoodId){
					for (var j = 0; j < of.newFood[i].combo.length; j++) {
						var comboFood = of.newFood[i].combo[j].comboFood;
						if(comboFood.id == chooseOrderFoodCommonTaste.curComboFoodId){
							of.newFood[i].combo[j].foodUnit = foodUnit;
						}
					}
				}else{
					of.newFood[i].foodUnit = foodUnit;
					of.newFood[i].unitPrice = foodUnit.price;
				}
				break; 
			}
		}	
		
		of.initNewFoodContent({
			data : of.selectedOrderFood
		});		
		
	}
};



function scrolldown(c){
	if(!c){
		c = {};
	}
	var dom = document.getElementById('divOrderFoodsCmp');
	if(dom.scrollHeight - dom.scrollTop < 50){
		dom.scrollTop = dom.scrollHeight;
	}else{
		$('#divOrderFoodsCmp').animate({scrollTop: dom.scrollTop + (typeof c.size == 'number' ? c.size : 50 * 3)}, 'fast');
	}	
}

/**
 * 打开助记码
 */
of.openAliasOrderFood = function(){
	$('#txtFoodAlias').focus();
	$('#txtFoodAlias').val("");
	
	$('#orderFoodByAliasCmp').popup('open');
	
	closePinyin();
	closeHandWriting();
};




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
	
	var foodData = [], type = true;
	if(c.force != undefined && c.force){
		//force insert
		type = 23;
		foodData = of.newFood.slice(0);
	}else if(of.table.statusValue == 1){
		//update order
		type = 7;
		foodData = of.newFood.slice(0).concat(of.order.orderFoods.slice(0));
	}else{
		//insert order
		type = 1;
		foodData = of.newFood.slice(0);
	}
	
	Util.LM.show();
	
	orderDataModel.tableID = of.table.id;
	orderDataModel.customNum = of.table.customNum;
	orderDataModel.comment = of.table.comment;
	orderDataModel.orderFoods = foodData;
	orderDataModel.categoryValue =  of.table.categoryValue;
	if(of.order){
		orderDataModel.id = of.order.id;
		orderDataModel.orderDate = of.order.orderDate;
	}
	
	$.ajax({
		url : '../InsertOrder.do',
		type : 'post',
		data : {
			commitOrderData : JSON.stringify(Wireless.ux.commitOrderData(orderDataModel)),
			type : type,
			notPrint : c.notPrint ? c.notPrint : false
		},
		success : function(data, status, xhr) {
			if (data.success){

				of.newFood = [];
				Util.LM.hide();								
				closePinyin();
				closeHandWriting();
				Util.msg.alert({
					msg : data.msg,
					topTip : true
				});
				
				if(c.postSubmit){
					c.postSubmit();
				}
				
			}else {
				//为了防止有其他popup时，无法弹出‘强制提交’的popup，因此延迟执行
				setTimeout(function(){
					Util.LM.hide();
					Util.msg.alert({
						title : data.title,
						renderTo : 'orderFoodMgr',
						msg : data.msg, 
						buttons : 'YESBACK',
						btnEnter : '继续提交',
						fn : function(btn){
							if(btn == 'yes'){
								of.submit({
									force : true,
									postSubmit : function(){
										uo.entry({table : of.table});
									}
								});
							}
						}
					});
				}, 250);


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
		delcookie('isNeedWriter');
	}
	
	$(c.event).checkboxradio('refresh');
}
function isNeedNumKeyboard(c){
	$('#isKeyboard4Taste').checkboxradio('refresh');
}


//手写关闭
function closeHandWriting(){
	$('#orderHandCmp').hide();
}

//关闭拼音
function closePinyin(){	
	$('#orderPinyinCmp').hide();	
}



$(function(){

	//手写重写按钮的click事件
	$('#rewrite_a_orderFood').click(function(){
		if(handWriting){
			handWriting.rewrite();  
		}
		$('#searchWord_div_orderFood').html('');
		$('#handWritingInput_input_orderFood').focus();
	});
	
	//手写搜索的清空按钮事件
	$('#handDel_a_orderFood').click(function(){
		$('#handWritingInput_input_orderFood').val('');
		$('#handWritingInput_input_orderFood').trigger('input');
	});
	
	//监听手写搜索输入框值变化的input事件
	$("#handWritingInput_input_orderFood").on("input", function(){
		search(this.value, 'handWriting');
	});
	
	//监听拼音搜索输入框值变化的input时间
	$('#pinyinInput_input_orderFood').on("input", function(){
		search(this.value, 'pinyin');
	});
	
	//手写搜索的关闭按钮事件
	$('#handWritingClose_a_orderFood').click(function(){
		//模拟点击事件
		var kitchen = $('#kitchensCmp > a[data-theme=b]');
		if(kitchen.length > 0){
			kitchen.click();
		}else{
			kitchen = $('#kitchensCmp a[data-type=kitchenCmp]:first')[0];
			kitchen.click();
		}
		kitchen = null;		
		closeHandWriting();
	});
	
	var handWriting = null;
	//已点菜界面手写板按钮的click事件
	$('#handWriteBoard_a_orderFood').click(function(){	
		$('#orderHandCmp').show();
		if(handWriting == null){
			handWriting = createHandWriting();
		}
		createHandWriting();
		handWriting.rewrite();
		closePinyin();
		$('#handWritingInput_input_orderFood').val('');
		$('#searchWord_div_orderFood').html('');
		$('#handWritingInput_input_orderFood').focus();
	});
	
	//拼音清空按钮事件
	$('#pinyinVal_a_orderFood').click(function(){
		$('#pinyinInput_input_orderFood').val('');
		$('#pinyinInput_input_orderFood').trigger('input');
	});
	
	//拼音删除按钮事件
	$('#pinyinDel_a_orderFood').click(function(){
		var s = $('#pinyinInput_input_orderFood').val();
		$('#pinyinInput_input_orderFood').val(s.substring(0, s.length - 1));
		$('#pinyinInput_input_orderFood').trigger('input');
	});
	
	
	//拼音关闭按钮
	$('#closePinyin_a_orderFood').click(function(){
		//模拟点击事件
		var kitchen = $('#kitchensCmp > a[data-theme=b]');
		if(kitchen.length > 0){
			kitchen.click();
		}else{
			kitchen = $('#kitchensCmp a[data-type=kitchenCmp]:first')[0];
			kitchen.click();
		}
		kitchen = null;	
		closePinyin();
	});
	
	//监听点菜界面拼音搜索按钮的事件
	$('#pinyinBoard_a_orderFood').click(function(){	
		$('#orderPinyinCmp').show();
		$('#pinyin_div_orderFood').show();	
		$('#pinyinInput_input_orderFood').val('');
		closeHandWriting();
		createPinyinKeyboard();
		$('#pinyinInput_input_orderFood').focus();
	});
	
	//点菜页面的下一页
	$('#getNextPage_a_orderFood').click(function(){foodPaging.getNextPage();});
	
	//点菜页面的上一页
	$('#getPrevious_a_orderFood').click(function(){foodPaging.getPreviousPage();});
	
	//菜品分页
	var foodPaging = null;		
	//拼音 && 手写 搜索
	function search(value, qw){	
		var sortMethod = null;
		var data = null;
		
		function byConsumption(obj1, obj2){
			//菜品搜索结果按点菜数量排序
		    var val1 = obj1.foodCnt;
		    var val2 = obj2.foodCnt;
		    if (val1 < val2) {
		        return 1;
		    } else if (val1 > val2) {
		        return -1;
		    } else {
		        return 0;
		    } 
		}
		
		function byAlias(obj1, obj2){
			if(!isNaN(obj1.alias) && !isNaN(obj2.alias)){
				var alias1 = parseInt(obj1.alias);
				var alias2 = parseInt(obj2.alias);
				if(alias1 < alias2){
					return -1;
				}else if(alias1 > alias2){
					return 1;					
				}else{
					return 0;
				}
			}else if(isNaN(obj1.alias) && isNaN(obj2.alias)){
				var id1 = parseInt(obj1.id);
				var id2 = parseInt(obj2.id);
				if(id1 < id2){
					return -1;
				}else if(id1 > id2){
					return 1;					
				}else{
					return 0;
				}
			}else if(isNaN(obj1.alias) && !isNaN(obj2.alias)){
				return 1;
			}else if(!isNaN(obj1.alias) && isNaN(obj2.alias)){
				return -1;
			}else{
				return 0;
			}
		}
		
		if(qw == 'pinyin' && value.trim().length > 0){
			data = [];
			for(var i = 0; i < of.foodList.length; i++){
				if(of.foodList[i].pinyin.indexOf(value.trim().toLowerCase()) != -1){
					data.push(of.foodList[i]);
				}
			}	
			sortMethod = byConsumption;
			
		}else if(qw == 'handWriting' && value.trim().length > 0){
			data = [];
			for(var i = 0; i < of.foodList.length; i++){
				if(of.foodList[i].name.indexOf(value.trim()) != -1){
					data.push(of.foodList[i]);
				}
			}		
			sortMethod = byConsumption;
							
		}else if(qw == 'byDept'){
			if(value.kitchenId != undefined && value.kitchenId != -1){
				data = of.foodList.filter(function(item, pos, self){
					return item.kitchen.id == value.kitchenId;
				});
			}
			if(value.deptId != undefined && value.deptId != -1){
				if(data){
					data = data.filter(function(item, pos, self){
						return item.kitchen.dept.id == value.deptId;
					});
				}else{
					data = of.foodList.filter(function(item, pos, self){
						return item.kitchen.dept.id == value.deptId;
					});
				}
			}
			if(data == null){
				data = of.foodList.slice(0);				
			}
			sortMethod = byAlias;
		}
			
		//创建菜品分页的控件
		if(foodPaging == null){
			foodPaging = Util.to.padding({
				renderTo : "foodsCmp",
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
						weigh : (c.data.status & 1 << 7) != 0 ? '称' : '',
						commonStatus : (c.data.status & 1 << 10) != 0 ? 'none' : '',
						limitStatus : (c.data.status & 1 << 10) != 0 ? '' : 'none',
						foodLimitAmount : c.data.foodLimitAmount,
						foodLimitRemain : c.data.foodLimitRemain
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
		
		foodPaging.init({
			data : data ? data.sort(sortMethod) : of.foodList,
			callback : function(){
				foodPaging.getFirstPage();
			} 
		});
	}

	//手写板的创建
	function createHandWriting(){
		return new HandWritingPanel(
				{ renderTo : document.getElementById('handWritingPanel_th_orderFood'),
				  result : function(data){
				  	var temp = data.slice(0, 6);			
					var zifu = "";
					for(var i = 0; i < temp.length; i++){								
						var eachCharactar = '<input type="button" style="width:33%;height:65%;font-size:30px;" value="' + temp[i] + '">';							
						if(i % 3 == 0 ){
							zifu += '<br>'; 
						}
						zifu += eachCharactar;										
					}
					document.getElementById('searchWord_div_orderFood').innerHTML = zifu;
					$('#searchWord_div_orderFood').find('input').each(function(index, element){
						element.onclick = function(){
							$('#handWritingInput_input_orderFood').val($('#handWritingInput_input_orderFood').val() + element.value);
							$('#handWritingInput_input_orderFood').trigger('input');
							$('#rewrite_a_orderFood').click();
						};
					});
				   }
				});	
	}
	
	
	//拼音键盘动态生成
	function createPinyinKeyboard(){
		var allKeys = new Array("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W"
							, "X", "Y", "Z");
		var keys = "";
		for(var i = 0; i < allKeys.length; i++){
			var eachKeys = '<input type="button" style="width:11%;height:21%;font-size:20px;" value="' + allKeys[i] + '">';
			if(i % 9 == 0){
				keys += '<br>';
			}
			keys += eachKeys;
		}	
		document.getElementById('pinyin_div_orderFood').innerHTML = keys;
		$('#pinyin_div_orderFood input').each(function(index, element){
			element.onclick = function(){
				$('#pinyinInput_input_orderFood').val($('#pinyinInput_input_orderFood').val() + element.value);
				$('#pinyinInput_input_orderFood').trigger('input');		
			};
		});	
	}
	
	//保存选择分厨的id
	var selectedKitchen = null;	
	//保存临时菜
	$('#saveTemp_a_orderFood').click(function(){
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
			count.val(1);
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
					id : selectedKitchen
				},
				tasteGroup : {
					tastePref : '无口味',
					price : 0,
					normalTasteContent : []
				}
			}
		});	
		
		$('#closeTemp_a_orderFood').click();
	});

	
	 
	 // 关闭临时菜
	 
	$('#closeTemp_a_orderFood').click(function(){
		$('#addTempFoodCmp').hide();
		$('#shadowForPopup').hide();	
		$('#tempFoodName').val('');
		$('#tempFoodPrice').val('');
		$('#tempFoodCount').val(1);
		//关闭键盘
		$('#numberKeyboard').hide();	
		//临时菜文本框detach
		HandWritingAttacher.instance().detach($('#tempFoodName')[0]);
	});

	//弹出临时菜
	$('#addTemp_a_orderFood').click(function(){
		//初始化临时菜分厨
		var html = [];
		for (var i = 0; i < of.tempKitchens.length; i++) {
			html.push('<li class="tempFoodKitchen" id="' + of.tempKitchens[i].id + '"><a>' + of.tempKitchens[i].name +'</a></li>');
		}
			
		$('#tempFoodKitchensCmp').html(html.join("")).trigger('create');
		
		$('#tempFoodKitchensCmp').find('li').each(function(index, element){
			element.onclick = function(){
				selectedKitchen = element.id;
				$('#lab4TempKitchen').text($(element).find('a').text());
				$('#popupTempFoodKitchensCmp').popup('close');			
			};						
		});
		
		$('#tempFoodKitchensCmp').listview('refresh');
		
		//默认选中第一个厨房
		selectedKitchen = of.tempKitchens[0].id;
	    	
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
		
		//临时菜输入框弹出手写板控件
		HandWritingAttacher.instance().attach($('#tempFoodName')[0]);
		
		$('#tempFoodName').focus();
		
		//关闭拼音和手写搜索
		closePinyin();
		closeHandWriting();
		
	});
	
	var updateTempTaste = false;
	//打开手写口味
	$('#addTaste_a_orderFood').click(function(){
		var foodContent = $('#orderFoodsCmp > li[data-theme=e]');
		if(foodContent.length != 1){
			Util.msg.alert({
				msg : '请选中一道菜品',
				topTip : true
			});
			return;
		}
		
		//是否选中键盘
		of.selectedOrderFood = of.newFood[foodContent.attr('data-index')];
		
		$('#addTempTasteCmp').show();
		$('#addTempTasteCmp').css('top', '150px');
		$('#shadowForPopup').show();
		
		//关闭弹出常用口味
		closeFoodCommonTaste();
		
		focusInput = "tempTastePrice";
		var tasteGroup = null;
		
		//是否为套菜子菜
		if((of.selectedOrderFood.status & 1 << 5) != 0 && chooseOrderFoodCommonTaste.curComboFoodId){
			//切换子菜时更新已选口味
			for (var j = 0; j < of.selectedOrderFood.combo.length; j++) {
				var comboFood = of.selectedOrderFood.combo[j];
				if(chooseOrderFoodCommonTaste.curComboFoodId == comboFood.comboFood.id){
					tasteGroup = comboFood.tasteGroup;
					break;
				}
			}
		}else{
			tasteGroup = of.selectedOrderFood.tasteGroup;
		}
		
		if(tasteGroup.tmpTaste){
			$('#tempTasteName').val(tasteGroup.tmpTaste.name);
			$('#tempTastePrice').val(tasteGroup.tmpTaste.price);
			
			updateTempTaste = true;
		}else{
			$('#tempTasteName').val('');
			$('#tempTastePrice').val('');
			
			updateTempTaste= false;
		}	
		//临时口味输入框弹出手写板控件
		HandWritingAttacher.instance().attach($('#tempTasteName')[0]);
		$('#tempTasteName').focus();
	});

	
	//添加临时口味的确定按钮
	$('#saveTempTaste_a_orderFood').click(function(){
		of.ot.allBill = 2;
		var name = $('#tempTasteName').val();
		var price = $('#tempTastePrice').val();
		
		if(price == ''){
			price = 0;
		}
		
		of.ot.choosedTastes.length = 0;
		var tasteGroup = {};
		//是否为套菜子菜
		if((of.selectedOrderFood.status & 1 << 5) != 0 && chooseOrderFoodCommonTaste.curComboFoodId){
			//切换子菜时更新已选口味
			for (var j = 0; j < of.selectedOrderFood.combo.length; j++) {
				var comboFood = of.selectedOrderFood.combo[j];
				if(chooseOrderFoodCommonTaste.curComboFoodId == comboFood.comboFood.id){
					tasteGroup = comboFood.tasteGroup;
					for (var k = 0; k < comboFood.tasteGroup.normalTasteContent.length; k++) {
						of.ot.choosedTastes.push({
							taste : comboFood.tasteGroup.normalTasteContent[k]
						});
					}
					break;
				}
			}
		}else{
			tasteGroup = of.selectedOrderFood.tasteGroup;
			for (var i = 0; i < of.selectedOrderFood.tasteGroup.normalTasteContent.length; i++) {
				of.ot.choosedTastes.push({
					taste : of.selectedOrderFood.tasteGroup.normalTasteContent[i]
				});
			}		
		}
		
		//当临时口味是修改状态时
		if(updateTempTaste){
			if(tasteGroup.tmpTaste){
				//如果名称为空则是删除
				if(name){
					tasteGroup.tmpTaste.name = name;
					tasteGroup.tmpTaste.price = price;
					
					of.ot.choosedTastes.push({taste : tasteGroup.tmpTaste});
				}else{
					delete tasteGroup.tmpTaste;
				}
				of.ot.saveOrderFoodTaste();
			}
		}else{
			if(of.ot.tasteId){
				updateTempTaste = true;
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
		updateTempTaste = false;	
		of.ot.tasteId = null;
		
		$('#closeTempTaste_a_orderFood').click();
	});
	
	//临时口味取消按钮
	$('#closeTempTaste_a_orderFood').click(function(){
		$('#addTempTasteCmp').hide();
		$('#shadowForPopup').hide();
	
		$('#tempTasteName').val('');
		$('#tempTastePrice').val('');	
		$('#numberKeyboard').hide();	
		
	});	
	
	//进入点菜界面的时候渲染数据
	$('#orderFoodMgr').on("entry", function(event){
		if(of.table){
			$('#divNFCOTableBasicMsg').html(of.table.alias + '<br>' + of.table.name);
		}else{
			$('#divNFCOTableBasicMsg').html("1号" + '<br>' + "餐桌");
			document.getElementById("divNFCOTableBasicMsg").style.textAlign="center";
		}
			
		//正常点菜
		if(of.orderFoodOperateType == 'normal'){
			of.newFood = [];
			$('#normalOrderFood_a_orderFood').show();
			$('#btnOrderAndPay').show();
			$('#addBookOrderFood').hide();
			$('#bookSeatOrderFood').hide();
			$('#multiOpenTable').hide();
		}else if(of.orderFoodOperateType == 'bookSeat'){
			$('#bookSeatOrderFood').show();
			$('#addBookOrderFood').hide();
			$('#btnOrderAndPay').hide();
			$('#normalOrderFood_a_orderFood').hide();		
			$('#multiOpenTable').hide();		
		}else if(of.orderFoodOperateType == 'addBook'){
			$('#addBookOrderFood').show();
			$('#bookSeatOrderFood').hide();
			$('#normalOrderFood_a_orderFood').hide();
			$('#btnOrderAndPay').hide();	
			$('#multiOpenTable').hide();
		}else if(of.orderFoodOperateType == 'multiOpenTable'){
			of.newFood = [];
			$('#multiOpenTable').show();
			$('#addBookOrderFood').hide();
			$('#bookSeatOrderFood').hide();
			$('#normalOrderFood_a_orderFood').hide();
			$('#btnOrderAndPay').hide();	
		}else if(of.orderFoodOperateType == 'fast'){
			of.newFood = [];
			$('#normalOrderFood_a_orderFood').hide();
			$('#btnOrderAndPay').hide();
			$('#addBookOrderFood').hide();
			$('#bookSeatOrderFood').hide();
			$('#multiOpenTable').hide();
			//快餐模式的牌子号
			$('#brand_a_orderFood').show();
			//下单更多
			$('#orderFoodMore_a_orderFood').hide();
			//点菜界面的返回
			$('#orderFoodBack_a_orderFood').hide();
		}
			
			
		//渲染数据
		initDeptContent();
		initKitchenContent();
		

		 //第一次加载不成功,延迟来一直加载达到菜品显示出来为止
		(function initFoodContent(){
			//console.log($('#foodsCmp')[0].clientHeight);
			if($('#foodsCmp')[0].clientHeight != 0){
				search({}, 'byDept');
			}else{
				setTimeout(arguments.callee, 500);
			}
		})();
		of.initNewFoodContent();
	
	});
	
	//部门分页
	var deptPagingStart = 0;
	
	 // 初始化部门选择
	function initDeptContent(){
		var allDeptCmpId = new Date().getTime() + '_dept';
		var html = ['<a id="' + allDeptCmpId + '" data-role="button" data-inline="true" class="deptKitBtnFont" data-value="-1" data-type="deptCmp">全部部门</a>'];
		
		//部门列表
		var eachDeptClass = new Date().getTime() + '_deptClass';
		var deptCmpTemplet = '<a data-role="button" data-inline="true" class="deptKitBtnFont ' + eachDeptClass + '" data-type="deptCmp" data-value="{id}" >{name}</a>';
		//真实宽度
		var usefullWidth = document.body.clientWidth - 220;
		//每行显示部门的个数
		var displayDeptCount =  parseInt(usefullWidth / 88);	
		
		var deptPagingLimit = of.depts.root.length > displayDeptCount ? displayDeptCount-1 : displayDeptCount;
		
		var limit = of.depts.root.length >= deptPagingStart + deptPagingLimit ? deptPagingLimit : deptPagingLimit - (deptPagingStart + deptPagingLimit - of.depts.root.length);
		
		
		if(of.depts.root.length > 0){
			for (var i = 0; i < limit; i++) {
				var dName = of.depts.root[deptPagingStart + i].name;
				html.push(deptCmpTemplet.format({
					id : of.depts.root[deptPagingStart + i].id,
					name : dName.length > 4? dName.substring(0, 4) : dName
				}));
			}
		}	
		//显示部门分页按钮
		var deptPrevId = new Date().getTime() + '_prevPage';
		var deptNextId = new Date().getTime() + '_nextPage';
		if(of.depts.root.length > displayDeptCount){
			html.push('<a id="' + deptPrevId + '" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">L</a>' +
					'<a id="' + deptNextId + '" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">R</a>');
		}	
		$("#deptsCmp").html(html.join("")).trigger('create').trigger('refresh');
		
		//全部部门的点击事件
		$('#' + allDeptCmpId ).click(function(){
			initKitchenContent();
			search({}, 'byDept'); 
		});
		
		//部门点击事件
		$('.' + eachDeptClass).each(function(index, element){
			element.onclick = function(){
				initKitchenContent(parseInt(element.getAttribute('data-value')));
				search({deptId : parseInt(element.getAttribute('data-value'))}, 'byDept');
			};
		});
		
		//部门分页下一页	
		$('#' + deptNextId).click(function(){
			deptPagingStart += deptPagingLimit;
			if(deptPagingStart > of.depts.root.length){
				deptPagingStart -= deptPagingLimit;
				return;
			}
			initDeptContent();
		});
	
		//部门分页上一页
		$('#' + deptPrevId).click(function(){
			deptPagingStart -= deptPagingLimit;
			if(deptPagingStart < 0){
				deptPagingStart += deptPagingLimit;
				return;
			}
			initDeptContent();
		});	
		
	}


	/**
	 * 初始化分厨选择
	 * @param c
	 */
    function initKitchenContent(deptId){
		var sl = $('#deptsCmp a[data-type=deptCmp]');
		sl.attr('data-theme', 'c');
		//存放对应部门的厨房
		var kitchenPagingData = null;
		if(deptId != undefined){
			for(var i = 0; i < sl.length; i++){
				if($(sl[i]).attr('data-value') == deptId){
					$(sl[i]).attr('data-theme', 'b');
				}else{
					$(sl[i]).attr('data-theme', 'c');
				}
			}
			kitchenPagingData = [];
			for(var i = 0; i < of.kitchens.root.length; i++){
				var temp = of.kitchens.root[i];
				if(typeof deptId == 'number' && deptId != undefined){
					if(temp.dept.id == deptId){
						kitchenPagingData.push({
							id : temp.id,
							name : temp.name
						});
					}
				}else{
					if(temp.dept.id != -1){
						kitchenPagingData.push({
							id : temp.id,
							name : temp.name
						});
					}
				}
			}
		}else{
			for(var i = 0; i < sl.length; i++){
				if($(sl[i]).attr('data-value') == -1){
					$(sl[i]).attr('data-theme', 'b');
				}else{
					$(sl[i]).attr('data-theme', 'c');
				}
			}
			kitchenPagingData = of.kitchens.root.slice(0);
		}
		
		sl.buttonMarkup( "refresh" );
		
		//显示厨房分页
		showKitchenPaging(0, kitchenPagingData);
		
		closePinyin();
		closeHandWriting();
	}
	
	

	/**
	 * 显示厨房分页
	 */
    function showKitchenPaging(kitchenPagingStart, kitchenPagingData){
		var kc = $("#kitchensCmp");
		var findAllKiechen = new Date().getTime() + '_allKitchen';
		var html = ['<a id="' + findAllKiechen + '" data-role="button" data-inline="true" data-type="kitchenCmp" data-value=-1 class="deptKitBtnFont">全部厨房</a>'];
		
		//真实宽度
		var usefullWidth = document.body.clientWidth - 220;
		//每行显示厨房的个数
		var displayKitchenCount =  parseInt(usefullWidth / 88);
		
		var kitchenPagingLimit = kitchenPagingData.length > displayKitchenCount ? displayKitchenCount-1 : displayKitchenCount;
		
		var limit = kitchenPagingData.length >= kitchenPagingStart + kitchenPagingLimit ? kitchenPagingLimit : kitchenPagingLimit - (kitchenPagingStart + kitchenPagingLimit - kitchenPagingData.length);
		
		var  eachFindKitchenClass = new Date().getTime() + '_eachKitchen';
		//厨房列表
		var kitchenCmpTemplate = '<a data-role="button" data-inline="true" class="deptKitBtnFont ' + eachFindKitchenClass + '" data-type="kitchenCmp" data-value={id} ">{name}</a>';
		
		if(kitchenPagingData.length > 0){
			for (var i = 0; i < limit ; i++) {
				var kName = kitchenPagingData[kitchenPagingStart + i].name;
				html.push(kitchenCmpTemplate.format({
					id : kitchenPagingData[kitchenPagingStart + i].id,
					name : kName.length > 4? kName.substring(0, 4) : kName
				}));
			}
		}
		
		var kitchenPrevId = new Date().getTime() + 'kitchenPrev';
		var kitchenNextId = new Date().getTime() + 'kitchenNext';
		//显示分页按钮
		if(kitchenPagingData.length > displayKitchenCount){
			html.push('<a id="' + kitchenPrevId + '" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">L</a>' +
					'<a id="' + kitchenNextId + '" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">R</a>');
		}	
		kc.html(html.join("")).trigger('create').trigger('refresh');
		
		//FIXME
		$('#kitchensCmp a').each(function(index, element){
			if($(element).attr('data-theme') != 'b'){
				$(element).attr('data-theme', 'c');
			}
		});
		
		$('#kitchensCmp a').attr('data-theme', 'c');
		$('#kitchensCmp a').buttonMarkup( 'refresh' );
		
		//全部厨房点击事件
		$('#' + findAllKiechen).click(function(){
			var dl = $('#deptsCmp a[data-theme=b]');
			search({deptId : parseInt(dl[0].getAttribute('data-value'))}, 'byDept');
			//将所有厨房的样式都变为未选中状态
			$('#kitchensCmp a[data-type=kitchenCmp]').each(function(index, element){
				$(element).attr('data-theme', 'c');
				$(element).buttonMarkup( 'refresh' );
			});
			var sl = $('#kitchensCmp a[data-type=kitchenCmp]:first');		
			sl.attr('data-theme', 'b');
			sl.buttonMarkup( "refresh" );
		});
		
		

		
		//厨房点击事件
		$('.' + eachFindKitchenClass).each(function(index, element){
			element.onclick = function(){
				var dl = $('#deptsCmp a[data-theme=b]');
				var kitchenId = parseInt(element.getAttribute('data-value'));
				var deptId = parseInt(dl[0].getAttribute('data-value'));
				if(kitchenId == -1){
					search({deptId : deptId}, 'byDept');
				}else{
					search({deptId : deptId, kitchenId : kitchenId}, 'byDept');
				}
			
				var sl = $('#kitchensCmp a[data-type=kitchenCmp]');
				sl.attr('data-theme', 'c');
				if(element){
					$(element).attr('data-theme', 'b');
				}

				sl.buttonMarkup( "refresh" );
			
				closePinyin();
				closeHandWriting();
			};
		});
		
		
		//厨房分页上一页
		$('#' + kitchenPrevId).click(function(){
			kitchenPagingStart -= kitchenPagingLimit;
			if(kitchenPagingStart < 0){
				kitchenPagingStart += kitchenPagingLimit;
				return;
			}
			showKitchenPaging(kitchenPagingStart, kitchenPagingData);
		});
		
		// 厨房分页下一页
		$('#' + kitchenNextId).click(function(){
			kitchenPagingStart += kitchenPagingLimit;
			if(kitchenPagingStart > kitchenPagingData.length){
				kitchenPagingStart -= of.kitchenPagingLimit;
				return;
			}
			showKitchenPaging(kitchenPagingStart, kitchenPagingData);
		});
	};

	//快餐模式下牌子号的点击事件
	$('#brand_a_orderFood').click(function(){
		$('#orderFoodByBrandCmp').popup('open');
		$('#brandText_input_orderFood').focus();
		$('#brandText_input_orderFood').val("");
	});
	//输入牌子号的确定的点击事件
	$('#brandSubmit_a_orderFood').click(function(){
		var brandNo;
		if($("#brandText_input_orderFood").val()){
			brandNo = parseInt($("#brandText_input_orderFood").val());
		}else{
			brandNo = 1;
		}
		var bandTemp = tables.slice(0);
		
		//遍历来判断输入的牌子号是否存在
		for(var i = 0; i < bandTemp.length; i++){
			if(brandNo == bandTemp[i].alias){
				of.table = bandTemp[i];
				break;
			}
		}
		
		if(of.table){
			of.submit({
				force : true,
				postSubmit : function(){
					pm.entry({table : of.table});
				}
			});
		}else{
			Util.msg.alert({
				msg : '没有此餐桌号.', 
				topTip : true
			});
		}
			
		
	});
	
	//牌子号的取消按钮
	$('#brandClose_a_orderFood').click(function(){
		$('#orderFoodByBrandCmp').popup('close');
	});

	//普通结账按钮事件
	$('#normalOrderFood_a_orderFood').click(function(){
		of.submit({
			notPrint : false,
			postSubmit : function(){
				uo.entry({table : of.table});
			}
		});
		
	});
	
	//下单并结账按钮事件
	$('#orderPay_li_orderFood').click(function(){
		$('#orderMore_div_orderFood').popup('close');
		of.submit({
			//设置无论从哪个界面进入点菜, 下单后都会去结账界面
			postSubmit : function(){
				pm.entry({table:of.table});
			}
		});
	});
	
	//下单不打印按钮事件
	$('#orderNotPrint_li_orderFood').click(function(){
		$('#orderMore_div_orderFood').popup('close');
		of.submit({
			notPrint : true,
			postSubmit : function(){
				uo.entry({table : of.table});
			}
		});	
	});
	
	
	//先送按钮事件
	$('#orderPre_li_orderFood').click(function(){
		$('#orderMore_div_orderFood').popup('close');
		of.submit({
			notPrint : false,
			postSubmit : function(){
				//清空已点菜
				$('#orderFoodsCmp').html('');
				//清空状态栏
				$('#divDescForCreateOrde div:first').html('');
				$('#orderFoodsCmp').listview('refresh');
				//更新餐台
				if(of.order == null){
					initTableData();
				}
				
				//更新餐台
				$.post('../QueryTable.do', {tableID : of.table.id}, function(result){
					of.table = result.root[0];
				});
				//更新账单
				$.post('../QueryOrderByCalc.do', {tableID : of.table.id}, function(result){
					of.order = result.other.order;
				});
			} 
		});	
	});	
	
	//助记码取消按钮
	$('#closeAlias_a_orderFood').click(function(){
		$('#orderFoodByAliasCmp').popup('close');
	});
	
	
});


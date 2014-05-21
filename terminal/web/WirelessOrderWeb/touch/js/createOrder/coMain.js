/**
 * 
 */
co.show = function(c){
	// 菜品分页包
	if(!co.fp){
		co.fp = new Util.padding({
			renderTo : 'divCFCOAllFood',
			displayId : 'divDescForCreateOrde-padding-msg',
			templet : function(c){
				return Templet.co.boxFood.format({
					dataIndex : c.dataIndex,
					id : c.data.id,
					name : c.data.name,
					unitPrice : c.data.unitPrice,
					click : 'co.insertFood({foodId:'+c.data.id+'})',
					foodState : (c.data.status & 1 << 3) != 0 ? '赠' : (c.data.status & 1 << 2) != 0 ? '停' : '',
					color : (c.data.status & 1 << 3) != 0 ? 'green' : 'FireBrick'
				});
			}
		});
	}
	Util.toggleContentDisplay({
		type:'show', 
		renderTo:'divCreateOrder'
	});
	co.initDeptContent();
	var defaults = $('#divSelectDeptForOrder > div[data-value=-1]');
	defaults[0].click();
	
	$('#divCFCONewFood').css('height', $('#divCenterForCreateOrde').height());
	
	co.table = c.table;
	co.order = typeof c.order != 'undefined' ? c.order : null;
	co.callback = typeof c.callback == 'function' ? c.callback : null;
	$('#divNFCOTableBasicMsg').html('<div>{alias}</div><div>{name}</div>'.format({
		alias : co.table.alias,
		name : co.table.name
	}));
};
/**
 * 清理内存
 */
co.clear = function(){
	Util.toggleContentDisplay({
		type:'hide', 
		renderTo:'divCreateOrder'
	});
	//
	co.table = null;
	co.newFood = [];
	co.callback = null;
	co.initNewFoodContent();
};
/**
 * 菜品操作返回
 */
co.back = function(c){
	if(c && co.newFood.length > 0){
		Util.msg.alert({
			msg : '还有新点菜未处理, 是否继续退出?',
			buttons : 'YESBACK',
			fn : function(btn){
				if(btn == 'yes'){
					co.clear();
				}
			}
		});
	}else{
		co.clear();
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
//	for(var i = 0; i < co.fp.getPageData().length; i++){
//		if(co.fp.getPageData()[i].id == c.foodId){
//			data = co.fp.getPageData()[i];
//			break;
//		}
//	}
	for(var i = 0; i < foodData.root.length; i++){
		if(foodData.root[i].id == c.foodId){
			//返回连接空数组的副本
			data = (foodData.root.concat()[i]);
			break;
		}
	}
	if(data == null){
		alert('添加菜品失败, 程序异常, 请刷新后重试或联系客服人员');
		return;
	}else{
		if((data.status & 1 << 2) != 0){
			Util.msg.alert({
				msg : '此菜品已停售!'
			});
			return;
		}
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
		data.isHangup = false;
		data.tasteGroup = {
			tastePref : '无口味',
			price : 0,
			normalTasteContent : []
		};
		co.newFood.push(data);
	}
	//
	co.initNewFoodContent({
		data : data
	});
	data = null;
	if(typeof c.callback == 'function'){
		c.callback();
	}
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
 * 添加菜品
 */
co.addFood = function(){
	co.operateFoodCount({
		count : 1
	});
};
/**
 * 减少菜品
 */
co.cutFood = function(){
	co.operateFoodCount({
		count : -1
	});
};
/**
 *设置菜品数量
 */
co.setFood = function(){
	co.operateFoodCount({
		otype : 'set'
	});
};
co.saveForSetFood = function(c){
	var count = parseFloat($("#" + inputNumIdUO).val());
	c.data.count = count;
	co.initNewFoodContent({
		data : c.data
	});
	Util.dialongDisplay({
		type : 'hide',
		renderTo : 'divKeyboardPeopleForUO'
	});
	$("#divLeftForKeyboardPeopleForUO > div[class*=isSave]").unbind("click");
	inputNumValUO = "";
	$("#" + inputNumIdUO).val(inputNumValUO);
};

/**
 * 删除菜品
 */
co.deleteFood = function(){
	co.operateFoodCount({
		otype : 'delete'
	});
};
/**
 * 操作菜品数量
 */
co.operateFoodCount = function(c){
	var foodContent = $('#divCFCONewFood > div[class*=div-newFood-select]');
	if(foodContent.length != 1){
		Util.msg.alert({
			msg : '请选中一道菜品'
		});
		return;
	}
	var data = co.newFood[foodContent.attr('data-index')];
	if(typeof c.otype == 'string'){
		if(c.otype.toLowerCase() == 'delete'){
			Util.msg.alert({
				title : '重要',
				msg : '是否删除菜品?',
				buttons : 'YESBACK',
				fn : function(btn){
					if(btn == 'yes'){
						co.newFood.splice(foodContent.attr('data-index'), 1);
						co.initNewFoodContent({
							data : data
						});
					}
				}
			});
			return;
		}else if(c.otype.toLowerCase() == 'set'){
			uo.showdivKeyboardPeopleForUO({type : "setFood", data : data});
			return;
		}
	}else{
		var nc = data.count + c.count;
		if(nc <= 0){
			co.operateFoodCount({
				otype : 'delete'
			});
		}else{
			data.count = nc;
		}
	}
	//
	co.initNewFoodContent({
		data : data
	});
};
/**
 * 叫起
 * params
 *  type: 1:全单叫起 2:单个叫起
 */
co.foodHangup = function(c){
	if(c == null || typeof c.type != 'number'){
		return;
	}
	if(c.type == 1){
		var isHangup = true;
		for(var i = 0; i < co.newFood.length; i++){
//			co.newFood[i].isHangup = typeof co.newFood[i].isHangup != 'boolean' ? true : co.newFood[i].isHangup ? co.newFood[i].isHangup : !co.newFood[i].isHangup;
			if(i == 0){
				isHangup = typeof co.newFood[i].isHangup != 'boolean' ? true : !co.newFood[0].isHangup;
			}
			co.newFood[i].isHangup = isHangup;
		}
		co.initNewFoodContent();
	}else if(c.type == 2){
		var foodContent = $('#divCFCONewFood > div[class*=div-newFood-select]');
		if(foodContent.length != 1){
			Util.msg.alert({
				msg : '请选中一道菜品'
			});
			return;
		}
		var data = co.newFood[foodContent.attr('data-index')];
		data.isHangup = typeof data.isHangup != 'boolean' ? true : !data.isHangup;;
		co.initNewFoodContent({
			data : data
		});
	}
	
};

/*** -------------------------------------------------- ***/

/**
 * 口味操作
 */
co.ot.show = function(c){
	var sf = $('#divCFCONewFood > div[class*=div-newFood-select]');
	if(c.type == 2 && sf.length != 1){
		Util.msg.alert({
			msg : '请选中一道菜品.'
		});
		return;
	}else if(c.type == 1 && co.newFood.length <= 0){
		Util.msg.alert({
			msg : '还未选择菜品.'
		});
		return;
	}
	
	co.ot.allBill = c.type;
	var foodData = typeof co.newFood[sf.attr('data-index')] != 'undefined' ? co.newFood[sf.attr('data-index')] : co.newFood[0];
	if(c.type == 2 && typeof foodData.isTemporary == 'boolean' && foodData.isTemporary){
		Util.msg.alert({
			msg : '临时菜不能选择口味.'
		});
		return;
	}
	co.ot.foodData = foodData;
	
	Util.dialongDisplay({
		type : 'show',
		renderTo : 'divOperateBoxForFoodTaste'
	});
	// 常用口味
	if(typeof co.ot.ctp == 'undefined'){
		co.ot.initBarForCommomTaste();
	}
	// 所有口味
	if(typeof co.ot.atp == 'undefined'){
		co.ot.initBarForAllTaste();
	}
	// 口味组
	if(typeof co.ot.tctp == 'undefined'){
		co.ot.initBarForTasteCategory();
	}
	
	co.ot.changeTaste({
		foodData : co.ot.foodData,
		type : 1,
		event : $('#divCFOTTasteChange > div[data-value=1]'),
		change : true
	});
	//
	co.ot.newTaste = typeof co.ot.foodData.tasteGroup == 'undefined' ? [] : co.ot.foodData.tasteGroup.normalTasteContent.slice(0);
	co.ot.initNewTasteContent();
};
/**
 * 口味操作返回
 */
co.ot.back = function(){
	Util.dialongDisplay({
		type:'hide', 
		renderTo:'divOperateBoxForFoodTaste'
	});
	co.ot.foodData = null;
	co.ot.newTaste = [];
	$('#divCFOTHasTasteContent').html('');
};

/**
 * 切换口味选择
 */
co.ot.changeTaste = function(c){
	if(c == null || typeof c.type != 'number'){
		return;
	}
	var ac = $('#divCFOTTasteChange > div');
	for(var i = 0; i < ac.length; i++){
		$(ac[i]).removeClass('div-deptOrKitchen-select');
	}
	$('#divCFOTTasteChange > div[data-value='+c.type+']').addClass('div-deptOrKitchen-select');
	
	if(c.type == 1){
		// 常用口味
		co.ot.tp = co.ot.ctp;
		$.ajax({
			url : '../QueryFoodTaste.do',
			type : 'post',
			data : {
				foodID : co.ot.foodData.id,
				pin : pin,
				restaurantID : restaurantID
			},
			success : function(data, status, xhr){
				if(data.success && data.root.length > 0){
					co.ot.ctp.init({
						data : data.root.slice(0)
					});
					co.ot.tp.getFirstPage();
				}else{
					co.ot.tp.clearContent();
					if(c.change){
						co.ot.changeTaste({
							type : 2,
							foodData : c.foodData
						});
					}
				}
			},
			error : function(request, status, err){
				alert('加载菜品常用口味数据失败.');
			}
		});
	}else if(c.type == 2){
		// 所有口味
		co.ot.tp = co.ot.atp;
		co.ot.tp.getFirstPage();
	}else if(c.type == 3){
		// 口味组
		co.ot.tp = co.ot.tctp;
		co.ot.tp.getFirstPage();
	}
};
/**
 * 口味选择切换
 */
co.ot.changeTasteCategory = function(c){
	if(c == null || typeof c.tasteId != 'number'){
		return;
	}
	var data = [];
	for(var i = 0; i < co.ot.tctp.data.length; i++){
		if(co.ot.tctp.data[i].id == c.tasteId){
			data = co.ot.tctp.data[i].items;
			break;
		}
	}
	co.ot.tp = new Util.padding({
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
	co.ot.tp.getFirstPage();
};
/**
 * 添加新口味
 */
co.ot.insertTaste = function(c){
	if(c == null || typeof c.tasteId != 'number'){
		return;
	}
	var has = false;
	var data = {};
	for(var i = 0; i < co.ot.tp.getPageData().length; i++){
		if(co.ot.tp.getPageData()[i].taste.id == c.tasteId){
			data = co.ot.tp.getPageData()[i].taste;
			// 
			for(var j = 0; j < co.ot.newTaste.length; j++){
				if(co.ot.newTaste[j].id == data.id){
					has = true;
					break;
				}
			}
			break;
		}
	}
	if(!has){
		co.ot.newTaste.push(data);
		data = null;
	}
	// 
	co.ot.initNewTasteContent();
};
/**
 * 删除口味
 */
co.ot.deleteTaste = function(c){
	if(c == null || typeof c.tasteId != 'number'){
		return;
	}
	for(var i = 0; i < co.ot.newTaste.length; i++){
		if(co.ot.newTaste[i].id == c.tasteId){
			co.ot.newTaste.splice(i, 1);
			break;
		}
	}
	//
	co.ot.initNewTasteContent();
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
co.ot.save = function(c){
	var tasteGroup = co.ot.foodData.tasteGroup;
	
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
	
	var temp = null;
	for(var i = 0; i < co.ot.newTaste.length; i++){
		temp = co.ot.newTaste[i];
		tasteGroup.normalTasteContent.push(temp);
		tasteGroup.normalTaste.name += (i > 0 ? ',' + temp.name : temp.name);
		if(temp.cateStatusValue == 2){
			tasteGroup.normalTaste.price += temp.price;
		}else if(temp.cateStatusValue == 1){
			tasteGroup.normalTaste.price += co.ot.foodData.unitPrice * temp.rate;
		}
	}
	tasteGroup.tastePref = tasteGroup.normalTaste.name;
	tasteGroup.price = tasteGroup.normalTaste.price;
	
	if(co.ot.allBill == 1){
		for(var i = 0; i < co.newFood.length; i++){
			if(typeof co.newFood[i].isTemporary == 'undefined'){
				co.newFood[i].tasteGroup = clone(tasteGroup);
			}
		}
		co.initNewFoodContent();
		
	}else if(co.ot.allBill == 2){
		for(var i = 0; i < co.newFood.length; i++){
			if(co.newFood[i].id == co.ot.foodData.id){
				co.newFood[i].tasteGroup = tasteGroup;
				break;
			}
		}
		co.initNewFoodContent({
			data : co.ot.foodData
		});
	}

	//
	co.ot.back();
	tasteGroup = null;
};

/*** -------------------------------------------------- ***/
/**
 * 账单提交
 */
co.submit = function(c){
	if(co.newFood == null || typeof co.newFood == 'undefined' || co.newFood.length == 0){
		Util.msg.alert({
			title : '温馨提示',
			msg : '请选择菜品后再继续操作.', 
			fn : function(btn){
				
			}
		});
		return;
	}
	
	var foodData = [], isFree = true;
	if(co.table.statusValue == 1){
		isFree = false;
		foodData = co.newFood.slice(0).concat(co.order.orderFoods.slice(0));
	}else{
		isFree = true;
		foodData = co.newFood.slice(0);
	}
	
//	var foods = Wireless.ux.createOrder({orderFoods: (typeof c.commitType != 'undefined'? co.newFood.slice(0) : foodData), dataType : 1});
	
	Util.LM.show();
	
	orderDataModel.tableAlias = co.table.alias;
	orderDataModel.customNum = co.table.customNum;
	orderDataModel.orderFoods = (typeof c.commitType != 'undefined'? co.newFood.slice(0) : foodData);
	orderDataModel.categoryValue =  co.table.categoryValue;
	if(!isFree){
		orderDataModel.id = co.order.id;
		orderDataModel.orderDate = co.order.orderDate;
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
//			Util.LM.hide();
			//下单成功时才出现倒数, 否则提示是否强制提交
			if (data.success == true) {
					if(typeof c.tempPrint != 'undefined'){
						$.ajax({
							url : '../QueryOrderByCalc.do',
							type : 'post',
							data : {
								calc:false,
								serviceRate:0,
								tableID:co.table.alias
							},
							dataType : 'text',
							success : function(results, status, xhr){
								Util.LM.hide();
								results = eval("(" + results + ")");
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
											result = eval("(" + result + ")");
											if(result.success){
												Util.msg.alert({
													title : '提示',
													msg : result.data,
													time : 3,
													fn : function(btn){
														if(co.callback != null && typeof co.callback == 'function'){
															co.callback();
														}
														co.back();
													}
												});
				//								initOrderData({table : uo.table});
											}else{
												Util.msg.alert({
													title : '错误',
													msg : result.data,
													time : 3
												});
											}
										},
										error : function(xhr, status, err){
											Util.LM.hide();
											Util.msg.alert({
												title : '错误',
												msg : err,
												time : 3
											});
										}
									});
								}else{
									Util.msg.alert({
										title : '错误',
										msg : results.data,
										time : 3
									});
								}
							},
							error : function(xhr, status, err){
								Util.LM.hide();
								Util.msg.alert({
									title : '错误',
									msg : err,
									time : 3
								});
							}
						});

					}else{
						Util.LM.hide();
						Util.msg.alert({
							title : data.title,
							msg : data.msg,
							time : 3,
							fn : function(btn){
								if(co.callback != null && typeof co.callback == 'function'){
									co.callback();
								}
								co.back();
							}
						});
					}
			} else {
				Util.msg.alert({
					title : data.title,
					msg : data.msg, 
					buttons : 'YESBACK',
					btnEnter : '继续提交',
					fn : function(btn){
						if(btn == 'yes'){
							c.commitType = 23;
							co.submit(c);
						}else{
							Util.LM.hide();
						}
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
/**
 * 
 * @param {} c
 */
co.setValueToFoodAlias = function (c){
	var alias=getDom('txtFoodAlias');
	if(c.type === 1){
		alias.value=alias.value.substring(0, alias.value.length - 1);
	}else if(c.type === 2){
		alias.value='';
	}else{
		alias.value=alias.value + '' + c.value;
	}
	alias.focus();
};


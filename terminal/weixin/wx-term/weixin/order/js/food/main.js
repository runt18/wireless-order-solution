/**
 * 
 * @param e
 */
function filtersFood(e){
	if($(e).hasClass(params.DNSC)){ return; }
	var kitchenBodyList = $('#divNavKitchen-kitchen > div[data-r=body] > div');
	//
	for(var i = 0; i < kitchenBodyList.length; i++){
		$(kitchenBodyList[i]).removeClass(params.DNSC);
	}
	var kitchenId = parseInt(e.getAttribute('data-value'));
	if(kitchenId > 0){ $(e).addClass(params.DNSC); }
	
	params.kitchenId = kitchenId;
	params.start = 0;
	params.limit = 10;
	var clone = Util.getDom('divOperateFoodPaging').cloneNode();
	Util.getDom('divFoodList').innerHTML = '';
	Util.getDom('divFoodList').appendChild(clone);
	initFoodData({
		callback : function(){
			operateKitchenSearch({otype:'hide'});
		}
	});
}

function filtersFood2(e){
	if($(e).hasClass(params.DNSC)){ return; }
	var kitchenBodyList = $('#ulKitchenList').find('a');
	console.log(kitchenBodyList.length)
	for(var i = 0; i < kitchenBodyList.length; i++){
		$(kitchenBodyList[i]).removeClass(params.DNSC);
	}
	
	var kitchenId = parseInt(e.getAttribute('data-value'));
	console.log(kitchenId)
	if(kitchenId > 0){ $(e).addClass(params.DNSC); }
	
	params.kitchenId = kitchenId;
	params.start = 0;
	params.limit = 10;
	var clone = Util.getDom('divOperateFoodPaging').cloneNode();
	Util.getDom('divFoodList').innerHTML = '';
	Util.getDom('divFoodList').appendChild(clone);
	initFoodData();
}

function operateKitchenSearch(c){
	var temp, html = [], kitchenNav = $('#divNavKitchen'), kitchenBody = $('#divNavKitchen-kitchen > div[data-r=body]')[0];
	if(c.otype == 'show'){
		if(kitchenBody.getAttribute('isInit') == null){
			if(params.kitchenData.length > 0){
				for(var i = 0; i < params.kitchenData.length; i++){
					temp = params.kitchenData[i];
					html.push(Templet.kitchenBox2.format({
						id : temp.id,
						name : temp.name
					}));
				}
				kitchenBody.innerHTML = html.join('');
				kitchenBody.setAttribute('isInit', true),
				temp = null;
				html = null;
			}
		}
		kitchenNav.removeClass('left-nav-hide').addClass('left-nav-show');
	}else if(c.otype == 'hide'){
		kitchenNav.removeClass('left-nav-show').addClass('left-nav-hide');
	}
}

/**
 * 加载菜品分页数据
 */
function loadFoodPaging(){
	params.start += params.limit;
	initFoodData();
}

function saleOrderFood(c){
	c.event.value = "下单√";
	var sl = $('div[class*=box-food-list-r] > div[data-r=r]');
	for(var i = 0; i < sl.length; i++){
		if(parseInt(sl[i].getAttribute('data-value')) == c.id){
			$(sl[i]).addClass("select-food");
			break;
		}
	}	
	operateFood({otype:c.otype, id:c.id, event:c.event});
}

/**
 * 
 * @param c
 * 	{id:foodId,otype:otype}
 */
function operateFood(c){
	var temp, has = false, displayCount = null, calc = true;
	if(typeof c.event != 'undefined'){
		displayCount = $(c.event.parentNode).find('div[data-type=count]');
	}
	
	if(c.otype == 'add' || c.otype == 'plus' || c.otype == 'order'){
		var add = null;
		for(var i = 0; i < params.orderData.length; i++){
			if(params.orderData[i].id == c.id){
				add = params.orderData[i];
				has = true;
				break;
			}
		}
			
		if(c.otype == 'add'){
			calc = changeImg(c.event);
		}
		
		if(has){
			if(c.otype != 'order'){
				if(calc){
					add.count++;
				}else{
					for(var i = 0; i < params.orderData.length; i++){
						temp = params.orderData[i];
						if(temp.id == add.id){
							var tl = $('#divFoodListForSC > div');
							for(var j = 0; j < tl.length; j++){
								if(parseInt(tl[j].getAttribute('data-value')) == temp.id){
									$(tl[j]).remove();
									break;
								}
							}
							params.orderData.splice(i, 1);
							break;
						}
					}					
				}			
			}
		}else{
			add = null;
			for(var i = 0; i < params.foodData.length; i++){
				add = params.foodData[i];
				if(add.id == c.id){
					params.orderData.push({
						id : add.id,
						name : add.name,
						count : 1,
						unitPrice : add.unitPrice,
						desc : add.desc,
						img : add.img
					});
					add = params.orderData[params.orderData.length - 1];
					break;
				}
			}
		}
		if(displayCount){
			displayCount.css('display', 'block').html(add.count);
		}
		add = null;
		//
		displayOrderFoodMsg();
	}else if(c.otype == 'cut'){
		for(var i = 0; i < params.orderData.length; i++){
			temp = params.orderData[i];
			if(temp.id == c.id){
				if(temp.count - 1 == 0){
					Util.dialog.show({
						msg : '是否删除该菜品?',
						callback : function(btn){
							if(btn == 'yes'){
								var tl = $('#divFoodListForSC > div');
								for(var j = 0; j < tl.length; j++){
									if(parseInt(tl[j].getAttribute('data-value')) == temp.id){
										$(tl[j]).remove();
										break;
									}
								}
								params.orderData.splice(i, 1);
								displayOrderFoodMsg();
							}
						}
					});
				}else{
					params.orderData[i].count--;					
					if(displayCount){
						displayCount.css('display', 'block').html(params.orderData[i].count);
					}
				}
				has = true;
				displayOrderFoodMsg();
				break;
			}
		}
		
	}else if(c.otype == 'claer'){
		Util.dialog.show({
			msg : '是否清空已选菜品?',
			callback : function(btn){
				if(btn == 'yes'){
					params.orderData = [];
					operateShoppingCart({otype:'hide'});
					Util.getDom('divFoodListForSC').innerHTML = '';
				}
			}
		});
	}
}
function displayOrderFoodMsg(c){
	var sumCount = 0, sumPrice = 0, temp, has = false;
	for(var i = 0; i < params.orderData.length; i++){
		sumCount += params.orderData[i].count;
		sumPrice += params.orderData[i].unitPrice * params.orderData[i].count;
	}
	Util.getDom('spanSumCountForSC').innerHTML = parseInt(sumCount);
	Util.getDom('spanSumPriceForSC').innerHTML = sumPrice.toFixed(2);
	
	var display = Util.getDom('spanDisplayFoodCount');
				
	if(params.orderData.length > 0){
		display.innerHTML = params.orderData.length;
		display.style.visibility = 'visible';
	}else{
		display.innerHTML = '';
		display.style.visibility = 'hidden';
	}
	
	//var sl = $('div[class*=box-food-list-r] > div:visible[data-type=count]');
	var sl = $('div[class*=box-food-list-r] > div[class*=select-food]');
	for(var i = 0; i < sl.length; i++){
		has = false;
		for(var j = 0; j < params.orderData.length; j++){
			temp = params.orderData[j];
			if(parseInt(sl[i].getAttribute('data-value')) == temp.id){
				has = true;
				$(sl[i]).addClass("select-food");
				break;
			}
		}
		if(!has){
			$(sl[i]).removeClass("select-food");
		}
	}
}

function operateShoppingCart(c){
	var scBox = $('#divShoppingCart'), scMainView = $('#divFoodListForSC');
	var sumCount = 0, sumCountView = $('#spanSumCountForSC');
	var sumPrice = 0, sumPriceView = $('#spanSumPriceForSC');
	var html = [], temp;
	
	if(c.otype == 'show'){
		if(params.orderData.length == 0){
			Util.dialog.show({ msg : '您的购物车没有菜品, 请先选菜.' });
			return;
		}
		if(scBox.hasClass('left-nav-hide')){
			scBox.removeClass('left-nav-hide');			
		}
		scBox.addClass('left-nav-show');
		for(var i = 0; i < params.orderData.length; i++){
			temp = params.orderData[i];
			sumCount += temp.count;
			sumPrice += (temp.unitPrice * temp.count);
			html.push(Templet.shoppingBox.format({
				id : temp.id,
				name : temp.name,
				unitPrice : temp.unitPrice,
				count : temp.count
			}));
		}
		scMainView.html(html.join(''));
		sumCountView.html(parseInt(sumCount));
		sumPriceView.html(sumPrice.toFixed(2));
	}else if(c.otype == 'hide'){
		displayOrderFoodMsg();
		if(scBox.hasClass('left-nav-show')){
			scBox.removeClass('left-nav-show');
		}
		scBox.addClass('left-nav-hide');
		scMainView.html(html.join(''));
	}else if(c.otype == 'confirm'){
//		Util.dialog.show({ msg : '请叫服务员照单下单.' });return;
		
		if(params.orderData.length == 0){
			Util.dialog.show({ msg : '您的购物车没有菜品, 请先选菜.' });
			return;
		}
		
		Util.dialog.show({
			msg : '是否确定下单?',
			callback : function(btn){
				if(btn == 'yes'){
					var foods = "";
					for(var i = 0; i < params.orderData.length; i++){
						temp = params.orderData[i];
						if(i > 0) foods += '&';
						foods += (temp.id + ',' + temp.count);
					}
					$.ajax({
						url : '../../WXOperateOrder.do',
						dataType : 'json',
						type : 'post',
						data : {
							dataSource : 'insertOrder',
							oid : Util.mp.oid,
							fid : Util.mp.fid,
							foods : foods
						},
						success : function(data, status, xhr){
							if(data.success){
								params.orderData = [];
								operateShoppingCart({otype:'hide'});
								Util.getDom('divFoodListForSC').innerHTML = '';
								
								Util.dialog.show({ msg : data.msg + '<br>菜单标识码是: ' + data.other.order.code + '', btn : 'yes' });
							}else{
								Util.dialog.show({ msg : data.msg });
							}
						},
						error : function(xhr, errorType, error){
							Util.dialog.show({ msg : '操作失败, 数据请求发生错误.' });
						}
					});
				}
			}
		});		
		
	}
}

function foodShowAbout(c){
	var box = $('#divFoodShowAbout');
	if(c.otype == 'show'){
		box.removeClass('left-nav-hide').addClass('left-nav-show');
		var temp;
		for(var i = 0; i < params.foodData.length; i++){
			temp = params.foodData[i];
			if(temp.id == c.id){
				box.find('div:first-child > div[data-type=img]').css({
					'background' : 'url({0})'.format(temp.img.image),
					'background-size' : '100% 100%'
				});
				//box.find('div[data-region=msg]').html('&nbsp;'+ temp.name + '&nbsp;&nbsp;&nbsp;&nbsp;￥'+ temp.unitPrice);
				//box.find('div[data-region=desc]').html(typeof temp.desc == 'string' && temp.desc.trim().length > 0 ? '&nbsp;&nbsp;&nbsp;&nbsp;'+temp.desc : '暂无简介');
				break;
			}
		}
	}else if(c.otype == 'hide'){
		box.removeClass('left-nav-show').addClass('left-nav-hide');
	}
}


function PickFoodComponent(param){
	
	param = param || {
		orderDataCount : null,
		callback : '',
	};
	
	//总的div
	var _container = null;
	//厨房数据
	var _kitchenData = [];
	//厨房id
	var _kitchenId = null;
	//菜品数据
	var _foodData = [];
	//订单数据
	var _orderData = [];
	
	this.open = function(){
		$('#WXCmp_div_member').hide();
		//渲染
		createWxPickFood();
		//设置开始样式
		window.onresize = setView;
		window.onresize();
		//加载厨房
		initKitchen();
	};
	
	this.close = function(){
		$('#WXCmp_div_member').show();
		if(_container){
			_container.remove();
			_container = null;
		}
		
	};
	
	this.openShopping = function(){
		openShoppingCar({otype:'show'});
	};
	
	//渲染
	function createWxPickFood(){
		_container = $('<div/>');
		
		
		var shoppingCarList = '<div id="shadowForPopup" style="display:none;opacity:0; position: absolute; top:0; left:0; width: 100%;background: #DDD;display: none;" ></div>'
			+'<div id="divShoppingCart" class="box-vertical">'
		  	+'<div id="divFoodListForSC"></div>'
		  	+'<div id="divSumForSC">'
		  		+'菜品数量:&nbsp;<span id="spanSumCountForSC">--.--</span> &nbsp;&nbsp;'
		  		+'合计:&nbsp;<span id="spanSumPriceForSC">--.--</span>'
		  	+'</div>'
			+'<div id="divButtonForSC" class="hewarp">'
				+'<ul>'
					+'<li style="width: 33.33%">'
						+'<a href="javascript:operateFood();"> '
							+'<i class="foundicon-remove fcolor1"></i>清&nbsp;空'
						+'</a>'
					+'</li>'
					+'<li style="width: 33.33%">'
						+'<a href="javascript:orderOrTakeout(isTakeout);">'
							+'<i class="foundicon-checkmark fcolor2"></i>选好了'
						+'</a>'
					+'</li>'
					+'<li style="width: 33.33%">'
						+'<a href="javascript:operateShoppingCart();">'
							+'<i class="foundicon-left-arrow fcolor3"></i>返&nbsp;回'
						+'</a>'
					+'</li>'
				+'</ul>'
			+'</div>'
		+'</div>';

		var kitchenList = '<div id="divMainView" class="box-vertical">'
					  +'<section class="panel" id="panelNav">'
						  +'<ul class="nav-aside" id="ulKitchenList">'
							  +'<li class="li-k-title"><a href="#">全部厨房</a></li>'
						  +'</ul>'
					  +'</section>'
		
					  +'<div id="divFoodList" class="div-full" style="margin-left: 80px;">'
					  	  +'<div id="divOperateFoodPaging">&nbsp;</div>'
					  +'</div>'
		
					
				+'</div>';
		
		var foodAbout = '<div id="divFoodShowAbout">'
			+'<div data-region="img" class="d-fsa-img">'
			+'<div data-type="img" style="position: relative;">'
			+'<div class="recommend-food-detail">'
				+'&nbsp;<span id="recommendFoodName" data-type="n">菜名</span>'
				+'<span id="recommendFoodPrice" data-type="p" style="margin-right: 5px; float: right; color: #ff6600">￥88</span>'
			+'</div>'
			+'<div class="recommend-food-order" foodType="saleOrderFood">下单</div>'
			+'</div>'
			+'</div>'
			+'<div data-region=desc class="d-fsa-desc">暂无简介</div>'
		
			+'<div data-type="closeFoodAbout" data-region="mask" class="d-fsa-mask div-full"></div>'
		+'</div>';
		
		

		
		_container.append(shoppingCarList).append(kitchenList).append(foodAbout);
		//点击其他处关闭展示菜品详情
		_container.find('[data-type="closeFoodAbout"]').each(function(index, element){
			element.onclick = function(){
				showFoodAbout({otype : 'hide'});
			};
		});
		
		_container.after($('#bottom'));
		$('body').append(_container);
	}
	
	function getOrderFoodCount(id){
		var count = 0 ;
		for(var i = 0; i < _orderData.length; i++){
			if(_orderData[i].id == id){
				count = _orderData[i].count;
			}
		}
		return count;
	}
	
	//设置样式
	function setView(){
		var height = document.documentElement.clientHeight;
		$('#divMainView').css('height', height-45);
		$('#divShoppingCart').css('height', height-45);
		$('#divFoodShowAbout').css('height', height);
		$('#shadowForPopup').css('height', height-45);
	}
	//加载厨房数据
	function initKitchen(){
		var temp = null;
		var ketchenHtml = [];
		var kitchenList = $('#ulKitchenList');
		var kitchenBox = '<li><a data-value="{id}" data-type="kitchenBox" class={star}>{name}</a></li>';
		Util.lm.show();
		$.ajax({
			url : '../../WXQueryDept.do',
			dataType : 'json',
			type : 'post',
			data : {
				dataSource : 'kitchen',
				fid : Util.mp.params.r
			},
			success : function(data, status, xhr){
				Util.lm.hide();
				_kitchenData = data.root;
				if(_kitchenData.length > 0){
					//默认显示加载第一个厨房的菜品数据
					_kitchenId = _kitchenData[0].id;
					//加载菜品数据
					initFoodData(_kitchenId);
					for(var i = 0; i < _kitchenData.length; i++){
						temp = _kitchenData[i];
						ketchenHtml.push(kitchenBox.format({
							id : temp.id,
							//如果是明星菜则用橙色背景, 否则第一个默认选中用灰色背景.
							star : temp.id == -10 ? 'star-kitchen-name' : (i == 0 ?'divNavKitchen-item-select':''),
							name : temp.name.substring(0, 4)
						}));
					}
				}
				kitchenList.html(ketchenHtml.join(''));
				kitchenList.find('[data-type="kitchenBox"]').each(function(index, element){
					element.onclick = function(){
						filtersFood(element);
					}
				});
			}
		});
	}
	
	//点击厨房筛选菜品
	function filtersFood(e){
		if($(e).hasClass('divNavKitchen-item-select')){
			return;
		}
		var kitchenBodyList = $('#ulKitchenList').find('a');
		for(var i =0; i < kitchenBodyList.length; i++){
			$(kitchenBodyList[i]).removeClass('divNavKitchen-item-select');
		}
		
		
		if(eachKitchenId > -11){ 
			$(e).addClass('divNavKitchen-item-select');
		}
		
		_kitchenId = eachKitchenId;
		
		var clone = Util.getDom('divOperateFoodPaging').cloneNode();
		Util.getDom('divFoodList').innerHTML = '';
		Util.getDom('divFoodList').appendChild(clone);
		initFoodData(_kitchenId);
	}
	
	//加载菜品
	function initFoodData(kitchenId){
		var foodBox = '<div class="box-horizontal box-food-list">'
			+ '<div data-r="l">'
				+ '<div food-id={foodId} foodStype="image" style="background-image: url({img});"></div>'
			+ '</div>'
			+ '<div data-r="c" class="box-food-list-c" food-id={foodId} foodStype="word">'
				+ '<div data-r="t"><b>{name}</b></div>'
				+ '<div data-r="m"><span>￥{unitPrice}</span></div>'
				+ '<div data-r="b" class={orderAction}><font>{foodCnt}</font>人点过</div>'
			+ '</div>'
			+ '<div data-r="r" class="box-horizontal box-food-list-r">'
				+ '<div data-r="r" food-id={foodId} {selected} data-type="checkbox"></div>'
			+ '</div>'
			+ '</div>';
		var requestParams = {
				dataSource : 'normal',
				fid : Util.mp.fid,
				kitchenId : typeof kitchenId != 'undefined' ? kitchenId : -1,
		};
		
		//判断是否明星菜
		if(kitchenId && kitchenId == -10){
			requestParams.dataSource = 'isRecommend';
		}
		
		Util.lm.show();
		
		$.ajax({
			url : '../../WXQueryFood.do',
			dataType : 'json',
			type : 'post',
			data : requestParams,
			success : function(data, status, xhr){
				_foodData = data.root;
				Util.lm.hide();
				if(data.root.length > 0){
					var foodHtml = [];
					var count = null;
					var temp = null;
					for(var i = 0; i < _foodData.length; i++){
//						count = getOrderFoodCount(_foodData.id);
						foodHtml.push(foodBox.format({
							foodId : _foodData[i].id,
							img : _foodData[i].img.thumbnail,
							name : (_foodData[i].name.length > 7 ? _foodData[i].name.substring(0, 6) + "…" : _foodData[i].name),
							unitPrice : _foodData[i].unitPrice,
							foodCnt : parseInt(_foodData[i].foodCnt),
							count : count,
							selected : count > 0 ? 'class="select-food"' : '',
							display : count > 0 ? 'block' : 'none',
							orderAction : _foodData[i].foodCnt > 0 ? '' : 'html-hide'
						}));
					}
					Util.getDom('divOperateFoodPaging').insertAdjacentHTML('beforeBegin', foodHtml.join(''));
					
					//图片点击展示菜品详情
					$('#divFoodList').find('[foodStype="image"]').each(function(index, element){
						element.onclick = function(){
							showFoodAbout({id : $(element).attr('food-id'), otype : 'show', event : element});
						};
					});
					
					//点击中间
					$('#divFoodList').find('[foodStype="word"]').each(function(index, element){
						element.onclick = function(){
							operateFood({otype : 'add', id : $($(element).next().find('div[data-r=r]')[0]).attr('food-id'), event : $(element).next().find('div[data-r=r]')[0]});
						};
					});
					
					//点击check
					$('#divFoodList').find('[data-type="checkbox"]').each(function(index, element){
						element.onclick =function(){
							 operateFood({otype : 'add', id : $(element).attr('food-id'), event : element});
						};
					});
					
					
				}else{
					Util.getDom('divOperateFoodPaging').innerHTML = '没有记录.';
				}
			},
			error : function(xhr, status, error){
				Util.lm.hide();
				Util.dialog.show({msg : '加载菜品失败'});
			}
		});
	}
	
	//显示菜品详情
	function showFoodAbout(c){
		var aboutBox = $('#divFoodShowAbout');
		if(c.otype == 'show'){
			aboutBox.find('div:first-child > div[data-type=img] > div[class=recommend-food-order]').html('下单');
			aboutBox.find('div:first-child > div[data-type=img] > div[class=recommend-food-order]').click(function(){
				saleOrderFood({otype:"order", id:c.id, event:this});
			});
			for(var i = 0; i < _orderData.length; i++){
				if(_orderData[i].id == c.id){
					aboutBox.find('div:first-child > div[data-type=img] > div[class=recommend-food-order]').html('下单√');
					break;
				}
			}
			aboutBox.removeClass('left-nav-hide').addClass('left-nav-show');
			for(var i= 0; i < _foodData.length; i++){
				if(_foodData[i].id == c.id){
					aboutBox.find('div:first-child > div[data-type=img]').css({
						'background' : 'url({0})'.format(_foodData[i].img.image),
						'background-size' : '100% 100%'
					});
					$('#recommendFoodPrice').text('￥' + _foodData[i].unitPrice);
					$('#recommendFoodName').text(_foodData[i].name);
					
					aboutBox.find('div[data-region=desc]').html(typeof _foodData[i].desc == 'string' && _foodData[i].desc.trim().length > 0 ? _foodData[i].desc : '暂无简介');
					break;
				}
			}
		}else if(c.otype == 'hide'){
			aboutBox.removeClass('left-nav-show').addClass('left-nav-hide');
		}
	}
	
	
	//菜品详情下单
	function saleOrderFood(c){
		c.event.innerHTML = "下单√";
		var sl = $('div[class*=box-food-list-r] > div[data-r=r]');
		for(var i = 0; i < sl.length; i++){
			if(parseInt($(sl[i]).attr('food-id')) == c.id){
				$(sl[i]).addClass("select-food");
				break;
			}
		}	
		showFoodAbout({otype : 'hide'});
		operateFood({otype : c.otype, id : c.id, event : c.event});
	}
	
	//添加进购物车
	function operateFood(c){
		var displayCount = null;
		var calc = null;
		var temp = null;
		if(c.event != 'undefined'){
			//购物车上方数量
			displayCount = $(c.event.parentNode).find('div[data-type=count]');
		}
		
		if(c.otype == 'add' || c.otype == 'plus' || c.otype == 'order'){
			var add = null;
			for(var i = 0; i < _orderData.length; i++){
				if(_orderData[i].id == c.id){
					add = _orderData[i];
					break;
				}
			}
			
			if(c.otype == 'add'){
				calc = changeImg(c.event);
			}
			
			if(add){
				if(c.otype !== 'order'){
					if(calc){
						add.count++;
					}else{
						for(var i = 0; i < _orderData.length; i++){
							temp = _orderData[i];
							if(temp.id == add.id){
								var tl = $('#divFoodListForSC > div');
								for(var j = 0; j < tl.length; j++){
									if(parseInt(tl[j].getAttribute('data-value')) == temp.id){
										$(tl[j]).remove();
										break;
									}
								}
								_orderData.splice(i, 1);
								break;
							}
						}
					}
				}
			}else{
				for(var i = 0; i < _foodData.length; i++){
					add = _foodData[i];
					if(add.id == c.id){
						_orderData.push({
							id : add.id,
							name : add.name,
							count : 1,
							unitPrice : add.unitPrice,
							desc : add.desc,
							img : add.img
						});
						add = _orderData[_orderData.length - 1];
						break;
					}
				}
			}
			if(displayCount){
				displayCount.css('display', 'block').html(add.count);
			}
			
			displayOrderFoodMsg();
		}else if(c.otype == 'cut'){
			for(var i = 0; i <  _orderData.length; i++){
				temp = _orderData[i];
				if(temp.id == c.id){
					if(temp.count - 1 == 0){
						Util.dialog.show({
							msg : '是否删除该菜品',
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
									
									//刷新购物车界面, 没有菜品时隐藏
									if(params.orderData.length > 0){
										$('#divShoppingCart').height('auto');
									}else{
										operateShoppingCart({event:this, otype:'hide'});
									}
									
								}
							}
						});
					}else{
						_orderData[i].count--;					
						if(displayCount){
							displayCount.css('display', 'block').html(params.orderData[i].count);
						}
					}
					displayOrderFoodMsg();
					break;
				}
			}
		}else if(c.otype == 'clear'){
			Util.dialog.show({
				msg : '是否清空已选菜品?',
				callback : function(btn){
					if(btn == 'yes'){
						//清空数据
						var li = $('.select-food');

						if(li.length > 0){
							li.each(function(e){
								$(this).removeClass("select-food");
							});
						}
						_orderData.length = 0;
						$(param.orderDataCount).html('');
						operateShoppingCart({otype:'hide'});
					}
				}
			});
		}
	}
	
	function changeImg(e){
		if($(e).hasClass("select-food")){
			$(e).removeClass("select-food");
			return false;
		}else{
			$(e).addClass("select-food");
			return true;
		}
	}
	
	//刷新购物车菜品
	function displayOrderFoodMsg(){
		var sumPrice = 0;
		var temp = null;
		var display = null;
		for(var i = 0; i < _orderData.length; i++){
			sumPrice += _orderData[i].unitPrice * _orderData[i].count;
		}
		
		//点菜
		Util.getDom('spanSumCountForSC').innerHTML = _orderData.length;
		Util.getDom('spanSumPriceForSC').innerHTML = sumPrice.toFixed(2);
		
		display = param.orderDataCount;
		if(_orderData.length > 0){
			display.innerHTML = _orderData.length;
			display.style.visibility = 'visible';
		}else{
			display.innerHTML = '';
			display.style.visibility = 'hidden';
		}
		
		var sl = $('div[class*=box-food-list-r] > div[class*=select-food]');
		for(var i = 0; i < sl.length; i++){
			for(var j = 0; j < _orderData.length; j++){
				temp = _orderData[j];
				if(parseInt(sl[i].getAttribute('data-value')) == temp.id){
					$(sl[i]).addClass("select-food");
					break;
				}
			}
		}
	}
	
	//打开购物车
	
	function openShoppingCar(c){
		console.log(_orderData);
		var shoppingBox = '<div data-value="{id}" class="div-fl-f-sc-box box-horizontal">'
			+ '<div data-type="msg" class="div-full">'
				+ '<div><b>{name}</b></div>'
				+ '<div>价格: <span>￥{unitPrice}</span></div>'
			+ '</div>'
			+ '<div data-type="cut" onclick="operateFood({otype:\'cut\', id:{id}, event:this})">-</div>'
			+ '<div data-type="count">{count}</div>'
			+ '<div data-type="plus" onclick="operateFood({otype:\'plus\', id:{id}, event:this})">+</div>'
			+ '</div>';
		var shoppingCarBox = $('#divShoppingCart');
		var shoppingCarMainView = $('#divFoodListForSC');
		var sumCountView = $('#spanSumCountForSC');
		var sumPrice = 0, sumPriceView = $('#spanSumPriceForSC');
		var html = [];
		var temp = null;
		var generalHeight = 87, foodHeight = 51;
		if(c.otype == 'show'){
			if(_orderData.length == 0){
				Util.dialog.show({ msg : '您的购物车没有菜品, 请先选菜.', btn : 'yes'});
				return;
			}
			
			$('#divShoppingCart').show();
			$('#shadowForPopup').show();
			if(shoppingCarBox.hasClass('left-nav-hide')){
				shoppingCarBox.removeClass('left-nav-hide');			
			}
			shoppingCarBox.addClass('left-nav-show');
			
			for(var i = 0; i < _orderData.length; i++){
				temp = _orderData[i];
				sumPrice += (temp.unitPrice * temp.count);
				html.push(shoppingBox.format({
					id : temp.id,
					name : temp.name,
					unitPrice : temp.unitPrice,
					count :temp.count
				}));
			}
			
			shoppingCarMainView.html(html.join(''));
			sumCountView.html(_orderData.length);
			sumPriceView.html(sumPrice.toFixed(2));
			
			//当菜品列表高过屏幕高度时, 固定列表div的高度
			shoppingCarMainView.height('auto');
			shoppingCarBox.height(generalHeight + _orderData.length * foodHeight);
			$('#shadowForPopup').height($('#shadowForPopup').height() - shoppingCarBox.height());
		}else if(c.otype = 'hide'){
			if(shoppingCarBox.hasClass('left-nav-show')){
				shoppingCarBox.removeClass('left-nav-show');
			}
			shoppingCarBox.addClass('left-nav-hide');
			$('#divShoppingCart').hide();
			shoppingCarMainView.html(html.join(''));		
		}
		
		
		
	}
	
	
	
	
	
}
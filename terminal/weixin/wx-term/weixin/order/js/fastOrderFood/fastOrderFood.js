function PickFoodComponent(param){
	
	param = param || {
		orderDataCount : null,
		confirm : function(selectedFoods, comment){},
		onCartChange : function(selectedFoods){}
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
	//备注信息
	var _commentData = null;
	
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
	
	this.refresh = function(){
		displayOrderFoodMsg();
	};
	
	this.closeShopping = function(){
		openShoppingCar({otype:'hide'});
	};
	
	//渲染
	function createWxPickFood(){
		_container = $('<div/>');
		
		
		var shoppingCarList = '<div id="shadowForPopup_div_fastOrderFood" style="height:188px;display:none;opacity:0; position: absolute; top:0; left:0; width: 100%;background: #DDD;display: none;" ></div>'
			+'<div id="shoppingCart_div_fastOrderFood" class="box-vertical">'
		  	+'<div id="shoppingCartFood_div_fastOrderFood"></div>'
		  	+'<div id="shoppingFoodsum_div_fastOrderFood">'
		  		+'菜品数量:&nbsp;<span id="sumCount_span_fastOrderFood">--.--</span> &nbsp;&nbsp;'
		  		+'合计:&nbsp;<span id="sumPrice_div_fastOrderFood">--.--</span>'
		  	+'</div>'
		  	+'<div id="cartComment_div_fastOrderFood"> </div>'
			+'<div id="shoppingBtn_div_fastOrderFood" class="hewarp">'
				+'<ul>'
					+'<li style="width: 33%" id="shoppingCarBack_li_fastOrderFood">'
						+'<a href="#">'
							+'<i class="foundicon-left-arrow fcolor3"></i>返&nbsp;回'
						+'</a>'
					+'</li>'
					+'<li style="width: 33%" id="shoppingCarSelect_li_fastOrderFood">'
						+'<a href="#">'
							+'<i class="foundicon-checkmark fcolor2"></i>下单'
						+'</a>'
					+'</li>'
					+'<li style="width: 33%">'
						+'<a href="#" id="commentClick_a_fastOrderFood"> '
							+'<i class="foundicon-edit fcolor1"></i>备&nbsp;注'
						+'</a>'
					+'</li>'
				+'</ul>'
			+'</div>'
		+'</div>';

		var kitchenList = '<div id="mainView_div_fastOrderFood" class="box-vertical">'
					  +'<section class="panel">'
						  +'<ul class="nav-aside" id="kitchenList_ul_fastOrderFood">'
							  +'<li class="li-k-title"><a href="#">全部厨房</a></li>'
						  +'</ul>'
					  +'</section>'
		
					  +'<div id="foods_div_fastOrderFood" class="div-full" style="margin-left: 80px;">'
					  	  +'<div id="foodList_div_fastOrderFood">&nbsp;</div>'
					  +'</div>'
		
					
				+'</div>';
		
		var foodAbout = '<div id="foodShowAbout_div_fastOrderFood">'
			+'<div data-region="img" class="d-fsa-img">'
			+'<div data-type="img" style="position: relative;">'
			+'<div class="recommend-food-detail">'
				+'&nbsp;<span id="recommendFoodName_span_fastOrderFood" data-type="n">菜名</span>'
				+'<span id="recommendFoodPrice_div_fastOrderFood" data-type="p" style="margin-right: 5px; float: right; color: #ff6600">￥88</span>'
			+'</div>'
			+'<div class="recommend-food-order" href="#" foodType="saleOrderFood">下单</div>'
			+'</div>'
			+'</div>'
			+'<div data-region=desc class="d-fsa-desc">暂无简介</div>'
		
			+'<div data-type="closeFoodAbout" data-region="mask" class="d-fsa-mask div-full"></div>'
		+'</div>';
		
		var comment = '<div id="comment_div_fastOrderFood">'
			+'<div data-region="img" class="d-sss-img">'
			+'<div data-type="img" style="position: relative;">'
			+'<div class="commend-food-detail">'
				+'&nbsp;<span id="recommendFoodName_span_fastOrderFood" >请输入备注</span>'
			+'</div>'
			+'<div align="center">'
			+ '<textarea id="textarea_textarea_fastOrderFood" style="width:98%;height:104px;font-size:16px;"></textarea>'
			+'</div>'
			+'<div class="commend-food-order" href="#" id="commentSave_div_fastOrderFood">保存</div>'
			+'</div>'
			+'</div>'
			+'<div data-type="closeComment" data-region="mask" class="d-fsa-mask div-full"></div>'
		+'</div>';
		
		

		
		_container.append(shoppingCarList).append(kitchenList).append(foodAbout).append(comment);
		//点击其他处关闭展示菜品详情
		_container.find('[data-type="closeFoodAbout"]').each(function(index, element){
			element.onclick = function(){
				showFoodAbout({otype : 'hide'});
			};
		});
		
		//点击其他处关闭备注
		_container.find('[data-type="closeComment"]').each(function(index, element){
			element.onclick = function(){
					$('#comment_div_fastOrderFood').removeClass('left-nav-show').addClass('left-nav-hide');
			};
		});
		
		
		//购物车内的返回
		_container.find('[id="shoppingCarBack_li_fastOrderFood"]').click(function(){
			openShoppingCar({otype : 'hide'});
		});
		
		//购物车的备注
		_container.find('[id="commentClick_a_fastOrderFood"]').click(function(){
			$('#comment_div_fastOrderFood').removeClass('left-nav-hide').addClass('left-nav-show');
			openShoppingCar({otype : 'hide'});
			$('#commentSave_div_fastOrderFood').click(function(){
				_commentData = $('#textarea_textarea_fastOrderFood').val();
				$('#comment_div_fastOrderFood').removeClass('left-nav-show').addClass('left-nav-hide');
				openShoppingCar({otype : 'show'});
			})
		});
		
		//购物车的选好了
		_container.find('[id="shoppingCarSelect_li_fastOrderFood"]').click(function(){
			param.confirm(_orderData, _commentData);
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
		$('#mainView_div_fastOrderFood').css('height', height-45);
		$('#shoppingCart_div_fastOrderFood').css('height', height-45);
		$('#foodShowAbout_div_fastOrderFood').css('height', height);
		$('#comment_div_fastOrderFood').css('height', height);
		$('#shadowForPopup_div_fastOrderFood').css('height', height-45);
	}
	//加载厨房数据
	function initKitchen(){
		var temp = null;
		var ketchenHtml = [];
		var kitchenList = $('#kitchenList_ul_fastOrderFood');
		var kitchenBox = '<li><a data-value="{id}" href="#" data-type="kitchenBox" class={star}>{name}<div style="text-align:right;" id="kitchenFoodAmount_div_fastOrderFood"></div></a></li>';
		Util.lm.show();
		$.ajax({
			url : '../../WxQueryDept.do', 
			dataType : 'json',
			type : 'post',
			data : {
				dataSource : 'kitchen',
				fid : Util.mp.fid,
				oid : Util.mp.oid
			},
			success : function(data, status, xhr){
				Util.lm.hide();
				_kitchenData = data.root;
				if(_kitchenData.length > 0){
					//默认显示加载第一个厨房的菜品数据
					_kitchenId = _kitchenData[0].id;
					_kitchenData.forEach(function(e, index){
						var star;
						if(e.id == -10){//明星菜
							star = 'star-kitchen-name';
						}else if(e.id == -9){//我的最爱
							star = 'favor-kitchen-name';
						}else if(e.id == -8){//为我推荐
							star = 'recommend-kitchen-name';
						}else{
							star = (index == 0 ? 'divNavKitchen-item-select' : '');
						}
						ketchenHtml.push(kitchenBox.format({
							id : e.id,
							//如果是明星菜则用橙色背景, 否则第一个默认选中用灰色背景.
							star : star,
							name : e.name.substring(0, 4)
						}));
						
					});
					//加载菜品数据
					initFoodData(_kitchenId);
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
		var kitchenBodyList = $('#kitchenList_ul_fastOrderFood').find('a');
		for(var i =0; i < kitchenBodyList.length; i++){
			$(kitchenBodyList[i]).removeClass('divNavKitchen-item-select');
		}
		
		var eachKitchenId = parseInt(e.getAttribute('data-value'));
		if(eachKitchenId > -11){ 
			$(e).addClass('divNavKitchen-item-select');
		}
		
		_kitchenId = eachKitchenId;
		
		var clone = Util.getDom('foodList_div_fastOrderFood').cloneNode();
		Util.getDom('foods_div_fastOrderFood').innerHTML = '';
		Util.getDom('foods_div_fastOrderFood').appendChild(clone);
		initFoodData(_kitchenId);
		
	}
	
	//加载菜品
	function initFoodData(kitchenId){
		var foodBox ='<div data-type="eachFood_div_fastOrderFood" class="box-food-list">'
			+ '<div class="box-horizontal ">'
			+ '<div data-r="l">'
				+ '<div food-id={foodId} foodStype="image" href="#" noImage={hasImage} style="background-image: url({img});"></div>'
			+ '</div>'
			+ '<div data-r="c" class="box-food-list-c" food-id={foodId} href="#" foodStype="word">'
				+ '<div data-r="t"><b>{name}</b></div>'
				+ '<div data-r="m"><span>{unitPrice}</span></div>'
				+ '<div data-r="b" class={orderAction}><font>{foodCnt}</font>人点过</div>'
			+ '</div>'
			+ '<div data-r="r" class="box-horizontal box-food-list-r">'
				+ '<div data-r="r" food-id={foodId} {selected} data-type="checkbox" href="#"></div>'
			+ '</div>'
			+'</div>'
			+ '<div data-type="standard_div_fastOrderFood" style="width:100%" class="box-horizontal">{unitPriceHtml}</div>'
			+ '</div>';
			
		var unitPrice = '<div multiUnit-Id={multiId} class="unit_css_fastOrderFood" data-type="unitPrice" href="#">'
						+ '<ul class="m-b-list" style="margin-top:10px;font-size:13px;"><a data-type="unitPrice_a_fastOrderFood">{unitPrice}</a>&nbsp;/&nbsp;{unitName}</ul>'
						+ '</div>';
			
				
			
		var requestParams = {
			fid : Util.mp.fid,
			oid : Util.mp.oid,
			start : 0,
			limit : 20,
			kitchenId : typeof kitchenId != 'undefined' ? kitchenId : -1
		};
		
		if(kitchenId){
			if(kitchenId == -10){
				//明星菜
				requestParams.dataSource = 'star';
			}else if(kitchenId == -9){
				//我的最爱
				requestParams.dataSource = 'favor';
			}else if(kitchenId == -8){
				//向你推荐
				requestParams.dataSource = 'recommend';
			}else{
				requestParams.dataSource = 'normal';
			}
		}
		
		Util.lm.show();
		
		$.ajax({
			url : '../../WxQueryFood.do',
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
						count = getOrderFoodCount(_foodData[i].id);
						var multiUnit = [];	
						if(_foodData[i].multiUnitPrice.length > 0){
							_foodData[i].multiUnitPrice.forEach(function(e, index){
								multiUnit.push(unitPrice.format({
									multiId : e.id,
									unitPrice : e.price,
									unitName : e.unit
								}));
							});
						}
						
						var noImage = 'images/noImage.jpg';
						
						foodHtml.push(foodBox.format({
							foodId : _foodData[i].id,
							img : _foodData[i].img ? _foodData[i].img.thumbnail : noImage,
							hasImage : _foodData[i].img ? 'no' : 'yes',
							name : (_foodData[i].name.length > 7 ? _foodData[i].name.substring(0, 6) + "…" : _foodData[i].name),
							unitPrice :  (_foodData[i].status & (1 << 4)) != 0 ? '时价' : '￥' + _foodData[i].unitPrice,
							foodCnt : parseInt(_foodData[i].foodCnt),
							count : count,
							selected : count > 0 ? 'class="select-food"' : '',
							display : count > 0 ? 'block' : 'none',
							orderAction : _foodData[i].foodCnt > 0 ? '' : 'html-hide',
							unitPriceHtml : multiUnit.slice(0, 3).join('')
						}));
						
					} 
					
					Util.getDom('foodList_div_fastOrderFood').insertAdjacentHTML('beforeBegin', foodHtml.join(''));
					
					
					
					
					//多规格的点击事件
					$('#foods_div_fastOrderFood').find('[data-type="eachFood_div_fastOrderFood"]').each(function(index, element){
						$(element).find('[data-type="unitPrice"]:first').addClass('selectUnitPrice_css_fastOrderFood');
						if($(element).find('[data-type="unitPrice"]:first').hasClass('selectUnitPrice_css_fastOrderFood')){
							$(element).find('[data-r="m"]').html('<span>￥' + parseInt($($(element).find('[data-type="unitPrice"]:first')).find('[data-type="unitPrice_a_fastOrderFood"]').text()) + '</span>')
						}
						$(element).find('[data-type="unitPrice"]').each(function(index, unitPriceElement){
							unitPriceElement.onclick = function(){
								if($(unitPriceElement).hasClass('selectUnitPrice_css_fastOrderFood')){
									$(unitPriceElement).addClass('selectUnitPrice_css_fastOrderFood');
								}else{
									$(element).find('[data-type="unitPrice"]').removeClass('selectUnitPrice_css_fastOrderFood');
									$(unitPriceElement).addClass('selectUnitPrice_css_fastOrderFood');
									$(unitPriceElement).parent().parent().find('[data-r="m"]').html('<span>￥' + parseInt($(unitPriceElement).find('[data-type="unitPrice_a_fastOrderFood"]').text()) + '</span>');
								}
							}
						});
					});
					
					
					//图片点击展示菜品详情
					$('#foods_div_fastOrderFood').find('[foodStype="image"]').each(function(index, element){
						if($(element).attr('noimage') == 'no'){
							element.onclick = function(){
								console.log(element);
								showFoodAbout({id : $(element).attr('food-id'), otype : 'show', event : element});
							};
						}
					});
					
					//点击中间
					$('#foods_div_fastOrderFood').find('[foodStype="word"]').each(function(index, element){
						element.onclick = function(){
							operateFood({otype : 'add', id : $($(element).next().find('div[data-r=r]')[0]).attr('food-id'), event : $(element).next().find('div[data-r=r]')[0]});
							if(param.onCartChange && typeof param.onCartChange == 'function'){
								param.onCartChange(_orderData);
						 	}
							onDeptChange(_orderData);
						};
					});
					
					//点击check
					$('#foods_div_fastOrderFood').find('[data-type="checkbox"]').each(function(index, element){
						element.onclick =function(){
							 operateFood({otype : 'add', id : $(element).attr('food-id'), event : element});
							 if(param.onCartChange && typeof param.onCartChange == 'function'){
									param.onCartChange(_orderData);
							 	}
							 onDeptChange(_orderData);
						};
					});
					
				}else{
					Util.getDom('foodList_div_fastOrderFood').innerHTML = '没有记录.';
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
		var aboutBox = $('#foodShowAbout_div_fastOrderFood');
		var unitPrice = null;
		if(c.otype == 'show'){
			aboutBox.find('div:first-child > div[data-type=img] > div[class=recommend-food-order]').html('下单');
			aboutBox.find('div:first-child > div[data-type=img] > div[class=recommend-food-order]').click(function(){
				saleOrderFood({otype:"order", id:c.id, event:this});
				if(param.onCartChange && typeof param.onCartChange == 'function'){
					param.onCartChange(_orderData);
			 	}
				onDeptChange(_orderData);
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
					
					var unit = $(c.event).parent().parent().parent().find('[data-type="unitPrice"]')
					for(var j = 0; j < unit.length; j++){
						if($(unit[j]).hasClass('selectUnitPrice_css_fastOrderFood')){
							for(var k = 0; k < _foodData[i].multiUnitPrice.length; k++){
								if($(unit[j]).attr('multiUnit-Id') == _foodData[i].multiUnitPrice[k].id){
									unitPrice = _foodData[i].multiUnitPrice[k].price;
								}
							}
						}
					}
					if(_foodData[i].multiUnitPrice.length > 0){
						$('#recommendFoodPrice_div_fastOrderFood').text('￥' + unitPrice);
					}else{
						$('#recommendFoodPrice_div_fastOrderFood').text('￥' + _foodData[i].unitPrice);
					}
					
					$('#recommendFoodName_span_fastOrderFood').text(_foodData[i].name);
					
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
		var calc = true;
		var temp = null;
		if(typeof c.event != 'undefined'){
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
								var tl = $('#shoppingCartFood_div_fastOrderFood > div');
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
					var selectUnit = $(c.event).parent().parent().parent().find('[data-type="unitPrice"]')
					var selectUnitPrice = null;
					var unitPriceId = null;
					var selectUnitName = null;
					for(var k = 0; k < selectUnit.length; k++){
						if($(selectUnit[k]).hasClass('selectUnitPrice_css_fastOrderFood')){
							selectUnitPrice = $(selectUnit[k]).find('[data-type="unitPrice_a_fastOrderFood"]').text();
							selectUnitName = $(selectUnit[k]).find('[data-type="unitPrice_a_fastOrderFood"]').parent().text();
							unitPriceId = $($(selectUnit[k]).find('[data-type="unitPrice_a_fastOrderFood"]')).parent().parent().attr('multiUnit-Id');
						}
					}
					if(add.id == c.id){
						_orderData.push({
							id : add.id,
							name : add.name,
							count : 1,
							unitPrice : add.unitPrice,
							desc : add.desc,
							img : add.img,
							kitchenId : add.kitchen.id,
							multiUnitPrice : add.multiUnitPrice,
							selectUnitPrice : selectUnitPrice,
							selectUnitName : selectUnitName,
							unitPriceId : unitPriceId
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
									var tl = $('#shoppingCartFood_div_fastOrderFood > div');
									var sl = $('div[class*=box-food-list-r] > div[class*=select-food]');
									for(var j = 0; j < tl.length; j++){
										if(parseInt(tl[j].getAttribute('data-value')) == temp.id){
											$(tl[j]).remove();
											break;
										}
									}
									
									for(var k = 0; k < sl.length; k++){
										if(parseInt(sl[k].getAttribute('food-id')) == temp.id){
											$(sl[k]).removeClass("select-food");
											break;
										}
									}
									
									_orderData.splice(i, 1);
									
									displayOrderFoodMsg();
									
									//刷新购物车界面, 没有菜品时隐藏
									if(_orderData.length > 0){
										$('#shoppingCart_div_fastOrderFood').height('auto');
									}else{
										openShoppingCar({event:this, otype:'hide'});
									}
									onDeptChange(_orderData);
								}
							}
						});
					}else{
						_orderData[i].count--;					
						if(displayCount){
							displayCount.css('display', 'block').html(_orderData[i].count);
						}
					}
					displayOrderFoodMsg();
					break;
				}
			}
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
	
	//刷新厨房显示菜品数量
	function onDeptChange(orderFoodData){
		
		 
		 var kitchen = $('#kitchenList_ul_fastOrderFood').find('[data-type="kitchenBox"]');
		 for(var i = 0; i < kitchen.length; i++){
			 var selectFoods = [];
			 for(var j = 0; j < orderFoodData.length; j++){
				 if($(kitchen[i]).attr('data-value') == orderFoodData[j].kitchenId){
					 selectFoods.push(orderFoodData[j]);
				 }
			 }
			 if(selectFoods.length > 0 ){
				 $(kitchen[i]).find('[id="kitchenFoodAmount_div_fastOrderFood"]').html('&nbsp' + selectFoods.length);
				 $(kitchen[i]).find('[id="kitchenFoodAmount_div_fastOrderFood"]').css({'visibility' : 'visible'});
			 }else{
				 $(kitchen[i]).find('[id="kitchenFoodAmount_div_fastOrderFood"]').html('');
				 $(kitchen[i]).find('[id="kitchenFoodAmount_div_fastOrderFood"]').css({'visibility' : 'hidden'});
			 }

		 }
		 
	}
	
	//刷新购物车菜品
	function displayOrderFoodMsg(){
		var sumPrice = 0;
		var temp = null;
		var count = 0;
		for(var i = 0; i < _orderData.length; i++){
			
			temp = _orderData[i];
			if(temp.selectUnitPrice != null){
				sumPrice += (temp.selectUnitPrice * temp.count);
			}else{
				sumPrice += (temp.unitPrice * temp.count);
			}
			
			count += _orderData[i].count;
		}
		
		//点菜
		Util.getDom('sumCount_span_fastOrderFood').innerHTML = count;
		Util.getDom('sumPrice_div_fastOrderFood').innerHTML = sumPrice.toFixed(2);

		if(param.onCartChange && typeof param.onCartChange == 'function'){
			param.onCartChange(_orderData);
		}
		
		var sl = $('div[class*=box-food-list-r] > div[class*=select-food]');
		for(var i = 0; i < sl.length; i++){
			for(var j = 0; j < _orderData.length; j++){
				temp = _orderData[j];
				if(parseInt(sl[i].getAttribute('food-id')) == temp.id){
					$(sl[i]).addClass("select-food");
					break;
				}
			}
		}
		
		
	}
	
	//打开购物车
	
	function openShoppingCar(c){
		var shoppingBox = '<div data-value="{id}" class="div-fl-f-sc-box box-horizontal">'
			+ '<div data-type="msg" class="div-full">'
				+ '<div><b>{name}</b></div>'
				+ '<div>价格: <span>￥{unitPrice}</span></div>'
			+ '</div>'
			+ '<div data-type="cut" shoppingFoodId={id}>-</div>'
			+ '<div data-type="count">{count}</div>'
			+ '<div data-type="plus" shoppingFoodId={id}>+</div>'
			+ '</div>';
		var shoppingCarBox = $('#shoppingCart_div_fastOrderFood');
		var shoppingCarMainView = $('#shoppingCartFood_div_fastOrderFood');
		var sumCountView = $('#sumCount_span_fastOrderFood');
		var sumPrice = 0, sumPriceView = $('#sumPrice_div_fastOrderFood');
		var html = [];
		var temp = null;
		var generalHeight = 87, foodHeight = 51;
		if(c.otype == 'show'){
			if(_orderData.length == 0){
				Util.dialog.show({ msg : '您的购物车没有菜品, 请先选菜.', btn : 'yes'});
				return;
			}
			
			$('#shoppingCart_div_fastOrderFood').show();
			$('#shadowForPopup_div_fastOrderFood').show();
			if(shoppingCarBox.hasClass('left-nav-hide')){
				shoppingCarBox.removeClass('left-nav-hide');			
			}
			shoppingCarBox.addClass('left-nav-show');
			
			
			
			for(var i = 0; i < _orderData.length; i++){
				temp = _orderData[i];
				if(temp.selectUnitPrice != null){
					sumPrice += (temp.selectUnitPrice * temp.count);
				}else{
					sumPrice += (temp.unitPrice * temp.count);
				}
				
				
				html.push(shoppingBox.format({
					id : temp.id,
					name : temp.name,
					unitPrice : temp.selectUnitName != null ? temp.selectUnitName : temp.unitPrice,
					count :temp.count
				}));
			}
			
			if(_commentData){
				$('#cartComment_div_fastOrderFood').html('<font size="3px">备注:<font color="#26a9d0">' +_commentData + '</font></font>');
			}else{
				$('#cartComment_div_fastOrderFood').html('');
			}
			
			shoppingCarMainView.html(html.join(''));
			//购物车里面的减号
			shoppingCarMainView.find('[data-type="cut"]').each(function(index, element){
				element.onclick = function(){
					operateFood({otype:'cut', id : $(element).attr('shoppingFoodId'), event : element});
				};
			});
			
			//购物车里面的加号
			shoppingCarMainView.find('[data-type="plus"]').each(function(index, element){
				element.onclick = function(){
					operateFood({otype : 'plus', id : $(element).attr('shoppingFoodId'), event : element});
				}
			});
			
			sumCountView.html(_orderData.length);
			sumPriceView.html(sumPrice.toFixed(2));
			
			//当菜品列表高过屏幕高度时, 固定列表div的高度
			shoppingCarMainView.height('auto');
			shoppingCarBox.height(generalHeight + _orderData.length * foodHeight);
			$('#shadowForPopup_div_fastOrderFood').height($('#shadowForPopup_div_fastOrderFood').height() - shoppingCarBox.height());
		}else if(c.otype = 'hide'){
			if(shoppingCarBox.hasClass('left-nav-show')){
				shoppingCarBox.removeClass('left-nav-show');
			}
			$('#shadowForPopup_div_fastOrderFood').hide();
			shoppingCarBox.addClass('left-nav-hide');
			shoppingCarMainView.html(html.join(''));		
		}
		
	}
	
	
	
	
	
}
function PickFoodComponent(param){
	
	param = param || {
		renderTo : '',
		callback : ''
	}
	
	
	//厨房数据
	var _kitchenData = [];
	//厨房id
	var _kitchenId = null;
	
	this.open = function(){
		$('#WXCmp_div_member').hide();
		createWxPickFood(param.renderTo);
		//设置开始样式
		window.onresize = setView;
		window.onresize();
		//加载厨房
		initKitchen();
	};
	
	this.close = function(){
		$('#WXCmp_div_member').show();
		$('#pickFoodCmp_div_member').hide();
	};
	
	//渲染
	function createWxPickFood(renderTo){
		var shoppingCarList = '<div id="divShoppingCart" class="box-vertical">'
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
		
					+'<div id="divBottomNav" class="hewarp">'
						+'<ul class="swiper-wrapper">'
							+'<li class="index swiper-slide">'
								+'<a> '
									+'<i class="foundicon-home fcolor6"></i>首&nbsp;页'
								+'</a>'
							+'</li>'
							+'<li class="user swiper-slide">'
								+'<a id="foodOrderList" href="javascript:linkToOrders();"> '
									+'<i class="foundicon-star fcolor1"></i><font id="food_order">我的订单</font>'
								+'</a>'
							+'</li>'
							+'<li class="special swiper-slide">'
								+'<a title="购物车"> '
									+'<i class="foundicon-cart fcolor2"><div id="spanDisplayFoodCount"></div></i>购物车'
								+'</a>'
							+'</li>'
							+'<li class="user swiper-slide">'
								+'<a href="javascript:window.location.reload();" title="刷新"> '
									+'<i class="foundicon-refresh fcolor3"></i>刷&nbsp;新'
								+'</a>'
							+'</li>'
							+'<li class="special swiper-slide">'
						+'</ul>'
					+'</div>'
				+'</div>';
		
		var foodAbout = '<div id="divFoodShowAbout">'
			+'<div data-region="img" class="d-fsa-img">'
			+'<div data-type="img" style="position: relative;">'
			+'<div class="recommend-food-detail">'
				+'&nbsp;<span id="recommendFoodName" data-type="n">菜名</span>'
				+'<span id="recommendFoodPrice" data-type="p" style="margin-right: 5px; float: right; color: #ff6600">￥88</span>'
			+'</div>'
			+'<div class="recommend-food-order" onclick="saleOrderFood()">下单</div>'
			+'</div>'
			+'</div>'
			+'<div data-region=desc class="d-fsa-desc">暂无简介</div>'
		
			+'<div data-region="mask" class="d-fsa-mask div-full"onclick="foodShowAbout()"></div>'
		+'</div>';
		
		
		var navBottomSwiper = new Swiper('#divBottomNav', {
		paginationClickable : true,
		slidesPerView : 'auto',
		disableAutoResize : true
		});
		var li = $('#divBottomNav li');
		var tw = $('#divBottomNav').width();
		tw = li.length > 4 ? parseInt(tw / 4) : parseInt(tw / li.length);
		for (var i = 0; i < li.length; i++) {
		li[i].style.width = tw + 'px';
		}
		
		renderTo.append(shoppingCarList).append(kitchenList).append(foodAbout);
	}
	
	//设置样式
	function setView(){
		var height = document.documentElement.clientHeight;
		$('#divMainView').css('height', height);
		$('#divShoppingCart').css('height', height);
		$('#divFoodShowAbout').css('height', height);
	}
	
	//加载厨房数据
	function initKitchen(){
		var temp = null;
		var ketchenHtml = [];
		var kitchenList = $('#ulKitchenList');
		var kitchenBox = '<li><a data-value="{id}" onclick="filtersFood2(this)" class={star}>{name}</a></li>';
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
					_kitchenId = _kitchenData[0].id;
					//TODO 
					//加载菜品数据
					
					for(var i = 0; i < _kitchenData.length; i++){
						temp = _kitchenData[i];
						ketchenHtml.push(kitchenBox.format({
							id : temp.id,
							//如果是明星菜则用橙色背景, 否则第一个默认选中用灰色背景
							star : temp.id == -10 ? 'star-kitchen-name' : (i == 0 ?'divNavKitchen-item-select':''),
							name : temp.name.substring(0, 4)
						}));
					}
				}
				kitchenList.html(ketchenHtml.join(''));
			}
		});
	}
	
	//加载菜品
	function initFoodData(){
		
	}
	
	
	
	
}
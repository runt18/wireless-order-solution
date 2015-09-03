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
	if(kitchenId > 0){ 
		$(e).addClass(params.DNSC); 
	}
	
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

//点击厨房测试筛选菜品
function filtersFood2(e){
	if($(e).hasClass(params.DNSC)){ return; }
	var kitchenBodyList = $('#ulKitchenList').find('a');
	for(var i = 0; i < kitchenBodyList.length; i++){
		$(kitchenBodyList[i]).removeClass(params.DNSC);
	}
	
	var kitchenId = parseInt(e.getAttribute('data-value'));
	//明星菜是-10
	if(kitchenId > -11){ $(e).addClass(params.DNSC); }
	
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
	c.event.innerHTML = "下单√";
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
								
								//有外卖模块时
								if(true){
									var tol = $('#divFoodListForTO > div');
									for(var j = 0; j < tol.length; j++){
										if(parseInt(tol[j].getAttribute('data-value')) == temp.id){
											$(tol[j]).remove();
											break;
										}
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
					//清空数据
					var li = $('.select-food');

					if(li.length > 0){
						li.each(function(e){
							$(this).removeClass("select-food");
						});
					}
					params.orderData.length = 0;
					$('#spanDisplayFoodCount').html('');
					operateShoppingCart({otype:'hide'});
				}
			}
		});
	}
}
//刷新菜品列表信息
function displayOrderFoodMsg(c){
	var sumPrice = 0, temp, has = false;
	for(var i = 0; i < params.orderData.length; i++){
		sumPrice += params.orderData[i].unitPrice * params.orderData[i].count;
	}
	//点菜
	Util.getDom('spanSumCountForSC').innerHTML = params.orderData.length;
	Util.getDom('spanSumPriceForSC').innerHTML = sumPrice.toFixed(2);
	
	//外卖
	Util.getDom('spanSumCountForTO').innerHTML = params.orderData.length;
	Util.getDom('spanSumPriceForTO').innerHTML = sumPrice.toFixed(2);	
	
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


function orderOrTakeout(otype){
	
	if(otype){//外卖
		operateShoppingCart({otype:'show', takeout:true});
	}else{//店内
		operateShoppingCart({otype:'confirm'});
	}
}

function operateShoppingCart(c){
	var scBox = $('#divShoppingCart'), scMainView = $('#divFoodListForSC');
	var sumCountView = $('#spanSumCountForSC');
	var sumPrice = 0, sumPriceView = $('#spanSumPriceForSC');
	
	//如果是外卖模块时
	if(c.takeout){
		scBox = $('#divTakeout'), scMainView = $('#divFoodListForTO');
		sumCountView = $('#spanSumCountForTO');
		sumPrice = 0, sumPriceView = $('#spanSumPriceForTO');
	}
	var html = [], temp;
	
	if(c.otype == 'show'){
		
		if(params.orderData.length == 0){
			Util.dialog.show({ msg : '您的购物车没有菜品, 请先选菜.', btn : 'yes'});
			return;
		}
		
		if(c.takeout){
			$('#divTakeout').show();
			if(scBox.hasClass('right-nav-hide')){
				scBox.removeClass('right-nav-hide');			
			}
			scBox.addClass('right-nav-show');	
			operateShoppingCart({otype : 'hide'});
		}else{
			$('#divShoppingCart').show();
			if(scBox.hasClass('left-nav-hide')){
				scBox.removeClass('left-nav-hide');			
			}
			scBox.addClass('left-nav-show');			
		}


		for(var i = 0; i < params.orderData.length; i++){
			temp = params.orderData[i];
			sumPrice += (temp.unitPrice * temp.count);
			html.push(Templet.shoppingBox.format({
				id : temp.id,
				name : temp.name,
				unitPrice : temp.unitPrice,
				count : temp.count
			}));
		}
		scMainView.html(html.join(''));
		sumCountView.html(params.orderData.length);
		sumPriceView.html(sumPrice.toFixed(2));
		
		
		if(!c.takeout){
			//当菜品列表高过屏幕高度时, 固定列表div的高度
			if((generalHeight + params.orderData.length * foodHeight) > htmlHeight){
				scBox.height(htmlHeight);
				scMainView.height(htmlHeight - generalHeight);
			}else{
				scMainView.height('auto');
				scBox.height(generalHeight + params.orderData.length * foodHeight);
			}			
		}
		shopCartInit = true;
	}else if(c.otype == 'hide'){
		//是否外卖界面
		if(c.takeout){
			if(scBox.hasClass('right-nav-show')){
				scBox.removeClass('right-nav-show');
			}
			scBox.addClass('right-nav-hide');
			$('#divTakeout').hide();
			showOtherAddress({otype: 'other', showHas:true});
		}else{
			if(shopCartInit){
				if(scBox.hasClass('left-nav-show')){
					scBox.removeClass('left-nav-show');
				}
				scBox.addClass('left-nav-hide');
				$('#divShoppingCart').hide();
				scMainView.html(html.join(''));		
			}			
		}
	}else if(c.otype == 'confirm'){
		if(params.orderData.length == 0){
			Util.dialog.show({ msg : '您的购物车没有菜品, 请先选菜.', btn : 'yes'});
			return;
		}
		
		Util.lm.show();
		
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
				Util.lm.hide();
				if(data.success){
					params.orderData = [];
					//刷新界面
					displayOrderFoodMsg();
					operateShoppingCart({otype:'hide'});
					//清空列表
					Util.getDom('divFoodListForSC').innerHTML = '';
					Util.getDom('divFoodListForTO').innerHTML = '';
					
					Util.dialog.show({title : '请呼叫服务员确认订单', msg : '<font style="font-weight:bold;font-size:25px;">订单号: ' + data.other.order.code + '</font>', btn : 'yes' });
				}else{
					Util.dialog.show({ msg : data.msg });
				}
			},
			error : function(xhr, errorType, error){
				Util.lm.hide();
				Util.dialog.show({ msg : '操作失败, 数据请求发生错误.' });
			}
		});
		
	}
}

function foodShowAbout(c){
	var box = $('#divFoodShowAbout');
	if(c.otype == 'show'){
		box.find('div:first-child > div[data-type=img] > div[class=recommend-food-order]').html('下单');
		box.find('div:first-child > div[data-type=img] > div[class=recommend-food-order]').click(function(){
			saleOrderFood({otype:"order", id:c.id, event:this});
		});
		for(var i = 0; i < params.orderData.length; i++){
			if(params.orderData[i].id == c.id){
				box.find('div:first-child > div[data-type=img] > div[class=recommend-food-order]').html('下单√');
				break;
			}
		}			
		
		box.removeClass('left-nav-hide').addClass('left-nav-show');
		var temp;
		for(var i = 0; i < params.foodData.length; i++){
			temp = params.foodData[i];
			if(temp.id == c.id){
				box.find('div:first-child > div[data-type=img]').css({
					'background' : 'url({0})'.format(temp.img.image),
					'background-size' : '100% 100%'
				});
				$('#recommendFoodPrice').text('￥' + temp.unitPrice);
				$('#recommendFoodName').text(temp.name);
				
				box.find('div[data-region=desc]').html(typeof temp.desc == 'string' && temp.desc.trim().length > 0 ? temp.desc : '暂无简介');
				break;
			}
		}
	
	}else if(c.otype == 'hide'){
		box.removeClass('left-nav-show').addClass('left-nav-hide');
	}
}

function selectAddress(thiz){
	for (var i = 0; i < $('.takeout_address_added').length; i++) {
		var address = $($('.takeout_address_added')[i]);
		if(!address.hasClass('takeout_address_unselect')){
			address.addClass('takeout_address_unselect');
		}
	}
	//样式选中
	$(thiz).removeClass('takeout_address_unselect');
	//设置已有地址
	to.defaultAddress = $(thiz).attr('data-value');
}

//加载外卖地址
function loadCustomerAddress(){
	$('#divAddressList').show();
	var html=[];
	for (var i = 0; i < to.customerContects.length; i++) {
		var temp = to.customerContects[i];
		html.push(Templet.contectBox.format({
			id : temp.id,
			name : temp.name,
			phone : temp.phone,
			address : temp.address,
			hidden : (temp.isDefault == true?'':'takeout_address_hidden takeout_address_unselect')
		}));		
		if(temp.isDefault){
			to.defaultAddress = temp.id;
		}
	}
	
	$('#divAddressList').html(html.join('') + '<div id="takeoutOtherId" align="right" class="takeout_other_address"><span onclick="showOtherAddress({event:this, otype:\'other\'})">→使用其他地址</span> </div>'
											+ '<div id="takeoutNewId" align="right" class="takeout_other_address takeout_address_hidden"><span onclick="showOtherAddress({event:this, otype:\'new\'})">+填写并使用新的地址</span> </div>');
}

//地址操作
function showOtherAddress(c){
	if(c.otype == 'other'){
		if(to.customerContects.length == 1 && !c.showHas){
			showOtherAddress({otype : 'new'});
			return ;
		}
		
		to.useNewAddress = false;
		//隐藏其他地址按钮
		$('#takeoutOtherId').removeClass('takeout_other_address');
		$('#takeoutOtherId').hide();
		//显示所有的地址
		$('.takeout_address_added').css('display', 'block');	
		//显示添加按钮
		$('#takeoutNewId').removeClass('takeout_address_hidden');
		$('#takeoutNewId').addClass('takeout_other_address');
	}else if(c.otype == 'new'){
		to.useNewAddress = true;
		//隐藏添加新地址
		$(c.event).parent().removeClass('takeout_other_address');
		$(c.event).hide();		
		//显示填写地址
		$('#divNewAddress4TO').show();
		//隐藏已有地址
		$('#divAddressList').hide();
		//显示选回原有地址按钮
		$('#takeoutOtherId').addClass('takeout_other_address');
		$('#takeoutOtherId').show();		
//		$('.takeout_address_added').addClass('takeout_address_unselect');
	}
}

//外卖提交
to.takeoutCommit = function(){
	var name = $('#to_name').val();
	var address = $('#to_address').val();
	var phone = $('#to_phone').val();	
	//支付方式, 默认货到付款
	var payment  = 1;
	
	if(to.useNewAddress){
		var reg = /^(139|138|137|136|135|134|147|150|151|152|157|158|159|182|183|187|188|130|131|132|155|156|185|186|145|133|153|180|181|189)\d{8}$/;
		
		if(!address || !phone){
			Util.dialog.show({ msg : '请填写正确的地址和电话', btn:'yes' });
			return;
		}
		  
	    if(phone.length != 11 || reg.test(phone) == false){
			Util.dialog.show({ msg : '请填写正确手机号码', btn:'yes' });
			return;
	    }
	}
	
	Util.lm.show();
	
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
			dataSource : 'takeoutCommit',
			oid : Util.mp.oid,
			fid : Util.mp.fid,
			foods : foods,
			payment : payment ,
			oldAddress : to.defaultAddress,
			name : name,
			phone : phone,
			address : address
		},
		success : function(data, status, xhr){
			Util.lm.hide();
			if(data.success){
				params.orderData = [];
				//刷新界面
				displayOrderFoodMsg();
				operateShoppingCart({otype:'hide', takeout:true});
				
				//清空购物车
				Util.getDom('divFoodListForSC').innerHTML = '';
				Util.getDom('divFoodListForTO').innerHTML = '';
				
				Util.dialog.show({title : '温馨提示', msg : '下单成功, 可以在我的外卖中查看', btn : 'yes' });
				$('#foodOrderList').prepend('<img id="imgNewOrderTip" src="images/WXnew.gif" style="float : right;"></img>');
			}else{
				Util.dialog.show({ msg : data.msg, btn : 'yes'});
			}
		},
		error : function(xhr, errorType, error){
			Util.lm.hide();
			Util.dialog.show({ msg : '操作失败, 数据请求发生错误.' });
		}
	});
	
}

//查看订单
function linkToOrders(){
	//传入账单类型
	Util.jump('orderList.html', Util.mp.extra);
}
